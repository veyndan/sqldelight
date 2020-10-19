/*
 * Copyright (C) 2017 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.sqldelight.core.lang

import com.alecstrong.sql.psi.core.psi.Queryable
import com.alecstrong.sql.psi.core.psi.SqlBindExpr
import com.alecstrong.sql.psi.core.psi.SqlColumnDef
import com.intellij.psi.util.PsiTreeUtil
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.sqldelight.core.compiler.integration.adapterName
import com.squareup.sqldelight.core.dialect.api.DialectType
import com.squareup.sqldelight.core.lang.psi.ColumnTypeMixin
import com.squareup.sqldelight.core.lang.util.isArrayParameter

/**
 * Internal representation for a column type, which has SQLite data affinity as well as JVM class
 * type.
 */
internal data class IntermediateType(
  val dialectType: DialectType,
  val javaType: TypeName = dialectType.javaType,
  /**
   * The column definition this type is sourced from, or null if there is none.
   */
  val column: SqlColumnDef? = null,
  /**
   * The name of this intermediate type as exposed in the generated api.
   */
  val name: String = "value",
  /**
   * The original bind argument expression this intermediate type comes from.
   */
  val bindArg: SqlBindExpr? = null,
  /**
   * Whether or not this argument is extracted from a different type
   */
  val extracted: Boolean = false
) {
  fun asNullable() = copy(javaType = javaType.copy(nullable = true))

  fun asNonNullable() = copy(javaType = javaType.copy(nullable = false))

  fun nullableIf(predicate: Boolean): IntermediateType {
    return if (predicate) asNullable() else asNonNullable()
  }

  fun argumentType() = if (bindArg?.isArrayParameter() == true) {
    Collection::class.asClassName().parameterizedBy(javaType)
  } else {
    javaType
  }

  /**
   * @return A [CodeBlock] which binds this type to [columnIndex] on [STATEMENT_NAME].
   *
   * eg: statement.bindBytes(0, queryWrapper.tableNameAdapter.columnNameAdapter.encode(column))
   */
  fun preparedStatementBinder(
    columnIndex: String
  ): CodeBlock {
    var value = CodeBlock.of(this.name)

    (defaultColumnAdapter() ?: columnAdapter())?.let {
      value = if (javaType.isNullable) {
        CodeBlock.of("%L?.let { %L.encode(it) }", value, it)
      } else {
        CodeBlock.of("%L.encode(%L)", it, value)
      }
    }

    return dialectType.prepareStatementBinder(columnIndex, value)
  }

  fun cursorGetter(columnIndex: Int): CodeBlock {
    var cursorGetter = dialectType.cursorGetter(columnIndex)

    if (!javaType.isNullable) {
      cursorGetter = CodeBlock.of("$cursorGetter!!")
    }

   (defaultColumnAdapter() ?: columnAdapter())?.let {
      cursorGetter = if (javaType.isNullable) {
        CodeBlock.of("%L?.let(%L::decode)", cursorGetter, it)
      } else {
        CodeBlock.of("%L.decode(%L)", it, cursorGetter)
      }
    }

    return cursorGetter
  }

  private fun columnAdapter(): CodeBlock? = (column?.columnType as ColumnTypeMixin?)?.adapter()?.let { adapter ->
    val adapterName = PsiTreeUtil.getParentOfType(column, Queryable::class.java)!!.tableExposed().adapterName
    CodeBlock.of("$CUSTOM_DATABASE_NAME.$adapterName.%N", adapter)
  }

  private fun defaultColumnAdapter(): CodeBlock? {
    return JAVA_TYPE_TO_COLUMN_ADAPTER_TYPE[javaType.copy(nullable = false)]?.let { CodeBlock.of("%T", it) }
  }
}

private val JAVA_TYPE_TO_COLUMN_ADAPTER_TYPE = mapOf(
  FLOAT to ClassName("com.squareup.sqldelight", "FloatColumnAdapter"),
  BYTE to ClassName("com.squareup.sqldelight", "ByteColumnAdapter"),
  SHORT to ClassName("com.squareup.sqldelight", "ShortColumnAdapter"),
  INT to ClassName("com.squareup.sqldelight", "IntColumnAdapter"),
  BOOLEAN to ClassName("com.squareup.sqldelight", "BooleanColumnAdapter")
)

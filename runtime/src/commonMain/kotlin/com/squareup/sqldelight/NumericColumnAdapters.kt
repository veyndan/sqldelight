package com.squareup.sqldelight

/** A [ColumnAdapter] which maps a [Float] to a [Double] in the database. */
object FloatColumnAdapter : ColumnAdapter<Float, Double> {
  override fun decode(databaseValue: Double): Float = databaseValue.toFloat()

  override fun encode(value: Float): Double = value.toDouble()
}

/** A [ColumnAdapter] which maps a [Byte] to a [Long] in the database. */
object ByteColumnAdapter : ColumnAdapter<Byte, Long> {
  override fun decode(databaseValue: Long): Byte = databaseValue.toByte()

  override fun encode(value: Byte): Long = value.toLong()
}

/** A [ColumnAdapter] which maps a [Short] to a [Long] in the database. */
object ShortColumnAdapter : ColumnAdapter<Short, Long> {
  override fun decode(databaseValue: Long): Short = databaseValue.toShort()

  override fun encode(value: Short): Long = value.toLong()
}

/** A [ColumnAdapter] which maps a [Int] to a [Long] in the database. */
object IntColumnAdapter : ColumnAdapter<Int, Long> {
  override fun decode(databaseValue: Long): Int = databaseValue.toInt()

  override fun encode(value: Int): Long = value.toLong()
}

/** A [ColumnAdapter] which maps a [Boolean] to a [Long] in the database. */
object BooleanColumnAdapter : ColumnAdapter<Boolean, Long> {
  override fun decode(databaseValue: Long): Boolean = databaseValue == 1L

  override fun encode(value: Boolean): Long = if (value) 1L else 0L
}

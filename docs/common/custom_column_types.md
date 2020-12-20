## Custom Column Types

If you'd like to retrieve columns as custom types you can specify a Kotlin type:

```sql
import kotlin.String;
import kotlin.collections.List;

CREATE TABLE hockeyPlayer (
  cup_wins TEXT AS List<String> NOT NULL
);
```

However, creating the `Database` will require you to provide a `ColumnAdapter` which knows how to map between the database type and your custom type:

```kotlin
val listOfStringsAdapter = object : ColumnAdapter<List<String>, String> {
  override fun decode(databaseValue: String) =
    if (databaseValue.isEmpty()) {
      listOf()
    } else {
      databaseValue.split(",")
    }
  override fun encode(value: List<String>) = value.joinToString(separator = ",")
}

val queryWrapper: Database = Database(
  driver = driver,
  hockeyPlayerAdapter = hockeyPlayer.Adapter(
    cup_winsAdapter = listOfStringsAdapter
  )
)
```

## Primitives

A sibling module that adapts primitives for your convenience.

```groovy
dependencies {
  implementation "com.squareup.sqldelight:jdbc-driver:{{ versions.sqldelight }}"
}
```

The following adapters exist:

- `BooleanColumnAdapter` — Retrieves `kotlin.Boolean` for an SQL type implicitly stored as `kotlin.Long`
- `DoubleColumnAdapter` — Retrieves `kotlin.Double` for an SQL type implicitly stored as `kotlin.Long`
- `FloatColumnAdapter` — Retrieves `kotlin.Float` for an SQL type implicitly stored as `kotlin.Long`
- `IntColumnAdapter` — Retrieves `kotlin.Int` for an SQL type implicitly stored as `kotlin.Long`
- `ShortColumnAdapter` — Retrieves `kotlin.Short` for an SQL type implicitly stored as `kotlin.Long`

## Enums

As a convenience the SQLDelight runtime includes a `ColumnAdapter` for storing an enum as String data.

```sql
import com.example.hockey.HockeyPlayer;

CREATE TABLE hockeyPlayer (
  position TEXT AS HockeyPlayer.Position
)
```

```kotlin
val queryWrapper: Database = Database(
  driver = driver,
  hockeyPlayerAdapter = HockeyPlayer.Adapter(
    positionAdapter = EnumColumnAdapter()
  )
)
```

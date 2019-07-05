<p align="center">    
  <img width="256" height="256" src="./logo.png?raw=true" alt="Simpli"/>    
</p> 

# simpli-sql
Kotlin and Java tools that makes SQL connections easier and don't use ORM

## Install
**Gradle**
```
compile group: 'br.com.simpli', name: 'simpli-sql', version: '3.0.0'
```
**Maven**
```xml
<dependency>
  <groupId>br.com.simpli</groupId>
  <artifactId>simpli-sql</artifactId>
  <version>3.0.0</version>
</dependency>
```

## Get a transactional connector to read and write
```kotlin
val transacPipe = TransacConPipe("jdbc/datasourceName")

transacPipe.handle { con ->
  val num = con.getFirstInt(Query("SELECT COUNT(*) FROM table"))
  con.execute(Query("UPDATE table_count SET num = ? ", num))
  // all good!
}
```

## Get a read-only connector, to make sure you will not try to write accidentally
```kotlin
val conPipe = ReadConPipe("jdbc/datasourceName")

conPipe.handle { con ->
  val num = con.getFirstInt(Query("SELECT COUNT(*) FROM table"))
  con.execute(Query("UPDATE table_count SET num = ? ", num))
  // ⚠ Exception: You can't update using a read connection
}
```

## Select
```kotlin
val myQuery = Query()
    .select("mycolumn")
    .from("mytable")
    .innerJoin("othertable", "idOtherTableFk", "idOtherTablePk")
    .whereEq("myothercolumn", 2) // 'where' methods adds WHERE or AND (if not the first)
    .where(Query()
        .orEq("columito", 5) // 'or' or 'and' methods adds OR/AND or nothing (if the first)
        .orNull("nulito"))
        
val mycolumnList = con.getStringList(myQuery)

/* 
query translated to:
SELECT mycolumn
FROM mytable
INNER JOIN othertable ON idOtherTableFk = idOtherTablePk
WHERE myothercolumn = 2
AND (
    columito = 5
    OR nulito IS NULL
)
*/
```
You can also use: `selectRaw`, `selectAll`, `count`, `countRaw`, `countAll`, `innerJoinRaw`, `leftjoin`, `leftJoinRaw`, `and`, `or`, `having`, `orderBy`, `groupBy`, `limit` 

## Insert
```kotlin
val myQuery = Query().insertInto("mytable").insertValues("mycolumn" to "thenewvalue", "myothercolumn" to 5)
val newId = con.execute(myQuery).key
```

## Update
```kotlin
val myQuery = Query().updateTable("mytable")
    .updateSet("mycolumn" to "thenewvalue", "myothercolumn" to 5)
    .whereGt("myothercolumn", 2)
val numOfRowsAffected = con.execute(myQuery).affectedRows
```

## Other methods

### Raw
```kotlin
Query.raw("anything here ?, got it?", 3, 7)
// output:
// anything here 3, got it7
// it also put spaces in the end

// or you can simply:
Query("this is a raw shortcut ?", 4)
```

### Concat
```kotlin
val q1 = Query().raw("hey")
val q2 = Query().raw("jude")
val q3 = Query().raw("nananananana")
q1.concat(q2, q3)
// output:
// hey jude nananananana
```

### OddText
```kotlin
Query().oddText(3, "mama", ",")
// output:
// mama,mama,mama
```
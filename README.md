<p align="center">    
  <img width="256" height="256" src="./logo.png?raw=true" alt="Simpli"/>    
</p> 

# simpli-sql
**You don't need to use reflection!** They are heavy to process.

Simpli-SQL is a Kotlin and Java library that makes SQL operations easier and more flexible than using an ORM.

## Install
**Gradle**
```
compile group: 'br.com.simpli', name: 'simpli-sql', version: '3.2.1'
```
**Maven**
```xml
<dependency>
  <groupId>br.com.simpli</groupId>
  <artifactId>simpli-sql</artifactId>
  <version>3.2.1</version>
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

## How to make a Select
```kotlin
// build your query
val myQuery = Query()
    .select("mycolumn")
    .from("mytable")
    .innerJoin("othertable", "idOtherTableFk", "idOtherTablePk")
    .whereEq("myothercolumn", 2) // 'where' methods adds WHERE or AND (if not the first)
    .whereSomeEq(
        "columito" to 5,
        "otrita" to "abc"
     )
     
// run the query using a connector, in this case we are getting a simple String List   
val mycolumnList = con.getStringList(myQuery)
```

query translated to:
```
SELECT mycolumn
FROM mytable
INNER JOIN othertable ON idOtherTableFk = idOtherTablePk
WHERE myothercolumn = 2
AND (
    columito = 5
    OR otrita = "abc"
)
``` 

## How to make an Insert
```kotlin
val myQuery = Query().insertInto("mytable").insertValues(
    "mycolumn" to "thenewvalue",
    "myothercolumn" to 5)
val newId = con.execute(myQuery).key // key is the generated ID
```

## Update
```kotlin
val myQuery = Query().updateTable("mytable")
    .updateSet(
        "mycolumn" to "thenewvalue",
        "myothercolumn" to 5)
    .whereGt("myothercolumn", 2)
val numOfRowsAffected = con.execute(myQuery).affectedRows
// affectedRows are the number of rows affected by the query
```

# Documentation
Really **EASY** and **QUICK** documentation

- [Query documentation](QUERY_DOCUMENTATION.md) - How to build your queries
- [Connector documentation](CONNECTOR_DOCUMENTATION.md) - How to execute the operations and retrieve the information
- [VirtualSelect documentation](VIRTUALSELECT_DOCUMENTATION.md) - How to build smarter queries

Deprecated:
- [ResultBuilder documentation](RESULTBUILDER_DOCUMENTATION.md)

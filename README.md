# simpli-sql
Kotlin and Java tools that makes SQL connections easier and don't use ORM

## Install
**Gradle**
```
compile group: 'br.com.simpli', name: 'simpli-sql', version: '2.0.0'
```
**Maven**
```xml
<dependency>
  <groupId>br.com.simpli</groupId>
  <artifactId>simpli-sql</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Get a connection
```kotlin
val conPipe = ConnectionPipe("jdbc/datasourceName")

conPipe.handle { con ->
  // do something with the connection inside this scope
}
```

## Get a transactional connection
```kotlin
val transacPipe = TransactionPipe("jdbc/datasourceName")

transacPipe.handle { con ->
  // if you throw an exception it will make a rollback of all changes
}
```

## Create a Data Access Object
```kotlin
class MyDao(con: Connection) : Dao(con, EnglishLanguage()) {
  // put all your database methods here
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
        
val mycolumnList = this.getStringList(myQuery)

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

## Insert
```kotlin
val myQuery = Query().insertInto("mytable").insertValues("mycolumn" to "thenewvalue", "myothercolumn" to 5)
val newId = this.execute(myQuery).key
```

## Update
```kotlin
val myQuery = Query().updateTable("mytable")
    .updateSet("mycolumn" to "thenewvalue", "myothercolumn" to 5)
    .whereGt("myothercolumn", 2)
val numOfRowsAffected = this.execute(myQuery).affectedRows
```

## Other methods

### Raw
```kotlin
Query.raw("anything here ?, got it?", 3, 7)
// output:
// anything here 3, got it7
// it also put spaces in the end
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
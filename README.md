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
val myQuery = this.q.select("mycolumn").from("mytable").whereEq("myothercolumn", 2)
val mycolumnList = this.getStringList(myQuery)
```

## Insert
```kotlin
val myQuery = this.q.insertInto("mytable").insertValues("mycolumn" to "thenewvalue", "myothercolumn" to 5)
val newId = this.execute(myQuery).key
```

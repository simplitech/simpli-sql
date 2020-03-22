# Query methods

## raw
add raw string to the query
```kotlin
Query()
.raw("SELECT * FROM table WHERE column = ? AND other = ?", "abc", 123)
```
Resulting query: `SELECT * FROM table WHERE column = "abc" AND other = 123`

## selectRaw
simply adds "SELECT" in the query
```kotlin
Query()
.selectRaw("column, other").raw("FROM table")
```
Resulting query: `SELECT column, other FROM table`
 
## selectAll
select all columns
```kotlin
Query()
.selectAll()
.raw("FROM table")
```
Resulting query: `SELECT * FROM table`

## select
select the fields by name
```kotlin
Query()
.select("column", "other")
.raw("FROM table")
```
Resulting query: `SELECT column, other FROM table`

## selectFields
select the fields by name, using an array
```kotlin
val fields = arrayOf("column", "other")
Query()
.selectFields(fields)
.raw("FROM table")
```
Resulting query: `SELECT column, other FROM table`

## countRaw
select counting for something
```kotlin
Query()
.countRaw("column")
.raw("FROM table")
```
Resulting query: `SELECT COUNT(column) FROM table`

## countAll
select counting all
```kotlin
Query()
.countAll()
.raw("FROM table")
```
Resulting query: `SELECT COUNT(*) FROM table`

## count
select counting the fields by name
```kotlin
Query()
.count("column", "other")
.raw("FROM table")
```
Resulting query: `SELECT COUNT(column, other) FROM table`

## from
simply adds "FROM" in the query
```kotlin
Query("SELECT column")
.from("table")
```
Resulting query: `SELECT column FROM table`

## from with Query
uses FROM with an inner query
```kotlin
Query("SELECT column")
.from(Query("SELECT column FROM table ORDER BY other", "myinner"))
.raw("FROM table")
```
Resulting query: `SELECT column FROM ( SELECT column FROM table ORDER BY other ) myinner FROM table`

## where
adds "WHERE" if there isn't any "WHERE" yet on the query, otherwise adds "AND"
```kotlin
Query("SELECT column FROM table")
.where("column = ?", "abc")
.where("other = ?", 123)
```
Resulting query: `SELECT column FROM table WHERE column = "abc" AND other = 123`

## whereAll
 adds a WHERE with multiple conditions, all of them needs to be true
 ```kotlin
 Query("SELECT column FROM table").whereAll {
      where("column = ?", "abc")
      where("other = ?", 123)
 }
 ```
 Resulting query: `SELECT column FROM table WHERE (column = "abc" AND other = 123)`

## whereSome
adds a WHERE with multiple conditions, only one of them needs to be true
 ```kotlin
 Query("SELECT column FROM table").whereSome {
      where("column = ?", "abc")
      where("other = ?", 123)
 }
 ```
Resulting query: `SELECT column FROM table WHERE (column = "abc" OR other = 123)`

## whereAllEq
adds a WHERE with multiple conditions, all of them needs to be equal to the specified value
 ```kotlin
 Query("SELECT column FROM table").whereAllEq(
      "column" to "abc",
      "other" to 123,
      "password" to Query("SHA1(?)", "ultrasecret")
 )
```
Resulting query: `SELECT column FROM table WHERE (column = "abc" AND other = 123 AND password = SHA1("ultrasecret"))`

You can use a map as well
 ```kotlin
 val conditionsMap = mapOf(
      "column" to "abc",
      "other" to 123,
      "password" to Query("SHA1(?)", "ultrasecret")
 )
 
 Query("SELECT column FROM table").whereAllEq(conditionsMap)
 ```
Resulting query: `SELECT column FROM table WHERE (column = "abc" AND other = 123 AND password = SHA1("ultrasecret"))`
 
## whereSomeEq
adds a WHERE with multiple conditions, only one of them needs to be equal to the specified value
```kotlin
 Query("SELECT column FROM table").whereSomeEq(
      "column" to "abc",
      "other" to 123,
      "password" to Query("SHA1(?)", "ultrasecret")
 )
```
Resulting query: `SELECT column FROM table WHERE (column = "abc" OR other = 123 OR password = SHA1("ultrasecret"))`

You can use a map as well
 ```kotlin
 val conditionsMap = mapOf(
      "column" to "abc",
      "other" to 123,
      "password" to Query("SHA1(?)", "ultrasecret")
 )

 Query("SELECT column FROM table").whereSomeEq(conditionsMap)
```
Resulting query: `SELECT column FROM table WHERE (column = "abc" OR other = 123 OR password = SHA1("ultrasecret"))`

# other Where methods:
```kotlin
Query()
.selectAll()
.from("table")
.whereEq("column", "abc") // WHERE column = "abc"
.whereNotEq("other", 456)  // AND other != 456
.whereGt("numbii", 3) // AND numbii > 3
.whereLt("numbii", 6) // AND numbii < 6
.whereGtEq("nomboo", 5) // AND nomboo >= 5
.whereLtEq("nomboo", 7) // AND nomboo <= 7
.whereNull("thatnullable") // AND thatnullable IS NULL
.whereNotNull("thatothernulable") // AND thatothernulable IS NOT NULL
.whereBetween("rangeable", 3, 7) // AND rangeable BETWEEN 3 AND 7
.whereLike("name", "%john%") // AND name LIKE "%john%"
.whereIn("id", 2, 3, 4) // AND id IN (2, 3, 4)
.whereNotIn("anotherid", 6, 7, 8) // AND anotherid NOT IN 6, 7, 8
```
 
## innerJoinRaw
simply adds "INNER JOIN" in the query
```kotlin
Query("SELECT column FROM table").innerJoinRaw("othertable ON primarykey = foreignkey")
```
Resulting query: `SELECT column FROM table INNER JOIN othertable ON primarykey = foreignkey`

## innerJoin
add an inner join with the joining columns
```kotlin
Query("SELECT column FROM table").innerJoin("othertable", "primarykey", "foreignkey")
```
Resulting query: `SELECT column FROM table INNER JOIN othertable ON primarykey = foreignkey`

## leftJoinRaw
simply adds "LEFT JOIN" in the query
```kotlin
Query("SELECT column FROM table").leftJoinRaw("othertable ON primarykey = foreignkey")
```
Resulting query: `SELECT column FROM table LEFT JOIN othertable ON primarykey = foreignkey`

## leftJoin
add a left join with the joining columns
```kotlin
Query("SELECT column FROM table").leftJoin("othertable", "primarykey", "foreignkey")
```
Resulting query: `SELECT column FROM table LEFT JOIN othertable ON primarykey = foreignkey`

## groupByRaw
simply adds "GROUP BY" in the query
```kotlin
Query("SELECT COUNT(column) FROM table").groupByRaw("column")
```
Resulting query: `SELECT COUNT(column) FROM table GROUP BY column`

## groupBy
adds group by of the columns
```kotlin
Query("SELECT COUNT(column) FROM table").groupBy("column", "other")
```
Resulting query: `SELECT COUNT(column) FROM table GROUP BY column, other`

## having
simply adds "HAVING" in the query
```kotlin
Query("SELECT COUNT(column) AS countt FROM table GROUP BY column").having("countt > 3")
```
Resulting query: `SELECT COUNT(column) AS countt FROM table GROUP BY column HAVING countt > 3`

# other Having methods:
```kotlin
Query()
.selectAll()
.from("table")
.havingEq("column", "abc") // HAVING column = "abc"
.havingNotEq("other", 456)  // HAVING other != 456
.havingGt("numbii", 3) // HAVING numbii > 3
.havingLt("numbii", 6) // HAVING numbii < 6
.havingGtEq("nomboo", 5) // HAVING nomboo >= 5
.havingLtEq("nomboo", 7) // HAVING nomboo <= 7
.havingNull("thatnullable") // HAVING thatnullable IS NULL
.havingNotNull("thatothernulable") // HAVING thatothernulable IS NOT NULL
.havingBetween("rangeable", 3, 7) // HAVING rangeable BETWEEN 3 AND 7
.havingLike("name", "%john%") // HAVING name LIKE "%john%"
.havingIn("id", 2, 3, 4) // HAVING id IN (2, 3, 4)
.havingNotIn("anotherid", 6, 7, 8) // HAVING anotherid NOT IN 6, 7, 8
```

## orderByRaw
simply adds "ORDER BY" in the query
```kotlin
Query("SELECT column FROM table").orderByRaw("column ASC")
```
Resulting query: `SELECT column FROM table ORDER BY column`

## orderBy
add order by with a string identifing if ASC or DESC
```kotlin
Query("SELECT column FROM table").orderBy("column", "ASC")
```
Resulting query: `SELECT column FROM table ORDER BY column ASC`

## orderByAsc
add order by with optional true for ASC and false for DESC
```kotlin
 Query("SELECT column FROM table").orderByAsc("column")
```
Resulting query: `SELECT column FROM table ORDER BY column ASC`

```kotlin
Query("SELECT column FROM table").orderByAsc("column", false)
```
Resulting query: `SELECT column FROM table ORDER BY column DESC`

## limit
adds limit with index and size
```kotlin
Query("SELECT column FROM table ORDER BY column ASC").limit(3, 20)
```
Resulting query: `SELECT column FROM table ORDER BY column ASC LIMIT 3, 20`

## insertInto
adds insert into in the query
```kotlin
Query().insertInto("table").raw("(column, other) VALUES (?, ?)", "abc", 123)
```
Resulting query: `INSERT INTO table (column, other) VALUES ("abc", 123)`

## insertValues
easily adds the values of insert in the query
```kotlin
Query("INSERT INTO table").insertValues(
     "column" to "abc",
     "other" to 123,
     "password" to Query("SHA1(?)", "ultrasecret")
)
```
Resulting query: `INSERT INTO table (column, other, password) VALUES ("abc", 123, SHA1("ultrasecret"))`

You can use a map as well
```
val insertMap = mapOf(
     "column" to "abc",
     "other" to 123,
     "password" to Query("SHA1(?)", "ultrasecret")
)

Query("INSERT INTO table").insertValues(insertMap)
```
Resulting query: `INSERT INTO table (column, other, password) VALUES ("abc", 123, SHA1("ultrasecret"))`

## updateTable
adds update to the query
```kotlin
Query().updateTable("table").raw("SET column = ?, other = ? WHERE id = ?", "abc", 123, 1)
```
Resulting query: `UPDATE table SET column = "abc", other = 123 WHERE id = 1`

## updateSet
easily adds the set of changes of update in the query
```kotlin
Query("UPDATE table").updateSet(
     "column" to "abc",
     "other" to 123,
     "password" to Query("SHA1(?)", "ultrasecret")
).raw("WHERE id = ?", 1)
```
Resulting query: `UPDATE table SET column = "abc", other = 123, password = SHA1("ultrasecret") WHERE id = 1`

You can use a map as well
```kotlin
val updateMap = mapOf(
     "column" to "abc",
     "other" to 123,
     "password" to Query("SHA1(?)", "ultrasecret")
)

Query("UPDATE table").updateSet(updateMap).raw("WHERE id = ?", 1)
```
Resulting query: `UPDATE table SET column = "abc", other = 123, password = SHA1("ultrasecret") WHERE id = 1`

## deleteFrom
adds delete from on the query
```kotlin
Query().deleteFrom("table").raw("WHERE id = ?", 1)
```
Resulting query: `DELETE FROM table WHERE id = 1`

## concat
concatenate all queries
```kotlin
Query("SELECT * FROM table").concat(Query("WHERE column = ?", "abc"), Query("AND other = ?", 123))
```
Resulting query: `SELECT * FROM table WHERE column = "abc" AND other = 123`

## oddText
utility function to tokenize
```kotlin
Query().oddText(3, "?", ",")
```
Resulting query: `?,?,?`

## ifThen
execute the callback only if the boolean is true, util to avoid breaking the Query chain
```kotlin
Query("SELECT * FROM table").ifThen(compareColumn) {
     where("column = ?", "abc")
}.where("other = ?", 123)
```
Resulting query when compareColumn == true: `SELECT * FROM table WHERE column = "abc" AND other = 123`

Resulting query when compareColumn == false: `SELECT * FROM table WHERE other = 123`

## elseIf
executes the callback only if the boolean is true and the previous ifThen (or letThen) was not fulfilled
```kotlin
Query("SELECT * FROM table").ifThen(compareColumn) {
     where("column = ?", "abc")
}.elseIf(compareOther) {
     where("other = ?", 123)
}
```

## elseThen
executes the callback only if the previous ifThen (or letThen) was not fulfilled
```kotlin
Query("SELECT * FROM table").ifThen(compareColumn) {
     where("column = ?", "abc")
}.elseThen {
     where("other = ?", 123)
}
```
Resulting query when compareColumn == true: `SELECT * FROM table WHERE column = "abc"`

Resulting query when compareColumn == false: `SELECT * FROM table WHERE other = 123`

## letThen
execute the callback only if the variable is not null, util to avoid breaking the Query chain
```kotlin
Query("SELECT * FROM table").letThen(columnSearch) {
     where("column = ?", columnSearch)
}.where("other = ?", 123)
```
Resulting query when columnSearch == "abc": `SELECT * FROM table WHERE column = "abc" AND other = 123`

Resulting query when compareColumn == null: `SELECT * FROM table WHERE other = 123`

## fieldsQuestionsAndParams
utility function to organize name and value pairs that may have an inner query
```kotlin
Query().fieldsQuestionsAndParams(
     "column" to "abc",
     "other" to Query("SHA1(?)", 123)
)
```
Resulting list: `[{ field: "column", question: "?", params: ["abc"] }, { field: "other", question: "SHA1(?)", params: [123] }]`
# Connector methods

## execute
runs a write query that returns a key for insert's ID or update's affectedRows
```kotlin
val id = con.execute(query1).key
val ar = con.execute(query2).affectedRows
```

## getOne
gets a single row of result providing a callback to build it
```kotlin
con.getOne(query) { rs ->
    return Person(rs.getString("name"), rs.getTimestamp("birthdate"))
}
```

## getList
gets a list of results providing a callback to build each row
```kotlin
con.getList(query) { rs ->
    return Person(rs.getString("name"), rs.getTimestamp("birthdate"))
}
```

## exist
return true if there is any resulting row
```kotin
con.exist(query)
```

## getFirstInt
returns the first column of the first resulting row
```kotlin
getFirstInt(query)
```

## getIntList
returns a list of the first column of each row
```kotlin
con.getIntList(query)
```

## getFirstLong
returns the first column of the first resulting row
```kotlin
con.getFirstLong(query)
```

## getLongList
returns a list of the first column of each row
```kotlin
con.getLongList(query)
```

## getFirstDouble
returns the first column of the first resulting row
```kotlin
con.getFirstDouble(query)
```

## getDoubleList
returns a list of the first column of each row
```kotlin
con.getDoubleList(query)
```

## getFirstString
returns the first column of the first resulting row
```kotlin
con.getFirstString(query)
```

## getStringList
returns a list of the first column of each row
```kotlin
con.getStringList(query)
```

## getFirstDate
returns the first column of the first resulting row
```kotlin
con.getFirstDate(query)
```

## getDateList
returns a list of the first column of each row
```kotlin
con.getDateList(query)
```

## getFirstBoolean
returns the first column of the first resulting row
```kotlin
con.getFirstBoolean(query)
```

## getBooleanList
returns a list of the first column of each row
```kotlin
con.getBooleanList(query)
```


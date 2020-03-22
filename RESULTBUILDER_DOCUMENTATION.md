# ResultBuilder

## Usecase
In the examples below we want to get all students of a particular teacher.
Both the teacher and the students are in the same table called "user".
And we only want to select "id" and "email" columns.

## NOT using ResultBuilder
We will need to control the alias manually and use ifs before asking the resulting fields.
```kotlin
fun getJohnStudents(): MutableList<User> {
    val idAndEmailFields = arrayOf("id", "email")
    
    val query = Query()
            .selectFields(idAndEmailFields)
            .from("user AS student")
            .innerJoin("user AS teacher", "student.idTeacherFk", "teacher.id")
            .whereEq("teacher.name", "John")
    
    return con.getList(query) {
        User("student", idAndEmailFields, it)
    }
}

class User() {
    var id: Long = 0
    var email: String? = null
    var name: String? = null

    constructor(alias: String, selectFields: Array<string>, rs: ResultSet) : this() {
        if (selectFields.contains("id"))
            id = rs.getLong(alias + ".id")

        if (selectFields.contains("email"))
            email = rs.getString(alias + ".email")
            
        if (selectFields.contains("name"))
            name = rs.getString(alias + ".name")
    }
}
```

## USING ResultBuilder
ResultBuilder will take care of the alias, and it is optional, you can omit it.
You don't need those ifs, you only need to pass the fields array in the constructor,
if the fields is not on the array the method will return null (or the default value, 0 for Long, false for boolean, etc).
```kotlin
fun getJohnStudents(): MutableList<User> {
    val idAndEmailFields = arrayOf("id", "email")
    
    val query = Query()
            .selectFields(idAndEmailFields)
            .from("user AS student")
            .innerJoin("user AS teacher", "student.idTeacherFk", "teacher.id")
            .whereEq("teacher.name", "John")
    
    return con.getList(query) {
        User(ResultBuilder("student", idAndEmailFields, it))
    }
}

class User() {
    var id: Long = 0
    var email: String? = null
    var name: String? = null

    constructor(rs: ResultBuilder) : this() {
        id = rs.getLong("id")
        email = rs.getString("email")
        name = rs.getString("name")
    }
}
```

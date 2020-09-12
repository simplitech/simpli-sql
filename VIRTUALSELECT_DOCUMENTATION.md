# VirtualSelect methods

Before Learning how to use VirtualSelect you should take a look at [Query documentation](QUERY_DOCUMENTATION.md) first.

While the Query class concatenates the query string as the methods are being called, the VirtualSelect uses a different approach,
storing all the required fields and operations individually.

Since VirtualSelect stores things individually, we can do things in a smarter way, but sadly we can't write raw queries.
So if you need to make a raw query you should use the Query class.

# Learn by example

## Create a RelationalMapper
This class will control how your model is related to the database. It should inherit RelationalMapper of your model, "MyTable" in this case.
```kotlin
class MyTableRM(override var alias: String? = null) : RelationalMapper<MyTable>() {
    override val table = "myTable" // you should declare the table name on the database

    // you need to declare all columns
    val idMyTablePk = col("idMyTablePk",
            { idMyTablePk }, // this is the "getter", it describes how to get the value stored on MyTable model
            { idMyTablePk = it.value() }) // this is the "setter", it describes how save the value on MyTable model

    val text = col("text",
            { text },
            { text = it.value() })

    val otherColumn = col("otherColumn",
            { otherColumn },
            { otherColumn = it.value() })
            
    fun buildModelFromResultSet(rs: ResultSet): MyTable {
        val model = MyTable()
        idMyTablePk.build(model, rs) // by calling this method you can fill the property on model with the value from the database 
        text.build(model, rs)
        otherColumn.build(model, rs)
        return model
    }
    
    // BUT INSTEAD OF WRITING THE METHOD ABOVE, I RECOMMEND WRITING THE CODE BELOW:
    
    fun build(rs: ResultSet) = MyTable().apply {
        fieldsToSelect.forEach { col ->
            col.build(this, rs) 
        }
    }

    val fieldsToSelect
        get() = arrayOf(
                idMyTablePk,
                text,
                otherColumn
        )
}

```

## Using VirtualSelect
```kotlin
val myTableRm = MyTableRM() // instantiate the RelationalMappers of all tables related with the query
val anotherTableRm = AnotherTableRM()
val anotherTableSecondJoinRm = AnotherTableRM("aliasOf2ndJoin") // you can pass alias if you are going to use the same table again

// you can call almost the same methods of Query but in any order you want
// and instead of using the columns and tables as string you should use the references in the RelationalMappers
val vs = VirtualSelect()
        .selectFields(myTableRm.fieldsToSelect)
        .selectFields(anotherTableRm.fieldsToSelect)
        .from(myTableRm)
        .innerJoin(anotherTableRm, anotherTableRm.idAnotherTablePk, myTableRm.idMyTablePk)
        
// the biggest advantage of VirtualSelect is conditional-joins, this join will only be built if it is necessary by the query
vs.conditionalLeftJoin(anotherTableSecondJoinRm, anotherTableSecondJoinRm.idAnotherTablePk, myTableRm.idMyTablePk)

// if no field of this RelationalMapper is required by the query it will be not built, so you can make all the joins you want and use IFs later:  
if (filter.myThing) {
    vs.whereEq(anotherTableSecondJoinRm.myThing, filter.myThing)
}

// and then, you can get the Query from VirtualSelect and use it
val query = vs.toQuery()

val myTable = con.getOne(query) {
    myTableRm.build(it)
}
```

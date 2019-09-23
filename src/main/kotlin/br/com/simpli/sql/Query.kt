package br.com.simpli.sql

import java.util.*

/**
 * A Query Builder
 *
 * @author gil
 */
open class Query {

    var strSt = ""
        private set

    val paramsSt = ArrayList<Any?>()

    private var ifNotFulfilled = false

    constructor()

    /**
     * shortcut for raw function
     *
     * Query("SELECT * FROM table WHERE column = ? AND other = ?", "abc", 123)
     *
     * SELECT * FROM table WHERE column = "abc" AND other = 123
     */
    constructor(str: String?, vararg params: Any?) {
        raw(str, *params)
    }

    /**
     * add raw string to the query
     *
     * Query().raw("SELECT * FROM table WHERE column = ? AND other = ?", "abc", 123)
     *
     * SELECT * FROM table WHERE column = "abc" AND other = 123
     */
    open fun raw(str: String?, vararg params: Any?) : Query {
        ifNotFulfilled = false // reset the ifThen logic

        str?.let {
            strSt += " $str "
        }

        if (params.isNotEmpty()) {
            paramsSt.addAll(params.toList())
        }

        return this
    }

    /**
     * simply adds "SELECT" in the query
     *
     * Query().selectRaw("column, other").raw("FROM table")
     *
     * SELECT column, other FROM table
     */
    open fun selectRaw(str: String?, vararg params: Any?) = raw("SELECT $str", *params)

    /**
     * select all columns
     *
     * Query().selectAll().raw("FROM table")
     *
     * SELECT * FROM table
     */
    open fun selectAll() = selectRaw("*")

    /**
     * select the fields by name
     *
     * Query().select("column", "other").raw("FROM table")
     *
     * SELECT column, other FROM table
     */
    open fun select(vararg columns: String) = selectRaw(columns.joinToString())

    /**
     * select counting for something
     *
     * Query().countRaw("column").raw("FROM table")
     *
     * SELECT COUNT(column) FROM table
     */
    open fun countRaw(str: String?, vararg params: Any?) = selectRaw("COUNT($str)", *params)

    /**
     * select counting all
     *
     * Query().countAll().raw("FROM table")
     *
     * SELECT COUNT(*) FROM table
     */
    open fun countAll() = countRaw("*")

    /**
     * select counting the fields by name
     *
     * Query().count("column", "other").raw("FROM table")
     *
     * SELECT COUNT(column, other) FROM table
     */
    open fun count(vararg columns: String) = countRaw(columns.joinToString())

    /**
     * simply adds "FROM" in the query
     *
     * Query("SELECT column").from("table")
     *
     * SELECT column FROM table
     */
    open fun from(str: String?, vararg params: Any?) = raw("FROM $str", *params)

    /**
     * uses FROM with an inner query
     *
     * Query("SELECT column").from(Query("SELECT column FROM table ORDER BY other", "myinner")).raw("FROM table")
     *
     * SELECT column FROM ( SELECT column FROM table ORDER BY other ) myinner FROM table
     */
    open fun from(query: Query, name: String) = from("(").concat(query).raw(") $name")

    /**
     * adds "WHERE" if there isn't any "WHERE" yet on the query. otherwise adds "AND"
     *
     * Query("SELECT column FROM table").where("column = ?", "abc").where("other = ?", 123)
     *
     * SELECT column FROM table WHERE column = "abc" AND other = 123
     */
    open fun where(str: String?, vararg params: Any?): Query {
        val word = if (!strSt.toUpperCase().contains("WHERE ")) "WHERE" else "AND"
        return raw("$word $str", *params)
    }

    /**
     * adds a WHERE with multiple conditions, all of them needs to be true
     *
     * Query("SELECT column FROM table").whereAll {
     *      where("column = ?", "abc")
     *      where("other = ?", 123)
     * }
     *
     * SELECT column FROM table WHERE (column = "abc" AND other = 123)
     */
    open fun whereAll(callback: Query.() -> Unit): Query {
        val innerQuery = WhereAllQuery()
        callback(innerQuery)
        return where("(").concat(innerQuery).raw(")")
    }


    /**
     * adds a WHERE with multiple conditions, only one of them needs to be true
     *
     * Query("SELECT column FROM table").whereSome {
     *      where("column = ?", "abc")
     *      where("other = ?", 123)
     * }
     *
     * SELECT column FROM table WHERE (column = "abc" OR other = 123)
     */
    open fun whereSome(callback: Query.() -> Unit): Query {
        val innerQuery = WhereSomeQuery()
        callback(innerQuery)
        return where("(").concat(innerQuery).raw(")")
    }

    open class WhereAllQuery : Query() {
        override fun where(str: String?, vararg params: Any?): Query {
            val word = if (strSt.isEmpty()) "" else "AND"
            return raw("$word $str", *params)
        }
    }

    open class WhereSomeQuery : Query() {
        override fun where(str: String?, vararg params: Any?): Query {
            val word = if (strSt.isEmpty()) "" else "OR"
            return raw("$word $str", *params)
        }
    }

    /**
     * adds a WHERE with multiple conditions, all of them needs to be equal to the specified value
     *
     * Query("SELECT column FROM table").whereAllEq(
     *      "column" to "abc",
     *      "other" to 123,
     *      "password" to Query("SHA1(?)", "ultrasecret")
     * )
     *
     * SELECT column FROM table WHERE (column = "abc" AND other = 123 AND password = SHA1("ultrasecret"))
     */
    open fun whereAllEq(vararg value: Pair<String, Any?>): Query {
        val processed = fieldsQuestionsAndParams(*value)
        val params = ArrayList<Any?>()
        processed.forEach{ params.addAll(it.params) }

        return where("(${processed.map { "${it.field} = ${it.question}" }.joinToString(" AND ")})",
                *params.toTypedArray())
    }

    /**
     * adds a WHERE with multiple conditions, all of them needs to be equal to the specified value
     *
     * val conditionsMap = mapOf(
     *      "column" to "abc",
     *      "other" to 123,
     *      "password" to Query("SHA1(?)", "ultrasecret")
     * )
     *
     * Query("SELECT column FROM table").whereAllEq(conditionsMap)
     *
     * SELECT column FROM table WHERE (column = "abc" AND other = 123 AND password = SHA1("ultrasecret"))
     */
    open fun whereAllEq(value: Map<String, Any?>) = whereAllEq(*value.toList().toTypedArray())

    /**
     * adds a WHERE with multiple conditions, only one of them needs to be equal to the specified value
     *
     * Query("SELECT column FROM table").whereSomeEq(
     *      "column" to "abc",
     *      "other" to 123,
     *      "password" to Query("SHA1(?)", "ultrasecret")
     * )
     *
     * SELECT column FROM table WHERE (column = "abc" OR other = 123 OR password = SHA1("ultrasecret"))
     */
    open fun whereSomeEq(vararg value: Pair<String, Any?>): Query {
        val processed = fieldsQuestionsAndParams(*value)
        val params = ArrayList<Any?>()
        processed.forEach{ params.addAll(it.params) }

        return where("(${processed.map { "${it.field} = ${it.question}" }.joinToString(" AND ")})",
                *params.toTypedArray())
    }

    /**
     * adds a WHERE with multiple conditions, only one of them needs to be equal to the specified value
     *
     * val conditionsMap = mapOf(
     *      "column" to "abc",
     *      "other" to 123,
     *      "password" to Query("SHA1(?)", "ultrasecret")
     * )
     *
     * Query("SELECT column FROM table").whereSomeEq(conditionsMap)
     *
     * SELECT column FROM table WHERE (column = "abc" OR other = 123 OR password = SHA1("ultrasecret"))
     */
    open fun whereSomeEq(value: Map<String, Any?>) = whereSomeEq(*value.toList().toTypedArray())

    open fun whereEq(column: String, param: Any) = where("$column = ?", param)
    open fun whereNotEq(column: String, param: Any) = where("$column != ?", param)
    open fun whereGt(column: String, param: Any) = where("$column > ?", param)
    open fun whereLt(column: String, param: Any) = where("$column < ?", param)
    open fun whereGtEq(column: String, param: Any) = where("$column >= ?", param)
    open fun whereLtEq(column: String, param: Any) = where("$column <= ?", param)
    open fun whereNull(column: String) = where("$column IS NULL")
    open fun whereNotNull(column: String) = where("$column IS NOT NULL")
    open fun whereBetween(column: String, p1: Any, p2: Any) = where("$column BETWEEN ? AND ?", p1, p2)
    open fun whereLike(column: String, param: Any) = where("$column LIKE ?", param)
    open fun whereIn(column: String, vararg param: Any?) = where("$column IN (${oddText(param.size, "?", ",")})", *param)
    open fun whereNotIn(column: String, vararg param: Any?) = whereIn("$column NOT", param)

    /**
     * simply adds "INNER JOIN" in the query
     *
     * Query("SELECT column FROM table").innerJoinRaw("othertable ON primarykey = foreignkey")
     *
     * SELECT column FROM table INNER JOIN othertable ON primarykey = foreignkey
     */
    open fun innerJoinRaw(str: String?, vararg params: Any?) = raw("INNER JOIN $str", *params)

    /**
     * add an inner join with the joining columns
     *
     * Query("SELECT column FROM table").innerJoin("othertable", "primarykey", "foreignkey")
     *
     * SELECT column FROM table INNER JOIN othertable ON primarykey = foreignkey
     */
    open fun innerJoin(otherTable: String, oneColumn: String, otherColumn: String) = innerJoinRaw("$otherTable ON $oneColumn = $otherColumn")

    /**
     * simply adds "LEFT JOIN" in the query
     *
     * Query("SELECT column FROM table").leftJoinRaw("othertable ON primarykey = foreignkey")
     *
     * SELECT column FROM table LEFT JOIN othertable ON primarykey = foreignkey
     */
    open fun leftJoinRaw(str: String?, vararg params: Any?) = raw("LEFT JOIN $str", *params)

    /**
     * add a left join with the joining columns
     *
     * Query("SELECT column FROM table").leftJoin("othertable", "primarykey", "foreignkey")
     *
     * SELECT column FROM table LEFT JOIN othertable ON primarykey = foreignkey
     */
    open fun leftJoin(otherTable: String, oneColumn: String, otherColumn: String) = leftJoinRaw("$otherTable ON $oneColumn = $otherColumn")

    /**
     * simply adds "GROUP BY" in the query
     *
     * Query("SELECT COUNT(column) FROM table").groupByRaw("column")
     *
     * SELECT COUNT(column) FROM table GROUP BY column
     */
    open fun groupByRaw(str: String?, vararg params: Any?) = raw("GROUP BY $str", *params)

    /**
     * adds group by of the columns
     *
     * Query("SELECT COUNT(column) FROM table").groupBy("column", "other")
     *
     * SELECT COUNT(column) FROM table GROUP BY column, other
     */
    open fun groupBy(vararg columns: String) = groupByRaw(columns.joinToString())

    /**
     * simply adds "HAVING" in the query
     *
     * Query("SELECT COUNT(column) AS countt FROM table GROUP BY column").having("countt > 3")
     *
     * SELECT COUNT(column) AS countt FROM table GROUP BY column HAVING countt > 3
     */
    open fun having(str: String?, vararg param: Any?) = raw("HAVING $str", *param)

    open fun havingEq(column: String, param: Any) = having("$column = ?", param)
    open fun havingNotEq(column: String, param: Any) = having("$column != ?", param)
    open fun havingGt(column: String, param: Any) = having("$column > ?", param)
    open fun havingLt(column: String, param: Any) = having("$column < ?", param)
    open fun havingGtEq(column: String, param: Any) = having("$column >= ?", param)
    open fun havingLtEq(column: String, param: Any) = having("$column <= ?", param)
    open fun havingNull(column: String) = having("$column IS NULL")
    open fun havingNotNull(column: String) = having("$column IS NOT NULL")
    open fun havingBetween(column: String, p1: Any, p2: Any) = having("$column BETWEEN ? AND ?", p1, p2)
    open fun havingLike(column: String, param: Any) = having("$column LIKE ?", param)
    open fun havingIn(column: String, vararg param: Any?) = having("$column IN (${oddText(param.size, "?", ",")})", *param)
    open fun havingNotIn(column: String, vararg param: Any?) = havingIn("$column NOT", param)

    /**
     * simply adds "ORDER BY" in the query
     *
     * Query("SELECT column FROM table").orderByRaw("column ASC")
     *
     * SELECT column FROM table ORDER BY column
     */
    open fun orderByRaw(str: String?, vararg params: Any?) = raw("ORDER BY $str", *params)

    /**
     * add order by with a string identifing if ASC or DESC
     *
     * Query("SELECT column FROM table").orderBy("column", "ASC")
     *
     * SELECT column FROM table ORDER BY column ASC
     */
    open fun orderBy(column: String, order: String) = orderByRaw("$column $order")

    /**
     * add order by with optional true for ASC and false for DESC
     *
     * Query("SELECT column FROM table").orderByAsc("column")
     *
     * SELECT column FROM table ORDER BY column ASC
     *
     * Query("SELECT column FROM table").orderByAsc("column", false)
     *
     * SELECT column FROM table ORDER BY column DESC
     */
    open fun orderByAsc(column: String, asc: Boolean? = true) = orderBy(column, if (asc != false) "ASC" else "DESC")

    /**
     * adds limit with index and size
     *
     * Query("SELECT column FROM table ORDER BY column ASC").limit(3, 20)
     *
     * SELECT column FROM table ORDER BY column ASC LIMIT 3, 20
     */
    open fun limit(index: Number = 0, size: Number) = raw("LIMIT ?, ?", index, size)

    /**
     * adds insert into in the query
     *
     * Query().insertInto("table").raw("(column, other) VALUES (?, ?)", "abc", 123)
     *
     * INSERT INTO table (column, other) VALUES ("abc", 123)
     */
    open fun insertInto(str: String, vararg params: Any?) = raw("INSERT INTO $str", *params)

    /**
     * easily adds the values of insert in the query
     *
     * Query("INSERT INTO table").insertValues(
     *      "column" to "abc",
     *      "other" to 123,
     *      "password" to Query("SHA1(?)", "ultrasecret")
     * )
     *
     * INSERT INTO table (column, other, password) VALUES ("abc", 123, SHA1("ultrasecret"))
     */
    open fun insertValues(vararg value: Pair<String, Any?>): Query {
        val processed = fieldsQuestionsAndParams(*value)
        val params = ArrayList<Any?>()
        processed.forEach{ params.addAll(it.params) }

        return raw("(${processed.map{ it.field }.joinToString(",")}) VALUES (${processed.map{ it.question }.joinToString(",")})",
                *params.toTypedArray())
    }

    /**
     * easily adds the values of insert in the query
     *
     * val insertMap = mapOf(
     *      "column" to "abc",
     *      "other" to 123,
     *      "password" to Query("SHA1(?)", "ultrasecret")
     * )
     *
     * Query("INSERT INTO table").insertValues(insertMap)
     *
     * INSERT INTO table (column, other, password) VALUES ("abc", 123, SHA1("ultrasecret"))
     */
    open fun insertValues(value: Map<String, Any?>) = insertValues(*value.toList().toTypedArray())

    /**
     * adds update to the query
     *
     * Query().updateTable("table").raw("SET column = ?, other = ? WHERE id = ?", "abc", 123, 1)
     *
     * UPDATE table SET column = "abc", other = 123 WHERE id = 1
     */
    open fun updateTable(str: String, vararg params: Any?) = raw("UPDATE $str", *params)

    /**
     * easily adds the set of changes of update in the query
     *
     * Query("UPDATE table").updateSet(
     *      "column" to "abc",
     *      "other" to 123,
     *      "password" to Query("SHA1(?)", "ultrasecret")
     * ).raw("WHERE id = ?", 1)
     *
     * UPDATE table SET column = "abc", other = 123, password = SHA1("ultrasecret") WHERE id = 1
     */
    open fun updateSet(vararg value: Pair<String, Any?>): Query {
        val processed = fieldsQuestionsAndParams(*value)
        val params = ArrayList<Any?>()
        processed.forEach{ params.addAll(it.params) }

        return raw("SET ${processed.map { "${it.field} = ${it.question}" }.joinToString(", ")}",
                *params.toTypedArray())
    }

    /**
     * easily adds the set of changes of update in the query
     *
     * val updateMap = mapOf(
     *      "column" to "abc",
     *      "other" to 123,
     *      "password" to Query("SHA1(?)", "ultrasecret")
     * )
     *
     * Query("UPDATE table").updateSet(updateMap).raw("WHERE id = ?", 1)
     *
     * UPDATE table SET column = "abc", other = 123, password = SHA1("ultrasecret") WHERE id = 1
     */
    open fun updateSet(value: Map<String, Any?>) = updateSet(*value.toList().toTypedArray())

    /**
     * adds delete from on the query
     *
     * Query().deleteFrom("table").raw("WHERE id = ?", 1)
     *
     * DELETE FROM table WHERE id = 1
     */
    open fun deleteFrom(str: String, vararg params: Any?) = raw("DELETE FROM $str", *params)

    /**
     * concatenate all queries
     *
     * Query("SELECT * FROM table").concat(Query("WHERE column = ?", "abc"), Query("AND other = ?", 123))
     *
     * SELECT * FROM table WHERE column = "abc" AND other = 123
     */
    open fun concat(vararg queries: Query): Query {
        queries.forEach { raw(it.strSt, *it.paramsSt.toArray()) }
        return this
    }

    /**
     * utility function to tokenize
     *
     * Query().oddText(3, "?", ",")
     *
     * ?,?,?
     */
    open fun oddText(size: Int, text: String, separator: String) = List(size) { text }.toTypedArray().joinToString(separator)

    /**
     * execute the callback only if the boolean is true, util to avoid breaking the Query chain
     *
     * Query("SELECT * FROM table").ifThen(compareColumn) {
     *      where("column = ?", "abc")
     * }.where("other = ?", 123)
     *
     * compareColumn == true: SELECT * FROM table WHERE column = "abc" AND other = 123
     *
     * compareColumn == false: SELECT * FROM table WHERE other = 123
     */
    open fun ifThen(bool: Boolean?, callback: Query.() -> Unit): Query {
        if (bool == true) {
            callback(this)
        } else {
            ifNotFulfilled = true
        }
        return this
    }

    /**
     * executes the callback only if the boolean is true and the previous ifThen (or letThen) was not fulfilled
     *
     * Query("SELECT * FROM table").ifThen(compareColumn) {
     *      where("column = ?", "abc")
     * }.elseIf(compareOther) {
     *      where("other = ?", 123)
     * }
     */
    open fun elseIf(bool: Boolean?, callback: Query.() -> Unit): Query {
        if (ifNotFulfilled && bool == true) {
            ifNotFulfilled = false
            callback(this)
        }
        return this
    }

    /**
     * executes the callback only if the previous ifThen (or letThen) was not fulfilled
     *
     * Query("SELECT * FROM table").ifThen(compareColumn) {
     *      where("column = ?", "abc")
     * }.elseThen {
     *      where("other = ?", 123)
     * }
     *
     * compareColumn == true: SELECT * FROM table WHERE column = "abc"
     *
     * compareColumn == false: SELECT * FROM table WHERE other = 123
     */
    open fun elseThen(callback: (Query) -> Unit) = elseIf(true, callback)

    /**
     * execute the callback only if the variable is not null, util to avoid breaking the Query chain
     *
     * Query("SELECT * FROM table").letThen(columnSearch) {
     *      where("column = ?", columnSearch)
     * }.where("other = ?", 123)
     *
     * columnSearch == "abc": SELECT * FROM table WHERE column = "abc" AND other = 123
     *
     * compareColumn == null: SELECT * FROM table WHERE other = 123
     */
    open fun <T: Any> letThen(param: T?, callback: Query.(T) -> Unit): Query {
        param?.let {
            callback(this, it)
        } ?: run {
            ifNotFulfilled = true
        }
        return this
    }

    /**
     * utility function to organize name and value pairs that may have an inner query
     *
     * Query().fieldsQuestionsAndParams(
     *      "column" to "abc",
     *      "other" to Query("SHA1(?)", 123)
     * )
     *
     * [{ field: "column", question: "?", params: ["abc"] }, { field: "other", question: "SHA1(?)", params: [123] }]
     */
    open fun fieldsQuestionsAndParams(vararg value: Pair<String, Any?>): List<FieldQuestionAndParams> {
        return value.map {
            if (it.second is Query) {
                val query = it.second as Query
                FieldQuestionAndParams(it.first, query.strSt, query.paramsSt)
            } else {
                FieldQuestionAndParams(it.first, "?", arrayListOf(it.second))
            }
        }
    }

    class FieldQuestionAndParams(val field: String, val question: String, val params: List<Any?>)
}
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

    constructor(str: String?, vararg params: Any?) {
        raw(str, *params)
    }


    fun raw(str: String?, vararg params: Any?) : Query {
        ifNotFulfilled = false // reset the ifThen logic

        str?.let {
            strSt += " $str "
        }

        if (params.isNotEmpty()) {
            paramsSt.addAll(params.toList())
        }

        return this
    }

    fun concat(vararg queries: Query): Query {
        queries.forEach { raw(it.strSt, *it.paramsSt.toArray()) }
        return this
    }

    fun oddText(size: Int, text: String, separator: String) = List(size) { text }.toTypedArray().joinToString(separator)

    fun fieldsQuestionsAndParams(vararg value: Pair<String, Any?>): List<FieldQuestionAndParams> {
        return value.map {
            if (it.second is Query) {
                val query = it.second as Query
                FieldQuestionAndParams(it.first, query.strSt, query.paramsSt)
            } else {
                FieldQuestionAndParams(it.first, "?", arrayListOf(it.second))
            }
        }
    }

    fun ifThen(bool: Boolean?, callback: Query.() -> Unit): Query {
        if (bool == true) {
            callback(this)
        } else {
            ifNotFulfilled = true
        }
        return this
    }

    fun elseIf(bool: Boolean?, callback: Query.() -> Unit): Query {
        if (ifNotFulfilled && bool == true) {
            ifNotFulfilled = false
            callback(this)
        }
        return this
    }

    fun elseThen(callback: (Query) -> Unit) = elseIf(true, callback)

    fun <T: Any> letThen(param: T?, callback: Query.(T) -> Unit): Query {
        param?.let {
            callback(this, it)
        } ?: run {
            ifNotFulfilled = true
        }
        return this
    }

    fun selectRaw(str: String?, vararg params: Any?) = raw("SELECT $str", *params)
    fun selectAll() = selectRaw("*")
    fun select(vararg columns: String) = selectRaw(columns.joinToString())

    fun countRaw(str: String?, vararg params: Any?) = selectRaw("COUNT($str)", *params)
    fun countAll() = countRaw("*")
    fun count(vararg columns: String) = countRaw(columns.joinToString())

    fun from(str: String?, vararg params: Any?) = raw("FROM $str", *params)
    fun from(query: Query, name: String) = from("(").concat(query).raw(") $name")

    fun innerJoinRaw(str: String?, vararg params: Any?) = raw("INNER JOIN $str", *params)
    fun innerJoin(otherTable: String, oneColumn: String, otherColumn: String) = innerJoinRaw("$otherTable ON $oneColumn = $otherColumn")

    fun leftJoinRaw(str: String?, vararg params: Any?) = raw("LEFT JOIN $str", *params)
    fun leftJoin(otherTable: String, oneColumn: String, otherColumn: String) = leftJoinRaw("$otherTable ON $oneColumn = $otherColumn")

    fun where(str: String?, vararg params: Any?): Query {
        val word = if (!strSt.toUpperCase().contains("WHERE ")) "WHERE" else "AND"
        return raw("$word $str", *params)
    }
    fun where(vararg queries: Query) = where("(").concat(*queries).raw(")")

    fun and(str: String?, vararg params: Any?): Query {
        val word = if (strSt.isEmpty()) "" else "AND"
        return raw("$word $str", *params)
    }

    fun or(str: String?, vararg params: Any?): Query {
        val word = if (strSt.isEmpty()) "" else "OR"
        return raw("$word $str", *params)
    }

    fun having(str: String?, vararg param: Any?) = raw("HAVING $str", *param)

    fun whereEq(column: String, param: Any) = where("$column = ?", param)
    fun andEq(column: String, param: Any) = and("$column = ?", param)
    fun orEq(column: String, param: Any) = or("$column = ?", param)
    fun havingEq(column: String, param: Any) = having("$column = ?", param)

    fun whereNotEq(column: String, param: Any) = where("$column != ?", param)
    fun andNotEq(column: String, param: Any) = and("$column != ?", param)
    fun orNotEq(column: String, param: Any) = or("$column != ?", param)
    fun havingNotEq(column: String, param: Any) = having("$column != ?", param)

    fun whereGt(column: String, param: Any) = where("$column > ?", param)
    fun andGt(column: String, param: Any) = and("$column > ?", param)
    fun orGt(column: String, param: Any) = or("$column > ?", param)
    fun havingGt(column: String, param: Any) = having("$column > ?", param)

    fun whereLt(column: String, param: Any) = where("$column < ?", param)
    fun andLt(column: String, param: Any) = and("$column < ?", param)
    fun orLt(column: String, param: Any) = or("$column < ?", param)
    fun havingLt(column: String, param: Any) = having("$column < ?", param)

    fun whereGtEq(column: String, param: Any) = where("$column >= ?", param)
    fun andGtEq(column: String, param: Any) = and("$column >= ?", param)
    fun orGtEq(column: String, param: Any) = or("$column >= ?", param)
    fun havingGtEq(column: String, param: Any) = having("$column >= ?", param)

    fun whereLtEq(column: String, param: Any) = where("$column <= ?", param)
    fun andLtEq(column: String, param: Any) = and("$column <= ?", param)
    fun orLtEq(column: String, param: Any) = or("$column <= ?", param)
    fun havingLtEq(column: String, param: Any) = having("$column <= ?", param)

    fun whereNull(column: String) = where("$column IS NULL")
    fun andNull(column: String) = and("$column IS NULL")
    fun orNull(column: String) = or("$column IS NULL")
    fun havingNull(column: String) = having("$column IS NULL")

    fun whereNotNull(column: String) = where("$column IS NOT NULL")
    fun andNotNull(column: String) = and("$column IS NOT NULL")
    fun orNotNull(column: String) = or("$column IS NOT NULL")
    fun havingNotNull(column: String) = having("$column IS NOT NULL")

    fun whereBetween(column: String, p1: Any, p2: Any) = where("$column BETWEEN ? AND ?", p1, p2)
    fun andBetween(column: String, p1: Any, p2: Any) = and("$column BETWEEN ? AND ?", p1, p2)
    fun orBetween(column: String, p1: Any, p2: Any) = or("$column BETWEEN ? AND ?", p1, p2)
    fun havingBetween(column: String, p1: Any, p2: Any) = having("$column BETWEEN ? AND ?", p1, p2)

    fun whereLike(column: String, param: Any) = where("$column LIKE ?", param)
    fun andLike(column: String, param: Any) = and("$column LIKE ?", param)
    fun orLike(column: String, param: Any) = or("$column LIKE ?", param)
    fun havingLike(column: String, param: Any) = having("$column LIKE ?", param)

    fun whereIn(column: String, vararg param: Any?) = where("$column IN (${oddText(param.size, "?", ",")})", *param)
    fun andIn(column: String, vararg param: Any?) = and("$column IN (${oddText(param.size, "?", ",")})", *param)
    fun orIn(column: String, vararg param: Any?) = or("$column IN (${oddText(param.size, "?", ",")})", *param)
    fun havingIn(column: String, vararg param: Any?) = having("$column IN (${oddText(param.size, "?", ",")})", *param)

    fun whereNotIn(column: String, vararg param: Any?) = whereIn("$column NOT", param)
    fun andNotIn(column: String, vararg param: Any?) = andIn("$column NOT", param)
    fun orNotIn(column: String, vararg param: Any?) = orIn("$column NOT", param)
    fun havingNotIn(column: String, vararg param: Any?) = havingIn("$column NOT", param)

    fun orderByRaw(str: String?, vararg params: Any?) = raw("ORDER BY $str", *params)
    fun orderBy(column: String, order: String) = orderByRaw("$column $order")
    fun orderByAsc(column: String, asc: Boolean? = true) = orderBy(column, if (asc != false) "ASC" else "DESC")

    fun groupByRaw(str: String?, vararg params: Any?) = raw("GROUP BY $str", *params)
    fun groupBy(vararg columns: String) = groupByRaw(columns.joinToString())

    fun limit(index: Number = 0, size: Number) = raw("LIMIT ?, ?", index, size)

    fun insertInto(str: String, vararg params: Any?) = raw("INSERT INTO $str", *params)

    fun insertValues(vararg value: Pair<String, Any?>): Query {
        val processed = fieldsQuestionsAndParams(*value)
        val params = ArrayList<Any?>()
        processed.forEach{ params.addAll(it.params) }

        return raw("(${processed.map{ it.field }.joinToString(",")}) VALUES (${processed.map{ it.question }.joinToString(",")})",
                *params.toTypedArray())
    }

    fun insertValues(value: Map<String, Any?>) = insertValues(*value.toList().toTypedArray())

    fun updateTable(str: String, vararg params: Any?) = raw("UPDATE $str", *params)

    fun updateSet(vararg value: Pair<String, Any?>): Query {
        val processed = fieldsQuestionsAndParams(*value)
        val params = ArrayList<Any?>()
        processed.forEach{ params.addAll(it.params) }

        return raw("SET ${processed.map { "${it.field} = ${it.question}" }.joinToString(", ")}",
                *params.toTypedArray())
    }

    fun updateSet(value: Map<String, Any?>) = updateSet(*value.toList().toTypedArray())

    fun deleteFrom(str: String, vararg params: Any?) = raw("DELETE FROM $str", *params)

    class FieldQuestionAndParams(val field: String, val question: String, val params: List<Any?>)
}
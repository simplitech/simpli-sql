package br.com.simpli.sql

import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

class VirtualSelect : VirtualWhere() {
    private var allSelectFields: Array<VirtualColumn<*>> = emptyArray()
    private var allSelectRaws: Array<VirtualSelectRaw> = emptyArray()
    private var fromRM: RelationalMapper<*>? = null
    private var fromQuery: Query? = null
    private var fromQueryName: String? = null
    private var allJoins: Array<VirtualJoin> = emptyArray()
    private var allGroupBy: Array<VirtualColumn<*>> = emptyArray()
    private var allOrderBy: Array<VirtualOrder> = emptyArray()
    private var limitIndex: Int? = null
    private var limitSize: Int? = null

    // region populate methods
    fun selectFields(columns: Array<out VirtualColumn<*>>): VirtualSelect {
        allSelectFields += columns
        return this
    }

    fun select(vararg columns: VirtualColumn<*>) = selectFields(columns)

    fun selectRaw(raw: String, column: VirtualColumn<*>, alias: String? = null): VirtualSelect {
        allSelectRaws += VirtualSelectRaw(raw, column, alias)
        return this
    }

    fun from(relationalMapper: RelationalMapper<*>): VirtualSelect {
        fromRM = relationalMapper
        fromQuery = null
        fromQueryName = null
        return this
    }

    fun from(query: Query, name: String): VirtualSelect {
        fromRM = null
        fromQuery = query
        fromQueryName = name
        return this
    }

    private fun join(virtualJoin: VirtualJoin): VirtualSelect {
        allJoins += virtualJoin
        return this
    }

    fun innerJoin(rm: RelationalMapper<*>, columnFrom: VirtualColumn<*>, columnTo: VirtualColumn<*>) = join(VirtualJoin(rm, columnFrom, columnTo, inner = true))
    fun conditionalInnerJoin(rm: RelationalMapper<*>, columnFrom: VirtualColumn<*>, columnTo: VirtualColumn<*>) = join(VirtualJoin(rm, columnFrom, columnTo, inner = true, conditional = true))

    fun leftJoin(rm: RelationalMapper<*>, columnFrom: VirtualColumn<*>, columnTo: VirtualColumn<*>) = join(VirtualJoin(rm, columnFrom, columnTo, inner = false))
    fun conditionalLeftJoin(rm: RelationalMapper<*>, columnFrom: VirtualColumn<*>, columnTo: VirtualColumn<*>) = join(VirtualJoin(rm, columnFrom, columnTo, inner = false, conditional = true))

    fun groupBy(vararg column: VirtualColumn<*>): VirtualSelect {
        allGroupBy += column
        return this
    }

    fun orderBy(vararg columnAndAsc: Pair<out VirtualColumn<*>, Boolean?>): VirtualSelect {
        allOrderBy += columnAndAsc.map { VirtualOrder(it.first, it.second == true) }.toTypedArray()
        return this
    }

    fun orderBy(orderMap: Map<String, out VirtualColumn<*>>, vararg columnAndAsc: Pair<String?, Boolean?>): VirtualSelect {
        allOrderBy += columnAndAsc.mapNotNull { orderMap[it.first]?.let { first -> VirtualOrder(first, it.second == true) } }.toTypedArray()
        return this
    }

    fun limitByPage(page: Int?, size: Int?): VirtualSelect {
        val index = (page ?: 0) * (size ?: 0)
        limitByIndex(index, size)
        return this
    }

    fun limitByIndex(index: Int?, size: Int?): VirtualSelect {
        limitIndex = index
        limitSize = size
        return this
    }

    override fun whereAll(callback: VirtualWhere.() -> Unit): VirtualSelect {
        super.whereAll(callback)
        return this
    }

    override fun whereSome(callback: VirtualWhere.() -> Unit): VirtualSelect {
        super.whereSome(callback)
        return this
    }

    override fun whereGt(column: VirtualColumn<*>, param: Any): VirtualSelect {
        super.whereGt(column, param)
        return this
    }

    override fun whereLt(column: VirtualColumn<*>, param: Any): VirtualSelect {
        super.whereLt(column, param)
        return this
    }

    override fun whereGtEq(column: VirtualColumn<*>, param: Any): VirtualSelect {
        super.whereGtEq(column, param)
        return this
    }

    override fun whereLtEq(column: VirtualColumn<*>, param: Any): VirtualSelect {
        super.whereLtEq(column, param)
        return this
    }

    override fun whereDateGt(column: VirtualColumn<*>, param: Any): VirtualSelect {
        super.whereDateGt(column, param)
        return this
    }

    override fun whereDateLt(column: VirtualColumn<*>, param: Any): VirtualSelect {
        super.whereDateLt(column, param)
        return this
    }

    override fun whereDateGtEq(column: VirtualColumn<*>, param: Any): VirtualSelect {
        super.whereDateGtEq(column, param)
        return this
    }

    override fun whereDateLtEq(column: VirtualColumn<*>, param: Any): VirtualSelect {
        super.whereDateLtEq(column, param)
        return this
    }

    override fun whereEq(column: VirtualColumn<*>, param: Any?): VirtualSelect {
        super.whereEq(column, param)
        return this
    }

    override fun whereNotEq(column: VirtualColumn<*>, param: Any?): VirtualSelect {
        super.whereNotEq(column, param)
        return this
    }

    override fun whereDateEq(column: VirtualColumn<*>, param: Any?): VirtualSelect {
        super.whereDateEq(column, param)
        return this
    }

    override fun whereNull(column: VirtualColumn<*>): VirtualSelect {
        super.whereNull(column)
        return this
    }

    override fun whereNotNull(column: VirtualColumn<*>): VirtualSelect {
        super.whereNotNull(column)
        return this
    }

    override fun whereIn(column: VirtualColumn<*>, vararg param: Any?): VirtualSelect {
        super.whereIn(column, *param)
        return this
    }

    override fun whereNotIn(column: VirtualColumn<*>, vararg param: Any?): VirtualSelect {
        super.whereNotIn(column, *param)
        return this
    }

    override fun whereBetween(column: VirtualColumn<*>, param1: Any, param2: Any): VirtualSelect {
        super.whereBetween(column, param1, param2)
        return this
    }

    override fun whereSomeLikeThis(columns: Array<out VirtualColumn<*>>, param: String): VirtualSelect {
        super.whereSomeLikeThis(columns, param)
        return this
    }

    override fun whereRaw(raw: String, columns: Array<out VirtualColumn<*>>, vararg param: Any?): VirtualSelect {
        super.whereRaw(raw, columns, *param)
        return this
    }

    // endregion

    // region build query methods

    fun toQuery(): Query {
        val q = Query()
        buildSelect(q)
        buildFrom(q)
        buildJoins(q)
        buildWhere(q)
        buildGroupBy(q)
        buildOrderBy(q)
        buildLimit(q)
        return q
    }

    private fun buildSelect(query: Query) {
        query.selectRaw(
                (
                        allSelectRaws.map {
                            "${it.raw.format(it.column.toString())}${it.alias?.let { alias -> " AS $alias" } ?: ""}"
                        } +
                        allSelectFields.map { it.toString() }
                ).joinToString(","))
    }

    private fun buildFrom(query: Query) {
        fromRM?.also {
            query.from(it.aliasOrTable)
        } ?: fromQuery?.also { innerQ ->
            fromQueryName?.also { name ->
                query.from(innerQ, name)
            }
        }
    }

    private fun buildJoins(query: Query) {
        val notUsedConditionalJoins = ArrayList<VirtualJoin>()
        val usedJoins = ArrayList<VirtualJoin>()

        // separating joins that are used (except on other joins) from unused
        allJoins.forEach {
            if (it.conditional && (!allSelectFields.any { s -> s.aliasOrTable == it.rm.aliasOrTable }
                            && !allSelectRaws.any { s -> s.column.aliasOrTable == it.rm.aliasOrTable }
                            && !allGroupBy.any { s -> s.aliasOrTable == it.rm.aliasOrTable }
                            && !allOrderBy.any { s -> s.column.aliasOrTable == it.rm.aliasOrTable }
                            && !hasWhereWithAliasOrTable(it.rm.aliasOrTable))) {
                notUsedConditionalJoins.add(it)
            } else {
                usedJoins.add(it)
            }
        }

        // making a recap by searching unused joins on used joins references
        var i = 0
        while (i < usedJoins.size) {
            val used = usedJoins[i]
            for (j in notUsedConditionalJoins.indices.reversed()) {
                val notUsed = notUsedConditionalJoins[j]
                if (used.columnFrom.aliasOrTable == notUsed.rm.aliasOrTable
                        || used.columnTo.aliasOrTable == notUsed.rm.aliasOrTable) {
                    notUsedConditionalJoins.removeAt(j)
                    usedJoins.add(notUsed)
                }
            }
            i++
        }

        // sorting the joins by reference, so the query doesn't bug
        val sortedJoins = ArrayList<VirtualJoin>()
        i = 0
        while (i < usedJoins.size) {
            val used = usedJoins[i]
            if (used.columnTo.aliasOrTable == fromRM?.aliasOrTable || sortedJoins.any { it.rm.aliasOrTable == used.columnTo.aliasOrTable }) {
                sortedJoins.add(used)
                usedJoins.removeAt(i)
                i = 0
            } else {
                i++
            }
        }

        // building
        sortedJoins.forEach {
            if (it.inner) {
                query.innerJoin(it.rm.table, it.rm.alias, it.columnFrom.toString(), it.columnTo.toString())
            } else {
                query.leftJoin(it.rm.table, it.rm.alias, it.columnFrom.toString(), it.columnTo.toString())
            }
        }
    }

    private fun buildGroupBy(query: Query) {
        query.groupBy(*allGroupBy.map { it.toString() }.toTypedArray())
    }

    private fun buildOrderBy(query: Query) {
        query.orderBy(*allOrderBy.map { it.column.toString() to it.asc }.toTypedArray())
    }

    private fun buildLimit(query: Query) {
        limitSize?.let {
            query.limit(limitIndex ?: 0, it)
        }
    }

    // endregion

    override fun toString(): String {
        return toQuery().toString()
    }
}

class VirtualColumn<T>(val table: String, val alias: String?, val column: String, private val getter: T.() -> Any?, private val setter: T.(builder: VirtualBuilder) -> Unit) {
    override fun toString(): String {
        return "${alias ?: table}.$column"
    }
    val aliasOrTable get() = alias ?: table
    fun build(model: T, rs: ResultSet) {
        setter(model, VirtualBuilder(this, rs))
    }
    fun useValue(model: T): Any? {
        return getter(model)
    }
}

class VirtualSelectRaw(val raw: String, val column: VirtualColumn<*>, val alias: String? = null)
private class VirtualJoin(val rm: RelationalMapper<*>, val columnFrom: VirtualColumn<*>, val columnTo: VirtualColumn<*>, val inner: Boolean = true, val conditional: Boolean = false)
private class VirtualOrder(val column: VirtualColumn<*>, val asc: Boolean)

open class VirtualWhere {
    private var allWhereAll: Array<VirtualWhere.() -> Unit> = emptyArray()
    private var allWhereSome: Array<VirtualWhere.() -> Unit> = emptyArray()
    private var allVirtualWhereItemOneParam: Array<VirtualWhereItemOneParam> = emptyArray()
    private var allVirtualWhereItemNullableParam: Array<VirtualWhereItemNullableParam> = emptyArray()
    private var allVirtualWhereItemNoParam: Array<VirtualWhereItemNoParam> = emptyArray()
    private var allVirtualWhereItemVarargParam: Array<VirtualWhereItemVarargParam> = emptyArray()
    private var allVirtualWhereBetween: Array<VirtualWhereBetween> = emptyArray()
    private var allVirtualWhereSomeLikeThis: Array<VirtualWhereSomeLikeThis> = emptyArray()
    private var allVirtualWhereRaw: Array<VirtualWhereRaw> = emptyArray()

    open fun whereAll(callback: VirtualWhere.() -> Unit): VirtualWhere {
        allWhereAll += callback
        return this
    }

    open fun whereSome(callback: VirtualWhere.() -> Unit): VirtualWhere {
        allWhereSome += callback
        return this
    }

    private fun whereOneParam(column: VirtualColumn<*>, type: VirtualWhereTypeOneParam, param: Any): VirtualWhere {
        allVirtualWhereItemOneParam += VirtualWhereItemOneParam(column, type, param)
        return this
    }

    private fun whereNullableParam(column: VirtualColumn<*>, type: VirtualWhereTypeNullableParam, param: Any?): VirtualWhere {
        allVirtualWhereItemNullableParam += VirtualWhereItemNullableParam(column, type, param)
        return this
    }

    private fun whereNoParam(column: VirtualColumn<*>, type: VirtualWhereTypeNoParam): VirtualWhere {
        allVirtualWhereItemNoParam += VirtualWhereItemNoParam(column, type)
        return this
    }

    private fun whereVarargParam(column: VirtualColumn<*>, type: VirtualWhereTypeVarargParam, vararg param: Any?): VirtualWhere {
        allVirtualWhereItemVarargParam += VirtualWhereItemVarargParam(column, type, *param)
        return this
    }

    open fun whereGt(column: VirtualColumn<*>, param: Any) = whereOneParam(column, VirtualWhereTypeOneParam.Gt, param)
    open fun whereLt(column: VirtualColumn<*>, param: Any) = whereOneParam(column, VirtualWhereTypeOneParam.Lt, param)
    open fun whereGtEq(column: VirtualColumn<*>, param: Any) = whereOneParam(column, VirtualWhereTypeOneParam.GtEq, param)
    open fun whereLtEq(column: VirtualColumn<*>, param: Any) = whereOneParam(column, VirtualWhereTypeOneParam.LtEq, param)
    open fun whereDateGt(column: VirtualColumn<*>, param: Any) = whereOneParam(column, VirtualWhereTypeOneParam.DateGt, param)
    open fun whereDateLt(column: VirtualColumn<*>, param: Any) = whereOneParam(column, VirtualWhereTypeOneParam.DateLt, param)
    open fun whereDateGtEq(column: VirtualColumn<*>, param: Any) = whereOneParam(column, VirtualWhereTypeOneParam.DateGtEq, param)
    open fun whereDateLtEq(column: VirtualColumn<*>, param: Any) = whereOneParam(column, VirtualWhereTypeOneParam.DateLtEq, param)

    open fun whereEq(column: VirtualColumn<*>, param: Any?) = whereNullableParam(column, VirtualWhereTypeNullableParam.Eq, param)
    open fun whereNotEq(column: VirtualColumn<*>, param: Any?) = whereNullableParam(column, VirtualWhereTypeNullableParam.NotEq, param)
    open fun whereDateEq(column: VirtualColumn<*>, param: Any?) = whereNullableParam(column, VirtualWhereTypeNullableParam.DateEq, param)

    open fun whereNull(column: VirtualColumn<*>) = whereNoParam(column, VirtualWhereTypeNoParam.Null)
    open fun whereNotNull(column: VirtualColumn<*>) = whereNoParam(column, VirtualWhereTypeNoParam.NotNull)

    open fun whereIn(column: VirtualColumn<*>, vararg param: Any?) = whereVarargParam(column, VirtualWhereTypeVarargParam.In, *param)
    open fun whereNotIn(column: VirtualColumn<*>, vararg param: Any?) = whereVarargParam(column, VirtualWhereTypeVarargParam.NotIn, *param)

    open fun whereBetween(column: VirtualColumn<*>, param1: Any, param2: Any): VirtualWhere {
        allVirtualWhereBetween += VirtualWhereBetween(column, param1, param2)
        return this
    }

    open fun whereSomeLikeThis(columns: Array<out VirtualColumn<*>>, param: String): VirtualWhere {
        allVirtualWhereSomeLikeThis += VirtualWhereSomeLikeThis(columns, param)
        return this
    }

    open fun whereRaw(raw: String, columns: Array<out VirtualColumn<*>>, vararg param: Any?): VirtualWhere {
        allVirtualWhereRaw += VirtualWhereRaw(raw, columns, *param)
        return this
    }

    protected fun buildWhere(query: Query, useSameQuery: Boolean = false) {
        val whereQuery = if (useSameQuery) query else Query() // making a separated where to avoid bugs on WHERE/AND (issue #19)
        allVirtualWhereItemOneParam.forEach {
            when (it.type) {
                VirtualWhereTypeOneParam.Gt -> whereQuery.whereGt(it.column.toString(), it.param)
                VirtualWhereTypeOneParam.Lt -> whereQuery.whereLt(it.column.toString(), it.param)
                VirtualWhereTypeOneParam.GtEq -> whereQuery.whereGtEq(it.column.toString(), it.param)
                VirtualWhereTypeOneParam.LtEq -> whereQuery.whereLtEq(it.column.toString(), it.param)
                VirtualWhereTypeOneParam.DateGt -> whereQuery.whereDateGt(it.column.toString(), it.param)
                VirtualWhereTypeOneParam.DateLt -> whereQuery.whereDateLt(it.column.toString(), it.param)
                VirtualWhereTypeOneParam.DateGtEq -> whereQuery.whereDateGtEq(it.column.toString(), it.param)
                VirtualWhereTypeOneParam.DateLtEq -> whereQuery.whereDateLtEq(it.column.toString(), it.param)
            }
        }

        allVirtualWhereItemNullableParam.forEach {
            when (it.type) {
                VirtualWhereTypeNullableParam.Eq -> whereQuery.whereEq(it.column.toString(), it.param)
                VirtualWhereTypeNullableParam.NotEq -> whereQuery.whereNotEq(it.column.toString(), it.param)
                VirtualWhereTypeNullableParam.DateEq -> whereQuery.whereDateEq(it.column.toString(), it.param)
            }
        }

        allVirtualWhereItemNoParam.forEach {
            when (it.type) {
                VirtualWhereTypeNoParam.Null -> whereQuery.whereNull(it.column.toString())
                VirtualWhereTypeNoParam.NotNull -> whereQuery.whereNotNull(it.column.toString())
            }
        }

        allVirtualWhereItemVarargParam.forEach {
            when (it.type) {
                VirtualWhereTypeVarargParam.In -> whereQuery.whereIn(it.column.toString(), *it.param)
                VirtualWhereTypeVarargParam.NotIn -> whereQuery.whereNotIn(it.column.toString(), *it.param)
            }
        }

        allVirtualWhereBetween.forEach {
            whereQuery.whereBetween(it.column.toString(), it.param1, it.param2)
        }

        allVirtualWhereSomeLikeThis.forEach {
            whereQuery.whereSomeLikeThis(it.columns.map { col -> col.toString() }.toTypedArray(), it.param)
        }

        allVirtualWhereRaw.forEach {
            whereQuery.where(it.raw.format(*it.columns.map { col -> col.toString() }.toTypedArray()), *it.param)
        }

        allWhereAll.forEach {
            val inner = VirtualWhere()
            it(inner)
            whereQuery.whereAll {
                inner.buildWhere(this, true)
            }
        }

        allWhereSome.forEach {
            val inner = VirtualWhere()
            it(inner)
            whereQuery.whereSome {
                inner.buildWhere(this, true)
            }
        }

        if (!useSameQuery) {
            query.concat(whereQuery)
        }
    }

    protected fun hasWhereWithAliasOrTable(aliasOrTable: String): Boolean {
        return allVirtualWhereItemOneParam.any { w -> w.column.aliasOrTable == aliasOrTable }
                || allVirtualWhereItemNullableParam.any { w -> w.column.aliasOrTable == aliasOrTable }
                || allVirtualWhereItemNoParam.any { w -> w.column.aliasOrTable == aliasOrTable }
                || allVirtualWhereItemVarargParam.any { w -> w.column.aliasOrTable == aliasOrTable }
                || allVirtualWhereBetween.any { w -> w.column.aliasOrTable == aliasOrTable }
                || allVirtualWhereSomeLikeThis.any { w -> w.columns.any { c -> c.aliasOrTable == aliasOrTable } }
                || allVirtualWhereRaw.any { w -> w.columns.any { c -> c.aliasOrTable == aliasOrTable } }
                || allWhereAll.any {
            val inner = VirtualWhere()
            it(inner)
            inner.hasWhereWithAliasOrTable(aliasOrTable)
        }
                || allWhereSome.any {
            val inner = VirtualWhere()
            it(inner)
            inner.hasWhereWithAliasOrTable(aliasOrTable)
        }
    }
}

private class VirtualWhereItemOneParam(val column: VirtualColumn<*>, val type: VirtualWhereTypeOneParam, val param: Any)
private class VirtualWhereItemNullableParam(val column: VirtualColumn<*>, val type: VirtualWhereTypeNullableParam, val param: Any?)
private class VirtualWhereItemNoParam(val column: VirtualColumn<*>, val type: VirtualWhereTypeNoParam)
private class VirtualWhereItemVarargParam(val column: VirtualColumn<*>, val type: VirtualWhereTypeVarargParam, vararg val param: Any?)
private class VirtualWhereBetween(val column: VirtualColumn<*>, val param1: Any, val param2: Any)
private class VirtualWhereSomeLikeThis(val columns: Array<out VirtualColumn<*>>, val param: String)
private class VirtualWhereRaw(val raw: String, val columns: Array<out VirtualColumn<*>>, vararg val param: Any?)

private enum class VirtualWhereTypeOneParam {
    Gt,
    Lt,
    GtEq,
    LtEq,
    DateGt,
    DateLt,
    DateGtEq,
    DateLtEq,
}

private enum class VirtualWhereTypeNullableParam {
    Eq,
    NotEq,
    DateEq
}

private enum class VirtualWhereTypeNoParam {
    Null,
    NotNull
}

private enum class VirtualWhereTypeVarargParam {
    In,
    NotIn
}

abstract class RelationalMapper<T> {
    abstract val table: String
    open var alias: String? = null

    val aliasOrTable: String get() = alias ?: table

    fun col(column: String, getter: T.() -> Any?, build: T.(builder: VirtualBuilder) -> Unit) = VirtualColumn(table, alias, column, getter, build)

    fun colsToMap(model: T, vararg columns: VirtualColumn<T>) = columns.map { it.toString() to it.useValue(model) }.toMap()
}

class VirtualBuilder(val column: VirtualColumn<*>, val rs: ResultSet) {
    inline fun <reified T> value(): T {
        return if (null is T) {
            when (T::class) {
                String::class -> string as T
                Date::class -> timestamp as T
                Boolean::class -> booleanOrNull as T
                Int::class -> intOrNull as T
                Long::class -> longOrNull as T
                Double::class -> doubleOrNull as T
                else -> null as T
            }
        } else {
            when (T::class) {
                Boolean::class -> boolean as T
                Int::class -> int as T
                Long::class -> long as T
                Double::class -> double as T
                else -> throw TypeCastException()
            }
        }
    }

    val string: String? get() {
        return rs.getString(column.toString())
    }

    val timestamp: Timestamp? get() {
        return rs.getTimestamp(column.toString())
    }

    val boolean: Boolean get() {
        return rs.getBoolean(column.toString())
    }

    val int: Int get() {
        return rs.getInt(column.toString())
    }

    val long: Long get() {
        return rs.getLong(column.toString())
    }

    val double: Double get() {
        return rs.getDouble(column.toString())
    }

    val booleanOrNull: Boolean? get() {
        val v = rs.getBoolean(column.toString())
        return if (rs.wasNull()) null else v
    }

    val intOrNull: Int? get() {
        val v = rs.getInt(column.toString())
        return if (rs.wasNull()) null else v
    }

    val longOrNull: Long? get() {
        val v = rs.getLong(column.toString())
        return if (rs.wasNull()) null else v
    }

    val doubleOrNull: Double? get() {
        val v = rs.getDouble(column.toString())
        return if (rs.wasNull()) null else v
    }
}

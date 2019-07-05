package br.com.simpli.sql

import br.com.simpli.model.EnglishLanguage
import br.com.simpli.model.LanguageHolder
import br.com.simpli.model.RespException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Executes the Queries
 *
 * @author gil
 */
@Deprecated("Use a Connector")
open class Dao(protected var con: Connection, protected var lang: LanguageHolder) {

    protected fun execute(query: Query): GenericResult {

        var statem: PreparedStatement? = null
        val genResult = GenericResult()
        var keys: ResultSet? = null
        try {
            statem = prepareStatement(con, Statement.RETURN_GENERATED_KEYS, query.strSt, *query.paramsSt.toTypedArray())
            genResult.affectedRows = statem.executeUpdate()

            keys = statem.generatedKeys

            if (keys.next()) {
                genResult.key = keys.getLong(1)
            }
            closeStatementAndResult(statem, keys)
        } catch (ex: Exception) {
            Logger.getLogger(Dao::class.java.name).log(Level.INFO, statem.toString())
            closeStatementAndResult(statem, keys)
            val re = RespException(lang.unexpectedError())
            re.initCause(ex)
            throw re
        }

        return genResult

    }

    @Deprecated("Use a Query as parameter", ReplaceWith("execute(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun execute(strSt: String, vararg objStatement: Any?) = execute(Query().raw(strSt, *objStatement))


    protected fun <T> getResp(query: Query, callback: (rs: ResultSet) -> T): T {

        var statem: PreparedStatement? = null
        var rs: ResultSet? = null
        var result: T?

        try {
            statem = prepareStatement(con, null, query.strSt, *query.paramsSt.toTypedArray())
            rs = statem.executeQuery()

            result = callback(rs)
            closeStatementAndResult(statem, rs)
        } catch (ex: Exception) {
            Logger.getLogger(Dao::class.java.name).log(Level.INFO, statem.toString())
            closeStatementAndResult(statem, rs)
            val re = RespException(lang.unexpectedError())
            re.initCause(ex)
            throw re
        }

        return result

    }

    @Deprecated("Use a Query as parameter", ReplaceWith("getResp(Query().raw(strSt, *objStatement), callback)", "br.com.simpli.sql.Query"))
    protected fun <T> getResp(strSt: String, callback: (rs: ResultSet) -> T, vararg objStatement: Any?) = getResp(Query().raw(strSt, *objStatement), callback)

    protected fun <T> getList(query: Query, callback: (rs: ResultSet) -> T) = getResp(query) {
        val result = LinkedList<T>()

        while (it.next()) {
            result.add(callback(it))
        }

        result
    }

    @Deprecated("Use a Query as parameter", ReplaceWith("getList(Query().raw(strSt, *objStatement), callback)", "br.com.simpli.sql.Query"))
    protected fun <T> getList(strSt: String, callback: (rs: ResultSet) -> T, vararg objStatement: Any?) = getList(Query().raw(strSt, *objStatement), callback)

    protected fun <T> getOne(query: Query, callback: (rs: ResultSet) -> T): T? = getResp(query) {
        if (it.next()) callback(it) else null
    }

    @Deprecated("Use a Query as parameter", ReplaceWith("getOne(Query().raw(strSt, *objStatement), callback)", "br.com.simpli.sql.Query"))
    protected fun <T> getOne(strSt: String, callback: (rs: ResultSet) -> T, vararg objStatement: Any?) = getOne(Query().raw(strSt, *objStatement), callback)

    protected fun exist(query: Query) = getResp(query) { it.next() }

    @Deprecated("Use a Query as parameter", ReplaceWith("exist(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun exist(strSt: String, vararg objStatement: Any?) = exist(Query().raw(strSt, *objStatement))

    protected fun getFirstInt(query: Query) = getOne(query) { it.getIntOrNull(1) }
    protected fun getIntList(query: Query) = getList(query) { it.getIntOrNull(1) }

    @Deprecated("Use a Query as parameter", ReplaceWith("getFirstInt(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun getFirstInt(strSt: String, vararg objStatement: Any?) = getFirstInt(Query().raw(strSt, *objStatement))
    @Deprecated("Use a Query as parameter", ReplaceWith("getIntList(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun getIntList(strSt: String, vararg objStatement: Any?) = getIntList(Query().raw(strSt, *objStatement))

    protected fun getFirstLong(query: Query) = getOne(query) { it.getLongOrNull(1) }
    protected fun getLongList(query: Query) = getList(query) { it.getLongOrNull(1) }

    @Deprecated("Use a Query as parameter", ReplaceWith("getFirstLong(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun getFirstLong(strSt: String, vararg objStatement: Any?) = getFirstLong(Query().raw(strSt, *objStatement))
    @Deprecated("Use a Query as parameter", ReplaceWith("getLongList(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun getLongList(strSt: String, vararg objStatement: Any?) = getLongList(Query().raw(strSt, *objStatement))

    protected fun getFirstDouble(query: Query) = getOne(query) { it.getDoubleOrNull(1) }
    protected fun getDoubleList(query: Query) = getList(query) { it.getDoubleOrNull(1) }

    @Deprecated("Use a Query as parameter", ReplaceWith("getFirstDouble(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun getFirstDouble(strSt: String, vararg objStatement: Any?) = getFirstDouble(Query().raw(strSt, *objStatement))
    @Deprecated("Use a Query as parameter", ReplaceWith("getDoubleList(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun getDoubleList(strSt: String, vararg objStatement: Any?) = getDoubleList(Query().raw(strSt, *objStatement))

    protected fun getFirstString(query: Query) = getOne(query) { it.getString(1) }
    protected fun getStringList(query: Query) = getList(query) { it.getString(1) }

    @Deprecated("Use a Query as parameter", ReplaceWith("getFirstString(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun getFirstString(strSt: String, vararg objStatement: Any?) = getFirstString(Query().raw(strSt, *objStatement))
    @Deprecated("Use a Query as parameter", ReplaceWith("getStringList(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun getStringList(strSt: String, vararg objStatement: Any?) = getStringList(Query().raw(strSt, *objStatement))

    protected fun getFirstDate(query: Query) = getOne(query) { it.getTimestamp(1) }
    protected fun getDateList(query: Query) = getList(query) { it.getTimestamp(1) }

    @Deprecated("Use a Query as parameter", ReplaceWith("getFirstDate(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun getFirstDate(strSt: String, vararg objStatement: Any?) = getFirstDate(Query().raw(strSt, *objStatement))
    @Deprecated("Use a Query as parameter", ReplaceWith("getDateList(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun getDateList(strSt: String, vararg objStatement: Any?) = getDateList(Query().raw(strSt, *objStatement))

    protected fun getFirstBoolean(query: Query) = getOne(query) { it.getBooleanOrNull(1) }
    protected fun getBooleanList(query: Query) = getList(query) { it.getBooleanOrNull(1) }

    @Deprecated("Use a Query as parameter", ReplaceWith("getFirstBoolean(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun getFirstBoolean(strSt: String, vararg objStatement: Any?) = getFirstBoolean(Query().raw(strSt, *objStatement))
    @Deprecated("Use a Query as parameter", ReplaceWith("getBooleanList(Query().raw(strSt, *objStatement))", "br.com.simpli.sql.Query"))
    protected fun getBooleanList(strSt: String, vararg objStatement: Any?) = getBooleanList(Query().raw(strSt, *objStatement))

    /**
     * prepare the statement
     *
     * @param con connection to be used
     * @param strStatement your query with ? for the variables
     * @param objStatement variables that will replace the ?
     * @return the prepared statement
     * @throws SQLException if (1) couldn't get the connection; (2) coudn't
     * prepare the statement; (3) coudn't understand the object
     */
    @Throws(SQLException::class)
    protected fun prepareStatement(con: Connection, autoGeneratedKeys: Int?, strStatement: String, vararg objStatement: Any?): PreparedStatement {
        val statem = if (autoGeneratedKeys != null) {
            con.prepareStatement(strStatement, autoGeneratedKeys)
        } else {
            con.prepareStatement(strStatement)
        }

        var i = 1
        for (o in objStatement) {
            var os = o

            if (os != null && os.javaClass == Date::class.java) {
                os = java.sql.Timestamp((os as Date).time)
            }

            statem.setObject(i, os)
            i++
        }

        return statem
    }

    /**
     * closes the statement and resultset
     *
     * @param pstmt
     * @param rs
     */
    protected fun closeStatementAndResult(pstmt: PreparedStatement?, rs: ResultSet?) {

        if (rs != null) {
            try {
                rs.close()
            } catch (ex: Exception) {
                try {
                    Logger.getLogger(Dao::class.java.name).log(Level.SEVERE, ex.message, ex)
                } catch (ex2: Exception) {
                }

            }

        }

        if (pstmt != null) {
            try {
                pstmt.close()
            } catch (ex: Exception) {
                try {
                    Logger.getLogger(Dao::class.java.name).log(Level.SEVERE, ex.message, ex)
                } catch (ex2: Exception) {
                }

            }

        }
    }

    /**
     * result of the execute. affectedRows is the amount of affected rows; key id
     * the id of the insert;
     */
    inner class GenericResult {
        var affectedRows: Int = 0
        var key: Long = 0
    }

    companion object {
        fun executeForTest(con: Connection, query: Query): GenericResult {
            return Dao(con, EnglishLanguage()).execute(query)
        }

        fun existForTest(con: Connection, query: Query): Boolean {
            return Dao(con, EnglishLanguage()).exist(query)
        }
    }

}
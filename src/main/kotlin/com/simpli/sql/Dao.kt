package com.simpli.sql

import com.simpli.model.EnglishLanguage
import com.simpli.model.LanguageHolder
import com.simpli.model.RespException
import java.sql.*
import java.util.*
import java.util.Date
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Uma solucao para encapsular o dao, deixando-o mais simples e legivel
 *
 * @author gil
 */
class Dao(protected var con: Connection, protected var lang: LanguageHolder) {

    /**
     * to insert or make an update
     *
     * @param strStatement your query with ? for the variables
     * @param objStatement variables that will replace the ?
     * @return database response
     */
    protected fun update(strStatement: String, vararg objStatement: Any): GenericResult {

        var statem: PreparedStatement? = null
        val genResult = GenericResult()
        var keys: ResultSet? = null
        try {
            statem = prepareUpdate(con, strStatement, *objStatement)
            Logger.getLogger(Dao::class.java.name).log(Level.INFO, statem.toString())
            genResult.affectedRows = statem.executeUpdate()

            keys = statem.generatedKeys

            if (keys.next()) {
                genResult.key = keys.getLong(1)
            }
            closeStatementAndResult(statem, keys)
        } catch (ex: Exception) {
            closeStatementAndResult(statem, keys)
            val re = RespException(lang.unexpectedError())
            re.initCause(ex)
            throw re
        }

        return genResult

    }

    /**
     * to make a select
     *
     * @param <T> object type to be constructed by the select
     * @param strStatement your query with ? for the variables
     * @param callback a callback to construct the resulting object with the
     * ResultSet
     * @param objStatement variables that will replace the ?
     * @return constructed object
    </T> */
    protected fun <T> select(strStatement: String, callback: (rs: ResultSet) -> T, vararg objStatement: Any): T {

        var statem: PreparedStatement? = null
        var rs: ResultSet? = null
        var result: T? = null

        try {
            statem = prepareSelect(con, strStatement, *objStatement)
            Logger.getLogger(Dao::class.java.name).log(Level.INFO, statem.toString())
            rs = statem.executeQuery()

            result = callback(rs)
            closeStatementAndResult(statem, rs)
        } catch (ex: Exception) {
            closeStatementAndResult(statem, rs)
            val re = RespException(lang.unexpectedError())
            re.initCause(ex)
            throw re
        }

        return result

    }

    /**
     * to make a select with an object list
     *
     * @param <T> type of list that will be constructed by the select
     * @param strStatement your query with ? for the variables
     * @param callback a callback to construct each object of the list with the
     * ResultSet
     * @param objStatement variables that will replace the ?
     * @return constructed list
    </T> */
    protected fun <T> selectList(strStatement: String, callback: (rs: ResultSet) -> T, vararg objStatement: Any): List<T> {

        return select(strStatement, { rs: ResultSet ->
            val result = LinkedList<T>()

            while (rs.next()) {
                result.add(callback(rs))
            }

            result
        }, *objStatement)

    }

    /**
     * to make a select with a single object
     *
     * @param <T> type of object that will be constructed by the select
     * @param strStatement your query with ? for the variables
     * @param callback a callback to construct each object of the list with the
     * ResultSet
     * @param objStatement variables that will replace the ?
     * @return constructed object
    </T> */
    protected fun <T> selectOne(strStatement: String, callback: (rs: ResultSet) -> T, vararg objStatement: Any): T? {

        return select(strStatement, { rs: ResultSet ->
            if (rs.next()) {
                callback(rs)
            } else null
        }, *objStatement)

    }

    /**
     * checks if the query returns any data
     *
     * @param strStatement your query with ? for the variables
     * @param objStatement variables that will replace the ?
     * @return true if there is data in the query
     */
    protected fun exist(strStatement: String, vararg objStatement: Any): Boolean {
        return select(strStatement, { rs: ResultSet -> rs.next() }, *objStatement) ?: false
    }

    protected fun selectFirstInt(strStatement: String, vararg objStatement: Any): Int? {
        return selectOne(strStatement, { rs: ResultSet -> rs.getIntOrNull(1) }, *objStatement)
    }

    protected fun selectFirstLong(strStatement: String, vararg objStatement: Any): Long? {
        return selectOne(strStatement, { rs: ResultSet -> rs.getLongOrNull(1) }, *objStatement)
    }

    protected fun selectFirstDouble(strStatement: String, vararg objStatement: Any): Double? {
        return selectOne(strStatement, { rs: ResultSet -> rs.getDoubleOrNull(1) }, *objStatement)
    }

    protected fun selectFirstString(strStatement: String, vararg objStatement: Any): String? {
        return selectOne(strStatement, { rs: ResultSet -> rs.getString(1) }, *objStatement)
    }

    protected fun selectFirstDate(strStatement: String, vararg objStatement: Any): Date? {
        return selectOne(strStatement, { rs: ResultSet -> rs.getTimestamp(1) }, *objStatement)
    }

    protected fun selectFirstBoolean(strStatement: String, vararg objStatement: Any): Boolean? {
        return selectOne(strStatement, { rs: ResultSet -> rs.getBooleanOrNull(1) }, *objStatement)
    }

    protected fun insertWithMap(tablename: String, columnNamesAndValues: Map<String, Any>): GenericResult {
        val columnNames = columnNamesAndValues.keys.toTypedArray()

        return update("INSERT INTO "
                + tablename
                + " ( "
                + stringifyInsertColumns(columnNames)
                + ") VALUES "
                + generateInsertCommas(1, columnNamesAndValues.size),
                *columnNamesAndValues.values.toTypedArray())
    }

    protected fun updateWithMap(tablename: String, columnNamesAndValues: Map<String, Any>, clause: String, vararg params: Any): GenericResult {
        val columnNames = columnNamesAndValues.keys.toTypedArray()
        val values = ArrayList(columnNamesAndValues.values)
        values.addAll(Arrays.asList(*params))

        return update("UPDATE "
                + tablename
                + " SET "
                + stringifyUpdateColumns(columnNames)
                + clause,
                *values.toTypedArray())
    }

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
    protected fun prepareUpdate(con: Connection, strStatement: String, vararg objStatement: Any): PreparedStatement {
        val statem = con.prepareStatement(strStatement, Statement.RETURN_GENERATED_KEYS)

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
     * prepara prepare the statement to select
     *
     * @param con connection to be used
     * @param strStatement your query with ? for the variables
     * @param objStatement variables that will replace the ?
     * @return the prepared statement
     * @throws SQLException if (1) couldn't get the connection; (2) coudn't
     * prepare the statement; (3) coudn't understand the object
     */
    @Throws(SQLException::class)
    protected fun prepareSelect(con: Connection, strStatement: String, vararg objStatement: Any): PreparedStatement {
        val statem = con.prepareStatement(strStatement)

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

    protected fun nullToFalse(obj: Boolean?): Boolean {
        return obj ?: false
    }

    protected fun nullToZero(obj: Int?): Int {
        return obj ?: 0
    }

    protected fun nullToZero(obj: Long?): Long {
        return obj ?: 0
    }

    protected fun nullToZero(obj: Double?): Double {
        return obj ?: 0.0
    }

    protected fun nullToZero(obj: Float?): Float {
        return obj ?: 0F
    }

    protected fun generateInsertCommas(lines: Int, columns: Int): String {
        val sb = StringBuilder()

        sb.append("(")

        for (j in 0 until columns) {
            if (j > 0) {
                sb.append(", ")
            }

            sb.append("?")
        }

        sb.append(")")

        val l = sb.toString()
        sb.setLength(0)

        for (i in 0 until lines) {
            if (i > 0) {
                sb.append(", ")
            }

            sb.append(l)
        }

        return sb.toString()
    }

    protected fun stringifyUpdateColumns(columns: Array<String>): String {
        val sb = StringBuilder()

        for (j in columns.indices) {
            if (j > 0) {
                sb.append(", ")
            }

            sb.append(columns[j])
            sb.append(" = ?")
        }

        sb.append(" ")

        return sb.toString()
    }

    protected fun stringifyInsertColumns(columns: Array<String>): String {
        return columns.joinToString(",") + " "
    }

    /**
     * result of the update. affectedRows is the amount of affected rows; key id
     * the id of the insert;
     */
    inner class GenericResult {
        var affectedRows: Int = 0
        var key: Long = 0
    }

    companion object {
        fun updateForTest(con: Connection, strStatement: String, vararg objStatement: Any): GenericResult {
            return Dao(con, EnglishLanguage()).update(strStatement, *objStatement)
        }

        fun existForTest(con: Connection, strStatement: String, vararg objStatement: Any): Boolean {
            return Dao(con, EnglishLanguage()).exist(strStatement, *objStatement)
        }
    }

}
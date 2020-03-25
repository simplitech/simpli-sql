package br.com.simpli.sql

import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp

/**
 *
 * @author gil
 */

open class ResultBuilder(private val allowedColumns: Array<String>, private val rs: ResultSet, private val alias: String? = null) {

    fun isAllowed(column: String): Boolean {
        return allowedColumns.contains(column) || allowedColumns.contains(putAlias(column))
    }

    private fun putAlias(columnLabel: String): String {
        return if (alias != null) "$alias.$columnLabel" else columnLabel
    }

    @Throws(SQLException::class)
    fun getString(columnLabel: String): String? {
        if (!isAllowed(columnLabel)) return null
        return rs.getString(putAlias(columnLabel))
    }

    @Throws(SQLException::class)
    fun getTimestamp(columnLabel: String): Timestamp? {
        if (!isAllowed(columnLabel)) return null
        return rs.getTimestamp(putAlias(columnLabel))
    }

    @Throws(SQLException::class)
    fun getBoolean(columnLabel: String): Boolean {
        if (!isAllowed(columnLabel)) return false
        return rs.getBoolean(putAlias(columnLabel))
    }

    @Throws(SQLException::class)
    fun getInt(columnLabel: String): Int {
        if (!isAllowed(columnLabel)) return 0
        return rs.getInt(putAlias(columnLabel))
    }

    @Throws(SQLException::class)
    fun getLong(columnLabel: String): Long {
        if (!isAllowed(columnLabel)) return 0L
        return rs.getLong(putAlias(columnLabel))
    }

    @Throws(SQLException::class)
    fun getDouble(columnLabel: String): Double {
        if (!isAllowed(columnLabel)) return 0.0
        return rs.getDouble(putAlias(columnLabel))
    }

    @Throws(SQLException::class)
    fun getBooleanOrNull(columnLabel: String): Boolean? {
        if (!isAllowed(columnLabel)) return null
        val v = rs.getBoolean(putAlias(columnLabel))
        return if (rs.wasNull()) null else v
    }

    @Throws(SQLException::class)
    fun getIntOrNull(columnLabel: String): Int? {
        if (!isAllowed(columnLabel)) return null
        val v = rs.getInt(putAlias(columnLabel))
        return if (rs.wasNull()) null else v
    }

    @Throws(SQLException::class)
    fun getLongOrNull(columnLabel: String): Long? {
        if (!isAllowed(columnLabel)) return null
        val v = rs.getLong(putAlias(columnLabel))
        return if (rs.wasNull()) null else v
    }

    @Throws(SQLException::class)
    fun getDoubleOrNull(columnLabel: String): Double? {
        if (!isAllowed(columnLabel)) return null
        val v = rs.getDouble(putAlias(columnLabel))
        return if (rs.wasNull()) null else v
    }
}

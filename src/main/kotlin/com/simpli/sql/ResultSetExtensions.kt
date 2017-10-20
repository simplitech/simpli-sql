package com.simpli.sql

import java.sql.ResultSet
import java.sql.SQLException

/**
 *
 * @author gil
 */

private fun ResultSet.putAlias(alias: String?, columnLabel: String): String {
    return if (alias != null) alias + "." + columnLabel else columnLabel
}

@Throws(SQLException::class)
fun ResultSet.getBooleanOrNull(columnLabel: String): Boolean? {
    val v = this.getBoolean(columnLabel)
    return if (this.wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getBooleanOrNull(columnIndex: Int): Boolean? {
    val v = this.getBoolean(columnIndex)
    return if (this.wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getIntOrNull(columnLabel: String): Int? {
    val v = this.getInt(columnLabel)
    return if (this.wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getIntOrNull(columnIndex: Int): Int? {
    val v = this.getInt(columnIndex)
    return if (this.wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getLongOrNull(columnLabel: String): Long? {
    val v = this.getLong(columnLabel)
    return if (this.wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getLongOrNull(columnIndex: Int): Long? {
    val v = this.getLong(columnIndex)
    return if (this.wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getDoubleOrNull(columnLabel: String): Double? {
    val v = this.getDouble(columnLabel)
    return if (this.wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getDoubleOrNull(columnIndex: Int): Double? {
    val v = this.getDouble(columnIndex)
    return if (this.wasNull()) null else v
}
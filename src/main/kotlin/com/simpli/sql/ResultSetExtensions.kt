package com.simpli.sql

import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp

/**
 *
 * @author gil
 */

private fun ResultSet.putAlias(alias: String?, columnLabel: String): String {
    return if (alias != null) alias + "." + columnLabel else columnLabel
}

@Throws(SQLException::class)
fun ResultSet.getBooleanOrNull(columnLabel: String): Boolean? {
    val v = getBoolean(columnLabel)
    return if (wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getBooleanOrNull(columnIndex: Int): Boolean? {
    val v = getBoolean(columnIndex)
    return if (wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getIntOrNull(columnLabel: String): Int? {
    val v = getInt(columnLabel)
    return if (wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getIntOrNull(columnIndex: Int): Int? {
    val v = getInt(columnIndex)
    return if (wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getLongOrNull(columnLabel: String): Long? {
    val v = getLong(columnLabel)
    return if (wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getLongOrNull(columnIndex: Int): Long? {
    val v = getLong(columnIndex)
    return if (wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getDoubleOrNull(columnLabel: String): Double? {
    val v = getDouble(columnLabel)
    return if (wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getDoubleOrNull(columnIndex: Int): Double? {
    val v = getDouble(columnIndex)
    return if (wasNull()) null else v
}

@Throws(SQLException::class)
fun ResultSet.getString(alias: String, columnLabel: String): String? {
    return getString(putAlias(alias, columnLabel))
}

@Throws(SQLException::class)
fun ResultSet.getTimestamp(alias: String, columnLabel: String): Timestamp? {
    return getTimestamp(putAlias(alias, columnLabel))
}

@Throws(SQLException::class)
fun ResultSet.getBoolean(alias: String, columnLabel: String): Boolean {
    return getBoolean(putAlias(alias, columnLabel))
}

@Throws(SQLException::class)
fun ResultSet.getInt(alias: String, columnLabel: String): Int {
    return getInt(putAlias(alias, columnLabel))
}

@Throws(SQLException::class)
fun ResultSet.getLong(alias: String, columnLabel: String): Long {
    return getLong(putAlias(alias, columnLabel))
}

@Throws(SQLException::class)
fun ResultSet.getDouble(alias: String, columnLabel: String): Double {
    return getDouble(putAlias(alias, columnLabel))
}

@Throws(SQLException::class)
fun ResultSet.getBooleanOrNull(alias: String, columnLabel: String): Boolean? {
    return getBooleanOrNull(putAlias(alias, columnLabel))
}

@Throws(SQLException::class)
fun ResultSet.getIntOrNull(alias: String, columnLabel: String): Int? {
    return getIntOrNull(putAlias(alias, columnLabel))
}

@Throws(SQLException::class)
fun ResultSet.getLongOrNull(alias: String, columnLabel: String): Long? {
    return getLongOrNull(putAlias(alias, columnLabel))
}

@Throws(SQLException::class)
fun ResultSet.getDoubleOrNull(alias: String, columnLabel: String): Double? {
    return getDoubleOrNull(putAlias(alias, columnLabel))
}
package br.com.simpli.sql

import  br.com.simpli.model.EnglishLanguage
import  br.com.simpli.model.RespException
import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Level
import java.util.logging.Logger
import javax.naming.Context
import javax.naming.InitialContext
import javax.sql.DataSource

/**
 *
 * @author gil
 */
open class ConnectionPipe(dsName: String, val autoCommit: Boolean = true) {

    private var ds: DataSource? = null

    init {
        try {
            if (envContext == null) {
                envContext = InitialContext().lookup("java:/comp/env") as Context
            }

            ds = envContext!!.lookup(dsName) as DataSource

        } catch (ex: Exception) {
            Logger.getLogger(ConnectionPipe::class.java.name).log(Level.SEVERE, ex.message, ex)
        }
    }

    fun <T> handle(c: (con: Connection) -> T): T {

        val result: T
        val con: Connection

        try {
            con = ds!!.connection
            con.autoCommit = autoCommit
        } catch (ex: Exception) {
            Logger.getLogger(ConnectionPipe::class.java.name).log(Level.SEVERE, ex.message, ex)
            throw RespException(EnglishLanguage().unexpectedError())
        }

        try {
            result = c(con)
            if (!autoCommit) {
                commit(con) // if autoCommit is true it already commited after the operation
            }
            finish(con)
        } catch (e: Throwable) {
            if (!autoCommit) {
                rollback(con) // if autoCommit is true it already rollback after the operation
            }
            finish(con)
            throw e
        }

        return result
    }

    companion object {

        private var envContext: Context? = null

        fun commit(con: Connection?) {
            if (con != null) {
                try {
                    con.commit()
                } catch (ex: SQLException) {
                    Logger.getLogger(ConnectionPipe::class.java.name).log(Level.SEVERE, ex.message, ex)
                }

            }
        }

        fun rollback(con: Connection?) {
            if (con != null) {
                try {
                    con.rollback()
                } catch (ex: SQLException) {
                    Logger.getLogger(ConnectionPipe::class.java.name).log(Level.SEVERE, ex.message, ex)
                }

            }
        }

        fun finish(con: Connection?) {
            if (con != null) {
                try {
                    con.close()
                } catch (ex: SQLException) {
                    Logger.getLogger(ConnectionPipe::class.java.name).log(Level.SEVERE, ex.message, ex)
                }

            }
        }
    }

}

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
abstract class AbstractConPipe(dsName: String) {

    private var ds: DataSource? = null

    init {
        try {
            if (envContext == null) {
                envContext = InitialContext().lookup("java:/comp/env") as Context
            }

            ds = envContext!!.lookup(dsName) as DataSource

        } catch (ex: Exception) {
            Logger.getLogger(AbstractConPipe::class.java.name).log(Level.SEVERE, ex.message, ex)
        }
    }

    abstract fun <T> handle(c: (connector: AbstractConnector) -> T): T

    protected open fun <T> handleConnection(callback: (con: Connection) -> T): T {

        val result: T
        val con: Connection

        try {
            con = ds!!.connection
        } catch (ex: Exception) {
            Logger.getLogger(AbstractConPipe::class.java.name).log(Level.SEVERE, ex.message, ex)
            throw RespException(EnglishLanguage().unexpectedError())
        }

        try {
            result = callback(con)
            commit(con)
            finish(con)
        } catch (e: Throwable) {
            rollback(con)
            finish(con)
            throw e
        }

        return result
    }

    open fun commit(con: Connection?) {
        if (con != null) {
            try {
                con.commit()
            } catch (ex: SQLException) {
                Logger.getLogger(AbstractConPipe::class.java.name).log(Level.SEVERE, ex.message, ex)
            }

        }
    }

    open fun rollback(con: Connection?) {
        if (con != null) {
            try {
                con.rollback()
            } catch (ex: SQLException) {
                Logger.getLogger(AbstractConPipe::class.java.name).log(Level.SEVERE, ex.message, ex)
            }

        }
    }

    companion object {

        private var envContext: Context? = null

        fun finish(con: Connection?) {
            if (con != null) {
                try {
                    con.close()
                } catch (ex: SQLException) {
                    Logger.getLogger(AbstractConPipe::class.java.name).log(Level.SEVERE, ex.message, ex)
                }

            }
        }
    }

}

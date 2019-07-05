package br.com.simpli.sql

import br.com.simpli.model.RespException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Executes the Queries
 *
 * @author gil
 */
open class TransacConnector(con: Connection) : AbstractConnector(con) {

    override fun execute(query: Query): GenericResult {

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
            Logger.getLogger(TransacConnector::class.java.name).log(Level.INFO, statem.toString())
            closeStatementAndResult(statem, keys)
            val re = RespException(lang.unexpectedError())
            re.initCause(ex)
            throw re
        }

        return genResult

    }

    companion object {
        fun executeForTest(con: Connection, query: Query): GenericResult {
            return TransacConnector(con).execute(query)
        }

        fun existForTest(con: Connection, query: Query): Boolean {
            return TransacConnector(con).exist(query)
        }
    }

}
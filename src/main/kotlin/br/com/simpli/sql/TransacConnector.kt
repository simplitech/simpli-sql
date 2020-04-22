package br.com.simpli.sql

import java.sql.Connection
import java.sql.Statement

/**
 * Executes the Queries
 *
 * @author gil
 */
open class TransacConnector(
        con: Connection,
        deadlockRetries: Int = DEFAULT_DEADLOCK_RETRIES,
        deadlockWait: Long = DEFAULT_DEADLOCK_WAIT
) : AbstractConnector(con, deadlockRetries, deadlockWait) {

    override fun execute(query: Query): GenericResult = handleOperation(query, Statement.RETURN_GENERATED_KEYS) {
        val genResult = GenericResult()
        genResult.affectedRows = it.executeUpdate()
        val keys = it.generatedKeys

        if (keys.next()) {
            genResult.key = keys.getLong(1)
        }

        ResultAndResultSet(genResult, keys)
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

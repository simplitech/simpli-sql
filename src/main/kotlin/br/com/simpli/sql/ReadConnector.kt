package br.com.simpli.sql

import java.sql.Connection

/**
 * Executes the Queries
 *
 * @author gil
 */
open class ReadConnector(
        con: Connection,
        deadlockRetries: Int = DEFAULT_DEADLOCK_RETRIES,
        deadlockWait: Long = DEFAULT_DEADLOCK_WAIT
) : AbstractConnector(con, deadlockRetries, deadlockWait) {

    override fun execute(query: Query): GenericResult {
        throw NotImplementedError("Use TransacConnector to execute operations")
    }

}

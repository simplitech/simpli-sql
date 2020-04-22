package br.com.simpli.sql

import java.sql.Connection

/**
 *
 * @author gil
 */
open class TransacConPipe(
        dsName: String,
        private val deadlockRetries: Int = AbstractConnector.DEFAULT_DEADLOCK_RETRIES,
        private val deadlockWait: Long = AbstractConnector.DEFAULT_DEADLOCK_WAIT
) : AbstractConPipe(dsName) {

    override fun <T> handle(callback: (connector: AbstractConnector) -> T) = handleConnection { con ->
        con.autoCommit = false
        callback(TransacConnector(con, deadlockRetries, deadlockWait))
    }
}

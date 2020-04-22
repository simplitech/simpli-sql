package br.com.simpli.sql

/**
 *
 * @author gil
 */
open class ReadConPipe(
        dsName: String,
        private val deadlockRetries: Int = AbstractConnector.DEFAULT_DEADLOCK_RETRIES,
        private val deadlockWait: Long = AbstractConnector.DEFAULT_DEADLOCK_WAIT
) : AbstractConPipe(dsName) {

    override fun <T> handle(callback: (connector: AbstractConnector) -> T) = handleConnection { con ->
        callback(ReadConnector(con, deadlockRetries, deadlockWait))
    }

}

package br.com.simpli.sql

/**
 *
 * @author gil
 */
open class ReadConPipe(dsName: String) : AbstractConPipe(dsName) {

    override fun <T> handle(callback: (connector: AbstractConnector) -> T) = handleConnection { con ->
        callback(ReadConnector(con))
    }

}
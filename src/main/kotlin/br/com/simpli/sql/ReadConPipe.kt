package br.com.simpli.sql

/**
 *
 * @author gil
 */
open class ReadConPipe(dsName: String) : AbstractConPipe(dsName) {

    override fun <T> handle(c: (connector: AbstractConnector) -> T) = handleConnection { con ->
        c(ReadConnector(con))
    }

}
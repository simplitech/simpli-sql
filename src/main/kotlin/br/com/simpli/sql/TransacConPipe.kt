package br.com.simpli.sql

import java.sql.Connection

/**
 *
 * @author gil
 */
open class TransacConPipe(dsName: String) : AbstractConPipe(dsName) {

    override fun <T> handle(callback: (connector: AbstractConnector) -> T) = handleConnection { con ->
        con.autoCommit = false
        callback(TransacConnector(con))
    }
}
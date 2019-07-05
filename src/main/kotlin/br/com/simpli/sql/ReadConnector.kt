package br.com.simpli.sql

import java.sql.Connection

/**
 * Executes the Queries
 *
 * @author gil
 */
open class ReadConnector(con: Connection) : AbstractConnector(con) {

    override fun execute(query: Query): GenericResult {
        throw NotImplementedError("Use TransacConnector to execute operations")
    }

}
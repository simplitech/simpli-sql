package br.com.simpli.sql

/**
 *
 * @author gil
 */
@Deprecated("Use TransacConPipe")
class TransactionPipe(dsName: String) : ConnectionPipe(dsName, false)
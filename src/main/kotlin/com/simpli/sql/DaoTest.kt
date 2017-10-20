package com.simpli.sql

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource
import org.junit.After
import org.junit.AfterClass
import java.sql.Connection
import java.sql.SQLException
import java.util.logging.Level
import java.util.logging.Logger
import javax.naming.Context
import javax.naming.NamingException

/**
 *
 * @author ricardomeira
 */
open class DaoTest(dsName: String, databaseName: String) {

    fun getConnection(): Connection {
        return currentConnection!!
    }

    init {
        try {
            // sets up the InitialContextFactoryForTest as default factory.
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    InitialContextFactoryForTests::class.java.name)

            val dataSource = MysqlConnectionPoolDataSource()
            dataSource.user = "root"
            dataSource.setPassword("root")
            dataSource.serverName = "localhost"
            dataSource.port = 3306
            dataSource.databaseName = databaseName

            InitialContextFactoryForTests.bind(dsName, dataSource)
            val ctx = InitialContextFactoryForTests().getInitialContext()
            InitialContextFactoryForTests.bind("java:/comp/env", ctx!!)

            dataSource.connection.autoCommit = false
            currentConnection = dataSource.connection

        } catch (ex: NamingException) {
            Logger.getLogger(DaoTest::class.java.name).log(Level.SEVERE, null, ex)
        } catch (ex: SQLException) {
            Logger.getLogger(DaoTest::class.java.name).log(Level.SEVERE, null, ex)
        }

    }

    @After
    @Throws(Exception::class)
    fun afterEach() {
        currentConnection?.rollback()
    }

    companion object {
        private var currentConnection: Connection? = null


        @AfterClass
        @Throws(SQLException::class)
        fun afterAll() {
            if (currentConnection != null) {
                currentConnection?.close()
            }
        }
    }
}

package br.com.simpli.sql

import br.com.simpli.tools.ResourceLoader
import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource
import org.junit.After
import org.junit.AfterClass
import java.security.InvalidParameterException
import java.sql.Connection
import java.sql.SQLException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.naming.Context
import javax.naming.NamingException

/**
 *
 * @author JoaoLippi
 */
open class DaoTest private constructor(
    user: String,
    password: String ,
    host: String,
    port: Int,
    dsName: String?,
    databaseName: String?,
    configPath: String?
) {

    constructor(
        user: String,
        password: String,
        host: String,
        port: Int,
        dsName: String,
        databaseName: String
    ) : this (user, password, host, port, dsName, databaseName, null)

    constructor(
        dsName: String?,
        databaseName: String?
    ) : this ("root", "root", "localhost", 3306, dsName, databaseName, null)

    @JvmOverloads
    constructor(
        configPath: String? = "/test.ds.properties"
    ) : this ("root", "root", "localhost", 3306, null, null, configPath)

    fun getConnection(): Connection {
        return currentConnection!!
    }

    init {
        try {
            val props = configPath?.run { ResourceLoader.getProperties(this) } ?: Properties()

            // sets up the InitialContextFactoryForTest as default factory.
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    InitialContextFactoryForTests::class.java.name)

            currentConnection = MysqlConnectionPoolDataSource().apply {
                this.user = props["user"] as? String ?: user
                this.setPassword(props["password"] as? String ?: password)
                this.serverName = props["host"] as? String ?: host
                this.port = props["port"] as? Int ?: port
                this.databaseName = props["database"] as? String
                    ?: databaseName
                    ?: throw InvalidParameterException("Missing database name")

                InitialContextFactoryForTests.bind(
                    props["datasource"] as? String
                    ?: dsName
                    ?: throw InvalidParameterException("Missing data source name"),
                    this
                )
                InitialContextFactoryForTests().getInitialContext()?.apply {
                    InitialContextFactoryForTests.bind("java:/comp/env", this)
                }
            }.connection?.apply {
                autoCommit = false
            }

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

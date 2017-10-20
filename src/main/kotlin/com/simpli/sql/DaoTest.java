package com.simpli.sql;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import org.junit.After;
import org.junit.AfterClass;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ricardomeira
 */
public class DaoTest {
    protected DataSource dataSource;
    private String dsName;
    private String databaseName;
    private static Connection currentConnection;
    
    
    public DaoTest(String _dsName, String databaseName) {
        try {
            this.dsName = dsName;
            this.databaseName = databaseName;
            
            // sets up the InitialContextFactoryForTest as default factory.
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    InitialContextFactoryForTests.class.getName());
            
            // binds the object
            dataSource = getDataSource();
            InitialContextFactoryForTests.Companion.bind(dsName, dataSource);
            Context ctx = new InitialContextFactoryForTests().getInitialContext(null);
            InitialContextFactoryForTests.Companion.bind("java:/comp/env", ctx);
            currentConnection = dataSource.getConnection();
            currentConnection.setAutoCommit(false);
        } catch (NamingException ex) {
            Logger.getLogger(DaoTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DaoTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @AfterClass
    public static void afterAll() throws SQLException {
        if (currentConnection != null) {
            currentConnection.close();
        }
    }
    
    @After
    public void afterEach() throws Exception{
        if (currentConnection != null) {
            currentConnection.rollback();
        }
    }

    private DataSource getDataSource() {
        MysqlConnectionPoolDataSource localDb = new MysqlConnectionPoolDataSource();
        localDb.setUser("root");
        localDb.setPassword("root");
        localDb.setServerName("localhost");
        localDb.setPort(3306);
        localDb.setDatabaseName(databaseName);
        return localDb;
    }
    
    protected Connection getConnection() {
        return currentConnection;
    }
}

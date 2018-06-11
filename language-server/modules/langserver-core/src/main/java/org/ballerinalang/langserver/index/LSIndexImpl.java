package org.ballerinalang.langserver.index;

import org.h2.tools.Script;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public class LSIndexImpl implements LSIndex {
 
    private LSIndexQueryProcessor queryProcessor = null;
 
    private static final LSIndexImpl instance = new LSIndexImpl();
 
    private Connection connection;
 
    private LSIndexImpl() {}

    public static LSIndexImpl getInstance() {
        return instance;
    }

    /**
     * Init the Lang server Index with the index DB connection.
     * 
     * @param connection    Connection to the index db
     */
    public void init(Connection connection) {
        if (connection == null) {
            // Only at the build time
            initDefaultConnection();
        } else {
            this.queryProcessor = new LSIndexQueryProcessor(connection);
        }
    }

    /**
     * Load the index from a dump index database.
     *
     * @return {@link Boolean}  Whether the index loading is successful or not
     */
    @Override
    public boolean initFromIndexDump(String indexDumpPath) {
        String connectionURL
                = "jdbc:h2:mem:test\\;INIT=RUNSCRIPT FROM '" + indexDumpPath + "'";
        try {
            this.connection = getNewConnection(connectionURL);
            this.setQueryProcessor(this.connection);
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            // TODO: Handle Properly
        }
        
        return false;
    }

    /**
     * Create the connection to the index database.
     */
    private void initDefaultConnection() {
            try {
                this.connection = getNewConnection(Constants.DEFAULT_CONNECTION_URL);
                this.setQueryProcessor(this.connection);
            } catch (ClassNotFoundException | SQLException e) {
                System.out.println("SSSSSSSSSSSSSSS");
                // TODO: Handle Properly
            }
    }

    private static Connection getNewConnection(String connectionURL) throws ClassNotFoundException, SQLException {
        Class.forName(Constants.DRIVER);
        return DriverManager.getConnection(connectionURL);
    }

    public LSIndexQueryProcessor getQueryProcessor() {
        return queryProcessor;
    }

    private void setQueryProcessor(Connection connection) {
        this.queryProcessor = new LSIndexQueryProcessor(connection);
    }

    /**
     * Load the index database schema from the disk.
     *
     * @return {@link Boolean}  Whether the index schema loading is successful or not
     */
    @Override
    public boolean loadIndexSchema() {
        return false;
    }

    /**
     * Re-Index the Language server index.
     *
     * @return {@link Boolean}  Whether the re-indexing process is success or not
     */
    @Override
    public boolean reIndex() {
        return false;
    }

    /**
     * Carryout the Language Server Index.
     *
     * @return {@link Boolean}  Whether the indexing process is success or not
     */
    @Override
    public boolean doIndex() {
        return false;
    }

    /**
     * Save the current in-memory index to a given file location.
     *
     * @param path File path to save the index dump
     * @return {@link Boolean}      Whether the save process is success or not
     */
    @Override
    public boolean saveIndexDump(Path path) {
        try {
            Script.process(connection, path.toString(), "", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

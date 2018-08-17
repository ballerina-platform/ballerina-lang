/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.index;

import org.h2.tools.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Implementation for LSIndex.
 */
public class LSIndexImpl implements LSIndex {

    private static final LSIndexImpl instance = new LSIndexImpl();

    private static final Logger logger = LoggerFactory.getLogger(LSIndexImpl.class);
 
    private LSIndexQueryProcessor queryProcessor = null;
 
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
                = "jdbc:h2:mem:test\\;INIT=RUNSCRIPT FROM '" + indexDumpPath.replace("\\", "\\\\") + "'";
        try {
            this.connection = getNewConnection(connectionURL);
            this.setQueryProcessor(this.connection);
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Error in Creating new Index DB Connection.");
        }
        
        return false;
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
            logger.error("Error in Creating Index DB Dump.");
        }
        return false;
    }

    /**
     * Get the Query Processor Instance.
     * @return {@link LSIndexQueryProcessor}    Index Query Processor Instance
     */
    public LSIndexQueryProcessor getQueryProcessor() {
        return queryProcessor;
    }

    /**
     * Create the connection to the index database.
     */
    private void initDefaultConnection() {
        try {
            this.connection = getNewConnection(Constants.DEFAULT_CONNECTION_URL);
            this.setQueryProcessor(this.connection);
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Error in Creating new Index DB Connection.");
        }
    }

    private static Connection getNewConnection(String connectionURL) throws ClassNotFoundException, SQLException {
        Class.forName(Constants.DRIVER);
        return DriverManager.getConnection(connectionURL);
    }

    private void setQueryProcessor(Connection connection) {
        this.queryProcessor = new LSIndexQueryProcessor(connection);
    }
}

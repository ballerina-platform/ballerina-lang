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

import org.ballerinalang.langserver.index.dao.DAOFactory;
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

    private static final String DRIVER = "org.h2.Driver";

    private static final Logger logger = LoggerFactory.getLogger(LSIndexImpl.class);
 
    private Connection connection;
    
    private DAOFactory daoFactory;

    public LSIndexImpl(String indexPath) {
        String connectionURL = "jdbc:h2:mem:test\\;INIT=RUNSCRIPT FROM '" + indexPath.replace("\\", "\\\\") + "'";
        try {
            Class.forName(DRIVER);
            this.connection = DriverManager.getConnection(connectionURL);
            this.daoFactory = new DAOFactory(this.connection);
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Error in Creating new Index DB Connection.");
        }
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
     * Close the Language Server Index Connection.
     *
     * @return Whether the operation is success or not
     */
    @Override
    public boolean closeConnection() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            logger.error("Failed to close the Index DB connection");
        }
        return true;
    }

    /**
     * Get the DAO Factory instance.
     * 
     * @return {@link DAOFactory}   DAOFactory instance
     */
    public DAOFactory getDaoFactory() {
        return daoFactory;
    }
}

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
package org.ballerinalang.langserver.index.dao;

import org.ballerinalang.langserver.index.LSIndexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract DAO representation.
 * 
 * @since 0.983.0
 */
abstract class AbstractDAO<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDAO.class);
    
    protected Connection connection;

    AbstractDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Insert a single entry to Index DB.
     *
     * @param dto   DTO to insert in to the index DB
     * @return {@link Integer}  generated ID
     * @throws LSIndexException Exception while index access
     */
    public abstract int insert(T dto) throws LSIndexException;

    /**
     * Insert a list of entries in Index DB.
     *
     * @param dtoList           List of entries to be inserted
     * @return {@link List}     List of generated IDs
     * @throws LSIndexException Exception while index access
     */
    public abstract List<Integer> insertBatch(java.util.List<T> dtoList) throws LSIndexException;

    /**
     * Get all the entries in the corresponding table.
     *
     * @return {@link List}     List of retrieved entries
     * @throws LSIndexException Exception while index access
     */
    public abstract List<T> getAll() throws LSIndexException;

    /**
     * Get a single entry from id.
     * 
     * @param id            Entry ID to retrieve
     * @return {@link T}    Retrieved entry
     * @throws LSIndexException Exception while retrieving entry from index
     */
    public abstract T get(int id) throws LSIndexException;

    /**
     * Get a single entry from dto.
     * 
     * @param dto           Entry ID to retrieve
     * @return {@link T}    Retrieved entry
     * @throws LSIndexException Exception while retrieving entry from index
     */
    public abstract List<T> get(T dto) throws LSIndexException;

    /**
     * Release the resources.
     * 
     * @param resultSet     Result Set to close
     * @param statement     statement to close
     */
    public void releaseResources(ResultSet resultSet, Statement statement) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.error("Error while closing the result set");
            }
        }
        
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.error("Error while closing the prepared statement");
            }
        }
    }

    /**
     * Get the list of generated keys(ids) from the result set.
     *
     * @param resultSet         Result Set to get the IDs
     * @return {@link List}     List of generated IDs
     */
    protected List<Integer> getGeneratedKeys(ResultSet resultSet) {
        List<Integer> generatedKeys = new ArrayList<>();
        try {
            while (resultSet.next()) {
                generatedKeys.add(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error getting the Generated Keys: [" + e.getMessage() + "]");
        }
        return generatedKeys;
    }
}

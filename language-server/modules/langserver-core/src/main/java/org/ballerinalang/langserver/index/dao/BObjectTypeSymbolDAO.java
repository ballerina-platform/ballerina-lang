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

import org.ballerinalang.langserver.common.utils.index.DTOUtil;
import org.ballerinalang.langserver.index.LSIndexException;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * DAO for BObjectTypeSymbol.
 * 
 * @since 0.983.0
 */
public class BObjectTypeSymbolDAO extends AbstractDAO<BObjectTypeSymbolDTO> {
    
    public BObjectTypeSymbolDAO(Connection connection) {
        super(connection);
    }

    /**
     * Insert a single entry to Index DB.
     *
     * @param dto DTO to insert in to the index DB
     */
    @Override
    public void insert(BObjectTypeSymbolDTO dto) {
        
    }

    /**
     * Insert a list of entries in Index DB.
     *
     * @param dtoList List of entries to be inserted
     * @return {@link List}     List of generated IDs
     */
    @Override
    public List<Integer> insertBatch(List<BObjectTypeSymbolDTO> dtoList) throws LSIndexException {
        String query = "INSERT INTO bLangObject (packageId, name, fields, type, private, completionItem)" +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (BObjectTypeSymbolDTO dto : dtoList) {
                statement.setInt(1, dto.getPackageId());
                statement.setString(2, dto.getName());
                statement.setString(3, dto.getFields());
                statement.setInt(4, dto.getType().getValue());
                statement.setBoolean(5, dto.isPrivate());
                statement.setString(6, DTOUtil.completionItemToJSON(dto.getCompletionItem()));
                statement.addBatch();
            }
            statement.executeBatch();
            
            return this.getGeneratedKeys(statement.getResultSet());
        } catch (SQLException e) {
            throw new LSIndexException("Error while inserting BLang Object in to Index");
        }
    }

    /**
     * Get all the entries in the corresponding table.
     *
     * @return {@link List}     List of retrieved entries
     */
    @Override
    public List<BObjectTypeSymbolDTO> getAll() {
        return null;
    }

    /**
     * Get a single entry from id.
     *
     * @param id Entry ID to retrieve
     * @return {@link BObjectTypeSymbolDTO}    Retrieved entry
     */
    @Override
    public BObjectTypeSymbolDTO get(int id) {
        return null;
    }

    /**
     * Do a batch update on action holder IDs.
     * 
     * @param endpoints             List of endpoint ids
     * @param actionHolders         List of action holder ids
     * @return {@link List}         List of generated Keys
     * @throws LSIndexException     Exception while updating the entries
     */
    public List<Integer> updateActionHolderIDs(List<Integer> endpoints, List<Integer> actionHolders)
            throws LSIndexException {
        String query = "UPDATE bLangObject SET actionHolderId = ? WHERE id = ?";
        try {
            PreparedStatement statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < endpoints.size(); i++) {
                statement.setInt(1, actionHolders.get(i));
                statement.setInt(2, endpoints.get(i));
                statement.addBatch();
            }
            statement.executeBatch();
            
            return this.getGeneratedKeys(statement.getResultSet());
        } catch (SQLException e) {
            throw new LSIndexException("Error while updating endpoint action holder IDs");
        }
    }
}

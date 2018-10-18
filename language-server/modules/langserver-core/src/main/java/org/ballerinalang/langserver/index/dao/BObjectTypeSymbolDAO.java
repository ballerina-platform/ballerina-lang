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

import org.ballerinalang.langserver.index.DTOUtil;
import org.ballerinalang.langserver.index.LSIndexException;
import org.ballerinalang.langserver.index.ObjectType;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for BObjectTypeSymbol.
 * 
 * @since 0.983.0
 */
public class BObjectTypeSymbolDAO extends AbstractDAO<BObjectTypeSymbolDTO> {
    
    BObjectTypeSymbolDAO(Connection connection) {
        super(connection);
    }

    /**
     * Insert a single entry to Index DB.
     *
     * @param dto DTO to insert in to the index DB
     */
    @Override
    public int insert(BObjectTypeSymbolDTO dto) {
        return -1;
    }

    /**
     * Insert a list of entries in Index DB.
     *
     * @param dtoList List of entries to be inserted
     * @return {@link List}     List of generated IDs
     * @throws LSIndexException Exception while index access
     */
    @Override
    public List<Integer> insertBatch(List<BObjectTypeSymbolDTO> dtoList) throws LSIndexException {
        String query = "INSERT INTO bLangObject (packageId, name, fields, type, private, completionItem)" +
                "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
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
            rs = statement.getGeneratedKeys();
            return this.getGeneratedKeys(rs);
        } catch (SQLException e) {
            throw new LSIndexException("Error while inserting BLang Object in to Index");
        } finally {
            this.releaseResources(rs, statement);
        }
    }

    /**
     * Get all the entries in the corresponding table.
     *
     * @return {@link List}     List of retrieved entries
     * @throws LSIndexException Exception while index access
     */
    @Override
    public List<BObjectTypeSymbolDTO> getAll() throws LSIndexException {
        return null;
    }

    /**
     * Get a single entry from id.
     *
     * @param id Entry ID to retrieve
     * @return {@link BObjectTypeSymbolDTO}    Retrieved entry
     * @throws LSIndexException Exception while retrieving entry from index
     */
    @Override
    public BObjectTypeSymbolDTO get(int id) throws LSIndexException {
        return null;
    }

    @Override
    public List<BObjectTypeSymbolDTO> get(BObjectTypeSymbolDTO dto) throws LSIndexException {
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
        PreparedStatement statement = null;
        ResultSet rs = null;
        String query = "UPDATE bLangObject SET actionHolderId = ? WHERE id = ?";
        try {
            statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < endpoints.size(); i++) {
                statement.setInt(1, actionHolders.get(i));
                statement.setInt(2, endpoints.get(i));
                statement.addBatch();
            }
            statement.executeBatch();
            rs = statement.getGeneratedKeys();
            return this.getGeneratedKeys(rs);
        } catch (SQLException e) {
            throw new LSIndexException("Error while updating endpoint action holder IDs");
        } finally {
            this.releaseResources(rs, statement);
        }
    }

    /**
     * Get all the endpoint objects.
     *
     * @return  {@link List}    List of retrieved endpoint objects
     * @throws LSIndexException Exception while index access
     */
    public List<BObjectTypeSymbolDTO> getAllEndpoints() throws LSIndexException {
        String query = "SELECT id, packageId, name, type, actionHolderId, private, completionItem FROM bLangObject " +
                "WHERE type = 1";
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            List<BObjectTypeSymbolDTO> epDTOs = new ArrayList<>();
            statement = this.connection.prepareStatement(query);
            rs = statement.executeQuery();
            while (rs.next()) {
                BObjectTypeSymbolDTO epDto = new BObjectTypeSymbolDTO.BObjectTypeSymbolDTOBuilder()
                        .setId(rs.getInt(1))
                        .setPackageId(rs.getInt(2))
                        .setName(rs.getString(3))
                        .setType(ObjectType.get(rs.getInt(4)))
                        .setActionHolderId(rs.getInt(5))
                        .setPrivate(rs.getBoolean(6))
                        .setCompletionItem(DTOUtil.jsonToCompletionItem(rs.getString(7)))
                        .build();
                epDTOs.add(epDto);
            }
            
            return epDTOs;
        } catch (SQLException e) {
            throw new LSIndexException("Error retrieving endpoints from index");
        } finally {
            this.releaseResources(rs, statement);
        }
    }
}

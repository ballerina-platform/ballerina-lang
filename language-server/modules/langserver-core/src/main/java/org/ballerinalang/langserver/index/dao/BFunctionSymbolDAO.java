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
import org.ballerinalang.langserver.index.dto.BFunctionSymbolDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for BFunctionSymbol.
 * 
 * @since 0.983.0
 */
public class BFunctionSymbolDAO extends AbstractDAO<BFunctionSymbolDTO> {
    
    BFunctionSymbolDAO(Connection connection) {
        super(connection);
    }

    /**
     * Insert a single entry to Index DB.
     *
     * @param dto DTO to insert in to the index DB
     */
    @Override
    public int insert(BFunctionSymbolDTO dto) {
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
    public List<Integer> insertBatch(List<BFunctionSymbolDTO> dtoList) throws LSIndexException {
        String query = "INSERT INTO bLangFunction (packageId, objectId, name, completionItem, private, attached) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (BFunctionSymbolDTO dto : dtoList) {
                statement.setInt(1, dto.getPackageId());
                statement.setInt(2, dto.getObjectId());
                statement.setString(3, dto.getName());
                statement.setString(4, DTOUtil.completionItemToJSON(dto.getCompletionItem()));
                statement.setBoolean(5, dto.isPrivate());
                statement.setBoolean(6, dto.isAttached());
                statement.addBatch();
            }
            statement.executeBatch();
            rs = statement.getGeneratedKeys();
            return this.getGeneratedKeys(rs);
        } catch (SQLException e) {
            throw new LSIndexException("Error while inserting BLang Function in to Index");
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
    public List<BFunctionSymbolDTO> getAll() throws LSIndexException {
        return null;
    }

    /**
     * Get a single entry from id.
     *
     * @param id                                Entry ID to retrieve
     * @return {@link BFunctionSymbolDTO}       Retrieved entry
     * @throws LSIndexException Exception while retrieving entry from index
     */
    @Override
    public BFunctionSymbolDTO get(int id) throws LSIndexException {
        return null;
    }

    @Override
    public List<BFunctionSymbolDTO> get(BFunctionSymbolDTO dto) throws LSIndexException {
        return null;
    }

    /**
     * Get all the endpoint actions.
     *
     * @return {@link List}         List of retrieved actions
     * @throws LSIndexException     Exception while accessing Index
     */
    public List<BFunctionSymbolDTO> getAllActions() throws LSIndexException {
        List<BFunctionSymbolDTO> funcDTOs = new ArrayList<>();
        String query = "SELECT p.ID, o.ID, f.NAME FROM BLANGOBJECT as o JOIN BLANGPACKAGE as p ON p.ID = o.PACKAGEID " +
                "JOIN BLANGFUNCTION as f ON f.OBJECTID   = o.ACTIONHOLDERID  WHERE o.TYPE  = 1 AND p.NAME LIKE ? AND " +
                "o.NAME LIKE ?";
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = this.connection.prepareStatement(query);
            rs = statement.executeQuery();
            BFunctionSymbolDTO funcDTO = new BFunctionSymbolDTO.BFunctionDTOBuilder()
                    .setPackageId(rs.getInt(1))
                    .setObjectId(rs.getInt(2))
                    .setName(rs.getString(3))
                    .build();
            funcDTOs.add(funcDTO);
            
            return funcDTOs;
        } catch (SQLException e) {
            throw new LSIndexException("Error retrieving actions from index");
        } finally {
            this.releaseResources(rs, statement);
        }
    }
}

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
import org.ballerinalang.langserver.index.dto.OtherTypeSymbolDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * DAO for Other Type Symbols.
 * 
 * @since 0.983.0
 */
public class BOtherTypeSymbolDAO extends AbstractDAO<OtherTypeSymbolDTO> {
    
    BOtherTypeSymbolDAO(Connection connection) {
        super(connection);
    }

    /**
     * Insert a single entry to Index DB.
     *
     * @param dto DTO to insert in to the index DB
     */
    @Override
    public int insert(OtherTypeSymbolDTO dto) {
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
    public List<Integer> insertBatch(List<OtherTypeSymbolDTO> dtoList) throws LSIndexException {
        String query = "INSERT INTO bLangType (packageId, name, fields, completionItem) values (?, ?, ?, ?)";
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (OtherTypeSymbolDTO dto : dtoList) {
                statement.setInt(1, dto.getPackageId());
                statement.setString(2, dto.getName());
                statement.setString(3, dto.getFields());
                statement.setString(4, DTOUtil.completionItemToJSON(dto.getCompletionItem()));
                statement.addBatch();
            }
            statement.executeBatch();
            rs = statement.getGeneratedKeys();
            
            return this.getGeneratedKeys(rs);
        } catch (SQLException e) {
            throw new LSIndexException("Error while inserting Other BType in to Index");
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
    public List<OtherTypeSymbolDTO> getAll() throws LSIndexException {
        return null;
    }

    /**
     * Get a single entry from id.
     *
     * @param id Entry ID to retrieve
     * @return {@link OtherTypeSymbolDTO}    Retrieved entry
     * @throws LSIndexException Exception while retrieving entry from index
     */
    @Override
    public OtherTypeSymbolDTO get(int id) throws LSIndexException {
        return null;
    }

    @Override
    public List<OtherTypeSymbolDTO> get(OtherTypeSymbolDTO dto) throws LSIndexException {
        return null;
    }
}

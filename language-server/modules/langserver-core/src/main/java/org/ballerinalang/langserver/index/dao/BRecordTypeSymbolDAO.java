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
import org.ballerinalang.langserver.index.dto.BRecordTypeSymbolDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * DAO for BRecordTypeSymbol.
 * 
 * @since 0.983.0
 */
public class BRecordTypeSymbolDAO extends AbstractDAO<BRecordTypeSymbolDTO> {
    
    BRecordTypeSymbolDAO(Connection connection) {
        super(connection);
    }

    /**
     * Insert a single entry to Index DB.
     *
     * @param dto DTO to insert in to the index DB
     */
    @Override
    public int insert(BRecordTypeSymbolDTO dto) {
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
    public List<Integer> insertBatch(List<BRecordTypeSymbolDTO> dtoList) throws LSIndexException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        String query = "INSERT INTO bLangRecord (packageId, name, fields, private, completionItem) VALUES " +
                "(?, ?, ?, ?, ?)";

        try {
            statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            for (BRecordTypeSymbolDTO dto : dtoList) {
                statement.setInt(1, dto.getPackageId());
                statement.setString(2, dto.getName());
                statement.setString(3, dto.getFields());
                statement.setBoolean(4, dto.isPrivate());
                statement.setString(5, DTOUtil.completionItemToJSON(dto.getCompletionItem()));
                statement.addBatch();
            }
            statement.executeBatch();
            rs = statement.getGeneratedKeys();
            return this.getGeneratedKeys(rs);
        } catch (SQLException e) {
            throw new LSIndexException("Error while inserting BLang Record in to Index");
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
    public List<BRecordTypeSymbolDTO> getAll() throws LSIndexException {
        return null;
    }

    /**
     * Get a single entry from id.
     *
     * @param id Entry ID to retrieve
     * @return {@link BRecordTypeSymbolDTO}    Retrieved entry
     * @throws LSIndexException Exception while retrieving entry from index
     */
    @Override
    public BRecordTypeSymbolDTO get(int id) throws LSIndexException {
        return null;
    }

    @Override
    public List<BRecordTypeSymbolDTO> get(BRecordTypeSymbolDTO dto) throws LSIndexException {
        return null;
    }
}

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
import org.ballerinalang.langserver.index.dto.BFunctionSymbolDTO;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.BRecordTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.OtherTypeSymbolDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for BPackageSymbol
 * 
 * @since 0.983.0
 */
public class BPackageSymbolDAO extends AbstractDAO<BPackageSymbolDTO> {
    
    public BPackageSymbolDAO(Connection connection) {
        super(connection);
    }

    /**
     * Insert a single entry to Index DB.
     *
     * @param dto DTO to insert in to the index DB
     */
    @Override
    public void insert(BPackageSymbolDTO dto) {
        
    }

    /**
     * Insert a list of entries in Index DB.
     *
     * @param dtoList List of entries to be inserted
     * @return {@link List}     List of generated IDs
     */
    @Override
    public List<Integer> insertBatch(List<BPackageSymbolDTO> dtoList) {
        return null;
    }

    /**
     * Get all the entries in the corresponding table.
     *
     * @return {@link List}     List of retrieved entries
     */
    @Override
    public List<BPackageSymbolDTO> getAll() {
        return null;
    }

    /**
     * Get a single entry from id.
     *
     * @param id Entry ID to retrieve
     * @return {@link BPackageSymbolDTO}    Retrieved entry
     */
    @Override
    public BPackageSymbolDTO get(int id) {
        return null;
    }

    /**
     * Get list of functions for the given Package criteria.
     * 
     * @param dto   Package Symbol DTO
     * @return {@link List}         List of retrieved functions
     * @param isPrivate Whether private or public items
     * @throws LSIndexException     Exception while processing Index operation.
     */
    public List<BFunctionSymbolDTO> getFunctions(BPackageSymbolDTO dto, boolean isPrivate) throws LSIndexException {
        StringBuilder query = new StringBuilder("SELECT p.id, p.name, p.orgName, f.completionItem, f.name, " +
                "f.private, f.attached FROM (SELECT id, name, orgName FROM bLangPackage WHERE name = ?");
        if (dto.getOrgName().isEmpty()) {
            query.append(") ");
        } else {
            query.append(" AND orgName = ?) ");
        }
        query.append("AS p INNER JOIN bLangFunction AS f WHERE p.id=f.packageId AND f.objectId=-1 AND f.private = ? " +
                "AND f.name NOT LIKE '%<init>%' AND " + "f.name NOT LIKE '%<start>%' AND f.name NOT LIKE '%<stop>%'");

        try {
            PreparedStatement statement = this.connection.prepareStatement(query.toString());
            statement.setString(1, dto.getName());
            if (!dto.getOrgName().isEmpty()) {
                statement.setString(2, dto.getOrgName());
            }
            statement.setBoolean(3, isPrivate);

            ResultSet rs = statement.executeQuery();
            List<BFunctionSymbolDTO> functionSymbolDTOList = new ArrayList<>();
            while (rs.next()) {
                BFunctionSymbolDTO functionDto = new BFunctionSymbolDTO.BFunctionDTOBuilder()
                        .setPackageId(rs.getInt(1))
                        .setAttached(rs.getBoolean(7))
                        .setPrivate(rs.getBoolean(6))
                        .setCompletionItem(DTOUtil.JsonToCompletionItem(rs.getString(4)))
                        .setName(rs.getString(5))
                        .build();
                functionSymbolDTOList.add(functionDto);
            }
            
            return functionSymbolDTOList;
        } catch (SQLException e) {
            throw new LSIndexException("Error while retrieving Function information for package from Index");
        }
    }

    /**
     * Get list of records for the given Package criteria.
     *
     * @param dto   Package Symbol DTO
     * @return {@link List}         List of retrieved records
     * @param isPrivate Whether private or public items
     * @throws LSIndexException     Exception while processing Index operation.
     */
    public List<BRecordTypeSymbolDTO> getRecords(BPackageSymbolDTO dto, boolean isPrivate) throws LSIndexException {
        StringBuilder query = new StringBuilder("SELECT p.id, p.name, p.orgName, r.completionItem, r.name, r.private " +
                "FROM (SELECT id, name, orgName FROM bLangPackage WHERE name = ?");
        if (dto.getOrgName().isEmpty()) {
            query.append(") ");
        } else {
            query.append(" AND orgName = ?) ");
        }
        query.append("AS p INNER JOIN bLangRecord AS r WHERE p.id = r.packageId AND r.private = ?");

        try {
            PreparedStatement statement = this.connection.prepareStatement(query.toString());
            statement.setString(1, dto.getName());
            if (!dto.getOrgName().isEmpty()) {
                statement.setString(2, dto.getOrgName());
            }
            statement.setBoolean(3, isPrivate);

            ResultSet rs = statement.executeQuery();
            List<BRecordTypeSymbolDTO> recordTypeSymbolDTOs = new ArrayList<>();
            while (rs.next()) {
                BRecordTypeSymbolDTO recordDTO = new BRecordTypeSymbolDTO.BRecordTypeSymbolDTOBuilder()
                        .setPackageId(rs.getInt(1))
                        .setCompletionItem(DTOUtil.JsonToCompletionItem(rs.getString(4)))
                        .setName(rs.getString(5))
                        .setPrivate(rs.getBoolean(6))
                        .build();
                recordTypeSymbolDTOs.add(recordDTO);
            }

            return recordTypeSymbolDTOs;
        } catch (SQLException e) {
            throw new LSIndexException("Error while retrieving Record information for package from Index");
        }
    }

    /**
     * Get list of other types for the given Package criteria.
     *
     * @param dto   Package Symbol DTO
     * @return {@link List}         List of retrieved other types
     * @throws LSIndexException     Exception while processing Index operation.
     */
    public List<OtherTypeSymbolDTO> getOtherTypes(BPackageSymbolDTO dto) throws LSIndexException {
        StringBuilder query = new StringBuilder("SELECT p.id, p.name, p.orgName, t.completionItem, t.name " +
                "FROM (SELECT id, name, orgName FROM bLangPackage WHERE name = ?");
        if (dto.getOrgName().isEmpty()) {
            query.append(") "); 
        } else {
            query.append(" AND orgName = ?) ");
        }
        query.append("AS p INNER JOIN bLangType AS t WHERE p.id = t.packageId");

        try {
            PreparedStatement statement = this.connection.prepareStatement(query.toString());
            statement.setString(1, dto.getName());
            if (!dto.getOrgName().isEmpty()) {
                statement.setString(2, dto.getOrgName());
            }
            ResultSet rs = statement.executeQuery();
            List<OtherTypeSymbolDTO> otherTypeSymbolDTOs = new ArrayList<>();
            while (rs.next()) {
                OtherTypeSymbolDTO otherTypeSymbolDTO = new OtherTypeSymbolDTO.OtherTypeSymbolDTOBuilder()
                        .setPackageId(rs.getInt(1))
                        .setCompletionItem(DTOUtil.JsonToCompletionItem(rs.getString(4)))
                        .setName(rs.getString(5))
                        .build();
                otherTypeSymbolDTOs.add(otherTypeSymbolDTO);
            }

            return otherTypeSymbolDTOs;
        } catch (SQLException e) {
            throw new LSIndexException("Error while retrieving Other Type information for package from Index");
        }
    }

    /**
     * Get list of object types for the given Package criteria.
     *
     * @param dto   Package Symbol DTO
     * @return {@link List}         List of retrieved object types
     * @throws LSIndexException     Exception while processing Index operation.
     */
    public List<BObjectTypeSymbolDTO> getObjects(BPackageSymbolDTO dto, boolean isPrivate) throws LSIndexException {
        StringBuilder query = new StringBuilder("SELECT p.id, p.name, p.orgName, o.completionItem, o.name, " +
                "o.private FROM (select id, name, orgName FROM bLangPackage WHERE name = ?");
        if (dto.getOrgName().isEmpty()) {
            query.append(") ");
        } else {
            query.append(" AND orgName = ?) ");
        }
        query.append("AS p INNER JOIN bLangObject AS o WHERE p.id = o.packageId AND o.type = 3 AND o.private = ?");
        try {
            PreparedStatement statement = this.connection.prepareStatement(query.toString());
            statement.setString(1, dto.getName());
            if (!dto.getOrgName().isEmpty()) {
                statement.setString(2, dto.getOrgName());
            }
            statement.setBoolean(3, isPrivate);
            
            ResultSet rs = statement.executeQuery();
            List<BObjectTypeSymbolDTO> objectTypeSymbolDTOs = new ArrayList<>();
            while (rs.next()) {
                BObjectTypeSymbolDTO objectTypeSymbolDTO = new BObjectTypeSymbolDTO.BObjectTypeSymbolDTOBuilder()
                        .setPackageId(rs.getInt(1))
                        .setCompletionItem(DTOUtil.JsonToCompletionItem(rs.getString(4)))
                        .setName(rs.getString(5))
                        .build();
                objectTypeSymbolDTOs.add(objectTypeSymbolDTO);
            }

            return objectTypeSymbolDTOs;
        } catch (SQLException e) {
            throw new LSIndexException("Error while retrieving Object Type information for package from Index");
        }
    }
}

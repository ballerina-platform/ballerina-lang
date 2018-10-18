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
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.BRecordTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.OtherTypeSymbolDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for BPackageSymbol.
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
    public int insert(BPackageSymbolDTO dto) throws LSIndexException {
        String query = "INSERT INTO bLangPackage (name, orgName, version) VALUES (?, ?, ?)";
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, dto.getName());
            statement.setString(2, dto.getOrgName());
            statement.setString(3, dto.getVersion());
            statement.executeUpdate();
            rs = statement.getGeneratedKeys();
            List<Integer> keys = this.getGeneratedKeys(rs);
            return keys.get(0);
        } catch (SQLException e) {
            throw new LSIndexException("Error while inserting BLang Package");
        } finally {
            this.releaseResources(rs, statement);
        }
    }

    /**
     * Insert a list of entries in Index DB.
     *
     * @param dtoList List of entries to be inserted
     * @return {@link List}     List of generated IDs
     * @throws LSIndexException Exception while index access
     */
    @Override
    public List<Integer> insertBatch(List<BPackageSymbolDTO> dtoList) throws LSIndexException {
        return null;
    }

    /**
     * Get all the entries in the corresponding table.
     *
     * @return {@link List}     List of retrieved entries
     * @throws LSIndexException Exception while index access
     */
    @Override
    public List<BPackageSymbolDTO> getAll() throws LSIndexException {
        List<BPackageSymbolDTO> pkgDTOs = new ArrayList<>();
        String query = "SELECT id, name, orgName, version FROM bLangPackage";
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = this.connection.prepareStatement(query);
            rs = statement.executeQuery();
            while (rs.next()) {
                BPackageSymbolDTO dto = new BPackageSymbolDTO.BPackageSymbolDTOBuilder()
                        .setId(rs.getInt(1))
                        .setName(rs.getString(2))
                        .setOrgName(rs.getString(3))
                        .setVersion(rs.getString(4))
                        .build();
                pkgDTOs.add(dto);
            }
            
            return pkgDTOs;
        } catch (SQLException e) {
            throw new LSIndexException("Error while retrieving Packages from Index");
        } finally {
            this.releaseResources(rs, statement);
        }
    }

    /**
     * Get a single entry from id.
     *
     * @param id Entry ID to retrieve
     * @return {@link BPackageSymbolDTO}    Retrieved entry
     * @throws LSIndexException Exception while retrieving entry from index
     */
    @Override
    public BPackageSymbolDTO get(int id) throws LSIndexException {
        String query = "SELECT id, name, orgName, version FROM bLangPackage WHERE id = ?";
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            statement = this.connection.prepareStatement(query);
            statement.setInt(1, id);
            rs = statement.executeQuery();
            if (rs.next()) {
                return new BPackageSymbolDTO.BPackageSymbolDTOBuilder()
                        .setId(rs.getInt(1))
                        .setName(rs.getString(2))
                        .setOrgName(rs.getString(3))
                        .setVersion(rs.getString(4))
                        .build();
            } else {
                throw new LSIndexException("Invalid id provided to retrieve BLang Package Symbol");
            }
        } catch (SQLException e) {
            throw new LSIndexException("Error while retrieving package entry from Index");
        } finally {
            this.releaseResources(rs, statement);
        }
    }

    @Override
    public List<BPackageSymbolDTO> get(BPackageSymbolDTO dto) throws LSIndexException {
        StringBuilder query = new StringBuilder("SELECT id, name, orgName, version FROM bLangPackage WHERE name = ?");
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<BPackageSymbolDTO> resultList = new ArrayList<>();
        if (dto.getOrgName() != null && !dto.getOrgName().isEmpty()) {
            query.append(" AND orgName = ?");
        }

        try {
            statement = this.connection.prepareStatement(query.toString());
            statement.setString(1, dto.getName());
            if (dto.getOrgName() != null && !dto.getOrgName().isEmpty()) {
                statement.setString(2, dto.getOrgName());
            }
            rs = statement.executeQuery();
            while (rs.next()) {
                BPackageSymbolDTO resultDto = new BPackageSymbolDTO.BPackageSymbolDTOBuilder()
                        .setId(rs.getInt(1))
                        .setName(rs.getString(2))
                        .setOrgName(rs.getString(3))
                        .setVersion(rs.getString(4))
                        .build();
                resultList.add(resultDto);
            }
            return resultList;
        } catch (SQLException e) {
            throw new LSIndexException("Error retrieving package from Index");
        } finally {
            this.releaseResources(rs, statement);
        }
    }

    /**
     * Get list of functions for the given Package criteria.
     * 
     * @param dto                   Package Symbol DTO
     * @param objectId              Object ID which the function attached to, -1 if not
     * @param pvt                   Whether private or public items
     * @param attached              Whether attached function or not
     * @return {@link List}         List of retrieved functions
     * @throws LSIndexException     Exception while processing Index operation.
     */
    public List<BFunctionSymbolDTO> getFunctions(BPackageSymbolDTO dto, int objectId, boolean pvt, boolean attached)
            throws LSIndexException {
        int indexCounter = 0;
        ResultSet rs = null;
        PreparedStatement statement = null;
        StringBuilder query = new StringBuilder("SELECT p.id, p.name, p.orgName, f.completionItem, f.name, " +
                "f.private, f.attached FROM (SELECT id, name, orgName FROM bLangPackage WHERE name = ?");
        if (dto.getOrgName().isEmpty()) {
            query.append(") ");
        } else {
            query.append(" AND orgName = ?) ");
        }
        query.append("AS p INNER JOIN bLangFunction AS f WHERE p.id=f.packageId AND f.objectId = ? AND f.private = ? " +
                " AND f.attached = ? AND f.name NOT LIKE '%<init>%' AND " + "f.name NOT LIKE '%<start>%' AND f.name " +
                "NOT LIKE '%<stop>%'");

        try {
            statement = this.connection.prepareStatement(query.toString());
            statement.setString(++indexCounter, dto.getName());
            if (!dto.getOrgName().isEmpty()) {
                statement.setString(++indexCounter, dto.getOrgName());
            }
            statement.setInt(++indexCounter, objectId);
            statement.setBoolean(++indexCounter, pvt);
            statement.setBoolean(++indexCounter, attached);

            rs = statement.executeQuery();
            List<BFunctionSymbolDTO> functionSymbolDTOList = new ArrayList<>();
            while (rs.next()) {
                BFunctionSymbolDTO functionDto = new BFunctionSymbolDTO.BFunctionDTOBuilder()
                        .setPackageId(rs.getInt(1))
                        .setAttached(rs.getBoolean(7))
                        .setPrivate(rs.getBoolean(6))
                        .setCompletionItem(DTOUtil.jsonToCompletionItem(rs.getString(4)))
                        .setName(rs.getString(5))
                        .build();
                functionSymbolDTOList.add(functionDto);
            }
            
            return functionSymbolDTOList;
        } catch (SQLException e) {
            throw new LSIndexException("Error while retrieving Function information for package from Index");
        } finally {
            this.releaseResources(rs, statement);
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
        ResultSet rs = null;
        PreparedStatement statement = null;
        int parameterIndex = 0;
        StringBuilder query = new StringBuilder("SELECT p.id, p.name, p.orgName, r.completionItem, r.name, r.private " +
                "FROM (SELECT id, name, orgName FROM bLangPackage WHERE name = ?");
        if (dto.getOrgName().isEmpty()) {
            query.append(") ");
        } else {
            query.append(" AND orgName = ?) ");
        }
        query.append("AS p INNER JOIN bLangRecord AS r WHERE p.id = r.packageId AND r.private = ?");

        try {
            statement = this.connection.prepareStatement(query.toString());
            statement.setString(++parameterIndex, dto.getName());
            if (!dto.getOrgName().isEmpty()) {
                statement.setString(++parameterIndex, dto.getOrgName());
            }
            statement.setBoolean(++parameterIndex, isPrivate);

            rs = statement.executeQuery();
            List<BRecordTypeSymbolDTO> recordTypeSymbolDTOs = new ArrayList<>();
            while (rs.next()) {
                BRecordTypeSymbolDTO recordDTO = new BRecordTypeSymbolDTO.BRecordTypeSymbolDTOBuilder()
                        .setPackageId(rs.getInt(1))
                        .setCompletionItem(DTOUtil.jsonToCompletionItem(rs.getString(4)))
                        .setName(rs.getString(5))
                        .setPrivate(rs.getBoolean(6))
                        .build();
                recordTypeSymbolDTOs.add(recordDTO);
            }

            return recordTypeSymbolDTOs;
        } catch (SQLException e) {
            throw new LSIndexException("Error while retrieving Record information for package from Index");
        } finally {
            this.releaseResources(rs, statement);
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
        PreparedStatement statement = null;
        ResultSet rs = null;
        int parameterIndex = 0;
        StringBuilder query = new StringBuilder("SELECT p.id, p.name, p.orgName, t.completionItem, t.name " +
                "FROM (SELECT id, name, orgName FROM bLangPackage WHERE name = ?");
        if (dto.getOrgName().isEmpty()) {
            query.append(") "); 
        } else {
            query.append(" AND orgName = ?) ");
        }
        query.append("AS p INNER JOIN bLangType AS t WHERE p.id = t.packageId");

        try {
            statement = this.connection.prepareStatement(query.toString());
            statement.setString(++parameterIndex, dto.getName());
            if (!dto.getOrgName().isEmpty()) {
                statement.setString(++parameterIndex, dto.getOrgName());
            }
            rs = statement.executeQuery();
            List<OtherTypeSymbolDTO> otherTypeSymbolDTOs = new ArrayList<>();
            while (rs.next()) {
                OtherTypeSymbolDTO otherTypeSymbolDTO = new OtherTypeSymbolDTO.OtherTypeSymbolDTOBuilder()
                        .setPackageId(rs.getInt(1))
                        .setCompletionItem(DTOUtil.jsonToCompletionItem(rs.getString(4)))
                        .setName(rs.getString(5))
                        .build();
                otherTypeSymbolDTOs.add(otherTypeSymbolDTO);
            }

            return otherTypeSymbolDTOs;
        } catch (SQLException e) {
            throw new LSIndexException("Error while retrieving Other Type information for package from Index");
        } finally {
            this.releaseResources(rs, statement);
        }
    }

    /**
     * Get list of object types for the given Package criteria.
     *
     * @param dto                   Package Symbol DTO
     * @param isPrivate             Whether private objects or not
     * @return {@link List}         List of retrieved object types
     * @throws LSIndexException     Exception while processing Index operation.
     */
    public List<BObjectTypeSymbolDTO> getObjects(BPackageSymbolDTO dto, boolean isPrivate) throws LSIndexException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        int parameterIndex = 0;
        StringBuilder query = new StringBuilder("SELECT p.id, p.name, p.orgName, o.completionItem, o.name, " +
                "o.private FROM (select id, name, orgName FROM bLangPackage WHERE name = ?");
        if (dto.getOrgName().isEmpty()) {
            query.append(") ");
        } else {
            query.append(" AND orgName = ?) ");
        }
        query.append("AS p INNER JOIN bLangObject AS o WHERE p.id = o.packageId AND o.type = 3 AND o.private = ?");
        try {
            statement = this.connection.prepareStatement(query.toString());
            statement.setString(++parameterIndex, dto.getName());
            if (!dto.getOrgName().isEmpty()) {
                statement.setString(++parameterIndex, dto.getOrgName());
            }
            statement.setBoolean(++parameterIndex, isPrivate);
            
            rs = statement.executeQuery();
            List<BObjectTypeSymbolDTO> objectTypeSymbolDTOs = new ArrayList<>();
            while (rs.next()) {
                BObjectTypeSymbolDTO objectTypeSymbolDTO = new BObjectTypeSymbolDTO.BObjectTypeSymbolDTOBuilder()
                        .setPackageId(rs.getInt(1))
                        .setCompletionItem(DTOUtil.jsonToCompletionItem(rs.getString(4)))
                        .setName(rs.getString(5))
                        .build();
                objectTypeSymbolDTOs.add(objectTypeSymbolDTO);
            }

            return objectTypeSymbolDTOs;
        } catch (SQLException e) {
            throw new LSIndexException("Error while retrieving Object Type information for package from Index");
        } finally {
            this.releaseResources(rs, statement);
        }
    }
}

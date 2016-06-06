/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.eventtable.rdbms;


import org.apache.hadoop.util.bloom.CountingBloomFilter;
import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.extension.eventtable.cache.CachingTable;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class which act as layer between the database and Siddhi. This class performs all the RDBMS related operations and Blooms Filter
 */
public class DBHandler {

    private String tableName;
    private Map<String, String> elementMappings;
    private ExecutionInfo executionInfo;
    private List<Attribute> attributeList;
    private DataSource dataSource;
    private CountingBloomFilter[] bloomFilters;
    private boolean isBloomFilterEnabled;
    private TableDefinition tableDefinition;
    private int bloomFilterSize;
    private int bloomFilterHashFunction;
    private CachingTable cachingTable;
    private static final Logger log = Logger.getLogger(DBHandler.class);


    public DBHandler(DataSource dataSource, String tableName, List<Attribute> attributeList, TableDefinition tableDefinition) {

        Connection con = null;
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.attributeList = attributeList;
        this.tableDefinition = tableDefinition;
        executionInfo = new ExecutionInfo();

        try {

            con = dataSource.getConnection();
            initializeDatabaseExecutionInfo(executionInfo);
            initializeConnection();

        } catch (SQLException e) {
            throw new ExecutionPlanRuntimeException("Error while initialising the connection, " + e.getMessage(), e);
        } finally {
            cleanUpConnections(null, con);
        }

    }

    public ExecutionInfo getExecutionInfoInstance() {
        ExecutionInfo executionInfo = new ExecutionInfo();
        initializeDatabaseExecutionInfo(executionInfo);
        return executionInfo;
    }

    public Map<String, String> getElementMappings() {
        return elementMappings;
    }

    public String getTableName() {
        return tableName;
    }

    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public void setBloomFilterProperties(int bloomFilterSize, int bloomFilterHashFunction) {
        this.bloomFilterSize = bloomFilterSize;
        this.bloomFilterHashFunction = bloomFilterHashFunction;
    }

    public boolean isBloomFilterEnabled() {
        return isBloomFilterEnabled;
    }

    public CountingBloomFilter[] getBloomFilters() {
        return bloomFilters;
    }

    public void addEvent(ComplexEventChunk addingEventChunk) {
        addingEventChunk.reset();
        PreparedStatement stmt = null;
        Connection con = null;
        ArrayList<ComplexEvent> eventArrayList = new ArrayList<ComplexEvent>();

        try {
            con = dataSource.getConnection();
            stmt = con.prepareStatement(executionInfo.getPreparedInsertStatement());
            con.setAutoCommit(false);

            while (addingEventChunk.hasNext()) {
                ComplexEvent complexEvent = addingEventChunk.next();
                eventArrayList.add(complexEvent);
                populateStatement(complexEvent.getOutputData(), stmt, executionInfo.getInsertQueryColumnOrder());
                stmt.addBatch();
            }

            if (eventArrayList.size() > 0) {
                stmt.executeBatch();
                con.commit();

                for (ComplexEvent complexEvent : eventArrayList) {
                    if (cachingTable != null) {
                        cachingTable.add(complexEvent);
                    }
                }

                if (isBloomFilterEnabled) {
                    for (ComplexEvent event : eventArrayList) {
                        addToBloomFilters(event);
                    }
                }
            }

        } catch (SQLException e) {
            throw new ExecutionPlanRuntimeException("Error while adding events to event table, " + e.getMessage(), e);
        } finally {
            cleanUpConnections(stmt, con);
        }
    }


    public void deleteEvent(List<Object[]> eventList, ExecutionInfo executionInfo) {

        PreparedStatement deletionPreparedStatement = null;
        PreparedStatement selectionPreparedStatement = null;
        List<Object[]> selectedEventList = new ArrayList<Object[]>();
        Connection con = null;
        try {

            con = dataSource.getConnection();
            deletionPreparedStatement = con.prepareStatement(executionInfo.getPreparedDeleteStatement());
            selectionPreparedStatement = con.prepareStatement(executionInfo.getPreparedSelectTableStatement());
            con.setAutoCommit(false);

            for (Object[] obj : eventList) {
                populateStatement(obj, deletionPreparedStatement, executionInfo.getDeleteQueryColumnOrder());
                deletionPreparedStatement.addBatch();
                populateStatement(obj, selectionPreparedStatement, executionInfo.getConditionQueryColumnOrder());
                if (selectionPreparedStatement != null && isBloomFilterEnabled) {
                    ResultSet resultSet = selectionPreparedStatement.executeQuery();
                    populateEventListFromResultSet(selectedEventList, resultSet);
                }
            }

            int[] deletedRows = deletionPreparedStatement.executeBatch();
            con.commit();

            if (log.isDebugEnabled()) {
                log.debug(deletedRows.length + " rows deleted in table " + tableName);
            }

            if (isBloomFilterEnabled && (deletedRows != null && deletedRows.length > 0)) {
                for (Object[] obj : selectedEventList) {
                    removeFromBloomFilters(obj);
                }
            }

        } catch (SQLException e) {
            throw new ExecutionPlanRuntimeException("Error while deleting events from database," + e.getMessage(), e);
        } finally {
            cleanUpConnections(deletionPreparedStatement, con);
            cleanUpConnections(selectionPreparedStatement, con);
        }
    }

    public void updateEvent(List<Object[]> updateEventList, ExecutionInfo executionInfo) {

        PreparedStatement updatePreparedStatement = null;
        PreparedStatement selectionPreparedStatement = null;
        List<Object[]> selectedEventList = new ArrayList<Object[]>();
        Connection con = null;
        int[] updatedRows = null;
        try {

            con = dataSource.getConnection();
            updatePreparedStatement = con.prepareStatement(executionInfo.getPreparedUpdateStatement());
            selectionPreparedStatement = con.prepareStatement(executionInfo.getPreparedSelectTableStatement());
            con.setAutoCommit(false);
            for (Object[] obj : updateEventList) {
                populateStatement(obj, updatePreparedStatement, executionInfo.getUpdateQueryColumnOrder());
                updatePreparedStatement.addBatch();
                populateStatement(obj, selectionPreparedStatement, executionInfo.getConditionQueryColumnOrder());
                if (selectionPreparedStatement != null && isBloomFilterEnabled) {
                    ResultSet resultSet = selectionPreparedStatement.executeQuery();
                    populateEventListFromResultSet(selectedEventList, resultSet);
                }
            }

            updatedRows = updatePreparedStatement.executeBatch();
            con.commit();

            if (log.isDebugEnabled()) {
                log.debug(updatedRows.length + " updated in table " + tableName);
            }
        } catch (SQLException e) {
            throw new ExecutionPlanRuntimeException("Error while updating events in database," + e.getMessage(), e);
        } finally {
            cleanUpConnections(updatePreparedStatement, con);
            cleanUpConnections(selectionPreparedStatement, con);
        }

        if (isBloomFilterEnabled && (updatedRows != null && updatedRows.length > 0)) {
            for (Object[] obj : selectedEventList) {
                removeFromBloomFilters(obj);
            }

            for (Object[] obj : updateEventList) {
                addToBloomFilters(obj);
            }
        }
    }

    public void overwriteOrAddEvent(List<Object[]> updateEventList, ExecutionInfo executionInfo) {

        PreparedStatement updatePreparedStatement = null;
        PreparedStatement insertionPreparedStatement = null;
        PreparedStatement selectionPreparedStatement = null;
        List<Object[]> selectedEventList = new ArrayList<Object[]>();
        Connection con = null;
        int[] updatedRows;
        boolean isInserted = false;

        try {

            con = dataSource.getConnection();
            updatePreparedStatement = con.prepareStatement(executionInfo.getPreparedUpdateStatement());
            insertionPreparedStatement = con.prepareStatement(executionInfo.getPreparedInsertStatement());
            selectionPreparedStatement = con.prepareStatement(executionInfo.getPreparedSelectTableStatement());
            con.setAutoCommit(false);

            for (Object[] obj : updateEventList) {
                populateStatement(obj, updatePreparedStatement, executionInfo.getUpdateQueryColumnOrder());
                updatePreparedStatement.addBatch();
                populateStatement(obj, selectionPreparedStatement, executionInfo.getConditionQueryColumnOrder());
                if (selectionPreparedStatement != null && isBloomFilterEnabled) {
                    ResultSet resultSet = selectionPreparedStatement.executeQuery();
                    populateEventListFromResultSet(selectedEventList, resultSet);
                }
            }

            updatedRows = updatePreparedStatement.executeBatch();

            for (int i = 0; i < updatedRows.length; i++) {
                int isUpdated = updatedRows[i];
                if (isUpdated == 0) {
                    isInserted = true;
                    Object[] updateEvent = updateEventList.get(i);
                    populateStatement(updateEvent, insertionPreparedStatement, executionInfo.getInsertQueryColumnOrder());
                    insertionPreparedStatement.addBatch();

                    if (isBloomFilterEnabled) {
                        addToBloomFilters(updateEvent);
                    }
                }
            }

            if (isInserted) {
                insertionPreparedStatement.executeBatch();
            }
            con.commit();

            if (isBloomFilterEnabled) {
                for (Object[] obj : selectedEventList) {
                    removeFromBloomFilters(obj);
                }

                for (Object[] obj : updateEventList) {
                    addToBloomFilters(obj);
                }
            }

        } catch (SQLException e) {
            throw new ExecutionPlanRuntimeException("Error while insertOrOverwriting events from database," + e.getMessage(), e);
        } finally {
            cleanUpConnections(updatePreparedStatement, con);
            cleanUpConnections(insertionPreparedStatement, con);
            cleanUpConnections(selectionPreparedStatement, con);
        }

    }

    public StreamEvent selectEvent(Object[] obj, ExecutionInfo executionInfo) {

        PreparedStatement stmt = null;
        Connection con = null;
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>(false);
        try {

            con = dataSource.getConnection();
            stmt = con.prepareStatement(executionInfo.getPreparedSelectTableStatement());
            populateStatement(obj, stmt, executionInfo.getConditionQueryColumnOrder());
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                Object[] data = new Object[attributeList.size()];
                for (int i = 0; i < attributeList.size(); i++) {
                    switch (attributeList.get(i).getType()) {
                        case BOOL:
                            data[i] = resultSet.getBoolean(attributeList.get(i).getName());
                            break;
                        case DOUBLE:
                            data[i] = resultSet.getDouble(attributeList.get(i).getName());
                            break;
                        case FLOAT:
                            data[i] = resultSet.getFloat(attributeList.get(i).getName());
                            break;
                        case INT:
                            data[i] = resultSet.getInt(attributeList.get(i).getName());
                            break;
                        case LONG:
                            data[i] = resultSet.getLong(attributeList.get(i).getName());
                            break;
                        case STRING:
                            data[i] = resultSet.getString(attributeList.get(i).getName());
                            break;
                        default:
                            data[i] = resultSet.getObject(attributeList.get(i).getName());

                    }
                }
                StreamEvent streamEvent = new StreamEvent(0, 0, attributeList.size());
                streamEvent.setOutputData(data);
                returnEventChunk.add(streamEvent);
            }

        } catch (SQLException e) {
            throw new ExecutionPlanRuntimeException("Error while retrieving events from event table, " + e.getMessage(), e);
        } finally {
            cleanUpConnections(stmt, con);
        }
        return returnEventChunk.getFirst();
    }

    public boolean checkExistence(Object[] obj, ExecutionInfo executionInfo) {

        PreparedStatement stmt = null;
        Connection con = null;
        try {

            con = dataSource.getConnection();
            stmt = con.prepareStatement(executionInfo.getPreparedTableRowExistenceCheckStatement());
            populateStatement(obj, stmt, executionInfo.getConditionQueryColumnOrder());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                cleanUpConnections(stmt, con);
                return true;
            }

        } catch (SQLException e) {
            throw new ExecutionPlanRuntimeException("Error while retrieving events from event table, " + e.getMessage(), e);
        } finally {
            cleanUpConnections(stmt, con);
        }
        return false;
    }


    private void initializeConnection() {
        Statement stmt = null;
        Boolean tableExists = true;
        Connection con = null;

        try {
            con = dataSource.getConnection();
            stmt = con.createStatement();
            stmt.executeQuery(executionInfo.getPreparedTableExistenceCheckStatement());
        } catch (SQLException e) {
            tableExists = false;
            if (log.isDebugEnabled()) {
                log.debug("Table " + tableName + " does not Exist. Table Will be created. ");
            }
        } finally {
            cleanUpConnections(stmt, con);
        }

        try {
            if (!tableExists) {
                con = dataSource.getConnection();
                stmt = con.createStatement();
                stmt.executeUpdate(executionInfo.getPreparedCreateTableStatement());
                if (!con.getAutoCommit()) {
                    con.commit();
                }
            }
        } catch (SQLException e) {
            try {
                if (con != null && !con.getAutoCommit()) {
                    con.rollback();
                }
            } catch (Exception ex) {
                if (log.isDebugEnabled()) {
                    log.debug("Table " + tableName + " Creation Failed. Transaction rollback error ", ex);
                }
            }
            throw new ExecutionPlanRuntimeException("Exception while creating the event table, " + e.getMessage(), e);
        } finally {
            cleanUpConnections(stmt, con);
        }
    }


    /**
     * Populating column values to table Insert query
     */
    private void populateStatement(Object[] o, PreparedStatement stmt, List<Attribute> colOrder) {
        Attribute attribute = null;

        try {
            for (int i = 0; i < colOrder.size(); i++) {
                attribute = colOrder.get(i);
                Object value = o[i];
                if (value != null || attribute.getType() == Attribute.Type.STRING) {
                    switch (attribute.getType()) {
                        case INT:
                            stmt.setInt(i + 1, (Integer) value);
                            break;
                        case LONG:
                            stmt.setLong(i + 1, (Long) value);
                            break;
                        case FLOAT:
                            stmt.setFloat(i + 1, (Float) value);
                            break;
                        case DOUBLE:
                            stmt.setDouble(i + 1, (Double) value);
                            break;
                        case STRING:
                            stmt.setString(i + 1, (String) value);
                            break;
                        case BOOL:
                            stmt.setBoolean(i + 1, (Boolean) value);
                            break;
                    }
                } else {
                    throw new ExecutionPlanRuntimeException("Cannot Execute Insert/Update. Null value detected for " +
                            "attribute" + attribute.getName());
                }
            }
        } catch (SQLException e) {
            throw new ExecutionPlanRuntimeException("Cannot set value to attribute name " + attribute.getName() + ". " +
                    "Hence dropping the event." + e.getMessage(), e);
        }
    }


    /**
     * Generates an event list from db resultSet
     */
    private List<Object[]> populateEventListFromResultSet(List<Object[]> selectedEventList, ResultSet results) {

        int columnCount = tableDefinition.getAttributeList().size();
        try {
            while (results.next()) {
                Object[] eventArray = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    switch (tableDefinition.getAttributeList().get(i).getType()) {
                        case INT:
                            eventArray[i] = results.getInt(i + 1);
                            break;
                        case LONG:
                            eventArray[i] = results.getLong(i + 1);
                            break;
                        case FLOAT:
                            eventArray[i] = results.getFloat(i + 1);
                            break;
                        case DOUBLE:
                            eventArray[i] = results.getDouble(i + 1);
                            break;
                        case STRING:
                            eventArray[i] = results.getString(i + 1);
                            break;
                        case BOOL:
                            eventArray[i] = results.getBoolean(i + 1);
                            break;
                    }
                }
                selectedEventList.add(eventArray);
            }
            results.close();
        } catch (SQLException e) {
            throw new ExecutionPlanRuntimeException("Error while populating event list from db result set," + e.getMessage(), e);
        }
        return selectedEventList;
    }


    /**
     * Construct all the queries and assign to executionInfo instance
     */
    private void initializeDatabaseExecutionInfo(ExecutionInfo executionInfo) {

        String databaseType;
        Connection con = null;

        try {

            con = dataSource.getConnection();
            DatabaseMetaData databaseMetaData = con.getMetaData();
            databaseType = databaseMetaData.getDatabaseProductName();

            elementMappings = DBQueryHelper.getDbTypeMappings().get(databaseType.toLowerCase());
            if (elementMappings == null) {
                elementMappings = DBQueryHelper.getDbTypeMappings().get("default");
            }

            //Constructing (eg: ID  varchar2(255),INFORMATION  varchar2(255)) type values : column_types
            StringBuilder columnTypes = new StringBuilder("");

            //Constructing (eg: id,information) type values : columns
            StringBuilder columns = new StringBuilder("");

            //Constructing (eg: ?,?,?) type values : valuePositionsBuilder
            StringBuilder valuePositionsBuilder = new StringBuilder("");


            boolean appendComma = false;
            for (Attribute attribute : attributeList) {
                String columnName = attribute.getName().toUpperCase();
                if (appendComma) {
                    columnTypes.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_COMMA));
                }
                columnTypes.append(columnName).append("  ");
                if (attribute.getType().equals(Attribute.Type.INT)) {
                    columnTypes.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_INTEGER));
                } else if (attribute.getType().equals(Attribute.Type.LONG)) {
                    columnTypes.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_LONG));
                } else if (attribute.getType().equals(Attribute.Type.FLOAT)) {
                    columnTypes.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_FLOAT));
                } else if (attribute.getType().equals(Attribute.Type.DOUBLE)) {
                    columnTypes.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_DOUBLE));
                } else if (attribute.getType().equals(Attribute.Type.STRING)) {
                    columnTypes.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_STRING));
                } else if (attribute.getType().equals(Attribute.Type.BOOL)) {
                    columnTypes.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_BOOL));
                }

                if (appendComma) {
                    columns.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_COMMA));
                    valuePositionsBuilder.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_COMMA));
                } else {
                    appendComma = true;
                }

                columns.append(attribute.getName());
                valuePositionsBuilder.append(elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_QUESTION_MARK));
            }

            //Constructing quert to create a new table
            String createTableQuery = constructQuery(tableName, elementMappings.get(RDBMSEventTableConstants
                    .EVENT_TABLE_RDBMS_CREATE_TABLE), columnTypes, null, null, null, null);

            //constructing query to insert date into the table row
            String insertTableRowQuery = constructQuery(tableName, elementMappings.get(RDBMSEventTableConstants
                    .EVENT_TABLE_RDBMS_INSERT_DATA), null, columns, valuePositionsBuilder, null, null);

            //Constructing query to check for the table existence
            String isTableExistQuery = constructQuery(tableName, elementMappings.get(RDBMSEventTableConstants
                    .EVENT_TABLE_RDBMS_TABLE_EXIST), null, null, null, null, null);

            executionInfo.setPreparedInsertStatement(insertTableRowQuery);
            executionInfo.setPreparedCreateTableStatement(createTableQuery);
            executionInfo.setInsertQueryColumnOrder(attributeList);
            executionInfo.setPreparedTableExistenceCheckStatement(isTableExistQuery);

        } catch (SQLException e) {
            throw new ExecutionPlanRuntimeException("Error while accessing through datasource connection, " + e.getMessage(), e);
        } finally {
            cleanUpConnections(null, con);
        }
    }

    /**
     * Replace attribute values with target build queries
     *
     * @param tableName     Table Name
     * @param query         query  template
     * @param columnTypes   column types
     * @param columns       columns
     * @param values        values
     * @param column_values column_values
     * @param condition     condition
     * @return query as string
     */
    public String constructQuery(String tableName, String query, StringBuilder columnTypes, StringBuilder columns,
                                 StringBuilder values, StringBuilder column_values, StringBuilder condition) {

        if (query.contains(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_TABLE_NAME)) {
            query = query.replace(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_TABLE_NAME, tableName);
        }
        if (query.contains(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMN_TYPES)) {
            query = query.replace(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMN_TYPES,
                    columnTypes.toString());
        }
        if (query.contains(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMNS)) {
            query = query.replace(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMNS,
                    columns.toString());
        }
        if (query.contains(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_VALUES)) {
            query = query.replace(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_VALUES, values.toString());
        }
        if (query.contains(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMN_VALUES)) {
            query = query.replace(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMN_VALUES,
                    column_values.toString());
        }
        if (query.contains(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_CONDITION)) {
            if (condition == null || condition.toString().trim().equals("")) {
                query = query.replace(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_CONDITION,
                        "").replace("where", "").replace("WHERE", "");
            } else {
                query = query.replace(RDBMSEventTableConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_CONDITION,
                        condition.toString());
            }
        }
        return query;
    }

    private void cleanUpConnections(Statement stmt, Connection con) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                throw new ExecutionPlanRuntimeException("unable to release statement, " + e.getMessage(), e);
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw new ExecutionPlanRuntimeException("unable to release connection, " + e.getMessage(), e);
            }
        }
    }


    //Bloom Filter Operations  -----------------------------------------------------------------------------------------

    public void buildBloomFilters() {
        CountingBloomFilter[] bloomFilters = new CountingBloomFilter[tableDefinition.getAttributeList().size()];

        for (int i = 0; i < bloomFilters.length; i++) {
            bloomFilters[i] = new CountingBloomFilter(bloomFilterSize, bloomFilterHashFunction, Hash.MURMUR_HASH);
        }
        Connection con = null;
        Statement stmt = null;
        try {
            con = dataSource.getConnection();
            stmt = con.createStatement();
            String selectTableRowQuery = constructQuery(tableName, elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_SELECT_TABLE), null, null, null, null, null);
            ResultSet results = stmt.executeQuery(selectTableRowQuery);
            while (results.next()) {
                for (int i = 0; i < bloomFilters.length; i++) {
                    switch (tableDefinition.getAttributeList().get(i).getType()) {
                        case INT:
                            bloomFilters[i].add(new Key(Integer.toString(results.getInt(i + 1)).getBytes()));
                            break;
                        case LONG:
                            bloomFilters[i].add(new Key(Long.toString(results.getLong(i + 1)).getBytes()));
                            break;
                        case FLOAT:
                            bloomFilters[i].add(new Key(Float.toString(results.getFloat(i + 1)).getBytes()));
                            break;
                        case DOUBLE:
                            bloomFilters[i].add(new Key(Double.toString(results.getDouble(i + 1)).getBytes()));
                            break;
                        case STRING:
                            String attributeValue = results.getString(i + 1);
                            if(attributeValue != null){
                                bloomFilters[i].add(new Key(attributeValue.getBytes()));
                            }
                            break;
                        case BOOL:
                            bloomFilters[i].add(new Key(Boolean.toString(results.getBoolean(i + 1)).getBytes()));
                            break;

                    }
                }
            }
            results.close();
            this.bloomFilters = bloomFilters;
            this.isBloomFilterEnabled = true;
        } catch (SQLException ex) {
            throw new ExecutionPlanRuntimeException("Error while initiating blooms filter with db data, " + ex.getMessage(), ex);
        } finally {
            cleanUpConnections(stmt, con);
        }
    }

    public void addToBloomFilters(ComplexEvent event) {
        for (int i = 0; i < attributeList.size(); i++) {
            if(event.getOutputData()[i] != null){
                bloomFilters[i].add(new Key(event.getOutputData()[i].toString().getBytes()));
            }
        }
    }

    public void addToBloomFilters(Object[] obj) {
        for (int i = 0; i < attributeList.size(); i++) {
            if(obj[i] != null){
                bloomFilters[i].add(new Key(obj[i].toString().getBytes()));
            }
        }
    }

    public void removeFromBloomFilters(Object[] obj) {
        for (int i = 0; i < attributeList.size(); i++) {
            if(obj[i] != null){
                bloomFilters[i].delete(new Key(obj[i].toString().getBytes()));
            }
        }
    }


    //Pre loading the cache ---------------------------------------------------------------------------------------------------------

    public void loadDBCache(CachingTable cachingTable, String cacheSizeInString) {

        this.cachingTable = cachingTable;
        Connection con = null;
        Statement stmt = null;
        try {
            con = dataSource.getConnection();
            stmt = con.createStatement();
            con = dataSource.getConnection();
            stmt = con.createStatement();
            String selectTableRowQuery = constructQuery(tableName, elementMappings.get(RDBMSEventTableConstants.EVENT_TABLE_GENERIC_RDBMS_LIMIT_SELECT_TABLE), null, null, new StringBuilder(cacheSizeInString), null, null);
            ResultSet resultSet = stmt.executeQuery(selectTableRowQuery);
            while (resultSet.next()) {
                Object[] data = new Object[attributeList.size()];
                for (int i = 0; i < attributeList.size(); i++) {
                    switch (attributeList.get(i).getType()) {
                        case BOOL:
                            data[i] = resultSet.getBoolean(attributeList.get(i).getName());
                            break;
                        case DOUBLE:
                            data[i] = resultSet.getDouble(attributeList.get(i).getName());
                            break;
                        case FLOAT:
                            data[i] = resultSet.getFloat(attributeList.get(i).getName());
                            break;
                        case INT:
                            data[i] = resultSet.getInt(attributeList.get(i).getName());
                            break;
                        case LONG:
                            data[i] = resultSet.getLong(attributeList.get(i).getName());
                            break;
                        case STRING:
                            data[i] = resultSet.getString(attributeList.get(i).getName());
                            break;
                        default:
                            data[i] = resultSet.getObject(attributeList.get(i).getName());

                    }
                }
                StreamEvent streamEvent = new StreamEvent(0, 0, attributeList.size());
                streamEvent.setOutputData(data);
                cachingTable.add(streamEvent);
            }
            resultSet.close();

        } catch (SQLException ex) {
            throw new ExecutionPlanRuntimeException("Error while loading cache with db data, " + ex.getMessage(), ex);
        } finally {
            cleanUpConnections(stmt, con);
        }
    }


}

/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.siddhi.core.table.rdbms;


import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.query.api.definition.Attribute;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class DBConfiguration {

    private String databaseName;
    private String fullTableName;
    private String tableName;
    private Map<String, String> elementMappings;
    private ExecutionInfo executionInfo;
    private Connection con;
    private List<Attribute> attributeList;
    private DataSource dataSource;
    private Logger log = Logger.getLogger(DBQueryHelper.class);


    public DBConfiguration(DataSource dataSource, String tableName, List<Attribute> attributeList) {

        this.dataSource = dataSource;
        this.tableName = tableName;
        this.attributeList = attributeList;

        try {
            if (executionInfo == null) {
                executionInfo = new ExecutionInfo();
                con = dataSource.getConnection();
                databaseName = con.getCatalog();
                fullTableName = databaseName + "." + tableName;
                initializeDatabaseExecutionInfo();
            }
            initializeConnection();

        } catch (SQLException e) {
            log.error("Error while initialising the connection ", e);
        } finally {
            cleanUpConnections(null, con);
        }

    }

    public ExecutionInfo getExecutionInfo() {
        return executionInfo;
    }

    public Map<String, String> getElementMappings() {
        return elementMappings;
    }

    public String getFullTableName() {
        return fullTableName;
    }

    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public void addEvent(ComplexEventChunk<StreamEvent> addingEventChunk) {
        addingEventChunk.reset();
        PreparedStatement stmt = null;
        while (addingEventChunk.hasNext()) {
            StreamEvent streamEvent = addingEventChunk.next();
            try {
                con = dataSource.getConnection();
                stmt = con.prepareStatement(executionInfo.getPreparedInsertStatement());
                populateStatement(streamEvent.getOutputData(), stmt, executionInfo.getInsertQueryColumnOrder());
                stmt.executeUpdate();
            } catch (SQLException e) {
                log.error("Error while adding events to event table", e);
            } finally {
                cleanUpConnections(stmt, con);
            }
        }
    }


    public void deleteEvent(Object[] obj) {

        PreparedStatement stmt = null;
        try {

            con = dataSource.getConnection();
            stmt = con.prepareStatement(executionInfo.getPreparedDeleteStatement());
            populateStatement(obj, stmt, executionInfo.getDeleteQueryColumnOrder());
            int deletedRows = stmt.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug(deletedRows + " rows deleted in table " + fullTableName);
            }
        } catch (SQLException e) {
            log.error("Error while deleting the event", e);
        } finally {
            cleanUpConnections(stmt, con);
        }
    }

    public void updateEvent(Object[] obj) {

        PreparedStatement stmt = null;
        try {

            con = dataSource.getConnection();
            stmt = con.prepareStatement(executionInfo.getPreparedUpdateStatement());
            populateStatement(obj, stmt, executionInfo.getUpdateQueryColumnOrder());
            int updatedRows = stmt.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug(updatedRows + " updated deleted in table " + fullTableName);
            }
        } catch (SQLException e) {
            log.error("Error while updating the event", e);
        } finally {
            cleanUpConnections(stmt, con);
        }
    }

    public StreamEvent selectEvent(Object[] obj) {

        PreparedStatement stmt = null;
        ComplexEventChunk<StreamEvent> returnEventChunk = new ComplexEventChunk<StreamEvent>();
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
            log.error("Error while retrieving events from event table", e);
        } finally {
            cleanUpConnections(stmt, con);
        }
        return returnEventChunk.getFirst();
    }

    public boolean checkExistence(Object[] obj) {

        PreparedStatement stmt = null;
        try {

            con = dataSource.getConnection();
            stmt = con.prepareStatement(executionInfo.getPreparedTableRowExistenceCheckStatement());
            populateStatement(obj, stmt, executionInfo.getConditionQueryColumnOrder());
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return true;
            }

        } catch (SQLException e) {
            log.error("Error while retrieving events from event table", e);
        } finally {
            cleanUpConnections(stmt, con);
        }

        return false;
    }


    private void initializeConnection() {
        try {
            Statement stmt = null;
            Boolean tableExists = true;

            try {
                stmt = con.createStatement();
                stmt.executeQuery(executionInfo.getPreparedTableExistenceCheckStatement());
            } catch (SQLException e) {
                tableExists = false;
                if (log.isDebugEnabled()) {
                    log.debug("Table " + tableName + " does not Exist. Table Will be created. ");
                }
            }

            try {
                if (!tableExists) {
                    stmt.executeUpdate(executionInfo.getPreparedCreateTableStatement());
                }
            } catch (SQLException e) {
                log.error(e);
            }

        } catch (Exception e) {
            log.error(e);

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
                if (value != null) {
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
                    log.error("Cannot Execute Insert/Update. Null value detected for " +
                            "attribute" + attribute.getName());
                }
            }
        } catch (SQLException e) {
            log.error("Cannot set value to attribute name " + attribute.getName() + ". " +
                    "Hence dropping the event." + e.getMessage(), e);
        }
    }


    /**
     * Construct all the queries and assign to executionInfo instance
     */
    private void initializeDatabaseExecutionInfo() {

        String databaseType;

        try {

            DatabaseMetaData databaseMetaData = con.getMetaData();
            databaseType = databaseMetaData.getDatabaseProductName();

            elementMappings = DBQueryHelper.getDbTypeMappings().get(databaseType.toLowerCase());

            //Constructing (eg: ID  varchar2(255),INFORMATION  varchar2(255)) type values : column_types
            StringBuilder column_types = new StringBuilder("");

            //Constructing (eg: id,information) type values : columns
            StringBuilder columns = new StringBuilder("");

            //Constructing (eg: ?,?,?) type values : valuePositionsBuilder
            StringBuilder valuePositionsBuilder = new StringBuilder("");


            boolean appendComma = false;
            for (Attribute attribute : attributeList) {
                String columnName = attribute.getName().toUpperCase();
                if (appendComma) {
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_COMMA));
                }
                column_types.append(columnName).append("  ");
                if (attribute.getType().equals(Attribute.Type.INT)) {
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_INTEGER));
                } else if (attribute.getType().equals(Attribute.Type.LONG)) {
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_LONG));
                } else if (attribute.getType().equals(Attribute.Type.FLOAT)) {
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_FLOAT));
                } else if (attribute.getType().equals(Attribute.Type.DOUBLE)) {
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_DOUBLE));
                } else if (attribute.getType().equals(Attribute.Type.STRING)) {
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_STRING));
                } else if (attribute.getType().equals(Attribute.Type.BOOL)) {
                    column_types.append(elementMappings.get(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_BOOLEAN));
                }

                if (appendComma) {
                    columns.append(elementMappings.get(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_COMMA));
                    valuePositionsBuilder.append(elementMappings.get(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_COMMA));
                } else {
                    appendComma = true;
                }

                columns.append(attribute.getName());
                valuePositionsBuilder.append(elementMappings.get(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_QUESTION_MARK));
            }

            //Constructing quert to create a new table
            String createTableQuery = constructQuery(tableName, elementMappings.get(RDBMSEventAdaptorConstants
                    .EVENT_TABLE_RDBMS_CREATE_TABLE), column_types, null, null, null, null);

            //constructing query to insert date into the table row
            String insertTableRowQuery = constructQuery(tableName, elementMappings.get(RDBMSEventAdaptorConstants
                    .EVENT_TABLE_RDBMS_INSERT_DATA), null, columns, valuePositionsBuilder, null, null);

            //Constructing query to check for the table existence
            String isTableExistQuery = constructQuery(tableName, elementMappings.get(RDBMSEventAdaptorConstants
                    .EVENT_TABLE_RDBMS_TABLE_EXIST), null, null, null, null, null);

            executionInfo.setPreparedInsertStatement(insertTableRowQuery);
            executionInfo.setPreparedCreateTableStatement(createTableQuery);
            executionInfo.setInsertQueryColumnOrder(attributeList);
            executionInfo.setPreparedTableExistenceCheckStatement(isTableExistQuery);

        } catch (SQLException e) {
            log.error("Error while accessing through datasource connection", e);
        }
    }

    /**
     * Replace attribute values with target build queries
     */
    public String constructQuery(String tableName, String query, StringBuilder column_types, StringBuilder columns,
                                 StringBuilder values, StringBuilder column_values, StringBuilder condition) {

        if (query.contains(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_TABLE_NAME)) {
            query = query.replace(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_TABLE_NAME, tableName);
        }
        if (query.contains(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMN_TYPES)) {
            query = query.replace(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMN_TYPES,
                    column_types.toString());
        }
        if (query.contains(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMNS)) {
            query = query.replace(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMNS,
                    columns.toString());
        }
        if (query.contains(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_VALUES)) {
            query = query.replace(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_VALUES, values.toString());
        }
        if (query.contains(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMN_VALUES)) {
            query = query.replace(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMN_VALUES,
                    column_values.toString());
        }
        if (query.contains(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_CONDITION)) {
            if (condition.toString().trim().equals("")) {
                query = query.replace(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_CONDITION,
                        condition.toString()).replace("where", "").replace("WHERE", "");
            } else {

                query = query.replace(RDBMSEventAdaptorConstants.EVENT_TABLE_RDBMS_ATTRIBUTE_CONDITION,
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
                log.error("unable to release statement", e);
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("unable to release connection", e);
            }
        }
    }

}

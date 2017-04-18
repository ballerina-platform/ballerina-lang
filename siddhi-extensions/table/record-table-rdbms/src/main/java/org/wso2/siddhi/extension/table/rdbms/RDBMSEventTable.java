/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.extension.table.rdbms;

import org.wso2.siddhi.core.table.record.AbstractRecordTable;
import org.wso2.siddhi.core.table.record.ConditionBuilder;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.extension.table.rdbms.config.RDBMSQueryConfigurationEntry;
import org.wso2.siddhi.extension.table.rdbms.exception.RDBMSTableException;
import org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants;
import org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableUtils;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public class RDBMSEventTable extends AbstractRecordTable {

    private static final String DSNAME_KEY = "datasource";

    private static final String TABLE_NAME_PLACEHOLDER = "{{TABLE_NAME}}";

    private RDBMSQueryConfigurationEntry queryConfigurationEntry;
    private TableDefinition tableDefinition;
    private DataSource dataSource;
    private String tableName;

    private List<Attribute> attributes;

    @Override

    protected void init(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
        this.processTypes();
        Annotation annotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_FROM,
                tableDefinition.getAnnotations());
        String dsName = annotation.getElement(DSNAME_KEY);
        //TODO finalize how the datasource is received (carbon datasources or not)
        this.dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        this.queryConfigurationEntry = RDBMSTableUtils.lookupCurrentQueryConfigurationEntry(this.dataSource, "");

        this.tableName = annotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_TABLE_NAME);
        if (!this.tableExists()) {

        }
    }

    @Override
    protected void add(List<Object[]> records) {
        String insertQuery = this.translateQuery(this.queryConfigurationEntry.getRecordInsertQuery());


    }

    @Override
    protected List<Object[]> find(Map<String, Object> findConditionParameterMap, CompiledCondition compiledCondition) {
        Connection conn = this.getConnection();
        return WHAT ?
    }

    @Override
    protected boolean contains(Map<String, Object> containsConditionParameterMap, CompiledCondition compiledCondition) {
        return false;
    }

    @Override
    protected void delete(List<Map<String, Object>> deleteConditionParameterMaps, CompiledCondition compiledCondition) {

    }

    @Override
    protected void update(List<Map<String, Object>> updateConditionParameterMaps, CompiledCondition compiledCondition,
                          List<Map<String, Object>> updateValues) {

    }

    @Override
    protected void updateOrAdd(List<Map<String, Object>> updateConditionParameterMaps,
                               CompiledCondition compiledCondition, List<Map<String, Object>> updateValues,
                               List<Object[]> addingRecords) {

    }

    @Override
    protected CompiledCondition compileCondition(ConditionBuilder conditionBuilder) {
        RDBMSConditionVisitor visitor = new RDBMSConditionVisitor(this.queryConfigurationEntry);
        conditionBuilder.build(visitor);
        CompiledCondition rdbmsCompiledCondition = new RDBMSCompiledCondition(visitor.returnCondition());
        return null;
    }

    private Connection getConnection() {
        return this.getConnection(true);
    }

    private Connection getConnection(boolean autoCommit) {
        Connection conn = null;
        try {
            conn = this.dataSource.getConnection();
            conn.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new RDBMSTableException("Error initializing connection: " + e.getMessage(), e);
        }
        return conn;
    }

    private String translateQuery(String query) {
        if (query == null) {
            return null;
        }
        return query.replace(TABLE_NAME_PLACEHOLDER, this.tableName);
    }

    private void createTable() {
        String createQuery = this.translateQuery(this.queryConfigurationEntry.getTableCreateQuery());


    }

    private void executeQuery(String query) {
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
        } catch (SQLException e) {
            RDBMSTableUtils.rollbackConnection(conn);
        } finally {
            RDBMSTableUtils.cleanupConnection(rs, stmt, conn);
        }
    }

    private boolean tableExists() {
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String query = this.translateQuery(this.queryConfigurationEntry.getTableCheckQuery());
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            return true;
        } catch (SQLException e) {
            RDBMSTableUtils.rollbackConnection(conn);
            return false;
        } finally {
            RDBMSTableUtils.cleanupConnection(rs, stmt, conn);
        }
    }

    private void processTypes() {
        List<Attribute> attributes = this.tableDefinition.getAttributeList();
        this.attributes = new ArrayList<>();
        for (Attribute attribute : attributes) {
            this.attributes.add(attribute);
        }
    }

    private void populateStatement(Object[] records, PreparedStatement stmt) {
        Attribute attribute = null;
        try {
            for (int i = 0; i < this.attributes.size(); i++) {
                attribute = this.attributes.get(i);
                Object value = records[i];
                if (value != null || attribute.getType() == Attribute.Type.STRING) {
                    switch (attribute.getType()) {
                        case BOOL:
                            stmt.setBoolean(i + 1, (Boolean) value);
                            break;
                        case DOUBLE:
                            stmt.setDouble(i + 1, (Double) value);
                            break;
                        case FLOAT:
                            stmt.setFloat(i + 1, (Float) value);
                            break;
                        case INT:
                            stmt.setInt(i + 1, (Integer) value);
                            break;
                        case LONG:
                            stmt.setLong(i + 1, (Long) value);
                            break;
                        case STRING:
                            stmt.setString(i + 1, (String) value);
                            break;
                    }
                } else {
                    throw new RDBMSTableException("Cannot Execute Insert/Update: null value detected for " +
                            "attribute '" + attribute.getName() + "'");
                }
            }
        } catch (SQLException e) {
            throw new RDBMSTableException("Dropping event since value for attribute name " + attribute.getName() +
                    "cannot be set: " + e.getMessage(), e);
        }
    }

}

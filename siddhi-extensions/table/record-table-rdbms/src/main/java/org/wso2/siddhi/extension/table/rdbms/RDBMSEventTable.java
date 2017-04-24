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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.wso2.siddhi.core.exception.CannotLoadConfigurationException;
import org.wso2.siddhi.core.table.record.AbstractRecordTable;
import org.wso2.siddhi.core.table.record.ConditionBuilder;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.extension.table.rdbms.config.RDBMSQueryConfigurationEntry;
import org.wso2.siddhi.extension.table.rdbms.config.RDBMSTypeMapping;
import org.wso2.siddhi.extension.table.rdbms.exception.RDBMSTableException;
import org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants;
import org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableUtils;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class RDBMSEventTable extends AbstractRecordTable {

    private static final String ANNOTATION_STORE = "store";
    private static final String ANNOTATION_PRIMARY_KEY = "PrimaryKey";
    private static final String ANNOTATION_INDEX = "IndexBy";

    private static final String TABLE_NAME_PLACEHOLDER = "{{TABLE_NAME}}";
    private static final String COLUMNS_PLACEHOLDER = "{{COLUMNS, PRIMARY_KEYS}}";
    private static final String INDEX_PLACEHOLDER = "{{INDEX_COLUMNS}}";
    private static final String Q_PLACEHOLDER = "{{Q}}";

    private static final String SEPARATOR = ", ";
    private static final String PRIMARY_KEY_DEF = "PRIMARY KEY";

    private RDBMSQueryConfigurationEntry queryConfigurationEntry;
    private TableDefinition tableDefinition;
    private DataSource dataSource;
    private String tableName;

    private List<Attribute> attributes;

    @Override
    protected void init(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
        this.attributes = this.tableDefinition.getAttributeList();
        Annotation storeAnnotation = AnnotationHelper.getAnnotation(ANNOTATION_STORE,
                tableDefinition.getAnnotations());
        Annotation primaryKeys = AnnotationHelper.getAnnotation(ANNOTATION_PRIMARY_KEY,
                tableDefinition.getAnnotations());
        Annotation indices = AnnotationHelper.getAnnotation(ANNOTATION_INDEX, tableDefinition.getAnnotations());

        String jndiResourceName = storeAnnotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_JNDI_RESOURCE);
        if (RDBMSTableUtils.isEmpty(jndiResourceName)) {
            try {
                this.lookupDatasource(jndiResourceName);
            } catch (NamingException e) {
                throw new RDBMSTableException("Failed to lookup datasource with provided JNDI resource name '"
                        + jndiResourceName + "': " + e.getMessage(), e);
            }
        } else {
            this.initializeDatasource(storeAnnotation);
        }
        String tableName = storeAnnotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_TABLE_NAME);
        if (RDBMSTableUtils.isEmpty(tableName)) {
            this.tableName = tableName;
        } else {
            this.tableName = this.tableDefinition.getId();
        }
        try {
            this.queryConfigurationEntry = RDBMSTableUtils.lookupCurrentQueryConfigurationEntry(this.dataSource);
        } catch (CannotLoadConfigurationException e) {
            throw new RDBMSTableException("Failed to initialize DB Configuration entry for table '" + this.tableName
                    + "': " + e.getMessage(), e);
        }
        if (!this.tableExists()) {
            this.createTable(storeAnnotation, primaryKeys, indices);
        }
    }

    @Override
    protected void add(List<Object[]> records) {
        String insertQuery = this.resolveTableName(this.queryConfigurationEntry.getRecordInsertQuery());
        StringBuilder params = new StringBuilder();

        int fieldsLeft = this.attributes.size();
        while (fieldsLeft > 0) {
            params.append("?");
            if (fieldsLeft > 1) {
                params.append(SEPARATOR);
            }
            fieldsLeft = fieldsLeft - 1;
        }
        insertQuery = insertQuery.replace(Q_PLACEHOLDER, params.toString());
        try {
            this.batchExecuteQueriesWithRecords(insertQuery, records, false);
        } catch (SQLException e) {
            throw new RDBMSTableException("Error in adding events to '" + this.tableName + "' store: "
                    + e.getMessage(), e);
        }

    }

    @Override
    protected List<Object[]> find(Map<String, Object> findConditionParameterMap, CompiledCondition compiledCondition) {
        return null;
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
        //TODO verify
        RDBMSConditionVisitor visitor = new RDBMSConditionVisitor(this.queryConfigurationEntry);
        conditionBuilder.build(visitor);
        return new RDBMSCompiledCondition(visitor.returnCondition());
    }

    private void lookupDatasource(String resourceName) throws NamingException {
        this.dataSource = InitialContext.doLookup(resourceName);
    }

    private void initializeDatasource(Annotation storeAnnotation) {
        Properties connectionProperties = new Properties();
        String poolPropertyString = storeAnnotation.getElement(
                RDBMSTableConstants.ANNOTATION_ELEMENT_POOL_PROPERTIES);
        connectionProperties.setProperty("jdbcUrl", storeAnnotation.getElement(
                RDBMSTableConstants.ANNOTATION_ELEMENT_URL));
        connectionProperties.setProperty("dataSource.user", storeAnnotation.getElement(
                RDBMSTableConstants.ANNOTATION_ELEMENT_USERNAME));
        connectionProperties.setProperty("dataSource.password", storeAnnotation.getElement(
                RDBMSTableConstants.ANNOTATION_ELEMENT_PASSWORD));
        if (poolPropertyString != null) {
            List<String[]> poolProps = this.processKeyValuePairs(poolPropertyString);
            poolProps.forEach(pair -> connectionProperties.setProperty(pair[0], pair[1]));
        }

        HikariConfig config = new HikariConfig(connectionProperties);
        this.dataSource = new HikariDataSource(config);
    }

    private Connection getConnection() {
        return this.getConnection(true);
    }

    private Connection getConnection(boolean autoCommit) {
        Connection conn;
        try {
            conn = this.dataSource.getConnection();
            conn.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new RDBMSTableException("Error initializing connection: " + e.getMessage(), e);
        }
        return conn;
    }

    private String resolveTableName(String query) {
        if (query == null) {
            return null;
        }
        return query.replace(TABLE_NAME_PLACEHOLDER, this.tableName);
    }

    private void createTable(Annotation storeAnnotation, Annotation primaryKeys, Annotation indices) {
        //TODO once-over
        RDBMSTypeMapping typeMapping = this.queryConfigurationEntry.getRDBMSTypeMapping();
        StringBuilder builder = new StringBuilder();
        List<Element> primaryKeyMap = primaryKeys.getElements();
        List<Element> indexMap = indices.getElements();
        List<String> queries = new ArrayList<>();
        String createQuery = this.resolveTableName(this.queryConfigurationEntry.getTableCreateQuery());
        String indexQuery = this.resolveTableName(this.queryConfigurationEntry.getIndexCreateQuery());
        Map<String, String> fieldLengths = this.processFieldLengths(storeAnnotation.getElement(
                RDBMSTableConstants.ANNOTATION_ELEMENT_FIELD_LENGTHS));

        for (Attribute attribute : this.attributes) {
            builder.append(attribute.getName()).append(" ");
            switch (attribute.getType()) {
                case BOOL:
                    builder.append(typeMapping.getBooleanType());
                    break;
                case DOUBLE:
                    builder.append(typeMapping.getDoubleType());
                    break;
                case FLOAT:
                    builder.append(typeMapping.getFloatType());
                    break;
                case INT:
                    builder.append(typeMapping.getIntegerType());
                    break;
                case LONG:
                    builder.append(typeMapping.getLongType());
                    break;
                case OBJECT:
                    builder.append(typeMapping.getBinaryType());
                    break;
                case STRING:
                    builder.append(typeMapping.getStringType());
                    if (fieldLengths.containsKey(attribute.getName().toLowerCase())) {
                        builder.append("(").append(fieldLengths.get(attribute.getName())).append(")");
                    }
                    break;
            }
            if (this.attributes.indexOf(attribute) != this.attributes.size() - 1 || primaryKeyMap != null) {
                builder.append(SEPARATOR);
            }
            if (primaryKeyMap != null) {
                builder.append(PRIMARY_KEY_DEF).append("(").append(this.handleAnnotatedElements(primaryKeyMap)).append(")");
            }
        }
        queries.add(createQuery.replace(COLUMNS_PLACEHOLDER, builder.toString()));
        if (indexMap != null && !indexMap.isEmpty()) {
            queries.add(indexQuery.replace(INDEX_PLACEHOLDER, this.handleAnnotatedElements(indexMap)));
        }
        try {
            this.executeQueries(queries, false);
        } catch (SQLException e) {
            throw new RDBMSTableException("Unable to initialize table '" + this.tableName + "': " + e.getMessage(), e);
        }
    }

    private String handleAnnotatedElements(List<Element> elements) {
        StringBuilder sb = new StringBuilder();
        for (Element elem : elements) {
            sb.append(elem.getKey());
            if (elements.indexOf(elem) != elements.size() - 1) {
                sb.append(SEPARATOR);
            }
        }
        return sb.toString();
    }

    private void executeQueries(List<String> queries, boolean autocommit) throws SQLException {
        Connection conn = this.getConnection(autocommit);
        boolean committed = autocommit;
        PreparedStatement stmt;
        try {
            for (String query : queries) {
                stmt = conn.prepareStatement(query);
                stmt.execute();
                RDBMSTableUtils.cleanupConnection(null, stmt, null);
            }
            if (!autocommit) {
                conn.commit();
                committed = true;
            }
        } catch (SQLException e) {
            RDBMSTableUtils.rollbackConnection(conn);
            throw e;
        } finally {
            if (!committed) {
                RDBMSTableUtils.rollbackConnection(conn);
            }
            RDBMSTableUtils.cleanupConnection(null, null, conn);
        }
    }

    private void batchExecuteQueriesWithRecords(String query, List<Object[]> records, boolean autocommit) throws SQLException {
        PreparedStatement stmt = null;
        boolean committed = autocommit;
        //TODO check if autocommit needs to be false (e.g. for Postgres case)
        Connection conn = this.getConnection(autocommit);
        try {
            stmt = conn.prepareStatement(query);
            for (Object[] record : records) {
                this.populateStatement(record, stmt);
                stmt.addBatch();
            }
            stmt.executeBatch();
            if (!autocommit) {
                conn.commit();
                committed = true;
            }
        } catch (SQLException e) {
            RDBMSTableUtils.rollbackConnection(conn);
            throw e;
        } finally {
            if (!committed) {
                RDBMSTableUtils.rollbackConnection(conn);
            }
            RDBMSTableUtils.cleanupConnection(null, stmt, conn);
        }
    }

    private boolean tableExists() {
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String query = this.resolveTableName(this.queryConfigurationEntry.getTableCheckQuery());
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

    private Map<String, String> processFieldLengths(String fieldInfo) {
        Map<String, String> fieldLengths = new HashMap<>();
        List<String[]> processedLengths = this.processKeyValuePairs(fieldInfo);
        processedLengths.forEach(field -> fieldLengths.put(field[0].toLowerCase(), field[1]));
        return fieldLengths;
    }

    private List<String[]> processKeyValuePairs(String annotationString) {
        List<String[]> keyValuePairs = new ArrayList<>();
        if (RDBMSTableUtils.isEmpty(annotationString)) {
            String[] pairs = annotationString.split(",");
            for (String element : pairs) {
                if (!element.contains(":")) {
                    throw new RDBMSTableException("Property '" + element + "' does not adhere to the expected " +
                            "format: a property must be a key-value pair separated by a colon (:)");
                }
                String[] pair = element.split(":");
                if (pair.length != 2) {
                    throw new RDBMSTableException("Property '" + pair[0] + "' does not adhere to the expected " +
                            "format: a property must be a key-value pair separated by a colon (:)");
                } else {
                    keyValuePairs.add(pair);
                }
            }
        }
        return keyValuePairs;
    }

    private void populateStatement(Object[] record, PreparedStatement stmt) {
        Attribute attribute = null;
        try {
            for (int i = 0; i < this.attributes.size(); i++) {
                attribute = this.attributes.get(i);
                Object value = record[i];
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
                        case OBJECT:
                            stmt.setObject(i + 1, value);
                            break;
                        case STRING:
                            stmt.setString(i + 1, (String) value);
                            break;
                    }
                } else {
                    //TODO check behaviour for 1 invalid record (1 null field) for a list of records
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

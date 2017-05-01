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
import org.wso2.siddhi.core.table.record.RecordIterator;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.extension.table.rdbms.config.RDBMSQueryConfigurationEntry;
import org.wso2.siddhi.extension.table.rdbms.config.RDBMSTypeMapping;
import org.wso2.siddhi.extension.table.rdbms.exception.RDBMSTableException;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.*;

public class RDBMSEventTable extends AbstractRecordTable {

    private RDBMSQueryConfigurationEntry queryConfigurationEntry;
    private DataSource dataSource;
    private String tableName;

    private List<Attribute> attributes;

    @Override
    protected void init(TableDefinition tableDefinition) {
        this.attributes = tableDefinition.getAttributeList();
        Annotation storeAnnotation = AnnotationHelper.getAnnotation(ANNOTATION_STORE, tableDefinition.getAnnotations());
        Annotation primaryKeys = AnnotationHelper.getAnnotation(ANNOTATION_PRIMARY_KEY,
                tableDefinition.getAnnotations());
        Annotation indices = AnnotationHelper.getAnnotation(ANNOTATION_INDEX, tableDefinition.getAnnotations());
        String jndiResourceName = storeAnnotation.getElement(ANNOTATION_ELEMENT_JNDI_RESOURCE);
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
        String tableName = storeAnnotation.getElement(ANNOTATION_ELEMENT_TABLE_NAME);
        if (RDBMSTableUtils.isEmpty(tableName)) {
            this.tableName = tableName;
        } else {
            this.tableName = tableDefinition.getId();
        }
        try {
            if (this.queryConfigurationEntry == null) {
                this.queryConfigurationEntry = RDBMSTableUtils.lookupCurrentQueryConfigurationEntry(this.dataSource);
            }
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
        String sql = this.composeInsertQuery();
        try {
            this.batchExecuteQueriesWithRecords(sql, records, false);
        } catch (SQLException e) {
            throw new RDBMSTableException("Error in adding events to '" + this.tableName + "' store: "
                    + e.getMessage(), e);
        }
    }

    private String composeInsertQuery() {
        String insertQuery = this.resolveTableName(this.queryConfigurationEntry.getRecordInsertQuery());
        StringBuilder params = new StringBuilder();
        int fieldsLeft = this.attributes.size();
        while (fieldsLeft > 0) {
            params.append(QUESTION_MARK);
            if (fieldsLeft > 1) {
                params.append(SEPARATOR);
            }
            fieldsLeft = fieldsLeft - 1;
        }
        return insertQuery.replace(Q_PLACEHOLDER, params.toString());
    }

    @Override
    protected RecordIterator<Object[]> find(Map<String, Object> findConditionParameterMap,
                                            CompiledCondition compiledCondition) {
        String readQuery = this.queryConfigurationEntry.getRecordSelectQuery();
        String condition = ((RDBMSCompiledCondition) compiledCondition).getCompiledQuery();
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (RDBMSTableUtils.isEmpty(condition)) {
                stmt = conn.prepareStatement(readQuery.replace(PLACEHOLDER_CONDITION, ""));
            } else {
                stmt = conn.prepareStatement(RDBMSTableUtils.formatQueryWithCondition(readQuery, condition));
            }
            RDBMSTableUtils.resolveCondition(stmt, (RDBMSCompiledCondition) compiledCondition, findConditionParameterMap, 0);
            rs = stmt.executeQuery();
            //Passing all java.sql artifacts to the iterator to ensure everything gets cleaned up at once.
            return new RDBMSIterator(conn, stmt, rs, this.attributes, this.tableName);
        } catch (SQLException e) {
            throw new RDBMSTableException("Error retrieving records from table '" + this.tableName + "': "
                    + e.getMessage(), e);
        } finally {
            RDBMSTableUtils.cleanupConnection(rs, stmt, conn);
        }
    }

    @Override
    protected boolean contains(Map<String, Object> containsConditionParameterMap, CompiledCondition compiledCondition) {
        String containsQuery = this.resolveTableName(this.queryConfigurationEntry.getRecordExistsQuery());
        String condition = ((RDBMSCompiledCondition) compiledCondition).getCompiledQuery();
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (RDBMSTableUtils.isEmpty(condition)) {
                stmt = conn.prepareStatement(containsQuery.replace(PLACEHOLDER_CONDITION, ""));
            } else {
                stmt = conn.prepareStatement(RDBMSTableUtils.formatQueryWithCondition(containsQuery, condition));
            }
            RDBMSTableUtils.resolveCondition(stmt, (RDBMSCompiledCondition) compiledCondition, containsConditionParameterMap, 0);
            rs = stmt.executeQuery();
            return !rs.isBeforeFirst();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error performing a contains check on table '" + this.tableName
                    + "': " + e.getMessage(), e);
        } finally {
            RDBMSTableUtils.cleanupConnection(rs, stmt, conn);
        }
    }

    @Override
    protected void delete(List<Map<String, Object>> deleteConditionParameterMaps, CompiledCondition compiledCondition) {
        String deleteQuery = this.queryConfigurationEntry.getRecordDeleteQuery();
        String condition = ((RDBMSCompiledCondition) compiledCondition).getCompiledQuery();
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        try {
            if (RDBMSTableUtils.isEmpty(condition)) {
                stmt = conn.prepareStatement(deleteQuery.replace(PLACEHOLDER_CONDITION, ""));
            } else {
                stmt = conn.prepareStatement(RDBMSTableUtils.formatQueryWithCondition(deleteQuery, condition));
            }
            for (Map<String, Object> deleteConditionParameterMap : deleteConditionParameterMaps) {
                RDBMSTableUtils.resolveCondition(stmt, (RDBMSCompiledCondition) compiledCondition, deleteConditionParameterMap, 0);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error performing a contains check on table '" + this.tableName
                    + "': " + e.getMessage(), e);
        } finally {
            RDBMSTableUtils.cleanupConnection(null, stmt, conn);
        }
    }

    @Override
    protected void update(List<Map<String, Object>> updateConditionParameterMaps, CompiledCondition compiledCondition,
                          List<Map<String, Object>> updateValues) {

        String sql = this.composeUpdateQuery(compiledCondition);
        try {
            this.batchProcessUpdateOrMerge(sql, updateConditionParameterMaps, compiledCondition, updateValues);
        } catch (SQLException e) {
            throw new RDBMSTableException("Error performing update operation on table '" + this.tableName
                    + "': " + e.getMessage(), e);
        }
    }

    private String composeUpdateQuery(CompiledCondition compiledCondition) {
        String sql = this.queryConfigurationEntry.getRecordUpdateQuery();
        String condition = ((RDBMSCompiledCondition) compiledCondition).getCompiledQuery();
        StringBuilder columnsValues = new StringBuilder();
        this.attributes.forEach(attribute -> {
            columnsValues.append(attribute.getName()).append(EQUALS).append(QUESTION_MARK);
            if (this.attributes.indexOf(attribute) != this.attributes.size() - 1) {
                columnsValues.append(SEPARATOR);
            }
        });
        sql = sql.replace(PLACEHOLDER_COLUMNS_VALUES, columnsValues.toString());
        if (RDBMSTableUtils.isEmpty(condition)) {
            sql = sql.replace(PLACEHOLDER_CONDITION, "");
        } else {
            sql = RDBMSTableUtils.formatQueryWithCondition(sql, condition);
        }

        return sql;
    }

    private void batchProcessUpdateOrMerge(String sql, List<Map<String, Object>> updateConditionParameterMaps,
                                           CompiledCondition compiledCondition,
                                           List<Map<String, Object>> updateValues) throws SQLException {
        final int seed = this.attributes.size();
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            Iterator<Map<String, Object>> conditionParamIterator = updateConditionParameterMaps.iterator();
            Iterator<Map<String, Object>> valueIterator = updateValues.iterator();
            while (conditionParamIterator.hasNext() && valueIterator.hasNext()) {
                Map<String, Object> conditionParameters = conditionParamIterator.next();
                Map<String, Object> values = valueIterator.next();
                //Incrementing the ordinals of the conditions in the statement with the # of variables to be updated
                RDBMSTableUtils.resolveCondition(stmt, (RDBMSCompiledCondition) compiledCondition, conditionParameters,
                        seed);
                for (Attribute attribute : this.attributes) {
                    RDBMSTableUtils.populateStatementWithSingleElement(stmt, this.attributes.indexOf(attribute) + 1,
                            attribute.getType(), values.get(attribute.getName()));
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
        } finally {
            RDBMSTableUtils.cleanupConnection(null, stmt, conn);
        }
    }

    @Override
    protected void updateOrAdd(List<Map<String, Object>> updateConditionParameterMaps,
                               CompiledCondition compiledCondition, List<Map<String, Object>> updateValues,
                               List<Object[]> addingRecords) {
        String mergeSQL = this.queryConfigurationEntry.getRecordMergeQuery();
        if (mergeSQL != null) {
            try {
                this.mergeRecords(mergeSQL, updateConditionParameterMaps, compiledCondition, updateValues);
            } catch (SQLException e) {
                //Merge operations have failed, maybe because one (or more) constraint violations.
                //Now, let us try to sequentially update/insert records.
                this.UpdateOrInsertRecords(updateConditionParameterMaps, compiledCondition, updateValues, addingRecords);
            }
        } else {
            this.UpdateOrInsertRecords(updateConditionParameterMaps, compiledCondition, updateValues, addingRecords);
        }
    }

    /**
     * Attempts to use SQL MERGE queries to try and insert/update records.
     * Can fail due to one or more constraint violations in the supplied list of records.
     *
     * @param updateConditionParameterMaps
     * @param compiledCondition
     * @param updateValues
     * @throws SQLException if one or more batch MERGE operations fail.
     */
    private void mergeRecords(String sql, List<Map<String, Object>> updateConditionParameterMaps,
                              CompiledCondition compiledCondition, List<Map<String, Object>> updateValues)
            throws SQLException {
        String mergeSQL = this.composeMergeQuery(sql, compiledCondition);
        this.batchProcessUpdateOrMerge(mergeSQL, updateConditionParameterMaps, compiledCondition, updateValues);
    }

    private void UpdateOrInsertRecords(List<Map<String, Object>> updateConditionParameterMaps,
                                       CompiledCondition compiledCondition, List<Map<String, Object>> updateValues,
                                       List<Object[]> addingRecords) {
        int counter = 0;
        final int seed = this.attributes.size();
        Connection conn = this.getConnection(false);
        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;
        try {
            //TODO discuss order. Try insert -> update or vice versa?
            updateStmt = conn.prepareStatement(this.composeUpdateQuery(compiledCondition));
            insertStmt = conn.prepareStatement(this.composeInsertQuery());
            while (counter < updateValues.size()) {
                Map<String, Object> conditionParameters = updateConditionParameterMaps.get(counter);
                Map<String, Object> values = updateValues.get(counter);
                //Incrementing the ordinals of the conditions in the statement with the # of variables to be updated
                RDBMSTableUtils.resolveCondition(updateStmt, (RDBMSCompiledCondition) compiledCondition,
                        conditionParameters, seed);
                for (Attribute attribute : this.attributes) {
                    RDBMSTableUtils.populateStatementWithSingleElement(updateStmt,
                            this.attributes.indexOf(attribute) + 1, attribute.getType(),
                            values.get(attribute.getName()));
                }
                // Using a sub try-block to ensure that only exceptions in performing UPDATE DB operations are caught.
                // Other exceptions point to a larger problem and must be handled separately outside.
                try {
                    updateStmt.executeUpdate();
                    conn.commit();
                    counter++;
                } catch (SQLException e) {
                    RDBMSTableUtils.rollbackConnection(conn);
                    Object[] record = addingRecords.get(counter);
                    try {
                        this.populateStatement(record, insertStmt);
                        insertStmt.executeUpdate();
                        conn.commit();
                        counter++;
                    } catch (SQLException e2) {
                        //TODO log warn?
                        RDBMSTableUtils.rollbackConnection(conn);
                        throw new RDBMSTableException("Error performing update/insert operation (insert) on table '"
                                + this.tableName + "': " + e.getMessage(), e);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RDBMSTableException("Error performing update/insert operation (update) on table '" + this.tableName
                    + "': " + e.getMessage(), e);
        } finally {
            RDBMSTableUtils.cleanupConnection(null, updateStmt, null);
            RDBMSTableUtils.cleanupConnection(null, insertStmt, conn);
        }
    }

    private String composeMergeQuery(String sql, CompiledCondition compiledCondition) {
        //TODO
        return null;
    }

    @Override
    protected CompiledCondition compileCondition(ConditionBuilder conditionBuilder) {
        //TODO verify
        RDBMSConditionVisitor visitor = new RDBMSConditionVisitor(this.queryConfigurationEntry);
        conditionBuilder.build(visitor);
        return new RDBMSCompiledCondition(this.tableName, this.resolveTableName(visitor.returnCondition()),
                visitor.getParameters());
    }

    private void lookupDatasource(String resourceName) throws NamingException {
        this.dataSource = InitialContext.doLookup(resourceName);
    }

    private void initializeDatasource(Annotation storeAnnotation) {
        Properties connectionProperties = new Properties();
        String poolPropertyString = storeAnnotation.getElement(
                ANNOTATION_ELEMENT_POOL_PROPERTIES);
        connectionProperties.setProperty("jdbcUrl", storeAnnotation.getElement(
                ANNOTATION_ELEMENT_URL));
        connectionProperties.setProperty("dataSource.user", storeAnnotation.getElement(
                ANNOTATION_ELEMENT_USERNAME));
        connectionProperties.setProperty("dataSource.password", storeAnnotation.getElement(
                ANNOTATION_ELEMENT_PASSWORD));
        if (poolPropertyString != null) {
            List<String[]> poolProps = RDBMSTableUtils.processKeyValuePairs(poolPropertyString);
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

    private String resolveTableName(String statement) {
        if (statement == null) {
            return null;
        }
        return statement.replace(TABLE_NAME_PLACEHOLDER, this.tableName);
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
        Map<String, String> fieldLengths = RDBMSTableUtils.processFieldLengths(storeAnnotation.getElement(
                ANNOTATION_ELEMENT_FIELD_LENGTHS));
        this.attributes.forEach(attribute -> {
            builder.append(attribute.getName()).append(WHITESPACE);
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
                    if (this.queryConfigurationEntry.getStringSize() != null) {
                        builder.append(OPEN_PARENTHESIS);
                        if (fieldLengths.containsKey(attribute.getName().toLowerCase())) {
                            builder.append(fieldLengths.get(attribute.getName()));
                        } else {
                            builder.append(this.queryConfigurationEntry.getStringSize());
                        }
                        builder.append(CLOSE_PARENTHESIS);
                    }
                    break;
            }
            if (this.attributes.indexOf(attribute) != this.attributes.size() - 1 || primaryKeyMap != null) {
                builder.append(SEPARATOR);
            }
            if (primaryKeyMap != null) {
                builder.append(PRIMARY_KEY_DEF)
                        .append(OPEN_PARENTHESIS)
                        .append(RDBMSTableUtils.flattenAnnotatedElements(primaryKeyMap))
                        .append(CLOSE_PARENTHESIS);
            }
        });
        queries.add(createQuery.replace(PLACEHOLDER_COLUMNS, builder.toString()));
        if (indexMap != null && !indexMap.isEmpty()) {
            queries.add(indexQuery.replace(INDEX_PLACEHOLDER, RDBMSTableUtils.flattenAnnotatedElements(indexMap)));
        }
        try {
            this.executeDDQueries(queries, false);
        } catch (SQLException e) {
            throw new RDBMSTableException("Unable to initialize table '" + this.tableName + "': " + e.getMessage(), e);
        }
    }

    private void executeDDQueries(List<String> queries, boolean autocommit) throws SQLException {
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

    private void batchExecuteQueriesWithRecords(String query, List<Object[]> records, boolean autocommit)
            throws SQLException {
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
            //TODO log the error
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
            //TODO log the error
            RDBMSTableUtils.rollbackConnection(conn);
            return false;
        } finally {
            RDBMSTableUtils.cleanupConnection(rs, stmt, conn);
        }
    }

    private void populateStatement(Object[] record, PreparedStatement stmt) {
        Attribute attribute = null;
        try {
            for (int i = 0; i < this.attributes.size(); i++) {
                attribute = this.attributes.get(i);
                Object value = record[i];
                if (value != null || attribute.getType() == Attribute.Type.STRING) {
                    RDBMSTableUtils.populateStatementWithSingleElement(stmt, i + 1, attribute.getType(), value);
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

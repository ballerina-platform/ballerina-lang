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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.exception.CannotLoadConfigurationException;
import org.wso2.siddhi.core.table.record.AbstractRecordTable;
import org.wso2.siddhi.core.table.record.ExpressionBuilder;
import org.wso2.siddhi.core.table.record.RecordIterator;
import org.wso2.siddhi.core.table.record.RecordTableCompiledUpdateSet;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.operator.CompiledExpression;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.extension.table.rdbms.config.RDBMSQueryConfigurationEntry;
import org.wso2.siddhi.extension.table.rdbms.config.RDBMSTypeMapping;
import org.wso2.siddhi.extension.table.rdbms.exception.RDBMSTableException;
import org.wso2.siddhi.extension.table.rdbms.util.Constant;
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
import java.util.stream.Collectors;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import static org.wso2.siddhi.core.util.SiddhiConstants.ANNOTATION_STORE;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.ANNOTATION_ELEMENT_FIELD_LENGTHS;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.ANNOTATION_ELEMENT_JNDI_RESOURCE;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.ANNOTATION_ELEMENT_PASSWORD;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.ANNOTATION_ELEMENT_POOL_PROPERTIES;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.ANNOTATION_ELEMENT_TABLE_NAME;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.ANNOTATION_ELEMENT_URL;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.ANNOTATION_ELEMENT_USERNAME;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.CLOSE_PARENTHESIS;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.OPEN_PARENTHESIS;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.PLACEHOLDER_COLUMNS;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.PLACEHOLDER_COLUMNS_VALUES;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.PLACEHOLDER_CONDITION;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.PLACEHOLDER_INDEX;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.PLACEHOLDER_Q;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.PLACEHOLDER_TABLE_NAME;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.QUESTION_MARK;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.SEPARATOR;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.SQL_PRIMARY_KEY_DEF;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.WHITESPACE;

/**
 * Class representing the RDBMS Event Table implementation.
 */
@Extension(
        name = "rdbms",
        namespace = "store",
        description = "Using this extension the data source or the connection instructions can be " +
                "assigned to the event table.",
        parameters = {
                @Parameter(name = "jdbc.url",
                        description = "The JDBC URL for the RDBMS data store.",
                        type = {DataType.STRING}),
                @Parameter(name = "username",
                        description = "The username for accessing the data store.",
                        type = {DataType.STRING}),
                @Parameter(name = "password",
                        description = "The password for accessing the data store.",
                        type = {DataType.STRING}),
                @Parameter(name = "pool.properties",
                        description = "Any pool properties for the DB connection, specified as key-value pairs.",
                        type = {DataType.STRING}),
                @Parameter(name = "jndi.resource",
                        description = "Optional. The name of the JNDI resource (if any). If found, the connection " +
                                "parameters are not taken into account, and the connection will be attempted through "
                                + "JNDI lookup instead.",
                        type = {DataType.STRING}),
                @Parameter(name = "table.name",
                        description = "Optional. The name of the table in the store this Event Table should be " +
                                "persisted as. If not specified, the table name will be the same as the Siddhi table.",
                        type = {DataType.STRING}),
                @Parameter(name = "field.length",
                        description = "Optional. The length of any String fields the table definition contains. If not "
                                + "specified, the vendor-specific DB default will be chosen.",
                        type = {DataType.STRING}),
        },
        examples = {
                @Example(
                        syntax = "@Store(type=\"rdbms\", jdbc.url=\"jdbc:mysql://localhost:3306/das\", " +
                                "username=\"root\", password=\"root\",field.length=\"symbol:100\")\n" +
                                "@PrimaryKey(\"symbol\")" +
                                "@Index(\"volume\")" +
                                "define table StockTable (symbol string, price float, volume long);",
                        description = "The above example will create an Event Table named 'StockTable' on the DB if " +
                                "it doesn't already exist (with 3 fields 'symbol', 'price', and 'volume' with types" +
                                "string, float and long respectively). The connection parameters will be as" +
                                " specified in the '@Store' annotation. The 'symbol' field will be declared a unique " +
                                "field, and a DB index will be created for the 'symbol' field."
                )
        }
)
public class RDBMSEventTable extends AbstractRecordTable {

    private static final Log log = LogFactory.getLog(RDBMSEventTable.class);
    private RDBMSQueryConfigurationEntry queryConfigurationEntry;
    private DataSource dataSource;
    private String tableName;
    private List<Attribute> attributes;

    @Override
    protected void init(TableDefinition tableDefinition, ConfigReader configReader) {
        this.attributes = tableDefinition.getAttributeList();
        Annotation storeAnnotation = AnnotationHelper.getAnnotation(ANNOTATION_STORE, tableDefinition.getAnnotations());
        Annotation primaryKeys = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_PRIMARY_KEY,
                tableDefinition.getAnnotations());
        Annotation indices = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_INDEX,
                tableDefinition.getAnnotations());
        RDBMSTableUtils.validateAnnotation(primaryKeys);
        RDBMSTableUtils.validateAnnotation(indices);
        String jndiResourceName = storeAnnotation.getElement(ANNOTATION_ELEMENT_JNDI_RESOURCE);
        if (!RDBMSTableUtils.isEmpty(jndiResourceName)) {
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
        this.tableName = RDBMSTableUtils.isEmpty(tableName) ? tableDefinition.getId() : tableName;
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

    @Override
    protected RecordIterator<Object[]> find(Map<String, Object> findConditionParameterMap,
                                            CompiledExpression compiledExpression) {
        String selectQuery = this.resolveTableName(this.queryConfigurationEntry.getRecordSelectQuery());
        String condition = ((RDBMSCompiledExpression) compiledExpression).getCompiledQuery();
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs;
        try {
            stmt = RDBMSTableUtils.isEmpty(condition) ?
                    conn.prepareStatement(selectQuery.replace(PLACEHOLDER_CONDITION, "")) :
                    conn.prepareStatement(RDBMSTableUtils.formatQueryWithCondition(selectQuery, condition));
            RDBMSTableUtils.resolveCondition(stmt, (RDBMSCompiledExpression) compiledExpression,
                    findConditionParameterMap, 0);
            rs = stmt.executeQuery();
            //Passing all java.sql artifacts to the iterator to ensure everything gets cleaned up at once.
            return new RDBMSIterator(conn, stmt, rs, this.attributes, this.tableName);
        } catch (SQLException e) {
            RDBMSTableUtils.cleanupConnection(null, stmt, conn);
            throw new RDBMSTableException("Error retrieving records from table '" + this.tableName + "': "
                    + e.getMessage(), e);
        }
    }

    @Override
    protected boolean contains(Map<String, Object> containsConditionParameterMap, CompiledExpression compiledExpression) {
        String containsQuery = this.resolveTableName(this.queryConfigurationEntry.getRecordExistsQuery());
        String condition = ((RDBMSCompiledExpression) compiledExpression).getCompiledQuery();
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = RDBMSTableUtils.isEmpty(condition) ?
                    conn.prepareStatement(containsQuery.replace(PLACEHOLDER_CONDITION, "")) :
                    conn.prepareStatement(RDBMSTableUtils.formatQueryWithCondition(containsQuery, condition));
            RDBMSTableUtils.resolveCondition(stmt, (RDBMSCompiledExpression) compiledExpression,
                    containsConditionParameterMap, 0);
            rs = stmt.executeQuery();
            return rs.next() && !rs.isBeforeFirst();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error performing a contains check on table '" + this.tableName
                    + "': " + e.getMessage(), e);
        } finally {
            RDBMSTableUtils.cleanupConnection(rs, stmt, conn);
        }
    }

    @Override
    protected void delete(List<Map<String, Object>> deleteConditionParameterMaps, CompiledExpression compiledExpression) {
        String deleteQuery = this.resolveTableName(this.queryConfigurationEntry.getRecordDeleteQuery());
        String condition = ((RDBMSCompiledExpression) compiledExpression).getCompiledQuery();
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = RDBMSTableUtils.isEmpty(condition) ?
                    conn.prepareStatement(deleteQuery.replace(PLACEHOLDER_CONDITION, "")) :
                    conn.prepareStatement(RDBMSTableUtils.formatQueryWithCondition(deleteQuery, condition));
            for (Map<String, Object> deleteConditionParameterMap : deleteConditionParameterMaps) {
                RDBMSTableUtils.resolveCondition(stmt, (RDBMSCompiledExpression) compiledExpression,
                        deleteConditionParameterMap, 0);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RDBMSTableException("Error performing record deletion on table '" + this.tableName
                    + "': " + e.getMessage(), e);
        } finally {
            RDBMSTableUtils.cleanupConnection(null, stmt, conn);
        }
    }

    @Override
    protected void update(List<Map<String, Object>> updateConditionParameterMaps, CompiledExpression compiledExpression,
                          RecordTableCompiledUpdateSet recordTableCompiledUpdateSet, List<Map<String, Object>> updateValues) {
        String sql = this.composeUpdateQuery(compiledExpression, recordTableCompiledUpdateSet);
        try {
            this.batchProcessUpdates(sql, updateConditionParameterMaps, compiledExpression,
                    recordTableCompiledUpdateSet, updateValues);
        } catch (SQLException e) {
            throw new RDBMSTableException("Error performing record update operations on table '" + this.tableName
                    + "': " + e.getMessage(), e);
        }
    }

    @Override
    protected void updateOrAdd(List<Map<String, Object>> updateConditionParameterMaps,
                               CompiledExpression compiledExpression, RecordTableCompiledUpdateSet recordTableCompiledUpdateSet,
                               List<Map<String, Object>> updateSetParameterMaps,
                               List<Object[]> addingRecords) {
        this.updateOrInsertRecords(updateConditionParameterMaps, compiledExpression, recordTableCompiledUpdateSet,
                updateSetParameterMaps, addingRecords);
    }

    @Override
    protected CompiledExpression compileExpression(ExpressionBuilder expressionBuilder) {
        RDBMSConditionVisitor visitor = new RDBMSConditionVisitor(this.tableName);
        expressionBuilder.build(visitor);
        return new RDBMSCompiledExpression(visitor.returnCondition(), visitor.getParameters());
    }

    @Override
    protected CompiledExpression compileSetAttribute(ExpressionBuilder expressionBuilder) {
        return compileExpression(expressionBuilder);
    }

    /**
     * Method for looking up a datasource instance through JNDI.
     *
     * @param resourceName the name of the resource to be looked up.
     * @throws NamingException if the lookup fails.
     */
    private void lookupDatasource(String resourceName) throws NamingException {
        this.dataSource = InitialContext.doLookup(resourceName);
    }

    /**
     * Method for composing the SQL query for INSERT operations with proper placeholders.
     *
     * @return the composed SQL query in string form.
     */
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
        return insertQuery.replace(PLACEHOLDER_Q, params.toString());
    }

    /**
     * Method for composing the SQL query for UPDATE operations with proper placeholders.
     *
     * @return the composed SQL query in string form.
     */
    private String composeUpdateQuery(CompiledExpression compiledExpression, RecordTableCompiledUpdateSet recordTableCompiledUpdateSet) {
        String sql = this.resolveTableName(this.queryConfigurationEntry.getRecordUpdateQuery());
        String condition = ((RDBMSCompiledExpression) compiledExpression).getCompiledQuery();
        String result = recordTableCompiledUpdateSet.getUpdateSetMap().entrySet().stream().map(e -> e.getKey()
                + " = " + ((RDBMSCompiledExpression) e.getValue()).getCompiledQuery())
                .collect(Collectors.joining(", "));
        sql = sql.replace(PLACEHOLDER_COLUMNS_VALUES, result);

        sql = RDBMSTableUtils.isEmpty(condition) ? sql.replace(PLACEHOLDER_CONDITION, "") :
                RDBMSTableUtils.formatQueryWithCondition(sql, condition);
        return sql;
    }

    /**
     * Method for processing update operations in a batched manner. This assumes that all update operations will be
     * accepted by the database.
     *
     * @param sql                          the SQL update operation as string.
     * @param updateConditionParameterMaps the runtime parameters that should be populated to the condition.
     * @param compiledExpression            the condition that was built during compile time.
     * @param updateSetParameterMaps                 the runtime parameters that should be populated to the update statement.
     * @throws SQLException if the update operation fails.
     */
    private void batchProcessUpdates(String sql, List<Map<String, Object>> updateConditionParameterMaps,
                                     CompiledExpression compiledExpression,
                                     RecordTableCompiledUpdateSet recordTableCompiledUpdateSet,
                                     List<Map<String, Object>> updateSetParameterMaps) throws SQLException {
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            Iterator<Map<String, Object>> conditionParamIterator = updateConditionParameterMaps.iterator();
            Iterator<Map<String, Object>> valueIterator = updateSetParameterMaps.iterator();
            while (conditionParamIterator.hasNext() && valueIterator.hasNext()) {
                Map<String, Object> conditionParameters = conditionParamIterator.next();
                Map<String, Object> values = valueIterator.next();

                int ordinal = 1;
                for (Map.Entry<String, CompiledExpression> assignmentEntry :
                        recordTableCompiledUpdateSet.getUpdateSetMap().entrySet()) {
                    for (Map.Entry<Integer, Object> parameterEntry :
                            ((RDBMSCompiledExpression) assignmentEntry.getValue()).getParameters().entrySet()) {
                        Object parameter = parameterEntry.getValue();
                        if (parameter instanceof Constant) {
                            Constant constant = (Constant) parameter;
                            RDBMSTableUtils.populateStatementWithSingleElement(stmt, ordinal, constant.getType(),
                                    constant.getValue());
                        } else {
                            Attribute variable = (Attribute) parameter;
                            RDBMSTableUtils.populateStatementWithSingleElement(stmt, ordinal, variable.getType(),
                                    values.get(variable.getName()));
                        }
                        ordinal++;
                    }
                }

                //Incrementing the ordinals of the conditions in the statement with the # of variables to be updated
                RDBMSTableUtils.resolveCondition(stmt, (RDBMSCompiledExpression) compiledExpression, conditionParameters,
                        ordinal - 1);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } finally {
            RDBMSTableUtils.cleanupConnection(null, stmt, conn);
        }
    }

    /**
     * Method for performing insert/update operations for a given dataset.
     *
     * @param updateConditionParameterMaps update parameters that should be populated for each condition.
     * @param compiledExpression            the condition that was built during compile time.
     * @param recordTableCompiledUpdateSet
     * @param updateSetParameterMaps       the values for which the update operation should be done
     *                                     (i.e. the new values).
     * @param addingRecords                the records that should be inserted to the DB should the update operation
     */
    private void updateOrInsertRecords(List<Map<String, Object>> updateConditionParameterMaps,
                                       CompiledExpression compiledExpression,
                                       RecordTableCompiledUpdateSet recordTableCompiledUpdateSet, List<Map<String, Object>> updateSetParameterMaps,
                                       List<Object[]> addingRecords) {
        int counter = 0;
        Connection conn = this.getConnection(false);
        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;
        try {
            updateStmt = conn.prepareStatement(this.composeUpdateQuery(compiledExpression, recordTableCompiledUpdateSet));
            insertStmt = conn.prepareStatement(this.composeInsertQuery());
            while (counter < updateSetParameterMaps.size()) {
                int rowsChanged;
                //'values' are used for replacing ?s in the SET clause
                Map<String, Object> values = updateSetParameterMaps.get(counter);
                //Incrementing the ordinals of the conditions in the statement with the # of variables to be updated
                int ordinal = 0;
                for (Map.Entry<String, CompiledExpression> assignmentEntry :
                        recordTableCompiledUpdateSet.getUpdateSetMap().entrySet()) {
                    for (Map.Entry<Integer, Object> parameterEntry :
                            ((RDBMSCompiledExpression) assignmentEntry.getValue()).getParameters().entrySet()) {
                        Object parameter = parameterEntry.getValue();
                        if (parameter instanceof Constant) {
                            Constant constant = (Constant) parameter;
                            RDBMSTableUtils.populateStatementWithSingleElement(updateStmt,
                                    parameterEntry.getKey() + ordinal, constant.getType(),
                                    constant.getValue());
                        } else {
                            Attribute variable = (Attribute) parameter;
                            RDBMSTableUtils.populateStatementWithSingleElement(updateStmt,
                                    parameterEntry.getKey() + ordinal, variable.getType(),
                                    values.get(variable.getName()));
                        }
                    }
                    ordinal += ((RDBMSCompiledExpression) assignmentEntry.getValue()).getParameters().size();
                }
                Map<String, Object> conditionParameters = updateConditionParameterMaps.get(counter);
                RDBMSTableUtils.resolveCondition(updateStmt, (RDBMSCompiledExpression) compiledExpression,
                        conditionParameters, ordinal);

                rowsChanged = updateStmt.executeUpdate();
                conn.commit();
                if (rowsChanged < 1) {
                    Object[] record = addingRecords.get(counter);
                    try {
                        this.populateStatement(record, insertStmt);
                        insertStmt.executeUpdate();
                        conn.commit();
                    } catch (SQLException e2) {
                        RDBMSTableUtils.rollbackConnection(conn);
                        throw new RDBMSTableException("Error performing update/insert operation (insert) on table '"
                                + this.tableName + "': " + e2.getMessage(), e2);
                    }
                }
                counter++;
            }
        } catch (SQLException e) {
            throw new RDBMSTableException("Error performing update/insert operation (update) on table '"
                    + this.tableName
                    + "': " + e.getMessage(), e);
        } finally {
            RDBMSTableUtils.cleanupConnection(null, updateStmt, null);
            RDBMSTableUtils.cleanupConnection(null, insertStmt, conn);
        }
    }

    /**
     * Method for creating and initializing the datasource instance given the "@Store" annotation.
     *
     * @param storeAnnotation the source annotation which contains the needed parameters.
     */
    private void initializeDatasource(Annotation storeAnnotation) {
        Properties connectionProperties = new Properties();
        String poolPropertyString = storeAnnotation.getElement(ANNOTATION_ELEMENT_POOL_PROPERTIES);
        String url = storeAnnotation.getElement(ANNOTATION_ELEMENT_URL);
        String username = storeAnnotation.getElement(ANNOTATION_ELEMENT_USERNAME);
        String password = storeAnnotation.getElement(ANNOTATION_ELEMENT_PASSWORD);
        if (RDBMSTableUtils.isEmpty(url)) {
            throw new RDBMSTableException("Required parameter '" + ANNOTATION_ELEMENT_URL + "' for DB " +
                    "connectivity cannot be empty.");
        }
        if (RDBMSTableUtils.isEmpty(username)) {
            throw new RDBMSTableException("Required parameter '" + ANNOTATION_ELEMENT_USERNAME + "' for DB " +
                    "connectivity cannot be empty.");
        }
        if (RDBMSTableUtils.isEmpty(password)) {
            throw new RDBMSTableException("Required parameter '" + ANNOTATION_ELEMENT_PASSWORD + "' for DB " +
                    "connectivity cannot be empty.");
        }
        connectionProperties.setProperty("jdbcUrl", url);
        connectionProperties.setProperty("dataSource.user", username);
        connectionProperties.setProperty("dataSource.password", password);
        if (poolPropertyString != null) {
            List<String[]> poolProps = RDBMSTableUtils.processKeyValuePairs(poolPropertyString);
            poolProps.forEach(pair -> connectionProperties.setProperty(pair[0], pair[1]));
        }
        HikariConfig config = new HikariConfig(connectionProperties);
        this.dataSource = new HikariDataSource(config);
    }

    /**
     * Returns a connection instance assuming that autocommit should be true.
     *
     * @return a new {@link Connection} instance from the datasource.
     */
    private Connection getConnection() {
        return this.getConnection(true);
    }

    /**
     * Returns a connection instance.
     *
     * @param autoCommit whether or not transactions to the connections should be committed automatically.
     * @return a new {@link Connection} instance from the datasource.
     */
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

    /**
     * Method for replacing the placeholder for the table name with the Event Table's name.
     *
     * @param statement the SQL statement in string form.
     * @return the formatted SQL statement.
     */
    private String resolveTableName(String statement) {
        if (statement == null) {
            return null;
        }
        return statement.replace(PLACEHOLDER_TABLE_NAME, this.tableName);
    }

    /**
     * Method for creating a table on the data store in question, if it does not exist already.
     *
     * @param storeAnnotation the "@Store" annotation that contains the connection properties.
     * @param primaryKeys     the unique keys that should be set for the table.
     * @param indices         the DB indices that should be set for the table.
     */
    private void createTable(Annotation storeAnnotation, Annotation primaryKeys, Annotation indices) {
        RDBMSTypeMapping typeMapping = this.queryConfigurationEntry.getRdbmsTypeMapping();
        StringBuilder builder = new StringBuilder();
        List<Element> primaryKeyList = (primaryKeys == null) ? new ArrayList<>() : primaryKeys.getElements();
        List<Element> indexElementList = (indices == null) ? new ArrayList<>() : indices.getElements();
        List<String> queries = new ArrayList<>();
        String createQuery = this.resolveTableName(this.queryConfigurationEntry.getTableCreateQuery());
        String indexQuery = this.resolveTableName(this.queryConfigurationEntry.getIndexCreateQuery());
        Map<String, String> fieldLengths = RDBMSTableUtils.processFieldLengths(storeAnnotation.getElement(
                ANNOTATION_ELEMENT_FIELD_LENGTHS));
        this.validateFieldLengths(fieldLengths);
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
                        if (fieldLengths.containsKey(attribute.getName())) {
                            builder.append(fieldLengths.get(attribute.getName()));
                        } else {
                            builder.append(this.queryConfigurationEntry.getStringSize());
                        }
                        builder.append(CLOSE_PARENTHESIS);
                    }
                    break;
            }
            if (this.attributes.indexOf(attribute) != this.attributes.size() - 1 || !primaryKeyList.isEmpty()) {
                builder.append(SEPARATOR);
            }
        });
        if (primaryKeyList != null && !primaryKeyList.isEmpty()) {
            builder.append(SQL_PRIMARY_KEY_DEF)
                    .append(OPEN_PARENTHESIS)
                    .append(RDBMSTableUtils.flattenAnnotatedElements(primaryKeyList))
                    .append(CLOSE_PARENTHESIS);
        }
        queries.add(createQuery.replace(PLACEHOLDER_COLUMNS, builder.toString()));
        if (indexElementList != null && !indexElementList.isEmpty()) {
            queries.add(indexQuery.replace(PLACEHOLDER_INDEX,
                    RDBMSTableUtils.flattenAnnotatedElements(indexElementList)));
        }
        try {
            this.executeDDQueries(queries, false);
        } catch (SQLException e) {
            throw new RDBMSTableException("Unable to initialize table '" + this.tableName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Method used to validate the field length specifications and ensure that the table definition contains them.
     *
     * @param fieldLengths the specified list of custom string field lengths.
     */
    private void validateFieldLengths(Map<String, String> fieldLengths) {
        List<String> attributeNames = new ArrayList<>();
        this.attributes.forEach(attribute -> attributeNames.add(attribute.getName()));
        fieldLengths.keySet().forEach(field -> {
            if (!attributeNames.contains(field)) {
                throw new RDBMSTableException("Field '" + field + "' (for which a size of " + fieldLengths.get(field)
                        + " has been specified) does not exist in the table's list of fields.");
            }
        });
    }

    /**
     * Method for performing data definition queries for the current datasource.
     *
     * @param queries    the list of queries to be executed.
     * @param autocommit whether or not the transactions should automatically be committed.
     * @throws SQLException if the query execution fails.
     */
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
            if (!autocommit) {
                RDBMSTableUtils.rollbackConnection(conn);
            }
            throw e;
        } finally {
            if (!committed) {
                RDBMSTableUtils.rollbackConnection(conn);
            }
            RDBMSTableUtils.cleanupConnection(null, null, conn);
        }
    }

    /**
     * Given a set of records and a query, this method performs that query per each record.
     *
     * @param query      the query to be executed.
     * @param records    the records to use.
     * @param autocommit whether or not the transactions should automatically be committed.
     * @throws SQLException if the query execution fails.
     */
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
            if (log.isDebugEnabled()) {
                log.debug("Attempted execution of query [" + query + "] produced an exception: " + e.getMessage());
            }
            if (!autocommit) {
                RDBMSTableUtils.rollbackConnection(conn);
            }
            throw e;
        } finally {
            if (!committed) {
                RDBMSTableUtils.rollbackConnection(conn);
            }
            RDBMSTableUtils.cleanupConnection(null, stmt, conn);
        }
    }

    /**
     * Method for checking whether or not the given table (which reflects the current event table instance) exists.
     *
     * @return true/false based on the table existence.
     */
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
            if (log.isDebugEnabled()) {
                log.debug("Table '" + this.tableName + "' assumed to not exist since its existence check resulted "
                        + "in exception " + e.getMessage());
            }
            return false;
        } finally {
            RDBMSTableUtils.cleanupConnection(rs, stmt, conn);
        }
    }

    /**
     * Method for populating values to a pre-created SQL prepared statement.
     *
     * @param record the record whose values should be populated.
     * @param stmt   the statement to which the values should be set.
     */
    private void populateStatement(Object[] record, PreparedStatement stmt) {
        Attribute attribute = null;
        try {
            for (int i = 0; i < this.attributes.size(); i++) {
                attribute = this.attributes.get(i);
                Object value = record[i];
                if (value != null || attribute.getType() == Attribute.Type.STRING) {
                    RDBMSTableUtils.populateStatementWithSingleElement(stmt, i + 1, attribute.getType(), value);
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

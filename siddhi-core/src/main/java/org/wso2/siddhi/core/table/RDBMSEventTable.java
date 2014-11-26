/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.table;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StateEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InStateEvent;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.core.table.cache.CachingTable;
import org.wso2.siddhi.core.table.predicate.PredicateTreeNode;
import org.wso2.siddhi.core.table.predicate.sql.SQLPredicateBuilder;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RDBMSEventTable implements EventTable {
    static final String PARAM_TABLE_NAME = "table.name";
    static final String PARAM_DATASOURCE_NAME = "datasource.name";
    static final String PARAM_CREATE_QUERY = "create.query";
    static final String PARAM_CACHING_ALGORITHM = "caching.algorithm";
    static final String PARAM_CACHE_SIZE = "cache.size";
    static final String PARAM_CACHE_LOADING = "cache.loading";
    static final String PARAM_BLOOM_FILTERS = "bloom.filters";

    static final Logger log = Logger.getLogger(RDBMSEventTable.class);

    private TableDefinition tableDefinition;
    private QueryEventSource queryEventSource;
    // attribute list used for accessing the table.
    private List<Attribute> attributeList;
    private boolean eagerCacheLoading;

    private DataSource dataSource;
    private String databaseName;
    private String tableName;
    private String fullTableName;  // schema.tableName
    private String tableColumnList;   // for insertion queries.

    private boolean isInitialized;

    private String insertQuery;
    private boolean bloomFiltersEnabled;

    private CachingTable cachedTable;
    private BloomFilter[] bloomFilters;

    public RDBMSEventTable(TableDefinition tableDefinition, SiddhiContext siddhiContext) {
        this.tableDefinition = tableDefinition;
        this.queryEventSource = new QueryEventSource(tableDefinition.getExternalTable().getParameter(PARAM_TABLE_NAME), tableDefinition.getTableId(), tableDefinition, null, null, null);
        this.dataSource = siddhiContext.getDataSource(tableDefinition.getExternalTable().getParameter(PARAM_DATASOURCE_NAME));
        this.attributeList = new ArrayList<Attribute>();
        if ((tableDefinition.getExternalTable().getParameter(PARAM_CACHING_ALGORITHM) != null) && (!tableDefinition.getExternalTable().getParameter(PARAM_CACHING_ALGORITHM).equalsIgnoreCase("disable"))) {
            this.cachedTable = new CachingTable(tableDefinition.getTableId(), tableDefinition.getExternalTable().getParameter(PARAM_CACHING_ALGORITHM), tableDefinition.getExternalTable().getParameter(PARAM_CACHE_SIZE), siddhiContext);
        }
        if (cachedTable != null && (tableDefinition.getExternalTable().getParameter(PARAM_CACHE_LOADING) != null) && (tableDefinition.getExternalTable().getParameter(PARAM_CACHE_LOADING).equalsIgnoreCase("eager"))) {
            this.eagerCacheLoading = true;
        }

        if ((tableDefinition.getExternalTable().getParameter(PARAM_BLOOM_FILTERS) != null) && (tableDefinition.getExternalTable().getParameter(PARAM_BLOOM_FILTERS).equalsIgnoreCase("enable"))) {
            this.bloomFiltersEnabled = true;
        }

        try {
            initializeConnection();
            createPreparedStatementQueries();
            if (eagerCacheLoading) {
                preloadCache();
            }
            if (bloomFiltersEnabled) {
                buildBloomFilters();
            }

        } catch (Exception e) {
            log.error("Unable to connect to the database.", e);
        }
    }

    private synchronized void initializeConnection() throws SQLException, ClassNotFoundException {
        if (!isInitialized) {
            Connection con = null;
            Statement statement = null;
            try {
                tableName = tableDefinition.getExternalTable().getParameter(PARAM_TABLE_NAME);

                if (dataSource == null) {
                    throw new RuntimeException("Data source doesn't exist: " + tableDefinition.getExternalTable().getParameter(PARAM_DATASOURCE_NAME));
                }
                con = dataSource.getConnection();

                // default mysql jdbc driver
                databaseName = con.getCatalog();
                fullTableName = databaseName + "." + tableName;

                statement = con.createStatement();


                String createQuery = tableDefinition.getExternalTable().getParameter(PARAM_CREATE_QUERY);

                // table creation.
                if (createQuery == null || createQuery.length() < 1) {
                    StringBuilder stringBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
                    stringBuilder.append(fullTableName);
                    stringBuilder.append(" (");
                    boolean appendComma = false;
                    for (Attribute column : tableDefinition.getAttributeList()) {
                        if (appendComma) {
                            stringBuilder.append(", ");
                        } else {
                            appendComma = true;
                        }
                        stringBuilder.append(column.getName());
                        stringBuilder.append("  ");
                        switch (column.getType()) {
                            case INT:
                                stringBuilder.append("INT");
                                break;
                            case LONG:
                                stringBuilder.append("BIGINT");
                                break;
                            case FLOAT:
                                stringBuilder.append("DECIMAL(30,10)");
                                break;
                            case DOUBLE:
                                stringBuilder.append("DECIMAL(40,15)");
                                break;
                            case BOOL:
                                stringBuilder.append("BOOL");
                                break;
                            default:
                                stringBuilder.append("VARCHAR(255)");
                                break;
                        }
                    }
                    stringBuilder.append(");");
                    createQuery = stringBuilder.toString();
                    statement.execute(createQuery);

                } else {
                    // users may not use 'IF NOT EXISTS' clause, so need to check whether the table exists before
                    // executing their create queries.
                    try {
                        statement.execute("SELECT 1 FROM " + fullTableName + " LIMIT 1");
                    } catch (SQLException e) {
                        statement.execute(createQuery);
                    }
                }

                StringBuilder builder = new StringBuilder("(");
                boolean appendComma = false;
                for (Attribute att : tableDefinition.getAttributeList()) {
                    attributeList.add(att);
                    if (appendComma) {
                        builder.append(",");
                    }
                    builder.append(att.getName());
                    appendComma = true;
                }
                builder.append(")");
                tableColumnList = builder.toString();
                isInitialized = true;
            } finally {
                cleanUpConnections(statement, con);
            }
        }
    }


    private synchronized void buildBloomFilters() {
        this.bloomFilters = new BloomFilter[tableDefinition.getAttributeList().size()];
        for (int i = 0; i < bloomFilters.length; i++) {
            bloomFilters[i] = BloomFilter.create(Funnels.byteArrayFunnel(), 100000);
        }
        Connection con = null;
        Statement stmt = null;
        try {
            con = dataSource.getConnection();
            stmt = con.createStatement();
            ResultSet results = stmt.executeQuery("SELECT * FROM " + fullTableName);
            int count = 0;
            while (results.next()) {
                count++;
                for (int i = 0; i < bloomFilters.length; i++) {
                    switch (tableDefinition.getAttributeList().get(i).getType()) {
                        case INT:
                            bloomFilters[i].put(Integer.toString(results.getInt(i + 1)).getBytes());
                            break;
                        case LONG:
                            bloomFilters[i].put(Long.toString(results.getLong(i + 1)).getBytes());
                            break;
                        case FLOAT:
                            bloomFilters[i].put(Float.toString(results.getFloat(i + 1)).getBytes());
                            break;
                        case DOUBLE:
                            bloomFilters[i].put(Double.toString(results.getDouble(i + 1)).getBytes());
                            break;
                        case STRING:
                            bloomFilters[i].put(results.getString(i + 1).getBytes());
                            break;
                        case BOOL:
                            bloomFilters[i].put(Boolean.toString(results.getBoolean(i + 1)).getBytes());
                            break;

                    }
                }
            }
            results.close();

        } catch (Exception ex) {
            log.error(ex);
        } finally {
            cleanUpConnections(stmt, con);
        }
    }


    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public void add(StreamEvent streamEvent) {

        Connection con = null;
        PreparedStatement statement = null;
        try {
            initializeConnection();
            con = dataSource.getConnection();
            statement = con.prepareStatement(insertQuery);

            if (streamEvent instanceof AtomicEvent) {
                populateInsertQuery((Event) streamEvent, statement);
                statement.executeUpdate();
            } else {
                ListEvent listEvent = ((ListEvent) streamEvent);
                for (int i = 0, size = listEvent.getActiveEvents(); i < size; i++) {
                    populateInsertQuery(listEvent.getEvent(i), statement);
                    statement.addBatch();
                }
                statement.executeBatch();
            }

            if (cachedTable != null) {
                cachedTable.add(streamEvent);
            }
            if (bloomFiltersEnabled) {
                buildBloomFilters();
            }

        } catch (ClassNotFoundException e) {
            log.error("Couldn't load the database driver", e);
        } catch (SQLException e) {
            log.error("Unable to insert the records to the table", e);
        } finally {
            cleanUpConnections(statement, con);
        }
    }

    private void preloadCache() {
        Connection con = null;
        Statement statement = null;
        try {
            con = dataSource.getConnection();
            statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + fullTableName +
                                                         " LIMIT 0, " + cachedTable.getCacheLimit());
            resultSet.setFetchSize(cachedTable.getCacheLimit());
            List<StreamEvent> eventList = new ArrayList<StreamEvent>();
            long timestamp = System.currentTimeMillis();
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
                Event event = new InEvent(tableDefinition.getExternalTable().getParameter(PARAM_TABLE_NAME), timestamp, data);
                eventList.add(event);
            }
            if (cachedTable != null) {
                cachedTable.addAll(eventList);
                ResultSet resultCount = statement.executeQuery("SELECT COUNT(*) FROM " + fullTableName);
                int rowCount = 0;
                while (resultCount.next()) {
                    rowCount = resultCount.getInt(1);
                }
                if (rowCount <= cachedTable.getCacheLimit()) {
                    cachedTable.setFullyLoaded(true);
                }
                resultCount.close();
            }
            resultSet.close();
        } catch (SQLException e) {
            log.error("Unable to read the table: " + tableDefinition.getExternalTable().getParameter(PARAM_TABLE_NAME), e);
        } finally {
            cleanUpConnections(statement, con);
        }
    }

    private void populateInsertQuery(Event event, PreparedStatement statement) throws SQLException {
        for (int i = 0; i < attributeList.size(); i++) {
            switch (attributeList.get(i).getType()) {
                case INT:
                    statement.setInt(i + 1, ((Number) event.getData(i)).intValue());
                    break;
                case LONG:
                    statement.setLong(i + 1, ((Number) event.getData(i)).longValue());
                    break;
                case FLOAT:
                    statement.setFloat(i + 1, ((Number) event.getData(i)).floatValue());
                    break;
                case DOUBLE:
                    statement.setDouble(i + 1, ((Number) event.getData(i)).doubleValue());
                    break;
                case BOOL:
                    statement.setBoolean(i + 1, (Boolean) event.getData(i));
                    break;
                default:
                    statement.setString(i + 1, event.getData(i).toString());
                    break;
            }
        }
    }

    private void createPreparedStatementQueries() {
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(fullTableName);
        builder.append(tableColumnList);

        builder.append(" VALUES (");
        for (int i = 0; i < attributeList.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append("?");
        }
        builder.append(")");
        insertQuery = builder.toString();
    }

    @Override
    public void delete(StreamEvent streamEvent, ConditionExecutor conditionExecutor) {
        PreparedStatement statement = null;
        Connection con = null;
        try {
            initializeConnection();
            con = dataSource.getConnection();
            StringBuilder statementBuilder = new StringBuilder("DELETE FROM ");
            statementBuilder.append(fullTableName);
            statementBuilder.append(" WHERE ");
            if (streamEvent instanceof AtomicEvent) {
                PredicateTreeNode predicate = conditionExecutor.constructPredicate((Event) streamEvent, tableDefinition, new SQLPredicateBuilder());
                statementBuilder.append(predicate.buildPredicateString());
                statement = con.prepareStatement(statementBuilder.toString());
                ArrayList paramList = new ArrayList();
                predicate.populateParameters(paramList);
                for (int i = 0; i < paramList.size(); i++) {
                    populateStatement(statement, i + 1, paramList.get(i));
                }
                statement.executeUpdate();
            } else {
                for (int i = 0, size = ((ListEvent) streamEvent).getActiveEvents(); i < size; i++) {
                    if (i > 0) {
                        statementBuilder.append(" OR ");
                    }
                    statementBuilder.append("(");
                    statementBuilder.append(conditionExecutor.constructPredicate((Event) ((ListEvent) streamEvent).getEvent(i), tableDefinition, new SQLPredicateBuilder()).buildPredicateString());
                    statementBuilder.append(")");
                }
                statement = con.prepareStatement(statementBuilder.toString());
                statement.executeUpdate();
            }
            if (cachedTable != null) {
                cachedTable.delete(streamEvent, conditionExecutor);
            }
            if (bloomFiltersEnabled) {
                buildBloomFilters();
            }

        } catch (SQLException e) {
            log.error("Unable to execute deletion.", e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to load the database driver.", e);
        } finally {
            cleanUpConnections(statement, con);
        }

    }

    @Override
    public void update(StreamEvent streamEvent, ConditionExecutor conditionExecutor,
                       int[] attributeUpdateMappingPosition) {
        Connection con = null;
        PreparedStatement statement = null;

        try {
            initializeConnection();
            con = dataSource.getConnection();
            Event atomicEvent = null;
            SQLPredicateBuilder predicateBuilder = new SQLPredicateBuilder();
            if (streamEvent instanceof AtomicEvent) {
                atomicEvent = (Event) streamEvent;
            } else {
                if (((ListEvent) streamEvent).getActiveEvents() > 0) {
                    atomicEvent = ((ListEvent) streamEvent).getEvent(0);
                }
            }

            PredicateTreeNode predicate = conditionExecutor.constructPredicate(atomicEvent, tableDefinition, predicateBuilder);
            String query = createUpdateQuery(predicate.buildPredicateString(), attributeUpdateMappingPosition);
            statement = con.prepareStatement(query);

            for (int i = 0; i < attributeList.size(); i++) {
                populateStatement(statement, i + 1, atomicEvent.getData(i));
            }

            ArrayList paramList = new ArrayList();
            predicate.populateParameters(paramList);
            for (int i = 0; i < paramList.size(); i++) {
                populateStatement(statement, attributeList.size() + i + 1, paramList.get(i));
            }
            statement.executeUpdate();

            if (streamEvent instanceof ListEvent) {
                statement.clearParameters();

                //  event at index 0 is already processed
                for (int j = 1, size = ((ListEvent) streamEvent).getActiveEvents(); j < size; j++) {
                    Event event = ((ListEvent) streamEvent).getEvent(j);
                    predicate = conditionExecutor.constructPredicate(event, tableDefinition, predicateBuilder);
                    paramList.clear();
                    predicate.populateParameters(paramList);
                    for (int i = 0; i < attributeList.size(); i++) {
                        populateStatement(statement, i + 1, event.getData(i));
                    }
                    for (int i = 0; i < paramList.size(); i++) {
                        populateStatement(statement, attributeList.size() + i + 1, paramList.get(i));
                    }
                    statement.addBatch();
                }
                statement.executeBatch();
            }

            if (cachedTable != null) {
                cachedTable.update(streamEvent, conditionExecutor, attributeUpdateMappingPosition);
            }
            if (bloomFiltersEnabled) {
                buildBloomFilters();
            }

        } catch (SQLException e) {
            log.error("Unable to execute update on " + streamEvent, e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to load the database driver for " + tableDefinition.getExternalTable().getParameter(PARAM_TABLE_NAME), e);
        } finally {
            cleanUpConnections(statement, con);
        }

    }


    @Override
    public boolean contains(AtomicEvent atomicEvent, ConditionExecutor conditionExecutor) {

        PredicateTreeNode predicate = null;

        if (bloomFiltersEnabled) {
            // bloom filters only for the equal conditions only.
            predicate = conditionExecutor.constructPredicate(atomicEvent, tableDefinition, new SQLPredicateBuilder());
            ArrayList tokenList = new ArrayList();
            predicate.populateTokens(tokenList);
//        if (predicateParts.length == 2) {
            // looking for two sided equals conditions only.
            for (int ops = 1; ops < tokenList.size() - 1; ops++) {
                if (tokenList.get(ops).toString().startsWith("op:") &&
                    tokenList.get(ops).toString().replaceFirst("op:", "").trim().equals("=")) {
                    String param = tokenList.get(ops - 1).toString();
                    String value = tokenList.get(ops + 1).toString();
                    if (!param.startsWith("var:")) {
                        param = value.replaceFirst("var:", "").trim();
                        value = tokenList.get(ops - 1).toString().replaceFirst("val:", "").trim();
                    } else {
                        param = param.replaceFirst("var:", "").trim();
                        value = value.replaceFirst("val:", "").trim();
                    }
                    for (int i = 0; i < attributeList.size(); i++) {
                        if (attributeList.get(i).getName().equals(param)) {
                            boolean mightContain = bloomFilters[i].mightContain(value.getBytes());
                            if (!mightContain) {
                                return false;
                            }
                        }
                    }
                }
            }
        }


        if ((cachedTable != null) && cachedTable.contains(atomicEvent, conditionExecutor)) {
            return true;
        } else {
            Connection con = null;
            PreparedStatement statement = null;
            try {
                if (predicate == null) {
                    predicate = conditionExecutor.constructPredicate(atomicEvent, tableDefinition, new SQLPredicateBuilder());
                }

                if (dataSource != null) {
                    con = dataSource.getConnection();
                    statement = con.prepareStatement("SELECT * FROM " + fullTableName + " WHERE " + predicate.buildPredicateString() + " LIMIT 0,1");
                    ArrayList paramList = new ArrayList();
                    predicate.populateParameters(paramList);
                    for (int i = 0; i < paramList.size(); i++) {
                        populateStatement(statement, i + 1, paramList.get(i));
                    }
                    ResultSet resultSet = statement.executeQuery();
//                resultSet.setFetchSize(1);
                    boolean contains = false;
                    long timestamp = System.currentTimeMillis();

                    while (resultSet.next()) {
                        contains = true;
                        if (cachedTable != null) {

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
                            Event event = new InEvent(tableDefinition.getExternalTable().getParameter(PARAM_TABLE_NAME), timestamp, data);
                            cachedTable.add(event);
                        } else {
                            break;
                        }
                    }
                    resultSet.close();
                    return contains;
                } else {
                    log.error("DataSource not found for the given configuration of event table, please verify the configuration");
                    return false;
                }
            } catch (SQLException e) {
                log.error("Can't read the database table: " + tableDefinition.getExternalTable().getParameter(PARAM_TABLE_NAME), e);
            } finally {
                cleanUpConnections(statement, con);
            }
            return false;
        }
    }

    private String createUpdateQuery(String predicate, int[] attributeMappingPositions) {
        StringBuilder statementBuilder = new StringBuilder("UPDATE ");
        statementBuilder.append(fullTableName);
        statementBuilder.append(" SET ");

        for (int i = 0; i < attributeList.size(); i++) {
            if (i > 0) {
                statementBuilder.append(", ");
            }
            statementBuilder.append(attributeList.get(i).getName());
            statementBuilder.append(" = ?");
        }
        statementBuilder.append(" WHERE ");
        if (predicate != null) {
            statementBuilder.append(predicate);
        }
        return statementBuilder.toString();
    }

    @Override
    public QueryEventSource getQueryEventSource() {
        return queryEventSource;
    }

    @Override
    public Iterator<StreamEvent> iterator(StreamEvent event, ConditionExecutor conditionExecutor) {
        if (cachedTable != null && cachedTable.isFullyLoaded()) {
            if (event instanceof AtomicEvent) {
                synchronized (this) {
                    ArrayList<StreamEvent> resultEvents = new ArrayList<StreamEvent>();
                    Iterator<StreamEvent> iterator = cachedTable.iterator();
                    StateEvent stateEvent = new InStateEvent(new StreamEvent[2]);
                    stateEvent.setStreamEvent(0, event);
                    while (iterator.hasNext()) {
                        StreamEvent cachedEvent = iterator.next();
                        stateEvent.setStreamEvent(1, cachedEvent);
                        if (conditionExecutor.execute(stateEvent)) {
                            resultEvents.add(cachedEvent);
                        }
                    }
                    return resultEvents.iterator();
                }

            }
        }
        PredicateTreeNode predicate = conditionExecutor.constructPredicate((AtomicEvent) event, tableDefinition, new SQLPredicateBuilder());
        String sqlPredicate = predicate.buildPredicateString();
        if (sqlPredicate.trim().equals("?")) {
            return iterator();
        }
        ArrayList paramList = new ArrayList();
        predicate.populateParameters(paramList);
        for (int i = 0; i < paramList.size(); i++) {
            Object value = paramList.get(i);
            if (value != null) {
                value = value.toString().replaceAll("'", "''");
            }
            sqlPredicate = sqlPredicate.replaceFirst("\\?", "'" + value.toString() + "'");
        }
        return iterator(sqlPredicate);
    }

    @Override
    public Iterator<StreamEvent> iterator(String SQLPredicate) {
        Connection con = null;
        Statement statement = null;
        try {
            con = dataSource.getConnection();
            statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + fullTableName +
                                                         ((SQLPredicate == null) ? "" : (" WHERE " + SQLPredicate)));
            resultSet.setFetchSize(10000);
            ArrayList<StreamEvent> eventList = new ArrayList<StreamEvent>();
            long timestamp = System.currentTimeMillis();
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
                Event event = new InEvent(tableDefinition.getExternalTable().getParameter(PARAM_TABLE_NAME), timestamp, data);
                eventList.add(event);
            }
            resultSet.close();
            return eventList.iterator();
        } catch (SQLException e) {
            log.error("Unable to read the table: " + tableDefinition.getExternalTable().getParameter(PARAM_TABLE_NAME), e);
        } finally {
            cleanUpConnections(statement, con);
        }
        return null;
    }

    @Override
    public Iterator<StreamEvent> iterator() {
        return iterator(null);
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

    private void populateStatement(PreparedStatement stmt, int index, Object value)
            throws SQLException {
        if (value instanceof String) {
            stmt.setString(index, (String) value);
        } else if (value instanceof Integer) {
            stmt.setInt(index, (Integer) value);
        } else if (value instanceof Double) {
            stmt.setDouble(index, (Double) value);
        } else if (value instanceof Boolean) {
            stmt.setBoolean(index, (Boolean) value);
        } else if (value instanceof Float) {
            stmt.setFloat(index, (Float) value);
        } else if (value instanceof Long) {
            stmt.setLong(index, (Long) value);
        } else {
            stmt.setString(index, (String) value);
        }
    }
}

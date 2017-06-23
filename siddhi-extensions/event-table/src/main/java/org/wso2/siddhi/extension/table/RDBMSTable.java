/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.siddhi.extension.table;

import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.exception.CannotLoadConfigurationException;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.extension.eventtable.rdbms.DBQueryHelper;
import org.wso2.siddhi.extension.eventtable.rdbms.PooledDataSource;
import org.wso2.siddhi.extension.eventtable.rdbms.RDBMSOperator;
import org.wso2.siddhi.extension.eventtable.rdbms.RDBMSOperatorParser;
import org.wso2.siddhi.extension.table.cache.CachingTable;
import org.wso2.siddhi.extension.table.rdbms.DBHandler;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.sql.DataSource;

@Extension(
        name = "rdbms",
        namespace = "store",
        description = "Using this extension the data source or the connection instructions can be " +
                "assigned to the event table.",
        parameters = {
                @Parameter(name = "datasource.name",
                        description = "Name of the data source, which is registered in the system.",
                        type = {DataType.STRING}),
                @Parameter(name = "table.name",
                        description = "Name of the backed by table.",
                        type = {DataType.STRING}),
                @Parameter(name = "cache",
                        description = "Type of the cache implementation.\n(" +
                                "    Basic: Events are cached in a FIFO manner where the oldest event will be " +
                                "dropped when the cache is full.\n" +
                                "    LRU (Least Recently Used): The least recently used event is dropped " +
                                "when the cache is full.\n" +
                                "    LFU (Least Frequently Used): The least frequently used event is dropped " +
                                "when the cache is full." +
                                ")\n" +
                                " if the cache element is not specified the Basic cache will be assigned by default.",
                        type = {DataType.STRING}),
                @Parameter(name = "cache.size",
                        description = "The size of the cache. If the element is not assigned the default value 4096 " +
                                "will be assigned as the cache size.",
                        type = {DataType.STRING}),
                @Parameter(name = "bloom.filters",
                        description = "The bloom filter enable or disable property.",
                        type = {DataType.STRING}),
                @Parameter(name = "jdbc.url",
                        description = "The jdbc url for databases which is not registered as datasource.",
                        type = {DataType.STRING}),
                @Parameter(name = "driver.name",
                        description = "The jdbc driver name for databases which is not registered as datasource.",
                        type = {DataType.STRING}),
                @Parameter(name = "username",
                        description = "The username for databases which is not registered as datasource.",
                        type = {DataType.STRING}),
                @Parameter(name = "password",
                        description = "The password for databases which is not registered as datasource.",
                        type = {DataType.STRING})
        }
)
public class RDBMSTable implements Table {

    private static final Logger log = Logger.getLogger(RDBMSTable.class);

    /*
    Loads rdbms-table-config.xml file which provides DB mapping details
     */
    static {
        try {
            DBQueryHelper.loadConfiguration();
        } catch (CannotLoadConfigurationException e) {
            throw new SiddhiAppCreationException("Error while loading the rdbms configuration file", e);
        }
    }

    private TableDefinition tableDefinition;
    private DBHandler dbHandler;
    private CachingTable cachedTable;
    private String cacheSizeInString;
    private boolean isCachingEnabled;

    /**
     * Event Table initialization method, it checks the annotation and do necessary pre configuration tasks.
     * * @param tableDefinition        Definition of event table
     * @param storeEventPool
     * @param storeEventCloner
     * @param configReader
     * @param siddhiAppContext SiddhiApp related meta information
     */
    public void init(TableDefinition tableDefinition, StreamEventPool storeEventPool, StreamEventCloner
            storeEventCloner, ConfigReader configReader, SiddhiAppContext siddhiAppContext) {
        this.tableDefinition = tableDefinition;
        Connection con = null;
        int bloomFilterSize = RDBMSTableConstants.BLOOM_FILTER_SIZE;
        int bloomFilterHashFunctions = RDBMSTableConstants.BLOOM_FILTER_HASH_FUNCTIONS;
        String dataSourceName;
        String tableName;
        String cacheType;
        String cacheLoadingType;
        String cacheValidityInterval;
        String bloomsEnabled;
        String bloomFilterValidityInterval;


        Annotation fromAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_STORE,
                tableDefinition.getAnnotations());
        dataSourceName = fromAnnotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_DATASOURCE_NAME);
        tableName = fromAnnotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_TABLE_NAME);

        DataSource dataSource = siddhiAppContext.getSiddhiContext().getSiddhiDataSource(dataSourceName);
        List<Attribute> attributeList = tableDefinition.getAttributeList();

        if (dataSource == null && dataSourceName == null) {
            String jdbcConnectionUrl;
            String username;
            String password;
            String driverName;

            jdbcConnectionUrl = fromAnnotation.getElement(RDBMSTableConstants.EVENT_TABLE_RDBMS_TABLE_JDBC_URL);
            username = fromAnnotation.getElement(RDBMSTableConstants.EVENT_TABLE_RDBMS_TABLE_USERNAME);
            password = fromAnnotation.getElement(RDBMSTableConstants.EVENT_TABLE_RDBMS_TABLE_PASSWORD);
            driverName = fromAnnotation.getElement(RDBMSTableConstants.EVENT_TABLE_RDBMS_TABLE_DRIVER_NAME);
            List<Element> connectionPropertyElements = null;

            Annotation connectionAnnotation = AnnotationHelper.getAnnotation(RDBMSTableConstants.ANNOTATION_CONNECTION, tableDefinition.getAnnotations());
            if (connectionAnnotation != null) {
                connectionPropertyElements = connectionAnnotation.getElements();
            }
            dataSource = PooledDataSource.getPoolDataSource(driverName, jdbcConnectionUrl, username, password,
                    connectionPropertyElements);
        }

        if (dataSource == null) {
            throw new SiddhiAppCreationException("Datasource specified for the event table is invalid/null");
        }
        if (tableName == null) {
            throw new SiddhiAppCreationException("Invalid query specified. Required properties (tableName) not " +
                    "found ");
        }

        cacheType = fromAnnotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_CACHE);
        cacheSizeInString = fromAnnotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_CACHE_SIZE);
        cacheLoadingType = fromAnnotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_CACHE_LOADING);
        cacheValidityInterval = fromAnnotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_CACHE_VALIDITY_PERIOD);
        bloomsEnabled = fromAnnotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_BLOOM_FILTERS);
        bloomFilterValidityInterval = fromAnnotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_BLOOM_VALIDITY_PERIOD);

        try {
            this.dbHandler = new DBHandler(dataSource, tableName, attributeList, tableDefinition);

            if ((con = dataSource.getConnection()) == null) {
                throw new SiddhiAppCreationException("Error while making connection to database");
            }

            if (cacheType != null) {
                cachedTable = new CachingTable(cacheType, cacheSizeInString, siddhiAppContext, tableDefinition);
                isCachingEnabled = true;

                if (cacheLoadingType != null && cacheLoadingType.equalsIgnoreCase(RDBMSTableConstants.EAGER_CACHE_LOADING_ELEMENT)) {
                    dbHandler.loadDBCache(cachedTable, cacheSizeInString);
                }

                if (cacheValidityInterval != null) {
                    Long cacheTimeInterval = Long.parseLong(cacheValidityInterval);
                    Timer timer = new Timer();
                    CacheUpdateTask cacheUpdateTask = new CacheUpdateTask();
                    timer.schedule(cacheUpdateTask, cacheTimeInterval);
                }

            } else if (bloomsEnabled != null && bloomsEnabled.equalsIgnoreCase("enable")) {
                String bloomsFilterSize;
                String bloomsFilterHash;
                bloomsFilterSize = fromAnnotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_BLOOM_FILTERS_SIZE);
                bloomsFilterHash = fromAnnotation.getElement(RDBMSTableConstants.ANNOTATION_ELEMENT_BLOOM_FILTERS_HASH);
                if (bloomsFilterSize != null) {
                    bloomFilterSize = Integer.parseInt(bloomsFilterSize);
                }
                if (bloomsFilterHash != null) {
                    bloomFilterHashFunctions = Integer.parseInt(bloomsFilterHash);
                }

                dbHandler.setBloomFilters(bloomFilterSize, bloomFilterHashFunctions);
                dbHandler.buildBloomFilters();
                if (bloomFilterValidityInterval != null) {
                    Long bloomTimeInterval = Long.parseLong(bloomFilterValidityInterval);
                    Timer timer = new Timer();
                    BloomsUpdateTask bloomsUpdateTask = new BloomsUpdateTask();
                    timer.schedule(bloomsUpdateTask, bloomTimeInterval, bloomTimeInterval);
                }
            }

        } catch (SQLException e) {
            throw new SiddhiAppCreationException("Error while making connection to database", e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    log.error("unable to release connection", e);
                }
            }
        }
    }

    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    /**
     * Called when adding an event to the event table
     *
     * @param addingEventChunk input event list
     */
    @Override
    public void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        dbHandler.addEvent(addingEventChunk);
    }

    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, CompiledCondition compiledCondition,
                       UpdateAttributeMapper[] updateAttributeMappers) {
        ((Operator) compiledCondition).update(updatingEventChunk, null, null);
        if (isCachingEnabled) {
            ((RDBMSOperator) compiledCondition).getInMemoryTableOperator().update(updatingEventChunk,
                    cachedTable.getCacheList(), updateAttributeMappers);
        }
    }

    @Override
    public void updateOrAdd(ComplexEventChunk<StateEvent> updateOrAddingEventChunk,
                            CompiledCondition compiledCondition, UpdateAttributeMapper[] updateAttributeMappers,
                            AddingStreamEventExtractor addingStreamEventExtractor) {
        ((Operator) compiledCondition).tryUpdate(updateOrAddingEventChunk, null, null,
                addingStreamEventExtractor);
        if (isCachingEnabled) {
            ((RDBMSOperator) compiledCondition).getInMemoryTableOperator().tryUpdate(updateOrAddingEventChunk,
                    cachedTable.getCacheList(), updateAttributeMappers, addingStreamEventExtractor);
        }
    }

    /**
     * Called when having "in" condition, to check the existence of the event
     *
     * @param matchingEvent     Event that need to be check for existence
     * @param compiledCondition Operator that perform RDBMS related search
     */
    @Override
    public synchronized boolean contains(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        if (isCachingEnabled) {
            return ((RDBMSOperator) compiledCondition).getInMemoryTableOperator().contains(matchingEvent,
                    cachedTable.getCacheList()) || ((Operator) compiledCondition).contains(matchingEvent, null);
        } else {
            return ((Operator) compiledCondition).contains(matchingEvent, null);
        }
    }

    /**
     * Called when deleting an event chunk from event table
     *
     * @param deletingEventChunk Event list for deletion
     * @param compiledCondition  Operator that perform RDBMS related operations
     */
    @Override
    public synchronized void delete(ComplexEventChunk deletingEventChunk, CompiledCondition compiledCondition) {
        ((Operator) compiledCondition).delete(deletingEventChunk, null);
        if (isCachingEnabled) {
            ((RDBMSOperator) compiledCondition).getInMemoryTableOperator().delete(deletingEventChunk,
                    cachedTable.getCacheList());
        }
    }

    /**
     * Called to find a event from event table
     */
    @Override
    public synchronized StreamEvent find(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        return ((Operator) compiledCondition).find(matchingEvent, null, null);
    }

    /**
     * Called to construct a operator to perform search, delete and update operations
     */
    @Override
    public CompiledCondition compileCondition(Expression expression, MatchingMetaInfoHolder matchingMetaInfoHolder,
                                              SiddhiAppContext siddhiAppContext,
                                              List<VariableExpressionExecutor> variableExpressionExecutors,
                                              Map<String, Table> tableMap, String queryName) {
        return RDBMSOperatorParser.parse(dbHandler, expression, matchingMetaInfoHolder, siddhiAppContext,
                variableExpressionExecutors, tableMap, tableDefinition, cachedTable, queryName);
    }


    class CacheUpdateTask extends TimerTask {
        public void run() {
            cachedTable.invalidateCache();
            dbHandler.loadDBCache(cachedTable, cacheSizeInString);
        }
    }

    class BloomsUpdateTask extends TimerTask {
        public void run() {
            dbHandler.buildBloomFilters();
        }
    }

}

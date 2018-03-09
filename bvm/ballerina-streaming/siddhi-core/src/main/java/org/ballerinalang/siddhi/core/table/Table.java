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

package org.ballerinalang.siddhi.core.table;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.state.StateEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventCloner;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.exception.ConnectionUnavailableException;
import org.ballerinalang.siddhi.core.executor.VariableExpressionExecutor;
import org.ballerinalang.siddhi.core.query.processor.stream.window.FindableProcessor;
import org.ballerinalang.siddhi.core.table.record.RecordTableHandler;
import org.ballerinalang.siddhi.core.util.ExceptionUtil;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.core.util.StringUtil;
import org.ballerinalang.siddhi.core.util.collection.AddingStreamEventExtractor;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledCondition;
import org.ballerinalang.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.core.util.parser.helper.QueryParserHelper;
import org.ballerinalang.siddhi.core.util.statistics.LatencyTracker;
import org.ballerinalang.siddhi.core.util.statistics.MemoryCalculable;
import org.ballerinalang.siddhi.core.util.statistics.ThroughputTracker;
import org.ballerinalang.siddhi.core.util.transport.BackoffRetryCounter;
import org.ballerinalang.siddhi.query.api.definition.TableDefinition;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.UpdateSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Interface class to represent Tables in Siddhi. There are multiple implementations. Ex: {@link InMemoryTable}. Table
 * will support basic operations of add, delete, update, update or add and contains. *
 */
public abstract class Table implements FindableProcessor, MemoryCalculable {

    private static final Logger LOG = LoggerFactory.getLogger(Table.class);

    protected TableDefinition tableDefinition;

    private AtomicBoolean isTryingToConnect = new AtomicBoolean(false);
    private BackoffRetryCounter backoffRetryCounter = new BackoffRetryCounter();
    private AtomicBoolean isConnected = new AtomicBoolean(false);
    private ScheduledExecutorService scheduledExecutorService;
    private SiddhiAppContext siddhiAppContext;
    private RecordTableHandler recordTableHandler;
    private LatencyTracker latencyTrackerFind;
    private LatencyTracker latencyTrackerInsert;
    private LatencyTracker latencyTrackerUpdate;
    private LatencyTracker latencyTrackerDelete;
    private LatencyTracker latencyTrackerUpdateOrInsert;
    private LatencyTracker latencyTrackerContains;
    private ThroughputTracker throughputTrackerFind;
    private ThroughputTracker throughputTrackerInsert;
    private ThroughputTracker throughputTrackerUpdate;
    private ThroughputTracker throughputTrackerDelete;
    private ThroughputTracker throughputTrackerUpdateOrInsert;
    private ThroughputTracker throughputTrackerContains;

    public void initTable(TableDefinition tableDefinition, StreamEventPool storeEventPool,
                          StreamEventCloner storeEventCloner,
                          ConfigReader configReader, SiddhiAppContext siddhiAppContext,
                          RecordTableHandler recordTableHandler) {
        this.tableDefinition = tableDefinition;
        this.scheduledExecutorService = siddhiAppContext.getScheduledExecutorService();
        this.siddhiAppContext = siddhiAppContext;
        this.recordTableHandler = recordTableHandler;
        if (siddhiAppContext.getStatisticsManager() != null) {
            latencyTrackerFind = QueryParserHelper.createLatencyTracker(siddhiAppContext, tableDefinition.getId(),
                    SiddhiConstants.METRIC_INFIX_TABLES, SiddhiConstants.METRIC_TYPE_FIND);
            latencyTrackerInsert = QueryParserHelper.createLatencyTracker(siddhiAppContext, tableDefinition.getId(),
                    SiddhiConstants.METRIC_INFIX_TABLES, SiddhiConstants.METRIC_TYPE_INSERT);
            latencyTrackerUpdate = QueryParserHelper.createLatencyTracker(siddhiAppContext, tableDefinition.getId(),
                    SiddhiConstants.METRIC_INFIX_TABLES, SiddhiConstants.METRIC_TYPE_UPDATE);
            latencyTrackerDelete = QueryParserHelper.createLatencyTracker(siddhiAppContext, tableDefinition.getId(),
                    SiddhiConstants.METRIC_INFIX_TABLES, SiddhiConstants.METRIC_TYPE_DELETE);
            latencyTrackerUpdateOrInsert = QueryParserHelper.createLatencyTracker(siddhiAppContext,
                    tableDefinition.getId(), SiddhiConstants.METRIC_INFIX_TABLES,
                    SiddhiConstants.METRIC_TYPE_UPDATE_OR_INSERT);
            latencyTrackerContains = QueryParserHelper.createLatencyTracker(siddhiAppContext, tableDefinition.getId(),
                    SiddhiConstants.METRIC_INFIX_TABLES, SiddhiConstants.METRIC_TYPE_CONTAINS);

            throughputTrackerFind = QueryParserHelper.createThroughputTracker(siddhiAppContext, tableDefinition.getId(),
                    SiddhiConstants.METRIC_INFIX_TABLES, SiddhiConstants.METRIC_TYPE_FIND);
            throughputTrackerInsert = QueryParserHelper.createThroughputTracker(siddhiAppContext,
                    tableDefinition.getId(), SiddhiConstants.METRIC_INFIX_TABLES, SiddhiConstants.METRIC_TYPE_INSERT);
            throughputTrackerUpdate = QueryParserHelper.createThroughputTracker(siddhiAppContext,
                    tableDefinition.getId(), SiddhiConstants.METRIC_INFIX_TABLES, SiddhiConstants.METRIC_TYPE_UPDATE);
            throughputTrackerDelete = QueryParserHelper.createThroughputTracker(siddhiAppContext,
                    tableDefinition.getId(), SiddhiConstants.METRIC_INFIX_TABLES, SiddhiConstants.METRIC_TYPE_DELETE);
            throughputTrackerUpdateOrInsert = QueryParserHelper.createThroughputTracker(siddhiAppContext,
                    tableDefinition.getId(), SiddhiConstants.METRIC_INFIX_TABLES,
                    SiddhiConstants.METRIC_TYPE_UPDATE_OR_INSERT);
            throughputTrackerContains = QueryParserHelper.createThroughputTracker(siddhiAppContext,
                    tableDefinition.getId(), SiddhiConstants.METRIC_INFIX_TABLES, SiddhiConstants.METRIC_TYPE_CONTAINS);


        }
        init(tableDefinition, storeEventPool, storeEventCloner, configReader, siddhiAppContext, recordTableHandler);
    }

    protected abstract void init(TableDefinition tableDefinition, StreamEventPool storeEventPool,
                                 StreamEventCloner storeEventCloner, ConfigReader configReader,
                                 SiddhiAppContext siddhiAppContext, RecordTableHandler recordTableHandler);

    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    public void addEvents(ComplexEventChunk<StreamEvent> addingEventChunk, int noOfEvents) {
        if (isConnected.get()) {
            try {
                if (latencyTrackerInsert != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerInsert.markIn();
                }
                addingEventChunk.reset();
                add(addingEventChunk);
                if (throughputTrackerInsert != null && siddhiAppContext.isStatsEnabled()) {
                    throughputTrackerInsert.eventsIn(noOfEvents);
                }
            } catch (ConnectionUnavailableException e) {
                isConnected.set(false);
                LOG.error(ExceptionUtil.getMessageWithContext(e, siddhiAppContext) +
                        " Connection unavailable at Table '" + tableDefinition.getId() +
                        "', will retry connection immediately.", e);
                connectWithRetry();
                addEvents(addingEventChunk, noOfEvents);
            } finally {
                if (latencyTrackerInsert != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerInsert.markOut();
                }
            }
        } else if (isTryingToConnect.get()) {
            LOG.error("Error on '" + siddhiAppContext.getName() + "'. Dropping event at Table '" +
                    tableDefinition.getId() + "' as its still trying to reconnect!, events dropped '" +
                    addingEventChunk + "'");
        } else {
            connectWithRetry();
            addEvents(addingEventChunk, noOfEvents);
        }
    }

    protected abstract void add(ComplexEventChunk<StreamEvent> addingEventChunk) throws ConnectionUnavailableException;

    public StreamEvent find(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        if (isConnected.get()) {
            try {
                if (latencyTrackerFind != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerFind.markIn();
                }
                StreamEvent results = find(compiledCondition, matchingEvent);
                if (throughputTrackerFind != null && siddhiAppContext.isStatsEnabled()) {
                    throughputTrackerFind.eventIn();
                }
                return results;
            } catch (ConnectionUnavailableException e) {
                isConnected.set(false);
                LOG.error(ExceptionUtil.getMessageWithContext(e, siddhiAppContext) +
                        " Connection unavailable at Table '" + tableDefinition.getId() +
                        "', will retry connection immediately.", e);
                connectWithRetry();
                return find(matchingEvent, compiledCondition);
            } finally {
                if (latencyTrackerFind != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerFind.markOut();
                }
            }
        } else if (isTryingToConnect.get()) {
            LOG.error("Error on '" + siddhiAppContext.getName() + "'. Find operation failed for event '" +
                    matchingEvent + "', at Table '" + tableDefinition.getId() + "' as its still trying to reconnect!");
            return null;
        } else {
            connectWithRetry();
            return find(matchingEvent, compiledCondition);
        }
    }

    protected abstract StreamEvent find(CompiledCondition compiledCondition, StateEvent matchingEvent)
            throws ConnectionUnavailableException;

    public void deleteEvents(ComplexEventChunk<StateEvent> deletingEventChunk, CompiledCondition compiledCondition,
                             int noOfEvents) {
        if (isConnected.get()) {
            try {
                if (latencyTrackerDelete != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerDelete.markIn();
                }
                delete(deletingEventChunk, compiledCondition);
                if (throughputTrackerDelete != null && siddhiAppContext.isStatsEnabled()) {
                    throughputTrackerDelete.eventsIn(noOfEvents);
                }
            } catch (ConnectionUnavailableException e) {
                isConnected.set(false);
                LOG.error(ExceptionUtil.getMessageWithContext(e, siddhiAppContext) +
                        " Connection unavailable at Table '" + tableDefinition.getId() +
                        "', will retry connection immediately.", e);
                connectWithRetry();
                deleteEvents(deletingEventChunk, compiledCondition, noOfEvents);
            } finally {
                if (latencyTrackerDelete != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerDelete.markOut();
                }
            }
        } else if (isTryingToConnect.get()) {
            LOG.error("Error on '" + siddhiAppContext.getName() + "'. Dropping event at Table '" +
                    tableDefinition.getId() + "' as its still trying to reconnect!, events dropped '" +
                    deletingEventChunk + "'");
        } else {
            connectWithRetry();
            deleteEvents(deletingEventChunk, compiledCondition, noOfEvents);
        }
    }

    protected abstract void delete(ComplexEventChunk<StateEvent> deletingEventChunk,
                                   CompiledCondition compiledCondition) throws ConnectionUnavailableException;


    public void updateEvents(ComplexEventChunk<StateEvent> updatingEventChunk,
                             CompiledCondition compiledCondition,
                             CompiledUpdateSet compiledUpdateSet, int noOfEvents) {
        if (isConnected.get()) {
            try {
                if (latencyTrackerUpdate != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerUpdate.markIn();
                }
                update(updatingEventChunk, compiledCondition, compiledUpdateSet);
                if (throughputTrackerUpdate != null && siddhiAppContext.isStatsEnabled()) {
                    throughputTrackerUpdate.eventsIn(noOfEvents);
                }
            } catch (ConnectionUnavailableException e) {
                isConnected.set(false);
                LOG.error(ExceptionUtil.getMessageWithContext(e, siddhiAppContext) +
                        " Connection unavailable at Table '" + tableDefinition.getId() +
                        "', will retry connection immediately.", e);
                connectWithRetry();
                updateEvents(updatingEventChunk, compiledCondition, compiledUpdateSet, noOfEvents);
            } finally {
                if (latencyTrackerUpdate != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerUpdate.markOut();
                }
            }
        } else if (isTryingToConnect.get()) {
            LOG.error("Error on '" + siddhiAppContext.getName() + "'. Dropping event at Table '" +
                    tableDefinition.getId() + "' as its still trying to reconnect!, events dropped '" +
                    updatingEventChunk + "'");
        } else {
            connectWithRetry();
            updateEvents(updatingEventChunk, compiledCondition, compiledUpdateSet, noOfEvents);
        }
    }

    protected abstract void update(ComplexEventChunk<StateEvent> updatingEventChunk,
                                   CompiledCondition compiledCondition,
                                   CompiledUpdateSet compiledUpdateSet)
            throws ConnectionUnavailableException;

    public void updateOrAddEvents(ComplexEventChunk<StateEvent> updateOrAddingEventChunk,
                                  CompiledCondition compiledCondition,
                                  CompiledUpdateSet compiledUpdateSet,
                                  AddingStreamEventExtractor addingStreamEventExtractor, int noOfEvents) {
        if (isConnected.get()) {
            try {
                if (latencyTrackerUpdateOrInsert != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerUpdateOrInsert.markIn();
                }
                updateOrAdd(updateOrAddingEventChunk, compiledCondition, compiledUpdateSet,
                        addingStreamEventExtractor);
                if (throughputTrackerUpdateOrInsert != null && siddhiAppContext.isStatsEnabled()) {
                    throughputTrackerUpdateOrInsert.eventsIn(noOfEvents);
                }
            } catch (ConnectionUnavailableException e) {
                isConnected.set(false);
                LOG.error(ExceptionUtil.getMessageWithContext(e, siddhiAppContext) +
                        " Connection unavailable at Table '" + tableDefinition.getId() +
                        "', will retry connection immediately.", e);
                connectWithRetry();
                updateOrAddEvents(updateOrAddingEventChunk, compiledCondition, compiledUpdateSet,
                        addingStreamEventExtractor, noOfEvents);
            } finally {
                if (latencyTrackerUpdateOrInsert != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerUpdateOrInsert.markOut();
                }
            }
        } else if (isTryingToConnect.get()) {
            LOG.error("Error on '" + siddhiAppContext.getName() + "'. Dropping event at Table '" +
                    tableDefinition.getId() + "' as its still trying to reconnect!, events dropped '" +
                    updateOrAddingEventChunk + "'");
        } else {
            connectWithRetry();
            updateOrAddEvents(updateOrAddingEventChunk, compiledCondition, compiledUpdateSet,
                    addingStreamEventExtractor, noOfEvents);
        }
    }

    protected abstract void updateOrAdd(ComplexEventChunk<StateEvent> updateOrAddingEventChunk,
                                        CompiledCondition compiledCondition,
                                        CompiledUpdateSet compiledUpdateSet,
                                        AddingStreamEventExtractor addingStreamEventExtractor)
            throws ConnectionUnavailableException;

    public boolean containsEvent(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        if (isConnected.get()) {
            try {
                if (latencyTrackerContains != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerContains.markIn();
                }
                boolean results = contains(matchingEvent, compiledCondition);
                if (throughputTrackerContains != null && siddhiAppContext.isStatsEnabled()) {
                    throughputTrackerContains.eventIn();
                }
                return results;
            } catch (ConnectionUnavailableException e) {
                isConnected.set(false);
                LOG.error(ExceptionUtil.getMessageWithContext(e, siddhiAppContext) +
                        " Connection unavailable at Table '" + tableDefinition.getId() +
                        "', will retry connection immediately.", e);
                connectWithRetry();
                return containsEvent(matchingEvent, compiledCondition);
            } finally {
                if (latencyTrackerContains != null && siddhiAppContext.isStatsEnabled()) {
                    latencyTrackerContains.markOut();
                }
            }
        } else if (isTryingToConnect.get()) {
            LOG.error("Error on '" + siddhiAppContext.getName() + "'. Dropping event at Table '" +
                    tableDefinition.getId() + "' as its still trying to reconnect!, event matching failed for event '"
                    + matchingEvent + "'.");
            return false;
        } else {
            connectWithRetry();
            return containsEvent(matchingEvent, compiledCondition);
        }
    }

    protected abstract boolean contains(StateEvent matchingEvent, CompiledCondition compiledCondition)
            throws ConnectionUnavailableException;

    public void connectWithRetry() {
        if (!isConnected.get()) {
            isTryingToConnect.set(true);
            try {
                connect();
                isConnected.set(true);
                isTryingToConnect.set(false);
                backoffRetryCounter.reset();
            } catch (ConnectionUnavailableException e) {
                LOG.error(StringUtil.removeCRLFCharacters(ExceptionUtil.getMessageWithContext(e, siddhiAppContext)) +
                        " Error while connecting to Table '" + StringUtil.removeCRLFCharacters(tableDefinition.getId())
                        + "', will retry in '" + StringUtil.removeCRLFCharacters(backoffRetryCounter.
                        getTimeInterval()) + "'.", e);
                scheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        connectWithRetry();
                    }
                }, backoffRetryCounter.getTimeIntervalMillis(), TimeUnit.MILLISECONDS);
                backoffRetryCounter.increment();
            } catch (RuntimeException e) {
                LOG.error(StringUtil.removeCRLFCharacters(ExceptionUtil.getMessageWithContext(e, siddhiAppContext)) +
                        " . Error while connecting to Table '" + StringUtil.removeCRLFCharacters(tableDefinition.
                        getId()) + "'.", e);
                throw e;
            }
        }
    }

    /**
     * Builds the "compiled" set clause of an update query.
     * Here, all the pre-processing that can be done prior to receiving the update event is done,
     * so that such pre-processing work will not be done at each update-event-arrival.
     *
     * @param updateSet                   the set of assignment expressions, each containing the table column to be
     *                                    updated and the expression to be assigned.
     * @param matchingMetaInfoHolder      the meta structure of the incoming matchingEvent
     * @param siddhiAppContext            current siddhi app context
     * @param variableExpressionExecutors the list of variable ExpressionExecutors already created
     * @param tableMap                    map of event tables
     * @param queryName                   query name to which the update statement belongs.
     * @return CompiledUpdateSet
     */
    public abstract CompiledUpdateSet compileUpdateSet(UpdateSet updateSet,
                                                       MatchingMetaInfoHolder matchingMetaInfoHolder,
                                                       SiddhiAppContext siddhiAppContext,
                                                       List<VariableExpressionExecutor> variableExpressionExecutors,
                                                       Map<String, Table> tableMap, String queryName);

    protected abstract void connect() throws ConnectionUnavailableException;

    protected abstract void disconnect();

    protected abstract void destroy();

    public RecordTableHandler getHandler() {
        return recordTableHandler;
    }

    public void shutdown() {
        disconnect();
        destroy();
        isConnected.set(false);
        isTryingToConnect.set(false);
    }
}

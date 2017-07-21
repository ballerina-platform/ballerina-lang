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

package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.aggregation.AggregationRuntime;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.state.populater.StateEventPopulatorFactory;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent.EventType;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.exception.StoreQueryCreationException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.QueryRuntime;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.input.stream.join.JoinStreamRuntime;
import org.wso2.siddhi.core.query.input.stream.single.SingleStreamRuntime;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.output.ratelimit.snapshot.WrappedSnapshotOutputRateLimiter;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.lock.LockSynchronizer;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.parser.helper.QueryParserHelper;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.core.window.Window;
import org.wso2.siddhi.query.api.aggregation.Within;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.DuplicateDefinitionException;
import org.wso2.siddhi.query.api.execution.query.StoreQuery;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.store.AggregationInputStore;
import org.wso2.siddhi.query.api.execution.query.input.store.InputStore;
import org.wso2.siddhi.query.api.execution.query.input.store.Store;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to parse {@link QueryRuntime}
 */
public class StoreQueryParser {

    /**
     * Parse a query and return corresponding QueryRuntime.
     *
     * @param query                    query to be parsed.
     * @param siddhiAppContext         associated Siddhi app context.
     * @param streamDefinitionMap      keyvalue containing user given stream definitions.
     * @param tableDefinitionMap       keyvalue containing table definitions.
     * @param windowDefinitionMap      keyvalue containing window definition map.
     * @param aggregationDefinitionMap keyvalue containing aggregation definition map.
     * @param tableMap                 keyvalue containing event tables.
     * @param aggregationMap           keyvalue containing aggrigation runtimes.
     * @param windowMap                keyvalue containing event window map.
     * @param lockSynchronizer         Lock synchronizer for sync the lock across queries.
     * @param queryIndex               query index to identify unknown query by number
     * @return queryRuntime
     */
    public static QueryRuntime parse(StoreQuery storeQuery, SiddhiAppContext siddhiAppContext,
                                     Map<String, AbstractDefinition> tableDefinitionMap,
                                     Map<String, AbstractDefinition> windowDefinitionMap,
                                     Map<String, AbstractDefinition> aggregationDefinitionMap,
                                     Map<String, Table> tableMap,
                                     Map<String, AggregationRuntime> aggregationMap, Map<String, Window> windowMap,
                                     LockSynchronizer lockSynchronizer) {
        List<VariableExpressionExecutor> executors = new ArrayList<VariableExpressionExecutor>();
        LatencyTracker latencyTracker = null;
        LockWrapper lockWrapper = null;
        try {
            String queryName = "store_query_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString();
            latencyTracker = QueryParserHelper.getLatencyTracker(siddhiAppContext, queryName,
                    SiddhiConstants.METRIC_INFIX_QUERIES);
            boolean outputExpectsExpiredEvents = false;
            storeQuery.getInputStore();
            StreamRuntime streamRuntime = parse(storeQuery.getInputStore(),
                    siddhiAppContext, tableDefinitionMap, windowDefinitionMap,
                    aggregationDefinitionMap, tableMap, windowMap, aggregationMap, executors, latencyTracker,
                    outputExpectsExpiredEvents, queryName);
            QuerySelector selector = SelectorParser.parse(query.getSelector(), query.getOutputStream(),
                    siddhiAppContext, streamRuntime.getMetaComplexEvent(), tableMap, executors, queryName);
            boolean isWindow = query.getInputStream() instanceof JoinInputStream;
            if (!isWindow && query.getInputStream() instanceof SingleInputStream) {
                for (StreamHandler streamHandler : ((SingleInputStream) query.getInputStream()).getStreamHandlers()) {
                    if (streamHandler instanceof org.wso2.siddhi.query.api.execution.query.input.handler.Window) {
                        isWindow = true;
                        break;
                    }
                }
            }

            Element synchronizedElement = AnnotationHelper.getAnnotationElement("synchronized",
                    null, query.getAnnotations());
            if (synchronizedElement != null) {
                if (!("false".equalsIgnoreCase(synchronizedElement.getValue()))) {
                    lockWrapper = new LockWrapper(""); // Query LockWrapper does not need a unique
                    // id since it will
                    // not be passed to the LockSynchronizer.
                    lockWrapper.setLock(new ReentrantLock());   // LockWrapper does not have a default lock
                }
            } else {
                if (isWindow || !(streamRuntime instanceof SingleStreamRuntime)) {
                    if (streamRuntime instanceof JoinStreamRuntime) {
                        // If at least one Window is involved in the join, use the LockWrapper of that window
                        // for the query as well.
                        // If join is between two EventWindows, sync the locks of the LockWrapper of those windows
                        // and use either of them for query.
                        MetaStateEvent metaStateEvent = (MetaStateEvent) streamRuntime.getMetaComplexEvent();
                        MetaStreamEvent[] metaStreamEvents = metaStateEvent.getMetaStreamEvents();

                        if (metaStreamEvents[0].getEventType() == EventType.WINDOW &&
                                metaStreamEvents[1].getEventType() == EventType.WINDOW) {
                            LockWrapper leftLockWrapper = windowMap.get(metaStreamEvents[0]
                                    .getLastInputDefinition().getId()).getLock();
                            LockWrapper rightLockWrapper = windowMap.get(metaStreamEvents[1]
                                    .getLastInputDefinition().getId()).getLock();

                            if (!leftLockWrapper.equals(rightLockWrapper)) {
                                // Sync the lock across both wrappers
                                lockSynchronizer.sync(leftLockWrapper, rightLockWrapper);
                            }
                            // Can use either leftLockWrapper or rightLockWrapper since both of them will hold the
                            // same lock internally
                            // If either of their lock is updated later, the other lock also will be update by the
                            // LockSynchronizer.
                            lockWrapper = leftLockWrapper;
                        } else if (metaStreamEvents[0].getEventType() == EventType.WINDOW) {
                            // Share the same wrapper as the query lock wrapper
                            lockWrapper = windowMap.get(metaStreamEvents[0].getLastInputDefinition().getId())
                                    .getLock();
                        } else if (metaStreamEvents[1].getEventType() == EventType.WINDOW) {
                            // Share the same wrapper as the query lock wrapper
                            lockWrapper = windowMap.get(metaStreamEvents[1].getLastInputDefinition().getId())
                                    .getLock();
                        } else {
                            // Join does not contain any Window
                            lockWrapper = new LockWrapper("");  // Query LockWrapper does not need a unique
                            // id since
                            // it will not be passed to the LockSynchronizer.
                            lockWrapper.setLock(new ReentrantLock());   // LockWrapper does not have a default lock
                        }

                    } else {
                        lockWrapper = new LockWrapper("");
                        lockWrapper.setLock(new ReentrantLock());
                    }
                }
            }

            OutputRateLimiter outputRateLimiter = OutputParser.constructOutputRateLimiter(
                    query.getOutputStream().getId(), query.getOutputRate(),
                    query.getSelector().getGroupByList().size() != 0, isWindow,
                    siddhiAppContext.getScheduledExecutorService(), siddhiAppContext, queryName);
            if (outputRateLimiter instanceof WrappedSnapshotOutputRateLimiter) {
                selector.setBatchingEnabled(false);
            }
            siddhiAppContext.addEternalReferencedHolder(outputRateLimiter);

            OutputCallback outputCallback = OutputParser.constructOutputCallback(query.getOutputStream(),
                    streamRuntime.getMetaComplexEvent().getOutputStreamDefinition(), tableMap, windowMap,
                    siddhiAppContext, !(streamRuntime instanceof SingleStreamRuntime), queryName);

            QueryParserHelper.reduceMetaComplexEvent(streamRuntime.getMetaComplexEvent());
            QueryParserHelper.updateVariablePosition(streamRuntime.getMetaComplexEvent(), executors);
            QueryParserHelper.initStreamRuntime(streamRuntime, streamRuntime.getMetaComplexEvent(), lockWrapper,
                    queryName);
            selector.setEventPopulator(StateEventPopulatorFactory.constructEventPopulator(streamRuntime
                    .getMetaComplexEvent()));
            queryRuntime = new QueryRuntime(query, siddhiAppContext, streamRuntime, selector, outputRateLimiter,
                    outputCallback, streamRuntime.getMetaComplexEvent(), lockWrapper != null, queryName);

            if (outputRateLimiter instanceof WrappedSnapshotOutputRateLimiter) {
                selector.setBatchingEnabled(false);
                ((WrappedSnapshotOutputRateLimiter) outputRateLimiter)
                        .init(streamRuntime.getMetaComplexEvent().getOutputStreamDefinition().getAttributeList().size(),
                                selector.getAttributeProcessorList(), streamRuntime.getMetaComplexEvent());
            }
            outputRateLimiter.init(siddhiAppContext, lockWrapper, queryName);

        } catch (DuplicateDefinitionException e) {
            if (nameElement != null) {
                throw new DuplicateDefinitionException(e.getMessage() + ", when creating query " + nameElement
                        .getValue(), e);
            } else {
                throw new DuplicateDefinitionException(e.getMessage(), e);
            }
        } catch (RuntimeException e) {
            if (nameElement != null) {
                throw new SiddhiAppCreationException(e.getMessage() + ", when creating query " + nameElement
                        .getValue(), e);
            } else {
                throw new SiddhiAppCreationException(e.getMessage(), e);
            }
        }
        return queryRuntime;
    }

    private static StreamRuntime parse(InputStore inputStore, SiddhiAppContext siddhiAppContext,
                                       Map<String, AbstractDefinition> tableDefinitionMap,
                                       Map<String, AbstractDefinition> windowDefinitionMap,
                                       Map<String, AbstractDefinition> aggregationDefinitionMap,
                                       Map<String, Table> tableMap,
                                       Map<String, Window> windowMap,
                                       Map<String, AggregationRuntime> aggregationMap,
                                       List<VariableExpressionExecutor> executors,
                                       LatencyTracker latencyTracker, boolean outputExpectsExpiredEvents,
                                       String queryName) {
        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.setInputReferenceId(inputStore.getStoreReferenceId());

        Within within = null;
        Expression per = null;

        if (inputStore instanceof AggregationInputStore) {
            AggregationInputStore aggregationInputStore = (AggregationInputStore) inputStore;
            if (aggregationInputStore.getPer() != null && aggregationInputStore.getWithin() != null) {
                if (metaStreamEvent.getEventType() != EventType.AGGREGATE) {
                    throw new StoreQueryCreationException(inputStore.getStoreId() +
                            " is not an aggregation hence it cannot be processed with 'within' and 'per'.");
                }
                within = aggregationInputStore.getWithin();
                per = aggregationInputStore.getPer();
            } else if (aggregationInputStore.getPer() != null || aggregationInputStore.getWithin() != null) {
                throw new StoreQueryCreationException(inputStore.getStoreId() +
                        " should either have both 'within' and 'per' defined or none.");
            }
            ((AggregationInputStore) inputStore).getPer()
        }

        AbstractDefinition inputDefinition = tableDefinitionMap.get(inputStore.getStoreId());
        if (inputDefinition != null) {
            metaStreamEvent.setEventType(EventType.TABLE);
            initMetaStreamEvent(metaStreamEvent, inputDefinition);

          Table table=  tableMap.get(inputStore.getStoreId());
          table.compileCondition()
        } else {
            inputDefinition = aggregationDefinitionMap.get(inputStore.getStoreId());
            if (inputDefinition != null) {
                metaStreamEvent.setEventType(EventType.AGGREGATE);
                initMetaStreamEvent(metaStreamEvent, inputDefinition);

            } else {
                inputDefinition = windowDefinitionMap.get(inputStore.getStoreId());
                if (inputDefinition != null) {
                    metaStreamEvent.setEventType(EventType.WINDOW);
                    initMetaStreamEvent(metaStreamEvent, inputDefinition);

                } else {
                    throw new StoreQueryCreationException(inputStore.getStoreId() +
                            " is neither a table, aggregation or window");
                }
            }
        }



        if (inputStore instanceof Store) {
            Store store = (Store) inputStore;
//            ProcessStreamReceiver processStreamReceiver = new ProcessStreamReceiver(store.getStoreId(),
//                    latencyTracker, queryName);
//            processStreamReceiver.setBatchProcessingAllowed(batchProcessingAllowed);
            store.getStoreId()
            tableDefinitionMap.containsKey(store.getStoreId())
            MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
            metaStreamEvent.setEventType();
            return SingleInputStreamParser.parseInputStream((SingleInputStream) inputStream,
                    siddhiAppContext, executors, streamDefinitionMap,
                    null, windowDefinitionMap, aggregationDefinitionMap, tableMap,
                    new MetaStreamEvent(), processStreamReceiver,
                    true, outputExpectsExpiredEvents, queryName);
        } else if (inputStream instanceof JoinInputStream) {
            return JoinInputStreamParser.parseInputStream(((JoinInputStream) inputStream), siddhiAppContext,
                    streamDefinitionMap, tableDefinitionMap, windowDefinitionMap,
                    aggregationDefinitionMap, tableMap, windowMap, aggregationMap,
                    executors, latencyTracker, outputExpectsExpiredEvents,
                    queryName);
        } else if (inputStream instanceof StateInputStream) {
            MetaStateEvent metaStateEvent = new MetaStateEvent(inputStream.getAllStreamIds().size());
            return StateInputStreamParser.parseInputStream(((StateInputStream) inputStream), siddhiAppContext,
                    metaStateEvent, streamDefinitionMap, null,
                    null, aggregationDefinitionMap, tableMap, executors, latencyTracker,
                    queryName);
        } else {
            throw new OperationNotSupportedException();
        }
    }

    private static void initMetaStreamEvent(MetaStreamEvent metaStreamEvent, AbstractDefinition inputDefinition) {
        metaStreamEvent.addInputDefinition(inputDefinition);
        metaStreamEvent.initializeAfterWindowData();
        for (Attribute attribute : inputDefinition.getAttributeList()) {
            metaStreamEvent.addData(attribute);
        }
    }

}

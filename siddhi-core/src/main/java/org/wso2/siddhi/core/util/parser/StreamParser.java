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
package org.wso2.siddhi.core.util.parser;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.filter.FilterProcessor;
import org.wso2.siddhi.core.query.processor.filter.PassthruFilterProcessor;
import org.wso2.siddhi.core.query.processor.handler.HandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.SimpleHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.TableHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.pattern.AndPatternInnerHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.pattern.CountPatternInnerHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.pattern.OrPatternInnerHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.pattern.PatternHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.pattern.PatternInnerHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.sequence.CountSequenceInnerHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.sequence.OrSequenceInnerHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.sequence.SequenceHandlerProcessor;
import org.wso2.siddhi.core.query.processor.handler.sequence.SequenceInnerHandlerProcessor;
import org.wso2.siddhi.core.query.processor.join.JoinProcessor;
import org.wso2.siddhi.core.query.processor.join.LeftInStreamJoinProcessor;
import org.wso2.siddhi.core.query.processor.join.LeftRemoveStreamJoinProcessor;
import org.wso2.siddhi.core.query.processor.join.RightInStreamJoinProcessor;
import org.wso2.siddhi.core.query.processor.join.RightRemoveStreamJoinProcessor;
import org.wso2.siddhi.core.query.processor.transform.TransformProcessor;
import org.wso2.siddhi.core.query.processor.window.RunnableWindowProcessor;
import org.wso2.siddhi.core.query.processor.window.TableWindowProcessor;
import org.wso2.siddhi.core.query.processor.window.WindowProcessor;
import org.wso2.siddhi.core.query.statemachine.pattern.AndPatternState;
import org.wso2.siddhi.core.query.statemachine.pattern.CountPatternState;
import org.wso2.siddhi.core.query.statemachine.pattern.OrPatternState;
import org.wso2.siddhi.core.query.statemachine.pattern.PatternState;
import org.wso2.siddhi.core.query.statemachine.sequence.CountSequenceState;
import org.wso2.siddhi.core.query.statemachine.sequence.OrSequenceState;
import org.wso2.siddhi.core.query.statemachine.sequence.SequenceState;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.QueryPartComposite;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.extension.holder.TransformExtensionHolder;
import org.wso2.siddhi.core.extension.holder.WindowExtensionHolder;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.condition.ConditionValidator;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.query.QueryEventSource;
import org.wso2.siddhi.query.api.query.input.JoinStream;
import org.wso2.siddhi.query.api.query.input.Stream;
import org.wso2.siddhi.query.api.query.input.WindowStream;
import org.wso2.siddhi.query.api.query.input.handler.Filter;
import org.wso2.siddhi.query.api.query.input.handler.Window;
import org.wso2.siddhi.query.api.query.input.pattern.PatternStream;
import org.wso2.siddhi.query.api.query.input.sequence.SequenceStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StreamParser {

    static final Logger log = Logger.getLogger(StreamParser.class);

    public static QueryPartComposite parseSingleStream(Stream queryStream, QueryEventSource queryEventSource, List<QueryEventSource> queryEventSourceList, ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap,
                                                   ConcurrentMap<String, EventTable> eventTableMap, SiddhiContext siddhiContext) {
        QueryPartComposite queryPartComposite = new QueryPartComposite();

        SimpleHandlerProcessor simpleHandlerProcessor = new SimpleHandlerProcessor(queryEventSource,
                                                                                   generateFilerProcessor(queryEventSource, queryEventSourceList, streamTableDefinitionMap, eventTableMap, siddhiContext),
                                                                                   generateTransformProcessor(queryEventSource, queryEventSourceList, siddhiContext),
                                                                                   siddhiContext);

        if (queryStream instanceof WindowStream) {
            WindowProcessor windowProcessor = generateWindowProcessor(queryEventSource, siddhiContext, null, false);
            windowProcessor.initWindow();
            simpleHandlerProcessor.setNext(windowProcessor);
            queryPartComposite.getPreSelectProcessingElementList().add(windowProcessor);
        } else {
            queryPartComposite.getPreSelectProcessingElementList().add(simpleHandlerProcessor);
        }

        queryPartComposite.getHandlerProcessorList().add(simpleHandlerProcessor);
        return queryPartComposite;
    }

    public static QueryPartComposite parseJoinStream(Stream queryStream, QueryEventSource leftQueryEventSource, QueryEventSource rightQueryEventSource, List<QueryEventSource> queryEventSourceList,
                                                 ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap,
                                                 ConcurrentMap<String, EventTable> eventTableMap, SiddhiContext siddhiContext) {
        QueryPartComposite queryPartComposite = new QueryPartComposite();

        boolean fromDB = false;
        SimpleHandlerProcessor leftSimpleHandlerProcessor;
        if (leftQueryEventSource.getInDefinition() instanceof TableDefinition) {

            leftSimpleHandlerProcessor = new TableHandlerProcessor(leftQueryEventSource, siddhiContext);
            if (((TableDefinition) leftQueryEventSource.getInDefinition()).getExternalTable() != null) {
                fromDB = true;
            }
        } else {
            leftSimpleHandlerProcessor = new SimpleHandlerProcessor(leftQueryEventSource,
                                                                    generateFilerProcessor(leftQueryEventSource, queryEventSourceList, streamTableDefinitionMap, eventTableMap, siddhiContext),
                                                                    generateTransformProcessor(leftQueryEventSource, queryEventSourceList, siddhiContext),
                                                                    siddhiContext);
        }

        SimpleHandlerProcessor rightSimpleHandlerProcessor;
        if (rightQueryEventSource.getInDefinition() instanceof TableDefinition) {

            rightSimpleHandlerProcessor = new TableHandlerProcessor(rightQueryEventSource, siddhiContext);
            if (((TableDefinition) rightQueryEventSource.getInDefinition()).getExternalTable() != null) {
                fromDB = true;
            }
        } else {
            rightSimpleHandlerProcessor = new SimpleHandlerProcessor(rightQueryEventSource,
                                                                     generateFilerProcessor(rightQueryEventSource, queryEventSourceList, streamTableDefinitionMap, eventTableMap, siddhiContext),
                                                                     generateTransformProcessor(rightQueryEventSource, queryEventSourceList, siddhiContext),
                                                                     siddhiContext);
        }

        ConditionExecutor onConditionExecutor;
        if (((JoinStream) queryStream).getOnCompare() != null) {
            onConditionExecutor = ExecutorParser.parseCondition(((JoinStream) queryStream).getOnCompare(), queryEventSourceList, null, eventTableMap, false, siddhiContext);
        } else {
            onConditionExecutor = ExecutorParser.parseCondition(Condition.bool(Expression.value(true)), queryEventSourceList, null, eventTableMap, false, siddhiContext);
        }
        JoinProcessor leftInStreamJoinProcessor;
        JoinProcessor rightInStreamJoinProcessor;
        JoinProcessor leftRemoveStreamJoinProcessor;
        JoinProcessor rightRemoveStreamJoinProcessor;
        Lock lock;
        if (siddhiContext.isDistributedProcessingEnabled()) {
            lock = siddhiContext.getHazelcastInstance().getLock(siddhiContext.getElementIdGenerator().createNewId() + "-join-lock");
        } else {
            lock = new ReentrantLock();
        }
        switch (((JoinStream) queryStream).getTrigger()) {
            case LEFT:
                leftInStreamJoinProcessor = new LeftInStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessingEnabled(), lock, fromDB);
                rightInStreamJoinProcessor = new RightInStreamJoinProcessor(onConditionExecutor, false, siddhiContext.isDistributedProcessingEnabled(), lock, fromDB);
                leftRemoveStreamJoinProcessor = new LeftRemoveStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessingEnabled(), lock, fromDB);
                rightRemoveStreamJoinProcessor = new RightRemoveStreamJoinProcessor(onConditionExecutor, false, siddhiContext.isDistributedProcessingEnabled(), lock, fromDB);
                break;
            case RIGHT:
                leftInStreamJoinProcessor = new LeftInStreamJoinProcessor(onConditionExecutor, false, siddhiContext.isDistributedProcessingEnabled(), lock, fromDB);
                rightInStreamJoinProcessor = new RightInStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessingEnabled(), lock, fromDB);
                leftRemoveStreamJoinProcessor = new LeftRemoveStreamJoinProcessor(onConditionExecutor, false, siddhiContext.isDistributedProcessingEnabled(), lock, fromDB);
                rightRemoveStreamJoinProcessor = new RightRemoveStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessingEnabled(), lock, fromDB);
                break;
            default:
                leftInStreamJoinProcessor = new LeftInStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessingEnabled(), lock, fromDB);
                rightInStreamJoinProcessor = new RightInStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessingEnabled(), lock, fromDB);
                leftRemoveStreamJoinProcessor = new LeftRemoveStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessingEnabled(), lock, fromDB);
                rightRemoveStreamJoinProcessor = new RightRemoveStreamJoinProcessor(onConditionExecutor, true, siddhiContext.isDistributedProcessingEnabled(), lock, fromDB);
                break;
        }
        Constant within = ((JoinStream) queryStream).getWithin();
        if (within != null) {
            long longValue = ExecutorParser.getLong(within);
            leftInStreamJoinProcessor.setWithin(longValue);
            rightInStreamJoinProcessor.setWithin(longValue);
            leftRemoveStreamJoinProcessor.setWithin(longValue);
            rightRemoveStreamJoinProcessor.setWithin(longValue);
        }

        WindowProcessor leftWindowProcessor;
        if (leftQueryEventSource.getInDefinition() instanceof TableDefinition) {
            leftWindowProcessor = new TableWindowProcessor(eventTableMap.get(leftQueryEventSource.getSourceId()));
        } else {
            leftWindowProcessor = generateWindowProcessor(leftQueryEventSource, siddhiContext, lock, false);
        }

        WindowProcessor rightWindowProcessor;
        if (rightQueryEventSource.getInDefinition() instanceof TableDefinition) {
            rightWindowProcessor = new TableWindowProcessor(eventTableMap.get(rightQueryEventSource.getSourceId()));
        } else {
            rightWindowProcessor = generateWindowProcessor(rightQueryEventSource, siddhiContext, lock, false);
        }

        leftSimpleHandlerProcessor.setNext(leftInStreamJoinProcessor);
        rightSimpleHandlerProcessor.setNext(rightInStreamJoinProcessor);

        //joinStreamPacker next allocation
        queryPartComposite.getPreSelectProcessingElementList().add(leftInStreamJoinProcessor);
        queryPartComposite.getPreSelectProcessingElementList().add(rightInStreamJoinProcessor);
        queryPartComposite.getPreSelectProcessingElementList().add(leftRemoveStreamJoinProcessor);
        queryPartComposite.getPreSelectProcessingElementList().add(rightRemoveStreamJoinProcessor);

        //Window joinStreamPacker relation settings
        leftInStreamJoinProcessor.setWindowProcessor(leftWindowProcessor);
        leftWindowProcessor.setNext(leftRemoveStreamJoinProcessor);

        rightInStreamJoinProcessor.setWindowProcessor(rightWindowProcessor);
        rightWindowProcessor.setNext(rightRemoveStreamJoinProcessor);

        //init window
        leftWindowProcessor.initWindow();
        rightWindowProcessor.initWindow();

        //joinStreamPacker prev
        rightInStreamJoinProcessor.setOppositeWindowProcessor(leftInStreamJoinProcessor.getWindowProcessor());
        leftInStreamJoinProcessor.setOppositeWindowProcessor(rightInStreamJoinProcessor.getWindowProcessor());

        rightRemoveStreamJoinProcessor.setOppositeWindowProcessor(leftInStreamJoinProcessor.getWindowProcessor());
        leftRemoveStreamJoinProcessor.setOppositeWindowProcessor(rightInStreamJoinProcessor.getWindowProcessor());

        queryPartComposite.getHandlerProcessorList().add(leftSimpleHandlerProcessor);
        queryPartComposite.getHandlerProcessorList().add(rightSimpleHandlerProcessor);
        return queryPartComposite;
    }

    public static QueryPartComposite parsePatternStream(Stream queryStream, List<PatternState> patternStateList, List<QueryEventSource> queryEventSourceList, ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap,
                                                    ConcurrentMap<String, EventTable> eventTableMap, SiddhiContext siddhiContext) {
        QueryPartComposite queryPartComposite = new QueryPartComposite();

        Map<Integer, PatternInnerHandlerProcessor> statePatternInnerHandlerProcessorMap = new HashMap<Integer, PatternInnerHandlerProcessor>();

        for (String streamId : queryStream.getStreamIds()) {

            //    List<BasicStream> streamList = new ArrayList<BasicStream>();
            List<PatternInnerHandlerProcessor> patternInnerHandlerProcessorList = new ArrayList<PatternInnerHandlerProcessor>();
            for (PatternState state : patternStateList) {
                if (state.getTransformedStream().getStreamId().equals(streamId)) {
                    //           streamList.add(state.getTransformedStream());

                    QueryEventSource queryEventSource = state.getTransformedStream().getQueryEventSource();
                    FilterProcessor filterProcessor = generateFilerProcessor(queryEventSource, queryEventSourceList, streamTableDefinitionMap, eventTableMap, siddhiContext);

                    PatternInnerHandlerProcessor patternInnerHandlerProcessor;

                    if (state instanceof OrPatternState) {
                        patternInnerHandlerProcessor = new OrPatternInnerHandlerProcessor(((OrPatternState) state), filterProcessor, patternStateList.size(), siddhiContext,
                                                                                          siddhiContext.getElementIdGenerator().createNewId());
                    } else if (state instanceof AndPatternState) {
                        patternInnerHandlerProcessor = new AndPatternInnerHandlerProcessor(((AndPatternState) state), filterProcessor, patternStateList.size(), siddhiContext,
                                                                                           siddhiContext.getElementIdGenerator().createNewId());
                    } else if (state instanceof CountPatternState) {
                        patternInnerHandlerProcessor = new CountPatternInnerHandlerProcessor(((CountPatternState) state), filterProcessor, patternStateList.size(), siddhiContext,
                                                                                             siddhiContext.getElementIdGenerator().createNewId());
                    } else {
                        patternInnerHandlerProcessor = new PatternInnerHandlerProcessor(state, filterProcessor, patternStateList.size(), siddhiContext,
                                                                                        siddhiContext.getElementIdGenerator().createNewId());
                    }

                    statePatternInnerHandlerProcessorMap.put(state.getStateNumber(), patternInnerHandlerProcessor);
                    patternInnerHandlerProcessorList.add(patternInnerHandlerProcessor);
                    //  patternSingleStreamAnalyserArray[state.getStateNumber()] = patternInnerHandlerProcessor;

                    queryPartComposite.getPreSelectProcessingElementList().add(patternInnerHandlerProcessor);

                    //patternInnerHandlerProcessor.setPrevious(singleStreamPacker); since not needed not set
                }
            }

            PatternHandlerProcessor patternHandlerProcessor = new PatternHandlerProcessor(streamId, patternInnerHandlerProcessorList, siddhiContext);
            patternHandlerProcessor.setElementId(siddhiContext.getElementIdGenerator().createNewId());

            queryPartComposite.getHandlerProcessorList().add(patternHandlerProcessor);

            //for persistence, elementId marking and window
            for (PatternInnerHandlerProcessor patternInnerHandlerProcessor : patternInnerHandlerProcessorList) {
                if (((PatternStream) queryStream).getWithin() != null) {
                    patternInnerHandlerProcessor.setWithin(ExecutorParser.getLong(((PatternStream) queryStream).getWithin()));
                }
                siddhiContext.getSnapshotService().addSnapshotable(patternInnerHandlerProcessor);
            }

        }
        //   patternStreamPacker.setPatternSingleStreamAnalyserArray(patternSingleStreamAnalyserArray);
        //patternStreamPacker next
        //  patternStreamPacker.setNext(next, 0);

        for (PatternState state : patternStateList) {
            statePatternInnerHandlerProcessorMap.get(state.getStateNumber()).init(statePatternInnerHandlerProcessorMap);
        }
        return queryPartComposite;
    }

    public static QueryPartComposite parseSequenceStream(Stream queryStream, List<SequenceState> sequenceStateList, List<QueryEventSource> queryEventSourceList, ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap,
                                                     ConcurrentMap<String, EventTable> eventTableMap, SiddhiContext siddhiContext) {
        QueryPartComposite queryPartComposite = new QueryPartComposite();

        Map<Integer, SequenceInnerHandlerProcessor> stateSequenceInnerHandlerProcessorMap = new HashMap<Integer, SequenceInnerHandlerProcessor>();
        for (String streamId : queryStream.getStreamIds()) {

            //    List<BasicStream> streamList = new ArrayList<BasicStream>();
            List<SequenceInnerHandlerProcessor> sequenceInnerHandlerProcessorList = new ArrayList<SequenceInnerHandlerProcessor>();
            for (SequenceState state : sequenceStateList) {
                if (state.getTransformedStream().getStreamId().equals(streamId)) {
                    //           streamList.add(state.getTransformedStream());

                    QueryEventSource queryEventSource = state.getTransformedStream().getQueryEventSource();
                    FilterProcessor filterProcessor = generateFilerProcessor(queryEventSource, queryEventSourceList, streamTableDefinitionMap, eventTableMap, siddhiContext);

                    //update outputStreamDefinition as no transformer for sequence
                    queryEventSource.setOutDefinition(queryEventSource.getInDefinition());

                    SequenceInnerHandlerProcessor sequenceInnerHandlerProcessor;


                    if (state instanceof OrSequenceState) {
                        sequenceInnerHandlerProcessor = new OrSequenceInnerHandlerProcessor(((OrSequenceState) state), filterProcessor, sequenceStateList.size(),
                                                                                            siddhiContext, siddhiContext.getElementIdGenerator().createNewId());
                    } else if (state instanceof CountSequenceState) {
                        sequenceInnerHandlerProcessor = new CountSequenceInnerHandlerProcessor(((CountSequenceState) state), filterProcessor, sequenceStateList.size(),
                                                                                               siddhiContext, siddhiContext.getElementIdGenerator().createNewId());
                    } else {
                        sequenceInnerHandlerProcessor = new SequenceInnerHandlerProcessor(state, filterProcessor, sequenceStateList.size(),
                                                                                          siddhiContext, siddhiContext.getElementIdGenerator().createNewId());
                    }

                    stateSequenceInnerHandlerProcessorMap.put(state.getStateNumber(), sequenceInnerHandlerProcessor);
//                    state.setSequenceInnerHandlerProcessor(sequenceInnerHandlerProcessor);
                    sequenceInnerHandlerProcessorList.add(sequenceInnerHandlerProcessor);
                    //  patternSingleStreamAnalyserArray[state.getStateNumber()] = patternSingleStreamAnalyser;

                    queryPartComposite.getPreSelectProcessingElementList().add(sequenceInnerHandlerProcessor);

                }
            }

            SequenceHandlerProcessor sequenceHandlerProcessor = new SequenceHandlerProcessor(streamId, sequenceInnerHandlerProcessorList, siddhiContext);
            sequenceHandlerProcessor.setElementId(siddhiContext.getElementIdGenerator().createNewId());

            queryPartComposite.getHandlerProcessorList().add(sequenceHandlerProcessor);

            //for persistence, elementId marking and window
            for (SequenceInnerHandlerProcessor sequenceInnerHandlerProcessor : sequenceInnerHandlerProcessorList) {
                if (((SequenceStream) queryStream).getWithin() != null) {
                    sequenceInnerHandlerProcessor.setWithin(ExecutorParser.getLong(((SequenceStream) queryStream).getWithin()));
                }
                siddhiContext.getSnapshotService().addSnapshotable(sequenceInnerHandlerProcessor);
            }
        }

        //   patternStreamPacker.setPatternSingleStreamAnalyserArray(patternSingleStreamAnalyserArray);
        //patternStreamPacker next
        //  patternStreamPacker.setNext(next, 0);

        for (SequenceState state : sequenceStateList) {
            stateSequenceInnerHandlerProcessorMap.get(state.getStateNumber()).init(stateSequenceInnerHandlerProcessorMap);
        }

        for (HandlerProcessor queryStreamProcessor : queryPartComposite.getHandlerProcessorList()) {
            List<SequenceInnerHandlerProcessor> otherStreamAnalyserList = new ArrayList<SequenceInnerHandlerProcessor>();
            for (HandlerProcessor otherQueryStreamProcessor : queryPartComposite.getHandlerProcessorList()) {
                if (otherQueryStreamProcessor != queryStreamProcessor) {
                    otherStreamAnalyserList.addAll(((SequenceHandlerProcessor) otherQueryStreamProcessor).
                            getSequenceInnerHandlerProcessorList());
                }
            }
            ((SequenceHandlerProcessor) queryStreamProcessor).setOtherSequenceInnerHandlerProcessorList(otherStreamAnalyserList);
        }

        return queryPartComposite;
    }


    private static FilterProcessor generateFilerProcessor(QueryEventSource queryEventSource,
                                                          List<QueryEventSource> queryEventSourceList,
                                                          ConcurrentMap<String, AbstractDefinition> streamTableDefinitionMap, ConcurrentMap<String, EventTable> eventTableMap, SiddhiContext siddhiContext) {
        Filter filter = queryEventSource.getFilter();
        if (filter == null) {
            return new PassthruFilterProcessor();
        } else {
            Condition condition = filter.getFilterCondition();
            ConditionValidator.validate(condition, queryEventSourceList, streamTableDefinitionMap, queryEventSource.getReferenceSourceId(), true);
            return new FilterProcessor(ExecutorParser.parseCondition(condition, queryEventSourceList, queryEventSource.getReferenceSourceId(), eventTableMap, true, siddhiContext));
        }


    }

    private static TransformProcessor generateTransformProcessor(QueryEventSource queryEventSource,
                                                                 List<QueryEventSource> queryEventSourceList,
                                                                 SiddhiContext siddhiContext) {
        if (queryEventSource.getTransformer() == null) {
            return null;
        }
        TransformProcessor transformProcessor = (TransformProcessor) SiddhiClassLoader.loadProcessor(queryEventSource.getTransformer().getName(), queryEventSource.getTransformer().getExtension(),
                                                                                                     TransformProcessor.class, TransformExtensionHolder.getInstance(siddhiContext));

        siddhiContext.addEternalReferencedHolder(transformProcessor);
        transformProcessor.setSiddhiContext(siddhiContext);
        transformProcessor.setInStreamDefinition((StreamDefinition) queryEventSource.getInDefinition());
        List<ExpressionExecutor> expressionExecutors = new LinkedList<ExpressionExecutor>();
        for (Expression expression : queryEventSource.getTransformer().getParameters()) {
            expressionExecutors.add(ExecutorParser.parseExpression(expression, queryEventSourceList, queryEventSource.getReferenceSourceId(), true, siddhiContext));
        }
        transformProcessor.setExpressionExecutors(expressionExecutors);
        transformProcessor.setParameters(queryEventSource.getTransformer().getParameters());

        //for adding elementId
        transformProcessor.setElementId(siddhiContext.getElementIdGenerator().createNewId());

        //for persistence
        siddhiContext.getSnapshotService().addSnapshotable(transformProcessor);
//        queryEventSource.setOutDefinition(transformProcessor.getOutStreamDefinition());
//        updateOutDefinitionsToQueryEventStreams(queryEventStreamList, siddhiContext);

        transformProcessor.initTransformProcessor();
        return transformProcessor;
    }

//    private static void updateOutDefinitionsToQueryEventStreams(
//            List<QueryEventStream> queryEventStreamList, SiddhiContext siddhiContext) {
//        for (QueryEventStream queryEventStream : queryEventStreamList) {
//            if (queryEventStream.getTransformer() == null) {
//                queryEventStream.setOutDefinition(queryEventStream.getInDefinition());
//            } else {
//                Transformer transformer = queryEventStream.getTransformer();
//                TransformProcessor transformProcessor = (TransformProcessor) SiddhiClassLoader.loadProcessor(transformer.getName(), transformer.getExtension(), TransformProcessor.class,
//                                                                                                             TransformExtensionHolder.getInstance(siddhiContext));
//                queryEventStream.setOutDefinition(transformProcessor.getOutDefinition());
//            }
//        }
//    }

//    private static void connectToStreamFlow(List<QueryStreamProcessor> queryStreamProcessorList,
//                                            QueryStreamProcessor queryStreamProcessor) {
//        if (queryStreamProcessorList.size() > 0) {
//            QueryStreamHandler prevStreamHandler = (QueryStreamHandler) queryStreamProcessorList.get(queryStreamProcessorList.size() - 1);
//            prevStreamHandler.setNext(queryStreamProcessor);
//        }
//        queryStreamProcessorList.add(queryStreamProcessor);
//    }

    private static WindowProcessor generateWindowProcessor(QueryEventSource queryEventSource,
                                                           SiddhiContext siddhiContext, Lock lock,
                                                           boolean async) {
        Window window = queryEventSource.getWindow();
        if (window == null) {
            window = new Window("length", new Expression[]{Expression.value(Integer.MAX_VALUE)});
        }
        WindowProcessor windowProcessor = (WindowProcessor) SiddhiClassLoader.loadProcessor(window.getName(), window.getExtension(),
                                                                                            WindowProcessor.class, WindowExtensionHolder.getInstance(siddhiContext));

        siddhiContext.addEternalReferencedHolder(windowProcessor);

//                    Window window = new TimeWindowProcessor();
        windowProcessor.setSiddhiContext(siddhiContext);
        windowProcessor.setDefinition(queryEventSource.getOutDefinition());
        if (windowProcessor instanceof RunnableWindowProcessor) {
            ((RunnableWindowProcessor) windowProcessor).setScheduledExecutorService(siddhiContext.getScheduledExecutorService());
            ((RunnableWindowProcessor) windowProcessor).setThreadBarrier(siddhiContext.getThreadBarrier());
        }
        windowProcessor.setParameters(window.getParameters());

        //for adding elementId
        windowProcessor.setElementId(siddhiContext.getElementIdGenerator().createNewId());

        if (lock == null) {
            if (siddhiContext.isDistributedProcessingEnabled()) {
                windowProcessor.setLock(siddhiContext.getHazelcastInstance().getLock(windowProcessor.getElementId() + "-lock"));
            } else {
                windowProcessor.setLock(new ReentrantLock());
            }
        } else {
            windowProcessor.setLock(lock);
        }
        //for persistence
        siddhiContext.getSnapshotService().addSnapshotable(windowProcessor);
        windowProcessor.setAsync(async);
        return windowProcessor;
    }


    public static void updateQueryEventSourceOutDefinition(QueryEventSource queryEventSource,
                                                           List<QueryEventSource> queryEventSourceList,
                                                           SiddhiContext siddhiContext) {
        if (queryEventSource.getTransformer() == null) {
            queryEventSource.setOutDefinition(queryEventSource.getInDefinition());
            return;
        }
        TransformProcessor transformProcessor = (TransformProcessor) SiddhiClassLoader.loadProcessor(queryEventSource.getTransformer().getName(), queryEventSource.getTransformer().getExtension(),
                                                                                                     TransformProcessor.class, TransformExtensionHolder.getInstance(siddhiContext));
        siddhiContext.addEternalReferencedHolder(transformProcessor);
        transformProcessor.setSiddhiContext(siddhiContext);
        transformProcessor.setInStreamDefinition((StreamDefinition) queryEventSource.getInDefinition());
        List<ExpressionExecutor> expressionExecutors = new LinkedList<ExpressionExecutor>();
        for (Expression expression : queryEventSource.getTransformer().getParameters()) {
            expressionExecutors.add(ExecutorParser.parseExpression(expression, queryEventSourceList, queryEventSource.getReferenceSourceId(), true, siddhiContext));
        }
        transformProcessor.setExpressionExecutors(expressionExecutors);
        transformProcessor.setParameters(queryEventSource.getTransformer().getParameters());

        //for adding elementId
        transformProcessor.setElementId(siddhiContext.getElementIdGenerator().createNewId());
        transformProcessor.initTransformProcessor();

        //for persistence
        siddhiContext.getSnapshotService().addSnapshotable(transformProcessor);
        queryEventSource.setOutDefinition(transformProcessor.getOutStreamDefinition());
//        updateOutDefinitionsToQueryEventStreams(queryEventStreamList, siddhiContext);
    }

}

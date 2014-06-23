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
package org.wso2.siddhi.core.query.selector;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.BundleEvent;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.ListEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.in.InStream;
import org.wso2.siddhi.core.event.remove.RemoveStream;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.core.extension.holder.ExecutorExtensionHolder;
import org.wso2.siddhi.core.extension.holder.OutputAttributeExtensionHolder;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateManager;
import org.wso2.siddhi.core.query.selector.attribute.factory.OutputAttributeAggregatorFactory;
import org.wso2.siddhi.core.query.selector.attribute.processor.AbstractAggregationAttributeProcessor;
import org.wso2.siddhi.core.query.selector.attribute.processor.AttributeProcessor;
import org.wso2.siddhi.core.query.selector.attribute.processor.AttributeProcessorFactory;
import org.wso2.siddhi.core.query.selector.attribute.processor.GroupByAttributeProcessor;
import org.wso2.siddhi.core.query.selector.attribute.processor.NonGroupingAttributeProcessor;
import org.wso2.siddhi.core.query.selector.attribute.processor.PassThroughAttributeProcessor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.LogHelper;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.util.parser.ExecutorParser;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.ExpressionExtension;
import org.wso2.siddhi.query.api.extension.Extension;
import org.wso2.siddhi.query.api.query.QueryEventSource;
import org.wso2.siddhi.query.api.query.input.handler.Filter;
import org.wso2.siddhi.query.api.query.selection.Selector;
import org.wso2.siddhi.query.api.query.selection.attribute.ComplexAttribute;
import org.wso2.siddhi.query.api.query.selection.attribute.OutputAttribute;
import org.wso2.siddhi.query.api.query.selection.attribute.OutputAttributeExtension;
import org.wso2.siddhi.query.api.query.selection.attribute.SimpleAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class QuerySelector implements QueryPostProcessingElement {

    static final Logger log = Logger.getLogger(QuerySelector.class);

    //    private List<Object[]> dataList = new ArrayList<Object[]>();
    private List<AttributeProcessor> attributeProcessorList;
    private List<AttributeProcessor> aggregateAttributeProcessorList;
    private int outputSize;
    private String outputStreamId;
    private StreamDefinition outputStreamDefinition;
    private Selector selector;
    private ConditionExecutor havingConditionExecutor = null;
    //    private OutStream outStream;
    public boolean currentOn = false;
    public boolean expiredOn = false;
    public OutputGroupByKeyGenerator groupByKeyGenerator = null;
    private boolean groupBy = false;
    private boolean distributedProcessing = false;
    private final OutputRateManager outputRateManager;

    public QuerySelector(String outputStreamId, Selector selector,
                         OutputRateManager outputRateManager, List<QueryEventSource> queryEventSourceList,
                         ConcurrentMap<String, EventTable> eventTableMap, SiddhiContext siddhiContext, boolean currentOn, boolean expiredOn) {
        this.outputStreamId = outputStreamId;
        this.currentOn = currentOn;
        this.expiredOn = expiredOn;
        this.selector = selector;

        outputSize = selector.getSelectionList().size();
        attributeProcessorList = new ArrayList<AttributeProcessor>(outputSize);
        aggregateAttributeProcessorList = new ArrayList<AttributeProcessor>(outputSize);
        outputStreamDefinition = new StreamDefinition();
        outputStreamDefinition.name(outputStreamId);

        if (selector.getGroupByList().size() > 0) {
            groupBy = true;
            groupByKeyGenerator = new OutputGroupByKeyGenerator(selector.getGroupByList(), queryEventSourceList, siddhiContext);
        }

        populateOutputAttributes(queryEventSourceList, siddhiContext);

        havingConditionExecutor = generateHavingExecutor(selector.getHavingCondition(), outputStreamDefinition, eventTableMap, siddhiContext);

        distributedProcessing = siddhiContext.isDistributedProcessingEnabled();

        this.outputRateManager = outputRateManager;

    }

    private ConditionExecutor generateHavingExecutor(Condition condition,
                                                     StreamDefinition outputStreamDefinition,
                                                     ConcurrentMap<String, EventTable> eventTableMap, SiddhiContext siddhiContext) {
        if (condition != null) {
            List<QueryEventSource> queryEventSourceList = new ArrayList<QueryEventSource>();
            queryEventSourceList.add(new QueryEventSource(null, null, outputStreamDefinition, new Filter(condition), null, null));
            return ExecutorParser.parseCondition(condition, queryEventSourceList, outputStreamId, eventTableMap, true, siddhiContext);
        }
        return null;
    }

    private void populateOutputAttributes(List<QueryEventSource> queryEventSourceList,
                                          SiddhiContext siddhiContext) {
        for (OutputAttribute outputAttribute : selector.getSelectionList()) {
            if (outputAttribute instanceof SimpleAttribute) {
                PassThroughAttributeProcessor attributeGenerator = new PassThroughAttributeProcessor(ExecutorParser.parseExpression(((SimpleAttribute) outputAttribute).getExpression(), queryEventSourceList, null, false, siddhiContext));
                attributeProcessorList.add(attributeGenerator);
                outputStreamDefinition.attribute(outputAttribute.getRename(), attributeGenerator.getOutputType());
            } else {
                OutputAttributeAggregatorFactory outputAttributeAggregatorFactory;
                if (outputAttribute instanceof ComplexAttribute) {
                    try {
                        outputAttributeAggregatorFactory = (OutputAttributeAggregatorFactory) SiddhiClassLoader.loadSiddhiImplementation(((ComplexAttribute) outputAttribute).getAttributeName(), OutputAttributeAggregatorFactory.class);
                    } catch (QueryCreationException e) {
                        if (e.isClassLoadingIssue()) {
                            try {
                                Expression expression = Expression.function(((ComplexAttribute) outputAttribute).getAttributeName(), ((ComplexAttribute) outputAttribute).getExpressions());
                                PassThroughAttributeProcessor attributeGenerator = new PassThroughAttributeProcessor(ExecutorParser.parseExpression(expression, queryEventSourceList, null, false, siddhiContext));
                                attributeProcessorList.add(attributeGenerator);
                                outputStreamDefinition.attribute(outputAttribute.getRename(), attributeGenerator.getOutputType());
                                continue;
                            } catch (QueryCreationException ex) {
                                if (ex.isClassLoadingIssue()) {
                                    throw new QueryCreationException("No extension exist in for " + ((ComplexAttribute) outputAttribute).getAttributeName() + ": " + e.getMessage() + " or " + ex.getMessage());
                                } else {
                                    throw new QueryCreationException("Cannot create ExpressionFunction " + ex.getMessage(), ex);
                                }
                            }
                        } else {
                            throw new QueryCreationException("Cannot create OutputAttributeAggregatorFactory, " + e.getMessage(), e);
                        }
                    }
                } else {//extension
                    if (null != OutputAttributeExtensionHolder.getInstance(siddhiContext).getExtension(((Extension) outputAttribute).getNamespace(), ((Extension) outputAttribute).getFunction())) {
                        outputAttributeAggregatorFactory = (OutputAttributeAggregatorFactory) SiddhiClassLoader.loadExtensionImplementation(((OutputAttributeExtension) outputAttribute), OutputAttributeExtensionHolder.getInstance(siddhiContext));
                    } else if (null != ExecutorExtensionHolder.getInstance(siddhiContext).getExtension(((Extension) outputAttribute).getNamespace(), ((Extension) outputAttribute).getFunction())) {
                        ExpressionExtension expressionExtension = new ExpressionExtension(((Extension) outputAttribute).getNamespace(), ((Extension) outputAttribute).getFunction(), ((OutputAttributeExtension) outputAttribute).getExpressions());
                        PassThroughAttributeProcessor attributeGenerator = new PassThroughAttributeProcessor(ExecutorParser.parseExpression(expressionExtension, queryEventSourceList, null, false, siddhiContext));
                        attributeProcessorList.add(attributeGenerator);
                        outputStreamDefinition.attribute(outputAttribute.getRename(), attributeGenerator.getOutputType());
                        continue;
                    } else {
                        throw new QueryCreationException("No extension exist for " + outputAttribute);
                    }
                }
                Expression[] expressions = null;
                if (outputAttribute instanceof ComplexAttribute) {
                    expressions = ((ComplexAttribute) outputAttribute).getExpressions();
                } else {
                    expressions = ((OutputAttributeExtension) outputAttribute).getExpressions();
                }
                AttributeProcessor attributeProcessor = AttributeProcessorFactory.createAttributeProcessor(expressions, queryEventSourceList, outputAttributeAggregatorFactory, siddhiContext, groupBy);
                //for persistence
                siddhiContext.getSnapshotService().addSnapshotable((AbstractAggregationAttributeProcessor) attributeProcessor);
                aggregateAttributeProcessorList.add(attributeProcessor);
                attributeProcessorList.add(attributeProcessor);
                outputStreamDefinition.attribute(outputAttribute.getRename(), attributeProcessor.getOutputType());
            }
        }


    }


    public StreamDefinition getOutputStreamDefinition() {
        return outputStreamDefinition;
    }

    public void process(AtomicEvent atomicEvent) {
        LogHelper.logMethod(log, atomicEvent);
        try {
            String groupByKey = null;
            if (groupBy) {
                groupByKey = groupByKeyGenerator.constructEventKey(atomicEvent);
            }
            if ((!(atomicEvent instanceof InStream) || !currentOn) && (!(atomicEvent instanceof RemoveStream) || !expiredOn)) {
                for (AttributeProcessor attributeProcessor : aggregateAttributeProcessorList) {
                    processOutputAttributeGenerator(atomicEvent, groupByKey, attributeProcessor);
                }
                return;
            }

            Object[] data = new Object[outputSize];
            for (int i = 0; i < outputSize; i++) {
                AttributeProcessor attributeProcessor = attributeProcessorList.get(i);
                data[i] = processOutputAttributeGenerator(atomicEvent, groupByKey, attributeProcessor);
            }

            //   dataList.add(data);
//            if (outputStreamJunction != null) {
            StreamEvent event = null;
            if (havingConditionExecutor == null) {
                if (atomicEvent instanceof InStream) {
                    event = new InEvent(outputStreamId, atomicEvent.getTimeStamp(), data);
                    outputRateManager.send(event.getTimeStamp(), event, null, groupByKey);
                } else {
                    event = new InEvent(outputStreamId, ((RemoveStream) atomicEvent).getExpiryTime(), data);
                    outputRateManager.send(event.getTimeStamp(), null, event, groupByKey);
                }
            } else {
                if (atomicEvent instanceof InStream) {
                    event = new InEvent(outputStreamId, atomicEvent.getTimeStamp(), data);
                    if (havingConditionExecutor.execute((AtomicEvent) event)) {
                        outputRateManager.send(event.getTimeStamp(), event, null, groupByKey);
                    }
                } else {
                    event = new InEvent(outputStreamId, ((RemoveStream) atomicEvent).getExpiryTime(), data);
                    if (havingConditionExecutor.execute((AtomicEvent) event)) {
                        outputRateManager.send(event.getTimeStamp(), null, event, groupByKey);
                    }
                }
            }

//            }
        } catch (ClassCastException e) {
            log.error("Input event attribute type " + e.getMessage() + " type defined in the stream definition!", e);
        }
    }

    public void process(BundleEvent bundleEvent) {

        if (distributedProcessing) {
            //to improve performance
            for (AttributeProcessor attributeProcessor : aggregateAttributeProcessorList) {
                attributeProcessor.lock();
            }
        }
        String groupByKey = null;
        try {
            if ((!(bundleEvent instanceof InStream) || !currentOn) && (!(bundleEvent instanceof RemoveStream) || !expiredOn)) {
                for (int i = 0, eventsLength = bundleEvent.getActiveEvents(); i < eventsLength; i++) {
                    AtomicEvent event = bundleEvent.getEvent(i);
                    if (groupBy) {
                        groupByKey = groupByKeyGenerator.constructEventKey(event);
                    }
                    for (AttributeProcessor attributeProcessor : aggregateAttributeProcessorList) {
                        processOutputAttributeGenerator(event, groupByKey, attributeProcessor);
                    }
                }
                return;
            }
            if (!groupBy) {
                if (aggregateAttributeProcessorList.size() > 0) {
                    for (int i = 0, iterateLength = bundleEvent.getActiveEvents() - 1; i < iterateLength; i++) {
                        for (AttributeProcessor attributeProcessor : aggregateAttributeProcessorList) {
                            processOutputAttributeGenerator(bundleEvent.getEvent(i), null, attributeProcessor);
                        }
                    }
                    process(bundleEvent.getEvent(bundleEvent.getActiveEvents() - 1));
                } else {
                    List<Object[]> groupedEventData = new ArrayList<Object[]>();
                    for (int j = 0, eventsLength = bundleEvent.getActiveEvents(); j < eventsLength; j++) {
                        AtomicEvent event = bundleEvent.getEvent(j);
                        Object[] data = new Object[outputSize];
                        for (int i = 0; i < outputSize; i++) {
                            AttributeProcessor attributeProcessor = attributeProcessorList.get(i);
                            data[i] = processOutputAttributeGenerator(event, null, attributeProcessor);
                        }
                        groupedEventData.add(data);
                    }
                    sendEvents(bundleEvent, groupedEventData, groupByKey);
                }
            } else {
                LinkedHashMap<String, Object[]> groupedEvents = new LinkedHashMap<String, Object[]>();
                for (int j = 0, eventsLength = bundleEvent.getActiveEvents(); j < eventsLength; j++) {
                    AtomicEvent event = bundleEvent.getEvent(j);
                    groupByKey = groupByKeyGenerator.constructEventKey(event);
                    Object[] data = new Object[outputSize];
                    for (int i = 0; i < outputSize; i++) {
                        AttributeProcessor attributeProcessor = attributeProcessorList.get(i);
                        data[i] = processOutputAttributeGenerator(event, groupByKey, attributeProcessor);
                    }
                    groupedEvents.put(groupByKey, data);
                }
                sendEvents(bundleEvent, groupedEvents.values(), groupByKey);

            }
        } catch (ClassCastException e) {
            log.error("Input event attribute type " + e.getMessage() + " type defined in the stream definition!", e);
        }

        if (distributedProcessing) {
            for (AttributeProcessor attributeProcessor : aggregateAttributeProcessorList) {
                attributeProcessor.unlock();
            }
        }
    }

    private void sendEvents(ComplexEvent referenceEvent,
                            Collection<Object[]> groupedEventData, String groupByKey) {
        if (outputRateManager.hasCallBack()) {
            long timeStamp;
            if (referenceEvent instanceof InStream) {
                timeStamp = referenceEvent.getTimeStamp();
            } else {
                timeStamp = ((RemoveStream) referenceEvent).getExpiryTime();
            }
            ListEvent outputListEvent = new InListEvent(groupedEventData.size());
            if (havingConditionExecutor == null) {
                for (Object[] data : groupedEventData) {
                    outputListEvent.addEvent(new InEvent(outputStreamId, timeStamp, data));
                }
            } else {
                for (Object[] data : groupedEventData) {
                    Event event = new InEvent(outputStreamId, timeStamp, data);
                    if (havingConditionExecutor.execute(event)) {
                        outputListEvent.addEvent(event);
                    }
                }
            }
            if (outputListEvent.getActiveEvents() > 0) {
                if (referenceEvent instanceof InStream) {
                    outputRateManager.send(outputListEvent.getTimeStamp(), outputListEvent, null, groupByKey);
                } else {
                    outputRateManager.send(outputListEvent.getTimeStamp(), null, outputListEvent, groupByKey);
                }
            }
        }
    }

    private Object processOutputAttributeGenerator(AtomicEvent atomicEvent, String groupByKey,
                                                   AttributeProcessor attributeProcessor) {
        if (attributeProcessor instanceof NonGroupingAttributeProcessor) {
            return ((NonGroupingAttributeProcessor) attributeProcessor).process(atomicEvent);
        } else {
            return ((GroupByAttributeProcessor) attributeProcessor).process(atomicEvent, groupByKey);
        }
    }

    public List<Integer> getAggregateAttributePositionList() {
        List<Integer> aggregateAttributePositionList = new ArrayList<Integer>();
        for (int i = 0; i < attributeProcessorList.size(); i++) {
            AttributeProcessor attributeProcessor = attributeProcessorList.get(i);
            if (attributeProcessor instanceof AbstractAggregationAttributeProcessor) {
                aggregateAttributePositionList.add(i);
            }
        }
        return aggregateAttributePositionList;
    }

    public int getAttributeSize() {
        return attributeProcessorList.size();
    }
}


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
package org.wso2.siddhi.core.query.selector;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.GroupedComplexEvent;
import org.wso2.siddhi.core.event.state.populater.StateEventPopulator;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.condition.ConditionExpressionExecutor;
import org.wso2.siddhi.core.query.output.ratelimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.selector.attribute.processor.AttributeProcessor;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuerySelector implements Processor {


    private static final Logger log = Logger.getLogger(QuerySelector.class);
    private static final ThreadLocal<String> keyThreadLocal = new ThreadLocal<String>();
    private Selector selector;
    private ExecutionPlanContext executionPlanContext;
    private boolean currentOn = false;
    private boolean expiredOn = false;
    private boolean containsAggregator = false;
    private OutputRateLimiter outputRateLimiter;
    private List<AttributeProcessor> attributeProcessorList;
    private ConditionExpressionExecutor havingConditionExecutor = null;
    private boolean isGroupBy = false;
    private GroupByKeyGenerator groupByKeyGenerator;
    private String id;
    private StateEventPopulator eventPopulator;
    private boolean batchingEnabled = true;

    public QuerySelector(String id, Selector selector, boolean currentOn, boolean expiredOn, ExecutionPlanContext executionPlanContext) {
        this.id = id;
        this.currentOn = currentOn;
        this.expiredOn = expiredOn;
        this.selector = selector;
        this.executionPlanContext = executionPlanContext;
    }

    public static String getThreadLocalGroupByKey() {
        return keyThreadLocal.get();
    }

    @Override
    public void process(ComplexEventChunk complexEventChunk) {

        if (log.isTraceEnabled()) {
            log.trace("event is processed by selector " + id + this);
        }
        if (containsAggregator && complexEventChunk.isBatch() && batchingEnabled) {
            if (isGroupBy) {
                processInBatchGroupBy(complexEventChunk);
            } else {
                processInBatchNoGroupBy(complexEventChunk);
            }
        } else {
            if (isGroupBy) {
                processGroupBy(complexEventChunk);
            } else {
                processNoGroupBy(complexEventChunk);
            }
        }

    }

    private void processNoGroupBy(ComplexEventChunk complexEventChunk) {
        complexEventChunk.reset();
        synchronized (this) {
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                switch (event.getType()) {

                    case CURRENT:
                    case EXPIRED:
                        eventPopulator.populateStateEvent(event);
                        for (AttributeProcessor attributeProcessor : attributeProcessorList) {
                            attributeProcessor.process(event);
                        }
                        if (((event.getType() != StreamEvent.Type.CURRENT || !currentOn) && (event.getType() != StreamEvent.Type.EXPIRED || !expiredOn)) || ((havingConditionExecutor != null && !havingConditionExecutor.execute(event)))) {
                            complexEventChunk.remove();
                        }
                        break;
                    case RESET:
                        for (AttributeProcessor attributeProcessor : attributeProcessorList) {
                            attributeProcessor.process(event);
                        }
                    case TIMER:
                        complexEventChunk.remove();
                        break;
                }
            }
        }
        complexEventChunk.reset();
        if (complexEventChunk.hasNext()) {
            outputRateLimiter.process(complexEventChunk);
        }
    }

    private void processGroupBy(ComplexEventChunk complexEventChunk) {
        complexEventChunk.reset();

        ComplexEventChunk<ComplexEvent> currentComplexEventChunk = new ComplexEventChunk<ComplexEvent>(complexEventChunk.isBatch());

        synchronized (this) {
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                switch (event.getType()) {

                    case CURRENT:
                    case EXPIRED:
                        eventPopulator.populateStateEvent(event);
                        String groupedByKey = groupByKeyGenerator.constructEventKey(event);
                        keyThreadLocal.set(groupedByKey);

                        for (AttributeProcessor attributeProcessor : attributeProcessorList) {
                            attributeProcessor.process(event);
                        }
                        if ((event.getType() == StreamEvent.Type.CURRENT && currentOn) || (event.getType() == StreamEvent.Type.EXPIRED && expiredOn)) {
                            if (!(havingConditionExecutor != null && !havingConditionExecutor.execute(event))) {
                                complexEventChunk.remove();
                                currentComplexEventChunk.add(new GroupedComplexEvent(groupedByKey, event));
                            }
                        }
                        keyThreadLocal.remove();
                        break;
                    case TIMER:
                        break;
                    case RESET:
                        for (AttributeProcessor attributeProcessor : attributeProcessorList) {
                            attributeProcessor.process(event);
                        }
                        break;
                }
            }
        }
        currentComplexEventChunk.reset();
        if (currentComplexEventChunk.hasNext()) {
            outputRateLimiter.process(currentComplexEventChunk);
        }
    }

    private void processInBatchNoGroupBy(ComplexEventChunk complexEventChunk) {
        complexEventChunk.reset();
        ComplexEvent lastEvent = null;

        synchronized (this) {
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                switch (event.getType()) {
                    case CURRENT:
                    case EXPIRED:
                        eventPopulator.populateStateEvent(event);
                        for (AttributeProcessor attributeProcessor : attributeProcessorList) {
                            attributeProcessor.process(event);
                        }
                        if (!(havingConditionExecutor != null && !havingConditionExecutor.execute(event))) {
                            if ((event.getType() == StreamEvent.Type.CURRENT && currentOn) || (event.getType() == StreamEvent.Type.EXPIRED && expiredOn)) {
                                complexEventChunk.remove();
                                lastEvent = event;
                            }
                        }
                        break;
                    case TIMER:
                        break;
                    case RESET:
                        for (AttributeProcessor attributeProcessor : attributeProcessorList) {
                            attributeProcessor.process(event);
                        }
                        break;
                }
            }
        }

        if (lastEvent != null) {
            complexEventChunk.clear();
            complexEventChunk.add(lastEvent);
            outputRateLimiter.process(complexEventChunk);
        }
    }

    private void processInBatchGroupBy(ComplexEventChunk complexEventChunk) {
        Map<String, ComplexEvent> groupedEvents = new LinkedHashMap<String, ComplexEvent>();
        complexEventChunk.reset();

        synchronized (this) {
            while (complexEventChunk.hasNext()) {
                ComplexEvent event = complexEventChunk.next();
                switch (event.getType()) {

                    case CURRENT:
                    case EXPIRED:
                        eventPopulator.populateStateEvent(event);
                        String groupByKey = groupByKeyGenerator.constructEventKey(event);
                        keyThreadLocal.set(groupByKey);

                        for (AttributeProcessor attributeProcessor : attributeProcessorList) {
                            attributeProcessor.process(event);
                        }

                        if (!(havingConditionExecutor != null && !havingConditionExecutor.execute(event))) {
                            if ((event.getType() == StreamEvent.Type.CURRENT && currentOn) || (event.getType() == StreamEvent.Type.EXPIRED && expiredOn)) {
                                complexEventChunk.remove();
                                groupedEvents.put(groupByKey, event);
                            }
                        }
                        keyThreadLocal.remove();
                        break;
                    case TIMER:
                        break;
                    case RESET:
                        for (AttributeProcessor attributeProcessor : attributeProcessorList) {
                            attributeProcessor.process(event);
                        }
                        break;
                }
            }
        }

        if (groupedEvents.size() != 0) {
            complexEventChunk.clear();
            for (Map.Entry<String, ComplexEvent> groupedEventEntry : groupedEvents.entrySet()) {
                complexEventChunk.add(new GroupedComplexEvent(groupedEventEntry.getKey(), groupedEventEntry.getValue()));
            }
            complexEventChunk.reset();
            outputRateLimiter.process(complexEventChunk);
        }
    }

    @Override
    public Processor getNextProcessor() {
        return null;    //since there is no processors after a query selector
    }

    @Override
    public void setNextProcessor(Processor processor) {
        //this method will not be used as there is no processors after a query selector
    }


    public void setNextProcessor(OutputRateLimiter outputRateLimiter) {
        if (this.outputRateLimiter == null) {
            this.outputRateLimiter = outputRateLimiter;
        } else {
            throw new ExecutionPlanCreationException("outputRateLimiter is already assigned");
        }
    }

    @Override
    public void setToLast(Processor processor) {
        if (getNextProcessor() == null) {
            this.setNextProcessor(processor);
        } else {
            getNextProcessor().setToLast(processor);
        }
    }

    @Override
    public Processor cloneProcessor(String key) {
        return null;
    }

    public List<AttributeProcessor> getAttributeProcessorList() {
        return attributeProcessorList;
    }

    public void setAttributeProcessorList(List<AttributeProcessor> attributeProcessorList, boolean containsAggregator) {
        this.attributeProcessorList = attributeProcessorList;
        this.containsAggregator = this.containsAggregator || containsAggregator;
    }

    public void setGroupByKeyGenerator(GroupByKeyGenerator groupByKeyGenerator) {
        isGroupBy = true;
        this.groupByKeyGenerator = groupByKeyGenerator;
    }

    public void setHavingConditionExecutor(ConditionExpressionExecutor havingConditionExecutor, boolean containsAggregator) {
        this.havingConditionExecutor = havingConditionExecutor;
        this.containsAggregator = this.containsAggregator || containsAggregator;
    }

    public QuerySelector clone(String key) {
        QuerySelector clonedQuerySelector = new QuerySelector(id + key, selector, currentOn, expiredOn, executionPlanContext);
        List<AttributeProcessor> clonedAttributeProcessorList = new ArrayList<AttributeProcessor>();
        for (AttributeProcessor attributeProcessor : attributeProcessorList) {
            clonedAttributeProcessorList.add(attributeProcessor.cloneProcessor(key));
        }
        clonedQuerySelector.attributeProcessorList = clonedAttributeProcessorList;
        clonedQuerySelector.isGroupBy = isGroupBy;
        clonedQuerySelector.containsAggregator = containsAggregator;
        clonedQuerySelector.groupByKeyGenerator = groupByKeyGenerator;
        clonedQuerySelector.havingConditionExecutor = havingConditionExecutor;
        clonedQuerySelector.eventPopulator = eventPopulator;
        clonedQuerySelector.batchingEnabled = batchingEnabled;
        return clonedQuerySelector;
    }

    public void setBatchingEnabled(boolean batchingEnabled) {
        this.batchingEnabled = batchingEnabled;
    }

    public void setEventPopulator(StateEventPopulator eventPopulator) {
        this.eventPopulator = eventPopulator;
    }

}

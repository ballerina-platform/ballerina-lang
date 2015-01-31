/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.query.selector;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.populater.StateEventPopulator;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.condition.ConditionExpressionExecutor;
import org.wso2.siddhi.core.query.output.rateLimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.selector.attribute.processor.AttributeProcessor;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;

import java.util.ArrayList;
import java.util.List;

public class QuerySelector implements Processor {


    private static final Logger log = Logger.getLogger(QuerySelector.class);
    private static final ThreadLocal<String> keyThreadLocal = new ThreadLocal<String>();
    private Selector selector;
    private ExecutionPlanContext executionPlanContext;
    private boolean currentOn = false;
    private boolean expiredOn = false;
    private OutputRateLimiter outputRateLimiter;
    private List<AttributeProcessor> attributeProcessorList;
    private ConditionExpressionExecutor havingConditionExecutor = null;
    private boolean isGroupBy = false;
    private GroupByKeyGenerator groupByKeyGenerator;
    private String id;
    private StateEventPopulator eventPopulator;

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

        complexEventChunk.reset();

        if (log.isTraceEnabled()) {
            log.trace("event is processed by selector " + id + this);
        }

        while (complexEventChunk.hasNext()) {       //todo optimize
            ComplexEvent event = complexEventChunk.next();

            if (event.getType() == StreamEvent.Type.CURRENT || event.getType() == StreamEvent.Type.EXPIRED) {

                eventPopulator.populateStateEvent(event);

                if (isGroupBy) {
                    keyThreadLocal.set(groupByKeyGenerator.constructEventKey(event));
                }

                //TODO: have to change for windows
                for (AttributeProcessor attributeProcessor : attributeProcessorList) {
                    attributeProcessor.process(event);
                }

                if (isGroupBy) {
                    keyThreadLocal.remove();
                }

                if (havingConditionExecutor != null && !havingConditionExecutor.execute(event)) {
                    complexEventChunk.remove();
                }
            } else {
                complexEventChunk.remove();
            }

        }

        if (complexEventChunk.getFirst() != null) {
            outputRateLimiter.process(complexEventChunk);
        }
    }

    private void evaluateHavingConditions(ComplexEventChunk<StreamEvent> streamEventBuffer) {
        while (streamEventBuffer.hasNext()) {
            StreamEvent streamEvent = streamEventBuffer.next();
            if (!havingConditionExecutor.execute(streamEvent)) {
                streamEventBuffer.remove();
//                        eventManager.clear(event);  todo use this after fixing join cases
            }
        }
    }

    @Override
    public Processor getNextProcessor() {
        return outputRateLimiter;
    }

    @Override
    public void setNextProcessor(Processor processor) {
        if (!(processor instanceof OutputRateLimiter)) {
            throw new ExecutionPlanCreationException("processor is not an instance of OutputRateLimiter");
        }
        if (outputRateLimiter == null) {
            this.outputRateLimiter = (OutputRateLimiter) processor;
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

    public void setAttributeProcessorList(List<AttributeProcessor> attributeProcessorList) {
        this.attributeProcessorList = attributeProcessorList;
    }

    public void setGroupByKeyGenerator(GroupByKeyGenerator groupByKeyGenerator) {
        isGroupBy = true;
        this.groupByKeyGenerator = groupByKeyGenerator;
    }

    public void setHavingConditionExecutor(ConditionExpressionExecutor havingConditionExecutor) {
        this.havingConditionExecutor = havingConditionExecutor;
    }

    public QuerySelector clone(String key) {
        QuerySelector clonedQuerySelector = new QuerySelector(id + key, selector, currentOn, expiredOn, executionPlanContext);
        List<AttributeProcessor> clonedAttributeProcessorList = new ArrayList<AttributeProcessor>();
        for (AttributeProcessor attributeProcessor : attributeProcessorList) {
            clonedAttributeProcessorList.add(attributeProcessor.cloneProcessor(key));
        }
        clonedQuerySelector.attributeProcessorList = clonedAttributeProcessorList;
        clonedQuerySelector.isGroupBy = isGroupBy;
        clonedQuerySelector.groupByKeyGenerator = groupByKeyGenerator;
        clonedQuerySelector.havingConditionExecutor = havingConditionExecutor;
        clonedQuerySelector.eventPopulator = eventPopulator;
        return clonedQuerySelector;
    }

    public void setEventPopulator(StateEventPopulator eventPopulator) {
        this.eventPopulator = eventPopulator;
    }

}

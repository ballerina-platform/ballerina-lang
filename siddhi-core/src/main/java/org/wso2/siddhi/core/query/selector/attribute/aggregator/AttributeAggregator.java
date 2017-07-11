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
package org.wso2.siddhi.core.query.selector.attribute.aggregator;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.extension.holder.EternalReferencedHolder;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * Abstract parent class for attribute aggregators. Attribute aggregators are used to perform aggregate operations
 * such as count, average, etc.
 */
public abstract class AttributeAggregator implements EternalReferencedHolder, Snapshotable {

    protected ExpressionExecutor[] attributeExpressionExecutors;
    protected SiddhiAppContext siddhiAppContext;
    protected String elementId;
    private int attributeSize;
    private ConfigReader configReader;

    public void initAggregator(ExpressionExecutor[] attributeExpressionExecutors, SiddhiAppContext
            siddhiAppContext, ConfigReader configReader) {
        this.configReader = configReader;
        try {
            this.siddhiAppContext = siddhiAppContext;
            this.attributeExpressionExecutors = attributeExpressionExecutors;
            this.attributeSize = attributeExpressionExecutors.length;
            siddhiAppContext.addEternalReferencedHolder(this);
            if (elementId == null) {
                elementId = "AttributeAggregator-" + siddhiAppContext.getElementIdGenerator().createNewId();
            }
            //Not added to Snapshotable as the AggregationAttributeExecutors are added
//            siddhiAppContext.getSnapshotService().addSnapshotable(this);
            init(attributeExpressionExecutors, configReader, siddhiAppContext);
        } catch (Throwable t) {
            throw new SiddhiAppCreationException(t);
        }
    }

    public AttributeAggregator cloneAggregator(String key) {
        try {
            AttributeAggregator attributeAggregator = this.getClass().newInstance();
            ExpressionExecutor[] innerExpressionExecutors = new ExpressionExecutor[attributeSize];
            for (int i = 0; i < attributeSize; i++) {
                innerExpressionExecutors[i] = attributeExpressionExecutors[i].cloneExecutor(key);
            }
            attributeAggregator.elementId = elementId + "-" + key;
            attributeAggregator.initAggregator(innerExpressionExecutors, siddhiAppContext, configReader);
            attributeAggregator.start();
            return attributeAggregator;
        } catch (Exception e) {
            throw new SiddhiAppRuntimeException("Exception in cloning " + this.getClass().getCanonicalName(), e);
        }
    }

    public synchronized Object process(ComplexEvent event) {
        if (attributeSize > 1) {
            Object[] data = new Object[attributeSize];
            for (int i = 0; i < attributeSize; i++) {
                data[i] = attributeExpressionExecutors[i].execute(event);
            }
            switch (event.getType()) {
                case CURRENT:
                    return processAdd(data);
                case EXPIRED:
                    return processRemove(data);
                case RESET:
                    return reset();
            }
        } else if (attributeSize == 1) {
            switch (event.getType()) {
                case CURRENT:
                    return processAdd(attributeExpressionExecutors[0].execute(event));
                case EXPIRED:
                    return processRemove(attributeExpressionExecutors[0].execute(event));
                case RESET:
                    return reset();
            }
        } else {
            switch (event.getType()) {
                case CURRENT:
                    return processAdd(null);
                case EXPIRED:
                    return processRemove(null);
                case RESET:
                    return reset();
            }
        }
        return null;
    }

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param configReader this hold the {@link AttributeAggregator} extensions configuration reader.
     * @param siddhiAppContext         Siddhi app runtime context
     */
    protected abstract void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                                 SiddhiAppContext
                                         siddhiAppContext);

    public abstract Attribute.Type getReturnType();

    public abstract Object processAdd(Object data);

    public abstract Object processAdd(Object[] data);

    public abstract Object processRemove(Object data);

    public abstract Object processRemove(Object[] data);

    public abstract Object reset();

    @Override
    public String getElementId() {
        return elementId;
    }
}

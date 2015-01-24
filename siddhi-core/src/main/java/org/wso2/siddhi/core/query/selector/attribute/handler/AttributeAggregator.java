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
package org.wso2.siddhi.core.query.selector.attribute.handler;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.extension.EternalReferencedHolder;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public abstract class AttributeAggregator implements Serializable, EternalReferencedHolder {

    protected List<ExpressionExecutor> attributeExpressionExecutors;
    protected ExecutionPlanContext executionPlanContext;
    private int attributeSize;

    public void initAggregator(List<ExpressionExecutor> attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
        this.attributeExpressionExecutors = attributeExpressionExecutors;
        this.attributeSize = attributeExpressionExecutors.size();
        executionPlanContext.addEternalReferencedHolder(this);
        init(attributeExpressionExecutors, executionPlanContext);
    }

    public AttributeAggregator cloneAggregator() {
        try {
            AttributeAggregator attributeAggregator = this.getClass().newInstance();
            List<ExpressionExecutor> innerExpressionExecutors = new LinkedList<ExpressionExecutor>();
            for (ExpressionExecutor attributeExecutor : this.attributeExpressionExecutors) {
                innerExpressionExecutors.add(attributeExecutor.cloneExecutor());
            }
            attributeAggregator.initAggregator(innerExpressionExecutors, executionPlanContext);
            attributeAggregator.start();
            return attributeAggregator;
        } catch (Exception e) {
            throw new ExecutionPlanRuntimeException("Exception in cloning " + this.getClass().getCanonicalName(), e);
        }
    }

    public Object process(ComplexEvent event) {
        if (attributeSize > 1) {
            Object[] data = new Object[attributeSize];
            for (int i = 0; i < attributeSize; i++) {
                data[i] = attributeExpressionExecutors.get(i).execute(event);
            }
            switch (event.getType()) {
                case CURRENT:
                    return processAdd(data);
                case EXPIRED:
                    return processRemove(data);
                case RESET:
                    return reset();
            }
        } else {
            switch (event.getType()) {
                case CURRENT:
                    return processAdd(attributeExpressionExecutors.get(0).execute(event));
                case EXPIRED:
                    return processRemove(attributeExpressionExecutors.get(0).execute(event));
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
     * @param executionPlanContext         Execution plan runtime context
     */
    protected abstract void init(List<ExpressionExecutor> attributeExpressionExecutors, ExecutionPlanContext executionPlanContext);

    public abstract Attribute.Type getReturnType();

    public abstract Object processAdd(Object data);

    public abstract Object processAdd(Object[] data);

    public abstract Object processRemove(Object data);

    public abstract Object processRemove(Object[] data);

    public abstract Object reset();
}

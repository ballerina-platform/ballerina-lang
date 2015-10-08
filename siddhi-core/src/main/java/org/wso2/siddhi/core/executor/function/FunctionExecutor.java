/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.executor.function;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.extension.holder.EternalReferencedHolder;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;

public abstract class FunctionExecutor implements ExpressionExecutor, EternalReferencedHolder, Snapshotable {

    private static final Logger log = Logger.getLogger(FunctionExecutor.class);
    protected ExpressionExecutor[] attributeExpressionExecutors;
    protected ExecutionPlanContext executionPlanContext;
    protected String elementId;
    private int attributeSize;

    public void initExecutor(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        try {
            this.executionPlanContext = executionPlanContext;
            this.attributeExpressionExecutors = attributeExpressionExecutors;
            attributeSize = attributeExpressionExecutors.length;
            executionPlanContext.addEternalReferencedHolder(this);
            if (elementId == null) {
                elementId = executionPlanContext.getElementIdGenerator().createNewId();
            }
            executionPlanContext.getSnapshotService().addSnapshotable(this);
            init(attributeExpressionExecutors, executionPlanContext);
        } catch (Throwable t) {
            throw new ExecutionPlanCreationException(t);
        }
    }

    @Override
    public ExpressionExecutor cloneExecutor(String key) {
        try {
            FunctionExecutor functionExecutor = this.getClass().newInstance();
            ExpressionExecutor[] innerExpressionExecutors = new ExpressionExecutor[attributeSize];
            for (int i = 0; i < attributeSize; i++) {
                innerExpressionExecutors[i] = attributeExpressionExecutors[i].cloneExecutor(key);
            }
            functionExecutor.elementId = elementId + "-" + key;
            functionExecutor.initExecutor(innerExpressionExecutors, executionPlanContext);
            functionExecutor.start();
            return functionExecutor;
        } catch (Exception e) {
            throw new ExecutionPlanRuntimeException("Exception in cloning " + this.getClass().getCanonicalName(), e);
        }
    }

    /**
     * The initialization method for FunctionExecutor, this method will be called before the other methods
     *
     * @param attributeExpressionExecutors are the executors of each function parameters
     * @param executionPlanContext         the context of the execution plan
     */
    protected abstract void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext);


    /**
     * The main execution method which will be called upon event arrival
     *
     * @param event the event to be executed
     * @return the execution result
     */
    @Override
    public Object execute(ComplexEvent event) {
        try {
            switch (attributeSize) {
                case 0:
                    return execute((Object) null);
                case 1:
                    return execute(attributeExpressionExecutors[0].execute(event));
                default:
                    Object[] data = new Object[attributeSize];
                    for (int i = 0; i < attributeSize; i++) {
                        data[i] = attributeExpressionExecutors[i].execute(event);
                    }
                    return execute(data);
            }
        } catch (Exception e) {
            log.error("Exception on execution plan '" + executionPlanContext.getName() + "' on class '" + this.getClass().getName() + "', " + e.getMessage(), e);
            return null;
        }
    }


    /**
     * The main execution method which will be called upon event arrival
     * when there are more then one function parameter
     *
     * @param data the runtime values of function parameters
     * @return the function result
     */
    protected abstract Object execute(Object[] data);

    /**
     * The main execution method which will be called upon event arrival
     * when there are zero or one function parameter
     *
     * @param data null if the function parameter count is zero or
     *             runtime data value of the function parameter
     * @return the function result
     */
    protected abstract Object execute(Object data);

    @Override
    public String getElementId() {
        return elementId;
    }
}

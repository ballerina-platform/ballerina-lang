/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.extension.EternalReferencedHolder;

import java.util.List;

public abstract class FunctionExecutor implements ExpressionExecutor, EternalReferencedHolder {

    protected List<ExpressionExecutor> attributeExpressionExecutors;
    protected ExecutionPlanContext executionPlanContext;
    protected int attributeSize;

    public void setExecutionPlanContext(ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
    }

    public void setAttributeExpressionExecutors(List<ExpressionExecutor> attributeExpressionExecutors) {
        this.attributeExpressionExecutors = attributeExpressionExecutors;
        attributeSize = attributeExpressionExecutors.size();
    }

    public void init() {
        init(attributeExpressionExecutors, executionPlanContext);
    }

    /**
     * The initialization method for FunctionExecutor
     *  @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext                SiddhiContext
     */
    public abstract void init(List<ExpressionExecutor> attributeExpressionExecutors, ExecutionPlanContext executionPlanContext);


    @Override
    public Object execute(StreamEvent event) {

        if (attributeSize > 1) {
            Object[] data = new Object[attributeExpressionExecutors.size()];
            for (int i = 0, size = data.length; i < size; i++) {
                data[i] = attributeExpressionExecutors.get(i).execute(event);
            }
            return execute(data);
        } else {
            return execute(attributeExpressionExecutors.get(0).execute(event));
        }
    }


    /**
     * The main executions method which will be called upon event arrival
     *
     * @param data the runtime values of the attributeExpressionExecutors
     * @return
     */
    protected abstract Object execute(Object[] data);

    protected abstract Object execute(Object data);

}

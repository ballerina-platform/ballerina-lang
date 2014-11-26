/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.executor.function;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.extension.EternalReferencedHolder;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

public abstract class FunctionExecutor implements ExpressionExecutor, EternalReferencedHolder {

    protected List<ExpressionExecutor> attributeExpressionExecutors;
    protected SiddhiContext siddhiContext;
    protected int attributeSize;
    protected Attribute.Type[] attributeTypes;


    public void setSiddhiContext(SiddhiContext siddhiContext) {
        this.siddhiContext = siddhiContext;
    }

    public void setAttributeExpressionExecutors(List<ExpressionExecutor> attributeExpressionExecutors) {
        this.attributeExpressionExecutors = attributeExpressionExecutors;
        attributeSize = attributeExpressionExecutors.size();
        attributeTypes = new Attribute.Type[attributeExpressionExecutors.size()];
        for (int i = 0; i < attributeExpressionExecutors.size(); i++) {
            attributeTypes[i] = attributeExpressionExecutors.get(i).getReturnType();
        }
    }

    @Override
    public Object execute(AtomicEvent event) {

        if (attributeSize > 1) {
            Object[] data = new Object[attributeExpressionExecutors.size()];
            for (int i = 0, size = data.length; i < size; i++) {
                data[i] = attributeExpressionExecutors.get(i).execute(event);
            }
            return process(data);
        } else {
            return process(attributeExpressionExecutors.get(0).execute(event));
        }
    }


    public void init() {
        init(attributeTypes, siddhiContext);
    }

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeTypes are the type if the  attributes to the executor function
     * @param siddhiContext  SiddhiContext
     */
    public abstract void init(Attribute.Type[] attributeTypes, SiddhiContext siddhiContext);

    /**
     * The main executions method which will be called upon event arrival
     *
     * @param data the runtime values of the attributeExpressionExecutors
     * @return
     */
    protected abstract Object process(Object data);

}

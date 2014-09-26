/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query.extension.util;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

public class CustomFunctionExtension extends FunctionExecutor {
    Attribute.Type returnType;


    @Override
    public void destroy() {
    }
    /**
     * Return type of the custom function mentioned
     *
     * @return
     */

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public ExpressionExecutor cloneExecutor() {
        return this;
    }

    @Override
    public void init(List<ExpressionExecutor> attributeExpressionExecutors, SiddhiContext siddhiContext) {
        for(ExpressionExecutor expressionExecutor: attributeExpressionExecutors){
        Attribute.Type attributeType = expressionExecutor.getReturnType();
            if (attributeType == Attribute.Type.DOUBLE) {
                returnType = attributeType;

            } else if ((attributeType == Attribute.Type.STRING) || (attributeType == Attribute.Type.BOOL)) {
                throw new QueryCreationException("Plus cannot have parameters with types String or Bool");
            } else {
                returnType = Attribute.Type.LONG;
            }
        }

    }

    @Override
    protected Object execute(Object[] obj) {
        if (returnType == Attribute.Type.DOUBLE) {
            double total = 0;
                for (Object aObj : obj) {
                    total += Double.parseDouble(String.valueOf(aObj));
                }

            return total;
        } else {
            long total = 0;
                for (Object aObj :  obj) {
                    total += Long.parseLong(String.valueOf(aObj));
            }
            return total;
        }

    }

    @Override
    protected Object execute(Object obj) {
        if (returnType == Attribute.Type.DOUBLE) {
            double total = 0;
            if (obj instanceof Object[]) {
                for (Object aObj : (Object[]) obj) {
                    total += Double.parseDouble(String.valueOf(aObj));
                }
            }
            return total;
        } else {
            long total = 0;
            if (obj instanceof Object[]) {
                for (Object aObj : (Object[]) obj) {
                    total += Long.parseLong(String.valueOf(aObj));
                }
            }
            return total;
        }
    }
}
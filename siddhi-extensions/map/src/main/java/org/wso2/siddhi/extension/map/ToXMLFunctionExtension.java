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

package org.wso2.siddhi.extension.map;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.Map;


/**
 * toXML(Map) or toXML(Map, RootElementName)
 * Returns the string representation of the map in XML format
 * Accept Type(s): (Map) or (Map, String)
 * Return Type(s): String
 */
public class ToXMLFunctionExtension extends FunctionExecutor {
    private Attribute.Type returnType = Attribute.Type.STRING;
    private String rootElement = null;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if ((attributeExpressionExecutors.length) > 2) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to map:toXML() function, "
                    + "required only 1 or 2, but found " + attributeExpressionExecutors.length);

        } else if ((attributeExpressionExecutors.length) == 2) {
            Object rootElementObject = attributeExpressionExecutors[1].execute(null);
            if (rootElementObject instanceof String) {
                rootElement = ((String) rootElementObject);
            } else {
                throw new OperationNotSupportedException("Root element name should be of type String. But found "
                        + attributeExpressionExecutors[1].getReturnType());
            }
        }
    }

    @Override
    protected Object execute(Object[] data) {
        if (data[0] instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) data[0];
            return getXmlFromMapWithRootElement(map);
        } else {
            throw new ExecutionPlanRuntimeException("Data should be a string");
        }
    }

    @Override
    protected Object execute(Object data) {
        if (data instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) data;
            return getXmlFromMap(map);
        } else {
            throw new ExecutionPlanRuntimeException("Data should be a string");
        }
    }

    private Object getXmlFromMap(Map<Object, Object> map) {
        return addingElements(map);
    }

    private Object getXmlFromMapWithRootElement(Map<Object, Object> map) {
        StringBuilder xmlValue = new StringBuilder();
        xmlValue.append("<" + rootElement + ">");
        xmlValue.append(addingElements(map));
        xmlValue.append("</" + rootElement + ">");
        return xmlValue.toString();
    }

    private String addingElements(Map<Object, Object> map) {
        StringBuilder xmlValue = new StringBuilder();
        for (Map.Entry<Object, Object> mapEntry : map.entrySet()) {
            xmlValue.append("<" + mapEntry.getKey().toString() + ">");
            if (mapEntry.getValue() instanceof Map) {
                xmlValue.append(getXmlFromMap((Map<Object, Object>) mapEntry.getValue()));
            } else {
                xmlValue.append(mapEntry.getValue().toString());
            }
            xmlValue.append("</" + mapEntry.getKey().toString() + ">");
        }
        return xmlValue.toString();
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return null;    //No need to maintain a state.
    }

    @Override
    public void restoreState(Object[] state) {
        //Since there's no need to maintain a state, nothing needs to be done here.
    }
}



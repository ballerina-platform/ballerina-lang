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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import javax.xml.stream.XMLStreamException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * createFromXML(String)
 * Returns the created hashmap
 * Accept Type(s): (String)
 * Return Type(s): Map
 */
public class CreateFromXMLFunctionExtension extends FunctionExecutor {
    private Attribute.Type returnType = Attribute.Type.OBJECT;
    private NumberFormat numberFormat = NumberFormat.getInstance();

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if ((attributeExpressionExecutors.length) != 1) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to map:createFromXML() function, " +
                    "required only 1, but found " + attributeExpressionExecutors.length);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        return null;
    }

    @Override
    protected Object execute(Object data) {
        if (data instanceof String) {
            try {
                Map<Object, Object> topLevelMap = new HashMap<Object, Object>();
                OMElement parentElement = AXIOMUtil.stringToOM(data.toString());
                topLevelMap.put(parentElement.getQName().toString(), getMapFromXML(parentElement));
                return topLevelMap;
            } catch (XMLStreamException e) {
                throw new ExecutionPlanRuntimeException("Input data cannot be parsed to xml: " + e.getMessage(), e);
            }
        } else {
            throw new ExecutionPlanRuntimeException("Data should be a string");
        }
    }

    private Object getMapFromXML(OMElement parentElement) throws XMLStreamException {
        Map<Object, Object> map = new HashMap<Object, Object>();
        Iterator iterator = parentElement.getChildElements();
        while (iterator.hasNext()) {
            OMElement streamAttributeElement = (OMElement) iterator.next();
            String key = streamAttributeElement.getQName().toString();
            Object value;
            if (streamAttributeElement.getFirstElement() != null) {
                value = getMapFromXML(streamAttributeElement);
            } else {
                String elementText = streamAttributeElement.getText();
                if (elementText.equals("true") || elementText.equals("false")) {
                    value = Boolean.parseBoolean(elementText);
                } else {
                    try {
                        value = numberFormat.parse(elementText);
                    } catch (ParseException e) {
                        value = elementText;
                    }
                }
            }
            map.put(key, value);
        }
        return map;
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



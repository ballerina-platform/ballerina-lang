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
 * Return Type(s): HashMap
 */
public class CreateFromXMLFunctionExtension extends FunctionExecutor {
    private Attribute.Type returnType = Attribute.Type.OBJECT;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if ((attributeExpressionExecutors.length) != 1) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to map:create() function, " +
                    "required only 1, but found " + attributeExpressionExecutors.length);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        throw new ExecutionPlanRuntimeException("Cannot process with arguments > 1");
    }

    @Override
    protected Object execute(Object data) {
        if (data == null) {
            throw new ExecutionPlanRuntimeException("Data can not be null.");
        }
        if (data instanceof String) {
            Map<Object, Object> hashMap = new HashMap<Object, Object>();
            try {
                OMElement parentElement = AXIOMUtil.stringToOM(data.toString());
                Iterator iterator = parentElement.getChildElements();
                while (iterator.hasNext()) {
                    OMElement streamAtrributeElement = (OMElement) iterator.next();
                    String key = streamAtrributeElement.getQName().toString();
                    Iterator childIterator = streamAtrributeElement.getChildElements();
                    Object value;
                    if (childIterator.hasNext()) {
                        value = "";
                        do {
                            OMElement childElement = (OMElement) childIterator.next();
                            value = value + childElement.toString();
                        } while (childIterator.hasNext());
                    } else {
                        String elementText = streamAtrributeElement.getText();  // getText() returns a string
                        if (elementText.equals("true") || elementText.equals("false")) {
                            value = Boolean.parseBoolean(elementText);
                        } else {
                            NumberFormat nf = NumberFormat.getInstance();
                            try {
                                value = nf.parse(elementText);
                            } catch (ParseException e) {
                                value = elementText;
                            }
                        }
                    }
                    hashMap.put(key, value);
                }
                return hashMap;
            } catch (XMLStreamException e) {
                throw new ExecutionPlanRuntimeException("Input data cannot be parsed to xml: " + e.getMessage(), e);
            }
        } else {
            throw new ExecutionPlanRuntimeException("Data should be a string");
        }
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



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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

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
    static final Logger log = Logger.getLogger(CreateFromXMLFunctionExtension.class);
    Attribute.Type returnType = Attribute.Type.OBJECT;
    private Map<Object, Object> hashMap = new HashMap<>();

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if ((attributeExpressionExecutors.length)  != 1) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to map:create() function, " +
                    "required only 1, but found " + attributeExpressionExecutors.length);
        }
    }

    @Override
    protected Object execute(Object[] data) {

        return hashMap;
    }

    @Override
    protected Object execute(Object data) {

        if (data == null) {
            throw new ExecutionPlanRuntimeException("Data can not be null.");
        }

        if(data instanceof String) {
            try{
                OMElement element=AXIOMUtil.stringToOM(data.toString());
                Iterator i=element.getChildren();

                while (i.hasNext()) {
                    OMElement ome=(OMElement)i.next();
                    String key = ome.getQName().toString();
                    Object value = ome.getText();  // getText() returns a string

                    // have to get the type of value and put into the hash map

                }
            }
            catch(Exception e){
                throw new ExecutionPlanRuntimeException("Input data cannot be parsed to xml");
            }
        }
        else{
            throw new ExecutionPlanRuntimeException("Data should be a string");
        }
        return hashMap;
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



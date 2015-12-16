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

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.json.*;


import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



/**
 * createFromJSON(String)
 * Returns the created hashmap
 * Accept Type(s): (String)
 * Return Type(s): HashMap
 */
public class CreateFromJSONFunctionExtension extends FunctionExecutor {
    static final Logger log = Logger.getLogger(CreateFromJSONFunctionExtension.class);
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

        if (data == null) {
            throw new ExecutionPlanRuntimeException("Data can not be null.");
        }
        if ((data.length % 2) == 1) {
            throw new ExecutionPlanRuntimeException("Number of values for data should be a multiple of 2");
        }
        for (int i = 0; i < data.length; i += 2) {
            hashMap.put(data[i], data[i + 1]);
        }
        return hashMap;
    }

    @Override
    protected Object execute(Object data) {

        if (data == null) {
            throw new ExecutionPlanRuntimeException("Data can not be null.");
        }

        if(data instanceof String) {

            JSONObject jsonObject=new JSONObject(data.toString());
            Iterator<String> keys=jsonObject.keys();

            while(keys.hasNext()){

                String key=keys.next();
                Object value=jsonObject.get(key);

                hashMap.put(key,value);

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



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

import org.json.JSONException;
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
 * createFromJSON(String)
 * Returns the created map
 * Accept Type(s): (String)
 * Return Type(s): Map
 */
public class CreateFromJSONFunctionExtension extends FunctionExecutor {
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
        return null;
    }

    @Override
    protected Object execute(Object data) {
        if (data instanceof String) {
            Map<Object, Object> map = new HashMap<Object, Object>();
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(data.toString());
            } catch (JSONException e) {
                throw new ExecutionPlanRuntimeException("Cannot create JSON from '"+data.toString()+"' in create from json function", e);
            }
            return getMapFromJson(map, jsonObject);
        } else {
            throw new ExecutionPlanRuntimeException("Data should be a string");
        }
    }

    private Map<Object, Object> getMapFromJson(Map<Object, Object> map, JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = null;
            try {
                value = jsonObject.get(key);
            } catch (JSONException e) {
                throw new ExecutionPlanRuntimeException("JSON '"+jsonObject+"'does not contain key '"+key+"' in create from json function", e);
            }
            if (value instanceof JSONObject) {
                value = getMapFromJson(new HashMap<Object, Object>(), (JSONObject) value);
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



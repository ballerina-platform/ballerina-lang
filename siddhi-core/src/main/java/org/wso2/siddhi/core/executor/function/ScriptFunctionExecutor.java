/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.function.Script;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Map;

/**
 * Executor class for Script function. Function execution logic is implemented in execute here.
 */
public class ScriptFunctionExecutor extends FunctionExecutor {

    static final Logger LOG = Logger.getLogger(ScriptFunctionExecutor.class);
    Attribute.Type returnType;
    Script script;

    public ScriptFunctionExecutor() { }

    public ScriptFunctionExecutor(String name) {
        this.functionId = name;
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        returnType = siddhiAppContext.getScript(functionId).getReturnType();
        script = siddhiAppContext.getScript(functionId);
    }

    @Override
    protected Object execute(Object[] data) {
        return script.eval(functionId, data);
    }

    @Override
    protected Object execute(Object data) {
        return script.eval(functionId, new Object[]{data});
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> state) {

    }
}

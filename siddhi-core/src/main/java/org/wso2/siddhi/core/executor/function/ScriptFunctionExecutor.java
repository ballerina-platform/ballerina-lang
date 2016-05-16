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

package org.wso2.siddhi.core.executor.function;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.function.EvalScript;
import org.wso2.siddhi.query.api.definition.Attribute;

public class ScriptFunctionExecutor extends FunctionExecutor {

    static final Logger log = Logger.getLogger(ScriptFunctionExecutor.class);

    private String functionId;
    Attribute.Type returnType;
    EvalScript evalScript;

    public ScriptFunctionExecutor(String name) {
        this.functionId = name;
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        returnType = executionPlanContext.getEvalScript(functionId).getReturnType();
        evalScript = executionPlanContext.getEvalScript(functionId);
    }

    @Override
    protected Object execute(Object[] data) {
        return evalScript.eval(functionId, data);
    }

    @Override
    protected Object execute(Object data) {
        return evalScript.eval(functionId, new Object[]{data});
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] state) {

    }
}

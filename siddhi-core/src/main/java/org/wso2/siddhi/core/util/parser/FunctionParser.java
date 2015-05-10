/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.core.util.parser;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.function.EvalScript;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.util.extension.holder.EvalScriptExtensionHolder;
import org.wso2.siddhi.query.api.definition.FunctionDefinition;
import org.wso2.siddhi.query.api.extension.Extension;

public class FunctionParser {
    static final Logger log = Logger.getLogger(FunctionParser.class);

    public static void addFunction(SiddhiContext siddhiContext, final FunctionDefinition functionDefinition) {
        ExecutionPlanContext executionPlanContext = new ExecutionPlanContext();
        executionPlanContext.setSiddhiContext(siddhiContext);
        EvalScript evalScript = (EvalScript) SiddhiClassLoader.loadExtensionImplementation(
                new Extension() {
                    @Override
                    public String getNamespace() {
                        return "evalscript";
                    }

                    @Override
                    public String getFunction() {
                        return functionDefinition.getLanguage().toLowerCase();
                    }
                }, EvalScriptExtensionHolder.getInstance(executionPlanContext));
        evalScript.init(functionDefinition.getFunctionID(), functionDefinition.getBody());
        evalScript.setReturnType(functionDefinition.getReturnType());
        siddhiContext.getScriptFunctionMap().put(functionDefinition.getFunctionID(),evalScript);
    }
}
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

package org.wso2.siddhi.extension.script;

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.core.function.Script;
import org.wso2.siddhi.query.api.definition.Attribute;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@Extension(
        name = "javascript",
        namespace = "script",
        description = ""
)
public class EvalJavaScript implements Script {

    String functionName;
    private ScriptEngine engine;
    private Attribute.Type returnType;

    public EvalJavaScript() {
        engine = new ScriptEngineManager().getEngineByName("JavaScript");
    }

    @Override
    public void init(String name, String body) {
        this.functionName = name;
        if (returnType == null) {
            throw new SiddhiAppCreationException("Cannot find the return type of the function " + functionName);
        }
        try {
            engine.eval("function " + name + "(data)" + "{" + body + "}");
        } catch (ScriptException e) {
            throw new SiddhiAppCreationException("Compilation Failure of the JavaScript Function " + name, e);
        }
    }

    @Override
    public Object eval(String name, Object[] args) {
        StringBuilder jsArray = new StringBuilder("var data = [");
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i] instanceof String) {
                jsArray.append("\"").append(args[i].toString()).append("\"");
            } else {
                jsArray.append(args[i].toString());
            }
            jsArray.append(",");
        }
        if (args[args.length - 1] instanceof String) {
            jsArray.append("\"").append(args[args.length - 1].toString()).append("\"");
        } else {
            jsArray.append(args[args.length - 1].toString());
        }
        jsArray.append("]");
        try {
            engine.eval(jsArray.toString());
            return engine.eval(name + "(data);");
        } catch (ScriptException e) {
            throw new SiddhiAppRuntimeException("Error evaluating JavaScript Function " + name, e);
        }
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public void setReturnType(Attribute.Type returnType) {
        this.returnType = returnType;
    }
}

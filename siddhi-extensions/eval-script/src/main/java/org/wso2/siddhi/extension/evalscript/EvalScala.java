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

package org.wso2.siddhi.extension.evalscript;

import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.function.EvalScript;
import org.wso2.siddhi.query.api.definition.Attribute;
import scala.Function1;

public class EvalScala implements EvalScript {

    private Function1<Object[], Object> scalaFunction;
    private Attribute.Type returnType;
    private String functionName;

    public EvalScala() {

    }

    public void init(String name, String body) {
        this.functionName = name;
        if( returnType == null ) {
            throw new ExecutionPlanCreationException("Cannot find the return type of the function " + functionName);
        }
        ScalaEvaluationEngine scalaEvaluationEngine = new ScalaEvaluationEngine();
        try {
            scalaFunction = scalaEvaluationEngine.eval("data: (Array[Any]) =>  {\n" + body + "\n}");
        } catch (Exception e) {
            throw new ExecutionPlanCreationException("Compilation Failure of the Scala Function " + name, e);
        }
    }

    @Override
    public Object eval(String name, Object[] arg) {
        try {
            return scalaFunction.apply(arg);
        } catch (Exception e) {
            throw new ExecutionPlanRuntimeException("Error while evaluating function " + name, e);
        }
    }

    @Override
    public void setReturnType(Attribute.Type returnType) {
        this.returnType = returnType;
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }
}
/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.engine.invokable;

import com.sun.jdi.Method;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR;

/**
 * JVM generated method representation of a ballerina function.
 *
 * @since 2.0.0
 */
public abstract class GeneratedMethod extends JvmMethod {

    GeneratedMethod(SuspendedContext context, Method methodRef) {
        super(context, methodRef);
    }

    @Override
    protected List<Value> getMethodArgs(JvmMethod method) throws EvaluationException {
        if (argValues == null) {
            throw createEvaluationException(FUNCTION_EXECUTION_ERROR, methodRef.name());
        }

        List<Value> argValueList = new ArrayList<>();
        // Here we use the existing strand instance to execute the function invocation expression.
        Value strand = context.getCurrentStrand();
        argValueList.add(strand);
        argValueList.addAll(argValues);
        return argValueList;
    }
}

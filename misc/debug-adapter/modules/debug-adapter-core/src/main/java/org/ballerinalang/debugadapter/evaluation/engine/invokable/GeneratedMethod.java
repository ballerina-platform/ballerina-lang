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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.DoubleType;
import com.sun.jdi.FloatType;
import com.sun.jdi.IntegerType;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.LongType;
import com.sun.jdi.Method;
import com.sun.jdi.ShortType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import io.ballerina.runtime.api.types.BooleanType;
import io.ballerina.runtime.api.types.ByteType;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.evaluation.engine.InvocationArgProcessor.DEFAULTABLE_PARAM_SUFFIX;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.STRAND_VAR_NAME;

/**
 * JVM generated method representation of a ballerina function.
 *
 * @since 2.0.0
 */
public abstract class GeneratedMethod extends JvmMethod {

    GeneratedMethod(SuspendedContext context, Method methodRef) {
        super(context, methodRef);
    }

    public Method getJDIMethodRef() {
        return methodRef;
    }

    @Override
    protected List<Value> getMethodArgs(JvmMethod method) throws EvaluationException {
        if (argValues == null && argEvaluators == null) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString()
                    , methodRef.name()));
        }

        List<Value> argValueList = new ArrayList<>();
        if (argValues != null) {
            // Here we use the existing strand instance to execute the function invocation expression.
            Value strand = context.getCurrentStrand();
            argValueList.add(strand);
            argValueList.addAll(argValues);
            return argValueList;
        }

        // Evaluates all function argument expressions at first.
        for (Map.Entry<String, Evaluator> argEvaluator : argEvaluators) {
            argValueList.add(argEvaluator.getValue().evaluate().getJdiValue());
        }

        return EvaluationUtils.getAsObjects(context, argValueList);
    }
}

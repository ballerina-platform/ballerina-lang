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

package org.ballerinalang.debugadapter.evaluation.engine;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Method;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.EvaluationUtils;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.STRAND_VAR_NAME;

/**
 * JDI based java method representation for a given ballerina function.
 *
 * @since 2.0.0
 */
public abstract class JvmMethod {
    protected final SuspendedContext context;
    protected final Method methodRef;
    protected final List<Evaluator> argEvaluators;

    JvmMethod(SuspendedContext context, Method methodRef, List<Evaluator> argEvaluators) {
        this.context = context;
        this.methodRef = methodRef;
        this.argEvaluators = argEvaluators;
    }

    /**
     * Invokes the underlying JVM method with given args.
     *
     * @return invocation result
     */
    public abstract Value invoke() throws EvaluationException;

    protected List<Value> generateJvmArgs(JvmMethod method) throws EvaluationException {
        try {
            List<Value> argValueList = new ArrayList<>();
            // Evaluates all function argument expressions at first.
            for (Evaluator argEvaluator : argEvaluators) {
                argValueList.add(argEvaluator.evaluate().getJdiValue());
                // Assuming all the arguments are positional args.
                argValueList.add(EvaluationUtils.make(context, true).getJdiValue());
            }

            List<Type> types = method.methodRef.argumentTypes();
            // Removes injected arguments added during the jvm method gen phase.
            for (int index = types.size() - 1; index >= 0; index -= 2) {
                types.remove(index);
            }

            // Todo - IMPORTANT: Add remaining steps to validate and match named, defaultable and rest args
            // Todo - verify
            // Here we use the parent strand instance to execute the function invocation expression.
            Value parentStrand = getParentStrand();
            argValueList.add(0, parentStrand);
            return argValueList;
        } catch (ClassNotLoadedException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString(),
                    methodRef.name()));
        }
    }

    /**
     * Returns the JDI value of the strand instance that is being used, by visiting visible variables of the given
     * debug context.
     *
     * @return JDI value of the strand instance that is being used
     */
    private Value getParentStrand() throws EvaluationException {
        try {
            Value strand = context.getFrame().getValue(context.getFrame().visibleVariableByName(STRAND_VAR_NAME));
            if (strand == null) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.STRAND_NOT_FOUND.getString(),
                        methodRef.name()));
            }
            return strand;
        } catch (JdiProxyException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.STRAND_NOT_FOUND.getString(),
                    methodRef));
        }
    }
}

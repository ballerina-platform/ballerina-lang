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

import com.sun.jdi.Method;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;

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
    protected List<Evaluator> argEvaluators;
    protected List<Value> argValues;

    JvmMethod(SuspendedContext context, Method methodRef) {
        this.context = context;
        this.methodRef = methodRef;
    }

    JvmMethod(SuspendedContext context, Method methodRef, List<Evaluator> argEvaluators, List<Value> argsList) {
        this.context = context;
        this.methodRef = methodRef;
        this.argEvaluators = argEvaluators;
        this.argValues = argsList;
    }

    /**
     * Invokes the underlying JVM method with given args.
     *
     * @return invocation result
     */
    public abstract Value invoke() throws EvaluationException;

    /**
     * Returns the required set of argument values to invoke underlying JVM method.
     *
     * @return invocation result
     */
    protected abstract List<Value> getMethodArgs(JvmMethod method) throws EvaluationException;

    public void setArgValues(List<Value> argValues) {
        this.argValues = argValues;
    }

    public void setArgEvaluators(List<Evaluator> argEvaluators) {
        this.argEvaluators = argEvaluators;
    }

    /**
     * Returns the JDI value of the strand instance that is being used, by visiting visible variables of the given
     * debug context.
     *
     * @return JDI value of the strand instance that is being used
     */
    protected Value getParentStrand() throws EvaluationException {
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

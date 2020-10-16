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
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.EvaluationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Ballerina JVM runtime static method representation.
 *
 * @since 2.0.0
 */
public class RuntimeStaticMethod extends JvmMethod {

    private final ReferenceType classRef;

    public RuntimeStaticMethod(SuspendedContext context, ReferenceType classRef, Method methodRef) {
        super(context, methodRef);
        this.classRef = classRef;
    }

    public RuntimeStaticMethod(SuspendedContext context, ReferenceType classRef, Method methodRef,
                               List<Evaluator> argEvaluators, List<Value> argsList) {
        super(context, methodRef, argEvaluators, argsList);
        this.classRef = classRef;
    }

    @Override
    public Value invoke() throws EvaluationException {
        try {
            if (!(classRef instanceof ClassType)) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR
                        .getString(), methodRef.name()));
            }
            List<Value> argValueList = getMethodArgs(this);
            return ((ClassType) classRef).invokeMethod(context.getOwningThread().getThreadReference(),
                    methodRef, argValueList, ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (ClassNotLoadedException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                    methodRef.name()));
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString(),
                    methodRef.name()));
        }
    }

    @Override
    protected List<Value> getMethodArgs(JvmMethod method) throws EvaluationException {
        try {
            if (argValues == null && argEvaluators == null) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR
                        .getString(), methodRef.name()));
            }
            if (argValues != null) {
                return argValues;
            }
            List<Value> argValueList = new ArrayList<>();
            // Evaluates all function argument expressions at first.
            for (Evaluator argEvaluator : argEvaluators) {
                argValueList.add(argEvaluator.evaluate().getJdiValue());
                // Assuming all the arguments are positional args.
                argValueList.add(EvaluationUtils.make(context, true).getJdiValue());
            }
            return argValueList;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString(),
                    methodRef.name()));
        }
    }
}

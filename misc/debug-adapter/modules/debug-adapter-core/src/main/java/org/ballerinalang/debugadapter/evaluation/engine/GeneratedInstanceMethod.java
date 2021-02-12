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
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.List;

/**
 * JVM generated instance method representation of a ballerina function.
 *
 * @since 2.0.0
 */
public class GeneratedInstanceMethod extends GeneratedMethod {

    private final Value objectValueRef;

    GeneratedInstanceMethod(SuspendedContext context, Value objectRef, Method methodRef) {
        super(context, methodRef);
        this.objectValueRef = objectRef;
    }

    @Override
    public Value invoke() throws EvaluationException {
        try {
            if (!(objectValueRef instanceof ObjectReference)) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR
                        .getString(), methodRef.name()));
            }
            List<Value> argValueList = getMethodArgs(this);
            return ((ObjectReference) objectValueRef).invokeMethod(context.getOwningThread().getThreadReference(),
                    methodRef, argValueList, ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (ClassNotLoadedException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.OBJECT_METHOD_NOT_FOUND.getString(),
                    methodRef.name(), VariableFactory.getVariable(context, objectValueRef).computeValue()));
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            return extractBErrors(e);
        }
    }
}

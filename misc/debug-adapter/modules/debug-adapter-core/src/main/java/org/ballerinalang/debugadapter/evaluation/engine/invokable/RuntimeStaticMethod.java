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

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;

import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.FUNCTION_NOT_FOUND;

/**
 * Ballerina JVM runtime static method representation.
 *
 * @since 2.0.0
 */
public class RuntimeStaticMethod extends RuntimeMethod {

    private final ReferenceType classRef;

    public RuntimeStaticMethod(SuspendedContext context, ReferenceType classRef, Method methodRef) {
        super(context, methodRef);
        this.classRef = classRef;
    }

    @Override
    protected Value invoke() throws EvaluationException {
        try {
            if (!(classRef instanceof ClassType)) {
                throw createEvaluationException(FUNCTION_EXECUTION_ERROR, methodRef.name());
            }
            List<Value> argValueList = getMethodArgs(this);
            return ((ClassType) classRef).invokeMethod(context.getFrame().threadProxy().getThreadReference(),
                    methodRef, argValueList, ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (ClassNotLoadedException e) {
            throw createEvaluationException(FUNCTION_NOT_FOUND, methodRef.name());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            return extractBErrors(e);
        }
    }
}

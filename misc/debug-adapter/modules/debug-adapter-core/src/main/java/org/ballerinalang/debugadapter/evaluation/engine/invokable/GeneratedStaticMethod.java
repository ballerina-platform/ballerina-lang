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

import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_SCHEDULER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.INVOKE_FUNCTION_ASYNC;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_LANG_CLASSLOADER;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_OBJECT_ARRAY_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_STRING_CLASS;

/**
 * JVM generated static method representation of a ballerina function.
 *
 * @since 2.0.0
 */
public class GeneratedStaticMethod extends GeneratedMethod {

    private final ReferenceType classRef;

    public GeneratedStaticMethod(SuspendedContext context, ReferenceType classRef, Method methodRef) {
        super(context, methodRef);
        this.classRef = classRef;
    }

    @Override
    protected Value invoke() throws EvaluationException {
        try {
            if (!(classRef instanceof ClassType)) {
                throw createEvaluationException(FUNCTION_EXECUTION_ERROR, methodRef.name());
            }
            List<String> argTypeList = new ArrayList<>();
            argTypeList.add(JAVA_LANG_CLASSLOADER);
            argTypeList.add(B_SCHEDULER_CLASS);
            argTypeList.add(JAVA_STRING_CLASS);
            argTypeList.add(JAVA_STRING_CLASS);
            argTypeList.add(JAVA_OBJECT_ARRAY_CLASS);
            RuntimeStaticMethod scheduleMethod = EvaluationUtils.getRuntimeMethod(context,
                    B_DEBUGGER_RUNTIME_CLASS, INVOKE_FUNCTION_ASYNC, argTypeList);

            List<Value> scheduleMethodArgs = new ArrayList<>();
            scheduleMethodArgs.add(context.getDebuggeeClassLoader());
            scheduleMethodArgs.add(null);
            scheduleMethodArgs.add(EvaluationUtils.getAsJString(context, classRef.name()));
            scheduleMethodArgs.add(EvaluationUtils.getAsJString(context, methodRef.name()));
            scheduleMethodArgs.addAll(getMethodArgs(this));
            scheduleMethod.setArgValues(scheduleMethodArgs);
            return scheduleMethod.invoke();
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            return extractBErrors(e);
        }
    }
}

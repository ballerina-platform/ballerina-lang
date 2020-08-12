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

package org.ballerinalang.debugadapter.evaluation;

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import com.sun.tools.jdi.ConcreteMethodImpl;
import io.ballerinalang.compiler.syntax.tree.Node;
import org.ballerinalang.debugadapter.SuspendedContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Debug expression evaluation utils.
 */
public class EvaluationUtils {

    private static final String JAVA_LANG_CLASS = "java.lang.Class";
    private static final String FOR_NAME_METHOD = "forName";
    public static final String STRAND_VAR_NAME = "__strand";

    public static ReferenceType loadClass(SuspendedContext evaluationContext, Node syntaxNode, String qName,
                                          ClassLoaderReference classLoader) throws EvaluationException {
        try {
            ClassType classType = (ClassType) evaluationContext.getAttachedVm().classesByName(JAVA_LANG_CLASS).get(0);
            if (classType == null) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "jvm " +
                        "class instance for the function invocation couldn't be loaded due to an internal error."));
            }
            Method forNameMethod = null;
            List<Method> methods = classType.methodsByName(FOR_NAME_METHOD);
            for (Method method : methods) {
                if (((ConcreteMethodImpl) method).argumentSignatures().size() == 3) {
                    forNameMethod = method;
                }
            }
            if (forNameMethod == null) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "jvm " +
                        "class instance for the function invocation couldn't be loaded due to an internal error."));
            }

            // Do not use unmodifiable lists because the list will be modified by JPDA.
            List<Value> args = new ArrayList<>();
            args.add(evaluationContext.getAttachedVm().mirrorOf(qName));
            args.add(evaluationContext.getAttachedVm().mirrorOf(true));
            args.add(classLoader);
            Value classReference = classType.invokeMethod(evaluationContext.getOwningThread().getThreadReference(),
                    forNameMethod, args, ObjectReference.INVOKE_SINGLE_THREADED);
            return ((ClassObjectReference) classReference).reflectedType();
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Error " +
                    "occurred when trying to load required classes to execute the function: " + syntaxNode.toString()));
        }
    }

    public static BExpressionValue make(SuspendedContext context, boolean val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, byte val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, char val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, short val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, int val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, long val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, float val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, double val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }

    public static BExpressionValue make(SuspendedContext context, String val) {
        return new BExpressionValue(context, context.getAttachedVm().mirrorOf(val));
    }
}

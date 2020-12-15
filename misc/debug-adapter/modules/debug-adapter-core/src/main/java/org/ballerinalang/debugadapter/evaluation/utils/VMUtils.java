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

package org.ballerinalang.debugadapter.evaluation.utils;

import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.RuntimeStaticMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_STRING_UTILS_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.FROM_STRING_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;
import static org.ballerinalang.debugadapter.variable.VariableUtils.removeRedundantQuotes;

/**
 * Evaluation VM related utils.
 *
 * @since 2.0.0
 */
public class VMUtils {

    private VMUtils() {
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

    public static BExpressionValue make(SuspendedContext context, String val) throws EvaluationException {
        val = removeRedundantQuotes(val);
        Value bStringVal = getAsBString(context, val);
        return new BExpressionValue(context, bStringVal);
    }

    /**
     * Converts the user given string literal into an {@link io.ballerina.runtime.api.values.BString} instance.
     *
     * @param context suspended debug context
     * @param val     string value
     * @return {@link io.ballerina.runtime.api.values.BString} instance
     */
    private static Value getAsBString(SuspendedContext context, String val) throws EvaluationException {
        List<String> argTypeNames = new ArrayList<>();
        argTypeNames.add(JAVA_STRING_CLASS);
        RuntimeStaticMethod fromStringMethod = getRuntimeMethod(context, B_STRING_UTILS_CLASS, FROM_STRING_METHOD,
                argTypeNames);
        fromStringMethod.setArgValues(Collections.singletonList(context.getAttachedVm().mirrorOf(val)));
        return fromStringMethod.invoke();
    }
}

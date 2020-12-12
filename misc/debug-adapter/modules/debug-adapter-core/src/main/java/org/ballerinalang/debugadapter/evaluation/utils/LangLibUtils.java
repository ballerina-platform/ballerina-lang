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

import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.GeneratedStaticMethod;
import org.ballerinalang.debugadapter.variable.BVariableType;

import java.util.StringJoiner;

import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.encodeModuleName;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getGeneratedMethod;
import static org.ballerinalang.debugadapter.variable.BVariableType.ARRAY;
import static org.ballerinalang.debugadapter.variable.BVariableType.INT;
import static org.ballerinalang.debugadapter.variable.BVariableType.MAP;

/**
 * Ballerina lang libraries related utils.
 *
 * @since 2.0.0
 */
public class LangLibUtils {

    public static final String LANG_LIB_ORG = "ballerina";
    public static final String LANG_LIB_PACKAGE_PREFIX = "lang.";
    private static final String LANG_VALUE = "value";

    private LangLibUtils() {
    }

    public static GeneratedStaticMethod loadLangLibMethod(SuspendedContext context, BExpressionValue result,
                                                          String methodName) throws EvaluationException {
        GeneratedStaticMethod generatedMethod = null;
        // Search within the specific lang lib related to the value type.
        String langLibName = getAssociatedLangLibName(result.getType());
        String langLibCls = getQualifiedClassName(context, langLibName);
        try {
            generatedMethod = getGeneratedMethod(context, langLibCls, methodName);
        } catch (EvaluationException ignored) {
        }

        // If no matches found, falls back to the `value` lang lib.
        if (generatedMethod == null) {
            langLibCls = getQualifiedClassName(context, LANG_VALUE);
            try {
                generatedMethod = getGeneratedMethod(context, langLibCls, methodName);
            } catch (EvaluationException ignored) {
            }
        }
        if (generatedMethod == null) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.LANG_LIB_METHOD_NOT_FOUND.getString(),
                    methodName, result.getType().getString()));
        }

        return generatedMethod;
    }

    private static String getQualifiedClassName(SuspendedContext context, String langLibName)
            throws EvaluationException {
        try {
            return new StringJoiner(".")
                    .add(LANG_LIB_ORG)
                    .add(encodeModuleName(LANG_LIB_PACKAGE_PREFIX + langLibName))
                    .add(context.getLoadedLangLibVersions().get(langLibName))
                    .add(langLibName)
                    .toString();
        } catch (Exception ignored) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.LANG_LIB_NOT_FOUND.getString(),
                    langLibName));
        }
    }

    private static String getAssociatedLangLibName(BVariableType bVarType) {
        switch (bVarType) {
            case INT:
            case BYTE:
                return INT.getString();
            case ARRAY:
            case TUPLE:
                return ARRAY.getString();
            case RECORD:
            case MAP:
                return MAP.getString();
            case FLOAT:
            case DECIMAL:
            case STRING:
            case BOOLEAN:
            case STREAM:
            case OBJECT:
            case ERROR:
            case FUTURE:
            case TYPE_DESC:
            case XML:
            case TABLE:
                return bVarType.getString();
            default:
                return LANG_VALUE;
        }
    }
}

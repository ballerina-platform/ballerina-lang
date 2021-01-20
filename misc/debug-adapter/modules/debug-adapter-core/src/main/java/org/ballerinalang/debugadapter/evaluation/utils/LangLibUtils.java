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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.GeneratedStaticMethod;
import org.ballerinalang.debugadapter.variable.BVariableType;

import java.util.Optional;
import java.util.StringJoiner;

import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.encodeModuleName;
import static org.ballerinalang.debugadapter.evaluation.engine.FunctionInvocationExpressionEvaluator.modifyName;
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
    public static final String LANG_LIB_VALUE = "value";

    private LangLibUtils() {
    }

    public static GeneratedStaticMethod loadLangLibMethod(SuspendedContext context, BExpressionValue result,
                                                          String langLibCls, String methodName)
            throws EvaluationException {

        GeneratedStaticMethod generatedMethod = null;
        try {
            generatedMethod = getGeneratedMethod(context, langLibCls, methodName);
        } catch (EvaluationException ignored) {
        }

        if (generatedMethod == null) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.LANG_LIB_METHOD_NOT_FOUND.getString(),
                    methodName, result.getType().getString()));
        }

        return generatedMethod;
    }

    public static String getQualifiedLangLibClassName(ModuleSymbol moduleSymbol, String langLibName)
            throws EvaluationException {
        try {
            return new StringJoiner(".")
                    .add(LANG_LIB_ORG)
                    .add(encodeModuleName(LANG_LIB_PACKAGE_PREFIX + langLibName))
                    .add(moduleSymbol.moduleID().version().replaceAll("\\.", "_"))
                    .add(langLibName)
                    .toString();
        } catch (Exception ignored) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.LANG_LIB_NOT_FOUND.getString(),
                    langLibName));
        }
    }

    public static Optional<ModuleSymbol> getLangLibDefinition(SuspendedContext context, String langLibName) {
        SemanticModel semanticContext = context.getDebugCompiler().getSemanticInfo();
        LinePosition position = LinePosition.from(context.getLineNumber(), 0);

        return semanticContext.visibleSymbols(context.getDocument(), position)
                .stream()
                .filter(symbol -> symbol.kind() == MODULE
                        && symbol.moduleID().orgName().equals(LANG_LIB_ORG)
                        && symbol.moduleID().moduleName().startsWith(LANG_LIB_PACKAGE_PREFIX)
                        && symbol.moduleID().moduleName().endsWith(langLibName))
                .findFirst()
                .map(symbol -> (ModuleSymbol) symbol);
    }

    public static Optional<FunctionSymbol> getLangLibFunctionDefinition(SuspendedContext context,
                                                                        ModuleSymbol langLibDef, String functionName) {
        return langLibDef.functions()
                .stream()
                .filter(functionSymbol -> modifyName(functionSymbol.name()).equals(functionName))
                .findFirst();
    }

    public static String getAssociatedLangLibName(BVariableType bVarType) {
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
                return LANG_LIB_VALUE;
        }
    }
}

/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;
import org.ballerinalang.debugadapter.variable.BVariableType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.TYPE_MISMATCH;
import static org.ballerinalang.debugadapter.evaluation.engine.NameBasedTypeResolver.ARRAY_TYPE_SUFFIX;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.checkIsType;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getValueAsObject;

/**
 * Invocation argument processor based on semantic API's symbols.
 *
 * @since 2.0.0
 */
public class SymbolBasedArgProcessor extends InvocationArgProcessor {

    private final FunctionSymbol definitionSymbol;
    private final FunctionTypeSymbol definitionTypeSymbol;

    public SymbolBasedArgProcessor(SuspendedContext context, String functionName, Method jdiMethodReference,
                                   FunctionSymbol definitionSymbol) {
        super(context, functionName, jdiMethodReference);
        this.definitionSymbol = definitionSymbol;
        this.definitionTypeSymbol = definitionSymbol.typeDescriptor();
    }

    @Override
    public List<Value> process(List<Map.Entry<String, Evaluator>> argEvaluators)
            throws EvaluationException {

        boolean namedArgsFound = false;
        boolean restArgsFound = false;
        boolean restArgsProcessed = false;
        Value restArrayType = null;
        String restParamName = null;
        String restParamTypeName = null;
        List<Value> restValues = new ArrayList<>();
        List<ParameterSymbol> params = new ArrayList<>();
        Map<String, ParameterSymbol> remainingParams = new LinkedHashMap<>();

        if (definitionTypeSymbol.params().isPresent()) {
            for (ParameterSymbol parameterSymbol : definitionTypeSymbol.params().get()) {
                params.add(parameterSymbol);
                remainingParams.put(parameterSymbol.getName().orElse(UNKNOWN_VALUE), parameterSymbol);
            }
        }

        if (definitionTypeSymbol.restParam().isPresent()) {
            ParameterSymbol restParam = definitionTypeSymbol.restParam().get();
            params.add(restParam);
            remainingParams.put(restParam.getName().orElse(UNKNOWN_VALUE), restParam);
        }

        Map<String, Value> argValues = new HashMap<>();
        for (int argIndex = 0, paramIndex = 0; argIndex < argEvaluators.size(); argIndex++) {
            Map.Entry<String, Evaluator> arg = argEvaluators.get(argIndex);
            ArgType argType = getArgType(arg);
            if (argType == ArgType.POSITIONAL) {
                if (namedArgsFound) {
                    throw createEvaluationException("positional args are not allowed after named args.");
                }

                if (remainingParams.isEmpty() && !restArgsFound) {
                    throw createEvaluationException("too many arguments in call to '" + functionName + "'.");
                }

                BExpressionValue argValue = arg.getValue().evaluate();
                Value jdiArgValue = argValue.getJdiValue();
                if (params.get(paramIndex).typeDescriptor().signature().equals(BVariableType.ANY.getString())) {
                    jdiArgValue = getValueAsObject(context, jdiArgValue);
                }

                if (params.get(paramIndex).paramKind() == ParameterKind.REST) {
                    for (Map.Entry<String, ParameterSymbol> entry : remainingParams.entrySet()) {
                        ParameterKind parameterType = entry.getValue().paramKind();
                        if (parameterType == ParameterKind.REST) {
                            restParamName = entry.getKey();
                            restParamTypeName = entry.getValue().typeDescriptor().signature();
                            break;
                        }
                    }
                    if (restParamName == null) {
                        throw createEvaluationException("undefined rest parameter.");
                    }

                    if (!restArgsFound) {
                        restArrayType = resolveType(context, restParamTypeName);
                        restArgsFound = true;
                    }
                    Value elementType = getElementType(context, restArrayType);
                    if (!checkIsType(context, jdiArgValue, elementType)) {
                        throw createEvaluationException(TYPE_MISMATCH, restParamTypeName.replaceAll(ARRAY_TYPE_SUFFIX,
                                ""), argValue.getType().getString(), restParamName);
                    }
                    restValues.add(jdiArgValue);
                    remainingParams.remove(restParamName);
                } else {
                    String parameterName = params.get(paramIndex).getName().orElse(UNKNOWN_VALUE);
                    argValues.put(parameterName, jdiArgValue);
                    remainingParams.remove(parameterName);
                    paramIndex++;
                }
            } else if (argType == ArgType.NAMED) {
                if (restArgsFound) {
                    throw createEvaluationException("named args are not allowed after rest args.");
                }

                String argName = arg.getKey();
                if (!remainingParams.containsKey(argName)) {
                    throw createEvaluationException("undefined defaultable parameter '" + argName + "'.");
                }
                namedArgsFound = true;
                Value argValue = arg.getValue().evaluate().getJdiValue();
                if (params.get(paramIndex).typeDescriptor().signature().equals(BVariableType.ANY.getString())) {
                    argValue = getValueAsObject(context, argValue);
                }
                argValues.put(argName, argValue);
                remainingParams.remove(argName);
                paramIndex++;
            } else if (argType == ArgType.REST) {
                if (namedArgsFound) {
                    throw createEvaluationException("rest args are not allowed after named args.");
                }

                for (Map.Entry<String, ParameterSymbol> entry : remainingParams.entrySet()) {
                    ParameterKind parameterType = entry.getValue().paramKind();
                    if (parameterType == ParameterKind.REST) {
                        restParamName = entry.getKey();
                        restParamTypeName = entry.getValue().typeDescriptor().signature();
                        break;
                    }
                }
                if (restParamName == null) {
                    throw createEvaluationException("undefined rest parameter.");
                }

                restArgsFound = true;
                argValues.put(restParamName, arg.getValue().evaluate().getJdiValue());
                remainingParams.remove(restParamName);
                restArgsProcessed = true;
                paramIndex++;
            }
        }

        for (Map.Entry<String, ParameterSymbol> entry : remainingParams.entrySet()) {
            String paramName = entry.getKey();
            ParameterKind parameterType = entry.getValue().paramKind();
            if (parameterType == ParameterKind.REQUIRED) {
                throw createEvaluationException("missing required parameter '" + paramName + "'.");
            } else if (parameterType == ParameterKind.DEFAULTABLE) {
                argValues.put(paramName + DEFAULTABLE_PARAM_SUFFIX, VMUtils.make(context, 0).getJdiValue());
            } else if (parameterType == ParameterKind.REST) {
                restParamTypeName = entry.getValue().typeDescriptor().signature();
                restArrayType = resolveType(context, restParamTypeName);
                argValues.put(paramName, getRestArgArray(context, getElementType(context, restArrayType)));
                restArgsProcessed = true;
            }
        }

        if (restArgsFound && !restArgsProcessed) {
            argValues.put(restParamName, getRestArgArray(context, restArrayType, restValues.toArray(new Value[0])));
        }

        return getOrderedArgList(argValues, definitionSymbol, null);
    }
}

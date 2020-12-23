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

import com.sun.jdi.Value;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.REST_ARG_IDENTIFIER;

/**
 * Validates and processes invocation arguments of ballerina functions and object methods.
 *
 * @since 2.0.0
 */
public class InvocationArgProcessor {

    public static final String DEFAULTABLE_PARAM_SUFFIX = "[Defaultable]";

    static Map<String, Value> generateNamedArgs(SuspendedContext context, String functionName, FunctionTypeSymbol
            definition, List<Map.Entry<String, Evaluator>> argEvaluators) throws EvaluationException {

        boolean namedArgsFound = false;
        boolean restArgsFound = false;
        List<ParameterSymbol> params = new ArrayList<>();
        Map<String, ParameterSymbol> remainingParams = new HashMap<>();

        for (ParameterSymbol parameterSymbol : definition.parameters()) {
            params.add(parameterSymbol);
            remainingParams.put(parameterSymbol.name().get(), parameterSymbol);
        }
        if (definition.restParam().isPresent()) {
            params.add(definition.restParam().get());
            remainingParams.put(definition.restParam().get().name().get(), definition.restParam().get());
        }

        Map<String, Value> argValues = new HashMap<>();
        for (int i = 0; i < argEvaluators.size(); i++) {
            Map.Entry<String, Evaluator> arg = argEvaluators.get(i);
            ArgType argType = getArgType(arg);
            if (argType == ArgType.POSITIONAL) {
                if (namedArgsFound) {
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "positional args are not allowed after named args."));
                } else if (restArgsFound) {
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "positional args are not allowed after rest args."));
                }

                if (remainingParams.isEmpty()) {
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "too many arguments in call to '" + functionName + "'."));
                }

                String parameterName = params.get(i).name().get();
                argValues.put(parameterName, arg.getValue().evaluate().getJdiValue());
                remainingParams.remove(parameterName);
            } else if (argType == ArgType.NAMED) {
                if (restArgsFound) {
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "named args are not allowed after rest args."));
                }

                String argName = arg.getKey();
                if (!remainingParams.containsKey(argName)) {
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "undefined defaultable parameter '" + argName + "'."));
                }
                namedArgsFound = true;
                argValues.put(argName, arg.getValue().evaluate().getJdiValue());
                remainingParams.remove(argName);
            } else if (argType == ArgType.REST) {
                if (namedArgsFound) {
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "rest args are not allowed after named args."));
                }

                String restParamName = null;
                for (Map.Entry<String, ParameterSymbol> entry : remainingParams.entrySet()) {
                    ParameterKind parameterType = entry.getValue().kind();
                    if (parameterType == ParameterKind.REST) {
                        restParamName = entry.getKey();
                        break;
                    }
                }
                if (restParamName == null) {
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "undefined rest parameter."));
                }
                restArgsFound = true;
                argValues.put(restParamName, arg.getValue().evaluate().getJdiValue());
                remainingParams.remove(restParamName);
            }
        }

        for (Map.Entry<String, ParameterSymbol> entry : remainingParams.entrySet()) {
            String paramName = entry.getKey();
            ParameterKind parameterType = entry.getValue().kind();
            if (parameterType == ParameterKind.REQUIRED) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        "missing required parameter '" + paramName + "'."));
            } else if (parameterType == ParameterKind.DEFAULTABLE) {
                argValues.put(paramName + DEFAULTABLE_PARAM_SUFFIX, VMUtils.make(context, 0).getJdiValue());
            }
        }

        return argValues;
    }

    private static ArgType getArgType(Map.Entry<String, Evaluator> arg) {
        if (isPositionalArg(arg)) {
            return ArgType.POSITIONAL;
        } else if (isNamedArg(arg)) {
            return ArgType.NAMED;
        } else if (isRestArg(arg)) {
            return ArgType.REST;
        }
        return ArgType.UNKNOWN;
    }

    private static boolean isPositionalArg(Map.Entry<String, Evaluator> arg) {
        return arg.getKey().isEmpty();
    }

    private static boolean isNamedArg(Map.Entry<String, Evaluator> arg) {
        return !arg.getKey().isEmpty() && !isRestArg(arg);
    }

    private static boolean isRestArg(Map.Entry<String, Evaluator> arg) {
        return !arg.getKey().isEmpty() && arg.getKey().equals(REST_ARG_IDENTIFIER);
    }

    private enum ArgType {
        POSITIONAL,
        NAMED,
        REST,
        UNKNOWN
    }
}

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
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.ExpressionEvaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.REST_ARG_IDENTIFIER;

/**
 * Validates and processes invocation arguments of ballerina functions and object methods.
 *
 * @since 2.0.0
 */
public class InvocationArgProcessor {

    static Map<String, Value> validateAndProcessArguments(SuspendedContext context, String functionName,
                                                          List<Map.Entry<String, Evaluator>> argEvaluators)
            throws EvaluationException {

        Optional<FunctionDefinitionNode> functionDefinition = new FunctionNodeFinder(functionName).searchIn(context
                .getDocument().module());
        if (functionDefinition.isEmpty()) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                    functionName));
        }
        SeparatedNodeList<ParameterNode> params = functionDefinition.get().functionSignature().parameters();
        return generateNamedArgs(context, functionName, params, argEvaluators);
    }

    static Map<String, Value> generateNamedArgs(SuspendedContext context, String functionName,
                                                SeparatedNodeList<ParameterNode> params,
                                                List<Map.Entry<String, Evaluator>> argEvaluators)
            throws EvaluationException {

        boolean namedArgsFound = false;
        boolean restArgsFound = false;

        Map<String, Value> argValues = new HashMap<>();

        Map<String, ParameterNode> remainingParams = new HashMap<>();
        params.stream().forEach(parameterNode -> remainingParams.put(getParameterName(parameterNode), parameterNode));

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
                String parameterName = getParameterName(params.get(i));
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
                for (Map.Entry<String, ParameterNode> entry : remainingParams.entrySet()) {
                    ParameterType parameterType = getParamType(entry.getValue());
                    if (parameterType == ParameterType.REST) {
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

        for (Map.Entry<String, ParameterNode> entry : remainingParams.entrySet()) {
            String paramName = entry.getKey();
            ParameterType parameterType = getParamType(entry.getValue());
            if (parameterType == ParameterType.REQUIRED) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        "missing required parameter '" + paramName + "'."));
            } else if (parameterType == ParameterType.DEFAULTABLE) {
                Value defaultValue = new ExpressionEvaluator(context)
                        .evaluate(((DefaultableParameterNode) entry.getValue()).expression()
                                .toSourceCode());
                argValues.put(paramName, defaultValue);
            }
        }

        return argValues;
    }

    private static String getParameterName(ParameterNode parameterNode) {
        if (parameterNode instanceof RequiredParameterNode) {
            return ((RequiredParameterNode) parameterNode).paramName().get().toSourceCode().trim();
        } else if (parameterNode instanceof DefaultableParameterNode) {
            return ((DefaultableParameterNode) parameterNode).paramName().get().toSourceCode().trim();
        } else if (parameterNode instanceof RestParameterNode) {
            return ((RestParameterNode) parameterNode).paramName().get().toSourceCode().trim();
        }
        return null;
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

    private static ParameterType getParamType(ParameterNode node) {
        if (node instanceof RequiredParameterNode) {
            return ParameterType.REQUIRED;
        } else if (node instanceof DefaultableParameterNode) {
            return ParameterType.DEFAULTABLE;
        } else if (node instanceof RestParameterNode) {
            return ParameterType.REST;
        }
        return ParameterType.UNKNOWN;
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

    private enum ParameterType {
        REQUIRED,
        DEFAULTABLE,
        REST,
        UNKNOWN
    }
}

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
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;
import org.ballerinalang.debugadapter.variable.BVariableType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.REST_ARG_IDENTIFIER;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getValueAsObject;

/**
 * Validates and processes invocation arguments of ballerina functions and object methods.
 *
 * @since 2.0.0
 */
public class InvocationArgProcessor {

    public static final String DEFAULTABLE_PARAM_SUFFIX = "[Defaultable]";

    public static Map<String, Value> generateNamedArgs(SuspendedContext context, String functionName, FunctionTypeSymbol
            definition, List<Map.Entry<String, Evaluator>> argEvaluators) throws EvaluationException {

        boolean namedArgsFound = false;
        boolean restArgsFound = false;
        List<ParameterSymbol> params = new ArrayList<>();
        Map<String, ParameterSymbol> remainingParams = new HashMap<>();

        if (definition.params().isPresent()) {
            for (ParameterSymbol parameterSymbol : definition.params().get()) {
                params.add(parameterSymbol);
                remainingParams.put(parameterSymbol.getName().get(), parameterSymbol);
            }
        }

        if (definition.restParam().isPresent()) {
            params.add(definition.restParam().get());
            remainingParams.put(definition.restParam().get().getName().get(), definition.restParam().get());
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

                String parameterName = params.get(i).getName().get();
                Value argValue = arg.getValue().evaluate().getJdiValue();
                // For the ballerina function parameters with type "any", the generated runtime method will only accept
                // the args which are subtypes of "java.lang.Object". Therefore all the primitive typed arguments
                // must be converted into their wrapper implementations (i.e. 'int' -> 'Integer'), before passing
                // into the method.
                if (params.get(i).typeDescriptor().typeKind().getName().equals(BVariableType.ANY.getString())) {
                    argValue = getValueAsObject(context, argValue);
                }
                argValues.put(parameterName, argValue);
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
                Value argValue = arg.getValue().evaluate().getJdiValue();
                // For the ballerina function parameters with type "any", the generated runtime method will only accept
                // the args which are subtypes of "java.lang.Object". Therefore all the primitive typed arguments
                // must be converted into their wrapper implementations (i.e. 'int' -> 'Integer'), before passing
                // into the method.
                if (params.get(i).typeDescriptor().typeKind().getName().equals(BVariableType.ANY.getString())) {
                    argValue = getValueAsObject(context, argValue);
                }
                argValues.put(argName, argValue);
                remainingParams.remove(argName);
            } else if (argType == ArgType.REST) {
                if (namedArgsFound) {
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "rest args are not allowed after named args."));
                }

                String restParamName = null;
                for (Map.Entry<String, ParameterSymbol> entry : remainingParams.entrySet()) {
                    ParameterKind parameterType = entry.getValue().paramKind();
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
            ParameterKind parameterType = entry.getValue().paramKind();
            if (parameterType == ParameterKind.REQUIRED) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        "missing required parameter '" + paramName + "'."));
            } else if (parameterType == ParameterKind.DEFAULTABLE) {
                argValues.put(paramName + DEFAULTABLE_PARAM_SUFFIX, VMUtils.make(context, 0).getJdiValue());
            }
        }

        return argValues;
    }

    public static Map<String, Value> generateNamedArgs(SuspendedContext context, String functionName,
                                                       FunctionSignatureNode functionSignature,
                                                       List<Map.Entry<String, Evaluator>> argEvaluators)
            throws EvaluationException {

        boolean namedArgsFound = false;
        boolean restArgsFound = false;
        List<ParameterNode> params = new ArrayList<>();
        Map<String, ParameterNode> remainingParams = new HashMap<>();

        if (!functionSignature.parameters().isEmpty()) {
            for (ParameterNode paramNode : functionSignature.parameters()) {
                params.add(paramNode);
                remainingParams.put(getParameterName(paramNode), paramNode);
            }
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

                String parameterName = getParameterName(params.get(i));
                Value argValue = arg.getValue().evaluate().getJdiValue();
                // For the ballerina function parameters with type "any", the generated runtime method will only accept
                // the args which are subtypes of "java.lang.Object". Therefore all the primitive typed arguments
                // must be converted into their wrapper implementations (i.e. 'int' -> 'Integer'), before passing
                // into the method.
                if (getParameterTypeName(params.get(i)).equals(BVariableType.ANY.getString())) {
                    argValue = getValueAsObject(context, argValue);
                }
                argValues.put(parameterName, argValue);
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
                Value argValue = arg.getValue().evaluate().getJdiValue();
                // For the ballerina function parameters with type "any", the generated runtime method will only accept
                // the args which are subtypes of "java.lang.Object". Therefore all the primitive typed arguments
                // must be converted into their wrapper implementations (i.e. 'int' -> 'Integer'), before passing
                // into the method.
                if (getParameterTypeName(params.get(i)).equals(BVariableType.ANY.getString())) {
                    argValue = getValueAsObject(context, argValue);
                }
                argValues.put(argName, argValue);
                remainingParams.remove(argName);
            } else if (argType == ArgType.REST) {
                if (namedArgsFound) {
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "rest args are not allowed after named args."));
                }

                String restParamName = null;
                for (Map.Entry<String, ParameterNode> entry : remainingParams.entrySet()) {
                    SyntaxKind parameterType = entry.getValue().kind();
                    if (parameterType == SyntaxKind.REST_PARAM) {
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
            SyntaxKind parameterType = entry.getValue().kind();
            if (parameterType == SyntaxKind.REQUIRED_PARAM) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        "missing required parameter '" + paramName + "'."));
            } else if (parameterType == SyntaxKind.DEFAULTABLE_PARAM) {
                argValues.put(paramName + DEFAULTABLE_PARAM_SUFFIX, VMUtils.make(context, 0).getJdiValue());
            }
        }

        return argValues;
    }

    private static String getParameterName(ParameterNode parameterNode) {
        Optional<Token> paramNameToken;
        switch (parameterNode.kind()) {
            case REQUIRED_PARAM:
                paramNameToken = ((RequiredParameterNode) parameterNode).paramName();
                break;
            case DEFAULTABLE_PARAM:
                paramNameToken = ((DefaultableParameterNode) parameterNode).paramName();
                break;
            case REST_PARAM:
                paramNameToken = ((RestParameterNode) parameterNode).paramName();
                break;
            default:
                paramNameToken = Optional.empty();
                break;
        }

        return paramNameToken.isPresent() ? paramNameToken.get().text() : "unknown";
    }

    private static String getParameterTypeName(ParameterNode parameterNode) {
        switch (parameterNode.kind()) {
            case REQUIRED_PARAM:
                return ((RequiredParameterNode) parameterNode).typeName().toSourceCode().trim();
            case DEFAULTABLE_PARAM:
                return ((DefaultableParameterNode) parameterNode).typeName().toSourceCode().trim();
            case REST_PARAM:
                return ((RestParameterNode) parameterNode).typeName().toSourceCode().trim();
            default:
                return "unknown";
        }
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

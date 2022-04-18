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

import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
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
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeInstanceMethod;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;
import org.ballerinalang.debugadapter.variable.BVariableType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.CUSTOM_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.OBJECT_METHOD_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.TYPE_MISMATCH;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_VALUE_ARRAY_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.GET_REST_ARG_ARRAY_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.REST_ARG_IDENTIFIER;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.checkIsType;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getValueAsObject;

/**
 * Validates and processes invocation arguments of ballerina functions and object methods.
 *
 * @since 2.0.0
 */
public class InvocationArgProcessor {

    public static final String DEFAULTABLE_PARAM_SUFFIX = "[Defaultable]";
    private static final String GET_ELEMENT_TYPE_METHOD = "getElementType";
    private static final String UNKNOWN = "unknown";

    public static Map<String, Value> generateNamedArgs(SuspendedContext context, String functionName, FunctionTypeSymbol
            definition, List<Map.Entry<String, Evaluator>> argEvaluators) throws EvaluationException {

        boolean namedArgsFound = false;
        boolean restArgsFound = false;
        boolean restArgsProcessed = false;
        Value restArrayType = null;
        String restParamName = null;
        String restParamTypeName = null;
        List<Value> restValues = new ArrayList<>();
        List<ParameterSymbol> params = new ArrayList<>();
        Map<String, ParameterSymbol> remainingParams = new LinkedHashMap<>();

        if (definition.params().isPresent()) {
            for (ParameterSymbol parameterSymbol : definition.params().get()) {
                params.add(parameterSymbol);
                remainingParams.put(parameterSymbol.getName().orElse(UNKNOWN), parameterSymbol);
            }
        }

        if (definition.restParam().isPresent()) {
            params.add(definition.restParam().get());
            remainingParams.put(definition.restParam().get().getName().orElse(UNKNOWN), definition.restParam().get());
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
                        throw createEvaluationException(TYPE_MISMATCH,
                                restParamTypeName.replaceAll(NameBasedTypeResolver.ARRAY_TYPE_SUFFIX, ""),
                                argValue.getType().getString(), restParamName);
                    }
                    restValues.add(jdiArgValue);
                    remainingParams.remove(restParamName);
                } else {
                    String parameterName = params.get(paramIndex).getName().orElse(UNKNOWN);
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
        return argValues;
    }

    public static Map<String, Value> generateNamedArgs(SuspendedContext context, String functionName,
                                                       FunctionSignatureNode functionSignature,
                                                       List<Map.Entry<String, Evaluator>> argEvaluators)
            throws EvaluationException {

        boolean namedArgsFound = false;
        boolean restArgsFound = false;
        boolean restArgsProcessed = false;
        Value restArrayType = null;
        String restParamName = null;
        String restParamTypeName = null;
        List<Value> restValues = new ArrayList<>();
        List<ParameterNode> params = new ArrayList<>();
        Map<String, ParameterNode> remainingParams = new HashMap<>();

        if (!functionSignature.parameters().isEmpty()) {
            for (ParameterNode paramNode : functionSignature.parameters()) {
                params.add(paramNode);
                remainingParams.put(getParameterName(paramNode), paramNode);
            }
        }

        Map<String, Value> argValues = new HashMap<>();
        for (int argIndex = 0, paramIndex = 0; argIndex < argEvaluators.size(); argIndex++) {
            Map.Entry<String, Evaluator> arg = argEvaluators.get(argIndex);
            ArgType argType = getArgType(arg);
            if (argType == ArgType.POSITIONAL) {
                if (namedArgsFound) {
                    throw createEvaluationException(CUSTOM_ERROR, "positional args are not allowed after named args.");
                }

                if (remainingParams.isEmpty() && restArgsFound) {
                    throw createEvaluationException("too many arguments in call to '" + functionName + "'.");
                }
                BExpressionValue argValue = arg.getValue().evaluate();
                Value jdiArgValue = argValue.getJdiValue();
                if (getParameterTypeName(params.get(paramIndex)).equals(BVariableType.ANY.getString())) {
                    jdiArgValue = getValueAsObject(context, jdiArgValue);
                }

                if (params.get(paramIndex).kind() == SyntaxKind.REST_PARAM) {
                    for (Map.Entry<String, ParameterNode> entry : remainingParams.entrySet()) {
                        SyntaxKind parameterType = entry.getValue().kind();
                        if (parameterType == SyntaxKind.REST_PARAM) {
                            restParamName = entry.getKey();
                            restParamTypeName = ((RestParameterNode) entry.getValue()).typeName().toSourceCode().trim();
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
                        throw createEvaluationException(TYPE_MISMATCH,
                                restParamTypeName.replaceAll(NameBasedTypeResolver.ARRAY_TYPE_SUFFIX, ""),
                                argValue.getType().getString(), restParamName);
                    }
                    restValues.add(jdiArgValue);
                    remainingParams.remove(restParamName);
                } else {
                    String parameterName = getParameterName(params.get(paramIndex));
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
                if (getParameterTypeName(params.get(paramIndex)).equals(BVariableType.ANY.getString())) {
                    argValue = getValueAsObject(context, argValue);
                }
                argValues.put(argName, argValue);
                remainingParams.remove(argName);
                paramIndex++;
            } else if (argType == ArgType.REST) {
                if (namedArgsFound) {
                    throw createEvaluationException("rest args are not allowed after named args.");
                }

                for (Map.Entry<String, ParameterNode> entry : remainingParams.entrySet()) {
                    SyntaxKind parameterType = entry.getValue().kind();
                    if (parameterType == SyntaxKind.REST_PARAM) {
                        restParamName = entry.getKey();
                        restParamTypeName = ((RestParameterNode) entry.getValue()).typeName().toSourceCode().trim();
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

        for (Map.Entry<String, ParameterNode> entry : remainingParams.entrySet()) {
            String paramName = entry.getKey();
            SyntaxKind parameterType = entry.getValue().kind();
            if (parameterType == SyntaxKind.REQUIRED_PARAM) {
                throw createEvaluationException("missing required parameter '" + paramName + "'.");
            } else if (parameterType == SyntaxKind.DEFAULTABLE_PARAM) {
                argValues.put(paramName + DEFAULTABLE_PARAM_SUFFIX, VMUtils.make(context, 0).getJdiValue());
            } else if (parameterType == SyntaxKind.REST_PARAM) {
                restParamTypeName = ((RestParameterNode) entry.getValue()).typeName().toSourceCode().trim();
                restArrayType = resolveType(context, restParamTypeName);
                argValues.put(paramName, getRestArgArray(context, restArrayType));
                restArgsProcessed = true;
            }
        }

        if (restArgsFound && !restArgsProcessed) {
            argValues.put(restParamName, getRestArgArray(context, restArrayType, restValues.toArray(new Value[0])));
        }
        return argValues;
    }

    private static Value getRestArgArray(SuspendedContext context, Value arrayType, Value... values)
            throws EvaluationException {

        List<String> argTypeNames = new ArrayList<>();
        argTypeNames.add(B_TYPE_CLASS);
        argTypeNames.add(B_VALUE_ARRAY_CLASS);
        RuntimeStaticMethod getRestArgArray = getRuntimeMethod(context, B_DEBUGGER_RUNTIME_CLASS,
                GET_REST_ARG_ARRAY_METHOD, argTypeNames);
        List<Value> argValues = new ArrayList<>();
        argValues.add(arrayType);
        argValues.addAll(Arrays.asList(values));
        getRestArgArray.setArgValues(argValues);
        return getRestArgArray.invokeSafely();
    }

    private static Value getElementType(SuspendedContext context, Value arrayType) throws EvaluationException {
        ReferenceType arrayTypeRef = ((ObjectReference) arrayType).referenceType();
        List<Method> methods = arrayTypeRef.methodsByName(GET_ELEMENT_TYPE_METHOD);
        if (methods == null || methods.size() != 1) {
            throw createEvaluationException(OBJECT_METHOD_NOT_FOUND, GET_ELEMENT_TYPE_METHOD, "ArrayType");
        }
        RuntimeInstanceMethod method = new RuntimeInstanceMethod(context, arrayType, methods.get(0));
        method.setArgValues(new ArrayList<>());
        return method.invokeSafely();
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

    private static Value resolveType(SuspendedContext context, String arrayTypeName) throws EvaluationException {
        NameBasedTypeResolver bTypeResolver = new NameBasedTypeResolver(new EvaluationContext(context));
        List<Value> resolvedTypes = bTypeResolver.resolve(arrayTypeName);
        return resolvedTypes.size() > 1 ? bTypeResolver.getUnionTypeFrom(resolvedTypes) : resolvedTypes.get(0);
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

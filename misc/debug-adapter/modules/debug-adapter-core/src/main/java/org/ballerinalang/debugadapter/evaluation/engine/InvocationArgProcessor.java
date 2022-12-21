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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.DebugExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeInstanceMethod;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.CANNOT_INFER_PARAM_TYPE;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.OBJECT_METHOD_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.engine.NodeBasedArgProcessor.getParameterName;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_VALUE_ARRAY_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.GET_REST_ARG_ARRAY_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.REST_ARG_IDENTIFIER;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.SELF_VAR_NAME;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.STRAND_VAR_NAME;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;

/**
 * Validates and processes invocation arguments of ballerina functions and object methods.
 *
 * @since 2.0.0
 */
public abstract class InvocationArgProcessor {

    protected final SuspendedContext context;
    protected final String functionName;
    protected final Method jdiMethodReference;

    protected static final String DEFAULTABLE_PARAM_SUFFIX = "[Defaultable]";
    protected static final String GET_ELEMENT_TYPE_METHOD = "getElementType";
    protected static final String UNKNOWN_VALUE = "unknown";

    protected InvocationArgProcessor(SuspendedContext context, String functionName, Method jdiMethodReference) {
        this.context = context;
        this.functionName = functionName;
        this.jdiMethodReference = jdiMethodReference;
    }

    /**
     * Process the user provided function arguments (required/named/rest) and returns as an ordered list to be
     * aligned with the corresponding generated method in Ballerina runtime.
     *
     * @param argEvaluators list of evaluators related to the user provided arguments and yest to be evaluated.
     * @return an ordered argument list to be aligned with the corresponding generated method in Ballerina runtime.
     */
    public abstract List<Value> process(List<Map.Entry<String, Evaluator>> argEvaluators) throws EvaluationException;

    protected List<Value> getOrderedArgList(Map<String, Value> namedArgValues, FunctionSymbol definitionSymbol,
                                            FunctionDefinitionNode definitionNode) throws EvaluationException {
        try {
            List<Value> argValueList = new ArrayList<>();
            List<LocalVariable> args = jdiMethodReference.arguments();
            List<String> argNames = args.stream()
                    .filter(LocalVariable::isArgument)
                    .map(LocalVariable::name)
                    .collect(Collectors.toList());

            for (int i = 0, argNamesSize = argNames.size(); i < argNamesSize; i++) {
                String argName = argNames.get(i);

                // This is a hack to avoid the weird issue introduced after the "self" variable being added to the
                // variable table. Now all the object methods contain 'self' as a method argument when retrieving
                // from 'methodRef.arguments()', even if the actual method does not have it.
                if (argName.equals(SELF_VAR_NAME) || argName.equals(STRAND_VAR_NAME)) {
                    continue;
                }
                // If this is a defaultable parameter
                String defaultableArgName = argName + DEFAULTABLE_PARAM_SUFFIX;
                if (namedArgValues.get(argName) == null && namedArgValues.get(defaultableArgName) != null) {
                    if (definitionNode == null && definitionSymbol != null) {
                        definitionNode = getDefinitionNodeFrom(argName.replace(DEFAULTABLE_PARAM_SUFFIX, ""),
                                definitionSymbol);
                    }
                    if (definitionNode != null) {
                        argValueList.add(getDefaultValue(context, args.get(i), definitionNode));
                    }
                } else {
                    argValueList.add(namedArgValues.get(argName));
                }
            }

            return EvaluationUtils.getAsObjects(context, argValueList);
        } catch (AbsentInformationException e) {
            throw createEvaluationException(FUNCTION_EXECUTION_ERROR, jdiMethodReference.name());
        }
    }

    protected static Value getRestArgArray(SuspendedContext context, Value arrayType, Value... values)
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

    protected static Value getElementType(SuspendedContext context, Value arrayType) throws EvaluationException {
        ReferenceType arrayTypeRef = ((ObjectReference) arrayType).referenceType();
        List<Method> methods = arrayTypeRef.methodsByName(GET_ELEMENT_TYPE_METHOD);
        if (methods == null || methods.size() != 1) {
            throw createEvaluationException(OBJECT_METHOD_NOT_FOUND, GET_ELEMENT_TYPE_METHOD, "ArrayType");
        }
        RuntimeInstanceMethod method = new RuntimeInstanceMethod(context, arrayType, methods.get(0));
        method.setArgValues(new ArrayList<>());
        return method.invokeSafely();
    }

    protected static ArgType getArgType(Map.Entry<String, Evaluator> arg) {
        if (isPositionalArg(arg)) {
            return ArgType.POSITIONAL;
        } else if (isNamedArg(arg)) {
            return ArgType.NAMED;
        } else if (isRestArg(arg)) {
            return ArgType.REST;
        }
        return ArgType.UNKNOWN;
    }

    protected static Value resolveType(SuspendedContext context, String arrayTypeName) throws EvaluationException {
        NameBasedTypeResolver bTypeResolver = new NameBasedTypeResolver(new EvaluationContext(context));
        List<Value> resolvedTypes = bTypeResolver.resolve(arrayTypeName);
        return resolvedTypes.size() > 1 ? bTypeResolver.getUnionTypeFrom(resolvedTypes) : resolvedTypes.get(0);
    }

    private FunctionDefinitionNode getDefinitionNodeFrom(String argName, FunctionSymbol definitionSymbol)
            throws EvaluationException {
        try {
            TextRange textRange = definitionSymbol.getLocation().get().textRange();
            NonTerminalNode node = ((ModulePartNode) context.getDocument().syntaxTree().rootNode()).findNode(textRange);
            return (FunctionDefinitionNode) node;
        } catch (Exception e) {
            throw createEvaluationException(String.format("failed to evaluate the default value expression of the " +
                    "parameter '%s' in function '%s'.", argName, functionName));
        }
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

    private static Value getDefaultValue(SuspendedContext context, LocalVariable localVariable,
                                         FunctionDefinitionNode functionDefinition) throws EvaluationException {

        String name = localVariable.name();
        Optional<ParameterNode> paramNode = functionDefinition.functionSignature().parameters().stream()
                .filter(parameterNode -> getParameterName(parameterNode).equals(name))
                .findFirst();

        Node expression = ((DefaultableParameterNode) paramNode.get()).expression();
        if (expression.kind().equals(SyntaxKind.INFERRED_TYPEDESC_DEFAULT)) {
            throw createEvaluationException(CANNOT_INFER_PARAM_TYPE, getParameterName(paramNode.get()),
                    functionDefinition.functionName().text());
        }

        DebugExpressionEvaluator defaultValueEvaluator = new DebugExpressionEvaluator(new EvaluationContext(context));
        defaultValueEvaluator.setExpression(expression.toSourceCode());
        return defaultValueEvaluator.evaluate().getJdiValue();
    }

    /**
     * Possible types for invocation arguments.
     */
    protected enum ArgType {
        POSITIONAL,
        NAMED,
        REST,
        UNKNOWN
    }
}

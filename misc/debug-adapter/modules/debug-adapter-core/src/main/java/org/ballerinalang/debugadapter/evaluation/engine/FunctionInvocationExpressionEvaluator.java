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

import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import org.ballerinalang.debugadapter.DebugSourceType;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.EvaluationUtils;
import org.ballerinalang.debugadapter.evaluation.ExpressionEvaluator;
import org.ballerinalang.debugadapter.utils.PackageUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.REST_ARG_IDENTIFIER;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;

/**
 * Evaluator implementation for function invocation expressions.
 *
 * @since 2.0.0
 */
public class FunctionInvocationExpressionEvaluator extends Evaluator {

    private final FunctionCallExpressionNode syntaxNode;
    private final String functionName;
    private final List<Map.Entry<String, Evaluator>> argEvaluators;

    public FunctionInvocationExpressionEvaluator(SuspendedContext context, FunctionCallExpressionNode node,
                                                 List<Map.Entry<String, Evaluator>> argEvaluators) {
        super(context);
        this.syntaxNode = node;
        this.argEvaluators = argEvaluators;
        this.functionName = syntaxNode.functionName().toSourceCode();
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            Map<String, Value> argValueMap = validateAndProcessArguments();
            // First we try to find the matching JVM method from the JVM backend, among already loaded classes.
            Optional<GeneratedStaticMethod> jvmMethod = findFunctionFromLoadedClasses();
            if (jvmMethod.isEmpty()) {
                // If we cannot find the matching method within the loaded classes, then we try to forcefully load
                // all the generated classes related to the current module using the JDI classloader, and search
                // again.
                jvmMethod = loadFunction();
            }
            if (jvmMethod.isEmpty()) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                        functionName));
            }
            jvmMethod.get().setNamedArgValues(argValueMap);
            Value result = jvmMethod.get().invoke();
            return new BExpressionValue(context, result);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }

    /**
     * Searches for a matching jvm method for a given ballerina function using its syntax node and the debug context
     * information.
     *
     * @return the matching JVM method, if available
     */
    private Optional<GeneratedStaticMethod> findFunctionFromLoadedClasses() {
        List<ReferenceType> allClasses = context.getAttachedVm().allClasses();
        DebugSourceType sourceType = context.getSourceType();
        for (ReferenceType cls : allClasses) {
            try {
                // Expected class name should end with the file name of the ballerina source, only for single
                // ballerina sources. (We cannot be sure about the module context, as we can invoke any method
                // defined within the module.)
                if (sourceType == DebugSourceType.SINGLE_FILE && !cls.name().endsWith(context.getFileName())) {
                    continue;
                }
                // If the sources reside inside a ballerina module/project, generated class name should start with the
                // organization name of the ballerina module/project source.
                if (sourceType == DebugSourceType.PACKAGE && !cls.name().startsWith(context.getPackageOrg().get())) {
                    continue;
                }
                List<Method> methods = cls.methodsByName(functionName);
                for (Method method : methods) {
                    // Note - All the ballerina functions are represented as java static methods and all the generated
                    // jvm methods contain strand as its first argument.
                    if (method.isStatic()) {
                        return Optional.of(new GeneratedStaticMethod(context, cls, method));
                    }
                }
            } catch (ClassNotPreparedException ignored) {
                // Unprepared classes should be skipped.
            }
        }
        return Optional.empty();
    }

    /**
     * Loads the generated jvm method of the particular ballerina function.
     *
     * @return JvmMethod instance
     */
    private Optional<GeneratedStaticMethod> loadFunction() throws EvaluationException {
        // If the debug source is a ballerina module file and the method is still not loaded into the JVM, we have
        // iterate over all the classes generated for this particular ballerina module and check each class for a
        // matching method.
        if (context.getSourceType() == DebugSourceType.PACKAGE) {
            List<String> moduleFiles = PackageUtils.getModuleClassNames(context);
            for (String fileName : moduleFiles) {
                String className = fileName.replace(BAL_FILE_EXT, "").replace(File.separator, ".");
                className = className.startsWith(".") ? className.substring(1) : className;
                String qualifiedClassName = PackageUtils.getQualifiedClassName(context, className);
                ReferenceType refType = EvaluationUtils.loadClass(context, qualifiedClassName, functionName);
                List<Method> methods = refType.methodsByName(functionName);
                if (!methods.isEmpty()) {
                    return Optional.of(new GeneratedStaticMethod(context, refType, methods.get(0)));
                }
            }
            return Optional.empty();
        } else {
            // If the source is a single bal file, the method(class)must be loaded by now already.
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                    functionName));
        }
    }

    Map<String, Value> validateAndProcessArguments() throws EvaluationException {
        Optional<FunctionDefinitionNode> functionDefinition = new FunctionFinder(functionName).searchIn(context
                .getDocument().module());
        if (functionDefinition.isEmpty()) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                    functionName));
        }
        SeparatedNodeList<ParameterNode> params = functionDefinition.get().functionSignature().parameters();
        return mapArgs(context, params, argEvaluators);
    }

    static Map<String, Value> mapArgs(SuspendedContext context, SeparatedNodeList<ParameterNode> params,
                                      List<Map.Entry<String, Evaluator>> argEvaluators)
            throws EvaluationException {

        boolean namedArgsFound = false;
        boolean restArgsFound = false;

        List<Map.Entry<String, Evaluator>> args = argEvaluators;
        Map<String, Value> argValues = new HashMap<>();

        Map<String, ParameterNode> remainingParams = new HashMap<>();
        params.stream().forEach(parameterNode -> remainingParams.put(getParameterName(parameterNode), parameterNode));

        for (int i = 0; i < args.size(); i++) {
            Map.Entry<String, Evaluator> arg = args.get(i);
            ArgType argType = getArgType(arg);
            if (argType == ArgType.POSITIONAL) {
                if (namedArgsFound) {
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "positional args are not allowed after named args."));
                } else if (restArgsFound) {
                    throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                            "positional args are not allowed after rest args."));
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
                        "missing required parameter + '" + paramName + "'."));
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

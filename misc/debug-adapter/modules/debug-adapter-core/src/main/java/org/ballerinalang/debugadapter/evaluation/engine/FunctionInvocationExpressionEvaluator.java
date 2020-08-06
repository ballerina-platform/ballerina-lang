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

import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import org.ballerinalang.debugadapter.DebugSourceType;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.EvaluationUtils;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.utils.PackageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.STRAND_VAR_NAME;
import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;

/**
 * Function invocation expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class FunctionInvocationExpressionEvaluator extends Evaluator {

    private final FunctionCallExpressionNode syntaxNode;
    private final List<Evaluator> argEvaluators;

    public FunctionInvocationExpressionEvaluator(SuspendedContext context, FunctionCallExpressionNode node,
                                                 List<Evaluator> argEvaluators) {
        super(context);
        this.syntaxNode = node;
        this.argEvaluators = argEvaluators;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        Optional<JvmMethod> jvmMethod;
        try {
            // First we try to find the matching JVM method from the JVM backend, among already loaded classes.
            jvmMethod = findFunctionFromLoadedClasses();
            if (!jvmMethod.isPresent()) {
                // If we cannot find the matching method within the loaded classes, then we try to forcefully load
                // all the generated classes related to the current module using the JDI classloader, and search
                // again.
                jvmMethod = loadFunction(syntaxNode);
            }
            if (!jvmMethod.isPresent()) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                        syntaxNode.functionName().toString()));
            }

            ReferenceType classRef = jvmMethod.get().classRef;
            if (!(classRef instanceof ClassType)) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR
                        .getString(), syntaxNode.toString()));
            }
            Method methodRef = jvmMethod.get().methodRef;
            List<Value> argValueList = generateJvmArgs(jvmMethod.get());
            Value result = ((ClassType) classRef).invokeMethod(context.getOwningThread().getThreadReference(),
                    methodRef, argValueList, ObjectReference.INVOKE_SINGLE_THREADED);
            return new BExpressionValue(context, result);
        } catch (ClassNotLoadedException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                    syntaxNode.functionName().toString()));
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString(),
                    syntaxNode.toString()));
        }
    }

    /**
     * Searches for a matching jvm method for a given ballerina function using its syntax node and the debug context
     * information.
     *
     * @return the matching JVM method, if available
     */
    private Optional<JvmMethod> findFunctionFromLoadedClasses() {
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
                if (sourceType == DebugSourceType.MODULE && !cls.name().startsWith(context.getOrgName().get())) {
                    continue;
                }
                List<Method> methods = cls.methodsByName(syntaxNode.functionName().toString().trim());
                for (Method method : methods) {
                    // Note - All the ballerina functions are represented as java static methods and all the generated
                    // jvm methods contain strand as its first argument.
                    if (method.isStatic()) {
                        return Optional.of(new JvmMethod(cls, method));
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
     * @param functionNode syntax node
     * @return JvmMethod instance
     */
    private Optional<JvmMethod> loadFunction(FunctionCallExpressionNode functionNode) throws EvaluationException {
        // If the debug source is a ballerina module file and the method is still not loaded into the JVM, we have
        // iterate over all the classes generated for this particular ballerina module and check each class for a
        // matching method.
        if (context.getSourceType() == DebugSourceType.MODULE) {
            List<String> moduleFileNames = PackageUtils.getAllModuleFileNames(context.getBreakPointSourcePath());
            for (String fileName : moduleFileNames) {
                String className = fileName.replace(BAL_FILE_EXT, "").replace(File.separator, ".");
                className = className.startsWith(".") ? className.substring(1) : className;
                StringJoiner classNameJoiner = new StringJoiner(".");
                classNameJoiner.add(context.getOrgName().get()).add(context.getModuleName().get())
                        .add(context.getVersion().get().replace(".", "_")).add(className);
                ClassLoaderReference classLoader = context.getClassLoader();
                ReferenceType referenceType = EvaluationUtils.loadClass(context, syntaxNode, classNameJoiner.toString(),
                        classLoader);
                List<Method> methods = referenceType.methodsByName(syntaxNode.functionName().toString().trim());
                if (!methods.isEmpty()) {
                    return Optional.of(new JvmMethod(referenceType, methods.get(0)));
                }
            }
            return Optional.empty();
        } else {
            // If the source is a single bal file, the method(class)must be loaded by now already.
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                    syntaxNode.functionName().toString()));
        }
    }

    private List<Value> generateJvmArgs(JvmMethod method) throws EvaluationException {
        try {
            List<Value> argValueList = new ArrayList<>();
            // Evaluates all function argument expressions at first.
            for (Evaluator argEvaluator : argEvaluators) {
                argValueList.add(argEvaluator.evaluate().getJdiValue());
                // Assuming all the arguments are positional args.
                argValueList.add(EvaluationUtils.make(context, true).getJdiValue());
            }

            List<Type> types = method.methodRef.argumentTypes();
            // Removes injected arguments added during the jvm method gen phase.
            for (int index = types.size() - 1; index >= 0; index -= 2) {
                types.remove(index);
            }

            // Todo - IMPORTANT: Add remaining steps to validate and match named, defaultable and rest args

            // Todo - verify
            // Here we use the parent strand instance to execute the function invocation expression.
            Value parentStrand = getParentStrand();
            argValueList.add(0, parentStrand);
            return argValueList;
        } catch (ClassNotLoadedException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString(),
                    syntaxNode.functionName().toString()));
        }
    }

    /**
     * Returns the JDI value of the strand instance that is being used, by visiting visible variables of the given
     * debug context.
     *
     * @return JDI value of the strand instance that is being used
     */
    private Value getParentStrand() throws EvaluationException {
        try {
            Value strand = context.getFrame().getValue(context.getFrame().visibleVariableByName(STRAND_VAR_NAME));
            if (strand == null) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.STRAND_NOT_FOUND.getString(),
                        syntaxNode.functionName().toString()));
            }
            return strand;
        } catch (JdiProxyException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.STRAND_NOT_FOUND.getString(),
                    syntaxNode.functionName().toString()));
        }
    }

    /**
     * JDI based java method representation for a given ballerina function.
     */
    static class JvmMethod {
        final ReferenceType classRef;
        final Method methodRef;

        JvmMethod(ReferenceType classRef, Method methodRef) {
            this.classRef = classRef;
            this.methodRef = methodRef;
        }
    }
}

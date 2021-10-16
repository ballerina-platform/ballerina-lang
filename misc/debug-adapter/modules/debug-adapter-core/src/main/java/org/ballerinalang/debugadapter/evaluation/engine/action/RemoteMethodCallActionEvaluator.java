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

package org.ballerinalang.debugadapter.evaluation.engine.action;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.ClassDefinitionResolver;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.expression.MethodCallExpressionEvaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.GeneratedInstanceMethod;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.CLASS_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.REMOTE_METHOD_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.engine.InvocationArgProcessor.generateNamedArgs;

/**
 * Evaluator implementation for remote method call invocation actions.
 *
 * @since 2.0.0
 */
public class RemoteMethodCallActionEvaluator extends MethodCallExpressionEvaluator {

    private final RemoteMethodCallActionNode syntaxNode;
    private final String methodName;
    private final Evaluator objectExpressionEvaluator;
    private final List<Map.Entry<String, Evaluator>> argEvaluators;

    public RemoteMethodCallActionEvaluator(EvaluationContext context, RemoteMethodCallActionNode remoteMethodActionNode,
                                           Evaluator expression, List<Map.Entry<String, Evaluator>> argEvaluators) {
        super(context, null, expression, argEvaluators);
        this.syntaxNode = remoteMethodActionNode;
        this.objectExpressionEvaluator = expression;
        this.argEvaluators = argEvaluators;
        this.methodName = syntaxNode.methodName().toSourceCode().trim();
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            // Calls a remote method of a client object. This works the same as a method call expression, except that
            // it is used for a client object method with the remote qualifier.
            BExpressionValue result = objectExpressionEvaluator.evaluate();
            BVariable resultVar = VariableFactory.getVariable(context, result.getJdiValue());

            // If the expression result is an object, try invoking as an object method invocation.
            if (result.getType() != BVariableType.OBJECT) {
                throw createEvaluationException("invalid remote method call: expected a client object, but found " +
                        "'other'");
            }

            Value invocationResult = invokeRemoteMethod(resultVar);
            return new BExpressionValue(context, invocationResult);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }

    private Value invokeRemoteMethod(BVariable resultVar) throws EvaluationException {

        ClassDefinitionResolver classDefResolver = new ClassDefinitionResolver(context);
        String className = resultVar.getDapVariable().getValue();
        Optional<ClassSymbol> classDef = classDefResolver.findBalClassDefWithinModule(className);
        if (classDef.isEmpty()) {
            // Resolves the JNI signature to see if the object/class is defined with a dependency module.
            String signature = resultVar.getJvmValue().type().signature();
            if (!signature.startsWith(QUALIFIED_TYPE_SIGNATURE_PREFIX)) {
                throw createEvaluationException(CLASS_NOT_FOUND, className);
            }

            String[] signatureParts = signature.substring(1).split(JNI_SIGNATURE_SEPARATOR);
            if (signatureParts.length < 2) {
                throw createEvaluationException(CLASS_NOT_FOUND, className);
            }
            String orgName = signatureParts[0];
            String packageName = signatureParts[1];
            classDef = classDefResolver.findBalClassDefWithinDependencies(orgName, packageName, className);
        }

        if (classDef.isEmpty()) {
            throw createEvaluationException(CLASS_NOT_FOUND, className);
        }

        Optional<MethodSymbol> objectMethodDef = findObjectMethodInClass(classDef.get(), methodName);
        if (objectMethodDef.isEmpty()) {
            throw createEvaluationException(REMOTE_METHOD_NOT_FOUND, syntaxNode.methodName().toString().trim(),
                    className);
        }

        GeneratedInstanceMethod objectMethod = getRemoteMethodByName(resultVar, objectMethodDef.get());
        Map<String, Value> argValueMap = generateNamedArgs(context, methodName, objectMethodDef.get().typeDescriptor(),
                argEvaluators);
        objectMethod.setNamedArgValues(argValueMap);
        return objectMethod.invokeSafely();
    }

    private GeneratedInstanceMethod getRemoteMethodByName(BVariable objectVar, MethodSymbol methodDefinition)
            throws EvaluationException {
        try {
            ReferenceType objectRef = ((ObjectReference) objectVar.getJvmValue()).referenceType();
            int argsCountInDefinition = methodDefinition.typeDescriptor().params().get().size() +
                    (methodDefinition.typeDescriptor().restParam().isPresent() ? 1 : 0);

            List<Method> methods = objectRef.methodsByName(methodName);
            for (Method method : methods) {
                // Since Ballerina codegen phase introduces an additional boolean helper parameter for every
                // method parameter except for the `strand` parameter, total number of parameters defined in the runtime
                // method will be 2n + 1.
                int expectedArgsCountInRuntime = 2 * argsCountInDefinition + 1;
                if (method.argumentTypes().size() == expectedArgsCountInRuntime) {
                    return new GeneratedInstanceMethod(context, objectVar.getJvmValue(), methods.get(0));
                }
            }
            throw createEvaluationException(REMOTE_METHOD_NOT_FOUND, syntaxNode.methodName().toString().trim(),
                    objectVar.computeValue());
        } catch (ClassNotLoadedException e) {
            throw createEvaluationException(REMOTE_METHOD_NOT_FOUND, syntaxNode.methodName().toString().trim(),
                    objectVar.computeValue());
        }
    }
}

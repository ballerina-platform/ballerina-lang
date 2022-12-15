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

package org.ballerinalang.debugadapter.evaluation.engine.expression;

import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VoidValue;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.projects.Package;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.ClassDefinitionResolver;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.NodeBasedArgProcessor;
import org.ballerinalang.debugadapter.evaluation.engine.SymbolBasedArgProcessor;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.GeneratedInstanceMethod;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.GeneratedStaticMethod;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.CLASS_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.LANG_LIB_METHOD_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.LANG_LIB_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.OBJECT_METHOD_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.modifyName;
import static org.ballerinalang.debugadapter.evaluation.utils.LangLibUtils.LANG_LIB_PACKAGE_PREFIX;
import static org.ballerinalang.debugadapter.evaluation.utils.LangLibUtils.LANG_LIB_VALUE;
import static org.ballerinalang.debugadapter.evaluation.utils.LangLibUtils.getAssociatedLangLibName;
import static org.ballerinalang.debugadapter.evaluation.utils.LangLibUtils.getLangLibFunctionDefinition;
import static org.ballerinalang.debugadapter.evaluation.utils.LangLibUtils.getLangLibPackage;
import static org.ballerinalang.debugadapter.evaluation.utils.LangLibUtils.getQualifiedLangLibClassName;
import static org.ballerinalang.debugadapter.evaluation.utils.LangLibUtils.loadLangLibMethod;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Evaluator implementation for method call invocation expressions.
 *
 * @since 2.0.0
 */
public class MethodCallExpressionEvaluator extends Evaluator {

    private final ExpressionNode syntaxNode;
    private final String methodName;
    private final Evaluator objectExpressionEvaluator;
    private final List<Map.Entry<String, Evaluator>> argEvaluators;
    protected static final String QUALIFIED_TYPE_SIGNATURE_PREFIX = "L";
    protected static final String JNI_SIGNATURE_SEPARATOR = "/";

    public MethodCallExpressionEvaluator(EvaluationContext context, ExpressionNode methodCallExpressionNode,
                                         Evaluator expression, List<Map.Entry<String, Evaluator>> argEvaluators) {
        super(context);
        this.syntaxNode = methodCallExpressionNode;
        this.objectExpressionEvaluator = expression;
        this.argEvaluators = argEvaluators;
        if (syntaxNode instanceof MethodCallExpressionNode) {
            this.methodName = ((MethodCallExpressionNode) syntaxNode).methodName().toSourceCode().trim();
        } else if (syntaxNode instanceof RemoteMethodCallActionNode) {
            this.methodName = ((RemoteMethodCallActionNode) syntaxNode).methodName().toSourceCode().trim();
        } else {
            methodName = UNKNOWN_VALUE;
        }
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            // If the static type of expression is a subtype of object, and the object type includes a method named
            // method-name, then the method-call-expr is executed by calling that method on v.
            //
            // Otherwise, the method-call-expr will be turned into a call to a function in the lang library M,
            // where M is selected as follows.
            //  - If the static type of expression is a subtype of some basic type with identifier B, and the module
            //      lang.B contains a function method-name then M is B. The identifier for a basic type is the reserved
            //      identifier used in type descriptors for subtypes of that basic type, as listed in the Lang library
            //      section.
            //  - Otherwise, if the static type of expression is xml:Text and the module lang.string contains a
            //      function method-name, then M is string, and the result of evaluating expression is implicitly
            //      converted to a string before the function is called.
            //  - Otherwise, M is value.

            Value invocationResult = null;
            BExpressionValue result = objectExpressionEvaluator.evaluate();
            BVariable resultVar = VariableFactory.getVariable(context, result.getJdiValue());

            // If the expression result is an object, try invoking as an object method invocation.
            if (result.getType() == BVariableType.OBJECT) {
                invocationResult = invokeObjectMethod(resultVar);
                if (invocationResult == null) {
                    return new BExpressionValue(context, null);
                }
            }

            // Otherwise, try matching lang-lib methods.
            if (invocationResult == null || (invocationResult.virtualMachine() == null
                    && invocationResult.type() == null)) {
                invocationResult = invokeLangLibMethod(result);
            }

            return new BExpressionValue(context, invocationResult);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }

    private Value invokeObjectMethod(BVariable resultVar) throws EvaluationException {
        boolean isFoundObjectMethod = false;
        try {
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
                throw createEvaluationException(OBJECT_METHOD_NOT_FOUND, methodName.trim(), className);
            }

            isFoundObjectMethod = true;
            GeneratedInstanceMethod objectMethod = getObjectMethodByName(resultVar, methodName);
            SymbolBasedArgProcessor argProcessor = new SymbolBasedArgProcessor(context, methodName,
                    objectMethod.getJDIMethodRef(), objectMethodDef.get());
            List<Value> orderedArgsList = argProcessor.process(argEvaluators);
            objectMethod.setArgValues(orderedArgsList);
            return objectMethod.invokeSafely();
        } catch (EvaluationException e) {
            // If the object method is not found, we have to ignore the Evaluation Exception and try to find any
            // matching lang library functions.
            if (isFoundObjectMethod) {
                throw e;
            }

            // Returning an empty value, as returning `null` in here might lead to unexpected results, as the method
            // invocation can also return null if the program output is nil.
            return new VoidValue() {
                @Override
                public VirtualMachine virtualMachine() {
                    return null;
                }

                @Override
                public Type type() {
                    return null;
                }
            };
        }
    }

    private Value invokeLangLibMethod(BExpressionValue resultVar) throws EvaluationException {
        FunctionDefinitionNode langLibFunctionDef = null;
        GeneratedStaticMethod langLibMethod = null;

        // Tries to use the dedicated lang library functions.
        String langLibName = getAssociatedLangLibName(resultVar.getType());
        Optional<Package> langLibPkg = getLangLibPackage(context, langLibName);
        if (langLibPkg.isPresent()) {
            Optional<FunctionDefinitionNode> functionDef = getLangLibFunctionDefinition(langLibPkg.get(), methodName);
            if (functionDef.isPresent()) {
                String langLibCls = getQualifiedLangLibClassName(langLibPkg.get(), langLibName);
                langLibFunctionDef = functionDef.get();
                langLibMethod = loadLangLibMethod(context, resultVar, langLibCls, methodName);
            }
        }

        // Tries to use "value" lang library functions.
        if (langLibMethod == null) {
            Optional<Package> valueLibPkg = getLangLibPackage(context, LANG_LIB_VALUE);
            if (valueLibPkg.isEmpty()) {
                throw createEvaluationException(LANG_LIB_NOT_FOUND, LANG_LIB_PACKAGE_PREFIX + langLibName + ", " +
                        LANG_LIB_PACKAGE_PREFIX + LANG_LIB_VALUE);
            }

            Optional<FunctionDefinitionNode> functionDef = getLangLibFunctionDefinition(valueLibPkg.get(), methodName);
            if (functionDef.isEmpty()) {
                throw createEvaluationException(LANG_LIB_METHOD_NOT_FOUND, methodName, langLibName);
            }

            String langLibCls = getQualifiedLangLibClassName(valueLibPkg.get(), LANG_LIB_VALUE);
            langLibFunctionDef = functionDef.get();
            langLibMethod = loadLangLibMethod(context, resultVar, langLibCls, methodName);
        }

        argEvaluators.add(0, new AbstractMap.SimpleEntry<>("", objectExpressionEvaluator));
        NodeBasedArgProcessor argProcessor = new NodeBasedArgProcessor(context, methodName, langLibMethod
                .getJDIMethodRef(), langLibFunctionDef);
        List<Value> orderedArgsList = argProcessor.process(argEvaluators);
        langLibMethod.setArgValues(orderedArgsList);
        return langLibMethod.invokeSafely();
    }

    protected Optional<MethodSymbol> findObjectMethodInClass(ClassSymbol classDef, String methodName) {
        return classDef.methods().values()
                .stream()
                .filter(methodSymbol -> modifyName(methodSymbol.getName().get()).equals(methodName))
                .findFirst();
    }

    private GeneratedInstanceMethod getObjectMethodByName(BVariable objectVar, String methodName)
            throws EvaluationException {

        ReferenceType objectRef = ((ObjectReference) objectVar.getJvmValue()).referenceType();
        List<Method> methods = objectRef.methodsByName(methodName);
        if (methods == null || methods.size() != 1) {
            throw createEvaluationException(OBJECT_METHOD_NOT_FOUND, methodName.trim(), objectVar.computeValue());
        }
        return new GeneratedInstanceMethod(context, objectVar.getJvmValue(), methods.get(0));
    }
}

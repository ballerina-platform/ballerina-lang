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

package org.ballerinalang.debugadapter.evaluation.engine.expression;

import com.sun.jdi.Value;
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.IdentifierModifier;
import org.ballerinalang.debugadapter.evaluation.engine.ClassDefinitionResolver;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.CLASS_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.NON_PUBLIC_OR_UNDEFINED_ACCESS;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.NON_PUBLIC_OR_UNDEFINED_CLASS;
import static org.ballerinalang.debugadapter.evaluation.engine.EvaluationTypeResolver.isPublicSymbol;
import static org.ballerinalang.debugadapter.evaluation.engine.InvocationArgProcessor.generateNamedArgs;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_OBJECT_VALUE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_OBJECT_ARRAY_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.MODULE_VERSION_SEPARATOR_REGEX;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getAsJString;

/**
 * Evaluator implementation for implicit/explicit new expressions.
 *
 * @since 2.0.0
 */
public class NewExpressionEvaluator extends Evaluator {

    private final ExpressionNode syntaxNode;
    private final List<Map.Entry<String, Evaluator>> argEvaluators;
    private static final String OBJECT_INIT_METHOD_NAME = "init";

    public NewExpressionEvaluator(EvaluationContext context, ExpressionNode newExpressionNode, List<Map.Entry<String,
            Evaluator>> argEvaluators) {
        super(context);
        this.syntaxNode = newExpressionNode;
        this.argEvaluators = argEvaluators;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            // A new-expr constructs a new object or stream. The class-descriptor in an explicit-new-expr must refer to
            // a class or a stream type.
            //
            // An explicit-type-expr specifying a class-descriptor T has static type T, except that if T is an class
            // type and the type of the init method is E?, where E is a subtype of error, then it has static type T|E.
            //
            // An implicit-new-expr is equivalent to an explicit-new-expr that specifies the applicable contextually
            // expected type as the class-descriptor. An implicit-new-expr consisting of just new is equivalent to
            // new(). It is an error if the applicable contextually expected type is not a class or stream type.
            if (syntaxNode instanceof ImplicitNewExpressionNode) {
                throw createEvaluationException("Implicit new expressions are not supported by the evaluator. " +
                        "Try using the equivalent explicit expression by specifying the class descriptor " +
                        "(i.e. 'new T()') instead.");
            }

            Value value = invokeObjectInitMethod(((ExplicitNewExpressionNode) syntaxNode).typeDescriptor());
            return new BExpressionValue(context, value);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }

    private Value invokeObjectInitMethod(TypeDescriptorNode classType) throws EvaluationException {

        String className;
        Optional<String> modulePrefix;
        if (classType.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            modulePrefix = Optional.of(((QualifiedNameReferenceNode) classType).modulePrefix().text().trim());
            className = ((QualifiedNameReferenceNode) classType).identifier().text().trim();
        } else {
            className = classType.toSourceCode().trim();
            modulePrefix = Optional.empty();
        }
        // Need to decode the class name, since the Ballerina semantic model is based on decoded identifier names.
        className = IdentifierModifier.decodeIdentifier(className);
        ClassDefinitionResolver classDefResolver = new ClassDefinitionResolver(context);

        Optional<ClassSymbol> classSymbol;
        if (modulePrefix.isPresent()) {
            ModuleSymbol importedModule = resolvedImports.get(modulePrefix.get());
            classSymbol = classDefResolver.findBalClassDefWithinModule(importedModule, className);
        } else {
            classSymbol = classDefResolver.findBalClassDefWithinModule(className);
        }

        if (classSymbol.isEmpty()) {
            if (modulePrefix.isPresent()) {
                throw createEvaluationException(NON_PUBLIC_OR_UNDEFINED_CLASS, className, modulePrefix.get());
            } else {
                throw createEvaluationException(CLASS_NOT_FOUND, classType.toSourceCode().trim());
            }
        } else if (!isPublicSymbol(classSymbol.get())) {
            throw createEvaluationException(NON_PUBLIC_OR_UNDEFINED_ACCESS, classType.toSourceCode().trim());
        }

        ModuleID moduleId = classSymbol.get().getModule().get().id();
        Optional<MethodSymbol> initMethodRef = classSymbol.get().initMethod();
        if (initMethodRef.isEmpty() && !argEvaluators.isEmpty()) {
            throw createEvaluationException("too many arguments in call to '" + OBJECT_INIT_METHOD_NAME + "'.");
        }

        // Validates user provided args against the `init` method signature.
        if (initMethodRef.isPresent()) {
            generateNamedArgs(context, OBJECT_INIT_METHOD_NAME, initMethodRef.get().typeDescriptor(), argEvaluators);
        }
        List<String> argTypeNames = new ArrayList<>();
        argTypeNames.add(JAVA_STRING_CLASS);
        argTypeNames.add(JAVA_STRING_CLASS);
        argTypeNames.add(JAVA_STRING_CLASS);
        argTypeNames.add(JAVA_STRING_CLASS);
        argTypeNames.add(JAVA_OBJECT_ARRAY_CLASS);
        RuntimeStaticMethod createObjectMethod = EvaluationUtils.getRuntimeMethod(context, B_DEBUGGER_RUNTIME_CLASS,
                CREATE_OBJECT_VALUE_METHOD, argTypeNames);

        List<Value> argValues = new ArrayList<>();
        argValues.add(getAsJString(context, moduleId.orgName()));
        argValues.add(getAsJString(context, moduleId.moduleName()));
        argValues.add(getAsJString(context, moduleId.version().split(MODULE_VERSION_SEPARATOR_REGEX)[0]));
        argValues.add(getAsJString(context, className));
        for (Map.Entry<String, Evaluator> evaluator : argEvaluators) {
            try {
                Value jdiValue = evaluator.getValue().evaluate().getJdiValue();
                argValues.add(EvaluationUtils.getValueAsObject(context, jdiValue));
            } catch (EvaluationException e) {
                throw createEvaluationException("failed to resolve object init arguments due to an internal error.");
            }
        }
        createObjectMethod.setArgValues(argValues);
        return createObjectMethod.invokeSafely();
    }
}

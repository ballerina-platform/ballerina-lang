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
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.IdentifierModifier;
import org.ballerinalang.debugadapter.evaluation.engine.ClassDefinitionResolver;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.engine.InvocationArgProcessor.generateNamedArgs;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_OBJECT_VALUE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_OBJECT_ARRAY_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_STRING_CLASS;

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
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        "Implicit new expressions are not supported by the evaluator. Try using the equivalent " +
                                "explicit expression by specifying the class descriptor (i.e. 'new T()') instead."));
            }

            String className = ((ExplicitNewExpressionNode) syntaxNode).typeDescriptor().toSourceCode();
            // Need to decode the class name, since the Ballerina semantic model is based on decoded identifier names.
            className = IdentifierModifier.decodeIdentifier(className);
            Value value = invokeObjectInitMethod(className);
            return new BExpressionValue(context, value);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }

    private Value invokeObjectInitMethod(String className) throws EvaluationException {
        ClassDefinitionResolver classDefResolver = new ClassDefinitionResolver(context);
        Optional<ClassSymbol> classDef = classDefResolver.findBalClassDefWithinModule(className);

        if (classDef.isEmpty()) {
            // Todo - support object initialization for classes defined in dependency modules.
            throw new EvaluationException(String.format(EvaluationExceptionKind.CLASS_NOT_FOUND.getString(),
                    className));
        }

        Optional<MethodSymbol> initMethodRef = classDef.get().initMethod();
        if (initMethodRef.isEmpty() && !argEvaluators.isEmpty()) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                    "too many arguments in call to '" + OBJECT_INIT_METHOD_NAME + "'."));
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
        argValues.add(EvaluationUtils.getAsJString(context, context.getPackageOrg().orElse("")));
        argValues.add(EvaluationUtils.getAsJString(context, context.getPackageName().orElse("")));
        argValues.add(EvaluationUtils.getAsJString(context, context.getPackageMajorVersion().orElse("")));
        argValues.add(EvaluationUtils.getAsJString(context, className));
        for (Map.Entry<String, Evaluator> evaluator : argEvaluators) {
            try {
                Value jdiValue = evaluator.getValue().evaluate().getJdiValue();
                argValues.add(EvaluationUtils.getValueAsObject(context, jdiValue));
            } catch (EvaluationException e) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        "Failed to resolve object init arguments due to an internal error."));
            }
        }
        createObjectMethod.setArgValues(argValues);
        return createObjectMethod.invokeSafely();
    }
}

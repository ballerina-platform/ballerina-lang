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

package org.ballerinalang.debugadapter.evaluation.engine;

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_CHECKER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_UTILS_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CHECK_IS_TYPE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_OBJECT_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.VALUE_FROM_STRING_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getValueAsObject;

/**
 * Type test expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class TypeTestExpressionEvaluator extends Evaluator {

    private final TypeTestExpressionNode syntaxNode;
    private final Evaluator exprEvaluator;

    public TypeTestExpressionEvaluator(SuspendedContext context, TypeTestExpressionNode typeofExpressionNode,
                                       Evaluator exprEvaluator) {
        super(context);
        this.syntaxNode = typeofExpressionNode;
        this.exprEvaluator = exprEvaluator;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            BExpressionValue result = exprEvaluator.evaluate();
            // primitive types need to be handled separately, as the jvm runtime util method accepts only the sub
            // classes of 'java.lang.Object'. Therefore java primitive types are converted into their wrapper
            // implementations first.
            Value valueAsObject = getValueAsObject(context, result.getJdiValue());

            List<String> methodArgTypeNames = new ArrayList<>();
            methodArgTypeNames.add(JAVA_OBJECT_CLASS);
            methodArgTypeNames.add(B_TYPE_CLASS);
            RuntimeStaticMethod method = getRuntimeMethod(context, B_TYPE_CHECKER_CLASS, CHECK_IS_TYPE_METHOD,
                    methodArgTypeNames);

            List<Value> methodArgs = new ArrayList<>();
            methodArgs.add(valueAsObject);
            methodArgs.add(getTypeFromTypeName(syntaxNode.typeDescriptor().toSourceCode().trim()));
            method.setArgValues(methodArgs);
            return new BExpressionValue(context, method.invoke());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }

    private Value getTypeFromTypeName(String typeName) throws EvaluationException {
        // checks if the type is a predefined type.
        Optional<Value> typeValue = getPredefinedTypeFromTypeName(typeName);
        if (typeValue.isEmpty()) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                    "failed to resolve type: " + typeName));
        }
        return typeValue.get();
    }

    private Optional<Value> getPredefinedTypeFromTypeName(String typeName) throws EvaluationException {
        try {
            List<String> methodArgTypeNames = Collections.singletonList(JAVA_STRING_CLASS);
            RuntimeStaticMethod method = getRuntimeMethod(context, B_TYPE_UTILS_CLASS, VALUE_FROM_STRING_METHOD,
                    methodArgTypeNames);
            method.setArgValues(Collections.singletonList(context.getAttachedVm().mirrorOf(typeName)));
            return Optional.of(new BExpressionValue(context, method.invoke()).getJdiValue());
        } catch (EvaluationException e) {
            if (!e.getMessage().startsWith(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString())) {
                throw e;
            }
            return Optional.empty();
        }
    }
}

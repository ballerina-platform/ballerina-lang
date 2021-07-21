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
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_VALUE_ARRAY_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_VALUE_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_ERROR_VALUE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.REST_ARG_IDENTIFIER;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;

/**
 * Evaluator implementation for error constructor expressions.
 *
 * @since 2.0.0
 */
public class ErrorConstructorExpressionEvaluator extends Evaluator {

    private final ErrorConstructorExpressionNode syntaxNode;
    private final List<Map.Entry<String, Evaluator>> argEvaluators;

    public ErrorConstructorExpressionEvaluator(SuspendedContext context, ErrorConstructorExpressionNode expressionNode,
                                               List<Map.Entry<String, Evaluator>> argEvaluators) {
        super(context);
        this.syntaxNode = expressionNode;
        this.argEvaluators = argEvaluators;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            // An error constructor constructs a new error value. If an error-type-reference is specified, it must
            // denote a type that is a subtype of error.
            // The first positional-arg is of type string and specifies the error message;
            // the second positional-arg, if present, is of type error?, with a default of nil, and specifies the cause.
            if (syntaxNode.typeReference().isPresent()) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                        "Error constructor expressions with type references (e.g. '" + syntaxNode.typeReference().get()
                                .toSourceCode() + "') are not supported by the evaluator. "));
            }

            List<String> argTypeNames = new ArrayList<>();
            argTypeNames.add(B_VALUE_CLASS);
            argTypeNames.add(B_VALUE_CLASS);
            argTypeNames.add(B_VALUE_ARRAY_CLASS);
            RuntimeStaticMethod createErrorValueMethod = getRuntimeMethod(context, B_DEBUGGER_RUNTIME_CLASS,
                    CREATE_ERROR_VALUE_METHOD, argTypeNames);

            List<Value> argValues = new ArrayList<>();
            if (argEvaluators.isEmpty()) {
                throw new EvaluationException(EvaluationExceptionKind.MISSING_MESSAGE_IN_ERROR.getString());
            }
            for (int i = 0; i < argEvaluators.size(); i++) {
                if (i == 0) {
                    // Process error message.
                    if (!argEvaluators.get(i).getKey().isEmpty()) {
                        throw new EvaluationException(EvaluationExceptionKind.MISSING_MESSAGE_IN_ERROR.getString());
                    }
                    argValues.add(argEvaluators.get(i).getValue().evaluate().getJdiValue());
                    if (argEvaluators.size() == 1) {
                        argValues.add(null);
                    }
                } else if (i == 1) {
                    // Process error cause (optional).
                    if (argEvaluators.get(i).getKey().equals(REST_ARG_IDENTIFIER)) {
                        throw new EvaluationException(EvaluationExceptionKind.REST_ARG_IN_ERROR.getString());
                    } else if (!argEvaluators.get(i).getKey().isEmpty()) {
                        argValues.add(null);
                    }
                    argValues.add(argEvaluators.get(i).getValue().evaluate().getJdiValue());
                } else {
                    // Process error details (optional).
                    if (argEvaluators.get(i).getKey().isEmpty()) {
                        throw new EvaluationException(EvaluationExceptionKind.ADDITIONAL_ARG_IN_ERROR.getString());
                    } else if (argEvaluators.get(i).getKey().equals(REST_ARG_IDENTIFIER)) {
                        throw new EvaluationException(EvaluationExceptionKind.REST_ARG_IN_ERROR.getString());
                    }
                    argValues.add(VMUtils.make(context, argEvaluators.get(i).getKey()).getJdiValue());
                    argValues.add(argEvaluators.get(i).getValue().evaluate().getJdiValue());
                }
            }
            createErrorValueMethod.setArgValues(argValues);
            return new BExpressionValue(context, createErrorValueMethod.invokeSafely());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }
}

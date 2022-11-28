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

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.NameBasedTypeResolver;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.VariableUtils;

import java.util.LinkedList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.NAME_REF_RESOLVING_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.VARIABLE_EXECUTION_ERROR;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_VALUE_CREATOR_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_TYPEDESC_VALUE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;

/**
 * Simple name reference evaluator implementation.
 *
 * @since 2.0.0
 */
public class SimpleNameReferenceEvaluator extends Evaluator {

    private final String nameReference;

    public SimpleNameReferenceEvaluator(EvaluationContext context, SimpleNameReferenceNode node) {
        super(context);
        this.nameReference = node.name().text();
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            Value value = null;
            try {
                value = VariableUtils.fetchVariableValue(context, nameReference);
            } catch (EvaluationException ignored) {
            }

            if (value == null) {
                NameBasedTypeResolver typeResolver = new NameBasedTypeResolver(evaluationContext);
                List<Value> resolvedTypes = typeResolver.resolve(nameReference);
                if (resolvedTypes.size() != 1) {
                    throw createEvaluationException(NAME_REF_RESOLVING_ERROR, nameReference);
                }
                value = resolvedTypes.get(0);
                List<String> argTypeNames = new LinkedList<>();
                argTypeNames.add(B_TYPE_CLASS);
                RuntimeStaticMethod createTypedescMethod = getRuntimeMethod(context, B_VALUE_CREATOR_CLASS,
                        CREATE_TYPEDESC_VALUE_METHOD, argTypeNames);
                List<Value> argValues = new LinkedList<>();
                argValues.add(value);
                createTypedescMethod.setArgValues(argValues);
                return new BExpressionValue(context, createTypedescMethod.invokeSafely());
            }

            return new BExpressionValue(context, value);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(VARIABLE_EXECUTION_ERROR, nameReference);
        }
    }
}

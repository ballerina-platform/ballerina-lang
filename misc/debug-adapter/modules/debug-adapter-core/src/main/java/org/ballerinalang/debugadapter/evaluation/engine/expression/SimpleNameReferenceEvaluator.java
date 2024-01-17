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

import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.VARIABLE_EXECUTION_ERROR;

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
            return new BExpressionValue(context, EvaluationUtils.fetchNameReferenceValue(evaluationContext,
                    nameReference));
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(VARIABLE_EXECUTION_ERROR, nameReference);
        }
    }
}

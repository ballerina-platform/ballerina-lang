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

import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;

/**
 * Evaluator implementation for optional field access expressions.
 *
 * @since 2.0.0
 */
public class OptionalFieldAccessExpressionEvaluator extends FieldAccessExpressionEvaluator {

    private static final String UNDEFINED_FIELD_ERROR_PREFIX = "Undefined field";
    private static final String UNDEFINED_ATTRIBUTE_ERROR_PREFIX = "Undefined attribute";

    public OptionalFieldAccessExpressionEvaluator(EvaluationContext context, Evaluator expression,
                                                  OptionalFieldAccessExpressionNode fieldAccessExpressionNode) {
        super(context, expression, fieldAccessExpressionNode);
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        // expression is evaluated resulting in a value v
        // if v is (), the result is ()
        // otherwise, if v has basic type error, the result is v
        // otherwise, if v does not have basic type mapping, the result is a new error value
        // otherwise, if v does not have a member whose key is field-name, the result is ()
        // otherwise, the result is the member of v whose key is field-name.
        try {
            return super.evaluate();
        } catch (EvaluationException e) {
            // if expression result does not have a member whose key is field-name, the result should be nil.
            if (e.getMessage().contains(UNDEFINED_FIELD_ERROR_PREFIX) ||
                    e.getMessage().contains(UNDEFINED_ATTRIBUTE_ERROR_PREFIX)) {
                return new BExpressionValue(context, null);
            }
            throw e;
        }
    }
}

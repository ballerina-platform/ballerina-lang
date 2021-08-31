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
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.GeneratedStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_RANGE_EXPR_HELPER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_INT_RANGE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getGeneratedMethod;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getValueAsObject;

/**
 * Evaluator implementation for range expressions.
 *
 * @since 2.0.0
 */
public class RangeExpressionEvaluator extends BinaryExpressionEvaluator {

    public RangeExpressionEvaluator(EvaluationContext context, BinaryExpressionNode node, Evaluator lhsEvaluator,
                                    Evaluator rhsEvaluator) {
        super(context, node, lhsEvaluator, rhsEvaluator);
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        // The result of a range-expr is a new object belonging to the object type Iterable<int,()> that will iterate
        // over a sequence of integers in increasing order, where the sequence includes all integers
        // that are less than / less than or equal to the value of the second expression.
        try {
            BExpressionValue lhsResult = lhsEvaluator.evaluate();
            BExpressionValue rhsResult = rhsEvaluator.evaluate();
            BVariable lVar = VariableFactory.getVariable(context, lhsResult.getJdiValue());
            BVariable rVar = VariableFactory.getVariable(context, rhsResult.getJdiValue());
            SyntaxKind operatorType = syntaxNode.operator().kind();

            // Determines the range (whether the end value should be exclusive), based on the operator type.
            boolean excludeEndValue = operatorType == SyntaxKind.DOUBLE_DOT_LT_TOKEN;
            Value excludeEndValueMirror = VMUtils.make(context, excludeEndValue).getJdiValue();
            List<Value> argList = new ArrayList<>();
            argList.add(getValueAsObject(context, lVar));
            argList.add(getValueAsObject(context, rVar));
            argList.add(getValueAsObject(context, excludeEndValueMirror));
            GeneratedStaticMethod createIntRangeMethod = getGeneratedMethod(context, B_RANGE_EXPR_HELPER_CLASS,
                    CREATE_INT_RANGE_METHOD);
            createIntRangeMethod.setArgValues(argList);
            return new BExpressionValue(context, createIntRangeMethod.invokeSafely());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }
}

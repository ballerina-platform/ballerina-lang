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
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.GeneratedStaticMethod;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_UTILS_HELPER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_VALUE_CREATOR_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_DECIMAL_VALUE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getGeneratedMethod;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getValueAsObject;

/**
 * Evaluator implementation for Basic literals.
 *
 * @since 2.0.0
 */
public class RangeExpressionEvaluator extends BinaryExpressionEvaluator {

    public RangeExpressionEvaluator(SuspendedContext context, BinaryExpressionNode node, Evaluator lhsEvaluator,
                                    Evaluator rhsEvaluator) {
        super(context, node, lhsEvaluator, rhsEvaluator);
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            BExpressionValue lhsResult = lhsEvaluator.evaluate();
            BExpressionValue rhsResult = rhsEvaluator.evaluate();
            BVariable lVar = VariableFactory.getVariable(context, lhsResult.getJdiValue());
            BVariable rVar = VariableFactory.getVariable(context, rhsResult.getJdiValue());
            SyntaxKind operatorType = syntaxNode.operator().kind();

            if (lhsResult.getType() != BVariableType.INT || rhsResult.getType() != BVariableType.INT) {
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
            }

            List<Value> argList = new ArrayList<>();
            argList.add(getValueAsObject(context, lVar));
            argList.add(getValueAsObject(context, rVar));
            GeneratedStaticMethod createIntRangeMethod = getGeneratedMethod(context, B_UTILS_HELPER_CLASS, "createIntRange");
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

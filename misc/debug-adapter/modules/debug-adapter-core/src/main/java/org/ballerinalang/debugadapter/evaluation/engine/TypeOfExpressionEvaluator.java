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

package org.ballerinalang.debugadapter.evaluation.engine;

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.TypeofExpressionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;

import java.util.Collections;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.B_TYPE_CHECKER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.GET_TYPEDESC_METHOD;
import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.JAVA_OBJECT_CLASS;
import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.getRuntimeMethod;
import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.getValueAsObject;

/**
 * `typeof` expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class TypeOfExpressionEvaluator extends Evaluator {

    private final TypeofExpressionNode syntaxNode;
    private final Evaluator exprEvaluator;

    public TypeOfExpressionEvaluator(SuspendedContext context, TypeofExpressionNode typeofExpressionNode,
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
            List<String> methodArgTypeNames = Collections.singletonList(JAVA_OBJECT_CLASS);
            RuntimeStaticMethod method = getRuntimeMethod(context, B_TYPE_CHECKER_CLASS, GET_TYPEDESC_METHOD,
                    methodArgTypeNames);
            method.setArgValues(Collections.singletonList(valueAsObject));
            return new BExpressionValue(context, method.invoke());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }
}

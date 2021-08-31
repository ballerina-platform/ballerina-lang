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
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.BallerinaTypeResolver;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;

import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.checkIsType;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getValueAsObject;

/**
 * Type test expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class TypeTestExpressionEvaluator extends Evaluator {

    private final TypeTestExpressionNode syntaxNode;
    private final Evaluator exprEvaluator;

    public TypeTestExpressionEvaluator(EvaluationContext context, TypeTestExpressionNode typetestExpressionNode,
                                       Evaluator exprEvaluator) {
        super(context);
        this.syntaxNode = typetestExpressionNode;
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

            // Resolves runtime type(s) using type descriptor nodes.
            // resolvedTypes.size() > 1 for union types.
            List<Value> resolvedTypes = BallerinaTypeResolver.resolve(context, syntaxNode.typeDescriptor());
            if (resolvedTypes.isEmpty()) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.TYPE_RESOLVING_ERROR.getString(),
                        syntaxNode.typeDescriptor().toSourceCode()));
            }

            for (Value type : resolvedTypes) {
                boolean typeMatched = checkIsType(context, valueAsObject, type);
                if (typeMatched) {
                    return VMUtils.make(context, true);
                }
            }
            return VMUtils.make(context, false);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }
}

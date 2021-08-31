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
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.BallerinaTypeResolver;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_CHECKER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CHECK_CAST_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_OBJECT_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getUnionTypeFrom;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getValueAsObject;

/**
 * Type cast expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class TypeCastExpressionEvaluator extends Evaluator {

    private final TypeCastExpressionNode syntaxNode;
    private final Evaluator exprEvaluator;

    public TypeCastExpressionEvaluator(EvaluationContext context, TypeCastExpressionNode typeCastExpressionNode,
                                       Evaluator exprEvaluator) {
        super(context);
        this.syntaxNode = typeCastExpressionNode;
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
            // Normally, the parameter for a type-cast-expr includes a type-descriptor. However, it is also allowed
            // for the parameter to consist only of annotations; in this case, the only effect of the type cast is for
            // the contextually expected type for expression to be augmented with the specified annotations.
            NodeList<AnnotationNode> annotations = syntaxNode.typeCastParam().annotations();

            // Todo - should process annotations?
            if (!annotations.isEmpty()) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Type " +
                        "casting with annotations is not supported by the evaluator."));
            }
            List<Value> resolvedTypes = BallerinaTypeResolver.resolve(context, syntaxNode.typeCastParam().type().get());
            if (resolvedTypes.isEmpty()) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.TYPE_RESOLVING_ERROR.getString(),
                        syntaxNode.typeCastParam().type().get().toSourceCode()));
            }

            // If the type descriptor is resolved into multiple types, creates a "BUnionType" instance by combining
            // its member types.
            Value bTypeDescriptor = resolvedTypes.size() > 1 ? getUnionTypeFrom(context, resolvedTypes) :
                    resolvedTypes.get(0);
            return checkCast(valueAsObject, bTypeDescriptor);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }

    /**
     * Performs casting operation using ballerina runtime utils.
     */
    private BExpressionValue checkCast(Value lhsExpressionResult, Value type) throws EvaluationException {
        List<String> methodArgTypeNames = new ArrayList<>();
        methodArgTypeNames.add(JAVA_OBJECT_CLASS);
        methodArgTypeNames.add(B_TYPE_CLASS);
        RuntimeStaticMethod method = getRuntimeMethod(context, B_TYPE_CHECKER_CLASS, CHECK_CAST_METHOD,
                methodArgTypeNames);

        List<Value> methodArgs = new ArrayList<>();
        methodArgs.add(lhsExpressionResult);
        methodArgs.add(type);
        method.setArgValues(methodArgs);
        return new BExpressionValue(context, method.invokeSafely());
    }
}

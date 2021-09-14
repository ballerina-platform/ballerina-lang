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
import io.ballerina.compiler.syntax.tree.AnnotAccessExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;
import org.ballerinalang.debugadapter.variable.BVariableType;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.TYPE_MISMATCH;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.GET_ANNOT_VALUE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_OBJECT_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getValueAsObject;

/**
 * Annotation access expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class AnnotationAccessExpressionEvaluator extends Evaluator {

    private final AnnotAccessExpressionNode syntaxNode;
    private final Evaluator exprEvaluator;

    public AnnotationAccessExpressionEvaluator(EvaluationContext context, AnnotAccessExpressionNode syntaxNode,
                                               Evaluator exprEvaluator) {
        super(context);
        this.syntaxNode = syntaxNode;
        this.exprEvaluator = exprEvaluator;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            // An annotation access expression is evaluated by first evaluating expression resulting in a typedesc
            // value t.
            // If t has an annotation with the tag referenced by annot-tag-reference, then the result of the
            // annotation access expression is the value of that annotation; otherwise, the result is nil.
            BExpressionValue result = exprEvaluator.evaluate();
            Value valueAsObject = getValueAsObject(context, result.getJdiValue());

            if (result.getType() != BVariableType.TYPE_DESC && result.getType() != BVariableType.UNKNOWN) {
                throw createEvaluationException(TYPE_MISMATCH, BVariableType.TYPE_DESC.getString(),
                        result.getType().getString(), syntaxNode.toSourceCode());
            }

            List<String> argTypeNames = new ArrayList<>();
            argTypeNames.add(JAVA_OBJECT_CLASS);
            argTypeNames.add(JAVA_STRING_CLASS);
            RuntimeStaticMethod getAnnotationValueMethod = getRuntimeMethod(context, B_DEBUGGER_RUNTIME_CLASS,
                    GET_ANNOT_VALUE_METHOD, argTypeNames);
            List<Value> argValues = new ArrayList<>();
            argValues.add(valueAsObject);
            argValues.add(EvaluationUtils.getAsJString(context, syntaxNode.annotTagReference().toSourceCode()));
            getAnnotationValueMethod.setArgValues(argValues);
            return new BExpressionValue(context, getAnnotationValueMethod.invokeSafely());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }
}

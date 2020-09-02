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

import io.ballerinalang.compiler.syntax.tree.SimpleNameReferenceNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.LocalVariableProxyImpl;

/**
 * Simple name reference evaluator implementation.
 *
 * @since 2.0.0
 */
public class SimpleNameReferenceEvaluator extends Evaluator {

    private final SimpleNameReferenceNode syntaxNode;
    private final String nameRef;

    public SimpleNameReferenceEvaluator(SuspendedContext context, SimpleNameReferenceNode node) {
        super(context);
        this.syntaxNode = node;
        this.nameRef = node.toSourceCode().trim();
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            LocalVariableProxyImpl jvmVar = context.getFrame().visibleVariableByName(nameRef);
            if (jvmVar == null) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.VARIABLE_NOT_FOUND.getString(),
                        syntaxNode.toString()));
            }
            return new BExpressionValue(context, context.getFrame().getValue(jvmVar));
        } catch (EvaluationException e) {
            throw e;
        } catch (JdiProxyException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.VARIABLE_NOT_FOUND.getString(),
                    nameRef));
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.VARIABLE_EXECUTION_ERROR.getString(),
                    nameRef));
        }
    }
}

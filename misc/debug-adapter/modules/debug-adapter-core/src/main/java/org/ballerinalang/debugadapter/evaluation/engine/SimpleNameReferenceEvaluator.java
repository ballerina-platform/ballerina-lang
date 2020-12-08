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

import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.LocalVariableProxyImpl;
import org.ballerinalang.debugadapter.utils.PackageUtils;

import java.util.List;
import java.util.Optional;

import static org.ballerinalang.debugadapter.utils.PackageUtils.INIT_CLASS_NAME;

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
        this.nameRef = node.name().text();
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            // Searches within global variable scope.
            Optional<BExpressionValue> result = searchInGlobalVariables();
            if (result.isPresent()) {
                return result.get();
            }
            // If no results found, searches within local variable scope.
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

    private Optional<BExpressionValue> searchInGlobalVariables() {
        String classQName = PackageUtils.getQualifiedClassName(context, INIT_CLASS_NAME);
        List<ReferenceType> cls = context.getAttachedVm().classesByName(classQName);
        if (cls.size() != 1) {
            return Optional.empty();
        }
        ReferenceType initClassReference = cls.get(0);
        Field field = initClassReference.fieldByName(nameRef);
        if (field == null) {
            return Optional.empty();
        }
        return Optional.of(new BExpressionValue(context, initClassReference.getValue(field)));
    }
}

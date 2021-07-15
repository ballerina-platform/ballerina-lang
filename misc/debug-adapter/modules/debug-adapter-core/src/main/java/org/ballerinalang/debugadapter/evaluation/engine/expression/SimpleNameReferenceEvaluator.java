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

import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.LocalVariableProxyImpl;
import org.ballerinalang.debugadapter.utils.PackageUtils;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.DebugVariableException;
import org.ballerinalang.debugadapter.variable.IndexedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;
import org.ballerinalang.debugadapter.variable.VariableUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.utils.PackageUtils.INIT_CLASS_NAME;

/**
 * Simple name reference evaluator implementation.
 *
 * @since 2.0.0
 */
public class SimpleNameReferenceEvaluator extends Evaluator {

    private final String nameReference;

    public SimpleNameReferenceEvaluator(SuspendedContext context, SimpleNameReferenceNode node) {
        super(context);
        this.nameReference = node.name().text();
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
            result = searchInLocalVariables();
            if (result.isPresent()) {
                return result.get();
            }

            throw new EvaluationException(String.format(EvaluationExceptionKind.VARIABLE_NOT_FOUND.getString(),
                    nameReference));
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.VARIABLE_EXECUTION_ERROR.getString(),
                    nameReference));
        }
    }

    private Optional<BExpressionValue> searchInGlobalVariables() {
        String classQName = PackageUtils.getQualifiedClassName(context, INIT_CLASS_NAME);
        List<ReferenceType> cls = context.getAttachedVm().classesByName(classQName);
        if (cls.size() != 1) {
            return Optional.empty();
        }
        ReferenceType initClassReference = cls.get(0);
        Field field = initClassReference.fieldByName(nameReference);
        if (field == null) {
            return Optional.empty();
        }
        return Optional.of(new BExpressionValue(context, initClassReference.getValue(field)));
    }

    private Optional<BExpressionValue> searchInLocalVariables() {
        try {
            LocalVariableProxyImpl jvmVar = context.getFrame().visibleVariableByName(nameReference);
            if (jvmVar != null) {
                return Optional.of(new BExpressionValue(context, context.getFrame().getValue(jvmVar)));
            }

            // As all the ballerina variables which are being used inside lambda functions are converted into maps
            // during the the runtime code generation, such local variables should be accessed in a different manner.
            List<LocalVariableProxyImpl> lambdaParamMaps = context.getFrame().visibleVariables().stream()
                    .filter(VariableUtils::isLambdaParamMap)
                    .collect(Collectors.toList());

            Optional<Value> localVariableMatch = lambdaParamMaps.stream()
                    .map(localVariableProxy -> {
                        try {
                            Value varValue = context.getFrame().getValue(localVariableProxy);
                            BVariable mapVar = VariableFactory.getVariable(context, varValue);
                            if (mapVar == null || mapVar.getBType() != BVariableType.MAP
                                    || !(mapVar instanceof IndexedCompoundVariable)) {
                                return null;
                            }
                            return ((IndexedCompoundVariable) mapVar).getChildByName(nameReference);
                        } catch (JdiProxyException | DebugVariableException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findAny();

            if (localVariableMatch.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new BExpressionValue(context, localVariableMatch.get()));
        } catch (JdiProxyException e) {
            return Optional.empty();
        }
    }
}

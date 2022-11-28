/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.NameBasedTypeResolver;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;

import java.util.LinkedList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.NAME_REF_RESOLVING_ERROR;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_VALUE_CREATOR_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_TYPEDESC_VALUE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;

/**
 * Builtin simple name reference evaluator implementation.
 *
 * @since 2201.3.1
 */
public class BuiltinSimpleNameReferenceEvaluator extends Evaluator {

    private final String typeName;

    public BuiltinSimpleNameReferenceEvaluator(EvaluationContext context, BuiltinSimpleNameReferenceNode node) {
        super(context);
        this.typeName = node.name().text().trim();
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        NameBasedTypeResolver builtInTypeResolver = new NameBasedTypeResolver(evaluationContext);
        List<Value> resolvedTypes = builtInTypeResolver.resolve(typeName);
        if (resolvedTypes.size() != 1) {
            throw createEvaluationException(NAME_REF_RESOLVING_ERROR, typeName);
        }

        Value type = resolvedTypes.get(0);
        List<String> argTypeNames = new LinkedList<>();
        argTypeNames.add(B_TYPE_CLASS);
        RuntimeStaticMethod createTypedescMethod = getRuntimeMethod(context, B_VALUE_CREATOR_CLASS,
                CREATE_TYPEDESC_VALUE_METHOD, argTypeNames);
        List<Value> argValues = new LinkedList<>();
        argValues.add(type);
        createTypedescMethod.setArgValues(argValues);
        return new BExpressionValue(context, createTypedescMethod.invokeSafely());
    }
}

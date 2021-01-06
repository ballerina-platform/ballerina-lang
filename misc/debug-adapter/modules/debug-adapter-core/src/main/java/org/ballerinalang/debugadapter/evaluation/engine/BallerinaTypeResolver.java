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

package org.ballerinalang.debugadapter.evaluation.engine;

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_UTILS_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.VALUE_FROM_STRING_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;

/**
 * Ballerina type resolver implementation for resolving ballerina runtime types using their syntax nodes.
 *
 * @since 2.0.0
 */
public class BallerinaTypeResolver {

    private static final String UNION_TYPE_SEPARATOR_REGEX = "\\|";

    /**
     * @param context        debug context
     * @param typeDescriptor type descriptor node
     * @return a collection of resolved types
     * @throws EvaluationException if unsupported type(s) found
     */
    public static List<Value> resolve(SuspendedContext context, Node typeDescriptor) throws EvaluationException {
        List<Value> resolvedTypes = new ArrayList<>();
        // If the type is a union, resolves each sub type iteratively.
        if (typeDescriptor.kind() == SyntaxKind.UNION_TYPE_DESC) {
            String[] unionTypes = typeDescriptor.toSourceCode().split(UNION_TYPE_SEPARATOR_REGEX);
            for (String typeName : unionTypes) {
                resolvedTypes.add(resolveSingleType(context, typeName));
            }
        } else {
            resolvedTypes.add(resolveSingleType(context, typeDescriptor.toSourceCode().trim()));
        }
        return resolvedTypes;
    }

    private static Value resolveSingleType(SuspendedContext context, String typeName) throws EvaluationException {
        Optional<Value> result = resolvePredefinedType(context, typeName);
        // Todo: Add support to resolve other named types.
        if (result.isEmpty()) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                    "failed to resolve type '" + typeName + "'."));
        }
        return result.get();
    }

    private static Optional<Value> resolvePredefinedType(SuspendedContext context, String typeName) {
        try {
            List<String> methodArgTypeNames = Collections.singletonList(JAVA_STRING_CLASS);
            RuntimeStaticMethod method = getRuntimeMethod(context, B_TYPE_UTILS_CLASS, VALUE_FROM_STRING_METHOD,
                    methodArgTypeNames);
            method.setArgValues(Collections.singletonList(context.getAttachedVm().mirrorOf(typeName)));
            return Optional.of(new BExpressionValue(context, method.invoke()).getJdiValue());
        } catch (EvaluationException e) {
            return Optional.empty();
        }
    }
}

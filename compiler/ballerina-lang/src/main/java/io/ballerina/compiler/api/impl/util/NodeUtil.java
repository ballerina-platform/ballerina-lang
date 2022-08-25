/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl.util;

import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.Optional;

/**
 * Common util methods related to BLangNode.
 */
public class NodeUtil {
    public static Optional<BType> getExpectedType(BLangNode bLangNode) {
        if (bLangNode == null) {
            return Optional.empty();
        }

        switch (bLangNode.getKind()) {
            case SIMPLE_VARIABLE_REF:
                return Optional.of(((BLangSimpleVarRef) bLangNode).expectedType);
            case FIELD_BASED_ACCESS_EXPR:
                return Optional.of(((BLangFieldBasedAccess) bLangNode).expectedType);
            case INVOCATION:
                return Optional.of(((BLangInvocation) bLangNode).expectedType);
            case FUNCTION_TYPE:
            default:
                return Optional.empty();
        }
    }
}

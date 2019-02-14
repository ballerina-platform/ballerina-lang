/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * @since 0.990.4
 */
public class ConstantValueResolver extends BLangNodeVisitor {

    private static final CompilerContext.Key<ConstantValueResolver> CONSTANT_VALUE_RESOLVER_KEY =
            new CompilerContext.Key<>();

    private ConstantValueResolver(CompilerContext context) {
        context.put(CONSTANT_VALUE_RESOLVER_KEY, this);
    }

    public static ConstantValueResolver getInstance(CompilerContext context) {
        ConstantValueResolver constantValueResolver = context.get(CONSTANT_VALUE_RESOLVER_KEY);
        if (constantValueResolver == null) {
            constantValueResolver = new ConstantValueResolver(context);
        }
        return constantValueResolver;
    }

    public BLangLiteral getValue(BLangIdentifier key, BLangRecordLiteral.BLangMapLiteral mapLiteral) {

        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : mapLiteral.keyValuePairs) {
            Object value = ((BLangLiteral) keyValuePair.key.expr).value;

            if (value.equals(key.value)) {
                if (keyValuePair.valueExpr.getKind() == NodeKind.LITERAL) {
                    return ((BLangLiteral) keyValuePair.valueExpr);
                } else {
                    // Todo
                }
            }
        }
        return null;
    }
}

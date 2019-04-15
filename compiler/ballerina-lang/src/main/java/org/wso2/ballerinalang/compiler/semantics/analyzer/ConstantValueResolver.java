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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * @since 0.990.4
 */
public class ConstantValueResolver extends BLangNodeVisitor {

    private static final CompilerContext.Key<ConstantValueResolver> CONSTANT_VALUE_RESOLVER_KEY =
            new CompilerContext.Key<>();

    private BLangIdentifier keyIdentifier;
    private BLangExpression result;

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

    public BLangExpression getValue(BLangIdentifier keyIdentifier, BLangMapLiteral mapLiteral) {
        this.result = null;
        this.keyIdentifier = keyIdentifier;

        mapLiteral.accept(this);

        return this.result;
    }

    @Override
    public void visit(BLangMapLiteral mapLiteral) {
        // Iterate through all key-value pairs in the record literal.
        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : mapLiteral.keyValuePairs) {
            //  Get the key.
            Object key = ((BLangLiteral) keyValuePair.key.expr).value;
            // If the key is equal to the value of the key, that means the key which we are looking for is
            // in the record literal.
            if (!key.equals(keyIdentifier.value)) {
                continue;
            }
            // Since we are looking for a literal which can be used as at compile time, it should be a literal.
            NodeKind nodeKind = keyValuePair.valueExpr.getKind();
            if (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL ||
                    nodeKind == NodeKind.RECORD_LITERAL_EXPR || nodeKind == NodeKind.CONSTANT_REF) {
                result = keyValuePair.valueExpr;
                return;
            }
            throw new RuntimeException("unsupported node kind");
        }
        // If this line is reached, that means there is an issue in ConstantValueChecker.
        throw new RuntimeException("constant key not found");
    }
}

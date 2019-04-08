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
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;

/**
 * @since 0.990.4
 */
public class ConstantValueChecker extends BLangNodeVisitor {

    private static final CompilerContext.Key<ConstantValueChecker> CONSTANT_VALUE_RESOLVER_KEY =
            new CompilerContext.Key<>();

    private BLangDiagnosticLog dlog;

    private BLangExpression referenceExpression;
    private BLangIdentifier keyIdentifier;

    private ConstantValueChecker(CompilerContext context) {
        context.put(CONSTANT_VALUE_RESOLVER_KEY, this);

        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public static ConstantValueChecker getInstance(CompilerContext context) {
        ConstantValueChecker constantValueChecker = context.get(CONSTANT_VALUE_RESOLVER_KEY);
        if (constantValueChecker == null) {
            constantValueChecker = new ConstantValueChecker(context);
        }
        return constantValueChecker;
    }

    void checkValue(BLangExpression referenceExpression, BLangIdentifier keyIdentifier, BLangRecordLiteral mapLiteral) {
        this.referenceExpression = referenceExpression;
        this.keyIdentifier = keyIdentifier;

        mapLiteral.accept(this);
    }

    // Note - We have implemented visit methods for both `BLangRecordLiteral` and `BLangMapLiteral` because the symbols
    // which comes from balo contains `BLangMapLiteral` value and the file which is being compiled will contain
    // `BLangRecordLiteral` value. These `BLangRecordLiteral` will be rewritten to `BLangMapLiteral` when they reach
    // the desugar.

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        // Iterate through all key-value pairs in the record literal.
        for (BLangRecordKeyValue keyValuePair : recordLiteral.keyValuePairs) {
            //  Get the key.
            Object key = ((BLangLiteral) keyValuePair.key.expr).value;

            // If the key is equal to the value of the keyIdentifier, that means the key which we are looking for is
            // in the record literal.
            if (!key.equals(keyIdentifier.value)) {
                continue;
            }

            // Since we are looking for a literal which can be used as at compile time, it should be a literal.
            NodeKind nodeKind = keyValuePair.valueExpr.getKind();
            if (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL ||
                    nodeKind == NodeKind.RECORD_LITERAL_EXPR || nodeKind == NodeKind.CONSTANT_REF) {
                return;
            }

            if (nodeKind == NodeKind.SIMPLE_VARIABLE_REF) {
                BSymbol symbol = ((BLangSimpleVarRef) keyValuePair.valueExpr).symbol;
                if (symbol.tag == SymTag.CONSTANT) {
                    return;
                }
            }

            throw new RuntimeException("unsupported node kind");
        }

        // If this line is reached, that means the key haven't been found. In that case, log a compilation error.
        dlog.error(keyIdentifier.pos, DiagnosticCode.KEY_NOT_FOUND, keyIdentifier, referenceExpression);
    }

    @Override
    public void visit(BLangMapLiteral mapLiteral) {
        // Iterate through all key-value pairs in the record literal.
        for (BLangRecordKeyValue keyValuePair : mapLiteral.keyValuePairs) {

            //  Get the key.
            Object key = ((BLangLiteral) keyValuePair.key.expr).value;

            // If the key is equal to the value of the keyIdentifier, that means the key which we are looking for is
            // in the record literal.
            if (!key.equals(keyIdentifier.value)) {
                continue;
            }

            // Since we are looking for a literal which can be used as at compile time, it should be a literal.
            NodeKind nodeKind = keyValuePair.valueExpr.getKind();
            if (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL ||
                    nodeKind == NodeKind.RECORD_LITERAL_EXPR || nodeKind == NodeKind.CONSTANT_REF) {
                return;
            }

            throw new RuntimeException("unsupported node kind");
        }
        // If this line is reached, that means the key haven't been found. In that case, log a compilation error.
        dlog.error(keyIdentifier.pos, DiagnosticCode.KEY_NOT_FOUND, keyIdentifier, referenceExpression);
    }
}

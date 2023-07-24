/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.CollectContextInvocationNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.Arrays;

/**
 * Implementation of {@link org.ballerinalang.model.tree.expressions.CollectContextInvocationNode}.
 *
 * @since 2201.7.0
 */
public class BLangCollectContextInvocation extends BLangExpression implements CollectContextInvocationNode {
    public BLangInvocation invocation;

    @Override
    public NodeKind getKind() {
        return NodeKind.COLLECT_CONTEXT_INVOCATION;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder br = new StringBuilder();
        br.append("(");
        if (invocation.expr != null) {
            // Action invocation or lambda invocation.
            br.append(invocation.expr).append(".");
        } else if (invocation.pkgAlias != null && !invocation.pkgAlias.getValue().isEmpty()) {
            br.append(invocation.pkgAlias).append(":");
        }
        br.append(invocation.name == null ? String.valueOf(invocation.symbol.name) : String.valueOf(invocation.name));
        br.append("(");
        if (invocation.argExprs.size() > 0) {
            String s = Arrays.toString(invocation.argExprs.toArray());
            br.append(s, 1, s.length() - 1);
        }
        br.append("))");
        return br.toString();
    }
}

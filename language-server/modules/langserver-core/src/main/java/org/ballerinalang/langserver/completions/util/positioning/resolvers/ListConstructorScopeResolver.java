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
package org.ballerinalang.langserver.completions.util.positioning.resolvers;

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.langserver.completions.util.CompletionVisitorUtil;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * List Constructor scope position resolver.
 *
 * @since 2.0.0
 */
public class ListConstructorScopeResolver extends CursorPositionResolver {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCursorBeforeNode(DiagnosticPos position, TreeVisitor treeVisitor, LSContext ctx, BLangNode node) {
        if (!(node.parent instanceof BLangListConstructorExpr)) {
            return false;
        }
        BLangListConstructorExpr listExpr = (BLangListConstructorExpr) node.parent;
        return CompletionVisitorUtil.isCursorWithinBlock(listExpr.pos, treeVisitor.getSymbolEnv(), ctx, treeVisitor);
    }
}

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
package org.ballerinalang.langserver.extensions.ballerina.syntaxhighlighter;

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.tree.*;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;

import java.util.List;
/**
 * Finds the symbols for Semantic Syntax Highlighting.
 *
 * @since 1.0.2
 */
public class SemanticHighlightingVisitor extends LSNodeVisitor {

    private List<SemanticHighlightProvider.HighlightInfo> list;

    public SemanticHighlightingVisitor(LSContext context) {
        this.list = context.get(SemanticHighlightKeys.SEMANTIC_HIGHLIGHTING_KEY);
    }

    @Override
    public void visit(BLangPackage pkgNode) {

        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;
        topLevelNodes.forEach(topLevelNode -> this.acceptNode((BLangNode) topLevelNode));

    }

    private void acceptNode(BLangNode node) {
        node.accept(this);
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {

        super.visit(compUnit);
    }

    @Override
    public void visit(BLangFunction funcNode) {

        this.visit(funcNode.body);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        super.visit(blockNode);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        if(CommonUtil.isClientObject(varNode.symbol)){
            SemanticHighlightProvider.HighlightInfo highlightInfo = new SemanticHighlightProvider.HighlightInfo(ScopeEnum.ENDPOINT, varNode.name);
            list.add(highlightInfo);
        }
    }
}

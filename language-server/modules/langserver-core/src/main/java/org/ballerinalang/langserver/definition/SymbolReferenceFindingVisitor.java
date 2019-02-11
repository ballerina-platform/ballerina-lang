/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.definition;

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Node Visitor to find the symbol references in different compilation units in multiple packages.
 *
 * @since 0.990.4
 */
public class SymbolReferenceFindingVisitor extends LSNodeVisitor {

    private LSContext lsContext;
    private boolean terminateVisitor = false;
    private SymbolReferencesModel symbolReferences;
    private List<Integer> referenceLines;
    private String tokenName;
    private String cUnitName;
    private int cursorLine;
    private int cursorCol;
    private boolean currentCUnitMode;
    private String pkgName;

    public SymbolReferenceFindingVisitor(LSContext lsContext, String pkgName, List<TokenReferenceModel> tokenRefs,
                                         boolean currentCUnitMode) {
        this.lsContext = lsContext;
        this.symbolReferences = lsContext.get(NodeContextKeys.REFERENCES_KEY);
        this.tokenName = lsContext.get(NodeContextKeys.NODE_NAME_KEY);
        this.referenceLines = tokenRefs.stream()
                .map(tokenReferenceModel -> tokenReferenceModel.getRange().getStart().getLine())
                .collect(Collectors.toList());
        Position position = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        this.cursorLine = position.getLine();
        this.cursorCol = position.getCharacter();
        this.currentCUnitMode = currentCUnitMode;
        this.pkgName = pkgName;
    }

    public SymbolReferenceFindingVisitor(LSContext lsContext, String pkgName, List<TokenReferenceModel> tokenRefs) {
        this(lsContext, pkgName, tokenRefs, false);
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        this.cUnitName = compUnit.name;
        String currentPkgName = this.lsContext.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        String currentCUnitName = this.lsContext.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);

        // Avoid visiting the current compilation unit if the mode is not current cUnit mode
        if (currentPkgName.equals(this.pkgName) && currentCUnitName.equals(this.cUnitName) && !this.currentCUnitMode) {
            return;
        }
        compUnit.getTopLevelNodes().forEach(topLevelNode -> this.acceptNode((BLangNode) topLevelNode));
    }

    @Override
    public void visit(BLangFunction funcNode) {
        if (!this.isWithinNodeRange(funcNode)) {
            return;
        }
        if (funcNode.getName().value.equals(this.tokenName)) {
            this.addSymbol(funcNode.symbol, true, funcNode.pos);
        } else {
            // analyze the function parameters
        }

        this.acceptNode(funcNode.body);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.getStatements().forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        if (!this.isWithinNodeRange(varDefNode)) {
            return;
        }
        BLangSimpleVariable variable = varDefNode.var;
        if (variable.name.value.equals(this.tokenName)) {
            this.addSymbol(variable.symbol, true, varDefNode.pos);
        }
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        if (!this.isWithinNodeRange(exprStmtNode)) {
            return;
        }
        this.acceptNode(exprStmtNode.expr);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (!this.isWithinNodeRange(invocationExpr)) {
            return;
        }
        if (invocationExpr.getName().getValue().equals(this.tokenName)) {
            DiagnosticPos invocationPos = invocationExpr.pos;
            DiagnosticPos pos = new DiagnosticPos(invocationPos.src, invocationPos.sLine, invocationPos.eLine,
                    invocationPos.sCol, invocationPos.sCol + this.tokenName.length());
            this.addSymbol(invocationExpr.symbol, false, pos);
        }
    }

    private void acceptNode(BLangNode node) {
        if (this.terminateVisitor) {
            return;
        }
        node.accept(this);
    }

    private boolean isWithinNodeRange(BLangNode node) {
        return this.referenceLines.stream().anyMatch(refLine -> node.pos.sLine <= refLine && node.pos.eLine >= refLine);
    }

    private SymbolReferencesModel.Reference getSymbolReference(BSymbol symbol, DiagnosticPos position) {
        String pkgName = symbol.pkgID.nameComps.stream().map(Name::getValue).collect(Collectors.joining("."));
        return new SymbolReferencesModel.Reference(position, symbol, this.cUnitName, pkgName);
    }

    private void addSymbol(BSymbol bSymbol, boolean isDefinition, DiagnosticPos position) {
        DiagnosticPos zeroBasedPos = CommonUtil.toZeroBasedPosition(position);
        SymbolReferencesModel.Reference ref = this.getSymbolReference(bSymbol, zeroBasedPos);
        if (this.currentCUnitMode && this.cursorLine == zeroBasedPos.sLine && this.cursorCol >= zeroBasedPos.sCol
                && this.cursorCol <= zeroBasedPos.eCol) {
            // This is the symbol at current cursor position
            this.symbolReferences.setSymbolAtCursor(ref);
            return;
        }

        if (isDefinition) {
            this.symbolReferences.addDefinition(ref);
            return;
        }

        this.symbolReferences.addReference(ref);
    }
}

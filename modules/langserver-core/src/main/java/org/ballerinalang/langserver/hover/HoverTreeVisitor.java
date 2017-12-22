/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.hover;

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.hover.constants.HoverConstants;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Tree visitor for the hover functionality.
 */
public class HoverTreeVisitor extends BLangNodeVisitor {

    private String fileName;
    private Position position;
    private boolean terminateVisitor = false;
    private SymbolEnter symbolEnter;
    private TextDocumentServiceContext context;
    private Object previousNode;

    public HoverTreeVisitor(TextDocumentServiceContext context) {
        this.context = context;
        this.position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        this.fileName = context.get(DocumentServiceKeys.FILE_NAME_KEY);
        this.symbolEnter = SymbolEnter.getInstance(context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));
        this.position.setLine(this.position.getLine() + 1);
    }

    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        // Then visit each top-level element sorted using the compilation unit
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes.stream().filter(node ->
                node.getPosition().getSource().getCompilationUnitName().equals(this.fileName)
        ).collect(Collectors.toList());

        if (topLevelNodes.isEmpty()) {
            terminateVisitor = true;
            acceptNode(null);
        } else {
            topLevelNodes.forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
        }
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgSymbol);
        acceptNode(pkgEnv.node);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangFunction funcNode) {
        // Check for native functions
        BSymbol funcSymbol = funcNode.symbol;
        if (Symbols.isNative(funcSymbol)) {
            return;
        }

        previousNode = funcNode;

        if (!funcNode.params.isEmpty()) {
            funcNode.params.forEach(this::acceptNode);
        }

        if (!funcNode.retParams.isEmpty()) {
            funcNode.retParams.forEach(this::acceptNode);
        }

        if (funcNode.body != null) {
            this.acceptNode(funcNode.body);
        }

        // Process workers
        if (!funcNode.workers.isEmpty()) {
            funcNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        previousNode = userDefinedType;
        userDefinedType.getPosition().eCol = userDefinedType.getPosition().sCol
                + userDefinedType.typeName.value.length()
                + (!userDefinedType.pkgAlias.value.isEmpty() ? (userDefinedType.pkgAlias.value + ":").length() : 0);
        if (HoverUtil.isMatchingPosition(userDefinedType.getPosition(), this.position)) {
            this.context.put(HoverKeys.HOVERING_OVER_NODE_KEY, userDefinedType);
            this.context.put(HoverKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            this.context.put(HoverKeys.NAME_OF_HOVER_NODE_KEY, userDefinedType.typeName);
            this.context.put(HoverKeys.PACKAGE_OF_HOVER_NODE_KEY, userDefinedType.type.tsymbol.pkgID);
            this.context.put(HoverKeys.SYMBOL_KIND_OF_HOVER_NODE_KEY, userDefinedType.type.tsymbol.kind);
            terminateVisitor = true;
        }
    }

    @Override
    public void visit(BLangEnum enumNode) {
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangVariable varNode) {
        previousNode = varNode;
        if (varNode.expr != null) {
            this.acceptNode(varNode.expr);
        }

        if (varNode.getTypeNode() != null && varNode.getTypeNode() instanceof BLangUserDefinedType) {
            this.acceptNode(varNode.getTypeNode());
        }
    }

    @Override
    public void visit(BLangLiteral litNode) {
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        varRefExpr.getPosition().eCol = varRefExpr.getPosition().sCol
                + varRefExpr.variableName.value.length()
                + (!varRefExpr.pkgAlias.value.isEmpty() ? (varRefExpr.pkgAlias.value + ":").length() : 0);
        if (varRefExpr.type.tsymbol.kind != null
                && (varRefExpr.type.tsymbol.kind.name().equals(HoverConstants.ENUM)
                || varRefExpr.type.tsymbol.kind.name().equals(HoverConstants.STRUCT))
                && HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position)) {
            this.context.put(HoverKeys.HOVERING_OVER_NODE_KEY, varRefExpr);
            this.context.put(HoverKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            varRefExpr.variableName.setValue(varRefExpr.type.tsymbol.name.getValue());
            this.context.put(HoverKeys.NAME_OF_HOVER_NODE_KEY, varRefExpr.variableName);
            this.context.put(HoverKeys.PACKAGE_OF_HOVER_NODE_KEY, varRefExpr.type.tsymbol.pkgID);
            this.context.put(HoverKeys.SYMBOL_KIND_OF_HOVER_NODE_KEY, varRefExpr.type.tsymbol.kind);
            terminateVisitor = true;
        }
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        previousNode = fieldAccessExpr;
        if (fieldAccessExpr.expr != null) {
            this.acceptNode(fieldAccessExpr.expr);
        }
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        previousNode = binaryExpr;
        if (binaryExpr.lhsExpr != null) {
            acceptNode(binaryExpr.lhsExpr);
        }

        if (binaryExpr.rhsExpr != null) {
            acceptNode(binaryExpr.rhsExpr);
        }
    }

    // Statements

    @Override
    public void visit(BLangBlockStmt blockNode) {
        if (!blockNode.stmts.isEmpty()) {
            blockNode.stmts.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        previousNode = varDefNode;
        if (varDefNode.getVariable() != null) {
            this.acceptNode(varDefNode.getVariable());
        }
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        previousNode = assignNode;
        if (assignNode.expr != null && assignNode.getPosition().sLine <= this.position.getLine()
                && assignNode.getPosition().eLine >= this.position.getLine()) {
            this.acceptNode(assignNode.expr);
        }

        if (!assignNode.varRefs.isEmpty()) {
            assignNode.varRefs.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        previousNode = exprStmtNode;
        if (HoverUtil.isMatchingPosition(exprStmtNode.pos, this.position)) {
            this.acceptNode(exprStmtNode.expr);
        }
    }

    @Override
    public void visit(BLangIf ifNode) {
        previousNode = ifNode;
        if (ifNode.expr != null && HoverUtil.isMatchingPosition(ifNode.expr.pos, this.position)) {
            acceptNode(ifNode.expr);
        } else if (ifNode.body != null) {
            acceptNode(ifNode.body);
        }
    }

    public void visit(BLangStruct structNode) {

    }

    public void visit(BLangWhile whileNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangTransformer transformerNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangConnector connectorNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangAction actionNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangService serviceNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangResource resourceNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangTryCatchFinally tryCatchFinally) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangCatch bLangCatch) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangAbort abortNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangWorker workerNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangReturn returnNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangNext nextNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangInvocation invocationExpr) {
        if (HoverUtil.isMatchingPosition(invocationExpr.getPosition(), this.position)) {
            this.context.put(HoverKeys.HOVERING_OVER_NODE_KEY, invocationExpr);
            this.context.put(HoverKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            this.context.put(HoverKeys.NAME_OF_HOVER_NODE_KEY, invocationExpr.name);
            this.context.put(HoverKeys.PACKAGE_OF_HOVER_NODE_KEY, invocationExpr.symbol.pkgID);
            this.context.put(HoverKeys.SYMBOL_KIND_OF_HOVER_NODE_KEY, invocationExpr.symbol.kind);
            this.terminateVisitor = true;
        }
    }

    /**
     * Accept node to visit.
     */
    private void acceptNode(BLangNode node) {
        if (this.terminateVisitor) {
            return;
        }
        node.accept(this);
    }
}

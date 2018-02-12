/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langserver.completions;

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.BlockStatementScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ConnectorScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.PackageNodeScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ResourceParamScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ServiceScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.TopLevelNodeScopeResolver;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConnectorInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeofExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBind;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangEndpointTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class TreeVisitor extends BLangNodeVisitor {
    private SymbolEnv symbolEnv;
    private SymbolResolver symbolResolver;
    private boolean terminateVisitor = false;
    private SymbolEnter symbolEnter;
    private SymbolTable symTable;
    private Stack<Node> blockOwnerStack;
    private Stack<BLangBlockStmt> blockStmtStack;
    private Class cursorPositionResolver;
    private TextDocumentServiceContext documentServiceContext;

    public TreeVisitor(TextDocumentServiceContext documentServiceContext) {
        this.documentServiceContext = documentServiceContext;
        init(this.documentServiceContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));
    }

    private void init(CompilerContext compilerContext) {
        blockOwnerStack = new Stack<>();
        blockStmtStack = new Stack<>();
        symbolEnter = SymbolEnter.getInstance(compilerContext);
        symTable = SymbolTable.getInstance(compilerContext);
        symbolResolver = SymbolResolver.getInstance(compilerContext);
        documentServiceContext.put(DocumentServiceKeys.SYMBOL_TABLE_KEY, symTable);
    }

    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);

        // Then visit each top-level element sorted using the compilation unit
        String fileName = documentServiceContext.get(DocumentServiceKeys.FILE_NAME_KEY);
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes.stream().filter(node ->
                    node.getPosition().getSource().getCompilationUnitName().equals(fileName)
            ).collect(Collectors.toList());

        if (topLevelNodes.isEmpty()) {
            terminateVisitor = true;
            acceptNode(null, null);
        } else {
            cursorPositionResolver = PackageNodeScopeResolver.class;
            topLevelNodes.forEach(topLevelNode -> {
                cursorPositionResolver = TopLevelNodeScopeResolver.class;
                acceptNode((BLangNode) topLevelNode, pkgEnv);
            });
        }
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgSymbol);
        acceptNode(pkgEnv.node, pkgEnv);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(xmlnsNode.getPosition(), xmlnsNode, this, this.documentServiceContext);
    }

    public void visit(BLangFunction funcNode) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(funcNode.getPosition(), funcNode, this, this.documentServiceContext)) {
            // Check for native functions
            BSymbol funcSymbol = funcNode.symbol;
            if (Symbols.isNative(funcSymbol)) {
                return;
            }
            SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope, symbolEnv);

            this.blockOwnerStack.push(funcNode);
            // Cursor position is calculated against the Block statement scope resolver
            cursorPositionResolver = BlockStatementScopeResolver.class;
            this.acceptNode(funcNode.body, funcEnv);
            this.blockOwnerStack.pop();

            // Process workers
            if (terminateVisitor && !funcNode.workers.isEmpty()) {
                terminateVisitor = false;
            }
            funcNode.workers.forEach(e -> this.symbolEnter.defineNode(e, funcEnv));
            funcNode.workers.forEach(e -> this.acceptNode(e, funcEnv));
        }
    }

    public void visit(BLangStruct structNode) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(structNode.getPosition(), structNode, this, this.documentServiceContext)) {
            BSymbol structSymbol = structNode.symbol;
            SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(structNode, structSymbol.scope, symbolEnv);

            if (structNode.fields.isEmpty()) {
                symbolEnv = structEnv;
                Map<Name, Scope.ScopeEntry> visibleSymbolEntries = this.resolveAllVisibleSymbols(symbolEnv);
                this.populateSymbols(visibleSymbolEntries, null);
                this.terminateVisitor = true;
            } else {
                // Since the struct definition do not have a block statement within, we push null
                this.blockStmtStack.push(null);
                this.blockOwnerStack.push(structNode);
                // Cursor position is calculated against the Block statement scope resolver
                this.cursorPositionResolver = BlockStatementScopeResolver.class;
                structNode.fields.forEach(field -> acceptNode(field, structEnv));
                this.blockStmtStack.pop();
                this.blockOwnerStack.pop();
            }
        }
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
    }

    @Override
    public void visit(BLangVariable varNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(varNode.getPosition(), varNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangLiteral litNode) {
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
    }

    // Statements

    @Override
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, symbolEnv);
        this.blockStmtStack.push(blockNode);
        // Cursor position is calculated against the Block statement scope resolver
        this.cursorPositionResolver = BlockStatementScopeResolver.class;
        if (blockNode.stmts.isEmpty()) {
            this.isCursorWithinBlock((DiagnosticPos) (this.blockOwnerStack.peek()).getPosition(), blockEnv);
        } else {
            blockNode.stmts.forEach(stmt -> this.acceptNode(stmt, blockEnv));
        }
        this.blockStmtStack.pop();
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(varDefNode.getPosition(), varDefNode, this, this.documentServiceContext)) {
            this.acceptNode(varDefNode.var, symbolEnv);
        }
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(assignNode.getPosition(), assignNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(exprStmtNode.getPosition(), exprStmtNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangIf ifNode) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(ifNode.getPosition(), ifNode, this, this.documentServiceContext)) {
            this.blockOwnerStack.push(ifNode);
            this.acceptNode(ifNode.body, symbolEnv);
            this.blockOwnerStack.pop();

            if (ifNode.elseStmt != null) {
                if (!(ifNode.elseStmt instanceof BLangIf)) {
                    this.blockOwnerStack.push(ifNode.elseStmt);
                }
                acceptNode(ifNode.elseStmt, symbolEnv);
                if (!(ifNode.elseStmt instanceof BLangIf)) {
                    this.blockOwnerStack.pop();
                }
            }
        }
    }

    public void visit(BLangWhile whileNode) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(whileNode.getPosition(), whileNode, this, this.documentServiceContext)) {

            this.blockOwnerStack.push(whileNode);
            this.acceptNode(whileNode.body, symbolEnv);
            this.blockOwnerStack.pop();
        }
    }

    public void visit(BLangTransformer transformerNode) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(transformerNode.getPosition(), transformerNode, this,
                        this.documentServiceContext)) {
            this.blockOwnerStack.push(transformerNode);
            // Cursor position is calculated against the Block statement scope resolver
            cursorPositionResolver = BlockStatementScopeResolver.class;
            this.acceptNode(transformerNode.body, symbolEnv);
            this.blockOwnerStack.pop();
        }
    }

    public void visit(BLangConnector connectorNode) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(connectorNode.getPosition(), connectorNode, this, this.documentServiceContext)) {
            BSymbol connectorSymbol = connectorNode.symbol;
            SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, symbolEnv);

            // TODO: Handle Annotation attachments
            if (connectorNode.actions.isEmpty() && connectorNode.varDefs.isEmpty()) {
                this.isCursorWithinBlock(connectorNode.getPosition(), connectorEnv);
            } else {
                // Since the connector def does not contains a block statement, we consider the block owner only.
                // Here it is Connector Definition
                this.blockOwnerStack.push(connectorNode);
                connectorNode.varDefs.forEach(varDef -> {
                    // Cursor position is calculated against the Connector scope resolver
                    cursorPositionResolver = ConnectorScopeResolver.class;
                    this.acceptNode(varDef, connectorEnv);
                });
                connectorNode.actions.forEach(action -> {
                    // Cursor position is calculated against the Connector scope resolver
                    cursorPositionResolver = ConnectorScopeResolver.class;
                    this.acceptNode(action, connectorEnv);
                });
                if (terminateVisitor) {
                    this.acceptNode(null, null);
                }
                this.blockOwnerStack.pop();
            }
        }
    }

    public void visit(BLangAction actionNode) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(actionNode.getPosition(), actionNode, this, this.documentServiceContext)) {
            BSymbol actionSymbol = actionNode.symbol;
            SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, symbolEnv);

            // TODO: Handle Annotation attachments
            // Cursor position is calculated against the resource parameter scope resolver since both are similar
            cursorPositionResolver = ResourceParamScopeResolver.class;
            actionNode.workers.forEach(w -> this.acceptNode(w, actionEnv));
            // Cursor position is calculated against the Block statement scope resolver
            cursorPositionResolver = BlockStatementScopeResolver.class;
            this.blockOwnerStack.push(actionNode);
            acceptNode(actionNode.body, actionEnv);
            this.blockOwnerStack.pop();
        }
    }

    public void visit(BLangService serviceNode) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(serviceNode.getPosition(), serviceNode, this, this.documentServiceContext)) {
            BSymbol serviceSymbol = serviceNode.symbol;
            SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, symbolEnv);
            
            if (serviceNode.resources.isEmpty() && serviceNode.vars.isEmpty()) {
                this.isCursorWithinBlock(serviceNode.getPosition(), serviceEnv);
            } else {
                // Since the service does not contains a block statement, we consider the block owner only.
                // Here it is service
                this.blockOwnerStack.push(serviceNode);
                serviceNode.vars.forEach(v -> {
                    this.cursorPositionResolver = ServiceScopeResolver.class;
                    this.acceptNode(v, serviceEnv);
                });
                serviceNode.resources.forEach(r -> {
                    this.cursorPositionResolver = ServiceScopeResolver.class;
                    this.acceptNode(r, serviceEnv);
                });
                if (terminateVisitor) {
                    this.acceptNode(null, null);
                }
                this.blockOwnerStack.pop();
            }
        }
    }

    public void visit(BLangResource resourceNode) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(resourceNode.getPosition(), resourceNode, this, this.documentServiceContext)) {
            BSymbol resourceSymbol = resourceNode.symbol;
            SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode,
                    resourceSymbol.scope, symbolEnv);

            // TODO:Handle Annotation attachments
            // Cursor position is calculated against the resource parameter scope resolver
            cursorPositionResolver = ResourceParamScopeResolver.class;
            resourceNode.workers.forEach(w -> this.acceptNode(w, resourceEnv));
            this.blockOwnerStack.push(resourceNode);
            // Cursor position is calculated against the Block statement scope resolver
            cursorPositionResolver = BlockStatementScopeResolver.class;
            acceptNode(resourceNode.body, resourceEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangTryCatchFinally tryCatchFinally) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(tryCatchFinally.getPosition(),
                        tryCatchFinally, this, this.documentServiceContext)) {

            this.blockOwnerStack.push(tryCatchFinally);
            this.acceptNode(tryCatchFinally.tryBody, symbolEnv);
            this.blockOwnerStack.pop();

            tryCatchFinally.catchBlocks.forEach(c -> {
                this.blockOwnerStack.push(c);
                this.acceptNode(c, symbolEnv);
                this.blockOwnerStack.pop();
            });
            if (tryCatchFinally.finallyBody != null) {
                // Check how we can add the blang node to stack
                this.blockOwnerStack.push(tryCatchFinally);
                this.acceptNode(tryCatchFinally.finallyBody, symbolEnv);
                this.blockOwnerStack.pop();
            }
        }
    }

    @Override
    public void visit(BLangCatch bLangCatch) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(bLangCatch.getPosition(), bLangCatch, this, this.documentServiceContext)) {
            SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(bLangCatch.body, symbolEnv);
            this.acceptNode(bLangCatch.param, catchBlockEnv);

            this.blockOwnerStack.push(bLangCatch);
            this.acceptNode(bLangCatch.body, catchBlockEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        this.blockOwnerStack.push(transactionNode);
        this.acceptNode(transactionNode.transactionBody, symbolEnv);
        this.blockOwnerStack.pop();

        if (transactionNode.failedBody != null) {
            this.blockOwnerStack.push(transactionNode);
            this.acceptNode(transactionNode.failedBody, symbolEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(abortNode.getPosition(), abortNode, this, this.documentServiceContext);
    }

    private BLangVariableDef createVarDef(BLangVariable var) {
        BLangVariableDef varDefNode = new BLangVariableDef();
        varDefNode.var = var;
        varDefNode.pos = var.pos;
        return varDefNode;
    }

    private BLangBlockStmt generateCodeBlock(StatementNode... statements) {
        BLangBlockStmt block = new BLangBlockStmt();
        for (StatementNode stmt : statements) {
            block.addStatement(stmt);
        }
        return block;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        SymbolEnv folkJoinEnv = SymbolEnv.createFolkJoinEnv(forkJoin, this.symbolEnv);
        // TODO: check the symbolEnter.defineNode
        forkJoin.workers.forEach(e -> this.symbolEnter.defineNode(e, folkJoinEnv));
        forkJoin.workers.forEach(e -> this.acceptNode(e, folkJoinEnv));
//        if (!this.isJoinResultType(forkJoin.joinResultVar)) {
//            this.dlog.error(forkJoin.joinResultVar.pos, DiagnosticCode.INVALID_WORKER_JOIN_RESULT_TYPE);
//        }
        /* create code black and environment for join result section, i.e. (map results) */
        BLangBlockStmt joinResultsBlock = this.generateCodeBlock(this.createVarDef(forkJoin.joinResultVar));
        SymbolEnv joinResultsEnv = SymbolEnv.createBlockEnv(joinResultsBlock, this.symbolEnv);
        this.acceptNode(joinResultsBlock, joinResultsEnv);
        /* create an environment for the join body, making the enclosing environment the earlier
         * join result's environment */
        SymbolEnv joinBodyEnv = SymbolEnv.createBlockEnv(forkJoin.joinedBody, joinResultsEnv);
        this.acceptNode(forkJoin.joinedBody, joinBodyEnv);

        if (forkJoin.timeoutExpression != null) {
            /* create code black and environment for timeout section */
            BLangBlockStmt timeoutVarBlock = this.generateCodeBlock(this.createVarDef(forkJoin.timeoutVariable));
            SymbolEnv timeoutVarEnv = SymbolEnv.createBlockEnv(timeoutVarBlock, this.symbolEnv);
            this.acceptNode(timeoutVarBlock, timeoutVarEnv);
            /* create an environment for the timeout body, making the enclosing environment the earlier
             * timeout var's environment */
            SymbolEnv timeoutBodyEnv = SymbolEnv.createBlockEnv(forkJoin.timeoutBody, timeoutVarEnv);
            this.acceptNode(forkJoin.timeoutBody, timeoutBodyEnv);
        }
    }

    @Override
    public void visit(BLangWorker workerNode) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.symbolEnv);
        this.blockOwnerStack.push(workerNode);
        this.acceptNode(workerNode.body, workerEnv);
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(workerSendNode.getPosition(), workerSendNode,
                        this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(workerReceiveNode.getPosition(), workerReceiveNode,
                        this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangReturn returnNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(returnNode.getPosition(), returnNode, this, this.documentServiceContext);
    }

    public void visit(BLangNext nextNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(nextNode.getPosition(), nextNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        // No Implementation
    }

    @Override
    public void visit(BLangPackageDeclaration pkgDclNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangEnum enumNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver).isCursorBeforeNode(enumNode.getPosition(),
                enumNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangEnum.BLangEnumerator enumeratorNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangAnnotAttribute annotationAttribute) {
        // No Implementation
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {
        // No Implementation
    }

    @Override
    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {
        // No Implementation
    }

    @Override
    public void visit(BLangBind bindNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver).isCursorBeforeNode(bindNode.getPosition(),
                bindNode, this, this.documentServiceContext);
        // TODO: need to implement the bind context related suggestions. Implementation on hold - grammar inconsistency
    }

    @Override
    public void visit(BLangBreak breakNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver).isCursorBeforeNode(breakNode.getPosition(),
                breakNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangReturn.BLangWorkerReturn returnNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangThrow throwNode) {
        CursorScopeResolver.getResolverByClass(cursorPositionResolver).isCursorBeforeNode(throwNode.getPosition(),
                throwNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        // No Implementation
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        // No Implementation
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangConnectorInit connectorInitExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangTypeofExpr accessExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangTypeCastExpr castExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        // No Implementation
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        // No Implementation
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        // No Implementation
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        // No Implementation
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        // No Implementation
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        // No Implementation
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        // No Implementation
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        // No Implementation
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        // No Implementation
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangValueType valueType) {
        // No Implementation
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        // No Implementation
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        // No Implementation
    }

    @Override
    public void visit(BLangEndpointTypeNode endpointType) {
        // No Implementation
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        // No Implementation
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        // No Implementation
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        // No Implementation
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        // No Implementation
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        // No Implementation
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        // No Implementation
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {
        // No Implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        // No Implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        // No Implementation
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        // No Implementation
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangInvocation.BLangTransformerInvocation iExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangArrayLiteral.BLangJSONArrayLiteral jsonArrayLiteral) {
        // No Implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangEnumeratorAccessExpr enumeratorAccessExpr) {
        // No Implementation
    }

    @Override
    public void visit(BLangForeach foreach) {
        if (!CursorScopeResolver.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(foreach.getPosition(), foreach, this, this.documentServiceContext)) {
            this.blockOwnerStack.push(foreach);
            this.acceptNode(foreach.body, symbolEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        // No Implementation
    }

    /**
     * Resolve all visible symbols.
     * @param symbolEnv symbol environment
     * @return all visible symbols for current scope
     */
    public Map<Name, Scope.ScopeEntry> resolveAllVisibleSymbols(SymbolEnv symbolEnv) {
        return symbolResolver.getAllVisibleInScopeSymbols(symbolEnv);
    }

    /**
     * Populate the symbols.
     * @param symbolEntries symbol entries
     */
    public void populateSymbols(Map<Name, Scope.ScopeEntry> symbolEntries, SymbolEnv symbolEnv) {
        if (symbolEnv != null) {
            documentServiceContext.put(CompletionKeys.SYMBOL_ENV_NODE_KEY, symbolEnv.node);
        } else {
            documentServiceContext.put(CompletionKeys.SYMBOL_ENV_NODE_KEY, this.symbolEnv.node);
        }

        List<SymbolInfo> visibleSymbols = new ArrayList<>();
        symbolEntries.forEach((k, v) -> {
            SymbolInfo symbolInfo = new SymbolInfo(k.getValue(), v);
            visibleSymbols.add(symbolInfo);
        });

        documentServiceContext.put(CompletionKeys.VISIBLE_SYMBOLS_KEY, visibleSymbols);
    }

    public Stack<Node> getBlockOwnerStack() {
        return blockOwnerStack;
    }

    public Stack<BLangBlockStmt> getBlockStmtStack() {
        return blockStmtStack;
    }

    public SymbolEnv getSymbolEnv() {
        return symbolEnv;
    }

    public void setTerminateVisitor(boolean terminateVisitor) {
        this.terminateVisitor = terminateVisitor;
    }


    // Private Methods
    private void acceptNode(BLangNode node, SymbolEnv env) {

        if (this.terminateVisitor) {
            return;
        }

        SymbolEnv prevEnv = this.symbolEnv;
        this.symbolEnv = env;
        node.accept(this);
        this.symbolEnv = prevEnv;
    }

    private boolean isCursorWithinBlock(DiagnosticPos nodePosition, SymbolEnv symbolEnv) {
        int line = documentServiceContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int nodeSLine = nodePosition.sLine;
        int nodeELine = nodePosition.eLine;

        if ((nodeSLine <= line && nodeELine >= line)) {
            Map<Name, Scope.ScopeEntry> visibleSymbolEntries = this.resolveAllVisibleSymbols(symbolEnv);
            this.populateSymbols(visibleSymbolEntries, symbolEnv);
            this.terminateVisitor = true;
            return true;
        }

        return false;
    }
}

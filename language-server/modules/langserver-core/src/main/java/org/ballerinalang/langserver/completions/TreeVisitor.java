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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.util.ScopeResolverConstants;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.BlockStatementScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ConnectorScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.MatchStatementScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ObjectTypeScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.PackageNodeScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.RecordScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ResourceParamScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ServiceScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.TopLevelNodeScopeResolver;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.types.BLangEndpointTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class TreeVisitor extends LSNodeVisitor {
    private static final String NODE_TYPE_FUNCTION = "function";
    private static final String NODE_TYPE_ACTION = "action";
    private static final String NODE_TYPE_RESOURCE = "resource";
    private static final String NODE_TYPE_CONNECTOR = "connector";
    
    private boolean terminateVisitor = false;
    private int loopCount = 0;
    private int transactionCount = 0;
    private SymbolEnv symbolEnv;
    private SymbolResolver symbolResolver;
    private SymbolEnter symbolEnter;
    private SymbolTable symTable;
    private Stack<Node> blockOwnerStack;
    private Stack<BLangBlockStmt> blockStmtStack;
    private Stack<Boolean> isCurrentNodeTransactionStack;
    private Class cursorPositionResolver;
    private LSServiceOperationContext documentServiceContext;
    private BLangNode previousNode = null;
    private BLangNode nextNode = null;

    public TreeVisitor(LSServiceOperationContext documentServiceContext) {
        this.documentServiceContext = documentServiceContext;
        init(this.documentServiceContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));
    }

    private void init(CompilerContext compilerContext) {
        blockOwnerStack = new Stack<>();
        blockStmtStack = new Stack<>();
        isCurrentNodeTransactionStack = new Stack<>();
        symbolEnter = SymbolEnter.getInstance(compilerContext);
        symTable = SymbolTable.getInstance(compilerContext);
        symbolResolver = SymbolResolver.getInstance(compilerContext);
        documentServiceContext.put(DocumentServiceKeys.SYMBOL_TABLE_KEY, symTable);
    }

    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        this.symbolEnv = pkgEnv;

        // Then visit each top-level element sorted using the compilation unit
        String fileName = documentServiceContext.get(DocumentServiceKeys.FILE_NAME_KEY);
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes.stream().filter(node ->
                    node.getPosition().getSource().getCompilationUnitName().equals(fileName)
            ).collect(Collectors.toList());

        if (topLevelNodes.isEmpty()) {
            this.setTerminateVisitor(true);
            acceptNode(null, null);
        } else {
            cursorPositionResolver = PackageNodeScopeResolver.class;
            topLevelNodes.forEach(topLevelNode -> {
                cursorPositionResolver = TopLevelNodeScopeResolver.class;
                this.blockOwnerStack.push(pkgNode);
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
        ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(xmlnsNode.getPosition(), xmlnsNode, this, this.documentServiceContext);
    }

    public void visit(BLangFunction funcNode) {
        // Check for native functions
        BSymbol funcSymbol = funcNode.symbol;
        if (Symbols.isNative(funcSymbol)) {
            return;
        }

        String functionName = funcNode.getName().getValue();
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope, symbolEnv);
        
        if (isWithinParameterContext(functionName, NODE_TYPE_FUNCTION)) {
            this.populateSymbols(this.resolveAllVisibleSymbols(funcEnv), funcEnv);
            setTerminateVisitor(true);
        } else if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(funcNode.getPosition(), funcNode, this, this.documentServiceContext)) {
            
            // Visit the endpoints
            funcNode.endpoints.forEach(bLangEndpoint -> this.acceptNode(bLangEndpoint, funcEnv));
            
            this.blockOwnerStack.push(funcNode);
            // Cursor position is calculated against the Block statement scope resolver
            cursorPositionResolver = BlockStatementScopeResolver.class;
            this.acceptNode(funcNode.body, funcEnv);
            this.blockOwnerStack.pop();

            // Process workers
            if (terminateVisitor && !funcNode.workers.isEmpty()) {
                this.setTerminateVisitor(false);
            }
            
            funcNode.workers.forEach(e -> this.symbolEnter.defineNode(e, funcEnv));
            funcNode.workers.forEach(e -> this.acceptNode(e, funcEnv));
        }
    }

    @Override
    public void visit(BLangRecord bLangRecord) {
        if (!bLangRecord.getName().getValue().contains("$")
                && !ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(bLangRecord.getPosition(), bLangRecord, this, this.documentServiceContext)) {
            BSymbol structSymbol = bLangRecord.symbol;
            SymbolEnv recordEnv = SymbolEnv.createPkgLevelSymbolEnv(bLangRecord, structSymbol.scope, symbolEnv);
            if (bLangRecord.fields.isEmpty() && isCursorWithinBlock(bLangRecord.getPosition(), recordEnv)) {
                symbolEnv = recordEnv;
                Map<Name, Scope.ScopeEntry> visibleSymbolEntries = this.resolveAllVisibleSymbols(symbolEnv);
                this.populateSymbols(visibleSymbolEntries, null);
                this.setTerminateVisitor(true);
            } else if (!bLangRecord.fields.isEmpty()) {
                // Since the record definition do not have a block statement within, we push null
                cursorPositionResolver = RecordScopeResolver.class;
                this.blockOwnerStack.push(bLangRecord);
                bLangRecord.fields.forEach(field -> acceptNode(field, recordEnv));
                cursorPositionResolver = TopLevelNodeScopeResolver.class;
                this.blockOwnerStack.pop();
            }
        }
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
    }

    @Override
    public void visit(BLangVariable varNode) {
        boolean isCursorBeforeNode = ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(varNode.getPosition(), varNode, this, this.documentServiceContext);
        // This is an endpoint definition
        if (!isCursorBeforeNode && varNode.typeNode instanceof BLangEndpointTypeNode
                && this.isCursorWithinEndpointDef(varNode.getPosition())) {
            SymbolEnv endpointVarInitEnv = SymbolEnv.createVarInitEnv(varNode, symbolEnv, varNode.symbol);
            Map<Name, Scope.ScopeEntry> visibleSymbols = this.resolveAllVisibleSymbols(symbolEnv);
            this.populateSymbols(visibleSymbols, endpointVarInitEnv);
            this.setTerminateVisitor(true);
        }
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
        // Reset the previous node to null
        this.setPreviousNode(null);
        if (blockNode.stmts.isEmpty()) {
            this.isCursorWithinBlock((DiagnosticPos) (this.blockOwnerStack.peek()).getPosition(), blockEnv);
        } else {
            blockNode.stmts.forEach(stmt -> this.acceptNode(stmt, blockEnv));
        }
        this.blockStmtStack.pop();
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(varDefNode.getPosition(), varDefNode, this, this.documentServiceContext)) {
            this.acceptNode(varDefNode.var, symbolEnv);
        }
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(assignNode.getPosition(), assignNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(exprStmtNode.getPosition(), exprStmtNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangIf ifNode) {
        if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
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
        if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(whileNode.getPosition(), whileNode, this, this.documentServiceContext)) {
            this.blockOwnerStack.push(whileNode);
            loopCount++;
            this.acceptNode(whileNode.body, symbolEnv);
            loopCount--;
            this.blockOwnerStack.pop();
        }
    }

    public void visit(BLangTransformer transformerNode) {
        if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(transformerNode.getPosition(), transformerNode, this,
                        this.documentServiceContext)) {
            SymbolEnv transformerEnv = SymbolEnv
                    .createTransformerEnv(transformerNode, transformerNode.symbol.scope, symbolEnv);
            this.blockOwnerStack.push(transformerNode);
            // Cursor position is calculated against the Block statement scope resolver
            cursorPositionResolver = BlockStatementScopeResolver.class;
            this.acceptNode(transformerNode.body, transformerEnv);
            this.blockOwnerStack.pop();
        }
    }

    public void visit(BLangConnector connectorNode) {
        String connectorName = connectorNode.getName().getValue();
        BSymbol connectorSymbol = connectorNode.symbol;
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, symbolEnv);
        
        if (isWithinParameterContext(connectorName, NODE_TYPE_CONNECTOR)) {
            this.populateSymbols(this.resolveAllVisibleSymbols(connectorEnv), connectorEnv);
            setTerminateVisitor(true);
        } else if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(connectorNode.getPosition(), connectorNode, this, this.documentServiceContext)) {

            // Reset the previous node
            this.setPreviousNode(null);
            // TODO: Handle Annotation attachments
            if (!(connectorNode.actions.isEmpty() && connectorNode.varDefs.isEmpty()
                    && connectorNode.endpoints.isEmpty())) {
                
                // Visit the endpoints
                connectorNode.endpoints.forEach(bLangEndpoint -> this.acceptNode(bLangEndpoint, connectorEnv));
                
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
            } else {
                this.isCursorWithinBlock(connectorNode.getPosition(), connectorEnv);
            }
        }
    }

    public void visit(BLangAction actionNode) {
        String actionName = actionNode.getName().getValue();
        BSymbol actionSymbol = actionNode.symbol;
        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, symbolEnv);
        
        if (this.isWithinParameterContext(actionName, NODE_TYPE_ACTION)) {
            this.populateSymbols(this.resolveAllVisibleSymbols(actionEnv), actionEnv);
            setTerminateVisitor(true);
        } else if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(actionNode.getPosition(), actionNode, this, this.documentServiceContext)) {

            // TODO: Handle Annotation attachments
            // Visit the endpoints
            actionNode.endpoints.forEach(bLangEndpoint -> this.acceptNode(bLangEndpoint, actionEnv));
            
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
        BSymbol serviceSymbol = serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, symbolEnv);
        
        serviceNode.annAttachments.forEach(annotationAttachment -> this.acceptNode(annotationAttachment, serviceEnv));
        
        if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(serviceNode.getPosition(), serviceNode, this, this.documentServiceContext)) {
            
            // Reset the previous node
            this.setPreviousNode(null);
            if (!(serviceNode.resources.isEmpty() && serviceNode.vars.isEmpty() && serviceNode.endpoints.isEmpty())) {
                // Visit the endpoints
                serviceNode.endpoints.forEach(bLangEndpoint -> this.acceptNode(bLangEndpoint, serviceEnv));
                
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
            } else {
                this.isCursorWithinBlock(serviceNode.getPosition(), serviceEnv);
            }
        }
    }

    public void visit(BLangResource resourceNode) {
        String resourceName = resourceNode.getName().getValue();
        BSymbol resourceSymbol = resourceNode.symbol;
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, symbolEnv);

        resourceNode.annAttachments.forEach(annotationAttachment -> this.acceptNode(annotationAttachment, resourceEnv));
        
        if (isWithinParameterContext(resourceName, NODE_TYPE_RESOURCE)) {
            this.populateSymbols(this.resolveAllVisibleSymbols(resourceEnv), resourceEnv);
            setTerminateVisitor(true);
        } else if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(resourceNode.getPosition(), resourceNode, this, this.documentServiceContext)) {

            // TODO:Handle Annotation attachments
            // Visit the endpoints
            resourceNode.endpoints.forEach(bLangEndpoint -> this.acceptNode(bLangEndpoint, resourceEnv));
            
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
        if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
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
        if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
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
        this.isCurrentNodeTransactionStack.push(true);
        this.transactionCount++;
        this.acceptNode(transactionNode.transactionBody, symbolEnv);
        this.blockOwnerStack.pop();
        this.isCurrentNodeTransactionStack.pop();
        this.transactionCount--;

        if (transactionNode.onRetryBody != null) {
            this.blockOwnerStack.push(transactionNode);
            this.acceptNode(transactionNode.onRetryBody, symbolEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
        ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(abortNode.getPosition(), abortNode, this, this.documentServiceContext);
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
        ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(workerSendNode.getPosition(), workerSendNode,
                        this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(workerReceiveNode.getPosition(), workerReceiveNode,
                        this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangReturn returnNode) {
        ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(returnNode.getPosition(), returnNode, this, this.documentServiceContext);
    }

    public void visit(BLangNext nextNode) {
        ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(nextNode.getPosition(), nextNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangEnum enumNode) {
        ScopeResolverConstants.getResolverByClass(cursorPositionResolver).isCursorBeforeNode(enumNode.getPosition(),
                enumNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangBind bindNode) {
        ScopeResolverConstants.getResolverByClass(cursorPositionResolver).isCursorBeforeNode(bindNode.getPosition(),
                bindNode, this, this.documentServiceContext);
        // TODO: need to implement the bind context related suggestions. Implementation on hold - grammar inconsistency
    }

    @Override
    public void visit(BLangBreak breakNode) {
        ScopeResolverConstants.getResolverByClass(cursorPositionResolver).isCursorBeforeNode(breakNode.getPosition(),
                breakNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangThrow throwNode) {
        ScopeResolverConstants.getResolverByClass(cursorPositionResolver).isCursorBeforeNode(throwNode.getPosition(),
                throwNode, this, this.documentServiceContext);
    }

    @Override
    public void visit(BLangLock lockNode) {
        if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(lockNode.getPosition(), lockNode, this, this.documentServiceContext)) {
            this.blockOwnerStack.push(lockNode);
            this.acceptNode(lockNode.body, symbolEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangForeach foreach) {
        if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(foreach.getPosition(), foreach, this, this.documentServiceContext)) {
            this.blockOwnerStack.push(foreach);
            loopCount++;
            this.acceptNode(foreach.body, symbolEnv);
            loopCount--;
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(endpointNode.getPosition(), endpointNode, this, this.documentServiceContext)) {
            SymbolEnv endpointEnv = SymbolEnv
                    .createPkgLevelSymbolEnv(endpointNode, symbolEnv.scope, symbolEnv);
            this.isWithinEndpointContext(endpointNode.getPosition(), endpointEnv, endpointNode.getName().getValue());
        }
    }

    @Override
    public void visit(BLangObject objectNode) {
        BSymbol objectSymbol = objectNode.symbol;
        SymbolEnv objectEnv = SymbolEnv.createPkgLevelSymbolEnv(objectNode, objectSymbol.scope, symbolEnv);
        blockOwnerStack.push(objectNode);
        objectNode.fields.forEach(field -> {
            this.cursorPositionResolver = ObjectTypeScopeResolver.class;
            acceptNode(field, objectEnv);
        });
        // TODO: visit annotation and doc attachments
        objectNode.functions.forEach(f -> {
            this.cursorPositionResolver = ObjectTypeScopeResolver.class;
            acceptNode(f, objectEnv);
        });
        blockOwnerStack.pop();
        this.cursorPositionResolver = TopLevelNodeScopeResolver.class;
    }

    @Override
    public void visit(BLangMatch matchNode) {
        if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(matchNode.getPosition(), matchNode, this, this.documentServiceContext)) {
            this.blockOwnerStack.push(matchNode);
            matchNode.getPatternClauses().forEach(patternClause -> {
                cursorPositionResolver = MatchStatementScopeResolver.class;
                acceptNode(patternClause, symbolEnv);
            });
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangMatch.BLangMatchStmtPatternClause patternClause) {
        if (!ScopeResolverConstants.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(patternClause.getPosition(), patternClause, this, this.documentServiceContext)) {
            blockOwnerStack.push(patternClause);
            // If the variable is not equal to '_', then define the variable in the block scope
            if (!patternClause.variable.name.value.endsWith(Names.IGNORE.value)) {
                SymbolEnv blockEnv = SymbolEnv.createBlockEnv(patternClause.body, symbolEnv);
                cursorPositionResolver = BlockStatementScopeResolver.class;
                acceptNode(patternClause.body, blockEnv);
                blockOwnerStack.pop();
                return;
            }
            // TODO: Check with the semantic analyzer implementation as well.
            acceptNode(patternClause.body, symbolEnv);
            blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        SymbolEnv annotationAttachmentEnv = new SymbolEnv(annAttachmentNode, symbolEnv.scope);
        this.isCursorWithinBlock(annAttachmentNode.getPosition(), annotationAttachmentEnv);
    }

    public void setPreviousNode(BLangNode previousNode) {
        this.previousNode = previousNode;
    }

    public void setNextNode(BLangNode nextNode) {
        this.nextNode = nextNode;
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
        if (terminateVisitor) {
            boolean currentNodeIsTransaction = !this.isCurrentNodeTransactionStack.isEmpty();
            documentServiceContext.put(CompletionKeys.CURRENT_NODE_TRANSACTION_KEY, currentNodeIsTransaction);
            documentServiceContext.put(CompletionKeys.LOOP_COUNT_KEY, this.loopCount);
            documentServiceContext.put(CompletionKeys.TRANSACTION_COUNT_KEY, this.transactionCount);
            documentServiceContext.put(CompletionKeys.PREVIOUS_NODE_KEY, this.previousNode);
            if (!blockOwnerStack.isEmpty()) {
                documentServiceContext.put(CompletionKeys.BLOCK_OWNER_KEY, blockOwnerStack.peek());
            }
        }
        this.terminateVisitor = terminateVisitor;
    }


    // Private Methods
    private void acceptNode(BLangNode node, SymbolEnv env) {
        if (this.terminateVisitor) {
            documentServiceContext.put(CompletionKeys.NEXT_NODE_KEY, nextNode);
            return;
        }

        SymbolEnv prevEnv = this.symbolEnv;
        this.symbolEnv = env;
        node.accept(this);
        this.symbolEnv = prevEnv;
        this.setPreviousNode(node);
    }

    private boolean isCursorWithinBlock(DiagnosticPos nodePosition, SymbolEnv symbolEnv) {
        int line = documentServiceContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        DiagnosticPos zeroBasedPosition = CommonUtil.toZeroBasedPosition(nodePosition);
        int nodeSLine = zeroBasedPosition.sLine;
        int nodeELine = zeroBasedPosition.eLine;

        if ((nodeSLine <= line && nodeELine >= line)) {
            Map<Name, Scope.ScopeEntry> visibleSymbolEntries = new HashMap<>();
            if (symbolEnv.scope != null) {
                visibleSymbolEntries.putAll(this.resolveAllVisibleSymbols(symbolEnv));
            }
            this.populateSymbols(visibleSymbolEntries, symbolEnv);
            this.setTerminateVisitor(true);
            return true;
        }

        return false;
    }
    
    private boolean isWithinEndpointContext(DiagnosticPos diagnosticPos, SymbolEnv symbolEnv, String epName) {
        if (documentServiceContext.get(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY)
                .get(CompletionKeys.META_CONTEXT_IS_ENDPOINT_KEY) == null) {
            // No syntax errors has occurred
            return this.isCursorWithinBlock(diagnosticPos, symbolEnv);
        } else if (documentServiceContext.get(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY)
                .get(CompletionKeys.META_CONTEXT_IS_ENDPOINT_KEY)) {
            // Syntax errors has occurred
            boolean isWithinEndpoint = documentServiceContext.get(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY)
                    .get(CompletionKeys.META_CONTEXT_IS_ENDPOINT_KEY);
            String name = documentServiceContext.get(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY)
                    .get(CompletionKeys.META_CONTEXT_ENDPOINT_NAME_KEY);
            
            if (isWithinEndpoint && epName.equals(name)) {
                // Evaluating the particular Endpoint
                this.populateSymbols(new HashMap<>(), symbolEnv);
                this.setTerminateVisitor(true);

                return true;
            }
        }
        
        return false;
    }

    /**
     * Check whether the cursor is within the endpoint definition node.
     * @param nodePosition  Diagnostic position of the current node
     * @return              {@link Boolean} whether the cursor is within the node or not
     */
    private boolean isCursorWithinEndpointDef(DiagnosticPos nodePosition) {
        int line = documentServiceContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int column = documentServiceContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();
        int nodeSLine = nodePosition.sLine;
        int nodeELine = nodePosition.eLine;
        int nodeSCol = nodePosition.sCol;
        int nodeECol = nodePosition.eCol;
        
        return (line > nodeSLine && line < nodeELine)
                || (line > nodeSLine && line == nodeELine && column < nodeECol)
                || (line == nodeSLine && column > nodeSCol && line < nodeELine)
                || (line == nodeSLine && line == nodeELine && column > nodeSCol && column < nodeECol);
    }

    /**
     * Check whether the cursor resides within the given node type's parameter context.
     * Node name is used to identify the correct node
     * @param nodeName              Name of the node
     * @param nodeType              Node type (Function, Resource, Action or Connector)
     * @return {@link Boolean}      Whether the cursor is within the parameter context
     */
    private boolean isWithinParameterContext(String nodeName, String nodeType) {
        ParserRuleContext parserRuleContext = documentServiceContext.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY);
        TokenStream tokenStream = documentServiceContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        String terminalToken = "";
        
        // If the parser rule context is not parameter context or parameter list context, we skipp the calculation
        if (!(parserRuleContext instanceof BallerinaParser.ParameterContext
                || parserRuleContext instanceof BallerinaParser.ParameterListContext)) {
            return false;
        }
        
        int startTokenIndex = parserRuleContext.getStart().getTokenIndex();
        ArrayList<String> terminalKeywords = new ArrayList<>(
                Arrays.asList(NODE_TYPE_ACTION, NODE_TYPE_CONNECTOR, NODE_TYPE_FUNCTION, NODE_TYPE_RESOURCE)
        );
        ArrayList<Token> filteredTokens = new ArrayList<>();
        Token openBracket = null;
        boolean isWithinParams = false;
        
        // Find the index of the closing bracket
        while (true) {
            if (startTokenIndex > tokenStream.size()) {
                // In the ideal case, should not reach this point
                startTokenIndex = -1;
                break;
            }
            Token token = tokenStream.get(startTokenIndex);
            String tokenString = token.getText();
            if (tokenString.equals(")")) {
                break;
            }
            startTokenIndex++;
        }
        
        // Backtrack the token stream to find a terminal token
        while (true) {
            if (startTokenIndex < 0) {
                break;
            }
            Token token = tokenStream.get(startTokenIndex);
            String tokenString = token.getText();
            if (terminalKeywords.contains(tokenString)) {
                terminalToken = tokenString;
                break;
            }
            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                filteredTokens.add(token);
            }
            startTokenIndex--;
        }

        Collections.reverse(filteredTokens);
        
        /*
        This particular logic identifies a matching pair of closing and opening bracket and then check whether the
        cursor is within those bracket pair
         */
        if (nodeName.equals(filteredTokens.get(0).getText()) && terminalToken.equals(nodeType)) {
            String tokenText;
            for (Token token : filteredTokens) {
                tokenText = token.getText();
                if (tokenText.equals("(")) {
                    openBracket = token;
                } else if (tokenText.equals(")") && openBracket != null) {
                    Position cursorPos = documentServiceContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
                    int openBLine = openBracket.getLine() - 1;
                    int openBCol = openBracket.getCharPositionInLine();
                    int closeBLine = token.getLine() - 1;
                    int closeBCol = token.getCharPositionInLine();
                    int cursorLine = cursorPos.getLine();
                    int cursorCol = cursorPos.getCharacter();
                    
                    isWithinParams =  (cursorLine > openBLine && cursorLine < closeBLine)
                            || (cursorLine == openBLine && cursorCol > openBCol && cursorLine < closeBLine)
                            || (cursorLine > openBLine && cursorCol < closeBCol && cursorLine == closeBLine)
                            || (cursorLine == openBLine && cursorLine == closeBLine && cursorCol >= openBCol
                            && cursorCol <= closeBCol);
                    if (isWithinParams) {
                        break;
                    } else {
                        openBracket = null;
                    }
                }
            }
        }
        
        return isWithinParams;
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
}

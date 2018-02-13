/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.signature;

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.TextDocumentServiceUtil;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
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
 * Tree visitor to traverse through the ballerina node tree and find the scope of a given cursor position.
 */
public class SignatureTreeVisitor extends BLangNodeVisitor {
    private SymbolEnv symbolEnv;
    private SymbolResolver symbolResolver;
    private boolean terminateVisitor = false;
    private SymbolEnter symbolEnter;
    private SymbolTable symTable;
    private TextDocumentServiceContext documentServiceContext;
    private Stack<Node> blockOwnerStack;

    /**
     * Public constructor.
     * @param textDocumentServiceContext    Document service context for the signature operation
     */
    public SignatureTreeVisitor(TextDocumentServiceContext textDocumentServiceContext) {
        blockOwnerStack = new Stack<>();
        this.documentServiceContext = textDocumentServiceContext;
        init(documentServiceContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));
    }

    private void init(CompilerContext compilerContext) {
        symbolEnter = SymbolEnter.getInstance(compilerContext);
        symTable = SymbolTable.getInstance(compilerContext);
        symbolResolver = SymbolResolver.getInstance(compilerContext);
        documentServiceContext.put(DocumentServiceKeys.SYMBOL_TABLE_KEY, symTable);
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgNode.symbol);
        // Then visit each top-level element sorted using the compilation unit
        String fileName = documentServiceContext.get(DocumentServiceKeys.FILE_NAME_KEY);
        BLangCompilationUnit compilationUnit = pkgNode.getCompilationUnits().stream()
                .filter(bLangCompilationUnit -> bLangCompilationUnit.getName().equals(fileName))
                .findFirst().orElse(null);
        List<TopLevelNode> topLevelNodes = compilationUnit.getTopLevelNodes();

        if (!topLevelNodes.isEmpty()) {
            topLevelNodes.forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode, pkgEnv));
        }
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        super.visit(compUnit);
    }

    @Override
    public void visit(BLangPackageDeclaration pkgDclNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangFunction funcNode) {
        BSymbol funcSymbol = funcNode.symbol;
        if (Symbols.isNative(funcSymbol)) {
            return;
        }
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope, symbolEnv);
        blockOwnerStack.push(funcNode);
        this.acceptNode(funcNode.body, funcEnv);
        blockOwnerStack.pop();
        // Process workers
        if (terminateVisitor && !funcNode.workers.isEmpty()) {
            terminateVisitor = false;
        }
        funcNode.workers.forEach(e -> this.symbolEnter.defineNode(e, funcEnv));
        funcNode.workers.forEach(e -> this.acceptNode(e, funcEnv));
    }

    @Override
    public void visit(BLangService serviceNode) {
        BSymbol serviceSymbol = serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, symbolEnv);
        serviceNode.resources.forEach(r -> this.acceptNode(r, serviceEnv));
    }

    @Override
    public void visit(BLangResource resourceNode) {
        BSymbol resourceSymbol = resourceNode.symbol;
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, symbolEnv);
        resourceNode.workers.forEach(w -> this.acceptNode(w, resourceEnv));
        this.blockOwnerStack.push(resourceNode);
        acceptNode(resourceNode.body, resourceEnv);
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangConnector connectorNode) {
        BSymbol connectorSymbol = connectorNode.symbol;
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, symbolEnv);
        connectorNode.actions.forEach(action -> this.acceptNode(action, connectorEnv));
    }

    @Override
    public void visit(BLangAction actionNode) {
        BSymbol actionSymbol = actionNode.symbol;
        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, symbolEnv);
        actionNode.workers.forEach(w -> this.acceptNode(w, actionEnv));
        this.blockOwnerStack.push(actionNode);
        acceptNode(actionNode.body, actionEnv);
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangStruct structNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangEnum enumNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangEnum.BLangEnumerator enumeratorNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangVariable varNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangWorker workerNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
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
    public void visit(BLangTransformer transformerNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, symbolEnv);
        blockNode.stmts.forEach(stmt -> this.acceptNode(stmt, blockEnv));
        if (!terminateVisitor && this.isCursorWithinBlock()) {
            Map<Name, Scope.ScopeEntry> visibleSymbolEntries = symbolResolver.getAllVisibleInScopeSymbols(blockEnv);
            this.populateSymbols(visibleSymbolEntries);
        }
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        this.acceptNode(varDefNode.var, symbolEnv);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangBind bindNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangAbort abortNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangNext nextNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangBreak breakNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangReturn returnNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangReturn.BLangWorkerReturn returnNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangThrow throwNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangIf ifNode) {
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

    @Override
    public void visit(BLangForeach foreach) {
        this.blockOwnerStack.push(foreach);
        this.acceptNode(foreach.body, symbolEnv);
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangWhile whileNode) {
        this.blockOwnerStack.push(whileNode);
        this.acceptNode(whileNode.body, symbolEnv);
        this.blockOwnerStack.pop();
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
    public void visit(BLangTryCatchFinally tryNode) {
        this.blockOwnerStack.push(tryNode);
        this.acceptNode(tryNode.tryBody, symbolEnv);
        this.blockOwnerStack.pop();

        tryNode.catchBlocks.forEach(c -> {
            this.blockOwnerStack.push(c);
            this.acceptNode(c, symbolEnv);
            this.blockOwnerStack.pop();
        });
        if (tryNode.finallyBody != null) {
            this.blockOwnerStack.push(tryNode);
            this.acceptNode(tryNode.finallyBody, symbolEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangCatch catchNode) {
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(catchNode.body, symbolEnv);
        this.acceptNode(catchNode.param, catchBlockEnv);

        this.blockOwnerStack.push(catchNode);
        this.acceptNode(catchNode.body, catchBlockEnv);
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        // No Implementation
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
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
    public void visit(BLangSimpleVarRef varRefExpr) {
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
    public void visit(BLangWorkerSend workerSendNode) {
        // No Implementation
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
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
    public void visit(BLangIntRangeExpression intRangeExpression) {
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

    private boolean isCursorWithinBlock() {
        Position cursorPosition = this.documentServiceContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        Node blockOwner = blockOwnerStack.peek();
        DiagnosticPos nodePosition =
                TextDocumentServiceUtil.toZeroBasedPosition((DiagnosticPos) blockOwner.getPosition());
        int cursorLine = cursorPosition.getLine();
        int cursorColumn = cursorPosition.getCharacter();
        int nodeStrtLine = nodePosition.getStartLine();
        int nodeEndLine = nodePosition.getEndLine();
        int nodeStrtColumn = nodePosition.getStartColumn();
        int nodeEndColumn = nodePosition.getEndColumn();
        
        /*
          node Start ->{ <cursor_position> }<- node End.
          If the cursor is within the scope of the node, then the cursor should be located after the node start
          and before the node end
         */
        int isAfterBlockStart = nodeStrtLine == cursorLine ? cursorColumn - nodeStrtColumn : cursorLine - nodeStrtLine;
        int isBeforeBlockEnd = nodeEndLine == cursorLine ? nodeEndColumn - cursorColumn : nodeEndLine - cursorLine;

        return isAfterBlockStart > 0 && isBeforeBlockEnd > 0;
    }

    /**
     * Populate the symbols.
     * @param symbolEntries symbol entries
     */
    private void populateSymbols(Map<Name, Scope.ScopeEntry> symbolEntries) {
        // TODO: Populate only the visible functions
        this.terminateVisitor = true;
        String identifierAgainst = documentServiceContext.get(SignatureKeys.IDENTIFIER_AGAINST);
        List<SymbolInfo> visibleSymbols = new ArrayList<>();

        /*
          During the first iteration we filter out the functions and if there is, the variable reference against which
          the function is called.
         */
        symbolEntries.forEach((k, v) -> {
            if (v.symbol instanceof BInvokableSymbol && !(v.symbol instanceof BOperatorSymbol)
                    && !v.symbol.getName().getValue().contains("<init>")) {
                SymbolInfo symbolInfo = new SymbolInfo(k.getValue(), v);
                visibleSymbols.add(symbolInfo);
            } else if (v.symbol instanceof BVarSymbol && k.getValue().equals(identifierAgainst)) {
                documentServiceContext.put(SignatureKeys.IDENTIFIER_TYPE, v.symbol.type.toString());
            } else if (v.symbol instanceof BPackageSymbol && k.getValue().equals(identifierAgainst)) {
                documentServiceContext.put(SignatureKeys.IDENTIFIER_PKGID, v.symbol.pkgID.toString());
                documentServiceContext.put(SignatureKeys.IDENTIFIER_TYPE, v.symbol.type.toString());
                visibleSymbols.addAll(this.getInvokableSymbolsInPackage((BPackageSymbol) v.symbol));
            }
        });
        
        /*
          In this iteration we filter out the functions either having a receiver or otherwise.
          If the identifier against value is a valid value, then check whether the receiver type equals to identifier
          type. If there is no identifier, filter out functions without the receiver
         */
        List<SymbolInfo> filteredSymbols = new ArrayList<>();
        visibleSymbols.forEach(symbolInfo -> {
            BVarSymbol receiver = ((BInvokableSymbol) symbolInfo.getScopeEntry().symbol).receiverSymbol;
            String[] nameTokens = symbolInfo.getSymbolName().split("\\.");
            String funcNameFromSymbol = nameTokens[nameTokens.length - 1];
            String functionName = documentServiceContext.get(SignatureKeys.CALLABLE_ITEM_NAME);
            String identifierPkgName = documentServiceContext.get(SignatureKeys.IDENTIFIER_PKGID);
            boolean onIdentifierTypePkg = "package".equals(documentServiceContext.get(SignatureKeys.IDENTIFIER_TYPE))
                    && symbolInfo.getScopeEntry().symbol.pkgID.toString().equals(identifierPkgName);
            boolean onReceiverTypeMatchIdentifier = receiver != null &&
                    receiver.type.toString().equals(documentServiceContext.get(SignatureKeys.IDENTIFIER_TYPE));
            boolean onIdentifierAgainstNull = (receiver == null
                    && (identifierAgainst == null || identifierAgainst.equals("")));

            if ((onIdentifierTypePkg || onReceiverTypeMatchIdentifier || onIdentifierAgainstNull)
                    && funcNameFromSymbol.equals(functionName)) {
                filteredSymbols.add(symbolInfo);
            }
        });

        documentServiceContext.put(SignatureKeys.FILTERED_FUNCTIONS, filteredSymbols);
    }

    private List<SymbolInfo> getInvokableSymbolsInPackage(BPackageSymbol packageSymbol) {
        Map<Name, Scope.ScopeEntry> scopeEntries = packageSymbol.scope.entries;

        List<Scope.ScopeEntry> entriesList = scopeEntries.values().stream().collect(Collectors.toList());
        return entriesList.stream()
                .filter(scopeEntry -> scopeEntry.symbol instanceof BInvokableSymbol)
                .map(scopeEntry -> new SymbolInfo(scopeEntry.symbol.name.getValue(), scopeEntry))
                .collect(Collectors.toList());
    }
}

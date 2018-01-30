/**
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BEndpointType;
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
        // TODO: move position calculation logic to common place.
        userDefinedType.getPosition().sCol += (previousNode instanceof BLangEndpointTypeNode
                ? "endpoint<".length()
                : 0);
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

        if (varNode.getTypeNode() != null
                && (varNode.getTypeNode() instanceof BLangUserDefinedType
                || varNode.getTypeNode() instanceof BLangEndpointTypeNode)) {
            this.acceptNode(varNode.getTypeNode());
        }
    }

    @Override
    public void visit(BLangLiteral litNode) {
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        // TODO: Move position calculation logic to common place.
        varRefExpr.getPosition().eCol = varRefExpr.getPosition().sCol
                + varRefExpr.variableName.value.length()
                + (!varRefExpr.pkgAlias.value.isEmpty() ? (varRefExpr.pkgAlias.value + ":").length() : 0);
        if (varRefExpr.type instanceof BEndpointType && ((BEndpointType) varRefExpr.type).constraint != null
                && ((BEndpointType) varRefExpr.type).constraint.tsymbol.kind.name().equals(HoverConstants.CONNECTOR)
                && HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position)) {
            this.context.put(HoverKeys.HOVERING_OVER_NODE_KEY, varRefExpr);
            this.context.put(HoverKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            varRefExpr.variableName.setValue(((BEndpointType) varRefExpr.type).constraint.tsymbol.name.getValue());
            this.context.put(HoverKeys.NAME_OF_HOVER_NODE_KEY, varRefExpr.variableName);
            this.context.put(HoverKeys.PACKAGE_OF_HOVER_NODE_KEY,
                    ((BEndpointType) varRefExpr.type).constraint.tsymbol.pkgID);
            this.context.put(HoverKeys.SYMBOL_KIND_OF_HOVER_NODE_KEY,
                    ((BEndpointType) varRefExpr.type).constraint.tsymbol.kind);
            terminateVisitor = true;
        } else if (varRefExpr.type.tsymbol != null && varRefExpr.type.tsymbol.kind != null
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
        previousNode = blockNode;
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
        previousNode = whileNode;
        if (whileNode.expr != null) {
            this.acceptNode(whileNode.expr);
        }
        if (whileNode.body != null) {
            this.acceptNode(whileNode.body);
        }
    }

    public void visit(BLangTransformer transformerNode) {
        previousNode = transformerNode;
        if (transformerNode.source != null) {
            acceptNode(transformerNode.source);
        }

        if (!transformerNode.params.isEmpty()) {
            transformerNode.params.forEach(this::acceptNode);
        }

        if (!transformerNode.retParams.isEmpty()) {
            transformerNode.retParams.forEach(this::acceptNode);
        }

        if (transformerNode.body != null) {
            acceptNode(transformerNode.body);
        }

        if (!transformerNode.workers.isEmpty()) {
            transformerNode.workers.forEach(this::acceptNode);
        }
    }

    public void visit(BLangConnector connectorNode) {
        previousNode = connectorNode;

        if (!connectorNode.params.isEmpty()) {
            connectorNode.params.forEach(this::acceptNode);
        }

        if (!connectorNode.varDefs.isEmpty()) {
            connectorNode.varDefs.forEach(this::acceptNode);
        }

        if (!connectorNode.actions.isEmpty()) {
            connectorNode.actions.forEach(this::acceptNode);
        }
    }

    public void visit(BLangAction actionNode) {
        previousNode = actionNode;

        if (!actionNode.params.isEmpty()) {
            actionNode.params.forEach(this::acceptNode);
        }

        if (!actionNode.retParams.isEmpty()) {
            actionNode.retParams.forEach(this::acceptNode);
        }

        if (actionNode.body != null) {
            acceptNode(actionNode.body);
        }

        if (!actionNode.workers.isEmpty()) {
            actionNode.workers.forEach(this::acceptNode);
        }
    }

    public void visit(BLangService serviceNode) {
        previousNode = serviceNode;
        if (!serviceNode.resources.isEmpty()) {
            serviceNode.resources.forEach(this::acceptNode);
        }

        if (serviceNode.initFunction != null) {
            this.acceptNode(serviceNode.initFunction);
        }
    }

    public void visit(BLangResource resourceNode) {
        previousNode = resourceNode;
        if (!resourceNode.params.isEmpty()) {
            resourceNode.params.forEach(this::acceptNode);
        }

        if (!resourceNode.retParams.isEmpty()) {
            resourceNode.retParams.forEach(this::acceptNode);
        }

        if (resourceNode.body != null) {
            this.acceptNode(resourceNode.body);
        }
    }

    @Override
    public void visit(BLangTryCatchFinally tryCatchFinally) {
        previousNode = tryCatchFinally;
        if (tryCatchFinally.tryBody != null) {
            acceptNode(tryCatchFinally.tryBody);
        }

        if (!tryCatchFinally.catchBlocks.isEmpty()) {
            tryCatchFinally.catchBlocks.forEach(this::acceptNode);
        }

        if (tryCatchFinally.finallyBody != null) {
            acceptNode(tryCatchFinally.finallyBody);
        }
    }

    @Override
    public void visit(BLangCatch bLangCatch) {
        previousNode = bLangCatch;
        if (bLangCatch.param != null) {
            acceptNode(bLangCatch.param);
        }

        if (bLangCatch.body != null) {
            acceptNode(bLangCatch.body);
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        previousNode = transactionNode;
        if (transactionNode.transactionBody != null) {
            acceptNode(transactionNode.transactionBody);
        }

        if (transactionNode.failedBody != null) {
            acceptNode(transactionNode.failedBody);
        }

        if (transactionNode.retryCount != null) {
            acceptNode(transactionNode.retryCount);
        }
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
        previousNode = workerNode;
        if (!workerNode.params.isEmpty()) {
            workerNode.params.forEach(this::acceptNode);
        }

        if (!workerNode.retParams.isEmpty()) {
            workerNode.retParams.forEach(this::acceptNode);
        }

        if (workerNode.body != null) {
            acceptNode(workerNode.body);
        }

        if (!workerNode.workers.isEmpty()) {
            workerNode.workers.forEach(this::acceptNode);
        }
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
        previousNode = returnNode;
        if (!returnNode.exprs.isEmpty()) {
            returnNode.exprs.forEach(this::acceptNode);
        }
    }

    public void visit(BLangNext nextNode) {
        // Ignore.
    }

    public void visit(BLangInvocation invocationExpr) {
        previousNode = invocationExpr;
        if (invocationExpr.expr != null) {
            this.acceptNode(invocationExpr.expr);
        }

        if (!invocationExpr.argExprs.isEmpty()) {
            invocationExpr.argExprs.forEach(this::acceptNode);
        }

        if (!terminateVisitor && HoverUtil.isMatchingPosition(invocationExpr.getPosition(), this.position)) {
            this.context.put(HoverKeys.HOVERING_OVER_NODE_KEY, invocationExpr);
            this.context.put(HoverKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            this.context.put(HoverKeys.NAME_OF_HOVER_NODE_KEY, invocationExpr.name);
            this.context.put(HoverKeys.PACKAGE_OF_HOVER_NODE_KEY, invocationExpr.symbol.pkgID);
            this.context.put(HoverKeys.SYMBOL_KIND_OF_HOVER_NODE_KEY, invocationExpr.symbol.kind);
            this.terminateVisitor = true;
        }
    }

    public void visit(BLangForeach foreach) {
        previousNode = foreach;

        if (foreach.collection != null) {
            acceptNode(foreach.collection);
        }

        if (!foreach.varRefs.isEmpty()) {
            foreach.varRefs.forEach(this::acceptNode);
        }

        if (foreach.body != null) {
            acceptNode(foreach.body);
        }
    }

    public void visit(BLangCompilationUnit compUnit) {

    }

    public void visit(BLangPackageDeclaration pkgDclNode) {

    }

    public void visit(BLangEnum.BLangEnumerator enumeratorNode) {

    }

    public void visit(BLangIdentifier identifierNode) {

    }

    public void visit(BLangAnnotAttribute annotationAttribute) {

    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {

    }

    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {

    }

    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {

    }

    public void visit(BLangBind bindNode) {

    }

    public void visit(BLangBreak breakNode) {

    }

    public void visit(BLangReturn.BLangWorkerReturn returnNode) {

    }

    public void visit(BLangThrow throwNode) {

    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {

    }

    public void visit(BLangArrayLiteral arrayLiteral) {

    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {

    }

    public void visit(BLangConnectorInit connectorInitExpr) {
        previousNode = connectorInitExpr;
        if (connectorInitExpr.connectorType != null) {
            // TODO: check why connectorType's type is null as it is a userdefindtype
            connectorInitExpr.connectorType.type = connectorInitExpr.type;
            acceptNode(connectorInitExpr.connectorType);
        }

        if (!connectorInitExpr.argsExpr.isEmpty()) {
            connectorInitExpr.argsExpr.forEach(this::acceptNode);
        }
    }

    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {

    }

    public void visit(BLangTernaryExpr ternaryExpr) {

    }

    public void visit(BLangUnaryExpr unaryExpr) {

    }

    public void visit(BLangTypeofExpr accessExpr) {

    }

    public void visit(BLangTypeCastExpr castExpr) {

    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        previousNode = conversionExpr;

        if (conversionExpr.expr != null) {
            acceptNode(conversionExpr.expr);
        }

        if (conversionExpr.typeNode != null) {
            acceptNode(conversionExpr.typeNode);
        }

        if (conversionExpr.transformerInvocation != null) {
            acceptNode(conversionExpr.transformerInvocation);
        }
    }

    public void visit(BLangXMLQName xmlQName) {

    }

    public void visit(BLangXMLAttribute xmlAttribute) {

    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {

    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {

    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {

    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {

    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {

    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {

    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {

    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {

    }

    public void visit(BLangValueType valueType) {

    }

    public void visit(BLangArrayType arrayType) {

    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {

    }

    public void visit(BLangEndpointTypeNode endpointType) {
        previousNode = endpointType;
        if (endpointType.constraint != null) {
            acceptNode(endpointType.constraint);
        }
    }

    public void visit(BLangConstrainedType constrainedType) {

    }

    public void visit(BLangFunctionTypeNode functionTypeNode) {

    }

    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {

    }

    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {

    }

    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {

    }

    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {

    }

    public void visit(BLangFieldBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {

    }

    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {

    }

    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {

    }

    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {

    }

    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {

    }

    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {

    }

    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {

    }

    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {

    }

    public void visit(BLangInvocation.BLangFunctionInvocation iExpr) {

    }

    public void visit(BLangInvocation.BLangTransformerInvocation iExpr) {

    }

    public void visit(BLangArrayLiteral.BLangJSONArrayLiteral jsonArrayLiteral) {

    }

    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {

    }

    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {

    }

    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {

    }

    public void visit(BLangFieldBasedAccess.BLangEnumeratorAccessExpr enumeratorAccessExpr) {

    }

    /**
     * Accept node to visit.
     *
     * @param node node to be accepted to visit.
     */
    private void acceptNode(BLangNode node) {
        if (this.terminateVisitor) {
            return;
        }
        node.accept(this);
    }
}

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

package org.ballerinalang.langserver.symbols;

import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangDone;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
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

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor for creating a flat list of symbols found in a compilation unit.
 */
public class SymbolFindingVisitor extends BLangNodeVisitor {
    private List<SymbolInformation> symbols = new ArrayList<SymbolInformation>();
    private String uri = "";

    public SymbolFindingVisitor(LSServiceOperationContext documentServiceContext) {
        this.symbols = documentServiceContext.get(DocumentServiceKeys.SYMBOL_LIST_KEY);
        this.uri = documentServiceContext.get(DocumentServiceKeys.FILE_URI_KEY);
    }

    public void visit(BLangCompilationUnit compUnit) {
        compUnit.getTopLevelNodes().forEach(node -> {
            ((BLangNode) node).accept(this);
        });
    }

    public void visit(BLangFunction funcNode) {
        this.addSymbol(funcNode, funcNode.symbol, SymbolKind.Function);
        funcNode.getBody().accept(this);
    }

    public void visit(BLangStruct structNode) {
        this.addSymbol(structNode, structNode.symbol, null);
    }

    public void visit(BLangService serviceNode) {
        this.addSymbol(serviceNode, serviceNode.symbol, SymbolKind.Function);
    }

    public void visit(BLangConnector connectorNode) {
        this.addSymbol(connectorNode, connectorNode.symbol, SymbolKind.Function);
    }

    public void visit(BLangEnum enumNode) {
        this.addSymbol(enumNode, enumNode.symbol, SymbolKind.Enum);
    }

    public void visit(BLangTransformer transformerNode) {
        this.addSymbol(transformerNode, transformerNode.symbol, SymbolKind.Function);
    }

    public void visit(BLangVariable variableNode) {
        SymbolKind kind;
        String btype = variableNode.getTypeNode().toString();
        switch (btype) {
            case "boolean":
                kind = SymbolKind.Boolean;
                break;

            case "int":
                kind = SymbolKind.Number;
                break;

            case "float":
                kind = SymbolKind.Number;
                break;

            default:
                if (btype.endsWith("[]")) {
                    kind = SymbolKind.Array;
                } else {
                    kind = SymbolKind.Variable;
                }
        }
        this.addSymbol(variableNode, variableNode.symbol, kind);
    }

    public void visit(BLangBlockStmt blockNode) {
        blockNode.stmts.forEach(stmt -> stmt.accept(this));
    }

    public void visit(BLangVariableDef varDefNode) {
        varDefNode.getVariable().accept(this);
    }

    public void visit(BLangImportPackage importNode) {
        // ignore
    }

    public void visit(BLangPackage pkgNode) {
        // ignore
    }

    public void visit(BLangXMLNS xmlnsNode) {
        // ignore
    }

    public void visit(BLangResource resourceNode) {
        // ignore
    }

    public void visit(BLangAction actionNode) {
        // ignore
    }

    public void visit(BLangEnum.BLangEnumerator enumeratorNode) {
        // ignore
    }

    public void visit(BLangWorker workerNode) {
        // ignore
    }

    public void visit(BLangIdentifier identifierNode) {
        // ignore
    }

    public void visit(BLangAnnotation annotationNode) {
        // ignore
    }

    public void visit(BLangAnnotAttribute annotationAttribute) {
        // ignore
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        // ignore
    }

    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {
        // ignore
    }

    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {
        // ignore
    }

    public void visit(BLangAssignment assignNode) {
        // ignore
    }

    public void visit(BLangBind bindNode) {
        // ignore
    }

    public void visit(BLangAbort abortNode) {
        // ignore
    }
    
    public void visit(BLangDone doneNode) {
        // ignore
    }

    public void visit(BLangNext continueNode) {
        // ignore
    }

    public void visit(BLangBreak breakNode) {
        // ignore
    }

    public void visit(BLangReturn returnNode) {
        // ignore
    }

    public void visit(BLangThrow throwNode) {
        // ignore
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        // ignore
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        // ignore
    }

    public void visit(BLangIf ifNode) {
        // ignore
    }

    public void visit(BLangWhile whileNode) {
        // ignore
    }

    public void visit(BLangTransaction transactionNode) {
        // ignore
    }

    public void visit(BLangTryCatchFinally tryNode) {
        // ignore
    }

    public void visit(BLangCatch catchNode) {
        // ignore
    }

    public void visit(BLangForkJoin forkJoin) {
        // ignore
    }

    public void visit(BLangLiteral literalExpr) {
        // ignore
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        // ignore
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        // ignore
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        // ignore
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        // ignore
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        // ignore
    }

    public void visit(BLangInvocation invocationExpr) {
        // ignore
    }

    public void visit(BLangTypeInit connectorInitExpr) {
        // ignore
    }

    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        // ignore
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        // ignore
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        // ignore
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        // ignore
    }

    public void visit(BLangTypedescExpr accessExpr) {
        // ignore
    }

    public void visit(BLangTypeCastExpr castExpr) {
        // ignore
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        // ignore
    }

    public void visit(BLangXMLQName xmlQName) {
        // ignore
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
        // ignore
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        // ignore
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        // ignore
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        // ignore
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        // ignore
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        // ignore
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        // ignore
    }

    public void visit(BLangWorkerSend workerSendNode) {
        // ignore
    }

    public void visit(BLangWorkerReceive workerReceiveNode) {
        // ignore
    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        // ignore
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        // ignore
    }

    public void visit(BLangValueType valueType) {
        // ignore
    }

    public void visit(BLangArrayType arrayType) {
        // ignore
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        // ignore
    }

    public void visit(BLangEndpointTypeNode endpointType) {
        // ignore
    }

    public void visit(BLangConstrainedType constrainedType) {
        // ignore
    }

    public void visit(BLangUserDefinedType userDefinedType) {
        // ignore
    }

    public void visit(BLangFunctionTypeNode functionTypeNode) {
        // ignore
    }

    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        // ignore
    }

    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        // ignore
    }

    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        // ignore
    }

    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        // ignore
    }

    public void visit(BLangFieldBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        // ignore
    }

    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        // ignore
    }

    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        // ignore
    }

    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
        // ignore
    }

    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {
        // ignore
    }

    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        // ignore
    }

    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        // ignore
    }

    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        // ignore
    }

    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        // ignore
    }

    public void visit(BLangInvocation.BLangTransformerInvocation iExpr) {
        // ignore
    }

    public void visit(BLangArrayLiteral.BLangJSONArrayLiteral jsonArrayLiteral) {
        // ignore
    }

    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        // ignore
    }

    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        // ignore
    }

    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        // ignore
    }

    public void visit(BLangFieldBasedAccess.BLangEnumeratorAccessExpr enumeratorAccessExpr) {
        // ignore
    }

    private void addSymbol(BLangNode node, BSymbol balSymbol, SymbolKind kind) {
        SymbolInformation lspSymbol = new SymbolInformation();
        lspSymbol.setName(balSymbol.getName().getValue());
        lspSymbol.setKind(kind);
        lspSymbol.setLocation(new Location(this.uri, getRange(node)));
        this.symbols.add(lspSymbol);
    }

    private Range getRange(BLangNode node) {
        Range r = new Range();

        int startLine = node.getPosition().getStartLine() - 1; // LSP range is 0 based
        int startChar = node.getPosition().getStartColumn() - 1;
        int endLine = node.getPosition().getEndLine() - 1;
        int endChar = node.getPosition().getEndColumn() - 1;

        if (endLine <= 0) {
            endLine = startLine;
        }

        if (endChar <= 0) {
            endChar = startChar + 1;
        }

        r.setStart(new Position(startLine, startChar));
        r.setEnd(new Position(endLine, endChar));

        return r;
    }
}

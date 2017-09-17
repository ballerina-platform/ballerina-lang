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
package org.wso2.ballerinalang.compiler.tree;

import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLanXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangComment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReply;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BlangTransform;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

/**
 * @since 0.94
 */
public abstract class BLangNodeVisitor {

    public void visit(BLangPackage pkgNode) {
        /* ignore */
    }

    public void visit(BLangCompilationUnit compUnit) {
        /* ignore */
    }

    public void visit(BLangPackageDeclaration pkgDclNode) {
        /* ignore */
    }

    public void visit(BLangImportPackage importPkgNode) {
        /* ignore */
    }

    public void visit(BLangXMLNS xmlnsNode) {
        /* ignore */
    }

    public void visit(BLangFunction funcNode) {
        /* ignore */
    }
    
    public void visit(BLangWorker workerNode) {
        /* ignore */
    }
    
    public void visit(BLangForkJoin forkJoin) {
        /* ignore */
    }
    
    public void visit(BLangWorkerSend workerSendNode) {
        /* ignore */
    }
    
    public void visit(BLangWorkerReceive workerReceiveNode) {
        /* ignore */
    }

    public void visit(BLangService serviceNode) {
        /* ignore */
    }

    public void visit(BLangResource resourceNode) {
        /* ignore */
    }

    public void visit(BLangConnector connectorNode) {
        /* ignore */
    }

    public void visit(BLangAction actionNode) {
        /* ignore */
    }

    public void visit(BLangStruct structNode) {
        /* ignore */
    }

    public void visit(BLangEnum enumNode) {
        /* ignore */
    }

    public void visit(BLangVariable varNode) {
        /* ignore */
    }

    public void visit(BLangIdentifier identifierNode) {
        /* ignore */
    }

    public void visit(BLangAnnotation annotationNode) {
        /* ignore */
    }

    public void visit(BLangAnnotAttribute bLangAnnotationAttribute) {
        /* ignore */
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        /* ignore */
    }

    public void visit(BLangAnnotAttachmentAttributeValue bLangAnnotAttributeValue) {
        /* ignore */
    }

    public void visit(BLangAnnotAttachmentAttribute bLangAnnotAttachmentAttribute) {
        /* ignore */
    }

    // Statements
    public void visit(BLangBlockStmt blockNode) {
        /* ignore */
    }

    public void visit(BLangVariableDef varDefNode) {
        /* ignore */
    }

    public void visit(BLangAssignment assignNode) {
        /* ignore */
    }

    public void visit(BLangAbort abortNode) {
        /* ignore */
    }

    public void visit(BLangContinue continueNode) {
        /* ignore */
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    public void visit(BLangReturn returnNode) {
        /* ignore */
    }

    public void visit(BLangReply replyNode) {
        /* ignore */
    }

    public void visit(BLangThrow throwNode) {
        /* ignore */
    }

    public void visit(BLanXMLNSStatement xmlnsStmtNode) {
        /* ignore */
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        /* ignore */
    }

    public void visit(BLangComment commentNode) {
        /* ignore */
    }

    public void visit(BLangIf ifNode) {
        /* ignore */
    }

    public void visit(BLangWhile whileNode) {
        /* ignore */
    }

    public void visit(BLangTransaction transactionNode) {
        /* ignore */
    }

    public void visit(BlangTransform transformNode) {
        /* ignore */
    }

    public void visit(BLangTryCatchFinally tryNode) {
        /* ignore */
    }

    public void visit(BLangCatch catchNode) {
        /* ignore */
    }


    // Expressions

    public void visit(BLangLiteral literalExpr) {
        /* ignore */
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        /* ignore */
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        /* ignore */
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        /* ignore */
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        /* ignore */
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        /* ignore */
    }

    public void visit(BLangInvocation invocationExpr) {
        /* ignore */
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        /* ignore */
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        /* ignore */
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        /* ignore */
    }

    public void visit(BLangTypeCastExpr castExpr) {
        /* ignore */
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        /* ignore */
    }

    public void visit(BLangXMLQName bLangXMLQName) {
        /* ignore */
    }

    public void visit(BLangXMLAttribute bLangXMLAttribute) {
        /* ignore */
    }

    public void visit(BLangXMLElementLiteral bLangXMLElementLiteral) {
        /* ignore */
    }

    public void visit(BLangXMLTextLiteral bLangXMLTextLiteral) {
        /* ignore */
    }

    public void visit(BLangXMLCommentLiteral bLangXMLCommentLiteral) {
        /* ignore */
    }

    public void visit(BLangXMLProcInsLiteral bLangXMLProcInsLiteral) {
        /* ignore */
    }

    public void visit(BLangXMLQuotedString bLangXMLQuotedString) {
        /* ignore */
    }

    public void visit(BLangStringTemplateLiteral bLangStringTemplateLiteral) {
        /* ignore */
    }

    // Type nodes

    public void visit(BLangValueType valueType) {
        /* ignore */
    }

    public void visit(BLangArrayType arrayType) {
        /* ignore */
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        /* ignore */
    }

    public void visit(BLangConstrainedType constrainedType) {
        /* ignore */
    }

    public void visit(BLangUserDefinedType userDefinedType) {
        /* ignore */
    }
}

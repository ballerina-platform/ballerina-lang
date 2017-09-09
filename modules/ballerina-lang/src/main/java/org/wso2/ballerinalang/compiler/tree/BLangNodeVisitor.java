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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLanXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangComment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReply;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
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
        throw new AssertionError();
    }

    public void visit(BLangCompilationUnit compUnit) {
        throw new AssertionError();
    }

    public void visit(BLangPackageDeclaration pkgDclNode) {
        throw new AssertionError();
    }

    public void visit(BLangImportPackage importPkgNode) {
        throw new AssertionError();
    }

    public void visit(BLangXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangFunction funcNode) {
        throw new AssertionError();
    }

    public void visit(BLangService serviceNode) {
        throw new AssertionError();
    }

    public void visit(BLangResource resourceNode) {
        throw new AssertionError();
    }

    public void visit(BLangConnector connectorNode) {
        throw new AssertionError();
    }

    public void visit(BLangAction actionNode) {
        throw new AssertionError();
    }

    public void visit(BLangStruct structNode) {
        throw new AssertionError();
    }

    public void visit(BLangEnum enumNode) {
        throw new AssertionError();
    }

    public void visit(BLangVariable varNode) {
        throw new AssertionError();
    }

    public void visit(BLangIdentifier identifierNode) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotation annotationNode) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotAttribute bLangAnnotationAttribute) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotAttachmentAttributeValue bLangAnnotAttributeValue) {
        throw new AssertionError();
    }

    public void visit(BLangAnnotAttachmentAttribute bLangAnnotAttachmentAttribute) {
        throw new AssertionError();
    }

    // Statements
    public void visit(BLangBlockStmt blockNode) {
        throw new AssertionError();
    }

    public void visit(BLangVariableDef varDefNode) {
        throw new AssertionError();
    }

    public void visit(BLangAssignment assignNode) {
        throw new AssertionError();
    }

    public void visit(BLangAbort abortNode) {
        throw new AssertionError();
    }

    public void visit(BLangContinue continueNode) {
        throw new AssertionError();
    }

    public void visit(BLangBreak breakNode) {
        throw new AssertionError();
    }

    public void visit(BLangReturn returnNode) {
        throw new AssertionError();
    }

    public void visit(BLangReply replyNode) {
        throw new AssertionError();
    }

    public void visit(BLangThrow throwNode) {
        throw new AssertionError();
    }

    public void visit(BLanXMLNSStatement xmlnsStmtNode) {
        throw new AssertionError();
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        throw new AssertionError();
    }

    public void visit(BLangComment commentNode) {
        throw new AssertionError();
    }

    public void visit(BLangIf ifNode) {
        throw new AssertionError();
    }

    public void visit(BLangWhile whileNode) {
        throw new AssertionError();
    }

    public void visit(BLangTransaction transactionNode) {
        throw new AssertionError();
    }

    public void visit(BlangTransform transformNode) {
        throw new AssertionError();
    }

    public void visit(BLangTryCatchFinally tryNode) {
        throw new AssertionError();
    }


    // Expressions
    public void visit(BLangLiteral literalNode) {
        throw new AssertionError();
    }

    public void visit(BLangVariableReference varRefNode) {
        throw new AssertionError();
    }
    // Type nodes
    public void visit(BLangValueType valueType) {
        throw new AssertionError();
    }

    public void visit(BLangArrayType arrayType) {
        throw new AssertionError();
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        throw new AssertionError();
    }

    public void visit(BLangConstrainedType constrainedType) {
        throw new AssertionError();
    }

    public void visit(BLangUserDefinedType userDefinedType) {
        throw new AssertionError();
    }
}

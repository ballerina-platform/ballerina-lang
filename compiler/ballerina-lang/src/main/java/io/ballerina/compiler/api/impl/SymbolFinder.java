/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangClientDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangCaptureBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorCauseBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorFieldBindingPatterns;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorMessageBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangFieldBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangMappingBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangNamedArgBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangRestBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangSimpleBindingPattern;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecatedParametersDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAssertion;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAtomCharOrEscape;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAtomQuantifier;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCapturingGroups;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharSet;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharSetRange;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharacterClass;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReDisjunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReFlagExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReFlagsOnOff;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReQuantifier;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReSequence;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRegExpTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTransactionalExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementFilter;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangConstPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorCauseMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorFieldMatchPatterns;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorMessageMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangFieldMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangListMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMappingMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangNamedArgMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangRestMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangSimpleMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangVarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangClientDeclarationStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDo;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatchStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetryTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRollback;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.LinkedList;
import java.util.List;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * Finds the enclosing AST node's symbol for the given position.
 *
 * @since 2.0.0
 */
class SymbolFinder extends BaseVisitor {

    private LinePosition cursorPos;
    private BSymbol symbolAtCursor;

    BSymbol lookup(BLangCompilationUnit unit, LinePosition cursorPos) {
        this.cursorPos = cursorPos;
        this.symbolAtCursor = null;

        for (TopLevelNode node : unit.topLevelNodes) {
            if ((!PositionUtil.withinBlock(this.cursorPos, node.getPosition()) && !isWithinNodeMetaData(node))
                    || isLambdaFunction(node)) {
                continue;
            }

            ((BLangNode) node).accept(this);
        }

        return this.symbolAtCursor;
    }

    private void lookupNodes(List<? extends BLangNode> nodes) {
        for (BLangNode node : nodes) {
            if (!PositionUtil.withinBlock(this.cursorPos, node.pos)) {
                continue;
            }

            node.accept(this);
            // TODO: Check whether we can return from here.
        }
    }

    private void lookupNode(BLangNode node) {
        if (node == null) {
            return;
        }

        if (PositionUtil.withinBlock(this.cursorPos, node.pos)) {
            node.accept(this);
        }
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        if (importPkgNode.orgName.pos != null && setEnclosingNode(importPkgNode.symbol, importPkgNode.orgName.pos)) {
            return;
        }
        if (setEnclosingNode(importPkgNode.symbol, importPkgNode.alias.pos)) {
            return;
        }
        if (importPkgNode.version.pos != null) {
            setEnclosingNode(importPkgNode.symbol, importPkgNode.version.pos);
        }
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        if (setEnclosingNode(xmlnsNode.symbol, xmlnsNode.prefix.pos)) {
            return;
        }

        lookupNode(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangClientDeclarationStatement clientDeclarationStatement) {
        lookupNode(clientDeclarationStatement.getClientDeclaration());
    }

    @Override
    public void visit(BLangClientDeclaration clientDeclNode) {
        if (setEnclosingNode(clientDeclNode.symbol, clientDeclNode.prefix.pos)) {
            return;
        }

        lookupNode((BLangNode) clientDeclNode.getUri());
    }

    @Override
    public void visit(BLangFunction funcNode) {
        if (setEnclosingNode(funcNode.symbol, funcNode.name.pos)) {
            return;
        }

        lookupNode(funcNode.markdownDocumentationAttachment);
        lookupNodes(funcNode.annAttachments);
        lookupNodes(funcNode.requiredParams);
        lookupNode(funcNode.restParam);
        lookupNodes(funcNode.returnTypeAnnAttachments);
        lookupNode(funcNode.returnTypeNode);
        lookupNode(funcNode.body);
    }

    @Override
    public void visit(BLangResourceFunction resourceFunction) {
        visit((BLangFunction) resourceFunction);
    }

    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        lookupNodes(blockFuncBody.stmts);
    }

    @Override
    public void visit(BLangExprFunctionBody exprFuncBody) {
        lookupNode(exprFuncBody.expr);
    }

    @Override
    public void visit(BLangExternalFunctionBody externFuncBody) {
        lookupNodes(externFuncBody.annAttachments);
    }

    @Override
    public void visit(BLangService serviceNode) {
        lookupNodes(serviceNode.annAttachments);
        lookupNode(serviceNode.serviceClass);
        lookupNodes(serviceNode.attachedExprs);

        // A service decl is a special case. Since there isn't a name associated with it, we take the starting
        // position of the node to lookup the service symbol.
        if (this.symbolAtCursor == null && this.cursorPos.equals(serviceNode.pos.lineRange().startLine())) {
            this.symbolAtCursor = serviceNode.symbol;
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        if (setEnclosingNode(typeDefinition.symbol, typeDefinition.name.pos)) {
            return;
        }

        lookupNode(typeDefinition.markdownDocumentationAttachment);
        lookupNodes(typeDefinition.annAttachments);
        lookupNode(typeDefinition.typeNode);
    }

    @Override
    public void visit(BLangConstant constant) {
        if (setEnclosingNode(constant.symbol, constant.name.pos)) {
            return;
        }

        lookupNodes(constant.annAttachments);
        lookupNode(constant.typeNode);
        lookupNode(constant.expr);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        if (setEnclosingNode(varNode.symbol, varNode.name.pos)) {
            return;
        }

        lookupNodes(varNode.annAttachments);
        lookupNode(varNode.typeNode);
        lookupNode(varNode.expr);
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        // ignore
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        if (setEnclosingNode(annotationNode.symbol, annotationNode.name.pos)) {
            return;
        }

        lookupNodes(annotationNode.annAttachments);
        lookupNode(annotationNode.typeNode);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        if (annAttachmentNode.annotationSymbol != null
                && (setEnclosingNode(annAttachmentNode.annotationSymbol.owner, annAttachmentNode.pkgAlias.pos) ||
                        annAttachmentNode.annotationSymbol.getOrigin().equals(SymbolOrigin.BUILTIN))) {
            return;
        }

        if (setEnclosingNode(annAttachmentNode.annotationSymbol, annAttachmentNode.annotationName.pos)) {
            return;
        }

        lookupNode(annAttachmentNode.expr);
    }

    @Override
    public void visit(BLangTableKeySpecifier tableKeySpecifierNode) {
        // TODO: How to figure out the symbols for the specified keys
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint tableKeyTypeConstraint) {
        lookupNode(tableKeyTypeConstraint.keyType);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        lookupNodes(blockNode.stmts);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        lookupNode(varDefNode.var);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        lookupNode(assignNode.varRef);
        lookupNode(assignNode.expr);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        lookupNode(compoundAssignNode.varRef);
        lookupNode(compoundAssignNode.expr);
    }

    @Override
    public void visit(BLangRetry retryNode) {
        lookupNode(retryNode.retryBody);
        lookupNode(retryNode.retrySpec);
        lookupNode(retryNode.onFailClause);
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        lookupNode(retryTransaction.transaction);
        lookupNode(retryTransaction.retrySpec);
    }

    @Override
    public void visit(BLangRetrySpec retrySpec) {
        lookupNode(retrySpec.retryManagerType);
        lookupNodes(retrySpec.argExprs);
    }

    @Override
    public void visit(BLangContinue continueNode) {
        // ignore
    }

    @Override
    public void visit(BLangBreak breakNode) {
        // ignore
    }

    @Override
    public void visit(BLangReturn returnNode) {
        lookupNode(returnNode.expr);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        lookupNode(panicNode.expr);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        lookupNode(xmlnsStmtNode.xmlnsDecl);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        lookupNode(exprStmtNode.expr);
    }

    @Override
    public void visit(BLangIf ifNode) {
        lookupNode(ifNode.expr);
        lookupNode(ifNode.body);
        lookupNode(ifNode.elseStmt);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        lookupNodes(queryAction.queryClauseList);
        lookupNode(queryAction.doClause);
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern) {
        setEnclosingNode(captureBindingPattern.symbol, captureBindingPattern.getIdentifier().getPosition());
    }

    @Override
    public void visit(BLangListBindingPattern listBindingPattern) {
        lookupNodes(listBindingPattern.bindingPatterns);
        lookupNode(listBindingPattern.restBindingPattern);
    }

    @Override
    public void visit(BLangMappingBindingPattern mappingBindingPattern) {
        lookupNodes(mappingBindingPattern.fieldBindingPatterns);
        lookupNode(mappingBindingPattern.restBindingPattern);
    }

    @Override
    public void visit(BLangFieldBindingPattern fieldBindingPattern) {
        lookupNode(fieldBindingPattern.bindingPattern);
    }

    @Override
    public void visit(BLangRestBindingPattern restBindingPattern) {
        setEnclosingNode(restBindingPattern.symbol, restBindingPattern.getIdentifier().getPosition());
    }

    @Override
    public void visit(BLangErrorBindingPattern errorBindingPattern) {
        lookupNode(errorBindingPattern.errorTypeReference);
        lookupNode(errorBindingPattern.errorMessageBindingPattern);
        lookupNode(errorBindingPattern.errorCauseBindingPattern);
        lookupNode(errorBindingPattern.errorFieldBindingPatterns);
    }

    @Override
    public void visit(BLangErrorMessageBindingPattern errorMessageBindingPattern) {
        lookupNode(errorMessageBindingPattern.simpleBindingPattern);
    }

    @Override
    public void visit(BLangErrorCauseBindingPattern errorCauseBindingPattern) {
        lookupNode(errorCauseBindingPattern.simpleBindingPattern);
        lookupNode(errorCauseBindingPattern.errorBindingPattern);
    }

    @Override
    public void visit(BLangErrorFieldBindingPatterns errorFieldBindingPatterns) {
        lookupNodes(errorFieldBindingPatterns.namedArgBindingPatterns);
        lookupNode(errorFieldBindingPatterns.restBindingPattern);
    }

    @Override
    public void visit(BLangSimpleBindingPattern simpleBindingPattern) {
        lookupNode(simpleBindingPattern.captureBindingPattern);
    }

    @Override
    public void visit(BLangNamedArgBindingPattern namedArgBindingPattern) {
        lookupNode(namedArgBindingPattern.bindingPattern);
    }

    @Override
    public void visit(BLangMatchStatement matchStatementNode) {
        lookupNode(matchStatementNode.expr);
        lookupNodes(matchStatementNode.matchClauses);
        lookupNode(matchStatementNode.onFailClause);
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
        lookupNodes(matchClause.matchPatterns);
        lookupNode(matchClause.matchGuard);
        lookupNode(matchClause.blockStmt);
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern) {
        lookupNode(constMatchPattern.expr);
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern) {
        lookupNode(varBindingPattern.getBindingPattern());
    }

    @Override
    public void visit(BLangMappingMatchPattern mappingMatchPattern) {
        lookupNodes(mappingMatchPattern.fieldMatchPatterns);
        lookupNode(mappingMatchPattern.restMatchPattern);
    }

    @Override
    public void visit(BLangFieldMatchPattern fieldMatchPattern) {
        lookupNode(fieldMatchPattern.matchPattern);
    }

    @Override
    public void visit(BLangRestMatchPattern restMatchPattern) {
        setEnclosingNode(restMatchPattern.symbol, restMatchPattern.variableName.pos);
    }

    @Override
    public void visit(BLangListMatchPattern listMatchPattern) {
        lookupNodes(listMatchPattern.matchPatterns);
        lookupNode(listMatchPattern.restMatchPattern);
    }

    @Override
    public void visit(BLangErrorMatchPattern errorMatchPattern) {
        lookupNode(errorMatchPattern.errorTypeReference);
        lookupNode(errorMatchPattern.errorMessageMatchPattern);
        lookupNode(errorMatchPattern.errorCauseMatchPattern);
        lookupNode(errorMatchPattern.errorFieldMatchPatterns);
    }

    @Override
    public void visit(BLangErrorMessageMatchPattern errorMessageMatchPattern) {
        lookupNode(errorMessageMatchPattern.simpleMatchPattern);
    }

    @Override
    public void visit(BLangErrorCauseMatchPattern errorCauseMatchPattern) {
        lookupNode(errorCauseMatchPattern.simpleMatchPattern);
        lookupNode(errorCauseMatchPattern.errorMatchPattern);
    }

    @Override
    public void visit(BLangErrorFieldMatchPatterns errorFieldMatchPatterns) {
        lookupNodes(errorFieldMatchPatterns.namedArgMatchPatterns);
        lookupNode(errorFieldMatchPatterns.restMatchPattern);
    }

    @Override
    public void visit(BLangSimpleMatchPattern simpleMatchPattern) {
        lookupNode(simpleMatchPattern.constPattern);
        lookupNode(simpleMatchPattern.varVariableName);
    }

    @Override
    public void visit(BLangNamedArgMatchPattern namedArgMatchPattern) {
        lookupNode(namedArgMatchPattern.matchPattern);
    }

    @Override
    public void visit(BLangMatchGuard matchGuard) {
        lookupNode(matchGuard.expr);
    }

    @Override
    public void visit(BLangForeach foreach) {
        lookupNode((BLangNode) foreach.variableDefinitionNode);
        lookupNode(foreach.collection);
        lookupNode(foreach.body);
        lookupNode(foreach.onFailClause);
    }

    @Override
    public void visit(BLangDo doNode) {
        lookupNode(doNode.body);
        lookupNode(doNode.onFailClause);
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        lookupNode(fromClause.collection);
        lookupNode((BLangNode) fromClause.variableDefinitionNode);
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        lookupNode(joinClause.collection);
        lookupNode((BLangNode) joinClause.variableDefinitionNode);
        lookupNode(joinClause.onClause);
    }

    @Override
    public void visit(BLangLetClause letClause) {
        for (BLangLetVariable var : letClause.letVarDeclarations) {
            lookupNode((BLangNode) var.definitionNode);
        }
    }

    @Override
    public void visit(BLangOnClause onClause) {
        lookupNode(onClause.lhsExpr);
        lookupNode(onClause.rhsExpr);
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause) {
        lookupNode(orderKeyClause.expression);
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        for (OrderKeyNode key : orderByClause.orderByKeyList) {
            lookupNode((BLangNode) key);
        }
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        lookupNode(selectClause.expression);
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        lookupNode(whereClause.expression);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        lookupNode(doClause.body);
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        lookupNode((BLangNode) onFailClause.variableDefinitionNode);
        lookupNode(onFailClause.body);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        lookupNode(onConflictClause.expression);
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        lookupNode(limitClause.expression);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        lookupNode(whileNode.expr);
        lookupNode(whileNode.body);
        lookupNode(whileNode.onFailClause);
    }

    @Override
    public void visit(BLangLock lockNode) {
        lookupNode(lockNode.body);
        lookupNode(lockNode.onFailClause);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        lookupNode(transactionNode.transactionBody);
        lookupNode(transactionNode.onFailClause);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        lookupNode(stmt.expr);
        lookupNode(stmt.varRef);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        lookupNode(stmt.expr);
        lookupNode(stmt.varRef);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        lookupNode(stmt.expr);
        lookupNode(stmt.varRef);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        lookupNodes(forkJoin.workers);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        if (setEnclosingNode(workerSendNode.workerSymbol, workerSendNode.workerIdentifier.pos)) {
            return;
        }

        lookupNode(workerSendNode.expr);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        setEnclosingNode(workerReceiveNode.workerSymbol, workerReceiveNode.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        lookupNode(rollbackNode.expr);
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        // ignore
    }

    @Override
    public void visit(BLangConstRef constRef) {
        if (constRef.symbol != null && setEnclosingNode(constRef.symbol.owner, constRef.pkgAlias.pos)) {
            return;
        }
        this.symbolAtCursor = constRef.symbol;
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        // ignore
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            lookupNode((BLangNode) field);
        }
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        lookupNodes(varRefExpr.expressions);
        lookupNode(varRefExpr.restParam);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        for (BLangRecordVarRef.BLangRecordVarRefKeyValue recordRefField : varRefExpr.recordRefFields) {
            lookupNode(recordRefField.getBindingPattern());
        }

        lookupNode(varRefExpr.restParam);
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        lookupNode(varRefExpr.message);
        lookupNodes(varRefExpr.detail);
        lookupNode(varRefExpr.cause);
        lookupNode(varRefExpr.restVar);
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        if (setEnclosingNode(varRefExpr.symbol, varRefExpr.variableName.pos)) {
            return;
        }

        setEnclosingNode(varRefExpr.pkgSymbol, varRefExpr.pkgAlias.pos);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (setEnclosingNode(fieldAccessExpr.symbol, fieldAccessExpr.field.pos)) {
            return;
        }

        lookupNode(fieldAccessExpr.expr);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess nsPrefixedFieldBasedAccess) {
        if (setEnclosingNode(nsPrefixedFieldBasedAccess.nsSymbol, nsPrefixedFieldBasedAccess.nsPrefix.pos)) {
            return;
        }

        lookupNode(nsPrefixedFieldBasedAccess.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        lookupNode(indexAccessExpr.expr);

        if (indexAccessExpr.indexExpr instanceof BLangLiteral) {
            setEnclosingNode(indexAccessExpr.symbol, indexAccessExpr.indexExpr.pos);
        } else {
            lookupNode(indexAccessExpr.indexExpr);
        }
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        // The assumption for the first condition is that if it's moduled-qualified, it must be a public symbol.
        // Hence owner would be a package symbol.
        if ((invocationExpr.symbol != null && setEnclosingNode(invocationExpr.symbol.owner,
                invocationExpr.pkgAlias.pos))
                || setEnclosingNode(invocationExpr.symbol, invocationExpr.name.pos)) {
            return;
        }

        lookupNodes(invocationExpr.argExprs);
        lookupNodes(invocationExpr.restArgs);
        lookupNode(invocationExpr.expr);
    }

    @Override
    public void visit(BLangTypeInit typeInit) {
        lookupNodes(typeInit.argsExpr);
        lookupNode(typeInit.userDefinedType);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        // The assumption for the first condition is that if it's moduled-qualified, it must be a public symbol.
        // Hence owner would be a package symbol.
        if ((actionInvocationExpr.symbol != null && setEnclosingNode(actionInvocationExpr.symbol.owner,
                actionInvocationExpr.pkgAlias.pos))
                || setEnclosingNode(actionInvocationExpr.symbol, actionInvocationExpr.name.pos)) {
            return;
        }

        lookupNodes(actionInvocationExpr.annAttachments);
        lookupNodes(actionInvocationExpr.requiredArgs);
        lookupNodes(actionInvocationExpr.restArgs);
        lookupNode(actionInvocationExpr.expr);
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        lookupNode(ternaryExpr.expr);
        lookupNode(ternaryExpr.thenExpr);
        lookupNode(ternaryExpr.elseExpr);
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        lookupNodes(awaitExpr.exprList);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        lookupNode(trapExpr.expr);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        lookupNode(binaryExpr.lhsExpr);
        lookupNode(binaryExpr.rhsExpr);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        lookupNode(elvisExpr.lhsExpr);
        lookupNode(elvisExpr.rhsExpr);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        lookupNode(groupExpr.expression);
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        for (BLangLetVariable var : letExpr.letVarDeclarations) {
            lookupNode((BLangNode) var.definitionNode);
        }

        lookupNode(letExpr.expr);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        lookupNodes(listConstructorExpr.exprs);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangListConstructorSpreadOpExpr spreadOpExpr) {
        lookupNode(spreadOpExpr.expr);
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        lookupNode(tableConstructorExpr.tableKeySpecifier);
        lookupNodes(tableConstructorExpr.recordLiteralList);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        lookupNodes(tupleLiteral.exprs);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        lookupNodes(arrayLiteral.exprs);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        lookupNode(unaryExpr.expr);
    }

    @Override
    public void visit(BLangTypedescExpr typedescExpr) {
        lookupNode(typedescExpr.typeNode);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        lookupNodes(conversionExpr.annAttachments);
        lookupNode(conversionExpr.typeNode);
        lookupNode(conversionExpr.expr);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        if (setEnclosingNode(xmlQName.nsSymbol, xmlQName.prefix.pos)) {
            return;
        }

        setEnclosingNode(xmlQName.nsSymbol, xmlQName.localname.pos);
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        lookupNode(xmlAttribute.name);
        lookupNode(xmlAttribute.value);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        lookupNode(xmlElementLiteral.startTagName);
        lookupNodes(xmlElementLiteral.attributes);
        lookupNodes(xmlElementLiteral.children);
        lookupNode(xmlElementLiteral.endTagName);
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        lookupNode(xmlTextLiteral.concatExpr);
        lookupNodes(xmlTextLiteral.textFragments);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        lookupNode(xmlCommentLiteral.concatExpr);
        lookupNodes(xmlCommentLiteral.textFragments);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        lookupNode(xmlProcInsLiteral.dataConcatExpr);
        lookupNodes(xmlProcInsLiteral.dataFragments);
        lookupNode(xmlProcInsLiteral.target);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        lookupNode(xmlQuotedString.concatExpr);
        lookupNodes(xmlQuotedString.textFragments);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        lookupNodes(stringTemplateLiteral.exprs);
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        lookupNodes(rawTemplateLiteral.strings);
        lookupNodes(rawTemplateLiteral.insertions);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        lookupNode(bLangLambdaFunction.function);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        lookupNodes(bLangArrowFunction.params);
        lookupNode(bLangArrowFunction.body);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        lookupNode(bLangVarArgsExpression.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        if (setEnclosingNode(bLangNamedArgsExpression.varSymbol, bLangNamedArgsExpression.name.pos)) {
            return;
        }
        lookupNode(bLangNamedArgsExpression.expr);
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        lookupNode(assignableExpr.lhsExpr);
        lookupNode(assignableExpr.typeNode);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        lookupNode(checkedExpr.expr);
    }

    @Override
    public void visit(BLangFail failExpr) {
        lookupNode(failExpr.expr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        lookupNode(checkPanickedExpr.expr);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        lookupNode(serviceConstructorExpr.serviceNode);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        lookupNode(typeTestExpr.expr);
        lookupNode(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        lookupNode(typeTestExpr.expr);
        lookupNode(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        // ignore
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        lookupNode(annotAccessExpr.expr);
        if (annotAccessExpr.annotationName != null) {
            setEnclosingNode(annotAccessExpr.annotationSymbol, annotAccessExpr.annotationName.pos);
        }
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        lookupNodes(queryExpr.queryClauseList);
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr) {
        super.visit(transactionalExpr);
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
        super.visit(commitExpr);
    }

    @Override
    public void visit(BLangValueType valueType) {
        this.symbolAtCursor = valueType.getBType().tsymbol;
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        lookupNode(arrayType.elemtype);
        lookupNodes(arrayType.sizes);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        this.symbolAtCursor = builtInRefType.getBType().tsymbol;
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        lookupNode(constrainedType.constraint);

        if (this.symbolAtCursor == null) {
            this.symbolAtCursor = constrainedType.getBType().tsymbol;
        }
    }

    @Override
    public void visit(BLangStreamType streamType) {
        lookupNode(streamType.constraint);
        lookupNode(streamType.error);

        if (symbolAtCursor == null) {
            this.symbolAtCursor = streamType.getBType().tsymbol;
        }
    }

    @Override
    public void visit(BLangTableTypeNode tableType) {
        lookupNode(tableType.constraint);
        lookupNode(tableType.tableKeySpecifier);
        lookupNode(tableType.tableKeyTypeConstraint);

        if (this.symbolAtCursor == null && tableType.tableType != null) {
            this.symbolAtCursor = tableType.tableType.tsymbol;
        }
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        // Becomes null for undefined types
        if (userDefinedType.symbol == null) {
            return;
        }

        if (userDefinedType.symbol.origin == VIRTUAL
                || setEnclosingNode(userDefinedType.symbol, userDefinedType.typeName.pos)) {
            return;
        }

        setEnclosingNode(userDefinedType.symbol.owner, userDefinedType.pkgAlias.pos);
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        lookupNodes(functionTypeNode.params);
        lookupNode(functionTypeNode.restParam);
        lookupNode(functionTypeNode.returnTypeNode);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        lookupNodes(unionTypeNode.memberTypeNodes);
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        lookupNodes(intersectionTypeNode.constituentTypeNodes);
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        if (!isClassDefForServiceOrObjectCtr(classDefinition) &&
                setEnclosingNode(classDefinition.symbol, classDefinition.name.pos)) {
            return;
        }

        lookupNode(classDefinition.markdownDocumentationAttachment);
        lookupNodes(classDefinition.annAttachments);

        for (BLangSimpleVariable field : classDefinition.fields) {
            lookupNodes(field.annAttachments);
        }

        lookupNodes(classDefinition.fields);
        lookupNodes(classDefinition.referencedFields);
        lookupNode(classDefinition.initFunction);
        lookupNodes(classDefinition.functions);
        lookupNodes(classDefinition.typeRefs);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        for (BLangSimpleVariable field : objectTypeNode.fields) {
            lookupNodes(field.annAttachments);
        }

        lookupNodes(objectTypeNode.fields);
        lookupNodes(objectTypeNode.functions);
        lookupNodes(objectTypeNode.typeRefs);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        for (BLangSimpleVariable field : recordTypeNode.fields) {
            lookupNodes(field.annAttachments);
        }

        lookupNodes(recordTypeNode.fields);
        lookupNodes(recordTypeNode.typeRefs);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        // Special case handling for singleton types.
        if (finiteTypeNode.valueSpace.size() == 1 && this.symbolAtCursor == null) {
            this.symbolAtCursor = finiteTypeNode.getBType().tsymbol;
        }

        lookupNodes(finiteTypeNode.valueSpace);
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        lookupNodes(tupleTypeNode.memberTypeNodes);
        lookupNode(tupleTypeNode.restParamType);
    }

    @Override
    public void visit(BLangErrorType errorType) {
        lookupNode(errorType.detailType);

        if (symbolAtCursor == null) {
            this.symbolAtCursor = errorType.getBType().tsymbol;
        }
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        lookupNode(errorConstructorExpr.errorTypeRef);
        lookupNodes(errorConstructorExpr.positionalArgs);
        lookupNodes(errorConstructorExpr.namedArgs);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        this.symbolAtCursor = localVarRef.symbol;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        this.symbolAtCursor = fieldVarRef.symbol;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        this.symbolAtCursor = packageVarRef.symbol;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        this.symbolAtCursor = functionVarRef.symbol;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        this.symbolAtCursor = typeLoad.symbol;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        lookupNode(fieldAccessExpr.expr);
        setEnclosingNode(fieldAccessExpr.symbol, fieldAccessExpr.indexExpr.pos);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        if (setEnclosingNode(functionVarRef.symbol, functionVarRef.field.pos)) {
            return;
        }

        lookupNode(functionVarRef.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        lookupNode(mapKeyAccessExpr.expr);
        lookupNode(mapKeyAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        lookupNode(arrayIndexAccessExpr.expr);
        lookupNode(arrayIndexAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableKeyAccessExpr) {
        lookupNode(tableKeyAccessExpr.expr);
        lookupNode(tableKeyAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {
        lookupNode(xmlAccessExpr.expr);
        lookupNode(xmlAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        for (RecordLiteralNode.RecordField field : mapLiteral.fields) {
            lookupNode((BLangNode) field);
        }
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        for (RecordLiteralNode.RecordField field : structLiteral.fields) {
            lookupNode((BLangNode) field);
        }
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        if (setEnclosingNode(bFunctionPointerInvocation.symbol, bFunctionPointerInvocation.name.pos)) {
            return;
        }
        lookupNodes(bFunctionPointerInvocation.requiredArgs);
        lookupNodes(bFunctionPointerInvocation.restArgs);
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        if (setEnclosingNode(iExpr.symbol, iExpr.name.pos)) {
            return;
        }
        lookupNode(iExpr.expr);
        lookupNodes(iExpr.requiredArgs);
        lookupNodes(iExpr.restArgs);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        lookupNodes(jsonArrayLiteral.exprs);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        lookupNode(jsonAccessExpr.expr);
        lookupNode(jsonAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        lookupNode(stringAccessExpr.expr);
        lookupNode(stringAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        if (setEnclosingNode(xmlnsNode.symbol, xmlnsNode.prefix.pos)) {
            return;
        }

        lookupNode(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        if (setEnclosingNode(xmlnsNode.symbol, xmlnsNode.prefix.pos)) {
            return;
        }

        lookupNode(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        lookupNodes(bLangXMLSequenceLiteral.xmlItems);
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        lookupNode(bLangStatementExpression.expr);
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {
        // ignore
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {
        setEnclosingNode(bLangDocumentationParameter.symbol, bLangDocumentationParameter.pos);
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {
        // ignore
    }

    @Override
    public void visit(BLangMarkDownDeprecationDocumentation bLangMarkDownDeprecationDocumentation) {
        // ignore
    }

    @Override
    public void visit(BLangMarkDownDeprecatedParametersDocumentation bLangMarkDownDeprecatedParametersDocumentation) {
        // ignore
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
        lookupNodes(bLangMarkdownDocumentation.parameters);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        lookupNodes(bLangTupleVariable.annAttachments);
        lookupNode(bLangTupleVariable.typeNode);
        lookupNodes(bLangTupleVariable.memberVariables);
        lookupNode(bLangTupleVariable.restVariable);
        lookupNode(bLangTupleVariable.expr);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        lookupNode(bLangTupleVariableDef.var);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        lookupNodes(bLangRecordVariable.annAttachments);
        lookupNode(bLangRecordVariable.typeNode);

        for (BLangRecordVariable.BLangRecordVariableKeyValue var : bLangRecordVariable.variableList) {
            lookupNode(var.valueBindingPattern);
        }

        lookupNode(bLangRecordVariable.restParam);
        lookupNode(bLangRecordVariable.expr);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        lookupNode(bLangRecordVariableDef.var);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        lookupNodes(bLangErrorVariable.annAttachments);
        lookupNode(bLangErrorVariable.typeNode);
        lookupNode(bLangErrorVariable.message);

        for (BLangErrorVariable.BLangErrorDetailEntry detail : bLangErrorVariable.detail) {
            lookupNode(detail.valueBindingPattern);
        }

        lookupNode(bLangErrorVariable.detailExpr);
        lookupNode(bLangErrorVariable.cause);
        lookupNode(bLangErrorVariable.reasonMatchConst);
        lookupNode(bLangErrorVariable.restDetail);
        lookupNode(bLangErrorVariable.expr);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        lookupNode(bLangErrorVariableDef.errorVariable);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        // Ignore incomplete worker-flush expressions
        // Ex: var a = flush;
        if (workerFlushExpr.workerIdentifier == null) {
            return;
        }
        setEnclosingNode(workerFlushExpr.workerSymbol, workerFlushExpr.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        if (setEnclosingNode(syncSendExpr.workerSymbol, syncSendExpr.workerIdentifier.pos)) {
            return;
        }

        lookupNode(syncSendExpr.expr);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        for (BLangWaitForAllExpr.BLangWaitKeyValue keyValuePair : waitForAllExpr.getKeyValuePairs()) {
            lookupNode(keyValuePair);
        }
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
        lookupNode(recordKeyValue.key);
        lookupNode(recordKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKey recordKey) {
        if (!recordKey.computedKey && setEnclosingNode(recordKey.fieldSymbol, recordKey.pos)) {
            return;
        }

        lookupNode(recordKey.expr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField) {
        lookupNode(spreadOperatorField.expr);
    }

    @Override
    public void visit(BLangMarkdownReferenceDocumentation bLangMarkdownReferenceDocumentation) {
        // ignore
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        if (waitKeyValue.keyExpr == null && setEnclosingNode(waitKeyValue.keySymbol, waitKeyValue.key.pos)) {
            return;
        }

        lookupNode(waitKeyValue.valueExpr != null ? waitKeyValue.valueExpr : waitKeyValue.keyExpr);
    }

    @Override
    public void visit(BLangXMLElementFilter xmlElementFilter) {
        setEnclosingNode(xmlElementFilter.namespaceSymbol, xmlElementFilter.nsPos);
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        lookupNode(xmlElementAccess.expr);
        lookupNodes(xmlElementAccess.filters);
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        lookupNode(xmlNavigation.expr);
        lookupNode(xmlNavigation.childIndex);
        lookupNodes(xmlNavigation.filters);
    }

    @Override
    public void visit(BLangInvocation.BLangResourceAccessInvocation resourceAccessInvocation) {
        lookupNodes(resourceAccessInvocation.annAttachments);
        lookupNode(resourceAccessInvocation.resourceAccessPathSegments); //TODO: Need symbols for path segments (#37604)
        lookupNodes(resourceAccessInvocation.requiredArgs);
        lookupNodes(resourceAccessInvocation.restArgs);
        lookupNode(resourceAccessInvocation.expr);

        // The assumption is that if it's moduled-qualified, it must be a public symbol.
        // Hence, the owner would be a package symbol.
        if (resourceAccessInvocation.symbol != null &&
                setEnclosingNode(resourceAccessInvocation.symbol.owner, resourceAccessInvocation.pkgAlias.pos)) {
            return;
        }

        setEnclosingNode(resourceAccessInvocation.symbol, resourceAccessInvocation.resourceAccessPathSegments.pos);
    }

    @Override
    public void visit(BLangRegExpTemplateLiteral regExpTemplateLiteral) {
        lookupNode(regExpTemplateLiteral.reDisjunction);
    }

    @Override
    public void visit(BLangReSequence reSequence) {
        lookupNodes(reSequence.termList);
    }

    @Override
    public void visit(BLangReAtomQuantifier reAtomQuantifier) {
        lookupNode(reAtomQuantifier.atom);
        lookupNode(reAtomQuantifier.quantifier);
    }

    @Override
    public void visit(BLangReAtomCharOrEscape reAtomCharOrEscape) {
        lookupNode(reAtomCharOrEscape.charOrEscape);
    }

    @Override
    public void visit(BLangReQuantifier reQuantifier) {
        lookupNode(reQuantifier.quantifier);
        lookupNode(reQuantifier.nonGreedyChar);
    }

    @Override
    public void visit(BLangReCharacterClass reCharacterClass) {
        lookupNode(reCharacterClass.characterClassStart);
        lookupNode(reCharacterClass.negation);
        lookupNode(reCharacterClass.charSet);
        lookupNode(reCharacterClass.characterClassEnd);
    }

    @Override
    public void visit(BLangReCharSet reCharSet) {
        lookupNodes(reCharSet.charSetAtoms);
    }

    @Override
    public void visit(BLangReCharSetRange reCharSetRange) {
        lookupNode(reCharSetRange.lhsCharSetAtom);
        lookupNode(reCharSetRange.dash);
        lookupNode(reCharSetRange.rhsCharSetAtom);
    }

    @Override
    public void visit(BLangReAssertion reAssertion) {
        lookupNode(reAssertion.assertion);
    }

    @Override
    public void visit(BLangReCapturingGroups reCapturingGroups) {
        lookupNode(reCapturingGroups.openParen);
        lookupNode(reCapturingGroups.flagExpr);
        lookupNode(reCapturingGroups.disjunction);
        lookupNode(reCapturingGroups.closeParen);
    }

    @Override
    public void visit(BLangReDisjunction reDisjunction) {
        lookupNodes(reDisjunction.sequenceList);
    }

    @Override
    public void visit(BLangReFlagsOnOff reFlagsOnOff) {
        lookupNode(reFlagsOnOff.flags);
    }

    @Override
    public void visit(BLangReFlagExpression reFlagExpression) {
        lookupNode(reFlagExpression.questionMark);
        lookupNode(reFlagExpression.flagsOnOff);
        lookupNode(reFlagExpression.colon);
    }

    private boolean setEnclosingNode(BSymbol symbol, Location pos) {
        if (PositionUtil.withinBlock(this.cursorPos, pos)) {
            this.symbolAtCursor = symbol;
            return true;
        }

        return false;
    }

    private boolean isClassDefForServiceOrObjectCtr(BLangClassDefinition clazz) {
        return clazz.flagSet.contains(Flag.SERVICE) && clazz.flagSet.contains(Flag.ANONYMOUS)
                || clazz.flagSet.contains(Flag.OBJECT_CTOR);
    }

    private boolean isLambdaFunction(TopLevelNode node) {
        if (node.getKind() != NodeKind.FUNCTION) {
            return false;
        }

        BLangFunction func = (BLangFunction) node;
        return func.flagSet.contains(Flag.LAMBDA);
    }

    private boolean isWithinNodeMetaData(TopLevelNode node) {
        if (node instanceof AnnotatableNode) {

            List<AnnotationAttachmentNode> nodes =
                    (List<AnnotationAttachmentNode>) ((AnnotatableNode) node).getAnnotationAttachments();

            for (AnnotationAttachmentNode annotAttachment : nodes) {
                if (PositionUtil.withinBlock(this.cursorPos, annotAttachment.getPosition())) {
                    return true;
                }
            }
        }

        if (node instanceof DocumentableNode) {
            BLangMarkdownDocumentation markdown = ((DocumentableNode) node).getMarkdownDocumentationAttachment();
            if (markdown != null) {
                LinkedList<BLangMarkdownParameterDocumentation> parameters = markdown.getParameters();
                for (BLangMarkdownParameterDocumentation parameter : parameters) {
                    if (PositionUtil.withinBlock(this.cursorPos, parameter.getPosition())) {
                        return true;
                    }
                }

            }
        }

        return false;
    }
}

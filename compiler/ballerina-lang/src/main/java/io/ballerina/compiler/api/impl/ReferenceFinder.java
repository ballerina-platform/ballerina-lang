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
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
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
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAssertion;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAtomCharOrEscape;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReAtomQuantifier;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCapturingGroups;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangReCharSet;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangClientDeclarationStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * Given a particular AST node and a symbol, this will find all the references of that symbol within the given AST
 * node.
 *
 * @since 2.0.0
 */
public class ReferenceFinder extends BaseVisitor {

    private final boolean withDefinition;
    private List<Location> referenceLocations;
    private BSymbol targetSymbol;

    public ReferenceFinder(boolean withDefinition) {
        this.withDefinition = withDefinition;
    }

    public List<Location> findReferences(BLangNode node, BSymbol symbol) {
        this.referenceLocations = new ArrayList<>();
        this.targetSymbol = symbol;
        find(node);
        return this.referenceLocations;
    }

    void find(BLangNode node) {
        if (node == null) {
            return;
        }

        node.accept(this);
    }

    void find(List<? extends BLangNode> nodes) {
        for (BLangNode node : nodes) {
            find(node);
        }
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        find(pkgNode.imports);
        find(pkgNode.xmlnsList);
        find(pkgNode.constants);
        find(pkgNode.globalVars);
        find(pkgNode.services);
        find(pkgNode.annotations);
        find(pkgNode.typeDefinitions);
        find(pkgNode.classDefinitions.stream()
                     .filter(c -> !isGeneratedClassDefForService(c))
                     .collect(Collectors.toList()));
        find(pkgNode.functions.stream()
                     .filter(f -> !f.flagSet.contains(Flag.LAMBDA))
                     .collect(Collectors.toList()));

        if (!(pkgNode instanceof BLangTestablePackage)) {
            find(pkgNode.getTestablePkg());
        }
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        if (importPkgNode.symbol != null
                && this.targetSymbol.name.equals(importPkgNode.symbol.name)
                && this.targetSymbol.pkgID.equals(importPkgNode.symbol.pkgID)
                && this.targetSymbol.pos.equals(importPkgNode.symbol.pos)
                && this.withDefinition) {
            this.referenceLocations.add(importPkgNode.alias.pos);
        }
    }

    @Override
    public void visit(BLangCompilationUnit unit) {
        unit.getTopLevelNodes().forEach(topLevelNode -> find((BLangNode) topLevelNode));
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        find(xmlnsNode.namespaceURI);
        addIfSameSymbol(xmlnsNode.symbol, xmlnsNode.prefix.pos);
    }

    @Override
    public void visit(BLangClientDeclarationStatement clientDeclStmt) {
        find(clientDeclStmt.getClientDeclaration());
    }

    @Override
    public void visit(BLangClientDeclaration clientDeclNode) {
        find((BLangNode) clientDeclNode.getUri());
        addIfSameSymbol(clientDeclNode.symbol, clientDeclNode.prefix.pos);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        find(funcNode.annAttachments);
        find(funcNode.requiredParams);
        find(funcNode.restParam);
        find(funcNode.returnTypeAnnAttachments);
        find(funcNode.returnTypeNode);
        find(funcNode.body);

        if (funcNode.symbol.origin != VIRTUAL) {
            addIfSameSymbol(funcNode.symbol, funcNode.name.pos);
        }
    }

    @Override
    public void visit(BLangResourceFunction resourceFunction) {
        visit((BLangFunction) resourceFunction);
    }

    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        for (BLangStatement stmt : blockFuncBody.stmts) {
            find(stmt);
        }
    }

    @Override
    public void visit(BLangExprFunctionBody exprFuncBody) {
        find(exprFuncBody.expr);
    }

    @Override
    public void visit(BLangExternalFunctionBody externFuncBody) {
        find(externFuncBody.annAttachments);
    }

    @Override
    public void visit(BLangService serviceNode) {
        find(serviceNode.annAttachments);
        find(serviceNode.serviceClass);
        find(serviceNode.attachedExprs);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        // We don't want to visit enum type defs as they will be covered under enum constants
        if (!typeDefinition.flagSet.contains(Flag.ENUM)) {
            find(typeDefinition.typeNode);
        }
        find(typeDefinition.annAttachments);
        addIfSameSymbol(typeDefinition.symbol, typeDefinition.name.pos);
    }

    @Override
    public void visit(BLangConstant constant) {
        find(constant.typeNode);
        find(constant.expr);
        addIfSameSymbol(constant.symbol, constant.name.pos);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        find(varNode.annAttachments);
        find(varNode.typeNode);
        find(varNode.expr);
        addIfSameSymbol(varNode.symbol, varNode.name.pos);
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        find(annotationNode.annAttachments);
        find(annotationNode.typeNode);
        addIfSameSymbol(annotationNode.symbol, annotationNode.name.pos);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        find(annAttachmentNode.expr);

        if (!annAttachmentNode.pkgAlias.value.isEmpty()
                && annAttachmentNode.annotationSymbol != null
                && addIfSameSymbol(annAttachmentNode.annotationSymbol.owner, annAttachmentNode.pkgAlias.pos)) {
            return;
        }

        addIfSameSymbol(annAttachmentNode.annotationSymbol, annAttachmentNode.annotationName.pos);
    }

    @Override
    public void visit(BLangTableKeySpecifier tableKeySpecifierNode) {
        // TODO: create an issue for missing symbol info for the key specifiers
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint tableKeyTypeConstraint) {
        find(tableKeyTypeConstraint.keyType);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        for (BLangStatement stmt : blockNode.stmts) {
            find(stmt);
        }
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        find(varDefNode.var);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        find(assignNode.expr);
        find(assignNode.varRef);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        find(compoundAssignNode.expr);
        find(compoundAssignNode.varRef);
    }

    @Override
    public void visit(BLangRetry retryNode) {
        find(retryNode.retrySpec);
        find(retryNode.retryBody);
        find(retryNode.onFailClause);
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        find(retryTransaction.retrySpec);
        find(retryTransaction.transaction);
    }

    @Override
    public void visit(BLangRetrySpec retrySpec) {
        find(retrySpec.argExprs);
        find(retrySpec.retryManagerType);
    }

    @Override
    public void visit(BLangReturn returnNode) {
        find(returnNode.expr);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        find(panicNode.expr);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        find(xmlnsStmtNode.xmlnsDecl);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        find(exprStmtNode.expr);
    }

    @Override
    public void visit(BLangIf ifNode) {
        find(ifNode.expr);
        find(ifNode.body);
        find(ifNode.elseStmt);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        find(queryAction.doClause);
        find(queryAction.queryClauseList);
    }

    @Override
    public void visit(BLangMatchStatement matchStatementNode) {
        find(matchStatementNode.expr);
        find(matchStatementNode.matchClauses);
        find(matchStatementNode.onFailClause);
    }

    @Override
    public void visit(BLangMatchGuard matchGuard) {
        find(matchGuard.expr);
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern) {
        find(constMatchPattern.expr);
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern) {
        find(varBindingPattern.getBindingPattern());
    }

    @Override
    public void visit(BLangErrorMatchPattern errorMatchPattern) {
        find(errorMatchPattern.errorMessageMatchPattern);
        find(errorMatchPattern.errorTypeReference);
        find(errorMatchPattern.errorCauseMatchPattern);
        find(errorMatchPattern.errorFieldMatchPatterns);
    }

    @Override
    public void visit(BLangErrorMessageMatchPattern errorMessageMatchPattern) {
        find(errorMessageMatchPattern.simpleMatchPattern);
    }

    @Override
    public void visit(BLangErrorCauseMatchPattern errorCauseMatchPattern) {
        find(errorCauseMatchPattern.simpleMatchPattern);
        find(errorCauseMatchPattern.errorMatchPattern);
    }

    @Override
    public void visit(BLangErrorFieldMatchPatterns errorFieldMatchPatterns) {
        find(errorFieldMatchPatterns.namedArgMatchPatterns);
        find(errorFieldMatchPatterns.restMatchPattern);
    }

    @Override
    public void visit(BLangSimpleMatchPattern simpleMatchPattern) {
        find(simpleMatchPattern.varVariableName);
        find(simpleMatchPattern.constPattern);
    }

    @Override
    public void visit(BLangNamedArgMatchPattern namedArgMatchPattern) {
        find(namedArgMatchPattern.matchPattern);
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern) {
        addIfSameSymbol(captureBindingPattern.symbol, captureBindingPattern.getIdentifier().getPosition());
    }

    @Override
    public void visit(BLangListBindingPattern listBindingPattern) {
        find(listBindingPattern.bindingPatterns);
        find(listBindingPattern.restBindingPattern);
    }

    @Override
    public void visit(BLangMappingBindingPattern mappingBindingPattern) {
        find(mappingBindingPattern.fieldBindingPatterns);
        find(mappingBindingPattern.restBindingPattern);
    }

    @Override
    public void visit(BLangFieldBindingPattern fieldBindingPattern) {
        find(fieldBindingPattern.bindingPattern);
    }

    @Override
    public void visit(BLangRestBindingPattern restBindingPattern) {
        addIfSameSymbol(restBindingPattern.symbol, restBindingPattern.getIdentifier().getPosition());
    }

    @Override
    public void visit(BLangErrorBindingPattern errorBindingPattern) {
        find(errorBindingPattern.errorMessageBindingPattern);
        find(errorBindingPattern.errorTypeReference);
        find(errorBindingPattern.errorCauseBindingPattern);
        find(errorBindingPattern.errorFieldBindingPatterns);
    }

    @Override
    public void visit(BLangErrorMessageBindingPattern errorMessageBindingPattern) {
        find(errorMessageBindingPattern.simpleBindingPattern);
    }

    @Override
    public void visit(BLangErrorCauseBindingPattern errorCauseBindingPattern) {
        find(errorCauseBindingPattern.simpleBindingPattern);
        find(errorCauseBindingPattern.errorBindingPattern);
    }

    @Override
    public void visit(BLangErrorFieldBindingPatterns errorFieldBindingPatterns) {
        find(errorFieldBindingPatterns.namedArgBindingPatterns);
        find(errorFieldBindingPatterns.restBindingPattern);
    }

    @Override
    public void visit(BLangSimpleBindingPattern simpleBindingPattern) {
        find(simpleBindingPattern.captureBindingPattern);
    }

    @Override
    public void visit(BLangNamedArgBindingPattern namedArgBindingPattern) {
        find(namedArgBindingPattern.bindingPattern);
    }

    @Override
    public void visit(BLangForeach foreach) {
        find((BLangNode) foreach.variableDefinitionNode);
        find(foreach.collection);
        find(foreach.body);
        find(foreach.onFailClause);
    }

    @Override
    public void visit(BLangDo doNode) {
        find(doNode.body);
        find(doNode.onFailClause);
    }

    @Override
    public void visit(BLangFail failNode) {
        find(failNode.expr);
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        find((BLangNode) fromClause.variableDefinitionNode);
        find(fromClause.collection);
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        find((BLangNode) joinClause.variableDefinitionNode);
        find((BLangOnClause) joinClause.onClause);
        find(joinClause.collection);
    }

    @Override
    public void visit(BLangLetClause letClause) {
        for (BLangLetVariable letVariable : letClause.letVarDeclarations) {
            find((BLangNode) letVariable.definitionNode);
        }
    }

    @Override
    public void visit(BLangOnClause onClause) {
        find(onClause.lhsExpr);
        find(onClause.rhsExpr);
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause) {
        find(orderKeyClause.expression);
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        for (OrderKeyNode orderKeyNode : orderByClause.orderByKeyList) {
            find((BLangOrderKey) orderKeyNode);
        }
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        find(selectClause.expression);
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        find(whereClause.expression);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        find(doClause.body);
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        find((BLangNode) onFailClause.variableDefinitionNode);
        find(onFailClause.body);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        find(onConflictClause.expression);
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        find(limitClause.expression);
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
        find(matchClause.matchPatterns);
        find(matchClause.matchGuard);
        find(matchClause.blockStmt);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        find(whileNode.expr);
        find(whileNode.body);
        find(whileNode.onFailClause);
    }

    @Override
    public void visit(BLangLock lockNode) {
        find(lockNode.body);
        find(lockNode.onFailClause);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        find(transactionNode.transactionBody);
        find(transactionNode.onFailClause);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        find(stmt.expr);
        find(stmt.varRef);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        find(stmt.expr);
        find(stmt.varRef);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        find(stmt.expr);
        find(stmt.varRef);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        find(forkJoin.workers);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        find(workerSendNode.expr);
        addIfSameSymbol(workerSendNode.workerSymbol, workerSendNode.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        addIfSameSymbol(workerReceiveNode.workerSymbol, workerReceiveNode.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        find(rollbackNode.expr);
    }

    @Override
    public void visit(BLangConstRef constRef) {
        if (!constRef.pkgAlias.value.isEmpty()) {
            addIfSameSymbol(constRef.symbol.owner, constRef.pkgAlias.pos);
        }
        addIfSameSymbol(constRef.symbol, constRef.variableName.pos);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            find((BLangNode) field);
        }
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        find(varRefExpr.expressions);
        find((BLangNode) varRefExpr.restParam);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        for (BLangRecordVarRef.BLangRecordVarRefKeyValue recordRefField : varRefExpr.recordRefFields) {
            find(recordRefField.getBindingPattern());
        }

        find((BLangNode) varRefExpr.restParam);
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        find(varRefExpr.typeNode);
        find(varRefExpr.message);
        find(varRefExpr.cause);
        find(varRefExpr.restVar);

        if (varRefExpr.typeNode != null) {
            find(varRefExpr.detail);
        } else {
            // This is to avoid cases where the binding pattern doesn't have a error type ref
            // e.g., error (msg, cause, code=code)
            visitNamedArgWithoutAddingSymbol(varRefExpr.detail);
        }
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        // Becomes null for fields in a record literal.
        if (varRefExpr.symbol == null) {
            return;
        }

        if (varRefExpr.pkgAlias != null && !varRefExpr.pkgAlias.value.isEmpty() &&
                addIfSameSymbol(varRefExpr.symbol.owner, varRefExpr.pkgAlias.pos)) {
            return;
        }

        addIfSameSymbol(varRefExpr.symbol, varRefExpr.variableName.pos);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        find(fieldAccessExpr.expr);
        addIfSameSymbol(fieldAccessExpr.symbol, fieldAccessExpr.field.pos);
    }

    @Override
    public void visit(BLangNSPrefixedFieldBasedAccess nsPrefixedFieldBasedAccess) {
        find(nsPrefixedFieldBasedAccess.expr);
        addIfSameSymbol(nsPrefixedFieldBasedAccess.nsSymbol, nsPrefixedFieldBasedAccess.nsPrefix.pos);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        find(indexAccessExpr.expr);

        if (indexAccessExpr.indexExpr instanceof BLangLiteral) {
            addIfSameSymbol(indexAccessExpr.symbol, getLocationForLiteral(indexAccessExpr.indexExpr.pos));
        } else {
            find(indexAccessExpr.indexExpr);
        }
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        // Skipping lang libs because in lang lib function calls, the expr gets added as an argument.
        if (!invocationExpr.langLibInvocation) {
            find(invocationExpr.expr);
        }

        find(invocationExpr.annAttachments);
        find(invocationExpr.argExprs);

        if (!invocationExpr.pkgAlias.value.isEmpty() && invocationExpr.symbol != null) {
            addIfSameSymbol(invocationExpr.symbol.owner, invocationExpr.pkgAlias.pos);
        }
        addIfSameSymbol(invocationExpr.symbol, invocationExpr.name.pos);
    }

    @Override
    public void visit(BLangTypeInit typeInit) {
        find(typeInit.userDefinedType);
        find(typeInit.argsExpr);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        find(actionInvocationExpr.expr);
        find(actionInvocationExpr.requiredArgs);
        find(actionInvocationExpr.annAttachments);
        find(actionInvocationExpr.restArgs);

        if (!actionInvocationExpr.pkgAlias.value.isEmpty()) {
            addIfSameSymbol(actionInvocationExpr.symbol.owner, actionInvocationExpr.pkgAlias.pos);
        }
        addIfSameSymbol(actionInvocationExpr.symbol, actionInvocationExpr.name.pos);
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        find(ternaryExpr.expr);
        find(ternaryExpr.thenExpr);
        find(ternaryExpr.elseExpr);
    }

    @Override
    public void visit(BLangWaitExpr waitExpr) {
        find(waitExpr.exprList);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        find(trapExpr.expr);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        find(binaryExpr.lhsExpr);
        find(binaryExpr.rhsExpr);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        find(elvisExpr.lhsExpr);
        find(elvisExpr.rhsExpr);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        find(groupExpr.expression);
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        for (BLangLetVariable letVarDeclaration : letExpr.letVarDeclarations) {
            find((BLangNode) letVarDeclaration.definitionNode);
        }

        find(letExpr.expr);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        find(listConstructorExpr.exprs);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangListConstructorSpreadOpExpr spreadOpExpr) {
        find(spreadOpExpr.expr);
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        find(tableConstructorExpr.recordLiteralList);
        find(tableConstructorExpr.tableKeySpecifier);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        find(unaryExpr.expr);
    }

    @Override
    public void visit(BLangTypedescExpr typedescExpr) {
        find(typedescExpr.typeNode);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        find(conversionExpr.annAttachments);
        find(conversionExpr.typeNode);
        find(conversionExpr.expr);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        addIfSameSymbol(xmlQName.nsSymbol, xmlQName.prefix.pos);
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        find(xmlAttribute.name);
        find(xmlAttribute.value);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        find(xmlElementLiteral.startTagName);
        find(xmlElementLiteral.endTagName);
        find(xmlElementLiteral.children);
        find(xmlElementLiteral.attributes);
        find(xmlElementLiteral.inlineNamespaces);
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        find(xmlTextLiteral.textFragments);
        find(xmlTextLiteral.concatExpr);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        find(xmlCommentLiteral.textFragments);
        find(xmlCommentLiteral.concatExpr);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        find(xmlProcInsLiteral.target);
        find(xmlProcInsLiteral.dataFragments);
        find(xmlProcInsLiteral.dataConcatExpr);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        find(xmlQuotedString.textFragments);
        find(xmlQuotedString.concatExpr);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        find(stringTemplateLiteral.exprs);
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        find(rawTemplateLiteral.insertions);
        find(rawTemplateLiteral.strings);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        find(bLangLambdaFunction.function);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        find(bLangArrowFunction.params);
        find(bLangArrowFunction.body);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        find(bLangVarArgsExpression.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        find(bLangNamedArgsExpression.expr);
        addIfSameSymbol(bLangNamedArgsExpression.varSymbol, bLangNamedArgsExpression.name.pos);
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        find(assignableExpr.lhsExpr);
        find(assignableExpr.typeNode);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        find(checkedExpr.expr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        find(checkPanickedExpr.expr);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        find(serviceConstructorExpr.serviceNode);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        find(typeTestExpr.expr);
        find(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        find(typeTestExpr.expr);
        find(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        find(annotAccessExpr.expr);
        addIfSameSymbol(annotAccessExpr.annotationSymbol, annotAccessExpr.annotationName.pos);
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        find(queryExpr.queryClauseList);
    }

    @Override
    public void visit(BLangObjectConstructorExpression objConstructor) {
        find(objConstructor.classNode);
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        find(arrayType.elemtype);
        for (BLangExpression size : arrayType.sizes) {
            find(size);
        }
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        find(constrainedType.type);
        find(constrainedType.constraint);
    }

    @Override
    public void visit(BLangStreamType streamType) {
        find(streamType.constraint);
        find(streamType.error);
    }

    @Override
    public void visit(BLangTableTypeNode tableType) {
        find(tableType.constraint);
        find(tableType.tableKeySpecifier);
        find(tableType.tableKeyTypeConstraint);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        if (userDefinedType.symbol == null) {
            return;
        }
        if (!userDefinedType.pkgAlias.value.isEmpty()) {
            addIfSameSymbol(userDefinedType.symbol.owner, userDefinedType.pkgAlias.pos);
        }
        addIfSameSymbol(userDefinedType.symbol, userDefinedType.typeName.pos);
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        find(functionTypeNode.params);
        find(functionTypeNode.restParam);
        find(functionTypeNode.returnTypeNode);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        find(unionTypeNode.memberTypeNodes);
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        find(intersectionTypeNode.constituentTypeNodes);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        find(objectTypeNode.fields);
        find(objectTypeNode.functions);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        find(recordTypeNode.typeRefs);
        find(recordTypeNode.fields);
        find(recordTypeNode.restFieldType);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        find(finiteTypeNode.valueSpace);
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        find(tupleTypeNode.memberTypeNodes);
        find(tupleTypeNode.restParamType);
    }

    @Override
    public void visit(BLangErrorType errorType) {
        find(errorType.detailType);
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        find(errorConstructorExpr.errorTypeRef);
        find(errorConstructorExpr.positionalArgs);
        find(errorConstructorExpr.namedArgs);
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        find(bLangXMLSequenceLiteral.xmlItems);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        find(bLangTupleVariable.annAttachments);
        find(bLangTupleVariable.typeNode);
        find(bLangTupleVariable.memberVariables);
        find(bLangTupleVariable.restVariable);
        find(bLangTupleVariable.expr);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        find(bLangTupleVariableDef.var);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        find(bLangRecordVariable.annAttachments);
        find(bLangRecordVariable.typeNode);

        for (BLangRecordVariable.BLangRecordVariableKeyValue variableKeyValue : bLangRecordVariable.variableList) {
            find(variableKeyValue.valueBindingPattern);
        }

        find(bLangRecordVariable.expr);
        find(bLangRecordVariable.restParam);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        find(bLangRecordVariableDef.var);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        find(bLangErrorVariable.annAttachments);
        find(bLangErrorVariable.typeNode);
        find(bLangErrorVariable.message);
        find(bLangErrorVariable.restDetail);
        find(bLangErrorVariable.cause);
        find(bLangErrorVariable.reasonMatchConst);
        find(bLangErrorVariable.expr);

        for (BLangErrorVariable.BLangErrorDetailEntry errorDetailEntry : bLangErrorVariable.detail) {
            find(errorDetailEntry.valueBindingPattern);
            addIfSameSymbol(errorDetailEntry.keySymbol, errorDetailEntry.key.pos);
        }
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        find(bLangErrorVariableDef.errorVariable);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        // Ignore incomplete worker-flush expressions
        // Ex: var a = flush;
        if (workerFlushExpr.workerIdentifier == null) {
            return;
        }
        addIfSameSymbol(workerFlushExpr.workerSymbol, workerFlushExpr.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        find(syncSendExpr.expr);
        addIfSameSymbol(syncSendExpr.workerSymbol, syncSendExpr.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        find(waitForAllExpr.keyValuePairs);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
        find(recordKeyValue.key);
        find(recordKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKey recordKey) {
        find(recordKey.expr);
        addIfSameSymbol(recordKey.fieldSymbol, recordKey.pos);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField) {
        find(spreadOperatorField.expr);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        find(waitKeyValue.keyExpr);
        find(waitKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangXMLElementFilter xmlElementFilter) {
        addIfSameSymbol(xmlElementFilter.namespaceSymbol, xmlElementFilter.nsPos);
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        find(xmlElementAccess.expr);
        find(xmlElementAccess.filters);
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        find(xmlNavigation.childIndex);
        find(xmlNavigation.filters);
        find(xmlNavigation.expr);
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        find(classDefinition.annAttachments);
        find(classDefinition.fields);
        find(classDefinition.initFunction);
        find(classDefinition.functions);
        find(classDefinition.typeRefs);
        addIfSameSymbol(classDefinition.symbol, classDefinition.name.pos);
    }

    @Override
    public void visit(BLangListMatchPattern listMatchPattern) {
        find(listMatchPattern.matchPatterns);
        find(listMatchPattern.restMatchPattern);
    }

    @Override
    public void visit(BLangMappingMatchPattern mappingMatchPattern) {
        find(mappingMatchPattern.fieldMatchPatterns);
        find(mappingMatchPattern.restMatchPattern);
    }

    @Override
    public void visit(BLangFieldMatchPattern fieldMatchPattern) {
        find(fieldMatchPattern.matchPattern);
    }

    @Override
    public void visit(BLangRestMatchPattern restMatchPattern) {
        addIfSameSymbol(restMatchPattern.symbol, restMatchPattern.variableName.pos);
    }

    @Override
    public void visit(BLangInvocation.BLangResourceAccessInvocation resourceAccessInvocation) {
        find(resourceAccessInvocation.expr);
        find(resourceAccessInvocation.requiredArgs);
        find(resourceAccessInvocation.annAttachments);
        find(resourceAccessInvocation.restArgs);
        find(resourceAccessInvocation.resourceAccessPathSegments);

        if (!resourceAccessInvocation.pkgAlias.value.isEmpty()) {
            addIfSameSymbol(resourceAccessInvocation.symbol.owner, resourceAccessInvocation.pkgAlias.pos);
        }
        addIfSameSymbol(resourceAccessInvocation.symbol, resourceAccessInvocation.resourceAccessPathSegments.pos);
    }

    @Override
    public void visit(BLangRegExpTemplateLiteral regExpTemplateLiteral) {
        find(regExpTemplateLiteral.reDisjunction);
    }

    @Override
    public void visit(BLangReSequence reSequence) {
        find(reSequence.termList);
    }

    @Override
    public void visit(BLangReAtomQuantifier reAtomQuantifier) {
        find(reAtomQuantifier.atom);
        find(reAtomQuantifier.quantifier);
    }

    @Override
    public void visit(BLangReAtomCharOrEscape reAtomCharOrEscape) {
        find(reAtomCharOrEscape.charOrEscape);
    }

    @Override
    public void visit(BLangReQuantifier reQuantifier) {
        find(reQuantifier.quantifier);
        find(reQuantifier.nonGreedyChar);
    }

    @Override
    public void visit(BLangReCharacterClass reCharacterClass) {
        find(reCharacterClass.characterClassStart);
        find(reCharacterClass.negation);
        find(reCharacterClass.charSet);
        find(reCharacterClass.characterClassEnd);
    }

    @Override
    public void visit(BLangReCharSet reCharSet) {
        find(reCharSet.charSetAtoms);
    }

    @Override
    public void visit(BLangReAssertion reAssertion) {
        find(reAssertion.assertion);
    }

    @Override
    public void visit(BLangReCapturingGroups reCapturingGroups) {
        find(reCapturingGroups.openParen);
        find(reCapturingGroups.flagExpr);
        find(reCapturingGroups.disjunction);
        find(reCapturingGroups.closeParen);
    }

    @Override
    public void visit(BLangReDisjunction reDisjunction) {
        find(reDisjunction.sequenceList);
    }

    @Override
    public void visit(BLangReFlagsOnOff reFlagsOnOff) {
        find(reFlagsOnOff.flags);
    }

    @Override
    public void visit(BLangReFlagExpression reFlagExpression) {
        find(reFlagExpression.questionMark);
        find(reFlagExpression.flagsOnOff);
        find(reFlagExpression.colon);
    }

    // Private methods

    private void visitNamedArgWithoutAddingSymbol(List<BLangNamedArgsExpression> args) {
        for (BLangNamedArgsExpression arg : args) {
            find(arg.expr);
        }
    }

    private boolean addIfSameSymbol(BSymbol symbol, Location location) {
        if (symbol != null
                && this.targetSymbol.name.equals(symbol.name)
                && this.targetSymbol.pkgID.equals(symbol.pkgID)
                && this.targetSymbol.pos.equals(symbol.pos)
                && (this.withDefinition || !symbol.pos.equals(location))) {
            this.referenceLocations.add(location);
            return true;
        }
        return false;
    }

    private boolean isGeneratedClassDefForService(BLangClassDefinition clazz) {
        return clazz.flagSet.contains(Flag.ANONYMOUS) && clazz.flagSet.contains(Flag.SERVICE);
    }

    /**
     * This method is intended to be used for getting the location of a string value with the surrounding quotes
     * disregarded. If we give the original location, it'd be problematic for use cases such as renaming since we only
     * return a list of locations of references. Without further contextual info, it'll be hard to determine whether a
     * particular reference location is a string value.
     *
     * @param location Location of the string
     * @return The modified location with the quotes diregarded
     */
    private Location getLocationForLiteral(Location location) {
        LineRange lineRange = location.lineRange();
        return new BLangDiagnosticLocation(lineRange.filePath(),
                                           lineRange.startLine().line(), lineRange.endLine().line(),
                                           lineRange.startLine().offset() + 1, lineRange.endLine().offset() - 1,
                                           location.textRange().startOffset(), location.textRange().length());
    }
}

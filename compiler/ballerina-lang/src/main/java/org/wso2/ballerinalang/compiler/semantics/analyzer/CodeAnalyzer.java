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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.identifier.Utils;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.expressions.XMLNavigationAccess;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.ballerinalang.util.diagnostic.DiagnosticHintCode;
import org.ballerinalang.util.diagnostic.DiagnosticWarningCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.SimpleBLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangCaptureBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorCauseBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorFieldBindingPatterns;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorMessageBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangFieldBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangMappingBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangNamedArgBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangSimpleBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangWildCardBindingPattern;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInferredTypedescDefaultNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangListConstructorSpreadOpExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRegExpTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangValueExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
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
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangNamedArgMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangSimpleMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangVarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangWildCardMatchPattern;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Constants;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeDefBuilderHelper;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.ballerinalang.model.tree.NodeKind.LITERAL;
import static org.ballerinalang.util.BLangCompilerConstants.RETRY_MANAGER_OBJECT_SHOULD_RETRY_FUNC;
import static org.wso2.ballerinalang.compiler.tree.BLangInvokableNode.DEFAULT_WORKER_NAME;
import static org.wso2.ballerinalang.compiler.util.Constants.MAIN_FUNCTION_NAME;
import static org.wso2.ballerinalang.compiler.util.Constants.WORKER_LAMBDA_VAR_PREFIX;

/**
 * This represents the code analyzing pass of semantic analysis.
 * <p>
 * The following validations are done here:-
 * <p>
 * (*) Loop continuation statement validation.
 * (*) Function return path existence and unreachable code validation.
 * (*) Worker send/receive validation.
 * (*) Experimental feature usage.
 */
public class CodeAnalyzer extends SimpleBLangNodeAnalyzer<CodeAnalyzer.AnalyzerData> {

    private static final CompilerContext.Key<CodeAnalyzer> CODE_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private final SymbolResolver symResolver;
    private final SymbolTable symTable;
    private final Types types;
    private final BLangDiagnosticLog dlog;
    private final TypeChecker typeChecker;
    private final Names names;
    private final ReachabilityAnalyzer reachabilityAnalyzer;

    public static CodeAnalyzer getInstance(CompilerContext context) {
        CodeAnalyzer codeGenerator = context.get(CODE_ANALYZER_KEY);
        if (codeGenerator == null) {
            codeGenerator = new CodeAnalyzer(context);
        }
        return codeGenerator;
    }

    public CodeAnalyzer(CompilerContext context) {
        context.put(CODE_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.reachabilityAnalyzer = ReachabilityAnalyzer.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        final AnalyzerData data = new AnalyzerData();
        visitNode(pkgNode, data);
        return pkgNode;
    }

    @Override
    public void visit(BLangPackage pkgNode, AnalyzerData data) {
        this.dlog.setCurrentPackageId(pkgNode.packageID);
        if (pkgNode.completedPhases.contains(CompilerPhase.CODE_ANALYZE)) {
            return;
        }
        data.parent = pkgNode;
        data.env = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        analyzeTopLevelNodes(pkgNode, data);
        pkgNode.getTestablePkgs().forEach(testablePackage -> visitNode(testablePackage, data));
    }

    @Override
    public void visit(BLangTestablePackage node, AnalyzerData data) {
        visit((BLangPackage) node, data);
    }

    private void analyzeTopLevelNodes(BLangPackage pkgNode, AnalyzerData data) {
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;
        for (int i = 0; i < topLevelNodes.size(); i++) {
            analyzeNode((BLangNode) topLevelNodes.get(i), data);
        }
        pkgNode.completedPhases.add(CompilerPhase.CODE_ANALYZE);
    }

    @Override
    public void analyzeNode(BLangNode node, AnalyzerData data) {
        SymbolEnv prevEnv = data.env;
        BLangNode parent = data.parent;
        node.parent = parent;
        data.parent = node;
        visitNode(node, data);
        data.parent = parent;
        data.env = prevEnv;
    }

    private void analyzeTypeNode(BLangType node, AnalyzerData data) {

        if (node == null) {
            return;
        }
        analyzeNode(node, data);
    }

    @Override
    public void visit(BLangCompilationUnit compUnitNode, AnalyzerData data) {
        compUnitNode.topLevelNodes.forEach(e -> analyzeNode((BLangNode) e, data));
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition, AnalyzerData data) {

        analyzeTypeNode(typeDefinition.typeNode, data);
        typeDefinition.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, data));
    }

    @Override
    public void visit(BLangClassDefinition classDefinition, AnalyzerData data) {
        data.env = SymbolEnv.createClassEnv(classDefinition, classDefinition.symbol.scope, data.env);
        for (BLangSimpleVariable field : classDefinition.fields) {
            DefaultValueState prevDefaultValueState = data.defaultValueState;
            data.defaultValueState = DefaultValueState.OBJECT_FIELD_INITIALIZER;
            analyzeNode(field, data);
            data.defaultValueState = prevDefaultValueState;
        }

        List<BLangFunction> bLangFunctionList = new ArrayList<>(classDefinition.functions);
        if (classDefinition.initFunction != null) {
            bLangFunctionList.add(classDefinition.initFunction);
        }

        // To ensure the order of the compile errors
        bLangFunctionList.sort(Comparator.comparingInt(function -> function.pos.lineRange().startLine().line()));
        for (BLangFunction function : bLangFunctionList) {
            analyzeNode(function, data);
        }

        classDefinition.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, data));
    }

    @Override
    public void visit(BLangObjectConstructorExpression objectConstructorExpression, AnalyzerData data) {
        visit(objectConstructorExpression.typeInit, data);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef, AnalyzerData data) {

        analyzeNode(bLangTupleVariableDef.var, data);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef, AnalyzerData data) {

        analyzeNode(bLangRecordVariableDef.var, data);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef, AnalyzerData data) {

        analyzeNode(bLangErrorVariableDef.errorVariable, data);
    }

    @Override
    public void visit(BLangResourceFunction funcNode, AnalyzerData data) {
        visit((BLangFunction) funcNode, data);
    }

    @Override
    public void visit(BLangFunction funcNode, AnalyzerData data) {
        validateParams(funcNode, data);
        analyzeNode(funcNode.returnTypeNode, data);

        boolean isLambda = funcNode.flagSet.contains(Flag.LAMBDA);
        if (isLambda) {
            return;
        }

        if (Symbols.isPublic(funcNode.symbol) && !isMethodInServiceDeclaration(funcNode)) {
            funcNode.symbol.params.forEach(symbol -> analyzeExportableTypeRef(funcNode.symbol, symbol.type.tsymbol,
                                                                              true,
                                                                              funcNode.pos));
            if (funcNode.symbol.restParam != null) {
                analyzeExportableTypeRef(funcNode.symbol, funcNode.symbol.restParam.type.tsymbol, true,
                                         funcNode.restParam.pos);
            }
            analyzeExportableTypeRef(funcNode.symbol, funcNode.symbol.retType.tsymbol, true,
                                     funcNode.returnTypeNode.pos);
        }
        if (MAIN_FUNCTION_NAME.equals(funcNode.name.value)) {
            new MainFunctionValidator(types, dlog).validateMainFunction(funcNode);
        }
        this.validateModuleInitFunction(funcNode);
        try {
            this.initNewWorkerActionSystem(data);
            data.workerActionSystemStack.peek().startWorkerActionStateMachine(DEFAULT_WORKER_NAME,
                                                                              funcNode.pos,
                                                                              funcNode);
            this.visitFunction(funcNode, data);
            data.workerActionSystemStack.peek().endWorkerActionStateMachine();
        } finally {
            this.finalizeCurrentWorkerActionSystem(data);
        }
        funcNode.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, data));

        validateNamedWorkerUniqueReferences(data);
    }

    private boolean isMethodInServiceDeclaration(BLangFunction func) {
        BLangNode parent = func.parent;
        return parent.getKind() == NodeKind.CLASS_DEFN &&
                Symbols.isFlagOn(((BLangClassDefinition) parent).symbol.flags, Flags.SERVICE);
    }

    private void validateNamedWorkerUniqueReferences(AnalyzerData data) {
        for (var nodes : data.workerReferences.values()) {
            if (nodes.size() > 1) {
                for (BLangNode node: nodes) {
                    dlog.error(node.pos, DiagnosticErrorCode.ILLEGAL_WORKER_REFERENCE_AS_A_VARIABLE_REFERENCE, node);
                }
            }
        }

        data.workerReferences.clear();
    }

    private void validateParams(BLangFunction funcNode, AnalyzerData data) {
        for (BLangSimpleVariable parameter : funcNode.requiredParams) {
            analyzeNode(parameter, data);
        }
        if (funcNode.restParam != null) {
            analyzeNode(funcNode.restParam, data);
        }
    }

    private void visitFunction(BLangFunction funcNode, AnalyzerData data) {
        data.env = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, data.env);
        data.returnWithinTransactionCheckStack.push(true);
        data.returnTypes.push(new LinkedHashSet<>());
        data.transactionalFuncCheckStack.push(funcNode.flagSet.contains(Flag.TRANSACTIONAL));
        if (Symbols.isNative(funcNode.symbol)) {
            return;
        }
        if (isPublicInvokableNode(funcNode)) {
            analyzeNode(funcNode.returnTypeNode, data);
        }

        /* the body can be null in the case of Object type function declarations */
        if (funcNode.body != null) {

            DefaultValueState prevDefaultValueState = data.defaultValueState;
            if (prevDefaultValueState == DefaultValueState.RECORD_FIELD_DEFAULT ||
                    prevDefaultValueState == DefaultValueState.OBJECT_FIELD_INITIALIZER) {
                data.defaultValueState = DefaultValueState.FUNCTION_IN_DEFAULT_VALUE;
            }
            analyzeNode(funcNode.body, data);
            data.defaultValueState = prevDefaultValueState;
        }
        reachabilityAnalyzer.analyzeReachability(funcNode, data.env);
        data.returnTypes.pop();
        data.returnWithinTransactionCheckStack.pop();
        data.transactionalFuncCheckStack.pop();
    }

    private boolean isPublicInvokableNode(BLangInvokableNode invNode) {
        return Symbols.isPublic(invNode.symbol) && (SymbolKind.PACKAGE.equals(invNode.symbol.owner.getKind()) ||
                Symbols.isPublic(invNode.symbol.owner));
    }

    @Override
    public void visit(BLangBlockFunctionBody body, AnalyzerData data) {
        boolean prevWithinTxScope = data.withinTransactionScope;
        boolean prevLoopAlterNotAllowed = data.loopAlterNotAllowed;
        data.loopAlterNotAllowed = data.loopCount > 0;
        if (!prevWithinTxScope) {
            data.withinTransactionScope = data.transactionalFuncCheckStack.peek();
        }
        data.env = SymbolEnv.createFuncBodyEnv(body, data.env);
        for (BLangStatement e : body.stmts) {
            data.inInternallyDefinedBlockStmt = true;
            analyzeNode(e, data);
        }
        data.inInternallyDefinedBlockStmt = false;
        if (data.transactionalFuncCheckStack.peek()) {
            data.withinTransactionScope = prevWithinTxScope;
        }
        data.loopAlterNotAllowed = prevLoopAlterNotAllowed;
    }

    @Override
    public void visit(BLangExprFunctionBody body, AnalyzerData data) {
        analyzeExpr(body.expr, data);
    }

    @Override
    public void visit(BLangExternalFunctionBody body, AnalyzerData data) {
        // do nothing
    }

    @Override
    public void visit(BLangForkJoin forkJoin, AnalyzerData data) {
        if (forkJoin.workers.isEmpty()) {
            dlog.error(forkJoin.pos, DiagnosticErrorCode.INVALID_FOR_JOIN_SYNTAX_EMPTY_FORK);
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode, AnalyzerData data) {
        //Check whether transaction statement occurred in a transactional scope
        if (data.transactionalFuncCheckStack.peek()) {
            this.dlog.error(transactionNode.pos,
                    DiagnosticErrorCode.TRANSACTION_CANNOT_BE_USED_WITHIN_TRANSACTIONAL_SCOPE);
            return;
        }
        boolean previousWithinTxScope = data.withinTransactionScope;
        int previousCommitCount = data.commitCount;
        int previousRollbackCount = data.rollbackCount;
        boolean prevCommitRollbackAllowed = data.commitRollbackAllowed;
        data.commitRollbackAllowed = true;
        data.commitCount = 0;
        data.rollbackCount = 0;
        data.withinTransactionScope = true;
        data.loopWithinTransactionCheckStack.push(false);
        data.returnWithinTransactionCheckStack.push(false);
        data.transactionCount++;
        boolean onFailExists = transactionNode.onFailClause != null;
        boolean failureHandled = data.failureHandled;
        if (onFailExists) {
            data.errorTypes.push(new LinkedHashSet<>());
            data.failureHandled = true;
        }
        analyzeNode(transactionNode.transactionBody, data);
        data.failureHandled = failureHandled;
        if (data.commitCount < 1) {
            this.dlog.error(transactionNode.pos, DiagnosticErrorCode.INVALID_COMMIT_COUNT);
        }
        data.transactionCount--;
        data.withinTransactionScope = previousWithinTxScope;
        data.commitCount = previousCommitCount;
        data.rollbackCount = previousRollbackCount;
        data.commitRollbackAllowed = prevCommitRollbackAllowed;
        data.returnWithinTransactionCheckStack.pop();
        data.loopWithinTransactionCheckStack.pop();
        analyseOnFailAndUpdateBreakMode(onFailExists, transactionNode.transactionBody,
                transactionNode.onFailClause, data);
    }

    private void analyzeOnFailClause(BLangOnFailClause onFailClause, AnalyzerData data) {
        if (onFailClause != null) {
            analyzeNode(onFailClause, data);
        }
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangCommitExpr commitExpr, AnalyzerData data) {
        data.commitCount++;
        data.commitCountWithinBlock++;
        if (data.transactionCount == 0) {
            this.dlog.error(commitExpr.pos, DiagnosticErrorCode.COMMIT_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK);
            return;
        }
        if (data.transactionalFuncCheckStack.peek()) {
            this.dlog.error(commitExpr.pos, DiagnosticErrorCode.COMMIT_CANNOT_BE_WITHIN_TRANSACTIONAL_FUNCTION);
            return;
        }
        if (!data.withinTransactionScope || !data.commitRollbackAllowed ||
                data.loopWithinTransactionCheckStack.peek()) {
            this.dlog.error(commitExpr.pos, DiagnosticErrorCode.COMMIT_NOT_ALLOWED);
            return;
        }
        data.withinTransactionScope = false;
    }

    @Override
    public void visit(BLangRollback rollbackNode, AnalyzerData data) {
        data.rollbackCount++;
        data.rollbackCountWithinBlock++;
        if (data.transactionCount == 0 && !data.withinTransactionScope) {
            this.dlog.error(rollbackNode.pos, DiagnosticErrorCode.ROLLBACK_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK);
            return;
        }
        if (!data.transactionalFuncCheckStack.empty() && data.transactionalFuncCheckStack.peek()) {
            this.dlog.error(rollbackNode.pos, DiagnosticErrorCode.ROLLBACK_CANNOT_BE_WITHIN_TRANSACTIONAL_FUNCTION);
            return;
        }
        if (!data.withinTransactionScope || !data.commitRollbackAllowed ||
                (!data.loopWithinTransactionCheckStack.empty() && data.loopWithinTransactionCheckStack.peek())) {
            this.dlog.error(rollbackNode.pos, DiagnosticErrorCode.ROLLBACK_NOT_ALLOWED);
            return;
        }
        data.withinTransactionScope = false;
        analyzeExpr(rollbackNode.expr, data);
    }

    @Override
    public void visit(BLangRetry retryNode, AnalyzerData data) {
        boolean onFailExists = retryNode.onFailClause != null;
        boolean failureHandled = data.failureHandled;
        if (onFailExists) {
            data.errorTypes.push(new LinkedHashSet<>());
            data.failureHandled = true;
        }
        visitNode(retryNode.retrySpec, data);
        visitNode(retryNode.retryBody, data);
        data.failureHandled = failureHandled;
        analyseOnFailAndUpdateBreakMode(onFailExists, retryNode.retryBody, retryNode.onFailClause, data);
    }

    @Override
    public void visit(BLangRetrySpec retrySpec, AnalyzerData data) {
        if (retrySpec.retryManagerType != null) {
            BSymbol retryManagerTypeSymbol = symTable.langErrorModuleSymbol.scope
                    .lookup(names.fromString("RetryManager")).symbol;
            BType abstractRetryManagerType = retryManagerTypeSymbol.type;
            if (!types.isAssignable(retrySpec.retryManagerType.getBType(), abstractRetryManagerType)) {
                dlog.error(retrySpec.pos, DiagnosticErrorCode.INVALID_INTERFACE_ON_NON_ABSTRACT_OBJECT,
                           RETRY_MANAGER_OBJECT_SHOULD_RETRY_FUNC, retrySpec.retryManagerType.getBType());
            }
        }
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction, AnalyzerData data) {
        analyzeNode(retryTransaction.retrySpec, data);
        analyzeNode(retryTransaction.transaction, data);
    }

    @Override
    public void visit(BLangBlockStmt blockNode, AnalyzerData data) {
        int prevCommitCount = data.commitCountWithinBlock;
        int prevRollbackCount = data.rollbackCountWithinBlock;
        data.commitCountWithinBlock = 0;
        data.rollbackCountWithinBlock = 0;
        boolean inInternallyDefinedBlockStmt = data.inInternallyDefinedBlockStmt;
        data.inInternallyDefinedBlockStmt = checkBlockIsAnInternalBlockInImmediateFunctionBody(blockNode);
        data.env = SymbolEnv.createBlockEnv(blockNode, data.env);
        blockNode.stmts.forEach(e -> analyzeNode(e, data));
        data.inInternallyDefinedBlockStmt = inInternallyDefinedBlockStmt;
        if (data.commitCountWithinBlock > 1 || data.rollbackCountWithinBlock > 1) {
            this.dlog.error(blockNode.pos, DiagnosticErrorCode.MAX_ONE_COMMIT_ROLLBACK_ALLOWED_WITHIN_A_BRANCH);
        }
        data.commitCountWithinBlock = prevCommitCount;
        data.rollbackCountWithinBlock = prevRollbackCount;
    }

    private boolean checkBlockIsAnInternalBlockInImmediateFunctionBody(BLangNode node) {
        BLangNode parent = node.parent;

        while (parent != null) {
            final NodeKind kind = parent.getKind();
            if (kind == NodeKind.BLOCK_FUNCTION_BODY) {
                return true;
            }
            if (kind == NodeKind.BLOCK) {
                parent = parent.parent;
            } else {
                return false;
            }
        }

        return false;
    }

    @Override
    public void visit(BLangReturn returnStmt, AnalyzerData data) {
        if (checkReturnValidityInTransaction(data)) {
            this.dlog.error(returnStmt.pos, DiagnosticErrorCode.RETURN_CANNOT_BE_USED_TO_EXIT_TRANSACTION);
            return;
        }

        analyzeExpr(returnStmt.expr, data);
        data.returnTypes.peek().add(returnStmt.expr.getBType());
    }

    @Override
    public void visit(BLangIf ifStmt, AnalyzerData data) {
        boolean independentBlocks = false;
        int prevCommitCount = data.commitCount;
        int prevRollbackCount = data.rollbackCount;
        BLangStatement elseStmt = ifStmt.elseStmt;
        if (data.withinTransactionScope && elseStmt != null && elseStmt.getKind() != NodeKind.IF) {
                independentBlocks = true;
                data.commitRollbackAllowed = true;
        }
        boolean prevTxMode = data.withinTransactionScope;
        if ((ifStmt.expr.getKind() == NodeKind.GROUP_EXPR ?
                ((BLangGroupExpr) ifStmt.expr).expression.getKind() :
                ifStmt.expr.getKind()) == NodeKind.TRANSACTIONAL_EXPRESSION) {
            data.withinTransactionScope = true;
        }
        BLangBlockStmt body = ifStmt.body;
        analyzeNode(body, data);

        if (ifStmt.expr.getKind() == NodeKind.TRANSACTIONAL_EXPRESSION) {
            data.withinTransactionScope = prevTxMode;
        }
        if (elseStmt != null) {
            if (independentBlocks) {
                data.commitRollbackAllowed = true;
                data.withinTransactionScope = true;
            }
            analyzeNode(elseStmt, data);
            if ((prevCommitCount != data.commitCount) || prevRollbackCount != data.rollbackCount) {
                data.commitRollbackAllowed = false;
            }
        }

        analyzeExpr(ifStmt.expr, data);
    }

    @Override
    public void visit(BLangMatchStatement matchStatement, AnalyzerData data) {
        analyzeExpr(matchStatement.expr, data);
        boolean onFailExists = matchStatement.onFailClause != null;
        boolean failureHandled = data.failureHandled;
        if (onFailExists) {
            data.errorTypes.push(new LinkedHashSet<>());
            data.failureHandled = true;
        }
        List<BLangMatchClause> matchClauses = matchStatement.matchClauses;
        int clausesSize = matchClauses.size();
        for (int i = 0; i < clausesSize; i++) {
            BLangMatchClause firstClause = matchClauses.get(i);
            for (int j = i + 1; j < clausesSize; j++) {
                BLangMatchClause secondClause = matchClauses.get(j);
                if (!checkSimilarMatchGuard(firstClause.matchGuard, secondClause.matchGuard)) {
                    if (firstClause.matchGuard == null) {
                        checkSimilarMatchPatternsBetweenClauses(firstClause, secondClause);
                    }
                    continue;
                }
                checkSimilarMatchPatternsBetweenClauses(firstClause, secondClause);
            }
            analyzeNode(firstClause, data);
        }
        data.failureHandled = failureHandled;
        analyzeOnFailClause(matchStatement.onFailClause, data);
    }

    @Override
    public void visit(BLangMatchClause matchClause, AnalyzerData data) {
        Map<String, BVarSymbol> variablesInMatchPattern = new HashMap<>();
        boolean patternListContainsSameVars = true;

        List<BLangMatchPattern> matchPatterns = matchClause.matchPatterns;
        BLangMatchGuard matchGuard = matchClause.matchGuard;
        for (int i = 0; i < matchPatterns.size(); i++) {
            BLangMatchPattern matchPattern = matchPatterns.get(i);
            if (matchPattern.getBType() == symTable.noType) {
                dlog.warning(matchPattern.pos, DiagnosticWarningCode.MATCH_STMT_UNMATCHED_PATTERN);
            }
            if (patternListContainsSameVars) {
                patternListContainsSameVars = compareVariables(variablesInMatchPattern, matchPattern);
            }
            for (int j = i - 1; j >= 0; j--) {
                if (checkSimilarMatchPatterns(matchPatterns.get(j), matchPattern)) {
                    dlog.warning(matchPattern.pos, DiagnosticWarningCode.MATCH_STMT_PATTERN_UNREACHABLE);
                }
            }
            analyzeNode(matchPattern, data);
        }

        if (matchGuard != null) {
            analyzeNode(matchGuard, data);
        }

        if (!patternListContainsSameVars) {
            dlog.error(matchClause.pos, DiagnosticErrorCode.MATCH_PATTERNS_SHOULD_CONTAIN_SAME_SET_OF_VARIABLES);
        }

        analyzeNode(matchClause.blockStmt, data);
    }

    @Override
    public void visit(BLangMappingMatchPattern mappingMatchPattern, AnalyzerData data) {
    }

    @Override
    public void visit(BLangFieldMatchPattern fieldMatchPattern, AnalyzerData data) {

    }

    @Override
    public void visit(BLangMatchGuard matchGuard, AnalyzerData data) {
        analyzeExpr(matchGuard.expr, data);
    }

    private void checkSimilarMatchPatternsBetweenClauses(BLangMatchClause firstClause, BLangMatchClause secondClause) {
        for (BLangMatchPattern firstMatchPattern : firstClause.matchPatterns) {
            for (BLangMatchPattern secondMatchPattern : secondClause.matchPatterns) {
                if (checkSimilarMatchPatterns(firstMatchPattern, secondMatchPattern)) {
                    dlog.warning(secondMatchPattern.pos, DiagnosticWarningCode.MATCH_STMT_PATTERN_UNREACHABLE);
                }
            }
        }
    }

    private boolean checkSimilarMatchPatterns(BLangMatchPattern firstPattern, BLangMatchPattern secondPattern) {
        NodeKind firstPatternKind = firstPattern.getKind();
        NodeKind secondPatternKind = secondPattern.getKind();
        if (firstPatternKind != secondPatternKind) {
            if (firstPatternKind == NodeKind.VAR_BINDING_PATTERN_MATCH_PATTERN) {
                return checkEmptyListOrMapMatchWithVarBindingPatternMatch(secondPattern,
                        ((BLangVarBindingPatternMatchPattern) firstPattern));
            }
            if (secondPatternKind == NodeKind.VAR_BINDING_PATTERN_MATCH_PATTERN) {
                return checkEmptyListOrMapMatchWithVarBindingPatternMatch(firstPattern,
                        ((BLangVarBindingPatternMatchPattern) secondPattern));
            }
            return false;
        }

        switch (firstPatternKind) {
            case WILDCARD_MATCH_PATTERN:
            case REST_MATCH_PATTERN:
                return true;
            case CONST_MATCH_PATTERN:
                return checkSimilarConstMatchPattern((BLangConstPattern) firstPattern,
                        (BLangConstPattern) secondPattern);
            case VAR_BINDING_PATTERN_MATCH_PATTERN:
                return checkSimilarBindingPatterns(
                        ((BLangVarBindingPatternMatchPattern) firstPattern).getBindingPattern(),
                        ((BLangVarBindingPatternMatchPattern) secondPattern).getBindingPattern());
            case LIST_MATCH_PATTERN:
                return checkSimilarListMatchPattern((BLangListMatchPattern) firstPattern,
                        (BLangListMatchPattern) secondPattern);
            case MAPPING_MATCH_PATTERN:
                return checkSimilarMappingMatchPattern((BLangMappingMatchPattern) firstPattern,
                        (BLangMappingMatchPattern) secondPattern);
            case ERROR_MATCH_PATTERN:
                return checkSimilarErrorMatchPattern((BLangErrorMatchPattern) firstPattern,
                        (BLangErrorMatchPattern) secondPattern);
            default:
                return false;
        }
    }

    private boolean checkEmptyListOrMapMatchWithVarBindingPatternMatch(BLangMatchPattern firstPattern,
                                                                  BLangVarBindingPatternMatchPattern secondPattern) {
        if (firstPattern.getKind() == NodeKind.LIST_MATCH_PATTERN) {
            BLangBindingPattern bindingPattern = secondPattern.getBindingPattern();
            if (bindingPattern.getKind() != NodeKind.LIST_BINDING_PATTERN) {
                return false;
            }
            BLangListMatchPattern listMatchPattern = (BLangListMatchPattern) firstPattern;
            BLangListBindingPattern listBindingPattern = (BLangListBindingPattern) bindingPattern;
            return listMatchPattern.matchPatterns.isEmpty() && listBindingPattern.bindingPatterns.isEmpty() &&
                    listMatchPattern.restMatchPattern == null && listBindingPattern.restBindingPattern == null;
        }
        if (firstPattern.getKind() == NodeKind.MAPPING_MATCH_PATTERN) {
            BLangBindingPattern bindingPattern = secondPattern.getBindingPattern();
            if (secondPattern.getBindingPattern().getKind() != NodeKind.MAPPING_BINDING_PATTERN) {
                return false;
            }
            BLangMappingMatchPattern mappingMatchPattern = (BLangMappingMatchPattern) firstPattern;
            BLangMappingBindingPattern mappingBindingPattern = (BLangMappingBindingPattern) bindingPattern;
            return mappingMatchPattern.fieldMatchPatterns.isEmpty() &&
                    mappingBindingPattern.fieldBindingPatterns.isEmpty() &&
                    mappingMatchPattern.restMatchPattern == null && mappingBindingPattern.restBindingPattern == null;
        }
        return false;
    }

    private boolean checkSimilarErrorMatchPattern(BLangErrorMatchPattern firstErrorMatchPattern,
                                                  BLangErrorMatchPattern secondErrorMatchPattern) {
        if (firstErrorMatchPattern == null || secondErrorMatchPattern == null) {
            return false;
        }

        if (!checkSimilarErrorTypeReference(firstErrorMatchPattern.errorTypeReference,
                secondErrorMatchPattern.errorTypeReference)) {
            return false;
        }

        if (!checkSimilarErrorMessagePattern(firstErrorMatchPattern.errorMessageMatchPattern,
                secondErrorMatchPattern.errorMessageMatchPattern)) {
            return false;
        }

        if (!checkSimilarErrorCauseMatchPattern(firstErrorMatchPattern.errorCauseMatchPattern,
                secondErrorMatchPattern.errorCauseMatchPattern)) {
            return false;
        }

        return checkSimilarErrorFieldMatchPatterns(firstErrorMatchPattern.errorFieldMatchPatterns,
                secondErrorMatchPattern.errorFieldMatchPatterns);
    }

    private boolean checkSimilarErrorTypeReference(BLangUserDefinedType firstErrorTypeRef,
                                                   BLangUserDefinedType secondErrorTypeRef) {
        if (firstErrorTypeRef != null && secondErrorTypeRef != null) {
            return firstErrorTypeRef.typeName.value.equals(secondErrorTypeRef.typeName.value);
        }
        return firstErrorTypeRef == null && secondErrorTypeRef == null;
    }

    private boolean checkSimilarErrorMessagePattern(BLangErrorMessageMatchPattern firstErrorMsgMatchPattern,
                                                    BLangErrorMessageMatchPattern secondErrorMsgMatchPattern) {
        if (firstErrorMsgMatchPattern != null && secondErrorMsgMatchPattern != null) {
            return checkSimilarSimpleMatchPattern(firstErrorMsgMatchPattern.simpleMatchPattern,
                    secondErrorMsgMatchPattern.simpleMatchPattern);
        }
        return firstErrorMsgMatchPattern == null && secondErrorMsgMatchPattern == null;
    }

    private boolean checkSimilarSimpleMatchPattern(BLangSimpleMatchPattern firstSimpleMatchPattern,
                                                   BLangSimpleMatchPattern secondSimpleMatchPattern) {
        if (firstSimpleMatchPattern != null && secondSimpleMatchPattern != null) {
            if (firstSimpleMatchPattern.varVariableName != null) {
                return true;
            }
            BLangConstPattern firstConstPattern = firstSimpleMatchPattern.constPattern;
            BLangConstPattern secondConstPattern = secondSimpleMatchPattern.constPattern;
            if (firstConstPattern != null) {
                if (secondConstPattern != null) {
                    return checkSimilarConstMatchPattern(firstConstPattern, secondConstPattern);
                }
                return false;
            }
            return secondSimpleMatchPattern.varVariableName == null;
        }
        return firstSimpleMatchPattern == null && secondSimpleMatchPattern == null;
    }

    private boolean checkSimilarErrorCauseMatchPattern(BLangErrorCauseMatchPattern firstErrorCauseMatchPattern,
                                                       BLangErrorCauseMatchPattern secondErrorCauseMatchPattern) {
        if (firstErrorCauseMatchPattern != null && secondErrorCauseMatchPattern != null) {
            if (!checkSimilarSimpleMatchPattern(firstErrorCauseMatchPattern.simpleMatchPattern,
                    secondErrorCauseMatchPattern.simpleMatchPattern)) {
                return false;
            }
            return checkSimilarErrorMatchPattern(firstErrorCauseMatchPattern.errorMatchPattern,
                    secondErrorCauseMatchPattern.errorMatchPattern);
        }
        return firstErrorCauseMatchPattern == null && secondErrorCauseMatchPattern == null;
    }

    private boolean checkSimilarErrorFieldMatchPatterns(BLangErrorFieldMatchPatterns firstErrorFieldMatchPatterns,
                                                        BLangErrorFieldMatchPatterns secondErrorFieldMatchPatterns) {
        if (firstErrorFieldMatchPatterns == null) {
            return true;
        }
        List<BLangNamedArgMatchPattern> firstNamedArgPatterns = firstErrorFieldMatchPatterns.namedArgMatchPatterns;
        int firstNamedArgPatternsSize = firstNamedArgPatterns.size();
        if (firstNamedArgPatternsSize == 0) {
            return true;
        }
        if (secondErrorFieldMatchPatterns == null) {
            return false;
        }
        List<BLangNamedArgMatchPattern> secondNamedArgPatterns = secondErrorFieldMatchPatterns.namedArgMatchPatterns;
        if (firstNamedArgPatternsSize > secondNamedArgPatterns.size()) {
            return false;
        }
        for (int i = 0; i < firstNamedArgPatternsSize; i++) {
            if (!checkSimilarNamedArgMatchPatterns(firstNamedArgPatterns.get(i), secondNamedArgPatterns.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkSimilarNamedArgMatchPatterns(BLangNamedArgMatchPattern firstNamedArgMatchPattern,
                                                      BLangNamedArgMatchPattern secondNamedArgMatchPattern) {

        if (firstNamedArgMatchPattern.argName.value.equals(secondNamedArgMatchPattern.argName.value)) {
            return checkSimilarMatchPatterns(firstNamedArgMatchPattern.matchPattern,
                    secondNamedArgMatchPattern.matchPattern);
        }
        return false;
    }

    private boolean checkSimilarConstMatchPattern(BLangConstPattern firstConstMatchPattern,
                                                  BLangConstPattern secondConstMatchPattern) {
        Object firstConstValue = getConstValue(firstConstMatchPattern).keySet().iterator().next();
        Object secondConstValue = getConstValue(secondConstMatchPattern).keySet().iterator().next();
        BType firstConstType = getConstValue(firstConstMatchPattern).values().iterator().next();
        BType secondConstType = getConstValue(secondConstMatchPattern).values().iterator().next();

        if (firstConstValue == null || secondConstValue == null) {
            return false;
        }

        if (firstConstValue.equals(secondConstValue)) {
            return true;
        }

        if (firstConstType != null && Types.getReferredType(firstConstType).tag == TypeTags.FINITE) {
            firstConstValue = getConstValueFromFiniteType(((BFiniteType) firstConstType));
        }

        if (secondConstType != null && Types.getReferredType(secondConstType).tag == TypeTags.FINITE) {
            secondConstValue = getConstValueFromFiniteType(((BFiniteType) secondConstType));
        }

        if (firstConstValue == null || secondConstValue == null) {
            return false;
        }

        return firstConstValue.equals(secondConstValue);
    }

    private HashMap<Object, BType> getConstValue(BLangConstPattern constPattern) {
        HashMap<Object, BType> constValAndType = new HashMap<>();
        switch (constPattern.expr.getKind()) {
            case NUMERIC_LITERAL:
                constValAndType.put(((BLangNumericLiteral) constPattern.expr).value, null);
                break;
            case LITERAL:
                constValAndType.put(((BLangLiteral) constPattern.expr).value, null);
                break;
            case SIMPLE_VARIABLE_REF:
                constValAndType.put(((BLangSimpleVarRef) constPattern.expr).variableName, constPattern.getBType());
                break;
            case UNARY_EXPR:
                BLangNumericLiteral newNumericLiteral = Types.constructNumericLiteralFromUnaryExpr(
                        (BLangUnaryExpr) constPattern.expr);
                constValAndType.put(newNumericLiteral.value, null);
        }
        return constValAndType;
    }

    private Object getConstValueFromFiniteType(BFiniteType type) {
        if (type.getValueSpace().size() == 1) {
            BLangExpression expr = type.getValueSpace().iterator().next();
            switch (expr.getKind()) {
                case NUMERIC_LITERAL:
                    return ((BLangNumericLiteral) expr).value;
                case LITERAL:
                    return ((BLangLiteral) expr).value;
            }
        }
        return null;
    }

    private boolean checkSimilarListMatchPattern(BLangListMatchPattern firstListMatchPattern,
                                                 BLangListMatchPattern secondListMatchPattern) {
        List<BLangMatchPattern> firstMatchPatterns = firstListMatchPattern.matchPatterns;
        List<BLangMatchPattern> secondMatchPatterns = secondListMatchPattern.matchPatterns;
        int firstPatternsSize = firstMatchPatterns.size();
        int secondPatternsSize = secondMatchPatterns.size();
        if (firstPatternsSize <= secondPatternsSize) {
            for (int i = 0; i < firstPatternsSize; i++) {
                if (!checkSimilarMatchPatterns(firstMatchPatterns.get(i), secondMatchPatterns.get(i))) {
                    return false;
                }
            }
            if (firstPatternsSize == secondPatternsSize) {
                if (firstListMatchPattern.restMatchPattern != null) {
                    return true;
                }
                return secondListMatchPattern.restMatchPattern == null;
            }
            return firstListMatchPattern.restMatchPattern != null;
        }
        return false;
    }

    private boolean checkSimilarMappingMatchPattern(BLangMappingMatchPattern firstMappingMatchPattern,
                                                    BLangMappingMatchPattern secondMappingMatchPattern) {
        List<BLangFieldMatchPattern> firstFieldMatchPatterns = firstMappingMatchPattern.fieldMatchPatterns;
        List<BLangFieldMatchPattern> secondFieldMatchPatterns = secondMappingMatchPattern.fieldMatchPatterns;
        return checkSimilarFieldMatchPatterns(firstFieldMatchPatterns, secondFieldMatchPatterns);
    }

    private boolean checkSimilarFieldMatchPatterns(List<BLangFieldMatchPattern> firstFieldMatchPatterns,
                                                   List<BLangFieldMatchPattern> secondFieldMatchPatterns) {
        for (BLangFieldMatchPattern firstFieldMatchPattern : firstFieldMatchPatterns) {
            boolean isSamePattern = false;
            for (BLangFieldMatchPattern secondFieldMatchPattern : secondFieldMatchPatterns) {
                if (checkSimilarFieldMatchPattern(firstFieldMatchPattern, secondFieldMatchPattern)) {
                    isSamePattern = true;
                    break;
                }
            }
            if (!isSamePattern) {
                return false;
            }
        }
        return true;
    }

    private boolean checkSimilarFieldMatchPattern(BLangFieldMatchPattern firstFieldMatchPattern,
                                                  BLangFieldMatchPattern secondFieldMatchPattern) {
        return firstFieldMatchPattern.fieldName.value.equals(secondFieldMatchPattern.fieldName.value) &&
                checkSimilarMatchPatterns(firstFieldMatchPattern.matchPattern, secondFieldMatchPattern.matchPattern);
    }

    private boolean checkSimilarBindingPatterns(BLangBindingPattern firstBidingPattern,
                                                BLangBindingPattern secondBindingPattern) {
        NodeKind firstBindingPatternKind = firstBidingPattern.getKind();
        NodeKind secondBindingPatternKind = secondBindingPattern.getKind();
        if (firstBindingPatternKind != secondBindingPatternKind) {
            return false;
        }

        switch (firstBindingPatternKind) {
            case WILDCARD_BINDING_PATTERN:
            case REST_BINDING_PATTERN:
            case CAPTURE_BINDING_PATTERN:
                return true;
            case LIST_BINDING_PATTERN:
                return checkSimilarListBindingPatterns((BLangListBindingPattern) firstBidingPattern,
                        (BLangListBindingPattern) secondBindingPattern);
            case MAPPING_BINDING_PATTERN:
                return checkSimilarMappingBindingPattern((BLangMappingBindingPattern) firstBidingPattern,
                        (BLangMappingBindingPattern) secondBindingPattern);
            case ERROR_BINDING_PATTERN:
                return checkSimilarErrorBindingPatterns((BLangErrorBindingPattern) firstBidingPattern,
                        (BLangErrorBindingPattern) secondBindingPattern);
            default:
                return false;
        }
    }

    private boolean checkSimilarMappingBindingPattern(BLangMappingBindingPattern firstMappingBindingPattern,
                                                      BLangMappingBindingPattern secondMappingBindingPattern) {
        List<BLangFieldBindingPattern> firstFieldBindingPatterns = firstMappingBindingPattern.fieldBindingPatterns;
        List<BLangFieldBindingPattern> secondFieldBindingPatterns = secondMappingBindingPattern.fieldBindingPatterns;
        return checkSimilarFieldBindingPatterns(firstFieldBindingPatterns, secondFieldBindingPatterns);
    }

    private boolean checkSimilarFieldBindingPatterns(List<BLangFieldBindingPattern> firstFieldBindingPatterns,
                                                     List<BLangFieldBindingPattern> secondFieldBindingPatterns) {
        for (BLangFieldBindingPattern firstFieldBindingPattern : firstFieldBindingPatterns) {
            boolean isSamePattern = false;
            for (BLangFieldBindingPattern secondFieldBindingPattern : secondFieldBindingPatterns) {
                if (checkSimilarFieldBindingPattern(firstFieldBindingPattern, secondFieldBindingPattern)) {
                    isSamePattern = true;
                    break;
                }
            }
            if (!isSamePattern) {
                return false;
            }
        }
        return true;
    }

    private boolean checkSimilarFieldBindingPattern(BLangFieldBindingPattern firstFieldBindingPattern,
                                                    BLangFieldBindingPattern secondFieldBindingPattern) {
        boolean hasSameFieldNames = firstFieldBindingPattern.fieldName.value.
                        equals(secondFieldBindingPattern.fieldName.value);
        if (firstFieldBindingPattern.bindingPattern.getKind() == secondFieldBindingPattern.bindingPattern.getKind()) {
            return hasSameFieldNames && checkSimilarBindingPatterns(firstFieldBindingPattern.bindingPattern,
                    secondFieldBindingPattern.bindingPattern);
        }
        return hasSameFieldNames && firstFieldBindingPattern.bindingPattern.getKind() ==
                NodeKind.CAPTURE_BINDING_PATTERN;
    }

    private boolean checkSimilarListBindingPatterns(BLangListBindingPattern firstBindingPattern,
                                                    BLangListBindingPattern secondBindingPattern) {
        List<BLangBindingPattern> firstPatterns = firstBindingPattern.bindingPatterns;
        List<BLangBindingPattern> secondPatterns = secondBindingPattern.bindingPatterns;
        int firstPatternsSize = firstPatterns.size();
        int secondPatternsSize = secondPatterns.size();
        if (firstPatternsSize <= secondPatternsSize) {
            for (int i = 0; i < firstPatternsSize; i++) {
                if (!checkSimilarBindingPatterns(firstPatterns.get(i), secondPatterns.get(i))) {
                    return firstPatterns.get(i).getKind() == NodeKind.CAPTURE_BINDING_PATTERN;
                }
            }
            if (firstPatternsSize == secondPatternsSize) {
                if (firstBindingPattern.restBindingPattern != null) {
                    return true;
                }
                return secondBindingPattern.restBindingPattern == null;
            }
            return secondBindingPattern.restBindingPattern != null;
        }
        return false;
    }

    private boolean checkSimilarErrorBindingPatterns(BLangErrorBindingPattern firstErrorBindingPattern,
                                                     BLangErrorBindingPattern secondErrorBindingPattern) {
        if (firstErrorBindingPattern == null || secondErrorBindingPattern == null) {
            return false;
        }

        if (!checkSimilarErrorTypeReference(firstErrorBindingPattern.errorTypeReference,
                secondErrorBindingPattern.errorTypeReference)) {
            return false;
        }

        if (!checkSimilarErrorMessageBindingPattern(firstErrorBindingPattern.errorMessageBindingPattern,
                secondErrorBindingPattern.errorMessageBindingPattern)) {
            return false;
        }

        if (!checkSimilarErrorCauseBindingPattern(firstErrorBindingPattern.errorCauseBindingPattern,
                secondErrorBindingPattern.errorCauseBindingPattern)) {
            return false;
        }

        return checkSimilarErrorFieldBindingPatterns(firstErrorBindingPattern.errorFieldBindingPatterns,
                secondErrorBindingPattern.errorFieldBindingPatterns);

    }

    private boolean checkSimilarErrorMessageBindingPattern(BLangErrorMessageBindingPattern firstErrorMsgBindingPattern,
                                                      BLangErrorMessageBindingPattern secondErrorMsgBindingPattern) {
        if (firstErrorMsgBindingPattern != null && secondErrorMsgBindingPattern != null) {
            return checkSimilarSimpleBindingPattern(firstErrorMsgBindingPattern.simpleBindingPattern,
                    secondErrorMsgBindingPattern.simpleBindingPattern);
        }
        return firstErrorMsgBindingPattern == null && secondErrorMsgBindingPattern == null;
    }

    private boolean checkSimilarSimpleBindingPattern(BLangSimpleBindingPattern firstSimpleBindingPattern,
                                                     BLangSimpleBindingPattern secondSimpleBindingPattern) {
        if (firstSimpleBindingPattern != null && secondSimpleBindingPattern != null) {
            BLangBindingPattern firstCaptureBindingPattern = firstSimpleBindingPattern.captureBindingPattern;
            BLangBindingPattern secondCaptureBindingPattern = secondSimpleBindingPattern.captureBindingPattern;
            if (firstCaptureBindingPattern != null && secondCaptureBindingPattern != null) {
                return checkSimilarBindingPatterns(firstCaptureBindingPattern, secondCaptureBindingPattern);
            }
            return firstSimpleBindingPattern.wildCardBindingPattern != null;
        }
        return firstSimpleBindingPattern == null && secondSimpleBindingPattern == null;
    }

    private boolean checkSimilarErrorCauseBindingPattern(BLangErrorCauseBindingPattern firstErrorCauseBindingPattern,
                                                     BLangErrorCauseBindingPattern secondErrorCauseBindingPattern) {
        if (firstErrorCauseBindingPattern != null && secondErrorCauseBindingPattern != null) {
            if (!checkSimilarSimpleBindingPattern(firstErrorCauseBindingPattern.simpleBindingPattern,
                    secondErrorCauseBindingPattern.simpleBindingPattern)) {
                return false;
            }
            return checkSimilarErrorBindingPatterns(firstErrorCauseBindingPattern.errorBindingPattern,
                    secondErrorCauseBindingPattern.errorBindingPattern);
        }
        return firstErrorCauseBindingPattern == null && secondErrorCauseBindingPattern == null;
    }

    private boolean checkSimilarErrorFieldBindingPatterns(
                                          BLangErrorFieldBindingPatterns firstErrorFieldBindingPatterns,
                                          BLangErrorFieldBindingPatterns secondErrorFieldBindingPatterns) {
        if (firstErrorFieldBindingPatterns == null) {
            return true;
        }
        List<BLangNamedArgBindingPattern> firstNamedArgPatterns =
                firstErrorFieldBindingPatterns.namedArgBindingPatterns;
        int firstNamedArgPatternsSize = firstNamedArgPatterns.size();
        if (firstNamedArgPatternsSize == 0) { // only rest-binding-pattern
            return true;
        }
        if (secondErrorFieldBindingPatterns == null) {
            return false;
        }
        List<BLangNamedArgBindingPattern> secondNamedArgPatterns =
                secondErrorFieldBindingPatterns.namedArgBindingPatterns;
        if (firstNamedArgPatternsSize > secondNamedArgPatterns.size()) {
            return false;
        }
        for (int i = 0; i < firstNamedArgPatternsSize; i++) {
            if (!checkSimilarNamedArgBindingPatterns(firstNamedArgPatterns.get(i), secondNamedArgPatterns.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkSimilarNamedArgBindingPatterns(BLangNamedArgBindingPattern firstNamedArgBindingPattern,
                                                        BLangNamedArgBindingPattern secondNamedArgBindingPattern) {
        if (firstNamedArgBindingPattern.argName.value.equals(secondNamedArgBindingPattern.argName.value)) {
            return checkSimilarBindingPatterns(firstNamedArgBindingPattern.bindingPattern,
                    secondNamedArgBindingPattern.bindingPattern);
        }
        return false;
    }

    private boolean checkSimilarMatchGuard(BLangMatchGuard firstMatchGuard, BLangMatchGuard secondMatchGuard) {
        if (firstMatchGuard == null && secondMatchGuard == null) {
            return true;
        }
        if (firstMatchGuard == null || secondMatchGuard == null) {
            return false;
        }
        if (firstMatchGuard.expr.getKind() == NodeKind.TYPE_TEST_EXPR &&
                secondMatchGuard.expr.getKind() == NodeKind.TYPE_TEST_EXPR &&
                ((BLangTypeTestExpr) firstMatchGuard.expr).expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                ((BLangTypeTestExpr) secondMatchGuard.expr).expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangTypeTestExpr firstTypeTest = (BLangTypeTestExpr) firstMatchGuard.expr;
            BLangTypeTestExpr secondTypeTest = (BLangTypeTestExpr) secondMatchGuard.expr;
            return ((BLangSimpleVarRef) firstTypeTest.expr).variableName.toString().equals(
                    ((BLangSimpleVarRef) secondTypeTest.expr).variableName.toString()) &&
                    types.isAssignable(firstTypeTest.typeNode.getBType(),
                            secondTypeTest.typeNode.getBType());
        }
        return false;
    }

    private boolean compareVariables(Map<String, BVarSymbol> varsInPreviousMatchPattern,
                                     BLangMatchPattern matchPattern) {
        Map<String, BVarSymbol> varsInCurrentMatchPattern = matchPattern.declaredVars;
        if (varsInPreviousMatchPattern.size() == 0) {
            varsInPreviousMatchPattern.putAll(varsInCurrentMatchPattern);
            return true;
        }
        if (varsInPreviousMatchPattern.size() != varsInCurrentMatchPattern.size()) {
            return false;
        }
        for (String identifier : varsInPreviousMatchPattern.keySet()) {
            if (!varsInCurrentMatchPattern.containsKey(identifier)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void visit(BLangWildCardMatchPattern wildCardMatchPattern, AnalyzerData data) {
        wildCardMatchPattern.isLastPattern =
                wildCardMatchPattern.matchExpr != null && types.isAssignable(wildCardMatchPattern.matchExpr.getBType(),
                                                                             symTable.anyType);
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern, AnalyzerData data) {
        analyzeNode(constMatchPattern.expr, data);
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern, AnalyzerData data) {
        BLangBindingPattern bindingPattern = varBindingPattern.getBindingPattern();
        analyzeNode(bindingPattern, data);
        switch (bindingPattern.getKind()) {
            case WILDCARD_BINDING_PATTERN:
                varBindingPattern.isLastPattern =
                        varBindingPattern.matchExpr != null && types.isAssignable(
                                varBindingPattern.matchExpr.getBType(),
                                symTable.anyType);
                return;
            case CAPTURE_BINDING_PATTERN:
                varBindingPattern.isLastPattern =
                        varBindingPattern.matchExpr != null && !varBindingPattern.matchGuardIsAvailable;
                return;
            case LIST_BINDING_PATTERN:
                if (varBindingPattern.matchExpr == null) {
                    return;
                }
                varBindingPattern.isLastPattern = types.isSameType(varBindingPattern.matchExpr.getBType(),
                                                                   varBindingPattern.getBType()) || types.isAssignable(
                        varBindingPattern.matchExpr.getBType(),
                        varBindingPattern.getBType());
        }
    }

    @Override
    public void visit(BLangMappingBindingPattern mappingBindingPattern, AnalyzerData data) {

    }

    @Override
    public void visit(BLangWildCardBindingPattern wildCardBindingPattern, AnalyzerData data) {

    }

    @Override
    public void visit(BLangListMatchPattern listMatchPattern, AnalyzerData data) {
        if (listMatchPattern.matchExpr == null) {
            return;
        }
        listMatchPattern.isLastPattern = types.isAssignable(listMatchPattern.matchExpr.getBType(),
                listMatchPattern.getBType()) && !isConstMatchPatternExist(listMatchPattern);
    }

    private boolean isConstMatchPatternExist(BLangMatchPattern matchPattern) {
        switch (matchPattern.getKind()) {
            case CONST_MATCH_PATTERN:
                return true;
            case LIST_MATCH_PATTERN:
                for (BLangMatchPattern memberMatchPattern : ((BLangListMatchPattern) matchPattern).matchPatterns) {
                    if (isConstMatchPatternExist(memberMatchPattern)) {
                        return true;
                    }
                }
                return false;
            case MAPPING_MATCH_PATTERN:
                for (BLangFieldMatchPattern fieldMatchPattern :
                        ((BLangMappingMatchPattern) matchPattern).fieldMatchPatterns) {
                    if (isConstMatchPatternExist(fieldMatchPattern.matchPattern)) {
                        return true;
                    }
                }
                return false;
            default:
                return false;
        }
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern, AnalyzerData data) {
    }

    @Override
    public void visit(BLangListBindingPattern listBindingPattern, AnalyzerData data) {
    }

    @Override
    public void visit(BLangErrorMatchPattern errorMatchPattern, AnalyzerData data) {

    }

    @Override
    public void visit(BLangErrorBindingPattern errorBindingPattern, AnalyzerData data) {

    }

    @Override
    public void visit(BLangForeach foreach, AnalyzerData data) {
        data.loopWithinTransactionCheckStack.push(true);
        boolean onFailExists = foreach.onFailClause != null;
        boolean failureHandled = data.failureHandled;
        if (onFailExists) {
            data.errorTypes.push(new LinkedHashSet<>());
            data.failureHandled = true;
        }
        data.loopCount++;
        BLangBlockStmt body = foreach.body;
        data.env = SymbolEnv.createLoopEnv(foreach, data.env);
        analyzeNode(body, data);
        data.loopCount--;
        data.failureHandled = failureHandled;
        data.loopWithinTransactionCheckStack.pop();
        analyzeExpr(foreach.collection, data);
        analyseOnFailAndUpdateBreakMode(onFailExists, body, foreach.onFailClause, data);
    }

    @Override
    public void visit(BLangWhile whileNode, AnalyzerData data) {
        data.loopWithinTransactionCheckStack.push(true);
        boolean onFailExists = whileNode.onFailClause != null;
        boolean failureHandled = data.failureHandled;

        if (onFailExists) {
            data.errorTypes.push(new LinkedHashSet<>());
            data.failureHandled = true;
        }
        data.loopCount++;
        BLangBlockStmt body = whileNode.body;
        data.env = SymbolEnv.createLoopEnv(whileNode, data.env);
        analyzeNode(body, data);
        data.loopCount--;
        data.failureHandled = failureHandled;
        data.loopWithinTransactionCheckStack.pop();
        analyzeExpr(whileNode.expr, data);
        analyseOnFailAndUpdateBreakMode(onFailExists, whileNode.body, whileNode.onFailClause, data);
    }

    @Override
    public void visit(BLangDo doNode, AnalyzerData data) {
        boolean onFailExists = doNode.onFailClause != null;
        boolean failureHandled = data.failureHandled;
        if (onFailExists) {
            data.errorTypes.push(new LinkedHashSet<>());
            data.failureHandled = true;
        }
        analyzeNode(doNode.body, data);
        data.failureHandled = failureHandled;
        analyseOnFailAndUpdateBreakMode(onFailExists, doNode.body, doNode.onFailClause, data);
    }


    @Override
    public void visit(BLangFail failNode, AnalyzerData data) {
        data.failVisited = true;
        analyzeExpr(failNode.expr, data);
        if (data.env.scope.owner.getKind() == SymbolKind.PACKAGE) {
            // Check at module level.
            return;
        }
        typeChecker.checkExpr(failNode.expr, data.env);
        if (!data.errorTypes.empty()) {
            data.errorTypes.peek().add(getErrorTypes(failNode.expr.getBType()));
        }
        if (!data.failureHandled) {
            BType exprType = data.env.enclInvokable.getReturnTypeNode().getBType();
            data.returnTypes.peek().add(exprType);
            if (!types.isAssignable(getErrorTypes(failNode.expr.getBType()), exprType)) {
                dlog.error(failNode.pos, DiagnosticErrorCode.FAIL_EXPR_NO_MATCHING_ERROR_RETURN_IN_ENCL_INVOKABLE);
            }
        }
    }

    private BLangBlockStmt.FailureBreakMode getPossibleBreakMode(boolean possibleFailurePresent) {
        return possibleFailurePresent ? BLangBlockStmt.FailureBreakMode.BREAK_TO_OUTER_BLOCK
                : BLangBlockStmt.FailureBreakMode.NOT_BREAKABLE;
    }

    @Override
    public void visit(BLangLock lockNode, AnalyzerData data) {
        boolean onFailExists = lockNode.onFailClause != null;
        boolean failureHandled = data.failureHandled;
        if (onFailExists) {
            data.errorTypes.push(new LinkedHashSet<>());
            data.failureHandled = true;
        }
        boolean previousWithinLockBlock = data.withinLockBlock;
        data.withinLockBlock = true;
        lockNode.body.stmts.forEach(e -> analyzeNode(e, data));
        data.withinLockBlock = previousWithinLockBlock;
        data.failureHandled = failureHandled;
        analyseOnFailAndUpdateBreakMode(onFailExists, lockNode.body, lockNode.onFailClause, data);
    }

    @Override
    public void visit(BLangContinue continueNode, AnalyzerData data) {
        if (data.loopCount == 0) {
            this.dlog.error(continueNode.pos, DiagnosticErrorCode.CONTINUE_CANNOT_BE_OUTSIDE_LOOP);
            return;
        }
        if (checkNextBreakValidityInTransaction(data)) {
            this.dlog.error(continueNode.pos, DiagnosticErrorCode.CONTINUE_CANNOT_BE_USED_TO_EXIT_TRANSACTION);
            return;
        }
        if (data.loopAlterNotAllowed) {
            this.dlog.error(continueNode.pos, DiagnosticErrorCode.CONTINUE_NOT_ALLOWED);
        }
    }

    @Override
    public void visit(BLangImportPackage importPkgNode, AnalyzerData data) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }

        analyzeNode(pkgEnv.node, data);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangService serviceNode, AnalyzerData data) {
    }

    private void analyzeExportableTypeRef(BSymbol owner, BTypeSymbol symbol, boolean inFuncSignature,
                                          Location pos) {

        if (!inFuncSignature && Symbols.isFlagOn(owner.flags, Flags.ANONYMOUS)) {
            // Specially validate function signatures.
            return;
        }
        if (Symbols.isPublic(owner)) {
            HashSet<BTypeSymbol> visitedSymbols = new HashSet<>();
            checkForExportableType(symbol, pos, visitedSymbols);
        }
    }

    private void checkForExportableType(BTypeSymbol symbol, Location pos, HashSet<BTypeSymbol> visitedSymbols) {

        if (symbol == null || symbol.type == null || Symbols.isFlagOn(symbol.flags, Flags.TYPE_PARAM)) {
            // This is a built-in symbol or a type Param.
            return;
        }
        if (!visitedSymbols.add(symbol)) {
            return;
        }
        BType symbolType = symbol.type;
        switch (symbolType.tag) {
            case TypeTags.ARRAY:
                checkForExportableType(((BArrayType) symbolType).eType.tsymbol, pos, visitedSymbols);
                return;
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) symbolType;
                tupleType.getTupleTypes().forEach(t -> checkForExportableType(t.tsymbol, pos, visitedSymbols));
                if (tupleType.restType != null) {
                    checkForExportableType(tupleType.restType.tsymbol, pos, visitedSymbols);
                }
                return;
            case TypeTags.MAP:
                checkForExportableType(((BMapType) symbolType).constraint.tsymbol, pos, visitedSymbols);
                return;
            case TypeTags.RECORD:
                if (Symbols.isFlagOn(symbol.flags, Flags.ANONYMOUS)) {
                    BRecordType recordType = (BRecordType) symbolType;
                    recordType.fields.values().forEach(f -> checkForExportableType(f.type.tsymbol, pos,
                            visitedSymbols));
                    if (recordType.restFieldType != null) {
                        checkForExportableType(recordType.restFieldType.tsymbol, pos, visitedSymbols);
                    }
                    return;
                }
                break;
            case TypeTags.TABLE:
                BTableType tableType = (BTableType) symbolType;
                if (tableType.constraint != null) {
                    checkForExportableType(tableType.constraint.tsymbol, pos, visitedSymbols);
                }
                return;
            case TypeTags.STREAM:
                BStreamType streamType = (BStreamType) symbolType;
                if (streamType.constraint != null) {
                    checkForExportableType(streamType.constraint.tsymbol, pos, visitedSymbols);
                }
                return;
            case TypeTags.INVOKABLE:
                BInvokableType invokableType = (BInvokableType) symbolType;
                if (Symbols.isFlagOn(invokableType.flags, Flags.ANY_FUNCTION)) {
                    return;
                }
                if (invokableType.paramTypes != null) {
                    for (BType paramType : invokableType.paramTypes) {
                        checkForExportableType(paramType.tsymbol, pos, visitedSymbols);
                    }
                }
                if (invokableType.restType != null) {
                    checkForExportableType(invokableType.restType.tsymbol, pos, visitedSymbols);
                }
                checkForExportableType(invokableType.retType.tsymbol, pos, visitedSymbols);
                return;
            case TypeTags.PARAMETERIZED_TYPE:
                BTypeSymbol parameterizedType = ((BParameterizedType) symbolType).paramValueType.tsymbol;
                checkForExportableType(parameterizedType, pos, visitedSymbols);
                return;
            case TypeTags.ERROR:
                if (Symbols.isFlagOn(symbol.flags, Flags.ANONYMOUS)) {
                    checkForExportableType((((BErrorType) symbolType).detailType.tsymbol), pos, visitedSymbols);
                    return;
                }
                break;
            case TypeTags.TYPEREFDESC:
                symbolType = Types.getReferredType(symbolType);
                checkForExportableType(symbolType.tsymbol, pos, visitedSymbols);
                return;
            // TODO : Add support for other types. such as union and objects
        }
        if (!Symbols.isPublic(symbol)) {
            dlog.warning(pos, DiagnosticWarningCode.ATTEMPT_EXPOSE_NON_PUBLIC_SYMBOL, symbol.name);
        }
    }

    @Override
    public void visit(BLangLetExpression letExpression, AnalyzerData data) {
        long ownerSymTag = data.env.scope.owner.tag;
        if ((ownerSymTag & SymTag.RECORD) == SymTag.RECORD) {
            dlog.error(letExpression.pos, DiagnosticErrorCode.LET_EXPRESSION_NOT_YET_SUPPORTED_RECORD_FIELD);
        } else if ((ownerSymTag & SymTag.OBJECT) == SymTag.OBJECT) {
            dlog.error(letExpression.pos, DiagnosticErrorCode.LET_EXPRESSION_NOT_YET_SUPPORTED_OBJECT_FIELD);
        }

        data.env = letExpression.env;
        for (BLangLetVariable letVariable : letExpression.letVarDeclarations) {
            analyzeNode((BLangNode) letVariable.definitionNode, data);
        }
        analyzeExpr(letExpression.expr, data);
    }

    @Override
    public void visit(BLangSimpleVariable varNode, AnalyzerData data) {

        analyzeTypeNode(varNode.typeNode, data);

        analyzeExpr(varNode.expr, data);

        if (Objects.isNull(varNode.symbol)) {
            return;
        }

        if (!Symbols.isPublic(varNode.symbol)) {
            return;
        }

        long ownerSymTag = data.env.scope.owner.tag;
        if ((ownerSymTag & SymTag.RECORD) == SymTag.RECORD || (ownerSymTag & SymTag.OBJECT) == SymTag.OBJECT) {
            analyzeExportableTypeRef(data.env.scope.owner, varNode.getBType().tsymbol, false, varNode.pos);
        } else if ((ownerSymTag & SymTag.INVOKABLE) != SymTag.INVOKABLE) {
            // Only global level simpleVarRef, listeners etc.
            analyzeExportableTypeRef(varNode.symbol, varNode.getBType().tsymbol, false, varNode.pos);
        }

        varNode.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, data));
    }

    private boolean isValidInferredArray(BLangNode node) {
        switch (node.getKind()) {
            case INTERSECTION_TYPE_NODE:
            case UNION_TYPE_NODE:
                return isValidInferredArray(node.parent);
            case VARIABLE:
                BLangSimpleVariable varNode = (BLangSimpleVariable) node;
                BLangExpression expr = varNode.expr;
                return expr != null && isValidContextForInferredArray(node.parent) &&
                        isValidVariableForInferredArray(expr);
            default:
                return false;
        }
    }

    private boolean isValidContextForInferredArray(BLangNode node) {
        switch (node.getKind()) {
            case PACKAGE:
            case EXPR_FUNCTION_BODY:
            case BLOCK_FUNCTION_BODY:
            case BLOCK:
                return true;
            case VARIABLE_DEF:
                return isValidContextForInferredArray(node.parent);
            default:
                return false;
        }
    }

    private boolean isValidVariableForInferredArray(BLangNode node) {
        switch (node.getKind()) {
            case LITERAL:
                if (node.getBType().tag == TypeTags.ARRAY) {
                    return true;
                }
                break;
            case LIST_CONSTRUCTOR_EXPR:
                return true;
            case GROUP_EXPR:
                return isValidVariableForInferredArray(((BLangGroupExpr) node).expression);
        }
        return false;
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable, AnalyzerData data) {

        if (bLangTupleVariable.typeNode != null) {
            analyzeNode(bLangTupleVariable.typeNode, data);
        }
        analyzeExpr(bLangTupleVariable.expr, data);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable, AnalyzerData data) {

        if (bLangRecordVariable.typeNode != null) {
            analyzeNode(bLangRecordVariable.typeNode, data);
        }
        analyzeExpr(bLangRecordVariable.expr, data);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable, AnalyzerData data) {

        if (bLangErrorVariable.typeNode != null) {
            analyzeNode(bLangErrorVariable.typeNode, data);
        }
        analyzeExpr(bLangErrorVariable.expr, data);
    }

    @Override
    public void visit(BLangIdentifier identifierNode, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangAnnotation annotationNode, AnalyzerData data) {
        annotationNode.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, data));
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode, AnalyzerData data) {
        analyzeExpr(annAttachmentNode.expr, data);
        BAnnotationSymbol annotationSymbol = annAttachmentNode.annotationSymbol;
        if (annotationSymbol != null && Symbols.isFlagOn(annotationSymbol.flags, Flags.DEPRECATED)) {
            logDeprecatedWaring(annAttachmentNode.annotationName.toString(), annotationSymbol, annAttachmentNode.pos);
        }
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode, AnalyzerData data) {
        analyzeNode(varDefNode.var, data);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignment, AnalyzerData data) {
        BLangValueExpression varRef = compoundAssignment.varRef;
        analyzeExpr(varRef, data);
        analyzeExpr(compoundAssignment.expr, data);
    }

    @Override
    public void visit(BLangAssignment assignNode, AnalyzerData data) {
        BLangExpression varRef = assignNode.varRef;
        analyzeExpr(varRef, data);
        analyzeExpr(assignNode.expr, data);
    }

    @Override
    public void visit(BLangRecordDestructure stmt, AnalyzerData data) {
        List<BLangExpression> varRefs = getVarRefs(stmt.varRef);
        this.checkDuplicateVarRefs(varRefs);
        analyzeExpr(stmt.varRef, data);
        analyzeExpr(stmt.expr, data);
    }

    @Override
    public void visit(BLangErrorDestructure stmt, AnalyzerData data) {
        List<BLangExpression> varRefs = getVarRefs(stmt.varRef);
        this.checkDuplicateVarRefs(varRefs);
        analyzeExpr(stmt.varRef, data);
        analyzeExpr(stmt.expr, data);
    }

    @Override
    public void visit(BLangTupleDestructure stmt, AnalyzerData data) {
        List<BLangExpression> varRefs = getVarRefs(stmt.varRef);
        this.checkDuplicateVarRefs(varRefs);
        analyzeExpr(stmt.varRef, data);
        analyzeExpr(stmt.expr, data);
    }

    private void checkDuplicateVarRefs(List<BLangExpression> varRefs) {
        checkDuplicateVarRefs(varRefs, new HashSet<>());
    }

    private void checkDuplicateVarRefs(List<BLangExpression> varRefs, Set<BSymbol> symbols) {
        for (BLangExpression varRef : varRefs) {
            NodeKind kind = varRef.getKind();
            switch (kind) {
                case SIMPLE_VARIABLE_REF:
                    BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
                    if (simpleVarRef.symbol != null && !symbols.add(simpleVarRef.symbol)) {
                        this.dlog.error(varRef.pos, DiagnosticErrorCode.DUPLICATE_VARIABLE_IN_BINDING_PATTERN,
                                simpleVarRef.symbol);
                    }
                    break;
                case RECORD_VARIABLE_REF:
                    checkDuplicateVarRefs(getVarRefs((BLangRecordVarRef) varRef), symbols);
                    break;
                case ERROR_VARIABLE_REF:
                    checkDuplicateVarRefs(getVarRefs((BLangErrorVarRef) varRef), symbols);
                    break;
                case TUPLE_VARIABLE_REF:
                    checkDuplicateVarRefs(getVarRefs((BLangTupleVarRef) varRef), symbols);
                    break;
                default:
            }
        }
    }

    private List<BLangExpression> getVarRefs(BLangRecordVarRef varRef) {
        List<BLangExpression> varRefs = varRef.recordRefFields.stream()
                .map(e -> e.variableReference).collect(Collectors.toList());
        if (varRef.restParam != null) {
            varRefs.add(varRef.restParam);
        }
        return varRefs;
    }

    private List<BLangExpression> getVarRefs(BLangErrorVarRef varRef) {
        List<BLangExpression> varRefs = new ArrayList<>();
        if (varRef.message != null) {
            varRefs.add(varRef.message);
        }
        if (varRef.cause != null) {
            varRefs.add(varRef.cause);
        }
        varRefs.addAll(varRef.detail.stream().map(e -> e.expr).collect(Collectors.toList()));
        if (varRef.restVar != null) {
            varRefs.add(varRef.restVar);
        }
        return varRefs;
    }

    private List<BLangExpression> getVarRefs(BLangTupleVarRef varRef) {
        List<BLangExpression> varRefs = new ArrayList<>(varRef.expressions);
        if (varRef.restParam != null) {
            varRefs.add(varRef.restParam);
        }
        return varRefs;
    }

    @Override
    public void visit(BLangBreak breakNode, AnalyzerData data) {
        if (data.loopCount == 0) {
            this.dlog.error(breakNode.pos, DiagnosticErrorCode.BREAK_CANNOT_BE_OUTSIDE_LOOP);
            return;
        }
        if (checkNextBreakValidityInTransaction(data)) {
            this.dlog.error(breakNode.pos, DiagnosticErrorCode.BREAK_CANNOT_BE_USED_TO_EXIT_TRANSACTION);
            return;
        }
        if (data.loopAlterNotAllowed) {
            this.dlog.error(breakNode.pos, DiagnosticErrorCode.BREAK_NOT_ALLOWED);
        }
    }

    @Override
    public void visit(BLangPanic panicNode, AnalyzerData data) {
        analyzeExpr(panicNode.expr, data);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode, AnalyzerData data) {
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode, AnalyzerData data) {
        BLangExpression expr = exprStmtNode.expr;
        analyzeExpr(expr, data);
    }

    private boolean isTopLevel(SymbolEnv env) {
        return env.enclInvokable.body == env.node;
    }

    private boolean isInWorker(SymbolEnv env) {
        return env.enclInvokable.flagSet.contains(Flag.WORKER);
    }

    private boolean isCommunicationAllowedLocation(SymbolEnv env) {
        return isTopLevel(env);
    }

    private boolean isDefaultWorkerCommunication(String workerIdentifier) {
        return workerIdentifier.equals(DEFAULT_WORKER_NAME);
    }

    private boolean workerExists(BType type, String workerName, SymbolEnv env) {
        if (isDefaultWorkerCommunication(workerName) && isInWorker(env)) {
            return true;
        }
        if (type == symTable.semanticError) {
            return false;
        }
        BType refType = Types.getReferredType(type);
        return refType.tag == TypeTags.FUTURE && ((BFutureType) refType).workerDerivative;
    }


    // Asynchronous Send Statement
    @Override
    public void visit(BLangWorkerSend workerSendNode, AnalyzerData data) {
        BSymbol receiver =
                symResolver.lookupSymbolInMainSpace(data.env, names.fromIdNode(workerSendNode.workerIdentifier));
        if ((receiver.tag & SymTag.VARIABLE) != SymTag.VARIABLE) {
            receiver = symTable.notFoundSymbol;
        }
        verifyPeerCommunication(workerSendNode.pos, receiver, workerSendNode.workerIdentifier.value, data.env);

        WorkerActionSystem was = data.workerActionSystemStack.peek();

        BType type = workerSendNode.expr.getBType();
        if (type == symTable.semanticError) {
            // Error of this is already printed as undef-var
            was.hasErrors = true;
        } else if (workerSendNode.expr instanceof ActionNode) {
            this.dlog.error(workerSendNode.expr.pos, DiagnosticErrorCode.INVALID_SEND_EXPR);
        } else if (!types.isAssignable(type, symTable.cloneableType)) {
            this.dlog.error(workerSendNode.pos, DiagnosticErrorCode.INVALID_TYPE_FOR_SEND, type);
        }

        String workerName = workerSendNode.workerIdentifier.getValue();
        if (data.withinQuery || (!isCommunicationAllowedLocation(data.env) && !data.inInternallyDefinedBlockStmt)) {
            this.dlog.error(workerSendNode.pos, DiagnosticErrorCode.UNSUPPORTED_WORKER_SEND_POSITION);
            was.hasErrors = true;
        }

        if (!this.workerExists(workerSendNode.getBType(), workerName, data.env)
                || (!isWorkerFromFunction(data.env, names.fromString(workerName)) && !workerName.equals("function"))) {
            this.dlog.error(workerSendNode.pos, DiagnosticErrorCode.UNDEFINED_WORKER, workerName);
            was.hasErrors = true;
        }

        workerSendNode.setBType(
                createAccumulatedErrorTypeForMatchingReceive(workerSendNode.pos, workerSendNode.expr.getBType(), data));
        was.addWorkerAction(workerSendNode);
        analyzeExpr(workerSendNode.expr, data);
        validateActionParentNode(workerSendNode.pos, workerSendNode.expr);
    }

    private BType createAccumulatedErrorTypeForMatchingReceive(Location pos, BType exprType, AnalyzerData data) {
        Set<BType> returnTypesUpToNow = data.returnTypes.peek();
        LinkedHashSet<BType> returnTypeAndSendType = new LinkedHashSet<>() {
            {
                Comparator.comparing(BType::toString);
            }
        };
        for (BType returnType : returnTypesUpToNow) {
            if (onlyContainErrors(returnType)) {
                returnTypeAndSendType.add(returnType);
            } else {
                this.dlog.error(pos, DiagnosticErrorCode.WORKER_SEND_AFTER_RETURN);
            }
        }
        returnTypeAndSendType.add(exprType);
        if (returnTypeAndSendType.size() > 1) {
            return BUnionType.create(null, returnTypeAndSendType);
        } else {
            return exprType;
        }
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr, AnalyzerData data) {
        BSymbol receiver =
                symResolver.lookupSymbolInMainSpace(data.env, names.fromIdNode(syncSendExpr.workerIdentifier));
        if ((receiver.tag & SymTag.VARIABLE) != SymTag.VARIABLE) {
            receiver = symTable.notFoundSymbol;
        }
        verifyPeerCommunication(syncSendExpr.pos, receiver, syncSendExpr.workerIdentifier.value, data.env);

        // Validate worker synchronous send
        validateActionParentNode(syncSendExpr.pos, syncSendExpr);
        String workerName = syncSendExpr.workerIdentifier.getValue();
        WorkerActionSystem was = data.workerActionSystemStack.peek();

        if (data.withinQuery || (!isCommunicationAllowedLocation(data.env) && !data.inInternallyDefinedBlockStmt)) {
            this.dlog.error(syncSendExpr.pos, DiagnosticErrorCode.UNSUPPORTED_WORKER_SEND_POSITION);
            was.hasErrors = true;
        }

        if (!this.workerExists(syncSendExpr.workerType, workerName, data.env)) {
            this.dlog.error(syncSendExpr.pos, DiagnosticErrorCode.UNDEFINED_WORKER, syncSendExpr.workerSymbol);
            was.hasErrors = true;
        }
        syncSendExpr.setBType(
                createAccumulatedErrorTypeForMatchingReceive(syncSendExpr.pos, syncSendExpr.expr.getBType(), data));
        was.addWorkerAction(syncSendExpr);
        analyzeExpr(syncSendExpr.expr, data);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode, AnalyzerData data) {
        // Validate worker receive
        validateActionParentNode(workerReceiveNode.pos, workerReceiveNode);
        BSymbol sender =
                symResolver.lookupSymbolInMainSpace(data.env, names.fromIdNode(workerReceiveNode.workerIdentifier));
        if ((sender.tag & SymTag.VARIABLE) != SymTag.VARIABLE) {
            sender = symTable.notFoundSymbol;
        }
        verifyPeerCommunication(workerReceiveNode.pos, sender, workerReceiveNode.workerIdentifier.value, data.env);

        WorkerActionSystem was = data.workerActionSystemStack.peek();

        String workerName = workerReceiveNode.workerIdentifier.getValue();
        if (data.withinQuery || (!isCommunicationAllowedLocation(data.env) && !data.inInternallyDefinedBlockStmt)) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticErrorCode.INVALID_WORKER_RECEIVE_POSITION);
            was.hasErrors = true;
        }

        if (!this.workerExists(workerReceiveNode.workerType, workerName, data.env)) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticErrorCode.UNDEFINED_WORKER, workerName);
            was.hasErrors = true;
        }

        workerReceiveNode.matchingSendsError = createAccumulatedErrorTypeForMatchingSyncSend(workerReceiveNode, data);
        was.addWorkerAction(workerReceiveNode);
    }

    private void verifyPeerCommunication(Location pos, BSymbol otherWorker, String otherWorkerName, SymbolEnv env) {
        if (env.enclEnv.node.getKind() != NodeKind.FUNCTION) {
            return;
        }
        BLangFunction funcNode = (BLangFunction) env.enclEnv.node;

        Set<Flag> flagSet = funcNode.flagSet;
        // Analyze worker interactions inside workers
        Name workerDerivedName = names.fromString("0" + otherWorker.name.value);
        if (flagSet.contains(Flag.WORKER)) {
            // Interacting with default worker from a worker within a fork.
            if (otherWorkerName.equals(DEFAULT_WORKER_NAME)) {
                if (flagSet.contains(Flag.FORKED)) {
                    dlog.error(pos, DiagnosticErrorCode.WORKER_INTERACTIONS_ONLY_ALLOWED_BETWEEN_PEERS);
                }
                return;
            }

            Scope enclFunctionScope = env.enclEnv.enclEnv.scope;
            BInvokableSymbol wLambda = (BInvokableSymbol) enclFunctionScope.lookup(workerDerivedName).symbol;
            // Interactions across fork
            if (wLambda != null && funcNode.anonForkName != null
                    && !funcNode.anonForkName.equals(wLambda.enclForkName)) {
                dlog.error(pos, DiagnosticErrorCode.WORKER_INTERACTIONS_ONLY_ALLOWED_BETWEEN_PEERS);
            }
        } else {
            // Worker interactions outside of worker constructs (in default worker)
            BInvokableSymbol wLambda = (BInvokableSymbol) env.scope.lookup(workerDerivedName).symbol;
            if (wLambda != null && wLambda.enclForkName != null) {
                dlog.error(pos, DiagnosticErrorCode.WORKER_INTERACTIONS_ONLY_ALLOWED_BETWEEN_PEERS);
            }
        }
    }

    public BType createAccumulatedErrorTypeForMatchingSyncSend(BLangWorkerReceive workerReceiveNode,
                                                               AnalyzerData data) {
        Set<BType> returnTypesUpToNow = data.returnTypes.peek();
        LinkedHashSet<BType> returnTypeAndSendType = new LinkedHashSet<>();
        for (BType returnType : returnTypesUpToNow) {
            if (onlyContainErrors(returnType)) {
                returnTypeAndSendType.add(returnType);
            } else {
                this.dlog.error(workerReceiveNode.pos, DiagnosticErrorCode.WORKER_RECEIVE_AFTER_RETURN);
            }
        }
        returnTypeAndSendType.add(symTable.nilType);
        if (returnTypeAndSendType.size() > 1) {
            return BUnionType.create(null, returnTypeAndSendType);
        } else {
            return symTable.nilType;
        }
    }

    private boolean onlyContainErrors(BType returnType) {
        if (returnType == null) {
            return false;
        }

        returnType = types.getTypeWithEffectiveIntersectionTypes(returnType);
        returnType = Types.getReferredType(returnType);
        if (returnType.tag == TypeTags.ERROR) {
            return true;
        }

        if (returnType.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) returnType).getMemberTypes()) {
                BType t = types.getTypeWithEffectiveIntersectionTypes(memberType);
                if (t.tag != TypeTags.ERROR) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void visit(BLangLiteral literalExpr, AnalyzerData data) {
    }


    @Override
    public void visit(BLangConstRef constRef, AnalyzerData data) {
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr, AnalyzerData data) {
        for (BLangExpression expr : listConstructorExpr.exprs) {
            if (expr.getKind() == NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                expr = ((BLangListConstructorSpreadOpExpr) expr).expr;
            }
            analyzeExpr(expr, data);
        }
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr, AnalyzerData data) {
        analyzeExprs(tableConstructorExpr.recordLiteralList, data);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral, AnalyzerData data) {
        List<RecordLiteralNode.RecordField> fields = recordLiteral.fields;

        for (RecordLiteralNode.RecordField field : fields) {
            if (field.isKeyValueField()) {
                analyzeExpr(((BLangRecordKeyValueField) field).valueExpr, data);
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                analyzeExpr((BLangRecordLiteral.BLangRecordVarNameField) field, data);
            } else {
                analyzeExpr(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr, data);
            }
        }

        Set<Object> names = new HashSet<>();
        Set<Object> neverTypedKeys = new HashSet<>();
        BType literalBType = recordLiteral.getBType();
        BType type = Types.getReferredType(literalBType);
        boolean isRecord = type.tag == TypeTags.RECORD;
        boolean isOpenRecord = isRecord && !((BRecordType) type).sealed;

        // A record type is inferred for a record literal even if the contextually expected type is a map, if the
        // mapping constructor expression has `readonly` fields.
        boolean isInferredRecordForMapCET = isRecord && recordLiteral.expectedType != null &&
                recordLiteral.expectedType.tag == TypeTags.MAP;

        BLangRecordLiteral.BLangRecordSpreadOperatorField inclusiveTypeSpreadField = null;
        for (RecordLiteralNode.RecordField field : fields) {

            BLangExpression keyExpr;

            if (field.getKind() == NodeKind.RECORD_LITERAL_SPREAD_OP) {
                BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOpField =
                        (BLangRecordLiteral.BLangRecordSpreadOperatorField) field;
                BLangExpression spreadOpExpr = spreadOpField.expr;

                analyzeExpr(spreadOpExpr, data);

                BType spreadOpExprType = Types.getReferredType(spreadOpExpr.getBType());
                int spreadFieldTypeTag = spreadOpExprType.tag;
                if (spreadFieldTypeTag == TypeTags.MAP) {
                    if (inclusiveTypeSpreadField != null) {
                        this.dlog.error(spreadOpExpr.pos, DiagnosticErrorCode.MULTIPLE_INCLUSIVE_TYPES);
                        continue;
                    }
                    inclusiveTypeSpreadField = spreadOpField;

                    if (fields.size() > 1) {
                        if (names.size() > 0) {
                            this.dlog.error(spreadOpExpr.pos,
                                            DiagnosticErrorCode.SPREAD_FIELD_MAY_DULPICATE_ALREADY_SPECIFIED_KEYS,
                                            spreadOpExpr);
                        }
                        // Skipping to avoid multiple error messages
                        continue;
                    }
                }

                if (spreadFieldTypeTag != TypeTags.RECORD) {
                    continue;
                }

                BRecordType spreadExprRecordType = (BRecordType) spreadOpExprType;
                boolean isSpreadExprRecordTypeSealed = spreadExprRecordType.sealed;
                if (!isSpreadExprRecordTypeSealed) {
                    // More than one spread-field with inclusive-type-descriptors are not allowed.
                    if (inclusiveTypeSpreadField != null) {
                        this.dlog.error(spreadOpExpr.pos, DiagnosticErrorCode.MULTIPLE_INCLUSIVE_TYPES);
                    } else {
                        inclusiveTypeSpreadField = spreadOpField;
                    }
                }

                LinkedHashMap<String, BField> fieldsInRecordType = getUnescapedFieldList(spreadExprRecordType.fields);

                for (Object fieldName : names) {
                    if (!fieldsInRecordType.containsKey(fieldName) && !isSpreadExprRecordTypeSealed) {
                        this.dlog.error(spreadOpExpr.pos,
                                DiagnosticErrorCode.SPREAD_FIELD_MAY_DULPICATE_ALREADY_SPECIFIED_KEYS,
                                spreadOpExpr);
                        break;
                    }
                }

                for (String fieldName : fieldsInRecordType.keySet()) {
                    BField bField = fieldsInRecordType.get(fieldName);
                    if (names.contains(fieldName)) {
                        if (bField.type.tag != TypeTags.NEVER) {
                            this.dlog.error(spreadOpExpr.pos,
                                    DiagnosticErrorCode.DUPLICATE_KEY_IN_RECORD_LITERAL_SPREAD_OP,
                                    type.getKind().typeName(), fieldName, spreadOpField);
                        }
                        continue;
                    }

                    if (bField.type.tag == TypeTags.NEVER) {
                        neverTypedKeys.add(fieldName);
                        continue;
                    }

                    if (!neverTypedKeys.remove(fieldName) &&
                            inclusiveTypeSpreadField != null && isSpreadExprRecordTypeSealed) {
                        this.dlog.error(spreadOpExpr.pos,
                                DiagnosticErrorCode.POSSIBLE_DUPLICATE_OF_FIELD_SPECIFIED_VIA_SPREAD_OP,
                                Types.getReferredType(recordLiteral.expectedType).getKind().typeName(),
                                bField.symbol, spreadOpField);
                    }
                    names.add(fieldName);
                }

            } else {
                if (field.isKeyValueField()) {
                    BLangRecordLiteral.BLangRecordKey key = ((BLangRecordKeyValueField) field).key;
                    keyExpr = key.expr;
                    if (key.computedKey) {
                        analyzeExpr(keyExpr, data);
                        continue;
                    }
                } else {
                    keyExpr = (BLangRecordLiteral.BLangRecordVarNameField) field;
                }

                if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    String name = ((BLangSimpleVarRef) keyExpr).variableName.value;
                    String unescapedName = Utils.unescapeJava(name);
                    if (names.contains(unescapedName)) {
                        this.dlog.error(keyExpr.pos, DiagnosticErrorCode.DUPLICATE_KEY_IN_RECORD_LITERAL,
                                        Types.getReferredType(recordLiteral.expectedType).getKind().typeName(),
                                        unescapedName);
                    } else if (inclusiveTypeSpreadField != null && !neverTypedKeys.contains(unescapedName)) {
                        this.dlog.error(keyExpr.pos,
                                        DiagnosticErrorCode.POSSIBLE_DUPLICATE_OF_FIELD_SPECIFIED_VIA_SPREAD_OP,
                                unescapedName, inclusiveTypeSpreadField);
                    }

                    if (!isInferredRecordForMapCET && isOpenRecord && !((BRecordType) type).fields.containsKey(name)) {
                        dlog.error(keyExpr.pos, DiagnosticErrorCode.INVALID_RECORD_LITERAL_IDENTIFIER_KEY,
                                unescapedName);
                    }

                    names.add(unescapedName);
                } else if (keyExpr.getKind() == NodeKind.LITERAL || keyExpr.getKind() == NodeKind.NUMERIC_LITERAL) {
                    Object name = ((BLangLiteral) keyExpr).value;
                    if (names.contains(name)) {
                        this.dlog.error(keyExpr.pos, DiagnosticErrorCode.DUPLICATE_KEY_IN_RECORD_LITERAL,
                                Types.getReferredType(recordLiteral.parent.getBType())
                                        .getKind().typeName(), name);
                    } else if (inclusiveTypeSpreadField != null && !neverTypedKeys.contains(name)) {
                        this.dlog.error(keyExpr.pos,
                                        DiagnosticErrorCode.POSSIBLE_DUPLICATE_OF_FIELD_SPECIFIED_VIA_SPREAD_OP,
                                        name, inclusiveTypeSpreadField);
                    }
                    names.add(name);
                }
            }
        }

        if (isInferredRecordForMapCET) {
            recordLiteral.expectedType = type;
        }
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordVarNameField node, AnalyzerData data) {
        visit((BLangSimpleVarRef) node, data);
    }

    private LinkedHashMap<String, BField> getUnescapedFieldList(LinkedHashMap<String, BField> fieldMap) {
        LinkedHashMap<String, BField> newMap = new LinkedHashMap<>();
        for (String key : fieldMap.keySet()) {
            newMap.put(Utils.unescapeJava(key), fieldMap.get(key));
        }

        return newMap;
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr, AnalyzerData data) {
        switch (varRefExpr.parent.getKind()) {
            // Referring workers for worker interactions are allowed, hence skip the check.
            case WORKER_RECEIVE:
            case WORKER_SEND:
            case WORKER_SYNC_SEND:
                return;
            default:
                if (varRefExpr.getBType() != null && varRefExpr.getBType().tag == TypeTags.FUTURE) {
                    trackNamedWorkerReferences(varRefExpr, data);
                }
        }

        BSymbol symbol = varRefExpr.symbol;
        if (symbol != null && Symbols.isFlagOn(symbol.flags, Flags.DEPRECATED)) {
            logDeprecatedWaring(varRefExpr.variableName.toString(), symbol, varRefExpr.pos);
        }
    }

    private void trackNamedWorkerReferences(BLangSimpleVarRef varRefExpr, AnalyzerData data) {
        if (varRefExpr.symbol == null || (varRefExpr.symbol.flags & Flags.WORKER) != Flags.WORKER) {
            return;
        }

        data.workerReferences.computeIfAbsent(varRefExpr.symbol, s -> new LinkedHashSet<>());
        data.workerReferences.get(varRefExpr.symbol).add(varRefExpr);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr, AnalyzerData data) {
        analyzeFieldBasedAccessExpr(fieldAccessExpr, data);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess nsPrefixedFieldBasedAccess,
                      AnalyzerData data) {
        analyzeFieldBasedAccessExpr(nsPrefixedFieldBasedAccess, data);
    }

    private void analyzeFieldBasedAccessExpr(BLangFieldBasedAccess fieldAccessExpr, AnalyzerData data) {
        BLangExpression expr = fieldAccessExpr.expr;
        analyzeExpr(expr, data);
        BSymbol symbol = fieldAccessExpr.symbol;
        if (symbol != null && Symbols.isFlagOn(fieldAccessExpr.symbol.flags, Flags.DEPRECATED)) {
            String deprecatedConstruct = generateDeprecatedConstructString(expr, fieldAccessExpr.field.toString(),
                    symbol);
            dlog.warning(fieldAccessExpr.pos, DiagnosticWarningCode.USAGE_OF_DEPRECATED_CONSTRUCT, deprecatedConstruct);
        }
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr, AnalyzerData data) {
        analyzeExpr(indexAccessExpr.indexExpr, data);
        analyzeExpr(indexAccessExpr.expr, data);
    }

    @Override
    public void visit(BLangInvocation invocationExpr, AnalyzerData data) {
        analyzeExpr(invocationExpr.expr, data);
        analyzeExprs(invocationExpr.requiredArgs, data);
        analyzeExprs(invocationExpr.restArgs, data);

        validateInvocationInMatchGuard(invocationExpr);

        if ((invocationExpr.symbol != null) && invocationExpr.symbol.kind == SymbolKind.FUNCTION) {
            BSymbol funcSymbol = invocationExpr.symbol;
            if (Symbols.isFlagOn(funcSymbol.flags, Flags.TRANSACTIONAL) && !data.withinTransactionScope) {
                dlog.error(invocationExpr.pos, DiagnosticErrorCode.TRANSACTIONAL_FUNC_INVOKE_PROHIBITED);
                return;
            }
            if (Symbols.isFlagOn(funcSymbol.flags, Flags.DEPRECATED)) {
                logDeprecatedWarningForInvocation(invocationExpr);
            }
        }
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr, AnalyzerData data) {
        analyzeExprs(errorConstructorExpr.positionalArgs, data);
        if (!errorConstructorExpr.namedArgs.isEmpty()) {
            analyzeExprs(errorConstructorExpr.namedArgs, data);
        }
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocation, AnalyzerData data) {
        validateInvocationInMatchGuard(actionInvocation);

        if (!actionInvocation.async && !data.withinTransactionScope &&
                Symbols.isFlagOn(actionInvocation.symbol.flags, Flags.TRANSACTIONAL)) {
            dlog.error(actionInvocation.pos, DiagnosticErrorCode.TRANSACTIONAL_FUNC_INVOKE_PROHIBITED,
                       actionInvocation.symbol);
            return;
        }

        if (actionInvocation.async && data.withinTransactionScope &&
                !Symbols.isFlagOn(actionInvocation.symbol.flags, Flags.TRANSACTIONAL)) {
            dlog.error(actionInvocation.pos, DiagnosticErrorCode.USAGE_OF_START_WITHIN_TRANSACTION_IS_PROHIBITED);
            return;
        }

        analyzeExpr(actionInvocation.expr, data);
        analyzeExprs(actionInvocation.requiredArgs, data);
        analyzeExprs(actionInvocation.restArgs, data);

        if (actionInvocation.symbol != null && actionInvocation.symbol.kind == SymbolKind.FUNCTION &&
                Symbols.isFlagOn(actionInvocation.symbol.flags, Flags.DEPRECATED)) {
            logDeprecatedWarningForInvocation(actionInvocation);
        }

        if (actionInvocation.flagSet.contains(Flag.TRANSACTIONAL) && !data.withinTransactionScope) {
            dlog.error(actionInvocation.pos, DiagnosticErrorCode.TRANSACTIONAL_FUNC_INVOKE_PROHIBITED);
            return;
        }

        if (actionInvocation.async && data.withinLockBlock) {
            dlog.error(actionInvocation.pos, actionInvocation.functionPointerInvocation ?
                    DiagnosticErrorCode.USAGE_OF_WORKER_WITHIN_LOCK_IS_PROHIBITED :
                    DiagnosticErrorCode.USAGE_OF_START_WITHIN_LOCK_IS_PROHIBITED);
            return;
        }

        if (actionInvocation.symbol != null &&
                (actionInvocation.symbol.tag & SymTag.CONSTRUCTOR) == SymTag.CONSTRUCTOR) {
            dlog.error(actionInvocation.pos, DiagnosticErrorCode.INVALID_FUNCTIONAL_CONSTRUCTOR_INVOCATION,
                    actionInvocation.symbol);
            return;
        }

        validateActionInvocation(actionInvocation.pos, actionInvocation);

        if (!actionInvocation.async && data.withinTransactionScope) {
            actionInvocation.invokedInsideTransaction = true;
        }
    }
    
    @Override
    public void visit(BLangInvocation.BLangResourceAccessInvocation resourceActionInvocation, AnalyzerData data) {
        validateInvocationInMatchGuard(resourceActionInvocation);
        analyzeExpr(resourceActionInvocation.expr, data);
        analyzeExprs(resourceActionInvocation.requiredArgs, data);
        analyzeExprs(resourceActionInvocation.restArgs, data);
        analyzeExpr(resourceActionInvocation.resourceAccessPathSegments, data);
        resourceActionInvocation.invokedInsideTransaction = data.withinTransactionScope;
        
        if (Symbols.isFlagOn(resourceActionInvocation.symbol.flags, Flags.TRANSACTIONAL) &&
                !data.withinTransactionScope) {
            dlog.error(resourceActionInvocation.pos, DiagnosticErrorCode.TRANSACTIONAL_FUNC_INVOKE_PROHIBITED);
            return;
        }
        
        if (Symbols.isFlagOn(resourceActionInvocation.symbol.flags, Flags.DEPRECATED)) {
            logDeprecatedWarningForInvocation(resourceActionInvocation);
        }

        validateActionInvocation(resourceActionInvocation.pos, resourceActionInvocation);
    }

    private void logDeprecatedWarningForInvocation(BLangInvocation invocationExpr) {
        String deprecatedConstruct = invocationExpr.name.toString();
        BLangExpression expr = invocationExpr.expr;
        BSymbol funcSymbol = invocationExpr.symbol;

        if (expr != null) {
            // Method call
            deprecatedConstruct = generateDeprecatedConstructString(expr, deprecatedConstruct, funcSymbol);
        } else if (!Names.DOT.equals(funcSymbol.pkgID.name)) {
            deprecatedConstruct = funcSymbol.pkgID + ":" + deprecatedConstruct;
        }

        dlog.warning(invocationExpr.pos, DiagnosticWarningCode.USAGE_OF_DEPRECATED_CONSTRUCT, deprecatedConstruct);
    }

    private String generateDeprecatedConstructString(BLangExpression expr, String fieldOrMethodName,
                                                     BSymbol symbol) {
        BType bType = expr.getBType();
        if (bType.tag == TypeTags.TYPEREFDESC) {
            return bType + "." + fieldOrMethodName;
        }

        if (bType.tag == TypeTags.OBJECT) {
            BObjectType objectType = (BObjectType) bType;
            // for anonymous objects, only the field name will be in the error msg
            if (objectType.classDef == null || objectType.classDef.internal == false) {
                fieldOrMethodName = bType + "." + fieldOrMethodName;
            }
            return fieldOrMethodName;
        }

        if (symbol.kind == SymbolKind.FUNCTION && !Names.DOT.equals(symbol.pkgID.name)) {
            // for deprecated lang lib methods
            fieldOrMethodName = symbol.pkgID + ":" + fieldOrMethodName;
        }

        return fieldOrMethodName;
    }

    private void validateActionInvocation(Location pos, BLangInvocation iExpr) {
        if (iExpr.expr != null) {
            final NodeKind clientNodeKind = iExpr.expr.getKind();
            // Validation against node kind.
            if (clientNodeKind == NodeKind.FIELD_BASED_ACCESS_EXPR) {
                final BLangFieldBasedAccess fieldBasedAccess = (BLangFieldBasedAccess) iExpr.expr;
                if (fieldBasedAccess.expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                    dlog.error(pos, DiagnosticErrorCode.INVALID_ACTION_INVOCATION_AS_EXPR);
                } else {
                    final BLangSimpleVarRef selfName = (BLangSimpleVarRef) fieldBasedAccess.expr;
                    if (!Names.SELF.equals(selfName.symbol.name)) {
                        dlog.error(pos, DiagnosticErrorCode.INVALID_ACTION_INVOCATION_AS_EXPR);
                    }
                }
            } else if (clientNodeKind != NodeKind.SIMPLE_VARIABLE_REF &&
                    clientNodeKind != NodeKind.GROUP_EXPR) {
                dlog.error(pos, DiagnosticErrorCode.INVALID_ACTION_INVOCATION_AS_EXPR);
            }
        }
        validateActionParentNode(pos, iExpr);
    }

    /**
     * Actions can only occur as part of a statement or nested inside other actions.
     */
    private boolean validateActionParentNode(Location pos, BLangNode node) {
        // Validate for parent nodes.
        BLangNode parent = node.parent;

        while (parent != null) {
            final NodeKind kind = parent.getKind();
            if (parent instanceof StatementNode || checkActionInQuery(kind)) {
                return true;
            } else if (parent instanceof ActionNode || parent instanceof BLangVariable || kind == NodeKind.CHECK_EXPR ||
                    kind == NodeKind.CHECK_PANIC_EXPR || kind == NodeKind.TRAP_EXPR || kind == NodeKind.GROUP_EXPR ||
                    kind == NodeKind.TYPE_CONVERSION_EXPR) {
                if (parent instanceof BLangInvocation.BLangActionInvocation) {
                    // Prevent use of actions as arguments in a call
                    break;
                }

                parent = parent.parent;
                continue;
            }
            break;
        }
        dlog.error(pos, DiagnosticErrorCode.INVALID_ACTION_INVOCATION_AS_EXPR);
        return false;
    }

    private boolean checkActionInQuery(NodeKind parentKind) {
        return parentKind == NodeKind.FROM || parentKind == NodeKind.SELECT ||
                parentKind == NodeKind.LET_CLAUSE;
    }

    @Override
    public void visit(BLangTypeInit cIExpr, AnalyzerData data) {
        analyzeExprs(cIExpr.argsExpr, data);
        analyzeExpr(cIExpr.initInvocation, data);
        BType type = cIExpr.getBType();
        if (cIExpr.userDefinedType != null && Symbols.isFlagOn(type.tsymbol.flags, Flags.DEPRECATED)) {
            logDeprecatedWaring(((BLangUserDefinedType) cIExpr.userDefinedType).typeName.toString(), type.tsymbol,
                    cIExpr.pos);
        }
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr, AnalyzerData data) {
        analyzeExpr(ternaryExpr.expr, data);
        analyzeExpr(ternaryExpr.thenExpr, data);
        analyzeExpr(ternaryExpr.elseExpr, data);
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr, AnalyzerData data) {
        BLangExpression expr = awaitExpr.getExpression();
        boolean validWaitFuture = validateWaitFutureExpr(expr);
        analyzeExpr(expr, data);
        boolean validActionParent = validateActionParentNode(awaitExpr.pos, awaitExpr);

        WorkerActionSystem was = data.workerActionSystemStack.peek();
        was.addWorkerAction(awaitExpr, data.env);
        was.hasErrors = !(validWaitFuture || validActionParent);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr, AnalyzerData data) {
        boolean validWaitFuture = true;
        for (BLangWaitForAllExpr.BLangWaitKeyValue keyValue : waitForAllExpr.keyValuePairs) {
            BLangExpression expr = keyValue.valueExpr != null ? keyValue.valueExpr : keyValue.keyExpr;
            validWaitFuture = validWaitFuture && validateWaitFutureExpr(expr);
            analyzeExpr(expr, data);
        }

        boolean validActionParent = validateActionParentNode(waitForAllExpr.pos, waitForAllExpr);

        WorkerActionSystem was = data.workerActionSystemStack.peek();
        was.addWorkerAction(waitForAllExpr, data.env);
        was.hasErrors = !(validWaitFuture || validActionParent);
    }

    // wait-future-expr := expression but not mapping-constructor-expr
    private boolean validateWaitFutureExpr(BLangExpression expr) {
        if (expr.getKind() == NodeKind.RECORD_LITERAL_EXPR) {
            dlog.error(expr.pos, DiagnosticErrorCode.INVALID_WAIT_MAPPING_CONSTRUCTORS);
            return false;
        }

        if (expr instanceof ActionNode) {
            dlog.error(expr.pos, DiagnosticErrorCode.INVALID_WAIT_ACTIONS);
            return false;
        }
        return true;
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess, AnalyzerData data) {
        analyzeExpr(xmlElementAccess.expr, data);
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation, AnalyzerData data) {
        analyzeExpr(xmlNavigation.expr, data);
        if (xmlNavigation.childIndex != null) {
            if (xmlNavigation.navAccessType == XMLNavigationAccess.NavAccessType.DESCENDANTS
                    || xmlNavigation.navAccessType == XMLNavigationAccess.NavAccessType.CHILDREN) {
                dlog.error(xmlNavigation.pos, DiagnosticErrorCode.UNSUPPORTED_MEMBER_ACCESS_IN_XML_NAVIGATION);
            }
            analyzeExpr(xmlNavigation.childIndex, data);
        }
        validateMethodInvocationsInXMLNavigationExpression(xmlNavigation);
    }

    private void validateMethodInvocationsInXMLNavigationExpression(BLangXMLNavigationAccess expression) {
        if (!expression.methodInvocationAnalyzed && expression.parent.getKind() == NodeKind.INVOCATION) {
            BLangInvocation invocation = (BLangInvocation) expression.parent;
            // avoid langlib invocations re-written to have the receiver as first argument.
            if (invocation.argExprs.contains(expression)
                    && ((invocation.symbol.flags & Flags.LANG_LIB) != Flags.LANG_LIB)) {
                return;
            }

            dlog.error(invocation.pos, DiagnosticErrorCode.UNSUPPORTED_METHOD_INVOCATION_XML_NAV);
        }
        expression.methodInvocationAnalyzed = true;
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr, AnalyzerData data) {
        // Two scenarios should be handled
        // 1) flush w1 -> Wait till all the asynchronous sends to worker w1 is completed
        // 2) flush -> Wait till all asynchronous sends to all workers are completed
        BLangIdentifier flushWrkIdentifier = workerFlushExpr.workerIdentifier;
        Stack<WorkerActionSystem> workerActionSystems = data.workerActionSystemStack;
        WorkerActionSystem currentWrkerAction = workerActionSystems.peek();
        List<BLangWorkerSend> sendStmts = getAsyncSendStmtsOfWorker(currentWrkerAction);
        if (flushWrkIdentifier != null) {
            List<BLangWorkerSend> sendsToGivenWrkr = sendStmts.stream()
                                                              .filter(bLangNode -> bLangNode.workerIdentifier.equals
                                                                      (flushWrkIdentifier))
                                                              .collect(Collectors.toList());
            if (sendsToGivenWrkr.size() == 0) {
                this.dlog.error(workerFlushExpr.pos, DiagnosticErrorCode.INVALID_WORKER_FLUSH_FOR_WORKER,
                                workerFlushExpr.workerSymbol, currentWrkerAction.currentWorkerId());
                return;
            } else {
                sendStmts = sendsToGivenWrkr;
            }
        } else {
            if (sendStmts.size() == 0) {
                this.dlog.error(workerFlushExpr.pos, DiagnosticErrorCode.INVALID_WORKER_FLUSH,
                                currentWrkerAction.currentWorkerId());
                return;
            }
        }
        workerFlushExpr.cachedWorkerSendStmts = sendStmts;
        validateActionParentNode(workerFlushExpr.pos, workerFlushExpr);
    }

    private List<BLangWorkerSend> getAsyncSendStmtsOfWorker(WorkerActionSystem currentWorkerAction) {
        List<BLangNode> actions = currentWorkerAction.workerActionStateMachines.peek().actions;
        return actions.stream()
                      .filter(CodeAnalyzer::isWorkerSend)
                      .map(bLangNode -> (BLangWorkerSend) bLangNode)
                      .collect(Collectors.toList());
    }
    @Override
    public void visit(BLangTrapExpr trapExpr, AnalyzerData data) {
        analyzeExpr(trapExpr.expr, data);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr, AnalyzerData data) {
        if (validateBinaryExpr(binaryExpr)) {
            analyzeExpr(binaryExpr.lhsExpr, data);
            analyzeExpr(binaryExpr.rhsExpr, data);
        }
    }

    private boolean validateBinaryExpr(BLangBinaryExpr binaryExpr) {
        // 1) For usual binary expressions the lhs or rhs can never be future types, so return true if both of
        // them are not future types
        if (binaryExpr.lhsExpr.getBType().tag != TypeTags.FUTURE
                && binaryExpr.rhsExpr.getBType().tag != TypeTags.FUTURE) {
            return true;
        }

        // 2) For binary expressions followed with wait lhs and rhs are always future types and this is allowed so
        // return true : wait f1 | f2[orgName + moduleName
        BLangNode parentNode = binaryExpr.parent;
        if (binaryExpr.lhsExpr.getBType().tag == TypeTags.FUTURE
                || binaryExpr.rhsExpr.getBType().tag == TypeTags.FUTURE) {
            if (parentNode == null) {
                return false;
            }
            if (parentNode.getKind() == NodeKind.WAIT_EXPR) {
                return true;
            }
        }

        // 3) For binary expressions of future type which are not followed by the wait expression are not allowed.
        // So check if immediate parent is a binary expression and if the current binary expression operator kind
        // is bitwise OR
        if (parentNode.getKind() != NodeKind.BINARY_EXPR && binaryExpr.opKind == OperatorKind.BITWISE_OR) {
            dlog.error(binaryExpr.pos, DiagnosticErrorCode.OPERATOR_NOT_SUPPORTED, OperatorKind.BITWISE_OR,
                       symTable.futureType);
                return false;
        }

        if (parentNode.getKind() == NodeKind.BINARY_EXPR) {
            return validateBinaryExpr((BLangBinaryExpr) parentNode);
        }
        return true;
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr, AnalyzerData data) {
        analyzeExpr(elvisExpr.lhsExpr, data);
        analyzeExpr(elvisExpr.rhsExpr, data);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr, AnalyzerData data) {
        analyzeExpr(groupExpr.expression, data);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr, AnalyzerData data) {
        analyzeExpr(unaryExpr.expr, data);
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr, AnalyzerData data) {
        analyzeExpr(conversionExpr.expr, data);
        conversionExpr.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, data));
    }

    @Override
    public void visit(BLangXMLQName xmlQName, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute, AnalyzerData data) {
        analyzeExpr(xmlAttribute.name, data);
        analyzeExpr(xmlAttribute.value, data);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral, AnalyzerData data) {
        analyzeExpr(xmlElementLiteral.startTagName, data);
        analyzeExpr(xmlElementLiteral.endTagName, data);
        analyzeExprs(xmlElementLiteral.attributes, data);
        analyzeExprs(xmlElementLiteral.children, data);
    }

    @Override
    public void visit(BLangXMLSequenceLiteral xmlSequenceLiteral, AnalyzerData data) {
        analyzeExprs(xmlSequenceLiteral.xmlItems, data);
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral, AnalyzerData data) {
        analyzeExprs(xmlTextLiteral.textFragments, data);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral, AnalyzerData data) {
        analyzeExprs(xmlCommentLiteral.textFragments, data);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral, AnalyzerData data) {
        analyzeExprs(xmlProcInsLiteral.dataFragments, data);
        analyzeExpr(xmlProcInsLiteral.target, data);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString, AnalyzerData data) {
        analyzeExprs(xmlQuotedString.textFragments, data);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral, AnalyzerData data) {
        analyzeExprs(stringTemplateLiteral.exprs, data);
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral, AnalyzerData data) {
        analyzeExprs(rawTemplateLiteral.strings, data);
        analyzeExprs(rawTemplateLiteral.insertions, data);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction, AnalyzerData data) {
        boolean isWorker = false;

        analyzeNode(bLangLambdaFunction.function, data);

        if (bLangLambdaFunction.function.flagSet.contains(Flag.TRANSACTIONAL) &&
                bLangLambdaFunction.function.flagSet.contains(Flag.WORKER) && !data.withinTransactionScope) {
            dlog.error(bLangLambdaFunction.pos, DiagnosticErrorCode.TRANSACTIONAL_WORKER_OUT_OF_TRANSACTIONAL_SCOPE,
                    bLangLambdaFunction);
            return;
        }
        if (bLangLambdaFunction.parent.getKind() == NodeKind.VARIABLE) {
            String workerVarName = ((BLangSimpleVariable) bLangLambdaFunction.parent).name.value;
            if (workerVarName.startsWith(WORKER_LAMBDA_VAR_PREFIX)) {
                String workerName = workerVarName.substring(1);
                isWorker = true;
                data.workerActionSystemStack.peek().startWorkerActionStateMachine(workerName,
                                                                                  bLangLambdaFunction.function.pos,
                                                                                  bLangLambdaFunction.function);
            }
        }
        // If this is a worker we are already in a worker action system,
        // if not we need to initiate a worker action system
        if (isWorker) {
            this.visitFunction(bLangLambdaFunction.function, data);
        } else {
            try {
                this.initNewWorkerActionSystem(data);
                data.workerActionSystemStack.peek().startWorkerActionStateMachine(DEFAULT_WORKER_NAME,
                        bLangLambdaFunction.pos,
                        bLangLambdaFunction.function);
                this.visitFunction(bLangLambdaFunction.function, data);
                data.workerActionSystemStack.peek().endWorkerActionStateMachine();
            } finally {
                this.finalizeCurrentWorkerActionSystem(data);
            }
        }

        if (isWorker) {
            data.workerActionSystemStack.peek().endWorkerActionStateMachine();
        }
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction, AnalyzerData data) {

        DefaultValueState prevDefaultValueState = data.defaultValueState;
        if (prevDefaultValueState == DefaultValueState.RECORD_FIELD_DEFAULT ||
                prevDefaultValueState == DefaultValueState.OBJECT_FIELD_INITIALIZER) {
            data.defaultValueState = DefaultValueState.FUNCTION_IN_DEFAULT_VALUE;
        }
        analyzeExpr(bLangArrowFunction.body.expr, data);
        data.defaultValueState = prevDefaultValueState;
    }

    /* Type Nodes */

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode, AnalyzerData data) {

        data.env = SymbolEnv.createTypeEnv(recordTypeNode, recordTypeNode.symbol.scope, data.env);
        for (BLangSimpleVariable field : recordTypeNode.fields) {
            DefaultValueState prevDefaultValueState = data.defaultValueState;
            data.defaultValueState = DefaultValueState.RECORD_FIELD_DEFAULT;
            analyzeNode(field, data);
            data.defaultValueState = prevDefaultValueState;
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode, AnalyzerData data) {

        data.env = SymbolEnv.createTypeEnv(objectTypeNode, objectTypeNode.symbol.scope, data.env);
        for (BLangSimpleVariable field : objectTypeNode.fields) {
            analyzeNode(field, data);
        }

        List<BLangFunction> bLangFunctionList = new ArrayList<>(objectTypeNode.functions);
        if (objectTypeNode.initFunction != null) {
            bLangFunctionList.add(objectTypeNode.initFunction);
        }

        // To ensure the order of the compile errors
        bLangFunctionList.sort(Comparator.comparingInt(function -> function.pos.lineRange().startLine().line()));
        for (BLangFunction function : bLangFunctionList) {
            analyzeNode(function, data);
        }
    }

    @Override
    public void visit(BLangValueType valueType, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangArrayType arrayType, AnalyzerData data) {
        if (containsInferredArraySizesOfHigherDimensions(arrayType.sizes)) {
            dlog.error(arrayType.pos, DiagnosticErrorCode.INFER_SIZE_ONLY_SUPPORTED_IN_FIRST_DIMENSION);
        } else if (isSizeInferredArray(arrayType.sizes) && !isValidInferredArray(arrayType.parent)) {
            dlog.error(arrayType.pos, DiagnosticErrorCode.CANNOT_INFER_SIZE_ARRAY_SIZE_FROM_THE_CONTEXT);
        }

        analyzeTypeNode(arrayType.elemtype, data);
    }

    private boolean isSizeInferredArray(List<BLangExpression> indexSizes) {
        return !indexSizes.isEmpty() && isInferredArrayIndicator(indexSizes.get(indexSizes.size() - 1));
    }

    private boolean isInferredArrayIndicator(BLangExpression size) {
        return size.getKind() == LITERAL && ((BLangLiteral) size).value.equals(Constants.INFERRED_ARRAY_INDICATOR);
    }

    private boolean containsInferredArraySizesOfHigherDimensions(List<BLangExpression> sizes) {
        if (sizes.size() < 2) {
            return false;
        }
        for (int i = 0; i < sizes.size() - 1; i++) {
            if (isInferredArrayIndicator(sizes.get(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangConstrainedType constrainedType, AnalyzerData data) {

        analyzeTypeNode(constrainedType.constraint, data);
    }

    @Override
    public void visit(BLangStreamType streamType, AnalyzerData data) {

        analyzeTypeNode(streamType.constraint, data);
        analyzeTypeNode(streamType.error, data);
    }

    @Override
    public void visit(BLangTableTypeNode tableType, AnalyzerData data) {

        analyzeTypeNode(tableType.constraint, data);

        if (tableType.tableKeyTypeConstraint != null) {
            analyzeTypeNode(tableType.tableKeyTypeConstraint.keyType, data);
        }
    }

    @Override
    public void visit(BLangErrorType errorType, AnalyzerData data) {
        BLangType detailType = errorType.detailType;
        if (detailType != null && detailType.getKind() == NodeKind.CONSTRAINED_TYPE) {
            BLangType constraint = ((BLangConstrainedType) detailType).constraint;
            if (constraint.getKind() == NodeKind.USER_DEFINED_TYPE) {
                BLangUserDefinedType userDefinedType = (BLangUserDefinedType) constraint;
                if (userDefinedType.typeName.value.equals(TypeDefBuilderHelper.INTERSECTED_ERROR_DETAIL)) {
                    // skip this as this is special case added to support error intersection where detail
                    // type is a map.
                    return;
                }
            }
        }

        analyzeTypeNode(errorType.detailType, data);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType, AnalyzerData data) {
        BTypeSymbol typeSymbol = userDefinedType.getBType().tsymbol;
        if (typeSymbol != null && Symbols.isFlagOn(typeSymbol.flags, Flags.DEPRECATED)) {
            logDeprecatedWaring(userDefinedType.typeName.toString(), typeSymbol, userDefinedType.pos);
        }
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode, AnalyzerData data) {

        tupleTypeNode.members.forEach(member -> analyzeNode(member, data));
        analyzeTypeNode(tupleTypeNode.restParamType, data);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode, AnalyzerData data) {

        unionTypeNode.memberTypeNodes.forEach(memberType -> analyzeTypeNode(memberType, data));
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode, AnalyzerData data) {

        for (BLangType constituentTypeNode : intersectionTypeNode.constituentTypeNodes) {
            analyzeTypeNode(constituentTypeNode, data);
        }
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode, AnalyzerData data) {
        if (functionTypeNode.flagSet.contains(Flag.ANY_FUNCTION)) {
            return;
        }
        functionTypeNode.params.forEach(node -> analyzeNode(node, data));
        analyzeTypeNode(functionTypeNode.returnTypeNode, data);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode, AnalyzerData data) {

        /* Ignore */
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression, AnalyzerData data) {

        analyzeExpr(bLangVarArgsExpression.expr, data);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression, AnalyzerData data) {

        analyzeExpr(bLangNamedArgsExpression.expr, data);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr, AnalyzerData data) {
        data.failVisited = true;
        analyzeExpr(checkedExpr.expr, data);

        if (data.env.scope.owner.getKind() == SymbolKind.PACKAGE) {
            // Check at module level.
            return;
        }

        BLangInvokableNode enclInvokable = data.env.enclInvokable;

        List<BType> equivalentErrorTypeList = checkedExpr.equivalentErrorTypeList;
        if (equivalentErrorTypeList != null && !equivalentErrorTypeList.isEmpty()) {
            if (data.defaultValueState == DefaultValueState.RECORD_FIELD_DEFAULT) {
                dlog.error(checkedExpr.pos,
                           DiagnosticErrorCode.INVALID_USAGE_OF_CHECK_IN_RECORD_FIELD_DEFAULT_EXPRESSION);
                return;
            }

            if (data.defaultValueState == DefaultValueState.OBJECT_FIELD_INITIALIZER) {
                BAttachedFunction initializerFunc =
                        ((BObjectTypeSymbol) getEnclosingClass(data.env).getBType().tsymbol).initializerFunc;

                if (initializerFunc == null) {
                    dlog.error(checkedExpr.pos,
                            DiagnosticErrorCode
                                    .INVALID_USAGE_OF_CHECK_IN_OBJECT_FIELD_INITIALIZER_IN_OBJECT_WITH_NO_INIT_METHOD);
                    return;
                }

                BType exprErrorTypes = getErrorTypes(checkedExpr.expr.getBType());
                BType initMethodReturnType = initializerFunc.type.retType;
                if (!types.isAssignable(exprErrorTypes, initMethodReturnType)) {
                    dlog.error(checkedExpr.pos, DiagnosticErrorCode
                            .INVALID_USAGE_OF_CHECK_IN_OBJECT_FIELD_INITIALIZER_WITH_INIT_METHOD_RETURN_TYPE_MISMATCH,
                            initMethodReturnType, exprErrorTypes);
                }
                return;
            }
        }

        if (enclInvokable == null) {
            return;
        }

        BType exprType = enclInvokable.getReturnTypeNode().getBType();
        BType checkedExprType = checkedExpr.expr.getBType();
        BType errorType = getErrorTypes(checkedExprType);

        if (errorType == symTable.semanticError) {
            return;
        }

        boolean ignoreErrForCheckExpr = data.withinQuery && data.queryConstructType == Types.QueryConstructType.STREAM;
        if (!data.failureHandled && !ignoreErrForCheckExpr && !types.isAssignable(errorType, exprType)
                && !types.isNeverTypeOrStructureTypeWithARequiredNeverMember(checkedExprType)) {
            dlog.error(checkedExpr.pos,
                    DiagnosticErrorCode.CHECKED_EXPR_NO_MATCHING_ERROR_RETURN_IN_ENCL_INVOKABLE);
        }
        if (!data.errorTypes.empty()) {
            data.errorTypes.peek().add(getErrorTypes(checkedExpr.expr.getBType()));
        }

        BType errorTypes;
        if (exprType.tag == TypeTags.UNION) {
            errorTypes = types.getErrorType((BUnionType) exprType);
        } else {
            errorTypes = exprType;
        }
        data.returnTypes.peek().add(errorTypes);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanicExpr, AnalyzerData data) {
        analyzeExpr(checkPanicExpr.expr, data);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangQueryExpr queryExpr, AnalyzerData data) {
        boolean prevQueryToTableWithKey = data.queryToTableWithKey;
        Types.QueryConstructType prevQueryConstructType = data.queryConstructType;
        data.queryConstructType = types.getQueryConstructType(queryExpr);
        data.queryToTableWithKey = queryExpr.isTable() && !queryExpr.fieldNameIdentifierList.isEmpty();
        boolean prevWithinQuery = data.withinQuery;
        data.withinQuery = true;
        int fromCount = 0;
        for (BLangNode clause : queryExpr.getQueryClauses()) {
            if (clause.getKind() == NodeKind.FROM) {
                fromCount++;
                BLangFromClause fromClause = (BLangFromClause) clause;
                BLangExpression collection = (BLangExpression) fromClause.getCollection();
                if (fromCount > 1) {
                    if (TypeTags.STREAM == Types.getReferredType(collection.getBType()).tag) {
                        this.dlog.error(collection.pos, DiagnosticErrorCode.NOT_ALLOWED_STREAM_USAGE_WITH_FROM);
                    }
                }
            }
            analyzeNode(clause, data);
        }
        data.withinQuery = prevWithinQuery;
        data.queryToTableWithKey = prevQueryToTableWithKey;
        data.queryConstructType = prevQueryConstructType;
    }

    @Override
    public void visit(BLangQueryAction queryAction, AnalyzerData data) {
        boolean prevWithinQuery = data.withinQuery;
        data.withinQuery = true;
        int fromCount = 0;
        for (BLangNode clause : queryAction.getQueryClauses()) {
            if (clause.getKind() == NodeKind.FROM) {
                fromCount++;
                BLangFromClause fromClause = (BLangFromClause) clause;
                BLangExpression collection = (BLangExpression) fromClause.getCollection();
                if (fromCount > 1) {
                    if (TypeTags.STREAM == Types.getReferredType(collection.getBType()).tag) {
                        this.dlog.error(collection.pos, DiagnosticErrorCode.NOT_ALLOWED_STREAM_USAGE_WITH_FROM);
                    }
                }
            }
            analyzeNode(clause, data);
        }
        validateActionParentNode(queryAction.pos, queryAction);
        data.withinQuery = prevWithinQuery;
    }

    @Override
    public void visit(BLangFromClause fromClause, AnalyzerData data) {
        analyzeExpr(fromClause.collection, data);
    }

    @Override
    public void visit(BLangJoinClause joinClause, AnalyzerData data) {
        analyzeExpr(joinClause.collection, data);
        if (joinClause.onClause != null) {
            analyzeNode(joinClause.onClause, data);
        }
    }

    @Override
    public void visit(BLangLetClause letClause, AnalyzerData data) {
        for (BLangLetVariable letVariable : letClause.letVarDeclarations) {
            analyzeNode((BLangNode) letVariable.definitionNode.getVariable(), data);
        }
    }

    @Override
    public void visit(BLangWhereClause whereClause, AnalyzerData data) {
        analyzeExpr(whereClause.expression, data);
    }

    @Override
    public void visit(BLangOnClause onClause, AnalyzerData data) {
        analyzeExpr(onClause.lhsExpr, data);
        analyzeExpr(onClause.rhsExpr, data);
    }

    @Override
    public void visit(BLangOrderByClause orderByClause, AnalyzerData data) {
        orderByClause.orderByKeyList.forEach(value -> analyzeExpr((BLangExpression) value.getOrderKey(), data));
    }

    @Override
    public void visit(BLangSelectClause selectClause, AnalyzerData data) {
        analyzeExpr(selectClause.expression, data);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause, AnalyzerData data) {
        analyzeExpr(onConflictClause.expression, data);
        if (!(data.queryToTableWithKey || data.queryConstructType == Types.QueryConstructType.MAP)) {
            dlog.error(onConflictClause.pos,
                    DiagnosticErrorCode.ON_CONFLICT_ONLY_WORKS_WITH_MAPS_OR_TABLES_WITH_KEY_SPECIFIER);
        }
    }

    @Override
    public void visit(BLangDoClause doClause, AnalyzerData data) {
        analyzeNode(doClause.body, data);
    }

    @Override
    public void visit(BLangOnFailClause onFailClause, AnalyzerData data) {
        boolean currentFailVisited = data.failVisited;
        data.failVisited = false;
        VariableDefinitionNode onFailVarDefNode = onFailClause.variableDefinitionNode;

        if (onFailVarDefNode != null) {
            BLangVariable onFailVarNode = (BLangVariable) onFailVarDefNode.getVariable();
            for (BType errorType : data.errorTypes.peek()) {
                if (!types.isAssignable(errorType, onFailVarNode.getBType())) {
                    dlog.error(onFailVarNode.pos, DiagnosticErrorCode.INCOMPATIBLE_ON_FAIL_ERROR_DEFINITION, errorType,
                            onFailVarNode.getBType());
                }
            }
        }
        data.errorTypes.pop();
        analyzeNode(onFailClause.body, data);
        onFailClause.bodyContainsFail = data.failVisited;
        data.failVisited = currentFailVisited;
    }

    private void analyseOnFailAndUpdateBreakMode(boolean onFailExists, BLangBlockStmt blockStmt,
                                                 BLangOnFailClause onFailClause, AnalyzerData data) {
        if (onFailExists) {
            blockStmt.failureBreakMode = getPossibleBreakMode(!data.errorTypes.peek().isEmpty());
            analyzeOnFailClause(onFailClause, data);
        }
    }

    @Override
    public void visit(BLangLimitClause limitClause, AnalyzerData data) {
        analyzeExpr(limitClause.expression, data);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr, AnalyzerData data) {
        BLangExpression expr = typeTestExpr.expr;
        analyzeNode(expr, data);
        BType exprType = expr.getBType();
        BType typeNodeType = typeTestExpr.typeNode.getBType();
        if (typeNodeType == symTable.semanticError || exprType == symTable.semanticError) {
            return;
        }
        // Check whether the condition is always true. If the variable type is assignable to target type,
        // then type check will always evaluate to true.
        if (types.isAssignable(exprType, typeNodeType)) {
            if (typeTestExpr.isNegation) {
                dlog.hint(typeTestExpr.pos, DiagnosticHintCode.EXPRESSION_ALWAYS_FALSE);
                return;
            }
            if (types.isNeverTypeOrStructureTypeWithARequiredNeverMember(exprType)) {
                dlog.hint(typeTestExpr.pos, DiagnosticHintCode.UNNECESSARY_CONDITION_FOR_VARIABLE_OF_TYPE_NEVER);
                return;
            }
            dlog.hint(typeTestExpr.pos, DiagnosticHintCode.UNNECESSARY_CONDITION);
            return;
        }

        // Check whether the target type can ever be present as the type of the source.
        // It'll be only possible iff, the target type has been assigned to the source
        // variable at some point. To do that, a value of target type should be assignable
        // to the type of the source variable.
        if (!intersectionExists(expr, typeNodeType, data, typeTestExpr.pos)) {
            dlog.error(typeTestExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_CHECK, exprType, typeNodeType);
        }
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr, AnalyzerData data) {
        analyzeExpr(annotAccessExpr.expr, data);
        BAnnotationSymbol annotationSymbol = annotAccessExpr.annotationSymbol;
        if (annotationSymbol != null && Symbols.isFlagOn(annotationSymbol.flags, Flags.DEPRECATED)) {
            logDeprecatedWaring(annotAccessExpr.annotationName.toString(), annotationSymbol, annotAccessExpr.pos);
        }
    }

    @Override
    public void visit(BLangRegExpTemplateLiteral regExpTemplateLiteral, AnalyzerData data) {
        List<BLangExpression> interpolationsList =
                symResolver.getListOfInterpolations(regExpTemplateLiteral.reDisjunction.sequenceList);
        interpolationsList.forEach(interpolation -> analyzeExpr(interpolation, data));
    }

    private void logDeprecatedWaring(String deprecatedConstruct, BSymbol symbol, Location pos) {
        if (!Names.DOT.equals(symbol.pkgID.name)) {
            deprecatedConstruct = symbol.pkgID + ":" + deprecatedConstruct;
        }

        dlog.warning(pos, DiagnosticWarningCode.USAGE_OF_DEPRECATED_CONSTRUCT, deprecatedConstruct);
    }

    private boolean intersectionExists(BLangExpression expression, BType testType, AnalyzerData data,
                                       Location intersectionPos) {
        BType expressionType = expression.getBType();

        BType intersectionType = types.getTypeIntersection(
                Types.IntersectionContext.typeTestIntersectionExistenceContext(intersectionPos),
                expressionType, testType, data.env);

        // any and readonly has an intersection
        return (intersectionType != symTable.semanticError) ||
                (expressionType.tag == TypeTags.ANY && testType.tag == TypeTags.READONLY);
    }

    @Override
    public void visit(BLangInferredTypedescDefaultNode inferTypedescExpr, AnalyzerData data) {
        /* Ignore */
    }

    // private methods

    private <E extends BLangExpression> void analyzeExpr(E node, AnalyzerData data) {
        if (node == null) {
            return;
        }
        SymbolEnv prevEnv = data.env;
        BLangNode parent = data.parent;
        node.parent = data.parent;
        data.parent = node;
        node.accept(this, data);
        data.parent = parent;
        checkAccess(node, data);
        checkExpressionValidity(node, data);
        data.env = prevEnv;
    }

    private  <E extends BLangExpression> void checkExpressionValidity(E exprNode, AnalyzerData data) {
        if (exprNode.getKind() == NodeKind.GROUP_EXPR ||
                !types.isNeverTypeOrStructureTypeWithARequiredNeverMember(exprNode.getBType())) {
            return;
        }
        if (!checkExpressionInValidParent(exprNode.parent, data)) {
            dlog.error(exprNode.pos, DiagnosticErrorCode.EXPRESSION_OF_NEVER_TYPE_NOT_ALLOWED);
        }
    }

    private boolean checkExpressionInValidParent(BLangNode currentParent, AnalyzerData data) {
        if (currentParent == null) {
            return false;
        }
        if (currentParent.getKind() == NodeKind.GROUP_EXPR) {
            return checkExpressionInValidParent(currentParent.parent, data);
        }
        return  currentParent.getKind() == NodeKind.EXPRESSION_STATEMENT ||
                (currentParent.getKind() == NodeKind.VARIABLE &&
                        ((BLangSimpleVariable) data.parent).typeNode.getBType().tag == TypeTags.FUTURE)
                || currentParent.getKind() == NodeKind.TRAP_EXPR;
    }

    @Override
    public void visit(BLangConstant constant, AnalyzerData data) {

        analyzeTypeNode(constant.typeNode, data);
        analyzeNode(constant.expr, data);
        analyzeExportableTypeRef(constant.symbol, constant.symbol.type.tsymbol, false, constant.pos);
        constant.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, data));
    }

    /**
     * This method checks for private symbols being accessed or used outside of package and|or private symbols being
     * used in public fields of objects/records and will fail those occurrences.
     *
     * @param node expression node to analyze
     * @param data data used to analyze the node
     */
    private <E extends BLangExpression> void checkAccess(E node, AnalyzerData data) {
        if (node.getBType() != null) {
            checkAccessSymbol(node.getBType().tsymbol, data.env.enclPkg.symbol.pkgID, node.pos);
        }

        //check for object new invocation
        if (node.getKind() == NodeKind.INVOCATION) {
            BLangInvocation bLangInvocation = (BLangInvocation) node;
            checkAccessSymbol(bLangInvocation.symbol, data.env.enclPkg.symbol.pkgID, bLangInvocation.pos);
        }
    }

    private void checkAccessSymbol(BSymbol symbol, PackageID pkgID, Location position) {
        if (symbol == null) {
            return;
        }

        if (!pkgID.equals(symbol.pkgID) && !Symbols.isPublic(symbol)) {
            dlog.error(position, DiagnosticErrorCode.ATTEMPT_REFER_NON_ACCESSIBLE_SYMBOL, symbol.name);
        }
    }

    private <E extends BLangExpression> void analyzeExprs(List<E> nodeList, AnalyzerData data) {
        for (int i = 0; i < nodeList.size(); i++) {
            analyzeExpr(nodeList.get(i), data);
        }
    }

    private void initNewWorkerActionSystem(AnalyzerData data) {
        data.workerActionSystemStack.push(new WorkerActionSystem());
    }

    private void finalizeCurrentWorkerActionSystem(AnalyzerData data) {
        WorkerActionSystem was = data.workerActionSystemStack.pop();
        if (!was.hasErrors) {
            this.validateWorkerInteractions(was, data);
        }
    }

    private static boolean isWorkerSend(BLangNode action) {
        return action.getKind() == NodeKind.WORKER_SEND;
    }

    private static boolean isWorkerSyncSend(BLangNode action) {
        return action.getKind() == NodeKind.WORKER_SYNC_SEND;
    }

    private static boolean isWaitAction(BLangNode action) {
        return action.getKind() == NodeKind.WAIT_EXPR;
    }

    private String extractWorkerId(BLangNode action) {
        if (isWorkerSend(action)) {
            return ((BLangWorkerSend) action).workerIdentifier.value;
        } else if (isWorkerSyncSend(action)) {
            return ((BLangWorkerSyncSendExpr) action).workerIdentifier.value;
        } else {
            return ((BLangWorkerReceive) action).workerIdentifier.value;
        }
    }

    private void validateWorkerInteractions(WorkerActionSystem workerActionSystem, AnalyzerData data) {
        if (!validateWorkerInteractionsAfterWaitAction(workerActionSystem)) {
            return;
        }

        BLangNode currentAction;
        boolean systemRunning;
        data.workerSystemMovementSequence = 0;
        int systemIterationCount = 0;
        int prevWorkerSystemMovementSequence = data.workerSystemMovementSequence;
        do {
            systemRunning = false;
            systemIterationCount++;
            for (WorkerActionStateMachine worker : workerActionSystem.finshedWorkers) {
                if (worker.done()) {
                    continue;
                }
                currentAction = worker.currentAction();

                if (isWaitAction(currentAction)) {
                    handleWaitAction(workerActionSystem, currentAction, worker, data);
                    systemRunning = true;
                    continue;
                }
                if (!isWorkerSend(currentAction) && !isWorkerSyncSend(currentAction)) {
                    continue;
                }

                WorkerActionStateMachine otherSM = workerActionSystem.find(this.extractWorkerId(currentAction));
                if (otherSM.done()) {
                    continue;
                }
                if (isWaitAction(otherSM.currentAction())) {
                    systemRunning = false;
                    continue;
                }
                if (!otherSM.currentIsReceive(worker.workerId)) {
                    continue;
                }
                BLangWorkerReceive receive = (BLangWorkerReceive) otherSM.currentAction();
                if (isWorkerSyncSend(currentAction)) {
                    this.validateWorkerActionParameters((BLangWorkerSyncSendExpr) currentAction, receive);
                } else {
                    this.validateWorkerActionParameters((BLangWorkerSend) currentAction, receive);
                }
                otherSM.next();
                data.workerSystemMovementSequence++;
                worker.next();
                data.workerSystemMovementSequence++;


                systemRunning = true;
                String channelName = generateChannelName(worker.workerId, otherSM.workerId);
                otherSM.node.sendsToThis.add(channelName);

                worker.node.sendsToThis.add(channelName);
            }

            // If we iterated move than the number of workers in the system and did not progress,
            // this means we are in a deadlock.
            if (systemIterationCount > workerActionSystem.finshedWorkers.size()) {
                systemIterationCount = 0;
                if (prevWorkerSystemMovementSequence == data.workerSystemMovementSequence) {
                    systemRunning = false;
                }
                prevWorkerSystemMovementSequence = data.workerSystemMovementSequence;
            }
        } while (systemRunning);

        if (!workerActionSystem.everyoneDone()) {
            this.reportInvalidWorkerInteractionDiagnostics(workerActionSystem);
        }
    }

    private boolean validateWorkerInteractionsAfterWaitAction(WorkerActionSystem workerActionSystem) {
        boolean isValid = true;
        for (WorkerActionStateMachine worker : workerActionSystem.finshedWorkers) {
            Set<String> waitingOnWorkerSet = new HashSet<>();
            for (BLangNode action : worker.actions) {
                if (isWaitAction(action)) {
                    if (action instanceof BLangWaitForAllExpr) {
                        BLangWaitForAllExpr waitForAllExpr = (BLangWaitForAllExpr) action;
                        for (BLangWaitForAllExpr.BLangWaitKeyValue keyValuePair : waitForAllExpr.keyValuePairs) {
                            BSymbol workerSymbol = getWorkerSymbol(keyValuePair);
                            if (workerSymbol != null) {
                                waitingOnWorkerSet.add(workerSymbol.name.value);
                            }
                        }
                    } else {
                        BLangWaitExpr wait = (BLangWaitExpr) action;
                        for (String workerName : getWorkerNameList(wait.exprList.get(0),
                                workerActionSystem.getActionEnvironment(wait))) {
                            waitingOnWorkerSet.add(workerName);
                        }
                    }
                } else  if (isWorkerSend(action)) {
                    BLangWorkerSend send = (BLangWorkerSend) action;
                    if (waitingOnWorkerSet.contains(send.workerIdentifier.value)) {
                        dlog.error(action.pos, DiagnosticErrorCode.WORKER_INTERACTION_AFTER_WAIT_ACTION, action);
                        isValid = false;
                    }
                } else if (isWorkerSyncSend(action)) {
                    BLangWorkerSyncSendExpr syncSend = (BLangWorkerSyncSendExpr) action;
                    if (waitingOnWorkerSet.contains(syncSend.workerIdentifier.value)) {
                        dlog.error(action.pos, DiagnosticErrorCode.WORKER_INTERACTION_AFTER_WAIT_ACTION, action);
                        isValid = false;
                    }
                } else if (action.getKind() == NodeKind.WORKER_RECEIVE) {
                    BLangWorkerReceive receive = (BLangWorkerReceive) action;
                    if (waitingOnWorkerSet.contains(receive.workerIdentifier.value)) {
                        dlog.error(action.pos, DiagnosticErrorCode.WORKER_INTERACTION_AFTER_WAIT_ACTION, action);
                        isValid = false;
                    }
                }
            }
        }
        return isValid;
    }

    private void handleWaitAction(WorkerActionSystem workerActionSystem, BLangNode currentAction,
                                  WorkerActionStateMachine worker, AnalyzerData data) {
        if (currentAction instanceof BLangWaitForAllExpr) {
            boolean allWorkersAreDone = true;
            BLangWaitForAllExpr waitForAllExpr = (BLangWaitForAllExpr) currentAction;
            for (BLangWaitForAllExpr.BLangWaitKeyValue keyValuePair : waitForAllExpr.keyValuePairs) {
                BSymbol workerSymbol = getWorkerSymbol(keyValuePair);
                if (isWorkerSymbol(workerSymbol)) {
                    Name workerName = workerSymbol.name;
                    if (isWorkerFromFunction(workerActionSystem.getActionEnvironment(currentAction), workerName)) {
                        WorkerActionStateMachine otherSM = workerActionSystem.find(workerName.value);
                        allWorkersAreDone = allWorkersAreDone && otherSM.done();
                    }
                }
            }
            if (allWorkersAreDone) {
                worker.next();
                data.workerSystemMovementSequence++;
            }
        } else {
            BLangWaitExpr wait = (BLangWaitExpr) currentAction;
            List<String> workerNameList = getWorkerNameList(wait.exprList.get(0),
                    workerActionSystem.getActionEnvironment(currentAction));
            if (workerNameList.isEmpty()) {
                // No workers found, there must be only future references in the waiting list, we can move to next state
                worker.next();
                data.workerSystemMovementSequence++;
            }
            for (String workerName : workerNameList) {
                // If any worker in wait is done, we can continue.
                var otherSM = workerActionSystem.find(workerName);
                if (otherSM.done()) {
                    worker.next();
                    data.workerSystemMovementSequence++;
                    break;
                }
            }
        }
    }

    private BSymbol getWorkerSymbol(BLangWaitForAllExpr.BLangWaitKeyValue keyValuePair) {
        BLangExpression value = keyValuePair.getValue();
        if (value != null && value.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            return ((BLangSimpleVarRef) value).symbol;
        } else if (keyValuePair.keyExpr != null && keyValuePair.keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            return ((BLangSimpleVarRef) keyValuePair.keyExpr).symbol;
        }
        return null;
    }

    private List<String> getWorkerNameList(BLangExpression expr, SymbolEnv functionEnv) {
        ArrayList<String> workerNames = new ArrayList<>();
        populateWorkerNameList(expr, workerNames, functionEnv);
        return workerNames;
    }

    private void populateWorkerNameList(BLangExpression expr, ArrayList<String> workerNames, SymbolEnv functionEnv) {
        if (expr.getKind() == NodeKind.BINARY_EXPR) {
            BLangBinaryExpr binaryExpr = (BLangBinaryExpr) expr;
            populateWorkerNameList(binaryExpr.lhsExpr, workerNames, functionEnv);
            populateWorkerNameList(binaryExpr.rhsExpr, workerNames, functionEnv);
        } else if (expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) expr;
            if (isWorkerSymbol(varRef.symbol) && isWorkerFromFunction(functionEnv, varRef.symbol.name)) {
                workerNames.add(varRef.variableName.value);
            }
        }
    }

    private boolean isWorkerFromFunction(SymbolEnv functionEnv, Name workerName) {
        if (functionEnv == null) {
            return false;
        }

        if (functionEnv.scope.lookup(workerName).symbol != null) {
            return true;
        }

        if (functionEnv.enclInvokable != null) {
            Set<Flag> flagSet = functionEnv.enclInvokable.flagSet;
            if (flagSet.contains(Flag.LAMBDA) && !flagSet.contains(Flag.WORKER)) {
                return false;
            }
        }
        return isWorkerFromFunction(functionEnv.enclEnv, workerName);
    }

    private boolean isWorkerSymbol(BSymbol symbol) {
        return symbol != null && (symbol.flags & Flags.WORKER) == Flags.WORKER;
    }

    private void reportInvalidWorkerInteractionDiagnostics(WorkerActionSystem workerActionSystem) {
        this.dlog.error(workerActionSystem.getRootPosition(), DiagnosticErrorCode.INVALID_WORKER_INTERACTION,
                workerActionSystem.toString());
    }

    private void validateWorkerActionParameters(BLangWorkerSend send, BLangWorkerReceive receive) {
        types.checkType(receive, send.getBType(), receive.getBType());
        addImplicitCast(send.getBType(), receive);
        NodeKind kind = receive.parent.getKind();
        if (kind == NodeKind.TRAP_EXPR || kind == NodeKind.CHECK_EXPR || kind == NodeKind.CHECK_PANIC_EXPR ||
                kind == NodeKind.FAIL) {
            typeChecker.checkExpr((BLangExpression) receive.parent, receive.env);
        }
        receive.sendExpression = send.expr;
    }

    private void validateWorkerActionParameters(BLangWorkerSyncSendExpr send, BLangWorkerReceive receive) {
        send.receive = receive;
        NodeKind parentNodeKind = send.parent.getKind();
        if (parentNodeKind == NodeKind.VARIABLE) {
            BLangSimpleVariable variable = (BLangSimpleVariable) send.parent;

            if (variable.isDeclaredWithVar) {
                variable.setBType(variable.symbol.type = send.expectedType = receive.matchingSendsError);
            }
        } else if (parentNodeKind == NodeKind.ASSIGNMENT) {
            BLangAssignment assignment = (BLangAssignment) send.parent;
            if (assignment.varRef.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BSymbol varSymbol = ((BLangSimpleVarRef) assignment.varRef).symbol;
                if (varSymbol != null) {
                    send.expectedType = varSymbol.type;
                }
            }
        }

        if (receive.matchingSendsError != symTable.nilType && parentNodeKind == NodeKind.EXPRESSION_STATEMENT) {
            dlog.error(send.pos, DiagnosticErrorCode.ASSIGNMENT_REQUIRED, send.workerSymbol);
        } else {
            types.checkType(send.pos, receive.matchingSendsError, send.expectedType,
                            DiagnosticErrorCode.INCOMPATIBLE_TYPES);
        }

        types.checkType(receive, send.getBType(), receive.getBType());

        addImplicitCast(send.getBType(), receive);
        NodeKind kind = receive.parent.getKind();
        if (kind == NodeKind.TRAP_EXPR || kind == NodeKind.CHECK_EXPR || kind == NodeKind.CHECK_PANIC_EXPR) {
            typeChecker.checkExpr((BLangExpression) receive.parent, receive.env);
        }
        receive.sendExpression = send;
    }

    private void addImplicitCast(BType actualType, BLangWorkerReceive receive) {
        if (receive.getBType() != null && receive.getBType() != symTable.semanticError) {
            types.setImplicitCastExpr(receive, actualType, receive.getBType());
            receive.setBType(actualType);
        }
    }

    private boolean checkNextBreakValidityInTransaction(AnalyzerData data) {
        return !data.loopWithinTransactionCheckStack.peek() && data.transactionCount > 0 && data.withinTransactionScope;
    }

    private boolean checkReturnValidityInTransaction(AnalyzerData data) {
        return !data.returnWithinTransactionCheckStack.peek() && data.transactionCount > 0
                && data.withinTransactionScope;
    }

    private void validateModuleInitFunction(BLangFunction funcNode) {
        if (funcNode.attachedFunction || !Names.USER_DEFINED_INIT_SUFFIX.value.equals(funcNode.name.value)) {
            return;
        }

        if (Symbols.isPublic(funcNode.symbol)) {
            this.dlog.error(funcNode.pos, DiagnosticErrorCode.MODULE_INIT_CANNOT_BE_PUBLIC);
        }

        if (!funcNode.requiredParams.isEmpty() || funcNode.restParam != null) {
            this.dlog.error(funcNode.pos, DiagnosticErrorCode.MODULE_INIT_CANNOT_HAVE_PARAMS);
        }

        types.validateErrorOrNilReturn(funcNode, DiagnosticErrorCode.MODULE_INIT_RETURN_SHOULD_BE_ERROR_OR_NIL);
    }

    private BType getErrorTypes(BType bType) {
        if (bType == null) {
            return symTable.semanticError;
        }

        BType errorType = symTable.semanticError;

        int tag = bType.tag;
        if (tag == TypeTags.TYPEREFDESC) {
            return getErrorTypes(Types.getReferredType(bType));
        }
        if (tag == TypeTags.ERROR) {
            errorType = bType;
        } else if (tag == TypeTags.READONLY) {
            errorType = symTable.errorType;
        } else if (tag == TypeTags.UNION) {
            LinkedHashSet<BType> errTypes = new LinkedHashSet<>();
            Set<BType> memTypes = ((BUnionType) bType).getMemberTypes();
            for (BType memType : memTypes) {
                BType memErrType = getErrorTypes(memType);

                if (memErrType != symTable.semanticError) {
                    errTypes.add(memErrType);
                }
            }

            if (!errTypes.isEmpty()) {
                errorType = errTypes.size() == 1 ? errTypes.iterator().next() : BUnionType.create(null, errTypes);
            }
        }

        return errorType;
    }

    /**
     * This class contains the state machines for a set of workers.
     */
    private static class WorkerActionSystem {

        public List<WorkerActionStateMachine> finshedWorkers = new ArrayList<>();
        private Stack<WorkerActionStateMachine> workerActionStateMachines = new Stack<>();
        private Map<BLangNode, SymbolEnv> workerInteractionEnvironments = new IdentityHashMap<>();
        private boolean hasErrors = false;


        public void startWorkerActionStateMachine(String workerId, Location pos, BLangFunction node) {
            workerActionStateMachines.push(new WorkerActionStateMachine(pos, workerId, node));
        }

        public void endWorkerActionStateMachine() {
            finshedWorkers.add(workerActionStateMachines.pop());
        }

        public void addWorkerAction(BLangNode action) {
            this.workerActionStateMachines.peek().actions.add(action);
        }

        public WorkerActionStateMachine find(String workerId) {
            for (WorkerActionStateMachine worker : this.finshedWorkers) {
                if (worker.workerId.equals(workerId)) {
                    return worker;
                }
            }
            throw new AssertionError("Reference to non existing worker " + workerId);
        }

        public boolean everyoneDone() {
            return this.finshedWorkers.stream().allMatch(WorkerActionStateMachine::done);
        }

        public Location getRootPosition() {
            return this.finshedWorkers.iterator().next().pos;
        }

        @Override
        public String toString() {
            return this.finshedWorkers.toString();
        }

        public String currentWorkerId() {
            return workerActionStateMachines.peek().workerId;
        }

        public void addWorkerAction(BLangNode action, SymbolEnv env) {
            addWorkerAction(action);
            this.workerInteractionEnvironments.put(action, env);
        }

        private SymbolEnv getActionEnvironment(BLangNode currentAction) {
            return workerInteractionEnvironments.get(currentAction);
        }
    }

    /**
     * This class represents a state machine to maintain the state of the send/receive
     * actions of a worker.
     */
    private static class WorkerActionStateMachine {

        private static final String WORKER_SM_FINISHED = "FINISHED";

        public int currentState;

        public List<BLangNode> actions = new ArrayList<>();

        public Location pos;
        public String workerId;
        public BLangFunction node;

        public WorkerActionStateMachine(Location pos, String workerId, BLangFunction node) {
            this.pos = pos;
            this.workerId = workerId;
            this.node = node;
        }

        public boolean done() {
            return this.actions.size() == this.currentState;
        }

        public BLangNode currentAction() {
            return this.actions.get(this.currentState);
        }

        public boolean currentIsReceive(String sourceWorkerId) {
            if (this.done()) {
                return false;
            }
            BLangNode action = this.currentAction();
            return !isWorkerSend(action) && !isWorkerSyncSend(action) && !isWaitAction(action)
                    && ((BLangWorkerReceive) action).workerIdentifier.value.equals(sourceWorkerId);
        }

        public void next() {
            this.currentState++;
        }

        @Override
        public String toString() {
            if (this.done()) {
                return WORKER_SM_FINISHED;
            } else {
                BLangNode action = this.currentAction();
                if (isWorkerSend(action)) {
                    return ((BLangWorkerSend) action).toActionString();
                } else if (isWorkerSyncSend(action)) {
                    return ((BLangWorkerSyncSendExpr) action).toActionString();
                } else if (isWaitAction(action)) {
                    return action.toString();
                } else {
                    return ((BLangWorkerReceive) action).toActionString();
                }
            }
        }
    }

    public static String generateChannelName(String source, String target) {

        return source + "->" + target;
    }

    private BLangNode getEnclosingClass(SymbolEnv env) {
        BLangNode node = env.node;

        while (node.getKind() != NodeKind.CLASS_DEFN) {
            env = env.enclEnv;
            node = env.node;
        }
        return node;
    }

    private void validateInvocationInMatchGuard(BLangInvocation invocation) {
        BLangExpression matchedExpr = getMatchedExprIfCalledInMatchGuard(invocation);

        if (matchedExpr == null) {
            return;
        }

        BType matchedExprType = matchedExpr.getBType();

        if (types.isInherentlyImmutableType(matchedExprType) ||
                Symbols.isFlagOn(matchedExprType.flags, Flags.READONLY)) {
            return;
        }

        BSymbol invocationSymbol = invocation.symbol;

        if (invocationSymbol == null) {
            BLangNode parent = invocation.parent;
            if (parent == null || parent.getKind() != NodeKind.TYPE_INIT_EXPR) {
                return;
            }

            BLangTypeInit newExpr = (BLangTypeInit) parent;
            if (newExpr.getBType().tag != TypeTags.STREAM) {
                return;
            }

            List<BLangExpression> argsExpr = newExpr.argsExpr;
            if (argsExpr.isEmpty()) {
                return;
            }

            BLangExpression streamImplementorExpr = argsExpr.get(0);
            BType type = streamImplementorExpr.getBType();
            if (!types.isInherentlyImmutableType(type) && !Symbols.isFlagOn(type.flags, Flags.READONLY)) {
                dlog.error(streamImplementorExpr.pos,
                        DiagnosticErrorCode.INVALID_CALL_WITH_MUTABLE_ARGS_IN_MATCH_GUARD);
            }
            return;
        }

        long flags = invocationSymbol.flags;
        boolean methodCall = Symbols.isFlagOn(flags, Flags.ATTACHED);

        boolean callsNonIsolatedFunction = !Symbols.isFlagOn(flags, Flags.ISOLATED) ||
                (methodCall && !Symbols.isFlagOn(invocationSymbol.owner.flags, Flags.ISOLATED));

        if (callsNonIsolatedFunction) {
            dlog.error(invocation.pos, DiagnosticErrorCode.INVALID_NON_ISOLATED_CALL_IN_MATCH_GUARD);
        }

        List<BLangExpression> args = new ArrayList<>(invocation.requiredArgs);
        args.addAll(invocation.restArgs);

        for (BLangExpression arg : args) {
            BType type = arg.getBType();

            if (type != symTable.semanticError &&
                    !types.isInherentlyImmutableType(type) &&
                    !Symbols.isFlagOn(type.flags, Flags.READONLY)) {
                dlog.error(arg.pos, DiagnosticErrorCode.INVALID_CALL_WITH_MUTABLE_ARGS_IN_MATCH_GUARD);
            }
        }
    }

    private BLangExpression getMatchedExprIfCalledInMatchGuard(BLangInvocation invocation) {
        BLangNode prevParent = invocation;
        BLangNode parent = invocation.parent;
        boolean encounteredMatchGuard = false;
        while (parent != null) {
            NodeKind parentKind = parent.getKind();
            switch (parentKind) {
                // If the parent is a function before we reach a match clause, this isn't directly called in the
                // match guard.
                case LAMBDA:
                case FUNCTION:
                case RESOURCE_FUNC:
                    return null;
                case MATCH_CLAUSE:
                    if (encounteredMatchGuard) {
                        return ((BLangMatchStatement) parent.parent).expr;
                    }
                    // If the parent is a match clause before we reach a match guard this isn't directly called in the
                    // match guard.
                    return null;
                case MATCH_GUARD:
                    encounteredMatchGuard = true;
                    break;
                case INVOCATION:
                    BLangInvocation parentInvocation = (BLangInvocation) parent;

                    if (parentInvocation.langLibInvocation || prevParent != parentInvocation.expr) {
                        // Argument to a call. Return early and let it be handled for the parent invocation.
                        return null;
                    }
            }

            prevParent = parent;
            parent = parent.parent;
        }
        return null;
    }

    private enum DefaultValueState {
        NOT_IN_DEFAULT_VALUE,
        RECORD_FIELD_DEFAULT,
        OBJECT_FIELD_INITIALIZER,
        FUNCTION_IN_DEFAULT_VALUE
    }

    /**
     * @since 2.0.0
     */
    public static class AnalyzerData {
        SymbolEnv env;
        BLangNode parent;
        // Fields related to looping
        int loopCount;
        boolean loopAlterNotAllowed;
        // Fields related to worker system
        boolean inInternallyDefinedBlockStmt;
        int workerSystemMovementSequence;
        Stack<WorkerActionSystem> workerActionSystemStack = new Stack<>();
        Map<BSymbol, Set<BLangNode>> workerReferences = new HashMap<>();
        // Field related to transactions
        int transactionCount;
        boolean withinTransactionScope;
        int commitCount;
        int rollbackCount;
        boolean commitRollbackAllowed;
        int commitCountWithinBlock;
        int rollbackCountWithinBlock;
        Stack<Boolean> loopWithinTransactionCheckStack = new Stack<>();
        Stack<Boolean> returnWithinTransactionCheckStack = new Stack<>();
        Stack<Boolean> transactionalFuncCheckStack = new Stack<>();
        // Fields related to lock
        boolean withinLockBlock;
        // Common fields
        boolean failureHandled;
        boolean failVisited;
        boolean queryToTableWithKey;
        boolean withinQuery;
        Types.QueryConstructType queryConstructType;
        Stack<LinkedHashSet<BType>> returnTypes = new Stack<>();
        Stack<LinkedHashSet<BType>> errorTypes = new Stack<>();
        DefaultValueState defaultValueState = DefaultValueState.NOT_IN_DEFAULT_VALUE;
    }
}

/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.ballerinalang.util.diagnostic.DiagnosticHintCode;
import org.ballerinalang.util.diagnostic.DiagnosticWarningCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BHandleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNeverType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
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
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangRestBindingPattern;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInferredTypedescDefaultNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
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
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangRestMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangSimpleMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangVarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangWildCardMatchPattern;
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
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * Responsible for performing isolation analysis.
 *
 * @since 2.0.0
 */
public class IsolationAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<IsolationAnalyzer> ISOLATION_ANALYZER_KEY = new CompilerContext.Key<>();
    private static final String VALUE_LANG_LIB = "lang.value";
    private static final String CLONE_LANG_LIB_METHOD = "clone";
    private static final String CLONE_READONLY_LANG_LIB_METHOD = "cloneReadOnly";

    private SymbolEnv env;
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;
    private final Types types;
    private final BLangDiagnosticLog dlog;

    private boolean inferredIsolated = true;
    private boolean inLockStatement = false;
    private final Stack<LockInfo> copyInLockInfoStack = new Stack<>();
    private final Stack<Set<BSymbol>> isolatedLetVarStack = new Stack<>();
    private final Map<BSymbol, IsolationInferenceInfo> isolationInferenceInfoMap = new HashMap<>();
    private final Map<BLangArrowFunction, BInvokableSymbol> arrowFunctionTempSymbolMap = new HashMap<>();

    private IsolationAnalyzer(CompilerContext context) {
        context.put(ISOLATION_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public static IsolationAnalyzer getInstance(CompilerContext context) {
        IsolationAnalyzer isolationAnalyzer = context.get(ISOLATION_ANALYZER_KEY);
        if (isolationAnalyzer == null) {
            isolationAnalyzer = new IsolationAnalyzer(context);
        }
        return isolationAnalyzer;
    }

    private void analyzeNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        if (node != null) {
            node.accept(this);
        }
        this.env = prevEnv;
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        this.arrowFunctionTempSymbolMap.clear();
        this.isolationInferenceInfoMap.clear();
        this.dlog.setCurrentPackageId(pkgNode.packageID);
        pkgNode.accept(this);
        return pkgNode;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.ISOLATION_ANALYZE)) {
            return;
        }

        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);

        Set<BSymbol> moduleLevelVarSymbols = getModuleLevelVarSymbols(pkgNode.globalVars);
        populateNonPublicMutableOrNonIsolatedVars(moduleLevelVarSymbols);
        List<BLangClassDefinition> classDefinitions = pkgNode.classDefinitions;
        populateNonPublicIsolatedInferableClasses(classDefinitions);

        for (BLangTypeDefinition typeDefinition : pkgNode.typeDefinitions) {
            analyzeNode(typeDefinition.typeNode, pkgEnv);
        }

        for (BLangClassDefinition classDefinition : pkgNode.classDefinitions) {
            if (classDefinition.flagSet.contains(Flag.ANONYMOUS) && isIsolated(classDefinition.getBType().flags)) {
                // If this is a class definition for an object constructor expression, and the type is `isolated`,
                // that is due to the expected type being an `isolated` object. We now mark the class definition also
                // as `isolated`, to enforce the isolation validation.
                classDefinition.flagSet.add(Flag.ISOLATED);
                classDefinition.symbol.flags |= Flags.ISOLATED;
            }

            analyzeNode(classDefinition, pkgEnv);
        }

        for (BLangFunction function : pkgNode.functions) {
            // Skip visiting worker lambdas here. They will be visited when enclosing function is visited.
            if (!isWorkerLambda(function)) {
                analyzeNode(function, pkgEnv);
            }
        }

        for (BLangVariable globalVar : pkgNode.globalVars) {
            analyzeNode(globalVar, pkgEnv);
        }

        inferIsolation(moduleLevelVarSymbols, getPubliclyExposedObjectTypes(pkgNode), classDefinitions);
        logServiceIsolationHints(classDefinitions);
        this.arrowFunctionTempSymbolMap.clear();
        this.isolationInferenceInfoMap.clear();

        for (BLangTestablePackage testablePkg : pkgNode.testablePkgs) {
            visit((BLangPackage) testablePkg);
        }

        pkgNode.completedPhases.add(CompilerPhase.ISOLATION_ANALYZE);
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangResourceFunction funcNode) {
        // todo: revisit the isolate
        visit((BLangFunction) funcNode);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        boolean prevInferredIsolated = this.inferredIsolated;
        this.inferredIsolated = true;

        IsolationInferenceInfo functionIsolationInferenceInfo = null;

        BInvokableSymbol symbol = funcNode.symbol;

        if (isIsolationInferableFunction(funcNode) && !isolationInferenceInfoMap.containsKey(symbol)) {
            functionIsolationInferenceInfo = new IsolationInferenceInfo();
            isolationInferenceInfoMap.put(symbol, functionIsolationInferenceInfo);
        }

        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, symbol.scope, env);

        for (BLangSimpleVariable requiredParam : funcNode.requiredParams) {
            if (!requiredParam.symbol.isDefaultable) {
                continue;
            }

            analyzeNode(requiredParam.expr, funcEnv);
        }

        analyzeNode(funcNode.body, funcEnv);

        if (this.inferredIsolated &&
                !isIsolated(symbol.flags) &&
                functionIsolationInferenceInfo != null &&
                functionIsolationInferenceInfo.dependsOnlyOnInferableConstructs &&
                // Init method isolation depends on the object field-initializers too, so we defer inference.
                !funcNode.objInitFunction) {
            functionIsolationInferenceInfo.inferredIsolated = true;
        }

        this.inferredIsolated = this.inferredIsolated && prevInferredIsolated;
    }

    @Override
    public void visit(BLangBlockFunctionBody body) {
        SymbolEnv bodyEnv = SymbolEnv.createFuncBodyEnv(body, env);
        for (BLangStatement statement : body.stmts) {
            analyzeNode(statement, bodyEnv);
        }
    }

    @Override
    public void visit(BLangExprFunctionBody body) {
        SymbolEnv bodyEnv = SymbolEnv.createFuncBodyEnv(body, env);
        analyzeNode(body.expr, bodyEnv);
    }

    @Override
    public void visit(BLangExternalFunctionBody body) {
        markDependsOnIsolationNonInferableConstructs();
        inferredIsolated = false;
    }

    @Override
    public void visit(BLangService serviceNode) {
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        analyzeNode(typeDefinition.typeNode, env);
    }

    @Override
    public void visit(BLangConstant constant) {
        BLangType typeNode = constant.typeNode;
        if (typeNode != null) {
            analyzeNode(typeNode, env);
        }

        analyzeNode(constant.expr, env);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        BLangType typeNode = varNode.typeNode;
        if (typeNode != null &&
                (typeNode.getBType() == null || typeNode.getBType().tsymbol == null ||
                         typeNode.getBType().tsymbol.owner.getKind() != SymbolKind.PACKAGE)) {
            // Only analyze the type node if it is not available at module level, since module level type definitions
            // have already been analyzed.
            analyzeNode(typeNode, env);
        }

        BVarSymbol symbol = varNode.symbol;
        var flags = symbol.flags;

        BLangExpression expr = varNode.expr;

        BType fieldType = varNode.getBType();
        boolean isolatedClassField = isIsolatedClassField();

        if (isolatedClassField && isExpectedToBeAPrivateField(symbol, fieldType) &&
                !Symbols.isFlagOn(flags, Flags.PRIVATE)) {
            dlog.error(varNode.pos, DiagnosticErrorCode.INVALID_NON_PRIVATE_MUTABLE_FIELD_IN_ISOLATED_OBJECT);
        }

        if (expr == null) {
            return;
        }

        if (isolatedClassField || varNode.flagSet.contains(Flag.ISOLATED)) {
            validateIsolatedExpression(fieldType, expr);
        }

        analyzeNode(expr, env);

        BSymbol owner = symbol.owner;
        if (owner != null && ((owner.tag & SymTag.LET) == SymTag.LET) && isIsolatedExpression(expr)) {
            isolatedLetVarStack.peek().add(symbol);
        }

        if (expr.getKind() == NodeKind.LAMBDA) {
            addIsolationInfoForWorkerVarNode(((BLangLambdaFunction) expr).function, symbol);
        }
    }

    private void addIsolationInfoForWorkerVarNode(BLangFunction workerFunc, BVarSymbol workerVarSymbol) {
        if (workerFunc.flagSet.contains(Flag.WORKER) && workerFunc.flagSet.contains(Flag.LAMBDA) &&
                this.isolationInferenceInfoMap.containsKey(workerFunc.symbol)) {
            IsolationInferenceInfo functionIsolationInferenceInfo =
                    this.isolationInferenceInfoMap.get(workerFunc.symbol);
            this.isolationInferenceInfoMap.put(workerVarSymbol, functionIsolationInferenceInfo);
        }
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        BLangExpression expr = annAttachmentNode.expr;
        if (expr != null) {
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        for (BLangStatement statement : blockNode.stmts) {
            analyzeNode(statement, blockEnv);
        }
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        BLangVariable var = varDefNode.var;
        if (var.expr == null) {
            if (var.typeNode != null) {
                analyzeNode(var.typeNode, env);
            }
            return;
        }

        analyzeNode(var, env);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        BLangExpression varRef = assignNode.varRef;
        analyzeNode(varRef, env);

        BLangExpression expr = assignNode.expr;
        analyzeNode(expr, env);

        BLangInvokableNode enclInvokable = env.enclInvokable;
        if (varRef.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
            BLangFieldBasedAccess fieldAccess = (BLangFieldBasedAccess) varRef;

            if (enclInvokable != null && enclInvokable.getKind() == NodeKind.FUNCTION &&
                    ((BLangFunction) enclInvokable).objInitFunction &&
                    isIsolatedObjectFieldOrMethodAccessViaSelf(fieldAccess, false)) {
                validateIsolatedExpression(
                        ((BObjectType) enclInvokable.symbol.owner.type).fields.get(fieldAccess.field.value).type, expr);
            }
        }

        validateTransferOut(varRef, expr);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        analyzeNode(compoundAssignNode.varRef, env);
        analyzeNode(compoundAssignNode.expr, env);
    }

    @Override
    public void visit(BLangRetry retryNode) {
        analyzeNode(retryNode.retrySpec, env);
        analyzeNode(retryNode.retryBody, env);
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        analyzeNode(retryTransaction.retrySpec, env);
        analyzeNode(retryTransaction.transaction, env);
    }

    @Override
    public void visit(BLangRetrySpec retrySpec) {
        for (BLangExpression argExpr : retrySpec.argExprs) {
            analyzeNode(argExpr, env);
        }
    }

    @Override
    public void visit(BLangContinue continueNode) {
    }

    @Override
    public void visit(BLangBreak breakNode) {
    }

    @Override
    public void visit(BLangReturn returnNode) {
        BLangExpression expr = returnNode.expr;
        analyzeNode(expr, env);

        if (!this.inLockStatement) {
            return;
        }

        validateTransferOut(expr, this.copyInLockInfoStack.peek().nonIsolatedTransferOutExpressions);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        analyzeNode(panicNode.expr, env);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        analyzeNode(xmlnsStmtNode.xmlnsDecl, env);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        analyzeNode(exprStmtNode.expr, env);
    }

    @Override
    public void visit(BLangIf ifNode) {
        analyzeNode(ifNode.expr, env);
        analyzeNode(ifNode.body, env);
        analyzeNode(ifNode.elseStmt, env);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        for (BLangNode clause : queryAction.getQueryClauses()) {
            analyzeNode(clause, env);
        }
        analyzeNode(queryAction.doClause, env);
    }

    @Override
    public void visit(BLangMatchStatement matchStatement) {
        analyzeNode(matchStatement.expr, env);

        for (BLangMatchClause matchClause : matchStatement.matchClauses) {
            analyzeNode(matchClause, env);
        }

        if (matchStatement.onFailClause != null) {
            analyzeNode(matchStatement.onFailClause, env);
        }
    }

    @Override
    public void visit(BLangMatchGuard matchGuard) {
        analyzeNode(matchGuard.expr, env);
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern) {
        analyzeNode(constMatchPattern.expr, env);
    }

    @Override
    public void visit(BLangWildCardMatchPattern wildCardMatchPattern) {
    }

    @Override
    public void visit(BLangListMatchPattern listMatchPattern) {
        for (BLangMatchPattern matchPattern : listMatchPattern.matchPatterns) {
            analyzeNode(matchPattern, env);
        }

        if (listMatchPattern.restMatchPattern != null) {
            analyzeNode(listMatchPattern.restMatchPattern, env);
        }
    }

    @Override
    public void visit(BLangRestMatchPattern restMatchPattern) {
    }

    @Override
    public void visit(BLangMappingMatchPattern mappingMatchPattern) {
        for (BLangFieldMatchPattern fieldMatchPattern : mappingMatchPattern.fieldMatchPatterns) {
            analyzeNode(fieldMatchPattern, env);
        }
    }

    @Override
    public void visit(BLangFieldMatchPattern fieldMatchPattern) {
        analyzeNode(fieldMatchPattern.fieldName, env);
        analyzeNode(fieldMatchPattern.matchPattern, env);
    }

    @Override
    public void visit(BLangWildCardBindingPattern wildCardBindingPattern) {
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern) {
        analyzeNode(varBindingPattern.getBindingPattern(), env);
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern) {
    }

    @Override
    public void visit(BLangErrorBindingPattern errorBindingPattern) {
        analyzeNode(errorBindingPattern.errorMessageBindingPattern, env);
        analyzeNode(errorBindingPattern.errorCauseBindingPattern, env);
        analyzeNode(errorBindingPattern.errorFieldBindingPatterns, env);
    }

    @Override
    public void visit(BLangErrorMessageBindingPattern errorMessageBindingPattern) {
        analyzeNode(errorMessageBindingPattern.simpleBindingPattern, env);
    }

    @Override
    public void visit(BLangSimpleBindingPattern simpleBindingPattern) {
        analyzeNode(simpleBindingPattern.wildCardBindingPattern, env);
        analyzeNode(simpleBindingPattern.captureBindingPattern, env);
    }

    @Override
    public void visit(BLangErrorCauseBindingPattern errorCauseBindingPattern) {
        analyzeNode(errorCauseBindingPattern.simpleBindingPattern, env);
        analyzeNode(errorCauseBindingPattern.errorBindingPattern, env);
    }

    @Override
    public void visit(BLangErrorFieldBindingPatterns errorFieldBindingPatterns) {
        for (BLangNamedArgBindingPattern namedArgBindingPattern : errorFieldBindingPatterns.namedArgBindingPatterns) {
            analyzeNode(namedArgBindingPattern, env);
        }
        analyzeNode(errorFieldBindingPatterns.restBindingPattern, env);
    }

    @Override
    public void visit(BLangNamedArgBindingPattern namedArgBindingPattern) {
        analyzeNode(namedArgBindingPattern.argName, env);
        analyzeNode(namedArgBindingPattern.bindingPattern, env);
    }

    @Override
    public void visit(BLangErrorMatchPattern errorMatchPattern) {
        analyzeNode(errorMatchPattern.errorMessageMatchPattern, env);
        analyzeNode(errorMatchPattern.errorCauseMatchPattern, env);
        analyzeNode(errorMatchPattern.errorFieldMatchPatterns, env);
    }

    @Override
    public void visit(BLangErrorMessageMatchPattern errorMessageMatchPattern) {
        analyzeNode(errorMessageMatchPattern.simpleMatchPattern, env);
    }

    @Override
    public void visit(BLangSimpleMatchPattern simpleMatchPattern) {
        analyzeNode(simpleMatchPattern.wildCardMatchPattern, env);
        analyzeNode(simpleMatchPattern.constPattern, env);
        analyzeNode(simpleMatchPattern.varVariableName, env);
    }

    @Override
    public void visit(BLangErrorCauseMatchPattern errorCauseMatchPattern) {
        analyzeNode(errorCauseMatchPattern.simpleMatchPattern, env);
        analyzeNode(errorCauseMatchPattern.errorMatchPattern, env);
    }

    @Override
    public void visit(BLangErrorFieldMatchPatterns errorFieldMatchPatterns) {
        for (BLangNamedArgMatchPattern namedArgMatchPattern : errorFieldMatchPatterns.namedArgMatchPatterns) {
            analyzeNode(namedArgMatchPattern, env);
        }
        analyzeNode(errorFieldMatchPatterns.restMatchPattern, env);
    }

    @Override
    public void visit(BLangNamedArgMatchPattern namedArgMatchPattern) {
        analyzeNode(namedArgMatchPattern.argName, env);
        analyzeNode(namedArgMatchPattern.matchPattern, env);
    }

    @Override
    public void visit(BLangListBindingPattern listBindingPattern) {
        for (BLangBindingPattern bindingPattern : listBindingPattern.bindingPatterns) {
            analyzeNode(bindingPattern, env);
        }
    }

    @Override
    public void visit(BLangRestBindingPattern restBindingPattern) {
    }

    @Override
    public void visit(BLangMappingBindingPattern mappingBindingPattern) {
        for (BLangFieldBindingPattern fieldBindingPattern : mappingBindingPattern.fieldBindingPatterns) {
            analyzeNode(fieldBindingPattern, env);
        }
    }

    @Override
    public void visit(BLangFieldBindingPattern fieldBindingPattern) {
        analyzeNode(fieldBindingPattern.fieldName, env);
        analyzeNode(fieldBindingPattern.bindingPattern, env);
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
        for (BLangMatchPattern matchPattern : matchClause.matchPatterns) {
            analyzeNode(matchPattern, env);
        }

        BLangMatchGuard matchGuard = matchClause.matchGuard;
        if (matchGuard != null) {
            analyzeNode(matchGuard, env);
        }

        analyzeNode(matchClause.blockStmt, env);
    }

    @Override
    public void visit(BLangForeach foreach) {
        analyzeNode(foreach.collection, env);
        analyzeNode(foreach.body, env);

        BLangOnFailClause onFailClause = foreach.onFailClause;
        if (onFailClause != null) {
            analyzeNode(onFailClause, env);
        }
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        SymbolEnv fromEnv = fromClause.env;
        analyzeNode((BLangNode) fromClause.getVariableDefinitionNode(), fromEnv);
        analyzeNode(fromClause.collection, fromEnv);
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        SymbolEnv joinEnv = joinClause.env;
        analyzeNode((BLangNode) joinClause.getVariableDefinitionNode(), joinEnv);
        analyzeNode(joinClause.collection, joinEnv);
        analyzeNode((BLangNode) joinClause.onClause, joinEnv);
    }

    @Override
    public void visit(BLangLetClause letClause) {
        SymbolEnv letClauseEnv = letClause.env;
        for (BLangLetVariable letVarDeclaration : letClause.letVarDeclarations) {
            analyzeNode((BLangNode) letVarDeclaration.definitionNode, letClauseEnv);
        }
    }

    @Override
    public void visit(BLangOnClause onClause) {
        analyzeNode(onClause.lhsExpr, env);
        analyzeNode(onClause.rhsExpr, env);
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause) {
        analyzeNode(orderKeyClause.expression, env);
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        SymbolEnv orderByEnv = orderByClause.env;
        for (OrderKeyNode orderKeyNode : orderByClause.orderByKeyList) {
            analyzeNode((BLangExpression) orderKeyNode.getOrderKey(), orderByEnv);
        }
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        analyzeNode(selectClause.expression, selectClause.env);
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        analyzeNode(whereClause.expression, whereClause.env);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        analyzeNode(doClause.body, doClause.env);
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        analyzeNode(onFailClause.body, env);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        analyzeNode(onConflictClause.expression, env);
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        analyzeNode(limitClause.expression, env);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        analyzeNode(whileNode.expr, env);
        analyzeNode(whileNode.body, env);

        BLangOnFailClause onFailClause = whileNode.onFailClause;
        if (onFailClause != null) {
            analyzeNode(onFailClause, env);
        }
    }

    @Override
    public void visit(BLangLock lockNode) {
        boolean prevInLockStatement = this.inLockStatement;
        this.inLockStatement = true;
        copyInLockInfoStack.push(new LockInfo(lockNode));

        analyzeNode(lockNode.body, SymbolEnv.createLockEnv(lockNode, env));

        LockInfo copyInLockInfo = copyInLockInfoStack.pop();

        this.inLockStatement = prevInLockStatement;

        BLangOnFailClause onFailClause = lockNode.onFailClause;
        if (onFailClause != null) {
            analyzeNode(onFailClause, env);
        }

        Map<BSymbol, List<BLangSimpleVarRef>> accessedRestrictedVars = copyInLockInfo.accessedRestrictedVars;
        Set<BSymbol> accessedRestrictedVarKeys = accessedRestrictedVars.keySet();
        Set<BSymbol> accessedNonImmutableAndNonIsolatedVars = copyInLockInfo.accessedPotentiallyIsolatedVars;

        if (!accessedRestrictedVarKeys.isEmpty()) {
            if (accessedRestrictedVarKeys.size() > 1) {
                for (BSymbol accessedRestrictedVarKey : accessedRestrictedVarKeys) {
                    for (BLangSimpleVarRef varRef : accessedRestrictedVars.get(accessedRestrictedVarKey)) {
                        dlog.error(varRef.pos, DiagnosticErrorCode.INVALID_USAGE_OF_MULTIPLE_RESTRICTED_VARS_IN_LOCK);
                    }
                }
            }

            for (BLangSimpleVarRef varRef : copyInLockInfo.nonCaptureBindingPatternVarRefsOnLhs) {
                dlog.error(varRef.pos, DiagnosticErrorCode.INVALID_ASSIGNMENT_IN_LOCK_WITH_RESTRICTED_VAR_USAGE);
            }

            for (BLangExpression expr : copyInLockInfo.nonIsolatedTransferInExpressions) {
                dlog.error(expr.pos, DiagnosticErrorCode.INVALID_TRANSFER_INTO_LOCK_WITH_RESTRICTED_VAR_USAGE);
            }

            for (BLangExpression expr : copyInLockInfo.nonIsolatedTransferOutExpressions) {
                dlog.error(expr.pos, DiagnosticErrorCode.INVALID_TRANSFER_OUT_OF_LOCK_WITH_RESTRICTED_VAR_USAGE);
            }

            for (BLangInvocation invocation : copyInLockInfo.nonIsolatedInvocations) {
                dlog.error(invocation.pos,
                           DiagnosticErrorCode.INVALID_NON_ISOLATED_INVOCATION_IN_LOCK_WITH_RESTRICTED_VAR_USAGE);
            }
        }

        if (copyInLockInfoStack.empty()) {
            return;
        }

        BLangLock lastCheckedLockNode = lockNode;

        for (int i = copyInLockInfoStack.size() - 1; i >= 0; i--) {
            LockInfo prevCopyInLockInfo = copyInLockInfoStack.get(i);

            BLangLock outerLockNode = prevCopyInLockInfo.lockNode;

            if (!isEnclosedLockWithinSameFunction(lastCheckedLockNode, outerLockNode)) {
                return;
            }

            lastCheckedLockNode = outerLockNode;

            Map<BSymbol, List<BLangSimpleVarRef>> prevLockAccessedRestrictedVars =
                    prevCopyInLockInfo.accessedRestrictedVars;

            for (Map.Entry<BSymbol, List<BLangSimpleVarRef>> entry : accessedRestrictedVars.entrySet()) {
                BSymbol key = entry.getKey();

                if (prevLockAccessedRestrictedVars.containsKey(key)) {
                    prevLockAccessedRestrictedVars.get(key).addAll(entry.getValue());
                    continue;
                }

                prevLockAccessedRestrictedVars.put(key, entry.getValue());
            }

            prevCopyInLockInfo.accessedPotentiallyIsolatedVars.addAll(accessedNonImmutableAndNonIsolatedVars);

            if (!accessedRestrictedVars.isEmpty()) {
                continue;
            }

            prevCopyInLockInfo.nonCaptureBindingPatternVarRefsOnLhs.addAll(
                    copyInLockInfo.nonCaptureBindingPatternVarRefsOnLhs);
            prevCopyInLockInfo.nonIsolatedTransferInExpressions.addAll(copyInLockInfo.nonIsolatedTransferInExpressions);
            prevCopyInLockInfo.nonIsolatedTransferOutExpressions.addAll(
                    copyInLockInfo.nonIsolatedTransferOutExpressions);
            prevCopyInLockInfo.nonIsolatedInvocations.addAll(copyInLockInfo.nonIsolatedInvocations);
            prevCopyInLockInfo.accessedPotentiallyIsolatedVars.addAll(copyInLockInfo.accessedPotentiallyIsolatedVars);
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        analyzeNode(transactionNode.transactionBody, env);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        BLangTupleVarRef varRef = stmt.varRef;
        BLangExpression expr = stmt.expr;

        analyzeNode(varRef, env);
        analyzeNode(expr, env);

        validateTransferOut(varRef, expr);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        BLangRecordVarRef varRef = stmt.varRef;
        BLangExpression expr = stmt.expr;

        analyzeNode(varRef, env);
        analyzeNode(expr, env);

        validateTransferOut(varRef, expr);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        BLangErrorVarRef varRef = stmt.varRef;
        BLangExpression expr = stmt.expr;

        analyzeNode(varRef, env);
        analyzeNode(expr, env);

        validateTransferOut(varRef, expr);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        analyzeNode(rollbackNode.expr, env);
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
    }

    @Override
    public void visit(BLangConstRef constRef) {
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValuePair =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                if (keyValuePair.key.computedKey) {
                    analyzeNode(keyValuePair.key.expr, env);
                }
                analyzeNode(keyValuePair.valueExpr, env);
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                analyzeNode((BLangRecordLiteral.BLangRecordVarNameField) field, env);
            } else {
                analyzeNode(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr, env);
            }
        }
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        for (BLangExpression expression : varRefExpr.expressions) {
            analyzeNode(expression, env);
        }

        BLangExpression restParam = (BLangExpression) varRefExpr.restParam;
        if (restParam != null) {
            analyzeNode(restParam, env);
        }
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        for (BLangRecordVarRef.BLangRecordVarRefKeyValue recordRefField : varRefExpr.recordRefFields) {
            analyzeNode(recordRefField.variableReference, env);
        }

        BLangExpression restParam = (BLangExpression) varRefExpr.restParam;
        if (restParam != null) {
            analyzeNode(restParam, env);
        }
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        analyzeNode(varRefExpr.message, env);

        BLangVariableReference cause = varRefExpr.cause;
        if (cause != null) {
            analyzeNode(cause, env);
        }

        for (BLangNamedArgsExpression namedArgsExpression : varRefExpr.detail) {
            analyzeNode(namedArgsExpression, env);
        }

        BLangVariableReference restVar = varRefExpr.restVar;
        if (restVar != null) {
            analyzeNode(restVar, env);
        }

        BLangType typeNode = varRefExpr.typeNode;
        if (typeNode != null) {
            analyzeNode(typeNode, env);
        }
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        BType accessType = varRefExpr.getBType();

        BSymbol symbol = varRefExpr.symbol;
        BLangInvokableNode enclInvokable = env.enclInvokable;
        BLangType enclType = env.enclType;

        if (symbol == null) {
            return;
        }

        BLangNode parent = varRefExpr.parent;
        boolean isolatedModuleVariableReference = isIsolatedModuleVariableSymbol(symbol);

        boolean accessOfPotentiallyIsolatedVariable = false;
        boolean accessOfPotentiallyReadOnlyOrIsolatedObjectTypedFinalVariable = false;
        Set<BSymbol> inferableClasses = new HashSet<>();

        if ((symbol.owner.tag & SymTag.PACKAGE) == SymTag.PACKAGE) {
            accessOfPotentiallyIsolatedVariable = this.isolationInferenceInfoMap.containsKey(symbol) &&
                            this.isolationInferenceInfoMap.get(symbol).getKind() != IsolationInferenceKind.FUNCTION;

            accessOfPotentiallyReadOnlyOrIsolatedObjectTypedFinalVariable = Symbols.isFlagOn(symbol.flags, Flags.FINAL)
                    && !types.isSubTypeOfReadOnlyOrIsolatedObjectUnion(accessType)
                    && isSubtypeOfReadOnlyOrIsolatedObjectOrInferableObject(symbol.owner, accessType, inferableClasses);
        }

        if (inLockStatement) {
            LockInfo exprInfo = copyInLockInfoStack.peek();

            if (isolatedModuleVariableReference || isMethodCallOnSelfInIsolatedObject(varRefExpr, parent)) {
                addToAccessedRestrictedVars(exprInfo.accessedRestrictedVars, varRefExpr);
            }

            if (parent == null && varRefExpr.isLValue) {
                if (!isSelfOfObject(varRefExpr) && isInvalidCopyIn(varRefExpr, env)) {
                    exprInfo.nonCaptureBindingPatternVarRefsOnLhs.add(varRefExpr);
                }
            } else if ((!varRefExpr.isLValue || parent.getKind() != NodeKind.ASSIGNMENT) &&
                    !isIsolated(varRefExpr.symbol.flags) &&
                    !isSelfOfIsolatedObject(varRefExpr) &&
                    isInvalidCopyIn(varRefExpr, env)) {
                exprInfo.nonIsolatedTransferInExpressions.add(varRefExpr);
            }

            if (accessOfPotentiallyIsolatedVariable) {
                ((VariableIsolationInferenceInfo) this.isolationInferenceInfoMap.get(symbol)).accessedLockInfo
                        .add(exprInfo);
                exprInfo.accessedPotentiallyIsolatedVars.add(symbol);
            }
        } else if (accessOfPotentiallyIsolatedVariable ||
                accessOfPotentiallyReadOnlyOrIsolatedObjectTypedFinalVariable) {
            VariableIsolationInferenceInfo inferenceInfo =
                    (VariableIsolationInferenceInfo) this.isolationInferenceInfoMap.get(symbol);
            inferenceInfo.accessedOutsideLockStatement = true;
            inferenceInfo.accessOutsideLockStatementValidIfInferredIsolated = false;

            if (accessOfPotentiallyReadOnlyOrIsolatedObjectTypedFinalVariable) {
                inferenceInfo.dependsOnVariablesAndClasses.addAll(inferableClasses);
            }
        }

        boolean inIsolatedFunction = isInIsolatedFunction(env);
        boolean recordFieldDefaultValue = isRecordFieldDefaultValue(enclType);
        boolean objectFieldDefaultValueRequiringIsolation = !recordFieldDefaultValue &&
                isObjectFieldDefaultValueRequiringIsolation(env);

        SymbolEnv enclEnv = env.enclEnv;
        if (inIsolatedFunction) {
            if (enclInvokable == null) {
                BLangArrowFunction bLangArrowFunction = (BLangArrowFunction) enclEnv.node;

                for (BLangSimpleVariable param : bLangArrowFunction.params) {
                    if (param.symbol == symbol) {
                        return;
                    }
                }
            }
        }

        if (!recordFieldDefaultValue && !objectFieldDefaultValueRequiringIsolation && enclInvokable != null &&
                isReferenceToVarDefinedInSameInvokable(symbol.owner, enclInvokable.symbol)) {
            return;
        }

        long flags = symbol.flags;
        if (Symbols.isFlagOn(flags, Flags.CONSTANT)) {
            return;
        }

        if ((Symbols.isFlagOn(flags, Flags.FINAL) || Symbols.isFlagOn(flags, Flags.FUNCTION_FINAL)) &&
                types.isSubTypeOfReadOnlyOrIsolatedObjectUnion(accessType)) {
            return;
        }

        if (isDefinitionReference(symbol)) {
            return;
        }

        if (enclEnv != null && enclEnv.node != null && enclEnv.node.getKind() == NodeKind.ARROW_EXPR) {
            BLangArrowFunction bLangArrowFunction = (BLangArrowFunction) enclEnv.node;

            for (BLangSimpleVariable param : bLangArrowFunction.params) {
                if (param.symbol == symbol) {
                    return;
                }
            }
        }

        if (isolatedModuleVariableReference) {
            if (inLockStatement) {
                return;
            }

            if (recordFieldDefaultValue) {
                dlog.error(varRefExpr.pos,
                           DiagnosticErrorCode.INVALID_ISOLATED_VARIABLE_ACCESS_OUTSIDE_LOCK_IN_RECORD_DEFAULT);
            } else {
                dlog.error(varRefExpr.pos, DiagnosticErrorCode.INVALID_ISOLATED_VARIABLE_ACCESS_OUTSIDE_LOCK);
            }
            return;
        }

        if (accessOfPotentiallyIsolatedVariable) {
            markDependentlyIsolatedOnVar(symbol);
        } else {
            markDependsOnIsolationNonInferableConstructs();
        }

        inferredIsolated = false;

        if (inIsolatedFunction) {
            dlog.error(varRefExpr.pos, DiagnosticErrorCode.INVALID_MUTABLE_ACCESS_IN_ISOLATED_FUNCTION);
            return;
        }

        if (recordFieldDefaultValue) {
            dlog.error(varRefExpr.pos, DiagnosticErrorCode.INVALID_MUTABLE_ACCESS_AS_RECORD_DEFAULT);
            return;
        }

        if (objectFieldDefaultValueRequiringIsolation) {
            dlog.error(varRefExpr.pos, DiagnosticErrorCode.INVALID_MUTABLE_ACCESS_AS_OBJECT_DEFAULT);
            return;
        }

        if (isObjectFieldDefaultValue(env)) {
            BLangFunction initFunction = ((BLangClassDefinition) env.node).initFunction;
            if (initFunction != null) {
                markInitMethodDependentlyIsolatedOnVar(initFunction, symbol);
            }
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        analyzeFieldBasedAccess(fieldAccessExpr);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess nsPrefixedFieldBasedAccess) {
        analyzeFieldBasedAccess(nsPrefixedFieldBasedAccess);
    }

    private void analyzeFieldBasedAccess(BLangFieldBasedAccess fieldAccessExpr) {
        BLangExpression expr = fieldAccessExpr.expr;
        analyzeNode(expr, env);

        if (!isInvalidIsolatedObjectFieldOrMethodAccessViaSelfIfOutsideLock(fieldAccessExpr, true)) {
            BType bType = expr.getBType();
            BTypeSymbol tsymbol = bType.tsymbol;
            BLangIdentifier field = fieldAccessExpr.field;

            if (!isPotentiallyProtectedFieldAccessedInNonInitMethod(expr, tsymbol, field)) {
                return;
            }

            if (inLockStatement) {
                LockInfo lockInfo = copyInLockInfoStack.peek();
                ((VariableIsolationInferenceInfo) this.isolationInferenceInfoMap.get(tsymbol)).accessedLockInfo
                        .add(lockInfo);
                lockInfo.accessedPotentiallyIsolatedVars.add(tsymbol);
                return;
            }

            VariableIsolationInferenceInfo inferenceInfo =
                    (VariableIsolationInferenceInfo) this.isolationInferenceInfoMap.get(tsymbol);
            inferenceInfo.accessedOutsideLockStatement = true;

            BType fieldType = fieldAccessExpr.getBType();
            if (Symbols.isFlagOn(((BObjectType) bType).fields.get(field.value).symbol.flags, Flags.FINAL) &&
                    isSubtypeOfReadOnlyOrIsolatedObjectOrInferableObject(env.enclPkg.symbol, fieldType)) {
                inferenceInfo.typesOfFinalFieldsAccessedOutsideLock.add(fieldType);
            } else {
                inferenceInfo.accessOutsideLockStatementValidIfInferredIsolated = false;
            }
            return;
        }

        if (inLockStatement) {
            addToAccessedRestrictedVars(copyInLockInfoStack.peek().accessedRestrictedVars, (BLangSimpleVarRef) expr);
            return;
        }

        dlog.error(fieldAccessExpr.pos,
                   DiagnosticErrorCode.INVALID_MUTABLE_FIELD_ACCESS_IN_ISOLATED_OBJECT_OUTSIDE_LOCK);
    }

    private boolean isPotentiallyProtectedFieldAccessedInNonInitMethod(BLangExpression expr, BTypeSymbol tsymbol,
                                                                       BLangIdentifier field) {
        return expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                isSelfOfObject((BLangSimpleVarRef) expr) &&
                this.isolationInferenceInfoMap.containsKey(tsymbol) &&
                !inObjectInitMethod() &&
                ((ClassIsolationInferenceInfo) this.isolationInferenceInfoMap.get(tsymbol))
                        .protectedFields.contains(field);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        analyzeNode(indexAccessExpr.expr, env);
        analyzeNode(indexAccessExpr.indexExpr, env);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        analyzeInvocation(invocationExpr);
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        for (BLangExpression positionalArg : errorConstructorExpr.positionalArgs) {
            analyzeNode(positionalArg, env);
        }
        for (BLangNamedArgsExpression namedArgsExpression : errorConstructorExpr.namedArgs) {
            analyzeNode(namedArgsExpression, env);
        }
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        if (!actionInvocationExpr.async) {
            analyzeInvocation(actionInvocationExpr);
            return;
        }

        boolean isWorker = actionInvocationExpr.functionPointerInvocation;
        if (isInIsolatedFunction(env)) {
            if (checkStrandAnnotationExists(actionInvocationExpr.annAttachments, true, isWorker)) {
                return;
            }

            if (!isWorker && !isValidIsolatedAsyncInvocation(actionInvocationExpr)) {
                return;
            }

            analyzeInvocation(actionInvocationExpr);
            return;
        }

        if (checkStrandAnnotationExists(actionInvocationExpr.annAttachments, false, isWorker)) {
            markDependsOnIsolationNonInferableConstructs();
            inferredIsolated = false;
            return;
        }

        analyzeInvocation(actionInvocationExpr);
    }

    private boolean checkStrandAnnotationExists(List<BLangAnnotationAttachment> attachments, boolean inIsolatedFunc,
                                                boolean isWorker) {
        BAnnotationSymbol strandAnnotSymbol = symResolver.getStrandAnnotationSymbol();
        for (BLangAnnotationAttachment attachment : attachments) {
            if (attachment.annotationSymbol == strandAnnotSymbol) {
                if (inIsolatedFunc) {
                    String actionInvocation = isWorker ? "worker declaration" : "start action";
                    dlog.error(attachment.pos, DiagnosticErrorCode.INVALID_STRAND_ANNOTATION_IN_ISOLATED_FUNCTION,
                            actionInvocation);
                } else {
                    dlog.warning(attachment.pos, DiagnosticWarningCode.USAGE_OF_STRAND_ANNOTATION_WILL_BE_DEPRECATED);
                }
                return true;
            }
        }
        return false;
    }

    private boolean isValidIsolatedAsyncInvocation(BLangInvocation.BLangActionInvocation actionInvocation) {
        boolean isIsolatedStartAction = true;
        if (!isIsolated(actionInvocation.symbol.type.flags)) {
            dlog.error(actionInvocation.name.pos,
                    DiagnosticErrorCode.INVALID_ASYNC_INVOCATION_OF_NON_ISOLATED_FUNCTION_IN_ISOLATED_FUNCTION);
            isIsolatedStartAction = false;
        }

        if (actionInvocation.expr != null && !isIsolatedExpression(actionInvocation.expr)) {
            dlog.error(actionInvocation.expr.pos,
                    DiagnosticErrorCode.INVALID_ACCESS_OF_NON_ISOLATED_EXPR_IN_ARGS_OF_ASYNC_INV_OF_ISOLATED_FUNC);
            isIsolatedStartAction = false;
        }

        if (!containsIsolatedExpressionsForAllArgs(actionInvocation.requiredArgs)) {
            isIsolatedStartAction = false;
        }

        if (!containsIsolatedExpressionsForAllArgs(actionInvocation.restArgs)) {
            isIsolatedStartAction = false;
        }

        return isIsolatedStartAction;
    }

    private boolean containsIsolatedExpressionsForAllArgs(List<BLangExpression> args) {
        boolean containIsolatedExprs = true;
        for (BLangExpression arg : args) {
            if (!isIsolatedExpression(arg)) {
                dlog.error(arg.pos,
                        DiagnosticErrorCode.INVALID_ACCESS_OF_NON_ISOLATED_EXPR_IN_ARGS_OF_ASYNC_INV_OF_ISOLATED_FUNC);
                containIsolatedExprs = false;
            }
        }
        return containIsolatedExprs;
    }

    @Override
    public void visit(BLangInvocation.BLangResourceAccessInvocation resourceAccessInvocation) {
        analyzeNode(resourceAccessInvocation.resourceAccessPathSegments, env);
        analyzeInvocation(resourceAccessInvocation);
    }
    
    @Override
    public void visit(BLangTypeInit typeInitExpr) {
        BInvokableSymbol initInvocationSymbol =
                                        (BInvokableSymbol) ((BLangInvocation) typeInitExpr.initInvocation).symbol;
        if (initInvocationSymbol != null && !isIsolated(initInvocationSymbol.flags)) {
            analyzeFunctionForInference(initInvocationSymbol);

            inferredIsolated = false;

            if (isInIsolatedFunction(env.enclInvokable)) {
                dlog.error(typeInitExpr.pos,
                           DiagnosticErrorCode.INVALID_NON_ISOLATED_INIT_EXPRESSION_IN_ISOLATED_FUNCTION);
            } else if (isRecordFieldDefaultValue(env.enclType)) {
                dlog.error(typeInitExpr.pos,
                           DiagnosticErrorCode.INVALID_NON_ISOLATED_INIT_EXPRESSION_AS_RECORD_DEFAULT);
            } else if (isObjectFieldDefaultValueRequiringIsolation(env)) {
                dlog.error(typeInitExpr.pos,
                           DiagnosticErrorCode.INVALID_NON_ISOLATED_INIT_EXPRESSION_AS_OBJECT_DEFAULT);
            } else if (isObjectFieldDefaultValue(env)) {
                BLangFunction initFunction = ((BLangClassDefinition) env.node).initFunction;
                if (initFunction != null) {
                    markInitMethodDependentlyIsolatedOnFunction(initFunction, initInvocationSymbol);
                }
            }
        }

        for (BLangExpression expression : typeInitExpr.argsExpr) {
            analyzeNode(expression, env);
        }
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        analyzeNode(ternaryExpr.expr, env);
        analyzeNode(ternaryExpr.thenExpr, env);
        analyzeNode(ternaryExpr.elseExpr, env);
    }

    @Override
    public void visit(BLangWaitExpr waitExpr) {
        for (BLangExpression expression : waitExpr.exprList) {
            analyzeNode(expression, env);
        }
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        analyzeNode(trapExpr.expr, env);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        analyzeNode(binaryExpr.lhsExpr, env);
        analyzeNode(binaryExpr.rhsExpr, env);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        analyzeNode(elvisExpr.lhsExpr, env);
        analyzeNode(elvisExpr.rhsExpr, env);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        analyzeNode(groupExpr.expression, env);
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        isolatedLetVarStack.push(new HashSet<>());

        for (BLangLetVariable letVarDeclaration : letExpr.letVarDeclarations) {
            analyzeNode((BLangNode) letVarDeclaration.definitionNode, env);
        }

        analyzeNode(letExpr.expr, env);
        isolatedLetVarStack.pop();
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        for (BLangExpression expr : listConstructorExpr.exprs) {
            if (expr.getKind() == NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                expr = ((BLangListConstructorExpr.BLangListConstructorSpreadOpExpr) expr).expr;
            }
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        for (BLangRecordLiteral recordLiteral : tableConstructorExpr.recordLiteralList) {
            analyzeNode(recordLiteral, env);
        }
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        analyzeNode(unaryExpr.expr, env);
    }

    @Override
    public void visit(BLangTypedescExpr typedescExpr) {
        analyzeNode(typedescExpr.typeNode, env);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        analyzeNode(conversionExpr.typeNode, env);
        analyzeNode(conversionExpr.expr, env);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        analyzeNode(xmlAttribute.name, env);
        analyzeNode(xmlAttribute.value, env);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        for (BLangExpression child : xmlElementLiteral.children) {
            analyzeNode(child, env);
        }

        for (BLangXMLAttribute attribute : xmlElementLiteral.attributes) {
            analyzeNode(attribute, env);
        }

        for (BLangXMLNS inlineNamespace : xmlElementLiteral.inlineNamespaces) {
            analyzeNode(inlineNamespace, env);
        }
    }

    @Override
    public void visit(BLangXMLSequenceLiteral xmlSequenceLiteral) {
        for (BLangExpression expr : xmlSequenceLiteral.xmlItems) {
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        for (BLangExpression expr : xmlTextLiteral.textFragments) {
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        for (BLangExpression textFragment : xmlCommentLiteral.textFragments) {
            analyzeNode(textFragment, env);
        }
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        for (BLangExpression dataFragment : xmlProcInsLiteral.dataFragments) {
            analyzeNode(dataFragment, env);
        }
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        for (BLangExpression textFragment : xmlQuotedString.textFragments) {
            analyzeNode(textFragment, env);
        }
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        for (BLangExpression expr : stringTemplateLiteral.exprs) {
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        for (BLangExpression insertion : rawTemplateLiteral.insertions) {
            analyzeNode(insertion, env);
        }
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        // Visit worker lambdas.
        BLangFunction function = bLangLambdaFunction.function;
        if (isWorkerLambda(function)) {
            visit(function);
        }
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        SymbolEnv arrowFunctionEnv = SymbolEnv.createArrowFunctionSymbolEnv(bLangArrowFunction, env);
        createTempSymbolIfNonExistent(bLangArrowFunction);
        analyzeNode(bLangArrowFunction.body, arrowFunctionEnv);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        analyzeNode(bLangVarArgsExpression.expr, env);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        analyzeNode(bLangNamedArgsExpression.expr, env);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        analyzeNode(checkedExpr.expr, env);
    }

    @Override
    public void visit(BLangDo doNode) {
        analyzeNode(doNode.body, env);
        if (doNode.onFailClause != null) {
            analyzeNode(doNode.onFailClause, env);
        }
    }

    @Override
    public void visit(BLangFail failExpr) {
        analyzeNode(failExpr.expr, env);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        analyzeNode(checkPanickedExpr.expr, env);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        analyzeNode(serviceConstructorExpr.serviceNode, env);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        analyzeNode(typeTestExpr.expr, env);
        analyzeNode(typeTestExpr.typeNode, env);
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        analyzeNode(annotAccessExpr.expr, env);
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        for (BLangNode clause : queryExpr.getQueryClauses()) {
            analyzeNode(clause, env);
        }
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr) {
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
    }

    @Override
    public void visit(BLangValueType valueType) {
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        analyzeNode(arrayType.getElementType(), env);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        analyzeNode(constrainedType.constraint, env);
    }

    @Override
    public void visit(BLangStreamType streamType) {
        analyzeNode(streamType.constraint, env);
        analyzeNode(streamType.error, env);
    }

    @Override
    public void visit(BLangTableTypeNode tableType) {
        analyzeNode(tableType.constraint, env);

        if (tableType.tableKeyTypeConstraint != null) {
            analyzeNode(tableType.tableKeyTypeConstraint.keyType, env);
        }
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        for (BLangVariable param : functionTypeNode.params) {
            analyzeNode(param.typeNode, env);
        }
        if (functionTypeNode.restParam != null) {
            analyzeNode(functionTypeNode.restParam.typeNode, env);
        }
        analyzeNode(functionTypeNode.returnTypeNode, env);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        for (BLangType memberTypeNode : unionTypeNode.memberTypeNodes) {
            analyzeNode(memberTypeNode, env);
        }
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        for (BLangType constituentTypeNode : intersectionTypeNode.constituentTypeNodes) {
            analyzeNode(constituentTypeNode, env);
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        SymbolEnv objectEnv = SymbolEnv.createTypeEnv(objectTypeNode, objectTypeNode.symbol.scope, env);

        for (BLangSimpleVariable field : objectTypeNode.fields) {
            analyzeNode(field, objectEnv);
        }

        for (BLangSimpleVariable referencedField : objectTypeNode.includedFields) {
            analyzeNode(referencedField, objectEnv);
        }

        BLangFunction initFunction = objectTypeNode.initFunction;
        if (initFunction != null) {
            analyzeNode(initFunction, objectEnv);
        }

        for (BLangFunction function : objectTypeNode.functions) {
            analyzeNode(function, objectEnv);
        }
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        SymbolEnv classEnv = SymbolEnv.createClassEnv(classDefinition, classDefinition.symbol.scope, env);

        for (BLangSimpleVariable bLangSimpleVariable : classDefinition.fields) {
            analyzeNode(bLangSimpleVariable, classEnv);
        }

        for (BLangSimpleVariable field : classDefinition.referencedFields) {
            analyzeNode(field, classEnv);
        }

        BLangFunction initFunction = classDefinition.initFunction;
        if (initFunction != null) {
            analyzeNode(initFunction, classEnv);
        }

        for (BLangFunction function : classDefinition.functions) {
            analyzeNode(function, classEnv);
        }
    }

    @Override
    public void visit(BLangObjectConstructorExpression objectConstructorExpression) {
        visit(objectConstructorExpression.typeInit);
    }

    @Override
    public void visit(BLangInferredTypedescDefaultNode inferTypedescExpr) {
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        SymbolEnv typeEnv = SymbolEnv.createTypeEnv(recordTypeNode, recordTypeNode.symbol.scope, env);

        boolean prevInLockStatement = this.inLockStatement;
        this.inLockStatement = false;
        for (BLangSimpleVariable field : recordTypeNode.fields) {
            analyzeNode(field, typeEnv);
        }
        this.inLockStatement = prevInLockStatement;

        for (BLangSimpleVariable referencedField : recordTypeNode.includedFields) {
            analyzeNode(referencedField, typeEnv);
        }

        BLangType restFieldType = recordTypeNode.restFieldType;
        if (restFieldType != null) {
            analyzeNode(restFieldType, typeEnv);
        }
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        for (BLangExpression expression : finiteTypeNode.valueSpace) {
            analyzeNode(expression, env);
        }
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        for (BLangType memberTypeNode : tupleTypeNode.memberTypeNodes) {
            analyzeNode(memberTypeNode, env);
        }

        analyzeNode(tupleTypeNode.restParamType, env);
    }

    @Override
    public void visit(BLangErrorType errorTypeNode) {
        analyzeNode(errorTypeNode.detailType, env);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        analyzeNode(bLangTupleVariable.typeNode, env);

        BLangExpression expr = bLangTupleVariable.expr;
        if (expr != null) {
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        analyzeNode(bLangTupleVariableDef.var, env);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        analyzeNode(bLangRecordVariable.typeNode, env);
        BLangExpression expr = bLangRecordVariable.expr;
        if (expr != null) {
            analyzeNode(expr, env);
        }
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        analyzeNode(bLangRecordVariableDef.var, env);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        analyzeNode(bLangErrorVariable.typeNode, env);
        analyzeNode(bLangErrorVariable.expr, env);

        for (BLangErrorVariable.BLangErrorDetailEntry bLangErrorDetailEntry : bLangErrorVariable.detail) {
            analyzeNode(bLangErrorDetailEntry.valueBindingPattern, env);
        }
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        analyzeNode(bLangErrorVariableDef.errorVariable, env);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        for (BLangWaitForAllExpr.BLangWaitKeyValue keyValuePair : waitForAllExpr.keyValuePairs) {
            analyzeNode(keyValuePair, env);
        }
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        BLangExpression keyExpr = waitKeyValue.keyExpr;
        if (keyExpr != null) {
            analyzeNode(keyExpr, env);
        }


        BLangExpression valueExpr = waitKeyValue.valueExpr;
        if (valueExpr != null) {
            analyzeNode(valueExpr, env);
        }
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        BLangExpression childIndex = xmlNavigation.childIndex;
        if (childIndex != null) {
            analyzeNode(childIndex, env);
        }
    }
    @Override
    public void visit(BLangClientDeclarationStatement clientDeclarationStatement) {
        analyzeNode(clientDeclarationStatement.clientDeclaration, env);
    }

    @Override
    public void visit(BLangRegExpTemplateLiteral regExpTemplateLiteral) {
        List<BLangExpression> interpolationsList =
                symResolver.getListOfInterpolations(regExpTemplateLiteral.reDisjunction.sequenceList);
        interpolationsList.forEach(interpolation -> analyzeNode(interpolation, env));
    }

    private void analyzeInvocation(BLangInvocation invocationExpr) {
        List<BLangExpression> requiredArgs = invocationExpr.requiredArgs;
        List<BLangExpression> restArgs = invocationExpr.restArgs;

        BLangExpression expr = invocationExpr.expr;
        if (expr != null && (requiredArgs.isEmpty() || requiredArgs.get(0) != expr)) {
            analyzeNode(expr, env);
        }

        BInvokableSymbol symbol = (BInvokableSymbol) invocationExpr.symbol;
        if (symbol == null) {
            analyzeArgs(requiredArgs, restArgs);
            return;
        }

        boolean inIsolatedFunction = isInIsolatedFunction(env);
        boolean recordFieldDefaultValue = isRecordFieldDefaultValue(env.enclType);
        boolean objectFieldDefaultValueRequiringIsolation = isObjectFieldDefaultValueRequiringIsolation(env);

        boolean expectsIsolation =
                inIsolatedFunction || recordFieldDefaultValue || objectFieldDefaultValueRequiringIsolation;

        boolean isolatedFunctionCall = isIsolated(symbol.type.flags);

        boolean inStartAction = invocationExpr.async && !invocationExpr.functionPointerInvocation;

        if (inStartAction) {
            analyzeFunctionInStartActionForInference(env, requiredArgs, restArgs, expr, symbol);
        }

        if (isolatedFunctionCall) {
            analyzeArgIsolatedness(invocationExpr, requiredArgs, restArgs, symbol, expectsIsolation);
            return;
        }

        analyzeArgs(requiredArgs, restArgs);

        if (inLockStatement) {
            copyInLockInfoStack.peek().nonIsolatedInvocations.add(invocationExpr);
        }

        long flags = symbol.flags;
        if (Symbols.isFlagOn(flags, Flags.ISOLATED_PARAM)) {
            return;
        }

        if (!inStartAction) {
            analyzeFunctionForInference(symbol);
        }

        inferredIsolated = false;

        if (inIsolatedFunction && !invocationExpr.functionPointerInvocation) {
            dlog.error(invocationExpr.pos, DiagnosticErrorCode.INVALID_NON_ISOLATED_INVOCATION_IN_ISOLATED_FUNCTION);
            return;
        }

        if (recordFieldDefaultValue) {
            dlog.error(invocationExpr.pos, DiagnosticErrorCode.INVALID_NON_ISOLATED_INVOCATION_AS_RECORD_DEFAULT);
        }

        if (objectFieldDefaultValueRequiringIsolation) {
            dlog.error(invocationExpr.pos, DiagnosticErrorCode.INVALID_NON_ISOLATED_INVOCATION_AS_OBJECT_DEFAULT);
        }  else if (isObjectFieldDefaultValue(env)) {
            BLangFunction initFunction = ((BLangClassDefinition) env.node).initFunction;
            if (initFunction != null) {
                markInitMethodDependentlyIsolatedOnFunction(initFunction, symbol);
            }
        }
    }

    private void markFunctionDependentlyIsolatedOnStartAction(BInvokableSymbol enclInvokableSymbol,
                                                              Set<BLangExpression> argsList, BInvokableSymbol symbol) {
        boolean isIsolatedFunction = isIsolated(symbol.type.flags);
        if (!isIsolatedFunction && Symbols.isFlagOn(symbol.flags, Flags.PUBLIC)) {
            markDependsOnIsolationNonInferableConstructs();
            return;
        }

        if (!this.isolationInferenceInfoMap.containsKey(enclInvokableSymbol)) {
            return;
        }

        if (!isIsolatedFunction) {
            this.isolationInferenceInfoMap.get(enclInvokableSymbol).dependsOnFunctions.add(symbol);
        }

        this.isolationInferenceInfoMap.get(enclInvokableSymbol).dependsOnFuncCallArgExprs.addAll(argsList);
    }

    private boolean isInIsolatedFunction(SymbolEnv env) {
        BLangInvokableNode enclInvokable = env.enclInvokable;
        if (enclInvokable != null && enclInvokable.flagSet.contains(Flag.WORKER)) {
            return isInIsolatedFunction(getWorkerEnclosedFunctionInvokable());
        }
        return isInIsolatedFunction(enclInvokable);
    }

    private BLangInvokableNode getWorkerEnclosedFunctionInvokable() {
        SymbolEnv env = this.env;
        while (env != null) {
            BLangNode node = env.node;
            if (node != null && (node.getKind() == NodeKind.FUNCTION || node.getKind() == NodeKind.RESOURCE_FUNC) &&
                    !isWorkerLambda((BLangFunction) node)) {
                return env.enclInvokable;
            }
            env = env.enclEnv;
        }
        return this.env.enclInvokable;
    }

    private void analyzeArgs(List<BLangExpression> requiredArgs, List<BLangExpression> restArgs) {
        List<BLangExpression> args = new ArrayList<>(requiredArgs);
        args.addAll(restArgs);
        for (BLangExpression argExpr : args) {
            analyzeNode(argExpr, env);
        }
    }

    private void analyzeAndSetArrowFuncFlagForIsolatedParamArg(BLangExpression arg) {
        if (arg.getKind() == NodeKind.REST_ARGS_EXPR) {
            BLangExpression expr = ((BLangRestArgsExpression) arg).expr;
            if (expr.getKind() != NodeKind.LIST_CONSTRUCTOR_EXPR) {
                analyzeNode(arg, env);
                return;
            }

            for (BLangExpression expression : ((BLangListConstructorExpr) expr).exprs) {
                analyzeAndSetArrowFuncFlagForIsolatedParamArg(expression);
            }
            return;
        }

        boolean namedArg = arg.getKind() == NodeKind.NAMED_ARGS_EXPR;
        BLangExpression argExpr = namedArg ? ((BLangNamedArgsExpression) arg).expr : arg;

        if (argExpr.getKind() != NodeKind.ARROW_EXPR) {
            analyzeNode(argExpr, env);
            return;
        }

        boolean prevInferredIsolatedness = this.inferredIsolated;
        this.inferredIsolated = true;

        analyzeNode(argExpr, env);

        if (this.inferredIsolated) {
            BInvokableType invokableType = (BInvokableType) argExpr.getBType();
            BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) invokableType.tsymbol;

            BInvokableTypeSymbol dupInvokableTypeSymbol = new BInvokableTypeSymbol(tsymbol.tag,
                                                                                   tsymbol.flags | Flags.ISOLATED,
                                                                                   tsymbol.pkgID, null, tsymbol.owner,
                                                                                   tsymbol.pos, tsymbol.origin);
            dupInvokableTypeSymbol.params = tsymbol.params == null ? null : new ArrayList<>(tsymbol.params);
            BInvokableType dupInvokableType = new BInvokableType(invokableType.paramTypes, invokableType.restType,
                                                                 invokableType.retType, dupInvokableTypeSymbol);
            dupInvokableType.flags |= Flags.ISOLATED;
            dupInvokableTypeSymbol.type = dupInvokableType;
            argExpr.setBType(dupInvokableType);

            if (namedArg) {
                arg.setBType(dupInvokableType);
            }
        }
        this.inferredIsolated = prevInferredIsolatedness && this.inferredIsolated;
    }

    private void analyzeArgIsolatedness(BLangInvocation invocationExpr, List<BLangExpression> requiredArgs,
                                        List<BLangExpression> restArgs, BInvokableSymbol symbol,
                                        boolean expectsIsolation) {
        List<BVarSymbol> params = symbol.params;
        int paramsCount = params.size();

        if (restArgs.isEmpty()) {
            // Can have positional and named args.
            int nextParamIndex = 0;

            for (BLangExpression arg : requiredArgs) {
                if (arg.getKind() != NodeKind.NAMED_ARGS_EXPR) {
                    // Positional argument.
                    BVarSymbol varSymbol = params.get(nextParamIndex++);

                    if (!Symbols.isFlagOn(varSymbol.flags, Flags.ISOLATED_PARAM)) {
                        analyzeNode(arg, env);
                        continue;
                    }

                    analyzeAndSetArrowFuncFlagForIsolatedParamArg(arg);

                    handleNonExplicitlyIsolatedArgForIsolatedParam(invocationExpr, arg, expectsIsolation,
                                                                   arg.getBType(), arg.pos);

                    continue;
                }

                // Named argument, there cannot be positional arguments after this.
                String name = ((BLangNamedArgsExpression) arg).name.value;

                for (BVarSymbol param : params) {
                    if (!param.name.value.equals(name)) {
                        continue;
                    }

                    if (!Symbols.isFlagOn(param.flags, Flags.ISOLATED_PARAM)) {
                        analyzeNode(arg, env);
                        continue;
                    }

                    analyzeAndSetArrowFuncFlagForIsolatedParamArg(arg);

                    handleNonExplicitlyIsolatedArgForIsolatedParam(invocationExpr, arg, expectsIsolation,
                                                                   arg.getBType(), arg.pos);
                }
            }
            return;
        }

        // No parameter defaults are added and there should not be named args.
        int reqArgCount = requiredArgs.size();
        for (int i = 0; i < reqArgCount; i++) {
            BLangExpression arg = requiredArgs.get(i);
            if (!Symbols.isFlagOn(params.get(i).flags, Flags.ISOLATED_PARAM)) {
                analyzeNode(arg, env);
                continue;
            }

            if (arg.getBType() == symTable.semanticError) {
                continue;
            }

            analyzeAndSetArrowFuncFlagForIsolatedParamArg(arg);

            handleNonExplicitlyIsolatedArgForIsolatedParam(invocationExpr, arg, expectsIsolation,
                                                           arg.getBType(), arg.pos);
        }

        if (restArgs.get(restArgs.size() - 1).getKind() == NodeKind.REST_ARGS_EXPR) {
            BLangRestArgsExpression varArg = (BLangRestArgsExpression) restArgs.get(restArgs.size() - 1);
            BType varArgType = Types.getReferredType(varArg.getBType());
            Location varArgPos = varArg.pos;

            if (varArgType == symTable.semanticError) {
                return;
            }

            if (reqArgCount == paramsCount) {
                // Vararg is only for the rest param.
                if (!Symbols.isFlagOn(symbol.restParam.flags, Flags.ISOLATED_PARAM)) {
                    analyzeNode(varArg, env);
                    return;
                }

                analyzeAndSetArrowFuncFlagForIsolatedParamArg(varArg);

                analyzeVarArgIsolatedness(invocationExpr, varArg, varArgPos, expectsIsolation);
                return;
            }

            if (reqArgCount < paramsCount) {
                // Part of the non-rest params are provided via the vararg.
                BTupleType tupleType = (BTupleType) varArgType;
                List<BType> memberTypes = tupleType.tupleTypes;

                BLangExpression varArgExpr = varArg.expr;
                boolean listConstrVarArg =  varArgExpr.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR;
                BLangListConstructorExpr listConstructorExpr = listConstrVarArg ?
                        (BLangListConstructorExpr) varArgExpr : null;

                if (!listConstrVarArg) {
                    analyzeNode(varArg, env);
                }

                int tupleIndex = 0;
                for (int i = reqArgCount; i < paramsCount; i++) {
                    if (!Symbols.isFlagOn(params.get(i).flags, Flags.ISOLATED_PARAM)) {
                        if (listConstrVarArg) {
                            analyzeNode(listConstructorExpr.exprs.get(tupleIndex), env);
                        }
                        tupleIndex++;
                        continue;
                    }

                    BType type = memberTypes.get(tupleIndex);

                    BLangExpression arg = null;
                    if (listConstrVarArg) {
                        arg = listConstructorExpr.exprs.get(tupleIndex);
                        analyzeAndSetArrowFuncFlagForIsolatedParamArg(arg);
                        type = arg.getBType();
                    }

                    handleNonExplicitlyIsolatedArgForIsolatedParam(invocationExpr, arg, expectsIsolation,
                                                                   type, varArgPos);
                    tupleIndex++;
                }

                BVarSymbol restParam = symbol.restParam;

                if (restParam == null) {
                    return;
                }

                if (!Symbols.isFlagOn(restParam.flags, Flags.ISOLATED_PARAM)) {
                    if (listConstructorExpr == null) {
                        return;
                    }

                    List<BLangExpression> exprs = listConstructorExpr.exprs;
                    for (int i = tupleIndex; i < exprs.size(); i++) {
                        analyzeNode(exprs.get(i), env);
                    }
                    return;
                }

                int memberTypeCount = memberTypes.size();
                if (tupleIndex < memberTypeCount) {
                    for (int i = tupleIndex; i < memberTypeCount; i++) {
                        BType type = memberTypes.get(i);
                        BLangExpression arg = null;
                        if (listConstrVarArg) {
                            arg = listConstructorExpr.exprs.get(i);
                            analyzeAndSetArrowFuncFlagForIsolatedParamArg(arg);
                            type = arg.getBType();
                        }

                        handleNonExplicitlyIsolatedArgForIsolatedParam(invocationExpr, arg, expectsIsolation,
                                                                       type, varArgPos);
                    }
                }

                if (listConstrVarArg) {
                    List<BLangExpression> exprs = listConstructorExpr.exprs;
                    for (int i = tupleIndex; i < exprs.size(); i++) {
                        BLangExpression arg = exprs.get(i);
                        analyzeAndSetArrowFuncFlagForIsolatedParamArg(arg);

                        handleNonExplicitlyIsolatedArgForIsolatedParam(invocationExpr, arg, expectsIsolation,
                                                                       arg.getBType(), varArgPos);
                    }
                    return;
                }

                BType tupleRestType = tupleType.restType;
                if (tupleRestType == null) {
                    return;
                }

                handleNonExplicitlyIsolatedArgForIsolatedParam(invocationExpr, null, expectsIsolation,
                                                               tupleRestType, varArgPos);

                return;
            }
        }

        if (!Symbols.isFlagOn(symbol.restParam.flags, Flags.ISOLATED_PARAM)) {
            for (BLangExpression restArg : restArgs) {
                analyzeNode(restArg, env);
            }
            return;
        }

        // Args for rest param provided as both individual args and vararg.
        analyzeRestArgsForRestParam(invocationExpr, restArgs, symbol, expectsIsolation);
    }

    private void analyzeRestArgsForRestParam(BLangInvocation invocationExpr, List<BLangExpression> restArgs,
                                             BInvokableSymbol symbol, boolean expectsIsolation) {
        if (Symbols.isFlagOn(((BArrayType) symbol.restParam.type).eType.flags, Flags.ISOLATED)) {
            for (BLangExpression restArg : restArgs) {
                analyzeNode(restArg, env);
            }
            return;
        }

        for (BLangExpression restArg : restArgs) {
            analyzeAndSetArrowFuncFlagForIsolatedParamArg(restArg);
        }

        int size = restArgs.size();
        BLangExpression lastArg = restArgs.get(size - 1);

        boolean lastArgIsVarArg = lastArg.getKind() == NodeKind.REST_ARGS_EXPR;

        for (int i = 0; i < (lastArgIsVarArg ? size - 1 : size); i++) {
            BLangExpression arg = restArgs.get(i);
            handleNonExplicitlyIsolatedArgForIsolatedParam(invocationExpr, arg, expectsIsolation,
                                                           arg.getBType(), arg.pos);
        }

        if (lastArgIsVarArg) {
            analyzeVarArgIsolatedness(invocationExpr, (BLangRestArgsExpression) lastArg, lastArg.pos, expectsIsolation);
        }
    }

    private void analyzeVarArgIsolatedness(BLangInvocation invocationExpr, BLangRestArgsExpression restArgsExpression,
                                           Location pos, boolean expectsIsolation) {
        BLangExpression expr = restArgsExpression.expr;
        if (expr.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
            for (BLangExpression expression : ((BLangListConstructorExpr) expr).exprs) {
                analyzeAndSetArrowFuncFlagForIsolatedParamArg(expression);

                handleNonExplicitlyIsolatedArgForIsolatedParam(invocationExpr, expression, expectsIsolation,
                                                               expression.getBType(), pos);
            }
            return;
        }

        BType varArgType = Types.getReferredType(restArgsExpression.getBType());
        if (varArgType.tag == TypeTags.ARRAY) {
            handleNonExplicitlyIsolatedArgForIsolatedParam(invocationExpr, null, expectsIsolation,
                                                           ((BArrayType) varArgType).eType, pos);
            return;
        }

        BTupleType tupleType = (BTupleType) varArgType;

        for (BType type : tupleType.tupleTypes) {
            handleNonExplicitlyIsolatedArgForIsolatedParam(invocationExpr, null, expectsIsolation,
                                                           type, pos);
        }

        BType restType = tupleType.restType;
        if (restType != null) {
            handleNonExplicitlyIsolatedArgForIsolatedParam(invocationExpr, null, expectsIsolation, restType, pos);
        }
    }

    private void handleNonExplicitlyIsolatedArgForIsolatedParam(BLangInvocation invocationExpr, BLangExpression expr,
                                                                boolean expectsIsolation, BType type, Location pos) {
        if (Symbols.isFlagOn(type.flags, Flags.ISOLATED)) {
            return;
        }

        this.inferredIsolated = false;

        if (expectsIsolation) {
            dlog.error(pos, DiagnosticErrorCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
            return;
        }

        if (expr != null) {
            NodeKind kind = expr.getKind();
            if (kind == NodeKind.LAMBDA) {
                markFunctionDependentlyIsolatedOnFunction(env.enclInvokable,
                                                          ((BLangLambdaFunction) expr).function.symbol);
            } else if (kind == NodeKind.ARROW_EXPR) {
                markFunctionDependentlyIsolatedOnFunction(env.enclInvokable,
                                                          createTempSymbolIfNonExistent((BLangArrowFunction) expr));
            }
        } else {
            markDependsOnIsolationNonInferableConstructs();
        }

        if (inLockStatement) {
            copyInLockInfoStack.peek().nonIsolatedInvocations.add(invocationExpr);
        }
    }

    private boolean isInIsolatedFunction(BLangInvokableNode enclInvokable) {
        if (enclInvokable == null) {
            // TODO: 14/11/20 This feels hack-y but cannot think of a different approach without a class variable
            // maintaining isolated-ness.
            if (isNotInArrowFunctionBody(env)) {
                return false;
            }
            return isIsolated(((BLangArrowFunction) env.enclEnv.node).funcType.flags);
        }

        return isIsolated(enclInvokable.symbol.flags);
    }

    private boolean isRecordFieldDefaultValue(BLangType enclType) {
        if (enclType == null) {
            return false;
        }

        return enclType.getKind() == NodeKind.RECORD_TYPE;
    }

    private boolean isObjectFieldDefaultValueRequiringIsolation(SymbolEnv env) {
        if (!isObjectFieldDefaultValue(env)) {
            return false;
        }

        BLangClassDefinition classDefinition = (BLangClassDefinition) env.node;

        BLangFunction initFunction = classDefinition.initFunction;
        if (initFunction == null) {
            return true;
        }

        return isIsolated(initFunction.symbol.flags);
    }

    private boolean isObjectFieldDefaultValue(SymbolEnv env) {
        return env.node.getKind() == NodeKind.CLASS_DEFN;
    }

    private boolean isDefinitionReference(BSymbol symbol) {
        return Symbols.isTagOn(symbol, SymTag.TYPE_DEF) || Symbols.isTagOn(symbol, SymTag.FUNCTION);
    }

    private boolean isIsolated(long flags) {
        return Symbols.isFlagOn(flags, Flags.ISOLATED);
    }

    private boolean isIsolatedClassField() {
        BLangNode node = env.node;
        return node.getKind() == NodeKind.CLASS_DEFN && ((BLangClassDefinition) node).flagSet.contains(Flag.ISOLATED);
    }

    private boolean isExpectedToBeAPrivateField(BVarSymbol symbol, BType type) {
        return !Symbols.isFlagOn(symbol.flags, Flags.FINAL) || !types.isSubTypeOfReadOnlyOrIsolatedObjectUnion(type);
    }

    private boolean isIsolatedObjectFieldOrMethodAccessViaSelf(BLangFieldBasedAccess fieldAccessExpr,
                                                               boolean ignoreInit) {
        BLangExpression expr = fieldAccessExpr.expr;

        if (expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return false;
        }

        if (!isSelfOfObject((BLangSimpleVarRef) expr)) {
            return false;
        }

        return isInIsolatedObjectMethod(env, ignoreInit);
    }

    private boolean isInvalidIsolatedObjectFieldOrMethodAccessViaSelfIfOutsideLock(
            BLangFieldBasedAccess fieldAccessExpr, boolean ignoreInit) {
        if (!isIsolatedObjectFieldOrMethodAccessViaSelf(fieldAccessExpr, ignoreInit)) {
            return false;
        }

        BField field = ((BObjectType) env.enclInvokable.symbol.owner.type).fields.get(fieldAccessExpr.field.value);

        if (field == null) {
            // Bound method access.
            return false;
        }

        return isExpectedToBeAPrivateField(field.symbol, field.type);
    }

    private void validateIsolatedExpression(BType type, BLangExpression expression) {
        if (types.isSubTypeOfReadOnlyOrIsolatedObjectUnion(type)) {
            return;
        }

        validateIsolatedExpression(expression);
    }

    private void validateIsolatedExpression(BLangExpression expression) {
        isIsolatedExpression(expression, true, true, new ArrayList<>());
    }

    private boolean isIsolatedExpression(BLangExpression expression) {
        return isIsolatedExpression(expression, false, false, new ArrayList<>());
    }

    private boolean isIsolatedExpression(BLangExpression expression, boolean logErrors, boolean visitRestOnError,
                                         List<BLangExpression> nonIsolatedLocations) {
        return isIsolatedExpression(expression, logErrors, visitRestOnError, nonIsolatedLocations, false, null, null,
                null);
    }

    private boolean isIsolatedExpression(BLangExpression expression, boolean logErrors, boolean visitRestOnError,
                                         List<BLangExpression> nonIsolatedExpressions, boolean inferring,
                                         Set<BType> publiclyExposedObjectTypes,
                                         List<BLangClassDefinition> classDefinitions, Set<BSymbol> unresolvedSymbols) {
        BType type = expression.getBType();

        if (type != null &&
                isSubTypeOfReadOnlyOrIsolatedObjectUnionWithInference(publiclyExposedObjectTypes, classDefinitions,
                                                                      inferring, type, unresolvedSymbols)) {
            return true;
        }

        switch (expression.getKind()) {
            case SIMPLE_VARIABLE_REF:
                if (isReferenceOfLetVarInitializedWithAnIsolatedExpression((BLangSimpleVarRef) expression)) {
                    return true;
                }
                break;
            case LITERAL:
            case NUMERIC_LITERAL:
                return true;
            case LIST_CONSTRUCTOR_EXPR:
                for (BLangExpression expr : ((BLangListConstructorExpr) expression).exprs) {
                    if (isIsolatedExpression(expr, logErrors, visitRestOnError, nonIsolatedExpressions) || logErrors ||
                            visitRestOnError) {
                        continue;
                    }

                    return false;
                }
                return true;
            case TABLE_CONSTRUCTOR_EXPR:
                for (BLangRecordLiteral mappingConstr : ((BLangTableConstructorExpr) expression).recordLiteralList) {
                    if (isIsolatedExpression(mappingConstr, logErrors, visitRestOnError, nonIsolatedExpressions) ||
                            logErrors || visitRestOnError) {
                        continue;
                    }

                    return false;
                }
                return true;
            case RECORD_LITERAL_EXPR:
                for (RecordLiteralNode.RecordField field : ((BLangRecordLiteral) expression).fields) {
                    if (field.isKeyValueField()) {
                        BLangRecordLiteral.BLangRecordKeyValueField keyValueField =
                                (BLangRecordLiteral.BLangRecordKeyValueField) field;

                        BLangRecordLiteral.BLangRecordKey key = keyValueField.key;
                        if (key.computedKey) {
                            if (!isIsolatedExpression(key.expr, logErrors, visitRestOnError, nonIsolatedExpressions) &&
                                    !logErrors && !visitRestOnError) {
                                return false;
                            }
                        }

                        if (isIsolatedExpression(keyValueField.valueExpr, logErrors, visitRestOnError,
                                                 nonIsolatedExpressions) || logErrors || visitRestOnError) {
                            continue;
                        }
                        return false;
                    }

                    if (field.getKind() == NodeKind.RECORD_LITERAL_SPREAD_OP) {
                        if (isIsolatedExpression(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr,
                                                 logErrors, visitRestOnError, nonIsolatedExpressions) ||
                                logErrors || visitRestOnError) {
                            continue;
                        }
                        return false;
                    }

                    if (isIsolatedExpression((BLangRecordLiteral.BLangRecordVarNameField) field, logErrors,
                                             visitRestOnError, nonIsolatedExpressions) || logErrors ||
                                             visitRestOnError) {
                        continue;
                    }
                    return false;
                }
                return true;
            case XML_COMMENT_LITERAL:
                BLangXMLCommentLiteral commentLiteral = (BLangXMLCommentLiteral) expression;

                for (BLangExpression textFragment : commentLiteral.textFragments) {
                    if (isIsolatedExpression(textFragment, logErrors, visitRestOnError, nonIsolatedExpressions) ||
                            logErrors || visitRestOnError) {
                        continue;
                    }

                    return false;
                }

                BLangExpression commentLiteralConcatExpr = commentLiteral.concatExpr;
                if (commentLiteralConcatExpr == null) {
                    return true;
                }
                return isIsolatedExpression(commentLiteralConcatExpr, logErrors, visitRestOnError,
                                            nonIsolatedExpressions);
            case XML_TEXT_LITERAL:
                BLangXMLTextLiteral textLiteral = (BLangXMLTextLiteral) expression;

                for (BLangExpression textFragment : textLiteral.textFragments) {
                    if (isIsolatedExpression(textFragment, logErrors, visitRestOnError, nonIsolatedExpressions) ||
                            logErrors || visitRestOnError) {
                        continue;
                    }

                    return false;
                }

                BLangExpression textLiteralConcatExpr = textLiteral.concatExpr;
                if (textLiteralConcatExpr == null) {
                    return true;
                }
                return isIsolatedExpression(textLiteralConcatExpr, logErrors, visitRestOnError, nonIsolatedExpressions);
            case XML_PI_LITERAL:
                BLangXMLProcInsLiteral procInsLiteral = (BLangXMLProcInsLiteral) expression;

                for (BLangExpression dataFragment : procInsLiteral.dataFragments) {
                    if (isIsolatedExpression(dataFragment, logErrors, visitRestOnError, nonIsolatedExpressions) ||
                            logErrors || visitRestOnError) {
                        continue;
                    }

                    return false;
                }

                BLangExpression procInsLiteralConcatExpr = procInsLiteral.dataConcatExpr;
                if (procInsLiteralConcatExpr == null) {
                    return true;
                }
                return isIsolatedExpression(procInsLiteralConcatExpr, logErrors, visitRestOnError,
                                            nonIsolatedExpressions);
            case XML_ELEMENT_LITERAL:
                for (BLangExpression child : ((BLangXMLElementLiteral) expression).children) {
                    if (isIsolatedExpression(child, logErrors, visitRestOnError, nonIsolatedExpressions) || logErrors ||
                            visitRestOnError) {
                        continue;
                    }

                    return false;
                }
                return true;
            case XML_SEQUENCE_LITERAL:
                for (BLangExpression xmlItem : ((BLangXMLSequenceLiteral) expression).xmlItems) {
                    if (isIsolatedExpression(xmlItem, logErrors, visitRestOnError, nonIsolatedExpressions) ||
                            logErrors || visitRestOnError) {
                        continue;
                    }

                    return false;
                }
                return true;
            case RAW_TEMPLATE_LITERAL:
                for (BLangExpression insertion : ((BLangRawTemplateLiteral) expression).insertions) {
                    if (isIsolatedExpression(insertion, logErrors, visitRestOnError, nonIsolatedExpressions) ||
                            logErrors || visitRestOnError) {
                        continue;
                    }

                    return false;
                }
                return true;
            case STRING_TEMPLATE_LITERAL:
                for (BLangExpression expr : ((BLangStringTemplateLiteral) expression).exprs) {
                    if (isIsolatedExpression(expr, logErrors, visitRestOnError, nonIsolatedExpressions) || logErrors ||
                            visitRestOnError) {
                        continue;
                    }

                    return false;
                }
                return true;
            case TYPE_CONVERSION_EXPR:
                return isIsolatedExpression(((BLangTypeConversionExpr) expression).expr, logErrors, visitRestOnError,
                                            nonIsolatedExpressions);
            case CHECK_EXPR:
            case CHECK_PANIC_EXPR:
                return isIsolatedExpression(((BLangCheckedExpr) expression).expr, logErrors, visitRestOnError,
                                            nonIsolatedExpressions);
            case TRAP_EXPR:
                return isIsolatedExpression(((BLangTrapExpr) expression).expr, logErrors, visitRestOnError,
                                            nonIsolatedExpressions);
            case TERNARY_EXPR:
                BLangTernaryExpr ternaryExpr = (BLangTernaryExpr) expression;

                if (!isIsolatedExpression(ternaryExpr.expr, logErrors, visitRestOnError, nonIsolatedExpressions) &&
                        !logErrors && !visitRestOnError) {
                    return false;
                }

                if (!isIsolatedExpression(ternaryExpr.thenExpr, logErrors, visitRestOnError, nonIsolatedExpressions) &&
                        !logErrors && !visitRestOnError) {
                    return false;
                }

                return isIsolatedExpression(ternaryExpr.elseExpr, logErrors, visitRestOnError, nonIsolatedExpressions);
            case ELVIS_EXPR:
                BLangElvisExpr elvisExpr = (BLangElvisExpr) expression;

                if (!isIsolatedExpression(elvisExpr.lhsExpr, logErrors, visitRestOnError, nonIsolatedExpressions) &&
                        !logErrors && !visitRestOnError) {
                    return false;
                }

                return isIsolatedExpression(elvisExpr.rhsExpr, logErrors, visitRestOnError, nonIsolatedExpressions);
            case LET_EXPR:
                return isIsolatedExpression(((BLangLetExpression) expression).expr, logErrors, visitRestOnError,
                                            nonIsolatedExpressions);
            case GROUP_EXPR:
                return isIsolatedExpression(((BLangGroupExpr) expression).expression, logErrors, visitRestOnError,
                                            nonIsolatedExpressions);
            case TYPE_INIT_EXPR:
                BLangTypeInit typeInitExpr = (BLangTypeInit) expression;

                if (typeInitExpr == null) {
                    return true;
                }

                expression = typeInitExpr.initInvocation;
                break;
            case OBJECT_CTOR_EXPRESSION:
                var objectConstructorExpression = (BLangObjectConstructorExpression) expression;
                typeInitExpr = objectConstructorExpression.typeInit;

                if (typeInitExpr == null) {
                    return true;
                }

                expression = typeInitExpr.initInvocation;
                break;
        }

        if (expression.getKind() == NodeKind.INVOCATION) {
            BLangInvocation invocation = (BLangInvocation) expression;

            if (isCloneOrCloneReadOnlyInvocation(invocation)) {
                return true;
            }

            BSymbol invocationSymbol = invocation.symbol;
            if (invocationSymbol == null) {
                // This is `new` used with a stream.
                List<BLangExpression> argExprs = invocation.argExprs;
                if (argExprs.isEmpty()) {
                    return true;
                }

                return isIsolatedExpression(argExprs.get(0), logErrors, visitRestOnError, nonIsolatedExpressions);
            } else if (isIsolated(invocationSymbol.type.flags) ||
                    (inferring && this.isolationInferenceInfoMap.containsKey(invocationSymbol) &&
                            inferFunctionIsolation(invocationSymbol,
                                    this.isolationInferenceInfoMap.get(invocationSymbol), publiclyExposedObjectTypes,
                                    classDefinitions, unresolvedSymbols))) {
                List<BLangExpression> requiredArgs = invocation.requiredArgs;

                BLangExpression calledOnExpr = invocation.expr;

                if (calledOnExpr != null &&
                        (requiredArgs.isEmpty() || calledOnExpr != requiredArgs.get(0)) &&
                        (!isIsolatedExpression(calledOnExpr, logErrors, visitRestOnError, nonIsolatedExpressions) &&
                                !logErrors && !visitRestOnError)) {
                    return false;
                }

                for (BLangExpression requiredArg : requiredArgs) {
                    if (requiredArg.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                        if (isIsolatedExpression(((BLangNamedArgsExpression) requiredArg).expr, logErrors,
                                visitRestOnError, nonIsolatedExpressions) || logErrors || visitRestOnError) {
                            continue;
                        }
                        return false;
                    }

                    if (isIsolatedExpression(requiredArg, logErrors, visitRestOnError, nonIsolatedExpressions) ||
                            logErrors || visitRestOnError) {
                        continue;
                    }
                    return false;
                }

                for (BLangExpression restArg : invocation.restArgs) {
                    if (restArg.getKind() == NodeKind.REST_ARGS_EXPR) {
                        if (isIsolatedExpression(((BLangRestArgsExpression) restArg).expr, logErrors, visitRestOnError,
                                                 nonIsolatedExpressions)
                                || logErrors || visitRestOnError) {
                            continue;
                        }
                        return false;
                    }

                    if (isIsolatedExpression(restArg, logErrors, visitRestOnError, nonIsolatedExpressions) ||
                            logErrors || visitRestOnError) {
                        continue;
                    }
                    return false;
                }

                return true;
            }
        }

        if (logErrors) {
            dlog.error(expression.pos, DiagnosticErrorCode.INVALID_NON_ISOLATED_EXPRESSION_AS_INITIAL_VALUE);
        } else {
            nonIsolatedExpressions.add(expression);
        }

        return false;
    }

    private boolean isSubTypeOfReadOnlyOrIsolatedObjectUnionWithInference(Set<BType> publiclyExposedObjectTypes,
                                                                          List<BLangClassDefinition> classDefinitions,
                                                                          boolean inferring, BType type,
                                                                          Set<BSymbol> unresolvedSymbols) {
        type = Types.getReferredType(type);
        if (types.isSubTypeOfReadOnlyOrIsolatedObjectUnion(type)) {
            return true;
        }

        if (!inferring) {
            return false;
        }

        BTypeSymbol tsymbol = type.tsymbol;
        int tag = type.tag;
        if (tag == TypeTags.OBJECT) {
            if (this.isolationInferenceInfoMap.containsKey(tsymbol)) {
                return inferVariableOrClassIsolation(publiclyExposedObjectTypes, classDefinitions, tsymbol,
                                     (VariableIsolationInferenceInfo) this.isolationInferenceInfoMap.get(tsymbol),
                         true, unresolvedSymbols);
            }

            return false;
        }

        if (tag != TypeTags.UNION) {
            return false;
        }

        for (BType memberType : ((BUnionType) type).getMemberTypes()) {
            if (!isSubTypeOfReadOnlyOrIsolatedObjectUnionWithInference(publiclyExposedObjectTypes, classDefinitions,
                                                                       true, memberType, unresolvedSymbols)) {
                return false;
            }
        }
        return true;
    }

    private boolean isDependentlyIsolatedExpressionKind(BLangExpression expression) {
        switch (expression.getKind()) {
            case LIST_CONSTRUCTOR_EXPR:
            case TABLE_CONSTRUCTOR_EXPR:
            case RECORD_LITERAL_EXPR:
            case XML_COMMENT_LITERAL:
            case XML_TEXT_LITERAL:
            case XML_PI_LITERAL:
            case XML_ELEMENT_LITERAL:
            case XML_SEQUENCE_LITERAL:
            case RAW_TEMPLATE_LITERAL:
            case STRING_TEMPLATE_LITERAL:
            case TYPE_CONVERSION_EXPR:
            case CHECK_EXPR:
            case CHECK_PANIC_EXPR:
            case TRAP_EXPR:
            case TERNARY_EXPR:
            case ELVIS_EXPR:
                return true;
            case GROUP_EXPR:
                return isDependentlyIsolatedExpressionKind(((BLangGroupExpr) expression).expression);
        }
        return false;
    }

    private boolean isCloneOrCloneReadOnlyInvocation(BLangInvocation invocation) {
        if (!invocation.langLibInvocation) {
            return false;
        }

        String methodName = invocation.symbol.name.value;

        return invocation.symbol.pkgID.name.value.equals(VALUE_LANG_LIB) &&
                (methodName.equals(CLONE_LANG_LIB_METHOD) || methodName.equals(CLONE_READONLY_LANG_LIB_METHOD));
    }

    private boolean isInvalidTransferIn(BLangSimpleVarRef expression) {
        return isInvalidTransferIn(expression, isSelfOfObject(expression));
    }

    private boolean isInvalidTransferIn(BLangExpression expression, boolean invokedOnSelf) {
        BLangNode parent = expression.parent;

        NodeKind parentExprKind = parent.getKind();
        if (!(parent instanceof BLangExpression)) {
            return !isIsolatedExpression(expression);
        }

        BLangExpression parentExpression = (BLangExpression) parent;

        if (parentExprKind != NodeKind.INVOCATION) {
            if (!isSelfReference(expression) && isIsolatedExpression(expression)) {
                return false;
            }

            return isInvalidTransferIn(parentExpression, invokedOnSelf);
        }

        BLangInvocation invocation = (BLangInvocation) parentExpression;
        BLangExpression calledOnExpr = invocation.expr;

        if (calledOnExpr == expression) {
            if (isIsolatedExpression(expression)) {
                return false;
            }

            if (isCloneOrCloneReadOnlyInvocation(invocation)) {
                return false;
            }

            if (!invokedOnSelf && Types.getReferredType(invocation.getBType()).tag == TypeTags.NIL) {
                return true;
            }

            return isInvalidTransferIn(parentExpression, invokedOnSelf);
        }

        return !isIsolatedExpression(expression);
    }

    private void validateTransferOut(BLangExpression expression, List<BLangExpression> nonIsolatedCopyOutExpressions) {
        if (!isDependentlyIsolatedExpressionKind(expression)) {
            if (!isIsolatedExpression(expression)) {
                nonIsolatedCopyOutExpressions.add(expression);
            }
            return;
        }

        isIsolatedExpression(expression, false, true, nonIsolatedCopyOutExpressions);
    }

    private void validateTransferOutViaAssignment(BLangExpression expression, BLangExpression varRef,
                                                  List<BLangExpression> nonIsolatedCopyOutLocations) {
        if (!hasRefDefinedOutsideLock(varRef)) {
            return;
        }

        validateTransferOut(expression, nonIsolatedCopyOutLocations);
    }

    private void validateTransferOut(BLangExpression varRef, BLangExpression expr) {
        if (!this.inLockStatement) {
            return;
        }

        validateTransferOutViaAssignment(expr, varRef,
                                         this.copyInLockInfoStack.peek().nonIsolatedTransferOutExpressions);
    }

    private boolean isSelfReference(BLangExpression expression) {
        return expression.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                isSelfOfObject((BLangSimpleVarRef) expression);
    }

    private boolean isSelfOfObject(BLangSimpleVarRef varRefExpr) {
        if (!Names.SELF.value.equals(varRefExpr.variableName.value)) {
            return false;
        }

        BSymbol symbol = varRefExpr.symbol;
        if (symbol == null) {
            return false;
        }

        BSymbol owner = symbol.owner;
        if (owner == null || ((owner.tag & SymTag.INVOKABLE) != SymTag.INVOKABLE)) {
            return false;
        }

        return symbol == ((BInvokableSymbol) owner).receiverSymbol;
    }

    private boolean isSelfOfIsolatedObject(BLangSimpleVarRef varRefExpr) {
        return isSelfOfObject(varRefExpr) && isIsolated(varRefExpr.symbol.type.flags);
    }

    private boolean hasRefDefinedOutsideLock(BLangExpression variableReference) {
        switch (variableReference.getKind()) {
            case SIMPLE_VARIABLE_REF:
                BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) variableReference;
                return isDefinedOutsideLock(names.fromIdNode(simpleVarRef.variableName), simpleVarRef.symbol.tag,
                                            env);
            case RECORD_VARIABLE_REF:
                BLangRecordVarRef recordVarRef = (BLangRecordVarRef) variableReference;
                for (BLangRecordVarRef.BLangRecordVarRefKeyValue recordRefField : recordVarRef.recordRefFields) {
                    if (hasRefDefinedOutsideLock(recordRefField.variableReference)) {
                        return true;
                    }
                }
                ExpressionNode recordRestParam = recordVarRef.restParam;
                return recordRestParam != null && hasRefDefinedOutsideLock((BLangExpression) recordRestParam);
            case TUPLE_VARIABLE_REF:
                BLangTupleVarRef tupleVarRef = (BLangTupleVarRef) variableReference;
                for (BLangExpression expression : tupleVarRef.expressions) {
                    if (hasRefDefinedOutsideLock(expression)) {
                        return true;
                    }
                }
                ExpressionNode tupleRestParam = tupleVarRef.restParam;
                return tupleRestParam != null && hasRefDefinedOutsideLock((BLangExpression) tupleRestParam);
            case ERROR_VARIABLE_REF:
                BLangErrorVarRef errorVarRef = (BLangErrorVarRef) variableReference;

                BLangVariableReference message = errorVarRef.message;
                if (message != null && hasRefDefinedOutsideLock(message)) {
                    return true;
                }

                BLangVariableReference cause = errorVarRef.cause;
                if (cause != null && hasRefDefinedOutsideLock(cause)) {
                    return true;
                }

                for (BLangNamedArgsExpression namedArgsExpression : errorVarRef.detail) {
                    if (hasRefDefinedOutsideLock(namedArgsExpression.expr)) {
                        return true;
                    }
                }

                BLangVariableReference errorRestVar = errorVarRef.restVar;
                return errorRestVar != null && hasRefDefinedOutsideLock(errorRestVar);
        }
        return false;
    }

    private boolean isDefinedOutsideLock(Name name, int symTag, SymbolEnv currentEnv) {
        if (Names.IGNORE == name ||
                symResolver.lookupSymbolInGivenScope(currentEnv, name, symTag) != symTable.notFoundSymbol) {
            return false;
        }

        if (currentEnv.node.getKind() == NodeKind.LOCK) {
            return true;
        }

        return isDefinedOutsideLock(name, symTag, currentEnv.enclEnv);
    }

    private boolean isInIsolatedObjectMethod(SymbolEnv env, boolean ignoreInit) {
        BLangInvokableNode enclInvokable = env.enclInvokable;

        if (enclInvokable == null ||
                (enclInvokable.getKind() != NodeKind.FUNCTION && enclInvokable.getKind() != NodeKind.RESOURCE_FUNC)) {
            return false;
        }

        BLangFunction enclFunction = (BLangFunction) enclInvokable;

        if (!enclFunction.attachedFunction) {
            return false;
        }

        if (enclFunction.objInitFunction && ignoreInit) {
            return false;
        }

        BType ownerType = Types.getReferredType(enclInvokable.symbol.owner.type);

        return ownerType.tag == TypeTags.OBJECT && isIsolated(ownerType.flags);
    }

    private boolean isInvalidCopyIn(BLangSimpleVarRef varRefExpr, SymbolEnv currentEnv) {
        return isInvalidCopyIn(varRefExpr, names.fromIdNode(varRefExpr.variableName), varRefExpr.symbol.tag,
                               currentEnv);
    }

    private boolean isInvalidCopyIn(BLangSimpleVarRef varRefExpr, Name name, int symTag, SymbolEnv currentEnv) {
        BSymbol symbol = symResolver.lookupSymbolInGivenScope(currentEnv, name, symTag);
        if (symbol != symTable.notFoundSymbol &&
                (!(symbol instanceof BVarSymbol) || ((BVarSymbol) symbol).originalSymbol == null)) {
            return false;
        }

        if (currentEnv.node.getKind() == NodeKind.LOCK) {
            if (varRefExpr.parent == null) {
                return true;
            }

            return isInvalidTransferIn(varRefExpr);
        }

        return isInvalidCopyIn(varRefExpr, name, symTag, currentEnv.enclEnv);
    }

    private boolean isMethodCallOnSelfInIsolatedObject(BLangSimpleVarRef varRefExpr, BLangNode parent) {
        return isSelfVarInIsolatedObject(varRefExpr) &&
                parent != null && parent.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR;
    }

    private boolean isSelfVarInIsolatedObject(BLangSimpleVarRef varRefExpr) {
        return isInIsolatedObjectMethod(env, true) && isSelfOfObject(varRefExpr);
    }

    private boolean isIsolatedModuleVariableSymbol(BSymbol symbol) {
        return symbol.owner.getKind() == SymbolKind.PACKAGE && isIsolated(symbol.flags);
    }

    private BSymbol getOriginalSymbol(BSymbol symbol) {
        if (!(symbol instanceof  BVarSymbol)) {
            return symbol;
        }

        BVarSymbol varSymbol = (BVarSymbol) symbol;

        BVarSymbol originalSymbol = varSymbol.originalSymbol;
        return originalSymbol == null ? varSymbol : getOriginalSymbol(originalSymbol);
    }

    private void addToAccessedRestrictedVars(Map<BSymbol, List<BLangSimpleVarRef>> accessedRestrictedVars,
                                             BLangSimpleVarRef varRef) {
        BSymbol originalSymbol = getOriginalSymbol(varRef.symbol);

        if (accessedRestrictedVars.containsKey(originalSymbol)) {
            accessedRestrictedVars.get(originalSymbol).add(varRef);
            return;
        }

        accessedRestrictedVars.put(originalSymbol, new ArrayList<>() {{ add(varRef); }});
    }

    private boolean isEnclosedLockWithinSameFunction(BLangLock currentLock, BLangLock potentialOuterLock) {
        return isEnclosedLockWithinSameFunction(currentLock.parent, potentialOuterLock);
    }

    private boolean isEnclosedLockWithinSameFunction(BLangNode parent, BLangLock potentialOuterLock) {
        if (parent == potentialOuterLock) {
            return true;
        }

        if (parent == null || parent.getKind() == NodeKind.FUNCTION || parent.getKind() == NodeKind.RESOURCE_FUNC) {
            return false;
        }

        return isEnclosedLockWithinSameFunction(parent.parent, potentialOuterLock);
    }

    private boolean isReferenceOfLetVarInitializedWithAnIsolatedExpression(BLangSimpleVarRef varRef) {
        BSymbol symbol = varRef.symbol;

        if ((symbol.owner.tag & SymTag.LET) != SymTag.LET) {
            return false;
        }

        BSymbol originalSymbol = getOriginalSymbol(symbol);

        for (int i = isolatedLetVarStack.size() - 1; i >= 0; i--) {
            if (isolatedLetVarStack.get(i).contains(originalSymbol)) {
                return true;
            }
        }
        return false;
    }

    private boolean isReferenceToVarDefinedInSameInvokable(BSymbol currentOwner, BInvokableSymbol enclInvokableSymbol) {
        if (currentOwner == enclInvokableSymbol) {
            return true;
        }

        if ((currentOwner.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            return false;
        }

        BSymbol nextOwner = currentOwner.owner;

        if (nextOwner == null) {
            return false;
        }

        return isReferenceToVarDefinedInSameInvokable(nextOwner, enclInvokableSymbol);
    }

    private boolean isIsolationInferableFunction(BLangFunction funcNode) {
        Set<Flag> flagSet = funcNode.flagSet;

        if (flagSet.contains(Flag.INTERFACE)) {
            return false;
        }

        if (!flagSet.contains(Flag.ATTACHED)) {
            return !flagSet.contains(Flag.PUBLIC);
        }

        BSymbol owner = funcNode.symbol.owner;

        if (!Symbols.isFlagOn(owner.flags, Flags.PUBLIC)) {
            return true;
        }

        if (!(owner instanceof BClassSymbol)) {
            return false;
        }

        BClassSymbol ownerClassSymbol = (BClassSymbol) owner;
        return ownerClassSymbol.isServiceDecl || Symbols.isFlagOn(ownerClassSymbol.flags, Flags.OBJECT_CTOR);
    }

    private void markDependsOnIsolationNonInferableConstructs() {
        BLangInvokableNode enclInvokable = env.enclInvokable;
        BInvokableSymbol enclInvokableSymbol;

        if (enclInvokable == null) {
            if (isNotInArrowFunctionBody(env)) {
                return;
            }

            enclInvokableSymbol = this.arrowFunctionTempSymbolMap.get((BLangArrowFunction) env.enclEnv.node);
        } else {
            enclInvokableSymbol = enclInvokable.symbol;
            NodeKind enclInvokableKind = enclInvokable.getKind();

            if (enclInvokableKind == NodeKind.RESOURCE_FUNC || (enclInvokableKind == NodeKind.FUNCTION &&
                    ((BLangFunction) enclInvokable).attachedFunction)) {
                BSymbol owner = enclInvokableSymbol.owner;

                if (this.isolationInferenceInfoMap.containsKey(owner)) {
                    this.isolationInferenceInfoMap.get(owner).dependsOnlyOnInferableConstructs = false;
                }
            }

            if (!this.isolationInferenceInfoMap.containsKey(enclInvokableSymbol)) {
                return;
            }
        }

        this.isolationInferenceInfoMap.get(enclInvokableSymbol).dependsOnlyOnInferableConstructs = false;
    }

    private void analyzeFunctionInStartActionForInference(SymbolEnv env, List<BLangExpression> requiredArgs,
                                                          List<BLangExpression> restArgs, BLangExpression expr,
                                                          BInvokableSymbol symbol) {
        Set<BLangExpression> argsList = new HashSet<>(requiredArgs);
        argsList.addAll(restArgs);
        if (expr != null) {
            argsList.add(expr);
        }

        markFunctionDependentlyIsolatedOnStartAction(env.enclInvokable.symbol, argsList, symbol);
    }

    private void analyzeFunctionForInference(BInvokableSymbol symbol) {
        if (Symbols.isFlagOn(symbol.flags, Flags.PUBLIC)) {
            markDependsOnIsolationNonInferableConstructs();
            return;
        }
        markDependentlyIsolatedOnFunction(symbol);
    }

    private void markInitMethodDependentlyIsolatedOnFunction(BLangInvokableNode initMethod, BInvokableSymbol symbol) {
        BInvokableSymbol initMethodSymbol = initMethod.symbol;
        if (!isolationInferenceInfoMap.containsKey(initMethodSymbol)) {
            isolationInferenceInfoMap.put(initMethodSymbol, new IsolationInferenceInfo());

        }
        markFunctionDependentlyIsolatedOnFunction(initMethod, symbol);
    }

    private void markDependentlyIsolatedOnFunction(BInvokableSymbol symbol) {
        BLangInvokableNode enclInvokable = env.enclInvokable;
        markFunctionDependentlyIsolatedOnFunction(enclInvokable, symbol);
    }

    private void markFunctionDependentlyIsolatedOnFunction(BLangInvokableNode enclInvokable, BInvokableSymbol symbol) {
        BInvokableSymbol enclInvokableSymbol;

        if (enclInvokable == null) {
            if (isNotInArrowFunctionBody(env)) {
                return;
            }

            enclInvokableSymbol = this.arrowFunctionTempSymbolMap.get((BLangArrowFunction) env.enclEnv.node);
        } else {
            enclInvokableSymbol = enclInvokable.symbol;

            if (!isolationInferenceInfoMap.containsKey(enclInvokableSymbol)) {
                return;
            }
        }

        isolationInferenceInfoMap.get(enclInvokableSymbol).dependsOnFunctions.add(symbol);
    }

    private boolean isNotInArrowFunctionBody(SymbolEnv env) {
        return env.node.getKind() != NodeKind.EXPR_FUNCTION_BODY || env.enclEnv.node.getKind() != NodeKind.ARROW_EXPR;
    }

    private void markInitMethodDependentlyIsolatedOnVar(BLangInvokableNode initMethod, BSymbol symbol) {
        BInvokableSymbol initMethodSymbol = initMethod.symbol;
        if (!isolationInferenceInfoMap.containsKey(initMethodSymbol)) {
            isolationInferenceInfoMap.put(initMethodSymbol, new IsolationInferenceInfo());

        }
        markFunctionDependentlyIsolatedOnVar(initMethod, symbol);
    }

    private void markDependentlyIsolatedOnVar(BSymbol symbol) {
        BLangInvokableNode enclInvokable = env.enclInvokable;
        markFunctionDependentlyIsolatedOnVar(enclInvokable, symbol);
    }

    private void markFunctionDependentlyIsolatedOnVar(BLangInvokableNode enclInvokable, BSymbol symbol) {
        BInvokableSymbol enclInvokableSymbol;
        if (enclInvokable == null) {
            if (isNotInArrowFunctionBody(env)) {
                return;
            }

            enclInvokableSymbol = this.arrowFunctionTempSymbolMap.get((BLangArrowFunction) env.enclEnv.node);
        } else {
            enclInvokableSymbol = enclInvokable.symbol;

            if (!isolationInferenceInfoMap.containsKey(enclInvokableSymbol)) {
                return;
            }
        }

        isolationInferenceInfoMap.get(enclInvokableSymbol).dependsOnVariablesAndClasses.add(symbol);
    }

    private Set<BSymbol> getModuleLevelVarSymbols(List<BLangVariable> moduleLevelVars) {
        Set<BSymbol> symbols = new HashSet<>(moduleLevelVars.size());
        for (BLangVariable globalVar : moduleLevelVars) {
            symbols.add(globalVar.symbol);
        }
        return symbols;
    }

    private void populateNonPublicMutableOrNonIsolatedVars(Set<BSymbol> moduleLevelVarSymbols) {
        for (BSymbol moduleLevelVarSymbol : moduleLevelVarSymbols) {
            if (!isVarRequiringInference(moduleLevelVarSymbol)) {
                continue;
            }

            this.isolationInferenceInfoMap.put(moduleLevelVarSymbol, new VariableIsolationInferenceInfo());
        }
    }

    private void populateNonPublicIsolatedInferableClasses(List<BLangClassDefinition> classDefinitions) {
        for (BLangClassDefinition classDefinition : classDefinitions) {
            populateInferableClass(classDefinition);
        }
    }

    private boolean isWorkerLambda(BLangFunction function) {
        return function.flagSet.contains(Flag.WORKER) && function.flagSet.contains(Flag.LAMBDA);
    }

    private boolean inObjectInitMethod() {
        BLangInvokableNode enclInvokable = env.enclInvokable;

        if (enclInvokable == null || enclInvokable.getKind() != NodeKind.FUNCTION) {
            return false;
        }

        return ((BLangFunction) enclInvokable).objInitFunction;
    }

    private boolean isVarRequiringInference(BSymbol moduleLevelVarSymbol) {
        long symbolFlags = moduleLevelVarSymbol.flags;
        if (Symbols.isFlagOn(symbolFlags, Flags.PUBLIC) || Symbols.isFlagOn(symbolFlags, Flags.ISOLATED)) {
            return false;
        }

        if (!Symbols.isFlagOn(symbolFlags, Flags.FINAL)) {
            return true;
        }

        BType type = moduleLevelVarSymbol.type;
        return !types.isInherentlyImmutableType(type) && !Symbols.isFlagOn(type.flags, Flags.READONLY);
    }

    private void populateInferableClass(BLangClassDefinition classDefinition) {
        if (Symbols.isFlagOn(classDefinition.symbol.flags, Flags.PUBLIC) && !classDefinition.isServiceDecl &&
                !classDefinition.flagSet.contains(Flag.OBJECT_CTOR)) {
            return;
        }

        BType type = classDefinition.getBType();
        if (Symbols.isFlagOn(type.flags, Flags.ISOLATED)) {
            return;
        }

        Set<BLangIdentifier> protectedFields = new HashSet<>();
        Set<BSymbol> dependentObjectTypes = new HashSet<>();

        Map<String, BLangSimpleVariable> fields = new HashMap<>();

        for (BLangSimpleVariable field : classDefinition.fields) {
            fields.put(field.name.value, field);
        }

        for (BLangSimpleVariable referencedField : classDefinition.referencedFields) {
            String name = referencedField.name.value;
            if (fields.containsKey(name)) {
                // Shouldn't get here based on the current implementation, since the field will not be available as a
                // referenced field if explicitly specified in the including type.
                continue;
            }

            fields.put(name, referencedField);
        }

        for (BLangSimpleVariable field : fields.values()) {
            boolean isFinal = field.flagSet.contains(Flag.FINAL);
            boolean isPrivate = field.flagSet.contains(Flag.PRIVATE);

            if (!isFinal && !isPrivate) {
                return;
            }

            BType fieldType = field.getBType();
            if (isFinal && types.isSubTypeOfReadOnlyOrIsolatedObjectUnion(fieldType)) {
                continue;
            }

            boolean subtypeOfReadOnlyOrIsolatedObjectOrInferableObject =
                    isSubtypeOfReadOnlyOrIsolatedObjectOrInferableObject(classDefinition.symbol.owner, fieldType,
                                                                         dependentObjectTypes);
            if (!isPrivate && !subtypeOfReadOnlyOrIsolatedObjectOrInferableObject) {
                return;
            }

            protectedFields.add(field.name);
        }

        ClassIsolationInferenceInfo inferenceInfo = new ClassIsolationInferenceInfo(protectedFields);
        this.isolationInferenceInfoMap.put(classDefinition.symbol, inferenceInfo);
        inferenceInfo.dependsOnVariablesAndClasses.addAll(dependentObjectTypes);
    }

    private boolean isSubtypeOfReadOnlyOrIsolatedObjectOrInferableObject(BSymbol owner, BType type) {
        return isSubtypeOfReadOnlyOrIsolatedObjectOrInferableObject(owner, type, new HashSet<>());
    }

    private boolean isSubtypeOfReadOnlyOrIsolatedObjectOrInferableObject(BSymbol owner, BType type,
                                                                         Set<BSymbol> inferableClasses) {
        type = Types.getReferredType(type);
        if (types.isSubTypeOfReadOnlyOrIsolatedObjectUnion(type)) {
            return true;
        }

        int tag = type.tag;

        if (tag == TypeTags.OBJECT) {
            BTypeSymbol tsymbol = type.tsymbol;
            boolean inferable =
                    tsymbol.owner == owner && !Symbols.isFlagOn(tsymbol.flags, Flags.PUBLIC);

            if (inferable) {
                inferableClasses.add(tsymbol);
            }

            return inferable;
        }

        if (tag != TypeTags.UNION) {
            return false;
        }

        for (BType memberType : ((BUnionType) type).getMemberTypes()) {
            if (!isSubtypeOfReadOnlyOrIsolatedObjectOrInferableObject(owner, memberType, inferableClasses)) {
                return false;
            }
        }
        return true;
    }

    private Set<BType> getPubliclyExposedObjectTypes(BLangPackage bLangPackage) {
        Set<BType> publiclyExposedTypes = new HashSet<>();
        BPubliclyExposedInferableTypeCollector collector =
                new BPubliclyExposedInferableTypeCollector(publiclyExposedTypes);

        List<BLangVariable> moduleVarsAndConstants = new ArrayList<>() {{
            addAll(bLangPackage.globalVars);
            addAll(bLangPackage.constants);
        }};

        for (BLangVariable construct : moduleVarsAndConstants) {
            if (!construct.flagSet.contains(Flag.PUBLIC)) {
                continue;
            }

            BLangType typeNode = construct.typeNode;

            if (typeNode == null) {
                continue;
            }

            collector.visitType(typeNode.getBType());
        }

        for (BLangTypeDefinition typeDefinition : bLangPackage.typeDefinitions) {
            Set<Flag> flagSet = typeDefinition.flagSet;
            if (!flagSet.contains(Flag.PUBLIC) || flagSet.contains(Flag.ANONYMOUS)) {
                continue;
            }

            collector.visitType(typeDefinition.typeNode.getBType());
        }

        for (BLangClassDefinition classDefinition : bLangPackage.classDefinitions) {
            Set<Flag> flagSet = classDefinition.flagSet;
            if (!flagSet.contains(Flag.PUBLIC) || classDefinition.isServiceDecl || flagSet.contains(Flag.OBJECT_CTOR)) {
                continue;
            }

            collector.visitType(classDefinition.getBType());
        }

        for (BLangFunction function : bLangPackage.functions) {
            if (!function.flagSet.contains(Flag.PUBLIC) &&
                    (!function.attachedFunction || !function.receiver.flagSet.contains(Flag.PUBLIC))) {
                continue;
            }

            collector.visitType(function.getBType());
        }

        return publiclyExposedTypes;
    }

    private void inferIsolation(Set<BSymbol> moduleLevelVarSymbols, Set<BType> publiclyExposedObjectTypes,
                                List<BLangClassDefinition> classDefinitions) {
        for (Map.Entry<BSymbol, IsolationInferenceInfo> entry : this.isolationInferenceInfoMap.entrySet()) {
            IsolationInferenceInfo value = entry.getValue();

            BSymbol symbol = entry.getKey();

            if (value.getKind() == IsolationInferenceKind.FUNCTION) {
                if (inferFunctionIsolation(symbol, value, publiclyExposedObjectTypes, classDefinitions,
                                           new HashSet<>())) {
                    symbol.flags |= Flags.ISOLATED;

                    if (!moduleLevelVarSymbols.contains(symbol)) {
                        symbol.type.flags |= Flags.ISOLATED;
                    }
                }
                continue;
            }

            boolean isObjectType = symbol.kind == SymbolKind.OBJECT;

            if (!isObjectType &&
                    // If it is a final var that is of a type that is a subtype of `readonly|isolated object {}`
                    // don't infer isolated for it, since it can directly be accessed without a lock statement.
                    isFinalVarOfReadOnlyOrIsolatedObjectTypeWithInference(publiclyExposedObjectTypes, classDefinitions,
                                                                          symbol, new HashSet<>())) {
                continue;
            }

            if (inferVariableOrClassIsolation(publiclyExposedObjectTypes, classDefinitions, symbol,
                                              (VariableIsolationInferenceInfo) value, isObjectType, new HashSet<>())) {
                symbol.flags |= Flags.ISOLATED;

                if (isObjectType) {
                    symbol.type.flags |= Flags.ISOLATED;
                }
            }
        }

        this.isolationInferenceInfoMap.clear();
        this.arrowFunctionTempSymbolMap.clear();
    }

    private boolean inferVariableOrClassIsolation(Set<BType> publiclyExposedObjectTypes,
                                                  List<BLangClassDefinition> classDefinitions,
                                                  BSymbol symbol, VariableIsolationInferenceInfo inferenceInfo,
                                                  boolean isObjectType, Set<BSymbol> unresolvedSymbols) {
        if (!unresolvedSymbols.add(symbol)) {
            return true;
        }

        if (!inferenceInfo.dependsOnlyOnInferableConstructs) {
            return false;
        }

        if (inferenceInfo.accessedOutsideLockStatement) {
            if (!inferenceInfo.accessOutsideLockStatementValidIfInferredIsolated) {
                if (inferenceInfo.getKind() == IsolationInferenceKind.CLASS) {
                    return false;
                }

                if (Symbols.isFlagOn(symbol.flags, Flags.FINAL)) {
                    return isSubTypeOfReadOnlyOrIsolatedObjectUnionWithInference(publiclyExposedObjectTypes,
                            classDefinitions, true, symbol.type, unresolvedSymbols);
                }

                return false;
            }

            for (BType bType : inferenceInfo.typesOfFinalFieldsAccessedOutsideLock) {
                if (!isSubTypeOfReadOnlyOrIsolatedObjectUnionWithInference(publiclyExposedObjectTypes, classDefinitions,
                        true, bType, unresolvedSymbols)) {
                    return false;
                }
            }
        }

        if (isObjectType) {
            if (publiclyExposedObjectTypes.contains(symbol.type)) {
                return false;
            }

            BLangClassDefinition classDefinition = null;
            for (BLangClassDefinition classDef : classDefinitions) {
                if (classDef.symbol == symbol) {
                    classDefinition = classDef;
                    break;
                }
            }

            if (classDefinition != null) {
                List<BLangSimpleVariable> classFields = classDefinition.fields;
                Map<BLangIdentifier, BLangSimpleVariable> fields = new HashMap<>(classFields.size());

                for (BLangSimpleVariable classField : classFields) {
                    fields.put(classField.name, classField);
                }

                for (BLangIdentifier protectedField : ((ClassIsolationInferenceInfo) inferenceInfo).protectedFields) {
                    BLangSimpleVariable field = fields.get(protectedField);

                    if (field.flagSet.contains(Flag.PRIVATE)) {
                        continue;
                    }

                    if (!field.flagSet.contains(Flag.FINAL)) {
                        // Shouldn't get here, since we don't add classes with fields that are neither private nor
                        // final to the inferable construct map.
                        return false;
                    }

                    BType fieldType = field.typeNode.getBType();
                    if (!isSubTypeOfReadOnlyOrIsolatedObjectUnionWithInference(publiclyExposedObjectTypes,
                                    classDefinitions, true, fieldType, unresolvedSymbols)) {
                        return false;
                    }
                }

                for (BLangSimpleVariable field : classDefinition.fields) {
                    BLangExpression expr = field.expr;
                    if (expr != null && !isIsolatedExpression(expr, false, false, new ArrayList<>(),
                            true, publiclyExposedObjectTypes, classDefinitions, unresolvedSymbols)) {
                        return false;
                    }
                }

                BLangFunction initFunction = classDefinition.initFunction;
                if (initFunction != null) {
                    BLangFunctionBody body = initFunction.body;

                    for (BLangStatement stmt : ((BLangBlockFunctionBody) body).stmts) {
                        if (stmt.getKind() != NodeKind.ASSIGNMENT) {
                            continue;
                        }

                        BLangAssignment assignmentStmt = (BLangAssignment) stmt;
                        BLangExpression lhs = assignmentStmt.varRef;

                        if (lhs.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR) {
                            continue;
                        }

                        BLangFieldBasedAccess fieldAccessExpr = (BLangFieldBasedAccess) lhs;

                        BLangExpression calledOnExpr = fieldAccessExpr.expr;
                        if (calledOnExpr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                            continue;
                        }

                        if (!isSelfOfObject((BLangSimpleVarRef) calledOnExpr)) {
                            continue;
                        }

                        if (!isIsolatedExpression(assignmentStmt.expr, false, false, new ArrayList<>(), true,
                                publiclyExposedObjectTypes, classDefinitions, unresolvedSymbols)) {
                            return false;
                        }
                    }
                }
            }
        } else if (isFinalVarOfReadOnlyOrIsolatedObjectTypeWithInference(publiclyExposedObjectTypes,
                                                                         classDefinitions, symbol, unresolvedSymbols)) {
            return true;
        } else if (Symbols.isFlagOn(symbol.flags, Flags.LISTENER)) {
            // Listeners aren't allowed as isolated variables.
            return false;
        }

        for (LockInfo lockInfo : inferenceInfo.accessedLockInfo) {
            if (!lockInfo.accessedRestrictedVars.isEmpty()) {
                return false;
            }

            for (BSymbol accessedPotentiallyIsolatedVar : lockInfo.accessedPotentiallyIsolatedVars) {
                if (accessedPotentiallyIsolatedVar == symbol) {
                    continue;
                }

                if (!isFinalVarOfReadOnlyOrIsolatedObjectTypeWithInference(publiclyExposedObjectTypes, classDefinitions,
                                                                           accessedPotentiallyIsolatedVar,
                                                                           unresolvedSymbols)) {
                    return false;
                }
            }

            for (BLangExpression expr : lockInfo.nonIsolatedTransferInExpressions) {
                if (expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF && ((BLangSimpleVarRef) expr).symbol == symbol) {
                    continue;
                }

                if (isIsolatedExpression(expr, false, false, new ArrayList<>(), true, publiclyExposedObjectTypes,
                        classDefinitions, unresolvedSymbols)) {
                    continue;
                }

                return false;
            }

            for (BLangExpression expr : lockInfo.nonIsolatedTransferOutExpressions) {
                if (isIsolatedExpression(expr, false, false, new ArrayList<>(), true, publiclyExposedObjectTypes,
                        classDefinitions, unresolvedSymbols)) {
                    continue;
                }

                return false;
            }

            for (BLangInvocation nonIsolatedInvocation : lockInfo.nonIsolatedInvocations) {
                BSymbol funcSymbol = nonIsolatedInvocation.symbol;

                if (!this.isolationInferenceInfoMap.containsKey(funcSymbol)) {
                    return false;
                }

                if (inferFunctionIsolation(funcSymbol, this.isolationInferenceInfoMap.get(funcSymbol),
                                           publiclyExposedObjectTypes, classDefinitions, unresolvedSymbols)) {
                    continue;
                }

                return false;
            }
        }
        return true;
    }

    private boolean isFinalVarOfReadOnlyOrIsolatedObjectTypeWithInference(Set<BType> publiclyExposedObjectTypes,
                                                                          List<BLangClassDefinition> classDefinitions,
                                                                          BSymbol symbol,
                                                                          Set<BSymbol> unresolvedSymbols) {
        return Symbols.isFlagOn(symbol.flags, Flags.FINAL) &&
                isSubTypeOfReadOnlyOrIsolatedObjectUnionWithInference(publiclyExposedObjectTypes, classDefinitions,
                                                                      true, symbol.type, unresolvedSymbols);
    }

    private boolean inferFunctionIsolation(BSymbol symbol, IsolationInferenceInfo functionIsolationInferenceInfo,
                                           Set<BType> publiclyExposedObjectTypes,
                                           List<BLangClassDefinition> classDefinitions,
                                           Set<BSymbol> unresolvedSymbols) {
        if (!unresolvedSymbols.add(symbol)) {
            return true;
        }

        if (!functionIsolationInferenceInfo.dependsOnlyOnInferableConstructs) {
            return false;
        }

        if (symbol.kind == SymbolKind.FUNCTION) {
            BVarSymbol receiverSymbol = ((BInvokableSymbol) symbol).receiverSymbol;
            if (receiverSymbol != null && Types.getReferredType(receiverSymbol.type).tag == TypeTags.OBJECT &&
                    publiclyExposedObjectTypes.contains(receiverSymbol.type)) {
                return false;
            }
        }

        if (functionIsolationInferenceInfo.inferredIsolated) {
            return true;
        }

        for (BInvokableSymbol bInvokableSymbol : functionIsolationInferenceInfo.dependsOnFunctions) {
            if (!this.isolationInferenceInfoMap.containsKey(bInvokableSymbol)) {
                return false;
            }

            if (!inferFunctionIsolation(bInvokableSymbol, this.isolationInferenceInfoMap.get(bInvokableSymbol),
                                        publiclyExposedObjectTypes, classDefinitions, unresolvedSymbols)) {
                return false;
            }
        }

        for (BSymbol dependsOnVariable : functionIsolationInferenceInfo.dependsOnVariablesAndClasses) {
            if (Symbols.isFlagOn(dependsOnVariable.flags, Flags.ISOLATED)) {
                continue;
            }

            if (!this.isolationInferenceInfoMap.containsKey(dependsOnVariable)) {
                return false;
            }

            if (!inferVariableOrClassIsolation(publiclyExposedObjectTypes, classDefinitions, dependsOnVariable,
                                               (VariableIsolationInferenceInfo) this.isolationInferenceInfoMap.get(
                                                       dependsOnVariable), false, unresolvedSymbols)) {
                return false;
            }
        }

        for (BLangExpression dependsOnArg : functionIsolationInferenceInfo.dependsOnFuncCallArgExprs) {
            if (!isIsolatedExpression(dependsOnArg)) {
                return false;
            }
        }

        if (unresolvedSymbols.size() == 1) {
            // Mark as isolated only when the function is directly being analyzed as opposed to analysis due to calls
            // from other functions for which inference is attempted.
            functionIsolationInferenceInfo.inferredIsolated = true;
        }
        return true;
    }

    private void logServiceIsolationHints(List<BLangClassDefinition> classDefinitions) {
        for (BLangClassDefinition classDefinition : classDefinitions) {
            if (classDefinition.flagSet.contains(Flag.SERVICE)) {
                logServiceIsolationHints(classDefinition);
            }
        }
    }

    private void logServiceIsolationHints(BLangClassDefinition classDefinition) {
        boolean isolatedService = isIsolated(classDefinition.getBType().flags);

        for (BLangFunction function : classDefinition.functions) {
            Set<Flag> flagSet = function.flagSet;

            if (!flagSet.contains(Flag.RESOURCE) && !flagSet.contains(Flag.REMOTE)) {
                continue;
            }

            boolean isolatedMethod = isIsolated(function.getBType().flags);

            if (isolatedService && isolatedMethod) {
                continue;
            }

            dlog.hint(getStartLocation(function.pos), getHintCode(isolatedService, isolatedMethod));
        }
    }

    private Location getStartLocation(Location location) {
        LineRange lineRange = location.lineRange();
        LinePosition linePosition = lineRange.startLine();
        int startLine = linePosition.line();
        int startColumn = linePosition.offset();
        return new BLangDiagnosticLocation(lineRange.filePath(), startLine, startLine, startColumn, startColumn);
    }

    private DiagnosticHintCode getHintCode(boolean isolatedService, boolean isolatedMethod) {
        if (!isolatedService && !isolatedMethod) {
            return DiagnosticHintCode
                    .CONCURRENT_CALLS_WILL_NOT_BE_MADE_TO_NON_ISOLATED_METHOD_IN_NON_ISOLATED_SERVICE;
        }

        if (isolatedService) {
            return DiagnosticHintCode.CONCURRENT_CALLS_WILL_NOT_BE_MADE_TO_NON_ISOLATED_METHOD;
        }

        return DiagnosticHintCode.CONCURRENT_CALLS_WILL_NOT_BE_MADE_TO_NON_ISOLATED_SERVICE;
    }

    private BInvokableSymbol createTempSymbolIfNonExistent(BLangArrowFunction bLangArrowFunction) {
        if (arrowFunctionTempSymbolMap.containsKey(bLangArrowFunction)) {
            return arrowFunctionTempSymbolMap.get(bLangArrowFunction);
        }

        TemporaryArrowFunctionSymbol symbol = new TemporaryArrowFunctionSymbol(bLangArrowFunction);
        this.arrowFunctionTempSymbolMap.put(bLangArrowFunction, symbol);
        this.isolationInferenceInfoMap.put(symbol, new IsolationInferenceInfo());
        return symbol;
    }

    /**
     * For lock statements with restricted var usage, invalid transfers and non-isolated invocations should result in
     * compilation errors. This class holds potentially erroneous expression per lock statement, and the protected
     * variables accessed in the lock statement, and information required for isolated inference.
     */
    private static class LockInfo {
        BLangLock lockNode;

        Map<BSymbol, List<BLangSimpleVarRef>> accessedRestrictedVars = new HashMap<>();
        List<BLangSimpleVarRef> nonCaptureBindingPatternVarRefsOnLhs = new ArrayList<>();
        List<BLangExpression> nonIsolatedTransferInExpressions = new ArrayList<>();
        List<BLangExpression> nonIsolatedTransferOutExpressions = new ArrayList<>();
        List<BLangInvocation> nonIsolatedInvocations = new ArrayList<>();

        Set<BSymbol> accessedPotentiallyIsolatedVars = new HashSet<>();

        private LockInfo(BLangLock lockNode) {
            this.lockNode = lockNode;
        }
    }

    private static class IsolationInferenceInfo {
        boolean dependsOnlyOnInferableConstructs = true;
        Set<BInvokableSymbol> dependsOnFunctions = new HashSet<>();
        Set<BSymbol> dependsOnVariablesAndClasses = new HashSet<>();
        Set<BLangExpression> dependsOnFuncCallArgExprs = new HashSet<>();
        boolean inferredIsolated = false;

        IsolationInferenceKind getKind() {
            return IsolationInferenceKind.FUNCTION;
        }
    }

    private static class VariableIsolationInferenceInfo extends IsolationInferenceInfo {
        Set<LockInfo> accessedLockInfo = new HashSet<>();
        boolean accessedOutsideLockStatement = false;
        boolean accessOutsideLockStatementValidIfInferredIsolated = true;
        Set<BType> typesOfFinalFieldsAccessedOutsideLock = new HashSet<>();

        @Override
        IsolationInferenceKind getKind() {
            return IsolationInferenceKind.VARIABLE;
        }
    }

    private static class ClassIsolationInferenceInfo extends VariableIsolationInferenceInfo {
        Set<BLangIdentifier> protectedFields;

        ClassIsolationInferenceInfo(Set<BLangIdentifier> protectedFields) {
            this.protectedFields = protectedFields;
        }

        @Override
        IsolationInferenceKind getKind() {
            return IsolationInferenceKind.CLASS;
        }
    }

    private enum IsolationInferenceKind {
        CLASS,
        VARIABLE,
        FUNCTION
    }

    private class TemporaryArrowFunctionSymbol extends BInvokableSymbol {
        TemporaryArrowFunctionSymbol(BLangArrowFunction fn) {
            super(SymTag.FUNCTION, 0, Names.EMPTY, env.enclPkg.symbol.pkgID, fn.funcType, env.enclEnv.enclVarSym,
                    null, VIRTUAL);
            this.kind = SymbolKind.FUNCTION;
        }
    }

    private static class BPubliclyExposedInferableTypeCollector implements TypeVisitor {

        Set<BType> unresolvedTypes;
        Set<BType> exposedTypes;

        public BPubliclyExposedInferableTypeCollector(Set<BType> exposedTypes) {
            this.unresolvedTypes = new HashSet<>();
            this.exposedTypes = exposedTypes;
        }

        public void visitType(BType type) {
            if (type == null) {
                return;
            }

            if (!unresolvedTypes.add(type)) {
                return;
            }

            type.accept(this);
        }

        @Override
        public void visit(BAnnotationType bAnnotationType) {
        }

        @Override
        public void visit(BArrayType bArrayType) {
            visitType(bArrayType.eType);
        }

        @Override
        public void visit(BBuiltInRefType bBuiltInRefType) {
        }

        @Override
        public void visit(BAnyType bAnyType) {
        }

        @Override
        public void visit(BAnydataType bAnydataType) {
        }

        @Override
        public void visit(BErrorType bErrorType) {
            visitType(bErrorType.detailType);
        }

        @Override
        public void visit(BFiniteType bFiniteType) {
        }

        @Override
        public void visit(BInvokableType bInvokableType) {
            if (Symbols.isFlagOn(bInvokableType.flags, Flags.ANY_FUNCTION)) {
                return;
            }

            for (BType paramType : bInvokableType.paramTypes) {
                visitType(paramType);
            }
            visitType(bInvokableType.restType);
            visitType(bInvokableType.retType);
        }

        @Override
        public void visit(BJSONType bjsonType) {
        }

        @Override
        public void visit(BMapType bMapType) {
            visitType(bMapType.constraint);
        }

        @Override
        public void visit(BStreamType bStreamType) {
            visitType(bStreamType.constraint);
            visitType(bStreamType.completionType);
        }

        @Override
        public void visit(BTypedescType bTypedescType) {
            visitType(bTypedescType.constraint);
        }

        @Override
        public void visit(BTypeReferenceType bTypeReferenceType) {
            visitType(bTypeReferenceType.referredType);
        }

        @Override
        public void visit(BParameterizedType bTypedescType) {
        }

        @Override
        public void visit(BNeverType bNeverType) {
        }

        @Override
        public void visit(BNilType bNilType) {
        }

        @Override
        public void visit(BNoType bNoType) {
        }

        @Override
        public void visit(BPackageType bPackageType) {
        }

        @Override
        public void visit(BStructureType bStructureType) {
        }

        @Override
        public void visit(BTupleType bTupleType) {
            for (BType memType : bTupleType.tupleTypes) {
                visitType(memType);
            }

            visitType(bTupleType.restType);
        }

        @Override
        public void visit(BUnionType bUnionType) {
            for (BType memType : bUnionType.getMemberTypes()) {
                visitType(memType);
            }
        }

        @Override
        public void visit(BIntersectionType bIntersectionType) {
            for (BType constituentType : bIntersectionType.getConstituentTypes()) {
                visitType(constituentType);
            }
            visitType(bIntersectionType.effectiveType);
        }

        @Override
        public void visit(BXMLType bXmlType) {
            visitType(bXmlType.constraint);
        }

        @Override
        public void visit(BTableType bTableType) {
            visitType(bTableType.constraint);
            visitType(bTableType.keyTypeConstraint);
        }

        @Override
        public void visit(BRecordType bRecordType) {
            for (BField field : bRecordType.fields.values()) {
                visitType(field.type);
            }

            if (!bRecordType.sealed) {
                visitType(bRecordType.restFieldType);
            }
        }

        @Override
        public void visit(BObjectType bObjectType) {
            this.exposedTypes.add(bObjectType);
            for (BField field : bObjectType.fields.values()) {
                visitType(field.type);
            }

            for (BAttachedFunction attachedFunc : ((BObjectTypeSymbol) bObjectType.tsymbol).attachedFuncs) {
                visitType(attachedFunc.type);
            }
        }

        @Override
        public void visit(BType bType) {
        }

        @Override
        public void visit(BFutureType bFutureType) {
            visitType(bFutureType.constraint);
        }

        @Override
        public void visit(BHandleType bHandleType) {
        }
    }
}

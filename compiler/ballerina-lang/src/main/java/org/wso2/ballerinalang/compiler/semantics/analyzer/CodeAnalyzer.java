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

import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.expressions.XMLNavigationAccess;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
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
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable.BLangRecordVariableKeyValue;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangCaptureBindingPattern;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableMultiKeyExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangConstPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangVarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangWildCardMatchPattern;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStaticBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStructuredBindingPatternClause;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
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
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
public class CodeAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<CodeAnalyzer> CODE_ANALYZER_KEY =
            new CompilerContext.Key<>();
    private static final String NULL_LITERAL = "null";

    private final SymbolResolver symResolver;
    private int loopCount;
    private int transactionCount;
    private int retryCount;
    private boolean statementReturns;
    private boolean failureHandled;
    private boolean matchClauseReturns;
    private boolean lastStatement;
    private boolean errorThrown;
    private boolean failVisited;
    private boolean hasLastPatternInClause = false;
    private boolean withinLockBlock;
    private SymbolTable symTable;
    private Types types;
    private BLangDiagnosticLog dlog;
    private TypeChecker typeChecker;
    private Stack<WorkerActionSystem> workerActionSystemStack = new Stack<>();
    private Stack<Boolean> loopWithinTransactionCheckStack = new Stack<>();
    private Stack<Boolean> returnWithinTransactionCheckStack = new Stack<>();
    private Stack<Boolean> doneWithinTransactionCheckStack = new Stack<>();
    private Stack<Boolean> transactionalFuncCheckStack = new Stack<>();
    private Stack<Boolean> returnWithinLambdaWrappingCheckStack = new Stack<>();
    private BLangTransaction innerTransactionBlock = null;
    private BLangRetry innerRetryBlock = null;
    private BLangNode parent;
    private Names names;
    private SymbolEnv env;
    private final Stack<LinkedHashSet<BType>> returnTypes = new Stack<>();
    private final Stack<LinkedHashSet<BType>> errorTypes = new Stack<>();
    private final Stack<BType> onFailErrorType = new Stack<>();
    private boolean isJSONContext;
    private boolean enableExperimentalFeatures;
    private int commitCount;
    private int rollbackCount;
    private boolean withinTransactionScope;
    private boolean withinTransactionBlock;
    private boolean commitRollbackAllowed;
    private int commitCountWithinBlock;
    private int rollbackCountWithinBlock;
    private boolean queryToTableWithKey;
    private List<BType> matchExprTypes;
    private BType matchExprType;

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
        this.enableExperimentalFeatures = Boolean.parseBoolean(
                CompilerOptions.getInstance(context).get(CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED));
    }

    private void resetFunction() {
        this.resetStatementReturns();
        this.resetErrorThrown();
    }

    private void resetStatementReturns() {
        this.statementReturns = false;
    }

    private void resetLastStatement() {
        this.lastStatement = false;
    }

    private void resetErrorThrown() {
        this.errorThrown = false;
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        pkgNode.accept(this);
        return pkgNode;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.CODE_ANALYZE)) {
            return;
        }
        parent = pkgNode;
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        analyzeTopLevelNodes(pkgNode, pkgEnv);
        pkgNode.getTestablePkgs().forEach(testablePackage -> visit((BLangPackage) testablePackage));
    }

    private void analyzeTopLevelNodes(BLangPackage pkgNode, SymbolEnv pkgEnv) {
        pkgNode.topLevelNodes.forEach(topLevelNode -> analyzeNode((BLangNode) topLevelNode, pkgEnv));
        pkgNode.completedPhases.add(CompilerPhase.CODE_ANALYZE);
        parent = null;
    }

    private void analyzeNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        BLangNode myParent = parent;
        node.parent = parent;
        parent = node;
        node.accept(this);
        parent = myParent;
        this.env = prevEnv;
    }

    private void analyzeTypeNode(BLangType node, SymbolEnv env) {

        if (node == null) {
            return;
        }
        analyzeNode(node, env);
    }

    @Override
    public void visit(BLangCompilationUnit compUnitNode) {
        compUnitNode.topLevelNodes.forEach(e -> analyzeNode((BLangNode) e, env));
    }

    public void visit(BLangTypeDefinition typeDefinition) {

        analyzeTypeNode(typeDefinition.typeNode, this.env);
        typeDefinition.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, env));
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        SymbolEnv objectEnv = SymbolEnv.createClassEnv(classDefinition, classDefinition.symbol.scope, env);
        for (BLangSimpleVariable field : classDefinition.fields) {
            analyzeNode(field, objectEnv);
        }

        List<BLangFunction> bLangFunctionList = new ArrayList<>(classDefinition.functions);
        if (classDefinition.initFunction != null) {
            bLangFunctionList.add(classDefinition.initFunction);
        }

        // To ensure the order of the compile errors
        bLangFunctionList.sort(Comparator.comparingInt(function -> function.pos.sLine));
        for (BLangFunction function : bLangFunctionList) {
            this.analyzeNode(function, objectEnv);
        }

        classDefinition.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, env));
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {

        analyzeNode(bLangTupleVariableDef.var, this.env);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {

        analyzeNode(bLangRecordVariableDef.var, this.env);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {

        analyzeNode(bLangErrorVariableDef.errorVariable, this.env);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        boolean isLambda = funcNode.flagSet.contains(Flag.LAMBDA);
        if (isLambda) {
            return;
        }

        validateParams(funcNode);

        if (Symbols.isPublic(funcNode.symbol)) {
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
        this.validateMainFunction(funcNode);
        this.validateModuleInitFunction(funcNode);
        try {

            this.initNewWorkerActionSystem();
            this.workerActionSystemStack.peek().startWorkerActionStateMachine(DEFAULT_WORKER_NAME,
                                                                              funcNode.pos,
                                                                              funcNode);
            this.visitFunction(funcNode);
            this.workerActionSystemStack.peek().endWorkerActionStateMachine();
        } finally {
            this.finalizeCurrentWorkerActionSystem();
        }
        funcNode.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, env));
    }

    private void validateParams(BLangFunction funcNode) {
        for (BLangSimpleVariable parameter : funcNode.requiredParams) {
            analyzeNode(parameter, env);
        }
        if (funcNode.restParam != null) {
            analyzeNode(funcNode.restParam, env);
        }
    }

    private void visitFunction(BLangFunction funcNode) {
        SymbolEnv invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        this.returnWithinTransactionCheckStack.push(true);
        this.doneWithinTransactionCheckStack.push(true);
        this.returnTypes.push(new LinkedHashSet<>());
        this.transactionalFuncCheckStack.push(funcNode.flagSet.contains(Flag.TRANSACTIONAL));
        this.resetFunction();
        if (Symbols.isNative(funcNode.symbol)) {
            return;
        }
        if (isPublicInvokableNode(funcNode)) {
            analyzeNode(funcNode.returnTypeNode, invokableEnv);
        }

        /* the body can be null in the case of Object type function declarations */
        if (funcNode.body != null) {
            analyzeNode(funcNode.body, invokableEnv);

            boolean isNeverOrNilableReturn = funcNode.symbol.type.getReturnType().tag == TypeTags.NEVER ||
                    funcNode.symbol.type.getReturnType().isNullable();
            // If the return signature is nil-able, an implicit return will be added in Desugar.
            // Hence this only checks for non-nil-able return signatures and uncertain return in the body.
            if (!isNeverOrNilableReturn && !this.statementReturns) {
                this.dlog.error(funcNode.pos, DiagnosticCode.INVOKABLE_MUST_RETURN,
                        funcNode.getKind().toString().toLowerCase());
            }
        }
        this.returnTypes.pop();
        this.returnWithinTransactionCheckStack.pop();
        this.doneWithinTransactionCheckStack.pop();
        this.transactionalFuncCheckStack.pop();
    }

    private boolean isPublicInvokableNode(BLangInvokableNode invNode) {
        return Symbols.isPublic(invNode.symbol) && (SymbolKind.PACKAGE.equals(invNode.symbol.owner.getKind()) ||
                Symbols.isPublic(invNode.symbol.owner));
    }

    @Override
    public void visit(BLangBlockFunctionBody body) {
        boolean prevWithinTxScope = withinTransactionScope;
        if (!transactionalFuncCheckStack.empty() && !withinTransactionScope) {
            withinTransactionScope = transactionalFuncCheckStack.peek();
        }
        final SymbolEnv blockEnv = SymbolEnv.createFuncBodyEnv(body, env);
        for (BLangStatement e : body.stmts) {
            analyzeNode(e, blockEnv);
        }
        this.resetLastStatement();
        if (!transactionalFuncCheckStack.empty() && transactionalFuncCheckStack.peek()) {
            withinTransactionScope = prevWithinTxScope;
        }
    }

    @Override
    public void visit(BLangExprFunctionBody body) {
        analyzeExpr(body.expr);
        this.statementReturns = true;
        this.resetLastStatement();
    }

    @Override
    public void visit(BLangExternalFunctionBody body) {
        // do nothing
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        if (forkJoin.workers.isEmpty()) {
            dlog.error(forkJoin.pos, DiagnosticCode.INVALID_FOR_JOIN_SYNTAX_EMPTY_FORK);
        }
    }

    @Override
    public void visit(BLangWorker worker) {
        /* ignore, remove later */
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        this.checkStatementExecutionValidity(transactionNode);
        //Check whether transaction statement occurred in a transactional scope
        if (!transactionalFuncCheckStack.empty() && transactionalFuncCheckStack.peek()) {
            this.dlog.error(transactionNode.pos, DiagnosticCode.TRANSACTION_CANNOT_BE_USED_WITHIN_TRANSACTIONAL_SCOPE);
            return;
        }

        this.errorTypes.push(new LinkedHashSet<>());
        boolean failureHandled = this.failureHandled;

        boolean previousWithinTxScope = this.withinTransactionScope;
        boolean previousWithinTrxBlock = this.withinTransactionBlock;
        int previousCommitCount = this.commitCount;
        int previousRollbackCount = this.rollbackCount;
        boolean prevCommitRollbackAllowed = this.commitRollbackAllowed;
        this.commitRollbackAllowed = true;
        this.commitCount = 0;
        this.rollbackCount = 0;

        if (this.withinTransactionBlock) {
            this.innerTransactionBlock = transactionNode;
        }

        this.withinTransactionScope = true;

        if (!this.withinTransactionBlock) {
            this.withinTransactionBlock = true;
        }

        this.loopWithinTransactionCheckStack.push(false);
        this.returnWithinTransactionCheckStack.push(false);
        this.doneWithinTransactionCheckStack.push(false);
        this.returnWithinLambdaWrappingCheckStack.push(false);
        this.transactionCount++;
        if (!this.failureHandled) {
            this.failureHandled = transactionNode.onFailClause != null;
        }
        analyzeNode(transactionNode.transactionBody, env);
        this.failureHandled = failureHandled;
        if (commitCount < 1) {
            this.dlog.error(transactionNode.pos, DiagnosticCode.INVALID_COMMIT_COUNT);
        }

        transactionNode.statementBlockReturns = this.returnWithinLambdaWrappingCheckStack.peek();
        this.returnWithinLambdaWrappingCheckStack.pop();
        this.transactionCount--;
        this.withinTransactionScope = previousWithinTxScope;
        this.withinTransactionBlock = previousWithinTrxBlock;
        this.commitCount = previousCommitCount;
        this.rollbackCount = previousRollbackCount;
        this.commitRollbackAllowed = prevCommitRollbackAllowed;
        if (this.innerTransactionBlock != null) {
            transactionNode.statementBlockReturns = this.innerTransactionBlock.statementBlockReturns;
        }

        if (!this.withinTransactionBlock) {
            this.innerTransactionBlock = null;
        }
        this.returnWithinTransactionCheckStack.pop();
        this.loopWithinTransactionCheckStack.pop();
        this.doneWithinTransactionCheckStack.pop();
        transactionNode.transactionBody.isBreakable = transactionNode.onFailClause != null;
        analyzeOnFailClause(transactionNode.onFailClause);
        this.errorTypes.pop();
    }

    private void analyzeOnFailClause(BLangOnFailClause onFailClause) {
        if (onFailClause != null) {
            boolean currentStatementReturns = this.statementReturns;
            this.resetStatementReturns();
            this.resetLastStatement();
            analyzeNode(onFailClause, env);
            this.statementReturns = currentStatementReturns;
        }
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr) {

    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
        this.commitCount++;
        this.commitCountWithinBlock++;
        if (this.transactionCount == 0) {
            this.dlog.error(commitExpr.pos, DiagnosticCode.COMMIT_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK);
            return;
        }
        if (!this.transactionalFuncCheckStack.empty() && this.transactionalFuncCheckStack.peek()) {
            this.dlog.error(commitExpr.pos, DiagnosticCode.COMMIT_CANNOT_BE_WITHIN_TRANSACTIONAL_FUNCTION);
            return;
        }
        if (!withinTransactionScope || !commitRollbackAllowed) {
            this.dlog.error(commitExpr.pos, DiagnosticCode.COMMIT_NOT_ALLOWED);
            return;
        }
        withinTransactionScope = false;
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        rollbackCount++;
        this.rollbackCountWithinBlock++;
        if (this.transactionCount == 0 && !withinTransactionScope) {
            this.dlog.error(rollbackNode.pos, DiagnosticCode.ROLLBACK_CANNOT_BE_OUTSIDE_TRANSACTION_BLOCK);
            return;
        }
        if (!this.transactionalFuncCheckStack.empty() && this.transactionalFuncCheckStack.peek()) {
            this.dlog.error(rollbackNode.pos, DiagnosticCode.ROLLBACK_CANNOT_BE_WITHIN_TRANSACTIONAL_FUNCTION);
            return;
        }
        if (!withinTransactionScope || !commitRollbackAllowed) {
            this.dlog.error(rollbackNode.pos, DiagnosticCode.ROLLBACK_NOT_ALLOWED);
            return;
        }
        this.withinTransactionScope = false;
        analyzeExpr(rollbackNode.expr);
    }

    @Override
    public void visit(BLangRetry retryNode) {
        this.returnWithinLambdaWrappingCheckStack.push(false);
        this.errorTypes.push(new LinkedHashSet<>());
        boolean failureHandled = this.failureHandled;
        this.checkStatementExecutionValidity(retryNode);
        this.retryCount++;
        if (!this.failureHandled) {
            this.failureHandled = retryNode.onFailClause != null;
        }
        if (retryCount > 1) {
            this.innerRetryBlock = retryNode;
        }
        retryNode.retrySpec.accept(this);
        retryNode.retryBody.accept(this);
        this.failureHandled = failureHandled;
        if (!this.returnWithinLambdaWrappingCheckStack.peek() && this.innerRetryBlock != null) {
            retryNode.retryBodyReturns = this.innerRetryBlock.retryBodyReturns;
        } else {
            retryNode.retryBodyReturns = this.returnWithinLambdaWrappingCheckStack.peek();
        }
        this.returnWithinLambdaWrappingCheckStack.pop();
        this.resetLastStatement();
        this.resetErrorThrown();
        retryNode.retryBody.isBreakable = retryNode.onFailClause != null;
        analyzeOnFailClause(retryNode.onFailClause);
        this.errorTypes.pop();
        this.retryCount--;
        if (this.retryCount == 0) {
            this.innerRetryBlock = null;
        }
    }

    @Override
    public void visit(BLangRetrySpec retrySpec) {
        if (retrySpec.retryManagerType != null) {
            BTypeSymbol retryManagerTypeSymbol = (BObjectTypeSymbol) symTable.langTransactionModuleSymbol
                    .scope.lookup(names.fromString("RetryManager")).symbol;
            BType abstractRetryManagerType = retryManagerTypeSymbol.type;
            if (!types.isAssignable(retrySpec.retryManagerType.type, abstractRetryManagerType)) {
                dlog.error(retrySpec.pos, DiagnosticCode.INVALID_INTERFACE_ON_NON_ABSTRACT_OBJECT,
                        RETRY_MANAGER_OBJECT_SHOULD_RETRY_FUNC, retrySpec.retryManagerType.type);
            }
        }
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        analyzeNode(retryTransaction.retrySpec, env);
        analyzeNode(retryTransaction.transaction, env);
    }

    private void checkUnreachableCode(BLangStatement stmt) {
        if (this.statementReturns) {
            this.dlog.error(stmt.pos, DiagnosticCode.UNREACHABLE_CODE);
            this.resetStatementReturns();
        } else if (errorThrown) {
            this.dlog.error(stmt.pos, DiagnosticCode.UNREACHABLE_CODE);
            this.resetErrorThrown();
        }
        if (lastStatement) {
            this.dlog.error(stmt.pos, DiagnosticCode.UNREACHABLE_CODE);
            this.resetLastStatement();
        }
    }

    private void checkStatementExecutionValidity(BLangStatement stmt) {
        this.checkUnreachableCode(stmt);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        int prevCommitCount = this.commitCountWithinBlock;
        int prevRollbackCount = this.rollbackCountWithinBlock;
        this.commitCountWithinBlock = 0;
        this.rollbackCountWithinBlock = 0;
        final SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        blockNode.stmts.forEach(e -> {
            analyzeNode(e, blockEnv);
        });
        if (commitCountWithinBlock > 1 || rollbackCountWithinBlock > 1) {
            this.dlog.error(blockNode.pos, DiagnosticCode.MAX_ONE_COMMIT_ROLLBACK_ALLOWED_WITHIN_A_BRANCH);
        }
        this.commitCountWithinBlock = prevCommitCount;
        this.rollbackCountWithinBlock = prevRollbackCount;
        this.resetLastStatement();
    }

    @Override
    public void visit(BLangReturn returnStmt) {
        this.checkStatementExecutionValidity(returnStmt);

        if (checkReturnValidityInTransaction()) {
            this.dlog.error(returnStmt.pos, DiagnosticCode.RETURN_CANNOT_BE_USED_TO_EXIT_TRANSACTION);
            return;
        }
        if (!this.returnWithinLambdaWrappingCheckStack.empty()) {
            this.returnWithinLambdaWrappingCheckStack.pop();
            this.returnWithinLambdaWrappingCheckStack.push(true);
        }

        if (returnStmt.parent != null && returnStmt.parent.parent.getKind() == NodeKind.MATCH_CLAUSE) {
            this.matchClauseReturns = true;
        } else {
            this.statementReturns = true;
        }
        analyzeExpr(returnStmt.expr);
        this.returnTypes.peek().add(returnStmt.expr.type);
    }

    @Override
    public void visit(BLangIf ifStmt) {
        boolean independentBlocks = false;
        int prevCommitCount = commitCount;
        int prevRollbackCount = rollbackCount;
        this.checkStatementExecutionValidity(ifStmt);
        if (withinTransactionScope && ifStmt.elseStmt != null && ifStmt.elseStmt.getKind() != NodeKind.IF) {
                independentBlocks = true;
                commitRollbackAllowed = true;
        }
        boolean prevTxMode = this.withinTransactionScope;
        if (ifStmt.expr.getKind() == NodeKind.TRANSACTIONAL_EXPRESSION) {
            this.withinTransactionScope = true;
        }
        analyzeNode(ifStmt.body, env);
        if (ifStmt.expr.getKind() == NodeKind.TRANSACTIONAL_EXPRESSION) {
            this.withinTransactionScope = prevTxMode;
        }
        boolean ifStmtReturns = this.statementReturns;
        boolean currentErrorThrown = this.errorThrown;
        this.resetStatementReturns();
        this.resetErrorThrown();
        if (ifStmt.elseStmt != null) {
            if (independentBlocks) {
                commitRollbackAllowed = true;
                withinTransactionScope = true;
            }
            analyzeNode(ifStmt.elseStmt, env);
            if ((prevCommitCount != commitCount) || prevRollbackCount != rollbackCount) {
                commitRollbackAllowed = false;
            }
            this.statementReturns = ifStmtReturns && this.statementReturns;
            this.errorThrown = currentErrorThrown && this.errorThrown;
        }
        analyzeExpr(ifStmt.expr);
    }

    @Override
    public void visit(BLangMatchStatement matchStatement) {
        this.errorTypes.push(new LinkedHashSet<>());
        analyzeExpr(matchStatement.expr);
        if (!this.failureHandled) {
            this.failureHandled = matchStatement.onFailClause != null;
        }
        matchExprType = matchStatement.expr.type;

        boolean currentErrorThrown = this.errorThrown;
        this.matchClauseReturns = false;
        this.hasLastPatternInClause = false;
        boolean containsLastPatternInStatement = false;
        boolean allClausesReturns = true;
        for (BLangMatchClause matchClause : matchStatement.matchClauses) {
            this.resetErrorThrown();
            analyzeNode(matchClause, env);
            allClausesReturns = allClausesReturns && this.matchClauseReturns;
            containsLastPatternInStatement =
                    containsLastPatternInStatement || (matchClause.matchGuard == null && this.hasLastPatternInClause);
        }
        this.statementReturns = allClausesReturns && containsLastPatternInStatement;
        this.errorThrown = currentErrorThrown;
        analyzeOnFailClause(matchStatement.onFailClause);
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
        for (BLangMatchPattern matchPattern : matchClause.matchPatterns) {
            if (this.hasLastPatternInClause && matchClause.matchGuard == null) {
                dlog.error(matchPattern.pos, DiagnosticCode.MATCH_STMT_PATTERN_UNREACHABLE);
            }
            if (matchPattern.type == symTable.noType) {
                dlog.error(matchClause.pos, DiagnosticCode.MATCH_STMT_UNMATCHED_PATTERN);
            }

            this.isJSONContext = types.isJSONContext(matchExprType);
            analyzeNode(matchPattern, env);
            matchPattern.isLastPattern = this.hasLastPatternInClause;
        }

        analyzeNode(matchClause.blockStmt, env);
        resetStatementReturns();
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern) {
        analyzeNode(constMatchPattern.expr, env);
    }

    @Override
    public void visit(BLangWildCardMatchPattern wildCardMatchPattern) {
        this.hasLastPatternInClause = wildCardMatchPattern.matchesAll =
                types.isAssignable(wildCardMatchPattern.matchExpr.type, symTable.anyType);
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern) {
        BLangBindingPattern bindingPattern = varBindingPattern.getBindingPattern();
        analyzeNode(bindingPattern, env);
        this.hasLastPatternInClause = this.hasLastPatternInClause && !varBindingPattern.matchGuardIsAvailable;
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern) {
        this.hasLastPatternInClause = true;
    }

    @Override
    public void visit(BLangMatch matchStmt) {
        this.errorTypes.push(new LinkedHashSet<>());
        if (!this.failureHandled) {
            this.failureHandled = matchStmt.onFailClause != null;
        }
        analyzeExpr(matchStmt.expr);

        boolean staticLastPattern = false;
        if (!matchStmt.getStaticPatternClauses().isEmpty()) {
            staticLastPattern = analyzeStaticMatchPatterns(matchStmt);
        }

        boolean structuredLastPattern = false;
        if (!matchStmt.getStructuredPatternClauses().isEmpty()) {
            structuredLastPattern = analyzeStructuredMatchPatterns(matchStmt);
        }

        if (!matchStmt.getPatternClauses().isEmpty()) {
            analyzeEmptyMatchPatterns(matchStmt);
            analyzeMatchedPatterns(matchStmt, staticLastPattern, structuredLastPattern);
        }
        analyzeOnFailClause(matchStmt.onFailClause);
        this.errorTypes.pop();
    }

    @Override
    public void visit(BLangMatchStaticBindingPatternClause patternClause) {
        analyzeNode(patternClause.matchExpr, env);
        analyzeNode(patternClause.body, env);
        resetStatementReturns();
        resetErrorThrown();
    }

    @Override
    public void visit(BLangMatchStructuredBindingPatternClause patternClause) {
        analyzeNode(patternClause.matchExpr, env);
        analyzeNode(patternClause.body, env);
        resetStatementReturns();
        resetErrorThrown();
    }

    private void analyzeMatchedPatterns(BLangMatch matchStmt, boolean staticLastPattern,
                                        boolean structuredLastPattern) {
        if (staticLastPattern && structuredLastPattern) {
            dlog.error(matchStmt.pos, DiagnosticCode.MATCH_STMT_CONTAINS_TWO_DEFAULT_PATTERNS);
        }
        // Execute the following block if there are no unmatched expression types
        if ((staticLastPattern && !hasErrorType(matchStmt.exprTypes)) || structuredLastPattern) {
            if (matchStmt.getPatternClauses().size() == 1) {
                dlog.error(matchStmt.getPatternClauses().get(0).pos, DiagnosticCode.MATCH_STMT_PATTERN_ALWAYS_MATCHES);
            }
            this.checkStatementExecutionValidity(matchStmt);
            boolean matchStmtReturns = true;
            for (BLangMatchBindingPatternClause patternClause : matchStmt.getPatternClauses()) {
                analyzeNode(patternClause.body, env);
                matchStmtReturns = matchStmtReturns && this.statementReturns;
                this.resetStatementReturns();
                this.resetErrorThrown();
            }
            this.statementReturns = matchStmtReturns;
        }
    }

    private boolean hasErrorType(List<BType> typeList) {
        return typeList.stream().anyMatch(t -> types.isAssignable(t, symTable.errorType));
    }

    private boolean analyzeStructuredMatchPatterns(BLangMatch matchStmt) {
        if (matchStmt.exprTypes.isEmpty()) {
            return false;
        }

        for (BLangMatchStructuredBindingPatternClause patternClause : matchStmt.getStructuredPatternClauses()) {
            analyzeNode(patternClause, env);
        }

        return analyseStructuredBindingPatterns(matchStmt.getStructuredPatternClauses(),
                hasErrorType(matchStmt.exprTypes));
    }

    /**
     * This method is used to check structured `var []`, `var {}` & static `[]`, `{}` match pattern.
     *
     * @param matchStmt the match statement containing structured & static match patterns.
     */
    private void analyzeEmptyMatchPatterns(BLangMatch matchStmt) {
        List<BLangMatchBindingPatternClause> emptyLists = new ArrayList<>();
        List<BLangMatchBindingPatternClause> emptyRecords = new ArrayList<>();
        for (BLangMatchBindingPatternClause pattern : matchStmt.patternClauses) {
            if (pattern.getKind() == NodeKind.MATCH_STATIC_PATTERN_CLAUSE) {
                BLangMatchStaticBindingPatternClause staticPattern = (BLangMatchStaticBindingPatternClause) pattern;
                if (staticPattern.literal.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
                    BLangListConstructorExpr listLiteral = (BLangListConstructorExpr) staticPattern.literal;
                    if (listLiteral.exprs.isEmpty()) {
                        emptyLists.add(pattern);
                    }
                } else if (staticPattern.literal.getKind() == NodeKind.RECORD_LITERAL_EXPR) {
                    BLangRecordLiteral recordLiteral = (BLangRecordLiteral) staticPattern.literal;
                    if (recordLiteral.fields.isEmpty()) {
                        emptyRecords.add(pattern);
                    }
                }
            } else if (pattern.getKind() == NodeKind.MATCH_STRUCTURED_PATTERN_CLAUSE) {
                BLangMatchStructuredBindingPatternClause structuredPattern
                        = (BLangMatchStructuredBindingPatternClause) pattern;
                if (structuredPattern.bindingPatternVariable.getKind() == NodeKind.TUPLE_VARIABLE) {
                    BLangTupleVariable tupleVariable = (BLangTupleVariable) structuredPattern.bindingPatternVariable;
                    if (tupleVariable.memberVariables.isEmpty() && tupleVariable.restVariable == null) {
                        emptyLists.add(pattern);
                    }
                } else if (structuredPattern.bindingPatternVariable.getKind() == NodeKind.RECORD_VARIABLE) {
                    BLangRecordVariable recordVariable = (BLangRecordVariable) structuredPattern.bindingPatternVariable;
                    if (recordVariable.variableList.isEmpty() && recordVariable.restParam == null) {
                        emptyRecords.add(pattern);
                    }
                }
            }
        }
        if (emptyLists.size() > 1) {
            for (int i = 1; i < emptyLists.size(); i++) {
                dlog.error(emptyLists.get(i).pos, DiagnosticCode.MATCH_STMT_UNREACHABLE_PATTERN);
            }
        }
        if (emptyRecords.size() > 1) {
            for (int i = 1; i < emptyRecords.size(); i++) {
                dlog.error(emptyRecords.get(i).pos, DiagnosticCode.MATCH_STMT_UNREACHABLE_PATTERN);
            }
        }
    }

    /**
     * This method is used to check the isLike test in a static match pattern.
     * @param matchStmt the match statment containing static match patterns.
     */
    private boolean analyzeStaticMatchPatterns(BLangMatch matchStmt) {
        if (matchStmt.exprTypes.isEmpty()) {
            return false;
        }
        List<BLangMatchStaticBindingPatternClause> matchedPatterns = new ArrayList<>();
        for (BLangMatchStaticBindingPatternClause patternClause : matchStmt.getStaticPatternClauses()) {
            analyzeNode(patternClause, env);

            List<BType> matchedExpTypes = new ArrayList<>();
            for (BType exprType : matchStmt.exprTypes) {
                if (isValidStaticMatchPattern(exprType, patternClause.literal)) {
                    matchedExpTypes.add(exprType);
                }
            }

            if (matchedExpTypes.isEmpty()) {
                // log error if a pattern will not match to any of the expected types
                dlog.error(patternClause.pos, DiagnosticCode.MATCH_STMT_UNMATCHED_PATTERN);
                continue;
            }

            this.isJSONContext = types.isJSONContext(matchStmt.expr.type);
            analyzeNode(patternClause.literal, env);
            matchedPatterns.add(patternClause);
        }

        if (matchedPatterns.isEmpty()) {
            return false;
        }

        return analyzeStaticPatterns(matchedPatterns, hasErrorType(matchStmt.exprTypes));
    }

    private boolean analyzeStaticPatterns(List<BLangMatchStaticBindingPatternClause> matchedPatterns,
                                          boolean errorTypeInMatchExpr) {
        BLangMatchStaticBindingPatternClause finalPattern = matchedPatterns.get(matchedPatterns.size() - 1);
        if (finalPattern.literal.getKind() == NodeKind.SIMPLE_VARIABLE_REF
                && ((BLangSimpleVarRef) finalPattern.literal).variableName.value.equals(Names.IGNORE.value)
                && !errorTypeInMatchExpr) {
            finalPattern.isLastPattern = true;
        }

        for (int i = 0; i < matchedPatterns.size() - 1; i++) {
            BLangExpression precedingPattern = matchedPatterns.get(i).literal;
            for (int j = i + 1; j < matchedPatterns.size(); j++) {
                BLangExpression pattern = matchedPatterns.get(j).literal;
                if (checkLiteralSimilarity(precedingPattern, pattern)) {
                    dlog.error(pattern.pos, DiagnosticCode.MATCH_STMT_UNREACHABLE_PATTERN);
                    matchedPatterns.remove(j--);
                }
            }
        }
        return finalPattern.isLastPattern;
    }

    private boolean analyseStructuredBindingPatterns(List<BLangMatchStructuredBindingPatternClause> clauses,
                                                     boolean errorTypeInMatchExpr) {
        BLangMatchStructuredBindingPatternClause finalPattern = clauses.get(clauses.size() - 1);
        if (finalPattern.bindingPatternVariable.getKind() == NodeKind.VARIABLE
                && finalPattern.typeGuardExpr == null
                && !(errorTypeInMatchExpr && isWildcardMatchPattern(finalPattern))) {
            finalPattern.isLastPattern = true;
        }

        BLangMatchStructuredBindingPatternClause currentPattern;
        BLangMatchStructuredBindingPatternClause precedingPattern;
        for (int i = 0; i < clauses.size(); i++) {
            precedingPattern = clauses.get(i);
            if (precedingPattern.typeGuardExpr != null) {
                analyzeExpr(precedingPattern.typeGuardExpr);
            }

            for (int j = i + 1; j < clauses.size(); j++) {
                currentPattern = clauses.get(j);
                BLangVariable precedingVar = precedingPattern.bindingPatternVariable;
                BLangVariable currentVar = currentPattern.bindingPatternVariable;

                if (checkStructuredPatternSimilarity(precedingVar, currentVar, errorTypeInMatchExpr) &&
                        checkTypeGuardSimilarity(precedingPattern.typeGuardExpr, currentPattern.typeGuardExpr)) {
                    dlog.error(currentVar.pos, DiagnosticCode.MATCH_STMT_UNREACHABLE_PATTERN);
                    clauses.remove(j--);
                }
            }
        }
        return finalPattern.isLastPattern;
    }

    private boolean isWildcardMatchPattern(BLangMatchStructuredBindingPatternClause finalPattern) {
        return ((BLangSimpleVariable) finalPattern.bindingPatternVariable).name.value.equals(Names.IGNORE.value);
    }

    /**
     * This method will check if two patterns are similar to each other.
     * Having similar patterns in the match block will result in unreachable pattern.
     *
     * @param precedingPattern pattern taken to compare similarity.
     * @param pattern          the pattern that the precedingPattern is checked for similarity.
     * @return true if both patterns are similar.
     */
    private boolean checkLiteralSimilarity(BLangExpression precedingPattern, BLangExpression pattern) {
        if (precedingPattern.getKind() == NodeKind.BINARY_EXPR) {
            // If preceding pattern is a binary expression, check both sides of binary expression with current pattern.
            BLangBinaryExpr precedingBinaryExpr = (BLangBinaryExpr) precedingPattern;
            BLangExpression precedingLhsExpr = precedingBinaryExpr.lhsExpr;
            BLangExpression precedingRhsExpr = precedingBinaryExpr.rhsExpr;
            return checkLiteralSimilarity(precedingLhsExpr, pattern) ||
                    checkLiteralSimilarity(precedingRhsExpr, pattern);
        }

        if (pattern.getKind() == NodeKind.BINARY_EXPR) {
            // If current pattern is a binary expression, check both sides of binary expression with preceding pattern.
            BLangBinaryExpr binaryExpr = (BLangBinaryExpr) pattern;
            BLangExpression lhsExpr = binaryExpr.lhsExpr;
            BLangExpression rhsExpr = binaryExpr.rhsExpr;
            return checkLiteralSimilarity(precedingPattern, lhsExpr) ||
                    checkLiteralSimilarity(precedingPattern, rhsExpr);
        }

        switch (precedingPattern.type.tag) {
            case TypeTags.MAP:
                if (pattern.type.tag == TypeTags.MAP) {
                    BLangRecordLiteral precedingRecordLiteral = (BLangRecordLiteral) precedingPattern;
                    Map<String, BLangExpression> recordLiteral = ((BLangRecordLiteral) pattern).fields
                            .stream()
                            // Unchecked cast since BPs are always added as key-value.
                            .map(field -> (BLangRecordKeyValueField) field)
                            .collect(Collectors.toMap(
                                    keyValuePair -> ((BLangSimpleVarRef) keyValuePair.key.expr).variableName.value,
                                    BLangRecordKeyValueField::getValue
                            ));

                    for (int i = 0; i < precedingRecordLiteral.fields.size(); i++) {
                        // Unchecked cast since BPs are always added as key-value.
                        BLangRecordKeyValueField bLangRecordKeyValue =
                                (BLangRecordKeyValueField) precedingRecordLiteral.fields.get(i);
                        String key = ((BLangSimpleVarRef) bLangRecordKeyValue.key.expr).variableName.value;
                        if (!recordLiteral.containsKey(key)) {
                            return false;
                        }
                        if (!checkLiteralSimilarity(bLangRecordKeyValue.valueExpr, recordLiteral.get(key))) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            case TypeTags.TUPLE:
                if (pattern.type.tag == TypeTags.TUPLE) {
                    BLangListConstructorExpr precedingTupleLiteral = (BLangListConstructorExpr) precedingPattern;
                    BLangListConstructorExpr tupleLiteral = (BLangListConstructorExpr) pattern;
                    if (precedingTupleLiteral.exprs.size() != tupleLiteral.exprs.size()) {
                        return false;
                    }
                    return IntStream.range(0, precedingTupleLiteral.exprs.size())
                            .allMatch(i -> checkLiteralSimilarity(precedingTupleLiteral.exprs.get(i),
                                    tupleLiteral.exprs.get(i)));
                }
                return false;
            case TypeTags.INT:
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.STRING:
            case TypeTags.BOOLEAN:
                if (precedingPattern.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    // preceding pattern is a constant.
                    BConstantSymbol precedingPatternSym =
                            (BConstantSymbol) ((BLangSimpleVarRef) precedingPattern).symbol;
                    if (pattern.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                        if (!((BLangSimpleVarRef) pattern).variableName.value.equals(Names.IGNORE.value)) {
                            // pattern is a constant reference.
                            BConstantSymbol patternSym = (BConstantSymbol) ((BLangSimpleVarRef) pattern).symbol;
                            return precedingPatternSym.value.equals(patternSym.value);
                        }
                        // pattern is '_'.
                        return false;
                    }
                    // pattern is a literal.
                    BLangLiteral literal = pattern.getKind() == NodeKind.GROUP_EXPR ?
                            (BLangLiteral) ((BLangGroupExpr) pattern).expression :
                            (BLangLiteral) pattern;
                    return (precedingPatternSym.value.equals(literal.value));
                }

                if (types.isValueType(pattern.type)) {
                    // preceding pattern is a literal.
                    BLangLiteral precedingLiteral = precedingPattern.getKind() == NodeKind.GROUP_EXPR ?
                            (BLangLiteral) ((BLangGroupExpr) precedingPattern).expression :
                            (BLangLiteral) precedingPattern;

                    if (pattern.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                        if (pattern.type.tag != TypeTags.NONE) {
                            // pattern is a constant reference.
                            BConstantSymbol patternSym = (BConstantSymbol) ((BLangSimpleVarRef) pattern).symbol;
                            return patternSym.value.equals(precedingLiteral.value);
                        }
                        // pattern is '_'.
                        return false;
                    }
                    // pattern is a literal.
                    BLangLiteral literal = pattern.getKind() == NodeKind.GROUP_EXPR ?
                            (BLangLiteral) ((BLangGroupExpr) pattern).expression :
                            (BLangLiteral) pattern;
                    return (precedingLiteral.value.equals(literal.value));
                }
                return false;
            case TypeTags.ANY:
                // preceding pattern is '_'. Hence will match all patterns except error that follow.
                if (pattern.type.tag == TypeTags.ERROR) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * This method will determine if the type guard of the preceding pattern will result in the current pattern
     * being unreachable.
     *
     * @param precedingGuard type guard of the preceding structured pattern
     * @param currentGuard   type guard of the cuurent structured pattern
     * @return true if the current pattern is unreachable due to the type guard of the preceding pattern
     */
    private boolean checkTypeGuardSimilarity(BLangExpression precedingGuard, BLangExpression currentGuard) {
        // check if type guard is a type test expr and compare the variable ref and type node
        if (precedingGuard != null && currentGuard != null) {
            if (precedingGuard.getKind() == NodeKind.TYPE_TEST_EXPR &&
                    currentGuard.getKind() == NodeKind.TYPE_TEST_EXPR &&
                    ((BLangTypeTestExpr) precedingGuard).expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                    ((BLangTypeTestExpr) currentGuard).expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangTypeTestExpr precedingTypeTest = (BLangTypeTestExpr) precedingGuard;
                BLangTypeTestExpr currentTypeTest = (BLangTypeTestExpr) currentGuard;
                return ((BLangSimpleVarRef) precedingTypeTest.expr).variableName.toString().equals(
                        ((BLangSimpleVarRef) currentTypeTest.expr).variableName.toString()) &&
                        precedingTypeTest.typeNode.type.tag == currentTypeTest.typeNode.type.tag;
            }
            return false;
        }

        return currentGuard != null || precedingGuard == null;
    }

    /**
     * This method will determine if the current structured pattern will be unreachable due to a preceding pattern.
     *
     * @param precedingVar the structured pattern that appears on top
     * @param var          the structured pattern that appears after the precedingVar
     * @param errorTypeInMatchExpr
     * @return true if the the current pattern is unreachable due to the preceding pattern
     */
    private boolean checkStructuredPatternSimilarity(BLangVariable precedingVar,
                                                     BLangVariable var,
                                                     boolean errorTypeInMatchExpr) {
        if (precedingVar.type.tag == TypeTags.SEMANTIC_ERROR || var.type.tag == TypeTags.SEMANTIC_ERROR) {
            return false;
        }

        if (precedingVar.getKind() == NodeKind.RECORD_VARIABLE && var.getKind() == NodeKind.RECORD_VARIABLE) {
            BLangRecordVariable precedingRecVar = (BLangRecordVariable) precedingVar;
            BLangRecordVariable recVar = (BLangRecordVariable) var;
            Map<String, BLangVariable> recVarAsMap = recVar.variableList.stream()
                    .collect(Collectors.toMap(
                            keyValue -> keyValue.key.value,
                            keyValue -> keyValue.valueBindingPattern
                    ));

            if (precedingRecVar.variableList.size() > recVar.variableList.size()) {
                return false;
            }

            for (int i = 0; i < precedingRecVar.variableList.size(); i++) {
                BLangRecordVariableKeyValue precedingKeyValue = precedingRecVar.variableList.get(i);
                if (!recVarAsMap.containsKey(precedingKeyValue.key.value)) {
                    return false;
                }
                if (!checkStructuredPatternSimilarity(
                        precedingKeyValue.valueBindingPattern,
                        recVarAsMap.get(precedingKeyValue.key.value),
                        errorTypeInMatchExpr)) {
                    return false;
                }
            }

            if (precedingRecVar.hasRestParam() && recVar.hasRestParam()) {
                return true;
            }

            return precedingRecVar.hasRestParam() || !recVar.hasRestParam();
        }

        if (precedingVar.getKind() == NodeKind.TUPLE_VARIABLE && var.getKind() == NodeKind.TUPLE_VARIABLE) {
            List<BLangVariable> precedingMemberVars = ((BLangTupleVariable) precedingVar).memberVariables;
            BLangVariable precedingRestVar = ((BLangTupleVariable) precedingVar).restVariable;
            List<BLangVariable> memberVars = ((BLangTupleVariable) var).memberVariables;
            BLangVariable memberRestVar = ((BLangTupleVariable) var).restVariable;

            if (precedingRestVar != null && memberRestVar != null) {
                return true;
            }

            if (precedingRestVar == null && memberRestVar == null
                    && precedingMemberVars.size() != memberVars.size()) {
                return false;
            }

            if (precedingRestVar != null && precedingMemberVars.size() > memberVars.size()) {
                return false;
            }

            if (memberRestVar != null) {
                return false;
            }

            for (int i = 0; i < memberVars.size(); i++) {
                if (!checkStructuredPatternSimilarity(precedingMemberVars.get(i), memberVars.get(i),
                        errorTypeInMatchExpr)) {
                    return false;
                }
            }
            return true;
        }


        if (precedingVar.getKind() == NodeKind.ERROR_VARIABLE && var.getKind() == NodeKind.ERROR_VARIABLE) {
            BLangErrorVariable precedingErrVar = (BLangErrorVariable) precedingVar;
            BLangErrorVariable errVar = (BLangErrorVariable) var;

            // Rest pattern in previous binding-pattern can bind to all the error details,
            // hence current error pattern is not reachable.
            if (precedingErrVar.restDetail != null && isDirectErrorBindingPattern(precedingErrVar)) {
                return true;
            }

            // Current pattern can bind anything that is bound in preceding, to current's rest binding var.
            if (errVar.restDetail != null) {
                return false;
            }

            if (precedingErrVar.detail != null && errVar.detail != null) {
                // If preceding detail binding list contains all the details in current list,
                // even though preceding contains more bindings, since a binding can bind to (),
                // current is shadowed from preceding.
                // Error details are a map<anydata|error>
                Map<String, BLangVariable> preDetails = precedingErrVar.detail.stream()
                        .collect(Collectors.toMap(entry -> entry.key.value, entry -> entry.valueBindingPattern));

                for (BLangErrorVariable.BLangErrorDetailEntry detailEntry : errVar.detail) {
                    BLangVariable correspondingCurDetail = preDetails.get(detailEntry.key.value);
                    if (correspondingCurDetail == null) {
                        // Current binding pattern have more details to bind to
                        return false;
                    }
                    boolean similar =
                            checkStructuredPatternSimilarity(detailEntry.valueBindingPattern, correspondingCurDetail,
                                    errorTypeInMatchExpr);
                    if (!similar) {
                        return false;
                    }
                }
            }
            return true;
        }

        if (precedingVar.getKind() == NodeKind.VARIABLE
                && ((BLangSimpleVariable) precedingVar).name.value.equals(Names.IGNORE.value)
                && var.getKind() == NodeKind.ERROR_VARIABLE) {
            return false;
        }

        return precedingVar.getKind() == NodeKind.VARIABLE;
    }

    private boolean isDirectErrorBindingPattern(BLangErrorVariable precedingErrVar) {
        return precedingErrVar.typeNode == null;
    }

    /**
     * This method will check if the static match pattern is valid based on the matching type.
     *
     * @param matchType type of the expression being matched.
     * @param literal   the static match pattern.
     * @return true if the pattern is valid, else false.
     */
    private boolean isValidStaticMatchPattern(BType matchType, BLangExpression literal) {
        if (literal.type.tag == TypeTags.NONE) {
            return true; // When matching '_'
        }

        if (types.isSameType(literal.type, matchType)) {
            return true;
        }

        if (TypeTags.ANY == literal.type.tag) {
            return true;
        }

        switch (matchType.tag) {
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.JSON:
                return true;
            case TypeTags.UNION:
                BUnionType unionMatchType = (BUnionType) matchType;
                return unionMatchType.getMemberTypes()
                        .stream()
                        .anyMatch(memberMatchType -> isValidStaticMatchPattern(memberMatchType, literal));
            case TypeTags.TUPLE:
                if (literal.type.tag == TypeTags.TUPLE) {
                    BLangListConstructorExpr tupleLiteral = (BLangListConstructorExpr) literal;
                    BTupleType literalTupleType = (BTupleType) literal.type;
                    BTupleType matchTupleType = (BTupleType) matchType;
                    if (literalTupleType.tupleTypes.size() != matchTupleType.tupleTypes.size()) {
                        return false;
                    }
                    return IntStream.range(0, literalTupleType.tupleTypes.size())
                            .allMatch(i ->
                                    isValidStaticMatchPattern(matchTupleType.tupleTypes.get(i),
                                            tupleLiteral.exprs.get(i)));
                }
                break;
            case TypeTags.MAP:
                if (literal.type.tag == TypeTags.MAP) {
                    // if match type is map, check if literals match to the constraint
                    BLangRecordLiteral mapLiteral = (BLangRecordLiteral) literal;
                    return IntStream.range(0, mapLiteral.fields.size())
                            .allMatch(i -> isValidStaticMatchPattern(((BMapType) matchType).constraint,
                                                                     ((BLangRecordKeyValueField)
                                                                              mapLiteral.fields.get(i)).valueExpr));
                }
                break;
            case TypeTags.RECORD:
                if (literal.type.tag == TypeTags.MAP) {
                    // if match type is record, the fields must match to the static pattern fields
                    BLangRecordLiteral mapLiteral = (BLangRecordLiteral) literal;
                    BRecordType recordMatchType = (BRecordType) matchType;
                    Map<String, BField> recordFields = recordMatchType.fields;

                    for (RecordLiteralNode.RecordField field : mapLiteral.fields) {
                        BLangRecordKeyValueField literalKeyValue = (BLangRecordKeyValueField) field;
                        String literalKeyName;
                        NodeKind nodeKind = literalKeyValue.key.expr.getKind();
                        if (nodeKind == NodeKind.SIMPLE_VARIABLE_REF) {
                            literalKeyName = ((BLangSimpleVarRef) literalKeyValue.key.expr).variableName.value;
                        } else if (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL) {
                            literalKeyName = ((BLangLiteral) literalKeyValue.key.expr).value.toString();
                        } else {
                            return false;
                        }

                        if (recordFields.containsKey(literalKeyName)) {
                            if (!isValidStaticMatchPattern(
                                    recordFields.get(literalKeyName).type, literalKeyValue.valueExpr)) {
                                return false;
                            }
                        } else if (recordMatchType.sealed ||
                                !isValidStaticMatchPattern(recordMatchType.restFieldType, literalKeyValue.valueExpr)) {
                            return false;
                        }
                    }
                    return true;
                }
                break;
            case TypeTags.BYTE:
                if (literal.type.tag == TypeTags.INT) {
                    return true;
                }
                break;
            case TypeTags.FINITE:
                if (literal.getKind() == NodeKind.LITERAL || literal.getKind() == NodeKind.NUMERIC_LITERAL) {
                    return types.isAssignableToFiniteType(matchType, (BLangLiteral) literal);
                }
                if (literal.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                        ((BLangSimpleVarRef) literal).symbol.getKind() == SymbolKind.CONSTANT) {
                    BConstantSymbol constSymbol = (BConstantSymbol) ((BLangSimpleVarRef) literal).symbol;
                    return types.isAssignableToFiniteType(matchType,
                            (BLangLiteral) ((BFiniteType) constSymbol.type).getValueSpace().iterator().next());
                }
                break;
        }
        return false;
    }

    @Override
    public void visit(BLangForeach foreach) {
        this.loopWithinTransactionCheckStack.push(true);
        this.errorTypes.push(new LinkedHashSet<>());
        boolean statementReturns = this.statementReturns;
        boolean failureHandled = this.failureHandled;
        this.checkStatementExecutionValidity(foreach);
        if (!this.failureHandled) {
            this.failureHandled = foreach.onFailClause != null;
        }
        this.loopCount++;
        analyzeNode(foreach.body, env);
        this.loopCount--;
        this.statementReturns = statementReturns;
        this.failureHandled = failureHandled;
        this.resetLastStatement();
        this.loopWithinTransactionCheckStack.pop();
        analyzeExpr(foreach.collection);
        foreach.body.isBreakable = foreach.onFailClause != null;
        analyzeOnFailClause(foreach.onFailClause);
        this.errorTypes.pop();
    }

    @Override
    public void visit(BLangWhile whileNode) {
        this.loopWithinTransactionCheckStack.push(true);
        this.errorTypes.push(new LinkedHashSet<>());
        boolean statementReturns = this.statementReturns;
        boolean failureHandled = this.failureHandled;
        this.checkStatementExecutionValidity(whileNode);
        if (!this.failureHandled) {
            this.failureHandled = whileNode.onFailClause != null;
        }
        this.loopCount++;
        analyzeNode(whileNode.body, env);
        this.loopCount--;
        this.statementReturns = statementReturns;
        this.failureHandled = failureHandled;
        this.resetLastStatement();
        this.loopWithinTransactionCheckStack.pop();
        analyzeExpr(whileNode.expr);
        analyzeOnFailClause(whileNode.onFailClause);
        this.errorTypes.pop();
    }

    @Override
    public void visit(BLangDo doNode) {
        this.errorTypes.push(new LinkedHashSet<>());
        boolean failureHandled = this.failureHandled;
        this.checkStatementExecutionValidity(doNode);
        if (!this.failureHandled) {
            this.failureHandled = doNode.onFailClause != null;
        }
        analyzeNode(doNode.body, env);
        this.failureHandled = failureHandled;
        doNode.body.isBreakable = doNode.onFailClause != null;
        analyzeOnFailClause(doNode.onFailClause);
        this.errorTypes.pop();
    }


    @Override
    public void visit(BLangFail failNode) {
        this.checkStatementExecutionValidity(failNode);
        this.failVisited = true;
        this.errorThrown = true;
        analyzeExpr(failNode.expr);
        if (this.env.scope.owner.getKind() == SymbolKind.PACKAGE) {
            // Check at module level.
            return;
        }
        typeChecker.checkExpr(failNode.expr, env);
        if (!this.errorTypes.empty()) {
            this.errorTypes.peek().add(getErrorTypes(failNode.expr.type));
        }
        if (!this.failureHandled) {
            this.statementReturns = true;
            BType exprType = env.enclInvokable.getReturnTypeNode().type;
            this.returnTypes.peek().add(exprType);
            if (!types.isAssignable(getErrorTypes(failNode.expr.type), exprType)) {
                dlog.error(failNode.pos, DiagnosticCode.FAIL_EXPR_NO_MATCHING_ERROR_RETURN_IN_ENCL_INVOKABLE);
            }
        }
    }

    @Override
    public void visit(BLangLock lockNode) {
        this.errorTypes.push(new LinkedHashSet<>());
        boolean failureHandled = this.failureHandled;
        this.checkStatementExecutionValidity(lockNode);
        if (!this.failureHandled) {
            this.failureHandled = lockNode.onFailClause != null;
        }
        boolean previousWithinLockBlock = this.withinLockBlock;
        this.withinLockBlock = true;
        lockNode.body.stmts.forEach(e -> analyzeNode(e, env));
        this.withinLockBlock = previousWithinLockBlock;
        this.failureHandled = failureHandled;
        lockNode.body.isBreakable = lockNode.onFailClause != null;
        analyzeOnFailClause(lockNode.onFailClause);
        this.errorTypes.pop();
    }

    @Override
    public void visit(BLangContinue continueNode) {
        this.checkStatementExecutionValidity(continueNode);
        if (this.loopCount == 0) {
            this.dlog.error(continueNode.pos, DiagnosticCode.CONTINUE_CANNOT_BE_OUTSIDE_LOOP);
            return;
        }
        if (checkNextBreakValidityInTransaction()) {
            this.dlog.error(continueNode.pos, DiagnosticCode.CONTINUE_CANNOT_BE_USED_TO_EXIT_TRANSACTION);
            return;
        }
        this.lastStatement = true;
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }

        analyzeNode(pkgEnv.node, env);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        /* ignore */
    }

    public void visit(BLangService serviceNode) {
    }

    public void visit(BLangResource resourceNode) {
        throw new RuntimeException("Deprecated lang feature");
    }

    private void analyzeExportableTypeRef(BSymbol owner, BTypeSymbol symbol, boolean inFuncSignature,
                                          DiagnosticPos pos) {

        if (!inFuncSignature && Symbols.isFlagOn(owner.flags, Flags.ANONYMOUS)) {
            // Specially validate function signatures.
            return;
        }
        if (Symbols.isPublic(owner)) {
            checkForExportableType(symbol, pos);
        }
    }

    private void checkForExportableType(BTypeSymbol symbol, DiagnosticPos pos) {

        if (symbol == null || symbol.type == null || Symbols.isFlagOn(symbol.flags, Flags.TYPE_PARAM)) {
            // This is a built-in symbol or a type Param.
            return;
        }
        switch (symbol.type.tag) {
            case TypeTags.ARRAY:
                checkForExportableType(((BArrayType) symbol.type).eType.tsymbol, pos);
                return;
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) symbol.type;
                tupleType.tupleTypes.forEach(t -> checkForExportableType(t.tsymbol, pos));
                if (tupleType.restType != null) {
                    checkForExportableType(tupleType.restType.tsymbol, pos);
                }
                return;
            case TypeTags.MAP:
                checkForExportableType(((BMapType) symbol.type).constraint.tsymbol, pos);
                return;
            case TypeTags.RECORD:
                if (Symbols.isFlagOn(symbol.flags, Flags.ANONYMOUS)) {
                    BRecordType recordType = (BRecordType) symbol.type;
                    recordType.fields.values().forEach(f -> checkForExportableType(f.type.tsymbol, pos));
                    if (recordType.restFieldType != null) {
                        checkForExportableType(recordType.restFieldType.tsymbol, pos);
                    }
                    return;
                }
                break;
            case TypeTags.TABLE:
                BTableType tableType = (BTableType) symbol.type;
                if (tableType.constraint != null) {
                    checkForExportableType(tableType.constraint.tsymbol, pos);
                }
                return;
            case TypeTags.STREAM:
                BStreamType streamType = (BStreamType) symbol.type;
                if (streamType.constraint != null) {
                    checkForExportableType(streamType.constraint.tsymbol, pos);
                }
                return;
            case TypeTags.INVOKABLE:
                BInvokableType invokableType = (BInvokableType) symbol.type;
                if (invokableType.paramTypes != null) {
                    for (BType paramType : invokableType.paramTypes) {
                        checkForExportableType(paramType.tsymbol, pos);
                    }
                }
                if (invokableType.restType != null) {
                    checkForExportableType(invokableType.restType.tsymbol, pos);
                }
                checkForExportableType(invokableType.retType.tsymbol, pos);
                return;
            case TypeTags.PARAMETERIZED_TYPE:
                BTypeSymbol parameterizedType = ((BParameterizedType) symbol.type).paramValueType.tsymbol;
                checkForExportableType(parameterizedType, pos);
                return;
            // TODO : Add support for other types. such as union and objects
        }
        if (!Symbols.isPublic(symbol)) {
            dlog.error(pos, DiagnosticCode.ATTEMPT_EXPOSE_NON_PUBLIC_SYMBOL, symbol.name);
        }
    }

    public void visit(BLangLetExpression letExpression) {
        int ownerSymTag = this.env.scope.owner.tag;
        if ((ownerSymTag & SymTag.RECORD) == SymTag.RECORD) {
            dlog.error(letExpression.pos, DiagnosticCode.LET_EXPRESSION_NOT_YET_SUPPORTED_RECORD_FIELD);
        } else if ((ownerSymTag & SymTag.OBJECT) == SymTag.OBJECT) {
            dlog.error(letExpression.pos, DiagnosticCode.LET_EXPRESSION_NOT_YET_SUPPORTED_OBJECT_FIELD);
        }

        // This is to support when let expressions are used in return statements
        // Since variable declarations are visited after return node, this stops false positive unreachable code error
        boolean returnStateBefore = this.statementReturns;
        this.statementReturns = false;

        for (BLangLetVariable letVariable : letExpression.letVarDeclarations) {
            analyzeNode((BLangNode) letVariable.definitionNode, letExpression.env);
        }

        this.statementReturns = returnStateBefore;
        analyzeExpr(letExpression.expr, letExpression.env);
    }

    public void visit(BLangSimpleVariable varNode) {

        analyzeTypeNode(varNode.typeNode, this.env);

        analyzeExpr(varNode.expr);

        if (Objects.isNull(varNode.symbol)) {
            return;
        }

        if (!Symbols.isPublic(varNode.symbol)) {
            return;
        }

        int ownerSymTag = this.env.scope.owner.tag;
        if ((ownerSymTag & SymTag.RECORD) == SymTag.RECORD || (ownerSymTag & SymTag.OBJECT) == SymTag.OBJECT) {
            analyzeExportableTypeRef(this.env.scope.owner, varNode.type.tsymbol, false, varNode.pos);
        } else if ((ownerSymTag & SymTag.INVOKABLE) != SymTag.INVOKABLE) {
            // Only global level simpleVarRef, listeners etc.
            analyzeExportableTypeRef(varNode.symbol, varNode.type.tsymbol, false, varNode.pos);
        }

        varNode.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, env));
    }

    private void checkWorkerPeerWorkerUsageInsideWorker(DiagnosticPos pos, BSymbol symbol, SymbolEnv env) {
        if ((symbol.flags & Flags.WORKER) == Flags.WORKER) {
            if (isCurrentPositionInWorker(env) && env.scope.lookup(symbol.name).symbol == null) {
                if (referingForkedWorkerOutOfFork(symbol, env)) {
                    return;
                }
                dlog.error(pos, DiagnosticCode.INVALID_WORKER_REFERRENCE, symbol.name);
            }
        }
    }

    private boolean isCurrentPositionInWorker(SymbolEnv env) {
        if (env.enclInvokable != null && env.enclInvokable.flagSet.contains(Flag.WORKER)) {
            return true;
        }
        if (env.enclEnv != null
                && !(env.enclEnv.node.getKind() == NodeKind.PACKAGE
                    || env.enclEnv.node.getKind() == NodeKind.OBJECT_TYPE)) {
            return isCurrentPositionInWorker(env.enclEnv);
        }
        return false;
    }

    private boolean referingForkedWorkerOutOfFork(BSymbol symbol, SymbolEnv env) {
        return (symbol.flags & Flags.FORKED) == Flags.FORKED
                && env.enclInvokable.getKind() == NodeKind.FUNCTION
                && ((BLangFunction) env.enclInvokable).anonForkName == null;
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {

        if (bLangTupleVariable.typeNode != null) {
            analyzeNode(bLangTupleVariable.typeNode, this.env);
        }
        analyzeExpr(bLangTupleVariable.expr);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {

        if (bLangRecordVariable.typeNode != null) {
            analyzeNode(bLangRecordVariable.typeNode, this.env);
        }
        analyzeExpr(bLangRecordVariable.expr);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {

        if (bLangErrorVariable.typeNode != null) {
            analyzeNode(bLangErrorVariable.typeNode, this.env);
        }
        analyzeExpr(bLangErrorVariable.expr);
    }

    private BType getNilableType(BType type) {
        if (type.isNullable()) {
            return type;
        }

        BUnionType unionType = BUnionType.create(null);

        if (type.tag == TypeTags.UNION) {
            LinkedHashSet<BType> memTypes = new LinkedHashSet<>(((BUnionType) type).getMemberTypes());
            unionType.addAll(memTypes);
        }

        unionType.add(type);
        unionType.add(symTable.nilType);
        return unionType;
    }

    public void visit(BLangIdentifier identifierNode) {
        /* ignore */
    }

    public void visit(BLangAnnotation annotationNode) {
        annotationNode.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, env));
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        analyzeExpr(annAttachmentNode.expr);
        BAnnotationSymbol annotationSymbol = annAttachmentNode.annotationSymbol;
        if (annotationSymbol != null && Symbols.isFlagOn(annotationSymbol.flags, Flags.DEPRECATED)) {
            dlog.warning(annAttachmentNode.pos, DiagnosticCode.USAGE_OF_DEPRECATED_CONSTRUCT, annotationSymbol);
        }
    }

    public void visit(BLangSimpleVariableDef varDefNode) {
        this.checkStatementExecutionValidity(varDefNode);
        analyzeNode(varDefNode.var, env);
    }

    public void visit(BLangCompoundAssignment compoundAssignment) {
        this.checkStatementExecutionValidity(compoundAssignment);
        analyzeExpr(compoundAssignment.varRef);
        analyzeExpr(compoundAssignment.expr);
    }

    public void visit(BLangAssignment assignNode) {
        this.checkStatementExecutionValidity(assignNode);
        analyzeExpr(assignNode.varRef);
        analyzeExpr(assignNode.expr);
    }

    public void visit(BLangRecordDestructure stmt) {
        this.checkDuplicateVarRefs(getVarRefs(stmt.varRef));
        this.checkStatementExecutionValidity(stmt);
        analyzeExpr(stmt.varRef);
        analyzeExpr(stmt.expr);
    }

    public void visit(BLangErrorDestructure stmt) {
        this.checkDuplicateVarRefs(getVarRefs(stmt.varRef));
        this.checkStatementExecutionValidity(stmt);
        analyzeExpr(stmt.varRef);
        analyzeExpr(stmt.expr);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        this.checkDuplicateVarRefs(getVarRefs(stmt.varRef));
        this.checkStatementExecutionValidity(stmt);
        analyzeExpr(stmt.varRef);
        analyzeExpr(stmt.expr);
    }

    private void checkDuplicateVarRefs(List<BLangExpression> varRefs) {
        checkDuplicateVarRefs(varRefs, new HashSet<>());
    }

    private void checkDuplicateVarRefs(List<BLangExpression> varRefs, Set<BSymbol> symbols) {
        for (BLangExpression varRef : varRefs) {
            if (varRef == null || (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF
                    && varRef.getKind() != NodeKind.RECORD_VARIABLE_REF
                    && varRef.getKind() != NodeKind.ERROR_VARIABLE_REF
                    && varRef.getKind() != NodeKind.TUPLE_VARIABLE_REF)) {
                continue;
            }

            if (varRef.getKind() == NodeKind.SIMPLE_VARIABLE_REF
                    && names.fromIdNode(((BLangSimpleVarRef) varRef).variableName) == Names.IGNORE) {
                continue;
            }

            if (varRef.getKind() == NodeKind.TUPLE_VARIABLE_REF) {
                checkDuplicateVarRefs(getVarRefs((BLangTupleVarRef) varRef), symbols);
            }

            if (varRef.getKind() == NodeKind.RECORD_VARIABLE_REF) {
                checkDuplicateVarRefs(getVarRefs((BLangRecordVarRef) varRef), symbols);
            }

            if (varRef.getKind() == NodeKind.ERROR_VARIABLE_REF) {
                checkDuplicateVarRefs(getVarRefs((BLangErrorVarRef) varRef), symbols);
            }

            BLangVariableReference varRefExpr = (BLangVariableReference) varRef;
            if (varRefExpr.symbol != null && !symbols.add(varRefExpr.symbol)) {
                this.dlog.error(varRef.pos, DiagnosticCode.DUPLICATE_VARIABLE_IN_BINDING_PATTERN,
                        varRefExpr.symbol);
            }
        }
    }

    private List<BLangExpression> getVarRefs(BLangRecordVarRef varRef) {
        List<BLangExpression> varRefs = varRef.recordRefFields.stream()
                .map(e -> e.variableReference).collect(Collectors.toList());
        varRefs.add((BLangExpression) varRef.restParam);
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
        varRefs.add(varRef.restVar);
        return varRefs;
    }

    private List<BLangExpression> getVarRefs(BLangTupleVarRef varRef) {
        List<BLangExpression> varRefs = new ArrayList<>(varRef.expressions);
        varRefs.add((BLangExpression) varRef.restParam);
        return varRefs;
    }

    public void visit(BLangBreak breakNode) {
        this.checkStatementExecutionValidity(breakNode);
        if (this.loopCount == 0) {
            this.dlog.error(breakNode.pos, DiagnosticCode.BREAK_CANNOT_BE_OUTSIDE_LOOP);
            return;
        }
        if (checkNextBreakValidityInTransaction()) {
            this.dlog.error(breakNode.pos, DiagnosticCode.BREAK_CANNOT_BE_USED_TO_EXIT_TRANSACTION);
            return;
        }
        this.lastStatement = true;
    }

    public void visit(BLangThrow throwNode) {
        /* ignore */
    }

    public void visit(BLangPanic panicNode) {
        this.checkStatementExecutionValidity(panicNode);
        this.statementReturns = true;
        analyzeExpr(panicNode.expr);
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        this.checkStatementExecutionValidity(xmlnsStmtNode);
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        this.checkStatementExecutionValidity(exprStmtNode);
        analyzeExpr(exprStmtNode.expr);
        validateExprStatementExpression(exprStmtNode);
    }

    private void validateExprStatementExpression(BLangExpressionStmt exprStmtNode) {
        BLangExpression expr = exprStmtNode.expr;

        if (expr.getKind() == NodeKind.WORKER_SYNC_SEND) {
            return;
        }

        while (expr.getKind() == NodeKind.MATCH_EXPRESSION ||
                expr.getKind() == NodeKind.CHECK_EXPR ||
                expr.getKind() == NodeKind.CHECK_PANIC_EXPR) {
            if (expr.getKind() == NodeKind.MATCH_EXPRESSION) {
                expr = ((BLangMatchExpression) expr).expr;
            } else if (expr.getKind() == NodeKind.CHECK_EXPR) {
                expr = ((BLangCheckedExpr) expr).expr;
            } else if (expr.getKind() == NodeKind.CHECK_PANIC_EXPR) {
                expr = ((BLangCheckPanickedExpr) expr).expr;
            }
        }
        // Allowed expression kinds
        if (expr.getKind() == NodeKind.INVOCATION || expr.getKind() == NodeKind.WAIT_EXPR) {
            return;
        }
        // For other expressions, error is logged already.
        if (expr.type == symTable.nilType) {
            dlog.error(exprStmtNode.pos, DiagnosticCode.INVALID_EXPR_STATEMENT);
        }
    }

    public void visit(BLangTryCatchFinally tryNode) {
        /* ignore */
    }

    public void visit(BLangCatch catchNode) {
        /* ignore */
    }

    private boolean isTopLevel() {
        SymbolEnv env = this.env;
        return env.enclInvokable.body == env.node;
    }

    private boolean isInWorker() {
        return env.enclInvokable.flagSet.contains(Flag.WORKER);
    }

    private boolean isCommunicationAllowedLocation(String workerIdentifier) {
        return (isDefaultWorkerCommunication(workerIdentifier) && isInWorker()) || isTopLevel();
    }

    private boolean isDefaultWorkerCommunication(String workerIdentifier) {
        return workerIdentifier.equals(DEFAULT_WORKER_NAME);
    }

    private boolean workerExists(BType type, String workerName) {
        if (isDefaultWorkerCommunication(workerName) && isInWorker()) {
            return true;
        }
        if (type == symTable.semanticError) {
            return false;
        }
        return type.tag == TypeTags.FUTURE && ((BFutureType) type).workerDerivative;
    }


    // Asynchronous Send Statement
    public void visit(BLangWorkerSend workerSendNode) {
        BSymbol receiver = symResolver.lookupSymbolInMainSpace(env, names.fromIdNode(workerSendNode.workerIdentifier));
        if ((receiver.tag & SymTag.VARIABLE) != SymTag.VARIABLE) {
            receiver = symTable.notFoundSymbol;
        }
        verifyPeerCommunication(workerSendNode.pos, receiver, workerSendNode.workerIdentifier.value);

        this.checkStatementExecutionValidity(workerSendNode);
        if (workerSendNode.isChannel) {
            analyzeExpr(workerSendNode.expr);
            if (workerSendNode.keyExpr != null) {
                analyzeExpr(workerSendNode.keyExpr);
            }
            return;
        }

        WorkerActionSystem was = this.workerActionSystemStack.peek();

        BType type = workerSendNode.expr.type;
        if (type == symTable.semanticError) {
            // Error of this is already printed as undef-var
            was.hasErrors = true;
        } else if (workerSendNode.expr instanceof ActionNode) {
            this.dlog.error(workerSendNode.expr.pos, DiagnosticCode.INVALID_SEND_EXPR);
        } else if (!type.isAnydata()) {
            this.dlog.error(workerSendNode.pos, DiagnosticCode.INVALID_TYPE_FOR_SEND, type);
        }

        String workerName = workerSendNode.workerIdentifier.getValue();
        boolean allowedLocation = isCommunicationAllowedLocation(workerName);
        if (!allowedLocation) {
            this.dlog.error(workerSendNode.pos, DiagnosticCode.INVALID_WORKER_SEND_POSITION);
            was.hasErrors = true;
        }

        if (!this.workerExists(workerSendNode.type, workerName)) {
            this.dlog.error(workerSendNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
            was.hasErrors = true;
        }

        workerSendNode.type = createAccumulatedErrorTypeForMatchingRecive(workerSendNode.pos, workerSendNode.expr.type);
        was.addWorkerAction(workerSendNode);
        analyzeExpr(workerSendNode.expr);
        validateActionParentNode(workerSendNode.pos, workerSendNode.expr);
    }

    private BType createAccumulatedErrorTypeForMatchingRecive(DiagnosticPos pos, BType exprType) {
        Set<BType> returnTypesUpToNow = this.returnTypes.peek();
        LinkedHashSet<BType> returnTypeAndSendType = new LinkedHashSet<BType>() {
            {
                Comparator.comparing(BType::toString);
            }
        };
        for (BType returnType : returnTypesUpToNow) {
            if (returnType.tag == TypeTags.ERROR) {
                returnTypeAndSendType.add(returnType);
            } else {
                this.dlog.error(pos, DiagnosticCode.WORKER_SEND_AFTER_RETURN);
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
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        BSymbol receiver = symResolver.lookupSymbolInMainSpace(env, names.fromIdNode(syncSendExpr.workerIdentifier));
        if ((receiver.tag & SymTag.VARIABLE) != SymTag.VARIABLE) {
            receiver = symTable.notFoundSymbol;
        }
        verifyPeerCommunication(syncSendExpr.pos, receiver, syncSendExpr.workerIdentifier.value);

        // Validate worker synchronous send
        validateActionParentNode(syncSendExpr.pos, syncSendExpr);
        String workerName = syncSendExpr.workerIdentifier.getValue();
        WorkerActionSystem was = this.workerActionSystemStack.peek();

        boolean allowedLocation = isCommunicationAllowedLocation(workerName);
        if (!allowedLocation) {
            this.dlog.error(syncSendExpr.pos, DiagnosticCode.INVALID_WORKER_SEND_POSITION);
            was.hasErrors = true;
        }

        if (!this.workerExists(syncSendExpr.workerType, workerName)) {
            this.dlog.error(syncSendExpr.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
            was.hasErrors = true;
        }
        syncSendExpr.type = createAccumulatedErrorTypeForMatchingRecive(syncSendExpr.pos, syncSendExpr.expr.type);
        was.addWorkerAction(syncSendExpr);
        analyzeExpr(syncSendExpr.expr);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        // Validate worker receive
        validateActionParentNode(workerReceiveNode.pos, workerReceiveNode);
        BSymbol sender = symResolver.lookupSymbolInMainSpace(env, names.fromIdNode(workerReceiveNode.workerIdentifier));
        if ((sender.tag & SymTag.VARIABLE) != SymTag.VARIABLE) {
            sender = symTable.notFoundSymbol;
        }
        verifyPeerCommunication(workerReceiveNode.pos, sender, workerReceiveNode.workerIdentifier.value);

        if (workerReceiveNode.isChannel) {
            if (workerReceiveNode.keyExpr != null) {
                analyzeExpr(workerReceiveNode.keyExpr);
            }
            return;
        }
        WorkerActionSystem was = this.workerActionSystemStack.peek();

        String workerName = workerReceiveNode.workerIdentifier.getValue();
        boolean allowedLocation = isCommunicationAllowedLocation(workerName);
        if (!allowedLocation) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.INVALID_WORKER_RECEIVE_POSITION);
            was.hasErrors = true;
        }

        if (!this.workerExists(workerReceiveNode.workerType, workerName)) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
            was.hasErrors = true;
        }

        workerReceiveNode.matchingSendsError = createAccumulatedErrorTypeForMatchingSyncSend(workerReceiveNode);

        was.addWorkerAction(workerReceiveNode);
    }

    private void verifyPeerCommunication(DiagnosticPos pos, BSymbol otherWorker, String otherWorkerName) {
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
                    dlog.error(pos, DiagnosticCode.WORKER_INTERACTIONS_ONLY_ALLOWED_BETWEEN_PEERS);
                }
                return;
            }

            Scope enclFunctionScope = env.enclEnv.enclEnv.scope;
            BInvokableSymbol wLambda = (BInvokableSymbol) enclFunctionScope.lookup(workerDerivedName).symbol;
            // Interactions across fork
            if (wLambda != null && funcNode.anonForkName != null
                    && !funcNode.anonForkName.equals(wLambda.enclForkName)) {
                dlog.error(pos, DiagnosticCode.WORKER_INTERACTIONS_ONLY_ALLOWED_BETWEEN_PEERS);
            }
        } else {
            // Worker interactions outside of worker constructs (in default worker)
            BInvokableSymbol wLambda = (BInvokableSymbol) env.scope.lookup(workerDerivedName).symbol;
            if (wLambda != null && wLambda.enclForkName != null) {
                dlog.error(pos, DiagnosticCode.WORKER_INTERACTIONS_ONLY_ALLOWED_BETWEEN_PEERS);
            }
        }
    }

    public BType createAccumulatedErrorTypeForMatchingSyncSend(BLangWorkerReceive workerReceiveNode) {
        Set<BType> returnTypesUpToNow = this.returnTypes.peek();
        LinkedHashSet<BType> returnTypeAndSendType = new LinkedHashSet<>();
        for (BType returnType : returnTypesUpToNow) {
            if (returnType.tag == TypeTags.ERROR) {
                returnTypeAndSendType.add(returnType);
            } else {
                this.dlog.error(workerReceiveNode.pos, DiagnosticCode.WORKER_RECEIVE_AFTER_RETURN);
            }
        }
        returnTypeAndSendType.add(symTable.nilType);
        if (returnTypeAndSendType.size() > 1) {
            return BUnionType.create(null, returnTypeAndSendType);
        } else {
            return symTable.nilType;
        }
    }

    public void visit(BLangLiteral literalExpr) {
        if (literalExpr.type.tag == TypeTags.NIL &&
                NULL_LITERAL.equals(literalExpr.originalValue) &&
                !literalExpr.isJSONContext && !this.isJSONContext) {
            dlog.error(literalExpr.pos, DiagnosticCode.INVALID_USE_OF_NULL_LITERAL);
        }
    }

    public void visit(BLangListConstructorExpr listConstructorExpr) {
        analyzeExprs(listConstructorExpr.exprs);
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        analyzeExprs(tableConstructorExpr.recordLiteralList);
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        List<RecordLiteralNode.RecordField> fields = recordLiteral.fields;

        for (RecordLiteralNode.RecordField field : fields) {
            if (field.isKeyValueField()) {
                analyzeExpr(((BLangRecordKeyValueField) field).valueExpr);
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                analyzeExpr((BLangRecordLiteral.BLangRecordVarNameField) field);
            } else {
                analyzeExpr(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr);
            }
        }

        Set<Object> names = new HashSet<>();
        Set<Object> neverTypedKeys = new HashSet<>();
        BType type = recordLiteral.type;
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

                analyzeExpr(spreadOpExpr);

                BType spreadOpExprType = spreadOpExpr.type;
                int spreadFieldTypeTag = spreadOpExprType.tag;
                if (spreadFieldTypeTag == TypeTags.MAP) {
                    if (inclusiveTypeSpreadField != null) {
                        this.dlog.error(spreadOpExpr.pos, DiagnosticCode.MULTIPLE_INCLUSIVE_TYPES);
                        continue;
                    }
                    inclusiveTypeSpreadField = spreadOpField;

                    if (fields.size() > 1) {
                        if (names.size() > 0) {
                            this.dlog.error(spreadOpExpr.pos,
                                            DiagnosticCode.SPREAD_FIELD_MAY_DULPICATE_ALREADY_SPECIFIED_KEYS,
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
                        this.dlog.error(spreadOpExpr.pos, DiagnosticCode.MULTIPLE_INCLUSIVE_TYPES);
                    } else {
                        inclusiveTypeSpreadField = spreadOpField;
                    }
                }

                LinkedHashMap<String, BField> fieldsInRecordType = spreadExprRecordType.fields;
                for (Object fieldName : names) {
                    if (!fieldsInRecordType.containsKey(fieldName) && !isSpreadExprRecordTypeSealed) {
                        this.dlog.error(spreadOpExpr.pos,
                                DiagnosticCode.SPREAD_FIELD_MAY_DULPICATE_ALREADY_SPECIFIED_KEYS,
                                spreadOpExpr);
                        break;
                    }
                }

                for (BField bField : fieldsInRecordType.values()) {
                    String name = bField.name.value;
                    if (names.contains(name)) {
                        if (bField.type.tag != TypeTags.NEVER) {
                            this.dlog.error(spreadOpExpr.pos, DiagnosticCode.DUPLICATE_KEY_IN_RECORD_LITERAL_SPREAD_OP,
                                    recordLiteral.type.getKind().typeName(), name, spreadOpField);
                        }
                        continue;
                    }

                    if (bField.type.tag == TypeTags.NEVER) {
                        neverTypedKeys.add(name);
                        continue;
                    }

                    if (!neverTypedKeys.remove(name) &&
                            inclusiveTypeSpreadField != null && isSpreadExprRecordTypeSealed) {
                        this.dlog.error(spreadOpExpr.pos,
                                        DiagnosticCode.POSSIBLE_DUPLICATE_OF_FIELD_SPECIFIED_VIA_SPREAD_OP,
                                        recordLiteral.expectedType.getKind().typeName(), name, spreadOpField);
                    }
                    names.add(name);
                }

            } else {
                if (field.isKeyValueField()) {
                    BLangRecordLiteral.BLangRecordKey key = ((BLangRecordKeyValueField) field).key;
                    keyExpr = key.expr;
                    if (key.computedKey) {
                        analyzeExpr(keyExpr);
                        continue;
                    }
                } else {
                    keyExpr = (BLangRecordLiteral.BLangRecordVarNameField) field;
                }

                if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    String name = ((BLangSimpleVarRef) keyExpr).variableName.value;

                    if (names.contains(name)) {
                        this.dlog.error(keyExpr.pos, DiagnosticCode.DUPLICATE_KEY_IN_RECORD_LITERAL,
                                        recordLiteral.expectedType.getKind().typeName(), name);
                    } else if (inclusiveTypeSpreadField != null && !neverTypedKeys.contains(name)) {
                        this.dlog.error(keyExpr.pos, DiagnosticCode.POSSIBLE_DUPLICATE_OF_FIELD_SPECIFIED_VIA_SPREAD_OP,
                                        name, inclusiveTypeSpreadField);
                    }

                    if (!isInferredRecordForMapCET && isOpenRecord && !((BRecordType) type).fields.containsKey(name)) {
                        dlog.error(keyExpr.pos, DiagnosticCode.INVALID_RECORD_LITERAL_IDENTIFIER_KEY, name);
                    }

                    names.add(name);
                } else if (keyExpr.getKind() == NodeKind.LITERAL || keyExpr.getKind() == NodeKind.NUMERIC_LITERAL) {
                    Object name = ((BLangLiteral) keyExpr).value;
                    if (names.contains(name)) {
                        this.dlog.error(keyExpr.pos, DiagnosticCode.DUPLICATE_KEY_IN_RECORD_LITERAL,
                                        recordLiteral.parent.type.getKind().typeName(), name);
                    } else if (inclusiveTypeSpreadField != null && !neverTypedKeys.contains(name)) {
                        this.dlog.error(keyExpr.pos, DiagnosticCode.POSSIBLE_DUPLICATE_OF_FIELD_SPECIFIED_VIA_SPREAD_OP,
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

    public void visit(BLangSimpleVarRef varRefExpr) {
        switch (varRefExpr.parent.getKind()) {
            // Referring workers for worker interactions are allowed, hence skip the check.
            case WORKER_RECEIVE:
            case WORKER_SEND:
            case WORKER_SYNC_SEND:
                return;
            default:
                if (varRefExpr.type != null && varRefExpr.type.tag == TypeTags.FUTURE) {
                    checkWorkerPeerWorkerUsageInsideWorker(varRefExpr.pos, varRefExpr.symbol, this.env);
                }
        }
        if (varRefExpr.symbol != null && Symbols.isFlagOn(varRefExpr.symbol.flags, Flags.DEPRECATED)) {
            dlog.warning(varRefExpr.pos, DiagnosticCode.USAGE_OF_DEPRECATED_CONSTRUCT, varRefExpr);
        }
    }

    public void visit(BLangRecordVarRef varRefExpr) {
        /* ignore */
    }

    public void visit(BLangErrorVarRef varRefExpr) {
        /* ignore */
    }

    public void visit(BLangTupleVarRef varRefExpr) {
        /* ignore */
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        analyzeExpr(fieldAccessExpr.expr);
        BSymbol symbol = fieldAccessExpr.symbol;
        if (symbol != null && Symbols.isFlagOn(fieldAccessExpr.symbol.flags, Flags.DEPRECATED)) {
            dlog.warning(fieldAccessExpr.pos, DiagnosticCode.USAGE_OF_DEPRECATED_CONSTRUCT, fieldAccessExpr);
        }
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        analyzeExpr(indexAccessExpr.indexExpr);
        analyzeExpr(indexAccessExpr.expr);
    }

    public void visit(BLangTableMultiKeyExpr tableMultiKeyExpr) {
        analyzeExprs(tableMultiKeyExpr.multiKeyIndexExprs);
    }

    public void visit(BLangInvocation invocationExpr) {
        analyzeExpr(invocationExpr.expr);
        analyzeExprs(invocationExpr.requiredArgs);
        analyzeExprs(invocationExpr.restArgs);

        if ((invocationExpr.symbol != null) && invocationExpr.symbol.kind == SymbolKind.FUNCTION) {
            BSymbol funcSymbol = invocationExpr.symbol;
            if (Symbols.isFlagOn(funcSymbol.flags, Flags.TRANSACTIONAL) && !withinTransactionScope) {
                dlog.error(invocationExpr.pos, DiagnosticCode.TRANSACTIONAL_FUNC_INVOKE_PROHIBITED, invocationExpr);
                return;
            }
            if (Symbols.isFlagOn(funcSymbol.flags, Flags.DEPRECATED)) {
                dlog.warning(invocationExpr.pos, DiagnosticCode.USAGE_OF_DEPRECATED_CONSTRUCT, invocationExpr);
            }

            if (((BInvokableSymbol) funcSymbol).getReturnType().tag == TypeTags.NEVER &&
                    invocationExpr.parent.getKind() != NodeKind.EXPRESSION_STATEMENT) {
                // Log an error if the function returns never and invoked invalidly.
                dlog.error(invocationExpr.pos, DiagnosticCode.INVALID_NEVER_RETURN_TYPED_FUNCTION_INVOCATION,
                        funcSymbol.name);
            }
        }
    }

    public void visit(BLangInvocation.BLangActionInvocation actionInvocation) {
        if (!actionInvocation.async && !this.withinTransactionScope &&
                Symbols.isFlagOn(actionInvocation.symbol.flags, Flags.TRANSACTIONAL)) {
            dlog.error(actionInvocation.pos, DiagnosticCode.TRANSACTIONAL_FUNC_INVOKE_PROHIBITED, actionInvocation);
            return;
        }

        if (actionInvocation.async && this.withinTransactionScope &&
                !Symbols.isFlagOn(actionInvocation.symbol.flags, Flags.TRANSACTIONAL)) {
            dlog.error(actionInvocation.pos, DiagnosticCode.USAGE_OF_START_WITHIN_TRANSACTION_IS_PROHIBITED);
            return;
        }

        analyzeExpr(actionInvocation.expr);
        analyzeExprs(actionInvocation.requiredArgs);
        analyzeExprs(actionInvocation.restArgs);

        if (actionInvocation.symbol.kind == SymbolKind.FUNCTION &&
                Symbols.isFlagOn(actionInvocation.symbol.flags, Flags.DEPRECATED)) {
            dlog.warning(actionInvocation.pos, DiagnosticCode.USAGE_OF_DEPRECATED_CONSTRUCT, actionInvocation);
        }

        if (actionInvocation.flagSet.contains(Flag.TRANSACTIONAL) && !withinTransactionScope) {
            dlog.error(actionInvocation.pos, DiagnosticCode.TRANSACTIONAL_FUNC_INVOKE_PROHIBITED);
            return;
        }


        if (actionInvocation.async && this.withinLockBlock) {
            dlog.error(actionInvocation.pos, actionInvocation.functionPointerInvocation ?
                    DiagnosticCode.USAGE_OF_WORKER_WITHIN_LOCK_IS_PROHIBITED :
                    DiagnosticCode.USAGE_OF_START_WITHIN_LOCK_IS_PROHIBITED);
            return;
        }

        if ((actionInvocation.symbol.tag & SymTag.CONSTRUCTOR) == SymTag.CONSTRUCTOR) {
            dlog.error(actionInvocation.pos, DiagnosticCode.INVALID_FUNCTIONAL_CONSTRUCTOR_INVOCATION,
                       actionInvocation.symbol.name);
            return;
        }

        validateActionInvocation(actionInvocation.pos, actionInvocation);
    }

    private void validateActionInvocation(DiagnosticPos pos, BLangInvocation iExpr) {
        if (iExpr.expr != null) {
            final NodeKind clientNodeKind = iExpr.expr.getKind();
            // Validation against node kind.
            if (clientNodeKind != NodeKind.SIMPLE_VARIABLE_REF && clientNodeKind != NodeKind.FIELD_BASED_ACCESS_EXPR) {
                dlog.error(pos, DiagnosticCode.INVALID_ACTION_INVOCATION_AS_EXPR);
            } else if (clientNodeKind == NodeKind.FIELD_BASED_ACCESS_EXPR) {
                final BLangFieldBasedAccess fieldBasedAccess = (BLangFieldBasedAccess) iExpr.expr;
                if (fieldBasedAccess.expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                    dlog.error(pos, DiagnosticCode.INVALID_ACTION_INVOCATION_AS_EXPR);
                } else {
                    final BLangSimpleVarRef selfName = (BLangSimpleVarRef) fieldBasedAccess.expr;
                    if (!Names.SELF.equals(selfName.symbol.name)) {
                        dlog.error(pos, DiagnosticCode.INVALID_ACTION_INVOCATION_AS_EXPR);
                    }
                }
            }
        }
        validateActionParentNode(pos, iExpr);
    }

    /**
     * Actions can only occur as part of a statement or nested inside other actions.
     */
    private void validateActionParentNode(DiagnosticPos pos, BLangNode node) {
        // Validate for parent nodes.
        BLangNode parent = node.parent;

        while (parent != null) {
            final NodeKind kind = parent.getKind();
            if (parent instanceof StatementNode) {
                return;
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
        dlog.error(pos, DiagnosticCode.INVALID_ACTION_INVOCATION_AS_EXPR);
    }

    public void visit(BLangTypeInit cIExpr) {
        analyzeExprs(cIExpr.argsExpr);
        analyzeExpr(cIExpr.initInvocation);
        BType type = cIExpr.type;
        if (cIExpr.userDefinedType != null && Symbols.isFlagOn(type.tsymbol.flags, Flags.DEPRECATED)) {
            dlog.warning(cIExpr.pos, DiagnosticCode.USAGE_OF_DEPRECATED_CONSTRUCT, type);
        }
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        analyzeExpr(ternaryExpr.expr);
        boolean isJSONCtx = getIsJSONContext(ternaryExpr.type);
        this.isJSONContext = isJSONCtx;
        analyzeExpr(ternaryExpr.thenExpr);
        this.isJSONContext = isJSONCtx;
        analyzeExpr(ternaryExpr.elseExpr);
    }

    public void visit(BLangWaitExpr awaitExpr) {
        BLangExpression expr = awaitExpr.getExpression();
        validateWaitFutureExpr(expr);
        analyzeExpr(expr);
        validateActionParentNode(awaitExpr.pos, awaitExpr);
    }

    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        for (BLangWaitForAllExpr.BLangWaitKeyValue keyValue : waitForAllExpr.keyValuePairs) {
            BLangExpression expr = keyValue.valueExpr != null ? keyValue.valueExpr : keyValue.keyExpr;
            validateWaitFutureExpr(expr);
            analyzeExpr(expr);
        }

        validateActionParentNode(waitForAllExpr.pos, waitForAllExpr);
    }

    // wait-future-expr := expression but not mapping-constructor-expr
    private void validateWaitFutureExpr(BLangExpression expr) {
        if (expr.getKind() == NodeKind.RECORD_LITERAL_EXPR) {
            dlog.error(expr.pos, DiagnosticCode.INVALID_WAIT_MAPPING_CONSTRUCTORS);
        }

        if (expr instanceof ActionNode) {
            dlog.error(expr.pos, DiagnosticCode.INVALID_WAIT_ACTIONS);
        }
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        analyzeExpr(xmlElementAccess.expr);
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        analyzeExpr(xmlNavigation.expr);
        if (xmlNavigation.childIndex != null) {
            if (xmlNavigation.navAccessType == XMLNavigationAccess.NavAccessType.DESCENDANTS
                    || xmlNavigation.navAccessType == XMLNavigationAccess.NavAccessType.CHILDREN) {
                dlog.error(xmlNavigation.pos, DiagnosticCode.UNSUPPORTED_INDEX_IN_XML_NAVIGATION);
            }
            analyzeExpr(xmlNavigation.childIndex);
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

            dlog.error(invocation.pos, DiagnosticCode.UNSUPPORTED_METHOD_INVOCATION_XML_NAV);
        }
        expression.methodInvocationAnalyzed = true;
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        // Two scenarios should be handled
        // 1) flush w1 -> Wait till all the asynchronous sends to worker w1 is completed
        // 2) flush -> Wait till all asynchronous sends to all workers are completed
        BLangIdentifier flushWrkIdentifier = workerFlushExpr.workerIdentifier;
        Stack<WorkerActionSystem> workerActionSystems = this.workerActionSystemStack;
        WorkerActionSystem currentWrkerAction = workerActionSystems.peek();
        List<BLangWorkerSend> sendStmts = getAsyncSendStmtsOfWorker(currentWrkerAction);
        if (flushWrkIdentifier != null) {
            List<BLangWorkerSend> sendsToGivenWrkr = sendStmts.stream()
                                                              .filter(bLangNode -> bLangNode.workerIdentifier.equals
                                                                      (flushWrkIdentifier))
                                                              .collect(Collectors.toList());
            if (sendsToGivenWrkr.size() == 0) {
                this.dlog.error(workerFlushExpr.pos, DiagnosticCode.INVALID_WORKER_FLUSH_FOR_WORKER, flushWrkIdentifier,
                                currentWrkerAction.currentWorkerId());
                return;
            } else {
                sendStmts = sendsToGivenWrkr;
            }
        } else {
            if (sendStmts.size() == 0) {
                this.dlog.error(workerFlushExpr.pos, DiagnosticCode.INVALID_WORKER_FLUSH,
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
    public void visit(BLangTrapExpr trapExpr) {
        analyzeExpr(trapExpr.expr);
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        if (validateBinaryExpr(binaryExpr)) {
            boolean isJSONCtx = getIsJSONContext(binaryExpr.lhsExpr.type, binaryExpr.rhsExpr.type);
            this.isJSONContext = isJSONCtx;
            analyzeExpr(binaryExpr.lhsExpr);
            this.isJSONContext = isJSONCtx;
            analyzeExpr(binaryExpr.rhsExpr);
        }
    }

    private boolean validateBinaryExpr(BLangBinaryExpr binaryExpr) {
        // 1) For usual binary expressions the lhs or rhs can never be future types, so return true if both of
        // them are not future types
        if (binaryExpr.lhsExpr.type.tag != TypeTags.FUTURE && binaryExpr.rhsExpr.type.tag != TypeTags.FUTURE) {
            return true;
        }

        // 2) For binary expressions followed with wait lhs and rhs are always future types and this is allowed so
        // return true : wait f1 | f2[orgName + moduleName
        BLangNode parentNode = binaryExpr.parent;
        if (binaryExpr.lhsExpr.type.tag == TypeTags.FUTURE || binaryExpr.rhsExpr.type.tag == TypeTags.FUTURE) {
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
            dlog.error(binaryExpr.pos, DiagnosticCode.OPERATOR_NOT_SUPPORTED, OperatorKind.BITWISE_OR,
                       symTable.futureType);
                return false;
        }

        if (parentNode.getKind() == NodeKind.BINARY_EXPR) {
            return validateBinaryExpr((BLangBinaryExpr) parentNode);
        }
        return true;
    }

    public void visit(BLangElvisExpr elvisExpr) {
        analyzeExpr(elvisExpr.lhsExpr);
        analyzeExpr(elvisExpr.rhsExpr);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        analyzeExpr(groupExpr.expression);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        analyzeExpr(unaryExpr.expr);
    }

    public void visit(BLangTypedescExpr accessExpr) {
        /* ignore */
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        analyzeExpr(conversionExpr.expr);
        conversionExpr.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, env));
    }

    public void visit(BLangXMLQName xmlQName) {
        /* ignore */
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
        analyzeExpr(xmlAttribute.name);
        analyzeExpr(xmlAttribute.value);
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        analyzeExpr(xmlElementLiteral.startTagName);
        analyzeExpr(xmlElementLiteral.endTagName);
        analyzeExprs(xmlElementLiteral.attributes);
        analyzeExprs(xmlElementLiteral.children);
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        analyzeExprs(xmlTextLiteral.textFragments);
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        analyzeExprs(xmlCommentLiteral.textFragments);
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        analyzeExprs(xmlProcInsLiteral.dataFragments);
        analyzeExpr(xmlProcInsLiteral.target);
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        analyzeExprs(xmlQuotedString.textFragments);
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        analyzeExprs(stringTemplateLiteral.exprs);
    }

    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        analyzeExprs(rawTemplateLiteral.strings);
        analyzeExprs(rawTemplateLiteral.insertions);
    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        boolean isWorker = false;

        if (bLangLambdaFunction.function.flagSet.contains(Flag.TRANSACTIONAL) &&
                bLangLambdaFunction.function.flagSet.contains(Flag.WORKER) && !withinTransactionScope) {
            dlog.error(bLangLambdaFunction.pos, DiagnosticCode.TRANSACTIONAL_WORKER_OUT_OF_TRANSACTIONAL_SCOPE,
                    bLangLambdaFunction);
            return;
        }
        if (bLangLambdaFunction.parent.getKind() == NodeKind.VARIABLE) {
            String workerVarName = ((BLangSimpleVariable) bLangLambdaFunction.parent).name.value;
            if (workerVarName.startsWith(WORKER_LAMBDA_VAR_PREFIX)) {
                String workerName = workerVarName.substring(1);
                isWorker = true;
                this.workerActionSystemStack.peek().startWorkerActionStateMachine(workerName,
                                                                                  bLangLambdaFunction.function.pos,
                                                                                  bLangLambdaFunction.function);
            }
        }
        boolean statementReturn = this.statementReturns;
        this.visitFunction(bLangLambdaFunction.function);
        this.statementReturns = statementReturn;

        if (isWorker) {
            this.workerActionSystemStack.peek().endWorkerActionStateMachine();
        }
    }

    public void visit(BLangArrowFunction bLangArrowFunction) {

        analyzeExpr(bLangArrowFunction.body.expr);
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {

        analyzeExpr(xmlAttributeAccessExpr.expr);
        analyzeExpr(xmlAttributeAccessExpr.indexExpr);
    }

    public void visit(BLangIntRangeExpression intRangeExpression) {
        analyzeExpr(intRangeExpression.startExpr);
        analyzeExpr(intRangeExpression.endExpr);
    }


    /* Type Nodes */

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {

        SymbolEnv recordEnv = SymbolEnv.createTypeEnv(recordTypeNode, recordTypeNode.symbol.scope, env);
        for (BLangSimpleVariable field : recordTypeNode.fields) {
            analyzeNode(field, recordEnv);
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {

        SymbolEnv objectEnv = SymbolEnv.createTypeEnv(objectTypeNode, objectTypeNode.symbol.scope, env);
        for (BLangSimpleVariable field : objectTypeNode.fields) {
            analyzeNode(field, objectEnv);
        }

        List<BLangFunction> bLangFunctionList = new ArrayList<>(objectTypeNode.functions);
        if (objectTypeNode.initFunction != null) {
            bLangFunctionList.add(objectTypeNode.initFunction);
        }

        // To ensure the order of the compile errors
        bLangFunctionList.sort(Comparator.comparingInt(function -> function.pos.sLine));
        for (BLangFunction function : bLangFunctionList) {
            this.analyzeNode(function, objectEnv);
        }
    }

    @Override
    public void visit(BLangValueType valueType) {
        /* ignore */
    }

    @Override
    public void visit(BLangArrayType arrayType) {

        analyzeTypeNode(arrayType.elemtype, env);
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        /* ignore */
    }

    public void visit(BLangConstrainedType constrainedType) {

        analyzeTypeNode(constrainedType.constraint, env);
    }

    public void visit(BLangStreamType streamType) {

        analyzeTypeNode(streamType.constraint, env);
        analyzeTypeNode(streamType.error, env);
    }

    public void visit(BLangTableTypeNode tableType) {

        analyzeTypeNode(tableType.constraint, env);

        if (tableType.tableKeyTypeConstraint != null) {
            analyzeTypeNode(tableType.tableKeyTypeConstraint.keyType, env);
        }
    }

    public void visit(BLangErrorType errorType) {
        analyzeTypeNode(errorType.detailType, env);
    }

    public void visit(BLangUserDefinedType userDefinedType) {
        BTypeSymbol typeSymbol = userDefinedType.type.tsymbol;
        if (typeSymbol != null && Symbols.isFlagOn(typeSymbol.flags, Flags.DEPRECATED)) {
            dlog.warning(userDefinedType.pos, DiagnosticCode.USAGE_OF_DEPRECATED_CONSTRUCT, userDefinedType);
        }
    }

    public void visit(BLangTupleTypeNode tupleTypeNode) {

        tupleTypeNode.memberTypeNodes.forEach(memberType -> analyzeTypeNode(memberType, env));
        analyzeTypeNode(tupleTypeNode.restParamType, env);
    }

    public void visit(BLangUnionTypeNode unionTypeNode) {

        unionTypeNode.memberTypeNodes.forEach(memberType -> analyzeTypeNode(memberType, env));
    }

    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {

        for (BLangType constituentTypeNode : intersectionTypeNode.constituentTypeNodes) {
            analyzeTypeNode(constituentTypeNode, env);
        }
    }

    public void visit(BLangFunctionTypeNode functionTypeNode) {

        functionTypeNode.params.forEach(node -> analyzeNode(node, env));
        analyzeTypeNode(functionTypeNode.returnTypeNode, env);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {

        /* Ignore */
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {

        analyzeExpr(bLangVarArgsExpression.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {

        analyzeExpr(bLangNamedArgsExpression.expr);
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        this.failVisited = true;
        analyzeExpr(checkedExpr.expr);

        if (this.env.scope.owner.getKind() == SymbolKind.PACKAGE) {
            // Check at module level.
            return;
        }

        BType exprType = env.enclInvokable.getReturnTypeNode().type;

        if (!this.failureHandled && !types.isAssignable(getErrorTypes(checkedExpr.expr.type), exprType)) {
            dlog.error(checkedExpr.pos, DiagnosticCode.CHECKED_EXPR_NO_MATCHING_ERROR_RETURN_IN_ENCL_INVOKABLE);
        }
        if (!this.errorTypes.empty()) {
            this.errorTypes.peek().add(getErrorTypes(checkedExpr.expr.type));
        }
        returnTypes.peek().add(exprType);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanicExpr) {
        analyzeExpr(checkPanicExpr.expr);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        queryToTableWithKey = queryExpr.isTable() && !queryExpr.fieldNameIdentifierList.isEmpty();
        int fromCount = 0;
        for (BLangNode clause : queryExpr.getQueryClauses()) {
            if (clause.getKind() == NodeKind.FROM) {
                fromCount++;
                BLangFromClause fromClause = (BLangFromClause) clause;
                BLangExpression collection = (BLangExpression) fromClause.getCollection();
                if (fromCount > 1) {
                    if (TypeTags.STREAM == collection.type.tag) {
                        this.dlog.error(collection.pos, DiagnosticCode.NOT_ALLOWED_STREAM_USAGE_WITH_FROM);
                    }
                }
            }
            analyzeNode(clause, env);
        }
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        int fromCount = 0;
        for (BLangNode clause : queryAction.getQueryClauses()) {
            if (clause.getKind() == NodeKind.FROM) {
                fromCount++;
                BLangFromClause fromClause = (BLangFromClause) clause;
                BLangExpression collection = (BLangExpression) fromClause.getCollection();
                if (fromCount > 1) {
                    if (TypeTags.STREAM == collection.type.tag) {
                        this.dlog.error(collection.pos, DiagnosticCode.NOT_ALLOWED_STREAM_USAGE_WITH_FROM);
                    }
                }
            }
            analyzeNode(clause, env);
        }
        validateActionParentNode(queryAction.pos, queryAction);
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        analyzeExpr(fromClause.collection);
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        analyzeExpr(joinClause.collection);
        if (joinClause.onClause != null) {
            analyzeNode((BLangNode) joinClause.onClause, env);
        }
    }

    @Override
    public void visit(BLangLetClause letClause) {
        for (BLangLetVariable letVariable : letClause.letVarDeclarations) {
            analyzeNode((BLangNode) letVariable.definitionNode, env);
        }
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        analyzeExpr(whereClause.expression);
    }

    @Override
    public void visit(BLangOnClause onClause) {
        analyzeExpr(onClause.lhsExpr);
        analyzeExpr(onClause.rhsExpr);
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        orderByClause.orderByKeyList.forEach(value -> analyzeExpr((BLangExpression) value.getOrderKey()));
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        analyzeExpr(selectClause.expression);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        analyzeExpr(onConflictClause.expression);
        if (!queryToTableWithKey) {
            dlog.error(onConflictClause.pos, DiagnosticCode.ON_CONFLICT_ONLY_WORKS_WITH_TABLES_WITH_KEY_SPECIFIER);
        }
    }

    @Override
    public void visit(BLangDoClause doClause) {
        analyzeNode(doClause.body, env);
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        boolean currentFailVisited = this.failVisited;
        this.failVisited = false;
        this.resetLastStatement();
        this.resetErrorThrown();
        this.returnWithinLambdaWrappingCheckStack.push(false);
        BLangVariable onFailVarNode = (BLangVariable) onFailClause.variableDefinitionNode.getVariable();
        for (BType errorType : errorTypes.peek()) {
            if (!types.isAssignable(errorType, onFailVarNode.type)) {
                dlog.error(onFailVarNode.pos, DiagnosticCode.INCOMPATIBLE_ON_FAIL_ERROR_DEFINITION, errorType,
                        onFailVarNode.type);
            }
        }
        analyzeNode(onFailClause.body, env);
        onFailClause.bodyContainsFail = this.failVisited;
        onFailClause.statementBlockReturns = this.returnWithinLambdaWrappingCheckStack.peek();
        this.returnWithinLambdaWrappingCheckStack.pop();
        this.resetErrorThrown();
        this.failVisited = currentFailVisited;
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        analyzeExpr(limitClause.expression);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        analyzeNode(typeTestExpr.expr, env);
        if (typeTestExpr.typeNode.type == symTable.semanticError || typeTestExpr.expr.type == symTable.semanticError) {
            return;
        }

        // Check whether the condition is always true. If the variable type is assignable to target type,
        // then type check will always evaluate to true.
        if (types.isAssignable(typeTestExpr.expr.type, typeTestExpr.typeNode.type)) {
            dlog.error(typeTestExpr.pos, DiagnosticCode.UNNECESSARY_CONDITION);
            return;
        }

        // Check whether the target type can ever be present as the type of the source.
        // It'll be only possible iff, the target type has been assigned to the source
        // variable at some point. To do that, a value of target type should be assignable
        // to the type of the source variable.
        if (!types.isAssignable(typeTestExpr.typeNode.type, typeTestExpr.expr.type) &&
                !indirectIntersectionExists(typeTestExpr.expr, typeTestExpr.typeNode.type)) {
            dlog.error(typeTestExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPE_CHECK, typeTestExpr.expr.type,
                       typeTestExpr.typeNode.type);
        }
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        analyzeExpr(annotAccessExpr.expr);
        BAnnotationSymbol annotationSymbol = annotAccessExpr.annotationSymbol;
        if (annotationSymbol != null && Symbols.isFlagOn(annotationSymbol.flags, Flags.DEPRECATED)) {
            dlog.warning(annotAccessExpr.pos, DiagnosticCode.USAGE_OF_DEPRECATED_CONSTRUCT, annotationSymbol);
        }
    }

    private boolean indirectIntersectionExists(BLangExpression expression, BType testType) {
        BType expressionType = expression.type;
        switch (expressionType.tag) {
            case TypeTags.UNION:
                if (types.getTypeForUnionTypeMembersAssignableToType((BUnionType) expressionType, testType) !=
                        symTable.semanticError) {
                    return true;
                }
                break;
            case TypeTags.FINITE:
                if (types.getTypeForFiniteTypeValuesAssignableToType((BFiniteType) expressionType, testType) !=
                        symTable.semanticError) {
                    return true;
                }
        }

        switch (testType.tag) {
            case TypeTags.UNION:
                return types.getTypeForUnionTypeMembersAssignableToType((BUnionType) testType, expressionType) !=
                        symTable.semanticError;
            case TypeTags.FINITE:
                return types.getTypeForFiniteTypeValuesAssignableToType((BFiniteType) testType, expressionType) !=
                        symTable.semanticError;
        }
        return false;
    }

    // private methods

    private <E extends BLangExpression> void analyzeExpr(E node) {
        if (node == null) {
            return;
        }
        BLangNode myParent = parent;
        node.parent = parent;
        parent = node;
        node.accept(this);
        this.isJSONContext = false;
        parent = myParent;
        checkAccess(node);
    }

    private <E extends BLangExpression> void analyzeExpr(E node, SymbolEnv env) {
        if (node == null) {
            return;
        }
        SymbolEnv prevEnv = this.env;
        this.env = env;
        BLangNode myParent = parent;
        node.parent = parent;
        parent = node;
        node.accept(this);
        this.isJSONContext = false;
        parent = myParent;
        checkAccess(node);
        this.env = prevEnv;
    }

    @Override
    public void visit(BLangConstant constant) {

        analyzeTypeNode(constant.typeNode, env);
        analyzeNode(constant.expr, env);
        analyzeExportableTypeRef(constant.symbol, constant.symbol.type.tsymbol, false, constant.pos);
        constant.annAttachments.forEach(annotationAttachment -> analyzeNode(annotationAttachment, env));
    }

    /**
     * This method checks for private symbols being accessed or used outside of package and|or private symbols being
     * used in public fields of objects/records and will fail those occurrences.
     *
     * @param node expression node to analyze
     */
    private <E extends BLangExpression> void checkAccess(E node) {
        if (node.type != null) {
            checkAccessSymbol(node.type.tsymbol, node.pos);
        }

        //check for object new invocation
        if (node.getKind() == NodeKind.INVOCATION) {
            BLangInvocation bLangInvocation = (BLangInvocation) node;
            checkAccessSymbol(bLangInvocation.symbol, bLangInvocation.pos);
        }
    }

    private void checkAccessSymbol(BSymbol symbol, DiagnosticPos position) {
        if (symbol == null) {
            return;
        }

        if (env.enclPkg.symbol.pkgID != symbol.pkgID && !Symbols.isPublic(symbol)) {
            dlog.error(position, DiagnosticCode.ATTEMPT_REFER_NON_ACCESSIBLE_SYMBOL, symbol.name);
        }
    }

    private <E extends BLangExpression> void analyzeExprs(List<E> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            analyzeExpr(nodeList.get(i));
        }
    }

    private void initNewWorkerActionSystem() {
        this.workerActionSystemStack.push(new WorkerActionSystem());
    }

    private void finalizeCurrentWorkerActionSystem() {
        WorkerActionSystem was = this.workerActionSystemStack.pop();
        if (!was.hasErrors) {
            this.validateWorkerInteractions(was);
        }
    }

    private static boolean isWorkerSend(BLangNode action) {
        return action.getKind() == NodeKind.WORKER_SEND;
    }

    private static boolean isWorkerSyncSend(BLangNode action) {
        return action.getKind() == NodeKind.WORKER_SYNC_SEND;
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

    private void validateWorkerInteractions(WorkerActionSystem workerActionSystem) {
        BLangNode currentAction;
        boolean systemRunning;
        do {
            systemRunning = false;
            for (WorkerActionStateMachine worker : workerActionSystem.finshedWorkers) {
                if (worker.done()) {
                    continue;
                }
                currentAction = worker.currentAction();
                if (!isWorkerSend(currentAction) && !isWorkerSyncSend(currentAction)) {
                    continue;
                }
                WorkerActionStateMachine otherSM = workerActionSystem.find(this.extractWorkerId(currentAction));
                if (otherSM == null || !otherSM.currentIsReceive(worker.workerId)) {
                    continue;
                }
                BLangWorkerReceive receive = (BLangWorkerReceive) otherSM.currentAction();
                if (isWorkerSyncSend(currentAction)) {
                    this.validateWorkerActionParameters((BLangWorkerSyncSendExpr) currentAction, receive);
                } else {
                    this.validateWorkerActionParameters((BLangWorkerSend) currentAction, receive);
                }
                otherSM.next();
                worker.next();

                systemRunning = true;
                String channelName = generateChannelName(worker.workerId, otherSM.workerId);
                otherSM.node.sendsToThis.add(channelName);

                worker.node.sendsToThis.add(channelName);
            }
        } while (systemRunning);
        if (!workerActionSystem.everyoneDone()) {
            this.reportInvalidWorkerInteractionDiagnostics(workerActionSystem);
        }
    }

    private void reportInvalidWorkerInteractionDiagnostics(WorkerActionSystem workerActionSystem) {
        this.dlog.error(workerActionSystem.getRootPosition(), DiagnosticCode.INVALID_WORKER_INTERACTION,
                workerActionSystem.toString());
    }

    private void validateWorkerActionParameters(BLangWorkerSend send, BLangWorkerReceive receive) {
        types.checkType(receive, send.type, receive.type);
        addImplicitCast(send.type, receive);
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
                variable.type = variable.symbol.type = send.expectedType = receive.matchingSendsError;
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
            dlog.error(send.pos, DiagnosticCode.ASSIGNMENT_REQUIRED);
        } else {
            types.checkType(send.pos, receive.matchingSendsError, send.expectedType, DiagnosticCode.INCOMPATIBLE_TYPES);
        }

        types.checkType(receive, send.type, receive.type);

        addImplicitCast(send.type, receive);
        NodeKind kind = receive.parent.getKind();
        if (kind == NodeKind.TRAP_EXPR || kind == NodeKind.CHECK_EXPR || kind == NodeKind.CHECK_PANIC_EXPR) {
            typeChecker.checkExpr((BLangExpression) receive.parent, receive.env);
        }
        receive.sendExpression = send;
    }

    private void addImplicitCast(BType actualType, BLangWorkerReceive receive) {
        if (receive.type != null && receive.type != symTable.semanticError) {
            types.setImplicitCastExpr(receive, actualType, receive.type);
            receive.type = actualType;
        }
    }

    private boolean checkNextBreakValidityInTransaction() {
        return !this.loopWithinTransactionCheckStack.peek() && transactionCount > 0 && withinTransactionScope;
    }

    private boolean checkReturnValidityInTransaction() {
        return (this.returnWithinTransactionCheckStack.empty() || !this.returnWithinTransactionCheckStack.peek())
                && transactionCount > 0 && withinTransactionScope;
    }

    private void validateMainFunction(BLangFunction funcNode) {
        if (!MAIN_FUNCTION_NAME.equals(funcNode.name.value)) {
            return;
        }

        if (!Symbols.isPublic(funcNode.symbol)) {
            this.dlog.error(funcNode.pos, DiagnosticCode.MAIN_SHOULD_BE_PUBLIC);
        }

        funcNode.requiredParams.forEach(param -> {
            if (!param.type.isAnydata()) {
                this.dlog.error(param.pos, DiagnosticCode.MAIN_PARAMS_SHOULD_BE_ANYDATA, param.type);
            }
        });

        if (funcNode.restParam != null && !funcNode.restParam.type.isAnydata()) {
            this.dlog.error(funcNode.restParam.pos, DiagnosticCode.MAIN_PARAMS_SHOULD_BE_ANYDATA,
                            funcNode.restParam.type);
        }

        types.validateErrorOrNilReturn(funcNode, DiagnosticCode.MAIN_RETURN_SHOULD_BE_ERROR_OR_NIL);
    }

    private void validateModuleInitFunction(BLangFunction funcNode) {
        if (funcNode.attachedFunction || !Names.USER_DEFINED_INIT_SUFFIX.value.equals(funcNode.name.value)) {
            return;
        }

        if (Symbols.isPublic(funcNode.symbol)) {
            this.dlog.error(funcNode.pos, DiagnosticCode.MODULE_INIT_CANNOT_BE_PUBLIC);
        }

        if (!funcNode.requiredParams.isEmpty() || funcNode.restParam != null) {
            this.dlog.error(funcNode.pos, DiagnosticCode.MODULE_INIT_CANNOT_HAVE_PARAMS);
        }

        types.validateErrorOrNilReturn(funcNode, DiagnosticCode.MODULE_INIT_RETURN_SHOULD_BE_ERROR_OR_NIL);
    }

    private boolean getIsJSONContext(BType... arg) {
        if (this.isJSONContext) {
            return true;
        }
        for (BType type : arg) {
            if (types.isJSONContext(type)) {
                return true;
            }
        }
        return false;
    }

    private BType getErrorTypes(BType bType) {
        BType errorType = symTable.semanticError;

        int tag = bType.tag;
        if (tag == TypeTags.ERROR) {
            errorType = bType;
        } else if (tag == TypeTags.UNION) {
            LinkedHashSet<BType> errTypes = new LinkedHashSet<>();
            Set<BType> memTypes = ((BUnionType) bType).getMemberTypes();
            for (BType memType : memTypes) {
                if (memType.tag == TypeTags.ERROR) {
                    errTypes.add(memType);
                }
            }

            errorType = errTypes.size() == 1 ? errTypes.iterator().next() : BUnionType.create(null, errTypes);
        }

        return errorType;
    }

    /**
     * This class contains the state machines for a set of workers.
     */
    private static class WorkerActionSystem {

        public List<WorkerActionStateMachine> finshedWorkers = new ArrayList<>();
        private Stack<WorkerActionStateMachine> workerActionStateMachines = new Stack<>();
        private boolean hasErrors = false;


        public void startWorkerActionStateMachine(String workerId, DiagnosticPos pos, BLangFunction node) {
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

        public DiagnosticPos getRootPosition() {
            return this.finshedWorkers.iterator().next().pos;
        }

        @Override
        public String toString() {
            return this.finshedWorkers.toString();
        }

        public String currentWorkerId() {
            return workerActionStateMachines.peek().workerId;
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

        public DiagnosticPos pos;
        public String workerId;
        public BLangFunction node;

        public WorkerActionStateMachine(DiagnosticPos pos, String workerId, BLangFunction node) {
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
            return !isWorkerSend(action) && !isWorkerSyncSend(action) &&
                    ((BLangWorkerReceive) action).workerIdentifier.value.equals(sourceWorkerId);
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
                } else {
                    return ((BLangWorkerReceive) action).toActionString();
                }
            }
        }
    }

    private void checkExperimentalFeatureValidity(ExperimentalFeatures constructName, DiagnosticPos pos) {

        if (enableExperimentalFeatures) {
            return;
        }

        dlog.error(pos, DiagnosticCode.INVALID_USE_OF_EXPERIMENTAL_FEATURE, constructName.value);
    }

    public static String generateChannelName(String source, String target) {

        return source + "->" + target;
    }

    /**
     * Experimental feature list for JBallerina 1.0.0.
     *
     * @since JBallerina 1.0.0
     */
    private enum ExperimentalFeatures {
        LOCK("lock"),
        XML_ACCESS("xml access expression"),
        XML_ATTRIBUTES_ACCESS("xml attribute expression"),
        ;
        private String value;

        private ExperimentalFeatures(String value) {

            this.value = value;
        }

        @Override
        public String toString() {

            return value;
        }
    }

}

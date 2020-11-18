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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
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
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
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
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMatchPattern;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
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
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Responsible for performing isolation analysis.
 *
 * @since Swan Lake
 */
public class IsolationAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<IsolationAnalyzer> ISOLATION_ANALYZER_KEY = new CompilerContext.Key<>();
    private static final String VALUE_LANG_LIB = "lang.value";
    private static final String CLONE_LANG_LIB_METHOD = "clone";
    private static final String CLONE_READONLY_LANG_LIB_METHOD = "cloneReadOnly";

    private SymbolEnv env;
    private SymbolTable symTable;
    private SymbolResolver symResolver;
    private Names names;
    private Types types;
    private BLangDiagnosticLog dlog;

    private boolean inferredIsolated = true;
    private boolean inLockStatement = false;
    private Map<BSymbol, UniqueInitAndReferenceInfo> uniqueInitAndReferenceInfo = new HashMap<>();
    private Stack<PotentiallyInvalidExpressionInfo> copyInLockInfoStack = new Stack<>();

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
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        analyzeNode(pkgNode, pkgEnv);
        return pkgNode;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.ISOLATION_ANALYZE)) {
            return;
        }

        for (BLangTypeDefinition typeDefinition : pkgNode.typeDefinitions) {
            analyzeNode(typeDefinition.typeNode, env);
        }

        for (BLangClassDefinition classDefinition : pkgNode.classDefinitions) {
            if (classDefinition.flagSet.contains(Flag.ANONYMOUS) && isIsolated(classDefinition.type.flags)) {
                // If this is a class definition for an object constructor expression, and the type is `isolated`,
                // that is due to the expected type being an `isolated` object. We now mark the class definition also
                // as `isolated`, to enforce the isolation validation.
                classDefinition.flagSet.add(Flag.ISOLATED);
                classDefinition.symbol.flags |= Flags.ISOLATED;
            }

            analyzeNode(classDefinition, env);
        }

        for (BLangFunction function : pkgNode.functions) {
            analyzeNode(function, env);
        }

        for (BLangSimpleVariable globalVar : pkgNode.globalVars) {
            analyzeNode(globalVar, env);
        }

        for (BLangTestablePackage testablePkg : pkgNode.testablePkgs) {
            analyze(testablePkg);
        }

        pkgNode.completedPhases.add(CompilerPhase.ISOLATION_ANALYZE);

        for (UniqueInitAndReferenceInfo varInfo : uniqueInitAndReferenceInfo.values()) {
            int totalRefCount = varInfo.totalRefCount;
            List<BLangSimpleVarRef> refsOfVarExpectedToBeUnique = varInfo.refsOfVarExpectedToBeUnique;

            if (totalRefCount == 0 || refsOfVarExpectedToBeUnique.isEmpty()) {
                continue;
            }

            if (!varInfo.uniqueInitExpr) {
                for (BLangSimpleVarRef varRef : refsOfVarExpectedToBeUnique) {
                    dlog.error(varRef.pos,
                               DiagnosticCode.INVALID_NON_UNIQUE_EXPRESSION_AS_INITIAL_VALUE_IN_ISOLATED_OBJECT);
                }
                continue;
            }

            boolean nonUniqueInitExprDueToVarRefs = false;

            if (totalRefCount == 1) {
                for (BLangSimpleVarRef varRef : varInfo.refsUsedInInitExpr) {
                    UniqueInitAndReferenceInfo varRefInfo = uniqueInitAndReferenceInfo.get(varRef.symbol);

                    if (varRefInfo.totalRefCount > 1) {
                        nonUniqueInitExprDueToVarRefs = true;
                        break;
                    }
                }

                if (!nonUniqueInitExprDueToVarRefs) {
                    continue;
                }
            }

            for (BLangSimpleVarRef varRef : refsOfVarExpectedToBeUnique) {
                dlog.error(varRef.pos,
                           DiagnosticCode.INVALID_NON_UNIQUE_EXPRESSION_AS_INITIAL_VALUE_IN_ISOLATED_OBJECT);
            }
        }
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
    public void visit(BLangFunction funcNode) {
        boolean prevInferredIsolated = this.inferredIsolated;
        this.inferredIsolated = true;

        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);

        for (BLangSimpleVariable requiredParam : funcNode.requiredParams) {
            if (!requiredParam.symbol.defaultableParam) {
                continue;
            }

            analyzeNode(requiredParam.expr, funcEnv);
        }

        analyzeNode(funcNode.body, funcEnv);

        if (isBallerinaModule(env.enclPkg) && !isIsolated(funcNode.symbol.flags) &&
                this.inferredIsolated && !Symbols.isFlagOn(funcNode.symbol.flags, Flags.WORKER)) {
            dlog.warning(funcNode.pos, DiagnosticCode.FUNCTION_CAN_BE_MARKED_ISOLATED, funcNode.name);
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
                (typeNode.type == null || typeNode.type.tsymbol == null ||
                         typeNode.type.tsymbol.owner.getKind() != SymbolKind.PACKAGE)) {
            // Only analyze the type node if it is not available at module level, since module level type definitions
            // have already been analyzed.
            analyzeNode(typeNode, env);
        }

        BVarSymbol symbol = varNode.symbol;
        int flags = symbol.flags;

        BLangExpression expr = varNode.expr;

        BType fieldType = varNode.type;
        boolean isolatedClassField = isIsolatedClassField();

        if (isolatedClassField && isExpectedToBeAPrivateField(symbol, fieldType) &&
                !Symbols.isFlagOn(flags, Flags.PRIVATE)) {
            dlog.error(varNode.pos, DiagnosticCode.INVALID_NON_PRIVATE_MUTABLE_FIELD_IN_ISOLATED_OBJECT);
        }

        if (expr == null) {
            return;
        }

        if (isolatedClassField) {
            validateIsolatedObjectFieldInitialValue(fieldType, expr);
        }

        analyzeNode(expr, env);

        UniqueInitAndReferenceInfo varInfo = this.uniqueInitAndReferenceInfo.get(symbol);

        ArrayList<BLangSimpleVarRef> refsUsedInInitExpr = new ArrayList<>();
        boolean uniqueInitExpr = isReferenceOrIsolatedExpression(expr, refsUsedInInitExpr, false);
        if (varInfo == null) {
            uniqueInitAndReferenceInfo.put(symbol, new UniqueInitAndReferenceInfo(uniqueInitExpr, refsUsedInInitExpr));
        } else {
            varInfo.uniqueInitExpr = uniqueInitExpr;
            varInfo.refsUsedInInitExpr = refsUsedInInitExpr;
        }

        if (Symbols.isFlagOn(flags, Flags.WORKER)) {
            inferredIsolated = false;

            if (isInIsolatedFunction(env.enclInvokable)) {
                dlog.error(varNode.pos, DiagnosticCode.INVALID_WORKER_DECLARATION_IN_ISOLATED_FUNCTION);
            }
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
                    isIsolatedObjectFieldAccessViaSelf(fieldAccess, false)) {
                validateIsolatedObjectFieldInitialValue(
                        ((BObjectType) enclInvokable.symbol.owner.type).fields.get(fieldAccess.field.value).type, expr);
            }
        }
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
        analyzeNode(returnNode.expr, env);
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
    public void visit(BLangMatch matchNode) {
        analyzeNode(matchNode.expr, env);
        for (BLangMatch.BLangMatchBindingPatternClause patternClause : matchNode.patternClauses) {
            analyzeNode(patternClause, env);
        }
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        analyzeNode(patternClauseNode.variable, env);
        analyzeNode(patternClauseNode.body, env);
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
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern) {
        analyzeNode(varBindingPattern.getBindingPattern(), env);
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern) {
    }

    @Override
    public void visit(BLangListBindingPattern listBindingPattern) {
        for (BLangBindingPattern bindingPattern : listBindingPattern.bindingPatterns) {
            analyzeNode(bindingPattern, env);
        }
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
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        analyzeNode((BLangNode) fromClause.getVariableDefinitionNode(), env);
        analyzeNode(fromClause.collection, env);
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        analyzeNode((BLangNode) joinClause.getVariableDefinitionNode(), env);
        analyzeNode(joinClause.collection, env);
    }

    @Override
    public void visit(BLangLetClause letClause) {
        for (BLangLetVariable letVarDeclaration : letClause.letVarDeclarations) {
            analyzeNode((BLangNode) letVarDeclaration.definitionNode, env);
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
        for (OrderKeyNode orderKeyNode : orderByClause.orderByKeyList) {
            analyzeNode((BLangExpression) orderKeyNode.getOrderKey(), env);
        }
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        analyzeNode(selectClause.expression, env);
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        analyzeNode(whereClause.expression, env);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        analyzeNode(doClause.body, env);
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
    }

    @Override
    public void visit(BLangLock lockNode) {
        boolean prevInLockStatement = this.inLockStatement;
        this.inLockStatement = true;
        copyInLockInfoStack.push(new PotentiallyInvalidExpressionInfo(env.enclInvokable));

        analyzeNode(lockNode.body, SymbolEnv.createLockEnv(lockNode, env));

        PotentiallyInvalidExpressionInfo copyInInLockInfo = copyInLockInfoStack.pop();

        this.inLockStatement = prevInLockStatement;

        if (copyInInLockInfo.hasMutableAccessInLock) {
            for (BLangSimpleVarRef varRef : copyInInLockInfo.copyInVarRefs) {
                dlog.error(varRef.pos, DiagnosticCode.INVALID_COPY_IN_OF_MUTABLE_VALUE_INTO_ISOLATED_OBJECT);
            }

            for (BLangSimpleVarRef varRef : copyInInLockInfo.copyOutVarRefs) {
                dlog.error(varRef.pos, DiagnosticCode.INVALID_COPY_OUT_OF_MUTABLE_VALUE_FROM_ISOLATED_OBJECT);
            }

            for (BLangInvocation invocation : copyInInLockInfo.invocations) {
                dlog.error(invocation.pos, DiagnosticCode.INVALID_NON_ISOLATED_INVOCATION_IN_ISOLATED_OBJECT_METHOD);
            }

            return;
        }

        for (int i = copyInLockInfoStack.size() - 1; i >= 0; i--) {
            PotentiallyInvalidExpressionInfo prevCopyInLockInfo = copyInLockInfoStack.get(i);

            BLangInvokableNode enclInvokableNode = prevCopyInLockInfo.enclInvokableNode;
            if (enclInvokableNode.getKind() != NodeKind.FUNCTION) {
                return;
            }

            BLangFunction function = (BLangFunction) enclInvokableNode;
            if (!function.attachedFunction || function.objInitFunction) {
                return;
            }

            prevCopyInLockInfo.copyInVarRefs.addAll(copyInInLockInfo.copyInVarRefs);
            prevCopyInLockInfo.copyOutVarRefs.addAll(copyInInLockInfo.copyOutVarRefs);
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        analyzeNode(transactionNode.transactionBody, env);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        analyzeNode(stmt.varRef, env);
        analyzeNode(stmt.expr, env);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        analyzeNode(stmt.varRef, env);
        analyzeNode(stmt.expr, env);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        analyzeNode(stmt.varRef, env);
        analyzeNode(stmt.expr, env);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        inferredIsolated = false;

        if (isInIsolatedFunction(env.enclInvokable)) {
            dlog.error(forkJoin.pos, DiagnosticCode.INVALID_FORK_STATEMENT_IN_ISOLATED_FUNCTION);
        }
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
        BType accessType = varRefExpr.type;

        BSymbol symbol = varRefExpr.symbol;
        BLangInvokableNode enclInvokable = env.enclInvokable;
        BLangType enclType = env.enclType;

        if (symbol == null) {
            return;
        }

        UniqueInitAndReferenceInfo varInfo = uniqueInitAndReferenceInfo.get(symbol);

        if (varInfo == null) {
            uniqueInitAndReferenceInfo.put(symbol, new UniqueInitAndReferenceInfo());
        } else {
            varInfo.totalRefCount++;
        }

        if (isInIsolatedObjectMethod(env, true)) {
            if (inLockStatement) {
                if ((!varRefExpr.lhsVar || varRefExpr.parent.getKind() != NodeKind.ASSIGNMENT)  &&
                        !Names.SELF.value.equals(varRefExpr.variableName.value) && isInvalidCopyIn(varRefExpr, env)) {
                    copyInLockInfoStack.peek().copyInVarRefs.add(varRefExpr);
                } else if (!varRefExpr.lhsVar &&
                        isInvalidCopyingOfMutableValueInIsolatedObject(varRefExpr, true)) {
                    copyInLockInfoStack.peek().copyOutVarRefs.add(varRefExpr);
                }
            } else if (Names.SELF.value.equals(varRefExpr.variableName.value) &&
                    varRefExpr.parent.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR) {
                dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_MUTABLE_FIELD_ACCESS_IN_ISOLATED_OBJECT_OUTSIDE_LOCK);
            }
        }

        boolean inIsolatedFunction = isInIsolatedFunction(enclInvokable);
        boolean recordFieldDefaultValue = isRecordFieldDefaultValue(enclType);
        boolean objectFieldDefaultValue = !recordFieldDefaultValue && isObjectFieldDefaultValueRequiringIsolation(env);

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

        if (!recordFieldDefaultValue && !objectFieldDefaultValue && enclInvokable != null &&
                symbol.owner == enclInvokable.symbol) {
            return;
        }

        if (Symbols.isFlagOn(symbol.flags, Flags.CONSTANT)) {
            return;
        }

        if (Symbols.isFlagOn(symbol.flags, Flags.FINAL) &&
                (types.isInherentlyImmutableType(accessType) ||
                         Symbols.isFlagOn(accessType.flags, Flags.READONLY) ||
                         isIsolatedObjectTypes(accessType))) {
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

        inferredIsolated = false;

        if (inIsolatedFunction) {
            dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_MUTABLE_ACCESS_IN_ISOLATED_FUNCTION);
            return;
        }

        if (recordFieldDefaultValue) {
            if (isBallerinaModule(env.enclPkg)) {
                // TODO: 9/13/20 remove this error once stdlibs are migrated
                dlog.warning(varRefExpr.pos, DiagnosticCode.WARNING_INVALID_MUTABLE_ACCESS_AS_RECORD_DEFAULT);
            } else {
                dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_MUTABLE_ACCESS_AS_RECORD_DEFAULT);
            }
        }

        if (objectFieldDefaultValue) {
            dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_MUTABLE_ACCESS_AS_OBJECT_DEFAULT);
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        analyzeNode(fieldAccessExpr.expr, env);

        if (!isValidIsolatedObjectFieldAccessViaSelfOutsideLock(fieldAccessExpr, true)) {
            return;
        }

        if (inLockStatement) {
            copyInLockInfoStack.peek().hasMutableAccessInLock = true;
            return;
        }

        dlog.error(fieldAccessExpr.pos, DiagnosticCode.INVALID_MUTABLE_FIELD_ACCESS_IN_ISOLATED_OBJECT_OUTSIDE_LOCK);
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
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        if (!actionInvocationExpr.async) {
            analyzeInvocation(actionInvocationExpr);
            return;
        }

        inferredIsolated = false;

        if (actionInvocationExpr.functionPointerInvocation) {
            return;
        }

        if (isInIsolatedFunction(env.enclInvokable)) {
            dlog.error(actionInvocationExpr.pos, DiagnosticCode.INVALID_ASYNC_INVOCATION_IN_ISOLATED_FUNCTION);
        }
    }

    @Override
    public void visit(BLangTypeInit typeInitExpr) {
        BSymbol initInvocationSymbol = typeInitExpr.initInvocation.symbol;
        if (initInvocationSymbol != null && !isIsolated(initInvocationSymbol.flags)) {
            inferredIsolated = false;

            if (isInIsolatedFunction(env.enclInvokable)) {
                dlog.error(typeInitExpr.pos, DiagnosticCode.INVALID_NON_ISOLATED_INIT_EXPRESSION_IN_ISOLATED_FUNCTION);
            } else if (isRecordFieldDefaultValue(env.enclType)) {
                if (isBallerinaModule(env.enclPkg)) {
                    // TODO: 9/16/20 remove this once stdlibs are migrated
                    dlog.warning(typeInitExpr.pos,
                                 DiagnosticCode.WARNING_INVALID_NON_ISOLATED_INIT_EXPRESSION_AS_RECORD_DEFAULT);
                } else {
                    dlog.error(typeInitExpr.pos,
                               DiagnosticCode.INVALID_NON_ISOLATED_INIT_EXPRESSION_AS_RECORD_DEFAULT);
                }

            } else if (isObjectFieldDefaultValueRequiringIsolation(env)) {
                dlog.error(typeInitExpr.pos, DiagnosticCode.INVALID_NON_ISOLATED_INIT_EXPRESSION_AS_OBJECT_DEFAULT);
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
        for (BLangLetVariable letVarDeclaration : letExpr.letVarDeclarations) {
            analyzeNode((BLangNode) letVarDeclaration.definitionNode, env);
        }

        analyzeNode(letExpr.expr, env);
    }

    @Override
    public void visit(BLangLetVariable letVariable) {
        analyzeNode((BLangNode) letVariable.definitionNode.getVariable(), env);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        for (BLangExpression expr : listConstructorExpr.exprs) {
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
        // TODO: 8/21/20 add analysis
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        SymbolEnv arrowFunctionEnv = SymbolEnv.createArrowFunctionSymbolEnv(bLangArrowFunction, env);
        analyzeNode(bLangArrowFunction.body, arrowFunctionEnv);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        analyzeNode(intRangeExpression.startExpr, env);
        analyzeNode(intRangeExpression.endExpr, env);
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
    public void visit(BLangTableMultiKeyExpr tableMultiKeyExpr) {
        for (BLangExpression value : tableMultiKeyExpr.multiKeyIndexExprs) {
            analyzeNode(value, env);
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

        for (BLangSimpleVariable referencedField : objectTypeNode.referencedFields) {
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
    public void visit(BLangRecordTypeNode recordTypeNode) {
        SymbolEnv typeEnv = SymbolEnv.createTypeEnv(recordTypeNode, recordTypeNode.symbol.scope, env);

        for (BLangSimpleVariable field : recordTypeNode.fields) {
            analyzeNode(field, typeEnv);
        }

        for (BLangSimpleVariable referencedField : recordTypeNode.referencedFields) {
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
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause matchStaticBindingPatternClause) {
        analyzeNode(matchStaticBindingPatternClause.body, env);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause matchStmtStructuredBindingPatternClause) {
        analyzeNode(matchStmtStructuredBindingPatternClause.bindingPatternVariable, env);

        BLangExpression typeGuardExpr = matchStmtStructuredBindingPatternClause.typeGuardExpr;
        if (typeGuardExpr != null) {
            analyzeNode(typeGuardExpr, env);
        }

        analyzeNode(matchStmtStructuredBindingPatternClause.body, env);
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

    private void analyzeInvocation(BLangInvocation invocationExpr) {
        List<BLangExpression> requiredArgs = invocationExpr.requiredArgs;
        List<BLangExpression> restArgs = invocationExpr.restArgs;

        BLangExpression expr = invocationExpr.expr;
        if (expr != null && (requiredArgs.isEmpty() || requiredArgs.get(0) != expr)) {
            analyzeNode(expr, env);
        }

        BInvokableSymbol symbol = (BInvokableSymbol) invocationExpr.symbol;
        if (symbol == null || symbol.getKind() == SymbolKind.ERROR_CONSTRUCTOR) {
            analyzeArgs(requiredArgs, restArgs);
            return;
        }

        boolean inIsolatedFunction = isInIsolatedFunction(env.enclInvokable);
        boolean recordFieldDefaultValue = isRecordFieldDefaultValue(env.enclType);
        boolean objectFieldDefaultValueRequiringIsolation = isObjectFieldDefaultValueRequiringIsolation(env);

        boolean expectsIsolation =
                inIsolatedFunction || recordFieldDefaultValue || objectFieldDefaultValueRequiringIsolation;

        if (isIsolated(symbol.flags)) {
            if (!expectsIsolation) {
                analyzeArgs(requiredArgs, restArgs);
                return;
            }

            analyzeArgIsolatedness(requiredArgs, restArgs, symbol);
            return;
        }

        analyzeArgs(requiredArgs, restArgs);

        if (inLockStatement && isInIsolatedObjectMethod(env, true)) {
            copyInLockInfoStack.peek().invocations.add(invocationExpr);
        }

        inferredIsolated = false;

        if (inIsolatedFunction) {
            dlog.error(invocationExpr.pos, DiagnosticCode.INVALID_NON_ISOLATED_INVOCATION_IN_ISOLATED_FUNCTION);
            return;
        }

        if (recordFieldDefaultValue) {
            if (isBallerinaModule(env.enclPkg)) {
                // TODO: 9/13/20 remove this once stdlibs are migrated
                dlog.warning(invocationExpr.pos,
                             DiagnosticCode.WARNING_INVALID_NON_ISOLATED_INVOCATION_AS_RECORD_DEFAULT);
            } else {
                dlog.error(invocationExpr.pos, DiagnosticCode.INVALID_NON_ISOLATED_INVOCATION_AS_RECORD_DEFAULT);
            }
        }

        if (objectFieldDefaultValueRequiringIsolation) {
            dlog.error(invocationExpr.pos, DiagnosticCode.INVALID_NON_ISOLATED_INVOCATION_AS_OBJECT_DEFAULT);
        }
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
            BInvokableType invokableType = (BInvokableType) argExpr.type;
            BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) invokableType.tsymbol;

            BInvokableTypeSymbol dupInvokableTypeSymbol = new BInvokableTypeSymbol(tsymbol.tag,
                                                                                   tsymbol.flags | Flags.ISOLATED,
                                                                                   tsymbol.pkgID, null, tsymbol.owner,
                                                                                   tsymbol.pos, tsymbol.origin);
            BInvokableType dupInvokableType = new BInvokableType(invokableType.paramTypes, invokableType.restType,
                                                                 invokableType.retType, dupInvokableTypeSymbol);
            dupInvokableType.flags |= Flags.ISOLATED;
            dupInvokableTypeSymbol.type = dupInvokableType;
            argExpr.type = dupInvokableType;

            if (namedArg) {
                arg.type = dupInvokableType;
            }
        }
        this.inferredIsolated = prevInferredIsolatedness && this.inferredIsolated;
    }

    private void analyzeArgIsolatedness(List<BLangExpression> requiredArgs, List<BLangExpression> restArgs,
                                        BInvokableSymbol symbol) {
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

                    if (!Symbols.isFlagOn(arg.type.flags, Flags.ISOLATED)) {
                        dlog.error(arg.pos, DiagnosticCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
                    }

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

                    if (!Symbols.isFlagOn(arg.type.flags, Flags.ISOLATED)) {
                        dlog.error(arg.pos, DiagnosticCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
                    }
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

            if (arg.type == symTable.semanticError) {
                continue;
            }

            analyzeAndSetArrowFuncFlagForIsolatedParamArg(arg);

            if (!Symbols.isFlagOn(arg.type.flags, Flags.ISOLATED)) {
                dlog.error(arg.pos, DiagnosticCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
            }
        }

        if (restArgs.get(restArgs.size() - 1).getKind() == NodeKind.REST_ARGS_EXPR) {
            BLangRestArgsExpression varArg = (BLangRestArgsExpression) restArgs.get(restArgs.size() - 1);
            BType varArgType = varArg.type;
            DiagnosticPos varArgPos = varArg.pos;

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

                analyzeVarArgIsolatedness(varArg, varArgPos);
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

                    if (listConstrVarArg) {
                        BLangExpression arg = listConstructorExpr.exprs.get(tupleIndex);
                        analyzeAndSetArrowFuncFlagForIsolatedParamArg(arg);
                        type = arg.type;
                    }

                    if (!Symbols.isFlagOn(type.flags, Flags.ISOLATED)) {
                        dlog.error(varArgPos, DiagnosticCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
                    }
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
                        if (listConstrVarArg) {
                            BLangExpression arg = listConstructorExpr.exprs.get(i);
                            analyzeAndSetArrowFuncFlagForIsolatedParamArg(arg);
                            type = arg.type;
                        }

                        if (!Symbols.isFlagOn(type.flags, Flags.ISOLATED)) {
                            dlog.error(varArgPos, DiagnosticCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
                        }
                    }
                }

                if (listConstrVarArg) {
                    List<BLangExpression> exprs = listConstructorExpr.exprs;
                    for (int i = tupleIndex; i < exprs.size(); i++) {
                        BLangExpression arg = exprs.get(i);
                        analyzeAndSetArrowFuncFlagForIsolatedParamArg(arg);

                        if (!Symbols.isFlagOn(arg.type.flags, Flags.ISOLATED)) {
                            dlog.error(varArgPos, DiagnosticCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
                        }
                    }
                    return;
                }

                BType tupleRestType = tupleType.restType;
                if (tupleRestType == null) {
                    return;
                }

                if (!Symbols.isFlagOn(tupleRestType.flags, Flags.ISOLATED)) {
                    dlog.error(varArgPos, DiagnosticCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
                }

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
        analyzeRestArgsForRestParam(restArgs, symbol);
    }

    private void analyzeRestArgsForRestParam(List<BLangExpression> restArgs, BInvokableSymbol symbol) {
        if (Symbols.isFlagOn(((BArrayType) symbol.restParam.type).eType.flags, Flags.ISOLATED_PARAM)) {
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
            if (!Symbols.isFlagOn(arg.type.flags, Flags.ISOLATED)) {
                dlog.error(arg.pos, DiagnosticCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
            }
        }

        if (lastArgIsVarArg) {
            analyzeVarArgIsolatedness((BLangRestArgsExpression) lastArg, lastArg.pos);
        }
    }

    private void analyzeVarArgIsolatedness(BLangRestArgsExpression restArgsExpression, DiagnosticPos pos) {
        BLangExpression expr = restArgsExpression.expr;
        if (expr.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
            for (BLangExpression expression : ((BLangListConstructorExpr) expr).exprs) {
                analyzeAndSetArrowFuncFlagForIsolatedParamArg(expression);

                if (!Symbols.isFlagOn(expression.type.flags, Flags.ISOLATED)) {
                    dlog.error(pos, DiagnosticCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
                }
            }
            return;
        }

        BType varArgType = restArgsExpression.type;
        if (varArgType.tag == TypeTags.ARRAY) {
            if (!Symbols.isFlagOn(((BArrayType) varArgType).eType.flags, Flags.ISOLATED)) {
                dlog.error(pos, DiagnosticCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
            }
            return;
        }

        BTupleType tupleType = (BTupleType) varArgType;

        for (BType type : tupleType.tupleTypes) {
            if (!Symbols.isFlagOn(type.flags, Flags.ISOLATED)) {
                dlog.error(pos, DiagnosticCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
            }
        }

        BType restType = tupleType.restType;
        if (restType != null && !Symbols.isFlagOn(restType.flags, Flags.ISOLATED)) {
            dlog.error(pos, DiagnosticCode.INVALID_NON_ISOLATED_FUNCTION_AS_ARGUMENT);
        }
    }

    private boolean isBallerinaModule(BLangPackage module) {
        String orgName = module.packageID.orgName.value;
        return orgName.equals("ballerina") || orgName.equals("ballerinax");
    }

    private boolean isInIsolatedFunction(BLangInvokableNode enclInvokable) {
        if (enclInvokable == null) {
            // TODO: 14/11/20 This feels hack-y but cannot think of a different approach without a class variable
            // maintaining isolated-ness.
            if (env.node.getKind() != NodeKind.EXPR_FUNCTION_BODY ||
                    env.enclEnv.node.getKind() != NodeKind.ARROW_EXPR) {
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
        BLangNode node = env.node;
        NodeKind kind = node.getKind();

        if (kind != NodeKind.CLASS_DEFN) {
            return false;
        }

        BLangClassDefinition classDefinition = (BLangClassDefinition) node;

        BLangFunction initFunction = classDefinition.initFunction;
        if (initFunction == null) {
            return true;
        }

        return isIsolated(initFunction.symbol.flags);
    }

    private boolean isDefinitionReference(BSymbol symbol) {
        return Symbols.isTagOn(symbol, SymTag.SERVICE) ||
                Symbols.isTagOn(symbol, SymTag.TYPE_DEF) ||
                Symbols.isTagOn(symbol, SymTag.FUNCTION);
    }

    private boolean isIsolated(int flags) {
        return Symbols.isFlagOn(flags, Flags.ISOLATED);
    }

    private boolean isIsolatedClassField() {
        BLangNode node = env.node;
        return node.getKind() == NodeKind.CLASS_DEFN && ((BLangClassDefinition) node).flagSet.contains(Flag.ISOLATED);
    }

    private boolean isExpectedToBeAPrivateField(BVarSymbol symbol, BType type) {
        return !Symbols.isFlagOn(symbol.flags, Flags.FINAL) || isNotSubTypeOfReadOnlyOrIsolatedObject(type);
    }

    private boolean isNotSubTypeOfReadOnlyOrIsolatedObject(BType type) {
        if (type.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (isNotSubTypeOfReadOnlyOrIsolatedObject(memberType)) {
                    return true;
                }
            }
            return false;
        }

        int flags = type.flags;
        return !Symbols.isFlagOn(flags, Flags.READONLY) && !isIsolatedObjectTypes(type);
    }

    private boolean isIsolatedObjectFieldAccessViaSelf(BLangFieldBasedAccess fieldAccessExpr, boolean ignoreInit) {
        BLangExpression expr = fieldAccessExpr.expr;

        if (expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return false;
        }

        if (!Names.SELF.value.equals(((BLangSimpleVarRef) expr).variableName.value)) {
            return false;
        }

        return isInIsolatedObjectMethod(env, ignoreInit);
    }

    private boolean isValidIsolatedObjectFieldAccessViaSelfOutsideLock(BLangFieldBasedAccess fieldAccessExpr,
                                                                       boolean ignoreInit) {
        if (!isIsolatedObjectFieldAccessViaSelf(fieldAccessExpr, ignoreInit)) {
            return false;
        }

        BField field = ((BObjectType) env.enclInvokable.symbol.owner.type).fields.get(fieldAccessExpr.field.value);

        return isExpectedToBeAPrivateField(field.symbol, field.type);
    }

    private void validateIsolatedObjectFieldInitialValue(BType fieldType, BLangExpression expression) {
        if (Symbols.isFlagOn(fieldType.flags, Flags.READONLY) || isIsolatedObjectTypes(fieldType)) {
            return;
        }

        isReferenceOrIsolatedExpression(expression);
    }

    private boolean isIsolatedObjectTypes(BType type) {
        int tag = type.tag;

        if (tag == TypeTags.OBJECT) {
            return isIsolated(type.flags);
        }

        if (tag != TypeTags.UNION) {
            return false;
        }

        for (BType memberType : ((BUnionType) type).getMemberTypes()) {
            if (!isIsolated(memberType.flags)) {
                return false;
            }
        }
        return true;
    }

    private void isReferenceOrIsolatedExpression(BLangExpression expression) {
        isReferenceOrIsolatedExpression(expression, null, true);
    }

    private boolean isReferenceOrIsolatedExpression(BLangExpression expression, List<BLangSimpleVarRef> refList,
                                                    boolean logErrors) {
        BType type = expression.type;
        if (type != null && (Symbols.isFlagOn(type.flags, Flags.READONLY) || isIsolatedObjectTypes(type))) {
            return true;
        }

        switch (expression.getKind()) {
            case SIMPLE_VARIABLE_REF:
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) expression;
                BSymbol symbol = varRef.symbol;

                if (!logErrors) {
                    refList.add(varRef);
                }

                UniqueInitAndReferenceInfo varInfo = this.uniqueInitAndReferenceInfo.get(symbol);

                if (varInfo == null) {
                    uniqueInitAndReferenceInfo.put(symbol, new UniqueInitAndReferenceInfo(varRef));
                    return true;
                }

                if (logErrors) {
                    varInfo.refsOfVarExpectedToBeUnique.add(varRef);
                }

                return true;
            case LITERAL:
            case NUMERIC_LITERAL:
                return true;
            case LIST_CONSTRUCTOR_EXPR:
                for (BLangExpression expr : ((BLangListConstructorExpr) expression).exprs) {
                    if (isReferenceOrIsolatedExpression(expr, refList, logErrors) || logErrors) {
                        continue;
                    }

                    return false;
                }
                return true;
            case TABLE_CONSTRUCTOR_EXPR:
                for (BLangRecordLiteral mappingConstr : ((BLangTableConstructorExpr) expression).recordLiteralList) {
                    if (isReferenceOrIsolatedExpression(mappingConstr, refList, logErrors) || logErrors) {
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
                            if (!isReferenceOrIsolatedExpression(key.expr, refList, logErrors) && !logErrors) {
                                return false;
                            }
                        }

                        if (isReferenceOrIsolatedExpression(keyValueField.valueExpr, refList, logErrors) ||
                                logErrors) {
                            continue;
                        }
                        return false;
                    }

                    if (field.getKind() == NodeKind.RECORD_LITERAL_SPREAD_OP) {
                        if (isReferenceOrIsolatedExpression(
                                ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr, refList, logErrors) ||
                                logErrors) {
                            continue;
                        }
                        return false;
                    }

                    if (isReferenceOrIsolatedExpression((BLangRecordLiteral.BLangRecordVarNameField) field, refList,
                                                        logErrors) || logErrors) {
                        continue;
                    }
                    return false;
                }
                return true;
            case XML_COMMENT_LITERAL:
                BLangXMLCommentLiteral commentLiteral = (BLangXMLCommentLiteral) expression;

                for (BLangExpression textFragment : commentLiteral.textFragments) {
                    if (isReferenceOrIsolatedExpression(textFragment, refList, logErrors) || logErrors) {
                        continue;
                    }

                    return false;
                }

                BLangExpression commentLiteralConcatExpr = commentLiteral.concatExpr;
                if (commentLiteralConcatExpr == null) {
                    return true;
                }
                return isReferenceOrIsolatedExpression(commentLiteralConcatExpr, refList, logErrors);
            case XML_TEXT_LITERAL:
                BLangXMLTextLiteral textLiteral = (BLangXMLTextLiteral) expression;

                for (BLangExpression textFragment : textLiteral.textFragments) {
                    if (isReferenceOrIsolatedExpression(textFragment, refList, logErrors) || logErrors) {
                        continue;
                    }

                    return false;
                }

                BLangExpression textLiteralConcatExpr = textLiteral.concatExpr;
                if (textLiteralConcatExpr == null) {
                    return true;
                }
                return isReferenceOrIsolatedExpression(textLiteralConcatExpr, refList, logErrors);
            case XML_PI_LITERAL:
                BLangXMLProcInsLiteral procInsLiteral = (BLangXMLProcInsLiteral) expression;

                for (BLangExpression dataFragment : procInsLiteral.dataFragments) {
                    if (isReferenceOrIsolatedExpression(dataFragment, refList, logErrors) || logErrors) {
                        continue;
                    }

                    return false;
                }

                BLangExpression procInsLiteralConcatExpr = procInsLiteral.dataConcatExpr;
                if (procInsLiteralConcatExpr == null) {
                    return true;
                }
                return isReferenceOrIsolatedExpression(procInsLiteralConcatExpr, refList, logErrors);
            case XML_ELEMENT_LITERAL:
                for (BLangExpression child : ((BLangXMLElementLiteral) expression).children) {
                    if (isReferenceOrIsolatedExpression(child, refList, logErrors) || logErrors) {
                        continue;
                    }

                    return false;
                }
                return true;
            case XML_SEQUENCE_LITERAL:
                for (BLangExpression xmlItem : ((BLangXMLSequenceLiteral) expression).xmlItems) {
                    if (isReferenceOrIsolatedExpression(xmlItem, refList, logErrors) || logErrors) {
                        continue;
                    }

                    return false;
                }
                return true;
            case STRING_TEMPLATE_LITERAL:
                for (BLangExpression expr : ((BLangStringTemplateLiteral) expression).exprs) {
                    if (isReferenceOrIsolatedExpression(expr, refList, logErrors) || logErrors) {
                        continue;
                    }

                    return false;
                }
                return true;
            case TYPE_CONVERSION_EXPR:
                return isReferenceOrIsolatedExpression(((BLangTypeConversionExpr) expression).expr, refList, logErrors);
            case CHECK_EXPR:
            case CHECK_PANIC_EXPR:
                return isReferenceOrIsolatedExpression(((BLangCheckedExpr) expression).expr, refList, logErrors);
            case TRAP_EXPR:
                return isReferenceOrIsolatedExpression(((BLangTrapExpr) expression).expr, refList, logErrors);
            case TERNARY_EXPR:
                BLangTernaryExpr ternaryExpr = (BLangTernaryExpr) expression;

                if (!isReferenceOrIsolatedExpression(ternaryExpr.expr, refList, logErrors) && !logErrors) {
                    return false;
                }

                if (!isReferenceOrIsolatedExpression(ternaryExpr.thenExpr, refList, logErrors) && !logErrors) {
                    return false;
                }

                return isReferenceOrIsolatedExpression(ternaryExpr.elseExpr, refList, logErrors);
            case ELVIS_EXPR:
                BLangElvisExpr elvisExpr = (BLangElvisExpr) expression;

                if (!isReferenceOrIsolatedExpression(elvisExpr.lhsExpr, refList, logErrors) && !logErrors) {
                    return false;
                }

                return isReferenceOrIsolatedExpression(elvisExpr.rhsExpr, refList, logErrors);
            case TYPE_INIT_EXPR:
                BLangTypeInit typeInitExpr = (BLangTypeInit) expression;

                BLangInvocation typeInitInvocation = typeInitExpr.initInvocation;

                if (typeInitExpr == null) {
                    return true;
                }

                return isReferenceOrIsolatedExpression(typeInitInvocation, refList, logErrors);
            case INVOCATION:
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

                    return isReferenceOrIsolatedExpression(argExprs.get(0), refList, logErrors);
                } else if (isIsolated(invocationSymbol.type.flags)) {
                    List<BLangExpression> requiredArgs = invocation.requiredArgs;

                    BLangExpression calledOnExpr = invocation.expr;

                    if (calledOnExpr != null &&
                            (requiredArgs.isEmpty() || calledOnExpr != requiredArgs.get(0)) &&
                            (!isReferenceOrIsolatedExpression(calledOnExpr, refList, logErrors) && !logErrors)) {
                        return false;
                    }

                    for (BLangExpression requiredArg : requiredArgs) {
                        if (requiredArg.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                            if (isReferenceOrIsolatedExpression(((BLangNamedArgsExpression) requiredArg).expr,
                                                                refList, logErrors) || logErrors) {
                                continue;
                            }
                            return false;
                        }

                        if (isReferenceOrIsolatedExpression(requiredArg, refList, logErrors) || logErrors) {
                            continue;
                        }
                        return false;
                    }

                    for (BLangExpression restArg : invocation.restArgs) {
                        if (restArg.getKind() == NodeKind.REST_ARGS_EXPR) {
                            if (isReferenceOrIsolatedExpression(((BLangRestArgsExpression) restArg).expr, refList,
                                                                logErrors) || logErrors) {
                                continue;
                            }
                            return false;
                        }

                        if (isReferenceOrIsolatedExpression(restArg, refList, logErrors) || logErrors) {
                            continue;
                        }
                        return false;
                    }
                }
        }

        if (logErrors) {
            dlog.error(expression.pos,
                       DiagnosticCode.INVALID_NON_UNIQUE_EXPRESSION_AS_INITIAL_VALUE_IN_ISOLATED_OBJECT);
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

    private boolean isInvalidCopyingOfMutableValueInIsolatedObject(BLangSimpleVarRef expression, boolean copyOut) {
        return isInvalidCopyingOfMutableValueInIsolatedObject(
                expression, copyOut, Names.SELF.value.equals(expression.variableName.value));
    }

    private boolean isInvalidCopyingOfMutableValueInIsolatedObject(BLangExpression expression, boolean copyOut,
                                                                   boolean invokedOnSelf) {
        BType type = expression.type;
        if (Symbols.isFlagOn(type.flags, Flags.READONLY)) {
            return false;
        }

        BLangNode parent = expression.parent;

        NodeKind kind = parent.getKind();
        if (!(parent instanceof BLangExpression)) {
            if (isIsolatedObjectTypes(type)) {
                return false;
            }

            if (expression.getKind() == NodeKind.INVOCATION &&
                    isCloneOrCloneReadOnlyInvocation(((BLangInvocation) expression))) {
                return false;
            }

            if (!copyOut) {
                return true;
            }

            switch (kind) {
                case ASSIGNMENT:
                    BLangExpression varRef = ((BLangAssignment) parent).varRef;

                    if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                        return false;
                    }

                    BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
                    return isDefinedOutsideLock(names.fromIdNode(simpleVarRef.variableName), simpleVarRef.symbol.tag,
                                                env);
                case RECORD_DESTRUCTURE:
                    return hasRefDefinedOutsideLock(((BLangRecordDestructure) parent).varRef);
                case TUPLE_DESTRUCTURE:
                    return hasRefDefinedOutsideLock(((BLangTupleDestructure) parent).varRef);
                case ERROR_DESTRUCTURE:
                    return hasRefDefinedOutsideLock(((BLangErrorDestructure) parent).varRef);
                case RETURN:
                    return true;
            }
            return false;
        }

        BLangExpression parentExpression = (BLangExpression) parent;

        if (kind != NodeKind.INVOCATION) {
            return isInvalidCopyingOfMutableValueInIsolatedObject(parentExpression, copyOut, invokedOnSelf);
        }

        BLangInvocation invocation = (BLangInvocation) parentExpression;
        BLangExpression calledOnExpr = invocation.expr;

        if (calledOnExpr == expression) {
            // If this is the analysis of the called-on expression of a method call, do some additional checks.
            if (invocation.type.tag == TypeTags.NIL) {
                return false;
            }

            if (invokedOnSelf && !copyOut) {
                return false;
            }

            return isInvalidCopyingOfMutableValueInIsolatedObject(parentExpression, copyOut, invokedOnSelf);
        }

        // `expression` is an argument to a function
        return !isCloneOrCloneReadOnlyInvocation(invocation);
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

        if (enclInvokable == null || enclInvokable.getKind() != NodeKind.FUNCTION) {
            return false;
        }

        BLangFunction enclFunction = (BLangFunction) enclInvokable;

        if (!enclFunction.attachedFunction) {
            return false;
        }

        if (enclFunction.objInitFunction && ignoreInit) {
            return false;
        }

        BType ownerType = enclInvokable.symbol.owner.type;

        return ownerType.tag == TypeTags.OBJECT && isIsolated(ownerType.flags);
    }

    private boolean isInvalidCopyIn(BLangSimpleVarRef varRefExpr, SymbolEnv currentEnv) {
        return isInvalidCopyIn(varRefExpr, names.fromIdNode(varRefExpr.variableName), varRefExpr.symbol.tag,
                               currentEnv);
    }

    private boolean isInvalidCopyIn(BLangSimpleVarRef varRefExpr, Name name, int symTag, SymbolEnv currentEnv) {
        if (symResolver.lookupSymbolInGivenScope(currentEnv, name, symTag) != symTable.notFoundSymbol) {
            return false;
        }

        if (currentEnv.node.getKind() == NodeKind.LOCK) {
            return isInvalidCopyingOfMutableValueInIsolatedObject(varRefExpr, false);
        }

        return isInvalidCopyIn(varRefExpr, name, symTag, currentEnv.enclEnv);
    }

    private static class UniqueInitAndReferenceInfo {
        boolean uniqueInitExpr;
        List<BLangSimpleVarRef> refsUsedInInitExpr;
        List<BLangSimpleVarRef> refsOfVarExpectedToBeUnique;
        int totalRefCount;

        private UniqueInitAndReferenceInfo(boolean uniqueInitExpr, List<BLangSimpleVarRef> refsUsedInInitExpr) {
            this.uniqueInitExpr = uniqueInitExpr;
            this.refsUsedInInitExpr = refsUsedInInitExpr;
            this.refsOfVarExpectedToBeUnique = new ArrayList<>();
            this.totalRefCount = 0;
        }

        private UniqueInitAndReferenceInfo(BLangSimpleVarRef reference) {
            this.uniqueInitExpr = false;
            this.refsUsedInInitExpr = new ArrayList<>();
            this.refsOfVarExpectedToBeUnique = new ArrayList<>() {{ add(reference); }};
            this.totalRefCount = 0;
        }

        private UniqueInitAndReferenceInfo() {
            this.uniqueInitExpr = false;
            this.refsUsedInInitExpr = new ArrayList<>();
            this.refsOfVarExpectedToBeUnique = new ArrayList<>();
            this.totalRefCount = 1;
        }
    }

    /**
     * For isolated objects, invalid copy ins and non-isolated invocations should result in compilation errors if
     * they happen in a lock statement accessing a mutable field of the object. This class holds potentially
     * erroneous expression per lock statement, and whether or not the lock statement accesses a mutable field.
     */
    private static class PotentiallyInvalidExpressionInfo {
        BLangInvokableNode enclInvokableNode;
        boolean hasMutableAccessInLock = false;

        List<BLangSimpleVarRef> copyInVarRefs = new ArrayList<>();
        List<BLangSimpleVarRef> copyOutVarRefs = new ArrayList<>();
        List<BLangInvocation> invocations = new ArrayList<>();

        private PotentiallyInvalidExpressionInfo(BLangInvokableNode enclInvokableNode) {
            this.enclInvokableNode = enclInvokableNode;
        }
    }
}

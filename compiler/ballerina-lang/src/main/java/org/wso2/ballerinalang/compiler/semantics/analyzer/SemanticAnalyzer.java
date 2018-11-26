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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.RecordVariableNode.BLangRecordVariableKeyValueNode;
import org.ballerinalang.model.tree.clauses.GroupByNode;
import org.ballerinalang.model.tree.clauses.HavingNode;
import org.ballerinalang.model.tree.clauses.JoinStreamingInput;
import org.ballerinalang.model.tree.clauses.OrderByNode;
import org.ballerinalang.model.tree.clauses.OrderByVariableNode;
import org.ballerinalang.model.tree.clauses.PatternStreamingEdgeInputNode;
import org.ballerinalang.model.tree.clauses.SelectClauseNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.clauses.StreamActionNode;
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.clauses.WhereNode;
import org.ballerinalang.model.tree.clauses.WindowClauseNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.VariableReferenceNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.statements.StreamingQueryStatementNode;
import org.ballerinalang.model.tree.types.BuiltInReferenceTypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BChannelType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable.BLangRecordVariableKeyValue;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingEdgeInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSetAssignment;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWindow;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef.BLangRecordVarRefKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompensate;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDone;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStaticBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStructuredBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchTypedBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangScope;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.AttachPoints;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.model.tree.NodeKind.BRACED_TUPLE_EXPR;
import static org.ballerinalang.model.tree.NodeKind.LITERAL;
import static org.ballerinalang.model.tree.NodeKind.RECORD_LITERAL_EXPR;

/**
 * @since 0.94
 */
public class SemanticAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<SemanticAnalyzer> SYMBOL_ANALYZER_KEY =
            new CompilerContext.Key<>();
    private static final String AGGREGATOR_OBJECT_NAME = "Aggregator";

    private SymbolTable symTable;
    private SymbolEnter symbolEnter;
    private Names names;
    private SymbolResolver symResolver;
    private TypeChecker typeChecker;
    private Types types;
    private EndpointSPIAnalyzer endpointSPIAnalyzer;
    private BLangDiagnosticLog dlog;

    private SymbolEnv env;
    private BType expType;
    private DiagnosticCode diagCode;
    private BType resType;
    private boolean isSiddhiRuntimeEnabled;
    private boolean isGroupByAvailable;
    private Map<BVarSymbol, Set<BType>> typeGuards;

    public static SemanticAnalyzer getInstance(CompilerContext context) {
        SemanticAnalyzer semAnalyzer = context.get(SYMBOL_ANALYZER_KEY);
        if (semAnalyzer == null) {
            semAnalyzer = new SemanticAnalyzer(context);
        }

        return semAnalyzer;
    }

    public SemanticAnalyzer(CompilerContext context) {
        context.put(SYMBOL_ANALYZER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
        this.types = Types.getInstance(context);
        this.endpointSPIAnalyzer = EndpointSPIAnalyzer.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        pkgNode.accept(this);
        return pkgNode;
    }


    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.TYPE_CHECK)) {
            return;
        }
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);

        // Visit constants first.
        pkgNode.topLevelNodes.stream()
                .filter(pkgLevelNode -> pkgLevelNode.getKind() == NodeKind.CONSTANT)
                .forEach(constant -> analyzeDef((BLangNode) constant, pkgEnv));

        pkgNode.topLevelNodes.stream()
                .filter(pkgLevelNode -> pkgLevelNode.getKind() != NodeKind.CONSTANT)
                .filter(pkgLevelNode -> !(pkgLevelNode.getKind() == NodeKind.FUNCTION &&
                        ((BLangFunction) pkgLevelNode).flagSet.contains(Flag.LAMBDA)))
                .forEach(topLevelNode -> analyzeDef((BLangNode) topLevelNode, pkgEnv));

        while (pkgNode.lambdaFunctions.peek() != null) {
            BLangLambdaFunction lambdaFunction = pkgNode.lambdaFunctions.poll();
            BLangFunction function = lambdaFunction.function;
            lambdaFunction.type = function.symbol.type;
            analyzeDef(lambdaFunction.function, lambdaFunction.cachedEnv);
        }

        pkgNode.getTestablePkgs().forEach(testablePackage -> visit((BLangPackage) testablePackage));
        pkgNode.completedPhases.add(CompilerPhase.TYPE_CHECK);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.type = symTable.stringType;

        // Namespace node already having the symbol means we are inside an init-function,
        // and the symbol has already been declared by the original statement.
        if (xmlnsNode.symbol == null) {
            symbolEnter.defineNode(xmlnsNode, env);
        }

        typeChecker.checkExpr(xmlnsNode.namespaceURI, env, symTable.stringType);
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        analyzeNode(xmlnsStmtNode.xmlnsDecl, env);
    }

    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        //set function param flag to final
        funcNode.symbol.params.forEach(param -> param.flags |= Flags.FUNCTION_FINAL);

        funcNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoint = AttachPoint.FUNCTION;
            this.analyzeDef(annotationAttachment, funcEnv);
        });

        funcNode.requiredParams.forEach(p -> this.analyzeDef(p, funcEnv));
        funcNode.defaultableParams.forEach(p -> this.analyzeDef(p, funcEnv));
        if (funcNode.restParam != null) {
            this.analyzeDef(funcNode.restParam, funcEnv);
        }

        validateObjectAttachedFunction(funcNode);

        // Check for native functions
        if (Symbols.isNative(funcNode.symbol) || funcNode.interfaceFunction) {
            if (funcNode.body != null) {
                dlog.error(funcNode.pos, DiagnosticCode.EXTERN_FUNCTION_CANNOT_HAVE_BODY, funcNode.name);
            }
            return;
        }

        funcNode.endpoints.forEach(e -> {
            symbolEnter.defineNode(e, funcEnv);
            analyzeDef(e, funcEnv);
        });

        if (funcNode.body != null) {
            analyzeStmt(funcNode.body, funcEnv);
        }

        this.processWorkers(funcNode, funcEnv);
    }

    private void processWorkers(BLangInvokableNode invNode, SymbolEnv invEnv) {
        if (invNode.workers.size() > 0) {
            invEnv.scope.entries.putAll(invNode.body.scope.entries);
            invNode.workers.forEach(e -> this.symbolEnter.defineNode(e, invEnv));
            invNode.workers.forEach(e -> analyzeNode(e, invEnv));
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE
                || typeDefinition.typeNode.getKind() == NodeKind.RECORD_TYPE) {
            analyzeDef(typeDefinition.typeNode, env);
        }

        typeDefinition.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoint = AttachPoint.TYPE;
            annotationAttachment.accept(this);
        });
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        SymbolEnv objectEnv = SymbolEnv.createTypeEnv(objectTypeNode, objectTypeNode.symbol.scope, env);
        objectTypeNode.fields.forEach(field -> analyzeDef(field, objectEnv));

        // Visit functions as they are not in the same scope/env as the object fields
        objectTypeNode.functions.forEach(f -> analyzeDef(f, env));

        // Validate the referenced functions that don't have implementations within the function.
        ((BObjectTypeSymbol) objectTypeNode.symbol).referencedFunctions
                .forEach(func -> validateReferencedFunction(objectTypeNode.pos, func, env));

        if (objectTypeNode.initFunction == null) {
            return;
        }

        if (objectTypeNode.flagSet.contains(Flag.ABSTRACT)) {
            this.dlog.error(objectTypeNode.initFunction.pos, DiagnosticCode.ABSTRACT_OBJECT_CONSTRUCTOR,
                    objectTypeNode.symbol.name);
            return;
        }

        analyzeDef(objectTypeNode.initFunction, env);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        SymbolEnv recordEnv = SymbolEnv.createTypeEnv(recordTypeNode, recordTypeNode.symbol.scope, env);
        recordTypeNode.fields.forEach(field -> analyzeDef(field, recordEnv));
        analyzeDef(recordTypeNode.initFunction, recordEnv);
        validateDefaultable(recordTypeNode);
    }

    public void visit(BLangAnnotation annotationNode) {
        annotationNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoint = AttachPoint.ANNOTATION;
            annotationAttachment.accept(this);
        });
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        BSymbol symbol = this.symResolver.resolveAnnotation(annAttachmentNode.pos, env,
                names.fromString(annAttachmentNode.pkgAlias.getValue()),
                names.fromString(annAttachmentNode.getAnnotationName().getValue()));
        if (symbol == this.symTable.notFoundSymbol) {
            this.dlog.error(annAttachmentNode.pos, DiagnosticCode.UNDEFINED_ANNOTATION,
                    annAttachmentNode.getAnnotationName().getValue());
            return;
        }
        // Validate Attachment Point against the Annotation Definition.
        BAnnotationSymbol annotationSymbol = (BAnnotationSymbol) symbol;
        annAttachmentNode.annotationSymbol = annotationSymbol;
        if (annotationSymbol.attachPoints > 0 && !Symbols.isAttachPointPresent(annotationSymbol.attachPoints,
                AttachPoints.asMask(EnumSet.of(annAttachmentNode.attachPoint)))) {
            String msg = annAttachmentNode.attachPoint.getValue();
            this.dlog.error(annAttachmentNode.pos, DiagnosticCode.ANNOTATION_NOT_ALLOWED,
                    annotationSymbol, msg);
        }
        // Validate Annotation Attachment data struct against Annotation Definition struct.
        validateAnnotationAttachmentExpr(annAttachmentNode, annotationSymbol);
    }

    private void validateAnnotationAttachmentExpr(BLangAnnotationAttachment annAttachmentNode, BAnnotationSymbol
            annotationSymbol) {
        if (annotationSymbol.attachedType == null) {
            if (annAttachmentNode.expr != null) {
                this.dlog.error(annAttachmentNode.pos, DiagnosticCode.ANNOTATION_ATTACHMENT_NO_VALUE,
                        annotationSymbol.name);
            }
            return;
        }
        if (annAttachmentNode.expr != null) {
            this.typeChecker.checkExpr(annAttachmentNode.expr, env, annotationSymbol.attachedType.type);
        }
    }

    public void visit(BLangSimpleVariable varNode) {

        if (varNode.isDeclaredWithVar) {
            handleDeclaredWithVar(varNode);
            return;
        }

        int ownerSymTag = env.scope.owner.tag;
        if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            // This is a variable declared in a function, an action or a resource
            // If the variable is parameter then the variable symbol is already defined
            if (varNode.symbol == null) {
                symbolEnter.defineNode(varNode, env);
            }
        }

        if (varNode.symbol.type.tag == TypeTags.CHANNEL) {
            varNode.annAttachments.forEach(annotationAttachment -> {
                annotationAttachment.attachPoint = AttachPoint.CHANNEL;
                annotationAttachment.accept(this);
            });
        } else {
            varNode.annAttachments.forEach(annotationAttachment -> {
                annotationAttachment.attachPoint = AttachPoint.TYPE;
                annotationAttachment.accept(this);
            });
        }

        BType lhsType = varNode.symbol.type;
        varNode.type = lhsType;

        // Analyze the init expression
        BLangExpression rhsExpr = varNode.expr;
        if (rhsExpr == null) {
            if (lhsType.tag == TypeTags.ARRAY && typeChecker.isArrayOpenSealedType((BArrayType) lhsType)) {
                dlog.error(varNode.pos, DiagnosticCode.SEALED_ARRAY_TYPE_NOT_INITIALIZED);
                return;
            }
            if (varNode.symbol.type.tag != TypeTags.CHANNEL &&
                    varNode.symbol.type.tag != TypeTags.STREAM && varNode.symbol.owner.tag == SymTag.PACKAGE) {
                dlog.error(varNode.pos, DiagnosticCode.UNINITIALIZED_VARIABLE, varNode.name);
            }
            return;
        }

        // Here we create a new symbol environment to catch self references by keep the current
        // variable symbol in the symbol environment
        // e.g. int a = x + a;
        SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);

        typeChecker.checkExpr(rhsExpr, varInitEnv, lhsType);
    }

    public void visit(BLangRecordVariable varNode) {

        if (varNode.isDeclaredWithVar) {
            handleDeclaredWithVar(varNode);
            return;
        }

        if (varNode.type == null) {
            varNode.type = symResolver.resolveTypeNode(varNode.typeNode, env);
        }

        if (!validateRecordVariable(varNode)) {
            varNode.type = symTable.semanticError;
            return;
        }

        symbolEnter.defineNode(varNode, env);

        if (varNode.expr == null) {
            // we have no rhs to do type checking
            return;
        }

        typeChecker.checkExpr(varNode.expr, env, varNode.type);

    }

    public void visit(BLangTupleVariable varNode) {

        if (varNode.isDeclaredWithVar) {
            handleDeclaredWithVar(varNode);
            return;
        }

        if (varNode.type == null) {
            varNode.type = symResolver.resolveTypeNode(varNode.typeNode, env);
        }

        if (!(checkTypeAndVarCountConsistency(varNode))) {
            varNode.type = symTable.semanticError;
            return;
        }

        symbolEnter.defineNode(varNode, env);

        if (varNode.expr == null) {
            // we have no rhs to do type checking
            return;
        }

        typeChecker.checkExpr(varNode.expr, env, varNode.type);
    }

    private void handleDeclaredWithVar(BLangVariable variable) {

        BLangExpression varRefExpr = variable.expr;
        BType rhsType = typeChecker.checkExpr(varRefExpr, this.env, symTable.noType);

        if (NodeKind.VARIABLE == variable.getKind()) {

            if (!validateVariableDefinition(varRefExpr)) {
                rhsType = symTable.semanticError;
            }

            BLangSimpleVariable simpleVariable = (BLangSimpleVariable) variable;
            Name varName = names.fromIdNode(simpleVariable.name);
            if (varName == Names.IGNORE) {
                dlog.error(simpleVariable.pos, DiagnosticCode.UNDERSCORE_NOT_ALLOWED);
                return;
            }

            simpleVariable.type = rhsType;

            int ownerSymTag = env.scope.owner.tag;
            if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
                // This is a variable declared in a function, an action or a resource
                // If the variable is parameter then the variable symbol is already defined
                if (simpleVariable.symbol == null) {
                    symbolEnter.defineNode(simpleVariable, env);
                }
            }
        } else if (NodeKind.TUPLE_VARIABLE == variable.getKind()) {
            if (TypeTags.TUPLE != rhsType.tag) {
                dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_TYPE_DEFINITION_FOR_TUPLE_VAR, rhsType);
                return;
            }

            BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
            tupleVariable.type = rhsType;

            if (!(checkTypeAndVarCountConsistency(tupleVariable))) {
                return;
            }

            symbolEnter.defineNode(tupleVariable, env);

        } else if (NodeKind.RECORD_VARIABLE == variable.getKind()) {
            BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
            recordVariable.type = rhsType;

            validateRecordVariable(recordVariable);
        }
    }

    private boolean checkTypeAndVarCountConsistency(BLangTupleVariable varNode) {
        BTupleType tupleTypeNode;
        /*
          This switch block will resolve the tuple type of the tuple variable.
          For example consider the following - (int, string)|(boolean, float) (a, b) = foo();
          Since the varNode type is a union, the types of 'a' and 'b' will be resolved as follows:
          Type of 'a' will be (int | boolean) while the type of 'b' will be (string | float).
          Consider anydata (a, b) = foo();
          Here, the type of 'a'and type of 'b' will be both anydata.
         */
        switch (varNode.type.tag) {
            case TypeTags.UNION:
                BUnionType unionType = ((BUnionType) varNode.type);

                List<BTupleType> possibleTypes = unionType.memberTypes.stream()
                        .filter(type -> TypeTags.TUPLE == type.tag)
                        .map(BTupleType.class::cast)
                        .filter(tupleType -> varNode.memberVariables.size() == tupleType.tupleTypes.size())
                        .collect(Collectors.toList());

                if (possibleTypes.size() > 1) {
                    List<BType> memberTupleTypes = new ArrayList<>();
                    for (int i = 0; i < varNode.memberVariables.size(); i++) {
                        Set<BType> memberTypes = new HashSet<>();
                        for (BTupleType tupleType : possibleTypes) {
                            memberTypes.add(tupleType.tupleTypes.get(i));
                        }
                        memberTupleTypes.add(new BUnionType(null, memberTypes, false));
                    }
                    tupleTypeNode = new BTupleType(memberTupleTypes);
                } else {
                    tupleTypeNode = possibleTypes.get(0);
                }
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
                List<BType> memberTupleTypes = new ArrayList<>();
                for (int i = 0; i < varNode.memberVariables.size(); i++) {
                    memberTupleTypes.add(varNode.type);
                }
                tupleTypeNode = new BTupleType(memberTupleTypes);
                break;
            case TypeTags.TUPLE:
                tupleTypeNode = (BTupleType) varNode.type;
                break;
            default:
                dlog.error(varNode.pos, DiagnosticCode.INVALID_TYPE_DEFINITION_FOR_TUPLE_VAR, varNode.type);
                return false;
        }

        if (tupleTypeNode.tupleTypes.size() != varNode.memberVariables.size()) {
            dlog.error(varNode.pos, DiagnosticCode.INVALID_TUPLE_BINDING_PATTERN);
            return false;
        }

        int ignoredCount = 0;
        for (int i = 0; i < varNode.memberVariables.size(); i++) {
            BLangVariable var = varNode.memberVariables.get(i);
            if (var.getKind() == NodeKind.VARIABLE) {
                // '_' is allowed in tuple variables. Not allowed if all variables are named as '_'
                BLangSimpleVariable simpleVar = (BLangSimpleVariable) var;
                Name varName = names.fromIdNode(simpleVar.name);
                if (varName == Names.IGNORE) {
                    ignoredCount++;
                    simpleVar.type = symTable.noType;
                    continue;
                }
            }
            var.type = tupleTypeNode.tupleTypes.get(i);
            var.accept(this);
        }

        if (ignoredCount == varNode.memberVariables.size()) {
            dlog.error(varNode.pos, DiagnosticCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT);
            return false;
        }
        return true;
    }

    private boolean validateRecordVariable(BLangRecordVariable recordVar) {
        BRecordType recordVarType;
        /*
          This switch block will resolve the record type of the record variable.
          For example consider the following -
          type Foo record {int a, boolean b};
          type Bar record {string a, float b};
          Foo|Bar {a, b} = foo();
          Since the varNode type is a union, the types of 'a' and 'b' will be resolved as follows:
          Type of 'a' will be a union of the types of field 'a' in both Foo and Bar.
          i.e. type of 'a' is (int | string) and type of 'b' is (boolean | float).
          Consider anydata {a, b} = foo();
          Here, the type of 'a'and type of 'b' will be both anydata.
         */
        switch (recordVar.type.tag) {
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) recordVar.type;
                List<BRecordType> possibleTypes = unionType.memberTypes.stream()
                        .filter(type -> TypeTags.RECORD == type.tag)
                        .map(BRecordType.class::cast)
                        .filter(rec -> doesRecordContainKeys(rec, recordVar.variableList, recordVar.restParam != null))
                        .collect(Collectors.toList());
                if (possibleTypes.isEmpty()) {
                    dlog.error(recordVar.pos, DiagnosticCode.INVALID_RECORD_BINDING_PATTERN, recordVar.type);
                    return false;
                }

                if (possibleTypes.size() > 1) {
                    BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(0,
                            Names.EMPTY, env.enclPkg.symbol.pkgID, null, env.scope.owner);
                    recordVarType = (BRecordType) symTable.recordType;
                    List<BField> fields = new ArrayList<>();

                    recordVar.variableList.stream()
                            .map(bLangRecordVariableKeyValue -> bLangRecordVariableKeyValue.key.value)
                            .forEach(fieldName -> {
                                Set<BType> memberTypes = new HashSet<>();
                                possibleTypes.forEach(possibleType -> {
                                    Map<String, BType> possibleTypeFields = possibleType.fields
                                            .stream()
                                            .collect(Collectors.toMap(
                                                    field -> field.getName().getValue(),
                                                    BField::getType
                                            ));
                                    memberTypes.add(possibleTypeFields.get(fieldName) == null ?
                                            possibleType.restFieldType : possibleTypeFields.get(fieldName));
                                });
                                BType fieldType = memberTypes.size() > 1 ? new BUnionType(null, memberTypes, false) :
                                        memberTypes.iterator().next();
                                fields.add(new BField(names.fromString(fieldName),
                                        new BVarSymbol(0, names.fromString(fieldName), env.enclPkg.symbol.pkgID,
                                                fieldType, recordSymbol)));
                            });
                    if (recordVar.restParam != null) {
                        Set<BType> memberTypes = possibleTypes.stream()
                                .map(possibleType -> possibleType.restFieldType)
                                .collect(Collectors.toSet());

                        recordVarType.restFieldType = memberTypes.size() > 1 ?
                                new BUnionType(null, memberTypes, false) : memberTypes.iterator().next();
                    }
                    recordVarType.fields = fields;
                    recordSymbol.type = recordVarType;
                } else {
                    recordVarType = possibleTypes.get(0);
                }
                break;
            case TypeTags.RECORD:
                recordVarType = (BRecordType) recordVar.type;
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
                recordVarType = createSameTypedFieldsRecordType(recordVar, recordVar.type);
                break;
            default:
                dlog.error(recordVar.pos, DiagnosticCode.INVALID_RECORD_BINDING_PATTERN, recordVar.type);
                return false;
        }

        if (recordVar.isClosed) {
            if (!recordVarType.sealed) {
                dlog.error(recordVar.pos, DiagnosticCode.INVALID_CLOSED_RECORD_BINDING_PATTERN, recordVarType);
                return false;
            }

            if (recordVar.variableList.size() != recordVarType.fields.size()) {
                dlog.error(recordVar.pos, DiagnosticCode.NOT_ENOUGH_FIELDS_TO_MATCH_CLOSED_RECORDS, recordVarType);
                return false;
            }
        }

        Map<String, BField> recordVarTypeFields = recordVarType.fields
                .stream()
                .collect(Collectors.toMap(
                        field -> field.getName().getValue(),
                        field -> field
                ));

        boolean validRecord = true;
        for (BLangRecordVariableKeyValueNode variable : recordVar.variableList) {
            // Infer the type of each variable in recordVariable from the given record type
            // so that symbol enter is done recursively
            BLangVariable value = (BLangVariable) variable.getValue();
            if (!recordVarTypeFields.containsKey(variable.getKey().getValue())) {
                if (recordVarType.sealed) {
                    validRecord = false;
                    dlog.error(recordVar.pos, DiagnosticCode.INVALID_FIELD_IN_RECORD_BINDING_PATTERN,
                            variable.getKey().getValue(), recordVar.type);
                } else {
                    value.type = recordVarType.restFieldType;
                    value.accept(this);
                }
                continue;
            }

            value.type = recordVarTypeFields.get((variable.getKey().getValue())).type;
            value.accept(this);
        }

        if (recordVar.restParam != null) {
            ((BLangVariable) recordVar.restParam).type = new BMapType(TypeTags.MAP,
                    recordVarType.restFieldType, null);
            symbolEnter.defineNode((BLangNode) recordVar.restParam, env);
        }

        return validRecord;
    }

    private BRecordType createSameTypedFieldsRecordType(BLangRecordVariable recordVar, BType fieldTypes) {
        BRecordTypeSymbol recordSymbol =
                Symbols.createRecordSymbol(0, Names.EMPTY, env.enclPkg.symbol.pkgID, null, env.scope.owner);
        List<BField> fields = recordVar.variableList.stream()
                .map(bLangRecordVariableKeyValue -> bLangRecordVariableKeyValue.key.value)
                .map(fieldName -> new BField(names.fromString(fieldName), new BVarSymbol(0,
                        names.fromString(fieldName), env.enclPkg.symbol.pkgID, fieldTypes, recordSymbol)))
                .collect(Collectors.toList());

        BRecordType recordVarType = (BRecordType) symTable.recordType;
        recordVarType.fields = fields;
        recordSymbol.type = recordVarType;
        recordVarType.tsymbol = recordSymbol;
        if (recordVar.isClosed) {
            recordVarType.sealed = true;
        } else {
            recordVarType.sealed = false;
            recordVarType.restFieldType = fieldTypes;
        }

        return recordVarType;
    }

    private boolean doesRecordContainKeys(BRecordType recordVarType, List<BLangRecordVariableKeyValue> variableList,
                                          boolean hasRestParam) {
        Map<String, BField> recordVarTypeFields = recordVarType.fields
                .stream()
                .collect(Collectors.toMap(
                        field -> field.getName().getValue(),
                        field -> field
                ));
        for (BLangRecordVariableKeyValue var : variableList) {
            if (!recordVarTypeFields.containsKey(var.key.value) && recordVarType.sealed) {
                return false;
            }
        }

        if (!hasRestParam) {
            return true;
        }

        return !recordVarType.sealed;
    }

    // Statements

    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        blockNode.stmts.forEach(stmt -> analyzeStmt(stmt, blockEnv));
    }

    public void visit(BLangSimpleVariableDef varDefNode) {
        // This will prevent cases Eg:- int _ = 100;
        // We have prevented '_' from registering variable symbol at SymbolEnter, Hence this validation added.
        Name varName = names.fromIdNode(varDefNode.var.name);
        if (varName == Names.IGNORE) {
            dlog.error(varDefNode.var.pos, DiagnosticCode.UNDERSCORE_NOT_ALLOWED);
            return;
        }

        analyzeDef(varDefNode.var, env);
    }

    public void visit(BLangRecordVariableDef varDefNode) {
        // TODO: 10/18/18 Need to support record literals as well
        if (varDefNode.var.expr.getKind() == RECORD_LITERAL_EXPR) {
            dlog.error(varDefNode.pos, DiagnosticCode.INVALID_LITERAL_FOR_TYPE, "record binding pattern");
            return;
        }
        analyzeDef(varDefNode.var, env);
    }

    @Override
    public void visit(BLangTupleVariableDef tupleVariableDef) {
        analyzeDef(tupleVariableDef.var, env);
    }

    public void visit(BLangCompoundAssignment compoundAssignment) {
        List<BType> expTypes = new ArrayList<>();
        BLangExpression varRef = compoundAssignment.varRef;
        if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF &&
                varRef.getKind() != NodeKind.INDEX_BASED_ACCESS_EXPR &&
                varRef.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR &&
                varRef.getKind() != NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
            dlog.error(varRef.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRef);
            expTypes.add(symTable.semanticError);
        } else {
            this.typeChecker.checkExpr(varRef, env);
            expTypes.add(varRef.type);
        }
        this.typeChecker.checkExpr(compoundAssignment.expr, env);

        checkConstantAssignment(varRef);

        if (expTypes.get(0) != symTable.semanticError && compoundAssignment.expr.type != symTable.semanticError) {
            BSymbol opSymbol = this.symResolver.resolveBinaryOperator(compoundAssignment.opKind, expTypes.get(0),
                    compoundAssignment.expr.type);
            if (opSymbol == symTable.notFoundSymbol) {
                dlog.error(compoundAssignment.pos, DiagnosticCode.BINARY_OP_INCOMPATIBLE_TYPES,
                        compoundAssignment.opKind, expTypes.get(0), compoundAssignment.expr.type);
            } else {
                compoundAssignment.modifiedExpr = getBinaryExpr(varRef,
                        compoundAssignment.expr,
                        compoundAssignment.opKind,
                        opSymbol);
                this.types.checkTypes(compoundAssignment.modifiedExpr,
                        Lists.of(compoundAssignment.modifiedExpr.type), expTypes);
            }
        }
    }

    public void visit(BLangAssignment assignNode) {
        if (assignNode.varRef.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            ((BLangIndexBasedAccess) assignNode.varRef).leafNode = true;
        }

        // Check each LHS expression.
        BType expType = getTypeOfVarReferenceInAssignment(assignNode.varRef);
        typeChecker.checkExpr(assignNode.expr, this.env, expType);
    }

    @Override
    public void visit(BLangTupleDestructure tupleDeStmt) {
        getTypeOfVarReferenceInAssignment(tupleDeStmt.varRef);
        typeChecker.checkExpr(tupleDeStmt.expr, this.env);
        checkTupleVarRefEquivalency(tupleDeStmt.pos, tupleDeStmt.varRef, tupleDeStmt.expr.type, tupleDeStmt.expr.pos);
    }

    @Override
    public void visit(BLangRecordDestructure recordDeStmt) {

        // recursively visit the var refs and create the record type
        typeChecker.checkExpr(recordDeStmt.varRef, env);
        if (recordDeStmt.expr.getKind() == RECORD_LITERAL_EXPR) {
            // TODO: 10/18/18 Need to support record literals as well
            dlog.error(recordDeStmt.expr.pos, DiagnosticCode.INVALID_RECORD_LITERAL_BINDING_PATTERN);
            return;
        }
        typeChecker.checkExpr(recordDeStmt.expr, this.env);
        checkRecordVarRefEquivalency(recordDeStmt.pos, recordDeStmt.varRef, recordDeStmt.expr.type,
                recordDeStmt.expr.pos);
    }

    /**
     * When rhs is an expression of type record, this method will check the type of each field in the
     * record type against the record var ref fields.
     *
     * @param pos       diagnostic pos
     * @param lhsVarRef type of the record var ref
     * @param rhsType   the type on the rhs
     * @param rhsPos    position of the rhs expression
     */
    private void checkRecordVarRefEquivalency(DiagnosticPos pos, BLangRecordVarRef lhsVarRef, BType rhsType,
                                              DiagnosticPos rhsPos) {

        if (rhsType.tag != TypeTags.RECORD) {
            dlog.error(rhsPos, DiagnosticCode.INCOMPATIBLE_TYPES, "record type", rhsType);
            return;
        }

        BRecordType rhsRecordType = (BRecordType) rhsType;

        if (lhsVarRef.isClosed) {
            if (!rhsRecordType.sealed) {
                dlog.error(pos, DiagnosticCode.INVALID_CLOSED_RECORD_BINDING_PATTERN, rhsType);
                return;
            }

            if (lhsVarRef.recordRefFields.size() != rhsRecordType.fields.size()) {
                dlog.error(pos, DiagnosticCode.NOT_ENOUGH_FIELDS_TO_MATCH_CLOSED_RECORDS, rhsType);
                return;
            }
        }

        // check if all fields in record var ref are found in rhs record type
        lhsVarRef.recordRefFields.forEach(lhsField -> {
            if (rhsRecordType.fields.stream()
                    .noneMatch(rhsField -> lhsField.variableName.value.equals(rhsField.name.toString()))) {
                dlog.error(pos, DiagnosticCode.INVALID_FIELD_IN_RECORD_BINDING_PATTERN,
                        lhsField.variableName.value, rhsType);
            }
        });

        for (BField rhsField : rhsRecordType.fields) {
            List<BLangRecordVarRefKeyValue> expField = lhsVarRef.recordRefFields.stream()
                    .filter(field -> field.variableName.value.equals(rhsField.name.toString()))
                    .collect(Collectors.toList());

            if (expField.isEmpty()) {
                if (lhsVarRef.isClosed) {
                    dlog.error(lhsVarRef.pos, DiagnosticCode.NO_MATCHING_RECORD_REF_PATTERN, rhsField.name);
                }
                return;
            }

            if (expField.size() > 1) {
                dlog.error(pos, DiagnosticCode.MULTIPLE_RECORD_REF_PATTERN_FOUND, rhsField.name);
                return;
            }
            BLangExpression variableReference = expField.get(0).variableReference;
            if (variableReference.getKind() == NodeKind.RECORD_VARIABLE_REF) {
                checkRecordVarRefEquivalency(variableReference.pos,
                        (BLangRecordVarRef) variableReference, rhsField.type, rhsPos);
            } else if (variableReference.getKind() == NodeKind.TUPLE_VARIABLE_REF) {
                checkTupleVarRefEquivalency(pos, (BLangTupleVarRef) variableReference, rhsField.type, rhsPos);
            } else {
                types.checkType(variableReference.pos,
                        rhsField.type, variableReference.type, DiagnosticCode.INCOMPATIBLE_TYPES);
            }
        }

        //Check whether this is an readonly field.
        checkReadonlyAssignment(lhsVarRef);

        checkConstantAssignment(lhsVarRef);
    }

    private void checkTupleVarRefEquivalency(DiagnosticPos pos, BLangTupleVarRef varRef, BType rhsType,
                                             DiagnosticPos rhsPos) {
        if (rhsType.tag != TypeTags.TUPLE) {
            dlog.error(rhsPos, DiagnosticCode.INCOMPATIBLE_TYPES, varRef.type, rhsType);
            return;
        }
        if (varRef.expressions.size() != ((BTupleType) rhsType).tupleTypes.size()) {
            dlog.error(rhsPos, DiagnosticCode.INCOMPATIBLE_TYPES, varRef.type, rhsType);
            return;
        }
        for (int i = 0; i < varRef.expressions.size(); i++) {
            BLangExpression varRefExpr = varRef.expressions.get(i);
            if (NodeKind.RECORD_VARIABLE_REF == varRefExpr.getKind()) {
                BLangRecordVarRef recordVarRef = (BLangRecordVarRef) varRefExpr;
                checkRecordVarRefEquivalency(pos, recordVarRef, ((BTupleType) rhsType).tupleTypes.get(i), rhsPos);
            } else if (NodeKind.TUPLE_VARIABLE_REF == varRefExpr.getKind()) {
                BLangTupleVarRef tupleVarRef = (BLangTupleVarRef) varRefExpr;
                checkTupleVarRefEquivalency(pos, tupleVarRef, ((BTupleType) rhsType).tupleTypes.get(i), rhsPos);
            } else {
                if (varRefExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRefExpr;
                    Name varName = names.fromIdNode(simpleVarRef.variableName);
                    if (varName == Names.IGNORE) {
                        continue;
                    }
                }
                if (!types.isAssignable(((BTupleType) rhsType).tupleTypes.get(i), varRefExpr.type)) {
                    dlog.error(rhsPos, DiagnosticCode.INCOMPATIBLE_TYPES, varRef.type, rhsType);
                    break;
                }
            }
        }
    }

    private void checkConstantAssignment(BLangExpression varRef) {
        if (varRef.type == symTable.semanticError) {
            return;
        }

        if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return;
        }

        BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
        if (simpleVarRef.pkgSymbol != null && simpleVarRef.pkgSymbol.tag == SymTag.XMLNS) {
            dlog.error(varRef.pos, DiagnosticCode.XML_QNAME_UPDATE_NOT_ALLOWED);
            return;
        }

        Name varName = names.fromIdNode(simpleVarRef.variableName);
        if (!Names.IGNORE.equals(varName) && env.enclInvokable != env.enclPkg.initFunction) {
            if ((simpleVarRef.symbol.flags & Flags.FINAL) == Flags.FINAL) {
                dlog.error(varRef.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_FINAL, varRef);
            } else if ((simpleVarRef.symbol.flags & Flags.CONSTANT) == Flags.CONSTANT) {
                dlog.error(varRef.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_TO_CONSTANT);
            } else if ((simpleVarRef.symbol.flags & Flags.FUNCTION_FINAL) == Flags.FUNCTION_FINAL) {
                dlog.error(varRef.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_FUNCTION_ARGUMENT, varRef);
            }
        }
    }

    private void checkReadonlyAssignment(BLangExpression varRef) {
        if (varRef.type == symTable.semanticError) {
            return;
        }

        BLangVariableReference varRefExpr = (BLangVariableReference) varRef;
        if (varRefExpr.symbol != null) {
            if (env.enclPkg.symbol.pkgID != varRefExpr.symbol.pkgID && varRefExpr.lhsVar
                    && (varRefExpr.symbol.flags & Flags.READONLY) == Flags.READONLY) {
                dlog.error(varRefExpr.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_READONLY, varRefExpr);
            }
        }
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        // Creates a new environment here.
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, this.env.scope);
        this.env.copyTo(stmtEnv);
        BType bType = typeChecker.checkExpr(exprStmtNode.expr, stmtEnv, symTable.noType);
        if (bType != symTable.nilType && bType != symTable.semanticError) {
            dlog.error(exprStmtNode.pos, DiagnosticCode.ASSIGNMENT_REQUIRED);
        }
    }

    public void visit(BLangIf ifNode) {
        typeChecker.checkExpr(ifNode.expr, env, symTable.booleanType);

        Map<BVarSymbol, BType> typeGuards = typeChecker.getTypeGuards(ifNode.expr);
        if (!typeGuards.isEmpty()) {
            SymbolEnv ifBodyEnv = SymbolEnv.createBlockEnv(ifNode.body, env);
            for (Entry<BVarSymbol, BType> entry : typeGuards.entrySet()) {
                BVarSymbol originalVarSymbol = entry.getKey();
                BVarSymbol varSymbol = new BVarSymbol(0, originalVarSymbol.name, ifBodyEnv.scope.owner.pkgID,
                        entry.getValue(), this.env.scope.owner);
                symbolEnter.defineShadowedSymbol(ifNode.expr.pos, varSymbol, ifBodyEnv);

                // Cache the type guards, to be reused at the desugar.
                ifNode.ifTypeGuards.put(originalVarSymbol, varSymbol);
            }
        }

        BType actualType = ifNode.expr.type;
        if (TypeTags.TUPLE == actualType.tag) {
            dlog.error(ifNode.expr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.booleanType, actualType);
        }

        // Add the type guards of 'if' to the current type guards map.
        addTypeGuards(typeGuards);
        // Reset the current type guards before visiting the body.
        Map<BVarSymbol, Set<BType>> preTypeGuards = this.typeGuards;
        resetTypeGards();
        analyzeStmt(ifNode.body, env);
        // Restore the type guards after visiting the body
        this.typeGuards = preTypeGuards;

        if (ifNode.elseStmt != null) {
            // if this is the last 'else', add all the remaining type guards to the else.
            if (ifNode.elseStmt.getKind() == NodeKind.BLOCK) {
                addElseTypeGuards(ifNode);
            }
            analyzeStmt(ifNode.elseStmt, env);
        }

        // Reset the type guards when exiting from the if-else node
        resetTypeGards();
    }

    @Override
    public void visit(BLangMatch matchNode) {

        //first fail if both static and typed patterns have been defined in the match stmt
        if (!matchNode.getTypedPatternClauses().isEmpty() && !matchNode.getStaticPatternClauses().isEmpty()) {
            dlog.error(matchNode.pos, DiagnosticCode.INVALID_PATTERN_CLAUSES_IN_MATCH_STMT);
            return;
        }

        List<BType> exprTypes;
        BType exprType = typeChecker.checkExpr(matchNode.expr, env, symTable.noType);
        if (exprType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) exprType;
            exprTypes = new ArrayList<>(unionType.memberTypes);
        } else {
            exprTypes = Lists.of(exprType);
        }

        matchNode.patternClauses.forEach(patternClause -> {
            patternClause.matchExpr = matchNode.expr;
            patternClause.accept(this);
        });
        matchNode.exprTypes = exprTypes;
    }

    public void visit(BLangMatchTypedBindingPatternClause patternClause) {
        // If the variable is not equal to '_', then define the variable in the block scope
        if (!patternClause.variable.name.value.endsWith(Names.IGNORE.value)) {
            SymbolEnv blockEnv = SymbolEnv.createBlockEnv(patternClause.body, env);
            symbolEnter.defineNode(patternClause.variable, blockEnv);
            analyzeStmt(patternClause.body, blockEnv);
            return;
        }

        symbolEnter.defineNode(patternClause.variable, this.env);
        analyzeStmt(patternClause.body, this.env);
    }

    public void visit(BLangMatchStaticBindingPatternClause patternClause) {
        checkStaticMatchPatternLiteralType(patternClause.literal);
        analyzeStmt(patternClause.body, this.env);
    }

    private BType checkStaticMatchPatternLiteralType(BLangExpression expression) {

        switch (expression.getKind()) {
            case LITERAL:
                return typeChecker.checkExpr(expression, this.env);
            case BINARY_EXPR:
                BLangBinaryExpr binaryExpr = (BLangBinaryExpr) expression;

                if (OperatorKind.BITWISE_OR != binaryExpr.opKind) {
                    dlog.error(expression.pos, DiagnosticCode.INVALID_LITERAL_FOR_MATCH_PATTERN);
                    expression.type = symTable.errorType;
                    return expression.type;
                }

                checkStaticMatchPatternLiteralType(binaryExpr.lhsExpr);
                checkStaticMatchPatternLiteralType(binaryExpr.rhsExpr);
                expression.type =  symTable.anyType;
                return expression.type;
            case RECORD_LITERAL_EXPR:
                BLangRecordLiteral recordLiteral = (BLangRecordLiteral) expression;
                recordLiteral.type = new BMapType(TypeTags.MAP, symTable.anydataType, null);
                for (BLangRecordLiteral.BLangRecordKeyValue recLiteralKeyValue : recordLiteral.keyValuePairs) {
                    if (recLiteralKeyValue.key.expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF ||
                            (recLiteralKeyValue.key.expr.getKind() == NodeKind.LITERAL && typeChecker.checkExpr(
                                    recLiteralKeyValue.key.expr, this.env).tag == TypeTags.STRING)) {
                        BType fieldType = checkStaticMatchPatternLiteralType(recLiteralKeyValue.valueExpr);
                        types.setImplicitCastExpr(recLiteralKeyValue.valueExpr, fieldType, symTable.anyType);
                    } else {
                        recLiteralKeyValue.key.expr.type = symTable.errorType;
                        dlog.error(recLiteralKeyValue.key.expr.pos, DiagnosticCode.INVALID_RECORD_LITERAL_KEY);
                    }
                }
                return recordLiteral.type;
            case BRACED_TUPLE_EXPR:
                BLangBracedOrTupleExpr bracedOrTupleExpr = (BLangBracedOrTupleExpr) expression;
                List<BType> results = new ArrayList<>();
                for (int i = 0; i < bracedOrTupleExpr.expressions.size(); i++) {
                    results.add(checkStaticMatchPatternLiteralType(bracedOrTupleExpr.expressions.get(i)));
                }

                if (bracedOrTupleExpr.expressions.size() > 1) {
                    bracedOrTupleExpr.type = new BTupleType(results);
                    return bracedOrTupleExpr.type;
                } else {
                    bracedOrTupleExpr.isBracedExpr = true;
                    bracedOrTupleExpr.type = results.get(0);
                    return bracedOrTupleExpr.type;
                }
            default:
                dlog.error(expression.pos, DiagnosticCode.INVALID_LITERAL_FOR_MATCH_PATTERN);
                expression.type = symTable.errorType;
                return expression.type;
        }
    }

    public void visit(BLangMatchStructuredBindingPatternClause patternClause) {

        patternClause.bindingPatternVariable.type = patternClause.matchExpr.type;
        patternClause.bindingPatternVariable.expr = patternClause.matchExpr;

        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(patternClause.body, env);

        if (patternClause.typeGuardExpr != null) {
            BLangExpression typeGuardExpr = patternClause.typeGuardExpr;
            SymbolEnv typeGuardEnv = SymbolEnv.createExpressionEnv(typeGuardExpr, env);
            analyzeDef(patternClause.bindingPatternVariable, typeGuardEnv);
            blockEnv = SymbolEnv.createBlockEnv(patternClause.body, typeGuardEnv);
            typeChecker.checkExpr(patternClause.typeGuardExpr, typeGuardEnv);

            Map<BVarSymbol, BType> typeGuards = typeChecker.getTypeGuards(patternClause.typeGuardExpr);
            if (!typeGuards.isEmpty()) {
                SymbolEnv ifBodyEnv = SymbolEnv.createBlockEnv(patternClause.body, blockEnv);
                for (Entry<BVarSymbol, BType> entry : typeGuards.entrySet()) {
                    BVarSymbol originalVarSymbol = entry.getKey();
                    BVarSymbol varSymbol = new BVarSymbol(0, originalVarSymbol.name, ifBodyEnv.scope.owner.pkgID,
                            entry.getValue(), this.env.scope.owner);
                    symbolEnter.defineShadowedSymbol(patternClause.typeGuardExpr.pos, varSymbol, ifBodyEnv);

                    // Cache the type guards, to be reused at the desugar.
                    patternClause.typeGuards.put(originalVarSymbol, varSymbol);
                }
            }
        } else {
            analyzeDef(patternClause.bindingPatternVariable, blockEnv);
        }

        analyzeStmt(patternClause.body, blockEnv);
    }

    public void visit(BLangForeach foreach) {
        typeChecker.checkExpr(foreach.collection, env);
        foreach.varTypes = types.checkForeachTypes(foreach.collection, foreach.varRefs.size());
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(foreach.body, env);
        handleForeachVariables(foreach, foreach.varTypes, blockEnv);
        analyzeStmt(foreach.body, blockEnv);
    }

    public void visit(BLangWhile whileNode) {
        typeChecker.checkExpr(whileNode.expr, env, symTable.booleanType);

        BType actualType = whileNode.expr.type;
        if (TypeTags.TUPLE == actualType.tag) {
            dlog.error(whileNode.expr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.booleanType, actualType);
        }

        analyzeStmt(whileNode.body, env);
    }

    @Override
    public void visit(BLangLock lockNode) {
        analyzeStmt(lockNode.body, env);
    }

    public void visit(BLangAction actionNode) {
        BSymbol actionSymbol = actionNode.symbol;

        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);

        if (Symbols.isNative(actionSymbol)) {
            return;
        }

        actionNode.requiredParams.forEach(p -> this.analyzeDef(p, actionEnv));
        actionNode.defaultableParams.forEach(p -> this.analyzeDef(p, actionEnv));
        if (actionNode.restParam != null) {
            this.analyzeDef(actionNode.restParam, actionEnv);
        }

        actionNode.endpoints.forEach(e -> analyzeDef(e, actionEnv));
        analyzeStmt(actionNode.body, actionEnv);
        this.processWorkers(actionNode, actionEnv);
    }

    public void visit(BLangService serviceNode) {
        BServiceSymbol serviceSymbol = (BServiceSymbol) serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(serviceNode, serviceSymbol.scope, env);
        handleServiceTypeStruct(serviceNode);
        handleServiceEndpointBinds(serviceNode, serviceSymbol);
        handleAnonymousEndpointBind(serviceNode);
        serviceNode.annAttachments.forEach(a -> {
            a.attachPoint = AttachPoint.SERVICE;
            this.analyzeDef(a, serviceEnv);
        });
        serviceNode.nsDeclarations.forEach(xmlns -> this.analyzeDef(xmlns, serviceEnv));
        serviceNode.vars.forEach(v -> this.analyzeDef(v, serviceEnv));
        serviceNode.endpoints.forEach(e -> {
            symbolEnter.defineNode(e, serviceEnv);
            analyzeDef(e, serviceEnv);
        });
        this.analyzeDef(serviceNode.initFunction, serviceEnv);
        serviceNode.resources.forEach(r -> this.analyzeDef(r, serviceEnv));
    }

    private void handleServiceTypeStruct(BLangService serviceNode) {
        if (serviceNode.serviceTypeStruct == null) {
            return;
        }
        final BType serviceStructType = symResolver.resolveTypeNode(serviceNode.serviceTypeStruct, env);
        serviceNode.endpointType = endpointSPIAnalyzer.getEndpointTypeFromServiceType(
                serviceNode.serviceTypeStruct.pos, serviceStructType);
        if (serviceNode.endpointType != null) {
            serviceNode.endpointClientType = endpointSPIAnalyzer.getClientType(
                    (BObjectTypeSymbol) serviceNode.endpointType.tsymbol);
        }
    }

    private void handleServiceEndpointBinds(BLangService serviceNode, BServiceSymbol serviceSymbol) {
        for (BLangSimpleVarRef ep : serviceNode.boundEndpoints) {
            typeChecker.checkExpr(ep, env);
            if (ep.symbol == null || (ep.symbol.tag & SymTag.ENDPOINT) != SymTag.ENDPOINT) {
                dlog.error(ep.pos, DiagnosticCode.ENDPOINT_INVALID_TYPE, ep.variableName);
                continue;
            }
            final BEndpointVarSymbol epSym = (BEndpointVarSymbol) ep.symbol;
            if ((epSym.tag & SymTag.ENDPOINT) == SymTag.ENDPOINT) {
                if (epSym.registrable) {
                    serviceSymbol.boundEndpoints.add(epSym);
                    if (serviceNode.endpointType == null) {
                        serviceNode.endpointType = (BObjectType) epSym.type;
                        serviceNode.endpointClientType = endpointSPIAnalyzer.getClientType(
                                (BObjectTypeSymbol) serviceNode.endpointType.tsymbol);
                    }
                    // TODO : Validate serviceType endpoint type with bind endpoint types.
                } else {
                    dlog.error(ep.pos, DiagnosticCode.ENDPOINT_NOT_SUPPORT_REGISTRATION, epSym);
                }
            } else {
                dlog.error(ep.pos, DiagnosticCode.ENDPOINT_INVALID_TYPE, epSym);
            }
        }
        if (serviceNode.endpointType == null) {
            dlog.error(serviceNode.pos, DiagnosticCode.SERVICE_INVALID_ENDPOINT_TYPE, serviceNode.name);
        }
    }

    private void handleAnonymousEndpointBind(BLangService serviceNode) {
        if (serviceNode.anonymousEndpointBind == null) {
            return;
        }
        if (serviceNode.endpointType == null) {
            dlog.error(serviceNode.pos, DiagnosticCode.SERVICE_SERVICE_TYPE_REQUIRED_ANONYMOUS, serviceNode.name);
            return;
        }
        this.typeChecker.checkExpr(serviceNode.anonymousEndpointBind, env,
                endpointSPIAnalyzer.getEndpointConfigType((BObjectTypeSymbol) serviceNode.endpointType.tsymbol));
    }

    private void validateDefaultable(BLangRecordTypeNode recordTypeNode) {
        for (BLangSimpleVariable field : recordTypeNode.fields) {
            if (field.flagSet.contains(Flag.OPTIONAL) && field.expr != null) {
                dlog.error(field.pos, DiagnosticCode.DEFAULT_VALUES_NOT_ALLOWED_FOR_OPTIONAL_FIELDS, field.name.value);
            }
            if (field.expr != null) {
                continue;
            }
            break;
        }
    }

    public void visit(BLangResource resourceNode) {
        BSymbol resourceSymbol = resourceNode.symbol;
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, env);
        resourceNode.annAttachments.forEach(a -> {
            a.attachPoint = AttachPoint.RESOURCE;
            this.analyzeDef(a, resourceEnv);
        });
        defineResourceEndpoint(resourceNode, resourceEnv);
        resourceNode.requiredParams.forEach(p -> analyzeDef(p, resourceEnv));
        resourceNode.endpoints.forEach(e -> {
            symbolEnter.defineNode(e, resourceEnv);
            analyzeDef(e, resourceEnv);
        });
        analyzeStmt(resourceNode.body, resourceEnv);
        this.processWorkers(resourceNode, resourceEnv);
    }

    private void defineResourceEndpoint(BLangResource resourceNode, SymbolEnv resourceEnv) {
        if (!resourceNode.getParameters().isEmpty()) {
            final BLangSimpleVariable variable = resourceNode.getParameters().get(0);
            if (variable.type == symTable.endpointType) {
                String actualVarName = variable.name.value.substring(1);
                variable.name = new BLangIdentifier();
                variable.name.value = actualVarName;
                if (resourceEnv.enclService.endpointType != null) {
                    variable.type = resourceEnv.enclService.endpointType;
                    final BEndpointVarSymbol bEndpointVarSymbol = symbolEnter.defineEndpointVarSymbol(variable.pos,
                            EnumSet.noneOf(Flag.class), variable.type, names.fromString(actualVarName), resourceEnv);
                    variable.symbol = bEndpointVarSymbol;
                    if (variable.type.tsymbol.kind == SymbolKind.OBJECT
                            || variable.type.tsymbol.kind == SymbolKind.RECORD) {
                        endpointSPIAnalyzer.populateEndpointSymbol((BObjectTypeSymbol) variable.type.tsymbol,
                                bEndpointVarSymbol);
                    }
                } else {
                    variable.type = symTable.semanticError;
                    variable.symbol = symbolEnter.defineVarSymbol(variable.pos, EnumSet.noneOf(Flag.class),
                            variable.type, names.fromString(actualVarName), resourceEnv);
                }
                // Replace old symbol with new one.
                resourceNode.symbol.params.remove(0);
                resourceNode.symbol.params.add(0, variable.symbol);
                ((BInvokableType) resourceNode.symbol.type).paramTypes.remove(0);
                ((BInvokableType) resourceNode.symbol.type).paramTypes.add(0, variable.type);
            }
        }
    }

    public void visit(BLangTryCatchFinally tryCatchFinally) {
        dlog.error(tryCatchFinally.pos, DiagnosticCode.TRY_STMT_NOT_SUPPORTED);
    }

    public void visit(BLangCatch bLangCatch) {
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(bLangCatch.body, env);
        analyzeNode(bLangCatch.param, catchBlockEnv);
        if (bLangCatch.param.type.tag != TypeTags.ERROR) {
            dlog.error(bLangCatch.param.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.errorType,
                    bLangCatch.param.type);
        }
        analyzeStmt(bLangCatch.body, catchBlockEnv);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        analyzeStmt(transactionNode.transactionBody, env);
        if (transactionNode.onRetryBody != null) {
            analyzeStmt(transactionNode.onRetryBody, env);
        }
        if (transactionNode.retryCount != null) {
            typeChecker.checkExpr(transactionNode.retryCount, env, symTable.intType);
            checkRetryStmtValidity(transactionNode.retryCount);
        }

        if (transactionNode.onCommitFunction != null) {
            typeChecker.checkExpr(transactionNode.onCommitFunction, env, symTable.noType);
            if (transactionNode.onCommitFunction.type.tag == TypeTags.INVOKABLE) {
                ((BInvokableSymbol) ((BLangSimpleVarRef) transactionNode.onCommitFunction).symbol)
                        .isTransactionHandler = true;
            }
            checkTransactionHandlerValidity(transactionNode.onCommitFunction);
        }

        if (transactionNode.onAbortFunction != null) {
            typeChecker.checkExpr(transactionNode.onAbortFunction, env, symTable.noType);
            if (transactionNode.onAbortFunction.type.tag == TypeTags.INVOKABLE) {
                ((BInvokableSymbol) ((BLangSimpleVarRef) transactionNode.onAbortFunction).symbol)
                        .isTransactionHandler = true;
            }
            checkTransactionHandlerValidity(transactionNode.onAbortFunction);
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangDone doneNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangRetry retryNode) {
        /* ignore */
    }

    private boolean isJoinResultType(BLangSimpleVariable var) {
        BLangType type = var.typeNode;
        if (type instanceof BuiltInReferenceTypeNode) {
            return ((BuiltInReferenceTypeNode) type).getTypeKind() == TypeKind.MAP;
        }
        return false;
    }

    private BLangSimpleVariableDef createVarDef(BLangSimpleVariable var) {
        BLangSimpleVariableDef varDefNode = new BLangSimpleVariableDef();
        varDefNode.var = var;
        varDefNode.pos = var.pos;
        return varDefNode;
    }

    private BLangBlockStmt generateCodeBlock(StatementNode... statements) {
        BLangBlockStmt block = new BLangBlockStmt();
        for (StatementNode stmt : statements) {
            block.addStatement(stmt);
        }
        return block;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        SymbolEnv forkJoinEnv = SymbolEnv.createFolkJoinEnv(forkJoin, this.env);
        forkJoin.workers.forEach(e -> this.symbolEnter.defineNode(e, forkJoinEnv));
        forkJoin.workers.forEach(e -> this.analyzeDef(e, forkJoinEnv));
        if (!this.isJoinResultType(forkJoin.joinResultVar)) {
            this.dlog.error(forkJoin.joinResultVar.pos, DiagnosticCode.INVALID_WORKER_JOIN_RESULT_TYPE);
        }
        /* create code black and environment for join result section, i.e. (map results) */
        BLangBlockStmt joinResultsBlock = this.generateCodeBlock(this.createVarDef(forkJoin.joinResultVar));
        SymbolEnv joinResultsEnv = SymbolEnv.createBlockEnv(joinResultsBlock, this.env);
        this.analyzeNode(joinResultsBlock, joinResultsEnv);
        /* create an environment for the join body, making the enclosing environment the earlier
         * join result's environment */
        SymbolEnv joinBodyEnv = SymbolEnv.createBlockEnv(forkJoin.joinedBody, joinResultsEnv);
        this.analyzeNode(forkJoin.joinedBody, joinBodyEnv);

        if (forkJoin.timeoutExpression != null) {
            if (!this.isJoinResultType(forkJoin.timeoutVariable)) {
                this.dlog.error(forkJoin.timeoutVariable.pos, DiagnosticCode.INVALID_WORKER_TIMEOUT_RESULT_TYPE);
            }
            /* create code black and environment for timeout section */
            BLangBlockStmt timeoutVarBlock = this.generateCodeBlock(this.createVarDef(forkJoin.timeoutVariable));
            SymbolEnv timeoutVarEnv = SymbolEnv.createBlockEnv(timeoutVarBlock, this.env);
            this.typeChecker.checkExpr(forkJoin.timeoutExpression,
                    timeoutVarEnv, symTable.intType);
            this.analyzeNode(timeoutVarBlock, timeoutVarEnv);
            /* create an environment for the timeout body, making the enclosing environment the earlier
             * timeout var's environment */
            SymbolEnv timeoutBodyEnv = SymbolEnv.createBlockEnv(forkJoin.timeoutBody, timeoutVarEnv);
            this.analyzeNode(forkJoin.timeoutBody, timeoutBodyEnv);
        }

        this.validateJoinWorkerList(forkJoin, forkJoinEnv);
    }

    private void validateJoinWorkerList(BLangForkJoin forkJoin, SymbolEnv forkJoinEnv) {
        forkJoin.joinedWorkers.forEach(e -> {
            if (!this.workerExists(forkJoinEnv, e.value)) {
                this.dlog.error(forkJoin.pos, DiagnosticCode.UNDEFINED_WORKER, e.value);
            }
        });
    }

    @Override
    public void visit(BLangWorker workerNode) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.env);
        this.analyzeNode(workerNode.body, workerEnv);
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        endpointNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoint = AttachPoint.ENDPOINT;
            this.analyzeDef(annotationAttachment, env);
        });
        if (endpointNode.configurationExpr == null) {
            return;
        }
        BType configType = symTable.semanticError;
        if (endpointNode.symbol != null && endpointNode.symbol.type.tag == TypeTags.OBJECT) {
            if (endpointNode.configurationExpr.getKind() == RECORD_LITERAL_EXPR) {
                // Init expression.
                configType = endpointSPIAnalyzer.getEndpointConfigType(
                        (BObjectTypeSymbol) endpointNode.symbol.type.tsymbol);
            } else {
                // assign Expression.
                configType = endpointNode.symbol.type;
            }
        }
        this.typeChecker.checkExpr(endpointNode.configurationExpr, env, configType);
    }

    private boolean isInTopLevelWorkerEnv() {
        return this.env.enclEnv.node.getKind() == NodeKind.WORKER;
    }

    private boolean workerExists(SymbolEnv env, String workerName) {
        BSymbol symbol = this.symResolver.lookupSymbol(env, new Name(workerName), SymTag.WORKER);
        return (symbol != this.symTable.notFoundSymbol);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.env = this.env;
        this.typeChecker.checkExpr(workerSendNode.expr, this.env);

        BSymbol symbol = symResolver.lookupSymbol(env, names.fromIdNode(workerSendNode.workerIdentifier), SymTag
                .VARIABLE);
        if (workerSendNode.isChannel || symbol.getType().tag == TypeTags.CHANNEL) {
            visitChannelSend(workerSendNode, symbol);
            return;
        }

        if (!this.isInTopLevelWorkerEnv()) {
            this.dlog.error(workerSendNode.pos, DiagnosticCode.INVALID_WORKER_SEND_POSITION);
        }
        if (!workerSendNode.isForkJoinSend) {
            String workerName = workerSendNode.workerIdentifier.getValue();
            if (!this.workerExists(this.env, workerName)) {
                this.dlog.error(workerSendNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
            }
        }
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        BSymbol symbol = symResolver.lookupSymbol(env, names.fromIdNode(workerReceiveNode.workerIdentifier),
                SymTag.VARIABLE);

        if (workerReceiveNode.isChannel || symbol.getType().tag == TypeTags.CHANNEL) {
            visitChannelReceive(workerReceiveNode, symbol);
            return;
        }

        this.typeChecker.checkExpr(workerReceiveNode.expr, this.env);
        if (!this.isInTopLevelWorkerEnv()) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.INVALID_WORKER_RECEIVE_POSITION);
        }
        String workerName = workerReceiveNode.workerIdentifier.getValue();
        if (!this.workerExists(this.env, workerName)) {
            this.dlog.error(workerReceiveNode.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
        }

        if (workerReceiveNode.expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return;
        }
        BLangSimpleVarRef expr = (BLangSimpleVarRef) workerReceiveNode.expr;
        if (expr.symbol == null) {
            return;
        }
        if ((expr.symbol.flags & Flags.FINAL) == Flags.FINAL) {
            dlog.error(expr.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_FINAL);
        } else if ((expr.symbol.flags & Flags.CONSTANT) == Flags.CONSTANT) {
            dlog.error(expr.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_TO_CONSTANT);
        }
    }

    @Override
    public void visit(BLangReturn returnNode) {
        this.typeChecker.checkExpr(returnNode.expr, this.env,
                this.env.enclInvokable.returnTypeNode.type);
    }

    BType analyzeDef(BLangNode node, SymbolEnv env) {
        return analyzeNode(node, env);
    }

    BType analyzeStmt(BLangStatement stmtNode, SymbolEnv env) {
        return analyzeNode(stmtNode, env);
    }

    BType analyzeNode(BLangNode node, SymbolEnv env) {
        return analyzeNode(node, env, symTable.noType, null);
    }

    public void visit(BLangContinue continueNode) {
        /* ignore */
    }

    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangThrow throwNode) {
        dlog.error(throwNode.pos, DiagnosticCode.THROW_STMT_NOT_SUPPORTED);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        this.typeChecker.checkExpr(panicNode.expr, env);
        if (panicNode.expr.type.tag != TypeTags.ERROR) {
            dlog.error(panicNode.expr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.errorType, panicNode.expr.type);
        }
    }

    BType analyzeNode(BLangNode node, SymbolEnv env, BType expType, DiagnosticCode diagCode) {
        SymbolEnv prevEnv = this.env;
        BType preExpType = this.expType;
        DiagnosticCode preDiagCode = this.diagCode;

        // TODO Check the possibility of using a try/finally here
        this.env = env;
        this.expType = expType;
        this.diagCode = diagCode;
        node.accept(this);
        this.env = prevEnv;
        this.expType = preExpType;
        this.diagCode = preDiagCode;

        return resType;
    }


    //Streaming related methods.

    public void visit(BLangForever foreverStatement) {

        isSiddhiRuntimeEnabled = foreverStatement.isSiddhiRuntimeEnabled();
        foreverStatement.setEnv(env);
        for (StreamingQueryStatementNode streamingQueryStatement : foreverStatement.getStreamingQueryStatements()) {
            SymbolEnv stmtEnv = SymbolEnv.createStreamingQueryEnv(
                    (BLangStreamingQueryStatement) streamingQueryStatement, env);
            analyzeStmt((BLangStatement) streamingQueryStatement, stmtEnv);
        }

        if (isSiddhiRuntimeEnabled) {
            //Validate output attribute names with stream/struct
            for (StreamingQueryStatementNode streamingQueryStatement : foreverStatement.getStreamingQueryStatements()) {
                checkOutputAttributesWithOutputConstraint((BLangStatement) streamingQueryStatement);
                validateOutputAttributeTypes((BLangStatement) streamingQueryStatement);
            }
        }
    }

    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        defineSelectorAttributes(this.env, streamingQueryStatement);

        StreamingInput streamingInput = streamingQueryStatement.getStreamingInput();
        if (streamingInput != null) {
            ((BLangStreamingInput) streamingInput).accept(this);
            JoinStreamingInput joinStreamingInput = streamingQueryStatement.getJoiningInput();
            if (joinStreamingInput != null) {
                ((BLangJoinStreamingInput) joinStreamingInput).accept(this);
            }
        }

        SelectClauseNode selectClauseNode = streamingQueryStatement.getSelectClause();
        if (selectClauseNode != null) {
            ((BLangSelectClause) selectClauseNode).accept(this);
        }


        OrderByNode orderByNode = streamingQueryStatement.getOrderbyClause();
        if (orderByNode != null) {
            ((BLangOrderBy) orderByNode).accept(this);
        }

        StreamActionNode streamActionNode = streamingQueryStatement.getStreamingAction();
        if (streamActionNode != null) {
            ((BLangStreamAction) streamActionNode).accept(this);
        }

        BLangPatternClause patternClause = (BLangPatternClause) streamingQueryStatement.getPatternClause();
        if (patternClause != null) {
            patternClause.accept(this);
        }
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
        BLangPatternStreamingInput patternStreamingInput = (BLangPatternStreamingInput) patternClause
                .getPatternStreamingNode();
        patternStreamingInput.accept(this);
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
        List<PatternStreamingEdgeInputNode> patternStreamingEdgeInputs = patternStreamingInput
                .getPatternStreamingEdgeInputs();
        for (PatternStreamingEdgeInputNode inputNode : patternStreamingEdgeInputs) {
            BLangPatternStreamingEdgeInput streamingInput = (BLangPatternStreamingEdgeInput) inputNode;
            streamingInput.accept(this);
        }

        BLangPatternStreamingInput nestedPatternStreamingInput = (BLangPatternStreamingInput) patternStreamingInput
                .getPatternStreamingInput();
        if (nestedPatternStreamingInput != null) {
            nestedPatternStreamingInput.accept(this);
        }
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
        BLangVariableReference streamRef = (BLangVariableReference) patternStreamingEdgeInput.getStreamReference();
        typeChecker.checkExpr(streamRef, env);

        BLangWhere where = (BLangWhere) patternStreamingEdgeInput.getWhereClause();
        if (where != null) {
            where.accept(this);
        }
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        BLangExpression streamRef = (BLangExpression) streamingInput.getStreamReference();
        typeChecker.checkExpr(streamRef, env);

        WhereNode beforeWhereNode = streamingInput.getBeforeStreamingCondition();
        if (beforeWhereNode != null) {
            ((BLangWhere) beforeWhereNode).accept(this);
        }

        List<ExpressionNode> preInvocations = streamingInput.getPreFunctionInvocations();
        if (preInvocations != null) {
            preInvocations.stream().map(expr -> (BLangExpression) expr)
                    .forEach(expression -> expression.accept(this));
        }

        WindowClauseNode windowClauseNode = streamingInput.getWindowClause();
        if (windowClauseNode != null) {
            ((BLangWindow) windowClauseNode).accept(this);
        }

        List<ExpressionNode> postInvocations = streamingInput.getPostFunctionInvocations();
        if (postInvocations != null) {
            postInvocations.stream().map(expressionNode -> (BLangExpression) expressionNode)
                    .forEach(expression -> expression.accept(this));
        }

        WhereNode afterWhereNode = streamingInput.getAfterStreamingCondition();
        if (afterWhereNode != null) {
            ((BLangWhere) afterWhereNode).accept(this);
        }

        if (isTableReference(streamingInput.getStreamReference())) {
            if (streamingInput.getAlias() == null) {
                dlog.error(streamingInput.pos, DiagnosticCode.UNDEFINED_INVOCATION_ALIAS, ((BLangInvocation) streamRef)
                        .name.getValue());
            }
            if (streamingInput.getStreamReference().getKind() == NodeKind.INVOCATION) {
                BInvokableSymbol functionSymbol = (BInvokableSymbol) ((BLangInvocation) streamRef).symbol;
                symbolEnter.defineVarSymbol(streamingInput.pos, EnumSet.noneOf(Flag.class), ((BTableType) functionSymbol
                        .retType).constraint, names.fromString(streamingInput.getAlias()), env);
            } else {
                BType constraint = ((BTableType) ((BLangVariableReference) streamingInput.getStreamReference()).type)
                        .constraint;
                symbolEnter.defineVarSymbol(streamingInput.pos, EnumSet.noneOf(Flag.class), constraint,
                        names.fromString(streamingInput.getAlias()), env);
            }
        } else {
            //Create duplicate symbol for stream alias
            if (streamingInput.getAlias() != null) {
                BVarSymbol streamSymbol = (BVarSymbol) ((BLangSimpleVarRef) streamRef).symbol;
                BVarSymbol streamAliasSymbol = ASTBuilderUtil.duplicateVarSymbol(streamSymbol);
                streamAliasSymbol.name = names.fromString(streamingInput.getAlias());
                symbolEnter.defineSymbol(streamingInput.pos, streamAliasSymbol, env);
            }
        }
    }

    private boolean isTableReference(ExpressionNode streamReference) {
        if (streamReference.getKind() == NodeKind.INVOCATION) {
            return ((BLangInvocation) streamReference).type.tsymbol.type == symTable.tableType;
        } else {
            return ((BLangVariableReference) streamReference).type.tsymbol.type == symTable.tableType;
        }
    }

    @Override
    public void visit(BLangWindow windowClause) {
        //do nothing
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        BLangVariableReference variableReferenceNode = (BLangVariableReference) invocationExpr.getExpression();
        if (variableReferenceNode != null) {
            variableReferenceNode.accept(this);
        }
        if (!isSiddhiRuntimeEnabled) {
            if ((isGroupByAvailable)) {
                for (BLangExpression arg : invocationExpr.argExprs) {
                    typeChecker.checkExpr(arg, env);
                    switch (arg.getKind()) {
                        case NAMED_ARGS_EXPR:
                            invocationExpr.namedArgs.add(arg);
                            break;
                        case REST_ARGS_EXPR:
                            invocationExpr.restArgs.add(arg);
                            break;
                        default:
                            invocationExpr.requiredArgs.add(arg);
                            break;
                    }
                }
            } else {
                typeChecker.checkExpr(invocationExpr, env);
            }
        }
    }

    @Override
    public void visit(BLangWhere whereClause) {
        ExpressionNode expressionNode = whereClause.getExpression();
        ((BLangExpression) expressionNode).accept(this);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (isSiddhiRuntimeEnabled) {
            ExpressionNode leftExpression = binaryExpr.getLeftExpression();
            ((BLangExpression) leftExpression).accept(this);

            ExpressionNode rightExpression = binaryExpr.getRightExpression();
            ((BLangExpression) rightExpression).accept(this);
        } else {
            this.typeChecker.checkExpr(binaryExpr, env);
        }
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        GroupByNode groupByNode = selectClause.getGroupBy();
        if (groupByNode != null) {
            isGroupByAvailable = true;
            ((BLangGroupBy) groupByNode).accept(this);
        }

        HavingNode havingNode = selectClause.getHaving();
        if (havingNode != null) {
            ((BLangHaving) havingNode).accept(this);
        }

        List<? extends SelectExpressionNode> selectExpressionsList = selectClause.getSelectExpressions();
        if (selectExpressionsList != null) {
            for (SelectExpressionNode selectExpressionNode : selectExpressionsList) {
                ((BLangSelectExpression) selectExpressionNode).accept(this);
            }
        }
        isGroupByAvailable = false;
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        List<? extends ExpressionNode> variableExpressionList = groupBy.getVariables();
        for (ExpressionNode expressionNode : variableExpressionList) {
            if (isSiddhiRuntimeEnabled || !(expressionNode.getKind() == NodeKind.INVOCATION)) {
                ((BLangExpression) expressionNode).accept(this);
                return;
            }

            BLangInvocation invocationExpr = (BLangInvocation) expressionNode;
            VariableReferenceNode variableReferenceNode = (VariableReferenceNode) invocationExpr.getExpression();
            if (variableReferenceNode != null) {
                ((BLangVariableReference) variableReferenceNode).accept(this);
            }
            typeChecker.checkExpr(invocationExpr, env);
        }
    }

    @Override
    public void visit(BLangHaving having) {
        ExpressionNode expressionNode = having.getExpression();
        if (expressionNode != null) {
            ((BLangExpression) expressionNode).accept(this);
        }
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        List<? extends OrderByVariableNode> orderByVariableList = orderBy.getVariables();
        for (OrderByVariableNode orderByVariableNode : orderByVariableList) {
            ((BLangOrderByVariable) orderByVariableNode).accept(this);
        }
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
        BLangExpression expression = (BLangExpression) orderByVariable.getVariableReference();
        expression.accept(this);
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        ExpressionNode expressionNode = selectExpression.getExpression();
        if (!isSiddhiRuntimeEnabled) {
            if (expressionNode.getKind() == NodeKind.INVOCATION) {
                BLangInvocation invocation = (BLangInvocation) expressionNode;
                BSymbol invocationSymbol = symResolver.
                        resolvePkgSymbol(invocation.pos, env, names.fromString(invocation.pkgAlias.value)).
                        scope.lookup(new Name(invocation.name.value)).symbol;
                BSymbol aggregatorSymbol = symResolver.
                        resolvePkgSymbol(invocation.pos, env, Names.STREAMS_MODULE).
                        scope.lookup(new Name(AGGREGATOR_OBJECT_NAME)).symbol;

                if (invocationSymbol != null && invocationSymbol.type.getReturnType().tsymbol != aggregatorSymbol) {
                    this.typeChecker.checkExpr((BLangExpression) expressionNode, env);
                }
            } else {
                this.typeChecker.checkExpr((BLangExpression) expressionNode, env);
            }
        } else {
            ((BLangExpression) expressionNode).accept(this);
        }
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        BLangLambdaFunction function = (BLangLambdaFunction) streamAction.getInvokableBody();
        typeChecker.checkExpr(function, env);
        validateStreamingActionFunctionParameters(streamAction);
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        StreamingInput streamingInput = joinStreamingInput.getStreamingInput();
        if (streamingInput != null) {
            ((BLangStreamingInput) streamingInput).accept(this);
        }

        ExpressionNode expressionNode = joinStreamingInput.getOnExpression();
        if (expressionNode != null) {
            ((BLangExpression) expressionNode).accept(this);
        }
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        ExpressionNode expressionNode = setAssignmentClause.getExpressionNode();
        ((BLangExpression) expressionNode).accept(this);

        ExpressionNode variableReference = setAssignmentClause.getVariableReference();
        ((BLangExpression) variableReference).accept(this);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BLangVariableReference variableReferenceNode = (BLangVariableReference) fieldAccessExpr.getExpression();
        variableReferenceNode.accept(this);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        if (!isSiddhiRuntimeEnabled) {
            this.typeChecker.checkExpr(indexAccessExpr, env);
        }
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        if (!isSiddhiRuntimeEnabled) {
            this.typeChecker.checkExpr(varRefExpr, env);
        }
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        if (!isSiddhiRuntimeEnabled) {
            this.typeChecker.checkExpr(literalExpr, env);
        }
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        if (!isSiddhiRuntimeEnabled) {
            this.typeChecker.checkExpr(ternaryExpr, env);
        }
    }


    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        /* ignore */
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        /* ignore */
    }

    @Override
    public void visit(BLangScope scopeNode) {
        visit(scopeNode.scopeBody);

        symbolEnter.defineNode(scopeNode.compensationFunction.function, env);
        typeChecker.checkExpr(scopeNode.compensationFunction, env);
        symbolEnter.defineNode(scopeNode, env);
    }

    @Override
    public void visit(BLangCompensate node) {
        if (symTable.notFoundSymbol.equals(symResolver.lookupSymbol(env, names.fromString(node
                .getScopeName()
                .getValue()), SymTag.SCOPE))) {
            dlog.error(node.pos, DiagnosticCode.UNDEFINED_SYMBOL, node.getScopeName().getValue());
        }
    }

    @Override
    public void visit(BLangConstant constant) {
        BLangExpression expression = (BLangExpression) constant.value;
        if (expression.getKind() != NodeKind.LITERAL) {
            dlog.error(expression.pos, DiagnosticCode.ONLY_SIMPLE_LITERALS_CAN_BE_ASSIGNED_TO_CONST);
            return;
        }

        BLangLiteral value = (BLangLiteral) constant.value;

        if (constant.typeNode != null) {
            // Check the type of the value.
            typeChecker.checkExpr(value, env, constant.symbol.literalValueType);
        } else {
            // We don't have any expected type in this case since the type node is not available. So we get the type
            // from the type tag of the value.
            typeChecker.checkExpr(value, env, symTable.getTypeFromTag(value.typeTag));
        }

        // We need to update the literal value and the type tag here. Otherwise we will encounter issues when
        // creating new literal nodes in desugar because we wont be able to identify byte and decimal types.
        constant.symbol.literalValue = value.value;
        constant.symbol.literalValueTypeTag = value.typeTag;

        // We need to check types for the values in value spaces. Otherwise, float, decimal will not be identified in
        // codegen when retrieving the default value.
        BLangFiniteTypeNode typeNode = (BLangFiniteTypeNode) constant.associatedTypeDefinition.typeNode;
        for (BLangExpression literal : typeNode.valueSpace) {
            typeChecker.checkExpr(literal, env, constant.symbol.type);
        }
    }

    // Private methods

    private void visitChannelSend(BLangWorkerSend node, BSymbol channelSymbol) {
        node.isChannel = true;

        if (symTable.notFoundSymbol.equals(channelSymbol)) {
            dlog.error(node.pos, DiagnosticCode.UNDEFINED_SYMBOL, node.getWorkerName().getValue());
            return;
        }

        if (TypeTags.CHANNEL != channelSymbol.type.tag) {
            dlog.error(node.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.channelType, channelSymbol.type);
            return;
        }

        if (node.keyExpr != null) {
            typeChecker.checkExpr(node.keyExpr, env);
        }

        BType constraint = ((BChannelType) channelSymbol.type).constraint;
        if (node.expr.type.tag != constraint.tag) {
            dlog.error(node.pos, DiagnosticCode.INCOMPATIBLE_TYPES, constraint, node.expr.type);
        }
    }

    private void visitChannelReceive(BLangWorkerReceive node, BSymbol symbol) {
        node.isChannel = true;
        node.env = this.env;
        if (symbol == null) {
            symbol = symResolver.lookupSymbol(env, names.fromString(node.getWorkerName()
                    .getValue()), SymTag.VARIABLE);
        }

        if (symTable.notFoundSymbol.equals(symbol)) {
            dlog.error(node.pos, DiagnosticCode.UNDEFINED_SYMBOL, node.getWorkerName().getValue());
            return;
        }

        if (TypeTags.CHANNEL != symbol.type.tag) {
            dlog.error(node.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.channelType, symbol.type);
            return;
        }
        typeChecker.checkExpr(node.expr, env);

        BType constraint = ((BChannelType) symbol.type).constraint;
        if (node.expr.type.tag != constraint.tag) {
            dlog.error(node.pos, DiagnosticCode.INCOMPATIBLE_TYPES, constraint, node.expr.type);
            return;
        }

        if (node.keyExpr != null) {
            typeChecker.checkExpr(node.keyExpr, env);
        }
    }

    private void handleForeachVariables(BLangForeach foreachStmt, List<BType> varTypes, SymbolEnv env) {
        for (int i = 0; i < foreachStmt.varRefs.size(); i++) {
            BLangExpression varRef = foreachStmt.varRefs.get(i);
            // foreach variables supports only simpleVarRef expressions only.
            if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                dlog.error(varRef.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, varRef);
                continue;
            }
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
            simpleVarRef.lhsVar = true;
            Name varName = names.fromIdNode(simpleVarRef.variableName);
            if (varName == Names.IGNORE) {
                simpleVarRef.type = this.symTable.noType;
                typeChecker.checkExpr(simpleVarRef, env);
                continue;
            }
            // Check variable symbol for existence.
            BSymbol symbol = symResolver.lookupSymbol(env, varName, SymTag.VARIABLE);
            if (symbol == symTable.notFoundSymbol) {
                symbolEnter.defineVarSymbol(simpleVarRef.pos, Collections.emptySet(), varTypes.get(i), varName, env);
                typeChecker.checkExpr(simpleVarRef, env);
            } else {
                dlog.error(simpleVarRef.pos, DiagnosticCode.REDECLARED_SYMBOL, varName);
            }
        }
    }

    private void checkRetryStmtValidity(BLangExpression retryCountExpr) {
        boolean error = true;
        NodeKind retryKind = retryCountExpr.getKind();
        if (retryKind == LITERAL) {
            if (retryCountExpr.type.tag == TypeTags.INT) {
                int retryCount = Integer.parseInt(((BLangLiteral) retryCountExpr).getValue().toString());
                if (retryCount >= 0) {
                    error = false;
                }
            }
        } else if (retryKind == NodeKind.SIMPLE_VARIABLE_REF) {
            if (((BLangSimpleVarRef) retryCountExpr).symbol.flags == Flags.FINAL) {
                if (((BLangSimpleVarRef) retryCountExpr).symbol.type.tag == TypeTags.INT) {
                    error = false;
                }
            }
        }
        if (error) {
            this.dlog.error(retryCountExpr.pos, DiagnosticCode.INVALID_RETRY_COUNT);
        }
    }

    private void checkTransactionHandlerValidity(BLangExpression transactionHanlder) {
        if (transactionHanlder != null) {
            BSymbol handlerSymbol = ((BLangSimpleVarRef) transactionHanlder).symbol;
            if (handlerSymbol != null && handlerSymbol.kind != SymbolKind.FUNCTION) {
                dlog.error(transactionHanlder.pos, DiagnosticCode.INVALID_FUNCTION_POINTER_ASSIGNMENT_FOR_HANDLER);
            }
            if (transactionHanlder.type.tag == TypeTags.INVOKABLE) {
                BInvokableType handlerType = (BInvokableType) transactionHanlder.type;
                int parameterCount = handlerType.paramTypes.size();
                if (parameterCount != 1) {
                    dlog.error(transactionHanlder.pos, DiagnosticCode.INVALID_TRANSACTION_HANDLER_ARGS);
                }
                if (handlerType.paramTypes.get(0).tag != TypeTags.STRING) {
                    dlog.error(transactionHanlder.pos, DiagnosticCode.INVALID_TRANSACTION_HANDLER_ARGS);
                }
                if (handlerType.retType.tag != TypeTags.NIL) {
                    dlog.error(transactionHanlder.pos, DiagnosticCode.INVALID_TRANSACTION_HANDLER_SIGNATURE);
                }
            } else {
                dlog.error(transactionHanlder.pos, DiagnosticCode.LAMBDA_REQUIRED_FOR_TRANSACTION_HANDLER);
            }
        }
    }

    private BLangExpression getBinaryExpr(BLangExpression lExpr,
                                          BLangExpression rExpr,
                                          OperatorKind opKind,
                                          BSymbol opSymbol) {
        BLangBinaryExpr binaryExpressionNode = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpressionNode.lhsExpr = lExpr;
        binaryExpressionNode.rhsExpr = rExpr;
        binaryExpressionNode.pos = rExpr.pos;
        binaryExpressionNode.opKind = opKind;
        if (opSymbol != symTable.notFoundSymbol) {
            binaryExpressionNode.type = opSymbol.type.getReturnType();
            binaryExpressionNode.opSymbol = (BOperatorSymbol) opSymbol;
        } else {
            binaryExpressionNode.type = symTable.semanticError;
        }
        return binaryExpressionNode;
    }

    private boolean validateVariableDefinition(BLangExpression expr) {
        // following cases are invalid.
        // var a = [ x, y, ... ];
        // var a = { x : y };
        // var a = new ;
        final NodeKind kind = expr.getKind();
        if (kind == RECORD_LITERAL_EXPR || kind == NodeKind.ARRAY_LITERAL_EXPR
                || (kind == NodeKind.Type_INIT_EXPR && ((BLangTypeInit) expr).userDefinedType == null)) {
            dlog.error(expr.pos, DiagnosticCode.INVALID_ANY_VAR_DEF);
            return false;
        }
        if (kind == BRACED_TUPLE_EXPR) {
            BLangBracedOrTupleExpr bracedOrTupleExpr = (BLangBracedOrTupleExpr) expr;
            if (bracedOrTupleExpr.expressions.size() > 1 &&
                    bracedOrTupleExpr.expressions.stream().anyMatch(literal -> literal.getKind() == LITERAL)) {
                dlog.error(expr.pos, DiagnosticCode.INVALID_ANY_VAR_DEF);
                return false;
            }
        }
        return true;
    }

    private BType getTypeOfVarReferenceInAssignment(BLangExpression expr) {
        // In assignment, lhs supports only simpleVarRef, indexBasedAccess, filedBasedAccess expressions.
        if (expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF &&
                expr.getKind() != NodeKind.INDEX_BASED_ACCESS_EXPR &&
                expr.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR &&
                expr.getKind() != NodeKind.XML_ATTRIBUTE_ACCESS_EXPR &&
                expr.getKind() != NodeKind.TUPLE_VARIABLE_REF) {
            dlog.error(expr.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, expr);
            return symTable.semanticError;
        }

        BLangVariableReference varRefExpr = (BLangVariableReference) expr;
        varRefExpr.lhsVar = true;
        typeChecker.checkExpr(varRefExpr, env);

        //Check whether this is an readonly field.
        checkReadonlyAssignment(varRefExpr);

        checkConstantAssignment(varRefExpr);
        return varRefExpr.type;
    }

    private void checkOutputAttributesWithOutputConstraint(BLangStatement streamingQueryStatement) {
        List<? extends SelectExpressionNode> selectExpressions =
                ((BLangStreamingQueryStatement) streamingQueryStatement).getSelectClause().getSelectExpressions();

        List<String> variableList = new ArrayList<>();
        boolean isSelectAll = true;
        if (!((BLangStreamingQueryStatement) streamingQueryStatement).getSelectClause().isSelectAll()) {
            isSelectAll = false;
            for (SelectExpressionNode expressionNode : selectExpressions) {
                String variableName;
                if (expressionNode.getIdentifier() != null) {
                    variableName = expressionNode.getIdentifier();
                } else {
                    if (expressionNode.getExpression() instanceof BLangFieldBasedAccess) {
                        variableName = ((BLangFieldBasedAccess) expressionNode.getExpression()).field.value;
                    } else {
                        variableName = ((BLangSimpleVarRef) (expressionNode).getExpression()).variableName.value;
                    }
                }
                variableList.add(variableName);
            }
        }

        // Validate whether input stream constraint type only contains attribute type that can be processed by Siddhi
        if (((BLangStreamingQueryStatement) streamingQueryStatement).getStreamingInput() != null) {
            List<BField> fields = ((BStructureType) ((BStreamType) ((BLangExpression)
                    (((BLangStreamingQueryStatement) streamingQueryStatement).getStreamingInput()).
                            getStreamReference()).type).constraint).fields;

            for (BField structField : fields) {
                validateStreamEventType(((BLangStreamingQueryStatement) streamingQueryStatement).pos, structField);
                if (isSelectAll) {
                    //create the variable list to validate when select * clause is used in query
                    variableList.add(structField.name.value);
                }
            }
        }

        BType streamActionArgumentType = ((BInvokableType) ((BLangLambdaFunction) (((BLangStreamingQueryStatement)
                streamingQueryStatement).getStreamingAction()).getInvokableBody()).type).paramTypes.get(0);

        if (streamActionArgumentType.tag == TypeTags.ARRAY) {
            BType structType = (((BArrayType) streamActionArgumentType).eType);

            if (structType.tag == TypeTags.OBJECT || structType.tag == TypeTags.RECORD) {
                List<BField> structFieldList = ((BStructureType) structType).fields;
                List<String> structFieldNameList = new ArrayList<>();
                for (BField structField : structFieldList) {
                    validateStreamEventType(((BLangStreamAction) ((BLangStreamingQueryStatement)
                            streamingQueryStatement).getStreamingAction()).pos, structField);
                    structFieldNameList.add(structField.name.value);
                }

                if (!variableList.equals(structFieldNameList)) {
                    dlog.error(((BLangStreamAction) ((BLangStreamingQueryStatement) streamingQueryStatement).
                            getStreamingAction()).pos, DiagnosticCode.INCOMPATIBLE_STREAM_ACTION_ARGUMENT, structType);
                }
            }
        }
    }

    private void validateStreamEventType(DiagnosticPos pos, BField field) {
        if (!(field.type.tag == TypeTags.INT || field.type.tag == TypeTags.BOOLEAN || field.type.tag == TypeTags.STRING
                || field.type.tag == TypeTags.FLOAT)) {
            dlog.error(pos, DiagnosticCode.INVALID_STREAM_ATTRIBUTE_TYPE);
        }
    }

    private void validateStreamingEventType(DiagnosticPos pos, BType actualType, String attributeName, BType expType,
                                            DiagnosticCode diagCode) {
        if (expType.tag == TypeTags.SEMANTIC_ERROR) {
            return;
        } else if (expType.tag == TypeTags.NONE) {
            return;
        } else if (actualType.tag == TypeTags.SEMANTIC_ERROR) {
            return;
        } else if (this.types.isAssignable(actualType, expType)) {
            return;
        }

        // e.g. incompatible types: expected 'int' for attribute 'name', found 'string'
        dlog.error(pos, diagCode, expType, attributeName, actualType);
    }

    private void validateOutputAttributeTypes(BLangStatement streamingQueryStatement) {
        StreamingInput streamingInput = ((BLangStreamingQueryStatement) streamingQueryStatement).getStreamingInput();
        JoinStreamingInput joinStreamingInput = ((BLangStreamingQueryStatement) streamingQueryStatement).
                getJoiningInput();

        if (streamingInput != null) {
            Map<String, List<BField>> inputStreamSpecificFieldMap =
                    createInputStreamSpecificFieldMap(streamingInput, joinStreamingInput);
            BType streamActionArgumentType = ((BInvokableType) ((BLangLambdaFunction) (((BLangStreamingQueryStatement)
                    streamingQueryStatement).getStreamingAction()).getInvokableBody()).type).paramTypes.get(0);

            if (streamActionArgumentType.tag == TypeTags.ARRAY) {
                BType structType = (((BArrayType) streamActionArgumentType).eType);

                if (structType.tag == TypeTags.OBJECT || structType.tag == TypeTags.RECORD) {
                    List<BField> outputStreamFieldList = ((BStructureType) structType).fields;
                    List<? extends SelectExpressionNode> selectExpressions = ((BLangStreamingQueryStatement)
                            streamingQueryStatement).getSelectClause().getSelectExpressions();

                    if (!((BLangStreamingQueryStatement) streamingQueryStatement).getSelectClause().isSelectAll()) {
                        for (int i = 0; i < selectExpressions.size(); i++) {
                            SelectExpressionNode expressionNode = selectExpressions.get(i);
                            BField structField = null;
                            if (expressionNode.getExpression() instanceof BLangFieldBasedAccess) {
                                String attributeName =
                                        ((BLangFieldBasedAccess) expressionNode.getExpression()).field.value;
                                String streamIdentifier = ((BLangSimpleVarRef) ((BLangFieldBasedAccess) expressionNode.
                                        getExpression()).expr).variableName.value;

                                List<BField> streamFieldList = inputStreamSpecificFieldMap.
                                        get(streamIdentifier);
                                if (streamFieldList == null) {
                                    dlog.error(((BLangSelectClause)
                                                    ((BLangStreamingQueryStatement) streamingQueryStatement).
                                                            getSelectClause()).pos,
                                            DiagnosticCode.UNDEFINED_STREAM_REFERENCE, streamIdentifier);
                                } else {
                                    structField = getStructField(streamFieldList, attributeName);
                                    validateAttributeWithOutputStruct(structField, attributeName,
                                            streamingQueryStatement, outputStreamFieldList.get(i));
                                }
                            } else if (expressionNode.getExpression() instanceof BLangSimpleVarRef) {
                                String attributeName = ((BLangSimpleVarRef) expressionNode.getExpression()).
                                        variableName.getValue();

                                for (List<BField> streamFieldList :
                                        inputStreamSpecificFieldMap.values()) {
                                    structField = getStructField(streamFieldList, attributeName);
                                    if (structField != null) {
                                        break;
                                    }
                                }
                                validateAttributeWithOutputStruct(structField, attributeName, streamingQueryStatement,
                                        outputStreamFieldList.get(i));
                            }
                        }
                    } else {
                        List<BField> inputStreamFields = ((BStructureType) ((BStreamType)
                                ((BLangExpression) (((BLangStreamingQueryStatement) streamingQueryStatement).
                                        getStreamingInput()).getStreamReference()).type).constraint).fields;

                        for (int i = 0; i < inputStreamFields.size(); i++) {
                            BField inputStructField = inputStreamFields.get(i);
                            BField outputStructField = outputStreamFieldList.get(i);
                            validateStreamingEventType(((BLangStreamAction) ((BLangStreamingQueryStatement)
                                            streamingQueryStatement).getStreamingAction()).pos,
                                    outputStructField.getType(), outputStructField.getName().getValue(),
                                    inputStructField.getType(), DiagnosticCode.STREAMING_INCOMPATIBLE_TYPES);
                        }
                    }
                }
            }
        }
    }

    private List<BField> getFieldListFromStreamInput(StreamingInput streamingInput) {
        return ((BStructureType) ((BStreamType) ((BLangSimpleVarRef)
                streamingInput.getStreamReference()).type).constraint).fields;
    }

    private String getStreamIdentifier(StreamingInput streamingInput) {
        String streamIdentifier = streamingInput.getAlias();
        if (streamIdentifier == null) {
            streamIdentifier = ((BLangSimpleVarRef) streamingInput.getStreamReference()).variableName.value;
        }

        return streamIdentifier;
    }

    private BField getStructField(List<BField> fieldList, String fieldName) {
        for (BField structField : fieldList) {
            String structFieldName = structField.name.getValue();
            if (structFieldName.equalsIgnoreCase(fieldName)) {
                return structField;
            }
        }

        return null;
    }

    private void validateAttributeWithOutputStruct(BField structField, String attributeName,
                                                   BLangStatement streamingQueryStatement,
                                                   BField outputStructField) {

        if (structField != null) {
            validateStreamingEventType(((BLangStreamAction) ((BLangStreamingQueryStatement)
                            streamingQueryStatement).getStreamingAction()).pos,
                    outputStructField.getType(), attributeName, structField.getType(),
                    DiagnosticCode.STREAMING_INCOMPATIBLE_TYPES);
        }
    }

    private Map<String, List<BField>> createInputStreamSpecificFieldMap
            (StreamingInput streamingInput, JoinStreamingInput joinStreamingInput) {

        Map<String, List<BField>> inputStreamSpecificFieldMap = new HashMap<>();
        String firstStreamIdentifier = getStreamIdentifier(streamingInput);
        List<BField> firstInputStreamFieldList = getFieldListFromStreamInput(streamingInput);
        inputStreamSpecificFieldMap.put(firstStreamIdentifier, firstInputStreamFieldList);

        if (joinStreamingInput != null) {
            List<BField> secondInputStreamFieldList =
                    getFieldListFromStreamInput(joinStreamingInput.getStreamingInput());
            String secondStreamIdentifier = getStreamIdentifier(joinStreamingInput.getStreamingInput());
            inputStreamSpecificFieldMap.put(secondStreamIdentifier, secondInputStreamFieldList);
        }

        return inputStreamSpecificFieldMap;
    }

    private void validateStreamingActionFunctionParameters(BLangStreamAction streamAction) {
        List<BLangSimpleVariable> functionParameters = ((BLangFunction) streamAction.getInvokableBody().
                getFunctionNode()).requiredParams;
        if (functionParameters == null || functionParameters.size() != 1) {
            dlog.error((streamAction).pos,
                    DiagnosticCode.INVALID_STREAM_ACTION_ARGUMENT_COUNT,
                    functionParameters == null ? 0 : functionParameters.size());
        } else if (!(functionParameters.get(0).type.tag == TypeTags.ARRAY &&
                (((BArrayType) functionParameters.get(0).type).eType.tag == TypeTags.OBJECT)
                || ((BArrayType) functionParameters.get(0).type).eType.tag == TypeTags.RECORD)) {
            dlog.error((streamAction).pos, DiagnosticCode.INVALID_STREAM_ACTION_ARGUMENT_TYPE,
                    ((BArrayType) functionParameters.get(0).type).eType.getKind());
        }
    }

    private void defineSelectorAttributes(SymbolEnv stmtEnv, StreamingQueryStatementNode node) {
        if (node.getStreamingAction() == null) {
            return;
        }
        BType streamActionArgumentType = ((BLangLambdaFunction) node.getStreamingAction()
                .getInvokableBody()).function.requiredParams.get(0).type;
        if (streamActionArgumentType.tag != TypeTags.ARRAY) {
            return;
        }
        BType structType = (((BArrayType) streamActionArgumentType).eType);
        if (structType.tag == TypeTags.OBJECT || structType.tag == TypeTags.RECORD) {
            List<BField> outputStreamFieldList = ((BStructureType) structType).fields;
            for (BField field : outputStreamFieldList) {
                stmtEnv.scope.define(field.name, field.symbol);
            }
        }
    }

    /**
     * Validate functions attached to objects.
     *
     * @param funcNode Function node
     */
    private void validateObjectAttachedFunction(BLangFunction funcNode) {
        if (funcNode.attachedOuterFunction) {
            // object outer attached function must have a body
            if (funcNode.body == null) {
                dlog.error(funcNode.pos, DiagnosticCode.ATTACHED_FUNCTIONS_MUST_HAVE_BODY, funcNode.name);
            }

            if (Symbols.isFlagOn(funcNode.receiver.type.tsymbol.flags, Flags.ABSTRACT)) {
                dlog.error(funcNode.pos, DiagnosticCode.CANNOT_ATTACH_FUNCTIONS_TO_ABSTRACT_OBJECT, funcNode.name,
                        funcNode.receiver.type);
            }

            return;
        }

        if (!funcNode.attachedFunction) {
            return;
        }

        // If the function is attached to an abstract object, it don't need to have an implementation.
        if (Symbols.isFlagOn(funcNode.receiver.type.tsymbol.flags, Flags.ABSTRACT)) {
            if (funcNode.body != null) {
                dlog.error(funcNode.pos, DiagnosticCode.ABSTRACT_OBJECT_FUNCTION_CANNOT_HAVE_BODY, funcNode.name,
                        funcNode.receiver.type);
            }
            return;
        }

        // There must be an implementation at the outer level, if the function is an interface.
        if (funcNode.interfaceFunction && !env.enclPkg.objAttachedFunctions.contains(funcNode.symbol)) {
            dlog.error(funcNode.pos, DiagnosticCode.INVALID_INTERFACE_ON_NON_ABSTRACT_OBJECT, funcNode.name,
                    funcNode.receiver.type);
        }
    }

    private void validateReferencedFunction(DiagnosticPos pos, BAttachedFunction func, SymbolEnv env) {
        if (Symbols.isFlagOn(func.symbol.receiverSymbol.type.tsymbol.flags, Flags.ABSTRACT)) {
            return;
        }

        if (!Symbols.isFlagOn(func.symbol.flags, Flags.INTERFACE)) {
            return;
        }

        // There must be an implementation at the outer level, if the function is an interface.
        if (!env.enclPkg.objAttachedFunctions.contains(func.symbol)) {
            dlog.error(pos, DiagnosticCode.INVALID_INTERFACE_ON_NON_ABSTRACT_OBJECT, func.funcName,
                    func.symbol.receiverSymbol.type);
        }
    }

    private void addElseTypeGuards(BLangIf ifNode) {
        SymbolEnv elseEnv = SymbolEnv.createBlockEnv((BLangBlockStmt) ifNode.elseStmt, env);
        for (Entry<BVarSymbol, Set<BType>> entry : this.typeGuards.entrySet()) {
            BVarSymbol originalVarSymbol = entry.getKey();
            BType remainingType = getRemainingType(originalVarSymbol.type, entry.getValue());
            BVarSymbol varSymbol = new BVarSymbol(0, originalVarSymbol.name, elseEnv.scope.owner.pkgID, remainingType,
                    this.env.scope.owner);
            symbolEnter.defineShadowedSymbol(ifNode.expr.pos, varSymbol, elseEnv);

            // Cache the type guards, to be reused at the desugar.
            ifNode.elseTypeGuards.put(originalVarSymbol, varSymbol);
        }
    }

    private void addTypeGuards(Map<BVarSymbol, BType> typeGuards) {
        if (this.typeGuards == null) {
            this.typeGuards = new HashMap<>();
            this.typeGuards = typeGuards.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet() {
                        { add(e.getValue()); }
                    }));
            return;
        }

        for (Entry<BVarSymbol, BType> entry : typeGuards.entrySet()) {
            Set<BType> typGuardsForSymbol = this.typeGuards.get(entry.getKey());
            if (typGuardsForSymbol == null) {
                typGuardsForSymbol = new HashSet<>();
                this.typeGuards.put(entry.getKey(), typGuardsForSymbol);
            }

            typGuardsForSymbol.add(entry.getValue());
        }
    }

    private void resetTypeGards() {
        this.typeGuards = null;
    }

    private BType getRemainingType(BType originalType, Set<BType> set) {
        if (originalType.tag != TypeTags.UNION) {
            return originalType;
        }

        List<BType> memberTypes = new ArrayList<>(((BUnionType) originalType).getMemberTypes());

        for (BType removeType : set) {
            if (removeType.tag != TypeTags.UNION) {
                memberTypes.remove(removeType);
            } else {
                ((BUnionType) removeType).getMemberTypes().forEach(type -> memberTypes.remove(type));
            }

            if (memberTypes.size() == 1) {
                return memberTypes.get(0);
            }
        }

        return new BUnionType(null, new HashSet<>(memberTypes), memberTypes.contains(symTable.nilType));
    }
}

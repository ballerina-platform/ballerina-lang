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

import io.ballerina.compiler.api.symbols.DiagnosticState;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.ballerinalang.util.diagnostic.DiagnosticWarningCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEnumSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangResourcePathSegment;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
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
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangRestBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangSimpleBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangWildCardBindingPattern;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangListConstructorSpreadOpExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef.BLangRecordVarRefKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangValueExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.NumericLiteralSupport;
import org.wso2.ballerinalang.compiler.util.TypeDefBuilderHelper;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.Unifier;
import org.wso2.ballerinalang.util.AttachPoints;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.ballerinalang.model.symbols.SymbolOrigin.COMPILED_SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.ballerinalang.model.tree.NodeKind.FUNCTION;
import static org.ballerinalang.model.tree.NodeKind.LITERAL;
import static org.ballerinalang.model.tree.NodeKind.NUMERIC_LITERAL;
import static org.ballerinalang.model.tree.NodeKind.RECORD_LITERAL_EXPR;

/**
 * @since 0.94
 */
public class SemanticAnalyzer extends SimpleBLangNodeAnalyzer<SemanticAnalyzer.AnalyzerData> {

    private static final CompilerContext.Key<SemanticAnalyzer> SYMBOL_ANALYZER_KEY = new CompilerContext.Key<>();
    private static final String LISTENER_NAME = "listener";

    private final BLangAnonymousModelHelper anonModelHelper;
    private final ConstantAnalyzer constantAnalyzer;
    private final ConstantValueResolver constantValueResolver;
    private final BLangDiagnosticLog dlog;
    private final Names names;
    private final SymbolEnter symbolEnter;
    private final SymbolResolver symResolver;
    private final SymbolTable symTable;
    private final TypeChecker typeChecker;
    private final TypeNarrower typeNarrower;
    private final Types types;
    private final Unifier unifier;
    private final Stack<String> anonTypeNameSuffixes;

    public static SemanticAnalyzer getInstance(CompilerContext context) {
        SemanticAnalyzer semAnalyzer = context.get(SYMBOL_ANALYZER_KEY);
        if (semAnalyzer == null) {
            semAnalyzer = new SemanticAnalyzer(context);
        }

        return semAnalyzer;
    }

    private SemanticAnalyzer(CompilerContext context) {
        context.put(SYMBOL_ANALYZER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.typeNarrower = TypeNarrower.getInstance(context);
        this.constantAnalyzer = ConstantAnalyzer.getInstance(context);
        this.constantValueResolver = ConstantValueResolver.getInstance(context);
        this.anonModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.unifier = new Unifier();
        this.anonTypeNameSuffixes = new Stack<>();
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        final AnalyzerData data = new AnalyzerData(pkgEnv);
        visitNode(pkgNode, data);
        return pkgNode;
    }

    // Visitor methods
    @Override
    public void visit(BLangPackage pkgNode, AnalyzerData data) {
        this.dlog.setCurrentPackageId(pkgNode.packageID);
        if (pkgNode.completedPhases.contains(CompilerPhase.TYPE_CHECK)) {
            return;
        }
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        data.env = pkgEnv;

        // To keep track of original top level nodes to resolve user defined types independent of the assumptions that
        // new elements are added to the end of the list and the data structure is always going to be sequential.
        List<TopLevelNode> copyOfOriginalTopLevelNodes = new ArrayList<>(pkgNode.topLevelNodes);

        // Visit constants first.
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;
        for (int i = 0; i < topLevelNodes.size(); i++) {
            TopLevelNode constant = topLevelNodes.get(i);
            if (constant.getKind() == NodeKind.CONSTANT) {
                analyzeNode((BLangNode) constant, data);
            }
        }
        this.constantValueResolver.resolve(pkgNode.constants, pkgNode.packageID, pkgEnv);

        validateEnumMemberMetadata(pkgNode.constants);

        // Then resolve user defined types without analyzing type definitions that get added while analyzing other nodes
        for (int i = 0; i < copyOfOriginalTopLevelNodes.size(); i++)  {
            if (copyOfOriginalTopLevelNodes.get(i).getKind() == NodeKind.TYPE_DEFINITION) {
                analyzeNode((BLangNode) copyOfOriginalTopLevelNodes.get(i), data);
            }
        }

        for (int i = 0; i < pkgNode.topLevelNodes.size(); i++) {
            TopLevelNode pkgLevelNode = pkgNode.topLevelNodes.get(i);
            NodeKind kind = pkgLevelNode.getKind();
            if (kind == NodeKind.CONSTANT) {
                continue;
            }

            if (kind == FUNCTION) {
                BLangFunction blangFunction = (BLangFunction) pkgLevelNode;
                if (blangFunction.flagSet.contains(Flag.LAMBDA) || blangFunction.flagSet.contains(Flag.ATTACHED)) {
                    continue;
                }
            }

            if (kind == NodeKind.CLASS_DEFN && ((BLangClassDefinition) pkgLevelNode).isObjectContructorDecl) {
                // This is a class defined for an object-constructor-expression (OCE). This will be analyzed when
                // visiting the OCE in the type checker. This is a temporary workaround until we fix
                // https://github.com/ballerina-platform/ballerina-lang/issues/27009
                continue;
            }

            // To skip already analyzed type definitions and analyze the ones that get added while analyzing other nodes
            if (kind == NodeKind.TYPE_DEFINITION && copyOfOriginalTopLevelNodes.contains(pkgLevelNode)) {
                continue;
            }

            analyzeNode((BLangNode) pkgLevelNode, data);
        }
        analyzeModuleConfigurableAmbiguity(pkgNode);

        while (pkgNode.lambdaFunctions.peek() != null) {
            BLangLambdaFunction lambdaFunction = pkgNode.lambdaFunctions.poll();
            BLangFunction function = lambdaFunction.function;
            lambdaFunction.setBType(function.symbol.type);
            data.env = lambdaFunction.capturedClosureEnv;
            analyzeNode(lambdaFunction.function, data);
        }

        pkgNode.getTestablePkgs().forEach(testablePackage -> visit((BLangPackage) testablePackage, data));
        pkgNode.completedPhases.add(CompilerPhase.TYPE_CHECK);
    }

    private void validateEnumMemberMetadata(List<BLangConstant> constants) {
        Map<String, List<BLangConstant>> duplicateEnumMembersWithMetadata = new HashMap<>();

        for (BLangConstant constant : constants) {
            if (!constant.flagSet.contains(Flag.ENUM_MEMBER) ||
                    (constant.markdownDocumentationAttachment == null && constant.annAttachments.isEmpty())) {
                continue;
            }

            String name = constant.name.value;

            if (duplicateEnumMembersWithMetadata.containsKey(name)) {
                duplicateEnumMembersWithMetadata.get(name).add(constant);
                continue;
            }

            duplicateEnumMembersWithMetadata.put(name, new ArrayList<>() {{ add(constant); }});
        }

        for (Map.Entry<String, List<BLangConstant>> entry : duplicateEnumMembersWithMetadata.entrySet()) {
            List<BLangConstant> duplicateMembers = entry.getValue();

            if (duplicateMembers.size() == 1) {
                continue;
            }

            for (BLangConstant duplicateMember : duplicateMembers) {
                dlog.warning(duplicateMember.pos, DiagnosticWarningCode.INVALID_METADATA_ON_DUPLICATE_ENUM_MEMBER);
            }
        }
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        xmlnsNode.setBType(symTable.stringType);

        // Namespace node already having the symbol means we are inside an init-function,
        // and the symbol has already been declared by the original statement.
        if (xmlnsNode.symbol == null) {
            symbolEnter.defineNode(xmlnsNode, currentEnv);
        }

        typeChecker.checkExpr(xmlnsNode.namespaceURI, currentEnv, symTable.stringType, data.prevEnvs,
                data.commonAnalyzerData);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode, AnalyzerData data) {
        analyzeNode(xmlnsStmtNode.xmlnsDecl, data);
    }

    @Override
    public void visit(BLangResourceFunction funcNode, AnalyzerData data) {
        visit((BLangFunction) funcNode, data);
        BType returnType = funcNode.returnTypeNode.getBType();
        if (containsClientObjectTypeOrFunctionType(returnType)) {
            dlog.error(funcNode.returnTypeNode.getPosition(), DiagnosticErrorCode.INVALID_RESOURCE_METHOD_RETURN_TYPE);
        }

        List<BLangResourcePathSegment> resourcePathSegments = funcNode.resourcePathSegments;
        int pathSegmentCount = resourcePathSegments.size();
        BLangResourcePathSegment lastPathSegment = resourcePathSegments.get(resourcePathSegments.size() - 1);
        if (lastPathSegment.kind == NodeKind.RESOURCE_ROOT_PATH_SEGMENT) {
            return;
        }
        
        if (lastPathSegment.kind == NodeKind.RESOURCE_PATH_REST_PARAM_SEGMENT) {
            if (!types.isAssignable(lastPathSegment.getBType(), symTable.pathParamAllowedType)) {
                dlog.error(lastPathSegment.typeNode.getPosition(), DiagnosticErrorCode.UNSUPPORTED_REST_PATH_PARAM_TYPE,
                        lastPathSegment.getBType());
            }
            pathSegmentCount--;
        }

        if (pathSegmentCount > 0) {
            resourcePathSegments.subList(0, pathSegmentCount).stream()
                    .filter(pathSeg -> !types.isAssignable(pathSeg.typeNode.getBType(), symTable.pathParamAllowedType))
                    .forEach(pathSeg ->
                            dlog.error(pathSeg.typeNode.getPosition(), DiagnosticErrorCode.UNSUPPORTED_PATH_PARAM_TYPE,
                                    pathSeg.getBType()));
        }
    }

    private boolean containsClientObjectTypeOrFunctionType(BType type) {
        BType referredType = Types.getReferredType(type);
        if (referredType != symTable.semanticError && Symbols.isFlagOn(referredType.tsymbol.flags, Flags.CLIENT)) {
            return true;
        }
        switch (referredType.tag) {
            case TypeTags.INVOKABLE:
                return true;
            case TypeTags.UNION:
                for (BType memberType: ((BUnionType) referredType).getMemberTypes()) {
                    if (containsClientObjectTypeOrFunctionType(memberType)) {
                        return true;
                    }
                }
                break;
            case TypeTags.INTERSECTION:
                for (BType memberType: ((BIntersectionType) referredType).getConstituentTypes()) {
                    if (containsClientObjectTypeOrFunctionType(memberType)) {
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    @Override
    public void visit(BLangFunction funcNode, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, currentEnv);

        // TODO: Shouldn't this be done in symbol enter?
        //set function param flag to final
        funcNode.symbol.params.forEach(param -> param.flags |= Flags.FUNCTION_FINAL);

        if (!funcNode.flagSet.contains(Flag.WORKER)) {
            // annotation validation for workers is done for the invocation.
            funcNode.annAttachments.forEach(annotationAttachment -> {
                if (Symbols.isFlagOn(funcNode.symbol.flags, Flags.REMOTE) && funcNode.receiver != null
                        && Symbols.isService(funcNode.receiver.symbol)) {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.SERVICE_REMOTE);
                } else if (funcNode.attachedFunction) {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.OBJECT_METHOD);
                }
                annotationAttachment.attachPoints.add(AttachPoint.Point.FUNCTION);
                data.env = funcEnv;
                analyzeNode(annotationAttachment, data);
            });
            validateAnnotationAttachmentCount(funcNode.annAttachments);
        }
        ((List<BAnnotationAttachmentSymbol>) funcNode.symbol.getAnnotations()).addAll(
                getAnnotationAttachmentSymbols(funcNode.annAttachments));

        BLangType returnTypeNode = funcNode.returnTypeNode;
        boolean hasReturnType = returnTypeNode != null;
        if (hasReturnType) {
            funcNode.returnTypeAnnAttachments.forEach(annotationAttachment -> {
                annotationAttachment.attachPoints.add(AttachPoint.Point.RETURN);
                data.env = funcEnv;
                analyzeNode(annotationAttachment, data);
            });
            validateAnnotationAttachmentCount(funcNode.returnTypeAnnAttachments);
            ((BInvokableTypeSymbol) funcNode.symbol.type.tsymbol).returnTypeAnnots.addAll(
                    getAnnotationAttachmentSymbols(funcNode.returnTypeAnnAttachments));
        }

        boolean inIsolatedFunction = funcNode.flagSet.contains(Flag.ISOLATED);
        SymbolEnv clonedEnv =  funcNode.clonedEnv;

        for (BLangSimpleVariable param : funcNode.requiredParams) {
            symbolEnter.defineExistingVarSymbolInEnv(param.symbol, clonedEnv);
            data.env = clonedEnv;
            analyzeNode(param, data);

            BLangExpression expr = param.expr;
            if (expr != null) {
                funcNode.symbol.paramDefaultValTypes.put(param.symbol.name.value, expr.getBType());
            }

            validateIsolatedParamUsage(inIsolatedFunction, param, false, data);
        }

        BLangSimpleVariable restParam = funcNode.restParam;
        if (restParam != null) {
            symbolEnter.defineExistingVarSymbolInEnv(restParam.symbol, clonedEnv);
            data.env = clonedEnv;
            analyzeNode(restParam, data);
            validateIsolatedParamUsage(inIsolatedFunction, restParam, true, data);
        }

        if (hasReturnType && Symbols.isFlagOn(returnTypeNode.getBType().flags, Flags.PARAMETERIZED)) {
            unifier.validate(returnTypeNode.getBType(), funcNode, symTable, currentEnv, types, dlog);
        }

        validateObjectAttachedFunction(funcNode, data);

        if (funcNode.hasBody()) {
            data.env = funcEnv;
            this.anonTypeNameSuffixes.push(funcNode.name.value);
            analyzeNode(funcNode.body, returnTypeNode.getBType(), data);
            this.anonTypeNameSuffixes.pop();
        }

        if (funcNode.anonForkName != null) {
            funcNode.symbol.enclForkName = funcNode.anonForkName;
        }
    }

    @Override
    public void visit(BLangBlockFunctionBody body, AnalyzerData data) {
        SymbolEnv funcBodyEnv = SymbolEnv.createFuncBodyEnv(body, data.env);
        int stmtCount = -1;
        for (BLangStatement stmt : body.stmts) {
            stmtCount++;
            boolean analyzedStmt = analyzeBlockStmtFollowingIfWithoutElse(stmt,
                    stmtCount > 0 ? body.stmts.get(stmtCount - 1) : null, funcBodyEnv, data);
            if (analyzedStmt) {
                continue;
            }
            data.env = funcBodyEnv;
            analyzeStmt(stmt, data);
        }
        // Remove explicitly added function body env if exists
        data.prevEnvs.remove(funcBodyEnv);
        resetNotCompletedNormally(data);
    }

    private boolean analyzeBlockStmtFollowingIfWithoutElse(BLangStatement currentStmt, BLangStatement prevStatement,
                                                           SymbolEnv currentEnv, AnalyzerData data) {
        if (currentStmt.getKind() == NodeKind.BLOCK && prevStatement != null && prevStatement.getKind() == NodeKind.IF
                && ((BLangIf) prevStatement).elseStmt == null && data.notCompletedNormally) {
            BLangIf ifStmt = (BLangIf) prevStatement;
            data.notCompletedNormally =
                    ConditionResolver.checkConstCondition(types, symTable, ifStmt.expr) == symTable.trueType;
            // Explicitly add block env since it's required for resetting the types
            data.prevEnvs.push(currentEnv);
            // Types are narrowed following an `if` statement without an `else`, if it's not completed normally.
            data.env = typeNarrower.evaluateFalsity(ifStmt.expr, currentStmt, currentEnv, false);
            analyzeStmt(currentStmt, data);
            data.prevEnvs.pop();
            return true;
        }
        return false;
    }

    @Override
    public void visit(BLangExprFunctionBody body, AnalyzerData data) {
        SymbolEnv env = SymbolEnv.createFuncBodyEnv(body, data.env);
        typeChecker.checkExpr(body.expr, env, data.expType, data.prevEnvs, data.commonAnalyzerData);
    }

    @Override
    public void visit(BLangExternalFunctionBody body, AnalyzerData data) {
        // TODO: Check if a func body env is needed
        for (BLangAnnotationAttachment annotationAttachment : body.annAttachments) {
            annotationAttachment.attachPoints.add(AttachPoint.Point.EXTERNAL);
            this.anonTypeNameSuffixes.push(annotationAttachment.annotationName.value);
            analyzeNode(annotationAttachment, data);
            this.anonTypeNameSuffixes.pop();
        }
        validateAnnotationAttachmentCount(body.annAttachments);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition, AnalyzerData data) {
        analyzeNode(typeDefinition.typeNode, data);

        final List<BAnnotationAttachmentSymbol> annotSymbols = new ArrayList<>();

        typeDefinition.annAttachments.forEach(annotationAttachment -> {
            if (typeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
                annotationAttachment.attachPoints.add(AttachPoint.Point.OBJECT);
            }
            annotationAttachment.attachPoints.add(AttachPoint.Point.TYPE);

            annotationAttachment.accept(this, data);

            BAnnotationAttachmentSymbol annotationAttachmentSymbol = annotationAttachment.annotationAttachmentSymbol;
            if (annotationAttachmentSymbol != null) {
                annotSymbols.add(annotationAttachmentSymbol);
            }
        });

        BSymbol typeDefSym = typeDefinition.symbol;
        if (typeDefSym != null && typeDefSym.kind == SymbolKind.TYPE_DEF) {
            ((List<BAnnotationAttachmentSymbol>) ((BTypeDefinitionSymbol) typeDefSym).getAnnotations()).addAll(
                    annotSymbols);
        }

        if (typeDefinition.flagSet.contains(Flag.ENUM)) {
            ((BEnumSymbol) typeDefSym).addAnnotations(annotSymbols);
            HashSet<String> enumElements = new HashSet<String>();
            BLangUnionTypeNode bLangUnionTypeNode = (BLangUnionTypeNode)  typeDefinition.typeNode;
            for (int j = bLangUnionTypeNode.memberTypeNodes.size() - 1; j >= 0; j--) {
                BLangUserDefinedType nextType = (BLangUserDefinedType) bLangUnionTypeNode.memberTypeNodes.get(j);
                String nextTypeName = nextType.typeName.value;
                if (enumElements.contains(nextTypeName)) {
                    dlog.error(nextType.pos, DiagnosticErrorCode.REDECLARED_SYMBOL, nextTypeName);
                } else {
                    enumElements.add(nextTypeName);
                }
            }
        }

        validateAnnotationAttachmentCount(typeDefinition.annAttachments);
        validateBuiltinTypeAnnotationAttachment(typeDefinition.annAttachments, data);
    }

    @Override
    public void visit(BLangClassDefinition classDefinition, AnalyzerData data) {
        // Apply service attachpoint when this is a class representing a service-decl or object-ctor with service prefix
        AttachPoint.Point attachedPoint;
        Set<Flag> flagSet = classDefinition.flagSet;
        if (flagSet.contains(Flag.OBJECT_CTOR) && flagSet.contains(Flag.SERVICE)) {
            attachedPoint = AttachPoint.Point.SERVICE;
        } else {
            attachedPoint = AttachPoint.Point.CLASS;
        }

        BClassSymbol symbol = (BClassSymbol) classDefinition.symbol;

        classDefinition.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoints.add(attachedPoint);
            annotationAttachment.accept(this, data);

            BAnnotationAttachmentSymbol annotationAttachmentSymbol = annotationAttachment.annotationAttachmentSymbol;
            if (annotationAttachmentSymbol != null) {
                symbol.addAnnotation(annotationAttachmentSymbol);
            }
        });
        validateAnnotationAttachmentCount(classDefinition.annAttachments);

        analyzeClassDefinition(classDefinition, data);

        BType type = classDefinition.getBType();
        List<BLangType> inclusions = classDefinition.typeRefs;

        validateInclusions(flagSet, inclusions, false, Symbols.isFlagOn(type.tsymbol.flags, Flags.ANONYMOUS));
        validateTypesOfOverriddenFields(type, classDefinition.fields, inclusions);
    }

    private void analyzeClassDefinition(BLangClassDefinition classDefinition, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        SymbolEnv classEnv = SymbolEnv.createClassEnv(classDefinition, classDefinition.symbol.scope, currentEnv);
        for (BLangSimpleVariable field : classDefinition.fields) {
            data.env = classEnv;
            analyzeNode(field, data);
        }
        data.env = currentEnv;
        // Visit functions as they are not in the same scope/env as the object fields
        for (BLangFunction function : classDefinition.functions) {
            analyzeNode(function, data);
            if (!classDefinition.flagSet.contains(Flag.CLIENT) && function.flagSet.contains(Flag.RESOURCE) &&
                    function.flagSet.contains(Flag.NATIVE)) {
                this.dlog.error(function.pos,
                        DiagnosticErrorCode.SERVICE_RESOURCE_METHOD_CANNOT_BE_EXTERN, function.name);
            }
        }

        DiagnosticErrorCode code;
        if (classDefinition.isServiceDecl) {
            code = DiagnosticErrorCode.UNIMPLEMENTED_REFERENCED_METHOD_IN_SERVICE_DECL;
        } else if ((classDefinition.symbol.flags & Flags.OBJECT_CTOR) == Flags.OBJECT_CTOR) {
            code = DiagnosticErrorCode.UNIMPLEMENTED_REFERENCED_METHOD_IN_OBJECT_CTOR;
        } else {
            code = DiagnosticErrorCode.UNIMPLEMENTED_REFERENCED_METHOD_IN_CLASS;
        }

        // Validate the referenced functions that don't have implementations within the function.
        for (BAttachedFunction func : ((BObjectTypeSymbol) classDefinition.symbol).referencedFunctions) {
            validateReferencedFunction(classDefinition.pos, func, currentEnv, code);
        }

        analyzerClassInitMethod(classDefinition, data);
    }

    private void analyzerClassInitMethod(BLangClassDefinition classDefinition, AnalyzerData data) {
        if (classDefinition.initFunction == null) {
            return;
        }

        if (classDefinition.initFunction.flagSet.contains(Flag.PRIVATE)) {
            this.dlog.error(classDefinition.initFunction.pos, DiagnosticErrorCode.PRIVATE_OBJECT_CONSTRUCTOR,
                    classDefinition.symbol.name);
            return;
        }

        if (classDefinition.initFunction.flagSet.contains(Flag.NATIVE)) {
            this.dlog.error(classDefinition.initFunction.pos, DiagnosticErrorCode.OBJECT_INIT_FUNCTION_CANNOT_BE_EXTERN,
                    classDefinition.symbol.name);
            return;
        }

        analyzeNode(classDefinition.initFunction, data);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr, AnalyzerData data) {
        conversionExpr.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoints.add(AttachPoint.Point.TYPE);
            if (conversionExpr.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
                annotationAttachment.attachPoints.add(AttachPoint.Point.OBJECT);
            }

            annotationAttachment.accept(this, data);
        });
        validateAnnotationAttachmentCount(conversionExpr.annAttachments);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode, AnalyzerData data) {
        boolean foundUnaryExpr = false;
        boolean isErroredExprInFiniteType = false;
        NodeKind valueKind;
        BLangExpression value;

        for (int i = 0; i < finiteTypeNode.valueSpace.size(); i++) {
            value = finiteTypeNode.valueSpace.get(i);
            valueKind = value.getKind();

            if (valueKind == NodeKind.UNARY_EXPR) {
                foundUnaryExpr = true;
                BType resultType = typeChecker.checkExpr(value, data.env, symTable.noType, data.prevEnvs);
                if (resultType == symTable.semanticError) {
                    isErroredExprInFiniteType = true;
                }
                // Replacing unary expression with numeric literal type for + and - numeric values
                BLangNumericLiteral newNumericLiteral =
                        Types.constructNumericLiteralFromUnaryExpr((BLangUnaryExpr) value);
                finiteTypeNode.valueSpace.set(i, newNumericLiteral);
            } else if ((valueKind == NodeKind.LITERAL || valueKind == NodeKind.NUMERIC_LITERAL) &&
                    ((BLangLiteral) value).originalValue == null) {
                // To handle enums when the visit is being called from symbol resolver
                continue;
            } else {
                analyzeNode(value, data);
            }
        }

        if (foundUnaryExpr && isErroredExprInFiniteType) {
            finiteTypeNode.setBType(symTable.semanticError);
        }
    }

    @Override
    public void visit(BLangLiteral literalExpr, AnalyzerData data) {
        if (literalExpr.getKind() == NodeKind.NUMERIC_LITERAL) {
            NodeKind kind = ((BLangNumericLiteral) literalExpr).kind;
            if (kind == NodeKind.HEX_FLOATING_POINT_LITERAL ||
                    NumericLiteralSupport.isFloatDiscriminated(literalExpr.originalValue)) {
                types.validateFloatLiteral(literalExpr.pos, String.valueOf(literalExpr.value));
            }
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode, AnalyzerData data) {
        SymbolEnv objectEnv = SymbolEnv.createTypeEnv(objectTypeNode, objectTypeNode.symbol.scope, data.env);

        objectTypeNode.fields.forEach(field -> {
            data.env = objectEnv;
            analyzeNode(field, data);
            if (field.flagSet.contains(Flag.PRIVATE)) {
                this.dlog.error(field.pos, DiagnosticErrorCode.PRIVATE_FIELD_ABSTRACT_OBJECT, field.symbol.name);
            }
        });

        // Visit functions as they are not in the same scope/env as the object fields
        objectTypeNode.functions.forEach(func -> {
            analyzeNode(func, data);
            if (func.flagSet.contains(Flag.PRIVATE)) {
                this.dlog.error(func.pos, DiagnosticErrorCode.PRIVATE_FUNC_ABSTRACT_OBJECT, func.name,
                        objectTypeNode.symbol.name);
            }
            if (func.flagSet.contains(Flag.NATIVE)) {
                this.dlog.error(func.pos, DiagnosticErrorCode.EXTERN_FUNC_ABSTRACT_OBJECT, func.name,
                        objectTypeNode.symbol.name);
            }
            if (!objectTypeNode.flagSet.contains(Flag.CLIENT) && func.flagSet.contains(Flag.RESOURCE) &&
                    func.flagSet.contains(Flag.NATIVE)) {
                this.dlog.error(func.pos, DiagnosticErrorCode.SERVICE_RESOURCE_METHOD_CANNOT_BE_EXTERN, func.name);
            }
        });

        validateInclusions(objectTypeNode.flagSet, objectTypeNode.typeRefs, true, false);
        validateTypesOfOverriddenFields(objectTypeNode);

        if (objectTypeNode.initFunction == null) {
            return;
        }

        this.dlog.error(objectTypeNode.initFunction.pos, DiagnosticErrorCode.INIT_METHOD_IN_OBJECT_TYPE_DESCRIPTOR,
                objectTypeNode.symbol.name);
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint keyTypeConstraint, AnalyzerData data) {
        analyzeNode(keyTypeConstraint.keyType, data);
    }

    @Override
    public void visit(BLangTableTypeNode tableTypeNode, AnalyzerData data) {
        analyzeNode(tableTypeNode.constraint, data);
        if (tableTypeNode.tableKeyTypeConstraint != null) {
            analyzeNode(tableTypeNode.tableKeyTypeConstraint, data);
        }
        BType constraint = Types.getReferredType(tableTypeNode.constraint.getBType());
        if (!types.isAssignable(constraint, symTable.mapAllType)) {
            dlog.error(tableTypeNode.constraint.pos, DiagnosticErrorCode.TABLE_CONSTRAINT_INVALID_SUBTYPE, constraint);
            return;
        }

        if (constraint.tag == TypeTags.MAP) {
            typeChecker.validateMapConstraintTable(tableTypeNode.tableType);
            return;
        }

        List<String> fieldNameList = tableTypeNode.tableType.fieldNameList;
        if (!fieldNameList.isEmpty()) {
            typeChecker.validateKeySpecifier(fieldNameList,
                    constraint.tag != TypeTags.INTERSECTION ? constraint :
                            ((BIntersectionType) constraint).effectiveType,
                    tableTypeNode.tableKeySpecifier.pos);
        }

        analyzeNode(tableTypeNode.constraint, data);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode, AnalyzerData data) {
        if (recordTypeNode.analyzed) {
            return;
        }
        SymbolEnv recordEnv = SymbolEnv.createTypeEnv(recordTypeNode, recordTypeNode.symbol.scope, data.env);

        BType type = Types.getReferredType(recordTypeNode.getBType());

        boolean isRecordType = false;
        LinkedHashMap<String, BField> fields = null;

        boolean allReadOnlyFields = false;

        if (type.tag == TypeTags.RECORD) {
            isRecordType = true;

            BRecordType recordType = (BRecordType) type;
            fields = recordType.fields;
            allReadOnlyFields = recordType.sealed ||
                    Types.getReferredType(recordType.restFieldType).tag == TypeTags.NEVER;
        }

        List<BLangSimpleVariable> recordFields = new ArrayList<>(recordTypeNode.fields);
        recordFields.addAll(recordTypeNode.includedFields);

        for (BLangSimpleVariable field : recordFields) {
            if (field.flagSet.contains(Flag.READONLY)) {
                handleReadOnlyField(isRecordType, fields, field, data);
            } else {
                allReadOnlyFields = false;
            }

            data.env = recordEnv;
            analyzeNode(field, data);
        }

        if (isRecordType && allReadOnlyFields) {
            type.tsymbol.flags |= Flags.READONLY;
            type.flags |= Flags.READONLY;
        }

        validateDefaultable(recordTypeNode);
        validateTypesOfOverriddenFields(recordTypeNode);
        recordTypeNode.analyzed = true;
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        data.env = SymbolEnv.createTypeEnv(functionTypeNode, functionTypeNode.getBType().tsymbol.scope, currentEnv);
        for (BLangVariable param : functionTypeNode.params) {
            analyzeNode(param, data);
        }
        if (functionTypeNode.restParam != null) {
            analyzeNode(functionTypeNode.restParam.typeNode, data);
        }
        if (functionTypeNode.returnTypeNode != null) {
            analyzeNode(functionTypeNode.returnTypeNode, data);
        }
        functionTypeNode.analyzed = true;
    }

    @Override
    public void visit(BLangErrorType errorType, AnalyzerData data) {
        if (errorType.detailType == null) {
            return;
        }

        BType detailType = errorType.detailType.getBType();
        if (detailType != null && !types.isValidErrorDetailType(detailType)) {
            dlog.error(errorType.detailType.pos, DiagnosticErrorCode.INVALID_ERROR_DETAIL_TYPE, errorType.detailType,
                    symTable.detailType);
        }
        analyzeNode(errorType.detailType, data);
    }

    @Override
    public void visit(BLangConstrainedType constrainedType, AnalyzerData data) {
        analyzeNode(constrainedType.constraint, data);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode, AnalyzerData data) {
        for (BLangType memberType : unionTypeNode.memberTypeNodes) {
            analyzeNode(memberType, data);
        }
    }

    @Override
    public void visit(BLangStreamType streamType, AnalyzerData data) {
        analyzeNode(streamType.constraint, data);
        if (streamType.error != null) {
            analyzeNode(streamType.error, data);
        }
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode, AnalyzerData data) {
        for (BLangType constituentTypeNode : intersectionTypeNode.constituentTypeNodes) {
            analyzeNode(constituentTypeNode, data);
        }
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode, AnalyzerData data) {
        List<BLangType> memberTypeNodes = tupleTypeNode.memberTypeNodes;
        for (BLangType memType : memberTypeNodes) {
            analyzeNode(memType, data);
        }
        if (tupleTypeNode.restParamType != null) {
            analyzeNode(tupleTypeNode.restParamType, data);
        }
    }

    @Override
    public void visit(BLangArrayType arrayType, AnalyzerData data) {
        analyzeNode(arrayType.elemtype, data);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangValueType valueType, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangAnnotation annotationNode, AnalyzerData data) {
        BAnnotationSymbol symbol = (BAnnotationSymbol) annotationNode.symbol;
        annotationNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoints.add(AttachPoint.Point.ANNOTATION);
            annotationAttachment.accept(this, data);

            BAnnotationAttachmentSymbol annotationAttachmentSymbol = annotationAttachment.annotationAttachmentSymbol;
            if (annotationAttachmentSymbol != null) {
                symbol.addAnnotation(annotationAttachmentSymbol);
            }
        });
        validateAnnotationAttachmentCount(annotationNode.annAttachments);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode, AnalyzerData data) {
        BSymbol symbol = this.symResolver.resolveAnnotation(annAttachmentNode.pos, data.env,
                names.fromString(annAttachmentNode.pkgAlias.getValue()),
                names.fromString(annAttachmentNode.getAnnotationName().getValue()));
        if (symbol == this.symTable.notFoundSymbol) {
            this.dlog.error(annAttachmentNode.pos, DiagnosticErrorCode.UNDEFINED_ANNOTATION,
                    annAttachmentNode.getAnnotationName().getValue());
            return;
        }
        // Validate Attachment Point against the Annotation Definition.
        BAnnotationSymbol annotationSymbol = (BAnnotationSymbol) symbol;
        annAttachmentNode.annotationSymbol = annotationSymbol;
        if (annotationSymbol.maskedPoints > 0 &&
                !Symbols.isAttachPointPresent(annotationSymbol.maskedPoints,
                                              AttachPoints.asMask(annAttachmentNode.attachPoints))) {
            String msg = annAttachmentNode.attachPoints.stream()
                    .map(point -> point.name().toLowerCase())
                    .collect(Collectors.joining(", "));
            this.dlog.error(annAttachmentNode.pos, DiagnosticErrorCode.ANNOTATION_NOT_ALLOWED, annotationSymbol, msg);
        }
        // Validate Annotation Attachment expression against Annotation Definition type.
        validateAnnotationAttachmentExpr(annAttachmentNode, annotationSymbol, data);
        symResolver.populateAnnotationAttachmentSymbol(annAttachmentNode, data.env, this.constantValueResolver,
                this.anonTypeNameSuffixes);
    }

    @Override
    public void visit(BLangSimpleVariable varNode, AnalyzerData data) {
        boolean configurable = isConfigurable(varNode);

        if (varNode.isDeclaredWithVar) {
            // Configurable variable cannot be declared with var
            if (configurable) {
                dlog.error(varNode.pos, DiagnosticErrorCode.CONFIGURABLE_VARIABLE_CANNOT_BE_DECLARED_WITH_VAR);
            }
            validateWorkerAnnAttachments(varNode.expr, data);
            handleDeclaredWithVar(varNode, data);
            transferForkFlag(varNode);
            return;
        }
        SymbolEnv currentEnv = data.env;
        long ownerSymTag = currentEnv.scope.owner.tag;
        boolean isListenerDecl = varNode.flagSet.contains(Flag.LISTENER);
        if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE || (ownerSymTag & SymTag.LET) == SymTag.LET
                || currentEnv.node.getKind() == NodeKind.LET_CLAUSE) {
            // This is a variable declared in a function, let expression, an action or a resource
            // If the variable is parameter then the variable symbol is already defined
            if (varNode.symbol == null) {
                analyzeVarNode(varNode, data, AttachPoint.Point.VAR);
            } else {
                analyzeVarNode(varNode, data, AttachPoint.Point.PARAMETER);
            }
        } else if ((ownerSymTag & SymTag.OBJECT) == SymTag.OBJECT) {
            analyzeVarNode(varNode, data, AttachPoint.Point.OBJECT_FIELD, AttachPoint.Point.FIELD);
        } else if ((ownerSymTag & SymTag.RECORD) == SymTag.RECORD) {
            analyzeVarNode(varNode, data, AttachPoint.Point.RECORD_FIELD, AttachPoint.Point.FIELD);
        } else if ((ownerSymTag & SymTag.FUNCTION_TYPE) == SymTag.FUNCTION_TYPE) {
            analyzeVarNode(varNode, data, AttachPoint.Point.PARAMETER);
        } else {
            varNode.annAttachments.forEach(annotationAttachment -> {
                if (isListenerDecl) {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.LISTENER);
                } else if (Symbols.isFlagOn(varNode.symbol.flags, Flags.SERVICE)) {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.SERVICE);
                } else {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.VAR);
                }
                annotationAttachment.accept(this, data);

                BAnnotationAttachmentSymbol annotationAttachmentSymbol =
                        annotationAttachment.annotationAttachmentSymbol;
                if (annotationAttachmentSymbol != null) {
                    varNode.symbol.addAnnotation(annotationAttachmentSymbol);
                }
            });
        }
        validateAnnotationAttachmentCount(varNode.annAttachments);

        validateWorkerAnnAttachments(varNode.expr, data);

        handleWildCardBindingVariable(varNode, currentEnv);

        BType lhsType = varNode.symbol.type;
        varNode.setBType(lhsType);

        // Configurable variable type must be a subtype of anydata.
        if (configurable && varNode.typeNode != null) {
            if (!types.isAssignable(lhsType, symTable.anydataType)) {
                dlog.error(varNode.typeNode.pos,
                        DiagnosticErrorCode.CONFIGURABLE_VARIABLE_MUST_BE_ANYDATA);
            } else {
                if (!types.isInherentlyImmutableType(lhsType)) {
                    // Configurable variables are implicitly readonly
                    lhsType = ImmutableTypeCloner.getImmutableIntersectionType(varNode.pos, types,
                            lhsType, currentEnv, symTable, anonModelHelper, names, new HashSet<>());
                    varNode.setBType(lhsType);
                    varNode.symbol.type = lhsType;
                }
                // TODO: remove this check once runtime support all configurable types
                checkSupportedConfigType(varNode.symbol, varNode.pos, varNode.name.value);
            }
        }

        if (varNode.typeNode != null) {
            analyzeNode(varNode.typeNode, data);
        }

        // Analyze the init expression
        BLangExpression rhsExpr = varNode.expr;
        if (rhsExpr == null) {
            if (varNode.flagSet.contains(Flag.ISOLATED)) {
                dlog.error(varNode.pos, DiagnosticErrorCode.INVALID_ISOLATED_QUALIFIER_ON_MODULE_NO_INIT_VAR_DECL);
            }

            if (Types.getReferredType(lhsType).tag == TypeTags.ARRAY
                    && typeChecker.isArrayOpenSealedType((BArrayType) Types.getReferredType(lhsType))) {
                dlog.error(varNode.pos, DiagnosticErrorCode.CLOSED_ARRAY_TYPE_NOT_INITIALIZED);
            }
            return;
        }

        // Here we create a new symbol environment to catch self references by keep the current
        // variable symbol in the symbol environment
        // e.g. int a = x + a;
        SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, currentEnv, varNode.symbol);

        if (isListenerDecl) {
            BType rhsType = typeChecker.checkExpr(rhsExpr, varInitEnv,
                    BUnionType.create(null, lhsType, symTable.errorType), data.prevEnvs,
                    data.commonAnalyzerData);
            validateListenerCompatibility(varNode, rhsType);
        } else {
            typeChecker.checkExpr(rhsExpr, varInitEnv, lhsType, data.prevEnvs, data.commonAnalyzerData);
        }

        checkSelfReferencesInVarNode(varNode, rhsExpr, data);
        transferForkFlag(varNode);
    }

    private void analyzeModuleConfigurableAmbiguity(BLangPackage pkgNode) {
        if (pkgNode.moduleContextDataHolder == null) {
            return;
        }
        ModuleDescriptor rootModule = pkgNode.moduleContextDataHolder.descriptor();
        Set<BVarSymbol> configVars = symResolver.getConfigVarSymbolsIncludingImportedModules(pkgNode.symbol);
        String rootOrgName = rootModule.org().value();
        String rootModuleName = rootModule.packageName().value();
        Map<String, PackageID> configKeys =  getModuleKeys(configVars, rootOrgName);
        for (BVarSymbol variable : configVars) {
            String moduleName = variable.pkgID.name.value;
            String orgName = variable.pkgID.orgName.value;
            String varName = variable.name.value;
            validateMapConfigVariable(orgName + "." + moduleName + "." + varName, variable, configKeys);
            if (orgName.equals(rootOrgName)) {
                validateMapConfigVariable(moduleName + "." + varName, variable, configKeys);
                if (moduleName.equals(rootModuleName) && !(varName.equals(moduleName))) {
                    validateMapConfigVariable(varName, variable, configKeys);
                }
            }
        }
    }

    private Map<String, PackageID> getModuleKeys(Set<BVarSymbol> configVars, String rootOrg) {
        Map<String, PackageID> configKeys = new HashMap<>();
        for (BVarSymbol variable : configVars) {
            PackageID pkgID = variable.pkgID;
            String orgName = pkgID.orgName.value;
            String moduleName = pkgID.name.value;
            configKeys.put(orgName + "." + moduleName, pkgID);
            if (!orgName.equals(rootOrg)) {
                break;
            }
            configKeys.put(moduleName, pkgID);
        }
        return configKeys;
    }

    private void validateMapConfigVariable(String configKey, BVarSymbol variable, Map<String, PackageID> configKeys) {
        if (configKeys.containsKey(configKey) && types.isSubTypeOfMapping(variable.type)) {
            dlog.error(variable.pos, DiagnosticErrorCode.CONFIGURABLE_VARIABLE_MODULE_AMBIGUITY,
                    variable.name.value, configKeys.get(configKey));
        }
    }

    private void checkSupportedConfigType(BVarSymbol varSymbol, Location location, String varName) {
        List<String> errors = new ArrayList<>();
        if (!isSupportedConfigType(varSymbol.type, errors, varName, new HashSet<>(), Symbols.isFlagOn(varSymbol.flags,
                Flags.REQUIRED)) || !errors.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder();
            for (String error : errors) {
                errorMsg.append("\n\t").append(error);
            }
            dlog.error(location, DiagnosticErrorCode.CONFIGURABLE_VARIABLE_CURRENTLY_NOT_SUPPORTED, varSymbol.type,
                    errorMsg);
        }
    }

    private boolean isSupportedConfigType(BType type, List<String> errors, String varName, Set<BType> unresolvedTypes
            , boolean isRequired) {
        if (!unresolvedTypes.add(type)) {
            return true;
        }
        switch (type.getKind()) {
            case ANYDATA:
                break;
            case FINITE:
                return types.isAnydata(type);
            case NIL:
                return !isRequired;
            case ARRAY:
                BType elementType = ((BArrayType) type).eType;
                if (!isSupportedConfigType(elementType, errors, varName, unresolvedTypes, isRequired)) {
                    errors.add("array element type '" + elementType + "' is not supported");
                }
                break;
            case RECORD:
                BRecordType recordType = (BRecordType) type;
                Set<BType> invalidTypeSet = new HashSet<>();
                recordType.getFields().forEach((fieldName, field) -> {
                    BType fieldType = field.type;
                    String fieldString = varName + "." + fieldName;
                    if (invalidTypeSet.contains(fieldType)) {
                        errors.add("record field type '" + fieldType + "' of field '" + fieldString + "' is not " +
                                "supported");
                        return;
                    }
                    if (isNilableDefaultField(field, fieldType)) {
                        return;
                    }
                    if (!isSupportedConfigType(fieldType, errors, fieldString, unresolvedTypes, isRequired)) {
                        errors.add("record field type '" + fieldType + "' of field '" + fieldString + "' is not " +
                                "supported");
                        invalidTypeSet.add(fieldType);
                    }
                });
                break;
            case MAP:
                BMapType mapType = (BMapType) type;
                if (!isSupportedConfigType(mapType.constraint, errors, varName, unresolvedTypes, isRequired)) {
                    errors.add("map constraint type '" + mapType.constraint + "' is not supported");
                }
                break;
            case TABLE:
                BTableType tableType = (BTableType) type;
                if (!isSupportedConfigType(tableType.constraint, errors, varName, unresolvedTypes, isRequired)) {
                    errors.add("table constraint type '" + tableType.constraint + "' is not supported");
                }
                break;
            case INTERSECTION:
                return isSupportedConfigType(((BIntersectionType) type).effectiveType, errors, varName,
                        unresolvedTypes, isRequired);
            case UNION:
                BUnionType unionType = (BUnionType) type;
                for (BType memberType : unionType.getMemberTypes()) {
                    if (!isSupportedConfigType(memberType, errors, varName, unresolvedTypes, isRequired)) {
                        errors.add("union member type '" + memberType + "' is not supported");
                    }
                }
                break;
            case TUPLE:
                BTupleType tupleType = (BTupleType) type;
                for (BType memberType : tupleType.tupleTypes) {
                    if (!isSupportedConfigType(memberType, errors, varName, unresolvedTypes, isRequired)) {
                        errors.add("tuple element type '" + memberType + "' is not supported");
                    }
                }
                break;
            case TYPEREFDESC:
                return isSupportedConfigType(Types.getReferredType(type), errors, varName,
                        unresolvedTypes, isRequired);
            default:
                return  types.isAssignable(type, symTable.intType) ||
                        types.isAssignable(type, symTable.floatType) ||
                        types.isAssignable(type, symTable.stringType) ||
                        types.isAssignable(type, symTable.booleanType) ||
                        types.isAssignable(type, symTable.decimalType) ||
                        types.isAssignable(type, symTable.xmlType) ||
                        types.isAssignable(type, symTable.jsonType);
        }
        return true;
    }

    private boolean isNilableDefaultField(BField field, BType fieldType) {
        if (!Symbols.isFlagOn(field.symbol.flags, Flags.REQUIRED) && !Symbols.isFlagOn(field.symbol.flags,
                Flags.OPTIONAL)) {
            if (fieldType.tag == TypeTags.NIL) {
                return true;
            }
            if (fieldType.tag == TypeTags.UNION) {
                BUnionType unionType = (BUnionType) fieldType;
                for (BType memberType : unionType.getMemberTypes()) {
                    if (memberType.tag == TypeTags.NIL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void validateListenerCompatibility(BLangSimpleVariable varNode, BType rhsType) {
        if (Types.getReferredType(rhsType).tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) rhsType).getMemberTypes()) {
                memberType = Types.getReferredType(rhsType);
                if (memberType.tag == TypeTags.ERROR) {
                    continue;
                }
                if (!types.checkListenerCompatibility(varNode.symbol.type)) {
                    dlog.error(varNode.pos, DiagnosticErrorCode.INVALID_LISTENER_VARIABLE, varNode.name);
                }
            }
        } else {
            if (!types.checkListenerCompatibility(varNode.symbol.type)) {
                dlog.error(varNode.pos, DiagnosticErrorCode.INVALID_LISTENER_VARIABLE, varNode.name);
            }
        }
    }

    private void analyzeVarNode(BLangSimpleVariable varNode, AnalyzerData data, AttachPoint.Point... attachPoints) {
        SymbolEnv currentEnv = data.env;
        if (varNode.symbol == null) {
            symbolEnter.defineNode(varNode, currentEnv);
        }

        // When 'var' is used, the typeNode is null. Need to analyze the record type node here if it's a locally
        // defined record type.
        if (varNode.typeNode != null && varNode.typeNode.getKind() == NodeKind.RECORD_TYPE &&
                !((BLangRecordTypeNode) varNode.typeNode).analyzed) {
            data.env = currentEnv;
            analyzeNode(varNode.typeNode, data);
        }

        if (varNode.typeNode != null && varNode.typeNode.getKind() == NodeKind.FUNCTION_TYPE &&
                !((BLangFunctionTypeNode) varNode.typeNode).analyzed) {
            analyzeNode(varNode.typeNode, data);
        }

        List<AttachPoint.Point> attachPointsList = Arrays.asList(attachPoints);
        for (BLangAnnotationAttachment annotationAttachment : varNode.annAttachments) {
            annotationAttachment.attachPoints.addAll(attachPointsList);
            annotationAttachment.accept(this, data);

            BAnnotationAttachmentSymbol annotationAttachmentSymbol = annotationAttachment.annotationAttachmentSymbol;
            if (annotationAttachmentSymbol != null) {
                varNode.symbol.addAnnotation(annotationAttachmentSymbol);
            }
        }
    }

    private void transferForkFlag(BLangSimpleVariable varNode) {
        // Transfer FORK flag to workers future value.
        if (varNode.expr != null && varNode.expr.getKind() == NodeKind.INVOCATION
                && varNode.flagSet.contains(Flag.WORKER)) {

            BLangInvocation expr = (BLangInvocation) varNode.expr;
            if (expr.name.value.startsWith("0") && (expr.symbol.flags & Flags.FORKED) == Flags.FORKED) {
                varNode.symbol.flags |= Flags.FORKED;
            }
        }
    }

    /**
     * Validate annotation attachment of the `start` action or workers.
     *
     * @param expr expression to be validated.
     */
    private void validateWorkerAnnAttachments(BLangExpression expr,  AnalyzerData data) {
        if (expr != null && expr instanceof BLangInvocation.BLangActionInvocation &&
                ((BLangInvocation.BLangActionInvocation) expr).async) {
            ((BLangInvocation) expr).annAttachments.forEach(annotationAttachment -> {
                annotationAttachment.attachPoints.add(AttachPoint.Point.WORKER);
                annotationAttachment.accept(this, data);
            });
            validateAnnotationAttachmentCount(((BLangInvocation) expr).annAttachments);
        }
    }

    public void visit(BLangRecordVariable varNode, AnalyzerData data) {

        // Only simple variables are allowed to be configurable.
        if (isConfigurable(varNode)) {
            dlog.error(varNode.pos, DiagnosticErrorCode.ONLY_SIMPLE_VARIABLES_ARE_ALLOWED_TO_BE_CONFIGURABLE);
        }

        if (isIsolated(varNode)) {
            dlog.error(varNode.pos, DiagnosticErrorCode.ONLY_A_SIMPLE_VARIABLE_CAN_BE_MARKED_AS_ISOLATED);
        }

        if (varNode.isDeclaredWithVar) {
            handleDeclaredWithVar(varNode, data);
            return;
        }
        SymbolEnv currentEnv = data.env;
        if (varNode.getBType() == null) {
            varNode.setBType(symResolver.resolveTypeNode(varNode.typeNode, currentEnv));
        }

        long ownerSymTag = currentEnv.scope.owner.tag;
        // If this is a module record variable, checkTypeAndVarCountConsistency already done at symbolEnter.
        if ((ownerSymTag & SymTag.PACKAGE) != SymTag.PACKAGE &&
                !(this.symbolEnter.symbolEnterAndValidateRecordVariable(varNode, currentEnv))) {
            varNode.setBType(symTable.semanticError);
            return;
        }

        if (varNode.getBType() == symTable.semanticError) {
            // This will return module record variables with type error
            return;
        }

        BVarSymbol symbol = varNode.symbol;

        varNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoints.add(AttachPoint.Point.VAR);
            annotationAttachment.accept(this, data);
            symbol.addAnnotation(annotationAttachment.annotationAttachmentSymbol);
        });

        validateAnnotationAttachmentCount(varNode.annAttachments);

        if (varNode.expr == null) {
            // we have no rhs to do type checking
            return;
        }

        typeChecker.checkExpr(varNode.expr, currentEnv, varNode.getBType(), data.prevEnvs,
                data.commonAnalyzerData);

    }

    public void visit(BLangTupleVariable varNode, AnalyzerData data) {

        // Only simple variables are allowed to be configurable.
        if (isConfigurable(varNode)) {
            dlog.error(varNode.pos, DiagnosticErrorCode.ONLY_SIMPLE_VARIABLES_ARE_ALLOWED_TO_BE_CONFIGURABLE);
        }

        if (isIsolated(varNode)) {
            dlog.error(varNode.pos, DiagnosticErrorCode.ONLY_A_SIMPLE_VARIABLE_CAN_BE_MARKED_AS_ISOLATED);
        }

        if (varNode.isDeclaredWithVar) {
            data.expType = resolveTupleType(varNode);
            handleDeclaredWithVar(varNode, data);
            return;
        }
        SymbolEnv currentEnv = data.env;
        if (varNode.getBType() == null) {
            varNode.setBType(symResolver.resolveTypeNode(varNode.typeNode, currentEnv));
        }

        long ownerSymTag = currentEnv.scope.owner.tag;
        // If this is a module tuple variable, checkTypeAndVarCountConsistency already done at symbolEnter.
        if ((ownerSymTag & SymTag.PACKAGE) != SymTag.PACKAGE &&
                !(this.symbolEnter.checkTypeAndVarCountConsistency(varNode, currentEnv))) {
            varNode.setBType(symTable.semanticError);
            return;
        }

        BVarSymbol symbol = varNode.symbol;
        varNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoints.add(AttachPoint.Point.VAR);
            annotationAttachment.accept(this, data);
            symbol.addAnnotation(annotationAttachment.annotationAttachmentSymbol);
        });

        validateAnnotationAttachmentCount(varNode.annAttachments);

        if (varNode.expr == null) {
            // we have no rhs to do type checking
            return;
        }

        typeChecker.checkExpr(varNode.expr, currentEnv, varNode.getBType(), data.prevEnvs,
                data.commonAnalyzerData);
        checkSelfReferencesInVarNode(varNode, varNode.expr, data);
    }

    private void checkSelfReferencesInVarNode(BLangVariable variable, BLangExpression rhsExpr, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        switch (variable.getKind()) {
            case VARIABLE:
                SymbolEnv simpleVarEnv = currentEnv.enclVarSym != null ? currentEnv :
                                                      SymbolEnv.createVarInitEnv(variable, currentEnv, variable.symbol);
                checkSelfReferences(rhsExpr, simpleVarEnv);
                break;
            case TUPLE_VARIABLE:
                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                if (rhsExpr.getKind() != NodeKind.LIST_CONSTRUCTOR_EXPR ||
                        ((BLangListConstructorExpr) rhsExpr).exprs.size() > tupleVariable.memberVariables.size()) {
                    return;
                }
                BLangListConstructorExpr listExpr = (BLangListConstructorExpr) rhsExpr;
                for (int i = 0; i < listExpr.exprs.size(); i++) {
                    for (int j = 0; j < tupleVariable.memberVariables.size(); j++) {
                        if (listExpr.exprs.get(i).getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
                            checkSelfReferencesInVarNode(tupleVariable.memberVariables.get(j),
                                                         listExpr.exprs.get(i), data);
                            continue;
                        }
                        BLangVariable memberVar = tupleVariable.memberVariables.get(j);
                        SymbolEnv varEnv = SymbolEnv.createVarInitEnv(memberVar, currentEnv, memberVar.symbol);
                        checkSelfReferences(listExpr.exprs.get(i), varEnv);
                    }
                }
                break;
        }
    }

    private void checkSelfReferences(BLangExpression expr, SymbolEnv varInitEnv) {
        if (expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) expr;
            if (varRef.symbol != null && ((varRef.symbol.tag & SymTag.VARIABLE) == SymTag.VARIABLE)) {
                typeChecker.checkSelfReferences(varRef.pos, varInitEnv, (BVarSymbol) varRef.symbol);
            }
            return;
        }
        if (expr.getKind() == NodeKind.LET_EXPR) {
            for (BLangLetVariable letVar : ((BLangLetExpression) expr).letVarDeclarations) {
                checkSelfReferences(((BLangVariable) letVar.definitionNode.getVariable()).expr, varInitEnv);
            }
            return;
        }
        if (expr.getKind() == NodeKind.INVOCATION) {
            for (BLangExpression argExpr : ((BLangInvocation) expr).argExprs) {
                checkSelfReferences(argExpr, varInitEnv);
            }
            return;
        }
        if (expr.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
            BLangListConstructorExpr listExpr = (BLangListConstructorExpr) expr;
            for (int i = 0; i < listExpr.exprs.size(); i++) {
                BLangExpression expression = listExpr.exprs.get(i);
                if (expression.getKind() == NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                    expression = ((BLangListConstructorSpreadOpExpr) expression).expr;
                }
                checkSelfReferences(expression, varInitEnv);
            }
            return;
        }
        if (expr.getKind() == RECORD_LITERAL_EXPR) {
            BLangRecordLiteral recordLiteral = (BLangRecordLiteral) expr;
            for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
                if (field.isKeyValueField()) {
                    BLangRecordLiteral.BLangRecordKeyValueField pair =
                            (BLangRecordLiteral.BLangRecordKeyValueField) field;
                    checkSelfReferences(pair.valueExpr, varInitEnv);
                    continue;
                }
                if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    checkSelfReferences((BLangSimpleVarRef) field, varInitEnv);
                }
            }
        }
    }

    private BType resolveTupleType(BLangTupleVariable varNode) {
        List<BType> memberTypes = new ArrayList<>(varNode.memberVariables.size());
        for (BLangVariable memberVariable : varNode.memberVariables) {
            memberTypes.add(getTupleMemberType(memberVariable));
        }

        BLangVariable restVariable = varNode.restVariable;
        if (restVariable == null) {
            return new BTupleType(memberTypes);
        }

        return new BTupleType(null, memberTypes, getTupleMemberType(restVariable), 0);
    }

    private BType getTupleMemberType(BLangVariable memberVariable) {
        if (memberVariable.getKind() == NodeKind.TUPLE_VARIABLE) {
            return resolveTupleType((BLangTupleVariable) memberVariable);
        }
        return symTable.noType;
    }

    @Override
    public void visit(BLangErrorVariable varNode, AnalyzerData data) {

        // Only simple variables are allowed to be configurable.
        if (isConfigurable(varNode)) {
            dlog.error(varNode.pos, DiagnosticErrorCode.ONLY_SIMPLE_VARIABLES_ARE_ALLOWED_TO_BE_CONFIGURABLE);
        }

        if (isIsolated(varNode)) {
            dlog.error(varNode.pos, DiagnosticErrorCode.ONLY_A_SIMPLE_VARIABLE_CAN_BE_MARKED_AS_ISOLATED);
        }
        // Error variable declarations (destructuring etc.)
        if (varNode.isDeclaredWithVar) {
            handleDeclaredWithVar(varNode, data);
            validateErrorDetailBindingPatterns(varNode);
            return;
        }
        SymbolEnv currentEnv = data.env;
        if (varNode.getBType() == null) {
            varNode.setBType(symResolver.resolveTypeNode(varNode.typeNode, currentEnv));
        }

        // match err1 { error(reason,....) => ... }
        // reason must be a const of subtype of string.
        // then we match the error with this specific reason.
        if (!varNode.reasonVarPrefixAvailable && varNode.getBType() == null) {
            BErrorType errorType = new BErrorType(varNode.getBType().tsymbol, null);

            if (Types.getReferredType(varNode.getBType()).tag == TypeTags.UNION) {
                Set<BType> members = types.expandAndGetMemberTypesRecursive(varNode.getBType());
                List<BErrorType> errorMembers = members.stream()
                        .filter(m -> Types.getReferredType(m).tag == TypeTags.ERROR)
                        .map(m -> (BErrorType) Types.getReferredType(m))
                        .collect(Collectors.toList());

                if (errorMembers.isEmpty()) {
                    dlog.error(varNode.pos, DiagnosticErrorCode.INVALID_ERROR_MATCH_PATTERN);
                    return;
                } else if (errorMembers.size() == 1) {
                    errorType.detailType = errorMembers.get(0).detailType;
                } else {
                    errorType.detailType = symTable.detailType;
                }
                varNode.setBType(errorType);
            } else if (Types.getReferredType(varNode.getBType()).tag == TypeTags.ERROR) {
                errorType.detailType = ((BErrorType) Types.getReferredType(varNode.getBType()))
                        .detailType;
            }
        }

        long ownerSymTag = currentEnv.scope.owner.tag;
        // If this is a module error variable, checkTypeAndVarCountConsistency already done at symbolEnter.
        if ((ownerSymTag & SymTag.PACKAGE) != SymTag.PACKAGE &&
                !(this.symbolEnter.symbolEnterAndValidateErrorVariable(varNode, currentEnv))) {
            varNode.setBType(symTable.semanticError);
            return;
        }

        if (varNode.getBType() == symTable.semanticError) {
            // This will return module error variables with type error
            return;
        }

        BVarSymbol symbol = varNode.symbol;
        varNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoints.add(AttachPoint.Point.VAR);
            annotationAttachment.accept(this, data);
            symbol.addAnnotation(annotationAttachment.annotationAttachmentSymbol);
        });

        validateAnnotationAttachmentCount(varNode.annAttachments);

        if (varNode.expr == null) {
            // We have no rhs to do type checking.
            return;
        }

        typeChecker.checkExpr(varNode.expr, currentEnv, varNode.getBType(), data.prevEnvs,
                data.commonAnalyzerData);
        validateErrorDetailBindingPatterns(varNode);
    }

    private void validateErrorDetailBindingPatterns(BLangErrorVariable errorVariable) {
        BType rhsType = types.getReferredType(errorVariable.expr.getBType());
        if (rhsType.getKind() != TypeKind.ERROR) {
            return;
        }

        BErrorType errorType = (BErrorType) rhsType;
        BType detailType = types.getReferredType(errorType.detailType);

        if (detailType.getKind() != TypeKind.RECORD) {
            for (BLangErrorVariable.BLangErrorDetailEntry errorDetailEntry : errorVariable.detail) {
                dlog.error(errorDetailEntry.pos, DiagnosticErrorCode.CANNOT_BIND_UNDEFINED_ERROR_DETAIL_FIELD,
                        errorDetailEntry.key.value);
            }
            return;
        }

        BRecordType rhsDetailType = (BRecordType) detailType;
        LinkedHashMap<String, BField> detailFields = rhsDetailType.fields;

        for (BLangErrorVariable.BLangErrorDetailEntry errorDetailEntry : errorVariable.detail) {
            String entryName = errorDetailEntry.key.getValue();
            BField entryField = detailFields.get(entryName);

            if (entryField == null) {
                dlog.error(errorDetailEntry.pos, DiagnosticErrorCode.CANNOT_BIND_UNDEFINED_ERROR_DETAIL_FIELD,
                           errorDetailEntry.key.value);
                continue;
            }

            errorDetailEntry.keySymbol = entryField.symbol;

            if (Symbols.isFlagOn(entryField.symbol.flags, Flags.OPTIONAL)) {
                dlog.error(errorDetailEntry.pos,
                           DiagnosticErrorCode.INVALID_FIELD_BINDING_PATTERN_WITH_NON_REQUIRED_FIELD);
            }
        }
    }

    private void handleDeclaredWithVar(BLangVariable variable, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        BLangExpression varRefExpr = variable.expr;
        BType rhsType;
        if (varRefExpr == null) {
            rhsType = symTable.semanticError;
            variable.setBType(symTable.semanticError);
            dlog.error(variable.pos, DiagnosticErrorCode.VARIABLE_DECL_WITH_VAR_WITHOUT_INITIALIZER);
        } else {
            rhsType = typeChecker.checkExpr(varRefExpr, currentEnv, data.expType, data.prevEnvs,
                    data.commonAnalyzerData);
        }

        switch (variable.getKind()) {
            case VARIABLE:
            case LET_VARIABLE:
                if (!validateObjectTypeInitInvocation(varRefExpr)) {
                    rhsType = symTable.semanticError;
                }

                if (variable.flagSet.contains(Flag.LISTENER)) {
                    BType listenerType = getListenerType(rhsType);
                    if (listenerType == null) {
                        dlog.error(varRefExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, LISTENER_NAME, rhsType);
                        return;
                    }
                    rhsType = listenerType;
                }

                BLangSimpleVariable simpleVariable = (BLangSimpleVariable) variable;

                simpleVariable.setBType(rhsType);

                handleWildCardBindingVariable(simpleVariable, currentEnv);

                long ownerSymTag = currentEnv.scope.owner.tag;
                if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE || (ownerSymTag & SymTag.LET) == SymTag.LET) {
                    // This is a variable declared in a function, an action or a resource
                    // If the variable is parameter then the variable symbol is already defined
                    if (simpleVariable.symbol == null) {
                        symbolEnter.defineNode(simpleVariable, currentEnv);
                    }
                }

                // Set the type to the symbol. If the variable is a global variable, a symbol is already created in the
                // symbol enter. If the variable is a local variable, the symbol will be created above.
                simpleVariable.symbol.type = rhsType;

                if (simpleVariable.symbol.type == symTable.semanticError) {
                    simpleVariable.symbol.state = DiagnosticState.UNKNOWN_TYPE;
                }

                variable.annAttachments.forEach(annotationAttachment -> {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.VAR);
                    annotationAttachment.accept(this, data);

                    BAnnotationAttachmentSymbol annotationAttachmentSymbol =
                            annotationAttachment.annotationAttachmentSymbol;
                    if (annotationAttachmentSymbol != null) {
                        variable.symbol.addAnnotation(annotationAttachmentSymbol);
                    }
                });

                validateAnnotationAttachmentCount(variable.annAttachments);
                break;
            case TUPLE_VARIABLE:
                if (varRefExpr == null) {
                    return;
                }

                if (variable.isDeclaredWithVar && variable.expr.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
                    List<BLangExpression> members = ((BLangListConstructorExpr) varRefExpr).exprs;
                    dlog.error(varRefExpr.pos, DiagnosticErrorCode.CANNOT_INFER_TYPES_FOR_TUPLE_BINDING, members);
                    variable.setBType(symTable.semanticError);
                    return;
                }
                if (TypeTags.TUPLE != Types.getReferredType(rhsType).tag
                        && TypeTags.ARRAY != Types.getReferredType(rhsType).tag) {
                    dlog.error(varRefExpr.pos, DiagnosticErrorCode.INVALID_LIST_BINDING_PATTERN_INFERENCE, rhsType);
                    variable.setBType(symTable.semanticError);
                    return;
                }

                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                tupleVariable.setBType(rhsType);

                if (!(this.symbolEnter.checkTypeAndVarCountConsistency(tupleVariable, currentEnv))) {
                    tupleVariable.setBType(symTable.semanticError);
                    return;
                }
                BVarSymbol tupleVarSymbol = tupleVariable.symbol;
                tupleVariable.annAttachments.forEach(annotationAttachment -> {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.VAR);
                    annotationAttachment.accept(this, data);
                    tupleVarSymbol.addAnnotation(annotationAttachment.annotationAttachmentSymbol);
                });

                validateAnnotationAttachmentCount(tupleVariable.annAttachments);
                break;
            case RECORD_VARIABLE:
                if (varRefExpr == null) {
                    return;
                }

                BType recordRhsType = Types.getReferredType(rhsType);
                if (TypeTags.RECORD != recordRhsType.tag && TypeTags.MAP != recordRhsType.tag
                        && TypeTags.JSON != recordRhsType.tag) {
                    dlog.error(varRefExpr.pos, DiagnosticErrorCode.INVALID_TYPE_DEFINITION_FOR_RECORD_VAR, rhsType);
                    variable.setBType(symTable.semanticError);
                }

                BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
                recordVariable.setBType(rhsType);

                if (!this.symbolEnter.symbolEnterAndValidateRecordVariable(recordVariable, currentEnv)) {
                    recordVariable.setBType(symTable.semanticError);
                }

                BVarSymbol recordVarSymbol = recordVariable.symbol;
                recordVariable.annAttachments.forEach(annotationAttachment -> {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.VAR);
                    annotationAttachment.accept(this, data);
                    recordVarSymbol.addAnnotation(annotationAttachment.annotationAttachmentSymbol);
                });

                validateAnnotationAttachmentCount(recordVariable.annAttachments);
                break;
            case ERROR_VARIABLE:
                if (varRefExpr == null) {
                    return;
                }

                if (TypeTags.ERROR != Types.getReferredType(rhsType).tag) {
                    dlog.error(variable.expr.pos, DiagnosticErrorCode.INVALID_TYPE_DEFINITION_FOR_ERROR_VAR, rhsType);
                    variable.setBType(symTable.semanticError);
                    return;
                }

                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                if (errorVariable.typeNode != null) {
                    symResolver.resolveTypeNode(errorVariable.typeNode, currentEnv);
                }
                errorVariable.setBType(rhsType);

                if (!this.symbolEnter.symbolEnterAndValidateErrorVariable(errorVariable, currentEnv)) {
                    errorVariable.setBType(symTable.semanticError);
                    return;
                }

                BVarSymbol errorVarSymbol = errorVariable.symbol;
                errorVariable.annAttachments.forEach(annotationAttachment -> {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.VAR);
                    annotationAttachment.accept(this, data);
                    errorVarSymbol.addAnnotation(annotationAttachment.annotationAttachmentSymbol);
                });

                validateAnnotationAttachmentCount(errorVariable.annAttachments);
                break;
        }
    }

    private BType getListenerType(BType bType) {
        LinkedHashSet<BType> compatibleTypes = new LinkedHashSet<>();
        BType type = Types.getReferredType(bType);
        if (type.tag == TypeTags.UNION) {
            for (BType t : ((BUnionType) type).getMemberTypes()) {
                if (t.tag == TypeTags.ERROR) {
                    continue;
                }
                if (types.checkListenerCompatibility(t)) {
                    compatibleTypes.add(t);
                } else {
                    return null;
                }
            }
        } else if (types.checkListenerCompatibility(type)) {
            compatibleTypes.add(type);
        }

        if (compatibleTypes.isEmpty()) {
            return null;
        } else if (compatibleTypes.size() == 1) {
            return compatibleTypes.iterator().next();
        } else {
            return BUnionType.create(null, compatibleTypes);
        }
    }

    private void handleWildCardBindingVariable(BLangSimpleVariable variable, SymbolEnv env) {
        if (!variable.name.value.equals(Names.IGNORE.value)) {
            return;
        }
        BLangExpression bindingExp = variable.expr;
        BType bindingValueType = bindingExp != null && bindingExp.getBType() != null
                ? bindingExp.getBType() : variable.getBType();
        if (!types.isAssignable(bindingValueType, symTable.anyType)) {
            dlog.error(variable.pos, DiagnosticErrorCode.WILD_CARD_BINDING_PATTERN_ONLY_SUPPORTS_TYPE_ANY);
        }
        // Fake symbol to prevent runtime failures down the line.
        variable.symbol = new BVarSymbol(0, Names.IGNORE, env.enclPkg.packageID,
                variable.getBType(), env.scope.owner, variable.pos, VIRTUAL);
    }

    void handleDeclaredVarInForeach(BLangVariable variable, BType rhsType, SymbolEnv blockEnv) {
        rhsType = getApplicableRhsType(rhsType);

        switch (variable.getKind()) {
            case VARIABLE:
                BLangSimpleVariable simpleVariable = (BLangSimpleVariable) variable;
                simpleVariable.setBType(rhsType);

                handleWildCardBindingVariable(simpleVariable, blockEnv);

                long ownerSymTag = blockEnv.scope.owner.tag;
                if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE
                        || (ownerSymTag & SymTag.PACKAGE) == SymTag.PACKAGE
                        || (ownerSymTag & SymTag.LET) == SymTag.LET) {
                    // This is a variable declared in a function, an action or a resource
                    // If the variable is parameter then the variable symbol is already defined
                    if (simpleVariable.symbol == null) {
                        // Add flag to identify variable is used in foreach/from clause/join clause
                        variable.flagSet.add(Flag.NEVER_ALLOWED);
                        symbolEnter.defineNode(simpleVariable, blockEnv);
                    }
                }
                recursivelySetFinalFlag(simpleVariable);
                break;
            case TUPLE_VARIABLE:
                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                BType tupleRhsType = Types.getReferredType(rhsType);
                if ((TypeTags.TUPLE != tupleRhsType.tag && TypeTags.ARRAY != tupleRhsType.tag &&
                        TypeTags.UNION != tupleRhsType.tag) ||
                        (variable.isDeclaredWithVar && !types.isSubTypeOfBaseType(tupleRhsType, TypeTags.TUPLE))) {
                    dlog.error(variable.pos, DiagnosticErrorCode.INVALID_LIST_BINDING_PATTERN_INFERENCE, rhsType);
                    recursivelyDefineVariables(tupleVariable, blockEnv);
                    return;
                }

                tupleVariable.setBType(rhsType);

                if (Types.getReferredType(rhsType).tag == TypeTags.TUPLE
                        && !(this.symbolEnter.checkTypeAndVarCountConsistency(tupleVariable,
                        (BTupleType) Types.getReferredType(tupleVariable.getBType()),
                        blockEnv))) {
                    recursivelyDefineVariables(tupleVariable, blockEnv);
                    return;
                }

                if (Types.getReferredType(rhsType).tag == TypeTags.UNION ||
                        Types.getReferredType(rhsType).tag == TypeTags.ARRAY) {
                    BTupleType tupleVariableType = null;
                    BLangType type = tupleVariable.typeNode;
                    if (type != null && Types.getReferredType(type.getBType()).tag == TypeTags.TUPLE) {
                        tupleVariableType = (BTupleType) Types.getReferredType(type.getBType());
                    }
                    if (!(this.symbolEnter.checkTypeAndVarCountConsistency(tupleVariable,
                            tupleVariableType, blockEnv))) {
                        recursivelyDefineVariables(tupleVariable, blockEnv);
                        return;
                    }
                }
                recursivelySetFinalFlag(tupleVariable);
                break;
            case RECORD_VARIABLE:
                BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
                recordVariable.setBType(rhsType);
                this.symbolEnter.validateRecordVariable(recordVariable, blockEnv);
                recursivelySetFinalFlag(recordVariable);
                break;
            case ERROR_VARIABLE:
                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                if (TypeTags.ERROR != Types.getReferredType(rhsType).tag) {
                    dlog.error(variable.pos, DiagnosticErrorCode.INVALID_TYPE_DEFINITION_FOR_ERROR_VAR, rhsType);
                    recursivelyDefineVariables(errorVariable, blockEnv);
                    return;
                }
                errorVariable.setBType(rhsType);
                this.symbolEnter.validateErrorVariable(errorVariable, blockEnv);
                recursivelySetFinalFlag(errorVariable);
                break;
        }
    }

    private BType getApplicableRhsType(BType rhsType) {
        BType referredType = Types.getReferredType(rhsType);
        if (referredType.tag == TypeTags.INTERSECTION) {
            return ((BIntersectionType) referredType).effectiveType;
        }
        return rhsType;
    }

    private void recursivelyDefineVariables(BLangVariable variable, SymbolEnv blockEnv) {
        switch (variable.getKind()) {
            case VARIABLE:
                Name name = names.fromIdNode(((BLangSimpleVariable) variable).name);
                Name origName = names.originalNameFromIdNode(((BLangSimpleVariable) variable).name);
                variable.setBType(symTable.semanticError);
                symbolEnter.defineVarSymbol(variable.pos, variable.flagSet, variable.getBType(), name, origName,
                                            blockEnv, variable.internal);
                break;
            case TUPLE_VARIABLE:
                ((BLangTupleVariable) variable).memberVariables.forEach(memberVariable ->
                        recursivelyDefineVariables(memberVariable, blockEnv));
                break;
            case RECORD_VARIABLE:
                ((BLangRecordVariable) variable).variableList.forEach(value ->
                        recursivelyDefineVariables(value.valueBindingPattern, blockEnv));
                break;
        }
    }

    private void recursivelySetFinalFlag(BLangVariable variable) {
        if (variable == null) {
            return;
        }

        switch (variable.getKind()) {
            case VARIABLE:
                if (variable.symbol == null) {
                    return;
                }
                variable.symbol.flags |= Flags.FINAL;
                break;
            case TUPLE_VARIABLE:
                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                tupleVariable.memberVariables.forEach(this::recursivelySetFinalFlag);
                recursivelySetFinalFlag(tupleVariable.restVariable);
                break;
            case RECORD_VARIABLE:
                BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
                recordVariable.variableList.forEach(value -> recursivelySetFinalFlag(value.valueBindingPattern));
                recursivelySetFinalFlag(recordVariable.restParam);
                break;
            case ERROR_VARIABLE:
                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                recursivelySetFinalFlag(errorVariable.message);
                recursivelySetFinalFlag(errorVariable.restDetail);
                errorVariable.detail.forEach(bLangErrorDetailEntry ->
                        recursivelySetFinalFlag(bLangErrorDetailEntry.valueBindingPattern));
                break;
        }
    }

    // Statements
    @Override
    public void visit(BLangBlockStmt blockNode, AnalyzerData data) {
        data.env = SymbolEnv.createBlockEnv(blockNode, data.env);
        int stmtCount = -1;
        for (BLangStatement stmt : blockNode.stmts) {
            stmtCount++;
            boolean analyzedStmt = analyzeBlockStmtFollowingIfWithoutElse(stmt,
                    stmtCount > 0 ? blockNode.stmts.get(stmtCount - 1) : null, data.env, data);
            if (analyzedStmt) {
                continue;
            }
            analyzeStmt(stmt, data);
        }
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode, AnalyzerData data) {
        analyzeNode(varDefNode.var, data);
    }

    @Override
    public void visit(BLangRecordVariableDef varDefNode, AnalyzerData data) {
        // TODO: 10/18/18 Need to support record literals as well
        if (varDefNode.var.expr != null && varDefNode.var.expr.getKind() == RECORD_LITERAL_EXPR) {
            dlog.error(varDefNode.pos, DiagnosticErrorCode.INVALID_LITERAL_FOR_TYPE, "record binding pattern");
            return;
        }
        analyzeNode(varDefNode.var, data);
    }

    @Override
    public void visit(BLangErrorVariableDef varDefNode, AnalyzerData data) {
        analyzeNode(varDefNode.errorVariable, data);
    }

    @Override
    public void visit(BLangTupleVariableDef tupleVariableDef, AnalyzerData data) {
        analyzeNode(tupleVariableDef.var, data);
    }

    private Boolean validateLhsVar(BLangExpression vRef) {
        if (vRef.getKind() == NodeKind.INVOCATION) {
            dlog.error(vRef.pos, DiagnosticErrorCode.INVALID_INVOCATION_LVALUE_ASSIGNMENT, vRef);
            return false;
        }
        if (vRef.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR
                || vRef.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            validateLhsVar(((BLangAccessExpression) vRef).expr);
        }
        return true;
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignment, AnalyzerData data) {
        BType expType;
        BLangValueExpression varRef = compoundAssignment.varRef;
        SymbolEnv currentEnv = data.env;

        // Check whether the variable reference is an function invocation or not.
        boolean isValidVarRef = validateLhsVar(varRef);
        if (isValidVarRef) {
            varRef.isCompoundAssignmentLValue = true;
            this.typeChecker.checkExpr(varRef, currentEnv, symTable.noType, data.prevEnvs,
                    data.commonAnalyzerData);
            expType = varRef.getBType();
        } else {
            expType = symTable.semanticError;
        }

        this.typeChecker.checkExpr(compoundAssignment.expr, currentEnv, data.prevEnvs, data.commonAnalyzerData);

        checkConstantAssignment(varRef, data);

        if (expType != symTable.semanticError && compoundAssignment.expr.getBType() != symTable.semanticError) {
            BType expressionType = compoundAssignment.expr.getBType();
            if (expType.isNullable() || expressionType.isNullable()) {
                dlog.error(compoundAssignment.pos,
                        DiagnosticErrorCode.COMPOUND_ASSIGNMENT_NOT_ALLOWED_WITH_NULLABLE_OPERANDS);
            }

            BSymbol opSymbol = this.symResolver.resolveBinaryOperator(compoundAssignment.opKind, expType,
                    expressionType);
            if (opSymbol == symTable.notFoundSymbol) {
                opSymbol = symResolver.getArithmeticOpsForTypeSets(compoundAssignment.opKind, expType, expressionType);
            }
            if (opSymbol == symTable.notFoundSymbol) {
                opSymbol = symResolver.getBitwiseShiftOpsForTypeSets(compoundAssignment.opKind, expType,
                        expressionType);
            }
            if (opSymbol == symTable.notFoundSymbol) {
                opSymbol = symResolver.getBinaryBitwiseOpsForTypeSets(compoundAssignment.opKind, expType,
                        expressionType);
            }
            if (opSymbol == symTable.notFoundSymbol) {
                dlog.error(compoundAssignment.pos, DiagnosticErrorCode.BINARY_OP_INCOMPATIBLE_TYPES,
                           compoundAssignment.opKind, expType, expressionType);
            } else {
                compoundAssignment.modifiedExpr = getBinaryExpr(varRef,
                        compoundAssignment.expr,
                        compoundAssignment.opKind,
                        opSymbol);

                // If this is an update of a type narrowed variable, the assignment should allow assigning
                // values of its original type. Therefore treat all lhs simpleVarRefs in their original type.
                // For that create a new varRef with original type
                if (isSimpleVarRef(varRef)) {
                    BVarSymbol originSymbol = ((BVarSymbol) varRef.symbol).originalSymbol;
                    if (originSymbol != null) {
                        BLangSimpleVarRef simpleVarRef =
                                (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
                        simpleVarRef.pos = varRef.pos;
                        simpleVarRef.variableName = ((BLangSimpleVarRef) varRef).variableName;
                        simpleVarRef.symbol = varRef.symbol;
                        simpleVarRef.isLValue = true;
                        simpleVarRef.setBType(originSymbol.type);
                        compoundAssignment.varRef = simpleVarRef;
                    }
                }

                compoundAssignment.modifiedExpr.parent = compoundAssignment;
                this.types.checkType(compoundAssignment.modifiedExpr,
                                     compoundAssignment.modifiedExpr.getBType(), compoundAssignment.varRef.getBType());
            }
        }

        resetTypeNarrowing(compoundAssignment.varRef, data);
    }

    @Override
    public void visit(BLangAssignment assignNode, AnalyzerData data) {
        BLangExpression varRef = assignNode.varRef;
        if (varRef.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR ||
                varRef.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
            ((BLangAccessExpression) varRef).leafNode = true;
        }

        // Check each LHS expression.
        setTypeOfVarRefInAssignment(varRef, data);
        data.expType = varRef.getBType();

        checkInvalidTypeDef(varRef);
        if (varRef.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR && data.expType.tag != TypeTags.SEMANTIC_ERROR) {
            BLangFieldBasedAccess fieldBasedAccessVarRef = (BLangFieldBasedAccess) varRef;
            int varRefTypeTag = Types.getReferredType(fieldBasedAccessVarRef.expr.getBType()).tag;
            if (varRefTypeTag == TypeTags.RECORD && Symbols.isOptional(fieldBasedAccessVarRef.symbol)) {
                data.expType = types.addNilForNillableAccessType(data.expType);
            }
        }

        typeChecker.checkExpr(assignNode.expr, data.env, data.expType, data.prevEnvs, data.commonAnalyzerData);

        validateWorkerAnnAttachments(assignNode.expr, data);

        resetTypeNarrowing(varRef, data);
    }

    @Override
    public void visit(BLangTupleDestructure tupleDeStmt, AnalyzerData data) {
        for (BLangExpression tupleVar : tupleDeStmt.varRef.expressions) {
            setTypeOfVarRefForBindingPattern(tupleVar, data);
            checkInvalidTypeDef(tupleVar);
        }

        if (tupleDeStmt.varRef.restParam != null) {
            setTypeOfVarRefForBindingPattern(tupleDeStmt.varRef.restParam, data);
            checkInvalidTypeDef(tupleDeStmt.varRef.restParam);
        }

        setTypeOfVarRef(tupleDeStmt.varRef, data);

        BType type = typeChecker.checkExpr(tupleDeStmt.expr, data.env, tupleDeStmt.varRef.getBType(), data.prevEnvs,
                data.commonAnalyzerData);

        if (type.tag != TypeTags.SEMANTIC_ERROR) {
            checkTupleVarRefEquivalency(tupleDeStmt.pos, tupleDeStmt.varRef,
                                        tupleDeStmt.expr.getBType(), tupleDeStmt.expr.pos, data);
        }
    }

    @Override
    public void visit(BLangRecordDestructure recordDeStmt, AnalyzerData data) {
        // recursively visit the var refs and create the record type
        for (BLangRecordVarRefKeyValue keyValue : recordDeStmt.varRef.recordRefFields) {
            setTypeOfVarRefForBindingPattern(keyValue.variableReference, data);
            checkInvalidTypeDef(keyValue.variableReference);
        }
        if (recordDeStmt.varRef.restParam != null) {
            setTypeOfVarRefForBindingPattern(recordDeStmt.varRef.restParam, data);
            checkInvalidTypeDef(recordDeStmt.varRef.restParam);
        }
        setTypeOfVarRef(recordDeStmt.varRef, data);

        SymbolEnv currentEnv = data.env;
        typeChecker.checkExpr(recordDeStmt.varRef, currentEnv, symTable.noType, data.prevEnvs,
                data.commonAnalyzerData);

        if (recordDeStmt.expr.getKind() == RECORD_LITERAL_EXPR) {
            // TODO: 10/18/18 Need to support record literals as well
            dlog.error(recordDeStmt.expr.pos, DiagnosticErrorCode.INVALID_RECORD_LITERAL_BINDING_PATTERN);
            return;
        }
        typeChecker.checkExpr(recordDeStmt.expr, currentEnv, symTable.noType, data.prevEnvs,
                data.commonAnalyzerData);
        checkRecordVarRefEquivalency(recordDeStmt.pos, recordDeStmt.varRef, recordDeStmt.expr.getBType(),
                                     recordDeStmt.expr.pos, data);
    }

    @Override
    public void visit(BLangErrorDestructure errorDeStmt, AnalyzerData data) {
        BLangErrorVarRef varRef = errorDeStmt.varRef;
        if (varRef.message != null) {
            if (names.fromIdNode(((BLangSimpleVarRef) varRef.message).variableName) != Names.IGNORE) {
                setTypeOfVarRefInErrorBindingAssignment(varRef.message, data);
                checkInvalidTypeDef(varRef.message);
            } else {
                // set message var refs type to no type if the variable name is '_'
                varRef.message.setBType(symTable.noType);
            }
        }

        if (varRef.cause != null) {
            if (varRef.cause.getKind() != NodeKind.SIMPLE_VARIABLE_REF ||
                    names.fromIdNode(((BLangSimpleVarRef) varRef.cause).variableName) != Names.IGNORE) {
                setTypeOfVarRefInErrorBindingAssignment(varRef.cause, data);
                checkInvalidTypeDef(varRef.cause);
            } else {
                // set cause var refs type to no type if the variable name is '_'
                varRef.cause.setBType(symTable.noType);
            }
        }

        typeChecker.checkExpr(errorDeStmt.expr, data.env, symTable.noType, data.prevEnvs,
                data.commonAnalyzerData);
        checkErrorVarRefEquivalency(varRef, errorDeStmt.expr.getBType(), errorDeStmt.expr.pos, data);
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
    private void checkRecordVarRefEquivalency(Location pos, BLangRecordVarRef lhsVarRef, BType rhsType,
                                              Location rhsPos, AnalyzerData data) {
        rhsType = Types.getReferredType(rhsType);
        if (rhsType.tag == TypeTags.MAP) {
            for (BLangRecordVarRefKeyValue field: lhsVarRef.recordRefFields) {
                dlog.error(field.variableName.pos,
                        DiagnosticErrorCode.INVALID_FIELD_BINDING_PATTERN_WITH_NON_REQUIRED_FIELD);
            }
            return;
        }

        if (rhsType.tag != TypeTags.RECORD) {
            dlog.error(rhsPos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, "record type", rhsType);
            return;
        }

        BRecordType rhsRecordType = (BRecordType) rhsType;
        List<String> mappedFields = new ArrayList<>();

        // check if all fields in record var ref are found in rhs record type
        for (BLangRecordVarRefKeyValue lhsField : lhsVarRef.recordRefFields) {
            if (!rhsRecordType.fields.containsKey(lhsField.variableName.value)) {
                dlog.error(pos, DiagnosticErrorCode.INVALID_FIELD_IN_RECORD_BINDING_PATTERN,
                        lhsField.variableName.value, rhsType);
            } else if (Symbols.isOptional(rhsRecordType.fields.get(lhsField.variableName.value).symbol)) {
                dlog.error(lhsField.variableName.pos,
                        DiagnosticErrorCode.INVALID_FIELD_BINDING_PATTERN_WITH_NON_REQUIRED_FIELD);
            }
            mappedFields.add(lhsField.variableName.value);
        }

        for (BField rhsField : rhsRecordType.fields.values()) {
            List<BLangRecordVarRefKeyValue> expField = lhsVarRef.recordRefFields.stream()
                    .filter(field -> field.variableName.value.equals(rhsField.name.toString()))
                    .collect(Collectors.toList());

            if (expField.isEmpty()) {
                continue;
            }

            if (expField.size() > 1) {
                dlog.error(pos, DiagnosticErrorCode.MULTIPLE_RECORD_REF_PATTERN_FOUND, rhsField.name);
                return;
            }
            BLangExpression variableReference = expField.get(0).variableReference;
            if (variableReference.getKind() == NodeKind.RECORD_VARIABLE_REF) {
                checkRecordVarRefEquivalency(variableReference.pos,
                        (BLangRecordVarRef) variableReference, rhsField.type, rhsPos, data);
            } else if (variableReference.getKind() == NodeKind.TUPLE_VARIABLE_REF) {
                checkTupleVarRefEquivalency(pos, (BLangTupleVarRef) variableReference, rhsField.type, rhsPos, data);
            } else if (variableReference.getKind() == NodeKind.ERROR_VARIABLE_REF) {
                checkErrorVarRefEquivalency((BLangErrorVarRef) variableReference, rhsField.type, rhsPos, data);
            } else if (variableReference.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                Name varName = names.fromIdNode(((BLangSimpleVarRef) variableReference).variableName);
                if (varName == Names.IGNORE) {
                    continue;
                }

                resetTypeNarrowing(variableReference, data);
                types.checkType(variableReference.pos, rhsField.type,
                                variableReference.getBType(), DiagnosticErrorCode.INCOMPATIBLE_TYPES);
            } else {
                dlog.error(variableReference.pos, DiagnosticErrorCode.INVALID_VARIABLE_REFERENCE_IN_BINDING_PATTERN,
                        variableReference);
            }
        }

        if (lhsVarRef.restParam != null) {
            BLangSimpleVarRef varRefRest = (BLangSimpleVarRef) lhsVarRef.restParam;
            BType lhsRefType;
            if (Types.getReferredType(varRefRest.getBType()).tag == TypeTags.RECORD) {
                lhsRefType = varRefRest.getBType();
            } else {
                lhsRefType = ((BLangSimpleVarRef) lhsVarRef.restParam).getBType();
            }

            BType rhsRestConstraint = rhsRecordType.restFieldType == symTable.noType ? symTable.neverType
                    : rhsRecordType.restFieldType;
            BRecordType rhsResType = symbolEnter.createRecordTypeForRestField(pos, data.env, rhsRecordType,
                    mappedFields, rhsRestConstraint);

            types.checkType((lhsVarRef.restParam).pos, rhsResType, lhsRefType, DiagnosticErrorCode.INCOMPATIBLE_TYPES);
        }
    }

    /**
     * This method checks destructure patterns when given an array source.
     *
     * @param pos diagnostic Pos of the tuple de-structure statement.
     * @param target target tuple variable.
     * @param source source type.
     * @param rhsPos position of source expression.
     */
    private void checkArrayVarRefEquivalency(Location pos, BLangTupleVarRef target, BType source,
                                             Location rhsPos, AnalyzerData data) {
        BArrayType arraySource = (BArrayType) source;

        // For unsealed
        if (arraySource.size < target.expressions.size() && arraySource.state != BArrayState.OPEN) {
            dlog.error(rhsPos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, target.getBType(), arraySource);
        }

        BType souceElementType = arraySource.eType;
        for (BLangExpression expression : target.expressions) {
            if (NodeKind.RECORD_VARIABLE_REF == expression.getKind()) {
                BLangRecordVarRef recordVarRef = (BLangRecordVarRef) expression;
                checkRecordVarRefEquivalency(pos, recordVarRef, souceElementType, rhsPos, data);
            } else if (NodeKind.TUPLE_VARIABLE_REF == expression.getKind()) {
                BLangTupleVarRef tupleVarRef = (BLangTupleVarRef) expression;
                checkTupleVarRefEquivalency(pos, tupleVarRef, souceElementType, rhsPos, data);
            } else if (NodeKind.ERROR_VARIABLE_REF == expression.getKind()) {
                BLangErrorVarRef errorVarRef = (BLangErrorVarRef) expression;
                checkErrorVarRefEquivalency(errorVarRef, souceElementType, rhsPos, data);
            } else if (expression.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) expression;
                Name varName = names.fromIdNode(simpleVarRef.variableName);
                if (varName == Names.IGNORE) {
                    continue;
                }

                resetTypeNarrowing(simpleVarRef, data);

                BType targetType = simpleVarRef.getBType();
                if (!types.isAssignable(souceElementType, targetType)) {
                    dlog.error(rhsPos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, target.getBType(), arraySource);
                    break;
                }
            } else {
                dlog.error(expression.pos, DiagnosticErrorCode.INVALID_VARIABLE_REFERENCE_IN_BINDING_PATTERN,
                           expression);
            }
        }
    }

    private void checkTupleVarRefEquivalency(Location pos, BLangTupleVarRef target, BType source,
                                             Location rhsPos, AnalyzerData data) {
        source = Types.getReferredType(source);
        if (source.tag == TypeTags.ARRAY) {
            checkArrayVarRefEquivalency(pos, target, source, rhsPos, data);
            return;
        }

        if (source.tag != TypeTags.TUPLE) {
            dlog.error(rhsPos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, target.getBType(), source);
            return;
        }

        if (target.restParam == null) {
            if (((BTupleType) source).restType != null) {
                dlog.error(rhsPos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, target.getBType(), source);
                return;
            } else if (((BTupleType) source).tupleTypes.size() != target.expressions.size()) {
                dlog.error(rhsPos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, target.getBType(), source);
                return;
            }
        }

        List<BType> sourceTypes = new ArrayList<>(((BTupleType) source).tupleTypes);
        if (((BTupleType) source).restType != null) {
            sourceTypes.add(((BTupleType) source).restType);
        }

        for (int i = 0; i < sourceTypes.size(); i++) {
            BLangExpression varRefExpr;
            if ((target.expressions.size() > i)) {
                varRefExpr = target.expressions.get(i);
            } else {
                varRefExpr = (BLangExpression) target.restParam;
            }

            BType sourceType = sourceTypes.get(i);
            if (NodeKind.RECORD_VARIABLE_REF == varRefExpr.getKind()) {
                BLangRecordVarRef recordVarRef = (BLangRecordVarRef) varRefExpr;
                checkRecordVarRefEquivalency(pos, recordVarRef, sourceType, rhsPos, data);
            } else if (NodeKind.TUPLE_VARIABLE_REF == varRefExpr.getKind()) {
                BLangTupleVarRef tupleVarRef = (BLangTupleVarRef) varRefExpr;
                checkTupleVarRefEquivalency(pos, tupleVarRef, sourceType, rhsPos, data);
            } else if (NodeKind.ERROR_VARIABLE_REF == varRefExpr.getKind()) {
                BLangErrorVarRef errorVarRef = (BLangErrorVarRef) varRefExpr;
                checkErrorVarRefEquivalency(errorVarRef, sourceType, rhsPos, data);
            } else if (varRefExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRefExpr;
                Name varName = names.fromIdNode(simpleVarRef.variableName);
                if (varName == Names.IGNORE) {
                    continue;
                }

                BType targetType;
                resetTypeNarrowing(simpleVarRef, data);
                // Check if this is the rest param and get the type of rest param.
                if ((target.expressions.size() > i)) {
                    targetType = varRefExpr.getBType();
                } else {
                    BType varRefExprType = varRefExpr.getBType();
                    if (varRefExprType.tag == TypeTags.ARRAY) {
                        targetType = ((BArrayType) varRefExprType).eType;
                    } else {
                        targetType = varRefExprType;
                    }
                }

                if (!types.isAssignable(sourceType, targetType)) {
                    dlog.error(rhsPos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, target.getBType(), source);
                    break;
                }
            } else {
                dlog.error(varRefExpr.pos, DiagnosticErrorCode.INVALID_VARIABLE_REFERENCE_IN_BINDING_PATTERN,
                           varRefExpr);
            }
        }
    }

    private void checkErrorVarRefEquivalency(BLangErrorVarRef lhsRef, BType rhsType, Location rhsPos,
                                             AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        if (Types.getReferredType(rhsType).tag != TypeTags.ERROR) {
            dlog.error(rhsPos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, symTable.errorType, rhsType);
            return;
        }
        typeChecker.checkExpr(lhsRef, currentEnv, symTable.noType, data.prevEnvs, data.commonAnalyzerData);
        if (lhsRef.getBType() == symTable.semanticError) {
            return;
        }

        BErrorType rhsErrorType = (BErrorType) Types.getReferredType(rhsType);

        // Wrong error detail type in error type def, error already emitted  to dlog.
        BType refType = Types.getReferredType(rhsErrorType.detailType);
        if (!(refType.tag == TypeTags.RECORD || refType.tag == TypeTags.MAP)) {
            return;
        }
        BRecordType rhsDetailType = this.symbolEnter.getDetailAsARecordType(rhsErrorType);
        Map<String, BField> fields = rhsDetailType.fields;

        BType wideType = interpolateWideType(rhsDetailType, lhsRef.detail);
        for (BLangNamedArgsExpression detailItem : lhsRef.detail) {
            BField matchedDetailItem = fields.get(detailItem.name.value);
            BType matchedType;
            if (matchedDetailItem == null) {
                if (rhsDetailType.sealed) {
                    dlog.error(detailItem.pos, DiagnosticErrorCode.INVALID_FIELD_IN_RECORD_BINDING_PATTERN,
                               detailItem.name);
                    return;
                }
                dlog.error(detailItem.pos, DiagnosticErrorCode.CANNOT_BIND_UNDEFINED_ERROR_DETAIL_FIELD,
                           detailItem.name.value);
                continue;
            }

            detailItem.varSymbol = matchedDetailItem.symbol;

            if (Symbols.isOptional(matchedDetailItem.symbol)) {
                dlog.error(detailItem.pos, DiagnosticErrorCode.INVALID_FIELD_BINDING_PATTERN_WITH_NON_REQUIRED_FIELD);
                continue;
            }

            matchedType = matchedDetailItem.type;
            checkErrorDetailRefItem(detailItem.pos, rhsPos, detailItem, matchedType, data);
            resetTypeNarrowing(detailItem.expr, data);
            if (!types.isAssignable(matchedType, detailItem.expr.getBType())) {
                dlog.error(detailItem.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                           detailItem.expr.getBType(), matchedType);
            }
        }
        if (lhsRef.restVar != null && !isIgnoreVar(lhsRef)) {
            setTypeOfVarRefInErrorBindingAssignment(lhsRef.restVar, data);
            checkInvalidTypeDef(lhsRef.restVar);
            BMapType expRestType = new BMapType(TypeTags.MAP, wideType, null);
            BType restVarType = Types.getReferredType(lhsRef.restVar.getBType());
            if (restVarType.tag != TypeTags.MAP || !types.isAssignable(wideType, ((BMapType) restVarType).constraint)) {
                dlog.error(lhsRef.restVar.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, lhsRef.restVar.getBType(),
                        expRestType);
                return;
            }
            resetTypeNarrowing(lhsRef.restVar, data);
            typeChecker.checkExpr(lhsRef.restVar, currentEnv, data.prevEnvs, data.commonAnalyzerData);
        }
    }

    private BType interpolateWideType(BRecordType rhsDetailType, List<BLangNamedArgsExpression> detailType) {
        Set<String> extractedKeys = detailType.stream().map(detail -> detail.name.value).collect(Collectors.toSet());

        BUnionType wideType = BUnionType.create(null);
        for (BField field : rhsDetailType.fields.values()) {
            // avoid fields extracted from binding pattern
            if (!extractedKeys.contains(field.name.value)) {
                wideType.add(field.type);
            }
        }
        if (!rhsDetailType.sealed) {
            wideType.add(rhsDetailType.restFieldType);
        }
        return wideType;
    }

    private boolean isIgnoreVar(BLangErrorVarRef lhsRef) {
        if (lhsRef.restVar != null && lhsRef.restVar.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            return ((BLangSimpleVarRef) lhsRef.restVar).variableName.value.equals(Names.IGNORE.value);
        }
        return false;
    }

    private void checkErrorDetailRefItem(Location location,
                                         Location rhsLocation,
                                         BLangNamedArgsExpression detailItem,
                                         BType expectedType, AnalyzerData data) {
        if (detailItem.expr.getKind() == NodeKind.RECORD_VARIABLE_REF) {
            typeChecker.checkExpr(detailItem.expr, data.env, data.prevEnvs, data.commonAnalyzerData);
            checkRecordVarRefEquivalency(location, (BLangRecordVarRef) detailItem.expr, expectedType,
                    rhsLocation, data);
            return;
        }

        if (detailItem.getKind() == NodeKind.SIMPLE_VARIABLE_REF && detailItem.name.value.equals(Names.IGNORE.value)) {
            return;
        }

        setTypeOfVarRefInErrorBindingAssignment(detailItem.expr, data);
        checkInvalidTypeDef(detailItem.expr);
    }

    private void checkConstantAssignment(BLangExpression varRef, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        if (varRef.getBType() == symTable.semanticError) {
            return;
        }

        if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return;
        }

        BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
        if (simpleVarRef.pkgSymbol != null && simpleVarRef.pkgSymbol.tag == SymTag.XMLNS) {
            dlog.error(varRef.pos, DiagnosticErrorCode.XML_QNAME_UPDATE_NOT_ALLOWED);
            return;
        }

        Name varName = names.fromIdNode(simpleVarRef.variableName);
        if (!Names.IGNORE.equals(varName) && currentEnv.enclInvokable != currentEnv.enclPkg.initFunction) {
            if ((simpleVarRef.symbol.flags & Flags.FINAL) == Flags.FINAL) {
                if ((simpleVarRef.symbol.flags & Flags.SERVICE) == Flags.SERVICE) {
                    dlog.error(varRef.pos, DiagnosticErrorCode.INVALID_ASSIGNMENT_DECLARATION_FINAL, Names.SERVICE);
                } else if ((simpleVarRef.symbol.flags & Flags.LISTENER) == Flags.LISTENER) {
                    dlog.error(varRef.pos, DiagnosticErrorCode.INVALID_ASSIGNMENT_DECLARATION_FINAL, LISTENER_NAME);
                }
            } else if ((simpleVarRef.symbol.flags & Flags.CONSTANT) == Flags.CONSTANT) {
                dlog.error(varRef.pos, DiagnosticErrorCode.CANNOT_ASSIGN_VALUE_TO_CONSTANT);
            } else if ((simpleVarRef.symbol.flags & Flags.FUNCTION_FINAL) == Flags.FUNCTION_FINAL) {
                dlog.error(varRef.pos, DiagnosticErrorCode.CANNOT_ASSIGN_VALUE_FUNCTION_ARGUMENT, varRef);
            }
        }
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        // Creates a new environment here.
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, currentEnv.scope);
        currentEnv.copyTo(stmtEnv);
        BLangExpression expr = exprStmtNode.expr;
        BType bType = typeChecker.checkExpr(expr, stmtEnv, data.prevEnvs, data.commonAnalyzerData);
        if (bType != symTable.nilType && bType != symTable.semanticError &&
                expr.getKind() != NodeKind.FAIL &&
                !types.isNeverTypeOrStructureTypeWithARequiredNeverMember(bType)) {
            dlog.error(exprStmtNode.pos, DiagnosticErrorCode.ASSIGNMENT_REQUIRED, bType);
        } else if (expr.getKind() == NodeKind.INVOCATION &&
                types.isNeverTypeOrStructureTypeWithARequiredNeverMember(expr.getBType())) {
            data.notCompletedNormally = true;
        }
        validateWorkerAnnAttachments(exprStmtNode.expr, data);
    }

    @Override
    public void visit(BLangIf ifNode, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        typeChecker.checkExpr(ifNode.expr, currentEnv, symTable.booleanType, data.prevEnvs,
                data.commonAnalyzerData);
        BType actualType = ifNode.expr.getBType();
        if (TypeTags.TUPLE == Types.getReferredType(actualType).tag) {
            dlog.error(ifNode.expr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, symTable.booleanType, actualType);
        }

        Map<BVarSymbol, BType.NarrowedTypes> prevNarrowedTypeInfo = data.narrowedTypeInfo;

        // This map keeps the narrowed types of inner if statements and propagate the false types to the outer
        // block when the flow goes from out of the else block in compile time.
        Map<BVarSymbol, BType.NarrowedTypes> falseTypesOfNarrowedTypes = new HashMap<>();

        SymbolEnv ifEnv = typeNarrower.evaluateTruth(ifNode.expr, ifNode.body, currentEnv);

        data.narrowedTypeInfo = new HashMap<>();

        data.env = ifEnv;
        analyzeStmt(ifNode.body, data);

        if (ifNode.expr.narrowedTypeInfo == null || ifNode.expr.narrowedTypeInfo.isEmpty()) {
            ifNode.expr.narrowedTypeInfo = data.narrowedTypeInfo;
        } else {
            Map<BVarSymbol, BType.NarrowedTypes> existingNarrowedTypeInfo = ifNode.expr.narrowedTypeInfo;
            for (Map.Entry<BVarSymbol, BType.NarrowedTypes> entry : data.narrowedTypeInfo.entrySet()) {
                BVarSymbol key = entry.getKey();
                if (!existingNarrowedTypeInfo.containsKey(key)) {
                    existingNarrowedTypeInfo.put(key, entry.getValue());
                } else {
                    BType.NarrowedTypes existingNarrowTypes = existingNarrowedTypeInfo.get(key);
                    BUnionType unionType =
                            BUnionType.create(null, existingNarrowTypes.trueType, existingNarrowTypes.falseType);
                    BType.NarrowedTypes newPair = new BType.NarrowedTypes(existingNarrowTypes.trueType, unionType);
                    falseTypesOfNarrowedTypes.put(key, newPair);
                }
            }
        }

        if (prevNarrowedTypeInfo != null) {
            prevNarrowedTypeInfo.putAll(data.narrowedTypeInfo);
        }

        if (ifNode.elseStmt != null) {
            boolean ifCompletionStatus = data.notCompletedNormally;
            resetNotCompletedNormally(data);
            SymbolEnv elseEnv = typeNarrower.evaluateFalsity(ifNode.expr, ifNode.elseStmt, currentEnv, false);
            BLangStatement elseStmt = ifNode.elseStmt;
            data.env = elseEnv;
            analyzeStmt(elseStmt, data);
            if (elseStmt.getKind() == NodeKind.IF) {
                data.notCompletedNormally = ifCompletionStatus && data.notCompletedNormally;
            }
        }
        data.narrowedTypeInfo = prevNarrowedTypeInfo;
        if (data.narrowedTypeInfo != null) {
            data.narrowedTypeInfo.putAll(falseTypesOfNarrowedTypes);
        }
    }

    private void resetNotCompletedNormally(AnalyzerData data) {
        data.notCompletedNormally = false;
    }

    @Override
    public void visit(BLangMatchStatement matchStatement, AnalyzerData data) {
        typeChecker.checkExpr(matchStatement.expr, data.env, symTable.noType, data.prevEnvs,
                data.commonAnalyzerData);

        List<BLangMatchClause> matchClauses = matchStatement.matchClauses;
        if (matchClauses.size() == 0) {
            return;
        }
        analyzeNode(matchClauses.get(0), data);

        SymbolEnv matchClauseEnv = data.env;
        for (int i = 1; i < matchClauses.size(); i++) {
            BLangMatchClause prevMatchClause = matchClauses.get(i - 1);
            BLangMatchClause currentMatchClause = matchClauses.get(i);
            matchClauseEnv = typeNarrower.evaluateTruth(matchStatement.expr, prevMatchClause.patternsType,
                    currentMatchClause, matchClauseEnv);
            data.env = matchClauseEnv;
            analyzeNode(currentMatchClause, data);
        }

        if (matchStatement.onFailClause != null) {
            this.analyzeNode(matchStatement.onFailClause, data);
        }
    }

    @Override
    public void visit(BLangMatchClause matchClause, AnalyzerData data) {
        List<BLangMatchPattern> matchPatterns = matchClause.matchPatterns;
        if (matchPatterns.size() == 0) {
            return;
        }
        SymbolEnv currentEnv = data.env;
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(matchClause.blockStmt, currentEnv);
        Map<String, BVarSymbol> clauseVariables = matchClause.declaredVars;

        for (BLangMatchPattern matchPattern : matchPatterns) {
            data.env = SymbolEnv.createPatternEnv(matchPattern, currentEnv);
            analyzeNode(matchPattern, data);
            resolveMatchClauseVariableTypes(matchPattern, clauseVariables, blockEnv);
            if (matchPattern.getKind() == NodeKind.CONST_MATCH_PATTERN) {
                continue;
            }
            if (matchClause.patternsType == null) {
                matchClause.patternsType = matchPattern.getBType();
                continue;
            }
            matchClause.patternsType = types.mergeTypes(matchClause.patternsType, matchPattern.getBType());
        }

        BLangMatchGuard matchGuard = matchClause.matchGuard;
        if (matchGuard != null) {
            data.env = blockEnv;
            analyzeNode(matchGuard, data);
            blockEnv = typeNarrower.evaluateTruth(matchGuard.expr, matchClause.blockStmt, blockEnv);

            for (Map.Entry<BVarSymbol, BType.NarrowedTypes> entry :
                    matchGuard.expr.narrowedTypeInfo.entrySet()) {
                if (entry.getValue().trueType == symTable.semanticError) {
                    dlog.warning(matchClause.pos, DiagnosticWarningCode.MATCH_STMT_UNMATCHED_PATTERN);
                }
            }

            evaluatePatternsTypeAccordingToMatchGuard(matchClause, matchGuard.expr, blockEnv);
        }
        data.env = blockEnv;
        analyzeStmt(matchClause.blockStmt, data);
    }

    private void resolveMatchClauseVariableTypes(BLangMatchPattern matchPattern,
                                                 Map<String, BVarSymbol> clauseVariables, SymbolEnv env) {
        Map<String, BVarSymbol> patternVariables = matchPattern.declaredVars;
        for (String patternVariableName : patternVariables.keySet()) {
            BVarSymbol patternVariableSymbol = patternVariables.get(patternVariableName);
            if (!clauseVariables.containsKey(patternVariableName)) {
                BVarSymbol clauseVarSymbol = symbolEnter.defineVarSymbol(patternVariableSymbol.pos,
                        patternVariableSymbol.getFlags(), patternVariableSymbol.type, patternVariableSymbol.name,
                        env, false);
                clauseVariables.put(patternVariableName, clauseVarSymbol);
                continue;
            }
            BVarSymbol clauseVariableSymbol = clauseVariables.get(patternVariableName);
            clauseVariableSymbol.type = types.mergeTypes(clauseVariableSymbol.type, patternVariableSymbol.type);
        }
    }

    private void evaluatePatternsTypeAccordingToMatchGuard(BLangMatchClause matchClause, BLangExpression matchGuardExpr,
                                                           SymbolEnv env) {
        List<BLangMatchPattern> matchPatterns = matchClause.matchPatterns;
        if (matchGuardExpr.getKind() != NodeKind.TYPE_TEST_EXPR && matchGuardExpr.getKind() != NodeKind.BINARY_EXPR) {
            return;
        }
        evaluateMatchPatternsTypeAccordingToMatchGuard(matchPatterns.get(0), env);
        BType clauseType = matchPatterns.get(0).getBType();
        for (int i = 1; i < matchPatterns.size(); i++) {
            evaluateMatchPatternsTypeAccordingToMatchGuard(matchPatterns.get(i), env);
            clauseType = types.mergeTypes(matchPatterns.get(i).getBType(), clauseType);
        }
        matchClause.patternsType = clauseType;
    }

    private void evaluateBindingPatternsTypeAccordingToMatchGuard(BLangBindingPattern bindingPattern, SymbolEnv env) {
        NodeKind bindingPatternKind = bindingPattern.getKind();
        switch (bindingPatternKind) {
            case CAPTURE_BINDING_PATTERN:
                BLangCaptureBindingPattern captureBindingPattern =
                        (BLangCaptureBindingPattern) bindingPattern;
                Name varName =
                        new Name(captureBindingPattern.getIdentifier().getValue());
                if (env.scope.entries.containsKey(varName)) {
                    captureBindingPattern.setBType(env.scope.entries.get(varName).symbol.type);
                }
                break;
            default:
                break;
        }
    }

    private void evaluateMatchPatternsTypeAccordingToMatchGuard(BLangMatchPattern matchPattern, SymbolEnv env) {
        NodeKind patternKind = matchPattern.getKind();
        switch (patternKind) {
            case VAR_BINDING_PATTERN_MATCH_PATTERN:
                BLangVarBindingPatternMatchPattern varBindingPatternMatchPattern =
                        (BLangVarBindingPatternMatchPattern) matchPattern;
                BLangBindingPattern bindingPattern = varBindingPatternMatchPattern.getBindingPattern();
                evaluateBindingPatternsTypeAccordingToMatchGuard(bindingPattern, env);
                varBindingPatternMatchPattern.setBType(bindingPattern.getBType());
                break;
            case LIST_MATCH_PATTERN:
                BLangListMatchPattern listMatchPattern = (BLangListMatchPattern) matchPattern;
                List<BType> memberTypes = new ArrayList<>();
                for (BLangMatchPattern memberMatchPattern : listMatchPattern.matchPatterns) {
                    evaluateMatchPatternsTypeAccordingToMatchGuard(memberMatchPattern, env);
                    memberTypes.add(memberMatchPattern.getBType());
                }
                BTupleType matchPatternType = new BTupleType(memberTypes);

                if (listMatchPattern.restMatchPattern != null) {
                    evaluateMatchPatternsTypeAccordingToMatchGuard(listMatchPattern.restMatchPattern, env);
                    BType listRestType = listMatchPattern.restMatchPattern.getBType();
                    if (listRestType.tag == TypeTags.TUPLE) {
                        BTupleType restTupleType = (BTupleType) listRestType;
                        matchPatternType.tupleTypes.addAll(restTupleType.tupleTypes);
                        matchPatternType.restType = restTupleType.restType;
                    } else {
                        matchPatternType.restType = ((BArrayType) listRestType).eType;
                    }
                }
                listMatchPattern.setBType(matchPatternType);
                break;
            case REST_MATCH_PATTERN:
                BLangRestMatchPattern restMatchPattern = (BLangRestMatchPattern) matchPattern;
                Name varName = new Name(restMatchPattern.variableName.value);
                if (env.scope.entries.containsKey(varName)) {
                    restMatchPattern.setBType(env.scope.entries.get(varName).symbol.type);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void visit(BLangMatchGuard matchGuard, AnalyzerData data) {
        typeChecker.checkExpr(matchGuard.expr, data.env, symTable.booleanType, data.prevEnvs,
                data.commonAnalyzerData);
    }

    @Override
    public void visit(BLangMappingMatchPattern mappingMatchPattern, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        BRecordTypeSymbol recordSymbol = symbolEnter.createAnonRecordSymbol(currentEnv, mappingMatchPattern.pos);
        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();

        for (BLangFieldMatchPattern fieldMatchPattern : mappingMatchPattern.fieldMatchPatterns) {
            analyzeNode(fieldMatchPattern, data);
            Name fieldName = names.fromIdNode(fieldMatchPattern.fieldName);
            BVarSymbol fieldSymbol = new BVarSymbol(0, fieldName,
                                                    names.originalNameFromIdNode(fieldMatchPattern.fieldName),
                                                    currentEnv.enclPkg.symbol.pkgID,
                                                    fieldMatchPattern.matchPattern.getBType(),
                                                    recordSymbol, fieldMatchPattern.pos, COMPILED_SOURCE);
            BField field = new BField(fieldName, fieldMatchPattern.pos, fieldSymbol);
            fields.put(fieldName.getValue(), field);
            mappingMatchPattern.declaredVars.putAll(fieldMatchPattern.declaredVars);
        }
        BRecordType recordVarType = new BRecordType(recordSymbol);
        recordVarType.fields = fields;
        recordVarType.restFieldType = symTable.anyOrErrorType;
        if (mappingMatchPattern.restMatchPattern != null) {
            BRecordTypeSymbol matchPattenRecordSym =
                    symbolEnter.createAnonRecordSymbol(currentEnv, mappingMatchPattern.pos);
            BLangRestMatchPattern restMatchPattern = mappingMatchPattern.restMatchPattern;
            BType restType = restMatchPattern.getBType();
            BRecordType matchPatternRecType = new BRecordType(matchPattenRecordSym);
            matchPatternRecType.restFieldType = restType != null ? restType : symTable.anyOrErrorType;
            recordVarType.restFieldType = matchPatternRecType.restFieldType;
            restMatchPattern.setBType(matchPatternRecType);
            analyzeNode(restMatchPattern, data);
            mappingMatchPattern.declaredVars.put(restMatchPattern.variableName.value, restMatchPattern.symbol);

            BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(matchPatternRecType,
                    currentEnv.enclPkg.packageID, symTable, mappingMatchPattern.pos);
            recordTypeNode.initFunction =
                    TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, currentEnv, names, symTable);
            TypeDefBuilderHelper.createTypeDefinitionForTSymbol(matchPatternRecType, matchPattenRecordSym,
                    recordTypeNode, currentEnv);
        }

        mappingMatchPattern.setBType(types.resolvePatternTypeFromMatchExpr(mappingMatchPattern,
                                                                           recordVarType, currentEnv));
        assignTypesToMemberPatterns(mappingMatchPattern, mappingMatchPattern.getBType(), data);
    }

    private void assignTypesToMemberPatterns(BLangMatchPattern matchPattern, BType bType, AnalyzerData data) {
        BType patternType = Types.getReferredType(bType);
        NodeKind matchPatternKind = matchPattern.getKind();
        if (patternType.tag == TypeTags.INTERSECTION) {
            patternType = ((BIntersectionType) patternType).effectiveType;
        }
        switch (matchPatternKind) {
            case WILDCARD_MATCH_PATTERN:
            case CONST_MATCH_PATTERN:
                return;
            case VAR_BINDING_PATTERN_MATCH_PATTERN:
                BLangBindingPattern bindingPattern =
                        ((BLangVarBindingPatternMatchPattern) matchPattern).getBindingPattern();
                assignTypesToMemberPatterns(bindingPattern, patternType, data);
                matchPattern.setBType(bindingPattern.getBType());
                return;
            case LIST_MATCH_PATTERN:
                BLangListMatchPattern listMatchPattern = (BLangListMatchPattern) matchPattern;
                if (patternType.tag == TypeTags.UNION) {
                    for (BType type : ((BUnionType) patternType).getMemberTypes()) {
                        assignTypesToMemberPatterns(listMatchPattern, type, data);
                    }
                    listMatchPattern.setBType(patternType);
                    return;
                }
                if (patternType.tag == TypeTags.ARRAY) {
                    BArrayType arrayType = (BArrayType) patternType;
                    for (BLangMatchPattern memberPattern : listMatchPattern.matchPatterns) {
                        assignTypesToMemberPatterns(memberPattern, arrayType.eType, data);
                    }
                    if (listMatchPattern.restMatchPattern == null) {
                        return;
                    }
                    if (arrayType.state == BArrayState.CLOSED) {
                        BTupleType restTupleType = createTupleForClosedArray(
                                arrayType.size - listMatchPattern.matchPatterns.size(), arrayType.eType);
                        listMatchPattern.restMatchPattern.setBType(restTupleType);
                        BVarSymbol restMatchPatternSymbol = listMatchPattern.restMatchPattern.declaredVars
                                .get(listMatchPattern.restMatchPattern.getIdentifier().getValue());
                        restMatchPatternSymbol.type = restTupleType;
                        return;
                    }
                    BLangRestMatchPattern restMatchPattern = listMatchPattern.restMatchPattern;
                    BType restType = ((BArrayType) restMatchPattern.getBType()).eType;
                    restType = types.mergeTypes(restType, arrayType.eType);
                    ((BArrayType) restMatchPattern.getBType()).eType = restType;
                    return;
                }
                if (patternType.tag != TypeTags.TUPLE) {
                    return;
                }
                BTupleType patternTupleType = (BTupleType) patternType;
                List<BType> types = patternTupleType.tupleTypes;
                List<BLangMatchPattern> matchPatterns = listMatchPattern.matchPatterns;
                List<BType> memberTypes = new ArrayList<>();
                for (int i = 0; i < matchPatterns.size(); i++) {
                    assignTypesToMemberPatterns(matchPatterns.get(i), types.get(i), data);
                    memberTypes.add(matchPatterns.get(i).getBType());
                }
                BTupleType tupleType = new BTupleType(memberTypes);

                if (listMatchPattern.restMatchPattern == null) {
                    listMatchPattern.setBType(tupleType);
                    return;
                }
                tupleType.restType = createTypeForTupleRestType(matchPatterns.size(), types, patternTupleType.restType);
                listMatchPattern.restMatchPattern.setBType(tupleType.restType);
                matchPattern.setBType(patternType);
                BVarSymbol restMatchPatternSymbol = listMatchPattern.restMatchPattern.declaredVars
                        .get(listMatchPattern.restMatchPattern.getIdentifier().getValue());
                restMatchPatternSymbol.type = tupleType.restType;
                return;
            case MAPPING_MATCH_PATTERN:
                BLangMappingMatchPattern mappingMatchPattern = (BLangMappingMatchPattern) matchPattern;
                if (patternType.tag == TypeTags.UNION) {
                    for (BType type : ((BUnionType) patternType).getMemberTypes()) {
                        assignTypesToMemberPatterns(mappingMatchPattern, type, data);
                    }
                    return;
                }
                if (patternType.tag != TypeTags.RECORD) {
                    return;
                }
                BRecordType recordType = (BRecordType) patternType;
                List<String> boundedFieldNames = new ArrayList<>();

                for (BLangFieldMatchPattern fieldMatchPattern : mappingMatchPattern.fieldMatchPatterns) {
                    assignTypesToMemberPatterns(fieldMatchPattern.matchPattern,
                            recordType.fields.get(fieldMatchPattern.fieldName.value).type, data);
                    boundedFieldNames.add(fieldMatchPattern.fieldName.value);
                }

                if (mappingMatchPattern.restMatchPattern == null) {
                    return;
                }
                BLangRestMatchPattern restMatchPattern = mappingMatchPattern.restMatchPattern;
                BRecordType restPatternRecType = (BRecordType) restMatchPattern.getBType();
                BVarSymbol restVarSymbol =
                        restMatchPattern.declaredVars.get(restMatchPattern.getIdentifier().getValue());
                if (restVarSymbol.type.tag != TypeTags.RECORD) {
                    return;
                }
                BRecordType restVarSymbolRecordType = (BRecordType) restVarSymbol.type;
                setRestMatchPatternConstraintType(recordType, boundedFieldNames, restPatternRecType,
                        restVarSymbolRecordType, data);
        }
    }

    private BTupleType createTupleForClosedArray(int noOfElements, BType elementType) {
        List<BType> memTypes = Collections.nCopies(noOfElements, elementType);
        return new BTupleType(memTypes);
    }

    private BType createTypeForTupleRestType(int startIndex, List<BType> types, BType patternRestType) {
        List<BType> remainingTypes = new ArrayList<>();
        for (int i = startIndex; i < types.size(); i++) {
            remainingTypes.add(types.get(i));
        }
        if (!remainingTypes.isEmpty()) {
            BTupleType restTupleType = new BTupleType(remainingTypes);
            if (patternRestType != null) {
                restTupleType.restType = patternRestType;
            }
            return restTupleType;
        } else {
            if (patternRestType != null) {
                return new BArrayType(patternRestType);
            } else {
                return new BArrayType(symTable.anyOrErrorType);
            }
        }
    }

    @Override
    public void visit(BLangFieldMatchPattern fieldMatchPattern, AnalyzerData data) {
        BLangMatchPattern matchPattern = fieldMatchPattern.matchPattern;
        matchPattern.accept(this, data);
        fieldMatchPattern.declaredVars.putAll(matchPattern.declaredVars);
    }


    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        BLangBindingPattern bindingPattern = varBindingPattern.getBindingPattern();
        NodeKind patternKind = bindingPattern.getKind();
        BType patternType = null;
        if (varBindingPattern.matchExpr != null) {
            patternType = varBindingPattern.matchExpr.getBType();
        }

        switch (patternKind) {
            case WILDCARD_BINDING_PATTERN:
                BLangWildCardBindingPattern wildCardBindingPattern = (BLangWildCardBindingPattern) bindingPattern;
                wildCardBindingPattern.setBType(patternType);
                analyzeNode(wildCardBindingPattern, data);
                varBindingPattern.isLastPattern = types.isAssignable(wildCardBindingPattern.getBType(),
                                                                     symTable.anyType);
                break;
            case CAPTURE_BINDING_PATTERN:
                BLangCaptureBindingPattern captureBindingPattern = (BLangCaptureBindingPattern) bindingPattern;
                captureBindingPattern.setBType(
                        varBindingPattern.getBType() == null ? patternType : varBindingPattern.getBType());
                analyzeNode(captureBindingPattern, data);
                break;
            case LIST_BINDING_PATTERN:
                BLangListBindingPattern listBindingPattern = (BLangListBindingPattern) bindingPattern;
                analyzeNode(listBindingPattern, data);
                listBindingPattern.setBType(types.resolvePatternTypeFromMatchExpr(listBindingPattern, varBindingPattern,
                        currentEnv));
                assignTypesToMemberPatterns(listBindingPattern, listBindingPattern.getBType(), data);
                break;
            case ERROR_BINDING_PATTERN:
                BLangErrorBindingPattern errorBindingPattern = (BLangErrorBindingPattern) bindingPattern;
                analyzeNode(errorBindingPattern, data);
                errorBindingPattern.setBType(types.resolvePatternTypeFromMatchExpr(errorBindingPattern,
                                                                                   varBindingPattern.matchExpr,
                        currentEnv));
                break;
            case MAPPING_BINDING_PATTERN:
                BLangMappingBindingPattern mappingBindingPattern = (BLangMappingBindingPattern) bindingPattern;
                analyzeNode(mappingBindingPattern, data);
                mappingBindingPattern.setBType(types.resolvePatternTypeFromMatchExpr(mappingBindingPattern,
                                                                                     varBindingPattern, currentEnv));
                assignTypesToMemberPatterns(mappingBindingPattern, mappingBindingPattern.getBType(), data);
                break;
            default:
        }
        varBindingPattern.declaredVars.putAll(bindingPattern.declaredVars);
        varBindingPattern.setBType(bindingPattern.getBType());
    }

    @Override
    public void visit(BLangWildCardBindingPattern wildCardBindingPattern, AnalyzerData data) {
        if (wildCardBindingPattern.getBType() == null) {
            wildCardBindingPattern.setBType(symTable.anyType);
            return;
        }
        BType bindingPatternType = wildCardBindingPattern.getBType();
        BType intersectionType = types.getTypeIntersection(
                Types.IntersectionContext.compilerInternalIntersectionContext(),
                bindingPatternType, symTable.anyType, data.env);
        if (intersectionType == symTable.semanticError) {
            wildCardBindingPattern.setBType(symTable.noType);
            return;
        }
        wildCardBindingPattern.setBType(intersectionType);
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern, AnalyzerData data) {
        BLangIdentifier id = (BLangIdentifier) captureBindingPattern.getIdentifier();
        Name name = new Name(id.getValue());
        Name origName = new Name(id.originalValue);
        captureBindingPattern.setBType(captureBindingPattern.getBType() == null ? symTable.anyOrErrorType :
                                               captureBindingPattern.getBType());
        captureBindingPattern.symbol = symbolEnter.defineVarSymbol(captureBindingPattern.getIdentifier().getPosition(),
                                                                   Flags.unMask(0), captureBindingPattern.getBType(),
                                                                   name, origName, data.env, false);
        captureBindingPattern.declaredVars.put(name.value, captureBindingPattern.symbol);
    }

    @Override
    public void visit(BLangListBindingPattern listBindingPattern, AnalyzerData data) {
        List<BType> listMemberTypes = new ArrayList<>();
        for (BLangBindingPattern bindingPattern : listBindingPattern.bindingPatterns) {
            analyzeNode(bindingPattern, data);
            listMemberTypes.add(bindingPattern.getBType());
            listBindingPattern.declaredVars.putAll(bindingPattern.declaredVars);
        }
        BTupleType listBindingPatternType = new BTupleType(listMemberTypes);

        if (listBindingPattern.restBindingPattern != null) {
            BLangRestBindingPattern restBindingPattern = listBindingPattern.restBindingPattern;
            BType restBindingPatternType = restBindingPattern.getBType();
            BType restType = restBindingPatternType != null ? restBindingPatternType : symTable.anyOrErrorType;
            restBindingPattern.setBType(new BArrayType(restType));
            restBindingPattern.accept(this, data);
            listBindingPattern.declaredVars.put(restBindingPattern.variableName.value, restBindingPattern.symbol);
            listBindingPatternType.restType = restType;
        }
        listBindingPattern.setBType(listBindingPatternType);
    }

    @Override
    public void visit(BLangRestBindingPattern restBindingPattern, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        Name name = new Name(restBindingPattern.variableName.value);
        Name origName = names.originalNameFromIdNode(restBindingPattern.variableName);
        BSymbol symbol = symResolver.lookupSymbolInMainSpace(currentEnv, name);
        if (symbol == symTable.notFoundSymbol) {
            symbol = new BVarSymbol(0, name, origName, currentEnv.enclPkg.packageID, restBindingPattern.getBType(),
                                    currentEnv.scope.owner, restBindingPattern.variableName.pos, SOURCE);
            symbolEnter.defineSymbol(restBindingPattern.variableName.pos, symbol, currentEnv);
        }
        restBindingPattern.symbol = (BVarSymbol) symbol;
        restBindingPattern.declaredVars.put(name.value, restBindingPattern.symbol);
    }

    @Override
    public void visit(BLangErrorBindingPattern errorBindingPattern, AnalyzerData data) {
        if (errorBindingPattern.errorTypeReference != null) {
            errorBindingPattern.setBType(symResolver.resolveTypeNode(errorBindingPattern.errorTypeReference, data.env));
        } else {
            errorBindingPattern.setBType(symTable.errorType);
        }

        if (errorBindingPattern.errorMessageBindingPattern != null) {
            analyzeNode(errorBindingPattern.errorMessageBindingPattern, data);
            errorBindingPattern.declaredVars.putAll(errorBindingPattern.errorMessageBindingPattern.declaredVars);
        }

        if (errorBindingPattern.errorCauseBindingPattern != null) {
            analyzeNode(errorBindingPattern.errorCauseBindingPattern, data);
            errorBindingPattern.declaredVars.putAll(errorBindingPattern.errorCauseBindingPattern.declaredVars);
        }

        if (errorBindingPattern.errorFieldBindingPatterns != null) {
            analyzeNode(errorBindingPattern.errorFieldBindingPatterns, data);
            errorBindingPattern.declaredVars.putAll(errorBindingPattern.errorFieldBindingPatterns.declaredVars);
        }
    }

    @Override
    public void visit(BLangSimpleBindingPattern simpleBindingPattern, AnalyzerData data) {
        if (simpleBindingPattern.wildCardBindingPattern != null) {
            analyzeNode(simpleBindingPattern.wildCardBindingPattern, data);
            return;
        }
        if (simpleBindingPattern.captureBindingPattern != null) {
            analyzeNode(simpleBindingPattern.captureBindingPattern, data);
            simpleBindingPattern.declaredVars.putAll(simpleBindingPattern.captureBindingPattern.declaredVars);
        }
    }

    @Override
    public void visit(BLangErrorMessageBindingPattern errorMessageBindingPattern, AnalyzerData data) {
        BLangSimpleBindingPattern simpleBindingPattern = errorMessageBindingPattern.simpleBindingPattern;
        if (simpleBindingPattern.wildCardBindingPattern != null) {
            simpleBindingPattern.wildCardBindingPattern.setBType(symTable.stringType);
        }
        if (simpleBindingPattern.captureBindingPattern != null) {
            simpleBindingPattern.captureBindingPattern.setBType(symTable.stringType);
        }
        analyzeNode(simpleBindingPattern, data);
        errorMessageBindingPattern.declaredVars.putAll(simpleBindingPattern.declaredVars);
    }

    @Override
    public void visit(BLangErrorCauseBindingPattern errorCauseBindingPattern, AnalyzerData data) {
        if (errorCauseBindingPattern.simpleBindingPattern != null) {
            BLangSimpleBindingPattern simpleBindingPattern = errorCauseBindingPattern.simpleBindingPattern;
            if (simpleBindingPattern.captureBindingPattern != null) {
                simpleBindingPattern.captureBindingPattern.setBType(symTable.errorOrNilType);
            }
            analyzeNode(simpleBindingPattern, data);
            errorCauseBindingPattern.declaredVars.putAll(simpleBindingPattern.declaredVars);
            return;
        }
        if (errorCauseBindingPattern.errorBindingPattern != null) {
            analyzeNode(errorCauseBindingPattern.errorBindingPattern, data);
            errorCauseBindingPattern.declaredVars.putAll(errorCauseBindingPattern.errorBindingPattern.declaredVars);
        }
    }

    @Override
    public void visit(BLangErrorFieldBindingPatterns errorFieldBindingPatterns, AnalyzerData data) {
        for (BLangNamedArgBindingPattern namedArgBindingPattern : errorFieldBindingPatterns.namedArgBindingPatterns) {
            analyzeNode(namedArgBindingPattern, data);
            errorFieldBindingPatterns.declaredVars.putAll(namedArgBindingPattern.declaredVars);
        }
        if (errorFieldBindingPatterns.restBindingPattern != null) {
            errorFieldBindingPatterns.restBindingPattern.setBType(
                    new BMapType(TypeTags.MAP, symTable.anydataType, null));
            analyzeNode(errorFieldBindingPatterns.restBindingPattern, data);
            errorFieldBindingPatterns.declaredVars.putAll(errorFieldBindingPatterns.restBindingPattern.declaredVars);
        }
    }

    @Override
    public void visit(BLangNamedArgBindingPattern namedArgBindingPattern, AnalyzerData data) {
        setNamedArgBindingPatternType(namedArgBindingPattern.bindingPattern);
        analyzeNode(namedArgBindingPattern.bindingPattern, data);
        namedArgBindingPattern.declaredVars.putAll(namedArgBindingPattern.bindingPattern.declaredVars);
    }

    private void setNamedArgBindingPatternType(BLangBindingPattern bindingPattern) {
        switch(bindingPattern.getKind()) {
            case LIST_BINDING_PATTERN:
                BLangListBindingPattern listBindingPattern = (BLangListBindingPattern) bindingPattern;
                listBindingPattern.bindingPatterns.forEach(pattern -> pattern.setBType(symTable.cloneableType));
                if (listBindingPattern.restBindingPattern != null) {
                    listBindingPattern.restBindingPattern.setBType(symTable.cloneableType);
                }
                break;
            case MAPPING_BINDING_PATTERN:
                BLangMappingBindingPattern mappingBindingPattern = (BLangMappingBindingPattern) bindingPattern;
                mappingBindingPattern.fieldBindingPatterns.forEach(pattern ->
                        pattern.bindingPattern.setBType(symTable.cloneableType));
                if (mappingBindingPattern.restBindingPattern != null) {
                    mappingBindingPattern.restBindingPattern.setBType(symTable.cloneableType);
                }
                break;
            case CAPTURE_BINDING_PATTERN:
                BLangCaptureBindingPattern captureBindingPattern = (BLangCaptureBindingPattern) bindingPattern;
                captureBindingPattern.setBType(symTable.cloneableType);
                break;
        }
    }

    @Override
    public void visit(BLangErrorMatchPattern errorMatchPattern, AnalyzerData data) {
        if (errorMatchPattern.errorTypeReference != null) {
            errorMatchPattern.setBType(symResolver.resolveTypeNode(errorMatchPattern.errorTypeReference, data.env));
        } else {
            errorMatchPattern.setBType(symTable.errorType);
        }
        errorMatchPattern.setBType(types.resolvePatternTypeFromMatchExpr(errorMatchPattern,
                                                                         errorMatchPattern.matchExpr));

        if (errorMatchPattern.errorMessageMatchPattern != null) {
            analyzeNode(errorMatchPattern.errorMessageMatchPattern, data);
            errorMatchPattern.declaredVars.putAll(errorMatchPattern.errorMessageMatchPattern.declaredVars);
        }

        if (errorMatchPattern.errorCauseMatchPattern != null) {
            analyzeNode(errorMatchPattern.errorCauseMatchPattern, data);
            errorMatchPattern.declaredVars.putAll(errorMatchPattern.errorCauseMatchPattern.declaredVars);
        }

        if (errorMatchPattern.errorFieldMatchPatterns != null) {
            analyzeNode(errorMatchPattern.errorFieldMatchPatterns, data);
            errorMatchPattern.declaredVars.putAll(errorMatchPattern.errorFieldMatchPatterns.declaredVars);
        }
    }

    @Override
    public void visit(BLangSimpleMatchPattern simpleMatchPattern, AnalyzerData data) {
        if (simpleMatchPattern.wildCardMatchPattern != null) {
            analyzeNode(simpleMatchPattern.wildCardMatchPattern, data);
            simpleMatchPattern.wildCardMatchPattern.isLastPattern = true;
            return;
        }
        if (simpleMatchPattern.constPattern != null) {
            analyzeNode(simpleMatchPattern.constPattern, data);
            return;
        }
        if (simpleMatchPattern.varVariableName != null) {
            analyzeNode(simpleMatchPattern.varVariableName, data);
            simpleMatchPattern.declaredVars.putAll(simpleMatchPattern.varVariableName.declaredVars);
        }
    }

    @Override
    public void visit(BLangErrorMessageMatchPattern errorMessageMatchPattern, AnalyzerData data) {
        BLangSimpleMatchPattern simpleMatchPattern = errorMessageMatchPattern.simpleMatchPattern;
        if (simpleMatchPattern.varVariableName != null) {
            simpleMatchPattern.varVariableName.setBType(symTable.stringType);
        }
        analyzeNode(simpleMatchPattern, data);
        errorMessageMatchPattern.declaredVars.putAll(simpleMatchPattern.declaredVars);
    }

    @Override
    public void visit(BLangErrorCauseMatchPattern errorCauseMatchPattern, AnalyzerData data) {
        if (errorCauseMatchPattern.simpleMatchPattern != null) {
            BLangSimpleMatchPattern simpleMatchPattern = errorCauseMatchPattern.simpleMatchPattern;
            if (simpleMatchPattern.varVariableName != null) {
                simpleMatchPattern.varVariableName.setBType(symTable.errorOrNilType);
            }
            analyzeNode(simpleMatchPattern, data);
            errorCauseMatchPattern.declaredVars.putAll(simpleMatchPattern.declaredVars);
            return;
        }
        if (errorCauseMatchPattern.errorMatchPattern != null) {
            analyzeNode(errorCauseMatchPattern.errorMatchPattern, data);
            errorCauseMatchPattern.declaredVars.putAll(errorCauseMatchPattern.errorMatchPattern.declaredVars);
        }
    }

    @Override
    public void visit(BLangErrorFieldMatchPatterns errorFieldMatchPatterns, AnalyzerData data) {
        for (BLangNamedArgMatchPattern namedArgMatchPattern : errorFieldMatchPatterns.namedArgMatchPatterns) {
            analyzeNode(namedArgMatchPattern, data);
            errorFieldMatchPatterns.declaredVars.putAll(namedArgMatchPattern.declaredVars);
        }
        if (errorFieldMatchPatterns.restMatchPattern != null) {
            errorFieldMatchPatterns.restMatchPattern.setBType(new BMapType(TypeTags.MAP, symTable.anydataType, null));
            analyzeNode(errorFieldMatchPatterns.restMatchPattern, data);
            errorFieldMatchPatterns.declaredVars.putAll(errorFieldMatchPatterns.restMatchPattern.declaredVars);
        }
    }

    @Override
    public void visit(BLangNamedArgMatchPattern namedArgMatchPattern, AnalyzerData data) {
        setNamedArgMatchPatternType(namedArgMatchPattern.matchPattern);
        analyzeNode(namedArgMatchPattern.matchPattern, data);
        namedArgMatchPattern.declaredVars.putAll(namedArgMatchPattern.matchPattern.declaredVars);
    }

    private void setNamedArgMatchPatternType(BLangMatchPattern matchPattern) {
        switch(matchPattern.getKind()) {
            case CONST_MATCH_PATTERN:
            case VAR_BINDING_PATTERN_MATCH_PATTERN:
                matchPattern.setBType(symTable.cloneableType);
                break;
            case LIST_MATCH_PATTERN:
                BLangListMatchPattern listMatchPattern = (BLangListMatchPattern) matchPattern;
                listMatchPattern.matchPatterns.forEach(pattern -> pattern.setBType(symTable.cloneableType));
                if (listMatchPattern.restMatchPattern != null) {
                    listMatchPattern.restMatchPattern.setBType(symTable.cloneableType);
                }
                break;
            case MAPPING_MATCH_PATTERN:
                BLangMappingMatchPattern mappingMatchPattern = (BLangMappingMatchPattern) matchPattern;
                mappingMatchPattern.fieldMatchPatterns.forEach(pattern ->
                        pattern.matchPattern.setBType(symTable.cloneableType));
                if (mappingMatchPattern.restMatchPattern != null) {
                    mappingMatchPattern.restMatchPattern.setBType(symTable.cloneableType);
                }
                break;
        }
    }

    @Override
    public void visit(BLangMappingBindingPattern mappingBindingPattern, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        BRecordTypeSymbol recordSymbol = symbolEnter.createAnonRecordSymbol(currentEnv, mappingBindingPattern.pos);
        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();

        for (BLangFieldBindingPattern fieldBindingPattern : mappingBindingPattern.fieldBindingPatterns) {
            fieldBindingPattern.accept(this, data);
            Name fieldName = names.fromIdNode(fieldBindingPattern.fieldName);
            BVarSymbol fieldSymbol = new BVarSymbol(0, fieldName,
                                                    names.originalNameFromIdNode(fieldBindingPattern.fieldName),
                                                    currentEnv.enclPkg.symbol.pkgID,
                                                    fieldBindingPattern.bindingPattern.getBType(), recordSymbol,
                                                    fieldBindingPattern.pos, COMPILED_SOURCE);
            BField field = new BField(fieldName, fieldBindingPattern.pos, fieldSymbol);
            fields.put(fieldName.getValue(), field);
            mappingBindingPattern.declaredVars.putAll(fieldBindingPattern.declaredVars);
        }
        BRecordType recordVarType = new BRecordType(recordSymbol);
        recordVarType.fields = fields;
        recordVarType.restFieldType = symTable.anyOrErrorType;
        if (mappingBindingPattern.restBindingPattern != null) {
            BLangRestBindingPattern restBindingPattern = mappingBindingPattern.restBindingPattern;
            BType restType = restBindingPattern.getBType();
            BRecordTypeSymbol matchPattenRecordSym =
                                                 symbolEnter.createAnonRecordSymbol(currentEnv, restBindingPattern.pos);
            BRecordType matchPatternRecType = new BRecordType(matchPattenRecordSym);
            matchPatternRecType.restFieldType = restType != null ? restType : symTable.anyOrErrorType;
            recordVarType.restFieldType = matchPatternRecType.restFieldType;
            restBindingPattern.setBType(matchPatternRecType);
            restBindingPattern.accept(this, data);
            mappingBindingPattern.declaredVars.put(restBindingPattern.variableName.value, restBindingPattern.symbol);

            BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(matchPatternRecType,
                    currentEnv.enclPkg.packageID, symTable, restBindingPattern.pos);
            recordTypeNode.initFunction =
                    TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, currentEnv, names, symTable);
            TypeDefBuilderHelper.createTypeDefinitionForTSymbol(matchPatternRecType, matchPattenRecordSym,
                    recordTypeNode, currentEnv);
        }
        mappingBindingPattern.setBType(recordVarType);
    }

    @Override
    public void visit(BLangFieldBindingPattern fieldBindingPattern, AnalyzerData data) {
        BLangBindingPattern bindingPattern = fieldBindingPattern.bindingPattern;
        bindingPattern.accept(this, data);
        fieldBindingPattern.declaredVars.putAll(bindingPattern.declaredVars);
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern, AnalyzerData data) {
        BLangExpression constPatternExpr = constMatchPattern.expr;
        typeChecker.checkExpr(constPatternExpr, data.env, data.prevEnvs, data.commonAnalyzerData);
        if (constPatternExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef constRef = (BLangSimpleVarRef) constPatternExpr;
            if (constRef.symbol.kind != SymbolKind.CONSTANT) {
                dlog.error(constMatchPattern.pos, DiagnosticErrorCode.VARIABLE_SHOULD_BE_DECLARED_AS_CONSTANT,
                        constRef.variableName);
                constMatchPattern.setBType(symTable.noType);
                return;
            }
        }
        constMatchPattern.setBType(types.resolvePatternTypeFromMatchExpr(constMatchPattern, constPatternExpr));
    }

    @Override
    public void visit(BLangRestMatchPattern restMatchPattern, AnalyzerData data) {
        Name name = new Name(restMatchPattern.variableName.value);
        Name origName = new Name(restMatchPattern.variableName.originalValue);
        restMatchPattern.symbol = symbolEnter.defineVarSymbol(restMatchPattern.variableName.pos, Flags.unMask(0),
                                                              restMatchPattern.getBType(), name, origName, data.env,
                                                              false);
        restMatchPattern.declaredVars.put(name.value, restMatchPattern.symbol);
    }

    @Override
    public void visit(BLangListMatchPattern listMatchPattern, AnalyzerData data) {
        List<BType> memberTypes = new ArrayList<>();
        for (BLangMatchPattern memberMatchPattern : listMatchPattern.matchPatterns) {
            memberMatchPattern.accept(this, data);
            memberTypes.add(memberMatchPattern.getBType());
            checkForSimilarVars(listMatchPattern.declaredVars, memberMatchPattern.declaredVars, memberMatchPattern.pos);
            listMatchPattern.declaredVars.putAll(memberMatchPattern.declaredVars);
        }
        BTupleType matchPatternType = new BTupleType(memberTypes);

        if (listMatchPattern.getRestMatchPattern() != null) {
            BLangRestMatchPattern restMatchPattern = (BLangRestMatchPattern) listMatchPattern.getRestMatchPattern();
            BType restBindingPatternType = restMatchPattern.getBType();
            BType restType = restBindingPatternType != null ? restBindingPatternType : symTable.anyOrErrorType;
            restMatchPattern.setBType(new BArrayType(restType));
            restMatchPattern.accept(this, data);
            checkForSimilarVars(listMatchPattern.declaredVars, restMatchPattern.declaredVars, restMatchPattern.pos);
            listMatchPattern.declaredVars.put(restMatchPattern.variableName.value, restMatchPattern.symbol);
            matchPatternType.restType = restType;
        }

        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(data.env.enclPkg.symbol);
        listMatchPattern.setBType(types.resolvePatternTypeFromMatchExpr(listMatchPattern, matchPatternType, pkgEnv));
        assignTypesToMemberPatterns(listMatchPattern, listMatchPattern.getBType(), data);
    }

    private void checkForSimilarVars(Map<String, BVarSymbol> declaredVars, Map<String, BVarSymbol> var,
                                     Location pos) {
        for (String variableName : var.keySet()) {
            if (declaredVars.containsKey(variableName)) {
                dlog.error(pos, DiagnosticErrorCode.MATCH_PATTERN_CANNOT_REPEAT_SAME_VARIABLE);
            }
        }
    }

    private void assignTypesToMemberPatterns(BLangBindingPattern bindingPattern, BType patternType, AnalyzerData data) {
        NodeKind patternKind = bindingPattern.getKind();
        BType bindingPatternType = Types.getReferredType(patternType);
        switch (patternKind) {
            case WILDCARD_BINDING_PATTERN:
                return;
            case CAPTURE_BINDING_PATTERN:
                BLangCaptureBindingPattern captureBindingPattern = (BLangCaptureBindingPattern) bindingPattern;
                BVarSymbol captureBindingPatternSymbol =
                        captureBindingPattern.declaredVars.get(
                                captureBindingPattern.getIdentifier().getValue());
                captureBindingPatternSymbol.type = bindingPatternType.tag == TypeTags.ERROR ? bindingPatternType :
                        this.types.mergeTypes(captureBindingPatternSymbol.type, bindingPatternType);
                captureBindingPattern.setBType(captureBindingPatternSymbol.type);
                return;
            case LIST_BINDING_PATTERN:
                BLangListBindingPattern listBindingPattern = (BLangListBindingPattern) bindingPattern;
                if (bindingPatternType.tag == TypeTags.UNION) {
                    for (BType type : ((BUnionType) bindingPatternType).getMemberTypes()) {
                        assignTypesToMemberPatterns(bindingPattern, type, data);
                    }
                    listBindingPattern.setBType(bindingPatternType);
                    return;
                }
                if (bindingPatternType.tag == TypeTags.ARRAY) {
                    BArrayType arrayType = (BArrayType) bindingPatternType;
                    for (BLangBindingPattern memberBindingPattern : listBindingPattern.bindingPatterns) {
                        assignTypesToMemberPatterns(memberBindingPattern, arrayType.eType, data);
                    }
                    if (listBindingPattern.restBindingPattern == null) {
                        return;
                    }
                    if (arrayType.state == BArrayState.CLOSED) {
                        BTupleType restTupleType = createTupleForClosedArray(
                                arrayType.size - listBindingPattern.bindingPatterns.size(), arrayType.eType);
                        listBindingPattern.restBindingPattern.setBType(restTupleType);
                        BVarSymbol restBindingPatternSymbol = listBindingPattern.restBindingPattern.declaredVars
                                .get(listBindingPattern.restBindingPattern.getIdentifier().getValue());
                        restBindingPatternSymbol.type = restTupleType;
                        return;
                    }
                    BLangRestBindingPattern restBindingPattern = listBindingPattern.restBindingPattern;
                    BType restType = ((BArrayType) restBindingPattern.getBType()).eType;
                    restType = types.mergeTypes(restType, arrayType.eType);
                    ((BArrayType) restBindingPattern.getBType()).eType = restType;
                    return;
                }
                if (bindingPatternType.tag != TypeTags.TUPLE) {
                    return;
                }
                BTupleType bindingPatternTupleType = (BTupleType) bindingPatternType;
                List<BType> types = bindingPatternTupleType.getTupleTypes();
                List<BLangBindingPattern> bindingPatterns = listBindingPattern.bindingPatterns;
                List<BType> memberTypes = new ArrayList<>();
                for (int i = 0; i < bindingPatterns.size(); i++) {
                    assignTypesToMemberPatterns(bindingPatterns.get(i), types.get(i), data);
                    memberTypes.add(bindingPatterns.get(i).getBType());
                }
                BTupleType tupleType = new BTupleType(memberTypes);

                if (listBindingPattern.restBindingPattern == null) {
                    bindingPattern.setBType(tupleType);
                    return;
                }
                tupleType.restType = createTypeForTupleRestType(bindingPatterns.size(), types,
                        bindingPatternTupleType.restType);
                listBindingPattern.restBindingPattern.setBType(tupleType.restType);
                bindingPattern.setBType(bindingPatternType);
                BVarSymbol restBindingPatternSymbol =
                        listBindingPattern.restBindingPattern.declaredVars
                                .get(listBindingPattern.restBindingPattern.getIdentifier().getValue());
                restBindingPatternSymbol.type = tupleType.restType;
                return;
            case MAPPING_BINDING_PATTERN:
                BLangMappingBindingPattern mappingBindingPattern = (BLangMappingBindingPattern) bindingPattern;
                if (bindingPatternType.tag == TypeTags.UNION) {
                    for (BType type : ((BUnionType) bindingPatternType).getMemberTypes()) {
                        assignTypesToMemberPatterns(mappingBindingPattern, type, data);
                    }
                    return;
                }
                if (bindingPatternType.tag != TypeTags.RECORD) {
                    return;
                }
                BRecordType recordType = (BRecordType) bindingPatternType;
                List<String> boundedFields = new ArrayList<>();
                for (BLangFieldBindingPattern fieldBindingPattern : mappingBindingPattern.fieldBindingPatterns) {
                    assignTypesToMemberPatterns(fieldBindingPattern.bindingPattern,
                            recordType.fields.get(fieldBindingPattern.fieldName.value).type, data);
                    boundedFields.add(fieldBindingPattern.fieldName.value);
                }

                if (mappingBindingPattern.restBindingPattern == null) {
                    return;
                }
                BLangRestBindingPattern restBindingPattern = mappingBindingPattern.restBindingPattern;
                BRecordType restPatternRecordType = (BRecordType) restBindingPattern.getBType();
                BVarSymbol restVarSymbol =
                        restBindingPattern.declaredVars.get(restBindingPattern.getIdentifier().getValue());
                BRecordType restVarSymbolRecordType = (BRecordType) restVarSymbol.type;
                setRestMatchPatternConstraintType(recordType, boundedFields, restPatternRecordType,
                        restVarSymbolRecordType, data);
                return;
            default:
        }
    }

    @Override
    public void visit(BLangWildCardMatchPattern wildCardMatchPattern, AnalyzerData data) {
        if (wildCardMatchPattern.matchExpr == null) {
            wildCardMatchPattern.setBType(symTable.anyType);
            return;
        }
        BType matchExprType = wildCardMatchPattern.matchExpr.getBType();
        if (types.isAssignable(matchExprType, symTable.anyType)) {
            wildCardMatchPattern.setBType(symTable.anyType);
            return;
        }

        BType intersectionType = types.getTypeIntersection(
                Types.IntersectionContext.compilerInternalIntersectionContext(),
                matchExprType, symTable.anyType, data.env);
        if (intersectionType == symTable.semanticError) {
            wildCardMatchPattern.setBType(symTable.noType);
            return;
        }
        wildCardMatchPattern.setBType(intersectionType);
    }

    @Override
    public void visit(BLangForeach foreach, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        // Check the collection's type.
        typeChecker.checkExpr(foreach.collection, currentEnv, symTable.noType, data.prevEnvs,
                data.commonAnalyzerData);
        // object type collection should be a subtype of 'object:Iterable
        if (Types.getReferredType(foreach.collection.getBType()).tag == TypeTags.OBJECT
                && !types.isAssignable(foreach.collection.getBType(), symTable.iterableType)) {
            dlog.error(foreach.collection.pos, DiagnosticErrorCode.INVALID_ITERABLE_OBJECT_TYPE,
                       foreach.collection.getBType(), symTable.iterableType);
            foreach.resultType = symTable.semanticError;
            return;
        }
        // Set the type of the foreach node's type node.
        types.setForeachTypedBindingPatternType(foreach);
        // Create a new block environment for the foreach node's body.
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(foreach.body, currentEnv);
        // Check foreach node's variables and set types.
        handleForeachDefinitionVariables(foreach.variableDefinitionNode, foreach.varType, foreach.isDeclaredWithVar,
                false, blockEnv);
        boolean prevBreakFound = data.breakFound;
        // Analyze foreach node's statements.
        data.env = blockEnv;
        analyzeStmt(foreach.body, data);

        if (foreach.onFailClause != null) {
            this.analyzeNode(foreach.onFailClause, data);
        }
        data.notCompletedNormally = false;
        data.breakFound = prevBreakFound;
    }

    @Override
    public void visit(BLangOnFailClause onFailClause, AnalyzerData data) {
        // Create a new block environment for the on-fail node.
        SymbolEnv onFailEnv = SymbolEnv.createBlockEnv(onFailClause.body, data.env);
        VariableDefinitionNode onFailVarDefNode = onFailClause.variableDefinitionNode;
        if (onFailVarDefNode != null) {
            // Check on-fail node's variables and set types.
            handleForeachDefinitionVariables(onFailVarDefNode, symTable.errorType,
                    onFailClause.isDeclaredWithVar, true, onFailEnv);
            BLangVariable onFailVarNode = (BLangVariable) onFailVarDefNode.getVariable();
            if (!types.isAssignable(onFailVarNode.getBType(), symTable.errorType)) {
                dlog.error(onFailVarNode.pos, DiagnosticErrorCode.INVALID_TYPE_DEFINITION_FOR_ERROR_VAR,
                        onFailVarNode.getBType());
            }
        }
        data.env = onFailEnv;
        analyzeStmt(onFailClause.body, data);
    }

    @Override
    public void visit(BLangWhile whileNode, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        typeChecker.checkExpr(whileNode.expr, currentEnv, symTable.booleanType, data.prevEnvs,
                data.commonAnalyzerData);

        if (whileNode.onFailClause != null) {
            this.analyzeNode(whileNode.onFailClause, data);
        }

        BType actualType = whileNode.expr.getBType();
        if (TypeTags.TUPLE == Types.getReferredType(actualType).tag) {
            dlog.error(whileNode.expr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, symTable.booleanType, actualType);
        }

        boolean prevBreakFound = data.breakFound;
        SymbolEnv whileEnv = typeNarrower.evaluateTruth(whileNode.expr, whileNode.body, currentEnv);
        data.env = whileEnv;
        analyzeStmt(whileNode.body, data);
        data.notCompletedNormally =
                ConditionResolver.checkConstCondition(types, symTable, whileNode.expr) == symTable.trueType
                        && !data.breakFound;
        data.breakFound = prevBreakFound;
    }

    @Override
    public void visit(BLangDo doNode, AnalyzerData data) {
        data.env = SymbolEnv.createTypeNarrowedEnv(doNode, data.env);
        if (doNode.onFailClause != null) {
            this.analyzeNode(doNode.onFailClause, data);
        }
        analyzeStmt(doNode.body, data);
    }

    @Override
    public void visit(BLangFail failNode, AnalyzerData data) {
        BLangExpression errorExpression = failNode.expr;
        BType errorExpressionType = typeChecker.checkExpr(errorExpression, data.env, data.prevEnvs,
                data.commonAnalyzerData);

        if (errorExpressionType == symTable.semanticError ||
                !types.isSubTypeOfBaseType(errorExpressionType, symTable.errorType.tag)) {
            dlog.error(errorExpression.pos, DiagnosticErrorCode.ERROR_TYPE_EXPECTED, errorExpression.toString());
        }
        data.notCompletedNormally = true;
    }

    @Override
    public void visit(BLangLock lockNode, AnalyzerData data) {
        data.env = SymbolEnv.createLockEnv(lockNode, data.env);
        analyzeStmt(lockNode.body, data);
        if (lockNode.onFailClause != null) {
            this.analyzeNode(lockNode.onFailClause, data);
        }
    }

    @Override
    public void visit(BLangService serviceNode, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        inferServiceTypeFromListeners(serviceNode, data);
        addCheckExprToServiceVariable(serviceNode);
        analyzeNode(serviceNode.serviceVariable, data);
        if (serviceNode.serviceNameLiteral != null) {
            typeChecker.checkExpr(serviceNode.serviceNameLiteral, currentEnv, symTable.stringType, data.prevEnvs,
                    data.commonAnalyzerData);
        }

        serviceNode.setBType(serviceNode.serviceClass.getBType());
        BType serviceType = serviceNode.serviceClass.getBType();
        BServiceSymbol serviceSymbol = (BServiceSymbol) serviceNode.symbol;
        validateServiceTypeImplementation(serviceNode.pos, serviceType, serviceNode.inferredServiceType);

        for (BLangExpression attachExpr : serviceNode.attachedExprs) {
            final BType exprType = typeChecker.checkExpr(attachExpr, currentEnv, symTable.noType, data.prevEnvs,
                    data.commonAnalyzerData);
            if (exprType != symTable.semanticError && !types.checkListenerCompatibilityAtServiceDecl(exprType)) {
                dlog.error(attachExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, LISTENER_NAME, exprType);
            } else if (exprType != symTable.semanticError && serviceNode.listenerType == null) {
                serviceNode.listenerType = exprType;
            } else if (exprType != symTable.semanticError) {
                this.types.isSameType(exprType, serviceNode.listenerType);
            }

            if (attachExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                final BLangSimpleVarRef attachVarRef = (BLangSimpleVarRef) attachExpr;
                if (attachVarRef.symbol != null && attachVarRef.symbol != symTable.notFoundSymbol &&
                        !Symbols.isFlagOn(attachVarRef.symbol.flags, Flags.LISTENER)) {
                    dlog.error(attachVarRef.pos, DiagnosticErrorCode.INVALID_LISTENER_ATTACHMENT);
                }
            } else if (attachExpr.getKind() != NodeKind.TYPE_INIT_EXPR) {
                dlog.error(attachExpr.pos, DiagnosticErrorCode.INVALID_LISTENER_ATTACHMENT);
            }

            // Validate listener attachment based on attach-point of the service decl and second param of listener.
            if (exprType.getKind() == TypeKind.OBJECT) {
                BObjectType listenerType = (BObjectType) exprType;
                validateServiceAttachmentOnListener(serviceNode, attachExpr, listenerType, serviceType);
            } else if (exprType.getKind() == TypeKind.UNION) {
                for (BType memberType : ((BUnionType) exprType).getMemberTypes()) {
                    if (Types.getReferredType(memberType).tag == TypeTags.ERROR) {
                        continue;
                    }
                    BType refType = Types.getReferredType(memberType);
                    if (refType.tag == TypeTags.OBJECT) {
                        validateServiceAttachmentOnListener(serviceNode, attachExpr,
                                (BObjectType) refType, serviceType);
                    }
                }
            }

            serviceSymbol.addListenerType(exprType);
        }
    }

    private void validateServiceTypeImplementation(Location pos, BType implementedType, BType inferredServiceType) {
        if (!types.isAssignableIgnoreObjectTypeIds(implementedType, inferredServiceType)) {
            if (inferredServiceType.tag == TypeTags.UNION
                    && ((BUnionType) inferredServiceType).getMemberTypes().isEmpty()) {
                return;
            }
            dlog.error(pos, DiagnosticErrorCode.SERVICE_DOES_NOT_IMPLEMENT_REQUIRED_CONSTRUCTS, inferredServiceType);
        }
    }

    private void inferServiceTypeFromListeners(BLangService serviceNode, AnalyzerData data) {
        List<BLangType> typeRefs = serviceNode.serviceClass.typeRefs;
        if (!typeRefs.isEmpty()) {
            serviceNode.inferredServiceType = typeRefs.get(0).getBType();
            return;
        }

        LinkedHashSet<BType> listenerTypes = new LinkedHashSet<>();
        for (BLangExpression attachExpr : serviceNode.attachedExprs) {
            BType type = typeChecker.checkExpr(attachExpr, data.env, symTable.noType, data.prevEnvs,
                    data.commonAnalyzerData);
            flatMapAndGetObjectTypes(listenerTypes, type);
        }
        BType inferred;
        BTypeIdSet typeIdSet = BTypeIdSet.emptySet();
        if (listenerTypes.size() == 1) {
            inferred = listenerTypes.iterator().next();
            typeIdSet.add(getTypeIds(inferred));
        } else {
            for (BType attachType : listenerTypes) {
                typeIdSet.add(getTypeIds(attachType));
            }
            inferred = BUnionType.create(null, listenerTypes);
        }

        serviceNode.inferredServiceType = inferred;
        BType tServiceClass = serviceNode.serviceClass.getBType();
        getTypeIds(tServiceClass).add(typeIdSet);
    }

    private BTypeIdSet getTypeIds(BType type) {
        int tag = type.tag;
        if (tag == TypeTags.SERVICE || tag == TypeTags.OBJECT) {
            return ((BObjectType) type).typeIdSet;
        } else if (tag == TypeTags.TYPEREFDESC) {
            return getTypeIds(((BTypeReferenceType) type).referredType);
        }
        return BTypeIdSet.emptySet();
    }

    private void flatMapAndGetObjectTypes(Set<BType> result, BType type) {
        type = Types.getReferredType(type);
        if (!types.checkListenerCompatibilityAtServiceDecl(type)) {
            return;
        }
        if (type.tag == TypeTags.OBJECT) {
            BObjectType objectType = (BObjectType) type;
            BObjectTypeSymbol tsymbol = (BObjectTypeSymbol) objectType.tsymbol;
            for (BAttachedFunction func : tsymbol.attachedFuncs) {
                if (func.funcName.value.equals("attach")) {
                    BType firstParam = func.type.paramTypes.get(0);
                    result.add(firstParam);
                    return;
                }
            }
        } else if (type.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                flatMapAndGetObjectTypes(result, memberType);
            }
        } else if (type.tag == TypeTags.INTERSECTION) {
            BType effectiveType = ((BIntersectionType) type).effectiveType;
            flatMapAndGetObjectTypes(result, effectiveType);
        }
    }

    private void addCheckExprToServiceVariable(BLangService serviceNode) {
        BLangFunction initFunction = serviceNode.serviceClass.initFunction;
        if (initFunction != null && initFunction.returnTypeNode != null
                && types.containsErrorType(initFunction.returnTypeNode.getBType())) {
            BLangCheckedExpr checkedExpr = (BLangCheckedExpr) TreeBuilder.createCheckExpressionNode();
            checkedExpr.expr = serviceNode.serviceVariable.expr;
            checkedExpr.setBType(serviceNode.serviceClass.getBType());
            serviceNode.serviceVariable.expr = checkedExpr;
        }
    }

    private void validateServiceAttachmentOnListener(BLangService serviceNode, BLangExpression attachExpr,
                                                     BObjectType listenerType, BType serviceType) {
        for (var func : ((BObjectTypeSymbol) listenerType.tsymbol).attachedFuncs) {
            if (func.funcName.value.equals("attach")) {
                List<BType> paramTypes = func.type.paramTypes;
                if (serviceType != null && serviceType != symTable.noType) {
                    validateServiceTypeAgainstAttachMethod(serviceNode.inferredServiceType, paramTypes.get(0),
                            attachExpr.pos);
                }
                validateServiceAttachpointAgainstAttachMethod(serviceNode, attachExpr, paramTypes.get(1));
            }
        }
    }

    private void validateServiceTypeAgainstAttachMethod(BType serviceType, BType targetType, Location pos) {
        if (!types.isAssignable(serviceType, targetType)) {
            dlog.error(pos, DiagnosticErrorCode.SERVICE_TYPE_IS_NOT_SUPPORTED_BY_LISTENER);
        }
    }

    private void validateServiceAttachpointAgainstAttachMethod(BLangService serviceNode, BLangExpression listenerExpr,
                                                               BType attachPointParam) {
        boolean isStringComponentAvailable = types.isAssignable(symTable.stringType, attachPointParam);
        boolean isNullable = attachPointParam.isNullable();
        boolean isArrayComponentAvailable = types.isAssignable(symTable.arrayStringType, attachPointParam);

        boolean pathLiteral = serviceNode.serviceNameLiteral != null;
        boolean absolutePath = !serviceNode.absoluteResourcePath.isEmpty();

        Location pos = listenerExpr.getPosition();

        if (!pathLiteral && isStringComponentAvailable && !isArrayComponentAvailable && !isNullable) {
            dlog.error(pos, DiagnosticErrorCode.SERVICE_LITERAL_REQUIRED_BY_LISTENER);
        } else if (!absolutePath && isArrayComponentAvailable && !isStringComponentAvailable && !isNullable) {
            dlog.error(pos, DiagnosticErrorCode.SERVICE_ABSOLUTE_PATH_REQUIRED_BY_LISTENER);
        } else if (!pathLiteral && !absolutePath && !isNullable) {
            dlog.error(pos, DiagnosticErrorCode.SERVICE_ABSOLUTE_PATH_OR_LITERAL_IS_REQUIRED_BY_LISTENER);
        }

        // Path literal is provided, listener does not accept path literal
        if (pathLiteral && !isStringComponentAvailable) {
            dlog.error(pos, DiagnosticErrorCode.SERVICE_PATH_LITERAL_IS_NOT_SUPPORTED_BY_LISTENER);
        }

        // Absolute path is provided, Listener does not accept abs path
        if (absolutePath && !isArrayComponentAvailable) {
            dlog.error(pos, DiagnosticErrorCode.SERVICE_ABSOLUTE_PATH_IS_NOT_SUPPORTED_BY_LISTENER);
        }
    }

    private void validateDefaultable(BLangRecordTypeNode recordTypeNode) {
        for (BLangSimpleVariable field : recordTypeNode.fields) {
            if (field.flagSet.contains(Flag.OPTIONAL) && field.expr != null) {
                dlog.error(field.pos, DiagnosticErrorCode.DEFAULT_VALUES_NOT_ALLOWED_FOR_OPTIONAL_FIELDS,
                           field.name.value);
            }
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode, AnalyzerData data) {
        data.env = SymbolEnv.createTransactionEnv(transactionNode, data.env);

        if (transactionNode.onFailClause != null) {
            this.analyzeNode(transactionNode.onFailClause, data);
        }
        analyzeStmt(transactionNode.transactionBody, data);
    }

    @Override
    public void visit(BLangRollback rollbackNode, AnalyzerData data) {
        if (rollbackNode.expr != null) {
            BType expectedType = BUnionType.create(null, symTable.errorType, symTable.nilType);
            this.typeChecker.checkExpr(rollbackNode.expr, data.env, expectedType, data.prevEnvs,
                    data.commonAnalyzerData);
        }
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction, AnalyzerData data) {
        if (retryTransaction.retrySpec != null) {
            retryTransaction.retrySpec.accept(this, data);
        }

        retryTransaction.transaction.accept(this, data);
    }

    @Override
    public void visit(BLangRetry retryNode, AnalyzerData data) {
        if (retryNode.retrySpec != null) {
            retryNode.retrySpec.accept(this, data);
        }
        data.env = SymbolEnv.createRetryEnv(retryNode, data.env);
        analyzeStmt(retryNode.retryBody, data);

        if (retryNode.onFailClause != null) {
            this.analyzeNode(retryNode.onFailClause, data);
        }
    }

    @Override
    public void visit(BLangRetrySpec retrySpec, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        if (retrySpec.retryManagerType != null) {
            retrySpec.setBType(symResolver.resolveTypeNode(retrySpec.retryManagerType, currentEnv));
        }

        if (retrySpec.argExprs != null) {
            retrySpec.argExprs.forEach(arg -> this.typeChecker.checkExpr(arg, currentEnv, symTable.noType,
                                                                         data.prevEnvs, data.commonAnalyzerData));
        }
    }

    @Override
    public void visit(BLangForkJoin forkJoin, AnalyzerData data) {
        for (BLangSimpleVariableDef worker : forkJoin.workers) {
            BLangFunction function = ((BLangLambdaFunction) worker.var.expr).function;
            function.symbol.enclForkName = function.anonForkName;
            ((BInvokableSymbol) worker.var.symbol).enclForkName = function.anonForkName;
        }
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        // TODO Need to remove this cached env
        workerSendNode.env = currentEnv;
        this.typeChecker.checkExpr(workerSendNode.expr, currentEnv, data.prevEnvs, data.commonAnalyzerData);

        BSymbol symbol =
                symResolver.lookupSymbolInMainSpace(currentEnv, names.fromIdNode(workerSendNode.workerIdentifier));

        if (symTable.notFoundSymbol.equals(symbol)) {
            workerSendNode.setBType(symTable.semanticError);
        } else {
            workerSendNode.setBType(symbol.type);
            workerSendNode.workerSymbol = symbol;
        }
    }

    @Override
    public void visit(BLangReturn returnNode, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        this.typeChecker.checkExpr(returnNode.expr, currentEnv, currentEnv.enclInvokable.returnTypeNode.getBType(),
                                   data.prevEnvs, data.commonAnalyzerData);
        validateWorkerAnnAttachments(returnNode.expr, data);
        data.notCompletedNormally = true;
    }

    void analyzeStmt(BLangStatement stmtNode, AnalyzerData data) {
        analyzeNode(stmtNode, data);
    }

    public void analyzeNode(BLangNode node, SymbolEnv env) {
        AnalyzerData data = new AnalyzerData(env);
        analyzeNode(node, data);
    }

    public void analyzeNode(BLangNode node, SymbolEnv env, Types.CommonAnalyzerData commonAnalyzerData) {
        AnalyzerData data = new AnalyzerData(env);
        data.commonAnalyzerData = commonAnalyzerData;
        analyzeNode(node, data);
    }

    public void analyzeNode(BLangNode node, SymbolEnv env, Stack<SymbolEnv> prevEnvs) {
        AnalyzerData data = new AnalyzerData(env);
        data.prevEnvs = prevEnvs;
        analyzeNode(node, data);
    }

    public void analyzeNode(BLangNode node, SymbolEnv env, Stack<SymbolEnv> prevEnvs,
                            Types.CommonAnalyzerData commonAnalyzerData) {
        AnalyzerData data = new AnalyzerData(env);
        data.prevEnvs = prevEnvs;
        data.commonAnalyzerData = commonAnalyzerData;
        analyzeNode(node, data);
    }
    public void analyzeNode(BLangNode node,  AnalyzerData data) {
        analyzeNode(node, symTable.noType, data);
    }

    @Override
    public void visit(BLangContinue continueNode, AnalyzerData data) {
        data.notCompletedNormally = true;
    }

    @Override
    public void visit(BLangBreak breakNode, AnalyzerData data) {
        data.notCompletedNormally = true;
        data.breakFound = true;
    }

    @Override
    public void visit(BLangPanic panicNode, AnalyzerData data) {
        this.typeChecker.checkExpr(panicNode.expr, data.env, symTable.errorType, data.prevEnvs,
                data.commonAnalyzerData);
        data.notCompletedNormally = true;
    }

    void analyzeNode(BLangNode node, BType expType, AnalyzerData data) {
        data.prevEnvs.push(data.env);
        BType preExpType = data.expType;
        data.expType = expType;
        node.accept(this, data);
        data.env = data.prevEnvs.pop();
        data.expType = preExpType;
    }

    @Override
    public void visit(BLangConstant constant, AnalyzerData data) {
        if (constant.typeNode != null && !types.isAllowedConstantType(constant.typeNode.getBType())) {
            if (types.isAssignable(constant.typeNode.getBType(), symTable.anydataType) &&
                    !types.isNeverTypeOrStructureTypeWithARequiredNeverMember(constant.typeNode.getBType())) {
                dlog.error(constant.typeNode.pos, DiagnosticErrorCode.CONSTANT_DECLARATION_NOT_YET_SUPPORTED,
                        constant.typeNode);
            } else {
                dlog.error(constant.typeNode.pos, DiagnosticErrorCode.INVALID_CONST_DECLARATION,
                        constant.typeNode);
            }
        }

        this.anonTypeNameSuffixes.push(constant.name.value);
        constant.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoints.add(AttachPoint.Point.CONST);
            this.anonTypeNameSuffixes.push(annotationAttachment.annotationName.value);
            annotationAttachment.accept(this, data);
            this.anonTypeNameSuffixes.pop();

            BAnnotationAttachmentSymbol annotationAttachmentSymbol = annotationAttachment.annotationAttachmentSymbol;
            if (annotationAttachmentSymbol != null) {
                constant.symbol.addAnnotation(annotationAttachmentSymbol);
            }
        });
        this.anonTypeNameSuffixes.pop();

        BLangExpression expression = constant.expr;
        if (isNodeKindAllowedForConstants(expression, constant)) {
            // This has to return, because constant.symbol.type is required for further validations.
            // ATM this is only special cased for unary expressions with `+` and `-` operators with numeric expressions.
            dlog.error(expression.pos, DiagnosticErrorCode.TYPE_REQUIRED_FOR_CONST_WITH_EXPRESSIONS);
            return;
        }

        typeChecker.checkExpr(expression, data.env, constant.symbol.type, data.prevEnvs, data.commonAnalyzerData);

        // Check nested expressions.
        constantAnalyzer.visit(constant);
    }

    private boolean isLiteralInUnaryFromConstantNotAllowed(BLangUnaryExpr unaryExpr) {
        return unaryExpr.expr.getKind() != NodeKind.NUMERIC_LITERAL &&
                !types.isOperatorKindInUnaryValid(unaryExpr.operator);
    }

    private boolean isNodeKindAllowedForConstants(BLangExpression expression, BLangConstant constant) {
        NodeKind exprNodeKind = expression.getKind();
        if (exprNodeKind == NodeKind.UNARY_EXPR &&
                isLiteralInUnaryFromConstantNotAllowed((BLangUnaryExpr) expression)) {
            return constant.typeNode == null;
        }
        if (!(exprNodeKind == LITERAL || exprNodeKind == NUMERIC_LITERAL) && exprNodeKind != NodeKind.UNARY_EXPR) {
            return constant.typeNode == null;
        }
        return false;
    }

    // TODO: 7/10/19 Remove this once const support is added for lists. A separate method is introduced temporarily
    //  since we allow array/tuple literals with cont exprs for annotations
    private void checkAnnotConstantExpression(BLangExpression expression) {
        // Recursively check whether all the nested expressions in the provided expression are constants or can be
        // evaluated to constants.
        switch (expression.getKind()) {
            case LITERAL:
            case NUMERIC_LITERAL:
                break;
            case SIMPLE_VARIABLE_REF:
                BSymbol symbol = ((BLangSimpleVarRef) expression).symbol;
                // Symbol can be null in some invalid scenarios. Eg - const string m = { name: "Ballerina" };
                if (symbol != null && (symbol.tag & SymTag.CONSTANT) != SymTag.CONSTANT) {
                    dlog.error(expression.pos, DiagnosticErrorCode.EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION);
                }
                break;
            case RECORD_LITERAL_EXPR:
                ((BLangRecordLiteral) expression).fields.forEach(field -> {
                    if (field.isKeyValueField()) {
                        BLangRecordLiteral.BLangRecordKeyValueField pair =
                                (BLangRecordLiteral.BLangRecordKeyValueField) field;
                        checkAnnotConstantExpression(pair.key.expr);
                        checkAnnotConstantExpression(pair.valueExpr);
                    } else {
                        checkAnnotConstantExpression((BLangRecordLiteral.BLangRecordVarNameField) field);
                    }
                });
                break;
            case LIST_CONSTRUCTOR_EXPR:
                ((BLangListConstructorExpr) expression).exprs.forEach(this::checkAnnotConstantExpression);
                break;
            default:
                dlog.error(expression.pos, DiagnosticErrorCode.EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION);
                break;
        }
    }

    private void handleForeachDefinitionVariables(VariableDefinitionNode variableDefinitionNode, BType varType,
                                                  boolean isDeclaredWithVar, boolean isOnFailDef, SymbolEnv blockEnv) {
        BLangVariable variableNode = (BLangVariable) variableDefinitionNode.getVariable();
        // Check whether the foreach node's variables are declared with var.
        if (isDeclaredWithVar) {
            // If the foreach node's variables are declared with var, type is `varType`.
            handleDeclaredVarInForeach(variableNode, varType, blockEnv);
            return;
        }
        // If the type node is available, we get the type from it.
        BType typeNodeType = symResolver.resolveTypeNode(variableNode.typeNode, blockEnv);
        if (isOnFailDef) {
            BType sourceType = varType;
            varType = typeNodeType;
            typeNodeType = sourceType;
        }
        // Then we need to check whether the RHS type is assignable to LHS type.
        if (types.isAssignable(varType, typeNodeType)) {
            // If assignable, we set types to the variables.
            handleDeclaredVarInForeach(variableNode, varType, blockEnv);
            return;
        }
        // Log an error and define a symbol with the node's type to avoid undeclared symbol errors.
        if (variableNode.typeNode != null && variableNode.typeNode.pos != null) {
            dlog.error(variableNode.typeNode.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, varType, typeNodeType);
        }
        handleDeclaredVarInForeach(variableNode, typeNodeType, blockEnv);
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
            binaryExpressionNode.setBType(opSymbol.type.getReturnType());
            binaryExpressionNode.opSymbol = (BOperatorSymbol) opSymbol;
        } else {
            binaryExpressionNode.setBType(symTable.semanticError);
        }
        return binaryExpressionNode;
    }

    private boolean validateObjectTypeInitInvocation(BLangExpression expr) {
        // Following is invalid:
        // var a = new ;
        if (expr != null && expr.getKind() == NodeKind.TYPE_INIT_EXPR &&
                ((BLangTypeInit) expr).userDefinedType == null) {
            dlog.error(expr.pos, DiagnosticErrorCode.INVALID_ANY_VAR_DEF);
            return false;
        }
        return true;
    }

    private void setTypeOfVarRefInErrorBindingAssignment(BLangExpression expr, AnalyzerData data) {
        // In binding assignments, lhs supports only simple, record, error, tuple varRefs.
        if (expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF
                && expr.getKind() != NodeKind.RECORD_VARIABLE_REF
                && expr.getKind() != NodeKind.ERROR_VARIABLE_REF
                && expr.getKind() != NodeKind.TUPLE_VARIABLE_REF) {
            dlog.error(expr.pos, DiagnosticErrorCode.INVALID_VARIABLE_REFERENCE_IN_BINDING_PATTERN, expr);
            expr.setBType(symTable.semanticError);
        }
        setTypeOfVarRef(expr, data);
    }

    private void setTypeOfVarRefInAssignment(BLangExpression expr, AnalyzerData data) {
        // In assignments, lhs supports only simple, record, error, tuple
        // varRefs and field, xml and index based access expressions.
        if (!(expr instanceof BLangValueExpression)) {
            dlog.error(expr.pos, DiagnosticErrorCode.INVALID_VARIABLE_ASSIGNMENT, expr);
            expr.setBType(symTable.semanticError);
        }
        setTypeOfVarRef(expr, data);
    }

    private void setTypeOfVarRef(BLangExpression expr, AnalyzerData data) {
        BLangValueExpression varRefExpr = (BLangValueExpression) expr;
        varRefExpr.isLValue = true;
        typeChecker.checkExpr(varRefExpr, data.env, symTable.noType, data.prevEnvs, data.commonAnalyzerData);

        // Check whether this is an readonly field.
        checkConstantAssignment(varRefExpr, data);

        // If this is an update of a type narrowed variable, the assignment should allow assigning
        // values of its original type. Therefore treat all lhs simpleVarRefs in their original type.
        if (isSimpleVarRef(expr)) {
            BVarSymbol originSymbol = ((BVarSymbol) ((BLangSimpleVarRef) expr).symbol).originalSymbol;
            if (originSymbol != null) {
                varRefExpr.setBType(originSymbol.type);
            }
        }
    }

    private void setTypeOfVarRefForBindingPattern(BLangExpression expr, AnalyzerData data) {
        BLangVariableReference varRefExpr = (BLangVariableReference) expr;
        varRefExpr.isLValue = true;

        typeChecker.checkExpr(varRefExpr, data.env, data.prevEnvs, data.commonAnalyzerData);

        switch (expr.getKind()) {
            case SIMPLE_VARIABLE_REF:
                setTypeOfVarRef(expr, data);
                break;
            case TUPLE_VARIABLE_REF:
                BLangTupleVarRef tupleVarRef = (BLangTupleVarRef) expr;
                for (BLangExpression expression : tupleVarRef.expressions) {
                    setTypeOfVarRefForBindingPattern(expression, data);
                }
                if (tupleVarRef.restParam != null) {
                    setTypeOfVarRefForBindingPattern(tupleVarRef.restParam, data);
                }
                return;
            case RECORD_VARIABLE_REF:
                BLangRecordVarRef recordVarRef = (BLangRecordVarRef) expr;
                recordVarRef.recordRefFields
                        .forEach(refKeyValue -> setTypeOfVarRefForBindingPattern(refKeyValue.variableReference, data));
                if (recordVarRef.restParam != null) {
                    setTypeOfVarRefForBindingPattern((BLangExpression) recordVarRef.restParam, data);
                }
                return;
            case ERROR_VARIABLE_REF:
                BLangErrorVarRef errorVarRef = (BLangErrorVarRef) expr;
                if (errorVarRef.message != null) {
                    setTypeOfVarRefForBindingPattern(errorVarRef.message, data);
                }
                if (errorVarRef.cause != null) {
                    setTypeOfVarRefForBindingPattern(errorVarRef.cause, data);
                    if (!types.isAssignable(symTable.errorOrNilType, errorVarRef.cause.getBType())) {
                        dlog.error(errorVarRef.cause.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                                   symTable.errorOrNilType,
                                   errorVarRef.cause.getBType());
                    }
                }
                errorVarRef.detail.forEach(namedArgExpr -> setTypeOfVarRefForBindingPattern(namedArgExpr.expr, data));
                if (errorVarRef.restVar != null) {
                    setTypeOfVarRefForBindingPattern(errorVarRef.restVar, data);
                }
        }
    }

    private void checkInvalidTypeDef(BLangExpression expr) {
        switch (expr.getKind()) {
            case SIMPLE_VARIABLE_REF:
                BLangSimpleVarRef variableRef = (BLangSimpleVarRef) expr;
                if (variableRef.isLValue && variableRef.symbol != null &&
                        (variableRef.symbol.tag & SymTag.TYPE_DEF) == SymTag.TYPE_DEF) {
                    dlog.error(expr.pos, DiagnosticErrorCode.CANNOT_ASSIGN_VALUE_TO_TYPE_DEF);
                }
                return;
            case TUPLE_VARIABLE_REF:
                BLangTupleVarRef tupleVarRef = (BLangTupleVarRef) expr;
                for (BLangExpression tupleExpr : tupleVarRef.expressions) {
                    checkInvalidTypeDef(tupleExpr);
                }
                if (tupleVarRef.restParam != null) {
                    checkInvalidTypeDef((BLangExpression) tupleVarRef.restParam);
                }
                return;
            case RECORD_VARIABLE_REF:
                BLangRecordVarRef recordVarRef = (BLangRecordVarRef) expr;
                for (BLangRecordVarRefKeyValue refKeyValue : recordVarRef.recordRefFields) {
                    checkInvalidTypeDef(refKeyValue.variableReference);
                }
                if (recordVarRef.restParam != null) {
                    checkInvalidTypeDef((BLangExpression) recordVarRef.restParam);
                }
                return;
            case ERROR_VARIABLE_REF:
                BLangErrorVarRef errorVarRef = (BLangErrorVarRef) expr;
                if (errorVarRef.message != null) {
                    checkInvalidTypeDef(errorVarRef.message);
                }
                if (errorVarRef.cause != null) {
                    checkInvalidTypeDef(errorVarRef.cause);
                }
                for (BLangNamedArgsExpression namedArgExpr : errorVarRef.detail) {
                    checkInvalidTypeDef(namedArgExpr.expr);
                }
                if (errorVarRef.restVar != null) {
                    checkInvalidTypeDef(errorVarRef.restVar);
                }
        }
    }

    private void validateAnnotationAttachmentExpr(BLangAnnotationAttachment annAttachmentNode,
                                                  BAnnotationSymbol annotationSymbol, AnalyzerData data) {
        if (annotationSymbol.attachedType == null ||
                types.isAssignable(annotationSymbol.attachedType, symTable.trueType)) {
            BLangExpression expr = annAttachmentNode.expr;
            if (expr != null) {
                this.typeChecker.checkExpr(expr, data.env, symTable.semanticError, data.prevEnvs,
                        data.commonAnalyzerData);
                this.dlog.error(expr.pos, DiagnosticErrorCode.ANNOTATION_ATTACHMENT_CANNOT_HAVE_A_VALUE,
                                annotationSymbol);
            }
            return;
        }

        BType annotType = annotationSymbol.attachedType;
        if (annAttachmentNode.expr == null) {
            BType annotConstrainedType = Types.getReferredType(annotType);
            BRecordType recordType = annotConstrainedType.tag == TypeTags.RECORD
                    ? (BRecordType) annotConstrainedType
                    : (annotConstrainedType.tag == TypeTags.ARRAY
                    && Types.getReferredType(((BArrayType) annotConstrainedType).eType).tag == TypeTags.RECORD ?
                    (BRecordType) Types.getReferredType(((BArrayType) annotConstrainedType).eType) : null);
            if (recordType != null && hasRequiredFields(recordType)) {
                this.dlog.error(annAttachmentNode.pos, DiagnosticErrorCode.ANNOTATION_ATTACHMENT_REQUIRES_A_VALUE,
                        recordType);
                return;
            }
        }

        if (annAttachmentNode.expr != null) {
            this.typeChecker.checkExpr(annAttachmentNode.expr, data.env,
                    annotType.tag == TypeTags.ARRAY ? ((BArrayType) annotType).eType : annotType, data.prevEnvs,
                    data.commonAnalyzerData);

            if (Symbols.isFlagOn(annotationSymbol.flags, Flags.CONSTANT)) {
                if (annotationSymbol.points.stream().anyMatch(attachPoint -> !attachPoint.source)) {
                    constantAnalyzer.analyzeExpr(annAttachmentNode.expr);
                    return;
                }
                checkAnnotConstantExpression(annAttachmentNode.expr);
            }
        }
    }

    /**
     * Check whether a record type has required fields.
     *
     * @param recordType Record type.
     * @return true if the record type has required fields; false otherwise.
     */
    public boolean hasRequiredFields(BRecordType recordType) {
        for (BField field : recordType.fields.values()) {
            if (Symbols.isFlagOn(field.symbol.flags, Flags.REQUIRED)) {
                return true;
            }
        }
        return false;
    }

    private void validateAnnotationAttachmentCount(List<BLangAnnotationAttachment> attachments) {
        Map<BAnnotationSymbol, Integer> attachmentCounts = new HashMap<>();
        for (BLangAnnotationAttachment attachment : attachments) {
            if (attachment.annotationSymbol == null) {
                continue;
            }

            attachmentCounts.merge(attachment.annotationSymbol, 1, Integer::sum);
        }

        attachmentCounts.forEach((symbol, count) -> {
            if ((symbol.attachedType == null || Types.getReferredType(symbol.attachedType).tag != TypeTags.ARRAY)
                    && count > 1) {
                Optional<BLangAnnotationAttachment> found = Optional.empty();
                for (BLangAnnotationAttachment attachment : attachments) {
                    if (attachment.annotationSymbol.equals(symbol)) {
                        found = Optional.of(attachment);
                        break;
                    }
                }
                this.dlog.error(found.get().pos,
                                DiagnosticErrorCode.ANNOTATION_ATTACHMENT_CANNOT_SPECIFY_MULTIPLE_VALUES, symbol);
            }
        });
    }

    private void validateBuiltinTypeAnnotationAttachment(List<BLangAnnotationAttachment> attachments,
                                                         AnalyzerData data) {

        if (PackageID.isLangLibPackageID(data.env.enclPkg.packageID)) {
            return;
        }
        for (BLangAnnotationAttachment attachment : attachments) {
            if (attachment.annotationSymbol == null || // annotation symbol can be null on invalid attachment.
                    !attachment.annotationSymbol.pkgID.equals(PackageID.ANNOTATIONS)) {
                continue;
            }
            String annotationName = attachment.annotationName.value;
            if (annotationName.equals(Names.ANNOTATION_TYPE_PARAM.value)) {
                dlog.error(attachment.pos, DiagnosticErrorCode.TYPE_PARAM_OUTSIDE_LANG_MODULE);
            } else if (annotationName.equals(Names.ANNOTATION_BUILTIN_SUBTYPE.value)) {
                dlog.error(attachment.pos, DiagnosticErrorCode.BUILTIN_SUBTYPE_OUTSIDE_LANG_MODULE);
            }
        }
    }

    // TODO: Check if the below method can be removed. This doesn't seem necessary.
    //  https://github.com/ballerina-platform/ballerina-lang/issues/20997
    /**
     * Validate functions attached to objects.
     *
     * @param funcNode Function node
     */
    private void validateObjectAttachedFunction(BLangFunction funcNode, AnalyzerData data) {
        if (!funcNode.attachedFunction) {
            return;
        }

        // If the function is attached to an abstract object, it don't need to have an implementation.
        if (!Symbols.isFlagOn(funcNode.receiver.getBType().tsymbol.flags, Flags.CLASS)) {
            if (funcNode.body != null) {
                dlog.error(funcNode.pos, DiagnosticErrorCode.ABSTRACT_OBJECT_FUNCTION_CANNOT_HAVE_BODY, funcNode.name,
                           funcNode.receiver.getBType());
            }
            return;
        }

        // There must be an implementation at the outer level, if the function is an interface.
        if (funcNode.interfaceFunction && !data.env.enclPkg.objAttachedFunctions.contains(funcNode.symbol)) {
            dlog.error(funcNode.pos, DiagnosticErrorCode.INVALID_INTERFACE_ON_NON_ABSTRACT_OBJECT, funcNode.name,
                       funcNode.receiver.getBType());
        }
    }

    private void validateInclusions(Set<Flag> referencingTypeFlags, List<BLangType> typeRefs, boolean objectTypeDesc,
                                    boolean objectConstructorExpr) {
        boolean nonIsolated = !referencingTypeFlags.contains(Flag.ISOLATED);
        boolean nonService = !referencingTypeFlags.contains(Flag.SERVICE);
        boolean nonClient = !referencingTypeFlags.contains(Flag.CLIENT);
        boolean nonReadOnly = !referencingTypeFlags.contains(Flag.READONLY);

        for (BLangType typeRef : typeRefs) {
            BType type = typeRef.getBType();
            long flags = type.flags;

            List<Flag> mismatchedFlags = new ArrayList<>();

            if (nonIsolated && Symbols.isFlagOn(flags, Flags.ISOLATED)) {
                mismatchedFlags.add(Flag.ISOLATED);
            }

            if (nonService && Symbols.isFlagOn(flags, Flags.SERVICE)) {
                mismatchedFlags.add(Flag.SERVICE);
            }

            if (nonClient && Symbols.isFlagOn(flags, Flags.CLIENT)) {
                mismatchedFlags.add(Flag.CLIENT);
            }

            if (!mismatchedFlags.isEmpty()) {
                StringBuilder qualifierString = new StringBuilder(mismatchedFlags.get(0).toString().toLowerCase());

                for (int i = 1; i < mismatchedFlags.size(); i++) {
                    qualifierString.append(" ").append(mismatchedFlags.get(i).toString().toLowerCase());
                }

                dlog.error(typeRef.pos,
                           objectConstructorExpr ?
                                   DiagnosticErrorCode.INVALID_REFERENCE_WITH_MISMATCHED_QUALIFIERS :
                                   DiagnosticErrorCode.INVALID_INCLUSION_WITH_MISMATCHED_QUALIFIERS,
                           qualifierString.toString());
            }

            BTypeSymbol tsymbol = type.tsymbol;
            if (tsymbol == null ||
                    (!Symbols.isFlagOn(tsymbol.flags, Flags.CLASS)
                            && Types.getReferredType(type).tag != TypeTags.INTERSECTION) ||
                    !Symbols.isFlagOn(flags, Flags.READONLY)) {
                continue;
            }

            if (objectTypeDesc) {
                dlog.error(typeRef.pos,
                           DiagnosticErrorCode.INVALID_READ_ONLY_CLASS_INCLUSION_IN_OBJECT_TYPE_DESCRIPTOR);
                continue;
            }

            if (nonReadOnly && !objectConstructorExpr) {
                if (Types.getReferredType(type).tag == TypeTags.INTERSECTION) {
                    dlog.error(typeRef.pos,
                               DiagnosticErrorCode.INVALID_READ_ONLY_TYPEDESC_INCLUSION_IN_NON_READ_ONLY_CLASS);
                    continue;
                }
                dlog.error(typeRef.pos, DiagnosticErrorCode.INVALID_READ_ONLY_CLASS_INCLUSION_IN_NON_READ_ONLY_CLASS);
            }
        }
    }

    private void validateReferencedFunction(Location pos, BAttachedFunction func, SymbolEnv env,
                                            DiagnosticErrorCode code) {
        BInvokableSymbol invokableSymbol = func.symbol;
        BType receiverType = invokableSymbol.receiverSymbol.type;
        if (!Symbols.isFlagOn(receiverType.tsymbol.flags, Flags.CLASS)) {
            return;
        }

        if (!Symbols.isFunctionDeclaration(invokableSymbol)) {
            return;
        }

        if (!env.enclPkg.objAttachedFunctions.contains(invokableSymbol)) {
            if (Symbols.isResource(invokableSymbol)) {
                // Use the function signature in the error msg since resource function name will contain `$`s if
                // we use the func.funcName
                dlog.error(pos, code, func, receiverType);
            } else {
                dlog.error(pos, code, func.funcName, receiverType);
            }
        }
    }

    private boolean isSimpleVarRef(BLangExpression expr) {
        if (expr.getBType().tag == TypeTags.SEMANTIC_ERROR ||
                expr.getBType().tag == TypeTags.NONE ||
                expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return false;
        }

        if (((BLangSimpleVarRef) expr).symbol == null) {
            return false;
        }

        return (((BLangSimpleVarRef) expr).symbol.tag & SymTag.VARIABLE) == SymTag.VARIABLE;
    }

    private void resetTypeNarrowing(BLangExpression lhsExpr, AnalyzerData data) {
        if (!isSimpleVarRef(lhsExpr)) {
            return;
        }

        BVarSymbol varSymbol = (BVarSymbol) ((BLangSimpleVarRef) lhsExpr).symbol;

        if (Symbols.isFlagOn(varSymbol.flags, Flags.FINAL)) {
            return;
        }

        if (varSymbol.originalSymbol == null) {
            return;
        }

        if (data.narrowedTypeInfo != null) {
            // Record the vars for which type narrowing was unset, to define relevant shadowed symbols in branches.
            BType currentType = ((BLangSimpleVarRef) lhsExpr).symbol.type;
            data.narrowedTypeInfo.put(typeNarrower.getOriginalVarSymbol(varSymbol),
                                      new BType.NarrowedTypes(currentType, currentType));
        }

        defineOriginalSymbol(lhsExpr, typeNarrower.getOriginalVarSymbol(varSymbol), data.env, data);
        data.env = data.prevEnvs.pop();
    }

    private void defineOriginalSymbol(BLangExpression lhsExpr, BVarSymbol varSymbol, SymbolEnv env, AnalyzerData data) {
        BSymbol foundSym = symResolver.lookupSymbolInMainSpace(env, varSymbol.name);

        // Terminate if we reach the env where the original symbol is available.
        if (foundSym == varSymbol) {
            data.prevEnvs.push(env);
            return;
        }

        // Traverse back to all the fall-back-environments, and update the env with the new symbol.
        // Here the existing fall-back env will be replaced by a new env.
        // i.e: [new fall-back env] = [snapshot of old fall-back env] + [new symbol]
        env = SymbolEnv.createTypeNarrowedEnv(lhsExpr, env);
        symbolEnter.defineTypeNarrowedSymbol(lhsExpr.pos, env, varSymbol, varSymbol.type,
                                             varSymbol.origin == VIRTUAL);
        SymbolEnv prevEnv = data.prevEnvs.pop();
        defineOriginalSymbol(lhsExpr, varSymbol, prevEnv, data);
        data.prevEnvs.push(env);
    }

    // TODO: 2022-03-05 check if this can be replaced to access info via annot attchments from the symbol
    private void validateIsolatedParamUsage(boolean inIsolatedFunction, BLangSimpleVariable variable,
                                            boolean isRestParam, AnalyzerData data) {
        if (!hasAnnotation(variable.annAttachments, Names.ANNOTATION_ISOLATED_PARAM.value)) {
            return;
        }

        variable.symbol.flags |= Flags.ISOLATED_PARAM;

        if (!PackageID.isLangLibPackageID(data.env.enclPkg.packageID)) {
            dlog.error(variable.pos, DiagnosticErrorCode.ISOLATED_PARAM_OUTSIDE_LANG_MODULE);
        }

        BType type = isRestParam ? ((BArrayType) variable.getBType()).eType : variable.getBType();

        if (!types.isSubTypeOfBaseType(type, TypeTags.INVOKABLE)) {
            dlog.error(variable.pos, DiagnosticErrorCode.ISOLATED_PARAM_USED_WITH_INVALID_TYPE);
        }

        if (!inIsolatedFunction) {
            dlog.error(variable.pos, DiagnosticErrorCode.ISOLATED_PARAM_USED_IN_A_NON_ISOLATED_FUNCTION);
        }
    }

    private boolean hasAnnotation(List<BLangAnnotationAttachment> attachments, String name) {
        for (BLangAnnotationAttachment attachment : attachments) {
            if (attachment.annotationName.value.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isConfigurable(BLangVariable varNode) {
        return varNode.flagSet.contains(Flag.CONFIGURABLE);
    }

    private boolean isIsolated(BLangVariable varNode) {
        return varNode.flagSet.contains(Flag.ISOLATED);
    }

    private void handleReadOnlyField(boolean isRecordType, LinkedHashMap<String, BField> fields,
                                     BLangSimpleVariable field, AnalyzerData data) {
        BType fieldType = field.getBType();

        if (fieldType == symTable.semanticError) {
            return;
        }

        BType readOnlyFieldType = getReadOnlyFieldType(field.pos, fieldType, data);

        if (readOnlyFieldType == symTable.semanticError) {
            dlog.error(field.pos, DiagnosticErrorCode.INVALID_READONLY_FIELD_TYPE, fieldType);
            return;
        }

        if (isRecordType) {
            fields.get(field.name.value).type = readOnlyFieldType;
        }

        field.setBType(field.symbol.type = readOnlyFieldType);
    }

    private BType getReadOnlyFieldType(Location pos, BType fieldType, AnalyzerData data) {
        if (types.isInherentlyImmutableType(fieldType) || Symbols.isFlagOn(fieldType.flags, Flags.READONLY)) {
            return fieldType;
        }

        if (!types.isSelectivelyImmutableType(fieldType, data.env.enclPkg.packageID)) {
            return symTable.semanticError;
        }

        return ImmutableTypeCloner.getImmutableIntersectionType(pos, types, fieldType, data.env,
                symTable, anonModelHelper, names, new HashSet<>());
    }

    private void setRestMatchPatternConstraintType(BRecordType recordType,
                                                   List<String> boundedFieldNames,
                                                   BRecordType restPatternRecordType,
                                                   BRecordType restVarSymbolRecordType, AnalyzerData data) {
        BType restConstraintType = symbolEnter.getRestMatchPatternConstraintType(recordType, new HashMap<>(),
                restVarSymbolRecordType.restFieldType);
        LinkedHashMap<String, BField> unMappedFields = new LinkedHashMap<>() {{
            putAll(recordType.fields);
        }};
        symbolEnter.setRestRecordFields(recordType.tsymbol.pos, data.env,
                unMappedFields, boundedFieldNames, restConstraintType, restVarSymbolRecordType);
        restPatternRecordType.restFieldType = restVarSymbolRecordType.restFieldType;
    }

    private List<BAnnotationAttachmentSymbol> getAnnotationAttachmentSymbols(
            List<BLangAnnotationAttachment> annAttachments) {
        List<BAnnotationAttachmentSymbol> annotationAttachmentSymbols = new ArrayList<>();

        for (BLangAnnotationAttachment annAttachment : annAttachments) {
            BAnnotationAttachmentSymbol annotationAttachmentSymbol = annAttachment.annotationAttachmentSymbol;

            if (annotationAttachmentSymbol == null) {
                continue;
            }

            annotationAttachmentSymbols.add(annotationAttachmentSymbol);
        }
        return annotationAttachmentSymbols;
    }

    private void validateTypesOfOverriddenFields(BLangStructureTypeNode structureTypeNode) {
        validateTypesOfOverriddenFields(structureTypeNode.getBType(), structureTypeNode.fields,
                                        structureTypeNode.typeRefs);
    }

    private void validateTypesOfOverriddenFields(BType type, List<BLangSimpleVariable> fields,
                                                 List<BLangType> includedTypeNodes) {
        if (type == symTable.semanticError) {
            return;
        }

        LinkedHashMap<String, BField> fieldsOfIncludingType = ((BStructureType) type).fields;
        Map<String, Location> explicitlySpecifiedFieldLocations = getFieldLocations(fields);

        for (BLangType includedTypeNode : includedTypeNodes) {
            BType includedType = Types.getReferredType(includedTypeNode.getBType());

            int includedTypeTag = includedType.tag;
            if (includedTypeTag != TypeTags.RECORD && includedTypeTag != TypeTags.OBJECT) {
                continue;
            }

            BStructureType includedStructureType = (BStructureType) includedType;

            for (Map.Entry<String, BField> includedFieldEntry : includedStructureType.fields.entrySet()) {
                String fieldName = includedFieldEntry.getKey();

                if (!explicitlySpecifiedFieldLocations.containsKey(fieldName) ||
                        // Happens when the type cannot be resolved.
                        !fieldsOfIncludingType.containsKey(fieldName)) {
                    continue;
                }

                BType fieldTypeInIncludingType = fieldsOfIncludingType.get(fieldName).type;
                if (fieldTypeInIncludingType == symTable.semanticError) {
                    continue;
                }

                BType fieldTypeInIncludedType = includedFieldEntry.getValue().type;
                if (fieldTypeInIncludedType == symTable.semanticError) {
                    continue;
                }

                if (!types.isAssignable(fieldTypeInIncludingType, fieldTypeInIncludedType)) {
                    dlog.error(explicitlySpecifiedFieldLocations.get(fieldName),
                               DiagnosticErrorCode.INCOMPATIBLE_SUB_TYPE_FIELD,
                               fieldName, fieldTypeInIncludedType, fieldTypeInIncludingType);
                }
            }
        }
    }

    private Map<String, Location> getFieldLocations(List<BLangSimpleVariable> fields) {
        Map<String, Location> locations = new HashMap<>(fields.size());

        for (BLangSimpleVariable field : fields) {
            locations.put(field.name.value, field.pos);
        }

        return locations;
    }

    /**
     * @since 2.0.0
     */
    public static class AnalyzerData {
        SymbolEnv env;
        BType expType;
        Map<BVarSymbol, BType.NarrowedTypes> narrowedTypeInfo;
        boolean notCompletedNormally;
        boolean breakFound;
        Types.CommonAnalyzerData commonAnalyzerData = new Types.CommonAnalyzerData();
        Stack<SymbolEnv> prevEnvs = new Stack<>();

        public AnalyzerData(SymbolEnv env) {
            this.env = env;
        }
    }
}

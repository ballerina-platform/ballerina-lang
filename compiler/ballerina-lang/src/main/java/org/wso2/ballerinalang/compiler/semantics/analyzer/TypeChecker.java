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
import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.InvokableSymbol;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.expressions.NamedArgNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.expressions.XMLNavigationAccess;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.BLangCompilerConstants;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.ballerinalang.util.diagnostic.DiagnosticWarningCode;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.BLangMissingNodesHelper;
import org.wso2.ballerinalang.compiler.parser.NodeCloner;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BLetSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourceFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourcePathSegmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.OCEDynamicEnvironmentData;
import org.wso2.ballerinalang.compiler.tree.SimpleBLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordVarNameField;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangDo;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.ClosureVarSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.NumericLiteralSupport;
import org.wso2.ballerinalang.compiler.util.TypeDefBuilderHelper;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.Unifier;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;

import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.INVALID_NUM_INSERTIONS;
import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.INVALID_NUM_STRINGS;
import static org.wso2.ballerinalang.compiler.tree.BLangInvokableNode.DEFAULT_WORKER_NAME;

/**
 * @since 0.94
 */
public class TypeChecker extends SimpleBLangNodeAnalyzer<TypeChecker.AnalyzerData> {

    private static final CompilerContext.Key<TypeChecker> TYPE_CHECKER_KEY = new CompilerContext.Key<>();
    private static Set<String> listLengthModifierFunctions = new HashSet<>();
    private static Map<String, HashSet<String>> modifierFunctions = new HashMap<>();

    private static final String LIST_LANG_LIB = "lang.array";
    private static final String MAP_LANG_LIB = "lang.map";
    private static final String TABLE_LANG_LIB = "lang.table";
    private static final String VALUE_LANG_LIB = "lang.value";
    private static final String XML_LANG_LIB = "lang.xml";

    private static final String FUNCTION_NAME_PUSH = "push";
    private static final String FUNCTION_NAME_POP = "pop";
    private static final String FUNCTION_NAME_SHIFT = "shift";
    private static final String FUNCTION_NAME_UNSHIFT = "unshift";
    private static final String FUNCTION_NAME_ENSURE_TYPE = "ensureType";

    private final BLangAnonymousModelHelper anonymousModelHelper;
    private final BLangDiagnosticLog dlog;
    private final BLangMissingNodesHelper missingNodesHelper;
    private final Names names;
    private final NodeCloner nodeCloner;
    private final SemanticAnalyzer semanticAnalyzer;
    private final SymbolEnter symbolEnter;
    private final SymbolResolver symResolver;
    private final SymbolTable symTable;
    private final TypeNarrower typeNarrower;
    private final TypeParamAnalyzer typeParamAnalyzer;
    private final Types types;
    private final Unifier unifier;
    protected final QueryTypeChecker queryTypeChecker;

    static {
        listLengthModifierFunctions.add(FUNCTION_NAME_PUSH);
        listLengthModifierFunctions.add(FUNCTION_NAME_POP);
        listLengthModifierFunctions.add(FUNCTION_NAME_SHIFT);
        listLengthModifierFunctions.add(FUNCTION_NAME_UNSHIFT);

        modifierFunctions.put(LIST_LANG_LIB, new HashSet<String>() {{
            add("remove");
            add("removeAll");
            add("setLength");
            add("reverse");
            add("sort");
            add("pop");
            add("push");
            add("shift");
            add("unshift");
        }});

        modifierFunctions.put(MAP_LANG_LIB, new HashSet<String>() {{
            add("remove");
            add("removeIfHasKey");
            add("removeAll");
        }});

        modifierFunctions.put(TABLE_LANG_LIB, new HashSet<String>() {{
            add("put");
            add("add");
            add("remove");
            add("removeIfHasKey");
            add("removeAll");
        }});

        modifierFunctions.put(VALUE_LANG_LIB, new HashSet<String>() {{
            add("mergeJson");
        }});

        modifierFunctions.put(XML_LANG_LIB, new HashSet<String>() {{
            add("setName");
            add("setChildren");
            add("strip");
        }});
    }

    public static TypeChecker getInstance(CompilerContext context) {
        TypeChecker typeChecker = context.get(TYPE_CHECKER_KEY);
        if (typeChecker == null) {
            typeChecker = new TypeChecker(context);
        }

        return typeChecker;
    }

    public TypeChecker(CompilerContext context) {
        context.put(TYPE_CHECKER_KEY, this);

        this.names = Names.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.nodeCloner = NodeCloner.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.typeNarrower = TypeNarrower.getInstance(context);
        this.typeParamAnalyzer = TypeParamAnalyzer.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.semanticAnalyzer = SemanticAnalyzer.getInstance(context);
        this.missingNodesHelper = BLangMissingNodesHelper.getInstance(context);
        this.unifier = new Unifier();
        this.queryTypeChecker = QueryTypeChecker.getInstance(context);
    }

    public TypeChecker(CompilerContext context, CompilerContext.Key<TypeChecker> key) {
        context.put(key, this);

        this.names = Names.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.nodeCloner = NodeCloner.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.typeNarrower = TypeNarrower.getInstance(context);
        this.typeParamAnalyzer = TypeParamAnalyzer.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.semanticAnalyzer = SemanticAnalyzer.getInstance(context);
        this.missingNodesHelper = BLangMissingNodesHelper.getInstance(context);
        this.unifier = new Unifier();
        this.queryTypeChecker = null;
    }

    private BType checkExpr(BLangExpression expr, SymbolEnv env, AnalyzerData data) {
        return checkExpr(expr, env, symTable.noType, data);
    }

    private BType checkExpr(BLangExpression expr, AnalyzerData data) {
        return checkExpr(expr, data.env, symTable.noType, data);
    }

    private BType checkExpr(BLangExpression expr, SymbolEnv env, BType expType, AnalyzerData data) {
        return checkExpr(expr, env, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data);
    }

    private BType checkExpr(BLangExpression expr, BType expType, AnalyzerData data) {
        return checkExpr(expr, data.env, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data);
    }

    public BType checkExpr(BLangExpression expr, SymbolEnv env) {
        return checkExpr(expr, env, symTable.noType, new Stack<>());
    }

    public BType checkExpr(BLangExpression expr, SymbolEnv env, Stack<SymbolEnv> prevEnvs,
                           Types.CommonAnalyzerData commonAnalyzerData) {
        return checkExpr(expr, env, symTable.noType, prevEnvs, commonAnalyzerData);
    }

    public BType checkExpr(BLangExpression expr, SymbolEnv env, BType expType, Stack<SymbolEnv> prevEnvs) {
        final AnalyzerData data = new AnalyzerData();
        data.env = env;
        data.prevEnvs = prevEnvs;
        data.commonAnalyzerData.queryFinalClauses = new Stack<>();
        data.commonAnalyzerData.queryEnvs = new Stack<>();
        return checkExpr(expr, env, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data);
    }

    public BType checkExpr(BLangExpression expr, SymbolEnv env, BType expType, Stack<SymbolEnv> prevEnvs,
                           Types.CommonAnalyzerData commonAnalyzerData) {
        final AnalyzerData data = new AnalyzerData();
        data.env = env;
        data.prevEnvs = prevEnvs;
        data.commonAnalyzerData = commonAnalyzerData;
        return checkExpr(expr, env, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data);
    }

    @Override
    public void analyzeNode(BLangNode node, AnalyzerData data) {
        // Ignore
    }

    @Override
    public void visit(BLangPackage node, AnalyzerData data) {
    }

    public BType checkExpr(BLangExpression expr, SymbolEnv env, BType expType, DiagnosticCode diagCode,
                           AnalyzerData data) {
        if (expr.typeChecked) {
            return expr.getBType();
        }

        if (expType.tag == TypeTags.INTERSECTION) {
            expType = ((BIntersectionType) expType).effectiveType;
        }

        SymbolEnv prevEnv = data.env;
        BType preExpType = data.expType;
        DiagnosticCode preDiagCode = data.diagCode;
        data.env = env;
        data.diagCode = diagCode;
        data.expType = expType;
        data.isTypeChecked = true;

        expr.expectedType = expType;

        expr.accept(this, data);

        BType resultRefType = Types.getReferredType(data.resultType);
        if (resultRefType.tag == TypeTags.INTERSECTION) {
            data.resultType = ((BIntersectionType) resultRefType).effectiveType;
        }

        expr.setTypeCheckedType(data.resultType);
        expr.typeChecked = data.isTypeChecked;
        data.env = prevEnv;
        data.expType = preExpType;
        data.diagCode = preDiagCode;

        validateAndSetExprExpectedType(expr, data);

        return data.resultType;
    }

    private void analyzeObjectConstructor(BLangNode node, SymbolEnv env, AnalyzerData data) {
        if (!data.commonAnalyzerData.nonErrorLoggingCheck) {
            semanticAnalyzer.analyzeNode(node, env, data.commonAnalyzerData);
        }
    }

    private void validateAndSetExprExpectedType(BLangExpression expr, AnalyzerData data) {
        if (data.resultType.tag == TypeTags.SEMANTIC_ERROR) {
            return;
        }

        // If the expected type is a map, but a record type is inferred due to the presence of `readonly` fields in
        // the mapping constructor expression, we don't override the expected type.
        if (expr.getKind() == NodeKind.RECORD_LITERAL_EXPR && expr.expectedType != null &&
                Types.getReferredType(expr.expectedType).tag == TypeTags.MAP
                && Types.getReferredType(expr.getBType()).tag == TypeTags.RECORD) {
            return;
        }

        expr.expectedType = data.resultType;
    }


    // Expressions

    public void visit(BLangLiteral literalExpr, AnalyzerData data) {

        BType literalType = setLiteralValueAndGetType(literalExpr, data.expType, data);
        if (literalType == symTable.semanticError) {
            data.resultType = symTable.semanticError;
            return;
        }
        if (literalExpr.isFiniteContext) {
            return;
        }
        data.resultType = types.checkType(literalExpr, literalType, data.expType);
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess, AnalyzerData data) {
        // check for undeclared namespaces.
        checkXMLNamespacePrefixes(xmlElementAccess.filters, data);
        checkExpr(xmlElementAccess.expr, symTable.xmlType, data);
        data.resultType = types.checkType(xmlElementAccess, symTable.xmlElementSeqType, data.expType);
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation, AnalyzerData data) {
        checkXMLNamespacePrefixes(xmlNavigation.filters, data);
        if (xmlNavigation.childIndex != null) {
            checkExpr(xmlNavigation.childIndex, symTable.intType, data);
        }
        BType exprType = checkExpr(xmlNavigation.expr, symTable.xmlType, data);

        if (Types.getReferredType(exprType).tag == TypeTags.UNION) {
            dlog.error(xmlNavigation.pos, DiagnosticErrorCode.TYPE_DOES_NOT_SUPPORT_XML_NAVIGATION_ACCESS,
                       xmlNavigation.expr.getBType());
        }

        BType actualType = xmlNavigation.navAccessType == XMLNavigationAccess.NavAccessType.CHILDREN
                ? symTable.xmlType : symTable.xmlElementSeqType;

        types.checkType(xmlNavigation, actualType, data.expType);
        if (xmlNavigation.navAccessType == XMLNavigationAccess.NavAccessType.CHILDREN) {
            data.resultType = symTable.xmlType;
        } else {
            data.resultType = symTable.xmlElementSeqType;
        }
    }

    private void checkXMLNamespacePrefixes(List<BLangXMLElementFilter> filters, AnalyzerData data) {
        for (BLangXMLElementFilter filter : filters) {
            if (!filter.namespace.isEmpty()) {
                Name nsName = names.fromString(filter.namespace);
                BSymbol nsSymbol = symResolver.lookupSymbolInPrefixSpace(data.env, nsName);
                filter.namespaceSymbol = nsSymbol;
                if (nsSymbol.getKind() != SymbolKind.XMLNS) {
                    dlog.error(filter.nsPos, DiagnosticErrorCode.CANNOT_FIND_XML_NAMESPACE, nsName);
                }
            }
        }
    }

    private int getPreferredMemberTypeTag(BFiniteType finiteType) {
        for (BLangExpression valueExpr : finiteType.getValueSpace()) {
            int typeTag = Types.getReferredType(valueExpr.getBType()).tag;
            if (typeTag > TypeTags.DECIMAL) {
                continue;
            }
            for (int i = TypeTags.INT; i <= TypeTags.DECIMAL; i++) {
                if (typeTag == i) {
                    return i;
                }
            }
        }
        return TypeTags.NONE;
    }

    private BType getFiniteTypeMatchWithIntType(BLangLiteral literalExpr, BFiniteType finiteType, AnalyzerData data) {
        if (literalAssignableToFiniteType(literalExpr, finiteType, TypeTags.INT)) {
            setLiteralValueForFiniteType(literalExpr, symTable.intType, data);
            return symTable.intType;
        } else if (literalAssignableToFiniteType(literalExpr, finiteType, TypeTags.BYTE)) {
            setLiteralValueForFiniteType(literalExpr, symTable.byteType, data);
            return symTable.byteType;

        } else {
            for (int tag = TypeTags.SIGNED32_INT; tag <= TypeTags.UNSIGNED8_INT; tag++) {
                if (literalAssignableToFiniteType(literalExpr, finiteType, tag)) {
                    setLiteralValueForFiniteType(literalExpr, symTable.getTypeFromTag(tag), data);
                    return symTable.getTypeFromTag(tag);
                }
            }
        }
        return symTable.noType;
    }

    private BType getFiniteTypeMatchWithIntLiteral(BLangLiteral literalExpr, BFiniteType finiteType,
                                                   Object literalValue, AnalyzerData data) {
        BType intLiteralType = getFiniteTypeMatchWithIntType(literalExpr, finiteType, data);
        if (intLiteralType != symTable.noType) {
            return intLiteralType;
        }
        int typeTag = getPreferredMemberTypeTag(finiteType);
        if (typeTag == TypeTags.NONE) {
            return symTable.intType;
        }
        if (literalAssignableToFiniteType(literalExpr, finiteType, typeTag)) {
            BType type = symTable.getTypeFromTag(typeTag);
            setLiteralValueForFiniteType(literalExpr, type, data);
            literalExpr.value = String.valueOf(literalValue);
            return type;
        }
        // Handle out of range ints
        if (literalValue instanceof Double) {
            return symTable.floatType;
        }
        if (literalValue instanceof String) {
            return symTable.decimalType;
        }
        return symTable.intType;
    }

    private BType silentIntTypeCheck(BLangLiteral literalExpr, Object literalValue, BType expType,
                                       AnalyzerData data) {
        boolean prevNonErrorLoggingCheck = data.commonAnalyzerData.nonErrorLoggingCheck;
        data.commonAnalyzerData.nonErrorLoggingCheck = true;
        int prevErrorCount = this.dlog.errorCount();
        this.dlog.resetErrorCount();
        this.dlog.mute();

        BType exprCompatibleType = getIntegerLiteralType(nodeCloner.cloneNode(literalExpr), literalValue, expType,
                data);
        data.commonAnalyzerData.nonErrorLoggingCheck = prevNonErrorLoggingCheck;
        this.dlog.setErrorCount(prevErrorCount);

        if (!prevNonErrorLoggingCheck) {
            this.dlog.unmute();
        }
        return exprCompatibleType;
    }

    private BType silentCompatibleLiteralTypeCheck(BFiniteType finiteType, BLangLiteral literalExpr,
                                                      Object literalValue, AnalyzerData data) {
        BType resIntType = symTable.semanticError;
        for (BLangExpression valueExpr : finiteType.getValueSpace()) {
            resIntType = silentIntTypeCheck(literalExpr, literalValue, valueExpr.getBType(), data);
            if (resIntType != symTable.semanticError) {
                return resIntType;
            }
        }
        return resIntType;
    }

    private BType checkIfOutOfRangeAndReturnType(BFiniteType finiteType, BLangLiteral literalExpr, Object literalValue,
                                               AnalyzerData data) {
        BType compatibleType = silentCompatibleLiteralTypeCheck(finiteType, literalExpr, literalValue, data);
        if (compatibleType == symTable.semanticError) {
            dlog.error(literalExpr.pos, DiagnosticErrorCode.OUT_OF_RANGE, literalExpr.originalValue,
                    literalExpr.getBType());
        }
        return compatibleType;
    }

    private BType getIntegerLiteralType(BLangLiteral literalExpr, Object literalValue, BType expType,
                                        AnalyzerData data) {
        BType expectedType = Types.getReferredType(expType);
        if (expectedType.tag == TypeTags.BYTE || TypeTags.isIntegerTypeTag(expectedType.tag)) {
            BType resultType = getIntLiteralType(expType, literalValue, data);
            if (resultType == symTable.semanticError) {
                dlog.error(literalExpr.pos, DiagnosticErrorCode.OUT_OF_RANGE, literalExpr.originalValue, expType);
            }
            return resultType;
        } else if (expectedType.tag == TypeTags.FLOAT) {
            // The literalValue will be a string if it was not within the bounds of what is supported by Java Long
            // or Double when it was parsed in BLangNodeBuilder
            if (literalValue instanceof String) {
                dlog.error(literalExpr.pos, DiagnosticErrorCode.OUT_OF_RANGE, literalExpr.originalValue,
                        expectedType);
                return symTable.semanticError;
            }
            if (literalValue instanceof Double) {
                literalExpr.value = ((Double) literalValue).doubleValue();
            } else {
                literalExpr.value = ((Long) literalValue).doubleValue();
            }
            return symTable.floatType;
        } else if (expectedType.tag == TypeTags.DECIMAL) {
            literalExpr.value = String.valueOf(literalValue);
            return symTable.decimalType;
        } else if (expectedType.tag == TypeTags.FINITE) {
            BFiniteType finiteType = (BFiniteType) expectedType;
            BType compatibleType = checkIfOutOfRangeAndReturnType(finiteType, literalExpr, literalValue, data);
            if (compatibleType == symTable.semanticError) {
                return compatibleType;
            } else {
                return getFiniteTypeMatchWithIntLiteral(literalExpr, finiteType, literalValue, data);
            }
        } else if (expectedType.tag == TypeTags.UNION) {
            BUnionType expectedUnionType = (BUnionType) expectedType;
            List<BType> memberTypes = types.getAllReferredTypes(expectedUnionType);
            for (BType memType : memberTypes) {
                int tag = memType.tag;
                if (TypeTags.isIntegerTypeTag(tag) || tag == TypeTags.BYTE) {
                    BType intLiteralType = getIntLiteralType(memType, literalValue, data);
                    if (intLiteralType == memType) {
                        return intLiteralType;
                    }
                } else if (tag == TypeTags.JSON || tag == TypeTags.ANYDATA || tag == TypeTags.ANY) {
                    if (literalValue instanceof Double) {
                        return symTable.floatType;
                    }
                    if (literalValue instanceof String) {
                        return symTable.decimalType;
                    }
                    return symTable.intType;
                }
            }

            BType finiteType = getFiniteTypeWithValuesOfSingleType(expectedUnionType, symTable.intType);
            if (finiteType != symTable.semanticError) {
                BType setType = setLiteralValueAndGetType(literalExpr, finiteType, data);
                if (literalExpr.isFiniteContext) {
                    // i.e., a match was found for a finite type
                    return setType;
                }
            }
            BType finiteTypeMatchingByte = getFiniteTypeWithValuesOfSingleType(expectedUnionType, symTable.byteType);
            if (finiteTypeMatchingByte != symTable.semanticError) {
                finiteType = finiteTypeMatchingByte;
                BType setType = setLiteralValueAndGetType(literalExpr, finiteType, data);
                if (literalExpr.isFiniteContext) {
                    // i.e., a match was found for a finite type
                    return setType;
                }
            }
            return getTypeMatchingFloatOrDecimal(finiteType, memberTypes, literalExpr, expectedUnionType, data);
        }
        if (!(literalValue instanceof Long)) {
            dlog.error(literalExpr.pos, DiagnosticErrorCode.OUT_OF_RANGE, literalExpr.originalValue,
                    literalExpr.getBType());
            return symTable.semanticError;
        }
        return symTable.intType;
    }

    private BType getTypeOfLiteralWithFloatDiscriminator(BLangLiteral literalExpr, Object literalValue,
                                                         BType expType, AnalyzerData data) {
        String numericLiteral = NumericLiteralSupport.stripDiscriminator(String.valueOf(literalValue));
        if (!types.validateFloatLiteral(literalExpr.pos, numericLiteral)) {
            return symTable.semanticError;
        }
        literalExpr.value = Double.parseDouble(numericLiteral);
        BType referredType = Types.getReferredType(expType);
        if (referredType.tag == TypeTags.FINITE) {
            BFiniteType finiteType = (BFiniteType) referredType;
            if (literalAssignableToFiniteType(literalExpr, finiteType, TypeTags.FLOAT)) {
                setLiteralValueForFiniteType(literalExpr, symTable.floatType, data);
                return symTable.floatType;
            }
        } else if (referredType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) referredType;
            BType unionMember = getAndSetAssignableUnionMember(literalExpr, unionType, symTable.floatType, data);
            if (unionMember != symTable.noType) {
                return unionMember;
            }
        }
        return symTable.floatType;
    }

    private BType getTypeOfLiteralWithDecimalDiscriminator(BLangLiteral literalExpr, Object literalValue,
                                                           BType expType, AnalyzerData data) {
        literalExpr.value = NumericLiteralSupport.stripDiscriminator(String.valueOf(literalValue));
        if (!types.isValidDecimalNumber(literalExpr.pos, literalExpr.value.toString())) {
            return symTable.semanticError;
        }
        BType referredType = Types.getReferredType(expType);
        if (referredType.tag == TypeTags.FINITE) {
            BFiniteType finiteType = (BFiniteType) referredType;
            if (literalAssignableToFiniteType(literalExpr, finiteType, TypeTags.DECIMAL)) {
                setLiteralValueForFiniteType(literalExpr, symTable.decimalType, data);
                return symTable.decimalType;
            }
        } else if (referredType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) expType;
            BType unionMember = getAndSetAssignableUnionMember(literalExpr, unionType, symTable.decimalType, data);
            if (unionMember != symTable.noType) {
                return unionMember;
            }
        }
        return symTable.decimalType;
    }

    private BType getTypeOfDecimalFloatingPointLiteral(BLangLiteral literalExpr, Object literalValue, BType expType,
                                                       AnalyzerData data) {
        BType expectedType = Types.getReferredType(expType);
        String numericLiteral = String.valueOf(literalValue);
        if (expectedType.tag == TypeTags.DECIMAL) {
            if (types.isValidDecimalNumber(literalExpr.pos, literalExpr.value.toString())) {
                return symTable.decimalType;
            }
            return symTable.semanticError;
        } else if (expectedType.tag == TypeTags.FLOAT) {
            if (!types.validateFloatLiteral(literalExpr.pos, numericLiteral)) {
                data.resultType = symTable.semanticError;
                return symTable.semanticError;
            }
            return symTable.floatType;
        } else if (expectedType.tag == TypeTags.FINITE) {
            BFiniteType finiteType = (BFiniteType) expectedType;
            for (int tag = TypeTags.FLOAT; tag <= TypeTags.DECIMAL; tag++) {
                if (literalAssignableToFiniteType(literalExpr, finiteType, tag)) {
                    BType valueType = setLiteralValueAndGetType(literalExpr,  symTable.getTypeFromTag(tag), data);
                    setLiteralValueForFiniteType(literalExpr, valueType, data);
                    return valueType;
                }
            }
        } else if (expectedType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) expectedType;
            for (int tag = TypeTags.FLOAT; tag <= TypeTags.DECIMAL; tag++) {
                BType unionMember =
                        getAndSetAssignableUnionMember(literalExpr, unionType, symTable.getTypeFromTag(tag), data);
                if (unionMember == symTable.floatType && !types.validateFloatLiteral(literalExpr.pos, numericLiteral)) {
                    return symTable.semanticError;
                } else if (unionMember != symTable.noType) {
                    return unionMember;
                }
            }
        }
        return types.validateFloatLiteral(literalExpr.pos, numericLiteral)
                ? symTable.floatType : symTable.semanticError;
    }

    private BType getTypeOfHexFloatingPointLiteral(BLangLiteral literalExpr, Object literalValue, BType expType,
                                                   AnalyzerData data) {
        String numericLiteral = String.valueOf(literalValue);
        if (!types.validateFloatLiteral(literalExpr.pos, numericLiteral)) {
            return symTable.semanticError;
        }
        literalExpr.value = Double.parseDouble(numericLiteral);
        BType referredType = Types.getReferredType(expType);
        if (referredType.tag == TypeTags.FINITE) {
            BFiniteType finiteType = (BFiniteType) referredType;
            if (literalAssignableToFiniteType(literalExpr, finiteType, TypeTags.FLOAT)) {
                setLiteralValueForFiniteType(literalExpr, symTable.floatType, data);
                return symTable.floatType;
            }
        } else if (referredType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) referredType;
            BType unionMember = getAndSetAssignableUnionMember(literalExpr, unionType, symTable.floatType, data);
            if (unionMember != symTable.noType) {
                return unionMember;
            }
        }
        return symTable.floatType;
    }

    public BType setLiteralValueAndGetType(BLangLiteral literalExpr, BType expType, AnalyzerData data) {
        literalExpr.isFiniteContext = false;
        Object literalValue = literalExpr.value;
        BType expectedType = Types.getReferredType(expType);

        if (literalExpr.getKind() == NodeKind.NUMERIC_LITERAL) {
            NodeKind kind = ((BLangNumericLiteral) literalExpr).kind;
            if (kind == NodeKind.INTEGER_LITERAL) {
                return getIntegerLiteralType(literalExpr, literalValue, expectedType, data);
            } else if (kind == NodeKind.DECIMAL_FLOATING_POINT_LITERAL) {
                if (NumericLiteralSupport.isFloatDiscriminated(literalExpr.originalValue)) {
                    return getTypeOfLiteralWithFloatDiscriminator(literalExpr, literalValue, expectedType, data);
                } else if (NumericLiteralSupport.isDecimalDiscriminated(literalExpr.originalValue)) {
                    return getTypeOfLiteralWithDecimalDiscriminator(literalExpr, literalValue, expectedType, data);
                } else {
                    return getTypeOfDecimalFloatingPointLiteral(literalExpr, literalValue, expectedType, data);
                }
            } else {
                return getTypeOfHexFloatingPointLiteral(literalExpr, literalValue, expectedType, data);
            }
        }

        // Get the type matching to the tag from the symbol table.
        BType literalType = symTable.getTypeFromTag(literalExpr.getBType().tag);
        if (literalType.tag == TypeTags.STRING && types.isCharLiteralValue((String) literalValue)) {
            if (expectedType.tag == TypeTags.CHAR_STRING) {
                return symTable.charStringType;
            }
            if (expectedType.tag == TypeTags.UNION) {
                Set<BType> memberTypes = new HashSet<>(types.getAllTypes(expectedType, true));
                for (BType memType : memberTypes) {
                    memType = Types.getReferredType(memType);
                    if (TypeTags.isStringTypeTag(memType.tag)) {
                        return setLiteralValueAndGetType(literalExpr, memType, data);
                    } else if (memType.tag == TypeTags.JSON || memType.tag == TypeTags.ANYDATA ||
                            memType.tag == TypeTags.ANY) {
                        return setLiteralValueAndGetType(literalExpr, symTable.charStringType, data);
                    } else if (memType.tag == TypeTags.FINITE && types.isAssignableToFiniteType(memType,
                            literalExpr)) {
                        setLiteralValueForFiniteType(literalExpr, symTable.charStringType, data);
                        return literalType;
                    }
                }
            }
            boolean foundMember = types.isAssignableToFiniteType(expectedType, literalExpr);
            if (foundMember) {
                setLiteralValueForFiniteType(literalExpr, literalType, data);
                return literalType;
            }
        } else {
            if (expectedType.tag == TypeTags.FINITE) {
                boolean foundMember = types.isAssignableToFiniteType(expectedType, literalExpr);
                if (foundMember) {
                    setLiteralValueForFiniteType(literalExpr, literalType, data);
                    return literalType;
                }
            } else if (expectedType.tag == TypeTags.UNION) {
                BUnionType unionType = (BUnionType) expectedType;
                boolean foundMember = types.getAllTypes(unionType, true)
                        .stream()
                        .anyMatch(memberType -> types.isAssignableToFiniteType(memberType, literalExpr));
                if (foundMember) {
                    setLiteralValueForFiniteType(literalExpr, literalType, data);
                    return literalType;
                }
            }
        }

        if (Types.getReferredType(literalExpr.getBType()).tag == TypeTags.BYTE_ARRAY) {
            // check whether this is a byte array
            byte[] byteArray = types.convertToByteArray((String) literalExpr.value);
            literalType = new BArrayType(symTable.byteType, null, byteArray.length,
                    BArrayState.CLOSED);
            if (Symbols.isFlagOn(expectedType.flags, Flags.READONLY)) {
                literalType = ImmutableTypeCloner.getEffectiveImmutableType(literalExpr.pos, types,
                        literalType, data.env, symTable, anonymousModelHelper, names);
            }

            if (expectedType.tag == TypeTags.ARRAY) {
                BArrayType arrayType = (BArrayType) expectedType;
                if (arrayType.state == BArrayState.INFERRED) {
                    arrayType.size = byteArray.length;
                    arrayType.state = BArrayState.CLOSED;
                }
            }
        }

        return literalType;
    }

    private BType getTypeMatchingFloatOrDecimal(BType finiteType, List<BType> memberTypes, BLangLiteral literalExpr,
                                                BUnionType expType, AnalyzerData data) {
        for (int tag = TypeTags.FLOAT; tag <= TypeTags.DECIMAL; tag++) {
            if (finiteType == symTable.semanticError) {
                BType type = symTable.getTypeFromTag(tag);
                for (BType memType : memberTypes) {
                    if (memType.tag == tag) {
                        return setLiteralValueAndGetType(literalExpr, type, data);
                    }
                }

                finiteType = getFiniteTypeWithValuesOfSingleType((BUnionType) expType, type);
                if (finiteType != symTable.semanticError) {
                    BType setType = setLiteralValueAndGetType(literalExpr, finiteType, data);
                    if (literalExpr.isFiniteContext) {
                        // i.e., a match was found for a finite type
                        return setType;
                    }
                }
            }
        }
        return symTable.intType;
    }

    private BType getAndSetAssignableUnionMember(BLangLiteral literalExpr, BUnionType expType, BType desiredType,
                                                 AnalyzerData data) {
        List<BType> members = types.getAllTypes(expType, true);
        Set<BType> memberTypes = new HashSet<>();
        members.forEach(member -> memberTypes.addAll(members));
        if (memberTypes.stream()
                .anyMatch(memType -> memType.tag == desiredType.tag
                        || memType.tag == TypeTags.JSON
                        || memType.tag == TypeTags.ANYDATA
                        || memType.tag == TypeTags.ANY)) {
            return desiredType;
        }

        BType finiteType = getFiniteTypeWithValuesOfSingleType(expType, desiredType);
        if (finiteType != symTable.semanticError) {
            BType setType = setLiteralValueAndGetType(literalExpr, finiteType, data);
            if (literalExpr.isFiniteContext) {
                // i.e., a match was found for a finite type
                return setType;
            }
        }
        return symTable.noType;
    }

    private boolean literalAssignableToFiniteType(BLangLiteral literalExpr, BFiniteType finiteType,
                                                  int targetMemberTypeTag) {
        for (BLangExpression valueExpr : finiteType.getValueSpace()) {
            if (valueExpr.getBType().tag == targetMemberTypeTag &&
                    types.checkLiteralAssignabilityBasedOnType((BLangLiteral) valueExpr, literalExpr)) {
                return true;
            }
        }
        return false;
    }

    private void setLiteralValueForFiniteType(BLangLiteral literalExpr, BType type, AnalyzerData data) {
        types.setImplicitCastExpr(literalExpr, type, data.expType);
        data.resultType = type;
        literalExpr.isFiniteContext = true;
    }

    private BType getFiniteTypeWithValuesOfSingleType(BUnionType unionType, BType matchType) {
        List<BFiniteType> finiteTypeMembers = types.getAllTypes(unionType, true).stream()
                .filter(memType -> memType.tag == TypeTags.FINITE)
                .map(memFiniteType -> (BFiniteType) memFiniteType)
                .collect(Collectors.toList());

        if (finiteTypeMembers.isEmpty()) {
            return symTable.semanticError;
        }

        int tag = matchType.tag;
        Set<BLangExpression> matchedValueSpace = new LinkedHashSet<>();

        for (BFiniteType finiteType : finiteTypeMembers) {
            Set<BLangExpression> set = new HashSet<>();
            for (BLangExpression expression : finiteType.getValueSpace()) {
                if (expression.getBType().tag == tag) {
                    set.add(expression);
                }
            }
            matchedValueSpace.addAll(set);
        }

        if (matchedValueSpace.isEmpty()) {
            return symTable.semanticError;
        }

        return new BFiniteType(null, matchedValueSpace);
    }

    private BType getIntLiteralType(BType expType, Object literalValue, AnalyzerData data) {
        // The literalValue will be a string if it is not within the bounds of what is supported by Java Long,
        // indicating that it is an overflown Ballerina int
        if (!(literalValue instanceof Long)) {
            data.resultType = symTable.semanticError;
            return symTable.semanticError;
        }
        switch (expType.tag) {
            case TypeTags.INT:
                return symTable.intType;
            case TypeTags.BYTE:
                if (types.isByteLiteralValue((Long) literalValue)) {
                    return symTable.byteType;
                }
                break;
            case TypeTags.SIGNED32_INT:
                if (types.isSigned32LiteralValue((Long) literalValue)) {
                    return symTable.signed32IntType;
                }
                break;
            case TypeTags.SIGNED16_INT:
                if (types.isSigned16LiteralValue((Long) literalValue)) {
                    return symTable.signed16IntType;
                }
                break;
            case TypeTags.SIGNED8_INT:
                if (types.isSigned8LiteralValue((Long) literalValue)) {
                    return symTable.signed8IntType;
                }
                break;
            case TypeTags.UNSIGNED32_INT:
                if (types.isUnsigned32LiteralValue((Long) literalValue)) {
                    return symTable.unsigned32IntType;
                }
                break;
            case TypeTags.UNSIGNED16_INT:
                if (types.isUnsigned16LiteralValue((Long) literalValue)) {
                    return symTable.unsigned16IntType;
                }
                break;
            case TypeTags.UNSIGNED8_INT:
                if (types.isUnsigned8LiteralValue((Long) literalValue)) {
                    return symTable.unsigned8IntType;
                }
                break;
            default:
        }
        return symTable.intType;
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructor, AnalyzerData data) {
        BType expType = data.expType;
        if (expType.tag == TypeTags.NONE || expType.tag == TypeTags.READONLY) {
            BType inferredType = getInferredTupleType(listConstructor, expType, data);
            data.resultType = inferredType == symTable.semanticError ?
                    symTable.semanticError : types.checkType(listConstructor, inferredType, expType);
            return;
        }

        data.resultType = checkListConstructorCompatibility(expType, listConstructor, data);
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr, AnalyzerData data) {
        BType expType = data.expType;
        if (expType.tag == TypeTags.NONE || expType.tag == TypeTags.ANY || expType.tag == TypeTags.ANYDATA) {
            InferredTupleDetails inferredTupleDetails =
                    checkExprList(new ArrayList<>(tableConstructorExpr.recordLiteralList), data);

            // inferredTupleDetails cannot have restMemberTypes as it does not support spread operator yet.
            List<BType> memTypes = inferredTupleDetails.fixedMemberTypes;
            for (BType memType : memTypes) {
                if (memType == symTable.semanticError) {
                    data.resultType = symTable.semanticError;
                    return;
                }
            }

            // If we don't have a contextually applicable type and don't have members in the table constructor expr,
            // we cannot derive the table type
            if (expType.tag == TypeTags.NONE && tableConstructorExpr.recordLiteralList.size() == 0) {
                dlog.error(tableConstructorExpr.pos, DiagnosticErrorCode.CANNOT_INFER_MEMBER_TYPE_FOR_TABLE);
                data.resultType = symTable.semanticError;
                return;
            }

            // if the contextually expected type is `any` and the key specifier is defined,
            // then we cannot derive a table type
            if (expType.tag == TypeTags.ANY && tableConstructorExpr.tableKeySpecifier != null) {
                dlog.error(tableConstructorExpr.tableKeySpecifier.pos,
                        DiagnosticErrorCode.KEY_SPECIFIER_NOT_ALLOWED_FOR_TARGET_ANY);
                data.resultType = symTable.semanticError;
                return;
            }

            BType inherentMemberType;
            if (tableConstructorExpr.tableKeySpecifier == null && expType.tag != TypeTags.NONE) {
                inherentMemberType = getMappingConstructorCompatibleNonUnionType(expType, data);
            } else {
                inherentMemberType = inferTableMemberType(memTypes, tableConstructorExpr, data);
                for (BLangRecordLiteral recordLiteral : tableConstructorExpr.recordLiteralList) {
                    recordLiteral.setBType(inherentMemberType);
                }
            }
            BTableType tableType = new BTableType(TypeTags.TABLE, inherentMemberType, null);
            if (!validateTableConstructorExpr(tableConstructorExpr, tableType, data)) {
                data.resultType = symTable.semanticError;
                return;
            }
            if (checkKeySpecifier(tableConstructorExpr, tableType, data)) {
                return;
            }
            data.resultType = tableType;
            return;
        }

        BType applicableExpType = Types.getReferredType(expType);

        applicableExpType = applicableExpType.tag == TypeTags.INTERSECTION ?
                ((BIntersectionType) applicableExpType).effectiveType : applicableExpType;

        if (applicableExpType.tag == TypeTags.TABLE) {
            List<BType> memTypes = new ArrayList<>();
            for (BLangRecordLiteral recordLiteral : tableConstructorExpr.recordLiteralList) {
                BLangRecordLiteral clonedExpr = recordLiteral;
                if (data.commonAnalyzerData.nonErrorLoggingCheck) {
                    clonedExpr.cloneAttempt++;
                    clonedExpr = nodeCloner.cloneNode(recordLiteral);
                }
                BType recordType = checkExpr(clonedExpr, ((BTableType) applicableExpType).constraint, data);
                if (recordType == symTable.semanticError) {
                    data.resultType = symTable.semanticError;
                    return;
                }
                memTypes.add(recordType);
            }

            if (!(validateKeySpecifierInTableConstructor((BTableType) applicableExpType,
                    tableConstructorExpr.recordLiteralList, data) &&
                    validateTableConstructorExpr(tableConstructorExpr, (BTableType) applicableExpType, data))) {
                data.resultType = symTable.semanticError;
                return;
            }

            BTableType expectedTableType = (BTableType) applicableExpType;
            if (expectedTableType.constraint.tag == TypeTags.MAP && expectedTableType.isTypeInlineDefined) {
                if (validateMapConstraintTable(applicableExpType)) {
                    data.resultType = symTable.semanticError;
                    return;
                }
                data.resultType = expType;
                return;
            }

            BTableType tableType = new BTableType(TypeTags.TABLE, inferTableMemberType(memTypes, applicableExpType),
                                                  null);

            if (Symbols.isFlagOn(applicableExpType.flags, Flags.READONLY)) {
                tableType.flags |= Flags.READONLY;
            }

            if (checkKeySpecifier(tableConstructorExpr, tableType, data)) {
                return;
            }

            if (!expectedTableType.fieldNameList.isEmpty() && tableType.fieldNameList.isEmpty()) {
                tableType.fieldNameList = expectedTableType.fieldNameList;
            }

            if (isSameTableType(tableType, (BTableType) applicableExpType)) {
                data.resultType = expType;
            } else {
                data.resultType = tableType;
            }
        } else if (applicableExpType.tag == TypeTags.UNION) {

            boolean prevNonErrorLoggingCheck = data.commonAnalyzerData.nonErrorLoggingCheck;
            data.commonAnalyzerData.nonErrorLoggingCheck = true;
            int errorCount = this.dlog.errorCount();
            this.dlog.mute();

            List<BType> matchingTypes = new ArrayList<>();
            BUnionType expectedType = (BUnionType) applicableExpType;
            for (BType memType : expectedType.getMemberTypes()) {
                dlog.resetErrorCount();

                BLangTableConstructorExpr clonedTableExpr = tableConstructorExpr;
                if (data.commonAnalyzerData.nonErrorLoggingCheck) {
                    tableConstructorExpr.cloneAttempt++;
                    clonedTableExpr = nodeCloner.cloneNode(tableConstructorExpr);
                }

                BType resultType = checkExpr(clonedTableExpr, memType, data);
                if (resultType != symTable.semanticError && dlog.errorCount() == 0 &&
                        isUniqueType(matchingTypes, resultType)) {
                    matchingTypes.add(resultType);
                }
            }

            data.commonAnalyzerData.nonErrorLoggingCheck = prevNonErrorLoggingCheck;
            this.dlog.setErrorCount(errorCount);
            if (!prevNonErrorLoggingCheck) {
                this.dlog.unmute();
            }

            if (matchingTypes.isEmpty()) {
                BLangTableConstructorExpr exprToLog = tableConstructorExpr;
                if (data.commonAnalyzerData.nonErrorLoggingCheck) {
                    tableConstructorExpr.cloneAttempt++;
                    exprToLog = nodeCloner.cloneNode(tableConstructorExpr);
                }

                dlog.error(tableConstructorExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, expType,
                        getInferredTableType(exprToLog, data));

            } else if (matchingTypes.size() != 1) {
                dlog.error(tableConstructorExpr.pos, DiagnosticErrorCode.AMBIGUOUS_TYPES,
                        expType);
            } else {
                data.resultType = checkExpr(tableConstructorExpr, matchingTypes.get(0), data);
                return;
            }
            data.resultType = symTable.semanticError;
        } else {
            data.resultType = types.checkType(tableConstructorExpr.pos,
                    getInferredTableType(nodeCloner.cloneNode(tableConstructorExpr), data), expType,
                    DiagnosticErrorCode.INCOMPATIBLE_TYPES);
        }
    }

    private BType getInferredTableType(BLangTableConstructorExpr exprToLog, AnalyzerData data) {
        InferredTupleDetails inferredTupleDetails =
                checkExprList(new ArrayList<>(exprToLog.recordLiteralList), data);

        // inferredTupleDetails cannot have restMemberTypes as it does not support spread operator yet.
        List<BType> memTypes = inferredTupleDetails.fixedMemberTypes;
        for (BType memType : memTypes) {
            if (memType == symTable.semanticError) {
                return  symTable.semanticError;
            }
        }

        return new BTableType(TypeTags.TABLE, inferTableMemberType(memTypes, exprToLog, data), null);
    }

    private boolean checkKeySpecifier(BLangTableConstructorExpr tableConstructorExpr, BTableType tableType,
                                      AnalyzerData data) {
        if (tableConstructorExpr.tableKeySpecifier != null) {
            if (!(validateTableKeyValue(getTableKeyNameList(tableConstructorExpr.
                    tableKeySpecifier), tableConstructorExpr.recordLiteralList, tableType.constraint, data))) {
                data.resultType = symTable.semanticError;
                return true;
            }
            tableType.fieldNameList = getTableKeyNameList(tableConstructorExpr.tableKeySpecifier);
        }
        return false;
    }

    private BType inferTableMemberType(List<BType> memTypes, BType expType) {

        if (memTypes.isEmpty()) {
            return ((BTableType) expType).constraint;
        }

        LinkedHashSet<BType> result = new LinkedHashSet<>();

        result.add(memTypes.get(0));

        BUnionType unionType = BUnionType.create(null, result);
        for (int i = 1; i < memTypes.size(); i++) {
            BType source = memTypes.get(i);
            if (!types.isAssignable(source, unionType)) {
                result.add(source);
                unionType = BUnionType.create(null, result);
            }
        }

        if (unionType.getMemberTypes().size() == 1) {
            return memTypes.get(0);
        }

        return unionType;
    }

    private BType inferTableMemberType(List<BType> memTypes, BLangTableConstructorExpr tableConstructorExpr,
                                       AnalyzerData data) {
        BLangTableKeySpecifier keySpecifier = tableConstructorExpr.tableKeySpecifier;
        List<String> keySpecifierFieldNames = new ArrayList<>();
        List<BType> restFieldTypes = new ArrayList<>();


        if (keySpecifier != null) {
            for (IdentifierNode identifierNode : keySpecifier.fieldNameIdentifierList) {
                keySpecifierFieldNames.add(((BLangIdentifier) identifierNode).value);
            }
        }

        LinkedHashMap<String, List<BField>> fieldNameToFields = new LinkedHashMap<>();
        for (BType memType : memTypes) {
            BRecordType member = (BRecordType) memType;
            for (Map.Entry<String, BField> entry : member.fields.entrySet()) {
                String key = entry.getKey();
                BField field = entry.getValue();

                if (fieldNameToFields.containsKey(key)) {
                    fieldNameToFields.get(key).add(field);
                } else {
                    fieldNameToFields.put(key, new ArrayList<>() {{
                        add(field);
                    }});
                }
            }

            if (!member.sealed) {
                restFieldTypes.add(member.restFieldType);
            }
        }

        LinkedHashSet<BField> inferredFields = new LinkedHashSet<>();
        int memTypesSize = memTypes.size();

        for (Map.Entry<String, List<BField>> entry : fieldNameToFields.entrySet()) {
            String fieldName = entry.getKey();
            List<BField> fields = entry.getValue();

            List<BType> types = new ArrayList<>();
            for (BField field : fields) {
                types.add(field.getType());
            }

            for (BType memType : memTypes) {
                BRecordType bMemType = (BRecordType) memType;
                if (bMemType.sealed || bMemType.fields.containsKey(fieldName)) {
                    continue;
                }

                BType restFieldType = bMemType.restFieldType;
                types.add(restFieldType);
            }

            BField resultantField = createFieldWithType(fields.get(0), types);
            boolean isOptional = hasOptionalFields(fields) || fields.size() != memTypesSize;

            if (isOptional) {
                resultantField.symbol.flags = Flags.OPTIONAL;
            } else if (keySpecifierFieldNames.contains(fieldName)) {
                resultantField.symbol.flags = Flags.REQUIRED | Flags.READONLY;
            } else {
                resultantField.symbol.flags = Flags.REQUIRED;
            }

            inferredFields.add(resultantField);
        }

        return createTableConstraintRecordType(inferredFields, restFieldTypes, tableConstructorExpr.pos, data);
    }

    private boolean isSameTableType(BTableType source, BTableType target) {
        return target.keyTypeConstraint != symTable.neverType && source.constraint.equals(target.constraint) &&
                source.fieldNameList.equals(target.fieldNameList);
    }

    /**
     * Create a new {@code BField} out of existing {@code BField}, while changing its type.
     * The new type is derived from the given list of bTypes.
     *
     * @param field  - existing {@code BField}
     * @param bTypes - list of bTypes
     * @return a {@code BField}
     */
    private BField createFieldWithType(BField field, List<BType> bTypes) {
        BType resultantType = getResultantType(bTypes);

        BVarSymbol originalSymbol = field.symbol;
        BVarSymbol fieldSymbol = new BVarSymbol(originalSymbol.flags, originalSymbol.name, originalSymbol.pkgID,
                resultantType, originalSymbol.owner, originalSymbol.pos, VIRTUAL);

        return new BField(field.name, field.pos, fieldSymbol);
    }

    /**
     * Get the resultant type from a {@code List<BType>}.
     *
     * @param bTypes bType list (size > 0)
     * @return {@code BUnionType} if effective members in list is > 1. {@code BType} Otherwise.
     */
    private BType getResultantType(List<BType> bTypes) {
        LinkedHashSet<BType> bTypeSet = new LinkedHashSet<>(bTypes);
        List<BType> flattenBTypes = new ArrayList<>(bTypes.size());
        addFlattenMemberTypes(flattenBTypes, bTypeSet);

        return getRepresentativeBroadType(flattenBTypes);
    }

    private void addFlattenMemberTypes(List<BType> flattenBTypes, LinkedHashSet<BType> bTypes) {
        for (BType memberType : bTypes) {
            BType bType;
            switch (memberType.tag) {
                case TypeTags.UNION:
                    addFlattenMemberTypes(flattenBTypes, ((BUnionType) memberType).getMemberTypes());
                    continue;
                case TypeTags.TYPEREFDESC:
                    BType constraint = Types.getReferredType(memberType);
                    if (constraint.tag == TypeTags.UNION) {
                        addFlattenMemberTypes(flattenBTypes, ((BUnionType) constraint).getMemberTypes());
                        continue;
                    }
                    bType = constraint;
                    break;
                default:
                    bType = memberType;
                    break;
            }

            flattenBTypes.add(bType);
        }
    }

    private boolean hasOptionalFields(List<BField> fields) {
        for (BField field : fields) {
            if (field.symbol.getFlags().contains(Flag.OPTIONAL)) {
                return true;
            }
        }
        return false;
    }

    private BRecordType createTableConstraintRecordType(Set<BField> inferredFields, List<BType> restFieldTypes,
                                                        Location pos, AnalyzerData data) {
        PackageID pkgID = data.env.enclPkg.symbol.pkgID;
        BRecordTypeSymbol recordSymbol = createRecordTypeSymbol(pkgID, pos, VIRTUAL, data);

        for (BField field : inferredFields) {
            recordSymbol.scope.define(field.name, field.symbol);
        }

        BRecordType recordType = new BRecordType(recordSymbol);
        recordType.fields = inferredFields.stream().collect(getFieldCollector());

        recordSymbol.type = recordType;
        recordType.tsymbol = recordSymbol;

        BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(recordType, pkgID, symTable,
                pos);
        recordTypeNode.initFunction = TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, data.env,
                                                                                           names, symTable);
        TypeDefBuilderHelper.createTypeDefinitionForTSymbol(recordType, recordSymbol, recordTypeNode, data.env);

        if (restFieldTypes.isEmpty()) {
            recordType.sealed = true;
            recordType.restFieldType = symTable.noType;
        } else {
            recordType.restFieldType = getResultantType(restFieldTypes);
        }

        return recordType;
    }

    private Collector<BField, ?, LinkedHashMap<String, BField>> getFieldCollector() {
        BinaryOperator<BField> mergeFunc = (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
        return Collectors.toMap(field -> field.name.value, Function.identity(), mergeFunc, LinkedHashMap::new);
    }

    private boolean validateKeySpecifierInTableConstructor(BTableType tableType,
                                                         List<BLangRecordLiteral> recordLiterals, AnalyzerData data) {
        List<String> fieldNameList = tableType.fieldNameList;
        if (!fieldNameList.isEmpty()) {
            return validateTableKeyValue(fieldNameList, recordLiterals, tableType.constraint, data);
        }
        return true;
    }

    private boolean validateTableKeyValue(List<String> keySpecifierFieldNames, List<BLangRecordLiteral> rows,
                                          BType constraint, AnalyzerData data) {
        for (BLangRecordLiteral row : rows) {
            for (String fieldName : keySpecifierFieldNames) {
                BField field = types.getTableConstraintField(constraint, fieldName);
                BLangExpression recordKeyValueField = getRecordKeyValueField(row, fieldName);
                if (recordKeyValueField != null && isConstExpression(recordKeyValueField)) {
                    continue;
                }

                if (recordKeyValueField == null && isFieldWithDefaultValue(field)) {
                    dlog.error(row.pos,
                            DiagnosticErrorCode.UNSUPPORTED_USAGE_OF_DEFAULT_VALUES_FOR_KEY_FIELD_IN_TABLE_MEMBER);
                } else {
                    dlog.error(row.pos,
                            DiagnosticErrorCode.KEY_SPECIFIER_FIELD_VALUE_MUST_BE_CONSTANT_EXPR, fieldName);
                }
                data.resultType = symTable.semanticError;
            }
        }

        return data.resultType != symTable.semanticError;
    }

    private boolean isFieldWithDefaultValue(BField field) {
        long flags = field.symbol.flags;
        return !Symbols.isFlagOn(flags, Flags.REQUIRED) && !Symbols.isFlagOn(flags, Flags.OPTIONAL);
    }

    private boolean isConstExpression(BLangExpression expression) {
        switch(expression.getKind()) {
            case LITERAL:
            case NUMERIC_LITERAL:
            case STRING_TEMPLATE_LITERAL:
            case XML_ELEMENT_LITERAL:
            case XML_TEXT_LITERAL:
            case LIST_CONSTRUCTOR_EXPR:
            case TABLE_CONSTRUCTOR_EXPR:
            case RECORD_LITERAL_EXPR:
            case TYPE_CONVERSION_EXPR:
            case UNARY_EXPR:
            case BINARY_EXPR:
            case TYPE_TEST_EXPR:
            case TERNARY_EXPR:
            case REG_EXP_TEMPLATE_LITERAL:
                return true;
            case SIMPLE_VARIABLE_REF:
                return (((BLangSimpleVarRef) expression).symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT;
            case GROUP_EXPR:
                return isConstExpression(((BLangGroupExpr) expression).expression);
            default:
                return false;
        }
    }

    private BLangExpression getRecordKeyValueField(BLangRecordLiteral recordLiteral,
                                                            String fieldName) {
        for (RecordLiteralNode.RecordField recordField : recordLiteral.fields) {
            if (recordField.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField recordKeyValueField =
                        (BLangRecordLiteral.BLangRecordKeyValueField) recordField;
                if (fieldName.equals(recordKeyValueField.key.toString())) {
                    return recordKeyValueField.valueExpr;
                }
            } else if (recordField.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                if (fieldName.equals(((BLangRecordVarNameField) recordField).variableName.value)) {
                    return (BLangRecordLiteral.BLangRecordVarNameField) recordField;
                }
            }
        }

        return null;
    }

    public boolean validateKeySpecifier(List<String> fieldNameList, BType constraint,
                                         Location pos) {
        for (String fieldName : fieldNameList) {
            BField field = types.getTableConstraintField(constraint, fieldName);
            if (field == null) {
                dlog.error(pos,
                        DiagnosticErrorCode.INVALID_FIELD_NAMES_IN_KEY_SPECIFIER, fieldName, constraint);
                return true;
            }

            if (!Symbols.isFlagOn(field.symbol.flags, Flags.READONLY)) {
                dlog.error(pos,
                        DiagnosticErrorCode.KEY_SPECIFIER_FIELD_MUST_BE_READONLY, fieldName);
                return true;
            }

            if (Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL)) {
                dlog.error(pos,
                        DiagnosticErrorCode.KEY_SPECIFIER_FIELD_MUST_BE_REQUIRED, fieldName);
                return true;
            }

            if (!types.isAssignable(field.type, symTable.anydataType)) {
                dlog.error(pos,
                        DiagnosticErrorCode.KEY_SPECIFIER_FIELD_MUST_BE_ANYDATA, fieldName, constraint);
                return true;
            }
        }
        return false;
    }

    private boolean validateTableConstructorExpr(BLangTableConstructorExpr tableConstructorExpr, BTableType tableType,
                                                 AnalyzerData data) {
        BType constraintType = Types.getReferredType(tableType.constraint);
        List<String> fieldNameList = new ArrayList<>();
        boolean isKeySpecifierEmpty = tableConstructorExpr.tableKeySpecifier == null;
        if (!isKeySpecifierEmpty) {
            fieldNameList.addAll(getTableKeyNameList(tableConstructorExpr.tableKeySpecifier));

            if (tableType.fieldNameList.isEmpty() && validateKeySpecifier(fieldNameList,
                    constraintType.tag != TypeTags.INTERSECTION ? constraintType :
                            ((BIntersectionType) constraintType).effectiveType,
                    tableConstructorExpr.tableKeySpecifier.pos)) {
                data.resultType = symTable.semanticError;
                return false;
            }

            if (!tableType.fieldNameList.isEmpty() && !tableType.fieldNameList.equals(fieldNameList)) {
                dlog.error(tableConstructorExpr.tableKeySpecifier.pos, DiagnosticErrorCode.TABLE_KEY_SPECIFIER_MISMATCH,
                        tableType.fieldNameList.toString(), fieldNameList.toString());
                data.resultType = symTable.semanticError;
                return false;
            }
        }

        BType keyTypeConstraint = tableType.keyTypeConstraint;
        if (keyTypeConstraint != null) {
            keyTypeConstraint = Types.getReferredType(keyTypeConstraint);
            List<BType> memberTypes = new ArrayList<>();

            switch (keyTypeConstraint.tag) {
                case TypeTags.TUPLE:
                    for (BType type : ((BTupleType) keyTypeConstraint).getTupleTypes()) {
                        memberTypes.add(type);
                    }
                    break;
                case TypeTags.RECORD:
                    Map<String, BField> fieldList = ((BRecordType) keyTypeConstraint).getFields();
                    memberTypes = fieldList.entrySet().stream()
                            .filter(e -> fieldNameList.contains(e.getKey())).map(entry -> entry.getValue().type)
                            .collect(Collectors.toList());
                    if (memberTypes.isEmpty()) {
                        memberTypes.add(keyTypeConstraint);
                    }
                    break;
                default:
                    memberTypes.add(keyTypeConstraint);
            }

            if (isKeySpecifierEmpty && keyTypeConstraint.tag == TypeTags.NEVER) {
                return true;
            }

            if (isKeySpecifierEmpty ||
                    tableConstructorExpr.tableKeySpecifier.fieldNameIdentifierList.size() != memberTypes.size()) {
                if (isKeySpecifierEmpty) {
                    dlog.error(tableConstructorExpr.pos,
                            DiagnosticErrorCode.KEY_SPECIFIER_EMPTY_FOR_PROVIDED_KEY_CONSTRAINT, memberTypes);
                } else {
                    dlog.error(tableConstructorExpr.pos,
                            DiagnosticErrorCode.KEY_SPECIFIER_SIZE_MISMATCH_WITH_KEY_CONSTRAINT,
                            memberTypes, tableConstructorExpr.tableKeySpecifier.fieldNameIdentifierList);
                }
                data.resultType = symTable.semanticError;
                return false;
            }

            List<IdentifierNode> fieldNameIdentifierList = tableConstructorExpr.tableKeySpecifier.
                    fieldNameIdentifierList;

            int index = 0;
            for (IdentifierNode identifier : fieldNameIdentifierList) {
                BField field = types.getTableConstraintField(constraintType, ((BLangIdentifier) identifier).value);
                if (field == null || !types.isAssignable(field.type, memberTypes.get(index))) {
                    dlog.error(tableConstructorExpr.tableKeySpecifier.pos,
                            DiagnosticErrorCode.KEY_SPECIFIER_MISMATCH_WITH_KEY_CONSTRAINT,
                            fieldNameIdentifierList.toString(), memberTypes.toString());
                    data.resultType = symTable.semanticError;
                    return false;
                }
                index++;
            }
        }

        return true;
    }

    public boolean validateMapConstraintTable(BType expType) {
        if (expType != null && (!((BTableType) expType).fieldNameList.isEmpty() ||
                ((BTableType) expType).keyTypeConstraint != null) &&
                !expType.tsymbol.owner.getFlags().contains(Flag.LANG_LIB)) {
            dlog.error(((BTableType) expType).keyPos,
                    DiagnosticErrorCode.KEY_CONSTRAINT_NOT_SUPPORTED_FOR_TABLE_WITH_MAP_CONSTRAINT);
            return true;
        }
        return false;
    }

    private List<String> getTableKeyNameList(BLangTableKeySpecifier tableKeySpecifier) {
        List<String> fieldNamesList = new ArrayList<>();
        for (IdentifierNode identifier : tableKeySpecifier.fieldNameIdentifierList) {
            fieldNamesList.add(((BLangIdentifier) identifier).value);
        }

        return fieldNamesList;
    }

    private BType createTableKeyConstraint(List<String> fieldNames, BType constraintType) {
        if (fieldNames.isEmpty()) {
            return symTable.semanticError;
        }

        List<BTupleMember> memTypes = new ArrayList<>();
        for (String fieldName : fieldNames) {
            //null is not possible for field
            BField tableConstraintField = types.getTableConstraintField(constraintType, fieldName);

            if (tableConstraintField == null) {
                return symTable.semanticError;
            }

            BType fieldType = tableConstraintField.type;
            BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(fieldType);
            memTypes.add(new BTupleMember(fieldType, varSymbol));
        }

        if (memTypes.size() == 1) {
            return memTypes.get(0).type;
        }

        return new BTupleType(memTypes);
    }

    private BType checkListConstructorCompatibility(BType bType, BLangListConstructorExpr listConstructor,
                                                    AnalyzerData data) {
        int tag = bType.tag;
        if (tag == TypeTags.UNION) {
            boolean prevNonErrorLoggingCheck = data.commonAnalyzerData.nonErrorLoggingCheck;
            int errorCount = this.dlog.errorCount();
            data.commonAnalyzerData.nonErrorLoggingCheck = true;
            this.dlog.mute();

            List<BType> compatibleTypes = new ArrayList<>();
            boolean erroredExpType = false;
            for (BType memberType : ((BUnionType) bType).getMemberTypes()) {
                if (memberType == symTable.semanticError) {
                    if (!erroredExpType) {
                        erroredExpType = true;
                    }
                    continue;
                }

                BType listCompatibleMemType = getListConstructorCompatibleNonUnionType(memberType, data);
                if (listCompatibleMemType == symTable.semanticError) {
                    continue;
                }

                dlog.resetErrorCount();
                BType memCompatibiltyType = checkListConstructorCompatibility(listCompatibleMemType, listConstructor,
                                                                              data);
                if (memCompatibiltyType != symTable.semanticError && dlog.errorCount() == 0 &&
                        isUniqueType(compatibleTypes, memCompatibiltyType)) {
                    compatibleTypes.add(memCompatibiltyType);
                }
            }

            data.commonAnalyzerData.nonErrorLoggingCheck = prevNonErrorLoggingCheck;
            this.dlog.setErrorCount(errorCount);
            if (!prevNonErrorLoggingCheck) {
                this.dlog.unmute();
            }

            if (compatibleTypes.isEmpty()) {
                BLangListConstructorExpr exprToLog = listConstructor;
                if (data.commonAnalyzerData.nonErrorLoggingCheck) {
                    listConstructor.cloneAttempt++;
                    exprToLog = nodeCloner.cloneNode(listConstructor);
                }

                BType inferredTupleType = getInferredTupleType(exprToLog, symTable.noType, data);

                if (!erroredExpType && inferredTupleType != symTable.semanticError) {
                    dlog.error(listConstructor.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data.expType,
                               inferredTupleType);
                }
                return symTable.semanticError;
            } else if (compatibleTypes.size() != 1) {
                dlog.error(listConstructor.pos, DiagnosticErrorCode.AMBIGUOUS_TYPES,
                        data.expType);
                return symTable.semanticError;
            }

            return checkListConstructorCompatibility(compatibleTypes.get(0), listConstructor, data);
        }

        if (tag == TypeTags.TYPEREFDESC) {
            BType refType = Types.getReferredType(bType);
            BType compatibleType = checkListConstructorCompatibility(refType, listConstructor, data);
            return compatibleType == refType ? bType : compatibleType;
        }

        if (tag == TypeTags.INTERSECTION) {
            return checkListConstructorCompatibility(((BIntersectionType) bType).effectiveType, listConstructor, data);
        }

        BType possibleType = getListConstructorCompatibleNonUnionType(bType, data);

        switch (possibleType.tag) {
            case TypeTags.ARRAY:
                return checkArrayType(listConstructor, (BArrayType) possibleType, data);
            case TypeTags.TUPLE:
                return checkTupleType(listConstructor, (BTupleType) possibleType, data);
            case TypeTags.READONLY:
                return checkReadOnlyListType(listConstructor, data);
            case TypeTags.TYPEDESC:
                // i.e typedesc t = [int, string]
                listConstructor.isTypedescExpr = true;
                InferredTupleDetails inferredTupleDetails = new InferredTupleDetails();
                for (BLangExpression expr : listConstructor.exprs) {
                    if (expr.getKind() == NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                        BLangExpression spreadOpExpr = ((BLangListConstructorSpreadOpExpr) expr).expr;
                        BType spreadOpExprType = checkExpr(spreadOpExpr, symTable.noType, data);
                        updateInferredTupleDetailsFromSpreadMember(expr.pos, spreadOpExprType, inferredTupleDetails);
                        continue;
                    }

                    BType resultType = checkExpr(expr, symTable.noType, data);

                    BType memberType = resultType;
                    if (expr.getKind() == NodeKind.TYPEDESC_EXPRESSION) {
                        memberType = ((BLangTypedescExpr) expr).resolvedType;
                    } else if (expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                        memberType = ((BLangSimpleVarRef) expr).symbol.type;
                    }

                    if (inferredTupleDetails.restMemberTypes.isEmpty()) {
                        inferredTupleDetails.fixedMemberTypes.add(memberType);
                    } else {
                        inferredTupleDetails.restMemberTypes.add(memberType);
                    }
                }

                List<BTupleMember> members = new ArrayList<>();
                inferredTupleDetails.fixedMemberTypes.forEach(memberType -> members.add(new BTupleMember(memberType,
                        new BVarSymbol(memberType.flags, null, null, memberType, null, null, null))));
                BTupleType tupleType = new BTupleType(members);
                if (!inferredTupleDetails.restMemberTypes.isEmpty()) {
                    tupleType.restType = getRepresentativeBroadType(inferredTupleDetails.restMemberTypes);
                }

                listConstructor.typedescType = tupleType;
                return new BTypedescType(listConstructor.typedescType, null);
        }

        BLangListConstructorExpr exprToLog = listConstructor;
        if (data.commonAnalyzerData.nonErrorLoggingCheck) {
            listConstructor.cloneAttempt++;
            exprToLog = nodeCloner.cloneNode(listConstructor);
        }

        if (bType == symTable.semanticError) {
            // Ignore the return value, we only need to visit the expressions.
            getInferredTupleType(exprToLog, symTable.semanticError, data);
        } else {
            dlog.error(listConstructor.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, bType,
                    getInferredTupleType(exprToLog, symTable.noType, data));
        }

        return symTable.semanticError;
    }

    private void updateInferredTupleDetailsFromSpreadMember(Location spreadMemberPos, BType spreadOpExprType,
                                                            InferredTupleDetails inferredTupleDetails) {
        BType originalExprType = spreadOpExprType;
        spreadOpExprType = Types.getReferredType(spreadOpExprType);

        if (!inferredTupleDetails.restMemberTypes.isEmpty()) {
            if (spreadOpExprType.tag == TypeTags.TUPLE) {
                BTupleType bTupleType = (BTupleType) spreadOpExprType;
                bTupleType.getTupleTypes().forEach(t -> inferredTupleDetails.restMemberTypes.add(t));
                if (!types.isFixedLengthTuple(bTupleType)) {
                    inferredTupleDetails.restMemberTypes.add(bTupleType.restType);
                }
            } else if (spreadOpExprType.tag == TypeTags.ARRAY) {
                BArrayType bArrayType = (BArrayType) spreadOpExprType;
                inferredTupleDetails.restMemberTypes.add(bArrayType.eType);
            } else {
                dlog.error(spreadMemberPos, DiagnosticErrorCode.CANNOT_INFER_TYPE_FROM_SPREAD_OP, originalExprType);
                inferredTupleDetails.restMemberTypes.add(symTable.semanticError);
            }
            return;
        }

        if (spreadOpExprType.tag == TypeTags.TUPLE) {
            BTupleType bTupleType = (BTupleType) spreadOpExprType;
            inferredTupleDetails.fixedMemberTypes.addAll(bTupleType.getTupleTypes());
            if (!types.isFixedLengthTuple(bTupleType)) {
                inferredTupleDetails.restMemberTypes.add(bTupleType.restType);
            }
        } else if (spreadOpExprType.tag == TypeTags.ARRAY) {
            BArrayType bArrayType = (BArrayType) spreadOpExprType;
            if (bArrayType.state == BArrayState.CLOSED) {
                for (int i = 0; i < bArrayType.size; i++) {
                    BType memberType = bArrayType.eType;
                    inferredTupleDetails.fixedMemberTypes.add(memberType);
                }
            } else {
                inferredTupleDetails.restMemberTypes.add(bArrayType.eType);
            }
        } else {
            dlog.error(spreadMemberPos, DiagnosticErrorCode.CANNOT_INFER_TYPE_FROM_SPREAD_OP, originalExprType);
            inferredTupleDetails.fixedMemberTypes.add(symTable.semanticError);
        }
    }

    private BType getListConstructorCompatibleNonUnionType(BType type, AnalyzerData data) {
        switch (type.tag) {
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
            case TypeTags.READONLY:
            case TypeTags.TYPEDESC:
                return type;
            case TypeTags.JSON:
                return !Symbols.isFlagOn(type.flags, Flags.READONLY) ? symTable.arrayJsonType :
                        ImmutableTypeCloner.getEffectiveImmutableType(null, types, symTable.arrayJsonType,
                                data.env, symTable, anonymousModelHelper, names);
            case TypeTags.ANYDATA:
                return !Symbols.isFlagOn(type.flags, Flags.READONLY) ? symTable.arrayAnydataType :
                        ImmutableTypeCloner.getEffectiveImmutableType(null, types, symTable.arrayAnydataType,
                                data.env, symTable, anonymousModelHelper, names);
            case TypeTags.ANY:
                return !Symbols.isFlagOn(type.flags, Flags.READONLY) ? symTable.arrayAllType :
                        ImmutableTypeCloner.getEffectiveImmutableType(null, types, symTable.arrayAllType, data.env,
                                                                      symTable, anonymousModelHelper, names);
            case TypeTags.INTERSECTION:
                return ((BIntersectionType) type).effectiveType;
            case TypeTags.TYPEREFDESC:
                return type;
        }
        return symTable.semanticError;
    }

    private BType checkArrayType(BLangListConstructorExpr listConstructor, BArrayType arrayType, AnalyzerData data) {
        int listExprSize = 0;
        if (arrayType.state != BArrayState.OPEN) {
            for (BLangExpression expr : listConstructor.exprs) {
                if (expr.getKind() != NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                    listExprSize++;
                    continue;
                }

                BLangExpression spreadOpExpr = ((BLangListConstructorSpreadOpExpr) expr).expr;
                BType spreadOpType = checkExpr(spreadOpExpr, data);
                spreadOpType = Types.getReferredType(spreadOpType);

                switch (spreadOpType.tag) {
                    case TypeTags.ARRAY:
                        int arraySize = ((BArrayType) spreadOpType).size;
                        if (arraySize >= 0) {
                            listExprSize += arraySize;
                            continue;
                        }

                        dlog.error(spreadOpExpr.pos,
                                DiagnosticErrorCode.INVALID_SPREAD_OP_FIXED_LENGTH_LIST_EXPECTED);
                        return symTable.semanticError;
                    case TypeTags.TUPLE:
                        BTupleType tType = (BTupleType) spreadOpType;
                        if (types.isFixedLengthTuple(tType)) {
                            listExprSize += tType.getMembers().size();
                            continue;
                        }

                        dlog.error(spreadOpExpr.pos,
                                DiagnosticErrorCode.INVALID_SPREAD_OP_FIXED_LENGTH_LIST_EXPECTED);
                        return symTable.semanticError;
                }
            }
        }

        BType eType = arrayType.eType;

        if (arrayType.state == BArrayState.INFERRED) {
            arrayType.size = listExprSize;
            arrayType.state = BArrayState.CLOSED;
        } else if (arrayType.state != BArrayState.OPEN && arrayType.size != listExprSize) {
            if (arrayType.size < listExprSize) {
                dlog.error(listConstructor.pos, DiagnosticErrorCode.MISMATCHING_ARRAY_LITERAL_VALUES, arrayType.size,
                        listExprSize);
                return symTable.semanticError;
            }

            if (!types.hasFillerValue(eType)) {
                dlog.error(listConstructor.pos, DiagnosticErrorCode.INVALID_LIST_CONSTRUCTOR_ELEMENT_TYPE,
                        data.expType);
                return symTable.semanticError;
            }
        }

        boolean errored = false;
        for (BLangExpression expr : listConstructor.exprs) {
            if (expr.getKind() != NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                errored |= exprIncompatible(eType, expr, data);
                continue;
            }

            BLangExpression spreadOpExpr = ((BLangListConstructorSpreadOpExpr) expr).expr;
            BType spreadOpType = checkExpr(spreadOpExpr, data);
            BType spreadOpReferredType = Types.getReferredType(spreadOpType);

            switch (spreadOpReferredType.tag) {
                case TypeTags.ARRAY:
                    BType spreadOpeType = ((BArrayType) spreadOpReferredType).eType;
                    if (types.typeIncompatible(spreadOpExpr.pos, spreadOpeType, eType)) {
                        return symTable.semanticError;
                    }
                    break;
                case TypeTags.TUPLE:
                    BTupleType spreadOpTuple = (BTupleType) spreadOpReferredType;
                    List<BType> tupleTypes = spreadOpTuple.getTupleTypes();
                    for (BType tupleMemberType : tupleTypes) {
                        if (types.typeIncompatible(spreadOpExpr.pos, tupleMemberType, eType)) {
                            return symTable.semanticError;
                        }
                    }

                    if (!types.isFixedLengthTuple(spreadOpTuple)) {
                        if (types.typeIncompatible(spreadOpExpr.pos, spreadOpTuple.restType, eType)) {
                            return symTable.semanticError;
                        }
                    }
                    break;
                default:
                    dlog.error(spreadOpExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES_LIST_SPREAD_OP, spreadOpType);
                    return symTable.semanticError;
            }
        }

        return errored ? symTable.semanticError : arrayType;
    }

    private BType checkTupleType(BLangListConstructorExpr listConstructor, BTupleType tupleType, AnalyzerData data) {
        List<BLangExpression> exprs = listConstructor.exprs;
        List<BTupleMember> members = tupleType.getMembers();
        int memberTypeSize = members.size();
        BType restType = tupleType.restType;

        if (types.isFixedLengthTuple(tupleType)) {
            int listExprSize = 0;
            for (BLangExpression expr : exprs) {
                if (expr.getKind() != NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                    listExprSize++;
                    continue;
                }

                BLangExpression spreadOpExpr = ((BLangListConstructorSpreadOpExpr) expr).expr;
                BType spreadOpType = checkExpr(spreadOpExpr, data);
                spreadOpType = Types.getReferredType(spreadOpType);

                switch (spreadOpType.tag) {
                    case TypeTags.ARRAY:
                        int arraySize = ((BArrayType) spreadOpType).size;
                        if (arraySize >= 0) {
                            listExprSize += arraySize;
                            continue;
                        }

                        dlog.error(spreadOpExpr.pos, DiagnosticErrorCode.INVALID_SPREAD_OP_FIXED_LENGTH_LIST_EXPECTED);
                        return symTable.semanticError;
                    case TypeTags.TUPLE:
                        BTupleType tType = (BTupleType) spreadOpType;
                        if (types.isFixedLengthTuple(tType)) {
                            listExprSize += tType.getMembers().size();
                            continue;
                        }

                        dlog.error(spreadOpExpr.pos, DiagnosticErrorCode.INVALID_SPREAD_OP_FIXED_LENGTH_LIST_EXPECTED);
                        return symTable.semanticError;
                }
            }

            if (listExprSize < memberTypeSize) {
                for (int i = listExprSize; i < memberTypeSize; i++) {
                    // Skip filler values for resourceAccessPathSegments
                    if (data.isResourceAccessPathSegments || !types.hasFillerValue(members.get(i).type)) {
                        dlog.error(listConstructor.pos, DiagnosticErrorCode.INVALID_LIST_CONSTRUCTOR_ELEMENT_TYPE,
                                members.get(i));
                        return symTable.semanticError;
                    }
                }
            } else if (listExprSize > memberTypeSize) {
                dlog.error(listConstructor.pos, DiagnosticErrorCode.TUPLE_AND_EXPRESSION_SIZE_DOES_NOT_MATCH);
                return symTable.semanticError;
            }
        }

        boolean errored = false;
        int nonRestTypeIndex = 0;

        for (BLangExpression expr : exprs) {
            int remainNonRestCount = memberTypeSize - nonRestTypeIndex;

            if (expr.getKind() != NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                if (remainNonRestCount > 0) {
                    errored |= exprIncompatible(members.get(nonRestTypeIndex).type, expr, data);
                    nonRestTypeIndex++;
                } else {
                    errored |= exprIncompatible(restType, expr, data);
                }
                continue;
            }

            BLangExpression spreadOpExpr = ((BLangListConstructorSpreadOpExpr) expr).expr;
            BType spreadOpType = checkExpr(spreadOpExpr, data);
            BType spreadOpReferredType = Types.getReferredType(spreadOpType);

            switch (spreadOpReferredType.tag) {
                case TypeTags.ARRAY:
                    BArrayType spreadOpArray = (BArrayType) spreadOpReferredType;
                    if (spreadOpArray.state == BArrayState.CLOSED) {
                        for (int i = 0; i < spreadOpArray.size && nonRestTypeIndex < memberTypeSize;
                             i++, nonRestTypeIndex++) {
                            if (types.typeIncompatible(spreadOpExpr.pos, spreadOpArray.eType,
                                    members.get(nonRestTypeIndex).type)) {
                                return symTable.semanticError;
                            }
                        }

                        if (remainNonRestCount < spreadOpArray.size) {
                            if (types.typeIncompatible(spreadOpExpr.pos, spreadOpArray.eType, restType)) {
                                return symTable.semanticError;
                            }
                        }
                        continue;
                    }

                    if (remainNonRestCount > 0) {
                        dlog.error(spreadOpExpr.pos, DiagnosticErrorCode.INVALID_SPREAD_OP_FIXED_MEMBER_EXPECTED,
                                members.get(nonRestTypeIndex));
                        return symTable.semanticError;
                    }

                    if (types.typeIncompatible(spreadOpExpr.pos, spreadOpArray.eType, restType)) {
                        return symTable.semanticError;
                    }
                    break;
                case TypeTags.TUPLE:
                    BTupleType spreadOpTuple = (BTupleType) spreadOpReferredType;
                    List<BType> tupleMemberTypes = spreadOpTuple.getTupleTypes();
                    int spreadOpMemberTypeSize = tupleMemberTypes.size();

                    if (types.isFixedLengthTuple(spreadOpTuple)) {
                        for (int i = 0; i < spreadOpMemberTypeSize && nonRestTypeIndex < memberTypeSize;
                             i++, nonRestTypeIndex++) {
                            if (types.typeIncompatible(spreadOpExpr.pos, tupleMemberTypes.get(i),
                                    members.get(nonRestTypeIndex).type)) {
                                return symTable.semanticError;
                            }
                        }

                        for (int i = remainNonRestCount; i < spreadOpMemberTypeSize; i++) {
                            if (types.typeIncompatible(spreadOpExpr.pos, tupleMemberTypes.get(i),
                                    restType)) {
                                return symTable.semanticError;
                            }
                        }
                        continue;
                    }

                    if (spreadOpMemberTypeSize < remainNonRestCount) {
                        dlog.error(spreadOpExpr.pos, DiagnosticErrorCode.INVALID_SPREAD_OP_FIXED_MEMBER_EXPECTED,
                                members.get(nonRestTypeIndex + spreadOpMemberTypeSize));
                        return symTable.semanticError;
                    }

                    for (int i = 0; nonRestTypeIndex < memberTypeSize; i++, nonRestTypeIndex++) {
                        if (types.typeIncompatible(spreadOpExpr.pos, tupleMemberTypes.get(i),
                                members.get(nonRestTypeIndex).type)) {
                            return symTable.semanticError;
                        }
                    }

                    for (int i = nonRestTypeIndex; i < spreadOpMemberTypeSize; i++) {
                        if (types.typeIncompatible(spreadOpExpr.pos, tupleMemberTypes.get(i),
                                restType)) {
                            return symTable.semanticError;
                        }
                    }

                    if (types.typeIncompatible(spreadOpExpr.pos, spreadOpTuple.restType, restType)) {
                        return symTable.semanticError;
                    }
                    break;
                default:
                    dlog.error(spreadOpExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES_LIST_SPREAD_OP, spreadOpType);
                    return symTable.semanticError;
            }
        }

        while (nonRestTypeIndex < memberTypeSize) {

            // Skip filler values for resourceAccessPathSegments
            if (data.isResourceAccessPathSegments || !types.hasFillerValue(members.get(nonRestTypeIndex).type)) {
                dlog.error(listConstructor.pos, DiagnosticErrorCode.INVALID_LIST_CONSTRUCTOR_ELEMENT_TYPE,
                        members.get(nonRestTypeIndex));
                return symTable.semanticError;
            }
            nonRestTypeIndex++;
        }

        return errored ? symTable.semanticError : tupleType;
    }

    private BType checkReadOnlyListType(BLangListConstructorExpr listConstructor, AnalyzerData data) {
        if (!data.commonAnalyzerData.nonErrorLoggingCheck) {
            BType inferredType = getInferredTupleType(listConstructor, symTable.readonlyType, data);

            if (inferredType == symTable.semanticError) {
                return symTable.semanticError;
            }
            return types.checkType(listConstructor, inferredType, symTable.readonlyType);
        }

        for (BLangExpression expr : listConstructor.exprs) {
            if (expr.getKind() == NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                expr = ((BLangListConstructorSpreadOpExpr) expr).expr;
            }

            if (exprIncompatible(symTable.readonlyType, expr, data)) {
                return symTable.semanticError;
            }
        }

        return symTable.readonlyType;
    }

    private boolean exprIncompatible(BType eType, BLangExpression expr, AnalyzerData data) {
        if (expr.typeChecked) {
            return expr.getBType() == symTable.semanticError;
        }

        BLangExpression exprToCheck = expr;

        if (data.commonAnalyzerData.nonErrorLoggingCheck) {
            expr.cloneAttempt++;
            exprToCheck = nodeCloner.cloneNode(expr);
        }

        return checkExpr(exprToCheck, eType, data) == symTable.semanticError;
    }

    private InferredTupleDetails checkExprList(List<BLangExpression> exprs, AnalyzerData data) {
        return checkExprList(exprs, symTable.noType, data);
    }

    private InferredTupleDetails checkExprList(List<BLangExpression> exprs, BType expType, AnalyzerData data) {
        InferredTupleDetails inferredTupleDetails = new InferredTupleDetails();
        SymbolEnv prevEnv = data.env;
        BType preExpType = data.expType;
        data.expType = expType;
        for (BLangExpression e : exprs) {
            if (e.getKind() == NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                BLangExpression spreadOpExpr = ((BLangListConstructorSpreadOpExpr) e).expr;
                BType spreadOpExprType = checkExpr(spreadOpExpr, expType, data);
                updateInferredTupleDetailsFromSpreadMember(e.pos, spreadOpExprType, inferredTupleDetails);
                continue;
            }

            checkExpr(e, expType, data);
            if (inferredTupleDetails.restMemberTypes.isEmpty()) {
                inferredTupleDetails.fixedMemberTypes.add(data.resultType);
            } else {
                inferredTupleDetails.restMemberTypes.add(data.resultType);
            }
        }
        data.env = prevEnv;
        data.expType = preExpType;
        return inferredTupleDetails;
    }

    private static class InferredTupleDetails {
        List<BType> fixedMemberTypes = new ArrayList<>();
        List<BType> restMemberTypes = new ArrayList<>();
    }

    private BType getInferredTupleType(BLangListConstructorExpr listConstructor, BType expType, AnalyzerData data) {

        InferredTupleDetails inferredTupleDetails = checkExprList(listConstructor.exprs, expType, data);
        List<BType> fixedMemberTypes = inferredTupleDetails.fixedMemberTypes;
        List<BType> restMemberTypes = inferredTupleDetails.restMemberTypes;

        for (BType memType : fixedMemberTypes) {
            if (memType == symTable.semanticError) {
                return symTable.semanticError;
            }
        }

        for (BType memType : restMemberTypes) {
            if (memType == symTable.semanticError) {
                return symTable.semanticError;
            }
        }
        List<BTupleMember> members = new ArrayList<>();
        fixedMemberTypes.forEach(memberType -> members.add(new BTupleMember(memberType,
                new BVarSymbol(memberType.flags, null, null, memberType, null, null, null))));
        BTupleType tupleType = new BTupleType(members);
        if (!restMemberTypes.isEmpty()) {
            tupleType.restType = getRepresentativeBroadType(restMemberTypes);
        }

        if (expType.tag != TypeTags.READONLY) {
            return tupleType;
        }

        tupleType.flags |= Flags.READONLY;
        return tupleType;
    }


    public void visit(BLangRecordLiteral recordLiteral, AnalyzerData data) {
        BType expType = data.expType;
        int expTypeTag = Types.getReferredType(expType).tag;

        if (expTypeTag == TypeTags.NONE || expTypeTag == TypeTags.READONLY) {
            expType = defineInferredRecordType(recordLiteral, expType, data);
        } else if (expTypeTag == TypeTags.OBJECT) {
            dlog.error(recordLiteral.pos, DiagnosticErrorCode.INVALID_RECORD_LITERAL, expType);
            data.resultType = symTable.semanticError;
            return;
        }

        data.resultType = getEffectiveMappingType(recordLiteral,
                                             checkMappingConstructorCompatibility(expType, recordLiteral, data), data);
    }

    private BType getEffectiveMappingType(BLangRecordLiteral recordLiteral, BType applicableMappingType,
                                          AnalyzerData data) {
        BType refType = Types.getReferredType(applicableMappingType);
        if (applicableMappingType == symTable.semanticError ||
                (refType.tag == TypeTags.RECORD && Symbols.isFlagOn(applicableMappingType.flags,
                                                                                  Flags.READONLY))) {
            return applicableMappingType;
        }

        Map<String, RecordLiteralNode.RecordField> readOnlyFields = new LinkedHashMap<>();
        LinkedHashMap<String, BField> applicableTypeFields =
                refType.tag == TypeTags.RECORD ? ((BRecordType) refType).fields :
                        new LinkedHashMap<>();

        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            if (field.getKind() == NodeKind.RECORD_LITERAL_SPREAD_OP) {
                continue;
            }

            String name;
            if (field.isKeyValueField()) {
                BLangRecordKeyValueField keyValueField = (BLangRecordKeyValueField) field;

                if (!keyValueField.readonly) {
                    continue;
                }

                BLangExpression keyExpr = keyValueField.key.expr;
                if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    name = ((BLangSimpleVarRef) keyExpr).variableName.value;
                } else {
                    name = (String) ((BLangLiteral) keyExpr).value;
                }
            } else {
                BLangRecordVarNameField varNameField = (BLangRecordVarNameField) field;

                if (!varNameField.readonly) {
                    continue;
                }
                name = varNameField.variableName.value;
            }

            if (applicableTypeFields.containsKey(name) &&
                    Symbols.isFlagOn(applicableTypeFields.get(name).symbol.flags, Flags.READONLY)) {
                continue;
            }

            readOnlyFields.put(name, field);
        }

        if (readOnlyFields.isEmpty()) {
            return applicableMappingType;
        }

        PackageID pkgID = data.env.enclPkg.symbol.pkgID;
        Location pos = recordLiteral.pos;
        BRecordTypeSymbol recordSymbol = createRecordTypeSymbol(pkgID, pos, VIRTUAL, data);

        LinkedHashMap<String, BField> newFields = new LinkedHashMap<>();

        for (Map.Entry<String, RecordLiteralNode.RecordField> readOnlyEntry : readOnlyFields.entrySet()) {
            RecordLiteralNode.RecordField field = readOnlyEntry.getValue();

            String key = readOnlyEntry.getKey();
            Name fieldName = names.fromString(key);

            BType readOnlyFieldType;
            if (field.isKeyValueField()) {
                readOnlyFieldType = ((BLangRecordKeyValueField) field).valueExpr.getBType();
            } else {
                // Has to be a varname field.
                readOnlyFieldType = ((BLangRecordVarNameField) field).getBType();
            }

            BVarSymbol fieldSymbol = new BVarSymbol(Flags.asMask(new HashSet<Flag>() {{
                add(Flag.REQUIRED);
                add(Flag.READONLY);
            }}), fieldName, pkgID, readOnlyFieldType, recordSymbol,
                                                    ((BLangNode) field).pos, VIRTUAL);
            newFields.put(key, new BField(fieldName, null, fieldSymbol));
            recordSymbol.scope.define(fieldName, fieldSymbol);
        }

        BRecordType recordType = new BRecordType(recordSymbol, recordSymbol.flags);
        if (refType.tag == TypeTags.MAP) {
            recordType.sealed = false;
            recordType.restFieldType = ((BMapType) refType).constraint;
        } else {
            BRecordType applicableRecordType = (BRecordType) refType;
            boolean allReadOnlyFields = true;

            for (Map.Entry<String, BField> origEntry : applicableRecordType.fields.entrySet()) {
                String fieldName = origEntry.getKey();
                BField field = origEntry.getValue();

                if (readOnlyFields.containsKey(fieldName)) {
                    // Already defined.
                    continue;
                }

                BVarSymbol origFieldSymbol = field.symbol;
                long origFieldFlags = origFieldSymbol.flags;

                if (allReadOnlyFields && !Symbols.isFlagOn(origFieldFlags, Flags.READONLY)) {
                    allReadOnlyFields = false;
                }

                BVarSymbol fieldSymbol = new BVarSymbol(origFieldFlags, field.name, pkgID,
                                                        origFieldSymbol.type, recordSymbol, field.pos, VIRTUAL);
                newFields.put(fieldName, new BField(field.name, null, fieldSymbol));
                recordSymbol.scope.define(field.name, fieldSymbol);
            }

            recordType.sealed = applicableRecordType.sealed;
            recordType.restFieldType = applicableRecordType.restFieldType;

            if (recordType.sealed && allReadOnlyFields) {
                recordType.flags |= Flags.READONLY;
                recordType.tsymbol.flags |= Flags.READONLY;
            }

        }

        recordType.fields = newFields;
        recordSymbol.type = recordType;
        recordType.tsymbol = recordSymbol;

        BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(recordType, pkgID, symTable,
                                                                                       pos);
        recordTypeNode.initFunction = TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, data.env,
                                                                                           names, symTable);
        TypeDefBuilderHelper.createTypeDefinitionForTSymbol(recordType, recordSymbol, recordTypeNode, data.env);

        if (refType.tag == TypeTags.RECORD) {
            BRecordType applicableRecordType = (BRecordType) refType;
            BTypeSymbol applicableRecordTypeSymbol = applicableRecordType.tsymbol;
            BLangUserDefinedType origTypeRef = new BLangUserDefinedType(
                    ASTBuilderUtil.createIdentifier(
                            pos,
                            TypeDefBuilderHelper.getPackageAlias(data.env, pos.lineRange().filePath(),
                                                                 applicableRecordTypeSymbol.pkgID)),
                    ASTBuilderUtil.createIdentifier(pos, applicableRecordTypeSymbol.name.value));
            origTypeRef.pos = pos;
            origTypeRef.setBType(applicableRecordType);
            recordTypeNode.typeRefs.add(origTypeRef);
        } else if (refType.tag == TypeTags.MAP) {
            recordLiteral.expectedType = applicableMappingType;
        }

        return recordType;
    }

    private BType checkMappingConstructorCompatibility(BType bType, BLangRecordLiteral mappingConstructor,
                                                       AnalyzerData data) {
        int tag = bType.tag;
        if (tag == TypeTags.UNION) {
            boolean prevNonErrorLoggingCheck = data.commonAnalyzerData.nonErrorLoggingCheck;
            data.commonAnalyzerData.nonErrorLoggingCheck = true;
            int errorCount = this.dlog.errorCount();
            this.dlog.mute();

            List<BType> compatibleTypes = new ArrayList<>();
            boolean erroredExpType = false;
            for (BType memberType : ((BUnionType) bType).getMemberTypes()) {
                if (memberType == symTable.semanticError) {
                    if (!erroredExpType) {
                        erroredExpType = true;
                    }
                    continue;
                }

                BType listCompatibleMemType = getMappingConstructorCompatibleNonUnionType(memberType, data);
                if (listCompatibleMemType == symTable.semanticError) {
                    continue;
                }

                dlog.resetErrorCount();
                BType memCompatibiltyType = checkMappingConstructorCompatibility(listCompatibleMemType,
                                                                                 mappingConstructor, data);

                if (memCompatibiltyType != symTable.semanticError && dlog.errorCount() == 0 &&
                        isUniqueType(compatibleTypes, memCompatibiltyType)) {
                    compatibleTypes.add(memCompatibiltyType);
                }
            }

            data.commonAnalyzerData.nonErrorLoggingCheck = prevNonErrorLoggingCheck;
            dlog.setErrorCount(errorCount);
            if (!prevNonErrorLoggingCheck) {
                this.dlog.unmute();
            }

            if (compatibleTypes.isEmpty()) {
                if (!erroredExpType) {
                    reportIncompatibleMappingConstructorError(mappingConstructor, bType, data);
                }
                validateSpecifiedFields(mappingConstructor, symTable.semanticError, data);
                return symTable.semanticError;
            } else if (compatibleTypes.size() != 1) {
                dlog.error(mappingConstructor.pos, DiagnosticErrorCode.AMBIGUOUS_TYPES, bType);
                validateSpecifiedFields(mappingConstructor, symTable.semanticError, data);
                return symTable.semanticError;
            }

            return checkMappingConstructorCompatibility(compatibleTypes.get(0), mappingConstructor, data);
        }

        if (tag == TypeTags.TYPEREFDESC) {
            BType refType = Types.getReferredType(bType);
            BType compatibleType = checkMappingConstructorCompatibility(refType, mappingConstructor, data);
            return compatibleType == refType ? bType : compatibleType;
        }

        if (tag == TypeTags.INTERSECTION) {
            return checkMappingConstructorCompatibility(((BIntersectionType) bType).effectiveType, mappingConstructor,
                                                        data);
        }

        BType possibleType = getMappingConstructorCompatibleNonUnionType(bType, data);

        switch (possibleType.tag) {
            case TypeTags.MAP:
                return validateSpecifiedFields(mappingConstructor, possibleType, data) ? possibleType :
                        symTable.semanticError;
            case TypeTags.RECORD:
                boolean isSpecifiedFieldsValid = validateSpecifiedFields(mappingConstructor, possibleType, data);

                boolean hasAllRequiredFields = validateRequiredFields((BRecordType) possibleType,
                                                                      mappingConstructor.fields,
                                                                      mappingConstructor.pos, data);

                return isSpecifiedFieldsValid && hasAllRequiredFields ? possibleType : symTable.semanticError;
            case TypeTags.READONLY:
                return checkReadOnlyMappingType(mappingConstructor, data);
        }
        reportIncompatibleMappingConstructorError(mappingConstructor, bType, data);
        validateSpecifiedFields(mappingConstructor, symTable.semanticError, data);
        return symTable.semanticError;
    }

    private BType checkReadOnlyMappingType(BLangRecordLiteral mappingConstructor, AnalyzerData data) {
        if (!data.commonAnalyzerData.nonErrorLoggingCheck) {
            BType inferredType = defineInferredRecordType(mappingConstructor, symTable.readonlyType, data);

            if (inferredType == symTable.semanticError) {
                return symTable.semanticError;
            }
            return checkMappingConstructorCompatibility(inferredType, mappingConstructor, data);
        }

        for (RecordLiteralNode.RecordField field : mappingConstructor.fields) {
            BLangExpression exprToCheck;

            if (field.isKeyValueField()) {
                exprToCheck = ((BLangRecordKeyValueField) field).valueExpr;
            } else if (field.getKind() == NodeKind.RECORD_LITERAL_SPREAD_OP) {
                exprToCheck = ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr;
            } else {
                exprToCheck = (BLangRecordVarNameField) field;
            }

            if (exprIncompatible(symTable.readonlyType, exprToCheck, data)) {
                return symTable.semanticError;
            }
        }

        return symTable.readonlyType;
    }

    private BType getMappingConstructorCompatibleNonUnionType(BType type, AnalyzerData data) {
        switch (type.tag) {
            case TypeTags.MAP:
            case TypeTags.RECORD:
            case TypeTags.READONLY:
                return type;
            case TypeTags.JSON:
                return !Symbols.isFlagOn(type.flags, Flags.READONLY) ? symTable.mapJsonType :
                        ImmutableTypeCloner.getEffectiveImmutableType(null, types, symTable.mapJsonType, data.env,
                                                                      symTable, anonymousModelHelper, names);
            case TypeTags.ANYDATA:
                return !Symbols.isFlagOn(type.flags, Flags.READONLY) ? symTable.mapAnydataType :
                        ImmutableTypeCloner.getEffectiveImmutableType(null, types, symTable.mapAnydataType,
                                data.env, symTable, anonymousModelHelper, names);
            case TypeTags.ANY:
                return !Symbols.isFlagOn(type.flags, Flags.READONLY) ? symTable.mapAllType :
                        ImmutableTypeCloner.getEffectiveImmutableType(null, types, symTable.mapAllType, data.env,
                                                                      symTable, anonymousModelHelper, names);
            case TypeTags.INTERSECTION:
                return ((BIntersectionType) type).effectiveType;
            case TypeTags.TYPEREFDESC:
                BType refType = Types.getReferredType(type);
                BType compatibleType = getMappingConstructorCompatibleNonUnionType(refType, data);
                return compatibleType == refType ? type : compatibleType;
        }
        return symTable.semanticError;
    }

    private boolean isMappingConstructorCompatibleType(BType type) {
        return Types.getReferredType(type).tag == TypeTags.RECORD
                || Types.getReferredType(type).tag == TypeTags.MAP;
    }

    private void reportIncompatibleMappingConstructorError(BLangRecordLiteral mappingConstructorExpr, BType expType,
                                                           AnalyzerData data) {
        if (expType == symTable.semanticError) {
            return;
        }

        if (expType.tag != TypeTags.UNION) {
            dlog.error(mappingConstructorExpr.pos,
                    DiagnosticErrorCode.MAPPING_CONSTRUCTOR_COMPATIBLE_TYPE_NOT_FOUND, expType);
            return;
        }

        BUnionType unionType = (BUnionType) expType;
        BType[] memberTypes = types.getAllTypes(unionType, true).toArray(new BType[0]);

        // Special case handling for `T?` where T is a record type. This is done to give more user friendly error
        // messages for this common scenario.
        if (memberTypes.length == 2) {
            BRecordType recType = null;

            if (memberTypes[0].tag == TypeTags.RECORD && memberTypes[1].tag == TypeTags.NIL) {
                recType = (BRecordType) memberTypes[0];
            } else if (memberTypes[1].tag == TypeTags.RECORD && memberTypes[0].tag == TypeTags.NIL) {
                recType = (BRecordType) memberTypes[1];
            }

            if (recType != null) {
                validateSpecifiedFields(mappingConstructorExpr, recType, data);
                validateRequiredFields(recType, mappingConstructorExpr.fields, mappingConstructorExpr.pos, data);
                return;
            }
        }

        // By this point, we know there aren't any types to which we can assign the mapping constructor. If this is
        // case where there is at least one type with which we can use mapping constructors, but this particular
        // mapping constructor is incompatible, we give an incompatible mapping constructor error.
        for (BType bType : memberTypes) {
            if (isMappingConstructorCompatibleType(bType)) {
                dlog.error(mappingConstructorExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_MAPPING_CONSTRUCTOR,
                        unionType);
                return;
            }
        }

        dlog.error(mappingConstructorExpr.pos,
                DiagnosticErrorCode.MAPPING_CONSTRUCTOR_COMPATIBLE_TYPE_NOT_FOUND, unionType);
    }

    private boolean validateSpecifiedFields(BLangRecordLiteral mappingConstructor, BType possibleType,
                                            AnalyzerData data) {
        boolean isFieldsValid = true;

        for (RecordLiteralNode.RecordField field : mappingConstructor.fields) {
            BType checkedType = checkMappingField(field, Types.getReferredType(possibleType), data);
            if (isFieldsValid && checkedType == symTable.semanticError) {
                isFieldsValid = false;
            }
        }

        return isFieldsValid;
    }

    private boolean validateRequiredFields(BRecordType type, List<RecordLiteralNode.RecordField> specifiedFields,
                                           Location pos, AnalyzerData data) {
        HashSet<String> specFieldNames = getFieldNames(specifiedFields, data);
        boolean hasAllRequiredFields = true;

        for (BField field : type.fields.values()) {
            String fieldName = field.name.value;
            if (!specFieldNames.contains(fieldName) && Symbols.isFlagOn(field.symbol.flags, Flags.REQUIRED)
                    && !types.isNeverTypeOrStructureTypeWithARequiredNeverMember(field.type)) {
                // Check if `field` is explicitly assigned a value in the record literal
                // If a required field is missing, it's a compile error
                dlog.error(pos, DiagnosticErrorCode.MISSING_REQUIRED_RECORD_FIELD, field.name);
                if (hasAllRequiredFields) {
                    hasAllRequiredFields = false;
                }
            }
        }
        return hasAllRequiredFields;
    }

    private HashSet<String> getFieldNames(List<RecordLiteralNode.RecordField> specifiedFields, AnalyzerData data) {
        HashSet<String> fieldNames = new HashSet<>();

        for (RecordLiteralNode.RecordField specifiedField : specifiedFields) {
            if (specifiedField.isKeyValueField()) {
                String name = getKeyValueFieldName((BLangRecordKeyValueField) specifiedField);
                if (name == null) {
                    continue; // computed key
                }

                fieldNames.add(name);
            } else if (specifiedField.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                fieldNames.add(getVarNameFieldName((BLangRecordVarNameField) specifiedField));
            } else {
                fieldNames.addAll(getSpreadOpFieldRequiredFieldNames(
                        (BLangRecordLiteral.BLangRecordSpreadOperatorField) specifiedField, data));
            }
        }

        return fieldNames;
    }

    private String getKeyValueFieldName(BLangRecordKeyValueField field) {
        BLangRecordKey key = field.key;
        if (key.computedKey) {
            return null;
        }

        BLangExpression keyExpr = key.expr;

        if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            return ((BLangSimpleVarRef) keyExpr).variableName.value;
        } else if (keyExpr.getKind() == NodeKind.LITERAL) {
            return (String) ((BLangLiteral) keyExpr).value;
        }
        return null;
    }

    private String getVarNameFieldName(BLangRecordVarNameField field) {
        return field.variableName.value;
    }

    private List<String> getSpreadOpFieldRequiredFieldNames(BLangRecordLiteral.BLangRecordSpreadOperatorField field,
                                                            AnalyzerData data) {
        BType spreadType = Types.getReferredType(checkExpr(field.expr, data));

        if (spreadType.tag != TypeTags.RECORD) {
            return Collections.emptyList();
        }

        List<String> fieldNames = new ArrayList<>();
        for (BField bField : ((BRecordType) spreadType).getFields().values()) {
            if (!Symbols.isOptional(bField.symbol)) {
                fieldNames.add(bField.name.value);
            }
        }
        return fieldNames;
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr, AnalyzerData data) {
        if (workerFlushExpr.workerIdentifier != null) {
            String workerName = workerFlushExpr.workerIdentifier.getValue();
            if (!this.workerExists(data.env, workerName)) {
                this.dlog.error(workerFlushExpr.pos, DiagnosticErrorCode.UNDEFINED_WORKER, workerName);
            } else {
                BSymbol symbol = symResolver.lookupSymbolInMainSpace(data.env, names.fromString(workerName));
                if (symbol != symTable.notFoundSymbol) {
                    workerFlushExpr.workerSymbol = symbol;
                }
            }
        }
        BType actualType = BUnionType.create(null, symTable.errorType, symTable.nilType);
        data.resultType = types.checkType(workerFlushExpr, actualType, data.expType);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr, AnalyzerData data) {
        BSymbol symbol = symResolver.lookupSymbolInMainSpace(data.env, names.fromIdNode(syncSendExpr.workerIdentifier));

        if (symTable.notFoundSymbol.equals(symbol)) {
            syncSendExpr.workerType = symTable.semanticError;
        } else {
            syncSendExpr.workerType = symbol.type;
            syncSendExpr.workerSymbol = symbol;
        }

        // TODO Need to remove this cached env
        syncSendExpr.env = data.env;
        checkExpr(syncSendExpr.expr, data);

        // Validate if the send expression type is cloneableType
        if (!types.isAssignable(syncSendExpr.expr.getBType(), symTable.cloneableType)) {
            this.dlog.error(syncSendExpr.pos, DiagnosticErrorCode.INVALID_TYPE_FOR_SEND,
                            syncSendExpr.expr.getBType());
        }

        String workerName = syncSendExpr.workerIdentifier.getValue();
        if (!this.workerExists(data.env, workerName)) {
            this.dlog.error(syncSendExpr.pos, DiagnosticErrorCode.UNDEFINED_WORKER, workerName);
        }

        syncSendExpr.expectedType = data.expType;

        // Type checking against the matching receive is done during code analysis.
        // When the expected type is noType, set the result type as nil to avoid variable assignment is required errors.
        data.resultType = data.expType == symTable.noType ? symTable.nilType : data.expType;
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveExpr, AnalyzerData data) {
        BSymbol symbol =
                symResolver.lookupSymbolInMainSpace(data.env, names.fromIdNode(workerReceiveExpr.workerIdentifier));

        // TODO Need to remove this cached env
        workerReceiveExpr.env = data.env;

        if (symTable.notFoundSymbol.equals(symbol)) {
            workerReceiveExpr.workerType = symTable.semanticError;
        } else {
            workerReceiveExpr.workerType = symbol.type;
            workerReceiveExpr.workerSymbol = symbol;
        }
        // The receive expression cannot be assigned to var, since we cannot infer the type.
        if (symTable.noType == data.expType) {
            this.dlog.error(workerReceiveExpr.pos, DiagnosticErrorCode.INVALID_USAGE_OF_RECEIVE_EXPRESSION);
        }
        // We cannot predict the type of the receive expression as it depends on the type of the data sent by the other
        // worker/channel. Since receive is an expression now we infer the type of it from the lhs of the statement.
        workerReceiveExpr.setBType(data.expType);
        data.resultType = data.expType;
    }

    private boolean workerExists(SymbolEnv env, String workerName) {
        //TODO: move this method to CodeAnalyzer
        if (workerName.equals(DEFAULT_WORKER_NAME)) {
           return true;
        }
        BSymbol symbol = this.symResolver.lookupSymbolInMainSpace(env, new Name(workerName));
        return symbol != this.symTable.notFoundSymbol &&
               symbol.type.tag == TypeTags.FUTURE &&
               ((BFutureType) symbol.type).workerDerivative;
    }

    @Override
    public void visit(BLangConstRef constRef, AnalyzerData data) {
        constRef.symbol = symResolver.lookupMainSpaceSymbolInPackage(constRef.pos, data.env,
                names.fromIdNode(constRef.pkgAlias), names.fromIdNode(constRef.variableName));

        types.setImplicitCastExpr(constRef, constRef.getBType(), data.expType);
        data.resultType = constRef.getBType();
    }

    public void visit(BLangSimpleVarRef varRefExpr, AnalyzerData data) {
        // Set error type as the actual type.
        BType actualType = symTable.semanticError;

        BLangIdentifier identifier = varRefExpr.variableName;
        Name varName = names.fromIdNode(identifier);
        if (varName == Names.IGNORE) {
            varRefExpr.setBType(this.symTable.anyType);

            // If the variable name is a wildcard('_'), the symbol should be ignorable.
            varRefExpr.symbol = new BVarSymbol(0, true, varName,
                                               names.originalNameFromIdNode(identifier),
                    data.env.enclPkg.symbol.pkgID, varRefExpr.getBType(), data.env.scope.owner,
                                               varRefExpr.pos, VIRTUAL);

            data.resultType = varRefExpr.getBType();
            return;
        }

        Name compUnitName = getCurrentCompUnit(varRefExpr);
        BSymbol pkgSymbol = symResolver.resolvePrefixSymbol(data.env, names.fromIdNode(varRefExpr.pkgAlias),
                                                            compUnitName);
        varRefExpr.pkgSymbol = pkgSymbol;
        if (pkgSymbol == symTable.notFoundSymbol) {
            varRefExpr.symbol = symTable.notFoundSymbol;
            dlog.error(varRefExpr.pos, DiagnosticErrorCode.UNDEFINED_MODULE, varRefExpr.pkgAlias);
        }

        if (pkgSymbol.tag == SymTag.XMLNS) {
            actualType = symTable.stringType;
        } else if (pkgSymbol != symTable.notFoundSymbol) {
            BSymbol symbol = symResolver.lookupMainSpaceSymbolInPackage(varRefExpr.pos, data.env,
                    names.fromIdNode(varRefExpr.pkgAlias), varName);
            // if no symbol, check same for object attached function
            BLangType enclType = data.env.enclType;
            if (symbol == symTable.notFoundSymbol && enclType != null && enclType.getBType().tsymbol.scope != null) {
                Name objFuncName = names.fromString(Symbols
                        .getAttachedFuncSymbolName(enclType.getBType().tsymbol.name.value, varName.value));
                symbol = symResolver.resolveStructField(varRefExpr.pos, data.env, objFuncName,
                        enclType.getBType().tsymbol);
            }

            // TODO: call to isInLocallyDefinedRecord() is a temporary fix done to disallow local var references in
            //  locally defined record type defs. This check should be removed once local var referencing is supported.
            if (((symbol.tag & SymTag.VARIABLE) == SymTag.VARIABLE)) {
                BVarSymbol varSym = (BVarSymbol) symbol;
                checkSelfReferences(varRefExpr.pos, data.env, varSym);
                varRefExpr.symbol = varSym;
                actualType = varSym.type;
                markAndRegisterClosureVariable(symbol, varRefExpr.pos, data.env, data);
            } else if ((symbol.tag & SymTag.TYPE_DEF) == SymTag.TYPE_DEF) {
                actualType = symbol.type.tag == TypeTags.TYPEDESC ? symbol.type : new BTypedescType(symbol.type, null);
                varRefExpr.symbol = symbol;
            } else if ((symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT) {
                BConstantSymbol constSymbol = (BConstantSymbol) symbol;
                varRefExpr.symbol = constSymbol;
                BType symbolType = symbol.type;
                BType expectedType = Types.getReferredType(data.expType);
                if (symbolType != symTable.noType && expectedType.tag == TypeTags.FINITE ||
                        (expectedType.tag == TypeTags.UNION && types.getAllTypes(expectedType, true).stream()
                                .anyMatch(memType -> memType.tag == TypeTags.FINITE &&
                                        types.isAssignable(symbolType, memType)))) {
                    actualType = symbolType;
                } else {
                    actualType = constSymbol.literalType;
                }

                // If the constant is on the LHS, modifications are not allowed.
                // E.g. m.k = "10"; // where `m` is a constant.
                if (varRefExpr.isLValue || varRefExpr.isCompoundAssignmentLValue) {
                    actualType = symTable.semanticError;
                    dlog.error(varRefExpr.pos, DiagnosticErrorCode.CANNOT_UPDATE_CONSTANT_VALUE);
                }
            } else {
                varRefExpr.symbol = symbol; // Set notFoundSymbol
                logUndefinedSymbolError(varRefExpr.pos, varName.value);
            }
        }

        // Check type compatibility
        if (data.expType.tag == TypeTags.ARRAY && isArrayOpenSealedType((BArrayType) data.expType)) {
            dlog.error(varRefExpr.pos, DiagnosticErrorCode.CANNOT_INFER_SIZE_ARRAY_SIZE_FROM_THE_CONTEXT);
            data.resultType = symTable.semanticError;
            return;

        }

        data.resultType = types.checkType(varRefExpr, actualType, data.expType);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr, AnalyzerData data) {
        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();

        String recordName = this.anonymousModelHelper.getNextAnonymousTypeKey(data.env.enclPkg.symbol.pkgID);
        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.ANONYMOUS, names.fromString(recordName),
                data.env.enclPkg.symbol.pkgID, null, data.env.scope.owner,
                                                                         varRefExpr.pos, SOURCE);
        symbolEnter.defineSymbol(varRefExpr.pos, recordSymbol, data.env);

        boolean unresolvedReference = false;
        for (BLangRecordVarRef.BLangRecordVarRefKeyValue recordRefField : varRefExpr.recordRefFields) {
            BLangVariableReference bLangVarReference = (BLangVariableReference) recordRefField.variableReference;
            bLangVarReference.isLValue = true;
            checkExpr(recordRefField.variableReference, data);
            if (bLangVarReference.symbol == null || bLangVarReference.symbol == symTable.notFoundSymbol ||
                    !isValidVariableReference(recordRefField.variableReference)) {
                unresolvedReference = true;
                continue;
            }
            BVarSymbol bVarSymbol = (BVarSymbol) bLangVarReference.symbol;
            BField field = new BField(names.fromIdNode(recordRefField.variableName), varRefExpr.pos,
                                      new BVarSymbol(0, names.fromIdNode(recordRefField.variableName),
                                                     names.originalNameFromIdNode(recordRefField.variableName),
                                              data.env.enclPkg.symbol.pkgID, bVarSymbol.type, recordSymbol,
                                                     varRefExpr.pos, SOURCE));
            fields.put(field.name.value, field);
        }

        BLangExpression restParam = (BLangExpression) varRefExpr.restParam;
        if (restParam != null) {
            checkExpr(restParam, data);
            unresolvedReference = !isValidVariableReference(restParam);
        }

        if (unresolvedReference) {
            data.resultType = symTable.semanticError;
            return;
        }

        BRecordType bRecordType = new BRecordType(recordSymbol);
        bRecordType.fields = fields;
        recordSymbol.type = bRecordType;
        varRefExpr.symbol = new BVarSymbol(0, recordSymbol.name, recordSymbol.getOriginalName(),
                data.env.enclPkg.symbol.pkgID, bRecordType, data.env.scope.owner, varRefExpr.pos,
                                           SOURCE);

        if (restParam == null) {
            bRecordType.sealed = true;
            bRecordType.restFieldType = symTable.noType;
        } else if (restParam.getBType() == symTable.semanticError) {
            bRecordType.restFieldType = symTable.mapType;
        } else {
            // Rest variable type of Record ref (record destructuring assignment) is a record where T is the broad
            // type of all fields that are not specified in the destructuring pattern. Here we set the rest type of
            // record type to T.
            BType restFieldType;
            if (restParam.getBType().tag == TypeTags.RECORD) {
                restFieldType = ((BRecordType) restParam.getBType()).restFieldType;
            } else if (restParam.getBType().tag == TypeTags.MAP) {
                restFieldType = ((BMapType) restParam.getBType()).constraint;
            } else {
                restFieldType = restParam.getBType();
            }
            bRecordType.restFieldType = restFieldType;
        }

        data.resultType = bRecordType;
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr, AnalyzerData data) {
        if (varRefExpr.typeNode != null) {
            BType bType = symResolver.resolveTypeNode(varRefExpr.typeNode, data.env);
            varRefExpr.setBType(bType);
            checkIndirectErrorVarRef(varRefExpr, data);
            data.resultType = bType;
            return;
        }

        if (varRefExpr.message != null) {
            varRefExpr.message.isLValue = true;
            checkExpr(varRefExpr.message, data);
            if (!types.isAssignable(symTable.stringType, varRefExpr.message.getBType())) {
                dlog.error(varRefExpr.message.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, symTable.stringType,
                           varRefExpr.message.getBType());
            }
        }

        if (varRefExpr.cause != null) {
            varRefExpr.cause.isLValue = true;
            checkExpr(varRefExpr.cause, data);
            if (!types.isAssignable(symTable.errorOrNilType, varRefExpr.cause.getBType())) {
                dlog.error(varRefExpr.cause.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, symTable.errorOrNilType,
                           varRefExpr.cause.getBType());
            }
        }

        boolean unresolvedReference = false;

        for (BLangNamedArgsExpression detailItem : varRefExpr.detail) {
            BLangVariableReference refItem = (BLangVariableReference) detailItem.expr;
            refItem.isLValue = true;
            checkExpr(refItem, data);

            if (types.isFunctionVarRef(refItem)) {
                dlog.error(refItem.pos, DiagnosticErrorCode.INVALID_ASSIGNMENT_DECLARATION_FINAL,
                        Names.FUNCTION);
                unresolvedReference = true;
            }

            if (!isValidVariableReference(refItem)) {
                unresolvedReference = true;
                continue;
            }

            if (refItem.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR
                    || refItem.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
                dlog.error(refItem.pos, DiagnosticErrorCode.INVALID_VARIABLE_REFERENCE_IN_BINDING_PATTERN,
                        refItem);
                unresolvedReference = true;
                continue;
            }

            if (refItem.symbol == null) {
                unresolvedReference = true;
            }
        }

        if (varRefExpr.restVar != null) {
            varRefExpr.restVar.isLValue = true;
            if (varRefExpr.restVar.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                checkExpr(varRefExpr.restVar, data);
                unresolvedReference = unresolvedReference
                        || varRefExpr.restVar.symbol == null
                        || !isValidVariableReference(varRefExpr.restVar);
            }
        }

        if (unresolvedReference) {
            data.resultType = symTable.semanticError;
            return;
        }

        BType errorRefRestFieldType;
        if (varRefExpr.restVar == null) {
            errorRefRestFieldType = symTable.anydataOrReadonly;
        } else if (varRefExpr.restVar.getKind() == NodeKind.SIMPLE_VARIABLE_REF
                && ((BLangSimpleVarRef) varRefExpr.restVar).variableName.value.equals(Names.IGNORE.value)) {
            errorRefRestFieldType = symTable.anydataOrReadonly;
        } else if (varRefExpr.restVar.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR
            || varRefExpr.restVar.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
            errorRefRestFieldType = varRefExpr.restVar.getBType();
        } else if (varRefExpr.restVar.getBType().tag == TypeTags.MAP) {
            errorRefRestFieldType = ((BMapType) varRefExpr.restVar.getBType()).constraint;
        } else {
            dlog.error(varRefExpr.restVar.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                       varRefExpr.restVar.getBType(), symTable.detailType);
            data.resultType = symTable.semanticError;
            return;
        }

        BType errorDetailType = errorRefRestFieldType == symTable.anydataOrReadonly
                ? symTable.errorType.detailType
                : new BMapType(TypeTags.MAP, errorRefRestFieldType, null, Flags.PUBLIC);
        data.resultType = new BErrorType(symTable.errorType.tsymbol, errorDetailType);
    }

    private void checkIndirectErrorVarRef(BLangErrorVarRef varRefExpr, AnalyzerData data) {
        for (BLangNamedArgsExpression detailItem : varRefExpr.detail) {
            checkExpr(detailItem.expr, data);
            checkExpr(detailItem, detailItem.expr.getBType(), data);
        }

        if (varRefExpr.restVar != null) {
            checkExpr(varRefExpr.restVar, data);
        }

        if (varRefExpr.message != null) {
            varRefExpr.message.isLValue = true;
            checkExpr(varRefExpr.message, data);
        }

        if (varRefExpr.cause != null) {
            varRefExpr.cause.isLValue = true;
            checkExpr(varRefExpr.cause, data);
        }
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr, AnalyzerData data) {
        List<BTupleMember> results = new ArrayList<>();
        for (int i = 0; i < varRefExpr.expressions.size(); i++) {
            ((BLangVariableReference) varRefExpr.expressions.get(i)).isLValue = true;
            BType memberType = checkExpr(varRefExpr.expressions.get(i), symTable.noType, data);
            BVarSymbol varSymbol = new BVarSymbol(memberType.flags, null, null, memberType, null,
                    null, null);
            results.add(new BTupleMember(memberType, varSymbol));
        }
        BTupleType actualType = new BTupleType(results);
        if (varRefExpr.restParam != null) {
            BLangExpression restExpr = varRefExpr.restParam;
            ((BLangVariableReference) restExpr).isLValue = true;
            BType checkedType = checkExpr(restExpr, symTable.noType, data);
            if (!(checkedType.tag == TypeTags.ARRAY || checkedType.tag == TypeTags.TUPLE)) {
                dlog.error(varRefExpr.pos, DiagnosticErrorCode.INVALID_TYPE_FOR_REST_DESCRIPTOR, checkedType);
                data.resultType = symTable.semanticError;
                return;
            }
            if (checkedType.tag == TypeTags.ARRAY) {
                actualType.restType = ((BArrayType) checkedType).eType;
            } else {
                actualType.restType = checkedType;
            }
        }
        data.resultType = types.checkType(varRefExpr, actualType, data.expType);
    }

    /**
     * This method will recursively check if a multidimensional array has at least one open sealed dimension.
     *
     * @param arrayType array to check if open sealed
     * @return true if at least one dimension is open sealed
     */
    public boolean isArrayOpenSealedType(BArrayType arrayType) {
        if (arrayType.state == BArrayState.INFERRED) {
            return true;
        }
        if (arrayType.eType.tag == TypeTags.ARRAY) {
            return isArrayOpenSealedType((BArrayType) arrayType.eType);
        }
        return false;
    }

    /**
     * This method will recursively traverse and find the symbol environment of a lambda node (which is given as the
     * enclosing invokable node) which is needed to lookup closure variables. The variable lookup will start from the
     * enclosing invokable node's environment, which are outside of the scope of a lambda function.
     */
    private SymbolEnv findEnclosingInvokableEnv(SymbolEnv env, BLangInvokableNode encInvokable) {
        if (env.enclEnv.node == null) {
            return env;
        }
        NodeKind kind = env.enclEnv.node.getKind();
        if (kind == NodeKind.ARROW_EXPR || kind == NodeKind.ON_FAIL) {
            // TODO : check if we need ON_FAIL now
            return env.enclEnv;
        }

        if (kind == NodeKind.CLASS_DEFN) {
            return env.enclEnv.enclEnv;
        }

        if (env.enclInvokable != null && env.enclInvokable == encInvokable) {
            return findEnclosingInvokableEnv(env.enclEnv, encInvokable);
        }
        return env;
    }

    private SymbolEnv findEnclosingInvokableEnv(SymbolEnv env, BLangRecordTypeNode recordTypeNode) {
        if (env.enclEnv.node != null) {
            NodeKind kind = env.enclEnv.node.getKind();
            if (kind == NodeKind.ARROW_EXPR || kind == NodeKind.ON_FAIL || kind == NodeKind.CLASS_DEFN) {
                return env.enclEnv;
            }
        }

        if (env.enclType != null && env.enclType == recordTypeNode) {
            return findEnclosingInvokableEnv(env.enclEnv, recordTypeNode);
        }
        return env;
    }

    private boolean isFunctionArgument(BSymbol symbol, List<BLangSimpleVariable> params) {
        return params.stream().anyMatch(param -> (param.symbol.name.equals(symbol.name) &&
                param.getBType().tag == symbol.type.tag));
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess nsPrefixedFieldBasedAccess,
                      AnalyzerData data) {
        checkFieldBasedAccess(nsPrefixedFieldBasedAccess, true, data);
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr, AnalyzerData data) {
        checkFieldBasedAccess(fieldAccessExpr, false, data);
    }

    private void checkFieldBasedAccess(BLangFieldBasedAccess fieldAccessExpr, boolean isNsPrefixed, AnalyzerData data) {
        markLeafNode(fieldAccessExpr);

        // First analyze the accessible expression.
        BLangExpression containerExpression = fieldAccessExpr.expr;

        if (containerExpression instanceof BLangValueExpression) {
            ((BLangValueExpression) containerExpression).isLValue = fieldAccessExpr.isLValue;
            ((BLangValueExpression) containerExpression).isCompoundAssignmentLValue =
                    fieldAccessExpr.isCompoundAssignmentLValue;
        }

        BType varRefType = types.getTypeWithEffectiveIntersectionTypes(checkExpr(containerExpression, data));

        // Disallow `expr.ns:attrname` syntax on non xml expressions.
        if (isNsPrefixed && !isXmlAccess(fieldAccessExpr)) {
            dlog.error(fieldAccessExpr.pos, DiagnosticErrorCode.INVALID_FIELD_ACCESS_EXPRESSION);
            data.resultType = symTable.semanticError;
            return;
        }

        BType actualType;
        if (fieldAccessExpr.optionalFieldAccess) {
            if (fieldAccessExpr.isLValue || fieldAccessExpr.isCompoundAssignmentLValue) {
                dlog.error(fieldAccessExpr.pos, DiagnosticErrorCode.OPTIONAL_FIELD_ACCESS_NOT_REQUIRED_ON_LHS);
                data.resultType = symTable.semanticError;
                return;
            }
            actualType = checkOptionalFieldAccessExpr(fieldAccessExpr, varRefType,
                    names.fromIdNode(fieldAccessExpr.field), data);
        } else {
            actualType = checkFieldAccessExpr(fieldAccessExpr, varRefType, names.fromIdNode(fieldAccessExpr.field),
                                              data);

            if (actualType != symTable.semanticError &&
                    (fieldAccessExpr.isLValue || fieldAccessExpr.isCompoundAssignmentLValue)) {
                if (isAllReadonlyTypes(varRefType)) {
                    BType referredType = Types.getReferredType(varRefType);
                    if (referredType.tag != TypeTags.OBJECT
                            || !isInitializationInInit(referredType, data)) {
                        dlog.error(fieldAccessExpr.pos, DiagnosticErrorCode.CANNOT_UPDATE_READONLY_VALUE_OF_TYPE,
                                varRefType);
                        data.resultType = symTable.semanticError;
                        return;
                    }

                } else if (types.isSubTypeOfBaseType(varRefType, TypeTags.RECORD) &&
                        isInvalidReadonlyFieldUpdate(varRefType, fieldAccessExpr.field.value)) {
                    dlog.error(fieldAccessExpr.pos, DiagnosticErrorCode.CANNOT_UPDATE_READONLY_RECORD_FIELD,
                            fieldAccessExpr.field.value, varRefType);
                    data.resultType = symTable.semanticError;
                    return;
                }
                // Object final field updates will be analyzed at dataflow analysis.
            }
        }

        data.resultType = types.checkType(fieldAccessExpr, actualType, data.expType);
    }

    private boolean isAllReadonlyTypes(BType type) {
        if (type.tag != TypeTags.UNION) {
            return Symbols.isFlagOn(type.flags, Flags.READONLY);
        }

        for (BType memberType : ((BUnionType) type).getMemberTypes()) {
            if (!isAllReadonlyTypes(memberType)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInitializationInInit(BType type, AnalyzerData data) {
        BObjectType objectType = (BObjectType) type;
        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) objectType.tsymbol;
        BAttachedFunction initializerFunc = objectTypeSymbol.initializerFunc;

        return data.env.enclInvokable != null && initializerFunc != null &&
                data.env.enclInvokable.symbol == initializerFunc.symbol;
    }

    private boolean isInvalidReadonlyFieldUpdate(BType type, String fieldName) {
        if (Types.getReferredType(type).tag == TypeTags.RECORD) {
            if (Symbols.isFlagOn(type.flags, Flags.READONLY)) {
                return true;
            }

            BRecordType recordType = (BRecordType) Types.getReferredType(type);
            for (BField field : recordType.fields.values()) {
                if (!field.name.value.equals(fieldName)) {
                    continue;
                }

                return Symbols.isFlagOn(field.symbol.flags, Flags.READONLY);
            }
            return recordType.sealed;
        }

        // For unions, we consider this an invalid update only if it is invalid for all member types. If for at least
        // one member this is valid, we allow this at compile time with the potential to fail at runtime.
        boolean allInvalidUpdates = true;
        for (BType memberType : ((BUnionType) Types.getReferredType(type)).getMemberTypes()) {
            if (!isInvalidReadonlyFieldUpdate(memberType, fieldName)) {
                allInvalidUpdates = false;
            }
        }
        return allInvalidUpdates;
    }

    private boolean isXmlAccess(BLangFieldBasedAccess fieldAccessExpr) {
        BLangExpression expr = fieldAccessExpr.expr;
        BType exprType = Types.getReferredType(expr.getBType());

        if (exprType.tag == TypeTags.XML || exprType.tag == TypeTags.XML_ELEMENT) {
            return true;
        }

        if (expr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR  && hasLaxOriginalType((BLangFieldBasedAccess) expr)
                && exprType.tag == TypeTags.UNION) {
            Set<BType> memberTypes = ((BUnionType) exprType).getMemberTypes();
            return memberTypes.contains(symTable.xmlType) || memberTypes.contains(symTable.xmlElementType);
          }

        return false;
    }

    public void visit(BLangIndexBasedAccess indexBasedAccessExpr, AnalyzerData data) {
        markLeafNode(indexBasedAccessExpr);

        // First analyze the variable reference expression.
        BLangExpression containerExpression = indexBasedAccessExpr.expr;
        if (containerExpression.getKind() ==  NodeKind.TYPEDESC_EXPRESSION) {
            dlog.error(indexBasedAccessExpr.pos, DiagnosticErrorCode.OPERATION_DOES_NOT_SUPPORT_MEMBER_ACCESS,
                    ((BLangTypedescExpr) containerExpression).typeNode);
            data.resultType = symTable.semanticError;
            return;
        }

        if (containerExpression instanceof BLangValueExpression) {
            ((BLangValueExpression) containerExpression).isLValue = indexBasedAccessExpr.isLValue;
            ((BLangValueExpression) containerExpression).isCompoundAssignmentLValue =
                    indexBasedAccessExpr.isCompoundAssignmentLValue;
        }

        boolean isStringValue = containerExpression.getBType() != null
                && Types.getReferredType(containerExpression.getBType()).tag == TypeTags.STRING;
        if (!isStringValue) {
            checkExpr(containerExpression, symTable.noType, data);
        }

        BType exprType = containerExpression.getBType();
        BLangExpression indexExpr = indexBasedAccessExpr.indexExpr;

        if (indexExpr.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR &&
                Types.getReferredType(exprType).tag != TypeTags.TABLE) {
            dlog.error(indexBasedAccessExpr.pos, DiagnosticErrorCode.MULTI_KEY_MEMBER_ACCESS_NOT_SUPPORTED, exprType);
            data.resultType = symTable.semanticError;
            return;
        }

        BType actualType = checkIndexAccessExpr(indexBasedAccessExpr, data);
        if (actualType != symTable.semanticError &&
                (indexBasedAccessExpr.isLValue || indexBasedAccessExpr.isCompoundAssignmentLValue)) {
            if (isAllReadonlyTypes(exprType)) {
                dlog.error(indexBasedAccessExpr.pos, DiagnosticErrorCode.CANNOT_UPDATE_READONLY_VALUE_OF_TYPE,
                        exprType);
                data.resultType = symTable.semanticError;
                return;
            } else if (types.isSubTypeOfBaseType(exprType, TypeTags.RECORD) && isConstExpr(indexExpr) &&
                    isInvalidReadonlyFieldUpdate(exprType, getConstFieldName(indexExpr))) {
                dlog.error(indexBasedAccessExpr.pos, DiagnosticErrorCode.CANNOT_UPDATE_READONLY_RECORD_FIELD,
                        getConstFieldName(indexExpr), exprType);
                data.resultType = symTable.semanticError;
                return;
            }
        }

        // If this is on lhs, no need to do type checking further. And null/error
        // will not propagate from parent expressions
        if (indexBasedAccessExpr.isLValue) {
            indexBasedAccessExpr.originalType = actualType;
            indexBasedAccessExpr.setBType(actualType);
            data.resultType = actualType;
            return;
        }

        data.resultType = this.types.checkType(indexBasedAccessExpr, actualType, data.expType);
    }

    public void visit(BLangInvocation iExpr, AnalyzerData data) {
        // Variable ref expression null means this is the leaf node of the variable ref expression tree
        // e.g. foo();, foo(), foo().k;
        if (iExpr.expr == null) {
            // This is a function invocation expression. e.g. foo()
            checkFunctionInvocationExpr(iExpr, data);
            return;
        }

        // Module aliases cannot be used with methods
        if (invalidModuleAliasUsage(iExpr)) {
            return;
        }

        // Find the variable reference expression type
        checkExpr(iExpr.expr, symTable.noType, data);

        BType varRefType = iExpr.expr.getBType();
        visitInvocation(iExpr, varRefType, data);
    }

    private void visitInvocation(BLangInvocation iExpr, BType varRefType, AnalyzerData data) {
        switch (varRefType.tag) {
            case TypeTags.OBJECT:
                // Invoking a function bound to an object
                // First check whether there exist a function with this name
                // Then perform arg and param matching
                checkObjectFunctionInvocationExpr(iExpr, (BObjectType) varRefType, data);
                break;
            case TypeTags.RECORD:
                checkFieldFunctionPointer(iExpr, data);
                break;
            case TypeTags.NONE:
                dlog.error(iExpr.pos, DiagnosticErrorCode.UNDEFINED_FUNCTION, iExpr.name);
                break;
            case TypeTags.TYPEREFDESC:
                visitInvocation(iExpr, Types.getReferredType(varRefType), data);
                break;
            case TypeTags.INTERSECTION:
                visitInvocation(iExpr, ((BIntersectionType) varRefType).effectiveType, data);
                break;
            case TypeTags.SEMANTIC_ERROR:
                break;
            default:
                checkInLangLib(iExpr, varRefType, data);
        }
    }

    public void visit(BLangErrorConstructorExpr errorConstructorExpr, AnalyzerData data) {
        BLangUserDefinedType userProvidedTypeRef = errorConstructorExpr.errorTypeRef;
        if (userProvidedTypeRef != null) {
            symResolver.resolveTypeNode(userProvidedTypeRef, data.env,
                                        DiagnosticErrorCode.UNDEFINED_ERROR_TYPE_DESCRIPTOR);
        }
        validateErrorConstructorPositionalArgs(errorConstructorExpr, data);

        List<BType> expandedCandidates = getTypeCandidatesForErrorConstructor(errorConstructorExpr, data);

        List<BType> errorDetailTypes = new ArrayList<>(expandedCandidates.size());
        for (BType expandedCandidate : expandedCandidates) {
            BType detailType = ((BErrorType) Types.getReferredType(expandedCandidate)).detailType;
            errorDetailTypes.add(Types.getReferredType(detailType));
        }

        BType detailCandidate;
        if (errorDetailTypes.size() == 1) {
            detailCandidate = errorDetailTypes.get(0);
        } else {
            detailCandidate = BUnionType.create(null, new LinkedHashSet<>(errorDetailTypes));
        }

        BLangRecordLiteral recordLiteral = createRecordLiteralForErrorConstructor(errorConstructorExpr);
        BType inferredDetailType = checkExprSilent(recordLiteral, detailCandidate, data);

        int index = errorDetailTypes.indexOf(inferredDetailType);
        BType selectedCandidate = index < 0 ? symTable.semanticError : expandedCandidates.get(index);

        if (selectedCandidate != symTable.semanticError
                && (userProvidedTypeRef == null
                || Types.getReferredType(userProvidedTypeRef.getBType()) == Types.getReferredType(selectedCandidate))) {
            checkProvidedErrorDetails(errorConstructorExpr, inferredDetailType, data);
            // TODO: When the `userProvidedTypeRef` is present diagnostic message is provided for just `error`
            // https://github.com/ballerina-platform/ballerina-lang/issues/33574
            data.resultType = types.checkType(errorConstructorExpr.pos, selectedCandidate, data.expType,
                    DiagnosticErrorCode.INCOMPATIBLE_TYPES);
            return;
        }

        if (userProvidedTypeRef == null && errorDetailTypes.size() > 1) {
            dlog.error(errorConstructorExpr.pos, DiagnosticErrorCode.CANNOT_INFER_ERROR_TYPE, data.expType);
        }

        boolean validTypeRefFound = false;
        // Error details provided does not match the contextually expected error type.
        // if type reference is not provided let's take the `ballerina/lang.error:error` as the expected type.
        BErrorType errorType;
        if (userProvidedTypeRef != null
                && Types.getReferredType(userProvidedTypeRef.getBType()).tag == TypeTags.ERROR) {
            errorType = (BErrorType) Types.getReferredType(userProvidedTypeRef.getBType());
            validTypeRefFound = true;
        } else if (expandedCandidates.size() == 1) {
            errorType = (BErrorType) Types.getReferredType(expandedCandidates.get(0));
        } else {
            errorType = symTable.errorType;
        }
        List<BLangNamedArgsExpression> namedArgs =
                checkProvidedErrorDetails(errorConstructorExpr, errorType.detailType, data);

        BType detailType = errorType.detailType;

        if (Types.getReferredType(detailType).tag == TypeTags.MAP) {
            BType errorDetailTypeConstraint = ((BMapType) Types.getReferredType(detailType)).constraint;
            for (BLangNamedArgsExpression namedArgExpr: namedArgs) {
                if (Types.getReferredType(errorDetailTypeConstraint).tag == TypeTags.UNION &&
                        !types.isAssignable(namedArgExpr.expr.getBType(), errorDetailTypeConstraint)) {
                    dlog.error(namedArgExpr.pos, DiagnosticErrorCode.INVALID_ERROR_DETAIL_ARG_TYPE,
                               namedArgExpr.name, errorDetailTypeConstraint, namedArgExpr.expr.getBType());
                }
            }
        } else if (Types.getReferredType(detailType).tag == TypeTags.RECORD) {
            BRecordType targetErrorDetailRec = (BRecordType) Types.getReferredType(errorType.detailType);

            LinkedList<String> missingRequiredFields = targetErrorDetailRec.fields.values().stream()
                    .filter(f -> (f.symbol.flags & Flags.REQUIRED) == Flags.REQUIRED)
                    .map(f -> f.name.value)
                    .collect(Collectors.toCollection(LinkedList::new));

            LinkedHashMap<String, BField> targetFields = targetErrorDetailRec.fields;
            for (BLangNamedArgsExpression namedArg : namedArgs) {
                BField field = targetFields.get(namedArg.name.value);
                Location pos = namedArg.pos;
                if (field == null) {
                    if (targetErrorDetailRec.sealed) {
                        dlog.error(pos, DiagnosticErrorCode.UNKNOWN_DETAIL_ARG_TO_CLOSED_ERROR_DETAIL_REC,
                                namedArg.name, targetErrorDetailRec);
                    } else if (targetFields.isEmpty()
                            && !types.isAssignable(namedArg.expr.getBType(), targetErrorDetailRec.restFieldType)) {
                        dlog.error(pos, DiagnosticErrorCode.INVALID_ERROR_DETAIL_REST_ARG_TYPE,
                                namedArg.name, targetErrorDetailRec);
                    }
                } else {
                    missingRequiredFields.remove(namedArg.name.value);
                    if (Types.getReferredType(field.type).tag == TypeTags.UNION &&
                            !types.isAssignable(namedArg.expr.getBType(), field.type)) {
                        dlog.error(pos, DiagnosticErrorCode.INVALID_ERROR_DETAIL_ARG_TYPE,
                                   namedArg.name, field.type, namedArg.expr.getBType());
                    }
                }
            }

            for (String requiredField : missingRequiredFields) {
                dlog.error(errorConstructorExpr.pos, DiagnosticErrorCode.MISSING_ERROR_DETAIL_ARG, requiredField);
            }
        }

        if (userProvidedTypeRef != null) {
            errorConstructorExpr.setBType(Types.getReferredType(userProvidedTypeRef.getBType()));
        } else {
            errorConstructorExpr.setBType(errorType);
        }

        BType resolvedType = errorConstructorExpr.getBType();
        if (resolvedType != symTable.semanticError && data.expType != symTable.noType &&
                !types.isAssignable(resolvedType, data.expType)) {
            if (validTypeRefFound) {
                dlog.error(errorConstructorExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                        data.expType, userProvidedTypeRef);
            } else {
                dlog.error(errorConstructorExpr.pos,
                        DiagnosticErrorCode.ERROR_CONSTRUCTOR_COMPATIBLE_TYPE_NOT_FOUND, data.expType);
            }
            data.resultType = symTable.semanticError;
            return;
        }
        data.resultType = resolvedType;
    }

    private void validateErrorConstructorPositionalArgs(BLangErrorConstructorExpr errorConstructorExpr,
                                                        AnalyzerData data) {
        // Parser handle the missing error message case, and too many positional argument cases.
        if (errorConstructorExpr.positionalArgs.isEmpty()) {
            return;
        }

        checkExpr(errorConstructorExpr.positionalArgs.get(0), symTable.stringType, data);

        int positionalArgCount = errorConstructorExpr.positionalArgs.size();
        if (positionalArgCount > 1) {
            checkExpr(errorConstructorExpr.positionalArgs.get(1), symTable.errorOrNilType, data);
        }

        // todo: Need to add type-checking when fixing #29247 for positional args beyond second arg.
    }

    protected BType checkExprSilent(BLangExpression expr, BType expType, AnalyzerData data) {
        boolean prevNonErrorLoggingCheck = data.commonAnalyzerData.nonErrorLoggingCheck;
        data.commonAnalyzerData.nonErrorLoggingCheck = true;
        int errorCount = this.dlog.errorCount();
        this.dlog.mute();

        BType type = checkExpr(expr, expType, data);

        data.commonAnalyzerData.nonErrorLoggingCheck = prevNonErrorLoggingCheck;
        dlog.setErrorCount(errorCount);
        if (!prevNonErrorLoggingCheck) {
            this.dlog.unmute();
        }

        return type;
    }

    private BLangRecordLiteral createRecordLiteralForErrorConstructor(BLangErrorConstructorExpr errorConstructorExpr) {
        BLangRecordLiteral recordLiteral = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        for (NamedArgNode namedArg : errorConstructorExpr.getNamedArgs()) {
            BLangRecordKeyValueField field =
                    (BLangRecordKeyValueField) TreeBuilder.createRecordKeyValue();
            field.valueExpr = (BLangExpression) namedArg.getExpression();
            BLangLiteral expr = new BLangLiteral();
            expr.value = namedArg.getName().value;
            expr.setBType(symTable.stringType);
            field.key = new BLangRecordKey(expr);
            recordLiteral.fields.add(field);
        }
        return recordLiteral;
    }

    private List<BType> getTypeCandidatesForErrorConstructor(BLangErrorConstructorExpr errorConstructorExpr,
                                                             AnalyzerData data) {
        BLangUserDefinedType errorTypeRef = errorConstructorExpr.errorTypeRef;
        if (errorTypeRef == null) {
            // If contextually expected type for error constructor without type-ref contain errors take it.
            // Else take default error type as the contextually expected type.
            int expReferredTypeTag = Types.getReferredType(data.expType).tag;
            if (expReferredTypeTag == TypeTags.ERROR) {
                return List.of(data.expType);
            } else if (expReferredTypeTag == TypeTags.NEVER) {
                return List.of(symTable.errorType);
            } else if (types.isAssignable(data.expType, symTable.errorType) || data.expType.tag == TypeTags.UNION) {
                return expandExpectedErrorTypes(data.expType);
            }
        } else {
            // if `errorTypeRef.type == semanticError` then an error is already logged.
            BType errorType = Types.getReferredType(errorTypeRef.getBType());
            if (errorType.tag != TypeTags.ERROR) {
                if (errorType.tag != TypeTags.SEMANTIC_ERROR) {
                    dlog.error(errorTypeRef.pos, DiagnosticErrorCode.INVALID_ERROR_TYPE_REFERENCE, errorTypeRef);
                    errorConstructorExpr.errorTypeRef.setBType(symTable.semanticError);
                }
            } else {
                return List.of(errorTypeRef.getBType());
            }
        }

        return List.of(symTable.errorType);
    }

    private List<BType> expandExpectedErrorTypes(BType candidateType) {
        BType referredType = Types.getReferredType(candidateType);
        List<BType> expandedCandidates = new ArrayList<>();
        if (referredType.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) referredType).getMemberTypes()) {
                memberType = Types.getReferredType(memberType);
                if (types.isAssignable(memberType, symTable.errorType)) {
                    if (memberType.tag == TypeTags.INTERSECTION) {
                        expandedCandidates.add(((BIntersectionType) memberType).effectiveType);
                    } else {
                        expandedCandidates.add(memberType);
                    }
                }
            }
        } else if (types.isAssignable(candidateType, symTable.errorType)) {
            if (referredType.tag == TypeTags.INTERSECTION) {
                expandedCandidates.add(((BIntersectionType) referredType).effectiveType);
            } else {
                expandedCandidates.add(candidateType);
            }
        }
        return expandedCandidates;
    }

    public void visit(BLangInvocation.BLangActionInvocation aInv, AnalyzerData data) {
        // For an action invocation, this will only be satisfied when it's an async call of a function.
        // e.g., start foo();
        if (aInv.expr == null) {
            checkFunctionInvocationExpr(aInv, data);
            return;
        }

        // Module aliases cannot be used with remote method call actions
        if (invalidModuleAliasUsage(aInv)) {
            return;
        }

        // Find the variable reference expression type
        checkExpr(aInv.expr, symTable.noType, data);
        BLangExpression varRef = aInv.expr;

        checkActionInvocation(aInv, varRef.getBType(), data);
    }

    @Override
    public void visit(BLangInvocation.BLangResourceAccessInvocation resourceAccessInvocation, AnalyzerData data) {
        // Find the lhs expression type
        checkExpr(resourceAccessInvocation.expr, data);
        BType lhsExprType = resourceAccessInvocation.expr.getBType();
        BType referredLhsExprType = Types.getReferredType(lhsExprType);

        if (referredLhsExprType.tag != TypeTags.OBJECT ||
                !Symbols.isFlagOn(referredLhsExprType.tsymbol.flags, Flags.CLIENT)) {
            dlog.error(resourceAccessInvocation.expr.pos,
                    DiagnosticErrorCode.CLIENT_RESOURCE_ACCESS_ACTION_IS_ONLY_ALLOWED_ON_CLIENT_OBJECTS);
            data.resultType = symTable.semanticError;
            return;
        }

        BObjectTypeSymbol objectTypeSym = (BObjectTypeSymbol) referredLhsExprType.tsymbol;

        if (!validateResourceAccessPathSegmentTypes(resourceAccessInvocation.resourceAccessPathSegments, data)) {
            // Should not resolve the target resource method if the resource path segment types are invalid
            return;
        }

        // Filter all the resource methods defined on target resource path
        List<BResourceFunction> resourceFunctions = new ArrayList<>();
        data.isResourceAccessPathSegments = true;
        for (BAttachedFunction targetFunc : objectTypeSym.attachedFuncs) {
            if (Symbols.isResource(targetFunc.symbol)) {
                BResourceFunction resourceFunction = (BResourceFunction) targetFunc;
                BLangExpression clonedResourceAccPathSeg =
                        nodeCloner.cloneNode(resourceAccessInvocation.resourceAccessPathSegments);
                BType resolvedType = checkExprSilent(clonedResourceAccPathSeg,
                        getResourcePathType(resourceFunction.pathSegmentSymbols), data);
                if (resolvedType != symTable.semanticError) {
                    resourceFunctions.add(resourceFunction);
                }
            }
        }

        if (resourceFunctions.size() == 0) {
            dlog.error(resourceAccessInvocation.resourceAccessPathSegments.pos, DiagnosticErrorCode.UNDEFINED_RESOURCE,
                    lhsExprType);
            data.resultType = symTable.semanticError;
            return;
        }

        // Filter the resource methods in the list by resource access method name
        resourceFunctions.removeIf(func -> !func.accessor.value.equals(resourceAccessInvocation.name.value));
        int targetResourceFuncCount = resourceFunctions.size();
        if (targetResourceFuncCount == 0) {
            dlog.error(resourceAccessInvocation.name.pos,
                    DiagnosticErrorCode.UNDEFINED_RESOURCE_METHOD, resourceAccessInvocation.name, lhsExprType);
            data.resultType = symTable.semanticError;
        } else if (targetResourceFuncCount > 1) {
            dlog.error(resourceAccessInvocation.pos, DiagnosticErrorCode.AMBIGUOUS_RESOURCE_ACCESS_NOT_YET_SUPPORTED);
            data.resultType = symTable.semanticError;
        } else {
            BResourceFunction targetResourceFunc = resourceFunctions.get(0);
            checkExpr(resourceAccessInvocation.resourceAccessPathSegments,
                    getResourcePathType(targetResourceFunc.pathSegmentSymbols), data);
            resourceAccessInvocation.symbol = targetResourceFunc.symbol;
            resourceAccessInvocation.targetResourceFunc = targetResourceFunc;
            checkResourceAccessParamAndReturnType(resourceAccessInvocation, targetResourceFunc, data);
        }
    }
    
    private BTupleType getResourcePathType(List<BResourcePathSegmentSymbol> pathSegmentSymbols) {
        BType restType = null;
        int pathSegmentCount = pathSegmentSymbols.size();
        BResourcePathSegmentSymbol lastPathSegmentSym = pathSegmentSymbols.get(pathSegmentCount - 1);
        if (lastPathSegmentSym.kind == SymbolKind.RESOURCE_PATH_REST_PARAM_SEGMENT) {
            restType = lastPathSegmentSym.type;
            pathSegmentCount--;
        }

        BTupleType resourcePathType = new BTupleType(new ArrayList<>());
        if (pathSegmentCount > 0 && lastPathSegmentSym.kind != SymbolKind.RESOURCE_ROOT_PATH_SEGMENT) {
            for (BResourcePathSegmentSymbol s : pathSegmentSymbols.subList(0, pathSegmentCount)) {
                BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(s.type);
                resourcePathType.addMembers(new BTupleMember(s.type, varSymbol));
            }
        }
        
        resourcePathType.restType = restType;
        return resourcePathType;
    }

    /**
     * Validate resource access path segment types.
     *
     * @return true if the path segment types are valid. False otherwise
     */
    public boolean validateResourceAccessPathSegmentTypes(BLangListConstructorExpr rAPathSegments, AnalyzerData data) {
        // We should type check `pathSegments` against the resourcePathType. This method is just to validate
        // allowed types for resource access segments hence use clones of nodes
        boolean isValidResourceAccessPathSegmentTypes = true;
        BLangListConstructorExpr clonedRAPathSegments = nodeCloner.cloneNode(rAPathSegments);
        for (BLangExpression pathSegment : clonedRAPathSegments.exprs) {
            BLangExpression clonedPathSegment = nodeCloner.cloneNode(pathSegment);
            if (clonedPathSegment.getKind() == NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                BLangExpression spreadOpExpr = ((BLangListConstructorSpreadOpExpr) clonedPathSegment).expr;
                BType pathSegmentType = checkExpr(spreadOpExpr, data);
                if (!types.isAssignable(pathSegmentType, new BArrayType(symTable.pathParamAllowedType))) {
                    dlog.error(clonedPathSegment.getPosition(),
                            DiagnosticErrorCode.UNSUPPORTED_RESOURCE_ACCESS_REST_SEGMENT_TYPE, pathSegmentType);
                    isValidResourceAccessPathSegmentTypes = false;
                }
                continue;
            }

            BType pathSegmentType = checkExpr(clonedPathSegment, data);
            if (!types.isAssignable(pathSegmentType, symTable.pathParamAllowedType)) {
                dlog.error(clonedPathSegment.getPosition(),
                        DiagnosticErrorCode.UNSUPPORTED_COMPUTED_RESOURCE_ACCESS_PATH_SEGMENT_TYPE, pathSegmentType);
                isValidResourceAccessPathSegmentTypes = false;
            }
        }

        return isValidResourceAccessPathSegmentTypes;
    }

    public void checkResourceAccessParamAndReturnType(BLangInvocation.BLangResourceAccessInvocation resourceAccessInvoc,
                                                      BResourceFunction targetResourceFunc, AnalyzerData data) {
        // targetResourceFunc symbol params will contain path params and rest path params as well,
        // hence we need to remove path params from the list before calling to `checkInvocationParamAndReturnType` 
        // method otherwise we get `missing required parameter` error
        BInvokableSymbol targetResourceSym = targetResourceFunc.symbol;
        BInvokableType targetResourceSymType = targetResourceSym.getType();
        List<BVarSymbol> originalInvocableTSymParams =
                ((BInvokableTypeSymbol) targetResourceSymType.tsymbol).params;
        List<BType> originalInvocableSymParamTypes = targetResourceSymType.paramTypes;
        int pathParamCount = targetResourceFunc.pathParams.size() + (targetResourceFunc.restPathParam == null ? 0 : 1);
        int totalParamsCount = originalInvocableSymParamTypes.size();
        int functionParamCount = totalParamsCount - pathParamCount;

        List<BVarSymbol> params = new ArrayList<>(functionParamCount);
        List<BType> paramTypes = new ArrayList<>(functionParamCount);

        params.addAll(originalInvocableTSymParams.subList(pathParamCount, totalParamsCount));
        paramTypes.addAll(originalInvocableSymParamTypes.subList(pathParamCount, totalParamsCount));

        ((BInvokableTypeSymbol) targetResourceSymType.tsymbol).params = params;
        targetResourceSym.params = params;
        targetResourceSymType.paramTypes = paramTypes;

        checkInvocationParamAndReturnType(resourceAccessInvoc, data);

        ((BInvokableTypeSymbol) targetResourceSymType.tsymbol).params = originalInvocableTSymParams;
        targetResourceSym.params = originalInvocableTSymParams;
        targetResourceSymType.paramTypes = originalInvocableSymParamTypes;
    }

    private void checkActionInvocation(BLangInvocation.BLangActionInvocation aInv, BType type, AnalyzerData data) {
        switch (type.tag) {
            case TypeTags.OBJECT:
                checkActionInvocation(aInv, (BObjectType) type, data);
                break;
            case TypeTags.RECORD:
                checkFieldFunctionPointer(aInv, data);
                break;
            case TypeTags.NONE:
                dlog.error(aInv.pos, DiagnosticErrorCode.UNDEFINED_FUNCTION, aInv.name);
                data.resultType = symTable.semanticError;
                break;
            case TypeTags.TYPEREFDESC:
                checkActionInvocation(aInv, Types.getReferredType(type), data);
                break;
            case TypeTags.SEMANTIC_ERROR:
            default:
                dlog.error(aInv.pos, DiagnosticErrorCode.INVALID_ACTION_INVOCATION, type);
                data.resultType = symTable.semanticError;
                break;
        }
    }

    private boolean invalidModuleAliasUsage(BLangInvocation invocation) {
        Name pkgAlias = names.fromIdNode(invocation.pkgAlias);
        if (pkgAlias != Names.EMPTY) {
            dlog.error(invocation.pos, DiagnosticErrorCode.PKG_ALIAS_NOT_ALLOWED_HERE);
            return true;
        }
        return false;
    }

    public void visit(BLangLetExpression letExpression, AnalyzerData data) {
        BLetSymbol letSymbol = new BLetSymbol(SymTag.LET, Flags.asMask(new HashSet<>(Lists.of())),
                                              new Name(String.format("$let_symbol_%d$",
                                                       data.commonAnalyzerData.letCount++)),
                data.env.enclPkg.symbol.pkgID, letExpression.getBType(), data.env.scope.owner,
                                              letExpression.pos);
        letExpression.env = SymbolEnv.createExprEnv(letExpression, data.env, letSymbol);
        for (BLangLetVariable letVariable : letExpression.letVarDeclarations) {
            semanticAnalyzer.analyzeNode((BLangNode) letVariable.definitionNode, letExpression.env,
                    data.commonAnalyzerData);
        }
        BType exprType = checkExpr(letExpression.expr, letExpression.env, data.expType, data);
        types.checkType(letExpression, exprType, data.expType);
    }

    private void checkInLangLib(BLangInvocation iExpr, BType varRefType, AnalyzerData data) {
        BSymbol langLibMethodSymbol = getLangLibMethod(iExpr, Types.getReferredType(varRefType), data);
        if (langLibMethodSymbol == symTable.notFoundSymbol) {
            dlog.error(iExpr.name.pos, DiagnosticErrorCode.UNDEFINED_FUNCTION_IN_TYPE, iExpr.name.value,
                       iExpr.expr.getBType());
            data.resultType = symTable.semanticError;
            return;
        }

        if (checkInvalidImmutableValueUpdate(iExpr, varRefType, langLibMethodSymbol, data)) {
            return;
        }

        checkIllegalStorageSizeChangeMethodCall(iExpr, varRefType, data);
    }

    private boolean checkInvalidImmutableValueUpdate(BLangInvocation iExpr, BType varRefType,
                                                     BSymbol langLibMethodSymbol, AnalyzerData data) {
        if (!Symbols.isFlagOn(varRefType.flags, Flags.READONLY)) {
            return false;
        }

        String packageId = langLibMethodSymbol.pkgID.name.value;

        if (!modifierFunctions.containsKey(packageId)) {
            return false;
        }

        String funcName = langLibMethodSymbol.name.value;
        if (!modifierFunctions.get(packageId).contains(funcName)) {
            return false;
        }

        if (funcName.equals("mergeJson") && Types.getReferredType(varRefType).tag != TypeTags.MAP) {
            return false;
        }
        if (funcName.equals("strip") && TypeTags.isXMLTypeTag(Types.getReferredType(varRefType).tag)) {
            return false;
        }

        dlog.error(iExpr.pos, DiagnosticErrorCode.CANNOT_UPDATE_READONLY_VALUE_OF_TYPE, varRefType);
        data.resultType = symTable.semanticError;
        return true;
    }

    private boolean isFixedLengthList(BType type) {
        switch(type.tag) {
            case TypeTags.ARRAY:
                return (((BArrayType) type).state != BArrayState.OPEN);
            case TypeTags.TUPLE:
                return (((BTupleType) type).restType == null);
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) type;
                for (BType member : unionType.getMemberTypes()) {
                    if (!isFixedLengthList(member)) {
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }

    private void checkIllegalStorageSizeChangeMethodCall(BLangInvocation iExpr, BType varRefType, AnalyzerData data) {
        String invocationName = iExpr.name.getValue();
        if (!listLengthModifierFunctions.contains(invocationName)) {
            return;
        }

        if (isFixedLengthList(varRefType)) {
            dlog.error(iExpr.name.pos, DiagnosticErrorCode.ILLEGAL_FUNCTION_CHANGE_LIST_SIZE, invocationName,
                       varRefType);
            data.resultType = symTable.semanticError;
            return;
        }

        if (isShiftOnIncompatibleTuples(varRefType, invocationName)) {
            dlog.error(iExpr.name.pos, DiagnosticErrorCode.ILLEGAL_FUNCTION_CHANGE_TUPLE_SHAPE, invocationName,
                    varRefType);
            data.resultType = symTable.semanticError;
            return;
        }
    }

    private boolean isShiftOnIncompatibleTuples(BType varRefType, String invocationName) {
        if ((varRefType.tag == TypeTags.TUPLE) && (invocationName.compareTo(FUNCTION_NAME_SHIFT) == 0) &&
                hasDifferentTypeThanRest((BTupleType) varRefType)) {
            return true;
        }

        if ((varRefType.tag == TypeTags.UNION) && (invocationName.compareTo(FUNCTION_NAME_SHIFT) == 0)) {
            BUnionType unionVarRef = (BUnionType) varRefType;
            boolean allMemberAreFixedShapeTuples = true;
            for (BType member : unionVarRef.getMemberTypes()) {
                if (member.tag != TypeTags.TUPLE) {
                    allMemberAreFixedShapeTuples = false;
                    break;
                }
                if (!hasDifferentTypeThanRest((BTupleType) member)) {
                    allMemberAreFixedShapeTuples = false;
                    break;
                }
            }
            return allMemberAreFixedShapeTuples;
        }
        return false;
    }

    private boolean hasDifferentTypeThanRest(BTupleType tupleType) {
        if (tupleType.restType == null) {
            return false;
        }

        for (BType member : tupleType.getTupleTypes()) {
            if (!types.isSameType(tupleType.restType, member)) {
                return true;
            }
        }
        return false;
    }

    private void checkFieldFunctionPointer(BLangInvocation iExpr, AnalyzerData data) {
        BType type = checkExpr(iExpr.expr, data.env);
        checkIfLangLibMethodExists(iExpr, type, iExpr.name.pos, DiagnosticErrorCode.INVALID_FUNCTION_INVOCATION, data,
                                   type);
    }

    private void checkIfLangLibMethodExists(BLangInvocation iExpr, BType varRefType, Location pos,
                                            DiagnosticErrorCode errCode, AnalyzerData data, Object... diagMsgArgs) {
        BSymbol langLibMethodSymbol = getLangLibMethod(iExpr, varRefType, data);
        if (langLibMethodSymbol == symTable.notFoundSymbol) {
            dlog.error(pos, errCode, diagMsgArgs);
            data.resultType = symTable.semanticError;
        } else {
            checkInvalidImmutableValueUpdate(iExpr, varRefType, langLibMethodSymbol, data);
        }
    }

    @Override
    public void visit(BLangObjectConstructorExpression objectCtorExpression, AnalyzerData data) {
        BLangClassDefinition classNode = objectCtorExpression.classNode;
        classNode.oceEnvData.capturedClosureEnv = data.env;
        BLangClassDefinition originalClass = classNode.oceEnvData.originalClass;
        if (originalClass.cloneRef != null && !objectCtorExpression.defined) {
            classNode = (BLangClassDefinition) originalClass.cloneRef;
            symbolEnter.defineClassDefinition(classNode, data.env);
            objectCtorExpression.defined = true;
        }

        // TODO: check referenced type
        BObjectType objectType;
        if (objectCtorExpression.referenceType == null && objectCtorExpression.expectedType != null) {
            objectType = (BObjectType) objectCtorExpression.classNode.getBType();
            BType effectiveType = Types.getEffectiveType(Types.getReferredType(objectCtorExpression.expectedType));
            if (effectiveType.tag == TypeTags.OBJECT) {
                BObjectType expObjType = (BObjectType) Types.getReferredType(effectiveType);
                objectType.typeIdSet = expObjType.typeIdSet;
            } else if (effectiveType.tag != TypeTags.NONE) {
                if (!checkAndLoadTypeIdSet(objectCtorExpression.expectedType, objectType)) {
                    dlog.error(objectCtorExpression.pos, DiagnosticErrorCode.INVALID_TYPE_OBJECT_CONSTRUCTOR,
                            objectCtorExpression.expectedType);
                    data.resultType = symTable.semanticError;
                    return;
                }
            }
        }
        BLangTypeInit cIExpr = objectCtorExpression.typeInit;
        BType actualType = symResolver.resolveTypeNode(cIExpr.userDefinedType, data.env);
        if (actualType == symTable.semanticError) {
            data.resultType = symTable.semanticError;
            return;
        }

        BObjectType actualObjectType = (BObjectType) actualType;
        List<BLangType> typeRefs = classNode.typeRefs;
        SymbolEnv typeDefEnv = SymbolEnv.createObjectConstructorObjectEnv(classNode, data.env);
        classNode.oceEnvData.typeInit = objectCtorExpression.typeInit;

        dlog.unmute();
        if (Symbols.isFlagOn(data.expType.flags, Flags.READONLY)) {
            handleObjectConstrExprForReadOnly(objectCtorExpression, actualObjectType, typeDefEnv, false, data);
        } else if (!typeRefs.isEmpty() && Symbols.isFlagOn(typeRefs.get(0).getBType().flags,
                Flags.READONLY)) {
            handleObjectConstrExprForReadOnly(objectCtorExpression, actualObjectType, typeDefEnv, true, data);
        } else {
            semanticAnalyzer.analyzeNode(classNode, typeDefEnv);
        }
        dlog.unmute();
        markConstructedObjectIsolatedness(actualObjectType);

        if (((BObjectTypeSymbol) actualType.tsymbol).initializerFunc != null) {
            BLangInvocation initInvocation = (BLangInvocation) cIExpr.initInvocation;
            initInvocation.symbol = ((BObjectTypeSymbol) actualType.tsymbol).initializerFunc.symbol;
            checkInvocationParam(initInvocation, data);
            cIExpr.initInvocation.setBType(((BInvokableSymbol) initInvocation.symbol).retType);
        } else {
            // If the initializerFunc is null then this is a default constructor invocation. Hence should not
            // pass any arguments.
            if (!isValidInitInvocation(cIExpr, (BObjectType) actualType, data)) {
                return;
            }
        }
        if (cIExpr.initInvocation.getBType() == null) {
            cIExpr.initInvocation.setBType(symTable.nilType);
        }
        BType actualTypeInitType = getObjectConstructorReturnType(actualType, cIExpr.initInvocation.getBType(), data);
        data.resultType = types.checkType(cIExpr, actualTypeInitType, data.expType);
    }

    private boolean isDefiniteObjectType(BType bType, Set<BTypeIdSet> typeIdSets) {
        BType type = Types.getReferredType(bType);
        if (type.tag != TypeTags.OBJECT && type.tag != TypeTags.UNION) {
            return false;
        }

        Set<BType> visitedTypes = new HashSet<>();
        if (!collectObjectTypeIds(type, typeIdSets, visitedTypes)) {
            return false;
        }
        return typeIdSets.size() <= 1;
    }

    private boolean collectObjectTypeIds(BType type, Set<BTypeIdSet> typeIdSets, Set<BType> visitedTypes) {
        BType referredType = Types.getReferredType(type);
        if (referredType.tag == TypeTags.OBJECT) {
            var objectType = (BObjectType) referredType;
            typeIdSets.add(objectType.typeIdSet);
            return true;
        }
        if (referredType.tag == TypeTags.UNION) {
            if (!visitedTypes.add(type)) {
                return true;
            }
            for (BType member : ((BUnionType) referredType).getMemberTypes()) {
                if (!collectObjectTypeIds(member, typeIdSets, visitedTypes)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean checkAndLoadTypeIdSet(BType type, BObjectType objectType) {
        Set<BTypeIdSet> typeIdSets = new HashSet<>();
        if (!isDefiniteObjectType(type, typeIdSets)) {
            return false;
        }
        if (typeIdSets.isEmpty()) {
            objectType.typeIdSet = BTypeIdSet.emptySet();
            return true;
        }
        var typeIdIterator = typeIdSets.iterator();
        if (typeIdIterator.hasNext()) {
            BTypeIdSet typeIdSet = typeIdIterator.next();
            objectType.typeIdSet = typeIdSet;
            return true;
        }
        return true;
    }

    public void visit(BLangTypeInit cIExpr, AnalyzerData data) {
        if ((data.expType.tag == TypeTags.ANY && cIExpr.userDefinedType == null) ||
                data.expType.tag == TypeTags.RECORD) {
            dlog.error(cIExpr.pos, DiagnosticErrorCode.INVALID_TYPE_NEW_LITERAL, data.expType);
            data.resultType = symTable.semanticError;
            return;
        }

        BType actualType;
        if (cIExpr.userDefinedType != null) {
            actualType = symResolver.resolveTypeNode(cIExpr.userDefinedType, data.env);
        } else {
            actualType = data.expType;
        }

        if (actualType == symTable.semanticError) {
            //TODO dlog error?
            data.resultType = symTable.semanticError;
            return;
        }

        data.resultType = checkObjectCompatibility(actualType, cIExpr, data);
    }

    private BType checkObjectCompatibility(BType actualType, BLangTypeInit cIExpr, AnalyzerData data) {
        actualType = checkObjectType(actualType, cIExpr, data);

        if (actualType == symTable.semanticError) {
            return actualType;
        }
        if (cIExpr.initInvocation.getBType() == null) {
            cIExpr.initInvocation.setBType(symTable.nilType);
        }
        BType actualTypeInitType = getObjectConstructorReturnType(actualType, cIExpr.initInvocation.getBType(), data);
        return types.checkType(cIExpr, actualTypeInitType, data.expType);
    }

    private BType checkObjectType(BType actualType, BLangTypeInit cIExpr, AnalyzerData data) {
        BLangInvocation initInvocation = (BLangInvocation) cIExpr.initInvocation;
        switch (actualType.tag) {
            case TypeTags.OBJECT:
                BObjectType actualObjectType = (BObjectType) actualType;

                if ((actualType.tsymbol.flags & Flags.CLASS) != Flags.CLASS) {
                    dlog.error(cIExpr.pos, DiagnosticErrorCode.CANNOT_INITIALIZE_ABSTRACT_OBJECT,
                            actualType.tsymbol);
                    initInvocation.argExprs.forEach(expr -> checkExpr(expr, symTable.noType, data));
                    return symTable.semanticError;
                }

                if (actualObjectType.classDef != null && actualObjectType.classDef.flagSet.contains(Flag.OBJECT_CTOR)) {
                    if (cIExpr.initInvocation != null && actualObjectType.classDef.oceEnvData.typeInit != null) {
                        actualObjectType.classDef.oceEnvData.typeInit = cIExpr;
                    }
                    markConstructedObjectIsolatedness(actualObjectType);
                }
                if (((BObjectTypeSymbol) actualType.tsymbol).initializerFunc != null) {
                    initInvocation.symbol = ((BObjectTypeSymbol) actualType.tsymbol).initializerFunc.symbol;
                    checkInvocationParam(initInvocation, data);
                    initInvocation.setBType(((BInvokableSymbol) initInvocation.symbol).retType);
                } else {
                    // If the initializerFunc is null then this is a default constructor invocation. Hence should not
                    // pass any arguments.
                    if (!isValidInitInvocation(cIExpr, (BObjectType) actualType, data)) {
                        return symTable.semanticError;
                    }
                }
                break;
            case TypeTags.STREAM:
                if (initInvocation.argExprs.size() > 1) {
                    dlog.error(cIExpr.pos, DiagnosticErrorCode.INVALID_STREAM_CONSTRUCTOR, initInvocation);
                    return symTable.semanticError;
                }

                BStreamType actualStreamType = (BStreamType) actualType;
                if (actualStreamType.completionType != null) {
                    BType completionType = actualStreamType.completionType;
                    if (!types.isAssignable(completionType, symTable.errorOrNilType)) {
                        dlog.error(cIExpr.pos, DiagnosticErrorCode.ERROR_TYPE_EXPECTED, completionType.toString());
                        return symTable.semanticError;
                    }
                }

                BUnionType expectedNextReturnType =
                        createNextReturnType(cIExpr.pos, (BStreamType) actualType, data);
                if (initInvocation.argExprs.isEmpty()) {
                    if (!types.containsNilType(actualStreamType.completionType)) {
                        dlog.error(cIExpr.pos, DiagnosticErrorCode.INVALID_UNBOUNDED_STREAM_CONSTRUCTOR_ITERATOR,
                                expectedNextReturnType);
                        return symTable.semanticError;
                    }
                } else {
                    BLangExpression iteratorExpr = initInvocation.argExprs.get(0);
                    BType constructType = checkExpr(iteratorExpr, symTable.noType, data);
                    BType referredConstructType = Types.getReferredType(constructType);
                    if (referredConstructType.tag != TypeTags.OBJECT) {
                        dlog.error(iteratorExpr.pos, DiagnosticErrorCode.INVALID_STREAM_CONSTRUCTOR_ITERATOR,
                                expectedNextReturnType, constructType);
                        return symTable.semanticError;
                    }
                    BAttachedFunction closeFunc = types.getAttachedFuncFromObject((BObjectType) referredConstructType,
                            BLangCompilerConstants.CLOSE_FUNC);
                    if (closeFunc != null) {
                        BType closeableIteratorType = symTable.langQueryModuleSymbol.scope
                                .lookup(Names.ABSTRACT_STREAM_CLOSEABLE_ITERATOR).symbol.type;
                        if (!types.isAssignable(constructType, closeableIteratorType)) {
                            dlog.error(iteratorExpr.pos,
                                    DiagnosticErrorCode.INVALID_STREAM_CONSTRUCTOR_CLOSEABLE_ITERATOR,
                                    expectedNextReturnType, constructType);
                            return symTable.semanticError;
                        }
                    } else {
                        BType iteratorType = symTable.langQueryModuleSymbol.scope
                                .lookup(Names.ABSTRACT_STREAM_ITERATOR).symbol.type;
                        if (!types.isAssignable(constructType, iteratorType)) {
                            dlog.error(iteratorExpr.pos, DiagnosticErrorCode.INVALID_STREAM_CONSTRUCTOR_ITERATOR,
                                    expectedNextReturnType, constructType);
                            return symTable.semanticError;
                        }
                    }
                    BUnionType nextReturnType = types.getVarTypeFromIteratorFuncReturnType(constructType);
                    if (nextReturnType != null) {
                        types.checkType(iteratorExpr.pos, nextReturnType, expectedNextReturnType,
                                DiagnosticErrorCode.INCOMPATIBLE_TYPES);
                    } else {
                        dlog.error(referredConstructType.tsymbol.getPosition(),
                                DiagnosticErrorCode.INVALID_NEXT_METHOD_RETURN_TYPE, expectedNextReturnType);
                    }
                }
                if (data.expType.tag != TypeTags.NONE && !types.isAssignable(actualType, data.expType)) {
                    dlog.error(cIExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data.expType,
                            actualType);
                    return symTable.semanticError;
                }
                return actualType;
            case TypeTags.UNION:
                List<BType> matchingMembers = findMembersWithMatchingInitFunc(cIExpr, (BUnionType) actualType, data);
                BType matchedType = getMatchingType(matchingMembers, cIExpr, actualType, data);
                initInvocation.setBType(symTable.nilType);

                BType referredMatchedType = Types.getReferredType(matchedType);
                if (referredMatchedType.tag == TypeTags.OBJECT) {
                    if (((BObjectTypeSymbol) referredMatchedType.tsymbol).initializerFunc != null) {
                        initInvocation.symbol =
                                ((BObjectTypeSymbol) referredMatchedType.tsymbol).initializerFunc.symbol;
                        checkInvocationParam(initInvocation, data);
                        initInvocation.setBType(((BInvokableSymbol) initInvocation.symbol).retType);
                        actualType = matchedType;
                        break;
                    } else {
                        if (!isValidInitInvocation(cIExpr, (BObjectType) referredMatchedType, data)) {
                            return symTable.semanticError;
                        }
                    }
                }
                types.checkType(cIExpr, matchedType, data.expType);
                cIExpr.setBType(matchedType);
                return matchedType;
            case TypeTags.TYPEREFDESC:
                BType refType = Types.getReferredType(actualType);
                BType compatibleType = checkObjectType(refType, cIExpr, data);
                return compatibleType == refType ? actualType : compatibleType;
            case TypeTags.INTERSECTION:
                return checkObjectType(((BIntersectionType) actualType).effectiveType, cIExpr, data);
            default:
                dlog.error(cIExpr.pos, DiagnosticErrorCode.CANNOT_INFER_OBJECT_TYPE_FROM_LHS, actualType);
                return symTable.semanticError;
        }
        return actualType;
    }

    private BUnionType createNextReturnType(Location pos, BStreamType streamType, AnalyzerData data) {
        BRecordType recordType = new BRecordType(null, Flags.ANONYMOUS);
        recordType.restFieldType = symTable.noType;
        recordType.sealed = true;

        Name fieldName = Names.VALUE;
        BField field = new BField(fieldName, pos, new BVarSymbol(Flags.PUBLIC,
                                                                 fieldName, data.env.enclPkg.packageID,
                                                                 streamType.constraint, data.env.scope.owner, pos,
                                                                 VIRTUAL));
        field.type = streamType.constraint;
        recordType.fields.put(field.name.value, field);

        recordType.tsymbol = Symbols.createRecordSymbol(Flags.ANONYMOUS, Names.EMPTY, data.env.enclPkg.packageID,
                                                        recordType, data.env.scope.owner, pos, VIRTUAL);
        recordType.tsymbol.scope = new Scope(data.env.scope.owner);
        recordType.tsymbol.scope.define(fieldName, field.symbol);

        LinkedHashSet<BType> retTypeMembers = new LinkedHashSet<>();
        retTypeMembers.add(recordType);
        retTypeMembers.addAll(types.getAllTypes(streamType.completionType, false));

        BUnionType unionType = BUnionType.create(null);
        unionType.addAll(retTypeMembers);
        unionType.tsymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, 0, Names.EMPTY,
                data.env.enclPkg.symbol.pkgID, unionType, data.env.scope.owner, pos, VIRTUAL);

        return unionType;
    }

    private boolean isValidInitInvocation(BLangTypeInit cIExpr, BObjectType objType, AnalyzerData data) {
        BLangInvocation initInvocation = (BLangInvocation) cIExpr.initInvocation;
        if (!initInvocation.argExprs.isEmpty()
                && ((BObjectTypeSymbol) objType.tsymbol).initializerFunc == null) {
            dlog.error(cIExpr.pos, DiagnosticErrorCode.TOO_MANY_ARGS_FUNC_CALL,
                    initInvocation.name.value);
            initInvocation.argExprs.forEach(expr -> checkExpr(expr, symTable.noType, data));
            data.resultType = symTable.semanticError;
            return false;
        }
        return true;
    }

    private BType getObjectConstructorReturnType(BType objType, BType initRetType, AnalyzerData data) {
        if (initRetType.tag == TypeTags.UNION) {
            LinkedHashSet<BType> retTypeMembers = new LinkedHashSet<>();
            retTypeMembers.add(objType);

            retTypeMembers.addAll(((BUnionType) initRetType).getMemberTypes());
            retTypeMembers.remove(symTable.nilType);

            BUnionType unionType = BUnionType.create(null, retTypeMembers);
            unionType.tsymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, 0,
                                                         Names.EMPTY, data.env.enclPkg.symbol.pkgID, unionType,
                    data.env.scope.owner, symTable.builtinPos, VIRTUAL);
            return unionType;
        } else if (initRetType.tag == TypeTags.NIL) {
            return objType;
        }
        return symTable.semanticError;
    }

    private List<BType> findMembersWithMatchingInitFunc(BLangTypeInit cIExpr, BUnionType lhsUnionType,
                                                        AnalyzerData data) {
        int objectCount = 0;

        for (BType type : lhsUnionType.getMemberTypes()) {
            BType memberType = Types.getReferredType(type);
            int tag = memberType.tag;

            if (tag == TypeTags.OBJECT) {
                objectCount++;
                continue;
            }

            if (tag != TypeTags.INTERSECTION) {
                continue;
            }

            if (((BIntersectionType) memberType).effectiveType.tag == TypeTags.OBJECT) {
                objectCount++;
            }
        }

        boolean containsSingleObject = objectCount == 1;

        List<BType> matchingLhsMemberTypes = new ArrayList<>();
        for (BType type : lhsUnionType.getMemberTypes()) {
            BType memberType = Types.getReferredType(type);
            if (memberType.tag != TypeTags.OBJECT) {
                // member is not an object.
                continue;
            }
            if ((memberType.tsymbol.flags & Flags.CLASS) != Flags.CLASS) {
                dlog.error(cIExpr.pos, DiagnosticErrorCode.CANNOT_INITIALIZE_ABSTRACT_OBJECT,
                        lhsUnionType.tsymbol);
            }

            if (containsSingleObject) {
                return Collections.singletonList(type);
            }

            BAttachedFunction initializerFunc = ((BObjectTypeSymbol) memberType.tsymbol).initializerFunc;
            if (isArgsMatchesFunction(cIExpr.argsExpr, initializerFunc, data)) {
                matchingLhsMemberTypes.add(type);
            }
        }
        return matchingLhsMemberTypes;
    }

    private BType getMatchingType(List<BType> matchingLhsMembers, BLangTypeInit cIExpr, BType lhsUnion,
                                  AnalyzerData data) {
        if (matchingLhsMembers.isEmpty()) {
            // No union type member found which matches with initializer expression.
            dlog.error(cIExpr.pos, DiagnosticErrorCode.CANNOT_INFER_OBJECT_TYPE_FROM_LHS, lhsUnion);
            data.resultType = symTable.semanticError;
            return symTable.semanticError;
        } else if (matchingLhsMembers.size() == 1) {
            // We have a correct match.
            return matchingLhsMembers.get(0);
        } else {
            // Multiple matches found.
            dlog.error(cIExpr.pos, DiagnosticErrorCode.AMBIGUOUS_TYPES, lhsUnion);
            data.resultType = symTable.semanticError;
            return symTable.semanticError;
        }
    }

    private boolean isArgsMatchesFunction(List<BLangExpression> invocationArguments, BAttachedFunction function,
                                          AnalyzerData data) {
        invocationArguments.forEach(expr -> checkExpr(expr, symTable.noType, data));

        if (function == null) {
            return invocationArguments.isEmpty();
        }

        if (function.symbol.params.isEmpty() && invocationArguments.isEmpty()) {
            return true;
        }

        List<BLangNamedArgsExpression> namedArgs = new ArrayList<>();
        List<BLangExpression> positionalArgs = new ArrayList<>();
        for (BLangExpression argument : invocationArguments) {
            if (argument.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                namedArgs.add((BLangNamedArgsExpression) argument);
            } else {
                positionalArgs.add(argument);
            }
        }

        List<BVarSymbol> requiredParams = function.symbol.params.stream()
                .filter(param -> !param.isDefaultable)
                .collect(Collectors.toList());
        // Given named and positional arguments are less than required parameters.
        if (requiredParams.size() > invocationArguments.size()) {
            return false;
        }

        List<BVarSymbol> defaultableParams = function.symbol.params.stream()
                .filter(param -> param.isDefaultable)
                .collect(Collectors.toList());

        int givenRequiredParamCount = 0;
        for (int i = 0; i < positionalArgs.size(); i++) {
            if (function.symbol.params.size() > i) {
                givenRequiredParamCount++;
                BVarSymbol functionParam = function.symbol.params.get(i);
                // check the type compatibility of positional args against function params.
                if (!types.isAssignable(positionalArgs.get(i).getBType(), functionParam.type)) {
                    return false;
                }
                requiredParams.remove(functionParam);
                defaultableParams.remove(functionParam);
                continue;
            }

            if (function.symbol.restParam != null) {
                BType restParamType = ((BArrayType) function.symbol.restParam.type).eType;
                if (!types.isAssignable(positionalArgs.get(i).getBType(), restParamType)) {
                    return false;
                }
                continue;
            }

            // additional positional args given for function with no rest param
            return false;
        }

        for (BLangNamedArgsExpression namedArg : namedArgs) {
            boolean foundNamedArg = false;
            // check the type compatibility of named args against function params.
            List<BVarSymbol> params = function.symbol.params;
            for (int i = givenRequiredParamCount; i < params.size(); i++) {
                BVarSymbol functionParam = params.get(i);
                if (!namedArg.name.value.equals(functionParam.name.value)) {
                    continue;
                }
                foundNamedArg = true;
                BType namedArgExprType = checkExpr(namedArg.expr, data);
                if (!types.isAssignable(functionParam.type, namedArgExprType)) {
                    // Name matched, type mismatched.
                    return false;
                }
                requiredParams.remove(functionParam);
                defaultableParams.remove(functionParam);
            }
            if (!foundNamedArg) {
                return false;
            }
        }

        // all required params are not given by positional or named args.
        return requiredParams.size() <= 0;
    }

    public void visit(BLangWaitForAllExpr waitForAllExpr, AnalyzerData data) {
        setResultTypeForWaitForAllExpr(waitForAllExpr, data.expType, data);
        waitForAllExpr.setBType(data.resultType);

        if (data.resultType != null && data.resultType != symTable.semanticError) {
            types.setImplicitCastExpr(waitForAllExpr, waitForAllExpr.getBType(), data.expType);
        }
    }

    private void setResultTypeForWaitForAllExpr(BLangWaitForAllExpr waitForAllExpr, BType expType, AnalyzerData data) {
        switch (expType.tag) {
            case TypeTags.RECORD:
                checkTypesForRecords(waitForAllExpr, data);
                break;
            case TypeTags.MAP:
                checkTypesForMap(waitForAllExpr, ((BMapType) expType).constraint, data);
                LinkedHashSet<BType> memberTypesForMap = collectWaitExprTypes(waitForAllExpr.keyValuePairs);
                if (memberTypesForMap.size() == 1) {
                    data.resultType = new BMapType(TypeTags.MAP,
                            memberTypesForMap.iterator().next(), symTable.mapType.tsymbol);
                    break;
                }
                BUnionType constraintTypeForMap = BUnionType.create(null, memberTypesForMap);
                data.resultType = new BMapType(TypeTags.MAP, constraintTypeForMap, symTable.mapType.tsymbol);
                break;
            case TypeTags.NONE:
            case TypeTags.ANY:
                checkTypesForMap(waitForAllExpr, expType, data);
                LinkedHashSet<BType> memberTypes = collectWaitExprTypes(waitForAllExpr.keyValuePairs);
                if (memberTypes.size() == 1) {
                    data.resultType =
                            new BMapType(TypeTags.MAP, memberTypes.iterator().next(), symTable.mapType.tsymbol);
                    break;
                }
                BUnionType constraintType = BUnionType.create(null, memberTypes);
                data.resultType = new BMapType(TypeTags.MAP, constraintType, symTable.mapType.tsymbol);
                break;
            case TypeTags.TYPEREFDESC:
                setResultTypeForWaitForAllExpr(waitForAllExpr, Types.getReferredType(expType), data);
                break;
            default:
                dlog.error(waitForAllExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, expType,
                        getWaitForAllExprReturnType(waitForAllExpr, waitForAllExpr.pos, data));
                data.resultType = symTable.semanticError;
                break;
        }
    }

    private BRecordType getWaitForAllExprReturnType(BLangWaitForAllExpr waitExpr,
                                                    Location pos, AnalyzerData data) {
        BRecordType retType = new BRecordType(null, Flags.ANONYMOUS);
        List<BLangWaitForAllExpr.BLangWaitKeyValue> keyVals = waitExpr.keyValuePairs;

        for (BLangWaitForAllExpr.BLangWaitKeyValue keyVal : keyVals) {
            BLangIdentifier fieldName;
            if (keyVal.valueExpr == null || keyVal.valueExpr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                fieldName = keyVal.key;
            } else {
                fieldName = ((BLangSimpleVarRef) keyVal.valueExpr).variableName;
            }

            BSymbol symbol = symResolver.lookupSymbolInMainSpace(data.env, names.fromIdNode(fieldName));
            BType fieldType = symbol.type.tag == TypeTags.FUTURE ? ((BFutureType) symbol.type).constraint : symbol.type;
            BField field = new BField(names.fromIdNode(keyVal.key), null,
                                      new BVarSymbol(0, names.fromIdNode(keyVal.key),
                                                     names.originalNameFromIdNode(keyVal.key),
                                                     data.env.enclPkg.packageID, fieldType, null,
                                                     keyVal.pos, VIRTUAL));
            retType.fields.put(field.name.value, field);
        }

        retType.restFieldType = symTable.noType;
        retType.sealed = true;
        retType.tsymbol = Symbols.createRecordSymbol(Flags.ANONYMOUS, Names.EMPTY, data.env.enclPkg.packageID, retType,
                                              null, pos, VIRTUAL);
        return retType;
    }

    private LinkedHashSet<BType> collectWaitExprTypes(List<BLangWaitForAllExpr.BLangWaitKeyValue> keyVals) {
        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
        for (BLangWaitForAllExpr.BLangWaitKeyValue keyVal : keyVals) {
            BType bType = keyVal.keyExpr != null ? keyVal.keyExpr.getBType() : keyVal.valueExpr.getBType();
            if (bType.tag == TypeTags.FUTURE) {
                memberTypes.add(((BFutureType) bType).constraint);
            } else {
                memberTypes.add(bType);
            }
        }
        return memberTypes;
    }

    private void checkTypesForMap(BLangWaitForAllExpr waitForAllExpr, BType expType, AnalyzerData data) {
        List<BLangWaitForAllExpr.BLangWaitKeyValue> keyValuePairs = waitForAllExpr.keyValuePairs;
        keyValuePairs.forEach(keyVal -> checkWaitKeyValExpr(keyVal, expType, data));
    }

    private void checkTypesForRecords(BLangWaitForAllExpr waitExpr, AnalyzerData data) {
        List<BLangWaitForAllExpr.BLangWaitKeyValue> rhsFields = waitExpr.getKeyValuePairs();
        Map<String, BField> lhsFields = ((BRecordType) Types.getReferredType(data.expType)).fields;

        // check if the record is sealed, if so check if the fields in wait collection is more than the fields expected
        // by the lhs record
        if (((BRecordType) Types.getReferredType(data.expType)).sealed &&
                rhsFields.size() > lhsFields.size()) {
            dlog.error(waitExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data.expType,
                    getWaitForAllExprReturnType(waitExpr, waitExpr.pos, data));
            data.resultType = symTable.semanticError;
            return;
        }

        for (BLangWaitForAllExpr.BLangWaitKeyValue keyVal : rhsFields) {
            String key = keyVal.key.value;
            if (!lhsFields.containsKey(key)) {
                // Check if the field is sealed if so you cannot have dynamic fields
                if (((BRecordType) Types.getReferredType(data.expType)).sealed) {
                    dlog.error(waitExpr.pos, DiagnosticErrorCode.INVALID_FIELD_NAME_RECORD_LITERAL, key, data.expType);
                    data.resultType = symTable.semanticError;
                } else {
                    // Else if the record is an open record, then check if the rest field type matches the expression
                    BType restFieldType = ((BRecordType) Types.getReferredType(data.expType)).restFieldType;
                    checkWaitKeyValExpr(keyVal, restFieldType, data);
                }
            } else {
                checkWaitKeyValExpr(keyVal, lhsFields.get(key).type, data);
                keyVal.keySymbol = lhsFields.get(key).symbol;
            }
        }
        // If the record literal is of record type and types are validated for the fields, check if there are any
        // required fields missing.
        checkMissingReqFieldsForWait(((BRecordType) Types.getReferredType(data.expType)),
                rhsFields, waitExpr.pos);

        if (symTable.semanticError != data.resultType) {
            data.resultType = data.expType;
        }
    }

    private void checkMissingReqFieldsForWait(BRecordType type, List<BLangWaitForAllExpr.BLangWaitKeyValue> keyValPairs,
                                              Location pos) {
        type.fields.values().forEach(field -> {
            // Check if `field` is explicitly assigned a value in the record literal
            boolean hasField = keyValPairs.stream().anyMatch(keyVal -> field.name.value.equals(keyVal.key.value));

            // If a required field is missing, it's a compile error
            if (!hasField && Symbols.isFlagOn(field.symbol.flags, Flags.REQUIRED)) {
                dlog.error(pos, DiagnosticErrorCode.MISSING_REQUIRED_RECORD_FIELD, field.name);
            }
        });
    }

    private void checkWaitKeyValExpr(BLangWaitForAllExpr.BLangWaitKeyValue keyVal, BType type, AnalyzerData data) {
        BLangExpression expr;
        if (keyVal.keyExpr != null) {
            BSymbol symbol = symResolver.lookupSymbolInMainSpace(data.env, names.fromIdNode
                    (((BLangSimpleVarRef) keyVal.keyExpr).variableName));
            keyVal.keyExpr.setBType(symbol.type);
            expr = keyVal.keyExpr;
        } else {
            expr = keyVal.valueExpr;
        }
        BFutureType futureType = new BFutureType(TypeTags.FUTURE, type, null);
        checkExpr(expr, futureType, data);
        setEventualTypeForExpression(expr, type, data);
    }

    // eventual type if not directly referring a worker is T|error. future<T> --> T|error
    private void setEventualTypeForExpression(BLangExpression expression,
                                              BType currentExpectedType, AnalyzerData data) {
        if (expression == null) {
            return;
        }
        if (isSimpleWorkerReference(expression, data)) {
            return;
        }
        BFutureType futureType = (BFutureType) expression.expectedType;
        BType currentType = futureType.constraint;
        if (types.containsErrorType(currentType)) {
            return;
        }

        BUnionType eventualType = BUnionType.create(null, currentType, symTable.errorType);
        if (((currentExpectedType.tag != TypeTags.NONE) && (currentExpectedType.tag != TypeTags.NIL)) &&
                !types.isAssignable(eventualType, currentExpectedType)) {
            dlog.error(expression.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_WAIT_FUTURE_EXPR,
                    currentExpectedType, eventualType, expression);
        }
        futureType.constraint = eventualType;
    }

    private void setEventualTypeForWaitExpression(BLangExpression expression, Location pos, AnalyzerData data) {
        if ((data.resultType == symTable.semanticError) ||
                (types.containsErrorType(data.resultType))) {
            return;
        }
        if (isSimpleWorkerReference(expression, data)) {
            return;
        }
        BType currentExpectedType = ((BFutureType) data.expType).constraint;
        BUnionType eventualType = BUnionType.create(null, data.resultType, symTable.errorType);
        if ((currentExpectedType.tag == TypeTags.NONE) || (currentExpectedType.tag == TypeTags.NIL)) {
            data.resultType = eventualType;
            return;
        }

        if (!types.isAssignable(eventualType, currentExpectedType)) {
            dlog.error(pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_WAIT_FUTURE_EXPR, currentExpectedType,
                    eventualType, expression);
            data.resultType = symTable.semanticError;
            return;
        }
        if (data.resultType.tag == TypeTags.FUTURE) {
            ((BFutureType) data.resultType).constraint = eventualType;
        } else {
            data.resultType = eventualType;
        }
    }

    private void setEventualTypeForAlternateWaitExpression(BLangExpression expression, Location pos,
                                                           AnalyzerData data) {
        if ((data.resultType == symTable.semanticError) ||
                (expression.getKind() != NodeKind.BINARY_EXPR) ||
                (types.containsErrorType(data.resultType))) {
            return;
        }
        if (types.containsErrorType(data.resultType)) {
            return;
        }
        if (!isReferencingNonWorker((BLangBinaryExpr) expression, data)) {
            return;
        }

        BType currentExpectedType = ((BFutureType) data.expType).constraint;
        BUnionType eventualType = BUnionType.create(null, data.resultType, symTable.errorType);
        if ((currentExpectedType.tag == TypeTags.NONE) || (currentExpectedType.tag == TypeTags.NIL)) {
            data.resultType = eventualType;
            return;
        }

        if (!types.isAssignable(eventualType, currentExpectedType)) {
            dlog.error(pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_WAIT_FUTURE_EXPR, currentExpectedType,
                    eventualType, expression);
            data.resultType = symTable.semanticError;
            return;
        }
        if (data.resultType.tag == TypeTags.FUTURE) {
            ((BFutureType) data.resultType).constraint = eventualType;
        } else {
            data.resultType = eventualType;
        }
    }

    private boolean isSimpleWorkerReference(BLangExpression expression, AnalyzerData data) {
        if (expression.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return false;
        }
        BLangSimpleVarRef simpleVarRef = ((BLangSimpleVarRef) expression);
        BSymbol varRefSymbol = simpleVarRef.symbol;
        if (varRefSymbol == null) {
            return false;
        }
        if (workerExists(data.env, simpleVarRef.variableName.value)) {
            return true;
        }
        return false;
    }

    private boolean isReferencingNonWorker(BLangBinaryExpr binaryExpr, AnalyzerData data) {
        BLangExpression lhsExpr = binaryExpr.lhsExpr;
        BLangExpression rhsExpr = binaryExpr.rhsExpr;
        if (isReferencingNonWorker(lhsExpr, data)) {
            return true;
        }
        return isReferencingNonWorker(rhsExpr, data);
    }

    private boolean isReferencingNonWorker(BLangExpression expression, AnalyzerData data) {
        if (expression.getKind() == NodeKind.BINARY_EXPR) {
            return isReferencingNonWorker((BLangBinaryExpr) expression, data);
        } else if (expression.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) expression;
            BSymbol varRefSymbol = simpleVarRef.symbol;
            String varRefSymbolName = varRefSymbol.getName().value;
            if (workerExists(data.env, varRefSymbolName)) {
                return false;
            }
        }
        return true;
    }


    public void visit(BLangTernaryExpr ternaryExpr, AnalyzerData data) {
        BType condExprType = checkExpr(ternaryExpr.expr, this.symTable.booleanType, data);

        SymbolEnv thenEnv = typeNarrower.evaluateTruth(ternaryExpr.expr, ternaryExpr.thenExpr, data.env);
        BType thenType = checkExpr(ternaryExpr.thenExpr, thenEnv, data.expType, data);

        SymbolEnv elseEnv = typeNarrower.evaluateFalsity(ternaryExpr.expr, ternaryExpr.elseExpr, data.env, false);
        BType elseType = checkExpr(ternaryExpr.elseExpr, elseEnv, data.expType, data);

        if (condExprType == symTable.semanticError || thenType == symTable.semanticError ||
                elseType == symTable.semanticError) {
            data.resultType = symTable.semanticError;
        } else if (data.expType == symTable.noType) {
            if (types.isAssignable(elseType, thenType)) {
                data.resultType = thenType;
            } else if (types.isAssignable(thenType, elseType)) {
                data.resultType = elseType;
            } else {
                data.resultType = BUnionType.create(null, thenType, elseType);
            }
        } else {
            data.resultType = data.expType;
        }
    }

    public void visit(BLangWaitExpr waitExpr, AnalyzerData data) {
        data.expType = new BFutureType(TypeTags.FUTURE, data.expType, null);
        checkExpr(waitExpr.getExpression(), data.expType, data);
        // Handle union types in lhs
        if (data.resultType.tag == TypeTags.UNION) {
            LinkedHashSet<BType> memberTypes = collectMemberTypes((BUnionType) data.resultType, new LinkedHashSet<>());
            if (memberTypes.size() == 1) {
                data.resultType = memberTypes.toArray(new BType[0])[0];
            } else {
                data.resultType = BUnionType.create(null, memberTypes);
            }
        } else if (data.resultType != symTable.semanticError) {
            // Handle other types except for semantic errors
            data.resultType = ((BFutureType) data.resultType).constraint;
        }

        BLangExpression waitFutureExpression = waitExpr.getExpression();
        if (waitFutureExpression.getKind() == NodeKind.BINARY_EXPR) {
            setEventualTypeForAlternateWaitExpression(waitFutureExpression, waitExpr.pos, data);
        } else {
            setEventualTypeForWaitExpression(waitFutureExpression, waitExpr.pos, data);
        }
        waitExpr.setBType(data.resultType);

        if (data.resultType != null && data.resultType != symTable.semanticError) {
            types.setImplicitCastExpr(waitExpr, waitExpr.getBType(), ((BFutureType) data.expType).constraint);
        }
    }

    private LinkedHashSet<BType> collectMemberTypes(BUnionType unionType, LinkedHashSet<BType> memberTypes) {
        for (BType memberType : unionType.getMemberTypes()) {
            if (memberType.tag == TypeTags.FUTURE) {
                memberTypes.add(((BFutureType) memberType).constraint);
            } else {
                memberTypes.add(memberType);
            }
        }
        return memberTypes;
    }

    @Override
    public void visit(BLangTrapExpr trapExpr, AnalyzerData data) {
        boolean firstVisit = trapExpr.expr.getBType() == null;
        BType actualType;
        BType exprType = checkExpr(trapExpr.expr, data.expType, data);
        boolean definedWithVar = data.expType == symTable.noType;

        if (trapExpr.expr.getKind() == NodeKind.WORKER_RECEIVE) {
            if (firstVisit) {
                data.isTypeChecked = false;
                data.resultType = data.expType;
                return;
            } else {
                data.expType = trapExpr.getBType();
                exprType = trapExpr.expr.getBType();
            }
        }

        if (data.expType == symTable.semanticError || exprType == symTable.semanticError) {
            actualType = symTable.semanticError;
        } else {
            LinkedHashSet<BType> resultTypes = new LinkedHashSet<>();
            if (exprType.tag == TypeTags.UNION) {
                resultTypes.addAll(((BUnionType) exprType).getMemberTypes());
            } else {
                resultTypes.add(exprType);
            }
            resultTypes.add(symTable.errorType);
            actualType = BUnionType.create(null, resultTypes);
        }

        data.resultType = types.checkType(trapExpr, actualType, data.expType);
        if (definedWithVar && data.resultType != null && data.resultType != symTable.semanticError) {
            types.setImplicitCastExpr(trapExpr.expr, trapExpr.expr.getBType(), data.resultType);
        }
    }

    public void visit(BLangBinaryExpr binaryExpr, AnalyzerData data) {
        // Bitwise operator should be applied for the future types in the wait expression
        if (data.expType.tag == TypeTags.FUTURE && binaryExpr.opKind == OperatorKind.BITWISE_OR) {
            BType lhsResultType = checkExpr(binaryExpr.lhsExpr, data.expType, data);
            BType rhsResultType = checkExpr(binaryExpr.rhsExpr, data.expType, data);
            // Return if both or atleast one of lhs and rhs types are errors
            if (lhsResultType == symTable.semanticError || rhsResultType == symTable.semanticError) {
                data.resultType = symTable.semanticError;
                return;
            }
            data.resultType = BUnionType.create(null, lhsResultType, rhsResultType);
            return;
        }

        SymbolEnv rhsExprEnv;
        BType lhsType;
        BType referredExpType = Types.getReferredType(binaryExpr.expectedType);
        if (referredExpType.tag == TypeTags.FLOAT || referredExpType.tag == TypeTags.DECIMAL ||
                isOptionalFloatOrDecimal(referredExpType)) {
            lhsType = checkAndGetType(binaryExpr.lhsExpr, data.env, binaryExpr, data);
        } else {
            lhsType = checkExpr(binaryExpr.lhsExpr, data);
        }

        if (binaryExpr.opKind == OperatorKind.AND) {
            rhsExprEnv = typeNarrower.evaluateTruth(binaryExpr.lhsExpr, binaryExpr.rhsExpr, data.env, true);
        } else if (binaryExpr.opKind == OperatorKind.OR) {
            rhsExprEnv = typeNarrower.evaluateFalsity(binaryExpr.lhsExpr, binaryExpr.rhsExpr, data.env, true);
        } else {
            rhsExprEnv = data.env;
        }

        BType rhsType;

        if (referredExpType.tag == TypeTags.FLOAT || referredExpType.tag == TypeTags.DECIMAL ||
                isOptionalFloatOrDecimal(referredExpType)) {
            rhsType = checkAndGetType(binaryExpr.rhsExpr, rhsExprEnv, binaryExpr, data);
        } else {
            rhsType = checkExpr(binaryExpr.rhsExpr, rhsExprEnv, data);
        }

        // Set error type as the actual type.
        BType actualType = symTable.semanticError;

        //noinspection SwitchStatementWithTooFewBranches
        switch (binaryExpr.opKind) {
            // Do not lookup operator symbol for xml sequence additions
            case ADD:
                BType leftConstituent = getXMLConstituents(lhsType);
                BType rightConstituent = getXMLConstituents(rhsType);

                if (leftConstituent != null && rightConstituent != null) {
                    actualType = new BXMLType(BUnionType.create(null, leftConstituent, rightConstituent), null);
                    break;
                }
                // Fall through
            default:
                if (lhsType != symTable.semanticError && rhsType != symTable.semanticError) {
                    // Look up operator symbol if both rhs and lhs types aren't error or xml types
                    BSymbol opSymbol = symResolver.resolveBinaryOperator(binaryExpr.opKind, lhsType, rhsType);

                    if (opSymbol == symTable.notFoundSymbol) {
                        opSymbol = symResolver.getBitwiseShiftOpsForTypeSets(binaryExpr.opKind, lhsType, rhsType);
                    }

                    if (opSymbol == symTable.notFoundSymbol) {
                        opSymbol = symResolver.getBinaryBitwiseOpsForTypeSets(binaryExpr.opKind, lhsType, rhsType);
                    }

                    if (opSymbol == symTable.notFoundSymbol) {
                        opSymbol = symResolver.getArithmeticOpsForTypeSets(binaryExpr.opKind, lhsType, rhsType);
                    }

                    if (opSymbol == symTable.notFoundSymbol) {
                        opSymbol = symResolver.getBinaryEqualityForTypeSets(binaryExpr.opKind, lhsType, rhsType,
                                binaryExpr, data.env);
                    }

                    if (opSymbol == symTable.notFoundSymbol) {
                        opSymbol = symResolver.getBinaryComparisonOpForTypeSets(binaryExpr.opKind, lhsType, rhsType);
                    }

                    if (opSymbol == symTable.notFoundSymbol) {
                        opSymbol = symResolver.getRangeOpsForTypeSets(binaryExpr.opKind, lhsType, rhsType);
                    }

                    if (opSymbol == symTable.notFoundSymbol) {
                        DiagnosticErrorCode errorCode = DiagnosticErrorCode.BINARY_OP_INCOMPATIBLE_TYPES;

                        if ((binaryExpr.opKind == OperatorKind.DIV || binaryExpr.opKind == OperatorKind.MOD) &&
                                lhsType.tag == TypeTags.INT &&
                                (rhsType.tag == TypeTags.DECIMAL || rhsType.tag == TypeTags.FLOAT)) {
                            errorCode = DiagnosticErrorCode.BINARY_OP_INCOMPATIBLE_TYPES_INT_FLOAT_DIVISION;
                        }

                        dlog.error(binaryExpr.pos, errorCode, binaryExpr.opKind, lhsType, rhsType);
                    } else {
                        binaryExpr.opSymbol = (BOperatorSymbol) opSymbol;
                        actualType = opSymbol.type.getReturnType();
                    }
                }
        }

        data.resultType = types.checkType(binaryExpr, actualType, data.expType);
    }

    private boolean isOptionalFloatOrDecimal(BType expectedType) {
        if (expectedType.tag == TypeTags.UNION && expectedType.isNullable() && expectedType.tag != TypeTags.ANY) {
            Iterator<BType> memberTypeIterator = ((BUnionType) expectedType).getMemberTypes().iterator();
            while (memberTypeIterator.hasNext()) {
                BType memberType = memberTypeIterator.next();
                if (memberType.tag == TypeTags.FLOAT || memberType.tag == TypeTags.DECIMAL) {
                    return true;
                }
            }

        }
        return false;
    }

    private BType checkAndGetType(BLangExpression expr, SymbolEnv env, BLangBinaryExpr binaryExpr, AnalyzerData data) {
        boolean prevNonErrorLoggingCheck = data.commonAnalyzerData.nonErrorLoggingCheck;
        data.commonAnalyzerData.nonErrorLoggingCheck = true;
        int prevErrorCount = this.dlog.errorCount();
        this.dlog.resetErrorCount();
        this.dlog.mute();

        expr.cloneAttempt++;
        BType exprCompatibleType = checkExpr(nodeCloner.cloneNode(expr), env, binaryExpr.expectedType, data);
        data.commonAnalyzerData.nonErrorLoggingCheck = prevNonErrorLoggingCheck;
        int errorCount = this.dlog.errorCount();
        this.dlog.setErrorCount(prevErrorCount);
        if (!prevNonErrorLoggingCheck) {
            this.dlog.unmute();
        }
        if (errorCount == 0 && exprCompatibleType != symTable.semanticError) {
            return checkExpr(expr, env, binaryExpr.expectedType, data);
        } else {
            return checkExpr(expr, env, data);
        }
    }

    public void visit(BLangTransactionalExpr transactionalExpr, AnalyzerData data) {
        data.resultType = types.checkType(transactionalExpr, symTable.booleanType, data.expType);
    }

    public void visit(BLangCommitExpr commitExpr, AnalyzerData data) {
        BType actualType = BUnionType.create(null, symTable.errorType, symTable.nilType);
        data.resultType = types.checkType(commitExpr, actualType, data.expType);
    }

    private BType getXMLConstituents(BType bType) {
        BType type = Types.getReferredType(bType);
        BType constituent = null;
        if (type.tag == TypeTags.XML) {
            constituent = ((BXMLType) type).constraint;
        } else if (TypeTags.isXMLNonSequenceType(type.tag)) {
            constituent = type;
        }
        return constituent;
    }

    public void visit(BLangElvisExpr elvisExpr, AnalyzerData data) {
        BType lhsType = checkExpr(elvisExpr.lhsExpr, data);
        BType actualType = symTable.semanticError;
        if (lhsType != symTable.semanticError) {
            BType referencedType = Types.getReferredType(lhsType);
            if (referencedType.tag == TypeTags.UNION && referencedType.isNullable()) {
                BUnionType unionType = (BUnionType) referencedType;
                LinkedHashSet<BType> memberTypes = unionType.getMemberTypes().stream()
                        .filter(type -> type.tag != TypeTags.NIL)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                if (memberTypes.size() == 1) {
                    actualType = memberTypes.toArray(new BType[0])[0];
                } else {
                    actualType = BUnionType.create(null, memberTypes);
                }
            } else {
                dlog.error(elvisExpr.pos, DiagnosticErrorCode.OPERATOR_NOT_SUPPORTED, OperatorKind.ELVIS,
                        lhsType);
            }
        }
        BType rhsReturnType = checkExpr(elvisExpr.rhsExpr, data.expType, data);
        BType lhsReturnType = types.checkType(elvisExpr.lhsExpr.pos, actualType, data.expType,
                DiagnosticErrorCode.INCOMPATIBLE_TYPES);
        if (rhsReturnType == symTable.semanticError || lhsReturnType == symTable.semanticError) {
            data.resultType = symTable.semanticError;
        } else if (data.expType == symTable.noType) {
            if (types.isAssignable(rhsReturnType, lhsReturnType)) {
                data.resultType = lhsReturnType;
            } else if (types.isAssignable(lhsReturnType, rhsReturnType)) {
                data.resultType = rhsReturnType;
            } else {
                data.resultType = BUnionType.create(null, lhsReturnType, rhsReturnType);
            }
        } else {
            data.resultType = data.expType;
        }
    }

    @Override
    public void visit(BLangGroupExpr groupExpr, AnalyzerData data) {
        data.resultType = checkExpr(groupExpr.expression, data.expType, data);
    }

    public void visit(BLangTypedescExpr accessExpr, AnalyzerData data) {
        if (accessExpr.resolvedType == null) {
            accessExpr.resolvedType = symResolver.resolveTypeNode(accessExpr.typeNode, data.env);
        }

        int resolveTypeTag = accessExpr.resolvedType.tag;
        final BType actualType;
        if (resolveTypeTag != TypeTags.TYPEDESC && resolveTypeTag != TypeTags.NONE) {
            actualType = new BTypedescType(accessExpr.resolvedType, null);
        } else {
            actualType = accessExpr.resolvedType;
        }
        data.resultType = types.checkType(accessExpr, actualType, data.expType);
    }

    public LinkedHashSet<BType> getBasicNumericTypes(LinkedHashSet<BType> memberTypes) {
        LinkedHashSet<BType> basicNumericTypes = new LinkedHashSet<>(memberTypes.size());

        for (BType value : memberTypes) {
            BType referredType = types.getReferredType(value);
            int typeTag = referredType.tag;
            if (TypeTags.isIntegerTypeTag(typeTag)) {
                basicNumericTypes.add(symTable.intType);
            } else if (typeTag == TypeTags.FLOAT || typeTag == TypeTags.DECIMAL) {
                basicNumericTypes.add(value);
            } else if (typeTag == TypeTags.JSON || typeTag == TypeTags.ANYDATA || typeTag == TypeTags.ANY) {
                basicNumericTypes.add(symTable.intType);
                basicNumericTypes.add(symTable.floatType);
                basicNumericTypes.add(symTable.decimalType);
                break;
            } else if (typeTag == TypeTags.FINITE) {
                LinkedHashSet<BType> typesInValueSpace = getTypesInFiniteValueSpace((BFiniteType) referredType);
                basicNumericTypes.addAll(getBasicNumericTypes(typesInValueSpace));
            }
        }
        return basicNumericTypes;
    }

    public BType createFiniteTypeForNumericUnaryExpr(BLangUnaryExpr unaryExpr, AnalyzerData data) {
        BLangNumericLiteral newNumericLiteral = Types.constructNumericLiteralFromUnaryExpr(unaryExpr);
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE,
                0, Names.EMPTY, data.env.enclPkg.symbol.pkgID, null, data.env.scope.owner,
                unaryExpr.pos, SOURCE);
        BFiniteType finiteType = new BFiniteType(finiteTypeSymbol);
        finiteType.addValue(newNumericLiteral);
        finiteTypeSymbol.type = finiteType;

        types.setImplicitCastExpr(unaryExpr, unaryExpr.expr.getBType(), data.expType);
        return finiteType;
    }

    public LinkedHashSet<BType> getTypesInFiniteValueSpace(BFiniteType referredType) {
        Set<BLangExpression> valueSpace = referredType.getValueSpace();
        LinkedHashSet<BType> typesInValueSpace = new LinkedHashSet<>(valueSpace.size());
        for (BLangExpression expr : valueSpace) {
            typesInValueSpace.add(expr.getBType());
        }
        return typesInValueSpace;
    }

    public BType getNewExpectedTypeForFiniteAndUnion(LinkedHashSet<BType> numericTypes, BType newExpectedType) {
        LinkedHashSet<BType> basicNumericTypes = getBasicNumericTypes(numericTypes);
        if (basicNumericTypes.size() == 1) {
            newExpectedType = basicNumericTypes.iterator().next();
        } else if (basicNumericTypes.size() > 1) {
            newExpectedType = BUnionType.create(null, basicNumericTypes);
        }
        return newExpectedType;
    }

    public BType setExpectedTypeForSubtractionOperator(AnalyzerData data) {
        BType newExpectedType = data.expType;
        BType referredType = types.getReferredType(newExpectedType);
        int referredTypeTag = referredType.tag;

        if (TypeTags.isIntegerTypeTag(referredTypeTag)) {
            newExpectedType =
                    types.getTypeIntersection(Types.IntersectionContext.compilerInternalIntersectionTestContext(),
                            BUnionType.create(null, symTable.intType, symTable.floatType, symTable.decimalType),
                            symTable.intType, data.env);
        } else if (referredTypeTag == TypeTags.FLOAT || referredTypeTag == TypeTags.DECIMAL) {
            newExpectedType =
                    types.getTypeIntersection(Types.IntersectionContext.compilerInternalIntersectionTestContext(),
                            BUnionType.create(null, symTable.intType, symTable.floatType, symTable.decimalType),
                            referredType, data.env);
        } else if (referredTypeTag == TypeTags.FINITE) {
            LinkedHashSet<BType> typesInValueSpace = getTypesInFiniteValueSpace((BFiniteType) referredType);
            newExpectedType = getNewExpectedTypeForFiniteAndUnion(typesInValueSpace, newExpectedType);
        } else if (referredTypeTag == TypeTags.UNION) {
            newExpectedType = getNewExpectedTypeForFiniteAndUnion(((BUnionType) referredType).getMemberTypes(),
                    newExpectedType);
        } else if (referredTypeTag == TypeTags.JSON || referredTypeTag == TypeTags.ANYDATA ||
                referredTypeTag == TypeTags.ANY) {
            newExpectedType = BUnionType.create(null, symTable.intType, symTable.floatType,
                    symTable.decimalType);
        }
        return newExpectedType;
    }

    public BType getActualTypeForOtherUnaryExpr(BLangUnaryExpr unaryExpr, AnalyzerData data) {
        BType actualType = symTable.semanticError;
        BType newExpectedType = data.expType;
        BType referredType = types.getReferredType(newExpectedType);
        int referredTypeTag = referredType.tag;

        //Allow subtraction and add (to resolve ex: byte x = +7) operators to get expected type
        boolean isAddOrSubOperator = OperatorKind.SUB.equals(unaryExpr.operator) ||
                OperatorKind.ADD.equals(unaryExpr.operator);

        if (OperatorKind.SUB.equals(unaryExpr.operator)) {
            newExpectedType = setExpectedTypeForSubtractionOperator(data);
        }

        newExpectedType = silentTypeCheckExpr(unaryExpr.expr, newExpectedType, data);

        BType exprType;
        if (newExpectedType != symTable.semanticError) {
            exprType = isAddOrSubOperator ? checkExpr(unaryExpr.expr, newExpectedType, data) :
                    checkExpr(unaryExpr.expr, data);
        } else {
            exprType = isAddOrSubOperator ? checkExpr(unaryExpr.expr, data.expType, data) :
                    checkExpr(unaryExpr.expr, data);
        }

        if (exprType != symTable.semanticError) {
            BSymbol symbol = symResolver.resolveUnaryOperator(unaryExpr.operator, exprType);
            if (symbol == symTable.notFoundSymbol) {
                symbol = symResolver.getUnaryOpsForTypeSets(unaryExpr.operator, exprType);
            }
            if (symbol == symTable.notFoundSymbol) {
                dlog.error(unaryExpr.pos, DiagnosticErrorCode.UNARY_OP_INCOMPATIBLE_TYPES,
                        unaryExpr.operator, exprType);
            } else {
                unaryExpr.opSymbol = (BOperatorSymbol) symbol;
                actualType = symbol.type.getReturnType();
            }
        }

        // Explicitly set actual type
        if (isAddOrSubOperator && exprType != symTable.semanticError && types.isExpressionInUnaryValid(unaryExpr.expr)
                && (referredTypeTag == TypeTags.FINITE || referredTypeTag == TypeTags.UNION)) {
            if (referredTypeTag == TypeTags.FINITE) {
                actualType = createFiniteTypeForNumericUnaryExpr(unaryExpr, data);
            } else {
                if (silentCompatibleFiniteMembersInUnionTypeCheck(unaryExpr, (BUnionType) referredType, data)) {
                    return createFiniteTypeForNumericUnaryExpr(unaryExpr, data);
                }
                // We need to specifically check for int subtypes to set the correct actual type because we use the
                // basic type (int) when checking the expression.
                LinkedHashSet<BType> intTypesInUnion = getIntSubtypesInUnionType((BUnionType) referredType);
                if (!intTypesInUnion.isEmpty()) {
                    BType newReferredType = BUnionType.create(null, intTypesInUnion);
                    BType tempActualType = checkCompatibilityWithConstructedNumericLiteral(unaryExpr, newReferredType,
                            data);
                    if (tempActualType != symTable.semanticError) {
                        return  tempActualType;
                    }
                }
            }
        } else if (isAddOrSubOperator && exprType != symTable.semanticError &&
                TypeTags.isIntegerTypeTag(referredTypeTag) && referredTypeTag != TypeTags.INT
                && unaryExpr.expr.getKind() == NodeKind.NUMERIC_LITERAL) {
            BType tempActualType = checkCompatibilityWithConstructedNumericLiteral(unaryExpr, referredType, data);
            if (tempActualType != symTable.semanticError) {
                return  tempActualType;
            }
        }
        return actualType;
    }

    public BType checkCompatibilityWithConstructedNumericLiteral(BLangUnaryExpr unaryExpr, BType referredType,
                                                                 AnalyzerData data) {
        if (!types.isExpressionInUnaryValid(unaryExpr.expr)) {
            return silentTypeCheckExpr(unaryExpr.expr, referredType, data);
        }
        BLangNumericLiteral numericLiteral = Types.constructNumericLiteralFromUnaryExpr(unaryExpr);
        // To check value with sign against expected type
        return silentTypeCheckExpr(numericLiteral, referredType, data);
    }

    public LinkedHashSet getIntSubtypesInUnionType(BUnionType expectedType) {
        LinkedHashSet<BType> intTypesInUnion = new LinkedHashSet<>(expectedType.getMemberTypes().size());
        for (BType type : expectedType.getMemberTypes()) {
            BType referredType = types.getReferredType(type);
            if (type.tag != TypeTags.INT && TypeTags.isIntegerTypeTag(referredType.tag)) {
                intTypesInUnion.add(referredType);
            }
        }
        return intTypesInUnion;
    }

    public boolean silentCompatibleFiniteMembersInUnionTypeCheck(BLangUnaryExpr unaryExpr, BUnionType expectedType,
                                                              AnalyzerData data) {
        boolean prevNonErrorLoggingCheck = data.commonAnalyzerData.nonErrorLoggingCheck;
        data.commonAnalyzerData.nonErrorLoggingCheck = true;
        int prevErrorCount = this.dlog.errorCount();
        this.dlog.resetErrorCount();
        this.dlog.mute();

        BType compatibleTypeOfUnaryExpression;
        for (BType type : expectedType.getMemberTypes()) {
            compatibleTypeOfUnaryExpression = checkExpr(nodeCloner.cloneNode(unaryExpr), types.getReferredType(type),
                    data);
            if (compatibleTypeOfUnaryExpression.tag == TypeTags.FINITE) {
                unmuteDlog(data, prevNonErrorLoggingCheck, prevErrorCount);
                return true;
            }
        }
        unmuteDlog(data, prevNonErrorLoggingCheck, prevErrorCount);
        return false;
    }

    private void unmuteDlog(AnalyzerData data, boolean prevNonErrorLoggingCheck, int prevErrorCount) {
        data.commonAnalyzerData.nonErrorLoggingCheck = prevNonErrorLoggingCheck;
        this.dlog.setErrorCount(prevErrorCount);
        if (!prevNonErrorLoggingCheck) {
            this.dlog.unmute();
        }
    }

    public BType silentTypeCheckExpr(BLangExpression expr, BType referredType, AnalyzerData data) {
        boolean prevNonErrorLoggingCheck = data.commonAnalyzerData.nonErrorLoggingCheck;
        data.commonAnalyzerData.nonErrorLoggingCheck = true;
        int prevErrorCount = this.dlog.errorCount();
        this.dlog.resetErrorCount();
        this.dlog.mute();

        BType exprCompatibleType = checkExpr(nodeCloner.cloneNode(expr), referredType, data);

        unmuteDlog(data, prevNonErrorLoggingCheck, prevErrorCount);
        return exprCompatibleType;
    }

    public void visit(BLangUnaryExpr unaryExpr, AnalyzerData data) {
        BType exprType;

        BType actualType = symTable.semanticError;
        if (OperatorKind.UNTAINT.equals(unaryExpr.operator)) {
            exprType = checkExpr(unaryExpr.expr, data);
            if (exprType != symTable.semanticError) {
                actualType = exprType;
            }
        } else if (OperatorKind.TYPEOF.equals(unaryExpr.operator)) {
            exprType = checkExpr(unaryExpr.expr, data);
            if (exprType != symTable.semanticError) {
                actualType = new BTypedescType(exprType, null);
            }
        } else {
            actualType = getActualTypeForOtherUnaryExpr(unaryExpr, data);
        }
        data.resultType = types.checkType(unaryExpr, actualType, data.expType);
    }

    public void visit(BLangTypeConversionExpr conversionExpr, AnalyzerData data) {
        // Set error type as the actual type.
        BType actualType = symTable.semanticError;

        for (BLangAnnotationAttachment annAttachment : conversionExpr.annAttachments) {
            annAttachment.attachPoints.add(AttachPoint.Point.TYPE);
            semanticAnalyzer.analyzeNode(annAttachment, data.env);
        }

        // Annotation such as <@untainted [T]>, where T is not provided,
        // it's merely a annotation on contextually expected type.
        BLangExpression expr = conversionExpr.expr;
        if (conversionExpr.typeNode == null) {
            if (!conversionExpr.annAttachments.isEmpty()) {
                data.resultType = checkExpr(expr, data.expType, data);
            }
            return;
        }

        // If typeNode is of finite type with unary expressions in the value space, we need to
        // convert them into numeric literals.
        if (conversionExpr.typeNode.getKind() == NodeKind.FINITE_TYPE_NODE) {
            semanticAnalyzer.analyzeNode(conversionExpr.typeNode, data.env);
        }

        BType targetType = getEffectiveReadOnlyType(conversionExpr.typeNode.pos,
                                                  symResolver.resolveTypeNode(conversionExpr.typeNode, data.env), data);

        conversionExpr.targetType = targetType;

        boolean prevNonErrorLoggingCheck = data.commonAnalyzerData.nonErrorLoggingCheck;
        data.commonAnalyzerData.nonErrorLoggingCheck = true;
        int prevErrorCount = this.dlog.errorCount();
        this.dlog.resetErrorCount();
        this.dlog.mute();

        BType exprCompatibleType = checkExpr(nodeCloner.cloneNode(expr), targetType, data);
        data.commonAnalyzerData.nonErrorLoggingCheck = prevNonErrorLoggingCheck;
        int errorCount = this.dlog.errorCount();
        this.dlog.setErrorCount(prevErrorCount);

        if (!prevNonErrorLoggingCheck) {
            this.dlog.unmute();
        }

        if ((errorCount == 0 && exprCompatibleType != symTable.semanticError) ||
                (requireTypeInference(expr, false) &&
                        // Temporary workaround for backward compatibility with `object {}` for
                        // https://github.com/ballerina-platform/ballerina-lang/issues/38105.
                        isNotObjectConstructorWithObjectSuperTypeInTypeCastExpr(expr, targetType))) {
            checkExpr(expr, targetType, data);
        } else {
            checkExpr(expr, symTable.noType, data);
        }

        BType exprType = expr.getBType();
        if (types.isTypeCastable(expr, exprType, targetType, data.env)) {
            // We reach this block only if the cast is valid, so we set the target type as the actual type.
            actualType = targetType;
        } else if (exprType != symTable.semanticError && exprType != symTable.noType) {
            dlog.error(conversionExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES_CAST, exprType, targetType);
        }
        data.resultType = types.checkType(conversionExpr, actualType, data.expType);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction, AnalyzerData data) {
        SymbolEnv currentEnv = data.env;
        if (data.commonAnalyzerData.nonErrorLoggingCheck) {
            BLangFunction funcNode = bLangLambdaFunction.function;
            BInvokableSymbol funcSymbol = Symbols.createFunctionSymbol(Flags.asMask(funcNode.flagSet),
                                                                       names.fromIdNode(funcNode.name), Names.EMPTY,
                                                                       currentEnv.enclPkg.symbol.pkgID, null,
                                                                       currentEnv.scope.owner, funcNode.hasBody(),
                                                                       funcNode.pos, VIRTUAL);
            funcSymbol.scope = new Scope(funcSymbol);
            SymbolEnv invokableEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope, currentEnv);
            invokableEnv.scope = funcSymbol.scope;
            symbolEnter.defineInvokableSymbolParams(bLangLambdaFunction.function, funcSymbol, invokableEnv);
            funcNode.setBType(funcSymbol.type);
        } else if (bLangLambdaFunction.function.symbol == null) {
            symbolEnter.defineNode(bLangLambdaFunction.function, currentEnv);
        }
        bLangLambdaFunction.setBType(bLangLambdaFunction.function.getBType());
        // creating a copy of the env to visit the lambda function later
        bLangLambdaFunction.capturedClosureEnv = data.env.createClone();

        if (!data.commonAnalyzerData.nonErrorLoggingCheck) {
            if (bLangLambdaFunction.function.flagSet.contains(Flag.WORKER)) {
                currentEnv.enclPkg.lambdaFunctions.add(bLangLambdaFunction);
            } else {
                semanticAnalyzer.analyzeNode(bLangLambdaFunction.function, bLangLambdaFunction.capturedClosureEnv);
            }
       }

        data.resultType = types.checkType(bLangLambdaFunction, bLangLambdaFunction.getBType(), data.expType);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction, AnalyzerData data) {
        BType expectedType = Types.getReferredType(data.expType);
        if (expectedType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) expectedType;
            BType invokableType = unionType.getMemberTypes().stream().filter(type -> type.tag == TypeTags.INVOKABLE)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                                if (list.size() != 1) {
                                    return null;
                                }
                                return list.get(0);
                            }
                    ));

            if (invokableType != null) {
                expectedType = invokableType;
            }
        }
        if (expectedType.tag != TypeTags.INVOKABLE || Symbols.isFlagOn(expectedType.flags, Flags.ANY_FUNCTION)) {
            dlog.error(bLangArrowFunction.pos,
                    DiagnosticErrorCode.ARROW_EXPRESSION_CANNOT_INFER_TYPE_FROM_LHS);
            data.resultType = symTable.semanticError;
            return;
        }

        BInvokableType expectedInvocation = (BInvokableType) expectedType;
        populateArrowExprParamTypes(bLangArrowFunction, expectedInvocation.paramTypes, data);
        bLangArrowFunction.body.expr.setBType(populateArrowExprReturn(bLangArrowFunction, expectedInvocation.retType,
                                              data));
        // if function return type is none, assign the inferred return type
        if (expectedInvocation.retType.tag == TypeTags.NONE) {
            expectedInvocation.retType = bLangArrowFunction.body.expr.getBType();
        }
        for (BLangSimpleVariable simpleVariable : bLangArrowFunction.params) {
            if (simpleVariable.symbol != null) {
                symResolver.checkForUniqueSymbol(simpleVariable.pos, data.env, simpleVariable.symbol);
            }
        }
        data.resultType = bLangArrowFunction.funcType = expectedInvocation;
    }

    public void visit(BLangXMLQName bLangXMLQName, AnalyzerData data) {
        String prefix = bLangXMLQName.prefix.value;
        data.resultType = types.checkType(bLangXMLQName, symTable.stringType, data.expType);
        // TODO: check isLHS

        if (data.env.node.getKind() == NodeKind.XML_ATTRIBUTE && prefix.isEmpty()
                && bLangXMLQName.localname.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            ((BLangXMLAttribute) data.env.node).isNamespaceDeclr = true;
            return;
        }

        if (data.env.node.getKind() == NodeKind.XML_ATTRIBUTE && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            ((BLangXMLAttribute) data.env.node).isNamespaceDeclr = true;
            return;
        }

        if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            dlog.error(bLangXMLQName.pos, DiagnosticErrorCode.INVALID_NAMESPACE_PREFIX, prefix);
            bLangXMLQName.setBType(symTable.semanticError);
            return;
        }

        // XML attributes without a namespace prefix does not inherit default namespace
        // https://www.w3.org/TR/xml-names/#defaulting
        if (bLangXMLQName.prefix.value.isEmpty()) {
            return;
        }

        BSymbol xmlnsSymbol = symResolver.lookupSymbolInPrefixSpace(data.env, names.fromIdNode(bLangXMLQName.prefix));
        if (prefix.isEmpty() && xmlnsSymbol == symTable.notFoundSymbol) {
            return;
        }

        if (!prefix.isEmpty() && xmlnsSymbol == symTable.notFoundSymbol) {
            logUndefinedSymbolError(bLangXMLQName.pos, prefix);
            bLangXMLQName.setBType(symTable.semanticError);
            return;
        }

        if (xmlnsSymbol.getKind() == SymbolKind.PACKAGE) {
            xmlnsSymbol = findXMLNamespaceFromPackageConst(bLangXMLQName.localname.value, bLangXMLQName.prefix.value,
                    (BPackageSymbol) xmlnsSymbol, bLangXMLQName.pos, data);
        }

        if (xmlnsSymbol == null || xmlnsSymbol.getKind() != SymbolKind.XMLNS) {
            data.resultType = symTable.semanticError;
            return;
        }

        bLangXMLQName.nsSymbol = (BXMLNSSymbol) xmlnsSymbol;
        bLangXMLQName.namespaceURI = bLangXMLQName.nsSymbol.namespaceURI;
    }

    private BSymbol findXMLNamespaceFromPackageConst(String localname, String prefix,
                                                     BPackageSymbol pkgSymbol, Location pos, AnalyzerData data) {
        // Resolve a const from module scope.
        BSymbol constSymbol = symResolver.lookupMemberSymbol(pos, pkgSymbol.scope, data.env,
                names.fromString(localname), SymTag.CONSTANT);
        if (constSymbol == symTable.notFoundSymbol) {
            if (!missingNodesHelper.isMissingNode(prefix) && !missingNodesHelper.isMissingNode(localname)) {
                dlog.error(pos, DiagnosticErrorCode.UNDEFINED_SYMBOL, prefix + ":" + localname);
            }
            return null;
        }

        // If Resolved const is not a string, it is an error.
        BConstantSymbol constantSymbol = (BConstantSymbol) constSymbol;
        if (constantSymbol.literalType.tag != TypeTags.STRING) {
            dlog.error(pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, symTable.stringType, constantSymbol.literalType);
            return null;
        }

        // If resolve const contain a string in {namespace url}local form extract namespace uri and local part.
        String constVal = (String) constantSymbol.value.value;
        int s = constVal.indexOf('{');
        int e = constVal.lastIndexOf('}');
        if (e > s + 1) {
            pkgSymbol.isUsed = true;
            String nsURI = constVal.substring(s + 1, e);
            String local = constVal.substring(e);
            return new BXMLNSSymbol(names.fromString(local), nsURI, constantSymbol.pkgID, constantSymbol.owner, pos,
                                    SOURCE);
        }

        // Resolved const string is not in valid format.
        dlog.error(pos, DiagnosticErrorCode.INVALID_ATTRIBUTE_REFERENCE, prefix + ":" + localname);
        return null;
    }

    public void visit(BLangXMLAttribute bLangXMLAttribute, AnalyzerData data) {
        SymbolEnv xmlAttributeEnv = SymbolEnv.getXMLAttributeEnv(bLangXMLAttribute, data.env);

        // check attribute name
        BLangXMLQName name = (BLangXMLQName) bLangXMLAttribute.name;
        checkExpr(name, xmlAttributeEnv, symTable.stringType, data);
        // XML attributes without a prefix does not belong to enclosing elements default namespace.
        // https://www.w3.org/TR/xml-names/#uniqAttrs
        if (name.prefix.value.isEmpty()) {
            name.namespaceURI = null;
        }

        // check attribute value
        checkExpr(bLangXMLAttribute.value, xmlAttributeEnv, symTable.stringType, data);

        symbolEnter.defineNode(bLangXMLAttribute, data.env);
    }

    public void visit(BLangXMLElementLiteral bLangXMLElementLiteral, AnalyzerData data) {
        SymbolEnv xmlElementEnv = SymbolEnv.getXMLElementEnv(bLangXMLElementLiteral, data.env);

        // Keep track of used namespace prefixes in this element and only add namespace attr for those used ones.
        Set<String> usedPrefixes = new HashSet<>();
        BLangIdentifier elemNamePrefix = ((BLangXMLQName) bLangXMLElementLiteral.startTagName).prefix;
        if (elemNamePrefix != null && !elemNamePrefix.value.isEmpty()) {
            usedPrefixes.add(elemNamePrefix.value);
        }

        // Visit in-line namespace declarations and define the namespace.
        for (BLangXMLAttribute attribute : bLangXMLElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME && isXmlNamespaceAttribute(attribute)) {
                BLangXMLQuotedString value = attribute.value;
                if (value.getKind() == NodeKind.XML_QUOTED_STRING && value.textFragments.size() > 1) {
                    dlog.error(value.pos, DiagnosticErrorCode.INVALID_XML_NS_INTERPOLATION);
                }
                checkExpr(attribute, xmlElementEnv, symTable.noType, data);
            }
            BLangIdentifier prefix = ((BLangXMLQName) attribute.name).prefix;
            if (prefix != null && !prefix.value.isEmpty()) {
                usedPrefixes.add(prefix.value);
            }
        }

        // Visit attributes, this may depend on the namespace defined in previous attribute iteration.
        bLangXMLElementLiteral.attributes.forEach(attribute -> {
            if (!(attribute.name.getKind() == NodeKind.XML_QNAME && isXmlNamespaceAttribute(attribute))) {
                checkExpr(attribute, xmlElementEnv, symTable.noType, data);
            }
        });

        Map<Name, BXMLNSSymbol> namespaces = symResolver.resolveAllNamespaces(xmlElementEnv);
        Name defaultNs = names.fromString(XMLConstants.DEFAULT_NS_PREFIX);
        if (namespaces.containsKey(defaultNs)) {
            bLangXMLElementLiteral.defaultNsSymbol = namespaces.remove(defaultNs);
        }
        for (Map.Entry<Name, BXMLNSSymbol> nsEntry : namespaces.entrySet()) {
            if (usedPrefixes.contains(nsEntry.getKey().value)) {
                bLangXMLElementLiteral.namespacesInScope.put(nsEntry.getKey(), nsEntry.getValue());
            }
        }

        // Visit the tag names
        validateTags(bLangXMLElementLiteral, xmlElementEnv, data);

        // Visit the children
        bLangXMLElementLiteral.modifiedChildren =
                concatSimilarKindXMLNodes(bLangXMLElementLiteral.children, xmlElementEnv, data);

        if (data.expType == symTable.noType) {
            data.resultType = types.checkType(bLangXMLElementLiteral, symTable.xmlElementType, data.expType);
            return;
        }

        data.resultType = checkXmlSubTypeLiteralCompatibility(bLangXMLElementLiteral.pos, symTable.xmlElementType,
                                                         data.expType, data);

        if (Symbols.isFlagOn(data.resultType.flags, Flags.READONLY)) {
            markChildrenAsImmutable(bLangXMLElementLiteral, data);
        }
    }

    private boolean isXmlNamespaceAttribute(BLangXMLAttribute attribute) {
        BLangXMLQName attrName = (BLangXMLQName) attribute.name;
        return (attrName.prefix.value.isEmpty()
                    && attrName.localname.value.equals(XMLConstants.XMLNS_ATTRIBUTE))
                || attrName.prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE);
    }

    public BType getXMLSequenceType(BType xmlSubType) {
        switch (xmlSubType.tag) {
            case TypeTags.XML_ELEMENT:
                return new BXMLType(symTable.xmlElementType,  null);
            case TypeTags.XML_COMMENT:
                return new BXMLType(symTable.xmlCommentType,  null);
            case TypeTags.XML_PI:
                return new BXMLType(symTable.xmlPIType,  null);
            default:
                // Since 'xml:Text is same as xml<'xml:Text>
                return symTable.xmlTextType;
        }
    }

    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral, AnalyzerData data) {
        BType expType = Types.getReferredType(data.expType);
        if (expType.tag != TypeTags.XML && expType.tag != TypeTags.UNION && expType.tag != TypeTags.XML_TEXT
        && expType != symTable.noType) {
            dlog.error(bLangXMLSequenceLiteral.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data.expType,
                    "XML Sequence");
            data.resultType = symTable.semanticError;
            return;
        }

        List<BType> xmlTypesInSequence = new ArrayList<>();

        for (BLangExpression expressionItem : bLangXMLSequenceLiteral.xmlItems) {
            data.resultType = checkExpr(expressionItem, data.expType, data);
            if (!xmlTypesInSequence.contains(data.resultType)) {
                xmlTypesInSequence.add(data.resultType);
            }
        }

        // Set type according to items in xml sequence and expected type
        if (expType.tag == TypeTags.XML || expType == symTable.noType) {
            if (xmlTypesInSequence.size() == 1) {
                data.resultType = getXMLSequenceType(xmlTypesInSequence.get(0));
                return;
            }
            data.resultType = symTable.xmlType;
            return;
        }
        // Since 'xml:Text is same as xml<'xml:Text>
        if (expType.tag == TypeTags.XML_TEXT) {
            data.resultType = symTable.xmlTextType;
            return;
        }
        // Disallow unions with 'xml:T (singleton) items
         for (BType item : ((BUnionType) expType).getMemberTypes()) {
             item = Types.getReferredType(item);
             if (item.tag != TypeTags.XML_TEXT && item.tag != TypeTags.XML) {
                 dlog.error(bLangXMLSequenceLiteral.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                         expType, symTable.xmlType);
                 data.resultType = symTable.semanticError;
                 return;
             }
         }
        data.resultType = symTable.xmlType;
    }

    public void visit(BLangXMLTextLiteral bLangXMLTextLiteral, AnalyzerData data) {
        List<BLangExpression> literalValues = bLangXMLTextLiteral.textFragments;
        checkStringTemplateExprs(literalValues, data);
        BLangExpression xmlExpression = literalValues.get(0);
        if (literalValues.size() == 1 && xmlExpression.getKind() == NodeKind.LITERAL &&
                ((String) ((BLangLiteral) xmlExpression).value).isEmpty()) {
            data.resultType = types.checkType(bLangXMLTextLiteral, symTable.xmlNeverType, data.expType);
            return;
        }
        data.resultType = types.checkType(bLangXMLTextLiteral, symTable.xmlTextType, data.expType);
    }

    public void visit(BLangXMLCommentLiteral bLangXMLCommentLiteral, AnalyzerData data) {
        checkStringTemplateExprs(bLangXMLCommentLiteral.textFragments, data);

        if (data.expType == symTable.noType) {
            data.resultType = types.checkType(bLangXMLCommentLiteral, symTable.xmlCommentType, data.expType);
            return;
        }
        data.resultType = checkXmlSubTypeLiteralCompatibility(bLangXMLCommentLiteral.pos, symTable.xmlCommentType,
                                                         data.expType, data);
    }

    public void visit(BLangXMLProcInsLiteral bLangXMLProcInsLiteral, AnalyzerData data) {
        checkExpr(bLangXMLProcInsLiteral.target, symTable.stringType, data);
        checkStringTemplateExprs(bLangXMLProcInsLiteral.dataFragments, data);
        if (data.expType == symTable.noType) {
            data.resultType = types.checkType(bLangXMLProcInsLiteral, symTable.xmlPIType, data.expType);
            return;
        }
        data.resultType =
                checkXmlSubTypeLiteralCompatibility(bLangXMLProcInsLiteral.pos, symTable.xmlPIType, data.expType, data);
    }

    public void visit(BLangXMLQuotedString bLangXMLQuotedString, AnalyzerData data) {
        checkStringTemplateExprs(bLangXMLQuotedString.textFragments, data);
        data.resultType = types.checkType(bLangXMLQuotedString, symTable.stringType, data.expType);
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral, AnalyzerData data) {
        checkStringTemplateExprs(stringTemplateLiteral.exprs, data);
        data.resultType = types.checkType(stringTemplateLiteral, symTable.stringType, data.expType);
    }

    @Override
    public void visit(BLangRegExpTemplateLiteral regExpTemplateLiteral, AnalyzerData data) {
        // Check expr with insertions to resolve its type.
        List<BLangExpression> interpolationsList =
                symResolver.getListOfInterpolations(regExpTemplateLiteral.reDisjunction.sequenceList);
        interpolationsList.forEach(interpolation -> checkExpr(interpolation, data));
        data.resultType = types.checkType(regExpTemplateLiteral, symTable.regExpType, data.expType);
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral, AnalyzerData data) {
        // First, ensure that the contextually expected type is compatible with the RawTemplate type.
        // The RawTemplate type should have just two fields: strings and insertions. There shouldn't be any methods.
        BType type = determineRawTemplateLiteralType(rawTemplateLiteral, data.expType);

        if (type == symTable.semanticError) {
            data.resultType = type;
            return;
        }

        // Once we ensure the types are compatible, need to ensure that the types of the strings and insertions are
        // compatible with the types of the strings and insertions fields.
        BObjectType literalType = (BObjectType) Types.getReferredType(type);
        BType stringsType = literalType.fields.get("strings").type;

        if (evaluateRawTemplateExprs(rawTemplateLiteral.strings, stringsType, INVALID_NUM_STRINGS,
                                     rawTemplateLiteral.pos, data)) {
            type = symTable.semanticError;
        }

        BType insertionsType = literalType.fields.get("insertions").type;

        if (evaluateRawTemplateExprs(rawTemplateLiteral.insertions, insertionsType, INVALID_NUM_INSERTIONS,
                                     rawTemplateLiteral.pos, data)) {
            type = symTable.semanticError;
        }

        data.resultType = type;
    }

    private BType determineRawTemplateLiteralType(BLangRawTemplateLiteral rawTemplateLiteral, BType expType) {
        // Contextually expected type is NoType when `var` is used. When `var` is used, the literal is considered to
        // be of type `RawTemplate`.
        if (expType == symTable.noType || containsAnyType(expType)) {
            return symTable.rawTemplateType;
        }

        BType compatibleType = getCompatibleRawTemplateType(expType, rawTemplateLiteral.pos);
        BType type = types.checkType(rawTemplateLiteral, compatibleType, symTable.rawTemplateType,
                DiagnosticErrorCode.INVALID_RAW_TEMPLATE_TYPE);

        if (type == symTable.semanticError) {
            return type;
        }

        // Raw template literals can be directly assigned only to abstract object types
        if (Symbols.isFlagOn(type.tsymbol.flags, Flags.CLASS)) {
            dlog.error(rawTemplateLiteral.pos, DiagnosticErrorCode.INVALID_RAW_TEMPLATE_ASSIGNMENT, type);
            return symTable.semanticError;
        }

        // Ensure that only the two fields, strings and insertions, are there
        BObjectType litObjType = (BObjectType) Types.getReferredType(type);
        BObjectTypeSymbol objTSymbol = (BObjectTypeSymbol) litObjType.tsymbol;

        if (litObjType.fields.size() > 2) {
            dlog.error(rawTemplateLiteral.pos, DiagnosticErrorCode.INVALID_NUM_FIELDS, litObjType);
            type = symTable.semanticError;
        }

        if (!objTSymbol.attachedFuncs.isEmpty()) {
            dlog.error(rawTemplateLiteral.pos, DiagnosticErrorCode.METHODS_NOT_ALLOWED, litObjType);
            type = symTable.semanticError;
        }

        return type;
    }

    private boolean evaluateRawTemplateExprs(List<? extends BLangExpression> exprs, BType fieldType,
                                             DiagnosticCode code, Location pos, AnalyzerData data) {
        BType listType = Types.getReferredType(fieldType);

        listType = listType.tag != TypeTags.INTERSECTION ? listType :
                ((BIntersectionType) listType).effectiveType;

        boolean errored = false;

        if (listType.tag == TypeTags.ARRAY) {
            BArrayType arrayType = (BArrayType) listType;

            if (arrayType.state == BArrayState.CLOSED && (exprs.size() != arrayType.size)) {
                dlog.error(pos, code, arrayType.size, exprs.size());
                return false;
            }

            for (BLangExpression expr : exprs) {
                errored = (checkExpr(expr, arrayType.eType, data) == symTable.semanticError) || errored;
            }
        } else if (listType.tag == TypeTags.TUPLE) {
            BTupleType tupleType = (BTupleType) listType;
            final int size = exprs.size();
            final int requiredItems = tupleType.getMembers().size();

            if (size < requiredItems || (size > requiredItems && tupleType.restType == null)) {
                dlog.error(pos, code, requiredItems, size);
                return false;
            }

            int i;
            List<BType> memberTypes = tupleType.getTupleTypes();
            for (i = 0; i < requiredItems; i++) {
                errored = (checkExpr(exprs.get(i), memberTypes.get(i), data) == symTable.semanticError) ||
                                                                                                                errored;
            }

            if (size > requiredItems) {
                for (; i < size; i++) {
                    errored = (checkExpr(exprs.get(i), tupleType.restType, data) == symTable.semanticError) ||
                                                                                                                errored;
                }
            }
        } else {
            throw new IllegalStateException("Expected a list type, but found: " + listType);
        }

        return errored;
    }

    private boolean containsAnyType(BType bType) {
        BType type = Types.getReferredType(bType);
        if (type == symTable.anyType) {
            return true;
        }

        if (type.tag == TypeTags.UNION) {
            return ((BUnionType) type).getMemberTypes().contains(symTable.anyType);
        }

        return false;
    }

    private BType getCompatibleRawTemplateType(BType bType, Location pos) {
        BType expType = Types.getReferredType(bType);
        if (expType.tag != TypeTags.UNION) {
            return bType;
        }

        BUnionType unionType = (BUnionType) expType;
        List<BType> compatibleTypes = new ArrayList<>();

        for (BType type : unionType.getMemberTypes()) {
            if (types.isAssignable(type, symTable.rawTemplateType)) {
                compatibleTypes.add(type);
            }
        }

        if (compatibleTypes.size() == 0) {
            return expType;
        }

        if (compatibleTypes.size() > 1) {
            dlog.error(pos, DiagnosticErrorCode.MULTIPLE_COMPATIBLE_RAW_TEMPLATE_TYPES, symTable.rawTemplateType,
                       expType);
            return symTable.semanticError;
        }

        return compatibleTypes.get(0);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangRestArgExpression, AnalyzerData data) {
        data.resultType = checkExpr(bLangRestArgExpression.expr, data.expType, data);
    }

    @Override
    public void visit(BLangInferredTypedescDefaultNode inferTypedescExpr, AnalyzerData data) {
        BType referredType = Types.getReferredType(data.expType);
        if (referredType.tag != TypeTags.TYPEDESC) {
            dlog.error(inferTypedescExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data.expType, symTable.typeDesc);
            data.resultType = symTable.semanticError;
            return;
        }
        data.resultType = referredType;
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression, AnalyzerData data) {
        data.resultType = checkExpr(bLangNamedArgsExpression.expr, data.env, data.expType, data);
        bLangNamedArgsExpression.setBType(bLangNamedArgsExpression.expr.getBType());
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr, AnalyzerData data) {
        visitCheckAndCheckPanicExpr(checkedExpr, data);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkedExpr, AnalyzerData data) {
        visitCheckAndCheckPanicExpr(checkedExpr, data);
    }

    @Override
    public void visit(BLangQueryExpr queryExpr, AnalyzerData data) {
        queryTypeChecker.checkQueryType(queryExpr, data);
    }

    @Override
    public void visit(BLangQueryAction queryAction, AnalyzerData data) {
        queryTypeChecker.checkQueryAction(queryAction, data);
    }

    @Override
    public void visit(BLangDo doNode, AnalyzerData data) {
        if (doNode.onFailClause != null) {
            doNode.onFailClause.accept(this, data);
        }
    }

    public void visit(BLangOnFailClause onFailClause, AnalyzerData data) {
        onFailClause.body.stmts.forEach(stmt -> stmt.accept(this, data));
    }

    protected void visitCheckAndCheckPanicExpr(BLangCheckedExpr checkedExpr, AnalyzerData data) {
        String operatorType = checkedExpr.getKind() == NodeKind.CHECK_EXPR ? "check" : "checkpanic";
        BLangExpression exprWithCheckingKeyword = checkedExpr.expr;
        boolean firstVisit = exprWithCheckingKeyword.getBType() == null;

        BType checkExprCandidateType;
        if (data.expType == symTable.noType) {
            checkExprCandidateType = symTable.noType;
        } else {
            BType exprType = getCandidateType(checkedExpr, data.expType, data);
            if (exprType == symTable.semanticError) {
                checkExprCandidateType = BUnionType.create(null, data.expType, symTable.errorType);
            } else {
                checkExprCandidateType = addDefaultErrorIfNoErrorComponentFound(data.expType);
            }
        }

        if (checkedExpr.getKind() == NodeKind.CHECK_EXPR && types.isUnionOfSimpleBasicTypes(data.expType)) {
            rewriteWithEnsureTypeFunc(checkedExpr, checkExprCandidateType, data);
        }

        BType exprType = checkExpr(checkedExpr.expr, checkExprCandidateType, data);
        if (checkedExpr.expr.getKind() == NodeKind.WORKER_RECEIVE) {
            if (firstVisit) {
                data.isTypeChecked = false;
                data.resultType = data.expType;
                return;
            } else {
                data.expType = checkedExpr.getBType();
                exprType = checkedExpr.expr.getBType();
            }
        }

        boolean isErrorType = types.isAssignable(Types.getReferredType(exprType), symTable.errorType);
        if (Types.getReferredType(exprType).tag != TypeTags.UNION && !isErrorType) {
            if (exprType.tag == TypeTags.READONLY) {
                checkedExpr.equivalentErrorTypeList = new ArrayList<>(1) {{
                    add(symTable.errorType);
                }};
                data.resultType = symTable.anyAndReadonly;
                return;
            } else if (exprType != symTable.semanticError) {
                dlog.warning(checkedExpr.expr.pos,
                        DiagnosticWarningCode.CHECKED_EXPR_INVALID_USAGE_NO_ERROR_TYPE_IN_RHS,
                        operatorType);
                checkedExpr.isRedundantChecking = true;
                data.resultType = checkedExpr.expr.getBType();

                // Reset impConversionExpr as it was previously based on default error added union type
                resetImpConversionExpr(checkedExpr.expr, data.resultType, data.expType);
            }
            checkedExpr.setBType(symTable.semanticError);
            return;
        }

        // Filter out the list of types which are not equivalent with the error type.
        List<BType> errorTypes = new ArrayList<>();
        List<BType> nonErrorTypes = new ArrayList<>();
        if (!isErrorType) {
            for (BType memberType : types.getAllTypes(exprType, true)) {
                if (memberType.tag == TypeTags.READONLY) {
                    errorTypes.add(symTable.errorType);
                    nonErrorTypes.add(symTable.anyAndReadonly);
                    continue;
                }
                if (types.isAssignable(memberType, symTable.errorType)) {
                    errorTypes.add(memberType);
                    continue;
                }
                nonErrorTypes.add(memberType);
            }
        } else {
            errorTypes.add(exprType);
        }

        // This list will be used in the desugar phase
        checkedExpr.equivalentErrorTypeList = errorTypes;
        if (errorTypes.isEmpty()) {
            // No member types in this union is equivalent to the error type
            dlog.warning(checkedExpr.expr.pos,
                    DiagnosticWarningCode.CHECKED_EXPR_INVALID_USAGE_NO_ERROR_TYPE_IN_RHS, operatorType);
            checkedExpr.isRedundantChecking = true;

            // Reset impConversionExpr as it was previously based on default error added union type
            resetImpConversionExpr(checkedExpr.expr, data.resultType, data.expType);

            checkedExpr.setBType(symTable.semanticError);
            return;
        }

        BType actualType;
        if (nonErrorTypes.size() == 0) {
            actualType = symTable.neverType;
        } else if (nonErrorTypes.size() == 1) {
            actualType = nonErrorTypes.get(0);
        } else {
            actualType = BUnionType.create(null, new LinkedHashSet<>(nonErrorTypes));
        }

        data.resultType = types.checkType(checkedExpr, actualType, data.expType);
    }

    private void resetImpConversionExpr(BLangExpression expr, BType actualType, BType targetType) {
        expr.impConversionExpr = null;
        types.setImplicitCastExpr(expr, actualType, targetType);
    }

    private void rewriteWithEnsureTypeFunc(BLangCheckedExpr checkedExpr, BType type, AnalyzerData data) {
        BType rhsType = getCandidateType(checkedExpr, type, data);
        if (rhsType == symTable.semanticError) {
            rhsType = getCandidateType(checkedExpr, rhsType, data);
        }
        BType candidateLaxType = getCandidateLaxType(checkedExpr.expr, rhsType);
        if (!types.isLax(candidateLaxType)) {
            return;
        }
        ArrayList<BLangExpression> argExprs = new ArrayList<>();
        BType typedescType = new BTypedescType(data.expType, null);
        BLangTypedescExpr typedescExpr = new BLangTypedescExpr();
        typedescExpr.resolvedType = data.expType;
        typedescExpr.setBType(typedescType);
        argExprs.add(typedescExpr);
        BLangInvocation invocation = ASTBuilderUtil.createLangLibInvocationNode(FUNCTION_NAME_ENSURE_TYPE,
                argExprs, checkedExpr.expr, checkedExpr.pos);
        invocation.symbol = symResolver.lookupLangLibMethod(type, names.fromString(invocation.name.value), data.env);
        invocation.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        checkedExpr.expr = invocation;
    }

    private BType getCandidateLaxType(BLangNode expr, BType rhsType) {
        if (expr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
            return types.getSafeType(rhsType, false, true);
        }
        return rhsType;
    }

    private BType getCandidateType(BLangCheckedExpr checkedExpr, BType checkExprCandidateType, AnalyzerData data) {
        boolean prevNonErrorLoggingCheck = data.commonAnalyzerData.nonErrorLoggingCheck;
        data.commonAnalyzerData.nonErrorLoggingCheck = true;
        int prevErrorCount = this.dlog.errorCount();
        this.dlog.resetErrorCount();
        this.dlog.mute();

        checkedExpr.expr.cloneAttempt++;
        BLangExpression clone = nodeCloner.cloneNode(checkedExpr.expr);
        BType rhsType;
        if (checkExprCandidateType == symTable.semanticError) {
            rhsType = checkExpr(clone, data);
        } else {
            rhsType = checkExpr(clone, checkExprCandidateType, data);
        }
        data.commonAnalyzerData.nonErrorLoggingCheck = prevNonErrorLoggingCheck;
        this.dlog.setErrorCount(prevErrorCount);
        if (!prevNonErrorLoggingCheck) {
            this.dlog.unmute();
        }
        return rhsType;
    }

    private BType addDefaultErrorIfNoErrorComponentFound(BType type) {
        for (BType t : types.getAllTypes(type, false)) {
            if (types.isAssignable(t, symTable.errorType)) {
                return type;
            }
        }
        return BUnionType.create(null, type, symTable.errorType);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr, AnalyzerData data) {
        data.resultType = serviceConstructorExpr.serviceNode.symbol.type;
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr, AnalyzerData data) {
        typeTestExpr.typeNode.setBType(symResolver.resolveTypeNode(typeTestExpr.typeNode, data.env));
        checkExpr(typeTestExpr.expr, data);

        data.resultType = types.checkType(typeTestExpr, symTable.booleanType, data.expType);
    }

    public void visit(BLangAnnotAccessExpr annotAccessExpr, AnalyzerData data) {
        checkExpr(annotAccessExpr.expr, symTable.typeDesc, data);

        BType actualType = symTable.semanticError;
        BSymbol symbol =
                this.symResolver.resolveAnnotation(annotAccessExpr.pos, data.env,
                        names.fromString(annotAccessExpr.pkgAlias.getValue()),
                        names.fromString(annotAccessExpr.annotationName.getValue()));
        if (symbol == this.symTable.notFoundSymbol) {
            this.dlog.error(annotAccessExpr.pos, DiagnosticErrorCode.UNDEFINED_ANNOTATION,
                    annotAccessExpr.annotationName.getValue());
        } else {
            annotAccessExpr.annotationSymbol = (BAnnotationSymbol) symbol;
            BType annotType = ((BAnnotationSymbol) symbol).attachedType == null ? symTable.trueType :
                    ((BAnnotationSymbol) symbol).attachedType;
            actualType = BUnionType.create(null, annotType, symTable.nilType);
        }

        data.resultType = this.types.checkType(annotAccessExpr, actualType, data.expType);
    }

    // Private methods

    private boolean isValidVariableReference(BLangExpression varRef) {
        switch (varRef.getKind()) {
            case SIMPLE_VARIABLE_REF:
            case RECORD_VARIABLE_REF:
            case TUPLE_VARIABLE_REF:
            case ERROR_VARIABLE_REF:
            case FIELD_BASED_ACCESS_EXPR:
            case INDEX_BASED_ACCESS_EXPR:
            case XML_ATTRIBUTE_ACCESS_EXPR:
                return true;
            default:
                dlog.error(varRef.pos, DiagnosticErrorCode.INVALID_RECORD_BINDING_PATTERN, varRef.getBType());
                return false;
        }
    }

    private BType getEffectiveReadOnlyType(Location pos, BType type, AnalyzerData data) {
        BType origTargetType = Types.getReferredType(type);
        if (origTargetType == symTable.readonlyType) {
            if (types.isInherentlyImmutableType(data.expType) ||
                    !types.isSelectivelyImmutableType(data.expType, data.env.enclPkg.packageID)) {
                return origTargetType;
            }

            return ImmutableTypeCloner.getImmutableIntersectionType(pos, types, data.expType, data.env, symTable,
                    anonymousModelHelper, names, new HashSet<>());
        }

        if (origTargetType.tag != TypeTags.UNION) {
            return origTargetType;
        }

        boolean hasReadOnlyType = false;

        LinkedHashSet<BType> nonReadOnlyTypes = new LinkedHashSet<>();

        for (BType memberType : ((BUnionType) origTargetType).getMemberTypes()) {
            if (memberType == symTable.readonlyType) {
                hasReadOnlyType = true;
                continue;
            }

            nonReadOnlyTypes.add(memberType);
        }

        if (!hasReadOnlyType) {
            return origTargetType;
        }

        if (types.isInherentlyImmutableType(data.expType) ||
                !types.isSelectivelyImmutableType(data.expType, data.env.enclPkg.packageID)) {
            return origTargetType;
        }

        BUnionType nonReadOnlyUnion = BUnionType.create(null, nonReadOnlyTypes);

        nonReadOnlyUnion.add(ImmutableTypeCloner.getImmutableIntersectionType(pos, types, data.expType, data.env,
                             symTable, anonymousModelHelper, names, new HashSet<>()));
        return nonReadOnlyUnion;
    }

    private BType populateArrowExprReturn(BLangArrowFunction bLangArrowFunction, BType expectedRetType,
                                          AnalyzerData data) {
        SymbolEnv arrowFunctionEnv = SymbolEnv.createArrowFunctionSymbolEnv(bLangArrowFunction, data.env);
        bLangArrowFunction.params.forEach(param -> symbolEnter.defineNode(param, arrowFunctionEnv));
        return checkExpr(bLangArrowFunction.body.expr, arrowFunctionEnv, expectedRetType, data);
    }

    private void populateArrowExprParamTypes(BLangArrowFunction bLangArrowFunction, List<BType> paramTypes,
                                             AnalyzerData data) {
        if (paramTypes.size() != bLangArrowFunction.params.size()) {
            dlog.error(bLangArrowFunction.pos,
                    DiagnosticErrorCode.ARROW_EXPRESSION_MISMATCHED_PARAMETER_LENGTH,
                    paramTypes.size(), bLangArrowFunction.params.size());
            data.resultType = symTable.semanticError;
            bLangArrowFunction.params.forEach(param -> param.setBType(symTable.semanticError));
            return;
        }

        for (int i = 0; i < bLangArrowFunction.params.size(); i++) {
            BLangSimpleVariable paramIdentifier = bLangArrowFunction.params.get(i);
            BType bType = paramTypes.get(i);
            BLangValueType valueTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            valueTypeNode.setTypeKind(bType.getKind());
            valueTypeNode.pos = symTable.builtinPos;
            paramIdentifier.setTypeNode(valueTypeNode);
            paramIdentifier.setBType(bType);
        }
    }

    public void checkSelfReferences(Location pos, SymbolEnv env, BVarSymbol varSymbol) {
        if (env.enclVarSym == varSymbol) {
            dlog.error(pos, DiagnosticErrorCode.SELF_REFERENCE_VAR, varSymbol.name);
        }
    }

    private void checkFunctionInvocationExpr(BLangInvocation iExpr, AnalyzerData data) {
        Name funcName = names.fromIdNode(iExpr.name);
        Name pkgAlias = names.fromIdNode(iExpr.pkgAlias);
        BSymbol funcSymbol = symTable.notFoundSymbol;

        BSymbol pkgSymbol = symResolver.resolvePrefixSymbol(data.env, pkgAlias, getCurrentCompUnit(iExpr));
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(iExpr.pos, DiagnosticErrorCode.UNDEFINED_MODULE, pkgAlias);
        } else {
            if (funcSymbol == symTable.notFoundSymbol) {
                BSymbol symbol = symResolver.lookupMainSpaceSymbolInPackage(iExpr.pos, data.env, pkgAlias, funcName);
                if ((symbol.tag & SymTag.VARIABLE) == SymTag.VARIABLE) {
                    funcSymbol = symbol;
                }
                if (symTable.rootPkgSymbol.pkgID.equals(symbol.pkgID) &&
                        (symbol.tag & SymTag.VARIABLE_NAME) == SymTag.VARIABLE_NAME) {
                    funcSymbol = symbol;
                }
            }
            if (funcSymbol == symTable.notFoundSymbol || ((funcSymbol.tag & SymTag.TYPE) == SymTag.TYPE)) {
                BSymbol ctor =
                        symResolver.lookupConstructorSpaceSymbolInPackage(iExpr.pos, data.env, pkgAlias, funcName);
                funcSymbol = ctor != symTable.notFoundSymbol ? ctor : funcSymbol;
            }
        }

        if (funcSymbol == symTable.notFoundSymbol || isNotFunction(funcSymbol)) {
            if (!missingNodesHelper.isMissingNode(funcName)) {
                dlog.error(iExpr.pos, DiagnosticErrorCode.UNDEFINED_FUNCTION, funcName);
            }
            iExpr.argExprs.forEach(arg -> checkExpr(arg, data));
            data.resultType = symTable.semanticError;
            return;
        }
        if (isFunctionPointer(funcSymbol)) {
            iExpr.functionPointerInvocation = true;
            markAndRegisterClosureVariable(funcSymbol, iExpr.pos, data.env, data);
        }
        if (Symbols.isFlagOn(funcSymbol.flags, Flags.REMOTE)) {
            dlog.error(iExpr.pos, DiagnosticErrorCode.INVALID_ACTION_INVOCATION_SYNTAX, iExpr.name.value);
        }
        if (Symbols.isFlagOn(funcSymbol.flags, Flags.RESOURCE)) {
            dlog.error(iExpr.pos, DiagnosticErrorCode.INVALID_RESOURCE_FUNCTION_INVOCATION);
        }

        boolean langLibPackageID = PackageID.isLangLibPackageID(pkgSymbol.pkgID);

        if (langLibPackageID) {
            // This will enable, type param support, if the function is called directly.
            data.env = SymbolEnv.createInvocationEnv(iExpr, data.env);
        }
        // Set the resolved function symbol in the invocation expression.
        // This is used in the code generation phase.
        iExpr.symbol = funcSymbol;
        checkInvocationParamAndReturnType(iExpr, data);

        if (langLibPackageID && !iExpr.argExprs.isEmpty()) {
            checkInvalidImmutableValueUpdate(iExpr, iExpr.argExprs.get(0).getBType(), funcSymbol, data);
        }
    }

    protected void markAndRegisterClosureVariable(BSymbol symbol, Location pos, SymbolEnv env, AnalyzerData data) {
        BLangInvokableNode encInvokable = env.enclInvokable;
        BLangNode bLangNode = env.node;
        if ((env.enclType != null && env.enclType.getKind() == NodeKind.FUNCTION_TYPE) ||
                (symbol.owner.tag & SymTag.PACKAGE) == SymTag.PACKAGE &&
                bLangNode.getKind() != NodeKind.ARROW_EXPR &&
                bLangNode.getKind() != NodeKind.EXPR_FUNCTION_BODY &&
                encInvokable != null && !encInvokable.flagSet.contains(Flag.LAMBDA) &&
                !encInvokable.flagSet.contains(Flag.OBJECT_CTOR)) {
            return;
        }
        if (!symbol.closure) {
            if (searchClosureVariableInExpressions(symbol, pos, env, encInvokable, bLangNode)) {
                return;
            }
        }

        BLangNode node = bLangNode;
        if (isObjectCtorClass(node))  {
            BLangClassDefinition classDef = (BLangClassDefinition) node;
            OCEDynamicEnvironmentData oceData = classDef.oceEnvData;
            BLangFunction currentFunc = (BLangFunction) encInvokable;
            if ((currentFunc != null) && !currentFunc.attachedFunction &&
                    !(currentFunc.symbol.receiverSymbol == symbol)) {
                BSymbol resolvedSymbol = symResolver.lookupClosureVarSymbol(oceData.capturedClosureEnv, symbol.name,
                        SymTag.VARIABLE);
                if (resolvedSymbol != symTable.notFoundSymbol && !resolvedSymbol.closure) {
                    if (resolvedSymbol.owner.getKind() != SymbolKind.PACKAGE) {
                        updateObjectCtorClosureSymbols(pos, currentFunc, resolvedSymbol, classDef, data);
                        return;
                    }
                }
            }
        }

        SymbolEnv cEnv = env;
        while (node != null) {
            if (node.getKind() == NodeKind.FUNCTION) {
                BLangFunction function = (BLangFunction) node;
                if (!function.flagSet.contains(Flag.OBJECT_CTOR) && !function.flagSet.contains(Flag.ATTACHED)) {
                    break;
                }
            }
            if (!symbol.closure) {
                if (searchClosureVariableInExpressions(symbol, pos, env, encInvokable, node)) {
                    return;
                }
            }
            if (isObjectCtorClass(node)) {
                BLangFunction currentFunction = (BLangFunction) encInvokable;
                if ((currentFunction != null) && currentFunction.attachedFunction &&
                        (currentFunction.symbol.receiverSymbol == symbol)) {
                    // self symbol
                    return;
                }
                SymbolEnv encInvokableEnv = findEnclosingInvokableEnv(env, encInvokable);
                BSymbol resolvedSymbol = symResolver.lookupClosureVarSymbol(encInvokableEnv, symbol.name,
                        SymTag.VARIABLE);
                BLangClassDefinition classDef = (BLangClassDefinition) node;
                if (resolvedSymbol != symTable.notFoundSymbol) {
                    if (resolvedSymbol.owner.getKind() == SymbolKind.PACKAGE) {
                        break;
                    }
                    updateObjectCtorClosureSymbols(pos, currentFunction, resolvedSymbol, classDef, data);
                    return;
                }
                break;
            }
            SymbolEnv enclEnv = cEnv.enclEnv;
            if (enclEnv == null) {
                break;
            }
            cEnv = enclEnv;
            node = cEnv.node;
        }
    }

    private boolean isObjectCtorClass(BLangNode node) {
        return node.getKind() == NodeKind.CLASS_DEFN &&
                ((BLangClassDefinition) node).flagSet.contains(Flag.OBJECT_CTOR);
    }

    private boolean searchClosureVariableInExpressions(BSymbol symbol, Location pos, SymbolEnv env,
                                                       BLangInvokableNode encInvokable, BLangNode bLangNode) {
        if (encInvokable != null && encInvokable.flagSet.contains(Flag.LAMBDA)
                && !isFunctionArgument(symbol, encInvokable.requiredParams)) {
            SymbolEnv encInvokableEnv = findEnclosingInvokableEnv(env, encInvokable);
            BSymbol resolvedSymbol =
                    symResolver.lookupClosureVarSymbol(encInvokableEnv, symbol.name, SymTag.VARIABLE);
            if (resolvedSymbol != symTable.notFoundSymbol && !encInvokable.flagSet.contains(Flag.ATTACHED)) {
                resolvedSymbol.closure = true;
                ((BLangFunction) encInvokable).closureVarSymbols.add(new ClosureVarSymbol(resolvedSymbol, pos));
                return true;
            }
        }

        if (bLangNode.getKind() == NodeKind.ARROW_EXPR
                && !isFunctionArgument(symbol, ((BLangArrowFunction) bLangNode).params)) {
            SymbolEnv encInvokableEnv = findEnclosingInvokableEnv(env, encInvokable);
            BSymbol resolvedSymbol =
                    symResolver.lookupClosureVarSymbol(encInvokableEnv, symbol.name, SymTag.VARIABLE);
            if (resolvedSymbol != symTable.notFoundSymbol) {
                resolvedSymbol.closure = true;
                ((BLangArrowFunction) bLangNode).closureVarSymbols.add(new ClosureVarSymbol(resolvedSymbol, pos));
                return true;
            }
        }

        if (env.enclType != null && env.enclType.getKind() == NodeKind.RECORD_TYPE) {
            SymbolEnv encInvokableEnv = findEnclosingInvokableEnv(env, (BLangRecordTypeNode) env.enclType);
            BSymbol resolvedSymbol =
                    symResolver.lookupClosureVarSymbol(encInvokableEnv, symbol.name, SymTag.VARIABLE);
            if (resolvedSymbol != symTable.notFoundSymbol && encInvokable != null &&
                    !encInvokable.flagSet.contains(Flag.ATTACHED)) {
                resolvedSymbol.closure = true;
                ((BLangFunction) encInvokable).closureVarSymbols.add(new ClosureVarSymbol(resolvedSymbol, pos));
                return true;
            }
        }
        return false;
    }

    private void updateObjectCtorClosureSymbols(Location pos, BLangFunction currentFunction, BSymbol resolvedSymbol,
                                                BLangClassDefinition classDef, AnalyzerData data) {
        classDef.hasClosureVars = true;
        resolvedSymbol.closure = true;
        if (currentFunction != null) {
            currentFunction.closureVarSymbols.add(new ClosureVarSymbol(resolvedSymbol, pos));
            // TODO: can identify if attached here
        }
        OCEDynamicEnvironmentData oceEnvData = classDef.oceEnvData;
        if (currentFunction != null && (currentFunction.symbol.params.contains(resolvedSymbol)
                || (currentFunction.symbol.restParam == resolvedSymbol))) {
            oceEnvData.closureFuncSymbols.add(resolvedSymbol);
        } else {
             oceEnvData.closureBlockSymbols.add(resolvedSymbol);
        }
        updateProceedingClasses(data.env.enclEnv, oceEnvData, classDef);
    }

    private void updateProceedingClasses(SymbolEnv envArg, OCEDynamicEnvironmentData oceEnvData,
                                         BLangClassDefinition origClassDef) {
        SymbolEnv localEnv = envArg;
        while (localEnv != null) {
            BLangNode node = localEnv.node;
            if (node.getKind() == NodeKind.PACKAGE) {
                break;
            }

            if (node.getKind() == NodeKind.CLASS_DEFN) {
                BLangClassDefinition classDef = (BLangClassDefinition) node;
                if (classDef != origClassDef) {
                    classDef.hasClosureVars = true;
                    OCEDynamicEnvironmentData parentOceData = classDef.oceEnvData;
                    oceEnvData.parents.push(classDef);
                    parentOceData.closureFuncSymbols.addAll(oceEnvData.closureFuncSymbols);
                    parentOceData.closureBlockSymbols.addAll(oceEnvData.closureBlockSymbols);
                }
            }
            localEnv = localEnv.enclEnv;
        }
    }

    private boolean isNotFunction(BSymbol funcSymbol) {
        if ((funcSymbol.tag & SymTag.FUNCTION) == SymTag.FUNCTION
                || (funcSymbol.tag & SymTag.CONSTRUCTOR) == SymTag.CONSTRUCTOR) {
            return false;
        }

        if (isFunctionPointer(funcSymbol)) {
            return false;
        }

        return true;
    }

    private boolean isFunctionPointer(BSymbol funcSymbol) {
        if ((funcSymbol.tag & SymTag.FUNCTION) == SymTag.FUNCTION) {
            return false;
        }
        return (funcSymbol.tag & SymTag.FUNCTION) == SymTag.VARIABLE
                && funcSymbol.kind == SymbolKind.FUNCTION
                && !Symbols.isNative(funcSymbol);
    }

    private List<BLangNamedArgsExpression> checkProvidedErrorDetails(BLangErrorConstructorExpr errorConstructorExpr,
                                                                     BType expectedType, AnalyzerData data) {
        List<BLangNamedArgsExpression> namedArgs = new ArrayList<>(errorConstructorExpr.namedArgs.size());
        for (BLangNamedArgsExpression namedArgsExpression : errorConstructorExpr.namedArgs) {
            BType target = checkErrCtrTargetTypeAndSetSymbol(namedArgsExpression, expectedType);

            if (Types.getReferredType(target).tag != TypeTags.UNION) {
                checkExpr(namedArgsExpression, target, data);
            } else {
                checkExpr(namedArgsExpression, data);
            }

            namedArgs.add(namedArgsExpression);
        }
        return namedArgs;
    }

    private BType checkErrCtrTargetTypeAndSetSymbol(BLangNamedArgsExpression namedArgsExpression, BType expectedType) {
        BType type = Types.getReferredType(expectedType);
        if (type == symTable.semanticError) {
            return symTable.semanticError;
        }

        if (type.tag == TypeTags.MAP) {
            return ((BMapType) type).constraint;
        }

        if (type.tag != TypeTags.RECORD) {
            return symTable.semanticError;
        }

        BRecordType recordType = (BRecordType) type;
        BField targetField = recordType.fields.get(namedArgsExpression.name.value);
        if (targetField != null) {
            // Set the symbol of the namedArgsExpression, with the matching record field symbol.
            namedArgsExpression.varSymbol = targetField.symbol;
            return targetField.type;
        }

        if (!recordType.sealed && !recordType.fields.isEmpty()) {
            dlog.error(namedArgsExpression.pos, DiagnosticErrorCode.INVALID_REST_DETAIL_ARG, namedArgsExpression.name,
                    recordType);
        }

        return recordType.sealed ? symTable.noType : recordType.restFieldType;
    }

    private void checkObjectFunctionInvocationExpr(BLangInvocation iExpr, BObjectType objectType, AnalyzerData data) {
        if (objectType.getKind() == TypeKind.SERVICE &&
                !(iExpr.expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                (Names.SELF.equals(((BLangSimpleVarRef) iExpr.expr).symbol.name)))) {
            dlog.error(iExpr.pos, DiagnosticErrorCode.SERVICE_FUNCTION_INVALID_INVOCATION);
            return;
        }
        // check for object attached function
        Name funcName =
                names.fromString(Symbols.getAttachedFuncSymbolName(objectType.tsymbol.name.value, iExpr.name.value));
        BSymbol funcSymbol =
                symResolver.resolveObjectMethod(iExpr.pos, data.env, funcName, (BObjectTypeSymbol) objectType.tsymbol);

        if (funcSymbol == symTable.notFoundSymbol) {
            BSymbol invocableField = symResolver.resolveInvocableObjectField(
                    iExpr.pos, data.env, names.fromIdNode(iExpr.name), (BObjectTypeSymbol) objectType.tsymbol);

            if (invocableField != symTable.notFoundSymbol && invocableField.kind == SymbolKind.FUNCTION) {
                funcSymbol = invocableField;
                iExpr.functionPointerInvocation = true;
            }
        }

        if (funcSymbol == symTable.notFoundSymbol || Types.getReferredType(funcSymbol.type).tag != TypeTags.INVOKABLE) {
            if (!checkLangLibMethodInvocationExpr(iExpr, objectType, data)) {
                dlog.error(iExpr.name.pos, DiagnosticErrorCode.UNDEFINED_METHOD_IN_OBJECT, iExpr.name.value,
                        objectType);
                data.resultType = symTable.semanticError;
                return;
            }
        } else {
            iExpr.symbol = funcSymbol;
        }

        // init method can be called in a method-call-expr only when the expression
        // preceding the . is self
        if (iExpr.name.value.equals(Names.USER_DEFINED_INIT_SUFFIX.value) &&
                !(iExpr.expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                (Names.SELF.equals(((BLangSimpleVarRef) iExpr.expr).symbol.name)))) {
            dlog.error(iExpr.pos, DiagnosticErrorCode.INVALID_INIT_INVOCATION);
        }

        if (Symbols.isFlagOn(funcSymbol.flags, Flags.REMOTE)) {
            dlog.error(iExpr.pos, DiagnosticErrorCode.INVALID_ACTION_INVOCATION_SYNTAX, iExpr.name.value);
        }
        if (Symbols.isFlagOn(funcSymbol.flags, Flags.RESOURCE)) {
            dlog.error(iExpr.pos, DiagnosticErrorCode.INVALID_RESOURCE_FUNCTION_INVOCATION);
        }
        checkInvocationParamAndReturnType(iExpr, data);
    }

    // Here, an action invocation can be either of the following three forms:
    // - foo->bar();
    // - start foo.bar(); or start foo->bar(); or start (new Foo()).foo();
    private void checkActionInvocation(BLangInvocation.BLangActionInvocation aInv, BObjectType expType,
                                       AnalyzerData data) {

        if (checkInvalidActionInvocation(aInv)) {
            dlog.error(aInv.pos, DiagnosticErrorCode.INVALID_ACTION_INVOCATION, aInv.expr.getBType());
            data.resultType = symTable.semanticError;
            aInv.symbol = symTable.notFoundSymbol;
            return;
        }

        Name remoteMethodQName = names
                .fromString(Symbols.getAttachedFuncSymbolName(expType.tsymbol.name.value, aInv.name.value));
        Name actionName = names.fromIdNode(aInv.name);
        BSymbol remoteFuncSymbol = symResolver.resolveObjectMethod(aInv.pos, data.env,
            remoteMethodQName, (BObjectTypeSymbol) Types.getReferredType(expType).tsymbol);

        if (remoteFuncSymbol == symTable.notFoundSymbol) {
            BSymbol invocableField = symResolver.resolveInvocableObjectField(
                    aInv.pos, data.env, names.fromIdNode(aInv.name), (BObjectTypeSymbol) expType.tsymbol);

            if (invocableField != symTable.notFoundSymbol && invocableField.kind == SymbolKind.FUNCTION) {
                remoteFuncSymbol = invocableField;
                aInv.functionPointerInvocation = true;
            }
        }

        if (remoteFuncSymbol == symTable.notFoundSymbol && !checkLangLibMethodInvocationExpr(aInv, expType, data)) {
            dlog.error(aInv.name.pos, DiagnosticErrorCode.UNDEFINED_METHOD_IN_OBJECT, aInv.name.value, expType);
            data.resultType = symTable.semanticError;
            return;
        }

        if (!Symbols.isFlagOn(remoteFuncSymbol.flags, Flags.REMOTE) && !aInv.async) {
            dlog.error(aInv.pos, DiagnosticErrorCode.INVALID_METHOD_INVOCATION_SYNTAX, actionName);
            data.resultType = symTable.semanticError;
            return;
        }
        if (Symbols.isFlagOn(remoteFuncSymbol.flags, Flags.REMOTE) &&
                Symbols.isFlagOn(expType.flags, Flags.CLIENT) &&
                types.isNeverTypeOrStructureTypeWithARequiredNeverMember
                        ((BType) ((InvokableSymbol) remoteFuncSymbol).getReturnType())) {
            dlog.error(aInv.pos, DiagnosticErrorCode.INVALID_CLIENT_REMOTE_METHOD_CALL);
        }

        aInv.symbol = remoteFuncSymbol;
        checkInvocationParamAndReturnType(aInv, data);
    }

    private boolean checkInvalidActionInvocation(BLangInvocation.BLangActionInvocation aInv) {
        return aInv.expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                (((((BLangSimpleVarRef) aInv.expr).symbol.tag & SymTag.ENDPOINT) !=
                        SymTag.ENDPOINT) && !aInv.async);
    }

    private boolean checkLangLibMethodInvocationExpr(BLangInvocation iExpr, BType bType, AnalyzerData data) {
        return getLangLibMethod(iExpr, bType, data) != symTable.notFoundSymbol;
    }

    private BSymbol getLangLibMethod(BLangInvocation iExpr, BType bType, AnalyzerData data) {

        Name funcName = names.fromString(iExpr.name.value);
        BSymbol funcSymbol = symResolver.lookupLangLibMethod(bType, funcName, data.env);

        if (funcSymbol == symTable.notFoundSymbol) {
            return symTable.notFoundSymbol;
        }

        iExpr.symbol = funcSymbol;
        iExpr.langLibInvocation = true;
        SymbolEnv enclEnv = data.env;
        data.env = SymbolEnv.createInvocationEnv(iExpr, data.env);
        iExpr.argExprs.add(0, iExpr.expr);
        checkInvocationParamAndReturnType(iExpr, data);
        data.env = enclEnv;

        return funcSymbol;
    }

    private void checkInvocationParamAndReturnType(BLangInvocation iExpr, AnalyzerData data) {
        BType actualType = checkInvocationParam(iExpr, data);
        data.resultType = types.checkType(iExpr, actualType, data.expType);
    }

    private BVarSymbol incRecordParamAllowAdditionalFields(List<BVarSymbol> openIncRecordParams,
                                                           Set<String> requiredParamNames) {
        if (openIncRecordParams.size() != 1) {
            return null;
        }
        LinkedHashMap<String, BField> fields =
                ((BRecordType) Types.getReferredType(openIncRecordParams.get(0).type)).fields;
        for (String paramName : requiredParamNames) {
            if (!fields.containsKey(paramName)) {
                return null;
            }
        }
        return openIncRecordParams.get(0);
    }

    private BVarSymbol checkForIncRecordParamAllowAdditionalFields(BInvokableSymbol invokableSymbol,
                                                                   List<BVarSymbol> incRecordParams) {
        Set<String> requiredParamNames = new HashSet<>();
        List<BVarSymbol> openIncRecordParams = new ArrayList<>();
        for (BVarSymbol paramSymbol : invokableSymbol.params) {
            BType paramType = Types.getReferredType(paramSymbol.type);
            if (Symbols.isFlagOn(Flags.asMask(paramSymbol.getFlags()), Flags.INCLUDED) &&
                    paramType.getKind() == TypeKind.RECORD) {
                boolean recordWithDisallowFieldsOnly = true;
                LinkedHashMap<String, BField> fields = ((BRecordType) paramType).fields;
                for (String fieldName : fields.keySet()) {
                    BField field = fields.get(fieldName);
                    if (field.symbol.type.tag != TypeTags.NEVER) {
                        recordWithDisallowFieldsOnly = false;
                        incRecordParams.add(field.symbol);
                        requiredParamNames.add(fieldName);
                    }
                }
                if (recordWithDisallowFieldsOnly && ((BRecordType) paramType).restFieldType != symTable.noType) {
                    openIncRecordParams.add(paramSymbol);
                }
            } else {
                requiredParamNames.add(paramSymbol.name.value);
            }
        }
        return incRecordParamAllowAdditionalFields(openIncRecordParams, requiredParamNames);
    }

    private BType checkInvocationParam(BLangInvocation iExpr, AnalyzerData data) {
        if (Symbols.isFlagOn(iExpr.symbol.type.flags, Flags.ANY_FUNCTION)) {
            dlog.error(iExpr.pos, DiagnosticErrorCode.INVALID_FUNCTION_POINTER_INVOCATION_WITH_TYPE);
            return symTable.semanticError;
        }
        BType invocableType = Types.getReferredType(iExpr.symbol.type);
        if (invocableType.tag != TypeTags.INVOKABLE) {
            dlog.error(iExpr.pos, DiagnosticErrorCode.INVALID_FUNCTION_INVOCATION, iExpr.symbol.type);
            return symTable.noType;
        }

        BInvokableSymbol invokableSymbol = ((BInvokableSymbol) iExpr.symbol);
        List<BType> paramTypes = ((BInvokableType) invocableType).getParameterTypes();
        List<BVarSymbol> incRecordParams = new ArrayList<>();
        BVarSymbol incRecordParamAllowAdditionalFields = checkForIncRecordParamAllowAdditionalFields(invokableSymbol,
                                                                                                     incRecordParams);
        int parameterCountForPositionalArgs = paramTypes.size();
        int parameterCountForNamedArgs = parameterCountForPositionalArgs + incRecordParams.size();
        iExpr.requiredArgs = new ArrayList<>();
        for (BVarSymbol symbol : invokableSymbol.params) {
            if (!Symbols.isFlagOn(Flags.asMask(symbol.getFlags()), Flags.INCLUDED) ||
                    Types.getReferredType(symbol.type).tag != TypeTags.RECORD) {
                continue;
            }
            LinkedHashMap<String, BField> fields =
                    ((BRecordType) Types.getReferredType(symbol.type)).fields;
            if (fields.isEmpty()) {
                continue;
            }
            for (String field : fields.keySet()) {
                if (Types.getReferredType(fields.get(field).type).tag != TypeTags.NEVER) {
                    parameterCountForNamedArgs = parameterCountForNamedArgs - 1;
                    break;
                }
            }
        }

        // Split the different argument types: required args, named args and rest args
        int i = 0;
        BLangExpression vararg = null;
        boolean foundNamedArg = false;
        for (BLangExpression expr : iExpr.argExprs) {
            switch (expr.getKind()) {
                case NAMED_ARGS_EXPR:
                    foundNamedArg = true;
                    if (i < parameterCountForNamedArgs || incRecordParamAllowAdditionalFields != null) {
                        iExpr.requiredArgs.add(expr);
                    } else {
                        // can not provide a rest parameters as named args
                        dlog.error(expr.pos, DiagnosticErrorCode.TOO_MANY_ARGS_FUNC_CALL, iExpr.name.value);
                    }
                    i++;
                    break;
                case REST_ARGS_EXPR:
                    if (foundNamedArg) {
                        dlog.error(expr.pos, DiagnosticErrorCode.REST_ARG_DEFINED_AFTER_NAMED_ARG);
                        continue;
                    }
                    vararg = expr;
                    break;
                default: // positional args
                    if (foundNamedArg) {
                        dlog.error(expr.pos, DiagnosticErrorCode.POSITIONAL_ARG_DEFINED_AFTER_NAMED_ARG);
                    }
                    if (i < parameterCountForPositionalArgs) {
                        iExpr.requiredArgs.add(expr);
                    } else {
                        iExpr.restArgs.add(expr);
                    }
                    i++;
                    break;
            }
        }

        return checkInvocationArgs(iExpr, paramTypes, vararg, incRecordParams,
                                    incRecordParamAllowAdditionalFields, data);
    }

    private BType checkInvocationArgs(BLangInvocation iExpr, List<BType> paramTypes, BLangExpression vararg,
                                      List<BVarSymbol> incRecordParams,
                                      BVarSymbol incRecordParamAllowAdditionalFields, AnalyzerData data) {
        BInvokableSymbol invokableSymbol = (BInvokableSymbol) iExpr.symbol;
        BInvokableType bInvokableType = (BInvokableType) Types.getReferredType(invokableSymbol.type);
        BInvokableTypeSymbol invokableTypeSymbol = (BInvokableTypeSymbol) bInvokableType.tsymbol;
        List<BVarSymbol> nonRestParams = new ArrayList<>(invokableSymbol.params);

        List<BLangExpression> nonRestArgs = iExpr.requiredArgs;
        List<BVarSymbol> valueProvidedParams = new ArrayList<>();

        int nonRestArgCount = nonRestArgs.size();
        List<BVarSymbol> requiredParams = new ArrayList<>(nonRestParams.size() + nonRestArgCount);
        List<BVarSymbol> requiredIncRecordParams = new ArrayList<>(incRecordParams.size() + nonRestArgCount);

        for (BVarSymbol nonRestParam : nonRestParams) {
            if (nonRestParam.isDefaultable) {
                continue;
            }

            requiredParams.add(nonRestParam);
        }

        List<String> includedRecordParamFieldNames = new ArrayList<>(incRecordParams.size());
        for (BVarSymbol incRecordParam : incRecordParams) {
            if (Symbols.isFlagOn(Flags.asMask(incRecordParam.getFlags()), Flags.REQUIRED)) {
                requiredIncRecordParams.add(incRecordParam);
            }
            includedRecordParamFieldNames.add(incRecordParam.name.value);
        }

        HashSet<String> includedRecordFields = new HashSet<>();
        List<BLangExpression> namedArgs = new ArrayList<>();
        int i = 0;
        for (; i < nonRestArgCount; i++) {
            BLangExpression arg = nonRestArgs.get(i);

            // Special case handling for the first param because for parameterized invocations, we have added the
            // value on which the function is invoked as the first param of the function call. If we run checkExpr()
            // on it, it will recursively add the first param to argExprs again, resulting in a too many args in
            // function call error.
            if (i == 0 && arg.typeChecked && iExpr.expr != null && iExpr.expr == arg) {
                BType expectedType = paramTypes.get(i);
                BType actualType = arg.getBType();
                if (Types.getReferredType(expectedType) == symTable.charStringType) {
                    arg.cloneAttempt++;
                    BLangExpression clonedArg = nodeCloner.cloneNode(arg);
                    BType argType = checkExprSilent(clonedArg, expectedType, data);
                    if (argType != symTable.semanticError) {
                        actualType = argType;
                    }
                }
                types.checkType(arg.pos, actualType, expectedType, DiagnosticErrorCode.INCOMPATIBLE_TYPES);
                types.setImplicitCastExpr(arg, arg.getBType(), expectedType);
            }

            if (arg.getKind() != NodeKind.NAMED_ARGS_EXPR) {
                // if arg is positional, corresponding parameter in the same position should be of same type.
                if (i < nonRestParams.size()) {
                    BVarSymbol param = nonRestParams.get(i);
                    if (Symbols.isFlagOn(param.flags, Flags.INCLUDED)) {
                        populateIncludedRecordParams(param, includedRecordFields, includedRecordParamFieldNames);
                    }
                    checkTypeParamExpr(arg, param.type, iExpr.langLibInvocation, data);
                    valueProvidedParams.add(param);
                    requiredParams.remove(param);
                    continue;
                }
                // Arg count > required + defaultable param count.
                break;
            }

            if (arg.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                // if arg is named, function should have a parameter with this name.
                BLangIdentifier argName = ((NamedArgNode) arg).getName();
                BVarSymbol varSym = checkParameterNameForDefaultArgument(argName, ((BLangNamedArgsExpression) arg).expr,
                                            nonRestParams, incRecordParams, incRecordParamAllowAdditionalFields, data);

                if (varSym == null) {
                    dlog.error(arg.pos, DiagnosticErrorCode.UNDEFINED_PARAMETER, argName);
                    break;
                }
                if (Symbols.isFlagOn(varSym.flags, Flags.INCLUDED)) {
                    populateIncludedRecordParams(varSym, includedRecordFields, includedRecordParamFieldNames);
                } else {
                    namedArgs.add(arg);
                }
                requiredParams.remove(varSym);
                requiredIncRecordParams.remove(varSym);
                if (valueProvidedParams.contains(varSym)) {
                    dlog.error(arg.pos, DiagnosticErrorCode.DUPLICATE_NAMED_ARGS, varSym.name.value);
                    continue;
                }
                checkTypeParamExpr(arg, varSym.type, iExpr.langLibInvocation, data);
                ((BLangNamedArgsExpression) arg).varSymbol = varSym;
                valueProvidedParams.add(varSym);
            }
        }
        checkSameNamedArgsInIncludedRecords(namedArgs, includedRecordFields);
        BVarSymbol restParam = invokableTypeSymbol.restParam;

        boolean errored = false;

        if (!requiredParams.isEmpty() && vararg == null) {
            // Log errors if any required parameters are not given as positional/named args and there is
            // no vararg either.
            for (BVarSymbol requiredParam : requiredParams) {
                if (!Symbols.isFlagOn(Flags.asMask(requiredParam.getFlags()), Flags.INCLUDED)) {
                    dlog.error(iExpr.pos, DiagnosticErrorCode.MISSING_REQUIRED_PARAMETER, requiredParam.name,
                            iExpr.name.value);
                    errored = true;
                }
            }
        }

        if (!requiredIncRecordParams.isEmpty() && !requiredParams.isEmpty()) {
            // Log errors if any non-defaultable required record fields of included record parameters are not given as
            // named args.
            for (BVarSymbol requiredIncRecordParam : requiredIncRecordParams) {
                for (BVarSymbol requiredParam : requiredParams) {
                    if (Types.getReferredType(requiredParam.type) ==
                            Types.getReferredType(requiredIncRecordParam.owner.type)) {
                        dlog.error(iExpr.pos, DiagnosticErrorCode.MISSING_REQUIRED_PARAMETER,
                                requiredIncRecordParam.name, iExpr.name.value);
                        errored = true;
                    }
                }
            }
        }

        if (restParam == null &&
                (!iExpr.restArgs.isEmpty() ||
                         (vararg != null && valueProvidedParams.size() == nonRestParams.size()))) {
            dlog.error(iExpr.pos, DiagnosticErrorCode.TOO_MANY_ARGS_FUNC_CALL, iExpr.name.value);
            errored = true;
        }

        if (errored) {
            return symTable.semanticError;
        }

        BType listTypeRestArg = restParam == null ? null : restParam.type;
        BRecordType mappingTypeRestArg = null;

        if (vararg != null && nonRestArgs.size() < nonRestParams.size()) {
            // We only reach here if there are no named args and there is a vararg, and part of the non-rest params
            // are provided via the vararg.
            // Create a new tuple type and a closed record type as the expected rest param type with expected
            // required/defaultable paramtypes as members.
            PackageID pkgID = data.env.enclPkg.symbol.pkgID;
            List<BTupleMember> tupleMembers = new ArrayList<>();
            BRecordTypeSymbol recordSymbol = createRecordTypeSymbol(pkgID, null, VIRTUAL, data);
            mappingTypeRestArg = new BRecordType(recordSymbol);
            LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
            BType tupleRestType = null;
            BVarSymbol fieldSymbol;

            for (int j = nonRestArgs.size(); j < nonRestParams.size(); j++) {
                BType paramType = paramTypes.get(j);
                BVarSymbol nonRestParam = nonRestParams.get(j);
                Name paramName = nonRestParam.name;
                BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(paramType);
                tupleMembers.add(new BTupleMember(paramType, varSymbol));
                boolean required = requiredParams.contains(nonRestParam);
                fieldSymbol = new BVarSymbol(Flags.asMask(new HashSet<Flag>() {{
                                             add(required ? Flag.REQUIRED : Flag.OPTIONAL); }}), paramName,
                                             nonRestParam.getOriginalName(), pkgID, paramType, recordSymbol,
                                             symTable.builtinPos, VIRTUAL);
                fields.put(paramName.value, new BField(paramName, null, fieldSymbol));
            }

            if (listTypeRestArg != null) {
                if (listTypeRestArg.tag == TypeTags.ARRAY) {
                    tupleRestType = ((BArrayType) listTypeRestArg).eType;
                } else if (listTypeRestArg.tag == TypeTags.TUPLE) {
                    BTupleType restTupleType = (BTupleType) listTypeRestArg;
                    tupleMembers.addAll(restTupleType.getMembers());
                    restTupleType.getMembers().forEach(t -> tupleMembers.add(t));
                    if (restTupleType.restType != null) {
                        tupleRestType = restTupleType.restType;
                    }
                }
            }

            BTupleType tupleType = new BTupleType(tupleMembers);
            tupleType.restType = tupleRestType;
            listTypeRestArg = tupleType;
            mappingTypeRestArg.sealed = true;
            mappingTypeRestArg.restFieldType = symTable.noType;
            mappingTypeRestArg.fields = fields;
            recordSymbol.type = mappingTypeRestArg;
            mappingTypeRestArg.tsymbol = recordSymbol;
        }

        // Check whether the expected param count and the actual args counts are matching.
        if (listTypeRestArg == null && (vararg != null || !iExpr.restArgs.isEmpty())) {
            dlog.error(iExpr.pos, DiagnosticErrorCode.TOO_MANY_ARGS_FUNC_CALL, iExpr.name.value);
            return symTable.semanticError;
        }

        BType restType = null;
        if (vararg != null && !iExpr.restArgs.isEmpty()) {
            // We reach here if args are provided for the rest param as both individual rest args and a vararg.
            // Thus, the rest param type is the original rest param type which is an array type.
            BType elementType = ((BArrayType) listTypeRestArg).eType;

            for (BLangExpression restArg : iExpr.restArgs) {
                checkTypeParamExpr(restArg, elementType, true, data);
            }

            checkTypeParamExpr(vararg, listTypeRestArg, iExpr.langLibInvocation, data);
            iExpr.restArgs.add(vararg);
            restType = data.resultType;
        } else if (vararg != null) {
            iExpr.restArgs.add(vararg);
            if (mappingTypeRestArg != null) {
                LinkedHashSet<BType> restTypes = new LinkedHashSet<>();
                restTypes.add(listTypeRestArg);
                restTypes.add(mappingTypeRestArg);
                BType actualType = BUnionType.create(null, restTypes);
                checkTypeParamExpr(vararg, actualType, iExpr.langLibInvocation, data);
            } else {
                checkTypeParamExpr(vararg, listTypeRestArg, iExpr.langLibInvocation, data);
            }
            restType = data.resultType;
        } else if (!iExpr.restArgs.isEmpty()) {
            if (listTypeRestArg.tag == TypeTags.ARRAY) {
                BType elementType = ((BArrayType) listTypeRestArg).eType;
                for (BLangExpression restArg : iExpr.restArgs) {
                    checkTypeParamExpr(restArg, elementType, true, data);
                    if (restType != symTable.semanticError && data.resultType == symTable.semanticError) {
                        restType = data.resultType;
                    }
                }
            } else if (listTypeRestArg.tag == TypeTags.TUPLE) {
                BTupleType tupleType = (BTupleType) listTypeRestArg;
                List<BType> tupleMemberTypes = tupleType.getTupleTypes();
                BType tupleRestType = tupleType.restType;

                int tupleMemCount = tupleMemberTypes.size();

                for (int j = 0; j < iExpr.restArgs.size(); j++) {
                    BLangExpression restArg = iExpr.restArgs.get(j);
                    BType memType = j < tupleMemCount ? tupleMemberTypes.get(j) : tupleRestType;
                    checkTypeParamExpr(restArg, memType, true, data);
                    if (restType != symTable.semanticError && data.resultType == symTable.semanticError) {
                        restType = data.resultType;
                    }
                }
            } else {
                for (BLangExpression restArg : iExpr.restArgs) {
                    checkExpr(restArg, symTable.semanticError, data);
                }
                data.resultType = symTable.semanticError;
            }
        }

        BType retType = typeParamAnalyzer.getReturnTypeParams(data.env, bInvokableType.getReturnType());
        long invokableSymbolFlags = invokableSymbol.flags;
        if (restType != symTable.semanticError && (Symbols.isFlagOn(invokableSymbolFlags, Flags.INTERFACE)
                || Symbols.isFlagOn(invokableSymbolFlags, Flags.NATIVE)) &&
                Symbols.isFlagOn(retType.flags, Flags.PARAMETERIZED)) {
            retType = unifier.build(retType, data.expType, iExpr, types, symTable, dlog);
        }

        // check argument types in arr:sort function
        boolean langLibPackageID = PackageID.isLangLibPackageID(iExpr.symbol.pkgID);
        String sortFuncName = "sort";
        if (langLibPackageID && sortFuncName.equals(iExpr.name.value)) {
            checkArrayLibSortFuncArgs(iExpr);
        }

        if (iExpr instanceof ActionNode && (iExpr).async) {
            return this.generateFutureType(invokableSymbol, retType);
        } else {
            return retType;
        }
    }

    private void populateIncludedRecordParams(BVarSymbol param, HashSet<String> includedRecordFields,
                                              List<String> includedRecordParamNames) {
        BType paramType = Types.getReferredType(param.type);
        if (paramType.tag != TypeTags.RECORD) {
            return;
        }

        Set<String> fields = ((BRecordType) paramType).fields.keySet();
        for (String field : fields) {
            if (includedRecordParamNames.contains(field)) {
                includedRecordFields.add(field);
            }
        }
    }

    // If there is a named-arg or positional-arg corresponding to an included-record-param,
    // it is an error for a named-arg to specify a field of that included-record-param.
    private void checkSameNamedArgsInIncludedRecords(List<BLangExpression> namedArgs,
                                                     HashSet<String> incRecordFields) {
        if (incRecordFields.isEmpty()) {
            return;
        }
        for (BLangExpression namedArg : namedArgs) {
            String argName = ((NamedArgNode) namedArg).getName().value;
            if (incRecordFields.contains(argName)) {
                dlog.error(namedArg.pos,
                        DiagnosticErrorCode.
                        CANNOT_SPECIFY_NAMED_ARG_FOR_FIELD_OF_INCLUDED_RECORD_WHEN_ARG_SPECIFIED_FOR_INCLUDED_RECORD);
            }
        }
    }

    private void checkArrayLibSortFuncArgs(BLangInvocation iExpr) {
        List<BLangExpression> argExprs = iExpr.argExprs;
        BLangExpression keyFunction = null;

        for (int i = 0; i < argExprs.size(); i++) {
            BLangExpression arg = argExprs.get(i);
            if (arg.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                BLangNamedArgsExpression argExpr = (BLangNamedArgsExpression) arg;
                if (argExpr.name.value.equals("key")) {
                    keyFunction = argExpr.expr;
                    break;
                }
            } else if (i == 2) {
                keyFunction = arg;
                break;
            }
        }

        BLangExpression arrExpr = argExprs.get(0);
        BType arrType = arrExpr.getBType();
        boolean isOrderedType = types.isOrderedType(arrType, false);
        if (keyFunction == null) {
            if (!isOrderedType) {
                dlog.error(arrExpr.pos, DiagnosticErrorCode.INVALID_SORT_ARRAY_MEMBER_TYPE, arrType);
            }
            return;
        }

        BType keyFunctionType = keyFunction.getBType();

        if (keyFunctionType.tag == TypeTags.SEMANTIC_ERROR) {
            return;
        }

        if (keyFunctionType.tag == TypeTags.NIL) {
            if (!isOrderedType) {
                dlog.error(arrExpr.pos, DiagnosticErrorCode.INVALID_SORT_ARRAY_MEMBER_TYPE, arrType);
            }
            return;
        }

        Location pos;
        BType returnType;

        if (keyFunction.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            pos = keyFunction.pos;
            returnType = keyFunction.getBType().getReturnType();
        } else if (keyFunction.getKind() == NodeKind.ARROW_EXPR) {
            BLangArrowFunction arrowFunction = ((BLangArrowFunction) keyFunction);
            pos = arrowFunction.body.expr.pos;
            returnType = arrowFunction.body.expr.getBType();
            if (returnType.tag == TypeTags.SEMANTIC_ERROR) {
                return;
            }
        } else {
            BLangLambdaFunction keyLambdaFunction = (BLangLambdaFunction) keyFunction;
            pos = keyLambdaFunction.function.pos;
            returnType = keyLambdaFunction.function.getBType().getReturnType();
        }

        if (!types.isOrderedType(returnType, false)) {
            dlog.error(pos, DiagnosticErrorCode.INVALID_SORT_FUNC_RETURN_TYPE, returnType);
        }
    }

    private BVarSymbol checkParameterNameForDefaultArgument(BLangIdentifier argName, BLangExpression expr,
                                                            List<BVarSymbol> nonRestParams,
                                                            List<BVarSymbol> incRecordParams,
                                                            BVarSymbol incRecordParamAllowAdditionalFields,
                                                            AnalyzerData data) {
        for (BVarSymbol nonRestParam : nonRestParams) {
            if (nonRestParam.getName().value.equals(argName.value)) {
                return nonRestParam;
            }
        }
        for (BVarSymbol incRecordParam : incRecordParams) {
            if (incRecordParam.getName().value.equals(argName.value)) {
                return incRecordParam;
            }
        }
        if (incRecordParamAllowAdditionalFields != null) {
            BRecordType incRecordType =
                    (BRecordType) Types.getReferredType(incRecordParamAllowAdditionalFields.type);
            checkExpr(expr, incRecordType.restFieldType, data);
            if (!incRecordType.fields.containsKey(argName.value)) {
                return new BVarSymbol(0, names.fromIdNode(argName), names.originalNameFromIdNode(argName),
                                      null, symTable.noType, null, argName.pos, VIRTUAL);
            }
        }
        return null;
    }

    private BFutureType generateFutureType(BInvokableSymbol invocableSymbol, BType retType) {
        boolean isWorkerStart = Symbols.isFlagOn(invocableSymbol.flags, Flags.WORKER);
        return new BFutureType(TypeTags.FUTURE, retType, null, isWorkerStart);
    }

    private void checkTypeParamExpr(BLangExpression arg, BType expectedType,
                                    boolean inferTypeForNumericLiteral, AnalyzerData data) {
        checkTypeParamExpr(arg.pos, arg, expectedType, inferTypeForNumericLiteral, data);
    }

    private void checkTypeParamExpr(Location pos, BLangExpression arg, BType expectedType,
                                    boolean inferTypeForNumericLiteral, AnalyzerData data) {

        SymbolEnv env = data.env;
        if (typeParamAnalyzer.notRequireTypeParams(env)) {
            checkExpr(arg, expectedType, data);
            return;
        }
        if (requireTypeInference(arg, inferTypeForNumericLiteral)) {
            // Need to infer the type. Calculate matching bound type, with no type.
            BType expType = typeParamAnalyzer.getMatchingBoundType(expectedType, env);
            BType inferredType = checkExpr(arg, expType, data);
            typeParamAnalyzer.checkForTypeParamsInArg(arg, pos, inferredType, data.env, expectedType);
            types.checkType(arg.pos, inferredType, expectedType, DiagnosticErrorCode.INCOMPATIBLE_TYPES);
            return;
        }
        checkExpr(arg, expectedType, data);
        typeParamAnalyzer.checkForTypeParamsInArg(arg, pos, arg.getBType(), data.env, expectedType);
    }

    private boolean requireTypeInference(BLangExpression expr, boolean inferTypeForNumericLiteral) {

        switch (expr.getKind()) {
            case GROUP_EXPR:
                return requireTypeInference(((BLangGroupExpr) expr).expression, inferTypeForNumericLiteral);
            case ARROW_EXPR:
            case LIST_CONSTRUCTOR_EXPR:
            case RECORD_LITERAL_EXPR:
            case OBJECT_CTOR_EXPRESSION:
            case RAW_TEMPLATE_LITERAL:
            case TABLE_CONSTRUCTOR_EXPR:
            case TYPE_INIT_EXPR:
            case ERROR_CONSTRUCTOR_EXPRESSION:
                return true;
            case ELVIS_EXPR:
            case TERNARY_EXPR:
            case NUMERIC_LITERAL:
                return inferTypeForNumericLiteral;
            default:
                return false;
        }
    }

    private boolean isNotObjectConstructorWithObjectSuperTypeInTypeCastExpr(BLangExpression expression,
                                                                            BType targetType) {
        if (expression.getKind() != NodeKind.OBJECT_CTOR_EXPRESSION) {
            return true;
        }

        targetType = Types.getEffectiveType(Types.getReferredType(targetType));
        int tag = targetType.tag;

        if (tag == TypeTags.OBJECT) {
            return !isAllObjectsObjectType((BObjectType) targetType);
        }

        if (tag != TypeTags.UNION) {
            return false;
        }

        for (BType memberType : ((BUnionType) targetType).getMemberTypes()) {
            memberType = Types.getEffectiveType(Types.getReferredType(memberType));
            if (memberType.tag == TypeTags.OBJECT && isAllObjectsObjectType((BObjectType) memberType)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAllObjectsObjectType(BObjectType objectType) {
        return objectType.fields.isEmpty() && ((BObjectTypeSymbol) objectType.tsymbol).attachedFuncs.isEmpty();
    }

    private BType checkMappingField(RecordLiteralNode.RecordField field, BType mappingType, AnalyzerData data) {
        BType fieldType = symTable.semanticError;
        boolean keyValueField = field.isKeyValueField();
        boolean spreadOpField = field.getKind() == NodeKind.RECORD_LITERAL_SPREAD_OP;

        boolean readOnlyConstructorField = false;
        String fieldName = null;
        Location pos = null;

        BLangExpression valueExpr = null;

        if (keyValueField) {
            valueExpr = ((BLangRecordKeyValueField) field).valueExpr;
        } else if (!spreadOpField) {
            valueExpr = (BLangRecordVarNameField) field;
        }

        boolean isOptional = false;
        switch (mappingType.tag) {
            case TypeTags.RECORD:
                if (keyValueField) {
                    BLangRecordKeyValueField keyValField = (BLangRecordKeyValueField) field;
                    BLangRecordKey key = keyValField.key;
                    TypeSymbolPair typeSymbolPair = checkRecordLiteralKeyExpr(key.expr, key.computedKey,
                                                                              (BRecordType) mappingType, data);
                    BVarSymbol fieldSymbol = typeSymbolPair.fieldSymbol;
                    if (fieldSymbol != null && Symbols.isOptional(fieldSymbol)) {
                        isOptional = true;
                    }
                    fieldType = typeSymbolPair.determinedType;
                    key.fieldSymbol = fieldSymbol;
                    readOnlyConstructorField = keyValField.readonly;
                    pos = key.expr.pos;
                    fieldName = getKeyValueFieldName(keyValField);
                } else if (spreadOpField) {
                    BLangExpression spreadExpr = ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr;
                    checkExpr(spreadExpr, data);

                    BType spreadExprType = Types.getReferredType(spreadExpr.getBType());
                    if (spreadExprType.tag == TypeTags.MAP) {
                        return types.checkType(spreadExpr.pos, ((BMapType) spreadExprType).constraint,
                                getAllFieldType((BRecordType) mappingType),
                                DiagnosticErrorCode.INCOMPATIBLE_TYPES);
                    }

                    if (spreadExprType.tag != TypeTags.RECORD) {
                        dlog.error(spreadExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES_SPREAD_OP,
                                spreadExprType);
                        return symTable.semanticError;
                    }

                    boolean errored = false;
                    for (BField bField : ((BRecordType) spreadExprType).fields.values()) {
                        BType specFieldType = bField.type;
                        BSymbol fieldSymbol = symResolver.resolveStructField(spreadExpr.pos, data.env, bField.name,
                                                                             mappingType.tsymbol);
                        BType expectedFieldType = checkRecordLiteralKeyByName(spreadExpr.pos, fieldSymbol, bField.name,
                                                                              (BRecordType) mappingType);
                        if (expectedFieldType != symTable.semanticError &&
                                !types.isAssignable(specFieldType, expectedFieldType)) {
                            dlog.error(spreadExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES_FIELD,
                                       expectedFieldType, bField.name, specFieldType);
                            if (!errored) {
                                errored = true;
                            }
                        }
                    }
                    return errored ? symTable.semanticError : symTable.noType;
                } else {
                    BLangRecordVarNameField varNameField = (BLangRecordVarNameField) field;
                    TypeSymbolPair typeSymbolPair = checkRecordLiteralKeyExpr(varNameField, false,
                                                                              (BRecordType) mappingType, data);
                    BVarSymbol fieldSymbol = typeSymbolPair.fieldSymbol;
                    if (fieldSymbol != null && Symbols.isOptional(fieldSymbol)) {
                        isOptional = true;
                    }
                    fieldType = typeSymbolPair.determinedType;
                    readOnlyConstructorField = varNameField.readonly;
                    pos = varNameField.pos;
                    fieldName = getVarNameFieldName(varNameField);
                }
                break;
            case TypeTags.MAP:
                if (spreadOpField) {
                    BLangExpression spreadExp = ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr;
                    BType spreadOpType = checkExpr(spreadExp, data);
                    BType spreadOpMemberType = checkSpreadFieldWithMapType(spreadOpType);
                    if (spreadOpMemberType.tag == symTable.semanticError.tag) {
                        dlog.error(spreadExp.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES_SPREAD_OP,
                                spreadOpType);
                        return symTable.semanticError;
                    }

                    return types.checkType(spreadExp.pos, spreadOpMemberType, ((BMapType) mappingType).constraint,
                            DiagnosticErrorCode.INCOMPATIBLE_TYPES);
                }

                boolean validMapKey;
                if (keyValueField) {
                    BLangRecordKeyValueField keyValField = (BLangRecordKeyValueField) field;
                    BLangRecordKey key = keyValField.key;
                    validMapKey = checkValidJsonOrMapLiteralKeyExpr(key.expr, key.computedKey, data);
                    readOnlyConstructorField = keyValField.readonly;
                    pos = key.pos;
                    fieldName = getKeyValueFieldName(keyValField);
                } else {
                    BLangRecordVarNameField varNameField = (BLangRecordVarNameField) field;
                    validMapKey = checkValidJsonOrMapLiteralKeyExpr(varNameField, false, data);
                    readOnlyConstructorField = varNameField.readonly;
                    pos = varNameField.pos;
                    fieldName = getVarNameFieldName(varNameField);
                }

                fieldType = validMapKey ? ((BMapType) mappingType).constraint : symTable.semanticError;
                break;
        }


        if (readOnlyConstructorField) {
            if (types.isSelectivelyImmutableType(fieldType, data.env.enclPkg.packageID)) {
                fieldType =
                        ImmutableTypeCloner.getImmutableIntersectionType(pos, types, fieldType, data.env, symTable,
                                anonymousModelHelper, names, new HashSet<>());
            } else if (!types.isInherentlyImmutableType(fieldType)) {
                dlog.error(pos, DiagnosticErrorCode.INVALID_READONLY_MAPPING_FIELD, fieldName, fieldType);
                fieldType = symTable.semanticError;
            }
        }

        if (spreadOpField) {
            // If we reach this point for a spread operator it is due to the mapping type being a semantic error.
            // In such a scenario, valueExpr would be null here, and fieldType would be symTable.semanticError.
            // We set the spread op expression as the valueExpr here, to check it against symTable.semanticError.
            valueExpr = ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr;
        }

        BLangExpression exprToCheck = valueExpr;
        if (data.commonAnalyzerData.nonErrorLoggingCheck) {
            exprToCheck = nodeCloner.cloneNode(valueExpr);
        } else {
            ((BLangNode) field).setBType(fieldType);
        }

        return checkExpr(exprToCheck, data.env,
                isOptional ? types.addNilForNillableAccessType(fieldType) : fieldType, data);
    }

    private BType checkSpreadFieldWithMapType(BType spreadOpType) {
        switch (spreadOpType.tag) {
            case TypeTags.RECORD:
                List<BType> types = new ArrayList<>();
                BRecordType recordType = (BRecordType) spreadOpType;

                for (BField recField : recordType.fields.values()) {
                    types.add(recField.type);
                }

                if (!recordType.sealed) {
                    types.add(recordType.restFieldType);
                }

                return getRepresentativeBroadType(types);
            case TypeTags.MAP:
                return ((BMapType) spreadOpType).constraint;
            case TypeTags.TYPEREFDESC:
                BType refType = Types.getReferredType(spreadOpType);
                BType compatibleType = checkSpreadFieldWithMapType(refType);
                return compatibleType == refType ? spreadOpType : compatibleType;
            default:
                return symTable.semanticError;
        }
    }

    private TypeSymbolPair checkRecordLiteralKeyExpr(BLangExpression keyExpr, boolean computedKey,
                                                     BRecordType recordType, AnalyzerData data) {
        Name fieldName;

        if (computedKey) {
            checkExpr(keyExpr, symTable.stringType, data);

            if (keyExpr.getBType() == symTable.semanticError) {
                return new TypeSymbolPair(null, symTable.semanticError);
            }

            LinkedHashSet<BType> fieldTypes = recordType.fields.values().stream()
                    .map(field -> field.type)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (recordType.restFieldType.tag != TypeTags.NONE) {
                fieldTypes.add(recordType.restFieldType);
            }

            return new TypeSymbolPair(null, BUnionType.create(null, fieldTypes));
        } else if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) keyExpr;
            fieldName = names.fromIdNode(varRef.variableName);
        } else if (keyExpr.getKind() == NodeKind.LITERAL && keyExpr.getBType().tag == TypeTags.STRING) {
            fieldName = names.fromString((String) ((BLangLiteral) keyExpr).value);
        } else {
            dlog.error(keyExpr.pos, DiagnosticErrorCode.INVALID_RECORD_LITERAL_KEY);
            return new TypeSymbolPair(null, symTable.semanticError);
        }

        // Check whether the struct field exists
        BSymbol fieldSymbol = symResolver.resolveStructField(keyExpr.pos, data.env, fieldName, recordType.tsymbol);
        BType type = checkRecordLiteralKeyByName(keyExpr.pos, fieldSymbol, fieldName, recordType);

        return new TypeSymbolPair(fieldSymbol instanceof BVarSymbol ? (BVarSymbol) fieldSymbol : null, type);
    }

    private BType checkRecordLiteralKeyByName(Location location, BSymbol fieldSymbol, Name key,
                                              BRecordType recordType) {
        if (fieldSymbol != symTable.notFoundSymbol) {
            return fieldSymbol.type;
        }

        if (recordType.sealed) {
            dlog.error(location, DiagnosticErrorCode.UNDEFINED_STRUCTURE_FIELD_WITH_TYPE, key,
                       recordType.tsymbol.type.getKind().typeName(), recordType);
            return symTable.semanticError;
        }

        return recordType.restFieldType;
    }

    private BType getAllFieldType(BRecordType recordType) {
        LinkedHashSet<BType> possibleTypes = new LinkedHashSet<>();

        for (BField field : recordType.fields.values()) {
            possibleTypes.add(field.type);
        }

        BType restFieldType = recordType.restFieldType;

        if (restFieldType != null && restFieldType != symTable.noType) {
            possibleTypes.add(restFieldType);
        }

        return BUnionType.create(null, possibleTypes);
    }

    private boolean checkValidJsonOrMapLiteralKeyExpr(BLangExpression keyExpr, boolean computedKey, AnalyzerData data) {
        if (computedKey) {
            checkExpr(keyExpr, symTable.stringType, data);

            if (keyExpr.getBType() == symTable.semanticError) {
                return false;
            }
            return true;
        } else if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF ||
                (keyExpr.getKind() == NodeKind.LITERAL && ((BLangLiteral) keyExpr).getBType().tag == TypeTags.STRING)) {
            return true;
        }
        dlog.error(keyExpr.pos, DiagnosticErrorCode.INVALID_RECORD_LITERAL_KEY);
        return false;
    }

    private BType checkRecordRequiredFieldAccess(BLangAccessExpression varReferExpr, Name fieldName,
                                                 BRecordType recordType, AnalyzerData data) {
        BSymbol fieldSymbol = symResolver.resolveStructField(varReferExpr.pos, data.env, fieldName, recordType.tsymbol);

        if (Symbols.isOptional(fieldSymbol) || fieldSymbol == symTable.notFoundSymbol) {
            return symTable.semanticError;
        }

        // Set the field symbol to use during the code generation phase.
        varReferExpr.symbol = fieldSymbol;
        return fieldSymbol.type;
    }

    private BType checkRecordOptionalFieldAccess(BLangAccessExpression varReferExpr, Name fieldName,
                                                 BRecordType recordType, AnalyzerData data) {
        BSymbol fieldSymbol = symResolver.resolveStructField(varReferExpr.pos, data.env, fieldName, recordType.tsymbol);

        if (fieldSymbol == symTable.notFoundSymbol || !Symbols.isOptional(fieldSymbol)) {
            return symTable.semanticError;
        }

        // Set the field symbol to use during the code generation phase.
        varReferExpr.symbol = fieldSymbol;
        return fieldSymbol.type;
    }

    private BType checkRecordRestFieldAccess(BLangAccessExpression varReferExpr, Name fieldName,
                                             BRecordType recordType, AnalyzerData data) {
        BSymbol fieldSymbol = symResolver.resolveStructField(varReferExpr.pos, data.env, fieldName, recordType.tsymbol);

        if (fieldSymbol != symTable.notFoundSymbol) {
            // The field should not exist as a required or optional field.
            return symTable.semanticError;
        }

        if (recordType.sealed) {
            return symTable.semanticError;
        }

        return recordType.restFieldType;
    }

    private BType checkObjectFieldAccess(BLangFieldBasedAccess bLangFieldBasedAccess,
                                         Name fieldName, BObjectType objectType, AnalyzerData data) {
        BSymbol fieldSymbol = symResolver.resolveStructField(bLangFieldBasedAccess.pos,
                data.env, fieldName, objectType.tsymbol);

        if (fieldSymbol != symTable.notFoundSymbol) {
            // Setting the field symbol. This is used during the code generation phase
            bLangFieldBasedAccess.symbol = fieldSymbol;
            return fieldSymbol.type;
        }

        // check if it is an attached function pointer call
        Name objFuncName = names.fromString(Symbols.getAttachedFuncSymbolName(objectType.tsymbol.name.value,
                fieldName.value));
        fieldSymbol =
                symResolver.resolveObjectField(bLangFieldBasedAccess.pos, data.env, objFuncName, objectType.tsymbol);

        if (fieldSymbol == symTable.notFoundSymbol) {
            dlog.error(bLangFieldBasedAccess.field.pos,
                    DiagnosticErrorCode.UNDEFINED_STRUCTURE_FIELD_WITH_TYPE, fieldName,
                    objectType.tsymbol.type.getKind().typeName(), objectType.tsymbol);
            return symTable.semanticError;
        }

        if (Symbols.isFlagOn(fieldSymbol.flags, Flags.REMOTE)) {
            dlog.error(bLangFieldBasedAccess.field.pos,
                       DiagnosticErrorCode.CANNOT_USE_FIELD_ACCESS_TO_ACCESS_A_REMOTE_METHOD);
            return symTable.semanticError;
        }

        if (Symbols.isFlagOn(fieldSymbol.type.flags, Flags.ISOLATED) &&
                !Symbols.isFlagOn(objectType.flags, Flags.ISOLATED)) {
            fieldSymbol = ASTBuilderUtil.duplicateInvokableSymbol((BInvokableSymbol) fieldSymbol);

            fieldSymbol.flags &= ~Flags.ISOLATED;
            fieldSymbol.type.flags &= ~Flags.ISOLATED;
        }

        // Setting the field symbol. This is used during the code generation phase
        bLangFieldBasedAccess.symbol = fieldSymbol;
        return fieldSymbol.type;
    }

    private BType checkTupleFieldType(BType tupleType, int indexValue) {
        BTupleType bTupleType = (BTupleType) tupleType;
        List<BType> tupleMemberTypes = bTupleType.getTupleTypes();
        if (tupleMemberTypes.size() <= indexValue && bTupleType.restType != null) {
            return bTupleType.restType;
        } else if (indexValue < 0 || tupleMemberTypes.size() <= indexValue) {
            return symTable.semanticError;
        }
        return tupleMemberTypes.get(indexValue);
    }

    private void validateTags(BLangXMLElementLiteral bLangXMLElementLiteral, SymbolEnv xmlElementEnv,
                              AnalyzerData data) {
        // check type for start and end tags
        BLangExpression startTagName = bLangXMLElementLiteral.startTagName;
        checkExpr(startTagName, xmlElementEnv, symTable.stringType, data);
        BLangExpression endTagName = bLangXMLElementLiteral.endTagName;
        if (endTagName == null) {
            return;
        }

        checkExpr(endTagName, xmlElementEnv, symTable.stringType, data);
        if (startTagName.getKind() == NodeKind.XML_QNAME && endTagName.getKind() == NodeKind.XML_QNAME &&
                startTagName.equals(endTagName)) {
            return;
        }

        if (startTagName.getKind() != NodeKind.XML_QNAME && endTagName.getKind() != NodeKind.XML_QNAME) {
            return;
        }

        dlog.error(bLangXMLElementLiteral.pos, DiagnosticErrorCode.XML_TAGS_MISMATCH);
    }

    private void checkStringTemplateExprs(List<? extends BLangExpression> exprs, AnalyzerData data) {
        for (BLangExpression expr : exprs) {
            checkExpr(expr, data);

            BType type = expr.getBType();

            if (type == symTable.semanticError) {
                continue;
            }

            if (!types.isNonNilSimpleBasicTypeOrString(type)) {
                dlog.error(expr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                        BUnionType.create(null, symTable.intType, symTable.floatType,
                                symTable.decimalType, symTable.stringType,
                                symTable.booleanType), type);
            }
        }
    }

    /**
     * Concatenate the consecutive text type nodes, and get the reduced set of children.
     *
     * @param exprs         Child nodes
     * @param xmlElementEnv
     * @return Reduced set of children
     */
    private List<BLangExpression> concatSimilarKindXMLNodes(List<BLangExpression> exprs, SymbolEnv xmlElementEnv,
                                                            AnalyzerData data) {
        List<BLangExpression> newChildren = new ArrayList<>();
        List<BLangExpression> tempConcatExpressions = new ArrayList<>();

        for (BLangExpression expr : exprs) {
            boolean prevNonErrorLoggingCheck = data.commonAnalyzerData.nonErrorLoggingCheck;
            data.commonAnalyzerData.nonErrorLoggingCheck = true;
            int prevErrorCount = this.dlog.errorCount();
            this.dlog.resetErrorCount();
            this.dlog.mute();

            BType exprType = checkExpr(nodeCloner.cloneNode(expr), xmlElementEnv, symTable.xmlType, data);

            data.commonAnalyzerData.nonErrorLoggingCheck = prevNonErrorLoggingCheck;
            int errorCount = this.dlog.errorCount();
            this.dlog.setErrorCount(prevErrorCount);

            if (!prevNonErrorLoggingCheck) {
                this.dlog.unmute();
            }

            if (errorCount == 0 && exprType != symTable.semanticError) {
                exprType = checkExpr(expr, xmlElementEnv, symTable.xmlType, data);
            } else {
                exprType = checkExpr(expr, xmlElementEnv, data);
            }

            if (TypeTags.isXMLTypeTag(Types.getReferredType(exprType).tag)) {
                if (!tempConcatExpressions.isEmpty()) {
                    newChildren.add(getXMLTextLiteral(tempConcatExpressions));
                    tempConcatExpressions = new ArrayList<>();
                }
                newChildren.add(expr);
                continue;
            }

            BType type = expr.getBType();
            BType referredType = Types.getReferredType(type);
            if (referredType.tag >= TypeTags.JSON &&
                    !TypeTags.isIntegerTypeTag(referredType.tag) && !TypeTags.isStringTypeTag(referredType.tag)) {
                if (referredType != symTable.semanticError && !TypeTags.isXMLTypeTag(referredType.tag)) {
                    dlog.error(expr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                            BUnionType.create(null, symTable.intType, symTable.floatType,
                                    symTable.decimalType, symTable.stringType,
                                    symTable.booleanType, symTable.xmlType), type);
                }
                continue;
            }

            tempConcatExpressions.add(expr);
        }

        // Add remaining concatenated text nodes as children
        if (!tempConcatExpressions.isEmpty()) {
            newChildren.add(getXMLTextLiteral(tempConcatExpressions));
        }

        return newChildren;
    }

    private BLangExpression getXMLTextLiteral(List<BLangExpression> exprs) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.textFragments = exprs;
        xmlTextLiteral.pos = exprs.get(0).pos;
        xmlTextLiteral.setBType(symTable.xmlType);
        return xmlTextLiteral;
    }

    private boolean returnsNull(BLangAccessExpression accessExpr) {
        BType parentType = accessExpr.expr.getBType();
        if (parentType.isNullable() && parentType.tag != TypeTags.JSON) {
            return true;
        }

        // Check whether this is a map access by index. If not, null is not a possible return type.
        if (parentType.tag != TypeTags.MAP) {
            return false;
        }

        // A map access with index, returns nullable type
        if (accessExpr.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR
                && accessExpr.expr.getBType().tag == TypeTags.MAP) {
            BType constraintType = ((BMapType) accessExpr.expr.getBType()).constraint;

            // JSON and any is special cased here, since those are two union types, with null within them.
            // Therefore return 'type' will not include null.
            return constraintType != null && constraintType.tag != TypeTags.ANY && constraintType.tag != TypeTags.JSON;
        }

        return false;
    }

    private BType checkObjectFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType, Name fieldName,
                                             AnalyzerData data) {
        if (varRefType.tag == TypeTags.OBJECT) {
            return checkObjectFieldAccess(fieldAccessExpr, fieldName, (BObjectType) varRefType, data);
        }

        // If the type is not an object, it needs to be a union of objects.
        // Resultant field type is calculated here.
        Set<BType> memberTypes = ((BUnionType) varRefType).getMemberTypes();

        LinkedHashSet<BType> fieldTypeMembers = new LinkedHashSet<>();

        for (BType memType : memberTypes) {
            BType individualFieldType = checkObjectFieldAccess(fieldAccessExpr, fieldName, (BObjectType) memType, data);

            if (individualFieldType == symTable.semanticError) {
                return individualFieldType;
            }

            fieldTypeMembers.add(individualFieldType);
        }

        if (fieldTypeMembers.size() == 1) {
            return fieldTypeMembers.iterator().next();
        }

        return BUnionType.create(null, fieldTypeMembers);
    }

    private BType checkRecordFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType type, Name fieldName,
                                             AnalyzerData data) {
        BType varRefType = Types.getReferredType(type);
        if (varRefType.tag == TypeTags.RECORD) {
            BSymbol fieldSymbol = symResolver.resolveStructField(fieldAccessExpr.pos, data.env,
                    fieldName, varRefType.tsymbol);

            if (Symbols.isOptional(fieldSymbol) && !fieldSymbol.type.isNullable() && !fieldAccessExpr.isLValue) {
                fieldAccessExpr.symbol = fieldSymbol;
                return types.addNilForNillableAccessType(fieldSymbol.type);
            }
            return checkRecordRequiredFieldAccess(fieldAccessExpr, fieldName, (BRecordType) varRefType, data);
        }

        // If the type is not a record, it needs to be a union of records.
        // Resultant field type is calculated here.
        Set<BType> memberTypes = ((BUnionType) varRefType).getMemberTypes();

        // checks whether if the field symbol type is nilable and the field is optional in other records
        for (BType memType : memberTypes) {
            BSymbol fieldSymbol = symResolver.resolveStructField(fieldAccessExpr.pos, data.env,
                    fieldName, memType.tsymbol);
            if (fieldSymbol.type.isNullable() &&
                    isFieldOptionalInRecords(((BUnionType) varRefType), fieldName, fieldAccessExpr, data)) {
                return symTable.semanticError;
            }
        }

        LinkedHashSet<BType> fieldTypeMembers = new LinkedHashSet<>();

        for (BType memType : memberTypes) {
            BType individualFieldType = checkRecordFieldAccessExpr(fieldAccessExpr, memType, fieldName, data);

            if (individualFieldType == symTable.semanticError) {
                return individualFieldType;
            }

            fieldTypeMembers.add(individualFieldType);
        }

        if (fieldTypeMembers.size() == 1) {
            return fieldTypeMembers.iterator().next();
        }

        return BUnionType.create(null, fieldTypeMembers);
    }

    private boolean isFieldOptionalInRecords(BUnionType unionType, Name fieldName,
                                             BLangFieldBasedAccess fieldAccessExpr, AnalyzerData data) {
        Set<BType> memberTypes = unionType.getMemberTypes();
        for (BType memType: memberTypes) {
            BSymbol fieldSymbol = symResolver.resolveStructField(fieldAccessExpr.pos, data.env,
                    fieldName, memType.tsymbol);
            if (Symbols.isOptional(fieldSymbol)) {
                return true;
            }
        }
        return false;
    }

    private BType checkRecordFieldAccessLhsExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType,
                                                Name fieldName, AnalyzerData data) {
        if (varRefType.tag == TypeTags.RECORD) {
            BType fieldType =
                    checkRecordRequiredFieldAccess(fieldAccessExpr, fieldName, (BRecordType) varRefType, data);
            if (fieldType != symTable.semanticError) {
                return fieldType;
            }

            // For the LHS, the field could be optional.
            return checkRecordOptionalFieldAccess(fieldAccessExpr, fieldName, (BRecordType) varRefType, data);
        }

        // If the type is not an record, it needs to be a union of records.
        // Resultant field type is calculated here.
        Set<BType> memberTypes = ((BUnionType) varRefType).getMemberTypes();

        LinkedHashSet<BType> fieldTypeMembers = new LinkedHashSet<>();

        for (BType memType : memberTypes) {
            BType individualFieldType = checkRecordFieldAccessLhsExpr(fieldAccessExpr, memType, fieldName, data);

            if (individualFieldType == symTable.semanticError) {
                return symTable.semanticError;
            }

            fieldTypeMembers.add(individualFieldType);
        }

        if (fieldTypeMembers.size() == 1) {
            return fieldTypeMembers.iterator().next();
        }

        return BUnionType.create(null, fieldTypeMembers);
    }

    private BType checkOptionalRecordFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType,
                                                     Name fieldName, AnalyzerData data) {
        BType refType = Types.getReferredType(varRefType);
        if (refType.tag == TypeTags.RECORD) {
            BType fieldType = checkRecordRequiredFieldAccess(fieldAccessExpr, fieldName, (BRecordType) refType, data);
            if (fieldType != symTable.semanticError) {
                return fieldType;
            }

            fieldType = checkRecordOptionalFieldAccess(fieldAccessExpr, fieldName, (BRecordType) refType, data);
            if (fieldType == symTable.semanticError) {
                return fieldType;
            }
            return types.addNilForNillableAccessType(fieldType);
        }

        // If the type is not an record, it needs to be a union of records.
        // Resultant field type is calculated here.
        Set<BType> memberTypes = ((BUnionType) refType).getMemberTypes();

        BType fieldType;

        boolean nonMatchedRecordExists = false;

        LinkedHashSet<BType> fieldTypeMembers = new LinkedHashSet<>();

        for (BType memType : memberTypes) {
            BType individualFieldType = checkOptionalRecordFieldAccessExpr(fieldAccessExpr, memType, fieldName, data);

            if (individualFieldType == symTable.semanticError) {
                nonMatchedRecordExists = true;
                continue;
            }

            fieldTypeMembers.add(individualFieldType);
        }

        if (fieldTypeMembers.isEmpty()) {
            return symTable.semanticError;
        }

        if (fieldTypeMembers.size() == 1) {
            fieldType = fieldTypeMembers.iterator().next();
        } else {
            fieldType = BUnionType.create(null, fieldTypeMembers);
        }

        return nonMatchedRecordExists ? types.addNilForNillableAccessType(fieldType) : fieldType;
    }

    private RecordUnionDiagnostics checkRecordUnion(BLangFieldBasedAccess fieldAccessExpr, Set<BType> memberTypes,
                                                    Name fieldName, AnalyzerData data) {

        RecordUnionDiagnostics recordUnionDiagnostics = new RecordUnionDiagnostics();

        for (BType memberType : memberTypes) {
            BRecordType recordMember = (BRecordType) Types.getReferredType(memberType);

            if (recordMember.getFields().containsKey(fieldName.getValue())) {

                if (isNilableType(fieldAccessExpr, memberType, fieldName, data)) {
                    recordUnionDiagnostics.nilableInRecords.add(recordMember);
                }

            } else {
                // The field being accessed is not declared in this record member type
                recordUnionDiagnostics.undeclaredInRecords.add(recordMember);
            }

        }

        return recordUnionDiagnostics;
    }

    private boolean isNilableType(BLangFieldBasedAccess fieldAccessExpr, BType memberType,
                              Name fieldName, AnalyzerData data) {
        BSymbol fieldSymbol = symResolver.resolveStructField(fieldAccessExpr.pos, data.env,
                fieldName, memberType.tsymbol);
        return fieldSymbol.type.isNullable();
    }

    private void logRhsFieldAccExprErrors(BLangFieldBasedAccess fieldAccessExpr, BType varRefType, Name fieldName,
                                          AnalyzerData data) {
        if (varRefType.tag == TypeTags.RECORD) {

            BRecordType recordVarRefType = (BRecordType) varRefType;
            boolean isFieldDeclared = recordVarRefType.getFields().containsKey(fieldName.getValue());

            if (isFieldDeclared) {
                // The field being accessed using the field access expression is declared as an optional field
                dlog.error(fieldAccessExpr.pos,
                        DiagnosticErrorCode.FIELD_ACCESS_CANNOT_BE_USED_TO_ACCESS_OPTIONAL_FIELDS);
            } else if (recordVarRefType.sealed) {
                // Accessing an undeclared field in a close record
                dlog.error(fieldAccessExpr.pos, DiagnosticErrorCode.UNDECLARED_FIELD_IN_RECORD, fieldName, varRefType);

            } else {
                // The field accessed is either not declared or maybe declared as a rest field in an open record
                dlog.error(fieldAccessExpr.pos, DiagnosticErrorCode.INVALID_FIELD_ACCESS_IN_RECORD_TYPE, fieldName,
                        varRefType);
            }

        } else {
            // If the type is not a record, it needs to be a union of records
            LinkedHashSet<BType> memberTypes = ((BUnionType) varRefType).getMemberTypes();
            RecordUnionDiagnostics recUnionInfo = checkRecordUnion(fieldAccessExpr, memberTypes, fieldName, data);

            if (recUnionInfo.hasNilableAndUndeclared()) {

                dlog.error(fieldAccessExpr.pos,
                        DiagnosticErrorCode.UNDECLARED_AND_NILABLE_FIELDS_IN_UNION_OF_RECORDS, fieldName,
                        recUnionInfo.recordsToString(recUnionInfo.undeclaredInRecords),
                        recUnionInfo.recordsToString(recUnionInfo.nilableInRecords));
            } else if (recUnionInfo.hasUndeclared()) {

                dlog.error(fieldAccessExpr.pos, DiagnosticErrorCode.UNDECLARED_FIELD_IN_UNION_OF_RECORDS, fieldName,
                        recUnionInfo.recordsToString(recUnionInfo.undeclaredInRecords));
            } else if (recUnionInfo.hasNilable()) {

                dlog.error(fieldAccessExpr.pos, DiagnosticErrorCode.NILABLE_FIELD_IN_UNION_OF_RECORDS, fieldName,
                        recUnionInfo.recordsToString(recUnionInfo.nilableInRecords));
            }
        }
    }

    private BType checkFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType, Name fieldName,
                                       AnalyzerData data) {
        BType actualType = symTable.semanticError;
        varRefType = Types.getReferredType(varRefType);

        if (types.isSubTypeOfBaseType(varRefType, TypeTags.OBJECT)) {
            actualType = checkObjectFieldAccessExpr(fieldAccessExpr, varRefType, fieldName, data);
            fieldAccessExpr.originalType = actualType;
        } else if (types.isSubTypeOfBaseType(varRefType, TypeTags.RECORD)) {
            actualType = checkRecordFieldAccessExpr(fieldAccessExpr, varRefType, fieldName, data);

            if (actualType != symTable.semanticError) {
                fieldAccessExpr.originalType = actualType;
                return actualType;
            }

            if (!fieldAccessExpr.isLValue) {
                logRhsFieldAccExprErrors(fieldAccessExpr, varRefType, fieldName, data);
                return actualType;
            }

            // If this is an LHS expression, check if there is a required and/ optional field by the specified field
            // name in all records.
            actualType = checkRecordFieldAccessLhsExpr(fieldAccessExpr, varRefType, fieldName, data);
            fieldAccessExpr.originalType = actualType;
            if (actualType == symTable.semanticError) {
                dlog.error(fieldAccessExpr.pos, DiagnosticErrorCode.UNDEFINED_STRUCTURE_FIELD_WITH_TYPE,
                        fieldName,
                        varRefType.getKind() == TypeKind.UNION ? "union" : varRefType.getKind().typeName(), varRefType);
            }
        } else if (types.isLax(varRefType)) {
            if (fieldAccessExpr.isLValue) {
                dlog.error(fieldAccessExpr.pos,
                        DiagnosticErrorCode.OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS_FOR_ASSIGNMENT,
                        varRefType);
                return symTable.semanticError;
            }
            if (fieldAccessExpr.fieldKind == FieldKind.WITH_NS) {
                resolveXMLNamespace((BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess) fieldAccessExpr, data);
            }
            BType laxFieldAccessType = getLaxFieldAccessType(varRefType);
            actualType = BUnionType.create(null, laxFieldAccessType, symTable.errorType);
            fieldAccessExpr.originalType = laxFieldAccessType;
        } else if (fieldAccessExpr.expr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR &&
                hasLaxOriginalType(((BLangFieldBasedAccess) fieldAccessExpr.expr))) {
            BType laxFieldAccessType =
                    getLaxFieldAccessType(((BLangFieldBasedAccess) fieldAccessExpr.expr).originalType);
            if (fieldAccessExpr.fieldKind == FieldKind.WITH_NS) {
                resolveXMLNamespace((BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess) fieldAccessExpr, data);
            }
            actualType = BUnionType.create(null, laxFieldAccessType, symTable.errorType);
            fieldAccessExpr.errorSafeNavigation = true;
            fieldAccessExpr.originalType = laxFieldAccessType;
        } else if (TypeTags.isXMLTypeTag(varRefType.tag)) {
            if (fieldAccessExpr.isLValue) {
                dlog.error(fieldAccessExpr.pos, DiagnosticErrorCode.CANNOT_UPDATE_XML_SEQUENCE);
            }
            // todo: field access on a xml value is not attribute access, return type should be string?
            // `_` is a special field that refer to the element name.
            actualType = symTable.xmlType;
            fieldAccessExpr.originalType = actualType;
        } else if (varRefType.tag != TypeTags.SEMANTIC_ERROR) {
            dlog.error(fieldAccessExpr.pos, DiagnosticErrorCode.OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS,
                    varRefType);
        }

        return actualType;
    }

    private void resolveXMLNamespace(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess fieldAccessExpr,
                                     AnalyzerData data) {
        BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess nsPrefixedFieldAccess = fieldAccessExpr;
        String nsPrefix = nsPrefixedFieldAccess.nsPrefix.value;
        BSymbol nsSymbol = symResolver.lookupSymbolInPrefixSpace(data.env, names.fromString(nsPrefix));

        if (nsSymbol == symTable.notFoundSymbol) {
            dlog.error(nsPrefixedFieldAccess.nsPrefix.pos, DiagnosticErrorCode.CANNOT_FIND_XML_NAMESPACE,
                    nsPrefixedFieldAccess.nsPrefix);
        } else if (nsSymbol.getKind() == SymbolKind.PACKAGE) {
            nsPrefixedFieldAccess.nsSymbol = (BXMLNSSymbol) findXMLNamespaceFromPackageConst(
                    nsPrefixedFieldAccess.field.value, nsPrefixedFieldAccess.nsPrefix.value,
                    (BPackageSymbol) nsSymbol, fieldAccessExpr.pos, data);
        } else {
            nsPrefixedFieldAccess.nsSymbol = (BXMLNSSymbol) nsSymbol;
        }
    }

    private boolean hasLaxOriginalType(BLangFieldBasedAccess fieldBasedAccess) {
        return fieldBasedAccess.originalType != null && types.isLax(fieldBasedAccess.originalType);
    }

    private BType getLaxFieldAccessType(BType exprType) {
        switch (exprType.tag) {
            case TypeTags.JSON:
                return symTable.jsonType;
            case TypeTags.XML:
            case TypeTags.XML_ELEMENT:
                return symTable.stringType;
            case TypeTags.MAP:
                return ((BMapType) exprType).constraint;
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) exprType;
                if (types.isSameType(symTable.jsonType, unionType)) {
                    return symTable.jsonType;
                }
                LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
                unionType.getMemberTypes().forEach(bType -> memberTypes.add(getLaxFieldAccessType(bType)));
                return memberTypes.size() == 1 ? memberTypes.iterator().next() : BUnionType.create(null, memberTypes);
            case TypeTags.TYPEREFDESC:
                BType refType = Types.getReferredType(exprType);
                BType compatibleType = getLaxFieldAccessType(refType);
                return compatibleType == refType ? exprType : compatibleType;
        }
        return symTable.semanticError;
    }

    private BType checkOptionalFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType, Name fieldName,
                                               AnalyzerData data) {
        BType actualType = symTable.semanticError;
        BType referredType = Types.getReferredType(varRefType);

        boolean nillableExprType = false;
        BType effectiveType = Types.getReferredType(varRefType);

        if (referredType.tag == TypeTags.UNION) {
            Set<BType> memTypes = ((BUnionType) referredType).getMemberTypes();

            if (memTypes.contains(symTable.nilType)) {
                LinkedHashSet<BType> nilRemovedSet = new LinkedHashSet<>();
                for (BType bType : memTypes) {
                    if (bType != symTable.nilType) {
                        nilRemovedSet.add(bType);
                    } else {
                        nillableExprType = true;
                    }
                }

                effectiveType = nilRemovedSet.size() == 1 ? nilRemovedSet.iterator().next() :
                        BUnionType.create(null, nilRemovedSet);
            }
        }

        if (types.isSubTypeOfBaseType(effectiveType, TypeTags.RECORD)) {
            actualType = checkOptionalRecordFieldAccessExpr(fieldAccessExpr, effectiveType, fieldName, data);
            if (actualType == symTable.semanticError) {
                dlog.error(fieldAccessExpr.pos,
                        DiagnosticErrorCode.OPERATION_DOES_NOT_SUPPORT_OPTIONAL_FIELD_ACCESS_FOR_FIELD,
                        varRefType, fieldName);
            }
            fieldAccessExpr.nilSafeNavigation = nillableExprType;
            fieldAccessExpr.originalType = fieldAccessExpr.leafNode || !nillableExprType ? actualType :
                    types.getTypeWithoutNil(actualType);
        } else if (types.isLax(effectiveType)) {
            BType laxFieldAccessType = getLaxFieldAccessType(effectiveType);
            actualType = accessCouldResultInError(effectiveType) ?
                    BUnionType.create(null, laxFieldAccessType, symTable.errorType) : laxFieldAccessType;
            if (fieldAccessExpr.fieldKind == FieldKind.WITH_NS) {
                resolveXMLNamespace((BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess) fieldAccessExpr, data);
            }
            fieldAccessExpr.originalType = laxFieldAccessType;
            fieldAccessExpr.nilSafeNavigation = true;
            nillableExprType = true;
        } else if (fieldAccessExpr.expr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR &&
                hasLaxOriginalType(((BLangFieldBasedAccess) fieldAccessExpr.expr))) {
            BType laxFieldAccessType =
                    getLaxFieldAccessType(((BLangFieldBasedAccess) fieldAccessExpr.expr).originalType);
            actualType = accessCouldResultInError(effectiveType) ?
                    BUnionType.create(null, laxFieldAccessType, symTable.errorType) : laxFieldAccessType;
            if (fieldAccessExpr.fieldKind == FieldKind.WITH_NS) {
                resolveXMLNamespace((BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess) fieldAccessExpr, data);
            }
            fieldAccessExpr.errorSafeNavigation = true;
            fieldAccessExpr.originalType = laxFieldAccessType;
            fieldAccessExpr.nilSafeNavigation = true;
            nillableExprType = true;
        } else if (varRefType.tag != TypeTags.SEMANTIC_ERROR) {
            dlog.error(fieldAccessExpr.pos,
                    DiagnosticErrorCode.OPERATION_DOES_NOT_SUPPORT_OPTIONAL_FIELD_ACCESS, varRefType);
        }

        if (nillableExprType && actualType != symTable.semanticError && !actualType.isNullable()) {
            actualType = BUnionType.create(null, actualType, symTable.nilType);
        }

        return actualType;
    }

    private boolean accessCouldResultInError(BType bType) {
        BType type = Types.getReferredType(bType);
        if (type.tag == TypeTags.JSON) {
            return true;
        }

        if (type.tag == TypeTags.MAP) {
            return false;
        }

        if (type.tag == TypeTags.XML) {
            return true;
        }

        if (type.tag == TypeTags.UNION) {
            return ((BUnionType) type).getMemberTypes().stream().anyMatch(this::accessCouldResultInError);
        } else {
            return false;
        }
    }

    private BType checkIndexAccessExpr(BLangIndexBasedAccess indexBasedAccessExpr, AnalyzerData data) {
        BType effectiveType = types.getTypeWithEffectiveIntersectionTypes(indexBasedAccessExpr.expr.getBType());
        BType varRefType = Types.getReferredType(effectiveType);
        boolean nillableExprType = false;

        if (varRefType.tag == TypeTags.UNION) {
            Set<BType> memTypes = ((BUnionType) varRefType).getMemberTypes();

            if (memTypes.contains(symTable.nilType)) {
                LinkedHashSet<BType> nilRemovedSet = new LinkedHashSet<>();
                for (BType bType : memTypes) {
                    if (bType != symTable.nilType) {
                        nilRemovedSet.add(bType);
                    } else {
                        nillableExprType = true;
                    }
                }

                if (nillableExprType) {
                    varRefType = nilRemovedSet.size() == 1 ? nilRemovedSet.iterator().next() :
                            BUnionType.create(null, nilRemovedSet);

                    if (!types.isSubTypeOfMapping(varRefType)) {
                        // Member access is allowed on optional types only with mappings.
                        dlog.error(indexBasedAccessExpr.pos,
                                DiagnosticErrorCode.OPERATION_DOES_NOT_SUPPORT_MEMBER_ACCESS,
                                   indexBasedAccessExpr.expr.getBType());
                        return symTable.semanticError;
                    }

                    if (indexBasedAccessExpr.isLValue || indexBasedAccessExpr.isCompoundAssignmentLValue) {
                        dlog.error(indexBasedAccessExpr.pos,
                                DiagnosticErrorCode.OPERATION_DOES_NOT_SUPPORT_MEMBER_ACCESS_FOR_ASSIGNMENT,
                                   indexBasedAccessExpr.expr.getBType());
                        return symTable.semanticError;
                    }
                }
            }
        }


        BLangExpression indexExpr = indexBasedAccessExpr.indexExpr;
        BType actualType = symTable.semanticError;

        if (types.isSubTypeOfMapping(varRefType)) {
            checkExpr(indexExpr, symTable.stringType, data);

            if (indexExpr.getBType() == symTable.semanticError) {
                return symTable.semanticError;
            }

            actualType = checkMappingIndexBasedAccess(indexBasedAccessExpr, varRefType, data);

            if (actualType == symTable.semanticError) {
                if (isConstExpr(indexExpr)) {
                    String fieldName = getConstFieldName(indexExpr);
                    dlog.error(indexBasedAccessExpr.pos, DiagnosticErrorCode.UNDEFINED_STRUCTURE_FIELD,
                            fieldName, indexBasedAccessExpr.expr.getBType());
                    return actualType;
                }

                dlog.error(indexExpr.pos, DiagnosticErrorCode.INVALID_RECORD_MEMBER_ACCESS_EXPR, indexExpr.getBType());
                return actualType;
            }

            indexBasedAccessExpr.nilSafeNavigation = nillableExprType;
            indexBasedAccessExpr.originalType = indexBasedAccessExpr.leafNode || !nillableExprType ? actualType :
                    types.getTypeWithoutNil(actualType);
        } else if (types.isSubTypeOfList(varRefType)) {
            checkExpr(indexExpr, symTable.intType, data);

            if (indexExpr.getBType() == symTable.semanticError) {
                return symTable.semanticError;
            }

            actualType = checkListIndexBasedAccess(indexBasedAccessExpr, varRefType);
            indexBasedAccessExpr.originalType = actualType;

            if (actualType == symTable.semanticError) {
                if (isConstExpr(indexExpr)) {
                    dlog.error(indexBasedAccessExpr.indexExpr.pos,
                            DiagnosticErrorCode.LIST_INDEX_OUT_OF_RANGE, getConstIndex(indexExpr));
                    return actualType;
                }
                dlog.error(indexExpr.pos, DiagnosticErrorCode.INVALID_LIST_MEMBER_ACCESS_EXPR, indexExpr.getBType());
                return actualType;
            }
        } else if (types.isAssignable(varRefType, symTable.stringType)) {
            if (indexBasedAccessExpr.isLValue) {
                dlog.error(indexBasedAccessExpr.pos,
                        DiagnosticErrorCode.OPERATION_DOES_NOT_SUPPORT_MEMBER_ACCESS_FOR_ASSIGNMENT,
                           indexBasedAccessExpr.expr.getBType());
                return symTable.semanticError;
            }

            checkExpr(indexExpr, symTable.intType, data);

            if (indexExpr.getBType() == symTable.semanticError) {
                return symTable.semanticError;
            }

            indexBasedAccessExpr.originalType = symTable.charStringType;
            actualType = symTable.charStringType;
        } else if (TypeTags.isXMLTypeTag(varRefType.tag)) {
            if (indexBasedAccessExpr.isLValue) {
                indexExpr.setBType(symTable.semanticError);
                dlog.error(indexBasedAccessExpr.pos, DiagnosticErrorCode.CANNOT_UPDATE_XML_SEQUENCE);
                return actualType;
            }

            BType type = checkExpr(indexExpr, symTable.intType, data);
            if (type == symTable.semanticError) {
                return type;
            }
            // Note: out of range member access returns empty xml value unlike lists
            // hence, this needs to be set to xml type
            indexBasedAccessExpr.originalType = varRefType;
            actualType = varRefType;
        } else if (varRefType.tag == TypeTags.TABLE) {
            if (indexBasedAccessExpr.isLValue) {
                dlog.error(indexBasedAccessExpr.pos, DiagnosticErrorCode.CANNOT_UPDATE_TABLE_USING_MEMBER_ACCESS,
                        varRefType);
                return symTable.semanticError;
            }
            BTableType tableType = (BTableType) Types.getReferredType(indexBasedAccessExpr.expr.getBType());
            BType keyTypeConstraint = tableType.keyTypeConstraint;
            if (tableType.keyTypeConstraint == null) {
                keyTypeConstraint = createTableKeyConstraint(tableType.fieldNameList, tableType.constraint);

                if (keyTypeConstraint == symTable.semanticError) {
                    dlog.error(indexBasedAccessExpr.pos,
                               DiagnosticErrorCode.MEMBER_ACCESS_NOT_SUPPORTED_FOR_KEYLESS_TABLE,
                               indexBasedAccessExpr.expr);
                    return symTable.semanticError;
                }
            }

            BType indexExprType = checkExpr(indexExpr, keyTypeConstraint, data);
            if (indexExprType == symTable.semanticError) {
                return symTable.semanticError;
            }

            if (data.expType.tag != TypeTags.NONE) {
                BType resultType = checkExpr(indexBasedAccessExpr.expr, data.expType, data);
                if (resultType == symTable.semanticError) {
                    return symTable.semanticError;
                }
            }
            BType constraint = tableType.constraint;
            actualType = types.addNilForNillableAccessType(constraint);
            indexBasedAccessExpr.originalType = indexBasedAccessExpr.leafNode || !nillableExprType ? actualType :
                    types.getTypeWithoutNil(actualType);
        } else if (varRefType == symTable.semanticError) {
            indexBasedAccessExpr.indexExpr.setBType(symTable.semanticError);
            return symTable.semanticError;
        } else {
            indexBasedAccessExpr.indexExpr.setBType(symTable.semanticError);
            dlog.error(indexBasedAccessExpr.pos, DiagnosticErrorCode.OPERATION_DOES_NOT_SUPPORT_MEMBER_ACCESS,
                       indexBasedAccessExpr.expr.getBType());
            return symTable.semanticError;
        }

        if (nillableExprType && !actualType.isNullable()) {
            actualType = BUnionType.create(null, actualType, symTable.nilType);
        }

        return actualType;
    }

    private Long getConstIndex(BLangExpression indexExpr) {
        switch (indexExpr.getKind()) {
            case GROUP_EXPR:
                BLangGroupExpr groupExpr = (BLangGroupExpr) indexExpr;
                return getConstIndex(groupExpr.expression);
            case NUMERIC_LITERAL:
                return (Long) ((BLangLiteral) indexExpr).value;
            case UNARY_EXPR:
                BLangNumericLiteral numericLiteral =
                        Types.constructNumericLiteralFromUnaryExpr((BLangUnaryExpr) indexExpr);
                return (Long) numericLiteral.value;
            default:
                return (Long) ((BConstantSymbol) ((BLangSimpleVarRef) indexExpr).symbol).value.value;
        }
    }

    private String getConstFieldName(BLangExpression indexExpr) {
        switch (indexExpr.getKind()) {
            case GROUP_EXPR:
                BLangGroupExpr groupExpr = (BLangGroupExpr) indexExpr;
                return getConstFieldName(groupExpr.expression);
            case LITERAL:
                return (String) ((BLangLiteral) indexExpr).value;
            default:
                return (String) ((BConstantSymbol) ((BLangSimpleVarRef) indexExpr).symbol).value.value;
        }
    }

    private BType checkArrayIndexBasedAccess(BLangIndexBasedAccess indexBasedAccess, BType indexExprType,
                                             BArrayType arrayType) {
        BType actualType = symTable.semanticError;

        int tag = indexExprType.tag;

        if (tag == TypeTags.BYTE || TypeTags.isIntegerTypeTag(tag)) {
            BLangExpression indexExpr = indexBasedAccess.indexExpr;
            if (!isConstExpr(indexExpr) || arrayType.state == BArrayState.OPEN) {
                return arrayType.eType;
            }
            Long indexVal = getConstIndex(indexExpr);
            return indexVal >= arrayType.size || indexVal < 0 ? symTable.semanticError : arrayType.eType;
        }

        switch (tag) {
            case TypeTags.FINITE:
                BFiniteType finiteIndexExpr = (BFiniteType) indexExprType;
                boolean validIndexExists = false;
                for (BLangExpression finiteMember : finiteIndexExpr.getValueSpace()) {
                    int indexValue = ((Long) ((BLangLiteral) finiteMember).value).intValue();
                    if (indexValue >= 0 &&
                            (arrayType.state == BArrayState.OPEN || indexValue < arrayType.size)) {
                        validIndexExists = true;
                        break;
                    }
                }
                if (!validIndexExists) {
                    return symTable.semanticError;
                }
                actualType = arrayType.eType;
                break;
            case TypeTags.UNION:
                // address the case where we have a union of finite types
                List<BFiniteType> finiteTypes = ((BUnionType) indexExprType).getMemberTypes().stream()
                        .filter(memType -> Types.getReferredType(memType).tag == TypeTags.FINITE)
                        .map(matchedType -> (BFiniteType) Types.getReferredType(matchedType))
                        .collect(Collectors.toList());

                BFiniteType finiteType;
                if (finiteTypes.size() == 1) {
                    finiteType = finiteTypes.get(0);
                } else {
                    Set<BLangExpression> valueSpace = new LinkedHashSet<>();
                    finiteTypes.forEach(constituent -> valueSpace.addAll(constituent.getValueSpace()));
                    finiteType = new BFiniteType(null, valueSpace);
                }

                BType elementType = checkArrayIndexBasedAccess(indexBasedAccess, finiteType, arrayType);
                if (elementType == symTable.semanticError) {
                    return symTable.semanticError;
                }
                actualType = arrayType.eType;
                break;
            case TypeTags.TYPEREFDESC:
                return checkArrayIndexBasedAccess(indexBasedAccess, Types.getReferredType(indexExprType),
                        arrayType);
        }
        return actualType;
    }

    private BType checkListIndexBasedAccess(BLangIndexBasedAccess accessExpr, BType type) {
        if (type.tag == TypeTags.ARRAY) {
            return checkArrayIndexBasedAccess(accessExpr, accessExpr.indexExpr.getBType(), (BArrayType) type);
        }

        if (type.tag == TypeTags.TUPLE) {
            return checkTupleIndexBasedAccess(accessExpr, (BTupleType) type, accessExpr.indexExpr.getBType());
        }

        LinkedHashSet<BType> fieldTypeMembers = new LinkedHashSet<>();

        for (BType memType : ((BUnionType) type).getMemberTypes()) {
            BType individualFieldType = checkListIndexBasedAccess(accessExpr, memType);

            if (individualFieldType == symTable.semanticError) {
                continue;
            }

            fieldTypeMembers.add(individualFieldType);
        }

        if (fieldTypeMembers.size() == 0) {
            return symTable.semanticError;
        }

        if (fieldTypeMembers.size() == 1) {
            return fieldTypeMembers.iterator().next();
        }
        return BUnionType.create(null, fieldTypeMembers);
    }

    private BType checkTupleIndexBasedAccess(BLangIndexBasedAccess accessExpr, BTupleType tuple, BType currentType) {
        BType actualType = symTable.semanticError;
        BLangExpression indexExpr = accessExpr.indexExpr;

        int tag = currentType.tag;

        if (tag == TypeTags.BYTE || TypeTags.isIntegerTypeTag(tag)) {
            if (isConstExpr(indexExpr)) {
                return checkTupleFieldType(tuple, getConstIndex(indexExpr).intValue());
            }

            LinkedHashSet<BType> tupleTypes = collectTupleFieldTypes(tuple, new LinkedHashSet<>());
            return tupleTypes.size() == 1 ? tupleTypes.iterator().next() : BUnionType.create(null, tupleTypes);
        }

        switch (tag) {
            case TypeTags.FINITE:
                BFiniteType finiteIndexExpr = (BFiniteType) currentType;
                LinkedHashSet<BType> possibleTypes = new LinkedHashSet<>();
                for (BLangExpression finiteMember : finiteIndexExpr.getValueSpace()) {
                    int indexValue = ((Long) ((BLangLiteral) finiteMember).value).intValue();
                    BType fieldType = checkTupleFieldType(tuple, indexValue);
                    if (fieldType.tag != TypeTags.SEMANTIC_ERROR) {
                        possibleTypes.add(fieldType);
                    }
                }
                if (possibleTypes.size() == 0) {
                    return symTable.semanticError;
                }
                actualType = possibleTypes.size() == 1 ? possibleTypes.iterator().next() :
                        BUnionType.create(null, possibleTypes);
                break;

            case TypeTags.UNION:
                LinkedHashSet<BType> possibleTypesByMember = new LinkedHashSet<>();
                List<BFiniteType> finiteTypes = new ArrayList<>();
                ((BUnionType) currentType).getMemberTypes().forEach(memType -> {
                    memType = Types.getReferredType(memType);
                    if (memType.tag == TypeTags.FINITE) {
                        finiteTypes.add((BFiniteType) memType);
                    } else {
                        BType possibleType = checkTupleIndexBasedAccess(accessExpr, tuple, memType);
                        if (possibleType.tag == TypeTags.UNION) {
                            possibleTypesByMember.addAll(((BUnionType) possibleType).getMemberTypes());
                        } else {
                            possibleTypesByMember.add(possibleType);
                        }
                    }
                });

                BFiniteType finiteType;
                if (finiteTypes.size() == 1) {
                    finiteType = finiteTypes.get(0);
                } else {
                    Set<BLangExpression> valueSpace = new LinkedHashSet<>();
                    finiteTypes.forEach(constituent -> valueSpace.addAll(constituent.getValueSpace()));
                    finiteType = new BFiniteType(null, valueSpace);
                }

                BType possibleType = checkTupleIndexBasedAccess(accessExpr, tuple, finiteType);
                if (possibleType.tag == TypeTags.UNION) {
                    possibleTypesByMember.addAll(((BUnionType) possibleType).getMemberTypes());
                } else {
                    possibleTypesByMember.add(possibleType);
                }

                if (possibleTypesByMember.contains(symTable.semanticError)) {
                    return symTable.semanticError;
                }
                actualType = possibleTypesByMember.size() == 1 ? possibleTypesByMember.iterator().next() :
                        BUnionType.create(null, possibleTypesByMember);
                break;
            case TypeTags.TYPEREFDESC:
                BType refType = Types.getReferredType(currentType);
                BType compatibleType = checkTupleIndexBasedAccess(accessExpr, tuple, refType);
                return compatibleType == refType ? currentType : compatibleType;
        }
        return actualType;
    }

    private LinkedHashSet<BType> collectTupleFieldTypes(BTupleType tupleType, LinkedHashSet<BType> memberTypes) {
        tupleType.getTupleTypes()
                .forEach(memberType -> {
                    if (memberType.tag == TypeTags.UNION) {
                        collectMemberTypes((BUnionType) memberType, memberTypes);
                    } else {
                        memberTypes.add(memberType);
                    }
                });
        BType tupleRestType = tupleType.restType;
        if (tupleRestType != null) {
            memberTypes.add(tupleRestType);
        }
        return memberTypes;
    }

    private BType checkMappingIndexBasedAccess(BLangIndexBasedAccess accessExpr, BType bType, AnalyzerData data) {
        BType type = Types.getReferredType(bType);
        if (type.tag == TypeTags.MAP) {
            BType constraint = Types.getReferredType(((BMapType) type).constraint);
            return accessExpr.isLValue ? constraint : types.addNilForNillableAccessType(constraint);
        }

        if (type.tag == TypeTags.RECORD) {
            return checkRecordIndexBasedAccess(accessExpr, (BRecordType) type, accessExpr.indexExpr.getBType(), data);
        }

        BType fieldType;

        boolean nonMatchedRecordExists = false;

        LinkedHashSet<BType> fieldTypeMembers = new LinkedHashSet<>();

        for (BType memType : ((BUnionType) type).getMemberTypes()) {
            BType individualFieldType = checkMappingIndexBasedAccess(accessExpr, memType, data);

            if (individualFieldType == symTable.semanticError) {
                nonMatchedRecordExists = true;
                continue;
            }

            fieldTypeMembers.add(individualFieldType);
        }

        if (fieldTypeMembers.size() == 0) {
            return symTable.semanticError;
        }

        if (fieldTypeMembers.size() == 1) {
            fieldType = fieldTypeMembers.iterator().next();
        } else {
            fieldType = BUnionType.create(null, fieldTypeMembers);
        }

        return nonMatchedRecordExists ? types.addNilForNillableAccessType(fieldType) : fieldType;
    }

    private BType checkRecordIndexBasedAccess(BLangIndexBasedAccess accessExpr, BRecordType record, BType currentType,
                                              AnalyzerData data) {
        BType actualType = symTable.semanticError;
        BLangExpression indexExpr = accessExpr.indexExpr;
        switch (currentType.tag) {
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
                if (isConstExpr(indexExpr)) {
                    String fieldName = Utils.escapeSpecialCharacters(getConstFieldName(indexExpr));
                    actualType = checkRecordRequiredFieldAccess(accessExpr, names.fromString(fieldName), record, data);
                    if (actualType != symTable.semanticError) {
                        return actualType;
                    }

                    actualType = checkRecordOptionalFieldAccess(accessExpr, names.fromString(fieldName), record, data);
                    if (actualType == symTable.semanticError) {
                        actualType = checkRecordRestFieldAccess(accessExpr, names.fromString(fieldName), record, data);
                        if (actualType == symTable.semanticError) {
                            return actualType;
                        }
                        if (actualType == symTable.neverType) {
                            return actualType;
                        }
                        return types.addNilForNillableAccessType(actualType);
                    }

                    if (accessExpr.isLValue) {
                        return actualType;
                    }
                    return types.addNilForNillableAccessType(actualType);
                }

                LinkedHashSet<BType> fieldTypes = record.fields.values().stream()
                        .map(field -> field.type)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                if (record.restFieldType.tag != TypeTags.NONE) {
                    fieldTypes.add(record.restFieldType);
                }

                if (fieldTypes.stream().noneMatch(BType::isNullable)) {
                    fieldTypes.add(symTable.nilType);
                }

                actualType = BUnionType.create(null, fieldTypes);
                break;
            case TypeTags.FINITE:
                BFiniteType finiteIndexExpr = (BFiniteType) currentType;
                LinkedHashSet<BType> possibleTypes = new LinkedHashSet<>();
                for (BLangExpression finiteMember : finiteIndexExpr.getValueSpace()) {
                    String fieldName = (String) ((BLangLiteral) finiteMember).value;
                    BType fieldType =
                            checkRecordRequiredFieldAccess(accessExpr, names.fromString(fieldName), record, data);
                    if (fieldType == symTable.semanticError) {
                        fieldType =
                                checkRecordOptionalFieldAccess(accessExpr, names.fromString(fieldName), record, data);
                        if (fieldType == symTable.semanticError) {
                            fieldType =
                                    checkRecordRestFieldAccess(accessExpr, names.fromString(fieldName), record, data);
                        }

                        if (fieldType != symTable.semanticError) {
                            fieldType = types.addNilForNillableAccessType(fieldType);
                        }
                    }

                    if (fieldType.tag == TypeTags.SEMANTIC_ERROR) {
                        continue;
                    }
                    possibleTypes.add(fieldType);
                }

                if (possibleTypes.isEmpty()) {
                    return symTable.semanticError;
                }

                if (possibleTypes.stream().noneMatch(BType::isNullable)) {
                    possibleTypes.add(symTable.nilType);
                }

                actualType = possibleTypes.size() == 1 ? possibleTypes.iterator().next() :
                        BUnionType.create(null, possibleTypes);
                break;
            case TypeTags.UNION:
                LinkedHashSet<BType> possibleTypesByMember = new LinkedHashSet<>();
                List<BFiniteType> finiteTypes = new ArrayList<>();
                types.getAllTypes(currentType, true).forEach(memType -> {
                    if (memType.tag == TypeTags.FINITE) {
                        finiteTypes.add((BFiniteType) memType);
                    } else {
                        BType possibleType = checkRecordIndexBasedAccess(accessExpr, record, memType, data);
                        if (possibleType.tag == TypeTags.UNION) {
                            possibleTypesByMember.addAll(((BUnionType) possibleType).getMemberTypes());
                        } else {
                            possibleTypesByMember.add(possibleType);
                        }
                    }
                });

                BFiniteType finiteType;
                if (finiteTypes.size() == 1) {
                    finiteType = finiteTypes.get(0);
                } else {
                    Set<BLangExpression> valueSpace = new LinkedHashSet<>();
                    finiteTypes.forEach(constituent -> valueSpace.addAll(constituent.getValueSpace()));
                    finiteType = new BFiniteType(null, valueSpace);
                }

                BType possibleType = checkRecordIndexBasedAccess(accessExpr, record, finiteType, data);
                if (possibleType.tag == TypeTags.UNION) {
                    possibleTypesByMember.addAll(((BUnionType) possibleType).getMemberTypes());
                } else {
                    possibleTypesByMember.add(possibleType);
                }

                if (possibleTypesByMember.contains(symTable.semanticError)) {
                    return symTable.semanticError;
                }
                actualType = possibleTypesByMember.size() == 1 ? possibleTypesByMember.iterator().next() :
                        BUnionType.create(null, possibleTypesByMember);
                break;
            case TypeTags.TYPEREFDESC:
                return checkRecordIndexBasedAccess(accessExpr, record,
                        Types.getReferredType(currentType), data);
        }
        return actualType;
    }

    private boolean isConstExpr(BLangExpression expression) {
        switch (expression.getKind()) {
            case LITERAL:
            case NUMERIC_LITERAL:
                return true;
            case GROUP_EXPR:
                BLangGroupExpr groupExpr = (BLangGroupExpr) expression;
                return isConstExpr(groupExpr.expression);
            case SIMPLE_VARIABLE_REF:
                return (((BLangSimpleVarRef) expression).symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT;
            case UNARY_EXPR:
                BLangUnaryExpr unaryExpr = (BLangUnaryExpr) expression;
                if (types.isLiteralInUnaryAllowed(unaryExpr)) {
                    return isConstExpr(unaryExpr.expr);
                } else {
                    return false;
                }
            default:
                return false;
        }
    }

    private Name getCurrentCompUnit(BLangNode node) {
        return names.fromString(node.pos.lineRange().filePath());
    }


    private BType getRepresentativeBroadType(List<BType> inferredTypeList) {
        for (int i = 0; i < inferredTypeList.size(); i++) {
            BType type = inferredTypeList.get(i);
            if (type.tag == TypeTags.SEMANTIC_ERROR) {
                return type;
            }

            for (int j = i + 1; j < inferredTypeList.size(); j++) {
                BType otherType = inferredTypeList.get(j);

                if (otherType.tag == TypeTags.SEMANTIC_ERROR) {
                    return otherType;
                }

                if (types.isAssignable(otherType, type)) {
                    inferredTypeList.remove(j);
                    j -= 1;
                    continue;
                }

                if (types.isAssignable(type, otherType)) {
                    inferredTypeList.remove(i);
                    i -= 1;
                    break;
                }
            }
        }

        if (inferredTypeList.size() == 1) {
            return inferredTypeList.get(0);
        }

        return BUnionType.create(null, inferredTypeList.toArray(new BType[0]));
    }

    private BType defineInferredRecordType(BLangRecordLiteral recordLiteral, BType expType, AnalyzerData data) {
        SymbolEnv env = data.env;
        PackageID pkgID = env.enclPkg.symbol.pkgID;
        BRecordTypeSymbol recordSymbol = createRecordTypeSymbol(pkgID, recordLiteral.pos, VIRTUAL, data);

        Map<String, FieldInfo> nonRestFieldTypes = new LinkedHashMap<>();
        List<BType> restFieldTypes = new ArrayList<>();

        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            if (field.isKeyValueField()) {
                BLangRecordKeyValueField keyValue = (BLangRecordKeyValueField) field;
                BLangRecordKey key = keyValue.key;
                BLangExpression expression = keyValue.valueExpr;
                BLangExpression keyExpr = key.expr;
                if (key.computedKey) {
                    checkExpr(keyExpr, symTable.stringType, data);
                    BType exprType = checkExpr(expression, expType, data);
                    if (isUniqueType(restFieldTypes, exprType)) {
                        restFieldTypes.add(exprType);
                    }
                } else {
                    addToNonRestFieldTypes(nonRestFieldTypes, getKeyName(keyExpr),
                                           keyValue.readonly ? checkExpr(expression, symTable.readonlyType, data) :
                                                   checkExpr(expression, expType, data),
                                           true, keyValue.readonly);
                }
            } else if (field.getKind() == NodeKind.RECORD_LITERAL_SPREAD_OP) {
                BType spreadOpType = checkExpr(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr,
                                               expType, data);
                BType type = Types.getReferredType(spreadOpType);

                if (type.tag == TypeTags.MAP) {
                    BType constraintType = ((BMapType) type).constraint;

                    if (isUniqueType(restFieldTypes, constraintType)) {
                        restFieldTypes.add(constraintType);
                    }
                }

                if (type.tag != TypeTags.RECORD) {
                    continue;
                }

                BRecordType recordType = (BRecordType) type;
                for (BField recField : recordType.fields.values()) {
                    addToNonRestFieldTypes(nonRestFieldTypes, recField.name.value, recField.type,
                                           !Symbols.isOptional(recField.symbol), false);
                }

                if (!recordType.sealed) {
                    BType restFieldType = recordType.restFieldType;
                    if (isUniqueType(restFieldTypes, restFieldType)) {
                        restFieldTypes.add(restFieldType);
                    }
                }
            } else {
                BLangRecordVarNameField varNameField = (BLangRecordVarNameField) field;
                addToNonRestFieldTypes(nonRestFieldTypes, getKeyName(varNameField), varNameField.readonly ?
                                       checkExpr(varNameField, symTable.readonlyType, data) :
                                       checkExpr(varNameField, expType, data),
                                       true, varNameField.readonly);
            }
        }

        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
        boolean allReadOnlyNonRestFields = true;

        for (Map.Entry<String, FieldInfo> entry : nonRestFieldTypes.entrySet()) {
            FieldInfo fieldInfo = entry.getValue();
            List<BType> types = fieldInfo.types;

            if (types.contains(symTable.semanticError)) {
                return symTable.semanticError;
            }

            String key = entry.getKey();
            Name fieldName = names.fromString(key);
            BType type = types.size() == 1 ? types.get(0) : BUnionType.create(null, types.toArray(new BType[0]));

            Set<Flag> flags = new HashSet<>();

            if (fieldInfo.required) {
                flags.add(Flag.REQUIRED);
            } else {
                flags.add(Flag.OPTIONAL);
            }

            if (fieldInfo.readonly) {
                flags.add(Flag.READONLY);
            } else if (allReadOnlyNonRestFields) {
                allReadOnlyNonRestFields = false;
            }

            BVarSymbol fieldSymbol = new BVarSymbol(Flags.asMask(flags), fieldName, pkgID, type, recordSymbol,
                                                    symTable.builtinPos, VIRTUAL);
            fields.put(fieldName.value, new BField(fieldName, null, fieldSymbol));
            recordSymbol.scope.define(fieldName, fieldSymbol);
        }

        BRecordType recordType = new BRecordType(recordSymbol);
        recordType.fields = fields;

        if (restFieldTypes.contains(symTable.semanticError)) {
            return symTable.semanticError;
        }

        if (restFieldTypes.isEmpty()) {
            recordType.sealed = true;
            recordType.restFieldType = symTable.noType;
        } else if (restFieldTypes.size() == 1) {
            recordType.restFieldType = restFieldTypes.get(0);
        } else {
            recordType.restFieldType = BUnionType.create(null, restFieldTypes.toArray(new BType[0]));
        }
        recordSymbol.type = recordType;
        recordType.tsymbol = recordSymbol;

        if (expType == symTable.readonlyType || (recordType.sealed && allReadOnlyNonRestFields)) {
            recordType.flags |= Flags.READONLY;
            recordSymbol.flags |= Flags.READONLY;
        }

        BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(recordType, pkgID, symTable,
                                                                                       recordLiteral.pos);
        recordTypeNode.initFunction = TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, env,
                                                                                           names, symTable);
        TypeDefBuilderHelper.createTypeDefinitionForTSymbol(recordType, recordSymbol, recordTypeNode, env);

        return recordType;
    }

    private BRecordTypeSymbol createRecordTypeSymbol(PackageID pkgID, Location location,
                                                     SymbolOrigin origin, AnalyzerData data) {
        SymbolEnv env = data.env;
        BRecordTypeSymbol recordSymbol =
                Symbols.createRecordSymbol(Flags.ANONYMOUS,
                                           names.fromString(anonymousModelHelper.getNextAnonymousTypeKey(pkgID)),
                                           pkgID, null, env.scope.owner, location, origin);

        BInvokableType bInvokableType = new BInvokableType(new ArrayList<>(), symTable.nilType, null);
        BInvokableSymbol initFuncSymbol = Symbols.createFunctionSymbol(
                Flags.PUBLIC, Names.EMPTY, Names.EMPTY, env.enclPkg.symbol.pkgID, bInvokableType, env.scope.owner,
                false, symTable.builtinPos, VIRTUAL);
        initFuncSymbol.retType = symTable.nilType;
        recordSymbol.initializerFunc = new BAttachedFunction(Names.INIT_FUNCTION_SUFFIX, initFuncSymbol,
                                                             bInvokableType, location);

        recordSymbol.scope = new Scope(recordSymbol);
        recordSymbol.scope.define(
                names.fromString(recordSymbol.name.value + "." + recordSymbol.initializerFunc.funcName.value),
                recordSymbol.initializerFunc.symbol);
        return recordSymbol;
    }

    private String getKeyName(BLangExpression key) {
        return key.getKind() == NodeKind.SIMPLE_VARIABLE_REF ?
                ((BLangSimpleVarRef) key).variableName.value : (String) ((BLangLiteral) key).value;
    }

    private void addToNonRestFieldTypes(Map<String, FieldInfo> nonRestFieldTypes, String keyString,
                                        BType exprType, boolean required, boolean readonly) {
        if (!nonRestFieldTypes.containsKey(keyString)) {
            nonRestFieldTypes.put(keyString, new FieldInfo(new ArrayList<BType>() {{ add(exprType); }}, required,
                                                           readonly));
            return;
        }

        FieldInfo fieldInfo = nonRestFieldTypes.get(keyString);
        List<BType> typeList = fieldInfo.types;

        if (isUniqueType(typeList, exprType)) {
            typeList.add(exprType);
        }

        if (required && !fieldInfo.required) {
            fieldInfo.required = true;
        }
    }

    private boolean isUniqueType(Iterable<BType> typeList, BType type) {
        boolean isRecord = type.tag == TypeTags.RECORD;

        for (BType bType : typeList) {

            if (isRecord) {
                if (type == bType) {
                    return false;
                }
            } else if (types.isSameType(type, bType)) {
                return false;
            }
        }
        return true;
    }

    private BType checkXmlSubTypeLiteralCompatibility(Location location, BXMLSubType mutableXmlSubType,
                                                      BType expType, AnalyzerData data) {
        if (expType == symTable.semanticError) {
            return expType;
        }

        boolean unionExpType = expType.tag == TypeTags.UNION;

        if (expType == mutableXmlSubType) {
            return expType;
        }

        if (!unionExpType && types.isAssignable(mutableXmlSubType, expType)) {
            return mutableXmlSubType;
        }

        BXMLSubType immutableXmlSubType = (BXMLSubType)
                ImmutableTypeCloner.getEffectiveImmutableType(location, types, mutableXmlSubType, data.env, symTable,
                                                              anonymousModelHelper, names);

        if (expType == immutableXmlSubType) {
            return expType;
        }

        if (!unionExpType && types.isAssignable(immutableXmlSubType, expType)) {
            return immutableXmlSubType;
        }

        if (!unionExpType) {
            dlog.error(location, DiagnosticErrorCode.INCOMPATIBLE_TYPES, expType, mutableXmlSubType);
            return symTable.semanticError;
        }

        List<BType> compatibleTypes = new ArrayList<>();
        for (BType memberType : ((BUnionType) expType).getMemberTypes()) {
            if (compatibleTypes.contains(memberType)) {
                continue;
            }

            if (memberType == mutableXmlSubType || memberType == immutableXmlSubType) {
                compatibleTypes.add(memberType);
                continue;
            }

            if (types.isAssignable(mutableXmlSubType, memberType) && !compatibleTypes.contains(mutableXmlSubType)) {
                compatibleTypes.add(mutableXmlSubType);
                continue;
            }

            if (types.isAssignable(immutableXmlSubType, memberType) && !compatibleTypes.contains(immutableXmlSubType)) {
                compatibleTypes.add(immutableXmlSubType);
            }
        }

        if (compatibleTypes.isEmpty()) {
            dlog.error(location, DiagnosticErrorCode.INCOMPATIBLE_TYPES, expType, mutableXmlSubType);
            return symTable.semanticError;
        }

        if (compatibleTypes.size() == 1) {
            return compatibleTypes.get(0);
        }

        dlog.error(location, DiagnosticErrorCode.AMBIGUOUS_TYPES, expType);
        return symTable.semanticError;
    }

    private void markChildrenAsImmutable(BLangXMLElementLiteral bLangXMLElementLiteral, AnalyzerData data) {
        for (BLangExpression modifiedChild : bLangXMLElementLiteral.modifiedChildren) {
            BType childType = modifiedChild.getBType();
            if (Symbols.isFlagOn(childType.flags, Flags.READONLY) ||
                    !types.isSelectivelyImmutableType(childType, data.env.enclPkg.packageID)) {
                continue;
            }
            modifiedChild.setBType(ImmutableTypeCloner.getEffectiveImmutableType(modifiedChild.pos, types, childType,
                    data.env, symTable, anonymousModelHelper, names));

            if (modifiedChild.getKind() == NodeKind.XML_ELEMENT_LITERAL) {
                markChildrenAsImmutable((BLangXMLElementLiteral) modifiedChild, data);
            }
        }
    }

    private void logUndefinedSymbolError(Location pos, String name) {
        if (!missingNodesHelper.isMissingNode(name)) {
            dlog.error(pos, DiagnosticErrorCode.UNDEFINED_SYMBOL, name);
        }
    }

    private void markTypeAsIsolated(BType actualType) {
        actualType.flags |= Flags.ISOLATED;
        actualType.tsymbol.flags |= Flags.ISOLATED;
    }

    private void handleObjectConstrExprForReadOnly(
            BLangObjectConstructorExpression objectCtorExpr, BObjectType actualObjectType, SymbolEnv env,
            boolean logErrors, AnalyzerData data) {

        BLangClassDefinition classDefForConstructor = objectCtorExpr.classNode;
        boolean hasNeverReadOnlyField = false;

        for (BField field : actualObjectType.fields.values()) {
            BType fieldType = field.type;
            if (!types.isInherentlyImmutableType(fieldType) &&
                    !types.isSelectivelyImmutableType(fieldType, false, data.env.enclPkg.packageID)) {
                analyzeObjectConstructor(classDefForConstructor, env, data);
                hasNeverReadOnlyField = true;

                if (!logErrors) {
                    return;
                }

                dlog.error(field.pos,
                           DiagnosticErrorCode.INVALID_FIELD_IN_OBJECT_CONSTUCTOR_EXPR_WITH_READONLY_REFERENCE,
                           fieldType);
            }
        }

        if (hasNeverReadOnlyField) {
            return;
        }

        classDefForConstructor.flagSet.add(Flag.READONLY);
        actualObjectType.flags |= Flags.READONLY;
        actualObjectType.tsymbol.flags |= Flags.READONLY;

        ImmutableTypeCloner.markFieldsAsImmutable(classDefForConstructor, env, actualObjectType, types,
                                                  anonymousModelHelper, symTable, names, objectCtorExpr.pos);

        analyzeObjectConstructor(classDefForConstructor, env, data);
    }

    private void markConstructedObjectIsolatedness(BObjectType actualObjectType) {
        if (actualObjectType.markedIsolatedness) {
            return;
        }
        if (Symbols.isFlagOn(actualObjectType.flags, Flags.READONLY)) {
            markTypeAsIsolated(actualObjectType);
            return;
        }

        for (BField field : actualObjectType.fields.values()) {
            if (!Symbols.isFlagOn(field.symbol.flags, Flags.FINAL) ||
                    !types.isSubTypeOfReadOnlyOrIsolatedObjectUnion(field.type)) {
                return;
            }
        }

        markTypeAsIsolated(actualObjectType);
        actualObjectType.markedIsolatedness = true;
    }

    private void markLeafNode(BLangAccessExpression accessExpression) {
        BLangNode parent = accessExpression.parent;
        if (parent == null) {
            accessExpression.leafNode = true;
            return;
        }

        NodeKind kind = parent.getKind();

        while (kind == NodeKind.GROUP_EXPR) {
            parent = parent.parent;

            if (parent == null) {
                accessExpression.leafNode = true;
                break;
            }

            kind = parent.getKind();
        }

        if (kind != NodeKind.FIELD_BASED_ACCESS_EXPR && kind != NodeKind.INDEX_BASED_ACCESS_EXPR) {
            accessExpression.leafNode = true;
        }
    }

    private static class FieldInfo {
        List<BType> types;
        boolean required;
        boolean readonly;

        private FieldInfo(List<BType> types, boolean required, boolean readonly) {
            this.types = types;
            this.required = required;
            this.readonly = readonly;
        }
    }

    private static class TypeSymbolPair {
        private BVarSymbol fieldSymbol;
        private BType determinedType;

        public TypeSymbolPair(BVarSymbol fieldSymbol, BType determinedType) {
            this.fieldSymbol = fieldSymbol;
            this.determinedType = determinedType;
        }
    }

    private static class RecordUnionDiagnostics {
        // Set of record types which doesn't have the field name declared
        Set<BRecordType> undeclaredInRecords = new LinkedHashSet<>();

        // Set of record types which has the field type that includes nil
        Set<BRecordType> nilableInRecords = new LinkedHashSet<>();

        boolean hasUndeclared() {
            return undeclaredInRecords.size() > 0;
        }

        boolean hasNilable() {
            return nilableInRecords.size() > 0;
        }

        boolean hasNilableAndUndeclared() {
            return nilableInRecords.size() > 0 && undeclaredInRecords.size() > 0;
        }

        String recordsToString(Set<BRecordType> recordTypeSet) {
            StringBuilder recordNames = new StringBuilder();
            int recordSetSize = recordTypeSet.size();
            int index = 0;

            for (BRecordType recordType : recordTypeSet) {
                index++;
                recordNames.append(recordType.tsymbol.getName().getValue());

                if (recordSetSize > 1) {

                    if (index == recordSetSize - 1) {
                        recordNames.append("', and '");
                    } else if (index < recordSetSize) {
                        recordNames.append("', '");
                    }
                }
            }

            return recordNames.toString();
        }
    }

    /**
     * @since 2.0.0
     */
    public static class AnalyzerData {
        public SymbolEnv env;
        boolean isTypeChecked;
        Stack<SymbolEnv> prevEnvs;
        Types.CommonAnalyzerData commonAnalyzerData = new Types.CommonAnalyzerData();
        DiagnosticCode diagCode;
        BType expType;
        BType resultType;
        boolean isResourceAccessPathSegments = false;
        QueryTypeChecker.AnalyzerData queryData;
    }
}

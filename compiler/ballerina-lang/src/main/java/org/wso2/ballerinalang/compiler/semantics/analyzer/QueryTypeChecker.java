/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.util.BLangCompilerConstants;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.NodeCloner;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSequenceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BSequenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangCollectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupingKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangInputClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCollectContextInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.INVALID_QUERY_CONSTRUCT_INFERRED_MAP;
import static org.ballerinalang.util.diagnostic.DiagnosticErrorCode.INVALID_QUERY_CONSTRUCT_TYPE;

/**
 * @since 2201.5.0
 */
public class QueryTypeChecker extends TypeChecker {

    private static final CompilerContext.Key<QueryTypeChecker> QUERY_TYPE_CHECKER_KEY = new CompilerContext.Key<>();

    private final Types types;
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final SemanticAnalyzer semanticAnalyzer;
    private final TypeParamAnalyzer typeParamAnalyzer;
    private final TypeNarrower typeNarrower;
    private final BLangAnonymousModelHelper anonymousModelHelper;
    private final Names names;
    private final BLangDiagnosticLog dlog;
    private final NodeCloner nodeCloner;

    public static QueryTypeChecker getInstance(CompilerContext context) {
        QueryTypeChecker queryTypeChecker = context.get(QUERY_TYPE_CHECKER_KEY);
        if (queryTypeChecker == null) {
            queryTypeChecker = new QueryTypeChecker(context);
        }
        return queryTypeChecker;
    }

    public QueryTypeChecker(CompilerContext context) {
        super(context, new CompilerContext.Key<>());
        context.put(QUERY_TYPE_CHECKER_KEY, this);

        this.names = Names.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.typeNarrower = TypeNarrower.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.semanticAnalyzer = SemanticAnalyzer.getInstance(context);
        this.typeParamAnalyzer = TypeParamAnalyzer.getInstance(context);
        this.nodeCloner = NodeCloner.getInstance(context);
    }

    @Override
    public void visit(BLangQueryExpr queryExpr, TypeChecker.AnalyzerData data) {
        checkQueryType(queryExpr, data);
    }

    public void checkQueryType(BLangQueryExpr queryExpr, TypeChecker.AnalyzerData data) {
        AnalyzerData prevData = data.queryData;
        data.queryData = new AnalyzerData();

        Types.CommonAnalyzerData commonAnalyzerData = data.commonAnalyzerData;

        //reset common analyzer data
        HashSet<BType> prevCheckedErrorList = commonAnalyzerData.checkedErrorList;
        commonAnalyzerData.checkedErrorList = new HashSet<>();

        Stack<BLangNode> prevQueryFinalClauses = commonAnalyzerData.queryFinalClauses;
        commonAnalyzerData.queryFinalClauses = new Stack<>();

        int prevLetCount = commonAnalyzerData.letCount;
        commonAnalyzerData.letCount = 0;

        if (commonAnalyzerData.breakToParallelQueryEnv) {
            commonAnalyzerData.queryEnvs.push(data.prevEnvs.peek());
        } else {
            commonAnalyzerData.queryEnvs.push(data.env);
            data.prevEnvs.push(data.env);
        }

        BLangNode finalClause = queryExpr.getFinalClause();
        commonAnalyzerData.queryFinalClauses.push(finalClause);
        data.queryVariables = new HashSet<>();
        List<BLangNode> clauses = queryExpr.getQueryClauses();
        clauses.forEach(clause -> clause.accept(this, data));
        data.queryVariables.clear();

        BType actualType;
        if (finalClause.getKind() == NodeKind.SELECT) {
            actualType = resolveQueryType(commonAnalyzerData.queryEnvs.peek(),
                    ((BLangSelectClause) finalClause).expression, data.expType, queryExpr, clauses, data);
            queryExpr.setDeterminedType(actualType);
            actualType = (actualType == symTable.semanticError) ? actualType : types.checkType(queryExpr.pos,
                    actualType, data.expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES);
        } else {
            if (queryExpr.isTable || queryExpr.isStream || queryExpr.isMap) {
                dlog.error(queryExpr.pos, DiagnosticErrorCode.QUERY_CONSTRUCT_TYPES_CANNOT_BE_USED_WITH_COLLECT);
            }
            BLangExpression finalClauseExpr = ((BLangCollectClause) finalClause).expression;
            BType queryType = checkExpr(finalClauseExpr, commonAnalyzerData.queryEnvs.peek(), data);
            List<BType> collectionTypes = getCollectionTypes(clauses);
            BType completionType = getCompletionType(collectionTypes, Types.QueryConstructType.DEFAULT, data);
            if (completionType != null) {
                queryType = BUnionType.create(null, queryType, completionType);
            }
            queryExpr.setDeterminedType(queryType);
            actualType = types.checkType(finalClauseExpr.pos, queryType, data.expType,
                    DiagnosticErrorCode.INCOMPATIBLE_TYPES);
        }
        commonAnalyzerData.queryFinalClauses.pop();
        commonAnalyzerData.queryEnvs.pop();
        if (!commonAnalyzerData.breakToParallelQueryEnv) {
            data.prevEnvs.pop();
        }

        BType referredActualType = Types.getImpliedType(actualType);
        if (referredActualType.tag == TypeTags.TABLE) {
            BTableType tableType = (BTableType) referredActualType;
            tableType.constraintPos = queryExpr.pos;
            tableType.isTypeInlineDefined = true;
            if (!validateTableType(tableType, data)) {
                data.resultType = symTable.semanticError;
                return;
            }
        }

        //re-assign common analyzer data
        commonAnalyzerData.checkedErrorList = prevCheckedErrorList;
        commonAnalyzerData.queryFinalClauses = prevQueryFinalClauses;
        commonAnalyzerData.letCount = prevLetCount;

        data.resultType = actualType;
        data.queryData = prevData;
    }

    @Override
    public void visit(BLangQueryAction queryAction, TypeChecker.AnalyzerData data) {
        checkQueryAction(queryAction, data);
    }

    public void checkQueryAction(BLangQueryAction queryAction, TypeChecker.AnalyzerData data) {
        AnalyzerData prevData = data.queryData;
        data.queryData = new AnalyzerData();

        Types.CommonAnalyzerData commonAnalyzerData = data.commonAnalyzerData;

        //reset common analyzer data
        Stack<BLangNode> prevQueryFinalClauses = commonAnalyzerData.queryFinalClauses;
        commonAnalyzerData.queryFinalClauses = new Stack<>();

        int prevLetCount = commonAnalyzerData.letCount;
        commonAnalyzerData.letCount = 0;

        if (commonAnalyzerData.breakToParallelQueryEnv) {
            commonAnalyzerData.queryEnvs.push(data.prevEnvs.peek());
        } else {
            commonAnalyzerData.queryEnvs.push(data.env);
            data.prevEnvs.push(data.env);
        }
        BLangDoClause doClause = queryAction.getDoClause();
        commonAnalyzerData.queryFinalClauses.push(doClause);
        data.queryVariables = new HashSet<>();
        List<BLangNode> clauses = queryAction.getQueryClauses();
        clauses.forEach(clause -> clause.accept(this, data));
        data.queryVariables.clear();
        List<BType> collectionTypes = getCollectionTypes(clauses);
        BType completionType = getCompletionType(collectionTypes, Types.QueryConstructType.ACTION, data);
        // Analyze foreach node's statements.
        semanticAnalyzer.analyzeNode(doClause.body, SymbolEnv.createBlockEnv(doClause.body,
                commonAnalyzerData.queryEnvs.peek()), data.prevEnvs, this, commonAnalyzerData);
        BType actualType = completionType == null ? symTable.nilType : completionType;
        data.resultType = types.checkType(doClause.pos, actualType, data.expType,
                        DiagnosticErrorCode.INCOMPATIBLE_TYPES);
        commonAnalyzerData.queryFinalClauses.pop();
        commonAnalyzerData.queryEnvs.pop();
        if (!commonAnalyzerData.breakToParallelQueryEnv) {
            data.prevEnvs.pop();
        }

        //re-assign common analyzer data
        commonAnalyzerData.queryFinalClauses = prevQueryFinalClauses;
        commonAnalyzerData.letCount = prevLetCount;
        data.queryData = prevData;
    }

    public BType resolveQueryType(SymbolEnv env, BLangExpression selectExp, BType targetType,
                                  BLangQueryExpr queryExpr, List<BLangNode> clauses, TypeChecker.AnalyzerData data) {
        List<BType> safeResultTypes = types.getAllTypes(targetType, true).stream()
                .filter(t -> !types.isAssignable(t, symTable.errorType))
                .filter(t -> !types.isAssignable(t, symTable.nilType))
                .collect(Collectors.toList());
        // resultTypes will be empty if the targetType is `error?`
        if (safeResultTypes.isEmpty()) {
            safeResultTypes.add(symTable.noType);
        }
        BType actualType = symTable.semanticError;
        List<BType> selectTypes = new ArrayList<>();
        List<BType> resolvedTypes = new ArrayList<>();
        BType selectType;
        BLangExpression collectionNode = (BLangExpression) ((BLangFromClause) clauses.get(0)).getCollection();
        solveSelectTypeAndResolveType(queryExpr, selectExp, safeResultTypes, collectionNode.getBType(), selectTypes,
                resolvedTypes, env, data, false);

        if (selectTypes.size() == 1) {
            selectType = selectTypes.get(0);
            checkExpr(selectExp, env, selectType, data);
            List<BType> collectionTypes = getCollectionTypes(clauses);
            BType completionType = getCompletionType(collectionTypes, types.getQueryConstructType(queryExpr), data);

            if (queryExpr.isStream) {
                return new BStreamType(TypeTags.STREAM, selectType, completionType, null);
            } else if (queryExpr.isTable) {
                actualType = getQueryTableType(queryExpr, selectType, resolvedTypes.get(0), env);
            } else if (queryExpr.isMap) {
                BType mapConstraintType = getTypeOfTypeParameter(selectType,
                        queryExpr.getSelectClause().expression.pos);
                if (mapConstraintType != symTable.semanticError) {
                    actualType = new BMapType(TypeTags.MAP, mapConstraintType, null);
                    if (Symbols.isFlagOn(resolvedTypes.get(0).flags, Flags.READONLY)) {
                        actualType = ImmutableTypeCloner.getImmutableIntersectionType(null, types, actualType, env,
                                symTable, anonymousModelHelper, names, null);
                    }
                }
            } else {
                actualType = resolvedTypes.get(0);
            }

            if (completionType != null && completionType.tag != TypeTags.NIL) {
                return BUnionType.create(null, actualType, types.getSafeType(completionType, true, false));
            } else {
                return actualType;
            }
        } else if (selectTypes.size() > 1) {
            dlog.error(selectExp.pos, DiagnosticErrorCode.AMBIGUOUS_TYPES, selectTypes);
            return actualType;
        } else {
            return actualType;
        }
    }

    void solveSelectTypeAndResolveType(BLangQueryExpr queryExpr, BLangExpression selectExp, List<BType> expTypes,
                                       BType collectionType, List<BType> selectTypes, List<BType> resolvedTypes,
                                       SymbolEnv env, TypeChecker.AnalyzerData data, boolean isReadonly) {
        LinkedHashSet<BType> possibleSelectTypes = new LinkedHashSet<>();
        List<BType> possibleResolvedTypes = new ArrayList<>();
        LinkedHashSet<BType> errorTypes = new LinkedHashSet<>();
        for (BType expType : expTypes) {
            BType selectType, resolvedType;
            BType type = Types.getReferredType(expType);
            switch (type.tag) {
                case TypeTags.ARRAY:
                    BType elementType = ((BArrayType) type).eType;
                    selectType = checkExprSilent(selectExp, env, elementType, data);
                    if (selectType == symTable.semanticError) {
                        errorTypes.add(elementType);
                        continue;
                    }
                    BType queryResultType = new BArrayType(selectType);
                    resolvedType = getResolvedType(queryResultType, type, isReadonly, env);
                    break;
                case TypeTags.TABLE:
                    BType tableConstraint = types.getSafeType(((BTableType) type).constraint,
                            true, true);
                    selectType = checkExprSilent(selectExp, env, tableConstraint, data);
                    if (selectType == symTable.semanticError) {
                        errorTypes.add(tableConstraint);
                        continue;
                    }
                    resolvedType = getResolvedType(symTable.tableType, type, isReadonly, env);
                    break;
                case TypeTags.STREAM:
                    BType streamConstraint = types.getSafeType(((BStreamType) type).constraint,
                            true, true);
                    selectType = checkExprSilent(selectExp, env, streamConstraint, data);
                    if (selectType == symTable.semanticError) {
                        errorTypes.add(streamConstraint);
                        continue;
                    }
                    resolvedType = symTable.streamType;
                    break;
                case TypeTags.MAP:
                    List<BTupleMember> memberTypeList = new ArrayList<>(2);
                    BVarSymbol stringVarSymbol = new BVarSymbol(0, null, null,
                            symTable.semanticError, null, symTable.builtinPos, SymbolOrigin.VIRTUAL);
                    memberTypeList.add(new BTupleMember(symTable.stringType, stringVarSymbol));
                    BType memberType = ((BMapType) type).getConstraint();
                    BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(memberType);
                    memberTypeList.add(new BTupleMember(memberType, varSymbol));
                    BTupleType newExpType = new BTupleType(null, memberTypeList);
                    selectType = checkExprSilent(selectExp, env, newExpType, data);
                    if (selectType == symTable.semanticError) {
                        errorTypes.add(newExpType);
                        continue;
                    }
                    resolvedType = getResolvedType(selectType, type, isReadonly, env);
                    break;
                case TypeTags.STRING:
                case TypeTags.XML:
                case TypeTags.XML_COMMENT:
                case TypeTags.XML_ELEMENT:
                case TypeTags.XML_PI:
                case TypeTags.XML_TEXT:
                    selectType = checkExprSilent(selectExp, env, type, data);
                    if (selectType == symTable.semanticError) {
                        errorTypes.add(type);
                        continue;
                    }

                    BType refSelectType = Types.getReferredType(selectType);
                    if (TypeTags.isXMLTypeTag(refSelectType.tag) || TypeTags.isStringTypeTag(refSelectType.tag)) {
                        selectType = type;
                    }

                    if (types.isAssignable(selectType, symTable.xmlType)) {
                        resolvedType = getResolvedType(new BXMLType(selectType, null), type, isReadonly, env);
                    } else {
                        resolvedType = selectType;
                    }
                    break;
                case TypeTags.INTERSECTION:
                    type = ((BIntersectionType) type).effectiveType;
                    solveSelectTypeAndResolveType(queryExpr, selectExp, List.of(type), collectionType, selectTypes,
                            resolvedTypes, env, data, Symbols.isFlagOn(type.flags, Flags.READONLY));
                    return;
                case TypeTags.NONE:
                default:
                    // contextually expected type not given (i.e var).
                    BType inferredSelectType = symTable.semanticError;
                    selectType = checkExprSilent(selectExp, env, type, data);
                    if (type != symTable.noType) {
                        inferredSelectType = checkExprSilent(selectExp, env, symTable.noType, data);
                    }

                    if (selectType != symTable.semanticError && inferredSelectType != symTable.semanticError
                            && inferredSelectType != symTable.noType) {
                        selectType = types.getTypeIntersection(
                                Types.IntersectionContext.typeTestIntersectionCalculationContext(),
                                selectType, inferredSelectType, env);
                    } else {
                        BType checkedType = symTable.semanticError;
                        if (selectType == symTable.semanticError) {
                            checkedType = checkExpr(selectExp, env, data);
                        }

                        if (checkedType != symTable.semanticError && expTypes.size() == 1) {
                            selectType = checkedType;
                        }
                    }

                    if (queryExpr.isMap) { // A query-expr that constructs a mapping must start with the map keyword.
                        resolvedType = symTable.mapType;
                    } else if (queryExpr.isStream) {
                        resolvedType = symTable.streamType;
                    } else {
                        resolvedType = getNonContextualQueryType(selectType, collectionType, selectExp.pos);
                    }
                    break;
            }
            if (selectType != symTable.semanticError) {

                if (resolvedType.tag == TypeTags.STREAM) {
                    queryExpr.isStream = true;
                }
                if (resolvedType.tag == TypeTags.TABLE) {
                    queryExpr.isTable = true;
                }
                possibleSelectTypes.add(selectType);
                possibleResolvedTypes.add(resolvedType);
            }
        }
        if (!possibleSelectTypes.isEmpty() && !possibleResolvedTypes.isEmpty()) {
            selectTypes.addAll(possibleSelectTypes);
            resolvedTypes.addAll(possibleResolvedTypes);
            return;
        }
        if (!errorTypes.isEmpty()) {
            if (errorTypes.size() > 1) {
                BType actualQueryType = silentTypeCheckExpr(queryExpr, symTable.noType, data);
                if (actualQueryType != symTable.semanticError) {
                    types.checkType(queryExpr, actualQueryType,
                            BUnionType.create(null, new LinkedHashSet<>(expTypes)));
                    errorTypes.forEach(expType -> {
                        if (expType.tag == TypeTags.UNION) {
                            checkExpr(nodeCloner.cloneNode(selectExp), env, expType, data);
                        }
                    });
                    checkExpr(selectExp, env, data);
                    return;
                }
            }
            errorTypes.forEach(expType -> {
                selectExp.typeChecked = false;
                checkExpr(selectExp, env, expType, data);
            });
            selectExp.typeChecked = true;
        }
    }

    private BType getQueryTableType(BLangQueryExpr queryExpr, BType constraintType, BType resolvedType, SymbolEnv env) {
        final BTableType tableType = new BTableType(TypeTags.TABLE, constraintType, null);
        if (!queryExpr.fieldNameIdentifierList.isEmpty()) {
            validateKeySpecifier(queryExpr.fieldNameIdentifierList, constraintType);
            markReadOnlyForConstraintType(constraintType);
            tableType.fieldNameList = queryExpr.fieldNameIdentifierList.stream()
                    .map(identifier -> ((BLangIdentifier) identifier).value).collect(Collectors.toList());
        }
        if (Symbols.isFlagOn(resolvedType.flags, Flags.READONLY)) {
            return ImmutableTypeCloner.getImmutableIntersectionType(null, types,
                    tableType, env, symTable, anonymousModelHelper, names, null);
        }
        return tableType;
    }

    private void validateKeySpecifier(List<IdentifierNode> fieldList, BType constraintType) {
        for (IdentifierNode identifier : fieldList) {
            BField field = types.getTableConstraintField(constraintType, identifier.getValue());
            if (field == null) {
                dlog.error(identifier.getPosition(), DiagnosticErrorCode.INVALID_FIELD_NAMES_IN_KEY_SPECIFIER,
                        identifier.getValue(), constraintType);
            } else if (!Symbols.isFlagOn(field.symbol.flags, Flags.READONLY)) {
                field.symbol.flags |= Flags.READONLY;
            }
        }
    }

    private void markReadOnlyForConstraintType(BType constraintType) {
        if (constraintType.tag != TypeTags.RECORD) {
            return;
        }
        BRecordType recordType = (BRecordType) constraintType;
        for (BField field : recordType.fields.values()) {
            if (!Symbols.isFlagOn(field.symbol.flags, Flags.READONLY)) {
                return;
            }
        }
        if (recordType.sealed) {
            recordType.flags |= Flags.READONLY;
            recordType.tsymbol.flags |= Flags.READONLY;
        }
    }

    private BType getTypeOfTypeParameter(BType selectType, Location pos) {
        BType referredType = Types.getImpliedType(selectType);
        if (referredType.tag == TypeTags.INTERSECTION) {
            referredType = ((BIntersectionType) referredType).effectiveType;
        }

        if (referredType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) referredType;
            LinkedHashSet<BType> memberTypes = new LinkedHashSet<>(unionType.getMemberTypes().size());
            for (BType type : unionType.getMemberTypes()) {
                BType mapType = getTypeOfTypeParameter(type, pos);
                if (mapType == symTable.semanticError) {
                    return symTable.semanticError;
                }
                memberTypes.add(mapType);
            }
            return new BUnionType(null, memberTypes, false, false);
        } else {
            return getQueryMapConstraintType(referredType, pos);
        }
    }

    private BType getQueryMapConstraintType(BType type, Location pos) {
        if (type.tag == TypeTags.ARRAY) {
            BArrayType arrayType = (BArrayType) type;
            if (arrayType.state != BArrayState.OPEN && arrayType.size == 2 &&
                    types.isAssignable(arrayType.eType, symTable.stringType)) {
                return arrayType.eType;
            }
        } else if (type.tag == TypeTags.TUPLE) {
            List<BType> tupleTypeList = ((BTupleType) type).getTupleTypes();
            if (tupleTypeList.size() == 2 && types.isAssignable(tupleTypeList.get(0), symTable.stringType)) {
                return tupleTypeList.get(1);
            }
        }
        dlog.error(pos, DiagnosticErrorCode.INCOMPATIBLE_TYPE_IN_SELECT_CLAUSE, type);
        return symTable.semanticError;
    }

    private BType getCompletionType(List<BType> collectionTypes, Types.QueryConstructType queryConstructType,
                                    TypeChecker.AnalyzerData data) {
        Set<BType> completionTypes = new LinkedHashSet<>();
        BType returnType, completionType = null;
        for (BType collectionType : collectionTypes) {
            if (collectionType.tag == TypeTags.SEMANTIC_ERROR) {
                return null;
            }
            collectionType = Types.getImpliedType(collectionType);
            switch (collectionType.tag) {
                case TypeTags.STREAM -> {
                    completionType = ((BStreamType) collectionType).completionType;
                    returnType = completionType;
                }
                case TypeTags.OBJECT -> returnType = types.getVarTypeFromIterableObject((BObjectType) collectionType);
                default -> {
                    BSymbol itrSymbol = symResolver.lookupLangLibMethod(collectionType,
                            Names.fromString(BLangCompilerConstants.ITERABLE_COLLECTION_ITERATOR_FUNC), data.env);
                    if (itrSymbol == this.symTable.notFoundSymbol) {
                        return null;
                    }
                    BInvokableSymbol invokableSymbol = (BInvokableSymbol) itrSymbol;
                    returnType = types.getResultTypeOfNextInvocation(
                            (BObjectType) Types.getImpliedType(invokableSymbol.retType));
                }
            }
            if (returnType != null) {
                if (queryConstructType == Types.QueryConstructType.STREAM ||
                        queryConstructType == Types.QueryConstructType.ACTION) {
                    types.getAllTypes(returnType, true).stream()
                            .filter(t -> (types.isAssignable(t, symTable.errorType)
                                    || types.isAssignable(t, symTable.nilType)))
                            .forEach(completionTypes::add);
                } else {
                    types.getAllTypes(returnType, true).stream()
                            .filter(t -> types.isAssignable(t, symTable.errorType))
                            .forEach(completionTypes::add);
                }
            }
        }

        Types.CommonAnalyzerData commonAnalyzerData = data.commonAnalyzerData;
        if (data.queryData.queryCompletesEarly) {
            if (queryConstructType == Types.QueryConstructType.TABLE ||
                    queryConstructType == Types.QueryConstructType.MAP) {
                completionTypes.addAll(data.queryData.completeEarlyErrorList);
            }
        } else if (queryConstructType == Types.QueryConstructType.STREAM) {
            completionTypes.addAll(commonAnalyzerData.checkedErrorList);
            if (completionTypes.isEmpty()) {
                // if there's no completion type at this point,
                // then () gets added as a valid completion type for streams.
                completionTypes.add(symTable.nilType);
            }
        }

        if (!completionTypes.isEmpty()) {
            if (completionTypes.size() == 1) {
                completionType = completionTypes.iterator().next();
            } else {
                completionType = BUnionType.create(null, completionTypes.toArray(new BType[0]));
            }
        }
        return completionType;
    }

    private List<BType> getCollectionTypes(List<BLangNode> clauses) {
        return clauses.stream()
                .filter(clause -> (clause.getKind() == NodeKind.FROM || clause.getKind() == NodeKind.JOIN))
                .map(clause -> ((BLangInputClause) clause).collection.getBType())
                .collect(Collectors.toList());
    }

    private BType getResolvedType(BType initType, BType expType, boolean isReadonly, SymbolEnv env) {
        if (initType.tag != TypeTags.SEMANTIC_ERROR && (isReadonly ||
                Symbols.isFlagOn(expType.flags, Flags.READONLY))) {
            return ImmutableTypeCloner.getImmutableIntersectionType(null, types, initType, env,
                    symTable, anonymousModelHelper, names, null);
        }
        return initType;
    }

    private BType getNonContextualQueryType(BType constraintType, BType basicType, Location pos) {
        switch (Types.getImpliedType(basicType).tag) {
            case TypeTags.TABLE:
                if (types.isAssignable(constraintType, symTable.mapAllType)) {
                    return symTable.tableType;
                }
                break;
            case TypeTags.STREAM:
// todo: Depends on https://github.com/ballerina-platform/ballerina-spec/issues/1252 decision
//                dlog.error(pos, INVALID_QUERY_CONSTRUCT_INFERRED_STREAM);
//                return symTable.semanticError;
                return symTable.streamType;
            case TypeTags.MAP:
                dlog.error(pos, INVALID_QUERY_CONSTRUCT_INFERRED_MAP);
                return symTable.semanticError;
            case TypeTags.XML:
                if (types.isSubTypeOfBaseType(constraintType, symTable.xmlType.tag)) {
                    return new BXMLType(constraintType, null);
                }
                break;
            case TypeTags.STRING:
                if (types.isSubTypeOfBaseType(constraintType, TypeTags.STRING)) {
                    return symTable.stringType;
                }
                break;
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
            case TypeTags.OBJECT:
                return new BArrayType(constraintType);
            default:
                return symTable.semanticError;
        }
        dlog.error(pos, INVALID_QUERY_CONSTRUCT_TYPE, basicType, constraintType);
        return symTable.semanticError;
    }

    private boolean validateTableType(BTableType tableType, TypeChecker.AnalyzerData data) {
        BType constraint = Types.getImpliedType(tableType.constraint);
        if (tableType.isTypeInlineDefined && !types.isAssignable(constraint, symTable.mapAllType)) {
            dlog.error(tableType.constraintPos, DiagnosticErrorCode.TABLE_CONSTRAINT_INVALID_SUBTYPE, constraint);
            data.resultType = symTable.semanticError;
            return false;
        }
        return true;
    }

    private BType checkInvocation(BLangExpression expr, TypeChecker.AnalyzerData data) {
        return checkExpr(expr, data.env, symTable.noType, data);
    }

    private BType checkExpr(BLangExpression expr, SymbolEnv env, TypeChecker.AnalyzerData data) {
        return checkExpr(expr, env, symTable.noType, data);
    }

    private BType checkExpr(BLangExpression expr, SymbolEnv env, BType expType, TypeChecker.AnalyzerData data) {
        return checkExpr(expr, env, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data);
    }

    @Override
    public void visit(BLangFromClause fromClause, TypeChecker.AnalyzerData data) {
        Types.CommonAnalyzerData commonAnalyzerData = data.commonAnalyzerData;
        boolean prevBreakToParallelEnv = commonAnalyzerData.breakToParallelQueryEnv;
        BLangExpression collection = fromClause.collection;
        if (collection.getKind() == NodeKind.QUERY_EXPR ||
                (collection.getKind() == NodeKind.GROUP_EXPR
                        && ((BLangGroupExpr) collection).expression.getKind() == NodeKind.QUERY_EXPR)) {
            commonAnalyzerData.breakToParallelQueryEnv = true;
        }
        SymbolEnv fromEnv = SymbolEnv.createTypeNarrowedEnv(fromClause, commonAnalyzerData.queryEnvs.pop());
        fromClause.env = fromEnv;
        commonAnalyzerData.queryEnvs.push(fromEnv);
        checkExpr(fromClause.collection, fromEnv, data);
        // Set the type of the foreach node's type node.
        types.setInputClauseTypedBindingPatternType(fromClause);
        handleInputClauseVariables(fromClause, fromEnv);
        commonAnalyzerData.breakToParallelQueryEnv = prevBreakToParallelEnv;
        for (Name variable : fromEnv.scope.entries.keySet()) {
            data.queryVariables.add(variable.value);
        }
    }

    private void handleInputClauseVariables(BLangInputClause bLangInputClause, SymbolEnv blockEnv) {
        if (bLangInputClause.variableDefinitionNode == null) {
            //not-possible
            return;
        }

        BLangVariable variableNode = (BLangVariable) bLangInputClause.variableDefinitionNode.getVariable();
        // Check whether the foreach node's variables are declared with var.
        if (bLangInputClause.isDeclaredWithVar) {
            // If the foreach node's variables are declared with var, type is `varType`.
            semanticAnalyzer.handleDeclaredVarInForeach(variableNode, bLangInputClause.varType, blockEnv);
            return;
        }
        // If the type node is available, we get the type from it.
        BType typeNodeType = symResolver.resolveTypeNode(variableNode.typeNode, blockEnv);
        // Then we need to check whether the RHS type is assignable to LHS type.
        if (types.isAssignable(bLangInputClause.varType, typeNodeType)) {
            // If assignable, we set types to the variables.
            semanticAnalyzer.handleDeclaredVarInForeach(variableNode, bLangInputClause.varType, blockEnv);
            return;
        }
        // Log an error and define a symbol with the node's type to avoid undeclared symbol errors.
        if (typeNodeType != symTable.semanticError) {
            dlog.error(variableNode.typeNode.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                    bLangInputClause.varType, typeNodeType);
        }
        semanticAnalyzer.handleDeclaredVarInForeach(variableNode, typeNodeType, blockEnv);
    }

    @Override
    public void visit(BLangJoinClause joinClause, TypeChecker.AnalyzerData data) {
        Types.CommonAnalyzerData commonAnalyzerData = data.commonAnalyzerData;
        boolean prevBreakEnv = commonAnalyzerData.breakToParallelQueryEnv;
        BLangExpression collection = joinClause.collection;
        if (collection.getKind() == NodeKind.QUERY_EXPR ||
                (collection.getKind() == NodeKind.GROUP_EXPR
                        && ((BLangGroupExpr) collection).expression.getKind() == NodeKind.QUERY_EXPR)) {
            commonAnalyzerData.breakToParallelQueryEnv = true;
        }
        SymbolEnv joinEnv = SymbolEnv.createTypeNarrowedEnv(joinClause, commonAnalyzerData.queryEnvs.pop());
        joinClause.env = joinEnv;
        commonAnalyzerData.queryEnvs.push(joinEnv);
        checkExpr(joinClause.collection, joinEnv, data);
        // Set the type of the foreach node's type node.
        types.setInputClauseTypedBindingPatternType(joinClause);
        if (joinClause.isOuterJoin) {
            if (!joinClause.isDeclaredWithVar) {
                this.dlog.error(joinClause.variableDefinitionNode.getPosition(),
                        DiagnosticErrorCode.OUTER_JOIN_MUST_BE_DECLARED_WITH_VAR);
                return;
            }
            joinClause.varType = types.addNilForNillableAccessType(joinClause.varType);
        }
        handleInputClauseVariables(joinClause, joinEnv);
        if (joinClause.onClause != null) {
            joinClause.onClause.accept(this, data);
        }
        for (Name variable : joinEnv.scope.entries.keySet()) {
            data.queryVariables.add(variable.value);
        }
        commonAnalyzerData.breakToParallelQueryEnv = prevBreakEnv;
    }

    @Override
    public void visit(BLangOnClause onClause, TypeChecker.AnalyzerData data) {
        Types.CommonAnalyzerData commonAnalyzerData = data.commonAnalyzerData;
        BType lhsType, rhsType;
        BLangNode joinNode = getLastInputNodeFromEnv(commonAnalyzerData.queryEnvs.peek());
        // lhsExprEnv should only contain scope entries before join condition.
        onClause.lhsEnv = getEnvBeforeInputNode(commonAnalyzerData.queryEnvs.peek(), joinNode);
        lhsType = checkExpr(onClause.lhsExpr, onClause.lhsEnv, data);
        // rhsExprEnv should only contain scope entries after join condition.
        onClause.rhsEnv = getEnvAfterJoinNode(commonAnalyzerData.queryEnvs.peek(), joinNode);
        rhsType = checkExpr(onClause.rhsExpr, onClause.rhsEnv, data);
        if (!types.isAssignable(lhsType, rhsType)) {
            dlog.error(onClause.rhsExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, lhsType, rhsType);
        }
    }

    @Override
    public void visit(BLangLetClause letClause, TypeChecker.AnalyzerData data) {
        Types.CommonAnalyzerData commonAnalyzerData = data.commonAnalyzerData;
        SymbolEnv letEnv = SymbolEnv.createTypeNarrowedEnv(letClause, commonAnalyzerData.queryEnvs.pop());
        letClause.env = letEnv;
        commonAnalyzerData.queryEnvs.push(letEnv);
        for (BLangLetVariable letVariable : letClause.letVarDeclarations) {
            semanticAnalyzer.analyzeNode((BLangNode) letVariable.definitionNode, letEnv, this, commonAnalyzerData);
        }
        for (Name variable : letEnv.scope.entries.keySet()) {
            data.queryVariables.add(variable.value);
        }
    }

    @Override
    public void visit(BLangWhereClause whereClause, TypeChecker.AnalyzerData data) {
        whereClause.env = handleFilterClauses(whereClause.expression, data);
    }

    private SymbolEnv handleFilterClauses (BLangExpression filterExpression, TypeChecker.AnalyzerData data) {
        checkExpr(filterExpression, data.commonAnalyzerData.queryEnvs.peek(), symTable.booleanType, data);
        BType actualType = filterExpression.getBType();
        if (TypeTags.TUPLE == actualType.tag) {
            dlog.error(filterExpression.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                    symTable.booleanType, actualType);
        }
        SymbolEnv filterEnv = typeNarrower.evaluateTruth(filterExpression,
                data.commonAnalyzerData.queryFinalClauses.peek(),
                data.commonAnalyzerData.queryEnvs.pop());
        data.commonAnalyzerData.queryEnvs.push(filterEnv);
        return filterEnv;
    }

    @Override
    public void visit(BLangSelectClause selectClause, TypeChecker.AnalyzerData data) {
        Types.CommonAnalyzerData commonAnalyzerData = data.commonAnalyzerData;
        SymbolEnv selectEnv = SymbolEnv.createTypeNarrowedEnv(selectClause, commonAnalyzerData.queryEnvs.pop());
        selectClause.env = selectEnv;
        commonAnalyzerData.queryEnvs.push(selectEnv);
    }

    @Override
    public void visit(BLangCollectClause collectClause, TypeChecker.AnalyzerData data) {
        Types.CommonAnalyzerData commonAnalyzerData = data.commonAnalyzerData;
        SymbolEnv collectEnv = SymbolEnv.createTypeNarrowedEnv(collectClause, commonAnalyzerData.queryEnvs.pop());
        collectClause.env = collectEnv;
        commonAnalyzerData.queryEnvs.push(collectEnv);

        collectClause.nonGroupingKeys = new HashSet<>(data.queryVariables);
        for (String var : collectClause.nonGroupingKeys) {
            Name name = new Name(var);
            BSymbol originalSymbol = symResolver.lookupSymbolInMainSpace(collectEnv, name);
            BSequenceSymbol sequenceSymbol = new BSequenceSymbol(originalSymbol.flags, name, originalSymbol.pkgID,
                    new BSequenceType(originalSymbol.getType()), originalSymbol.owner, originalSymbol.pos);
            collectEnv.scope.define(name, sequenceSymbol);
        }
    }

    @Override
    public void visit(BLangDoClause doClause, TypeChecker.AnalyzerData data) {
        Types.CommonAnalyzerData commonAnalyzerData = data.commonAnalyzerData;
        SymbolEnv letEnv = SymbolEnv.createTypeNarrowedEnv(doClause, commonAnalyzerData.queryEnvs.pop());
        doClause.env = letEnv;
        commonAnalyzerData.queryEnvs.push(letEnv);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause, TypeChecker.AnalyzerData data) {
        Types.CommonAnalyzerData commonAnalyzerData = data.commonAnalyzerData;
        BType type = checkExpr(onConflictClause.expression, commonAnalyzerData.queryEnvs.peek(),
                symTable.errorOrNilType, data);
        if (types.containsErrorType(type)) {
            data.queryData.queryCompletesEarly = true;
            if (data.queryData.completeEarlyErrorList != null) {
                BType possibleErrorType = type.tag == TypeTags.UNION ?
                        types.getErrorType((BUnionType) type) :
                        types.getErrorType(BUnionType.create(null, type));
                data.queryData.completeEarlyErrorList.add(possibleErrorType);
            }
        }
    }

    @Override
    public void visit(BLangLimitClause limitClause, TypeChecker.AnalyzerData data) {
        BType exprType = checkExpr(limitClause.expression, data.commonAnalyzerData.queryEnvs.peek(), data);
        if (!types.isAssignable(exprType, symTable.intType)) {
            dlog.error(limitClause.expression.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES,
                    symTable.intType, exprType);
        }
    }

    @Override
    public void visit(BLangOrderByClause orderByClause, TypeChecker.AnalyzerData data) {
        orderByClause.env = data.commonAnalyzerData.queryEnvs.peek();
        for (OrderKeyNode orderKeyNode : orderByClause.getOrderKeyList()) {
            BType exprType = checkExpr((BLangExpression) orderKeyNode.getOrderKey(), orderByClause.env, data);
            if (!types.isOrderedType(exprType, false)) {
                dlog.error(((BLangOrderKey) orderKeyNode).expression.pos, DiagnosticErrorCode.ORDER_BY_NOT_SUPPORTED);
            }
        }
    }

    @Override
    public void visit(BLangGroupByClause groupByClause, TypeChecker.AnalyzerData data) {
        SymbolEnv groupByEnv = SymbolEnv.createTypeNarrowedEnv(groupByClause, data.commonAnalyzerData.queryEnvs.pop());
        groupByClause.env = groupByEnv;
        data.commonAnalyzerData.queryEnvs.push(groupByEnv);
        groupByClause.nonGroupingKeys = new HashSet<>(data.queryVariables);
        for (BLangGroupingKey groupingKey : groupByClause.groupingKeyList) {
            String variable;
            BType keyType;
            if (groupingKey.variableRef != null) {
                checkExpr(groupingKey.variableRef, groupByClause.env, data);
                variable = groupingKey.variableRef.variableName.value;
                keyType = groupingKey.variableRef.getBType();
            } else {
                semanticAnalyzer.analyzeNode(groupingKey.variableDef, groupByClause.env, this,
                        data.commonAnalyzerData);
                variable = groupingKey.variableDef.var.name.value;
                data.queryVariables.add(variable);
                keyType = groupingKey.variableDef.var.getBType();
            }
            if (!types.isAssignable(keyType, symTable.anydataType)) {
                dlog.error(groupingKey.pos, DiagnosticErrorCode.INVALID_GROUPING_KEY_TYPE, keyType);
            }
            groupByClause.nonGroupingKeys.remove(variable);
        }
        for (String var : groupByClause.nonGroupingKeys) {
            Name name = new Name(var);
            BSymbol originalSymbol = symResolver.lookupSymbolInMainSpace(groupByEnv, name);
            BSequenceSymbol sequenceSymbol = new BSequenceSymbol(originalSymbol.flags, name, originalSymbol.pkgID,
                    new BSequenceType(originalSymbol.getType()), originalSymbol.owner, originalSymbol.pos);
            groupByEnv.scope.define(name, sequenceSymbol);
        }
    }

    @Override
    public void visit(BLangGroupingKey node, TypeChecker.AnalyzerData data) {

    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr, TypeChecker.AnalyzerData data) {
        visitCheckAndCheckPanicExpr(checkedExpr, data);
        if (checkedExpr.equivalentErrorTypeList != null) {
            data.commonAnalyzerData.checkedErrorList.addAll(checkedExpr.equivalentErrorTypeList);
        }
    }

    public void visit(BLangInvocation iExpr, TypeChecker.AnalyzerData data) {
        if (!hasSequenceArgs(iExpr, data)) {
            super.visit(iExpr, data);
            return;
        }
        Name pkgAlias = names.fromIdNode(iExpr.pkgAlias);
        BSymbol pkgSymbol = symResolver.resolvePrefixSymbol(data.env, pkgAlias, getCurrentCompUnit(iExpr));
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(iExpr.pos, DiagnosticErrorCode.UNDEFINED_MODULE, pkgAlias);
        }
        BType invocationType;
        if (iExpr.expr != null) {
            invocationType = checkInvocation(iExpr.expr, data);
            iExpr.argExprs.add(0, iExpr.expr);
        } else {
            BType firstArgType = silentTypeCheckExpr(iExpr.argExprs.get(0), symTable.noType, data);
            invocationType =
                    firstArgType.tag == TypeTags.SEQUENCE ? ((BSequenceType) firstArgType).elementType : firstArgType;
        }
        Name funcName = names.fromIdNode(iExpr.name);
        BSymbol symbol = symResolver.lookupLangLibMethod(invocationType, funcName, data.env);
        if (symbol == symTable.notFoundSymbol) {
            symbol = symResolver.lookupMainSpaceSymbolInPackage(iExpr.pos, data.env, pkgAlias, funcName);
            if (symbol == symTable.notFoundSymbol) {
                dlog.error(iExpr.pos, DiagnosticErrorCode.UNDEFINED_FUNCTION, funcName);
                return;
            }
            if (!Symbols.isFlagOn(symbol.flags, Flags.LANG_LIB)) {
                dlog.error(iExpr.pos,
                        DiagnosticErrorCode.USER_DEFINED_FUNCTIONS_ARE_DISALLOWED_WITH_AGGREGATED_VARIABLES);
                return;
            }
        }
        boolean langLibPackageID = PackageID.isLangLibPackageID(pkgSymbol.pkgID);
        if (langLibPackageID) {
            data.env = SymbolEnv.createInvocationEnv(iExpr, data.env);
        }
        BInvokableSymbol functionSymbol = (BInvokableSymbol) symbol;
        iExpr.symbol = functionSymbol;
        // match between params and args
        // make sure all params are divided between required and rest
        int argCount = 0;
        List<BVarSymbol> params = functionSymbol.params;
        for (int i = 0; i < params.size(); i++) {
            BLangExpression arg = iExpr.argExprs.get(i);
            BType argType = silentTypeCheckExpr(arg, symTable.noType, data);
            if (argType.tag == TypeTags.SEQUENCE) {
                types.checkType(arg.pos, ((BSequenceType) argType).elementType, params.get(i).type,
                        DiagnosticErrorCode.INCOMPATIBLE_TYPES);
            } else {
                checkArg(arg, params.get(i).type, data);
                iExpr.requiredArgs.add(arg);
                argCount++;
            }
        }
        boolean foundSeqRestArg = false;
        for (int i = argCount; i < iExpr.argExprs.size(); i++) {
            BLangExpression argExpr = iExpr.argExprs.get(i);
            if (functionSymbol.restParam != null) {
                if (foundSeqRestArg) {
                    dlog.error(argExpr.pos, DiagnosticErrorCode.SEQ_ARG_FOLLOWED_BY_ANOTHER_SEQ_ARG);
                } else {
                    BType restParamType = functionSymbol.restParam.type;
                    NodeKind argExprKind = argExpr.getKind();
                    iExpr.restArgs.add(argExpr);
                    if (argExprKind == NodeKind.SIMPLE_VARIABLE_REF) {
                        if (silentTypeCheckExpr(argExpr, symTable.noType, data).tag == TypeTags.SEQUENCE) {
                            foundSeqRestArg = true;
                            checkArg(argExpr, restParamType, data);
                            continue;
                        }
                    }
                    if (restParamType.tag == TypeTags.ARRAY) {
                        checkArg(argExpr, ((BArrayType) restParamType).eType, data);
                    } else {
                        checkArg(argExpr, restParamType, data);
                    }
                }
            }
        }
        BInvokableType bInvokableType = (BInvokableType) Types.getImpliedType(functionSymbol.type);
        BType retType = typeParamAnalyzer.getReturnTypeParams(data.env, bInvokableType.getReturnType());
        data.resultType = types.checkType(iExpr, retType, data.expType);
    }

    // In the collect clause if there are invocations, those invocations should return `()` if the argument is empty.
    private boolean isNilReturnInvocationInCollectClause(BLangInvocation invocation, TypeChecker.AnalyzerData data) {
        BInvokableSymbol symbol = (BInvokableSymbol) invocation.symbol;
        return symbol != null && symbol.restParam != null
                && symbol.params.size() > 0 && invocation.argExprs.size() == 1 && invocation.restArgs.size() == 1;
    }

    // Check the argument within sequence context.
    private boolean hasSequenceArgs(BLangInvocation invocation, TypeChecker.AnalyzerData data) {
        for (BLangExpression arg : invocation.argExprs) {
            if (arg.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BType argType = silentTypeCheckExpr(arg, symTable.noType, data);
                if (argType.tag == TypeTags.SEQUENCE) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkArg(BLangExpression arg, BType expectedType, TypeChecker.AnalyzerData data) {
        data.queryData.withinSequenceContext = arg.getKind() == NodeKind.SIMPLE_VARIABLE_REF;
        checkTypeParamExpr(arg, expectedType, data);
        data.queryData.withinSequenceContext = false;
    }

    public void visit(BLangCollectContextInvocation collectContextInvocation, TypeChecker.AnalyzerData data) {
        BLangInvocation invocation = collectContextInvocation.invocation;
        data.resultType = checkExpr(invocation, data.env, data);
        if (isNilReturnInvocationInCollectClause(invocation, data)) {
            data.resultType = BUnionType.create(null, data.resultType, symTable.nilType);
        }
        collectContextInvocation.setBType(data.resultType);
    }

    public void visit(BLangSimpleVarRef varRefExpr, TypeChecker.AnalyzerData data) {
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
                Name objFuncName = Names.fromString(Symbols
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
            } else if ((symbol.tag & SymTag.SEQUENCE) == SymTag.SEQUENCE) {
                varRefExpr.symbol = symbol;
                actualType = symbol.type;
                if (!data.queryData.withinSequenceContext) {
                    dlog.error(varRefExpr.pos,
                            DiagnosticErrorCode.
                                    SEQUENCE_VARIABLE_CAN_BE_USED_IN_SINGLE_ELEMENT_LIST_CTR_OR_FUNC_INVOCATION);
                }
                if (actualType.tag == TypeTags.SEQUENCE
                        && ((BSequenceType) actualType).elementType.tag == TypeTags.SEQUENCE) {
                    dlog.error(varRefExpr.pos, DiagnosticErrorCode.VARIABLE_IS_SEQUENCED_MORE_THAN_ONCE, varName);
                }
            } else if ((symbol.tag & SymTag.TYPE_DEF) == SymTag.TYPE_DEF) {
                actualType = symbol.type.tag == TypeTags.TYPEDESC ? symbol.type : new BTypedescType(symbol.type, null);
                varRefExpr.symbol = symbol;
            } else if ((symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT) {
                BConstantSymbol constSymbol = (BConstantSymbol) symbol;
                varRefExpr.symbol = constSymbol;
                BType symbolType = symbol.type;
                BType expectedType = Types.getImpliedType(data.expType);
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
    public void visit(BLangListConstructorExpr listConstructor, TypeChecker.AnalyzerData data) {
        BType expType = data.expType;
        if (listConstructor.exprs.size() == 1) {
            BLangExpression expr = listConstructor.exprs.get(0);
            if (expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BType type = silentTypeCheckExpr(expr, symTable.noType, data);
                if (type.tag == TypeTags.SEQUENCE) {
                    data.queryData.withinSequenceContext = true;
                    checkExpr(expr, data.env, symTable.noType, data);
                    data.queryData.withinSequenceContext = false;
                    data.resultType = types.checkType(listConstructor.pos,
                            new BTupleType(null, new ArrayList<>(0), ((BSequenceType) type).elementType, 0),
                            expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES);
                    listConstructor.setBType(data.resultType);
                    return;
                }
            }
        }
        super.visit(listConstructor, data);
    }

    private void checkTupleWithSequence(BType type) {
        if (type.tag != TypeTags.TUPLE) {
            return;
        }
        BTupleType tupleType = (BTupleType) type;
        if (tupleType.getMembers().size() != 1) {
            return;
        }
        BTupleMember memberType = tupleType.getMembers().get(0);
        if (memberType.type.tag == TypeTags.SEQUENCE) {
            tupleType.restType = ((BSequenceType) memberType.type).elementType;
            tupleType.getMembers().clear();
        }
    }

    private SymbolEnv getEnvAfterJoinNode(SymbolEnv env, BLangNode node) {
        SymbolEnv clone = env.createClone();
        while (clone != null && clone.node != node) {
            clone = clone.enclEnv;
        }
        if (clone != null) {
            clone.enclEnv = getEnvBeforeInputNode(clone.enclEnv, getLastInputNodeFromEnv(clone.enclEnv));
        } else {
            clone = new SymbolEnv(node, null);
        }
        return clone;
    }

    private SymbolEnv getEnvBeforeInputNode(SymbolEnv env, BLangNode node) {
        while (env != null && env.node != node) {
            env = env.enclEnv;
        }
        return env != null && env.enclEnv != null
                ? env.enclEnv.createClone()
                : new SymbolEnv(node, null);
    }

    private BLangNode getLastInputNodeFromEnv(SymbolEnv env) {
        while (env != null && (env.node.getKind() != NodeKind.FROM && env.node.getKind() != NodeKind.JOIN)) {
            env = env.enclEnv;
        }
        return env != null ? env.node : null;
    }

    /**
     * @since 2201.5.0
     */
    public static class AnalyzerData {
        boolean queryCompletesEarly = false;
        HashSet<BType> completeEarlyErrorList = new HashSet<>();
        boolean withinSequenceContext = false;
    }
}

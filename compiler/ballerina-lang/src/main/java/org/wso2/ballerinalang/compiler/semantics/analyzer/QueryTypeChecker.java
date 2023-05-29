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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDo;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
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

/**
 * @since 2201.5.0
 */
public class QueryTypeChecker extends TypeChecker {

    private static final CompilerContext.Key<QueryTypeChecker> QUERY_TYPE_CHECKER_KEY = new CompilerContext.Key<>();

    private final Types types;
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final SemanticAnalyzer semanticAnalyzer;
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

        commonAnalyzerData.queryFinalClauses.push(queryExpr.getSelectClause());
        List<BLangNode> clauses = queryExpr.getQueryClauses();
        clauses.forEach(clause -> clause.accept(this, data));

        BType actualType = resolveQueryType(commonAnalyzerData.queryEnvs.peek(),
                ((BLangSelectClause) commonAnalyzerData.queryFinalClauses.peek()).expression,
                data.expType, queryExpr, clauses, data);
        actualType = (actualType == symTable.semanticError) ? actualType : types.checkType(queryExpr.pos,
                actualType, data.expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES);
        commonAnalyzerData.queryFinalClauses.pop();
        commonAnalyzerData.queryEnvs.pop();
        if (!commonAnalyzerData.breakToParallelQueryEnv) {
            data.prevEnvs.pop();
        }

        BType referredActualType = Types.getReferredType(actualType);
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
        List<BLangNode> clauses = queryAction.getQueryClauses();
        clauses.forEach(clause -> clause.accept(this, data));
        List<BType> collectionTypes = getCollectionTypes(clauses);
        BType completionType = getCompletionType(collectionTypes, Types.QueryConstructType.ACTION, data);
        // Analyze foreach node's statements.
        semanticAnalyzer.analyzeNode(doClause.body, SymbolEnv.createBlockEnv(doClause.body,
                commonAnalyzerData.queryEnvs.peek()), data.prevEnvs, commonAnalyzerData);
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
        for (BType type : safeResultTypes) {
            solveSelectTypeAndResolveType(queryExpr, selectExp, type, collectionNode.getBType(), selectTypes,
                    resolvedTypes, env, data, false);
        }
        if (selectTypes.size() == 1) {
            List<BType> collectionTypes = getCollectionTypes(clauses);
            BType completionType = getCompletionType(collectionTypes, types.getQueryConstructType(queryExpr), data);
            selectType = selectTypes.get(0);
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

    void solveSelectTypeAndResolveType(BLangQueryExpr queryExpr, BLangExpression selectExp, BType expType,
                                       BType collectionType, List<BType> selectTypes, List<BType> resolvedTypes,
                                       SymbolEnv env, TypeChecker.AnalyzerData data, boolean isReadonly) {
        BType selectType, resolvedType;
        BType type = Types.getReferredType(expType);
        switch (type.tag) {
            case TypeTags.ARRAY:
                BType elementType = ((BArrayType) type).eType;
                selectType = checkExpr(selectExp, env, elementType, data);
                BType queryResultType = new BArrayType(selectType);
                resolvedType = getResolvedType(queryResultType, type, isReadonly, env);
                break;
            case TypeTags.TABLE:
                selectType = checkExpr(selectExp, env, types.getSafeType(((BTableType) type).constraint,
                        true, true), data);
                resolvedType = getResolvedType(symTable.tableType, type, isReadonly, env);
                break;
            case TypeTags.STREAM:
                selectType = checkExpr(selectExp, env, types.getSafeType(((BStreamType) type).constraint,
                        true, true), data);
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
                selectType = checkExpr(selectExp, env, newExpType, data);
                resolvedType = getResolvedType(selectType, type, isReadonly, env);
                break;
            case TypeTags.STRING:
            case TypeTags.XML:
            case TypeTags.XML_COMMENT:
            case TypeTags.XML_ELEMENT:
            case TypeTags.XML_PI:
            case TypeTags.XML_TEXT:
                selectType = checkExpr(selectExp, env, type, data);
                resolvedType = selectType;
                break;
            case TypeTags.INTERSECTION:
                type = ((BIntersectionType) type).effectiveType;
                solveSelectTypeAndResolveType(queryExpr, selectExp, type, collectionType, selectTypes,
                        resolvedTypes, env, data, Symbols.isFlagOn(type.flags, Flags.READONLY));
                return;
            case TypeTags.NONE:
            default:
                // contextually expected type not given (i.e var).
                selectType = checkExprSilent(nodeCloner.cloneNode(selectExp), type, data);
                if (selectType != symTable.semanticError) {
                    selectType = checkExpr(selectExp, env, type, data);
                }  else {
                    selectType = checkExpr(selectExp, env, data);
                }
                if (queryExpr.isMap) { // A query-expr that constructs a mapping must start with the map keyword.
                    resolvedType = symTable.mapType;
                } else {
                    resolvedType = getNonContextualQueryType(selectType, collectionType);
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
            selectTypes.add(selectType);
            resolvedTypes.add(resolvedType);
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
        BType referredType = Types.getReferredType(selectType);
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
            collectionType = Types.getReferredType(collectionType);
            switch (collectionType.tag) {
                case TypeTags.STREAM:
                    completionType = ((BStreamType) collectionType).completionType;
                    returnType = completionType;
                    break;
                case TypeTags.OBJECT:
                    returnType = types.getVarTypeFromIterableObject((BObjectType) collectionType);
                    break;
                default:
                    BSymbol itrSymbol = symResolver.lookupLangLibMethod(collectionType,
                            Names.fromString(BLangCompilerConstants.ITERABLE_COLLECTION_ITERATOR_FUNC), data.env);
                    if (itrSymbol == this.symTable.notFoundSymbol) {
                        return null;
                    }
                    BInvokableSymbol invokableSymbol = (BInvokableSymbol) itrSymbol;
                    returnType = types.getResultTypeOfNextInvocation(
                            (BObjectType) Types.getReferredType(invokableSymbol.retType));
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

    private BType getNonContextualQueryType(BType staticType, BType basicType) {
        BType resultType;
        switch (Types.getReferredType(basicType).tag) {
            case TypeTags.TABLE:
                resultType = symTable.tableType;
                break;
            case TypeTags.STREAM:
                resultType = symTable.streamType;
                break;
            case TypeTags.XML:
                resultType = new BXMLType(staticType, null);
                break;
            case TypeTags.STRING:
                resultType = symTable.stringType;
                break;
            default:
                resultType = new BArrayType(staticType);
                break;
        }
        return resultType;
    }

    private boolean validateTableType(BTableType tableType, TypeChecker.AnalyzerData data) {
        BType constraint = Types.getReferredType(tableType.constraint);
        if (tableType.isTypeInlineDefined && !types.isAssignable(constraint, symTable.mapAllType)) {
            dlog.error(tableType.constraintPos, DiagnosticErrorCode.TABLE_CONSTRAINT_INVALID_SUBTYPE, constraint);
            data.resultType = symTable.semanticError;
            return false;
        }
        return true;
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
            semanticAnalyzer.analyzeNode((BLangNode) letVariable.definitionNode, letEnv, commonAnalyzerData);
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
    public void visit(BLangDo doNode, TypeChecker.AnalyzerData data) {
        if (doNode.onFailClause != null) {
            doNode.onFailClause.accept(this, data);
        }
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr, TypeChecker.AnalyzerData data) {
        visitCheckAndCheckPanicExpr(checkedExpr, data);
        if (checkedExpr.equivalentErrorTypeList != null) {
            data.commonAnalyzerData.checkedErrorList.addAll(checkedExpr.equivalentErrorTypeList);
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
    }
}

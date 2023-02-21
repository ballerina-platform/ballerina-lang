/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.desugar;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangInputClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordSpreadOperatorField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRegExpTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNDERSCORE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * Class responsible for desugar query pipeline into actual Ballerina code.
 *
 * @since 1.2.0
 */
public class QueryDesugar extends BLangNodeVisitor {
    private static final Name QUERY_CREATE_PIPELINE_FUNCTION = new Name("createPipeline");
    private static final Name QUERY_CREATE_INPUT_FUNCTION = new Name("createInputFunction");
    private static final Name QUERY_CREATE_NESTED_FROM_FUNCTION = new Name("createNestedFromFunction");
    private static final Name QUERY_CREATE_LET_FUNCTION = new Name("createLetFunction");
    private static final Name QUERY_CREATE_INNER_JOIN_FUNCTION = new Name("createInnerJoinFunction");
    private static final Name QUERY_CREATE_OUTER_JOIN_FUNCTION = new Name("createOuterJoinFunction");
    private static final Name QUERY_CREATE_FILTER_FUNCTION = new Name("createFilterFunction");
    private static final Name QUERY_CREATE_ORDER_BY_FUNCTION = new Name("createOrderByFunction");
    private static final Name QUERY_CREATE_SELECT_FUNCTION = new Name("createSelectFunction");
    private static final Name QUERY_CREATE_DO_FUNCTION = new Name("createDoFunction");
    private static final Name QUERY_CREATE_LIMIT_FUNCTION = new Name("createLimitFunction");
    private static final Name QUERY_ADD_STREAM_FUNCTION = new Name("addStreamFunction");
    private static final Name QUERY_CONSUME_STREAM_FUNCTION = new Name("consumeStream");
    private static final Name QUERY_TO_ARRAY_FUNCTION = new Name("toArray");
    private static final Name QUERY_TO_STRING_FUNCTION = new Name("toString");
    private static final Name QUERY_TO_XML_FUNCTION = new Name("toXML");
    private static final Name QUERY_ADD_TO_TABLE_FUNCTION = new Name("addToTable");
    private static final Name QUERY_ADD_TO_MAP_FUNCTION = new Name("addToMap");
    private static final Name QUERY_GET_STREAM_FROM_PIPELINE_FUNCTION = new Name("getStreamFromPipeline");
    private static final Name QUERY_GET_QUERY_ERROR_ROOT_CAUSE_FUNCTION = new Name("getQueryErrorRootCause");
    private static final String FRAME_PARAMETER_NAME = "$frame$";
    private static final Name QUERY_BODY_DISTINCT_ERROR_NAME = new Name("Error");
    private static final Name QUERY_PIPELINE_DISTINCT_ERROR_NAME = new Name("CompleteEarlyError");
    private static final Name QUERY_DISTINCT_UNION_ERROR_NAME = new Name("QueryErrorTypes");
    private static final CompilerContext.Key<QueryDesugar> QUERY_DESUGAR_KEY = new CompilerContext.Key<>();
    private BLangExpression onConflictExpr;
    private BVarSymbol currentFrameSymbol;
    private BLangBlockFunctionBody currentQueryLambdaBody;
    private Map<String, BSymbol> identifiers;
    private int streamElementCount = 0;
    private final Desugar desugar;
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final Names names;
    private final Types types;
    private SymbolEnv env;
    private SymbolEnv queryEnv;
    private boolean containsCheckExpr;
    private boolean withinLambdaFunc = false;
    private HashSet<BType> checkedErrorList;

    private QueryDesugar(CompilerContext context) {
        context.put(QUERY_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
        this.desugar = Desugar.getInstance(context);
    }

    public static QueryDesugar getInstance(CompilerContext context) {
        QueryDesugar desugar = context.get(QUERY_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new QueryDesugar(context);
        }
        return desugar;
    }

    /**
     * Desugar query expression.
     *
     * @param queryExpr query expression to be desugared.
     * @param env       symbol env.
     * @param stmtsToBePropagated list of statements to be propagated.
     * @return desugared query expression.
     */
    BLangStatementExpression desugar(BLangQueryExpr queryExpr, SymbolEnv env,
                                     List<BLangStatement> stmtsToBePropagated) {
        containsCheckExpr = false;
        HashSet<BType> prevCheckedErrorList = this.checkedErrorList;
        this.checkedErrorList = new HashSet<>();

        List<BLangNode> clauses = queryExpr.getQueryClauses();
        Location pos = clauses.get(0).pos;
        BLangBlockStmt queryBlock = ASTBuilderUtil.createBlockStmt(pos);
        BLangVariableReference streamRef = buildStream(clauses, queryExpr.getBType(), env,
                queryBlock, stmtsToBePropagated);
        BLangExpression result = streamRef;
        BLangLiteral isReadonly = ASTBuilderUtil.createLiteral(pos, symTable.booleanType,
                Symbols.isFlagOn(queryExpr.getBType().flags, Flags.READONLY));
        BType resultType = queryExpr.getBType();
        if (queryExpr.isStream) {
            resultType = streamRef.getBType();
        } else if (queryExpr.isTable) {
            onConflictExpr = (onConflictExpr == null)
                    ? ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE)
                    : onConflictExpr;
            BLangVariableReference tableRef = addTableConstructor(queryExpr, queryBlock);
            result = getStreamFunctionVariableRef(queryBlock,
                    QUERY_ADD_TO_TABLE_FUNCTION, Lists.of(streamRef, tableRef, onConflictExpr, isReadonly), pos);
            resultType = tableRef.getBType();
            onConflictExpr = null;
        } else if (queryExpr.isMap) {
            onConflictExpr = (onConflictExpr == null)
                    ? ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE)
                    : onConflictExpr;
            BMapType mapType = (BMapType) getMapType(queryExpr.getBType());
            BLangRecordLiteral.BLangMapLiteral mapLiteral = new BLangRecordLiteral.BLangMapLiteral(queryExpr.pos,
                    mapType, new ArrayList<>());
            result = getStreamFunctionVariableRef(queryBlock,
                    QUERY_ADD_TO_MAP_FUNCTION, Lists.of(streamRef, mapLiteral, onConflictExpr, isReadonly), pos);
            onConflictExpr = null;
        } else {
            BType refType = Types.getReferredType(queryExpr.getBType());
            if (isXml(refType)) {
                if (types.isSubTypeOfReadOnly(refType, env)) {
                    isReadonly.value = true;
                }
                result = getStreamFunctionVariableRef(queryBlock, QUERY_TO_XML_FUNCTION,
                        Lists.of(streamRef, isReadonly), pos);
            } else if (TypeTags.isStringTypeTag(refType.tag)) {
                result = getStreamFunctionVariableRef(queryBlock, QUERY_TO_STRING_FUNCTION, Lists.of(streamRef), pos);
            } else {
                BType arrayType = refType;
                if (refType.tag == TypeTags.UNION) {
                    arrayType = ((BUnionType) refType).getMemberTypes()
                            .stream().filter(m -> Types.getReferredType(m).tag == TypeTags.ARRAY)
                            .findFirst().orElse(symTable.arrayType);
                }
                BLangArrayLiteral arr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
                arr.exprs = new ArrayList<>();
                arr.setBType(arrayType);
                result = getStreamFunctionVariableRef(queryBlock, QUERY_TO_ARRAY_FUNCTION,
                        Lists.of(streamRef, arr, isReadonly), pos);
            }
        }
        handleErrorReturnsFromQuery(pos, result, queryBlock, false, resultType);
        BLangStatementExpression streamStmtExpr = ASTBuilderUtil.createStatementExpression(queryBlock,
                addTypeConversionExpr(result, queryExpr.getBType()));
        streamStmtExpr.setBType(resultType);
        this.checkedErrorList = prevCheckedErrorList;
        return streamStmtExpr;
    }

    private BType getMapType(BType type) {
        BType resultantType = types.getSafeType(Types.getReferredType(type), false, true);
        if (resultantType.tag == TypeTags.INTERSECTION) {
            return getMapType(((BIntersectionType) resultantType).effectiveType);
        }
        return resultantType;
    }

    private boolean isXml(BType type) {
        BType refType = Types.getReferredType(type);

        if (TypeTags.isXMLTypeTag(refType.tag)) {
            return true;
        }
        switch (refType.tag) {
            case TypeTags.UNION:
                for (BType memberType : ((BUnionType) refType).getMemberTypes()) {
                    if (!isXml(memberType)) {
                        return false;
                    }
                }
                return true;
            case TypeTags.INTERSECTION:
                return isXml(((BIntersectionType) refType).getEffectiveType());
            default:
                return false;
        }
    }

    /**
     * Desugar query action.
     *
     * @param queryAction         query action to be desugared.
     * @param env                 symbol env.
     * @param stmtsToBePropagated statements to be propagated to do clause.
     * @return desugared query action.
     */
    BLangStatementExpression desugar(BLangQueryAction queryAction, SymbolEnv env,
                                     List<BLangStatement> stmtsToBePropagated) {
        containsCheckExpr = false;
        HashSet<BType> prevCheckedErrorList = this.checkedErrorList;
        this.checkedErrorList = new HashSet<>();
        List<BLangNode> clauses = queryAction.getQueryClauses();
        Location pos = clauses.get(0).pos;
        BType returnType = symTable.errorOrNilType;
        if (queryAction.returnsWithinDoClause) {
            BInvokableSymbol invokableSymbol = env.enclInvokable.symbol;
            returnType = ((BInvokableType) invokableSymbol.type).retType;
        }
        BLangBlockStmt queryBlock = ASTBuilderUtil.createBlockStmt(pos);
        BLangVariableReference streamRef = buildStream(clauses, returnType, env, queryBlock, stmtsToBePropagated);
        BLangVariableReference result = getStreamFunctionVariableRef(queryBlock,
                QUERY_CONSUME_STREAM_FUNCTION, null, Lists.of(streamRef), pos);
        BLangStatementExpression stmtExpr;
        handleErrorReturnsFromQuery(pos, result, queryBlock, queryAction.returnsWithinDoClause, returnType);

        stmtExpr = ASTBuilderUtil.createStatementExpression(queryBlock,
                addTypeConversionExpr(result, returnType));
        stmtExpr.setBType(returnType);
        this.checkedErrorList = prevCheckedErrorList;
        return stmtExpr;
    }

    //Creates return branches to handle different runtime values
    private void handleErrorReturnsFromQuery(Location pos, BLangExpression resultRef, BLangBlockStmt queryBlock,
                                     boolean returnsWithinDoClause, BType returnType) {
        if (containsCheckExpr) {
            BLangInvocation getRootCauseError = createQueryLibInvocation(QUERY_GET_QUERY_ERROR_ROOT_CAUSE_FUNCTION,
                    Lists.of(addTypeConversionExpr(resultRef, symTable.errorType)), pos);
            // if ($streamElement$ is QueryError) {
            //      fail <error>$streamElement$;
            // }
            BSymbol queryErrorSymbol = symTable.langQueryModuleSymbol
                    .scope.lookup(QUERY_BODY_DISTINCT_ERROR_NAME).symbol;
            BType errorType = queryErrorSymbol.type;
            BLangErrorType queryErrorTypeNode = desugar.getErrorTypeNode();
            queryErrorTypeNode.setBType(errorType);
            // $streamElement$ is QueryError
            BLangTypeTestExpr testExpr = ASTBuilderUtil.createTypeTestExpr(pos, resultRef, queryErrorTypeNode);
            testExpr.setBType(symTable.booleanType);
            desugar.failFastForErrorResult(pos, queryBlock, testExpr, getRootCauseError);
        }
        // if (!($streamElement$ is () || $streamElement$ is CompleteEarlyError )) {
        //      return $streamElement$;
        // } else if ($streamElement$ is CompleteEarlyError) {
        //      $streamElement$ =  unwrapQueryError($streamElement$);
        // }
        BLangIf ifStatement = ASTBuilderUtil.createIfStmt(pos, queryBlock);
        if (returnsWithinDoClause) {
            BLangReturn returnStmt = ASTBuilderUtil.createReturnStmt(pos, addTypeConversionExpr(resultRef, returnType));
            BLangBlockStmt ifBody = ASTBuilderUtil.createBlockStmt(pos);
            ifBody.stmts.add(returnStmt);

            BSymbol completeEarlyErrorSymbol = symTable.langQueryModuleSymbol
                    .scope.lookup(QUERY_PIPELINE_DISTINCT_ERROR_NAME).symbol;
            BType completeEarlyErrorType = completeEarlyErrorSymbol.type;
            BLangErrorType completeEarlyErrorTypeNode = (BLangErrorType) TreeBuilder.createErrorTypeNode();
            completeEarlyErrorTypeNode.setBType(completeEarlyErrorType);

            BLangTypeTestExpr completeEarlyTypeTestExpr = desugar.createTypeCheckExpr(pos, resultRef,
                    completeEarlyErrorTypeNode);
            BLangTypeTestExpr nilTypeTestExpr = desugar.getNilTypeTestExpr(pos, resultRef);
            BLangBinaryExpr isNilOrErrorCheck = ASTBuilderUtil.createBinaryExpr(pos, nilTypeTestExpr,
                    completeEarlyTypeTestExpr, symTable.booleanType, OperatorKind.OR, null);

            BLangGroupExpr notNilOrErrCheckExpr = new BLangGroupExpr();
            notNilOrErrCheckExpr.setBType(symTable.booleanType);
            // !($streamElement$ is () || $streamElement$ is CompleteEarlyError)
            notNilOrErrCheckExpr.expression = desugar.createNotBinaryExpression(pos, isNilOrErrorCheck);
            ifStatement.expr = notNilOrErrCheckExpr;
            ifStatement.body = ifBody;
        }
        BSymbol queryErrorUnionSymbol = symTable.langQueryModuleSymbol
                .scope.lookup(QUERY_DISTINCT_UNION_ERROR_NAME).symbol;
        BType queryErrorUnionErrorType = queryErrorUnionSymbol.type;
        BLangErrorType queryErrorUnionErrorTypeNode = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        queryErrorUnionErrorTypeNode.setBType(queryErrorUnionErrorType);
        BLangTypeTestExpr queryErrorUnionTypeTestExpr = desugar.createTypeCheckExpr(pos, resultRef,
                queryErrorUnionErrorTypeNode);

        BLangInvocation getRootCauseErrorInvo = createQueryLibInvocation(QUERY_GET_QUERY_ERROR_ROOT_CAUSE_FUNCTION,
                Lists.of(addTypeConversionExpr(resultRef, symTable.errorType)), pos);
        BLangBlockStmt ifErrorBody = ASTBuilderUtil.createBlockStmt(pos);
        BLangAssignment unwrapDistinctError = ASTBuilderUtil.createAssignmentStmt(pos, resultRef,
                desugar.addConversionExprIfRequired(getRootCauseErrorInvo, symTable.errorType));
        ifErrorBody.stmts.add(unwrapDistinctError);
        if (ifStatement.expr == null) {
            ifStatement.expr = queryErrorUnionTypeTestExpr;
            ifStatement.body = ifErrorBody;
        } else {
            BLangIf elseIfStmt = ASTBuilderUtil.createIfStmt(pos, queryBlock);
            elseIfStmt.expr = queryErrorUnionTypeTestExpr;
            elseIfStmt.body = ifErrorBody;
            ifStatement.elseStmt = elseIfStmt;
        }
    }

    /**
     * Write the pipeline to the given `block` and return the reference to the resulting stream.
     *
     * @param clauses list of query clauses.
     * @param resultType result type of the query output.
     * @param env symbol env.
     * @param block parent block to write to.
     * @param stmtsToBePropagated list of statements to be propagated.
     * @return variableReference to created _StreamPipeline.
     */
    BLangVariableReference buildStream(List<BLangNode> clauses, BType resultType, SymbolEnv env,
                                       BLangBlockStmt block, List<BLangStatement> stmtsToBePropagated) {
        this.env = env;
        BLangFromClause initFromClause = (BLangFromClause) clauses.get(0);
        final BLangVariableReference initPipeline = addPipeline(block, initFromClause.pos,
                initFromClause.collection, resultType);
        BLangVariableReference initFrom = addInputFunction(block, initFromClause, stmtsToBePropagated);
        addStreamFunction(block, initPipeline, initFrom);
        for (BLangNode clause : clauses.subList(1, clauses.size())) {
            switch (clause.getKind()) {
                case FROM:
                    BLangFromClause fromClause = (BLangFromClause) clause;
                    BLangVariableReference nestedFromFunc = addNestedFromFunction(block, fromClause,
                            stmtsToBePropagated);
                    addStreamFunction(block, initPipeline, nestedFromFunc);
                    BLangVariableReference fromInputFunc = addInputFunction(block, fromClause, stmtsToBePropagated);
                    addStreamFunction(block, initPipeline, fromInputFunc);
                    break;
                case JOIN:
                    BLangJoinClause joinClause = (BLangJoinClause) clause;
                    BLangVariableReference joinPipeline = addPipeline(block, joinClause.pos,
                            joinClause.collection, resultType);
                    BLangVariableReference joinInputFunc = addInputFunction(block, joinClause, stmtsToBePropagated);
                    addStreamFunction(block, joinPipeline, joinInputFunc);
                    BLangVariableReference joinFunc = addJoinFunction(block, joinClause, joinPipeline,
                            stmtsToBePropagated);
                    addStreamFunction(block, initPipeline, joinFunc);
                    break;
                case LET_CLAUSE:
                    BLangVariableReference letFunc = addLetFunction(block, (BLangLetClause) clause,
                            stmtsToBePropagated);
                    addStreamFunction(block, initPipeline, letFunc);
                    break;
                case WHERE:
                    BLangVariableReference whereFunc = addWhereFunction(block, (BLangWhereClause) clause,
                            stmtsToBePropagated);
                    addStreamFunction(block, initPipeline, whereFunc);
                    break;
                case ORDER_BY:
                    BLangVariableReference orderFunc = addOrderByFunction(block, (BLangOrderByClause) clause,
                            stmtsToBePropagated);
                    addStreamFunction(block, initPipeline, orderFunc);
                    break;
                case SELECT:
                    BLangVariableReference selectFunc = addSelectFunction(block, (BLangSelectClause) clause,
                            stmtsToBePropagated);
                    addStreamFunction(block, initPipeline, selectFunc);
                    break;
                case DO:
                    BLangVariableReference doFunc = addDoFunction(block, (BLangDoClause) clause, stmtsToBePropagated);
                    addStreamFunction(block, initPipeline, doFunc);
                    break;
                case LIMIT:
                    BLangVariableReference limitFunc = addLimitFunction(block, (BLangLimitClause) clause);
                    addStreamFunction(block, initPipeline, limitFunc);
                    break;
                case ON_CONFLICT:
                    final BLangOnConflictClause onConflict = (BLangOnConflictClause) clause;
                    onConflictExpr = onConflict.expression;
                    break;
            }
        }
        return addGetStreamFromPipeline(block, initPipeline);
    }

    // ---- Util methods to create the stream pipeline. ---- //
    /**
     * Desugar fromClause/joinClause to below and return a reference to created join _StreamPipeline.
     * _StreamPipeline pipeline = createPipeline(collection);
     *
     * @param blockStmt  parent block to write to.
     * @param pos diagnostic pos of the collection.
     * @param collection reference to the collection.
     * @param resultType constraint type of the collection.
     * @return variableReference to created _StreamPipeline.
     */
    BLangVariableReference addPipeline(BLangBlockStmt blockStmt, Location pos,
                                       BLangExpression collection, BType resultType) {
        String name = getNewVarName();
        BVarSymbol dataSymbol = new BVarSymbol(0, Names.fromString(name), env.scope.owner.pkgID,
                collection.getBType(), this.env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable dataVariable =
                ASTBuilderUtil.createVariable(pos, name, collection.getBType(),
                        addTypeConversionExpr(collection, collection.getBType()), dataSymbol);
        BLangSimpleVariableDef dataVarDef = ASTBuilderUtil.createVariableDef(pos, dataVariable);
        BLangVariableReference valueVarRef = ASTBuilderUtil.createVariableRef(pos, dataSymbol);
        blockStmt.addStatement(dataVarDef);
        BType constraintType = resultType;
        BType completionType = symTable.errorOrNilType;
        BType refType = Types.getReferredType(resultType);
        boolean isStream = false;
        if (refType.tag == TypeTags.ARRAY) {
            constraintType = ((BArrayType) refType).eType;
        } else if (refType.tag == TypeTags.STREAM) {
            isStream = true;
            constraintType = ((BStreamType) refType).constraint;
            completionType = ((BStreamType) refType).completionType;
        }
        BType constraintTdType = new BTypedescType(constraintType, symTable.typeDesc.tsymbol);
        BLangTypedescExpr constraintTdExpr = new BLangTypedescExpr();
        constraintTdExpr.resolvedType = constraintType;
        constraintTdExpr.setBType(constraintTdType);
        BType completionTdType = new BTypedescType(completionType, symTable.typeDesc.tsymbol);
        BLangTypedescExpr completionTdExpr = new BLangTypedescExpr();
        completionTdExpr.resolvedType = completionType;
        completionTdExpr.setBType(completionTdType);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_PIPELINE_FUNCTION,
                Lists.of(valueVarRef, constraintTdExpr, completionTdExpr, desugar.getBooleanLiteral(isStream)), pos);
    }

    /**
     * Desugar inputClause to below and return a reference to created from _StreamFunction.
     * _StreamFunction xsFrom = createFromFunction(function(_Frame frame) returns _Frame|error? {
     * int x = <int> frame["value"];
     * frame["x"] = x;
     * return frame;
     * });
     *
     * @param blockStmt           parent block to write to.
     * @param inputClause         to be desugared.
     * @param stmtsToBePropagated list of statements to be propagated.
     * @return variableReference to created from _StreamFunction.
     */
    BLangVariableReference addInputFunction(BLangBlockStmt blockStmt, BLangInputClause inputClause,
                                            List<BLangStatement> stmtsToBePropagated) {
        Location pos = inputClause.pos;
        // function(_Frame frame) returns _Frame|error? { return frame; }
        BLangLambdaFunction lambda = createPassthroughLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        body.stmts.addAll(0, stmtsToBePropagated);
        BVarSymbol frameSymbol = lambda.function.requiredParams.get(0).symbol;

        // frame["x"] = x;, note: stmts will get added in reverse order.
        List<BVarSymbol> symbols = getIntroducedSymbols((BLangVariable)
                inputClause.variableDefinitionNode.getVariable());
        shadowSymbolScope(pos, body, ASTBuilderUtil.createVariableRef(pos, frameSymbol), symbols);

        // int x = <int> frame["value"];, note: stmts will get added in reverse order.
        BLangFieldBasedAccess valueAccessExpr = desugar.getValueAccessExpression(inputClause.pos,
                symTable.anyOrErrorType, frameSymbol);
        valueAccessExpr.expr = desugar.addConversionExprIfRequired(valueAccessExpr.expr,
                types.getSafeType(valueAccessExpr.expr.getBType(), true, false));
        VariableDefinitionNode variableDefinitionNode = inputClause.variableDefinitionNode;
        BLangVariable variable = (BLangVariable) variableDefinitionNode.getVariable();
        setSymbolOwner(variable, env.scope.owner);
        variable.setInitialExpression(desugar.addConversionExprIfRequired(valueAccessExpr, inputClause.varType));
        // add at 0, otherwise, this goes under existing stmts.
        body.stmts.add(0, (BLangStatement) variableDefinitionNode);

        // at this point;
        // function(_Frame frame) returns _Frame|error? {
        //      int x = <int> frame["value"];
        //      frame["x"] = x;
        //      return frame;
        // }
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_INPUT_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar fromClause to below and return a reference to created from _StreamFunction.
     * _StreamFunction xnFrom = createNestedFromFunction(function(_Frame frame) returns any|error? {
     * any collection = frame["collection"]
     * return collection;
     * });
     *
     * @param blockStmt           parent block to write to.
     * @param fromClause          to be desugared.
     * @param stmtsToBePropagated list of statements to be propagated.
     * @return variableReference to created from _StreamFunction.
     */
    BLangVariableReference addNestedFromFunction(BLangBlockStmt blockStmt, BLangFromClause fromClause,
                                                 List<BLangStatement> stmtsToBePropagated) {
        Location pos = fromClause.pos;
        // function(_Frame frame) returns any|error? { return collection; }
        BLangUnionTypeNode returnType = getAnyAndErrorTypeNode();
        BLangReturn returnNode = (BLangReturn) TreeBuilder.createReturnNode();
        returnNode.expr = fromClause.collection;
        returnNode.pos = pos;
        BLangLambdaFunction lambda = createLambdaFunction(pos, returnType, returnNode, false);
        ((BLangBlockFunctionBody) lambda.function.body).stmts.addAll(0, stmtsToBePropagated);
        lambda.accept(this);
        // at this point;
        // function(_Frame frame) returns any|error? {
        //      any collection = frame["collection"]
        //      return collection;
        // }
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_NESTED_FROM_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar joinClauses to below and return a reference to created join _StreamFunction.
     * _StreamFunction joinFunc = createJoinFunction(joinPipeline);
     *
     * @param blockStmt    parent block to write to.
     * @param joinClause   to be desugared.
     * @param joinPipeline previously created _StreamPipeline reference to be joined.
     * @return variableReference to created join _StreamFunction.
     */
    BLangVariableReference addJoinFunction(BLangBlockStmt blockStmt, BLangJoinClause joinClause,
                                           BLangVariableReference joinPipeline,
                                           List<BLangStatement> stmtsToBePropagated) {
        BLangExpression lhsExpr = (BLangExpression) joinClause.onClause.getLeftExpression();
        BLangExpression rhsExpr = (BLangExpression) joinClause.onClause.getRightExpression();
        BLangLambdaFunction lhsKeyFunction = createKeyFunction(lhsExpr, stmtsToBePropagated);
        BLangLambdaFunction rhsKeyFunction = createKeyFunction(rhsExpr, stmtsToBePropagated);
        if (joinClause.isOuterJoin) {
            List<BVarSymbol> symbols =
                    getIntroducedSymbols((BLangVariable) joinClause.variableDefinitionNode.getVariable());
            final BLangSimpleVarRef nilFrame = defineNilFrameForType(symbols, blockStmt, rhsExpr.pos);
            return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_OUTER_JOIN_FUNCTION,
                    Lists.of(joinPipeline, lhsKeyFunction, rhsKeyFunction, nilFrame), joinClause.pos);
        } else {
            return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_INNER_JOIN_FUNCTION,
                    Lists.of(joinPipeline, lhsKeyFunction, rhsKeyFunction), joinClause.pos);
        }
    }

    /**
     * Desugar letClause to below and return a reference to created let _StreamFunction.
     * _StreamFunction ysLet = createLetFunction(function(_Frame frame) returns _Frame|error? {
     * frame["y2"] = <int> frame["y"] * <int> frame["y"];
     * return frame;
     * });
     *
     * @param blockStmt           parent block to write to.
     * @param letClause           to be desugared.
     * @param stmtsToBePropagated list of statements to be propagated.
     * @return variableReference to created let _StreamFunction.
     */
    BLangVariableReference addLetFunction(BLangBlockStmt blockStmt, BLangLetClause letClause,
                                          List<BLangStatement> stmtsToBePropagated) {
        Location pos = letClause.pos;
        // function(_Frame frame) returns _Frame|error? { return frame; }
        BLangLambdaFunction lambda = createPassthroughLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        BVarSymbol frameSymbol = lambda.function.requiredParams.get(0).symbol;

        // frame["x"] = x;, note: stmts will get added in reverse order.
        List<BVarSymbol> symbols = getIntroducedSymbols(letClause);
        shadowSymbolScope(pos, body, ASTBuilderUtil.createVariableRef(pos, frameSymbol), symbols);

        Collections.reverse(letClause.letVarDeclarations);
        for (BLangLetVariable letVariable : letClause.letVarDeclarations) {
            // add at 0, otherwise, this goes under existing stmts.
            body.stmts.add(0, (BLangStatement) letVariable.definitionNode);
            setSymbolOwner((BLangVariable) letVariable.definitionNode.getVariable(), env.scope.owner);
        }
        body.stmts.addAll(0, stmtsToBePropagated);
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_LET_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar whereClause to below and return a reference to created filter _StreamFunction.
     * _StreamFunction xsFilter = createFilterFunction(function(_Frame frame) returns boolean {
     * return <int>frame["x"] > 0;
     * });
     *
     * @param blockStmt           parent block to write to.
     * @param whereClause         to be desugared.
     * @param stmtsToBePropagated list of statements to be propagated.
     * @return variableReference to created filter _StreamFunction.
     */
    BLangVariableReference addWhereFunction(BLangBlockStmt blockStmt, BLangWhereClause whereClause,
                                            List<BLangStatement> stmtsToBePropagated) {
        Location pos = whereClause.pos;
        BLangLambdaFunction lambda = createFilterLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        BLangReturn returnNode = (BLangReturn) TreeBuilder.createReturnNode();
        returnNode.pos = pos;
        body.stmts.addAll(0, stmtsToBePropagated);
        returnNode.expr = desugar.addConversionExprIfRequired(whereClause.expression,
                lambda.function.returnTypeNode.getBType());
        body.addStatement(returnNode);
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_FILTER_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar orderByClause to below and return a reference to created orderBy _StreamFunction.
     * _StreamFunction orderByFunc = createOrderByFunction(function(_Frame frame) {
     * _Frame frame = {"orderKey": frame["x2"] + frame["y2"], $orderDirection$: true + false"};
     * });
     *
     * @param blockStmt           parent block to write to.
     * @param orderByClause       to be desugared.
     * @param stmtsToBePropagated list of statements to be propagated.
     * @return variableReference to created orderBy _StreamFunction.
     */
    BLangVariableReference addOrderByFunction(BLangBlockStmt blockStmt, BLangOrderByClause orderByClause,
                                              List<BLangStatement> stmtsToBePropagated) {
        Location pos = orderByClause.pos;
        BLangLambdaFunction lambda = createActionLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        body.stmts.addAll(0, stmtsToBePropagated);
        BVarSymbol frameSymbol = lambda.function.requiredParams.get(0).symbol;
        BLangSimpleVarRef frame = ASTBuilderUtil.createVariableRef(pos, frameSymbol);

        BLangArrayLiteral sortFieldsArrayExpr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
        sortFieldsArrayExpr.exprs = new ArrayList<>();
        sortFieldsArrayExpr.setBType(new BArrayType(symTable.anydataType));

        BLangArrayLiteral sortModesArrayExpr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
        sortModesArrayExpr.exprs = new ArrayList<>();
        sortModesArrayExpr.setBType(new BArrayType(symTable.booleanType));

        // Each order-key expression is added to sortFieldsArrayExpr.
        // Corresponding order-direction is added to sortModesArrayExpr.
        for (OrderKeyNode orderKeyNode : orderByClause.getOrderKeyList()) {
            BLangOrderKey orderKey = (BLangOrderKey) orderKeyNode;
            sortFieldsArrayExpr.exprs.add(orderKey.expression);
            sortModesArrayExpr.exprs.add(ASTBuilderUtil.createLiteral(orderKey.pos, symTable.booleanType,
                    orderKey.getOrderDirection()));
        }

        // order-key expressions and order-directions are evaluated for each frame.
        // $frame$["$orderKey$"] = sortFieldsArrExpr;
        BLangStatement orderKeyStmt = getAddToFrameStmt(pos, frame, "$orderKey$", sortFieldsArrayExpr);
        body.stmts.add(orderKeyStmt);
        // $frame$["$orderDirection$"] = sortModesArrayExpr;
        BLangStatement orderDirectionStmt = getAddToFrameStmt(pos, frame, "$orderDirection$", sortModesArrayExpr);
        body.stmts.add(orderDirectionStmt);
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_ORDER_BY_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar selectClause to below and return a reference to created select _StreamFunction.
     * _StreamFunction selectFunc = createSelectFunction(function(_Frame frame) returns _Frame|error? {
     * int x2 = <int> frame["x2"];
     * int y2 = <int> frame["y2"];
     * _Frame frame = {"value": x2 + y2};
     * return frame;
     * });
     *
     * @param blockStmt           parent block to write to.
     * @param selectClause        to be desugared.
     * @param stmtsToBePropagated list of statements to be propagated.
     * @return variableReference to created select _StreamFunction.
     */
    BLangVariableReference addSelectFunction(BLangBlockStmt blockStmt, BLangSelectClause selectClause,
                                             List<BLangStatement> stmtsToBePropagated) {
        Location pos = selectClause.pos;
        BLangLambdaFunction lambda = createPassthroughLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        body.stmts.addAll(0, stmtsToBePropagated);
        BVarSymbol oldFrameSymbol = lambda.function.requiredParams.get(0).symbol;
        BLangSimpleVarRef frame = ASTBuilderUtil.createVariableRef(pos, oldFrameSymbol);
        // $frame$["$value$"] = select-expr;
        BLangStatement assignment = getAddToFrameStmt(pos, frame, "$value$", selectClause.expression);
        body.stmts.add(body.stmts.size() - 1, assignment);
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_SELECT_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar doClause to below and return a reference to created do _StreamFunction.
     * _StreamFunction doFunc = createDoFunction(function(_Frame frame) {
     * int x2 = <int> frame["x2"];
     * int y2 = <int> frame["y2"];
     * });
     *
     * @param blockStmt           parent block to write to.
     * @param doClause            to be desugared.
     * @param stmtsToBePropagated list of statements to be propagated.
     * @return variableReference to created do _StreamFunction.
     */
    BLangVariableReference addDoFunction(BLangBlockStmt blockStmt, BLangDoClause doClause,
                                         List<BLangStatement> stmtsToBePropagated) {
        Location pos = doClause.pos;
        BLangLambdaFunction lambda = createActionLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        body.stmts.addAll(0, stmtsToBePropagated);
        for (BLangStatement stmt : doClause.body.stmts) {
            body.addStatement(stmt);
        }
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_DO_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar limit clause and return a reference to created limit _StreamFunction.
     *
     * @param blockStmt parent block to write to.
     * @param limitClause  to be desugared.
     * @return variableReference to created do _StreamFunction.
     */
    BLangVariableReference addLimitFunction(BLangBlockStmt blockStmt, BLangLimitClause limitClause) {
        Location pos = limitClause.pos;
        BLangUnionTypeNode returnTypeNode = getIntErrorTypeNode();
        BLangReturn returnNode = (BLangReturn) TreeBuilder.createReturnNode();
        returnNode.expr = desugar.addConversionExprIfRequired(limitClause.expression, returnTypeNode.getBType());
        returnNode.pos = pos;
        BLangLambdaFunction limitFunction = createLambdaFunction(pos, returnTypeNode, returnNode, false);
        limitFunction.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_LIMIT_FUNCTION, Lists.of(limitFunction), pos);
    }

    /**
     * Desugar to following invocation.
     * stream:addStreamFunction(pipeline, streamFunction);
     *
     * @param blockStmt   parent block to write to.
     * @param pipelineRef variableReference to pipeline.
     * @param functionRef variableReference to stream function.
     */
    void addStreamFunction(BLangBlockStmt blockStmt, BLangVariableReference pipelineRef,
                           BLangVariableReference functionRef) {
        BLangInvocation addStreamFunctionInvocation = createQueryLibInvocation(QUERY_ADD_STREAM_FUNCTION,
                Lists.of(pipelineRef, functionRef), pipelineRef.pos);
        BLangExpressionStmt stmt = ASTBuilderUtil.createExpressionStmt(pipelineRef.pos, blockStmt);
        stmt.expr = addStreamFunctionInvocation;
    }

    /**
     * Desugar to following invocation.
     * stream<any|error, error?> result = xsPipeline.getStream();
     *
     * @param blockStmt   parent block to write to.
     * @param pipelineRef variableReference to pipeline.
     * @return variableReference to stream.
     */
    BLangVariableReference addGetStreamFromPipeline(BLangBlockStmt blockStmt, BLangVariableReference pipelineRef) {
        Location pos = pipelineRef.pos;
        return getStreamFunctionVariableRef(blockStmt,
                QUERY_GET_STREAM_FROM_PIPELINE_FUNCTION, null, Lists.of(pipelineRef), pos);
    }

    /**
     * Create a table constructor expression.
     *
     * @param queryExpr  query expression.
     * @param queryBlock parent block to write to.
     * @return reference to updated table.
     */
    BLangVariableReference addTableConstructor(BLangQueryExpr queryExpr, BLangBlockStmt queryBlock) {
        // desugar `table<Customer> key(id, name) tab = table key(id, name);`
        Location pos = queryExpr.pos;
        final BType type = queryExpr.getBType();
        String name = getNewVarName();
        BType tableType = type;
        BType refType = Types.getReferredType(type);
        if (refType.tag == TypeTags.UNION) {
            tableType = symTable.tableType;
            for (BType memberType : ((BUnionType) refType).getMemberTypes()) {
                int memberTypeTag = Types.getReferredType(memberType).tag;
                if (memberTypeTag == TypeTags.TABLE) {
                    tableType = memberType;
                } else if (memberTypeTag == TypeTags.INTERSECTION &&
                        ((BIntersectionType) memberType).effectiveType.tag == TypeTags.TABLE) {
                    tableType = ((BIntersectionType) memberType).effectiveType;
                }
            }
        }

        final List<IdentifierNode> keyFieldIdentifiers = queryExpr.fieldNameIdentifierList;
        BLangTableConstructorExpr tableConstructorExpr = (BLangTableConstructorExpr)
                TreeBuilder.createTableConstructorExpressionNode();
        tableConstructorExpr.pos = pos;
        tableConstructorExpr.setBType(tableType);
        if (!keyFieldIdentifiers.isEmpty()) {
            BLangTableKeySpecifier keySpecifier = (BLangTableKeySpecifier)
                    TreeBuilder.createTableKeySpecifierNode();
            keySpecifier.pos = pos;
            for (IdentifierNode identifier : keyFieldIdentifiers) {
                keySpecifier.addFieldNameIdentifier(identifier);
            }
            tableConstructorExpr.tableKeySpecifier = keySpecifier;
        }
        BVarSymbol tableSymbol = new BVarSymbol(0, Names.fromString(name),
                                                env.scope.owner.pkgID, tableType, this.env.scope.owner, pos, VIRTUAL);
        BLangSimpleVariable tableVariable = ASTBuilderUtil.createVariable(pos,
                name, tableType, tableConstructorExpr, tableSymbol);
        queryBlock.addStatement(ASTBuilderUtil.createVariableDef(pos, tableVariable));
        return ASTBuilderUtil.createVariableRef(pos, tableSymbol);
    }

    /**
     * Adds a type cast expression to given expression.
     * @param expr to be casted.
     * @param type to be casted into.
     * @return expression with the type cast.
     */
    private BLangExpression addTypeConversionExpr(BLangExpression expr, BType type) {
        BLangTypeConversionExpr conversionExpr = (BLangTypeConversionExpr)
                TreeBuilder.createTypeConversionNode();
        conversionExpr.expr = expr;
        conversionExpr.targetType = type;
        conversionExpr.setBType(type);
        conversionExpr.pos = expr.pos;
        conversionExpr.checkTypes = false;
        return conversionExpr;
    }

    /**
     * Create and return a lambda `function(_Frame frame) returns _Frame|error? {...; return frame;}`
     *
     * @param pos of the lambda.
     * @return created lambda function.
     */
    private BLangLambdaFunction createPassthroughLambda(Location pos) {
        // returns (_Frame|error)?
        BLangUnionTypeNode returnType = getFrameErrorNilTypeNode();
        // return frame;
        BLangReturn returnNode = (BLangReturn) TreeBuilder.createReturnNode();
        returnNode.pos = pos;
        return createLambdaFunction(pos, returnType, returnNode, true);
    }

    /**
     * Create and return a lambda `function(_Frame frame) returns boolean {...}`.
     *
     * @param pos of the lambda.
     * @return created lambda function.
     */
    private BLangLambdaFunction createFilterLambda(Location pos) {
        // returns boolean|error
        BLangUnionTypeNode returnType = getBooleanErrorTypeNode();
        return createLambdaFunction(pos, returnType, null, false);
    }

    /**
     * Create and return a lambda `function(_Frame frame) {...}`.
     *
     * @param pos of the lambda.
     * @return created lambda function.
     */
    private BLangLambdaFunction createActionLambda(Location pos) {
        // returns any|error
        BLangUnionTypeNode returnType = getAnyAndErrorTypeNode();
        return createLambdaFunction(pos, returnType, null, false);
    }

    /**
     * Creates and return a lambda function without body.
     *
     * @param pos of the lambda.
     * @return created lambda function.
     */
    private BLangLambdaFunction createLambdaFunction(Location pos,
                                                     TypeNode returnType,
                                                     BLangReturn returnNode,
                                                     boolean isPassthrough) {
        // function(_Frame frame) ... and ref to frame
        BType frameType = getFrameTypeSymbol().type;
        BVarSymbol frameSymbol = new BVarSymbol(0, Names.fromString(FRAME_PARAMETER_NAME),
                                                this.env.scope.owner.pkgID, frameType, this.env.scope.owner, pos,
                                                VIRTUAL);
        BLangSimpleVariable frameVariable = ASTBuilderUtil.createVariable(pos, FRAME_PARAMETER_NAME,
                frameSymbol.type, null, frameSymbol);
        BLangVariableReference frameVarRef = ASTBuilderUtil.createVariableRef(pos, frameSymbol);

        // lambda body
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();

        // add `return x;`
        if (returnNode != null) {
            // passthrough will return same frame parameter
            if (isPassthrough) {
                returnNode.setExpression(frameVarRef);
            }
            body.addStatement(returnNode);
        }
        return createLambdaFunction(pos, Lists.of(frameVariable), returnType, body);
    }

    /**
     * Creates and returns a lambda function.
     *
     * @param pos            diagnostic pos.
     * @param requiredParams required parameters.
     * @param returnType     return type of the lambda function.
     * @param lambdaBody     body of the lambda function.
     * @return created lambda function.
     */
    private BLangLambdaFunction createLambdaFunction(Location pos,
                                                     List<BLangSimpleVariable> requiredParams,
                                                     TypeNode returnType,
                                                     BLangFunctionBody lambdaBody) {
        BLangLambdaFunction lambdaFunction = desugar.createLambdaFunction(pos, "$streamLambda$",
                requiredParams, returnType, lambdaBody);
        lambdaFunction.function.addFlag(Flag.QUERY_LAMBDA);
        lambdaFunction.capturedClosureEnv = env;
        return lambdaFunction;
    }

    /**
     * Creates a variable to hold what function invocation returns,
     * and then return a varRef to that variable.
     *
     * @param blockStmt    parent block to write the varDef into.
     * @param functionName function name.
     * @param requiredArgs required args.
     * @param pos          pos diagnostic pos.
     * @return varRef to the created variable.
     */
    private BLangVariableReference getStreamFunctionVariableRef(BLangBlockStmt blockStmt,
                                                                Name functionName,
                                                                List<BLangExpression> requiredArgs,
                                                                Location pos) {
        return getStreamFunctionVariableRef(blockStmt, functionName, null, requiredArgs, pos);
    }

    /**
     * Creates a variable to hold what function invocation returns,
     * and then return a varRef to that variable.
     *
     * @param blockStmt    parent block to write the varDef into.
     * @param functionName function name.
     * @param type         expected type of the variable.
     * @param requiredArgs required args.
     * @param pos          pos diagnostic pos.
     * @return varRef to the created variable.
     */
    private BLangVariableReference getStreamFunctionVariableRef(BLangBlockStmt blockStmt,
                                                                Name functionName,
                                                                BType type,
                                                                List<BLangExpression> requiredArgs,
                                                                Location pos) {
        String name = getNewVarName();
        BLangInvocation queryLibInvocation = createQueryLibInvocation(functionName, requiredArgs, pos);
        type = (type == null) ? queryLibInvocation.getBType() : type;
        BVarSymbol varSymbol = new BVarSymbol(0, new Name(name), env.scope.owner.pkgID, type, env.scope.owner, pos,
                                              VIRTUAL);
        BLangSimpleVariable variable = ASTBuilderUtil.createVariable(pos, name, type,
                desugar.addConversionExprIfRequired(queryLibInvocation, type), varSymbol);
        BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDef(pos, variable);
        blockStmt.addStatement(variableDef);
        return ASTBuilderUtil.createVariableRef(pos, variable.symbol);
    }

    /**
     * Get unique variable name.
     *
     * @return new variable name.
     */
    private String getNewVarName() {
        return "$streamElement$" + UNDERSCORE + streamElementCount++;
    }

    /**
     * Load a function invokable symbol and return a invocation for that function.
     *
     * @param functionName function name.
     * @param requiredArgs list of required args.
     * @param pos          diagnostic pos.
     * @return created invocation.
     */
    private BLangInvocation createQueryLibInvocation(Name functionName,
                                                     List<BLangExpression> requiredArgs,
                                                     Location pos) {
        BInvokableSymbol symbol = getQueryLibInvokableSymbol(functionName);
        BLangInvocation bLangInvocation = ASTBuilderUtil
                .createInvocationExprForMethod(pos, symbol, requiredArgs, symResolver);
        bLangInvocation.setBType(symbol.retType);
        return bLangInvocation;
    }

    /**
     * Load and return symbol for given functionName in query lib.
     *
     * @param functionName of the function.
     * @return symbol for the function.
     */
    private BInvokableSymbol getQueryLibInvokableSymbol(Name functionName) {
        return (BInvokableSymbol) symTable.langQueryModuleSymbol.scope
                .lookup(functionName).symbol;
    }

    private BLangStatement getAddToFrameStmt(Location pos,
                                             BLangVariableReference frame,
                                             String key,
                                             BLangExpression value) {
        BLangIdentifier valueIdentifier = ASTBuilderUtil.createIdentifier(pos, key);
        BLangFieldBasedAccess valueAccess = ASTBuilderUtil.createFieldAccessExpr(frame, valueIdentifier);
        valueAccess.pos = pos;
        valueAccess.setBType(symTable.anyOrErrorType);
        valueAccess.originalType = valueAccess.getBType();
        return ASTBuilderUtil.createAssignmentStmt(pos, valueAccess, value);
    }

    private void shadowSymbolScope(Location pos,
                                   BLangBlockFunctionBody lambdaBody,
                                   BLangSimpleVarRef frameRef,
                                   List<BVarSymbol> symbols) {
        Collections.reverse(symbols);
        for (BVarSymbol symbol : symbols) {
            // since the var decl is now within lambda, remove scope entry from encl env.
            env.scope.entries.remove(symbol.name);
            env.enclPkg.globalVariableDependencies.values().forEach(d -> d.remove(symbol));
            BLangStatement addToFrameStmt = getAddToFrameStmt(pos, frameRef,
                    symbol.name.value, ASTBuilderUtil.createVariableRef(pos, symbol));
            lambdaBody.stmts.add(0, addToFrameStmt);
        }
    }

    private void setSymbolOwner(BLangVariable variable, BSymbol owner) {
        if (variable == null) {
            return;
        }
        switch (variable.getKind()) {
            case VARIABLE:
                if (variable.symbol == null) {
                    return;
                }
                variable.symbol.owner = owner;
                break;
            case TUPLE_VARIABLE:
                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                tupleVariable.memberVariables.forEach(v -> setSymbolOwner(v, owner));
                setSymbolOwner(tupleVariable.restVariable, owner);
                break;
            case RECORD_VARIABLE:
                BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
                recordVariable.variableList.forEach(value -> setSymbolOwner(value.valueBindingPattern, owner));
                setSymbolOwner(recordVariable.restParam, owner);
                break;
            case ERROR_VARIABLE:
                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                setSymbolOwner(errorVariable.message, owner);
                setSymbolOwner(errorVariable.restDetail, owner);
                errorVariable.detail.forEach(bLangErrorDetailEntry ->
                        setSymbolOwner(bLangErrorDetailEntry.valueBindingPattern, owner));
                break;
        }
    }

    private List<BVarSymbol> getIntroducedSymbols(BLangLetClause letClause) {
        List<BVarSymbol> symbols = new ArrayList<>();
        for (BLangLetVariable letVariable : letClause.letVarDeclarations) {
            symbols.addAll(getIntroducedSymbols(letVariable));
        }
        return symbols;
    }

    private List<BVarSymbol> getIntroducedSymbols(BLangLetVariable variable) {
        return getIntroducedSymbols((BLangVariable) variable.definitionNode.getVariable());
    }

    private List<BVarSymbol> getIntroducedSymbols(BLangVariable variable) {
        if (variable != null) {
            List<BVarSymbol> symbols = new ArrayList<>();
            if (variable.getKind() == NodeKind.RECORD_VARIABLE) {
                // Record binding
                BLangRecordVariable record = (BLangRecordVariable) variable;
                for (BLangRecordVariable.BLangRecordVariableKeyValue keyValue : record.variableList) {
                    symbols.addAll(getIntroducedSymbols(keyValue.valueBindingPattern));
                }
                if (record.hasRestParam()) {
                    symbols.addAll(getIntroducedSymbols(record.restParam));
                }
            } else if (variable.getKind() == NodeKind.TUPLE_VARIABLE) {
                // Tuple binding
                BLangTupleVariable tuple = (BLangTupleVariable) variable;
                for (BLangVariable memberVariable : tuple.memberVariables) {
                    symbols.addAll(getIntroducedSymbols(memberVariable));
                }
                if (tuple.restVariable != null) {
                    symbols.addAll(getIntroducedSymbols(tuple.restVariable));
                }
            } else if (variable.getKind() == NodeKind.ERROR_VARIABLE) {
                // Error binding
                BLangErrorVariable error = (BLangErrorVariable) variable;
                if (error.message != null) {
                    symbols.addAll(getIntroducedSymbols(error.message));
                }
                if (error.restDetail != null) {
                    symbols.addAll(getIntroducedSymbols(error.restDetail));
                }
                for (BLangErrorVariable.BLangErrorDetailEntry entry : error.detail) {
                    symbols.addAll(getIntroducedSymbols(entry.valueBindingPattern));
                }
            } else {
                // Simple binding
                if (variable.symbol != null) {
                    symbols.add(((BLangSimpleVariable) variable).symbol);
                }
            }
            return symbols;
        }
        return Collections.emptyList();
    }

    /**
     * Creates a lambda key function for a given expression.
     * function (_Frame _frame) returns any {
     * returns keyExpr;
     * }
     *
     * @param expr key function expression.
     * @param stmtsToBePropagated list of statements to be propagated.
     * @return created key function lambda.
     */
    private BLangLambdaFunction createKeyFunction(BLangExpression expr, List<BLangStatement> stmtsToBePropagated) {
        BLangReturn returnNode = (BLangReturn) TreeBuilder.createReturnNode();
        returnNode.expr = desugar.addConversionExprIfRequired(expr, symTable.anyOrErrorType);
        returnNode.pos = expr.pos;
        BLangLambdaFunction keyFunction = createLambdaFunction(expr.pos, getAnyAndErrorTypeNode(), returnNode, false);
        ((BLangBlockFunctionBody) keyFunction.function.body).stmts.addAll(0, stmtsToBePropagated);
        keyFunction.accept(this);
        return keyFunction;
    }

    /**
     * Defines a _Frame with nil value fields for given symbols.
     *
     * @param symbols   list to be added to the _Frame.
     * @param blockStmt parent block to write to.
     * @param pos       diagnostic position.
     * @return variableReference to created _Frame.
     */
    private BLangSimpleVarRef defineNilFrameForType(List<BVarSymbol> symbols, BLangBlockStmt blockStmt,
                                                    Location pos) {
        BLangSimpleVarRef frame = defineFrameVariable(blockStmt, pos);
        for (BVarSymbol symbol : symbols) {
            BType type = symbol.type;
            String key = symbol.name.value;
            BType structureType = Types.getReferredType(type);
            if (structureType.tag == TypeTags.RECORD || structureType.tag == TypeTags.OBJECT) {
                List<BVarSymbol> nestedSymbols = new ArrayList<>();
                for (BField field : ((BStructureType) structureType).fields.values()) {
                    nestedSymbols.add(field.symbol);
                }
                addFrameValueToFrame(frame, key, defineNilFrameForType(nestedSymbols, blockStmt, pos), blockStmt, pos);
            } else {
                addNilValueToFrame(frame, key, blockStmt, pos);
            }
        }
        return frame;
    }

    /**
     * Adds nil value fields to a given _Frame.
     *
     * @param frameToAddValueTo _Frame to add nil values to.
     * @param key               field name.
     * @param blockStmt         parent block to write to.
     * @param pos               diagnostic position.
     */
    private void addNilValueToFrame(BLangSimpleVarRef frameToAddValueTo, String key,
                                    BLangBlockStmt blockStmt, Location pos) {
        BLangStatement addToFrameStmt = getAddToFrameStmt(pos, frameToAddValueTo, key,
                ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE));
        blockStmt.addStatement(addToFrameStmt);
    }

    /**
     * Adds _Frame value fields to a given _Frame.
     *
     * @param frameToAddValueTo _Frame to add values to.
     * @param key               field name.
     * @param frameValue        frame value to be added.
     * @param blockStmt         parent block to write to.
     * @param pos               diagnostic position.
     */
    private void addFrameValueToFrame(BLangSimpleVarRef frameToAddValueTo, String key,
                                      BLangSimpleVarRef frameValue, BLangBlockStmt blockStmt,
                                      Location pos) {
        BLangStatement addToFrameStmt = getAddToFrameStmt(pos, frameToAddValueTo, key, frameValue);
        blockStmt.addStatement(addToFrameStmt);
    }

    /**
     * Creates _Frame $frame$ = new; variable definition and return a reference to the created frame.
     *
     * @param pos diagnostic position.
     * @return reference to the defined frame.
     */
    private BLangSimpleVarRef defineFrameVariable(BLangBlockStmt blockStmt, Location pos) {
        BSymbol frameTypeSymbol = getFrameTypeSymbol();
        BRecordType frameType = (BRecordType) frameTypeSymbol.type;
        String frameName = getNewVarName();
        BVarSymbol frameSymbol = new BVarSymbol(0, Names.fromString(frameName),
                env.scope.owner.pkgID, frameType, this.env.scope.owner, pos, VIRTUAL);
        BLangRecordLiteral frameInit = ASTBuilderUtil.createEmptyRecordLiteral(pos, frameType);
        BLangSimpleVariable frameVariable = ASTBuilderUtil.createVariable(
                pos, frameName, frameType, frameInit, frameSymbol);
        blockStmt.addStatement(ASTBuilderUtil.createVariableDef(pos, frameVariable));
        return ASTBuilderUtil.createVariableRef(pos, frameSymbol);
    }

    /**
     * Return BLangValueType of a nil `()` type.
     *
     * @return a nil type node.
     */
    BLangValueType getNilTypeNode() {
        BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nilTypeNode.typeKind = TypeKind.NIL;
        nilTypeNode.setBType(symTable.nilType);
        return nilTypeNode;
    }

    /**
     * Return BLangValueType of a any type.
     *
     * @return a any type node.
     */
    BLangValueType getAnyTypeNode() {
        BLangValueType anyTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        anyTypeNode.typeKind = TypeKind.ANY;
        anyTypeNode.setBType(symTable.anyType);
        return anyTypeNode;
    }

    /**
     * Return BLangValueType of a int type.
     *
     * @return a int type node.
     */
    BLangValueType getIntTypeNode() {
        BLangValueType intTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        intTypeNode.typeKind = TypeKind.INT;
        intTypeNode.setBType(symTable.intType);
        return intTypeNode;
    }

    /**
     * Return BLangErrorType node.
     *
     * @return a error type node.
     */
    BLangErrorType getErrorTypeNode() {
        BLangErrorType errorTypeNode = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        errorTypeNode.setBType(symTable.errorType);
        return errorTypeNode;
    }

    /**
     * Return BLangValueType of a boolean type.
     *
     * @return a boolean type node.
     */
    private BLangValueType getBooleanTypeNode() {
        BLangValueType booleanTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        booleanTypeNode.typeKind = TypeKind.BOOLEAN;
        booleanTypeNode.setBType(symTable.booleanType);
        return booleanTypeNode;
    }

    /**
     * Return union type node consists of _Frame & error & ().
     *
     * @return a union type node.
     */
    private BLangUnionTypeNode getFrameErrorNilTypeNode() {
        BType frameType = getFrameTypeSymbol().type;
        BUnionType unionType = BUnionType.create(null, frameType, symTable.errorType, symTable.nilType);
        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        unionTypeNode.setBType(unionType);
        unionTypeNode.memberTypeNodes.add(getFrameTypeNode());
        unionTypeNode.memberTypeNodes.add(getErrorTypeNode());
        unionTypeNode.memberTypeNodes.add(getNilTypeNode());
        unionTypeNode.desugared = true;
        return unionTypeNode;
    }

    private BLangUnionTypeNode getBooleanErrorTypeNode() {
        BUnionType unionType = BUnionType.create(null, symTable.errorType, symTable.booleanType);
        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        unionTypeNode.setBType(unionType);
        unionTypeNode.memberTypeNodes.add(getErrorTypeNode());
        unionTypeNode.memberTypeNodes.add(getBooleanTypeNode());
        unionTypeNode.desugared = true;
        return unionTypeNode;
    }

    private BLangUnionTypeNode getIntErrorTypeNode() {
        BUnionType unionType = BUnionType.create(null, symTable.errorType, symTable.intType);
        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        unionTypeNode.setBType(unionType);
        unionTypeNode.memberTypeNodes.add(getErrorTypeNode());
        unionTypeNode.memberTypeNodes.add(getIntTypeNode());
        unionTypeNode.desugared = true;
        return unionTypeNode;
    }

    /**
     * Return union type node consists of any & error.
     *
     * @return a any & error type node.
     */
    private BLangUnionTypeNode getAnyAndErrorTypeNode() {
        BUnionType unionType = BUnionType.create(null, symTable.anyType, symTable.errorType);
        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        unionTypeNode.memberTypeNodes.add(getAnyTypeNode());
        unionTypeNode.memberTypeNodes.add(getErrorTypeNode());
        unionTypeNode.setBType(unionType);
        unionTypeNode.desugared = true;
        return unionTypeNode;
    }

    /**
     * Return _Frame type node.
     *
     * @return a _Frame type node.
     */
    private BLangRecordTypeNode getFrameTypeNode() {
        BSymbol frameTypeSymbol = getFrameTypeSymbol();
        BRecordType frameType = (BRecordType) frameTypeSymbol.type;

        BLangUnionTypeNode restFieldType = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        restFieldType.setBType(frameType.restFieldType);
        restFieldType.memberTypeNodes.add(getErrorTypeNode());
        restFieldType.memberTypeNodes.add(getAnyTypeNode());

        BLangRecordTypeNode frameTypeNode = (BLangRecordTypeNode) TreeBuilder.createRecordTypeNode();
        frameTypeNode.setBType(frameType);
        frameTypeNode.restFieldType = restFieldType;
        frameTypeNode.symbol = frameType.tsymbol;
        frameTypeNode.desugared = true;
        return frameTypeNode;
    }

    /**
     * Load and return symbol for _Frame.
     *
     * @return _Frame type symbol.
     */
    private BSymbol getFrameTypeSymbol() {
        return symTable.langQueryModuleSymbol
                .scope.lookup(Names.fromString("_Frame")).symbol;
    }

    // ---- Visitor methods to replace frame access and mark closure variables ---- //
    @Override
    public void visit(BLangLambdaFunction lambda) {
        lambda.function.accept(this);
        lambda.function = desugar.rewrite(lambda.function, lambda.capturedClosureEnv);
    }

    @Override
    public void visit(BLangFunction function) {
        if (function.flagSet.contains(Flag.QUERY_LAMBDA)) {
            BLangBlockFunctionBody prevQueryLambdaBody = currentQueryLambdaBody;
            BVarSymbol prevFrameSymbol = currentFrameSymbol;
            Map<String, BSymbol> prevIdentifiers = identifiers;
            currentFrameSymbol = function.requiredParams.get(0).symbol;
            identifiers = new HashMap<>();
            currentQueryLambdaBody = (BLangBlockFunctionBody) function.getBody();
            currentQueryLambdaBody.accept(this);
            currentFrameSymbol = prevFrameSymbol;
            identifiers = prevIdentifiers;
            currentQueryLambdaBody = prevQueryLambdaBody;
        } else {
            boolean prevWithinLambdaFunc = withinLambdaFunc;
            withinLambdaFunc = true;
            function.getBody().accept(this);
            withinLambdaFunc = prevWithinLambdaFunc;
        }
    }

    @Override
    public void visit(BLangBlockFunctionBody body) {
        List<BLangStatement> stmts = new ArrayList<>(body.getStatements());
        stmts.forEach(stmt -> stmt.accept(this));
    }

    @Override
    public void visit(BLangExprFunctionBody exprBody) {
        exprBody.expr.accept(this);
    }

    @Override
    public void visit(BLangSimpleVariableDef bLangSimpleVariableDef) {
        bLangSimpleVariableDef.getVariable().accept(this);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        bLangRecordVariableDef.var.accept(this);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        bLangRecordVariable.variableList.forEach(v -> v.getValue().accept(this));
        this.acceptNode(bLangRecordVariable.expr);
        if (bLangRecordVariable.hasRestParam()) {
            bLangRecordVariable.restParam.accept(this);
        }
    }

    @Override
    public void visit(BLangSimpleVariable bLangSimpleVariable) {
        identifiers.putIfAbsent(bLangSimpleVariable.name.value, bLangSimpleVariable.symbol);
        this.acceptNode(bLangSimpleVariable.expr);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        fieldAccessExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess nsPrefixedFieldBasedAccess) {
        nsPrefixedFieldBasedAccess.expr.accept(this);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef structFunctionVarRef) {
        structFunctionVarRef.expr.accept(this);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr.accept(this);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        List<BLangExpression> requiredArgs = invocationExpr.requiredArgs;
        if (invocationExpr.langLibInvocation && !requiredArgs.isEmpty()) {
            requiredArgs = requiredArgs.subList(1, requiredArgs.size());
        }
        requiredArgs.forEach(this::acceptNode);
        invocationExpr.restArgs.forEach(this::acceptNode);
        this.acceptNode(invocationExpr.expr);
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation functionPointerInvocationExpr) {
        visit((BLangInvocation) functionPointerInvocationExpr);
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation attachedFunctionInvocation) {
        visit((BLangInvocation) attachedFunctionInvocation);
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        // do nothing;
    }

    @Override
    public void visit(BLangReturn bLangReturn) {
        this.acceptNode(bLangReturn.expr);
    }

    @Override
    public void visit(BLangBinaryExpr bLangBinaryExpr) {
        this.acceptNode(bLangBinaryExpr.lhsExpr);
        this.acceptNode(bLangBinaryExpr.rhsExpr);
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
    }

    @Override
    public void visit(BLangAssignment bLangAssignment) {
        this.acceptNode(bLangAssignment.varRef);
        this.acceptNode(bLangAssignment.expr);
    }

    @Override
    public void visit(BLangRecordLiteral bLangRecordLiteral) {
        bLangRecordLiteral.fields.forEach(field -> this.acceptNode((BLangNode) field));
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        visit((BLangRecordLiteral) structLiteral);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        visit((BLangRecordLiteral) mapLiteral);
    }

    @Override
    public void visit(BLangRecordKeyValueField recordKeyValue) {
        this.acceptNode(recordKeyValue.key.expr);
        this.acceptNode(recordKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangRecordSpreadOperatorField spreadOperatorField) {
        this.acceptNode(spreadOperatorField.expr);
    }

    @Override
    public void visit(BLangConstRef constRef) {
        //do nothing
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        //do nothing
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        varRefExpr.expressions.forEach(this::acceptNode);
        this.acceptNode((BLangNode) varRefExpr.restParam);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        varRefExpr.recordRefFields.forEach(recordVarRefKeyValue
                -> this.acceptNode(recordVarRefKeyValue.variableReference));
        this.acceptNode((BLangNode) varRefExpr.restParam);
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        this.acceptNode(varRefExpr.message);
        this.acceptNode(varRefExpr.restVar);
        varRefExpr.detail.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangSimpleVarRef bLangSimpleVarRef) {
        BSymbol symbol = bLangSimpleVarRef.symbol;
        String identifier = bLangSimpleVarRef.variableName == null ? String.valueOf(bLangSimpleVarRef.varSymbol.name) :
                String.valueOf(bLangSimpleVarRef.variableName);
        BSymbol resolvedSymbol = symResolver.lookupClosureVarSymbol(env,
                Names.fromString(identifier), SymTag.VARIABLE);
        // check whether the symbol and resolved symbol are the same.
        // because, lookup using name produce unexpected results if there's variable shadowing.
        if (symbol != null && symbol != resolvedSymbol && !FRAME_PARAMETER_NAME.equals(identifier)) {
            if (symbol instanceof BVarSymbol) {
                BVarSymbol originalSymbol = ((BVarSymbol) symbol).originalSymbol;
                if (originalSymbol != null) {
                    symbol = originalSymbol;
                }
            }
            if ((withinLambdaFunc || queryEnv == null || !queryEnv.scope.entries.containsKey(symbol.name))
                    && !identifiers.containsKey(identifier)) {
                Location pos = currentQueryLambdaBody.pos;
                BLangFieldBasedAccess frameAccessExpr = desugar.getFieldAccessExpression(pos, identifier,
                        symTable.anyOrErrorType, currentFrameSymbol);
                frameAccessExpr.expr = desugar.addConversionExprIfRequired(frameAccessExpr.expr,
                        types.getSafeType(frameAccessExpr.expr.getBType(), true, false));

                if (symbol instanceof BVarSymbol) {
                    ((BVarSymbol) symbol).originalSymbol = null;
                    if (withinLambdaFunc) {
                        if (symbol.closure) {
                            // When there's a closure in a lambda inside a query lambda the symbol.closure is
                            // true for all its usages. Therefore mark symbol.closure = false for the existing
                            // symbol and create a new symbol with the same properties.
                            symbol.closure = false;
                            symbol = new BVarSymbol(0, symbol.name, env.scope.owner.pkgID, symbol.type,
                                                    env.scope.owner, pos, VIRTUAL);
                            symbol.closure = true;
                            bLangSimpleVarRef.symbol = symbol;
                            bLangSimpleVarRef.varSymbol = symbol;
                            BLangSimpleVariable variable = ASTBuilderUtil.createVariable(pos, identifier, symbol.type,
                                                           desugar.addConversionExprIfRequired(frameAccessExpr,
                                                                   symbol.type), (BVarSymbol) symbol);
                            BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDef(pos, variable);
                            currentQueryLambdaBody.stmts.add(0, variableDef);
                            SymbolEnv queryLambdaEnv = SymbolEnv.createFuncBodyEnv(currentQueryLambdaBody, env);
                            queryLambdaEnv.scope.define(symbol.name, symbol);
                        }
                    } else {
                        BLangSimpleVariable variable = ASTBuilderUtil.createVariable(pos, identifier, symbol.type,
                                desugar.addConversionExprIfRequired(frameAccessExpr, symbol.type), (BVarSymbol) symbol);
                        BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDef(pos, variable);
                        currentQueryLambdaBody.stmts.add(0, variableDef);
                        SymbolEnv queryLambdaEnv = SymbolEnv.createFuncBodyEnv(currentQueryLambdaBody, env);
                        queryLambdaEnv.scope.define(symbol.name, symbol);
                    }
                }
                identifiers.put(identifier, symbol);
            } else if (identifiers.containsKey(identifier) && withinLambdaFunc) {
                symbol = identifiers.get(identifier);
                bLangSimpleVarRef.symbol = symbol;
                bLangSimpleVarRef.varSymbol = symbol;
            }
        } else if (resolvedSymbol != symTable.notFoundSymbol && symbol != null) {
            resolvedSymbol.closure = true;
            // When there's a type guard, there can be a enclSymbol before type narrowing.
            // So, we have to mark that as a closure as well.
            BSymbol enclSymbol = symResolver.lookupClosureVarSymbol(env.enclEnv,
                    Names.fromString(identifier), SymTag.VARIABLE);
            if (enclSymbol != null && enclSymbol != symTable.notFoundSymbol) {
                enclSymbol.closure = true;
            }
        }
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef bLangPackageVarRef) {
        visit((BLangSimpleVarRef) bLangPackageVarRef);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        visit(((BLangSimpleVarRef) localVarRef));
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        visit(((BLangSimpleVarRef) fieldVarRef));
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        visit(((BLangSimpleVarRef) functionVarRef));
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.indexExpr.accept(this);
        indexAccessExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr structFieldAccessExpr) {
        visit((BLangIndexBasedAccess) structFieldAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapAccessExpr) {
        visit((BLangIndexBasedAccess) mapAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayAccessExpr) {
        visit((BLangIndexBasedAccess) arrayAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableAccessExpr) {
        visit((BLangIndexBasedAccess) tableAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr tupleAccessExpr) {
        visit((BLangIndexBasedAccess) tupleAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        visit((BLangIndexBasedAccess) stringAccessExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {
        visit((BLangIndexBasedAccess) xmlAccessExpr);
    }

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
        connectorInitExpr.argsExpr.forEach(this::acceptNode);
        connectorInitExpr.initInvocation.accept(this);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        actionInvocationExpr.argExprs.forEach(this::acceptNode);
        this.acceptNode(actionInvocationExpr.expr);
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        this.acceptNode(errorConstructorExpr.errorTypeRef);
        if (errorConstructorExpr.namedArgs != null) {
            errorConstructorExpr.namedArgs.forEach(this::acceptNode);
        }
        this.acceptNode(errorConstructorExpr.errorDetail);
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.expr.accept(this);
        ternaryExpr.elseExpr.accept(this);
        ternaryExpr.thenExpr.accept(this);
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        awaitExpr.exprList.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        this.acceptNode(trapExpr.expr);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        this.acceptNode(elvisExpr.lhsExpr);
        this.acceptNode(elvisExpr.rhsExpr);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        this.acceptNode(groupExpr.expression);
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        this.acceptNode(letExpr.expr);
        letExpr.letVarDeclarations.forEach(var -> this.acceptNode((BLangNode) var.definitionNode));
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        listConstructorExpr.exprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangListConstructorSpreadOpExpr spreadOpExpr) {
        this.acceptNode(spreadOpExpr.expr);
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        tupleLiteral.exprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        jsonArrayLiteral.exprs.forEach(expression -> expression.accept(this));
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        arrayLiteral.exprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        this.acceptNode(unaryExpr.expr);
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        this.acceptNode(xmlAttribute.name);
        this.acceptNode(xmlAttribute.value);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        this.acceptNode(xmlElementLiteral.startTagName);
        this.acceptNode(xmlElementLiteral.endTagName);
        xmlElementLiteral.attributes.forEach(this::acceptNode);
        xmlElementLiteral.children.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.textFragments.forEach(this::acceptNode);
        this.acceptNode(xmlTextLiteral.concatExpr);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.textFragments.forEach(this::acceptNode);
        this.acceptNode(xmlCommentLiteral.concatExpr);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.dataFragments.forEach(this::acceptNode);
        this.acceptNode(xmlProcInsLiteral.dataConcatExpr);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.textFragments.forEach(this::acceptNode);
        this.acceptNode(xmlQuotedString.concatExpr);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.exprs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        rawTemplateLiteral.strings.forEach(this::acceptNode);
        rawTemplateLiteral.insertions.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        bLangArrowFunction.params.forEach(this::acceptNode);
        this.acceptNode(bLangArrowFunction.body);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        this.acceptNode(bLangVarArgsExpression.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        this.acceptNode(bLangNamedArgsExpression.expr);
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        this.acceptNode(assignableExpr.lhsExpr);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        containsCheckExpr = true;
        if (this.checkedErrorList != null && checkedExpr.equivalentErrorTypeList != null) {
            this.checkedErrorList.addAll(checkedExpr.equivalentErrorTypeList);
        }
        this.acceptNode(checkedExpr.expr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        this.acceptNode(checkPanickedExpr.expr);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        this.acceptNode(serviceConstructorExpr.serviceNode);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        this.acceptNode(typeTestExpr.expr);
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        this.acceptNode(typeTestExpr.expr);
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        bLangXMLSequenceLiteral.xmlItems.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        this.acceptNode(bLangStatementExpression.expr);
        this.acceptNode(bLangStatementExpression.stmt);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        this.acceptNode(bLangTupleVariable.restVariable);
        bLangTupleVariable.memberVariables.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        this.acceptNode(bLangTupleVariableDef.var.restVariable);
        this.acceptNode(bLangTupleVariableDef.var.expr);
        if (bLangTupleVariableDef.var.memberVariables != null) {
            bLangTupleVariableDef.var.memberVariables.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        this.acceptNode(bLangErrorVariable.message);
        bLangErrorVariable.detail.forEach(var -> this.acceptNode(var.valueBindingPattern));
        this.acceptNode(bLangErrorVariable.restDetail);
        this.acceptNode(bLangErrorVariable.detailExpr);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        this.acceptNode(bLangErrorVariableDef.errorVariable);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        waitForAllExpr.keyValuePairs.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
    }

    @Override
    public void visit(BLangMarkdownReferenceDocumentation bLangMarkdownReferenceDocumentation) {
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        this.acceptNode(waitKeyValue.key);
        this.acceptNode(waitKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangXMLElementFilter xmlElementFilter) {
        this.acceptNode(xmlElementFilter.impConversionExpr);
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        this.acceptNode(xmlElementAccess.expr);
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        this.acceptNode(xmlNavigation.expr);
        this.acceptNode(xmlNavigation.childIndex);
    }

    //statements
    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.stmts.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        this.acceptNode(compoundAssignNode.expr);
        this.acceptNode(compoundAssignNode.modifiedExpr);
        this.acceptNode(compoundAssignNode.varRef);
    }

    @Override
    public void visit(BLangRetry retryNode) {
    }

    @Override
    public void visit(BLangContinue continueNode) {
    }

    @Override
    public void visit(BLangBreak breakNode) {
    }

    @Override
    public void visit(BLangPanic panicNode) {
        this.acceptNode(panicNode.expr);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        this.acceptNode(xmlnsStmtNode.xmlnsDecl);
    }

    @Override
    public void visit(BLangIf ifNode) {
        this.acceptNode(ifNode.expr);
        this.acceptNode(ifNode.body);
        this.acceptNode(ifNode.elseStmt);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        SymbolEnv prevQueryEnv = this.queryEnv;
        queryAction.getQueryClauses().forEach(this::acceptNode);
        this.queryEnv = prevQueryEnv;
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        SymbolEnv prevQueryEnv = this.queryEnv;
        queryExpr.getQueryClauses().forEach(this::acceptNode);
        this.queryEnv = prevQueryEnv;
    }

    @Override
    public void visit(BLangForeach foreach) {
        this.acceptNode(foreach.collection);
        this.acceptNode(foreach.body);
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        this.queryEnv = fromClause.env;
        this.acceptNode(fromClause.collection);
        //we don't have to reset the env to the prev env because from clause is the init clause for the query
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        this.acceptNode(joinClause.collection);
        joinClause.collection.accept(this);
        this.acceptNode((BLangNode) joinClause.onClause.getLeftExpression());
        this.acceptNode((BLangNode) joinClause.onClause.getRightExpression());
    }

    @Override
    public void visit(BLangLetClause letClause) {
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        this.acceptNode(selectClause.expression);
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        this.acceptNode(whereClause.expression);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        doClause.body.getStatements().forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        this.acceptNode(onConflictClause.expression);
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        this.acceptNode(limitClause.expression);
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        orderByClause.orderByKeyList.forEach(key -> this.acceptNode(((BLangOrderKey) key).expression));
    }

    @Override
    public void visit(BLangWhile whileNode) {
        this.acceptNode(whileNode.expr);
        this.acceptNode(whileNode.body);
    }

    @Override
    public void visit(BLangDo doNode) {
        doNode.body.stmts.forEach(this::acceptNode);
        this.acceptNode(doNode.onFailClause);
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        onFailClause.body.stmts.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangFail failNode) {
        this.acceptNode(failNode.expr);
    }

    @Override
    public void visit(BLangLock lockNode) {
        this.acceptNode(lockNode.body);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        this.acceptNode(transactionNode.transactionBody);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        this.acceptNode(stmt.varRef);
        this.acceptNode(stmt.expr);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        this.acceptNode(stmt.expr);
        this.acceptNode(stmt.varRef);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        this.acceptNode(stmt.expr);
        this.acceptNode(stmt.varRef);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers.forEach(this::acceptNode);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        this.acceptNode(workerSendNode.expr);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        this.acceptNode(workerReceiveNode.sendExpression);
    }

    @Override
    public void visit(BLangInvocation.BLangResourceAccessInvocation resourceAccessInvocation) {
        resourceAccessInvocation.argExprs.forEach(this::acceptNode);
        this.acceptNode(resourceAccessInvocation.expr);
    }

    @Override
    public void visit(BLangRegExpTemplateLiteral regExpTemplateLiteral) {
        List<BLangExpression> interpolationsList =
                symResolver.getListOfInterpolations(regExpTemplateLiteral.reDisjunction.sequenceList);
        interpolationsList.forEach(this::acceptNode);
    }

    private void acceptNode(BLangNode node) {
        if (node == null) {
            return;
        }
        node.accept(this);
    }
}

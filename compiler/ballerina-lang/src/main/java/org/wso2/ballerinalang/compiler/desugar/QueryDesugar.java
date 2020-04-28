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

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordSpreadOperatorField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordVarNameField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for desugar query pipeline into actual Ballerina code.
 *
 * @since 1.2.0
 */
public class QueryDesugar extends BLangNodeVisitor {
    public static final Name QUERY_CREATE_PIPELINE_FUNCTION = new Name("createPipeline");
    public static final Name QUERY_CREATE_FROM_FUNCTION = new Name("createFromFunction");
    public static final Name QUERY_CREATE_LET_FUNCTION = new Name("createLetFunction");
    public static final Name QUERY_CREATE_JOIN_FUNCTION = new Name("createJoinFunction");
    public static final Name QUERY_CREATE_FILTER_FUNCTION = new Name("createFilterFunction");
    public static final Name QUERY_CREATE_SELECT_FUNCTION = new Name("createSelectFunction");
    public static final Name QUERY_CREATE_DO_FUNCTION = new Name("createDoFunction");
    public static final Name QUERY_ADD_STREAM_FUNCTION = new Name("addStreamFunction");
    public static final Name QUERY_ADD_TO_FRAME_FUNCTION = new Name("addToFrame");
    public static final Name QUERY_SPREAD_TO_FRAME_FUNCTION = new Name("spreadToFrame");
    public static final Name QUERY_CONSUME_STREAM_FUNCTION = new Name("consumeStream");
    public static final Name QUERY_GET_STREAM_FROM_PIPELINE_FUNCTION = new Name("getStreamFromPipeline");
    public static final String FRAME_PARAMETER_NAME = "frame";
    private static final CompilerContext.Key<QueryDesugar> QUERY_DESUGAR_KEY = new CompilerContext.Key<>();
    private final SymbolEnter symbolEnter;
    private final Desugar desugar;
    private final SymbolTable symTable;
    private final BLangAnonymousModelHelper anonymousModelHelper;
    private BLangDiagnosticLogHelper dlog;
    private final SymbolResolver symResolver;
    private final Names names;
    private final Types types;
    private int streamElementCount = 0;
    private SymbolEnv env;

    private QueryDesugar(CompilerContext context) {
        context.put(QUERY_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
    }

    public static QueryDesugar getInstance(CompilerContext context) {
        QueryDesugar desugar = context.get(QUERY_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new QueryDesugar(context);
        }
        return desugar;
    }

    BLangStatementExpression desugar(BLangQueryExpr queryExpr, SymbolEnv env) {
        List<BLangNode> clauses = queryExpr.getQueryClauses();
        BLangBlockStmt queryBlock = ASTBuilderUtil.createBlockStmt(clauses.get(0).pos);
        BLangVariableReference streamRef = buildStream(clauses, env, queryBlock);

        //        // TODO
        //        if (queryExpr.isStream) {
        //            // addGetStreamFromPipeline(queryBlock, initPipeline);
        //        } else {
        //            // to array?
        //        }
        //        // --- end new stream desugar ---

        BLangStatementExpression streamStmtExpr = ASTBuilderUtil.createStatementExpression(queryBlock, streamRef);
        streamStmtExpr.type = streamRef.type;
        return streamStmtExpr;
    }

    BLangStatementExpression desugar(BLangQueryAction queryAction, SymbolEnv env) {
        List<BLangNode> clauses = queryAction.getQueryClauses();
        DiagnosticPos pos = clauses.get(0).pos;
        BLangBlockStmt queryBlock = ASTBuilderUtil.createBlockStmt(pos);
        BLangVariableReference streamRef = buildStream(clauses, env, queryBlock);
        BLangVariableReference result = getStreamFunctionVariableRef(queryBlock,
                QUERY_CONSUME_STREAM_FUNCTION, symTable.errorOrNilType, Lists.of(streamRef), pos);
        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(queryBlock, result);
        stmtExpr.type = symTable.errorOrNilType;
        return stmtExpr;
    }

    /**
     * Write the pipeline to the given `block` and return the reference to the resulting stream.
     *
     * @param clauses
     * @param env
     * @param block
     * @return
     */
    BLangVariableReference buildStream(List<BLangNode> clauses, SymbolEnv env, BLangBlockStmt block) {
        this.env = env;
        BLangNode initFromClause = clauses.get(0);
        final BLangVariableReference initPipeline = addPipeline(block, (BLangFromClause) initFromClause);
        BLangVariableReference initFrom = addFromFunction(block, (BLangFromClause) initFromClause);
        addStreamFunction(block, initPipeline, initFrom);
        for (BLangNode clause : clauses.subList(1, clauses.size())) {
            switch (clause.getKind()) {
                case FROM:
                    BLangVariableReference pipeline = addPipeline(block, (BLangFromClause) clause);
                    BLangVariableReference fromFunc = addFromFunction(block, (BLangFromClause) clause);
                    addStreamFunction(block, pipeline, fromFunc);
                    BLangVariableReference joinFunc = addJoinFunction(block, pipeline);
                    addStreamFunction(block, initPipeline, joinFunc);
                    break;
                case LET_CLAUSE:
                    BLangVariableReference letFunc = addLetFunction(block, (BLangLetClause) clause);
                    addStreamFunction(block, initPipeline, letFunc);
                    break;
                case WHERE:
                    BLangVariableReference filterFunc = addFilterFunction(block, (BLangWhereClause) clause);
                    addStreamFunction(block, initPipeline, filterFunc);
                    break;
                case SELECT:
                    BLangVariableReference selectFunc = addSelectFunction(block, (BLangSelectClause) clause);
                    addStreamFunction(block, initPipeline, selectFunc);
                    break;
                case DO:
                    BLangVariableReference doFunc = addDoFunction(block, (BLangDoClause) clause);
                    addStreamFunction(block, initPipeline, doFunc);
                    break;
            }
        }
        return addGetStreamFromPipeline(block, initPipeline);
    }

    /**
     * Desugar fromClause/joinClause to below and return a reference to created join _StreamPipeline.
     * _StreamPipeline pipeline = createPipeline(collection);
     *
     * @param blockStmt  parent block to write to.
     * @param fromClause to init pipeline.
     * @return variableReference to created _StreamPipeline.
     */
    BLangVariableReference addPipeline(BLangBlockStmt blockStmt, BLangFromClause fromClause) {
        BLangExpression collection = fromClause.collection;
        DiagnosticPos pos = fromClause.pos;
        String name = getNewVarName();
        BVarSymbol dataSymbol = new BVarSymbol(0, names.fromString(name), env.scope.owner.pkgID,
                collection.type, this.env.scope.owner);
        BLangSimpleVariable dataVariable = ASTBuilderUtil.createVariable(fromClause.pos, name,
                collection.type, collection, dataSymbol);
        BLangSimpleVariableDef dataVarDef = ASTBuilderUtil.createVariableDef(fromClause.pos, dataVariable);
        BLangVariableReference valueVarRef = ASTBuilderUtil.createVariableRef(pos, dataSymbol);
        blockStmt.addStatement(dataVarDef);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_PIPELINE_FUNCTION,
                Lists.of(valueVarRef), fromClause.pos);
    }

    /**
     * Desugar fromClause to below and return a reference to created from _StreamFunction.
     * _StreamFunction xsFrom = createFromFunction(function(_Frame frame) returns _Frame|error? {
     * int x = <int> frame["value"];
     * frame["x"] = x;
     * return frame;
     * });
     *
     * @param blockStmt  parent block to write to.
     * @param fromClause to be desugared.
     * @return variableReference to created from _StreamFunction.
     */
    BLangVariableReference addFromFunction(BLangBlockStmt blockStmt, BLangFromClause fromClause) {
        DiagnosticPos pos = fromClause.pos;
        // function(_Frame frame) returns _Frame|error? { return frame; }
        BLangLambdaFunction lambda = createPassthroughLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        BVarSymbol frameSymbol = lambda.function.requiredParams.get(0).symbol;

        // frame["x"] = x;, note: stmts will get added in reverse order.
        List<BVarSymbol> symbols = getIntroducedSymbols((BLangVariable)
                fromClause.variableDefinitionNode.getVariable());
        Collections.reverse(symbols);
        for (BVarSymbol symbol : symbols) {
            body.stmts.add(0, addToFrameFunctionStmt(pos, frameSymbol, symbol));
        }

        // int x = <int> frame["value"];, note: stmts will get added in reverse order.
        BLangFieldBasedAccess valueAccessExpr = desugar.getValueAccessExpression(fromClause.pos,
                symTable.anyOrErrorType, frameSymbol);
        valueAccessExpr.expr = desugar.addConversionExprIfRequired(valueAccessExpr.expr,
                types.getSafeType(valueAccessExpr.expr.type, true, false));
        VariableDefinitionNode variableDefinitionNode = fromClause.variableDefinitionNode;
        BLangVariable variable = (BLangVariable) variableDefinitionNode.getVariable();
        variable.setInitialExpression(desugar.addConversionExprIfRequired(valueAccessExpr, fromClause.varType));
        // add at 0, otherwise, this goes under existing stmts.
        body.stmts.add(0, (BLangStatement) variableDefinitionNode);

        // at this point;
        // function(_Frame frame) returns _Frame|error? {
        //      int x = <int> frame["value"];
        //      frame["x"] = x;
        //      return frame;
        // }
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_FROM_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar joinClauses / nested fromClauses to below and return a reference to created join _StreamFunction.
     * _StreamFunction joinFunc = createJoinFunction(joinPipeline);
     *
     * @param blockStmt    parent block to write to.
     * @param joinPipeline previously created _StreamPipeline reference to be joined.
     * @return variableReference to created join _StreamFunction.
     */
    BLangVariableReference addJoinFunction(BLangBlockStmt blockStmt, BLangVariableReference joinPipeline) {
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_JOIN_FUNCTION,
                Lists.of(joinPipeline), joinPipeline.pos);
    }

    /**
     * Desugar letClause to below and return a reference to created let _StreamFunction.
     * _StreamFunction ysLet = createLetFunction(function(_Frame frame) returns _Frame|error? {
     * frame["y2"] = <int> frame["y"] * <int> frame["y"];
     * return frame;
     * });
     *
     * @param blockStmt parent block to write to.
     * @param letClause to be desugared.
     * @return variableReference to created let _StreamFunction.
     */
    BLangVariableReference addLetFunction(BLangBlockStmt blockStmt, BLangLetClause letClause) {
        DiagnosticPos pos = letClause.pos;
        // function(_Frame frame) returns _Frame|error? { return frame; }
        BLangLambdaFunction lambda = createPassthroughLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        BVarSymbol frameSymbol = lambda.function.requiredParams.get(0).symbol;

        // frame["x"] = x;, note: stmts will get added in reverse order.
        List<BVarSymbol> symbols = getIntroducedSymbols(letClause);
        Collections.reverse(symbols);
        for (BVarSymbol symbol : symbols) {
            body.stmts.add(0, addToFrameFunctionStmt(pos, frameSymbol, symbol));
        }

        for (BLangLetVariable letVariable : letClause.letVarDeclarations) {
            // add at 0, otherwise, this goes under existing stmts.
            body.stmts.add(0, (BLangStatement) letVariable.definitionNode);
        }
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_LET_FUNCTION, Lists.of(lambda), pos);
    }

    /**
     * Desugar whereClause to below and return a reference to created filter _StreamFunction.
     * _StreamFunction xsFilter = createFilterFunction(function(_Frame frame) returns boolean {
     * return <int>frame["x"] > 0;
     * });
     *
     * @param blockStmt   parent block to write to.
     * @param whereClause to be desugared.
     * @return variableReference to created filter _StreamFunction.
     */
    BLangVariableReference addFilterFunction(BLangBlockStmt blockStmt, BLangWhereClause whereClause) {
        DiagnosticPos pos = whereClause.pos;
        BLangLambdaFunction lambda = createFilterLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        BLangReturn returnNode = (BLangReturn) TreeBuilder.createReturnNode();
        returnNode.pos = pos;
        returnNode.setExpression(whereClause.expression);
        body.addStatement(returnNode);
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_FILTER_FUNCTION, Lists.of(lambda), pos);
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
     * @param blockStmt    parent block to write to.
     * @param selectClause to be desugared.
     * @return variableReference to created select _StreamFunction.
     */
    BLangVariableReference addSelectFunction(BLangBlockStmt blockStmt, BLangSelectClause selectClause) {
        DiagnosticPos pos = selectClause.pos;
        BLangLambdaFunction lambda = createPassthroughLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        BVarSymbol oldFrameSymbol = lambda.function.requiredParams.get(0).symbol;
        BLangSimpleVarRef oldFrameRef = ASTBuilderUtil.createVariableRef(pos, oldFrameSymbol);
        BLangSimpleVariableDef newFrameDef = getNewFrameDef(pos);
        BVarSymbol newFrameSymbol = newFrameDef.getVariable().symbol;
        BLangSimpleVarRef newFrameRef = ASTBuilderUtil.createVariableRef(pos, newFrameSymbol);
        // Frame $streamElement1$ = {};
        body.stmts.add(0, newFrameDef);
        // addToFrame($streamElement1$, elements);
        BLangRecordLiteral selectRecord = (BLangRecordLiteral) selectClause.expression;
        for (RecordLiteralNode.RecordField field : selectRecord.fields) {
            BLangExpressionStmt addToFrameStmt;
            if (field.getKind() == NodeKind.RECORD_LITERAL_KEY_VALUE) {
                BLangRecordKeyValueField keyValField = (BLangRecordKeyValueField) field;
                // TODO: assuming key will only be a literal (no-other expr types).
                BLangLiteral key = ASTBuilderUtil.createLiteral(pos, symTable.stringType,
                        ((BLangSimpleVarRef) keyValField.key.expr).variableName.value);
                addToFrameStmt = addToFrameFunctionStmt(pos, newFrameRef, key, keyValField.valueExpr);
            } else if (field.getKind() == NodeKind.RECORD_LITERAL_SPREAD_OP) {
                BLangRecordSpreadOperatorField spreadField = (BLangRecordSpreadOperatorField) field;
                addToFrameStmt = spreadToFrameFunctionStmt(pos, newFrameRef, spreadField.expr);
            } else if (field.getKind() == NodeKind.CONSTANT_REF) {
                BLangConstRef constField = (BLangConstRef) field;
                BLangLiteral key = ASTBuilderUtil.createLiteral(pos, symTable.stringType,
                        constField.variableName.value);
                BLangLiteral value = ASTBuilderUtil.createLiteral(pos, constField.type, constField.value);
                addToFrameStmt = addToFrameFunctionStmt(pos, newFrameRef, key, value);
            } else {
                BLangRecordVarNameField nameField = (BLangRecordVarNameField) field;
                addToFrameStmt = addToFrameFunctionStmt(pos, newFrameSymbol, (BVarSymbol) nameField.varSymbol);
            }
            body.stmts.add(body.stmts.size() - 1, addToFrameStmt);
        }
        // frame = $streamElement1$ <- swap
        body.stmts.add(body.stmts.size() - 1, ASTBuilderUtil.createAssignmentStmt(pos, oldFrameRef, newFrameRef));
        // return frame; <- this comes from createPassthroughLambda()
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_SELECT_FUNCTION, Lists.of(lambda), pos);
    }

    BLangSimpleVariableDef getNewFrameDef(DiagnosticPos pos) {
        // Frame $streamElement1$ = {};
        String name = getNewVarName();
        BRecordTypeSymbol frameTypeSymbol = (BRecordTypeSymbol) symTable.langQueryModuleSymbol.scope
                .lookup(names.fromString("_Frame")).symbol;
        BRecordType frameType = (BRecordType) frameTypeSymbol.type;
        BVarSymbol frameVarSymbol = new BVarSymbol(0, names.fromString(name),
                env.scope.owner.pkgID, frameType, env.scope.owner);
        BLangRecordLiteral emptyFrameExpr = ASTBuilderUtil.createEmptyRecordLiteral(pos, frameType);
        BLangSimpleVariable frameVariable = ASTBuilderUtil.createVariable(pos, name, frameType,
                emptyFrameExpr, frameVarSymbol);
        return ASTBuilderUtil.createVariableDef(pos, frameVariable);
    }

    /**
     * Desugar doClause to below and return a reference to created do _StreamFunction.
     * _StreamFunction doFunc = createDoFunction(function(_Frame frame) {
     * int x2 = <int> frame["x2"];
     * int y2 = <int> frame["y2"];
     * });
     *
     * @param blockStmt parent block to write to.
     * @param doClause  to be desugared.
     * @return variableReference to created do _StreamFunction.
     */
    BLangVariableReference addDoFunction(BLangBlockStmt blockStmt, BLangDoClause doClause) {
        DiagnosticPos pos = doClause.pos;
        BLangLambdaFunction lambda = createActionLambda(pos);
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) lambda.function.body;
        for (BLangStatement stmt : doClause.body.stmts) {
            body.addStatement(stmt);
        }
        lambda.accept(this);
        return getStreamFunctionVariableRef(blockStmt, QUERY_CREATE_DO_FUNCTION, Lists.of(lambda), pos);
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
        DiagnosticPos pos = pipelineRef.pos;
        // TODO: instead of null, send the expected type;??
        // TODO: for now type will be stream<any|error, error?> ; we can pass the expected type and add a cast
        BLangVariableReference streamVarRef = getStreamFunctionVariableRef(blockStmt,
                QUERY_GET_STREAM_FROM_PIPELINE_FUNCTION, null, Lists.of(pipelineRef), pos);
        return streamVarRef;
    }

    private BVarSymbol currectFrameSymbol;
    private BLangBlockFunctionBody currectLambdaBody;
    private Map<String, BSymbol> identifiers;

    @Override
    public void visit(BLangLambdaFunction lambda) {
        BLangFunction function = lambda.function;
        currectFrameSymbol = function.requiredParams.get(0).symbol;
        identifiers = new HashMap<>();
        currectLambdaBody = (BLangBlockFunctionBody) function.getBody();
        List<BLangStatement> stmts = new ArrayList<>(currectLambdaBody.getStatements());
        stmts.forEach(stmt -> stmt.accept(this));
        currectFrameSymbol = null;
        identifiers = null;
        currectLambdaBody = null;
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
        bLangRecordVariable.variableList.forEach(v -> {
            BLangVariable variable = v.getValue();
            identifiers.putIfAbsent(variable.symbol.name.value, variable.symbol);
        });
    }

    @Override
    public void visit(BLangSimpleVariable bLangSimpleVariable) {
        identifiers.putIfAbsent(bLangSimpleVariable.name.value, bLangSimpleVariable.symbol);
        bLangSimpleVariable.expr.accept(this);
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
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr.accept(this);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        invocationExpr.requiredArgs.forEach(arg -> arg.accept(this));
        invocationExpr.restArgs.forEach(arg -> arg.accept(this));
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        // do nothing;
    }

    @Override
    public void visit(BLangReturn bLangReturn) {
        bLangReturn.expr.accept(this);
    }

    @Override
    public void visit(BLangBinaryExpr bLangBinaryExpr) {
        bLangBinaryExpr.lhsExpr.accept(this);
        bLangBinaryExpr.rhsExpr.accept(this);
    }

    @Override
    public void visit(BLangAssignment bLangAssignment) {
        bLangAssignment.varRef.accept(this);
        bLangAssignment.expr.accept(this);
    }

    @Override
    public void visit(BLangRecordLiteral bLangRecordLiteral) {
        for (RecordLiteralNode.RecordField field : bLangRecordLiteral.fields) {
            ((BLangNode) field).accept(this);
        }
    }

    @Override
    public void visit(BLangRecordKeyValueField recordKeyValue) {
        recordKeyValue.key.accept(this);
        recordKeyValue.valueExpr.accept(this);
    }

    @Override
    public void visit(BLangRecordSpreadOperatorField spreadOperatorField) {
        spreadOperatorField.expr.accept(this);
    }

    @Override
    public void visit(BLangSimpleVarRef bLangSimpleVarRef) {
        BSymbol symbol = bLangSimpleVarRef.symbol;
        // TODO: check constants?
        BSymbol resolvedSymbol = symResolver.lookupClosureVarSymbol(env, symbol.name, SymTag.VARIABLE);
        if (resolvedSymbol == symTable.notFoundSymbol) {
            String identifier = bLangSimpleVarRef.variableName.getValue();
            if (!FRAME_PARAMETER_NAME.equals(identifier) && !identifiers.containsKey(identifier)) {
                DiagnosticPos pos = currectLambdaBody.pos;
                BLangFieldBasedAccess frameAccessExpr = desugar.getFieldAccessExpression(pos, identifier,
                        symTable.anyOrErrorType, currectFrameSymbol);
                frameAccessExpr.expr = desugar.addConversionExprIfRequired(frameAccessExpr.expr,
                        types.getSafeType(frameAccessExpr.expr.type, true, false));
                BLangSimpleVariable variable = ASTBuilderUtil.createVariable(pos, identifier, symbol.type,
                        desugar.addConversionExprIfRequired(frameAccessExpr, symbol.type), (BVarSymbol) symbol);
                BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDef(pos, variable);
                currectLambdaBody.stmts.add(0, variableDef);
                identifiers.put(identifier, symbol);
            }
        } else {
            resolvedSymbol.closure = true;
        }
    }

    /**
     * Create and return a lambda `function(_Frame frame) returns _Frame|error? {...; return frame;}`
     *
     * @param pos of the lambda.
     * @return created lambda function.
     */
    private BLangLambdaFunction createPassthroughLambda(DiagnosticPos pos) {
        // returns (_Frame|error)?
        BLangUnionTypeNode returnType = getUnionTypeNode();
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
    private BLangLambdaFunction createFilterLambda(DiagnosticPos pos) {
        // returns boolean
        BLangValueType returnType = getBooleanTypeNode();
        return createLambdaFunction(pos, returnType, null, false);
    }

    /**
     * Create and return a lambda `function(_Frame frame) {...}`.
     *
     * @param pos of the lambda.
     * @return created lambda function.
     */
    private BLangLambdaFunction createActionLambda(DiagnosticPos pos) {
        // returns ()
        BLangValueType returnType = getNilTypeNode();
        return createLambdaFunction(pos, returnType, null, false);
    }

    /**
     * Creates and return a lambda function without body.
     *
     * @param pos of the lambda.
     * @return created lambda function.
     */
    private BLangLambdaFunction createLambdaFunction(DiagnosticPos pos,
                                                     TypeNode returnType,
                                                     BLangReturn returnNode,
                                                     boolean isPassthrough) {
        // load symbol for function query:lambdaTemplate
        BInvokableSymbol templateSymbol = getLambdaTemplateSymbol();
        BVarSymbol templateFrameSymbol = templateSymbol.getParameters().get(0);

        // function(_Frame frame) ... and ref to frame
        BVarSymbol frameSymbol = new BVarSymbol(0, templateFrameSymbol.name,
                this.env.scope.owner.pkgID, templateFrameSymbol.type, this.env.scope.owner);
        BLangSimpleVariable frameVariable = ASTBuilderUtil.createVariable(pos, null,
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
    private BLangLambdaFunction createLambdaFunction(DiagnosticPos pos,
                                                     List<BLangSimpleVariable> requiredParams,
                                                     TypeNode returnType,
                                                     BLangFunctionBody lambdaBody) {
        return desugar.createLambdaFunction(pos, "$streamLambda$",
                requiredParams, returnType, lambdaBody);
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
                                                                DiagnosticPos pos) {
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
                                                                DiagnosticPos pos) {
        String name = getNewVarName();
        BLangInvocation queryLibInvocation = createQueryLibInvocation(functionName, requiredArgs, pos);
        type = (type == null) ? queryLibInvocation.type : type;
        BVarSymbol varSymbol = new BVarSymbol(0, new Name(name), env.scope.owner.pkgID, type, env.scope.owner);
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
        return "$streamElement$" + streamElementCount++;
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
                                                     DiagnosticPos pos) {
        BInvokableSymbol symbol = getQueryLibInvokableSymbol(functionName);
        BLangInvocation bLangInvocation = ASTBuilderUtil
                .createInvocationExprForMethod(pos, symbol, requiredArgs, symResolver);
        bLangInvocation.type = symbol.retType;
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

    /**
     * Load and return symbol for function query:lambdaTemplate().
     *
     * @return symbol for above function.
     */
    private BInvokableSymbol getLambdaTemplateSymbol() {
        return getQueryLibInvokableSymbol(names.fromString("lambdaTemplate"));
    }

    private BLangExpressionStmt spreadToFrameFunctionStmt(DiagnosticPos pos,
                                                          BLangExpression frameExpr,
                                                          BLangExpression valueExpr) {
        return addToFrameFunctionStmt(pos, frameExpr, null, valueExpr, true);
    }

    private BLangExpressionStmt addToFrameFunctionStmt(DiagnosticPos pos,
                                                       BLangExpression frameExpr,
                                                       BLangExpression keyExpr,
                                                       BLangExpression valueExpr) {
        return addToFrameFunctionStmt(pos, frameExpr, keyExpr, valueExpr, false);
    }

    private BLangExpressionStmt addToFrameFunctionStmt(DiagnosticPos pos,
                                                       BVarSymbol frameSymbol,
                                                       BVarSymbol valueSymbol) {
        BLangLiteral keyLiteral = ASTBuilderUtil.createLiteral(pos, symTable.stringType, valueSymbol.name.value);
        BLangVariableReference frameVarRef = ASTBuilderUtil.createVariableRef(pos, frameSymbol);
        BLangVariableReference valueVarRef = ASTBuilderUtil.createVariableRef(pos, valueSymbol);
        return addToFrameFunctionStmt(pos, frameVarRef, keyLiteral, valueVarRef, false);
    }

    private BLangExpressionStmt addToFrameFunctionStmt(DiagnosticPos pos,
                                                       BLangExpression frameExpr,
                                                       BLangExpression keyExpr,
                                                       BLangExpression valueExpr,
                                                       boolean isSpread) {
        List<BLangExpression> requiredArgs;
        Name functionName;
        if (isSpread) {
            requiredArgs = Lists.of(frameExpr, valueExpr);
            functionName = QUERY_SPREAD_TO_FRAME_FUNCTION;
        } else {
            requiredArgs = Lists.of(frameExpr, keyExpr, valueExpr);
            functionName = QUERY_ADD_TO_FRAME_FUNCTION;
        }
        BLangInvocation invocation = createQueryLibInvocation(functionName, requiredArgs, pos);
        final BLangExpressionStmt exprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        exprStmt.expr = invocation;
        exprStmt.pos = pos;
        return exprStmt;
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
                    symbols.addAll(getIntroducedSymbols((BLangVariable) record.restParam));
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
                if (error.reason != null) {
                    symbols.addAll(getIntroducedSymbols(error.reason));
                }
                if (error.restDetail != null) {
                    symbols.addAll(getIntroducedSymbols(error.restDetail));
                }
                for (BLangErrorVariable.BLangErrorDetailEntry entry : error.detail) {
                    symbols.addAll(getIntroducedSymbols(entry.valueBindingPattern));
                }
            } else {
                // Simple binding
                symbols.add(((BLangSimpleVariable) variable).symbol);
            }
            return symbols;
        }
        return Collections.emptyList();
    }

    /**
     * Return BLangValueType of a nil `()` type.
     *
     * @return a nil type node.
     */
    BLangValueType getNilTypeNode() {
        BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nilTypeNode.typeKind = TypeKind.NIL;
        nilTypeNode.type = symTable.nilType;
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
        anyTypeNode.type = symTable.anyType;
        return anyTypeNode;
    }

    /**
     * Return BLangErrorType node.
     *
     * @return a error type node.
     */
    BLangErrorType getErrorTypeNode() {
        BLangErrorType errorTypeNode = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        errorTypeNode.type = symTable.errorType;
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
        booleanTypeNode.type = symTable.booleanType;
        return booleanTypeNode;
    }

    /**
     * Return union type node consists of _Frame & error & ().
     *
     * @return a union type node.
     */
    private BLangUnionTypeNode getUnionTypeNode() {
        BInvokableSymbol templateSymbol = getLambdaTemplateSymbol();
        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        unionTypeNode.type = templateSymbol.retType;
        unionTypeNode.desugared = true;
        unionTypeNode.memberTypeNodes.add(getFrameTypeNode());
        unionTypeNode.memberTypeNodes.add(getErrorTypeNode());
        unionTypeNode.memberTypeNodes.add(getNilTypeNode());
        return unionTypeNode;
    }

    /**
     * Return _Frame type node.
     *
     * @return a _Frame type node.
     */
    private BLangRecordTypeNode getFrameTypeNode() {
        BRecordTypeSymbol frameTypeSymbol = (BRecordTypeSymbol) symTable.langQueryModuleSymbol.scope
                .lookup(names.fromString("_Frame")).symbol;
        BRecordType frameType = (BRecordType) frameTypeSymbol.type;

        BLangUnionTypeNode restFieldType = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        restFieldType.type = frameType.restFieldType;
        restFieldType.memberTypeNodes.add(getErrorTypeNode());
        restFieldType.memberTypeNodes.add(getAnyTypeNode());

        BLangRecordTypeNode frameTypeNode = (BLangRecordTypeNode) TreeBuilder.createRecordTypeNode();
        frameTypeNode.type = frameType;
        frameTypeNode.restFieldType = restFieldType;
        frameTypeNode.symbol = frameType.tsymbol;
        frameTypeNode.desugared = true;
        return frameTypeNode;
    }

}

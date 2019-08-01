/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

/**
 * This class processes streams models which comes before the selector processor.
 *
 * @since 0.990.0
 */
public class StreamsPreSelectDesuagr extends BLangNodeVisitor {

    private static final CompilerContext.Key<StreamsPreSelectDesuagr> PRE_SELECT_STREAMING_DESUGAR_KEY =
            new CompilerContext.Key<>();
    private final SymbolTable symTable;
    private final Desugar desugar;
    private final StreamingCodeDesugar streamingCodeDesugar;
    private final BLangDiagnosticLog dlog;
    private final SymbolResolver symResolver;

    private BSymbol[] mapVarSymbols;
    private BLangNode result;
    private Map<String, String> aliasMap;
    private BLangVariableReference rhsStream;
    private BRecordType outputType;
    private LongAdder aggregatorIndex;
    private BSymbol aggregatorArray;
    private BVarSymbol streamEventSymbol;

    private StreamsPreSelectDesuagr(CompilerContext context) {
        context.put(PRE_SELECT_STREAMING_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.streamingCodeDesugar = StreamingCodeDesugar.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
    }

    public static StreamsPreSelectDesuagr getInstance(CompilerContext context) {
        StreamsPreSelectDesuagr desugar = context.get(PRE_SELECT_STREAMING_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new StreamsPreSelectDesuagr(context);
        }

        return desugar;
    }

    BLangNode rewrite(BLangNode node, BSymbol[] mapVarSymbol, Map<String, String> aliasMap,
                      BLangVariableReference rhsStream, BType outputType) {
        return rewrite(node, mapVarSymbol, aliasMap, rhsStream, outputType, null, null, null);
    }

    BLangNode rewrite(BLangNode node, BSymbol[] mapVarSymbol, Map<String, String> aliasMap,
                      BLangVariableReference rhsStream, BType outputType, BSymbol aggregatorArrSymbol,
                      LongAdder aggregatorIndex, BVarSymbol streamEventSymbol) {
        if (node == null) {
            return null;
        }

        this.mapVarSymbols = mapVarSymbol;
        this.aggregatorIndex = aggregatorIndex;
        this.aggregatorArray = aggregatorArrSymbol;
        this.aliasMap = aliasMap;
        this.rhsStream = rhsStream;
        this.outputType = (BRecordType) outputType;
        this.streamEventSymbol = streamEventSymbol;
        node.accept(this);
        BLangNode resultNode = this.result;

        this.result = null;
        this.aliasMap = null;
        this.rhsStream = null;
        this.mapVarSymbols = null;
        this.outputType = null;
        this.aggregatorIndex = null;
        this.aggregatorArray = null;
        this.streamEventSymbol = null;

        return resultNode;
    }


    private BLangNode rewrite(BLangNode node) {
        node.accept(this);
        BLangNode resultNode = this.result;

        this.result = null;
        return resultNode;
    }

    private BLangIndexBasedAccess createMapVariableIndexAccessExpr(BVarSymbol mapVariableSymbol,
                                                                   BLangExpression expression) {
        BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(expression.pos, mapVariableSymbol);
        BLangIndexBasedAccess indexExpr = ASTBuilderUtil.createIndexAccessExpr(varRef,
                ASTBuilderUtil.createLiteral(expression.pos, symTable.stringType, expression.toString()));
        indexExpr.type = ((BMapType) mapVariableSymbol.type).constraint;
        indexExpr.pos = expression.pos;
        return indexExpr;
    }

    public void visit(BLangTypeConversionExpr typeConversionExpr) {
        result = desugar.addConversionExprIfRequired((BLangExpression) rewrite(typeConversionExpr.expr),
                        typeConversionExpr.type);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.indexExpr =
                desugar.addConversionExprIfRequired((BLangExpression) rewrite(indexAccessExpr.indexExpr),
                                                    indexAccessExpr.indexExpr.type);
        result = indexAccessExpr;
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        typeTestExpr.expr = desugar.addConversionExprIfRequired((BLangExpression) rewrite(typeTestExpr.expr),
                                                                typeTestExpr.expr.type);
        result = typeTestExpr;
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        // do nothing;
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        elvisExpr.lhsExpr = desugar.addConversionExprIfRequired((BLangExpression) rewrite(elvisExpr.lhsExpr),
                                                                elvisExpr.lhsExpr.type);
        elvisExpr.rhsExpr = desugar.addConversionExprIfRequired((BLangExpression) rewrite(elvisExpr.rhsExpr),
                                                                elvisExpr.rhsExpr.type);
        result = elvisExpr;
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.expr = desugar.addConversionExprIfRequired((BLangExpression) rewrite(ternaryExpr.expr),
                                                               ternaryExpr.expr.type);
        ternaryExpr.thenExpr = desugar.addConversionExprIfRequired((BLangExpression) rewrite(ternaryExpr.thenExpr),
                                                                   ternaryExpr.thenExpr.type);
        ternaryExpr.elseExpr = desugar.addConversionExprIfRequired((BLangExpression) rewrite(ternaryExpr.elseExpr),
                                                                   ternaryExpr.elseExpr.type);
        result = ternaryExpr;
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.expr = desugar.addConversionExprIfRequired((BLangExpression) rewrite(unaryExpr.expr),
                                                             unaryExpr.expr.type);
        result = unaryExpr;
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {

        if (invocationExpr.expr != null) {
            invocationExpr.expr = (BLangExpression) rewrite(invocationExpr.expr);
            if (invocationExpr.expr.type.getKind() != TypeKind.OBJECT) {
                invocationExpr.expr = desugar.addConversionExprIfRequired(invocationExpr.expr, invocationExpr.type);
            }
        }

        BInvokableSymbol symbol = streamingCodeDesugar.getInvokableSymbol(invocationExpr, StreamingCodeDesugar
                .AGGREGATOR_OBJECT_NAME, true);
        if (symbol != null) {
            if (streamingCodeDesugar.isReturnTypeMatching(invocationExpr.pos, StreamingCodeDesugar
                    .AGGREGATOR_OBJECT_NAME, symbol)) {
                aggregatorIndex.increment();
                // aggregatorArr[0].process(e.data["inputStream.age"], e.eventType)
                result = generateAggregatorInvocation(streamEventSymbol, (BVarSymbol) aggregatorArray,
                        aggregatorIndex.longValue() - 1, invocationExpr);
                return;
            }
        }

        if (invocationExpr.requiredArgs.size() > 0) {
            List<BLangExpression> refactoredArgExprs;
            List<BLangExpression> functionArgsList = invocationExpr.requiredArgs;
            List<BLangExpression> list = new ArrayList<>();
            IntStream.range(0, functionArgsList.size()).forEach(i -> {
                BLangExpression arg = functionArgsList.get(i);
                BType type = ((BInvokableSymbol) invocationExpr.symbol).params.get(i).type;
                BLangExpression expression = (BLangExpression) refactorArgsList(arg, type);
                list.add(expression);
            });
            refactoredArgExprs = list;
            invocationExpr.argExprs = refactoredArgExprs;
            invocationExpr.requiredArgs = refactoredArgExprs;
        }

        result = invocationExpr;
    }

    private BLangInvocation generateAggregatorInvocation(BVarSymbol streamEventSymbol, BVarSymbol aggregatorArraySymbol,
                                                         long aggregatorIndex, BLangInvocation invocation) {
        // aggregatorArr[0]
        BLangIndexBasedAccess indexBasedAccess = createIndexBasedAggregatorExpr(aggregatorArraySymbol,
                                                                                aggregatorIndex, invocation.pos);

        // aggregatorArr[0].process(..)
        BLangInvocation aggregatorInvocation = ASTBuilderUtil.createInvocationExpr(invocation.pos,
                streamingCodeDesugar.getNextProcessFunctionSymbol(indexBasedAccess.type.tsymbol),
                Collections.emptyList(), symResolver);
        aggregatorInvocation.expr = indexBasedAccess;

        // arguments of aggregatorArr[0].process(..). e.g. (t.age, e.eventType)
        List<BVarSymbol> params = ((BInvokableSymbol) aggregatorInvocation.symbol).params;
        aggregatorInvocation.requiredArgs = generateAggregatorInvocationArgs(streamEventSymbol, invocation,
                                                                             params);
        aggregatorInvocation.argExprs = aggregatorInvocation.requiredArgs;

        return aggregatorInvocation;
    }

    private BLangIndexBasedAccess createIndexBasedAggregatorExpr(BVarSymbol aggregatorArraySymbol, long aggregatorIndex,
                                                                 DiagnosticPos pos) {
        BLangSimpleVarRef fieldVarRef = ASTBuilderUtil.createVariableRef(pos, aggregatorArraySymbol);
        BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(pos, symTable.intType, aggregatorIndex);
        BLangIndexBasedAccess indexAccessExpr = ASTBuilderUtil.createIndexAccessExpr(fieldVarRef, indexExpr);
        indexAccessExpr.type = ((BArrayType) aggregatorArraySymbol.type).eType;
        return indexAccessExpr;
    }

    private List<BLangExpression> generateAggregatorInvocationArgs(BVarSymbol streamEventSymbol,
                                                                   BLangInvocation funcInvocation,
                                                                   List<BVarSymbol> params) {
        // generates the fields which will be aggregated e.g. t.age
        List<BLangExpression> args = generateAggregatorInputFieldsArgs(funcInvocation, params);
        // generate the EventType for the aggregation o.eventType
        BLangFieldBasedAccess streamEventFieldAccess = generateStreamEventTypeForAggregatorArg(streamEventSymbol,
                funcInvocation.pos, params);
        // (t.age, o.eventType)
        args.add(streamEventFieldAccess);
        return args;
    }

    private BLangFieldBasedAccess generateStreamEventTypeForAggregatorArg(BVarSymbol streamEventSymbol,
                                                                          DiagnosticPos pos,
                                                                          List<BVarSymbol> params) {
        BLangFieldBasedAccess streamEventFieldAccess =
                streamingCodeDesugar.createFieldBasedEventTypeExpr(streamEventSymbol, pos);
        // always the 2nd parameter is the EventType, so the 2nd parameter's type should match.
        streamEventFieldAccess.type = params.get(1).type;
        return streamEventFieldAccess;
    }

    private List<BLangExpression> generateAggregatorInputFieldsArgs(BLangInvocation funcInvocation,
                                                                    List<BVarSymbol> params) {
        List<BLangExpression> args = new ArrayList<>();
        int i = 0;
        for (BLangExpression expr : funcInvocation.argExprs) {
            BLangExpression castExpr =
                    desugar.addConversionExprIfRequired((BLangExpression) rewrite(expr), params.get(i).type);
            args.add(castExpr);
            i++;
        }
        // handles special cases like count(), which does not need arguments.
        if (args.isEmpty()) {
            args.add(ASTBuilderUtil.createLiteral(funcInvocation.pos, symTable.nilType, Names.NIL_VALUE));
        }
        return args;
    }

    private BLangNode refactorArgsList(BLangExpression arg, BType type) {
        BLangNode refactoredArg = rewrite(arg);
        return desugar.addConversionExprIfRequired((BLangExpression) refactoredArg, type);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        groupExpr.expression = (BLangExpression) rewrite(groupExpr.expression);
        result = groupExpr;
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        IntStream.range(0, listConstructorExpr.exprs.size()).forEach(i -> listConstructorExpr.exprs
                .set(i, (BLangExpression) rewrite(listConstructorExpr.exprs.get(i))));
        result = listConstructorExpr;
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        IntStream.range(0, tupleLiteral.exprs.size()).forEach(i -> tupleLiteral.exprs
                .set(i, (BLangExpression) rewrite(tupleLiteral.exprs.get(i))));
        result = tupleLiteral;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr = desugar.addConversionExprIfRequired((BLangExpression) rewrite(binaryExpr.lhsExpr),
                                                                 binaryExpr.lhsExpr.type);
        binaryExpr.rhsExpr = desugar.addConversionExprIfRequired((BLangExpression) rewrite(binaryExpr.rhsExpr),
                                                                 binaryExpr.rhsExpr.type);
        result = binaryExpr;
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        result = varRefExpr;
        outputType.fields.forEach(bField -> {
            if (bField.name.value.equals(varRefExpr.variableName.value)) {
                dlog.error(varRefExpr.pos, DiagnosticCode.OUTPUT_FIELD_VISIBLE_IN_HAVING_ORDER_BY, varRefExpr);
            }
        });
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        result = literalExpr;
    }

    @Override
    public void visit(BLangTypedescExpr typedescExpr) {
        result = typedescExpr;
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (fieldAccessExpr.expr.type.tag == TypeTags.STREAM || fieldAccessExpr.expr.type.tag == TypeTags.TABLE) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) fieldAccessExpr.expr;
            String variableName = ((BLangSimpleVarRef) (fieldAccessExpr).expr).variableName.value;
            if (aliasMap.containsKey(variableName)) {
                ((BLangSimpleVarRef) (fieldAccessExpr).expr).variableName.value = aliasMap.get(variableName);
            }
            String mapKey = fieldAccessExpr.toString();
            BLangExpression indexExpr = ASTBuilderUtil.createLiteral(fieldAccessExpr.pos, symTable.stringType, mapKey);

            if (mapVarSymbols != null) {
                generateMapAccessExpr(fieldAccessExpr.pos, varRef, indexExpr);
            } else if (streamEventSymbol != null) {
                generateStreamEventGetInvocation(fieldAccessExpr.pos, indexExpr);
            }
        } else {
            result = fieldAccessExpr;
        }
    }

    private void generateStreamEventGetInvocation(DiagnosticPos pos, BLangExpression indexExpr) {
        BLangInvocation getFuncInvocation = ASTBuilderUtil.createInvocationExprForMethod(pos, StreamingCodeDesugar.
                getInvokableSymbolOfObject(streamEventSymbol, StreamingCodeDesugar.GET_METHOD_NAME),
                Lists.of(indexExpr), symResolver);
        getFuncInvocation.expr = ASTBuilderUtil.createVariableRef(pos, streamEventSymbol);
        getFuncInvocation.argExprs = getFuncInvocation.requiredArgs;
        result = getFuncInvocation;
    }

    private void generateMapAccessExpr(DiagnosticPos pos, BLangSimpleVarRef varRef,
                                       BLangExpression indexExpr) {
        int mapVarArgIndex;
        BLangSimpleVarRef mapRef;
        //mapVarArgs can contain at most 2 map arguments required for conditional expr in join clause
        if (rhsStream != null && ((varRef.variableName.value.equals((rhsStream).symbol.toString()))
                                  || (varRef.variableName.value.equals(aliasMap.get((rhsStream).symbol.toString()))))) {
            mapVarArgIndex = 1;
        } else {
            mapVarArgIndex = 0;
        }
        mapRef = ASTBuilderUtil.createVariableRef(pos, mapVarSymbols[mapVarArgIndex]);
        result = createMapVariableIndexAccessExpr((BVarSymbol) mapRef.symbol, indexExpr);
    }
}

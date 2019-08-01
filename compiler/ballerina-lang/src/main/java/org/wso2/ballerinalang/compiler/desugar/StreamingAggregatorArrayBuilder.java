/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
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
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Collections;
import java.util.List;

import static org.wso2.ballerinalang.compiler.desugar.StreamingCodeDesugar.AGGREGATOR_OBJECT_NAME;

/**
 * This class will create an array of aggregator functions out of the select clause by visiting through all the
 * expressions in the select clause.
 *
 * @since 0.990.3
 */
public class StreamingAggregatorArrayBuilder extends BLangNodeVisitor {

    private static final CompilerContext.Key<StreamingAggregatorArrayBuilder> AGGREGATOR_ARRAY_BUILDER_KEY =
            new CompilerContext.Key<>();
    private List<BLangExpression> exprs;
    private StreamingCodeDesugar streamingCodeDesugar;
    private SymbolResolver symResolver;

    private StreamingAggregatorArrayBuilder(CompilerContext context) {
        context.put(AGGREGATOR_ARRAY_BUILDER_KEY, this);
        this.streamingCodeDesugar = StreamingCodeDesugar.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
    }

    public static StreamingAggregatorArrayBuilder getInstance(CompilerContext context) {
        StreamingAggregatorArrayBuilder builder = context.get(AGGREGATOR_ARRAY_BUILDER_KEY);
        if (builder == null) {
            builder = new StreamingAggregatorArrayBuilder(context);
        }
        return builder;
    }

    void collectAggregators(List<BLangExpression> exprs, BLangExpression expr, SymbolEnv env) {
        this.exprs = exprs;
        expr.accept(this);
        this.exprs = null;
    }

    private void collectAggregators(BLangExpression expr) {
        expr.accept(this);
    }

    @Override
    public void visit(BLangInvocation invocation) {
        if (invocation.expr != null) {
            collectAggregators(invocation.expr);
        }

        BInvokableSymbol aggregatorInvokableSymbol =
                streamingCodeDesugar.getInvokableSymbol(invocation, AGGREGATOR_OBJECT_NAME, false);
        if (aggregatorInvokableSymbol != null) {
            if (streamingCodeDesugar.isReturnTypeMatching(invocation.pos, AGGREGATOR_OBJECT_NAME,
                                                          aggregatorInvokableSymbol)) {
                BLangInvocation aggregatorInvocation = ASTBuilderUtil.
                        createInvocationExprForMethod(invocation.pos, aggregatorInvokableSymbol,
                                                      Collections.emptyList(), symResolver);
                aggregatorInvocation.type = aggregatorInvokableSymbol.retType;
                exprs.add(aggregatorInvocation);
            }
        }
        invocation.argExprs.forEach(this::collectAggregators);
    }

    @Override
    public void visit(BLangTypeConversionExpr typeConversionExpr) {
        collectAggregators(typeConversionExpr.expr);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        listConstructorExpr.exprs.forEach(this::collectAggregators);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        this.collectAggregators(groupExpr.expression);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldBasedAccess) {
        collectAggregators(fieldBasedAccess.expr);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        collectAggregators(binaryExpr.lhsExpr);
        collectAggregators(binaryExpr.rhsExpr);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        collectAggregators(unaryExpr.expr);
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        collectAggregators(ternaryExpr.expr);
        collectAggregators(ternaryExpr.thenExpr);
        collectAggregators(ternaryExpr.elseExpr);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        collectAggregators(elvisExpr.lhsExpr);
        collectAggregators(elvisExpr.rhsExpr);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        collectAggregators(typeTestExpr.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        collectAggregators(indexAccessExpr.expr);
        collectAggregators(indexAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangLiteral literal) {
        // do nothing;
    }

    @Override
    public void visit(BLangSimpleVarRef simpleVarRef) {
        // do nothing;
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        // do nothing;
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        // do nothing;
    }
}

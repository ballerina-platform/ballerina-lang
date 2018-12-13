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

import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class processes streams models which comes before the selector processor.
 *
 * @since 0.990.0
 */
public class StreamsPreSelectDesuagr extends BLangNodeVisitor {

    private static final CompilerContext.Key<StreamsPreSelectDesuagr> STREAMING_DESUGAR_KEY =
            new CompilerContext.Key<>();
    private final SymbolTable symTable;
    private final Desugar desugar;

    private BSymbol[] mapVarSymbols;
    private BLangNode result;
    private Map<String, String> aliasMap;
    private BLangVariableReference rhsStream;

    private StreamsPreSelectDesuagr(CompilerContext context) {
        context.put(STREAMING_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.desugar = Desugar.getInstance(context);
    }

    public static StreamsPreSelectDesuagr getInstance(CompilerContext context) {
        StreamsPreSelectDesuagr desugar = context.get(STREAMING_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new StreamsPreSelectDesuagr(context);
        }

        return desugar;
    }

    public BLangNode rewrite(BLangNode node, BSymbol[] mapVarSymbol, Map<String, String> aliasMap,
                             BLangVariableReference rhsStream) {
        if (node == null) {
            return null;
        }

        this.mapVarSymbols = mapVarSymbol;
        this.aliasMap = aliasMap;
        this.rhsStream = rhsStream;
        node.accept(this);
        BLangNode resultNode = this.result;

        this.result = null;
        this.aliasMap = null;
        this.rhsStream = null;
        this.mapVarSymbols = null;

        return resultNode;
    }

    public BLangNode rewrite(BLangNode node, BSymbol[] mapVarSymbols) {
        return rewrite(node, mapVarSymbols, aliasMap, rhsStream);
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

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (invocationExpr.requiredArgs.size() > 0) {
            List<BLangExpression> refactoredArgExprs;
            List<BLangExpression> functionArgsList = invocationExpr.requiredArgs;
            refactoredArgExprs = functionArgsList.stream().map(arg -> (BLangExpression) rewrite(arg))
                    .collect(Collectors.toList());
            invocationExpr.argExprs = refactoredArgExprs;
            invocationExpr.requiredArgs = refactoredArgExprs;
        }

        result = invocationExpr;
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        IntStream.range(0, bracedOrTupleExpr.expressions.size()).forEach(i -> bracedOrTupleExpr.expressions
                .set(i, (BLangExpression) rewrite(bracedOrTupleExpr.expressions.get(i))));
        result = bracedOrTupleExpr;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr = (BLangExpression) rewrite(binaryExpr.lhsExpr);
        binaryExpr.rhsExpr = (BLangExpression) rewrite(binaryExpr.rhsExpr);
        result = binaryExpr;
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        result = varRefExpr;
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        result = literalExpr;
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (fieldAccessExpr.expr.type.tag == TypeTags.STREAM) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) fieldAccessExpr.expr;
            BLangSimpleVarRef mapRef;
            int mapVarArgIndex;

            //mapVarArgs can contain at most 2 map arguments required for conditional expr in join clause
            if (rhsStream != null && ((varRef.variableName.value.equals((rhsStream).symbol.toString()))
                    || (varRef.variableName.value.equals(aliasMap.get((rhsStream).symbol.toString()))))) {
                mapVarArgIndex = 1;
            } else {
                mapVarArgIndex = 0;
            }
            mapRef = ASTBuilderUtil.createVariableRef(fieldAccessExpr.pos, mapVarSymbols[mapVarArgIndex]);

            String variableName = ((BLangSimpleVarRef) (fieldAccessExpr).expr).variableName.value;
            if (aliasMap.containsKey(variableName)) {
                ((BLangSimpleVarRef) (fieldAccessExpr).expr).variableName.value = aliasMap.get(variableName);
            }
            String mapKey = fieldAccessExpr.toString();
            BLangExpression indexExpr = ASTBuilderUtil.createLiteral(fieldAccessExpr.pos, symTable.stringType,
                                                                     mapKey);
            BLangIndexBasedAccess mapAccessExpr = createMapVariableIndexAccessExpr((BVarSymbol) mapRef.symbol,
                                                                                   indexExpr);
            result = desugar.addConversionExprIfRequired(mapAccessExpr, fieldAccessExpr.type);
        } else {
            result = fieldAccessExpr;
        }
    }
}

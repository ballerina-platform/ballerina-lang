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

import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;

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
    private final BLangDiagnosticLog dlog;

    private BSymbol[] mapVarSymbols;
    private BLangNode result;
    private Map<String, String> aliasMap;
    private BLangVariableReference rhsStream;
    private BRecordType outputType;

    private StreamsPreSelectDesuagr(CompilerContext context) {
        context.put(STREAMING_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public static StreamsPreSelectDesuagr getInstance(CompilerContext context) {
        StreamsPreSelectDesuagr desugar = context.get(STREAMING_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new StreamsPreSelectDesuagr(context);
        }

        return desugar;
    }

    public BLangNode rewrite(BLangNode node, BSymbol[] mapVarSymbol, Map<String, String> aliasMap,
                             BLangVariableReference rhsStream, BType outputType) {
        if (node == null) {
            return null;
        }

        this.mapVarSymbols = mapVarSymbol;
        this.aliasMap = aliasMap;
        this.rhsStream = rhsStream;
        this.outputType = (BRecordType) outputType;
        node.accept(this);
        BLangNode resultNode = this.result;

        this.result = null;
        this.aliasMap = null;
        this.rhsStream = null;
        this.mapVarSymbols = null;
        this.outputType = null;

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
        if (invocationExpr.requiredArgs.size() > 0) {
            List<BLangExpression> refactoredArgExprs;
            List<BLangExpression> functionArgsList = invocationExpr.requiredArgs;
            refactoredArgExprs = functionArgsList.stream().map(arg -> (BLangExpression) refactorArgsList(arg))
                    .collect(Collectors.toList());
            invocationExpr.argExprs = refactoredArgExprs;
            invocationExpr.requiredArgs = refactoredArgExprs;
        }

        result = invocationExpr;
    }

    private BLangNode refactorArgsList(BLangExpression arg) {
        BLangNode refactoredArg = rewrite(arg);
        return desugar.addConversionExprIfRequired((BLangExpression) refactoredArg, arg.type);
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        IntStream.range(0, bracedOrTupleExpr.expressions.size()).forEach(i -> bracedOrTupleExpr.expressions
                .set(i, (BLangExpression) refactorArgsList(bracedOrTupleExpr.expressions.get(i))));
        result = bracedOrTupleExpr;
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
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (fieldAccessExpr.expr.type.tag == TypeTags.STREAM || fieldAccessExpr.expr.type.tag == TypeTags.TABLE) {
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
            result = mapAccessExpr;
        } else {
            result = fieldAccessExpr;
        }
    }
}

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
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class processes streams models which comes after the selector processor.
 *
 * @since 0.990.0
 */
public class StreamsPostSelectDesugar extends BLangNodeVisitor {
    private static final CompilerContext.Key<StreamsPostSelectDesugar> STREAMING_DESUGAR_KEY =
            new CompilerContext.Key<>();
    private final SymbolTable symTable;
    private final Desugar desugar;

    private BLangNode result;
    private BSymbol mapVarSymbol;
    private BType outputEventType;

    private StreamsPostSelectDesugar(CompilerContext context) {
        context.put(STREAMING_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.desugar = Desugar.getInstance(context);
    }

    public static StreamsPostSelectDesugar getInstance(CompilerContext context) {
        StreamsPostSelectDesugar desugar = context.get(STREAMING_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new StreamsPostSelectDesugar(context);
        }

        return desugar;
    }

    public  BLangNode rewrite(BLangNode node, BSymbol mapVarSymbol, BType outputEventType) {
        if (node == null) {
            return null;
        }

        this.mapVarSymbol = mapVarSymbol;
        this.outputEventType = outputEventType;
        node.accept(this);
        BLangNode resultNode = this.result;

        this.result = null;
        this.mapVarSymbol = null;
        this.outputEventType = null;

        return resultNode;
    }

    private BLangNode rewrite(BLangNode node) {
        node.accept(this);
        BLangNode resultNode = this.result;

        this.result = null;

        return resultNode;
    }

    @Override
    public void visit(BLangSimpleVarRef varRef) {
        Map<String, BField> fields = ((BRecordType) outputEventType).fields.stream()
                .collect(Collectors.toMap(field -> field.name.getValue(), field -> field, (a, b) -> b));
        String key = varRef.variableName.value;
        if (fields.containsKey(key)) {
            result = createMapVariableIndexAccessExpr((BVarSymbol) mapVarSymbol,
                    ASTBuilderUtil.createLiteral(varRef.pos, symTable.stringType, getEquivalentOutputMapField(key)));
            result = desugar.addConversionExprIfRequired((BLangExpression) result, varRef.type);
        } else {
            result = varRef;
        }
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        for (int i = 0; i < bracedOrTupleExpr.expressions.size(); i++) {
            bracedOrTupleExpr.expressions.set(i, (BLangExpression) rewrite(bracedOrTupleExpr.expressions.get(i)));
        }
        result = bracedOrTupleExpr;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr = (BLangExpression) rewrite(binaryExpr.lhsExpr);
        binaryExpr.rhsExpr = (BLangExpression) rewrite(binaryExpr.rhsExpr);
        result = binaryExpr;
    }


    private String getEquivalentOutputMapField(String key) {
        return "OUTPUT." + key;
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
            List<BLangExpression> refactoredArgExprs = new ArrayList<>();
            List<BLangExpression> functionArgsList = invocationExpr.requiredArgs;
            for (BLangExpression arg : functionArgsList) {
                refactoredArgExprs.add((BLangExpression) rewrite(arg));
            }
            invocationExpr.argExprs = refactoredArgExprs;
            invocationExpr.requiredArgs = refactoredArgExprs;
        }

        result = invocationExpr;
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        result = literalExpr;
    }
}

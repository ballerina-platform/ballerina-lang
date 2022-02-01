/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.MatchExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * {@code BLangMatchExpression} represents a type switch expression in Ballerina.
 *
 * @since 0.970.0
 */
@Deprecated
public class BLangMatchExpression extends BLangExpression implements MatchExpressionNode {

    // BLangNodes
    public BLangExpression expr;
    public List<BLangMatchExprPatternClause> patternClauses = new ArrayList<>();

    @Override
    public NodeKind getKind() {
        return NodeKind.MATCH_EXPRESSION;
    }

    @Override
    public BLangExpression getExpression() {
        return expr;
    }

    @Override
    public List<BLangMatchExprPatternClause> getPatternClauses() {
        return patternClauses;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }

    /**
     * {@code BLangMatchExprPatternClause} represents a pattern inside a type switch expression.
     *
     * @since 0.970.0
     */
    public static class BLangMatchExprPatternClause extends BLangNode implements MatchExpressionPatternNode {
        // BLangNodes
        public BLangSimpleVariable variable;
        public BLangExpression expr;

        // Semantic Data
        // This field is used to capture types that are matched to this pattern.
        public Set<BType> matchedTypesDirect = new LinkedHashSet<>();
        public Set<BType> matchedTypesIndirect = new LinkedHashSet<>();

        @Override
        public NodeKind getKind() {
            return NodeKind.MATCH_EXPRESSION_PATTERN_CLAUSE;
        }

        @Override
        public BLangSimpleVariable getVariableNode() {
            return variable;
        }

        @Override
        public ExpressionNode getStatement() {
            return expr;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
            analyzer.visit(this, props);
        }

        @Override
        public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
            return modifier.transform(this, props);
        }
        
        @Override
        public String toString() {
            return variable + " => " + expr.toString();
        }
    }
    
    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");
        patternClauses.forEach(pattern -> sj.add(pattern.toString()));
        return expr.toString() + " but {" + sj.toString() + "}";
    }
}

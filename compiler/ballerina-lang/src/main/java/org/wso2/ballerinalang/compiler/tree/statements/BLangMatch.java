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
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.clauses.OnFailClauseNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.MatchNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * {@code BLangMatch} represents a type switch statement in Ballerina.
 *
 * @since 0.966.0
 */
public class BLangMatch extends BLangStatement implements MatchNode {

    public BLangMatch() {
        this.patternClauses = new ArrayList<>();
        this.exprTypes = new ArrayList<>();
    }

    public BLangExpression expr;
    public List<BLangMatchBindingPatternClause> patternClauses;
    public List<BType> exprTypes;
    public BLangOnFailClause onFailClause;

    @Override
    public NodeKind getKind() {
        return NodeKind.MATCH;
    }

    @Override
    public BLangExpression getExpression() {
        return expr;
    }

    @Override
    public OnFailClauseNode getOnFailClause() {
        return this.onFailClause;
    }

    @Override
    public void setOnFailClause(OnFailClauseNode onFailClause) {
        this.onFailClause = (BLangOnFailClause) onFailClause;
    }

    @Override
    public List<BLangMatchBindingPatternClause> getPatternClauses() {
        return patternClauses;
    }

    @Override
    public List<BLangMatchStaticBindingPatternClause> getStaticPatternClauses() {
        return patternClauses
                .stream()
                .filter(pattern -> NodeKind.MATCH_STATIC_PATTERN_CLAUSE == (pattern.getKind()))
                .map(BLangMatchStaticBindingPatternClause.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<BLangMatchStructuredBindingPatternClause> getStructuredPatternClauses() {
        return patternClauses
                .stream()
                .filter(pattern -> NodeKind.MATCH_STRUCTURED_PATTERN_CLAUSE == (pattern.getKind()))
                .map(BLangMatchStructuredBindingPatternClause.class::cast)
                .collect(Collectors.toList());
    }


    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(";");
        patternClauses.forEach(pattern -> sj.add(pattern.toString()));
        return String.valueOf(expr) + " match {" + String.valueOf(sj) + "}";
    }

    /**
     * {@code BLangMatchBindingPatternClause} is the parent class for all the pattern clauses.
     *
     * @since 0.985.0
     */
    public abstract static class BLangMatchBindingPatternClause extends BLangNode implements
            MatchBindingPatternNode {

        // pattern clause's body
        public BLangBlockStmt body;

        // match stmt expr
        public BLangExpression matchExpr;

        // flag to set the last pattern clause
        public boolean isLastPattern;
    }

    /**
     * {@code BLangMatchTypedBindingPatternClause} represents a pattern inside a type switch statement.
     *
     * @since 0.966.0
     */
    public static class BLangMatchTypedBindingPatternClause extends BLangMatchBindingPatternClause
            implements MatchTypedBindingPatternNode {

        public BLangSimpleVariable variable;

        @Override
        public NodeKind getKind() {
            return NodeKind.MATCH_TYPED_PATTERN_CLAUSE;
        }

        @Override
        public BLangSimpleVariable getVariableNode() {
            return variable;
        }

        @Override
        public BLangStatement getStatement() {
            return body;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return String.valueOf(variable) + " => " + String.valueOf(body);
        }
    }

    /**
     * {@code BLangMatchStaticBindingPatternClause} represents a static/constant pattern inside a match
     * statement.
     *
     * @since 0.985.0
     */
    public static class BLangMatchStaticBindingPatternClause extends BLangMatchBindingPatternClause
            implements MatchStaticBindingPatternNode {

        // static match literal expr
        public BLangExpression literal;

        @Override
        public NodeKind getKind() {
            return NodeKind.MATCH_STATIC_PATTERN_CLAUSE;
        }

        @Override
        public BLangExpression getLiteral() {
            return literal;
        }

        @Override
        public BLangStatement getStatement() {
            return body;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return String.valueOf(literal) + " => " + String.valueOf(body);
        }
    }

    /**
     * {@code BLangMatchStructuredBindingPatternClause} represents a structured pattern inside a match
     * statement.
     *
     * @since 0.985.0
     */
    public static class BLangMatchStructuredBindingPatternClause extends BLangMatchBindingPatternClause
            implements MatchStructuredBindingPatternNode {

        // binding match pattern
        public BLangVariable bindingPatternVariable;

        // type guard expression
        public BLangExpression typeGuardExpr;

        @Override
        public NodeKind getKind() {
            return NodeKind.MATCH_STRUCTURED_PATTERN_CLAUSE;
        }

        @Override
        public BLangVariable getVariableNode() {
            return bindingPatternVariable;
        }

        @Override
        public BLangExpression getTypeGuardExpr() {
            return typeGuardExpr;
        }

        @Override
        public BLangStatement getStatement() {
            return body;
        }

        @Override
        public void accept(BLangNodeVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return String.valueOf(bindingPatternVariable) + " => " + String.valueOf(body);
        }
    }
}

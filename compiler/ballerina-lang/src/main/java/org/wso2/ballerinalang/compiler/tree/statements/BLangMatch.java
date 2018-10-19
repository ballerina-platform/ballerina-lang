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

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.MatchNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

/**
 * {@code BLangMatch} represents a type switch statement in Ballerina.
 *
 * @since 0.966.0
 */
public class BLangMatch extends BLangStatement implements MatchNode {

    public BLangExpression expr;
    public List<BLangMatchStmtBindingPatternClause> patternClauses;
    public List<BType> exprTypes;

    //todo remove this list
    public List<BLangMatchStmtSimpleBindingPatternClause> simplePatternClauses;


    @Override
    public NodeKind getKind() {
        return NodeKind.MATCH;
    }

    @Override
    public BLangExpression getExpression() {
        return expr;
    }

    @Override
    public List<BLangMatchStmtSimpleBindingPatternClause> getSimplePatternClauses() {
        return simplePatternClauses;
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
     * {@code BLangMatchStmtBindingPatternClause} is the parent class for all the pattern clauses.
     *
     * @since 0.983.0
     */
    public static abstract class BLangMatchStmtBindingPatternClause extends BLangNode implements
            MatchStatementBindingPatternNode {
        public BLangBlockStmt body;

        // This field is used to capture types that are matched to this pattern.
        public Set<BType> matchedTypesDirect = new HashSet<>();
        public Set<BType> matchedTypesIndirect = new HashSet<>();
    }

    /**
     * {@code BLangMatchStmtSimpleBindingPatternClause} represents a pattern inside a type switch statement.
     *
     * @since 0.966.0
     */
    public static class BLangMatchStmtSimpleBindingPatternClause extends BLangMatchStmtBindingPatternClause
            implements MatchStatementSimpleBindingPatternNode {

        public BLangSimpleVariable variable;

        @Override
        public NodeKind getKind() {
            return NodeKind.MATCH_SIMPLE_PATTERN_CLAUSE;
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
     * {@code BLangMatchStmtStaticBindingPatternClause} represents a pattern inside a type switch statement.
     *
     * @since 0.983.0
     */
    public static class BLangMatchStmtStaticBindingPatternClause extends BLangMatchStmtBindingPatternClause
            implements MatchStatementStaticBindingPatternNode {

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
}

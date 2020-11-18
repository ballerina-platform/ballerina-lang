/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.clauses.MatchClauseNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.MatchGuard;
import org.ballerinalang.model.tree.matchpatterns.MatchPatternNode;
import org.ballerinalang.model.tree.statements.BlockStatementNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMatchPattern;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * @since Swan Lake
 */
public class BLangMatchClause extends BLangNode implements MatchClauseNode {

    public List<BLangMatchPattern> matchPatterns = new ArrayList<>();
    public BLangMatchGuard matchGuard;
    public BLangBlockStmt blockStmt;
    public boolean isLastClause;
    public BLangExpression expr; // This is used to keep the expression of match statement.

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.MATCH_CLAUSE;
    }

    @Override
    public BLangMatchGuard getMatchGuard() {
        return matchGuard;
    }

    @Override
    public void setMatchGuard(MatchGuard matchGuard) {
        this.matchGuard = (BLangMatchGuard) matchGuard;
    }

    @Override
    public BlockStatementNode getBLockStatement() {
        return blockStmt;
    }

    @Override
    public void setBlockStatement(BlockStatementNode blockStatement) {
        this.blockStmt = (BLangBlockStmt) blockStatement;
    }

    @Override
    public List<? extends MatchPatternNode> getMatchPatterns() {
        return matchPatterns;
    }

    @Override
    public void addMatchPattern(MatchPatternNode matchPattern) {
        matchPatterns.add((BLangMatchPattern) matchPattern);
    }

    @Override
    public boolean isLastClause() {
        return isLastClause;
    }

    @Override
    public void setLastClause() {
        this.isLastClause = true;
    }

    @Override
    public ExpressionNode getExpression() {
        return expr;
    }

    @Override
    public void setExpression(ExpressionNode expression) {
        this.expr = (BLangExpression) expression;
    }
}

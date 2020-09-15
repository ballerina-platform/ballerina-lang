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
package org.ballerinalang.model.clauses;

import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.MatchGuard;
import org.ballerinalang.model.tree.matchpatterns.MatchPatternNode;
import org.ballerinalang.model.tree.statements.BlockStatementNode;

import java.util.List;

/**
 * The interface with the APIs to implement the match-clause.
 *
 * @since Swan Lake
 */
public interface MatchClauseNode extends Node {

    MatchGuard getMatchGuard();

    void setMatchGuard(MatchGuard matchGuard);

    BlockStatementNode getBLockStatement();

    void setBlockStatement(BlockStatementNode blockStatement);

    List<? extends MatchPatternNode> getMatchPatterns();

    void addMatchPattern(MatchPatternNode matchPattern);

    boolean isLastClause();

    void setLastClause();

    ExpressionNode getExpression();

    void setExpression(ExpressionNode expression);
}

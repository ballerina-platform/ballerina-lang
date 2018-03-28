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

package org.ballerinalang.model.tree.statements;

import org.ballerinalang.model.tree.clauses.JoinStreamingInput;
import org.ballerinalang.model.tree.clauses.OrderByNode;
import org.ballerinalang.model.tree.clauses.OutputRateLimitNode;
import org.ballerinalang.model.tree.clauses.PatternClause;
import org.ballerinalang.model.tree.clauses.SelectClauseNode;
import org.ballerinalang.model.tree.clauses.StreamActionNode;
import org.ballerinalang.model.tree.clauses.StreamingInput;

/**
 * This class represents the streaming query statement in streams SQLish syntax.
 * <pre>Grammar:
 *      FROM (streamingInput (joinStreamingInput)?  | patternStreamingInput)
 *      selectClause?
 *      orderByClause?
 *      streamingAction
 *
 * E.g.
 *      from testStream
 *      where x &gt; 50
 *      select 10 as mo
 *      group by mo
 *      insert into test1
 * </pre>
 *
 * @since 0.965.0
 */

public interface StreamingQueryStatementNode extends StatementNode {

    void setStreamingInput(StreamingInput streamingInput);

    void setJoinStreamingInput(JoinStreamingInput joinStreamingInput);

    void setPatternClause(PatternClause patternClause);

    void setSelectClause(SelectClauseNode selectClause);

    void setOrderByClause(OrderByNode orderByClause);

    void setStreamingAction(StreamActionNode streamingAction);

    StreamingInput getStreamingInput();

    JoinStreamingInput getJoiningInput();

    PatternClause getPatternClause();

    SelectClauseNode getSelectClause();

    OrderByNode getOrderbyClause();

    StreamActionNode getStreamingAction();

    OutputRateLimitNode getOutputRateLimitNode();

    void setOutputRateLimitNode(OutputRateLimitNode outputRateLimitNode);
}

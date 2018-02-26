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
import org.ballerinalang.model.tree.clauses.JoinStreamingInput;
import org.ballerinalang.model.tree.clauses.OrderByNode;
import org.ballerinalang.model.tree.clauses.PatternStreamingInputNode;
import org.ballerinalang.model.tree.clauses.SelectClauseNode;
import org.ballerinalang.model.tree.clauses.StreamActionNode;
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.statements.StreamingQueryStatementNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * @since 0.94
 */
public class BLangStreamingQueryStatement extends BLangStatement implements StreamingQueryStatementNode {

    private StreamingInput streamingInput;
    private JoinStreamingInput joinStreamingInput;
    private PatternStreamingInputNode patternStreamingInputNode;
    private SelectClauseNode selectClauseNode;
    private OrderByNode orderByNode;
    private StreamActionNode streamActionNode;

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.STREAMING_QUERY;
    }

    @Override
    public void setStreamingInput(StreamingInput streamingInput) {
        this.streamingInput = streamingInput;
    }

    @Override
    public void setJoinStreamingInput(JoinStreamingInput joinStreamingInput) {
        this.joinStreamingInput = joinStreamingInput;
    }

    @Override
    public void setPatternStreamingInput(PatternStreamingInputNode patternStreamingInputNode) {
        this.patternStreamingInputNode = patternStreamingInputNode;
    }

    @Override
    public void setSelectClause(SelectClauseNode selectClause) {
        this.selectClauseNode = selectClause;
    }

    @Override
    public void setOrderByClause(OrderByNode orderByClause) {
        this.orderByNode = orderByClause;
    }

    @Override
    public void setStreamingAction(StreamActionNode streamingAction) {
        this.streamActionNode = streamingAction;
    }

    @Override
    public StreamingInput getStreamingInput() {
        return streamingInput;
    }

    @Override
    public JoinStreamingInput getJoiningInput() {
        return joinStreamingInput;
    }

    @Override
    public PatternStreamingInputNode getPatternStreamingInput() {
        return patternStreamingInputNode;
    }

    @Override
    public SelectClauseNode getSelectClause() {
        return selectClauseNode;
    }

    @Override
    public OrderByNode getOrderbyClause() {
        return orderByNode;
    }

    @Override
    public StreamActionNode getStreamingAction() {
        return streamActionNode;
    }
}

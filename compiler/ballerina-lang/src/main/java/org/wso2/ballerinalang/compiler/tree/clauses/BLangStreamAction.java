/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.clauses.SetAssignmentNode;
import org.ballerinalang.model.tree.clauses.StreamActionNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link StreamActionNode}.
 *
 * @since 0.965.0
 */
public class BLangStreamAction extends BLangNode implements StreamActionNode {

    private ExpressionNode targetReference;

    private String outputEventType;

    private StreamActionType action;

    private List<SetAssignmentNode> setAssignmentNodeList;

    private ExpressionNode expressionNode;

    private Map<String, StreamActionType> streamActionTypeMap = new HashMap<>();

    public BLangStreamAction() {
        streamActionTypeMap.put("insert", StreamActionType.INSERT);
        streamActionTypeMap.put("delete", StreamActionType.DELETE);
        streamActionTypeMap.put("update", StreamActionType.UPDATE);

        action = StreamActionType.UNKNOWN;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.STREAM_ACTION;
    }

    @Override
    public void setStreamActionType(String streamActionType) {
        if (streamActionTypeMap.containsKey(streamActionType)) {
            action = streamActionTypeMap.get(streamActionType);
        }
    }

    @Override
    public void setTargetReference(ExpressionNode ref) {
        this.targetReference = ref;
    }

    @Override
    public void setSetClause(List<SetAssignmentNode> setAssignmentNodeList) {
        this.setAssignmentNodeList = setAssignmentNodeList;
    }

    @Override
    public void setExpression(ExpressionNode expressionNode) {
        this.expressionNode = expressionNode;
    }

    @Override
    public ExpressionNode getTargetReference() {
        return targetReference;
    }

    @Override
    public List<SetAssignmentNode> getSetClause() {
        return setAssignmentNodeList;
    }

    @Override
    public ExpressionNode getExpression() {
        return expressionNode;
    }

    @Override
    public String getActionType() {
        return action.toString();
    }

    @Override
    public String getOutputEventType() {
        return outputEventType;
    }

    @Override
    public void setOutputEventType(boolean isAllEvents, boolean isCurrentEvents, boolean isExpiredEvents) {
        if (isAllEvents) {
            this.outputEventType = "all events";
        } else if (isExpiredEvents) {
            this.outputEventType = "expired events";
        } else if (isCurrentEvents) {
            this.outputEventType = "current events";
        }
    }

    private enum StreamActionType {
        INSERT,
        DELETE,
        UPDATE,
        UNKNOWN
    }
}

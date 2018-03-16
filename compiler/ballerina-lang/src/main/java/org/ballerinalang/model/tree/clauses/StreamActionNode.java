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

package org.ballerinalang.model.tree.clauses;

import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.expressions.ExpressionNode;

import java.util.List;

/**
 * The interface with the APIs to implement the "group by" in ballerina streams/table SQLish syntax.
 * <pre>Grammar:
 *     INSERT INTO Identifier |   UPDATE (OR INSERT INTO)? Identifier setClause ? ON expression |
 *     DELETE Identifier ON expression
 *
 * E.g.
 *      insert into testInputStream
 *      update testTable set x == 20
 *      delete testTable on symbol="WSO2"
 * </pre>
 *
 * @since 0.965.0
 */

public interface StreamActionNode extends Node {

    void setStreamActionType(String streamActionType);

    void setTargetReference(ExpressionNode ref);

    void setSetClause(List<SetAssignmentNode> setAssignmentNodeList);

    void setExpression(ExpressionNode expressionNode);

    ExpressionNode getTargetReference();

    List<SetAssignmentNode> getSetClause();

    ExpressionNode getExpression();

    String getActionType();

    String getOutputEventType();

    void setOutputEventType(boolean isAllEvents, boolean isCurrentEvents, boolean isExpiredEvents);
}

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

/**
 * @since 0.965.0
 * <p>
 * This interface represents the "join" clause of table/streams SQLish syntax.
 * <pre>Grammar:
 *      JOIN streamingInput ON expression
 *
 * E.g.
 *      join LogStream where timestamp == 1506756543
 *                  window time(3 sec)
 *                      where tenantId == -1234 as tempStream
 *                          on tempStream.component == anotherStream.component
 *
 * </pre>
 */
public interface JoinStreamingInput extends Node {

    String getJoinType();

    void setJoinType(String joinType);

    boolean isUnidirectionalBeforeJoin();

    void setUnidirectionalBeforeJoin(boolean unidirectionalBeforeJoin);

    boolean isUnidirectionalAfterJoin();

    void setUnidirectionalAfterJoin(boolean unidirectionalAfterJoin);

    void setStreamingInput(StreamingInput streamingInput);

    StreamingInput getStreamingInput();

    void setOnExpression(ExpressionNode expression);

    ExpressionNode getOnExpression();
}

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
 * The interface with the APIs to implement the "pattern" in ballerina streams/table SQLish syntax.
 * <pre>Grammar:
 *     Identifier whereClause? intRangeExpression? (AS alias=Identifier)?
 *
 * E.g.
 *      TempStream where e1.roomNo==roomNo [1..5) as e2
 * </pre>
 *
 * @since 0.965.0
 */

public interface PatternStreamingEdgeInputNode extends Node {


    void setStreamReference(ExpressionNode ref);

    void setWhereClause(WhereNode whereNode);

    void setExpression(ExpressionNode expressionNode);

    ExpressionNode getStreamReference();

    WhereNode getWhereClause();

    ExpressionNode getExpression();

    void setAliasIdentifier (String alias);

    String getAliasIdentifier();
}

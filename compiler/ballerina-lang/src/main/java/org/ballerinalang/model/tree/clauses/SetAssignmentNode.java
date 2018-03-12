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
 * This interface represents the Set Assignment clause in Streams/Tables grammar in Ballerina.
 * <pre>Grammar:
 *      variableReference ASSIGN expression
 *
 * E.g.
 *      symbol = "IBM"
 *      symbol = companyName
 * </pre>
 *
 * @since 0.965.0
 */

public interface SetAssignmentNode extends Node {

    void setVariableReference(ExpressionNode variableReference);

    ExpressionNode getVariableReference();

    void setExpression(ExpressionNode expressionNode);

    ExpressionNode getExpressionNode();
}

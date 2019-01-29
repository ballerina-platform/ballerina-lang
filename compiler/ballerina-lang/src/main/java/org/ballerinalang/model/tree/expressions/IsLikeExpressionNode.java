/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.model.tree.expressions;

import org.ballerinalang.model.tree.types.TypeNode;

/**
 * Represents an is like expression in Ballerina.
 * Syntax: x isLike T
 *
 * @since 0.985.0
 */
public interface IsLikeExpressionNode extends ExpressionNode {

    /**
     * Get the expression associated with the is like check.
     *
     * @return Expression associated with the is like check.
     */
    ExpressionNode getExpression();

    /**
     * Get the expression associated with the is like check.
     *
     * @param expr Expression associated with the is like check.
     */
    void setExpression(ExpressionNode expr);

    /**
     * Get the type node of this is like check expression.
     *
     * @return Type node of this is like check expression.
     */
    TypeNode getTypeNode();

    /**
     * Set the type node of this is like check expression.
     *
     * @param type Type node of this is like check expression.
     */
    void setTypeNode(TypeNode type);
}

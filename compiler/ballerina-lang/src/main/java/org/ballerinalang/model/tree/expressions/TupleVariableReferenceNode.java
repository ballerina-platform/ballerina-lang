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

import org.ballerinalang.model.tree.IdentifierNode;

import java.util.List;

/**
 * Represents a tuple variable reference node.
 * Example:
 *      string s;
 *      int i;
 *      boolean b;
 *
 *      (s, i, b) = ("Foo", 12, true);
 *
 * @since 0.985.0
 */
public interface TupleVariableReferenceNode extends VariableReferenceNode {

    IdentifierNode getPackageAlias();

    List<? extends ExpressionNode> getExpressions();

}

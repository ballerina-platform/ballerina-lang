/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
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

package org.ballerinalang.model.tree.expressions;

import java.util.List;

/**
 * Represents a natural expression node.
 *
 * @since 2201.13.0
 */
public interface NaturalExpressionNode extends ExpressionNode {

    List<? extends ExpressionNode> getInsertions();

    void addInsertion(ExpressionNode expression);

    List<? extends ExpressionNode> getStrings();

    void addString(ExpressionNode stringLiteral);
}

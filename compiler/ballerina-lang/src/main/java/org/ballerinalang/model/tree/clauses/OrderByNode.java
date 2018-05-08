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

import java.util.List;

/**
 * @since 0.965.0
 * <p>
 * The interface with the APIs to implement the "order by" in ballerina streams/table SQLish syntax.
 * <pre> Grammar:
 *      ORDER BY variableReferenceList
 *
 * E.g
 *      order by age, rank;
 * </pre>
 */
public interface OrderByNode extends Node {

    void addOrderByVariable(OrderByVariableNode orderByVariableNode);

    List<? extends OrderByVariableNode> getVariables();
}

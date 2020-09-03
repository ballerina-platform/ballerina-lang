/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.clauses.OrderByClauseNode;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Implementation of "order by" clause statement.
 *
 * @since Swan Lake
 */
public class BLangOrderByClause extends BLangNode implements OrderByClauseNode {
    public List<OrderKeyNode> orderByKeyList = new ArrayList<>();

    public BLangOrderByClause() {

    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ORDER_BY;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void addOrderKey(OrderKeyNode orderKeyNode) {
        orderByKeyList.add(orderKeyNode);
    }

    @Override
    public List<OrderKeyNode> getOrderKeyList() {
        return orderByKeyList;
    }

    @Override
    public String toString() {
        StringJoiner declarations = new StringJoiner(", ");
        for (OrderKeyNode orderKeyList : orderByKeyList) {
            declarations.add(String.valueOf(orderKeyList));
        }
        return "order by " + declarations.toString();
    }
}

/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree.statements;

import org.ballerinalang.model.clauses.DoClauseNode;
import org.ballerinalang.model.clauses.FromClauseNode;
import org.ballerinalang.model.clauses.WhereClauseNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.statements.QueryActionNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BLangQueryAction} represents the do action statement in Ballerina.
 *
 * @since 1.2.0
 */
public class BLangQueryAction extends BLangStatement implements QueryActionNode {

    public List<BLangFromClause> fromClauseList = new ArrayList<>();
    public List<BLangWhereClause> whereClauseList = new ArrayList<>();
    public BLangDoClause doClause;

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.DO_ACTION;
    }

    @Override
    public List<? extends FromClauseNode> getFromClauseNodes() {
        return fromClauseList;
    }

    @Override
    public void addFromClauseNode(FromClauseNode fromClauseNode) {
        fromClauseList.add((BLangFromClause) fromClauseNode);
    }

    @Override
    public List<? extends BLangWhereClause> getWhereClauseNode() {
        return whereClauseList;
    }

    @Override
    public void addWhereClauseNode(WhereClauseNode whereClauseNode) {
        this.whereClauseList.add((BLangWhereClause) whereClauseNode);
    }

    @Override
    public DoClauseNode getDoClauseNode() {
        return doClause;
    }

    @Override
    public void setDoClauseNode(DoClauseNode doClauseNode) {
        this.doClause = (BLangDoClause) doClauseNode;
    }
}

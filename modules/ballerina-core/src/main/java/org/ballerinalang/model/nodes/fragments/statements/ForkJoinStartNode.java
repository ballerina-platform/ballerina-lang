/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.model.nodes.fragments.statements;

import org.ballerinalang.model.LinkedNodeVisitor;
import org.ballerinalang.model.nodes.AbstractLinkedNode;
import org.ballerinalang.model.statements.ForkJoinStmt;

/**
 * Represents start of an ForkJoin Statement, where actual forJoin happens.
 */
public class ForkJoinStartNode extends AbstractLinkedNode {

    private ForkJoinStmt statement;

    public ForkJoinStartNode(ForkJoinStmt stmt) {
        this.statement = stmt;
        this.parent = stmt;
    }

    public ForkJoinStmt getStatement() {
        return statement;
    }

    @Override
    public void accept(LinkedNodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }
}

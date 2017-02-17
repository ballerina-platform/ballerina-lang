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
package org.ballerinalang.model.nodes.fragments.expressions;

import org.ballerinalang.model.LinkedNodeVisitor;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.nodes.AbstractLinkedNode;

/**
 * Represents node Where ConnectorInitExpr ends.
 */
public class ConnectorInitExprEndNode extends AbstractLinkedNode {

    private ConnectorInitExpr expression;
    private int retuningBranchID;
    private boolean hasReturningBranch;

    public ConnectorInitExprEndNode(ConnectorInitExpr expression) {
        this.expression = expression;
        this.parent = expression;
    }

    public ConnectorInitExpr getExpression() {
        return expression;
    }

    @Override
    public void accept(LinkedNodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }
    public int getGotoBranchID() {
        return retuningBranchID;
    }

    public void setGotoBranchID(int retuningBranchID) {
        this.retuningBranchID = retuningBranchID;
    }

    public boolean hasGotoBranchID() {
        return hasReturningBranch;
    }

    public void setHasGotoBranchID(boolean hasReturningBranch) {
        this.hasReturningBranch = hasReturningBranch;
    }

}

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
package org.ballerinalang.model.nodes;

import org.ballerinalang.model.LinkedNode;
import org.ballerinalang.model.LinkedNodeVisitor;
import org.ballerinalang.util.exceptions.FlowBuilderException;

import java.util.ArrayList;

/**
 * Represents a the multiple target branching points in linked execution.
 * Actual target will be derived at runtime.
 */
public class GotoNode extends AbstractLinkedNode {

    // TODO : Think a way to remove ArrayList.
    private ArrayList<LinkedNode> linkedNodes;

    public GotoNode() {
        linkedNodes = new ArrayList<>();
    }

    /**
     * Get next Linked node for given branchID.
     *
     * @param branchID of the next linked node.
     * @return next Linked node.
     */
    public LinkedNode next(int branchID) {
        return linkedNodes.get(branchID);
    }

    /**
     * Add next Linked node.
     *
     * @param node next Linked node.
     * @return id for next Linked node.
     */
    public int addNext(LinkedNode node) {
        linkedNodes.add(node);
        // Return the branch LinkID.
        return linkedNodes.size() - 1;
    }

    @Override
    public void setNext(LinkedNode statement) {
        throw new FlowBuilderException("Internal Error. Goto Node can't have next.");
    }

    @Override
    public LinkedNode next() {
        throw new FlowBuilderException("Internal Error. Goto Node doesn't have next.");
    }

    @Override
    public void accept(LinkedNodeVisitor nodeVisitor) {
        nodeVisitor.visit(this);
    }
}

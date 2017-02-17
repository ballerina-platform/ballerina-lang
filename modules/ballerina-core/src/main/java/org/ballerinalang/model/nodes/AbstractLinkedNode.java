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
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;

/**
 * Abstract implementation of the Linked node.
 *
 * @since 0.8.0
 */
public abstract class AbstractLinkedNode implements LinkedNode {

    public LinkedNode next;
    protected LinkedNode sibling, parent;

    @Override
    public LinkedNode next() {
        return next;
    }

    @Override
    public void setNext(LinkedNode linkedNode) {
        // Validation for incorrect Linking.
        if (next != null && next != linkedNode && !next.getClass().equals(linkedNode.getClass())) {
            throw new IllegalStateException(this.getClass() + " got different next." + next + " " + linkedNode);
        }
        this.next = linkedNode;
    }

    @Override
    public LinkedNode getNextSibling() {
        return sibling;
    }

    @Override
    public void setNextSibling(LinkedNode linkedNode) {
        this.sibling = linkedNode;
    }

    @Override
    public LinkedNode getParent() {
        return parent;
    }

    @Override
    public void setParent(LinkedNode linkedNode) {
        this.parent = linkedNode;
    }

    @Override
    public NodeLocation getNodeLocation() {
        return null;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        this.accept((LinkedNodeVisitor) visitor);
    }

    public abstract void accept(LinkedNodeVisitor visitor);
}

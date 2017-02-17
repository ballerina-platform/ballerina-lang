/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.expressions;

import org.ballerinalang.model.LinkedNode;
import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.FlowBuilderException;

/**
 * All the expressions should extend {@code AbstractExpression}
 * <p>
 * Provides the common behaviour of expressions.
 *
 * @since 0.8.0
 */
public abstract class AbstractExpression implements Expression {
    protected NodeLocation location;
    protected BType type;

    public LinkedNode next;
    private LinkedNode sibling, parent;
    protected boolean multipleReturnsAvailable;
    protected int offset;

    // Non-Blocking Implementation related fields.
    private int tempOffset;
    private boolean isTempOffsetSet = false;

    public AbstractExpression(NodeLocation location) {
        this.location = location;
    }

    public BType getType() {
        return type;
    }

    public void setType(BType type) {
        this.type = type;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isMultiReturnExpr() {
        return multipleReturnsAvailable;
    }

    public BValue execute(NodeExecutor executor) {
        return null;
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }

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
    public int getTempOffset() {
        return tempOffset;
    }

    @Override
    public void setTempOffset(int index) {
        if (isTempOffsetSet && index != tempOffset) {
            throw new FlowBuilderException("Internal Error. Attempt to Overwrite tempOffset. current :" + tempOffset +
                    ", new :" + index);
        }
        isTempOffsetSet = true;
        this.tempOffset = index;
    }

    @Override
    public boolean hasTemporaryValues() {
        return isTempOffsetSet;
    }
}

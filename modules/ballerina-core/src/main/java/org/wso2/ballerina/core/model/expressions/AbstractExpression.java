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
package org.wso2.ballerina.core.model.expressions;

import org.wso2.ballerina.core.exception.LinkerException;
import org.wso2.ballerina.core.model.NodeExecutor;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.nodes.AbstractLinkedNode;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * All the expressions should extend {@code AbstractExpression}
 * <p>
 * Provides the common behaviour of expressions.
 *
 * @since 0.8.0
 */
public abstract class AbstractExpression extends AbstractLinkedNode implements Expression {
    protected NodeLocation location;
    protected BType type;
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
    public int getTempOffset() {
        if (isTempOffsetSet) {
            return tempOffset;
        }
        throw new LinkerException("Internal Error. Set Temporary value before you access it.");
    }

    @Override
    public void setTempOffset(int index) {
        if (isTempOffsetSet && index != tempOffset) {
            throw new LinkerException("Internal Error. Attempt to Overwrite tempOffset. current :" + tempOffset +
                    ", new :" + index);
        }
        isTempOffsetSet = true;
        this.tempOffset = index;
    }
}

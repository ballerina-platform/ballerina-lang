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
package org.ballerinalang.core.model.expressions;

import org.ballerinalang.core.model.NodeLocation;
import org.ballerinalang.core.model.WhiteSpaceDescriptor;
import org.ballerinalang.core.model.types.BType;

/**
 * All the expressions should extend {@code AbstractExpression}
 * <p>
 * Provides the common behavior of expressions.
 *
 * @since 0.8.0
 */
public abstract class AbstractExpression implements Expression {
    protected NodeLocation location;
    protected WhiteSpaceDescriptor whiteSpaceDescriptor;
    protected BType type;

    public AbstractExpression(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.location = location;
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }

    public BType getType() {
        return type;
    }

    public void setType(BType type) {
        this.type = type;
    }

    @Override
    public NodeLocation getNodeLocation() {
        return location;
    }

    public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }

    @Override
    public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
        return whiteSpaceDescriptor;
    }
}

/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.model.statements;

import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.WhiteSpaceDescriptor;

/**
 * Represents an abstract statement. All statements nodes extends this abstract class.
 * <p>
 * A statement is a tree consisting of one or more of the concrete implementations
 * of {@link Statement} interface.
 *
 * @since 0.8.0
 */
public abstract class AbstractStatement implements Statement {
    protected NodeLocation location;
    protected WhiteSpaceDescriptor whiteSpaceDescriptor;
    protected boolean alwaysReturns;
    protected Statement parent;

    public AbstractStatement(NodeLocation location) {
        this.location = location;
    }

    public NodeLocation getNodeLocation() {
        return location;
    }

    public void setLocation(NodeLocation location) {
        this.location = location;
    }

    public void setWhiteSpaceDescriptor(WhiteSpaceDescriptor whiteSpaceDescriptor) {
        this.whiteSpaceDescriptor = whiteSpaceDescriptor;
    }

    @Override
    public WhiteSpaceDescriptor getWhiteSpaceDescriptor() {
        return whiteSpaceDescriptor;
    }

    public void setAlwaysReturns(boolean alwaysReturns) {
        this.alwaysReturns = alwaysReturns;
    }

    public boolean isAlwaysReturns() {
        return alwaysReturns;
    }

    @Override
    public void setParent(Statement parent) {
        this.parent = parent;
    }

    @Override
    public Statement getParent() {
        return this.parent;
    }

}

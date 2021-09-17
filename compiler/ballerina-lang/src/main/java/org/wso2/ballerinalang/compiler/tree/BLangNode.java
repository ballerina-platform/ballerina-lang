/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.tree.Node;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * @since 0.94
 */
public abstract class BLangNode implements Node, Cloneable {

    protected BType type;
    public BLangNode parent = null;

    /**
     * Indicates whether the node has already been desugared.
     */
    public boolean desugared;

    /**
     * Indicates whether the node has already been propagated.
     */
    public boolean constantPropagated;

    /**
     * Indicates whether this node is part of how a construct in the source code is modeled internally.
     */
    public boolean internal;

    /**
     * The location of this node in the source file.
     */
    public Location pos;

    /*
     * Following fields will be used for AST Cloning.
     */
    public BLangNode cloneRef;
    public int cloneAttempt;

    /**
     * The actual type derived for this expression.
     */
    protected BType determinedType;

    /**
     * Sets the specified type as the type of the node.
     *
     * @param type The type of the node
     */
    public void setBType(BType type) {
        this.type = type;
        this.determinedType = type;
    }

    /**
     * Gets the type of the node.
     *
     * @return The type of the node
     */
    public BType getBType() {
        return this.type;
    }

    public void setDeterminedType(BType type) {
        this.determinedType = type;
    }

    public BType getDeterminedType() {
        return this.determinedType;
    }

    public Location getPosition() {
        return pos;
    }

    public void setPosition(Location location) {
        this.pos = location;
    }

    public abstract void accept(BLangNodeVisitor visitor);

    public abstract <T> void accept(BLangNodeAnalyzer<T> analyzer, T props);

    public abstract <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props);

}

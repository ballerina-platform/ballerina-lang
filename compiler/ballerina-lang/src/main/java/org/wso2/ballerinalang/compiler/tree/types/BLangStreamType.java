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
package org.wso2.ballerinalang.compiler.tree.types;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.types.StreamTypeNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * @since 1.2.0
 */
public class BLangStreamType extends BLangType implements StreamTypeNode {
    public BLangType type;
    public BLangType constraint;
    public BLangType error;

    public BLangStreamType() {
    }

    public BLangStreamType(BLangType type, BLangType constraint, BLangType error) {
        this.type = type;
        this.constraint = constraint;
        this.error = error;
    }

    @Override
    public BLangType getType() {
        return type;
    }

    @Override
    public BLangType getConstraint() {
        return constraint;
    }

    @Override
    public TypeNode getError() {
        return error;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.type.toString() + "<" + this.constraint.toString() + ((this.error == null) ? ">" :
                this.error.toString() + ">");
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.STREAM_TYPE;
    }

}

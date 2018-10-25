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
package org.wso2.ballerinalang.compiler.tree.types;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.types.ErrorTypeNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

import java.util.Optional;

/**
 * Implementation of {@link ErrorTypeNode}.
 *
 *
 * @since 0.983.0
 */
public class BLangErrorType extends BLangType implements ErrorTypeNode {
    public BLangType reasonType;
    public BLangType detailType;

    public BLangErrorType() {
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return this.type.toString() + "<" + this.reasonType.toString() + Optional.ofNullable(detailType)
                .map(type -> "," + type.toString()) + ">";
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ERROR_TYPE;
    }

    @Override
    public TypeNode getReasonTypeNode() {
        return reasonType;
    }

    @Override
    public TypeNode getDetailsTypeNode() {
        return detailType;
    }
}

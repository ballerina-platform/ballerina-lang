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

package org.wso2.ballerinalang.compiler.tree.expressions;

import io.ballerina.runtime.util.exceptions.BallerinaException;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.types.TypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;


/**
 * Represents the object-constructor-expr.
 *
 * @since slp3
 */
public class BLangObjectConstructorExpression extends BLangExpression {

    public BLangClassDefinition classNode;
    public BLangTypeInit typeInit;
    public BLangType referenceType;
    public boolean isClient;

    public BLangObjectConstructorExpression() {
        super();
        this.isClient = false;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the kind of this node.
     *
     * @return the kind of this node.
     */
    @Override
    public NodeKind getKind() {
        return NodeKind.OBJECT_CTOR_EXPRESSION;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        if (isClient) {
            sb.append("client ");
        }
        sb.append("object ");
        if (referenceType != null && referenceType.type.name != null) {
            sb.append(referenceType.type.name.getValue());
        }
        sb.append(" {");
        sb.append(this.classNode.toString());
        sb.append("};\n");
        return sb.toString();
    }

    /**
     * Add a type reference.
     *
     * @param type Type that is referenced by this type.
     */
    public void addTypeReference(TypeNode type) {
        if (this.referenceType == null) {
            this.referenceType = (BLangType) type;
            this.classNode.addTypeReference(type);
            return;
        }
        throw new BallerinaException("object-constructor-expr can only have one type-reference");
    }
}

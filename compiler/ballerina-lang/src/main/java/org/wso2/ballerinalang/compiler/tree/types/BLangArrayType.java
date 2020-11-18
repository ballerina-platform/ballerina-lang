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
import org.ballerinalang.model.tree.types.ArrayTypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.Arrays;
import java.util.Collections;

/**
 * @since 0.94
 */
public class BLangArrayType extends BLangType implements ArrayTypeNode {
    public BLangType elemtype;

    public int dimensions;

    public BLangExpression[] sizes = new BLangExpression[0];

    public BLangArrayType() {
    }

    @Override
    public BLangType getElementType() {
        return elemtype;
    }

    @Override
    public int getDimensions() {
        return dimensions;
    }

    @Override
    public BLangExpression[] getSizes() {
        return sizes;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        final StringBuilder[] sb = {new StringBuilder(getTypeName())};
        if (sizes.length == 0) {
            Arrays.stream(sizes).forEach(size -> {
                if (size.getKind() == NodeKind.NUMERIC_LITERAL) {
                    Integer sizeIndicator = (Integer) (((BLangLiteral) size).getValue());
                    if (sizeIndicator == -1) {
                        sb[0].append("[]");
                    } else {
                        sb[0].append("[").append(sizeIndicator).append("]");
                    }
                } else {
                    sb[0].append("[").append(((BLangSimpleVarRef) size).variableName).append("]");
                }
            });
        } else {
            sb[0].append(String.join("", Collections.nCopies(dimensions, "[]")));
        }
        return sb[0].toString();
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.ARRAY_TYPE;
    }

    private String getTypeName() {
        return (elemtype instanceof BLangUserDefinedType ?
                ((BLangUserDefinedType) elemtype).typeName.value : elemtype.toString());
    }
}

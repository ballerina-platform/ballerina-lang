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
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since 0.94
 */
public class BLangArrayType extends BLangType implements ArrayTypeNode {

    // BLangNodes
    public BLangType elemtype;
    public List<BLangExpression> sizes = new ArrayList<>();

    public int inferredArrayValidateState = 0;  // (-1) - Invalid usage (not in correct context)
                                                // 0 - Not a INFERRED Array
                                                // 1 - Need to validate the initializer (In correct context)
                                                // 2 - Valid usage

    // Parser Flags and Data
    public int dimensions;

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
        return sizes.toArray(new BLangExpression[0]);
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
        analyzer.visit(this, props);
    }

    @Override
    public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
        return modifier.transform(this, props);
    }

    @Override
    public String toString() {
        final StringBuilder[] sb = {new StringBuilder(getTypeName())};
        if (sizes.size() == 0) {
            sizes.forEach(size -> {
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

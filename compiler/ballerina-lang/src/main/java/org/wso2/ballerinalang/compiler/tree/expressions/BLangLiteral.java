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
package org.wso2.ballerinalang.compiler.tree.expressions;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * @since 0.94
 */
public class BLangLiteral extends BLangExpression implements LiteralNode {

    // Original values created at parser builder.
    public BType literalType;
    public Object literalValue;

    public Object value;
    public String originalValue;
    public boolean isJSONContext;
    public boolean isFiniteContext;
    public boolean isConstant;

    public BLangLiteral() {
    }

    public BLangLiteral(Object value, BType type) {
        this.value = value;
        this.type = this.literalType = type;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String getOriginalValue() {
        return originalValue;
    }

    @Override
    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    @Override
    public NodeKind getKind() {
        return NodeKind.LITERAL;
    }

    @Override
    public String toString() {
        // To distinguish between `()` and `null`
        return value == null ? originalValue : String.valueOf(value);
    }

}

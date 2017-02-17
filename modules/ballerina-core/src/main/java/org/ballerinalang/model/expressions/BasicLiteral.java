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
package org.ballerinalang.model.expressions;

import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;

/**
 * {@code BasicLiteral} represents a basic literal in Ballerina.
 * <p>
 * This class is used to hold IntegerLiterals, FloatingPointLiterals, QuotedStringLiterals, BooleanLiterals and 'nil'
 *
 * @since 0.8.0
 */
public class BasicLiteral extends AbstractExpression {
    private SimpleTypeName typeName;
    private BValueType bValue;

    public BasicLiteral(NodeLocation location, BValueType bValueType) {
        super(location);
        this.bValue = bValueType;
    }

    public BasicLiteral(NodeLocation location, SimpleTypeName typeName, BValueType bValue) {
        super(location);
        this.bValue = bValue;
        this.typeName = typeName;
    }

    public SimpleTypeName getTypeName() {
        return typeName;
    }

    public BValueType getBValue() {
        return bValue;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public BValue execute(NodeExecutor executor) {
        return executor.visit(this);
    }

}

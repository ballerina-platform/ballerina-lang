/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.expressions;

import org.ballerinalang.model.ExecutableMultiReturnExpr;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.SimpleTypeName;

/**
 * Class to hold the data related to type conversion expression.
 *
 * @since 0.88
 */
public class TypeConversionExpr extends AbstractExpression implements ExecutableMultiReturnExpr {

    private SimpleTypeName typeName;
    private Expression rExpr;
    private BType[] types = new BType[0];
    private int opcode;

    private int[] offsets;

    public TypeConversionExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, 
            SimpleTypeName typeName, Expression rExpr) {
        super(location, whiteSpaceDescriptor);
        this.rExpr = rExpr;
        this.typeName = typeName;
    }

    public Expression getRExpr() {
        return rExpr;
    }

    public SimpleTypeName getTypeName() {
        return typeName;
    }

    public int getOpcode() {
        return opcode;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns an arrays of argument types of this callable unit invocation expression.
     *
     * @return an arrays of argument types
     */
    @Override
    public BType[] getTypes() {
        return types;
    }

    /**
     * Sets an arrays of argument types.
     *
     * @param types arrays of argument types
     */
    @Override
    public void setTypes(BType[] types) {
        this.types = types;

        multipleReturnsAvailable = types.length > 1;
        if (!multipleReturnsAvailable && types.length == 1) {
            this.type = types[0];
        }
    }

    @Override
    public int[] getOffsets() {
        return offsets;
    }

    @Override
    public void setOffsets(int[] offsets) {
        this.offsets = offsets;
        setTempOffset(offsets[0]);
    }

    @Override
    public void setMultiReturnAvailable(boolean multiReturnsAvailable) {
        this.multipleReturnsAvailable = multiReturnsAvailable;
    }
}

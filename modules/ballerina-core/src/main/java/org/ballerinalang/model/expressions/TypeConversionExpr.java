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
import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.WhiteSpaceDescriptor;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.typemappers.TriFunction;

/**
 * Class to hold the data related to type conversion expression.
 *
 * @since 0.88
 */
public class TypeConversionExpr extends AbstractExpression implements ExecutableMultiReturnExpr {

    private SimpleTypeName typeName;
    private Expression rExpr;
    protected TriFunction<BValue, BType, Boolean, BValue[]> evalFunc;
    private BType[] types = new BType[0];
    private int opcode;

    private int[] offsets;

    public TypeConversionExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, Expression rExpr,
                              BType targetType) {
        super(location, whiteSpaceDescriptor);
        this.rExpr = rExpr;
        this.type = targetType;
    }

    public TypeConversionExpr(NodeLocation location, WhiteSpaceDescriptor whiteSpaceDescriptor, 
            SimpleTypeName typeName, Expression rExpr) {
        super(location, whiteSpaceDescriptor);
        this.rExpr = rExpr;
        this.typeName = typeName;
    }

    public TriFunction<BValue, BType, Boolean, BValue[]> getEvalFunc() {
        return evalFunc;
    }

    public void setEvalFunc(TriFunction<BValue, BType, Boolean, BValue[]> evalFunc) {
        this.evalFunc = evalFunc;
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

    @Override
    public BValue execute(NodeExecutor executor) {
        return executor.visit(this)[0];
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
    }

    /**
     * Executes and Returns all the results of this expression.
     *
     * @param executor instance of a {@code NodeExecutor}
     * @return results of this expression
     */
    @Override
    public BValue[] executeMultiReturn(NodeExecutor executor) {
        return executor.visit(this);
    }
    
    @Override
    public void setMultiReturnAvailable(boolean multiReturnsAvailable) {
        this.multipleReturnsAvailable = multiReturnsAvailable;
    }
}

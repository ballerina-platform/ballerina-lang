/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import io.ballerina.tools.diagnostics.Location;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * New large array instruction modeled as BIR NonTerminator JInstruction.
 *
 * @since 2201.8.0
 */
public class JLargeArrayInstruction extends JInstruction {

    public BIROperand typedescOp;
    public BIROperand sizeOp;
    public BType type;
    public BIROperand values;

    public JLargeArrayInstruction(Location location, BType type, BIROperand lhsOp, BIROperand sizeOp,
                                  BIROperand values) {
        super(location);
        jKind = JInsKind.LARGE_ARRAY;
        this.type = type;
        this.lhsOp = lhsOp;
        this.sizeOp = sizeOp;
        this.values = values;
    }

    public JLargeArrayInstruction(Location location, BType type, BIROperand lhsOp, BIROperand typedescOp,
                                  BIROperand sizeOp, BIROperand values) {
        this(location, type, lhsOp, sizeOp, values);
        this.typedescOp = typedescOp;
    }

    @Override
    public BIROperand[] getRhsOperands() {
        if (typedescOp != null) {
            return new BIROperand[]{typedescOp, sizeOp, values};
        } else {
            return new BIROperand[]{sizeOp, values};
        }
    }

    @Override
    public void setRhsOperands(BIROperand[] operands) {
        if (operands.length == 3) {
            this.typedescOp = operands[0];
            this.sizeOp = operands[1];
            this.values = operands[2];
        } else {
            this.sizeOp = operands[0];
            this.values = operands[1];
        }
    }

    @Override
    public void accept(BIRVisitor visitor) {
        visitor.visit(this);
    }

}

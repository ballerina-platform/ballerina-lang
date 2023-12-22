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

/**
 * New large map instruction modeled as BIR NonTerminator JInstruction.
 *
 * @since 2201.8.0
 */
public class JLargeMapInstruction extends JInstruction {

    public BIROperand rhsOp;
    public BIROperand initialValues;

    public JLargeMapInstruction(Location pos, BIROperand lhsOp, BIROperand rhsOp, BIROperand initialValues) {
        super(pos);
        jKind = JInsKind.LARGE_MAP;
        this.lhsOp = lhsOp;
        this.rhsOp = rhsOp;
        this.initialValues = initialValues;
    }

    @Override
    public BIROperand[] getRhsOperands() {
        return new BIROperand[]{rhsOp, initialValues};
    }

    @Override
    public void setRhsOperands(BIROperand[] operands) {
        this.rhsOp = operands[0];
        this.initialValues = operands[1];
    }

    @Override
    public void accept(BIRVisitor visitor) {
        visitor.visit(this);
    }

}

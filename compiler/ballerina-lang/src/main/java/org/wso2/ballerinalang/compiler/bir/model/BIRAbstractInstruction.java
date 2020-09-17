/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.model;

import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Abstract instruction with an lhs-operand.
 *
 * @since 1.0
 */
public abstract class BIRAbstractInstruction extends BIRNode implements BIRInstruction {

    public InstructionKind kind;
    public BIROperand lhsOp;
    public BirScope scope;

    BIRAbstractInstruction(DiagnosticPos pos, InstructionKind kind) {
        super(pos);
        this.kind = kind;
    }

    @Override
    public InstructionKind getKind() {
        return this.kind;
    }

    public abstract BIROperand[] getRhsOperands();
}

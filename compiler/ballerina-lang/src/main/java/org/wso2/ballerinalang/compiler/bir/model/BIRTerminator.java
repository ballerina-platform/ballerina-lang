/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Terminators connects basic blocks together.
 * <p>
 * Each terminating instruction terminates a basic block.
 *
 * @since 0.980.0
 */
public abstract class BIRTerminator extends BIRNode implements BIRInstruction {

    /**
     * A goto instruction.
     * <p>
     * e.g., goto BB2
     *
     * @since 0.980.0
     */
    public static class GOTO extends BIRTerminator {
        public BIRBasicBlock bb;

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }


    /**
     * A function call instruction.
     * <p>
     * e.g., _4 = call doSomething _1 _2 _3
     *
     * @since 0.980.0
     */
    public static class Call extends BIRTerminator implements BIRAssignInstruction {
        public BIROperand.BIRVarRef lhsOp;
        public InstructionKind kind;
        public BIROperand[] args;

        @Override
        public BIROperand.BIRVarRef getLhsOperand() {
            return lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A return instruction.
     * <p>
     * e.g., return _4
     *
     * @since 0.980.0
     */
    public static class Return extends BIRTerminator {
        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }
}

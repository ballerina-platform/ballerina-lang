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

import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * A non-terminating instruction.
 * <p>
 * Non-terminating instructions do not terminate a basic block.
 *
 * @since 0.980.0
 */
public abstract class BIRNonTerminator extends BIRNode implements BIRInstruction {

    public InstructionKind kind;

    BIRNonTerminator(InstructionKind kind) {
        this.kind = kind;
    }

    /**
     * A move instruction that copy a value from variable to a temp location, vice versa.
     * <p>
     * e.g., _1 = move _2
     *
     * @since 0.980.0
     */
    public static class Move extends BIRNonTerminator implements BIRAssignInstruction {
        public BIROperand lhsOp;
        public org.wso2.ballerinalang.compiler.bir.model.BIROperand rhsOp;

        public Move(org.wso2.ballerinalang.compiler.bir.model.BIROperand fromOperand, BIROperand toOperand) {
            super(InstructionKind.MOVE);
            this.rhsOp = fromOperand;
            this.lhsOp = toOperand;
        }

        @Override
        public BIROperand getLhsOperand() {
            return lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A binary operator instruction.
     * <p>
     * e.g., _1 = add _2 _3
     *
     * @since 0.980.0
     */
    public static class BinaryOp extends BIRNonTerminator implements BIRAssignInstruction {
        public BIROperand lhsOp;
        public org.wso2.ballerinalang.compiler.bir.model.BIROperand rhsOp1;
        public org.wso2.ballerinalang.compiler.bir.model.BIROperand rhsOp2;

        public BinaryOp(InstructionKind kind,
                        BType type,
                        BIROperand lhsOp,
                        org.wso2.ballerinalang.compiler.bir.model.BIROperand rhsOp1,
                        org.wso2.ballerinalang.compiler.bir.model.BIROperand rhsOp2) {
            super(kind);
            this.lhsOp = lhsOp;
            this.rhsOp1 = rhsOp1;
            this.rhsOp2 = rhsOp2;
        }

        @Override
        public BIROperand getLhsOperand() {
            return lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A unary operator instruction.
     * <p>
     * e.g., _1 = minus _2
     *
     * @since 0.980.0
     */
    public static class UnaryOP extends BIRNonTerminator implements BIRAssignInstruction {
        public BIROperand lhsOp;

        public UnaryOP(InstructionKind kind, BIROperand lhsOp) {
            super(kind);
            this.lhsOp = lhsOp;
        }

        @Override
        public BIROperand getLhsOperand() {
            return lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A constant value load instruction.
     * <p>
     * e.g., _1 = const 10 (int)
     *
     * @since 0.980.0
     */
    public static class ConstantLoad extends BIRNonTerminator implements BIRAssignInstruction {
        public BIROperand lhsOp;
        public Object value;
        public BType type;

        public ConstantLoad(Object value, BType type, BIROperand lhsOp) {
            super(InstructionKind.CONST_LOAD);
            this.value = value;
            this.type = type;
            this.lhsOp = lhsOp;
        }

        @Override
        public BIROperand getLhsOperand() {
            return lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }
}

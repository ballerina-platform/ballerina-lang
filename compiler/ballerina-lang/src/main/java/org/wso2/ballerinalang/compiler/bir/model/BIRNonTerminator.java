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

import org.ballerinalang.model.tree.OperatorKind;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand.BIRVarRef;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * A non-terminating instruction.
 * <p>
 * Non-terminating instructions do not terminate a basic block.
 *
 * @since 0.980.0
 */
public abstract class BIRNonTerminator implements BIRInstruction {


    /**
     * A move instruction that copy a value from variable to a temp location, vice versa.
     * <p>
     * e.g., _1 = move _2
     *
     * @since 0.980.0
     */
    public static class Move extends BIRNonTerminator implements BIRAssignInstruction {
        public BIRVarRef lhsOp;
        public InstructionKind kind;
        public BIROperand rhsOp;

        public Move(BIROperand fromOperand, BIRVarRef toOperand) {
            this.rhsOp = fromOperand;
            this.lhsOp = toOperand;
        }

        @Override
        public BIRVarRef getLhsOperand() {
            return lhsOp;
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
        public OperatorKind binaryOpKind;
        public BType type;
        public BIRVarRef lhsOp;
        public BIROperand rhsOp1;
        public BIROperand rhsOp2;
        public InstructionKind instructionKind;

        public BinaryOp(OperatorKind binaryOpKind,
                        BType type,
                        BIRVarRef lhsOp,
                        BIROperand rhsOp1,
                        BIROperand rhsOp2) {
            this.binaryOpKind = binaryOpKind;
            this.type = type;
            this.lhsOp = lhsOp;
            this.rhsOp1 = rhsOp1;
            this.rhsOp2 = rhsOp2;
        }

        @Override
        public BIRVarRef getLhsOperand() {
            return lhsOp;
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
        public BIRVarRef lhsOp;
        public InstructionKind kind;
        public BIROperand rhsOp;

        @Override
        public BIRVarRef getLhsOperand() {
            return lhsOp;
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
        public BIRVarRef lhsOp;
        public InstructionKind kind;
        public BIROperand rhsOp;

        @Override
        public BIRVarRef getLhsOperand() {
            return lhsOp;
        }
    }
}

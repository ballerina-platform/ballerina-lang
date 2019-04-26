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
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * A non-terminating instruction.
 * <p>
 * Non-terminating instructions do not terminate a basic block.
 *
 * @since 0.980.0
 */
public abstract class BIRNonTerminator extends BIRNode implements BIRInstruction {

    public InstructionKind kind;

    BIRNonTerminator(DiagnosticPos pos, InstructionKind kind) {
        super(pos);
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
        public BIROperand rhsOp;

        public Move(DiagnosticPos pos, BIROperand fromOperand, BIROperand toOperand) {
            super(pos, InstructionKind.MOVE);
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
        public BIROperand rhsOp1;
        public BIROperand rhsOp2;

        public BinaryOp(DiagnosticPos pos,
                        InstructionKind kind,
                        BType type,
                        BIROperand lhsOp,
                        BIROperand rhsOp1,
                        BIROperand rhsOp2) {
            super(pos, kind);
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

        public UnaryOP(DiagnosticPos pos, InstructionKind kind, BIROperand lhsOp) {
            super(pos, kind);
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

        public ConstantLoad(DiagnosticPos pos, Object value, BType type, BIROperand lhsOp) {
            super(pos, InstructionKind.CONST_LOAD);
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

    /**
     * A new map instruction.
     * <p>
     * e.g., map a = {}
     *
     * @since 0.980.0
     */
    public static class NewStructure extends BIRNonTerminator {
        public BIROperand lhsOp;
        public BType type;

        public NewStructure(DiagnosticPos pos, BType type, BIROperand lhsOp) {
            super(pos, InstructionKind.NEW_STRUCTURE);
            this.type = type;
            this.lhsOp = lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A new instruction.
     * <p>
     * e.g., object{int i;}  a = new;
     *
     * @since 0.995.0
     */
    public static class NewInstance extends BIRNonTerminator {
        public BIRTypeDefinition def;
        public BIROperand lhsOp;

        public NewInstance(DiagnosticPos pos, BIRTypeDefinition def, BIROperand lhsOp) {
            super(pos, InstructionKind.NEW_INSTANCE);
            this.lhsOp = lhsOp;
            this.def = def;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A new array instruction.
     * <p>
     * e.g., int[] a = {}
     *
     * @since 0.980.0
     */
    public static class NewArray extends BIRNonTerminator {
        public BIROperand lhsOp;
        public BIROperand sizeOp;
        public BType type;

        public NewArray(DiagnosticPos pos, BType type, BIROperand lhsOp, BIROperand sizeOp) {
            super(pos, InstructionKind.NEW_ARRAY);
            this.type = type;
            this.lhsOp = lhsOp;
            this.sizeOp = sizeOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A field access expression.
     * <p>
     * e.g., a["b"] = 10 (int)
     * or
     * _1 = mapload _3 _2
     *
     * @since 0.980.0
     */
    public static class FieldAccess extends BIRNonTerminator {
        public BIROperand lhsOp;
        public BIROperand keyOp;
        public BIROperand rhsOp;

        public FieldAccess(DiagnosticPos pos, InstructionKind kind,
                           BIROperand lhsOp, BIROperand keyOp, BIROperand rhsOp) {
            super(pos, kind);
            this.lhsOp = lhsOp;
            this.keyOp = keyOp;
            this.rhsOp = rhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * An error constructor expression.
     * <p>
     * error(reason as string, detail as map)
     *
     * @since 0.995.0
     */
    public static class NewError extends BIRNonTerminator {

        public BIROperand lhsOp;

        public BIROperand reasonOp;

        public BIROperand detailOp;

        public NewError(DiagnosticPos pos, InstructionKind kind, BIROperand lhsOp,
                        BIROperand reasonOp, BIROperand detailOp) {
            super(pos, kind);
            this.lhsOp = lhsOp;
            this.reasonOp = reasonOp;
            this.detailOp = detailOp;
                        
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A type cast expression.
     * <p>
     * e.g., int a = cast(int) b;
     *
     * @since 0.980.0
     */
    public static class TypeCast extends BIRNonTerminator {
        public BIROperand lhsOp;
        public BIROperand rhsOp;

        public TypeCast(DiagnosticPos pos, BIROperand lhsOp, BIROperand rhsOp) {
            super(pos, InstructionKind.TYPE_CAST);
            this.lhsOp = lhsOp;
            this.rhsOp = rhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A is like instruction.
     * <p>
     * e.g., a isLike b
     *
     * @since 0.980.0
     */
    public static class IsLike extends BIRNonTerminator {
        public BIROperand lhsOp;
        public BIROperand rhsOp;
        public BType type;

        public IsLike(DiagnosticPos pos, BType type, BIROperand lhsOp, BIROperand rhsOp) {
            super(pos, InstructionKind.IS_LIKE);
            this.type = type;
            this.lhsOp = lhsOp;
            this.rhsOp = rhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A type test instruction.
     * <p>
     * e.g., a is int
     *
     * @since 0.980.0
     */
    public static class TypeTest extends BIRNonTerminator {
        public BIROperand lhsOp;
        public BIROperand rhsOp;
        public BType type;

        public TypeTest(DiagnosticPos pos, BType type, BIROperand lhsOp, BIROperand rhsOp) {
            super(pos, InstructionKind.TYPE_TEST);
            this.type = type;
            this.lhsOp = lhsOp;
            this.rhsOp = rhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * The new table instruction.
     * <p>
     * e.g. table<Employee> tbEmployee = table {
     *         { key id, name, salary },
     *         [ { 1, "Mary",  300.5 },
     *           { 2, "John",  200.5 },
     *           { 3, "Jim", 330.5 }
     *         ]
     *      };
     *
     * @since 0.995.0
     */
    public static class NewTable extends BIRNonTerminator {
        public BIROperand lhsOp;
        public BIROperand columnsOp;
        public BIROperand dataOp;
        public BIROperand indexColOp;
        public BIROperand keyColOp;
        public BType type;

        public NewTable(DiagnosticPos pos, BType type, BIROperand lhsOp, BIROperand columnsOp,
                        BIROperand dataOp, BIROperand indexColOp,
                        BIROperand keyColOp) {
            super(pos, InstructionKind.NEW_TABLE);
            this.type = type;
            this.lhsOp = lhsOp;
            this.columnsOp = columnsOp;
            this.dataOp = dataOp;
            this.indexColOp = indexColOp;
            this.keyColOp = keyColOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }
}

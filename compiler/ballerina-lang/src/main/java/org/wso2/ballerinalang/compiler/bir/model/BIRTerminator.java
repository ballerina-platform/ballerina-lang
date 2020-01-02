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

import org.ballerinalang.model.Name;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Terminators connects basic blocks together.
 * <p>
 * Each terminating instruction terminates a basic block.
 *
 * @since 0.980.0
 */
public abstract class BIRTerminator extends BIRAbstractInstruction implements BIRInstruction {

    public InstructionKind kind;

    public BIRBasicBlock thenBB;

    public BIRTerminator(DiagnosticPos pos, InstructionKind kind) {
        super(pos, kind);
        this.kind = kind;
    }

    @Override
    public InstructionKind getKind() {
        return this.kind;
    }

    /**
     * A goto instruction.
     * <p>
     * e.g., goto BB2
     *
     * @since 0.980.0
     */
    public static class GOTO extends BIRTerminator {

        public BIRBasicBlock targetBB;

        public GOTO(DiagnosticPos pos, BIRBasicBlock targetBB) {
            super(pos, InstructionKind.GOTO);
            this.targetBB = targetBB;
        }

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
        public boolean isVirtual;
        public List<BIROperand> args;
        public Name name;
        public PackageID calleePkg;

        public Call(DiagnosticPos pos,
                    InstructionKind kind,
                    boolean isVirtual,
                    PackageID calleePkg,
                    Name name,
                    List<BIROperand> args,
                    BIROperand lhsOp,
                    BIRBasicBlock thenBB) {
            super(pos, kind);
            this.lhsOp = lhsOp;
            this.isVirtual = isVirtual;
            this.args = args;
            this.thenBB = thenBB;
            this.name = name;
            this.calleePkg = calleePkg;
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
     * A async function call instruction.
     * <p>
     * e.g., _4 = callAsync doSomething _1 _2 _3
     *
     * @since 0.995.0
     */
    public static class AsyncCall extends Call {
        public List<BIRAnnotationAttachment> annotAttachments;

        public AsyncCall(DiagnosticPos pos,
                         InstructionKind kind,
                         boolean isVirtual,
                         PackageID calleePkg,
                         Name name,
                         List<BIROperand> args,
                         BIROperand lhsOp,
                         BIRBasicBlock thenBB,
                         List<BIRAnnotationAttachment> annotAttachments) {
            super(pos, kind, isVirtual, calleePkg, name, args, lhsOp, thenBB);
            this.annotAttachments = annotAttachments;
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
     * A function pointer invocation instruction.
     * <p>
     * e.g., _4 = fp.call();
     *
     * @since 0.995.0
     */
    public static class FPCall extends BIRTerminator {
        public BIROperand fp;
        public List<BIROperand> args;
        public boolean isAsync;

        public FPCall(DiagnosticPos pos,
                      InstructionKind kind,
                      BIROperand fp,
                      List<BIROperand> args,
                      BIROperand lhsOp,
                      boolean isAsync,
                      BIRBasicBlock thenBB) {
            super(pos, kind);
            this.fp = fp;
            this.lhsOp = lhsOp;
            this.args = args;
            this.isAsync = isAsync;
            this.thenBB = thenBB;
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

        public Return(DiagnosticPos pos) {
            super(pos, InstructionKind.RETURN);
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A branch instruction.
     * <p>
     * e.g., branch %4 [true:bb4, false:bb6]
     *
     * @since 0.980.0
     */
    public static class Branch extends BIRTerminator {
        public BIROperand op;
        public BIRBasicBlock trueBB;
        public BIRBasicBlock falseBB;

        public Branch(DiagnosticPos pos, BIROperand op, BIRBasicBlock trueBB, BIRBasicBlock falseBB) {
            super(pos, InstructionKind.BRANCH);
            this.op = op;
            this.trueBB = trueBB;
            this.falseBB = falseBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A lock instruction.
     * <p>
     * e.g., lock [#3, #0] bb6
     *
     * @since 0.990.4
     */
    public static class Lock extends BIRTerminator {
        public final BIRGlobalVariableDcl globalVar;
        public final BIRBasicBlock lockedBB;

        public Lock(DiagnosticPos pos, BIRGlobalVariableDcl globalVar, BIRBasicBlock lockedBB) {
            super(pos, InstructionKind.LOCK);
            this.globalVar = globalVar;
            this.lockedBB = lockedBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A lock instruction.
     * <p>
     * e.g., lock [#3, #0] bb6
     *
     * @since 0.990.4
     */
    public static class FieldLock extends BIRTerminator {
        public BIROperand localVar;
        public String field;
        public final BIRBasicBlock lockedBB;

        public FieldLock(DiagnosticPos pos, BIROperand localVar, String field, BIRBasicBlock lockedBB) {
            super(pos, InstructionKind.FIELD_LOCK);
            this.localVar = localVar;
            this.field = field;
            this.lockedBB = lockedBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * An unlock instruction.
     * <p>
     * e.g., unlock [#3, #0] bb8
     *
     * @since 0.990.4
     */
    public static class Unlock extends BIRTerminator {
        public final Set<BIRGlobalVariableDcl> globalVars;
        public final Map<BIROperand, Set<String>> fieldLocks;
        public final BIRBasicBlock unlockBB;

        public Unlock(DiagnosticPos pos, Set<BIRGlobalVariableDcl> globalVars,
                      Map<BIROperand, Set<String>> fieldLocks, BIRBasicBlock unlockBB) {
            super(pos, InstructionKind.UNLOCK);
            this.globalVars = globalVars;
            this.fieldLocks = fieldLocks;
            this.unlockBB = unlockBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A panic statement.
     * <p>
     * panic error
     *
     * @since 0.995.0
     */
    public static class Panic extends BIRTerminator {

        public BIROperand errorOp;

        public Panic(DiagnosticPos pos, BIROperand errorOp) {
            super(pos, InstructionKind.PANIC);
            this.errorOp = errorOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A wait instruction.
     * <p>
     * e.g., wait w1|w2;
     *
     * @since 0.995.0
     */
    public static class Wait extends BIRTerminator {
        public List<BIROperand> exprList;

        public Wait(DiagnosticPos pos, List<BIROperand> exprList, BIROperand lhsOp, BIRBasicBlock thenBB) {
            super(pos, InstructionKind.WAIT);
            this.exprList = exprList;
            this.lhsOp = lhsOp;
            this.thenBB = thenBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A flush instruction.
     * <p>
     * e.g., %5 = flush w1,w2;
     *
     * @since 0.995.0
     */
    public static class Flush extends BIRTerminator {
        public ChannelDetails[] channels;

        public Flush(DiagnosticPos pos, ChannelDetails[] channels, BIROperand lhsOp, BIRBasicBlock thenBB) {
            super(pos, InstructionKind.FLUSH);
            this.channels = channels;
            this.lhsOp = lhsOp;
            this.thenBB = thenBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A worker receive instruction.
     * <p>
     * e.g., WRK_RECEIVE w1;
     *
     * @since 0.995.0
     */
    public static class WorkerReceive extends BIRTerminator {
        public Name workerName;
        public boolean isSameStrand;

        public WorkerReceive(DiagnosticPos pos, Name workerName, BIROperand lhsOp,
                             boolean isSameStrand, BIRBasicBlock thenBB) {
            super(pos, InstructionKind.WK_RECEIVE);
            this.workerName = workerName;
            this.thenBB = thenBB;
            this.isSameStrand = isSameStrand;
            this.lhsOp = lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A worker send instruction.
     * <p>
     * e.g., %5 WRK_SEND w1;
     *
     * @since 0.995.0
     */
    public static class WorkerSend extends BIRTerminator {
        public Name channel;
        public BIROperand data;
        public boolean isSameStrand;
        public boolean isSync;

        public WorkerSend(DiagnosticPos pos, Name workerName, BIROperand data, boolean isSameStrand, boolean isSync,
                          BIROperand lhsOp, BIRBasicBlock thenBB) {
            super(pos, InstructionKind.WK_SEND);
            this.channel = workerName;
            this.data = data;
            this.thenBB = thenBB;
            this.lhsOp = lhsOp;
            this.isSameStrand = isSameStrand;
            this.isSync = isSync;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }

    /**
     * A wait all instruction.
     * <p>
     * e.g., record {id:w1,id2:w2} res = wait {w1, w2};
     *
     * @since 0.995.0
     */
    public static class WaitAll extends BIRTerminator {
        public List<String> keys;
        public List<BIROperand> valueExprs;

        public WaitAll(DiagnosticPos pos, BIROperand lhsOp, List<String> keys, List<BIROperand> valueExprs,
                       BIRBasicBlock thenBB) {
            super(pos, InstructionKind.WAIT_ALL);
            this.lhsOp = lhsOp;
            this.keys = keys;
            this.valueExprs = valueExprs;
            this.thenBB = thenBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }
    }
}

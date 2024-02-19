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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Terminators connects basic blocks together.
 * <p>
 * Each terminating instruction terminates a basic block.
 *
 * @since 0.980.0
 */
public abstract class BIRTerminator extends BIRAbstractInstruction implements BIRInstruction {

    public BIRBasicBlock thenBB;

    public BIRTerminator(Location pos, InstructionKind kind) {
        super(pos, kind);
        this.kind = kind;
    }

    public abstract BIRBasicBlock[] getNextBasicBlocks();

    /**
     * A goto instruction.
     * <p>
     * e.g., goto BB2
     *
     * @since 0.980.0
     */
    public static class GOTO extends BIRTerminator {

        public BIRBasicBlock targetBB;

        public GOTO(Location pos, BIRBasicBlock targetBB) {
            super(pos, InstructionKind.GOTO);
            this.targetBB = targetBB;
        }

        public GOTO(Location pos,
                    BIRBasicBlock targetBB,
                    BirScope scope) {
            this(pos, targetBB);
            this.scope = scope;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return new BIROperand[0];
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            // do nothing
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{targetBB};
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
        public List<BIRAnnotationAttachment> calleeAnnotAttachments;
        public Set<Flag> calleeFlags;

        public Call(Location pos,
                    InstructionKind kind,
                    boolean isVirtual,
                    PackageID calleePkg,
                    Name name,
                    List<BIROperand> args,
                    BIROperand lhsOp,
                    BIRBasicBlock thenBB,
                    List<BIRAnnotationAttachment> calleeAnnotAttachments,
                    Set<Flag> calleeFlags) {
            super(pos, kind);
            this.lhsOp = lhsOp;
            this.isVirtual = isVirtual;
            this.args = args;
            this.thenBB = thenBB;
            this.name = name;
            this.calleePkg = calleePkg;
            this.calleeAnnotAttachments = calleeAnnotAttachments;
            this.calleeFlags = calleeFlags;
        }

        public Call(Location pos,
                    InstructionKind kind,
                    boolean isVirtual,
                    PackageID calleePkg,
                    Name name,
                    List<BIROperand> args,
                    BIROperand lhsOp,
                    BIRBasicBlock thenBB,
                    List<BIRAnnotationAttachment> calleeAnnotAttachments,
                    Set<Flag> calleeFlags,
                    BirScope scope) {
            this(pos, kind, isVirtual, calleePkg, name, args, lhsOp, thenBB, calleeAnnotAttachments, calleeFlags);
            this.scope = scope;
        }

        @Override
        public BIROperand getLhsOperand() {
            return lhsOp;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return args.toArray(new BIROperand[0]);
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            this.args = List.of(operands);
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{thenBB};
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

        public AsyncCall(Location pos,
                         InstructionKind kind,
                         boolean isVirtual,
                         PackageID calleePkg,
                         Name name,
                         List<BIROperand> args,
                         BIROperand lhsOp,
                         BIRBasicBlock thenBB,
                         List<BIRAnnotationAttachment> annotAttachments,
                         List<BIRAnnotationAttachment> calleeAnnotAttachments,
                         Set<Flag> calleeFlags,
                         BirScope scope) {
            super(pos, kind, isVirtual, calleePkg, name, args, lhsOp, thenBB, calleeAnnotAttachments,
                    calleeFlags, scope);
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
        public boolean workerDerivative;

        public FPCall(Location pos,
                      InstructionKind kind,
                      BIROperand fp,
                      List<BIROperand> args,
                      BIROperand lhsOp,
                      boolean isAsync,
                      BIRBasicBlock thenBB,
                      BirScope scope) {
            super(pos, kind);
            this.fp = fp;
            this.lhsOp = lhsOp;
            this.args = args;
            this.isAsync = isAsync;
            this.thenBB = thenBB;
            this.scope = scope;
        }

        public FPCall(Location pos,
                      InstructionKind kind,
                      BIROperand fp,
                      List<BIROperand> args,
                      BIROperand lhsOp,
                      boolean isAsync,
                      BIRBasicBlock thenBB,
                      BirScope scope,
                      boolean workerDerivative) {
            this(pos, kind, fp, args, lhsOp, isAsync, thenBB, scope);
            this.workerDerivative = workerDerivative;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            BIROperand[] operands = new BIROperand[args.size() + 1];
            operands[0] = fp;
            int i = 1;
            for (BIROperand operand : args) {
                operands[i++] = operand;
            }
            return operands;
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            this.fp = operands[0];
            this.args = new ArrayList<>();
            this.args.addAll(Arrays.asList(operands).subList(1, operands.length));
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{thenBB};
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

        public Return(Location pos) {
            super(pos, InstructionKind.RETURN);
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return new BIROperand[0];
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            // do nothing
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[0];
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

        public Branch(Location pos, BIROperand op, BIRBasicBlock trueBB, BIRBasicBlock falseBB) {
            super(pos, InstructionKind.BRANCH);
            this.op = op;
            this.trueBB = trueBB;
            this.falseBB = falseBB;
        }

        public Branch(Location pos,
                      BIROperand op,
                      BIRBasicBlock trueBB,
                      BIRBasicBlock falseBB,
                      BirScope scope) {
            this(pos, op, trueBB, falseBB);
            this.scope = scope;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return new BIROperand[]{op};
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            this.op = operands[0];
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{trueBB, falseBB};
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
        public BIRBasicBlock lockedBB;

        public Set<BIRGlobalVariableDcl> lockVariables = new HashSet<>();

        public Integer lockId = -1;

        public Lock(Location pos, BIRBasicBlock lockedBB) {
            super(pos, InstructionKind.LOCK);
            this.lockedBB = lockedBB;
        }

        public Lock(Location pos,
                    BIRBasicBlock lockedBB,
                    BirScope scope) {
            this(pos, lockedBB);
            this.scope = scope;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return new BIROperand[0];
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            // do nothing
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{lockedBB};
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
        public BIRBasicBlock lockedBB;

        public FieldLock(Location pos, BIROperand localVar, String field, BIRBasicBlock lockedBB) {
            super(pos, InstructionKind.FIELD_LOCK);
            this.localVar = localVar;
            this.field = field;
            this.lockedBB = lockedBB;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return new BIROperand[]{localVar};
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            this.localVar = operands[0];
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{lockedBB};
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
        public BIRBasicBlock unlockBB;

        public BIRTerminator.Lock relatedLock;

        public Unlock(Location pos, BIRBasicBlock unlockBB) {
            super(pos, InstructionKind.UNLOCK);
            this.unlockBB = unlockBB;
        }

        public Unlock(Location pos, BIRBasicBlock unlockBB, BirScope scope) {
            this(pos, unlockBB);
            this.scope = scope;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return new BIROperand[0];
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            // do nothing
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{unlockBB};
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

        public Panic(Location pos, BIROperand errorOp) {
            super(pos, InstructionKind.PANIC);
            this.errorOp = errorOp;
        }

        public Panic(Location pos,
                     BIROperand errorOp,
                     BirScope scope) {
            this(pos, errorOp);
            this.scope = scope;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return new BIROperand[]{errorOp};
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            this.errorOp = operands[0];
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[0];
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

        public Wait(Location pos, List<BIROperand> exprList, BIROperand lhsOp, BIRBasicBlock thenBB) {
            super(pos, InstructionKind.WAIT);
            this.exprList = exprList;
            this.lhsOp = lhsOp;
            this.thenBB = thenBB;
        }

        public Wait(Location pos,
                    List<BIROperand> exprList,
                    BIROperand lhsOp,
                    BIRBasicBlock thenBB,
                    BirScope scope) {
            this(pos, exprList, lhsOp, thenBB);
            this.scope = scope;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return exprList.toArray(new BIROperand[0]);
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            this.exprList = new ArrayList<>(Arrays.asList(operands));
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{thenBB};
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

        public Flush(Location pos, ChannelDetails[] channels, BIROperand lhsOp, BIRBasicBlock thenBB) {
            super(pos, InstructionKind.FLUSH);
            this.channels = channels;
            this.lhsOp = lhsOp;
            this.thenBB = thenBB;
        }

        public Flush(Location pos,
                     ChannelDetails[] channels,
                     BIROperand lhsOp,
                     BIRBasicBlock thenBB,
                     BirScope scope) {
            this(pos, channels, lhsOp, thenBB);
            this.scope = scope;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return new BIROperand[0];
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            // do nothing
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{thenBB};
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

        public WorkerReceive(Location pos, Name workerName, BIROperand lhsOp,
                             boolean isSameStrand, BIRBasicBlock thenBB) {
            super(pos, InstructionKind.WK_RECEIVE);
            this.workerName = workerName;
            this.thenBB = thenBB;
            this.isSameStrand = isSameStrand;
            this.lhsOp = lhsOp;
        }

        public WorkerReceive(Location pos,
                             Name workerName,
                             BIROperand lhsOp,
                             boolean isSameStrand,
                             BIRBasicBlock thenBB,
                             BirScope scope) {
            this(pos, workerName, lhsOp, isSameStrand, thenBB);
            this.scope = scope;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return new BIROperand[0];
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            // do nothing
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{thenBB};
        }
    }


    /**
     * A worker receive instruction for alternate receive.
     * <p>
     * e.g., WRK_RECEIVE w1 | w2;
     *
     * @since 2201.9.0
     */
    public static class WorkerAlternateReceive extends BIRTerminator {
        public List<String> channels;
        public boolean isSameStrand;

        public WorkerAlternateReceive(Location pos, List<String> channels, BIROperand lhsOp,
                             boolean isSameStrand, BIRBasicBlock thenBB) {
            super(pos, InstructionKind.WK_ALT_RECEIVE);
            this.channels = channels;
            this.thenBB = thenBB;
            this.isSameStrand = isSameStrand;
            this.lhsOp = lhsOp;
        }

        public WorkerAlternateReceive(Location pos, List<String> channels, BIROperand lhsOp, boolean isSameStrand,
                             BIRBasicBlock thenBB, BirScope scope) {
            this(pos, channels, lhsOp, isSameStrand, thenBB);
            this.scope = scope;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return new BIROperand[0];
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            // do nothing
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{thenBB};
        }
    }

    /**
     * A worker receive instruction for multiple receive.
     * <p>
     * e.g., WRK_RECEIVE {w1 , w2};
     *
     * @since 2201.9.0
     */
    public static class WorkerMultipleReceive extends BIRTerminator {
        public boolean isSameStrand;
        public BType targetType;
        public List<ReceiveField> receiveFields;

        public WorkerMultipleReceive(Location pos, List<ReceiveField> receiveFields, BIROperand lhsOp,
                                      boolean isSameStrand, BIRBasicBlock thenBB) {
            super(pos, InstructionKind.WK_MULTIPLE_RECEIVE);
            this.thenBB = thenBB;
            this.isSameStrand = isSameStrand;
            this.lhsOp = lhsOp;
            this.targetType = lhsOp.variableDcl.type;
            this.receiveFields = receiveFields;
        }

        public WorkerMultipleReceive(Location pos, List<ReceiveField> receiveFields, BIROperand lhsOp,
                                     boolean isSameStrand, BIRBasicBlock thenBB, BirScope scope) {
            this(pos, receiveFields, lhsOp, isSameStrand, thenBB);
            this.scope = scope;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return new BIROperand[0];
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            // do nothing
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{thenBB};
        }

        /**
         *  A worker receive field for multiple receive action.
         *  @since 2201.9.0
         * @param key the field name of the result
         * @param workerReceive the channel name
         */
        public record ReceiveField(String key, String workerReceive) {
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

        public WorkerSend(Location location, Name workerName, BIROperand data,
                          boolean isSameStrand, boolean isSync, BIROperand lhsOp,
                          BIRBasicBlock thenBB) {
            super(location, InstructionKind.WK_SEND);
            this.channel = workerName;
            this.data = data;
            this.thenBB = thenBB;
            this.lhsOp = lhsOp;
            this.isSameStrand = isSameStrand;
            this.isSync = isSync;
        }

        public WorkerSend(Location location,
                          Name workerName,
                          BIROperand data,
                          boolean isSameStrand,
                          boolean isSync,
                          BIROperand lhsOp,
                          BIRBasicBlock thenBB,
                          BirScope scope) {
            this(location, workerName, data, isSameStrand, isSync, lhsOp, thenBB);
            this.scope = scope;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return new BIROperand[]{data};
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            this.data = operands[0];
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{thenBB};
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

        public WaitAll(Location pos, BIROperand lhsOp, List<String> keys, List<BIROperand> valueExprs,
                       BIRBasicBlock thenBB, BirScope scope) {
            super(pos, InstructionKind.WAIT_ALL);
            this.lhsOp = lhsOp;
            this.keys = keys;
            this.valueExprs = valueExprs;
            this.thenBB = thenBB;
            this.scope = scope;
        }

        @Override
        public void accept(BIRVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public BIROperand[] getRhsOperands() {
            return valueExprs.toArray(new BIROperand[0]);
        }

        @Override
        public void setRhsOperands(BIROperand[] operands) {
            this.valueExprs = Arrays.asList(operands);
        }

        @Override
        public BIRBasicBlock[] getNextBasicBlocks() {
            return new BIRBasicBlock[]{thenBB};
        }
    }
}

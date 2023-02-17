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
package org.wso2.ballerinalang.compiler.bir.writer;

import io.ballerina.tools.diagnostics.Location;
import io.netty.buffer.ByteBuf;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRGlobalVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewArray;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewStringXMLQName;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewStructure;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewTypeDesc;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLComment;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLElement;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLProcIns;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLQName;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLText;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.XMLAccess;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.BirScope;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.ByteCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.FloatCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.IntegerCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Responsible for serializing BIR instructions and operands.
 *
 * @since 0.980.0
 */
public class BIRInstructionWriter extends BIRVisitor {

    private ByteBuf buf;
    private ByteBuf scopeBuf;
    private ConstantPool cp;
    private BIRBinaryWriter binaryWriter;
    private int instructionOffset;
    private Set<BirScope> completedScopeSet;
    private int scopeCount;

    BIRInstructionWriter(ByteBuf buf, ByteBuf scopeBuf, ConstantPool cp, BIRBinaryWriter birBinaryWriter) {
        this.buf = buf;
        this.scopeBuf = scopeBuf;
        this.binaryWriter = birBinaryWriter;
        this.cp = cp;
        this.instructionOffset = 0;
        this.completedScopeSet = new HashSet<>();
        this.scopeCount = 0;
    }

    public int getScopeCount() {
        return scopeCount;
    }

    void writeBBs(List<BIRBasicBlock> bbList) {
        buf.writeInt(bbList.size());
        bbList.forEach(bb -> bb.accept(this));
    }

    void writeScopes(BIRNonTerminator instruction) {
        this.instructionOffset++;
        BirScope currentScope = instruction.scope;

        writeScope(currentScope);
    }

    void writeScope(BIRTerminator terminator) {
        if (terminator.kind != InstructionKind.RETURN) {
            BirScope currentScope = terminator.scope;
            writeScope(currentScope);
        }
    }

    private void writeScope(BirScope currentScope) {
        if (this.completedScopeSet.contains(currentScope)) {
            return;
        }

        this.completedScopeSet.add(currentScope);
        this.scopeCount++; // Increment the scope count so we can read the scopes iteratively

        scopeBuf.writeInt(currentScope.id);
        scopeBuf.writeInt(this.instructionOffset);

        if (currentScope.parent != null) {
            scopeBuf.writeBoolean(true); // Parent available.
            scopeBuf.writeInt(currentScope.parent.id);
            writeScope(currentScope.parent);
        } else {
            scopeBuf.writeBoolean(false);
        }
    }

    public void visit(BIRBasicBlock birBasicBlock) {
        //Name of the basic block
        addCpAndWriteString(birBasicBlock.id.value);
        // Number of instructions
        // Adding the terminator instruction as well.
        buf.writeInt(birBasicBlock.instructions.size() + 1);

        birBasicBlock.instructions.forEach(instruction -> {
            // write pos and kind
            writePosition(instruction.pos);
            writeScopes(instruction);
            buf.writeByte(instruction.kind.getValue());
            // write instruction
            instruction.accept(this);
        });

        BIRTerminator terminator = birBasicBlock.terminator;
        if (terminator == null) {
            throw new BLangCompilerException("Basic block without a terminator : " + birBasicBlock.id);
        }

        // write pos and kind
        writePosition(terminator.pos);
        writeScope(terminator);
        buf.writeByte(terminator.kind.getValue());
        // write instruction
        terminator.accept(this);
    }

    void writeErrorTable(List<BIRNode.BIRErrorEntry> errorEntries) {
        buf.writeInt(errorEntries.size());
        errorEntries.forEach(birErrorEntry -> birErrorEntry.accept(this));
    }

    public void visit(BIRNode.BIRErrorEntry errorEntry) {
        addCpAndWriteString(errorEntry.trapBB.id.value);
        addCpAndWriteString(errorEntry.endBB.id.value);
        errorEntry.errorOp.accept(this);
        addCpAndWriteString(errorEntry.targetBB.id.value);
    }

    // Terminating instructions

    public void visit(BIRTerminator.GOTO birGoto) {
        addCpAndWriteString(birGoto.targetBB.id.value);
    }

    public void visit(BIRTerminator.Lock lock) {
        addCpAndWriteString(lock.lockedBB.id.value);
    }

    public void visit(BIRTerminator.FieldLock lock) {
        // TODO properly use operand instead of variablDcl.name here
        addCpAndWriteString(lock.localVar.variableDcl.name.value);
        addCpAndWriteString(lock.field);
        addCpAndWriteString(lock.lockedBB.id.value);
    }

    public void visit(BIRTerminator.Unlock unlock) {
        addCpAndWriteString(unlock.unlockBB.id.value);
    }

    public void visit(BIRTerminator.Return birReturn) {
        // do nothing
    }

    public void visit(BIRTerminator.Branch birBranch) {
        birBranch.op.accept(this);
        // true:BB
        addCpAndWriteString(birBranch.trueBB.id.value);
        // false:BB
        addCpAndWriteString(birBranch.falseBB.id.value);
    }

    public void visit(BIRTerminator.Wait waitEntry) {
        buf.writeInt(waitEntry.exprList.size());
        for (BIROperand expr : waitEntry.exprList) {
            expr.accept(this);
        }
        waitEntry.lhsOp.accept(this);
        addCpAndWriteString(waitEntry.thenBB.id.value);
    }

    public void visit(BIRTerminator.Flush entry) {
        buf.writeInt(entry.channels.length);
        for (BIRNode.ChannelDetails detail : entry.channels) {
            addCpAndWriteString(detail.name);
            buf.writeBoolean(detail.channelInSameStrand);
            buf.writeBoolean(detail.send);
        }
        entry.lhsOp.accept(this);
        addCpAndWriteString(entry.thenBB.id.value);
    }

    public void visit(BIRTerminator.WorkerReceive entry) {
        buf.writeInt(addStringCPEntry(entry.workerName.getValue()));
        entry.lhsOp.accept(this);
        buf.writeBoolean(entry.isSameStrand);
        addCpAndWriteString(entry.thenBB.id.value);
    }

    public void visit(BIRTerminator.WorkerSend entry) {
        buf.writeInt(addStringCPEntry(entry.channel.getValue()));
        entry.data.accept(this);
        buf.writeBoolean(entry.isSameStrand);
        buf.writeBoolean(entry.isSync);
        if (entry.isSync) {
            entry.lhsOp.accept(this);
        }
        addCpAndWriteString(entry.thenBB.id.value);
    }

    public void visit(BIRTerminator.WaitAll waitAll) {
        waitAll.lhsOp.accept(this);
        buf.writeInt(waitAll.keys.size());
        waitAll.keys.forEach(key -> buf.writeInt(addStringCPEntry(key)));
        buf.writeInt(waitAll.valueExprs.size());
        waitAll.valueExprs.forEach(val -> val.accept(this));
        addCpAndWriteString(waitAll.thenBB.id.value);
    }

    // Non-terminating instructions

    @Override
    public void visit(BIRNonTerminator.NewTable newTable) {
        writeType(newTable.type);
        newTable.lhsOp.accept(this);
        newTable.keyColOp.accept(this);
        newTable.dataOp.accept(this);
    }

    public void visit(BIRNonTerminator.Move birMove) {
        birMove.rhsOp.accept(this);
        birMove.lhsOp.accept(this);
    }

    public void visit(BIRTerminator.Call birCall) {
        writeCallInstruction(birCall);
        addCpAndWriteString(birCall.thenBB.id.value);
    }

    public void visit(BIRTerminator.AsyncCall birAsyncCall) {
        writeCallInstruction(birAsyncCall);
        BIRWriterUtils.writeAnnotAttachments(this.cp, buf, birAsyncCall.annotAttachments);
        addCpAndWriteString(birAsyncCall.thenBB.id.value);
    }

    private void writeCallInstruction(BIRTerminator.Call birCall) {
        PackageID calleePkg = birCall.calleePkg;
        int pkgIndex = addPkgCPEntry(calleePkg);
        buf.writeBoolean(birCall.isVirtual);
        buf.writeInt(pkgIndex);
        buf.writeInt(addStringCPEntry(birCall.name.getValue()));
        buf.writeInt(birCall.args.size());
        for (BIROperand arg : birCall.args) {
            arg.accept(this);
        }
        if (birCall.lhsOp != null) {
            buf.writeByte(1);
            birCall.lhsOp.accept(this);
        } else {
            buf.writeByte(0);
        }
    }

    public void visit(BIRTerminator.FPCall fpCall) {
        fpCall.fp.accept(this);
        buf.writeInt(fpCall.args.size());
        for (BIROperand arg : fpCall.args) {
            arg.accept(this);
        }
        if (fpCall.lhsOp != null) {
            buf.writeByte(1);
            fpCall.lhsOp.accept(this);
        } else {
            buf.writeByte(0);
        }
        buf.writeBoolean(fpCall.isAsync);
        addCpAndWriteString(fpCall.thenBB.id.value);
    }

    public void visit(BIRNonTerminator.BinaryOp birBinaryOp) {
        birBinaryOp.rhsOp1.accept(this);
        birBinaryOp.rhsOp2.accept(this);
        birBinaryOp.lhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.UnaryOP birUnaryOp) {
        birUnaryOp.rhsOp.accept(this);
        birUnaryOp.lhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.ConstantLoad birConstantLoad) {
        writeType(birConstantLoad.type);
        birConstantLoad.lhsOp.accept(this);

        BType type = birConstantLoad.type;
        switch (type.tag) {
            case TypeTags.INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
                buf.writeInt(cp.addCPEntry(new IntegerCPEntry((Long) birConstantLoad.value)));
                break;
            case TypeTags.BYTE:
                // TODO: birConstantLoad.value should return an Integer. This is a temporary fix
                int byteValue = ((Number) birConstantLoad.value).intValue();
                buf.writeInt(cp.addCPEntry(new ByteCPEntry(byteValue)));
                break;
            case TypeTags.BOOLEAN:
                // Not adding to constant pool as it increases the size (bit vs integer)
                buf.writeBoolean((Boolean) birConstantLoad.value);
                break;
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
            case TypeTags.DECIMAL:
                buf.writeInt(cp.addCPEntry(new StringCPEntry(birConstantLoad.value.toString())));
                break;
            case TypeTags.FLOAT:
                double value = birConstantLoad.value instanceof Double ? (double) birConstantLoad.value
                        : Double.parseDouble((String) birConstantLoad.value);
                buf.writeInt(cp.addCPEntry(new FloatCPEntry(value)));
                break;
            case TypeTags.NIL:
                break;
            default:
                throw new IllegalStateException("unsupported constant type: " + type);
        }
    }

    public void visit(NewStructure birNewStructure) {
        birNewStructure.rhsOp.accept(this);
        birNewStructure.lhsOp.accept(this);
        buf.writeInt(birNewStructure.initialValues.size());
        for (BIRNode.BIRMappingConstructorEntry initialValue : birNewStructure.initialValues) {
            buf.writeBoolean(initialValue.isKeyValuePair());
            if (initialValue.isKeyValuePair()) {
                BIRNode.BIRMappingConstructorKeyValueEntry keyValueEntry =
                        (BIRNode.BIRMappingConstructorKeyValueEntry) initialValue;
                keyValueEntry.keyOp.accept(this);
                keyValueEntry.valueOp.accept(this);
            } else {
                BIRNode.BIRMappingConstructorSpreadFieldEntry spreadEntry =
                        (BIRNode.BIRMappingConstructorSpreadFieldEntry) initialValue;
                spreadEntry.exprOp.accept(this);
            }
        }
    }

    public void visit(BIRNonTerminator.NewInstance newInstance) {
        buf.writeBoolean(newInstance.isExternalDef);
        if (newInstance.isExternalDef) {
            assert newInstance.externalPackageId != null;
            buf.writeInt(addPkgCPEntry(newInstance.externalPackageId));
            buf.writeInt(addStringCPEntry(newInstance.objectName));
        } else {
            buf.writeInt(newInstance.def.index);
        }
        newInstance.lhsOp.accept(this);
    }

    public void visit(NewArray birNewArray) {
        writeType(birNewArray.type);
        birNewArray.lhsOp.accept(this);
        if (birNewArray.typedescOp != null) {
            buf.writeByte(1);
            birNewArray.typedescOp.accept(this);
        } else {
            buf.writeByte(0);
        }
        birNewArray.sizeOp.accept(this);
        buf.writeInt(birNewArray.values.size());
        for (BIRNode.BIRListConstructorEntry listValueEntry : birNewArray.values) {
            listValueEntry.exprOp.accept(this);
        }
    }

    public void visit(BIRNonTerminator.FieldAccess birFieldAccess) {
        if (birFieldAccess.kind == InstructionKind.MAP_LOAD || birFieldAccess.kind == InstructionKind.ARRAY_LOAD) {
            buf.writeBoolean(birFieldAccess.optionalFieldAccess);
            buf.writeBoolean(birFieldAccess.fillingRead);
        }
        birFieldAccess.lhsOp.accept(this);
        birFieldAccess.keyOp.accept(this);
        birFieldAccess.rhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.TypeCast birTypeCast) {
        birTypeCast.lhsOp.accept(this);
        birTypeCast.rhsOp.accept(this);
        writeType(birTypeCast.type);
        buf.writeBoolean(birTypeCast.checkTypes);
    }

    public void visit(BIRNonTerminator.IsLike birIsLike) {
        writeType(birIsLike.type);
        birIsLike.lhsOp.accept(this);
        birIsLike.rhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.TypeTest birTypeTest) {
        writeType(birTypeTest.type);
        birTypeTest.lhsOp.accept(this);
        birTypeTest.rhsOp.accept(this);
    }

    // Operands
    public void visit(BIROperand birOperand) {
        if (birOperand.variableDcl.ignoreVariable) {
            buf.writeBoolean(true);
            writeType(birOperand.variableDcl.type);
            return;
        }

        buf.writeBoolean(false);
        buf.writeByte(birOperand.variableDcl.kind.getValue());
        buf.writeByte(birOperand.variableDcl.scope.getValue());

        // TODO use the integer index of the variable.
        addCpAndWriteString(birOperand.variableDcl.name.value);

        if (birOperand.variableDcl.kind == VarKind.GLOBAL || birOperand.variableDcl.kind == VarKind.CONSTANT) {
            int pkgIndex = addPkgCPEntry(((BIRGlobalVariableDcl) birOperand.variableDcl).pkgId);
            buf.writeInt(pkgIndex);

            writeType(birOperand.variableDcl.type);
        }
    }

    public void visit(BIRNonTerminator.NewError birNewError) {
        writeType(birNewError.type);
        birNewError.lhsOp.accept(this);
        birNewError.messageOp.accept(this);
        birNewError.causeOp.accept(this);
        birNewError.detailOp.accept(this);
    }

    public void visit(BIRNonTerminator.FPLoad fpLoad) {
        fpLoad.lhsOp.accept(this);

        PackageID pkgId = fpLoad.pkgId;
        int pkgIndex = addPkgCPEntry(pkgId);
        buf.writeInt(pkgIndex);
        buf.writeInt(addStringCPEntry(fpLoad.funcName.getValue()));
        writeType(fpLoad.type);

        buf.writeInt(fpLoad.closureMaps.size());
        for (BIROperand op : fpLoad.closureMaps) {
            op.accept(this);
        }

        buf.writeInt(fpLoad.params.size());
        fpLoad.params.forEach(param -> {
            buf.writeByte(param.kind.getValue());
            writeType(param.type);
            buf.writeInt(addStringCPEntry(param.name.value));
        });

    }

    public void visit(BIRTerminator.Panic birPanic) {
        birPanic.errorOp.accept(this);
    }

    @Override
    public void visit(NewXMLElement newXMLElement) {
        newXMLElement.lhsOp.accept(this);
        newXMLElement.startTagOp.accept(this);
        newXMLElement.defaultNsURIOp.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewXMLSequence newXMLSequence) {
        newXMLSequence.lhsOp.accept(this);
    }

    @Override
    public void visit(NewXMLText newXMLText) {
        newXMLText.lhsOp.accept(this);
        newXMLText.textOp.accept(this);
    }

    @Override
    public void visit(NewXMLQName newXMLQName) {
        newXMLQName.lhsOp.accept(this);
        newXMLQName.localnameOp.accept(this);
        newXMLQName.nsURIOp.accept(this);
        newXMLQName.prefixOp.accept(this);
    }

    @Override
    public void visit(NewStringXMLQName newStringXMLQName) {
        newStringXMLQName.lhsOp.accept(this);
        newStringXMLQName.stringQNameOP.accept(this);
    }

    @Override
    public void visit(XMLAccess xmlAccess) {
        xmlAccess.lhsOp.accept(this);
        xmlAccess.rhsOp.accept(this);
    }

    @Override
    public void visit(NewXMLComment newXMLComment) {
        newXMLComment.lhsOp.accept(this);
        newXMLComment.textOp.accept(this);
    }

    @Override
    public void visit(NewXMLProcIns newXMLProcIns) {
        newXMLProcIns.lhsOp.accept(this);
        newXMLProcIns.dataOp.accept(this);
        newXMLProcIns.targetOp.accept(this);
    }

    @Override
    public void visit(NewTypeDesc newTypeDesc) {
        newTypeDesc.lhsOp.accept(this);
        writeType(newTypeDesc.type);
    }

    @Override
    public void visit(BIRNonTerminator.NewRegExp newRegExp) {
        newRegExp.lhsOp.accept(this);
        newRegExp.reDisjunction.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewReDisjunction reDisjunction) {
        reDisjunction.lhsOp.accept(this);
        reDisjunction.sequences.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewReSequence reSequence) {
        reSequence.lhsOp.accept(this);
        reSequence.terms.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewReAssertion reAssertion) {
        reAssertion.lhsOp.accept(this);
        reAssertion.assertion.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewReAtomQuantifier reAtomQuantifier) {
        reAtomQuantifier.lhsOp.accept(this);
        reAtomQuantifier.atom.accept(this);
        reAtomQuantifier.quantifier.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewReLiteralCharOrEscape reLiteralCharOrEscape) {
        reLiteralCharOrEscape.lhsOp.accept(this);
        reLiteralCharOrEscape.charOrEscape.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewReQuantifier reQuantifier) {
        reQuantifier.lhsOp.accept(this);
        reQuantifier.quantifier.accept(this);
        reQuantifier.nonGreedyChar.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewReCharacterClass reCharacterClass) {
        reCharacterClass.lhsOp.accept(this);
        reCharacterClass.classStart.accept(this);
        reCharacterClass.negation.accept(this);
        reCharacterClass.charSet.accept(this);
        reCharacterClass.classEnd.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewReCharSet reCharSet) {
        reCharSet.lhsOp.accept(this);
        reCharSet.charSetAtoms.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewReCharSetRange reCharSetRange) {
        reCharSetRange.lhsOp.accept(this);
        reCharSetRange.lhsCharSetAtom.accept(this);
        reCharSetRange.dash.accept(this);
        reCharSetRange.rhsCharSetAtom.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewReCapturingGroup reCapturingGroups) {
        reCapturingGroups.lhsOp.accept(this);
        reCapturingGroups.openParen.accept(this);
        reCapturingGroups.flagExpr.accept(this);
        reCapturingGroups.reDisjunction.accept(this);
        reCapturingGroups.closeParen.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewReFlagExpression reFlagExpression) {
        reFlagExpression.lhsOp.accept(this);
        reFlagExpression.questionMark.accept(this);
        reFlagExpression.flagsOnOff.accept(this);
        reFlagExpression.colon.accept(this);
    }

    @Override
    public void visit(BIRNonTerminator.NewReFlagOnOff reFlagsOnOff) {
        reFlagsOnOff.lhsOp.accept(this);
        reFlagsOnOff.flags.accept(this);
    }

    // Positions
    void writePosition(Location pos) {
        BIRWriterUtils.writePosition(pos, this.buf, this.cp);
    }

    int addPkgCPEntry(PackageID packageID) {
        return BIRWriterUtils.addPkgCPEntry(packageID, this.cp);
    }

    // private methods

    private void addCpAndWriteString(String string) {
        buf.writeInt(addStringCPEntry(string));
    }

    private int addStringCPEntry(String value) {
        return BIRWriterUtils.addStringCPEntry(value, this.cp);
    }

    private void writeType(BType type) {
        buf.writeInt(cp.addShapeCPEntry(type));
    }
}

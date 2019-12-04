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
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.ByteCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.FloatCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.IntegerCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for serializing BIR instructions and operands.
 *
 * @since 0.980.0
 */
public class BIRInstructionWriter extends BIRVisitor {
    private ByteBuf buf;
    private BIRTypeWriter typeWriter;
    private ConstantPool cp;
    private BIRBinaryWriter binaryWriter;

    public BIRInstructionWriter(ByteBuf buf, BIRTypeWriter typeWriter, ConstantPool cp,
                                BIRBinaryWriter birBinaryWriter) {
        this.buf = buf;
        this.typeWriter = typeWriter;
        this.binaryWriter = birBinaryWriter;
        this.cp = cp;
    }

    public void writeBBs(List<BIRBasicBlock> bbList) {
        buf.writeInt(bbList.size());
        bbList.forEach(bb -> bb.accept(this));
    }

    public void visit(BIRBasicBlock birBasicBlock) {
        //Name of the basic block
        addCpAndWriteString(birBasicBlock.id.value);
        // Number of instructions
        // Adding the terminator instruction as well.
        buf.writeInt(birBasicBlock.instructions.size() + 1);
            birBasicBlock.instructions.forEach(instruction -> ((BIRNonTerminator) instruction).accept(this));
        if (birBasicBlock.terminator == null) {
            throw new BLangCompilerException("Basic block without a terminator : " + birBasicBlock.id);
        }
        birBasicBlock.terminator.accept(this);
    }

    public void writeErrorTable(List<BIRNode.BIRErrorEntry> errorEntries) {
        buf.writeInt(errorEntries.size());
        errorEntries.forEach(birErrorEntry -> birErrorEntry.accept(this));
    }

    public void visit(BIRNode.BIRErrorEntry errorEntry) {
        addCpAndWriteString(errorEntry.trapBB.id.value);
        errorEntry.errorOp.accept(this);
        addCpAndWriteString(errorEntry.targetBB.id.value);
    }

    // Terminating instructions

    public void visit(BIRTerminator.GOTO birGoto) {
        writePosition(birGoto.pos);
        buf.writeByte(birGoto.kind.getValue());
        addCpAndWriteString(birGoto.targetBB.id.value);
    }

    public void visit(BIRTerminator.Lock lock) {
        writePosition(lock.pos);
        buf.writeByte(lock.kind.getValue());
        addCpAndWriteString(lock.globalVar.name.value);

        int pkgIndex = addPkgCPEntry(lock.globalVar.pkgId);
        buf.writeInt(pkgIndex);

        writeType(lock.globalVar.type);

        addCpAndWriteString(lock.lockedBB.id.value);
    }

    public void visit(BIRTerminator.FieldLock lock) {
        writePosition(lock.pos);
        buf.writeByte(lock.kind.getValue());
        addCpAndWriteString(lock.localVar.name.value);
        addCpAndWriteString(lock.field);
        addCpAndWriteString(lock.lockedBB.id.value);
    }

    public void visit(BIRTerminator.Unlock unlock) {
        writePosition(unlock.pos);
        buf.writeByte(unlock.kind.getValue());
        buf.writeInt(unlock.globalVars.size());
        for (BIRNode.BIRGlobalVariableDcl globalVar : unlock.globalVars) {
            addCpAndWriteString(globalVar.name.value);

            int pkgIndex = addPkgCPEntry(globalVar.pkgId);
            buf.writeInt(pkgIndex);

            writeType(globalVar.type);
        }
        buf.writeInt(unlock.fieldLocks.size());
        for (Map.Entry<BIRNode.BIRVariableDcl, Set<String>> entry : unlock.fieldLocks.entrySet()) {
            addCpAndWriteString(entry.getKey().name.value);
            buf.writeInt(entry.getValue().size());
            for (String field : entry.getValue()) {
                addCpAndWriteString(field);
            }
        }
        addCpAndWriteString(unlock.unlockBB.id.value);
    }


    public void visit(BIRTerminator.Return birReturn) {
        writePosition(birReturn.pos);
        buf.writeByte(birReturn.kind.getValue());
    }

    public void visit(BIRTerminator.Branch birBranch) {
        writePosition(birBranch.pos);
        buf.writeByte(birBranch.kind.getValue());
        birBranch.op.accept(this);
        // true:BB
        addCpAndWriteString(birBranch.trueBB.id.value);
        // false:BB
        addCpAndWriteString(birBranch.falseBB.id.value);
    }

    public void visit(BIRTerminator.Wait waitEntry) {
        writePosition(waitEntry.pos);
        buf.writeByte(waitEntry.kind.getValue());
        buf.writeInt(waitEntry.exprList.size());
        for (BIROperand expr : waitEntry.exprList) {
            expr.accept(this);
        }
        waitEntry.lhsOp.accept(this);
        addCpAndWriteString(waitEntry.thenBB.id.value);
    }

    public void visit(BIRTerminator.Flush entry) {
        writePosition(entry.pos);
        buf.writeByte(entry.kind.getValue());
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
        writePosition(entry.pos);
        buf.writeByte((entry.kind.getValue()));
        buf.writeInt(addStringCPEntry(entry.workerName.getValue()));
        entry.lhsOp.accept(this);
        buf.writeBoolean(entry.isSameStrand);
        addCpAndWriteString(entry.thenBB.id.value);
    }

    public void visit(BIRTerminator.WorkerSend entry) {
        writePosition(entry.pos);
        buf.writeByte((entry.kind.getValue()));
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
        writePosition(waitAll.pos);
        buf.writeByte((waitAll.kind.getValue()));
        waitAll.lhsOp.accept(this);
        buf.writeInt(waitAll.keys.size());
        waitAll.keys.forEach(key -> buf.writeInt(addStringCPEntry(key)));
        waitAll.valueExprs.forEach(val -> val.accept(this));
        addCpAndWriteString(waitAll.thenBB.id.value);
    }

    // Non-terminating instructions

    public void visit(BIRNonTerminator.Move birMove) {
        writePosition(birMove.pos);
        buf.writeByte(birMove.kind.getValue());
        birMove.rhsOp.accept(this);
        birMove.lhsOp.accept(this);
    }

    public void visit(BIRTerminator.Call birCall) {
        writePosition(birCall.pos);
        buf.writeByte(birCall.kind.getValue());
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
        addCpAndWriteString(birCall.thenBB.id.value);
    }

    public void visit(BIRTerminator.AsyncCall birAsyncCall) {
        writePosition(birAsyncCall.pos);
        buf.writeByte(birAsyncCall.kind.getValue());
        PackageID calleePkg = birAsyncCall.calleePkg;
        int pkgIndex = addPkgCPEntry(calleePkg);
        buf.writeBoolean(birAsyncCall.isVirtual);
        buf.writeInt(pkgIndex);
        buf.writeInt(addStringCPEntry(birAsyncCall.name.getValue()));
        buf.writeInt(birAsyncCall.args.size());
        for (BIROperand arg : birAsyncCall.args) {
            arg.accept(this);
        }
        if (birAsyncCall.lhsOp != null) {
            buf.writeByte(1);
            birAsyncCall.lhsOp.accept(this);
        } else {
            buf.writeByte(0);
        }

        binaryWriter.writeAnnotAttachments(buf, this, birAsyncCall.annotAttachments);
        addCpAndWriteString(birAsyncCall.thenBB.id.value);
    }

    public void visit(BIRTerminator.FPCall fpCall) {
        writePosition(fpCall.pos);
        buf.writeByte(fpCall.kind.getValue());
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
        writePosition(birBinaryOp.pos);
        buf.writeByte(birBinaryOp.kind.getValue());
        birBinaryOp.rhsOp1.accept(this);
        birBinaryOp.rhsOp2.accept(this);
        birBinaryOp.lhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.UnaryOP birUnaryOp) {
        writePosition(birUnaryOp.pos);
        buf.writeByte(birUnaryOp.kind.getValue());
        birUnaryOp.rhsOp.accept(this);
        birUnaryOp.lhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.ConstantLoad birConstantLoad) {
        writePosition(birConstantLoad.pos);
        buf.writeByte(birConstantLoad.kind.getValue());
        writeType(birConstantLoad.type);
        birConstantLoad.lhsOp.accept(this);

        BType type = birConstantLoad.type;
        switch (type.tag) {
            case TypeTags.INT:
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
                throw new IllegalStateException("unsupported constant type: " + type.getDesc());
        }
    }

    public void visit(NewStructure birNewStructure) {
        writePosition(birNewStructure.pos);
        buf.writeByte(birNewStructure.kind.getValue());
        writeType(birNewStructure.type);
        buf.writeBoolean(birNewStructure.isExternalDef);
        if (birNewStructure.isExternalDef) {
            assert birNewStructure.externalPackageId != null;
            buf.writeInt(addPkgCPEntry(birNewStructure.externalPackageId));
            buf.writeInt(addStringCPEntry(birNewStructure.recordName));
        }
        birNewStructure.lhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.NewInstance newInstance) {
        writePosition(newInstance.pos);
        buf.writeByte(newInstance.kind.getValue());
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
        writePosition(birNewArray.pos);
        buf.writeByte(birNewArray.kind.getValue());
        writeType(birNewArray.type);
        birNewArray.lhsOp.accept(this);
        birNewArray.sizeOp.accept(this);
    }

    public void visit(BIRNonTerminator.FieldAccess birFieldAccess) {
        writePosition(birFieldAccess.pos);
        buf.writeByte(birFieldAccess.kind.getValue());
        if (birFieldAccess.kind == InstructionKind.MAP_LOAD) {
            buf.writeBoolean(birFieldAccess.optionalFieldAccess);
            buf.writeBoolean(birFieldAccess.fillingRead);
        }
        birFieldAccess.lhsOp.accept(this);
        birFieldAccess.keyOp.accept(this);
        birFieldAccess.rhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.TypeCast birTypeCast) {
        writePosition(birTypeCast.pos);
        buf.writeByte(birTypeCast.kind.getValue());
        birTypeCast.lhsOp.accept(this);
        birTypeCast.rhsOp.accept(this);
        writeType(birTypeCast.type);
        buf.writeBoolean(birTypeCast.checkTypes);
    }

    public void visit(BIRNonTerminator.IsLike birIsLike) {
        writePosition(birIsLike.pos);
        buf.writeByte(birIsLike.kind.getValue());
        writeType(birIsLike.type);
        birIsLike.lhsOp.accept(this);
        birIsLike.rhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.TypeTest birTypeTest) {
        writePosition(birTypeTest.pos);
        buf.writeByte(birTypeTest.kind.getValue());
        writeType(birTypeTest.type);
        birTypeTest.lhsOp.accept(this);
        birTypeTest.rhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.NewTable newTable) {
        writePosition(newTable.pos);
        buf.writeByte(newTable.kind.getValue());
        writeType(newTable.type);
        newTable.lhsOp.accept(this);
        newTable.columnsOp.accept(this);
        newTable.dataOp.accept(this);
        newTable.keyColOp.accept(this);
    }

    public void visit(BIRNonTerminator.NewStream newStream) {
        writePosition(newStream.pos);
        buf.writeByte(newStream.kind.getValue());
        writeType(newStream.type);
        newStream.lhsOp.accept(this);
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
        writePosition(birNewError.pos);
        buf.writeByte(birNewError.kind.getValue());
        writeType(birNewError.type);
        birNewError.lhsOp.accept(this);
        birNewError.reasonOp.accept(this);
        birNewError.detailOp.accept(this);
    }

    public void visit(BIRNonTerminator.FPLoad fpLoad) {
        writePosition(fpLoad.pos);
        buf.writeByte(fpLoad.kind.getValue());
        fpLoad.lhsOp.accept(this);

        PackageID pkgId = fpLoad.pkgId;
        int pkgIndex = addPkgCPEntry(pkgId);
        buf.writeInt(pkgIndex);
        buf.writeInt(addStringCPEntry(fpLoad.funcName.getValue()));
        writeType(fpLoad.retType);

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
        writePosition(birPanic.pos);
        buf.writeByte(birPanic.kind.getValue());
        birPanic.errorOp.accept(this);
    }

    @Override
    public void visit(NewXMLElement newXMLElement) {
        writePosition(newXMLElement.pos);
        buf.writeByte(newXMLElement.kind.getValue());
        newXMLElement.lhsOp.accept(this);
        newXMLElement.startTagOp.accept(this);
        newXMLElement.endTagOp.accept(this);
        newXMLElement.defaultNsURIOp.accept(this);
    }

    @Override
    public void visit(NewXMLText newXMLText) {
        writePosition(newXMLText.pos);
        buf.writeByte(newXMLText.kind.getValue());
        newXMLText.lhsOp.accept(this);
        newXMLText.textOp.accept(this);
    }

    @Override
    public void visit(NewXMLQName newXMLQName) {
        writePosition(newXMLQName.pos);
        buf.writeByte(newXMLQName.kind.getValue());
        newXMLQName.lhsOp.accept(this);
        newXMLQName.localnameOp.accept(this);
        newXMLQName.nsURIOp.accept(this);
        newXMLQName.prefixOp.accept(this);
    }

    @Override
    public void visit(NewStringXMLQName newStringXMLQName) {
        writePosition(newStringXMLQName.pos);
        buf.writeByte(newStringXMLQName.kind.getValue());
        newStringXMLQName.lhsOp.accept(this);
        newStringXMLQName.stringQNameOP.accept(this);
    }

    @Override
    public void visit(XMLAccess xmlAccess) {
        writePosition(xmlAccess.pos);
        buf.writeByte(xmlAccess.kind.getValue());
        xmlAccess.lhsOp.accept(this);
        xmlAccess.rhsOp.accept(this);
    }

    @Override
    public void visit(NewXMLComment newXMLComment) {
        writePosition(newXMLComment.pos);
        buf.writeByte(newXMLComment.kind.getValue());
        newXMLComment.lhsOp.accept(this);
        newXMLComment.textOp.accept(this);
    }

    @Override
    public void visit(NewXMLProcIns newXMLProcIns) {
        writePosition(newXMLProcIns.pos);
        buf.writeByte(newXMLProcIns.kind.getValue());
        newXMLProcIns.lhsOp.accept(this);
        newXMLProcIns.dataOp.accept(this);
        newXMLProcIns.targetOp.accept(this);
    }

    @Override
    public void visit(NewTypeDesc newTypeDesc) {
        writePosition(newTypeDesc.pos);
        buf.writeByte(newTypeDesc.kind.getValue());
        newTypeDesc.lhsOp.accept(this);
        writeType(newTypeDesc.type);
    }

    // Positions
    // TODO Refactor duplicate methods
    void writePosition(DiagnosticPos pos) {
        int sLine = Integer.MIN_VALUE;
        int eLine = Integer.MIN_VALUE;
        int sCol = Integer.MIN_VALUE;
        int eCol = Integer.MIN_VALUE;
        String sourceFileName = "";
        if (pos != null) {
            sLine = pos.sLine;
            eLine = pos.eLine;
            sCol = pos.sCol;
            eCol = pos.eCol;
            if (pos.src != null) {
                sourceFileName = pos.src.cUnitName;
            }
        }
        buf.writeInt(sLine);
        buf.writeInt(eLine);
        buf.writeInt(sCol);
        buf.writeInt(eCol);
        buf.writeInt(addStringCPEntry(sourceFileName));
    }

    void writePosition(ByteBuf buf, DiagnosticPos pos) {
        int sLine = Integer.MIN_VALUE;
        int eLine = Integer.MIN_VALUE;
        int sCol = Integer.MIN_VALUE;
        int eCol = Integer.MIN_VALUE;
        String sourceFileName = "";
        if (pos != null) {
            sLine = pos.sLine;
            eLine = pos.eLine;
            sCol = pos.sCol;
            eCol = pos.eCol;
            if (pos.src != null) {
                sourceFileName = pos.src.cUnitName;
            }
        }
        buf.writeInt(sLine);
        buf.writeInt(eLine);
        buf.writeInt(sCol);
        buf.writeInt(eCol);
        buf.writeInt(addStringCPEntry(sourceFileName));
    }

    int addPkgCPEntry(PackageID packageID) {
        int orgCPIndex = addStringCPEntry(packageID.orgName.value);
        int nameCPIndex = addStringCPEntry(packageID.name.value);
        int versionCPIndex = addStringCPEntry(packageID.version.value);
        return cp.addCPEntry(new CPEntry.PackageCPEntry(orgCPIndex, nameCPIndex, versionCPIndex));
    }

    // private methods

    private void addCpAndWriteString(String string) {
        buf.writeInt(addStringCPEntry(string));
    }

    private int addStringCPEntry(String value) {
        return cp.addCPEntry(new CPEntry.StringCPEntry(value));
    }

    private void writeType(BType type) {
        buf.writeInt(cp.addShapeCPEntry(type));
    }
}

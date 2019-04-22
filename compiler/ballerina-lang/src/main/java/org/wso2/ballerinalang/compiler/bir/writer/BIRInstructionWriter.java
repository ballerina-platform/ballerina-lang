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
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewArray;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewStringXMLQName;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewStructure;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLComment;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLElement;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLProcIns;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLQName;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLText;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.XMLSeqStore;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.FloatCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.IntegerCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

/**
 * Responsible for serializing BIR instructions and operands.
 *
 * @since 0.980.0
 */
public class BIRInstructionWriter extends BIRVisitor {
    private ByteBuf buf;
    private BIRTypeWriter typeWriter;
    private ConstantPool cp;

    public BIRInstructionWriter(ByteBuf buf, BIRTypeWriter typeWriter, ConstantPool cp) {
        this.buf = buf;
        this.typeWriter = typeWriter;
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
    }

    // Terminating instructions

    public void visit(BIRTerminator.GOTO birGoto) {
        buf.writeByte(birGoto.kind.getValue());
        addCpAndWriteString(birGoto.targetBB.id.value);
    }

    public void visit(BIRTerminator.Return birReturn) {
        buf.writeByte(birReturn.kind.getValue());
    }

    public void visit(BIRTerminator.Branch birBranch) {
        buf.writeByte(birBranch.kind.getValue());
        birBranch.op.accept(this);
        // true:BB
        addCpAndWriteString(birBranch.trueBB.id.value);
        // false:BB
        addCpAndWriteString(birBranch.falseBB.id.value);
    }


    // Non-terminating instructions

    public void visit(BIRNonTerminator.Move birMove) {
        buf.writeByte(birMove.kind.getValue());
        birMove.rhsOp.accept(this);
        birMove.lhsOp.accept(this);
    }

    public void visit(BIRTerminator.Call birCall) {
        buf.writeByte(birCall.kind.getValue());
        PackageID calleePkg = birCall.calleePkg;
        int orgCPIndex = addStringCPEntry(calleePkg.orgName.value);
        int nameCPIndex = addStringCPEntry(calleePkg.name.value);
        int versionCPIndex = addStringCPEntry(calleePkg.version.value);
        int pkgIndex = cp.addCPEntry(new CPEntry.PackageCPEntry(orgCPIndex, nameCPIndex, versionCPIndex));
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
        buf.writeByte(birAsyncCall.kind.getValue());
        PackageID calleePkg = birAsyncCall.calleePkg;
        int orgCPIndex = addStringCPEntry(calleePkg.orgName.value);
        int nameCPIndex = addStringCPEntry(calleePkg.name.value);
        int versionCPIndex = addStringCPEntry(calleePkg.version.value);
        int pkgIndex = cp.addCPEntry(new CPEntry.PackageCPEntry(orgCPIndex, nameCPIndex, versionCPIndex));
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
        addCpAndWriteString(birAsyncCall.thenBB.id.value);
    }

    public void visit(BIRNonTerminator.BinaryOp birBinaryOp) {
        buf.writeByte(birBinaryOp.kind.getValue());
        birBinaryOp.rhsOp1.accept(this);
        birBinaryOp.rhsOp2.accept(this);
        birBinaryOp.lhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.UnaryOP birUnaryOp) {
        throw new AssertionError();
    }

    public void visit(BIRNonTerminator.ConstantLoad birConstantLoad) {
        buf.writeByte(birConstantLoad.kind.getValue());
        birConstantLoad.type.accept(typeWriter);
        birConstantLoad.lhsOp.accept(this);

        BType type = birConstantLoad.type;
        switch (type.tag) {
            case TypeTags.INT:
            case TypeTags.BYTE:
                buf.writeInt(cp.addCPEntry(new IntegerCPEntry((Long) birConstantLoad.value)));
                break;
            case TypeTags.BOOLEAN:
                // Not adding to constant pool as it increases the size (bit vs integer)
                buf.writeBoolean((Boolean) birConstantLoad.value);
                break;
            case TypeTags.STRING:
                buf.writeInt(cp.addCPEntry(new StringCPEntry((String) birConstantLoad.value)));
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
        buf.writeByte(birNewStructure.kind.getValue());
        birNewStructure.type.accept(typeWriter);
        birNewStructure.lhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.NewInstance newInstance) {
        buf.writeByte(newInstance.kind.getValue());
        buf.writeInt(newInstance.def.index);
        newInstance.lhsOp.accept(this);
    }


    public void visit(NewArray birNewArray) {
        buf.writeByte(birNewArray.kind.getValue());
        birNewArray.type.accept(typeWriter);
        birNewArray.lhsOp.accept(this);
        birNewArray.sizeOp.accept(this);
    }

    public void visit(BIRNonTerminator.FieldAccess birFieldAccess) {
        buf.writeByte(birFieldAccess.kind.getValue());
        birFieldAccess.lhsOp.accept(this);
        birFieldAccess.keyOp.accept(this);
        birFieldAccess.rhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.TypeCast birTypeCast) {
        buf.writeByte(birTypeCast.kind.getValue());
        birTypeCast.lhsOp.accept(this);
        birTypeCast.rhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.IsLike birIsLike) {
        buf.writeByte(birIsLike.kind.getValue());
        birIsLike.type.accept(typeWriter);
        birIsLike.lhsOp.accept(this);
        birIsLike.rhsOp.accept(this);
    }

    public void visit(BIRNonTerminator.TypeTest birTypeTest) {
        buf.writeByte(birTypeTest.kind.getValue());
        birTypeTest.type.accept(typeWriter);
        birTypeTest.lhsOp.accept(this);
        birTypeTest.rhsOp.accept(this);
    }

    // Operands
    public void visit(BIROperand birOperand) {
        buf.writeByte(birOperand.variableDcl.kind.getValue());
        buf.writeByte(birOperand.variableDcl.scope.getValue());
        // TODO use the integer index of the variable.
        addCpAndWriteString(birOperand.variableDcl.name.value);
    }

    public void visit(BIRNonTerminator.NewError birNewError) {
        buf.writeByte(birNewError.kind.getValue());
        birNewError.lhsOp.accept(this);
        birNewError.reasonOp.accept(this);
        birNewError.detailOp.accept(this);
    }

    public void visit(BIRTerminator.Panic birPanic) {
        buf.writeByte(birPanic.kind.getValue());
        birPanic.errorOp.accept(this);
    }

    @Override
    public void visit(NewXMLElement newXMLElement) {
        buf.writeByte(newXMLElement.kind.getValue());
        newXMLElement.lhsOp.accept(this);
        newXMLElement.startTagOp.accept(this);
        newXMLElement.endTagOp.accept(this);
        newXMLElement.defaultNsURIOp.accept(this);
    }

    @Override
    public void visit(NewXMLText newXMLText) {
        buf.writeByte(newXMLText.kind.getValue());
        newXMLText.lhsOp.accept(this);
        newXMLText.textOp.accept(this);
    }

    @Override
    public void visit(NewXMLQName newXMLQName) {
        buf.writeByte(newXMLQName.kind.getValue());
        newXMLQName.lhsOp.accept(this);
        newXMLQName.localnameOp.accept(this);
        newXMLQName.nsURIOp.accept(this);
        newXMLQName.prefixOp.accept(this);
    }

    @Override
    public void visit(NewStringXMLQName newStringXMLQName) {
        buf.writeByte(newStringXMLQName.kind.getValue());
        newStringXMLQName.lhsOp.accept(this);
        newStringXMLQName.stringQNameOP.accept(this);
    }

    @Override
    public void visit(XMLSeqStore xmlAddChild) {
        buf.writeByte(xmlAddChild.kind.getValue());
        xmlAddChild.lhsOp.accept(this);
        xmlAddChild.rhsOp.accept(this);
    }

    @Override
    public void visit(NewXMLComment newXMLComment) {
        buf.writeByte(newXMLComment.kind.getValue());
        newXMLComment.lhsOp.accept(this);
        newXMLComment.textOp.accept(this);
    }

    @Override
    public void visit(NewXMLProcIns newXMLProcIns) {
        buf.writeByte(newXMLProcIns.kind.getValue());
        newXMLProcIns.lhsOp.accept(this);
        newXMLProcIns.dataOp.accept(this);
        newXMLProcIns.targetOp.accept(this);
    }

    // private methods

    private void addCpAndWriteString(String string) {
        buf.writeInt(addStringCPEntry(string));
    }

    private int addStringCPEntry(String value) {
        return cp.addCPEntry(new CPEntry.StringCPEntry(value));
    }
}

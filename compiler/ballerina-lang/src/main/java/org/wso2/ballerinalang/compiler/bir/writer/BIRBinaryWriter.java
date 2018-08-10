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
import io.netty.buffer.Unpooled;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.IntegerCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.PackageCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Serialize BIR into a binary format.
 *
 * @since 0.980.0
 */
public class BIRBinaryWriter {
    private static final byte[] BIR_MAGIC = {(byte) 0xba, (byte) 0x10, (byte) 0xc0, (byte) 0xde};
    private static final int BIR_VERSION = 1;

    private ConstantPool cp = new ConstantPool();

    public byte[] write(BIRNode.BIRPackage birPackage) {
        ByteBuf birbuf = Unpooled.buffer();
        birbuf.writeBytes(BIR_MAGIC).writeInt(BIR_VERSION);

        // Write the package details in the form of constant pool entry
        int orgCPIndex = addStringCPEntry(birPackage.org.value);
        int nameCPIndex = addStringCPEntry(birPackage.name.value);
        int versionCPIndex = addStringCPEntry(birPackage.version.value);
        int pkgIndex = cp.addCPEntry(new PackageCPEntry(orgCPIndex, nameCPIndex, versionCPIndex));
        birbuf.writeInt(pkgIndex);

        // Write functions
        writeFunctions(birbuf, birPackage.functions);

        // Write the constant pool entries.
        // TODO Only one constant pool is available for now. This will change in future releases
        // TODO e.g., strtab, shstrtab, rodata.
        writeCP(birbuf);

        return birbuf.nioBuffer().array();
    }

    // private methods

    private void writeFunctions(ByteBuf buf, List<BIRNode.BIRFunction> birFunctionList) {
        buf.writeInt(birFunctionList.size());
        birFunctionList.forEach(func -> writeFunction(buf, func));
    }

    private void writeFunction(ByteBuf buf, BIRNode.BIRFunction birFunction) {
        // Function name CP Index
        buf.writeInt(addStringCPEntry(birFunction.name.value));
        // Function definition or a declaration
        // Non-zero value means this is a function declaration e.g. extern function
        buf.writeByte(birFunction.isDeclaration ? 1 : 0);
        // Visibility
        buf.writeByte(birFunction.visibility.value());

        // Function type as a CP Index
        buf.writeInt(addFuncSignature(birFunction.type));
        // Arg count
        buf.writeInt(birFunction.argsCount);
        // Local variables
        buf.writeInt(birFunction.localVars.size());
        for (BIRNode.BIRVariableDcl localVar : birFunction.localVars) {
            buf.writeByte(localVar.kind.getValue());
            buf.writeInt(addStringCPEntry(localVar.type.getDesc()));
            buf.writeInt(addStringCPEntry(localVar.name.value));
        }

        // Write basic blocks
        writeBasicBlocks(buf, birFunction.basicBlocks);
    }

    private void writeBasicBlocks(ByteBuf buf, List<BIRBasicBlock> birBBList) {
        BIRInstructionWriter insWriter = new BIRInstructionWriter(buf, cp);
        insWriter.writeBBs(birBBList);
    }

    private int addStringCPEntry(String value) {
        return cp.addCPEntry(new StringCPEntry(value));
    }

    private void writeCP(ByteBuf buf) {
        CPEntry[] cpEntries = cp.getConstPoolEntries();
        // TODO The length should be available in the const-pool section header.
        buf.writeInt(cpEntries.length);
        for (CPEntry cpEntry : cpEntries) {
            buf.writeByte(cpEntry.entryType.value);
            switch (cpEntry.entryType) {
                case CP_ENTRY_INTEGER:
                    buf.writeLong(((IntegerCPEntry) cpEntry).value);
                    break;
                case CP_ENTRY_BOOLEAN:
                    buf.writeBoolean(((CPEntry.BooleanCPEntry) cpEntry).value);
                    break;
                case CP_ENTRY_STRING:
                    StringCPEntry stringCPEntry = (StringCPEntry) cpEntry;
                    byte[] strBytes = stringCPEntry.value.getBytes(StandardCharsets.UTF_8);
                    buf.writeInt(strBytes.length).writeBytes(strBytes);
                    break;
                case CP_ENTRY_PACKAGE:
                    PackageCPEntry pkgCPEntry = (PackageCPEntry) cpEntry;
                    buf.writeInt(pkgCPEntry.orgNameCPIndex).writeInt(
                            pkgCPEntry.pkgNameCPIndex).writeInt(pkgCPEntry.versionCPIndex);
                    break;
                default:
                    throw new IllegalStateException("Unsupported constant pool entry type: " +
                            cpEntry.entryType.name());
            }
        }
    }

    private int addFuncSignature(BInvokableType funcType) {
        String funcSig = generateFunctionSig(funcType.paramTypes, funcType.retType);
        return this.cp.addCPEntry(new StringCPEntry(funcSig));
    }

    private String generateSig(List<BType> types) {
        StringBuilder builder = new StringBuilder();
        types.forEach(t -> builder.append(t.getDesc()));
        return builder.toString();
    }

    private String generateFunctionSig(List<BType> paramTypes, BType retType) {
        return "(" + generateSig(paramTypes) + ")(" + retType.getDesc() + ")";
    }
}

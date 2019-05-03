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
import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRGlobalVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.PackageCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Serialize BIR into a binary format.
 *
 * @since 0.980.0
 */
public class BIRBinaryWriter {
    private static final byte[] BIR_MAGIC = {(byte) 0xba, (byte) 0x10, (byte) 0xc0, (byte) 0xde};
    private static final int BIR_VERSION = 1;

    private final ConstantPool cp = new ConstantPool();
    private final BIRNode.BIRPackage birPackage;

    public BIRBinaryWriter(BIRNode.BIRPackage birPackage) {
        this.birPackage = birPackage;
    }

    public byte[] serialize() {
        ByteBuf birbuf = Unpooled.buffer();
        BIRTypeWriter typeWriter = new BIRTypeWriter(birbuf, cp);
        BIRInstructionWriter insWriter = new BIRInstructionWriter(birbuf, typeWriter, cp);


        // Write the package details in the form of constant pool entry
        int orgCPIndex = addStringCPEntry(birPackage.org.value);
        int nameCPIndex = addStringCPEntry(birPackage.name.value);
        int versionCPIndex = addStringCPEntry(birPackage.version.value);
        int pkgIndex = cp.addCPEntry(new PackageCPEntry(orgCPIndex, nameCPIndex, versionCPIndex));
        birbuf.writeInt(pkgIndex);

        //Write import module declarations
        writeImportModuleDecls(birbuf, birPackage.importModules);
        // Write type defs
        writeTypeDefs(birbuf, typeWriter, insWriter, birPackage.typeDefs);
        // Write global vars
        writeGlobalVars(birbuf, typeWriter, birPackage.globalVars);
        // Write type def bodies
        writeTypeDefBodies(birbuf, typeWriter, insWriter, birPackage.typeDefs);
        // Write functions
        writeFunctions(birbuf, typeWriter, insWriter, birPackage.functions);

        // Write the constant pool entries.
        // TODO Only one constant pool is available for now. This will change in future releases
        // TODO e.g., strtab, shstrtab, rodata.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dataOut = new DataOutputStream(baos)) {
            dataOut.write(BIR_MAGIC);
            dataOut.writeInt(BIR_VERSION);
            dataOut.write(cp.serialize());
            dataOut.write(birbuf.nioBuffer().array(), 0, birbuf.nioBuffer().limit());
            return baos.toByteArray();
        } catch (IOException e) {
            throw new BLangCompilerException("failed to serialize the bir", e);
        }
    }

    // private methods

    private void writeImportModuleDecls(ByteBuf buf, List<BIRNode.BIRImportModule> birImpModList) {
        buf.writeInt(birImpModList.size());
        birImpModList.forEach(impMod -> {
            buf.writeInt(addStringCPEntry(impMod.org.value));
            buf.writeInt(addStringCPEntry(impMod.name.value));
            buf.writeInt(addStringCPEntry(impMod.version.value));
        });
    }


    /**
     * Write the type definitions. Only the container will be written, to avoid
     * cyclic dependencies with global vars.
     * 
     * @param buf ByteBuf
     * @param typeWriter Type writer
     * @param insWriter Instruction writer              
     * @param birTypeDefList Type definitions list
     */
    private void writeTypeDefs(ByteBuf buf, BIRTypeWriter typeWriter, BIRInstructionWriter insWriter,
                List<BIRTypeDefinition> birTypeDefList) {
        buf.writeInt(birTypeDefList.size());
        birTypeDefList.forEach(typeDef -> writeType(buf, typeWriter, insWriter, typeDef));
    }

    /**
     * Write the body of the type definitions.
     * 
     * @param buf ByteBuf
     * @param typeWriter Type writer
     * @param birTypeDefList Type definitions list
     */
    private void writeTypeDefBodies(ByteBuf buf, BIRTypeWriter typeWriter, BIRInstructionWriter insWriter,
                                    List<BIRTypeDefinition> birTypeDefList) {
        birTypeDefList.forEach(typeDef -> writeAttachedFuncs(buf, typeWriter, insWriter, typeDef));
    }

    private void writeGlobalVars(ByteBuf buf, BIRTypeWriter typeWriter, List<BIRGlobalVariableDcl> birGlobalVars) {
        buf.writeInt(birGlobalVars.size());
        for (BIRGlobalVariableDcl birGlobalVar : birGlobalVars) {
            buf.writeByte(birGlobalVar.kind.getValue());
            // Name
            buf.writeInt(addStringCPEntry(birGlobalVar.name.value));
            // Visibility
            buf.writeByte(birGlobalVar.visibility.value());

            // Function type as a CP Index
            birGlobalVar.type.accept(typeWriter);
        }
    }

    private void writeAttachedFuncs(ByteBuf buf, BIRTypeWriter typeWriter, BIRInstructionWriter insWriter,
                                    BIRTypeDefinition typeDef) {
        int defType = typeDef.type.tag;
        if (defType == TypeTags.OBJECT || defType == TypeTags.RECORD) {
            writeFunctions(buf, typeWriter, insWriter, typeDef.attachedFuncs);
        }
    }

    private void writeType(ByteBuf buf, BIRTypeWriter typeWriter, BIRInstructionWriter insWriter,
                           BIRTypeDefinition typeDef) {
        insWriter.writePosition(typeDef.pos);
        // Type name CP Index
        buf.writeInt(addStringCPEntry(typeDef.name.value));
        // Visibility
        buf.writeByte(typeDef.visibility.value());
        typeDef.type.accept(typeWriter);
    }

    private void writeFunctions(ByteBuf buf, BIRTypeWriter typeWriter, BIRInstructionWriter insWriter,
                                List<BIRNode.BIRFunction> birFunctionList) {
        buf.writeInt(birFunctionList.size());
        birFunctionList.forEach(func -> writeFunction(buf, typeWriter, insWriter, func));
    }

    private void writeFunction(ByteBuf buf, BIRTypeWriter typeWriter, BIRInstructionWriter insWriter,
                               BIRNode.BIRFunction birFunction) {

        // Write Position
        insWriter.writePosition(birFunction.pos);
        // Function name CP Index
        buf.writeInt(addStringCPEntry(birFunction.name.value));
        // Function definition or a declaration
        // Non-zero value means this is a function declaration e.g. extern function
        buf.writeByte(birFunction.isDeclaration ? 1 : 0);
        buf.writeByte(birFunction.isInterface ? 1 : 0);
        // Visibility
        buf.writeByte(birFunction.visibility.value());

        // Function type as a CP Index
        birFunction.type.accept(typeWriter);

        // Arg count
        buf.writeInt(birFunction.argsCount);
        // Local variables
        buf.writeInt(birFunction.localVars.size());
        for (BIRNode.BIRVariableDcl localVar : birFunction.localVars) {
            buf.writeByte(localVar.kind.getValue());
            localVar.type.accept(typeWriter);
            buf.writeInt(addStringCPEntry(localVar.name.value));
        }

        // Write basic blocks
        insWriter.writeBBs(birFunction.basicBlocks);

        // Write error table
        insWriter.writeErrorTable(birFunction.errorTable);
    }

    private int addStringCPEntry(String value) {
        return cp.addCPEntry(new StringCPEntry(value));
    }
}

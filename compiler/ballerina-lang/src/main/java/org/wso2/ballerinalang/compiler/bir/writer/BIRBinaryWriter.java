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
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.ConstValue;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.TaintTable;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.ByteCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.FloatCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.IntegerCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.PackageCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serialize BIR into a binary format.
 *
 * @since 0.980.0
 */
public class BIRBinaryWriter {

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
        // Write annotations
        writeAnnotations(birbuf, typeWriter, insWriter, birPackage.annotations);
        // Write constants
        writeConstants(birbuf, birPackage.constants);

        // Write the constant pool entries.
        // TODO Only one constant pool is available for now. This will change in future releases
        // TODO e.g., strtab, shstrtab, rodata.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dataOut = new DataOutputStream(baos)) {
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
        List<BIRTypeDefinition> filtered = birTypeDefList.stream().filter(t -> t.type.tag == TypeTags.OBJECT
                || t.type.tag == TypeTags.RECORD).collect(Collectors.toList());
        filtered.forEach(typeDef -> writeFunctions(buf, typeWriter, insWriter, typeDef.attachedFuncs));
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
            typeWriter.visitType(birGlobalVar.type);
        }
    }

    private void writeType(ByteBuf buf, BIRTypeWriter typeWriter, BIRInstructionWriter insWriter,
                           BIRTypeDefinition typeDef) {
        insWriter.writePosition(typeDef.pos);
        // Type name CP Index
        buf.writeInt(addStringCPEntry(typeDef.name.value));
        // Visibility
        buf.writeByte(typeDef.visibility.value());
        typeWriter.visitType(typeDef.type);
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
        typeWriter.visitType(birFunction.type);

        buf.writeInt(birFunction.requiredParams.size());
        for (BIRParameter parameter : birFunction.requiredParams) {
            buf.writeInt(addStringCPEntry(parameter.name.value));
        }

        buf.writeInt(birFunction.defaultParams.size());
        for (BIRParameter parameter : birFunction.defaultParams) {
            buf.writeInt(addStringCPEntry(parameter.name.value));
        }

        // TODO find a better way
        boolean restParamExist = birFunction.restParam != null;
        buf.writeBoolean(restParamExist);
        if (restParamExist) {
            buf.writeInt(addStringCPEntry(birFunction.restParam.name.value));
        }

        boolean hasReceiverType = birFunction.receiverType != null;
        buf.writeBoolean(hasReceiverType);
        if (hasReceiverType) {
            typeWriter.visitType(birFunction.receiverType);
        }

        writeTaintTable(buf, birFunction.taintTable);

        ByteBuf birbuf = Unpooled.buffer();
        BIRTypeWriter funcTypeWriter = new BIRTypeWriter(birbuf, cp);
        BIRInstructionWriter funcInsWriter = new BIRInstructionWriter(birbuf, funcTypeWriter, cp);

        // Arg count
        birbuf.writeInt(birFunction.argsCount);
        // Local variables

        birbuf.writeBoolean(birFunction.returnVariable != null);
        if (birFunction.returnVariable != null) {
            birbuf.writeByte(birFunction.returnVariable.kind.getValue());
            funcTypeWriter.visitType(birFunction.returnVariable.type);
            birbuf.writeInt(addStringCPEntry(birFunction.returnVariable.name.value));
        }

        birbuf.writeInt(birFunction.parameters.size());
        for (BIRNode.BIRFunctionParameter param : birFunction.parameters.keySet()) {
            birbuf.writeByte(param.kind.getValue());
            funcTypeWriter.visitType(param.type);
            birbuf.writeInt(addStringCPEntry(param.name.value));
            birbuf.writeBoolean(param.hasDefaultExpr);
        }

        birbuf.writeInt(birFunction.localVars.size());
        for (BIRNode.BIRVariableDcl localVar : birFunction.localVars) {
            birbuf.writeByte(localVar.kind.getValue());
            funcTypeWriter.visitType(localVar.type);
            birbuf.writeInt(addStringCPEntry(localVar.name.value));
        }

        // Write basic blocks related to parameter default values
        birFunction.parameters.values().stream().filter(bbList -> !bbList.isEmpty()).forEach(funcInsWriter::writeBBs);

        // Write basic blocks
        funcInsWriter.writeBBs(birFunction.basicBlocks);

        // Write error table
        funcInsWriter.writeErrorTable(birFunction.errorTable);

        // write worker interaction channels info
        birbuf.writeInt(birFunction.workerChannels.length);
        for (BIRNode.ChannelDetails details : birFunction.workerChannels) {
            birbuf.writeInt(addStringCPEntry(details.name));
            birbuf.writeBoolean(details.channelInSameStrand);
            birbuf.writeBoolean(details.send);
        }


        // Write length of the function body so that it can be skipped easily.
        int length = birbuf.nioBuffer().limit();
        buf.writeLong(length);
        buf.writeBytes(birbuf.nioBuffer().array(), 0, length);
    }

    private void writeTaintTable(ByteBuf buf, TaintTable taintTable) {
        ByteBuf birbuf = Unpooled.buffer();
        birbuf.writeShort(taintTable.rowCount);
        birbuf.writeShort(taintTable.columnCount);
        for (Integer paramIndex : taintTable.taintTable.keySet()) {
            birbuf.writeShort(paramIndex);
            List<Byte> taintRecord = taintTable.taintTable.get(paramIndex);
            for (Byte taintStatus : taintRecord) {
                birbuf.writeByte(taintStatus);
            }
        }
        int length = birbuf.nioBuffer().limit();
        buf.writeLong(length);
        buf.writeBytes(birbuf.nioBuffer().array(), 0, length);
    }

    private void writeAnnotations(ByteBuf buf, BIRTypeWriter typeWriter, BIRInstructionWriter insWriter,
                                List<BIRNode.BIRAnnotation> birAnnotationList) {
        buf.writeInt(birAnnotationList.size());
        birAnnotationList.forEach(annotation -> writeAnnotation(buf, typeWriter, annotation));
    }

    private void writeAnnotation(ByteBuf buf, BIRTypeWriter typeWriter,
                               BIRNode.BIRAnnotation birAnnotation) {
        // Annotation name CP Index
        buf.writeInt(addStringCPEntry(birAnnotation.name.value));

        buf.writeByte(birAnnotation.visibility.value());

        buf.writeInt(birAnnotation.attachPoints);
        typeWriter.visitType(birAnnotation.annotationType);
    }

    private void writeConstants(ByteBuf buf, List<BIRNode.BIRConstant> birConstList) {
        ByteBuf birbuf = Unpooled.buffer();
        BIRTypeWriter constTypeWriter = new BIRTypeWriter(birbuf, cp);

        birbuf.writeInt(birConstList.size());
        birConstList.forEach(constant -> writeConstant(birbuf, constTypeWriter, constant));

        // Write length of the function body so that it can be skipped easily.
        int length = birbuf.nioBuffer().limit();
        buf.writeLong(length);
        buf.writeBytes(birbuf.nioBuffer().array(), 0, length);
    }

    private void writeConstant(ByteBuf buf, BIRTypeWriter typeWriter, BIRNode.BIRConstant birConstant) {
        // Annotation name CP Index
        buf.writeInt(addStringCPEntry(birConstant.name.value));
        buf.writeByte(birConstant.visibility.value());
        typeWriter.visitType(birConstant.type);
        writeConstValue(buf, typeWriter, birConstant.constValue);
    }

    private void writeConstValue(ByteBuf buf, BIRTypeWriter typeWriter, ConstValue constValue) {
        typeWriter.visitType(constValue.valueType);
        switch (constValue.valueType.tag) {
            case TypeTags.INT:
                buf.writeInt(addIntCPEntry((Long) constValue.literalValue));
                break;
            case TypeTags.BYTE:
                int byteValue = ((Number) constValue.literalValue).intValue();
                buf.writeByte(addByteCPEntry(byteValue));
                break;
            case TypeTags.FLOAT:
                // TODO:Remove the instanceof check by converting the float literal instance in Semantic analysis phase
                double doubleVal = constValue.literalValue instanceof String ?
                        Double.parseDouble((String) constValue.literalValue) : (Double) constValue.literalValue;
                buf.writeInt(addFloatCPEntry(doubleVal));
                break;
            case TypeTags.STRING:
            case TypeTags.DECIMAL:
                buf.writeInt(addStringCPEntry((String) constValue.literalValue));
                break;
            case TypeTags.BOOLEAN:
                buf.writeByte((Boolean) constValue.literalValue ? 1 : 0);
                break;
            case TypeTags.NIL:
                break;
            default:
                // TODO support for other types
                throw new UnsupportedOperationException("finite type value is not supported for type: "
                        + constValue.valueType);

        }
    }

    private int addIntCPEntry(long value) {
        return cp.addCPEntry(new IntegerCPEntry(value));
    }

    private int addFloatCPEntry(double value) {
        return cp.addCPEntry(new FloatCPEntry(value));
    }

    private int addStringCPEntry(String value) {
        return cp.addCPEntry(new StringCPEntry(value));
    }

    private int addByteCPEntry(int value) {
        return cp.addCPEntry(new ByteCPEntry(value));
    }
}

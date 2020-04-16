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
import org.ballerinalang.model.elements.AttachPoint;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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

        // Write the package details in the form of constant pool entry
        int orgCPIndex = addStringCPEntry(birPackage.org.value);
        int nameCPIndex = addStringCPEntry(birPackage.name.value);
        int versionCPIndex = addStringCPEntry(birPackage.version.value);
        int pkgIndex = cp.addCPEntry(new PackageCPEntry(orgCPIndex, nameCPIndex, versionCPIndex));
        birbuf.writeInt(pkgIndex);

        //Write import module declarations
        writeImportModuleDecls(birbuf, birPackage.importModules);
        // Write constants
        writeConstants(birbuf, birPackage.constants);
        // Write type defs
        writeTypeDefs(birbuf, birPackage.typeDefs);
        // Write global vars
        writeGlobalVars(birbuf, birPackage.globalVars);
        // Write type def bodies
        writeTypeDefBodies(birbuf, birPackage.typeDefs);
        // Write functions
        writeFunctions(birbuf, birPackage.functions);
        // Write annotations
        writeAnnotations(birbuf, birPackage.annotations);

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
     * @param birTypeDefList Type definitions list
     */
    private void writeTypeDefs(ByteBuf buf, List<BIRTypeDefinition> birTypeDefList) {
        buf.writeInt(birTypeDefList.size());
        birTypeDefList.forEach(typeDef -> writeType(buf, typeDef));
    }

    /**
     * Write the body of the type definitions.
     * 
     * @param buf ByteBuf
     * @param birTypeDefList Type definitions list
     */
    private void writeTypeDefBodies(ByteBuf buf, List<BIRTypeDefinition> birTypeDefList) {
        List<BIRTypeDefinition> filtered = birTypeDefList.stream().filter(t -> t.type.tag == TypeTags.OBJECT
                || t.type.tag == TypeTags.RECORD).collect(Collectors.toList());
        for (BIRTypeDefinition typeDef : filtered) {
            writeFunctions(buf, typeDef.attachedFuncs);
        }
    }

    private void writeGlobalVars(ByteBuf buf, List<BIRGlobalVariableDcl> birGlobalVars) {
        buf.writeInt(birGlobalVars.size());
        for (BIRGlobalVariableDcl birGlobalVar : birGlobalVars) {
            // Name
            buf.writeInt(addStringCPEntry(birGlobalVar.name.value));
            // Flags
            buf.writeInt(birGlobalVar.flags);

            DocAttachmentWriter.writeMarkdownDocAttachment(buf, birGlobalVar.markdownDocAttachment, cp);
            // Function type as a CP Index
            writeType(buf, birGlobalVar.type);
        }
    }

    private void writeType(ByteBuf buf, BIRTypeDefinition typeDef) {
        // Type name CP Index
        buf.writeInt(addStringCPEntry(typeDef.name.value));
        // Flags
        buf.writeInt(typeDef.flags);
        buf.writeByte(typeDef.isLabel ? 1 : 0);
        // write documentation
        DocAttachmentWriter.writeMarkdownDocAttachment(buf, typeDef.markdownDocAttachment, cp);
        writeType(buf, typeDef.type);
    }

    private void writeFunctions(ByteBuf buf, List<BIRNode.BIRFunction> birFunctionList) {
        buf.writeInt(birFunctionList.size());
        birFunctionList.forEach(func -> writeFunction(buf, func));
    }

    private void writeFunction(ByteBuf buf, BIRNode.BIRFunction birFunction) {
        // Write source file name
        buf.writeInt(addStringCPEntry(birFunction.pos != null ? birFunction.pos.src.cUnitName : ""));
        // Function name CP Index
        buf.writeInt(addStringCPEntry(birFunction.name.value));
        // Flags
        buf.writeInt(birFunction.flags);

        // Function type as a CP Index
        writeType(buf, birFunction.type);

        buf.writeInt(birFunction.requiredParams.size());
        for (BIRParameter parameter : birFunction.requiredParams) {
            buf.writeInt(addStringCPEntry(parameter.name.value));
            buf.writeInt(parameter.flags);
        }

        // TODO find a better way
        boolean restParamExist = birFunction.restParam != null;
        buf.writeBoolean(restParamExist);
        if (restParamExist) {
            buf.writeInt(addStringCPEntry(birFunction.restParam.name.value));
        }

        boolean hasReceiverType = birFunction.receiver != null;
        buf.writeBoolean(hasReceiverType);
        if (hasReceiverType) {
            buf.writeByte(birFunction.receiver.kind.getValue());
            writeType(buf, birFunction.receiver.type);
            buf.writeInt(addStringCPEntry(birFunction.receiver.name.value));
        }

        writeTaintTable(buf, birFunction.taintTable);

        DocAttachmentWriter.writeMarkdownDocAttachment(buf, birFunction.markdownDocAttachment, cp);
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

    private void writeAnnotations(ByteBuf buf, List<BIRNode.BIRAnnotation> birAnnotationList) {
        buf.writeInt(birAnnotationList.size());
        birAnnotationList.forEach(annotation -> writeAnnotation(buf, annotation));
    }

    private void writeAnnotation(ByteBuf buf, BIRNode.BIRAnnotation birAnnotation) {
        // Annotation name CP Index
        buf.writeInt(addStringCPEntry(birAnnotation.name.value));

        buf.writeInt(birAnnotation.flags);

        buf.writeInt(birAnnotation.attachPoints.size());
        for (AttachPoint attachPoint : birAnnotation.attachPoints) {
            buf.writeInt(addStringCPEntry(attachPoint.point.getValue()));
            buf.writeBoolean(attachPoint.source);
        }

        writeType(buf, birAnnotation.annotationType);
        DocAttachmentWriter.writeMarkdownDocAttachment(buf, birAnnotation.markdownDocAttachment, cp);
    }

    private void writeConstants(ByteBuf buf, List<BIRNode.BIRConstant> birConstList) {
        buf.writeInt(birConstList.size());
        birConstList.forEach(constant -> writeConstant(buf, constant));
    }

    private void writeConstant(ByteBuf buf, BIRNode.BIRConstant birConstant) {
        // Annotation name CP Index
        buf.writeInt(addStringCPEntry(birConstant.name.value));
        buf.writeInt(birConstant.flags);

        DocAttachmentWriter.writeMarkdownDocAttachment(buf, birConstant.markdownDocAttachment, cp);

        writeType(buf, birConstant.type);

        // write the length of the constant value, so that it can be skipped.
        ByteBuf birbuf = Unpooled.buffer();
        writeConstValue(birbuf, birConstant.constValue);
        int length = birbuf.nioBuffer().limit();
        buf.writeBytes(birbuf.nioBuffer().array(), 0, length);
    }

    private void writeConstValue(ByteBuf buf, ConstValue constValue) {
        writeType(buf, constValue.type);
        switch (constValue.type.tag) {
            case TypeTags.INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
                buf.writeInt(addIntCPEntry((Long) constValue.value));
                break;
            case TypeTags.BYTE:
                int byteValue = ((Number) constValue.value).intValue();
                buf.writeInt(addByteCPEntry(byteValue));
                break;
            case TypeTags.FLOAT:
                // TODO:Remove the instanceof check by converting the float literal instance in Semantic analysis phase
                double doubleVal = constValue.value instanceof String ? Double.parseDouble((String) constValue.value)
                        : (Double) constValue.value;
                buf.writeInt(addFloatCPEntry(doubleVal));
                break;
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
            case TypeTags.DECIMAL:
                buf.writeInt(addStringCPEntry((String) constValue.value));
                break;
            case TypeTags.BOOLEAN:
                buf.writeByte((Boolean) constValue.value ? 1 : 0);
                break;
            case TypeTags.NIL:
                break;
            case TypeTags.MAP:
                Map<String, ConstValue> mapConstVal = (Map<String, ConstValue>) constValue.value;
                buf.writeInt(mapConstVal.size());
                mapConstVal.forEach((key, value) -> {
                    buf.writeInt(addStringCPEntry(key));
                    writeConstValue(buf, value);
                });
                break;
            default:
                // TODO support for other types
                throw new UnsupportedOperationException(
                        "finite type value is not supported for type: " + constValue.type);

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

    private void writeType(ByteBuf buf, BType type) {
        buf.writeInt(cp.addShapeCPEntry(type));
    }
}

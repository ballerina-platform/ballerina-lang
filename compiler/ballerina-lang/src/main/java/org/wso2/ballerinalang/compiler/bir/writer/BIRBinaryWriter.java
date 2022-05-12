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
import io.netty.buffer.Unpooled;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRAnnotationAttachment;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRGlobalVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRParameter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.ConstValue;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.ByteCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.FloatCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.IntegerCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;

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
        BIRTypeWriter typeWriter = new BIRTypeWriter(birbuf, cp);


        // Write the package details in the form of constant pool entry
        birbuf.writeInt(BIRWriterUtils.addPkgCPEntry(birPackage.packageID, this.cp));

        //Write import module declarations
        writeImportModuleDecls(birbuf, birPackage.importModules);
        // Write constants
        writeConstants(birbuf, birPackage.constants);
        // Write type defs
        writeTypeDefs(birbuf, typeWriter, birPackage.typeDefs);
        // Write global vars
        writeGlobalVars(birbuf, typeWriter, birPackage.globalVars);
        // Write type def bodies
        writeTypeDefBodies(birbuf, typeWriter, birPackage.typeDefs);
        // Write functions
        writeFunctions(birbuf, typeWriter, birPackage.functions);
        // Write annotations
        writeAnnotations(birbuf, typeWriter, birPackage.annotations);
        // Write service declarations
        writeServiceDeclarations(birbuf, birPackage.serviceDecls);

        // Write the constant pool entries.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dataOut = new DataOutputStream(baos)) {
            dataOut.write(CompiledBinaryFile.BIRPackageFile.BIR_MAGIC);
            dataOut.writeInt(CompiledBinaryFile.BIRPackageFile.BIR_VERSION);
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
            PackageID packageID =  impMod.packageID;
            buf.writeInt(addStringCPEntry(packageID.orgName.getValue()));
            buf.writeInt(addStringCPEntry(packageID.pkgName.getValue()));
            buf.writeInt(addStringCPEntry(packageID.name.getValue()));
            buf.writeInt(addStringCPEntry(packageID.version.getValue()));
        });
    }


    /**
     * Write the type definitions. Only the container will be written, to avoid
     * cyclic dependencies with global vars.
     *
     * @param buf ByteBuf
     * @param typeWriter Type writer
     * @param birTypeDefList Type definitions list
     */
    private void writeTypeDefs(ByteBuf buf, BIRTypeWriter typeWriter,
                               List<BIRTypeDefinition> birTypeDefList) {
        buf.writeInt(birTypeDefList.size());
        birTypeDefList.forEach(typeDef -> writeType(buf, typeWriter, typeDef));
    }

    /**
     * Write the body of the type definitions.
     *
     * @param buf ByteBuf
     * @param typeWriter Type writer
     * @param birTypeDefList Type definitions list
     */
    private void writeTypeDefBodies(ByteBuf buf, BIRTypeWriter typeWriter,
                                    List<BIRTypeDefinition> birTypeDefList) {
        List<BIRTypeDefinition> filtered = birTypeDefList.stream().filter(t -> t.type.tag == TypeTags.OBJECT
                || t.type.tag == TypeTags.RECORD).collect(Collectors.toList());
        buf.writeInt(filtered.size());
        filtered.forEach(typeDef -> {
            writeFunctions(buf, typeWriter, typeDef.attachedFuncs);
            writeReferencedTypes(buf, typeDef.referencedTypes);
        });
    }

    private void writeReferencedTypes(ByteBuf buf, List<BType> referencedTypes) {
        buf.writeInt(referencedTypes.size());
        referencedTypes.forEach(type -> writeType(buf, type));
    }

    private void writeGlobalVars(ByteBuf buf, BIRTypeWriter typeWriter, List<BIRGlobalVariableDcl> birGlobalVars) {
        buf.writeInt(birGlobalVars.size());
        for (BIRGlobalVariableDcl birGlobalVar : birGlobalVars) {
            buf.writeByte(birGlobalVar.kind.getValue());
            // Name
            buf.writeInt(addStringCPEntry(birGlobalVar.name.value));
            // Flags
            buf.writeLong(birGlobalVar.flags);
            // Origin
            buf.writeByte(birGlobalVar.origin.value());

            typeWriter.writeMarkdownDocAttachment(buf, birGlobalVar.markdownDocAttachment);

            // Function type as a CP Index
            writeType(buf, birGlobalVar.type);

            writeAnnotAttachments(buf, birGlobalVar.annotAttachments);
        }
    }

    private void writeType(ByteBuf buf, BIRTypeWriter typeWriter,
                           BIRTypeDefinition typeDef) {
        writePosition(buf, typeDef.pos);
        // Type name CP Index
        buf.writeInt(addStringCPEntry(typeDef.internalName.value));
        // Type original-name CP Index
        buf.writeInt(addStringCPEntry(typeDef.originalName.value));
        // Flags
        buf.writeLong(typeDef.flags);
        // Origin
        buf.writeByte(typeDef.origin.value());
        // write documentation
        typeWriter.writeMarkdownDocAttachment(buf, typeDef.markdownDocAttachment);
        writeType(buf, typeDef.type);
        boolean hasReferenceType = typeDef.referenceType != null;
        buf.writeBoolean(hasReferenceType);
        writeAnnotAttachments(buf, typeDef.annotAttachments);
    }

    private void writeFunctions(ByteBuf buf, BIRTypeWriter typeWriter,
                                List<BIRNode.BIRFunction> birFunctionList) {
        buf.writeInt(birFunctionList.size());
        birFunctionList.forEach(func -> writeFunction(buf, typeWriter, func));
    }

    private void writeFunction(ByteBuf buf, BIRTypeWriter typeWriter, BIRNode.BIRFunction birFunction) {

        // Write Position
        writePosition(buf, birFunction.pos);
        // Function name CP Index
        buf.writeInt(addStringCPEntry(birFunction.name.value));
        // Function original name CP Index
        buf.writeInt(addStringCPEntry(birFunction.originalName.value));
        // Function worker name CP Index
        buf.writeInt(addStringCPEntry(birFunction.workerName.value));
        // Flags
        buf.writeLong(birFunction.flags);
        // Origin
        buf.writeByte(birFunction.origin.value());

        // Function type as a CP Index
        writeType(buf, birFunction.type);

        // Store annotations here...
        writeAnnotAttachments(buf, birFunction.annotAttachments);

        // Store return type annotations
        writeAnnotAttachments(buf, birFunction.returnTypeAnnots);

        buf.writeInt(birFunction.requiredParams.size());
        for (BIRParameter parameter : birFunction.requiredParams) {
            buf.writeInt(addStringCPEntry(parameter.name.value));
            buf.writeLong(parameter.flags);
            writeAnnotAttachments(buf, parameter.annotAttachments);
        }

        // TODO find a better way
        BIRParameter restParam = birFunction.restParam;
        boolean restParamExist = restParam != null;
        buf.writeBoolean(restParamExist);
        if (restParamExist) {
            buf.writeInt(addStringCPEntry(restParam.name.value));
            writeAnnotAttachments(buf, restParam.annotAttachments);
        }

        boolean hasReceiverType = birFunction.receiver != null;
        buf.writeBoolean(hasReceiverType);
        if (hasReceiverType) {
            buf.writeByte(birFunction.receiver.kind.getValue());
            writeType(buf, birFunction.receiver.type);
            buf.writeInt(addStringCPEntry(birFunction.receiver.name.value));
        }

        typeWriter.writeMarkdownDocAttachment(buf, birFunction.markdownDocAttachment);

        writeFunctionsGlobalVarDependency(buf, birFunction);

        ByteBuf birbuf = Unpooled.buffer();
        ByteBuf scopebuf = Unpooled.buffer();
        BIRInstructionWriter funcInsWriter = new BIRInstructionWriter(birbuf, scopebuf, cp, this);

        // Arg count
        birbuf.writeInt(birFunction.argsCount);

        // Write return variable, function parameters and local variables
        birbuf.writeBoolean(birFunction.returnVariable != null);
        if (birFunction.returnVariable != null) {
            birbuf.writeByte(birFunction.returnVariable.kind.getValue());
            writeType(birbuf, birFunction.returnVariable.type);
            birbuf.writeInt(addStringCPEntry(birFunction.returnVariable.name.value));
        }

        birbuf.writeInt(birFunction.parameters.size());
        for (BIRNode.BIRFunctionParameter param : birFunction.parameters.keySet()) {
            birbuf.writeByte(param.kind.getValue());
            writeType(birbuf, param.type);
            birbuf.writeInt(addStringCPEntry(param.name.value));
            if (param.kind.equals(VarKind.ARG)) {
                birbuf.writeInt(addStringCPEntry(param.metaVarName != null ? param.metaVarName : ""));
            }
            birbuf.writeBoolean(param.hasDefaultExpr);
        }

        birbuf.writeInt(birFunction.localVars.size());
        for (BIRNode.BIRVariableDcl localVar : birFunction.localVars) {
            birbuf.writeByte(localVar.kind.getValue());
            writeType(birbuf, localVar.type);
            birbuf.writeInt(addStringCPEntry(localVar.name.value));
            // skip compiler added vars and only write metaVarName for user added vars
            if (localVar.kind.equals(VarKind.ARG)) {
                birbuf.writeInt(addStringCPEntry(localVar.metaVarName != null ? localVar.metaVarName : ""));
            }
            // add enclosing basic block id
            if (localVar.kind.equals(VarKind.LOCAL)) {
                birbuf.writeInt(addStringCPEntry(localVar.metaVarName != null ? localVar.metaVarName : ""));
                birbuf.writeInt(addStringCPEntry(localVar.endBB != null ? localVar.endBB.id.value : ""));
                birbuf.writeInt(addStringCPEntry(localVar.startBB != null ? localVar.startBB.id.value : ""));
                birbuf.writeInt(localVar.insOffset);
            }
        }

        // Write basic blocks related to parameter default values
        birFunction.parameters.values().forEach(funcInsWriter::writeBBs);

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

        // Write the instruction vs scope table
        writeScopes(buf, scopebuf, funcInsWriter.getScopeCount());

        // Write length of the function body so that it can be skipped easily.
        int length = birbuf.nioBuffer().limit();
        buf.writeLong(length);
        buf.writeBytes(birbuf.nioBuffer().array(), 0, length);
    }

    private void writeFunctionsGlobalVarDependency(ByteBuf buf, BIRNode.BIRFunction birFunction) {
        buf.writeInt(birFunction.dependentGlobalVars.size());

        for (BIRNode.BIRVariableDcl var : birFunction.dependentGlobalVars) {
            buf.writeInt(addStringCPEntry(var.name.value));
        }
    }

    private void writeScopes(ByteBuf buf, ByteBuf scopebuf, int scopeCount) {
        int length = scopebuf.nioBuffer().limit();
        // 4 is the size of int which is the number of scopes that we are going to add to the beginning of the buffer
        buf.writeLong(length + 4);
        buf.writeInt(scopeCount);
        buf.writeBytes(scopebuf.nioBuffer().array(), 0, length);
    }

    private void writeAnnotations(ByteBuf buf, BIRTypeWriter typeWriter,
                                  List<BIRNode.BIRAnnotation> birAnnotationList) {
        buf.writeInt(birAnnotationList.size());
        birAnnotationList.forEach(annotation -> writeAnnotation(buf, typeWriter, annotation));
    }

    private void writeAnnotation(ByteBuf buf, BIRTypeWriter typeWriter, BIRNode.BIRAnnotation birAnnotation) {
        buf.writeInt(BIRWriterUtils.addPkgCPEntry(birAnnotation.packageID, this.cp));

        // Annotation name CP Index
        buf.writeInt(addStringCPEntry(birAnnotation.name.value));
        // Annotation original name CP Index
        buf.writeInt(addStringCPEntry(birAnnotation.originalName.value));

        buf.writeLong(birAnnotation.flags);
        buf.writeByte(birAnnotation.origin.value());
        writePosition(buf, birAnnotation.pos);

        buf.writeInt(birAnnotation.attachPoints.size());
        for (AttachPoint attachPoint : birAnnotation.attachPoints) {
            buf.writeInt(addStringCPEntry(attachPoint.point.getValue()));
            buf.writeBoolean(attachPoint.source);
        }

        writeType(buf, birAnnotation.annotationType);
        typeWriter.writeMarkdownDocAttachment(buf, birAnnotation.markdownDocAttachment);
        writeAnnotAttachments(buf, birAnnotation.annotAttachments);
    }

    private void writeConstants(ByteBuf buf, List<BIRNode.BIRConstant> birConstList) {
        BIRTypeWriter constTypeWriter = new BIRTypeWriter(buf, cp);
        buf.writeInt(birConstList.size());
        birConstList.forEach(constant -> writeConstant(buf, constTypeWriter, constant));
    }

    private void writeConstant(ByteBuf buf, BIRTypeWriter typeWriter, BIRNode.BIRConstant birConstant) {
        // Annotation name CP Index
        buf.writeInt(addStringCPEntry(birConstant.name.value));
        buf.writeLong(birConstant.flags);
        buf.writeByte(birConstant.origin.value());
        writePosition(buf, birConstant.pos);

        typeWriter.writeMarkdownDocAttachment(buf, birConstant.markdownDocAttachment);

        writeType(buf, birConstant.type);

        // write the length of the constant value, so that it can be skipped.
        ByteBuf birbuf = Unpooled.buffer();
        writeType(birbuf, birConstant.constValue.type);
        writeAnnotAttachments(buf, birConstant.annotAttachments);
        writeConstValue(birbuf, birConstant.constValue);
        int length = birbuf.nioBuffer().limit();
        buf.writeLong(length);
        buf.writeBytes(birbuf.nioBuffer().array(), 0, length);
    }

    private void writeConstValue(ByteBuf buf, ConstValue constValue) {
        writeConstValue(buf, constValue.value, constValue.type);
    }

    private void writeConstValue(ByteBuf buf, Object value, BType type) {
        switch (type.tag) {
            case TypeTags.INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
                buf.writeInt(addIntCPEntry((Long) value));
                break;
            case TypeTags.BYTE:
                int byteValue = ((Number) value).intValue();
                buf.writeInt(addByteCPEntry(byteValue));
                break;
            case TypeTags.FLOAT:
                // TODO:Remove the instanceof check by converting the float literal instance in Semantic analysis phase
                double doubleVal = value instanceof String ? Double.parseDouble((String) value)
                        : (Double) value;
                buf.writeInt(addFloatCPEntry(doubleVal));
                break;
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
            case TypeTags.DECIMAL:
                buf.writeInt(addStringCPEntry((String) value));
                break;
            case TypeTags.BOOLEAN:
                buf.writeBoolean((Boolean) value);
                break;
            case TypeTags.NIL:
                break;
            case TypeTags.RECORD:
                Map<String, ConstValue> mapConstVal = (Map<String, ConstValue>) value;
                buf.writeInt(mapConstVal.size());
                mapConstVal.forEach((key, fieldValue) -> {
                    buf.writeInt(addStringCPEntry(key));
                    writeType(buf, fieldValue.type);
                    writeConstValue(buf, fieldValue);
                });
                break;
            case TypeTags.TUPLE:
                ConstValue[] tupleConstVal = (ConstValue[]) value;
                buf.writeInt(tupleConstVal.length);
                for (ConstValue memValue : tupleConstVal) {
                    writeType(buf, memValue.type);
                    writeConstValue(buf, memValue);
                }
                break;
            case TypeTags.INTERSECTION:
                BType effectiveType = ((BIntersectionType) type).effectiveType;
                writeConstValue(buf, new ConstValue(value, effectiveType));
                break;
            default:
                // TODO support for other types
                throw new UnsupportedOperationException(
                        "finite type value is not supported for type: " + type);

        }
    }

    private void writeServiceDeclarations(ByteBuf buf,
                                          List<BIRNode.BIRServiceDeclaration> birServiceDeclList) {
        buf.writeInt(birServiceDeclList.size());
        birServiceDeclList.forEach(service -> writeServiceDeclaration(buf, service));
    }

    private void writeServiceDeclaration(ByteBuf buf, BIRNode.BIRServiceDeclaration birServiceDecl) {
        buf.writeInt(addStringCPEntry(birServiceDecl.generatedName.value));
        buf.writeInt(addStringCPEntry(birServiceDecl.associatedClassName.value));
        buf.writeLong(birServiceDecl.flags);
        buf.writeByte(birServiceDecl.origin.value());
        writePosition(buf, birServiceDecl.pos);

        buf.writeBoolean(birServiceDecl.type != null);
        if (birServiceDecl.type != null) {
            writeType(buf, birServiceDecl.type);
        }

        buf.writeBoolean(birServiceDecl.attachPoint != null);
        if (birServiceDecl.attachPoint != null) {
            buf.writeInt(birServiceDecl.attachPoint.size());

            for (String pathSegment : birServiceDecl.attachPoint) {
                buf.writeInt(addStringCPEntry(pathSegment));
            }
        }

        buf.writeBoolean(birServiceDecl.attachPointLiteral != null);
        if (birServiceDecl.attachPointLiteral != null) {
            buf.writeInt(addStringCPEntry(birServiceDecl.attachPointLiteral));
        }

        buf.writeInt(birServiceDecl.listenerTypes.size());
        for (BType listenerType : birServiceDecl.listenerTypes) {
            writeType(buf, listenerType);
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

    void writeAnnotAttachments(ByteBuf buff, List<BIRAnnotationAttachment> annotAttachments) {
        ByteBuf annotBuf = Unpooled.buffer();
        annotBuf.writeInt(annotAttachments.size());
        for (BIRAnnotationAttachment annotAttachment : annotAttachments) {
            writeAnnotAttachment(annotBuf, annotAttachment);
        }
        int length = annotBuf.nioBuffer().limit();
        buff.writeLong(length);
        buff.writeBytes(annotBuf.nioBuffer().array(), 0, length);
    }

    private void writeAnnotAttachment(ByteBuf annotBuf, BIRAnnotationAttachment annotAttachment) {
        // Write module information of the annotation attachment
        annotBuf.writeInt(BIRWriterUtils.addPkgCPEntry(annotAttachment.annotPkgId, this.cp));
        // Write position
        writePosition(annotBuf, annotAttachment.pos);
        annotBuf.writeInt(addStringCPEntry(annotAttachment.annotTagRef.value));

        if (!(annotAttachment instanceof BIRNode.BIRConstAnnotationAttachment)) {
            annotBuf.writeBoolean(false);
            return;
        }

        annotBuf.writeBoolean(true);
        ConstValue constValue = ((BIRNode.BIRConstAnnotationAttachment) annotAttachment).annotValue;
        writeType(annotBuf, constValue.type);
        writeConstValue(annotBuf, constValue);
    }

    private void writePosition(ByteBuf buf, Location pos) {
        BIRWriterUtils.writePosition(pos, buf, this.cp);
    }
}

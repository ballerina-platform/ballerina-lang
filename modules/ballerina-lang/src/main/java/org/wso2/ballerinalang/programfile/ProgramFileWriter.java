/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.programfile;


import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.programfile.attributes.AnnotationAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.CodeAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.DefaultValueAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.ErrorTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LineNumberTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LocalVariableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.ParamAnnotationAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.VarTypeCountAttributeInfo;
import org.wso2.ballerinalang.programfile.cpentries.ActionRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.ConstantPoolEntry;
import org.wso2.ballerinalang.programfile.cpentries.FloatCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.ForkJoinCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.FunctionCallCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.FunctionRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.IntegerCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.PackageRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StringCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StructureRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.TypeRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.UTF8CPEntry;
import org.wso2.ballerinalang.programfile.cpentries.WorkerDataChannelRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.WrkrInteractionArgsCPEntry;
import org.wso2.ballerinalang.util.Flags;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Dump Ballerina program model to a file.
 *
 * @since 0.90
 */
public class ProgramFileWriter {

    public static void writeProgram(ProgramFile programFile, Path execFilePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(execFilePath));
        writeProgram(programFile, bos);
    }

    public static void writeProgram(ProgramFile programFile, OutputStream programOutStream) throws IOException {
        DataOutputStream dataOutStream = null;
        try {
            // TODO improve this with Java 7 File API.
            dataOutStream = new DataOutputStream(programOutStream);

            dataOutStream.writeInt(programFile.getMagicValue());
            dataOutStream.writeShort(programFile.getVersion());

            writeCP(dataOutStream, programFile.getConstPoolEntries());
            writeEntryPoint(dataOutStream, programFile);

            // Emit package info entries;
            PackageInfo[] packageInfoEntries = programFile.getPackageInfoEntries();
            dataOutStream.writeShort(packageInfoEntries.length);
            for (PackageInfo packageInfo : packageInfoEntries) {
                writePackageInfo(dataOutStream, packageInfo);
            }

            writeAttributeInfoEntries(dataOutStream, programFile.getAttributeInfoEntries());

            dataOutStream.flush();
            dataOutStream.close();
        } finally {
            if (dataOutStream != null) {
                dataOutStream.close();
            }
        }
    }

    private static void writeCP(DataOutputStream dataOutStream,
                                ConstantPoolEntry[] constPool) throws IOException {
        dataOutStream.writeInt(constPool.length);

        for (ConstantPoolEntry cpEntry : constPool) {
            // Emitting the kind of the constant pool entry.
            dataOutStream.writeByte(cpEntry.getEntryType().getValue());

            int nameCPIndex;
            switch (cpEntry.getEntryType()) {
                case CP_ENTRY_UTF8:
                    String stringVal = ((UTF8CPEntry) cpEntry).getValue();
                    byte[] bytes = toUTF(stringVal);
                    dataOutStream.writeShort(bytes.length);
                    dataOutStream.writeUTF(stringVal);
                    break;
                case CP_ENTRY_INTEGER:
                    long longVal = ((IntegerCPEntry) cpEntry).getValue();
                    dataOutStream.writeLong(longVal);
                    break;
                case CP_ENTRY_FLOAT:
                    double doubleVal = ((FloatCPEntry) cpEntry).getValue();
                    dataOutStream.writeDouble(doubleVal);
                    break;
                case CP_ENTRY_STRING:
                    nameCPIndex = ((StringCPEntry) cpEntry).getStringCPIndex();
                    dataOutStream.writeInt(nameCPIndex);
                    break;
                case CP_ENTRY_PACKAGE:
                    nameCPIndex = ((PackageRefCPEntry) cpEntry).nameCPIndex;
                    dataOutStream.writeInt(nameCPIndex);
                    break;
                case CP_ENTRY_FUNCTION_REF:
                    FunctionRefCPEntry funcRefEntry = (FunctionRefCPEntry) cpEntry;
                    dataOutStream.writeInt(funcRefEntry.packageCPIndex);
                    dataOutStream.writeInt(funcRefEntry.nameCPIndex);
                    break;
                case CP_ENTRY_ACTION_REF:
                    ActionRefCPEntry actionRefEntry = (ActionRefCPEntry) cpEntry;
                    dataOutStream.writeInt(actionRefEntry.getPackageCPIndex());
                    dataOutStream.writeInt(actionRefEntry.getConnectorRefCPIndex());
                    dataOutStream.writeInt(actionRefEntry.getNameCPIndex());
                    break;
                case CP_ENTRY_FUNCTION_CALL_ARGS:
                    FunctionCallCPEntry funcCallEntry = (FunctionCallCPEntry) cpEntry;
                    int[] argRegs = funcCallEntry.getArgRegs();
                    dataOutStream.writeByte(argRegs.length);
                    for (int argReg : argRegs) {
                        dataOutStream.writeInt(argReg);
                    }
                    int[] retRegs = funcCallEntry.getRetRegs();
                    dataOutStream.writeByte(retRegs.length);
                    for (int retReg : retRegs) {
                        dataOutStream.writeInt(retReg);
                    }
                    break;
                case CP_ENTRY_STRUCTURE_REF:
                    StructureRefCPEntry structureRefCPEntry = (StructureRefCPEntry) cpEntry;
                    dataOutStream.writeInt(structureRefCPEntry.packageCPIndex);
                    dataOutStream.writeInt(structureRefCPEntry.nameCPIndex);
                    break;
                case CP_ENTRY_TYPE_REF:
                    TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) cpEntry;
                    dataOutStream.writeInt(typeRefCPEntry.typeSigCPIndex);
                    break;
                case CP_ENTRY_FORK_JOIN:
                    ForkJoinCPEntry forkJoinCPEntry = (ForkJoinCPEntry) cpEntry;
                    dataOutStream.writeInt(forkJoinCPEntry.forkJoinInfoIndex);
                    break;
                case CP_ENTRY_WRKR_INTERACTION:
                    WrkrInteractionArgsCPEntry workerInvokeCPEntry = (WrkrInteractionArgsCPEntry) cpEntry;
                    dataOutStream.writeInt(workerInvokeCPEntry.getTypesSignatureCPIndex());
                    int[] workerInvokeArgRegs = workerInvokeCPEntry.getArgRegs();
                    dataOutStream.writeByte(workerInvokeArgRegs.length);
                    for (int argReg : workerInvokeArgRegs) {
                        dataOutStream.writeInt(argReg);
                    }
                    break;
                case CP_ENTRY_WRKR_DATA_CHNL_REF:
                    WorkerDataChannelRefCPEntry workerDataChannelCPEntry = (WorkerDataChannelRefCPEntry) cpEntry;
                    dataOutStream.writeInt(workerDataChannelCPEntry.getUniqueNameCPIndex());
                    break;
            }
        }
    }

    private static void writeEntryPoint(DataOutputStream dataOutStream,
                                        ProgramFile programFile) throws IOException {
        dataOutStream.writeInt(programFile.entryPkgCPIndex);
        int flags = 0;
        flags = programFile.isMainEPAvailable() ? flags | ProgramFile.EP_MAIN_FLAG : flags;
        flags = programFile.isServiceEPAvailable() ? flags | ProgramFile.EP_SERVICE_FLAG : flags;
        dataOutStream.writeByte(flags);
    }

    private static void writePackageInfo(DataOutputStream dataOutStream,
                                         PackageInfo packageInfo) throws IOException {
        dataOutStream.writeInt(packageInfo.nameCPIndex);
        writeCP(dataOutStream, packageInfo.getConstPoolEntries());

        // Emit struct info entries
        StructInfo[] structTypeInfoEntries = packageInfo.getStructInfoEntries();
        dataOutStream.writeShort(structTypeInfoEntries.length);
        for (StructInfo structInfo : structTypeInfoEntries) {
            writeStructInfo(dataOutStream, structInfo);
        }

        // Emit Connector info entries
        ConnectorInfo[] connectorInfoEntries = packageInfo.getConnectorInfoEntries();
        dataOutStream.writeShort(connectorInfoEntries.length);
        for (ConnectorInfo connectorInfo : connectorInfoEntries) {
            writeConnectorInfo(dataOutStream, connectorInfo);
        }

        // TODO Emit service info entries
        ServiceInfo[] serviceInfoEntries = packageInfo.getServiceInfoEntries();
        dataOutStream.writeShort(serviceInfoEntries.length);
        for (ServiceInfo serviceInfo : serviceInfoEntries) {
            writeServiceInfo(dataOutStream, serviceInfo);
        }

        // Emit constant info entries
        writeGlobalVarInfoEntries(dataOutStream, packageInfo.getConstantInfoEntries());

        // Emit global variable info entries
        writeGlobalVarInfoEntries(dataOutStream, packageInfo.getPackageInfoEntries());

        // Emit function info entries
        dataOutStream.writeShort(packageInfo.functionInfoMap.size());
        for (FunctionInfo functionInfo : packageInfo.functionInfoMap.values()) {
            writeFunctionInfo(dataOutStream, functionInfo);
        }

        // TODO Emit AnnotationInfo entries

        // Emit Package level attributes
        writeAttributeInfoEntries(dataOutStream, packageInfo.getAttributeInfoEntries());

        // Emit instructions 
        Instruction[] instructions = packageInfo.instructionList.toArray(new Instruction[0]);
        byte[] code = writeInstructions(instructions);
        dataOutStream.writeInt(code.length);
        dataOutStream.write(code);
    }

    private static void writeGlobalVarInfoEntries(DataOutputStream dataOutStream,
                                                  PackageVarInfo[] packageVarInfoEntry) throws IOException {
        dataOutStream.writeShort(packageVarInfoEntry.length);
        for (PackageVarInfo packageVarInfo : packageVarInfoEntry) {
            dataOutStream.writeInt(packageVarInfo.nameCPIndex);
            dataOutStream.writeInt(packageVarInfo.signatureCPIndex);

            writeAttributeInfoEntries(dataOutStream, packageVarInfo.getAttributeInfoEntries());
        }
    }

    private static void writeFunctionInfo(DataOutputStream dataOutStream,
                                          FunctionInfo functionInfo) throws IOException {
        dataOutStream.writeInt(functionInfo.nameCPIndex);
        dataOutStream.writeInt(functionInfo.signatureCPIndex);

        // TODO Temp solution
        boolean b = (functionInfo.flags & Flags.NATIVE) == Flags.NATIVE;
        dataOutStream.writeByte(b ? 1 : 0);
        //dataOutStream.writeInt(functionInfo.flags);

        WorkerDataChannelInfo[] workerDataChannelInfos = functionInfo.getWorkerDataChannelInfo();
        dataOutStream.writeShort(workerDataChannelInfos.length);
        for (WorkerDataChannelInfo dataChannelInfo : workerDataChannelInfos) {
            writeWorkerDataChannelInfo(dataOutStream, dataChannelInfo);
        }

        WorkerInfo defaultWorker = functionInfo.defaultWorkerInfo;
        WorkerInfo[] workerInfoEntries = functionInfo.getWorkerInfoEntries();
        dataOutStream.writeShort(workerInfoEntries.length + 1);
        writeWorkerInfo(dataOutStream, defaultWorker);
        for (WorkerInfo workerInfo : workerInfoEntries) {
            writeWorkerInfo(dataOutStream, workerInfo);
        }

        writeAttributeInfoEntries(dataOutStream, functionInfo.getAttributeInfoEntries());
    }

    private static void writeWorkerDataChannelInfo(DataOutputStream dataOutStream,
                                                   WorkerDataChannelInfo dataChannelInfo) throws IOException {
        dataOutStream.writeInt(dataChannelInfo.getSourceCPIndex());
        dataOutStream.writeInt(dataChannelInfo.getTargetCPIndex());
        dataOutStream.writeInt(dataChannelInfo.getDataChannelRefIndex());
    }

    private static void writeStructInfo(DataOutputStream dataOutStream,
                                        StructInfo structInfo) throws IOException {
        dataOutStream.writeInt(structInfo.nameCPIndex);
        StructFieldInfo[] structFieldInfoEntries = structInfo.fieldInfoEntries.toArray(new StructFieldInfo[0]);
        dataOutStream.writeShort(structFieldInfoEntries.length);
        for (StructFieldInfo structFieldInfoEntry : structFieldInfoEntries) {
            writeStructFieldInfo(dataOutStream, structFieldInfoEntry);
        }

        // Write attribute info
        writeAttributeInfoEntries(dataOutStream, structInfo.getAttributeInfoEntries());
    }

    private static void writeConnectorInfo(DataOutputStream dataOutStream,
                                           ConnectorInfo connectorInfo) throws IOException {
        dataOutStream.writeInt(connectorInfo.nameCPIndex);
        // TODO write property flags  e.g. public
        dataOutStream.writeInt(connectorInfo.signatureCPIndex);

        ActionInfo[] actionInfoEntries = connectorInfo.actionInfoMap.values().toArray(new ActionInfo[0]);
        dataOutStream.writeShort(actionInfoEntries.length);
        for (ActionInfo actionInfo : actionInfoEntries) {
            writeActionInfo(dataOutStream, actionInfo);
        }

        // Write attribute info entries
        writeAttributeInfoEntries(dataOutStream, connectorInfo.getAttributeInfoEntries());
    }

    private static void writeServiceInfo(DataOutputStream dataOutStream,
                                         ServiceInfo serviceInfo) throws IOException {
        dataOutStream.writeInt(serviceInfo.nameCPIndex);
        dataOutStream.writeInt(serviceInfo.protocolPkgPathCPIndex);

        ResourceInfo[] resourceInfoEntries = serviceInfo.resourceInfoMap.values().toArray(new ResourceInfo[0]);
        dataOutStream.writeShort(resourceInfoEntries.length);
        for (ResourceInfo resourceInfo : resourceInfoEntries) {
            writeResourceInfo(dataOutStream, resourceInfo);
        }

        // Write attribute info entries
        writeAttributeInfoEntries(dataOutStream, serviceInfo.getAttributeInfoEntries());
    }

    private static void writeActionInfo(DataOutputStream dataOutStream,
                                        ActionInfo actionInfo) throws IOException {
        dataOutStream.writeInt(actionInfo.nameCPIndex);
        dataOutStream.writeInt(actionInfo.signatureCPIndex);
        dataOutStream.writeInt(actionInfo.flags);

        // TODO Temp solution
        boolean b = (actionInfo.flags & Flags.NATIVE) == Flags.NATIVE;
        dataOutStream.writeByte(b ? 1 : 0);

        WorkerDataChannelInfo[] workerDataChannelInfos = actionInfo.getWorkerDataChannelInfo();
        dataOutStream.writeShort(workerDataChannelInfos.length);
        for (WorkerDataChannelInfo dataChannelInfo : workerDataChannelInfos) {
            writeWorkerDataChannelInfo(dataOutStream, dataChannelInfo);
        }

        WorkerInfo defaultWorker = actionInfo.defaultWorkerInfo;
        WorkerInfo[] workerInfoEntries = actionInfo.getWorkerInfoEntries();
        dataOutStream.writeShort(workerInfoEntries.length + 1);
        writeWorkerInfo(dataOutStream, defaultWorker);
        for (WorkerInfo workerInfo : workerInfoEntries) {
            writeWorkerInfo(dataOutStream, workerInfo);
        }

        writeAttributeInfoEntries(dataOutStream, actionInfo.getAttributeInfoEntries());
    }

    private static void writeResourceInfo(DataOutputStream dataOutStream,
                                          ResourceInfo resourceInfo) throws IOException {
        dataOutStream.writeInt(resourceInfo.nameCPIndex);
        dataOutStream.writeInt(resourceInfo.signatureCPIndex);

        int[] paramNameCPIndexes = resourceInfo.paramNameCPIndexes;
        dataOutStream.writeShort(paramNameCPIndexes.length);
        for (int paramNameCPIndex : paramNameCPIndexes) {
            dataOutStream.writeInt(paramNameCPIndex);
        }

        WorkerDataChannelInfo[] workerDataChannelInfos = resourceInfo.getWorkerDataChannelInfo();
        dataOutStream.writeShort(workerDataChannelInfos.length);
        for (WorkerDataChannelInfo dataChannelInfo : workerDataChannelInfos) {
            writeWorkerDataChannelInfo(dataOutStream, dataChannelInfo);
        }

        WorkerInfo defaultWorker = resourceInfo.defaultWorkerInfo;
        WorkerInfo[] workerInfoEntries = resourceInfo.getWorkerInfoEntries();
        dataOutStream.writeShort(workerInfoEntries.length + 1);
        writeWorkerInfo(dataOutStream, defaultWorker);
        for (WorkerInfo workerInfo : workerInfoEntries) {
            writeWorkerInfo(dataOutStream, workerInfo);
        }

        writeAttributeInfoEntries(dataOutStream, resourceInfo.getAttributeInfoEntries());
    }

    private static void writeWorkerInfo(DataOutputStream dataOutStream,
                                        WorkerInfo workerInfo) throws IOException {
        dataOutStream.writeInt(workerInfo.getWorkerNameCPIndex());

        dataOutStream.writeInt(workerInfo.getWrkrDtChnlRefCPIndex());

        ForkjoinInfo[] forkjoinInfos = workerInfo.getForkjoinInfos();
        dataOutStream.writeShort(forkjoinInfos.length);
        for (ForkjoinInfo forkjoinInfo : forkjoinInfos) {
            writeForkJoinInfo(dataOutStream, forkjoinInfo);
        }

        writeAttributeInfoEntries(dataOutStream, workerInfo.getAttributeInfoEntries());
    }

    private static void writeForkJoinInfo(DataOutputStream dataOutStream,
                                          ForkjoinInfo forkjoinInfo) throws IOException {
        dataOutStream.writeInt(forkjoinInfo.getIndexCPIndex());

        int[] argRegs = forkjoinInfo.getArgRegs();
        dataOutStream.writeShort(argRegs.length);
        for (int argReg : argRegs) {
            dataOutStream.writeInt(argReg);
        }
        int[] retRegs = forkjoinInfo.getRetRegs();
        dataOutStream.writeShort(retRegs.length);
        for (int retReg : retRegs) {
            dataOutStream.writeInt(retReg);
        }

        WorkerInfo[] workerInfos = forkjoinInfo.getWorkerInfos();
        dataOutStream.writeShort(workerInfos.length);
        for (WorkerInfo workerInfo : workerInfos) {
            writeWorkerInfo(dataOutStream, workerInfo);
        }

        dataOutStream.writeBoolean(forkjoinInfo.isTimeoutAvailable());

        dataOutStream.writeInt(forkjoinInfo.getJoinTypeCPIndex());

        dataOutStream.writeInt(forkjoinInfo.getWorkerCount());

        int[] joinWrkrNameCPIndexes = forkjoinInfo.getJoinWrkrNameIndexes();
        dataOutStream.writeShort(joinWrkrNameCPIndexes.length);
        for (int cpIndex : joinWrkrNameCPIndexes) {
            dataOutStream.writeInt(cpIndex);
        }

        dataOutStream.writeInt(forkjoinInfo.getTimeoutIp());
        dataOutStream.writeInt(forkjoinInfo.getTimeoutMemOffset());
        dataOutStream.writeInt(forkjoinInfo.getJoinIp());
        dataOutStream.writeInt(forkjoinInfo.getJoinMemOffset());
    }

    private static void writeAttributeInfoEntries(DataOutputStream dataOutStream,
                                                  AttributeInfo[] attributeInfoEntries) throws IOException {
        dataOutStream.writeShort(attributeInfoEntries.length);
        for (AttributeInfo attributeInfo : attributeInfoEntries) {
            writeAttributeInfo(dataOutStream, attributeInfo);
        }
    }

    private static void writeAttributeInfo(DataOutputStream dataOutStream,
                                           AttributeInfo attributeInfo) throws IOException {
        AttributeInfo.Kind attributeKind = attributeInfo.getKind();
        dataOutStream.writeInt(attributeInfo.getAttributeNameIndex());

        switch (attributeKind) {
            case CODE_ATTRIBUTE:
                CodeAttributeInfo codeAttributeInfo = (CodeAttributeInfo) attributeInfo;
                dataOutStream.writeInt(codeAttributeInfo.codeAddrs);

                dataOutStream.writeShort(codeAttributeInfo.maxLongLocalVars);
                dataOutStream.writeShort(codeAttributeInfo.maxDoubleLocalVars);
                dataOutStream.writeShort(codeAttributeInfo.maxStringLocalVars);
                dataOutStream.writeShort(codeAttributeInfo.maxIntLocalVars);
                dataOutStream.writeShort(codeAttributeInfo.maxByteLocalVars);
                dataOutStream.writeShort(codeAttributeInfo.maxRefLocalVars);

                dataOutStream.writeShort(codeAttributeInfo.maxLongRegs);
                dataOutStream.writeShort(codeAttributeInfo.maxDoubleRegs);
                dataOutStream.writeShort(codeAttributeInfo.maxStringRegs);
                dataOutStream.writeShort(codeAttributeInfo.maxIntRegs);
                dataOutStream.writeShort(codeAttributeInfo.maxByteRegs);
                dataOutStream.writeShort(codeAttributeInfo.maxRefRegs);
                break;

            case VARIABLE_TYPE_COUNT_ATTRIBUTE:
                VarTypeCountAttributeInfo varCountAttributeInfo = (VarTypeCountAttributeInfo) attributeInfo;
                dataOutStream.writeShort(varCountAttributeInfo.getMaxLongVars());
                dataOutStream.writeShort(varCountAttributeInfo.getMaxDoubleVars());
                dataOutStream.writeShort(varCountAttributeInfo.getMaxStringVars());
                dataOutStream.writeShort(varCountAttributeInfo.getMaxIntVars());
                dataOutStream.writeShort(varCountAttributeInfo.getMaxByteVars());
                dataOutStream.writeShort(varCountAttributeInfo.getMaxRefVars());
                break;

            case ERROR_TABLE:
                ErrorTableAttributeInfo errTable = (ErrorTableAttributeInfo) attributeInfo;
                ErrorTableEntry[] errorTableEntries =
                        errTable.getErrorTableEntriesList().toArray(new ErrorTableEntry[0]);
                dataOutStream.writeShort(errorTableEntries.length);
                for (ErrorTableEntry errorTableEntry : errorTableEntries) {
                    dataOutStream.writeInt(errorTableEntry.ipFrom);
                    dataOutStream.writeInt(errorTableEntry.ipTo);
                    dataOutStream.writeInt(errorTableEntry.ipTarget);
                    dataOutStream.writeInt(errorTableEntry.priority);
                    dataOutStream.writeInt(errorTableEntry.errorStructCPIndex);
                }
                break;

            case ANNOTATIONS_ATTRIBUTE:
                AnnotationAttributeInfo annAttributeInfo = (AnnotationAttributeInfo) attributeInfo;
                AnnAttachmentInfo[] attachmentInfoEntries = annAttributeInfo.getAttachmentInfoEntries();
                dataOutStream.writeShort(attachmentInfoEntries.length);
                for (AnnAttachmentInfo attachmentInfo : attachmentInfoEntries) {
                    writeAnnAttachmentInfo(dataOutStream, attachmentInfo);
                }
                break;
            case PARAMETER_ANNOTATIONS_ATTRIBUTE:
                ParamAnnotationAttributeInfo prmAnnAtrInfo = (ParamAnnotationAttributeInfo) attributeInfo;
                ParamAnnAttachmentInfo[] prmAnnAtchmntInfo = prmAnnAtrInfo.getAttachmentInfoArray();
                dataOutStream.writeShort(prmAnnAtchmntInfo.length);
                for (ParamAnnAttachmentInfo prmAtchInfo : prmAnnAtchmntInfo) {
                    writeParamAnnAttachmentInfo(dataOutStream, prmAtchInfo);
                }
                break;
            case LOCAL_VARIABLES_ATTRIBUTE:
                LocalVariableAttributeInfo localVarAttrInfo = (LocalVariableAttributeInfo) attributeInfo;
                LocalVariableInfo[] localVarInfoArray = localVarAttrInfo.localVars.toArray(
                        new LocalVariableInfo[0]);
                dataOutStream.writeShort(localVarInfoArray.length);
                for (LocalVariableInfo localVariableInfo : localVarInfoArray) {
                    writeLocalVariableInfo(dataOutStream, localVariableInfo);
                }
                break;
            case LINE_NUMBER_TABLE_ATTRIBUTE:
                LineNumberTableAttributeInfo lnNoTblAttrInfo = (LineNumberTableAttributeInfo) attributeInfo;
                LineNumberInfo[] lineNumberInfoEntries = lnNoTblAttrInfo.getLineNumberInfoEntries();
                dataOutStream.writeShort(lineNumberInfoEntries.length);
                for (LineNumberInfo lineNumberInfo : lineNumberInfoEntries) {
                    writeLineNumberInfo(dataOutStream, lineNumberInfo);
                }
                break;
            case DEFAULT_VALUE_ATTRIBUTE:
                DefaultValueAttributeInfo defaultValAttrInfo = (DefaultValueAttributeInfo) attributeInfo;
                writeDefaultValue(dataOutStream, defaultValAttrInfo.getDefaultValue());
                break;
        }

        // TODO Support other types of attributes
    }

    private static byte[] writeInstructions(Instruction[] instructions) throws IOException {
        ByteArrayOutputStream byteAOS = new ByteArrayOutputStream();
        DataOutputStream dataOutStream = new DataOutputStream(byteAOS);
        for (Instruction instruction : instructions) {
            dataOutStream.write(instruction.opcode);
            int[] operands = instruction.operands;
            for (int operand : operands) {
                dataOutStream.writeInt(operand);
            }
        }
        return byteAOS.toByteArray();
    }

    private static void writeStructFieldInfo(DataOutputStream dataOutStream,
                                             StructFieldInfo structFieldInfo) throws IOException {
        dataOutStream.writeInt(structFieldInfo.nameCPIndex);
        dataOutStream.writeInt(structFieldInfo.signatureCPIndex);

        // Write attribute info
        writeAttributeInfoEntries(dataOutStream, structFieldInfo.getAttributeInfoEntries());
    }

    private static void writeAnnAttachmentInfo(DataOutputStream dataOutStream,
                                               AnnAttachmentInfo attachmentInfo) throws IOException {
        dataOutStream.writeInt(attachmentInfo.getPkgPathCPIndex());
        dataOutStream.writeInt(attachmentInfo.getNameCPIndex());

        AnnAttributeKeyValuePair[] attribKeyValuePairs = attachmentInfo.getAttributeKeyValuePairs();
        dataOutStream.writeShort(attribKeyValuePairs.length);
        for (AnnAttributeKeyValuePair keyValuePair : attribKeyValuePairs) {
            dataOutStream.writeInt(keyValuePair.getAttributeNameCPIndex());
            writeAnnAttributeValue(dataOutStream, keyValuePair.getAttributeValue());
        }
    }

    private static void writeParamAnnAttachmentInfo(DataOutputStream dataOutStream,
                                                    ParamAnnAttachmentInfo attachmentInfo) throws IOException {
        dataOutStream.writeInt(attachmentInfo.getParamIdex());

        AnnAttachmentInfo[] annAttachmentInfos = attachmentInfo.getAnnAttachmentInfos();
        dataOutStream.writeShort(annAttachmentInfos.length);
        for (AnnAttachmentInfo annAttachmentInfo : annAttachmentInfos) {
            writeAnnAttachmentInfo(dataOutStream, annAttachmentInfo);
        }
    }

    private static void writeLocalVariableInfo(DataOutputStream dataOutStream,
                                               LocalVariableInfo localVariableInfo) throws IOException {
        dataOutStream.writeInt(localVariableInfo.varNameCPIndex);
        dataOutStream.writeInt(localVariableInfo.varIndex);
        dataOutStream.writeInt(localVariableInfo.varTypeSigCPIndex);

        int[] attachmentsIndexes = localVariableInfo.attachmentIndexes;
        dataOutStream.writeShort(attachmentsIndexes.length);
        for (int attachmentIndex : attachmentsIndexes) {
            dataOutStream.writeInt(attachmentIndex);
        }
    }

    private static void writeLineNumberInfo(DataOutputStream dataOutStream,
                                            LineNumberInfo lineNumberInfo) throws IOException {
        dataOutStream.writeInt(lineNumberInfo.getLineNumber());
        dataOutStream.writeInt(lineNumberInfo.getFileNameCPIndex());
        dataOutStream.writeInt(lineNumberInfo.getIp());
    }

    private static void writeAnnAttributeValue(DataOutputStream dataOutStream,
                                               AnnAttributeValue attributeValue) throws IOException {
        dataOutStream.writeInt(attributeValue.getTypeDescCPIndex());
        dataOutStream.writeBoolean(attributeValue.isConstVarExpr());
        if (attributeValue.isConstVarExpr()) {
            dataOutStream.writeInt(attributeValue.getConstPkgCPIndex());
            dataOutStream.writeInt(attributeValue.getConstNameCPIndex());
            return;
        }
        String typeDesc = attributeValue.getTypeDesc();
        if (TypeDescriptor.SIG_ANNOTATION.equals(typeDesc)) {
            writeAnnAttachmentInfo(dataOutStream, attributeValue.getAnnotationAttachmentValue());
        } else if (TypeDescriptor.SIG_ARRAY.equals(typeDesc)) {
            AnnAttributeValue[] attributeValues = attributeValue.getAttributeValueArray();
            dataOutStream.writeShort(attributeValues.length);
            for (AnnAttributeValue value : attributeValues) {
                writeAnnAttributeValue(dataOutStream, value);
            }
        } else if (TypeDescriptor.SIG_BOOLEAN.equals(typeDesc)) {
            dataOutStream.writeBoolean(attributeValue.getBooleanValue());
        } else {
            dataOutStream.writeInt(attributeValue.getValueCPIndex());
        }
    }

    private static byte[] toUTF(String value) throws IOException {
        ByteArrayOutputStream byteAOS = new ByteArrayOutputStream();
        DataOutputStream dataOutStream = new DataOutputStream(byteAOS);
        dataOutStream.writeUTF(value);
        return byteAOS.toByteArray();
    }

    private static void writeDefaultValue(DataOutputStream dataOutStream, StructFieldDefaultValue defaultValueInfo)
            throws IOException {
        dataOutStream.writeInt(defaultValueInfo.typeDescCPIndex);
        String typeDesc = defaultValueInfo.desc;
        if (TypeDescriptor.SIG_BOOLEAN.equals(typeDesc)) {
            dataOutStream.writeBoolean(defaultValueInfo.booleanValue);
        } else {
            dataOutStream.writeInt(defaultValueInfo.valueCPIndex);
        }
    }
}

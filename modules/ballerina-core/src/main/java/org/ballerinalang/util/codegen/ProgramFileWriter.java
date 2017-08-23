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
package org.ballerinalang.util.codegen;

import org.ballerinalang.model.types.TypeSignature;
import org.ballerinalang.util.codegen.attributes.AnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo.Kind;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.codegen.attributes.DefaultValueAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ErrorTableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LineNumberTableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ParamAnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.VarTypeCountAttributeInfo;
import org.ballerinalang.util.codegen.cpentries.ActionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;
import org.ballerinalang.util.codegen.cpentries.FloatCPEntry;
import org.ballerinalang.util.codegen.cpentries.ForkJoinCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionCallCPEntry;
import org.ballerinalang.util.codegen.cpentries.FunctionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.IntegerCPEntry;
import org.ballerinalang.util.codegen.cpentries.PackageRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.StringCPEntry;
import org.ballerinalang.util.codegen.cpentries.StructureRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.TypeRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;
import org.ballerinalang.util.codegen.cpentries.WorkerDataChannelRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.WrkrInteractionArgsCPEntry;
import org.ballerinalang.util.exceptions.ProgramFileFormatException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Dump Ballerina program model to a file.
 *
 * @since 0.90
 */
public class ProgramFileWriter {

    public static void writeProgram(ProgramFile programFile, Path execFilePath) throws IOException {
        DataOutputStream dataOutStream = null;
        try {
            // TODO improve this with Java 7 File API.
            BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(execFilePath));
            dataOutStream = new DataOutputStream(bos);

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
                    nameCPIndex = ((PackageRefCPEntry) cpEntry).getNameCPIndex();
                    dataOutStream.writeInt(nameCPIndex);
                    break;
                case CP_ENTRY_FUNCTION_REF:
                    FunctionRefCPEntry funcRefEntry = (FunctionRefCPEntry) cpEntry;
                    dataOutStream.writeInt(funcRefEntry.getPackageCPIndex());
                    dataOutStream.writeInt(funcRefEntry.getNameCPIndex());
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
                    dataOutStream.writeInt(structureRefCPEntry.getPackageCPIndex());
                    dataOutStream.writeInt(structureRefCPEntry.getNameCPIndex());
                    break;
                case CP_ENTRY_TYPE_REF:
                    TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) cpEntry;
                    dataOutStream.writeInt(typeRefCPEntry.getTypeSigCPIndex());
                    break;
                case CP_ENTRY_FORK_JOIN:
                    ForkJoinCPEntry forkJoinCPEntry = (ForkJoinCPEntry) cpEntry;
                    dataOutStream.writeInt(forkJoinCPEntry.getForkJoinCPIndex());
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
                default:
                    throw new ProgramFileFormatException("invalid constant pool entry " + cpEntry.getEntryType());
            }
        }
    }

    private static void writeEntryPoint(DataOutputStream dataOutStream,
                                        ProgramFile programFile) throws IOException {
        dataOutStream.writeInt(programFile.getEntryPkgCPIndex());
        int flags = 0;
        flags = programFile.isMainEPAvailable() ? flags | ProgramFile.EP_MAIN_FLAG : flags;
        flags = programFile.isServiceEPAvailable() ? flags | ProgramFile.EP_SERVICE_FLAG : flags;
        dataOutStream.writeByte(flags);
    }

    private static void writePackageInfo(DataOutputStream dataOutStream,
                                         PackageInfo packageInfo) throws IOException {
        dataOutStream.writeInt(packageInfo.getPkgNameCPIndex());
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
        FunctionInfo[] funcInfoEntries = packageInfo.getFunctionInfoEntries();
        dataOutStream.writeShort(funcInfoEntries.length);
        for (FunctionInfo functionInfo : funcInfoEntries) {
            writeFunctionInfo(dataOutStream, functionInfo);
        }

        // TODO Emit AnnotationInfo entries

        // Emit Package level attributes
        writeAttributeInfoEntries(dataOutStream, packageInfo.getAttributeInfoEntries());

        // Emit instructions 
        Instruction[] instructions = packageInfo.getInstructions();
        byte[] code = writeInstructions(instructions);
        dataOutStream.writeInt(code.length);
        dataOutStream.write(code);
    }

    private static void writeGlobalVarInfoEntries(DataOutputStream dataOutStream,
                                                  PackageVarInfo[] packageVarInfoEntry) throws IOException {
        dataOutStream.writeShort(packageVarInfoEntry.length);
        for (PackageVarInfo packageVarInfo : packageVarInfoEntry) {
            dataOutStream.writeInt(packageVarInfo.getNameCPIndex());
            dataOutStream.writeInt(packageVarInfo.getSignatureCPIndex());

            writeAttributeInfoEntries(dataOutStream, packageVarInfo.getAttributeInfoEntries());
        }
    }

    private static void writeFunctionInfo(DataOutputStream dataOutStream,
                                          FunctionInfo functionInfo) throws IOException {
        dataOutStream.writeInt(functionInfo.getNameCPIndex());
        dataOutStream.writeInt(functionInfo.getSignatureCPIndex());

        // TODO Set the callable unit property flags - Hard coding 'native' property
        // TODO We need introduce 'public' keyword soon.
        dataOutStream.writeByte(functionInfo.isNative() ? 1 : 0);

        WorkerDataChannelInfo[] workerDataChannelInfos = functionInfo.getWorkerDataChannelInfo();
        dataOutStream.writeShort(workerDataChannelInfos.length);
        for (WorkerDataChannelInfo dataChannelInfo : workerDataChannelInfos) {
            writeWorkerDataChannelInfo(dataOutStream, dataChannelInfo);
        }

        WorkerInfo defaultWorker = functionInfo.getDefaultWorkerInfo();
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
        dataOutStream.writeInt(structInfo.getNameCPIndex());
        StructFieldInfo[] structFieldInfoEntries = structInfo.getFieldInfoEntries();
        dataOutStream.writeShort(structFieldInfoEntries.length);
        for (StructFieldInfo structFieldInfoEntry : structFieldInfoEntries) {
            writeStructFieldInfo(dataOutStream, structFieldInfoEntry);
        }

        // Write attribute info
        writeAttributeInfoEntries(dataOutStream, structInfo.getAttributeInfoEntries());
    }

    private static void writeConnectorInfo(DataOutputStream dataOutStream,
                                           ConnectorInfo connectorInfo) throws IOException {
        dataOutStream.writeInt(connectorInfo.getNameCPIndex());
        // TODO write property flags  e.g. public
        dataOutStream.writeInt(connectorInfo.getSignatureCPIndex());

        Map<Integer, Integer> methodTable = connectorInfo.getMethodTableIndex();
        dataOutStream.writeInt(methodTable.size());
        for (Integer s : methodTable.keySet()) {
            dataOutStream.writeInt(s);
            dataOutStream.writeInt(methodTable.get(s));
        }

        dataOutStream.writeBoolean(connectorInfo.isFilterConnector());

        ActionInfo[] actionInfoEntries = connectorInfo.getActionInfoEntries();
        dataOutStream.writeShort(actionInfoEntries.length);
        for (ActionInfo actionInfo : actionInfoEntries) {
            writeActionInfo(dataOutStream, actionInfo);
        }

        // Write attribute info entries
        writeAttributeInfoEntries(dataOutStream, connectorInfo.getAttributeInfoEntries());
    }

    private static void writeServiceInfo(DataOutputStream dataOutStream,
                                         ServiceInfo serviceInfo) throws IOException {
        dataOutStream.writeInt(serviceInfo.getNameCPIndex());
        dataOutStream.writeInt(serviceInfo.getProtocolPkgPathCPIndex());

        ResourceInfo[] resourceInfoEntries = serviceInfo.getResourceInfoEntries();
        dataOutStream.writeShort(resourceInfoEntries.length);
        for (ResourceInfo resourceInfo : resourceInfoEntries) {
            writeResourceInfo(dataOutStream, resourceInfo);
        }

        // Write attribute info entries
        writeAttributeInfoEntries(dataOutStream, serviceInfo.getAttributeInfoEntries());
    }

    private static void writeActionInfo(DataOutputStream dataOutStream,
                                        ActionInfo actionInfo) throws IOException {
        dataOutStream.writeInt(actionInfo.getNameCPIndex());
        dataOutStream.writeInt(actionInfo.getSignatureCPIndex());

        // TODO Set the callable unit property flags - Hard coding 'native' property
        // TODO We need introduce 'public' keyword soon.
        dataOutStream.writeByte(actionInfo.isNative() ? 1 : 0);

        WorkerDataChannelInfo[] workerDataChannelInfos = actionInfo.getWorkerDataChannelInfo();
        dataOutStream.writeShort(workerDataChannelInfos.length);
        for (WorkerDataChannelInfo dataChannelInfo : workerDataChannelInfos) {
            writeWorkerDataChannelInfo(dataOutStream, dataChannelInfo);
        }

        WorkerInfo defaultWorker = actionInfo.getDefaultWorkerInfo();
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
        dataOutStream.writeInt(resourceInfo.getNameCPIndex());
        dataOutStream.writeInt(resourceInfo.getSignatureCPIndex());

        int[] paramNameCPIndexes = resourceInfo.getParamNameCPIndexes();
        dataOutStream.writeShort(paramNameCPIndexes.length);
        for (int paramNameCPIndex : paramNameCPIndexes) {
            dataOutStream.writeInt(paramNameCPIndex);
        }

        WorkerDataChannelInfo[] workerDataChannelInfos = resourceInfo.getWorkerDataChannelInfo();
        dataOutStream.writeShort(workerDataChannelInfos.length);
        for (WorkerDataChannelInfo dataChannelInfo : workerDataChannelInfos) {
            writeWorkerDataChannelInfo(dataOutStream, dataChannelInfo);
        }

        WorkerInfo defaultWorker = resourceInfo.getDefaultWorkerInfo();
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

//        dataOutStream.writeInt(forkjoinInfo.getWorkerCount()); //TODO

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
        Kind attributeKind = attributeInfo.getKind();
        dataOutStream.writeInt(attributeInfo.getAttributeNameIndex());

        switch (attributeKind) {
            case CODE_ATTRIBUTE:
                CodeAttributeInfo codeAttributeInfo = (CodeAttributeInfo) attributeInfo;
                dataOutStream.writeInt(codeAttributeInfo.getCodeAddrs());

                dataOutStream.writeShort(codeAttributeInfo.getMaxLongLocalVars());
                dataOutStream.writeShort(codeAttributeInfo.getMaxDoubleLocalVars());
                dataOutStream.writeShort(codeAttributeInfo.getMaxStringLocalVars());
                dataOutStream.writeShort(codeAttributeInfo.getMaxIntLocalVars());
                dataOutStream.writeShort(codeAttributeInfo.getMaxByteLocalVars());
                dataOutStream.writeShort(codeAttributeInfo.getMaxRefLocalVars());

                dataOutStream.writeShort(codeAttributeInfo.getMaxLongRegs());
                dataOutStream.writeShort(codeAttributeInfo.getMaxDoubleRegs());
                dataOutStream.writeShort(codeAttributeInfo.getMaxStringRegs());
                dataOutStream.writeShort(codeAttributeInfo.getMaxIntRegs());
                dataOutStream.writeShort(codeAttributeInfo.getMaxByteRegs());
                dataOutStream.writeShort(codeAttributeInfo.getMaxRefRegs());
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
                LocalVariableAttributeInfo localVariableAttributeInfo = (LocalVariableAttributeInfo) attributeInfo;
                LocalVariableInfo[] localVariableInfos = localVariableAttributeInfo.getLocalVariableInfoEntries();
                dataOutStream.writeShort(localVariableInfos.length);
                for (LocalVariableInfo localVariableInfo : localVariableInfos) {
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
        dataOutStream.writeInt(structFieldInfo.getNameCPIndex());
        dataOutStream.writeInt(structFieldInfo.getSignatureCPIndex());

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
        dataOutStream.writeInt(localVariableInfo.getVariableNameCPIndex());
        dataOutStream.writeInt(localVariableInfo.getVariableIndex());
        dataOutStream.writeInt(localVariableInfo.getVarTypeSigCPIndex());

        int[] attachemntsIndexes = localVariableInfo.getAttachmentIndexes();
        dataOutStream.writeShort(attachemntsIndexes.length);
        for (int attachmentIndex : attachemntsIndexes) {
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
        if (TypeSignature.SIG_ANNOTATION.equals(typeDesc)) {
            writeAnnAttachmentInfo(dataOutStream, attributeValue.getAnnotationAttachmentValue());
        } else if (TypeSignature.SIG_ARRAY.equals(typeDesc)) {
            AnnAttributeValue[] attributeValues = attributeValue.getAttributeValueArray();
            dataOutStream.writeShort(attributeValues.length);
            for (AnnAttributeValue value : attributeValues) {
                writeAnnAttributeValue(dataOutStream, value);
            }
        } else if (TypeSignature.SIG_BOOLEAN.equals(typeDesc)) {
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
        dataOutStream.writeInt(defaultValueInfo.getTypeDescCPIndex());
        String typeDesc = defaultValueInfo.getTypeDesc();
        if (TypeSignature.SIG_BOOLEAN.equals(typeDesc)) {
            dataOutStream.writeBoolean(defaultValueInfo.getBooleanValue());
        } else {
            dataOutStream.writeInt(defaultValueInfo.getValueCPIndex());
        }
    }
}

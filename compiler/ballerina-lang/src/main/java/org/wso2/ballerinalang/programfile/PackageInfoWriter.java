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
import org.wso2.ballerinalang.programfile.Instruction.Operand;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.CodeAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.DefaultValueAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.ErrorTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LineNumberTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LocalVariableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.ParamDefaultValueAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.ParameterAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.TaintTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.VarTypeCountAttributeInfo;
import org.wso2.ballerinalang.programfile.cpentries.ActionRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.ConstantPoolEntry;
import org.wso2.ballerinalang.programfile.cpentries.FloatCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.ForkJoinCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.FunctionRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.IntegerCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.PackageRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StringCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StructureRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.TransformerRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.TypeRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.UTF8CPEntry;
import org.wso2.ballerinalang.programfile.cpentries.WorkerDataChannelRefCPEntry;
import org.wso2.ballerinalang.util.Flags;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Serialize Ballerina {@code PackageInfo} structure to a byte stream.
 *
 * @since 0.90
 */
public class PackageInfoWriter {

    // Size to be written to tag a null value
    private static final int NULL_VALUE_FIELD_SIZE_TAG = -1;

    public static void writeCP(DataOutputStream dataOutStream,
                               ConstantPoolEntry[] constPool) throws IOException {
        dataOutStream.writeInt(constPool.length);
        for (ConstantPoolEntry cpEntry : constPool) {
            // Emitting the kind of the constant pool entry.
            dataOutStream.writeByte(cpEntry.getEntryType().getValue());
            int nameCPIndex;
            switch (cpEntry.getEntryType()) {
                case CP_ENTRY_UTF8:
                    String stringVal = ((UTF8CPEntry) cpEntry).getValue();
                    if (stringVal != null) {
                        byte[] bytes = toUTF(stringVal);
                        dataOutStream.writeShort(bytes.length);
                        dataOutStream.write(bytes);
                    } else {
                        // If the string value is null, we write the size as -1. 
                        // This marks that the value followed by -1 size is a null value.
                        dataOutStream.writeShort(NULL_VALUE_FIELD_SIZE_TAG);
                    }
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
                    int versionCPIndex = ((PackageRefCPEntry) cpEntry).versionCPIndex;
                    dataOutStream.writeInt(versionCPIndex);
                    break;
                case CP_ENTRY_FUNCTION_REF:
                    FunctionRefCPEntry funcRefEntry = (FunctionRefCPEntry) cpEntry;
                    dataOutStream.writeInt(funcRefEntry.packageCPIndex);
                    dataOutStream.writeInt(funcRefEntry.nameCPIndex);
                    break;
                case CP_ENTRY_ACTION_REF:
                    ActionRefCPEntry actionRefEntry = (ActionRefCPEntry) cpEntry;
                    dataOutStream.writeInt(actionRefEntry.getPackageCPIndex());
                    dataOutStream.writeInt(actionRefEntry.getNameCPIndex());
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
                case CP_ENTRY_WRKR_DATA_CHNL_REF:
                    WorkerDataChannelRefCPEntry workerDataChannelCPEntry = (WorkerDataChannelRefCPEntry) cpEntry;
                    dataOutStream.writeInt(workerDataChannelCPEntry.getUniqueNameCPIndex());
                    break;
                case CP_ENTRY_TRANSFORMER_REF:
                    TransformerRefCPEntry transformerRefEntry = (TransformerRefCPEntry) cpEntry;
                    dataOutStream.writeInt(transformerRefEntry.packageCPIndex);
                    dataOutStream.writeInt(transformerRefEntry.nameCPIndex);
                    break;
            }
        }
    }

    public static byte[] getPackageBinary(PackageInfo packageInfo) throws IOException {
        try (ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream()) {
            DataOutputStream dataOutStream = new DataOutputStream(byteArrayOS);
            writePackageInfo(packageInfo, dataOutStream);
            return byteArrayOS.toByteArray();
        }
    }

    public static void writePackageInfo(PackageInfo packageInfo, DataOutputStream dataOutStream) throws IOException {
        // Package level CP entries
        writeCP(dataOutStream, packageInfo.getConstPoolEntries());

        // Write package name and version number
        dataOutStream.writeInt(packageInfo.nameCPIndex);
        dataOutStream.writeInt(packageInfo.versionCPIndex);

        // Write import package entries
        dataOutStream.writeShort(packageInfo.importPkgInfoSet.size());
        for (ImportPackageInfo importPkgInfo : packageInfo.importPkgInfoSet) {
            dataOutStream.writeInt(importPkgInfo.nameCPIndex);
            dataOutStream.writeInt(importPkgInfo.versionCPIndex);
        }

        // Emit struct info entries
        StructInfo[] structTypeInfoEntries = packageInfo.getStructInfoEntries();
        dataOutStream.writeShort(structTypeInfoEntries.length);
        for (StructInfo structInfo : structTypeInfoEntries) {
            writeStructInfo(dataOutStream, structInfo);
        }

        // Write Type Definition entries
        TypeDefinitionInfo[] typeDefEntries = packageInfo.getTypeDefinitionInfoEntries();
        dataOutStream.writeShort(typeDefEntries.length);
        for (TypeDefinitionInfo typeDefInfo : typeDefEntries) {
            writeTypeDefinitionInfo(dataOutStream, typeDefInfo);
        }

        // TODO Emit service info entries
        ServiceInfo[] serviceInfoEntries = packageInfo.getServiceInfoEntries();
        dataOutStream.writeShort(serviceInfoEntries.length);
        for (ServiceInfo serviceInfo : serviceInfoEntries) {
            writeServiceInfo(dataOutStream, serviceInfo);
        }

        for (ServiceInfo serviceInfo : serviceInfoEntries) {
            writeResourceInfo(dataOutStream, serviceInfo);
        }

        // Emit global variable info entries
        writeGlobalVarInfoEntries(dataOutStream, packageInfo.getPackageInfoEntries());

        // Emit function info entries
        dataOutStream.writeShort(packageInfo.functionInfoMap.size());
        for (FunctionInfo functionInfo : packageInfo.functionInfoMap.values()) {
            writeCallableUnitInfo(dataOutStream, functionInfo);
        }

        // Emit Package level attributes
        writeAttributeInfoEntries(dataOutStream, packageInfo.getAttributeInfoEntries());

        // Emit instructions
        Instruction[] instructions = packageInfo.instructionList.toArray(new Instruction[0]);
        byte[] code = writeInstructions(instructions);
        dataOutStream.writeInt(code.length);
        dataOutStream.write(code);
    }

    public static void writeAttributeInfoEntries(DataOutputStream dataOutStream,
                                                 AttributeInfo[] attributeInfoEntries) throws IOException {
        dataOutStream.writeShort(attributeInfoEntries.length);
        for (AttributeInfo attributeInfo : attributeInfoEntries) {
            writeAttributeInfo(dataOutStream, attributeInfo);
        }
    }


    // Private methods

    private static void writeGlobalVarInfoEntries(DataOutputStream dataOutStream,
                                                  PackageVarInfo[] packageVarInfoEntry) throws IOException {
        dataOutStream.writeShort(packageVarInfoEntry.length);
        for (PackageVarInfo packageVarInfo : packageVarInfoEntry) {
            dataOutStream.writeInt(packageVarInfo.nameCPIndex);
            dataOutStream.writeInt(packageVarInfo.signatureCPIndex);
            dataOutStream.writeInt(packageVarInfo.globalMemIndex);

            writeAttributeInfoEntries(dataOutStream, packageVarInfo.getAttributeInfoEntries());
        }
    }

    /**
     * Write function info and transformer info entries to the compiling file.
     *
     * @param dataOutStream    Output stream to write
     * @param callableUnitInfo Info object of the callable unit
     * @throws IOException if an I/O error occurs.
     */
    private static void writeCallableUnitInfo(DataOutputStream dataOutStream,
                                              CallableUnitInfo callableUnitInfo) throws IOException {
        dataOutStream.writeInt(callableUnitInfo.nameCPIndex);
        dataOutStream.writeInt(callableUnitInfo.signatureCPIndex);
        dataOutStream.writeInt(callableUnitInfo.flags);
        boolean attached = (callableUnitInfo.flags & Flags.ATTACHED) == Flags.ATTACHED;
        if (attached) {
            dataOutStream.writeInt(callableUnitInfo.attachedToTypeCPIndex);
        }

        ByteArrayOutputStream workerDataBAOS = new ByteArrayOutputStream();
        DataOutputStream workerDataDOS = new DataOutputStream(workerDataBAOS);

        WorkerDataChannelInfo[] workerDataChannelInfoEntries = callableUnitInfo.getWorkerDataChannelInfo();
        workerDataDOS.writeShort(workerDataChannelInfoEntries.length);
        for (WorkerDataChannelInfo dataChannelInfo : workerDataChannelInfoEntries) {
            writeWorkerDataChannelInfo(workerDataDOS, dataChannelInfo);
        }

        WorkerInfo defaultWorker = callableUnitInfo.defaultWorkerInfo;
        WorkerInfo[] workerInfoEntries = callableUnitInfo.getWorkerInfoEntries();
        workerDataDOS.writeShort(workerInfoEntries.length + 1);
        writeWorkerInfo(workerDataDOS, defaultWorker);
        for (WorkerInfo workerInfo : workerInfoEntries) {
            writeWorkerInfo(workerDataDOS, workerInfo);
        }

        byte[] workerData = workerDataBAOS.toByteArray();
        dataOutStream.writeInt(workerData.length);
        dataOutStream.write(workerData);

        writeAttributeInfoEntries(dataOutStream, callableUnitInfo.getAttributeInfoEntries());
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
        dataOutStream.writeInt(structInfo.flags);

        // Write struct field info entries
        dataOutStream.writeShort(structInfo.fieldInfoEntries.size());
        for (StructFieldInfo structFieldInfoEntry : structInfo.fieldInfoEntries) {
            writeStructFieldInfo(dataOutStream, structFieldInfoEntry);
        }

        // Write attached function info entries
        dataOutStream.writeShort(structInfo.attachedFuncInfoEntries.size());
        for (AttachedFunctionInfo attachedFuncInfo : structInfo.attachedFuncInfoEntries) {
            writeAttachedFunctionInfo(dataOutStream, attachedFuncInfo);
        }

        // Write attribute info
        writeAttributeInfoEntries(dataOutStream, structInfo.getAttributeInfoEntries());
    }

    private static void writeTypeDefinitionInfo(DataOutputStream dataOutStream,
                                                TypeDefinitionInfo typeDefinitionInfo) throws IOException {
        dataOutStream.writeInt(typeDefinitionInfo.nameCPIndex);
        dataOutStream.writeInt(typeDefinitionInfo.flags);
        ValueSpaceItemInfo[] valueSpaceItemInfos = typeDefinitionInfo.
                valueSpaceItemInfos.toArray(new ValueSpaceItemInfo[0]);
        dataOutStream.writeShort(typeDefinitionInfo.typeDescCPIndexes.size());
        for (int typeDescCPindex : typeDefinitionInfo.typeDescCPIndexes) {
            dataOutStream.writeInt(typeDescCPindex);
        }

        dataOutStream.writeShort(valueSpaceItemInfos.length);
        for (ValueSpaceItemInfo valueSpaceItem : valueSpaceItemInfos) {
            writeDefaultValue(dataOutStream, valueSpaceItem.value);
        }

        // Write attribute info
        writeAttributeInfoEntries(dataOutStream, typeDefinitionInfo.getAttributeInfoEntries());
    }

    private static void writeServiceInfo(DataOutputStream dataOutStream,
                                         ServiceInfo serviceInfo) throws IOException {
        dataOutStream.writeInt(serviceInfo.nameCPIndex);
        dataOutStream.writeInt(serviceInfo.flags);
        dataOutStream.writeInt(serviceInfo.endpointNameCPIndex);
    }

    private static void writeResourceInfo(DataOutputStream dataOutStream,
                                          ServiceInfo serviceInfo) throws IOException {
        ResourceInfo[] resourceInfoEntries = serviceInfo.resourceInfoMap.values().toArray(new ResourceInfo[0]);
        dataOutStream.writeShort(resourceInfoEntries.length);
        for (ResourceInfo resourceInfo : resourceInfoEntries) {
            writeResourceInfo(dataOutStream, resourceInfo);
        }

        // Write attribute info entries
        writeAttributeInfoEntries(dataOutStream, serviceInfo.getAttributeInfoEntries());
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
    }

    private static void writeAttributeInfo(DataOutputStream dataOutStream,
                                           AttributeInfo attributeInfo) throws IOException {
        AttributeInfo.Kind attributeKind = attributeInfo.getKind();
        dataOutStream.writeInt(attributeInfo.getAttributeNameIndex());

        // This to get the length of the attributes in bytes.
        ByteArrayOutputStream attrDataBAOS = new ByteArrayOutputStream();
        DataOutputStream attrDataOutStream = new DataOutputStream(attrDataBAOS);

        switch (attributeKind) {
            case CODE_ATTRIBUTE:
                CodeAttributeInfo codeAttributeInfo = (CodeAttributeInfo) attributeInfo;
                attrDataOutStream.writeInt(codeAttributeInfo.codeAddrs);

                attrDataOutStream.writeShort(codeAttributeInfo.maxLongLocalVars);
                attrDataOutStream.writeShort(codeAttributeInfo.maxDoubleLocalVars);
                attrDataOutStream.writeShort(codeAttributeInfo.maxStringLocalVars);
                attrDataOutStream.writeShort(codeAttributeInfo.maxIntLocalVars);
                attrDataOutStream.writeShort(codeAttributeInfo.maxByteLocalVars);
                attrDataOutStream.writeShort(codeAttributeInfo.maxRefLocalVars);

                attrDataOutStream.writeShort(codeAttributeInfo.maxLongRegs);
                attrDataOutStream.writeShort(codeAttributeInfo.maxDoubleRegs);
                attrDataOutStream.writeShort(codeAttributeInfo.maxStringRegs);
                attrDataOutStream.writeShort(codeAttributeInfo.maxIntRegs);
                attrDataOutStream.writeShort(codeAttributeInfo.maxByteRegs);
                attrDataOutStream.writeShort(codeAttributeInfo.maxRefRegs);
                break;

            case VARIABLE_TYPE_COUNT_ATTRIBUTE:
                VarTypeCountAttributeInfo varCountAttributeInfo = (VarTypeCountAttributeInfo) attributeInfo;
                attrDataOutStream.writeShort(varCountAttributeInfo.getMaxLongVars());
                attrDataOutStream.writeShort(varCountAttributeInfo.getMaxDoubleVars());
                attrDataOutStream.writeShort(varCountAttributeInfo.getMaxStringVars());
                attrDataOutStream.writeShort(varCountAttributeInfo.getMaxIntVars());
                attrDataOutStream.writeShort(varCountAttributeInfo.getMaxByteVars());
                attrDataOutStream.writeShort(varCountAttributeInfo.getMaxRefVars());
                break;

            case ERROR_TABLE:
                ErrorTableAttributeInfo errTable = (ErrorTableAttributeInfo) attributeInfo;
                ErrorTableEntry[] errorTableEntries =
                        errTable.getErrorTableEntriesList().toArray(new ErrorTableEntry[0]);
                attrDataOutStream.writeShort(errorTableEntries.length);
                for (ErrorTableEntry errorTableEntry : errorTableEntries) {
                    attrDataOutStream.writeInt(errorTableEntry.ipFrom);
                    attrDataOutStream.writeInt(errorTableEntry.ipTo);
                    attrDataOutStream.writeInt(errorTableEntry.ipTarget);
                    attrDataOutStream.writeInt(errorTableEntry.priority);
                    attrDataOutStream.writeInt(errorTableEntry.errorStructCPIndex);
                }
                break;

            case LOCAL_VARIABLES_ATTRIBUTE:
                LocalVariableAttributeInfo localVarAttrInfo = (LocalVariableAttributeInfo) attributeInfo;
                LocalVariableInfo[] localVarInfoArray = localVarAttrInfo.localVars.toArray(
                        new LocalVariableInfo[0]);
                attrDataOutStream.writeShort(localVarInfoArray.length);
                for (LocalVariableInfo localVariableInfo : localVarInfoArray) {
                    writeLocalVariableInfo(attrDataOutStream, localVariableInfo);
                }
                break;
            case LINE_NUMBER_TABLE_ATTRIBUTE:
                LineNumberTableAttributeInfo lnNoTblAttrInfo = (LineNumberTableAttributeInfo) attributeInfo;
                LineNumberInfo[] lineNumberInfoEntries = lnNoTblAttrInfo.getLineNumberInfoEntries();
                attrDataOutStream.writeShort(lineNumberInfoEntries.length);
                for (LineNumberInfo lineNumberInfo : lineNumberInfoEntries) {
                    writeLineNumberInfo(attrDataOutStream, lineNumberInfo);
                }
                break;
            case DEFAULT_VALUE_ATTRIBUTE:
                DefaultValueAttributeInfo defaultValAttrInfo = (DefaultValueAttributeInfo) attributeInfo;
                writeDefaultValue(attrDataOutStream, defaultValAttrInfo.getDefaultValue());
                break;
            case PARAMETER_DEFAULTS_ATTRIBUTE:
                ParamDefaultValueAttributeInfo paramDefaultValAttrInfo = (ParamDefaultValueAttributeInfo) attributeInfo;
                DefaultValue[] defaultValues = paramDefaultValAttrInfo.getDefaultValueInfo();
                attrDataOutStream.writeShort(defaultValues.length);
                for (DefaultValue defaultValue : defaultValues) {
                    writeDefaultValue(attrDataOutStream, defaultValue);
                }
                break;
            case PARAMETERS_ATTRIBUTE:
                ParameterAttributeInfo parameterAttributeInfo = (ParameterAttributeInfo) attributeInfo;
                attrDataOutStream.writeInt(parameterAttributeInfo.requiredParamsCount);
                attrDataOutStream.writeInt(parameterAttributeInfo.defaultableParamsCount);
                attrDataOutStream.writeInt(parameterAttributeInfo.restParamCount);
                break;
            case TAINT_TABLE:
                TaintTableAttributeInfo taintTableAttributeInfo = (TaintTableAttributeInfo) attributeInfo;
                attrDataOutStream.writeShort(taintTableAttributeInfo.rowCount);
                attrDataOutStream.writeShort(taintTableAttributeInfo.columnCount);
                for (Integer paramIndex : taintTableAttributeInfo.taintTable.keySet()) {
                    attrDataOutStream.writeShort(paramIndex);
                    List<Boolean> taintRecord = taintTableAttributeInfo.taintTable.get(paramIndex);
                    for (Boolean taintStatus : taintRecord) {
                        attrDataOutStream.writeBoolean(taintStatus);
                    }
                }
                break;
        }

        byte[] attrDataBytes = attrDataBAOS.toByteArray();
        dataOutStream.writeInt(attrDataBytes.length);
        dataOutStream.write(attrDataBytes);
    }

    private static byte[] writeInstructions(Instruction[] instructions) throws IOException {
        ByteArrayOutputStream byteAOS = new ByteArrayOutputStream();
        DataOutputStream dataOutStream = new DataOutputStream(byteAOS);
        for (Instruction instruction : instructions) {
            dataOutStream.write(instruction.opcode);
            for (Operand operand : instruction.ops) {
                dataOutStream.writeInt(operand.value);
            }
        }
        return byteAOS.toByteArray();
    }

    private static void writeStructFieldInfo(DataOutputStream dataOutStream,
                                             StructFieldInfo structFieldInfo) throws IOException {
        dataOutStream.writeInt(structFieldInfo.nameCPIndex);
        dataOutStream.writeInt(structFieldInfo.signatureCPIndex);
        dataOutStream.writeInt(structFieldInfo.flags);

        // Write attribute info
        writeAttributeInfoEntries(dataOutStream, structFieldInfo.getAttributeInfoEntries());
    }

    private static void writeAttachedFunctionInfo(DataOutputStream dataOutStream,
                                                  AttachedFunctionInfo attachedFuncInfo) throws IOException {
        dataOutStream.writeInt(attachedFuncInfo.nameCPIndex);
        dataOutStream.writeInt(attachedFuncInfo.signatureCPIndex);
        dataOutStream.writeInt(attachedFuncInfo.flags);
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

    private static byte[] toUTF(String value) throws IOException {
        ByteArrayOutputStream byteAOS = new ByteArrayOutputStream();
        DataOutputStream dataOutStream = new DataOutputStream(byteAOS);
        dataOutStream.writeUTF(value);
        return byteAOS.toByteArray();
    }

    private static void writeDefaultValue(DataOutputStream dataOutStream, DefaultValue defaultValueInfo)
            throws IOException {
        dataOutStream.writeInt(defaultValueInfo.typeDescCPIndex);
        String typeDesc = defaultValueInfo.desc;
        switch (typeDesc) {
            case TypeDescriptor.SIG_BOOLEAN:
                dataOutStream.writeBoolean(defaultValueInfo.booleanValue);
                break;
            case TypeDescriptor.SIG_NULL:
                // write nothing
                break;
            default:
                dataOutStream.writeInt(defaultValueInfo.valueCPIndex);
        }
    }
}

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
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.programfile.Instruction.Operand;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.CodeAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.DefaultValueAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.DocumentationAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.DocumentationAttributeInfo.ParameterDocumentInfo;
import org.wso2.ballerinalang.programfile.attributes.ErrorTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LineNumberTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.LocalVariableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.ParamDefaultValueAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.ParameterAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.TaintTableAttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.VarTypeCountAttributeInfo;
import org.wso2.ballerinalang.programfile.cpentries.ActionRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.BlobCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.ByteCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.ConstantPoolEntry;
import org.wso2.ballerinalang.programfile.cpentries.FloatCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.ForkJoinCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.FunctionRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.IntegerCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.PackageRefCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StringCPEntry;
import org.wso2.ballerinalang.programfile.cpentries.StructureRefCPEntry;
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
                case CP_ENTRY_BYTE:
                    byte byteVal = ((ByteCPEntry) cpEntry).getValue();
                    dataOutStream.writeByte(byteVal);
                    break;
                case CP_ENTRY_FLOAT:
                    double doubleVal = ((FloatCPEntry) cpEntry).getValue();
                    dataOutStream.writeDouble(doubleVal);
                    break;
                case CP_ENTRY_STRING:
                    nameCPIndex = ((StringCPEntry) cpEntry).getStringCPIndex();
                    dataOutStream.writeInt(nameCPIndex);
                    break;
                case CP_ENTRY_BLOB:
                    byte[] blobValue = ((BlobCPEntry) cpEntry).getValue();
                    dataOutStream.writeInt(blobValue.length);
                    dataOutStream.write(blobValue);
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
        dataOutStream.writeInt(packageInfo.orgNameCPIndex);
        dataOutStream.writeInt(packageInfo.nameCPIndex);
        dataOutStream.writeInt(packageInfo.versionCPIndex);

        // Write import package entries
        dataOutStream.writeShort(packageInfo.importPkgInfoSet.size());
        for (ImportPackageInfo importPkgInfo : packageInfo.importPkgInfoSet) {
            dataOutStream.writeInt(importPkgInfo.orgNameCPIndex);
            dataOutStream.writeInt(importPkgInfo.nameCPIndex);
            dataOutStream.writeInt(importPkgInfo.versionCPIndex);
        }

        // Write Type Definition entries
        TypeDefInfo[] typeDefEntries = packageInfo.getTypeDefInfoEntries();
        dataOutStream.writeShort(typeDefEntries.length);
        for (TypeDefInfo typeDefInfo : typeDefEntries) {
            writeTypeDefInfo(dataOutStream, typeDefInfo);
        }

        // Write Annotation entries
        AnnotationInfo[] annotationEntries = packageInfo.getAnnotationInfoEntries();
        dataOutStream.writeShort(annotationEntries.length);
        for (AnnotationInfo annotationInfo : annotationEntries) {
            writeAnnotatoinInfo(dataOutStream, annotationInfo);
        }

        // TODO Emit service info entries
        ServiceInfo[] serviceInfoEntries = packageInfo.getServiceInfoEntries();
        dataOutStream.writeShort(serviceInfoEntries.length);
        for (ServiceInfo serviceInfo : serviceInfoEntries) {
            writeServiceInfo(dataOutStream, serviceInfo);
        }

        dataOutStream.writeShort(serviceInfoEntries.length);
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
            dataOutStream.writeInt(packageVarInfo.flags);
            dataOutStream.writeInt(packageVarInfo.globalMemIndex);

            writeAttributeInfoEntries(dataOutStream, packageVarInfo.getAttributeInfoEntries());
        }
    }

    /**
     * Write function info entries to the compiling file.
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

        writeWorkerData(dataOutStream, callableUnitInfo);

        writeAttributeInfoEntries(dataOutStream, callableUnitInfo.getAttributeInfoEntries());
    }

    private static void writeWorkerData(DataOutputStream dataOutStream, CallableUnitInfo callableUnitInfo)
            throws IOException {
        ByteArrayOutputStream workerDataBAOS = new ByteArrayOutputStream();
        DataOutputStream workerDataDOS = new DataOutputStream(workerDataBAOS);

        WorkerInfo defaultWorker = callableUnitInfo.defaultWorkerInfo;
        if (defaultWorker == null) {
            // No default worker implies an abstract function. Then there's no worker data.
            dataOutStream.writeInt(0);
        } else {
            WorkerDataChannelInfo[] workerDataChannelInfoEntries = callableUnitInfo.getWorkerDataChannelInfo();
            workerDataDOS.writeShort(workerDataChannelInfoEntries.length);
            for (WorkerDataChannelInfo dataChannelInfo : workerDataChannelInfoEntries) {
                writeWorkerDataChannelInfo(workerDataDOS, dataChannelInfo);
            }

            WorkerInfo[] workerInfoEntries = callableUnitInfo.getWorkerInfoEntries();
            workerDataDOS.writeShort(workerInfoEntries.length + 1);
            writeWorkerInfo(workerDataDOS, defaultWorker);
            for (WorkerInfo workerInfo : workerInfoEntries) {
                writeWorkerInfo(workerDataDOS, workerInfo);
            }

            byte[] workerData = workerDataBAOS.toByteArray();
            dataOutStream.writeInt(workerData.length);
            dataOutStream.write(workerData);
        }
    }

    private static void writeWorkerDataChannelInfo(DataOutputStream dataOutStream,
                                                   WorkerDataChannelInfo dataChannelInfo) throws IOException {
        dataOutStream.writeInt(dataChannelInfo.getSourceCPIndex());
        dataOutStream.writeInt(dataChannelInfo.getTargetCPIndex());
        dataOutStream.writeInt(dataChannelInfo.getDataChannelRefIndex());
    }

    private static void writeTypeDefInfo(DataOutputStream dataOutStream,
                                         TypeDefInfo typeDefInfo) throws IOException {
        dataOutStream.writeInt(typeDefInfo.nameCPIndex);
        dataOutStream.writeInt(typeDefInfo.flags);
        dataOutStream.writeBoolean(typeDefInfo.isLabel);
        dataOutStream.writeInt(typeDefInfo.typeTag);

        if (typeDefInfo.isLabel) {
            writeLabelTypeDefInfo(dataOutStream, (LabelTypeInfo) typeDefInfo.typeInfo);
            // Write attribute info
            writeAttributeInfoEntries(dataOutStream, typeDefInfo.getAttributeInfoEntries());
            return;
        }
        switch (typeDefInfo.typeTag) {
            case TypeTags.OBJECT:
                writeObjectTypeDefInfo(dataOutStream, (ObjectTypeInfo) typeDefInfo.typeInfo);
                break;
            case TypeTags.RECORD:
                writeRecordTypeDefInfo(dataOutStream, (RecordTypeInfo) typeDefInfo.typeInfo);
                break;
            case TypeTags.FINITE:
                writeFiniteTypeDefInfo(dataOutStream, (FiniteTypeInfo) typeDefInfo.typeInfo);
                break;
            default:
                writeLabelTypeDefInfo(dataOutStream, (LabelTypeInfo) typeDefInfo.typeInfo);
                break;
        }

        // Write attribute info
        writeAttributeInfoEntries(dataOutStream, typeDefInfo.getAttributeInfoEntries());
    }

    private static void writeAnnotatoinInfo(DataOutputStream dataOutStream,
                                            AnnotationInfo typeDefInfo) throws IOException {
        dataOutStream.writeInt(typeDefInfo.nameCPIndex);
        dataOutStream.writeInt(typeDefInfo.flags);
        dataOutStream.writeInt(typeDefInfo.attachPoints);
        dataOutStream.writeInt(typeDefInfo.signatureCPIndex);
    }

    private static void writeObjectTypeDefInfo(DataOutputStream dataOutStream,
                                               ObjectTypeInfo objectInfo) throws IOException {
        // Write struct field info entries
        dataOutStream.writeShort(objectInfo.fieldInfoEntries.size());
        for (StructFieldInfo structFieldInfoEntry : objectInfo.fieldInfoEntries) {
            writeStructFieldInfo(dataOutStream, structFieldInfoEntry);
        }

        // Write attribute info
        writeAttributeInfoEntries(dataOutStream, objectInfo.getAttributeInfoEntries());
    }

    private static void writeRecordTypeDefInfo(DataOutputStream dataOutStream,
                                               RecordTypeInfo recordInfo) throws IOException {
        if (recordInfo.recordType.sealed) {
            dataOutStream.writeBoolean(true);
        } else {
            dataOutStream.writeBoolean(false);
            dataOutStream.writeInt(recordInfo.restFieldTypeSigCPIndex);
        }
        // Write struct field info entries
        dataOutStream.writeShort(recordInfo.fieldInfoEntries.size());
        for (StructFieldInfo structFieldInfoEntry : recordInfo.fieldInfoEntries) {
            writeStructFieldInfo(dataOutStream, structFieldInfoEntry);
        }

        // Write attribute info
        writeAttributeInfoEntries(dataOutStream, recordInfo.getAttributeInfoEntries());
    }

    private static void writeFiniteTypeDefInfo(DataOutputStream dataOutStream,
                                               FiniteTypeInfo finiteTypeDefInfo) throws IOException {
        ValueSpaceItemInfo[] valueSpaceItemInfos = finiteTypeDefInfo.
                valueSpaceItemInfos.toArray(new ValueSpaceItemInfo[0]);

        dataOutStream.writeShort(valueSpaceItemInfos.length);
        for (ValueSpaceItemInfo valueSpaceItem : valueSpaceItemInfos) {
            writeDefaultValue(dataOutStream, valueSpaceItem.value);
        }
    }

    private static void writeLabelTypeDefInfo(DataOutputStream dataOutStream,
                                              LabelTypeInfo labelTypeInfo) throws IOException {
        dataOutStream.writeInt(labelTypeInfo.typeSigCPIndex);
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

        writeWorkerData(dataOutStream, resourceInfo);

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
                attrDataOutStream.writeShort(codeAttributeInfo.maxRefLocalVars);

                attrDataOutStream.writeShort(codeAttributeInfo.maxLongRegs);
                attrDataOutStream.writeShort(codeAttributeInfo.maxDoubleRegs);
                attrDataOutStream.writeShort(codeAttributeInfo.maxStringRegs);
                attrDataOutStream.writeShort(codeAttributeInfo.maxIntRegs);
                attrDataOutStream.writeShort(codeAttributeInfo.maxRefRegs);
                break;

            case VARIABLE_TYPE_COUNT_ATTRIBUTE:
                VarTypeCountAttributeInfo varCountAttributeInfo = (VarTypeCountAttributeInfo) attributeInfo;
                attrDataOutStream.writeShort(varCountAttributeInfo.getMaxLongVars());
                attrDataOutStream.writeShort(varCountAttributeInfo.getMaxDoubleVars());
                attrDataOutStream.writeShort(varCountAttributeInfo.getMaxStringVars());
                attrDataOutStream.writeShort(varCountAttributeInfo.getMaxIntVars());
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
                attrDataOutStream.writeInt(lineNumberInfoEntries.length);
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
            case DOCUMENT_ATTACHMENT_ATTRIBUTE:
                DocumentationAttributeInfo docAttrInfo = (DocumentationAttributeInfo) attributeInfo;
                attrDataOutStream.writeInt(docAttrInfo.descriptionCPIndex);
                attrDataOutStream.writeShort(docAttrInfo.paramDocInfoList.size());
                for (ParameterDocumentInfo paramDocInfo : docAttrInfo.paramDocInfoList) {
                    attrDataOutStream.writeInt(paramDocInfo.nameCPIndex);
                    attrDataOutStream.writeInt(paramDocInfo.descriptionCPIndex);
                }
                if (docAttrInfo.returnParameterDescriptionCPIndex != -1) {
                    attrDataOutStream.writeBoolean(true);
                    attrDataOutStream.writeInt(docAttrInfo.returnParameterDescriptionCPIndex);
                } else {
                    attrDataOutStream.writeBoolean(false);
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
        dataOutStream.writeInt(structFieldInfo.varIndex);

        // Write attribute info
        writeAttributeInfoEntries(dataOutStream, structFieldInfo.getAttributeInfoEntries());
    }

    private static void writeLocalVariableInfo(DataOutputStream dataOutStream,
                                               LocalVariableInfo localVariableInfo) throws IOException {
        dataOutStream.writeInt(localVariableInfo.varNameCPIndex);
        dataOutStream.writeInt(localVariableInfo.varIndex);
        dataOutStream.writeInt(localVariableInfo.varTypeSigCPIndex);

        dataOutStream.writeInt(localVariableInfo.scopeStartLineNumber);
        dataOutStream.writeInt(localVariableInfo.scopeEndLineNumber);

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

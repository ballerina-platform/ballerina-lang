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
package org.ballerinalang.util.codegen;

import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BAttachedFunction;
import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.model.types.BField;
import org.ballerinalang.model.types.BFiniteType;
import org.ballerinalang.model.types.BFunctionType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BObjectType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BServiceType;
import org.ballerinalang.model.types.BStreamType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTableType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeSignature;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.Flags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.NativeUnitLoader;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.CallableUnitInfo.ChannelDetails;
import org.ballerinalang.util.codegen.Instruction.InstructionCALL;
import org.ballerinalang.util.codegen.Instruction.InstructionIteratorNext;
import org.ballerinalang.util.codegen.Instruction.InstructionLock;
import org.ballerinalang.util.codegen.Instruction.InstructionUnLock;
import org.ballerinalang.util.codegen.Instruction.InstructionVCALL;
import org.ballerinalang.util.codegen.Instruction.InstructionWRKSendReceive;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.codegen.attributes.DefaultValueAttributeInfo;
import org.ballerinalang.util.codegen.attributes.DocumentationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.DocumentationAttributeInfo.ParameterDocumentInfo;
import org.ballerinalang.util.codegen.attributes.ErrorTableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LineNumberTableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ParamDefaultValueAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ParameterAttributeInfo;
import org.ballerinalang.util.codegen.attributes.TaintTableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.VarTypeCountAttributeInfo;
import org.ballerinalang.util.codegen.attributes.WorkerSendInsAttributeInfo;
import org.ballerinalang.util.codegen.cpentries.ActionRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.BlobCPEntry;
import org.ballerinalang.util.codegen.cpentries.ByteCPEntry;
import org.ballerinalang.util.codegen.cpentries.ConstantPool;
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
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.ProgramFileFormatException;
import org.wso2.ballerinalang.compiler.TypeCreater;
import org.wso2.ballerinalang.compiler.TypeSignatureReader;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Stream;

import static org.ballerinalang.util.BLangConstants.CONSTRUCTOR_FUNCTION_SUFFIX;
import static org.ballerinalang.util.BLangConstants.INIT_FUNCTION_SUFFIX;
import static org.ballerinalang.util.BLangConstants.START_FUNCTION_SUFFIX;
import static org.ballerinalang.util.BLangConstants.STOP_FUNCTION_SUFFIX;
import static org.ballerinalang.util.BLangConstants.TEST_INIT_FUNCTION_SUFFIX;
import static org.ballerinalang.util.BLangConstants.TEST_START_FUNCTION_SUFFIX;
import static org.ballerinalang.util.BLangConstants.TEST_STOP_FUNCTION_SUFFIX;

/**
 * Reads a Ballerina {@code PackageInfo} structure from a file.
 *
 * @since 0.975.0
 */
public class PackageInfoReader {

    private ProgramFile programFile;
    private DataInputStream dataInStream;
    private List<ConstantPoolEntry> unresolvedCPEntries = new ArrayList<>();
    private TypeSignatureReader<BType> typeSigReader;

    public PackageInfoReader(DataInputStream dataInStream, ProgramFile programFile) {
        this.dataInStream = dataInStream;
        this.programFile = programFile;
        this.typeSigReader = new TypeSignatureReader<>();
    }

    public void readConstantPool(ConstantPool constantPool) throws IOException {
        int constantPoolSize = dataInStream.readInt();
        for (int i = 0; i < constantPoolSize; i++) {
            byte cpTag = dataInStream.readByte();
            ConstantPoolEntry.EntryType cpEntryType = ConstantPoolEntry.EntryType.values()[cpTag - 1];
            ConstantPoolEntry cpEntry = readCPEntry(constantPool, cpEntryType);
            constantPool.addCPEntry(cpEntry);
        }
    }

    private ConstantPoolEntry readCPEntry(ConstantPool constantPool,
                                          ConstantPoolEntry.EntryType cpEntryType) throws IOException {
        int cpIndex;
        int pkgCPIndex;
        UTF8CPEntry utf8CPEntry;
        PackageRefCPEntry packageRefCPEntry;
        Optional<PackageInfo> packageInfoOptional;
        switch (cpEntryType) {
            case CP_ENTRY_UTF8:
                short length = dataInStream.readShort();
                String strValue = null;

                // If the length of the bytes is -1, that means no UTF value has been written.
                // i.e: string value represented by the UTF should be null.
                // Therefore we read the UTF value only if the length >= 0.
                if (length >= 0) {
                    strValue = dataInStream.readUTF();
                }
                return new UTF8CPEntry(strValue);

            case CP_ENTRY_BLOB:
                int blobLength = dataInStream.readInt();
                byte[] blobValue = null;
                if (blobLength >= 0) {
                    blobValue = new byte[blobLength];
                    dataInStream.readFully(blobValue);
                }
                return new BlobCPEntry(blobValue);

            case CP_ENTRY_INTEGER:
                long longVal = dataInStream.readLong();
                return new IntegerCPEntry(longVal);

            case CP_ENTRY_BYTE:
                byte byteVal = dataInStream.readByte();
                return new ByteCPEntry(byteVal);

            case CP_ENTRY_FLOAT:
                double doubleVal = dataInStream.readDouble();
                return new FloatCPEntry(doubleVal);

            case CP_ENTRY_STRING:
                cpIndex = dataInStream.readInt();
                utf8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(cpIndex);
                return new StringCPEntry(cpIndex, utf8CPEntry.getValue());

            case CP_ENTRY_PACKAGE:
                cpIndex = dataInStream.readInt();
                utf8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(cpIndex);
                int versionCPIndex = dataInStream.readInt();
                UTF8CPEntry utf8VersionCPEntry = (UTF8CPEntry) constantPool.getCPEntry(versionCPIndex);
                packageRefCPEntry = new PackageRefCPEntry(cpIndex, utf8CPEntry.getValue(), versionCPIndex,
                        utf8VersionCPEntry.getValue());
                packageInfoOptional = Optional.ofNullable(
                        programFile.getPackageInfo(packageRefCPEntry.getPackageName()));
                if (packageInfoOptional.isPresent()) {
                    packageRefCPEntry.setPackageInfo(packageInfoOptional.get());
                } else {
                    unresolvedCPEntries.add(packageRefCPEntry);
                }
                return packageRefCPEntry;

            case CP_ENTRY_FUNCTION_REF:
                pkgCPIndex = dataInStream.readInt();
                packageRefCPEntry = (PackageRefCPEntry) constantPool.getCPEntry(pkgCPIndex);

                cpIndex = dataInStream.readInt();
                utf8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(cpIndex);
                String funcName = utf8CPEntry.getValue();
                FunctionRefCPEntry functionRefCPEntry = new FunctionRefCPEntry(pkgCPIndex,
                        packageRefCPEntry.getPackageName(), cpIndex, funcName);

                // Find the functionInfo
                packageInfoOptional = Optional.ofNullable(
                        programFile.getPackageInfo(packageRefCPEntry.getPackageName()));
                Optional<FunctionInfo> funcInfoOptional = packageInfoOptional.map(
                        packageInfo -> packageInfo.getFunctionInfo(funcName));
                if (!funcInfoOptional.isPresent()) {
                    // This must reference to the current package and the current package is not been read yet.
                    // Therefore we add this to the unresolved CP Entry list.
                    unresolvedCPEntries.add(functionRefCPEntry);
                    return functionRefCPEntry;
                }

                functionRefCPEntry.setFunctionInfo(funcInfoOptional.get());
                return functionRefCPEntry;

            case CP_ENTRY_ACTION_REF:
                pkgCPIndex = dataInStream.readInt();
                packageRefCPEntry = (PackageRefCPEntry) constantPool.getCPEntry(pkgCPIndex);

                cpIndex = dataInStream.readInt();
                UTF8CPEntry nameCPEntry = (UTF8CPEntry) constantPool.getCPEntry(cpIndex);
                String actionName = nameCPEntry.getValue();
                return new ActionRefCPEntry(pkgCPIndex, packageRefCPEntry.getPackageName(),
                        cpIndex, actionName);

            case CP_ENTRY_STRUCTURE_REF:
                pkgCPIndex = dataInStream.readInt();
                packageRefCPEntry = (PackageRefCPEntry) constantPool.getCPEntry(pkgCPIndex);
                cpIndex = dataInStream.readInt();
                utf8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(cpIndex);
                StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex,
                        packageRefCPEntry.getPackageName(),
                        cpIndex, utf8CPEntry.getValue());

                packageInfoOptional = Optional.ofNullable(
                        programFile.getPackageInfo(packageRefCPEntry.getPackageName()));
                Optional<CustomTypeInfo> structInfoOptional = packageInfoOptional.map(
                        packageInfo -> packageInfo.getStructureTypeInfo(utf8CPEntry.getValue()));
                if (!structInfoOptional.isPresent()) {
                    // This must reference to the current package and the current package is not been read yet.
                    // Therefore we add this to the unresolved CP Entry list.
                    unresolvedCPEntries.add(structureRefCPEntry);
                    return structureRefCPEntry;
                }

                structureRefCPEntry.setStructureTypeInfo(structInfoOptional.get());
                return structureRefCPEntry;
            case CP_ENTRY_TYPE_REF:
                int typeSigCPIndex = dataInStream.readInt();
                utf8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(typeSigCPIndex);
                TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex, utf8CPEntry.getValue());
                unresolvedCPEntries.add(typeRefCPEntry);
                return typeRefCPEntry;
            case CP_ENTRY_FORK_JOIN:
                int forkJoinCPIndex = dataInStream.readInt();
                return new ForkJoinCPEntry(forkJoinCPIndex);
            case CP_ENTRY_WRKR_DATA_CHNL_REF:
                int uniqueNameCPIndex = dataInStream.readInt();
                UTF8CPEntry wrkrDtChnlTypesSigCPEntry = (UTF8CPEntry) constantPool
                        .getCPEntry(uniqueNameCPIndex);

                return new WorkerDataChannelRefCPEntry(uniqueNameCPIndex, wrkrDtChnlTypesSigCPEntry.getValue());
            default:
                throw new ProgramFileFormatException("invalid constant pool entry " + cpEntryType.getValue());
        }
    }

    public void readEntryPoint() throws IOException {
        int pkdCPIndex = dataInStream.readInt();
        PackageRefCPEntry packageRefCPEntry = (PackageRefCPEntry) programFile.getCPEntry(pkdCPIndex);
        programFile.setEntryPkgCPIndex(pkdCPIndex);
        programFile.setEntryPkgName(packageRefCPEntry.getPackageName());

        int flags = dataInStream.readByte();
        if ((flags & ProgramFile.EP_MAIN_FLAG) == ProgramFile.EP_MAIN_FLAG) {
            programFile.setMainEPAvailable(true);
        }

        if ((flags & ProgramFile.EP_SERVICE_FLAG) == ProgramFile.EP_SERVICE_FLAG) {
            programFile.setServiceEPAvailable(true);
        }
    }

    public void readPackageInfo() throws IOException {
        PackageInfo packageInfo = new PackageInfo();

        // Read constant pool in the package.
        readConstantPool(packageInfo);

        // Read org name
        int orgNameCPIndex = dataInStream.readInt();
        UTF8CPEntry orgNameCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(orgNameCPIndex);
        packageInfo.orgNameCPIndex = orgNameCPIndex;

        // Read package name
        int pkgNameCPIndex = dataInStream.readInt();
        UTF8CPEntry pkgNameCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(pkgNameCPIndex);
        packageInfo.nameCPIndex = pkgNameCPIndex;


        // Read package version
        int pkgVersionCPIndex = dataInStream.readInt();
        UTF8CPEntry pkgVersionCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(pkgVersionCPIndex);
        packageInfo.versionCPIndex = pkgVersionCPIndex;
        packageInfo.pkgVersion = pkgVersionCPEntry.getValue();

        packageInfo.pkgPath =
                getPackagePath(orgNameCPEntry.getValue(), pkgNameCPEntry.getValue(), pkgVersionCPEntry.getValue());

        packageInfo.setProgramFile(programFile);

        // Read import package entries
        readImportPackageInfoEntries(packageInfo);

        packageInfo.pkgIndex = programFile.currentPkgIndex++;

        programFile.addPackageInfo(packageInfo.pkgPath, packageInfo);

        // Read type def info entries
        readTypeDefInfoEntries(packageInfo);

        // Read annotation info entries
        readAnnotationInfoEntries(packageInfo);

        // Read service info entries
        readServiceInfoEntries(packageInfo);

        // Resolve user-defined type i.e. structs and connectors
        resolveUserDefinedTypes(packageInfo);

        // Read resource info entries.
        readResourceInfoEntries(packageInfo);

        // Read constant info entries
        readConstantInfoEntries(packageInfo);

        // Read global var info entries
        readGlobalVarInfoEntries(packageInfo);

        // Read function info entries in the package
        readFunctionInfoEntries(packageInfo);

        // Attach the function to the respective types
        setAttachedFunctions(packageInfo);

        // TODO Read annotation info entries

        // Resolve unresolved CP entries.
        resolveCPEntries(packageInfo);

        // Read attribute info entries
        readAttributeInfoEntries(packageInfo, packageInfo, packageInfo);

        // Read instructions
        readInstructions(packageInfo);

        packageInfo.complete();
    }

    private void readImportPackageInfoEntries(PackageInfo packageInfo) throws IOException {
        int impPkgCount = dataInStream.readShort();
        for (int i = 0; i < impPkgCount; i++) {
            int orgNameCPIndex = dataInStream.readInt();
            UTF8CPEntry orgNameCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(orgNameCPIndex);

            int pkgNameCPIndex = dataInStream.readInt();
            UTF8CPEntry pkgNameCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(pkgNameCPIndex);

            int pkgVersionCPIndex = dataInStream.readInt();
            UTF8CPEntry pkgVersionCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(pkgVersionCPIndex);

            this.getPackageInfo(getPackagePath(orgNameCPEntry.getValue(),
                    pkgNameCPEntry.getValue(), pkgVersionCPEntry.getValue()));
        }
    }

    private void readTypeDefInfoEntries(PackageInfo packageInfo) throws IOException {
        int typeDefCount = dataInStream.readShort();
        for (int i = 0; i < typeDefCount; i++) {

            int typeDefNameCPIndex = dataInStream.readInt();
            int flags = dataInStream.readInt();
            boolean isLabel = dataInStream.readBoolean();
            int typeTag = dataInStream.readInt();
            UTF8CPEntry typeDefNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(typeDefNameCPIndex);
            String typeDefName = typeDefNameUTF8Entry.getValue();
            TypeDefInfo typeDefInfo = new TypeDefInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                    typeDefNameCPIndex, typeDefName, flags);

            typeDefInfo.typeTag = typeTag;

            if (isLabel) {
                //Read index and ignore as this type is not needed for runtime
                dataInStream.readInt();
                // Read attributes of the struct info
                readAttributeInfoEntries(packageInfo, packageInfo, typeDefInfo);
                continue;
            }

            switch (typeDefInfo.typeTag) {
                case TypeTags.OBJECT_TYPE_TAG:
                    readObjectInfoEntry(packageInfo, typeDefInfo);
                    packageInfo.addTypeDefInfo(typeDefName, typeDefInfo);
                    break;
                case TypeTags.RECORD_TYPE_TAG:
                    readRecordInfoEntry(packageInfo, typeDefInfo);
                    packageInfo.addTypeDefInfo(typeDefName, typeDefInfo);
                    break;
                case TypeTags.FINITE_TYPE_TAG:
                    readFiniteTypeInfoEntry(packageInfo, typeDefInfo);
                    packageInfo.addTypeDefInfo(typeDefName, typeDefInfo);
                    break;
                default:
                    //Read index and ignore as this type is not needed for runtime
                    dataInStream.readInt();
                    break;
            }
            // Read attributes of the struct info
            readAttributeInfoEntries(packageInfo, packageInfo, typeDefInfo);
        }

    }

    private void readAnnotationInfoEntries(PackageInfo packageInfo) throws IOException {
        int typeDefCount = dataInStream.readShort();
        for (int i = 0; i < typeDefCount; i++) {
            dataInStream.readInt();
            dataInStream.readInt();
            dataInStream.readInt();
            dataInStream.readInt();
        }
    }

    private void readObjectInfoEntry(PackageInfo packageInfo, TypeDefInfo typeDefInfo) throws IOException {
        ObjectTypeInfo objectInfo = new ObjectTypeInfo();

        // Set struct type
        BObjectType objectType;
        if (Flags.isFlagOn(typeDefInfo.flags, Flags.SERVICE)) {
            objectType = new BServiceType(objectInfo, typeDefInfo.name, packageInfo.getPkgPath(), typeDefInfo.flags);
        } else {
            objectType = new BObjectType(objectInfo, typeDefInfo.name, packageInfo.getPkgPath(), typeDefInfo.flags);
        }
        objectInfo.setType(objectType);

        // Read struct field info entries
        int fieldCount = dataInStream.readShort();
        for (int j = 0; j < fieldCount; j++) {
            // Read field name
            int fieldNameCPIndex = dataInStream.readInt();
            UTF8CPEntry fieldNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(fieldNameCPIndex);

            // Read field type signature
            int fieldTypeSigCPIndex = dataInStream.readInt();
            UTF8CPEntry fieldTypeSigUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(fieldTypeSigCPIndex);

            int fieldFlags = dataInStream.readInt();

            //read and ignore varindex
            dataInStream.readInt();

            StructFieldInfo fieldInfo = new StructFieldInfo(fieldNameCPIndex, fieldNameUTF8Entry.getValue(),
                    fieldTypeSigCPIndex, fieldTypeSigUTF8Entry.getValue(), fieldFlags);
            objectInfo.addFieldInfo(fieldInfo);

            readAttributeInfoEntries(packageInfo, packageInfo, fieldInfo);
        }

        // Read attributes of the struct info
        readAttributeInfoEntries(packageInfo, packageInfo, objectInfo);
        typeDefInfo.typeInfo = objectInfo;
    }

    private void readRecordInfoEntry(PackageInfo packageInfo, TypeDefInfo typeDefInfo) throws IOException {
        RecordTypeInfo recordInfo = new RecordTypeInfo();

        // Set struct type
        BRecordType recordType = new BRecordType(recordInfo, typeDefInfo.name,
                packageInfo.getPkgPath(), typeDefInfo.flags);
        recordInfo.setType(recordType);

        recordType.sealed = dataInStream.readBoolean();
        if (!recordType.sealed) {
            int restFieldCPIndex = dataInStream.readInt();
            UTF8CPEntry restFieldTypeSigUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(restFieldCPIndex);
            recordInfo.setRestFieldSignatureCPIndex(restFieldCPIndex);
            recordInfo.setRestFieldTypeSignature(restFieldTypeSigUTF8Entry.getValue());
        }

        // Read struct field info entries
        int fieldCount = dataInStream.readShort();
        for (int j = 0; j < fieldCount; j++) {
            // Read field name
            int fieldNameCPIndex = dataInStream.readInt();
            UTF8CPEntry fieldNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(fieldNameCPIndex);

            // Read field type signature
            int fieldTypeSigCPIndex = dataInStream.readInt();
            UTF8CPEntry fieldTypeSigUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(fieldTypeSigCPIndex);

            int fieldFlags = dataInStream.readInt();

            //read and ignore varindex
            dataInStream.readInt();

            StructFieldInfo fieldInfo = new StructFieldInfo(fieldNameCPIndex, fieldNameUTF8Entry.getValue(),
                    fieldTypeSigCPIndex, fieldTypeSigUTF8Entry.getValue(), fieldFlags);
            recordInfo.addFieldInfo(fieldInfo);

            readAttributeInfoEntries(packageInfo, packageInfo, fieldInfo);
        }

        // Read attributes of the struct info
        readAttributeInfoEntries(packageInfo, packageInfo, recordInfo);
        typeDefInfo.typeInfo = recordInfo;
    }

    private void readFiniteTypeInfoEntry(PackageInfo packageInfo, TypeDefInfo typeDefInfo) throws IOException {
        FiniteTypeInfo typeInfo = new FiniteTypeInfo();

        BFiniteType finiteType = new BFiniteType(typeDefInfo.name, packageInfo.getPkgPath());
        typeInfo.setType(finiteType);

        int valueSpaceCount = dataInStream.readShort();
        for (int k = 0; k < valueSpaceCount; k++) {
            finiteType.valueSpace.add(getDefaultValueToBValue(getDefaultValue(packageInfo)));
        }

        typeDefInfo.typeInfo = typeInfo;
    }

    private void readServiceInfoEntries(PackageInfo packageInfo) throws IOException {
        int serviceCount = dataInStream.readShort();
        for (int i = 0; i < serviceCount; i++) {
            // Read connector name cp index
            int serviceNameCPIndex = dataInStream.readInt();
            UTF8CPEntry serviceNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(serviceNameCPIndex);
            int flags = dataInStream.readInt();

            // Read service and listener type cp index;
            TypeRefCPEntry serviceType = (TypeRefCPEntry) packageInfo.getCPEntry(dataInStream.readInt());
            ServiceInfo serviceInfo = new ServiceInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                    serviceNameCPIndex, serviceNameUTF8Entry.getValue(), flags, serviceType);
            serviceInfo.setPackageInfo(packageInfo);
            packageInfo.addServiceInfo(serviceInfo.getName(), serviceInfo);
        }
    }

    private void readResourceInfoEntries(PackageInfo packageInfo) throws IOException {
        dataInStream.readShort();   // Ignore service count.
        for (ServiceInfo serviceInfo : packageInfo.getServiceInfoEntries()) {
            int actionCount = dataInStream.readShort();
            for (int j = 0; j < actionCount; j++) {
                // Read action name;
                int resNameCPIndex = dataInStream.readInt();
                UTF8CPEntry resNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(resNameCPIndex);
                String resName = resNameUTF8Entry.getValue();
                serviceInfo.addResourceInfo(resName);
            }
        }
    }

    private void readConstantInfoEntries(PackageInfo packageInfo) throws IOException {
        int constCount = dataInStream.readShort();
        for (int i = 0; i < constCount; i++) {
            // Read constant info and ignore.
            readConstantInfo(packageInfo, packageInfo);
        }
    }

    private void readGlobalVarInfoEntries(PackageInfo packageInfo) throws IOException {
        int globalVarCount = dataInStream.readShort();
        for (int i = 0; i < globalVarCount; i++) {
            PackageVarInfo packageVarInfo = getGlobalVarInfo(packageInfo, packageInfo);
            packageInfo.addPackageVarInfo(packageVarInfo.getName(), packageVarInfo);
        }
    }

    private void readConstantInfo(PackageInfo packageInfo, ConstantPool constantPool) throws IOException {
        // Read constant name cp index and ignore.
        dataInStream.readInt();

        // Read finite type cp index and ignore.
        dataInStream.readInt();

        // Read value type cp index and ignore.
        dataInStream.readInt();

        // Read and ignore flags.
        dataInStream.readInt();

        // Read and ignore attributes.
        int attributesCount = dataInStream.readShort();
        for (int k = 0; k < attributesCount; k++) {
            getAttributeInfo(packageInfo, constantPool);
        }
    }

    private PackageVarInfo getGlobalVarInfo(PackageInfo packageInfo, ConstantPool constantPool) throws IOException {
        // Read variable name;
        int nameCPIndex = dataInStream.readInt();
        UTF8CPEntry nameUTF8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(nameCPIndex);

        // Read variable type;
        int sigCPIndex = dataInStream.readInt();
        UTF8CPEntry sigUTF8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(sigCPIndex);

        // Read and ignore flags
        dataInStream.readInt();

        // Read memory index
        int globalMemIndex = dataInStream.readInt();

        // Read identifier kind flag
        boolean isIdentifierLiteral = dataInStream.readBoolean();

        BType type = getBTypeFromDescriptor(packageInfo, sigUTF8CPEntry.getValue());
        PackageVarInfo packageVarInfo = new PackageVarInfo(nameCPIndex, nameUTF8CPEntry.getValue(),
                sigCPIndex, globalMemIndex, type, isIdentifierLiteral);

        // Read attributes
        readAttributeInfoEntries(packageInfo, constantPool, packageVarInfo);
        return packageVarInfo;
    }

    private void readFunctionInfoEntries(PackageInfo packageInfo) throws IOException {
        int funcCount = dataInStream.readShort();
        for (int i = 0; i < funcCount; i++) {
            readFunctionInfo(packageInfo);
        }

        packageInfo.setInitFunctionInfo(packageInfo.getFunctionInfo(packageInfo.getPkgPath() + INIT_FUNCTION_SUFFIX));
        packageInfo.setStartFunctionInfo(packageInfo.getFunctionInfo(packageInfo.getPkgPath() + START_FUNCTION_SUFFIX));
        packageInfo.setStopFunctionInfo(packageInfo.getFunctionInfo(packageInfo.getPkgPath() + STOP_FUNCTION_SUFFIX));

        // Set for the testable package
        packageInfo.setTestInitFunctionInfo(packageInfo.getFunctionInfo(packageInfo.getPkgPath() +
                TEST_INIT_FUNCTION_SUFFIX));
        packageInfo.setTestStartFunctionInfo(packageInfo.getFunctionInfo(packageInfo.getPkgPath() +
                TEST_START_FUNCTION_SUFFIX));
        packageInfo.setTestStopFunctionInfo(packageInfo.getFunctionInfo(packageInfo.getPkgPath() +
                TEST_STOP_FUNCTION_SUFFIX));
    }

    private void readFunctionInfo(PackageInfo packageInfo) throws IOException {
        int funcNameCPIndex = dataInStream.readInt();
        UTF8CPEntry funcNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(funcNameCPIndex);
        String funcName = funcNameUTF8Entry.getValue();
        FunctionInfo functionInfo = new FunctionInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                funcNameCPIndex, funcName);
        functionInfo.setPackageInfo(packageInfo);

        int funcSigCPIndex = dataInStream.readInt();
        UTF8CPEntry funcSigUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(funcSigCPIndex);
        setCallableUnitSignature(packageInfo, functionInfo, funcSigUTF8Entry.getValue());

        functionInfo.flags = dataInStream.readInt();
        boolean nativeFunc = Flags.isFlagOn(functionInfo.flags, Flags.NATIVE);
        functionInfo.setNative(nativeFunc);
        functionInfo.setPublic(Flags.isFlagOn(functionInfo.flags, Flags.PUBLIC));

        String uniqueFuncName;
        boolean attached = Flags.isFlagOn(functionInfo.flags, Flags.ATTACHED);
        if (attached) {
            functionInfo.attachedToTypeCPIndex = dataInStream.readInt();
            TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) packageInfo.getCPEntry(functionInfo.attachedToTypeCPIndex);
            functionInfo.attachedToType = typeRefCPEntry.getType();
            uniqueFuncName = Symbols.getAttachedFuncSymbolName(typeRefCPEntry.getType().getName(), funcName);
            updateAttachFunctionInfo(packageInfo, typeRefCPEntry.getType(), funcName, functionInfo);
        } else {
            uniqueFuncName = funcName;
        }

        packageInfo.addFunctionInfo(uniqueFuncName, functionInfo);

        readWorkerData(packageInfo, functionInfo);

        if (nativeFunc) {
            NativeCallableUnit nativeFunction = NativeUnitLoader.getInstance().loadNativeFunction(
                    functionInfo.getPkgPath(), uniqueFuncName);
            if (nativeFunction == null) {
                throw new BLangRuntimeException("native function not available " +
                        functionInfo.getPkgPath() + ":" + uniqueFuncName);
            }
            functionInfo.setNativeCallableUnit(nativeFunction);
        }

        // Read attributes
        readAttributeInfoEntries(packageInfo, packageInfo, functionInfo);

        // Set worker send in channels
        WorkerSendInsAttributeInfo attributeInfo =
                (WorkerSendInsAttributeInfo) functionInfo.getAttributeInfo(AttributeInfo.Kind.WORKER_SEND_INS);
        functionInfo.workerSendInChannels = new ChannelDetails[attributeInfo.sendIns.length];
        if (functionInfo.workerSendInChannels.length == 0) {
            return;
        }
        String currentWorkerName = functionInfo.defaultWorkerInfo.getWorkerName();
        for (int i = 0; i < attributeInfo.sendIns.length; i++) {
            String chnlName = attributeInfo.sendIns[i];

            functionInfo.workerSendInChannels[i] = new ChannelDetails(chnlName, currentWorkerName
                    .equals(BLangConstants.DEFAULT_WORKER_NAME), isChannelSend(chnlName, currentWorkerName));
        }
    }

    //TODO remove below and pass these details from compiler
    private boolean isChannelSend(String chnlName, String workerName) {
        return chnlName.startsWith(workerName) && chnlName.split(workerName)[1].startsWith("->");
    }

    private void readWorkerData(PackageInfo packageInfo, CallableUnitInfo callableUnitInfo) throws IOException {
        int workerDataLength = dataInStream.readInt();
        if (workerDataLength == 0) {
            return;
        }

        int workerDataChannelsLength = dataInStream.readShort();
        for (int i = 0; i < workerDataChannelsLength; i++) {
            readWorkerDataChannelEntries(packageInfo, callableUnitInfo);
        }

        // Read worker info entries
        readWorkerInfoEntries(packageInfo, callableUnitInfo);
    }

    private void updateAttachFunctionInfo(PackageInfo packageInfo, BType attachedType, String funcName,
                                          FunctionInfo functionInfo) throws IOException {
        // Append the receiver type to the parameter types. This is done because in the VM,
        // first parameter will always be the attached type. These param types will be used
        // to allocate worker local data.
        // This is the only place where we append the receiver to the params.
        BType[] paramTypes =
                Stream.concat(Stream.of(functionInfo.attachedToType), Stream.of(functionInfo.getParamTypes()))
                        .toArray(BType[]::new);
        functionInfo.setParamTypes(paramTypes);

        if (attachedType.getTag() != TypeTags.OBJECT_TYPE_TAG && attachedType.getTag() != TypeTags.RECORD_TYPE_TAG) {
            return;
        }

        //Update the attachedFunctionInfo
        String objectInit;
        BStructureType structType = (BStructureType) attachedType;
        if (attachedType.getTag() == TypeTags.OBJECT_TYPE_TAG) {
            objectInit = CONSTRUCTOR_FUNCTION_SUFFIX;
        } else {
            objectInit = structType.getName() + INIT_FUNCTION_SUFFIX;
        }

        StructureTypeInfo typeInfo = (StructureTypeInfo) structType.getTypeInfo();
        typeInfo.funcInfoEntries.put(funcName, functionInfo);
        // Setting the object initializer
        if (objectInit.equals(funcName)) {
            typeInfo.initializer = functionInfo;
        }
    }

    public void readWorkerDataChannelEntries(PackageInfo packageInfo, CallableUnitInfo callableUnitInfo)
            throws IOException {
        int sourceCPIndex = dataInStream.readInt();
        int targetCPIndex = dataInStream.readInt();

        UTF8CPEntry sourceCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(sourceCPIndex);
        UTF8CPEntry targetCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(targetCPIndex);

        WorkerDataChannelInfo workerDataChannelInfo = new WorkerDataChannelInfo(sourceCPIndex,
                sourceCPEntry.getValue(), targetCPIndex, targetCPEntry.getValue());

        int dataChannelRefCPIndex = dataInStream.readInt();
        WorkerDataChannelRefCPEntry refCPEntry = (WorkerDataChannelRefCPEntry) packageInfo
                .getCPEntry(dataChannelRefCPIndex);
        refCPEntry.setWorkerDataChannelInfo(workerDataChannelInfo);
        callableUnitInfo.addWorkerDataChannelInfo(workerDataChannelInfo);
    }

    private void setCallableUnitSignature(PackageInfo packageInfo, CallableUnitInfo callableUnitInfo, String sig) {
        callableUnitInfo.funcType = getFunctionType(packageInfo, sig);
        callableUnitInfo.setParamTypes(callableUnitInfo.funcType.paramTypes);
        callableUnitInfo.setRetParamTypes(callableUnitInfo.funcType.retParamTypes);
    }

    private BFunctionType getFunctionType(PackageInfo packageInfo, String sig) {
        char[] chars = sig.toCharArray();
        Stack<BType> typeStack = new Stack<>();
        this.typeSigReader.createFunctionType(new RuntimeTypeCreater(packageInfo), chars, 0, typeStack);
        return (BFunctionType) typeStack.pop();
    }

    private BType[] getParamTypes(PackageInfo packageInfo, String signature) {
        int index = 0;
        Stack<BType> typeStack = new Stack<>();
        char[] chars = signature.toCharArray();
        while (index < chars.length) {
            index = this.typeSigReader.createBTypeFromSig(new RuntimeTypeCreater(packageInfo), chars, index, typeStack);
        }

        return typeStack.toArray(new BType[0]);
    }

    private void readWorkerInfoEntries(PackageInfo packageInfo, CallableUnitInfo callableUnitInfo)
            throws IOException {
        int workerCount = dataInStream.readShort();
        // First worker is always the default worker.
        WorkerInfo defaultWorker = getWorkerInfo(packageInfo);
        callableUnitInfo.setDefaultWorkerInfo(defaultWorker);

        for (int i = 0; i < workerCount - 1; i++) {
            WorkerInfo workerInfo = getWorkerInfo(packageInfo);
            callableUnitInfo.addWorkerInfo(workerInfo.getWorkerName(), workerInfo);
        }
    }

    private WorkerInfo getWorkerInfo(PackageInfo packageInfo) throws IOException {
        int workerNameCPIndex = dataInStream.readInt();
        UTF8CPEntry workerNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(workerNameCPIndex);
        WorkerInfo workerInfo = new WorkerInfo(workerNameCPIndex, workerNameUTF8Entry.getValue());

        int wrkrDtChnlRefCPIndex = dataInStream.readInt();
        workerInfo.setWrkrDtChnlRefCPIndex(wrkrDtChnlRefCPIndex);

        if (wrkrDtChnlRefCPIndex >= 0) {
            WorkerDataChannelRefCPEntry refCPEntry = (WorkerDataChannelRefCPEntry) packageInfo
                    .getCPEntry(wrkrDtChnlRefCPIndex);

            workerInfo.setWorkerDataChannelInfoForForkJoin(refCPEntry.getWorkerDataChannelInfo());
        }

        readForkJoinInfo(packageInfo, workerInfo);
        // Read attributes
        readAttributeInfoEntries(packageInfo, packageInfo, workerInfo);
        CodeAttributeInfo codeAttribute = (CodeAttributeInfo) workerInfo.getAttributeInfo(
                AttributeInfo.Kind.CODE_ATTRIBUTE);
        workerInfo.setCodeAttributeInfo(codeAttribute);
        return workerInfo;
    }

    private void readForkJoinInfo(PackageInfo packageInfo, WorkerInfo workerInfo) throws IOException {
        int forkJoinCount = dataInStream.readShort();
        ForkjoinInfo[] forkjoinInfos = new ForkjoinInfo[forkJoinCount];
        for (int i = 0; i < forkJoinCount; i++) {
            ForkjoinInfo forkjoinInfo = getForkJoinInfo(packageInfo);
            forkjoinInfos[i] = forkjoinInfo;
        }
        workerInfo.setForkjoinInfos(forkjoinInfos);
    }

    private ForkjoinInfo getForkJoinInfo(PackageInfo packageInfo) throws IOException {
        int indexCPIndex = dataInStream.readInt();

        int argRegLength = dataInStream.readShort();
        int[] argRegs = new int[argRegLength];
        for (int i = 0; i < argRegLength; i++) {
            argRegs[i] = dataInStream.readInt();
        }

        ForkJoinCPEntry forkJoinCPEntry = (ForkJoinCPEntry) packageInfo.getCPEntry(indexCPIndex);
        ForkjoinInfo forkjoinInfo = new ForkjoinInfo(argRegs);
        forkjoinInfo.setIndex(forkJoinCPEntry.getForkJoinCPIndex());
        forkjoinInfo.setIndexCPIndex(indexCPIndex);

        int workerCount = dataInStream.readShort();
        for (int i = 0; i < workerCount; i++) {
            WorkerInfo workerInfo = getWorkerInfo(packageInfo);
            forkjoinInfo.addWorkerInfo(workerInfo.getWorkerName(), workerInfo);
        }

        boolean isTimeoutAvailable = dataInStream.readBoolean();
        forkjoinInfo.setTimeoutAvailable(isTimeoutAvailable);

        int joinTypeCPIndex = dataInStream.readInt();
        UTF8CPEntry joinTypeCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(joinTypeCPIndex);

        forkjoinInfo.setJoinType(joinTypeCPEntry.getValue());
        forkjoinInfo.setJoinTypeCPIndex(joinTypeCPIndex);

        int joinWorkerCount = dataInStream.readInt();
        forkjoinInfo.setWorkerCount(joinWorkerCount);

        int joinWrkrCPIndexesLen = dataInStream.readShort();
        int[] joinWrkrCPIndexes = new int[joinWrkrCPIndexesLen];
        String[] joinWrkrNames = new String[joinWrkrCPIndexesLen];
        for (int i = 0; i < joinWrkrCPIndexesLen; i++) {
            int cpIndex = dataInStream.readInt();
            UTF8CPEntry workerNameCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(cpIndex);
            joinWrkrCPIndexes[i] = cpIndex;
            joinWrkrNames[i] = workerNameCPEntry.getValue();
        }
        forkjoinInfo.setJoinWrkrNameIndexes(joinWrkrCPIndexes);
        forkjoinInfo.setJoinWorkerNames(joinWrkrNames);
        forkJoinCPEntry.setForkjoinInfo(forkjoinInfo);
        return forkjoinInfo;
    }


    public void readAttributeInfoEntries(PackageInfo packageInfo, ConstantPool constantPool,
                                         AttributeInfoPool attributeInfoPool) throws IOException {
        int attributesCount = dataInStream.readShort();
        for (int k = 0; k < attributesCount; k++) {
            AttributeInfo attributeInfo = getAttributeInfo(packageInfo, constantPool);
            if (attributeInfo != null) {
                attributeInfoPool.addAttributeInfo(attributeInfo.getKind(), attributeInfo);
            }
        }
    }

    private AttributeInfo getAttributeInfo(PackageInfo packageInfo, ConstantPool constantPool) throws IOException {
        int attribNameCPIndex = dataInStream.readInt();
        UTF8CPEntry attribNameCPEntry = (UTF8CPEntry) constantPool.getCPEntry(attribNameCPIndex);
        AttributeInfo.Kind attribKind = AttributeInfo.Kind.fromString(attribNameCPEntry.getValue());
        if (attribKind == null) {
            throw new ProgramFileFormatException("unknown attribute kind " + attribNameCPEntry.getValue());
        }

        // The length of the attribute data in bytes. Ignoring this value for now.
        dataInStream.readInt();

        switch (attribKind) {
            case CODE_ATTRIBUTE:
                CodeAttributeInfo codeAttributeInfo = new CodeAttributeInfo();
                codeAttributeInfo.setAttributeNameIndex(attribNameCPIndex);
                codeAttributeInfo.setCodeAddrs(dataInStream.readInt());

                codeAttributeInfo.setMaxLongLocalVars(dataInStream.readUnsignedShort());
                codeAttributeInfo.setMaxDoubleLocalVars(dataInStream.readShort());
                codeAttributeInfo.setMaxStringLocalVars(dataInStream.readShort());
                codeAttributeInfo.setMaxIntLocalVars(dataInStream.readShort());
                codeAttributeInfo.setMaxRefLocalVars(dataInStream.readShort());

                codeAttributeInfo.setMaxLongRegs(dataInStream.readShort());
                codeAttributeInfo.setMaxDoubleRegs(dataInStream.readShort());
                codeAttributeInfo.setMaxStringRegs(dataInStream.readShort());
                codeAttributeInfo.setMaxIntRegs(dataInStream.readShort());
                codeAttributeInfo.setMaxRefRegs(dataInStream.readShort());
                return codeAttributeInfo;

            case VARIABLE_TYPE_COUNT_ATTRIBUTE:
                VarTypeCountAttributeInfo varCountAttributeInfo =
                        new VarTypeCountAttributeInfo(attribNameCPIndex);
                varCountAttributeInfo.setMaxLongVars(dataInStream.readShort());
                varCountAttributeInfo.setMaxDoubleVars(dataInStream.readShort());
                varCountAttributeInfo.setMaxStringVars(dataInStream.readShort());
                varCountAttributeInfo.setMaxIntVars(dataInStream.readShort());
                varCountAttributeInfo.setMaxRefVars(dataInStream.readShort());
                return varCountAttributeInfo;

            case ERROR_TABLE:
                ErrorTableAttributeInfo tableAttributeInfo = new ErrorTableAttributeInfo(attribNameCPIndex);
                int tableEntryCount = dataInStream.readShort();
                for (int i = 0; i < tableEntryCount; i++) {
                    int ipFrom = dataInStream.readInt();
                    int ipTo = dataInStream.readInt();
                    int ipTarget = dataInStream.readInt();
                    int regIndex = dataInStream.readInt();
                    ErrorTableEntry tableEntry = new ErrorTableEntry(ipFrom, ipTo, ipTarget, regIndex);
                    tableAttributeInfo.addErrorTableEntry(tableEntry);
                }
                return tableAttributeInfo;

            case LOCAL_VARIABLES_ATTRIBUTE:
                LocalVariableAttributeInfo localVarAttrInfo = new LocalVariableAttributeInfo(attribNameCPIndex);
                int localVarInfoCount = dataInStream.readShort();
                for (int i = 0; i < localVarInfoCount; i++) {
                    LocalVariableInfo localVariableInfo = getLocalVariableInfo(packageInfo, constantPool);
                    localVarAttrInfo.addLocalVarInfo(localVariableInfo);
                }
                return localVarAttrInfo;
            case WORKER_SEND_INS:
                WorkerSendInsAttributeInfo workerSendInsAttrInfo = new WorkerSendInsAttributeInfo(attribNameCPIndex);
                int sendInsCount = dataInStream.readShort();
                workerSendInsAttrInfo.sendIns = new String[sendInsCount];
                for (int i = 0; i < sendInsCount; i++) {
                    UTF8CPEntry stringCPEntry = (UTF8CPEntry) constantPool.getCPEntry(dataInStream.readInt());
                    workerSendInsAttrInfo.sendIns[i] = stringCPEntry.getValue();
                }
                return workerSendInsAttrInfo;
            case LINE_NUMBER_TABLE_ATTRIBUTE:
                LineNumberTableAttributeInfo lnNoTblAttrInfo = new LineNumberTableAttributeInfo(attribNameCPIndex);
                int lineNoInfoCount = dataInStream.readInt();
                for (int i = 0; i < lineNoInfoCount; i++) {
                    LineNumberInfo lineNumberInfo = getLineNumberInfo(constantPool);
                    lnNoTblAttrInfo.addLineNumberInfo(lineNumberInfo);
                }
                return lnNoTblAttrInfo;
            case DEFAULT_VALUE_ATTRIBUTE:
                DefaultValue defaultValue = getDefaultValue(constantPool);
                DefaultValueAttributeInfo defaultValAttrInfo =
                        new DefaultValueAttributeInfo(attribNameCPIndex, defaultValue);
                return defaultValAttrInfo;
            case DOCUMENT_ATTACHMENT_ATTRIBUTE:
                return getDocumentAttachmentAttribute(constantPool, attribNameCPIndex);
            case PARAMETER_DEFAULTS_ATTRIBUTE:
                ParamDefaultValueAttributeInfo paramDefaultValAttrInfo =
                        new ParamDefaultValueAttributeInfo(attribNameCPIndex);
                int paramDefaultsInfoCount = dataInStream.readShort();
                for (int i = 0; i < paramDefaultsInfoCount; i++) {
                    DefaultValue paramDefaultValue = getDefaultValue(constantPool);
                    paramDefaultValAttrInfo.addParamDefaultValueInfo(paramDefaultValue);
                }
                return paramDefaultValAttrInfo;
            case PARAMETERS_ATTRIBUTE:
                ParameterAttributeInfo parameterAttributeInfo = new ParameterAttributeInfo(attribNameCPIndex);
                parameterAttributeInfo.requiredParamsCount = dataInStream.readInt();
                parameterAttributeInfo.defaultableParamsCount = dataInStream.readInt();
                parameterAttributeInfo.restParamCount = dataInStream.readInt();
                return parameterAttributeInfo;
            case TAINT_TABLE:
                TaintTableAttributeInfo taintTableAttributeInfo = new TaintTableAttributeInfo(attribNameCPIndex);
                taintTableAttributeInfo.rowCount = dataInStream.readShort();
                taintTableAttributeInfo.columnCount = dataInStream.readShort();
                for (int rowIndex = 0; rowIndex < taintTableAttributeInfo.rowCount; rowIndex++) {
                    int paramIndex = dataInStream.readShort();
                    List<Boolean> taintRecord = new ArrayList<>();
                    for (int columnIndex = 0; columnIndex < taintTableAttributeInfo.columnCount; columnIndex++) {
                        taintRecord.add(dataInStream.readBoolean());
                    }
                    taintTableAttributeInfo.taintTable.put(paramIndex, taintRecord);
                }
                return taintTableAttributeInfo;
            default:
                throw new ProgramFileFormatException("unsupported attribute kind " + attribNameCPEntry.getValue());
        }
    }


    private AttributeInfo getDocumentAttachmentAttribute(ConstantPool constantPool,
                                                         int attribNameCPIndex) throws IOException {
        int descCPIndex = dataInStream.readInt();
        String docDesc = getUTF8EntryValue(descCPIndex, constantPool);
        DocumentationAttributeInfo docAttrInfo = new DocumentationAttributeInfo(
                attribNameCPIndex, descCPIndex, docDesc);

        int noOfParamInfoEntries = dataInStream.readShort();
        for (int i = 0; i < noOfParamInfoEntries; i++) {
            int nameCPIndex = dataInStream.readInt();
            String name = getUTF8EntryValue(nameCPIndex, constantPool);
            int paramDescCPIndex = dataInStream.readInt();
            String paramDesc = getUTF8EntryValue(paramDescCPIndex, constantPool);
            ParameterDocumentInfo paramDocInfo = new ParameterDocumentInfo(nameCPIndex, name, paramDescCPIndex,
                    paramDesc);
            docAttrInfo.paramDocInfoList.add(paramDocInfo);
        }

        boolean isReturnDocDescriptionAvailable = dataInStream.readBoolean();
        if (isReturnDocDescriptionAvailable) {
            int returnParamDescCPIndex = dataInStream.readInt();
            docAttrInfo.returnParameterDescription = getUTF8EntryValue(returnParamDescCPIndex, constantPool);
        }

        return docAttrInfo;
    }

    private LocalVariableInfo getLocalVariableInfo(PackageInfo packageInfo, ConstantPool constantPool)
            throws IOException {
        int varNameCPIndex = dataInStream.readInt();
        UTF8CPEntry varNameCPEntry = (UTF8CPEntry) constantPool.getCPEntry(varNameCPIndex);
        int variableIndex = dataInStream.readInt();

        int typeSigCPIndex = dataInStream.readInt();
        int scopeStartLineNumber = dataInStream.readInt();
        int scopeEndLineNumber = dataInStream.readInt();

        boolean isIdentifierLiteral = dataInStream.readBoolean();

        UTF8CPEntry typeSigCPEntry = (UTF8CPEntry) constantPool.getCPEntry(typeSigCPIndex);

        BType type = getBTypeFromDescriptor(packageInfo, typeSigCPEntry.getValue());
        LocalVariableInfo localVariableInfo = new LocalVariableInfo(varNameCPEntry.getValue(), varNameCPIndex,
                variableIndex, typeSigCPIndex, type, scopeStartLineNumber, scopeEndLineNumber, isIdentifierLiteral);
        int attchmntIndexesLength = dataInStream.readShort();
        int[] attachmentIndexes = new int[attchmntIndexesLength];
        for (int i = 0; i < attchmntIndexesLength; i++) {
            attachmentIndexes[i] = dataInStream.readInt();
        }
        localVariableInfo.setAttachmentIndexes(attachmentIndexes);

        return localVariableInfo;
    }

    private LineNumberInfo getLineNumberInfo(ConstantPool constantPool) throws IOException {
        int lineNumber = dataInStream.readInt();
        int fileNameCPIndex = dataInStream.readInt();
        int ip = dataInStream.readInt();

        UTF8CPEntry fileNameCPEntry = (UTF8CPEntry) constantPool.getCPEntry(fileNameCPIndex);

        LineNumberInfo lineNumberInfo = new LineNumberInfo(lineNumber, fileNameCPIndex,
                fileNameCPEntry.getValue(), ip);

        //In here constant pool is always a packageInfo since only package info has lineNumberTableAttribute
        lineNumberInfo.setPackageInfo((PackageInfo) constantPool);

        return lineNumberInfo;
    }

    private void readInstructions(PackageInfo packageInfo) throws IOException {
        int codeLength = dataInStream.readInt();
        byte[] code = new byte[codeLength];
        // Ignore bytes read should be same as the code length.
        dataInStream.read(code);
        DataInputStream codeStream = new DataInputStream(new ByteArrayInputStream(code));
        while (codeStream.available() > 0) {
            int i, j, k, h, l;
            int funcRefCPIndex;
            FunctionRefCPEntry funcRefCPEntry;
            int flags;
            int[] argRegs;
            int[] retRegs;

            int opcode = codeStream.readUnsignedByte();
            switch (opcode) {
                case InstructionCodes.HALT:
                case InstructionCodes.RET:
                    packageInfo.addInstruction(InstructionFactory.get(opcode));
                    break;

                case InstructionCodes.ICONST_0:
                case InstructionCodes.ICONST_1:
                case InstructionCodes.ICONST_2:
                case InstructionCodes.ICONST_3:
                case InstructionCodes.ICONST_4:
                case InstructionCodes.ICONST_5:
                case InstructionCodes.FCONST_0:
                case InstructionCodes.FCONST_1:
                case InstructionCodes.FCONST_2:
                case InstructionCodes.FCONST_3:
                case InstructionCodes.FCONST_4:
                case InstructionCodes.FCONST_5:
                case InstructionCodes.BCONST_0:
                case InstructionCodes.BCONST_1:
                case InstructionCodes.RCONST_NULL:
                case InstructionCodes.GOTO:
                case InstructionCodes.PANIC:
                case InstructionCodes.NEWXMLSEQ:
                    i = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, i));
                    break;

                case InstructionCodes.VFPLOAD:
                case InstructionCodes.FPLOAD:
                    readFunctionPointerLoadInstruction(packageInfo, codeStream, opcode);
                    break;
                case InstructionCodes.ICONST:
                case InstructionCodes.FCONST:
                case InstructionCodes.SCONST:
                case InstructionCodes.BICONST:
                case InstructionCodes.DCONST:
                case InstructionCodes.BACONST:
                case InstructionCodes.IMOVE:
                case InstructionCodes.FMOVE:
                case InstructionCodes.SMOVE:
                case InstructionCodes.BMOVE:
                case InstructionCodes.RMOVE:
                case InstructionCodes.INEG:
                case InstructionCodes.FNEG:
                case InstructionCodes.DNEG:
                case InstructionCodes.BNOT:
                case InstructionCodes.REQ_NULL:
                case InstructionCodes.RNE_NULL:
                case InstructionCodes.BR_TRUE:
                case InstructionCodes.BR_FALSE:
                case InstructionCodes.NEWSTRUCT:
                case InstructionCodes.ITR_NEW:
                case InstructionCodes.XML2XMLATTRS:
                case InstructionCodes.NEWXMLCOMMENT:
                case InstructionCodes.NEWXMLTEXT:
                case InstructionCodes.XMLSEQSTORE:
                case InstructionCodes.TYPELOAD:
                case InstructionCodes.NEWMAP:
                case InstructionCodes.I2ANY:
                case InstructionCodes.BI2ANY:
                case InstructionCodes.F2ANY:
                case InstructionCodes.S2ANY:
                case InstructionCodes.B2ANY:
                case InstructionCodes.ANY2I:
                case InstructionCodes.ANY2BI:
                case InstructionCodes.ANY2F:
                case InstructionCodes.ANY2S:
                case InstructionCodes.ANY2B:
                case InstructionCodes.ANY2D:
                case InstructionCodes.ANY2JSON:
                case InstructionCodes.ANY2XML:
                case InstructionCodes.ANY2MAP:
                case InstructionCodes.ANY2TYPE:
                case InstructionCodes.ANY2DT:
                case InstructionCodes.I2F:
                case InstructionCodes.I2S:
                case InstructionCodes.I2B:
                case InstructionCodes.I2D:
                case InstructionCodes.I2BI:
                case InstructionCodes.BI2I:
                case InstructionCodes.F2I:
                case InstructionCodes.F2S:
                case InstructionCodes.F2B:
                case InstructionCodes.F2D:
                case InstructionCodes.S2I:
                case InstructionCodes.S2F:
                case InstructionCodes.S2B:
                case InstructionCodes.S2D:
                case InstructionCodes.B2I:
                case InstructionCodes.B2F:
                case InstructionCodes.B2S:
                case InstructionCodes.B2D:
                case InstructionCodes.D2I:
                case InstructionCodes.D2F:
                case InstructionCodes.D2S:
                case InstructionCodes.D2B:
                case InstructionCodes.DT2XML:
                case InstructionCodes.DT2JSON:
                case InstructionCodes.T2MAP:
                case InstructionCodes.XMLATTRS2MAP:
                case InstructionCodes.ANY2SCONV:
                case InstructionCodes.XML2S:
                case InstructionCodes.XMLLOADALL:
                case InstructionCodes.ARRAY2JSON:
                case InstructionCodes.REASON:
                case InstructionCodes.DETAIL:
                case InstructionCodes.FREEZE:
                case InstructionCodes.IS_FROZEN:
                case InstructionCodes.CLONE:
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, i, j));
                    break;
                case InstructionCodes.IRET:
                case InstructionCodes.FRET:
                case InstructionCodes.SRET:
                case InstructionCodes.BRET:
                case InstructionCodes.DRET:
                case InstructionCodes.RRET:
                    j = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, j));
                    break;
                case InstructionCodes.IALOAD:
                case InstructionCodes.BIALOAD:
                case InstructionCodes.FALOAD:
                case InstructionCodes.SALOAD:
                case InstructionCodes.BALOAD:
                case InstructionCodes.RALOAD:
                case InstructionCodes.JSONALOAD:
                case InstructionCodes.IASTORE:
                case InstructionCodes.BIASTORE:
                case InstructionCodes.FASTORE:
                case InstructionCodes.SASTORE:
                case InstructionCodes.BASTORE:
                case InstructionCodes.RASTORE:
                case InstructionCodes.JSONASTORE:
                case InstructionCodes.MAPSTORE:
                case InstructionCodes.JSONLOAD:
                case InstructionCodes.JSONSTORE:
                case InstructionCodes.IADD:
                case InstructionCodes.FADD:
                case InstructionCodes.SADD:
                case InstructionCodes.DADD:
                case InstructionCodes.XMLADD:
                case InstructionCodes.ISUB:
                case InstructionCodes.FSUB:
                case InstructionCodes.DSUB:
                case InstructionCodes.IMUL:
                case InstructionCodes.FMUL:
                case InstructionCodes.DMUL:
                case InstructionCodes.IDIV:
                case InstructionCodes.FDIV:
                case InstructionCodes.DDIV:
                case InstructionCodes.IMOD:
                case InstructionCodes.FMOD:
                case InstructionCodes.DMOD:
                case InstructionCodes.IEQ:
                case InstructionCodes.FEQ:
                case InstructionCodes.SEQ:
                case InstructionCodes.BEQ:
                case InstructionCodes.DEQ:
                case InstructionCodes.REQ:
                case InstructionCodes.REF_EQ:
                case InstructionCodes.INE:
                case InstructionCodes.FNE:
                case InstructionCodes.SNE:
                case InstructionCodes.BNE:
                case InstructionCodes.DNE:
                case InstructionCodes.RNE:
                case InstructionCodes.REF_NEQ:
                case InstructionCodes.IGT:
                case InstructionCodes.FGT:
                case InstructionCodes.DGT:
                case InstructionCodes.IGE:
                case InstructionCodes.FGE:
                case InstructionCodes.DGE:
                case InstructionCodes.ILT:
                case InstructionCodes.FLT:
                case InstructionCodes.DLT:
                case InstructionCodes.ILE:
                case InstructionCodes.FLE:
                case InstructionCodes.DLE:
                case InstructionCodes.IAND:
                case InstructionCodes.BIAND:
                case InstructionCodes.IOR:
                case InstructionCodes.BIOR:
                case InstructionCodes.IXOR:
                case InstructionCodes.BIXOR:
                case InstructionCodes.BILSHIFT:
                case InstructionCodes.BIRSHIFT:
                case InstructionCodes.IRSHIFT:
                case InstructionCodes.ILSHIFT:
                case InstructionCodes.IURSHIFT:
                case InstructionCodes.XMLATTRLOAD:
                case InstructionCodes.XMLATTRSTORE:
                case InstructionCodes.S2QNAME:
                case InstructionCodes.NEWXMLPI:
                case InstructionCodes.TEQ:
                case InstructionCodes.TNE:
                case InstructionCodes.XMLLOAD:
                case InstructionCodes.LENGTH:
                case InstructionCodes.STAMP:
                case InstructionCodes.CONVERT:
                case InstructionCodes.NEWSTREAM:
                case InstructionCodes.CHECKCAST:
                case InstructionCodes.TYPE_ASSERTION:
                case InstructionCodes.MAP2T:
                case InstructionCodes.JSON2T:
                case InstructionCodes.ANY2T:
                case InstructionCodes.ANY2C:
                case InstructionCodes.ANY2E:
                case InstructionCodes.IS_ASSIGNABLE:
                case InstructionCodes.XMLSEQLOAD:
                case InstructionCodes.T2JSON:
                case InstructionCodes.MAP2JSON:
                case InstructionCodes.JSON2MAP:
                case InstructionCodes.JSON2ARRAY:
                case InstructionCodes.INT_RANGE:
                case InstructionCodes.INEWARRAY:
                case InstructionCodes.BINEWARRAY:
                case InstructionCodes.FNEWARRAY:
                case InstructionCodes.SNEWARRAY:
                case InstructionCodes.BNEWARRAY:
                case InstructionCodes.RNEWARRAY:
                case InstructionCodes.O2JSON:
                case InstructionCodes.TYPE_TEST:
                case InstructionCodes.IS_LIKE:
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    k = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, i, j, k));
                    break;
                case InstructionCodes.TR_RETRY:
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    k = codeStream.readInt();
                    packageInfo.addInstruction(new Instruction.InstructionTrRetry(opcode, i, j, k));
                    break;
                case InstructionCodes.NEWQNAME:
                case InstructionCodes.NEWXMLELEMENT:
                case InstructionCodes.MAPLOAD:
                case InstructionCodes.ERROR:
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    k = codeStream.readInt();
                    h = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, i, j, k, h));
                    break;
                case InstructionCodes.TR_END:
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    k = codeStream.readInt();
                    h = codeStream.readInt();
                    packageInfo.addInstruction(new Instruction.InstructionTrEnd(opcode, i, j, k, h));
                    break;
                case InstructionCodes.TR_BEGIN:
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    k = codeStream.readInt();
                    h = codeStream.readInt();
                    l = codeStream.readInt();
                    packageInfo.addInstruction(new Instruction.InstructionTrBegin(opcode, i, j, k, h, l));
                    break;

                case InstructionCodes.NEWTABLE:
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    k = codeStream.readInt();
                    h = codeStream.readInt();
                    l = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, i, j, k, h, l));
                    break;
                case InstructionCodes.WAIT:
                    int c = codeStream.readInt();
                    int[] ops = new int[c + 3];
                    ops[0] = c;
                    ops[1] = codeStream.readInt();
                    ops[2] = codeStream.readInt();
                    for (int p = 0; p < c; p++) {
                        ops[p + 3] = codeStream.readInt();
                    }
                    packageInfo.addInstruction(InstructionFactory.get(opcode, ops));
                    break;
                case InstructionCodes.WAITALL:
                    int d = codeStream.readInt();
                    int[] oprds = new int[d + 3];
                    oprds[0] = d;
                    oprds[1] = codeStream.readInt();
                    oprds[2] = codeStream.readInt();
                    for (int p = 0; p < d; p++) {
                        oprds[p + 3] = codeStream.readInt();
                    }
                    packageInfo.addInstruction(InstructionFactory.get(opcode, oprds));
                    break;
                case InstructionCodes.FLUSH:
                    int retReg = codeStream.readInt();
                    int workerCount  = codeStream.readInt();
                    String[] workerList = new String[workerCount];
                    for (int wrkCount = 0; wrkCount < workerCount; wrkCount++) {
                        int channelRefCPIndex = codeStream.readInt();
                        WorkerDataChannelRefCPEntry channelRefCPEntry = (WorkerDataChannelRefCPEntry)
                                packageInfo.getCPEntry(channelRefCPIndex);
                        workerList[wrkCount] = channelRefCPEntry.getWorkerDataChannelInfo().getChannelName();
                    }
                    packageInfo.addInstruction(new Instruction.InstructionFlush(opcode, retReg, workerList));
                    break;
                case InstructionCodes.WORKERSYNCSEND:
                    int syncChannelRefCPIndex = codeStream.readInt();
                    WorkerDataChannelRefCPEntry syncChannelRefCPEntry = (WorkerDataChannelRefCPEntry)
                            packageInfo.getCPEntry(syncChannelRefCPIndex);
                    int syncSigCPIndex = codeStream.readInt();
                    UTF8CPEntry syncSigCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(syncSigCPIndex);
                    BType syncSendType = getParamTypes(packageInfo, syncSigCPEntry.getValue())[0];
                    int exprIndex = codeStream.readInt();
                    int syncSendIndex = codeStream.readInt();
                    WorkerDataChannelInfo syncChannelInfo = syncChannelRefCPEntry.getWorkerDataChannelInfo();
                    boolean channelSendInSameStrand =
                            syncChannelInfo.getSource().equals(BLangConstants.DEFAULT_WORKER_NAME);
                    packageInfo.addInstruction(new Instruction.InstructionWRKSyncSend(opcode, syncChannelRefCPIndex,
                            syncChannelInfo, syncSigCPIndex, syncSendType, exprIndex
                            , syncSendIndex, channelSendInSameStrand));
                    break;
                case InstructionCodes.IGLOAD:
                case InstructionCodes.FGLOAD:
                case InstructionCodes.SGLOAD:
                case InstructionCodes.BGLOAD:
                case InstructionCodes.RGLOAD:
                case InstructionCodes.IGSTORE:
                case InstructionCodes.FGSTORE:
                case InstructionCodes.SGSTORE:
                case InstructionCodes.BGSTORE:
                case InstructionCodes.RGSTORE:
                    int pkgRefCPIndex = codeStream.readInt();
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    PackageRefCPEntry pkgRefCPEntry = (PackageRefCPEntry) packageInfo.getCPEntry(pkgRefCPIndex);
                    packageInfo.addInstruction(InstructionFactory.get(opcode,
                            pkgRefCPEntry.getPackageInfo().pkgIndex, i, j));
                    break;
                case InstructionCodes.CALL:
                    funcRefCPIndex = codeStream.readInt();
                    flags = codeStream.readInt();
                    funcRefCPEntry = (FunctionRefCPEntry) packageInfo.getCPEntry(funcRefCPIndex);
                    packageInfo.addInstruction(new InstructionCALL(opcode, funcRefCPIndex,
                            funcRefCPEntry.getFunctionInfo(), flags, getArgRegs(codeStream), getArgRegs(codeStream)));
                    break;
                case InstructionCodes.VCALL:
                    int receiverRegIndex = codeStream.readInt();
                    funcRefCPIndex = codeStream.readInt();
                    flags = codeStream.readInt();
                    funcRefCPEntry = (FunctionRefCPEntry) packageInfo.getCPEntry(funcRefCPIndex);
                    packageInfo.addInstruction(new InstructionVCALL(opcode, receiverRegIndex, funcRefCPIndex,
                            funcRefCPEntry.getFunctionInfo(), flags, getArgRegs(codeStream), getArgRegs(codeStream)));
                    break;
                case InstructionCodes.FPCALL:
                    funcRefCPIndex = codeStream.readInt();
                    flags = codeStream.readInt();
                    argRegs = getArgRegs(codeStream);
                    retRegs = getArgRegs(codeStream);

                    FunctionCallCPEntry funcCallCPEntry = new FunctionCallCPEntry(flags, argRegs, retRegs);
                    int funcCallCPIndex = packageInfo.addCPEntry(funcCallCPEntry);

                    packageInfo.addInstruction(InstructionFactory.get(opcode, funcRefCPIndex, funcCallCPIndex));
                    break;
                case InstructionCodes.WRKSEND:
                case InstructionCodes.WRKRECEIVE:
                    int channelRefCPIndex = codeStream.readInt();
                    WorkerDataChannelRefCPEntry channelRefCPEntry = (WorkerDataChannelRefCPEntry)
                            packageInfo.getCPEntry(channelRefCPIndex);
                    int sigCPIndex = codeStream.readInt();
                    UTF8CPEntry sigCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(sigCPIndex);
                    BType bType = getParamTypes(packageInfo, sigCPEntry.getValue())[0];
                    WorkerDataChannelInfo dataChannelInfo = channelRefCPEntry.getWorkerDataChannelInfo();
                    boolean channelInSameStrand =  opcode == InstructionCodes.WRKSEND ? dataChannelInfo.getSource()
                            .equals(BLangConstants.DEFAULT_WORKER_NAME) : dataChannelInfo.getTarget()
                            .equals(BLangConstants.DEFAULT_WORKER_NAME);
                    packageInfo.addInstruction(new InstructionWRKSendReceive(opcode, channelRefCPIndex,
                            dataChannelInfo, sigCPIndex, bType, codeStream.readInt(), channelInSameStrand));
                    break;
                case InstructionCodes.CHNRECEIVE:
                    BType keyType = null;
                    int keyIndex = -1;
                    int hasKey = codeStream.readInt();
                    if (hasKey == 1) {
                        UTF8CPEntry keySigCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(codeStream.readInt());
                        keyType = getParamTypes(packageInfo, keySigCPEntry.getValue())[0];
                        keyIndex = codeStream.readInt();
                    }
                    String channelName = ((UTF8CPEntry) packageInfo.getCPEntry(codeStream.readInt())).getValue();
                    UTF8CPEntry receiverSigCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(codeStream.readInt());
                    BType chnReceiveType = getParamTypes(packageInfo, receiverSigCPEntry.getValue())[0];
                    int receiverIndex = codeStream.readInt();

                    packageInfo.addInstruction(new Instruction.InstructionCHNReceive(opcode, channelName,
                            chnReceiveType, receiverIndex, keyType, keyIndex));
                    break;
                case InstructionCodes.CHNSEND:
                    keyType = null;
                    keyIndex = -1;
                    hasKey = codeStream.readInt();
                    if (hasKey == 1) {
                        keyIndex = codeStream.readInt();
                        UTF8CPEntry keySigCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(codeStream.readInt());
                        keyType = getParamTypes(packageInfo, keySigCPEntry.getValue())[0];
                    }
                    String chnName = ((UTF8CPEntry) packageInfo.getCPEntry(codeStream.readInt())).getValue();
                    int dataIndex = codeStream.readInt();
                    UTF8CPEntry dataSigCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(codeStream.readInt());
                    BType dataType = getParamTypes(packageInfo, dataSigCPEntry.getValue())[0];
                    packageInfo.addInstruction(new Instruction.InstructionCHNSend(opcode, chnName, dataType,
                            dataIndex, keyType, keyIndex));
                    break;
                case InstructionCodes.ITR_NEXT:
                    int iteratorIndex = codeStream.readInt();
                    int[] typeTags = getArgRegs(codeStream);
                    retRegs = getArgRegs(codeStream);

                    int constraintTypeSigCPIndex = codeStream.readInt();
                    TypeRefCPEntry constraintTypeRefCPEntry =
                            (TypeRefCPEntry) packageInfo.getCPEntry(constraintTypeSigCPIndex);
                    BType constraintType = constraintTypeRefCPEntry.getType();
                    packageInfo.addInstruction(new InstructionIteratorNext(opcode, iteratorIndex, retRegs.length,
                            typeTags, retRegs, constraintType));
                    break;
                case InstructionCodes.LOCK:
                    int varCount = codeStream.readInt();
                    int fieldCount = codeStream.readInt();
                    BType[] varTypes = new BType[varCount];
                    int[] pkgRefs = new int[varCount + fieldCount];
                    int[] varRegs = new int[varCount + fieldCount];
                    int[] fieldRegs = new int[fieldCount];
                    for (int m = 0; m < varCount; m++) {
                        int varSigCPIndex = codeStream.readInt();
                        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) packageInfo.getCPEntry(varSigCPIndex);
                        varTypes[m] = typeRefCPEntry.getType();

                        pkgRefCPIndex = codeStream.readInt();
                        pkgRefCPEntry = (PackageRefCPEntry) packageInfo.getCPEntry(pkgRefCPIndex);

                        pkgRefs[m] = pkgRefCPEntry.getPackageInfo().pkgIndex;
                        varRegs[m] = codeStream.readInt();
                    }

                    String uuid = ((UTF8CPEntry) packageInfo.getCPEntry(codeStream.readInt())).getValue();

                    for (int n = 0; n < fieldCount; n++) {
                        pkgRefCPIndex = codeStream.readInt();
                        pkgRefCPEntry = (PackageRefCPEntry) packageInfo.getCPEntry(pkgRefCPIndex);

                        pkgRefs[varCount + n] = pkgRefCPEntry.getPackageInfo().pkgIndex;
                        varRegs[varCount + n] = codeStream.readInt();

                        fieldRegs[n] = codeStream.readInt();
                    }
                    packageInfo.addInstruction(new InstructionLock(opcode, varTypes, pkgRefs, varRegs, fieldRegs,
                            varCount, uuid));
                    break;
                case InstructionCodes.UNLOCK:
                    int globalVarCount = codeStream.readInt();
                    boolean hasFieldVar = (codeStream.readInt() > 0) ? true : false;
                    String lockUuid = ((UTF8CPEntry) packageInfo.getCPEntry(codeStream.readInt())).getValue();
                    BType[] globalVarTypes = new BType[globalVarCount];
                    int[] lockPkgRefs = new int[globalVarCount];
                    int[] globalVarRegs = new int[globalVarCount];

                    for (int m = 0; m < globalVarCount; m++) {
                        int varSigCPIndex = codeStream.readInt();
                        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) packageInfo.getCPEntry(varSigCPIndex);
                        globalVarTypes[m] = typeRefCPEntry.getType();

                        pkgRefCPIndex = codeStream.readInt();
                        pkgRefCPEntry = (PackageRefCPEntry) packageInfo.getCPEntry(pkgRefCPIndex);

                        lockPkgRefs[m] = pkgRefCPEntry.getPackageInfo().pkgIndex;
                        globalVarRegs[m] = codeStream.readInt();
                    }
                    packageInfo.addInstruction(new InstructionUnLock(opcode, globalVarTypes, lockPkgRefs, globalVarRegs,
                            globalVarCount, lockUuid, hasFieldVar));
                    break;
                default:
                    throw new ProgramFileFormatException("unknown opcode " + opcode +
                            " in module " + packageInfo.getPkgPath());
            }
        }
    }

    private void readFunctionPointerLoadInstruction(PackageInfo packageInfo, DataInputStream codeStream, int opcode)
            throws IOException {
        packageInfo.addInstruction(InstructionFactory.get(opcode, getFunctionPointerLoadOperands(codeStream)));
    }

    private int[] getFunctionPointerLoadOperands(DataInputStream codeStream) throws IOException {
        int h;
        int i;
        int j;
        int k;
        h = codeStream.readInt();
        i = codeStream.readInt();
        j = codeStream.readInt();
        k = codeStream.readInt();
        int[] operands;
        if (k == 0) { // no additional reading is needed
            operands = new int[4];
            operands[0] = h;
            operands[1] = i;
            operands[2] = j;
            operands[3] = k;
        } else { //this is a closure related scenario, so read the closure indexes
            operands = new int[4 + k];
            operands[0] = h;
            operands[1] = i;
            operands[2] = j;
            operands[3] = k;
            for (int x = 0; x < k; x++) {
                operands[x + 4] = codeStream.readInt();
            }
        }

        return operands;
    }

    void resolveCPEntries(PackageInfo currentPackageInfo) {
        for (ConstantPoolEntry cpEntry : unresolvedCPEntries) {
            PackageInfo packageInfo;
            StructureRefCPEntry structureRefCPEntry;
            switch (cpEntry.getEntryType()) {
                case CP_ENTRY_PACKAGE:
                    PackageRefCPEntry pkgRefCPEntry = (PackageRefCPEntry) cpEntry;
                    packageInfo = this.getPackageInfo(pkgRefCPEntry.getPackageName());
                    pkgRefCPEntry.setPackageInfo(packageInfo);
                    break;
                case CP_ENTRY_FUNCTION_REF:
                    FunctionRefCPEntry funcRefCPEntry = (FunctionRefCPEntry) cpEntry;
                    packageInfo = this.getPackageInfo(funcRefCPEntry.getPackagePath());
                    FunctionInfo functionInfo = packageInfo.getFunctionInfo(funcRefCPEntry.getFunctionName());
                    funcRefCPEntry.setFunctionInfo(functionInfo);
                    break;
                case CP_ENTRY_STRUCTURE_REF:
                    structureRefCPEntry = (StructureRefCPEntry) cpEntry;
                    packageInfo = this.getPackageInfo(structureRefCPEntry.getPackagePath());
                    CustomTypeInfo structureTypeInfo = packageInfo.getStructureTypeInfo(
                            structureRefCPEntry.getStructureName());
                    structureRefCPEntry.setStructureTypeInfo(structureTypeInfo);
                    break;
                case CP_ENTRY_TYPE_REF:
                    TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) cpEntry;
                    String typeSig = typeRefCPEntry.getTypeSig();
                    BType bType = getBTypeFromDescriptor(currentPackageInfo, typeSig);
                    typeRefCPEntry.setType(bType);
                    break;
                default:
                    break;
            }
        }
    }

    private void resolveUserDefinedTypes(PackageInfo packageInfo) {
        // TODO Improve this. We should be able to this in a single pass.
        TypeDefInfo[] structInfoEntries = packageInfo.getTypeDefInfoEntries();
        for (TypeDefInfo structInfo : structInfoEntries) {
            if (structInfo.typeTag == TypeTags.FINITE_TYPE_TAG) {
                continue;
            }
            StructureTypeInfo structureTypeInfo = (StructureTypeInfo) structInfo.typeInfo;
            StructFieldInfo[] fieldInfoEntries = structureTypeInfo.getFieldInfoEntries();

            BStructureType structType = structureTypeInfo.getType();
            Map<String, BField> structFields = new LinkedHashMap<>();
            for (int i = 0; i < fieldInfoEntries.length; i++) {
                // Get the BType from the type descriptor
                StructFieldInfo fieldInfo = fieldInfoEntries[i];
                String typeDesc = fieldInfo.getTypeDescriptor();
                BType fieldType = getBTypeFromDescriptor(packageInfo, typeDesc);
                fieldInfo.setFieldType(fieldType);

                // Create the StructField in the BStructType. This is required for the type equivalence algorithm
                BField structField = new BField(fieldType,
                        fieldInfo.getName(), fieldInfo.flags);
                structFields.put(structField.fieldName, structField);
            }

            if (structType.getTag() == TypeTags.RECORD_TYPE_TAG && !((BRecordType) structType).sealed) {
                RecordTypeInfo recTypeInfo = (RecordTypeInfo) structureTypeInfo;
                String restTypeDesc = recTypeInfo.getRestFieldTypeSignature();
                BType restFieldType = getBTypeFromDescriptor(packageInfo, restTypeDesc);
                recTypeInfo.getType().restFieldType = restFieldType;
            }

            VarTypeCountAttributeInfo attributeInfo = (VarTypeCountAttributeInfo)
                    structInfo.typeInfo.getAttributeInfo(AttributeInfo.Kind.VARIABLE_TYPE_COUNT_ATTRIBUTE);
            structType.setFieldTypeCount(attributeInfo.getVarTypeCount());
            structType.setFields(structFields);
        }

        for (ConstantPoolEntry cpEntry : unresolvedCPEntries) {
            switch (cpEntry.getEntryType()) {
                case CP_ENTRY_TYPE_REF:
                    TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) cpEntry;
                    String typeSig = typeRefCPEntry.getTypeSig();
                    BType bType = getBTypeFromDescriptor(packageInfo, typeSig);
                    typeRefCPEntry.setType(bType);
                    break;
                default:
                    break;
            }
        }
    }

    private void setAttachedFunctions(PackageInfo packageInfo) {
        TypeDefInfo[] structInfoEntries = packageInfo.getTypeDefInfoEntries();
        for (TypeDefInfo structInfo : structInfoEntries) {
            if (structInfo.typeTag == TypeTags.FINITE_TYPE_TAG) {
                continue;
            }
            StructureTypeInfo structureTypeInfo = (StructureTypeInfo) structInfo.typeInfo;
            BStructureType structType = structureTypeInfo.getType();

            // Resolve attached function signature
            if (structType.getTag() != TypeTags.OBJECT_TYPE_TAG && structType.getTag() != TypeTags.RECORD_TYPE_TAG) {
                continue;
            }

            int attachedFuncCount = structureTypeInfo.funcInfoEntries.size();
            BAttachedFunction[] attachedFunctions = new BAttachedFunction[attachedFuncCount];
            int count = 0;
            for (FunctionInfo attachedFuncInfo : structureTypeInfo.funcInfoEntries.values()) {
                BAttachedFunction attachedFunction =
                        new BAttachedFunction(attachedFuncInfo.name, attachedFuncInfo.funcType, attachedFuncInfo.flags);
                attachedFunctions[count++] = attachedFunction;
                if (structureTypeInfo.initializer == attachedFuncInfo) {
                    structureTypeInfo.getType().initializer = attachedFunction;
                }
            }
            structureTypeInfo.getType().setAttachedFunctions(attachedFunctions);
        }
    }

    private DefaultValue getDefaultValue(ConstantPool constantPool)
            throws IOException {
        int typeDescCPIndex = dataInStream.readInt();
        UTF8CPEntry typeDescCPEntry = (UTF8CPEntry) constantPool.getCPEntry(typeDescCPIndex);
        String typeDesc = typeDescCPEntry.getValue();
        DefaultValue defaultValue = new DefaultValue(typeDescCPIndex, typeDesc);

        int valueCPIndex;
        switch (typeDesc) {
            case TypeSignature.SIG_BOOLEAN:
                boolean boolValue = dataInStream.readBoolean();
                defaultValue.setBooleanValue(boolValue);
                break;
            case TypeSignature.SIG_INT:
                valueCPIndex = dataInStream.readInt();
                IntegerCPEntry integerCPEntry = (IntegerCPEntry) constantPool.getCPEntry(valueCPIndex);
                defaultValue.setIntValue(integerCPEntry.getValue());
                break;
            case TypeSignature.SIG_BYTE:
                valueCPIndex = dataInStream.readInt();
                ByteCPEntry byteCPEntry = (ByteCPEntry) constantPool.getCPEntry(valueCPIndex);
                defaultValue.setByteValue(byteCPEntry.getValue());
                break;
            case TypeSignature.SIG_FLOAT:
                valueCPIndex = dataInStream.readInt();
                FloatCPEntry floatCPEntry = (FloatCPEntry) constantPool.getCPEntry(valueCPIndex);
                defaultValue.setFloatValue(floatCPEntry.getValue());
                break;
            case TypeSignature.SIG_DECIMAL:
                valueCPIndex = dataInStream.readInt();
                UTF8CPEntry decimalEntry = (UTF8CPEntry) constantPool.getCPEntry(valueCPIndex);
                defaultValue.setDecimalValue(new BigDecimal(decimalEntry.getValue(), MathContext.DECIMAL128));
                break;
            case TypeSignature.SIG_STRING:
                valueCPIndex = dataInStream.readInt();
                UTF8CPEntry stringCPEntry = (UTF8CPEntry) constantPool.getCPEntry(valueCPIndex);
                defaultValue.setStringValue(stringCPEntry.getValue());
                break;
            case TypeSignature.SIG_NULL:
                break;
            default:
                throw new ProgramFileFormatException("unknown default value type " + typeDesc);
        }
        return defaultValue;
    }

    private BValue getDefaultValueToBValue(DefaultValue defaultValue)
            throws IOException {
        String typeDesc = defaultValue.getTypeDesc();
        BValue value;

        switch (typeDesc) {
            case TypeSignature.SIG_BOOLEAN:
                boolean boolValue = defaultValue.getBooleanValue();
                value = new BBoolean(boolValue);
                break;
            case TypeSignature.SIG_INT:
                long intValue = defaultValue.getIntValue();
                value = new BInteger(intValue);
                break;
            case TypeSignature.SIG_BYTE:
                byte byteValue = defaultValue.getByteValue();
                value = new BByte(byteValue);
                break;
            case TypeSignature.SIG_FLOAT:
                double floatValue = defaultValue.getFloatValue();
                value = new BFloat(floatValue);
                break;
            case TypeSignature.SIG_DECIMAL:
                BigDecimal decimalValue = defaultValue.getDecimalValue();
                value = new BDecimal(decimalValue);
                break;
            case TypeSignature.SIG_STRING:
                String stringValue = defaultValue.getStringValue();
                value = new BString(stringValue);
                break;
            case TypeSignature.SIG_NULL:
                value = null;
                break;
            default:
                throw new ProgramFileFormatException("unknown default value type " + typeDesc);

        }

        return value;
    }

    private int[] getArgRegs(DataInputStream codeStream) throws IOException {
        int nArgRegs = codeStream.readInt();
        int[] argRegs = new int[nArgRegs];
        for (int index = 0; index < nArgRegs; index++) {
            argRegs[index] = codeStream.readInt();
        }
        return argRegs;
    }

    private String getUTF8EntryValue(int cpEntryIndex, ConstantPool constantPool) {
        UTF8CPEntry pkgNameCPEntry = (UTF8CPEntry) constantPool.getCPEntry(cpEntryIndex);
        return pkgNameCPEntry.getValue();
    }

    private PackageInfo getPackageInfo(String pkgPath) {
        PackageInfo pkgInfo = programFile.getPackageInfo(pkgPath);

        // if the package info is not available in the balx file, then it should be read from the balo
        if (pkgInfo != null) {
            return pkgInfo;
        }

        try {
            PackageFileReader pkgFileReader = new PackageFileReader(this.programFile);
            pkgFileReader.readPackage(pkgPath);
        } catch (IOException e) {
            throw new BLangRuntimeException("error reading module: " + pkgPath, e);
        }

        return programFile.getPackageInfo(pkgPath);
    }

    private BType getBTypeFromDescriptor(PackageInfo packageInfo, String desc) {
        return this.typeSigReader.getBTypeFromDescriptor(new RuntimeTypeCreater(packageInfo), desc);
    }

    private String getPackagePath(String orgName, String pkgName, String version) {
        if (Names.DOT.value.equals(pkgName)) {
            return pkgName;
        }
        if (orgName.equals(Names.ANON_ORG.value)) {
            orgName = "";
        } else {
            orgName = orgName + Names.ORG_NAME_SEPARATOR.value;
        }

        if (Names.EMPTY.value.equals(version)) {
            return orgName + pkgName;
        }

        return orgName + pkgName + Names.VERSION_SEPARATOR.value + version;
    }

    /**
     * Create types for compiler phases.
     *
     * @since 0.975.0
     */
    private class RuntimeTypeCreater implements TypeCreater<BType> {

        PackageInfo packageInfo;

        public RuntimeTypeCreater(PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
        }

        @Override
        public BType getBasicType(char typeChar) {
            switch (typeChar) {
                case 'I':
                    return BTypes.typeInt;
                case 'W':
                    return BTypes.typeByte;
                case 'F':
                    return BTypes.typeFloat;
                case 'L':
                    return BTypes.typeDecimal;
                case 'S':
                    return BTypes.typeString;
                case 'B':
                    return BTypes.typeBoolean;
                case 'Y':
                    return BTypes.typeDesc;
                case 'A':
                    return BTypes.typeAny;
                case 'N':
                    return BTypes.typeNull;
                case 'K':
                    return BTypes.typeAnydata;
                default:
                    throw new IllegalArgumentException("unsupported basic type char: " + typeChar);
            }
        }

        @Override
        public BType getBuiltinRefType(String typeName) {
            return BTypes.getTypeFromName(typeName);
        }

        @Override
        public BType getRefType(char typeChar, String pkgId, String typeName) {
            if (typeName.isEmpty()) {
                return null;
            }

            PackageInfo packageInfoOfType;
            if (pkgId != null) {
                packageInfoOfType = getPackageInfo(pkgId);
            } else {
                packageInfoOfType = packageInfo;
            }
            switch (typeChar) {
                case 'X':
                    return BTypes.typeAnyService;
                default:
                    TypeDefInfo typeDefInfo = packageInfoOfType.getTypeDefInfo(typeName);
                    if (typeDefInfo != null) {
                        return typeDefInfo.typeInfo.getType();
                    }
                    return BTypes.getTypeFromName(typeName);
            }
        }

        @Override
        public BType getConstrainedType(char typeChar, BType constraint) {
            switch (typeChar) {
                case 'D':
                    if (constraint == null) {
                        return BTypes.typeTable;
                    }
                    return new BTableType(constraint);
                case 'M':
                    if (constraint == null || constraint == BTypes.typeAny) {
                        return BTypes.typeMap;
                    }
                    return new BMapType(constraint);
                case 'H':
                    return new BStreamType(constraint);
                case 'G':
                case 'T':
                case 'X':
                case 'Q':
                default:
                    return constraint;
            }
        }

        @Override
        public BType getArrayType(BType elementType, int size) {
            return new BArrayType(elementType, size);
        }

        @Override
        public BType getCollectionType(char typeChar, List<BType> memberTypes) {
            switch (typeChar) {
                case 'O':
                    return new BUnionType(memberTypes);
                case 'P':
                    return new BTupleType(memberTypes);
                default:
                    throw new IllegalArgumentException("unsupported collection type char: " + typeChar);
            }
        }

        @Override
        public BType getFunctionType(List<BType> funcParams, BType retType) {
            BType[] returnTypes;
            if (retType == null) {
                returnTypes = new BType[0];
            } else {
                returnTypes = new BType[]{retType};
            }
            return new BFunctionType(funcParams.toArray(new BType[funcParams.size()]), returnTypes);
        }

        @Override
        public BType getErrorType(BType reasonType, BType detailsType) {
            if (reasonType == BTypes.typeString && detailsType == BTypes.typeMap) {
                return BTypes.typeError;
            }
            return new BErrorType(reasonType, detailsType);
        }
    }
}

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

import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BFiniteType;
import org.ballerinalang.model.types.BFunctionType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BServiceType;
import org.ballerinalang.model.types.BStreamType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BTableType;
import org.ballerinalang.model.types.BTupleType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.BUnionType;
import org.ballerinalang.model.types.TypeSignature;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.Flags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.NativeUnitLoader;
import org.ballerinalang.util.codegen.Instruction.InstructionCALL;
import org.ballerinalang.util.codegen.Instruction.InstructionFORKJOIN;
import org.ballerinalang.util.codegen.Instruction.InstructionIteratorNext;
import org.ballerinalang.util.codegen.Instruction.InstructionLock;
import org.ballerinalang.util.codegen.Instruction.InstructionTCALL;
import org.ballerinalang.util.codegen.Instruction.InstructionVCALL;
import org.ballerinalang.util.codegen.Instruction.InstructionWRKSendReceive;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.codegen.attributes.DefaultValueAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ErrorTableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LineNumberTableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ParamDefaultValueAttributeInfo;
import org.ballerinalang.util.codegen.attributes.VarTypeCountAttributeInfo;
import org.ballerinalang.util.codegen.cpentries.ActionRefCPEntry;
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
import org.ballerinalang.util.codegen.cpentries.TransformerRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.TypeRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;
import org.ballerinalang.util.codegen.cpentries.WorkerDataChannelRefCPEntry;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.ProgramFileFormatException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import static org.ballerinalang.util.BLangConstants.CONSTRUCTOR_FUNCTION_SUFFIX;
import static org.ballerinalang.util.BLangConstants.INIT_FUNCTION_SUFFIX;
import static org.ballerinalang.util.BLangConstants.MAGIC_NUMBER;
import static org.ballerinalang.util.BLangConstants.START_FUNCTION_SUFFIX;
import static org.ballerinalang.util.BLangConstants.STOP_FUNCTION_SUFFIX;
import static org.ballerinalang.util.BLangConstants.VERSION_NUMBER;

/**
 * Reads a Ballerina program from a file.
 *
 * @since 0.90
 */
public class ProgramFileReader {

    private ProgramFile programFile;

    private List<ConstantPoolEntry> unresolvedCPEntries = new ArrayList<>();

    // This is used to assign a number to a package.
    // This number is used as an offset when dealing with package level variables.
    // A temporary solution until we implement dynamic package loading.
    private int currentPkgIndex = 0;

    public ProgramFile readProgram(Path programFilePath) throws IOException {
        InputStream fileIS = null;
        try {
            programFile = new ProgramFile();
            programFile.setProgramFilePath(programFilePath);
            fileIS = Files.newInputStream(programFilePath, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
            BufferedInputStream bufferedIS = new BufferedInputStream(fileIS);
            DataInputStream dataInStream = new DataInputStream(bufferedIS);
            return readProgramInternal(dataInStream);
        } finally {
            if (fileIS != null) {
                fileIS.close();
            }
        }
    }

    public ProgramFile readProgram(InputStream programFileInStream) throws IOException {
        programFile = new ProgramFile();
        DataInputStream dataInStream = new DataInputStream(programFileInStream);
        return readProgramInternal(dataInStream);
    }

    private ProgramFile readProgramInternal(DataInputStream dataInStream) throws IOException {
        int magicNumber = dataInStream.readInt();
        if (magicNumber != MAGIC_NUMBER) {
            throw new BLangRuntimeException("ballerina: invalid magic number " + magicNumber);
        }

        short version = dataInStream.readShort();
        if (version != VERSION_NUMBER) {
            throw new BLangRuntimeException("ballerina: unsupported program file version " + version);
        }
        programFile.setVersion(version);

        readConstantPool(dataInStream, programFile);
        readEntryPoint(dataInStream);

        // Read PackageInfo entries
        int pkgInfoCount = dataInStream.readShort();
        for (int i = 0; i < pkgInfoCount; i++) {
            currentPkgIndex = i;
            readPackageInfo(dataInStream);
        }

        PackageInfo entryPkg = programFile.getPackageInfo(programFile.getEntryPkgName());
        programFile.setEntryPackage(entryPkg);
        entryPkg.setProgramFile(programFile);

        // Read program level attributes
        readAttributeInfoEntries(dataInStream, programFile, programFile);

        // TODO This needs to be moved out of this class
        programFile.initializeGlobalMemArea();
        return programFile;
    }

    private void readConstantPool(DataInputStream dataInStream,
                                  ConstantPool constantPool) throws IOException {
        int constantPoolSize = dataInStream.readInt();
        for (int i = 0; i < constantPoolSize; i++) {
            byte cpTag = dataInStream.readByte();
            ConstantPoolEntry.EntryType cpEntryType = ConstantPoolEntry.EntryType.values()[cpTag - 1];
            ConstantPoolEntry cpEntry = readCPEntry(dataInStream, constantPool, cpEntryType);
            constantPool.addCPEntry(cpEntry);
        }
    }

    private ConstantPoolEntry readCPEntry(DataInputStream dataInStream,
                                          ConstantPool constantPool,
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

            case CP_ENTRY_INTEGER:
                long longVal = dataInStream.readLong();
                return new IntegerCPEntry(longVal);

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

            case CP_ENTRY_TRANSFORMER_REF:
                pkgCPIndex = dataInStream.readInt();
                packageRefCPEntry = (PackageRefCPEntry) constantPool.getCPEntry(pkgCPIndex);

                cpIndex = dataInStream.readInt();
                utf8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(cpIndex);
                String transformerName = utf8CPEntry.getValue();
                TransformerRefCPEntry transformerRefCPEntry = new TransformerRefCPEntry(pkgCPIndex,
                        packageRefCPEntry.getPackageName(), cpIndex, transformerName);

                // Find the transformerInfo
                packageInfoOptional = Optional.ofNullable(
                        programFile.getPackageInfo(packageRefCPEntry.getPackageName()));
                Optional<TransformerInfo> transInfoOptional = packageInfoOptional.map(
                        packageInfo -> packageInfo.getTransformerInfo(transformerName));
                if (!transInfoOptional.isPresent()) {
                    // This must reference to the current package and the current package is not been read yet.
                    // Therefore we add this to the unresolved CP Entry list.
                    unresolvedCPEntries.add(transformerRefCPEntry);
                    return transformerRefCPEntry;
                }

                transformerRefCPEntry.setTransformerInfo(transInfoOptional.get());
                return transformerRefCPEntry;

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
                Optional<StructureTypeInfo> structInfoOptional = packageInfoOptional.map(
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

    private void readEntryPoint(DataInputStream dataInStream) throws IOException {
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

    private void readPackageInfo(DataInputStream dataInStream) throws IOException {
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.pkgIndex = currentPkgIndex;

        // Read constant pool in the package.
        readConstantPool(dataInStream, packageInfo);

        int pkgNameCPIndex = dataInStream.readInt();
        UTF8CPEntry pkgNameCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(pkgNameCPIndex);
        packageInfo.nameCPIndex = pkgNameCPIndex;
        packageInfo.pkgPath = pkgNameCPEntry.getValue();

        int pkgVersionCPIndex = dataInStream.readInt();
        UTF8CPEntry pkgVersionCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(pkgVersionCPIndex);
        packageInfo.versionCPIndex = pkgVersionCPIndex;
        packageInfo.pkgVersion = pkgVersionCPEntry.getValue();

        packageInfo.setProgramFile(programFile);
        programFile.addPackageInfo(packageInfo.pkgPath, packageInfo);

        // Read struct info entries
        readStructInfoEntries(dataInStream, packageInfo);

        // Read type definition info entries
        readTypeDefinitionInfoEntries(dataInStream, packageInfo);

        // Read service info entries
        readServiceInfoEntries(dataInStream, packageInfo);

        // Resolve user-defined type i.e. structs and connectors
        resolveUserDefinedTypes(packageInfo);

        // Read resource info entries.
        readResourceInfoEntries(dataInStream, packageInfo);

        // Read constant info entries
        readConstantInfoEntries(dataInStream, packageInfo);

        // Read global var info entries
        readGlobalVarInfoEntries(dataInStream, packageInfo);

        // Read function info entries in the package
        readFunctionInfoEntries(dataInStream, packageInfo);

        // Read transformer info entries in the package
        readTransformerInfoEntries(dataInStream, packageInfo);

        // TODO Read annotation info entries

        // Resolve unresolved CP entries.
        resolveCPEntries();

        // Read attribute info entries
        readAttributeInfoEntries(dataInStream, packageInfo, packageInfo);

        // Read instructions
        readInstructions(dataInStream, packageInfo);

        packageInfo.complete();
    }

    private void readStructInfoEntries(DataInputStream dataInStream,
                                       PackageInfo packageInfo) throws IOException {
        int structCount = dataInStream.readShort();
        for (int i = 0; i < structCount; i++) {
            // Create struct info entry
            int structNameCPIndex = dataInStream.readInt();
            int flags = dataInStream.readInt();
            UTF8CPEntry structNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(structNameCPIndex);
            String structName = structNameUTF8Entry.getValue();
            StructInfo structInfo = new StructInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                    structNameCPIndex, structName, flags);
            packageInfo.addStructInfo(structName, structInfo);

            // Set struct type
            BStructType bStructType = new BStructType(structInfo, structName, packageInfo.getPkgPath(), flags);
            structInfo.setType(bStructType);

            // Read struct field info entries
            int structFiledCount = dataInStream.readShort();
            for (int j = 0; j < structFiledCount; j++) {
                // Read field name
                int fieldNameCPIndex = dataInStream.readInt();
                UTF8CPEntry fieldNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(fieldNameCPIndex);

                // Read field type signature
                int fieldTypeSigCPIndex = dataInStream.readInt();
                UTF8CPEntry fieldTypeSigUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(fieldTypeSigCPIndex);

                int fieldFlags = dataInStream.readInt();

                StructFieldInfo fieldInfo = new StructFieldInfo(fieldNameCPIndex, fieldNameUTF8Entry.getValue(),
                        fieldTypeSigCPIndex, fieldTypeSigUTF8Entry.getValue(), fieldFlags);
                structInfo.addFieldInfo(fieldInfo);

                readAttributeInfoEntries(dataInStream, packageInfo, fieldInfo);
            }

            String defaultInit = structName + INIT_FUNCTION_SUFFIX;
            String objectInit = CONSTRUCTOR_FUNCTION_SUFFIX;

            // Read attached function info entries
            int attachedFuncCount = dataInStream.readShort();
            for (int j = 0; j < attachedFuncCount; j++) {
                // Read function name
                int nameCPIndex = dataInStream.readInt();
                UTF8CPEntry nameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(nameCPIndex);
                String attachedFuncName = nameUTF8Entry.getValue();

                // Read function type signature
                int typeSigCPIndex = dataInStream.readInt();
                UTF8CPEntry typeSigUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(typeSigCPIndex);

                int funcFlags = dataInStream.readInt();
                AttachedFunctionInfo functionInfo = new AttachedFunctionInfo(nameCPIndex, attachedFuncName,
                        typeSigCPIndex, typeSigUTF8Entry.getValue(), funcFlags);
                structInfo.funcInfoEntries.put(functionInfo.name, functionInfo);

                // TODO remove when removing struct
                // Setting the initializer function info, if any.
                if (structName.equals(attachedFuncName)) {
                    structInfo.initializer = functionInfo;
                }
                // Setting the object initializer
                if (objectInit.equals(attachedFuncName)) {
                    structInfo.initializer = functionInfo;
                }

                // TODO remove when removing struct
                // Setting the default initializer function info
                if (defaultInit.equals(attachedFuncName)) {
                    structInfo.defaultsValuesInitFunc = functionInfo;
                }
            }

            // Read attributes of the struct info
            readAttributeInfoEntries(dataInStream, packageInfo, structInfo);
        }
    }

    private void readTypeDefinitionInfoEntries(DataInputStream dataInStream,
                                               PackageInfo packageInfo) throws IOException {
        int typeDefCount = dataInStream.readShort();
        for (int i = 0; i < typeDefCount; i++) {

            int typeDefNameCPIndex = dataInStream.readInt();
            int flags = dataInStream.readInt();
            UTF8CPEntry typeDefNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(typeDefNameCPIndex);
            String typeDefName = typeDefNameUTF8Entry.getValue();
            TypeDefinitionInfo typeDefinitionInfo = new TypeDefinitionInfo(packageInfo.getPkgNameCPIndex(),
                    packageInfo.getPkgPath(),
                    typeDefNameCPIndex, typeDefName, flags);
            packageInfo.addTypeDefinitionInfo(typeDefName, typeDefinitionInfo);

            BFiniteType finiteType = new BFiniteType(typeDefName, packageInfo.getPkgPath());
            typeDefinitionInfo.setType(finiteType);

            int memberTypeCount = dataInStream.readShort();
            for (int j = 0; j < memberTypeCount; j++) {
                int memberTypeCPIndex = dataInStream.readInt();
                TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) packageInfo.getCPEntry(memberTypeCPIndex);
                finiteType.memberTypes.add(getBTypeFromDescriptor(typeRefCPEntry.getTypeSig()));
            }

            int valueSpaceCount = dataInStream.readShort();
            for (int k = 0; k < valueSpaceCount; k++) {
                finiteType.valueSpace.add(getDefaultValueToBValue(getDefaultValue(dataInStream, packageInfo)));
            }

            readAttributeInfoEntries(dataInStream, packageInfo, typeDefinitionInfo);
        }

    }

    private void readServiceInfoEntries(DataInputStream dataInStream,
                                        PackageInfo packageInfo) throws IOException {
        int serviceCount = dataInStream.readShort();
        for (int i = 0; i < serviceCount; i++) {
            // Read connector name cp index
            int serviceNameCPIndex = dataInStream.readInt();
            UTF8CPEntry serviceNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(serviceNameCPIndex);
            int flags = dataInStream.readInt();

            // Read connector signature cp index;
            int endpointNameCPIndex = dataInStream.readInt();
            UTF8CPEntry endpointNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(endpointNameCPIndex);

            ServiceInfo serviceInfo = new ServiceInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                    serviceNameCPIndex, serviceNameUTF8Entry.getValue(), flags,
                    endpointNameCPIndex, endpointNameUTF8Entry.getValue());
            serviceInfo.setPackageInfo(packageInfo);
            packageInfo.addServiceInfo(serviceInfo.getName(), serviceInfo);
            serviceInfo.setType(new BServiceType(serviceInfo.getName(), packageInfo.getPkgPath()));
        }
    }

    private void readResourceInfoEntries(DataInputStream dataInStream,
                                         PackageInfo packageInfo) throws IOException {
        for (ServiceInfo serviceInfo : packageInfo.getServiceInfoEntries()) {
            int actionCount = dataInStream.readShort();
            for (int j = 0; j < actionCount; j++) {
                // Read action name;
                int resNameCPIndex = dataInStream.readInt();
                UTF8CPEntry resNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(resNameCPIndex);
                String resName = resNameUTF8Entry.getValue();

                ResourceInfo resourceInfo = new ResourceInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                        resNameCPIndex, resName);
                resourceInfo.setServiceInfo(serviceInfo);
                resourceInfo.setPackageInfo(packageInfo);
                serviceInfo.addResourceInfo(resName, resourceInfo);

                // Read action signature
                int resSigCPIndex = dataInStream.readInt();
                resourceInfo.setSignatureCPIndex(resSigCPIndex);
                UTF8CPEntry resSigUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(resSigCPIndex);
                String resSig = resSigUTF8Entry.getValue();
                resourceInfo.setSignature(resSig);
                setCallableUnitSignature(resourceInfo, resSig, packageInfo);

                // Read parameter names
                // TODO Find a better alternative. Storing just param names is like a hack.
                int paramNameCPIndexesCount = dataInStream.readShort();
                int[] paramNameCPIndexes = new int[paramNameCPIndexesCount];
                String[] paramNames = new String[paramNameCPIndexesCount];
                for (int k = 0; k < paramNameCPIndexesCount; k++) {
                    int paramNameCPIndex = dataInStream.readInt();
                    paramNameCPIndexes[k] = paramNameCPIndex;
                    UTF8CPEntry paramNameCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(paramNameCPIndex);
                    paramNames[k] = paramNameCPEntry.getValue();
                }
                resourceInfo.setParamNameCPIndexes(paramNameCPIndexes);
                resourceInfo.setParamNames(paramNames);

                int workerDataChannelsLength = dataInStream.readShort();
                for (int k = 0; k < workerDataChannelsLength; k++) {
                    readWorkerDataChannelEntries(dataInStream, packageInfo, resourceInfo);
                }

                // Read workers
                readWorkerInfoEntries(dataInStream, packageInfo, resourceInfo);

                // Read attributes of the struct info
                readAttributeInfoEntries(dataInStream, packageInfo, resourceInfo);
            }

            // Read attributes of the struct info
            readAttributeInfoEntries(dataInStream, packageInfo, serviceInfo);
        }
    }

    private void readConstantInfoEntries(DataInputStream dataInStream,
                                         PackageInfo packageInfo) throws IOException {
        int constCount = dataInStream.readShort();
        for (int i = 0; i < constCount; i++) {
            PackageVarInfo packageVarInfo = getGlobalVarInfo(dataInStream, packageInfo);
            packageInfo.addConstantInfo(packageVarInfo.getName(), packageVarInfo);
        }
    }

    private void readGlobalVarInfoEntries(DataInputStream dataInStream,
                                          PackageInfo packageInfo) throws IOException {
        int globalVarCount = dataInStream.readShort();
        for (int i = 0; i < globalVarCount; i++) {
            PackageVarInfo packageVarInfo = getGlobalVarInfo(dataInStream, packageInfo);
            packageInfo.addPackageVarInfo(packageVarInfo.getName(), packageVarInfo);
        }
    }

    private PackageVarInfo getGlobalVarInfo(DataInputStream dataInStream,
                                            ConstantPool constantPool) throws IOException {
        // Read variable name;
        int nameCPIndex = dataInStream.readInt();
        UTF8CPEntry nameUTF8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(nameCPIndex);

        // Read variable type;
        int sigCPIndex = dataInStream.readInt();
        UTF8CPEntry sigUTF8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(sigCPIndex);

        int globalMemIndex = dataInStream.readInt();

        PackageVarInfo packageVarInfo = new PackageVarInfo(nameCPIndex, nameUTF8CPEntry.getValue(),
                sigCPIndex, sigUTF8CPEntry.getValue(), globalMemIndex);

        // Read attributes
        readAttributeInfoEntries(dataInStream, constantPool, packageVarInfo);
        return packageVarInfo;
    }

    private void readFunctionInfoEntries(DataInputStream dataInStream,
                                         PackageInfo packageInfo) throws IOException {
        int funcCount = dataInStream.readShort();
        for (int i = 0; i < funcCount; i++) {
            readFunctionInfo(dataInStream, packageInfo);
        }

        packageInfo.setInitFunctionInfo(packageInfo.getFunctionInfo(packageInfo.getPkgPath() + INIT_FUNCTION_SUFFIX));
        packageInfo.setStartFunctionInfo(packageInfo.getFunctionInfo(packageInfo.getPkgPath() + START_FUNCTION_SUFFIX));
        packageInfo.setStopFunctionInfo(packageInfo.getFunctionInfo(packageInfo.getPkgPath() + STOP_FUNCTION_SUFFIX));

        // TODO Improve this. We should be able to this in a single pass.
        ServiceInfo[] serviceInfoEntries = packageInfo.getServiceInfoEntries();
        for (ServiceInfo serviceInfo : serviceInfoEntries) {
            FunctionInfo serviceIniFuncInfo = packageInfo.getFunctionInfo(
                    serviceInfo.getName() + INIT_FUNCTION_SUFFIX);
            serviceInfo.setInitFunctionInfo(serviceIniFuncInfo);
        }
    }

    private void readFunctionInfo(DataInputStream dataInStream,
                                  PackageInfo packageInfo) throws IOException {
        int funcNameCPIndex = dataInStream.readInt();
        UTF8CPEntry funcNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(funcNameCPIndex);
        String funcName = funcNameUTF8Entry.getValue();
        FunctionInfo functionInfo = new FunctionInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                funcNameCPIndex, funcName);
        functionInfo.setPackageInfo(packageInfo);

        int funcSigCPIndex = dataInStream.readInt();
        UTF8CPEntry funcSigUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(funcSigCPIndex);
        setCallableUnitSignature(functionInfo, funcSigUTF8Entry.getValue(), packageInfo);

        int flags = dataInStream.readInt();
        boolean nativeFunc = Flags.isFlagOn(flags, Flags.NATIVE);
        functionInfo.setNative(nativeFunc);

        String uniqueFuncName;
        boolean attached = Flags.isFlagOn(flags, Flags.ATTACHED);
        if (attached) {
            int attachedToTypeCPIndex = dataInStream.readInt();
            functionInfo.attachedToTypeCPIndex = attachedToTypeCPIndex;
            TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) packageInfo.getCPEntry(attachedToTypeCPIndex);
            functionInfo.attachedToType = typeRefCPEntry.getType();
            uniqueFuncName = AttachedFunctionInfo.getUniqueFuncName(typeRefCPEntry.getType().getName(), funcName);
            packageInfo.addFunctionInfo(uniqueFuncName, functionInfo);

            //Update the attachedFunctionInfo
            if (typeRefCPEntry.getType().getTag() == TypeTags.STRUCT_TAG) {
                BStructType structType = (BStructType) typeRefCPEntry.getType();
                AttachedFunctionInfo attachedFuncInfo = structType.structInfo.funcInfoEntries.get(funcName);
                attachedFuncInfo.functionInfo = functionInfo;
            }
        } else {
            uniqueFuncName = funcName;
            packageInfo.addFunctionInfo(uniqueFuncName, functionInfo);
        }

        // Read and ignore the workerData length
        dataInStream.readInt();
        int workerDataChannelsLength = dataInStream.readShort();
        for (int i = 0; i < workerDataChannelsLength; i++) {
            readWorkerDataChannelEntries(dataInStream, packageInfo, functionInfo);
        }

        // Read worker info entries
        readWorkerInfoEntries(dataInStream, packageInfo, functionInfo);

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
        readAttributeInfoEntries(dataInStream, packageInfo, functionInfo);
    }

    private void readTransformerInfoEntries(DataInputStream dataInStream, PackageInfo packageInfo) throws IOException {
        int transformerCount = dataInStream.readShort();
        for (int i = 0; i < transformerCount; i++) {
            readTransformerInfo(dataInStream, packageInfo);
        }
    }

    private void readTransformerInfo(DataInputStream dataInStream, PackageInfo packageInfo) throws IOException {
        int transformerNameCPIndex = dataInStream.readInt();
        UTF8CPEntry transformerNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(transformerNameCPIndex);
        TransformerInfo transformerInfo = new TransformerInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                transformerNameCPIndex, transformerNameUTF8Entry.getValue());
        transformerInfo.setPackageInfo(packageInfo);
        packageInfo.addTransformerInfo(transformerNameUTF8Entry.getValue(), transformerInfo);

        int transformerSigCPIndex = dataInStream.readInt();
        UTF8CPEntry transformerSigUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(transformerSigCPIndex);
        setCallableUnitSignature(transformerInfo, transformerSigUTF8Entry.getValue(), packageInfo);

        boolean nativeFunc = Flags.isFlagOn(dataInStream.readInt(), Flags.NATIVE);
        transformerInfo.setNative(nativeFunc);

        int workerDataChannelsLength = dataInStream.readShort();
        for (int i = 0; i < workerDataChannelsLength; i++) {
            readWorkerDataChannelEntries(dataInStream, packageInfo, transformerInfo);
        }

        // Read worker info entries
        readWorkerInfoEntries(dataInStream, packageInfo, transformerInfo);

        // Read attributes
        readAttributeInfoEntries(dataInStream, packageInfo, transformerInfo);
    }

    public void readWorkerDataChannelEntries(DataInputStream dataInputStream, PackageInfo packageInfo,
                                             CallableUnitInfo callableUnitInfo) throws IOException {
        int sourceCPIndex = dataInputStream.readInt();
        int targetCPIndex = dataInputStream.readInt();

        UTF8CPEntry sourceCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(sourceCPIndex);
        UTF8CPEntry targetCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(targetCPIndex);

        WorkerDataChannelInfo workerDataChannelInfo = new WorkerDataChannelInfo(sourceCPIndex,
                sourceCPEntry.getValue(), targetCPIndex, targetCPEntry.getValue());

        int dataChannelRefCPIndex = dataInputStream.readInt();
        WorkerDataChannelRefCPEntry refCPEntry = (WorkerDataChannelRefCPEntry) packageInfo
                .getCPEntry(dataChannelRefCPIndex);
        refCPEntry.setWorkerDataChannelInfo(workerDataChannelInfo);
        callableUnitInfo.addWorkerDataChannelInfo(workerDataChannelInfo);
    }

    private void setCallableUnitSignature(CallableUnitInfo callableUnitInfo, String sig, PackageInfo packageInfo) {
        BFunctionType funcType = getFunctionType(sig, packageInfo);
        callableUnitInfo.setParamTypes(funcType.paramTypes);
        callableUnitInfo.setRetParamTypes(funcType.retParamTypes);
    }

    private BFunctionType getFunctionType(String sig, PackageInfo packageInfo) {
        int indexOfSep = sig.indexOf(")(");
        String paramSig = sig.substring(1, indexOfSep);
        String retParamSig = sig.substring(indexOfSep + 2, sig.length() - 1);

        BType[] paramTypes = getParamTypes(paramSig, packageInfo);
        BType[] retParamTypes = getParamTypes(retParamSig, packageInfo);
        return new BFunctionType(paramTypes, retParamTypes);
    }

    private BType[] getParamTypes(String signature, PackageInfo packageInfo) {
        int index = 0;
        Stack<BType> typeStack = new Stack<>();
        char[] chars = signature.toCharArray();
        while (index < chars.length) {
            index = createBTypeFromSig(chars, index, typeStack, packageInfo);
        }

        return typeStack.toArray(new BType[0]);
    }

    private int createBTypeFromSig(char[] chars, int index, Stack<BType> typeStack, PackageInfo packageInfo) {
        int nameIndex;
        char ch = chars[index];
        switch (ch) {
            case 'I':
                typeStack.push(BTypes.typeInt);
                return index + 1;
            case 'F':
                typeStack.push(BTypes.typeFloat);
                return index + 1;
            case 'S':
                typeStack.push(BTypes.typeString);
                return index + 1;
            case 'B':
                typeStack.push(BTypes.typeBoolean);
                return index + 1;
            case 'L':
                typeStack.push(BTypes.typeBlob);
                return index + 1;
            case 'Y':
                typeStack.push(BTypes.typeDesc);
                return index + 1;
            case 'A':
                typeStack.push(BTypes.typeAny);
                return index + 1;
            case 'R':
                // TODO Improve this logic
                index++;
                nameIndex = index;
                while (chars[nameIndex] != ';') {
                    nameIndex++;
                }
                String typeName = new String(Arrays.copyOfRange(chars, index, nameIndex));
                typeStack.push(BTypes.getTypeFromName(typeName));
                return nameIndex + 1;
            case 'C':
            case 'J':
            case 'T':
            case 'E':
            case 'D':
            case 'G':
            case 'H':
            case 'Z':
                char typeChar = chars[index];
                // TODO Improve this logic
                index++;
                nameIndex = index;
                int colonIndex = -1;
                while (chars[nameIndex] != ';') {
                    if (chars[nameIndex] == ':') {
                        colonIndex = nameIndex;
                    }
                    nameIndex++;
                }

                String pkgPath;
                String name;
                PackageInfo packageInfoOfType;
                if (colonIndex != -1) {
                    pkgPath = new String(Arrays.copyOfRange(chars, index, colonIndex));
                    name = new String(Arrays.copyOfRange(chars, colonIndex + 1, nameIndex));
                    packageInfoOfType = programFile.getPackageInfo(pkgPath);
                } else {
                    name = new String(Arrays.copyOfRange(chars, index, nameIndex));
                    // Setting the current package;
                    packageInfoOfType = packageInfo;
                }

                if (typeChar == 'J') {
                    if (name.isEmpty()) {
                        typeStack.push(BTypes.typeJSON);
                    } else {
                        typeStack.push(new BJSONType(packageInfoOfType.getStructInfo(name).getType()));
                    }
                } else if (typeChar == 'D') {
                    if (name.isEmpty()) {
                        typeStack.push(BTypes.typeTable);
                    } else {
                        typeStack.push(new BTableType(packageInfoOfType.getStructInfo(name).getType()));
                    }
                } else if (typeChar == 'H') {
                    if (name.isEmpty()) {
                        typeStack.push(BTypes.typeStream);
                    } else {
                        typeStack.push(new BStreamType(packageInfoOfType.getStructInfo(name).getType()));
                    }
                } else if (typeChar == 'G') {
                    typeStack.push(packageInfoOfType.getTypeDefinitionInfo(name).getType());
                } else {
                    // This is a struct type
                    typeStack.push(packageInfoOfType.getStructInfo(name).getType());
                }

                return nameIndex + 1;
            case '[':
                index = createBTypeFromSig(chars, index + 1, typeStack, packageInfo);
                BType elemType = typeStack.pop();
                BArrayType arrayType = new BArrayType(elemType);
                typeStack.push(arrayType);
                return index;
            case 'M':
                index = createBTypeFromSig(chars, index + 1, typeStack, packageInfo);
                BType constrainedType = typeStack.pop();
                BType mapType;
                if (constrainedType == BTypes.typeAny) {
                    mapType = BTypes.typeMap;
                } else {
                    mapType = new BMapType(constrainedType);
                }
                typeStack.push(mapType);
                return index;
            case 'U':
                // TODO : Fix this for type casting.
                typeStack.push(new BFunctionType());
                return index + 1;
            case 'O':
            case 'P':
                typeChar = chars[index];
                index++;
                nameIndex = index;
                while (chars[nameIndex] != ';') {
                    nameIndex++;
                }
                List<BType> memberTypes = new ArrayList<>();
                int memberCount = Integer.parseInt(new String(Arrays.copyOfRange(chars, index, nameIndex)));
                index = nameIndex;
                for (int i = 0; i < memberCount; i++) {
                    index = createBTypeFromSig(chars, index + 1, typeStack, packageInfo) - 1;
                    memberTypes.add(typeStack.pop());
                }
                if (typeChar == 'O') {
                    typeStack.push(new BUnionType(memberTypes));
                } else if (typeChar == 'P') {
                    typeStack.push(new BTupleType(memberTypes));
                }
                return index + 1;
            case 'N':
                typeStack.push(BTypes.typeNull);
                return index + 1;
            default:
                throw new ProgramFileFormatException("unsupported base type char: " + ch);
        }
    }

    private BType getBTypeFromDescriptor(String desc) {
        char ch = desc.charAt(0);
        switch (ch) {
            case 'I':
                return BTypes.typeInt;
            case 'F':
                return BTypes.typeFloat;
            case 'S':
                return BTypes.typeString;
            case 'B':
                return BTypes.typeBoolean;
            case 'Y':
                return BTypes.typeDesc;
            case 'L':
                return BTypes.typeBlob;
            case 'A':
                return BTypes.typeAny;
            case 'R':
                return BTypes.getTypeFromName(desc.substring(1, desc.length() - 1));
            case 'M':
                BType constrainedType = getBTypeFromDescriptor(desc.substring(1));
                if (constrainedType == BTypes.typeAny) {
                    return BTypes.typeMap;
                } else {
                    return new BMapType(constrainedType);
                }
            case 'C':
            case 'X':
            case 'J':
            case 'T':
            case 'E':
            case 'H':
            case 'Z':
            case 'G':
            case 'D':
                String typeName = desc.substring(1, desc.length() - 1);
                String[] parts = typeName.split(":");

                if (parts.length == 1) {
                    if (ch == 'J') {
                        return BTypes.typeJSON;
                    } else if (ch == 'D') {
                        return BTypes.typeTable;
                    } else if (ch == 'H') { //TODO:CHECK
                        return BTypes.typeStream;
                    }
                }

                String pkgPath = parts[0];
                String name = parts[1];
                PackageInfo packageInfoOfType = programFile.getPackageInfo(pkgPath);
                if (ch == 'J') {
                    return new BJSONType(packageInfoOfType.getStructInfo(name).getType());
                } else if (ch == 'X') {
                    return packageInfoOfType.getServiceInfo(name).getType();
                } else if (ch == 'D') {
                    return new BTableType(packageInfoOfType.getStructInfo(name).getType());
                } else if (ch == 'H') {
                    return new BStreamType(packageInfoOfType.getStructInfo(name).getType());
                } else if (ch == 'G') {
                    return packageInfoOfType.getTypeDefinitionInfo(name).getType();
                } else {
                    return packageInfoOfType.getStructInfo(name).getType();
                }
            case '[':
                BType elemType = getBTypeFromDescriptor(desc.substring(1));
                return new BArrayType(elemType);
            case 'U':
                // TODO : Fix this for type casting.
                return new BFunctionType();
            case 'O':
            case 'P':
                Stack<BType> typeStack = new Stack<BType>();
                createBTypeFromSig(desc.toCharArray(), 0, typeStack, null);
                return typeStack.pop();
            case 'N':
                return BTypes.typeNull;
            default:
                throw new ProgramFileFormatException("unsupported base type char: " + ch);
        }
    }


    private void readWorkerInfoEntries(DataInputStream dataInStream,
                                       PackageInfo packageInfo,
                                       CallableUnitInfo callableUnitInfo) throws IOException {

        int workerCount = dataInStream.readShort();
        // First worker is always the default worker.
        WorkerInfo defaultWorker = getWorkerInfo(dataInStream, packageInfo);
        callableUnitInfo.setDefaultWorkerInfo(defaultWorker);

        for (int i = 0; i < workerCount - 1; i++) {
            WorkerInfo workerInfo = getWorkerInfo(dataInStream, packageInfo);
            callableUnitInfo.addWorkerInfo(workerInfo.getWorkerName(), workerInfo);
        }
    }

    private WorkerInfo getWorkerInfo(DataInputStream dataInStream,
                                     PackageInfo packageInfo) throws IOException {
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

        readForkJoinInfo(dataInStream, packageInfo, workerInfo);
        // Read attributes
        readAttributeInfoEntries(dataInStream, packageInfo, workerInfo);
        CodeAttributeInfo codeAttribute = (CodeAttributeInfo) workerInfo.getAttributeInfo(
                AttributeInfo.Kind.CODE_ATTRIBUTE);
        workerInfo.setCodeAttributeInfo(codeAttribute);
        return workerInfo;
    }

    private void readForkJoinInfo(DataInputStream dataInStream,
                                  PackageInfo packageInfo, WorkerInfo workerInfo) throws IOException {
        int forkJoinCount = dataInStream.readShort();
        ForkjoinInfo[] forkjoinInfos = new ForkjoinInfo[forkJoinCount];
        for (int i = 0; i < forkJoinCount; i++) {
            ForkjoinInfo forkjoinInfo = getForkJoinInfo(dataInStream, packageInfo);
            forkjoinInfos[i] = forkjoinInfo;
        }
        workerInfo.setForkjoinInfos(forkjoinInfos);
    }

    private ForkjoinInfo getForkJoinInfo(DataInputStream dataInStream, PackageInfo packageInfo) throws IOException {
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
            WorkerInfo workerInfo = getWorkerInfo(dataInStream, packageInfo);
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


    private void readAttributeInfoEntries(DataInputStream dataInStream,
                                          ConstantPool constantPool,
                                          AttributeInfoPool attributeInfoPool) throws IOException {
        int attributesCount = dataInStream.readShort();
        for (int k = 0; k < attributesCount; k++) {
            AttributeInfo attributeInfo = getAttributeInfo(dataInStream, constantPool);
            attributeInfoPool.addAttributeInfo(attributeInfo.getKind(), attributeInfo);
        }
    }

    private AttributeInfo getAttributeInfo(DataInputStream dataInStream,
                                           ConstantPool constantPool) throws IOException {
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
                codeAttributeInfo.setMaxByteLocalVars(dataInStream.readShort());
                codeAttributeInfo.setMaxRefLocalVars(dataInStream.readShort());

                codeAttributeInfo.setMaxLongRegs(dataInStream.readShort());
                codeAttributeInfo.setMaxDoubleRegs(dataInStream.readShort());
                codeAttributeInfo.setMaxStringRegs(dataInStream.readShort());
                codeAttributeInfo.setMaxIntRegs(dataInStream.readShort());
                codeAttributeInfo.setMaxByteRegs(dataInStream.readShort());
                codeAttributeInfo.setMaxRefRegs(dataInStream.readShort());
                return codeAttributeInfo;

            case VARIABLE_TYPE_COUNT_ATTRIBUTE:
                VarTypeCountAttributeInfo varCountAttributeInfo =
                        new VarTypeCountAttributeInfo(attribNameCPIndex);
                varCountAttributeInfo.setMaxLongVars(dataInStream.readShort());
                varCountAttributeInfo.setMaxDoubleVars(dataInStream.readShort());
                varCountAttributeInfo.setMaxStringVars(dataInStream.readShort());
                varCountAttributeInfo.setMaxIntVars(dataInStream.readShort());
                varCountAttributeInfo.setMaxByteVars(dataInStream.readShort());
                varCountAttributeInfo.setMaxRefVars(dataInStream.readShort());
                return varCountAttributeInfo;

            case ERROR_TABLE:
                ErrorTableAttributeInfo tableAttributeInfo = new ErrorTableAttributeInfo(attribNameCPIndex);
                int tableEntryCount = dataInStream.readShort();
                for (int i = 0; i < tableEntryCount; i++) {
                    int ipFrom = dataInStream.readInt();
                    int ipTo = dataInStream.readInt();
                    int ipTarget = dataInStream.readInt();
                    int priority = dataInStream.readInt();
                    int errorStructCPIndex = dataInStream.readInt();
                    ErrorTableEntry tableEntry = new ErrorTableEntry(ipFrom, ipTo,
                            ipTarget, priority, errorStructCPIndex);

                    if (errorStructCPIndex != -1) {
                        StructureRefCPEntry structureRefCPEntry = (StructureRefCPEntry)
                                constantPool.getCPEntry(errorStructCPIndex);
                        tableEntry.setError((StructInfo) structureRefCPEntry.getStructureTypeInfo());
                    }
                    tableAttributeInfo.addErrorTableEntry(tableEntry);
                }
                return tableAttributeInfo;

            case LOCAL_VARIABLES_ATTRIBUTE:
                LocalVariableAttributeInfo localVarAttrInfo = new LocalVariableAttributeInfo(attribNameCPIndex);
                int localVarInfoCount = dataInStream.readShort();
                for (int i = 0; i < localVarInfoCount; i++) {
                    LocalVariableInfo localVariableInfo = getLocalVariableInfo(dataInStream, constantPool);
                    localVarAttrInfo.addLocalVarInfo(localVariableInfo);
                }
                return localVarAttrInfo;
            case LINE_NUMBER_TABLE_ATTRIBUTE:
                LineNumberTableAttributeInfo lnNoTblAttrInfo = new LineNumberTableAttributeInfo(attribNameCPIndex);
                int lineNoInfoCount = dataInStream.readShort();
                for (int i = 0; i < lineNoInfoCount; i++) {
                    LineNumberInfo lineNumberInfo = getLineNumberInfo(dataInStream, constantPool);
                    lnNoTblAttrInfo.addLineNumberInfo(lineNumberInfo);
                }
                return lnNoTblAttrInfo;
            case DEFAULT_VALUE_ATTRIBUTE:
                DefaultValue defaultValue = getDefaultValue(dataInStream, constantPool);
                DefaultValueAttributeInfo defaultValAttrInfo =
                        new DefaultValueAttributeInfo(attribNameCPIndex, defaultValue);
                return defaultValAttrInfo;
            case PARAMETER_DEFAULTS_ATTRIBUTE:
                ParamDefaultValueAttributeInfo paramDefaultValAttrInfo =
                        new ParamDefaultValueAttributeInfo(attribNameCPIndex);
                int paramDefaultsInfoCount = dataInStream.readShort();
                for (int i = 0; i < paramDefaultsInfoCount; i++) {
                    DefaultValue paramDefaultValue = getDefaultValue(dataInStream, constantPool);
                    paramDefaultValAttrInfo.addParamDefaultValueInfo(paramDefaultValue);
                }
                return paramDefaultValAttrInfo;
            default:
                throw new ProgramFileFormatException("unsupported attribute kind " + attribNameCPEntry.getValue());
        }
    }

    private LocalVariableInfo getLocalVariableInfo(DataInputStream dataInStream,
                                                   ConstantPool constantPool) throws IOException {
        int varNameCPIndex = dataInStream.readInt();
        UTF8CPEntry varNameCPEntry = (UTF8CPEntry) constantPool.getCPEntry(varNameCPIndex);
        int variableIndex = dataInStream.readInt();

        int typeSigCPIndex = dataInStream.readInt();

        UTF8CPEntry typeSigCPEntry = (UTF8CPEntry) constantPool.getCPEntry(typeSigCPIndex);

        BType type = getBTypeFromDescriptor(typeSigCPEntry.getValue());
        LocalVariableInfo localVariableInfo = new LocalVariableInfo(varNameCPEntry.getValue(), varNameCPIndex,
                variableIndex, typeSigCPIndex, type);
        int attchmntIndexesLength = dataInStream.readShort();
        int[] attachmentIndexes = new int[attchmntIndexesLength];
        for (int i = 0; i < attchmntIndexesLength; i++) {
            attachmentIndexes[i] = dataInStream.readInt();
        }
        localVariableInfo.setAttachmentIndexes(attachmentIndexes);

        return localVariableInfo;
    }

    private LineNumberInfo getLineNumberInfo(DataInputStream dataInStream,
                                             ConstantPool constantPool) throws IOException {
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

    private void readInstructions(DataInputStream dataInStream,
                                  PackageInfo packageInfo) throws IOException {
        int codeLength = dataInStream.readInt();
        byte[] code = new byte[codeLength];
        // Ignore bytes read should be same as the code length.
        dataInStream.read(code);
        DataInputStream codeStream = new DataInputStream(new ByteArrayInputStream(code));
        while (codeStream.available() > 0) {
            int i, j, k, h;
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
                case InstructionCodes.THROW:
                case InstructionCodes.ERRSTORE:
                case InstructionCodes.NEWXMLSEQ:
                    i = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, i));
                    break;

                case InstructionCodes.FPLOAD: {
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    k = codeStream.readInt();
                    int[] operands = new int[3 + (k * 2)];
                    operands[0] = i;
                    operands[1] = j;
                    operands[2] = k;
                    for (int x = 0; x < (k * 2); x++) {
                        operands[x + 3] = codeStream.readInt();
                    }
                    packageInfo.addInstruction(InstructionFactory.get(opcode, operands));
                    break;
                }
                case InstructionCodes.ICONST:
                case InstructionCodes.FCONST:
                case InstructionCodes.SCONST:
                case InstructionCodes.IMOVE:
                case InstructionCodes.FMOVE:
                case InstructionCodes.SMOVE:
                case InstructionCodes.BMOVE:
                case InstructionCodes.LMOVE:
                case InstructionCodes.RMOVE:
                case InstructionCodes.INEG:
                case InstructionCodes.FNEG:
                case InstructionCodes.BNOT:
                case InstructionCodes.REQ_NULL:
                case InstructionCodes.RNE_NULL:
                case InstructionCodes.BR_TRUE:
                case InstructionCodes.BR_FALSE:
                case InstructionCodes.TR_END:
                case InstructionCodes.ARRAYLEN:
                case InstructionCodes.INEWARRAY:
                case InstructionCodes.FNEWARRAY:
                case InstructionCodes.SNEWARRAY:
                case InstructionCodes.BNEWARRAY:
                case InstructionCodes.LNEWARRAY:
                case InstructionCodes.RNEWARRAY:
                case InstructionCodes.JSONNEWARRAY:
                case InstructionCodes.NEWSTRUCT:
                case InstructionCodes.ITR_NEW:
                case InstructionCodes.ITR_HAS_NEXT:
                case InstructionCodes.IRET:
                case InstructionCodes.FRET:
                case InstructionCodes.SRET:
                case InstructionCodes.BRET:
                case InstructionCodes.LRET:
                case InstructionCodes.RRET:
                case InstructionCodes.XML2XMLATTRS:
                case InstructionCodes.NEWXMLCOMMENT:
                case InstructionCodes.NEWXMLTEXT:
                case InstructionCodes.XMLSEQSTORE:
                case InstructionCodes.TYPEOF:
                case InstructionCodes.TYPELOAD:
                case InstructionCodes.SEQ_NULL:
                case InstructionCodes.SNE_NULL:
                case InstructionCodes.NEWJSON:
                case InstructionCodes.NEWMAP:
                case InstructionCodes.I2ANY:
                case InstructionCodes.F2ANY:
                case InstructionCodes.S2ANY:
                case InstructionCodes.B2ANY:
                case InstructionCodes.L2ANY:
                case InstructionCodes.ANY2I:
                case InstructionCodes.ANY2F:
                case InstructionCodes.ANY2S:
                case InstructionCodes.ANY2B:
                case InstructionCodes.ANY2L:
                case InstructionCodes.ANY2JSON:
                case InstructionCodes.ANY2XML:
                case InstructionCodes.ANY2MAP:
                case InstructionCodes.ANY2TYPE:
                case InstructionCodes.ANY2DT:
                case InstructionCodes.NULL2JSON:
                case InstructionCodes.I2F:
                case InstructionCodes.I2S:
                case InstructionCodes.I2B:
                case InstructionCodes.I2JSON:
                case InstructionCodes.F2I:
                case InstructionCodes.F2S:
                case InstructionCodes.F2B:
                case InstructionCodes.F2JSON:
                case InstructionCodes.S2I:
                case InstructionCodes.S2F:
                case InstructionCodes.S2B:
                case InstructionCodes.S2JSON:
                case InstructionCodes.B2I:
                case InstructionCodes.B2F:
                case InstructionCodes.B2S:
                case InstructionCodes.B2JSON:
                case InstructionCodes.JSON2I:
                case InstructionCodes.JSON2F:
                case InstructionCodes.JSON2S:
                case InstructionCodes.JSON2B:
                case InstructionCodes.DT2XML:
                case InstructionCodes.DT2JSON:
                case InstructionCodes.T2MAP:
                case InstructionCodes.XML2JSON:
                case InstructionCodes.JSON2XML:
                case InstructionCodes.XMLATTRS2MAP:
                case InstructionCodes.ANY2SCONV:
                case InstructionCodes.S2XML:
                case InstructionCodes.XML2S:
                case InstructionCodes.S2JSONX:
                case InstructionCodes.NULL2S:
                case InstructionCodes.AWAIT:
                case InstructionCodes.CHECK_CONVERSION:
                case InstructionCodes.XMLLOADALL:
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, i, j));
                    break;

                case InstructionCodes.IALOAD:
                case InstructionCodes.FALOAD:
                case InstructionCodes.SALOAD:
                case InstructionCodes.BALOAD:
                case InstructionCodes.LALOAD:
                case InstructionCodes.RALOAD:
                case InstructionCodes.JSONALOAD:
                case InstructionCodes.IASTORE:
                case InstructionCodes.FASTORE:
                case InstructionCodes.SASTORE:
                case InstructionCodes.BASTORE:
                case InstructionCodes.LASTORE:
                case InstructionCodes.RASTORE:
                case InstructionCodes.JSONASTORE:
                case InstructionCodes.IFIELDLOAD:
                case InstructionCodes.FFIELDLOAD:
                case InstructionCodes.SFIELDLOAD:
                case InstructionCodes.BFIELDLOAD:
                case InstructionCodes.LFIELDLOAD:
                case InstructionCodes.RFIELDLOAD:
                case InstructionCodes.IFIELDSTORE:
                case InstructionCodes.FFIELDSTORE:
                case InstructionCodes.SFIELDSTORE:
                case InstructionCodes.BFIELDSTORE:
                case InstructionCodes.LFIELDSTORE:
                case InstructionCodes.RFIELDSTORE:
                case InstructionCodes.MAPLOAD:
                case InstructionCodes.MAPSTORE:
                case InstructionCodes.JSONLOAD:
                case InstructionCodes.JSONSTORE:
                case InstructionCodes.IADD:
                case InstructionCodes.FADD:
                case InstructionCodes.SADD:
                case InstructionCodes.XMLADD:
                case InstructionCodes.ISUB:
                case InstructionCodes.FSUB:
                case InstructionCodes.IMUL:
                case InstructionCodes.FMUL:
                case InstructionCodes.IDIV:
                case InstructionCodes.FDIV:
                case InstructionCodes.IMOD:
                case InstructionCodes.FMOD:
                case InstructionCodes.IEQ:
                case InstructionCodes.FEQ:
                case InstructionCodes.SEQ:
                case InstructionCodes.BEQ:
                case InstructionCodes.REQ:
                case InstructionCodes.INE:
                case InstructionCodes.FNE:
                case InstructionCodes.SNE:
                case InstructionCodes.BNE:
                case InstructionCodes.RNE:
                case InstructionCodes.IGT:
                case InstructionCodes.FGT:
                case InstructionCodes.IGE:
                case InstructionCodes.FGE:
                case InstructionCodes.ILT:
                case InstructionCodes.FLT:
                case InstructionCodes.ILE:
                case InstructionCodes.FLE:
                case InstructionCodes.XMLATTRLOAD:
                case InstructionCodes.XMLATTRSTORE:
                case InstructionCodes.S2QNAME:
                case InstructionCodes.NEWXMLPI:
                case InstructionCodes.TEQ:
                case InstructionCodes.TNE:
                case InstructionCodes.XMLLOAD:
                case InstructionCodes.NEW_INT_RANGE:
                case InstructionCodes.LENGTHOF:
                case InstructionCodes.NEWSTREAM:
                case InstructionCodes.CHECKCAST:
                case InstructionCodes.MAP2T:
                case InstructionCodes.JSON2T:
                case InstructionCodes.ANY2T:
                case InstructionCodes.ANY2C:
                case InstructionCodes.ANY2E:
                case InstructionCodes.IS_ASSIGNABLE:
                case InstructionCodes.TR_RETRY:
                case InstructionCodes.XMLSEQLOAD:
                case InstructionCodes.NEWTABLE:
                case InstructionCodes.T2JSON:
                case InstructionCodes.MAP2JSON:
                case InstructionCodes.JSON2MAP:
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    k = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, i, j, k));
                    break;
                case InstructionCodes.NEWQNAME:
                case InstructionCodes.NEWXMLELEMENT:
                case InstructionCodes.TR_BEGIN:
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    k = codeStream.readInt();
                    h = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, i, j, k, h));
                    break;

                case InstructionCodes.IGLOAD:
                case InstructionCodes.FGLOAD:
                case InstructionCodes.SGLOAD:
                case InstructionCodes.BGLOAD:
                case InstructionCodes.LGLOAD:
                case InstructionCodes.RGLOAD:
                case InstructionCodes.IGSTORE:
                case InstructionCodes.FGSTORE:
                case InstructionCodes.SGSTORE:
                case InstructionCodes.BGSTORE:
                case InstructionCodes.LGSTORE:
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
                case InstructionCodes.TCALL:
                    int transformCPIndex = codeStream.readInt();
                    flags = codeStream.readInt();
                    TransformerRefCPEntry transformerRefCPEntry =
                            (TransformerRefCPEntry) packageInfo.getCPEntry(transformCPIndex);
                    packageInfo.addInstruction(new InstructionTCALL(opcode, transformCPIndex,
                            transformerRefCPEntry.getTransformerInfo(), flags,
                            getArgRegs(codeStream), getArgRegs(codeStream)));
                    break;
                case InstructionCodes.WRKSEND:
                case InstructionCodes.WRKRECEIVE:
                    int channelRefCPIndex = codeStream.readInt();
                    WorkerDataChannelRefCPEntry channelRefCPEntry = (WorkerDataChannelRefCPEntry)
                            packageInfo.getCPEntry(channelRefCPIndex);
                    int sigCPIndex = codeStream.readInt();
                    UTF8CPEntry sigCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(sigCPIndex);
                    BType bType = getParamTypes(sigCPEntry.getValue(), packageInfo)[0];
                    packageInfo.addInstruction(new InstructionWRKSendReceive(opcode, channelRefCPIndex,
                            channelRefCPEntry.getWorkerDataChannelInfo(), sigCPIndex, bType, codeStream.readInt()));
                    break;
                case InstructionCodes.FORKJOIN:
                    int forkJoinIndexCPIndex = codeStream.readInt();
                    ForkJoinCPEntry forkJoinIndexCPEntry =
                            (ForkJoinCPEntry) packageInfo.getCPEntry(forkJoinIndexCPIndex);
                    int timeoutRegIndex = codeStream.readInt();
                    int joinVarRegIndex = codeStream.readInt();
                    int joinBlockAddr = codeStream.readInt();
                    int timeoutVarRegIndex = codeStream.readInt();
                    int timeoutBlockAddr = codeStream.readInt();
                    packageInfo.addInstruction(new InstructionFORKJOIN(opcode, forkJoinIndexCPIndex,
                            forkJoinIndexCPEntry, timeoutRegIndex, joinVarRegIndex, joinBlockAddr,
                            timeoutVarRegIndex, timeoutBlockAddr));
                    break;
                case InstructionCodes.ITR_NEXT:
                    int iteratorIndex = codeStream.readInt();
                    int[] typeTags = getArgRegs(codeStream);
                    retRegs = getArgRegs(codeStream);
                    packageInfo.addInstruction(new InstructionIteratorNext(opcode, iteratorIndex, retRegs.length,
                            typeTags, retRegs));
                    break;
                case InstructionCodes.LOCK:
                case InstructionCodes.UNLOCK:
                    int varCount = codeStream.readInt();
                    BType[] varTypes = new BType[varCount];
                    int[] varRegs = new int[varCount];
                    for (int m = 0; m < varCount; m++) {
                        int varSigCPIndex = codeStream.readInt();
                        TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) packageInfo.getCPEntry(varSigCPIndex);
                        varTypes[m] = typeRefCPEntry.getType();
                        varRegs[m] = codeStream.readInt();
                    }
                    packageInfo.addInstruction(new InstructionLock(opcode, varTypes, varRegs));
                    break;
                default:
                    throw new ProgramFileFormatException("unknown opcode " + opcode +
                            " in package " + packageInfo.getPkgPath());
            }
        }
    }

    private void resolveCPEntries() {
        for (ConstantPoolEntry cpEntry : unresolvedCPEntries) {
            PackageInfo packageInfo;
            StructureRefCPEntry structureRefCPEntry;
            switch (cpEntry.getEntryType()) {
                case CP_ENTRY_PACKAGE:
                    PackageRefCPEntry pkgRefCPEntry = (PackageRefCPEntry) cpEntry;
                    packageInfo = programFile.getPackageInfo(pkgRefCPEntry.getPackageName());
                    pkgRefCPEntry.setPackageInfo(packageInfo);
                    break;
                case CP_ENTRY_FUNCTION_REF:
                    FunctionRefCPEntry funcRefCPEntry = (FunctionRefCPEntry) cpEntry;
                    packageInfo = programFile.getPackageInfo(funcRefCPEntry.getPackagePath());
                    FunctionInfo functionInfo = packageInfo.getFunctionInfo(funcRefCPEntry.getFunctionName());
                    funcRefCPEntry.setFunctionInfo(functionInfo);
                    break;
                case CP_ENTRY_STRUCTURE_REF:
                    structureRefCPEntry = (StructureRefCPEntry) cpEntry;
                    packageInfo = programFile.getPackageInfo(structureRefCPEntry.getPackagePath());
                    StructureTypeInfo structureTypeInfo = packageInfo.getStructureTypeInfo(
                            structureRefCPEntry.getStructureName());
                    structureRefCPEntry.setStructureTypeInfo(structureTypeInfo);
                    break;
                case CP_ENTRY_TYPE_REF:
                    TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) cpEntry;
                    String typeSig = typeRefCPEntry.getTypeSig();
                    BType bType = getBTypeFromDescriptor(typeSig);
                    typeRefCPEntry.setType(bType);
                    break;
                case CP_ENTRY_TRANSFORMER_REF:
                    TransformerRefCPEntry transformerRefCPEntry = (TransformerRefCPEntry) cpEntry;
                    packageInfo = programFile.getPackageInfo(transformerRefCPEntry.getPackagePath());
                    TransformerInfo transformerInfo =
                            packageInfo.getTransformerInfo(transformerRefCPEntry.getTransformerName());
                    transformerRefCPEntry.setTransformerInfo(transformerInfo);
                    break;
                default:
                    break;
            }
        }
    }

    private void resolveUserDefinedTypes(PackageInfo packageInfo) {
        // TODO Improve this. We should be able to this in a single pass.
        StructInfo[] structInfoEntries = packageInfo.getStructInfoEntries();
        for (StructInfo structInfo : structInfoEntries) {
            StructFieldInfo[] fieldInfoEntries = structInfo.getFieldInfoEntries();

            BStructType structType = structInfo.getType();
            BStructType.StructField[] structFields = new BStructType.StructField[fieldInfoEntries.length];
            for (int i = 0; i < fieldInfoEntries.length; i++) {
                // Get the BType from the type descriptor
                StructFieldInfo fieldInfo = fieldInfoEntries[i];
                String typeDesc = fieldInfo.getTypeDescriptor();
                BType fieldType = getBTypeFromDescriptor(typeDesc);
                fieldInfo.setFieldType(fieldType);

                // Create the StructField in the BStructType. This is required for the type equivalence algorithm
                BStructType.StructField structField = new BStructType.StructField(fieldType,
                        fieldInfo.getName(), fieldInfo.flags);
                structFields[i] = structField;
            }

            VarTypeCountAttributeInfo attributeInfo = (VarTypeCountAttributeInfo)
                    structInfo.getAttributeInfo(AttributeInfo.Kind.VARIABLE_TYPE_COUNT_ATTRIBUTE);
            structType.setFieldTypeCount(attributeInfo.getVarTypeCount());
            structType.setStructFields(structFields);

            // Resolve attached function signature
            int attachedFuncCount = structInfo.funcInfoEntries.size();
            BStructType.AttachedFunction[] attachedFunctions = new BStructType.AttachedFunction[attachedFuncCount];
            int count = 0;
            for (AttachedFunctionInfo attachedFuncInfo : structInfo.funcInfoEntries.values()) {
                BFunctionType funcType = getFunctionType(attachedFuncInfo.typeSignature, packageInfo);

                BStructType.AttachedFunction attachedFunction = new BStructType.AttachedFunction(
                        attachedFuncInfo.name, funcType, attachedFuncInfo.flags);
                attachedFunctions[count++] = attachedFunction;
                if (structInfo.initializer == attachedFuncInfo) {
                    structType.initializer = attachedFunction;
                } else if (structInfo.defaultsValuesInitFunc == attachedFuncInfo) {
                    structType.defaultsValuesInitFunc = attachedFunction;
                }
            }
            structType.setAttachedFunctions(attachedFunctions);
        }

        for (ConstantPoolEntry cpEntry : unresolvedCPEntries) {
            switch (cpEntry.getEntryType()) {
                case CP_ENTRY_TYPE_REF:
                    TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) cpEntry;
                    String typeSig = typeRefCPEntry.getTypeSig();
                    BType bType = getBTypeFromDescriptor(typeSig);
                    typeRefCPEntry.setType(bType);
                    break;
                default:
                    break;
            }
        }
    }

    private DefaultValue getDefaultValue(DataInputStream dataInStream, ConstantPool constantPool)
            throws IOException {
        DefaultValue defaultValue;
        int typeDescCPIndex = dataInStream.readInt();
        UTF8CPEntry typeDescCPEntry = (UTF8CPEntry) constantPool.getCPEntry(typeDescCPIndex);
        String typeDesc = typeDescCPEntry.getValue();

        int valueCPIndex;
        switch (typeDesc) {
            case TypeSignature.SIG_BOOLEAN:
                boolean boolValue = dataInStream.readBoolean();
                defaultValue = new DefaultValue(typeDescCPIndex, typeDesc);
                defaultValue.setBooleanValue(boolValue);
                break;
            case TypeSignature.SIG_INT:
                valueCPIndex = dataInStream.readInt();
                IntegerCPEntry integerCPEntry = (IntegerCPEntry) constantPool.getCPEntry(valueCPIndex);
                defaultValue = new DefaultValue(typeDescCPIndex, typeDesc);
                defaultValue.setIntValue(integerCPEntry.getValue());
                break;
            case TypeSignature.SIG_FLOAT:
                valueCPIndex = dataInStream.readInt();
                FloatCPEntry floatCPEntry = (FloatCPEntry) constantPool.getCPEntry(valueCPIndex);
                defaultValue = new DefaultValue(typeDescCPIndex, typeDesc);
                defaultValue.setFloatValue(floatCPEntry.getValue());
                break;
            case TypeSignature.SIG_STRING:
                valueCPIndex = dataInStream.readInt();
                UTF8CPEntry stringCPEntry = (UTF8CPEntry) constantPool.getCPEntry(valueCPIndex);
                defaultValue = new DefaultValue(typeDescCPIndex, typeDesc);
                defaultValue.setStringValue(stringCPEntry.getValue());
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
            case TypeSignature.SIG_FLOAT:
                double floatValue = defaultValue.getFloatValue();
                value = new BFloat(floatValue);
                break;
            case TypeSignature.SIG_STRING:
                String stringValue = defaultValue.getStringValue();
                value = new BString(stringValue);
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
}

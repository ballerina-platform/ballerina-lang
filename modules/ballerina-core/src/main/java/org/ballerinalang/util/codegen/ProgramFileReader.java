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

import org.ballerinalang.model.ActionSymbolName;
import org.ballerinalang.model.FunctionSymbolName;
import org.ballerinalang.model.NativeScope;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BConnectorType;
import org.ballerinalang.model.types.BFunctionType;
import org.ballerinalang.model.types.BJSONConstraintType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeSignature;
import org.ballerinalang.model.util.LangModelUtils;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.NativeUnitProxy;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.util.codegen.attributes.AnnotationAttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfoPool;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.codegen.attributes.DefaultValueAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ErrorTableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LineNumberTableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.codegen.attributes.ParamAnnotationAttributeInfo;
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
import org.ballerinalang.util.codegen.cpentries.TypeRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;
import org.ballerinalang.util.codegen.cpentries.WorkerDataChannelRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.WrkrInteractionArgsCPEntry;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.ballerinalang.util.BLangConstants.INIT_FUNCTION_SUFFIX;
import static org.ballerinalang.util.BLangConstants.MAGIC_NUMBER;
import static org.ballerinalang.util.BLangConstants.VERSION_NUMBER;

/**
 * Reads a Ballerina program from a file.
 *
 * @since 0.90
 */
public class ProgramFileReader {

    private NativeScope nativeScope;
    private ProgramFile programFile;

    public ProgramFileReader() {
        this.nativeScope = NativeScope.getInstance();
    }

    private List<ConstantPoolEntry> unresolvedCPEntries = new ArrayList<>();

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
            readPackageInfo(dataInStream);
        }

        PackageInfo entryPkg = programFile.getPackageInfo(programFile.getEntryPkgName());
        programFile.setEntryPackage(entryPkg);
        entryPkg.setProgramFile(programFile);

        // Read program level attributes
        readAttributeInfoEntries(dataInStream, programFile, programFile);
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
        PackageInfo packageInfo;
        switch (cpEntryType) {
            case CP_ENTRY_UTF8:
                // Discard the length of bytes for now.
                dataInStream.readShort();
                String strValue = dataInStream.readUTF();
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
                return new PackageRefCPEntry(cpIndex, utf8CPEntry.getValue());

            case CP_ENTRY_FUNCTION_REF:
                pkgCPIndex = dataInStream.readInt();
                packageRefCPEntry = (PackageRefCPEntry) constantPool.getCPEntry(pkgCPIndex);

                cpIndex = dataInStream.readInt();
                utf8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(cpIndex);
                String funcName = utf8CPEntry.getValue();
                FunctionRefCPEntry functionRefCPEntry = new FunctionRefCPEntry(pkgCPIndex,
                        packageRefCPEntry.getPackageName(), cpIndex, funcName);

                // Find the functionInfo
                packageInfo = programFile.getPackageInfo(packageRefCPEntry.getPackageName());
                FunctionInfo functionInfo = packageInfo.getFunctionInfo(funcName);
                if (functionInfo == null) {
                    // This must reference to the current package and the current package is not been read yet.
                    // Therefore we add this to the unresolved CP Entry list.
                    unresolvedCPEntries.add(functionRefCPEntry);
                    return functionRefCPEntry;
                }

                functionRefCPEntry.setFunctionInfo(packageInfo.getFunctionInfo(funcName));
                return functionRefCPEntry;

            case CP_ENTRY_ACTION_REF:
                pkgCPIndex = dataInStream.readInt();
                packageRefCPEntry = (PackageRefCPEntry) constantPool.getCPEntry(pkgCPIndex);

                int connectorCPIndex = dataInStream.readInt();
                StructureRefCPEntry connectorRefCPEntry = (StructureRefCPEntry)
                        constantPool.getCPEntry(connectorCPIndex);

                cpIndex = dataInStream.readInt();
                UTF8CPEntry nameCPEntry = (UTF8CPEntry) constantPool.getCPEntry(cpIndex);
                String actionName = nameCPEntry.getValue();
                ActionRefCPEntry actionRefCPEntry = new ActionRefCPEntry(pkgCPIndex, packageRefCPEntry.getPackageName(),
                        connectorCPIndex, connectorRefCPEntry, cpIndex, actionName);

                ConnectorInfo connectorInfo = (ConnectorInfo) connectorRefCPEntry.getStructureTypeInfo();
                if (connectorInfo == null) {
                    // This must reference to the current package and the current package is not been read yet.
                    // Therefore we add this to the unresolved CP Entry list.
                    unresolvedCPEntries.add(actionRefCPEntry);
                    return actionRefCPEntry;
                }

                actionRefCPEntry.setActionInfo(connectorInfo.getActionInfo(actionName));
                return actionRefCPEntry;
            case CP_ENTRY_FUNCTION_CALL_ARGS:
                int argLength = dataInStream.readByte();
                int[] argRegs = new int[argLength];
                for (int i = 0; i < argLength; i++) {
                    argRegs[i] = dataInStream.readInt();
                }

                int retLength = dataInStream.readByte();
                int[] retRegs = new int[retLength];
                for (int i = 0; i < retLength; i++) {
                    retRegs[i] = dataInStream.readInt();
                }

                return new FunctionCallCPEntry(argRegs, retRegs);
            case CP_ENTRY_STRUCTURE_REF:
                pkgCPIndex = dataInStream.readInt();
                packageRefCPEntry = (PackageRefCPEntry) constantPool.getCPEntry(pkgCPIndex);
                cpIndex = dataInStream.readInt();
                utf8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(cpIndex);
                StructureRefCPEntry structureRefCPEntry = new StructureRefCPEntry(pkgCPIndex,
                        packageRefCPEntry.getPackageName(),
                        cpIndex, utf8CPEntry.getValue());

                packageInfo = programFile.getPackageInfo(packageRefCPEntry.getPackageName());
                StructureTypeInfo structureTypeInfo = packageInfo.getStructureTypeInfo(utf8CPEntry.getValue());
                if (structureTypeInfo == null) {
                    // This must reference to the current package and the current package is not been read yet.
                    // Therefore we add this to the unresolved CP Entry list.
                    unresolvedCPEntries.add(structureRefCPEntry);
                    return structureRefCPEntry;
                }

                structureRefCPEntry.setStructureTypeInfo(structureTypeInfo);
                return structureRefCPEntry;
            case CP_ENTRY_TYPE_REF:
                int typeSigCPIndex = dataInStream.readInt();
                utf8CPEntry = (UTF8CPEntry) constantPool.getCPEntry(typeSigCPIndex);
                TypeRefCPEntry typeRefCPEntry = new TypeRefCPEntry(typeSigCPIndex, utf8CPEntry.getValue());
                unresolvedCPEntries.add(typeRefCPEntry);
                return typeRefCPEntry;
            case CP_ENTRY_FORK_JOIN:
                int forkJoinCPIndex = dataInStream.readInt();
                ForkJoinCPEntry forkJoinCPEntry = new ForkJoinCPEntry(forkJoinCPIndex);
                return forkJoinCPEntry;
            case CP_ENTRY_WRKR_INTERACTION:
                int typesSignatureCPIndex = dataInStream.readInt();
                UTF8CPEntry typesSignatureCPEntry = (UTF8CPEntry) constantPool.getCPEntry(typesSignatureCPIndex);
                // When it comes to here, constantPool is always package info
                BType[] bTypes = getParamTypes(typesSignatureCPEntry.getValue(), (PackageInfo) constantPool);
                int workerInvokeArgLength = dataInStream.readByte();
                int[] workerInvokeArgRegs = new int[workerInvokeArgLength];
                for (int i = 0; i < workerInvokeArgLength; i++) {
                    workerInvokeArgRegs[i] = dataInStream.readInt();
                }
                WrkrInteractionArgsCPEntry wrkrInvokeCPEntry
                        = new WrkrInteractionArgsCPEntry(workerInvokeArgRegs, bTypes);
                return wrkrInvokeCPEntry;
            case CP_ENTRY_WRKR_DATA_CHNL_REF:
                int uniqueNameCPIndex = dataInStream.readInt();
                UTF8CPEntry wrkrDtChnlTypesSigCPEntry = (UTF8CPEntry) constantPool
                        .getCPEntry(uniqueNameCPIndex);

                WorkerDataChannelRefCPEntry wrkrDtChnlRefCPEntry
                        = new WorkerDataChannelRefCPEntry(uniqueNameCPIndex, wrkrDtChnlTypesSigCPEntry.getValue());
                return wrkrDtChnlRefCPEntry;
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
        int pkgNameCPIndex = dataInStream.readInt();
        UTF8CPEntry pkgNameCPEntry = (UTF8CPEntry) programFile.getCPEntry(pkgNameCPIndex);
        PackageInfo packageInfo = new PackageInfo(pkgNameCPIndex, pkgNameCPEntry.getValue());
        programFile.addPackageInfo(packageInfo.getPkgPath(), packageInfo);

        // Read constant pool in the package.
        readConstantPool(dataInStream, packageInfo);

        // Read struct info entries
        readStructInfoEntries(dataInStream, packageInfo);

        // Read connector info entries
        readConnectorInfoEntries(dataInStream, packageInfo);

        // Read service info entries
        readServiceInfoEntries(dataInStream, packageInfo);

        // Resolve user-defined type i.e. structs and connectors
        resolveUserDefinedTypes(packageInfo);

        // Read constant info entries
        readConstantInfoEntries(dataInStream, packageInfo);

        // Read global var info entries
        readGlobalVarInfoEntries(dataInStream, packageInfo);

        // Read function info entries in the package
        readFunctionInfoEntries(dataInStream, packageInfo);

        // TODO Read annotation info entries

        // Resolve unresolved CP entries.
        resolveCPEntries();

        resolveConnectorMethodTables(packageInfo);

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
            UTF8CPEntry structNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(structNameCPIndex);
            String structName = structNameUTF8Entry.getValue();
            StructInfo structInfo = new StructInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                    structNameCPIndex, structName);
            packageInfo.addStructInfo(structName, structInfo);

            // Set struct type
            BStructType bStructType = new BStructType(structName, packageInfo.getPkgPath());
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

                StructFieldInfo fieldInfo = new StructFieldInfo(fieldNameCPIndex, fieldNameUTF8Entry.getValue(),
                        fieldTypeSigCPIndex, fieldTypeSigUTF8Entry.getValue());
                structInfo.addFieldInfo(fieldInfo);

                readAttributeInfoEntries(dataInStream, packageInfo, fieldInfo);
            }

            // Read attributes of the struct info
            readAttributeInfoEntries(dataInStream, packageInfo, structInfo);
        }
    }

    private void readConnectorInfoEntries(DataInputStream dataInStream,
                                          PackageInfo packageInfo) throws IOException {
        int connectorCount = dataInStream.readShort();
        for (int i = 0; i < connectorCount; i++) {
            // Read connector name cp index
            int connectorNameCPIndex = dataInStream.readInt();
            UTF8CPEntry connectorNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(connectorNameCPIndex);

            // Read connector signature cp index;
            int connectorSigCPIndex = dataInStream.readInt();
            UTF8CPEntry connectorSigUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(connectorSigCPIndex);

            // Create connector info
            String connectorName = connectorNameUTF8Entry.getValue();
            ConnectorInfo connectorInfo = new ConnectorInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                    connectorNameCPIndex, connectorName, connectorSigCPIndex, connectorSigUTF8Entry.getValue());
            packageInfo.addConnectorInfo(connectorName, connectorInfo);

            // Set connector type
            BConnectorType bConnectorType = new BConnectorType(connectorName, packageInfo.getPkgPath());
            connectorInfo.setType(bConnectorType);

            Map<Integer, Integer> methodTableInteger = new HashMap<>();
            int count = dataInStream.readInt();

            for (int k = 0; k < count; k++) {
                int key = dataInStream.readInt();
                int value = dataInStream.readInt();
                methodTableInteger.put(new Integer(key), new Integer(value));
            }

            connectorInfo.setMethodTableIndex(methodTableInteger);

            boolean isFilterConnector = dataInStream.readBoolean();
            connectorInfo.setFilterConnector(isFilterConnector);

            // Read action info entries
            int actionCount = dataInStream.readShort();
            for (int j = 0; j < actionCount; j++) {
                // Read action name;
                int actionNameCPIndex = dataInStream.readInt();
                UTF8CPEntry actionNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(actionNameCPIndex);
                String actionName = actionNameUTF8Entry.getValue();
                ActionInfo actionInfo = new ActionInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                        actionNameCPIndex, actionName, connectorInfo);
                actionInfo.setPackageInfo(packageInfo);
                connectorInfo.addActionInfo(actionName, actionInfo);

                // Read action signature
                int actionSigCPIndex = dataInStream.readInt();
                UTF8CPEntry actionSigUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(actionSigCPIndex);
                actionInfo.setSignatureCPIndex(actionSigCPIndex);
                actionInfo.setSignature(actionSigUTF8Entry.getValue());
                setCallableUnitSignature(actionInfo, actionSigUTF8Entry.getValue(), packageInfo);

                // TODO Temp solution.
                boolean nativeAction = dataInStream.readByte() == 1;
                actionInfo.setNative(nativeAction);

                int workerDataChannelsLength = dataInStream.readShort();
                for (int k = 0; k < workerDataChannelsLength; k++) {
                    readWorkerDataChannelEntries(dataInStream, packageInfo, actionInfo);
                }

                // Read worker info entries
                readWorkerInfoEntries(dataInStream, packageInfo, actionInfo);

                if (nativeAction) {
                    ActionSymbolName nativeActionSymName =
                            LangModelUtils.getNativeActionSymName(actionInfo.getName(),
                                    actionInfo.getConnectorInfo().getName(),
                                    actionInfo.getPkgPath(), actionInfo.getParamTypes());
                    BLangSymbol actionSymbol = nativeScope.resolve(nativeActionSymName);
                    if (actionSymbol == null) {
                        throw new BLangRuntimeException("native action not available " +
                                actionInfo.getPkgPath() + ":" + actionName);
                    }

                    NativeUnit nativeUnit = ((NativeUnitProxy) actionSymbol).load();
                    actionInfo.setNativeAction((AbstractNativeAction) nativeUnit);
                }

                // Read attributes of the struct info
                readAttributeInfoEntries(dataInStream, packageInfo, actionInfo);
            }

            // Read attributes of the struct info
            readAttributeInfoEntries(dataInStream, packageInfo, connectorInfo);
        }

        for (ConstantPoolEntry cpEntry : unresolvedCPEntries) {
            switch (cpEntry.getEntryType()) {
                case CP_ENTRY_TYPE_REF:
                    TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) cpEntry;
                    String typeSig = typeRefCPEntry.getTypeSig();
                    BType bType = getBTypeFromDescriptor(typeSig);
                    typeRefCPEntry.setType(bType);
                    break;
            }
        }
    }

    private void readServiceInfoEntries(DataInputStream dataInStream,
                                        PackageInfo packageInfo) throws IOException {
        int serviceCount = dataInStream.readShort();
        for (int i = 0; i < serviceCount; i++) {
            // Read connector name cp index
            int serviceNameCPIndex = dataInStream.readInt();
            UTF8CPEntry serviceNameUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(serviceNameCPIndex);

            // Read connector signature cp index;
            int serviceProtocolCPIndex = dataInStream.readInt();
            UTF8CPEntry serviceProtocolUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(serviceProtocolCPIndex);

            ServiceInfo serviceInfo = new ServiceInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                    serviceNameCPIndex, serviceNameUTF8Entry.getValue(),
                    serviceProtocolCPIndex, serviceProtocolUTF8Entry.getValue());
            serviceInfo.setPackageInfo(packageInfo);
            packageInfo.addServiceInfo(serviceInfo.getName(), serviceInfo);

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

        PackageVarInfo packageVarInfo = new PackageVarInfo(nameCPIndex, nameUTF8CPEntry.getValue(),
                sigCPIndex, sigUTF8CPEntry.getValue());

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

        FunctionInfo pkgIniFunInfo = packageInfo.getFunctionInfo(
                packageInfo.getPkgPath() + INIT_FUNCTION_SUFFIX);
        packageInfo.setInitFunctionInfo(pkgIniFunInfo);

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
        FunctionInfo functionInfo = new FunctionInfo(packageInfo.getPkgNameCPIndex(), packageInfo.getPkgPath(),
                funcNameCPIndex, funcNameUTF8Entry.getValue());
        functionInfo.setPackageInfo(packageInfo);
        packageInfo.addFunctionInfo(funcNameUTF8Entry.getValue(), functionInfo);

        int funcSigCPIndex = dataInStream.readInt();
        UTF8CPEntry funcSigUTF8Entry = (UTF8CPEntry) packageInfo.getCPEntry(funcSigCPIndex);
        setCallableUnitSignature(functionInfo, funcSigUTF8Entry.getValue(), packageInfo);

        // TODO Temp solution.
        boolean nativeFunc = dataInStream.readByte() == 1;
        functionInfo.setNative(nativeFunc);

        int workerDataChannelsLength = dataInStream.readShort();
        for (int i = 0; i < workerDataChannelsLength; i++) {
            readWorkerDataChannelEntries(dataInStream, packageInfo, functionInfo);
        }

        // Read worker info entries
        readWorkerInfoEntries(dataInStream, packageInfo, functionInfo);

        if (nativeFunc) {
            FunctionSymbolName symbolName = LangModelUtils.getFuncSymNameWithParams(functionInfo.getName(),
                    functionInfo.getPkgPath(), functionInfo.getParamTypes());
            BLangSymbol functionSymbol = nativeScope.resolve(symbolName);
            if (functionSymbol == null) {
                throw new BLangRuntimeException("native function not available " +
                        functionInfo.getPkgPath() + ":" + funcNameUTF8Entry.getValue());
            }

            NativeUnit nativeUnit = ((NativeUnitProxy) functionSymbol).load();
            functionInfo.setNativeFunction((AbstractNativeFunction) nativeUnit);
        }


        // Read attributes
        readAttributeInfoEntries(dataInStream, packageInfo, functionInfo);
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
        int indexOfSep = sig.indexOf(")(");
        String paramSig = sig.substring(1, indexOfSep);
        String retParamSig = sig.substring(indexOfSep + 2, sig.length() - 1);

        if (paramSig.length() == 0) {
            callableUnitInfo.setParamTypes(new BType[0]);
        } else {
            callableUnitInfo.setParamTypes(getParamTypes(paramSig, packageInfo));
        }

        if (retParamSig.length() == 0) {
            callableUnitInfo.setRetParamTypes(new BType[0]);
        } else {
            callableUnitInfo.setRetParamTypes(getParamTypes(retParamSig, packageInfo));
        }
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
                typeStack.push(BTypes.typeType);
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
            case 'K':
            case 'T':
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

                if (typeChar == 'C') {
                    typeStack.push(packageInfoOfType.getConnectorInfo(name).getType());
                } else if (typeChar == 'K') {
                    typeStack.push(new BJSONConstraintType(packageInfoOfType.getStructInfo(name).getType()));
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
            case 'U':
                // TODO : Fix this for type casting.
                typeStack.push(new BFunctionType());
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
                return BTypes.typeType;
            case 'L':
                return BTypes.typeBlob;
            case 'A':
                return BTypes.typeAny;
            case 'R':
                return BTypes.getTypeFromName(desc.substring(1, desc.length() - 1));
            case 'C':
            case 'K':
            case 'T':
                String pkgPath;
                String name;
                PackageInfo packageInfoOfType;
                String typeName = desc.substring(1, desc.length() - 1);
                String[] parts = typeName.split(":");
                pkgPath = parts[0];
                name = parts[1];
                packageInfoOfType = programFile.getPackageInfo(pkgPath);

                if (ch == 'C') {
                    return packageInfoOfType.getConnectorInfo(name).getType();
                } else if (ch == 'K') {
                    return new BJSONConstraintType(packageInfoOfType.getStructInfo(name).getType());
                } else {
                    return packageInfoOfType.getStructInfo(name).getType();
                }
            case '[':
                BType elemType = getBTypeFromDescriptor(desc.substring(1));
                return new BArrayType(elemType);
            case 'U':
                // TODO : Fix this for type casting.
                return new BFunctionType();
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
            forkjoinInfos[forkjoinInfo.getIndex()] = forkjoinInfo;
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

        int retRegLength = dataInStream.readShort();
        int[] retRegs = new int[retRegLength];
        for (int i = 0; i < retRegLength; i++) {
            retRegs[i] = dataInStream.readInt();
        }

        ForkJoinCPEntry forkJoinCPEntry = (ForkJoinCPEntry) packageInfo.getCPEntry(indexCPIndex);

        ForkjoinInfo forkjoinInfo = new ForkjoinInfo(argRegs, retRegs);
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


        int joinWorkerCount = dataInStream.readShort();
        int[] joinWrkrCPIndexes = new int[joinWorkerCount];
        String[] joinWrkrNames = new String[joinWorkerCount];
        for (int i = 0; i < joinWorkerCount; i++) {
            int cpIndex = dataInStream.readInt();
            UTF8CPEntry workerNameCPEntry = (UTF8CPEntry) packageInfo.getCPEntry(cpIndex);
            joinWrkrCPIndexes[i] = cpIndex;
            joinWrkrNames[i] = workerNameCPEntry.getValue();
        }
        forkjoinInfo.setJoinWrkrNameIndexes(joinWrkrCPIndexes);
        forkjoinInfo.setJoinWorkerNames(joinWrkrNames);

        int timeoutIp = dataInStream.readInt();
        forkjoinInfo.setTimeoutIp(timeoutIp);

        int timeoutMemOffset = dataInStream.readInt();
        forkjoinInfo.setTimeoutMemOffset(timeoutMemOffset);

        int joinIp = dataInStream.readInt();
        forkjoinInfo.setJoinIp(joinIp);

        int joinMemOffset = dataInStream.readInt();
        forkjoinInfo.setJoinMemOffset(joinMemOffset);

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

            case ANNOTATIONS_ATTRIBUTE:
                AnnotationAttributeInfo annAttributeInfo = new AnnotationAttributeInfo(attribNameCPIndex);
                int entryCount = dataInStream.readShort();
                for (int i = 0; i < entryCount; i++) {
                    AnnAttachmentInfo attachmentInfo = getAttachmentInfo(dataInStream, constantPool);
                    annAttributeInfo.addAttachmentInfo(attachmentInfo);
                }
                return annAttributeInfo;

            case PARAMETER_ANNOTATIONS_ATTRIBUTE:
                int prmAttchmentCnt = dataInStream.readShort();
                ParamAnnotationAttributeInfo attributeInfo = new ParamAnnotationAttributeInfo(attribNameCPIndex);
                for (int i = 0; i < prmAttchmentCnt; i++) {
                    ParamAnnAttachmentInfo prmAnnAtchmentInfo = getParamAttachmentInfo(dataInStream, constantPool);
                    attributeInfo.addParamAttachmentInfo(prmAnnAtchmentInfo);
                }
                return attributeInfo;
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
                StructFieldDefaultValue defaultValue = getDefaultValue(dataInStream, constantPool);
                DefaultValueAttributeInfo defaultValAttrInfo =
                        new DefaultValueAttributeInfo(attribNameCPIndex, defaultValue);
                return defaultValAttrInfo;
            default:
                throw new ProgramFileFormatException("unsupported attribute kind " + attribNameCPEntry.getValue());
        }
    }

    private AnnAttachmentInfo getAttachmentInfo(DataInputStream dataInStream,
                                                ConstantPool constantPool) throws IOException {
        int pkgCPIndex = dataInStream.readInt();
        PackageRefCPEntry pkgCPEntry = (PackageRefCPEntry) constantPool.getCPEntry(pkgCPIndex);
        int nameCPIndex = dataInStream.readInt();
        UTF8CPEntry nameCPEntry = (UTF8CPEntry) constantPool.getCPEntry(nameCPIndex);

        AnnAttachmentInfo attachmentInfo = new AnnAttachmentInfo(pkgCPIndex, pkgCPEntry.getPackageName(),
                nameCPIndex, nameCPEntry.getValue());

        int attribKeyValuePairsCount = dataInStream.readShort();
        for (int i = 0; i < attribKeyValuePairsCount; i++) {
            int attribNameCPIndex = dataInStream.readInt();
            UTF8CPEntry attribNameCPEntry = (UTF8CPEntry) constantPool.getCPEntry(attribNameCPIndex);
            String attribName = attribNameCPEntry.getValue();
            AnnAttributeValue attributeValue = getAnnAttributeValue(dataInStream, constantPool);
            attachmentInfo.addAttributeValue(attribNameCPIndex, attribName, attributeValue);
        }

        return attachmentInfo;
    }

    private ParamAnnAttachmentInfo getParamAttachmentInfo(DataInputStream dataInStream,
                                                ConstantPool constantPool) throws IOException {
        int paramIndex = dataInStream.readInt();
        ParamAnnAttachmentInfo prmAnnAttchmntInfo = new ParamAnnAttachmentInfo(paramIndex);

        int annAttchmntCount = dataInStream.readShort();
        for (int i = 0; i < annAttchmntCount; i++) {
            AnnAttachmentInfo annAttachmentInfo = getAttachmentInfo(dataInStream, constantPool);
            prmAnnAttchmntInfo.addAnnotationAttachmentInfo(annAttachmentInfo);
        }

        return prmAnnAttchmntInfo;
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


    private AnnAttributeValue getAnnAttributeValue(DataInputStream dataInStream,
                                                   ConstantPool constantPool) throws IOException {
        AnnAttributeValue attributeValue;
        int typeDescCPIndex = dataInStream.readInt();
        UTF8CPEntry typeDescCPEntry = (UTF8CPEntry) constantPool.getCPEntry(typeDescCPIndex);
        String typeDesc = typeDescCPEntry.getValue();

        boolean isConstVarExpr = dataInStream.readBoolean();
        if (isConstVarExpr) {
            int constPkgCPIndex = dataInStream.readInt();
            int constNameCPIndex = dataInStream.readInt();

            UTF8CPEntry constPkgCPEntry = (UTF8CPEntry) constantPool.getCPEntry(constPkgCPIndex);
            UTF8CPEntry constNameCPEntry = (UTF8CPEntry) constantPool.getCPEntry(constNameCPIndex);
            attributeValue = new AnnAttributeValue(typeDescCPIndex, typeDesc, constPkgCPIndex,
                    constPkgCPEntry.getValue(), constNameCPIndex, constNameCPEntry.getValue());
            attributeValue.setConstVarExpr(isConstVarExpr);
            programFile.addUnresolvedAnnAttrValue(attributeValue);
            return attributeValue;
        }

        int valueCPIndex;
        switch (typeDesc) {
            case TypeSignature.SIG_ANNOTATION:
                AnnAttachmentInfo attachmentInfo = getAttachmentInfo(dataInStream, constantPool);
                attributeValue = new AnnAttributeValue(typeDescCPIndex, typeDesc, attachmentInfo);
                break;
            case TypeSignature.SIG_ARRAY:
                int attributeValueCount = dataInStream.readShort();
                AnnAttributeValue[] annAttributeValues = new AnnAttributeValue[attributeValueCount];
                for (int i = 0; i < attributeValueCount; i++) {
                    annAttributeValues[i] = getAnnAttributeValue(dataInStream, constantPool);
                }
                attributeValue = new AnnAttributeValue(typeDescCPIndex, typeDesc, annAttributeValues);
                break;
            case TypeSignature.SIG_BOOLEAN:
                boolean boolValue = dataInStream.readBoolean();
                attributeValue = new AnnAttributeValue(typeDescCPIndex, typeDesc);
                attributeValue.setBooleanValue(boolValue);
                break;
            case TypeSignature.SIG_INT:
                valueCPIndex = dataInStream.readInt();
                IntegerCPEntry integerCPEntry = (IntegerCPEntry) constantPool.getCPEntry(valueCPIndex);
                attributeValue = new AnnAttributeValue(typeDescCPIndex, typeDesc);
                attributeValue.setIntValue(integerCPEntry.getValue());
                break;
            case TypeSignature.SIG_FLOAT:
                valueCPIndex = dataInStream.readInt();
                FloatCPEntry floatCPEntry = (FloatCPEntry) constantPool.getCPEntry(valueCPIndex);
                attributeValue = new AnnAttributeValue(typeDescCPIndex, typeDesc);
                attributeValue.setFloatValue(floatCPEntry.getValue());
                break;
            case TypeSignature.SIG_STRING:
                valueCPIndex = dataInStream.readInt();
                UTF8CPEntry stringCPEntry = (UTF8CPEntry) constantPool.getCPEntry(valueCPIndex);
                attributeValue = new AnnAttributeValue(typeDescCPIndex, typeDesc);
                attributeValue.setStringValue(stringCPEntry.getValue());
                break;
            default:
                throw new ProgramFileFormatException("unknown annotation attribute value type " + typeDesc);
        }

        return attributeValue;
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
                case InstructionCodes.FORKJOIN:
                case InstructionCodes.THROW:
                case InstructionCodes.ERRSTORE:
                case InstructionCodes.TR_END:
                case InstructionCodes.NEWJSON:
                case InstructionCodes.NEWMAP:
                case InstructionCodes.NEWMESSAGE:
                case InstructionCodes.NEWDATATABLE:
                case InstructionCodes.REP:
                    i = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, i));
                    break;

                case InstructionCodes.ICONST:
                case InstructionCodes.FCONST:
                case InstructionCodes.SCONST:
                case InstructionCodes.ILOAD:
                case InstructionCodes.FLOAD:
                case InstructionCodes.SLOAD:
                case InstructionCodes.BLOAD:
                case InstructionCodes.LLOAD:
                case InstructionCodes.RLOAD:
                case InstructionCodes.IGLOAD:
                case InstructionCodes.FGLOAD:
                case InstructionCodes.SGLOAD:
                case InstructionCodes.BGLOAD:
                case InstructionCodes.LGLOAD:
                case InstructionCodes.RGLOAD:
                case InstructionCodes.ISTORE:
                case InstructionCodes.FSTORE:
                case InstructionCodes.SSTORE:
                case InstructionCodes.BSTORE:
                case InstructionCodes.LSTORE:
                case InstructionCodes.RSTORE:
                case InstructionCodes.IGSTORE:
                case InstructionCodes.FGSTORE:
                case InstructionCodes.SGSTORE:
                case InstructionCodes.BGSTORE:
                case InstructionCodes.LGSTORE:
                case InstructionCodes.RGSTORE:
                case InstructionCodes.INEG:
                case InstructionCodes.FNEG:
                case InstructionCodes.BNOT:
                case InstructionCodes.REQ_NULL:
                case InstructionCodes.RNE_NULL:
                case InstructionCodes.BR_TRUE:
                case InstructionCodes.BR_FALSE:
                case InstructionCodes.TR_RETRY:
                case InstructionCodes.TR_BEGIN:
                case InstructionCodes.CALL:
                case InstructionCodes.WRKINVOKE:
                case InstructionCodes.WRKREPLY:
                case InstructionCodes.NCALL:
                case InstructionCodes.ACALL:
                case InstructionCodes.NACALL:
                case InstructionCodes.FPCALL:
                case InstructionCodes.FPLOAD:
                case InstructionCodes.ARRAYLEN:
                case InstructionCodes.INEWARRAY:
                case InstructionCodes.FNEWARRAY:
                case InstructionCodes.SNEWARRAY:
                case InstructionCodes.BNEWARRAY:
                case InstructionCodes.LNEWARRAY:
                case InstructionCodes.RNEWARRAY:
                case InstructionCodes.JSONNEWARRAY:
                case InstructionCodes.NEWSTRUCT:
                case InstructionCodes.NEWCONNECTOR:
                case InstructionCodes.IRET:
                case InstructionCodes.FRET:
                case InstructionCodes.SRET:
                case InstructionCodes.BRET:
                case InstructionCodes.LRET:
                case InstructionCodes.RRET:
                case InstructionCodes.XML2XMLATTRS:
                case InstructionCodes.NEWXMLCOMMENT:
                case InstructionCodes.NEWXMLTEXT:
                case InstructionCodes.XMLSTORE:
                case InstructionCodes.LENGTHOF:
                case InstructionCodes.LENGTHOFJSON:
                case InstructionCodes.TYPEOF:
                case InstructionCodes.TYPELOAD:
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
                case InstructionCodes.ANY2MSG:
                case InstructionCodes.ANY2TYPE:
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
                case InstructionCodes.T2JSON:
                case InstructionCodes.XML2JSON:
                case InstructionCodes.JSON2XML:
                case InstructionCodes.XMLATTRS2MAP:
                case InstructionCodes.XMLATTRLOAD:
                case InstructionCodes.XMLATTRSTORE:
                case InstructionCodes.S2QNAME:
                case InstructionCodes.NEWXMLPI:
                case InstructionCodes.TEQ:
                case InstructionCodes.TNE:
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    k = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, i, j, k));
                    break;

                case InstructionCodes.ANY2T:
                case InstructionCodes.ANY2C:
                case InstructionCodes.CHECKCAST:
                case InstructionCodes.MAP2T:
                case InstructionCodes.JSON2T:
                case InstructionCodes.NEWQNAME:
                case InstructionCodes.NEWXMLELEMENT:
                    i = codeStream.readInt();
                    j = codeStream.readInt();
                    k = codeStream.readInt();
                    h = codeStream.readInt();
                    packageInfo.addInstruction(InstructionFactory.get(opcode, i, j, k, h));
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
                case CP_ENTRY_FUNCTION_REF:
                    FunctionRefCPEntry funcRefCPEntry = (FunctionRefCPEntry) cpEntry;
                    packageInfo = programFile.getPackageInfo(funcRefCPEntry.getPackagePath());
                    FunctionInfo functionInfo = packageInfo.getFunctionInfo(funcRefCPEntry.getFunctionName());
                    funcRefCPEntry.setFunctionInfo(functionInfo);
                    break;
                case CP_ENTRY_ACTION_REF:
                    ActionRefCPEntry actionRefCPEntry = (ActionRefCPEntry) cpEntry;
                    structureRefCPEntry = actionRefCPEntry.getConnectorRefCPEntry();
                    ConnectorInfo connectorInfo = (ConnectorInfo) structureRefCPEntry.getStructureTypeInfo();
                    ActionInfo actionInfo = connectorInfo.getActionInfo(actionRefCPEntry.getActionName());
                    actionRefCPEntry.setActionInfo(actionInfo);
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
                BStructType.StructField structField = new BStructType.StructField(fieldType, fieldInfo.getName());
                structFields[i] = structField;
            }

            VarTypeCountAttributeInfo attributeInfo = (VarTypeCountAttributeInfo)
                    structInfo.getAttributeInfo(AttributeInfo.Kind.VARIABLE_TYPE_COUNT_ATTRIBUTE);
            structType.setFieldTypeCount(attributeInfo.getVarTypeCount());
            structType.setStructFields(structFields);
        }
    }

    private void resolveConnectorMethodTables(PackageInfo packageInfo) {
        ConnectorInfo[] connectorInfoEntries = packageInfo.getConnectorInfoEntries();
        for (ConnectorInfo connectorInfo : connectorInfoEntries) {
            BConnectorType connectorType = connectorInfo.getType();

            VarTypeCountAttributeInfo attributeInfo = (VarTypeCountAttributeInfo)
                    connectorInfo.getAttributeInfo(AttributeInfo.Kind.VARIABLE_TYPE_COUNT_ATTRIBUTE);
            connectorType.setFieldTypeCount(attributeInfo.getVarTypeCount());

            Map<Integer, Integer> methodTableInteger = connectorInfo.getMethodTableIndex();
            Map<BConnectorType, ConnectorInfo> methodTableType = new HashMap<>();
            for (Integer key : methodTableInteger.keySet()) {
                int keyType = methodTableInteger.get(key);
                TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) packageInfo.getCPEntry(key);
                StructureRefCPEntry structureRefCPEntry = (StructureRefCPEntry) packageInfo.getCPEntry(keyType);
                ConnectorInfo connectorInfoType = (ConnectorInfo) structureRefCPEntry.getStructureTypeInfo();
                methodTableType.put((BConnectorType) typeRefCPEntry.getType(), connectorInfoType);
            }

            connectorInfo.setMethodTableType(methodTableType);

            for (ActionInfo actionInfo : connectorInfo.getActionInfoEntries()) {
                setCallableUnitSignature(actionInfo, actionInfo.getSignature(), packageInfo);
            }
        }
    }

    private StructFieldDefaultValue getDefaultValue(DataInputStream dataInStream, ConstantPool constantPool)
            throws IOException {
        StructFieldDefaultValue defaultValue;
        int typeDescCPIndex = dataInStream.readInt();
        UTF8CPEntry typeDescCPEntry = (UTF8CPEntry) constantPool.getCPEntry(typeDescCPIndex);
        String typeDesc = typeDescCPEntry.getValue();

        int valueCPIndex;
        switch (typeDesc) {
            case TypeSignature.SIG_BOOLEAN:
                boolean boolValue = dataInStream.readBoolean();
                defaultValue = new StructFieldDefaultValue(typeDescCPIndex, typeDesc);
                defaultValue.setBooleanValue(boolValue);
                break;
            case TypeSignature.SIG_INT:
                valueCPIndex = dataInStream.readInt();
                IntegerCPEntry integerCPEntry = (IntegerCPEntry) constantPool.getCPEntry(valueCPIndex);
                defaultValue = new StructFieldDefaultValue(typeDescCPIndex, typeDesc);
                defaultValue.setIntValue(integerCPEntry.getValue());
                break;
            case TypeSignature.SIG_FLOAT:
                valueCPIndex = dataInStream.readInt();
                FloatCPEntry floatCPEntry = (FloatCPEntry) constantPool.getCPEntry(valueCPIndex);
                defaultValue = new StructFieldDefaultValue(typeDescCPIndex, typeDesc);
                defaultValue.setFloatValue(floatCPEntry.getValue());
                break;
            case TypeSignature.SIG_STRING:
                valueCPIndex = dataInStream.readInt();
                UTF8CPEntry stringCPEntry = (UTF8CPEntry) constantPool.getCPEntry(valueCPIndex);
                defaultValue = new StructFieldDefaultValue(typeDescCPIndex, typeDesc);
                defaultValue.setStringValue(stringCPEntry.getValue());
                break;
            default:
                throw new ProgramFileFormatException("unknown default value type " + typeDesc);
        }
        return defaultValue;
    }
}

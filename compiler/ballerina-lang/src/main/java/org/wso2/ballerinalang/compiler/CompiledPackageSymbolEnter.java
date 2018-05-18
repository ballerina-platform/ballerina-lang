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
package org.wso2.ballerinalang.compiler;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.TaintRecord;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfo;
import org.wso2.ballerinalang.programfile.attributes.AttributeInfo.Kind;
import org.wso2.ballerinalang.programfile.cpentries.BlobCPEntry;
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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.function.Consumer;

import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;
import static org.wso2.ballerinalang.util.LambdaExceptionUtils.rethrow;

/**
 * This class is responsible for reading the compiled package file (balo) and creating a package symbol.
 * <p>
 * TODO This class contain duplicate code which we are planning to refactor
 *
 * @since 0.970.0
 */
public class CompiledPackageSymbolEnter {
    private final PackageLoader packageLoader;
    private final SymbolTable symTable;
    private final Names names;
    private final BLangDiagnosticLog dlog;

    private CompiledPackageSymbolEnv env;

    private static final CompilerContext.Key<CompiledPackageSymbolEnter> COMPILED_PACKAGE_SYMBOL_ENTER_KEY =
            new CompilerContext.Key<>();

    public static CompiledPackageSymbolEnter getInstance(CompilerContext context) {
        CompiledPackageSymbolEnter packageReader = context.get(COMPILED_PACKAGE_SYMBOL_ENTER_KEY);
        if (packageReader == null) {
            packageReader = new CompiledPackageSymbolEnter(context);
        }

        return packageReader;
    }

    private CompiledPackageSymbolEnter(CompilerContext context) {
        context.put(COMPILED_PACKAGE_SYMBOL_ENTER_KEY, this);

        this.packageLoader = PackageLoader.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public BPackageSymbol definePackage(PackageID packageId,
                                        PackageRepository packageRepository,
                                        byte[] packageBinaryContent) {
        BPackageSymbol pkgSymbol = definePackage(packageId, packageRepository,
                new ByteArrayInputStream(packageBinaryContent));

        // Strip magic value (4 bytes) and the version (2 bytes) off from the binary content of the package.
        byte[] modifiedPkgBinaryContent = Arrays.copyOfRange(
                packageBinaryContent, 6, packageBinaryContent.length);
        pkgSymbol.packageFile = new CompiledBinaryFile.PackageFile(modifiedPkgBinaryContent);
        return pkgSymbol;
    }

    public BPackageSymbol definePackage(PackageID packageId,
                                        PackageRepository packageRepository,
                                        InputStream programFileInStream) {
        // TODO packageID --> package to be loaded. this is required for error reporting..
        try (DataInputStream dataInStream = new DataInputStream(programFileInStream)) {
            CompiledPackageSymbolEnv prevEnv = this.env;
            this.env = new CompiledPackageSymbolEnv();
            this.env.requestedPackageId = packageId;
            this.env.loadedRepository = packageRepository;
            BPackageSymbol pkgSymbol = definePackage(dataInStream);
            this.env = prevEnv;
            return pkgSymbol;
        } catch (IOException e) {
            // TODO dlog.error();
            throw new BLangCompilerException("io error: " + e.getMessage(), e);
//            return null;
        } catch (Throwable e) {
            // TODO format error
            throw new BLangCompilerException("format error: " + e.getMessage(), e);
//            return null;
        }
    }

    private BPackageSymbol definePackage(DataInputStream dataInStream) throws IOException {
        int magicNumber = dataInStream.readInt();
        if (magicNumber != CompiledBinaryFile.PackageFile.MAGIC_VALUE) {
            // TODO dlog.error() with package name
            throw new BLangCompilerException("ballerina: invalid magic number " + magicNumber);
        }

        short version = dataInStream.readShort();
        if (version != CompiledBinaryFile.PackageFile.LANG_VERSION) {
            // TODO dlog.error() with package name
            throw new BLangCompilerException("ballerina: unsupported program file version " + version);
        }

        // Read constant pool entries of the package info.
        this.env.constantPool = readConstantPool(dataInStream);

        // Read packageName and version
        String pkgName = getUTF8CPEntryValue(dataInStream);
        String pkgVersion = getUTF8CPEntryValue(dataInStream);
        return definePackage(dataInStream, pkgName, pkgVersion);
    }

    private BPackageSymbol definePackage(DataInputStream dataInStream,
                                         String pkgName,
                                         String pkgVersion) throws IOException {

        PackageID pkgId = createPackageID(pkgName, pkgVersion);
        this.env.pkgSymbol = Symbols.createPackageSymbol(pkgId, this.symTable);
//        this.env.pkgSymbol.packageRepository = this.env.loadedRepository;

        // TODO Validate this pkdID with the requestedPackageID available in the env.

        // define import packages
        defineSymbols(dataInStream, rethrow(this::defineImportPackage));

        // define objects
        defineSymbols(dataInStream, rethrow(this::defineObject));

        // define type defs
        defineSymbols(dataInStream, rethrow(this::defineTypeDef));

        // define services
        defineSymbols(dataInStream, rethrow(this::defineService));


        // Resolve unresolved types..
        resolveTypes();

        // Read resource info entries.
        defineSymbols(dataInStream, rethrow(this::defineResource));

        // Define package level variables
        defineSymbols(dataInStream, rethrow(this::definePackageLevelVariables));

        // Define resource entries

        // Define global variables

        // Define functions
        defineSymbols(dataInStream, rethrow(this::defineFunction));
        assignInitFunctions();

        // Read package level attributes
        readAttributes(dataInStream);

        // Read instructions array

        return this.env.pkgSymbol;
    }

    private ConstantPoolEntry[] readConstantPool(DataInputStream dataInStream) throws IOException {
        int constantPoolSize = dataInStream.readInt();
        ConstantPoolEntry[] constantPool = new ConstantPoolEntry[constantPoolSize];
        for (int i = 0; i < constantPoolSize; i++) {
            byte cpTag = dataInStream.readByte();
            ConstantPoolEntry.EntryType cpEntryType = ConstantPoolEntry.EntryType.values()[cpTag - 1];
            constantPool[i] = readCPEntry(dataInStream, constantPool, cpEntryType);
        }
        return constantPool;
    }

    private ConstantPoolEntry readCPEntry(DataInputStream dataInStream,
                                          ConstantPoolEntry[] constantPool,
                                          ConstantPoolEntry.EntryType cpEntryType) throws IOException {
        int cpIndex;
        int pkgCPIndex;
        UTF8CPEntry utf8CPEntry;
        switch (cpEntryType) {
            case CP_ENTRY_UTF8:
                // Discard the length of bytes for now.
                dataInStream.readShort();
                return new UTF8CPEntry(dataInStream.readUTF());
            case CP_ENTRY_INTEGER:
                return new IntegerCPEntry(dataInStream.readLong());
            case CP_ENTRY_FLOAT:
                return new FloatCPEntry(dataInStream.readDouble());
            case CP_ENTRY_STRING:
                cpIndex = dataInStream.readInt();
                utf8CPEntry = (UTF8CPEntry) constantPool[cpIndex];
                return new StringCPEntry(cpIndex, utf8CPEntry.getValue());
            case CP_ENTRY_BLOB:
                int length = dataInStream.readInt();
                byte[] blobValue = new byte[length];
                dataInStream.readFully(blobValue);
                return new BlobCPEntry(blobValue);
            case CP_ENTRY_PACKAGE:
                cpIndex = dataInStream.readInt();
                int versionCPIndex = dataInStream.readInt();
                return new PackageRefCPEntry(cpIndex, versionCPIndex);
            case CP_ENTRY_FUNCTION_REF:
                pkgCPIndex = dataInStream.readInt();
                cpIndex = dataInStream.readInt();
                return new FunctionRefCPEntry(pkgCPIndex, cpIndex);
            case CP_ENTRY_STRUCTURE_REF:
                pkgCPIndex = dataInStream.readInt();
                cpIndex = dataInStream.readInt();
                return new StructureRefCPEntry(pkgCPIndex, cpIndex);
            case CP_ENTRY_TYPE_REF:
                int typeSigCPIndex = dataInStream.readInt();
                return new TypeRefCPEntry(typeSigCPIndex);
            case CP_ENTRY_FORK_JOIN:
                int forkJoinCPIndex = dataInStream.readInt();
                return new ForkJoinCPEntry(forkJoinCPIndex);
            case CP_ENTRY_WRKR_DATA_CHNL_REF:
                int uniqueNameCPIndex = dataInStream.readInt();
                UTF8CPEntry wrkrDtChnlTypesSigCPEntry = (UTF8CPEntry) constantPool[uniqueNameCPIndex];
                return new WorkerDataChannelRefCPEntry(uniqueNameCPIndex, wrkrDtChnlTypesSigCPEntry.getValue());
            default:
                throw new BLangCompilerException("invalid constant pool entry " + cpEntryType.getValue());
        }
    }

    private void defineSymbols(DataInputStream dataInStream,
                               Consumer<DataInputStream> symbolDefineFunc) throws IOException {
        int symbolCount = dataInStream.readShort();
        for (int i = 0; i < symbolCount; i++) {
            symbolDefineFunc.accept(dataInStream);
        }
    }

    // TODO do we need to load all the import packages of a compiled package.
    private void defineImportPackage(DataInputStream dataInStream) throws IOException {
        String pkgName = getUTF8CPEntryValue(dataInStream);
        String pkgVersion = getUTF8CPEntryValue(dataInStream);
        PackageID importPkgID = createPackageID(pkgName, pkgVersion);
        BPackageSymbol importPackageSymbol = packageLoader.loadPackageSymbol(importPkgID, this.env.loadedRepository);
        // Define the import package with the alias being the package name
        this.env.pkgSymbol.scope.define(importPkgID.name, importPackageSymbol);
    }

    private void defineObject(DataInputStream dataInStream) throws IOException {
        String objectName = getUTF8CPEntryValue(dataInStream);
        int flags = dataInStream.readInt();

        BTypeSymbol objectSymbol = Symbols.createStructSymbol(flags, names.fromString(objectName),
                this.env.pkgSymbol.pkgID, null, this.env.pkgSymbol);
        BStructType objectType = new BStructType(objectSymbol);
        objectSymbol.type = objectType;
        this.env.pkgSymbol.scope.define(objectSymbol.name, objectSymbol);

        // Define Object Fields
        defineSymbols(dataInStream, rethrow(dataInputStream ->
                defineObjectField(dataInStream, objectSymbol, objectType)));

        // Define attached functions.
        // TODO define attached functions..

        // Read and ignore attributes
        readAttributes(dataInStream);
    }

    private void defineObjectField(DataInputStream dataInStream,
                                   BTypeSymbol objectSymbol,
                                   BStructType objectType) throws IOException {
        String fieldName = getUTF8CPEntryValue(dataInStream);
        String typeSig = getUTF8CPEntryValue(dataInStream);
        int flags = dataInStream.readInt();

        BVarSymbol varSymbol = new BVarSymbol(flags, names.fromString(fieldName),
                objectSymbol.pkgID, null, objectSymbol.scope.owner);
        objectSymbol.scope.define(varSymbol.name, varSymbol);

        // Read the default value attribute
        Map<AttributeInfo.Kind, byte[]> attrData = readAttributes(dataInStream);
        byte[] defaultValueAttrData = attrData.get(AttributeInfo.Kind.DEFAULT_VALUE_ATTRIBUTE);
        varSymbol.defaultValue = getObjectFieldDefaultValue(defaultValueAttrData);

        // The object field type cannot be resolved now. Hence add it to the unresolved type list.
        UnresolvedType unresolvedFieldType = new UnresolvedType(typeSig, type -> {
            varSymbol.type = type;
            BStructType.BStructField structField = new BStructType.BStructField(varSymbol.name,
                    varSymbol, varSymbol.defaultValue != null);
            objectType.fields.add(structField);
        });
        this.env.unresolvedTypes.add(unresolvedFieldType);
    }

    private void defineFunction(DataInputStream dataInStream) throws IOException {
        // Consider attached functions.. remove the first variable
        String funcName = getUTF8CPEntryValue(dataInStream);
        String funcSig = getUTF8CPEntryValue(dataInStream);
        int flags = dataInStream.readInt();

        if (Symbols.isFlagOn(flags, Flags.ATTACHED)) {
            int attachedToTypeRefCPIndex = dataInStream.readInt();
        }

        // Read and ignore worker data
        int noOfWorkerDataBytes = dataInStream.readInt();
        byte[] workerData = new byte[noOfWorkerDataBytes];
        int bytesRead = dataInStream.read(workerData);
        if (bytesRead != noOfWorkerDataBytes) {
            // TODO throw an error
        }

        // Read attributes
        Map<Kind, byte[]> attrDataMap = readAttributes(dataInStream);

        // TODO create function symbol and define..
        BInvokableSymbol invokableSymbol = Symbols.createFunctionSymbol(flags, names.fromString(funcName),
                this.env.pkgSymbol.pkgID, null, this.env.pkgSymbol, Symbols.isFlagOn(flags, Flags.NATIVE));
        invokableSymbol.type = createInvokableType(funcSig);

        // set parameter symbols to the function symbol
        setParamSymbols(invokableSymbol, attrDataMap);

        // set taint table to the function symbol
        setTaintTable(invokableSymbol, attrDataMap);
        
        this.env.pkgSymbol.scope.define(invokableSymbol.name, invokableSymbol);
    }

    private void defineTypeDef(DataInputStream dataInStream) throws IOException {
    }

    private void defineService(DataInputStream dataInStream) throws IOException {
        // Read connector name cp index
        String serviceName = getUTF8CPEntryValue(dataInStream);
        int flags = dataInStream.readInt();
        // endpoint type is not required for service symbol.
        getUTF8CPEntryValue(dataInStream);

        BServiceSymbol serviceSymbol = Symbols.createServiceSymbol(flags,
                names.fromString(serviceName), this.env.pkgSymbol.pkgID, null, env.pkgSymbol);
        serviceSymbol.type = new BServiceType(serviceSymbol);
        this.env.pkgSymbol.scope.define(serviceSymbol.name, serviceSymbol);
    }

    private void defineResource(DataInputStream dataInStream) throws IOException {
        int resourceCount = dataInStream.readShort();
        for (int j = 0; j < resourceCount; j++) {
            dataInStream.readInt();
            dataInStream.readInt();
            int paramNameCPIndexesCount = dataInStream.readShort();
            for (int k = 0; k < paramNameCPIndexesCount; k++) {
                dataInStream.readInt();
            }

            // Read and ignore worker data
            int noOfWorkerDataBytes = dataInStream.readInt();
            byte[] workerData = new byte[noOfWorkerDataBytes];
            int bytesRead = dataInStream.read(workerData);
            if (bytesRead != noOfWorkerDataBytes) {
                // TODO throw an error
            }

            // Read attributes
            readAttributes(dataInStream);
        }
        readAttributes(dataInStream);
    }

    private void definePackageLevelVariables(DataInputStream dataInStream) throws IOException {
        String name = getUTF8CPEntryValue(dataInStream);
        String typeSig = getUTF8CPEntryValue(dataInStream);
        int memIndex = dataInStream.readInt();

        readAttributes(dataInStream);
    }


    private Map<AttributeInfo.Kind, byte[]> readAttributes(DataInputStream dataInStream) throws IOException {
        int attributesCount = dataInStream.readShort();
        if (attributesCount == 0) {
            return new HashMap<>();
        }

        Map<AttributeInfo.Kind, byte[]> attrDataMap = new HashMap<>(attributesCount);
        for (int i = 0; i < attributesCount; i++) {
            String attrName = getUTF8CPEntryValue(dataInStream);
            AttributeInfo.Kind attrKind = AttributeInfo.Kind.fromString(attrName);
            if (attrKind == null) {
                // TODO use dlog....
                throw new BLangCompilerException("unknown attribute kind " + attrName);
            }

            int noOfAttrDataBytes = dataInStream.readInt();
            byte[] attrData = new byte[noOfAttrDataBytes];
            int noOfBytesRead = dataInStream.read(attrData);
            if (noOfAttrDataBytes != noOfBytesRead) {
                // TODO This is and error..
            }

            attrDataMap.put(attrKind, attrData);
        }
        return attrDataMap;
    }

    /**
     * Set parameter symbols to the invokable symbol.
     * 
     * @param invokableSymbol Invokable symbol
     * @param attrDataMap Attribute data map
     * @throws IOException
     */
    private void setParamSymbols(BInvokableSymbol invokableSymbol, Map<AttributeInfo.Kind, byte[]> attrDataMap)
            throws IOException {

        if (!attrDataMap.containsKey(AttributeInfo.Kind.PARAMETERS_ATTRIBUTE) ||
                !attrDataMap.containsKey(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE)) {
            return;
        }

        // Get parameter counts
        byte[] paramData = attrDataMap.get(AttributeInfo.Kind.PARAMETERS_ATTRIBUTE);
        DataInputStream paramDataInStream = new DataInputStream(new ByteArrayInputStream(paramData));
        int requiredParamCount = paramDataInStream.readInt();
        int defaultableParamCount = paramDataInStream.readInt();
        int restParamCount = paramDataInStream.readInt();

        // Get var names and create var symbols
        byte[] localVarData = attrDataMap.get(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE);
        DataInputStream localVarDataInStream = new DataInputStream(new ByteArrayInputStream(localVarData));
        localVarDataInStream.readShort();
        BInvokableType funcType = (BInvokableType) invokableSymbol.type;
        for (int i = 0; i < requiredParamCount; i++) {
            String varName = getVarName(localVarDataInStream);
            BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(varName), this.env.pkgSymbol.pkgID,
                    funcType.paramTypes.get(i), this.env.pkgSymbol);
            invokableSymbol.params.add(varSymbol);
        }

        for (int i = requiredParamCount; i < defaultableParamCount; i++) {
            String varName = getVarName(localVarDataInStream);
            BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(varName), this.env.pkgSymbol.pkgID,
                    funcType.paramTypes.get(i), this.env.pkgSymbol);
            invokableSymbol.defaultableParams.add(varSymbol);
        }

        if (restParamCount == 1) {
            String varName = getVarName(localVarDataInStream);
            BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(varName), this.env.pkgSymbol.pkgID,
                    funcType.paramTypes.get(requiredParamCount + defaultableParamCount), this.env.pkgSymbol);
            invokableSymbol.restParam = varSymbol;
        }
    }

    /**
     * Set taint table to the invokable symbol.
     *
     * @param invokableSymbol Invokable symbol
     * @param attrDataMap Attribute data map
     * @throws IOException
     */
    private void setTaintTable(BInvokableSymbol invokableSymbol, Map<AttributeInfo.Kind, byte[]> attrDataMap)
            throws IOException {

        if (!attrDataMap.containsKey(Kind.TAINT_TABLE) ||
                !attrDataMap.containsKey(AttributeInfo.Kind.TAINT_TABLE)) {
            return;
        }

        // Get taint table dimensions
        byte[] taintTableBytes = attrDataMap.get(AttributeInfo.Kind.TAINT_TABLE);
        DataInputStream taintTableDataInStream = new DataInputStream(new ByteArrayInputStream(taintTableBytes));
        int rowCount = taintTableDataInStream.readShort();
        int columnCount = taintTableDataInStream.readShort();

        // Extract and set taint table to the symbol
        invokableSymbol.taintTable = new HashMap<>();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            int paramIndex = taintTableDataInStream.readShort();
            List<Boolean> retParamTaintedStatus = new ArrayList<>();
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                retParamTaintedStatus.add(taintTableDataInStream.readBoolean());
            }
            TaintRecord taintRecord = new TaintRecord(retParamTaintedStatus, null);
            invokableSymbol.taintTable.put(paramIndex, taintRecord);
        }
    }

    private String getVarName(DataInputStream dataInStream) throws IOException {
        String varName = getUTF8CPEntryValue(dataInStream);
        // read variable index
        dataInStream.readInt();
        getUTF8CPEntryValue(dataInStream);

        int attchmntIndexesLength = dataInStream.readShort();
        for (int i = 0; i < attchmntIndexesLength; i++) {
            dataInStream.readInt();
        }

        return varName;
    }

    private Object getObjectFieldDefaultValue(byte[] defaultValueAttrData) throws IOException {
        try (DataInputStream dataInStream = new DataInputStream(
                new ByteArrayInputStream(defaultValueAttrData))) {
            String typeDesc = getUTF8CPEntryValue(dataInStream);
            switch (typeDesc) {
                case TypeDescriptor.SIG_INT:
                    return dataInStream.readInt();
                case TypeDescriptor.SIG_FLOAT:
                    return dataInStream.readFloat();
                case TypeDescriptor.SIG_BOOLEAN:
                    return dataInStream.readBoolean();
                case TypeDescriptor.SIG_STRING:
                    return getUTF8CPEntryValue(dataInStream);
                default:
                    throw new BLangCompilerException("unknown default value type " + typeDesc);
            }
        }
    }


    // private utility methods

    private String getUTF8CPEntryValue(DataInputStream dataInStream) throws IOException {
        int pkgNameCPIndex = dataInStream.readInt();
        UTF8CPEntry pkgNameCPEntry = (UTF8CPEntry) this.env.constantPool[pkgNameCPIndex];
        return pkgNameCPEntry.getValue();
    }

    private PackageID createPackageID(String pkgName, String pkgVersion) {
        String[] parts = pkgName.split("\\.");
        if (parts.length < 2) {
            throw new BLangCompilerException("Invalid package name '" + pkgName + "' in compiled package file");
        }

        StringJoiner joiner = new StringJoiner(Names.DOT.value);
        Arrays.stream(parts, 1, parts.length).forEachOrdered(joiner::add);
        Name orgName = names.fromString(parts[0]);
        return new PackageID(orgName,
                names.fromString(joiner.toString()),
                names.fromString(pkgVersion));
    }

    private BInvokableType createInvokableType(String sig) {
        int indexOfSep = sig.indexOf(")(");
        String paramSig = sig.substring(1, indexOfSep);
        String retParamSig = sig.substring(indexOfSep + 2, sig.length() - 1);
        BType[] paramTypes = getParamTypes(paramSig);
        BType[] retParamTypes = getParamTypes(retParamSig);
        BType retType = retParamTypes.length != 0 ? retParamTypes[0] : this.symTable.nilType;
        return new BInvokableType(Arrays.asList(paramTypes), retType, null);
    }

    private BType[] getParamTypes(String signature) {
        int index = 0;
        Stack<BType> typeStack = new Stack<>();
        char[] chars = signature.toCharArray();
        while (index < chars.length) {
            index = createBTypeFromSig(chars, index, typeStack);
        }

        return typeStack.toArray(new BType[0]);
    }

    private int createBTypeFromSig(char[] chars, int index, Stack<BType> typeStack) {
        int nameIndex;
        char ch = chars[index];
        switch (ch) {
            case 'I':
                typeStack.push(this.symTable.intType);
                return index + 1;
            case 'F':
                typeStack.push(this.symTable.floatType);
                return index + 1;
            case 'S':
                typeStack.push(this.symTable.stringType);
                return index + 1;
            case 'B':
                typeStack.push(this.symTable.booleanType);
                return index + 1;
            case 'L':
                typeStack.push(this.symTable.blobType);
                return index + 1;
            case 'Y':
                typeStack.push(this.symTable.typeDesc);
                return index + 1;
            case 'A':
                typeStack.push(this.symTable.anyType);
                return index + 1;
            case 'R':
                index++;
                nameIndex = index;
                while (chars[nameIndex] != ';') {
                    nameIndex++;
                }
                String typeName = new String(Arrays.copyOfRange(chars, index, nameIndex));
                typeStack.push(getBuiltinRefTypeFromName(typeName));
                return nameIndex + 1;
            case '[':
                index = createBTypeFromSig(chars, index + 1, typeStack);
                BType elemType = typeStack.pop();
                BArrayType arrayType = new BArrayType(elemType);
                typeStack.push(arrayType);
                return index;
            case 'J':
            case 'T':
            case 'D':
            case 'G':
            case 'H':
                char typeChar = chars[index];
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
                BPackageSymbol pkgSymbol;
                if (colonIndex != -1) {
                    pkgPath = new String(Arrays.copyOfRange(chars, index, colonIndex));
                    name = new String(Arrays.copyOfRange(chars, colonIndex + 1, nameIndex));
                    pkgSymbol = lookupImportPackageSymbol(names.fromString(pkgPath));
                } else {
                    name = new String(Arrays.copyOfRange(chars, index, nameIndex));
                    // Setting the current package;
                    pkgSymbol = this.env.pkgSymbol;
                }

                if (typeChar == 'J') {
                    if (name.isEmpty()) {
                        typeStack.push(this.symTable.jsonType);
                    } else {
                        typeStack.push(new BJSONType(TypeTags.JSON, lookupUserDefinedType(pkgSymbol, name), null));
                    }
                } else if (typeChar == 'D') {
                    if (name.isEmpty()) {
                        typeStack.push(this.symTable.tableType);
                    } else {
                        typeStack.push(new BTableType(TypeTags.TABLE, lookupUserDefinedType(pkgSymbol, name), null));
                    }
                } else if (typeChar == 'H') {
                    if (name.isEmpty()) {
                        typeStack.push(this.symTable.streamType);
                    } else {
                        typeStack.push(new BStreamType(TypeTags.STREAM, lookupUserDefinedType(pkgSymbol, name), null));
                    }
                } else if (typeChar == 'G' || typeChar == 'T') {
                    typeStack.push(lookupUserDefinedType(pkgSymbol, name));
                    typeStack.push(lookupUserDefinedType(pkgSymbol, name));
                }

                return nameIndex + 1;
            case 'M':
                index = createBTypeFromSig(chars, index + 1, typeStack);
                BType constrainedType = typeStack.pop();
                BType mapType;
                if (constrainedType == this.symTable.anyType) {
                    mapType = this.symTable.mapType;
                } else {
                    mapType = new BMapType(TypeTags.MAP, constrainedType, null);
                }
                typeStack.push(mapType);
                return index;
            case 'U':
                // TODO : Fix this for type casting.
//                typeStack.push(new BFunctBionType());
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
                    index = createBTypeFromSig(chars, index + 1, typeStack) - 1;
                    memberTypes.add(typeStack.pop());
                }
                if (typeChar == 'O') {
                    typeStack.push(new BUnionType(null, new LinkedHashSet<>(memberTypes),
                            memberTypes.contains(this.symTable.nilType)));
                } else if (typeChar == 'P') {
                    typeStack.push(new BTupleType(memberTypes));
                }
                return index + 1;
            case 'N':
                typeStack.push(this.symTable.nilType);
                return index + 1;
            default:
                throw new BLangCompilerException("unsupported base type char: " + ch);
        }
    }

    private BType getBTypeFromDescriptor(String sig) {
        char ch = sig.charAt(0);
        switch (ch) {
            case 'I':
                return this.symTable.intType;
            case 'F':
                return this.symTable.floatType;
            case 'S':
                return this.symTable.stringType;
            case 'B':
                return this.symTable.booleanType;
            case 'Y':
                return this.symTable.typeDesc;
            case 'L':
                return this.symTable.blobType;
            case 'A':
                return this.symTable.anyType;
            case 'N':
                return this.symTable.nilType;
            case 'R':
                return getBuiltinRefTypeFromName(sig.substring(1, sig.length() - 1));
            case 'M':
                BType constrainedType = getBTypeFromDescriptor(sig.substring(1));
                if (constrainedType == this.symTable.anyType) {
                    return this.symTable.mapType;
                } else {
                    return new BMapType(TypeTags.MAP, constrainedType, null);
                }
            case 'J':
            case 'T':
            case 'D':
            case 'G':
            case 'H':
            case 'X':
                String typeName = sig.substring(1, sig.length() - 1);
                String[] parts = typeName.split(":");

                if (parts.length == 1) {
                    if (ch == 'J') {
                        return this.symTable.jsonType;
                    } else if (ch == 'D') {
                        return this.symTable.tableType;
                    } else if (ch == 'H') { //TODO:CHECK
                        return this.symTable.streamType;
                    }
                }

                String pkgPath = parts[0];
                String name = parts[1];
                BPackageSymbol pkgSymbol = lookupImportPackageSymbol(names.fromString(pkgPath));
                if (ch == 'J') {
                    return new BJSONType(TypeTags.JSON, lookupUserDefinedType(pkgSymbol, name), null);
                } else if (ch == 'X') {
                    return lookupUserDefinedType(pkgSymbol, name);
                } else if (ch == 'D') {
                    return new BTableType(TypeTags.TABLE, lookupUserDefinedType(pkgSymbol, name), null);
                } else if (ch == 'H') {
                    return new BStreamType(TypeTags.STREAM, lookupUserDefinedType(pkgSymbol, name), null);
                } else {
                    return lookupUserDefinedType(pkgSymbol, name);
                }
            case '[':
                BType elemType = getBTypeFromDescriptor(sig.substring(1));
                return new BArrayType(elemType);
            case 'U':
                // TODO : Fix this for type casting.
                return null;
            case 'O':
            case 'P':
                Stack<BType> typeStack = new Stack<BType>();
                createBTypeFromSig(sig.toCharArray(), 0, typeStack);
                return typeStack.pop();
            default:
                throw new BLangCompilerException("Unknown type signature: " + sig);
        }
    }

    private BPackageSymbol lookupImportPackageSymbol(Name packageName) {
        BSymbol symbol = lookupMemberSymbol(this.env.pkgSymbol.scope, packageName, SymTag.PACKAGE);
        if (symbol == this.symTable.notFoundSymbol) {
            throw new BLangCompilerException("Unknown imported package: " + packageName);
        }

        return (BPackageSymbol) symbol;
    }

    private BSymbol lookupMemberSymbol(Scope scope, Name name, int expSymTag) {
        Scope.ScopeEntry entry = scope.lookup(name);
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & expSymTag) != expSymTag) {
                entry = entry.next;
            } else {
                return entry.symbol;
            }
        }

        return symTable.notFoundSymbol;
    }

    private BType lookupUserDefinedType(BPackageSymbol packageSymbol, String typeName) {
        BSymbol typeSymbol = lookupMemberSymbol(packageSymbol.scope, names.fromString(typeName), SymTag.TYPE);
        if (typeSymbol == this.symTable.notFoundSymbol) {
            throw new BLangCompilerException("Unknown type name: " + typeName);
        }

        return typeSymbol.type;
    }

    private BType getBuiltinRefTypeFromName(String typeName) {
        BSymbol typeSymbol = lookupMemberSymbol(this.symTable.rootScope, names.fromString(typeName), SymTag.TYPE);
        if (typeSymbol == this.symTable.notFoundSymbol) {
            throw new BLangCompilerException("Unknown type name: " + typeName);
        }

        return typeSymbol.type;
    }

    private void assignInitFunctions() {
        BPackageSymbol pkgSymbol = this.env.pkgSymbol;
        PackageID pkgId = pkgSymbol.pkgID;
        Name initFuncName = names.merge(names.fromString(pkgId.bvmAlias()), Names.INIT_FUNCTION_SUFFIX);
        BSymbol initFuncSymbol = lookupMemberSymbol(pkgSymbol.scope, initFuncName, SymTag.FUNCTION);
        pkgSymbol.initFunctionSymbol = (BInvokableSymbol) initFuncSymbol;

        Name startFuncName = names.merge(names.fromString(pkgId.bvmAlias()), Names.START_FUNCTION_SUFFIX);
        BSymbol startFuncSymbol = lookupMemberSymbol(pkgSymbol.scope, startFuncName, SymTag.FUNCTION);
        pkgSymbol.startFunctionSymbol = (BInvokableSymbol) startFuncSymbol;

        Name stopFuncName = names.merge(names.fromString(pkgId.bvmAlias()), Names.STOP_FUNCTION_SUFFIX);
        BSymbol stopFuncSymbol = lookupMemberSymbol(pkgSymbol.scope, stopFuncName, SymTag.FUNCTION);
        pkgSymbol.stopFunctionSymbol = (BInvokableSymbol) stopFuncSymbol;
    }

    private void resolveTypes() {
        for (UnresolvedType unresolvedType : this.env.unresolvedTypes) {
            BType type = getBTypeFromDescriptor(unresolvedType.typeSig);
            unresolvedType.completer.accept(type);
        }
    }

    /**
     * This class holds compiled package specific information during the symbol enter phase of the compiled package.
     *
     * @since 0.970.0
     */
    private static class CompiledPackageSymbolEnv {
        PackageID requestedPackageId;
        PackageRepository loadedRepository;
        BPackageSymbol pkgSymbol;
        ConstantPoolEntry[] constantPool;
        List<UnresolvedType> unresolvedTypes;

        CompiledPackageSymbolEnv() {
            this.unresolvedTypes = new ArrayList<>();
        }
    }

    private static class UnresolvedType {
        String typeSig;
        Consumer<BType> completer;

        UnresolvedType(String typeSig, Consumer<BType> completer) {
            this.typeSig = typeSig;
            this.completer = completer;
        }
    }
}

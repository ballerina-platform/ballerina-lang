/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.bir.model.VisibilityFlags;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.ByteCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.FloatCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.IntegerCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.PackageCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.TaintRecord;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.BIRPackageFile;
import org.wso2.ballerinalang.util.Flags;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static org.wso2.ballerinalang.util.LambdaExceptionUtils.rethrow;

/**
 * This class is responsible for reading the compiled package file (bir) and creating a package symbol.
 * <p>
 *
 * @since 0.995.0
 */
public class BIRPackageSymbolEnter {
    private final PackageLoader packageLoader;
    private final SymbolTable symTable;
    private final Names names;
    private final BLangDiagnosticLog dlog;
    private BIRTypeReader typeReader;

    private BIRPackageSymbolEnv env;
    private List<BStructureTypeSymbol> structureTypes; // TODO find a better way
    private BStructureTypeSymbol currentStructure = null;

    private static final CompilerContext.Key<BIRPackageSymbolEnter> COMPILED_PACKAGE_SYMBOL_ENTER_KEY =
            new CompilerContext.Key<>();

    public static BIRPackageSymbolEnter getInstance(CompilerContext context) {
        BIRPackageSymbolEnter packageReader = context.get(COMPILED_PACKAGE_SYMBOL_ENTER_KEY);
        if (packageReader == null) {
            packageReader = new BIRPackageSymbolEnter(context);
        }

        return packageReader;
    }

    private BIRPackageSymbolEnter(CompilerContext context) {
        context.put(COMPILED_PACKAGE_SYMBOL_ENTER_KEY, this);

        this.packageLoader = PackageLoader.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public BPackageSymbol definePackage(PackageID packageId,
                                        RepoHierarchy packageRepositoryHierarchy,
                                        byte[] packageBinaryContent) {
        BPackageSymbol pkgSymbol = definePackage(packageId, packageRepositoryHierarchy,
                new ByteArrayInputStream(packageBinaryContent));

        // Strip magic value (4 bytes) and the version (2 bytes) off from the binary content of the package.
        byte[] modifiedPkgBinaryContent = Arrays.copyOfRange(
                packageBinaryContent, 8, packageBinaryContent.length);
        pkgSymbol.birPackageFile = new CompiledBinaryFile.BIRPackageFile(modifiedPkgBinaryContent);
        SymbolEnv builtinEnv = this.symTable.pkgEnvMap.get(symTable.builtInPackageSymbol);
        SymbolEnv pkgEnv = SymbolEnv.createPkgEnv(null, pkgSymbol.scope, builtinEnv);
        this.symTable.pkgEnvMap.put(pkgSymbol, pkgEnv);
        return pkgSymbol;
    }

    private BPackageSymbol definePackage(PackageID packageId,
                                        RepoHierarchy packageRepositoryHierarchy,
                                        InputStream programFileInStream) {
        // TODO packageID --> package to be loaded. this is required for error reporting..
        try (DataInputStream dataInStream = new DataInputStream(programFileInStream)) {
            BIRPackageSymbolEnv prevEnv = this.env;
            this.env = new BIRPackageSymbolEnv();
            this.env.requestedPackageId = packageId;
            this.env.repoHierarchy = packageRepositoryHierarchy;
            BPackageSymbol pkgSymbol = definePackage(dataInStream);
            this.env = prevEnv;
            return pkgSymbol;
        } catch (IOException e) {
            // TODO dlog.error();
            throw new BLangCompilerException(e.getMessage(), e);
            //            return null;
        } catch (Throwable e) {
            // TODO format error
            throw new BLangCompilerException(e.getMessage(), e);
            //            return null;
        }
    }

    private BPackageSymbol definePackage(DataInputStream dataInStream) throws IOException {
        this.typeReader = new BIRTypeReader(dataInStream); //TODO find a better approach than this
        byte[] magic = new byte[4];
        dataInStream.read(magic, 0, 4);
        if (!Arrays.equals(magic, BIRPackageFile.BIR_MAGIC)) {
            // TODO dlog.error() with package name
            throw new BLangCompilerException("invalid magic number " + Arrays.toString(magic));
        }

        int version = dataInStream.readInt();
        if (version != BIRPackageFile.BIR_VERSION) {
            // TODO dlog.error() with package name
            throw new BLangCompilerException("unsupported program file version " + version);
        }

        // Read constant pool entries of the package info.
        this.env.constantPool = readConstantPool(dataInStream);

        int pkgCPIndex = dataInStream.readInt();
        return definePackage(dataInStream, pkgCPIndex);
    }

    private BPackageSymbol definePackage(DataInputStream dataInStream, int pkgCpIndex) throws IOException {

        PackageCPEntry pkgCpEntry = (PackageCPEntry) this.env.constantPool[pkgCpIndex];

        String orgName = ((StringCPEntry) this.env.constantPool[pkgCpEntry.orgNameCPIndex]).value;
        String pkgName = ((StringCPEntry) this.env.constantPool[pkgCpEntry.pkgNameCPIndex]).value;
        String pkgVersion = ((StringCPEntry) this.env.constantPool[pkgCpEntry.versionCPIndex]).value;

        PackageID pkgId = createPackageID(orgName, pkgName, pkgVersion);
        this.env.pkgSymbol = Symbols.createPackageSymbol(pkgId, this.symTable);

        // TODO Validate this pkdID with the requestedPackageID available in the env.

        // Define import packages.
        defineSymbols(dataInStream, rethrow(this::defineImportPackage));

        // Define typeDescRef definitions.
        this.structureTypes = new ArrayList<>();
        defineSymbols(dataInStream, rethrow(this::defineTypeDef));

        // Define package level variables.
        defineSymbols(dataInStream, rethrow(this::definePackageLevelVariables));

        defineAttachedFunctions(dataInStream);

        // Define constants.
//        defineSymbols(dataInStream, rethrow(this::defineConstants));

        // Define functions.
        defineSymbols(dataInStream, rethrow(this::defineFunction));

        // Define annotations.
        defineSymbols(dataInStream, rethrow(this::defineAnnotations));

        // Define constants.
        dataInStream.readLong(); //read and ignore constant byte chunk length.
        defineSymbols(dataInStream, rethrow(this::defineConstant));

        return this.env.pkgSymbol;
    }

    private void defineAttachedFunctions(DataInputStream dataInStream) throws IOException {
        for (BStructureTypeSymbol structureTypeSymbol : this.structureTypes) {
            this.currentStructure = structureTypeSymbol;
            defineSymbols(dataInStream, rethrow(this::defineFunction));
        }
        this.currentStructure = null;
    }

    private CPEntry[] readConstantPool(DataInputStream dataInStream) throws IOException {
        int constantPoolSize = dataInStream.readInt();
        CPEntry[] constantPool = new CPEntry[constantPoolSize];
        for (int i = 0; i < constantPoolSize; i++) {
            byte cpTag = dataInStream.readByte();
            CPEntry.Type cpEntryType = CPEntry.Type.values()[cpTag - 1];
            constantPool[i] = readCPEntry(dataInStream, constantPool, cpEntryType);
        }
        return constantPool;
    }

    private CPEntry readCPEntry(DataInputStream dataInStream,
                                CPEntry[] constantPool,
                                CPEntry.Type cpEntryType) throws IOException {
        switch (cpEntryType) {
            case CP_ENTRY_INTEGER:
                return new CPEntry.IntegerCPEntry(dataInStream.readLong());
            case CP_ENTRY_FLOAT:
                return new CPEntry.FloatCPEntry(dataInStream.readDouble());
            case CP_ENTRY_BOOLEAN:
                return new CPEntry.BooleanCPEntry(dataInStream.readBoolean());
            case CP_ENTRY_STRING:
                int length = dataInStream.readInt();
                String strValue = null;

                // If the length of the bytes is -1, that means no UTF value has been written.
                // i.e: string value represented by the UTF should be null.
                // Therefore we read the UTF value only if the length >= 0.
                if (length >= 0) {
                    byte[] bytes = new byte[length];
                    dataInStream.read(bytes, 0, length);
                    strValue = new String(bytes);
                }
                return new CPEntry.StringCPEntry(strValue);
            case CP_ENTRY_PACKAGE:
                return new CPEntry.PackageCPEntry(dataInStream.readInt(),
                        dataInStream.readInt(), dataInStream.readInt());
            default:
                throw new IllegalStateException("unsupported constant pool entry type: " +
                        cpEntryType.name());
        }
    }

    private void defineSymbols(DataInputStream dataInStream,
                               Consumer<DataInputStream> symbolDefineFunc) throws IOException {
        int symbolCount = dataInStream.readInt();
        for (int i = 0; i < symbolCount; i++) {
            symbolDefineFunc.accept(dataInStream);
        }
    }

    // TODO do we need to load all the import packages of a compiled package.
    private void defineImportPackage(DataInputStream dataInStream) throws IOException {
        String orgName = getStringCPEntryValue(dataInStream);
        String pkgName = getStringCPEntryValue(dataInStream);
        String pkgVersion = getStringCPEntryValue(dataInStream);
        PackageID importPkgID = createPackageID(orgName, pkgName, pkgVersion);
        BPackageSymbol importPackageSymbol = packageLoader.loadPackageSymbol(importPkgID, this.env.pkgSymbol.pkgID,
                this.env.repoHierarchy);
        //TODO: after balo_change try to not to add to scope, it's duplicated with 'imports'
        // Define the import package with the alias being the package name
        this.env.pkgSymbol.scope.define(importPkgID.name, importPackageSymbol);
        this.env.pkgSymbol.imports.add(importPackageSymbol);
    }

    private void defineFunction(DataInputStream dataInStream) throws IOException {
        skipPosition(dataInStream); // Position details are skipped
        // Consider attached functions.. remove the first variable
        String funcName = getStringCPEntryValue(dataInStream);
        int flags = 0;

        flags = dataInStream.readByte() == 1 ? flags | Flags.NATIVE : flags;
        flags = dataInStream.readByte() == 1 ? flags | Flags.INTERFACE : flags;

        flags = visibilityAsMask(flags, dataInStream.readByte());

        BInvokableType funcType = (BInvokableType) typeReader.readType();
        BInvokableSymbol invokableSymbol = Symbols.createFunctionSymbol(flags, names.fromString(funcName),
                this.env.pkgSymbol.pkgID, funcType, this.env.pkgSymbol, Symbols.isFlagOn(flags, Flags.NATIVE));
        Scope scopeToDefine = this.env.pkgSymbol.scope;

        if (this.currentStructure != null) {
            BType attachedType = this.currentStructure.type;

            // Update the symbol
            invokableSymbol.owner = attachedType.tsymbol;
            invokableSymbol.name =
                    names.fromString(Symbols.getAttachedFuncSymbolName(attachedType.tsymbol.name.value, funcName));
            if (attachedType.tag == TypeTags.OBJECT || attachedType.tag == TypeTags.RECORD) {
                if (attachedType.tag == TypeTags.OBJECT) {
                    scopeToDefine = ((BObjectTypeSymbol) attachedType.tsymbol).methodScope;
                } else {
                    scopeToDefine = attachedType.tsymbol.scope;
                }
                BAttachedFunction attachedFunc =
                        new BAttachedFunction(names.fromString(funcName), invokableSymbol, funcType);
                BStructureTypeSymbol structureTypeSymbol = (BStructureTypeSymbol) attachedType.tsymbol;
                if (Names.OBJECT_INIT_SUFFIX.value.equals(funcName)
                        || funcName.equals(Names.INIT_FUNCTION_SUFFIX.value)) {
                    structureTypeSymbol.attachedFuncs.add(attachedFunc);
                    structureTypeSymbol.initializerFunc = attachedFunc;
                }
            }
        }

        // set parameter symbols to the function symbol
        setParamSymbols(invokableSymbol, dataInStream);

        // set taint table to the function symbol
        readTaintTable(invokableSymbol, dataInStream);
//
//        setDocumentation(invokableSymbol, attrDataMap);


        dataInStream.skip(dataInStream.readLong()); // read and skip method body

        scopeToDefine.define(invokableSymbol.name, invokableSymbol);
    }

    private void skipPosition(DataInputStream dataInStream) throws IOException {
        // TODO find a better way to skip this
        dataInStream.readInt();
        dataInStream.readInt();
        dataInStream.readInt();
        dataInStream.readInt();
        dataInStream.readInt();
    }

    private int visibilityAsMask(int flags, byte visibility) {
        switch (visibility) {
            case VisibilityFlags.PACKAGE_PRIVATE:
                return flags;
            case VisibilityFlags.PRIVATE:
                return flags | Flags.PRIVATE;
            case VisibilityFlags.PUBLIC:
                return flags | Flags.PUBLIC;
            case VisibilityFlags.OPTIONAL:
                return flags | Flags.OPTIONAL;
        }
        return flags;
    }

    private void defineTypeDef(DataInputStream dataInStream) throws IOException {
        skipPosition(dataInStream);
        String typeDefName = getStringCPEntryValue(dataInStream);

        int flags = 0;

        flags = visibilityAsMask(flags, dataInStream.readByte());

        BType type = typeReader.readType();

        // Temp solution to add abstract flag if available TODO find a better approach
        flags = Symbols.isFlagOn(type.tsymbol.flags, Flags.ABSTRACT) ? flags | Flags.ABSTRACT : flags;

        BTypeSymbol symbol = type.tsymbol;
        symbol.type = type;


        symbol.name = names.fromString(typeDefName);
        symbol.pkgID = this.env.pkgSymbol.pkgID;
        symbol.flags = flags;

//        setDocumentation(typeDefSymbol, attrDataMap); //TODO fix

        if (type.tag == TypeTags.RECORD || type.tag == TypeTags.OBJECT) {
            this.structureTypes.add((BStructureTypeSymbol) symbol);
        }

        this.env.pkgSymbol.scope.define(symbol.name, symbol);
    }

    private void defineAnnotations(DataInputStream dataInStream) throws IOException {
        String name = getStringCPEntryValue(dataInStream);

        int flags = 0;
        flags = visibilityAsMask(flags, dataInStream.readByte());

        int attachPoints = dataInStream.readInt();
        BType annotationType = typeReader.readType();

        BAnnotationSymbol annotationSymbol = Symbols.createAnnotationSymbol(flags, attachPoints, names.fromString(name),
                this.env.pkgSymbol.pkgID, null, this.env.pkgSymbol);
        annotationSymbol.type = new BAnnotationType(annotationSymbol);

        this.env.pkgSymbol.scope.define(annotationSymbol.name, annotationSymbol);
        if (annotationType != symTable.noType) { //TODO fix properly
            annotationSymbol.attachedType = annotationType.tsymbol;
        }
    }

    private void defineConstant(DataInputStream dataInStream) throws IOException {
        String constantName = getStringCPEntryValue(dataInStream);
        int flags = 0;
        flags = visibilityAsMask(flags, dataInStream.readByte());
        BType finiteType = typeReader.readType();
        BType valueType = typeReader.readType();

        // Get the simple literal value.
        Object object = readLiteralValue(dataInStream, valueType);

        Scope enclScope = this.env.pkgSymbol.scope;

        // Create the constant symbol.
        BConstantSymbol constantSymbol = new BConstantSymbol(flags, names.fromString(constantName),
                this.env.pkgSymbol.pkgID, finiteType, valueType, enclScope.owner);
        constantSymbol.literalValue = object;
        constantSymbol.literalValueTypeTag = valueType.tag;

        // Define constant.
        enclScope.define(constantSymbol.name, constantSymbol);
    }

    private Object readLiteralValue(DataInputStream dataInStream, BType valueType) throws IOException {
        switch (valueType.tag) {
            case TypeTags.INT:
                int integerCpIndex = dataInStream.readInt();
                IntegerCPEntry integerCPEntry = (IntegerCPEntry) this.env.constantPool[integerCpIndex];
                return integerCPEntry.value;
            case TypeTags.BYTE:
                int byteCpIndex = dataInStream.readInt();
                ByteCPEntry byteCPEntry = (ByteCPEntry) this.env.constantPool[byteCpIndex];
                return byteCPEntry.value;
            case TypeTags.FLOAT:
                int floatCpIndex = dataInStream.readInt();
                FloatCPEntry floatCPEntry = (FloatCPEntry) this.env.constantPool[floatCpIndex];
                return Double.toString(floatCPEntry.value);
            case TypeTags.STRING:
            case TypeTags.DECIMAL:
                return getStringCPEntryValue(dataInStream);
            case TypeTags.BOOLEAN:
                return dataInStream.readByte() == 1;
            case TypeTags.NIL:
                return null;
            default:
                // TODO implement for other types
                throw new RuntimeException("unexpected type: " + valueType);
        }
    }

    private void definePackageLevelVariables(DataInputStream dataInStream) throws IOException {
        dataInStream.readByte(); // Read and ignore the kind as it is anyway global variable
        String varName = getStringCPEntryValue(dataInStream);
        int flags = visibilityAsMask(0, dataInStream.readByte());

        // Create variable symbol
        BType varType = typeReader.readType();
        Scope enclScope = this.env.pkgSymbol.scope;
        BVarSymbol varSymbol;

        if (varType.tag == TypeTags.INVOKABLE) {
            // Here we don't set the required-params, defaultable params and the rest param of
            // the symbol. Because, for the function pointers we directly read the param types
            // from the varType (i.e: from InvokableType), and assumes it can have only required
            // params.
            varSymbol = new BInvokableSymbol(SymTag.VARIABLE, flags, names.fromString(varName),
                    this.env.pkgSymbol.pkgID, varType, enclScope.owner);
        } else {
            varSymbol = new BVarSymbol(flags, names.fromString(varName), this.env.pkgSymbol.pkgID, varType,
                    enclScope.owner);
            if (Symbols.isFlagOn(varType.tsymbol.flags, Flags.CLIENT)) {
                varSymbol.tag = SymTag.ENDPOINT;
            }
        }

//        setDocumentation(varSymbol, attrDataMap); //TODO Fix

        enclScope.define(varSymbol.name, varSymbol);
    }

    private void setParamSymbols(BInvokableSymbol invokableSymbol, DataInputStream dataInStream)
            throws IOException {

        int requiredParamCount = dataInStream.readInt();

        for (int i = 0; i < requiredParamCount; i++) {
            String paramName = getStringCPEntryValue(dataInStream);
            BInvokableType invokableType = (BInvokableType) invokableSymbol.type;
            BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(paramName), this.env.pkgSymbol.pkgID,
                    invokableType.paramTypes.get(i), invokableSymbol);
            invokableSymbol.params.add(varSymbol);
        }

        int defaultableParamCount = dataInStream.readInt();

        for (int i = requiredParamCount; i < defaultableParamCount + requiredParamCount; i++) {
            String paramName = getStringCPEntryValue(dataInStream);
            BInvokableType invokableType = (BInvokableType) invokableSymbol.type;
            BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(paramName), this.env.pkgSymbol.pkgID,
                    invokableType.paramTypes.get(i), invokableSymbol);
            invokableSymbol.defaultableParams.add(varSymbol);
        }

        if (dataInStream.readBoolean()) { //if rest param exist
            String paramName = getStringCPEntryValue(dataInStream);
            BInvokableType invokableType = (BInvokableType) invokableSymbol.type;
            BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(paramName), this.env.pkgSymbol.pkgID,
                    invokableType.paramTypes.get(requiredParamCount + defaultableParamCount), invokableSymbol);
            invokableSymbol.restParam = varSymbol;
        }

        boolean hasReceiver = dataInStream.readBoolean(); //if receiver type is written, read and ignore
        if (hasReceiver) {
            typeReader.readType();
        }
    }

    /**
     * Set taint table to the invokable symbol.
     *
     * @param invokableSymbol Invokable symbol
     * @param dataInStream    Input stream
     * @throws IOException
     */
    private void readTaintTable(BInvokableSymbol invokableSymbol, DataInputStream dataInStream)
            throws IOException {
        long length = dataInStream.readLong();
        if (length <= 0) {
            return;
        }
        int rowCount = dataInStream.readShort();
        int columnCount = dataInStream.readShort();

        // Extract and set taint table to the symbol
        invokableSymbol.taintTable = new HashMap<>();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            int paramIndex = dataInStream.readShort();
            TaintRecord.TaintedStatus returnTaintedStatus =
                    convertByteToTaintedStatus(dataInStream.readByte());
            List<TaintRecord.TaintedStatus> parameterTaintedStatusList = new ArrayList<>();
            for (int columnIndex = 1; columnIndex < columnCount; columnIndex++) {
                parameterTaintedStatusList.add(convertByteToTaintedStatus(dataInStream.readByte()));
            }
            TaintRecord taintRecord = new TaintRecord(returnTaintedStatus, parameterTaintedStatusList);
            invokableSymbol.taintTable.put(paramIndex, taintRecord);
        }
    }

    private TaintRecord.TaintedStatus convertByteToTaintedStatus(byte readByte) {
        return EnumSet.allOf(TaintRecord.TaintedStatus.class).stream()
                .filter(taintedStatus -> readByte == taintedStatus.getByteValue()).findFirst().get();
    }

    // private utility methods
    //TODO rename this method to stringcp
    private String getStringCPEntryValue(DataInputStream dataInStream) throws IOException {
        int pkgNameCPIndex = dataInStream.readInt();
        StringCPEntry stringCPEntry = (StringCPEntry) this.env.constantPool[pkgNameCPIndex];
        return stringCPEntry.value;
    }

    private PackageID createPackageID(String orgName, String pkgName, String pkgVersion) {
        if (orgName == null || orgName.isEmpty()) {
            throw new BLangCompilerException("invalid module name '" + pkgName + "' in compiled package file");
        }

        return new PackageID(names.fromString(orgName),
                names.fromString(pkgName),
                names.fromString(pkgVersion));
    }

    /**
     * This class holds compiled package specific information during the symbol enter phase of the compiled package.
     *
     * @since 0.970.0
     */
    private static class BIRPackageSymbolEnv {
        PackageID requestedPackageId;
        RepoHierarchy repoHierarchy;
        BPackageSymbol pkgSymbol;
        CPEntry[] constantPool;
        List<UnresolvedType> unresolvedTypes;

        BIRPackageSymbolEnv() {
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

    private class BIRTypeReader {
        public static final int TYPE_TAG_SELF = 50;
        public static final int SERVICE_TYPE_TAG = 51;
        private DataInputStream inputStream;
        private LinkedList<Object> compositeStack = new LinkedList<>();

        public BIRTypeReader(DataInputStream inputStream) {
            this.inputStream = inputStream;
        }

        public BType readType() throws IOException {
            byte tag = inputStream.readByte();
            switch (tag) {
                case TypeTags.INT:
                    return symTable.intType;
                case TypeTags.BYTE:
                    return symTable.byteType;
                case TypeTags.FLOAT:
                    return symTable.floatType;
                case TypeTags.DECIMAL:
                    return symTable.decimalType;
                case TypeTags.STRING:
                    return symTable.stringType;
                case TypeTags.BOOLEAN:
                    return symTable.booleanType;
                // All the above types are values type
                case TypeTags.JSON:
                    return symTable.jsonType;
                case TypeTags.XML:
                    return symTable.xmlType;
                case TypeTags.TABLE:
                    BTableType bTableType = new BTableType(TypeTags.TABLE, null, symTable.tableType.tsymbol);
                    bTableType.constraint = readType();
                    return bTableType;
                case TypeTags.NIL:
                    return symTable.nilType;
                case TypeTags.ANYDATA:
                    return symTable.anydataType;
                case TypeTags.RECORD:
                    String name = getStringCPEntryValue(inputStream);
                    BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                            names.fromString(name), env.pkgSymbol.pkgID, null, env.pkgSymbol);
                    recordSymbol.scope = new Scope(recordSymbol);
                    BRecordType recordType = new BRecordType(recordSymbol);
                    recordSymbol.type = recordType;

                    compositeStack.push(recordType);

                    recordType.sealed = inputStream.readBoolean();
                    recordType.restFieldType = readType();

                    int recordFields = inputStream.readInt();
                    for (int i = 0; i < recordFields; i++) {
                        String fieldName = getStringCPEntryValue(inputStream);
                        int fieldFlags = 0;
                        fieldFlags = visibilityAsMask(fieldFlags, inputStream.readByte());
                        BType fieldType = readType();
                        BVarSymbol varSymbol = new BVarSymbol(fieldFlags, names.fromString(fieldName),
                                recordSymbol.pkgID, fieldType, recordSymbol.scope.owner);
                        recordSymbol.scope.define(varSymbol.name, varSymbol);
                    }

                    // read record init function
                    getStringCPEntryValue(inputStream);
                    inputStream.readByte();
                    readType();

//                    setDocumentation(varSymbol, attrData); // TODO fix

                    Object poppedRecordType = compositeStack.pop();
                    assert poppedRecordType == recordType;
                    return recordType;
                case TypeTags.TYPEDESC:
                    return symTable.typeDesc;
                case TypeTags.STREAM:
                    BStreamType bStreamType = new BStreamType(TypeTags.STREAM, null, symTable.streamType.tsymbol);
                    bStreamType.constraint = readType();
                    return bStreamType;
                case TypeTags.MAP:
                    BMapType bMapType = new BMapType(TypeTags.MAP, null, symTable.mapType.tsymbol);
                    bMapType.constraint = readType();
                    return bMapType;
                case TypeTags.INVOKABLE:
                    BTypeSymbol tsymbol = Symbols.createTypeSymbol(SymTag.FUNCTION_TYPE,
                            Flags.asMask(EnumSet.of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null,
                            env.pkgSymbol.owner);
                    BInvokableType bInvokableType = new BInvokableType(null, null, tsymbol);
                    int paramCount = inputStream.readInt();
                    List<BType> paramTypes = new ArrayList<>();
                    for (int i = 0; i < paramCount; i++) {
                        paramTypes.add(readType());
                    }
                    BType retType = readType();
                    bInvokableType.paramTypes = paramTypes;
                    bInvokableType.retType = retType;
                    return bInvokableType;
                // All the above types are branded types
                case TypeTags.ANY:
                    return symTable.anyType;
                case TypeTags.ENDPOINT:
                    // TODO fix
                    break;
                case TypeTags.ARRAY:
                    byte state = inputStream.readByte();
                    int size = inputStream.readInt();
                    BTypeSymbol arrayTypeSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE, Flags.asMask(EnumSet
                            .of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
                    BArrayType bArrayType = new BArrayType(null, arrayTypeSymbol, size, BArrayState.valueOf(state));
                    bArrayType.eType = readType();
                    return bArrayType;
                case TypeTags.UNION:
                    BTypeSymbol unionTypeSymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, Flags.asMask(EnumSet
                            .of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
                    BUnionType unionType = BUnionType.create(unionTypeSymbol,
                            new LinkedHashSet<>()); //TODO improve(useless second param)
                    int unionMemberCount = inputStream.readInt();
                    for (int i = 0; i < unionMemberCount; i++) {
                        unionType.add(readType());
                    }
                    return unionType;
                case TypeTags.PACKAGE:
                    // TODO fix
                    break;
                case TypeTags.NONE:
                    return symTable.noType;
                case TypeTags.VOID:
                    // TODO fix
                    break;
                case TypeTags.XMLNS:
                    // TODO fix
                    break;
                case TypeTags.ANNOTATION:
                    // TODO fix
                    break;
                case TypeTags.SEMANTIC_ERROR:
                    // TODO fix
                    break;
                case TypeTags.ERROR:
                    BTypeSymbol errorSymbol = new BErrorTypeSymbol(SymTag.ERROR, Flags.PUBLIC, Names.EMPTY,
                            env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
                    BErrorType errorType = new BErrorType(errorSymbol);
                    compositeStack.push(errorType);
                    BType reasonType = readType();
                    BType detailsType = readType();
                    errorType.reasonType = reasonType;
                    errorType.detailType = detailsType;
                    errorSymbol.type = errorType;
                    Object poppedErrorType = compositeStack.pop();
                    assert poppedErrorType == errorType;
                    return errorType;
                case TypeTags.ITERATOR:
                    // TODO fix
                    break;
                case TypeTags.TUPLE:
                    BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet
                            .of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
                    BTupleType bTupleType = new BTupleType(tupleTypeSymbol, null);
                    int tupleMemberCount = inputStream.readInt();
                    List<BType> tupleMemberTypes = new ArrayList<>();
                    for (int i = 0; i < tupleMemberCount; i++) {
                        tupleMemberTypes.add(readType());
                    }
                    bTupleType.tupleTypes = tupleMemberTypes;
                    return bTupleType;
                case TypeTags.FUTURE:
                    BFutureType bFutureType = new BFutureType(TypeTags.FUTURE, null, symTable.futureType.tsymbol);
                    bFutureType.constraint = readType();
                    return bFutureType;
                case TypeTags.INTERMEDIATE_COLLECTION:
                    // TODO fix
                    break;
                case TypeTags.FINITE:
                    String finiteTypeName = getStringCPEntryValue(inputStream);
                    BTypeSymbol symbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, 0,
                            names.fromString(finiteTypeName), env.pkgSymbol.pkgID, null, env.pkgSymbol);
                    symbol.scope = new Scope(symbol);
                    BFiniteType finiteType = new BFiniteType(symbol);
                    symbol.type = finiteType;
                    int valueSpaceSize = inputStream.readInt();
                    for (int i = 0; i < valueSpaceSize; i++) {
                        defineValueSpace(inputStream, finiteType);
                    }
                    return finiteType;
                case TypeTags.OBJECT:
                    boolean service = inputStream.readByte() == 1;
                    String objName = getStringCPEntryValue(inputStream);
                    int objFlags = (inputStream.readBoolean() ? Flags.ABSTRACT : 0) | Flags.PUBLIC;
                    BObjectTypeSymbol objectSymbol = (BObjectTypeSymbol) Symbols.createObjectSymbol(objFlags,
                            names.fromString(objName), env.pkgSymbol.pkgID, null, env.pkgSymbol);
                    objectSymbol.scope = new Scope(objectSymbol);
                    objectSymbol.methodScope = new Scope(objectSymbol);
                    BObjectType objectType;
                    // Below is a temporary fix, need to fix this properly by using the type tag
                    if (service) {
                        objectType = new BServiceType(objectSymbol);
                    } else {
                        objectType = new BObjectType(objectSymbol);
                    }
                    objectSymbol.type = objectType;
                    compositeStack.push(objectType);
                    int fieldCount = inputStream.readInt();
                    for (int i = 0; i < fieldCount; i++) {
                        String fieldName = getStringCPEntryValue(inputStream);
                        int fieldFlags = 0;
                        fieldFlags = visibilityAsMask(fieldFlags, inputStream.readByte());
                        BType fieldType = readType();
                        BVarSymbol objectVarSymbol = new BVarSymbol(fieldFlags, names.fromString(fieldName),
                                objectSymbol.pkgID, fieldType, objectSymbol.scope.owner);
                        objectSymbol.scope.define(objectVarSymbol.name, objectVarSymbol);
//                        setDocumentation(varSymbol, attrData); // TODO fix
                    }
                    int funcCount = inputStream.readInt();
                    for (int i = 0; i < funcCount; i++) {
                        String funcName = getStringCPEntryValue(inputStream);
                        int funcFlags = 0;
                        funcFlags = visibilityAsMask(funcFlags, inputStream.readByte());
                        BInvokableType funcType = (BInvokableType) readType();
                        BInvokableSymbol invokableSymbol = Symbols.createFunctionSymbol(funcFlags,
                                names.fromString(funcName), env.pkgSymbol.pkgID, funcType,
                                env.pkgSymbol, Symbols.isFlagOn(objFlags, Flags.NATIVE));
                        invokableSymbol.retType = funcType.retType;

                        BAttachedFunction attachedFunc =
                                new BAttachedFunction(names.fromString(funcName), invokableSymbol, funcType);
                        if (!Names.OBJECT_INIT_SUFFIX.value.equals(funcName) &&
                                !funcName.equals(Names.INIT_FUNCTION_SUFFIX.value)) {
                            objectSymbol.attachedFuncs.add(attachedFunc);
                            if (Names.OBJECT_INIT_SUFFIX.value.equals(funcName)
                                    || funcName.equals(Names.INIT_FUNCTION_SUFFIX.value)) {
                                objectSymbol.initializerFunc = attachedFunc;
                            }
                        }

//                        setDocumentation(varSymbol, attrData); // TODO fix
                    }
                    Object poppedObjType = compositeStack.pop();
                    assert poppedObjType == objectType;
                    return objectType;
                case TypeTags.BYTE_ARRAY:
                    // TODO fix
                    break;
                case TypeTags.FUNCTION_POINTER:
                    // TODO fix
                    break;
                case TYPE_TAG_SELF:
                    int index = inputStream.readInt();
                    return (BType) compositeStack.get(index);
                case SERVICE_TYPE_TAG:
                    return symTable.anyServiceType;
//                case TypeTags.CHANNEL:

//                case TypeTags.SERVICE:

            }
            return null;
        }
    }

    private void defineValueSpace(DataInputStream dataInStream, BFiniteType finiteType) throws IOException {
        BType valueType = typeReader.readType();
        BLangLiteral litExpr = createLiteralBasedOnType(valueType);
        switch (valueType.tag) {
            case TypeTags.INT:
                int integerCpIndex = dataInStream.readInt();
                IntegerCPEntry integerCPEntry = (IntegerCPEntry) this.env.constantPool[integerCpIndex];
                litExpr.value = integerCPEntry.value;
                break;
            case TypeTags.BYTE:
                int byteCpIndex = dataInStream.readInt();
                ByteCPEntry byteCPEntry = (ByteCPEntry) this.env.constantPool[byteCpIndex];
                litExpr.value = byteCPEntry.value;
                break;
            case TypeTags.FLOAT:
                int floatCpIndex = dataInStream.readInt();
                FloatCPEntry floatCPEntry = (FloatCPEntry) this.env.constantPool[floatCpIndex];
                litExpr.value = Double.toString(floatCPEntry.value);
                break;
            case TypeTags.STRING:
            case TypeTags.DECIMAL:
                litExpr.value = getStringCPEntryValue(dataInStream);
                break;
            case TypeTags.BOOLEAN:
                litExpr.value = dataInStream.readByte() == 1;
                break;
            case TypeTags.NIL:
                break;
            default:
                throw new UnsupportedOperationException("finite type value is not supported for type: " + valueType);
        }

        litExpr.type = valueType;

        finiteType.valueSpace.add(litExpr);
    }

    private BLangLiteral createLiteralBasedOnType(BType valueType) {
        NodeKind nodeKind = valueType.tag <= TypeTags.DECIMAL ? NodeKind.NUMERIC_LITERAL : NodeKind.LITERAL;
        return nodeKind == NodeKind.LITERAL ? (BLangLiteral) TreeBuilder.createLiteralExpression() :
                (BLangLiteral) TreeBuilder.createNumericLiteralExpression();
    }
}

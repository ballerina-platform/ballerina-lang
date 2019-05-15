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
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.FloatCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.PackageCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
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
    private boolean defineFuncsInPkg = true;

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

        // Define services.
//        defineSymbols(dataInStream, rethrow(this::defineService));

        // Resolve unresolved types.
//        resolveTypes();

        // Read resource info entries.
//        defineSymbols(dataInStream, rethrow(this::defineResource));

        // Define constants.
//        defineSymbols(dataInStream, rethrow(this::defineConstants));

        // Define functions.
        defineSymbols(dataInStream, rethrow(this::defineFunction));

        // Define annotations.
        defineSymbols(dataInStream, rethrow(this::defineAnnotations));
//        assignInitFunctions();

        // Read package level attributes
//        readAttributes(dataInStream);

        return this.env.pkgSymbol;
    }

    private void defineAttachedFunctions(DataInputStream dataInStream) throws IOException {
        this.defineFuncsInPkg = false;
        for (BStructureTypeSymbol ignored : this.structureTypes) {
            defineSymbols(dataInStream, rethrow(this::defineFunction));
        }
        this.defineFuncsInPkg = true;
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
        String orgName = getUTF8CPEntryValue(dataInStream);
        String pkgName = getUTF8CPEntryValue(dataInStream);
        String pkgVersion = getUTF8CPEntryValue(dataInStream);
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
        String funcName = getUTF8CPEntryValue(dataInStream);
        int flags = 0;

        flags = dataInStream.readByte() == 1 ? flags | Flags.NATIVE : flags;
        flags = dataInStream.readByte() == 1 ? flags | Flags.INTERFACE : flags;

        flags = visibilityAsMask(flags, dataInStream.readByte());

        BInvokableType funcType = (BInvokableType) typeReader.readType();
        BInvokableSymbol invokableSymbol = Symbols.createFunctionSymbol(flags, names.fromString(funcName),
                this.env.pkgSymbol.pkgID, funcType, this.env.pkgSymbol, Symbols.isFlagOn(flags, Flags.NATIVE));
        Scope scopeToDefine = this.env.pkgSymbol.scope;

        // set parameter symbols to the function symbol
        setParamSymbols(invokableSymbol, dataInStream);
//
        // set taint table to the function symbol
        readTaintTable(invokableSymbol, dataInStream);
//
//        setDocumentation(invokableSymbol, attrDataMap);


        dataInStream.skip(dataInStream.readLong()); // read and skip method body

        if (this.defineFuncsInPkg) { //TODO find a better way?
            scopeToDefine.define(invokableSymbol.name, invokableSymbol);
        }
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
        }
        return flags;
    }

    private void defineTypeDef(DataInputStream dataInStream) throws IOException {
        skipPosition(dataInStream);
        String typeDefName = getUTF8CPEntryValue(dataInStream);

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

//        setDocumentation(typeDefSymbol, attrDataMap); //TODO fix - rajith

        if (type.tag == TypeTags.RECORD || type.tag == TypeTags.OBJECT) {
            this.structureTypes.add((BStructureTypeSymbol) symbol);
        }

        this.env.pkgSymbol.scope.define(symbol.name, symbol);
    }

    private void defineAnnotations(DataInputStream dataInStream) throws IOException {
        String name = getUTF8CPEntryValue(dataInStream);

        int flags = 0;
        flags = visibilityAsMask(flags, dataInStream.readByte());

        int attachPoints = dataInStream.readInt();
        BType annotationType = typeReader.readType();

        BSymbol annotationSymbol = Symbols.createAnnotationSymbol(flags, attachPoints, names.fromString(name),
                this.env.pkgSymbol.pkgID, null, this.env.pkgSymbol);
        annotationSymbol.type = new BAnnotationType((BAnnotationSymbol) annotationSymbol);

        this.env.pkgSymbol.scope.define(annotationSymbol.name, annotationSymbol);
        if (annotationType != symTable.noType) { //TODO fix properly
            ((BAnnotationSymbol) annotationSymbol).attachedType = annotationType.tsymbol;
        }
    }

//    private void defineService(DataInputStream dataInStream) throws IOException {
//        dataInStream.readInt();
//        dataInStream.readInt();
//        dataInStream.readInt();
//    }
//
//    private void defineResource(DataInputStream dataInStream) throws IOException {
//        int resourceCount = dataInStream.readShort();
//        for (int j = 0; j < resourceCount; j++) {
//            dataInStream.readInt();
//        }
//    }
//
//
//    private void defineConstants(DataInputStream dataInStream) throws IOException {
//        String constantName = getUTF8CPEntryValue(dataInStream);
//        String finiteTypeSig = getUTF8CPEntryValue(dataInStream);
//        BType finiteType = getBTypeFromDescriptor(finiteTypeSig);
//        String valueTypeSig = getUTF8CPEntryValue(dataInStream);
//        BType valueType = getBTypeFromDescriptor(valueTypeSig);
//
//        int flags = dataInStream.readInt();
//
//        // Create constant symbol.
//        Scope enclScope = this.env.pkgSymbol.scope;
//        BConstantSymbol constantSymbol = new BConstantSymbol(flags, names.fromString(constantName),
//                this.env.pkgSymbol.pkgID, finiteType, valueType, enclScope.owner);
//
//        enclScope.define(constantSymbol.name, constantSymbol);
//
//        Map<Kind, byte[]> attrDataMap = readAttributes(dataInStream);
//        setDocumentation(constantSymbol, attrDataMap);
//
//        // Read value of the constant and set it in the symbol.
//        BLangLiteral constantValue = getConstantValue(attrDataMap);
//        constantSymbol.literalValue = constantValue.value;
//        constantSymbol.literalValueType = constantValue.typeDescRef;
//        constantSymbol.literalValueTypeTag = constantValue.typeTag;
//    }

    private void definePackageLevelVariables(DataInputStream dataInStream) throws IOException {
        dataInStream.readByte(); // Read and ignore the kind as it is anyway global variable
        String varName = getUTF8CPEntryValue(dataInStream);
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

//        setDocumentation(varSymbol, attrDataMap); //TODO Fix - rajith

        enclScope.define(varSymbol.name, varSymbol);
    }

    private void setParamSymbols(BInvokableSymbol invokableSymbol, DataInputStream dataInStream)
            throws IOException {

        int requiredParamCount = dataInStream.readInt();

        for (int i = 0; i < requiredParamCount; i++) {
            String paramName = getUTF8CPEntryValue(dataInStream);
            BInvokableType invokableType = (BInvokableType) invokableSymbol.type;
            BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(paramName), this.env.pkgSymbol.pkgID,
                    invokableType.paramTypes.get(i), invokableSymbol);
            invokableSymbol.params.add(varSymbol);
        }

        int defaultableParamCount = dataInStream.readInt();

        for (int i = requiredParamCount; i < defaultableParamCount + requiredParamCount; i++) {
            String paramName = getUTF8CPEntryValue(dataInStream);
            BInvokableType invokableType = (BInvokableType) invokableSymbol.type;
            BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(paramName), this.env.pkgSymbol.pkgID,
                    invokableType.paramTypes.get(i), invokableSymbol);
            invokableSymbol.defaultableParams.add(varSymbol);
        }

        if (dataInStream.readBoolean()) { //if rest param exist
            String paramName = getUTF8CPEntryValue(dataInStream);
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

//    private void setDocumentation(BSymbol symbol, Map<Kind, byte[]> attrDataMap) throws IOException {
//        if (!attrDataMap.containsKey(Kind.DOCUMENT_ATTACHMENT_ATTRIBUTE)) {
//            return;
//        }
//
//        byte[] documentationBytes = attrDataMap.get(Kind.DOCUMENT_ATTACHMENT_ATTRIBUTE);
//        DataInputStream documentDataStream = new DataInputStream(new ByteArrayInputStream(documentationBytes));
//
//        String docDesc = getUTF8CPEntryValue(documentDataStream);
//
//        MarkdownDocAttachment docAttachment = new MarkdownDocAttachment();
//        docAttachment.description = docDesc;
//
//        int noOfParams = documentDataStream.readShort();
//        for (int i = 0; i < noOfParams; i++) {
//            String name = getUTF8CPEntryValue(documentDataStream);
//            String paramDesc = getUTF8CPEntryValue(documentDataStream);
//            MarkdownDocAttachment.Parameter parameter = new MarkdownDocAttachment.Parameter(name, paramDesc);
//            docAttachment.parameters.add(parameter);
//        }
//
//        boolean isReturnDocDescriptionAvailable = documentDataStream.readBoolean();
//        if (isReturnDocDescriptionAvailable) {
//            docAttachment.returnValueDescription = getUTF8CPEntryValue(documentDataStream);
//        }
//
//        symbol.markdownDocumentation = docAttachment;
//    }

    // private utility methods
    //TODO rename this method to stringcp
    private String getUTF8CPEntryValue(DataInputStream dataInStream) throws IOException {
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

//    private void assignInitFunctions() {
//        BPackageSymbol pkgSymbol = this.env.pkgSymbol;
//        PackageID pkgId = pkgSymbol.pkgID;
//        Name initFuncName = names.merge(names.fromString(pkgId.toString()), Names.INIT_FUNCTION_SUFFIX);
//        BSymbol initFuncSymbol = lookupMemberSymbol(pkgSymbol.scope, initFuncName, SymTag.FUNCTION);
//        pkgSymbol.initFunctionSymbol = (BInvokableSymbol) initFuncSymbol;
//
//        Name startFuncName = names.merge(names.fromString(pkgId.toString()), Names.START_FUNCTION_SUFFIX);
//        BSymbol startFuncSymbol = lookupMemberSymbol(pkgSymbol.scope, startFuncName, SymTag.FUNCTION);
//        pkgSymbol.startFunctionSymbol = (BInvokableSymbol) startFuncSymbol;
//
//        Name stopFuncName = names.merge(names.fromString(pkgId.toString()), Names.STOP_FUNCTION_SUFFIX);
//        BSymbol stopFuncSymbol = lookupMemberSymbol(pkgSymbol.scope, stopFuncName, SymTag.FUNCTION);
//        pkgSymbol.stopFunctionSymbol = (BInvokableSymbol) stopFuncSymbol;
//    }

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
                    String name = getUTF8CPEntryValue(inputStream);
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
                        String fieldName = getUTF8CPEntryValue(inputStream);
                        BType fieldType = readType();
                        BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(fieldName),
                                recordSymbol.pkgID, fieldType, recordSymbol.scope.owner);
                        recordSymbol.scope.define(varSymbol.name, varSymbol);
                    }

                    // read record init function
                    getUTF8CPEntryValue(inputStream);
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
                    BTypeSymbol symbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, 0, Names.EMPTY,
                            env.pkgSymbol.pkgID, null, env.pkgSymbol);
                    symbol.scope = new Scope(symbol);
                    BFiniteType finiteType = new BFiniteType(symbol);
                    symbol.type = finiteType;
                    int valueSpaceSize = inputStream.readInt();
                    for (int i = 0; i < valueSpaceSize; i++) {
                        defineValueSpace(inputStream, finiteType);
                    }
                    return finiteType;
                case TypeTags.OBJECT:
                    String objName = getUTF8CPEntryValue(inputStream);
                    int objFlags = (inputStream.readBoolean() ? Flags.ABSTRACT : 0) | Flags.PUBLIC;
                    BObjectTypeSymbol objectSymbol = (BObjectTypeSymbol) Symbols.createObjectSymbol(objFlags,
                            names.fromString(objName), env.pkgSymbol.pkgID, null, env.pkgSymbol);
                    objectSymbol.scope = new Scope(objectSymbol);
                    objectSymbol.methodScope = new Scope(objectSymbol);
                    BObjectType objectType = new BObjectType(objectSymbol);
                    objectSymbol.type = objectType;
                    compositeStack.push(objectType);
                    int fieldCount = inputStream.readInt();
                    for (int i = 0; i < fieldCount; i++) {
                        String fieldName = getUTF8CPEntryValue(inputStream);
                        int fieldFlags = 0;
                        fieldFlags = visibilityAsMask(fieldFlags, inputStream.readByte());
                        BType fieldType = readType();
                        BVarSymbol objectVarSymbol = new BVarSymbol(fieldFlags, names.fromString(fieldName),
                                objectSymbol.pkgID, fieldType, objectSymbol.scope.owner);
                        objectSymbol.scope.define(objectVarSymbol.name, objectVarSymbol);
//                        setDocumentation(varSymbol, attrData); // TODO fix - rajith
                    }
                    int funcCount = inputStream.readInt();
                    for (int i = 0; i < funcCount; i++) {
                        String funcName = getUTF8CPEntryValue(inputStream);
                        int funcFlags = 0;
                        funcFlags = visibilityAsMask(funcFlags, inputStream.readByte());
                        BInvokableType funcType = (BInvokableType) readType();
                        BInvokableSymbol invokableSymbol = Symbols.createFunctionSymbol(funcFlags,
                                names.fromString(funcName), env.pkgSymbol.pkgID, funcType,
                                env.pkgSymbol, Symbols.isFlagOn(objFlags, Flags.NATIVE));
                        invokableSymbol.retType = funcType.retType;
                        objectSymbol.methodScope.define(invokableSymbol.name, invokableSymbol);
//                        setDocumentation(varSymbol, attrData); // TODO fix - rajith
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
                case TypeTags.CHANNEL:
                    // TODO fix
                    break;
                case TypeTags.SERVICE:
                    return symTable.anyServiceType;

            }
            return null;
        }
    }

    private void defineValueSpace(DataInputStream dataInStream, BFiniteType finiteType) throws IOException {
        BType valueType = typeReader.readType();
        BLangLiteral litExpr = createLiteralBasedOnType(valueType);
        switch (valueType.tag) {
            case TypeTags.INT:
            case TypeTags.BYTE:
                litExpr.value = dataInStream.readInt();
                break;
            case TypeTags.FLOAT:
                int floatCpIndex = dataInStream.readInt();
                FloatCPEntry floatCPEntry = (FloatCPEntry) this.env.constantPool[floatCpIndex];
                litExpr.value = Double.toString(floatCPEntry.value);
                break;
            case TypeTags.STRING:
            case TypeTags.DECIMAL:
                litExpr.value = getUTF8CPEntryValue(dataInStream);
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

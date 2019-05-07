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
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.PackageCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
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
import java.util.LinkedHashSet;
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
    private BStructureTypeSymbol structureTypeSymbol;

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
        if (packageId.orgName.value.equals("ballerina")) { //TODO remove temp fix
            return null;
        }
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

        //TODO remove - rajith (temp to skip type defs)
        defineSymbols(dataInStream, rethrow(this::defineAttachedFunctions));


        // Define annotations.
//        defineSymbols(dataInStream, rethrow(this::defineAnnotations));

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
//        assignInitFunctions();

        // Read package level attributes
//        readAttributes(dataInStream);

        return this.env.pkgSymbol;
    }

    private void defineAttachedFunctions(DataInputStream dataInStream) throws IOException {
        for (BStructureTypeSymbol structureTypeSymbol : this.structureTypes) {
            this.structureTypeSymbol = structureTypeSymbol;
            defineSymbols(dataInStream, rethrow(this::defineFunction));
        }
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
//                    strValue = dataInStream.readUTF();
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

//        if (Symbols.isFlagOn(flags, Flags.ATTACHED)) {
//            int attachedToTypeRefCPIndex = dataInStream.readInt();
//            TypeRefCPEntry typeRefCPEntry = (TypeRefCPEntry) this.env.constantPool[attachedToTypeRefCPIndex];
//            UTF8CPEntry typeSigCPEntry = (UTF8CPEntry) this.env.constantPool[typeRefCPEntry.typeSigCPIndex];
//            BType attachedType = getBTypeFromDescriptor(typeSigCPEntry.getValue());
//
//            // Update the symbol
//            invokableSymbol.owner = attachedType.tsymbol;
//            invokableSymbol.name =
//                    names.fromString(Symbols.getAttachedFuncSymbolName(attachedType.tsymbol.name.value, funcName));
//            if (attachedType.tag == TypeTags.OBJECT || attachedType.tag == TypeTags.RECORD) {
//                if (attachedType.tag == TypeTags.OBJECT) {
//                    scopeToDefine = ((BObjectTypeSymbol) attachedType.tsymbol).methodScope;
//                } else {
//                    scopeToDefine = attachedType.tsymbol.scope;
//                }
//                BAttachedFunction attachedFunc =
//                        new BAttachedFunction(names.fromString(funcName), invokableSymbol, funcType);
//                BStructureTypeSymbol structureTypeSymbol = (BStructureTypeSymbol) attachedType.tsymbol;
//                structureTypeSymbol.attachedFuncs.add(attachedFunc);
//                if (Names.OBJECT_INIT_SUFFIX.value.equals(funcName)
//                        || funcName.equals(Names.INIT_FUNCTION_SUFFIX.value)) {
//                    structureTypeSymbol.initializerFunc = attachedFunc;
//                }
//            }
//        }


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
            case 0:
                return flags;
            case 1:
                return flags | Flags.PRIVATE;
            case 2:
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

        BTypeSymbol symbol = type.tsymbol.createLabelSymbol();
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

//    private void defineAnnotations(DataInputStream dataInStream) throws IOException {
//        String name = getUTF8CPEntryValue(dataInStream);
//        int flags = dataInStream.readInt();
//        int attachPoints = dataInStream.readInt();
//        int typeSig = dataInStream.readInt();
//
//        BSymbol annotationSymbol = Symbols.createAnnotationSymbol(flags, attachPoints, names.fromString(name),
//                this.env.pkgSymbol.pkgID, null, this.env.pkgSymbol);
//        annotationSymbol.typeDescRef = new BAnnotationType((BAnnotationSymbol) annotationSymbol);
//
//        this.env.pkgSymbol.scope.define(annotationSymbol.name, annotationSymbol);
//        if (typeSig > 0) {
//            UTF8CPEntry typeSigCPEntry = (UTF8CPEntry) this.env.constantPool[typeSig];
//            BType varType = getBTypeFromDescriptor(typeSigCPEntry.getValue());
//            ((BAnnotationSymbol) annotationSymbol).attachedType = varType.tsymbol;
//        }
//    }
//
//    private BObjectTypeSymbol readObjectTypeSymbol(DataInputStream dataInStream,
//                                                   String name, int flags) throws IOException {
//        BObjectTypeSymbol symbol = (BObjectTypeSymbol) Symbols.createObjectSymbol(flags, names.fromString(name),
//                this.env.pkgSymbol.pkgID, null, this.env.pkgSymbol);
//        symbol.scope = new Scope(symbol);
//        symbol.methodScope = new Scope(symbol);
//        BObjectType typeDescRef = new BObjectType(symbol);
//        symbol.typeDescRef = typeDescRef;
//
//        // Define Object Fields
//        defineSymbols(dataInStream, rethrow(dataInputStream ->
//                defineStructureField(dataInStream, symbol, typeDescRef)));
//
//        // Read and ignore attributes
//        readAttributes(dataInStream);
//
//        return symbol;
//    }
//
//    private BRecordTypeSymbol readRecordTypeSymbol(DataInputStream dataInStream,
//                                                   String name, int flags) throws IOException {
//        BRecordTypeSymbol symbol = Symbols.createRecordSymbol(flags, names.fromString(name),
//                this.env.pkgSymbol.pkgID, null,
//                this.env.pkgSymbol);
//        symbol.scope = new Scope(symbol);
//        BRecordType typeDescRef = new BRecordType(symbol);
//        symbol.typeDescRef = typeDescRef;
//
//        typeDescRef.sealed = dataInStream.readBoolean();
//        if (!typeDescRef.sealed) {
//            String restFieldTypeDesc = getUTF8CPEntryValue(dataInStream);
//            UnresolvedType restFieldType = new UnresolvedType(restFieldTypeDesc,
//                                                              restType -> typeDescRef.restFieldType = restType);
//            this.env.unresolvedTypes.add(restFieldType);
//        } else {
//            typeDescRef.restFieldType = symTable.noType;
//        }
//
//        // Define Object Fields
//        defineSymbols(dataInStream, rethrow(dataInputStream ->
//                defineStructureField(dataInStream, symbol, typeDescRef)));
//
//        // Read and ignore attributes
//        readAttributes(dataInStream);
//
//        return symbol;
//    }
//
//    private BTypeSymbol readFiniteTypeSymbol(DataInputStream dataInStream,
//                                             String name, int flags) throws IOException {
//        BTypeSymbol symbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, flags, names.fromString(name),
//                this.env.pkgSymbol.pkgID, null, this.env.pkgSymbol);
//        symbol.scope = new Scope(symbol);
//        BFiniteType finiteType = new BFiniteType(symbol);
//        symbol.typeDescRef = finiteType;
//
//        // Define Object Fields
//        defineSymbols(dataInStream, rethrow(dataInputStream ->
//                defineValueSpace(dataInStream, finiteType)));
//
//        return symbol;
//    }
//
//    private BTypeSymbol readLabelTypeSymbol(DataInputStream dataInStream,
//                                            String name, int flags) throws IOException {
//        String typeSig = getUTF8CPEntryValue(dataInStream);
//        BType typeDescRef = getBTypeFromDescriptor(typeSig);
//
//        BTypeSymbol symbol = typeDescRef.tsymbol.createLabelSymbol();
//        symbol.typeDescRef = typeDescRef;
//
//        symbol.name = names.fromString(name);
//        symbol.pkgID = this.env.pkgSymbol.pkgID;
//        symbol.flags = flags;
//
//        return symbol;
//    }
//
//    private void defineValueSpace(DataInputStream dataInStream, BFiniteType finiteType) throws IOException {
//        int typeDescCPIndex = dataInStream.readInt();
//        UTF8CPEntry typeDescCPEntry = (UTF8CPEntry) this.env.constantPool[typeDescCPIndex];
//        String typeDesc = typeDescCPEntry.getValue();
//
//        BLangLiteral litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
//
//        int valueCPIndex;
//        switch (typeDesc) {
//            case TypeDescriptor.SIG_BOOLEAN:
//                litExpr.value = dataInStream.readBoolean();
//                litExpr.typeTag = TypeTags.BOOLEAN;
//                break;
//            case TypeDescriptor.SIG_INT:
//                valueCPIndex = dataInStream.readInt();
//                IntegerCPEntry integerCPEntry = (IntegerCPEntry) this.env.constantPool[valueCPIndex];
//                litExpr.value = integerCPEntry.getValue();
//                litExpr.typeTag = TypeTags.INT;
//                break;
//            case TypeDescriptor.SIG_BYTE:
//                valueCPIndex = dataInStream.readInt();
//                ByteCPEntry byteCPEntry = (ByteCPEntry) this.env.constantPool[valueCPIndex];
//                litExpr.value = byteCPEntry.getValue();
//                litExpr.typeTag = TypeTags.BYTE;
//                break;
//            case TypeDescriptor.SIG_FLOAT:
//                valueCPIndex = dataInStream.readInt();
//                FloatCPEntry floatCPEntry = (FloatCPEntry) this.env.constantPool[valueCPIndex];
//                litExpr.value = Double.toString(floatCPEntry.getValue());
//                litExpr.typeTag = TypeTags.FLOAT;
//                break;
//            case TypeDescriptor.SIG_DECIMAL:
//                valueCPIndex = dataInStream.readInt();
//                UTF8CPEntry decimalEntry = (UTF8CPEntry) this.env.constantPool[valueCPIndex];
//                litExpr.value = decimalEntry.getValue();
//                litExpr.typeTag = TypeTags.DECIMAL;
//                break;
//            case TypeDescriptor.SIG_STRING:
//                valueCPIndex = dataInStream.readInt();
//                UTF8CPEntry stringCPEntry = (UTF8CPEntry) this.env.constantPool[valueCPIndex];
//                litExpr.value = stringCPEntry.getValue();
//                litExpr.typeTag = TypeTags.STRING;
//                break;
//            case TypeDescriptor.SIG_NULL:
//                litExpr.typeTag = TypeTags.NIL;
//                break;
//            default:
//                throw new BLangCompilerException("unknown default value typeDescRef " + typeDesc);
//        }
//
//        litExpr.typeDescRef = symTable.getTypeFromTag(litExpr.typeTag);
//
//        finiteType.valueSpace.add(litExpr);
//    }
//
//    private void defineStructureField(DataInputStream dataInStream,
//                                      BTypeSymbol objectSymbol,
//                                      BStructureType objectType) throws IOException {
//        String fieldName = getUTF8CPEntryValue(dataInStream);
//        String typeSig = getUTF8CPEntryValue(dataInStream);
//        int flags = dataInStream.readInt();
//        int memIndex = dataInStream.readInt();
//
//        BVarSymbol varSymbol = new BVarSymbol(flags, names.fromString(fieldName),
//                objectSymbol.pkgID, null, objectSymbol.scope.owner);
//        objectSymbol.scope.define(varSymbol.name, varSymbol);
//
//        // Read the default value attribute
//        Map<Kind, byte[]> attrData = readAttributes(dataInStream);
//
//        // The object field typeDescRef cannot be resolved now. Hence add it to the unresolved typeDescRef list.
//        UnresolvedType unresolvedFieldType = new UnresolvedType(typeSig, typeDescRef -> {
//            varSymbol.typeDescRef = typeDescRef;
//            varSymbol.varIndex = new RegIndex(memIndex, typeDescRef.tag);
//            // Here the field doesn't need the position as it comes from a compiled package.
//            BField structField = new BField(varSymbol.name, null, varSymbol);
//            objectType.fields.add(structField);
//        });
//
//        setDocumentation(varSymbol, attrData);
//
//        this.env.unresolvedTypes.add(unresolvedFieldType);
//    }
//
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
//
//    private BLangLiteral getConstantValue(Map<Kind, byte[]> attrDataMap) throws IOException {
//        // Constants must have a value attribute.
//        byte[] documentationBytes = attrDataMap.get(Kind.DEFAULT_VALUE_ATTRIBUTE);
//        DataInputStream documentDataStream = new DataInputStream(new ByteArrayInputStream(documentationBytes));
//        // Create a new literal.
//        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
//        // Read the value from the stream. We need to set `value`, `valueTag` and `typeDescRef` of the literal.
//        String typeDesc = getUTF8CPEntryValue(documentDataStream);
//        int valueCPIndex;
//        switch (typeDesc) {
//            case TypeDescriptor.SIG_BOOLEAN:
//                literal.value = documentDataStream.readBoolean();
//                literal.typeTag = TypeTags.BOOLEAN;
//                break;
//            case TypeDescriptor.SIG_INT:
//                valueCPIndex = documentDataStream.readInt();
//                IntegerCPEntry integerCPEntry = (IntegerCPEntry) this.env.constantPool[valueCPIndex];
//                literal.value = integerCPEntry.getValue();
//                literal.typeTag = TypeTags.INT;
//                break;
//            case TypeDescriptor.SIG_BYTE:
//                valueCPIndex = documentDataStream.readInt();
//                ByteCPEntry byteCPEntry = (ByteCPEntry) this.env.constantPool[valueCPIndex];
//                literal.value = byteCPEntry.getValue();
//                literal.typeTag = TypeTags.BYTE;
//                break;
//            case TypeDescriptor.SIG_FLOAT:
//                valueCPIndex = documentDataStream.readInt();
//                FloatCPEntry floatCPEntry = (FloatCPEntry) this.env.constantPool[valueCPIndex];
//                literal.value = floatCPEntry.getValue();
//                literal.typeTag = TypeTags.FLOAT;
//                break;
//            case TypeDescriptor.SIG_DECIMAL:
//                valueCPIndex = documentDataStream.readInt();
//                UTF8CPEntry decimalEntry = (UTF8CPEntry) this.env.constantPool[valueCPIndex];
//                literal.value = decimalEntry.getValue();
//                literal.typeTag = TypeTags.DECIMAL;
//                break;
//            case TypeDescriptor.SIG_STRING:
//                valueCPIndex = documentDataStream.readInt();
//                UTF8CPEntry stringCPEntry = (UTF8CPEntry) this.env.constantPool[valueCPIndex];
//                literal.value = stringCPEntry.getValue();
//                literal.typeTag = TypeTags.STRING;
//                break;
//            case TypeDescriptor.SIG_NULL:
//                literal.value = null;
//                literal.typeTag = TypeTags.NIL;
//                break;
//            default:
//                // Todo - Allow json and xml.
//                throw new RuntimeException("unknown constant value typeDescRef " + typeDesc);
//        }
//        literal.typeDescRef = symTable.getTypeFromTag(literal.typeTag);
//        return literal;
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

//    private Map<Kind, byte[]> readAttributes(DataInputStream dataInStream) throws IOException {
//        int attributesCount = dataInStream.readShort();
//        if (attributesCount == 0) {
//            return new HashMap<>();
//        }
//
//        Map<Kind, byte[]> attrDataMap = new HashMap<>(attributesCount);
//        for (int i = 0; i < attributesCount; i++) {
//            String attrName = getUTF8CPEntryValue(dataInStream);
//            Kind attrKind = Kind.fromString(attrName);
//            if (attrKind == null) {
//                // TODO use dlog....
//                throw new BLangCompilerException("unknown attribute kind " + attrName);
//            }
//
//            int noOfAttrDataBytes = dataInStream.readInt();
//            byte[] attrData = new byte[noOfAttrDataBytes];
//            int noOfBytesRead = dataInStream.read(attrData);
//            if (noOfAttrDataBytes != noOfBytesRead) {
//                // TODO This is and error..
//            }
//
//            attrDataMap.put(attrKind, attrData);
//        }
//        return attrDataMap;
//    }
//
//    /**
//     * Set parameter symbols to the invokable symbol.
//     *
//     * @param invokableSymbol Invokable symbol
//     * @param attrDataMap     Attribute data map
//     * @throws IOException
//     */
//    private void setParamSymbols(BInvokableSymbol invokableSymbol, Map<Kind, byte[]> attrDataMap)
//            throws IOException {
//
//        if (!attrDataMap.containsKey(Kind.PARAMETERS_ATTRIBUTE) ||
//                !attrDataMap.containsKey(Kind.LOCAL_VARIABLES_ATTRIBUTE)) {
//            return;
//        }
//
//        // Get parameter counts
//        byte[] paramData = attrDataMap.get(Kind.PARAMETERS_ATTRIBUTE);
//        DataInputStream paramDataInStream = new DataInputStream(new ByteArrayInputStream(paramData));
//        int requiredParamCount = paramDataInStream.readInt();
//        int defaultableParamCount = paramDataInStream.readInt();
//        int restParamCount = paramDataInStream.readInt();
//
//        // Get var names and create var symbols
//        byte[] localVarData = attrDataMap.get(Kind.LOCAL_VARIABLES_ATTRIBUTE);
//        DataInputStream localVarDataInStream = new DataInputStream(new ByteArrayInputStream(localVarData));
//        localVarDataInStream.readShort();
//        BInvokableType funcType = (BInvokableType) invokableSymbol.typeDescRef;
//        if (Symbols.isFlagOn(invokableSymbol.flags, Flags.ATTACHED)) {
//            //remove first variable name
//            getVarName(localVarDataInStream);
//        }
//        for (int i = 0; i < requiredParamCount; i++) {
//            String varName = getVarName(localVarDataInStream);
//            BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(varName), this.env.pkgSymbol.pkgID,
//                    funcType.paramTypes.get(i), invokableSymbol);
//            invokableSymbol.params.add(varSymbol);
//        }
//
//        for (int i = requiredParamCount; i < requiredParamCount + defaultableParamCount; i++) {
//            String varName = getVarName(localVarDataInStream);
//            BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(varName), this.env.pkgSymbol.pkgID,
//                    funcType.paramTypes.get(i), invokableSymbol);
//            invokableSymbol.defaultableParams.add(varSymbol);
//        }
//
//        if (restParamCount == 1) {
//            String varName = getVarName(localVarDataInStream);
//            BVarSymbol varSymbol = new BVarSymbol(0, names.fromString(varName), this.env.pkgSymbol.pkgID,
//                    funcType.paramTypes.get(requiredParamCount + defaultableParamCount), invokableSymbol);
//            invokableSymbol.restParam = varSymbol;
//        }
//
//        byte[] paramDefaultsData = attrDataMap.get(Kind.PARAMETER_DEFAULTS_ATTRIBUTE);
//        DataInputStream paramDefaultsDataInStream = new DataInputStream(new ByteArrayInputStream(paramDefaultsData));
//        int paramDefaultsInfoCount = paramDefaultsDataInStream.readShort();
//        for (int i = 0; i < paramDefaultsInfoCount; i++) {
//            invokableSymbol.defaultableParams.get(i).defaultValue = getDefaultValue(paramDefaultsDataInStream);
//        }
//    }
//
//    private DefaultValueLiteral getDefaultValue(DataInputStream dataInStream)
//            throws IOException {
//        String typeDesc = getUTF8CPEntryValue(dataInStream);
//
//        int valueCPIndex;
//        switch (typeDesc) {
//            case TypeDescriptor.SIG_BOOLEAN:
//                return new DefaultValueLiteral(dataInStream.readBoolean(), TypeTags.BOOLEAN);
//            case TypeDescriptor.SIG_INT:
//                valueCPIndex = dataInStream.readInt();
//                IntegerCPEntry integerCPEntry = (IntegerCPEntry) this.env.constantPool[valueCPIndex];
//                return new DefaultValueLiteral(integerCPEntry.getValue(), TypeTags.INT);
//            case TypeDescriptor.SIG_BYTE:
//                valueCPIndex = dataInStream.readInt();
//                ByteCPEntry byteCPEntry = (ByteCPEntry) this.env.constantPool[valueCPIndex];
//                return new DefaultValueLiteral(byteCPEntry.getValue(), TypeTags.BYTE);
//            case TypeDescriptor.SIG_FLOAT:
//                valueCPIndex = dataInStream.readInt();
//                FloatCPEntry floatCPEntry = (FloatCPEntry) this.env.constantPool[valueCPIndex];
//                return new DefaultValueLiteral(floatCPEntry.getValue(), TypeTags.FLOAT);
//            case TypeDescriptor.SIG_DECIMAL:
//                valueCPIndex = dataInStream.readInt();
//                UTF8CPEntry decimalEntry = (UTF8CPEntry) this.env.constantPool[valueCPIndex];
//                return new DefaultValueLiteral(decimalEntry.getValue(), TypeTags.DECIMAL);
//            case TypeDescriptor.SIG_STRING:
//                valueCPIndex = dataInStream.readInt();
//                UTF8CPEntry stringCPEntry = (UTF8CPEntry) this.env.constantPool[valueCPIndex];
//                return new DefaultValueLiteral(stringCPEntry.getValue(), TypeTags.STRING);
//            case TypeDescriptor.SIG_NULL:
//                break;
//            default:
//                throw new RuntimeException("unknown default value typeDescRef " + typeDesc);
//        }
//        return null;
//    }
//
//    /**
//     * Set taint table to the invokable symbol.
//     *
//     * @param invokableSymbol Invokable symbol
//     * @param attrDataMap     Attribute data map
//     * @throws IOException
//     */
//    private void setTaintTable(BInvokableSymbol invokableSymbol, Map<Kind, byte[]> attrDataMap)
//            throws IOException {
//
//        if (!attrDataMap.containsKey(Kind.TAINT_TABLE) ||
//                !attrDataMap.containsKey(Kind.TAINT_TABLE)) {
//            return;
//        }
//
//        // Get taint table dimensions
//        byte[] taintTableBytes = attrDataMap.get(Kind.TAINT_TABLE);
//        DataInputStream taintTableDataInStream = new DataInputStream(new ByteArrayInputStream(taintTableBytes));
//        int rowCount = taintTableDataInStream.readShort();
//        int columnCount = taintTableDataInStream.readShort();
//
//        // Extract and set taint table to the symbol
//        invokableSymbol.taintTable = new HashMap<>();
//        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
//            int paramIndex = taintTableDataInStream.readShort();
//            TaintRecord.TaintedStatus returnTaintedStatus =
//                    convertByteToTaintedStatus(taintTableDataInStream.readByte());
//            List<TaintRecord.TaintedStatus> parameterTaintedStatusList = new ArrayList<>();
//            for (int columnIndex = 1; columnIndex < columnCount; columnIndex++) {
//                parameterTaintedStatusList.add(convertByteToTaintedStatus(taintTableDataInStream.readByte()));
//            }
//            TaintRecord taintRecord = new TaintRecord(returnTaintedStatus, parameterTaintedStatusList);
//            invokableSymbol.taintTable.put(paramIndex, taintRecord);
//        }
//    }
//
//    private TaintRecord.TaintedStatus convertByteToTaintedStatus(byte readByte) {
//        return EnumSet.allOf(TaintRecord.TaintedStatus.class).stream()
//                .filter(taintedStatus -> readByte == taintedStatus.getByteValue()).findFirst().get();
//    }
//
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
//
//    private String getVarName(DataInputStream dataInStream) throws IOException {
//        String varName = getUTF8CPEntryValue(dataInStream);
//        // read variable index
//        dataInStream.readInt();
//        dataInStream.readInt();
//        dataInStream.readInt();
//        dataInStream.readInt();
//        // Read and ignore identifier kind flag
//        dataInStream.readBoolean();
//
//        int attchmntIndexesLength = dataInStream.readShort();
//        for (int i = 0; i < attchmntIndexesLength; i++) {
//            dataInStream.readInt();
//        }
//
//        return varName;
//    }
//
//    private Object getObjectFieldDefaultValue(byte[] defaultValueAttrData) throws IOException {
//        try (DataInputStream dataInStream = new DataInputStream(
//                new ByteArrayInputStream(defaultValueAttrData))) {
//            String typeDesc = getUTF8CPEntryValue(dataInStream);
//            switch (typeDesc) {
//                case TypeDescriptor.SIG_INT:
//                    return dataInStream.readInt();
//                case TypeDescriptor.SIG_FLOAT:
//                    return dataInStream.readFloat();
//                case TypeDescriptor.SIG_DECIMAL:
//                    return dataInStream.readUTF();
//                case TypeDescriptor.SIG_BOOLEAN:
//                    return dataInStream.readBoolean();
//                case TypeDescriptor.SIG_STRING:
//                    return getUTF8CPEntryValue(dataInStream);
//                default:
//                    throw new BLangCompilerException("unknown default value typeDescRef " + typeDesc);
//            }
//        }
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

//    private BInvokableType createInvokableType(String sig) {
//        char[] chars = sig.toCharArray();
//        Stack<BType> typeStack = new Stack<>();
//        this.typeSigReader.createFunctionType(new CompilerTypeCreater(), chars, 0, typeStack);
//        return (BInvokableType) typeStack.pop();
//    }
//
//    private BPackageSymbol lookupPackageSymbol(String packagePath) {
//        //TODO below is a temporary fix, this needs to be removed later.
//        PackageID pkgID = getPackageID(packagePath);
//        if (pkgID.equals(env.pkgSymbol.pkgID)) {
//            return env.pkgSymbol;
//        }
//
//        BSymbol symbol = lookupMemberSymbol(this.env.pkgSymbol.scope, pkgID.name, SymTag.PACKAGE);
//        if (symbol == this.symTable.notFoundSymbol && pkgID.orgName.equals(Names.BUILTIN_ORG)) {
//            symbol = this.packageLoader.loadPackageSymbol(pkgID, this.env.pkgSymbol.pkgID, this.env.repoHierarchy);
//            if (symbol == null) {
//                throw new BLangCompilerException("unknown imported module: " + pkgID.name);
//            }
//        }
//
//        return (BPackageSymbol) symbol;
//    }
//
//    private PackageID getPackageID(String packagePath) {
//        String[] orgNameParts = packagePath.split(Names.ORG_NAME_SEPARATOR.value);
//        String[] pkgNameParts = orgNameParts[1].split(":");
//        String version = pkgNameParts.length == 2 ? pkgNameParts[1] : Names.EMPTY.value;
//        return createPackageID(orgNameParts[0], pkgNameParts[0], version);
//    }
//
//    private BSymbol lookupMemberSymbol(Scope scope, Name name, int expSymTag) {
//        Scope.ScopeEntry entry = scope.lookup(name);
//        while (entry != NOT_FOUND_ENTRY) {
//            if ((entry.symbol.tag & expSymTag) != expSymTag) {
//                entry = entry.next;
//            } else {
//                return entry.symbol;
//            }
//        }
//
//        return symTable.notFoundSymbol;
//    }
//
//    private BType lookupUserDefinedType(BPackageSymbol packageSymbol, String typeName) {
//        BSymbol typeSymbol = lookupMemberSymbol(packageSymbol.scope, names.fromString(typeName), SymTag.TYPE);
//        if (typeSymbol == this.symTable.notFoundSymbol) {
//            throw new BLangCompilerException("unknown typeDescRef name: " + typeName);
//        }
//
//        return typeSymbol.typeDescRef;
//    }
//
//    private BType getBuiltinRefTypeFromName(String typeName) {
//        BSymbol typeSymbol = lookupMemberSymbol(this.symTable.rootScope, names.fromString(typeName), SymTag.TYPE);
//        if (typeSymbol == this.symTable.notFoundSymbol) {
//            throw new BLangCompilerException("unknown typeDescRef name: " + typeName);
//        }
//
//        return typeSymbol.typeDescRef;
//    }
//
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
//
//    private void resolveTypes() {
//        for (UnresolvedType unresolvedType : this.env.unresolvedTypes) {
//            BType typeDescRef = getBTypeFromDescriptor(unresolvedType.typeSig);
//            unresolvedType.completer.accept(typeDescRef);
//        }
//    }
//
//    private BType getBTypeFromDescriptor(String typeSig) {
//        return this.typeSigReader.getBTypeFromDescriptor(new CompilerTypeCreater(), typeSig);
//    }
//
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
//
//    /**
//     * Create types for compiler phases.
//     *
//     * @since 0.975.0
//     */
//    private class CompilerTypeCreater implements TypeCreater<BType> {
//
//        @Override
//        public BType getBasicType(char typeChar) {
//            switch (typeChar) {
//                case 'I':
//                    return symTable.intType;
//                case 'W':
//                    return symTable.byteType;
//                case 'F':
//                    return symTable.floatType;
//                case 'L':
//                    return symTable.decimalType;
//                case 'S':
//                    return symTable.stringType;
//                case 'B':
//                    return symTable.booleanType;
//                case 'Y':
//                    return symTable.typeDesc;
//                case 'A':
//                    return symTable.anyType;
//                case 'N':
//                    return symTable.nilType;
//                case 'K':
//                    return symTable.anydataType;
//                default:
//                    throw new IllegalArgumentException("unsupported basic typeDescRef char: " + typeChar);
//            }
//        }
//
//        @Override
//        public BType getBuiltinRefType(String typeName) {
//            return getBuiltinRefTypeFromName(typeName);
//        }
//
//        @Override
//        public BType getRefType(char typeChar, String pkgPath, String typeName) {
//            if (typeName.isEmpty()) {
//                return null;
//            }
//
//            BPackageSymbol pkgSymbol;
//            if (pkgPath != null) {
//                pkgSymbol = lookupPackageSymbol(pkgPath);
//            } else {
//                pkgSymbol = env.pkgSymbol;
//            }
//
//            switch (typeChar) {
//                case 'X':
//                    return symTable.anyServiceType;
//                default:
//                    return lookupUserDefinedType(pkgSymbol, typeName);
//            }
//        }
//
//        @Override
//        public BType getConstrainedType(char typeChar, BType constraint) {
//            switch (typeChar) {
//                case 'D':
//                    if (constraint == null) {
//                        return symTable.tableType;
//                    }
//                    return new BTableType(TypeTags.TABLE, constraint, symTable.tableType.tsymbol);
//                case 'M':
//                    if (constraint == null || constraint == symTable.anyType) {
//                        return symTable.mapType;
//                    }
//                    return new BMapType(TypeTags.MAP, constraint, symTable.mapType.tsymbol);
//                case 'H':
//                    return new BStreamType(TypeTags.STREAM, constraint, symTable.streamType.tsymbol);
//                case 'Q':
//                    return new BChannelType(TypeTags.CHANNEL, constraint, symTable.channelType.tsymbol);
//                case 'G':
//                case 'T':
//                case 'X':
//                default:
//                    return constraint;
//            }
//        }
//
//        @Override
//        public BType getArrayType(BType elementType, int size) {
//            BTypeSymbol arrayTypeSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE, Flags.asMask(EnumSet
//                    .of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
//            return size == -1 ? new BArrayType(elementType, arrayTypeSymbol, size, BArrayState.UNSEALED) :
//                    new BArrayType(elementType, arrayTypeSymbol, size, BArrayState.CLOSED_SEALED);
//        }
//
//        @Override
//        public BType getCollectionType(char typeChar, List<BType> memberTypes) {
//
//            switch (typeChar) {
//                case 'O':
//                    BTypeSymbol unionTypeSymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, Flags.asMask(EnumSet
//                            .of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
//                    return new BUnionType(unionTypeSymbol, new LinkedHashSet<>(memberTypes),
//                            memberTypes.contains(symTable.nilType));
//                case 'P':
//                    BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet
//                            .of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
//                    return new BTupleType(tupleTypeSymbol, memberTypes);
//                default:
//                    throw new IllegalArgumentException("unsupported collection typeDescRef char: " + typeChar);
//            }
//        }
//
//        @Override
//        public BType getFunctionType(List<BType> funcParams, BType retType) {
//            if (retType == null) {
//                retType = symTable.nilType;
//            }
//            BTypeSymbol tsymbol = Symbols.createTypeSymbol(SymTag.FUNCTION_TYPE,
// Flags.asMask(EnumSet.of(Flag.PUBLIC)),
//                                                           Names.EMPTY, env.pkgSymbol.pkgID, null,
//                                                           env.pkgSymbol.owner);
//            return new BInvokableType(funcParams, retType, tsymbol);
//        }
//
//        @Override
//        public BType getErrorType(BType reasonType, BType detailsType) {
//            if (reasonType == symTable.stringType && detailsType == symTable.mapType) {
//                return symTable.errorType;
//            }
//            BTypeSymbol errorSymbol = new BErrorTypeSymbol(SymTag.RECORD, Flags.PUBLIC, Names.EMPTY,
//                    env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
//            BErrorType errorType = new BErrorType(errorSymbol, reasonType, detailsType);
//            errorSymbol.typeDescRef = errorType;
//            return errorType;
//        }
//    }

    private class BIRTypeReader {
        private DataInputStream inputStream;
        //TODO implement recursive types

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
                case TypeTags.XML:
                case TypeTags.TABLE:
                    return new BTableType(TypeTags.TABLE, readType(), symTable.tableType.tsymbol);
                case TypeTags.NIL:
                    return symTable.nilType;
                case TypeTags.ANYDATA:
                    return symTable.anydataType;
                case TypeTags.RECORD:
                case TypeTags.TYPEDESC:
                    return symTable.typeDesc;
                case TypeTags.STREAM:
                case TypeTags.MAP:
                    return new BMapType(TypeTags.MAP, readType(), symTable.mapType.tsymbol);
                case TypeTags.INVOKABLE:
                    int paramCount = inputStream.readInt();
                    List<BType> paramTypes = new ArrayList<>();
                    for (int i = 0; i < paramCount; i++) {
                        paramTypes.add(readType());
                    }
                    BType retType = readType();
//                    if (retType == null) {
//                        retType = symTable.nilType;
//                    }
                    BTypeSymbol tsymbol = Symbols.createTypeSymbol(SymTag.FUNCTION_TYPE,
                            Flags.asMask(EnumSet.of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null,
                            env.pkgSymbol.owner);
                    return new BInvokableType(paramTypes, retType, tsymbol);
                // All the above types are branded types
                case TypeTags.ANY:
                    return symTable.anyType;
                case TypeTags.ENDPOINT:
                case TypeTags.ARRAY:
                    byte state = inputStream.readByte();
                    int size = inputStream.readInt();
                    BType elementType = readType();
                    BTypeSymbol arrayTypeSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE, Flags.asMask(EnumSet
                            .of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
                    return new BArrayType(elementType, arrayTypeSymbol, size, BArrayState.valueOf(state));
                case TypeTags.UNION:
                    int unionMemberCount = inputStream.readInt();
                    List<BType> unionMemberTypes = new ArrayList<>();
                    for (int i = 0; i < unionMemberCount; i++) {
                        unionMemberTypes.add(readType());
                    }
                    BTypeSymbol unionTypeSymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, Flags.asMask(EnumSet
                            .of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
                    return BUnionType.create(unionTypeSymbol, new LinkedHashSet<>(unionMemberTypes));
                case TypeTags.PACKAGE:
                case TypeTags.NONE:
                case TypeTags.VOID:
                case TypeTags.XMLNS:
                case TypeTags.ANNOTATION:
                case TypeTags.SEMANTIC_ERROR:
                case TypeTags.ERROR:
                    BType reasonType = readType();
                    BType detailsType = readType();
                    BTypeSymbol errorSymbol = new BErrorTypeSymbol(SymTag.ERROR, Flags.PUBLIC, Names.EMPTY,
                            env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
                    BErrorType errorType = new BErrorType(errorSymbol, reasonType, detailsType);
                    errorSymbol.type = errorType;
                    return errorType;
                case TypeTags.ITERATOR:
                case TypeTags.TUPLE:
                    int tupleMemberCount = inputStream.readInt();
                    List<BType> tupleMemberTypes = new ArrayList<>();
                    for (int i = 0; i < tupleMemberCount; i++) {
                        tupleMemberTypes.add(readType());
                    }
                    BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet
                            .of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
                    return new BTupleType(tupleTypeSymbol, tupleMemberTypes);
                case TypeTags.FUTURE:
                case TypeTags.INTERMEDIATE_COLLECTION:
                case TypeTags.FINITE:
                case TypeTags.OBJECT:
                case TypeTags.BYTE_ARRAY:
                case TypeTags.FUNCTION_POINTER:
//                case TypeTags.CHANNEL:

//                case TypeTags.SERVICE:

            }
            return null;
        }
    }
}

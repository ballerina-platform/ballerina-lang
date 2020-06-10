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
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.ByteCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.FloatCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.IntegerCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.PackageCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeParamAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstructorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final SymbolResolver symbolResolver;
    private final SymbolTable symTable;
    private final Names names;
    private final TypeParamAnalyzer typeParamAnalyzer;

    private BIRPackageSymbolEnv env;
    private List<BStructureTypeSymbol> structureTypes; // TODO find a better way
    private BStructureTypeSymbol currentStructure = null;
    private LinkedList<Object> compositeStack = new LinkedList<>();

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
        this.symbolResolver = SymbolResolver.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.typeParamAnalyzer = TypeParamAnalyzer.getInstance(context);
    }

    BPackageSymbol definePackage(PackageID packageId, RepoHierarchy packageRepositoryHierarchy,
                                 byte[] packageBinaryContent) {
        BPackageSymbol pkgSymbol = definePackage(packageId, packageRepositoryHierarchy,
                new ByteArrayInputStream(packageBinaryContent));

        // Strip magic value (4 bytes) and the version (2 bytes) off from the binary content of the package.
        byte[] modifiedPkgBinaryContent = Arrays.copyOfRange(
                packageBinaryContent, 8, packageBinaryContent.length);
        pkgSymbol.birPackageFile = new CompiledBinaryFile.BIRPackageFile(modifiedPkgBinaryContent);
        SymbolEnv builtinEnv = this.symTable.pkgEnvMap.get(symTable.langAnnotationModuleSymbol);
        SymbolEnv pkgEnv = SymbolEnv.createPkgEnv(null, pkgSymbol.scope, builtinEnv);
        this.symTable.pkgEnvMap.put(pkgSymbol, pkgEnv);
        return pkgSymbol;
    }

    private BPackageSymbol definePackage(PackageID packageId, RepoHierarchy packageRepositoryHierarchy,
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
        } catch (Throwable e) {
            // TODO dlog.error();
            // TODO format error
            throw new BLangCompilerException(e.getMessage(), e);
        }
    }

    private BPackageSymbol definePackage(DataInputStream dataInStream) throws IOException {
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

        // Define constants.
        defineSymbols(dataInStream, rethrow(this::defineConstant));

        // Define typeDescRef definitions.
        this.structureTypes = new ArrayList<>();
        defineSymbols(dataInStream, rethrow(this::defineTypeDef));

        // Define package level variables.
        defineSymbols(dataInStream, rethrow(this::definePackageLevelVariables));

        readTypeDefBodies(dataInStream);

        // Define functions.
        defineSymbols(dataInStream, rethrow(this::defineFunction));

        // Define annotations.
        defineSymbols(dataInStream, rethrow(this::defineAnnotations));

        return this.env.pkgSymbol;
    }

    private void readTypeDefBodies(DataInputStream dataInStream) throws IOException {
        for (BStructureTypeSymbol structureTypeSymbol : this.structureTypes) {
            this.currentStructure = structureTypeSymbol;
            defineSymbols(dataInStream, rethrow(this::defineFunction));
        }
        this.currentStructure = null;
    }

    private CPEntry[] readConstantPool(DataInputStream dataInStream) throws IOException {
        int constantPoolSize = dataInStream.readInt();
        CPEntry[] constantPool = new CPEntry[constantPoolSize];
        this.env.constantPool = constantPool;
        for (int i = 0; i < constantPoolSize; i++) {
            byte cpTag = dataInStream.readByte();
            CPEntry.Type cpEntryType = CPEntry.Type.values()[cpTag - 1];
            constantPool[i] = readCPEntry(dataInStream, constantPool, cpEntryType, i);
        }
        return constantPool;
    }

    private CPEntry readCPEntry(DataInputStream dataInStream,
                                CPEntry[] constantPool,
                                CPEntry.Type cpEntryType, int i) throws IOException {
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
            case CP_ENTRY_SHAPE:
                env.unparsedBTypeCPs.put(i, readByteArray(dataInStream));
                return null;
            case CP_ENTRY_BYTE:
                return new CPEntry.ByteCPEntry(dataInStream.readInt());
            default:
                throw new IllegalStateException("unsupported constant pool entry type: " +
                        cpEntryType.name());
        }
    }

    private byte[] readByteArray(DataInputStream dataInStream) throws IOException {
        int length = dataInStream.readInt();
        byte[] bytes = new byte[length];
        dataInStream.readFully(bytes);
        return bytes;
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
        String source = getStringCPEntryValue(dataInStream);
        // Consider attached functions.. remove the first variable
        String funcName = getStringCPEntryValue(dataInStream);
        int flags = dataInStream.readInt();

        BInvokableType funcType = (BInvokableType) readBType(dataInStream);
        BInvokableSymbol invokableSymbol = Symbols.createFunctionSymbol(flags, names.fromString(funcName),
                this.env.pkgSymbol.pkgID, funcType, this.env.pkgSymbol, Symbols.isFlagOn(flags, Flags.NATIVE));
        invokableSymbol.source = source;
        invokableSymbol.retType = funcType.retType;

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
                if (Names.USER_DEFINED_INIT_SUFFIX.value.equals(funcName)
                        || funcName.equals(Names.INIT_FUNCTION_SUFFIX.value)) {
                    structureTypeSymbol.initializerFunc = attachedFunc;
                } else if (funcName.equals(Names.GENERATED_INIT_SUFFIX.value)) {
                    ((BObjectTypeSymbol) structureTypeSymbol).generatedInitializerFunc = attachedFunc;
                } else {
                    structureTypeSymbol.attachedFuncs.add(attachedFunc);
                }
            }
        }

        // set parameter symbols to the function symbol
        setParamSymbols(invokableSymbol, dataInStream);

        // set taint table to the function symbol
        readTaintTable(invokableSymbol, dataInStream);

        defineMarkDownDocAttachment(invokableSymbol, readDocBytes(dataInStream));

        scopeToDefine.define(invokableSymbol.name, invokableSymbol);
    }

    private void defineTypeDef(DataInputStream dataInStream) throws IOException {
        String typeDefName = getStringCPEntryValue(dataInStream);

        int flags = dataInStream.readInt();
        boolean isLabel = dataInStream.readByte() == 1;

        byte[] docBytes = readDocBytes(dataInStream);

        BType type = readBType(dataInStream);
        if (type.tag == TypeTags.INVOKABLE) {
            setInvokableTypeSymbol((BInvokableType) type);
        }

        // Temp solution to add abstract flag if available TODO find a better approach
        flags = Symbols.isFlagOn(type.tsymbol.flags, Flags.ABSTRACT) ? flags | Flags.ABSTRACT : flags;

        // Temp solution to add client flag if available TODO find a better approach
        flags = Symbols.isFlagOn(type.tsymbol.flags, Flags.CLIENT) ? flags | Flags.CLIENT : flags;

        BTypeSymbol symbol;
        if (isLabel) {
            symbol = type.tsymbol.createLabelSymbol();
        } else {
            symbol = type.tsymbol;
        }

        defineMarkDownDocAttachment(symbol, docBytes);

        symbol.name = names.fromString(typeDefName);
        symbol.type = type;
        symbol.pkgID = this.env.pkgSymbol.pkgID;
        symbol.flags = flags;

        if (type.tag == TypeTags.RECORD || type.tag == TypeTags.OBJECT) {
            this.structureTypes.add((BStructureTypeSymbol) symbol);
        }

        this.env.pkgSymbol.scope.define(symbol.name, symbol);
        if (type.tag == TypeTags.ERROR) {
            defineErrorConstructor(this.env.pkgSymbol.scope, symbol);
        }
    }

    private void setInvokableTypeSymbol(BInvokableType invokableType) {
        BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) invokableType.tsymbol;
        List<BVarSymbol> params = new ArrayList<>();
        for (BType paramType : invokableType.paramTypes) {
            BVarSymbol varSymbol = new BVarSymbol(paramType.flags, Names.EMPTY, //TODO: should be written/read to BIR
                    this.env.pkgSymbol.pkgID,
                    paramType, null);
            params.add(varSymbol);
        }
        tsymbol.params = params;

        if (invokableType.restType != null) {
            tsymbol.restParam = new BVarSymbol(0, Names.EMPTY, this.env.pkgSymbol.pkgID, invokableType.restType, null);
        }

        tsymbol.returnType = invokableType.retType;
    }

    private void defineMarkDownDocAttachment(BSymbol symbol, byte[] docBytes) throws IOException {
        DataInputStream dataInStream = new DataInputStream(new ByteArrayInputStream(docBytes));
        boolean docPresent = dataInStream.readBoolean();
        if (!docPresent) {
            return;
        }
        MarkdownDocAttachment markdownDocAttachment = new MarkdownDocAttachment();

        int descCPIndex = dataInStream.readInt();
        int retDescCPIndex = dataInStream.readInt();
        markdownDocAttachment.description = descCPIndex >= 0 ? getStringCPEntryValue(descCPIndex) : null;
        markdownDocAttachment.returnValueDescription
                = retDescCPIndex  >= 0 ? getStringCPEntryValue(retDescCPIndex) : null;

        int paramLength = dataInStream.readInt();
        for (int i = 0; i < paramLength; i++) {
            int nameCPIndex = dataInStream.readInt();
            int paramDescCPIndex = dataInStream.readInt();
            String name = nameCPIndex >= 0 ? getStringCPEntryValue(nameCPIndex) : null;
            String description = paramDescCPIndex >= 0 ? getStringCPEntryValue(paramDescCPIndex) : null;
            MarkdownDocAttachment.Parameter parameter = new MarkdownDocAttachment.Parameter(name, description);
            markdownDocAttachment.parameters.add(parameter);
        }
        symbol.markdownDocumentation = markdownDocAttachment;
    }

    private void defineErrorConstructor(Scope scope, BTypeSymbol typeDefSymbol) {
        BConstructorSymbol symbol = new BConstructorSymbol(typeDefSymbol.flags, typeDefSymbol.name,
                typeDefSymbol.pkgID, typeDefSymbol.type, typeDefSymbol.owner);
        symbol.kind = SymbolKind.ERROR_CONSTRUCTOR;
        symbol.scope = new Scope(symbol);
        symbol.retType = typeDefSymbol.type;
        scope.define(symbol.name, symbol);

        ((BErrorTypeSymbol) typeDefSymbol).ctorSymbol = symbol;
    }

    private BType readBType(DataInputStream dataInStream) throws IOException {
        int typeCpIndex = dataInStream.readInt();
        CPEntry cpEntry = this.env.constantPool[typeCpIndex];
        BType type = null;
        if (cpEntry != null) {
            type = ((CPEntry.ShapeCPEntry) cpEntry).shape;
            if (type.tag != TypeTags.INVOKABLE) {
                return type;
            }
        }
        if (type == null) {
            byte[] e = env.unparsedBTypeCPs.get(typeCpIndex);
            type = new BIRTypeReader(new DataInputStream(new ByteArrayInputStream(e))).readType(typeCpIndex);
            addShapeCP(type, typeCpIndex);
        }

        if (type.tag == TypeTags.INVOKABLE) {
            return createClonedInvokableTypeWithTsymbol((BInvokableType) type);
        }

        return type;
    }

    private BInvokableType createClonedInvokableTypeWithTsymbol(BInvokableType bInvokableType) {
        BInvokableType clonedType = new BInvokableType(bInvokableType.paramTypes, bInvokableType.restType,
                bInvokableType.retType, null);
        clonedType.tsymbol = Symbols.createInvokableTypeSymbol(SymTag.FUNCTION_TYPE,
                bInvokableType.flags, env.pkgSymbol.pkgID, null,
                env.pkgSymbol.owner);
        //TODO: tsymbol param values should be read from bir and added here
        return clonedType;
    }

    private void addShapeCP(BType bType, int typeCpIndex) {
        this.env.constantPool[typeCpIndex] = new CPEntry.ShapeCPEntry(bType);
    }

    private void defineAnnotations(DataInputStream dataInStream) throws IOException {
        String name = getStringCPEntryValue(dataInStream);

        int flags = dataInStream.readInt();

        int attachPointCount = dataInStream.readInt();
        Set<AttachPoint> attachPoints = new HashSet<>(attachPointCount);

        for (int i = 0; i < attachPointCount; i++) {
            attachPoints.add(AttachPoint.getAttachmentPoint(getStringCPEntryValue(dataInStream),
                                                            dataInStream.readBoolean()));
        }

        BType annotationType = readBType(dataInStream);

        BAnnotationSymbol annotationSymbol = Symbols.createAnnotationSymbol(flags, attachPoints, names.fromString(name),
                this.env.pkgSymbol.pkgID, null, this.env.pkgSymbol);
        annotationSymbol.type = new BAnnotationType(annotationSymbol);

        defineMarkDownDocAttachment(annotationSymbol, readDocBytes(dataInStream));

        this.env.pkgSymbol.scope.define(annotationSymbol.name, annotationSymbol);
        if (annotationType != symTable.noType) { //TODO fix properly
            annotationSymbol.attachedType = annotationType.tsymbol;
        }
    }

    private void defineConstant(DataInputStream dataInStream) throws IOException {
        String constantName = getStringCPEntryValue(dataInStream);
        int flags = dataInStream.readInt();

        byte[] docBytes = readDocBytes(dataInStream);

        BType type = readBType(dataInStream);
        Scope enclScope = this.env.pkgSymbol.scope;

        // Create the constant symbol.
        BConstantSymbol constantSymbol = new BConstantSymbol(flags, names.fromString(constantName),
                this.env.pkgSymbol.pkgID, null, type, enclScope.owner);

        defineMarkDownDocAttachment(constantSymbol, docBytes);

        constantSymbol.value = readConstLiteralValue(dataInStream);
        constantSymbol.literalType = constantSymbol.value.type;

        // Define constant.
        enclScope.define(constantSymbol.name, constantSymbol);
    }

    private BLangConstantValue readConstLiteralValue(DataInputStream dataInStream) throws IOException {
        BType valueType = readBType(dataInStream);
        switch (valueType.tag) {
            case TypeTags.INT:
                return new BLangConstantValue(getIntCPEntryValue(dataInStream), symTable.intType);
            case TypeTags.BYTE:
                return new BLangConstantValue(getByteCPEntryValue(dataInStream), symTable.byteType);
            case TypeTags.FLOAT:
                return new BLangConstantValue(getFloatCPEntryValue(dataInStream), symTable.floatType);
            case TypeTags.STRING:
                return new BLangConstantValue(getStringCPEntryValue(dataInStream), symTable.stringType);
            case TypeTags.DECIMAL:
                return new BLangConstantValue(getStringCPEntryValue(dataInStream), symTable.decimalType);
            case TypeTags.BOOLEAN:
                return new BLangConstantValue(dataInStream.readByte() == 1, symTable.booleanType);
            case TypeTags.NIL:
                return new BLangConstantValue(null, symTable.nilType);
            case TypeTags.MAP:
                int size = dataInStream.readInt();
                Map<String, BLangConstantValue> keyValuePairs = new LinkedHashMap<>();
                for (int i = 0; i < size; i++) {
                    String key = getStringCPEntryValue(dataInStream);
                    BLangConstantValue value = readConstLiteralValue(dataInStream);
                    keyValuePairs.put(key, value);
                }
                return new BLangConstantValue(keyValuePairs, valueType);
            default:
                // TODO implement for other types
                throw new RuntimeException("unexpected type: " + valueType);
        }
    }

    private void definePackageLevelVariables(DataInputStream dataInStream) throws IOException {
        String varName = getStringCPEntryValue(dataInStream);
        int flags = dataInStream.readInt();

        byte[] docBytes = readDocBytes(dataInStream);

        // Create variable symbol
        BType varType = readBType(dataInStream);
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
            if (varType.tsymbol != null && Symbols.isFlagOn(varType.tsymbol.flags, Flags.CLIENT)) {
                varSymbol.tag = SymTag.ENDPOINT;
            }
        }

        defineMarkDownDocAttachment(varSymbol, docBytes);

        enclScope.define(varSymbol.name, varSymbol);
    }

    private void setParamSymbols(BInvokableSymbol invokableSymbol, DataInputStream dataInStream)
            throws IOException {

        int requiredParamCount = dataInStream.readInt();

        BInvokableType invokableType = (BInvokableType) invokableSymbol.type;
        for (int i = 0; i < requiredParamCount; i++) {
            String paramName = getStringCPEntryValue(dataInStream);
            int flags = dataInStream.readInt();
            BVarSymbol varSymbol = new BVarSymbol(flags, names.fromString(paramName), this.env.pkgSymbol.pkgID,
                    invokableType.paramTypes.get(i), invokableSymbol);
            varSymbol.defaultableParam = ((flags & Flags.OPTIONAL) == Flags.OPTIONAL);
            invokableSymbol.params.add(varSymbol);
        }

        if (dataInStream.readBoolean()) { //if rest param exist
            String paramName = getStringCPEntryValue(dataInStream);
            invokableSymbol.restParam = new BVarSymbol(0, names.fromString(paramName), this.env.pkgSymbol.pkgID,
                    invokableType.restType, invokableSymbol);
        }
        BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) invokableType.tsymbol;
        tsymbol.flags = invokableSymbol.flags;
        tsymbol.params = invokableSymbol.params;
        tsymbol.restParam = invokableSymbol.restParam;
        tsymbol.returnType = invokableSymbol.retType;

        boolean hasReceiver = dataInStream.readBoolean(); // if receiver is written, read and ignore
        if (hasReceiver) {
            dataInStream.readByte();
            readBType(dataInStream);
            getStringCPEntryValue(dataInStream);
        }
    }

    /**
     * Set taint table to the invokable symbol.
     *
     * @param invokableSymbol   Invokable symbol
     * @param dataInStream      Input stream
     * @throws IOException      On error while reading the stream
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
    private String getStringCPEntryValue(DataInputStream dataInStream) throws IOException {
        int pkgNameCPIndex = dataInStream.readInt();
        StringCPEntry stringCPEntry = (StringCPEntry) this.env.constantPool[pkgNameCPIndex];
        return stringCPEntry.value;
    }

    private String getStringCPEntryValue(int cpIndex) {
        StringCPEntry stringCPEntry = (StringCPEntry) this.env.constantPool[cpIndex];
        return stringCPEntry.value;
    }

    private long getIntCPEntryValue(DataInputStream dataInStream) throws IOException {
        int pkgNameCPIndex = dataInStream.readInt();
        IntegerCPEntry intCPEntry = (IntegerCPEntry) this.env.constantPool[pkgNameCPIndex];
        return intCPEntry.value;
    }

    private int getByteCPEntryValue(DataInputStream dataInStream) throws IOException {
        int byteCpIndex = dataInStream.readInt();
        ByteCPEntry byteCPEntry = (ByteCPEntry) this.env.constantPool[byteCpIndex];
        return byteCPEntry.value;
    }

    private String getFloatCPEntryValue(DataInputStream dataInStream) throws IOException {
        int floatCpIndex = dataInStream.readInt();
        FloatCPEntry floatCPEntry = (FloatCPEntry) this.env.constantPool[floatCpIndex];
        return Double.toString(floatCPEntry.value);
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
        Map<Integer, byte[]> unparsedBTypeCPs = new HashMap<>();
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
        static final int SERVICE_TYPE_TAG = 51;
        private DataInputStream inputStream;

        BIRTypeReader(DataInputStream inputStream) {
            this.inputStream = inputStream;
        }

        private BType readTypeFromCp() throws IOException {
            return readBType(inputStream);
        }

        BType readType(int cpI) throws IOException {
            byte tag = inputStream.readByte();
            Name name = names.fromString(getStringCPEntryValue(inputStream));
            int flags = inputStream.readInt();

            // read and ignore type flags. These are only needed for runtime.
            inputStream.readInt();

            switch (tag) {
                case TypeTags.INT:
                    return typeParamAnalyzer.getNominalType(symTable.intType, name, flags);
                case TypeTags.BYTE:
                    return typeParamAnalyzer.getNominalType(symTable.byteType, name, flags);
                case TypeTags.FLOAT:
                    return typeParamAnalyzer.getNominalType(symTable.floatType, name, flags);
                case TypeTags.DECIMAL:
                    return typeParamAnalyzer.getNominalType(symTable.decimalType, name, flags);
                case TypeTags.STRING:
                    return typeParamAnalyzer.getNominalType(symTable.stringType, name, flags);
                case TypeTags.BOOLEAN:
                    return typeParamAnalyzer.getNominalType(symTable.booleanType, name, flags);
                // All the above types are values type
                case TypeTags.JSON:
                    return symTable.jsonType;
                case TypeTags.XML:
                    BType constraintType = readTypeFromCp();
                    return new BXMLType(constraintType, symTable.xmlType.tsymbol);
                case TypeTags.NIL:
                    return symTable.nilType;
                case TypeTags.NEVER:
                    return symTable.neverType;
                case TypeTags.ANYDATA:
                    return typeParamAnalyzer.getNominalType(symTable.anydataType, name, flags);
                case TypeTags.RECORD:
                    int pkgCpIndex = inputStream.readInt();
                    PackageID pkgId = getPackageId(pkgCpIndex);

                    String recordName = getStringCPEntryValue(inputStream);
                    BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                            names.fromString(recordName), env.pkgSymbol.pkgID, null, env.pkgSymbol);
                    recordSymbol.flags |= flags;
                    recordSymbol.scope = new Scope(recordSymbol);
                    BRecordType recordType = new BRecordType(recordSymbol);
                    recordType.flags |= flags;
                    recordSymbol.type = recordType;

                    compositeStack.push(recordType);
                    addShapeCP(recordType, cpI);

                    recordType.sealed = inputStream.readBoolean();
                    recordType.restFieldType = readTypeFromCp();

                    int recordFields = inputStream.readInt();
                    for (int i = 0; i < recordFields; i++) {
                        String fieldName = getStringCPEntryValue(inputStream);
                        int fieldFlags = inputStream.readInt();

                        byte[] docBytes = readDocBytes(inputStream);

                        BType fieldType = readTypeFromCp();

                        BVarSymbol varSymbol = new BVarSymbol(fieldFlags, names.fromString(fieldName),
                                recordSymbol.pkgID, fieldType, recordSymbol.scope.owner);

                        defineMarkDownDocAttachment(varSymbol, docBytes);

                        BField structField = new BField(varSymbol.name, null, varSymbol);
                        recordType.fields.put(structField.name.value, structField);
                        recordSymbol.scope.define(varSymbol.name, varSymbol);
                    }

                    boolean isInitAvailable = inputStream.readByte() == 1;
                    if (isInitAvailable) {
                        // read record init function
                        String recordInitFuncName = getStringCPEntryValue(inputStream);
                        int recordInitFuncFlags = inputStream.readInt();
                        BInvokableType recordInitFuncType = (BInvokableType) readTypeFromCp();
                        Name initFuncName = names.fromString(recordInitFuncName);
                        boolean isNative = Symbols.isFlagOn(recordInitFuncFlags, Flags.NATIVE);
                        BInvokableSymbol recordInitFuncSymbol =
                                Symbols.createFunctionSymbol(recordInitFuncFlags,
                                        initFuncName, env.pkgSymbol.pkgID, recordInitFuncType,
                                        env.pkgSymbol, isNative);
                        recordInitFuncSymbol.retType = recordInitFuncType.retType;
                        recordSymbol.initializerFunc = new BAttachedFunction(initFuncName, recordInitFuncSymbol,
                                recordInitFuncType);
                        recordSymbol.scope.define(initFuncName, recordInitFuncSymbol);
                    }

//                    setDocumentation(varSymbol, attrData); // TODO fix

                    Object poppedRecordType = compositeStack.pop();
                    assert poppedRecordType == recordType;

                    if (pkgId.equals(env.pkgSymbol.pkgID)) {
                        return recordType;
                    }

                    BPackageSymbol pkgSymbol = packageLoader.loadPackageSymbol(pkgId, null, null);
                    SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgSymbol);
                    return symbolResolver.lookupSymbolInMainSpace(pkgEnv, names.fromString(recordName)).type;
                case TypeTags.TYPEDESC:
                    BTypedescType typedescType = new BTypedescType(null, symTable.typeDesc.tsymbol);
                    typedescType.constraint = readTypeFromCp();
                    return typedescType;
                case TypeTags.STREAM:
                    BStreamType bStreamType = new BStreamType(TypeTags.STREAM, null, null, symTable.streamType.tsymbol);
                    bStreamType.constraint = readTypeFromCp();
                    boolean hasError = inputStream.readByte() == 1;
                    if (hasError) {
                        bStreamType.error = readTypeFromCp();
                    }
                    return bStreamType;
                case TypeTags.TABLE:
                    BTableType bTableType = new BTableType(TypeTags.TABLE, null, symTable.tableType.tsymbol);
                    bTableType.constraint = readTypeFromCp();
                    boolean hasFieldNameList = inputStream.readByte() == 1;
                    boolean hasKeyConstraint = inputStream.readByte() == 1;

                    if (hasFieldNameList) {
                        bTableType.fieldNameList = new ArrayList<>();
                        int fieldNameListSize = inputStream.readInt();
                        for (int i = 0; i < fieldNameListSize; i++) {
                            String fieldName = getStringCPEntryValue(inputStream);
                            bTableType.fieldNameList.add(fieldName);
                        }
                    }

                    if (hasKeyConstraint) {
                        bTableType.keyTypeConstraint = readTypeFromCp();
                        if (bTableType.keyTypeConstraint.tsymbol == null) {
                            bTableType.keyTypeConstraint.tsymbol = Symbols.createTypeSymbol(SymTag.TYPE,
                                    Flags.asMask(EnumSet.of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID,
                                    bTableType.keyTypeConstraint, env.pkgSymbol.owner);
                        }
                    }
                    return bTableType;
                case TypeTags.MAP:
                    BMapType bMapType = new BMapType(TypeTags.MAP, null, symTable.mapType.tsymbol);
                    bMapType.constraint = readTypeFromCp();
                    return bMapType;
                case TypeTags.INVOKABLE:
                    BInvokableType bInvokableType = new BInvokableType(null, null, null, null);
                    bInvokableType.flags = flags;
                    int paramCount = inputStream.readInt();
                    List<BType> paramTypes = new ArrayList<>();
                    for (int i = 0; i < paramCount; i++) {
                        paramTypes.add(readTypeFromCp());
                    }
                    bInvokableType.paramTypes = paramTypes;
                    if (inputStream.readBoolean()) { //if rest param exist
                        bInvokableType.restType = readTypeFromCp();
                    }
                    bInvokableType.retType = readTypeFromCp();
                    return bInvokableType;
                // All the above types are branded types
                case TypeTags.ANY:
                    return typeParamAnalyzer.getNominalType(symTable.anyType, name, flags);
                case TypeTags.HANDLE:
                    return symTable.handleType;
                case TypeTags.READONLY:
                    return symTable.readonlyType;
                case TypeTags.ENDPOINT:
                    // TODO fix
                    break;
                case TypeTags.ARRAY:
                    byte state = inputStream.readByte();
                    int size = inputStream.readInt();
                    BTypeSymbol arrayTypeSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE, Flags.asMask(EnumSet
                            .of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
                    BArrayType bArrayType = new BArrayType(null, arrayTypeSymbol, size, BArrayState.valueOf(state));
                    bArrayType.eType = readTypeFromCp();
                    return bArrayType;
                case TypeTags.UNION:
                    BTypeSymbol unionTypeSymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, Flags.asMask(EnumSet
                            .of(Flag.PUBLIC)), Names.EMPTY, env.pkgSymbol.pkgID, null, env.pkgSymbol.owner);
                    BUnionType unionType = BUnionType.create(unionTypeSymbol,
                            new LinkedHashSet<>()); //TODO improve(useless second param)
                    int unionMemberCount = inputStream.readInt();
                    for (int i = 0; i < unionMemberCount; i++) {
                        unionType.add(readTypeFromCp());
                    }
                    return unionType;
                case TypeTags.INTERSECTION:
                    BTypeSymbol intersectionTypeSymbol = Symbols.createTypeSymbol(SymTag.INTERSECTION_TYPE,
                                                                                  Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                                                                                  Names.EMPTY, env.pkgSymbol.pkgID,
                                                                                  null, env.pkgSymbol.owner);
                    int intersectionMemberCount = inputStream.readInt();
                    LinkedHashSet<BType> constituentTypes = new LinkedHashSet<>(intersectionMemberCount);
                    for (int i = 0; i < intersectionMemberCount; i++) {
                        constituentTypes.add(readTypeFromCp());
                    }

                    BType effectiveType = readTypeFromCp();
                    return new BIntersectionType(intersectionTypeSymbol, constituentTypes, effectiveType);
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
                    addShapeCP(errorType, cpI);
                    compositeStack.push(errorType);
                    pkgCpIndex = inputStream.readInt();
                    pkgId = getPackageId(pkgCpIndex);
                    String errorName = getStringCPEntryValue(inputStream);
                    BType detailsType = readTypeFromCp();
                    errorType.detailType = detailsType;
                    errorSymbol.type = errorType;
                    errorSymbol.pkgID = pkgId;
                    errorSymbol.name = names.fromString(errorName);
                    Object poppedErrorType = compositeStack.pop();
                    assert poppedErrorType == errorType;
                    if (!env.pkgSymbol.pkgID.equals(PackageID.ANNOTATIONS)
                            && Symbols.isFlagOn(flags, Flags.NATIVE)) {
                        // This is a workaround to avoid, getting no type for error detail field.
                        return symTable.errorType;
                    }
                    errorType.typeIdSet = readTypeIdSet(inputStream);
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
                        tupleMemberTypes.add(readTypeFromCp());
                    }
                    bTupleType.tupleTypes = tupleMemberTypes;
                    return bTupleType;
                case TypeTags.FUTURE:
                    BFutureType bFutureType = new BFutureType(TypeTags.FUTURE, null, symTable.futureType.tsymbol);
                    bFutureType.constraint = readTypeFromCp();
                    return bFutureType;
                case TypeTags.FINITE:
                    String finiteTypeName = getStringCPEntryValue(inputStream);
                    int finiteTypeFlags = inputStream.readInt();
                    BTypeSymbol symbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, finiteTypeFlags,
                            names.fromString(finiteTypeName), env.pkgSymbol.pkgID, null, env.pkgSymbol);
                    symbol.scope = new Scope(symbol);
                    BFiniteType finiteType = new BFiniteType(symbol);
                    symbol.type = finiteType;
                    int valueSpaceSize = inputStream.readInt();
                    for (int i = 0; i < valueSpaceSize; i++) {
                        defineValueSpace(inputStream, finiteType, this);
                    }
                    return finiteType;
                case TypeTags.OBJECT:
                    boolean service = inputStream.readByte() == 1;

                    pkgCpIndex = inputStream.readInt();
                    pkgId = getPackageId(pkgCpIndex);

                    String objName = getStringCPEntryValue(inputStream);
                    int objFlags = (inputStream.readBoolean() ? Flags.ABSTRACT : 0) | Flags.PUBLIC;
                    objFlags = inputStream.readBoolean() ? objFlags | Flags.CLIENT : objFlags;
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
                    addShapeCP(objectType, cpI);
                    compositeStack.push(objectType);
                    int fieldCount = inputStream.readInt();
                    for (int i = 0; i < fieldCount; i++) {
                        String fieldName = getStringCPEntryValue(inputStream);
                        int fieldFlags = inputStream.readInt();

                        byte[] docBytes = readDocBytes(inputStream);

                        BType fieldType = readTypeFromCp();
                        BVarSymbol objectVarSymbol = new BVarSymbol(fieldFlags, names.fromString(fieldName),
                                objectSymbol.pkgID, fieldType, objectSymbol.scope.owner);

                        defineMarkDownDocAttachment(objectVarSymbol, docBytes);

                        BField structField = new BField(objectVarSymbol.name, null, objectVarSymbol);
                        objectType.fields.put(structField.name.value, structField);
                        objectSymbol.scope.define(objectVarSymbol.name, objectVarSymbol);
                    }
                    boolean generatedConstructorPresent = inputStream.readBoolean();
                    if (generatedConstructorPresent) {
                        ignoreAttachedFunc();
                    }
                    boolean constructorPresent = inputStream.readBoolean();
                    if (constructorPresent) {
                        ignoreAttachedFunc();
                    }
                    int funcCount = inputStream.readInt();
                    for (int i = 0; i < funcCount; i++) {
                        ignoreAttachedFunc();
                    }
                    Object poppedObjType = compositeStack.pop();
                    assert poppedObjType == objectType;

                    if (pkgId.equals(env.pkgSymbol.pkgID)) {
                        return objectType;
                    }

                    pkgSymbol = packageLoader.loadPackageSymbol(pkgId, null, null);
                    pkgEnv = symTable.pkgEnvMap.get(pkgSymbol);
                    return symbolResolver.lookupSymbolInMainSpace(pkgEnv, names.fromString(objName)).type;
                case TypeTags.BYTE_ARRAY:
                    // TODO fix
                    break;
                case TypeTags.FUNCTION_POINTER:
                    // TODO fix
                    break;
                case SERVICE_TYPE_TAG:
                    return symTable.anyServiceType;
                case TypeTags.SIGNED32_INT:
                    return symTable.signed32IntType;
                case TypeTags.SIGNED16_INT:
                    return symTable.signed16IntType;
                case TypeTags.SIGNED8_INT:
                    return symTable.signed8IntType;
                case TypeTags.UNSIGNED32_INT:
                    return symTable.unsigned32IntType;
                case TypeTags.UNSIGNED16_INT:
                    return symTable.unsigned16IntType;
                case TypeTags.UNSIGNED8_INT:
                    return symTable.unsigned8IntType;
                case TypeTags.CHAR_STRING:
                    return symTable.charStringType;
                case TypeTags.XML_ELEMENT:
                    return symTable.xmlElementType;
                case TypeTags.XML_PI:
                    return symTable.xmlPIType;
                case TypeTags.XML_COMMENT:
                    return symTable.xmlCommentType;
                case TypeTags.XML_TEXT:
                    return symTable.xmlTextType;
            }
            return null;
        }

        private BTypeIdSet readTypeIdSet(DataInputStream inputStream) throws IOException {
            Set<BTypeIdSet.BTypeId> primary = new HashSet<>();
            int primaryTypeIdCount = inputStream.readInt();
            for (int i = 0; i < primaryTypeIdCount; i++) {
                primary.add(readTypeId(inputStream));
            }

            Set<BTypeIdSet.BTypeId> secondary = new HashSet<>();
            int secondaryTypeIdCount = inputStream.readInt();
            for (int i = 0; i < secondaryTypeIdCount; i++) {
                secondary.add(readTypeId(inputStream));
            }

            return new BTypeIdSet(primary, secondary);
        }

        private BTypeIdSet.BTypeId readTypeId(DataInputStream inputStream) throws IOException {
            int pkgCPIndex = inputStream.readInt();
            PackageID packageId = getPackageId(pkgCPIndex);
            String name = getStringCPEntryValue(inputStream);
            boolean isPublicTypeId = inputStream.readBoolean();
            return new BTypeIdSet.BTypeId(packageId, name, isPublicTypeId);
        }

        private void ignoreAttachedFunc() throws IOException {
            getStringCPEntryValue(inputStream);
            inputStream.readInt();
            readTypeFromCp();
        }
    }

    private byte[] readDocBytes(DataInputStream inputStream) throws IOException {
        int docLength = inputStream.readInt();
        byte[] docBytes = new byte[docLength];
        int noOfBytesRead = inputStream.read(docBytes);
        if (docLength != noOfBytesRead) {
            throw new RuntimeException("Failed to read Markdown Documenation");
        }
        return docBytes;
    }

    private PackageID getPackageId(int pkgCPIndex) {
        PackageCPEntry pkgCpEntry = (PackageCPEntry) env.constantPool[pkgCPIndex];
        String orgName = ((StringCPEntry) env.constantPool[pkgCpEntry.orgNameCPIndex]).value;
        String pkgName = ((StringCPEntry) env.constantPool[pkgCpEntry.pkgNameCPIndex]).value;
        String version = ((StringCPEntry) env.constantPool[pkgCpEntry.versionCPIndex]).value;
        return new PackageID(names.fromString(orgName),
                names.fromString(pkgName), names.fromString(version));
    }

    private void defineValueSpace(DataInputStream dataInStream, BFiniteType finiteType, BIRTypeReader typeReader)
            throws IOException {
        BType valueType = typeReader.readTypeFromCp();
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

        finiteType.addValue(litExpr);
    }

    private BLangLiteral createLiteralBasedOnType(BType valueType) {
        NodeKind nodeKind = valueType.tag <= TypeTags.DECIMAL ? NodeKind.NUMERIC_LITERAL : NodeKind.LITERAL;
        return nodeKind == NodeKind.LITERAL ? (BLangLiteral) TreeBuilder.createLiteralExpression() :
                (BLangLiteral) TreeBuilder.createNumericLiteralExpression();
    }
}

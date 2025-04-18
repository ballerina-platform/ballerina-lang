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

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.types.Atom;
import io.ballerina.types.AtomicType;
import io.ballerina.types.BasicTypeBitSet;
import io.ballerina.types.Bdd;
import io.ballerina.types.CellAtomicType;
import io.ballerina.types.CellSemType;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.EnumerableCharString;
import io.ballerina.types.EnumerableDecimal;
import io.ballerina.types.EnumerableFloat;
import io.ballerina.types.EnumerableString;
import io.ballerina.types.Env;
import io.ballerina.types.FixedLengthArray;
import io.ballerina.types.FunctionAtomicType;
import io.ballerina.types.ListAtomicType;
import io.ballerina.types.MappingAtomicType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.PredefinedTypeEnv;
import io.ballerina.types.ProperSubtypeData;
import io.ballerina.types.RecAtom;
import io.ballerina.types.SemType;
import io.ballerina.types.TypeAtom;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.CharStringSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.NonCharStringSubtype;
import io.ballerina.types.subtypedata.Range;
import io.ballerina.types.subtypedata.StringSubtype;
import io.ballerina.types.subtypedata.XmlSubtype;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.Annotatable;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.types.ConstrainedType;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.ByteCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.FloatCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.IntegerCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.PackageCPEntry;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry.StringCPEntry;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeParamAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEnumSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourceFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourcePathSegmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.semantics.model.types.SemNamedType;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
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
import java.math.BigDecimal;
import java.math.BigInteger;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static io.ballerina.types.PredefinedType.BDD_REC_ATOM_READONLY;
import static org.ballerinalang.model.symbols.SymbolOrigin.COMPILED_SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.ballerinalang.model.symbols.SymbolOrigin.toOrigin;
import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;
import static org.wso2.ballerinalang.util.LambdaExceptionUtils.rethrow;

/**
 * This class is responsible for reading the compiled package file (bir) and creating a package symbol.
 *
 * @since 0.995.0
 */
public class BIRPackageSymbolEnter {

    private final PackageCache packageCache;
    private final SymbolResolver symbolResolver;
    private final SymbolTable symTable;
    private final Names names;
    private final TypeParamAnalyzer typeParamAnalyzer;
    private final Types types;
    private BIRTypeReader typeReader;

    private BIRPackageSymbolEnv env;
    private List<BStructureTypeSymbol> structureTypes; // TODO find a better way
    private BStructureTypeSymbol currentStructure = null;
    private final LinkedList<Object> compositeStack = new LinkedList<>();
    private final Env typeEnv;
    private AtomOffsets offsets;

    private static final CompilerContext.Key<BIRPackageSymbolEnter> COMPILED_PACKAGE_SYMBOL_ENTER_KEY =
            new CompilerContext.Key<>();

    private final Map<String, BVarSymbol> globalVarMap = new HashMap<>();

    public static BIRPackageSymbolEnter getInstance(CompilerContext context) {
        BIRPackageSymbolEnter packageReader = context.get(COMPILED_PACKAGE_SYMBOL_ENTER_KEY);
        if (packageReader == null) {
            packageReader = new BIRPackageSymbolEnter(context);
        }

        return packageReader;
    }

    private BIRPackageSymbolEnter(CompilerContext context) {
        context.put(COMPILED_PACKAGE_SYMBOL_ENTER_KEY, this);

        this.packageCache = PackageCache.getInstance(context);
        this.symbolResolver = SymbolResolver.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.typeParamAnalyzer = TypeParamAnalyzer.getInstance(context);
        this.types = Types.getInstance(context);
        this.typeEnv = symTable.typeEnv();
        this.offsets = null;
    }

    public BPackageSymbol definePackage(PackageID packageId, byte[] packageBinaryContent) {
        BPackageSymbol pkgSymbol = definePackage(packageId, new ByteArrayInputStream(packageBinaryContent));

        // Strip magic value (4 bytes) and the version (2 bytes) off from the binary content of the package.
        byte[] modifiedPkgBinaryContent = Arrays.copyOfRange(
                packageBinaryContent, 8, packageBinaryContent.length);
        pkgSymbol.birPackageFile = new CompiledBinaryFile.BIRPackageFile(modifiedPkgBinaryContent);
        SymbolEnv builtinEnv = this.symTable.pkgEnvMap.get(symTable.langAnnotationModuleSymbol);
        SymbolEnv pkgEnv = SymbolEnv.createPkgEnv(null, pkgSymbol.scope, builtinEnv);
        this.symTable.pkgEnvMap.put(pkgSymbol, pkgEnv);
        return pkgSymbol;
    }

    private BPackageSymbol definePackage(PackageID packageId, InputStream programFileInStream) {
        // TODO packageID --> package to be loaded. this is required for error reporting..
        try (DataInputStream dataInStream = new DataInputStream(programFileInStream)) {
            BIRPackageSymbolEnv prevEnv = this.env;
            this.env = new BIRPackageSymbolEnv();
            this.env.requestedPackageId = packageId;

            BPackageSymbol pkgSymbol = definePackage(dataInStream);
            this.env = prevEnv;
            return pkgSymbol;
        } catch (Throwable e) {
            throw new BLangCompilerException("failed to load the module '" + packageId.toString() + "' from its BIR" +
                    (e.getMessage() != null ? (" due to: " + e.getMessage()) : ""), e);
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
        String moduleName = ((StringCPEntry) this.env.constantPool[pkgCpEntry.moduleNameCPIndex]).value;
        String pkgVersion = ((StringCPEntry) this.env.constantPool[pkgCpEntry.versionCPIndex]).value;

        PackageID pkgId = createPackageID(orgName, pkgName, moduleName, pkgVersion);
        this.env.pkgSymbol = Symbols.createPackageSymbol(pkgId, this.symTable, COMPILED_SOURCE);
        this.offsets = AtomOffsets.from(typeEnv);

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

        // Define service declarations
        defineSymbols(dataInStream, rethrow(this::defineServiceDeclarations));

        populateReferencedFunctions();

        this.typeReader = null;
        this.offsets = null;
        return this.env.pkgSymbol;
    }

    private void populateReferencedFunctions() {
        for (BStructureTypeSymbol structureTypeSymbol : this.structureTypes) {
            BType referredStructureTypeSymbol = Types.getImpliedType(structureTypeSymbol.type);
            if (referredStructureTypeSymbol.tag == TypeTags.OBJECT) {
                BObjectType objectType = (BObjectType) referredStructureTypeSymbol;
                for (BType ref : objectType.typeInclusions) {
                    BType typeRef = Types.getImpliedType(ref);
                    if (typeRef.tsymbol == null || typeRef.tsymbol.kind != SymbolKind.OBJECT) {
                        continue;
                    }

                    List<BAttachedFunction> attachedFunctions = ((BObjectTypeSymbol) typeRef.tsymbol).attachedFuncs;
                    for (BAttachedFunction function : attachedFunctions) {
                        if (Symbols.isPrivate(function.symbol)) {
                            // we should not copy private functions.
                            continue;
                        }
                        String referencedFuncName = function.funcName.value;
                        Name funcName = Names.fromString(
                                Symbols.getAttachedFuncSymbolName(structureTypeSymbol.name.value, referencedFuncName));
                        Scope.ScopeEntry matchingObjFuncSym = objectType.tsymbol.scope.lookup(funcName);
                        if (matchingObjFuncSym == NOT_FOUND_ENTRY) {
                            structureTypeSymbol.attachedFuncs.add(function);
                            ((BObjectTypeSymbol) structureTypeSymbol).referencedFunctions.add(function);
                        }
                    }
                }
            }
        }
    }

    private void readTypeDefBodies(DataInputStream dataInStream) throws IOException {
        dataInStream.readInt(); // ignore the size
        for (BStructureTypeSymbol structureTypeSymbol : this.structureTypes) {
            this.currentStructure = structureTypeSymbol;
            defineSymbols(dataInStream, rethrow(this::defineFunction));

            // read and ignore the type references
            defineSymbols(dataInStream, rethrow(this::readBType));
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
                return new CPEntry.PackageCPEntry(dataInStream.readInt(), dataInStream.readInt(),
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
        String moduleName = getStringCPEntryValue(dataInStream);
        String pkgVersion = getStringCPEntryValue(dataInStream);
        PackageID importPkgID = createPackageID(orgName, pkgName, moduleName, pkgVersion);
        BPackageSymbol importPackageSymbol = packageCache.getSymbol(importPkgID);
        //TODO: after bala_change try to not to add to scope, it's duplicated with 'imports'
        // Define the import package with the alias being the package name
        if (importPackageSymbol == null) {
            throw new BLangCompilerException("cannot resolve module " + importPkgID);
        }
        this.env.pkgSymbol.scope.define(importPkgID.name, importPackageSymbol);
        this.env.pkgSymbol.imports.add(importPackageSymbol);
    }

    private void defineFunction(DataInputStream dataInStream) throws IOException {
        Location pos = readPosition(dataInStream);

        // Consider attached functions.. remove the first variable
        String funcName = getStringCPEntryValue(dataInStream);
        String funcOrigName = getStringCPEntryValue(dataInStream);
        String workerName = getStringCPEntryValue(dataInStream);
        long flags = dataInStream.readLong();
        byte origin = dataInStream.readByte();

        BInvokableType funcType = (BInvokableType) readBType(dataInStream);
        BInvokableSymbol invokableSymbol =
                Symbols.createFunctionSymbol(flags, Names.fromString(funcName), Names.fromString(funcOrigName),
                                             this.env.pkgSymbol.pkgID, funcType, this.env.pkgSymbol,
                                             Symbols.isFlagOn(flags, Flags.NATIVE), pos, toOrigin(origin));
        invokableSymbol.source = pos.lineRange().fileName();
        invokableSymbol.retType = funcType.retType;

        Scope scopeToDefine = this.env.pkgSymbol.scope;

        boolean isResourceFunction = dataInStream.readBoolean();

        if (this.currentStructure != null) {
            BType attachedType = Types.getImpliedType(this.currentStructure.type);

            // Update the symbol
            invokableSymbol.owner = attachedType.tsymbol;
            invokableSymbol.name =
                    Names.fromString(Symbols.getAttachedFuncSymbolName(attachedType.tsymbol.name.value, funcName));
            if (attachedType.tag == TypeTags.OBJECT || attachedType.tag == TypeTags.RECORD) {
                scopeToDefine = attachedType.tsymbol.scope;
                if (isResourceFunction) {
                    int pathParamCount = dataInStream.readInt();
                    List<BVarSymbol> pathParams = new ArrayList<>(pathParamCount);
                    for (int i = 0; i < pathParamCount; i++) {
                        Name pathParamName = Names.fromString(getStringCPEntryValue(dataInStream));
                        BType paramType = readBType(dataInStream);
                        BVarSymbol varSymbol = new BVarSymbol(0, pathParamName, this.env.pkgSymbol.pkgID,
                                paramType, null, symTable.builtinPos, COMPILED_SOURCE);
                        pathParams.add(varSymbol);
                    }

                    boolean restPathParamExist = dataInStream.readBoolean();
                    BVarSymbol restPathParam = null;
                    if (restPathParamExist) {
                        Name pathParamName = Names.fromString(getStringCPEntryValue(dataInStream));
                        BType paramType = readBType(dataInStream);
                        restPathParam = new BVarSymbol(0, pathParamName, this.env.pkgSymbol.pkgID, paramType,
                                null, symTable.builtinPos, COMPILED_SOURCE);
                    }

                    int resourcePathCount = dataInStream.readInt();
                    List<Name> resourcePath = new ArrayList<>(resourcePathCount);
                    List<Location> resourcePathSegmentPosList = new ArrayList<>(resourcePathCount);
                    List<BType> pathSegmentTypeList = new ArrayList<>(resourcePathCount);
                    for (int i = 0; i < resourcePathCount; i++) {
                        resourcePath.add(Names.fromString(getStringCPEntryValue(dataInStream)));
                        resourcePathSegmentPosList.add(readPosition(dataInStream));
                        pathSegmentTypeList.add(readBType(dataInStream));
                    }

                    Name accessor = Names.fromString(getStringCPEntryValue(dataInStream));

                    BResourceFunction resourceFunction = new BResourceFunction(Names.fromString(funcName),
                            invokableSymbol, funcType, accessor, pathParams, restPathParam, symTable.builtinPos);

                    // If it is a resource function, attached type should be an object
                    BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) attachedType.tsymbol;
                    List<BResourcePathSegmentSymbol> pathSegmentSymbols = new ArrayList<>(resourcePathCount);
                    BResourcePathSegmentSymbol parentResource = null;
                    for (int i = 0; i < resourcePathCount; i++) {
                        Name resourcePathSymbolName = resourcePath.get(i);
                        BType resourcePathSegmentType = pathSegmentTypeList.get(i);

                        BResourcePathSegmentSymbol pathSym = Symbols.createResourcePathSegmentSymbol(
                                resourcePathSymbolName, env.pkgSymbol.pkgID, resourcePathSegmentType, objectTypeSymbol,
                                resourcePathSegmentPosList.get(i), parentResource, resourceFunction, COMPILED_SOURCE);

                        objectTypeSymbol.resourcePathSegmentScope.define(pathSym.name, pathSym);
                        pathSegmentSymbols.add(pathSym);
                        parentResource = pathSym;
                    }

                    resourceFunction.pathSegmentSymbols = pathSegmentSymbols;
                    objectTypeSymbol.attachedFuncs.add(resourceFunction);
                } else {
                    BAttachedFunction attachedFunc =
                            new BAttachedFunction(Names.fromString(funcName), invokableSymbol, funcType,
                                    symTable.builtinPos);
                    BStructureTypeSymbol structureTypeSymbol = (BStructureTypeSymbol) attachedType.tsymbol;
                    if (Names.USER_DEFINED_INIT_SUFFIX.value.equals(funcName) ||
                            funcName.equals(Names.INIT_FUNCTION_SUFFIX.value)) {
                        ((BObjectTypeSymbol) structureTypeSymbol).initializerFunc = attachedFunc;
                    } else if (funcName.equals(Names.GENERATED_INIT_SUFFIX.value)) {
                        ((BObjectTypeSymbol) structureTypeSymbol).generatedInitializerFunc = attachedFunc;
                    } else {
                        structureTypeSymbol.attachedFuncs.add(attachedFunc);
                    }
                }
            }
        }

        // Read annotation attachments
        defineAnnotAttachmentSymbols(dataInStream, invokableSymbol);
        defineAnnotAttachmentSymbolsOnExternal(dataInStream, invokableSymbol);

        BTypeSymbol tsymbol = invokableSymbol.type.tsymbol;
        if (tsymbol == null) {
            // Skip return type annotations
            dataInStream.skip(dataInStream.readLong());
        } else {
            ((BInvokableTypeSymbol) tsymbol).returnTypeAnnots.addAll(readAnnotAttachmentSymbols(dataInStream,
                                                                                                invokableSymbol));
        }

        // set parameter symbols to the function symbol
        setParamSymbols(invokableSymbol, dataInStream);

        defineMarkDownDocAttachment(invokableSymbol, readDocBytes(dataInStream));

        defineGlobalVarDependencies(invokableSymbol, dataInStream);

        dataInStream.skip(dataInStream.readLong()); // read and skip scope table info

        dataInStream.skip(dataInStream.readLong()); // read and skip method body

        scopeToDefine.define(invokableSymbol.name, invokableSymbol);
    }

    private void defineGlobalVarDependencies(BInvokableSymbol invokableSymbol, DataInputStream dataInStream)
            throws IOException {

        long length = dataInStream.readInt();
        for (int i = 0; i < length; i++) {
            String globalVarName = getStringCPEntryValue(dataInStream.readInt());
            invokableSymbol.dependentGlobalVars.add(this.globalVarMap.get(globalVarName));
        }
    }

    private void defineTypeDef(DataInputStream dataInStream) throws IOException {
        Location pos = readPosition(dataInStream);
        String typeDefName = getStringCPEntryValue(dataInStream);
        String typeDefOrigName = getStringCPEntryValue(dataInStream);

        long flags = dataInStream.readLong();
        byte origin = dataInStream.readByte();

        byte[] docBytes = readDocBytes(dataInStream);

        BType type = readBType(dataInStream);

        BTypeReferenceType referenceType = null;
        boolean hasReferenceType = dataInStream.readBoolean();
        if (hasReferenceType) {
            if (type.tag == TypeTags.TYPEREFDESC && Objects.equals(type.tsymbol.name.value, typeDefName)
                    && type.tsymbol.owner == this.env.pkgSymbol) {
                referenceType = (BTypeReferenceType) type;
                referenceType.tsymbol.pos = pos;
            } else {
                BTypeSymbol typeSymbol = new BTypeSymbol(SymTag.TYPE_REF, flags, Names.fromString(typeDefName),
                        this.env.pkgSymbol.pkgID, type, this.env.pkgSymbol, pos, COMPILED_SOURCE);
                referenceType = new BTypeReferenceType(type, typeSymbol, flags);
            }
        }

        if (type.tag == TypeTags.INVOKABLE) {
            setInvokableTypeSymbol((BInvokableType) type);
        }

        // Temp solution to add abstract flag if available TODO find a better approach
        boolean isClass = Symbols.isFlagOn(type.tsymbol.flags, Flags.CLASS);
        flags = isClass ? flags | Flags.CLASS : flags;

        // Temp solution to add client flag if available TODO find a better approach
        flags = Symbols.isFlagOn(type.tsymbol.flags, Flags.CLIENT) ? flags | Flags.CLIENT : flags;

        BSymbol symbol;
        boolean isEnum = Symbols.isFlagOn(type.tsymbol.flags, Flags.ENUM);
        if (isClass || isEnum) {
            symbol = type.tsymbol;
            symbol.pos = pos;
        } else {
            symbol = Symbols.createTypeDefinitionSymbol(flags, Names.fromString(typeDefName),
                    this.env.pkgSymbol.pkgID, type, this.env.pkgSymbol, pos, COMPILED_SOURCE);
            ((BTypeDefinitionSymbol) symbol).referenceType = referenceType;
        }
        symbol.originalName = Names.fromString(typeDefOrigName);
        symbol.origin = toOrigin(origin);
        symbol.flags = flags;

        defineMarkDownDocAttachment(symbol, docBytes);
        defineAnnotAttachmentSymbols(dataInStream,
                                     (isClass || isEnum || symbol.tag == SymTag.TYPE_DEF) ? (Annotatable) symbol :
                                             null);

        if (type.tsymbol.name == Names.EMPTY && type.tag != TypeTags.INVOKABLE) {
            type.tsymbol.name = symbol.name;
            type.tsymbol.originalName = symbol.originalName;
        }

        if (type.tag == TypeTags.RECORD || type.tag == TypeTags.OBJECT) {
            if (!isClass) {
                ((BStructureTypeSymbol) type.tsymbol).typeDefinitionSymbol = (BTypeDefinitionSymbol) symbol;
            }
            type.tsymbol.origin = toOrigin(origin);
            this.structureTypes.add((BStructureTypeSymbol) type.tsymbol);
        }

        this.env.pkgSymbol.scope.define(symbol.name, symbol);
    }

    private void skipPosition(DataInputStream dataInStream) throws IOException {
        // skip line start, line end, column start and column end
        for (int i = 0; i < 4; i++) {
            dataInStream.readInt();
        }
    }

    private void setInvokableTypeSymbol(BInvokableType invokableType) {
        if (Symbols.isFlagOn(invokableType.getFlags(), Flags.ANY_FUNCTION)) {
            return;
        }
        BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) invokableType.tsymbol;

        if (invokableType.restType != null) {
            tsymbol.restParam = new BVarSymbol(0, Names.EMPTY, this.env.pkgSymbol.pkgID, invokableType.restType, null,
                                               symTable.builtinPos, COMPILED_SOURCE);
        }

        tsymbol.returnType = invokableType.retType;
    }

    private void defineMarkDownDocAttachment(BSymbol symbol, byte[] docBytes) throws IOException {
        DataInputStream dataInStream = new DataInputStream(new ByteArrayInputStream(docBytes));
        boolean docPresent = dataInStream.readBoolean();
        if (!docPresent) {
            return;
        }

        int descCPIndex = dataInStream.readInt();
        int retDescCPIndex = dataInStream.readInt();
        int paramLength = dataInStream.readInt();
        MarkdownDocAttachment markdownDocAttachment = new MarkdownDocAttachment(paramLength);

        markdownDocAttachment.description = descCPIndex >= 0 ? getStringCPEntryValue(descCPIndex) : null;
        markdownDocAttachment.returnValueDescription
                = retDescCPIndex >= 0 ? getStringCPEntryValue(retDescCPIndex) : null;
        readAndSetParamDocumentation(dataInStream, markdownDocAttachment.parameters, paramLength);

        int deprecatedDescCPIndex = dataInStream.readInt();
        int deprecatedParamLength = dataInStream.readInt();
        markdownDocAttachment.deprecatedDocumentation = deprecatedDescCPIndex >= 0
                ? getStringCPEntryValue(deprecatedDescCPIndex) : null;
        readAndSetParamDocumentation(dataInStream, markdownDocAttachment.deprecatedParams, deprecatedParamLength);

        symbol.markdownDocumentation = markdownDocAttachment;
    }

    private void readAndSetParamDocumentation(DataInputStream inputStream, List<MarkdownDocAttachment.Parameter> params,
                                              int nParams) throws IOException {
        for (int i = 0; i < nParams; i++) {
            int nameCPIndex = inputStream.readInt();
            int paramDescCPIndex = inputStream.readInt();
            String name = nameCPIndex >= 0 ? getStringCPEntryValue(nameCPIndex) : null;
            String description = paramDescCPIndex >= 0 ? getStringCPEntryValue(paramDescCPIndex) : null;
            MarkdownDocAttachment.Parameter parameter = new MarkdownDocAttachment.Parameter(name, description);
            params.add(parameter);
        }
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
        return type;
    }

    private void addShapeCP(BType bType, int typeCpIndex) {
        this.env.constantPool[typeCpIndex] = new CPEntry.ShapeCPEntry(bType);
    }

    private void defineAnnotations(DataInputStream dataInStream) throws IOException {
        BAnnotationSymbol annotationSymbol = defineAnnotation(dataInStream);
        this.env.pkgSymbol.scope.define(annotationSymbol.name, annotationSymbol);
    }

    private BAnnotationSymbol defineAnnotation(DataInputStream dataInStream) throws IOException {
        int pkgCpIndex = dataInStream.readInt();
        PackageID pkgId = getPackageId(pkgCpIndex);

        String name = getStringCPEntryValue(dataInStream);
        String originalName = getStringCPEntryValue(dataInStream);

        long flags = dataInStream.readLong();
        byte origin = dataInStream.readByte();
        Location pos = readPosition(dataInStream);

        int attachPointCount = dataInStream.readInt();
        Set<AttachPoint> attachPoints = new HashSet<>(attachPointCount);

        for (int i = 0; i < attachPointCount; i++) {
            attachPoints.add(AttachPoint.getAttachmentPoint(getStringCPEntryValue(dataInStream),
                                                            dataInStream.readBoolean()));
        }

        BType annotationType = readBType(dataInStream);

        BPackageSymbol pkgSymbol = pkgId.equals(env.pkgSymbol.pkgID) ? this.env.pkgSymbol :
                packageCache.getSymbol(pkgId);
        BAnnotationSymbol annotationSymbol = Symbols.createAnnotationSymbol(flags, attachPoints, Names.fromString(name),
                                                                            Names.fromString(originalName),
                                                                            pkgId, null, pkgSymbol, pos,
                                                                            toOrigin(origin));
        annotationSymbol.type = new BAnnotationType(annotationSymbol);

        defineMarkDownDocAttachment(annotationSymbol, readDocBytes(dataInStream));
        defineAnnotAttachmentSymbols(dataInStream, annotationSymbol);

        if (annotationType != symTable.noType) { //TODO fix properly
            annotationSymbol.attachedType = annotationType;
        }
        return annotationSymbol;
    }

    private BAnnotationAttachmentSymbol defineAnnotationAttachmentSymbol(DataInputStream dataInStream, BSymbol owner)
            throws IOException {
        PackageID pkgId = getPackageId(dataInStream.readInt());
        Location pos = readPosition(dataInStream);
        Name annotTagRef = Names.fromString(getStringCPEntryValue(dataInStream.readInt()));

        boolean constAnnotation = dataInStream.readBoolean();

        if (!constAnnotation) {
            return new BAnnotationAttachmentSymbol(pkgId, annotTagRef, this.env.pkgSymbol.pkgID, owner, pos,
                                                   COMPILED_SOURCE, null);
        }

        BType constantValType = readBType(dataInStream);

        BConstantSymbol constantSymbol = new BConstantSymbol(0, Names.EMPTY, Names.EMPTY,
                                                             this.env.pkgSymbol.pkgID, null, constantValType,
                                                             owner, pos, COMPILED_SOURCE);
        constantSymbol.value = readConstLiteralValue(constantValType, dataInStream);
        constantSymbol.literalType = constantSymbol.value.type;
        return new BAnnotationAttachmentSymbol.BConstAnnotationAttachmentSymbol(pkgId, annotTagRef,
                                                                                this.env.pkgSymbol.pkgID, owner, pos,
                                                                                COMPILED_SOURCE, constantSymbol, null);
    }

    private void defineConstant(DataInputStream dataInStream) throws IOException {
        String constantName = getStringCPEntryValue(dataInStream);
        long flags = dataInStream.readLong();
        byte origin = dataInStream.readByte();
        Location pos = readPosition(dataInStream);

        byte[] docBytes = readDocBytes(dataInStream);

        BType type = readBType(dataInStream);
        Scope enclScope = this.env.pkgSymbol.scope;

        // Create the constant symbol.
        BConstantSymbol constantSymbol = new BConstantSymbol(flags, Names.fromString(constantName),
                                                             this.env.pkgSymbol.pkgID, null, type, enclScope.owner,
                                                             pos, toOrigin(origin));

        defineMarkDownDocAttachment(constantSymbol, docBytes);
        defineAnnotAttachmentSymbols(dataInStream, constantSymbol);

        // read and ignore constant value's byte chunk length.
        dataInStream.readLong();

        BType constantValType = readBType(dataInStream);
        constantSymbol.value = readConstLiteralValue(constantValType, dataInStream);
        constantSymbol.literalType = constantSymbol.value.type;

        // Define constant.
        enclScope.define(constantSymbol.name, constantSymbol);
    }

    private BLangConstantValue readConstLiteralValue(BType valueType, DataInputStream dataInStream) throws IOException {
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
                return new BLangConstantValue(dataInStream.readBoolean(), symTable.booleanType);
            case TypeTags.NIL:
                return new BLangConstantValue(null, symTable.nilType);
            case TypeTags.RECORD:
                int size = dataInStream.readInt();
                Map<String, BLangConstantValue> keyValuePairs = new LinkedHashMap<>();
                for (int i = 0; i < size; i++) {
                    String key = getStringCPEntryValue(dataInStream);
                    BType type = readBType(dataInStream);
                    BLangConstantValue value = readConstLiteralValue(type, dataInStream);
                    keyValuePairs.put(key, value);
                }
                return new BLangConstantValue(keyValuePairs, valueType);
            case TypeTags.TUPLE:
                int tupleSize = dataInStream.readInt();
                List<BLangConstantValue> members = new ArrayList<>(tupleSize);
                for (int i = 0; i < tupleSize; i++) {
                    BType type = readBType(dataInStream);
                    BLangConstantValue value = readConstLiteralValue(type, dataInStream);
                    members.add(value);
                }
                return new BLangConstantValue(members, valueType);
            case TypeTags.INTERSECTION:
                return readConstLiteralValue(((BIntersectionType) valueType).effectiveType, dataInStream);
            case TypeTags.TYPEREFDESC:
                return readConstLiteralValue(Types.getImpliedType(valueType), dataInStream);
            default:
                // TODO implement for other types
                throw new RuntimeException("unexpected type: " + valueType);
        }
    }

    private void defineServiceDeclarations(DataInputStream inputStream) throws IOException {
        String serviceName = getStringCPEntryValue(inputStream);
        String associatedClassName = getStringCPEntryValue(inputStream);
        long flags = inputStream.readLong();
        byte origin = inputStream.readByte();
        Location pos = readPosition(inputStream);

        BType type = null;
        if (inputStream.readBoolean()) {
            type = readBType(inputStream);
        }

        List<String> attachPoint = null;
        if (inputStream.readBoolean()) {
            attachPoint = new ArrayList<>();
            int nSegments = inputStream.readInt();
            for (int i = 0; i < nSegments; i++) {
                attachPoint.add(getStringCPEntryValue(inputStream));
            }
        }

        String attachPointLiteral = null;
        if (inputStream.readBoolean()) {
            attachPointLiteral = getStringCPEntryValue(inputStream);
        }

        BSymbol classSymbol = this.env.pkgSymbol.scope.lookup(Names.fromString(associatedClassName)).symbol;
        BServiceSymbol serviceDecl = new BServiceSymbol((BClassSymbol) classSymbol, flags,
                                                        Names.fromString(serviceName), this.env.pkgSymbol.pkgID, type,
                                                        this.env.pkgSymbol, pos, SymbolOrigin.toOrigin(origin));

        int nListeners = inputStream.readInt();
        for (int i = 0; i < nListeners; i++) {
            serviceDecl.addListenerType(readBType(inputStream));
        }
        
        serviceDecl.setAttachPointStringLiteral(attachPointLiteral);
        serviceDecl.setAbsResourcePath(attachPoint);
        this.env.pkgSymbol.scope.define(Names.fromString(serviceName), serviceDecl);
    }

    private void definePackageLevelVariables(DataInputStream dataInStream) throws IOException {
        Location pos = readPosition(dataInStream);
        dataInStream.readByte(); // Read and ignore the kind as it is anyway global variable
        String varName = getStringCPEntryValue(dataInStream);
        long flags = dataInStream.readLong();
        byte origin = dataInStream.readByte();

        byte[] docBytes = readDocBytes(dataInStream);

        // Create variable symbol
        BType varType = readBType(dataInStream);
        BType referredVarType = Types.getImpliedType(varType);
        Scope enclScope = this.env.pkgSymbol.scope;
        BVarSymbol varSymbol;
        if (referredVarType.tag == TypeTags.INVOKABLE) {
            BInvokableTypeSymbol bInvokableTypeSymbol = (BInvokableTypeSymbol) referredVarType.tsymbol;
            BInvokableSymbol invokableSymbol = new BInvokableSymbol(SymTag.VARIABLE, flags, Names.fromString(varName),
                    this.env.pkgSymbol.pkgID, referredVarType, enclScope.owner, symTable.builtinPos, toOrigin(origin));

            invokableSymbol.kind = SymbolKind.FUNCTION;
            if (bInvokableTypeSymbol != null) {
                invokableSymbol.params = bInvokableTypeSymbol.params;
                invokableSymbol.restParam = bInvokableTypeSymbol.restParam;
            }
            invokableSymbol.retType = ((BInvokableType) invokableSymbol.type).retType;
            varSymbol = invokableSymbol;
        } else {
            varSymbol = new BVarSymbol(flags, Names.fromString(varName), this.env.pkgSymbol.pkgID, varType,
                                       enclScope.owner, symTable.builtinPos, toOrigin(origin));
            if (varType.tsymbol != null && Symbols.isFlagOn(varType.tsymbol.flags, Flags.CLIENT)) {
                varSymbol.tag = SymTag.ENDPOINT;
            }
        }
        varSymbol.pos = pos;

        this.globalVarMap.put(varName, varSymbol);

        defineMarkDownDocAttachment(varSymbol, docBytes);
        defineAnnotAttachmentSymbols(dataInStream, varSymbol);

        enclScope.define(varSymbol.name, varSymbol);
    }

    private void setParamSymbols(BInvokableSymbol invokableSymbol, DataInputStream dataInStream)
            throws IOException {

        int requiredParamCount = dataInStream.readInt();

        BInvokableType invokableType = (BInvokableType) invokableSymbol.type;
        for (int i = 0; i < requiredParamCount; i++) {
            String paramName = getStringCPEntryValue(dataInStream);
            long flags = dataInStream.readLong();
            BVarSymbol varSymbol = new BVarSymbol(flags, Names.fromString(paramName), this.env.pkgSymbol.pkgID,
                                                  invokableType.paramTypes.get(i), invokableSymbol,
                                                  symTable.builtinPos, COMPILED_SOURCE);
            varSymbol.isDefaultable = ((flags & Flags.OPTIONAL) == Flags.OPTIONAL);
            defineAnnotAttachmentSymbols(dataInStream, varSymbol);
            invokableSymbol.params.add(varSymbol);
        }

        if (dataInStream.readBoolean()) { //if rest param exist
            String paramName = getStringCPEntryValue(dataInStream);
            BVarSymbol restParam = new BVarSymbol(0, Names.fromString(paramName), this.env.pkgSymbol.pkgID,
                                                  invokableType.restType, invokableSymbol, symTable.builtinPos,
                                                  COMPILED_SOURCE);
            invokableSymbol.restParam = restParam;
            defineAnnotAttachmentSymbols(dataInStream, restParam);
        }

        if (Symbols.isFlagOn(invokableSymbol.retType.getFlags(), Flags.PARAMETERIZED)) {
            Map<Name, BVarSymbol> paramsMap = new HashMap<>();
            for (BVarSymbol param : invokableSymbol.params) {
                if (paramsMap.put(param.getName(), param) != null) {
                    throw new IllegalStateException("duplicate key: " + param.getName());
                }
            }
            populateParameterizedType(invokableSymbol.retType, paramsMap, invokableSymbol);
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

    private void defineAnnotAttachmentSymbols(DataInputStream dataInStream, Annotatable owner) throws IOException {
        ((List<BAnnotationAttachmentSymbol>) owner.getAnnotations()).addAll(readAnnotAttachmentSymbols(dataInStream,
                                                                                                     (BSymbol) owner));
    }

    private void defineAnnotAttachmentSymbolsOnExternal(DataInputStream dataInStream,
                                                        BInvokableSymbol owner) throws IOException {
        if (Symbols.isFlagOn(owner.flags, Flags.NATIVE)) {
            owner.setAnnotationAttachmentsOnExternal(readAnnotAttachmentSymbols(dataInStream, owner));
        }
    }

    private List<BAnnotationAttachmentSymbol> readAnnotAttachmentSymbols(DataInputStream dataInStream, BSymbol owner)
            throws IOException {
        dataInStream.readLong(); // Read and skip annotation symbol info length.
        int annotSymbolCount = dataInStream.readInt();

        if (annotSymbolCount == 0) {
            return new ArrayList<>(0);
        }

        List<BAnnotationAttachmentSymbol> annotationAttachmentSymbols = new ArrayList<>(annotSymbolCount);
        for (int j = 0; j < annotSymbolCount; j++) {
            annotationAttachmentSymbols.add(defineAnnotationAttachmentSymbol(dataInStream, owner));
        }
        return annotationAttachmentSymbols;
    }

    /**
     * This method is used for filling the `paramSymbol` field in a parameterized type. Since we want to use the same
     * symbol of the parameter referred to by the type, we have to wait until the parameter symbols are defined to fill
     * in the `paramSymbol` field. Only types with constituent types are considered here since those are the only types
     * which can recursively hold a parameterized type.
     *
     * @param type      The return type of a function, which possibly contains a parameterized type
     * @param paramsMap A mapping between the parameter names and the parameter symbols of the function
     * @param invSymbol The symbol of the function
     */
    private void populateParameterizedType(BType type, final Map<Name, BVarSymbol> paramsMap,
                                           BInvokableSymbol invSymbol) {
        if (type == null) {
            return;
        }

        type = Types.getImpliedType(type);
        switch (type.tag) {
            case TypeTags.PARAMETERIZED_TYPE:
                BParameterizedType varType = (BParameterizedType) type;
                varType.paramSymbol = paramsMap.get(varType.name);
                varType.tsymbol = new BTypeSymbol(SymTag.TYPE, Flags.PARAMETERIZED | varType.paramSymbol.flags,
                                                  varType.paramSymbol.name, varType.paramSymbol.originalName,
                                                  varType.paramSymbol.pkgID, varType, invSymbol,
                                                  varType.paramSymbol.pos, VIRTUAL);
                break;
            case TypeTags.MAP:
            case TypeTags.FUTURE:
            case TypeTags.TYPEDESC:
                ConstrainedType constrainedType = (ConstrainedType) type;
                populateParameterizedType((BType) constrainedType.getConstraint(), paramsMap, invSymbol);
                break;
            case TypeTags.XML:
                populateParameterizedType(((BXMLType) type).constraint, paramsMap, invSymbol);
                break;
            case TypeTags.ARRAY:
                populateParameterizedType(((BArrayType) type).eType, paramsMap, invSymbol);
                break;
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) type;
                for (BType tupleMemberType : tupleType.getTupleTypes()) {
                    populateParameterizedType(tupleMemberType, paramsMap, invSymbol);
                }
                populateParameterizedType(tupleType.restType, paramsMap, invSymbol);
                break;
            case TypeTags.STREAM:
                BStreamType streamType = (BStreamType) type;
                populateParameterizedType(streamType.constraint, paramsMap, invSymbol);
                populateParameterizedType(streamType.completionType, paramsMap, invSymbol);
                break;
            case TypeTags.TABLE:
                BTableType tableType = (BTableType) type;
                populateParameterizedType(tableType.constraint, paramsMap, invSymbol);
                populateParameterizedType(tableType.keyTypeConstraint, paramsMap, invSymbol);
                break;
            case TypeTags.INVOKABLE:
                BInvokableType invokableType = (BInvokableType) type;
                if (Symbols.isFlagOn(invokableType.getFlags(), Flags.ANY_FUNCTION)) {
                    break;
                }
                for (BType t : invokableType.paramTypes) {
                    populateParameterizedType(t, paramsMap, invSymbol);
                }

                populateParameterizedType(invokableType.restType, paramsMap, invSymbol);
                populateParameterizedType(invokableType.retType, paramsMap, invSymbol);
                break;
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) type;
                for (BType t : unionType.getMemberTypes()) {
                    populateParameterizedType(t, paramsMap, invSymbol);
                }
                break;
        }
    }

    private Location readPosition(DataInputStream dataInStream) throws IOException {
        String cUnitName = getStringCPEntryValue(dataInStream);
        int sLine = dataInStream.readInt();
        int sCol = dataInStream.readInt();
        int eLine = dataInStream.readInt();
        int eCol = dataInStream.readInt();
        return new BLangDiagnosticLocation(cUnitName, sLine, eLine, sCol, eCol);
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

    private PackageID createPackageID(String orgName, String pkgName, String moduleName, String pkgVersion) {
        if (orgName == null || orgName.isEmpty()) {
            throw new BLangCompilerException("invalid module name '" + moduleName + "' in compiled package file");
        }

        return new PackageID(Names.fromString(orgName),
                Names.fromString(pkgName),
                Names.fromString(moduleName),
                Names.fromString(pkgVersion), null);
    }

    /**
     * This class holds compiled package specific information during the symbol enter phase of the compiled package.
     *
     * @since 0.970.0
     */
    private static class BIRPackageSymbolEnv {
        PackageID requestedPackageId;
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
        private final DataInputStream inputStream;
        private final PredefinedTypeEnv predefinedTypeEnv = PredefinedTypeEnv.getInstance();

        public BIRTypeReader(DataInputStream inputStream) {
            this.inputStream = inputStream;
        }

        private BType readTypeFromCp() throws IOException {
            return readBType(inputStream);
        }

        private BInvokableType setTSymbolForInvokableType(BInvokableType bInvokableType,
                                                          BType retType) throws IOException {
            BInvokableTypeSymbol tSymbol = (BInvokableTypeSymbol) bInvokableType.tsymbol;
            boolean hasTSymbol = inputStream.readBoolean();
            if (!hasTSymbol) {
                return bInvokableType;
            }

            int params = inputStream.readInt();
            for (int i = 0; i < params; i++) {
                String paramName = getStringCPEntryValue(inputStream);
                long paramFlags = inputStream.readLong();
                byte[] docBytes = readDocBytes(inputStream);
                BType fieldType = readTypeFromCp();

                BVarSymbol varSymbol = new BVarSymbol(paramFlags, Names.fromString(paramName), tSymbol.pkgID,
                                                      fieldType, tSymbol, symTable.builtinPos,
                                                      COMPILED_SOURCE);

                varSymbol.isDefaultable = ((paramFlags & Flags.OPTIONAL) == Flags.OPTIONAL);
                defineMarkDownDocAttachment(varSymbol, docBytes);
                tSymbol.params.add(varSymbol);
            }

            boolean hasRestParam = inputStream.readBoolean();
            if (hasRestParam) {
                String fieldName = getStringCPEntryValue(inputStream);
                long fieldFlags = inputStream.readLong();
                byte[] docBytes = readDocBytes(inputStream);
                BType fieldType = readTypeFromCp();

                BVarSymbol varSymbol = new BVarSymbol(fieldFlags, Names.fromString(fieldName), tSymbol.pkgID, fieldType,
                                                      tSymbol, symTable.builtinPos, COMPILED_SOURCE);
                defineMarkDownDocAttachment(varSymbol, docBytes);
                tSymbol.restParam = varSymbol;
            }

            tSymbol.returnType = retType;

            int defaultValues = inputStream.readInt();
            for (int i = 0; i < defaultValues; i++) {
                String paramName = getStringCPEntryValue(inputStream);
                BInvokableSymbol invokableSymbol = getSymbolOfClosure();
                tSymbol.defaultValues.put(paramName, invokableSymbol);
            }
            return bInvokableType;
        }

        private BInvokableSymbol getSymbolOfClosure() throws IOException {
            String name = getStringCPEntryValue(inputStream);
            long flags = inputStream.readLong();
            BType type = readTypeFromCp();
            int pkgCpIndex = inputStream.readInt();
            PackageID pkgId = getPackageId(pkgCpIndex);

            BInvokableSymbol invokableSymbol = Symbols.createFunctionSymbol(flags, Names.fromString(name),
                                               Names.fromString(name), pkgId, type, null, false,
                                               symTable.builtinPos, VIRTUAL);
            invokableSymbol.retType = invokableSymbol.type.getReturnType();

            int parameters = inputStream.readInt();
            for (int i = 0; i < parameters; i++) {
                String fieldName = getStringCPEntryValue(inputStream);
                long fieldFlags = inputStream.readLong();
                byte[] docBytes = readDocBytes(inputStream);
                BType fieldType = readTypeFromCp();
                BVarSymbol varSymbol = new BVarSymbol(fieldFlags, Names.fromString(fieldName), pkgId, fieldType, null,
                                                      symTable.builtinPos, COMPILED_SOURCE);
                defineMarkDownDocAttachment(varSymbol, docBytes);
                invokableSymbol.params.add(varSymbol);
            }
            return invokableSymbol;
        }

        public BType readType(int cpI) throws IOException {
            byte tag = inputStream.readByte();
            Name name = Names.fromString(getStringCPEntryValue(inputStream));
            long flags = inputStream.readLong();

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
                    return isImmutable(flags) ? getEffectiveImmutableType(symTable.jsonType) : symTable.jsonType;
                case TypeTags.XML:
                    BType constraintType = readTypeFromCp();
                    BXMLType mutableXmlType = new BXMLType(constraintType, symTable.xmlType.tsymbol);
                    if (Symbols.isFlagOn(flags, Flags.PARAMETERIZED)) {
                        mutableXmlType.addFlags(Flags.PARAMETERIZED);
                    }
                    return isImmutable(flags) ? getEffectiveImmutableType(mutableXmlType) : mutableXmlType;
                case TypeTags.NIL:
                    return symTable.nilType;
                case TypeTags.NEVER:
                    return symTable.neverType;
                case TypeTags.ANYDATA:
                    if (name.getValue().equals(Names.ANYDATA.getValue())) {
                        name = Names.EMPTY;
                    }
                    BType anydataNominalType = typeParamAnalyzer.getNominalType(symTable.anydataType, name, flags);
                    return isImmutable(flags) ? getEffectiveImmutableType(anydataNominalType,
                            symTable.anydataType.tsymbol.pkgID,
                            symTable.anydataType.tsymbol.owner) :
                            anydataNominalType;
                case TypeTags.RECORD:
                    int pkgCpIndex = inputStream.readInt();
                    PackageID pkgId = getPackageId(pkgCpIndex);

                    String recordName = getStringCPEntryValue(inputStream);
                    BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                                                                                Names.fromString(recordName),
                                                                                env.pkgSymbol.pkgID, null,
                                                                                env.pkgSymbol, symTable.builtinPos,
                                                                                COMPILED_SOURCE);
                    recordSymbol.scope = new Scope(recordSymbol);

                    BRecordType recordType = new BRecordType(symTable.typeEnv(), recordSymbol, flags);
                    recordSymbol.type = recordType;

                    compositeStack.push(recordType);
                    addShapeCP(recordType, cpI);

                    recordType.sealed = inputStream.readBoolean();
                    recordType.restFieldType = readTypeFromCp();

                    int recordFields = inputStream.readInt();
                    for (int i = 0; i < recordFields; i++) {
                        String fieldName = getStringCPEntryValue(inputStream);
                        long fieldFlags = inputStream.readLong();

                        byte[] docBytes = readDocBytes(inputStream);

                        BType fieldType = readTypeFromCp();

                        BVarSymbol varSymbol = new BVarSymbol(fieldFlags, Names.fromString(fieldName),
                                                              recordSymbol.pkgID, fieldType,
                                                              recordSymbol.scope.owner, symTable.builtinPos,
                                                              COMPILED_SOURCE);

                        defineAnnotAttachmentSymbols(inputStream, varSymbol);

                        defineMarkDownDocAttachment(varSymbol, docBytes);

                        BField structField = new BField(varSymbol.name, varSymbol.pos, varSymbol);
                        recordType.fields.put(structField.name.value, structField);
                        recordSymbol.scope.define(varSymbol.name, varSymbol);
                    }

                    recordType.typeInclusions = readTypeInclusions();

                    int defaultValues = inputStream.readInt();
                    for (int i = 0; i < defaultValues; i++) {
                        recordSymbol.defaultValues.put(getStringCPEntryValue(inputStream), getSymbolOfClosure());
                    }

//                    setDocumentation(varSymbol, attrData); // TODO fix

                    Object poppedRecordType = compositeStack.pop();
                    assert poppedRecordType == recordType;

                    if (pkgId.equals(env.pkgSymbol.pkgID)) {
                        return recordType;
                    }

                    SymbolEnv pkgEnv = symTable.pkgEnvMap.get(packageCache.getSymbol(pkgId));
                    return lookupSymbolInMainSpace(pkgEnv, Names.fromString(recordName));
                case TypeTags.TYPEDESC:
                    BTypedescType typedescType = new BTypedescType(symTable.typeEnv(), null, symTable.typeDesc.tsymbol);
                    typedescType.constraint = readTypeFromCp();
                    typedescType.setFlags(flags);
                    return typedescType;
                case TypeTags.TYPEREFDESC:
                    int pkgIndex = inputStream.readInt();
                    PackageID pkg = getPackageId(pkgIndex);
                    BPackageSymbol pkgSymbol = pkg.equals(env.pkgSymbol.pkgID) ? env.pkgSymbol :
                            packageCache.getSymbol(pkg);

                    String typeDefName = getStringCPEntryValue(inputStream);
                    BTypeSymbol typeSymbol = Symbols.createTypeSymbol(SymTag.TYPE_REF,
                            Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                            Names.fromString(typeDefName), pkg, null, pkgSymbol,
                            symTable.builtinPos, COMPILED_SOURCE);

                    BTypeReferenceType typeReferenceType = new BTypeReferenceType(null, typeSymbol, flags);
                    addShapeCP(typeReferenceType, cpI);
                    compositeStack.push(typeReferenceType);
                    typeReferenceType.referredType = readTypeFromCp();

                    Object poppedRefType = compositeStack.pop();
                    assert poppedRefType == typeReferenceType;
                    return typeReferenceType;
                case TypeTags.PARAMETERIZED_TYPE:
                    BParameterizedType type = new BParameterizedType(null, null, null, name, -1);
                    type.paramValueType = readTypeFromCp();
                    type.setFlags(flags);
                    type.paramIndex = inputStream.readInt();
                    return type;
                case TypeTags.STREAM:
                    BStreamType bStreamType = new BStreamType(symTable.typeEnv(), TypeTags.STREAM, null, null,
                            symTable.streamType.tsymbol);
                    bStreamType.constraint = readTypeFromCp();
                    bStreamType.completionType = readTypeFromCp();
                    bStreamType.setFlags(flags);
                    return bStreamType;
                case TypeTags.TABLE:
                    BTableType bTableType = new BTableType(symTable.typeEnv(), null,
                            symTable.tableType.tsymbol, flags);
                    bTableType.constraint = readTypeFromCp();

                    boolean hasFieldNameList = inputStream.readByte() == 1;
                    if (hasFieldNameList) {
                        int fieldNameListSize = inputStream.readInt();
                        bTableType.fieldNameList = new ArrayList<>(fieldNameListSize);
                        for (int i = 0; i < fieldNameListSize; i++) {
                            String fieldName = getStringCPEntryValue(inputStream);
                            bTableType.fieldNameList.add(fieldName);
                        }
                    }

                    boolean hasKeyConstraint = inputStream.readByte() == 1;
                    if (hasKeyConstraint) {
                        bTableType.keyTypeConstraint = readTypeFromCp();
                        if (bTableType.keyTypeConstraint.tsymbol == null) {
                            bTableType.keyTypeConstraint.tsymbol =
                                    Symbols.createTypeSymbol(SymTag.TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                                                             Names.EMPTY, env.pkgSymbol.pkgID,
                                                             bTableType.keyTypeConstraint, env.pkgSymbol.owner,
                                                             symTable.builtinPos, COMPILED_SOURCE);
                        }
                    }
                    return bTableType;
                case TypeTags.MAP:
                    BMapType bMapType = new BMapType(symTable.typeEnv(), TypeTags.MAP, null, symTable.mapType.tsymbol,
                            flags);
                    bMapType.constraint = readTypeFromCp();
                    return bMapType;
                case TypeTags.INVOKABLE:
                    BInvokableType bInvokableType = new BInvokableType(typeEnv, List.of(), null, null, null);
                    bInvokableType.tsymbol = Symbols.createInvokableTypeSymbol(SymTag.FUNCTION_TYPE, flags,
                            env.pkgSymbol.pkgID, null,
                            env.pkgSymbol.owner, symTable.builtinPos,
                            COMPILED_SOURCE);
                    bInvokableType.setFlags(flags);
                    if (inputStream.readBoolean()) {
                        // Return if an any function
                        return bInvokableType;
                    }
                    int paramCount = inputStream.readInt();
                    List<BType> paramTypes = new ArrayList<>(paramCount);
                    for (int i = 0; i < paramCount; i++) {
                        paramTypes.add(readTypeFromCp());
                    }
                    bInvokableType.paramTypes = paramTypes;
                    if (inputStream.readBoolean()) { //if rest param exist
                        bInvokableType.restType = readTypeFromCp();
                    }
                    bInvokableType.retType = readTypeFromCp();
                    return setTSymbolForInvokableType(bInvokableType, bInvokableType.retType);
                // All the above types are branded types
                case TypeTags.ANY:
                    return isImmutable(flags) ? BAnyType.newImmutableBAnyType() : new BAnyType(name, flags);
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
                    BTypeSymbol arrayTypeSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE,
                                                                           Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                                                                           Names.EMPTY, env.pkgSymbol.pkgID, null,
                                                                           env.pkgSymbol.owner, symTable.builtinPos,
                                                                           COMPILED_SOURCE);
                    BArrayType bArrayType =
                            new BArrayType(symTable.typeEnv(), null, arrayTypeSymbol, size, BArrayState.valueOf(state),
                            flags);
                    bArrayType.eType = readTypeFromCp();
                    return bArrayType;
                case TypeTags.UNION:
                    boolean isCyclic = inputStream.readByte() == 1;
                    boolean hasName = inputStream.readByte() == 1;
                    PackageID unionsPkgId = env.pkgSymbol.pkgID;
                    Name unionName = Names.EMPTY;
                    if (hasName) {
                        pkgCpIndex = inputStream.readInt();
                        unionsPkgId = getPackageId(pkgCpIndex);
                        String unionNameStr = getStringCPEntryValue(inputStream);
                        unionName = Names.fromString(unionNameStr);
                    }
                    BTypeSymbol unionTypeSymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE,
                            Flags.asMask(EnumSet.of(Flag.PUBLIC)), unionName, unionsPkgId,
                            null, env.pkgSymbol, symTable.builtinPos, COMPILED_SOURCE);

                    int unionMemberCount = inputStream.readInt();
                    BUnionType unionType =
                            BUnionType.create(types.typeEnv(), unionTypeSymbol, new LinkedHashSet<>(unionMemberCount));
                    unionType.name = unionName;

                    addShapeCP(unionType, cpI);
                    compositeStack.push(unionType);

                    unionType.setFlags(flags);
                    unionType.isCyclic = isCyclic;
                    for (int i = 0; i < unionMemberCount; i++) {
                        unionType.add(readTypeFromCp());
                    }

                    int unionOriginalMemberCount = inputStream.readInt();
                    LinkedHashSet<BType> originalMemberTypes = new LinkedHashSet<>(unionOriginalMemberCount);
                    for (int i = 0; i < unionOriginalMemberCount; i++) {
                        originalMemberTypes.add(readTypeFromCp());
                    }
                    unionType.setOriginalMemberTypes(originalMemberTypes);

                    Object poppedUnionType = compositeStack.pop();
                    assert poppedUnionType == unionType;

                    boolean isEnum = inputStream.readBoolean();
                    if (isEnum) {
                        readAndSetEnumSymbol(unionType, flags);
                    }

                    if (hasName) {
                        if (unionsPkgId.equals(env.pkgSymbol.pkgID)) {
                            return unionType;
                        } else {
                            pkgEnv = symTable.pkgEnvMap.get(packageCache.getSymbol(unionsPkgId));
                            if (pkgEnv != null) {
                                BType existingUnionType = lookupSymbolInMainSpace(pkgEnv, unionName);
                                if (existingUnionType != symTable.noType) {
                                    return existingUnionType;
                                }
                            }
                        }
                    }
                    return unionType;
                case TypeTags.INTERSECTION:
                    BTypeSymbol intersectionTypeSymbol = Symbols.createTypeSymbol(SymTag.INTERSECTION_TYPE,
                                                                                  Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                                                                                  Names.EMPTY, env.pkgSymbol.pkgID,
                                                                                  null, env.pkgSymbol,
                                                                                  symTable.builtinPos, COMPILED_SOURCE);
                    int intersectionMemberCount = inputStream.readInt();
                    LinkedHashSet<BType> constituentTypes = new LinkedHashSet<>(intersectionMemberCount);
                    for (int i = 0; i < intersectionMemberCount; i++) {
                        constituentTypes.add(readTypeFromCp());
                    }

                    BType effectiveType = readTypeFromCp();
                    return new BIntersectionType(intersectionTypeSymbol, constituentTypes, effectiveType, flags);
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
                    pkgCpIndex = inputStream.readInt();
                    pkgId = getPackageId(pkgCpIndex);
                    BPackageSymbol owner = packageCache.getSymbol(pkgId);
                    // It is assumed that if the owner is null, the error is defined within the current module.
                    BTypeSymbol errorSymbol;
                    if (owner != null) {
                        errorSymbol = new BErrorTypeSymbol(SymTag.ERROR, Flags.PUBLIC, Names.EMPTY, owner.pkgID,
                                null, owner, symTable.builtinPos, COMPILED_SOURCE);
                    } else {
                        errorSymbol = new BErrorTypeSymbol(SymTag.ERROR, Flags.PUBLIC, Names.EMPTY,
                                env.pkgSymbol.pkgID, null, env.pkgSymbol, symTable.builtinPos, COMPILED_SOURCE);
                    }
                    BErrorType errorType = new BErrorType(symTable.typeEnv(), errorSymbol);
                    addShapeCP(errorType, cpI);
                    compositeStack.push(errorType);
                    String errorName = getStringCPEntryValue(inputStream);
                    BType detailsType = readTypeFromCp();
                    errorType.detailType = detailsType;
                    errorType.setFlags(flags);
                    errorSymbol.type = errorType;
                    errorSymbol.pkgID = pkgId;
                    errorSymbol.originalName = errorSymbol.name = Names.fromString(errorName);
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
                    BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE,
                                                                           Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                                                                           Names.EMPTY, env.pkgSymbol.pkgID, null,
                                                                           env.pkgSymbol.owner, symTable.builtinPos,
                                                                           COMPILED_SOURCE);
                    int tupleMemberCount = inputStream.readInt();
                    List<BTupleMember> tupleMembers = new ArrayList<>(tupleMemberCount);
                    BSymbol tupleOwner = tupleTypeSymbol.owner;
                    PackageID tuplePkg = tupleTypeSymbol.pkgID;

                    for (int i = 0; i < tupleMemberCount; i++) {
                        String index = getStringCPEntryValue(inputStream);
                        long fieldFlags = inputStream.readLong();

                        BType memberType = readTypeFromCp();
                        BVarSymbol varSymbol = new BVarSymbol(fieldFlags, Names.fromString(index), tuplePkg,
                                memberType, tupleOwner, symTable.builtinPos, COMPILED_SOURCE);

                        defineAnnotAttachmentSymbols(inputStream, varSymbol);

                        tupleMembers.add(new BTupleMember(memberType, varSymbol));
                    }
                    BTupleType bTupleType = new BTupleType(symTable.typeEnv(), tupleTypeSymbol, tupleMembers);
                    bTupleType.setFlags(flags);

                    if (inputStream.readBoolean()) {
                        bTupleType.restType = readTypeFromCp();
                    }

                    return bTupleType;
                case TypeTags.FUTURE:
                    BFutureType bFutureType = new BFutureType(symTable.typeEnv(), null, symTable.futureType.tsymbol);
                    bFutureType.constraint = readTypeFromCp();
                    bFutureType.setFlags(flags);
                    return bFutureType;
                case TypeTags.FINITE:
                    String finiteTypeName = getStringCPEntryValue(inputStream);
                    long finiteTypeFlags = inputStream.readLong();
                    BTypeSymbol symbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, finiteTypeFlags,
                                                                  Names.fromString(finiteTypeName), env.pkgSymbol.pkgID,
                                                                  null, env.pkgSymbol, symTable.builtinPos,
                                                                  COMPILED_SOURCE);
                    symbol.scope = new Scope(symbol);
                    int valueSpaceLength = inputStream.readInt();
                    SemNamedType[] valueSpace = new SemNamedType[valueSpaceLength];
                    for (int i = 0; i < valueSpaceLength; i++) {
                        valueSpace[i] = readSemNamedType();
                    }
                    BFiniteType finiteType = new BFiniteType(symbol, valueSpace);
                    finiteType.setFlags(flags);
                    symbol.type = finiteType;
                    return finiteType;
                case TypeTags.OBJECT:
                    pkgCpIndex = inputStream.readInt();
                    pkgId = getPackageId(pkgCpIndex);

                    String objName = getStringCPEntryValue(inputStream);
                    long objSymFlags = inputStream.readLong();
                    BObjectTypeSymbol objectSymbol;

                    if (Symbols.isFlagOn(objSymFlags, Flags.CLASS)) {
                        objectSymbol = Symbols.createClassSymbol(objSymFlags, Names.fromString(objName),
                                env.pkgSymbol.pkgID, null, env.pkgSymbol,
                                symTable.builtinPos, COMPILED_SOURCE, false);
                    } else {
                        objectSymbol = Symbols.createObjectSymbol(objSymFlags, Names.fromString(objName),
                                env.pkgSymbol.pkgID, null, env.pkgSymbol,
                                symTable.builtinPos, COMPILED_SOURCE);
                    }

                    objectSymbol.scope = new Scope(objectSymbol);
                    BObjectType objectType;
                    // Below is a temporary fix, need to fix this properly by using the type tag
                    objectType = new BObjectType(symTable.typeEnv(), objectSymbol);
                    objectType.setFlags(flags);
                    objectSymbol.type = objectType;
                    addShapeCP(objectType, cpI);
                    compositeStack.push(objectType);
                    int fieldCount = inputStream.readInt();
                    for (int i = 0; i < fieldCount; i++) {
                        String fieldName = getStringCPEntryValue(inputStream);
                        long fieldFlags = inputStream.readLong();
                        boolean defaultable = inputStream.readBoolean();
                        byte[] docBytes = readDocBytes(inputStream);

                        BType fieldType = readTypeFromCp();
                        BVarSymbol objectVarSymbol = new BVarSymbol(fieldFlags, Names.fromString(fieldName),
                                                                    objectSymbol.pkgID, fieldType,
                                                                    objectSymbol.scope.owner, symTable.builtinPos,
                                                                    COMPILED_SOURCE);
                        objectVarSymbol.isDefaultable = defaultable;
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
                    boolean isImmutable = isImmutable(objectSymbol.flags);
                    for (int i = 0; i < funcCount; i++) {
                        //populate intersection type object functions
                        if (isImmutable) {
                            populateIntersectionTypeReferencedFunctions(inputStream, objectSymbol);
                        } else {
                            ignoreAttachedFunc();
                        }
                    }

                    objectType.typeInclusions = readTypeInclusions();
                    objectType.typeIdSet = readTypeIdSet(inputStream);

                    Object poppedObjType = compositeStack.pop();
                    assert poppedObjType == objectType;

                    if (pkgId.equals(env.pkgSymbol.pkgID)) {
                        return objectType;
                    }

                    pkgEnv = symTable.pkgEnvMap.get(packageCache.getSymbol(pkgId));
                    return lookupSymbolInMainSpace(pkgEnv, Names.fromString(objName));
                case TypeTags.BYTE_ARRAY:
                    // TODO fix
                    break;
                case TypeTags.FUNCTION_POINTER:
                    // TODO fix
                    break;
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
                    return isImmutable(flags) ? getEffectiveImmutableType(symTable.xmlElementType) :
                            symTable.xmlElementType;
                case TypeTags.XML_PI:
                    return isImmutable(flags) ? getEffectiveImmutableType(symTable.xmlPIType) : symTable.xmlPIType;
                case TypeTags.XML_COMMENT:
                    return isImmutable(flags) ? getEffectiveImmutableType(symTable.xmlCommentType) :
                            symTable.xmlCommentType;
                case TypeTags.XML_TEXT:
                    return symTable.xmlTextType;
                case TypeTags.REGEXP:
                    return symTable.regExpType;
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
            getStringCPEntryValue(inputStream);
            // TODO: check
            inputStream.readLong();
            readTypeFromCp();
        }

        private List<BType> readTypeInclusions() throws IOException {
            int nTypeInclusions = inputStream.readInt();
            List<BType> typeInclusions = new ArrayList<>(nTypeInclusions);
            for (int i = 0; i < nTypeInclusions; i++) {
                BType inclusion = readTypeFromCp();
                typeInclusions.add(inclusion);
            }
            return typeInclusions;
        }

        private void readAndSetEnumSymbol(BUnionType unionType, long flags) throws IOException {
            PackageID enumPkgId = getPackageId(inputStream.readInt());
            String enumName = getStringCPEntryValue(inputStream);
            int memberCount = inputStream.readInt();
            BSymbol pkgSymbol = packageCache.getSymbol(enumPkgId);

            // pkg symbol will be null if it's an enum in the current module.
            if (pkgSymbol == null) {
                pkgSymbol = env.pkgSymbol;
            }

            SymbolEnv enumPkgEnv = symTable.pkgEnvMap.get(pkgSymbol);

            // pkg env will be null if it's an enum in the current module.
            if (enumPkgEnv == null) {
                enumPkgEnv = SymbolEnv.createPkgEnv(null, env.pkgSymbol.scope, null);
            }

            List<BConstantSymbol> members = new ArrayList<>();
            for (int i = 0; i < memberCount; i++) {
                String memName = getStringCPEntryValue(inputStream);
                BSymbol sym = symbolResolver.lookupSymbolInMainSpace(enumPkgEnv, Names.fromString(memName));
                members.add((BConstantSymbol) sym);
            }

            unionType.tsymbol = new BEnumSymbol(members, flags, Names.fromString(enumName), pkgSymbol.pkgID, unionType,
                                                pkgSymbol, symTable.builtinPos, COMPILED_SOURCE);
        }

        private void populateIntersectionTypeReferencedFunctions(DataInputStream inputStream,
                                                                 BObjectTypeSymbol objectSymbol) throws IOException {
            String attachedFuncName = getStringCPEntryValue(inputStream);
            String attachedFuncOrigName = getStringCPEntryValue(inputStream);
            long attachedFuncFlags = inputStream.readLong();
            BInvokableType attachedFuncType = (BInvokableType) readTypeFromCp();
            Name funcName = Names.fromString(Symbols.getAttachedFuncSymbolName(
                    objectSymbol.name.value, attachedFuncName));
            Name funcOrigName = Names.fromString(attachedFuncOrigName);
            BInvokableSymbol attachedFuncSymbol =
                    Symbols.createFunctionSymbol(attachedFuncFlags, funcName, funcOrigName,
                            env.pkgSymbol.pkgID, attachedFuncType,
                            env.pkgSymbol, false, symTable.builtinPos,
                            COMPILED_SOURCE);
            BAttachedFunction attachedFunction = new BAttachedFunction(Names.fromString(attachedFuncName),
                    attachedFuncSymbol, attachedFuncType, symTable.builtinPos);

            setInvokableTypeSymbol(attachedFuncType);

            if (!Symbols.isFlagOn(attachedFuncType.getFlags(), Flags.ANY_FUNCTION)) {
                BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) attachedFuncType.tsymbol;
                attachedFuncSymbol.params = tsymbol.params;
                attachedFuncSymbol.restParam = tsymbol.restParam;
                attachedFuncSymbol.retType = tsymbol.returnType;
            }

            objectSymbol.referencedFunctions.add(attachedFunction);
            objectSymbol.attachedFuncs.add(attachedFunction);
            objectSymbol.scope.define(funcName, attachedFuncSymbol);
        }

        private Optional<String> readNullableString() throws IOException {
            boolean hasNonNullString = inputStream.readBoolean();
            if (hasNonNullString) {
                return Optional.of(getStringCPEntryValue(inputStream));
            } else {
                return Optional.empty();
            }
        }

        private SemNamedType readSemNamedType() throws IOException {
            SemType semType = readSemType();
            Optional<String> optName = readNullableString();
            return new SemNamedType(semType, optName);
        }

        // --------------------------------------- Read SemType ----------------------------------------------

        private SemType readSemType() throws IOException {
            if (!inputStream.readBoolean()) {
                return null;
            }

            if (inputStream.readBoolean()) {
                int bitset = inputStream.readInt();
                return BasicTypeBitSet.from(bitset);
            }

            int all = inputStream.readInt();
            int some = inputStream.readInt();
            byte subtypeDataListLength = inputStream.readByte();
            ProperSubtypeData[] subtypeList = new ProperSubtypeData[subtypeDataListLength];
            for (int i = 0; i < subtypeDataListLength; i++) {
                subtypeList[i] = readProperSubtypeData();
            }
            return createSemType(all, some, subtypeList);
        }

        private ProperSubtypeData readProperSubtypeData() throws IOException {
            switch (inputStream.readByte()) {
                case 1:
                    return readBdd();
                case 2:
                    return readIntSubtype();
                case 3:
                    return BooleanSubtype.from(inputStream.readBoolean());
                case 4:
                    return readFloatSubtype();
                case 5:
                    return readDecimalSubType();
                case 6:
                    return readStringSubtype();
                case 7:
                    return readXmlSubtype();
                default:
                    throw new IllegalStateException("Unexpected ProperSubtypeData kind");
            }
        }

        private Bdd readBdd() throws IOException {
            boolean isBddNode = inputStream.readBoolean();
            if (isBddNode) {
                return readBddNode();
            } else {
                boolean isAll = inputStream.readBoolean();
                return isAll ? BddAllOrNothing.bddAll() : BddAllOrNothing.bddNothing();
            }
        }

        enum AtomKind {
            REC,
            INLINED,
            TYPE
        }

        private AtomKind readAtomKind() throws IOException {
            return switch (inputStream.readByte()) {
                case 0 -> AtomKind.REC;
                case 1 -> AtomKind.INLINED;
                case 2 -> AtomKind.TYPE;
                default -> throw new IllegalStateException("Unexpected AtomKind kind");
            };
        }

        private BddNode readBddNode() throws IOException {
            AtomKind atomKind = readAtomKind();
            Atom atom = switch (atomKind) {
                case REC -> readRecAtom();
                case INLINED -> readInlinedAtom();
                case TYPE -> {
                    TypeAtom typeAtom = readTypeAtom();
                    typeEnv.deserializeTypeAtom(typeAtom);
                    yield typeAtom;
                }
            };

            Bdd left = readBdd();
            Bdd middle = readBdd();
            Bdd right = readBdd();
            return BddNode.create(atom, left, middle, right);
        }

        private Atom readInlinedAtom() throws IOException {
            int recAtomIndex = inputStream.readInt();
            assert recAtomIndex != BDD_REC_ATOM_READONLY;
            AtomicType atomicType = readTypeAtom().atomicType();
            Atom.Kind kind;
            if (atomicType instanceof MappingAtomicType) {
                recAtomIndex += offsets.mappingOffset();
                kind = Atom.Kind.MAPPING_ATOM;
            } else if (atomicType instanceof ListAtomicType) {
                recAtomIndex += offsets.listOffset();
                kind = Atom.Kind.LIST_ATOM;
            } else if (atomicType instanceof FunctionAtomicType) {
                recAtomIndex += offsets.functionOffset();
                kind = Atom.Kind.FUNCTION_ATOM;
            } else {
                throw new IllegalStateException("Unexpected inlined atomicType kind");
            }
            typeEnv.insertRecAtomAtIndex(recAtomIndex, atomicType);
            RecAtom recAtom = RecAtom.createRecAtom(recAtomIndex);
            recAtom.setKind(kind);
            return recAtom;
        }

        private TypeAtom readTypeAtom() throws IOException {
            int index = inputStream.readInt() + offsets.atomOffset();
            AtomicType atomicType = switch (inputStream.readByte()) {
                case 1 -> readMappingAtomicType();
                case 2 -> readListAtomicType();
                case 3 -> readFunctionAtomicType();
                case 4 -> readCellAtomicType();
                default -> throw new IllegalStateException("Unexpected atomicType kind");
            };
            return TypeAtom.createTypeAtom(index, atomicType);
        }

        private RecAtom readRecAtom() throws IOException {
            int index = inputStream.readInt();
            Optional<RecAtom> predefinedRecAtom = predefinedTypeEnv.getPredefinedRecAtom(index);
            if (predefinedRecAtom.isPresent()) {
                return predefinedRecAtom.get();
            }
            int kindOrdinal = inputStream.readInt();
            Atom.Kind kind = Atom.Kind.values()[kindOrdinal];
            int offset = switch (kind) {
                case LIST_ATOM -> offsets.listOffset();
                case FUNCTION_ATOM -> offsets.functionOffset();
                case MAPPING_ATOM -> offsets.mappingOffset();
                case DISTINCT_ATOM -> (-offsets.distinctOffset());
                case XML_ATOM -> 0;
                case CELL_ATOM -> throw new IllegalStateException("Cell atom cannot be recursive");
            };
            index += offset;
            RecAtom recAtom = RecAtom.createRecAtom(index);
            recAtom.setKind(kind);
            return recAtom;

        }

        private CellAtomicType readCellAtomicType() throws IOException {
            SemType ty = readSemType();
            byte ordinal = inputStream.readByte();
            CellAtomicType.CellMutability mut = CellAtomicType.CellMutability.values()[ordinal];
            return CellAtomicType.from(ty, mut);
        }

        private MappingAtomicType readMappingAtomicType() throws IOException {
            int namesLength = inputStream.readInt();
            String[] names = new String[namesLength];
            for (int i = 0; i < namesLength; i++) {
                names[i] = getStringCPEntryValue(inputStream);
            }

            int typesLength = inputStream.readInt();
            CellSemType[] types = new CellSemType[typesLength];
            for (int i = 0; i < typesLength; i++) {
                types[i] = (CellSemType) readSemType();
            }

            CellSemType rest = (CellSemType) readSemType();
            return MappingAtomicType.from(names, types, rest);
        }

        private ListAtomicType readListAtomicType() throws IOException {
            int initialLength = inputStream.readInt();
            List<CellSemType> initial = new ArrayList<>(initialLength);
            for (int i = 0; i < initialLength; i++) {
                initial.add((CellSemType) readSemType());
            }

            int fixedLength = inputStream.readInt();
            FixedLengthArray members = FixedLengthArray.from(initial, fixedLength);

            CellSemType rest = (CellSemType) readSemType();
            return ListAtomicType.from(members, rest);
        }

        private static ComplexSemType createSemType(int all, int some, ProperSubtypeData[] subtypeList) {
            if (some == PredefinedType.CELL.bitset && all == 0) {
                return CellSemType.from(subtypeList);
            }
            return ComplexSemType.createComplexSemType(all, some, subtypeList);
        }

        private FunctionAtomicType readFunctionAtomicType() throws IOException {
            SemType paramType = readSemType();
            SemType retType = readSemType();
            SemType qualifiers = readSemType();
            boolean isGeneric = inputStream.readBoolean();
            return isGeneric ? FunctionAtomicType.genericFrom(paramType, retType, qualifiers) :
                    FunctionAtomicType.from(paramType, retType, qualifiers);
        }

        private IntSubtype readIntSubtype() throws IOException {
            int rangesLength = inputStream.readInt();
            Range[] ranges = new Range[rangesLength];
            for (int i = 0; i < rangesLength; i++) {
                long min = inputStream.readLong();
                long max = inputStream.readLong();
                ranges[i] = new Range(min, max);

            }
            return IntSubtype.createIntSubtype(ranges);
        }

        private FloatSubtype readFloatSubtype() throws IOException {
            boolean allowed = inputStream.readBoolean();
            int valuesLength = inputStream.readInt();
            EnumerableFloat[] values = new EnumerableFloat[valuesLength];
            for (int i = 0; i < valuesLength; i++) {
                values[i] = EnumerableFloat.from(inputStream.readDouble());
            }

            return (FloatSubtype) FloatSubtype.createFloatSubtype(allowed, values);
        }

        private DecimalSubtype readDecimalSubType() throws IOException {
            boolean allowed = inputStream.readBoolean();
            int valuesLength = inputStream.readInt();
            EnumerableDecimal[] values = new EnumerableDecimal[valuesLength];
            for (int i = 0; i < valuesLength; i++) {
                int scale = inputStream.readInt();
                int byteLen = inputStream.readInt();
                byte[] unscaleValueBytes = inputStream.readNBytes(byteLen);
                BigDecimal bigDecimal = new BigDecimal(new BigInteger(unscaleValueBytes), scale);
                values[i] = EnumerableDecimal.from(bigDecimal);
            }
            return (DecimalSubtype) DecimalSubtype.createDecimalSubtype(allowed, values);
        }

        private StringSubtype readStringSubtype() throws IOException {
            CharStringSubtype charStringSubtype = readCharStringSubtype();
            NonCharStringSubtype nonCharStringSubtype = readNonCharStringSubtype();
            return StringSubtype.from(charStringSubtype, nonCharStringSubtype);
        }

        private CharStringSubtype readCharStringSubtype() throws IOException {
            boolean allowed = inputStream.readBoolean();
            int valuesLength = inputStream.readInt();
            EnumerableCharString[] values = new EnumerableCharString[valuesLength];
            for (int i = 0; i < valuesLength; i++) {
                values[i] = EnumerableCharString.from(getStringCPEntryValue(inputStream));
            }
            return CharStringSubtype.from(allowed, values);
        }

        private NonCharStringSubtype readNonCharStringSubtype() throws IOException {
            boolean allowed = inputStream.readBoolean();
            int valuesLength = inputStream.readInt();
            EnumerableString[] values = new EnumerableString[valuesLength];
            for (int i = 0; i < valuesLength; i++) {
                values[i] = EnumerableString.from(getStringCPEntryValue(inputStream));
            }
            return NonCharStringSubtype.from(allowed, values);
        }

        private XmlSubtype readXmlSubtype() throws IOException {
            int primitives = inputStream.readInt();
            Bdd sequence = readBdd();
            return XmlSubtype.from(primitives, sequence);
        }

        // --------------------------------------- End of SemType -----------------------------------------------
    }

    private BType lookupSymbolInMainSpace(SymbolEnv pkgEnv, Name name) {
        return symbolResolver.lookupSymbolInMainSpace(pkgEnv, name).type;
    }

    private byte[] readDocBytes(DataInputStream inputStream) throws IOException {
        int docLength = inputStream.readInt();
        byte[] docBytes = new byte[docLength];
        int noOfBytesRead = inputStream.read(docBytes);
        if (docLength != noOfBytesRead) {
            throw new RuntimeException("failed to read Markdown Documentation");
        }
        return docBytes;
    }

    private PackageID getPackageId(int pkgCPIndex) {
        PackageCPEntry pkgCpEntry = (PackageCPEntry) env.constantPool[pkgCPIndex];
        String orgName = ((StringCPEntry) env.constantPool[pkgCpEntry.orgNameCPIndex]).value;
        String pkgName = ((StringCPEntry) env.constantPool[pkgCpEntry.pkgNameCPIndex]).value;
        String moduleName = ((StringCPEntry) env.constantPool[pkgCpEntry.moduleNameCPIndex]).value;
        String version = ((StringCPEntry) env.constantPool[pkgCpEntry.versionCPIndex]).value;
        return new PackageID(Names.fromString(orgName), Names.fromString(pkgName),
                Names.fromString(moduleName), Names.fromString(version), null);
    }

    private boolean isImmutable(long flags) {
        return Symbols.isFlagOn(flags, Flags.READONLY);
    }

    private BType getEffectiveImmutableType(BType type) {
        return ImmutableTypeCloner.getEffectiveImmutableType(null, types, type,
                type.tsymbol.pkgID, type.tsymbol.owner,
                symTable, null, names);
    }

    private BType getEffectiveImmutableType(BType type, PackageID pkgID, BSymbol owner) {
        return ImmutableTypeCloner.getEffectiveImmutableType(null, types, type, pkgID, owner, symTable,
                null, names);
    }

    private record AtomOffsets(int atomOffset, int listOffset, int functionOffset, int mappingOffset,
                               int distinctOffset) {

        static AtomOffsets from(Env env) {
            PredefinedTypeEnv predefinedTypeEnv = PredefinedTypeEnv.getInstance();
            int recAtomOffset = predefinedTypeEnv.reservedRecAtomCount();
            return new AtomOffsets(env.atomCount(),
                    env.recListAtomCount() - recAtomOffset,
                    env.recFunctionAtomCount(),
                    env.recMappingAtomCount() - recAtomOffset,
                    env.distinctAtomCount());
        }
    }
}

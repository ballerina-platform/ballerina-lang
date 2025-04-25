/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.util;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;

/**
 * Helper class to create a clone of it.
 *
 * @since 1.3.0
 */
public final class ImmutableTypeCloner {

    private ImmutableTypeCloner() {
    }

    public static BType getEffectiveImmutableType(Location pos, Types types,
                                                  BType type, SymbolEnv env,
                                                  SymbolTable symTable, BLangAnonymousModelHelper anonymousModelHelper,
                                                  Names names) {
        return getImmutableIntersectionType(pos, types, type, env, env.enclPkg.packageID, env.scope.owner,
                symTable, anonymousModelHelper, names, new HashSet<>(),
                new HashSet<>()).effectiveType;
    }

    public static BType getEffectiveImmutableType(Location pos, Types types,
                                                  BType type, PackageID pkgId,
                                                  BSymbol owner, SymbolTable symTable,
                                                  BLangAnonymousModelHelper anonymousModelHelper, Names names) {
        return getImmutableIntersectionType(pos, types, type, null, pkgId, owner,
                symTable, anonymousModelHelper, names, new HashSet<>(),
                new HashSet<>()).effectiveType;
    }

    public static BIntersectionType getImmutableIntersectionType(Location pos, Types types,
                                                                 BType type,
                                                                 SymbolEnv env, SymbolTable symTable,
                                                                 BLangAnonymousModelHelper anonymousModelHelper,
                                                                 Names names, Set<Flag> origObjFlagSet) {
        return getImmutableIntersectionType(pos, types, type, env, env.enclPkg.packageID, env.scope.owner,
                                            symTable, anonymousModelHelper, names, origObjFlagSet, new HashSet<>());
    }

    public static BIntersectionType getImmutableIntersectionType(BType type, SymbolTable symbolTable, Names names,
                                                                 Types types, PackageID pkgId) {
        return getImmutableIntersectionType(null, types, type, null, pkgId, null, symbolTable,
                null, names, null, new HashSet<>());
    }

    public static void markFieldsAsImmutable(BLangClassDefinition classDef, SymbolEnv pkgEnv, BObjectType objectType,
                                             Types types, BLangAnonymousModelHelper anonymousModelHelper,
                                             SymbolTable symbolTable, Names names, Location pos) {
        SymbolEnv typeDefEnv = SymbolEnv.createClassEnv(classDef, objectType.tsymbol.scope, pkgEnv);

        Iterator<BField> objectTypeFieldIterator = objectType.fields.values().iterator();

        Map<String, BLangSimpleVariable> classFields = new HashMap<>();

        for (BLangSimpleVariable field : classDef.fields) {
            classFields.put(field.name.value, field);
        }

        for (BLangSimpleVariable field : classDef.referencedFields) {
            classFields.put(field.name.value, field);
        }

        while (objectTypeFieldIterator.hasNext()) {
            BField typeField = objectTypeFieldIterator.next();
            BLangSimpleVariable classField = classFields.get(typeField.name.value);

            BType type = typeField.type;

            if (!types.isInherentlyImmutableType(type)) {
                BType immutableFieldType = typeField.symbol.type = ImmutableTypeCloner.getImmutableIntersectionType(
                        pos, types, type, typeDefEnv, symbolTable, anonymousModelHelper, names, classDef.flagSet);
                classField.setBType(typeField.type = immutableFieldType);
            }

            typeField.symbol.flags |= Flags.FINAL;
            classField.flagSet.add(Flag.FINAL);
        }
    }

    public static BType getImmutableType(Location pos, Types types, BType type, SymbolEnv env,
                                          PackageID pkgId,
                                          BSymbol owner, SymbolTable symTable,
                                          BLangAnonymousModelHelper anonymousModelHelper, Names names,
                                          Set<BType> unresolvedTypes) {
        if (type == null) {
            return symTable.semanticError;
        }

        if (types.isInherentlyImmutableType(type) || Symbols.isFlagOn(type.getFlags(), Flags.READONLY)) {
            return type;
        }

        if (!types.isSelectivelyImmutableType(type, unresolvedTypes, pkgId)) {
            return symTable.semanticError;
        }

        return getImmutableIntersectionType(pos, types, type, env, pkgId,
                                            owner, symTable, anonymousModelHelper, names, new HashSet<>(),
                                            unresolvedTypes);
    }

    private static BIntersectionType getImmutableIntersectionType(Location pos,
                                                                  Types types, BType bType,
                                                                  SymbolEnv env, PackageID pkgId,
                                                                  BSymbol owner, SymbolTable symTable,
                                                                  BLangAnonymousModelHelper anonymousModelHelper,
                                                                  Names names,
                                                                  Set<Flag> origObjFlagSet,
                                                                  Set<BType> unresolvedTypes) {
        BType refType = Types.getReferredType(bType);
        SelectivelyImmutableReferenceType type = (SelectivelyImmutableReferenceType) refType;
        if (refType.tag == TypeTags.INTERSECTION && Symbols.isFlagOn(refType.getFlags(), Flags.READONLY)) {
            return (BIntersectionType) refType;
        }

        Optional<BIntersectionType> immutableType = Types.getImmutableType(symTable, pkgId, type);
        if (immutableType.isPresent()) {
            return immutableType.get();
        }

        return ImmutableTypeCloner.setImmutableType(pos, types, type, bType, env, pkgId, owner, symTable,
                                                    anonymousModelHelper, names, origObjFlagSet, unresolvedTypes);
    }

    private static BIntersectionType setImmutableType(Location pos, Types types,
                                                      SelectivelyImmutableReferenceType selectivelyImmutableRefType,
                                                      BType originalType,
                                                      SymbolEnv env, PackageID pkgId, BSymbol owner,
                                                      SymbolTable symTable,
                                                      BLangAnonymousModelHelper anonymousModelHelper,
                                                      Names names,
                                                      Set<Flag> origObjFlagSet, Set<BType> unresolvedTypes) {
        BType type = (BType) selectivelyImmutableRefType;
        switch (type.tag) {
            case TypeTags.XML_COMMENT:
            case TypeTags.XML_ELEMENT:
            case TypeTags.XML_PI:
                BXMLSubType origXmlSubType = (BXMLSubType) type;
                BXMLSubType immutableXmlSubType = BXMLSubType.newImmutableXMLSubType(origXmlSubType);

                BIntersectionType immutableXmlSubTypeIntersectionType =
                        createImmutableIntersectionType(pkgId, owner, originalType, immutableXmlSubType, symTable);
                Types.addImmutableType(symTable, pkgId, origXmlSubType, immutableXmlSubTypeIntersectionType);
                return immutableXmlSubTypeIntersectionType;
            case TypeTags.XML:
                return defineImmutableXMLType(pos, types, env, pkgId, owner, symTable, anonymousModelHelper, names,
                        unresolvedTypes, (BXMLType) type, originalType);
            case TypeTags.ARRAY:
            case TypeTags.BYTE_ARRAY:
                return defineImmutableArrayType(pos, types, env, pkgId, owner, symTable, anonymousModelHelper, names,
                        unresolvedTypes, (BArrayType) type, originalType);
            case TypeTags.TUPLE:
                return defineImmutableTupleType(pos, types, env, pkgId, owner, symTable, anonymousModelHelper, names,
                        unresolvedTypes, (BTupleType) type, originalType);
            case TypeTags.MAP:
                return defineImmutableMapType(pos, types, env, pkgId, owner, symTable, anonymousModelHelper, names,
                        unresolvedTypes, (BMapType) type, originalType);
            case TypeTags.RECORD:
                BRecordType origRecordType = (BRecordType) type;
                return defineImmutableRecordType(pos, origRecordType, originalType, env, symTable,
                        anonymousModelHelper, names, types, unresolvedTypes);
            case TypeTags.OBJECT:
                BObjectType origObjectType = (BObjectType) type;
                return defineImmutableObjectType(pos, origObjectType, originalType, env, symTable,
                        anonymousModelHelper, names, types, origObjFlagSet, unresolvedTypes);
            case TypeTags.TABLE:
                return defineImmutableTableType(pos, types, env, pkgId, owner, symTable, anonymousModelHelper, names,
                        unresolvedTypes, (BTableType) type, originalType);
            case TypeTags.ANY:
                BAnyType origAnyType = (BAnyType) type;
                BAnyType immutableAnyType = BAnyType.newImmutableBAnyType();
                BIntersectionType immutableAnyIntersectionType = createImmutableIntersectionType(pkgId, owner,
                                                                                                 originalType,
                                                                                                 immutableAnyType,
                                                                                                 symTable);
                Types.addImmutableType(symTable, pkgId, origAnyType, immutableAnyIntersectionType);
                return immutableAnyIntersectionType;
            case TypeTags.ANYDATA:
            case TypeTags.JSON:
                return defineImmutableBuiltInUnionType(pos, types, env, pkgId, owner, symTable, anonymousModelHelper,
                                                       names, unresolvedTypes, (BUnionType) type, originalType);
            case TypeTags.INTERSECTION:
                return (BIntersectionType) type;
            default:
                return defineImmutableUnionType(pos, types, env, pkgId, owner, symTable, anonymousModelHelper, names,
                                                unresolvedTypes, (BUnionType) type, originalType);
        }
    }

    private static BIntersectionType defineImmutableTableType(Location pos, Types types, SymbolEnv env,
                                                              PackageID pkgId, BSymbol owner, SymbolTable symTable,
                                                              BLangAnonymousModelHelper anonymousModelHelper,
                                                              Names names, Set<BType> unresolvedTypes,
                                                              BTableType type,
                                                              BType originalType) {
        BTypeSymbol immutableTableTSymbol = getReadonlyTSymbol(type.tsymbol, env, pkgId, owner);
        Optional<BIntersectionType> immutableType = Types.getImmutableType(symTable, pkgId, type);
        if (immutableType.isPresent()) {
            return immutableType.get();
        } else {
            Types.addImmutableType(symTable, pkgId, type, createImmutableIntersectionType(pkgId, owner,
                    originalType, new BTableType(symTable.typeEnv(), null, immutableTableTSymbol,
                            type.getFlags() | Flags.READONLY), symTable));
        }

        BIntersectionType immutableTableType = Types.getImmutableType(symTable, pkgId, type).orElseThrow();
        BTableType tableEffectiveImmutableType = (BTableType) immutableTableType.effectiveType;
        tableEffectiveImmutableType.constraint = getImmutableType(pos, types, type.constraint, env, pkgId, owner,
                symTable, anonymousModelHelper, names, unresolvedTypes);

        BType origKeyTypeConstraint = type.keyTypeConstraint;
        if (origKeyTypeConstraint != null) {
            tableEffectiveImmutableType.keyTypeConstraint = getImmutableType(pos, types, origKeyTypeConstraint, env,
                    pkgId, owner, symTable,
                    anonymousModelHelper, names,
                    unresolvedTypes);
        }

        tableEffectiveImmutableType.keyPos = type.keyPos;
        tableEffectiveImmutableType.constraintPos = type.constraintPos;
        tableEffectiveImmutableType.isTypeInlineDefined = type.isTypeInlineDefined;
        tableEffectiveImmutableType.fieldNameList = type.fieldNameList;
        tableEffectiveImmutableType.mutableType = type;

        if (immutableTableTSymbol != null) {
            immutableTableTSymbol.type = tableEffectiveImmutableType;
        }

        return immutableTableType;
    }

    private static BIntersectionType defineImmutableXMLType(Location pos, Types types, SymbolEnv env,
                                                            PackageID pkgId, BSymbol owner, SymbolTable symTable,
                                                            BLangAnonymousModelHelper anonymousModelHelper,
                                                            Names names, Set<BType> unresolvedTypes,
                                                            BXMLType type,
                                                            BType originalType) {
        BTypeSymbol immutableXmlTSymbol = getReadonlyTSymbol(type.tsymbol, env, pkgId, owner);
        Optional<BIntersectionType> immutableType = Types.getImmutableType(symTable, pkgId, type);
        if (immutableType.isPresent()) {
            return immutableType.get();
        } else {
            Types.addImmutableType(symTable, pkgId, type, createImmutableIntersectionType(pkgId, owner,
                    originalType, new BXMLType(null, immutableXmlTSymbol, type.getFlags() | Flags.READONLY),
                    symTable));
        }

        BIntersectionType immutableXMLType = Types.getImmutableType(symTable, pkgId, type).orElseThrow();
        BXMLType xmlEffectiveImmutableType = (BXMLType) immutableXMLType.effectiveType;
        xmlEffectiveImmutableType.mutableType = type;
        xmlEffectiveImmutableType.constraint = getImmutableType(pos, types, type.constraint, env, pkgId, owner,
                symTable, anonymousModelHelper, names, unresolvedTypes);
        return immutableXMLType;
    }

    private static BIntersectionType defineImmutableArrayType(Location pos, Types types, SymbolEnv env,
                                                              PackageID pkgId, BSymbol owner, SymbolTable symTable,
                                                              BLangAnonymousModelHelper anonymousModelHelper,
                                                              Names names, Set<BType> unresolvedTypes,
                                                              BArrayType type,
                                                              BType originalType) {
        BTypeSymbol immutableArrayTSymbol = getReadonlyTSymbol(type.tsymbol, env, pkgId, owner);
        Optional<BIntersectionType> immutableType = Types.getImmutableType(symTable, pkgId, type);
        if (immutableType.isPresent()) {
            return immutableType.get();
        } else {
            Types.addImmutableType(symTable, pkgId, type, createImmutableIntersectionType(pkgId, owner,
                    originalType,
                    new BArrayType(symTable.typeEnv(), null, immutableArrayTSymbol, type.getSize(), type.state,
                            type.getFlags() | Flags.READONLY), symTable));
        }

        BIntersectionType immutableArrayType = Types.getImmutableType(symTable, pkgId, type).orElseThrow();
        BArrayType arrayEffectiveImmutableType = (BArrayType) immutableArrayType.effectiveType;
        arrayEffectiveImmutableType.mutableType = type;
        arrayEffectiveImmutableType.eType = getImmutableType(pos, types, type.eType, env, pkgId, owner,
                symTable, anonymousModelHelper, names, unresolvedTypes);
        return immutableArrayType;
    }

    private static BIntersectionType defineImmutableMapType(Location pos, Types types, SymbolEnv env,
                                                            PackageID pkgId, BSymbol owner, SymbolTable symTable,
                                                            BLangAnonymousModelHelper anonymousModelHelper,
                                                            Names names, Set<BType> unresolvedTypes,
                                                            BMapType type,
                                                            BType originalType) {
        BTypeSymbol immutableMapTSymbol = getReadonlyTSymbol(type.tsymbol, env, pkgId, owner);
        Optional<BIntersectionType> immutableType = Types.getImmutableType(symTable, pkgId, type);
        if (immutableType.isPresent()) {
            return immutableType.get();
        } else {
            Types.addImmutableType(symTable, pkgId, type, createImmutableIntersectionType(pkgId, owner,
                    originalType, new BMapType(symTable.typeEnv(), TypeTags.MAP, null, immutableMapTSymbol,
                            type.getFlags() | Flags.READONLY), symTable));
        }

        BIntersectionType immutableMapType = Types.getImmutableType(symTable, pkgId, type).orElseThrow();

        BMapType mapEffectiveImmutableType = (BMapType) immutableMapType.effectiveType;
        mapEffectiveImmutableType.mutableType = type;
        mapEffectiveImmutableType.constraint = getImmutableType(pos, types, type.constraint, env, pkgId, owner,
                                                                 symTable, anonymousModelHelper, names,
                                                                 unresolvedTypes);
        return immutableMapType;
    }

    private static BIntersectionType defineImmutableTupleType(Location pos, Types types, SymbolEnv env,
                                                              PackageID pkgId, BSymbol owner, SymbolTable symTable,
                                                              BLangAnonymousModelHelper anonymousModelHelper,
                                                              Names names, Set<BType> unresolvedTypes,
                                                              BTupleType type,
                                                              BType originalType) {
        BTypeSymbol origTupleTypeSymbol = type.tsymbol;
        List<BTupleMember> origTupleMembers = type.getMembers();

        Optional<BIntersectionType> immutableType = Types.getImmutableType(symTable, pkgId, type);
        if (immutableType.isPresent()) {
            return immutableType.get();
        } else {
            Types.addImmutableType(symTable, pkgId, type, createImmutableIntersectionType(pkgId, owner,
                    originalType, new BTupleType(symTable.typeEnv(), origTupleTypeSymbol), symTable));
        }

        List<BTupleMember> immutableMemTypes = new ArrayList<>(origTupleMembers.size());
        BTupleType tupleEffectiveImmutableType =
                (BTupleType) Types.getImmutableType(symTable, pkgId, type).get().effectiveType;
        tupleEffectiveImmutableType.mutableType = type;
        tupleEffectiveImmutableType.isCyclic = type.isCyclic;
        tupleEffectiveImmutableType.setMembers(immutableMemTypes);

        String originalTypeName = origTupleTypeSymbol == null ? "" : origTupleTypeSymbol.name.getValue();
        Name origTupleTypeSymbolName = Names.EMPTY;
        if (!originalTypeName.isEmpty()) {
            origTupleTypeSymbolName = origTupleTypeSymbol.name.value.isEmpty() ? Names.EMPTY :
                    Types.getImmutableTypeName(getSymbolFQN(origTupleTypeSymbol));
            tupleEffectiveImmutableType.name = origTupleTypeSymbolName;
        }

        for (BTupleMember origTupleMemType : origTupleMembers) {
            if (types.isInherentlyImmutableType(origTupleMemType.type)) {
                tupleEffectiveImmutableType.addMembers(origTupleMemType);
                continue;
            }
            if (!types.isSelectivelyImmutableType(origTupleMemType.type, unresolvedTypes, pkgId)) {
                continue;
            }
            BType newType = getImmutableType(pos, types, origTupleMemType.type, env,
                    pkgId, owner, symTable, anonymousModelHelper, names, unresolvedTypes);
            BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(newType);
            BTupleMember member = new BTupleMember(newType, varSymbol);
            tupleEffectiveImmutableType.addMembers(member);
        }

        if (type.restType != null) {
            tupleEffectiveImmutableType.addRestType(getImmutableType(pos, types, type.restType, env, pkgId,
                    owner, symTable, anonymousModelHelper, names, unresolvedTypes));
        }

        BIntersectionType immutableTupleIntersectionType = Types.getImmutableType(symTable, pkgId, type).get();
        BType effectiveTypeFromType = immutableTupleIntersectionType.effectiveType;

        if (origTupleTypeSymbol != null) {
            BTypeSymbol immutableTupleTSymbol =
                    getReadonlyTSymbol(origTupleTypeSymbol, env, pkgId, owner, origTupleTypeSymbolName);
            effectiveTypeFromType.tsymbol = immutableTupleTSymbol;
            effectiveTypeFromType.addFlags(type.getFlags() | Flags.READONLY);

            immutableTupleTSymbol.type = effectiveTypeFromType;
        } else {
            effectiveTypeFromType.addFlags(type.getFlags() | Flags.READONLY);
        }

        BType effectiveType = immutableTupleIntersectionType.effectiveType;
        BTypeSymbol tsymbol = immutableTupleIntersectionType.effectiveType.tsymbol;
        if (Types.getImpliedType(effectiveType).tag != TypeTags.TUPLE || tsymbol == null || tsymbol.name == null ||
                tsymbol.name.value.isEmpty()) {
            return immutableTupleIntersectionType;
        }

        BLangTupleTypeNode tupleTypeNode = (BLangTupleTypeNode) TreeBuilder.createTupleTypeNode();
        tupleTypeNode.setBType(effectiveType);
        BLangTypeDefinition typeDefinition = TypeDefBuilderHelper.addTypeDefinition(effectiveType,
                effectiveType.tsymbol, tupleTypeNode, env);
        typeDefinition.pos = pos;
        effectiveType.addFlags(Flags.EFFECTIVE_TYPE_DEF);
        return immutableTupleIntersectionType;
    }

    public static void defineUndefinedImmutableFields(BLangTypeDefinition immutableTypeDefinition,
                                                      Types types, SymbolEnv pkgEnv, SymbolTable symTable,
                                                      BLangAnonymousModelHelper anonymousModelHelper,
                                                      Names names) {
        Location pos = immutableTypeDefinition.pos;
        SymbolEnv env = SymbolEnv.createTypeEnv(immutableTypeDefinition.typeNode, immutableTypeDefinition.symbol.scope,
                                                pkgEnv);
        PackageID pkgID = env.enclPkg.symbol.pkgID;

        BType immutableType = Types.getImpliedType(immutableTypeDefinition.getBType());
        if (immutableType.tag == TypeTags.RECORD) {
            defineUndefinedImmutableRecordFields((BRecordType) immutableType, pos, pkgID, immutableTypeDefinition,
                                                 types, env, symTable, anonymousModelHelper, names);
            return;
        }
        defineUndefinedImmutableObjectFields((BObjectType) immutableType, pos, pkgID, immutableTypeDefinition,
                                             types, env, symTable, anonymousModelHelper, names);
    }

    private static void defineUndefinedImmutableRecordFields(BRecordType immutableRecordType,
                                                             Location loc,
                                                             PackageID pkgID,
                                                             BLangTypeDefinition immutableTypeDefinition,
                                                             Types types, SymbolEnv env, SymbolTable symTable,
                                                             BLangAnonymousModelHelper anonymousModelHelper,
                                                             Names names) {
        BRecordType origRecordType = immutableRecordType.mutableType;
        if (origRecordType != null && (origRecordType.fields.size() != immutableRecordType.fields.size())) {

            populateImmutableStructureFields(types, symTable, anonymousModelHelper, names,
                                             (BLangRecordTypeNode) immutableTypeDefinition.typeNode,
                                             immutableRecordType, origRecordType, loc, env, pkgID, new HashSet<>());
        }

        BType currentRestFieldType = immutableRecordType.restFieldType;
        if (currentRestFieldType != null && currentRestFieldType != symTable.noType) {
            return;
        }

        setRestType(types, symTable, anonymousModelHelper, names, immutableRecordType, origRecordType, loc, env,
                    new HashSet<>());
    }

    private static void defineUndefinedImmutableObjectFields(BObjectType immutableObjectType,
                                                             Location location,
                                                             PackageID pkgID,
                                                             BLangTypeDefinition immutableTypeDefinition,
                                                             Types types, SymbolEnv env, SymbolTable symTable,
                                                             BLangAnonymousModelHelper anonymousModelHelper,
                                                             Names names) {
        BObjectType origObjectType = immutableObjectType.mutableType;
        if (origObjectType.fields.size() != immutableObjectType.fields.size()) {

            TypeDefBuilderHelper.populateStructureFieldsAndTypeInclusions(types, symTable, anonymousModelHelper, names,
                    (BLangObjectTypeNode) immutableTypeDefinition.typeNode, immutableObjectType, origObjectType,
                    location, env, pkgID, new HashSet<>(), Flags.FINAL, true);
        }
    }

    private static void populateImmutableStructureFields(Types types, SymbolTable symTable,
                                                         BLangAnonymousModelHelper anonymousModelHelper, Names names,
                                                         BLangStructureTypeNode immutableStructureTypeNode,
                                                         BStructureType immutableStructureType,
                                                         BStructureType origStructureType, Location pos,
                                                         SymbolEnv env, PackageID pkgID, Set<BType> unresolvedTypes) {
        TypeDefBuilderHelper.populateStructureFieldsAndTypeInclusions(types, symTable, anonymousModelHelper, names,
                immutableStructureTypeNode, immutableStructureType, origStructureType, pos, env, pkgID, unresolvedTypes,
                Flags.READONLY, true);
    }

    private static void setRestType(Types types, SymbolTable symTable, BLangAnonymousModelHelper anonymousModelHelper,
                                    Names names, BRecordType immutableRecordType, BRecordType origRecordType,
                                    Location pos, SymbolEnv env, Set<BType> unresolvedTypes) {
        immutableRecordType.sealed = origRecordType.sealed;

        BType origRestFieldType = origRecordType.restFieldType;

        if (origRestFieldType == null || origRestFieldType == symTable.noType) {
            immutableRecordType.restFieldType = origRestFieldType;
            return;
        }
        BType restFieldImmutableType = getImmutableType(pos, types, origRestFieldType, env, env.enclPkg.packageID,
                                                        env.scope.owner, symTable, anonymousModelHelper, names,
                                                        unresolvedTypes);
        immutableRecordType.restFieldType = restFieldImmutableType == symTable.semanticError ?
                symTable.neverType : restFieldImmutableType;
    }

    private static BIntersectionType defineImmutableRecordType(Location pos, BRecordType origRecordType,
                                                               BType originalType,
                                                               SymbolEnv env, SymbolTable symTable,
                                                               BLangAnonymousModelHelper anonymousModelHelper,
                                                               Names names, Types types, Set<BType> unresolvedTypes) {
        PackageID pkgID = env.enclPkg.symbol.pkgID;
        BTypeSymbol recordTypeSymbol = origRecordType.tsymbol;
        BRecordTypeSymbol recordSymbol =
                Symbols.createRecordSymbol(recordTypeSymbol.flags | Flags.READONLY,
                        Types.getImmutableTypeName(getSymbolFQN(recordTypeSymbol)),
                        pkgID, null, env.scope.owner, pos, VIRTUAL);

        BInvokableType bInvokableType =
                new BInvokableType(symTable.typeEnv(), List.of(), symTable.nilType, null);
        BInvokableSymbol initFuncSymbol = Symbols.createFunctionSymbol(
                Flags.PUBLIC, Names.EMPTY, Names.EMPTY, env.enclPkg.symbol.pkgID, bInvokableType, env.scope.owner,
                false, symTable.builtinPos, VIRTUAL);
        initFuncSymbol.retType = symTable.nilType;

        recordSymbol.scope = new Scope(recordSymbol);

        BRecordType immutableRecordType = new BRecordType(symTable.typeEnv(), recordSymbol,
                origRecordType.getFlags() | Flags.READONLY);

        BIntersectionType immutableRecordIntersectionType = createImmutableIntersectionType(env, originalType,
                                                                                            immutableRecordType,
                                                                                            symTable);

        Types.addImmutableType(symTable, pkgID, origRecordType, immutableRecordIntersectionType);
        immutableRecordType.mutableType = origRecordType;

        recordSymbol.type = immutableRecordType;
        immutableRecordType.tsymbol = recordSymbol;

        BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(new ArrayList<>(),
                                                                                       immutableRecordType, pos);

        populateImmutableStructureFields(types, symTable, anonymousModelHelper, names, recordTypeNode,
                                         immutableRecordType, origRecordType, pos, env, pkgID, unresolvedTypes);

        setRestType(types, symTable, anonymousModelHelper, names, immutableRecordType, origRecordType, pos, env,
                    unresolvedTypes);

        TypeDefBuilderHelper.addTypeDefinition(immutableRecordType, recordSymbol, recordTypeNode, env);
        return immutableRecordIntersectionType;
    }

    private static BIntersectionType defineImmutableObjectType(Location pos,
                                                               BObjectType origObjectType, BType originalType,
                                                               SymbolEnv env, SymbolTable symTable,
                                                               BLangAnonymousModelHelper anonymousModelHelper,
                                                               Names names, Types types,
                                                               Set<Flag> flagSet, Set<BType> unresolvedTypes) {
        PackageID pkgID = env.enclPkg.symbol.pkgID;
        BObjectTypeSymbol origObjectTSymbol = (BObjectTypeSymbol) origObjectType.tsymbol;

        long flags = origObjectTSymbol.flags | Flags.READONLY;
        flags &= ~Flags.CLASS;

        BObjectTypeSymbol objectSymbol = Symbols.createObjectSymbol(flags,
                                                                    Types.getImmutableTypeName(
                                                                            getSymbolFQN(origObjectTSymbol)),
                                                                    pkgID, null, env.scope.owner, pos, VIRTUAL);

        objectSymbol.scope = new Scope(objectSymbol);

        defineObjectFunctions(objectSymbol, origObjectTSymbol, names, symTable);

        BObjectType immutableObjectType =
                new BObjectType(symTable.typeEnv(), objectSymbol, origObjectType.getFlags() | Flags.READONLY);

        immutableObjectType.typeIdSet = origObjectType.typeIdSet;
        BIntersectionType immutableObjectIntersectionType = createImmutableIntersectionType(env, originalType,
                                                                                            immutableObjectType,
                                                                                            symTable);

        Types.addImmutableType(symTable, pkgID, origObjectType, immutableObjectIntersectionType);
        immutableObjectType.mutableType = origObjectType;

        objectSymbol.type = immutableObjectType;
        immutableObjectType.tsymbol = objectSymbol;

        BLangObjectTypeNode objectTypeNode = TypeDefBuilderHelper.createObjectTypeNode(new ArrayList<>(),
                                                                                       immutableObjectType, pos);
        objectTypeNode.flagSet.addAll(flagSet);

        TypeDefBuilderHelper.populateStructureFieldsAndTypeInclusions(types, symTable, anonymousModelHelper, names,
                                                                      objectTypeNode, immutableObjectType,
                                                                      origObjectType, pos, env, pkgID, unresolvedTypes,
                                                                      Flags.FINAL, true);

        BLangTypeDefinition typeDefinition = TypeDefBuilderHelper.addTypeDefinition(immutableObjectType, objectSymbol,
                                                                                    objectTypeNode, env);
        typeDefinition.pos = pos;
        return immutableObjectIntersectionType;
    }

    public static void defineObjectFunctions(BObjectTypeSymbol immutableObjectSymbol,
                                             BObjectTypeSymbol originalObjectSymbol, Names names,
                                             SymbolTable symTable) {
        List<BAttachedFunction> originalObjectAttachedFuncs = originalObjectSymbol.attachedFuncs;
        List<BAttachedFunction> immutableObjectAttachedFuncs = immutableObjectSymbol.attachedFuncs;

        if (originalObjectAttachedFuncs.isEmpty() ||
                immutableObjectAttachedFuncs.size() == originalObjectAttachedFuncs.size()) {
            return;
        }

        List<BAttachedFunction> immutableFuncs = new ArrayList<>();
        for (BAttachedFunction origFunc : originalObjectAttachedFuncs) {
            Name funcName = Names.fromString(Symbols.getAttachedFuncSymbolName(immutableObjectSymbol.name.value,
                                                                               origFunc.funcName.value));
            BInvokableSymbol immutableFuncSymbol =
                    ASTBuilderUtil.duplicateFunctionDeclarationSymbol(symTable.typeEnv(), origFunc.symbol,
                                                                      immutableObjectSymbol,
                                                                      funcName, immutableObjectSymbol.pkgID,
                                                                      symTable.builtinPos, VIRTUAL);
            immutableFuncs.add(new BAttachedFunction(origFunc.funcName, immutableFuncSymbol,
                                                     (BInvokableType) immutableFuncSymbol.type, symTable.builtinPos));
            immutableObjectSymbol.scope.define(funcName, immutableFuncSymbol);
        }
        immutableObjectSymbol.attachedFuncs = immutableFuncs;
    }

    private static BIntersectionType defineImmutableUnionType(Location pos, Types types, SymbolEnv env,
                                                              PackageID pkgId, BSymbol owner, SymbolTable symTable,
                                                              BLangAnonymousModelHelper anonymousModelHelper,
                                                              Names names, Set<BType> unresolvedTypes,
                                                              BUnionType type,
                                                              BType originalType) {
        BTypeSymbol origUnionTypeSymbol = type.tsymbol;

        LinkedHashSet<BType> originalMemberList = type.getMemberTypes();
        Optional<BIntersectionType> immutableTypeOptional = Types.getImmutableType(symTable, pkgId, type);
        if (immutableTypeOptional.isPresent()) {
            return immutableTypeOptional.get();
        } else {
            BUnionType immutableUnionType = BUnionType.create(symTable.typeEnv(), origUnionTypeSymbol);
            Types.addImmutableType(symTable, pkgId, type, createImmutableIntersectionType(pkgId, owner,
                                      originalType, immutableUnionType, symTable));
        }

        BIntersectionType immutableType = handleImmutableUnionType(pos, types, env, pkgId, owner, symTable,
                                                                   anonymousModelHelper, names,
                                                                   unresolvedTypes, type, origUnionTypeSymbol,
                                                                   originalMemberList);
        BType effectiveType = immutableType.effectiveType;
        BTypeSymbol tsymbol = effectiveType.tsymbol;
        if (effectiveType.tag != TypeTags.UNION || tsymbol == null || tsymbol.name == null ||
                tsymbol.name.value.isEmpty()) {
            return immutableType;
        }

        BLangUnionTypeNode unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
        unionTypeNode.setBType(effectiveType);
        BLangTypeDefinition typeDefinition = TypeDefBuilderHelper.addTypeDefinition(effectiveType,
                                                                                    effectiveType.tsymbol,
                                                                                    unionTypeNode, env);
        typeDefinition.pos = pos;
        return immutableType;
    }

    private static BIntersectionType defineImmutableBuiltInUnionType(Location pos, Types types, SymbolEnv env,
                                                                     PackageID pkgId, BSymbol owner,
                                                                     SymbolTable symTable,
                                                                     BLangAnonymousModelHelper anonymousModelHelper,
                                                                     Names names, Set<BType> unresolvedTypes,
                                                                     BUnionType type, BType originalType) {
        BTypeSymbol origBuiltInUnionTypeSymbol = type.tsymbol;

        Optional<BIntersectionType> immutableTypeOptional = Types.getImmutableType(symTable, pkgId, type);
        if (immutableTypeOptional.isPresent()) {
            return immutableTypeOptional.get();
        }

        BUnionType effectiveType;
        if (type.tag == TypeTags.JSON) {
            effectiveType = defineImmutableJsonType(env, pkgId, owner, names, (BJSONType) type);
        } else {
            effectiveType = defineImmutableAnydataType(env, pkgId, owner, names, (BAnydataType) type);
        }

        BIntersectionType immutableBuiltInUnionIntersectionType =
                createImmutableIntersectionType(pkgId, owner, originalType, effectiveType, symTable);
        Types.addImmutableType(symTable, pkgId, type, immutableBuiltInUnionIntersectionType);

        return handleImmutableUnionType(pos, types, env, pkgId, owner, symTable, anonymousModelHelper, names,
                                        unresolvedTypes, type, origBuiltInUnionTypeSymbol, type.getMemberTypes());
    }

    private static BAnydataType defineImmutableAnydataType(SymbolEnv env, PackageID pkgId, BSymbol owner, Names names,
                                                           BAnydataType type) {
        BTypeSymbol immutableAnydataTSymbol = getReadonlyTSymbol(type.tsymbol, env, pkgId, owner);

        if (immutableAnydataTSymbol != null) {
            BAnydataType immutableAnydataType = BAnydataType.newImmutableBAnydataType(type, immutableAnydataTSymbol,
                                     immutableAnydataTSymbol.name,
                                     type.isNullable());
            immutableAnydataTSymbol.type = immutableAnydataType;
            return immutableAnydataType;
        }
        return BAnydataType.newImmutableBAnydataType(type, null,
                                 Types.getImmutableTypeName(TypeKind.ANYDATA.typeName()), type.isNullable());
    }

    private static BJSONType defineImmutableJsonType(SymbolEnv env, PackageID pkgId, BSymbol owner, Names names,
                                                     BJSONType type) {
        BTypeSymbol immutableJsonTSymbol = getReadonlyTSymbol(type.tsymbol, env, pkgId, owner);
        BJSONType immutableJsonType = BJSONType.newImmutableBJSONType(type, immutableJsonTSymbol, type.isNullable());
        if (immutableJsonTSymbol != null) {
            immutableJsonTSymbol.type = immutableJsonType;
        }
        return immutableJsonType;
    }

    private static BIntersectionType handleImmutableUnionType(Location pos, Types types, SymbolEnv env, PackageID pkgId,
                                                              BSymbol owner, SymbolTable symTable,
                                                              BLangAnonymousModelHelper anonymousModelHelper,
                                                              Names names, Set<BType> unresolvedTypes, BUnionType type,
                                                              BTypeSymbol origUnionTypeSymbol,
                                                              LinkedHashSet<BType> originalMemberList) {
        BIntersectionType immutableType = Types.getImmutableType(symTable, pkgId, type).get();

        LinkedHashSet<BType> readOnlyMemTypes = new LinkedHashSet<>(originalMemberList.size());
        BUnionType unionEffectiveImmutableType = (BUnionType) immutableType.effectiveType;
        unionEffectiveImmutableType.isCyclic = type.isCyclic;
        unionEffectiveImmutableType.setMemberTypes(readOnlyMemTypes);

        String originalTypeName = origUnionTypeSymbol == null ? "" : origUnionTypeSymbol.name.getValue();
        if (!originalTypeName.isEmpty()) {
            unionEffectiveImmutableType.name = Types.getImmutableTypeName(getSymbolFQN(origUnionTypeSymbol));
        }

        for (BType memberType : originalMemberList) {
            if (types.isInherentlyImmutableType(memberType)) {
                unionEffectiveImmutableType.add(memberType);
                continue;
            }

            if (!types.isSelectivelyImmutableType(memberType, unresolvedTypes, pkgId)) {
                continue;
            }

            BType immutableMemberType = getImmutableType(pos, types, memberType, env, pkgId, owner, symTable,
                                                         anonymousModelHelper, names, unresolvedTypes);

            unionEffectiveImmutableType.add(immutableMemberType);
        }

        if (readOnlyMemTypes.size() == 1) {
            immutableType.effectiveType = readOnlyMemTypes.iterator().next();
        } else if (origUnionTypeSymbol != null) {
            BTypeSymbol immutableUnionTSymbol =
                    getReadonlyTSymbol(origUnionTypeSymbol, env, pkgId, owner,
                                       origUnionTypeSymbol.name.value.isEmpty() ? Names.EMPTY :
                                               Types.getImmutableTypeName(getSymbolFQN(origUnionTypeSymbol)));
            immutableType.effectiveType.tsymbol = immutableUnionTSymbol;
            immutableType.effectiveType.addFlags(type.getFlags() | Flags.READONLY);

            immutableUnionTSymbol.type = immutableType.effectiveType;
        } else {
            immutableType.effectiveType.addFlags(type.getFlags() | Flags.READONLY);
        }

        return immutableType;
    }

    private static BTypeSymbol getReadonlyTSymbol(BTypeSymbol originalTSymbol, SymbolEnv env,
                                                  PackageID pkgId, BSymbol owner) {
        if (originalTSymbol == null) {
            return null;
        }

        return getReadonlyTSymbol(originalTSymbol, env, pkgId, owner, getImmutableTypeName(originalTSymbol));
    }

    private static BTypeSymbol getReadonlyTSymbol(BTypeSymbol originalTSymbol, SymbolEnv env, PackageID pkgId,
                                                  BSymbol owner, Name immutableTypeName) {
        if (originalTSymbol == null) {
            return null;
        }

        if (env == null) {
            return Symbols.createTypeSymbol(originalTSymbol.tag, originalTSymbol.flags | Flags.READONLY,
                                            immutableTypeName, pkgId, null, owner, originalTSymbol.pos, SOURCE);
        }

        return Symbols.createTypeSymbol(originalTSymbol.tag, originalTSymbol.flags | Flags.READONLY,
                                        immutableTypeName, env.enclPkg.symbol.pkgID, null, env.scope.owner,
                                        originalTSymbol.pos, SOURCE);
    }

    private static String getSymbolFQN(BTypeSymbol originalTSymbol) {
        PackageID pkgID = originalTSymbol.pkgID;
        if (pkgID == PackageID.DEFAULT ||
                pkgID.equals(PackageID.ANNOTATIONS) ||
                pkgID.name == Names.DEFAULT_PACKAGE) {
            return originalTSymbol.name.value;
        }
        return pkgID.orgName + Names.ORG_NAME_SEPARATOR.value + pkgID.name + Names.VERSION_SEPARATOR +
                getMajorVersion(pkgID.version.value) + ":" + originalTSymbol.name;
    }

    private static Name getImmutableTypeName(BTypeSymbol originalTSymbol) {
        return Types.getImmutableTypeName(originalTSymbol.name.getValue());
    }

    private static BIntersectionType createImmutableIntersectionType(SymbolEnv env, BType nonReadOnlyType,
                                                                     BType effectiveType, SymbolTable symTable) {
        return createImmutableIntersectionType(env.enclPkg.symbol.pkgID, env.scope.owner, nonReadOnlyType,
                                               effectiveType, symTable);
    }

    private static BIntersectionType createImmutableIntersectionType(PackageID pkgId, BSymbol owner,
                                                                     BType nonReadOnlyType, BType effectiveType,
                                                                     SymbolTable symTable) {
        BTypeSymbol intersectionTypeSymbol = Symbols.createTypeSymbol(SymTag.INTERSECTION_TYPE,
                                                                      Flags.asMask(EnumSet.of(Flag.PUBLIC,
                                                                                              Flag.READONLY)),
                                                                      Names.EMPTY, pkgId, null, owner,
                                                                      symTable.builtinPos, VIRTUAL);

        LinkedHashSet<BType> constituentTypes = new LinkedHashSet<>() {{
            add(nonReadOnlyType);
            add(symTable.readonlyType);
        }};

        BIntersectionType intersectionType = new BIntersectionType(intersectionTypeSymbol, constituentTypes,
                effectiveType, Flags.READONLY | effectiveType.getFlags());
        intersectionTypeSymbol.type = intersectionType;
        return intersectionType;
    }
}

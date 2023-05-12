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
import org.ballerinalang.model.types.IntersectableReferenceType;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnnotationType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BHandleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNeverType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
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
import java.util.LinkedHashMap;
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
public class ImmutableTypeCloner {

    private static final String AND_READONLY_SUFFIX = " & readonly";

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

        if (types.isInherentlyImmutableType(type) || Symbols.isFlagOn(type.flags, Flags.READONLY)) {
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
        if (refType.tag == TypeTags.INTERSECTION && Symbols.isFlagOn(refType.flags, Flags.READONLY)) {
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

                // TODO: 4/28/20 Check tsymbol
                BXMLSubType immutableXmlSubType =
                        new BXMLSubType(origXmlSubType.tag,
                                        names.fromString(origXmlSubType.name.getValue().concat(AND_READONLY_SUFFIX)),
                                        origXmlSubType.flags | Flags.READONLY);

                BIntersectionType immutableXmlSubTypeIntersectionType =
                        createImmutableIntersectionType(pkgId, owner, originalType, immutableXmlSubType, symTable);
                Types.addImmutableType(symTable, pkgId, origXmlSubType, immutableXmlSubTypeIntersectionType);
                return immutableXmlSubTypeIntersectionType;
            case TypeTags.XML:
                BXMLType origXmlType = (BXMLType) type;

                BTypeSymbol immutableXmlTSymbol = getReadonlyTSymbol(names, origXmlType.tsymbol, env, pkgId, owner);
                BXMLType immutableXmlType = new BXMLType(getImmutableType(pos, types, origXmlType.constraint, env,
                                                                          pkgId, owner, symTable, anonymousModelHelper,
                                                                          names, unresolvedTypes),
                                                immutableXmlTSymbol, origXmlType.flags | Flags.READONLY);
                if (immutableXmlTSymbol != null) {
                    immutableXmlTSymbol.type = immutableXmlType;
                }

                BIntersectionType immutableXmlIntersectionType = createImmutableIntersectionType(pkgId, owner,
                                                                                                 originalType,
                                                                                                 immutableXmlType,
                                                                                                 symTable);
                Types.addImmutableType(symTable, pkgId, origXmlType, immutableXmlIntersectionType);
                return immutableXmlIntersectionType;
            case TypeTags.ARRAY:
                BArrayType origArrayType = (BArrayType) type;

                BTypeSymbol immutableArrayTSymbol = getReadonlyTSymbol(names, origArrayType.tsymbol, env, pkgId, owner);
                BArrayType immutableArrayType = new BArrayType(getImmutableType(pos, types,
                                                                                origArrayType.getElementType(), env,
                                                                                pkgId, owner, symTable,
                                                                                anonymousModelHelper,
                                                                                names, unresolvedTypes),
                                                    immutableArrayTSymbol, origArrayType.size, origArrayType.state,
                                                    origArrayType.flags | Flags.READONLY);

                immutableArrayType.mutableType = origArrayType;
                if (immutableArrayTSymbol != null) {
                    immutableArrayTSymbol.type = immutableArrayType;
                }

                BIntersectionType immutableArrayIntersectionType = createImmutableIntersectionType(pkgId, owner,
                                                                                                   originalType,
                                                                                                   immutableArrayType,
                                                                                                   symTable);
                Types.addImmutableType(symTable, pkgId, origArrayType, immutableArrayIntersectionType);
                return immutableArrayIntersectionType;
            case TypeTags.TUPLE:
                BTupleType origTupleType = (BTupleType) type;
                return defineImmutableTupleType(pos, types, env, pkgId, owner, symTable, anonymousModelHelper, names,
                        unresolvedTypes, origTupleType);
            case TypeTags.MAP:
                BMapType origMapType = (BMapType) type;

                BTypeSymbol immutableMapTSymbol = getReadonlyTSymbol(names, origMapType.tsymbol, env, pkgId, owner);
                BMapType immutableMapType = new BMapType(origMapType.tag,
                                                         getImmutableType(pos, types, origMapType.constraint, env,
                                                                          pkgId, owner, symTable, anonymousModelHelper,
                                                                          names, unresolvedTypes),
                                                immutableMapTSymbol, origMapType.flags | Flags.READONLY);
                immutableMapType.mutableType = origMapType;
                if (immutableMapTSymbol != null) {
                    immutableMapTSymbol.type = immutableMapType;
                }

                BIntersectionType immutableMapIntersectionType = createImmutableIntersectionType(pkgId, owner,
                        originalType, immutableMapType, symTable);
                Types.addImmutableType(symTable, pkgId, origMapType, immutableMapIntersectionType);
                return immutableMapIntersectionType;
            case TypeTags.RECORD:
                BRecordType origRecordType = (BRecordType) type;

                return defineImmutableRecordType(pos, origRecordType, originalType, env, symTable,
                        anonymousModelHelper, names, types, unresolvedTypes);
            case TypeTags.OBJECT:
                BObjectType origObjectType = (BObjectType) type;

                return defineImmutableObjectType(pos, origObjectType, originalType, env, symTable,
                        anonymousModelHelper, names, types, origObjFlagSet, unresolvedTypes);
            case TypeTags.TABLE:
                BTableType origTableType = (BTableType) type;

                BTypeSymbol immutableTableTSymbol = getReadonlyTSymbol(names, origTableType.tsymbol, env, pkgId, owner);
                BTableType immutableTableType = new BTableType(origTableType.tag,
                                                               getImmutableType(pos, types, origTableType.constraint,
                                                                                env, pkgId, owner, symTable,
                                                                                anonymousModelHelper, names,
                                                                                unresolvedTypes),
                                                immutableTableTSymbol, origTableType.flags | Flags.READONLY);

                BType origKeyTypeConstraint = origTableType.keyTypeConstraint;
                if (origKeyTypeConstraint != null) {
                    immutableTableType.keyTypeConstraint = getImmutableType(pos, types, origKeyTypeConstraint, env,
                                                                            pkgId, owner, symTable,
                                                                            anonymousModelHelper, names,
                                                                            unresolvedTypes);
                }

                immutableTableType.keyPos = origTableType.keyPos;
                immutableTableType.constraintPos = origTableType.constraintPos;
                immutableTableType.isTypeInlineDefined = origTableType.isTypeInlineDefined;
                immutableTableType.fieldNameList = origTableType.fieldNameList;
                immutableTableType.mutableType = origTableType;

                if (immutableTableTSymbol != null) {
                    immutableTableTSymbol.type = immutableTableType;
                }

                BIntersectionType immutableTableIntersectionType = createImmutableIntersectionType(pkgId, owner,
                                                                                                   originalType,
                                                                                                   immutableTableType,
                                                                                                   symTable);
                Types.addImmutableType(symTable, pkgId, origTableType, immutableTableIntersectionType);
                return immutableTableIntersectionType;
            case TypeTags.ANY:
                BAnyType origAnyType = (BAnyType) type;

                BTypeSymbol immutableAnyTSymbol = getReadonlyTSymbol(names, origAnyType.tsymbol, env, pkgId, owner);

                BAnyType immutableAnyType;
                if (immutableAnyTSymbol != null) {
                    immutableAnyType = new BAnyType(origAnyType.tag, immutableAnyTSymbol, immutableAnyTSymbol.name,
                                                    origAnyType.flags | Flags.READONLY, origAnyType.isNullable());
                    immutableAnyTSymbol.type = immutableAnyType;
                } else {
                    immutableAnyType = new BAnyType(origAnyType.tag, immutableAnyTSymbol,
                                                    getImmutableTypeName(names, TypeKind.ANY.typeName()),
                                                    origAnyType.flags | Flags.READONLY, origAnyType.isNullable());
                }

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
                                                unresolvedTypes, (BUnionType) type);
        }
    }

    private static BIntersectionType defineImmutableTupleType(Location pos, Types types, SymbolEnv env,
                                                              PackageID pkgId, BSymbol owner, SymbolTable symTable,
                                                              BLangAnonymousModelHelper anonymousModelHelper,
                                                              Names names, Set<BType> unresolvedTypes,
                                                              BTupleType type) {
        BTypeSymbol origTupleTypeSymbol = type.tsymbol;
        List<BTupleMember> origTupleMembers = type.getMembers();

        Optional<BIntersectionType> immutableType = Types.getImmutableType(symTable, pkgId, type);
        if (immutableType.isPresent()) {
            return immutableType.get();
        } else {
            Types.addImmutableType(symTable, pkgId, type, createImmutableIntersectionType(pkgId, owner,
                    type, new BTupleType(origTupleTypeSymbol), symTable));
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
                    getImmutableTypeName(names, getSymbolFQN(origTupleTypeSymbol));
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
            effectiveTypeFromType.flags |= (type.flags | Flags.READONLY);

            if (immutableTupleTSymbol != null) {
                immutableTupleTSymbol.type = effectiveTypeFromType;
            }
        } else {
            effectiveTypeFromType.flags |= (type.flags | Flags.READONLY);
        }

        BType effectiveType = immutableTupleIntersectionType.effectiveType;
        BTypeSymbol tsymbol = immutableTupleIntersectionType.effectiveType.tsymbol;
        if (effectiveType.tag != TypeTags.TUPLE || tsymbol == null || tsymbol.name == null ||
                tsymbol.name.value.isEmpty()) {
            return immutableTupleIntersectionType;
        }

        BLangTupleTypeNode tupleTypeNode = (BLangTupleTypeNode) TreeBuilder.createTupleTypeNode();
        tupleTypeNode.setBType(effectiveType);
        BLangTypeDefinition typeDefinition = TypeDefBuilderHelper.addTypeDefinition(effectiveType,
                effectiveType.tsymbol, tupleTypeNode, env);
        typeDefinition.pos = pos;
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

        BType immutableType = immutableTypeDefinition.getBType();
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

            TypeDefBuilderHelper.populateStructureFields(types, symTable, anonymousModelHelper, names,
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
        TypeDefBuilderHelper.populateStructureFields(types, symTable, anonymousModelHelper, names,
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
                        getImmutableTypeName(names,  getSymbolFQN(recordTypeSymbol)),
                        pkgID, null, env.scope.owner, pos, recordTypeSymbol.origin);

        BInvokableType bInvokableType = new BInvokableType(new ArrayList<>(), symTable.nilType, null);
        BInvokableSymbol initFuncSymbol = Symbols.createFunctionSymbol(
                Flags.PUBLIC, Names.EMPTY, Names.EMPTY, env.enclPkg.symbol.pkgID, bInvokableType, env.scope.owner,
                false, symTable.builtinPos, VIRTUAL);
        initFuncSymbol.retType = symTable.nilType;
        recordSymbol.initializerFunc = new BAttachedFunction(Names.INIT_FUNCTION_SUFFIX, initFuncSymbol,
                                                             bInvokableType, symTable.builtinPos);

        recordSymbol.scope = new Scope(recordSymbol);
        recordSymbol.scope.define(
                names.fromString(recordSymbol.name.value + "." + recordSymbol.initializerFunc.funcName.value),
                recordSymbol.initializerFunc.symbol);

        BRecordType immutableRecordType = new BRecordType(recordSymbol, origRecordType.flags | Flags.READONLY);

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

        TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, env, names, symTable);
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
                                                                    getImmutableTypeName(names,
                                                                            getSymbolFQN(origObjectTSymbol)),
                                                                    pkgID, null, env.scope.owner, pos, SOURCE);

        objectSymbol.scope = new Scope(objectSymbol);

        defineObjectFunctions(objectSymbol, origObjectTSymbol, names, symTable);

        BObjectType immutableObjectType = new BObjectType(objectSymbol, origObjectType.flags | Flags.READONLY);
        immutableObjectType.typeIdSet = origObjectType.typeIdSet;

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

        TypeDefBuilderHelper.populateStructureFields(types, symTable, anonymousModelHelper, names, objectTypeNode,
                immutableObjectType, origObjectType, pos, env, pkgID, unresolvedTypes, Flags.FINAL, true);

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
            Name funcName = names.fromString(Symbols.getAttachedFuncSymbolName(immutableObjectSymbol.name.value,
                                                                               origFunc.funcName.value));
            BInvokableSymbol immutableFuncSymbol =
                    ASTBuilderUtil.duplicateFunctionDeclarationSymbol(origFunc.symbol, immutableObjectSymbol,
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
                                                              BUnionType type) {
        BTypeSymbol origUnionTypeSymbol = type.tsymbol;

        LinkedHashSet<BType> originalMemberList = type.getMemberTypes();
        Optional<BIntersectionType> immutableTypeOptional = Types.getImmutableType(symTable, pkgId, type);
        if (immutableTypeOptional.isPresent()) {
            return immutableTypeOptional.get();
        } else {
            BUnionType immutableUnionType = BUnionType.create(origUnionTypeSymbol);
            immutableUnionType.mutableType = type;
            Types.addImmutableType(symTable, pkgId, type, createImmutableIntersectionType(pkgId, owner,
                                      type, immutableUnionType, symTable));
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
        BTypeSymbol immutableAnydataTSymbol = getReadonlyTSymbol(names, type.tsymbol, env, pkgId, owner);

        if (immutableAnydataTSymbol != null) {
            BAnydataType immutableAnydataType =
                    new BAnydataType(immutableAnydataTSymbol,
                                     immutableAnydataTSymbol.name, type.flags | Flags.READONLY,
                                     type.isNullable());
            immutableAnydataTSymbol.type = immutableAnydataType;
            return immutableAnydataType;
        }
         return new BAnydataType(immutableAnydataTSymbol,
                                 getImmutableTypeName(names, TypeKind.ANYDATA.typeName()),
                                 type.flags | Flags.READONLY, type.isNullable());
    }

    private static BJSONType defineImmutableJsonType(SymbolEnv env, PackageID pkgId, BSymbol owner, Names names,
                                                     BJSONType type) {
        BTypeSymbol immutableJsonTSymbol = getReadonlyTSymbol(names, type.tsymbol, env, pkgId, owner);
        BJSONType immutableJsonType = new BJSONType(immutableJsonTSymbol,
                                                    type.isNullable(),
                                                    type.flags | Flags.READONLY);
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
            unionEffectiveImmutableType.name = getImmutableTypeName(names, getSymbolFQN(origUnionTypeSymbol));
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
                                               getImmutableTypeName(names, getSymbolFQN(origUnionTypeSymbol)));
            immutableType.effectiveType.tsymbol = immutableUnionTSymbol;
            immutableType.effectiveType.flags |= (type.flags | Flags.READONLY);

            if (immutableUnionTSymbol != null) {
                immutableUnionTSymbol.type = immutableType.effectiveType;
            }
        } else {
            immutableType.effectiveType.flags |= (type.flags | Flags.READONLY);
        }

        return immutableType;
    }

    private static BTypeSymbol getReadonlyTSymbol(Names names, BTypeSymbol originalTSymbol, SymbolEnv env,
                                                  PackageID pkgId, BSymbol owner) {
        if (originalTSymbol == null) {
            return null;
        }

        return getReadonlyTSymbol(originalTSymbol, env, pkgId, owner, getImmutableTypeName(names, originalTSymbol));
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

    private static Name getImmutableTypeName(Names names, BTypeSymbol originalTSymbol) {
        return getImmutableTypeName(names, originalTSymbol.name.getValue());
    }

    private static Name getImmutableTypeName(Names names, String origName) {
        if (origName.isEmpty()) {
            return Names.EMPTY;
        }

        return names.fromString("(".concat(origName).concat(AND_READONLY_SUFFIX).concat(")"));
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
                                                                   (IntersectableReferenceType) effectiveType,
                                                                   Flags.READONLY);
        intersectionTypeSymbol.type = intersectionType;
        return intersectionType;
    }

    /**
     * @since 2201.6.0
     */
    public static class UpdateImmutableType implements TypeVisitor {

        private static final CompilerContext.Key<UpdateImmutableType> UPDATE_IMMUTABLE_TYPE_KEY =
                new CompilerContext.Key<>();
        private final SymbolTable symTable;
        private final Names names;
        private final Types types;
        private final BLangAnonymousModelHelper anonymousModelHelper;
        private Location loc;
        private PackageID pkgID;
        private BLangNode typeNode;
        private SymbolEnv env;
        private HashSet<BType> visitedImmutableTypes = new HashSet<>();

        public UpdateImmutableType(CompilerContext context) {
            context.put(UPDATE_IMMUTABLE_TYPE_KEY, this);
            this.symTable = SymbolTable.getInstance(context);
            this.names = Names.getInstance(context);
            this.types = Types.getInstance(context);
            this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        }

        public static UpdateImmutableType getInstance(CompilerContext context) {
            UpdateImmutableType updateImmutableType = context.get(UPDATE_IMMUTABLE_TYPE_KEY);
            if (updateImmutableType == null) {
                updateImmutableType = new UpdateImmutableType(context);
            }
            return updateImmutableType;
        }

        public void updateImmutableType(BType immutableType, Location loc, PackageID pkgID, BLangNode typeNode,
                                        SymbolEnv env) {
            if (!visitedImmutableTypes.add(immutableType)) {
                return;
            }
            Location prevLoc = loc;
            PackageID prevPkgID = pkgID;
            BLangNode prevTypeNode = typeNode;
            SymbolEnv prevEnv = env;
            this.loc = loc;
            this.pkgID = pkgID;
            this.typeNode = typeNode;
            this.env = env;
            immutableType.accept(this);
            this.loc = prevLoc;
            this.pkgID = prevPkgID;
            this.typeNode = prevTypeNode;
            this.env = prevEnv;
        }

        @Override
        public void visit(BAnnotationType bAnnotationType) {

        }

        @Override
        public void visit(BArrayType bArrayType) {
            BArrayType origArrayType = bArrayType.mutableType;
            if (origArrayType != null) {
                if (bArrayType.eType.tag == TypeTags.NEVER || bArrayType.eType == symTable.semanticError) {
                    bArrayType.eType = getImmutableType(loc, types, origArrayType.eType, env,
                            pkgID, env.scope.owner, symTable, anonymousModelHelper, names, new HashSet<>());
                }
                updateImmutableType(bArrayType.eType, loc, pkgID, typeNode, env);
                bArrayType.mutableType = null;
            }
        }

        @Override
        public void visit(BBuiltInRefType bBuiltInRefType) {

        }

        @Override
        public void visit(BAnyType bAnyType) {

        }

        @Override
        public void visit(BAnydataType bAnydataType) {

        }

        @Override
        public void visit(BErrorType bErrorType) {

        }

        @Override
        public void visit(BFiniteType bFiniteType) {

        }

        @Override
        public void visit(BInvokableType bInvokableType) {

        }

        @Override
        public void visit(BJSONType bjsonType) {

        }

        @Override
        public void visit(BMapType bMapType) {
            BMapType origMapType = bMapType.mutableType;
            if (origMapType != null) {
                if (bMapType.constraint.tag == TypeTags.NEVER || bMapType.constraint == symTable.semanticError) {
                    bMapType.constraint = getImmutableType(loc, types, origMapType.constraint, env,
                            pkgID, env.scope.owner, symTable, anonymousModelHelper, names,
                            new HashSet<>());
                }
                updateImmutableType(bMapType.constraint, loc, pkgID, typeNode, env);
                bMapType.mutableType = null;
            }
        }

        @Override
        public void visit(BStreamType bStreamType) {

        }

        @Override
        public void visit(BTypedescType bTypedescType) {

        }

        @Override
        public void visit(BTypeReferenceType bTypeReferenceType) {
            updateImmutableType(Types.getReferredType(bTypeReferenceType), loc, pkgID, typeNode, env);
        }

        @Override
        public void visit(BParameterizedType bTypedescType) {

        }

        @Override
        public void visit(BNeverType bNeverType) {

        }

        @Override
        public void visit(BNilType bNilType) {

        }

        @Override
        public void visit(BNoType bNoType) {

        }

        @Override
        public void visit(BPackageType bPackageType) {

        }

        @Override
        public void visit(BStructureType bStructureType) {

        }

        @Override
        public void visit(BTupleType bTupleType) {
            BTupleType origTupleType = bTupleType.mutableType;
            if (origTupleType != null) {
                if (origTupleType.getMembers().size() != bTupleType.getMembers().size()) {
                    List<BTupleMember> members = new ArrayList<>(origTupleType.getMembers().size());
                    bTupleType.setMembers(members);
                    for (BTupleMember origTupleMemType : origTupleType.getMembers()) {
                        if (types.isInherentlyImmutableType(origTupleMemType.type)) {
                            bTupleType.addMembers(origTupleMemType);
                            continue;
                        }
                        BType newType = getImmutableType(loc, types, origTupleMemType.type, env,
                                pkgID, env.scope.owner, symTable, anonymousModelHelper, names, new HashSet<>());
                        BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(newType);
                        BTupleMember member = new BTupleMember(newType, varSymbol);
                        bTupleType.addMembers(member);
                    }
                } else {
                    for (BType memberType : bTupleType.getTupleTypes()) {
                        updateImmutableType(memberType, loc, pkgID, typeNode, env);
                    }
                }
                bTupleType.mutableType = null;
            }
        }

        @Override
        public void visit(BUnionType bUnionType) {
            BUnionType origUnionType = bUnionType.mutableType;
            if (origUnionType != null) {
                LinkedHashSet<BType> originalMemberList = origUnionType.getMemberTypes();
                if (originalMemberList.size() != bUnionType.getMemberTypes().size()) {
                    // Update all the members
                    LinkedHashSet<BType> readOnlyMemTypes = new LinkedHashSet<>(originalMemberList.size());
                    bUnionType.setMemberTypes(readOnlyMemTypes);

                    for (BType memberType : originalMemberList) {
                        if (types.isInherentlyImmutableType(memberType)) {
                            bUnionType.add(memberType);
                            continue;
                        }

                        BType immutableMemberType = getImmutableType(loc, types, memberType, env, pkgID,
                                env.scope.owner, symTable, anonymousModelHelper, names, new HashSet<>());
                        bUnionType.add(immutableMemberType);
                    }
                    return;
                }
            }

            for (BType immutableMemberType : bUnionType.getMemberTypes()) {
                updateImmutableType(immutableMemberType, loc, pkgID, typeNode, env);
            }
            bUnionType.mutableType = null;
        }

        @Override
        public void visit(BIntersectionType bIntersectionType) {
            updateImmutableType(bIntersectionType.effectiveType, loc, pkgID, typeNode, env);
        }

        @Override
        public void visit(BXMLType bxmlType) {

        }

        @Override
        public void visit(BTableType bTableType) {
            BTableType origTableType = bTableType.mutableType;
            if (origTableType != null) {
                if (bTableType.constraint.tag == TypeTags.NEVER ||
                        bTableType.constraint == symTable.semanticError) {
                    bTableType.constraint = getImmutableType(loc, types, origTableType.constraint, env,
                            pkgID, env.scope.owner, symTable, anonymousModelHelper, names,
                            new HashSet<>());
                }
                updateImmutableType(bTableType.constraint, loc, pkgID, typeNode, env);

                if (origTableType.keyTypeConstraint != null) {
                    bTableType.keyTypeConstraint = getImmutableType(loc, types, origTableType.keyTypeConstraint, env,
                            pkgID, env.scope.owner, symTable, anonymousModelHelper, names,
                            new HashSet<>());
                    updateImmutableType(bTableType.keyTypeConstraint, loc, pkgID, typeNode, env);
                }
                bTableType.mutableType = null;
            }
        }

        @Override
        public void visit(BRecordType bRecordType) {
            BRecordType origRecordType = bRecordType.mutableType;
            BTypeSymbol structureSymbol = bRecordType.tsymbol;
            if (origRecordType != null) {
                if (origRecordType.fields.size() != bRecordType.fields.size()) {
                    LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
                    for (BField origField : origRecordType.fields.values()) {
                        BType fieldType = ImmutableTypeCloner.getImmutableType(loc, types, origField.type, env,
                                env.enclPkg.packageID, env.scope.owner, symTable, anonymousModelHelper, names,
                                new HashSet<>());

                        Name origFieldName = origField.name;
                        BVarSymbol fieldSymbol;
                        BType referredType = Types.getReferredType(fieldType);
                        if (referredType.tag == TypeTags.INVOKABLE && referredType.tsymbol != null) {
                            fieldSymbol = new BInvokableSymbol(origField.symbol.tag,
                                    origField.symbol.flags | Flags.READONLY, origFieldName, pkgID, fieldType,
                                    structureSymbol, origField.symbol.pos, SOURCE);
                            BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) referredType.tsymbol;
                            BInvokableSymbol invokableSymbol = (BInvokableSymbol) fieldSymbol;
                            invokableSymbol.params = tsymbol.params == null ? null : new ArrayList<>(tsymbol.params);
                            invokableSymbol.restParam = tsymbol.restParam;
                            invokableSymbol.retType = tsymbol.returnType;
                            invokableSymbol.flags = tsymbol.flags;
                        } else if (fieldType == symTable.semanticError) {
                            // Can only happen for records.
                            fieldSymbol = new BVarSymbol(origField.symbol.flags | Flags.READONLY | Flags.OPTIONAL,
                                    origFieldName, pkgID, symTable.neverType,
                                    structureSymbol, origField.symbol.pos, SOURCE);
                        } else {
                            fieldSymbol = new BVarSymbol(origField.symbol.flags | Flags.READONLY, origFieldName, pkgID,
                                    fieldType, structureSymbol,
                                    origField.symbol.pos, SOURCE);
                        }
                        String nameString = origFieldName.value;
                        fields.put(nameString, new BField(origFieldName, null, fieldSymbol));
                        structureSymbol.scope.define(origFieldName, fieldSymbol);
                    }
                    bRecordType.fields = fields;
                    bRecordType.restFieldType = origRecordType.restFieldType;
                } else {
                    for (BField immutableField : bRecordType.fields.values()) {
                        BField origField = origRecordType.fields.get(immutableField.name.value);
                        if (immutableField.type.tag == TypeTags.NEVER) {
                            immutableField.type = ImmutableTypeCloner.getImmutableType(loc, types, origField.type, env,
                                    env.enclPkg.packageID, env.scope.owner, symTable, anonymousModelHelper, names,
                                    new HashSet<>());
                        }
                        Name origFieldName = origField.name;
                        updateImmutableType(immutableField.type, loc, pkgID, typeNode, env);
                        immutableField.symbol = new BVarSymbol(origField.symbol.flags | Flags.READONLY, origFieldName,
                                pkgID, immutableField.type, structureSymbol, origField.symbol.pos, SOURCE);
                        structureSymbol.scope.define(origFieldName, immutableField.symbol);
                    }
                }
                bRecordType.mutableType = null;
            }
        }

        @Override
        public void visit(BObjectType bObjectType) {
            BObjectType origObjectType = bObjectType.mutableType;
            BTypeSymbol structureSymbol = bObjectType.tsymbol;
            if (origObjectType != null) {
                if (origObjectType.fields.size() != bObjectType.fields.size()) {
                    LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
                    for (BField origField : origObjectType.fields.values()) {
                        BType fieldType = ImmutableTypeCloner.getImmutableType(loc, types, origField.type, env,
                                env.enclPkg.packageID, env.scope.owner, symTable, anonymousModelHelper, names,
                                new HashSet<>());

                        Name origFieldName = origField.name;
                        BVarSymbol fieldSymbol;
                        BType referredType = Types.getReferredType(fieldType);
                        if (referredType.tag == TypeTags.INVOKABLE && referredType.tsymbol != null) {
                            fieldSymbol = new BInvokableSymbol(origField.symbol.tag,
                                    origField.symbol.flags | Flags.READONLY, origFieldName, pkgID, fieldType,
                                    structureSymbol, origField.symbol.pos, SOURCE);
                            BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) referredType.tsymbol;
                            BInvokableSymbol invokableSymbol = (BInvokableSymbol) fieldSymbol;
                            invokableSymbol.params = tsymbol.params == null ? null : new ArrayList<>(tsymbol.params);
                            invokableSymbol.restParam = tsymbol.restParam;
                            invokableSymbol.retType = tsymbol.returnType;
                            invokableSymbol.flags = tsymbol.flags;
                        } else {
                            fieldSymbol = new BVarSymbol(origField.symbol.flags | Flags.READONLY, origFieldName, pkgID,
                                    fieldType, structureSymbol,
                                    origField.symbol.pos, SOURCE);
                        }
                        String nameString = origFieldName.value;
                        fields.put(nameString, new BField(origFieldName, null, fieldSymbol));
                        structureSymbol.scope.define(origFieldName, fieldSymbol);
                    }
                    bObjectType.fields = fields;
                } else {
                    for (BField immutableField : bObjectType.fields.values()) {
                        BField origField = origObjectType.fields.get(immutableField.name.value);
                        if (immutableField.type.tag == TypeTags.NEVER) {
                            immutableField.type = ImmutableTypeCloner.getImmutableType(loc, types, origField.type, env,
                                    env.enclPkg.packageID, env.scope.owner, symTable, anonymousModelHelper, names,
                                    new HashSet<>());
                        }
                        Name origFieldName = origField.name;
                        updateImmutableType(immutableField.type, loc, pkgID, typeNode, env);
                        immutableField.symbol = new BVarSymbol(origField.symbol.flags | Flags.READONLY, origFieldName,
                                pkgID, immutableField.type, structureSymbol, origField.symbol.pos, SOURCE);
                        structureSymbol.scope.define(origFieldName, immutableField.symbol);
                    }
                }
                bObjectType.mutableType = null;
            }
        }

        @Override
        public void visit(BType bType) {

        }

        @Override
        public void visit(BFutureType bFutureType) {

        }

        @Override
        public void visit(BHandleType bHandleType) {

        }
    }
}

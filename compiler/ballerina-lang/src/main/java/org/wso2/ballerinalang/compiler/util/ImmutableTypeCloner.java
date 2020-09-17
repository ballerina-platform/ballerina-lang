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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * Helper class to create a clone of it.
 *
 * @since 1.3.0
 */
public class ImmutableTypeCloner {

    private static final String AND_READONLY_SUFFIX = " & readonly";

    public static BType getEffectiveImmutableType(DiagnosticPos pos, Types types,
                                                  SelectivelyImmutableReferenceType type, SymbolEnv env,
                                                  SymbolTable symTable, BLangAnonymousModelHelper anonymousModelHelper,
                                                  Names names) {
        return getImmutableIntersectionType(pos, types, type, env, env.enclPkg.packageID, env.scope.owner,
                                            symTable, anonymousModelHelper, names, new HashSet<>(),
                                            new HashSet<>()).effectiveType;
    }

    public static BType getEffectiveImmutableType(DiagnosticPos pos, Types types,
                                                  SelectivelyImmutableReferenceType type, PackageID pkgId,
                                                  BSymbol owner, SymbolTable symTable,
                                                  BLangAnonymousModelHelper anonymousModelHelper, Names names) {
        return getImmutableIntersectionType(pos, types, type, null, pkgId, owner,
                                            symTable, anonymousModelHelper, names, new HashSet<>(),
                                            new HashSet<>()).effectiveType;
    }

    public static BIntersectionType getImmutableIntersectionType(DiagnosticPos pos, Types types,
                                                                 SelectivelyImmutableReferenceType type,
                                                                 SymbolEnv env, SymbolTable symTable,
                                                                 BLangAnonymousModelHelper anonymousModelHelper,
                                                                 Names names, Set<Flag> origObjFlagSet) {
        return getImmutableIntersectionType(pos, types, type, env, env.enclPkg.packageID, env.scope.owner,
                                            symTable, anonymousModelHelper, names, origObjFlagSet, new HashSet<>());
    }

    private static BType getImmutableType(DiagnosticPos pos, Types types, BType type, SymbolEnv env, PackageID pkgId,
                                          BSymbol owner, SymbolTable symTable,
                                          BLangAnonymousModelHelper anonymousModelHelper, Names names,
                                          Set<BType> unresolvedTypes) {
        if (types.isInherentlyImmutableType(type)) {
            return type;
        }

        if (!types.isSelectivelyImmutableType(type, unresolvedTypes)) {
            return symTable.semanticError;
        }

        return getImmutableIntersectionType(pos, types, (SelectivelyImmutableReferenceType) type, env, pkgId,
                                            owner, symTable, anonymousModelHelper, names, new HashSet<>(),
                                            unresolvedTypes);
    }

    private static BIntersectionType getImmutableIntersectionType(DiagnosticPos pos,
                                                                  Types types, SelectivelyImmutableReferenceType type,
                                                                  SymbolEnv env, PackageID pkgId,
                                                                  BSymbol owner, SymbolTable symTable,
                                                                  BLangAnonymousModelHelper anonymousModelHelper,
                                                                  Names names,
                                                                  Set<Flag> origObjFlagSet,
                                                                  Set<BType> unresolvedTypes) {
        BType origBType =  (BType) type;
        if (origBType.tag == TypeTags.INTERSECTION && Symbols.isFlagOn(origBType.flags, Flags.READONLY)) {
            return (BIntersectionType) origBType;
        }

        BIntersectionType immutableType = type.getImmutableType();
        if (immutableType != null) {
            return immutableType;
        }

        return ImmutableTypeCloner.setImmutableType(pos, types, type, env, pkgId, owner, symTable,
                                                    anonymousModelHelper, names, origObjFlagSet, unresolvedTypes);
    }

    private static BIntersectionType setImmutableType(DiagnosticPos pos, Types types,
                                                      SelectivelyImmutableReferenceType selectivelyImmutableRefType,
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
                        createImmutableIntersectionType(pkgId, owner, origXmlSubType, immutableXmlSubType, symTable);
                origXmlSubType.immutableType = immutableXmlSubTypeIntersectionType;
                return immutableXmlSubTypeIntersectionType;
            case TypeTags.XML:
                BXMLType origXmlType = (BXMLType) type;

                BTypeSymbol immutableXmlTSymbol = getReadonlyTSymbol(names, origXmlType.tsymbol, env, pkgId, owner);
                BXMLType immutableXmlType = new BXMLType(getImmutableType(pos, types, origXmlType.constraint, env,
                                                                          pkgId, owner, symTable, anonymousModelHelper,
                                                                          names, unresolvedTypes),
                                                immutableXmlTSymbol, origXmlType.flags | Flags.READONLY);
                immutableXmlTSymbol.type = immutableXmlType;

                BIntersectionType immutableXmlIntersectionType = createImmutableIntersectionType(pkgId, owner,
                                                                                                 origXmlType,
                                                                                                 immutableXmlType,
                                                                                                 symTable);
                origXmlType.immutableType = immutableXmlIntersectionType;
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

                if (immutableArrayTSymbol != null) {
                    immutableArrayTSymbol.type = immutableArrayType;
                }

                BIntersectionType immutableArrayIntersectionType = createImmutableIntersectionType(env, origArrayType,
                                                                                                   immutableArrayType,
                                                                                                   symTable);
                origArrayType.immutableType = immutableArrayIntersectionType;
                return immutableArrayIntersectionType;
            case TypeTags.TUPLE:
                BTupleType origTupleType = (BTupleType) type;

                List<BType> origTupleMemTypes = origTupleType.tupleTypes;
                List<BType> immutableMemTypes = new ArrayList<>(origTupleMemTypes.size());

                for (BType origTupleMemType : origTupleMemTypes) {
                    immutableMemTypes.add(getImmutableType(pos, types, origTupleMemType, env, pkgId, owner, symTable,
                                                           anonymousModelHelper, names, unresolvedTypes));
                }

                BTypeSymbol immutableTupleTSymbol = getReadonlyTSymbol(names, origTupleType.tsymbol, env, pkgId, owner);
                BType origRestType = origTupleType.restType;
                BType tupleRestType = origRestType == null ? origRestType :
                        getImmutableType(pos, types, origRestType, env, pkgId, owner, symTable, anonymousModelHelper,
                                         names, unresolvedTypes);

                BTupleType immutableTupleType = new BTupleType(immutableTupleTSymbol, immutableMemTypes, tupleRestType,
                                                    origTupleType.flags | Flags.READONLY);
                if (immutableTupleTSymbol != null) {
                    immutableTupleTSymbol.type = immutableTupleType;
                }

                BIntersectionType immutableTupleIntersectionType = createImmutableIntersectionType(env, origTupleType,
                                                                                                   immutableTupleType,
                                                                                                   symTable);
                origTupleType.immutableType = immutableTupleIntersectionType;
                return immutableTupleIntersectionType;
            case TypeTags.MAP:
                BMapType origMapType = (BMapType) type;

                BTypeSymbol immutableMapTSymbol = getReadonlyTSymbol(names, origMapType.tsymbol, env, pkgId, owner);
                BMapType immutableMapType = new BMapType(origMapType.tag,
                                                         getImmutableType(pos, types, origMapType.constraint, env,
                                                                          pkgId, owner, symTable, anonymousModelHelper,
                                                                          names, unresolvedTypes),
                                                immutableMapTSymbol, origMapType.flags | Flags.READONLY);
                if (immutableMapTSymbol != null) {
                    immutableMapTSymbol.type = immutableMapType;
                }

                BIntersectionType immutableMapIntersectionType = createImmutableIntersectionType(env, origMapType,
                                                                                                 immutableMapType,
                                                                                                 symTable);
                origMapType.immutableType = immutableMapIntersectionType;
                return immutableMapIntersectionType;
            case TypeTags.RECORD:
                BRecordType origRecordType = (BRecordType) type;

                return defineImmutableRecordType(pos, origRecordType, env, symTable, anonymousModelHelper, names, types,
                                                 unresolvedTypes);
            case TypeTags.OBJECT:
                BObjectType origObjectType = (BObjectType) type;

                return defineImmutableObjectType(pos, origObjectType, env, symTable, anonymousModelHelper, names, types,
                                                 origObjFlagSet, unresolvedTypes);
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
                immutableTableType.fieldNameList = origTableType.fieldNameList;

                if (immutableTableTSymbol != null) {
                    immutableTableTSymbol.type = immutableTableType;
                }

                BIntersectionType immutableTableIntersectionType = createImmutableIntersectionType(env, origTableType,
                                                                                                   immutableTableType,
                                                                                                   symTable);
                origTableType.immutableType = immutableTableIntersectionType;
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
                                                                                                 origAnyType,
                                                                                                 immutableAnyType,
                                                                                                 symTable);
                origAnyType.immutableType = immutableAnyIntersectionType;
                return immutableAnyIntersectionType;
            case TypeTags.ANYDATA:
                BAnydataType origAnydataType = (BAnydataType) type;

                BTypeSymbol immutableAnydataTSymbol = getReadonlyTSymbol(names, origAnydataType.tsymbol, env, pkgId,
                                                                         owner);

                BAnydataType immutableAnydataType;
                if (immutableAnydataTSymbol != null) {
                    immutableAnydataType =
                            new BAnydataType(origAnydataType.tag, immutableAnydataTSymbol,
                                             immutableAnydataTSymbol.name, origAnydataType.flags | Flags.READONLY,
                                             origAnydataType.isNullable());
                    immutableAnydataTSymbol.type = immutableAnydataType;
                } else {
                    immutableAnydataType =
                            new BAnydataType(origAnydataType.tag, immutableAnydataTSymbol,
                                             getImmutableTypeName(names, TypeKind.ANYDATA.typeName()),
                                             origAnydataType.flags | Flags.READONLY, origAnydataType.isNullable());
                }

                BIntersectionType immutableAnydataIntersectionType =
                        createImmutableIntersectionType(pkgId, owner, origAnydataType, immutableAnydataType, symTable);
                origAnydataType.immutableType = immutableAnydataIntersectionType;
                return immutableAnydataIntersectionType;
            case TypeTags.JSON:
                BJSONType origJsonType = (BJSONType) type;

                BTypeSymbol immutableJsonTSymbol = getReadonlyTSymbol(names, origJsonType.tsymbol, env, pkgId, owner);
                BJSONType immutableJsonType = new BJSONType(origJsonType.tag, immutableJsonTSymbol,
                                                            origJsonType.isNullable(),
                                                            origJsonType.flags | Flags.READONLY);
                if (immutableJsonTSymbol != null) {
                    immutableJsonTSymbol.type = immutableJsonType;
                }

                BIntersectionType immutableJsonIntersectionType = createImmutableIntersectionType(pkgId, owner,
                                                                                                  origJsonType,
                                                                                                  immutableJsonType,
                                                                                                  symTable);
                origJsonType.immutableType = immutableJsonIntersectionType;
                return immutableJsonIntersectionType;
            case TypeTags.INTERSECTION:
                return (BIntersectionType) type;
            default:
                BUnionType origUnionType = (BUnionType) type;
                BType immutableType;

                LinkedHashSet<BType> readOnlyMemTypes = new LinkedHashSet<>();

                for (BType memberType : origUnionType.getMemberTypes()) {
                    if (types.isInherentlyImmutableType(memberType)) {
                        readOnlyMemTypes.add(memberType);
                        continue;
                    }

                    if (!types.isSelectivelyImmutableType(memberType, unresolvedTypes)) {
                        continue;
                    }

                    readOnlyMemTypes.add(getImmutableType(pos, types, memberType, env, pkgId, owner, symTable,
                                                          anonymousModelHelper, names, unresolvedTypes));
                }

                if (readOnlyMemTypes.size() == 1) {
                    immutableType = readOnlyMemTypes.iterator().next();
                } else if (origUnionType.tsymbol != null) {
                    BTypeSymbol immutableUnionTSymbol = getReadonlyTSymbol(names, origUnionType.tsymbol, env, pkgId,
                                                                           owner);
                    immutableType = BUnionType.create(immutableUnionTSymbol, readOnlyMemTypes);
                    immutableType.flags |= (origUnionType.flags | Flags.READONLY);
                    if (immutableUnionTSymbol != null) {
                        immutableUnionTSymbol.type = immutableType;
                    }
                } else {
                    immutableType = BUnionType.create(null, readOnlyMemTypes);
                    immutableType.flags |= (origUnionType.flags | Flags.READONLY);
                }

                BIntersectionType immutableUnionIntersectionType = createImmutableIntersectionType(env, origUnionType,
                                                                                                   immutableType,
                                                                                                   symTable);
                origUnionType.immutableType = immutableUnionIntersectionType;
                return immutableUnionIntersectionType;
        }
    }

    public static void defineUndefinedImmutableFields(BLangTypeDefinition immutableTypeDefinition,
                                                      Types types, SymbolEnv pkgEnv, SymbolTable symTable,
                                                      BLangAnonymousModelHelper anonymousModelHelper,
                                                      Names names) {
        DiagnosticPos pos = immutableTypeDefinition.pos;
        SymbolEnv env = SymbolEnv.createTypeEnv(immutableTypeDefinition.typeNode, immutableTypeDefinition.symbol.scope,
                                                pkgEnv);
        PackageID pkgID = env.enclPkg.symbol.pkgID;

        BType immutableType = immutableTypeDefinition.type;
        if (immutableType.tag == TypeTags.RECORD) {
            defineUndefinedImmutableRecordFields((BRecordType) immutableType, pos, pkgID, immutableTypeDefinition,
                                                 types, env, symTable, anonymousModelHelper, names);
            return;
        }
        defineUndefinedImmutableObjectFields((BObjectType) immutableType, pos, pkgID, immutableTypeDefinition,
                                             types, env, symTable, anonymousModelHelper, names);
    }

    private static void defineUndefinedImmutableRecordFields(BRecordType immutableRecordType, DiagnosticPos pos,
                                                             PackageID pkgID,
                                                             BLangTypeDefinition immutableTypeDefinition,
                                                             Types types, SymbolEnv env, SymbolTable symTable,
                                                             BLangAnonymousModelHelper anonymousModelHelper,
                                                             Names names) {
        BRecordType origRecordType = immutableRecordType.mutableType;
        if (origRecordType.fields.size() != immutableRecordType.fields.size()) {

            populateImmutableStructureFields(types, symTable, anonymousModelHelper, names,
                                             (BLangRecordTypeNode) immutableTypeDefinition.typeNode,
                                             immutableRecordType, origRecordType, pos, env, pkgID, new HashSet<>());
        }

        BType currentRestFieldType = immutableRecordType.restFieldType;
        if (currentRestFieldType != null && currentRestFieldType != symTable.noType) {
            return;
        }

        setRestType(types, symTable, anonymousModelHelper, names, immutableRecordType, origRecordType, pos, env,
                    new HashSet<>());
    }

    private static void defineUndefinedImmutableObjectFields(BObjectType immutableObjectType, DiagnosticPos pos,
                                                             PackageID pkgID,
                                                             BLangTypeDefinition immutableTypeDefinition,
                                                             Types types, SymbolEnv env, SymbolTable symTable,
                                                             BLangAnonymousModelHelper anonymousModelHelper,
                                                             Names names) {
        BObjectType origObjectType = immutableObjectType.mutableType;
        if (origObjectType.fields.size() != immutableObjectType.fields.size()) {

            populateImmutableStructureFields(types, symTable, anonymousModelHelper, names,
                                             (BLangObjectTypeNode) immutableTypeDefinition.typeNode,
                                             immutableObjectType, origObjectType,
                                             pos, env, pkgID, new HashSet<>(), Flags.FINAL);
        }
    }

    private static void populateImmutableStructureFields(Types types, SymbolTable symTable,
                                                         BLangAnonymousModelHelper anonymousModelHelper, Names names,
                                                         BLangStructureTypeNode immutableStructureTypeNode,
                                                         BStructureType immutableStructureType,
                                                         BStructureType origStructureType, DiagnosticPos pos,
                                                         SymbolEnv env, PackageID pkgID, Set<BType> unresolvedTypes) {
        populateImmutableStructureFields(types, symTable, anonymousModelHelper, names, immutableStructureTypeNode,
                                         immutableStructureType, origStructureType, pos, env, pkgID, unresolvedTypes,
                                         Flags.READONLY);
    }

    private static void populateImmutableStructureFields(Types types, SymbolTable symTable,
                                                         BLangAnonymousModelHelper anonymousModelHelper, Names names,
                                                         BLangStructureTypeNode immutableStructureTypeNode,
                                                         BStructureType immutableStructureType,
                                                         BStructureType origStructureType, DiagnosticPos pos,
                                                         SymbolEnv env, PackageID pkgID, Set<BType> unresolvedTypes,
                                                         int flag) {
        BTypeSymbol immutableStructureSymbol = immutableStructureType.tsymbol;
        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
        for (BField origField : origStructureType.fields.values()) {
            BType immutableFieldType = getImmutableType(pos, types, origField.type, env, env.enclPkg.packageID,
                                                        env.scope.owner, symTable, anonymousModelHelper, names,
                                                        unresolvedTypes);

            Name origFieldName = origField.name;
            BVarSymbol immutableFieldSymbol = new BVarSymbol(origField.symbol.flags | flag,
                                                             origFieldName, pkgID, immutableFieldType,
                                                             immutableStructureSymbol, origField.pos, SOURCE);
            if (immutableFieldType.tag == TypeTags.INVOKABLE && immutableFieldType.tsymbol != null) {
                BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) immutableFieldType.tsymbol;
                BInvokableSymbol invokableSymbol = (BInvokableSymbol) immutableFieldSymbol;
                invokableSymbol.params = tsymbol.params;
                invokableSymbol.restParam = tsymbol.restParam;
                invokableSymbol.retType = tsymbol.returnType;
                invokableSymbol.flags = tsymbol.flags;
            }
            String nameString = origFieldName.value;
            fields.put(nameString, new BField(origFieldName, null, immutableFieldSymbol));
            immutableStructureSymbol.scope.define(origFieldName, immutableFieldSymbol);
        }
        immutableStructureType.fields = fields;

        BLangUserDefinedType origTypeRef = new BLangUserDefinedType(
                ASTBuilderUtil.createIdentifier(pos, getPackageAlias(env, pos.getSource().getCompilationUnitName(),
                                                                     origStructureType.tsymbol.pkgID)),
                ASTBuilderUtil.createIdentifier(pos, origStructureType.tsymbol.name.value));
        origTypeRef.pos = pos;
        origTypeRef.type = origStructureType;
        immutableStructureTypeNode.typeRefs.add(origTypeRef);
    }

    private static String getPackageAlias(SymbolEnv env, String compUnitName, PackageID typePkgId) {
        for (BLangImportPackage importStmt : env.enclPkg.imports) {
            if (!typePkgId.equals(importStmt.symbol.pkgID)) {
                continue;
            }

            if (importStmt.compUnit.value.equals(compUnitName)) {
                return importStmt.alias.value;
            }

        }

        return ""; // current module
    }

    private static void setRestType(Types types, SymbolTable symTable, BLangAnonymousModelHelper anonymousModelHelper,
                                    Names names, BRecordType immutableRecordType, BRecordType origRecordType,
                                    DiagnosticPos pos, SymbolEnv env, Set<BType> unresolvedTypes) {
        immutableRecordType.sealed = origRecordType.sealed;

        BType origRestFieldType = origRecordType.restFieldType;

        if (origRestFieldType == null || origRestFieldType == symTable.noType) {
            immutableRecordType.restFieldType = origRestFieldType;
            return;
        }
        immutableRecordType.restFieldType = getImmutableType(pos, types, origRestFieldType, env, env.enclPkg.packageID,
                                                             env.scope.owner, symTable, anonymousModelHelper, names,
                                                             unresolvedTypes);
    }

    private static BIntersectionType defineImmutableRecordType(DiagnosticPos pos, BRecordType origRecordType,
                                                               SymbolEnv env, SymbolTable symTable,
                                                               BLangAnonymousModelHelper anonymousModelHelper,
                                                               Names names, Types types, Set<BType> unresolvedTypes) {
        PackageID pkgID = env.enclPkg.symbol.pkgID;
        BRecordTypeSymbol recordSymbol =
                Symbols.createRecordSymbol(origRecordType.tsymbol.flags | Flags.READONLY,
                        getImmutableTypeName(names, origRecordType.tsymbol.toString()),
                        pkgID, null, env.scope.owner, pos, SOURCE);

        BInvokableType bInvokableType = new BInvokableType(new ArrayList<>(), symTable.nilType, null);
        BInvokableSymbol initFuncSymbol = Symbols.createFunctionSymbol(
                Flags.PUBLIC, Names.EMPTY, env.enclPkg.symbol.pkgID, bInvokableType, env.scope.owner, false,
                symTable.builtinPos, VIRTUAL);
        initFuncSymbol.retType = symTable.nilType;
        recordSymbol.initializerFunc = new BAttachedFunction(Names.INIT_FUNCTION_SUFFIX, initFuncSymbol,
                                                             bInvokableType, symTable.builtinPos);

        recordSymbol.scope = new Scope(recordSymbol);
        recordSymbol.scope.define(
                names.fromString(recordSymbol.name.value + "." + recordSymbol.initializerFunc.funcName.value),
                recordSymbol.initializerFunc.symbol);

        BRecordType immutableRecordType = new BRecordType(recordSymbol, origRecordType.flags | Flags.READONLY);

        BIntersectionType immutableRecordIntersectionType = createImmutableIntersectionType(env, origRecordType,
                                                                                            immutableRecordType,
                                                                                            symTable);

        origRecordType.immutableType = immutableRecordIntersectionType;
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
        BLangTypeDefinition typeDefinition = TypeDefBuilderHelper.addTypeDefinition(immutableRecordType, recordSymbol,
                                                                                    recordTypeNode, env);
        typeDefinition.pos = pos;
        return immutableRecordIntersectionType;
    }

    private static BIntersectionType defineImmutableObjectType(DiagnosticPos pos,
                                                               BObjectType origObjectType, SymbolEnv env,
                                                               SymbolTable symTable,
                                                               BLangAnonymousModelHelper anonymousModelHelper,
                                                               Names names, Types types,
                                                               Set<Flag> flagSet, Set<BType> unresolvedTypes) {
        PackageID pkgID = env.enclPkg.symbol.pkgID;
        BObjectTypeSymbol origObjectTSymbol = (BObjectTypeSymbol) origObjectType.tsymbol;
        BObjectTypeSymbol objectSymbol =
                Symbols.createObjectSymbol(origObjectTSymbol.flags | Flags.READONLY,
                        getImmutableTypeName(names, origObjectTSymbol.toString()),
                        pkgID, null, env.scope.owner, pos, SOURCE);

        objectSymbol.scope = new Scope(objectSymbol);

        defineObjectFunctions(objectSymbol, origObjectTSymbol, names, symTable);

        BObjectType immutableObjectType = new BObjectType(objectSymbol, origObjectType.flags | Flags.READONLY);

        BIntersectionType immutableObjectIntersectionType = createImmutableIntersectionType(env, origObjectType,
                                                                                            immutableObjectType,
                                                                                            symTable);

        origObjectType.immutableType = immutableObjectIntersectionType;
        immutableObjectType.mutableType = origObjectType;

        objectSymbol.type = immutableObjectType;
        immutableObjectType.tsymbol = objectSymbol;

        BLangObjectTypeNode objectTypeNode = TypeDefBuilderHelper.createObjectTypeNode(new ArrayList<>(),
                                                                                       immutableObjectType, pos);
        objectTypeNode.flagSet.addAll(flagSet);

        populateImmutableStructureFields(types, symTable, anonymousModelHelper, names, objectTypeNode,
                                         immutableObjectType, origObjectType, pos, env, pkgID, unresolvedTypes,
                                         Flags.FINAL);

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

    private static BTypeSymbol getReadonlyTSymbol(Names names, BTypeSymbol originalTSymbol, SymbolEnv env,
                                                  PackageID pkgId, BSymbol owner) {
        if (originalTSymbol == null) {
            return null;
        }

        if (env == null) {
            return Symbols.createTypeSymbol(originalTSymbol.tag, originalTSymbol.flags | Flags.READONLY,
                                            getImmutableTypeName(names, originalTSymbol), pkgId, null, owner,
                                            originalTSymbol.pos, SOURCE);
        }

        return Symbols.createTypeSymbol(originalTSymbol.tag, originalTSymbol.flags | Flags.READONLY,
                                        getImmutableTypeName(names, originalTSymbol), env.enclPkg.symbol.pkgID, null,
                                        env.scope.owner, originalTSymbol.pos, SOURCE);
    }

    private static Name getImmutableTypeName(Names names, BTypeSymbol originalTSymbol) {
        return getImmutableTypeName(names, originalTSymbol.name.getValue());
    }

    private static Name getImmutableTypeName(Names names, String origName) {
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

        LinkedHashSet<BType> constituentTypes = new LinkedHashSet<BType>() {{
            add(nonReadOnlyType);
            add(symTable.readonlyType);
        }};

        BIntersectionType intersectionType = new BIntersectionType(intersectionTypeSymbol, constituentTypes,
                                                                   effectiveType, Flags.READONLY);
        intersectionTypeSymbol.type = intersectionType;
        return intersectionType;
    }
}

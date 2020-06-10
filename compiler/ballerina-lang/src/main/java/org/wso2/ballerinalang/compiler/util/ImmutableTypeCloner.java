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
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class to create a clone of it.
 *
 * @since 1.3.0
 */
public class ImmutableTypeCloner {

    private static final String AND_READONLY_SUFFIX = " & readonly";

    public static BIntersectionType getImmutableIntersectionType(DiagnosticPos pos, Types types,
                                                                 SelectivelyImmutableReferenceType type,
                                                                 SymbolEnv env, SymbolTable symTable,
                                                                 BLangAnonymousModelHelper anonymousModelHelper,
                                                                 Names names) {
        return getImmutableIntersectionType(null, pos, types, type, env, symTable, anonymousModelHelper, names,
                                            new HashSet<>());
    }

    public static BIntersectionType getImmutableIntersectionType(BLangType origTypeNode, DiagnosticPos pos, Types types,
                                                                 SelectivelyImmutableReferenceType type,
                                                                 SymbolEnv env, SymbolTable symTable,
                                                                 BLangAnonymousModelHelper anonymousModelHelper,
                                                                 Names names) {
        return getImmutableIntersectionType(origTypeNode, pos, types, type, env, symTable, anonymousModelHelper, names,
                                            new HashSet<>());
    }

    private static BType getImmutableType(DiagnosticPos pos, Types types, BType type, SymbolEnv env,
                                          SymbolTable symTable, BLangAnonymousModelHelper anonymousModelHelper,
                                          Names names, Set<BType> unresolvedTypes) {
        if (types.isInherentlyImmutableType(type)) {
            return type;
        }

        if (!types.isSelectivelyImmutableType(type, unresolvedTypes)) {
            return symTable.semanticError;
        }

        return getImmutableIntersectionType(null, pos, types, (SelectivelyImmutableReferenceType) type, env, symTable,
                                            anonymousModelHelper, names, unresolvedTypes);
    }

    private static BIntersectionType getImmutableIntersectionType(BLangType origTypeNode, DiagnosticPos pos,
                                                                  Types types, SelectivelyImmutableReferenceType type,
                                                                  SymbolEnv env, SymbolTable symTable,
                                                                  BLangAnonymousModelHelper anonymousModelHelper,
                                                                  Names names, Set<BType> unresolvedTypes) {
        BType origBType =  (BType) type;
        if (origBType.tag == TypeTags.INTERSECTION && Symbols.isFlagOn(origBType.flags, Flags.READONLY)) {
            return (BIntersectionType) origBType;
        }

        BIntersectionType immutableType = type.getImmutableType();
        if (immutableType != null) {
            return immutableType;
        }

        return ImmutableTypeCloner.setImmutableType(origTypeNode, pos, types, type, env, symTable, anonymousModelHelper,
                                                    names, unresolvedTypes);
    }

    private static BIntersectionType setImmutableType(BLangType origTypeNode, DiagnosticPos pos, Types types,
                                                      SelectivelyImmutableReferenceType selectivelyImmutableRefType,
                                                      SymbolEnv env, SymbolTable symTable,
                                                      BLangAnonymousModelHelper anonymousModelHelper,
                                                      Names names, Set<BType> unresolvedTypes) {
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
                        createImmutableIntersectionType(env, origXmlSubType, immutableXmlSubType, symTable);
                origXmlSubType.immutableType = immutableXmlSubTypeIntersectionType;
                return immutableXmlSubTypeIntersectionType;
            case TypeTags.XML:
                BXMLType origXmlType = (BXMLType) type;

                BTypeSymbol immutableXmlTSymbol = getReadonlyTSymbol(names, origXmlType.tsymbol, env);
                BXMLType immutableXmlType = new BXMLType(getImmutableType(pos, types, origXmlType.constraint, env,
                                                                          symTable, anonymousModelHelper, names,
                                                                          unresolvedTypes),
                                                immutableXmlTSymbol, origXmlType.flags | Flags.READONLY);
                immutableXmlTSymbol.type = immutableXmlType;

                BIntersectionType immutableXmlIntersectionType = createImmutableIntersectionType(env, origXmlType,
                                                                                                 immutableXmlType,
                                                                                                 symTable);
                origXmlType.immutableType = immutableXmlIntersectionType;
                return immutableXmlIntersectionType;
            case TypeTags.ARRAY:
                BArrayType origArrayType = (BArrayType) type;

                BTypeSymbol immutableArrayTSymbol = getReadonlyTSymbol(names, origArrayType.tsymbol, env);
                BArrayType immutableArrayType = new BArrayType(getImmutableType(pos, types,
                                                                                origArrayType.getElementType(), env,
                                                                                symTable, anonymousModelHelper,
                                                                                names, unresolvedTypes),
                                                    immutableArrayTSymbol, origArrayType.size, origArrayType.state,
                                                    origArrayType.flags | Flags.READONLY);
                immutableArrayTSymbol.type = immutableArrayType;

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
                    immutableMemTypes.add(getImmutableType(pos, types, origTupleMemType, env, symTable,
                                                           anonymousModelHelper, names, unresolvedTypes));
                }

                BTypeSymbol immutableTupleTSymbol = getReadonlyTSymbol(names, origTupleType.tsymbol, env);
                BType origRestType = origTupleType.restType;
                BType tupleRestType = origRestType == null ? origRestType :
                        getImmutableType(pos, types, origRestType, env, symTable, anonymousModelHelper, names,
                                         unresolvedTypes);

                BTupleType immutableTupleType = new BTupleType(immutableTupleTSymbol, immutableMemTypes, tupleRestType,
                                                    origTupleType.flags | Flags.READONLY);
                immutableTupleTSymbol.type = immutableTupleType;

                BIntersectionType immutableTupleIntersectionType = createImmutableIntersectionType(env, origTupleType,
                                                                                                   immutableTupleType,
                                                                                                   symTable);
                origTupleType.immutableType = immutableTupleIntersectionType;
                return immutableTupleIntersectionType;
            case TypeTags.MAP:
                BMapType origMapType = (BMapType) type;

                BTypeSymbol immutableMapTSymbol = getReadonlyTSymbol(names, origMapType.tsymbol, env);
                BMapType immutableMapType = new BMapType(origMapType.tag,
                                                         getImmutableType(pos, types, origMapType.constraint, env,
                                                                          symTable, anonymousModelHelper, names,
                                                                          unresolvedTypes),
                                                immutableMapTSymbol, origMapType.flags | Flags.READONLY);
                immutableMapTSymbol.type = immutableMapType;

                BIntersectionType immutableMapIntersectionType = createImmutableIntersectionType(env, origMapType,
                                                                                                 immutableMapType,
                                                                                                 symTable);
                origMapType.immutableType = immutableMapIntersectionType;
                return immutableMapIntersectionType;
            case TypeTags.RECORD:
                BRecordType origRecordType = (BRecordType) type;

                return defineImmutableRecordType(origTypeNode, pos, origRecordType, env, symTable,
                                                 anonymousModelHelper, names, types, unresolvedTypes);
            case TypeTags.TABLE:
                BTableType origTableType = (BTableType) type;

                BTypeSymbol immutableTableTSymbol = getReadonlyTSymbol(names, origTableType.tsymbol, env);
                BTableType immutableTableType = new BTableType(origTableType.tag,
                                                               getImmutableType(pos, types, origTableType.constraint,
                                                                                env, symTable, anonymousModelHelper,
                                                                                names, unresolvedTypes),
                                                immutableTableTSymbol, origTableType.flags | Flags.READONLY);

                BType origKeyTypeConstraint = origTableType.keyTypeConstraint;
                if (origKeyTypeConstraint != null) {
                    immutableTableType.keyTypeConstraint = getImmutableType(pos, types, origKeyTypeConstraint, env,
                                                                            symTable, anonymousModelHelper, names,
                                                                            unresolvedTypes);
                }

                immutableTableType.keyPos = origTableType.keyPos;
                immutableTableType.constraintPos = origTableType.constraintPos;
                immutableTableType.fieldNameList = origTableType.fieldNameList;

                immutableTableTSymbol.type = immutableTableType;

                BIntersectionType immutableTableIntersectionType = createImmutableIntersectionType(env, origTableType,
                                                                                                   immutableTableType,
                                                                                                   symTable);
                origTableType.immutableType = immutableTableIntersectionType;
                return immutableTableIntersectionType;
            case TypeTags.ANY:
                BAnyType origAnyType = (BAnyType) type;

                BTypeSymbol immutableAnyTSymbol = getReadonlyTSymbol(names, origAnyType.tsymbol, env);
                BAnyType immutableAnyType = new BAnyType(origAnyType.tag, immutableAnyTSymbol, immutableAnyTSymbol.name,
                                                         origAnyType.flags | Flags.READONLY, origAnyType.isNullable());
                immutableAnyTSymbol.type = immutableAnyType;

                BIntersectionType immutableAnyIntersectionType = createImmutableIntersectionType(env, origAnyType,
                                                                                                 immutableAnyType,
                                                                                                 symTable);
                origAnyType.immutableType = immutableAnyIntersectionType;
                return immutableAnyIntersectionType;
            case TypeTags.ANYDATA:
                BAnydataType origAnydataType = (BAnydataType) type;

                BTypeSymbol immutableAnydataTSymbol = getReadonlyTSymbol(names, origAnydataType.tsymbol, env);
                BAnydataType immutableAnydataType = new BAnydataType(origAnydataType.tag, immutableAnydataTSymbol,
                                                        immutableAnydataTSymbol.name,
                                                        origAnydataType.flags | Flags.READONLY,
                                                        origAnydataType.isNullable());
                immutableAnydataTSymbol.type = immutableAnydataType;

                BIntersectionType immutableAnydataIntersectionType =
                        createImmutableIntersectionType(env, origAnydataType, immutableAnydataType, symTable);
                origAnydataType.immutableType = immutableAnydataIntersectionType;
                return immutableAnydataIntersectionType;
            case TypeTags.JSON:
                BJSONType origJsonType = (BJSONType) type;

                BTypeSymbol immutableJsonTSymbol = getReadonlyTSymbol(names, origJsonType.tsymbol, env);
                BJSONType immutableJsonType = new BJSONType(origJsonType.tag, immutableJsonTSymbol,
                                                            origJsonType.isNullable(),
                                                            origJsonType.flags | Flags.READONLY);
                immutableJsonTSymbol.type = immutableJsonType;

                BIntersectionType immutableJsonIntersectionType = createImmutableIntersectionType(env, origJsonType,
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

                    readOnlyMemTypes.add(getImmutableType(pos, types, memberType, env, symTable, anonymousModelHelper,
                                                          names, unresolvedTypes));
                }

                if (readOnlyMemTypes.size() == 1) {
                    immutableType = readOnlyMemTypes.iterator().next();
                } else if (origUnionType.tsymbol != null) {
                    BTypeSymbol immutableUnionTSymbol = getReadonlyTSymbol(names, origUnionType.tsymbol, env);
                    immutableType = BUnionType.create(immutableUnionTSymbol, readOnlyMemTypes);
                    immutableType.flags |= (origUnionType.flags | Flags.READONLY);
                    immutableUnionTSymbol.type = immutableType;
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

    public static void defineUndefinedImmutableRecordFields(BLangTypeDefinition immutableTypeDefinition,
                                                            Types types,
                                                            SymbolEnv pkgEnv, SymbolTable symTable,
                                                            BLangAnonymousModelHelper anonymousModelHelper,
                                                            Names names) {
        BRecordType immutableRecordType = (BRecordType) immutableTypeDefinition.type;
        BRecordType origRecordType = immutableRecordType.mutableType;
        DiagnosticPos pos = immutableTypeDefinition.pos;
        SymbolEnv env = SymbolEnv.createTypeEnv(immutableTypeDefinition.typeNode, immutableTypeDefinition.symbol.scope,
                                                pkgEnv);
        PackageID pkgID = env.enclPkg.symbol.pkgID;

        if (origRecordType.fields.size() != immutableRecordType.fields.size()) {

            populateImmutableRecordFields(null, types, symTable, anonymousModelHelper, names,
                                          (BLangRecordTypeNode) immutableTypeDefinition.typeNode, immutableRecordType,
                                          origRecordType,
                                          pos, env, pkgID, new HashSet<>());
        }

        BType currentRestFieldType = immutableRecordType.restFieldType;
        if (currentRestFieldType != null && currentRestFieldType != symTable.noType) {
            return;
        }

        setRestType(types, symTable, anonymousModelHelper, names, immutableRecordType, origRecordType, pos, env,
                    new HashSet<>());
    }

    private static void populateImmutableRecordFields(BLangType origBLangType, Types types,
                                                      SymbolTable symTable,
                                                      BLangAnonymousModelHelper anonymousModelHelper,
                                                      Names names,
                                                      BLangRecordTypeNode immutableRecordTypeNode,
                                                      BRecordType immutableRecordType,
                                                      BRecordType origRecordType, DiagnosticPos pos, SymbolEnv env,
                                                      PackageID pkgID, Set<BType> unresolvedTypes) {
        BTypeSymbol immutableRecordSymbol = immutableRecordType.tsymbol;
        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
        for (BField origField : origRecordType.fields.values()) {
            BType immutableFieldType = getImmutableType(pos, types, origField.type, env, symTable,
                                                        anonymousModelHelper, names, unresolvedTypes);

            Name origFieldName = origField.name;
            BVarSymbol immutableFieldSymbol = new BVarSymbol(origField.symbol.flags | Flags.READONLY,
                                                             origFieldName, pkgID, immutableFieldType,
                                                             immutableRecordSymbol);
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
            immutableRecordSymbol.scope.define(origFieldName, immutableFieldSymbol);
        }
        immutableRecordType.fields = fields;

        if (origBLangType != null) {
            immutableRecordTypeNode.typeRefs.add(origBLangType);
        }
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
        immutableRecordType.restFieldType = getImmutableType(pos, types, origRestFieldType, env, symTable,
                                                             anonymousModelHelper, names, unresolvedTypes);
    }

    private static BIntersectionType defineImmutableRecordType(BLangType origTypeNode, DiagnosticPos pos,
                                                               BRecordType origRecordType, SymbolEnv env,
                                                               SymbolTable symTable,
                                                               BLangAnonymousModelHelper anonymousModelHelper,
                                                               Names names, Types types, Set<BType> unresolvedTypes) {
        PackageID pkgID = env.enclPkg.symbol.pkgID;
        BRecordTypeSymbol recordSymbol =
                Symbols.createRecordSymbol(origRecordType.tsymbol.flags | Flags.READONLY,
                                           getImmutableTypeName(names, origRecordType.tsymbol),
                                           pkgID, null, env.scope.owner);

        BInvokableType bInvokableType = new BInvokableType(new ArrayList<>(), symTable.nilType, null);
        BInvokableSymbol initFuncSymbol = Symbols.createFunctionSymbol(
                Flags.PUBLIC, Names.EMPTY, env.enclPkg.symbol.pkgID, bInvokableType, env.scope.owner, false);
        initFuncSymbol.retType = symTable.nilType;
        recordSymbol.initializerFunc = new BAttachedFunction(Names.INIT_FUNCTION_SUFFIX, initFuncSymbol,
                                                             bInvokableType);

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

        populateImmutableRecordFields(origTypeNode, types, symTable, anonymousModelHelper, names,
                                      recordTypeNode, immutableRecordType, origRecordType, pos, env,
                                      pkgID, unresolvedTypes);

        setRestType(types, symTable, anonymousModelHelper, names, immutableRecordType, origRecordType, pos, env,
                    unresolvedTypes);

        recordTypeNode.initFunction = TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, env, names,
                                                                                           symTable);
        TypeDefBuilderHelper.addTypeDefinition(immutableRecordType, recordSymbol, recordTypeNode, env);

        return immutableRecordIntersectionType;
    }

    private static BTypeSymbol getReadonlyTSymbol(Names names, BTypeSymbol originalTSymbol, SymbolEnv env) {
        return Symbols.createTypeSymbol(originalTSymbol.tag, originalTSymbol.flags | Flags.READONLY,
                                        getImmutableTypeName(names, originalTSymbol), env.enclPkg.symbol.pkgID, null,
                                        env.scope.owner);
    }

    private static Name getImmutableTypeName(Names names, BTypeSymbol originalTSymbol) {
        return names.fromString(originalTSymbol.name.getValue().concat(AND_READONLY_SUFFIX));
    }

    private static BIntersectionType createImmutableIntersectionType(SymbolEnv env, BType nonReadOnlyType,
                                                                     BType effectiveType, SymbolTable symTable) {
        BTypeSymbol intersectionTypeSymbol = Symbols.createTypeSymbol(SymTag.INTERSECTION_TYPE,
                                                                      Flags.asMask(EnumSet.of(Flag.PUBLIC,
                                                                                              Flag.READONLY)),
                                                                      Names.EMPTY, env.enclPkg.symbol.pkgID,
                                                                      null, env.scope.owner);

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

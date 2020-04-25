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

import org.ballerinalang.model.elements.PackageID;
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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Helper class to create a clone of it.
 *
 * @since 1.3.0
 */
public class ImmutableTypeCloner {

    public static BType setImmutableType(DiagnosticPos pos, Types types, BType type, SymbolEnv env,
                                         SymbolTable symTable, BLangAnonymousModelHelper anonymousModelHelper,
                                         Names names) {
        if (types.isReadonlyType(type)) {
            return type;
        }

        switch (type.tag) {
            case TypeTags.XML:
                // TODO: 4/24/20 Handle XML subtypes.
                BXMLType origXmlType = (BXMLType) type;
                BXMLType.BImmutableXmlType immutableXmlType = origXmlType.immutableType;
                if (immutableXmlType != null) {
                    return immutableXmlType;
                }

                BTypeSymbol immutableXmlTSymbol = getReadonlyTSymbol(names, origXmlType.tsymbol, env);
                immutableXmlType = new BXMLType.BImmutableXmlType(setImmutableType(pos, types, origXmlType.constraint,
                                                                                   env, symTable,
                                                                                   anonymousModelHelper, names),
                                                                  immutableXmlTSymbol, origXmlType.flags);
                immutableXmlTSymbol.type = immutableXmlType;
                origXmlType.immutableType = immutableXmlType;
                return immutableXmlType;
            case TypeTags.ARRAY:
                BArrayType origArrayType = (BArrayType) type;
                BArrayType.BImmutableArrayType immutableArrayType = origArrayType.immutableType;
                if (immutableArrayType != null) {
                    return immutableArrayType;
                }

                BTypeSymbol immutableArrayTSymbol = getReadonlyTSymbol(names, origArrayType.tsymbol, env);
                immutableArrayType = new BArrayType.BImmutableArrayType(setImmutableType(pos, types,
                                                                                         origArrayType.getElementType(),
                                                                                         env, symTable,
                                                                                         anonymousModelHelper, names),
                                                                        immutableArrayTSymbol, origArrayType.size,
                                                                        origArrayType.state, origArrayType.flags);
                immutableArrayTSymbol.type = immutableArrayType;
                origArrayType.immutableType = immutableArrayType;
                return immutableArrayType;
            case TypeTags.TUPLE:
                BTupleType origTupleType = (BTupleType) type;
                BTupleType.BImmutableTupleType immutableTupleType = origTupleType.immutableType;
                if (immutableTupleType != null) {
                    return immutableTupleType;
                }

                List<BType> origTupleMemTypes = origTupleType.tupleTypes;
                List<BType> immutableMemTypes = new ArrayList<>(origTupleMemTypes.size());

                for (BType origTupleMemType : origTupleMemTypes) {
                    immutableMemTypes.add(setImmutableType(pos, types, origTupleMemType, env, symTable,
                                                           anonymousModelHelper, names));
                }

                BTypeSymbol immutableTupleTSymbol = getReadonlyTSymbol(names, origTupleType.tsymbol, env);
                immutableTupleType = new BTupleType.BImmutableTupleType(immutableTupleTSymbol, immutableMemTypes,
                                                                        origTupleType.restType, origTupleType.flags);
                immutableTupleTSymbol.type = immutableTupleType;
                origTupleType.immutableType = immutableTupleType;
                return immutableTupleType;
            case TypeTags.MAP:
                BMapType origMapType = (BMapType) type;
                BMapType.BImmutableMapType immutableMapType = origMapType.immutableType;
                if (immutableMapType != null) {
                    return immutableMapType;
                }

                BTypeSymbol immutableMapTSymbol = getReadonlyTSymbol(names, origMapType.tsymbol, env);
                immutableMapType = new BMapType.BImmutableMapType(origMapType.tag,
                                                                  setImmutableType(pos, types, origMapType.constraint,
                                                                                   env, symTable, anonymousModelHelper,
                                                                                   names), immutableMapTSymbol,
                                                                  origMapType.flags);
                immutableMapTSymbol.type = immutableMapType;
                origMapType.immutableType = immutableMapType;
                return immutableMapType;
            case TypeTags.RECORD:
                BRecordType origRecordType = (BRecordType) type;
                BRecordType.BImmutableRecordType immutableRecordType = origRecordType.immutableType;
                if (immutableRecordType != null) {
                    return immutableRecordType;
                }

                immutableRecordType = defineImmutableRecordType(pos, origRecordType, env, symTable,
                                                                anonymousModelHelper, names, types);
                origRecordType.immutableType = immutableRecordType;
                return immutableRecordType;
            // TODO: 4/24/20 Table
            case TypeTags.ANY:
                BAnyType origAnyType = (BAnyType) type;
                BAnyType.BImmutableAnyType immutableAnyType = origAnyType.immutableType;
                if (immutableAnyType != null) {
                    return immutableAnyType;
                }

                BTypeSymbol immutableAnyTSymbol = getReadonlyTSymbol(names, origAnyType.tsymbol, env);
                immutableAnyType = new BAnyType.BImmutableAnyType(origAnyType.tag, immutableAnyTSymbol,
                                                                  immutableAnyTSymbol.name,
                                                                  origAnyType.flags, origAnyType.isNullable());
                immutableAnyTSymbol.type = immutableAnyType;
                origAnyType.immutableType = immutableAnyType;
                return immutableAnyType;
            case TypeTags.ANYDATA:
                BAnydataType origAnydataType = (BAnydataType) type;
                BAnydataType.BImmutableAnydataType immutableAnydataType = origAnydataType.immutableType;
                if (immutableAnydataType != null) {
                    return immutableAnydataType;
                }

                BTypeSymbol immutableAnydataTSymbol = getReadonlyTSymbol(names, origAnydataType.tsymbol, env);
                immutableAnydataType = new BAnydataType.BImmutableAnydataType(origAnydataType.tag,
                                                                              immutableAnydataTSymbol,
                                                                              immutableAnydataTSymbol.name,
                                                                              origAnydataType.flags,
                                                                              origAnydataType.isNullable());
                immutableAnydataTSymbol.type = immutableAnydataType;
                origAnydataType.immutableType = immutableAnydataType;
                return immutableAnydataType;
            case TypeTags.JSON:
                BJSONType origJsonType = (BJSONType) type;
                BJSONType.BImmutableJsonType immutableJsonType = origJsonType.immutableType;
                if (immutableJsonType != null) {
                    return immutableJsonType;
                }

                BTypeSymbol immutableJsonTSymbol = getReadonlyTSymbol(names, origJsonType.tsymbol, env);
                immutableJsonType = new BJSONType.BImmutableJsonType(origJsonType.tag, immutableJsonTSymbol,
                                                                     origJsonType.isNullable(), origJsonType.flags);
                immutableJsonTSymbol.type = immutableJsonType;
                origJsonType.immutableType = immutableJsonType;
                return immutableJsonType;
            default:
                BUnionType origUnionType = (BUnionType) type;
                BType immutableType = origUnionType.immutableType;
                if (immutableType != null) {
                    return immutableType;
                }

                LinkedHashSet<BType> readOnlyMemTypes = new LinkedHashSet<>();
                boolean hasNilableMem = false;

                for (BType memberType : origUnionType.getMemberTypes()) {
                    if (types.isReadonlyType(memberType)) {
                        readOnlyMemTypes.add(memberType);
                    }

                    if (!types.isSelectivelyImmutableType(memberType)) {
                        continue;
                    }

                    if (!hasNilableMem && memberType.isNullable()) {
                        hasNilableMem = true;
                    }

                    readOnlyMemTypes.add(setImmutableType(pos, types, memberType, env, symTable, anonymousModelHelper,
                                                          names));
                }

                if (readOnlyMemTypes.size() == 1) {
                    immutableType = readOnlyMemTypes.iterator().next();
                } else {
                    BTypeSymbol immutableUnionTSymbol = getReadonlyTSymbol(names, origUnionType.tsymbol, env);
                    immutableType = new BUnionType.BImmutableUnionType(immutableUnionTSymbol, readOnlyMemTypes,
                                                                       hasNilableMem);
                    immutableUnionTSymbol.type = immutableType;
                }
                origUnionType.immutableType = immutableType;
                return immutableType;
        }
    }

    private static BRecordType.BImmutableRecordType defineImmutableRecordType(DiagnosticPos pos,
                                                                              BRecordType origRecordType,
                                                                              SymbolEnv env,
                                                                              SymbolTable symTable,
                                                                              BLangAnonymousModelHelper
                                                                                      anonymousModelHelper,
                                                                              Names names,
                                                                              Types types) {
        PackageID pkgID = env.enclPkg.symbol.pkgID;
        BRecordTypeSymbol recordSymbol =
                Symbols.createRecordSymbol(origRecordType.tsymbol.flags |= Flags.READONLY,
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

        List<BField> fields = new ArrayList<>();

        for (BField origField : origRecordType.fields) {
            BType immutableFieldType = setImmutableType(pos, types, origField.type, env, symTable,
                                                        anonymousModelHelper, names);

            Name origFieldName = origField.name;
            BVarSymbol immutableFieldSymbol = new BVarSymbol(origField.symbol.flags |= Flags.READONLY, origFieldName,
                                                             pkgID, immutableFieldType, recordSymbol);
            if (immutableFieldType.tag == TypeTags.INVOKABLE && immutableFieldType.tsymbol != null) {
                BInvokableTypeSymbol tsymbol = (BInvokableTypeSymbol) immutableFieldType.tsymbol;
                BInvokableSymbol invokableSymbol = (BInvokableSymbol) immutableFieldSymbol;
                invokableSymbol.params = tsymbol.params;
                invokableSymbol.restParam = tsymbol.restParam;
                invokableSymbol.retType = tsymbol.returnType;
                invokableSymbol.flags = tsymbol.flags;
            }
            fields.add(new BField(origFieldName, null, immutableFieldSymbol));
            recordSymbol.scope.define(origFieldName, immutableFieldSymbol);
        }

        BRecordType.BImmutableRecordType immutableRecordType =
                new BRecordType.BImmutableRecordType(recordSymbol, fields, origRecordType.flags);

        immutableRecordType.sealed = origRecordType.sealed;
        immutableRecordType.restFieldType = origRecordType.restFieldType;

        recordSymbol.type = immutableRecordType;
        immutableRecordType.tsymbol = recordSymbol;

        BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(immutableRecordType, pkgID,
                                                                                       symTable, pos);
        recordTypeNode.initFunction = TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, env, names,
                                                                                           symTable);
        TypeDefBuilderHelper.addTypeDefinition(immutableRecordType, recordSymbol, recordTypeNode, env);

        return immutableRecordType;
    }

    private static BTypeSymbol getReadonlyTSymbol(Names names, BTypeSymbol originalTSymbol, SymbolEnv env) {
        return Symbols.createTypeSymbol(originalTSymbol.tag, originalTSymbol.flags |= Flags.READONLY,
                                        getImmutableTypeName(names, originalTSymbol), env.enclPkg.symbol.pkgID, null,
                                        env.scope.owner);
    }

    private static Name getImmutableTypeName(Names names, BTypeSymbol originalTSymbol) {
        return names.fromString(originalTSymbol.name.getValue().concat(" & readonly"));
    }
}

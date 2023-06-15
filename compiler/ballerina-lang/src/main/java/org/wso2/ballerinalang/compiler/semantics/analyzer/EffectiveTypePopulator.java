/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeDefBuilderHelper;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;

/**
 * Update the effective type of cyclic intersections.
 *
 * @since 2201.7.0
 */
public class EffectiveTypePopulator implements TypeVisitor {

    private static final CompilerContext.Key<EffectiveTypePopulator> UPDATE_IMMUTABLE_TYPE_KEY =
                new CompilerContext.Key<>();
    private final SymbolTable symTable;
    private final Names names;
    private final Types types;
    private final BLangAnonymousModelHelper anonymousModelHelper;
    private Location loc;
    private PackageID pkgID;
    private BLangNode typeNode;
    private SymbolEnv env;
    public HashSet<BType> visitedImmutableTypes = new HashSet<>();

    public EffectiveTypePopulator(CompilerContext context) {
        context.put(UPDATE_IMMUTABLE_TYPE_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
    }

    public static EffectiveTypePopulator getInstance(CompilerContext context) {
        EffectiveTypePopulator effectiveTypePopulator = context.get(UPDATE_IMMUTABLE_TYPE_KEY);
        if (effectiveTypePopulator == null) {
            effectiveTypePopulator = new EffectiveTypePopulator(context);
        }
        return effectiveTypePopulator;
    }

    public void updateType(BType immutableType, Location loc, PackageID pkgID, BLangNode typeNode,
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
                bArrayType.eType = ImmutableTypeCloner.getImmutableType(loc, types, origArrayType.eType, env,
                        pkgID, env.scope.owner, symTable, anonymousModelHelper, names, new HashSet<>());
            }
            updateType(bArrayType.eType, loc, pkgID, typeNode, env);
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
                bMapType.constraint = ImmutableTypeCloner.getImmutableType(loc, types, origMapType.constraint, env,
                        pkgID, env.scope.owner, symTable, anonymousModelHelper, names,
                        new HashSet<>());
            }
            updateType(bMapType.constraint, loc, pkgID, typeNode, env);
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
        updateType(Types.getReferredType(bTypeReferenceType), loc, pkgID, typeNode, env);
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
                    BType newType = ImmutableTypeCloner.getImmutableType(loc, types, origTupleMemType.type, env,
                            pkgID, env.scope.owner, symTable, anonymousModelHelper, names, new HashSet<>());
                    BVarSymbol varSymbol = Symbols.createVarSymbolForTupleMember(newType);
                    BTupleMember member = new BTupleMember(newType, varSymbol);
                    bTupleType.addMembers(member);
                }
            } else {
                for (BType memberType : bTupleType.getTupleTypes()) {
                    updateType(memberType, loc, pkgID, typeNode, env);
                }
            }

            BTypeSymbol tsymbol = bTupleType.tsymbol;
            if (tsymbol != null && tsymbol.name != null && !tsymbol.name.value.isEmpty()
                    && !Symbols.isFlagOn(bTupleType.flags, Flags.EFFECTIVE_TYPE_DEF)) {
                BLangTupleTypeNode tupleTypeNode = (BLangTupleTypeNode) TreeBuilder.createTupleTypeNode();
                tupleTypeNode.setBType(bTupleType);
                BLangTypeDefinition typeDefinition = TypeDefBuilderHelper.addTypeDefinition(bTupleType,
                        bTupleType.tsymbol, tupleTypeNode, env);
                typeDefinition.pos = loc;
            }
            bTupleType.mutableType = null;
        }
    }

    @Override
    public void visit(BUnionType bUnionType) {
        for (BType immutableMemberType : bUnionType.getMemberTypes()) {
            updateType(immutableMemberType, loc, pkgID, typeNode, env);
        }
    }

    @Override
    public void visit(BIntersectionType bIntersectionType) {
        updateType(bIntersectionType.effectiveType, loc, pkgID, typeNode, env);
    }

    @Override
    public void visit(BXMLType bXMLType) {
        BXMLType origXMLType = bXMLType.mutableType;
        if (origXMLType != null) {
            if (bXMLType.constraint == symTable.semanticError) {
                bXMLType.constraint = ImmutableTypeCloner.getImmutableType(loc, types, origXMLType.constraint, env,
                        pkgID, env.scope.owner, symTable, anonymousModelHelper, names, new HashSet<>());
            }
            updateType(bXMLType.constraint, loc, pkgID, typeNode, env);
            bXMLType.mutableType = null;
        }
    }

    @Override
    public void visit(BTableType bTableType) {
        BTableType origTableType = bTableType.mutableType;
        if (origTableType != null) {
            if (bTableType.constraint.tag == TypeTags.NEVER ||
                    bTableType.constraint == symTable.semanticError) {
                bTableType.constraint = ImmutableTypeCloner.getImmutableType(loc, types, origTableType.constraint, env,
                        pkgID, env.scope.owner, symTable, anonymousModelHelper, names, new HashSet<>());
            }
            updateType(bTableType.constraint, loc, pkgID, typeNode, env);

            if (origTableType.keyTypeConstraint != null) {
                bTableType.keyTypeConstraint = ImmutableTypeCloner.getImmutableType(loc, types,
                        origTableType.keyTypeConstraint, env, pkgID, env.scope.owner, symTable,
                        anonymousModelHelper, names, new HashSet<>());
                updateType(bTableType.keyTypeConstraint, loc, pkgID, typeNode, env);
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
                            pkgID, env.scope.owner, symTable, anonymousModelHelper, names,
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
                                pkgID, env.scope.owner, symTable, anonymousModelHelper, names,
                                new HashSet<>());
                    }
                    Name origFieldName = origField.name;
                    updateType(immutableField.type, loc, pkgID, typeNode, env);
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
                            pkgID, env.scope.owner, symTable, anonymousModelHelper, names,
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
                                pkgID, env.scope.owner, symTable, anonymousModelHelper, names,
                                new HashSet<>());
                    }
                    Name origFieldName = origField.name;
                    updateType(immutableField.type, loc, pkgID, typeNode, env);
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

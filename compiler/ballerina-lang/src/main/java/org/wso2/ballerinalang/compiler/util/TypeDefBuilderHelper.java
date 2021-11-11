/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.util;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createIdentifier;

/**
 * Helper class with util methods to create type definitions.
 *
 * @since 1.2.0
 */
public class TypeDefBuilderHelper {

    public static final String INTERSECTED_ERROR_DETAIL = "$IntersectedErrorDetail$";

    public static BLangRecordTypeNode createRecordTypeNode(BRecordType recordType, PackageID packageID,
                                                           SymbolTable symTable, Location pos) {
        List<BLangSimpleVariable> fieldList = new ArrayList<>();
        for (BField field : recordType.fields.values()) {
            BVarSymbol symbol = field.symbol;
            if (symbol == null) {
                symbol = new BVarSymbol(Flags.PUBLIC, field.name, packageID, symTable.pureType, null, field.pos,
                                        VIRTUAL);
            }

            BLangSimpleVariable fieldVar = ASTBuilderUtil.createVariable(field.pos, symbol.name.value, field.type,
                                                                         null, symbol);
            fieldList.add(fieldVar);
        }
        return createRecordTypeNode(fieldList, recordType, pos);
    }

    public static BLangObjectTypeNode createObjectTypeNode(BObjectType objectType, Location pos) {
        List<BLangSimpleVariable> fieldList = new ArrayList<>();
        for (BField field : objectType.fields.values()) {
            BVarSymbol symbol = field.symbol;
            BLangSimpleVariable fieldVar = ASTBuilderUtil.createVariable(field.pos, symbol.name.value, field.type,
                                                                         null, symbol);
            fieldList.add(fieldVar);
        }
        return createObjectTypeNode(fieldList, objectType, pos);
    }

    public static BLangRecordTypeNode createRecordTypeNode(List<BLangSimpleVariable> typeDefFields,
                                                           BRecordType recordType, Location pos) {
        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) TreeBuilder.createRecordTypeNode();
        recordTypeNode.setBType(recordType);
        recordTypeNode.fields = typeDefFields;
        recordTypeNode.symbol = recordType.tsymbol;
        recordTypeNode.pos = pos;

        return recordTypeNode;
    }

    public static BLangObjectTypeNode createObjectTypeNode(List<BLangSimpleVariable> typeDefFields,
                                                           BObjectType objectType, Location pos) {
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) TreeBuilder.createObjectTypeNode();
        objectTypeNode.setBType(objectType);
        objectTypeNode.fields = typeDefFields;
        objectTypeNode.symbol = objectType.tsymbol;
        objectTypeNode.pos = pos;

        return objectTypeNode;
    }

    public static BLangFunction createInitFunctionForRecordType(BLangRecordTypeNode recordTypeNode, SymbolEnv env,
                                                                Names names, SymbolTable symTable) {
        BLangFunction initFunction = createInitFunctionForStructureType(recordTypeNode.pos, recordTypeNode.symbol, env,
                                                                        names, Names.INIT_FUNCTION_SUFFIX, symTable,
                                                                        recordTypeNode.getBType());
        BStructureTypeSymbol structureSymbol = ((BStructureTypeSymbol) recordTypeNode.getBType().tsymbol);
        structureSymbol.initializerFunc = new BAttachedFunction(initFunction.symbol.name, initFunction.symbol,
                                                                (BInvokableType) initFunction.getBType(),
                                                                initFunction.pos);
        recordTypeNode.initFunction = initFunction;
        structureSymbol.scope.define(structureSymbol.initializerFunc.symbol.name,
                                     structureSymbol.initializerFunc.symbol);
        return initFunction;
    }

    public static BLangFunction createInitFunctionForStructureType(Location location,
                                                                   BSymbol symbol,
                                                                   SymbolEnv env,
                                                                   Names names,
                                                                   Name suffix,
                                                                   SymbolTable symTable,
                                                                   BType type) {
        return createInitFunctionForStructureType(location, symbol, env, names, suffix, type, symTable.nilType);
    }

    public static BLangFunction createInitFunctionForStructureType(Location location,
                                                                   BSymbol symbol,
                                                                   SymbolEnv env,
                                                                   Names names,
                                                                   Name suffix,
                                                                   BType type,
                                                                   BType returnType) {
        String structTypeName = type.tsymbol.name.value;
        BLangFunction initFunction = ASTBuilderUtil
                .createInitFunctionWithNilReturn(location, structTypeName, suffix);

        // Create the receiver and add receiver details to the node
        initFunction.receiver = ASTBuilderUtil.createReceiver(location, type);
        BVarSymbol receiverSymbol = new BVarSymbol(Flags.asMask(EnumSet.noneOf(Flag.class)),
                                                   names.fromIdNode(initFunction.receiver.name),
                                                   names.originalNameFromIdNode(initFunction.receiver.name),
                                                   env.enclPkg.symbol.pkgID, type, null, location, VIRTUAL);
        initFunction.receiver.symbol = receiverSymbol;
        initFunction.attachedFunction = true;
        initFunction.flagSet.add(Flag.ATTACHED);

        // Create the function type
        initFunction.setBType(new BInvokableType(new ArrayList<>(), returnType, null));

        // Create the function symbol
        Name funcSymbolName = names.fromString(Symbols.getAttachedFuncSymbolName(structTypeName, suffix.value));
        initFunction.symbol = Symbols
                .createFunctionSymbol(Flags.asMask(initFunction.flagSet), funcSymbolName, funcSymbolName,
                                      env.enclPkg.symbol.pkgID, initFunction.getBType(), symbol,
                                      initFunction.body != null, initFunction.pos, VIRTUAL);
        initFunction.symbol.scope = new Scope(initFunction.symbol);
        initFunction.symbol.scope.define(receiverSymbol.name, receiverSymbol);
        initFunction.symbol.receiverSymbol = receiverSymbol;
        initFunction.name = createIdentifier(location, funcSymbolName.value);

        // Create the function type symbol
        BInvokableTypeSymbol tsymbol = Symbols.createInvokableTypeSymbol(SymTag.FUNCTION_TYPE,
                                                                         initFunction.symbol.flags,
                                                                         env.enclPkg.packageID, initFunction.getBType(),
                                                                         initFunction.symbol, initFunction.pos,
                                                                         VIRTUAL);
        tsymbol.params = initFunction.symbol.params;
        tsymbol.restParam = initFunction.symbol.restParam;
        tsymbol.returnType = initFunction.symbol.retType;
        initFunction.getBType().tsymbol = tsymbol;

        receiverSymbol.owner = initFunction.symbol;

        // Add return type to the symbol
        initFunction.symbol.retType = returnType;

        return initFunction;
    }

    public static BLangTypeDefinition addTypeDefinition(BType type, BTypeSymbol symbol, BLangType typeNode,
                                                        SymbolEnv env) {
        BLangTypeDefinition typeDefinition = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        typeDefinition.typeNode = typeNode;
        typeDefinition.setBType(type);
        typeDefinition.symbol = symbol;
        typeDefinition.name = createIdentifier(symbol.pos, symbol.name.value);
        typeDefinition.pos = typeNode.getPosition();
        env.enclPkg.addTypeDefinition(typeDefinition);
        return typeDefinition;
    }

    public static BLangClassDefinition createClassDef(Location pos, BObjectTypeSymbol classTSymbol,
                                                      SymbolEnv env) {
        BObjectType objType = (BObjectType) classTSymbol.type;
        List<BLangSimpleVariable> fieldList = new ArrayList<>();
        for (BField field : objType.fields.values()) {
            BVarSymbol symbol = field.symbol;
            BLangSimpleVariable fieldVar = ASTBuilderUtil.createVariable(field.pos, symbol.name.value, field.type,
                                                                         null, symbol);
            fieldList.add(fieldVar);
        }

        BLangClassDefinition classDefNode = (BLangClassDefinition) TreeBuilder.createClassDefNode();
        classDefNode.setBType(objType);
        classDefNode.fields = fieldList;
        classDefNode.symbol = classTSymbol;
        classDefNode.pos = pos;

        env.enclPkg.addClassDefinition(classDefNode);

        return classDefNode;
    }

    public static BLangErrorType createBLangErrorType(Location pos, BErrorType type, SymbolEnv env,
                                                      BLangAnonymousModelHelper anonymousModelHelper) {
        BLangErrorType errorType = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        errorType.setBType(type);

        BLangUserDefinedType userDefinedTypeNode = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedTypeNode.pos = pos;
        userDefinedTypeNode.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();

        BType detailType = type.detailType;

        if (detailType.tag == TypeTags.MAP) {
            BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
            refType.typeKind = TypeKind.MAP;
            refType.pos = pos;

            BLangConstrainedType constrainedType = (BLangConstrainedType) TreeBuilder.createConstrainedTypeNode();
            constrainedType.constraint = userDefinedTypeNode; // We need to catch this and override the type-resolving
            userDefinedTypeNode.typeName = createIdentifier(pos, INTERSECTED_ERROR_DETAIL);
            constrainedType.type = refType;
            constrainedType.pos = pos;

            errorType.detailType = constrainedType;
            return errorType;
        }

        String typeName = detailType.tsymbol != null
                ? detailType.tsymbol.name.value
                : anonymousModelHelper.getNextAnonymousIntersectionErrorDetailTypeName(env.enclPkg.packageID);

        userDefinedTypeNode.typeName = createIdentifier(pos, typeName);
        userDefinedTypeNode.setBType(detailType);
        errorType.detailType = userDefinedTypeNode;

        return errorType;
    }

    public static String getPackageAlias(SymbolEnv env, String compUnitName, PackageID typePkgId) {
        for (BLangImportPackage importStmt : env.enclPkg.imports) {
            if (!importStmt.compUnit.value.equals(compUnitName)) {
                continue;
            }

            if (importStmt.symbol != null && typePkgId.equals(importStmt.symbol.pkgID)) {
                return importStmt.alias.value;
            }
        }

        return ""; // current module
    }
}

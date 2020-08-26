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

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TaintAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.TaintRecord;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * Helper class with util methods to create type definitions.
 *
 * @since 1.2.0
 */
public class TypeDefBuilderHelper {

    public static BLangRecordTypeNode createRecordTypeNode(BRecordType recordType, PackageID packageID,
                                                           SymbolTable symTable, DiagnosticPos pos) {
        List<BLangSimpleVariable> fieldList = new ArrayList<>();
        for (BField field : recordType.fields.values()) {
            BVarSymbol symbol = field.symbol;
            if (symbol == null) {
                symbol = new BVarSymbol(Flags.PUBLIC, field.name, packageID, symTable.pureType, null, field.pos, );
            }

            BLangSimpleVariable fieldVar = ASTBuilderUtil.createVariable(field.pos, symbol.name.value, field.type,
                                                                         null, symbol);
            fieldList.add(fieldVar);
        }
        return createRecordTypeNode(fieldList, recordType, pos);
    }

    public static BLangObjectTypeNode createObjectTypeNode(BObjectType objectType, DiagnosticPos pos) {
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
                                                           BRecordType recordType, DiagnosticPos pos) {
        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) TreeBuilder.createRecordTypeNode();
        recordTypeNode.type = recordType;
        recordTypeNode.fields = typeDefFields;
        recordTypeNode.symbol = recordType.tsymbol;
        recordTypeNode.pos = pos;

        return recordTypeNode;
    }

    public static BLangObjectTypeNode createObjectTypeNode(List<BLangSimpleVariable> typeDefFields,
                                                           BObjectType objectType, DiagnosticPos pos) {
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) TreeBuilder.createObjectTypeNode();
        objectTypeNode.type = objectType;
        objectTypeNode.fields = typeDefFields;
        objectTypeNode.symbol = objectType.tsymbol;
        objectTypeNode.pos = pos;

        return objectTypeNode;
    }

    public static BLangFunction createInitFunctionForRecordType(BLangRecordTypeNode recordTypeNode, SymbolEnv env,
                                                                Names names, SymbolTable symTable) {
        BLangFunction initFunction = createInitFunctionForStructureType(recordTypeNode, env,
                                                                        Names.INIT_FUNCTION_SUFFIX, names, symTable);
        BStructureTypeSymbol structureSymbol = ((BStructureTypeSymbol) recordTypeNode.type.tsymbol);
        structureSymbol.initializerFunc = new BAttachedFunction(initFunction.symbol.name, initFunction.symbol,
                                                                (BInvokableType) initFunction.type, initFunction.pos);
        recordTypeNode.initFunction = initFunction;
        structureSymbol.scope.define(structureSymbol.initializerFunc.symbol.name,
                                     structureSymbol.initializerFunc.symbol);
        return initFunction;
    }

    public static BLangFunction createInitFunctionForStructureType(BLangStructureTypeNode structureTypeNode,
                                                                   SymbolEnv env, Name suffix, Names names,
                                                                   SymbolTable symTable) {
        String structTypeName = structureTypeNode.type.tsymbol.name.value;
        BLangFunction initFunction = ASTBuilderUtil
                .createInitFunctionWithNilReturn(structureTypeNode.pos, structTypeName, suffix);

        // Create the receiver and add receiver details to the node
        initFunction.receiver = ASTBuilderUtil.createReceiver(structureTypeNode.pos, structureTypeNode.type);
        BVarSymbol receiverSymbol = new BVarSymbol(Flags.asMask(EnumSet.noneOf(Flag.class)),
                                                   names.fromIdNode(initFunction.receiver.name),
                                                   env.enclPkg.symbol.pkgID, structureTypeNode.type, null,
                                                   structureTypeNode.pos, );
        initFunction.receiver.symbol = receiverSymbol;
        initFunction.attachedFunction = true;
        initFunction.flagSet.add(Flag.ATTACHED);

        // Create the function type
        initFunction.type = new BInvokableType(new ArrayList<>(), symTable.nilType, null);

        // Create the function symbol
        Name funcSymbolName = names.fromString(Symbols.getAttachedFuncSymbolName(structTypeName, suffix.value));
        initFunction.symbol = Symbols
                .createFunctionSymbol(Flags.asMask(initFunction.flagSet), funcSymbolName, env.enclPkg.symbol.pkgID,
                                      initFunction.type, structureTypeNode.symbol, initFunction.body != null,
                                      initFunction.pos, VIRTUAL);
        initFunction.symbol.scope = new Scope(initFunction.symbol);
        initFunction.symbol.scope.define(receiverSymbol.name, receiverSymbol);
        initFunction.symbol.receiverSymbol = receiverSymbol;
        initFunction.name = ASTBuilderUtil.createIdentifier(structureTypeNode.pos, funcSymbolName.value);

        // Create the function type symbol
        BInvokableTypeSymbol tsymbol = Symbols.createInvokableTypeSymbol(SymTag.FUNCTION_TYPE,
                                                                         initFunction.symbol.flags,
                                                                         env.enclPkg.packageID, initFunction.type,
                                                                         initFunction.symbol, initFunction.pos,
                                                                         VIRTUAL);
        tsymbol.params = initFunction.symbol.params;
        tsymbol.restParam = initFunction.symbol.restParam;
        tsymbol.returnType = initFunction.symbol.retType;
        initFunction.type.tsymbol = tsymbol;

        receiverSymbol.owner = initFunction.symbol;

        // Add return type as nil to the symbol
        initFunction.symbol.retType = symTable.nilType;

        // Set the taint information to the constructed init function
        initFunction.symbol.taintTable = new HashMap<>();
        TaintRecord taintRecord = new TaintRecord(TaintRecord.TaintedStatus.UNTAINTED, new ArrayList<>());
        initFunction.symbol.taintTable.put(TaintAnalyzer.ALL_UNTAINTED_TABLE_ENTRY_INDEX, taintRecord);

        return initFunction;
    }

    public static BLangTypeDefinition addTypeDefinition(BType type, BTypeSymbol symbol, BLangType typeNode,
                                                        SymbolEnv env) {
        BLangTypeDefinition typeDefinition = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        env.enclPkg.addTypeDefinition(typeDefinition);
        typeDefinition.typeNode = typeNode;
        typeDefinition.type = type;
        typeDefinition.symbol = symbol;
        return typeDefinition;
    }
}

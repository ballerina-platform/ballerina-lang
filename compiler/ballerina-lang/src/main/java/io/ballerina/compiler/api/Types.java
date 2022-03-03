/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api;

import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Map;
import java.util.Optional;

/**
* Represents the Types of a semantic model
 *
 * @since 2.0.0
* */

public class Types {

    private static final CompilerContext.Key<Types> TYPES_KEY = new CompilerContext.Key<>();
    private final CompilerContext context;
    private final TypesFactory typesFactory;
    private final SymbolTable symbolTable;

    private final TypeSymbol BOOLEAN;
    private final TypeSymbol INT;
    private final TypeSymbol FLOAT;
    private final TypeSymbol DECIMAL;
    private final TypeSymbol STRING;
    private final TypeSymbol NIL;
    private final TypeSymbol XML;
    private final TypeSymbol ERROR;
//    private final TypeSymbol FUNCTION;
    private final TypeSymbol FUTURE;
    private final TypeSymbol TYPEDESC;
    private final TypeSymbol HANDLE;
    private final TypeSymbol STREAM;
    private final TypeSymbol ANY;
    private final TypeSymbol ANYDATA;
    private final TypeSymbol NEVER;
    private final TypeSymbol READONLY;
    private final TypeSymbol JSON;
    private final TypeSymbol BYTE;
    private final TypeSymbol COMPILATION_ERROR;


    private Types(CompilerContext context) {
        context.put(TYPES_KEY, this);
        this.context = context;
        this.typesFactory = TypesFactory.getInstance(context);
        this.symbolTable = SymbolTable.getInstance(context);

        this.BOOLEAN = typesFactory.getTypeDescriptor(symbolTable.booleanType);
        this.INT = typesFactory.getTypeDescriptor(symbolTable.intType);
        this.FLOAT = typesFactory.getTypeDescriptor(symbolTable.floatType);
        this.DECIMAL = typesFactory.getTypeDescriptor(symbolTable.decimalType);
        this.STRING = typesFactory.getTypeDescriptor(symbolTable.stringType);
        this.NIL = typesFactory.getTypeDescriptor(symbolTable.nilType);
        this.XML = typesFactory.getTypeDescriptor(symbolTable.xmlType);
        this.ERROR = typesFactory.getTypeDescriptor(symbolTable.errorType);
//        this.FUNCTION = typesFactory.getTypeDescriptor(null);
        this.FUTURE = typesFactory.getTypeDescriptor(symbolTable.futureType);
        this.TYPEDESC = typesFactory.getTypeDescriptor(symbolTable.typeDesc);
        this.HANDLE = typesFactory.getTypeDescriptor(symbolTable.handleType);
        this.STREAM = typesFactory.getTypeDescriptor(symbolTable.streamType);
        this.ANY = typesFactory.getTypeDescriptor(symbolTable.anyType);
        this.ANYDATA = typesFactory.getTypeDescriptor(symbolTable.anydataType);
        this.NEVER = typesFactory.getTypeDescriptor(symbolTable.neverType);
        this.READONLY = typesFactory.getTypeDescriptor(symbolTable.readonlyType);
        this.JSON = typesFactory.getTypeDescriptor(symbolTable.jsonType);
        this.BYTE = typesFactory.getTypeDescriptor(symbolTable.byteType);
        this.COMPILATION_ERROR = typesFactory.getTypeDescriptor(symbolTable.semanticError);
    }

    public Optional<TypeDefinitionSymbol> getByName(ModuleID moduleID, String typeDefName) {
        return Optional.empty();
    }

    public Optional<TypeDefinitionSymbol> getByName(String org, String module, String version, String typeDefName) {
        return Optional.empty();
    }

    public Optional<Map<String, TypeDefinitionSymbol>> typesInModule(ModuleID moduleID) {
        return Optional.empty();
    }

    public static Types getInstance(CompilerContext context) {
        Types types = context.get(TYPES_KEY);
        if (types == null) {
            types = new Types(context);
        }

        return types;
    }

}

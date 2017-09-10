/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model;


import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.OperatorKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNullType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.model.types.TypeKind.BLOB;
import static org.ballerinalang.model.types.TypeKind.BOOLEAN;
import static org.ballerinalang.model.types.TypeKind.DATATABLE;
import static org.ballerinalang.model.types.TypeKind.FLOAT;
import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.JSON;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.ballerinalang.model.types.TypeKind.XML;

/**
 * @since 0.94
 */
public class SymbolTable {

    private static final CompilerContext.Key<SymbolTable> SYM_TABLE_KEY =
            new CompilerContext.Key<>();

    public final BLangPackage rootPkgNode;
    public final BPackageSymbol rootPkgSymbol;
    public final BSymbol notFoundSymbol;
    public final Scope rootScope;

    public final BType intType = new BType(TypeTags.INT, null);
    public final BType floatType = new BType(TypeTags.FLOAT, null);
    public final BType stringType = new BType(TypeTags.STRING, null);
    public final BType booleanType = new BType(TypeTags.BOOLEAN, null);
    public final BType blobType = new BType(TypeTags.BLOB, null);
    public final BType jsonType = new BType(TypeTags.JSON, null);
    public final BType xmlType = new BType(TypeTags.XML, null);
    public final BType datatableType = new BType(TypeTags.DATATABLE, null);
    public final BType noType = new BNoType(TypeTags.NONE);
    public final BType nullType = new BNullType();
    public final BType voidType = new BNoType(TypeTags.VOID);

    public final BTypeSymbol errSymbol;
    public final BType errType;

    private Names names;

    private CompilerContext context;

    public static SymbolTable getInstance(CompilerContext context) {
        SymbolTable symTable = context.get(SYM_TABLE_KEY);
        if (symTable == null) {
            symTable = new SymbolTable(context);
        }

        return symTable;
    }

    private SymbolTable(CompilerContext context) {
        this.context = context;
        this.context.put(SYM_TABLE_KEY, this);

        this.names = Names.getInstance(context);

        this.rootPkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        this.rootPkgSymbol = new BPackageSymbol(PackageID.EMPTY, null);
        this.rootPkgNode.symbol = this.rootPkgSymbol;
        this.rootScope = new Scope(rootPkgSymbol);
        this.rootPkgSymbol.scope = this.rootScope;
        this.notFoundSymbol = new BSymbol(SymTag.NIL, Names.INVALID, noType, rootPkgSymbol);


        // Initialize built-in types in Ballerina
        initializeType(intType, INT.typeName());
        initializeType(floatType, FLOAT.typeName());
        initializeType(stringType, STRING.typeName());
        initializeType(booleanType, BOOLEAN.typeName());
        initializeType(blobType, BLOB.typeName());
        initializeType(jsonType, JSON.typeName());
        initializeType(xmlType, XML.typeName());
        initializeType(datatableType, DATATABLE.typeName());

        // Initialize error type;
        this.errType = new BErrorType(null);
        this.errSymbol = new BTypeSymbol(SymTag.ERROR, Names.INVALID, errType, rootPkgSymbol);
        defineType(errType, errSymbol);

        defineOperators();
    }

    public BType getTypeFromTag(int tag) {
        switch (tag) {
            case TypeTags.INT:
                return intType;
            case TypeTags.FLOAT:
                return floatType;
            case TypeTags.STRING:
                return stringType;
            case TypeTags.BOOLEAN:
                return booleanType;
            case TypeTags.BLOB:
                return blobType;
            case TypeTags.JSON:
                return jsonType;
            case TypeTags.XML:
                return xmlType;
            case TypeTags.DATATABLE:
                return datatableType;
            case TypeTags.NULL:
                return nullType;
            default:
                return errType;
        }
    }

    private void initializeType(BType type, String name) {
        initializeType(type, names.fromString(name));
    }

    private void initializeType(BType type, Name name) {
        defineType(type, new BTypeSymbol(SymTag.TYPE, name, type, rootPkgSymbol));
    }

    private void defineType(BType type, BTypeSymbol tSymbol) {
        type.tsymbol = tSymbol;
        rootScope.define(tSymbol.name, tSymbol);
    }

    private void defineOperators() {
        // Binary operator symbols
        defineBinaryOperator(OperatorKind.ADD, stringType, floatType, stringType, -1);
        defineBinaryOperator(OperatorKind.ADD, floatType, stringType, stringType, -1);
        defineBinaryOperator(OperatorKind.ADD, stringType, intType, stringType, -1);
        defineBinaryOperator(OperatorKind.ADD, intType, stringType, stringType, -1);
        defineBinaryOperator(OperatorKind.ADD, floatType, intType, floatType, -1);
        defineBinaryOperator(OperatorKind.ADD, intType, floatType, floatType, -1);
        defineBinaryOperator(OperatorKind.ADD, xmlType, xmlType, xmlType, -1);
        defineBinaryOperator(OperatorKind.ADD, stringType, stringType, floatType, -1);
        defineBinaryOperator(OperatorKind.ADD, floatType, floatType, floatType, -1);
        defineBinaryOperator(OperatorKind.ADD, intType, intType, intType, -1);

        defineBinaryOperator(OperatorKind.SUB, floatType, intType, intType, -1);
        defineBinaryOperator(OperatorKind.SUB, intType, floatType, intType, -1);
        defineBinaryOperator(OperatorKind.SUB, floatType, floatType, intType, -1);
        defineBinaryOperator(OperatorKind.SUB, intType, intType, intType, -1);

        defineBinaryOperator(OperatorKind.DIV, floatType, intType, intType, -1);
        defineBinaryOperator(OperatorKind.DIV, intType, floatType, intType, -1);
        defineBinaryOperator(OperatorKind.DIV, floatType, floatType, intType, -1);
        defineBinaryOperator(OperatorKind.DIV, intType, intType, intType, -1);

        defineBinaryOperator(OperatorKind.MUL, floatType, intType, intType, -1);
        defineBinaryOperator(OperatorKind.MUL, intType, floatType, intType, -1);
        defineBinaryOperator(OperatorKind.MUL, floatType, floatType, intType, -1);
        defineBinaryOperator(OperatorKind.MUL, intType, intType, intType, -1);


        // Unary operator symbols
        defineUnaryOperator(OperatorKind.ADD, floatType, floatType, -1);
        defineUnaryOperator(OperatorKind.ADD, intType, intType, -1);

        defineUnaryOperator(OperatorKind.SUB, floatType, floatType, -1);
        defineUnaryOperator(OperatorKind.SUB, intType, intType, -1);

        defineUnaryOperator(OperatorKind.NOT, booleanType, booleanType, -1);

    }

    private void defineBinaryOperator(OperatorKind kind,
                                      BType lhsType,
                                      BType rhsType,
                                      BType retType,
                                      int opcode) {
        List<BType> paramTypes = new ArrayList<>(2);
        paramTypes.add(lhsType);
        paramTypes.add(rhsType);
        List<BType> retTypes = new ArrayList<>(1);
        retTypes.add(retType);
        defineOperator(names.fromString(kind.value()), paramTypes, retTypes, opcode);
    }

    private void defineUnaryOperator(OperatorKind kind,
                                     BType type,
                                     BType retType,
                                     int opcode) {
        List<BType> paramTypes = new ArrayList<>(2);
        paramTypes.add(type);
        List<BType> retTypes = new ArrayList<>(1);
        retTypes.add(retType);
        defineOperator(names.fromString(kind.value()), paramTypes, retTypes, opcode);
    }

    private void defineOperator(Name name,
                                List<BType> paramTypes,
                                List<BType> retTypes,
                                int opcode) {
        BInvokableType opType = new BInvokableType(paramTypes, retTypes, null);
        BOperatorSymbol symbol = new BOperatorSymbol(name, opType, rootPkgSymbol, opcode);
        rootScope.define(name, symbol);
    }
}

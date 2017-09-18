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
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNullType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.List;

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
    public final BType typeType = new BType(TypeTags.TYPE, null);
    public final BType jsonType = new BBuiltInRefType(TypeTags.JSON, null);
    public final BType xmlType = new BBuiltInRefType(TypeTags.XML, null);
    public final BType datatableType = new BBuiltInRefType(TypeTags.DATATABLE, null);
    public final BType mapType = new BBuiltInRefType(TypeTags.MAP, null);
    public final BType anyType = new BBuiltInRefType(TypeTags.ANY, null);
    public final BType noType = new BNoType(TypeTags.NONE);
    public final BType nullType = new BNullType();
    public final BType voidType = new BNoType(TypeTags.VOID);

    public final BTypeSymbol errSymbol;
    public final BType errType;

    public final BStructType errStructType;
    public final BTypeSymbol errStructSymbol;
    public final BStructType stackFrameType;
    public final BTypeSymbol stackFrameSymbol;

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
        this.rootPkgSymbol = new BPackageSymbol(Names.EMPTY, Names.EMPTY, null);
        this.rootPkgNode.symbol = this.rootPkgSymbol;
        this.rootScope = new Scope(rootPkgSymbol);
        this.rootPkgSymbol.scope = this.rootScope;
        this.notFoundSymbol = new BSymbol(SymTag.NIL, 0, Names.INVALID, noType, rootPkgSymbol);

        // Initialize built-in types in Ballerina
        initializeType(intType, TypeKind.INT.typeName());
        initializeType(floatType, TypeKind.FLOAT.typeName());
        initializeType(stringType, TypeKind.STRING.typeName());
        initializeType(booleanType, TypeKind.BOOLEAN.typeName());
        initializeType(blobType, TypeKind.BLOB.typeName());
        initializeType(typeType, TypeKind.TYPE.typeName());
        initializeType(jsonType, TypeKind.JSON.typeName());
        initializeType(xmlType, TypeKind.XML.typeName());
        initializeType(datatableType, TypeKind.DATATABLE.typeName());
        initializeType(mapType, TypeKind.MAP.typeName());
        initializeType(anyType, TypeKind.ANY.typeName());

        // Initialize error type;
        this.errType = new BErrorType(null);
        this.errSymbol = new BTypeSymbol(SymTag.ERROR, 0, Names.INVALID, errType, rootPkgSymbol);
        defineType(errType, errSymbol);

        // Initialize builtin error struct type and stackFrame struct.
        BStructType.BStructField callerField = new BStructType.BStructField(Names.CALLER, stringType);
        BStructType.BStructField packageField = new BStructType.BStructField(Names.PACKAGE, stringType);
        BStructType.BStructField fileNameField = new BStructType.BStructField(Names.FILE_NAME, stringType);
        BStructType.BStructField lineNumberField = new BStructType.BStructField(Names.LINE_NUMBER, intType);
        this.stackFrameType = new BStructType(null,
                Lists.of(callerField, packageField, fileNameField, lineNumberField));
        this.stackFrameSymbol = Symbols.createStructSymbol(Flags.PUBLIC, Names.STACK_FRAME,
                this.stackFrameType, rootPkgSymbol);
        defineType(stackFrameType, stackFrameSymbol);


        BStructType.BStructField msgField = new BStructType.BStructField(Names.MSG, stringType);
        this.errStructType = new BStructType(null, Lists.of(msgField));
        BStructType.BStructField causeField = new BStructType.BStructField(Names.CAUSE, this.errStructType);
        BStructType.BStructField stackTraceField = new BStructType.BStructField(Names.STACK_TRACE,
                new BArrayType(stackFrameType));
        this.errStructType.fields = Lists.of(msgField, causeField, stackTraceField);
        this.errStructSymbol = Symbols.createStructSymbol(Flags.PUBLIC, Names.ERROR, this.errStructType, rootPkgSymbol);
        defineType(errStructType, errStructSymbol);

        // Define all operators e.g. binary, unary, cast and conversion
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
        defineType(type, new BTypeSymbol(SymTag.TYPE, 0, name, type, rootPkgSymbol));
    }

    private void defineType(BType type, BTypeSymbol tSymbol) {
        type.tsymbol = tSymbol;
        rootScope.define(tSymbol.name, tSymbol);
    }

    private void defineOperators() {
        // Binary operator symbols
        defineBinaryOperator(OperatorKind.ADD, xmlType, xmlType, xmlType, InstructionCodes.XMLADD);
        defineBinaryOperator(OperatorKind.ADD, floatType, stringType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, intType, stringType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, stringType, floatType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, stringType, intType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, stringType, stringType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, floatType, floatType, floatType, InstructionCodes.FADD);
        defineBinaryOperator(OperatorKind.ADD, intType, intType, intType, InstructionCodes.IADD);

        defineBinaryOperator(OperatorKind.SUB, floatType, floatType, floatType, -1);
        defineBinaryOperator(OperatorKind.SUB, intType, intType, intType, -1);

        defineBinaryOperator(OperatorKind.DIV, floatType, floatType, floatType, -1);
        defineBinaryOperator(OperatorKind.DIV, intType, intType, intType, -1);

        defineBinaryOperator(OperatorKind.MUL, floatType, floatType, floatType, -1);
        defineBinaryOperator(OperatorKind.MUL, intType, intType, intType, -1);

        // Unary operator symbols
        defineUnaryOperator(OperatorKind.ADD, floatType, floatType, -1);
        defineUnaryOperator(OperatorKind.ADD, intType, intType, -1);

        defineUnaryOperator(OperatorKind.SUB, floatType, floatType, -1);
        defineUnaryOperator(OperatorKind.SUB, intType, intType, -1);

        defineUnaryOperator(OperatorKind.NOT, booleanType, booleanType, -1);

        defineUnaryOperator(OperatorKind.LENGTHOF, jsonType, intType, -1);
        defineUnaryOperator(OperatorKind.LENGTHOF, new BArrayType(noType), intType, -1);
        defineUnaryOperator(OperatorKind.TYPEOF, noType, typeType, -1);

        defineCastOperators();
    }

    private void defineCastOperators() {
        // Define both implicit and explicit cast operators
        defineCastOperator(intType, jsonType, true, -1);
        defineCastOperator(intType, anyType, true, -1);
        defineCastOperator(floatType, jsonType, true, -1);
        defineCastOperator(floatType, anyType, true, -1);
        defineCastOperator(stringType, jsonType, true, -1);
        defineCastOperator(stringType, anyType, true, -1);
        defineCastOperator(booleanType, jsonType, true, -1);
        defineCastOperator(booleanType, anyType, true, -1);
        defineCastOperator(blobType, anyType, true, -1);
        defineCastOperator(typeType, anyType, true, -1);
        defineCastOperator(nullType, jsonType, true, -1);

        // Define explicit cast operators
        defineExplicitCastOperator(anyType, intType, false, -1);
        defineExplicitCastOperator(anyType, floatType, false, -1);
        defineExplicitCastOperator(anyType, stringType, false, -1);
        defineExplicitCastOperator(anyType, booleanType, false, -1);
        defineExplicitCastOperator(anyType, blobType, false, -1);
        defineExplicitCastOperator(anyType, typeType, false, -1);
        defineExplicitCastOperator(anyType, jsonType, false, -1);
        defineExplicitCastOperator(anyType, xmlType, false, -1);
        defineExplicitCastOperator(anyType, mapType, false, -1);
        defineExplicitCastOperator(anyType, datatableType, false, -1);

        defineExplicitCastOperator(jsonType, intType, false, -1);
        defineExplicitCastOperator(jsonType, floatType, false, -1);
        defineExplicitCastOperator(jsonType, stringType, false, -1);
        defineExplicitCastOperator(jsonType, booleanType, false, -1);
    }

    private void defineBinaryOperator(OperatorKind kind,
                                      BType lhsType,
                                      BType rhsType,
                                      BType retType,
                                      int opcode) {
        List<BType> paramTypes = Lists.of(lhsType, rhsType);
        List<BType> retTypes = Lists.of(retType);
        defineOperator(names.fromString(kind.value()), paramTypes, retTypes, opcode);
    }

    private void defineUnaryOperator(OperatorKind kind,
                                     BType type,
                                     BType retType,
                                     int opcode) {
        List<BType> paramTypes = Lists.of(type);
        List<BType> retTypes = Lists.of(retType);
        defineOperator(names.fromString(kind.value()), paramTypes, retTypes, opcode);
    }

    private void defineExplicitCastOperator(BType sourceType,
                                            BType targetType,
                                            boolean safe,
                                            int opcode) {
        defineCastOperator(sourceType, targetType, false, true, safe, opcode);
    }

    private void defineCastOperator(BType sourceType,
                                    BType targetType,
                                    boolean safe,
                                    int opcode) {
        defineCastOperator(sourceType, targetType, true, true, safe, opcode);
    }

    private void defineCastOperator(BType sourceType,
                                    BType targetType,
                                    boolean implicit,
                                    boolean explicit,
                                    boolean safe,
                                    int opcode) {
        List<BType> paramTypes = Lists.of(sourceType, targetType);
        List<BType> retTypes = Lists.of(targetType, this.errStructType);
        BInvokableType opType = new BInvokableType(paramTypes, retTypes, null);
        BCastOperatorSymbol symbol = new BCastOperatorSymbol(opType, rootPkgSymbol,
                implicit, explicit, safe, opcode);
        rootScope.define(symbol.name, symbol);
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

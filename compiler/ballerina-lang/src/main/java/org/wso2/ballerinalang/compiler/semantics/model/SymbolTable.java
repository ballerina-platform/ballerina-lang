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
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BConnectorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLAttributesType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @since 0.94
 */
public class SymbolTable {

    private static final CompilerContext.Key<SymbolTable> SYM_TABLE_KEY =
            new CompilerContext.Key<>();

    public static final PackageID BUILTIN = new PackageID(Names.BUILTIN_ORG,
                                                          Names.BUILTIN_PACKAGE,
                                                          Names.DEFAULT_VERSION);

    public final BLangPackage rootPkgNode;
    public final BPackageSymbol rootPkgSymbol;
    public final BSymbol notFoundSymbol;
    public final Scope rootScope;

    public final BType noType = new BNoType(TypeTags.NONE);
    public final BType nilType = new BNilType();
    public final BType intType = new BType(TypeTags.INT, null);
    public final BType floatType = new BType(TypeTags.FLOAT, null);
    public final BType stringType = new BType(TypeTags.STRING, null);
    public final BType booleanType = new BType(TypeTags.BOOLEAN, null);
    public final BType blobType = new BType(TypeTags.BLOB, null);
    public final BType typeDesc = new BType(TypeTags.TYPEDESC, null);
    public final BType jsonType = new BJSONType(TypeTags.JSON, noType, null);
    public final BType xmlType = new BXMLType(TypeTags.XML, null);
    public final BType tableType = new BTableType(TypeTags.TABLE, noType, null);
    public final BType streamType = new BStreamType(TypeTags.STREAM, noType, null);
    public final BType anyType = new BAnyType(TypeTags.ANY, null);
    public final BType mapType = new BMapType(TypeTags.MAP, anyType, null);
    public final BType futureType = new BFutureType(TypeTags.FUTURE, nilType, null);
    public final BType xmlAttributesType = new BXMLAttributesType(TypeTags.XML_ATTRIBUTES);
    public final BType connectorType = new BConnectorType(null, null);
    public final BType endpointType = new BType(TypeTags.CONNECTOR, null);
    public final BType arrayType = new BArrayType(noType);

    public final BTypeSymbol errSymbol;
    public final BType errType;

    public BStructType errStructType;

    public BPackageSymbol builtInPackageSymbol;
    private Names names;
    public Map<BPackageSymbol, SymbolEnv> pkgEnvMap = new HashMap<>();

    public static SymbolTable getInstance(CompilerContext context) {
        SymbolTable symTable = context.get(SYM_TABLE_KEY);
        if (symTable == null) {
            symTable = new SymbolTable(context);
        }

        return symTable;
    }

    private SymbolTable(CompilerContext context) {
        context.put(SYM_TABLE_KEY, this);

        this.names = Names.getInstance(context);

        this.rootPkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        this.rootPkgSymbol = new BPackageSymbol(BUILTIN, null);
        this.rootPkgNode.symbol = this.rootPkgSymbol;
        this.rootScope = new BuiltInScope(rootPkgSymbol);
        this.rootPkgSymbol.scope = this.rootScope;
        this.notFoundSymbol = new BSymbol(SymTag.NIL, Flags.PUBLIC, Names.INVALID,
                rootPkgSymbol.pkgID, noType, rootPkgSymbol);
        // Initialize built-in types in Ballerina
        initializeType(intType, TypeKind.INT.typeName());
        initializeType(floatType, TypeKind.FLOAT.typeName());
        initializeType(stringType, TypeKind.STRING.typeName());
        initializeType(booleanType, TypeKind.BOOLEAN.typeName());
        initializeType(blobType, TypeKind.BLOB.typeName());
        initializeType(typeDesc, TypeKind.TYPEDESC.typeName());
        initializeType(jsonType, TypeKind.JSON.typeName());
        initializeType(xmlType, TypeKind.XML.typeName());
        initializeType(tableType, TypeKind.TABLE.typeName());
        initializeType(streamType, TypeKind.STREAM.typeName());
        initializeType(mapType, TypeKind.MAP.typeName());
        initializeType(futureType, TypeKind.FUTURE.typeName());
        initializeType(anyType, TypeKind.ANY.typeName());
        initializeType(nilType, TypeKind.NIL.typeName());

        // Initialize error type;
        this.errType = new BErrorType(null);
        this.errSymbol = new BTypeSymbol(SymTag.ERROR, Flags.PUBLIC, Names.INVALID,
                rootPkgSymbol.pkgID, errType, rootPkgSymbol);
        defineType(errType, errSymbol);

        // Initialize Ballerina error struct type temporally.
        BTypeSymbol errorStructSymbol = new BStructSymbol(SymTag.STRUCT, Flags.PUBLIC, Names.ERROR,
                rootPkgSymbol.pkgID, null, rootPkgSymbol);
        this.errStructType = new BStructType(errorStructSymbol);
        errorStructSymbol.type = this.errStructType;
        errorStructSymbol.scope = new Scope(errorStructSymbol);
        defineType(this.errStructType, errorStructSymbol);

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
            case TypeTags.TABLE:
                return tableType;
            case TypeTags.STREAM:
                return streamType;
            case TypeTags.NIL:
                return nilType;
            default:
                return errType;
        }
    }

    private void initializeType(BType type, String name) {
        initializeType(type, names.fromString(name));
    }

    private void initializeType(BType type, Name name) {
        defineType(type, new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, name, rootPkgSymbol.pkgID, type, rootPkgSymbol));
    }

    private void defineType(BType type, BTypeSymbol tSymbol) {
        type.tsymbol = tSymbol;
        rootScope.define(tSymbol.name, tSymbol);
    }

    private void defineOperators() {
        // Binary arithmetic operators
        defineBinaryOperator(OperatorKind.ADD, xmlType, xmlType, xmlType, InstructionCodes.XMLADD);
        defineBinaryOperator(OperatorKind.ADD, floatType, stringType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, intType, stringType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, booleanType, stringType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, stringType, floatType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, stringType, intType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, stringType, booleanType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, stringType, stringType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, stringType, booleanType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, booleanType, stringType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, floatType, floatType, floatType, InstructionCodes.FADD);
        defineBinaryOperator(OperatorKind.ADD, intType, intType, intType, InstructionCodes.IADD);
        defineBinaryOperator(OperatorKind.ADD, intType, floatType, floatType, InstructionCodes.FADD);
        defineBinaryOperator(OperatorKind.ADD, floatType, intType, floatType, InstructionCodes.FADD);
        defineBinaryOperator(OperatorKind.SUB, floatType, floatType, floatType, InstructionCodes.FSUB);
        defineBinaryOperator(OperatorKind.SUB, intType, intType, intType, InstructionCodes.ISUB);
        defineBinaryOperator(OperatorKind.SUB, floatType, intType, floatType, InstructionCodes.FSUB);
        defineBinaryOperator(OperatorKind.SUB, intType, floatType, floatType, InstructionCodes.FSUB);
        defineBinaryOperator(OperatorKind.DIV, floatType, floatType, floatType, InstructionCodes.FDIV);
        defineBinaryOperator(OperatorKind.DIV, intType, intType, intType, InstructionCodes.IDIV);
        defineBinaryOperator(OperatorKind.DIV, intType, floatType, floatType, InstructionCodes.FDIV);
        defineBinaryOperator(OperatorKind.DIV, floatType, intType, floatType, InstructionCodes.FDIV);
        defineBinaryOperator(OperatorKind.MUL, floatType, floatType, floatType, InstructionCodes.FMUL);
        defineBinaryOperator(OperatorKind.MUL, intType, intType, intType, InstructionCodes.IMUL);
        defineBinaryOperator(OperatorKind.MUL, floatType, intType, floatType, InstructionCodes.FMUL);
        defineBinaryOperator(OperatorKind.MUL, intType, floatType, floatType, InstructionCodes.FMUL);
        defineBinaryOperator(OperatorKind.MOD, floatType, floatType, floatType, InstructionCodes.FMOD);
        defineBinaryOperator(OperatorKind.MOD, intType, intType, intType, InstructionCodes.IMOD);
        defineBinaryOperator(OperatorKind.MOD, floatType, intType, floatType, InstructionCodes.FMOD);
        defineBinaryOperator(OperatorKind.MOD, intType, floatType, floatType, InstructionCodes.FMOD);

        // Binary equality operators ==, !=
        defineBinaryOperator(OperatorKind.EQUAL, intType, intType, booleanType, InstructionCodes.IEQ);
        defineBinaryOperator(OperatorKind.EQUAL, floatType, floatType, booleanType, InstructionCodes.FEQ);
        defineBinaryOperator(OperatorKind.EQUAL, booleanType, booleanType, booleanType, InstructionCodes.BEQ);
        defineBinaryOperator(OperatorKind.EQUAL, stringType, stringType, booleanType, InstructionCodes.SEQ);
        defineBinaryOperator(OperatorKind.EQUAL, typeDesc, typeDesc, booleanType, InstructionCodes.TEQ);
        defineBinaryOperator(OperatorKind.EQUAL, jsonType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, jsonType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, xmlType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, xmlType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, tableType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, tableType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, streamType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, streamType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, anyType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, anyType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, mapType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, mapType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, connectorType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, connectorType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, arrayType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, arrayType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, stringType, nilType, booleanType, InstructionCodes.SEQ_NULL);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, stringType, booleanType, InstructionCodes.SEQ_NULL);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, intType, intType, booleanType, InstructionCodes.INE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, floatType, floatType, booleanType, InstructionCodes.FNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, booleanType, booleanType, booleanType, InstructionCodes.BNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, stringType, stringType, booleanType, InstructionCodes.SNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, typeDesc, typeDesc, booleanType, InstructionCodes.TNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, jsonType, nilType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, jsonType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, xmlType, nilType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, xmlType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, tableType, nilType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, tableType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, streamType, nilType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, streamType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, anyType, nilType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, anyType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, mapType, nilType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, mapType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, connectorType, nilType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, connectorType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, arrayType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, arrayType, nilType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, nilType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, stringType, nilType, booleanType, InstructionCodes.SNE_NULL);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, stringType, booleanType, InstructionCodes.SNE_NULL);

        // Binary comparison operators <=, <, >=, >
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, intType, booleanType, InstructionCodes.ILT);
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, floatType, booleanType, InstructionCodes.FLT);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, intType, booleanType, InstructionCodes.FLT);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, floatType, booleanType, InstructionCodes.FLT);

        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, intType, booleanType, InstructionCodes.ILE);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, intType, booleanType, InstructionCodes.FLE);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, floatType, booleanType, InstructionCodes.FLE);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, floatType, booleanType, InstructionCodes.FLE);

        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, intType, booleanType, InstructionCodes.IGT);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, intType, booleanType, InstructionCodes.FGT);
        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, floatType, booleanType, InstructionCodes.FGT);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, floatType, booleanType, InstructionCodes.FGT);

        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, intType, booleanType, InstructionCodes.IGE);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, intType, booleanType, InstructionCodes.FGE);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, floatType, booleanType, InstructionCodes.FGE);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, floatType, booleanType, InstructionCodes.FGE);

        defineBinaryOperator(OperatorKind.AND, booleanType, booleanType, booleanType, -1);
        defineBinaryOperator(OperatorKind.OR, booleanType, booleanType, booleanType, -1);

        // Unary operator symbols
        defineUnaryOperator(OperatorKind.ADD, floatType, floatType, -1);
        defineUnaryOperator(OperatorKind.ADD, intType, intType, -1);

        defineUnaryOperator(OperatorKind.SUB, floatType, floatType, InstructionCodes.FNEG);
        defineUnaryOperator(OperatorKind.SUB, intType, intType, InstructionCodes.INEG);

        defineUnaryOperator(OperatorKind.NOT, booleanType, booleanType, InstructionCodes.BNOT);

        defineUnaryOperator(OperatorKind.LENGTHOF, jsonType, intType, InstructionCodes.LENGTHOF);
        defineUnaryOperator(OperatorKind.LENGTHOF, arrayType, intType, InstructionCodes.LENGTHOF);
        defineUnaryOperator(OperatorKind.LENGTHOF, xmlType, intType, InstructionCodes.LENGTHOF);
        defineUnaryOperator(OperatorKind.LENGTHOF, mapType, intType, InstructionCodes.LENGTHOF);
        defineUnaryOperator(OperatorKind.LENGTHOF, stringType, intType, InstructionCodes.LENGTHOF);
        defineUnaryOperator(OperatorKind.LENGTHOF, blobType, intType, InstructionCodes.LENGTHOF);

        defineConversionOperators();
    }

    private void defineConversionOperators() {
        // Define both implicit and explicit conversion operators
        defineImplicitConversionOperator(intType, jsonType, true, InstructionCodes.I2JSON);
        defineImplicitConversionOperator(intType, anyType, true, InstructionCodes.I2ANY);
        defineImplicitConversionOperator(intType, floatType, true, InstructionCodes.I2F);
        defineImplicitConversionOperator(floatType, jsonType, true, InstructionCodes.F2JSON);
        defineImplicitConversionOperator(floatType, anyType, true, InstructionCodes.F2ANY);
        defineImplicitConversionOperator(stringType, jsonType, true, InstructionCodes.S2JSON);
        defineImplicitConversionOperator(stringType, anyType, true, InstructionCodes.S2ANY);
        defineImplicitConversionOperator(booleanType, jsonType, true, InstructionCodes.B2JSON);
        defineImplicitConversionOperator(booleanType, anyType, true, InstructionCodes.B2ANY);
        defineImplicitConversionOperator(blobType, anyType, true, InstructionCodes.L2ANY);
        defineImplicitConversionOperator(typeDesc, anyType, true, InstructionCodes.NOP);

        // Define explicit conversion operators
        defineConversionOperator(anyType, intType, false, InstructionCodes.CHECKCAST);
        defineConversionOperator(anyType, floatType, false, InstructionCodes.CHECKCAST);
        defineConversionOperator(anyType, stringType, false, InstructionCodes.CHECKCAST);
        defineConversionOperator(anyType, booleanType, false, InstructionCodes.CHECKCAST);
        defineConversionOperator(anyType, blobType, false, InstructionCodes.CHECKCAST);
        defineConversionOperator(anyType, typeDesc, false, InstructionCodes.ANY2TYPE);
        defineConversionOperator(anyType, jsonType, false, InstructionCodes.ANY2JSON);
        defineConversionOperator(anyType, xmlType, false, InstructionCodes.ANY2XML);
        defineConversionOperator(anyType, mapType, false, InstructionCodes.ANY2MAP);
        defineConversionOperator(anyType, tableType, false, InstructionCodes.ANY2DT);
        defineConversionOperator(anyType, streamType, false, InstructionCodes.ANY2STM);

        defineConversionOperator(jsonType, intType, false, InstructionCodes.JSON2I);
        defineConversionOperator(jsonType, floatType, false, InstructionCodes.JSON2F);
        defineConversionOperator(jsonType, stringType, false, InstructionCodes.JSON2S);
        defineConversionOperator(jsonType, booleanType, false, InstructionCodes.JSON2B);

        // Define conversion operators
        defineConversionOperator(anyType, stringType, true, InstructionCodes.ANY2SCONV);
//        defineConversionOperator(intType, floatType, true, InstructionCodes.I2F);
        defineConversionOperator(intType, booleanType, true, InstructionCodes.I2B);
        defineConversionOperator(intType, stringType, true, InstructionCodes.I2S);
        defineConversionOperator(floatType, intType, true, InstructionCodes.F2I);
        defineConversionOperator(floatType, booleanType, true, InstructionCodes.F2B);
        defineConversionOperator(floatType, stringType, true, InstructionCodes.F2S);
        defineConversionOperator(stringType, floatType, false, InstructionCodes.S2F);
        defineConversionOperator(stringType, intType, false, InstructionCodes.S2I);
        defineConversionOperator(stringType, booleanType, true, InstructionCodes.S2B);
        defineConversionOperator(booleanType, stringType, true, InstructionCodes.B2S);
        defineConversionOperator(booleanType, intType, true, InstructionCodes.B2I);
        defineConversionOperator(booleanType, floatType, true, InstructionCodes.B2F);
        defineConversionOperator(tableType, xmlType, false, InstructionCodes.DT2XML);
        defineConversionOperator(tableType, jsonType, false, InstructionCodes.DT2JSON);
        defineConversionOperator(xmlAttributesType, mapType, true, InstructionCodes.XMLATTRS2MAP);
//        defineConversionOperator(stringType, xmlType, false, InstructionCodes.S2XML);
        defineConversionOperator(xmlType, stringType, true, InstructionCodes.XML2S);
//        defineConversionOperator(stringType, jsonType, false, InstructionCodes.S2JSONX);
    }

    private void defineBinaryOperator(OperatorKind kind,
                                      BType lhsType,
                                      BType rhsType,
                                      BType retType,
                                      int opcode) {
        List<BType> paramTypes = Lists.of(lhsType, rhsType);
        defineOperator(names.fromString(kind.value()), paramTypes, retType, opcode);
    }

    private void defineUnaryOperator(OperatorKind kind,
                                     BType type,
                                     BType retType,
                                     int opcode) {
        List<BType> paramTypes = Lists.of(type);
        defineOperator(names.fromString(kind.value()), paramTypes, retType, opcode);
    }

    private void defineImplicitConversionOperator(BType sourceType,
                                                  BType targetType,
                                                  boolean safe,
                                                  int opcode) {
        defineConversionOperator(sourceType, targetType, true, safe, opcode);
    }

    private void defineConversionOperator(BType sourceType,
                                          BType targetType,
                                          boolean safe,
                                          int opcode) {
        defineConversionOperator(sourceType, targetType, false, safe, opcode);
    }

    private void defineConversionOperator(BType sourceType,
                                          BType targetType,
                                          boolean implicit,
                                          boolean safe,
                                          int opcode) {
        List<BType> paramTypes = Lists.of(sourceType, targetType);
        BType retType;
        if (safe) {
            retType = targetType;
        } else {
            if (targetType.tag == TypeTags.UNION) {
                BUnionType unionType = (BUnionType) targetType;
                unionType.memberTypes.add(this.errStructType);
                retType = targetType;
            } else {
                BUnionType unionType = new BUnionType(null,
                        new LinkedHashSet<BType>(2) {{
                            add(targetType);
                            add(errStructType);
                        }}, false);
                retType = unionType;
            }
        }
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        BConversionOperatorSymbol symbol = new BConversionOperatorSymbol(this.rootPkgSymbol.pkgID, opType,
                this.rootPkgSymbol, implicit, safe, opcode);
        symbol.kind = SymbolKind.CONVERSION_OPERATOR;
        rootScope.define(symbol.name, symbol);
    }

    private void defineOperator(Name name,
                                List<BType> paramTypes,
                                BType retType,
                                int opcode) {
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        BOperatorSymbol symbol = new BOperatorSymbol(name, rootPkgSymbol.pkgID, opType, rootPkgSymbol, opcode);
        rootScope.define(name, symbol);
    }
}

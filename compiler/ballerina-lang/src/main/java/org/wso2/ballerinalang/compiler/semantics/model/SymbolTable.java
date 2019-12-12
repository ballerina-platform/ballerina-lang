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
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstructorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BHandleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BSemanticErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @since 0.94
 */
public class SymbolTable {

    private static final CompilerContext.Key<SymbolTable> SYM_TABLE_KEY =
            new CompilerContext.Key<>();

    public static final PackageID TRANSACTION = new PackageID(Names.BUILTIN_ORG, Names.TRANSACTION_PACKAGE, 
                                                              Names.EMPTY);
    
    public static final Integer BBYTE_MIN_VALUE = 0;
    public static final Integer BBYTE_MAX_VALUE = 255;

    public final BLangPackage rootPkgNode;
    public final BPackageSymbol rootPkgSymbol;
    public final BSymbol notFoundSymbol;
    public final BSymbol invalidUsageSymbol;
    public final Scope rootScope;

    public final BType noType = new BNoType(TypeTags.NONE);
    public final BType nilType = new BNilType();
    public final BType intType = new BType(TypeTags.INT, null);
    public final BType byteType = new BType(TypeTags.BYTE, null);
    public final BType floatType = new BType(TypeTags.FLOAT, null);
    public final BType decimalType = new BType(TypeTags.DECIMAL, null);
    public final BType stringType = new BType(TypeTags.STRING, null);
    public final BType booleanType = new BType(TypeTags.BOOLEAN, null);
    public final BType jsonType = new BJSONType(TypeTags.JSON, null);
    public final BType xmlType = new BXMLType(TypeTags.XML, null);
    public final BType tableType = new BTableType(TypeTags.TABLE, noType, null);
    public final BType anyType = new BAnyType(TypeTags.ANY, null);
    public final BType anydataType = new BAnydataType(TypeTags.ANYDATA, null);
    public final BType mapType = new BMapType(TypeTags.MAP, anyType, null);
    public final BType mapStringType = new BMapType(TypeTags.MAP, stringType, null);
    public final BType mapAnydataType = new BMapType(TypeTags.MAP, anydataType, null);
    public final BType futureType = new BFutureType(TypeTags.FUTURE, nilType, null);
    public final BType arrayType = new BArrayType(noType);
    public final BType tupleType = new BTupleType(Lists.of(noType));
    public final BType recordType = new BRecordType(null);
    public final BType intArrayType = new BArrayType(intType);
    public final BType stringArrayType = new BArrayType(stringType);
    public final BType anydataArrayType = new BArrayType(anydataType);
    public final BType anydataMapArrayType = new BArrayType(mapAnydataType);
    public final BType anyServiceType = new BServiceType(null);
    public final BType handleType = new BHandleType(TypeTags.HANDLE, null);
    public final BType typeDesc;

    public final BTypeSymbol semanticErrSymbol;
    public final BType semanticError;

    public BErrorType errorType;
    public BRecordType detailType;
    public BConstructorSymbol errorConstructor;
    public BUnionType pureType;
    public BUnionType errorOrNilType;
    public BType streamType = new BStreamType(TypeTags.STREAM, pureType, null);
    public BFiniteType trueType;
    public BObjectType intRangeType;

    public BPackageSymbol langInternalModuleSymbol;
    public BPackageSymbol langAnnotationModuleSymbol;
    public BPackageSymbol langArrayModuleSymbol;
    public BPackageSymbol langDecimalModuleSymbol;
    public BPackageSymbol langErrorModuleSymbol;
    public BPackageSymbol langFloatModuleSymbol;
    public BPackageSymbol langFutureModuleSymbol;
    public BPackageSymbol langIntModuleSymbol;
    public BPackageSymbol langMapModuleSymbol;
    public BPackageSymbol langObjectModuleSymbol;
    public BPackageSymbol langStreamModuleSymbol;
    public BPackageSymbol langStringModuleSymbol;
    public BPackageSymbol langTableModuleSymbol;
    public BPackageSymbol langTypedescModuleSymbol;
    public BPackageSymbol langValueModuleSymbol;
    public BPackageSymbol langXmlModuleSymbol;

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
        this.rootPkgSymbol = new BPackageSymbol(PackageID.ANNOTATIONS, null);
        this.rootPkgNode.pos = new DiagnosticPos(new BDiagnosticSource(rootPkgSymbol.pkgID, Names.EMPTY.value), 0, 0,
                0, 0);
        this.rootPkgNode.symbol = this.rootPkgSymbol;
        this.rootScope = new Scope(rootPkgSymbol);
        this.rootPkgSymbol.scope = this.rootScope;

        this.notFoundSymbol = new BSymbol(SymTag.NIL, Flags.PUBLIC, Names.INVALID,
                rootPkgSymbol.pkgID, noType, rootPkgSymbol);
        this.invalidUsageSymbol = new BSymbol(SymTag.NIL, Flags.PUBLIC, Names.INVALID, rootPkgSymbol.pkgID, noType,
                                              rootPkgSymbol);
        // Initialize built-in types in Ballerina
        initializeType(intType, TypeKind.INT.typeName());
        initializeType(byteType, TypeKind.BYTE.typeName());
        initializeType(floatType, TypeKind.FLOAT.typeName());
        initializeType(decimalType, TypeKind.DECIMAL.typeName());
        initializeType(stringType, TypeKind.STRING.typeName());
        initializeType(booleanType, TypeKind.BOOLEAN.typeName());
        initializeType(jsonType, TypeKind.JSON.typeName());
        initializeType(xmlType, TypeKind.XML.typeName());
        initializeType(tableType, TypeKind.TABLE.typeName());
        initializeType(streamType, TypeKind.STREAM.typeName());
        initializeType(mapType, TypeKind.MAP.typeName());
        initializeType(mapStringType, TypeKind.MAP.typeName());
        initializeType(mapAnydataType, TypeKind.MAP.typeName());
        initializeType(futureType, TypeKind.FUTURE.typeName());
        initializeType(anyType, TypeKind.ANY.typeName());
        initializeType(anydataType, TypeKind.ANYDATA.typeName());
        initializeType(nilType, TypeKind.NIL.typeName());
        initializeType(anyServiceType, TypeKind.SERVICE.typeName());
        initializeType(handleType, TypeKind.HANDLE.typeName());


        // Initialize semantic error type;
        this.semanticError = new BSemanticErrorType(null);
        this.semanticErrSymbol = new BTypeSymbol(SymTag.SEMANTIC_ERROR, Flags.PUBLIC, Names.INVALID,
                rootPkgSymbol.pkgID, semanticError, rootPkgSymbol);
        defineType(semanticError, semanticErrSymbol);

        this.typeDesc = new BTypedescType(this.anyType, null);
        initializeType(typeDesc, TypeKind.TYPEDESC.typeName());


        BLangLiteral trueLiteral = new BLangLiteral();
        trueLiteral.type = this.booleanType;
        trueLiteral.value = Boolean.TRUE;

        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, Flags.PUBLIC,
                                                                names.fromString("$anonType$TRUE"),
                                                                rootPkgNode.packageID, null, rootPkgNode.symbol.owner);
        this.trueType = new BFiniteType(finiteTypeSymbol, new HashSet<BLangExpression>() {{ add(trueLiteral); }});
    }

    public BType getTypeFromTag(int tag) {
        switch (tag) {
            case TypeTags.INT:
                return intType;
            case TypeTags.BYTE:
                return byteType;
            case TypeTags.FLOAT:
                return floatType;
            case TypeTags.DECIMAL:
                return decimalType;
            case TypeTags.STRING:
                return stringType;
            case TypeTags.BOOLEAN:
                return booleanType;
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
            case TypeTags.ERROR:
                return errorType;
            default:
                return semanticError;
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

    public void defineOperators() {
        // Binary arithmetic operators
        defineBinaryOperator(OperatorKind.ADD, xmlType, xmlType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, xmlType, stringType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, stringType, stringType, stringType);
        defineBinaryOperator(OperatorKind.ADD, stringType, xmlType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.ADD, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.ADD, intType, intType, intType);
        defineBinaryOperator(OperatorKind.ADD, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.ADD, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.ADD, byteType, byteType, intType);
        defineBinaryOperator(OperatorKind.ADD, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.ADD, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.ADD, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.ADD, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.ADD, floatType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.ADD, decimalType, floatType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.SUB, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, intType, intType, intType);
        defineBinaryOperator(OperatorKind.SUB, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.SUB, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.SUB, byteType, byteType, intType);
        defineBinaryOperator(OperatorKind.SUB, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.SUB, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.SUB, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, decimalType, floatType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, floatType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.DIV, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, intType, intType, intType);
        defineBinaryOperator(OperatorKind.DIV, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.DIV, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.DIV, byteType, byteType, intType);
        defineBinaryOperator(OperatorKind.DIV, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.DIV, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.DIV, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, floatType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, decimalType, floatType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MUL, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, intType, intType, intType);
        defineBinaryOperator(OperatorKind.MUL, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.MUL, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.MUL, byteType, byteType, intType);
        defineBinaryOperator(OperatorKind.MUL, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.MUL, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MUL, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, decimalType, floatType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, floatType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MOD, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MOD, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MOD, intType, intType, intType);
        defineBinaryOperator(OperatorKind.MOD, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.MOD, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.MOD, byteType, byteType, intType);
        defineBinaryOperator(OperatorKind.MOD, floatType, intType, floatType);
        defineBinaryOperator(OperatorKind.MOD, intType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MOD, decimalType, intType, decimalType);
        defineBinaryOperator(OperatorKind.MOD, intType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.BITWISE_AND, byteType, byteType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_AND, byteType, intType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_AND, intType, byteType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_AND, intType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_OR, byteType, byteType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_OR, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_OR, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_OR, intType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_XOR, byteType, byteType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_XOR, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_XOR, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_XOR, intType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, intType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, byteType, byteType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, byteType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_RIGHT_SHIFT, intType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_RIGHT_SHIFT, intType, byteType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_RIGHT_SHIFT, byteType, byteType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_RIGHT_SHIFT, byteType, intType, byteType);
        defineBinaryOperator(OperatorKind.BITWISE_UNSIGNED_RIGHT_SHIFT, intType, intType, intType);
        defineBinaryOperator(OperatorKind.BITWISE_UNSIGNED_RIGHT_SHIFT, intType, byteType, intType);

        // Binary equality operators ==, !=
        defineBinaryOperator(OperatorKind.EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, jsonType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, jsonType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, anyType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, anyType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, anydataType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, anydataType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, jsonType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, jsonType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, anyType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, anyType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, anydataType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, anydataType, booleanType);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, nilType, booleanType);

        // Binary reference equality operators ===, !==
        defineBinaryOperator(OperatorKind.REF_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.REF_EQUAL, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, byteType, intType, booleanType);

        // Binary comparison operators <=, <, >=, >
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, floatType, booleanType);

        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, floatType, booleanType);

        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, floatType, booleanType);

        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, floatType, booleanType);

        defineBinaryOperator(OperatorKind.AND, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.OR, booleanType, booleanType, booleanType);

        // Unary operator symbols
        defineUnaryOperator(OperatorKind.ADD, floatType, floatType);
        defineUnaryOperator(OperatorKind.ADD, decimalType, decimalType);
        defineUnaryOperator(OperatorKind.ADD, intType, intType);

        defineUnaryOperator(OperatorKind.SUB, floatType, floatType);
        defineUnaryOperator(OperatorKind.SUB, decimalType, decimalType);
        defineUnaryOperator(OperatorKind.SUB, intType, intType);

        defineUnaryOperator(OperatorKind.NOT, booleanType, booleanType);
        defineUnaryOperator(OperatorKind.BITWISE_COMPLEMENT, byteType, byteType);
        defineUnaryOperator(OperatorKind.BITWISE_COMPLEMENT, intType, intType);

        defineConversionOperators();

    }

    private void defineConversionOperators() {
        // Define both implicit and explicit conversion operators
        defineImplicitCastOperator(intType, jsonType, true);
        defineImplicitCastOperator(intType, anyType, true);
        defineImplicitCastOperator(byteType, anyType, true);
        defineImplicitCastOperator(floatType, jsonType, true);
        defineImplicitCastOperator(floatType, anyType, true);
        defineImplicitCastOperator(decimalType, jsonType, true);
        defineImplicitCastOperator(decimalType, anyType, true);
        defineImplicitCastOperator(stringType, jsonType, true);
        defineImplicitCastOperator(stringType, anyType, true);
        defineImplicitCastOperator(booleanType, jsonType, true);
        defineImplicitCastOperator(booleanType, anyType, true);
        defineImplicitCastOperator(typeDesc, anyType, true);
        defineImplicitCastOperator(intType, anydataType, true);
        defineImplicitCastOperator(byteType, anydataType, true);
        defineImplicitCastOperator(floatType, anydataType, true);
        defineImplicitCastOperator(decimalType, anydataType, true);
        defineImplicitCastOperator(stringType, anydataType, true);
        defineImplicitCastOperator(booleanType, anydataType, true);

        // Define explicit conversion operators
        defineCastOperator(anyType, intType, false);
        defineCastOperator(anyType, byteType, false);
        defineCastOperator(anyType, floatType, false);
        defineCastOperator(anyType, decimalType, false);
        defineCastOperator(anyType, stringType, false);
        defineCastOperator(anyType, booleanType, false);
        defineCastOperator(anyType, typeDesc, false);
        defineCastOperator(anyType, jsonType, false);
        defineCastOperator(anyType, xmlType, false);
        defineCastOperator(anyType, mapType, false);
        defineCastOperator(anyType, tableType, false);
        defineCastOperator(anyType, streamType, false);
        defineCastOperator(anyType, handleType, false);
        defineCastOperator(anydataType, intType, false);
        defineCastOperator(anydataType, byteType, false);
        defineCastOperator(anydataType, floatType, false);
        defineCastOperator(anydataType, decimalType, false);
        defineCastOperator(anydataType, stringType, false);
        defineCastOperator(anydataType, booleanType, false);
        defineCastOperator(anydataType, jsonType, false);
        defineCastOperator(anydataType, xmlType, false);
        defineCastOperator(anydataType, tableType, false);

        defineCastOperator(jsonType, intType, false);
        defineCastOperator(jsonType, floatType, false);
        defineCastOperator(jsonType, decimalType, false);
        defineCastOperator(jsonType, stringType, false);
        defineCastOperator(jsonType, booleanType, false);

        // Define conversion operators

        defineConversionOperator(anyType, intType, false);
        defineConversionOperator(anyType, byteType, false);
        defineConversionOperator(anyType, floatType, false);
        defineConversionOperator(anyType, decimalType, false);
        defineConversionOperator(anyType, stringType, true);
        defineConversionOperator(anyType, booleanType, false);
        defineConversionOperator(anydataType, intType, false);
        defineConversionOperator(anydataType, byteType, false);
        defineConversionOperator(anydataType, floatType, false);
        defineConversionOperator(anydataType, decimalType, false);
        defineConversionOperator(anydataType, stringType, true);
        defineConversionOperator(anydataType, booleanType, false);
        defineConversionOperator(jsonType, intType, false);
        defineConversionOperator(jsonType, floatType, false);
        defineConversionOperator(jsonType, decimalType, false);
        defineConversionOperator(jsonType, stringType, false);
        defineConversionOperator(jsonType, booleanType, false);

        defineConversionOperator(intType, floatType, true);
        defineConversionOperator(intType, decimalType, true);
        defineConversionOperator(intType, booleanType, true);
        defineConversionOperator(intType, stringType, true);
        defineConversionOperator(intType, byteType, false);
        defineConversionOperator(floatType, intType, false);
        defineConversionOperator(floatType, decimalType, true);
        defineConversionOperator(floatType, booleanType, true);
        defineConversionOperator(floatType, stringType, true);
        defineConversionOperator(floatType, byteType, true);
        defineConversionOperator(decimalType, intType, false);
        defineConversionOperator(decimalType, floatType, true);
        defineConversionOperator(decimalType, booleanType, true);
        defineConversionOperator(decimalType, stringType, true);
        defineConversionOperator(decimalType, byteType, true);
        defineConversionOperator(byteType, intType, true);
        defineConversionOperator(byteType, floatType, true);
        defineConversionOperator(byteType, decimalType, true);
        defineConversionOperator(stringType, floatType, false);
        defineConversionOperator(stringType, decimalType, false);
        defineConversionOperator(stringType, intType, false);
        defineConversionOperator(stringType, booleanType, true);
        defineConversionOperator(booleanType, stringType, true);
        defineConversionOperator(booleanType, intType, true);
        defineConversionOperator(booleanType, floatType, true);
        defineConversionOperator(booleanType, decimalType, true);
        defineConversionOperator(tableType, xmlType, false);
        defineConversionOperator(tableType, jsonType, false);
        defineConversionOperator(xmlType, stringType, true);
    }

    public void defineBinaryOperator(OperatorKind kind,
                                     BType lhsType,
                                     BType rhsType,
                                     BType retType) {
        List<BType> paramTypes = Lists.of(lhsType, rhsType);
        defineOperator(names.fromString(kind.value()), paramTypes, retType);
    }

    private void defineUnaryOperator(OperatorKind kind,
                                     BType type,
                                     BType retType) {
        List<BType> paramTypes = Lists.of(type);
        defineOperator(names.fromString(kind.value()), paramTypes, retType);
    }

    private void defineImplicitCastOperator(BType sourceType,
                                            BType targetType,
                                            boolean safe) {

        defineCastOperator(sourceType, targetType, true, safe);
    }

    private void defineCastOperator(BType sourceType,
                                    BType targetType,
                                    boolean safe) {

        defineCastOperator(sourceType, targetType, false, safe);
    }

    private void defineConversionOperator(BType sourceType,
                                          BType targetType,
                                          boolean safe) {
        List<BType> paramTypes = Lists.of(sourceType, targetType);
        BType retType;
        if (safe) {
            retType = targetType;
        } else {
            if (targetType.tag == TypeTags.UNION) {
                BUnionType unionType = (BUnionType) targetType;
                unionType.add(this.errorType);
                retType = targetType;
            } else {
                retType = BUnionType.create(null, targetType, errorType);
            }
        }
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        BConversionOperatorSymbol symbol = new BConversionOperatorSymbol(this.rootPkgSymbol.pkgID, opType, sourceType,
                this.rootPkgSymbol, safe);
        rootScope.define(symbol.name, symbol);
    }

    private void defineCastOperator(BType sourceType,
                                    BType targetType,
                                    boolean implicit,
                                    boolean safe) {
        List<BType> paramTypes = Lists.of(sourceType, targetType);
        BType retType;
        if (safe) {
            retType = targetType;
        } else {
            if (targetType.tag == TypeTags.UNION) {
                BUnionType unionType = (BUnionType) targetType;
                unionType.add(this.errorType);
                retType = targetType;
            } else {
                retType = BUnionType.create(null, targetType, errorType);
            }
        }
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        BCastOperatorSymbol symbol = new BCastOperatorSymbol(this.rootPkgSymbol.pkgID, opType, sourceType,
                this.rootPkgSymbol, implicit, safe);
        rootScope.define(symbol.name, symbol);
    }

    private void defineOperator(Name name,
                                List<BType> paramTypes,
                                BType retType) {
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        BOperatorSymbol symbol = new BOperatorSymbol(name, rootPkgSymbol.pkgID, opType, rootPkgSymbol);
        rootScope.define(name, symbol);
    }
}

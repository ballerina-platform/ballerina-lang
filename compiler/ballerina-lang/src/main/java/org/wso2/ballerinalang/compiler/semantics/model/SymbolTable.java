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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
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
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
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

    public static final PackageID UTILS = new PackageID(Names.BUILTIN_ORG, Names.UTILS_PACKAGE, Names.EMPTY);

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
    public BType streamType = new BStreamType(TypeTags.STREAM, pureType, null);;
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
    public BPackageSymbol utilsPackageSymbol;

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
        defineBinaryOperator(OperatorKind.ADD, xmlType, xmlType, xmlType, InstructionCodes.XMLADD);
        defineBinaryOperator(OperatorKind.ADD, xmlType, stringType, xmlType, InstructionCodes.XMLADD);
        defineBinaryOperator(OperatorKind.ADD, stringType, stringType, stringType, InstructionCodes.SADD);
        defineBinaryOperator(OperatorKind.ADD, stringType, xmlType, xmlType, InstructionCodes.XMLADD);
        defineBinaryOperator(OperatorKind.ADD, floatType, floatType, floatType, InstructionCodes.FADD);
        defineBinaryOperator(OperatorKind.ADD, decimalType, decimalType, decimalType, InstructionCodes.DADD);
        defineBinaryOperator(OperatorKind.ADD, intType, intType, intType, InstructionCodes.IADD);
        defineBinaryOperator(OperatorKind.ADD, intType, byteType, intType, InstructionCodes.IADD);
        defineBinaryOperator(OperatorKind.ADD, byteType, intType, intType, InstructionCodes.IADD);
        defineBinaryOperator(OperatorKind.ADD, byteType, byteType, intType, InstructionCodes.IADD);
        defineBinaryOperator(OperatorKind.ADD, intType, floatType, floatType, InstructionCodes.FADD);
        defineBinaryOperator(OperatorKind.ADD, floatType, intType, floatType, InstructionCodes.FADD);
        defineBinaryOperator(OperatorKind.ADD, intType, decimalType, decimalType, InstructionCodes.DADD);
        defineBinaryOperator(OperatorKind.ADD, decimalType, intType, decimalType, InstructionCodes.DADD);
        defineBinaryOperator(OperatorKind.ADD, floatType, decimalType, decimalType, InstructionCodes.DADD);
        defineBinaryOperator(OperatorKind.ADD, decimalType, floatType, decimalType, InstructionCodes.DADD);
        defineBinaryOperator(OperatorKind.SUB, floatType, floatType, floatType, InstructionCodes.FSUB);
        defineBinaryOperator(OperatorKind.SUB, decimalType, decimalType, decimalType, InstructionCodes.DSUB);
        defineBinaryOperator(OperatorKind.SUB, intType, intType, intType, InstructionCodes.ISUB);
        defineBinaryOperator(OperatorKind.SUB, intType, byteType, intType, InstructionCodes.ISUB);
        defineBinaryOperator(OperatorKind.SUB, byteType, intType, intType, InstructionCodes.ISUB);
        defineBinaryOperator(OperatorKind.SUB, byteType, byteType, intType, InstructionCodes.ISUB);
        defineBinaryOperator(OperatorKind.SUB, floatType, intType, floatType, InstructionCodes.FSUB);
        defineBinaryOperator(OperatorKind.SUB, intType, floatType, floatType, InstructionCodes.FSUB);
        defineBinaryOperator(OperatorKind.SUB, decimalType, intType, decimalType, InstructionCodes.DSUB);
        defineBinaryOperator(OperatorKind.SUB, intType, decimalType, decimalType, InstructionCodes.DSUB);
        defineBinaryOperator(OperatorKind.SUB, decimalType, floatType, decimalType, InstructionCodes.DSUB);
        defineBinaryOperator(OperatorKind.SUB, floatType, decimalType, decimalType, InstructionCodes.DSUB);
        defineBinaryOperator(OperatorKind.DIV, floatType, floatType, floatType, InstructionCodes.FDIV);
        defineBinaryOperator(OperatorKind.DIV, decimalType, decimalType, decimalType, InstructionCodes.DDIV);
        defineBinaryOperator(OperatorKind.DIV, intType, intType, intType, InstructionCodes.IDIV);
        defineBinaryOperator(OperatorKind.DIV, intType, byteType, intType, InstructionCodes.IDIV);
        defineBinaryOperator(OperatorKind.DIV, byteType, intType, intType, InstructionCodes.IDIV);
        defineBinaryOperator(OperatorKind.DIV, byteType, byteType, intType, InstructionCodes.IDIV);
        defineBinaryOperator(OperatorKind.DIV, intType, floatType, floatType, InstructionCodes.FDIV);
        defineBinaryOperator(OperatorKind.DIV, floatType, intType, floatType, InstructionCodes.FDIV);
        defineBinaryOperator(OperatorKind.DIV, intType, decimalType, decimalType, InstructionCodes.DDIV);
        defineBinaryOperator(OperatorKind.DIV, decimalType, intType, decimalType, InstructionCodes.DDIV);
        defineBinaryOperator(OperatorKind.DIV, floatType, decimalType, decimalType, InstructionCodes.DDIV);
        defineBinaryOperator(OperatorKind.DIV, decimalType, floatType, decimalType, InstructionCodes.DDIV);
        defineBinaryOperator(OperatorKind.MUL, floatType, floatType, floatType, InstructionCodes.FMUL);
        defineBinaryOperator(OperatorKind.MUL, decimalType, decimalType, decimalType, InstructionCodes.DMUL);
        defineBinaryOperator(OperatorKind.MUL, intType, intType, intType, InstructionCodes.IMUL);
        defineBinaryOperator(OperatorKind.MUL, intType, byteType, intType, InstructionCodes.IMUL);
        defineBinaryOperator(OperatorKind.MUL, byteType, intType, intType, InstructionCodes.IMUL);
        defineBinaryOperator(OperatorKind.MUL, byteType, byteType, intType, InstructionCodes.IMUL);
        defineBinaryOperator(OperatorKind.MUL, floatType, intType, floatType, InstructionCodes.FMUL);
        defineBinaryOperator(OperatorKind.MUL, intType, floatType, floatType, InstructionCodes.FMUL);
        defineBinaryOperator(OperatorKind.MUL, decimalType, intType, decimalType, InstructionCodes.DMUL);
        defineBinaryOperator(OperatorKind.MUL, intType, decimalType, decimalType, InstructionCodes.DMUL);
        defineBinaryOperator(OperatorKind.MUL, decimalType, floatType, decimalType, InstructionCodes.DMUL);
        defineBinaryOperator(OperatorKind.MUL, floatType, decimalType, decimalType, InstructionCodes.DMUL);
        defineBinaryOperator(OperatorKind.MOD, floatType, floatType, floatType, InstructionCodes.FMOD);
        defineBinaryOperator(OperatorKind.MOD, decimalType, decimalType, decimalType, InstructionCodes.DMOD);
        defineBinaryOperator(OperatorKind.MOD, intType, intType, intType, InstructionCodes.IMOD);
        defineBinaryOperator(OperatorKind.MOD, intType, byteType, intType, InstructionCodes.IMOD);
        defineBinaryOperator(OperatorKind.MOD, byteType, intType, intType, InstructionCodes.IMOD);
        defineBinaryOperator(OperatorKind.MOD, byteType, byteType, intType, InstructionCodes.IMOD);
        defineBinaryOperator(OperatorKind.MOD, floatType, intType, floatType, InstructionCodes.FMOD);
        defineBinaryOperator(OperatorKind.MOD, intType, floatType, floatType, InstructionCodes.FMOD);
        defineBinaryOperator(OperatorKind.MOD, decimalType, intType, decimalType, InstructionCodes.DMOD);
        defineBinaryOperator(OperatorKind.MOD, intType, decimalType, decimalType, InstructionCodes.DMOD);
        defineBinaryOperator(OperatorKind.BITWISE_AND, byteType, byteType, byteType, InstructionCodes.IAND);
        defineBinaryOperator(OperatorKind.BITWISE_AND, byteType, intType, byteType, InstructionCodes.IAND);
        defineBinaryOperator(OperatorKind.BITWISE_AND, intType, byteType, byteType, InstructionCodes.IAND);
        defineBinaryOperator(OperatorKind.BITWISE_AND, intType, intType, intType, InstructionCodes.IAND);
        defineBinaryOperator(OperatorKind.BITWISE_OR, byteType, byteType, byteType, InstructionCodes.IOR);
        defineBinaryOperator(OperatorKind.BITWISE_OR, byteType, intType, intType, InstructionCodes.IOR);
        defineBinaryOperator(OperatorKind.BITWISE_OR, intType, byteType, intType, InstructionCodes.IOR);
        defineBinaryOperator(OperatorKind.BITWISE_OR, intType, intType, intType, InstructionCodes.IOR);
        defineBinaryOperator(OperatorKind.BITWISE_XOR, byteType, byteType, byteType, InstructionCodes.IXOR);
        defineBinaryOperator(OperatorKind.BITWISE_XOR, byteType, intType, intType, InstructionCodes.IXOR);
        defineBinaryOperator(OperatorKind.BITWISE_XOR, intType, byteType, intType, InstructionCodes.IXOR);
        defineBinaryOperator(OperatorKind.BITWISE_XOR, intType, intType, intType, InstructionCodes.IXOR);
        defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, intType, intType, intType, InstructionCodes.ILSHIFT);
        defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, intType, byteType, intType, InstructionCodes.ILSHIFT);
        defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, byteType, byteType, intType, InstructionCodes.BILSHIFT);
        defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, byteType, intType, intType, InstructionCodes.BILSHIFT);
        defineBinaryOperator(OperatorKind.BITWISE_RIGHT_SHIFT, intType, intType, intType, InstructionCodes.IRSHIFT);
        defineBinaryOperator(OperatorKind.BITWISE_RIGHT_SHIFT, intType, byteType, intType, InstructionCodes.IRSHIFT);
        defineBinaryOperator(OperatorKind.BITWISE_RIGHT_SHIFT, byteType, byteType, byteType, InstructionCodes.BIRSHIFT);
        defineBinaryOperator(OperatorKind.BITWISE_RIGHT_SHIFT, byteType, intType, byteType, InstructionCodes.BIRSHIFT);
        defineBinaryOperator(OperatorKind.BITWISE_UNSIGNED_RIGHT_SHIFT, intType, intType, intType,
                InstructionCodes.IURSHIFT);
        defineBinaryOperator(OperatorKind.BITWISE_UNSIGNED_RIGHT_SHIFT, intType, byteType, intType,
                InstructionCodes.IURSHIFT);

        // Binary equality operators ==, !=
        defineBinaryOperator(OperatorKind.EQUAL, intType, intType, booleanType, InstructionCodes.IEQ);
        defineBinaryOperator(OperatorKind.EQUAL, byteType, byteType, booleanType, InstructionCodes.IEQ);
        defineBinaryOperator(OperatorKind.EQUAL, floatType, floatType, booleanType, InstructionCodes.FEQ);
        defineBinaryOperator(OperatorKind.EQUAL, decimalType, decimalType, booleanType, InstructionCodes.DEQ);
        defineBinaryOperator(OperatorKind.EQUAL, booleanType, booleanType, booleanType, InstructionCodes.BEQ);
        defineBinaryOperator(OperatorKind.EQUAL, stringType, stringType, booleanType, InstructionCodes.SEQ);
        defineBinaryOperator(OperatorKind.EQUAL, intType, byteType, booleanType, InstructionCodes.IEQ);
        defineBinaryOperator(OperatorKind.EQUAL, byteType, intType, booleanType, InstructionCodes.IEQ);
        defineBinaryOperator(OperatorKind.EQUAL, jsonType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, jsonType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, anyType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, anyType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, anydataType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, anydataType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.EQUAL, nilType, nilType, booleanType, InstructionCodes.REQ);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, intType, intType, booleanType, InstructionCodes.INE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, byteType, byteType, booleanType, InstructionCodes.INE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, floatType, floatType, booleanType, InstructionCodes.FNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, decimalType, decimalType, booleanType, InstructionCodes.DNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, booleanType, booleanType, booleanType, InstructionCodes.BNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, stringType, stringType, booleanType, InstructionCodes.SNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, intType, byteType, booleanType, InstructionCodes.INE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, byteType, intType, booleanType, InstructionCodes.INE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, jsonType, nilType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, jsonType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, anyType, nilType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, anyType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, anydataType, nilType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, anydataType, booleanType, InstructionCodes.RNE);
        defineBinaryOperator(OperatorKind.NOT_EQUAL, nilType, nilType, booleanType, InstructionCodes.RNE);

        // Binary reference equality operators ===, !==
        defineBinaryOperator(OperatorKind.REF_EQUAL, intType, intType, booleanType, InstructionCodes.IEQ);
        defineBinaryOperator(OperatorKind.REF_EQUAL, byteType, byteType, booleanType, InstructionCodes.IEQ);
        defineBinaryOperator(OperatorKind.REF_EQUAL, floatType, floatType, booleanType, InstructionCodes.FEQ);
        defineBinaryOperator(OperatorKind.REF_EQUAL, decimalType, decimalType, booleanType, InstructionCodes.DEQ);
        defineBinaryOperator(OperatorKind.REF_EQUAL, booleanType, booleanType, booleanType, InstructionCodes.BEQ);
        defineBinaryOperator(OperatorKind.REF_EQUAL, stringType, stringType, booleanType, InstructionCodes.SEQ);
        defineBinaryOperator(OperatorKind.REF_EQUAL, intType, byteType, booleanType, InstructionCodes.IEQ);
        defineBinaryOperator(OperatorKind.REF_EQUAL, byteType, intType, booleanType, InstructionCodes.IEQ);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, intType, intType, booleanType, InstructionCodes.INE);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, byteType, byteType, booleanType, InstructionCodes.INE);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, floatType, floatType, booleanType, InstructionCodes.FNE);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, decimalType, decimalType, booleanType, InstructionCodes.DNE);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, booleanType, booleanType, booleanType, InstructionCodes.BNE);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, stringType, stringType, booleanType, InstructionCodes.SNE);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, intType, byteType, booleanType, InstructionCodes.INE);
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, byteType, intType, booleanType, InstructionCodes.INE);

        // Binary comparison operators <=, <, >=, >
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, intType, booleanType, InstructionCodes.ILT);
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, floatType, booleanType, InstructionCodes.FLT);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, intType, booleanType, InstructionCodes.FLT);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, floatType, booleanType, InstructionCodes.FLT);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, decimalType, booleanType, InstructionCodes.DLT);
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, decimalType, booleanType, InstructionCodes.DLT);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, intType, booleanType, InstructionCodes.DLT);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, decimalType, booleanType, InstructionCodes.DLT);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, floatType, booleanType, InstructionCodes.DLT);

        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, intType, booleanType, InstructionCodes.ILE);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, intType, booleanType, InstructionCodes.FLE);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, floatType, booleanType, InstructionCodes.FLE);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, floatType, booleanType, InstructionCodes.FLE);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, decimalType, booleanType, InstructionCodes.DLE);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, decimalType, booleanType, InstructionCodes.DLE);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, intType, booleanType, InstructionCodes.DLE);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, decimalType, booleanType, InstructionCodes.DLE);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, floatType, booleanType, InstructionCodes.DLE);

        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, intType, booleanType, InstructionCodes.IGT);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, intType, booleanType, InstructionCodes.FGT);
        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, floatType, booleanType, InstructionCodes.FGT);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, floatType, booleanType, InstructionCodes.FGT);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, decimalType, booleanType, InstructionCodes.DGT);
        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, decimalType, booleanType, InstructionCodes.DGT);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, intType, booleanType, InstructionCodes.DGT);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, decimalType, booleanType, InstructionCodes.DGT);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, floatType, booleanType, InstructionCodes.DGT);

        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, intType, booleanType, InstructionCodes.IGE);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, intType, booleanType, InstructionCodes.FGE);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, floatType, booleanType, InstructionCodes.FGE);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, floatType, booleanType, InstructionCodes.FGE);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, decimalType, booleanType, InstructionCodes.DGE);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, decimalType, booleanType, InstructionCodes.DGE);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, intType, booleanType, InstructionCodes.DGE);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, decimalType, booleanType, InstructionCodes.DGE);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, floatType, booleanType, InstructionCodes.DGE);

        defineBinaryOperator(OperatorKind.AND, booleanType, booleanType, booleanType, -1);
        defineBinaryOperator(OperatorKind.OR, booleanType, booleanType, booleanType, -1);

        // Unary operator symbols
        defineUnaryOperator(OperatorKind.ADD, floatType, floatType, -1);
        defineUnaryOperator(OperatorKind.ADD, decimalType, decimalType, -1);
        defineUnaryOperator(OperatorKind.ADD, intType, intType, -1);

        defineUnaryOperator(OperatorKind.SUB, floatType, floatType, InstructionCodes.FNEG);
        defineUnaryOperator(OperatorKind.SUB, decimalType, decimalType, InstructionCodes.DNEG);
        defineUnaryOperator(OperatorKind.SUB, intType, intType, InstructionCodes.INEG);

        defineUnaryOperator(OperatorKind.NOT, booleanType, booleanType, InstructionCodes.BNOT);
        defineUnaryOperator(OperatorKind.BITWISE_COMPLEMENT, byteType, byteType, -1);
        defineUnaryOperator(OperatorKind.BITWISE_COMPLEMENT, intType, intType, -1);

        defineConversionOperators();

    }

    private void defineConversionOperators() {
        // Define both implicit and explicit conversion operators
        defineImplicitCastOperator(intType, jsonType, true, InstructionCodes.I2ANY);
        defineImplicitCastOperator(intType, anyType, true, InstructionCodes.I2ANY);
        defineImplicitCastOperator(byteType, anyType, true, InstructionCodes.BI2ANY);
        defineImplicitCastOperator(floatType, jsonType, true, InstructionCodes.F2ANY);
        defineImplicitCastOperator(floatType, anyType, true, InstructionCodes.F2ANY);
        defineImplicitCastOperator(decimalType, jsonType, true, InstructionCodes.NOP);
        defineImplicitCastOperator(decimalType, anyType, true, InstructionCodes.NOP);
        defineImplicitCastOperator(stringType, jsonType, true, InstructionCodes.S2ANY);
        defineImplicitCastOperator(stringType, anyType, true, InstructionCodes.S2ANY);
        defineImplicitCastOperator(booleanType, jsonType, true, InstructionCodes.B2ANY);
        defineImplicitCastOperator(booleanType, anyType, true, InstructionCodes.B2ANY);
        defineImplicitCastOperator(typeDesc, anyType, true, InstructionCodes.NOP);
        defineImplicitCastOperator(intType, anydataType, true, InstructionCodes.I2ANY);
        defineImplicitCastOperator(byteType, anydataType, true, InstructionCodes.BI2ANY);
        defineImplicitCastOperator(floatType, anydataType, true, InstructionCodes.F2ANY);
        defineImplicitCastOperator(decimalType, anydataType, true, InstructionCodes.NOP);
        defineImplicitCastOperator(stringType, anydataType, true, InstructionCodes.S2ANY);
        defineImplicitCastOperator(booleanType, anydataType, true, InstructionCodes.B2ANY);

        // Define explicit conversion operators
        defineCastOperator(anyType, intType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(anyType, byteType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(anyType, floatType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(anyType, decimalType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(anyType, stringType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(anyType, booleanType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(anyType, typeDesc, false, InstructionCodes.ANY2TYPE);
        defineCastOperator(anyType, jsonType, false, InstructionCodes.ANY2JSON);
        defineCastOperator(anyType, xmlType, false, InstructionCodes.ANY2XML);
        defineCastOperator(anyType, mapType, false, InstructionCodes.ANY2MAP);
        defineCastOperator(anyType, tableType, false, InstructionCodes.ANY2DT);
        defineCastOperator(anyType, streamType, false, InstructionCodes.ANY2STM);
        defineCastOperator(anyType, handleType, false, InstructionCodes.NOP); // BVM instructions not used anymore
        defineCastOperator(anydataType, intType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(anydataType, byteType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(anydataType, floatType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(anydataType, decimalType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(anydataType, stringType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(anydataType, booleanType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(anydataType, jsonType, false, InstructionCodes.ANY2JSON);
        defineCastOperator(anydataType, xmlType, false, InstructionCodes.ANY2XML);
        defineCastOperator(anydataType, tableType, false, InstructionCodes.ANY2DT);

        defineCastOperator(jsonType, intType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(jsonType, floatType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(jsonType, decimalType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(jsonType, stringType, false, InstructionCodes.CHECKCAST);
        defineCastOperator(jsonType, booleanType, false, InstructionCodes.CHECKCAST);

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
        
//        defineConversionOperator(anyType, stringType, true, InstructionCodes.ANY2SCONV);
//        defineConversionOperator(anydataType, stringType, true, InstructionCodes.ANY2SCONV);
        defineConversionOperator(intType, floatType, true, InstructionCodes.I2F);
        defineConversionOperator(intType, decimalType, true, InstructionCodes.I2D);
        defineConversionOperator(intType, booleanType, true, InstructionCodes.I2B);
        defineConversionOperator(intType, stringType, true, InstructionCodes.I2S);
        defineConversionOperator(intType, byteType, false, InstructionCodes.I2BI);
        defineConversionOperator(floatType, intType, false, InstructionCodes.F2I);
        defineConversionOperator(floatType, decimalType, true, InstructionCodes.F2D);
        defineConversionOperator(floatType, booleanType, true, InstructionCodes.F2B);
        defineConversionOperator(floatType, stringType, true, InstructionCodes.F2S);
        defineConversionOperator(floatType, byteType, true, InstructionCodes.F2BI);
        defineConversionOperator(decimalType, intType, false, InstructionCodes.D2I);
        defineConversionOperator(decimalType, floatType, true, InstructionCodes.D2F);
        defineConversionOperator(decimalType, booleanType, true, InstructionCodes.D2B);
        defineConversionOperator(decimalType, stringType, true, InstructionCodes.D2S);
        defineConversionOperator(decimalType, byteType, true, InstructionCodes.D2BI);
        defineConversionOperator(byteType, intType, true, InstructionCodes.NOP);
        defineConversionOperator(byteType, floatType, true, InstructionCodes.I2F);
        defineConversionOperator(byteType, decimalType, true, InstructionCodes.I2D);
        defineConversionOperator(stringType, floatType, false, InstructionCodes.S2F);
        defineConversionOperator(stringType, decimalType, false, InstructionCodes.S2D);
        defineConversionOperator(stringType, intType, false, InstructionCodes.S2I);
        defineConversionOperator(stringType, booleanType, true, InstructionCodes.S2B);
        defineConversionOperator(booleanType, stringType, true, InstructionCodes.B2S);
        defineConversionOperator(booleanType, intType, true, InstructionCodes.B2I);
        defineConversionOperator(booleanType, floatType, true, InstructionCodes.B2F);
        defineConversionOperator(booleanType, decimalType, true, InstructionCodes.B2D);
        defineConversionOperator(tableType, xmlType, false, InstructionCodes.DT2XML);
        defineConversionOperator(tableType, jsonType, false, InstructionCodes.DT2JSON);
//        defineConversionOperator(stringType, xmlType, false, InstructionCodes.S2XML);
        defineConversionOperator(xmlType, stringType, true, InstructionCodes.XML2S);
//        defineConversionOperator(stringType, jsonType, false, InstructionCodes.S2JSONX);
    }

    public void defineBinaryOperator(OperatorKind kind,
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

    private void defineImplicitCastOperator(BType sourceType,
                                            BType targetType,
                                            boolean safe,
                                            int opcode) {
        defineCastOperator(sourceType, targetType, true, safe, opcode);
    }

    private void defineCastOperator(BType sourceType,
                                    BType targetType,
                                    boolean safe,
                                    int opcode) {
        defineCastOperator(sourceType, targetType, false, safe, opcode);
    }

    private void defineConversionOperator(BType sourceType,
                                          BType targetType,
                                          boolean safe) {
        defineConversionOperator(sourceType, targetType, safe, InstructionCodes.NOP);
    }

    private void defineConversionOperator(BType sourceType,
                                    BType targetType,
                                    boolean safe,
                                    int opcode) {
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
                                                                         this.rootPkgSymbol, opcode, safe);
        rootScope.define(symbol.name, symbol);
    }

    private void defineCastOperator(BType sourceType,
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
                unionType.add(this.errorType);
                retType = targetType;
            } else {
                retType = BUnionType.create(null, targetType, errorType);
            }
        }
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        BCastOperatorSymbol symbol = new BCastOperatorSymbol(this.rootPkgSymbol.pkgID, opType, sourceType,
                                                             this.rootPkgSymbol, implicit, safe, opcode);
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

    public BOperatorSymbol createOperator(Name name,
                                          List<BType> paramTypes,
                                          BType retType,
                                          int opcode) {
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        BOperatorSymbol symbol = new BOperatorSymbol(name, rootPkgSymbol.pkgID, opType, rootPkgSymbol, opcode);

        List<BVarSymbol> symbolList = new ArrayList<>();
        for (BType type : paramTypes) {
            BVarSymbol targetVarSymbol = new BVarSymbol(0, new Name("_"), rootPkgSymbol.pkgID,
                    type, rootPkgSymbol);

            symbolList.add(targetVarSymbol);
        }

        symbol.params = symbolList;
        symbol.retType = retType;
        rootScope.define(name, symbol);
        return symbol;
    }
}

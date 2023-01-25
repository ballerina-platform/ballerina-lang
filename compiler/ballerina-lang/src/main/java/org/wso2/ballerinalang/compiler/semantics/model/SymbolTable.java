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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstructorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNeverType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNoType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BReadonlyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRegexpType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStringSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ImmutableTypeCloner;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.model.symbols.SymbolOrigin.BUILTIN;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

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
    public static final Integer SIGNED32_MAX_VALUE = 2147483647;
    public static final Integer SIGNED32_MIN_VALUE = -2147483648;
    public static final Integer SIGNED16_MAX_VALUE = 32767;
    public static final Integer SIGNED16_MIN_VALUE = -32768;
    public static final Integer SIGNED8_MAX_VALUE = 127;
    public static final Integer SIGNED8_MIN_VALUE = -128;
    public static final Long UNSIGNED32_MAX_VALUE = 4294967295L;
    public static final Integer UNSIGNED16_MAX_VALUE = 65535;
    public static final Integer UNSIGNED8_MAX_VALUE = 255;

    public final Location builtinPos;
    public final BLangPackage rootPkgNode;
    public final BPackageSymbol rootPkgSymbol;
    public final BSymbol notFoundSymbol;
    public final Scope rootScope;

    public final BType noType = new BNoType(TypeTags.NONE);
    public final BType nilType = new BNilType();
    public final BType neverType = new BNeverType();
    public final BType intType = new BType(TypeTags.INT, null, Flags.READONLY);
    public final BType byteType = new BType(TypeTags.BYTE, null, Flags.READONLY);
    public final BType floatType = new BType(TypeTags.FLOAT, null, Flags.READONLY);
    public final BType decimalType = new BType(TypeTags.DECIMAL, null, Flags.READONLY);
    public final BType stringType = new BType(TypeTags.STRING, null, Flags.READONLY);
    public final BType booleanType = new BType(TypeTags.BOOLEAN, null, Flags.READONLY);

    public final BType anyType = new BAnyType(TypeTags.ANY, null);
    public final BMapType mapType = new BMapType(TypeTags.MAP, anyType, null);
    public final BMapType mapStringType = new BMapType(TypeTags.MAP, stringType, null);

    public final BFutureType futureType = new BFutureType(TypeTags.FUTURE, nilType, null);
    public final BArrayType arrayType = new BArrayType(anyType);
    public final BArrayType arrayStringType = new BArrayType(stringType);

    public final BType tupleType = new BTupleType(Lists.of(noType));
    public final BType recordType = new BRecordType(null);
    public final BType stringArrayType = new BArrayType(stringType);
    public final BType handleType = new BHandleType(TypeTags.HANDLE, null);
    public final BTypedescType typeDesc = new BTypedescType(this.anyType, null);
    public final BType readonlyType = new BReadonlyType(TypeTags.READONLY, null);
    public final BType pathParamAllowedType = BUnionType.create(null,
            intType, stringType, floatType, booleanType, decimalType);
    public final BIntersectionType anyAndReadonly;
    public BUnionType anyAndReadonlyOrError;

    public final BType errorIntersectionType = new BErrorType(null, null);

    public final BType semanticError = new BType(TypeTags.SEMANTIC_ERROR, null);
    public final BType nullSet = new BType(TypeTags.NULL_SET, null);
    public final BType invokableType = new BInvokableType(null, null, null, null);

    public BConstructorSymbol errorConstructor;
    public BUnionType anyOrErrorType;
    public BUnionType pureType;
    public BUnionType errorOrNilType;
    public BFiniteType trueType;
    public BFiniteType falseType;
    public BObjectType intRangeType;
    public BMapType mapAllType;
    public BArrayType arrayAllType;
    public BObjectType rawTemplateType;
    public BObjectType iterableType;

    // builtin subtypes
    public final BIntSubType signed32IntType = new BIntSubType(TypeTags.SIGNED32_INT, Names.SIGNED32);
    public final BIntSubType signed16IntType = new BIntSubType(TypeTags.SIGNED16_INT, Names.SIGNED16);
    public final BIntSubType signed8IntType = new BIntSubType(TypeTags.SIGNED8_INT, Names.SIGNED8);
    public final BIntSubType unsigned32IntType = new BIntSubType(TypeTags.UNSIGNED32_INT, Names.UNSIGNED32);
    public final BIntSubType unsigned16IntType = new BIntSubType(TypeTags.UNSIGNED16_INT, Names.UNSIGNED16);
    public final BIntSubType unsigned8IntType = new BIntSubType(TypeTags.UNSIGNED8_INT, Names.UNSIGNED8);
    public final BStringSubType charStringType = new BStringSubType(TypeTags.CHAR_STRING, Names.CHAR);
    public final BXMLSubType xmlElementType = new BXMLSubType(TypeTags.XML_ELEMENT, Names.XML_ELEMENT);
    public final BXMLSubType xmlPIType = new BXMLSubType(TypeTags.XML_PI, Names.XML_PI);
    public final BXMLSubType xmlCommentType = new BXMLSubType(TypeTags.XML_COMMENT, Names.XML_COMMENT);
    public final BXMLSubType xmlTextType = new BXMLSubType(TypeTags.XML_TEXT, Names.XML_TEXT, Flags.READONLY);
    public final BRegexpType regExpType = new BRegexpType(TypeTags.REGEXP, Names.REGEXP);
    public final BType xmlNeverType = new BXMLType(neverType,  null);
    public final BType xmlElementSeqType = new BXMLType(xmlElementType, null);

    public final BType xmlType = new BXMLType(BUnionType.create(null, xmlElementType, xmlCommentType,
            xmlPIType, xmlTextType),  null);

    public BAnydataType anydataType;
    public BArrayType arrayAnydataType;
    public BMapType mapAnydataType;
    public BType anydataOrReadonly;
    public BType streamType;
    public BType tableType;

    public BArrayType arrayJsonType;
    public BMapType mapJsonType;
    public BJSONType jsonType;

    public BUnionType cloneableType;
    public BMapType detailType;
    public BErrorType errorType;

    public BPackageSymbol langInternalModuleSymbol;
    public BPackageSymbol langAnnotationModuleSymbol;
    public BPackageSymbol langJavaModuleSymbol;
    public BPackageSymbol langArrayModuleSymbol;
    public BPackageSymbol langDecimalModuleSymbol;
    public BPackageSymbol langErrorModuleSymbol;
    public BPackageSymbol langFloatModuleSymbol;
    public BPackageSymbol langFutureModuleSymbol;
    public BPackageSymbol langFunctionModuleSymbol;
    public BPackageSymbol langIntModuleSymbol;
    public BPackageSymbol langMapModuleSymbol;
    public BPackageSymbol langObjectModuleSymbol;
    public BPackageSymbol langStreamModuleSymbol;
    public BPackageSymbol langStringModuleSymbol;
    public BPackageSymbol langTableModuleSymbol;
    public BPackageSymbol langTypedescModuleSymbol;
    public BPackageSymbol langValueModuleSymbol;
    public BPackageSymbol langXmlModuleSymbol;
    public BPackageSymbol langBooleanModuleSymbol;
    public BPackageSymbol langQueryModuleSymbol;
    public BPackageSymbol langRuntimeModuleSymbol;
    public BPackageSymbol langTransactionModuleSymbol;
    public BPackageSymbol internalTransactionModuleSymbol;

    public BPackageSymbol langRegexpModuleSymbol;

    private Names names;
    private Types types;
    public Map<BPackageSymbol, SymbolEnv> pkgEnvMap = new HashMap<>();
    public Map<Name, BPackageSymbol> predeclaredModules = new HashMap<>();
    public Map<String, Map<SelectivelyImmutableReferenceType, BIntersectionType>> immutableTypeMaps = new HashMap<>();

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
        this.types = Types.getInstance(context);

        this.rootPkgNode = (BLangPackage) TreeBuilder.createPackageNode();
        this.rootPkgSymbol = new BPackageSymbol(PackageID.ANNOTATIONS, null, null, BUILTIN);
        this.builtinPos = new BLangDiagnosticLocation(Names.EMPTY.value, -1, -1,
                                            -1, -1);
        this.rootPkgNode.pos = this.builtinPos;
        this.rootPkgNode.symbol = this.rootPkgSymbol;
        this.rootScope = new Scope(rootPkgSymbol);
        this.rootPkgSymbol.scope = this.rootScope;
        this.rootPkgSymbol.pos = this.builtinPos;

        this.notFoundSymbol = new BSymbol(SymTag.NIL, Flags.PUBLIC, Names.INVALID, rootPkgSymbol.pkgID, noType,
                                          rootPkgSymbol, builtinPos, SymbolOrigin.VIRTUAL);
        // Initialize built-in types in Ballerina
        initializeType(intType, TypeKind.INT.typeName(), BUILTIN);
        initializeType(byteType, TypeKind.BYTE.typeName(), BUILTIN);
        initializeType(floatType, TypeKind.FLOAT.typeName(), BUILTIN);
        initializeType(decimalType, TypeKind.DECIMAL.typeName(), BUILTIN);
        initializeType(stringType, TypeKind.STRING.typeName(), BUILTIN);
        initializeType(booleanType, TypeKind.BOOLEAN.typeName(), BUILTIN);
        initializeType(xmlType, TypeKind.XML.typeName(), BUILTIN);
        initializeType(mapType, TypeKind.MAP.typeName(), VIRTUAL);
        initializeType(mapStringType, TypeKind.MAP.typeName(), VIRTUAL);
        initializeType(futureType, TypeKind.FUTURE.typeName(), BUILTIN);
        initializeType(anyType, TypeKind.ANY.typeName(), BUILTIN);
        initializeType(nilType, TypeKind.NIL.typeName(), BUILTIN);
        initializeType(neverType, TypeKind.NEVER.typeName(), BUILTIN);
        initializeType(handleType, TypeKind.HANDLE.typeName(), BUILTIN);
        initializeType(typeDesc, TypeKind.TYPEDESC.typeName(), BUILTIN);
        initializeType(readonlyType, TypeKind.READONLY.typeName(), BUILTIN);

        // Define subtypes
        initializeTSymbol(signed32IntType, Names.SIGNED32, PackageID.INT);
        initializeTSymbol(signed16IntType, Names.SIGNED16, PackageID.INT);
        initializeTSymbol(signed8IntType, Names.SIGNED8, PackageID.INT);
        initializeTSymbol(unsigned32IntType, Names.UNSIGNED32, PackageID.INT);
        initializeTSymbol(unsigned16IntType, Names.UNSIGNED16, PackageID.INT);
        initializeTSymbol(unsigned8IntType, Names.UNSIGNED8, PackageID.INT);
        initializeTSymbol(charStringType, Names.CHAR, PackageID.STRING);
        initializeTSymbol(xmlElementType, Names.XML_ELEMENT, PackageID.XML);
        initializeTSymbol(xmlPIType, Names.XML_PI, PackageID.XML);
        initializeTSymbol(xmlCommentType, Names.XML_COMMENT, PackageID.XML);
        initializeTSymbol(xmlTextType, Names.XML_TEXT, PackageID.XML);
        initializeTSymbol(regExpType, Names.REGEXP, PackageID.REGEXP);

        BLangLiteral trueLiteral = new BLangLiteral();
        trueLiteral.setBType(this.booleanType);
        trueLiteral.value = Boolean.TRUE;

        BLangLiteral falseLiteral = new BLangLiteral();
        falseLiteral.setBType(this.booleanType);
        falseLiteral.value = Boolean.FALSE;

        defineCyclicUnionBasedInternalTypes();

        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, Flags.PUBLIC,
                                                                names.fromString("$anonType$TRUE"),
                                                                rootPkgNode.packageID, null, rootPkgNode.symbol.owner,
                                                                this.builtinPos, VIRTUAL);
        this.trueType = new BFiniteType(finiteTypeSymbol, new HashSet<>() {{
            add(trueLiteral);
        }});

        BTypeSymbol falseFiniteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, Flags.PUBLIC,
                names.fromString("$anonType$FALSE"), rootPkgNode.packageID, null, rootPkgNode.symbol.owner,
                this.builtinPos, VIRTUAL);
        this.falseType = new BFiniteType(falseFiniteTypeSymbol, new HashSet<>() {{
            add(falseLiteral);
        }});

        this.anyAndReadonly =
                ImmutableTypeCloner.getImmutableIntersectionType(this.anyType, this, names, this.types,
                                                                 rootPkgSymbol.pkgID);
        initializeType(this.anyAndReadonly, this.anyAndReadonly.effectiveType.name.getValue(), BUILTIN);

        // Initialize the invokable type
        this.invokableType.flags = Flags.ANY_FUNCTION;
        BInvokableTypeSymbol tSymbol = Symbols.createInvokableTypeSymbol(SymTag.FUNCTION_TYPE, Flags.ANY_FUNCTION,
                rootPkgSymbol.pkgID, this.invokableType, rootPkgNode.symbol.scope.owner, builtinPos, BUILTIN);
        tSymbol.params = null;
        tSymbol.restParam = null;
        tSymbol.returnType = null;
        this.invokableType.tsymbol = tSymbol;

        defineReadonlyCompoundType();
    }

    private void defineReadonlyCompoundType() {
        anyAndReadonlyOrError = BUnionType.create(null, anyAndReadonly, errorType);
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
            case TypeTags.XML_COMMENT:
                return xmlCommentType;
            case TypeTags.XML_PI:
                return xmlPIType;
            case TypeTags.XML_ELEMENT:
                return xmlElementType;
            case TypeTags.XML_TEXT:
                return xmlTextType;
            case TypeTags.STREAM:
                return streamType;
            case TypeTags.TABLE:
                return tableType;
            case TypeTags.NIL:
                return nilType;
            case TypeTags.NEVER:
                return neverType;
            case TypeTags.ERROR:
                return errorType;
            case TypeTags.SIGNED32_INT:
                return signed32IntType;
            case TypeTags.SIGNED16_INT:
                return signed16IntType;
            case TypeTags.SIGNED8_INT:
                return signed8IntType;
            case TypeTags.UNSIGNED32_INT:
                return unsigned32IntType;
            case TypeTags.UNSIGNED16_INT:
                return unsigned16IntType;
            case TypeTags.UNSIGNED8_INT:
                return unsigned8IntType;
            case TypeTags.CHAR_STRING:
                return charStringType;
            case TypeTags.REGEXP:
                return regExpType;
            default:
                return semanticError;
        }
    }

    public BType getLangLibSubType(String name) {
        // Assuming subtype names are unique across LangLib
        switch (name) {
            case Names.STRING_SIGNED32:
                return this.signed32IntType;
            case Names.STRING_SIGNED16:
                return this.signed16IntType;
            case Names.STRING_SIGNED8:
                return this.signed8IntType;
            case Names.STRING_UNSIGNED32:
                return this.unsigned32IntType;
            case Names.STRING_UNSIGNED16:
                return this.unsigned16IntType;
            case Names.STRING_UNSIGNED8:
                return this.unsigned8IntType;
            case Names.STRING_CHAR:
                return this.charStringType;
            case Names.STRING_XML_ELEMENT:
                return this.xmlElementType;
            case Names.STRING_XML_PI:
                return this.xmlPIType;
            case Names.STRING_XML_COMMENT:
                return this.xmlCommentType;
            case Names.STRING_XML_TEXT:
                return this.xmlTextType;
            case Names.STRING_REGEXP:
                return this.regExpType;
        }
        throw new IllegalStateException("LangLib Subtype not found: " + name);
    }

    public void loadPredeclaredModules() {
        this.predeclaredModules = Map.ofEntries(Map.entry(Names.BOOLEAN, this.langBooleanModuleSymbol),
                                                Map.entry(Names.DECIMAL, this.langDecimalModuleSymbol),
                                                Map.entry(Names.ERROR, this.langErrorModuleSymbol),
                                                Map.entry(Names.FLOAT, this.langFloatModuleSymbol),
                                                Map.entry(Names.FUNCTION, this.langFunctionModuleSymbol),
                                                Map.entry(Names.FUTURE, this.langFutureModuleSymbol),
                                                Map.entry(Names.INT, this.langIntModuleSymbol),
                                                Map.entry(Names.MAP, this.langMapModuleSymbol),
                                                Map.entry(Names.OBJECT, this.langObjectModuleSymbol),
                                                Map.entry(Names.STREAM, this.langStreamModuleSymbol),
                                                Map.entry(Names.STRING, this.langStringModuleSymbol),
                                                Map.entry(Names.TABLE, this.langTableModuleSymbol),
                                                Map.entry(Names.TRANSACTION, this.langTransactionModuleSymbol),
                                                Map.entry(Names.TYPEDESC, this.langTypedescModuleSymbol),
                                                Map.entry(Names.XML, this.langXmlModuleSymbol)
                );
    }

    public void initializeType(BType type, String name, SymbolOrigin origin) {
        initializeType(type, names.fromString(name), origin);
    }

    private void initializeType(BType type, Name name, SymbolOrigin origin) {
        defineType(type, new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, name, rootPkgSymbol.pkgID, type, rootPkgSymbol,
                                         builtinPos, origin));
    }

    private void initializeTSymbol(BType type, Name name, PackageID packageID) {
        type.tsymbol = new BTypeSymbol(SymTag.TYPE_DEF, Flags.PUBLIC, name, packageID, type, rootPkgSymbol, builtinPos,
                                       BUILTIN);
    }

    private void defineType(BType type, BTypeSymbol tSymbol) {
        type.tsymbol = tSymbol;
        rootScope.define(tSymbol.name, tSymbol);
    }

    public void updateBuiltinSubtypeOwners() {
        updateIntSubtypeOwners();
        updateStringSubtypeOwners();
        updateXMLSubtypeOwners();
    }

    public void updateIntSubtypeOwners() {
        this.signed8IntType.tsymbol.owner = this.langIntModuleSymbol;
        this.unsigned8IntType.tsymbol.owner = this.langIntModuleSymbol;
        this.signed16IntType.tsymbol.owner = this.langIntModuleSymbol;
        this.unsigned16IntType.tsymbol.owner = this.langIntModuleSymbol;
        this.signed32IntType.tsymbol.owner = this.langIntModuleSymbol;
        this.unsigned32IntType.tsymbol.owner = this.langIntModuleSymbol;
    }

    public void updateStringSubtypeOwners() {
        this.charStringType.tsymbol.owner = this.langStringModuleSymbol;
    }

    public void updateXMLSubtypeOwners() {
        this.xmlElementType.tsymbol.owner = this.langXmlModuleSymbol;
        this.xmlCommentType.tsymbol.owner = this.langXmlModuleSymbol;
        this.xmlPIType.tsymbol.owner = this.langXmlModuleSymbol;
        this.xmlTextType.tsymbol.owner = this.langXmlModuleSymbol;
    }

    public void defineOperators() {
        // Binary arithmetic operators
        defineIntegerArithmeticOperations();

        // Binary arithmetic operators for nullable integer types
        defineNilableIntegerArithmeticOperations();

        defineIntFloatingPointArithmeticOperations();

        defineNilableIntFloatingPointArithmeticOperations();

        // Binary bitwise operators for nullable integer types
        defineNilableIntegerBitwiseAndOperations();
        defineNilableIntegerBitwiseOrAndXorOperations(OperatorKind.BITWISE_OR);
        defineNilableIntegerBitwiseOrAndXorOperations(OperatorKind.BITWISE_XOR);

        // Binary shift operators for nullable integer types
        defineNilableIntegerLeftShiftOperations();
        defineNilableIntegerRightShiftOperations(OperatorKind.BITWISE_RIGHT_SHIFT);
        defineNilableIntegerRightShiftOperations(OperatorKind.BITWISE_UNSIGNED_RIGHT_SHIFT);

        // Unary operators for nullable integer types
        defineNilableIntegerUnaryOperations();

        // Binary operators for optional floating point types
        defineNilableFloatingPointOperations();

        // XML arithmetic operators
        defineXmlStringConcatanationOperations();
        defineBinaryOperator(OperatorKind.ADD, stringType, stringType, stringType);
        defineBinaryOperator(OperatorKind.ADD, stringType, charStringType, stringType);
        defineBinaryOperator(OperatorKind.ADD, charStringType, stringType, stringType);
        defineBinaryOperator(OperatorKind.ADD, charStringType, charStringType, stringType);
        defineBinaryOperator(OperatorKind.ADD, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.ADD, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.SUB, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.SUB, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.DIV, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.DIV, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MUL, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MUL, decimalType, decimalType, decimalType);
        defineBinaryOperator(OperatorKind.MOD, floatType, floatType, floatType);
        defineBinaryOperator(OperatorKind.MOD, decimalType, decimalType, decimalType);

        defineIntegerBitwiseAndOperations();
        defineIntegerBitwiseOrOperations(OperatorKind.BITWISE_OR);
        defineIntegerBitwiseOrOperations(OperatorKind.BITWISE_XOR);
        defineIntegerLeftShiftOperations();
        defineIntegerRightShiftOperations(OperatorKind.BITWISE_RIGHT_SHIFT);
        defineIntegerRightShiftOperations(OperatorKind.BITWISE_UNSIGNED_RIGHT_SHIFT);

        // Binary equality operators ==, !=, equals
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
        defineBinaryOperator(OperatorKind.EQUALS, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, jsonType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, nilType, jsonType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, anyType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, nilType, anyType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, anydataType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, nilType, anydataType, booleanType);
        defineBinaryOperator(OperatorKind.EQUALS, nilType, nilType, booleanType);
        defineBinaryOperator(OperatorKind.EQUAL, regExpType, regExpType, booleanType);

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
        defineBinaryOperator(OperatorKind.REF_NOT_EQUAL, regExpType, regExpType, booleanType);

        // Binary comparison operators <=, <, >=, >
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_THAN, nilType, nilType, booleanType);

        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.LESS_EQUAL, nilType, nilType, booleanType);

        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_THAN, nilType, nilType, booleanType);

        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, byteType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, intType, byteType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, byteType, intType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, floatType, floatType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, decimalType, decimalType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, stringType, stringType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.GREATER_EQUAL, nilType, nilType, booleanType);

        defineBinaryOperator(OperatorKind.AND, booleanType, booleanType, booleanType);
        defineBinaryOperator(OperatorKind.OR, booleanType, booleanType, booleanType);

        // Unary operator symbols
        defineIntegerUnaryOperations();

        defineUnaryOperator(OperatorKind.ADD, floatType, floatType);
        defineUnaryOperator(OperatorKind.ADD, decimalType, decimalType);

        defineUnaryOperator(OperatorKind.SUB, floatType, floatType);
        defineUnaryOperator(OperatorKind.SUB, decimalType, decimalType);

        defineUnaryOperator(OperatorKind.NOT, booleanType, booleanType);
    }

    private void defineIntegerUnaryOperations() {
        BType[] intTypes = {intType, byteType, signed32IntType, signed16IntType, signed8IntType, unsigned32IntType,
                unsigned16IntType, unsigned8IntType};
        for (BType type : intTypes) {
            defineUnaryOperator(OperatorKind.ADD, type, type);
            defineUnaryOperator(OperatorKind.SUB, type, intType);
            defineUnaryOperator(OperatorKind.BITWISE_COMPLEMENT, type, intType);
        }
    }

    private BUnionType getNilableBType(BType type) {
        BUnionType nilableType = BUnionType.create(null, type, nilType);
        nilableType.setNullable(true);
        return nilableType;
    }

    private void defineNilableIntegerUnaryOperations() {
        BUnionType[] nilableIntTypes = {
                getNilableBType(intType),
                getNilableBType(byteType),
                getNilableBType(signed32IntType),
                getNilableBType(signed16IntType),
                getNilableBType(signed8IntType),
                getNilableBType(unsigned32IntType),
                getNilableBType(unsigned16IntType),
                getNilableBType(unsigned8IntType)
        };

        BType intOptional = nilableIntTypes[0];

        for (BUnionType type : nilableIntTypes) {
            defineUnaryOperator(OperatorKind.ADD, type, intOptional);
            defineUnaryOperator(OperatorKind.SUB, type, intOptional);
            defineUnaryOperator(OperatorKind.BITWISE_COMPLEMENT, type, intOptional);
        }
    }

    private void defineXmlStringConcatanationOperations() {
        defineBinaryOperator(OperatorKind.ADD, xmlType, stringType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, xmlType, charStringType, xmlType);

        defineBinaryOperator(OperatorKind.ADD, stringType, xmlType, xmlType);
        defineBinaryOperator(OperatorKind.ADD, charStringType, xmlType, xmlType);

        defineBinaryOperator(OperatorKind.ADD, stringType, xmlTextType, xmlTextType);
        defineBinaryOperator(OperatorKind.ADD, charStringType, xmlTextType, xmlTextType);

        defineBinaryOperator(OperatorKind.ADD, xmlTextType, stringType, xmlTextType);
        defineBinaryOperator(OperatorKind.ADD, xmlTextType, charStringType, xmlTextType);
    }

    private void defineIntegerArithmeticOperations() {

        BType[] intTypes = {intType, byteType, signed32IntType, signed16IntType, signed8IntType, unsigned32IntType,
                unsigned16IntType,
                unsigned8IntType};
        for (BType lhs : intTypes) {
            for (BType rhs : intTypes) {
                defineBinaryOperator(OperatorKind.ADD, lhs, rhs, intType);
                defineBinaryOperator(OperatorKind.SUB, lhs, rhs, intType);
                defineBinaryOperator(OperatorKind.DIV, lhs, rhs, intType);
                defineBinaryOperator(OperatorKind.MUL, lhs, rhs, intType);
                defineBinaryOperator(OperatorKind.MOD, lhs, rhs, intType);
            }
        }
    }

    private void defineIntFloatingPointArithmeticOperations() {
        BType[] intTypes = {intType, byteType, signed32IntType, signed16IntType, signed8IntType, unsigned32IntType,
                unsigned16IntType, unsigned8IntType};

        for (BType intType : intTypes) {
            defineBinaryOperator(OperatorKind.MUL, decimalType, intType, decimalType);
            defineBinaryOperator(OperatorKind.MUL, intType, decimalType, decimalType);
            defineBinaryOperator(OperatorKind.MUL, floatType, intType, floatType);
            defineBinaryOperator(OperatorKind.MUL, intType, floatType, floatType);
            defineBinaryOperator(OperatorKind.DIV, decimalType, intType, decimalType);
            defineBinaryOperator(OperatorKind.DIV, floatType, intType, floatType);
            defineBinaryOperator(OperatorKind.MOD, decimalType, intType, decimalType);
            defineBinaryOperator(OperatorKind.MOD, floatType, intType, floatType);
        }
    }

    private void defineNilableIntFloatingPointArithmeticOperations() {
        BType[] intTypes = {intType, byteType, signed32IntType, signed16IntType, signed8IntType, unsigned32IntType,
                unsigned16IntType, unsigned8IntType};
        BUnionType nullableFloat = getNilableBType(floatType);
        BUnionType nullableDecimal = getNilableBType(decimalType);

        BUnionType[] nilableIntTypes = new BUnionType[8];

        for (int i = 0; i < intTypes.length; i++) {
            nilableIntTypes[i] = getNilableBType(intTypes[i]);
        }

        for (BType intType : intTypes) {
            defineBinaryOperator(OperatorKind.MUL, nullableDecimal, intType, nullableDecimal);
            defineBinaryOperator(OperatorKind.MUL, intType, nullableDecimal, nullableDecimal);
            defineBinaryOperator(OperatorKind.MUL, nullableFloat, intType, nullableFloat);
            defineBinaryOperator(OperatorKind.MUL, intType, nullableFloat, nullableFloat);
            defineBinaryOperator(OperatorKind.DIV, nullableDecimal, intType, nullableDecimal);
            defineBinaryOperator(OperatorKind.DIV, nullableFloat, intType, nullableFloat);
            defineBinaryOperator(OperatorKind.MOD, nullableDecimal, intType, nullableDecimal);
            defineBinaryOperator(OperatorKind.MOD, nullableFloat, intType, nullableFloat);
        }

        for (BUnionType nilableIntType : nilableIntTypes) {
            defineBinaryOperator(OperatorKind.MUL, floatType, nilableIntType, nullableFloat);
            defineBinaryOperator(OperatorKind.MUL, nilableIntType, floatType, nullableFloat);
            defineBinaryOperator(OperatorKind.MUL, nilableIntType, nullableFloat, nullableFloat);
            defineBinaryOperator(OperatorKind.MUL, nullableFloat, nilableIntType, nullableFloat);
            defineBinaryOperator(OperatorKind.MUL, decimalType, nilableIntType, nullableDecimal);
            defineBinaryOperator(OperatorKind.MUL, nilableIntType, decimalType, nullableDecimal);
            defineBinaryOperator(OperatorKind.MUL, nilableIntType, nullableDecimal, nullableDecimal);
            defineBinaryOperator(OperatorKind.MUL, nullableDecimal, nilableIntType, nullableDecimal);

            defineBinaryOperator(OperatorKind.DIV, floatType, nilableIntType, nullableFloat);
            defineBinaryOperator(OperatorKind.DIV, nullableFloat, nilableIntType, nullableFloat);
            defineBinaryOperator(OperatorKind.DIV, decimalType, nilableIntType, nullableDecimal);
            defineBinaryOperator(OperatorKind.DIV, nullableDecimal, nilableIntType, nullableDecimal);

            defineBinaryOperator(OperatorKind.MOD, floatType, nilableIntType, nullableFloat);
            defineBinaryOperator(OperatorKind.MOD, nullableFloat, nilableIntType, nullableFloat);
            defineBinaryOperator(OperatorKind.MOD, decimalType, nilableIntType, nullableDecimal);
            defineBinaryOperator(OperatorKind.MOD, nullableDecimal, nilableIntType, nullableDecimal);
        }
    }

    private void defineNilableIntegerArithmeticOperations() {
        BType[] intTypes = {intType, byteType, signed32IntType, signed16IntType, signed8IntType, unsigned32IntType,
                unsigned16IntType,
                unsigned8IntType};

        BUnionType[] nilableIntTypes = new BUnionType[8];

        for (int i = 0; i < intTypes.length; i++) {
            nilableIntTypes[i] = getNilableBType(intTypes[i]);
        }

        BType intOptional = nilableIntTypes[0];

        for (BUnionType lhs : nilableIntTypes) {
            for (BUnionType rhs : nilableIntTypes) {
                defineBinaryOperator(OperatorKind.ADD, lhs, rhs, intOptional);
                defineBinaryOperator(OperatorKind.SUB, lhs, rhs, intOptional);
                defineBinaryOperator(OperatorKind.DIV, lhs, rhs, intOptional);
                defineBinaryOperator(OperatorKind.MUL, lhs, rhs, intOptional);
                defineBinaryOperator(OperatorKind.MOD, lhs, rhs, intOptional);
            }
        }
    }

    private void defineIntegerBitwiseAndOperations() {
        BType[] unsignedIntTypes = {byteType, unsigned8IntType, unsigned16IntType, unsigned32IntType};
        BType[] signedIntTypes = {intType, signed8IntType, signed16IntType, signed32IntType};

        for (BType unsigned : unsignedIntTypes) {
            for (BType signed : signedIntTypes) {
                defineBinaryOperator(OperatorKind.BITWISE_AND, unsigned, signed, unsigned);
            }
        }

        for (int i = 0; i < unsignedIntTypes.length; i++) {
            for (int j = 0; j < unsignedIntTypes.length; j++) {
                BType unsignedIntTypeLhs = unsignedIntTypes[i];
                BType unsignedIntTypeRhs = unsignedIntTypes[j];
                defineBinaryOperator(OperatorKind.BITWISE_AND, unsignedIntTypeLhs, unsignedIntTypeRhs,
                                     i <= j ? unsignedIntTypeLhs : unsignedIntTypeRhs);
            }
        }

        for (BType signed : signedIntTypes) {
            for (BType unsigned : unsignedIntTypes) {
                defineBinaryOperator(OperatorKind.BITWISE_AND, signed, unsigned, unsigned);
            }
        }

        for (BType signedLhs : signedIntTypes) {
            for (BType signedRhs : signedIntTypes) {
                defineBinaryOperator(OperatorKind.BITWISE_AND, signedLhs, signedRhs, intType);
            }
        }
    }

    private void defineIntegerBitwiseOrOperations(OperatorKind orOpKind) {
        BType[] unsignedIntTypes = {byteType, unsigned8IntType, unsigned16IntType, unsigned32IntType};
        BType[] signedIntTypes = {intType, signed8IntType, signed16IntType, signed32IntType};

        for (BType unsigned : unsignedIntTypes) {
            for (BType signed : signedIntTypes) {
                defineBinaryOperator(orOpKind, unsigned, signed, intType);
            }
        }

        for (int i = 0; i < unsignedIntTypes.length; i++) {
            for (int j = 0; j < unsignedIntTypes.length; j++) {
                BType unsignedIntTypeLhs = unsignedIntTypes[i];
                BType unsignedIntTypeRhs = unsignedIntTypes[j];
                defineBinaryOperator(orOpKind, unsignedIntTypeLhs, unsignedIntTypeRhs,
                                     i >= j ? unsignedIntTypeLhs : unsignedIntTypeRhs);
            }
        }

        for (BType signed : signedIntTypes) {
            for (BType unsigned : unsignedIntTypes) {
                defineBinaryOperator(orOpKind, signed, unsigned, intType);
            }
        }

        for (BType signedLhs : signedIntTypes) {
            for (BType signedRhs : signedIntTypes) {
                defineBinaryOperator(orOpKind, signedLhs, signedRhs, intType);
            }
        }
    }

    private void defineNilableIntegerBitwiseAndOperations() {
        BType[] unsignedIntTypes = {byteType, unsigned8IntType, unsigned16IntType, unsigned32IntType};
        BType[] signedIntTypes = {intType, signed8IntType, signed16IntType, signed32IntType};

        BUnionType[] unsignedNilableIntTypes = new BUnionType[4];
        BUnionType[] signedNilableIntTypes = new BUnionType[4];

        for (int i = 0; i < unsignedIntTypes.length; i++) {
            unsignedNilableIntTypes[i] = getNilableBType(unsignedIntTypes[i]);
            signedNilableIntTypes[i] = getNilableBType(signedIntTypes[i]);
        }

        BType intOptional = signedNilableIntTypes[0];

        for (int i = 0; i < unsignedNilableIntTypes.length; i++) {
            for (int j = 0; j < signedNilableIntTypes.length; j++) {
                defineBinaryOperator(OperatorKind.BITWISE_AND, unsignedNilableIntTypes[i],
                        signedNilableIntTypes[j], unsignedNilableIntTypes[i]);
                defineBinaryOperator(OperatorKind.BITWISE_AND, unsignedNilableIntTypes[i],
                        signedIntTypes[j], unsignedNilableIntTypes[i]);
                defineBinaryOperator(OperatorKind.BITWISE_AND, unsignedIntTypes[i],
                        signedNilableIntTypes[j], unsignedNilableIntTypes[i]);

                defineBinaryOperator(OperatorKind.BITWISE_AND, signedNilableIntTypes[i],
                        unsignedNilableIntTypes[j], unsignedNilableIntTypes[j]);
                defineBinaryOperator(OperatorKind.BITWISE_AND, signedIntTypes[i],
                        unsignedNilableIntTypes[j], unsignedNilableIntTypes[j]);
                defineBinaryOperator(OperatorKind.BITWISE_AND, signedNilableIntTypes[i],
                        unsignedIntTypes[j], unsignedNilableIntTypes[j]);

                defineBinaryOperator(OperatorKind.BITWISE_AND, signedNilableIntTypes[i],
                        signedNilableIntTypes[j], intOptional);
                defineBinaryOperator(OperatorKind.BITWISE_AND, signedIntTypes[i],
                        signedNilableIntTypes[j], intOptional);
                defineBinaryOperator(OperatorKind.BITWISE_AND, signedNilableIntTypes[i],
                        signedIntTypes[j], intOptional);
            }
        }

        for (int i = 0; i < unsignedNilableIntTypes.length; i++) {
            for (int j = 0; j < unsignedNilableIntTypes.length; j++) {
                defineBinaryOperator(OperatorKind.BITWISE_AND, unsignedNilableIntTypes[i], unsignedNilableIntTypes[j],
                        i <= j ? unsignedNilableIntTypes[i] : unsignedNilableIntTypes[j]);
                defineBinaryOperator(OperatorKind.BITWISE_AND, unsignedNilableIntTypes[i], unsignedIntTypes[j],
                        i <= j ? unsignedNilableIntTypes[i] : unsignedNilableIntTypes[j]);
                defineBinaryOperator(OperatorKind.BITWISE_AND, unsignedIntTypes[i], unsignedNilableIntTypes[j],
                        i <= j ? unsignedNilableIntTypes[i] : unsignedNilableIntTypes[j]);
            }
        }
    }

    private void defineNilableIntegerBitwiseOrAndXorOperations(OperatorKind opKind) {
        BType[] unsignedIntTypes = {byteType, unsigned8IntType, unsigned16IntType, unsigned32IntType};
        BType[] signedIntTypes = {intType, signed8IntType, signed16IntType, signed32IntType};

        BUnionType[] unsignedNilableIntTypes = new BUnionType[4];
        BUnionType[] signedNilableIntTypes = new BUnionType[4];

        for (int i = 0; i < unsignedIntTypes.length; i++) {
            unsignedNilableIntTypes[i] = getNilableBType(unsignedIntTypes[i]);
            signedNilableIntTypes[i] = getNilableBType(signedIntTypes[i]);
        }

        BType intOptionalType = signedNilableIntTypes[0];

        for (int i = 0; i < signedNilableIntTypes.length; i++) {
            for (int j = 0; j < unsignedNilableIntTypes.length; j++) {
                defineBinaryOperator(opKind, unsignedNilableIntTypes[i],
                        signedNilableIntTypes[j], intOptionalType);
                defineBinaryOperator(opKind, unsignedNilableIntTypes[i],
                        signedIntTypes[j], intOptionalType);
                defineBinaryOperator(opKind, unsignedIntTypes[i],
                        signedNilableIntTypes[j], intOptionalType);

                defineBinaryOperator(opKind, signedNilableIntTypes[i],
                        unsignedNilableIntTypes[j], intOptionalType);
                defineBinaryOperator(opKind, signedIntTypes[i],
                        unsignedNilableIntTypes[j], intOptionalType);
                defineBinaryOperator(opKind, signedNilableIntTypes[i],
                        unsignedIntTypes[j], intOptionalType);

                defineBinaryOperator(opKind, signedNilableIntTypes[i],
                        signedNilableIntTypes[j], intOptionalType);
                defineBinaryOperator(opKind, signedIntTypes[i],
                        signedNilableIntTypes[j], intOptionalType);
                defineBinaryOperator(opKind, signedNilableIntTypes[i],
                        signedIntTypes[j], intOptionalType);
            }
        }

        for (int i = 0; i < unsignedNilableIntTypes.length; i++) {
            for (int j = 0; j < unsignedNilableIntTypes.length; j++) {
                defineBinaryOperator(opKind, unsignedNilableIntTypes[i], unsignedNilableIntTypes[j],
                        i >= j ? unsignedNilableIntTypes[i] : unsignedNilableIntTypes[j]);
                defineBinaryOperator(opKind, unsignedNilableIntTypes[i], unsignedIntTypes[j],
                        i >= j ? unsignedNilableIntTypes[i] : unsignedNilableIntTypes[j]);
                defineBinaryOperator(opKind, unsignedIntTypes[i], unsignedNilableIntTypes[j],
                        i >= j ? unsignedNilableIntTypes[i] : unsignedNilableIntTypes[j]);
            }
        }
    }

    private void defineIntegerLeftShiftOperations() {
        BType[] allIntTypes = {intType, byteType, signed32IntType, signed16IntType, signed8IntType, unsigned32IntType,
                unsigned16IntType, unsigned8IntType};

        for (BType lhs : allIntTypes) {
            for (BType rhs : allIntTypes) {
                defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, lhs, rhs, intType);
            }
        }
    }

    private void defineNilableIntegerLeftShiftOperations() {
        BType[] intTypes = {intType, byteType, signed32IntType, signed16IntType, signed8IntType, unsigned32IntType,
                unsigned16IntType,
                unsigned8IntType};

        BUnionType[] nilableIntTypes = new BUnionType[8];

        for (int i = 0; i < intTypes.length; i++) {
            nilableIntTypes[i] = getNilableBType(intTypes[i]);
        }

        BType intOptionalType = nilableIntTypes[0];

        for (int i = 0; i < intTypes.length; i++) {
            for (int j = 0; j < intTypes.length; j++) {
                defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, nilableIntTypes[i],
                        nilableIntTypes[j], intOptionalType);
                defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, nilableIntTypes[i],
                        intTypes[j], intOptionalType);
                defineBinaryOperator(OperatorKind.BITWISE_LEFT_SHIFT, intTypes[i],
                        nilableIntTypes[j], intOptionalType);
            }
        }
    }

    private void defineIntegerRightShiftOperations(OperatorKind rightShiftOpKind) {
        BType[] unsignedIntTypes = {byteType, unsigned8IntType, unsigned16IntType, unsigned32IntType};
        BType[] signedIntTypes = {intType, signed8IntType, signed16IntType, signed32IntType};

        BType[] allIntTypes = {intType, byteType, signed32IntType, signed16IntType, signed8IntType, unsigned32IntType,
                unsigned16IntType, unsigned8IntType};

        for (BType unsignedLhs : unsignedIntTypes) {
            for (BType intRhs : allIntTypes) {
                defineBinaryOperator(rightShiftOpKind, unsignedLhs, intRhs, unsignedLhs);
            }
        }

        for (BType signedLhs : signedIntTypes) {
            for (BType intRhs : allIntTypes) {
                defineBinaryOperator(rightShiftOpKind, signedLhs, intRhs, intType);
            }
        }
    }

    private void defineNilableIntegerRightShiftOperations(OperatorKind rightShiftOpKind) {
        BType[] unsignedIntTypes = {byteType, unsigned8IntType, unsigned16IntType, unsigned32IntType};
        BType[] signedIntTypes = {intType, signed8IntType, signed16IntType, signed32IntType};

        BUnionType[] unsignedNilableIntTypes = new BUnionType[4];
        BUnionType[] signedNilableIntTypes = new BUnionType[4];

        for (int i = 0; i < unsignedIntTypes.length; i++) {
            unsignedNilableIntTypes[i] = getNilableBType(unsignedIntTypes[i]);
            signedNilableIntTypes[i] = getNilableBType(signedIntTypes[i]);
        }

        BType intOptional = signedNilableIntTypes[0];

        BType[] allIntTypes = {intType, byteType, signed32IntType, signed16IntType, signed8IntType, unsigned32IntType,
                unsigned16IntType,
                unsigned8IntType};

        BUnionType[] nilableAllIntTypes = new BUnionType[8];

        for (int i = 0; i < allIntTypes.length; i++) {
            nilableAllIntTypes[i] = getNilableBType(allIntTypes[i]);
        }

        for (int i = 0; i < unsignedNilableIntTypes.length; i++) {
            for (int j = 0; j < nilableAllIntTypes.length; j++) {
                defineBinaryOperator(rightShiftOpKind, unsignedNilableIntTypes[i],
                        nilableAllIntTypes[j], unsignedNilableIntTypes[i]);
                defineBinaryOperator(rightShiftOpKind, unsignedIntTypes[i],
                        nilableAllIntTypes[j], unsignedNilableIntTypes[i]);
                defineBinaryOperator(rightShiftOpKind, unsignedNilableIntTypes[i],
                        allIntTypes[j], unsignedNilableIntTypes[i]);
            }
        }

        for (int i = 0; i < signedNilableIntTypes.length; i++) {
            for (int j = 0; j < nilableAllIntTypes.length; j++) {
                defineBinaryOperator(rightShiftOpKind, signedNilableIntTypes[i],
                        nilableAllIntTypes[j], intOptional);
                defineBinaryOperator(rightShiftOpKind, signedNilableIntTypes[i],
                        allIntTypes[j], intOptional);
                defineBinaryOperator(rightShiftOpKind, signedIntTypes[i],
                        nilableAllIntTypes[j], intOptional);
            }
        }
    }

    private void defineNilableFloatingPointOperations() {
        BType floatOptional = getNilableBType(floatType);
        BType decimalOptional = getNilableBType(decimalType);

        OperatorKind[] binaryOperators = {OperatorKind.ADD, OperatorKind.SUB, OperatorKind.MUL,
                OperatorKind.DIV, OperatorKind.MOD};

        for (OperatorKind operator : binaryOperators) {
            defineBinaryOperator(operator, floatType, floatOptional, floatOptional);
            defineBinaryOperator(operator, floatOptional, floatType, floatOptional);
            defineBinaryOperator(operator, floatOptional, floatOptional, floatOptional);
            defineBinaryOperator(operator, decimalType, decimalOptional, decimalOptional);
            defineBinaryOperator(operator, decimalOptional, decimalType, decimalOptional);
            defineBinaryOperator(operator, decimalOptional, decimalOptional, decimalOptional);
        }

        OperatorKind[] unaryOperators = {OperatorKind.ADD, OperatorKind.SUB, OperatorKind.BITWISE_COMPLEMENT};

        for (OperatorKind operator : unaryOperators) {
            defineUnaryOperator(operator, floatOptional, floatOptional);
            defineUnaryOperator(operator, decimalOptional, decimalOptional);
        }
    }

    public void defineIntRangeOperations() {
        BType[] intTypes = {byteType, intType, signed32IntType, signed16IntType, signed8IntType,
                unsigned32IntType, unsigned16IntType, unsigned8IntType};
        for (BType lhs : intTypes) {
            for (BType rhs : intTypes) {
                defineBinaryOperator(OperatorKind.CLOSED_RANGE, lhs, rhs, intRangeType);
                defineBinaryOperator(OperatorKind.HALF_OPEN_RANGE, lhs, rhs, intRangeType);
            }
        }
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

    private void defineOperator(Name name,
                                List<BType> paramTypes,
                                BType retType) {
        BInvokableType opType = new BInvokableType(paramTypes, retType, null);
        BOperatorSymbol symbol = new BOperatorSymbol(name, rootPkgSymbol.pkgID, opType, rootPkgSymbol, this.builtinPos,
                                                     BUILTIN);
        rootScope.define(name, symbol);
    }

    private void defineCyclicUnionBasedInternalTypes() {
        defineAnydataCyclicTypeAndDependentTypes();
        defineJsonCyclicTypeAndDependentTypes();
        defineCloneableCyclicTypeAndDependentTypes();
    }

    private void defineCloneableCyclicTypeAndDependentTypes() {
        cloneableType = BUnionType.create(null, readonlyType, xmlType);
        addCyclicArrayMapTableOfMapMembers(cloneableType);

        // `cloneableType` and its symbol gets replaced by `Cloneable` type defined in lang value module. To prevent
        // cyclic dependencies need to define duplicate `Cloneable type in other modules as well. Due to this reason
        // symbol in symbol table is created by default as private.
        cloneableType.tsymbol = new BTypeSymbol(SymTag.TYPE, Flags.PRIVATE, Names.CLONEABLE, rootPkgSymbol.pkgID,
                cloneableType, rootPkgSymbol, builtinPos, BUILTIN);

        detailType = new BMapType(TypeTags.MAP, cloneableType, null);
        errorType = new BErrorType(null, detailType);
        errorType.tsymbol = new BErrorTypeSymbol(SymTag.ERROR, Flags.PUBLIC, Names.ERROR,
                rootPkgSymbol.pkgID, errorType, rootPkgSymbol, builtinPos, BUILTIN);

        errorOrNilType = BUnionType.create(null, errorType, nilType);
        anyOrErrorType = BUnionType.create(null, anyType, errorType);

        mapAllType = new BMapType(TypeTags.MAP, anyOrErrorType, null);
        arrayAllType = new BArrayType(anyOrErrorType);
        typeDesc.constraint = anyOrErrorType;
        futureType.constraint = anyOrErrorType;

        pureType = BUnionType.create(null, anydataType, errorType);
        streamType = new BStreamType(TypeTags.STREAM, pureType, nilType, null);
        tableType = new BTableType(TypeTags.TABLE, pureType, null);

        initializeType(streamType, TypeKind.STREAM.typeName(), BUILTIN);
        initializeType(tableType, TypeKind.TABLE.typeName(), BUILTIN);
    }

    private void addCyclicArrayMapTableOfMapMembers(BUnionType unionType) {
        BArrayType arrayCloneableType = new BArrayType(unionType);
        BMapType mapCloneableType = new BMapType(TypeTags.MAP, unionType, null);
        BType tableMapCloneableType = new BTableType(TypeTags.TABLE, mapCloneableType, null);
        unionType.add(arrayCloneableType);
        unionType.add(mapCloneableType);
        unionType.add(tableMapCloneableType);
        unionType.isCyclic = true;
    }

    private void defineJsonCyclicTypeAndDependentTypes() {
        BUnionType jsonInternal = BUnionType.create(null, nilType, booleanType, intType, floatType, decimalType,
                stringType);
        BArrayType arrayJsonTypeInternal = new BArrayType(jsonInternal);
        BMapType mapJsonTypeInternal = new BMapType(TypeTags.MAP, jsonInternal, null);
        jsonInternal.add(arrayJsonTypeInternal);
        jsonInternal.add(mapJsonTypeInternal);
        jsonInternal.isCyclic = true;

        jsonType = new BJSONType(jsonInternal);
        PackageID pkgID = rootPkgSymbol.pkgID;
        Optional<BIntersectionType> immutableType = Types.getImmutableType(this, pkgID, jsonInternal);
        if (immutableType.isPresent()) {
            Types.addImmutableType(this, pkgID, jsonType, immutableType.get());
        }
        jsonType.tsymbol = new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, Names.JSON, pkgID, jsonType,
                                           rootPkgSymbol, builtinPos, BUILTIN);

        arrayJsonType = new BArrayType(jsonType);
        mapJsonType = new BMapType(TypeTags.MAP, jsonType, null);
    }

    private void defineAnydataCyclicTypeAndDependentTypes() {
        BUnionType anyDataInternal = BUnionType.create(null, nilType, booleanType, intType, floatType, decimalType,
                stringType, xmlType);
        addCyclicArrayMapTableOfMapMembers(anyDataInternal);

        anydataType = new BAnydataType(anyDataInternal);
        PackageID pkgID = rootPkgSymbol.pkgID;
        Optional<BIntersectionType> immutableType = Types.getImmutableType(this, pkgID, anyDataInternal);
        if (immutableType.isPresent()) {
            Types.addImmutableType(this, pkgID, anydataType, immutableType.get());
        }

        anydataType.tsymbol = new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, Names.ANYDATA, pkgID,
                                              anydataType, rootPkgSymbol, builtinPos, BUILTIN);
        arrayAnydataType = new BArrayType(anydataType);
        mapAnydataType = new BMapType(TypeTags.MAP, anydataType, null);
        anydataOrReadonly = BUnionType.create(null, anydataType, readonlyType);

        initializeType(mapAnydataType, TypeKind.MAP.typeName(), VIRTUAL);
    }
}

/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.IntTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
import io.ballerina.types.Core;
import io.ballerina.types.Value;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemTypeHelper;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
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
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.Objects;
import java.util.Optional;

import static org.ballerinalang.model.types.TypeKind.PARAMETERIZED;
import static org.wso2.ballerinalang.compiler.util.TypeTags.ANY;
import static org.wso2.ballerinalang.compiler.util.TypeTags.ANYDATA;
import static org.wso2.ballerinalang.compiler.util.TypeTags.BOOLEAN;
import static org.wso2.ballerinalang.compiler.util.TypeTags.BYTE;
import static org.wso2.ballerinalang.compiler.util.TypeTags.DECIMAL;
import static org.wso2.ballerinalang.compiler.util.TypeTags.ERROR;
import static org.wso2.ballerinalang.compiler.util.TypeTags.FLOAT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.FUTURE;
import static org.wso2.ballerinalang.compiler.util.TypeTags.HANDLE;
import static org.wso2.ballerinalang.compiler.util.TypeTags.INT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.JSON;
import static org.wso2.ballerinalang.compiler.util.TypeTags.MAP;
import static org.wso2.ballerinalang.compiler.util.TypeTags.NEVER;
import static org.wso2.ballerinalang.compiler.util.TypeTags.NIL;
import static org.wso2.ballerinalang.compiler.util.TypeTags.READONLY;
import static org.wso2.ballerinalang.compiler.util.TypeTags.SEMANTIC_ERROR;
import static org.wso2.ballerinalang.compiler.util.TypeTags.SIGNED16_INT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.SIGNED32_INT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.SIGNED8_INT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.STREAM;
import static org.wso2.ballerinalang.compiler.util.TypeTags.STRING;
import static org.wso2.ballerinalang.compiler.util.TypeTags.TABLE;
import static org.wso2.ballerinalang.compiler.util.TypeTags.TYPEDESC;
import static org.wso2.ballerinalang.compiler.util.TypeTags.UNSIGNED16_INT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.UNSIGNED32_INT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.UNSIGNED8_INT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.XML;
import static org.wso2.ballerinalang.compiler.util.TypeTags.XML_COMMENT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.XML_ELEMENT;
import static org.wso2.ballerinalang.compiler.util.TypeTags.XML_PI;
import static org.wso2.ballerinalang.compiler.util.TypeTags.XML_TEXT;

/**
 * Represents a set of factory methods to generate the {@link TypeSymbol}s.
 *
 * @since 2.0.0
 */
public class TypesFactory {

    private static final CompilerContext.Key<TypesFactory> TYPES_FACTORY_KEY = new CompilerContext.Key<>();

    private final CompilerContext context;
    private final SymbolFactory symbolFactory;
    private final BLangAnonymousModelHelper anonymousModelHelper;
    private SymbolTable symTable;

    private TypesFactory(CompilerContext context) {
        context.put(TYPES_FACTORY_KEY, this);

        this.context = context;
        this.symbolFactory = SymbolFactory.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
    }

    public static TypesFactory getInstance(CompilerContext context) {
        TypesFactory typesFactory = context.get(TYPES_FACTORY_KEY);
        if (typesFactory == null) {
            typesFactory = new TypesFactory(context);
        }

        return typesFactory;
    }

    public TypeSymbol getTypeDescriptor(BType bType) {
        return getTypeDescriptor(bType, bType != null ? bType.tsymbol : null, false);
    }

    public TypeSymbol getTypeDescriptor(BType bType, BSymbol tSymbol) {
        return getTypeDescriptor(bType, tSymbol, false);
    }

    /**
     * Get the type descriptor for the given type. This method takes a type and a type symbol both because there are
     * instances where the tSymbol != tSymbol.type.tsymbol. e.g., a type symbol created for a type definition. Neither
     * bType nor tSymbol should be null.
     *
     * @param bType       {@link BType} of the type descriptor
     * @param tSymbol     The type symbol associated with the context in which this method is called
     * @param rawTypeOnly Whether to convert the type descriptor to type reference or keep the raw type
     * @return {@link TypeSymbol} generated
     */
    public TypeSymbol getTypeDescriptor(BType bType, BSymbol tSymbol, boolean rawTypeOnly) {
        return getTypeDescriptor(bType, tSymbol, rawTypeOnly, true, false);
    }

    TypeSymbol getTypeDescriptor(BType bType, BSymbol tSymbol, boolean rawTypeOnly, boolean getOriginalType,
                                 boolean typeRefFromIntersectType) {
        if (bType == null) {
            return null;
        }

        if (isTypeReference(bType, tSymbol, rawTypeOnly)) {
            return new BallerinaTypeReferenceTypeSymbol(this.context, bType, tSymbol, typeRefFromIntersectType);
        }
        BTypeSymbol typeSymbol = tSymbol instanceof BTypeDefinitionSymbol ? tSymbol.type.tsymbol
                : (BTypeSymbol) tSymbol;
        return createTypeDescriptor(bType, typeSymbol);
    }

    private TypeSymbol createTypeDescriptor(BType bType, BTypeSymbol tSymbol) {
        switch (bType.getKind()) {
            case BOOLEAN:
                return new BallerinaBooleanTypeSymbol(this.context, bType);
            case BYTE:
                return new BallerinaByteTypeSymbol(this.context, bType);
            case INT:
                if (bType instanceof BIntSubType intSubType) {
                    return createIntSubType(intSubType);
                }
                return new BallerinaIntTypeSymbol(this.context, bType);
            case FLOAT:
                return new BallerinaFloatTypeSymbol(this.context, bType);
            case DECIMAL:
                return new BallerinaDecimalTypeSymbol(this.context, bType);
            case STRING:
                if (bType instanceof BStringSubType stringSubType) {
                    return new BallerinaStringCharTypeSymbol(this.context, stringSubType);
                }
                return new BallerinaStringTypeSymbol(this.context, bType);
            case ANY:
                return new BallerinaAnyTypeSymbol(this.context, (BAnyType) bType);
            case ANYDATA:
                if (bType instanceof BRegexpType regexpType) {
                    return new BallerinaRegexpTypeSymbol(this.context, regexpType);
                }
                return new BallerinaAnydataTypeSymbol(this.context, (BAnydataType) bType);
            case HANDLE:
                return new BallerinaHandleTypeSymbol(this.context, (BHandleType) bType);
            case JSON:
                return new BallerinaJSONTypeSymbol(this.context, (BJSONType) bType);
            case READONLY:
                return new BallerinaReadonlyTypeSymbol(this.context, (BReadonlyType) bType);
            case TABLE:
                return new BallerinaTableTypeSymbol(this.context, (BTableType) bType);
            case XML:
                if (bType instanceof BXMLSubType subType) {
                    return createXMLSubType(subType);
                }
                return new BallerinaXMLTypeSymbol(this.context, (BXMLType) bType);
            case OBJECT:
                ObjectTypeSymbol objType = new BallerinaObjectTypeSymbol(this.context, (BObjectType) bType);
                if (Symbols.isFlagOn(tSymbol.flags, Flags.CLASS)) {
                    return symbolFactory.createClassSymbol((BClassSymbol) tSymbol, tSymbol.name.value, objType);
                }
                return objType;
            case RECORD:
                return new BallerinaRecordTypeSymbol(this.context, (BRecordType) bType);
            case ERROR:
                return new BallerinaErrorTypeSymbol(this.context, (BErrorType) bType);
            case UNION:
                return new BallerinaUnionTypeSymbol(this.context, (BUnionType) bType);
            case FUTURE:
                return new BallerinaFutureTypeSymbol(this.context, (BFutureType) bType);
            case MAP:
                return new BallerinaMapTypeSymbol(this.context, (BMapType) bType);
            case STREAM:
                return new BallerinaStreamTypeSymbol(this.context, (BStreamType) bType);
            case ARRAY:
                return new BallerinaArrayTypeSymbol(this.context, (BArrayType) bType);
            case TUPLE:
                return new BallerinaTupleTypeSymbol(this.context, (BTupleType) bType);
            case TYPEDESC:
                return new BallerinaTypeDescTypeSymbol(this.context, (BTypedescType) bType);
            case NIL:
                return new BallerinaNilTypeSymbol(this.context, (BNilType) bType);
            case FINITE:
                BFiniteType finiteType = (BFiniteType) bType;
                Optional<Value> value = Core.singleShape(finiteType.semType());
                if (value.isPresent()) {
                    BType broadType = SemTypeHelper.broadTypes(finiteType, symTable).iterator()
                            .next();
                    String valueString = Objects.toString(value.get().value, "()");
                    return new BallerinaSingletonTypeSymbol(this.context, broadType, valueString, bType);
                }
                return new BallerinaUnionTypeSymbol(this.context, finiteType);
            case FUNCTION:
                return new BallerinaFunctionTypeSymbol(this.context, (BInvokableTypeSymbol) tSymbol, bType);
            case NEVER:
                return new BallerinaNeverTypeSymbol(this.context, (BNeverType) bType);
            case NONE:
                return new BallerinaNoneTypeSymbol(this.context, (BNoType) bType);
            case INTERSECTION:
                return new BallerinaIntersectionTypeSymbol(this.context, (BIntersectionType) bType);
            case PARAMETERIZED:
            case TYPEREFDESC:
                return new BallerinaTypeReferenceTypeSymbol(this.context, bType, tSymbol, false);
            case REGEXP:
                return new BallerinaRegexpTypeSymbol(this.context, (BRegexpType) bType);
            default:
                if (bType.tag == SEMANTIC_ERROR) {
                    return new BallerinaCompilationErrorTypeSymbol(this.context, bType);
                }

                return new BallerinaTypeSymbol(this.context, bType);
        }
    }

    private IntTypeSymbol createIntSubType(BIntSubType internalType) {
        return switch (internalType.tag) {
            case UNSIGNED8_INT -> new BallerinaIntUnsigned8TypeSymbol(this.context, internalType);
            case SIGNED8_INT -> new BallerinaIntSigned8TypeSymbol(this.context, internalType);
            case UNSIGNED16_INT -> new BallerinaIntUnsigned16TypeSymbol(this.context, internalType);
            case SIGNED16_INT -> new BallerinaIntSigned16TypeSymbol(this.context, internalType);
            case UNSIGNED32_INT -> new BallerinaIntUnsigned32TypeSymbol(this.context, internalType);
            case SIGNED32_INT -> new BallerinaIntSigned32TypeSymbol(this.context, internalType);
            default -> throw new IllegalStateException("Invalid integer subtype type tag: " + internalType.tag);
        };
    }

    private XMLTypeSymbol createXMLSubType(BXMLSubType internalType) {
        return switch (internalType.tag) {
            case XML_ELEMENT -> new BallerinaXMLElementTypeSymbol(this.context, internalType);
            case XML_PI -> new BallerinaXMLProcessingInstructionTypeSymbol(this.context, internalType);
            case XML_COMMENT -> new BallerinaXMLCommentTypeSymbol(this.context, internalType);
            case XML_TEXT -> new BallerinaXMLTextTypeSymbol(this.context, internalType);
            default -> throw new IllegalStateException("Invalid XML subtype type tag: " + internalType.tag);
        };
    }

    public boolean isTypeReference(BType bType, BSymbol tSymbol, boolean rawTypeOnly) {
        // Not considering type params as type refs for now because having it in the typedesc form will make more
        // sense for end users of the API consumers (e.g., VS Code plugin users). This probably can be removed once
        // https://github.com/ballerina-platform/ballerina-lang/issues/18150 is fixed.
        if (rawTypeOnly || tSymbol == null || Symbols.isFlagOn(tSymbol.flags, Flags.TYPE_PARAM)) {
            return false;
        }

        if (Symbols.isFlagOn(tSymbol.flags, Flags.ANONYMOUS)) {
            return false;
        }

        if ((tSymbol.tag & SymTag.FUNCTION_TYPE) == SymTag.FUNCTION_TYPE) {
            return false;
        }

        if (!isBuiltinNamedType(bType.tag) && !(tSymbol.name.value.isEmpty()
                || anonymousModelHelper.isAnonymousType(tSymbol))) {
            return true;
        }

        final TypeKind kind = bType.getKind();
        return kind == PARAMETERIZED
                || tSymbol.kind == SymbolKind.ENUM || isCustomError(tSymbol);
    }

    public static TypeDescKind getTypeDescKind(TypeKind bTypeKind) {
        return switch (bTypeKind) {
            case ANY -> TypeDescKind.ANY;
            case ANYDATA -> TypeDescKind.ANYDATA;
            case ARRAY -> TypeDescKind.ARRAY;
            case BOOLEAN -> TypeDescKind.BOOLEAN;
            case BYTE -> TypeDescKind.BYTE;
            case DECIMAL -> TypeDescKind.DECIMAL;
            case FLOAT -> TypeDescKind.FLOAT;
            case HANDLE -> TypeDescKind.HANDLE;
            case INT -> TypeDescKind.INT;
            case NEVER -> TypeDescKind.NEVER;
            case NIL -> TypeDescKind.NIL;
            case STRING -> TypeDescKind.STRING;
            case JSON -> TypeDescKind.JSON;
            case XML -> TypeDescKind.XML;
            case FUNCTION -> TypeDescKind.FUNCTION;
            case FUTURE -> TypeDescKind.FUTURE;
            case MAP -> TypeDescKind.MAP;
            case OBJECT -> TypeDescKind.OBJECT;
            case STREAM -> TypeDescKind.STREAM;
            case TUPLE -> TypeDescKind.TUPLE;
            case TYPEDESC -> TypeDescKind.TYPEDESC;
            case UNION -> TypeDescKind.UNION;
            case INTERSECTION -> TypeDescKind.INTERSECTION;
            case ERROR -> TypeDescKind.ERROR;
            case NONE, OTHER -> TypeDescKind.NONE;
            default -> null;
        };
    }

    private static boolean isCustomError(BSymbol tSymbol) {
        return tSymbol.kind == SymbolKind.ERROR && !Names.ERROR.equals(tSymbol.name);
    }

    private static boolean isBuiltinNamedType(int tag) {
        return switch (tag) {
            case INT,
                 BYTE,
                 FLOAT,
                 DECIMAL,
                 STRING,
                 BOOLEAN,
                 JSON,
                 XML,
                 NIL,
                 ANY,
                 ANYDATA,
                 HANDLE,
                 READONLY,
                 NEVER,
                 MAP,
                 STREAM,
                 TYPEDESC,
                 TABLE,
                 ERROR,
                 FUTURE,
                 SEMANTIC_ERROR -> true;
            default -> false;
        };
    }
}

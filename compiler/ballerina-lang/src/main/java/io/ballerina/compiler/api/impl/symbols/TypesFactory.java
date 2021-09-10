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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.BallerinaModuleID;
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.IntTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.types.IntersectableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClassSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeDefinitionSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStringSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLSubType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.Optional;
import java.util.Set;

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
import static org.wso2.ballerinalang.compiler.util.TypeTags.NONE;
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
    private final SymbolTable symbolTable;
    private final Types types;

    private TypesFactory(CompilerContext context) {
        context.put(TYPES_FACTORY_KEY, this);

        this.context = context;
        this.symbolFactory = SymbolFactory.getInstance(context);
        this.symbolTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
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
        if (bType == null || bType.tag == NONE) {
            return null;
        }

        if (getOriginalType && bType instanceof IntersectableReferenceType) {
            Optional<BIntersectionType> intersectionType = ((IntersectableReferenceType) bType).getIntersectionType();
            if (intersectionType.isPresent()) {
                bType = intersectionType.get();
                tSymbol = bType.tsymbol;
            }
        }

        ModuleID moduleID = tSymbol == null ? null : new BallerinaModuleID(tSymbol.pkgID);

        if (isTypeReference(bType, tSymbol, rawTypeOnly)) {
            return new BallerinaTypeReferenceTypeSymbol(this.context, moduleID,
                    types.getReferredType(bType), tSymbol, typeRefFromIntersectType);
        } else {
            if (tSymbol instanceof BTypeDefinitionSymbol) {
                return createTypeDescriptor(bType, tSymbol.type.tsymbol, moduleID);
            }
            return createTypeDescriptor(bType, (BTypeSymbol) tSymbol, moduleID);
        }

    }

    private TypeSymbol createTypeDescriptor(BType bType, BTypeSymbol tSymbol, ModuleID moduleID) {
        switch (bType.getKind()) {
            case BOOLEAN:
                return new BallerinaBooleanTypeSymbol(this.context, moduleID, bType);
            case BYTE:
                return new BallerinaByteTypeSymbol(this.context, moduleID, bType);
            case INT:
                if (bType instanceof BIntSubType) {
                    return createIntSubType((BIntSubType) bType);
                }
                return new BallerinaIntTypeSymbol(this.context, moduleID, bType);
            case FLOAT:
                return new BallerinaFloatTypeSymbol(this.context, moduleID, bType);
            case DECIMAL:
                return new BallerinaDecimalTypeSymbol(this.context, moduleID, bType);
            case STRING:
                if (bType instanceof BStringSubType) {
                    moduleID = new BallerinaModuleID(symbolTable.langStringModuleSymbol.pkgID);
                    return new BallerinaStringCharTypeSymbol(this.context, moduleID, (BStringSubType) bType);
                }
                return new BallerinaStringTypeSymbol(this.context, moduleID, bType);
            case ANY:
                return new BallerinaAnyTypeSymbol(this.context, moduleID, (BAnyType) bType);
            case ANYDATA:
                return new BallerinaAnydataTypeSymbol(this.context, moduleID, (BAnydataType) bType);
            case HANDLE:
                return new BallerinaHandleTypeSymbol(this.context, moduleID, (BHandleType) bType);
            case JSON:
                return new BallerinaJSONTypeSymbol(this.context, moduleID, (BJSONType) bType);
            case READONLY:
                return new BallerinaReadonlyTypeSymbol(this.context, moduleID, (BReadonlyType) bType);
            case TABLE:
                return new BallerinaTableTypeSymbol(this.context, moduleID, (BTableType) bType);
            case XML:
                if (bType instanceof BXMLSubType) {
                    return createXMLSubType((BXMLSubType) bType);
                }
                return new BallerinaXMLTypeSymbol(this.context, moduleID, (BXMLType) bType);
            case OBJECT:
                ObjectTypeSymbol objType = new BallerinaObjectTypeSymbol(this.context, moduleID, (BObjectType) bType);
                if (Symbols.isFlagOn(tSymbol.flags, Flags.CLASS)) {
                    BTypeSymbol classSymbol = tSymbol;
                    return symbolFactory.createClassSymbol((BClassSymbol) classSymbol, classSymbol.name.value, objType);
                }
                return objType;
            case RECORD:
                return new BallerinaRecordTypeSymbol(this.context, moduleID, (BRecordType) bType);
            case ERROR:
                return new BallerinaErrorTypeSymbol(this.context, moduleID, (BErrorType) bType);
            case UNION:
                return new BallerinaUnionTypeSymbol(this.context, moduleID, (BUnionType) bType);
            case FUTURE:
                return new BallerinaFutureTypeSymbol(this.context, moduleID, (BFutureType) bType);
            case MAP:
                return new BallerinaMapTypeSymbol(this.context, moduleID, (BMapType) bType);
            case STREAM:
                return new BallerinaStreamTypeSymbol(this.context, moduleID, (BStreamType) bType);
            case ARRAY:
                return new BallerinaArrayTypeSymbol(this.context, moduleID, (BArrayType) bType);
            case TUPLE:
                return new BallerinaTupleTypeSymbol(this.context, moduleID, (BTupleType) bType);
            case TYPEDESC:
                return new BallerinaTypeDescTypeSymbol(this.context, moduleID, (BTypedescType) bType);
            case NIL:
                return new BallerinaNilTypeSymbol(this.context, moduleID, (BNilType) bType);
            case FINITE:
                BFiniteType finiteType = (BFiniteType) bType;
                Set<BLangExpression> valueSpace = finiteType.getValueSpace();

                if (valueSpace.size() == 1) {
                    BLangExpression shape = valueSpace.iterator().next();
                    return new BallerinaSingletonTypeSymbol(this.context, moduleID, shape, bType);
                }

                return new BallerinaUnionTypeSymbol(this.context, moduleID, finiteType);
            case FUNCTION:
                return new BallerinaFunctionTypeSymbol(this.context, moduleID, (BInvokableTypeSymbol) tSymbol, bType);
            case NEVER:
                return new BallerinaNeverTypeSymbol(this.context, moduleID, (BNeverType) bType);
            case NONE:
                return new BallerinaNoneTypeSymbol(this.context, moduleID, (BNoType) bType);
            case INTERSECTION:
                return new BallerinaIntersectionTypeSymbol(this.context, moduleID, (BIntersectionType) bType);
            case TYPEREFDESC:
                return createTypeDescriptor(types.getReferredType(bType), types.getReferredType(bType).tsymbol, moduleID);
            default:
                if (bType.tag == SEMANTIC_ERROR) {
                    return new BallerinaCompilationErrorTypeSymbol(this.context, moduleID, bType);
                }

                return new BallerinaTypeSymbol(this.context, moduleID, bType);
        }
    }

    private IntTypeSymbol createIntSubType(BIntSubType internalType) {
        ModuleID moduleID = new BallerinaModuleID(symbolTable.langIntModuleSymbol.pkgID);

        switch (internalType.tag) {
            case UNSIGNED8_INT:
                return new BallerinaIntUnsigned8TypeSymbol(this.context, moduleID, internalType);
            case SIGNED8_INT:
                return new BallerinaIntSigned8TypeSymbol(this.context, moduleID, internalType);
            case UNSIGNED16_INT:
                return new BallerinaIntUnsigned16TypeSymbol(this.context, moduleID, internalType);
            case SIGNED16_INT:
                return new BallerinaIntSigned16TypeSymbol(this.context, moduleID, internalType);
            case UNSIGNED32_INT:
                return new BallerinaIntUnsigned32TypeSymbol(this.context, moduleID, internalType);
            case SIGNED32_INT:
                return new BallerinaIntSigned32TypeSymbol(this.context, moduleID, internalType);
        }

        throw new IllegalStateException("Invalid integer subtype type tag: " + internalType.tag);
    }

    private XMLTypeSymbol createXMLSubType(BXMLSubType internalType) {
        ModuleID moduleID = new BallerinaModuleID(symbolTable.langXmlModuleSymbol.pkgID);

        switch (internalType.tag) {
            case XML_ELEMENT:
                return new BallerinaXMLElementTypeSymbol(this.context, moduleID, internalType);
            case XML_PI:
                return new BallerinaXMLProcessingInstructionTypeSymbol(this.context, moduleID, internalType);
            case XML_COMMENT:
                return new BallerinaXMLCommentTypeSymbol(this.context, moduleID, internalType);
            case XML_TEXT:
                return new BallerinaXMLTextTypeSymbol(this.context, moduleID, internalType);
        }

        throw new IllegalStateException("Invalid XML subtype type tag: " + internalType.tag);
    }

    private boolean isTypeReference(BType bType, BSymbol tSymbol, boolean rawTypeOnly) {
        // Not considering type params as type refs for now because having it in the typedesc form will make more
        // sense for end users of the API consumers (e.g., VS Code plugin users). This probably can be removed once
        // https://github.com/ballerina-platform/ballerina-lang/issues/18150 is fixed.
        if (rawTypeOnly || tSymbol == null || Symbols.isFlagOn(tSymbol.flags, Flags.TYPE_PARAM)) {
            return false;
        }

        if (Symbols.isFlagOn(tSymbol.flags, Flags.ANONYMOUS)) {
            return false;
        }

        if (!isBuiltinNamedType(bType.tag) && !tSymbol.name.value.isEmpty()) {
            return true;
        }

        final TypeKind kind = bType.getKind();
        return kind == PARAMETERIZED || tSymbol instanceof BTypeDefinitionSymbol
                || tSymbol.kind == SymbolKind.ENUM || isCustomError(tSymbol);
    }

    public static TypeDescKind getTypeDescKind(TypeKind bTypeKind) {
        switch (bTypeKind) {
            case ANY:
                return TypeDescKind.ANY;
            case ANYDATA:
                return TypeDescKind.ANYDATA;
            case ARRAY:
                return TypeDescKind.ARRAY;
            case BOOLEAN:
                return TypeDescKind.BOOLEAN;
            case BYTE:
                return TypeDescKind.BYTE;
            case DECIMAL:
                return TypeDescKind.DECIMAL;
            case FLOAT:
                return TypeDescKind.FLOAT;
            case HANDLE:
                return TypeDescKind.HANDLE;
            case INT:
                return TypeDescKind.INT;
            case NEVER:
                return TypeDescKind.NEVER;
            case NIL:
                return TypeDescKind.NIL;
            case STRING:
                return TypeDescKind.STRING;
            case JSON:
                return TypeDescKind.JSON;
            case XML:
                return TypeDescKind.XML;
            case FUNCTION:
                return TypeDescKind.FUNCTION;
            case FUTURE:
                return TypeDescKind.FUTURE;
            case MAP:
                return TypeDescKind.MAP;
            case OBJECT:
                return TypeDescKind.OBJECT;
            case STREAM:
                return TypeDescKind.STREAM;
            case TUPLE:
                return TypeDescKind.TUPLE;
            case TYPEDESC:
                return TypeDescKind.TYPEDESC;
            case UNION:
                return TypeDescKind.UNION;
            case INTERSECTION:
                return TypeDescKind.INTERSECTION;
            case ERROR:
                return TypeDescKind.ERROR;
            case PARAMETERIZED:
            case ANNOTATION:
            case BLOB:
            case CHANNEL:
            case CONNECTOR:
            case ENDPOINT:
            case FINITE:
            case NONE:
            case OTHER:
            case PACKAGE:
            case READONLY:
            case SERVICE:
            case TABLE:
            case TYPEPARAM:
            case VOID:
            default:
                return null;
        }
    }

    private static boolean isCustomError(BSymbol tSymbol) {
        return tSymbol.kind == SymbolKind.ERROR && !Names.ERROR.equals(tSymbol.name);
    }

    private static boolean isBuiltinNamedType(int tag) {
        switch (tag) {
            case INT:
            case BYTE:
            case FLOAT:
            case DECIMAL:
            case STRING:
            case BOOLEAN:
            case JSON:
            case XML:
            case NIL:
            case ANY:
            case ANYDATA:
            case HANDLE:
            case READONLY:
            case NEVER:
            case MAP:
            case STREAM:
            case TYPEDESC:
            case TABLE:
            case ERROR:
            case FUTURE:
            case SEMANTIC_ERROR:
                return true;
        }

        return false;
    }
}

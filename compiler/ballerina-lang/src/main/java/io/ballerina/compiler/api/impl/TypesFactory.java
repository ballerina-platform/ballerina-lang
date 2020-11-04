/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.symbols.BallerinaArrayTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaErrorTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaFunctionTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaFutureTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaMapTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaNilTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaObjectTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaRecordTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaSimpleTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaSingletonTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaStreamTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaTupleTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaTypeDescTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaTypeReferenceTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.BallerinaUnionTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.Set;

import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.model.types.TypeKind.RECORD;

/**
 * Represents a set of factory methods to generate the {@link TypeSymbol}s.
 *
 * @since 2.0.0
 */
public class TypesFactory {

    public static TypeSymbol getTypeDescriptor(BType bType) {
        return getTypeDescriptor(bType, false);
    }

    /**
     * Get the type descriptor for the given type.
     *
     * @param bType       {@link BType} of the type descriptor
     * @param rawTypeOnly Whether to convert the type descriptor to type reference or keep the raw type
     * @return {@link TypeSymbol} generated
     */
    public static TypeSymbol getTypeDescriptor(BType bType, boolean rawTypeOnly) {
        if (bType == null || bType.tag == TypeTags.NONE) {
            return null;
        }

        ModuleID moduleID = bType.tsymbol == null ? null : new BallerinaModuleID(bType.tsymbol.pkgID);

        if (isTypeReference(bType, rawTypeOnly)) {
            return new BallerinaTypeReferenceTypeSymbol(moduleID, bType, bType.tsymbol.getName().getValue());
        }

        switch (bType.getKind()) {
            case OBJECT:
                return new BallerinaObjectTypeSymbol(moduleID, (BObjectType) bType);
            case RECORD:
                return new BallerinaRecordTypeSymbol(moduleID, (BRecordType) bType);
            case ERROR:
                return new BallerinaErrorTypeSymbol(moduleID, (BErrorType) bType);
            case UNION:
                return new BallerinaUnionTypeSymbol(moduleID, (BUnionType) bType);
            case FUTURE:
                return new BallerinaFutureTypeSymbol(moduleID, (BFutureType) bType);
            case MAP:
                return new BallerinaMapTypeSymbol(moduleID, (BMapType) bType);
            case STREAM:
                return new BallerinaStreamTypeSymbol(moduleID, (BStreamType) bType);
            case ARRAY:
                return new BallerinaArrayTypeSymbol(moduleID, (BArrayType) bType);
            case TUPLE:
                return new BallerinaTupleTypeSymbol(moduleID, (BTupleType) bType);
            case TYPEDESC:
                return new BallerinaTypeDescTypeSymbol(moduleID, (BTypedescType) bType);
            case NIL:
                return new BallerinaNilTypeSymbol(moduleID, (BNilType) bType);
            case FINITE:
                BFiniteType finiteType = (BFiniteType) bType;
                Set<BLangExpression> valueSpace = finiteType.getValueSpace();

                if (valueSpace.size() == 1) {
                    BLangExpression shape = valueSpace.iterator().next();
                    return new BallerinaSingletonTypeSymbol(moduleID, shape, bType);
                }

                return new BallerinaUnionTypeSymbol(moduleID, finiteType);
            case OTHER:
                if (bType instanceof BInvokableType) {
                    return new BallerinaFunctionTypeSymbol(moduleID, (BInvokableTypeSymbol) bType.tsymbol);
                }
                // fall through
            default:
                return new BallerinaSimpleTypeSymbol(moduleID, bType);
        }
    }

    private static boolean isTypeReference(BType bType, boolean rawTypeOnly) {
        if (rawTypeOnly || bType.tsymbol == null) {
            return false;
        }

        if ((bType.tsymbol.flags & Flags.ANONYMOUS) == Flags.ANONYMOUS) {
            return false;
        }

        final TypeKind kind = bType.getKind();
        return kind == RECORD || kind == OBJECT || bType.tsymbol.isLabel;
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
}

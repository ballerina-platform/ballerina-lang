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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl.util;

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.identifier.Utils;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.Optional;

/**
 * Common util methods related to symbols.
 */
public class SymbolUtils {

    public static String unescapeUnicode(String value) {
        if (value.startsWith("'")) {
            return Utils.unescapeUnicodeCodepoints(value.substring(1));
        }
        return Utils.unescapeUnicodeCodepoints(value);
    }

    public static Optional<TypeSymbol> getTypeDescriptor(Symbol symbol) {
        if (symbol == null) {
            return Optional.empty();
        }
        switch (symbol.kind()) {
            case TYPE_DEFINITION:
                return Optional.ofNullable(((TypeDefinitionSymbol) symbol).typeDescriptor());
            case VARIABLE:
                return Optional.ofNullable(((VariableSymbol) symbol).typeDescriptor());
            case PARAMETER:
                return Optional.ofNullable(((ParameterSymbol) symbol).typeDescriptor());
            case ANNOTATION:
                return ((AnnotationSymbol) symbol).typeDescriptor();
            case FUNCTION:
            case METHOD:
                return Optional.ofNullable(((FunctionSymbol) symbol).typeDescriptor());
            case CONSTANT:
            case ENUM_MEMBER:
                return Optional.ofNullable(((ConstantSymbol) symbol).typeDescriptor());
            case CLASS:
                return Optional.of((ClassSymbol) symbol);
            case RECORD_FIELD:
                return Optional.ofNullable(((RecordFieldSymbol) symbol).typeDescriptor());
            case OBJECT_FIELD:
                return Optional.of(((ObjectFieldSymbol) symbol).typeDescriptor());
            case CLASS_FIELD:
                return Optional.of(((ClassFieldSymbol) symbol).typeDescriptor());
            case TYPE:
                return Optional.of((TypeSymbol) symbol);
            default:
                return Optional.empty();
        }
    }

    /**
     * Retrieves the type parameter's bound type based on the kind of the input BType.
     *
     * @param type The input BType for which the bound type is to be retrieved.
     * @return The bound type based on the kind of the input BType. Returns null if the kind is not supported.
     */
     public static BType getTypeParamBoundType(BType type) {
        switch (type.getKind()) {
            case MAP:
                return ((BMapType) type).constraint;
            case ARRAY:
                return ((BArrayType) type).eType;
            case TABLE:
                return ((BTableType) type).constraint;
            case STREAM:
                return ((BStreamType) type).constraint;
            case XML:
                if (type.tag == TypeTags.XML) {
                    return ((BXMLType) type).constraint;
                }
                return type;
            // The following explicitly mentioned type kinds should be supported, but they are not for the moment.
            case ERROR:
            default:
                return null;
        }
    }
}

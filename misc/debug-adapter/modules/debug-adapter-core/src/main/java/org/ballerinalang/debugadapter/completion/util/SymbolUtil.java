/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.ballerinalang.debugadapter.completion.util;

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;

import java.util.Optional;

/**
 * Carries a set of utilities to check the types of the symbols.
 *
 * @since 2201.1.0
 */
public class SymbolUtil {

    private SymbolUtil() {
    }

    /**
     * Check whether the given symbol is a symbol with the type Object.
     *
     * @param symbol to evaluate
     * @return {@link Boolean} whether the symbol holds the type object
     */
    public static boolean isObject(Symbol symbol) {
        TypeSymbol typeDescriptor;
        switch (symbol.kind()) {
            case TYPE_DEFINITION:
                typeDescriptor = ((TypeDefinitionSymbol) symbol).typeDescriptor();
                break;
            case VARIABLE:
                typeDescriptor = ((VariableSymbol) symbol).typeDescriptor();
                break;
            case PARAMETER:
                typeDescriptor = ((ParameterSymbol) symbol).typeDescriptor();
                break;
            case CLASS:
                typeDescriptor = (ClassSymbol) symbol;
                break;
            case TYPE:
                typeDescriptor = (TypeSymbol) symbol;
                break;
            default:
                return false;
        }

        return CommonUtil.getRawType(typeDescriptor).typeKind() == TypeDescKind.OBJECT;
    }

    /**
     * Get the type descriptor of the given symbol.
     * If the symbol is not a variable symbol this method will return empty optional value
     *
     * @param symbol to evaluate
     * @return {@link Optional} type descriptor
     */
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
     * Get the type descriptor for the object symbol.
     *
     * @param symbol to evaluate
     * @return {@link ObjectTypeSymbol} for the object symbol
     */
    public static ObjectTypeSymbol getTypeDescForObjectSymbol(Symbol symbol) {
        Optional<? extends TypeSymbol> typeDescriptor = getTypeDescriptor(symbol);
        if (typeDescriptor.isEmpty() || !isObject(symbol)) {
            throw new UnsupportedOperationException("Cannot find a valid type descriptor");
        }

        return (ObjectTypeSymbol) CommonUtil.getRawType(typeDescriptor.get());
    }

    /**
     * Check Whether the provided symbol is a client symbol.
     *
     * @param symbol to be evaluated
     * @return {@link Boolean} status of the evaluation
     */
    public static boolean isClient(Symbol symbol) {
        if (!isObject(symbol)) {
            return false;
        }
        ObjectTypeSymbol typeDesc = getTypeDescForObjectSymbol(symbol);
        return typeDesc.qualifiers().contains(Qualifier.CLIENT);
    }
}

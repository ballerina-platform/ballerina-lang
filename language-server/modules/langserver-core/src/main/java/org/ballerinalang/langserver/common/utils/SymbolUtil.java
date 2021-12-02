/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.common.utils;

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ClassFieldSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Carries a set of utilities to check the types of the symbols.
 *
 * @since 2.0.0
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
    @Deprecated(forRemoval = true)
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
     * Check whether the given symbol is a class symbol.
     *
     * @param symbol to evaluate
     * @return {@link Boolean}
     */
    @Deprecated(forRemoval = true)
    public static boolean isClass(Symbol symbol) {
        TypeSymbol typeDescriptor;
        switch (symbol.kind()) {
            case TYPE:
                typeDescriptor = ((TypeDefinitionSymbol) symbol).typeDescriptor();
                break;
            case VARIABLE:
                typeDescriptor = ((VariableSymbol) symbol).typeDescriptor();
                break;
            case CLASS:
                return true;
            default:
                return false;
        }

        return CommonUtil.getRawType(typeDescriptor).kind() == SymbolKind.CLASS;
    }

    /**
     * Check whether the symbol is a class definition.
     * This method will not consider the variable symbols. In order to check the variable symbols,
     * use {@link #isClassVariable(Symbol)}
     *
     * @param symbol symbol to be evaluated
     * @return {@link Boolean}
     */
    public static boolean isClassDefinition(Symbol symbol) {
        if (symbol.kind() == SymbolKind.CLASS) {
            return true;
        }
        if (symbol.kind() == SymbolKind.TYPE_DEFINITION) {
            return CommonUtil.getRawType(((TypeDefinitionSymbol) symbol).typeDescriptor()).kind() == SymbolKind.CLASS;
        }

        return false;
    }

    /**
     * Check whether the symbol is a variable symbol while the kind is {@link SymbolKind#CLASS}.
     *
     * @param variableSymbol variable symbol to evaluate
     * @return {@link Boolean}
     */
    public static boolean isClassVariable(Symbol variableSymbol) {
        if (variableSymbol.kind() != SymbolKind.VARIABLE) {
            return false;
        }
        TypeSymbol typeSymbol = ((VariableSymbol) variableSymbol).typeDescriptor();
        return CommonUtil.getRawType(typeSymbol).kind() == SymbolKind.CLASS;
    }

    /**
     * Check whether the given symbol is a symbol with the type Record.
     *
     * @param symbol to evaluate
     * @return {@link Boolean} whether the symbol holds the type record
     */
    @Deprecated(forRemoval = true)
    public static boolean isRecord(Symbol symbol) {
        TypeSymbol typeDescriptor;
        switch (symbol.kind()) {
            case TYPE_DEFINITION:
                typeDescriptor = ((TypeDefinitionSymbol) symbol).typeDescriptor();
                break;
            case VARIABLE:
                typeDescriptor = ((VariableSymbol) symbol).typeDescriptor();
                break;
            default:
                return false;
        }

        return CommonUtil.getRawType(typeDescriptor).typeKind() == TypeDescKind.RECORD;
    }

    /**
     * Check whether the symbol is a record type definition.
     * This method will not consider the variable symbols. In order to check the variable symbols,
     * use {@link #isRecordVariable(Symbol)}
     *
     * @param symbol symbol to be evaluated
     * @return {@link Boolean}
     */
    public static boolean isRecordTypeDefinition(Symbol symbol) {
        if (symbol.kind() != SymbolKind.TYPE_DEFINITION) {
            return false;
        }
        TypeSymbol typeDescriptor = ((TypeDefinitionSymbol) symbol).typeDescriptor();

        return CommonUtil.getRawType(typeDescriptor).typeKind() == TypeDescKind.RECORD;
    }

    /**
     * Check whether the symbol is a variable symbol while the kind is {@link TypeDescKind#RECORD}.
     *
     * @param variableSymbol variable symbol to evaluate
     * @return {@link Boolean}
     */
    public static boolean isRecordVariable(Symbol variableSymbol) {
        if (variableSymbol.kind() != SymbolKind.VARIABLE) {
            return false;
        }
        TypeSymbol typeSymbol = ((VariableSymbol) variableSymbol).typeDescriptor();

        return CommonUtil.getRawType(typeSymbol).typeKind() == TypeDescKind.RECORD;
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
     * Get the type descriptor for the class symbol.
     *
     * @param symbol to evaluate
     * @return {@link ClassSymbol}
     */
    public static ClassSymbol getTypeDescForClassSymbol(Symbol symbol) {
        Optional<? extends TypeSymbol> typeDescriptor = getTypeDescriptor(symbol);
        if (typeDescriptor.isEmpty() || !isObject(symbol)) {
            throw new UnsupportedOperationException("Cannot find a valid type descriptor");
        }

        return (ClassSymbol) CommonUtil.getRawType(typeDescriptor.get());
    }

    /**
     * Get the type descriptor for the record symbol.
     *
     * @param symbol to evaluate
     * @return {@link RecordTypeSymbol} for the record symbol
     */
    public static RecordTypeSymbol getTypeDescForRecordSymbol(Symbol symbol) {
        Optional<? extends TypeSymbol> typeDescriptor = getTypeDescriptor(symbol);
        if (typeDescriptor.isEmpty() || !isRecord(symbol)) {
            throw new UnsupportedOperationException("Cannot find a valid type descriptor");
        }

        return (RecordTypeSymbol) CommonUtil.getRawType(typeDescriptor.get());
    }

    /**
     * Check Whether the provided symbol is a listener symbol.
     *
     * @param symbol to be evaluated
     * @return {@link Boolean} status of the evaluation
     */
    public static boolean isListener(Symbol symbol) {
        Optional<? extends TypeSymbol> symbolTypeDesc = getTypeDescriptor(symbol);

        if (symbolTypeDesc.isEmpty() || CommonUtil.getRawType(symbolTypeDesc.get()).kind() != SymbolKind.CLASS) {
            return false;
        }

        Map<String, MethodSymbol> attachedMethods =
                ((ClassSymbol) CommonUtil.getRawType(symbolTypeDesc.get())).methods();
        return attachedMethods.containsKey("start") && attachedMethods.containsKey("immediateStop")
                && attachedMethods.containsKey("attach");
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

    /**
     * Whether the symbol is an error symbol.
     *
     * @param symbol symbol to be evaluated
     * @return {@link Boolean} status of the evaluation
     */
    @Deprecated(forRemoval = true)
    public static boolean isError(Symbol symbol) {
        if (symbol.kind() != SymbolKind.VARIABLE) {
            return false;
        }
        TypeSymbol typeDescriptor = ((VariableSymbol) symbol).typeDescriptor();

        return typeDescriptor.typeKind() == TypeDescKind.ERROR;
    }

    /**
     * Get the type kind of a variable symbol.
     *
     * @param symbol to evaluate
     * @return {@link Optional} empty if the symbol is not a variable symbol
     */
    @Deprecated(forRemoval = true)
    public static Optional<TypeDescKind> getTypeKind(Symbol symbol) {
        if (symbol.kind() != SymbolKind.VARIABLE) {
            return Optional.empty();
        }

        return Optional.ofNullable(((VariableSymbol) symbol).typeDescriptor().typeKind());
    }

    /**
     * Predicate to evaluate whether a symbol is a type definition of the provided kind.
     *
     * @param typeDescKind to compare the symbol
     * @return {@link Predicate}
     */
    public static Predicate<Symbol> isOfType(TypeDescKind typeDescKind) {
        return symbol -> symbol.kind() == SymbolKind.TYPE_DEFINITION
                && CommonUtil.getRawType(((TypeDefinitionSymbol) symbol).typeDescriptor()).typeKind() == typeDescKind;
    }

    /**
     * Utility to evaluate whether the given symbol is a type definition of the provided kind.
     *
     * @param typeDescKind to compare the symbol
     * @return {@link Boolean}
     */
    public static boolean isOfType(Symbol symbol, TypeDescKind typeDescKind) {
        return symbol.kind() == SymbolKind.TYPE_DEFINITION
                && CommonUtil.getRawType(((TypeDefinitionSymbol) symbol).typeDescriptor()).typeKind() == typeDescKind;
    }
}

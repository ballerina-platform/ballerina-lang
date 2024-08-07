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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl.type.builders;

import io.ballerina.compiler.api.TypeBuilder;
import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.SingletonTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.Set;

import static org.ballerinalang.model.symbols.SymbolOrigin.COMPILED_SOURCE;

/**
 * The implementation of the methods used to build the Singleton type descriptor.
 *
 * @since 2201.2.0
 */
public class BallerinaSingletonTypeBuilder implements TypeBuilder.SINGLETON {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private TypeSymbol valueTypeSymbol;
    private Object value;

    public BallerinaSingletonTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
    }

    @Override
    public TypeBuilder.SINGLETON withValueSpace(Object value, TypeSymbol typeSymbol) {
        this.value = value;
        this.valueTypeSymbol = typeSymbol;
        return this;
    }

    @Override
    public SingletonTypeSymbol build() {
        if (value == null) {
            throw new IllegalArgumentException("The value provided to the singleton type can not be null");
        }

        if (!isValidValueType(value, valueTypeSymbol)) {
            throw new IllegalArgumentException("Type of value provided doesn't match with the provided type symbol");
        }

        BLangLiteral valueLiteral = new BLangLiteral(value, getValueBType(valueTypeSymbol));
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, Flags.PUBLIC,
                Names.fromString(value.toString()), symTable.rootPkgSymbol.pkgID, null, symTable.rootPkgSymbol,
                symTable.builtinPos, COMPILED_SOURCE);

        BFiniteType finiteType = new BFiniteType(finiteTypeSymbol, Set.of(valueLiteral));
        finiteTypeSymbol.type = finiteType;
        SingletonTypeSymbol singletonTypeSymbol = (SingletonTypeSymbol) typesFactory.getTypeDescriptor(finiteType,
                finiteTypeSymbol, true);

        this.value = null;
        this.valueTypeSymbol = null;

        return singletonTypeSymbol;
    }

    private boolean isValidValueType(Object value, TypeSymbol typeSymbol) {
        switch (typeSymbol.typeKind()) {
            case STRING:
                return value instanceof String || value instanceof Character;
            case INT:
                return value instanceof Integer;
            case DECIMAL:
            case FLOAT:
                return value instanceof Float || value instanceof Double;
            case BYTE:
                return value instanceof Byte;
            case BOOLEAN:
                return value instanceof Boolean;
            case NIL:
                return value.equals("()");
        }

        return false;
    }

    private BType getValueBType(TypeSymbol typeSymbol) {
        if (typeSymbol == null) {
            return symTable.noType;
        }

        if (typeSymbol instanceof AbstractTypeSymbol abstractTypeSymbol) {
            return abstractTypeSymbol.getBType();
        }

        return symTable.anyType;
    }
}

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
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import static org.ballerinalang.model.symbols.SymbolOrigin.COMPILED_SOURCE;

/**
 * The implementation of the methods used to build the Array type descriptor in Types API.
 *
 * @since 2201.2.0
 */
public class BallerinaArrayTypeBuilder implements TypeBuilder.ARRAY {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private TypeSymbol type;
    private int size = -1;
    private BArrayState state = BArrayState.OPEN;

    public BallerinaArrayTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
    }

    @Override
    public TypeBuilder.ARRAY withType(TypeSymbol type) {
        this.type = type;
        return this;
    }

    @Override
    public TypeBuilder.ARRAY withSize(Integer size) {
        if (size != null && size >= 0) {
            this.size = size;
            state = BArrayState.CLOSED;
        } else {
            this.size = -1;
            state = BArrayState.OPEN;
        }

        return this;
    }

    @Override
    public TypeBuilder.ARRAY withInferredSize() {
        state = BArrayState.INFERRED;
        size = -2;

        return this;
    }

    @Override
    public ArrayTypeSymbol build() {
        BTypeSymbol arrayTSymbol = Symbols.createTypeSymbol(SymTag.ARRAY_TYPE, Flags.PUBLIC, Names.EMPTY,
                symTable.rootPkgSymbol.pkgID, null, symTable.rootPkgSymbol, symTable.builtinPos, COMPILED_SOURCE);

        BArrayType arrayType = new BArrayType(getBType(type), arrayTSymbol, size, state);
        arrayTSymbol.type = arrayType;
        ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) typesFactory.getTypeDescriptor(arrayType);
        this.size = -1;
        this.type = null;
        this.state = BArrayState.OPEN;

        return arrayTypeSymbol;
    }

    private BType getBType(TypeSymbol type) {
        if (type == null) {
            throw new IllegalArgumentException("Array member type descriptor can not be null");
        }

        if (type instanceof AbstractTypeSymbol abstractTypeSymbol) {
            return abstractTypeSymbol.getBType();
        }

        throw new IllegalArgumentException("Invalid array member type descriptor provided");
    }
}

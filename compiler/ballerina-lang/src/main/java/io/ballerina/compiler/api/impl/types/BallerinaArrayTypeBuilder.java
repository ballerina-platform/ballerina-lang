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

package io.ballerina.compiler.api.impl.types;

import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * The implementation of the methods used to build the Array type descriptor in Types API.
 *
 * @since 2201.2.0
 */
public class BallerinaArrayTypeBuilder implements TypeBuilder.ARRAY {

    private final TypesFactory typesFactory;
    private TypeSymbol type;
    private Integer size;

    public BallerinaArrayTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
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
        }

        return this;
    }

    @Override
    public ArrayTypeSymbol build() {
        BArrayType arrayType = new BArrayType(getBType(type));
        arrayType.size = size;

        return (ArrayTypeSymbol) typesFactory.getTypeDescriptor(arrayType);
    }

    private BType getBType(TypeSymbol type) {
        if (type != null) {
            if (type.typeKind() == TypeDescKind.ARRAY) {
                throw new IllegalArgumentException("Invalid array member type descriptor provided");
            }

            if (type instanceof AbstractTypeSymbol) {
                return ((AbstractTypeSymbol) type).getBType();
            }
        }

        throw new IllegalArgumentException("Array member type descriptor can not be null");
    }
}

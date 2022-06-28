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
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * The implementation of the methods used to build the Map type descriptor in Types API.
 *
 * @since 2201.2.0
 */
public class BallerinaMapTypeBuilder implements TypeBuilder.MAP {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private TypeSymbol typeParam;

    protected BallerinaMapTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
    }

    @Override
    public TypeBuilder.MAP withTypeParam(TypeSymbol typeParam) {
        this.typeParam = typeParam;
        return this;
    }

    @Override
    public MapTypeSymbol build() {
        BMapType mapType = new BMapType(TypeTags.MAP, getBType(typeParam), symTable.mapType.tsymbol);
        return (MapTypeSymbol) typesFactory.getTypeDescriptor(mapType);
    }

    private BType getBType(TypeSymbol typeSymbol) {
        if (typeSymbol != null) {
            if (typeSymbol instanceof AbstractTypeSymbol
                    && typeSymbol.subtypeOf(typesFactory.getTypeDescriptor(symTable.anyType))) {
                return ((AbstractTypeSymbol) typeSymbol).getBType();
            }

            throw new IllegalArgumentException("Valid type parameter of Map type should be provided");
        }

        return null;
    }
}

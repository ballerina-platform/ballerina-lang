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

package io.ballerina.compiler.api.types;

import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

public class BallerinaTypeDescTypeBuilder implements TypeBuilder.TypeDescTypeBuilder {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private TypeSymbol typeParam;

    public BallerinaTypeDescTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
    }

    @Override
    public TypeBuilder.TypeDescTypeBuilder withTypeParam(TypeSymbol typeParam) {
        this.typeParam = typeParam;
        return this;
    }

    @Override
    public TypeDescTypeSymbol build() {

        BTypedescType typedescType = new BTypedescType(getBType(typeParam), symTable.typeDesc.tsymbol);
        return (TypeDescTypeSymbol) typesFactory.getTypeDescriptor(typedescType);
    }

    private BType getBType(TypeSymbol typeSymbol) {
        if (typeSymbol != null) {
            if (typeSymbol instanceof AbstractTypeSymbol) {
                return ((AbstractTypeSymbol) typeSymbol).getBType();
            }
            return symTable.noType;
        }

        return null;
    }
}

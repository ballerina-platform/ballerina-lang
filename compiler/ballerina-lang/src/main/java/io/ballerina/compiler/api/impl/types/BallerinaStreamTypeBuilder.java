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
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * The implementation of the methods used to build the Stream type descriptor in Types API.
 *
 * @since 2201.2.0
 */
public class BallerinaStreamTypeBuilder implements TypeBuilder.STREAM {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private TypeSymbol valueType;
    private TypeSymbol completionType;

    public BallerinaStreamTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
    }

    @Override
    public TypeBuilder.STREAM withValueType(TypeSymbol valueType) {
        this.valueType = valueType;
        return this;
    }

    @Override
    public TypeBuilder.STREAM withCompletionType(TypeSymbol completionType) {
        this.completionType = completionType;
        return this;
    }

    @Override
    public StreamTypeSymbol build() {
        BStreamType streamType = new BStreamType(TypeTags.STREAM, getValueBType(this.valueType),
                getCompletionBType(this.completionType), symTable.streamType.tsymbol);

        // TODO: Clear the parameters.
        return (StreamTypeSymbol) typesFactory.getTypeDescriptor(streamType);
    }

    private BType getValueBType(TypeSymbol valueType) {
        if (valueType instanceof AbstractTypeSymbol) {
            return ((AbstractTypeSymbol) valueType).getBType();
        }

        return symTable.noType;
    }

    private BType getCompletionBType(TypeSymbol completionType) {
        if (completionType instanceof AbstractTypeSymbol) {
            return ((AbstractTypeSymbol) completionType).getBType();
        }

        return symTable.nilType;
    }
}

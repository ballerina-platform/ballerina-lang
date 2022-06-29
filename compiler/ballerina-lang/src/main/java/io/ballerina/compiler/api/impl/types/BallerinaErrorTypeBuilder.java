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
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * The implementation of the methods used to build the Error type descriptor.
 *
 * @since 2201.2.0
 */
public class BallerinaErrorTypeBuilder implements TypeBuilder.ERROR {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private TypeSymbol typeParam;

    public BallerinaErrorTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeBuilder.ERROR withTypeParam(TypeSymbol typeParam) {
        this.typeParam = typeParam;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ErrorTypeSymbol build() {
        BErrorType errorType = new BErrorType(symTable.errorType.tsymbol, getBType(typeParam));
        ErrorTypeSymbol errorTypeSymbol = (ErrorTypeSymbol) typesFactory.getTypeDescriptor(errorType);
        this.typeParam = null;

        return errorTypeSymbol;
    }

    private BType getBType(TypeSymbol typeSymbol) {
        if (typeSymbol == null) {
            return symTable.errorType.detailType;
        }

        if (isValidTypeParam(typeSymbol)) {
            return ((AbstractTypeSymbol) typeSymbol).getBType();
        }

        throw new IllegalArgumentException("Valid error detail mapping should be provided");
    }

    private boolean isValidTypeParam(TypeSymbol typeSymbol) {
        if (typeSymbol.typeKind() == TypeDescKind.RECORD) {
            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeSymbol;
            boolean hasString = false, hasError = false;
            for (RecordFieldSymbol recordFieldSymbol : recordTypeSymbol.fieldDescriptors().values()) {
                if (recordFieldSymbol.typeDescriptor().typeKind() == TypeDescKind.STRING
                        && (recordFieldSymbol.isOptional() || recordFieldSymbol.hasDefaultValue())) {
                    hasString = true;
                }

                if (recordFieldSymbol.typeDescriptor().typeKind() == TypeDescKind.ERROR
                        && recordFieldSymbol.isOptional()) {
                    hasError = true;
                }

                if (hasString && hasError) {
                    return true;
                }
            }
        }

        return false;
    }
}

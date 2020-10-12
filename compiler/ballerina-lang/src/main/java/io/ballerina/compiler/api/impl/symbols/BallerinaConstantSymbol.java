/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import java.util.Set;

/**
 * Represent Constant Symbol.
 *
 * @since 2.0.0
 */
public class BallerinaConstantSymbol extends BallerinaVariableSymbol implements ConstantSymbol {

    private final Object constValue;

    private BallerinaConstantSymbol(String name,
                                    PackageID moduleID,
                                    Set<Qualifier> qualifiers,
                                    BallerinaTypeDescriptor typeDescriptor,
                                    Object constValue,
                                    BSymbol bSymbol) {
        super(name, moduleID, SymbolKind.CONSTANT, qualifiers, typeDescriptor, bSymbol);
        this.constValue = constValue;
    }

    /**
     * Get the constant value.
     *
     * @return {@link Object} value of the constant
     */
    @Override
    public Object constValue() {
        return constValue;
    }

    /**
     * Represents Ballerina Constant Symbol Builder.
     */
    public static class ConstantSymbolBuilder extends VariableSymbolBuilder {

        private Object constantValue;

        public ConstantSymbolBuilder(String name, PackageID moduleID, BSymbol symbol) {
            super(name, moduleID, symbol);
        }

        public BallerinaConstantSymbol build() {
            return new BallerinaConstantSymbol(this.name,
                    this.moduleID,
                    this.qualifiers,
                    this.typeDescriptor,
                    this.constantValue,
                    this.bSymbol);
        }

        public ConstantSymbolBuilder withConstValue(Object constValue) {
            this.constantValue = constValue;
            return this;
        }

        @Override
        public ConstantSymbolBuilder withTypeDescriptor(BallerinaTypeDescriptor typeDescriptor) {
            super.withTypeDescriptor(typeDescriptor);
            return this;
        }
    }
}

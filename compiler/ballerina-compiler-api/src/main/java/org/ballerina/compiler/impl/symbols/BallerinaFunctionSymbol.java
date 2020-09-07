/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerina.compiler.impl.symbols;

import org.ballerina.compiler.api.symbols.FunctionSymbol;
import org.ballerina.compiler.api.symbols.Qualifier;
import org.ballerina.compiler.api.symbols.SymbolKind;
import org.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerina.compiler.api.types.FunctionTypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represent Function Symbol.
 *
 * @since 2.0.0
 */
public class BallerinaFunctionSymbol extends BallerinaSymbol implements FunctionSymbol {

    private final FunctionTypeDescriptor typeDescriptor;
    private final List<Qualifier> qualifiers;

    protected BallerinaFunctionSymbol(String name,
                                     PackageID moduleID,
                                     List<Qualifier> qualifiers,
                                     FunctionTypeDescriptor typeDescriptor,
                                     BInvokableSymbol invokableSymbol) {
        super(name, moduleID, SymbolKind.FUNCTION, invokableSymbol);
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.typeDescriptor = typeDescriptor;
    }

    /**
     * Get the list of qualifiers.
     *
     * @return {@link List} of qualifiers
     */
    @Override
    public List<Qualifier> qualifiers() {
        return qualifiers;
    }

    @Override
    public Optional<BallerinaTypeDescriptor> typeDescriptor() {
        return Optional.ofNullable(typeDescriptor);
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    static class FunctionSymbolBuilder extends SymbolBuilder<FunctionSymbolBuilder> {

        protected List<Qualifier> qualifiers = new ArrayList<>();
        protected FunctionTypeDescriptor typeDescriptor;

        public FunctionSymbolBuilder(String name, PackageID moduleID, BInvokableSymbol bSymbol) {
            this(name, moduleID, SymbolKind.FUNCTION, bSymbol);
        }

        public FunctionSymbolBuilder(String name, PackageID moduleID, SymbolKind kind,
                BInvokableSymbol bSymbol) {
            super(name, moduleID, kind, bSymbol);
        }

        public FunctionSymbolBuilder withTypeDescriptor(FunctionTypeDescriptor typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public FunctionSymbolBuilder withQualifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        @Override
        public BallerinaFunctionSymbol build() {
            return new BallerinaFunctionSymbol(this.name,
                    this.moduleID,
                    this.qualifiers,
                    this.typeDescriptor,
                    (BInvokableSymbol) this.bSymbol);
        }
    }
}

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

import org.ballerina.compiler.api.symbols.Qualifier;
import org.ballerina.compiler.api.symbols.SymbolKind;
import org.ballerina.compiler.api.symbols.VariableSymbol;
import org.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a ballerina variable.
 *
 * @since 1.3.0
 */
public class BallerinaVariableSymbol extends BallerinaSymbol implements VariableSymbol {

    private final List<Qualifier> qualifiers;
    private final BallerinaTypeDescriptor typeDescriptorImpl;

    protected BallerinaVariableSymbol(String name,
                                          PackageID moduleID,
                                          SymbolKind ballerinaSymbolKind,
                                          List<Qualifier> qualifiers,
                                          BallerinaTypeDescriptor typeDescriptorImpl,
                                          BSymbol bSymbol) {
        super(name, moduleID, ballerinaSymbolKind, bSymbol);
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.typeDescriptorImpl = typeDescriptorImpl;
    }

    /**
     * Get the list of access modifiers attached to this Variable symbol.
     *
     * @return {@link List} of access modifiers
     */
    @Override
    public List<Qualifier> qualifiers() {
        return qualifiers;
    }

    /**
     * Get the Type of the variable.
     *
     * @return {@link BallerinaTypeDescriptor} of the variable
     */
    @Override
    public Optional<BallerinaTypeDescriptor> typeDescriptor() {
        return Optional.ofNullable(typeDescriptorImpl);
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    static class VariableSymbolBuilder extends SymbolBuilder<VariableSymbolBuilder> {

        protected List<Qualifier> qualifiers = new ArrayList<>();
        protected BallerinaTypeDescriptor typeDescriptor;

        public VariableSymbolBuilder(String name, PackageID moduleID, BSymbol bSymbol) {
            super(name, moduleID, SymbolKind.VARIABLE, bSymbol);
        }

        @Override
        public BallerinaVariableSymbol build() {
            return new BallerinaVariableSymbol(this.name,
                    this.moduleID,
                    this.ballerinaSymbolKind,
                    this.qualifiers,
                    this.typeDescriptor,
                    this.bSymbol);
        }

        public VariableSymbolBuilder withTypeDescriptor(BallerinaTypeDescriptor typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public VariableSymbolBuilder withAccessModifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }
    }
}

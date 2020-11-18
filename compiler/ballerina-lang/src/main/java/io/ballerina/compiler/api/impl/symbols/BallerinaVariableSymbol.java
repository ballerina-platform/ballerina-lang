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

import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.util.Flags;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a ballerina variable.
 *
 * @since 2.0.0
 */
public class BallerinaVariableSymbol extends BallerinaSymbol implements VariableSymbol {

    private final Set<Qualifier> qualifiers;
    private final BallerinaTypeDescriptor typeDescriptorImpl;
    private final boolean deprecated;

    protected BallerinaVariableSymbol(String name,
                                      PackageID moduleID,
                                      SymbolKind ballerinaSymbolKind,
                                      Set<Qualifier> qualifiers,
                                      BallerinaTypeDescriptor typeDescriptorImpl,
                                      BSymbol bSymbol) {
        super(name, moduleID, ballerinaSymbolKind, bSymbol);
        this.qualifiers = Collections.unmodifiableSet(qualifiers);
        this.typeDescriptorImpl = typeDescriptorImpl;
        this.deprecated = Symbols.isFlagOn(bSymbol.flags, Flags.DEPRECATED);
    }

    /**
     * Get the list of access modifiers attached to this Variable symbol.
     *
     * @return {@link List} of access modifiers
     */
    @Override
    public Set<Qualifier> qualifiers() {
        return qualifiers;
    }

    /**
     * Get the Type of the variable.
     *
     * @return {@link BallerinaTypeDescriptor} of the variable
     */
    @Override
    public BallerinaTypeDescriptor typeDescriptor() {
        return typeDescriptorImpl;
    }

    @Override
    public boolean deprecated() {
        return this.deprecated;
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    public static class VariableSymbolBuilder extends SymbolBuilder<VariableSymbolBuilder> {

        protected Set<Qualifier> qualifiers = new HashSet<>();
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

        public VariableSymbolBuilder withQualifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }
    }
}

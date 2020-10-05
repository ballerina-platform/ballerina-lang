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
import org.ballerinalang.model.util.Flags;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Represent Function Symbol.
 *
 * @since 2.0.0
 */
public class BallerinaFunctionSymbol extends BallerinaSymbol implements FunctionSymbol {

    private final FunctionTypeDescriptor typeDescriptor;
    private final Set<Qualifier> qualifiers;
    private final boolean isExternal;

    protected BallerinaFunctionSymbol(String name,
                                      PackageID moduleID,
                                      Set<Qualifier> qualifiers,
                                      FunctionTypeDescriptor typeDescriptor,
                                      BInvokableSymbol invokableSymbol) {
        super(name, moduleID, SymbolKind.FUNCTION, invokableSymbol);
        this.qualifiers = Collections.unmodifiableSet(qualifiers);
        this.typeDescriptor = typeDescriptor;
        this.isExternal = Symbols.isFlagOn(invokableSymbol.flags, Flags.NATIVE);
    }

    /**
     * Get the list of qualifiers.
     *
     * @return {@link List} of qualifiers
     */
    @Override
    public Set<Qualifier> qualifiers() {
        return qualifiers;
    }

    @Override
    public Optional<BallerinaTypeDescriptor> typeDescriptor() {
        return Optional.ofNullable(typeDescriptor);
    }

    @Override
    public boolean external() {
        return this.isExternal;
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    static class FunctionSymbolBuilder extends SymbolBuilder<FunctionSymbolBuilder> {

        protected Set<Qualifier> qualifiers = new HashSet<>();
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

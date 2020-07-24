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
package org.ballerina.compiler.impl.symbol;

import org.ballerina.compiler.api.symbol.BallerinaSymbolKind;
import org.ballerina.compiler.api.symbol.FunctionSymbol;
import org.ballerina.compiler.api.symbol.Qualifier;
import org.ballerina.compiler.api.type.BallerinaFunctionTypeDescriptor;
import org.ballerina.compiler.api.type.BallerinaTypeDescriptor;
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
public class FunctionSymbolImplImpl extends BallerinaSymbolImpl implements FunctionSymbol {

    private final BallerinaFunctionTypeDescriptor typeDescriptor;

    private final List<Qualifier> qualifiers;

    protected FunctionSymbolImplImpl(String name,
                                     PackageID moduleID,
                                     List<Qualifier> qualifiers,
                                     BallerinaFunctionTypeDescriptor typeDescriptor,
                                     BInvokableSymbol invokableSymbol) {
        super(name, moduleID, BallerinaSymbolKind.FUNCTION, invokableSymbol);
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.typeDescriptor = typeDescriptor;
    }

    /**
     * Get the list of qualifiers.
     *
     * @return {@link List} of qualifiers
     */
    public List<Qualifier> qualifiers() {
        return qualifiers;
    }

    public Optional<BallerinaTypeDescriptor> typeDescriptor() {
        return Optional.ofNullable(typeDescriptor);
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    static class FunctionSymbolBuilder extends SymbolBuilder<FunctionSymbolBuilder> {

        protected List<Qualifier> qualifiers = new ArrayList<>();

        protected BallerinaFunctionTypeDescriptor typeDescriptor;

        public FunctionSymbolBuilder(String name, PackageID moduleID, BInvokableSymbol bSymbol) {
            this(name, moduleID, BallerinaSymbolKind.FUNCTION, bSymbol);
        }

        public FunctionSymbolBuilder(String name, PackageID moduleID, BallerinaSymbolKind kind,
                                     BInvokableSymbol bSymbol) {
            super(name, moduleID, kind, bSymbol);
        }

        public FunctionSymbolBuilder withTypeDescriptor(BallerinaFunctionTypeDescriptor typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public FunctionSymbolBuilder withQualifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        public FunctionSymbolImplImpl build() {
            return new FunctionSymbolImplImpl(this.name,
                    this.moduleID,
                    this.qualifiers,
                    this.typeDescriptor,
                    (BInvokableSymbol) this.bSymbol);
        }

    }
}

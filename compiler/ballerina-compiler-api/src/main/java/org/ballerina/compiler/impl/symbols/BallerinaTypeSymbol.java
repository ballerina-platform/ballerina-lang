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
import org.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a ballerina type definition implementation.
 *
 * @since 2.0.0
 */
public class BallerinaTypeSymbol extends BallerinaSymbol implements TypeSymbol {

    private final List<Qualifier> qualifiers;

    private final BallerinaTypeDescriptor typeDescriptor;

    protected BallerinaTypeSymbol(String name,
                                 PackageID moduleID,
                                 List<Qualifier> qualifiers,
                                 BallerinaTypeDescriptor typeDescriptor,
                                 BSymbol bSymbol) {
        super(name, moduleID, SymbolKind.TYPE_DEF, bSymbol);
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.typeDescriptor = typeDescriptor;
    }

    @Override
    public String moduleQualifiedName() {
        return this.moduleID().moduleName() + ":" + this.name();
    }

    @Override
    public List<Qualifier> qualifiers() {
        return qualifiers;
    }

    @Override
    public Optional<BallerinaTypeDescriptor> typeDescriptor() {
        return Optional.ofNullable(typeDescriptor);
    }

    /**
     * Represents a type definition symbol builder.
     *
     * @since 1.3.0
     */
    public static class TypeDefSymbolBuilder extends SymbolBuilder<TypeDefSymbolBuilder> {

        protected List<Qualifier> qualifiers = new ArrayList<>();
        protected BallerinaTypeDescriptor typeDescriptor;

        public TypeDefSymbolBuilder(String name, PackageID moduleID, BSymbol symbol) {
            super(name, moduleID, SymbolKind.TYPE_DEF, symbol);
        }

        public TypeDefSymbolBuilder withTypeDescriptor(BallerinaTypeDescriptor typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public TypeDefSymbolBuilder withAccessModifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        @Override
        public BallerinaTypeSymbol build() {
            return new BallerinaTypeSymbol(this.name, this.moduleID, this.qualifiers, this.typeDescriptor,
                    this.bSymbol);
        }
    }
}

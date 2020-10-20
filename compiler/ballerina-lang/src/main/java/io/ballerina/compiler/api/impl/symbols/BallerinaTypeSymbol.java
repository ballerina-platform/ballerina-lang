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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.util.Flags;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a ballerina type definition implementation.
 *
 * @since 2.0.0
 */
public class BallerinaTypeSymbol extends BallerinaSymbol implements TypeSymbol {

    private final Set<Qualifier> qualifiers;
    private final BallerinaTypeDescriptor typeDescriptor;
    private final boolean deprecated;
    private final boolean readonly;

    protected BallerinaTypeSymbol(String name,
                                  PackageID moduleID,
                                  Set<Qualifier> qualifiers,
                                  BallerinaTypeDescriptor typeDescriptor,
                                  BSymbol bSymbol) {
        super(name, moduleID, SymbolKind.TYPE, bSymbol);
        this.qualifiers = Collections.unmodifiableSet(qualifiers);
        this.typeDescriptor = typeDescriptor;
        this.deprecated = Symbols.isFlagOn(bSymbol.flags, Flags.DEPRECATED);
        this.readonly = Symbols.isFlagOn(bSymbol.flags, Flags.READONLY);
    }

    @Override
    public String moduleQualifiedName() {
        return this.moduleID().moduleName() + ":" + this.name();
    }

    @Override
    public Set<Qualifier> qualifiers() {
        return qualifiers;
    }

    @Override
    public BallerinaTypeDescriptor typeDescriptor() {
        return this.typeDescriptor;
    }

    @Override
    public boolean deprecated() {
        return this.deprecated;
    }

    @Override
    public boolean readonly() {
        return this.readonly;
    }

    /**
     * Represents a type definition symbol builder.
     *
     * @since 2.0.0
     */
    public static class TypeDefSymbolBuilder extends SymbolBuilder<TypeDefSymbolBuilder> {

        protected Set<Qualifier> qualifiers = new HashSet<>();
        protected BallerinaTypeDescriptor typeDescriptor;

        public TypeDefSymbolBuilder(String name, PackageID moduleID, BSymbol symbol) {
            super(name, moduleID, SymbolKind.TYPE, symbol);
        }

        public TypeDefSymbolBuilder withTypeDescriptor(BallerinaTypeDescriptor typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public TypeDefSymbolBuilder withQualifier(Qualifier qualifier) {
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

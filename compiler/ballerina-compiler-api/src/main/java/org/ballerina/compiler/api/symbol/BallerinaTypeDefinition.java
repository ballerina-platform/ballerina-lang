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
package org.ballerina.compiler.api.symbol;

import org.ballerina.compiler.api.types.TypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a ballerina type definition.
 * 
 * @since 1.3.0
 */
public class BallerinaTypeDefinition extends BallerinaSymbol {

    private final List<Qualifier> qualifiers;

    private final TypeDescriptor typeDescriptor;
    
    protected BallerinaTypeDefinition(String name,
                                      PackageID moduleID,
                                      List<Qualifier> qualifiers,
                                      TypeDescriptor typeDescriptor,
                                      BSymbol bSymbol) {
        super(name, moduleID, BallerinaSymbolKind.TYPE_DEF, bSymbol);
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.typeDescriptor = typeDescriptor;
    }
    
    public String moduleQualifiedName() {
        return this.moduleID().getModuleName() + ":" + this.name();
    }

    public List<Qualifier> qualifiers() {
        return qualifiers;
    }

    public TypeDescriptor typeDescriptor() {
        return typeDescriptor;
    }

    /**
     * Represents a type definition symbol builder.
     * 
     * @since 1.3.0
     */
    public static class TypeDefSymbolBuilder extends SymbolBuilder<TypeDefSymbolBuilder> {

        protected List<Qualifier> qualifiers = new ArrayList<>();

        protected TypeDescriptor typeDescriptor;
        
        public TypeDefSymbolBuilder(String name, PackageID moduleID, BSymbol symbol) {
            super(name, moduleID, BallerinaSymbolKind.TYPE_DEF, symbol);
        }

        public TypeDefSymbolBuilder withTypeDescriptor(TypeDescriptor typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public TypeDefSymbolBuilder withAccessModifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        @Override
        public BallerinaTypeDefinition build() {
            return new BallerinaTypeDefinition(this.name,
                    this.moduleID,
                    this.qualifiers,
                    this.typeDescriptor,
                    this.bSymbol);
        }
    }
}

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

import org.ballerina.compiler.api.semantic.TypesFactory;
import org.ballerina.compiler.api.types.TypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Represent Function Symbol.
 * 
 * @since 2.0.0
 */
public class BallerinaFunctionSymbol extends BallerinaSymbol {
    
    private final List<BallerinaParameter> parameters;
    
    private final BallerinaParameter restParam;
    
    private final TypeDescriptor returnType;

    private final List<Qualifier> qualifiers;

    private final TypeDescriptor typeDescriptor;
    
    protected BallerinaFunctionSymbol(String name,
                                    PackageID moduleID,
                                    BallerinaSymbolKind symbolKind,
                                    List<Qualifier> qualifiers,
                                    TypeDescriptor typeDescriptor,
                                    BInvokableSymbol invokableSymbol) {
        super(name, moduleID, symbolKind, invokableSymbol);
        this.parameters = invokableSymbol.params.stream()
                .map(SymbolFactory::createBallerinaParameter)
                .collect(Collectors.collectingAndThen(toList(), Collections::unmodifiableList));
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.typeDescriptor = typeDescriptor;
        this.restParam = SymbolFactory.createBallerinaParameter(invokableSymbol.restParam);
        this.returnType = TypesFactory.getTypeDescriptor(invokableSymbol.retType);
    }

    /**
     * Get the Required parameters.
     * 
     * @return {@link List} of return parameters
     */
    public List<BallerinaParameter> params() {
        return this.parameters;
    }

    /**
     * Get the rest parameter.
     * 
     * @return {@link Optional} rest parameter
     */
    public Optional<BallerinaParameter> restParam() {
        return Optional.ofNullable(restParam);
    }

    /**
     * Get the return parameter.
     * 
     * @return {@link Optional} return type descriptor
     */
    public Optional<TypeDescriptor> returnType() {
        return Optional.ofNullable(returnType);
    }

    /**
     * Get the list of qualifiers.
     * 
     * @return {@link List} of qualifiers
     */
    public List<Qualifier> qualifiers() {
        return qualifiers;
    }

    public Optional<TypeDescriptor> typeDescriptor() {
        return Optional.ofNullable(typeDescriptor);
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    static class FunctionSymbolBuilder extends SymbolBuilder<FunctionSymbolBuilder> {

        protected List<Qualifier> qualifiers = new ArrayList<>();

        protected TypeDescriptor typeDescriptor;
        
        public FunctionSymbolBuilder(String name, PackageID moduleID, BInvokableSymbol bSymbol) {
            this(name, moduleID, BallerinaSymbolKind.FUNCTION, bSymbol);
        }
        
        public FunctionSymbolBuilder(String name, PackageID moduleID, BallerinaSymbolKind kind,
                                     BInvokableSymbol bSymbol) {
            super(name, moduleID, kind, bSymbol);
        }
        
        public FunctionSymbolBuilder withTypeDescriptor(TypeDescriptor typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public FunctionSymbolBuilder withQualifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        public BallerinaFunctionSymbol build() {
            return new BallerinaFunctionSymbol(this.name,
                    this.moduleID,
                    BallerinaSymbolKind.FUNCTION,
                    this.qualifiers,
                    this.typeDescriptor,
                    (BInvokableSymbol) this.bSymbol);
        }

    }
}

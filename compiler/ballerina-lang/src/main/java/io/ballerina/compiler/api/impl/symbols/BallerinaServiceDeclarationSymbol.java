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

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.ServiceAttachPoint;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.api.symbols.SymbolKind.SERVICE_DECLARATION;

/**
 * Represents a service declaration.
 *
 * @since 2.0.0
 */
public class BallerinaServiceDeclarationSymbol extends BallerinaSymbol implements ServiceDeclarationSymbol {

    private TypeSymbol typeDescriptor;
    private ServiceAttachPoint attachPoint;
    private List<Qualifier> qualifiers;
    private List<AnnotationSymbol> annots;

    protected BallerinaServiceDeclarationSymbol(String name, TypeSymbol typeDescriptor, ServiceAttachPoint attachPoint,
                                                List<Qualifier> qualifiers, List<AnnotationSymbol> annots,
                                                BSymbol bSymbol, CompilerContext context) {
        super(name, SERVICE_DECLARATION, bSymbol, context);
        this.typeDescriptor = typeDescriptor;
        this.attachPoint = attachPoint;
        this.annots = Collections.unmodifiableList(annots);
        this.qualifiers = Collections.unmodifiableList(qualifiers);
    }

    @Override
    public Optional<TypeSymbol> typeDescriptor() {
        return Optional.ofNullable(this.typeDescriptor);
    }

    @Override
    public Optional<ServiceAttachPoint> attachPoint() {
        return Optional.ofNullable(this.attachPoint);
    }

    @Override
    public List<AnnotationSymbol> annotations() {
        return this.annots;
    }

    @Override
    public Optional<Documentation> documentation() {
        return Optional.empty();
    }

    @Override
    public List<Qualifier> qualifiers() {
        return this.qualifiers;
    }

    /**
     * A service declaration symbol builder.
     *
     * @since 2.0.0
     */
    public static class ServiceDeclSymbolBuilder
            extends SymbolBuilder<BallerinaServiceDeclarationSymbol.ServiceDeclSymbolBuilder> {

        protected List<Qualifier> qualifiers = new ArrayList<>();
        protected List<AnnotationSymbol> annots = new ArrayList<>();
        protected TypeSymbol typeDescriptor;
        protected ServiceAttachPoint attachPoint;

        public ServiceDeclSymbolBuilder(BSymbol symbol, CompilerContext context) {
            super(null, SERVICE_DECLARATION, symbol, context);
        }

        public ServiceDeclSymbolBuilder withTypeDescriptor(TypeSymbol typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public ServiceDeclSymbolBuilder withQualifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        public ServiceDeclSymbolBuilder withAnnotation(AnnotationSymbol annot) {
            this.annots.add(annot);
            return this;
        }

        public ServiceDeclSymbolBuilder withAttachPoint(ServiceAttachPoint attachPoint) {
            this.attachPoint = attachPoint;
            return this;
        }

        @Override
        public BallerinaServiceDeclarationSymbol build() {
            return new BallerinaServiceDeclarationSymbol(this.name, this.typeDescriptor, this.attachPoint,
                                                         this.qualifiers, this.annots, this.bSymbol, this.context);
        }
    }
}

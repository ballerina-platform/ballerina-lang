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
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a ballerina type definition implementation.
 *
 * @since 2.0.0
 */
public class BallerinaTypeDefinitionSymbol extends BallerinaSymbol implements TypeDefinitionSymbol {

    private final List<Qualifier> qualifiers;
    private final TypeSymbol typeDescriptor;
    private final Documentation docAttachment;
    private final boolean deprecated;
    private final boolean readonly;
    private final List<AnnotationSymbol> annots;

    protected BallerinaTypeDefinitionSymbol(String name, List<Qualifier> qualifiers, List<AnnotationSymbol> annots,
                                            TypeSymbol typeDescriptor, BSymbol bSymbol, CompilerContext context) {
        super(name, SymbolKind.TYPE_DEFINITION, bSymbol, context);
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.typeDescriptor = typeDescriptor;
        this.docAttachment = getDocAttachment(bSymbol);
        this.deprecated = Symbols.isFlagOn(bSymbol.flags, Flags.DEPRECATED);
        this.readonly = Symbols.isFlagOn(bSymbol.flags, Flags.READONLY);
        this.annots = Collections.unmodifiableList(annots);
    }

    @Override
    public String moduleQualifiedName() {
        return this.getModule().get().id().modulePrefix() + ":" + this.getName().get();
    }

    @Override
    public List<Qualifier> qualifiers() {
        return qualifiers;
    }

    @Override
    public TypeSymbol typeDescriptor() {
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

    @Override
    public List<AnnotationSymbol> annotations() {
        return this.annots;
    }

    @Override
    public Optional<Documentation> documentation() {
        return Optional.ofNullable(this.docAttachment);
    }

    /**
     * Represents a type definition symbol builder.
     *
     * @since 2.0.0
     */
    public static class TypeDefSymbolBuilder extends SymbolBuilder<TypeDefSymbolBuilder> {

        protected List<Qualifier> qualifiers = new ArrayList<>();
        protected List<AnnotationSymbol> annots = new ArrayList<>();
        protected TypeSymbol typeDescriptor;

        public TypeDefSymbolBuilder(String name, BSymbol symbol, CompilerContext context) {
            super(name, SymbolKind.TYPE_DEFINITION, symbol, context);
        }

        public TypeDefSymbolBuilder withTypeDescriptor(TypeSymbol typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public TypeDefSymbolBuilder withQualifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        public TypeDefSymbolBuilder withAnnotation(AnnotationSymbol annot) {
            this.annots.add(annot);
            return this;
        }

        @Override
        public BallerinaTypeDefinitionSymbol build() {
            return new BallerinaTypeDefinitionSymbol(this.name, this.qualifiers, this.annots, this.typeDescriptor,
                                                     this.bSymbol, this.context);
        }
    }
}

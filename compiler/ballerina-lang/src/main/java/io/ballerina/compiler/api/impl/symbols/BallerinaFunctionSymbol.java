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
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represent Function Symbol.
 *
 * @since 2.0.0
 */
public class BallerinaFunctionSymbol extends BallerinaSymbol implements FunctionSymbol {

    private final FunctionTypeSymbol typeDescriptor;
    private final List<Qualifier> qualifiers;
    private final List<AnnotationSymbol> annots;
    private final Documentation docAttachment;
    private final boolean isExternal;
    private final boolean deprecated;

    protected BallerinaFunctionSymbol(String name, List<Qualifier> qualifiers, List<AnnotationSymbol> annots,
                                      FunctionTypeSymbol typeDescriptor, BInvokableSymbol invokableSymbol,
                                      CompilerContext context) {
        super(name, SymbolKind.FUNCTION, invokableSymbol, context);
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.annots = Collections.unmodifiableList(annots);
        this.docAttachment = getDocAttachment(invokableSymbol);
        this.typeDescriptor = typeDescriptor;
        this.isExternal = Symbols.isNative(invokableSymbol);
        this.deprecated = Symbols.isFlagOn(invokableSymbol.flags, Flags.DEPRECATED);
    }

    /**
     * Get the list of qualifiers.
     *
     * @return {@link List} of qualifiers
     */
    @Override
    public List<Qualifier> qualifiers() {
        return qualifiers;
    }

    @Override
    public FunctionTypeSymbol typeDescriptor() {
        return this.typeDescriptor;
    }

    @Override
    public boolean external() {
        return this.isExternal;
    }

    @Override
    public boolean deprecated() {
        return this.deprecated;
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
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    public static class FunctionSymbolBuilder extends SymbolBuilder<FunctionSymbolBuilder> {

        protected List<Qualifier> qualifiers = new ArrayList<>();
        protected List<AnnotationSymbol> annots = new ArrayList<>();
        protected FunctionTypeSymbol typeDescriptor;

        public FunctionSymbolBuilder(String name, BInvokableSymbol bSymbol, CompilerContext context) {
            this(name, SymbolKind.FUNCTION, bSymbol, context);
        }

        public FunctionSymbolBuilder(String name, SymbolKind kind, BInvokableSymbol bSymbol, CompilerContext context) {
            super(name, kind, bSymbol, context);
        }

        public FunctionSymbolBuilder withTypeDescriptor(FunctionTypeSymbol typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            return this;
        }

        public FunctionSymbolBuilder withQualifier(Qualifier qualifier) {
            this.qualifiers.add(qualifier);
            return this;
        }

        public FunctionSymbolBuilder withQualifiers(List<Qualifier> qualifiers) {
            this.qualifiers.addAll(qualifiers);
            return this;
        }

        public FunctionSymbolBuilder withAnnotation(AnnotationSymbol annot) {
            this.annots.add(annot);
            return this;
        }

        @Override
        public BallerinaFunctionSymbol build() {
            return new BallerinaFunctionSymbol(this.name, this.qualifiers, this.annots,
                    this.typeDescriptor, (BInvokableSymbol) this.bSymbol, this.context);
        }
    }
}

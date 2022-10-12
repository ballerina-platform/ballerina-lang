/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ClientDeclSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BClientDeclarationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an client declaration Symbol.
 *
 * @since 2201.3.0
 */
public class BallerinaClientDeclSymbol extends BallerinaSymbol implements ClientDeclSymbol {

    private final String serviceUri;
    private ModuleSymbol moduleSymbol;
    private final List<AnnotationSymbol> annots;
    private final List<AnnotationAttachmentSymbol> annotAttachments;

    private BallerinaClientDeclSymbol(String name, BSymbol symbol, String serviceUri, List<AnnotationSymbol> annots,
                                      List<AnnotationAttachmentSymbol> annotAttachments, CompilerContext context) {
        super(name, SymbolKind.CLIENT_DECLARATION, symbol, context);
        this.annotAttachments = Collections.unmodifiableList(annotAttachments);
        this.serviceUri = serviceUri;
        this.annots = annots;
    }

    @Override
    public String serviceUri() {
        return this.serviceUri;
    }

    @Override
    public ModuleSymbol moduleSymbol() {
        if (this.moduleSymbol != null) {
            return moduleSymbol;
        }

        SymbolFactory symbolFactory = SymbolFactory.getInstance(context);
        this.moduleSymbol = symbolFactory.getAssociatedModule((BClientDeclarationSymbol) this.getInternalSymbol());
        return this.moduleSymbol;
    }

    @Override
    public List<AnnotationSymbol> annotations() {
        return this.annots;
    }

    @Override
    public List<AnnotationAttachmentSymbol> annotAttachments() {
        return this.annotAttachments;
    }

    @Override
    public void accept(SymbolVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SymbolTransformer<T> transformer) {
        return transformer.transform(this);
    }

    /**
     * Represents Ballerina client-declaration Symbol Builder.
     */
    public static class ClientDeclSymbolBuilder extends SymbolBuilder<ClientDeclSymbolBuilder> {

        protected String uri;
        private List<AnnotationSymbol> annots = new ArrayList<>();
        private List<AnnotationAttachmentSymbol> annotAttachments = new ArrayList<>();

        /**
         * Symbol Builder's Constructor.
         *
         * @param name    Symbol Name
         * @param symbol  client-declaration symbol
         * @param context context of the compilation
         */
        public ClientDeclSymbolBuilder(String name, BClientDeclarationSymbol symbol, CompilerContext context) {
            super(name, SymbolKind.CLIENT_DECLARATION, symbol, context);
            this.uri = symbol.uri;
        }

        public ClientDeclSymbolBuilder withAnnotation(AnnotationSymbol annot) {
            this.annots.add(annot);
            return this;
        }

        public ClientDeclSymbolBuilder withAnnotationAttachment(AnnotationAttachmentSymbol annotationAttachment) {
            this.annotAttachments.add(annotationAttachment);
            return this;
        }

        @Override
        public BallerinaClientDeclSymbol build() {
            return new BallerinaClientDeclSymbol(this.name, this.bSymbol, this.uri, this.annots, this.annotAttachments,
                                                 this.context);
        }
    }
}

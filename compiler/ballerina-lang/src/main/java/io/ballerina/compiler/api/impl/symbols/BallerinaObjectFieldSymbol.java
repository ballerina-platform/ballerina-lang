/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerinalang.model.symbols.AnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static io.ballerina.compiler.api.symbols.Qualifier.PUBLIC;
import static io.ballerina.compiler.api.symbols.SymbolKind.OBJECT_FIELD;

/**
 * Represents a field in an object type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaObjectFieldSymbol extends BallerinaSymbol implements ObjectFieldSymbol {

    protected final BField bField;
    protected List<Qualifier> qualifiers;
    private final Documentation docAttachment;
    private TypeSymbol typeDescriptor;
    private List<AnnotationSymbol> annots;
    private String signature;
    private boolean deprecated;

    public BallerinaObjectFieldSymbol(CompilerContext context, BField bField, SymbolKind kind) {
        super(bField.symbol.getOriginalName().value, kind, bField.symbol, context);
        this.bField = bField;
        this.docAttachment = new BallerinaDocumentation(bField.symbol.markdownDocumentation);
        this.deprecated = Symbols.isFlagOn(bField.symbol.flags, Flags.DEPRECATED);
    }

    public BallerinaObjectFieldSymbol(CompilerContext context, BField bField) {
        this(context, bField, OBJECT_FIELD);
    }

    @Override
    public TypeSymbol typeDescriptor() {
        if (this.typeDescriptor == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.typeDescriptor = typesFactory.getTypeDescriptor(this.bField.type);
        }

        return this.typeDescriptor;
    }

    @Override
    public List<AnnotationSymbol> annotations() {
        if (this.annots != null) {
            return this.annots;
        }

        List<AnnotationSymbol> annots = new ArrayList<>();
        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        for (AnnotationAttachmentSymbol annot : bField.symbol.getAnnotations()) {
            annots.add(symbolFactory.createAnnotationSymbol((BAnnotationAttachmentSymbol) annot));
        }

        this.annots = Collections.unmodifiableList(annots);
        return this.annots;
    }

    @Override
    public boolean deprecated() {
        return this.deprecated;
    }

    @Override
    public Optional<Documentation> documentation() {
        return Optional.ofNullable(docAttachment);
    }

    @Override
    public List<Qualifier> qualifiers() {
        if (this.qualifiers != null) {
            return this.qualifiers;
        }

        List<Qualifier> quals = new ArrayList<>();
        if (Symbols.isFlagOn(this.bField.symbol.flags, Flags.PUBLIC)) {
            quals.add(PUBLIC);
        }

        this.qualifiers = Collections.unmodifiableList(quals);
        return this.qualifiers;
    }

    @Override
    public String signature() {
        if (this.signature != null) {
            return this.signature;
        }

        StringJoiner joiner = new StringJoiner(" ");

        for (Qualifier qualifier : this.qualifiers()) {
            joiner.add(qualifier.getValue());
        }

        this.signature = joiner.add(this.typeDescriptor().signature()).add(this.getName().get()).toString();
        return this.signature;
    }

    @Override
    public void accept(SymbolVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SymbolTransformer<T> transformer) {
        return transformer.transform(this);
    }
}

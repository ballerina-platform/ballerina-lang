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
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
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

import static io.ballerina.compiler.api.symbols.Qualifier.READONLY;

/**
 * Represents a field with a name and type.
 *
 * @since 2.0.0
 */
public class BallerinaRecordFieldSymbol extends BallerinaSymbol implements RecordFieldSymbol {

    private final Documentation docAttachment;
    private final BField bField;
    private TypeSymbol typeDescriptor;
    private List<AnnotationSymbol> annots;
    private List<Qualifier> qualifiers;
    private String signature;
    private boolean deprecated;

    public BallerinaRecordFieldSymbol(CompilerContext context, BField bField) {
        super(bField.symbol.getOriginalName().value, SymbolKind.RECORD_FIELD, bField.symbol, context);
        this.bField = bField;
        this.docAttachment = new BallerinaDocumentation(bField.symbol.markdownDocumentation);
        this.deprecated = Symbols.isFlagOn(bField.symbol.flags, Flags.DEPRECATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOptional() {
        return Symbols.isFlagOn(this.bField.symbol.flags, Flags.OPTIONAL);
    }

    @Override
    public boolean hasDefaultValue() {
        return !isOptional() && !Symbols.isFlagOn(this.bField.symbol.flags, Flags.REQUIRED);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Documentation> documentation() {
        return Optional.ofNullable(docAttachment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Qualifier> qualifiers() {
        if (this.qualifiers != null) {
            return this.qualifiers;
        }

        List<Qualifier> quals = new ArrayList<>();
        final long symFlags = this.bField.symbol.flags;

        if (Symbols.isFlagOn(symFlags, Flags.READONLY)) {
            quals.add(READONLY);
        }

        this.qualifiers = Collections.unmodifiableList(quals);
        return this.qualifiers;
    }

    /**
     * {@inheritDoc}
     */
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

        if (this.isOptional()) {
            this.signature += "?";
        }

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

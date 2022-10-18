/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;

/**
 * Represents a parameter with a name and type.
 *
 * @since 2.0.0
 */
public class BallerinaParameterSymbol extends BallerinaSymbol implements ParameterSymbol {

    // add the metadata field
    private final List<Qualifier> qualifiers;
    private final List<AnnotationSymbol> annots;
    private final List<AnnotationAttachmentSymbol> annotAttachments;
    private final TypeSymbol typeDescriptor;
    private final ParameterKind paramKind;

    public BallerinaParameterSymbol(String parameterName, TypeSymbol typeDescriptor, List<Qualifier> qualifiers,
                                    List<AnnotationSymbol> annots, List<AnnotationAttachmentSymbol> annotAttachments,
                                    ParameterKind paramKind, BVarSymbol symbol, CompilerContext context) {
        super(parameterName, PARAMETER, symbol, context);
        // TODO: Add the metadata
        this.typeDescriptor = typeDescriptor;
        this.qualifiers = Collections.unmodifiableList(qualifiers);
        this.annots = Collections.unmodifiableList(annots);
        this.annotAttachments = Collections.unmodifiableList(annotAttachments);
        this.paramKind = paramKind;
    }

    /**
     * Get the type descriptor of the field.
     *
     * @return {@link TypeSymbol} of the field
     */
    @Override
    public TypeSymbol typeDescriptor() {
        return typeDescriptor;
    }

    /**
     * Get the access modifiers.
     *
     * @return {@link List} of access modifiers
     */
    @Override
    public List<Qualifier> qualifiers() {
        return qualifiers;
    }

    @Override
    public List<AnnotationSymbol> annotations() {
        return this.annots;
    }

    @Override
    public List<AnnotationAttachmentSymbol> annotAttachments() {
        return this.annotAttachments;
    }

    /**
     * Get the signature of the field.
     *
     * @return {@link String} signature
     */
    @Override
    public String signature() {
        StringJoiner joiner = new StringJoiner(" ");
        this.qualifiers().forEach(accessModifier -> joiner.add(accessModifier.getValue()));
        String signature;
        if (this.paramKind() == ParameterKind.INCLUDED_RECORD) {
            signature = "*" + this.typeDescriptor().signature();
        } else if (this.paramKind() == ParameterKind.REST) {
            signature = this.typeDescriptor().signature();
            signature = signature.substring(0, signature.length() - 2) + "...";
        } else {
            signature = this.typeDescriptor().signature();
        }
        joiner.add(signature);
        if (this.getName().isPresent()) {
            joiner.add(this.getName().get());
        }

        return joiner.toString();
    }

    @Override
    public ParameterKind paramKind() {
        return this.paramKind;
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

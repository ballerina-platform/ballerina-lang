/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.values.ConstantValue;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Optional;

/**
 * Represents an Annotation attachment.
 *
 * @since 2201.3.0
 */
public class BallerinaAnnotationAttachmentSymbol extends BallerinaSymbol implements AnnotationAttachmentSymbol {

    private final AnnotationSymbol typeDescriptorImpl;
    private final ConstantValue attachmentValue;
    private final boolean isConstAnnotation;

    public BallerinaAnnotationAttachmentSymbol(String name, BAnnotationAttachmentSymbol annotAttachmentSymbol,
                                               AnnotationSymbol annotationSymbol, CompilerContext context) {
        super(name, SymbolKind.ANNOTATION_ATTACHMENT, annotAttachmentSymbol, context);
        this.typeDescriptorImpl = annotationSymbol;
        this.isConstAnnotation = false;
        this.attachmentValue = null;
    }

    public BallerinaAnnotationAttachmentSymbol(String name,
                                               BAnnotationAttachmentSymbol.
                                                       BConstAnnotationAttachmentSymbol annotAttachmentSymbol,
                                               AnnotationSymbol annotationSymbol,
                                               ConstantValue attachmentValue,
                                               CompilerContext context) {
        super(name, SymbolKind.ANNOTATION_ATTACHMENT, annotAttachmentSymbol, context);
        this.typeDescriptorImpl = annotationSymbol;
        this.isConstAnnotation = annotAttachmentSymbol.isConstAnnotation();
        this.attachmentValue = attachmentValue;
    }

    @Override
    public boolean isConstAnnotation() {
        return this.isConstAnnotation;
    }

    @Override
    public Optional<ConstantValue> attachmentValue() {
        return Optional.of(attachmentValue);
    }

    @Override
    public AnnotationSymbol typeDescriptor() {
        return typeDescriptorImpl;
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

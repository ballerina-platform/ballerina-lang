/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import org.ballerinalang.model.types.ArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BResourcePathSegmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents an implementation of path param symbol.
 *
 * @since 2.0.0
 */
public class BallerinaPathParameterSymbol extends BallerinaSymbol implements PathParameterSymbol {
    private static final String TYPE_ONLY_PATH_REST_PARAM = "$^^";
    private static final String TYPE_ONLY_PATH_PARAM = "$^";
    private final PathSegment.Kind segmentKind;
    private final boolean isTypeOnlyPathParam;
    private TypeSymbol typeDescriptor;
    private List<AnnotationSymbol> annots;
    private List<io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol> annotAttachments;
    private String signature;

    public BallerinaPathParameterSymbol(String name, PathSegment.Kind kind, BSymbol symbol, CompilerContext context) {
        super(name, SymbolKind.PATH_PARAMETER, symbol, context);
        this.segmentKind = kind;
        this.isTypeOnlyPathParam = TYPE_ONLY_PATH_REST_PARAM.equals(name) || TYPE_ONLY_PATH_PARAM.equals(name);
    }

    @Override
    public Optional<String> getName() {
        if (this.isTypeOnlyPathParam) {
            return Optional.empty();
        }
        return super.getName();
    }

    @Override
    public List<AnnotationSymbol> annotations() {
        if (this.annots != null) {
            return this.annots;
        }

        // TODO: Remove this once #41246 is fixed
        if (this.getInternalSymbol() instanceof BResourcePathSegmentSymbol) {
            this.annots =  Collections.emptyList();
            return this.annots;
        }

        BVarSymbol symbol = (BVarSymbol) this.getInternalSymbol();
        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        List<AnnotationSymbol> annotSymbols = new ArrayList<>();

        for (org.ballerinalang.model.symbols.AnnotationAttachmentSymbol annot : symbol.getAnnotations()) {
            annotSymbols.add(symbolFactory.createAnnotationSymbol((BAnnotationAttachmentSymbol) annot));
        }

        this.annots = Collections.unmodifiableList(annotSymbols);
        return this.annots;
    }

    @Override
    public List<AnnotationAttachmentSymbol> annotAttachments() {
        if (this.annotAttachments != null) {
            return this.annotAttachments;
        }

        // TODO: Remove this once #41246 is fixed
        if (this.getInternalSymbol() instanceof BResourcePathSegmentSymbol) {
            this.annotAttachments =  Collections.emptyList();
            return this.annotAttachments;
        }

        BVarSymbol symbol = (BVarSymbol) this.getInternalSymbol();
        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        List<AnnotationAttachmentSymbol> annotAttachments = new ArrayList<>();

        for (org.ballerinalang.model.symbols.AnnotationAttachmentSymbol annot : symbol.getAnnotations()) {
            annotAttachments.add(symbolFactory.createAnnotAttachment((BAnnotationAttachmentSymbol) annot));
        }

        this.annotAttachments = Collections.unmodifiableList(annotAttachments);
        return this.annotAttachments;
    }

    @Override
    public boolean isTypeOnlyParam() {
        return this.isTypeOnlyPathParam;
    }

    @Override
    public TypeSymbol typeDescriptor() {
        if (this.typeDescriptor != null) {
            return this.typeDescriptor;
        }

        BType bType;
        if (this.segmentKind == Kind.PATH_REST_PARAMETER && this.getInternalSymbol() instanceof BVarSymbol) {
            // TODO: Remove `this.getInternalSymbol() instanceof BVarSymbol` check once #41246 is fixed
            bType = (BType) ((ArrayType) this.getInternalSymbol().type).getElementType();
        } else {
            bType = this.getInternalSymbol().type;
        }

        TypesFactory typesFactory = TypesFactory.getInstance(this.context);
        this.typeDescriptor = typesFactory.getTypeDescriptor(bType);
        return this.typeDescriptor;
    }

    @Override
    public Kind pathSegmentKind() {
        return this.segmentKind;
    }

    @Override
    public String signature() {
        if (this.signature != null) {
            return this.signature;
        }

        String typeSignature;
        if (this.pathSegmentKind() == Kind.PATH_REST_PARAMETER) {
            typeSignature = this.typeDescriptor().signature() + "...";
        } else {
            typeSignature = this.typeDescriptor().signature();
        }

        this.signature = this.getName().isPresent() ?
                "[" + typeSignature + " " + this.getName().get() + "]" :
                "[" + typeSignature + "]";

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

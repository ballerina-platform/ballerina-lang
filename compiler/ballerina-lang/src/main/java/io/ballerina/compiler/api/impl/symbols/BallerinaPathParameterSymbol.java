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

import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an implementation of path param symbol.
 *
 * @since 2.0.0
 */
public class BallerinaPathParameterSymbol extends BallerinaSymbol implements PathParameterSymbol {

    private final PathSegment.Kind segmentKind;
    private TypeSymbol typeDescriptor;
    private List<AnnotationSymbol> annots;
    private String signature;

    public BallerinaPathParameterSymbol(PathSegment.Kind kind, BSymbol symbol, CompilerContext context) {
        super(symbol.getOriginalName().getValue(), SymbolKind.PATH_PARAMETER, symbol, context);
        this.segmentKind = kind;
    }

    @Override
    public List<AnnotationSymbol> annotations() {
        if (this.annots != null) {
            return this.annots;
        }

        BVarSymbol symbol = (BVarSymbol) this.getInternalSymbol();
        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        List<AnnotationSymbol> annotSymbols = new ArrayList<>();

        for (org.ballerinalang.model.symbols.AnnotationSymbol annot : symbol.getAnnotations()) {
            annotSymbols.add(symbolFactory.createAnnotationSymbol((BAnnotationSymbol) annot));
        }

        this.annots = Collections.unmodifiableList(annotSymbols);
        return this.annots;
    }

    @Override
    public TypeSymbol typeDescriptor() {
        if (this.typeDescriptor != null) {
            return this.typeDescriptor;
        }

        TypesFactory typesFactory = TypesFactory.getInstance(this.context);
        this.typeDescriptor = typesFactory.getTypeDescriptor(this.getInternalSymbol().type);
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
            typeSignature = ((ArrayTypeSymbol) this.typeDescriptor()).memberTypeDescriptor().signature() + "...";
        } else {
            typeSignature = this.typeDescriptor().signature();
        }

        this.signature = "[" + typeSignature + " " + this.getName().get() + "]";
        return this.signature;
    }
}

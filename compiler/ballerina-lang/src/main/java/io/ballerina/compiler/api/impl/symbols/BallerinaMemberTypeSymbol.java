/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.MemberTypeSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a tuple member symbol.
 *
 * @since 2201.4.0
 */
public class BallerinaMemberTypeSymbol extends BallerinaSymbol implements MemberTypeSymbol {

    private List<AnnotationSymbol> annots;
    private List<AnnotationAttachmentSymbol> annotAttachments;
    private final TypeSymbol type;

    public BallerinaMemberTypeSymbol(CompilerContext context,
                                     BVarSymbol symbol,
                                     TypeSymbol type) {
        super(null, SymbolKind.TUPLE_MEMBER, symbol, context);
        this.type = type;
    }

    @Override
    public Optional<String> getName() {
        return Optional.empty();
    }

    public TypeSymbol typeDescriptor() {
        return this.type;
    }

    @Override
    public List<AnnotationSymbol> annotations() {
        if (this.annots != null) {
            return this.annots;
        }

        List<AnnotationSymbol> annots = new ArrayList<>();
        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        for (org.ballerinalang.model.symbols.AnnotationAttachmentSymbol annot :
                ((BVarSymbol) this.getInternalSymbol()).getAnnotations()) {
            annots.add(symbolFactory.createAnnotationSymbol((BAnnotationAttachmentSymbol) annot));
        }

        this.annots = Collections.unmodifiableList(annots);
        return this.annots;
    }

    @Override
    public List<AnnotationAttachmentSymbol> annotAttachments() {
        if (this.annotAttachments != null) {
            return this.annotAttachments;
        }

        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        List<AnnotationAttachmentSymbol> annotAttachments = new ArrayList<>();

        for (org.ballerinalang.model.symbols.AnnotationAttachmentSymbol annot :
                ((BVarSymbol) this.getInternalSymbol()).getAnnotations()) {
            annotAttachments.add(symbolFactory.createAnnotAttachment((BAnnotationAttachmentSymbol) annot));
        }

        this.annotAttachments = Collections.unmodifiableList(annotAttachments);
        return this.annotAttachments;
    }
}

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

import io.ballerina.compiler.api.SymbolTransformer;
import io.ballerina.compiler.api.SymbolVisitor;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.WorkerSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ballerina worker.
 *
 * @since 2.0.0
 */
public class BallerinaWorkerSymbol extends BallerinaSymbol implements WorkerSymbol {

    private TypeSymbol returnType;
    private List<AnnotationSymbol> annots;
    private final List<AnnotationAttachmentSymbol> annotAttachments;

    private BallerinaWorkerSymbol(String name, SymbolKind ballerinaSymbolKind, TypeSymbol returnType,
                                  List<AnnotationSymbol> annots, List<AnnotationAttachmentSymbol> annotAttachments,
                                  BSymbol symbol, CompilerContext context) {
        super(name, ballerinaSymbolKind, symbol, context);
        this.returnType = returnType;
        this.annots = annots;
        this.annotAttachments = annotAttachments;
    }

    /**
     * Get the return type.
     *
     * @return {@link TypeSymbol} return type of the worker.
     */
    @Override
    public TypeSymbol returnType() {
        return returnType;
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
     * Represents Ballerina Worker Symbol Builder.
     */
    public static class WorkerSymbolBuilder extends SymbolBuilder<WorkerSymbolBuilder> {

        protected TypeSymbol returnType;
        protected List<AnnotationSymbol> annots = new ArrayList<>();
        protected List<AnnotationAttachmentSymbol> annotAttachments = new ArrayList<>();

        public WorkerSymbolBuilder(String name, BSymbol symbol, CompilerContext context) {
            super(name, SymbolKind.WORKER, symbol, context);
        }

        @Override
        public BallerinaWorkerSymbol build() {
            return new BallerinaWorkerSymbol(this.name, this.ballerinaSymbolKind, this.returnType, this.annots,
                                             this.annotAttachments, this.bSymbol, this.context);
        }

        public WorkerSymbolBuilder withReturnType(TypeSymbol typeDescriptor) {
            this.returnType = typeDescriptor;
            return this;
        }

        public WorkerSymbolBuilder withAnnotation(AnnotationSymbol annot) {
            this.annots.add(annot);
            return this;
        }

        public WorkerSymbolBuilder withAnnotationAttachment(AnnotationAttachmentSymbol annotAttachment) {
            this.annotAttachments.add(annotAttachment);
            return this;
        }
    }
}

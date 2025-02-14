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
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.Annotatable;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationAttachmentSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.ParameterKind.DEFAULTABLE;
import static io.ballerina.compiler.api.symbols.ParameterKind.INCLUDED_RECORD;
import static io.ballerina.compiler.api.symbols.ParameterKind.REQUIRED;
import static io.ballerina.compiler.api.symbols.ParameterKind.REST;
import static java.util.stream.Collectors.toList;

/**
 * Represents a function type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaFunctionTypeSymbol extends AbstractTypeSymbol implements FunctionTypeSymbol {

    private List<ParameterSymbol> requiredParams;
    private List<ParameterSymbol> params;
    private ParameterSymbol restParam;
    private TypeSymbol returnType;
    private Annotatable returnTypeAnnots;
    private final BInvokableTypeSymbol typeSymbol;
    private String signature;

    public BallerinaFunctionTypeSymbol(CompilerContext context, BInvokableTypeSymbol invokableSymbol, BType type) {
        super(context, TypeDescKind.FUNCTION, type);
        this.typeSymbol = invokableSymbol;
    }

    @Override
    public List<ParameterSymbol> parameters() {
        if (this.requiredParams == null) {
            // Becomes null for the function typedesc.
            if (this.typeSymbol.params == null) {
                this.requiredParams = Collections.emptyList();
                return this.requiredParams;
            }

            SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
            this.requiredParams = this.typeSymbol.params.stream()
                    .filter(symbol -> symbol.kind != SymbolKind.PATH_PARAMETER
                            && symbol.kind != SymbolKind.PATH_REST_PARAMETER)
                    .map(symbol -> {
                        ParameterKind parameterKind = symbol.isDefaultable ? DEFAULTABLE : REQUIRED;
                        return symbolFactory.createBallerinaParameter(symbol, parameterKind);
                    })
                    .collect(Collectors.collectingAndThen(toList(), Collections::unmodifiableList));
        }

        return this.requiredParams;
    }

    @Override
    public Optional<List<ParameterSymbol>> params() {
        if (this.params != null) {
            return Optional.of(this.params);
        }

        if (Symbols.isFlagOn(this.typeSymbol.flags, Flags.ANY_FUNCTION)) {
            return Optional.empty();
        }

        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        List<ParameterSymbol> params = new ArrayList<>();

        for (BVarSymbol param : this.typeSymbol.params) {
            ParameterKind paramKind;

            if (Symbols.isFlagOn(param.flags, Flags.REQUIRED_PARAM)) {
                paramKind = REQUIRED;
            } else if (Symbols.isFlagOn(param.flags, Flags.DEFAULTABLE_PARAM)) {
                paramKind = DEFAULTABLE;
            } else if (Symbols.isFlagOn(param.flags, Flags.INCLUDED)) {
                paramKind = INCLUDED_RECORD;
            } else {
                continue;
            }

            params.add(symbolFactory.createBallerinaParameter(param, paramKind));
        }
        this.params = Collections.unmodifiableList(params);
        return Optional.of(this.params);
    }

    @Override
    public Optional<ParameterSymbol> restParam() {
        if (restParam == null) {
            SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
            this.restParam = symbolFactory.createBallerinaParameter(typeSymbol.restParam, REST);
        }

        return Optional.ofNullable(this.restParam);
    }

    @Override
    public Optional<TypeSymbol> returnTypeDescriptor() {
        if (returnType == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.returnType = typesFactory.getTypeDescriptor(this.typeSymbol.returnType);
        }

        return Optional.ofNullable(this.returnType);
    }

    @Override
    public Optional<Annotatable> returnTypeAnnotations() {
        if (this.returnTypeAnnots != null) {
            return Optional.of(this.returnTypeAnnots);
        }

        if (this.typeSymbol.returnTypeAnnots.isEmpty()) {
            return Optional.empty();
        }

        SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);
        List<AnnotationSymbol> annots = new ArrayList<>();
        List<AnnotationAttachmentSymbol> annotationAttachments = new ArrayList<>();

        for (BAnnotationAttachmentSymbol annot : this.typeSymbol.returnTypeAnnots) {
            BallerinaAnnotationAttachmentSymbol annotAttachment =
                    symbolFactory.createAnnotAttachment(annot);
            annots.add(annotAttachment.typeDescriptor());
            annotationAttachments.add(annotAttachment);
        }

        AnnotatableReturnType annotatableReturnType = new AnnotatableReturnType();
        annotatableReturnType.setAnnotations(Collections.unmodifiableList(annots));
        annotatableReturnType.setAnnotAttachments(Collections.unmodifiableList(annotationAttachments));
        this.returnTypeAnnots = annotatableReturnType;

        return Optional.of(this.returnTypeAnnots);
    }

    @Override
    public String signature() {
        if (this.signature != null) {
            return this.signature;
        }

        if (Symbols.isFlagOn(getBType().flags, Flags.ANY_FUNCTION)) {
            this.signature = "function";
            return this.signature;
        }

        StringBuilder signature = new StringBuilder("function (");
        StringJoiner joiner = new StringJoiner(", ");
        for (ParameterSymbol requiredParam : this.params().get()) {
            String ballerinaParameterSignature = requiredParam.signature();
            joiner.add(ballerinaParameterSignature);
        }
        this.restParam().ifPresent(ballerinaParameter -> joiner.add(ballerinaParameter.signature()));
        signature.append(joiner.toString()).append(")");
        this.returnTypeDescriptor().ifPresent(typeDescriptor -> signature.append(" returns ")
                .append(typeDescriptor.signature()));
        return signature.toString();
    }

    @Override
    public void accept(SymbolVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SymbolTransformer<T> transformer) {
        return transformer.transform(this);
    }

    private static class AnnotatableReturnType implements Annotatable {

        private List<AnnotationSymbol> annots;
        private List<AnnotationAttachmentSymbol> annotAttachments;

        @Override
        public List<AnnotationSymbol> annotations() {
            return this.annots;
        }

        @Override
        public List<AnnotationAttachmentSymbol> annotAttachments() {
            return this.annotAttachments;
        }

        void setAnnotations(List<AnnotationSymbol> annots) {
            this.annots = annots;
        }

        public void setAnnotAttachments(List<AnnotationAttachmentSymbol> annotAttachments) {
            this.annotAttachments = annotAttachments;
        }
    }
}

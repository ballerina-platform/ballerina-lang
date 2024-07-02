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
import io.ballerina.compiler.api.impl.values.BallerinaConstantValue;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM_MEMBER;
import static org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols.isFlagOn;

/**
 * Represent Constant Symbol.
 *
 * @since 2.0.0
 */
public class BallerinaConstantSymbol extends BallerinaVariableSymbol implements ConstantSymbol {

    private final BallerinaConstantValue constValue;
    private TypeSymbol broaderType;

    private BallerinaConstantSymbol(String name, List<Qualifier> qualifiers, List<AnnotationSymbol> annots,
                                    List<AnnotationAttachmentSymbol> annotAttachments, TypeSymbol typeDescriptor,
                                    TypeSymbol broaderType, BallerinaConstantValue constValue, BSymbol bSymbol,
                                    CompilerContext context) {
        super(name, isFlagOn(bSymbol.flags, Flags.ENUM_MEMBER) ? ENUM_MEMBER : CONSTANT, qualifiers, annots,
              annotAttachments, typeDescriptor, bSymbol, context);
        this.constValue = constValue;
        this.broaderType = broaderType;
    }

    /**
     * Get the constant value.
     *
     * @return {@link Object} value of the constant
     */
    @Override
    public Object constValue() {
        return this.constValue;
    }

    @Override
    public Optional<String> resolvedValue() {
        if (this.constValue == null) {
            return Optional.empty();
        }

        return Optional.of(stringValueOf(this.constValue));
    }

    @Override
    public TypeSymbol broaderTypeDescriptor() {
        return this.broaderType;
    }

    @Override
    public TypeDescKind typeKind() {
        return TypeDescKind.SINGLETON;
    }

    @Override
    public String signature() {
        return this.getInternalSymbol().name.value;
    }

    @Override
    public List<FunctionSymbol> langLibMethods() {
        return this.typeDescriptor().langLibMethods();
    }

    @Override
    public boolean assignableTo(TypeSymbol targetType) {
        return this.typeDescriptor().subtypeOf(targetType);
    }

    @Override
    public boolean subtypeOf(TypeSymbol targetType) {
        return this.typeDescriptor().subtypeOf(targetType);
    }

    @Override
    public void accept(SymbolVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(SymbolTransformer<T> transformer) {
        return transformer.transform(this);
    }

    private String stringValueOf(BallerinaConstantValue value) {
        if (value == null) {
            return null;
        }

        if (value.value() instanceof BallerinaConstantValue ballerinaConstantValue) {
            return stringValueOf(ballerinaConstantValue);
        }

        if (value.value() instanceof HashMap<?, ?> map) {
            StringJoiner joiner = new StringJoiner(", ", "{", "}");

            map.forEach((k, v) -> {
                StringBuilder builder = new StringBuilder();
                builder.append(k).append(": ");

                if (v instanceof BallerinaConstantValue ballerinaConstantValue) {
                    builder.append(stringValueOf(ballerinaConstantValue));
                } else {
                    builder.append(toStringVal(v, value.valueType()));
                }

                joiner.add(builder.toString());
            });

            return joiner.toString();
        }

        return toStringVal(value.value(), value.valueType());
    }

    private String toStringVal(Object obj, TypeSymbol valType) {
        if (obj instanceof String && valType.typeKind() == TypeDescKind.STRING) {
            return String.format("\"%s\"", obj);
        }

        return String.valueOf(obj);
    }

    @Override
    public TypeSymbol originalType() {
        return this.broaderTypeDescriptor();
    }

    /**
     * Represents Ballerina Constant Symbol Builder.
     */
    public static class ConstantSymbolBuilder extends VariableSymbolBuilder {

        private BallerinaConstantValue constantValue;
        private TypeSymbol broaderType;

        public ConstantSymbolBuilder(String name, BSymbol symbol, CompilerContext context) {
            super(name, symbol, context);
        }

        @Override
        public BallerinaConstantSymbol build() {
            return new BallerinaConstantSymbol(this.name, this.qualifiers, this.annots, this.annotAttachments,
                                               this.typeDescriptor, this.broaderType, this.constantValue, this.bSymbol,
                                               this.context);
        }

        public ConstantSymbolBuilder withConstValue(BallerinaConstantValue constValue) {
            this.constantValue = constValue;
            return this;
        }

        @Override
        public ConstantSymbolBuilder withTypeDescriptor(TypeSymbol typeDescriptor) {
            super.withTypeDescriptor(typeDescriptor);
            return this;
        }

        public ConstantSymbolBuilder withBroaderTypeDescriptor(TypeSymbol typeDescriptor) {
            this.broaderType = typeDescriptor;
            return this;
        }
    }
}

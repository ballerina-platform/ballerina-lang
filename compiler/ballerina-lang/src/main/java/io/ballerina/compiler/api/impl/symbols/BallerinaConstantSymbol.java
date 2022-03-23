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
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangConstantValue;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final String constValue;
    private TypeSymbol broaderType;

    private BallerinaConstantSymbol(String name, List<Qualifier> qualifiers, List<AnnotationSymbol> annots,
                                    TypeSymbol typeDescriptor, TypeSymbol broaderType, Object constValue,
                                    BSymbol bSymbol, CompilerContext context) {
        super(name, isFlagOn(bSymbol.flags, Flags.ENUM_MEMBER) ? ENUM_MEMBER : CONSTANT, qualifiers, annots,
              typeDescriptor, bSymbol, context);
        this.constValue = stringValueOf((BLangConstantValue) constValue);
        this.broaderType = broaderType;
    }

    /**
     * Get the constant value.
     *
     * @return {@link Object} value of the constant
     */
    @Override
    public Object constValue() {
        // explicitly returning null here since this will be deprecated and this anyways used to return null always
        // since the corresponding internal API for this wasn't implemented before.
        return null;
    }

    @Override
    public Optional<String> resolvedValue() {
        if (this.constValue == null) {
            return Optional.empty();
        }

        return Optional.of(this.constValue);
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

    private String stringValueOf(BLangConstantValue value) {
        if (value == null) {
            return null;
        }

        if (value.value instanceof BLangConstantValue) {
            return stringValueOf((BLangConstantValue) value.value);
        }

        if (value.value instanceof HashMap) {
            StringJoiner joiner = new StringJoiner(", ", "{", "}");
            Map map = (Map) value.value;

            map.forEach((k, v) -> {
                StringBuilder builder = new StringBuilder();
                builder.append(k).append(": ");

                if (v instanceof BLangConstantValue) {
                    builder.append(stringValueOf((BLangConstantValue) v));
                } else {
                    builder.append(toStringVal(v, value.type));
                }

                joiner.add(builder.toString());
            });

            return joiner.toString();
        }

        return toStringVal(value.value, value.type);
    }

    private String toStringVal(Object obj, BType valType) {
        if (obj instanceof String && TypeTags.isStringTypeTag(valType.tag)) {
            return String.format("\"%s\"", obj);
        }

        return String.valueOf(obj);
    }

    /**
     * Represents Ballerina Constant Symbol Builder.
     */
    public static class ConstantSymbolBuilder extends VariableSymbolBuilder {

        private Object constantValue;
        private TypeSymbol broaderType;

        public ConstantSymbolBuilder(String name, BSymbol symbol, CompilerContext context) {
            super(name, symbol, context);
        }

        public BallerinaConstantSymbol build() {
            return new BallerinaConstantSymbol(this.name, this.qualifiers, this.annots, this.typeDescriptor,
                                               this.broaderType, this.constantValue, this.bSymbol, this.context);
        }

        public ConstantSymbolBuilder withConstValue(Object constValue) {
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

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

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;

import static io.ballerina.compiler.api.symbols.SymbolKind.CONSTANT;
import static io.ballerina.compiler.api.symbols.SymbolKind.ENUM_MEMBER;
import static org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols.isFlagOn;

/**
 * Represent Constant Symbol.
 *
 * @since 2.0.0
 */
public class BallerinaConstantSymbol extends BallerinaVariableSymbol implements ConstantSymbol {

    private final Object constValue;
    private TypeSymbol broaderType;

    private BallerinaConstantSymbol(String name, List<Qualifier> qualifiers, List<AnnotationSymbol> annots,
                                    TypeSymbol typeDescriptor, TypeSymbol broaderType, Object constValue,
                                    BSymbol bSymbol, CompilerContext context) {
        super(name, isFlagOn(bSymbol.flags, Flags.ENUM_MEMBER) ? ENUM_MEMBER : CONSTANT, qualifiers, annots,
              typeDescriptor, bSymbol, context);
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
        return constValue;
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

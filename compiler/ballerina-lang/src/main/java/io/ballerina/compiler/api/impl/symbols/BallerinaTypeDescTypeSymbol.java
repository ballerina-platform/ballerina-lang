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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Optional;

/**
 * Represents a typeDesc type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaTypeDescTypeSymbol extends AbstractTypeSymbol implements TypeDescTypeSymbol {

    private TypeSymbol typeParameter;

    public BallerinaTypeDescTypeSymbol(CompilerContext context, BTypedescType typedescType) {
        super(context, TypeDescKind.TYPEDESC, typedescType);
    }

    @Override
    public Optional<TypeSymbol> typeParameter() {
        if (this.typeParameter == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.typeParameter = typesFactory.getTypeDescriptor(((BTypedescType) this.getBType()).constraint);
        }

        return Optional.ofNullable(this.typeParameter);
    }

    @Override
    public String signature() {
        if (this.typeParameter().isPresent()) {
            return this.typeKind().getName() + "<" + this.typeParameter().get().signature() + ">";
        }
        return this.typeKind().name();
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

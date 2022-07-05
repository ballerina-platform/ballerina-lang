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
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Optional;

/**
 * Represents an future type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaFutureTypeSymbol extends AbstractTypeSymbol implements FutureTypeSymbol {

    private TypeSymbol memberTypeDesc;

    public BallerinaFutureTypeSymbol(CompilerContext context, BFutureType futureType) {
        super(context, TypeDescKind.FUTURE, futureType);
    }

    @Override
    public Optional<TypeSymbol> typeParameter() {
        if (this.memberTypeDesc == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.memberTypeDesc = typesFactory.getTypeDescriptor(((BFutureType) this.getBType()).constraint);
        }

        return Optional.ofNullable(this.memberTypeDesc);
    }

    @Override
    public String signature() {
        String memberSignature;
        if (typeParameter().isPresent()) {
            memberSignature = typeParameter().get().signature();
        } else {
            memberSignature = "()";
        }
        return "future<" + memberSignature + ">";
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

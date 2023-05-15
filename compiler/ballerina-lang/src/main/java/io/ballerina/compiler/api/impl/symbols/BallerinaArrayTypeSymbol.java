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
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an array type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaArrayTypeSymbol extends AbstractTypeSymbol implements ArrayTypeSymbol {
    private Integer size;
    private TypeSymbol memberTypeDesc;
    private String signature;

    public BallerinaArrayTypeSymbol(CompilerContext context, BArrayType arrayType) {
        super(context, TypeDescKind.ARRAY, arrayType);
        if (arrayType.getSize() >= 0) {
            size = arrayType.getSize();
        }
    }

    @Override
    public TypeSymbol memberTypeDescriptor() {
        if (this.memberTypeDesc == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            BType eType = ((BArrayType) this.getBType()).eType;
            if (eType.tsymbol != null && eType.tsymbol.getOrigin() == SymbolOrigin.VIRTUAL) {
                this.memberTypeDesc = typesFactory.getTypeDescriptor(eType, eType.tsymbol, true);
            } else {
                this.memberTypeDesc = typesFactory.getTypeDescriptor(eType);
            }
        }
        return memberTypeDesc;
    }

    @Override
    public String signature() {
        if (this.signature != null) {
            return this.signature;
        }

        StringBuilder sigBuilder = new StringBuilder();
        TypeSymbol memberType = memberTypeDescriptor();

        sigBuilder.append('[')
                .append(((BArrayType) getBType()).state == BArrayState.INFERRED ? "*" :
                        size().map(Objects::toString).orElse(""))
                .append(']');

        while (memberType.typeKind() == TypeDescKind.ARRAY) {
            ArrayTypeSymbol arrType = (ArrayTypeSymbol) memberType;
            BType memberBType = ((AbstractTypeSymbol) memberType).getBType();
            boolean isInferredMemberType =
                    memberBType instanceof BArrayType && ((BArrayType) memberBType).state == BArrayState.INFERRED;

            sigBuilder.append('[')
                    .append(isInferredMemberType ? "*" : arrType.size().map(Objects::toString).orElse(""))
                    .append(']');
            memberType = arrType.memberTypeDescriptor();
        }

        if (memberType.typeKind() == TypeDescKind.UNION || memberType.typeKind() == TypeDescKind.INTERSECTION) {
            this.signature = "(" + memberType.signature() + ")" + sigBuilder;
        } else {
            this.signature = memberType.signature() + sigBuilder;
        }

        return this.signature;
    }

    @Override
    public Optional<Integer> size() {
        return Optional.ofNullable(size);
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

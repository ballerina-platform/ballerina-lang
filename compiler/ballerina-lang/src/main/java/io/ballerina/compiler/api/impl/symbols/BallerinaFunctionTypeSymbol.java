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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.ParameterKind.DEFAULTABLE;
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
    private ParameterSymbol restParam;
    private TypeSymbol returnType;
    private final BInvokableTypeSymbol typeSymbol;

    public BallerinaFunctionTypeSymbol(CompilerContext context, ModuleID moduleID,
                                       BInvokableTypeSymbol invokableSymbol, BType type) {
        super(context, TypeDescKind.FUNCTION, type);
        this.typeSymbol = invokableSymbol;
    }

    @Override
    public List<ParameterSymbol> parameters() {
        if (this.requiredParams == null) {
            SymbolFactory symbolFactory = SymbolFactory.getInstance(this.context);

            this.requiredParams = this.typeSymbol.params.stream()
                    .map(symbol -> {
                        ParameterKind parameterKind = symbol.defaultableParam ? DEFAULTABLE : REQUIRED;
                        return symbolFactory.createBallerinaParameter(symbol, parameterKind);
                    })
                    .collect(Collectors.collectingAndThen(toList(), Collections::unmodifiableList));
        }

        return this.requiredParams;
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
    public String signature() {
        StringBuilder signature = new StringBuilder("function (");
        StringJoiner joiner = new StringJoiner(", ");
        for (ParameterSymbol requiredParam : this.parameters()) {
            String ballerinaParameterSignature = requiredParam.signature();
            joiner.add(ballerinaParameterSignature);
        }
        this.restParam().ifPresent(ballerinaParameter -> joiner.add(ballerinaParameter.signature()));
        signature.append(joiner.toString()).append(")");
        this.returnTypeDescriptor().ifPresent(typeDescriptor -> signature.append(" returns ")
                .append(typeDescriptor.signature()));
        return signature.toString();
    }
}

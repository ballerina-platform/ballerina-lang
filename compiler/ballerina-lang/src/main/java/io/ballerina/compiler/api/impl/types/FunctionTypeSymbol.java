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
package io.ballerina.compiler.api.impl.types;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.impl.TypesFactory;
import io.ballerina.compiler.api.types.TypeSymbol;
import io.ballerina.compiler.api.types.Parameter;
import io.ballerina.compiler.api.types.ParameterKind;
import io.ballerina.compiler.api.types.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.types.ParameterKind.DEFAULTABLE;
import static io.ballerina.compiler.api.types.ParameterKind.REQUIRED;
import static io.ballerina.compiler.api.types.ParameterKind.REST;
import static java.util.stream.Collectors.toList;

/**
 * Represents a function type descriptor.
 *
 * @since 2.0.0
 */
public class FunctionTypeSymbol extends AbstractTypeSymbol
        implements io.ballerina.compiler.api.types.FunctionTypeSymbol {

    private List<Parameter> requiredParams;
    private Parameter restParam;
    private TypeSymbol returnType;
    private final BInvokableTypeSymbol typeSymbol;

    public FunctionTypeSymbol(ModuleID moduleID, BInvokableTypeSymbol invokableSymbol) {
        super(TypeDescKind.FUNCTION, moduleID, invokableSymbol.type);
        this.typeSymbol = invokableSymbol;
    }

    @Override
    public List<Parameter> parameters() {
        if (this.requiredParams == null) {
            this.requiredParams = this.typeSymbol.params.stream()
                    .map(symbol -> {
                        ParameterKind parameterKind = symbol.defaultableParam ? DEFAULTABLE : REQUIRED;
                        return SymbolFactory.createBallerinaParameter(symbol, parameterKind);
                    })
                    .collect(Collectors.collectingAndThen(toList(), Collections::unmodifiableList));
        }
        return this.requiredParams;
    }

    @Override
    public Optional<Parameter> restParam() {
        if (restParam == null) {
            this.restParam = SymbolFactory.createBallerinaParameter(typeSymbol.restParam, REST);
        }
        return Optional.ofNullable(this.restParam);
    }

    @Override
    public Optional<TypeSymbol> returnTypeDescriptor() {
        if (returnType == null) {
            this.returnType = TypesFactory.getTypeDescriptor(this.typeSymbol.returnType);
        }
        return Optional.ofNullable(this.returnType);
    }

    @Override
    public String signature() {
        StringBuilder signature = new StringBuilder("function (");
        StringJoiner joiner = new StringJoiner(",");
        for (Parameter requiredParam : this.parameters()) {
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

/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerina.compiler.impl.types;

import org.ballerina.compiler.api.ModuleID;
import org.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerina.compiler.api.types.FunctionTypeDescriptor;
import org.ballerina.compiler.api.types.Parameter;
import org.ballerina.compiler.api.types.TypeDescKind;
import org.ballerina.compiler.impl.TypesFactory;
import org.ballerina.compiler.impl.symbols.SymbolFactory;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Represents a function type descriptor.
 *
 * @since 1.3.0
 */
public class BallerinaFunctionTypeDescriptor extends AbstractTypeDescriptor implements FunctionTypeDescriptor {

    private List<Parameter> requiredParams;
    private Parameter restParam;
    private BallerinaTypeDescriptor returnType;
    private final BInvokableTypeSymbol typeSymbol;
    // TODO: Represent the return type's annotations

    public BallerinaFunctionTypeDescriptor(ModuleID moduleID, BInvokableTypeSymbol invokableSymbol) {
        super(TypeDescKind.FUNCTION, moduleID, invokableSymbol.type);
        this.typeSymbol = invokableSymbol;
    }

    @Override
    public List<Parameter> requiredParams() {
        if (this.requiredParams == null) {
            this.requiredParams = this.typeSymbol.params.stream()
                    .map(SymbolFactory::createBallerinaParameter)
                    .collect(Collectors.collectingAndThen(toList(), Collections::unmodifiableList));
        }
        return this.requiredParams;
    }

    @Override
    public Optional<Parameter> restParam() {
        if (restParam == null) {
            this.restParam = SymbolFactory.createBallerinaParameter(typeSymbol.restParam);
        }
        return Optional.ofNullable(this.restParam);
    }

    @Override
    public Optional<BallerinaTypeDescriptor> getReturnType() {
        if (returnType == null) {
            this.returnType = TypesFactory.getTypeDescriptor(((BInvokableType) this.getBType()).retType);
        }
        return Optional.ofNullable(this.returnType);
    }

    @Override
    public String signature() {
        StringBuilder signature = new StringBuilder("function (");
        StringJoiner joiner = new StringJoiner(",");
        for (Parameter requiredParam : this.requiredParams()) {
            String ballerinaParameterSignature = requiredParam.signature();
            joiner.add(ballerinaParameterSignature);
        }
        this.restParam().ifPresent(ballerinaParameter -> joiner.add(ballerinaParameter.signature()));
        signature.append(joiner.toString()).append(")");
        this.getReturnType().ifPresent(typeDescriptor -> signature.append(" returns ")
                .append(typeDescriptor.signature()));
        return signature.toString();
    }
}

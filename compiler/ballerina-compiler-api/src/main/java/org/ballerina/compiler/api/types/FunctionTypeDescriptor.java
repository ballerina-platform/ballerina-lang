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
package org.ballerina.compiler.api.types;

import org.ballerina.compiler.api.model.ModuleID;
import org.ballerina.compiler.api.semantic.TypesFactory;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents a function type descriptor.
 *
 * @since 1.3.0
 */
public class FunctionTypeDescriptor extends BallerinaTypeDesc {
    private BInvokableType invokableType;
    private List<TypeDescriptor> requiredParams;
    private TypeDescriptor restParam;
    private TypeDescriptor returnType;
    // TODO: Represent the return type's annotations

    public FunctionTypeDescriptor(ModuleID moduleID,
                                  BInvokableType invokableType) {
        super(TypeDescKind.FUNCTION, moduleID);
        this.invokableType = invokableType;
    }

    public List<TypeDescriptor> getRequiredParams() {
        if (this.requiredParams == null) {
            this.requiredParams = new ArrayList<>();
            for (BType requiredParam : this.invokableType.getParameterTypes()) {
                this.requiredParams.add(TypesFactory.getTypeDescriptor(requiredParam));
            }
        }
        return this.requiredParams;
    }

    public Optional<TypeDescriptor> getRestParam() {
        if (restParam == null) {
            this.restParam = TypesFactory.getTypeDescriptor(this.invokableType.restType);
        }
        return Optional.ofNullable(this.restParam);
    }

    public Optional<TypeDescriptor> getReturnType() {
        if (returnType == null) {
            this.returnType = TypesFactory.getTypeDescriptor(this.invokableType.retType);
        }
        return Optional.ofNullable(this.returnType);
    }

    @Override
    public String getSignature() {
        StringBuilder signature = new StringBuilder("function (");
        StringJoiner joiner = new StringJoiner(",");
        for (TypeDescriptor requiredParam : this.getRequiredParams()) {
            String ballerinaParameterSignature = requiredParam.getSignature();
            joiner.add(ballerinaParameterSignature);
        }
        this.getRestParam().ifPresent(ballerinaParameter -> joiner.add(ballerinaParameter.getSignature()));
        signature.append(joiner.toString())
                .append(")");
        this.getReturnType().ifPresent(typeDescriptor -> signature.append(" returns ")
                .append(typeDescriptor.getSignature()));

        return signature.toString();
    }
}

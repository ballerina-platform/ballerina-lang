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

import org.ballerina.compiler.api.model.BallerinaParameter;
import org.ballerina.compiler.api.model.ModuleID;
import org.ballerinalang.model.types.TypeKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Represents a function type descriptor.
 * 
 * @since 1.3.0
 */
public class FunctionTypeDescriptor extends BallerinaTypeDesc {
    private List<BallerinaParameter> requiredParams;
    private BallerinaParameter restParam;
    private TypeDescriptor returnType;
    // TODO: Represent the return type's annotations
    
    public FunctionTypeDescriptor(TypeDescKind typeDescKind,
                                  ModuleID moduleID,
                                  List<BallerinaParameter> requiredParams,
                                  BallerinaParameter restParam,
                                  TypeDescriptor returnType) {
        super(typeDescKind, moduleID, TypeKind.FUNCTION);
        this.requiredParams = requiredParams;
        this.restParam = restParam;
        this.returnType = returnType;
    }

    public List<BallerinaParameter> getRequiredParams() {
        return requiredParams;
    }

    public Optional<BallerinaParameter> getRestParam() {
        return Optional.ofNullable(restParam);
    }

    public Optional<TypeDescriptor> getReturnType() {
        return Optional.ofNullable(returnType);
    }

    @Override
    public String getSignature() {
        StringBuilder signature = new StringBuilder("function (");
        StringJoiner joiner = new StringJoiner(",");
        for (BallerinaParameter requiredParam : this.requiredParams) {
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

    /**
     * Represents Tuple Type Descriptor.
     */
    public static class FunctionTypeBuilder extends TypeBuilder<FunctionTypeBuilder> {
        private List<BallerinaParameter> requiredParams = new ArrayList<>();
        private BallerinaParameter restParam;
        private TypeDescriptor returnType;

        /**
         * Symbol Builder Constructor.
         *
         * @param typeDescKind type descriptor kind
         * @param moduleID     Module ID of the type descriptor
         */
        public FunctionTypeBuilder(TypeDescKind typeDescKind, ModuleID moduleID) {
            super(typeDescKind, moduleID);
        }

        /**
         * Build the Ballerina Type Descriptor.
         *
         * @return {@link TypeDescriptor} built
         */
        public TypeDescriptor build() {
            return new FunctionTypeDescriptor(this.typeDescKind,
                    this.moduleID,
                    this.requiredParams,
                    this.restParam,
                    this.returnType);
        }

        public FunctionTypeBuilder withRequiredParam(BallerinaParameter requiredParam) {
            this.requiredParams.add(requiredParam);
            return this;
        }

        public FunctionTypeBuilder withRestParam(BallerinaParameter restParam) {
            this.restParam = restParam;
            return this;
        }

        public FunctionTypeBuilder withReturnType(TypeDescriptor returnType) {
            this.returnType = returnType;
            return this;
        }
    }
}

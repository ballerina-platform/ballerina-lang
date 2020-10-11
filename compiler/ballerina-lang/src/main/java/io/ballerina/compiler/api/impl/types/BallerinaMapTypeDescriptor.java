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
import io.ballerina.compiler.api.impl.TypesFactory;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.MapTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;

import java.util.Optional;

/**
 * Represents an array type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaMapTypeDescriptor extends AbstractTypeDescriptor implements MapTypeDescriptor {

    private BallerinaTypeDescriptor memberTypeDesc;

    public BallerinaMapTypeDescriptor(ModuleID moduleID, BMapType mapType) {
        super(TypeDescKind.MAP, moduleID, mapType);
    }

    @Override
    public Optional<BallerinaTypeDescriptor> typeParameter() {
        if (this.memberTypeDesc == null) {
            this.memberTypeDesc = TypesFactory.getTypeDescriptor(((BMapType) this.getBType()).constraint);
        }
        return Optional.ofNullable(this.memberTypeDesc);
    }

    @Override
    public String signature() {
        Optional<BallerinaTypeDescriptor> memberTypeDescriptor = this.typeParameter();
        return memberTypeDescriptor.map(typeDescriptor -> "map<" + typeDescriptor.signature() + ">").orElse("map<>");
    }
}

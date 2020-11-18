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
import io.ballerina.compiler.api.types.FutureTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;

import java.util.Optional;

/**
 * Represents an future type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaFutureTypeDescriptor extends AbstractTypeDescriptor implements FutureTypeDescriptor {

    private BallerinaTypeDescriptor memberTypeDesc;

    public BallerinaFutureTypeDescriptor(ModuleID moduleID, BFutureType futureType) {
        super(TypeDescKind.FUTURE, moduleID, futureType);
    }

    @Override
    public Optional<BallerinaTypeDescriptor> typeParameter() {
        if (this.memberTypeDesc == null) {
            this.memberTypeDesc = TypesFactory.getTypeDescriptor(((BFutureType) this.getBType()).constraint);
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
}

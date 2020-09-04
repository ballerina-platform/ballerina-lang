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
import org.ballerina.compiler.api.types.TupleTypeDescriptor;
import org.ballerina.compiler.api.types.TypeDescKind;
import org.ballerina.compiler.impl.TypesFactory;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents a tuple type descriptor.
 *
 * @since 1.3.0
 */
public class BallerinaTupleTypeDescriptor extends AbstractTypeDescriptor implements TupleTypeDescriptor {

    private List<BallerinaTypeDescriptor> memberTypes;
    private BallerinaTypeDescriptor restTypeDesc;

    public BallerinaTupleTypeDescriptor(ModuleID moduleID, BTupleType tupleType) {
        super(TypeDescKind.TUPLE, moduleID, tupleType);
    }

    @Override
    public List<BallerinaTypeDescriptor> memberTypes() {
        if (this.memberTypes == null) {
            this.memberTypes = new ArrayList<>();
            for (BType type : ((BTupleType) this.getBType()).tupleTypes) {
                this.memberTypes.add(TypesFactory.getTypeDescriptor(type));
            }
        }

        return this.memberTypes;
    }

    @Override
    public Optional<BallerinaTypeDescriptor> restType() {
        if (this.restTypeDesc == null) {
            this.restTypeDesc = TypesFactory.getTypeDescriptor(((BTupleType) this.getBType()).restType);
        }

        return Optional.ofNullable(this.restTypeDesc);
    }

    @Override
    public String signature() {
        StringJoiner joiner = new StringJoiner(",");
        for (BallerinaTypeDescriptor typeDescriptorImpl : memberTypes()) {
            String typeDescriptorSignature = typeDescriptorImpl.signature();
            joiner.add(typeDescriptorSignature);
        }
        if (restType().isPresent()) {
            joiner.add("..." + restType().get().signature());
        }

        return "[" + joiner.toString() + "]";
    }
}

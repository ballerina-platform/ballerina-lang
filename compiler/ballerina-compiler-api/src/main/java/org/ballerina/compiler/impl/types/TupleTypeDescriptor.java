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

import org.ballerina.compiler.api.element.ModuleID;
import org.ballerina.compiler.api.type.BallerinaTypeDescriptor;
import org.ballerina.compiler.api.type.TypeDescKind;
import org.ballerina.compiler.impl.semantic.BallerinaTypeDesc;
import org.ballerina.compiler.impl.semantic.TypesFactory;
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
public class TupleTypeDescriptor extends BallerinaTypeDesc {

    private List<BallerinaTypeDescriptor> memberTypes;

    private BallerinaTypeDescriptor restTypeDesc;

    public TupleTypeDescriptor(ModuleID moduleID,
                               BTupleType tupleType) {
        super(TypeDescKind.TUPLE, moduleID, tupleType);
    }

    public List<BallerinaTypeDescriptor> getMemberTypes() {
        if (this.memberTypes == null) {
            this.memberTypes = new ArrayList<>();
            for (BType type : ((BTupleType) this.getBType()).tupleTypes) {
                this.memberTypes.add(TypesFactory.getTypeDescriptor(type));
            }
        }

        return this.memberTypes;
    }

    public Optional<BallerinaTypeDescriptor> getRestType() {
        if (this.restTypeDesc == null) {
            this.restTypeDesc = TypesFactory.getTypeDescriptor(((BTupleType) this.getBType()).restType);
        }

        return Optional.ofNullable(this.restTypeDesc);
    }

    @Override
    public String signature() {
        StringJoiner joiner = new StringJoiner(",");
        for (BallerinaTypeDescriptor typeDescriptorImpl : this.getMemberTypes()) {
            String typeDescriptorSignature = typeDescriptorImpl.signature();
            joiner.add(typeDescriptorSignature);
        }
        if (this.getRestType().isPresent()) {
            joiner.add("..." + this.getRestType().get().signature());
        }

        return "[" + joiner.toString() + "]";
    }
}

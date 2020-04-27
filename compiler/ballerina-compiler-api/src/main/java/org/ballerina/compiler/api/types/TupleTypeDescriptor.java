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
import org.ballerinalang.model.elements.PackageID;

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
    private List<TypeDescriptor> memberTypes;
    private TypeDescriptor restTypeDesc;
    
    private TupleTypeDescriptor(TypeDescKind typeDescKind,
                               ModuleID moduleID,
                                List<TypeDescriptor> memberTypes,
                                TypeDescriptor restTypeDesc) {
        super(typeDescKind, moduleID);
        this.memberTypes = memberTypes;
        this.restTypeDesc = restTypeDesc;
    }
    
    public List<TypeDescriptor> getMemberTypes() {
        return this.memberTypes;
    }
    
    public Optional<TypeDescriptor> getRestType() {
        return Optional.ofNullable(this.restTypeDesc);
    }

    @Override
    public String getSignature() {
        StringJoiner joiner = new StringJoiner(",");
        for (TypeDescriptor typeDescriptor : this.getMemberTypes()) {
            String typeDescriptorSignature = typeDescriptor.getSignature();
            joiner.add(typeDescriptorSignature);
        }
        if (this.getRestType().isPresent()) {
            joiner.add("..." + this.getRestType().get().getSignature());
        }

        return "[" + joiner.toString() + "]";
    }

    /**
     * Represents Tuple Type Descriptor.
     */
    public static class TupleTypeBuilder extends TypeBuilder<TupleTypeBuilder> {
        private List<TypeDescriptor> memberTypes = new ArrayList<>();
        private TypeDescriptor restTypeDesc = null;

        /**
         * Symbol Builder Constructor.
         *
         * @param typeDescKind type descriptor kind
         * @param moduleID     Module ID of the type descriptor
         */
        public TupleTypeBuilder(TypeDescKind typeDescKind, PackageID moduleID) {
            super(typeDescKind, moduleID);
        }

        /**
         * Build the Ballerina Type Descriptor.
         *
         * @return {@link TypeDescriptor} built
         */
        public TypeDescriptor build() {
            return new TupleTypeDescriptor(this.typeDescKind,
                    this.moduleID,
                    this.memberTypes,
                    this.restTypeDesc);
        }
        
        public TupleTypeBuilder withMemberType(TypeDescriptor memberType) {
            this.memberTypes.add(memberType);
            return this;
        }
        
        public TupleTypeBuilder withRestType(TypeDescriptor restTypeDesc) {
            this.restTypeDesc = restTypeDesc;
            return this;
        }
    }
}

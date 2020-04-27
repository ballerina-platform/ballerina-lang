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

import java.util.Optional;

/**
 * Represents an future type descriptor.
 * 
 * @since 1.3.0
 */
public class FutureTypeDescriptor extends BallerinaTypeDesc {
    private TypeDescriptor memberTypeDesc;
    
    private FutureTypeDescriptor(TypeDescKind typeDescKind,
                                 ModuleID moduleID,
                                 TypeDescriptor memberTypeDesc) {
        super(typeDescKind, moduleID);
        this.memberTypeDesc = memberTypeDesc;
    }
    
    public Optional<TypeDescriptor> getMemberTypeDescriptor() {
        return Optional.ofNullable(this.memberTypeDesc);
    }

    @Override
    public String getSignature() {
        String memberSignature;
        if (this.getMemberTypeDescriptor().isPresent()) {
            memberSignature = this.getMemberTypeDescriptor().get().getSignature();
        } else {
            memberSignature = "()";
        }
        return "future<" + memberSignature + ">";
    }

    /**
     * Represents Future Type Descriptor Builder.
     */
    public static class FutureTypeBuilder extends TypeBuilder<FutureTypeBuilder> {
        private TypeDescriptor memberType;

        /**
         * Symbol Builder Constructor.
         *
         * @param typeDescKind type descriptor kind
         * @param moduleID     Module ID of the type descriptor
         */
        public FutureTypeBuilder(TypeDescKind typeDescKind, PackageID moduleID) {
            super(typeDescKind, moduleID);
        }

        /**
         * Build the Ballerina Type Descriptor.
         *
         * @return {@link TypeDescriptor} built
         */
        public TypeDescriptor build() {
            return new FutureTypeDescriptor(this.typeDescKind,
                    this.moduleID,
                    this.memberType);
        }

        public FutureTypeBuilder withMemberType(TypeDescriptor memberType) {
            this.memberType = memberType;
            return this;
        }
    }
}

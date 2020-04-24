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
import org.ballerinalang.model.types.TypeKind;

/**
 * Represents an array type descriptor.
 * 
 * @since 1.3.0
 */
public class ArrayTypeDescriptor extends BallerinaTypeDesc {
    private TypeDescriptor memberTypeDesc;
    
    public ArrayTypeDescriptor(TypeDescKind typeDescKind,
                               ModuleID moduleID,
                               TypeKind typeKind,
                               TypeDescriptor memberTypeDesc) {
        super(typeDescKind, moduleID, typeKind);
        this.memberTypeDesc = memberTypeDesc;
    }
    
    public TypeDescriptor getMemberTypeDescriptor() {
        return this.memberTypeDesc;
    }

    @Override
    public String getSignature() {
        return memberTypeDesc.getSignature() + "[]";
    }

    /**
     * Represents Tuple Type Descriptor.
     */
    public static class ArrayTypeBuilder extends TypeBuilder<ArrayTypeBuilder> {
        private TypeDescriptor memberType;

        /**
         * Symbol Builder Constructor.
         *
         * @param typeDescKind type descriptor kind
         * @param moduleID     Module ID of the type descriptor
         * @param typeKind     kind of the type descriptor
         */
        public ArrayTypeBuilder(TypeDescKind typeDescKind, PackageID moduleID, TypeKind typeKind) {
            super(typeDescKind, moduleID, typeKind);
        }

        /**
         * Build the Ballerina Type Descriptor.
         *
         * @return {@link TypeDescriptor} built
         */
        public TypeDescriptor build() {
            if (this.memberType == null) {
                throw new AssertionError("Member Type Cannot be null");
            }
            return new ArrayTypeDescriptor(this.typeDescKind,
                    this.moduleID,
                    this.typeKind,
                    this.memberType);
        }

        public ArrayTypeBuilder withMemberType(TypeDescriptor memberType) {
            this.memberType = memberType;
            return this;
        }
    }
}

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

import java.util.Optional;

/**
 * Represents a typeDesc type descriptor.
 * 
 * @since 1.3.0
 */
public class TypeDescTypeDescriptor extends BallerinaTypeDesc {
    private TypeDescriptor typeParameter;
    
    private TypeDescTypeDescriptor(TypeDescKind typeDescKind,
                                   ModuleID moduleID,
                                   TypeDescriptor typeParameter) {
        super(typeDescKind, moduleID, TypeKind.TYPEDESC);
        this.typeParameter = typeParameter;
    }
    
    public Optional<TypeDescriptor> getTypeParameter() {
        return Optional.ofNullable(this.typeParameter);
    }

    @Override
    public String getSignature() {
        if (this.getTypeParameter().isPresent()) {
            return this.getKind().name() + "<" + this.getTypeParameter().get().getSignature() + ">";
        }
        return this.getKind().name();
    }

    /**
     * Represents a builder for TypeDesc Type Descriptor.
     */
    public static class TypeDescTypeBuilder extends TypeBuilder<TypeDescTypeBuilder> {
        private TypeDescriptor typeParameter;

        /**
         * Symbol Builder Constructor.
         *
         * @param typeDescKind type descriptor kind
         * @param moduleID     Module ID of the type descriptor
         */
        public TypeDescTypeBuilder(TypeDescKind typeDescKind, PackageID moduleID) {
            super(typeDescKind, moduleID, TypeKind.TYPEDESC);
        }

        /**
         * Build the Ballerina Type Descriptor.
         *
         * @return {@link TypeDescriptor} built
         */
        public TypeDescriptor build() {
            return new TypeDescTypeDescriptor(this.typeDescKind,
                    this.moduleID,
                    this.typeParameter);
        }

        public TypeDescTypeBuilder withTypeParameter(TypeDescriptor typeParameter) {
            this.typeParameter = typeParameter;
            return this;
        }
    }
}

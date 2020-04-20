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

import org.ballerina.compiler.api.model.BallerinaTypeDefinition;
import org.ballerina.compiler.api.model.ModuleID;
import org.ballerinalang.model.types.TypeKind;

/**
 * Represents a TypeReference type descriptor.
 * 
 * @since 1.3.0
 */
public class TypeReferenceTypeDescriptor extends BallerinaTypeDesc {
    private BallerinaTypeDefinition typeDefinition;
    
    private TypeReferenceTypeDescriptor(TypeDescKind typeDescKind,
                                        ModuleID moduleID,
                                        BallerinaTypeDefinition typeDefinition) {
        super(typeDescKind, moduleID, TypeKind.NONE);
        this.typeDefinition = typeDefinition;
    }

    @Override
    public String getSignature() {
        return this.typeDefinition.getModuleID().getModulePrefix() + ":" + this.typeDefinition.getName();
    }

    /**
     * Represents a builder for Type Reference
     */
    public static class TypeReferenceBuilder extends TypeBuilder<TypeReferenceBuilder> {
        private BallerinaTypeDefinition typeDefinition;

        /**
         * Symbol Builder Constructor.
         *
         * @param typeDescKind type descriptor kind
         * @param moduleID     Module ID of the type descriptor
         * @param typeKind     kind of the type descriptor
         */
        public TypeReferenceBuilder(TypeDescKind typeDescKind, ModuleID moduleID, TypeKind typeKind) {
            super(typeDescKind, moduleID, typeKind);
        }

        /**
         * Build the Ballerina Type Descriptor.
         *
         * @return {@link TypeDescriptor} built
         */
        public TypeDescriptor build() {
            if (this.typeDefinition == null) {
                throw new AssertionError("Type Definition cannot be null");
            }
            return new TypeReferenceTypeDescriptor(this.typeDescKind,
                    this.moduleID,
                    this.typeDefinition);
        }

        public TypeReferenceBuilder withTypeDefinition(BallerinaTypeDefinition typeDefinition) {
            this.typeDefinition = typeDefinition;
            return this;
        }
    }
}

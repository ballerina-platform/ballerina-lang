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
 * Represents a Ballerina Type Descriptor.
 * 
 * @since 1.3.0
 */
public abstract class BallerinaTypeDesc implements TypeDescriptor {
    private TypeDescKind typeDescKind;
    private ModuleID moduleID;
    protected TypeKind typeKind;

    public BallerinaTypeDesc(TypeDescKind typeDescKind, ModuleID moduleID, TypeKind typeKind) {
        this.typeDescKind = typeDescKind;
        this.moduleID = moduleID;
        this.typeKind = typeKind;
    }

    public TypeDescKind getKind() {
        return typeDescKind;
    }

    public ModuleID getModuleID() {
        return moduleID;
    }
    
    public abstract String getSignature();

    /**
     * Represents Ballerina Symbol Builder.
     * @param <T> Symbol Type
     */
    protected abstract static class TypeBuilder<T extends TypeBuilder<T>> {
        protected TypeDescKind typeDescKind;
        protected ModuleID moduleID;
        protected TypeKind typeKind;
        
        /**
         * Symbol Builder Constructor.
         * 
         * @param typeDescKind type descriptor kind
         * @param moduleID Module ID of the type descriptor
         * @param typeKind kind of the type descriptor
         */
        public TypeBuilder(TypeDescKind typeDescKind, PackageID moduleID, TypeKind typeKind) {
            this.typeDescKind = typeDescKind;
            this.moduleID = new ModuleID(moduleID);
            this.typeKind = typeKind;
        }
        
        /**
         * Symbol Builder Constructor.
         *
         * @param typeDescKind type descriptor kind
         * @param moduleID Module ID of the type descriptor
         */
        public TypeBuilder(TypeDescKind typeDescKind, PackageID moduleID) {
            this.typeDescKind = typeDescKind;
            this.moduleID = new ModuleID(moduleID);
        }

        /**
         * Build the Ballerina Type Descriptor.
         *
         * @return {@link TypeDescriptor} built
         */
        public abstract TypeDescriptor build();
    }
}

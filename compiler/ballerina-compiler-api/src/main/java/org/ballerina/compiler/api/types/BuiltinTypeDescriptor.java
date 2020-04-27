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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

/**
 * Represents the builtin type descriptor.
 * 
 * @since 1.3.0
 */
public class BuiltinTypeDescriptor extends BallerinaTypeDesc {
    
    private String typeName;
    
    private BuiltinTypeDescriptor(TypeDescKind typeDescKind,
                                  ModuleID moduleID,
                                  String name) {
        super(typeDescKind, moduleID);
        this.typeName = name;
    }

    @Override
    public String getSignature() {
        // For the builtin types, return the type kind as the type name
        return this.typeName;
    }

    /**
     * Represents the builder for Builtin Type Descriptor.
     */
    public static class BuiltinTypeBuilder extends TypeBuilder<BuiltinTypeBuilder> {
        
        private BTypeSymbol typeSymbol;

        /**
         * Symbol Builder Constructor.
         *
         * @param typeDescKind type descriptor kind
         * @param moduleID     Module ID of the type descriptor
         * @param typeSymbol type symbol
         */
        public BuiltinTypeBuilder(TypeDescKind typeDescKind, PackageID moduleID, BTypeSymbol typeSymbol) {
            super(typeDescKind, moduleID);
            this.typeSymbol = typeSymbol;
        }

        /**
         * Build the Ballerina Type Descriptor.
         *
         * @return {@link TypeDescriptor} built
         */
        public BuiltinTypeDescriptor build() {
            return new BuiltinTypeDescriptor(this.typeDescKind,
                    this.moduleID,
                    this.typeSymbol.getName().getValue());
        }
    }
}

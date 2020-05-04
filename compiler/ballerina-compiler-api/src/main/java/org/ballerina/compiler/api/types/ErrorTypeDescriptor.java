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
import org.ballerina.compiler.api.semantic.TypesFactory;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;

/**
 * Represents an error type descriptor.
 * 
 * @since 1.3.0
 */
public class ErrorTypeDescriptor extends BallerinaTypeDesc {
    
    TypeDescriptor reason;
    TypeDescriptor detail;

    public ErrorTypeDescriptor(ModuleID moduleID,
                               TypeDescriptor reason,
                               TypeDescriptor detail) {
        super(TypeDescKind.ERROR, moduleID);
        this.reason = reason;
        this.detail = detail;
    }

    /**
     * Get the reason type descriptor.
     * 
     * @return {@link TypeDescriptor} reason
     */
    public TypeDescriptor getReason() {
        return reason;
    }

    /**
     * Get the detail type descriptor.
     *
     * @return {@link TypeDescriptor} detail
     */
    public TypeDescriptor getDetail() {
        return detail;
    }

    @Override
    public String getSignature() {
        return null;
    }

    /**
     * Represents the builder for error Type Descriptor.
     */
    public static class ErrorTypeBuilder extends TypeBuilder<ErrorTypeBuilder> {

        private BErrorTypeSymbol typeSymbol;
        
        public ErrorTypeBuilder(PackageID moduleID, BErrorTypeSymbol typeSymbol) {
            super(TypeDescKind.ERROR, moduleID);
            this.typeSymbol = typeSymbol;
        }

        /**
         * {@inheritDoc}
         */
        public ErrorTypeDescriptor build() {
            TypeDescriptor reason = TypesFactory.getTypeDescriptor(((BErrorType) typeSymbol.getType()).getReasonType());
            TypeDescriptor detail = TypesFactory.getTypeDescriptor(((BErrorType) typeSymbol.getType()).getDetailType());
            return new ErrorTypeDescriptor(this.moduleID, reason, detail);
        }
    }
}

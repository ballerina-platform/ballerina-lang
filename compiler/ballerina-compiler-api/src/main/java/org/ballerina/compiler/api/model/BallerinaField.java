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
package org.ballerina.compiler.api.model;

import org.ballerina.compiler.api.types.TypeDescriptor;

/**
 * Represents a field with a name and type.
 * 
 * @since 1.3.0
 */
public class BallerinaField {
    // add the metadata field
    private String fieldName;
    private TypeDescriptor typeDescriptor;
    private boolean isOptional;

    public BallerinaField(String fieldName, TypeDescriptor typeDescriptor, boolean isOptional) {
        this.fieldName = fieldName;
        this.typeDescriptor = typeDescriptor;
        this.isOptional = isOptional;
    }

    /**
     * Get the field name.
     * 
     * @return {@link String} name of the field
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Whether optional field or not.
     * 
     * @return {@link Boolean} optional status
     */
    public boolean isOptional() {
        return isOptional;
    }

    /**
     * Get the type descriptor of the field.
     * 
     * @return {@link TypeDescriptor} of the field
     */
    public TypeDescriptor getTypeDescriptor() {
        return typeDescriptor;
    }

    /**
     * Get the signature of the field.
     * 
     * @return {}
     */
    public String getSignature() {
        StringBuilder signature = new StringBuilder(this.typeDescriptor.getSignature() + " " + this.fieldName);
        if (this.isOptional) {
            signature.append("?");
        }
        
        return signature.toString();
    }
}

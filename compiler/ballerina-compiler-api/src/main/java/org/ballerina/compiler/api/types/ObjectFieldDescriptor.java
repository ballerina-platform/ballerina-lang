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

import org.ballerina.compiler.api.model.AccessModifier;

import java.util.Optional;

/**
 * Represents an object field descriptor.
 * 
 * @since 1.3.0
 */
public class ObjectFieldDescriptor {
    // todo: Represent the meta data
    private AccessModifier visibilityQualifier;
    private TypeDescriptor typeDescriptor;
    private String fieldName;

    public ObjectFieldDescriptor(AccessModifier visibilityQualifier,
                                 TypeDescriptor typeDescriptor,
                                 String fieldName) {
        this.visibilityQualifier = visibilityQualifier;
        this.typeDescriptor = typeDescriptor;
        this.fieldName = fieldName;
    }

    /**
     * Get the visibility qualifier.
     * 
     * @return {@link Optional} visibility qualifier
     */
    public Optional<AccessModifier> getVisibilityQualifier() {
        return Optional.ofNullable(this.visibilityQualifier);
    }

    /**
     * Get the type descriptor.
     * 
     * @return {@link TypeDescriptor} of the field
     */
    public TypeDescriptor getTypeDescriptor() {
        return typeDescriptor;
    }

    /**
     * Get the name of the field.
     * 
     * @return {@link String} name of the field
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Get the signature of the field.
     * 
     * @return {@link String} representation of the field
     */
    public String getSignature() {
        // TODO: implementation required
        return null;
    }
}

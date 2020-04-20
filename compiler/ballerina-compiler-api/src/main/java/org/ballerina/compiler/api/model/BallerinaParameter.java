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

import java.util.List;
import java.util.Optional;

/**
 * Represents a parameter with a name and type.
 * 
 * @since 1.3.0
 */
public class BallerinaParameter {
    // add the metadata field
    private List<AccessModifier> accessModifiers;
    private String parameterName;
    private TypeDescriptor typeDescriptor;

    public BallerinaParameter(String parameterName, TypeDescriptor typeDescriptor,
                              List<AccessModifier> accessModifiers) {
        // TODO: Add the meta
        this.parameterName = parameterName;
        this.typeDescriptor = typeDescriptor;
        this.accessModifiers = accessModifiers;
    }

    /**
     * Get the parameter name.
     * 
     * @return {@link Optional} name of the field
     */
    public Optional<String> getParameterName() {
        return Optional.ofNullable(parameterName);
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
     * Get the access modifiers.
     * 
     * @return {@link List} of access modifiers
     */
    public List<AccessModifier> getAccessModifiers() {
        return accessModifiers;
    }

    /**
     * Get the signature of the field.
     * 
     * @return {}
     */
    public String getSignature() {
        // TODO: Implementation required
        StringBuilder signature = new StringBuilder();
        
        return signature.toString();
    }
}

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

import org.ballerina.compiler.api.semantic.TypesFactory;
import org.ballerina.compiler.api.types.TypeDescriptor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.util.Flags;

import java.util.Optional;

/**
 * Represents a field with a name and type.
 * 
 * @since 1.3.0
 */
public class BallerinaField {
    private DocAttachment docAttachment;
    private BField bField;
    private TypeDescriptor typeDescriptor;

    public BallerinaField(BField bField) {
        this.bField = bField;
        this.typeDescriptor = TypesFactory.getTypeDescriptor(bField.getType());
        this.docAttachment = new DocAttachment(bField.symbol.markdownDocumentation);
    }

    /**
     * Get the field name.
     * 
     * @return {@link String} name of the field
     */
    public String getFieldName() {
        return this.bField.getName().getValue();
    }

    /**
     * Whether optional field or not.
     * 
     * @return {@link Boolean} optional status
     */
    public boolean isOptional() {
        return (this.bField.type.flags & Flags.OPTIONAL) == Flags.OPTIONAL;
    }
    
    /**
     * Whether required field or not.
     * 
     * @return {@link Boolean} required status
     */
    public boolean isRequired() {
        return (this.bField.type.flags & Flags.REQUIRED) == Flags.REQUIRED;
    }

    /**
     * Get the type descriptor of the field.
     * 
     * @return {@link TypeDescriptor} of the field
     */
    public TypeDescriptor getTypeDescriptor() {
        return TypesFactory.getTypeDescriptor(this.bField.getType());
    }

    /**
     * Get the documentation attachment.
     * 
     * @return {@link DocAttachment} of the field
     */
    public DocAttachment getDocAttachment() {
        return docAttachment;
    }

    /**
     * Get the accessibility modifier if available.
     * 
     * @return {@link Optional} accessibility modifier
     */
    public Optional<AccessModifier> getAccessModifier() {
        if ((this.bField.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            return Optional.of(AccessModifier.PUBLIC);
        } else if ((this.bField.symbol.flags & Flags.PRIVATE) == Flags.PRIVATE) {
            return Optional.of(AccessModifier.PRIVATE);
        }
        
        return Optional.empty();
    }

    /**
     * Get the signature of the field.
     * 
     * @return {}
     */
    public String getSignature() {
        StringBuilder signature = new StringBuilder(this.typeDescriptor.getSignature() + " " + this.getFieldName());
        if (this.isOptional()) {
            signature.append("?");
        }

        return signature.toString();
    }
}

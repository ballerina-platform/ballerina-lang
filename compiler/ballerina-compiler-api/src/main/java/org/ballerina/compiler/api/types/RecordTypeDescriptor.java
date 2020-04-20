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

import org.ballerina.compiler.api.model.BallerinaField;
import org.ballerina.compiler.api.model.ModuleID;
import org.ballerinalang.model.types.TypeKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents a record type descriptor.
 * 
 * @since 1.3.0
 */
public class RecordTypeDescriptor extends BallerinaTypeDesc {
    private List<BallerinaField> fieldDescriptors;
    private boolean isInclusive;
    private TypeDescriptor typeReference;
    private TypeDescriptor restTypeDesc;
    
    public RecordTypeDescriptor(TypeDescKind typeDescKind,
                                ModuleID moduleID,
                                List<BallerinaField> fieldDescriptors,
                                TypeDescriptor typeReference,
                                TypeDescriptor restTypeDesc,
                                boolean isInclusive) {
        super(typeDescKind, moduleID, TypeKind.RECORD);
        this.fieldDescriptors = fieldDescriptors;
        this.typeReference = typeReference;
        this.restTypeDesc = restTypeDesc;
        this.isInclusive = isInclusive;
    }

    /**
     * Get the list of field descriptors.
     * 
     * @return {@link List} of ballerina field
     */
    public List<BallerinaField> getFieldDescriptors() {
        return this.fieldDescriptors;
    }

    /**
     * Whether inclusive record ot not.
     * 
     * @return {@link Boolean} inclusive or not
     */
    public boolean isInclusive() {
        return isInclusive;
    }

    /**
     * Get the type reference.
     * 
     * @return {@link TypeDescriptor} type reference
     */
    public Optional<TypeDescriptor> getTypeReference() {
        return Optional.ofNullable(this.typeReference);
    }

    public Optional<TypeDescriptor> getRestTypeDesc() {
        return Optional.ofNullable(restTypeDesc);
    }

    @Override
    public String getSignature() {
        StringJoiner joiner = new StringJoiner(";");
        for (BallerinaField fieldDescriptor : this.fieldDescriptors) {
            String ballerinaFieldSignature = fieldDescriptor.getSignature();
            joiner.add(ballerinaFieldSignature);
        }
        this.getRestTypeDesc().ifPresent(typeDescriptor -> joiner.add(typeDescriptor.getSignature() + "..."));
        this.getTypeReference().ifPresent(typeDescriptor -> joiner.add("*" + typeDescriptor.getSignature()));
        
        StringBuilder signature = new StringBuilder("{");
        if (!this.isInclusive) {
            signature.append("|");
        }
        signature.append(joiner.toString());
        if (!this.isInclusive) {
            signature.append("|");
        }
        signature.append("}");
        return signature.toString();
    }

    /**
     * Represents Tuple Type Descriptor.
     */
    public static class RecordTypeBuilder extends TypeBuilder<RecordTypeBuilder> {
        private List<BallerinaField> fieldDescriptors = new ArrayList<>();
        private boolean isInclusive;
        private TypeDescriptor typeReference;
        private TypeDescriptor restTypeDesc;

        /**
         * Symbol Builder Constructor.
         *
         * @param typeDescKind type descriptor kind
         * @param moduleID     Module ID of the type descriptor
         */
        public RecordTypeBuilder(TypeDescKind typeDescKind, ModuleID moduleID, boolean isInclusive) {
            super(typeDescKind, moduleID);
            this.isInclusive = isInclusive;
        }

        /**
         * Build the Ballerina Type Descriptor.
         *
         * @return {@link TypeDescriptor} built
         */
        public TypeDescriptor build() {
            return new RecordTypeDescriptor(this.typeDescKind,
                    this.moduleID,
                    this.fieldDescriptors,
                    this.typeReference,
                    this.restTypeDesc,
                    this.isInclusive);
        }

        public RecordTypeBuilder withFieldTypeDesc(BallerinaField ballerinaField) {
            this.fieldDescriptors.add(ballerinaField);
            return this;
        }

        public RecordTypeBuilder withTypeReference(TypeDescriptor typeReference) {
            this.typeReference = typeReference;
            return this;
        }

        public RecordTypeBuilder withRestTypeDesc(TypeDescriptor restTypeDesc) {
            this.restTypeDesc = restTypeDesc;
            return this;
        }
    }
}

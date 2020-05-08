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
import org.ballerina.compiler.api.semantic.TypesFactory;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;

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
    
    private BRecordType recordType;
    private List<BallerinaField> fieldDescriptors;
    private boolean isInclusive;
    private TypeDescriptor typeReference;
    private TypeDescriptor restTypeDesc;
    
    public RecordTypeDescriptor(ModuleID moduleID, BRecordType recordType) {
        super(TypeDescKind.RECORD, moduleID);
        this.recordType = recordType;
        this.isInclusive = !recordType.sealed;
    }

    /**
     * Get the list of field descriptors.
     * 
     * @return {@link List} of ballerina field
     */
    public List<BallerinaField> getFieldDescriptors() {
        if (this.fieldDescriptors == null) {
            this.fieldDescriptors = new ArrayList<>();
            for (BField field : this.recordType.fields) {
                this.fieldDescriptors.add(new BallerinaField(field));
            }
        }
        
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
        if (this.restTypeDesc == null) {
            this.restTypeDesc = TypesFactory.getTypeDescriptor(this.recordType.restFieldType);
        }
        return Optional.ofNullable(this.restTypeDesc);
    }

    @Override
    public String getSignature() {
        StringJoiner joiner = new StringJoiner(";");
        for (BallerinaField fieldDescriptor : this.getFieldDescriptors()) {
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
}

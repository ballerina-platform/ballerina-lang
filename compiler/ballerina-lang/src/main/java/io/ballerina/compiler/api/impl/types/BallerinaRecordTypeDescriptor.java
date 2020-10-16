/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl.types;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.FieldDescriptor;
import io.ballerina.compiler.api.types.RecordTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents a record type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaRecordTypeDescriptor extends AbstractTypeDescriptor implements RecordTypeDescriptor {
    private List<FieldDescriptor> fieldDescriptors;
    private final boolean isInclusive;
    private BallerinaTypeDescriptor restTypeDesc;

    public BallerinaRecordTypeDescriptor(ModuleID moduleID, BRecordType recordType) {
        super(TypeDescKind.RECORD, moduleID, recordType);
        this.isInclusive = !recordType.sealed;
    }

    public BallerinaRecordTypeDescriptor(ModuleID moduleID, List<FieldDescriptor> fields,
                                         BallerinaTypeDescriptor restType, BRecordType recordType) {
        super(TypeDescKind.RECORD, moduleID, recordType);
        this.fieldDescriptors = fields;
        this.restTypeDesc = restType;
        this.isInclusive = !recordType.sealed;
    }

    /**
     * Get the list of field descriptors.
     *
     * @return {@link List} of ballerina field
     */
    @Override
    public List<FieldDescriptor> fieldDescriptors() {
        return this.fieldDescriptors;
    }

    /**
     * Whether inclusive record ot not.
     *
     * @return {@link Boolean} inclusive or not
     */
    @Override
    public boolean inclusive() {
        return isInclusive;
    }

    @Override
    public Optional<BallerinaTypeDescriptor> restTypeDescriptor() {
        return Optional.ofNullable(this.restTypeDesc);
    }

    @Override
    public String signature() {
        StringJoiner joiner;
        if (this.isInclusive) {
            joiner = new StringJoiner("; ", "{ ", " }");
        } else {
            joiner = new StringJoiner("; ", "{| ", " |}");
        }
        for (FieldDescriptor fieldDescriptor : this.fieldDescriptors()) {
            String ballerinaFieldSignature = fieldDescriptor.signature();
            joiner.add(ballerinaFieldSignature);
        }

        restTypeDescriptor().ifPresent(typeDescriptor -> joiner.add(typeDescriptor.signature() + "..."));

        return joiner.toString();
    }

    // Setters. Only to be used by the TypeBuilder
    void setFieldDescriptors(List<FieldDescriptor> fields) {
        this.fieldDescriptors = fields;
    }

    void setRestTypeDescriptor(BallerinaTypeDescriptor restType) {
        this.restTypeDesc = restType;
    }
}

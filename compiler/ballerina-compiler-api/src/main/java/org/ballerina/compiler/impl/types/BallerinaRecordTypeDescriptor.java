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
package org.ballerina.compiler.impl.types;

import org.ballerina.compiler.api.ModuleID;
import org.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import org.ballerina.compiler.api.types.FieldDescriptor;
import org.ballerina.compiler.api.types.RecordTypeDescriptor;
import org.ballerina.compiler.api.types.TypeDescKind;
import org.ballerina.compiler.impl.TypesFactory;
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
public class BallerinaRecordTypeDescriptor extends AbstractTypeDescriptor implements RecordTypeDescriptor {

    private List<FieldDescriptor> fieldDescriptors;
    private boolean isInclusive;
    // private TypeDescriptor typeReference;
    private BallerinaTypeDescriptor restTypeDesc;

    public BallerinaRecordTypeDescriptor(ModuleID moduleID, BRecordType recordType) {
        super(TypeDescKind.RECORD, moduleID, recordType);
        this.isInclusive = !recordType.sealed;
        // TODO: Fix this
        // this.typeReference = null;
    }

    /**
     * Get the list of field descriptors.
     *
     * @return {@link List} of ballerina field
     */
    @Override
    public List<FieldDescriptor> fieldDescriptors() {
        if (this.fieldDescriptors == null) {
            this.fieldDescriptors = new ArrayList<>();
            for (BField field : ((BRecordType) this.getBType()).fields.values()) {
                this.fieldDescriptors.add(new BallerinaFieldDescriptor(field));
            }
        }

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
    public Optional<BallerinaTypeDescriptor> getRestTypeDesc() {
        if (this.restTypeDesc == null) {
            this.restTypeDesc = TypesFactory.getTypeDescriptor(((BRecordType) this.getBType()).restFieldType);
        }
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

        getRestTypeDesc().ifPresent(typeDescriptor -> joiner.add(typeDescriptor.signature() + "..."));
        // this.getTypeReference().ifPresent(typeDescriptor -> joiner.add("*" + typeDescriptor.getSignature()));

        return joiner.toString();
    }
}

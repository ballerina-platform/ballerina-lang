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
import io.ballerina.compiler.api.impl.TypesFactory;
import io.ballerina.compiler.api.types.FieldSymbol;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents a record type descriptor.
 *
 * @since 2.0.0
 */
public class RecordTypeSymbol extends AbstractTypeSymbol implements io.ballerina.compiler.api.types.RecordTypeSymbol {
    private List<FieldSymbol> fieldSymbols;
    private final boolean isInclusive;
    private TypeSymbol restTypeDesc;

    public RecordTypeSymbol(ModuleID moduleID, BRecordType recordType) {
        super(TypeDescKind.RECORD, moduleID, recordType);
        this.isInclusive = !recordType.sealed;
    }

    /**
     * Get the list of field descriptors.
     *
     * @return {@link List} of ballerina field
     */
    @Override
    public List<FieldSymbol> fieldDescriptors() {
        if (this.fieldSymbols == null) {
            this.fieldSymbols = new ArrayList<>();
            for (BField field : ((BRecordType) this.getBType()).fields.values()) {
                this.fieldSymbols.add(new BallerinaFieldSymbol(field));
            }
        }

        return this.fieldSymbols;
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
    public Optional<TypeSymbol> restTypeDescriptor() {
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
        for (FieldSymbol fieldSymbol : this.fieldDescriptors()) {
            String ballerinaFieldSignature = fieldSymbol.signature();
            joiner.add(ballerinaFieldSignature);
        }

        restTypeDescriptor().ifPresent(typeDescriptor -> joiner.add(typeDescriptor.signature() + "..."));

        return joiner.toString();
    }
}

/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.compiler.api.impl.symbols;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.FieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents a record type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaRecordTypeSymbol extends AbstractTypeSymbol implements RecordTypeSymbol {

    private List<FieldSymbol> fieldSymbols;
    private final boolean isInclusive;
    private TypeSymbol restTypeDesc;
    private List<TypeSymbol> typeInclusions;

    public BallerinaRecordTypeSymbol(CompilerContext context, ModuleID moduleID, BRecordType recordType) {
        super(context, TypeDescKind.RECORD, moduleID, recordType);
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
                this.fieldSymbols.add(new BallerinaFieldSymbol(this.context, field));
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
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            this.restTypeDesc = typesFactory.getTypeDescriptor(((BRecordType) this.getBType()).restFieldType);
        }

        return Optional.ofNullable(this.restTypeDesc);
    }

    @Override
    public List<TypeSymbol> typeInclusions() {
        if (this.typeInclusions == null) {
            TypesFactory typesFactory = TypesFactory.getInstance(this.context);
            List<BType> inclusions = ((BRecordType) this.getBType()).typeInclusions;

            List<TypeSymbol> typeRefs = new ArrayList<>();
            for (BType inclusion : inclusions) {
                TypeSymbol type = typesFactory.getTypeDescriptor(inclusion);

                // If the inclusion was not a type ref, the type would be semantic error and the type factory will
                // return null. Therefore, skipping them.
                if (type != null) {
                    typeRefs.add(type);
                }
            }

            this.typeInclusions = Collections.unmodifiableList(typeRefs);
        }

        return this.typeInclusions;
    }

    @Override
    public String signature() {
        StringJoiner joiner;
        if (this.isInclusive) {
            joiner = new StringJoiner(" ", "{ ", " }");
        } else {
            joiner = new StringJoiner(" ", "{| ", " |}");
        }
        for (FieldSymbol fieldSymbol : this.fieldDescriptors()) {
            String ballerinaFieldSignature = fieldSymbol.signature() + ";";
            joiner.add(ballerinaFieldSignature);
        }

        restTypeDescriptor().ifPresent(typeDescriptor -> {
            joiner.add(typeDescriptor.signature() + "...;");
        });

        return "record " + joiner.toString();
    }
}

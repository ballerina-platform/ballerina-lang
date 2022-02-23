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
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents a record type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaRecordTypeSymbol extends AbstractTypeSymbol implements RecordTypeSymbol {

    private Map<String, RecordFieldSymbol> fieldSymbols;
    private TypeSymbol restTypeDesc;
    private List<TypeSymbol> typeInclusions;

    public BallerinaRecordTypeSymbol(CompilerContext context, ModuleID moduleID, BRecordType recordType) {
        super(context, TypeDescKind.RECORD, recordType);
    }

    @Override
    public Map<String, RecordFieldSymbol> fieldDescriptors() {
        if (this.fieldSymbols != null) {
            return this.fieldSymbols;
        }

        Map<String, RecordFieldSymbol> fields = new LinkedHashMap<>();
        BRecordType type = (BRecordType) this.getBType();

        for (BField field : type.fields.values()) {
            fields.put(field.name.value, new BallerinaRecordFieldSymbol(this.context, field));
        }

        this.fieldSymbols = Collections.unmodifiableMap(fields);
        return this.fieldSymbols;
    }

    @Override
    public Optional<TypeSymbol> restTypeDescriptor() {
        if (this.restTypeDesc == null) {
            BType restFieldType = ((BRecordType) this.getBType()).restFieldType;
            if (restFieldType.tag != TypeTags.NONE) {
                TypesFactory typesFactory = TypesFactory.getInstance(this.context);
                this.restTypeDesc = typesFactory.getTypeDescriptor(restFieldType);
            }
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
        // Treating every record typedesc as exclusive record typedescs.
        StringJoiner joiner = new StringJoiner(" ", "{| ", " |}");
        for (RecordFieldSymbol fieldSymbol : this.fieldDescriptors().values()) {
            String ballerinaFieldSignature = fieldSymbol.signature() + ";";
            joiner.add(ballerinaFieldSignature);
        }

        restTypeDescriptor().ifPresent(typeDescriptor -> {
            joiner.add(typeDescriptor.signature() + "...;");
        });

        return "record " + joiner.toString();
    }
}

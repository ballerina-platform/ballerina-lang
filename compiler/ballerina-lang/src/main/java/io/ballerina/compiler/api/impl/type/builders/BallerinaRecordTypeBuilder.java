/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl.type.builders;

import io.ballerina.compiler.api.TypeBuilder;
import io.ballerina.compiler.api.impl.symbols.AbstractTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerinalang.model.elements.Flag;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The implementation of the methods used to build the Record type descriptor in Types API.
 *
 * @since 2201.2.0
 */
public class BallerinaRecordTypeBuilder implements TypeBuilder.RECORD {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private TypeSymbol restField;
    private final List<RECORD_FIELD> recordFieldList = new ArrayList<>();
    private final List<TypeReferenceTypeSymbol> typeInclusions = new ArrayList<>();

    public BallerinaRecordTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
    }

    @Override
    public RECORD_FIELD fields() {
        return new RecordFieldDataHolder();
    }

    @Override
    public TypeBuilder.RECORD withFields(RECORD_FIELD... fields) {
        recordFieldList.clear();
        recordFieldList.addAll(Arrays.asList(fields));

        return this;
    }

    @Override
    public TypeBuilder.RECORD withRestField(TypeSymbol restType) {
        restField = restType;

        return this;
    }

    @Override
    public TypeBuilder.RECORD withTypeInclusions(TypeReferenceTypeSymbol... typeInclusions) {
        this.typeInclusions.clear();
        this.typeInclusions.addAll(Arrays.asList(typeInclusions));

        return this;
    }

    @Override
    public RecordTypeSymbol build() {

        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(Flags.PUBLIC, Names.EMPTY,
                symTable.rootPkgSymbol.pkgID, null, symTable.rootPkgSymbol, symTable.builtinPos,
                symTable.rootPkgSymbol.origin);

        BRecordType recordType = new BRecordType(recordSymbol);
        recordSymbol.type = recordType;
        recordType.typeInclusions = getTypeInclusions(typeInclusions);
        if (restField == null) {
            recordType.restFieldType = symTable.noType;
        } else {
            recordType.restFieldType = getBType(restField);
        }

        recordType.fields = getFields(recordFieldList, recordSymbol);
        RecordTypeSymbol recordFieldSymbol = (RecordTypeSymbol) typesFactory.getTypeDescriptor(recordType);

        typeInclusions.clear();
        restField = null;
        recordFieldList.clear();

        return recordFieldSymbol;
    }

    /**
     * The data holder class of Record fields.
     *
     * @since 2.0.0
     */
    public static class RecordFieldDataHolder implements RECORD_FIELD {

        private TypeSymbol typeSymbol;
        private String name;
        private boolean readOnly = false;
        private boolean isOptional = false;
        private boolean hasDefaultValue = false;
        public RecordFieldDataHolder() {
        }

        @Override
        public RECORD_FIELD isReadOnly() {
            readOnly = true;

            return this;
        }

        @Override
        public RECORD_FIELD withType(TypeSymbol type) {
            typeSymbol = type;

            return this;
        }

        @Override
        public RECORD_FIELD withName(String name) {
            this.name = name;

            return this;
        }

        @Override
        public RECORD_FIELD isOptional() {
            isOptional = true;

            return this;
        }

        @Override
        public RECORD_FIELD hasDefaultExpr() {
            hasDefaultValue = true;

            return this;
        }

        @Override
        public boolean isFieldReadOnly() {
            return readOnly;
        }

        @Override
        public TypeSymbol getType() {
            return typeSymbol;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isFieldOptional() {
            return isOptional;
        }

        @Override
        public boolean hasFieldDefaultExpr() {
            return hasDefaultValue;
        }

        @Override
        public RECORD_FIELD get() {
            if (name == null) {
                throw new IllegalArgumentException("Field name can not be null");
            }

            return this;
        }
    }

    // utils

    private LinkedHashMap<String, BField> getFields(List<RECORD_FIELD> recordFieldList,
                                                    BRecordTypeSymbol ownerRecordSymbol) {
        LinkedHashMap<String, BField> fieldList = new LinkedHashMap<>();
        for (RECORD_FIELD recordField : recordFieldList) {
            BField bField = getBField(recordField, ownerRecordSymbol);
            fieldList.put(bField.getName().getValue(), bField);
        }

        return fieldList;
    }

    private BField getBField(RECORD_FIELD recordField, BRecordTypeSymbol ownerRecordSymbol) {
        EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        if (recordField.isFieldOptional()) {
            flags.add(Flag.OPTIONAL);
        }

        if (recordField.isFieldReadOnly()) {
            flags.add(Flag.READONLY);
        }

        if (recordField.hasFieldDefaultExpr()) {
            flags.remove(Flag.OPTIONAL);
            flags.add(Flag.REQUIRED);
        }

        BVarSymbol fieldSymbol = new BVarSymbol(Flags.asMask(flags), Names.fromString(recordField.getName()),
                ownerRecordSymbol.pkgID, getBType(recordField.getType()), ownerRecordSymbol, symTable.builtinPos,
                ownerRecordSymbol.origin);

        return new BField(Names.fromString(recordField.getName()), symTable.builtinPos, fieldSymbol);
    }

    private List<BType> getTypeInclusions(List<TypeReferenceTypeSymbol> typeInclusions) {
        List<BType> typeInclusionList = new ArrayList<>();
        for (TypeReferenceTypeSymbol typeInclusion : typeInclusions) {
            typeInclusionList.add(getBType(typeInclusion));
        }

        return typeInclusionList;
    }

    private BType getBType(TypeSymbol typeSymbol) {
        if (typeSymbol != null) {
            if (typeSymbol instanceof AbstractTypeSymbol abstractTypeSymbol) {
                return abstractTypeSymbol.getBType();
            }

            throw new IllegalArgumentException("Invalid type provided");
        }

        return null;
    }
}

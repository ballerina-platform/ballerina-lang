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
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ballerina Record variable.
 * 
 * @since 1.3.0
 */
public class BallerinaRecordVarSymbol extends BallerinaVariable {
    
    private List<BallerinaField> recordFields;
    private TypeDescriptor restType;
    
    protected BallerinaRecordVarSymbol(String name,
                                       PackageID moduleID,
                                       List<AccessModifier> accessModifiers,
                                       TypeDescriptor typeDescriptor,
                                       List<BallerinaField> fields,
                                       TypeDescriptor restType,
                                       BSymbol symbol) {
        super(name, moduleID, BallerinaSymbolKind.RECORD, accessModifiers, typeDescriptor, symbol);
        this.recordFields = fields;
        this.restType = restType;
    }

    public List<BallerinaField> getRecordFields() {
        return recordFields;
    }

    public TypeDescriptor getRestType() {
        return restType;
    }

    /**
     * Represents the Record symbol builder.
     * 
     * @since 1.3.0
     */
    public static class RecordVarSymbolBuilder extends BallerinaVariable.VariableSymbolBuilder {
        
        private List<BallerinaField> recordFields = new ArrayList<>();
        private TypeDescriptor restType;
        
        public RecordVarSymbolBuilder(String name, PackageID moduleID, BRecordTypeSymbol recordTypeSymbol) {
            super(name, moduleID, recordTypeSymbol);
            withRecordTypeSymbol(recordTypeSymbol);
        }

        @Override
        public BallerinaRecordVarSymbol build() {
            if (this.typeDescriptor == null) {
                throw new AssertionError("Type descriptor cannot be null");
            }
            return new BallerinaRecordVarSymbol(this.name,
                    this.moduleID,
                    this.accessModifiers,
                    this.typeDescriptor,
                    this.recordFields,
                    this.restType,
                    this.bSymbol);
        }

        /**
         * Builder with the record type symbol.
         * 
         * @param recordTypeSymbol Record Type Symbol instance
         */
        private void withRecordTypeSymbol(BRecordTypeSymbol recordTypeSymbol) {
            for (BField field : ((BRecordType) recordTypeSymbol.getType()).fields.values()) {
                BallerinaField ballerinaField = new BallerinaField(field);
                this.recordFields.add(ballerinaField);
            }
            this.restType = TypesFactory.getTypeDescriptor(((BRecordType) recordTypeSymbol.getType()).restFieldType);
        }

        @Override
        public RecordVarSymbolBuilder withTypeDescriptor(TypeDescriptor typeDescriptor) {
            return (RecordVarSymbolBuilder) super.withTypeDescriptor(typeDescriptor);
        }
    }
}

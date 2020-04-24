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
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents an object type descriptor.
 * 
 * @since 1.3.0
 */
public class ObjectTypeDescriptor extends BallerinaTypeDesc {
    List<TypeQualifier> typeQualifiers;
    TypeDescriptor objectTypeReference;
    List<BallerinaField> objectFields;
    List<FunctionTypeDescriptor> methods;
    FunctionTypeDescriptor initFunction;
    
    private ObjectTypeDescriptor(TypeDescKind typeDescKind,
                                 ModuleID moduleID,
                                 List<TypeQualifier> typeQualifiers,
                                 List<BallerinaField> objectFields,
                                 List<FunctionTypeDescriptor> methods,
                                 FunctionTypeDescriptor initFunction,
                                 TypeDescriptor objectTypeReference) {
        super(typeDescKind, moduleID, TypeKind.OBJECT);
        this.typeQualifiers = typeQualifiers;
        this.objectFields = objectFields;
        this.methods = methods;
        this.initFunction = initFunction;
        this.objectTypeReference = objectTypeReference;
    }

    /**
     * Get the object type qualifiers.
     * 
     * @return {@link List} of object type qualifiers
     */
    public List<TypeQualifier> getTypeQualifiers() {
        return typeQualifiers;
    }

    /**
     * Get the object fields.
     * 
     * @return {@link List} of object fields
     */
    public List<BallerinaField> getObjectFields() {
        return objectFields;
    }

    /**
     * Get the list of methods.
     * 
     * @return {@link List} of object methods
     */
    public List<FunctionTypeDescriptor> getMethods() {
        return this.methods;
    }
    
    public FunctionTypeDescriptor getInitializer() {
        return this.initFunction;
    }

    /**
     * Get the object type reference.
     * 
     * @return {@link Optional} type reference
     */
    public Optional<TypeDescriptor> getObjectTypeReference() {
        return Optional.ofNullable(objectTypeReference);
    }

    @Override
    public String getSignature() {
        StringBuilder signature = new StringBuilder();
        StringJoiner qualifierJoiner = new StringJoiner(" ");
        StringJoiner fieldJoiner = new StringJoiner(";");
        StringJoiner methodJoiner = new StringJoiner(" ");
        
        for (TypeQualifier typeQualifier : this.getTypeQualifiers()) {
            String value = typeQualifier.getValue();
            qualifierJoiner.add(value);
        }
        signature.append(qualifierJoiner.toString()).append(" object {");
        
        this.getObjectTypeReference().ifPresent(typeDescriptor -> fieldJoiner.add("*" + typeDescriptor.getSignature()));
        this.getObjectFields().forEach(objectFieldDescriptor -> fieldJoiner.add(objectFieldDescriptor.getSignature()));
        this.getMethods().forEach(method -> methodJoiner.add(method.getSignature()));
        
        return signature.append(fieldJoiner.toString())
                .append(methodJoiner.toString())
                .append("}")
                .toString();
    }

    /**
     * Represents Object Type Builder.
     */
    public static class ObjectTypeBuilder extends TypeBuilder<ObjectTypeBuilder> {
        
        List<TypeQualifier> typeQualifiers = new ArrayList<>();
        List<BallerinaField> objectFields = new ArrayList<>();
        TypeDescriptor objectTypeReference;
        List<FunctionTypeDescriptor> methods = new ArrayList<>();
        FunctionTypeDescriptor initFunction;
        
        /**
         * Symbol Builder Constructor.
         *
         * @param typeDescKind type descriptor kind
         * @param moduleID     Module ID of the type descriptor
         */
        public ObjectTypeBuilder(TypeDescKind typeDescKind, PackageID moduleID) {
            super(typeDescKind, moduleID, TypeKind.OBJECT);
        }

        /**
         * Build the Ballerina Type Descriptor.
         *
         * @return {@link TypeDescriptor} built
         */
        public ObjectTypeDescriptor build() {
            return new ObjectTypeDescriptor(this.typeDescKind,
                    this.moduleID,
                    this.typeQualifiers,
                    this.objectFields,
                    this.methods,
                    this.initFunction,
                    this.objectTypeReference);
        }

        public ObjectTypeBuilder withTypeQualifier(TypeQualifier typeQualifier) {
            this.typeQualifiers.add(typeQualifier);
            return this;
        }

        public ObjectTypeBuilder withObjectField(BallerinaField objectField) {
            this.objectFields.add(objectField);
            return this;
        }

        public ObjectTypeBuilder withSymbol(BObjectTypeSymbol symbol) {
            for (BAttachedFunction function : symbol.attachedFuncs) {

                FunctionTypeDescriptor functionType =
                        TypesFactory.createFunctionTypeDescriptor(function.symbol.getType());
                this.methods.add(functionType);
            }
            if (symbol.initializerFunc != null) {
                this.initFunction = TypesFactory.createFunctionTypeDescriptor(symbol.initializerFunc.symbol.getType());
            }
            
            return this;
        }

        public ObjectTypeBuilder withObjectTypeReference(TypeDescriptor objectTypeReference) {
            this.objectTypeReference = objectTypeReference;
            return this;
        }
    }

    /**
     * Represents the object type qualifier.
     * 
     * @since 1.3.0
     */
    public enum TypeQualifier {
        ABSTRACT("abstract"),
        CLIENT("client");
        
        private String value;

        TypeQualifier(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

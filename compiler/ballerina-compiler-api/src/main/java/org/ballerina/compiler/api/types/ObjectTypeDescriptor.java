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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.util.Flags;

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

    private BObjectType objectType;
    private List<TypeQualifier> typeQualifiers;
    private TypeDescriptor objectTypeReference;
    private List<BallerinaField> objectFields;
    private List<FunctionTypeDescriptor> methods;
    private FunctionTypeDescriptor initFunction;

    public ObjectTypeDescriptor(ModuleID moduleID,
                                 BObjectType objectType) {
        super(TypeDescKind.OBJECT, moduleID);
        this.objectType = objectType;
    }

    /**
     * Get the object type qualifiers.
     *
     * @return {@link List} of object type qualifiers
     */
    public List<TypeQualifier> getTypeQualifiers() {
        if (this.typeQualifiers == null) {
            this.typeQualifiers = new ArrayList<>();
            if ((objectType.flags & Flags.ABSTRACT) == Flags.ABSTRACT) {
                this.typeQualifiers.add(ObjectTypeDescriptor.TypeQualifier.ABSTRACT);
            }
            if ((objectType.flags & Flags.CLIENT) == Flags.CLIENT) {
                this.typeQualifiers.add(ObjectTypeDescriptor.TypeQualifier.CLIENT);
            }
            if ((objectType.flags & Flags.LISTENER) == Flags.LISTENER) {
                this.typeQualifiers.add(ObjectTypeDescriptor.TypeQualifier.LISTENER);
            }
        }

        return this.typeQualifiers;
    }

    /**
     * Get the object fields.
     *
     * @return {@link List} of object fields
     */
    public List<BallerinaField> getObjectFields() {
        if (this.objectFields == null) {
            this.objectFields = new ArrayList<>();
            for (BField field : this.objectType.fields) {
                this.objectFields.add(new BallerinaField(field));
            }
        }
        return objectFields;
    }

    /**
     * Get the list of methods.
     *
     * @return {@link List} of object methods
     */
    public List<FunctionTypeDescriptor> getMethods() {
        if (this.methods == null) {
            this.methods = new ArrayList<>();
            for (BAttachedFunction attachedFunc : ((BObjectTypeSymbol) this.objectType.tsymbol).attachedFuncs) {
                this.methods.add(TypesFactory.createFunctionTypeDescriptor(attachedFunc.type));
            }
        }

        return this.methods;
    }

    public FunctionTypeDescriptor getInitializer() {
        if (this.initFunction == null) {
            this.initFunction = TypesFactory
                    .createFunctionTypeDescriptor(((BObjectTypeSymbol) this.objectType.tsymbol).initializerFunc.type);
        }

        return this.initFunction;
    }

    /**
     * Get the object type reference.
     *
     * @return {@link Optional} type reference
     */
    public Optional<TypeDescriptor> getObjectTypeReference() {
        return Optional.ofNullable(this.objectTypeReference);
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
     * Represents the object type qualifier.
     *
     * @since 1.3.0
     */
    public enum TypeQualifier {
        ABSTRACT("abstract"),
        LISTENER("listener"),
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

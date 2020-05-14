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
import org.ballerina.compiler.api.model.BallerinaFunctionSymbol;
import org.ballerina.compiler.api.model.ModuleID;
import org.ballerina.compiler.api.semantic.BallerinaTypeDesc;
import org.ballerina.compiler.api.semantic.SymbolFactory;
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

    private List<TypeQualifier> typeQualifiers;

    // private TypeDescriptor objectTypeReference;

    private List<BallerinaField> objectFields;

    private List<BallerinaFunctionSymbol> methods;

    private BallerinaFunctionSymbol initFunction;

    public ObjectTypeDescriptor(ModuleID moduleID,
                                BObjectType objectType) {
        super(TypeDescKind.OBJECT, moduleID, objectType);
        // TODO: Fix this
        // objectTypeReference = null;
    }

    /**
     * Get the object type qualifiers.
     *
     * @return {@link List} of object type qualifiers
     */
    public List<TypeQualifier> getTypeQualifiers() {
        if (this.typeQualifiers == null) {
            this.typeQualifiers = new ArrayList<>();
            if ((((BObjectType) this.getBType()).flags & Flags.ABSTRACT) == Flags.ABSTRACT) {
                this.typeQualifiers.add(ObjectTypeDescriptor.TypeQualifier.ABSTRACT);
            }
            if ((((BObjectType) this.getBType()).flags & Flags.CLIENT) == Flags.CLIENT) {
                this.typeQualifiers.add(ObjectTypeDescriptor.TypeQualifier.CLIENT);
            }
            if ((((BObjectType) this.getBType()).flags & Flags.LISTENER) == Flags.LISTENER) {
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
            for (BField field : ((BObjectType) this.getBType()).fields.values()) {
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
    public List<BallerinaFunctionSymbol> getMethods() {
        if (this.methods == null) {
            this.methods = new ArrayList<>();
            for (BAttachedFunction attachedFunc : ((BObjectTypeSymbol) ((BObjectType) this.getBType()).tsymbol)
                    .attachedFuncs) {
                this.methods.add(SymbolFactory.createFunctionSymbol(attachedFunc.symbol,
                        attachedFunc.funcName.getValue()));
            }
        }

        return this.methods;
    }

    public Optional<BallerinaFunctionSymbol> getInitializer() {
        if (this.initFunction == null) {
            BAttachedFunction initFunction =
                    ((BObjectTypeSymbol) ((BObjectType) this.getBType()).tsymbol).initializerFunc;
            this.initFunction = initFunction == null ? null : SymbolFactory
                    .createFunctionSymbol(initFunction.symbol, initFunction.funcName.getValue());
        }

        return Optional.ofNullable(this.initFunction);
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

        // this.getObjectTypeReference()
        //         .ifPresent(typeDescriptor -> fieldJoiner.add("*" + typeDescriptor.getSignature()));
        this.getObjectFields().forEach(objectFieldDescriptor -> fieldJoiner.add(objectFieldDescriptor.getSignature()));
        this.getMethods().forEach(method -> method.getTypeDescriptor()
                .ifPresent(typeDescriptor -> methodJoiner.add(typeDescriptor.getSignature())));

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

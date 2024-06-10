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
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import org.ballerinalang.model.elements.Flag;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;

import static org.ballerinalang.model.symbols.SymbolOrigin.COMPILED_SOURCE;

/**
 * The implementation of the methods used to build the Object type descriptor in Types API.
 *
 * @since 2201.2.0
 */
public class BallerinaObjectTypeBuilder implements TypeBuilder.OBJECT {

    private final TypesFactory typesFactory;
    private final SymbolTable symTable;
    private List<Qualifier> qualifiers = new ArrayList<>();
    private final List<OBJECT_FIELD> objectFieldList = new ArrayList<>();
    private final List<BAttachedFunction> objectMethodList = new ArrayList<>();
    private final List<TypeReferenceTypeSymbol> typeInclusions = new ArrayList<>();

    public BallerinaObjectTypeBuilder(CompilerContext context) {
        typesFactory = TypesFactory.getInstance(context);
        symTable = SymbolTable.getInstance(context);
    }

    @Override
    public TypeBuilder.OBJECT withQualifier(Qualifier... qualifiers) {
        this.qualifiers.clear();
        this.qualifiers.addAll(Arrays.asList(qualifiers));

        return this;
    }

    @Override
    public TypeBuilder.OBJECT withFields(OBJECT_FIELD... fields) {
        objectFieldList.clear();
        objectFieldList.addAll(Arrays.asList(fields));

        return this;
    }

    @Override
    public TypeBuilder.OBJECT withMethods(OBJECT_METHOD... methods) {
        objectMethodList.clear();
        objectMethodList.addAll(getObjectMethods(Arrays.asList(methods)));

        return this;
    }

    @Override
    public TypeBuilder.OBJECT withTypeInclusions(TypeReferenceTypeSymbol... inclusions) {
        typeInclusions.clear();
        typeInclusions.addAll(Arrays.asList(inclusions));

        return this;
    }

    @Override
    public OBJECT_FIELD fields() {
        return new ObjectFieldDataHolder();
    }

    @Override
    public OBJECT_METHOD methods() {
        return new ObjectMethodDataHolder();
    }

    @Override
    public ObjectTypeSymbol build() {
        EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        long typeFlags = 0;
        if (qualifiers.contains(Qualifier.ISOLATED)) {
            flags.add(Flag.ISOLATED);
            typeFlags |= Flags.ISOLATED;
        }

        if (qualifiers.contains(Qualifier.SERVICE)) {
            flags.add(Flag.SERVICE);
            typeFlags |= Flags.SERVICE;
        } else if (qualifiers.contains(Qualifier.CLIENT)) {
            flags.add(Flag.CLIENT);
            typeFlags |= Flags.CLIENT;
        }

        BObjectTypeSymbol objectSymbol = Symbols.createObjectSymbol(Flags.asMask(flags), Names.EMPTY,
                symTable.rootPkgSymbol.pkgID, null, symTable.rootPkgSymbol.owner, symTable.builtinPos, COMPILED_SOURCE);

        BObjectType objectType = new BObjectType(objectSymbol, typeFlags);
        objectType.fields = getObjectFields(objectFieldList, objectSymbol);
        objectType.typeInclusions.addAll(getTypeInclusions(typeInclusions));
        objectSymbol.type = objectType;
        objectSymbol.attachedFuncs = new ArrayList<>(objectMethodList);
        ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) typesFactory.getTypeDescriptor(objectType);

        objectFieldList.clear();
        objectMethodList.clear();
        typeInclusions.clear();
        qualifiers.clear();

        return objectTypeSymbol;
    }

    private List<BType> getTypeInclusions(List<TypeReferenceTypeSymbol> typeInclusions) {
        List<BType> typeInclusionList = new ArrayList<>();
        for (TypeReferenceTypeSymbol typeInclusion : typeInclusions) {
            typeInclusionList.add(getTypeInclusionBType(typeInclusion));
        }

        return typeInclusionList;
    }

    private List<BAttachedFunction> getObjectMethods(List<OBJECT_METHOD> objectMethodList) {
        List<BAttachedFunction> attachedFunctions = new ArrayList<>();
        for (OBJECT_METHOD objectMethod : objectMethodList) {
            BInvokableSymbol invokableSymbol = getObjectMethodInvokableSymbol(objectMethod);
            attachedFunctions.add(new BAttachedFunction(invokableSymbol.getOriginalName(), invokableSymbol,
                    invokableSymbol.getType(), symTable.builtinPos));
        }

        return attachedFunctions;
    }

    private BInvokableSymbol getObjectMethodInvokableSymbol(OBJECT_METHOD objectMethod) {
        EnumSet<Flag> flags = EnumSet.of(Flag.ATTACHED);
        Name symbolName = Names.fromString(objectMethod.getName());
        return Symbols.createInvokableSymbol(SymTag.FUNCTION, Flags.asMask(flags),
                symbolName, symbolName, symTable.rootPkgSymbol.pkgID, getBInvokableType(objectMethod.getType()),
                symTable.rootPkgSymbol.owner, symTable.builtinPos, symTable.rootPkgSymbol.origin);
    }

    private BInvokableType getBInvokableType(FunctionTypeSymbol type) {
        if (type instanceof AbstractTypeSymbol abstractTypeSymbol) {
            BType bType = abstractTypeSymbol.getBType();
            if (bType instanceof BInvokableType bInvokableType) {
                return bInvokableType;
            }
        }

        throw new IllegalArgumentException("Object method type descriptor can not be null");
    }

    private LinkedHashMap<String, BField> getObjectFields(List<OBJECT_FIELD> objectFieldList,
                                                          BObjectTypeSymbol ownerObjectTypeSymbol) {
        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
        for (OBJECT_FIELD objectField : objectFieldList) {
            BField bField = getBField(objectField, ownerObjectTypeSymbol);
            fields.put(bField.getName().getValue(), bField);
        }

        return fields;
    }

    private BField getBField(OBJECT_FIELD objectField, BObjectTypeSymbol ownerObjectTypeSymbol) {
        EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        if (objectField.isPublicField()) {
            flags.add(Flag.PUBLIC);
        }

        BVarSymbol fieldSymbol = new BVarSymbol(Flags.asMask(flags), Names.fromString(objectField.getName()),
                ownerObjectTypeSymbol.pkgID, getBType(objectField.getType()), ownerObjectTypeSymbol,
                symTable.builtinPos, ownerObjectTypeSymbol.origin);

        return new BField(Names.fromString(objectField.getName()), symTable.builtinPos, fieldSymbol);
    }

    private BType getBType(TypeSymbol typeSymbol) {
        if (typeSymbol != null) {
            if (typeSymbol instanceof AbstractTypeSymbol abstractTypeSymbol) {
                return abstractTypeSymbol.getBType();
            }
        }

        throw new IllegalArgumentException("Invalid type provided");
    }

    private BType getTypeInclusionBType(TypeSymbol typeSymbol) {
        if (typeSymbol != null) {
            if (typeSymbol instanceof AbstractTypeSymbol abstractTypeSymbol) {
                return abstractTypeSymbol.getBType();
            }

            throw new IllegalArgumentException("Invalid type provided");
        }

        return null;
    }

    /**
     * The data holder class of Object methods.
     *
     * @since 2.0.0
     */
    public static class ObjectMethodDataHolder implements OBJECT_METHOD {
        private List<Qualifier> qualifiers = new ArrayList<>();
        private FunctionTypeSymbol type;
        private String name;

        public ObjectMethodDataHolder() {
        }

        @Override
        public OBJECT_METHOD withQualifiers(Qualifier... qualifiers) {
            if (!this.qualifiers.isEmpty()) {
                this.qualifiers = new ArrayList<>();
            }

            this.qualifiers.addAll(Arrays.asList(qualifiers));

            return this;
        }

        @Override
        public OBJECT_METHOD withName(String name) {
            this.name = name;

            return this;
        }

        @Override
        public OBJECT_METHOD withType(FunctionTypeSymbol type) {
            this.type = type;

            return this;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public FunctionTypeSymbol getType() {
            return type;
        }

        @Override
        public OBJECT_METHOD get() {
            if (name == null) {
                throw new IllegalArgumentException("Method name can not be null");
            }

            return this;
        }
    }

    /**
     * The data holder class of Object fields.
     *
     * @since 2.0.0
     */
    public static class ObjectFieldDataHolder implements OBJECT_FIELD {
        private boolean isPublicField = false;
        private TypeSymbol type;
        private String name;
        public ObjectFieldDataHolder() {
        }

        @Override
        public OBJECT_FIELD isPublic() {
            isPublicField = true;

            return this;
        }

        @Override
        public OBJECT_FIELD withType(TypeSymbol type) {
            this.type = type;

            return this;
        }

        @Override
        public OBJECT_FIELD withName(String name) {
            this.name = name;

            return this;
        }

        @Override
        public boolean isPublicField() {
            return this.isPublicField;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public TypeSymbol getType() {
            return type;
        }

        @Override
        public OBJECT_FIELD get() {
            if (name == null) {
                throw new IllegalArgumentException("Field name can not be null");
            }

            return this;
        }
    }
}

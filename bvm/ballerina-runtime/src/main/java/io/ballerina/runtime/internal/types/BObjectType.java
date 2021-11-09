/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.types.TypeIdSet;
import io.ballerina.runtime.api.utils.IdentifierUtils;
import io.ballerina.runtime.api.utils.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringJoiner;

import static io.ballerina.runtime.api.TypeTags.SERVICE_TAG;

/**
 * {@code BObjectType} represents a user defined object type in Ballerina.
 *
 * @since 0.995.0
 */
public class BObjectType extends BStructureType implements ObjectType {

    private MethodType[] methodTypes;
    public MethodType initializer;
    public MethodType generatedInitializer;

    private final boolean readonly;
    protected IntersectionType immutableType;
    private IntersectionType intersectionType = null;
    public BTypeIdSet typeIdSet;

    private String cachedToString;
    private boolean resolving;

    /**
     * Create a {@code BObjectType} which represents the user defined struct type.
     *
     * @param typeName string name of the type
     * @param pkg package of the struct
     * @param flags flags of the object type
     */
    public BObjectType(String typeName, Module pkg, long flags) {
        super(typeName, pkg, flags, Object.class);
        this.readonly = SymbolFlags.isFlagOn(flags, SymbolFlags.READONLY);
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) ValueCreator.createObjectValue(this.pkg, this.typeName);
    }

    @Override
    public String getAnnotationKey() {
        return IdentifierUtils.decodeIdentifier(this.typeName);
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.OBJECT_TYPE_TAG;
    }
    
    @Override
    public MethodType[] getMethods() {
        return methodTypes;
    }

    @Override
    public boolean isIsolated() {
        return SymbolFlags.isFlagOn(getFlags(), SymbolFlags.ISOLATED);
    }

    @Override
    public boolean isIsolated(String methodName) {
        if (!isIsolated()) {
            return false;
        }
        for (MethodType method : this.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method.isIsolated();
            }
        }
        if (this.getTag() == SERVICE_TAG) {
            for (ResourceMethodType method : ((BServiceType) this).getResourceMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.isIsolated();
                }
            }
        }
        throw ErrorCreator.createError(StringUtils.fromString("No such method: " + methodName));
    }

    public void setMethods(MethodType[] methodTypes) {
        this.methodTypes = methodTypes;
    }

    public void setInitializer(BMethodType initializer) {
        this.initializer = initializer;
    }

    public void setGeneratedInitializer(BMethodType generatedInitializer) {
        this.generatedInitializer = generatedInitializer;
    }

    public void computeStringRepresentation() {
        if (cachedToString != null) {
            return;
        }
        final String name = (pkg == null || pkg.getName() == null || pkg.getName().equals(".")) ?
                typeName : pkg.getName() + ":" + typeName;
        if (!typeName.contains("$anon")) {
            cachedToString = name;
            return;
        }
        StringJoiner sj = new StringJoiner(",\n\t", name + " {\n\t", "\n}");
        for (Entry<String, Field> field : getFields().entrySet()) {
            sj.add(field.getKey() + " : " + field.getValue().getFieldType());
        }
        for (MethodType func : methodTypes) {
            sj.add(func.toString());
        }
        cachedToString = sj.toString();
    }

    public String toString() {
        if (resolving) {
            return "";
        }
        resolving = true;
        computeStringRepresentation();
        resolving = false;
        return cachedToString;
    }

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public IntersectionType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        this.immutableType = immutableType;
    }

    @Override
    public Optional<IntersectionType> getIntersectionType() {
        return this.intersectionType ==  null ? Optional.empty() : Optional.of(this.intersectionType);
    }

    @Override
    public void setIntersectionType(IntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }

    public void setTypeIdSet(BTypeIdSet typeIdSet) {
        this.typeIdSet = typeIdSet;
    }

    public BObjectType duplicate() {
        BObjectType type = new BObjectType(this.typeName, this.pkg, this.flags);
        type.setFields(fields);
        type.setMethods(duplicateArray(methodTypes));
        type.immutableType = this.immutableType;
        type.typeIdSet = this.typeIdSet;
        return type;
    }

    protected  <T extends MethodType> T[] duplicateArray(T[] methodTypes) {
        Class<?> elemType = methodTypes.getClass().getComponentType();
        T[] array = (T[]) Array.newInstance(elemType, methodTypes.length);
        for (int i = 0; i < methodTypes.length; i++) {
            BMethodType functionType = (BMethodType) methodTypes[i];
            array[i] = (T) functionType.duplicate();
        }

        return array;
    }

    public boolean hasAnnotations() {
        return !annotations.isEmpty();
    }

    @Override
    public TypeIdSet getTypeIdSet() {
        return new BTypeIdSet(new ArrayList<>(typeIdSet.ids));
    }
}

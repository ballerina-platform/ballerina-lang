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
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MemberFunctionType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.AnnotationUtils;
import io.ballerina.runtime.internal.scheduling.Strand;
import io.ballerina.runtime.internal.values.MapValue;

import java.util.Map.Entry;
import java.util.StringJoiner;

/**
 * {@code BObjectType} represents a user defined object type in Ballerina.
 *
 * @since 0.995.0
 */
public class BObjectType extends BStructureType implements ObjectType {

    private MemberFunctionType[] attachedFunctions;
    public MemberFunctionType initializer;
    public MemberFunctionType generatedInitializer;

    private final boolean readonly;
    private IntersectionType immutableType;
    public BTypeIdSet typeIdSet;

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
        return this.typeName;
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        return null;
    }

    @Override
    public int getTag() {
        return TypeTags.OBJECT_TYPE_TAG;
    }

    public MemberFunctionType[] getAttachedFunctions() {
        return attachedFunctions;
    }

    public void setAttachedFunctions(MemberFunctionType[] attachedFunctions) {
        this.attachedFunctions = attachedFunctions;
    }

    public void setInitializer(BMemberFunctionType initializer) {
        this.initializer = initializer;
    }

    public void setGeneratedInitializer(BMemberFunctionType generatedInitializer) {
        this.generatedInitializer = generatedInitializer;
    }

    public String toString() {
        String name = (pkg == null || pkg.getName() == null || pkg.getName().equals(".")) ?
                typeName : pkg.getName() + ":" + typeName;

        if (!typeName.contains("$anon")) {
            return name;
        }

        StringJoiner sj = new StringJoiner(",\n\t", name + " {\n\t", "\n}");

        for (Entry<String, Field> field : getFields().entrySet()) {
            sj.add(field.getKey() + " : " + field.getValue().getFieldType());
        }

        for (MemberFunctionType func : attachedFunctions) {
            sj.add(func.toString());
        }

        return sj.toString();
    }

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    @Override
    public Type getImmutableType() {
        return this.immutableType;
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        this.immutableType = immutableType;
    }

    public void setTypeIdSet(BTypeIdSet typeIdSet) {
        this.typeIdSet = typeIdSet;
    }

    public void processObjectCtorAnnots(MapValue globalAnnotationMap, Strand strand) {
        AnnotationUtils.processObjectCtorAnnotations(globalAnnotationMap, this, strand);
    }

    public boolean hasAnnotations() {
        return !annotations.isEmpty();
    }
}

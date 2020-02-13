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
package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.BallerinaValues;

import java.util.Map.Entry;
import java.util.StringJoiner;

/**
 * {@code BObjectType} represents a user defined object type in Ballerina.
 *
 * @since 0.995.0
 */
public class BObjectType extends BStructureType {

    private AttachedFunction[] attachedFunctions;
    public AttachedFunction initializer;
    public AttachedFunction defaultsValuesInitFunc;

    /**
     * Create a {@code BObjectType} which represents the user defined struct type.
     *
     * @param typeName string name of the type
     * @param pkg package of the struct
     * @param flags flags of the object type
     */
    public BObjectType(String typeName, BPackage pkg, int flags) {
        super(typeName, pkg, flags, Object.class);
    }

    @Override
    public <V extends Object> V getZeroValue() {
        return (V) BallerinaValues.createObjectValue(this.pkg, this.typeName);
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

    public AttachedFunction[] getAttachedFunctions() {
        return attachedFunctions;
    }

    public void setAttachedFunctions(AttachedFunction[] attachedFunctions) {
        this.attachedFunctions = attachedFunctions;
    }

    public void setInitializer(AttachedFunction initializer) {
        this.initializer = initializer;
    }

    public String toString() {
        String name = (pkg == null || pkg.getName() == null || pkg.getName().equals(".")) ?
                typeName : pkg.getName() + ":" + typeName;
        
        if (!typeName.contains("$anon")) {
            return name;
        }

        StringJoiner sj = new StringJoiner(",\n\t", name + " {\n\t", "\n}");

        for (Entry<String, BField> field : getFields().entrySet()) {
            sj.add(field.getKey() + " : " + field.getValue().type);
        }

        for (AttachedFunction func : attachedFunctions) {
            sj.add(func.toString());
        }

        return sj.toString();
    }
}

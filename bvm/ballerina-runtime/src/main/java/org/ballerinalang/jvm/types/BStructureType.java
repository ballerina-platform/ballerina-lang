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

import java.util.HashMap;
import java.util.Map;

/**
 * {@code BStructureType} represents a user defined structure type in Ballerina.
 *
 * @since 0.995.0
 */
public abstract class BStructureType extends AnnotatableType {

    protected Map<String, BField> fields;
    public int flags;

    /**
     * Create a {@code BStructType} which represents the user defined struct type.
     *
     * @param typeName string name of the type
     * @param pkg package of the struct
     * @param flags of the structure type
     * @param valueClass of the structure type
     */
    public BStructureType(String typeName, BPackage pkg, int flags, Class<? extends Object> valueClass) {
        super(typeName, pkg, valueClass);
        this.flags = flags;
        fields = new HashMap<>();
    }

    /**
     * Create a {@code BStructType} which represents the user defined struct type.
     *
     * @param typeName string name of the type
     * @param pkg package of the type
     * @param flags of the structure type
     * @param valueClass of the structure type
     * @param fields structure fields
     */
    public BStructureType(String typeName, BPackage pkg, int flags, Class<? extends Object> valueClass,
            Map<String, BField> fields) {
        super(typeName, pkg, valueClass);
        this.flags = flags;
        this.fields = fields;
    }

    public Map<String, BField> getFields() {
        return fields;
    }

    public void setFields(Map<String, BField> fields) {
        this.fields = fields;
    }
}

/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.model.types;

import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.annotations.BallerinaPrimitive;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code BMapType} represents a map that maps keys to values in Ballerina.
 * <p>
 * Maps are defined using the map keyword as follows:
 * map mapName
 * <p>
 * All maps are unbounded in length and support key based indexing.
 *
 * @since 0.8.0
 */
@BallerinaPrimitive(type = "json",
        description = "Represents a map that maps keys to values in Ballerina. " +
                "Maps are defined using map keyword as follows: map mapName. " +
                "All maps are unbounded in length and support key based indexing.",
        defaultValue = "{}")
public class BMapType extends BType {

    /**
     * Creates an instance of {@code BMapType}.
     *
     * @param typeName string name of the type
     * @param pkgPath package path
     * @param symbolScope scope of the symbol
     */
    BMapType(String typeName, String pkgPath, SymbolScope symbolScope) {
        super(typeName, pkgPath, symbolScope, BMap.class);
    }

    @Override
    public <V extends BValue> V getDefaultValue() {
        return (V) new BMap<BString, V>();
    }
}

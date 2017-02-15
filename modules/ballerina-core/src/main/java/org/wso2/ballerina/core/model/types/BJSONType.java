/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model.types;

import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.annotations.BallerinaPrimitive;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code BJSONType} represents a JSON document.
 *
 * @since 0.8.0
 */
@BallerinaPrimitive(type = "json",
        description = "Represents a JSON document.",
        defaultValue = "{}",
        usage = "json [variable] = `[json-document]`")
public class BJSONType extends BType {

    /**
     * Creates an instance of {@code BJSONType}.
     *
     * @param typeName string name of the type
     * @param pkgPath package path
     * @param symbolScope scope of the symbol
     */
    BJSONType(String typeName, String pkgPath, SymbolScope symbolScope) {
        super(typeName, pkgPath, symbolScope, BJSON.class);
    }

    @SuppressWarnings("unchecked")
    public <V extends BValue> V getDefaultValue() {
        return (V) new BJSON("{}");
    }
}

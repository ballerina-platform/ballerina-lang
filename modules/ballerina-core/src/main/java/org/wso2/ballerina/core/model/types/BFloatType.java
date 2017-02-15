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
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@code BFloatType} represents a 32-bit floating-point number according to the
 * standard IEEE 754 specifications.
 *
 * @since 0.8.0
 */
class BFloatType extends BType {

    /**
     * Creates an instance of {@code BFloatType}.
     *
     * @param typeName string name of the type
     * @param pkgPath package path
     * @param symbolScope scope of the symbol
     */
    BFloatType(String typeName, String pkgPath, SymbolScope symbolScope) {
        super(typeName, pkgPath, symbolScope, BFloat.class);
    }

    @SuppressWarnings("unchecked")
    public <V extends BValue> V getDefaultValue() {
        return (V) new BFloat(0);
    }
}

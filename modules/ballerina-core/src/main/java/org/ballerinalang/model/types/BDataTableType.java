/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.model.types;

import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.values.BDataTable;
import org.ballerinalang.model.values.BValue;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * {@code BDataTableType} represents a output data set of a SQL select query in Ballerina.
 *
 * @since 0.8.0
 */
public class BDataTableType extends BType {

    /**
     * Create a {@code BDataTableType} which represents the SQL Result Set.
     *
     * @param typeName   string name of the type
     */
    BDataTableType(String typeName, String pkgPath, SymbolScope symbolScope) {
        super(typeName, pkgPath, symbolScope, BDataTable.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends BValue> V getDefaultValue() {
        return (V) new BDataTable(null, new HashMap<>(0), new ArrayList<>(0));
    }
}

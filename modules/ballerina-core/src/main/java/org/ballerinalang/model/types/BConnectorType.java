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
package org.ballerinalang.model.types;

import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BValue;

/**
 * {@code BConnectorType} represents a {@code Connector} in Ballerina.
 *
 * @since 0.8.0
 */
public class BConnectorType extends BType {

    /**
     * Create a {@code BConnectorType} which represents the Ballerina Connector type.
     *
     * @param typeName    string name of the type
     * @param pkgPath     package of the connector
     * @param symbolScope symbol scope of the connector
     */
    public BConnectorType(String typeName, String pkgPath, SymbolScope symbolScope) {
        super(typeName, pkgPath, symbolScope, BConnector.class);
    }

    /**
     * Create a {@code BConnectorType} which represents the Ballerina Connector type.
     *
     * @param typeName    string name of the type
     * @param pkgPath     package of the connector
     */
    public BConnectorType(String typeName, String pkgPath) {
        super(typeName, pkgPath, null, BConnector.class);
    }

    @Override
    public <V extends BValue> V getZeroValue() {
        return null;
    }

    @Override
    public <V extends BValue> V getEmptyValue() {
        return (V) new BConnector(this);
    }

    @Override
    public TypeSignature getSig() {
        String packagePath = (pkgPath == null) ? "." : pkgPath;
        return new TypeSignature(TypeSignature.SIG_CONNECTOR, packagePath, typeName);
    }

    @Override
    public int getTag() {
        return TypeTags.CONNECTOR_TAG;
    }
}


/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.model.values;

import org.ballerinalang.model.Connector;

/**
 * The {@code BConnector} represents a Connector in Ballerina.
 *
 * @since 0.8.0
 */
public final class BConnector implements BRefType<Connector> {

    private Connector connector;
    private BValue[] connectorMemBlock;

    public BConnector() {
        this(null, new BValue[0]);
    }

    public BConnector(Connector connector, BValue[] connectorMemBlock) {
        this.connector = connector;
        this.connectorMemBlock = connectorMemBlock;
    }

    public BValue getValue(int offset) {
        return connectorMemBlock[offset];
    }

    public void setValue(int offset, BValue bValue) {
        this.connectorMemBlock[offset] = bValue;
    }

    @Override
    public Connector value() {
        return connector;
    }

    @Override
    public String stringValue() {
        return null;
    }
}

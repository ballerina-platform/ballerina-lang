/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.central.client.model.connector;

import java.util.List;

/**
 * {@code ConnectorJsonSchema} represents package search result from central.
 */
public class BalConnectorSearchResult {

    private List<BalConnector> connectors;
    private int count;
    private int offset;
    private int limit;

    public BalConnectorSearchResult() {
    }

    public BalConnectorSearchResult(List<BalConnector> connectors, int count, int offset, int limit) {
        this.connectors = connectors;
        this.count = count;
        this.offset = offset;
        this.limit = limit;
    }

    public List<BalConnector> getConnectors() {
        return connectors;
    }

    public void setConnectors(List<BalConnector> connectors) {
        this.connectors = connectors;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}

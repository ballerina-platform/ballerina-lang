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

package org.ballerinalang.central.client;

import org.ballerinalang.central.client.model.connector.BalConnector;
import org.ballerinalang.central.client.model.connector.BalConnectorSearchResult;
import org.ballerinalang.central.client.model.connector.BalFunction;
import org.ballerinalang.central.client.model.connector.BalType;
import org.ballerinalang.central.client.model.connector.BalTypeInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Test cases to test connector models.
 */
public class TestConnectorModels {

    private final String connectorName = "TestConnector";
    private final String orgName = "testOrg";
    private final String moduleName = "testModule";
    private final String packageName = "testPackage";
    private final String version = "0.1.0";
    private final String documentation = "test documentation";
    private final String recordType = "record";
    private final String initFunc = "init";
    private BalTypeInfo typeInfo;
    private BalType type;
    private BalFunction function;
    private BalConnector connector;
    private BalConnectorSearchResult searchResult;

    @BeforeClass
    public void initConnectorModels() {
        typeInfo = new BalTypeInfo(connectorName, orgName, moduleName, packageName, version);

        type = new BalType();
        type.typeName = recordType;
        type.optional = false;
        type.typeInfo = this.typeInfo;

        List<BalType> parameters = Arrays.asList(type);
        function = new BalFunction(initFunc, parameters, type, null, false, documentation);

        List<BalFunction> functions = Arrays.asList(function);
        connector = new BalConnector(orgName, moduleName, packageName, version, connectorName,
                documentation, null, functions);

        List<BalConnector> connectors = Arrays.asList(connector);
        searchResult = new BalConnectorSearchResult(connectors, 1, 0, 10);
    }


    @Test(description = "Test ballerina typeInfo object")
    public void getBalTypeInfo() {
        Assert.assertEquals(typeInfo.orgName, orgName);
        Assert.assertEquals(typeInfo.moduleName, moduleName);
        Assert.assertEquals(typeInfo.packageName, packageName);
        Assert.assertEquals(typeInfo.version, version);
    }

    @Test(description = "Test ballerina type object")
    public void getBalType() {
        Assert.assertEquals(type.typeName, recordType);
        Assert.assertEquals(type.optional, false);
        Assert.assertEquals(type.typeInfo.packageName, packageName);
    }

    @Test(description = "Test ballerina function object")
    public void getBalFunction() {
        Assert.assertEquals(function.name, initFunc);
        Assert.assertEquals(function.parameters.size(), 1);
        Assert.assertEquals(function.parameters.get(0).typeName, recordType);
        Assert.assertEquals(function.returnType.optional, false);
        Assert.assertEquals(function.documentation, documentation);
    }

    @Test(description = "Test ballerina connector object")
    public void getBalConnector() {
        Assert.assertEquals(connector.orgName, orgName);
        Assert.assertEquals(connector.moduleName, moduleName);
        Assert.assertEquals(connector.packageName, packageName);
        Assert.assertEquals(connector.version, version);
        Assert.assertEquals(connector.functions.size(), 1);
        Assert.assertEquals(connector.functions.get(0).name, initFunc);
    }

    @Test(description = "Test ballerina connector search results object")
    public void getBalConnectorSearchResult() {
        Assert.assertEquals(searchResult.getConnectors().size(), 1);
        Assert.assertEquals(searchResult.getConnectors().get(0).packageName, packageName);
        Assert.assertEquals(searchResult.getConnectors().get(0).documentation, documentation);
        Assert.assertEquals(searchResult.getCount(), 1);
        Assert.assertEquals(searchResult.getLimit(), 10);
    }
}

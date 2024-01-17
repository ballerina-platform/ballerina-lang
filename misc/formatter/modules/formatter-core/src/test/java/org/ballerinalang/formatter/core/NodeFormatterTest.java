/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.formatter.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Tests for nodes and partial nodes.
 *
 * @since 2201.4.1
 */
public class NodeFormatterTest {

    private static final String TEST_ROOT = "src/test/resources/nodes/";
    private static final String EXPRESSION_NODE_TESTCASE = TEST_ROOT + "expressionNodes.json";

    private static final String JSON_ATTRIBUTE_SOURCE_CODE = "source";
    private static final String JSON_ATTRIBUTE_EXPECTED_CODE = "expected";

    @Test(dataProvider = "nodeFormatterDataProvider")
    public void testExpressionNodes(JsonElement testData) throws FormatterException {
        JsonObject testDataObject = (JsonObject) testData;
        String source = testDataObject.get(JSON_ATTRIBUTE_SOURCE_CODE).getAsString();
        String expected = testDataObject.get(JSON_ATTRIBUTE_EXPECTED_CODE).getAsString();
        String formatted = Formatter.formatExpression(source);
        Assert.assertEquals(formatted, expected);
    }

    @DataProvider(name = "nodeFormatterDataProvider")
    public Object[] dataProvider(Method method) throws IOException, FormatterException {
        String filePath = null;
        if ("testExpressionNodes".equals(method.getName())) {
            filePath = EXPRESSION_NODE_TESTCASE;
        }

        if (filePath == null) {
            throw new FormatterException("Failed to load dataset for method: " + method.getName());
        }

        FileReader reader = new FileReader(filePath);
        Object testCaseObject = JsonParser.parseReader(reader);
        JsonArray fileData = (JsonArray) testCaseObject;
        List<JsonElement> elementList = new LinkedList<>();
        fileData.forEach(elementList::add);
        return elementList.toArray();
    }
}

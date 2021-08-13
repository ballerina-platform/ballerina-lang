/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.debugger.test.adapter.variables;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.Variable;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for variable paging scenarios.
 */
public class VariableQueryTest extends BaseTestCase {

    Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo;
    Map<String, Variable> localVariables = new HashMap<>();
    DebugTestRunner debugTestRunner;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        String testProjectName = "variable-query-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);

        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 38));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        localVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), DebugTestRunner.VariableScope.LOCAL);
    }

    @Test
    public void arrayVariableQueryTest() throws BallerinaTestException {
        // array child variable query test
        Map<String, Variable> arrayChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("arrVar"));
        Assert.assertEquals(arrayChildVariables.size(), 1000);
        debugTestRunner.assertVariable(arrayChildVariables, "[0]", "1", "int");
        debugTestRunner.assertVariable(arrayChildVariables, "[999]", "1000", "int");

        // array child variable query test with start = 0 and count = 0
        arrayChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("arrVar"), 0, 0);
        Assert.assertEquals(arrayChildVariables.size(), 1000);
        debugTestRunner.assertVariable(arrayChildVariables, "[0]", "1", "int");
        debugTestRunner.assertVariable(arrayChildVariables, "[999]", "1000", "int");

        // array child variable query test with start = 200 and count = 100
        arrayChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("arrVar"), 200, 100);
        Assert.assertEquals(arrayChildVariables.size(), 100);
        debugTestRunner.assertVariable(arrayChildVariables, "[200]", "201", "int");
        debugTestRunner.assertVariable(arrayChildVariables, "[299]", "300", "int");
    }

    @Test
    public void jsonVariableQueryTest() throws BallerinaTestException {
        // json child variable query test
        Map<String, Variable> jsonChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("jsonVar"));
        Assert.assertEquals(jsonChildVariables.size(), 1000);
        debugTestRunner.assertVariable(jsonChildVariables, "[0]", "1", "int");
        debugTestRunner.assertVariable(jsonChildVariables, "[999]", "1000", "int");

        // json child variable query test with start = 0 and count = 0
        jsonChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("jsonVar"), 0, 0);
        Assert.assertEquals(jsonChildVariables.size(), 1000);
        debugTestRunner.assertVariable(jsonChildVariables, "[0]", "1", "int");
        debugTestRunner.assertVariable(jsonChildVariables, "[999]", "1000", "int");

        // json child variable query test with start = 300 and count = 100
        jsonChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("jsonVar"), 300, 100);
        Assert.assertEquals(jsonChildVariables.size(), 100);
        debugTestRunner.assertVariable(jsonChildVariables, "[300]", "301", "int");
        debugTestRunner.assertVariable(jsonChildVariables, "[399]", "400", "int");
    }

    @Test
    public void mapVariableQueryTest() throws BallerinaTestException {
        // map child variable query test
        Map<String, Variable> mapChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("mapVar"));
        Assert.assertEquals(mapChildVariables.size(), 1000);
        debugTestRunner.assertVariable(mapChildVariables, "0", "1", "int");
        debugTestRunner.assertVariable(mapChildVariables, "999", "1000", "int");

        // map child variable query test with start = 0 and count = 0
        mapChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("mapVar"), 0, 0);
        Assert.assertEquals(mapChildVariables.size(), 1000);
        debugTestRunner.assertVariable(mapChildVariables, "0", "1", "int");
        debugTestRunner.assertVariable(mapChildVariables, "999", "1000", "int");

        // map child variable query test with start = 400 and count = 100
        mapChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("mapVar"), 400, 100);
        Assert.assertEquals(mapChildVariables.size(), 100);
        debugTestRunner.assertVariable(mapChildVariables, "400", "401", "int");
        debugTestRunner.assertVariable(mapChildVariables, "499", "500", "int");
    }

    @Test
    public void tableVariableQueryTest() throws BallerinaTestException {
        // table child variable query test
        Map<String, Variable> tableChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("tableVar"));
        Assert.assertEquals(tableChildVariables.size(), 1000);
        Map<String, Variable> tableGrandChildrenVariables =
                debugTestRunner.fetchChildVariables(tableChildVariables.get("[0]"));
        debugTestRunner.assertVariable(tableGrandChildrenVariables, "id", "1", "int");
        tableGrandChildrenVariables = debugTestRunner.fetchChildVariables(tableChildVariables.get("[999]"));
        debugTestRunner.assertVariable(tableGrandChildrenVariables, "id", "1000", "int");

        // table child variable query test with start = 0 and count = 0
        tableChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("tableVar"), 0, 0);
        Assert.assertEquals(tableChildVariables.size(), 1000);
        tableGrandChildrenVariables =
                debugTestRunner.fetchChildVariables(tableChildVariables.get("[0]"));
        debugTestRunner.assertVariable(tableGrandChildrenVariables, "id", "1", "int");
        tableGrandChildrenVariables =
                debugTestRunner.fetchChildVariables(tableChildVariables.get("[999]"));
        debugTestRunner.assertVariable(tableGrandChildrenVariables, "id", "1000", "int");

        // table child variable query test with start = 600 and count = 100
        tableChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("tableVar"), 600, 100);
        Assert.assertEquals(tableChildVariables.size(), 100);
        tableGrandChildrenVariables = debugTestRunner.fetchChildVariables(tableChildVariables.get("[600]"));
        debugTestRunner.assertVariable(tableGrandChildrenVariables, "id", "601", "int");
        tableGrandChildrenVariables = debugTestRunner.fetchChildVariables(tableChildVariables.get("[699]"));
        debugTestRunner.assertVariable(tableGrandChildrenVariables, "id", "700", "int");
    }

    @Test
    public void tupleVariableQueryTest() throws BallerinaTestException {
        // tuple child variable query test
        Map<String, Variable> tupleChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("tupleVar"));
        Assert.assertEquals(tupleChildVariables.size(), 1000);
        debugTestRunner.assertVariable(tupleChildVariables, "[0]", "1", "int");
        debugTestRunner.assertVariable(tupleChildVariables, "[999]", "1000", "int");

        // tuple child variable query test with start = 0 and count = 0
        tupleChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("tupleVar"), 0, 0);
        Assert.assertEquals(tupleChildVariables.size(), 1000);
        debugTestRunner.assertVariable(tupleChildVariables, "[0]", "1", "int");
        debugTestRunner.assertVariable(tupleChildVariables, "[999]", "1000", "int");

        // tuple child variable query test with start = 700 and count = 100
        tupleChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("tupleVar"), 700, 100);
        Assert.assertEquals(tupleChildVariables.size(), 100);
        debugTestRunner.assertVariable(tupleChildVariables, "[700]", "701", "int");
        debugTestRunner.assertVariable(tupleChildVariables, "[799]", "800", "int");
    }

    @Test
    public void xmlVariableQueryTest() throws BallerinaTestException {
        // xml child variable query test
        Map<String, Variable> xmlChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("xmlVar"));
        Assert.assertEquals(xmlChildVariables.size(), 1000);
        Map<String, Variable> xmlGrandChildrenVariables =
                debugTestRunner.fetchChildVariables(xmlChildVariables.get("[0]"));
        xmlGrandChildrenVariables = debugTestRunner.fetchChildVariables(xmlGrandChildrenVariables.get("children"));
        debugTestRunner.assertVariable(xmlGrandChildrenVariables, "[0]", "1", "xml");

        xmlGrandChildrenVariables = debugTestRunner.fetchChildVariables(xmlChildVariables.get("[999]"));
        xmlGrandChildrenVariables = debugTestRunner.fetchChildVariables(xmlGrandChildrenVariables.get("children"));
        debugTestRunner.assertVariable(xmlGrandChildrenVariables, "[0]", "1000", "xml");

        // xml child variable query test with start = 0 and count = 0
        xmlChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("xmlVar"), 0, 0);
        Assert.assertEquals(xmlChildVariables.size(), 1000);
        xmlGrandChildrenVariables = debugTestRunner.fetchChildVariables(xmlChildVariables.get("[0]"));
        xmlGrandChildrenVariables = debugTestRunner.fetchChildVariables(xmlGrandChildrenVariables.get("children"));
        debugTestRunner.assertVariable(xmlGrandChildrenVariables, "[0]", "1", "xml");

        xmlGrandChildrenVariables = debugTestRunner.fetchChildVariables(xmlChildVariables.get("[999]"));
        xmlGrandChildrenVariables = debugTestRunner.fetchChildVariables(xmlGrandChildrenVariables.get("children"));
        debugTestRunner.assertVariable(xmlGrandChildrenVariables, "[0]", "1000", "xml");

        // xml child variable query test with start = 800 and count = 100
        xmlChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("xmlVar"), 0, 0);
        Assert.assertEquals(xmlChildVariables.size(), 1000);
        xmlGrandChildrenVariables = debugTestRunner.fetchChildVariables(xmlChildVariables.get("[800]"));
        xmlGrandChildrenVariables = debugTestRunner.fetchChildVariables(xmlGrandChildrenVariables.get("children"));
        debugTestRunner.assertVariable(xmlGrandChildrenVariables, "[0]", "801", "xml");

        xmlGrandChildrenVariables = debugTestRunner.fetchChildVariables(xmlChildVariables.get("[899]"));
        xmlGrandChildrenVariables = debugTestRunner.fetchChildVariables(xmlGrandChildrenVariables.get("children"));
        debugTestRunner.assertVariable(xmlGrandChildrenVariables, "[0]", "900", "xml");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
    }
}

/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test Cases for ballerina.model.map.
 */
public class MapTest {

    private static BMap<String, BValue> dataSet;
    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("samples/mapTest.bal");
    }

    @Test
    public void testLength() {
        dataSet = new BMap<>();
        dataSet.put("country", new BString("US"));
        dataSet.put("currency", new BString("Dollar"));
        dataSet.put("states", new BInteger(50));
        BValue[] args = {dataSet};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testGetKeys() {
        dataSet = new BMap<>();
        dataSet.put("country", new BString("US"));
        dataSet.put("currency", new BString("Dollar"));
        dataSet.put("states", new BInteger(50));
        BValue[] args = {dataSet};
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testGetKeys", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BStringArray);
        BStringArray stringArray = (BStringArray) returnVals[0];
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < stringArray.size(); i++) {
            keys.add(stringArray.get(i));
        }
        Assert.assertTrue(keys.contains("country"), "Element didn't match");
        Assert.assertTrue(keys.contains("currency"), "Element didn't match");
        Assert.assertTrue(keys.contains("states"), "Element didn't match");
    }

    @Test
    public void testRemove() {
        dataSet = new BMap<>();
        dataSet.put("country", new BString("US"));
        dataSet.put("currency", new BString("Dollar"));
        dataSet.put("states", new BInteger(50));
        BValue[] args = {dataSet, new BString("country")};
        BLangFunctions.invokeNew(programFile, "testRemove", args);
        Assert.assertTrue(dataSet.size() == 2);
        Assert.assertFalse(dataSet.keySet().contains("country"), "Element still exits");
    }

    @Test
    public void testDefinition() {
        BValue[] returnVals = BLangFunctions.invokeNew(programFile, "testDefinition");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertTrue(((BBoolean) returnVals[0]).booleanValue(), "Test Failed. Reason: " +
                returnVals[1].stringValue());
    }

}

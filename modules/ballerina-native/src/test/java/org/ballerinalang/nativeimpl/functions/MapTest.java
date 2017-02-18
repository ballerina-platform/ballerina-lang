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

import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
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

    private static BMap<BString, BValue> dataSet;
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/mapTest.bal");
        dataSet = new BMap<>();
        dataSet.put(new BString("country"), new BString("US"));
        dataSet.put(new BString("currency"), new BString("Dollar"));
        dataSet.put(new BString("states"), new BInteger(50));
    }

    @Test
    public void testLength() {
        BValue[] args = {dataSet};
        BValue[] returnVals = BLangFunctions.invoke(bLangProgram, "testLength", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnVals[0]).intValue(), 3, "Length didn't match");
    }

    @Test
    public void testGetKeys() {
        BValue[] args = {dataSet};
        BValue[] returnVals = BLangFunctions.invoke(bLangProgram, "testGetKeys", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values.");
        Assert.assertTrue(returnVals[0] instanceof BArray);
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < ((BArray) returnVals[0]).size(); i++) {
            keys.add(((BArray) returnVals[0]).get(i).stringValue());
        }
        Assert.assertTrue(keys.contains("country"), "Element didn't match");
        Assert.assertTrue(keys.contains("currency"), "Element didn't match");
        Assert.assertTrue(keys.contains("states"), "Element didn't match");
    }

    @Test
    public void testRemove() {
        BValue[] args = {dataSet, new BString("country")};
        BLangFunctions.invoke(bLangProgram, "testRemove", args);
        Assert.assertTrue(dataSet.size() == 2);
        Assert.assertFalse(dataSet.keySet().contains("country"), "Element still exits");
    }

    @Test
    public void testDefinition() {
        BValue[] returnVals = BLangFunctions.invoke(bLangProgram, "testDefinition");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values.");
        Assert.assertTrue(((BBoolean) returnVals[0]).booleanValue(), "Test Failed. Reason: " +
                returnVals[1].stringValue());
    }

}

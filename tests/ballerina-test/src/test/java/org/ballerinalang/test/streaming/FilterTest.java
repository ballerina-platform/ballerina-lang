/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.streaming;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test filter behaviour of Ballerina Streaming.
 *
 * @since 0.965.0
 */
public class FilterTest {

    private CompileResult result;
    private CompileResult resultWithReference;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/streaming/filter-streaming-test-x.bal");
        //   resultWithReference = BCompileUtil.compile("test-src/streaming/filter-streaming-with-reference-test.bal");
    }

    @Test(description = "Test filter streaming query")
    public void testFilterQuery() {
        BValue[] avgAges = BRunUtil.invoke(result, "startFilterQuery");
        Assert.assertNotNull(avgAges);

        Assert.assertEquals(avgAges.length, 10, "Expected events are not received");

        BStruct avgAge0 = (BStruct) avgAges[0];
        BStruct avgAge1 = (BStruct) avgAges[1];
        BStruct avgAge2 = (BStruct) avgAges[2];
        BStruct avgAge3 = (BStruct) avgAges[3];
        BStruct avgAge4 = (BStruct) avgAges[4];
        BStruct avgAge5 = (BStruct) avgAges[5];
        BStruct avgAge6 = (BStruct) avgAges[6];
        BStruct avgAge7 = (BStruct) avgAges[7];
        BStruct avgAge8 = (BStruct) avgAges[8];
        BStruct avgAge9 = (BStruct) avgAges[9];

        Assert.assertEquals(avgAge0.getFloatField(0), 25.0);
        Assert.assertEquals(avgAge1.getFloatField(0), 29.0);
        Assert.assertEquals(avgAge2.getFloatField(0), 34.0);
        Assert.assertEquals(avgAge3.getFloatField(0), 34.0);
        Assert.assertEquals(avgAge4.getFloatField(0), 40.0);
        Assert.assertEquals(avgAge5.getFloatField(0), 43.0);
        Assert.assertEquals(avgAge6.getFloatField(0), 45.4);
        Assert.assertEquals(avgAge7.getFloatField(0), 48.6);
        Assert.assertEquals(avgAge8.getFloatField(0), 47.8);
        Assert.assertEquals(avgAge9.getFloatField(0), 40.0);
    }


    @Test(description = "Test filter streaming query", enabled = false)
    public void testFilterQueryWithFuntionParam() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultWithReference, "startFilterQuery");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 3, "Expected events are not received");

        BStruct employee0 = (BStruct) outputEmployeeEvents[0];
        BStruct employee1 = (BStruct) outputEmployeeEvents[1];

        Assert.assertEquals(employee0.getIntField(0), 33);
        Assert.assertEquals(employee1.getIntField(0), 45);
    }

}

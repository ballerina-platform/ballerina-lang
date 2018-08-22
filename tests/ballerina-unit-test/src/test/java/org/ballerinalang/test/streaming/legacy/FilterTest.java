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
package org.ballerinalang.test.streaming.legacy;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
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
        result = BCompileUtil.compile("test-src/streaming/legacy/filter-streaming-test.bal");
        resultWithReference = BCompileUtil.
                compile("test-src/streaming/legacy/filter-streaming-with-reference-test.bal");
    }

    @Test(description = "Test filter streaming query")
    public void testFilterQuery() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(result, "startFilterQuery");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 33);
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 45);
    }


    @Test(description = "Test filter streaming query")
    public void testFilterQueryWithFuntionParam() {
        BValue[] outputEmployeeEvents = BRunUtil.invoke(resultWithReference, "startFilterQuery");
        Assert.assertNotNull(outputEmployeeEvents);

        Assert.assertEquals(outputEmployeeEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> employee0 = (BMap<String, BValue>) outputEmployeeEvents[0];
        BMap<String, BValue> employee1 = (BMap<String, BValue>) outputEmployeeEvents[1];

        Assert.assertEquals(((BInteger) employee0.get("age")).intValue(), 33);
        Assert.assertEquals(((BInteger) employee1.get("age")).intValue(), 45);
    }

}

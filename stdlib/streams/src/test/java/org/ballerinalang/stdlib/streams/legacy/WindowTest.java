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
package org.ballerinalang.stdlib.streams.legacy;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.tool.util.BCompileUtil;
import org.ballerinalang.tool.util.BRunUtil;
import org.ballerinalang.tool.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test window behaviour of Ballerina Streaming.
 *
 * @since 0.965.0
 */
public class WindowTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/legacy/window-streaming-test.bal", true);
    }

    @Test(description = "Test window streaming query with groupby and aggregation function.")
    public void testWindowQuery() {
        BValue[] outputStatusCountArray = BRunUtil.invoke(result, "startWindowQuery");
        Assert.assertNotNull(outputStatusCountArray);

        Assert.assertEquals(outputStatusCountArray.length, 2, "Expected events are not received");

        BMap<String, BValue> statusCount0 = (BMap<String, BValue>) outputStatusCountArray[0];
        BMap<String, BValue> statusCount1 = (BMap<String, BValue>) outputStatusCountArray[1];

        Assert.assertEquals(((BInteger) statusCount0.get("totalCount")).intValue(), 2);
        Assert.assertEquals(((BInteger) statusCount1.get("totalCount")).intValue(), 1);
    }

}

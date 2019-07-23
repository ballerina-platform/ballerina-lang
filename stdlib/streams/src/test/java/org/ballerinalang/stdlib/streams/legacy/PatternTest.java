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

import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.tool.util.BCompileUtil;
import org.ballerinalang.tool.util.BRunUtil;
import org.ballerinalang.tool.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test patterns of Ballerina Streaming.
 *
 * @since 0.965.0
 */
public class PatternTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/legacy/pattern-streaming-test.bal", true);
    }

    @Test(description = "Test pattern streaming query with followed by pattern")
    public void testPatternQuery1() {
        BValue[] tempDifferences = BRunUtil.invoke(result, "runPatternQuery1");
        Assert.assertNotNull(tempDifferences);
        Assert.assertEquals(tempDifferences.length, 1);
        BMap<String, BValue> tempDifference = (BMap<String, BValue>) tempDifferences[0];
        Assert.assertEquals(((BFloat) tempDifference.get("tempDifference")).floatValue(), 7.0);
    }

    @Test(description = "Test pattern streaming query with 'Or'")
    public void testPatternQuery2() {
        BValue[] roomActions = BRunUtil.invoke(result, "runPatternQuery2");
        Assert.assertNotNull(roomActions);

        BMap<String, BValue> tempDifference = (BMap<String, BValue>) roomActions[0];
        Assert.assertEquals(tempDifference.get("userAction").stringValue(), "stop");
    }

    @Test(enabled = false, description = "Test pattern streaming query with 'And'")
    public void testPatternQuery3() {
        BValue[] roomActions = BRunUtil.invoke(result, "runPatternQuery3");
        Assert.assertNotNull(roomActions);

        BMap<String, BValue> tempDifference = (BMap<String, BValue>) roomActions[0];
        Assert.assertEquals(tempDifference.get("userAction").stringValue(), "RoomClosedWithRegulatorOff");
    }

    @Test(enabled = false, description = "Test pattern streaming query with 'Not' and 'And'")
    public void testPatternQuery4() {
        BValue[] roomActions = BRunUtil.invoke(result, "runPatternQuery4");
        Assert.assertNotNull(roomActions);

        BMap<String, BValue> tempDifference = (BMap<String, BValue>) roomActions[0];
        Assert.assertEquals(tempDifference.get("userAction").stringValue(), "RoomNotClosedWithRegulatorNotOff");
    }

    @Test(description = "Test pattern streaming query with 'Not' and 'For'")
    public void testPatternQuery5() {
        BValue[] roomActions = BRunUtil.invoke(result, "runPatternQuery5");
        Assert.assertNotNull(roomActions);

        BMap<String, BValue> tempDifference = (BMap<String, BValue>) roomActions[0];
        Assert.assertEquals(tempDifference.get("userAction").stringValue(), "CloseRoomAfter2Sec");
    }

    @Test(description = "Test pattern streaming query with within clause")
    public void testPatternQuery6() {
        BValue[] tempDifferences = BRunUtil.invoke(result, "runPatternQuery6");
        Assert.assertNotNull(tempDifferences);
        Assert.assertEquals(0, tempDifferences.length);
    }
}

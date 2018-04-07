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
 * This contains methods to test patterns of Ballerina Streaming.
 *
 * @since 0.965.0
 */
public class PatternTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/streaming/pattern-streaming-test.bal");
    }

    @Test(description = "Test pattern streaming query with followed by pattern")
    public void testPatternQuery1() {
        BValue[] tempDifferences = BRunUtil.invoke(result, "runPatternQuery1");

        Assert.assertNotNull(tempDifferences);

        BStruct tempDifference = (BStruct) tempDifferences[0];
        Assert.assertEquals(tempDifference.getFloatField(0), 7.0);
    }

    @Test(description = "Test pattern streaming query with 'Or'")
    public void testPatternQuery2() {
        BValue[] roomActions = BRunUtil.invoke(result, "runPatternQuery2");

        Assert.assertNotNull(roomActions);

        BStruct tempDifference = (BStruct) roomActions[0];
        Assert.assertEquals(tempDifference.getStringField(0), "stop");
    }

    @Test(description = "Test pattern streaming query with 'And'")
    public void testPatternQuery3() {
        BValue[] roomActions = BRunUtil.invoke(result, "runPatternQuery3");

        Assert.assertNotNull(roomActions);

        BStruct tempDifference = (BStruct) roomActions[0];
        Assert.assertEquals(tempDifference.getStringField(0), "RoomClosedWithRegulatorOff");
    }

    @Test(description = "Test pattern streaming query with 'Not' and 'And'")
    public void testPatternQuery4() {
        BValue[] roomActions = BRunUtil.invoke(result, "runPatternQuery4");

        Assert.assertNotNull(roomActions);

        BStruct tempDifference = (BStruct) roomActions[0];
        Assert.assertEquals(tempDifference.getStringField(0), "RoomNotClosedWithRegulatorNotOff");
    }

    @Test(description = "Test pattern streaming query with 'Not' and 'For'")
    public void testPatternQuery5() {
        BValue[] roomActions = BRunUtil.invoke(result, "runPatternQuery5");

        Assert.assertNotNull(roomActions);

        BStruct tempDifference = (BStruct) roomActions[0];
        Assert.assertEquals(tempDifference.getStringField(0), "CloseRoomAfter2Sec");
    }
}


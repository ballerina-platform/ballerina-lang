/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.streams.nativeimpl;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test native not pattern query behaviour in Ballerina Streaming V2.
 *
 * @since 0.995.0
 */
public class BallerinaStreamsV2NotPatternTest {

    private CompileResult notPatternTests;

    @BeforeClass
    public void setup() {
        notPatternTests = BCompileUtil.compile("test-src/native/streamingv2-native-simple-not-pattern-test.bal");
    }

    @Test(description = "Test (A -> not B for 2 sec) pattern query.", enabled = false)
    public void runPatternQuery1() {
        BValue[] outputEvents = BRunUtil.invoke(notPatternTests, "runPatternQuery1");
        Assert.assertNotNull(outputEvents);
        Assert.assertEquals(outputEvents.length, 1);
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("aId"), new BInteger(11));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("bId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("cId"), new BInteger(0));
    }

    @Test(description = "Test (A -> not B and C) pattern query.", enabled = false)
    public void runPatternQuery2() {
        BValue[] outputEvents = BRunUtil.invoke(notPatternTests, "runPatternQuery2");
        Assert.assertNotNull(outputEvents);
        Assert.assertEquals(outputEvents.length, 1);
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("aId"), new BInteger(11));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("bId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("cId"), new BInteger(31));
    }

    @Test(description = "Test (A -> not B for 2 sec and C) pattern query.", enabled = false)
    public void runPatternQuery3() {
        BValue[] outputEvents = BRunUtil.invoke(notPatternTests, "runPatternQuery3");
        Assert.assertNotNull(outputEvents);
        Assert.assertEquals(outputEvents.length, 1);
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("aId"), new BInteger(11));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("bId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("cId"), new BInteger(31));
    }

    @Test(description = "Test (A -> not B for 2 sec or C) pattern query.", enabled = false)
    public void runPatternQuery4() {
        BValue[] outputEvents = BRunUtil.invoke(notPatternTests, "runPatternQuery4");
        Assert.assertNotNull(outputEvents);
        Assert.assertEquals(outputEvents.length, 2);
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("aId"), new BInteger(11));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("bId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("cId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[1]).getMap().get("aId"), new BInteger(12));
        Assert.assertEquals(((BMap) outputEvents[1]).getMap().get("bId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[1]).getMap().get("cId"), new BInteger(0));
    }

    @Test(description = "Test (A -> not B for 2 sec and not C for 2 sec) pattern query.", enabled = false)
    public void runPatternQuery5() {
        BValue[] outputEvents = BRunUtil.invoke(notPatternTests, "runPatternQuery5");
        Assert.assertNotNull(outputEvents);
        Assert.assertEquals(outputEvents.length, 1);
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("aId"), new BInteger(11));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("bId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("cId"), new BInteger(0));
    }

    @Test(description = "Test (A -> not B for 2 sec or not C for 2 sec) pattern query.", enabled = false)
    public void runPatternQuery6() {
        BValue[] outputEvents = BRunUtil.invoke(notPatternTests, "runPatternQuery6");
        Assert.assertNotNull(outputEvents);
        Assert.assertEquals(outputEvents.length, 1);
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("aId"), new BInteger(11));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("bId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("cId"), new BInteger(0));
    }

    @Test(description = "Test (not A for 2 sec -> B) pattern query.", enabled = false)
    public void runPatternQuery7() {
        BValue[] outputEvents = BRunUtil.invoke(notPatternTests, "runPatternQuery7");
        Assert.assertNotNull(outputEvents);
        Assert.assertEquals(outputEvents.length, 1);
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("aId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("bId"), new BInteger(22));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("cId"), new BInteger(0));
    }

    @Test(description = "Test (not A and B -> C) pattern query.", enabled = false)
    public void runPatternQuery8() {
        BValue[] outputEvents = BRunUtil.invoke(notPatternTests, "runPatternQuery8");
        Assert.assertNotNull(outputEvents);
        Assert.assertEquals(outputEvents.length, 1);
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("aId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("bId"), new BInteger(21));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("cId"), new BInteger(31));
    }

    @Test(description = "Test (not A for 2 sec and B -> C) pattern query.", enabled = false)
    public void runPatternQuery9() {
        BValue[] outputEvents = BRunUtil.invoke(notPatternTests, "runPatternQuery9");
        Assert.assertNotNull(outputEvents);
        Assert.assertEquals(outputEvents.length, 1);
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("aId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("bId"), new BInteger(21));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("cId"), new BInteger(31));
    }

    @Test(description = "Test (not A for 2 sec and not B for 2 sec -> C) pattern query.", enabled = false)
    public void runPatternQuery10() {
        BValue[] outputEvents = BRunUtil.invoke(notPatternTests, "runPatternQuery10");
        Assert.assertNotNull(outputEvents);
        Assert.assertEquals(outputEvents.length, 1);
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("aId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("bId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("cId"), new BInteger(31));
    }
}

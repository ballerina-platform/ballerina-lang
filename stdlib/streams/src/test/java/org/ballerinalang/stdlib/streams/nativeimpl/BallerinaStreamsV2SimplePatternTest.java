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
 * This contains methods to test simple pattern query behaviour in Ballerina Streaming V2.
 *
 * @since 0.990.5
 */
public class BallerinaStreamsV2SimplePatternTest {

    private CompileResult simplePatternTests;

    @BeforeClass
    public void setup() {
        simplePatternTests =
                BCompileUtil.compileOffline("test-src/native/streamingv2-native-simple-pattern-test.bal");
    }

    //TODO: need to fix properly, intermittently failing
    @Test(description = "Test simple pattern query.", enabled = false)
    public void testSimplePatternQuery() {
        BValue[] outputEvents = BRunUtil.invoke(simplePatternTests, "startPatternQuery");
        Assert.assertNotNull(outputEvents);
        Assert.assertEquals(outputEvents.length, 2);
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("aId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("bId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("cId"), new BInteger(31));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("dId"), new BInteger(41));
        Assert.assertEquals(((BMap) outputEvents[1]).getMap().get("aId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[1]).getMap().get("bId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[1]).getMap().get("cId"), new BInteger(32));
        Assert.assertEquals(((BMap) outputEvents[1]).getMap().get("dId"), new BInteger(42));
    }
}

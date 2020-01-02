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
public class BallerinaStreamsV2CountingPatternTest {

    private CompileResult notPatternTests;

    @BeforeClass
    public void setup() {
        notPatternTests = BCompileUtil.compile("test-src/native/streamingv2-native-simple-counting-pattern-test.bal");
    }

    @Test(
            enabled = false, // disabling counting patterns with 9b4810d1778eca0cf70a28da32ae1ddd833cab7a
            description = "Test // A[2..4] -> B, select a[1].id as aId, b[0].id as bId, 0 as cID pattern query."
    )
    public void runPatternQuery1() {
        BValue[] outputEvents = BRunUtil.invoke(notPatternTests, "runPatternQuery1");
        Assert.assertNotNull(outputEvents);
        Assert.assertEquals(outputEvents.length, 2);
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("aId"), new BInteger(12));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("bId"), new BInteger(21));
        Assert.assertEquals(((BMap) outputEvents[0]).getMap().get("cId"), new BInteger(0));
        Assert.assertEquals(((BMap) outputEvents[1]).getMap().get("aId"), new BInteger(15));
        Assert.assertEquals(((BMap) outputEvents[1]).getMap().get("bId"), new BInteger(22));
        Assert.assertEquals(((BMap) outputEvents[1]).getMap().get("cId"), new BInteger(0));
    }
}

/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.streams;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test custom extensions in Ballerina Streaming V2.
 *
 * @since 0.990.3
 */
public class BallerinaStreamsV2CustomExtensionTest {

    private CompileResult result;
    private CompileResult result2;
    private CompileResult result3;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/streamingv2-custom-extension-test1.bal");
        result2 = BCompileUtil.compile("test-src/streamingv2-custom-extension-test2.bal");
        result3 = BCompileUtil.compile("test-src/streamingv2-custom-extension-test3.bal");
    }

    @Test
    public void testThrottleQuery1() {
        BValue[] outputValues = BRunUtil.invoke(result, "startThrottleQuery");
        Assert.assertNotNull(outputValues);

        Assert.assertEquals(((BMap) outputValues[0]).get("sumPrice"), new BFloat(700.0));
        Assert.assertEquals(((BMap) outputValues[1]).get("sumPrice"), new BFloat(760.5));
        Assert.assertEquals(((BMap) outputValues[2]).get("sumPrice"), new BFloat(700.0));
        Assert.assertEquals(((BMap) outputValues[3]).get("sumPrice"), new BFloat(760.5));
    }

    @Test
    public void testThrottleQuery2() {
        BValue[] outputValues = BRunUtil.invoke(result2, "startThrottleQuery");
        Assert.assertNotNull(outputValues);

        Assert.assertEquals(((BMap) outputValues[0]).get("sumPrice"), new BFloat(700.0));
        Assert.assertEquals(((BMap) outputValues[1]).get("sumPrice"), new BFloat(760.5));
    }

    @Test
    public void testThrottleQuery3() {
        BValue[] outputValues = BRunUtil.invoke(result3, "startThrottleQuery");
        Assert.assertNotNull(outputValues);

        Assert.assertEquals(((BMap) outputValues[0]).get("sumPrice"), new BFloat(700.0));
        Assert.assertEquals(((BMap) outputValues[1]).get("sumPrice"), new BFloat(760.5));
        Assert.assertEquals(((BMap) outputValues[2]).get("sumPrice"), new BFloat(700.0));
    }
}

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
 * This contains methods to test the extensibility of windows in Ballerina Streaming V2.
 *
 * @since 0.990.3
 */
public class BallerinaStreamsV2CustomWindowTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/streamingv2-custom-window-test.bal");
    }

    @Test(description = "Test streaming query with a custom window")
    public void testSelectQuery() {
        BValue[] personArray = BRunUtil.invoke(result, "startSelectQuery");

        Assert.assertNotNull(personArray);
        Assert.assertEquals(personArray.length, 3, "Expected events are not received");

        BMap<String, BValue> person1 = (BMap<String, BValue>) personArray[0];
        BMap<String, BValue> person2 = (BMap<String, BValue>) personArray[1];
        BMap<String, BValue> person3 = (BMap<String, BValue>) personArray[2];

        Assert.assertEquals(person1.get("name").stringValue(), "Grainier");
        Assert.assertEquals(((BInteger) person1.get("age")).intValue(), 27);
        Assert.assertEquals(person1.get("status").stringValue(), "single");
        Assert.assertEquals(person1.get("address").stringValue(), "Mountain View");
        Assert.assertEquals(person1.get("phoneNo").stringValue(), "123456");

        Assert.assertEquals(person2.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) person2.get("age")).intValue(), 30);
        Assert.assertEquals(person2.get("status").stringValue(), "single");
        Assert.assertEquals(person2.get("address").stringValue(), "Memphis");
        Assert.assertEquals(person2.get("phoneNo").stringValue(), "123456");

        Assert.assertEquals(person3.get("name").stringValue(), "Gimantha");
        Assert.assertEquals(((BInteger) person3.get("age")).intValue(), 29);
        Assert.assertEquals(person3.get("status").stringValue(), "single");
        Assert.assertEquals(person3.get("address").stringValue(), "Houston");
        Assert.assertEquals(person3.get("phoneNo").stringValue(), "123456");

    }
}

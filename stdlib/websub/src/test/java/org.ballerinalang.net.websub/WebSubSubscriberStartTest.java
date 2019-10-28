/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.websub;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test WebSub listener startup.
 */
public class WebSubSubscriberStartTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/subscriber/test_subscriber_startup.bal");
    }

    @Test(description = "Test multiple Subscriber service startup in a single port")
    public void testMultipleSubscribersStartUpInSamePort() {
        BValue[] returns = BRunUtil.invoke(result, "startSubscriberService");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0].stringValue().contains("failed to start server connector '0.0.0.0:8387': " +
                                                                    "Address already in use"));
    }
}

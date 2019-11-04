/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Class to test WebSub Hub startup.
 */
public class WebSubHubStartTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/hub/test_hub_startup.bal");
    }

    @Test
    public void testHubStartUp() {
        BValue[] returns = BRunUtil.invoke(result, "testHubStartUp");
        assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue((((BBoolean) returns[0]).value()));
    }

    @Test
    public void testPublisherAndSubscriptionInvalidSameResourcePath() {
        BValue[] returns = BRunUtil.invoke(result, "testPublisherAndSubscriptionInvalidSameResourcePath");
        assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue((((BBoolean) returns[0]).value()));
    }
}

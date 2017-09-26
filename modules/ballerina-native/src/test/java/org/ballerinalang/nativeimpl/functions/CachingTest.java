/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for caching package.
 */
public class CachingTest {

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("samples/cachingTest.bal");
    }

    @Test
    public void testCreateCache() {
        String cacheName = "userCache";
        int timeout = 60;
        int capacity = 10;
        BValue[] args = new BValue[3];
        args[0] = new BString(cacheName);
        args[1] = new BInteger(timeout);
        args[2] = new BInteger(capacity);
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCreateCache", args);
        Assert.assertTrue(returns.length == 3);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(returns[0].stringValue(), cacheName);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), timeout);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), capacity);
    }
}

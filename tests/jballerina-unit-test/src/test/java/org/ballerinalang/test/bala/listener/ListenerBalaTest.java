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
package org.ballerinalang.test.bala.listener;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for access listener from another project.
 */
public class ListenerBalaTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_listener");
        compileResult = BCompileUtil.compile("test-src/bala/test_bala/listener/external_listener_access.bal");
    }

    @Test(description = "Test access listener in different module")
    public void testBasicStructAsObject() {
        final BValue[] result = BRunUtil.invoke(compileResult, "getStartAndAttachCount");
        Assert.assertEquals(result.length, 1, "expected one return type");
        Assert.assertNotNull(result[0]);
        Assert.assertEquals(result[0].stringValue(), "2_3");
    }

    @Test(description = "Test no cyclic reference is identified when a custom listener " +
            "with service reference is used")
    public void testNoCycleIdentifiedWhenCustomListenerWithServiceReference() {
        CompileResult result = BCompileUtil.compile("test-src/bala/test_bala/listener/custom_listener_with_service.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }
}

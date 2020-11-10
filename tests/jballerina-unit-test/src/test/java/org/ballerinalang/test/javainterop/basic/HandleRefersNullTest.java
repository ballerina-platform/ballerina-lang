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
package org.ballerinalang.test.javainterop.basic;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BHandleValue;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ballerinax/java functions related to Java null.
 *
 * @since 1.0.0
 */
public class HandleRefersNullTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/basic/handle_null_test.bal");
    }

    @Test(description = "Test java:createNull method in ballerinax/java")
    public void testCreateNullHandle() {
        BValue[] returns = BRunUtil.invoke(result, "testCreateNullHandle");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(((BHandleValue) returns[0]).getValue());
    }

    @Test(description = "Test java:isNull method in ballerinax/java")
    public void testIsNull() {
        BValue[] args = new BValue[1];
        args[0] = new BHandleValue(null);
        BValue[] returns = BRunUtil.invoke(result, "testIsNull", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }
}

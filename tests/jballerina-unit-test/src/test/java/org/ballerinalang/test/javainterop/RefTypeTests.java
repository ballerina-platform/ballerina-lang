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
package org.ballerinalang.test.javainterop;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for java interop with ballerina ref types.
 *
 * @since 1.0.0
 */
public class RefTypeTests {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/ballerina_types_as_interop_types.bal");
    }

    @Test(description = "Test interoperability with ballerina array value as return and map as a param")
    public void testInteropWithBalArrayAndMap() {
        BValue[] returns = BRunUtil.invoke(result, "interopWithArrayAndMap");
        Assert.assertEquals(returns.length, 1);

        Assert.assertTrue(returns[0] instanceof BValueArray);
        BValueArray arr = (BValueArray) returns[0];
        Assert.assertEquals(arr.stringValue(), "[1, 8]");
    }
}

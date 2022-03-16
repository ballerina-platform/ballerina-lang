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

import io.ballerina.runtime.internal.values.HandleValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object returns = BRunUtil.invoke(result, "testCreateNullHandle");
        Assert.assertNull(((HandleValue) returns).getValue());
    }

    @Test(description = "Test java:isNull method in ballerinax/java")
    public void testIsNull() {
        Object[] args = new Object[1];
        args[0] = new HandleValue(null);
        Object returns = BRunUtil.invoke(result, "testIsNull", args);
        Assert.assertTrue((Boolean) returns);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}

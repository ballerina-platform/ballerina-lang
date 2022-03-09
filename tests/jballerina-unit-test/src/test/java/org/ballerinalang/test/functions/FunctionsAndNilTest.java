/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.functions;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains tests that are related to functions and nil type ().
 */
public class FunctionsAndNilTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/functions/functions-with-nil/functions_with_nil_basics.bal");
    }

    @Test(description = "Test functions that returns nil type")
    public void testFuncReturnNilImplicit() {
        Object returns = BRunUtil.invoke(result, "funcReturnNilImplicit");
        Assert.assertNull(returns);
    }

    @Test(description = "Test functions that returns nil type")
    public void funcReturnNilExplicit() {
        Object returns = BRunUtil.invoke(result, "funcReturnNilExplicit");
        Assert.assertNull(returns);
    }

    @Test(description = "Test functions that returns nil type")
    public void funcReturnNilOrError() {
        Object[] params = new Object[1];
        params[0] = (10);
        BRunUtil.invoke(result, "funcReturnNilOrError", params);

        params[0] = (30);
        BRunUtil.invoke(result, "funcReturnNilOrError", params);
    }

    @Test(description = "Test functions that returns nil type")
    public void funcReturnOptionallyError() {
        Object[] params = new Object[1];
        params[0] = (10);
        BRunUtil.invoke(result, "funcReturnOptionallyError", params);

        params[0] = (30);
        BRunUtil.invoke(result, "funcReturnOptionallyError", params);
    }

    @Test(description = "Test functions that returns nil type")
    public void testNilReturnAssignment() {
        Object returns = BRunUtil.invoke(result, "testNilReturnAssignment");
        Assert.assertNull(returns);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}

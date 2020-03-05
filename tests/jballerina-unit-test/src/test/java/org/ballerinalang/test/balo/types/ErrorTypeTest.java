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

package org.ballerinalang.test.balo.types;

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test error types constructor with balo.
 */
public class ErrorTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "errors");
        result = BCompileUtil.compile("test-src/balo/test_balo/types/error_type_test.bal");
    }

    @Test()
    public void errorFromAnotherPkg() {
        BValue[] returns = BRunUtil.invoke(result, "getApplicationError");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(returns[0].stringValue(),
                "{ballerina/sql}ApplicationError {message:\"Client has been stopped\"}");
    }

    @Test()
    public void indirectErrorCtorFromAnotherPkg() {
        BValue[] returns = BRunUtil.invoke(result, "getApplicationErrorIndirectCtor");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(returns[0].stringValue(),
                "{ballerina/sql}ApplicationError {message:\"Client has been stopped\"}");
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository(
                "test-src/balo/test_projects/finite_type_project", "finiteTypeTest", "foo");
    }
}

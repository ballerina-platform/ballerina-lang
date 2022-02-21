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

package org.ballerinalang.test.bala.types;

import io.ballerina.runtime.api.values.BError;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test error types constructor with bala.
 */
public class ErrorTypeTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_errors");
        result = BCompileUtil.compile("test-src/bala/test_bala/types/error_type_test.bal");
        negativeResult = BCompileUtil.compile("test-src/bala/test_bala/types/error_type_negative_test.bal");
    }

    @Test
    public void errorFromAnotherPkg() {
        Object returns = BRunUtil.invoke(result, "getApplicationError");
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BError);
        Assert.assertEquals(returns.toString(),
                "error ApplicationError (\"{ballerina/sql}ApplicationError\",message=\"Client has been stopped\")");
    }

    @Test
    public void indirectErrorCtorFromAnotherPkg() {
        Object returns = BRunUtil.invoke(result, "getApplicationErrorIndirectCtor");
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BError);
        Assert.assertEquals(returns.toString(),
                "error ApplicationError (\"{ballerina/sql}ApplicationError\",message=\"Client has been stopped\")");
    }

    @Test
    public void testUsageOfDistinctTypeFromAnotherPackage() {
        Object returns = BRunUtil.invoke(result, "getDistinctError");
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BError);
        Assert.assertEquals(returns.toString(),
                "error OrderCreationError2 (\"OrderCreationError2-msg\",message=\"Client has been stopped\")");
    }

    @Test
    public void testDistinctTypeFromAnotherPackageInATypeDef() {
        Object returns = BRunUtil.invoke(result, "testDistinctTypeFromAnotherPackageInATypeDef");
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BError);
        Assert.assertEquals(returns.toString(),
                "error OurError (\"Our error message\",message=\"Client has been stopped\")");
    }

    @Test
    public void testDistinctTypeFromAnotherPackageInATypeDefWithACast() {
        Object returns = BRunUtil.invoke(result, "testDistinctTypeFromAnotherPackageInATypeDefWithACast");
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BError);
        Assert.assertEquals(returns.toString(),
                "error OurProccessingError (\"Our error message\",message=\"Client has been stopped\")");
    }

    @Test
    public void testPerformInvalidCastWithDistinctErrorType() {
        Object returns = BRunUtil.invoke(result, "performInvalidCastWithDistinctErrorType");
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BError);
        Assert.assertEquals(returns.toString(),
                "error(\"{ballerina}TypeCastError\",message=\"incompatible types: 'OurProccessingError' cannot be " +
                        "cast to 'errors:OrderProcessingError'\")");
    }

    @Test
    public void testErrorDetailDefinedAfterErrorDef() {
        BRunUtil.invoke(result, "testErrorDetailDefinedAfterErrorDef");
    }
    
    @Test
    public void testDistinctErrorTypeNegative() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'testorg/errors:1.0.0:OrderCreationError2', " +
                        "found 'testorg/errors:1.0.0:OrderCreationError'", 23, 9);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'testorg/errors:1.0.0:NewPostDefinedError', " +
                        "found 'testorg/errors:1.0.0:PostDefinedError'", 28, 32);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}

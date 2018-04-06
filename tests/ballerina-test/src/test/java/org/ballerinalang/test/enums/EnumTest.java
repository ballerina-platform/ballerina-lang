/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.enums;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for enums in Ballerina.
 *
 * @since 0.95
 */
public class EnumTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/enums/enums.bal");
    }

    @Test(description = "Test Basic enum support")
    public void testBasicEnum() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBasicEnumSupport");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 14);
    }

    @Test(description = "Test enum in struct")
    public void testEnumInStruct() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEnumInStruct");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
    }


    @Test(description = "Test enum in another package")
    public void testEnumInAnotherPackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEnumInAnotherPackage");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 200);
    }

//    @Test(description = "Test enum to any cast expressions")
//    public void testEnumToAnyCasts() {
//        BValue[] returns = BRunUtil.invoke(compileResult, "testEnumToAnyCast");
//
//        Assert.assertTrue(returns[0] instanceof BInteger);
//        Assert.assertEquals(((BInteger) returns[0]).intValue(), 201);
//    }

    @Test(description = "Test enum to any cast expressions")
    public void testEnumSameTypeCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEnumSameTypeCast");

        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 404);
    }

    @Test(description = "Test negatives tests")
    public void testEnumNegativeCases() {
        CompileResult result = BCompileUtil.compile("test-src/enums/enums-negative.bal");

//      BAssertUtil.validateError(result, 0,
//          "operator '==' not defined for 'GREATER_THAN|NOT|OR|PLUS|MINUS|AND' and 'int'", 10, 24);
//      BAssertUtil.validateError(result, 1,
//          "incompatible types: expected 'GREATER_THAN|NOT|OR|PLUS|MINUS|AND', found 'string'", 16, 14);
//      BAssertUtil.validateError(result, 2,
//          "invalid operation: type 'AND|MINUS|NOT|GREATER_THAN|PLUS|OR' does not support field access", 21, 14);
        BAssertUtil.validateError(result, 3, "undefined symbol 'kind'", 26, 12);
        BAssertUtil.validateError(result, 4, "undefined symbol 'bar'", 34, 15);
    }
}

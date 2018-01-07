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

package org.ballerinalang.test.types.map;

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
 * Test cases for constraining map types with any other builtin or user defined types.
 */
public class ConstrainedMapTest {

    private CompileResult compileResult;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile(this, "test-src", "types/map/constrained-map.bal");
        negativeResult = BCompileUtil.compile(this, "test-src", "types/map/constrained-map-negative.bal");
    }

    @Test(description = "Map constrained with type negative semantic validations.")
    public void testConstrainedMapNegative() {
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        BAssertUtil.validateError(negativeResult, 0, "incompatible types: expected 'map<int>', found 'map'", 4, 12);
        BAssertUtil.validateError(negativeResult, 1, "incompatible types: expected 'int', found 'string'", 8, 44);
        BAssertUtil.validateError(negativeResult, 2, "incompatible types: expected 'string', found 'int'", 14, 23);
    }


    @Test(description = "Map constrained with value type value retrieval positive case.")
    public void testConstrainedMapValueTypePositive() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypePositive");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "kevin");
    }

    @Test(description = "Map constrained with value type value retrieval negative case.")
    public void testConstrainedMapValueTypeNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeNegative");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), null);
    }


    @Test(description = "Map constrained with value type index based value retrieval positive case.")
    public void testConstrainedMapValueTypeIndexBasedPositive() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeIndexBasedPositive");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "kevin");
    }

    @Test(description = "Map constrained with value type index based value retrieval negative case.")
    public void testConstrainedMapValueTypeIndexBasedNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapValueTypeIndexBasedNegative");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), null);
    }

    @Test(description = "Map constrained with user defined type value retrieval positive case.")
    public void testConstrainedMapStructTypePositive() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapStructTypePositive");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 25);
    }

    @Test(description = "Map constrained with user defined type value retrieval negative case.")
    public void testConstrainedMapStructTypeNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedMapStructTypeNegative");
        Assert.assertNull(returns[0]);
    }

    @Test(description = "test Map constrained with value type assignment to Map constrained with any.")
    public void testAnyAssignment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnyAssignment");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "kevin");
    }

}

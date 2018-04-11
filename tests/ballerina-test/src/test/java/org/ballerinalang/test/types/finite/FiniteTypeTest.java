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

package org.ballerinalang.test.types.finite;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test finite type.
 */
public class FiniteTypeTest {

    private CompileResult result;


    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/finite/finite-type.bal");
    }

    @Test()
    public void finiteAssignmentCompositeFiniteTypes() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentCompositeFiniteTypes");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "off");
    }

    @Test()
    public void finiteTypeSetIntersectionCaseOne() {
        BValue[] returns = BRunUtil.invoke(result, "finiteTypeSetIntersectionCaseOne");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "off");
    }

    @Test()
    public void finiteTypeSetIntersectionCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "finiteTypeSetIntersectionCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
    }

    @Test()
    public void finiteTypeSetIntersectionCaseThree() {
        BValue[] returns = BRunUtil.invoke(result, "finiteTypeSetIntersectionCaseThree");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "very good");
    }

    @Test()
    public void finiteAssignmentStateType() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentStateType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).stringValue(), "off");
    }

    @Test()
    public void finiteAssignmentNumberSetType() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentNumberSetType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test()
    public void finiteAssignmentStringOrIntSetType() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentStringOrIntSetType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals((((BString) returns[0]).stringValue()), "This is a string");
    }

    @Test()
    public void finiteAssignmentStringOrIntSetTypeCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentStringOrIntSetTypeCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 111);
    }


    @Test()
    public void finiteAssignmentIntSetType() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentIntSetType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 222);
    }

    @Test()
    public void finiteAssignmentIntArrayType() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentIntArrayType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 9989);
    }

    @Test()
    public void finiteAssignmentStateSameTypeComparison() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentStateSameTypeComparison");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 2);
    }

    @Test()
    public void finiteAssignmentStateSameTypeComparisonCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentStateSameTypeComparisonCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals((((BString) returns[0]).stringValue()), "on");
    }

    @Test()
    public void finiteAssignmentRefValueType() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentRefValueType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BStruct);
    }

    @Test()
    public void finiteAssignmentRefValueTypeCaseTwo() {
        BValue[] returns = BRunUtil.invoke(result, "finiteAssignmentRefValueTypeCaseTwo");
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals((((BInteger) returns[0]).intValue()), 4);
    }

}


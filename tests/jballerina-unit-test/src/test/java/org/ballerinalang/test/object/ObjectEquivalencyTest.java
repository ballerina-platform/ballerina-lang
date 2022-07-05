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
package org.ballerinalang.test.object;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for equivalence of user defined struct types with attached functions in ballerina.
 */
public class ObjectEquivalencyTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {

        compileResult = BCompileUtil.compile("test-src/object/ObjectEquivalencyProject");
    }

    @Test(description = "Test equivalence of objects that are in the same package and the members are visible at " +
            "module level or above.")
    public void testObjectEquivalenceWhenFieldsHaveModuleVisibility() {
        Object returns = BRunUtil.invoke(compileResult, "testObjectEquivalenceWhenFieldsHaveModuleVisibility");
        Assert.assertEquals(returns.toString(), "234-56-7890:employee");
    }

    @Test(description = "Test equivalence of of objects that are in the same package and all the members are public")
    public void testObjectEquivalenceWhenFieldsHavePublicVisibility() {
        Object returns = BRunUtil.invoke(compileResult, "testObjectEquivalenceWhenFieldsHavePublicVisibility");
        Assert.assertEquals(returns.toString(), "234-56-7890:employee");
    }

    @Test(description = "Test equivalence of public objects that are in the same package. " +
            "Equivalency test is performed in another package.")
    public void testEqOfPublicObjectsInBala() {
        Object returns = BRunUtil.invoke(compileResult, "testEqOfPublicObjectsInBala");
        Assert.assertEquals(returns.toString(), "234-56-7890:employee");
    }

    @Test(description = "Test equivalency of public objects that are in two different packages")
    public void testEqOfPublicObjects() {
        Object returns = BRunUtil.invoke(compileResult, "testEqOfPublicObjects");
        Assert.assertEquals(returns.toString(), "234-56-1234:employee");
    }

    @Test(description = "Test equivalency of public objects that are in two different packages")
    public void testEqOfPublicObjects2() {
        Object returns = BRunUtil.invoke(compileResult, "testEqOfPublicObjects2");
        Assert.assertEquals(returns.toString(), "234-56-3345:employee");
    }

    @Test(description = "Test runtime equivalency of objects")
    public void testNonPublicTypedescEq() {
        Object returns = BRunUtil.invoke(compileResult, "testNonPublicTypedescEq");
        Assert.assertEquals(returns.toString(), "ttt");
    }

    @Test(description = "Test runtime equivalency of public objects")
    public void testEqOfPublicObjectsInSamePackage() {
        Object returns = BRunUtil.invoke(compileResult, "testEqOfPublicObjectsInSamePackage");
        Assert.assertEquals(returns.toString(), "Skyhigh");
    }

    @Test(description = "Test runtime equivalency of public objects")
    public void testRuntimeEqPublicObjects() {
        Object returns = BRunUtil.invoke(compileResult, "testRuntimeEqPublicObjects");
        Assert.assertEquals(returns.toString(), "Skytop");
    }

    @Test(description = "Test runtime equivalency of object")
    public void testRuntimeEqPublicObjects1() {
        Object returns = BRunUtil.invoke(compileResult, "testRuntimeEqPublicObjects1");
        Assert.assertEquals(returns.toString(), "Brandon:userPFoo");
    }

    @Test(description = "Test object equivalency as an argument")
    public void testObjectEquivalencyWithArguments() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectEquivalencyWithArguments");

        Assert.assertEquals(returns.get(0).toString(), "ENG2CMB");
        Assert.assertEquals(returns.get(1).toString(), "1CMB");
        Assert.assertEquals(returns.get(2).toString(), "ENG2CMB");
    }

    @Test(description = "Test tuple equivalency with object equivalency ")
    public void testTupleMatchWithObjectEquivalency() {
        Object returns = BRunUtil.invoke(compileResult, "testTupleMatchWithObjectEquivalency");
        Assert.assertEquals(returns.toString(), "SUCCESS");
    }

    @Test
    public void testObjectEqualityWithDefaultConstructor() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectEqualityWithDefaultConstructor");
        Assert.assertEquals(returns.get(0).toString(), "{name:, id:}");
        Assert.assertEquals(returns.get(1).toString(), "{name:, id:}");
    }

    @Test
    public void testObjectEqualityWithRecursiveTypes() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectEqualityWithRecursiveTypes");
        Assert.assertEquals(returns.get(0).toString(), "{field:value A}");
        Assert.assertEquals(returns.get(1).toString(), "{field:value B}");
    }

    @Test
    public void testObjectMemberOrder() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testObjectMemberOrder");
        Assert.assertEquals(returns.get(0).toString(), "{age:45, name:Doe, address:}");
        Assert.assertEquals(returns.get(1).toString(), "{age:35, name:John, address:}");
    }

    @Test(description = "Test inherent type violation with nil value.",
            expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.object\\}InherentTypeViolation " +
                    "\\{\"message\":\"invalid value for object field 'x': expected value of type 'string', " +
                    "found '\\(\\)'.*")
    public void testInherentTypeViolationWithNilType() {
        BRunUtil.invoke(compileResult, "testInherentTypeViolationWithNilType");
    }

    @Test
    public void testObjectAssignabilityBetweenNonClientAndClientObject() {
        BRunUtil.invoke(compileResult, "testObjectAssignabilityBetweenNonClientAndClientObject");
    }

    @Test
    public void testSubtypingBetweenNonClientAndClientObject() {
        BRunUtil.invoke(compileResult, "testSubtypingBetweenNonClientAndClientObject");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

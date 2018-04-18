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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for equivalency of user defined struct types with attached functions in ballerina.
 */
public class ObjectEquivalencyTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/object/object-equivalency.bal");
    }

    @Test(description = "Test equivalence of structs that are in the same package")
    public void testEqOfPrivateStructsInSamePackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEquivalenceOfPrivateStructsInSamePackage");

        Assert.assertEquals(returns[0].stringValue(), "234-56-7890:employee");
    }

    @Test(description = "Test equivalence of public structs that are in the same package")
    public void testEqOfPublicStructsInSamePackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEquivalenceOfPublicStructsInSamePackage");

        Assert.assertEquals(returns[0].stringValue(), "234-56-7890:employee");
    }

    @Test(description = "Test equivalence of public structs that are in the same package. " +
            "Equivalency test is performed in another package.")
    public void testEqOfPublicStructs() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testEqOfPublicStructs");

        Assert.assertEquals(returns[0].stringValue(), "234-56-7890:employee");
    }

    @Test(description = "Test equivalency of public structs that are in two different packages")
    public void testEqOfPublicStructs1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEqOfPublicStructs1");

        Assert.assertEquals(returns[0].stringValue(), "234-56-1234:employee");
    }

    @Test(description = "Test equivalency of public structs that are in two different packages")
    public void testEqOfPublicStructs2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEqOfPublicStructs2");

        Assert.assertEquals(returns[0].stringValue(), "234-56-3345:employee");
    }

    @Test(description = "Test runtime equivalency of structs")
    public void testRuntimeEqPrivateStructsInSamePackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeEqPrivateStructsInSamePackage");

        Assert.assertEquals(returns[0].stringValue(), "ttt");
    }

    @Test(description = "Test runtime equivalency of structs")
    public void testRuntimeEqPublicStructsInSamePackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeEqPublicStructsInSamePackage");

        Assert.assertEquals(returns[0].stringValue(), "Skyhigh");
    }

    @Test(description = "Test runtime equivalency of structs")
    public void testRuntimeEqPublicStructs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeEqPublicStructs");

        Assert.assertEquals(returns[0].stringValue(), "Skytop");
    }

    @Test(description = "Test runtime equivalency of structs")
    public void testRuntimeEqPublicStructs1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeEqPublicStructs1");

        Assert.assertEquals(returns[0].stringValue(), "Brandon:userPFoo");
    }

    @Test(description = "Test struct equivalency as an argument")
    public void testStructEquivalencyWithArguments() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructEquivalencyWithArguments");

        Assert.assertEquals(returns[0].stringValue(), "ENG2CMB");
        Assert.assertEquals(returns[1].stringValue(), "1CMB");
        Assert.assertEquals(returns[2].stringValue(), "ENG2CMB");
    }

    @Test(description = "Test struct equivalency with function type which has structs")
    public void testStructEquivalencyWithFunctionType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructEquivalencyWithFunctionType");
        Assert.assertEquals(returns[0].stringValue(), "anyStruct{\"s\":\"sss\"}");
        Assert.assertEquals(returns[1].stringValue(), "someStruct{\"s\":\"sss\"}");
    }
    
    @Test(description = "Test tuple equivalency with object equivalency ")
    public void testTupleMatchWithObjectEquivalency() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTupleMatchWithObjectEquivalency");
        Assert.assertEquals(returns[0].stringValue(), "SUCCESS");
    }
    
}

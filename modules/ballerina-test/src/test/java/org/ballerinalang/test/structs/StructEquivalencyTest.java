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
package org.ballerinalang.test.structs;

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
public class StructEquivalencyTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/structs/struct-equivalency.bal");
    }

    @Test(description = "Test equivalence of structs that are in the same package")
    public void testEquivalenceOfStructsInSamePackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEquivalenceOfStructsInSamePackage");

        Assert.assertEquals(returns[0].stringValue(), "234-56-7890");
    }

    @Test(description = "Test equivalence of structs that are in the same package from a third package")
    public void testEquivalenceOfStructsInSamePackageFromDifferentPackage() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testEquivalenceOfStructsInSamePackageFromDifferentPackage");

        Assert.assertEquals(returns[0].stringValue(), "234-56-7890");
    }

    @Test(description = "Test struct equivalency in structs that are in two different packages. " +
            "This test function is in another package.")
    public void testStructEqViewFromThirdPackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStructEqViewFromThirdPackage");

        Assert.assertEquals(returns[0].stringValue(), "ball");
    }

    @Test(description = "Test struct equivalency in structs that are in two different packages. " +
            "This test function is in another package.")
    public void testStructEqRuntime() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeStructEq");

        Assert.assertEquals(returns[0].stringValue(), "ttt");
    }
}

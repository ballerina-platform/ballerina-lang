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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined struct types with private fields in ballerina.
 */
public class ObjectWithPrivateFieldsTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/structs/ObjectWithPrivateFieldsTestProject");
    }

    @Test(description = "Test private struct field access")
    public void testPrivateFieldAccessSamePackage() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "textPrivateFieldAccess1");

        Assert.assertEquals(returns.get(0).toString(), "sam");
        Assert.assertEquals(returns.get(1).toString(), "95134");
        Assert.assertEquals(returns.get(2).toString(), "234-56-7890");
        Assert.assertEquals(returns.get(3), 45034L);
        Assert.assertEquals(returns.get(4), 123456L);
    }

    @Test(description = "Test struct with private field which is in a different package")
    public void testStructWithPrivateFieldDifferentPackage() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "textPrivateFieldAccess2");

        Assert.assertEquals(returns.get(0), 12L);
        Assert.assertEquals(returns.get(1).toString(), "mad");
    }

    @Test(description = "Test compile time struct equivalence with private fields")
    public void testCompileTimeStructEqWithPrivateFields() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testCompileTimeStructEqWithPrivateFields");

        Assert.assertEquals(returns.get(0).toString(), "jay");
        Assert.assertEquals(returns.get(1).toString(), "95134");
        Assert.assertEquals(returns.get(2).toString(), "123-56-7890");
        Assert.assertEquals(returns.get(3), 458L);
    }

    @Test(description = "Test compile time struct equivalence with private fields. " +
            "Structs are in different packages")
    public void testCompileTimeStructEqWithPrivateFieldsTwoPackages() {
        BArray returns =
                (BArray) BRunUtil.invoke(compileResult, "testCompileTimeStructEqWithPrivateFieldsTwoPackages");

        Assert.assertEquals(returns.get(0), 28L);
        Assert.assertEquals(returns.get(1).toString(), "mal");
        Assert.assertEquals(returns.get(2).toString(), "95134");
    }

    @Test(description = "Test compile time struct equivalence with private fields")
    public void testRuntimeStructEqWithPrivateFields() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testRuntimeStructEqWithPrivateFields");

        Assert.assertEquals(returns.get(0).toString(), "jay");
        Assert.assertEquals(returns.get(1).toString(), "95134");
        Assert.assertEquals(returns.get(2).toString(), "123-56-7890");
        Assert.assertEquals(returns.get(3), 458L);
        Assert.assertEquals(returns.get(4), 123L);
    }

    @Test(description = "Test runtime time struct equivalence with private fields. " +
            "Structs are in different packages")
    public void testRuntimeStructEqWithPrivateFieldsTwoPackages1() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testRuntimeStructEqWithPrivateFieldsTwoPackages1");

        Assert.assertEquals(returns.get(0).toString(), "jay");
        Assert.assertEquals(returns.get(1).toString(), "95134");
        Assert.assertEquals(returns.get(2).toString(), "123-56-7890");
        Assert.assertEquals(returns.get(3), 458L);
    }

    @Test(description = "Test runtime time struct equivalence with private fields. " +
            "Structs are in different packages")
    public void testRuntimeStructEqWithPrivateFieldsTwoPackages2() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testRuntimeStructEqWithPrivateFieldsTwoPackages2");

        Assert.assertEquals(returns.get(0).toString(), "mal");
        Assert.assertEquals(returns.get(1), 56L);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

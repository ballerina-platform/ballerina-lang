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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined struct types with private fields in ballerina.
 */
public class ObjectWithPrivateFieldsTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/structs/object-private-fields.bal");
    }

    @Test(description = "Test private struct field access")
    public void testPrivateFieldAccessSamePackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "textPrivateFieldAccess1");

        Assert.assertEquals(returns[0].stringValue(), "sam");
        Assert.assertEquals(returns[1].stringValue(), "95134");
        Assert.assertEquals(returns[2].stringValue(), "234-56-7890");
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 45034);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 123456);
    }

    @Test(description = "Test struct with private field which is in a different package")
    public void testStructWithPrivateFieldDifferentPackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "textPrivateFieldAccess2");

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
        Assert.assertEquals(returns[1].stringValue(), "mad");
    }

    @Test(description = "Test compile time struct equivalence with private fields")
    public void testCompileTimeStructEqWithPrivateFields() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCompileTimeStructEqWithPrivateFields");

        Assert.assertEquals(returns[0].stringValue(), "jay");
        Assert.assertEquals(returns[1].stringValue(), "95134");
        Assert.assertEquals(returns[2].stringValue(), "123-56-7890");
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 458);
    }

    @Test(description = "Test compile time struct equivalence with private fields. " +
            "Structs are in different packages")
    public void testCompileTimeStructEqWithPrivateFieldsTwoPackages() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCompileTimeStructEqWithPrivateFieldsTwoPackages");

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 28);
        Assert.assertEquals(returns[1].stringValue(), "mal");
        Assert.assertEquals(returns[2].stringValue(), "95134");
    }

    @Test(description = "Test compile time struct equivalence with private fields")
    public void testRuntimeStructEqWithPrivateFields() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeStructEqWithPrivateFields");

        Assert.assertEquals(returns[0].stringValue(), "jay");
        Assert.assertEquals(returns[1].stringValue(), "95134");
        Assert.assertEquals(returns[2].stringValue(), "123-56-7890");
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 458);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 123);
    }

    @Test(description = "Test runtime time struct equivalence with private fields. " +
            "Structs are in different packages")
    public void testRuntimeStructEqWithPrivateFieldsTwoPackages1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeStructEqWithPrivateFieldsTwoPackages1");

        Assert.assertEquals(returns[0].stringValue(), "jay");
        Assert.assertEquals(returns[1].stringValue(), "95134");
        Assert.assertEquals(returns[2].stringValue(), "123-56-7890");
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 458);
    }

    @Test(description = "Test runtime time struct equivalence with private fields. " +
            "Structs are in different packages")
    public void testRuntimeStructEqWithPrivateFieldsTwoPackages2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeStructEqWithPrivateFieldsTwoPackages2");

        Assert.assertEquals(returns[0].stringValue(), "mal");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 56);
    }
}

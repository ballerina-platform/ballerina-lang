/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.record;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for equivalency of user defined record types with attached functions in ballerina.
 */
public class ClosedRecordEquivalencyTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/record_project_closed_rec_equiv");
    }

    @Test(description = "Test equivalence of records that are in the same package")
    public void testEqOfPrivateStructsInSamePackage() {
        Object returns = BRunUtil.invoke(compileResult, "testEquivalenceOfPrivateStructsInSamePackage");

        Assert.assertEquals(returns.toString(), "234-56-7890:employee");
    }

    @Test(description = "Test equivalence of public records that are in the same package")
    public void testEqOfPublicStructsInSamePackage() {
        Object returns = BRunUtil.invoke(compileResult, "testEquivalenceOfPublicStructsInSamePackage");

        Assert.assertEquals(returns.toString(), "234-56-7890:employee");
    }

    @Test(description = "Test equivalence of public records that are in the same package. " +
            "Equivalency test is performed in another package.")
    public void testEqOfPublicStructs() {
        Object returns = BRunUtil.invoke(compileResult, "testEqOfPublicStructs");
        Assert.assertEquals(returns.toString(), "234-56-7890:employee");
    }

    @Test(description = "Test equivalency of public records that are in two different packages")
    public void testEqOfPublicStructs1() {
        Object returns = BRunUtil.invoke(compileResult, "testEqOfPublicStructs1");

        Assert.assertEquals(returns.toString(), "234-56-1234:employee");
    }

    @Test(description = "Test equivalency of public records that are in two different packages")
    public void testEqOfPublicStructs2() {
        Object returns = BRunUtil.invoke(compileResult, "testEqOfPublicStructs2");

        Assert.assertEquals(returns.toString(), "234-56-3345:employee");
    }

    @Test(description = "Test runtime equivalency of records")
    public void testRuntimeEqPrivateStructsInSamePackage() {
        Object returns = BRunUtil.invoke(compileResult, "testRuntimeEqPrivateStructsInSamePackage");

        Assert.assertEquals(returns.toString(), "ttt");
    }

    @Test(description = "Test runtime equivalency of records")
    public void testRuntimeEqPublicStructsInSamePackage() {
        Object returns = BRunUtil.invoke(compileResult, "testRuntimeEqPublicStructsInSamePackage");

        Assert.assertEquals(returns.toString(), "Skyhigh");
    }

    @Test(description = "Test runtime equivalency of records")
    public void testRuntimeEqPublicStructs() {
        Object returns = BRunUtil.invoke(compileResult, "testRuntimeEqPublicStructs");

        Assert.assertEquals(returns.toString(), "Skytop");
    }

    @Test(description = "Test runtime equivalency of records")
    public void testRuntimeEqPublicStructs1() {
        Object returns = BRunUtil.invoke(compileResult, "testRuntimeEqPublicStructs1");

        Assert.assertEquals(returns.toString(), "Brandon");
    }

    @Test(description = "Test case for record equivalence")
    public void testRecordEquivalence() {
        Object returns = BRunUtil.invoke(compileResult, "testRecordEquivalence");
        BMap foo = (BMap) returns;

        Assert.assertEquals(foo.size(), 6);
        Assert.assertEquals(foo.get(StringUtils.fromString("a")).toString(), "A");
        Assert.assertEquals(foo.get(StringUtils.fromString("b")).toString(), "B");
        Assert.assertEquals(foo.get(StringUtils.fromString("c")).toString(), "C");
        Assert.assertEquals(foo.get(StringUtils.fromString("d")), 10L);
        Assert.assertEquals(foo.get(StringUtils.fromString("e")), 0.0D);
        Assert.assertNull(foo.get(StringUtils.fromString("p")));
    }

    @Test(description = "Test case for using records with unordered fields in a match")
    public void testUnorderedFieldRecordsInAMatch() {
        Object returns = BRunUtil.invoke(compileResult, "testUnorderedFieldRecordsInAMatch");
        BMap foo = (BMap) returns;

        Assert.assertEquals(foo.size(), 6);
        Assert.assertEquals(foo.get(StringUtils.fromString("a")).toString(), "A");
        Assert.assertEquals(foo.get(StringUtils.fromString("b")).toString(), "B");
        Assert.assertEquals(foo.get(StringUtils.fromString("c")).toString(), "C");
        Assert.assertEquals((foo.get(StringUtils.fromString("d"))), 10L);
        Assert.assertEquals((foo.get(StringUtils.fromString("e"))), 0.0D);
        Assert.assertNull(foo.get(StringUtils.fromString("p")));
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

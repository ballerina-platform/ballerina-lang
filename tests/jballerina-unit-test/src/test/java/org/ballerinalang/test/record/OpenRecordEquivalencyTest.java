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
package org.ballerinalang.test.record;

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for equivalency of user defined record types with attached functions in ballerina.
 */
public class OpenRecordEquivalencyTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/record-project", "open-rec-equiv");
    }

    @Test(description = "Test equivalence of records that are in the same package")
    public void testEqOfPrivateStructsInSamePackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEquivalenceOfPrivateStructsInSamePackage");

        Assert.assertEquals(returns[0].stringValue(), "234-56-7890:employee");
    }

    @Test(description = "Test equivalence of public records that are in the same package")
    public void testEqOfPublicStructsInSamePackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEquivalenceOfPublicStructsInSamePackage");

        Assert.assertEquals(returns[0].stringValue(), "234-56-7890:employee");
    }

    @Test(description = "Test equivalence of public records that are in the same package. " +
            "Equivalency test is performed in another package.")
    public void testEqOfPublicStructs() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                                           "testEqOfPublicStructs");

        Assert.assertEquals(returns[0].stringValue(), "234-56-7890:employee");
    }

    @Test(description = "Test equivalency of public records that are in two different packages")
    public void testEqOfPublicStructs1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEqOfPublicStructs1");

        Assert.assertEquals(returns[0].stringValue(), "234-56-1234:employee");
    }

    @Test(description = "Test equivalency of public records that are in two different packages")
    public void testEqOfPublicStructs2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEqOfPublicStructs2");

        Assert.assertEquals(returns[0].stringValue(), "234-56-3345:employee");
    }

    @Test(description = "Test runtime equivalency of records")
    public void testRuntimeEqPrivateStructsInSamePackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeEqPrivateStructsInSamePackage");

        Assert.assertEquals(returns[0].stringValue(), "ttt");
    }

    @Test(description = "Test runtime equivalency of records")
    public void testRuntimeEqPublicStructsInSamePackage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeEqPublicStructsInSamePackage");

        Assert.assertEquals(returns[0].stringValue(), "Skyhigh");
    }

    @Test(description = "Test runtime equivalency of records")
    public void testRuntimeEqPublicStructs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeEqPublicStructs");

        Assert.assertEquals(returns[0].stringValue(), "Skytop");
    }

    @Test(description = "Test runtime equivalency of records")
    public void testRuntimeEqPublicStructs1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRuntimeEqPublicStructs1");

        Assert.assertEquals(returns[0].stringValue(), "Brandon");
    }

    @Test(description = "Test case for record equivalence")
    public void testRecordEquivalence() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRecordEquivalence");
        BMap foo = (BMap) returns[0];

        Assert.assertEquals(foo.size(), 7);
        Assert.assertEquals(foo.get("a").stringValue(), "A");
        Assert.assertEquals(foo.get("b").stringValue(), "B");
        Assert.assertEquals(foo.get("c").stringValue(), "C");
        Assert.assertEquals(((BInteger) foo.get("d")).intValue(), 10);
        Assert.assertEquals(((BFloat) foo.get("e")).floatValue(), 0.0D);
        Assert.assertEquals(foo.get("f").stringValue(), "rest field");
        Assert.assertNull(foo.get("p"));
    }

    @Test(description = "Test case for using records with unordered fields in a match")
    public void testUnorderedFieldRecordsInAMatch() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUnorderedFieldRecordsInAMatch");
        BMap foo = (BMap) returns[0];

        Assert.assertEquals(foo.size(), 7);
        Assert.assertEquals(foo.get("a").stringValue(), "A");
        Assert.assertEquals(foo.get("b").stringValue(), "B");
        Assert.assertEquals(foo.get("c").stringValue(), "C");
        Assert.assertEquals(((BInteger) foo.get("d")).intValue(), 10);
        Assert.assertEquals(((BFloat) foo.get("e")).floatValue(), 0.0D);
        Assert.assertEquals(foo.get("f").stringValue(), "rest field");
        Assert.assertNull(foo.get("p"));
    }
}

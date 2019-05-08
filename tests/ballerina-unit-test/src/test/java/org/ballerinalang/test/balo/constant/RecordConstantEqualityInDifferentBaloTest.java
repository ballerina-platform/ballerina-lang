/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.balo.constant;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.balo.BaloCreator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constant test cases.
 */
public class RecordConstantEqualityInDifferentBaloTest {

    private static CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "foo");
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "bar");
        compileResult = BCompileUtil.compile("test-src/balo/test_balo/constant/" +
                "record-constant-equality-different-modules.bal");
    }

    // boolean type.

    @Test
    public void testSimpleBooleanRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleBooleanRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleBooleanRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexBooleanRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexBooleanRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexBooleanRecordValueEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanRecordValueEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanRecordValueEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanRecordValueEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanRecordReferenceEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanRecordReferenceEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanRecordReferenceEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanRecordReferenceEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleBooleanRecordValueEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanRecordValueEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanRecordValueEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanRecordValueEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanRecordReferenceEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanRecordReferenceEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanRecordReferenceEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanRecordReferenceEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexBooleanRecordValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexBooleanRecordValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanRecordReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexBooleanRecordReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanRecordValueEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleBooleanRecordValueEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanRecordReferenceEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleBooleanRecordReferenceEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // int type.

    @Test
    public void testSimpleIntRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleIntRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexIntRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexIntRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexIntRecordValueEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexIntRecordValueEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntRecordValueEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexIntRecordValueEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntRecordReferenceEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexIntRecordReferenceEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntRecordReferenceEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexIntRecordReferenceEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleIntRecordValueEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntRecordValueEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntRecordValueEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleIntRecordValueEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntRecordReferenceEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleIntRecordReferenceEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntRecordReferenceEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleIntRecordReferenceEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexIntRecordValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntRecordValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntRecordReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntRecordReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntRecordValueEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntRecordValueEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntRecordReferenceEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntRecordReferenceEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // byte type.

    @Test
    public void testSimpleByteRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleByteRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexByteRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexByteRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexByteRecordValueEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexByteRecordValueEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteRecordValueEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexByteRecordValueEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteRecordReferenceEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexByteRecordReferenceEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteRecordReferenceEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexByteRecordReferenceEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleByteRecordValueEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleByteRecordValueEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteRecordValueEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleByteRecordValueEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteRecordReferenceEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleByteRecordReferenceEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteRecordReferenceEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleByteRecordReferenceEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexByteRecordValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteRecordValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteRecordReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteRecordReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteRecordValueEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteRecordValueEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteRecordReferenceEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteRecordReferenceEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // float type.

    @Test
    public void testSimpleFloatRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleFloatRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexFloatRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexFloatRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexFloatRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexFloatRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexFloatRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexFloatRecordValueEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexFloatRecordValueEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatRecordValueEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexFloatRecordValueEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatRecordReferenceEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexFloatRecordReferenceEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatRecordReferenceEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexFloatRecordReferenceEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleFloatRecordValueEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleFloatRecordValueEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatRecordValueEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleFloatRecordValueEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatRecordReferenceEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleFloatRecordReferenceEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatRecordReferenceEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleFloatRecordReferenceEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexFloatRecordValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexFloatRecordValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatRecordReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexFloatRecordReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatRecordValueEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatRecordValueEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatRecordReferenceEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatRecordReferenceEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // decimal type.

    @Test
    public void testSimpleDecimalRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleDecimalRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleDecimalRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexDecimalRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexDecimalRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexDecimalRecordValueEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalRecordValueEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalRecordValueEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalRecordValueEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalRecordReferenceEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalRecordReferenceEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalRecordReferenceEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalRecordReferenceEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleDecimalRecordValueEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalRecordValueEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalRecordValueEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalRecordValueEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalRecordReferenceEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalRecordReferenceEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalRecordReferenceEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalRecordReferenceEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexDecimalRecordValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexDecimalRecordValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalRecordReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexDecimalRecordReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalRecordValueEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleDecimalRecordValueEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalRecordReferenceEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleDecimalRecordReferenceEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // string type.

    @Test
    public void testSimpleStringRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleStringRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleStringRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleStringRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleStringRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexStringRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexStringRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexStringRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexStringRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexStringRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexStringRecordValueEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexStringRecordValueEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringRecordValueEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexStringRecordValueEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringRecordReferenceEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexStringRecordReferenceEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringRecordReferenceEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexStringRecordReferenceEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleStringRecordValueEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleStringRecordValueEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringRecordValueEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleStringRecordValueEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringRecordReferenceEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleStringRecordReferenceEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringRecordReferenceEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleStringRecordReferenceEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexStringRecordValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexStringRecordValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringRecordReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexStringRecordReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringRecordValueEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleStringRecordValueEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringRecordReferenceEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleStringRecordReferenceEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // nil type.

    @Test
    public void testSimpleNilRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleNilRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexNilRecordValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexNilRecordValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilRecordValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexNilRecordValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilRecordReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexNilRecordReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilRecordReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexNilRecordReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexNilRecordValueEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexNilRecordValueEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilRecordValueEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexNilRecordValueEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilRecordReferenceEqualityUsingRecordAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexNilRecordReferenceEqualityUsingRecordAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilRecordReferenceEqualityUsingRecordAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexNilRecordReferenceEqualityUsingRecordAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleNilRecordValueEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilRecordValueEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilRecordValueEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleNilRecordValueEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilRecordReferenceEqualityUsingSameReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleNilRecordReferenceEqualityUsingSameReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilRecordReferenceEqualityUsingDifferentReferenceInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleNilRecordReferenceEqualityUsingDifferentReferenceInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexNilRecordValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexNilRecordValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilRecordReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexNilRecordReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilRecordValueEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilRecordValueEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilRecordReferenceEqualityInDifferentRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilRecordReferenceEqualityInDifferentRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }
}

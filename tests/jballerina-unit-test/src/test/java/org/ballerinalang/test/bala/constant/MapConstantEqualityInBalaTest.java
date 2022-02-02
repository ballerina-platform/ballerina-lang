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

package org.ballerinalang.test.bala.constant;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constant test cases.
 */
public class MapConstantEqualityInBalaTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        compileResult = BCompileUtil.compile("test-src/bala/test_bala/constant/map-literal-constant-equality.bal");
    }

    // boolean type.

    @Test
    public void testSimpleBooleanMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleBooleanMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapValueEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleBooleanMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleBooleanMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleBooleanMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexBooleanMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexBooleanMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapValueEqualityUsingDifferentReference() {
        Object returns =
                JvmRunUtil.invoke(compileResult, "testComplexBooleanMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexBooleanMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapReferenceEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexBooleanMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexBooleanMapValueEqualityUsingMapAccessSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexBooleanMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexBooleanMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexBooleanMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexBooleanMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleBooleanMapValueEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleBooleanMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleBooleanMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleBooleanMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleBooleanMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexBooleanMapValueEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexBooleanMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapReferenceEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexBooleanMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapValueEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleBooleanMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleBooleanMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // int type.

    @Test
    public void testSimpleIntMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleIntMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapValueEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleIntMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleIntMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapReferenceEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleIntMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexIntMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexIntMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapValueEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexIntMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexIntMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapReferenceEqualityUsingDifferentReference() {
        Object returns =
                JvmRunUtil.invoke(compileResult, "testComplexIntMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexIntMapValueEqualityUsingMapAccessSameReference() {
        Object returns =
                JvmRunUtil.invoke(compileResult, "testComplexIntMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexIntMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexIntMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexIntMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleIntMapValueEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleIntMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleIntMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleIntMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleIntMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexIntMapValueEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexIntMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapReferenceEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexIntMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapValueEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleIntMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapReferenceEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleIntMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // byte type.

    @Test
    public void testSimpleByteMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleByteMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapValueEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleByteMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleByteMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapReferenceEqualityUsingDifferentReference() {
        Object returns =
                JvmRunUtil.invoke(compileResult, "testSimpleByteMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexByteMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexByteMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapValueEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexByteMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexByteMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapReferenceEqualityUsingDifferentReference() {
        Object returns =
                JvmRunUtil.invoke(compileResult, "testComplexByteMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexByteMapValueEqualityUsingMapAccessSameReference() {
        Object returns =
                JvmRunUtil.invoke(compileResult, "testComplexByteMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexByteMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexByteMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexByteMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleByteMapValueEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleByteMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleByteMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleByteMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleByteMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexByteMapValueEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexByteMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapReferenceEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexByteMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapValueEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleByteMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapReferenceEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleByteMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // float type.

    @Test
    public void testSimpleFloatMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleFloatMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapValueEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleFloatMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleFloatMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityUsingDifferentReference() {
        Object returns =
                JvmRunUtil.invoke(compileResult, "testSimpleFloatMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexFloatMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexFloatMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapValueEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexFloatMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexFloatMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapReferenceEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexFloatMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexFloatMapValueEqualityUsingMapAccessSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexFloatMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexFloatMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexFloatMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexFloatMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleFloatMapValueEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleFloatMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleFloatMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleFloatMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleFloatMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexFloatMapValueEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexFloatMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapReferenceEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexFloatMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapValueEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleFloatMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleFloatMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // decimal type.

    @Test
    public void testSimpleDecimalMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleDecimalMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapValueEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleDecimalMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleDecimalMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleDecimalMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexDecimalMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexDecimalMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapValueEqualityUsingDifferentReference() {
        Object returns =
                JvmRunUtil.invoke(compileResult, "testComplexDecimalMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexDecimalMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapReferenceEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexDecimalMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexDecimalMapValueEqualityUsingMapAccessSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexDecimalMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexDecimalMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexDecimalMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexDecimalMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleDecimalMapValueEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleDecimalMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleDecimalMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleDecimalMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleDecimalMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexDecimalMapValueEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexDecimalMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapReferenceEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexDecimalMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapValueEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleDecimalMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleDecimalMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // string type.

    @Test
    public void testSimpleStringMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleStringMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapValueEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleStringMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleStringMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapReferenceEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleStringMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexStringMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexStringMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapValueEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexStringMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexStringMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapReferenceEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexStringMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexStringMapValueEqualityUsingMapAccessSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexStringMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexStringMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexStringMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexStringMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleStringMapValueEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleStringMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleStringMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleStringMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleStringMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexStringMapValueEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexStringMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapReferenceEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexStringMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapValueEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleStringMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapReferenceEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleStringMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // nil type.

    @Test
    public void testSimpleNilMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleNilMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapValueEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleNilMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleNilMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapReferenceEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleNilMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexNilMapValueEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexNilMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapValueEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexNilMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapReferenceEqualityUsingSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexNilMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapReferenceEqualityUsingDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexNilMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexNilMapValueEqualityUsingMapAccessSameReference() {
        Object returns =
                JvmRunUtil.invoke(compileResult, "testComplexNilMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexNilMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexNilMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testComplexNilMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleNilMapValueEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleNilMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleNilMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleNilMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = JvmRunUtil.invoke(compileResult,
                "testSimpleNilMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexNilMapValueEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexNilMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapReferenceEquality() {
        Object returns = JvmRunUtil.invoke(compileResult, "testComplexNilMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapValueEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleNilMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapReferenceEqualityInDifferentMap() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSimpleNilMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

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
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
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
        Object returns = BRunUtil.invoke(compileResult, "testSimpleBooleanMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapValueEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleBooleanMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleBooleanMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexBooleanMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexBooleanMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapValueEqualityUsingDifferentReference() {
        Object returns =
                BRunUtil.invoke(compileResult, "testComplexBooleanMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexBooleanMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapReferenceEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexBooleanMapValueEqualityUsingMapAccessSameReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleBooleanMapValueEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleBooleanMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexBooleanMapValueEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexBooleanMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexBooleanMapReferenceEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexBooleanMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapValueEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleBooleanMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleBooleanMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // int type.

    @Test
    public void testSimpleIntMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleIntMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapValueEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleIntMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleIntMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapReferenceEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleIntMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexIntMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexIntMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapValueEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexIntMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexIntMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapReferenceEqualityUsingDifferentReference() {
        Object returns =
                BRunUtil.invoke(compileResult, "testComplexIntMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexIntMapValueEqualityUsingMapAccessSameReference() {
        Object returns =
                BRunUtil.invoke(compileResult, "testComplexIntMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexIntMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexIntMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexIntMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleIntMapValueEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleIntMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleIntMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleIntMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleIntMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexIntMapValueEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexIntMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexIntMapReferenceEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexIntMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapValueEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleIntMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleIntMapReferenceEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleIntMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // byte type.

    @Test
    public void testSimpleByteMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleByteMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapValueEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleByteMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleByteMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapReferenceEqualityUsingDifferentReference() {
        Object returns =
                BRunUtil.invoke(compileResult, "testSimpleByteMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexByteMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexByteMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapValueEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexByteMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexByteMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapReferenceEqualityUsingDifferentReference() {
        Object returns =
                BRunUtil.invoke(compileResult, "testComplexByteMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexByteMapValueEqualityUsingMapAccessSameReference() {
        Object returns =
                BRunUtil.invoke(compileResult, "testComplexByteMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexByteMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexByteMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexByteMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleByteMapValueEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleByteMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleByteMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleByteMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleByteMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexByteMapValueEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexByteMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexByteMapReferenceEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexByteMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapValueEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleByteMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleByteMapReferenceEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleByteMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // float type.

    @Test
    public void testSimpleFloatMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapValueEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityUsingDifferentReference() {
        Object returns =
                BRunUtil.invoke(compileResult, "testSimpleFloatMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexFloatMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexFloatMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapValueEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexFloatMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexFloatMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapReferenceEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexFloatMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexFloatMapValueEqualityUsingMapAccessSameReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexFloatMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexFloatMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexFloatMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexFloatMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleFloatMapValueEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleFloatMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleFloatMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleFloatMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexFloatMapValueEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexFloatMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexFloatMapReferenceEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexFloatMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapValueEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // decimal type.

    @Test
    public void testSimpleDecimalMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleDecimalMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapValueEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleDecimalMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleDecimalMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexDecimalMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexDecimalMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapValueEqualityUsingDifferentReference() {
        Object returns =
                BRunUtil.invoke(compileResult, "testComplexDecimalMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexDecimalMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapReferenceEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexDecimalMapValueEqualityUsingMapAccessSameReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleDecimalMapValueEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleDecimalMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexDecimalMapValueEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexDecimalMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexDecimalMapReferenceEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexDecimalMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapValueEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleDecimalMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleDecimalMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // string type.

    @Test
    public void testSimpleStringMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleStringMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapValueEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleStringMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleStringMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapReferenceEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleStringMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexStringMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexStringMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapValueEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexStringMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexStringMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapReferenceEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexStringMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexStringMapValueEqualityUsingMapAccessSameReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexStringMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexStringMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexStringMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexStringMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleStringMapValueEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleStringMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleStringMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleStringMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleStringMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexStringMapValueEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexStringMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexStringMapReferenceEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexStringMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapValueEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleStringMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleStringMapReferenceEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleStringMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // nil type.

    @Test
    public void testSimpleNilMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleNilMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapValueEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleNilMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleNilMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapReferenceEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleNilMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexNilMapValueEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexNilMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapValueEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexNilMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapReferenceEqualityUsingSameReference() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexNilMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapReferenceEqualityUsingDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexNilMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexNilMapValueEqualityUsingMapAccessSameReference() {
        Object returns =
                BRunUtil.invoke(compileResult, "testComplexNilMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapValueEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexNilMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapReferenceEqualityUsingMapAccessSameReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexNilMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapReferenceEqualityUsingMapAccessDifferentReference() {
        Object returns = BRunUtil.invoke(compileResult,
                "testComplexNilMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleNilMapValueEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleNilMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapValueEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleNilMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapReferenceEqualityUsingSameReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleNilMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapReferenceEqualityUsingDifferentReferenceInMap() {
        Object returns = BRunUtil.invoke(compileResult,
                "testSimpleNilMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexNilMapValueEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexNilMapValueEquality");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testComplexNilMapReferenceEquality() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexNilMapReferenceEquality");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapValueEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleNilMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testSimpleNilMapReferenceEqualityInDifferentMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleNilMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

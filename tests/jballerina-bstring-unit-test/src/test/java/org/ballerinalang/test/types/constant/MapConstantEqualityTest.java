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

package org.ballerinalang.test.types.constant;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constant test cases.
 */
public class MapConstantEqualityTest {

    private static CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/map-literal-constant-equality.bal");
    }

    // boolean type.

    @Test
    public void testSimpleBooleanMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleBooleanMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleBooleanMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleBooleanMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexBooleanMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexBooleanMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexBooleanMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexBooleanMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexBooleanMapValueEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanMapValueEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanMapReferenceEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanMapReferenceEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexBooleanMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleBooleanMapValueEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleBooleanMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanMapValueEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleBooleanMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexBooleanMapValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexBooleanMapValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexBooleanMapReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexBooleanMapReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanMapValueEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleBooleanMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleBooleanMapReferenceEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleBooleanMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // int type.

    @Test
    public void testSimpleIntMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexIntMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexIntMapValueEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntMapValueEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexIntMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntMapReferenceEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexIntMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntMapReferenceEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexIntMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleIntMapValueEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntMapValueEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleIntMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntMapReferenceEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleIntMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntMapReferenceEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleIntMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexIntMapValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntMapValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexIntMapReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntMapReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntMapValueEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleIntMapReferenceEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // byte type.

    @Test
    public void testSimpleByteMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexByteMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexByteMapValueEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteMapValueEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexByteMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteMapReferenceEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexByteMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteMapReferenceEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexByteMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleByteMapValueEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteMapValueEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleByteMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteMapReferenceEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleByteMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteMapReferenceEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleByteMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexByteMapValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteMapValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexByteMapReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteMapReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteMapValueEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleByteMapReferenceEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // float type.

    @Test
    public void testSimpleFloatMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexFloatMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexFloatMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexFloatMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexFloatMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexFloatMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexFloatMapValueEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexFloatMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatMapValueEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexFloatMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatMapReferenceEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexFloatMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatMapReferenceEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexFloatMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleFloatMapValueEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatMapValueEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleFloatMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleFloatMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleFloatMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexFloatMapValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexFloatMapValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexFloatMapReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexFloatMapReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatMapValueEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleFloatMapReferenceEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // decimal type.

    @Test
    public void testSimpleDecimalMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleDecimalMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleDecimalMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleDecimalMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexDecimalMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexDecimalMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexDecimalMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexDecimalMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexDecimalMapValueEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalMapValueEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalMapReferenceEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalMapReferenceEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexDecimalMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleDecimalMapValueEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleDecimalMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalMapValueEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleDecimalMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexDecimalMapValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexDecimalMapValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexDecimalMapReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexDecimalMapReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalMapValueEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleDecimalMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleDecimalMapReferenceEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleDecimalMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // string type.

    @Test
    public void testSimpleStringMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleStringMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleStringMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleStringMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleStringMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexStringMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexStringMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexStringMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexStringMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexStringMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexStringMapValueEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexStringMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringMapValueEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexStringMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringMapReferenceEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexStringMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringMapReferenceEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexStringMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleStringMapValueEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleStringMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringMapValueEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleStringMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringMapReferenceEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleStringMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringMapReferenceEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleStringMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexStringMapValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexStringMapValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexStringMapReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexStringMapReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringMapValueEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleStringMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleStringMapReferenceEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleStringMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // nil type.

    @Test
    public void testSimpleNilMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexNilMapValueEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexNilMapValueEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilMapValueEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexNilMapValueEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilMapReferenceEqualityUsingSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexNilMapReferenceEqualityUsingSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilMapReferenceEqualityUsingDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexNilMapReferenceEqualityUsingDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexNilMapValueEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexNilMapValueEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilMapValueEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexNilMapValueEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilMapReferenceEqualityUsingMapAccessSameReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexNilMapReferenceEqualityUsingMapAccessSameReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilMapReferenceEqualityUsingMapAccessDifferentReference() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testComplexNilMapReferenceEqualityUsingMapAccessDifferentReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testSimpleNilMapValueEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilMapValueEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilMapValueEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleNilMapValueEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilMapReferenceEqualityUsingSameReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleNilMapReferenceEqualityUsingSameReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilMapReferenceEqualityUsingDifferentReferenceInMap() {
        BValue[] returns = BRunUtil.invoke(compileResult,
                "testSimpleNilMapReferenceEqualityUsingDifferentReferenceInMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    // -----------------------------------------------------------

    @Test
    public void testComplexNilMapValueEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexNilMapValueEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testComplexNilMapReferenceEquality() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexNilMapReferenceEquality");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilMapValueEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilMapValueEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testSimpleNilMapReferenceEqualityInDifferentMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilMapReferenceEqualityInDifferentMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }
}

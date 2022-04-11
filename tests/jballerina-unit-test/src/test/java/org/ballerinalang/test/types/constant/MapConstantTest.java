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

import io.ballerina.runtime.api.creators.ValueCreator;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Constant test cases.
 */
public class MapConstantTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/map-literal-constant.bal");
    }

    @Test
    public void testSimpleBooleanConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleBooleanConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key1\":true,\"key2\":false}");
    }

    @Test
    public void testComplexBooleanConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexBooleanConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key5\":{\"key1\":true,\"key2\":false},\"key6\":{\"key3\":false," +
                "\"key4\":true},\"key7\":{\"key8\":true,\"key9\":false}}");
    }

    @Test
    public void testSimpleIntConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleIntConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key1\":1,\"key2\":2}");
    }

    @Test
    public void testComplexIntConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexIntConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key5\":{\"key1\":1,\"key2\":2},\"key6\":{\"key3\":3,\"key4\":4}," +
                "\"key7:\":{\"key8\":8,\"key9\":9}}");
    }

    @Test
    public void testSimpleByteConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleByteConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key1\":10,\"key2\":20}");
    }

    @Test
    public void testComplexByteConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexByteConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key5\":{\"key1\":10,\"key2\":20},\"key6\":{\"key3\":30," +
                "\"key4\":40},\"key7\":{\"key8\":80,\"key9\":90}}");
    }

    @Test
    public void testSimpleDecimalConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleDecimalConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key1\":100,\"key2\":200}");
    }

    @Test
    public void testComplexDecimalConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexDecimalConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key5\":{\"key1\":100,\"key2\":200},\"key6\":{\"key3\":300," +
                "\"key4\":400},\"key7\":{\"key8\":800,\"key9\":900}}");
    }

    @Test
    public void testSimpleFloatConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleFloatConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key1\":1.0,\"key2\":2.0}");
    }

    @Test
    public void testComplexFloatConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexFloatConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key5\":{\"key1\":1.0,\"key2\":2.0},\"key6\":{\"key3\":3.0," +
                "\"key4\":4.0},\"key7\":{\"key8\":8.0,\"key9\":9.0}}");
    }

    @Test
    public void testSimpleStringConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleStringConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key1\":\"value1\",\"key2\":\"value2\"}");
    }

    @Test
    public void testComplexStringConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexStringConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key5\":{\"key1\":\"value1\",\"key2\":\"value2\"}," +
                "\"key6\":{\"key3\":\"value3\",\"key4\":\"value4\"},\"key7\":{\"key8\":\"value8\"," +
                "\"key9\":\"value9\"}}");
    }

    @Test
    public void testSimpleNilConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleNilConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key1\":null,\"key2\":null}");
    }

    @Test
    public void testComplexNilConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexNilConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"key5\":{\"key1\":null,\"key2\":null},\"key6\":{\"key3\":null," +
                "\"key4\":null},\"key7\":{\"key8\":null,\"key9\":null}}");
    }

    @Test
    public void testComplexConstMap() {
        Object returns = BRunUtil.invoke(compileResult, "testComplexConstMap");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"k3\":{\"k2\":{\"k1\":\"v1\"}}}");
    }

    @Test
    public void testBooleanMapConstMemberAccess() {
        Object returns = BRunUtil.invoke(compileResult, "testBooleanMapConstMemberAccess");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"bm5kn\":true}");
    }

    @Test
    public void testIntMapConstMemberAccess() {
        Object returns = BRunUtil.invoke(compileResult, "testIntMapConstMemberAccess");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"im5kn\":123}");
    }

    @Test
    public void testByteMapConstMemberAccess() {
        Object returns = BRunUtil.invoke(compileResult, "testByteMapConstMemberAccess");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"bytem5kn\":64}");
    }

    @Test
    public void testFloatMapConstMemberAccess() {
        Object returns = BRunUtil.invoke(compileResult, "testFloatMapConstMemberAccess");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"fm5kn\":12.5}");
    }

    @Test
    public void testDecimalMapConstMemberAccess() {
        Object returns = BRunUtil.invoke(compileResult, "testDecimalMapConstMemberAccess");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"dm5kn\":5.56}");
    }

    @Test
    public void testStringMapConstMemberAccess() {
        Object returns = BRunUtil.invoke(compileResult, "testStringMapConstMemberAccess");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"sm5kn\":\"sm4v\"}");
    }

    @Test
    public void testNilMapConstMemberAccess() {
        Object returns = BRunUtil.invoke(compileResult, "testNilMapConstMemberAccess");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"nm5kn\":null}");
    }

    @Test
    public void testBooleanConstMemberAccessInLocalVar() {
        Object returns = BRunUtil.invoke(compileResult, "testBooleanConstMemberAccessInLocalVar");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testIntConstMemberAccessInLocalVar() {
        Object returns = BRunUtil.invoke(compileResult, "testIntConstMemberAccessInLocalVar");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 123L);
    }

    @Test
    public void testByteConstMemberAccessInLocalVar() {
        Object returns = BRunUtil.invoke(compileResult, "testByteConstMemberAccessInLocalVar");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 64);
    }

    @Test
    public void testFloatConstMemberAccessInLocalVar() {
        Object returns = BRunUtil.invoke(compileResult, "testFloatConstMemberAccessInLocalVar");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 12.5);
    }

    @Test
    public void testDecimalConstMemberAccessInLocalVar() {
        Object returns = BRunUtil.invoke(compileResult, "testDecimalConstMemberAccessInLocalVar");
        Assert.assertNotNull(returns);
        BigDecimal expected = new BigDecimal("5.56", MathContext.DECIMAL128);
        Assert.assertEquals(returns, ValueCreator.createDecimalValue(expected));
    }

    @Test
    public void testStringConstMemberAccessInLocalVar() {
        Object returns = BRunUtil.invoke(compileResult, "testStringConstMemberAccessInLocalVar");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "sm4v");
    }

    @Test
    public void testNullConstMemberAccessInLocalVar() {
        Object returns = BRunUtil.invoke(compileResult, "testNullConstMemberAccessInLocalVar");
        Assert.assertNull(returns);
    }

    @Test
    public void testConstInAnnotations() {
        Object returns = BRunUtil.invoke(compileResult, "testConstInAnnotations");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "{\"s\":\"Ballerina\",\"i\":100,\"m\":{\"mKey\":\"mValue\"}}");
    }

    @Test
    public void testNestedConstMapAccess() {
        Object returns = BRunUtil.invoke(compileResult, "testNestedConstMapAccess");
        Assert.assertTrue((Boolean) returns);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

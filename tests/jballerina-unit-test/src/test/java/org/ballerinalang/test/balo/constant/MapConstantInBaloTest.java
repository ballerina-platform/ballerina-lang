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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Test cases for reading constants.
 */
public class MapConstantInBaloTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "foo");
        compileResult = BCompileUtil.compile("test-src/balo/test_balo/constant/map-literal-constant.bal");
    }

    @Test
    public void testSimpleBooleanConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleBooleanConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key1\":true, \"key2\":false}");
    }

    @Test
    public void testComplexBooleanConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexBooleanConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key5\":{\"key1\":true, \"key2\":false}, " +
                "\"key6\":{\"key3\":false, \"key4\":true}, \"key7\":{\"key8\":true, \"key9\":false}}");
    }

    @Test
    public void testSimpleIntConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleIntConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key1\":1, \"key2\":2}");
    }

    @Test
    public void testComplexIntConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexIntConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key5\":{\"key1\":1, \"key2\":2}, \"key6\":{\"key3\":3, " +
                "\"key4\":4}, \"key7:\":{\"key8\":8, \"key9\":9}}");
    }

    @Test
    public void testSimpleByteConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleByteConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key1\":10, \"key2\":20}");
    }

    @Test
    public void testComplexByteConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexByteConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key5\":{\"key1\":10, \"key2\":20}, \"key6\":{\"key3\":30, " +
                "\"key4\":40}, \"key7\":{\"key8\":80, \"key9\":90}}");
    }

    @Test
    public void testSimpleDecimalConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleDecimalConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key1\":100, \"key2\":200}");
    }

    @Test
    public void testComplexDecimalConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexDecimalConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key5\":{\"key1\":100, \"key2\":200}, " +
                "\"key6\":{\"key3\":300, \"key4\":400}, \"key7\":{\"key8\":800, \"key9\":900}}");
    }

    @Test
    public void testSimpleFloatConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleFloatConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key1\":1.0, \"key2\":2.0}");
    }

    @Test
    public void testComplexFloatConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexFloatConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key5\":{\"key1\":1.0, \"key2\":2.0}, \"key6\":{\"key3\":3" +
                ".0, \"key4\":4.0}, \"key7\":{\"key8\":8.0, \"key9\":9.0}}");
    }

    @Test
    public void testSimpleStringConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleStringConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key1\":\"value1\", \"key2\":\"value2\"}");
    }

    @Test
    public void testComplexStringConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexStringConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key5\":{\"key1\":\"value1\", \"key2\":\"value2\"}, " +
                "\"key6\":{\"key3\":\"value3\", \"key4\":\"value4\"}, \"key7\":{\"key8\":\"value8\", " +
                "\"key9\":\"value9\"}}");
    }

    @Test
    public void testSimpleNilConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleNilConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key1\":(), \"key2\":()}");
    }

    @Test
    public void testComplexNilConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexNilConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"key5\":{\"key1\":(), \"key2\":()}, \"key6\":{\"key3\":(), " +
                "\"key4\":()}, \"key7\":{\"key8\":(), \"key9\":()}}");
    }

    @Test
    public void testComplexConstMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testComplexConstMap");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"k3\":{\"k2\":{\"k1\":\"v1\"}}}");
    }

    @Test
    public void testBooleanConstKeyReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanConstKeyReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"bm5kn\":true}");
    }

    @Test
    public void testIntConstKeyReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntConstKeyReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"im5kn\":123}");
    }

    @Test
    public void testByteConstKeyReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteConstKeyReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"bytem5kn\":64}");
    }

    @Test
    public void testFloatConstKeyReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatConstKeyReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"fm5kn\":12.5}");
    }

    @Test
    public void testDecimalConstKeyReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDecimalConstKeyReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"dm5kn\":5.56}");
    }

    @Test
    public void testStringConstKeyReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringConstKeyReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"sm5kn\":\"sm4v\"}");
    }

    @Test
    public void testNullConstKeyReference() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNullConstKeyReference");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{\"nm5kn\":()}");
    }

    @Test
    public void testBooleanConstKeyReferenceInLocalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanConstKeyReferenceInLocalVar");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testIntConstKeyReferenceInLocalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntConstKeyReferenceInLocalVar");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 123);
    }

    @Test
    public void testByteConstKeyReferenceInLocalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteConstKeyReferenceInLocalVar");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BByte) returns[0]).intValue(), 64);
    }

    @Test
    public void testFloatConstKeyReferenceInLocalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatConstKeyReferenceInLocalVar");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 12.5);
    }

    @Test
    public void testDecimalConstKeyReferenceInLocalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDecimalConstKeyReferenceInLocalVar");
        Assert.assertNotNull(returns[0]);
        BigDecimal expected = new BigDecimal("5.56", MathContext.DECIMAL128);
        Assert.assertEquals(((BDecimal) returns[0]).decimalValue().compareTo(expected), 0);
    }

    @Test
    public void testStringConstKeyReferenceInLocalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringConstKeyReferenceInLocalVar");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "sm4v");
    }

    @Test
    public void testNullConstKeyReferenceInLocalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNullConstKeyReferenceInLocalVar");
        Assert.assertNull(returns[0]);
    }

    // Annotation tests

    @Test
    public void testConstInAnnotations() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstInAnnotations");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{s:\"Ballerina\", i:100, m:{\"mKey\":\"mValue\"}}");
    }
}

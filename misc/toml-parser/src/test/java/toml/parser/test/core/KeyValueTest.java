/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package toml.parser.test.core;

import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlBooleanValueNode;
import io.ballerina.toml.semantic.ast.TomlDoubleValueNodeNode;
import io.ballerina.toml.semantic.ast.TomlLongValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Basic Test for TOML Key Value Pairs.
 */
public class KeyValueTest {

    @Test
    public void testKeys() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/core/keys.toml");
        Toml read = Toml.read(inputStream);
        String basicKey = ((TomlStringValueNode) read.get("key")).getValue();
        String underscoreKey = ((TomlStringValueNode) read.get("underscore_key")).getValue();
        String dashKey = ((TomlStringValueNode) read.get("dash-key")).getValue();
        String intKey = ((TomlStringValueNode) read.get("1234")).getValue();

        String dottedString =
                ((TomlStringValueNode) read.get("127.0.0.1")).getValue();
        String spacing = ((TomlStringValueNode) read.get("character encoding")).getValue();
        String unicode = ((TomlStringValueNode) read.get("ʎǝʞ")).getValue();
        String general = ((TomlStringValueNode) read.get("key2")).getValue();
        String escape = ((TomlStringValueNode) read.get("quoted \"value\"")).getValue();

        Assert.assertEquals(basicKey, "basic key");
        Assert.assertEquals(underscoreKey, "Underscore Key");
        Assert.assertEquals(dashKey, "Dash Key");
        Assert.assertEquals(intKey, "Int Key");

        Assert.assertEquals(dottedString, "value");
        Assert.assertEquals(spacing, "value");
        Assert.assertEquals(unicode, "value");
        Assert.assertEquals(general, "value");
        Assert.assertEquals(escape, "value");
    }

    @Test
    public void testBasicValues() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/core/values.toml");
        Toml read = Toml.read(inputStream);
        String stringValue = ((TomlStringValueNode) read.get("key1")).getValue();
        Long longValue = ((TomlLongValueNode) read.get("key2")).getValue();
        Double doubleValue = ((TomlDoubleValueNodeNode) read.get("key3")).getValue();
        String multiString = ((TomlStringValueNode) read.get("key4")).getValue();
        boolean boolfalse = ((TomlBooleanValueNode) read.get("key6")).getValue();
        boolean booltrue = ((TomlBooleanValueNode) read.get("key7")).getValue();

        Assert.assertEquals(stringValue, "hello");
        Assert.assertEquals(longValue, Long.valueOf(123L));
        Assert.assertEquals(doubleValue, 56.55);
        Assert.assertEquals(multiString, "\n" +
                "Roses are red\n" +
                "Violets are blue");
        Assert.assertFalse(boolfalse);
        Assert.assertTrue(booltrue);
    }

    @Test
    public void testArrayValues() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/core/array.toml");
        Toml read = Toml.read(inputStream);

        TomlArrayValueNode mixedType = read.get("mixed_type");
        Long first = ((TomlLongValueNode) mixedType.get(0)).getValue();
        Long second = ((TomlLongValueNode) mixedType.get(1)).getValue();
        String third = ((TomlStringValueNode) mixedType.get(2)).getValue();
        Long fourth = ((TomlLongValueNode) mixedType.get(3)).getValue();

        Assert.assertEquals(first, Long.valueOf(1L));
        Assert.assertEquals(second, Long.valueOf(2L));
        Assert.assertEquals(fourth, Long.valueOf(3L));
        Assert.assertEquals(third, "test string");

        TomlArrayValueNode nestedArray = read.get("nested_array");
        Long nfirst = ((TomlLongValueNode) nestedArray.get(0)).getValue();
        Long nsecond = ((TomlLongValueNode) nestedArray.get(1)).getValue();
        Long nfourth = ((TomlLongValueNode) nestedArray.get(3)).getValue();
        TomlArrayValueNode childArray = nestedArray.get(2);
        Long cfirst = ((TomlLongValueNode) childArray.get(0)).getValue();
        Long csecond = ((TomlLongValueNode) childArray.get(1)).getValue();

        Assert.assertEquals(nfirst, Long.valueOf(1L));
        Assert.assertEquals(nsecond, Long.valueOf(2L));
        Assert.assertEquals(nfourth, Long.valueOf(3L));

        Assert.assertEquals(cfirst, Long.valueOf(5L));
        Assert.assertEquals(csecond, Long.valueOf(6L));
    }
}

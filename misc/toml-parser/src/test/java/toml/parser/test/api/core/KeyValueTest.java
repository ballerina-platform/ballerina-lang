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

package toml.parser.test.api.core;

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
                .getResourceAsStream("syntax/key-value/keys.toml");

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
                .getResourceAsStream("syntax/key-value/values.toml");
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
        Assert.assertEquals(multiString,
                String.format("Roses are red%sViolets are blue", System.lineSeparator()));
        Assert.assertFalse(boolfalse);
        Assert.assertTrue(booltrue);

        Long int1 = ((TomlLongValueNode) read.get("int1")).getValue();
        Assert.assertEquals(int1, Long.valueOf(99L));
        Long int2 = ((TomlLongValueNode) read.get("int2")).getValue();
        Assert.assertEquals(int2, Long.valueOf(42L));
        Long int3 = ((TomlLongValueNode) read.get("int3")).getValue();
        Assert.assertEquals(int3, Long.valueOf(0L));
        Long int4 = ((TomlLongValueNode) read.get("int4")).getValue();
        Assert.assertEquals(int4, Long.valueOf(-17L));

        Long int5 = ((TomlLongValueNode) read.get("int5")).getValue();
        Assert.assertEquals(int5, Long.valueOf(1000L));
        Long int6 = ((TomlLongValueNode) read.get("int6")).getValue();
        Assert.assertEquals(int6, Long.valueOf(5349221L));
        Long int7 = ((TomlLongValueNode) read.get("int7")).getValue();
        Assert.assertEquals(int7, Long.valueOf(5349221L));
        Long int8 = ((TomlLongValueNode) read.get("int8")).getValue();
        Assert.assertEquals(int8, Long.valueOf(12345L));

        Long hex1 = ((TomlLongValueNode) read.get("hex1")).getValue();
        Assert.assertEquals(hex1, Long.valueOf(3735928559L));
        Long hex2 = ((TomlLongValueNode) read.get("hex2")).getValue();
        Assert.assertEquals(hex2, Long.valueOf(3735928559L));
        Long hex3 = ((TomlLongValueNode) read.get("hex3")).getValue();
        Assert.assertEquals(hex3, Long.valueOf(3735928559L));

        Long oct1 = ((TomlLongValueNode) read.get("oct1")).getValue();
        Assert.assertEquals(oct1, Long.valueOf(342391L));
        Long oct2 = ((TomlLongValueNode) read.get("oct2")).getValue();
        Assert.assertEquals(oct2, Long.valueOf(493L));

        Long bin1 = ((TomlLongValueNode) read.get("bin1")).getValue();
        Assert.assertEquals(bin1, Long.valueOf(214L));

        Double flt1 = ((TomlDoubleValueNodeNode) read.get("flt1")).getValue();
        Assert.assertEquals(flt1, 1.0);
        Double flt2 = ((TomlDoubleValueNodeNode) read.get("flt2")).getValue();
        Assert.assertEquals(flt2, 3.1415);
        Double flt3 = ((TomlDoubleValueNodeNode) read.get("flt3")).getValue();
        Assert.assertEquals(flt3, -0.01);

        Double flt4 = ((TomlDoubleValueNodeNode) read.get("flt4")).getValue();
        Assert.assertEquals(flt4, 5e+22);
        Double flt5 = ((TomlDoubleValueNodeNode) read.get("flt5")).getValue();
        Assert.assertEquals(flt5, 1e06);
        Double flt6 = ((TomlDoubleValueNodeNode) read.get("flt6")).getValue();
        Assert.assertEquals(flt6, -2E-2);

        Double flt7 = ((TomlDoubleValueNodeNode) read.get("flt7")).getValue();
        Assert.assertEquals(flt7, 6.626e-34);

        Double flt8 = ((TomlDoubleValueNodeNode) read.get("flt8")).getValue();
        Assert.assertEquals(flt8, 224617.445991228);

        String stringEscape = ((TomlStringValueNode) read.get("str0")).getValue();
        Assert.assertEquals(stringEscape, "I'm a string. \"You can quote me\". Name\tJosé\n" +
                "Location\tSF.");

        String basicString = ((TomlStringValueNode) read.get("str1")).getValue();
        Assert.assertEquals(basicString, "The quick brown fox jumps over the lazy dog.");
        String backslashWithWhitespace = ((TomlStringValueNode) read.get("str2")).getValue();
        Assert.assertEquals(backslashWithWhitespace, "The quick brown fox jumps over the lazy dog.");
        String multilineBasklash = ((TomlStringValueNode) read.get("str3")).getValue();
        Assert.assertEquals(multilineBasklash, "The quick brown fox jumps over the lazy dog.");

        String twoEscapes = ((TomlStringValueNode) read.get("str4")).getValue();
        Assert.assertEquals(twoEscapes, "Here are two quotation marks: \"\". Simple enough.");
        String threeQuotesEscapes = ((TomlStringValueNode) read.get("str5")).getValue();
        Assert.assertEquals(threeQuotesEscapes, "Here are three quotation marks: \"\"\".");
        String fifteenQuotes = ((TomlStringValueNode) read.get("str6")).getValue();
        Assert.assertEquals(fifteenQuotes, "Here are fifteen quotation marks: \"\"\"\"\"\"\"\"\"\"\"\"\"\"\".");
        String mixedEscape = ((TomlStringValueNode) read.get("str7")).getValue();
        Assert.assertEquals(mixedEscape, "\"This,\" she said, \"is just a pointless statement.");

        String absPathLiteralString = ((TomlStringValueNode) read.get("lit1")).getValue();
        Assert.assertEquals(absPathLiteralString, "C:\\Users\\nodejs\\templates");
        String windowsPathLiteralString = ((TomlStringValueNode) read.get("lit2")).getValue();
        Assert.assertEquals(windowsPathLiteralString, "\\\\ServerX\\admin$\\system32\\");
        String doubeQuoteLiteralString = ((TomlStringValueNode) read.get("lit3")).getValue();
        Assert.assertEquals(doubeQuoteLiteralString, "Tom \"Dubs\" Preston-Werner");
        String regexLiteralString = ((TomlStringValueNode) read.get("lit4")).getValue();
        Assert.assertEquals(regexLiteralString, "<\\i\\c*\\s*>");
        String regex2LiteralString = ((TomlStringValueNode) read.get("lit5")).getValue();
        Assert.assertEquals(regex2LiteralString, "I [dw]on't need \\d{2} apples");

        String multilineLiteralString = ((TomlStringValueNode) read.get("lit6")).getValue();
        Assert.assertEquals(multilineLiteralString, String.format(
                "The first newline is%strimmed in raw strings.%s   All other whitespace%s   is preserved.%s",
                System.lineSeparator(), System.lineSeparator(), System.lineSeparator(), System.lineSeparator()));
        String fifteenQuotesWithLiteral = ((TomlStringValueNode) read.get("lit7")).getValue();
        Assert.assertEquals(fifteenQuotesWithLiteral,
                "Here are fifteen quotation marks: \"\"\"\"\"\"\"\"\"\"\"\"\"\"\"");
        String fifteenSingleQuotes = ((TomlStringValueNode) read.get("lit8")).getValue();
        Assert.assertEquals(fifteenSingleQuotes, "Here are fifteen apostrophes: '''''''''''''''");
    }

    @Test
    public void testArrayValues() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/key-value/array.toml");
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

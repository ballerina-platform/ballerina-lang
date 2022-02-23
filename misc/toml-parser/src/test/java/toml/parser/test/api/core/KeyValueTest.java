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
import io.ballerina.toml.semantic.ast.TomlInlineTableValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlLongValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.tools.text.LineRange;
import org.testng.Assert;
import org.testng.annotations.Test;
import toml.parser.test.ParserTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Basic Test for TOML Key Value Pairs.
 */
public class KeyValueTest {

    @Test
    public void testKeys() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/key-value/keys.toml");

        Toml read = Toml.read(inputStream);
        String basicKey = ((TomlStringValueNode) read.get("key").get()).getValue();
        String underscoreKey = ((TomlStringValueNode) read.get("underscore_key").get()).getValue();
        String dashKey = ((TomlStringValueNode) read.get("dash-key").get()).getValue();
        String intKey = ((TomlStringValueNode) read.get("1234").get()).getValue();

        String dottedString =
                ((TomlStringValueNode) read.get("\"127.0.0.1\"").get()).getValue();
        String spacing = ((TomlStringValueNode) read.get("\"character encoding\"").get()).getValue();
        String unicode = ((TomlStringValueNode) read.get("\"ʎǝʞ\"").get()).getValue();
        String general = ((TomlStringValueNode) read.get("\"key2\"").get()).getValue();
        String escape = ((TomlStringValueNode) read.get("\"quoted \"value\"\"").get()).getValue();

        Assert.assertEquals(basicKey, "basic key");
        Assert.assertEquals(underscoreKey, "Underscore Key");
        Assert.assertEquals(dashKey, "Dash Key");
        Assert.assertEquals(intKey, "Int Key");

        Assert.assertEquals(dottedString, "value");
        Assert.assertEquals(spacing, "value");
        Assert.assertEquals(unicode, "value");
        Assert.assertEquals(general, "value");
        Assert.assertEquals(escape, "value");

        LineRange lineRange = read.rootNode().entries().get("key").location().lineRange();
        ParserTestUtils.assertLineRange(lineRange, 18, 0, 18, 17);
    }

    @Test
    public void testBasicValues() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/key-value/values.toml");
        Toml read = Toml.read(inputStream);
        String stringValue = ((TomlStringValueNode) read.get("key1").get()).getValue();
        Long longValue = ((TomlLongValueNode) read.get("key2").get()).getValue();
        Double doubleValue = ((TomlDoubleValueNodeNode) read.get("key3").get()).getValue();
        String multiString = ((TomlStringValueNode) read.get("key4").get()).getValue();
        boolean boolfalse = ((TomlBooleanValueNode) read.get("key6").get()).getValue();
        boolean booltrue = ((TomlBooleanValueNode) read.get("key7").get()).getValue();

        Assert.assertEquals(stringValue, "hello");
        Assert.assertEquals(longValue, Long.valueOf(123L));
        Assert.assertEquals(doubleValue, Double.valueOf(56.55));
        Assert.assertEquals(multiString,
                String.format("Roses are red%sViolets are blue", System.lineSeparator()));
        Assert.assertFalse(boolfalse);
        Assert.assertTrue(booltrue);

        Long int1 = ((TomlLongValueNode) read.get("int1").get()).getValue();
        Assert.assertEquals(int1, Long.valueOf(99L));
        Long int2 = ((TomlLongValueNode) read.get("int2").get()).getValue();
        Assert.assertEquals(int2, Long.valueOf(42L));
        Long int3 = ((TomlLongValueNode) read.get("int3").get()).getValue();
        Assert.assertEquals(int3, Long.valueOf(0L));
        Long int4 = ((TomlLongValueNode) read.get("int4").get()).getValue();
        Assert.assertEquals(int4, Long.valueOf(-17L));

        Long int5 = ((TomlLongValueNode) read.get("int5").get()).getValue();
        Assert.assertEquals(int5, Long.valueOf(1000L));
        Long int6 = ((TomlLongValueNode) read.get("int6").get()).getValue();
        Assert.assertEquals(int6, Long.valueOf(5349221L));
        Long int7 = ((TomlLongValueNode) read.get("int7").get()).getValue();
        Assert.assertEquals(int7, Long.valueOf(5349221L));
        Long int8 = ((TomlLongValueNode) read.get("int8").get()).getValue();
        Assert.assertEquals(int8, Long.valueOf(12345L));

        Long hex1 = ((TomlLongValueNode) read.get("hex1").get()).getValue();
        Assert.assertEquals(hex1, Long.valueOf(3735928559L));
        Long hex2 = ((TomlLongValueNode) read.get("hex2").get()).getValue();
        Assert.assertEquals(hex2, Long.valueOf(3735928559L));
        Long hex3 = ((TomlLongValueNode) read.get("hex3").get()).getValue();
        Assert.assertEquals(hex3, Long.valueOf(3735928559L));

        Long oct1 = ((TomlLongValueNode) read.get("oct1").get()).getValue();
        Assert.assertEquals(oct1, Long.valueOf(342391L));
        Long oct2 = ((TomlLongValueNode) read.get("oct2").get()).getValue();
        Assert.assertEquals(oct2, Long.valueOf(493L));

        Long bin1 = ((TomlLongValueNode) read.get("bin1").get()).getValue();
        Assert.assertEquals(bin1, Long.valueOf(214L));

        Double flt1 = ((TomlDoubleValueNodeNode) read.get("flt1").get()).getValue();
        Assert.assertEquals(flt1, Double.valueOf(1.0));
        Double flt2 = ((TomlDoubleValueNodeNode) read.get("flt2").get()).getValue();
        Assert.assertEquals(flt2, Double.valueOf(3.1415));
        Double flt3 = ((TomlDoubleValueNodeNode) read.get("flt3").get()).getValue();
        Assert.assertEquals(flt3, Double.valueOf(-0.01));

        Double flt4 = ((TomlDoubleValueNodeNode) read.get("flt4").get()).getValue();
        Assert.assertEquals(flt4, Double.valueOf(5e+22));
        Double flt5 = ((TomlDoubleValueNodeNode) read.get("flt5").get()).getValue();
        Assert.assertEquals(flt5, Double.valueOf(1e06));
        Double flt6 = ((TomlDoubleValueNodeNode) read.get("flt6").get()).getValue();
        Assert.assertEquals(flt6, Double.valueOf(-2E-2));

        Double flt7 = ((TomlDoubleValueNodeNode) read.get("flt7").get()).getValue();
        Assert.assertEquals(flt7, Double.valueOf(6.626e-34));

        Double flt8 = ((TomlDoubleValueNodeNode) read.get("flt8").get()).getValue();
        Assert.assertEquals(flt8, Double.valueOf(224617.445991228));

        String stringEscape = ((TomlStringValueNode) read.get("str0").get()).getValue();
        Assert.assertEquals(stringEscape, "I'm a string. \"You can quote me\". Name\tJosé\n" +
                "Location\tSF.");

        String basicString = ((TomlStringValueNode) read.get("str1").get()).getValue();
        Assert.assertEquals(basicString, "The quick brown fox jumps over the lazy dog.");
        String backslashWithWhitespace = ((TomlStringValueNode) read.get("str2").get()).getValue();
        Assert.assertEquals(backslashWithWhitespace, "The quick brown fox jumps over the lazy dog.");
        String multilineBasklash = ((TomlStringValueNode) read.get("str3").get()).getValue();
        Assert.assertEquals(multilineBasklash, "The quick brown fox jumps over the lazy dog.");

        String twoEscapes = ((TomlStringValueNode) read.get("str4").get()).getValue();
        Assert.assertEquals(twoEscapes, "Here are two quotation marks: \"\". Simple enough.");
        String threeQuotesEscapes = ((TomlStringValueNode) read.get("str5").get()).getValue();
        Assert.assertEquals(threeQuotesEscapes, "Here are three quotation marks: \"\"\".");
        String fifteenQuotes = ((TomlStringValueNode) read.get("str6").get()).getValue();
        Assert.assertEquals(fifteenQuotes, "Here are fifteen quotation marks: \"\"\"\"\"\"\"\"\"\"\"\"\"\"\".");
        String mixedEscape = ((TomlStringValueNode) read.get("str7").get()).getValue();
        Assert.assertEquals(mixedEscape, "\"This,\" she said, \"is just a pointless statement.");

        String absPathLiteralString = ((TomlStringValueNode) read.get("lit1").get()).getValue();
        Assert.assertEquals(absPathLiteralString, "C:\\Users\\nodejs\\templates");
        String windowsPathLiteralString = ((TomlStringValueNode) read.get("lit2").get()).getValue();
        Assert.assertEquals(windowsPathLiteralString, "\\\\ServerX\\admin$\\system32\\");
        String doubeQuoteLiteralString = ((TomlStringValueNode) read.get("lit3").get()).getValue();
        Assert.assertEquals(doubeQuoteLiteralString, "Tom \"Dubs\" Preston-Werner");
        String regexLiteralString = ((TomlStringValueNode) read.get("lit4").get()).getValue();
        Assert.assertEquals(regexLiteralString, "<\\i\\c*\\s*>");
        String regex2LiteralString = ((TomlStringValueNode) read.get("lit5").get()).getValue();
        Assert.assertEquals(regex2LiteralString, "I [dw]on't need \\d{2} apples");

        String multilineLiteralString = ((TomlStringValueNode) read.get("lit6").get()).getValue();
        Assert.assertEquals(multilineLiteralString, String.format(
                "The first newline is%strimmed in raw strings.%s   All other whitespace%s   is preserved.%s",
                System.lineSeparator(), System.lineSeparator(), System.lineSeparator(), System.lineSeparator()));
        String fifteenQuotesWithLiteral = ((TomlStringValueNode) read.get("lit7").get()).getValue();
        Assert.assertEquals(fifteenQuotesWithLiteral,
                "Here are fifteen quotation marks: \"\"\"\"\"\"\"\"\"\"\"\"\"\"\"");
        String fifteenSingleQuotes = ((TomlStringValueNode) read.get("lit8").get()).getValue();
        Assert.assertEquals(fifteenSingleQuotes, "Here are fifteen apostrophes: '''''''''''''''");

        LineRange stringValueRange = read.get("key1").get().location().lineRange();
        ParserTestUtils.assertLineRange(stringValueRange, 18, 7, 18, 14);
        LineRange multiStringValueRange = read.get("key4").get().location().lineRange();
        ParserTestUtils.assertLineRange(multiStringValueRange, 21, 7, 23, 19);
        LineRange arrayRange = read.get("key5").get().location().lineRange();
        ParserTestUtils.assertLineRange(arrayRange, 24, 7, 24, 14);
        LineRange arrayValue =
                ((TomlArrayValueNode) ((TomlKeyValueNode) read.rootNode().entries().get("key5")).value()).get(2)
                        .location().lineRange();
        ParserTestUtils.assertLineRange(arrayValue, 24, 12, 24, 13);
    }

    @Test
    public void testArrayValues() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/key-value/array.toml");
        Toml read = Toml.read(inputStream);

        TomlArrayValueNode mixedType = (TomlArrayValueNode) read.get("mixed_type").get();
        Long first = ((TomlLongValueNode) mixedType.get(0)).getValue();
        Long second = ((TomlLongValueNode) mixedType.get(1)).getValue();
        String third = ((TomlStringValueNode) mixedType.get(2)).getValue();
        Long fourth = ((TomlLongValueNode) mixedType.get(3)).getValue();

        Assert.assertEquals(first, Long.valueOf(1L));
        Assert.assertEquals(second, Long.valueOf(2L));
        Assert.assertEquals(fourth, Long.valueOf(3L));
        Assert.assertEquals(third, "test string");

        TomlArrayValueNode nestedArray = (TomlArrayValueNode) read.get("nested_array").get();
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

        TomlArrayValueNode nestedArraysInts = (TomlArrayValueNode) read.get("nested_arrays_of_ints").get();
        TomlArrayValueNode firstNestedArraysInts = nestedArraysInts.get(0);
        TomlLongValueNode firstNestedArraysIntsValue = firstNestedArraysInts.get(0);
        TomlLongValueNode secondNestedArraysIntsValue = firstNestedArraysInts.get(1);
        Assert.assertEquals(firstNestedArraysIntsValue.getValue(), Long.valueOf(1L));
        Assert.assertEquals(secondNestedArraysIntsValue.getValue(), Long.valueOf(2L));
        TomlArrayValueNode secondNestedArraysInts = nestedArraysInts.get(1);
        TomlLongValueNode nestedArraysIntsValue1 = secondNestedArraysInts.get(0);
        TomlLongValueNode nestedArraysIntsValue2 = secondNestedArraysInts.get(1);
        TomlLongValueNode nestedArraysIntsValue3 = secondNestedArraysInts.get(2);
        Assert.assertEquals(nestedArraysIntsValue1.getValue(), Long.valueOf(3L));
        Assert.assertEquals(nestedArraysIntsValue2.getValue(), Long.valueOf(4L));
        Assert.assertEquals(nestedArraysIntsValue3.getValue(), Long.valueOf(5L));

        TomlArrayValueNode nestedMixedArray = (TomlArrayValueNode) read.get("nested_mixed_array").get();
        TomlArrayValueNode firstNestedMixedArray = nestedMixedArray.get(0);
        TomlLongValueNode firstNestedMixedArrayValue1 = firstNestedMixedArray.get(0);
        TomlLongValueNode firstNestedMixedArrayValue12 = firstNestedMixedArray.get(1);
        Assert.assertEquals(firstNestedMixedArrayValue1.getValue(), Long.valueOf(1L));
        Assert.assertEquals(firstNestedMixedArrayValue12.getValue(), Long.valueOf(2L));
        TomlArrayValueNode secondNestedMixedArray = nestedMixedArray.get(1);
        TomlStringValueNode firstNestedMixedArrayValue2 = secondNestedMixedArray.get(0);
        TomlStringValueNode firstNestedMixedArrayValue22 = secondNestedMixedArray.get(1);
        TomlStringValueNode firstNestedMixedArrayValue23 = secondNestedMixedArray.get(2);
        Assert.assertEquals(firstNestedMixedArrayValue2.getValue(), "a");
        Assert.assertEquals(firstNestedMixedArrayValue22.getValue(), "b");
        Assert.assertEquals(firstNestedMixedArrayValue23.getValue(), "c");

        TomlArrayValueNode stringArray = (TomlArrayValueNode) read.get("string_array").get();
        TomlStringValueNode stringArrayVal1 = stringArray.get(0);
        TomlStringValueNode stringArrayVal2 = stringArray.get(1);
        TomlStringValueNode stringArrayVal3 = stringArray.get(2);
        TomlStringValueNode stringArrayVal4 = stringArray.get(3);
        Assert.assertEquals(stringArrayVal1.getValue(), "all");
        Assert.assertEquals(stringArrayVal2.getValue(), "strings");
        Assert.assertEquals(stringArrayVal3.getValue(), "are the same");
        Assert.assertEquals(stringArrayVal4.getValue(), "type");

        TomlArrayValueNode mixedNumbers = (TomlArrayValueNode) read.get("numbers").get();
        TomlDoubleValueNodeNode mixedNumbersVal1 = mixedNumbers.get(0);
        TomlDoubleValueNodeNode mixedNumbersVal2 = mixedNumbers.get(1);
        TomlDoubleValueNodeNode mixedNumbersVal3 = mixedNumbers.get(2);
        TomlLongValueNode mixedNumbersVal4 = mixedNumbers.get(3);
        TomlLongValueNode mixedNumbersVal5 = mixedNumbers.get(4);
        TomlLongValueNode mixedNumbersVal6 = mixedNumbers.get(5);
        Assert.assertEquals(mixedNumbersVal1.getValue(), Double.valueOf(0.1));
        Assert.assertEquals(mixedNumbersVal2.getValue(), Double.valueOf(0.2));
        Assert.assertEquals(mixedNumbersVal3.getValue(), Double.valueOf(0.5));
        Assert.assertEquals(mixedNumbersVal4.getValue(), Long.valueOf(1L));
        Assert.assertEquals(mixedNumbersVal5.getValue(), Long.valueOf(2L));
        Assert.assertEquals(mixedNumbersVal6.getValue(), Long.valueOf(5L));

        TomlArrayValueNode contributors = (TomlArrayValueNode) read.get("contributors").get();
        TomlStringValueNode stringValue = contributors.get(0);
        Assert.assertEquals(stringValue.getValue(), "Foo Bar <foo@example.com>");
        TomlInlineTableValueNode inlineNode = contributors.get(1);
        TomlKeyValueNode nameKV = inlineNode.get(0);
        TomlKeyValueNode emailKV = inlineNode.get(1);
        TomlKeyValueNode urlKV = inlineNode.get(2);
        TomlStringValueNode nameVal = (TomlStringValueNode) nameKV.value();
        TomlStringValueNode emailVal = (TomlStringValueNode) emailKV.value();
        TomlStringValueNode urlVal = (TomlStringValueNode) urlKV.value();
        Assert.assertEquals(nameVal.getValue(), "Baz Qux");
        Assert.assertEquals(emailVal.getValue(), "bazqux@example.com");
        Assert.assertEquals(urlVal.getValue(), "https://example.com/bazqux");
    }

    @Test
    public void testDottedKey() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/key-value/dotted.toml");
        Toml read = Toml.read(inputStream);

        Optional<TomlValueNode> noneExistKey = read.get("none");
        Assert.assertTrue(noneExistKey.isEmpty());

        Optional<TomlValueNode> noneExistKeyNextedTable = read.get("table.none");
        Assert.assertTrue(noneExistKeyNextedTable.isEmpty());

        TomlStringValueNode plainKey = (TomlStringValueNode) read.get("key").get();
        Assert.assertEquals(plainKey.getValue(), "no quote");

        TomlStringValueNode simpleQuotedKey = (TomlStringValueNode) read.get("\"key\"").get();
        Assert.assertEquals(simpleQuotedKey.getValue(), "quoted key");

        TomlStringValueNode plainNestedChild = (TomlStringValueNode) read.get("table.hi").get();
        Assert.assertEquals(plainNestedChild.getValue(), "hii");

        TomlStringValueNode quotedSingle = (TomlStringValueNode) read.get("table.\"foo bar\"").get();
        Assert.assertEquals(quotedSingle.getValue(), "boo");

        TomlBooleanValueNode dotted1 = (TomlBooleanValueNode) read.get("site.\"google.com\"").get();
        Assert.assertTrue(dotted1.getValue());

        TomlStringValueNode nestedTable = (TomlStringValueNode) read.get("table.hello.child").get();
        Assert.assertEquals(nestedTable.getValue(), "new");

        List<Toml> envList = read.getTables("cloud.config.envs");
        Assert.assertEquals(envList.size(), 2);

        TomlStringValueNode appleType = (TomlStringValueNode) read.get("apple.type").get();
        Assert.assertEquals(appleType.getValue(), "fruit");

        TomlStringValueNode orangeType = (TomlStringValueNode) read.get("orange.type").get();
        Assert.assertEquals(orangeType.getValue(), "fruit");

        TomlStringValueNode appleSkin = (TomlStringValueNode) read.get("apple.skin").get();
        Assert.assertEquals(appleSkin.getValue(), "thin");

        TomlStringValueNode orangeSkin = (TomlStringValueNode) read.get("orange.skin").get();
        Assert.assertEquals(orangeSkin.getValue(), "thick");

        TomlStringValueNode appleColor = (TomlStringValueNode) read.get("apple.color").get();
        Assert.assertEquals(appleColor.getValue(), "red");

        TomlStringValueNode orangeColor = (TomlStringValueNode) read.get("orange.color").get();
        Assert.assertEquals(orangeColor.getValue(), "orange");

        TomlStringValueNode nestedDottedKey = (TomlStringValueNode) read.get("root.first.second").get();
        Assert.assertEquals(nestedDottedKey.getValue(), "value");

        TomlStringValueNode nestedDottedKey1 = (TomlStringValueNode) read.get("root.first.third").get();
        Assert.assertEquals(nestedDottedKey1.getValue(), "value1");

        TomlStringValueNode mixedDotted = (TomlStringValueNode) read.get("foo.bar").get();
        Assert.assertEquals(mixedDotted.getValue(), "test");

        LineRange dottedKVPairType =
                ((TomlTableNode) read.rootNode().entries().get("apple")).entries().get("type").location().lineRange();
        LineRange dottedValue =
                ((TomlKeyValueNode) ((TomlTableNode) read.rootNode().entries().get("apple")).entries().get("type"))
                        .value().location().lineRange();
        ParserTestUtils.assertLineRange(dottedKVPairType, 4, 0, 4, 20);
        ParserTestUtils.assertLineRange(dottedValue, 4, 13, 4, 20);

        LineRange dottedKVPairSkin =
                ((TomlTableNode) read.rootNode().entries().get("apple")).entries().get("skin").location().lineRange();
        ParserTestUtils.assertLineRange(dottedKVPairSkin, 7, 0, 7, 19);

        LineRange dottedKVPairNested1 =
                ((TomlTableNode) ((TomlTableNode) read.rootNode().entries().get("root")).entries().get("first"))
                        .entries().get("second").location().lineRange();
        ParserTestUtils.assertLineRange(dottedKVPairNested1, 30, 0, 30, 22);
        LineRange dottedKVPairNested2 =
                ((TomlTableNode) ((TomlTableNode) read.rootNode().entries().get("root")).entries().get("first"))
                        .entries().get("third").location().lineRange();
        ParserTestUtils.assertLineRange(dottedKVPairNested2, 31, 0, 31, 22);
    }

    @Test
    public void testInlineTables() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("syntax/key-value/inline-tables.toml");
        Toml read = Toml.read(inputStream);
        Assert.assertEquals(read.getTable("emptyInline").get().rootNode().entries().size(), 0);
        TomlStringValueNode first = (TomlStringValueNode) read.get("rootInline.first").get();
        TomlStringValueNode last = (TomlStringValueNode) read.get("rootInline.last").get();
        Assert.assertEquals(first.getValue(), "Tom");
        Assert.assertEquals(last.getValue(), "Preston-Werner");

        TomlStringValueNode rootGreet = (TomlStringValueNode) read.get("nestedInlineTableRoot.nested.greeting").get();
        TomlStringValueNode rootLast = (TomlStringValueNode) read.get("nestedInlineTableRoot.another").get();
        Assert.assertEquals(rootGreet.getValue(), "hello");
        Assert.assertEquals(rootLast.getValue(), "Another one");

        TomlStringValueNode dotted = (TomlStringValueNode) read.get("inline.parent.child").get();
        Assert.assertEquals(dotted.getValue(), "hello");

        TomlStringValueNode arrVal1 =
                (TomlStringValueNode) (read.getTables("tableArr").get(0).get("name.nested.greeting").get());
        Assert.assertEquals(arrVal1.getValue(), "hello");
        TomlStringValueNode arrVal2 =
                (TomlStringValueNode) (read.getTables("tableArr").get(1).get("name.nested.greeting").get());
        Assert.assertEquals(arrVal2.getValue(), "how are youu");
    }
}

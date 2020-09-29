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

import io.ballerina.toml.Toml;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic Test for TOML Key Value Pairs.
 */
public class KeyValueTest {

    @Test
    public void testKeys() throws IOException {

        Toml toml = new Toml();
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("core/keys.toml");
        Toml read = toml.read(inputStream);
        String basicKey = read.getString("key");
        String underscoreKey = read.getString("underscore_key");
        String dashKey = read.getString("dash-key");
        String intKey = read.getString("1234");

        String dottedString = read.getString("127.0.0.1");
        String spacing = read.getString("character encoding");
        String unicode = read.getString("ʎǝʞ");
        String general = read.getString("key2");
        String escape = read.getString("quoted \"value\"");

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
    public void testValues() throws IOException {

        Toml toml = new Toml();
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("core/values.toml");
        Toml read = toml.read(inputStream);
        String stringValue = read.getString("key1");
        Long longValue = read.getLong("key2");
        Double doubleValue = read.getDouble("key3");
        String multiString = read.getString("key4");
        List<Long> longList = read.getList("key5");
        boolean boolfalse = read.getBoolean("key6");
        boolean booltrue = read.getBoolean("key7");

        Assert.assertEquals(stringValue, "hello");
        Assert.assertEquals(longValue, new Long(123L));
        Assert.assertEquals(doubleValue, 56.55);
        Assert.assertEquals(multiString, "\n" +
                "Roses are red\n" +
                "Violets are blue");
        List<Long> expectedLongList = new ArrayList<>();
        expectedLongList.add(1L);
        expectedLongList.add(2L);
        expectedLongList.add(3L);
        Assert.assertEquals(longList, expectedLongList);
        Assert.assertFalse(boolfalse);
        Assert.assertTrue(booltrue);
    }

}

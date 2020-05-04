/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.stringutils;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Class to test functionality of string utils.
 *
 * @since 1.0
 */
public class StringUtilsTest {

    private CompileResult result = BCompileUtil.compile(String.valueOf(Paths.get("test-src", "string-utils-test.bal")));

    @Test(description = "Test contains function")
    public void testContains() {
        BValue[] inputs = {new BString("This is a long sentence"), new BString("a long")};
        BValue[] returnValues = BRunUtil.invoke(result, "testContains", inputs);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test contains function")
    public void testContainsNegative() {
        BValue[] inputs = {new BString("This is a long sentence"), new BString("nulll")};
        BValue[] returnValues = BRunUtil.invoke(result, "testContains", inputs);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Tests string equalsIgnoreCase function")
    public void testEqualsIgnoreCase() {
        BValue[] inputs = {new BString("TeSTiNg StrInG 1"), new BString("teStiNg strInG 1")};
        BValue[] returnValues = BRunUtil.invoke(result, "testEqualsIgnoreCase", inputs);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Tests string equalsIgnoreCase function")
    public void testEqualsIgnoreCaseNegative() {
        BValue[] inputs = {new BString("TeSTiNg StrInG 1"), new BString("teStiNg strInG 2")};
        BValue[] returnValues = BRunUtil.invoke(result, "testEqualsIgnoreCase", inputs);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Tests string hashCode function")
    public void testHashCode() {
        BValue[] inputs = {new BString("Testing String")};
        BValue[] returnValues = BRunUtil.invoke(result, "testHashCode", inputs);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 117955681);
    }

    @Test(description = "Tests lastIndexOf function")
    public void testLastIndexOf() {
        BValue[] inputs = {new BString("This is as large as Elephas maximus"), new BString("as")};
        BValue[] returnValues = BRunUtil.invoke(result, "testLastIndexOf", inputs);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returnValues[0]).intValue(), 25);
    }

    @Test(description = "Tests matches function")
    public void testMatches() {
        BValue[] inputs = {new BString("This Should Match"), new BString("Th.*ch")};
        BValue[] returnValues = BRunUtil.invoke(result, "testMatches", inputs);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test
    public void testReplaceFunction() {
        BValue[] returns = BRunUtil.invoke(result, "testReplace");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).value(), "Hello Amal! Nimal!");
    }

    @Test(description = "Tests the string replaceAll function")
    public void testReplaceAllFunction() {
        BValue[] inputs = {new BString("ReplaceTTTGGGThis"), new BString("T.*G"), new BString(" ")};
        BValue[] returns = BRunUtil.invoke(result, "testReplaceAll", inputs);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).value(), "Replace This");
    }

    @Test
    public void testReplaceFirstFunction() {
        BValue[] inputs = {new BString("ReplaceThisThisTextThis"), new BString("This"), new BString(" ")};
        BValue[] returns = BRunUtil.invoke(result, "testReplaceFirst", inputs);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).value(), "Replace ThisTextThis");
    }

    @Test
    public void testSplitFunction() {
        BValue[] returns = BRunUtil.invoke(result, "testSplit");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].size(), 4);
        Assert.assertEquals(returns[0].stringValue(), "[\"amal\", \"kamal\", \"nimal\", \"sunimal,\"]");
    }

    @Test(description = "Test toBoolean function")
    public void testToBoolean() {
        BValue[] inputs = {new BString("true")};
        BValue[] returnValues = BRunUtil.invoke(result, "testToBoolean", inputs);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test toBoolean function")
    public void testToBooleanCaps() {
        BValue[] inputs = {new BString("TrUe")};
        BValue[] returnValues = BRunUtil.invoke(result, "testToBoolean", inputs);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test toBoolean function")
    public void testToBooleanFalse() {
        BValue[] inputs = {new BString("false")};
        BValue[] returnValues = BRunUtil.invoke(result, "testToBoolean", inputs);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returnValues[0]).booleanValue());
    }

    @Test(description = "Test toBoolean function with invalid string")
    public void testToBooleanInvalid() {
        BValue[] inputs = {new BString("invalid_string")};
        BValue[] returnValues = BRunUtil.invoke(result, "testToBoolean", inputs);
        Assert.assertEquals(returnValues.length, 1);
        Assert.assertTrue(returnValues[0] instanceof BBoolean);
        Assert.assertFalse(((BBoolean) returnValues[0]).booleanValue());
    }
}

/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.statements.arrays;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for string array concatenation.
 */
public class ArrayConcatenationTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array-concat-test.bal");
    }

    @Test
    public void testSingleElementEmptyDelimiter() {
        String expected = "A";
        BStringArray arrayValue = new BStringArray();
        arrayValue.add(0, "A");

        BValue[] args = {arrayValue, new BString("")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testStringArrayConcatenation", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(), expected);
    }

    @Test
    public void testSingleElementNonEmptyDelimiter() {
        String expected = "A";
        BStringArray arrayValue = new BStringArray();
        arrayValue.add(0, "A");

        BValue[] args = {arrayValue, new BString(",")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testStringArrayConcatenation", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(), expected);
    }

    @Test
    public void testMultipleElementsEmptyDelimiter() {
        String expected = "ABC";
        BStringArray arrayValue = new BStringArray();
        arrayValue.add(0, "A");
        arrayValue.add(1, "B");
        arrayValue.add(2, "C");

        BValue[] args = {arrayValue, new BString("")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testStringArrayConcatenation", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(), expected);
    }

    @Test
    public void testMultipleElementsNonEmptyDelimiter() {
        String expected = "A,B,C";
        BStringArray arrayValue = new BStringArray();
        arrayValue.add(0, "A");
        arrayValue.add(1, "B");
        arrayValue.add(2, "C");

        BValue[] args = {arrayValue, new BString(",")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testStringArrayConcatenation", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid Return Values.");
        Assert.assertEquals(returnValues[0].stringValue(), expected);
    }
}

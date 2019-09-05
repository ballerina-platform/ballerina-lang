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

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
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

    @Test
    public void testSplitFunction() {
        BValue[] returns = BRunUtil.invoke(result, "testSplit");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(returns[0].size(), 4);
        Assert.assertEquals(returns[0].stringValue(), "[\"amal\", \"kamal\", \"nimal\", \"sunimal,\"]");
    }

    @Test
    public void testReplaceFunction() {
        BValue[] returns = BRunUtil.invoke(result, "testReplace");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(((BString) returns[0]).value(), "Hello Amal! Nimal!");
    }
}

/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;

public class RegexTest {

    private ProgramFile bLangProgram;
    private static final String content = "peach punch pinch test";

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.getProgramFile("samples/regexTest.bal");
    }


    @Test()
    public void testFindAll() throws UnsupportedEncodingException {
        BValue[] args = {new BString("p([a-z]+)ch"), new BString(content)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "findAll", args);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "peach");
        Assert.assertEquals(((BStringArray) returns[0]).get(1), "punch");
        Assert.assertEquals(((BStringArray) returns[0]).get(2), "pinch");
    }

    @Test()
    public void testMatch() throws UnsupportedEncodingException {
        BValue[] args1 = {new BString("p([a-z]+)ch"), new BString("passasach")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "match", args1);
        Assert.assertEquals(returns[0].stringValue(), "true");

        BValue[] args2 = {new BString("p([a-z]+)ch"), new BString("ballerina")};
        returns = BLangFunctions.invokeNew(bLangProgram, "match", args2);
        Assert.assertEquals(returns[0].stringValue(), "false");
    }

    @Test()
    public void testReplaceAll() throws UnsupportedEncodingException {
        BValue[] args = {new BString(content), new BString("p([a-z]+)ch"), new BString("ballerina")};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "replaceAll", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "ballerina ballerina ballerina test");
    }
}

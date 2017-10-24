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
package org.ballerinalang.test.nativeimpl.functions;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for package ballerina.net.uri.
 */
public class NetURITest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/nativeimpl/functions/netUri.bal");
    }

    @Test
    public void testEncode() {
        BValue[] args = {
                new BString("http://localhost:9090"),
                new BString("http://localhost:9090/echoService/hello world/"),
                new BString("http://localhost:9090/echoService?type=string&value=hello world"),
                new BString("http://localhost:9090/echoService#abc"),
                new BString("http://localhost:9090/echoService:abc"),
                new BString("http://localhost:9090/echoService+abc"),
                new BString("http://localhost:9090/echoService*abc"),
                new BString("http://localhost:9090/echoService%abc"),
                new BString("http://localhost:9090/echoService~abc"),
        };

        for (BValue arg : args) {
            BValue[] inputArg = {arg};
            BValue[] returnVals = BRunUtil.invoke(compileResult, "testEncode", inputArg);
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                    "Invalid Return Values for " + arg.stringValue());
            Assert.assertFalse(returnVals[0].stringValue().contains(" "), "Encoded valued can't contain space.");
            Assert.assertFalse(returnVals[0].stringValue().contains("*"), "Encoded valued can't contain *.");
            Assert.assertFalse(returnVals[0].stringValue().contains("+"), "Encoded valued can't contain +.");
            Assert.assertFalse(returnVals[0].stringValue().contains("%7E"), "Encoded valued can't contain %7E.");
        }
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testEncodeNegative() {
        BValue[] inputArg = {new BString(null)};
        BRunUtil.invoke(compileResult, "testEncode", inputArg);
    }
}

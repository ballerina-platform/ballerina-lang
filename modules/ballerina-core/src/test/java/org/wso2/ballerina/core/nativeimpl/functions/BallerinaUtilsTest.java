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
package org.wso2.ballerina.core.nativeimpl.functions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.util.Base64Decode;
import org.wso2.ballerina.core.nativeimpl.util.Base64Encode;
import org.wso2.ballerina.core.nativeimpl.util.GetHmac;
import org.wso2.ballerina.core.nativeimpl.util.GetRandomString;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.FunctionUtils;
import org.wso2.ballerina.core.utils.Functions;
import org.wso2.ballerina.core.utils.ParserUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Test Cases for ballerina.util native functions.
 */
public class BallerinaUtilsTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        if (symScope.lookup(new SymbolName("ballerina.lang.system:print_string")) == null) {
            BuiltInNativeConstructLoader.loadConstructs();
        }
        FunctionUtils.addNativeFunction(symScope, new Base64Decode());
        FunctionUtils.addNativeFunction(symScope, new Base64Encode());
        FunctionUtils.addNativeFunction(symScope, new GetRandomString());
        FunctionUtils.addNativeFunction(symScope, new GetHmac());
        bFile = ParserUtils.parseBalFile("samples/nativeimpl/utilsTest.bal", symScope);
    }

    @Test
    public void testEncoding() {
        String[] stringsToTest = {"Hi", "`Welcome\" \r^to^\tBallerina\\ /$TestCases$\n # ~^!!!",
                "https://example.com/test/index.html#title1",
                "https://example.com/test/index.html?greeting=hello world&your-name=Ballerina Test cases",
                "バレリーナ日本語", "Ballerina 的中文翻译", "Traducción al español de  la bailarina"};

        for (String s : stringsToTest) {
            BValue[] returnVals = Functions.invoke(bFile, "testEncodeDecode", new BValue[]{new BString(s)});
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                    "Invalid Return Values for :" + s);
            Assert.assertEquals(returnVals[0].stringValue(), s, "Original and Return value didn't match");
        }
    }

    @Test
    public void testRandomString() {
        BValue[] returnVals = Functions.invoke(bFile, "testRandomString");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for");
    }

    @Test
    public void testHmac() {
        final String key = "abcdefghijk";
        List<BValue[]> argsList = new ArrayList<>();

        argsList.add(new BValue[]{new BString("Ballerina HMAC test"), new BString(key), new BString("SHA1")});
        argsList.add(new BValue[]{new BString("Ballerina HMAC test"), new BString(key), new BString("SHA256")});
        argsList.add(new BValue[]{new BString("Ballerina HMAC test"), new BString(key), new BString("MD5")});

        for (BValue[] args : argsList) {
            BValue[] returnVals = Functions.invoke(bFile, "testHmac", args);
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                    "Invalid Return Values for");
        }
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testHmacNagativeInvalidAlgo() {
        final String key = "abcdefghijk";
        BValue[] returnVals = Functions.invoke(bFile, "testHmac",
                new BValue[]{new BString("Ballerina HMAC test"), new BString(key), new BString("SHA124")});
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for");

    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testHmacNagativeInvalidKey() {
        final String key = "";
        BValue[] returnVals = Functions.invoke(bFile, "testHmac",
                new BValue[]{new BString("Ballerina HMAC test"), new BString(key), new BString("SHA1")});
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for");

    }

}

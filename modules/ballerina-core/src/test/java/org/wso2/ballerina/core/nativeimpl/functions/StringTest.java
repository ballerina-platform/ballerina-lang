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
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueType;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

import java.io.IOException;

/**
 * Test Cases for ballerina.lang.string.
 */
public class StringTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        // Add Native functions.
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        if (symScope.lookup(new SymbolName("ballerina.lang.system:print_string")) == null) {
            BuiltInNativeConstructLoader.loadConstructs();
        }
        bFile = ParserUtils.parseBalFile("samples/nativeimpl/stringTest.bal", symScope);
    }

    @Test
    public void testValueOf() throws IOException {
        BValueType[] args = {new BLong(100), new BDouble(10.1)};
        BValue[] returnVals = Functions.invoke(bFile, "testValueOf", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for");
        Assert.assertEquals(returnVals[0].stringValue(), "1010010.110.1hellofalse");
    }

    @Test
    public void testValueOfJSON() throws IOException {
        BValue[] returnVals = Functions.invoke(bFile, "testValueOfJSON");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for");
        Assert.assertEquals(returnVals[0].stringValue(),
                "{\"counties\":[{\"code\":\"US\",\"currency\":\"Dollar\"},{\"code\":\"UK\"," +
                        "\"currency\":\"Pound\"}]}");
    }

    @Test
    public void testValueOfXML() throws IOException {
        BValue[] returnVals = Functions.invoke(bFile, "testValueOfXML");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for");
        Assert.assertEquals(returnVals[0].stringValue(),
                "<counties><country><code>US</code><currency>Dollar</currency></country>" +
                        "<country><code>UK</code><currency>Pound</currency></country></counties>");
    }

    @Test
    public void testCommon() throws IOException {
        BValue[] returnVals = Functions.invoke(bFile, "testCommon");
        Assert.assertFalse(returnVals == null || returnVals.length != 2 || returnVals[0] == null ||
                returnVals[1] == null, "Invalid Return Values for");
        Assert.assertTrue(((BBoolean) returnVals[0]).booleanValue(), returnVals[1].stringValue());
    }

}

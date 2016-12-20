/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.runtime;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.BLangInterpreter;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.linker.BLangLinker;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.IntValue;
import org.wso2.ballerina.core.utils.FunctionUtils;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Local function invocation test.
 */
public class LocalFuncInvocationTest {

    private BallerinaFile bFile;
    private static final String funcName = "process";

    @BeforeTest
    public void setup() {
        bFile = ParserUtils.parseBalFile("samples/runtime/localFuncInvocationTest.bal");
        // Linker
        BLangLinker linker = new BLangLinker(bFile);
        linker.link(null);
    }

    @Test
    public void testLocalFuncInvocation() {

        BValue[] arguments = {new IntValue(100), new IntValue(5), new IntValue(1)};
        FunctionInvocationExpr funcIExpr = FunctionUtils.createInvocationExpr(bFile, funcName, arguments);

        Context bContext = FunctionUtils.createInvocationContext(1);

        BLangInterpreter bLangInterpreter = new BLangInterpreter(bContext);
        funcIExpr.accept(bLangInterpreter);

        // TODO: Check this logic.
        int actual = FunctionUtils.getValue(bContext, 0).getInt();
        int expected = 116;

        Assert.assertEquals(actual, expected);
    }

    public static void main(String[] args) {
        LocalFuncInvocationTest test = new LocalFuncInvocationTest();
//        test.setup();
        test.testLocalFuncInvocation();
    }
}

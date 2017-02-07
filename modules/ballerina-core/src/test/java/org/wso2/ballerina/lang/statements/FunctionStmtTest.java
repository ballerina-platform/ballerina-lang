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
package org.wso2.ballerina.lang.statements;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Validate Function statement.
 */
public class FunctionStmtTest {

    private static final String funcPublic = "testHelloWorldPublic";
    private static final String funcPrivate = "testHelloWorldPrivate";
    private Function testHelloWorldPublic;
    private Function testHelloWorldPrivate;

    @BeforeClass
    public void setup() {
        BallerinaFile bFile = ParserUtils.parseBalFile("lang/statements/function-stmt.bal");
        testHelloWorldPublic = Functions.getFunction(bFile, funcPublic);
        testHelloWorldPrivate = Functions.getFunction(bFile, funcPrivate);
    }

    //    @Test(description = "Test function Modularity.")
    //    public void testFuncModularity() {
    //        Assert.assertTrue(testHelloWorldPublic.isPublic(), funcPublic + " is public, but found private)");
    //        Assert.assertTrue(!testHelloWorldPrivate.isPublic(), funcPublic + " is private, but found public)");
    //
    //        // TODO : Broken Fix this.
    //        //Assert.assertEquals(testHelloWorldPublic.getPackageName(), "lang.statements.func");
    //        //Assert.assertEquals(testHelloWorldPrivate.getPackageName(), "lang.statements.func");
    //    }

    @Test(description = "Test invoking an undefined function",
          expectedExceptions = { SemanticException.class },
          expectedExceptionsMessageRegExp = "undefined-function-stmt.bal:2: undefined function 'foo'")
    public void testUndefinedFunction() {
        ParserUtils.parseBalFile("lang/statements/undefined-function-stmt.bal");
    }

    @Test(description = "Test functions having return statement missing on required paths",
          expectedExceptions = { SemanticException.class },
          expectedExceptionsMessageRegExp = ".*missing return statement.*",
          dataProvider = "invalidReturnStatements")
    public void testFunctionInvalidReturnStatement(String filePath) {
        ParserUtils.parseBalFile(filePath);
    }

    @Test(description = "Test functions having return statement on required paths",
          dataProvider = "validReturnStatements")
    public void testFunctionReturnStatement(String filePath) {
        ParserUtils.parseBalFile(filePath);
    }

    @DataProvider(name = "invalidReturnStatements")
    public static Object[][] invalidReturnStatements() {
        return new Object[][] {
                { "lang/functions/invalid-return-in-ifelseblock.bal" }
                , { "lang/functions/invalid-return-in-only-ifblock.bal" }
                , { "lang/functions/invalid-return-in-ifelseifblock.bal" }
                , { "lang/functions/invalid-return-in-ifelseifblock3.bal" }
                , { "lang/functions/invalid-return-in-only-while.bal" }
                , { "lang/functions/invalid-return-in-ifandwhile.bal"}
        };
    }

    @DataProvider(name = "validReturnStatements")
    public static Object[][] validReturnStatements() {
        return new Object[][] {
                { "lang/functions/return-in-ifelseblock1.bal" }
                , { "lang/functions/return-in-ifelseblock2.bal" }
                , { "lang/functions/return-in-only-ifblock.bal" }
                , { "lang/functions/return-in-ifelseifblock1.bal" }
                , { "lang/functions/return-in-ifelseifblock2.bal" }
                , { "lang/functions/return-in-ifelseifblock3.bal" }
                , { "lang/functions/return-in-only-while.bal" }
                , { "lang/functions/return-in-ifandwhile.bal" }
        };
    }
}

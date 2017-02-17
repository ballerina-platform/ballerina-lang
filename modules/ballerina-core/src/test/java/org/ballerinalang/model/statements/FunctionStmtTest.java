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
package org.ballerinalang.model.statements;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.Function;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        BLangProgram bLangProgram = BTestUtils.parseBalFile("lang/statements/function-stmt.bal");
        testHelloWorldPublic = BLangFunctions.getFunction(bLangProgram, funcPublic);
        testHelloWorldPrivate = BLangFunctions.getFunction(bLangProgram, funcPrivate);
    }

//    @Test(description = "Test function Modularity.")
//    public void testFuncModularity() {
//        Assert.assertTrue(testHelloWorldPublic.isPublic(), funcPublic + " is public, but found private)");
//        Assert.assertTrue(!testHelloWorldPrivate.isPublic(), funcPublic + " is private, but found public)");
//
//        // TODO : Broken Fix this.
//        //Assert.assertEquals(testHelloWorldPublic.getPackageName(), "model.statements.func");
//        //Assert.assertEquals(testHelloWorldPrivate.getPackageName(), "model.statements.func");
//    }

    @Test(description = "Test invoking an undefined function",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "undefined-function-stmt.bal:2: undefined function 'foo'")
    public void testUndefinedFunction() {
        BTestUtils.parseBalFile("lang/statements/undefined-function-stmt.bal");
    }
}

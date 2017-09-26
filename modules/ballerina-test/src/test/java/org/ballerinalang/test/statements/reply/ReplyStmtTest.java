/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.statements.reply;

import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for reply statement.
 */
public class ReplyStmtTest {
    private CompileResult result;
    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
//        BuiltInNativeConstructLoader.loadConstructs();
//        result = BTestUtils.compile("test-src/statements/reply/reply-stmt.bal");
        resultNegative = BTestUtils.compile("test-src/statements/reply/reply-stmt-negative.bal");
    }

    //TODO service issue
    @Test(description = "Semantic validation test cases for reply statement in a service", enabled = false)
    public void testReplyFromFunction() {
//        BTestUtils.compile("test-src/statements/reply/reply-native-service.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }


    @Test(description = "Test reply statement with errors")
    public void testReplyStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        //testReplyInFunction
        BTestUtils.validateError(resultNegative, 0, "this function must return a result", 1, 0);
        //testReplyAction
        //TODO fix
//        BTestUtils.validateError(resultNegative, 1, "unreachable code", 25, 4);

    }

    /* Negative Tests */

//    @Test(description = "Test reply statement in a function",
//            expectedExceptions = {SemanticException.class},
//            expectedExceptionsMessageRegExp = "reply-from-function.bal:1: reply statement cannot be used in a " +
//                    "function definition")
//    public void testReplyFromFunctionwww() {
//        BTestUtils.compile("test-src/statements/reply/reply-from-function.bal");
//    }
//
//    @Test(description = "Test reply statement in a action",
//            expectedExceptions = {SemanticException.class},
//            expectedExceptionsMessageRegExp = "reply-from-action.bal:5: reply statement cannot be used in a action " +
//                    "definition")
//    public void testReplyFromAction() {
//        BTestUtils.compile("test-src/statements/reply/reply-from-action.bal");
//    }

}

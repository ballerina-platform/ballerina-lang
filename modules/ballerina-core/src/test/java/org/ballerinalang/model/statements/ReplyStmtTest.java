/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.statements;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.natives.BuiltInNativeConstructLoader;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for reply statement.
 */
public class ReplyStmtTest {

    @BeforeClass
    public void setup() {
        BuiltInNativeConstructLoader.loadConstructs();
    }

    /* Negative Tests */
    
    @Test(description = "Test reply statement in a function",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "reply-from-function.bal:1: reply statement cannot be used in a " +
            "function definition")
    public void testReplyFromFunction() {
        BTestUtils.parseBalFile("lang/statements/replyStmt/reply-from-function.bal");
    }
    
    @Test(description = "Test reply statement in a action",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "reply-from-action.bal:5: reply statement cannot be used in a action " +
            "definition")
    public void testReplyFromAction() {
        BTestUtils.parseBalFile("lang/statements/replyStmt/reply-from-action.bal");
    }
}

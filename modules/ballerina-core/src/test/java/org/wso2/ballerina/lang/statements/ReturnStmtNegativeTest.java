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
package org.wso2.ballerina.lang.statements;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.ParserUtils;

public class ReturnStmtNegativeTest {
    private BallerinaFile bFile;
    private SymScope globalSymScope;

    @BeforeClass
    public void setup() {
        BuiltInNativeConstructLoader.loadConstructs();
        globalSymScope = GlobalScopeHolder.getInstance().getScope();
    }

    @Test(description = "Test return statement in resource",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "return-in-resource.bal:14: return statement cannot be used " +
                    "in a resource definition")
    public void testReturnInResource() {
        ParserUtils.parseBalFile("lang/statements/returnstmt/return-in-resource.bal", globalSymScope);
    }

    @Test(description = "Test invoking an undefined function",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "not-enough-args-to-return-1.bal:2: not enough arguments to return")
    public void testNotEnoughArgsToReturn() {
        ParserUtils.parseBalFile("lang/statements/returnstmt/not-enough-args-to-return-1.bal", globalSymScope);
    }

    public static void n(String[] args) {
        ReturnStmtNegativeTest test = new ReturnStmtNegativeTest();
        test.setup();
//        test.testReturnInResource();
        test.testNotEnoughArgsToReturn();
    }
}

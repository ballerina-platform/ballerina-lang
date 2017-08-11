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
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.annotations.Test;

/**
 * This contains methods to test negative behaviours of the transform statement.
 *
 * @since 0.8.7
 */
public class TransformStmtNegativeTest {

    @Test(description = "Test empty transform statement body",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp =
                    "transform-stmt-empty-body.bal:17: no statements found in the transform statement body")
    public void testEmptyBody() {
        BTestUtils.getProgramFile("lang/statements/transformStmt/transform-stmt-empty-body.bal");
    }

    @Test(description = "Test invalid input and output combinations",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp =
                    "transform-stmt-invalid-input-output.bal:19: input and output variables cannot be interchanged " +
                    "in transform statement")
    public void testInvalidInputOutput () {
        BTestUtils.getProgramFile("lang/statements/transformStmt/transform-stmt-invalid-input-output.bal");
    }
}

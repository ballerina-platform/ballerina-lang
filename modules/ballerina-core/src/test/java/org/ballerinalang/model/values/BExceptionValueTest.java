/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.values;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.annotations.Test;

/**
 * Test class for ballerina exception initialization..
 */
public class BExceptionValueTest {

    private BLangProgram bLangProgram;

    @Test
    public void testExceptionValueInit() {
        bLangProgram = BTestUtils.parseBalFile("lang/values/exception-value.bal");
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testInvalidExceptionValueInit() {
        BTestUtils.parseBalFile("lang/values/invalid-exception-init.bal");
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testInvalidExceptionValueInitTwo() {
        BTestUtils.parseBalFile("lang/values/invalid-exception-init2.bal");
    }

    @Test(expectedExceptions = SemanticException.class,
            expectedExceptionsMessageRegExp = ".*'exception' cannot be cast to 'string'.*")
    public void testInvalidExceptionCast() {
        BTestUtils.parseBalFile("lang/values/invalid-exception-cast.bal");
    }
}

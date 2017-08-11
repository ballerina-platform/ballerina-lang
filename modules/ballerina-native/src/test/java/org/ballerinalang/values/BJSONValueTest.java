/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.values;

import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina map.
 */
public class BJSONValueTest {

    private ProgramFile programFile;
    private static final double DELTA = 0.01;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/values/json-value.bal");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testGetFromNull() {
        BLangFunctions.invokeNew(programFile, "testGetFromNull");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testAddToNull() {
        BLangFunctions.invokeNew(programFile, "testAddToNull");
    }

}

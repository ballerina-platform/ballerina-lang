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
package org.ballerinalang.model.functions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for functions.
 */
public class FunctionTest {

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/functions/function-with-no-return-stmt.bal");
    }

    @Test(description = "Test empty function scenario")
    public void testEmptyFunction() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "emptyFunction");
        Assert.assertEquals(returns.length, 0);
    }

    @Test(description = "Test function with empty default worker")
    public void testFunctionWithEmptyDefaultWorker() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "funcEmptyDefaultWorker");
        Assert.assertEquals(returns.length, 0);
    }
}

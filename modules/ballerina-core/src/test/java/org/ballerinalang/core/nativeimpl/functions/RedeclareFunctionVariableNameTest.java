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
package org.ballerinalang.core.nativeimpl.functions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test redeclare nature of functions and variable names.
 */
public class RedeclareFunctionVariableNameTest {

    private BLangProgram bLangProgram;
    private String redeclareStringFunctionName = "redeclareString";
    private String redeclareIntFunctionName = "redeclareInt";
    private String redeclareMultipleName = "multiple";

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/functions/redeclare-function-var-name.bal");
    }

    @Test(description = "Test for function name for same string variable name")
    public void testFunctionForStringName() {

        String result1 = "Hello World";
        BValue[] returnsStep1 = BLangFunctions.invoke(bLangProgram, redeclareStringFunctionName);
        Assert.assertEquals(returnsStep1[0].stringValue(), result1);
    }

    @Test(description = "Test for function name for same int variable name")
    public void testFunctionForIntName() {

        int result2 = 8;
        BValue[] returnsStep2 = BLangFunctions.invoke(bLangProgram, redeclareIntFunctionName);

        long actualResultStep2 = ((BInteger) returnsStep2[0]).intValue();
        Assert.assertEquals(actualResultStep2, result2);
    }

    @Test(description = "Test for function name for multiple variable name")
    public void testFunctionFormultipleName() {

        boolean result3 = true;
        BValue[] returnsStep3 = BLangFunctions.invoke(bLangProgram, redeclareMultipleName);

        boolean actualResultStep2 = ((BBoolean) returnsStep3[0]).booleanValue();
        Assert.assertEquals(actualResultStep2, result3);
    }

}

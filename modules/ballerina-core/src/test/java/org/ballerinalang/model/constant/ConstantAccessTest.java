/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.model.constant;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Constant access test cases
 */
public class ConstantAccessTest {

    @BeforeClass
    public void setup() {

    }

    @Test(description = "Test accessing constant from other packages")
    public void testAccessingConstantFromOtherPkg() {
        ProgramFile programFile = BTestUtils.getProgramFile("lang/constant/main");
        BValue[] returns = BLangFunctions.invokeNew(programFile, "lang.constant.main",
                "accessConstantFromOtherPkg");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 342342.234);
    }

    @Test(description = "Test assigning constant from other package to global variable")
    public void testAssigningConstFromOtherPkgToGlobalVar() {
        ProgramFile programFile = BTestUtils.getProgramFile("lang/constant/main");
        BValue[] returns = BLangFunctions.invokeNew(programFile, "lang.constant.main",
                "assignConstFromOtherPkgToGlobalVar");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 342342.234);
    }

}

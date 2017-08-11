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
package org.ballerinalang.core.lang.worker;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Worker vm test cases.
 */
public class WorkerVMTest {
    private ProgramFile bProgramFile;

    @BeforeClass
    public void setup() {
        bProgramFile = BTestUtils.getProgramFile("samples/worker-vm.bal");
    }


    @Test(description = "Test simple worker in vm")
    public void testVMWorkerInFunction() {
        BValue[] args = {new BInteger(100)};
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testWorkerInVM", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(returns[0].stringValue(), "1000");
    }

//    @Test(description = "Test multiple function calls in vm")
//    public void testVMMultiFunction() {
//        BValue[] args = {new BInteger(100)};
//        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "foo", args);
//        Assert.assertEquals(returns.length, 1);
//        Assert.assertTrue(returns[0] instanceof BInteger);
//        Assert.assertEquals(returns[0].stringValue(), "1000");
//    }

}

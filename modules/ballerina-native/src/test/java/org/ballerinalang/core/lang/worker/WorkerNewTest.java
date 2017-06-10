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

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Worker implementation test cases.
 */
public class WorkerNewTest {

    private ProgramFile bProgramFile;

    @BeforeClass
    public void setup() {
        bProgramFile = BTestUtils.getProgramFile("samples/worker-multi-interaction.bal");
    }


//    @Test(description = "Test worker interaction 3 workers")
//    public void testWorkerScenario1() {
//        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testWorkerScenario1");
//        Assert.assertEquals(returns.length, 1);
//        Assert.assertTrue(returns[0] instanceof BBoolean);
//        final String expected = "true";
//        Assert.assertEquals(returns[0].stringValue(), expected);
//    }

    @Test(description = "Test worker interaction 3 workers")
    public void testWorkerScenario1() {
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testWorkerInVM");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }
}

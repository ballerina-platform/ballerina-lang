/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.worker;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Wait for all workers related tests.
 *
 * @since 0.990.1
 */
public class WaitForAllWorkersTest {
    private CompileResult result;
    private PrintStream defaultOut;
    private ByteArrayOutputStream tempOutStream;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/wait_for_all_workers.bal");
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.asList(result.getDiagnostics()).toString());
    }

    @BeforeMethod
    private void initTempOut() throws IOException {
        defaultOut = System.out;
        tempOutStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(tempOutStream));
    }

    @Test(description = "Test to see if worker continues to run even when default worker has finished")
    public void sendToFork() {
        BRunUtil.invoke(result, "testWaitForAllWorkers", new BValue[0]);
        String s = getSysOut();
        Assert.assertEquals(s, "Finishing Default Worker\nFinishing Worker w2\n");
    }

    @AfterMethod
    private void closeTempOut() throws IOException {
        System.out.close();
        System.setOut(defaultOut);
    }

    private String getSysOut() {
        return new String(tempOutStream.toByteArray());
    }
}

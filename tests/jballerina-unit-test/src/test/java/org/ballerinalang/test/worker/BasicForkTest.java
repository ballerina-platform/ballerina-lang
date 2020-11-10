/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.worker;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * Basic worker related tests.
 */
public class BasicForkTest {

    private CompileResult result;
    private PrintStream defaultOut;
    private ByteArrayOutputStream tempOutStream;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/basic-fork.bal");
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.asList(result.getDiagnostics()).toString());
    }

    @Test
    public void sendToFork() {
        BRunUtil.invoke(result, "sendToFork", new BValue[0]);
        Assert.assertEquals(lastNum(getSysOut()), 24);
    }

    @Test
    public void forkInWorker() {
        BRunUtil.invoke(result, "forkInWorker", new BValue[0]);
        Assert.assertEquals(lastNum(getSysOut()), 87);
    }

    @BeforeMethod
    private void initTempOut() {
        defaultOut = System.out;
        tempOutStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(tempOutStream));
    }

    @AfterMethod
    private void closeTempOut() {
        System.out.close();
        System.setOut(defaultOut);
    }

    private String getSysOut() {
        return new String(tempOutStream.toByteArray());
    }

    private String lastLine(String multiLineStr) {
        return multiLineStr.substring(multiLineStr.lastIndexOf("\n") + 1);
    }

    private int lastNum(String multiLineStr) {
        return Integer.parseInt(lastLine(multiLineStr));
    }
}

/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.packaging;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Tests order of execution of listener methods.
 */
public class ModuleExecutionFlowTests extends BaseTest {
    public static final int TIMEOUT = 10000;

    @Test
    public void testModuleExecutionOrder() throws BallerinaTestException {
        Path projectPath = Paths.get("src", "test", "resources", "packaging", "proj1");
        runAndAssert(projectPath);
    }

    @Test
    public void testModuleMainExecutionOrder() throws BallerinaTestException {
        Path projectPath = Paths.get("src", "test", "resources", "packaging", "proj6");
        runAndAssert(projectPath);
    }

    @Test
    public void testImportModuleHasListener() throws BallerinaTestException {
        Path projectPath = Paths.get("src", "test", "resources", "packaging", "proj2");
        BServerInstance serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(projectPath.toAbsolutePath().toString(), projectPath.getFileName().toString(), null,
                                   null, null);
        LogLeecher errLeecherA = new LogLeecher("Stopped module A", LogLeecher.LeecherType.ERROR);
        serverInstance.addErrorLogLeecher(errLeecherA);
        serverInstance.shutdownServer();
        errLeecherA.waitForText(TIMEOUT);
        serverInstance.removeAllLeechers();
    }

    private void runAndAssert(Path projectPath) throws BallerinaTestException {
        BServerInstance serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(projectPath.toAbsolutePath().toString(), projectPath.getFileName().toString(), null,
                                   null, null);
        LogLeecher errLeecherA = new LogLeecher("Stopped module A", LogLeecher.LeecherType.ERROR);
        LogLeecher errLeecherB = new LogLeecher("Stopped module B", LogLeecher.LeecherType.ERROR);
        LogLeecher errLeecherC = new LogLeecher("Stopped module C", LogLeecher.LeecherType.ERROR);
        serverInstance.addErrorLogLeecher(errLeecherA);
        serverInstance.addErrorLogLeecher(errLeecherB);
        serverInstance.addErrorLogLeecher(errLeecherC);
        serverInstance.shutdownServer();
        errLeecherA.waitForText(TIMEOUT);
        errLeecherB.waitForText(TIMEOUT);
        errLeecherC.waitForText(TIMEOUT);
        serverInstance.removeAllLeechers();
    }

    @Test(description = "Test 'init' is called only once for each module at runtime")
    public void testModuleDependencyChainForInit() throws BallerinaTestException, InterruptedException {
        Path projectPath = Paths.get("src", "test", "resources", "packaging", "module_invocation_project");
        BServerInstance serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(projectPath.toAbsolutePath().toString(), "module_invocation_project", null, null,
                                   null);
        LogLeecher errLeecherCurrent = new LogLeecher("Stopped module current", LogLeecher.LeecherType.ERROR);
        LogLeecher errLeecherDep1 = new LogLeecher("Stopped module second dependent", LogLeecher.LeecherType.ERROR);
        LogLeecher errLeecherDep2 = new LogLeecher("Stopped module first dependent", LogLeecher.LeecherType.ERROR);
        LogLeecher errLeecherBasic = new LogLeecher("Stopped module basic", LogLeecher.LeecherType.ERROR);
        serverInstance.addErrorLogLeecher(errLeecherCurrent);
        serverInstance.addErrorLogLeecher(errLeecherDep1);
        serverInstance.addErrorLogLeecher(errLeecherDep2);
        serverInstance.addErrorLogLeecher(errLeecherBasic);
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
        serverInstance.shutdownServer();
        errLeecherCurrent.waitForText(TIMEOUT);
        errLeecherDep1.waitForText(TIMEOUT);
        errLeecherDep2.waitForText(TIMEOUT);
        errLeecherBasic.waitForText(TIMEOUT);
        serverInstance.removeAllLeechers();
    }

    @Test
    public void testDynamicListenerExecution() throws BallerinaTestException {
        Path projectPath = Paths.get("src", "test", "resources", "packaging", "dynamic_listener_execution");
        runAssertDynamicListener(projectPath);
    }

    @Test
    public void testDynamicListenerDeregister() throws BallerinaTestException {
        Path projectPath = Paths.get("src", "test", "resources", "packaging", "dynamic_listener_deregister");
        runAssertDynamicListener(projectPath);
    }

    @Test
    public void testMultipleDynamicListenersWithAsyncCall() throws BallerinaTestException {
        Path projectPath = Paths.get("src", "test", "resources", "packaging",
                "dynamic_listener_async_call_test_project");
        runAssertDynamicListener(projectPath);
    }

    private void runAssertDynamicListener(Path projectPath) throws BallerinaTestException {
        BServerInstance serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(projectPath.toAbsolutePath().toString(), projectPath.getFileName().toString(), null,
                                   null, null);
        LogLeecher errLeecherA = new LogLeecher("Stopped module A", LogLeecher.LeecherType.ERROR);
        serverInstance.addErrorLogLeecher(errLeecherA);
        serverInstance.shutdownServer();
        errLeecherA.waitForText(TIMEOUT);
        serverInstance.removeAllLeechers();
    }

    @Test
    public void testStopHandlerExecution() throws BallerinaTestException {
        Path projectPath = Paths.get("src", "test", "resources", "packaging", "stop_handler_execution");
        BServerInstance serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(projectPath.toAbsolutePath().toString(), projectPath.getFileName().toString(), null,
                null, null);
        LogLeecher infoLeecher1 = new LogLeecher("Stopped stopHandlerFunc3");
        LogLeecher infoLeecher2 = new LogLeecher("Stopped stopHandlerFunc2");
        LogLeecher infoLeecher3 = new LogLeecher("Stopped stopHandlerFunc1");
        serverInstance.addLogLeecher(infoLeecher1);
        serverInstance.addLogLeecher(infoLeecher2);
        serverInstance.addLogLeecher(infoLeecher3);
        serverInstance.shutdownServer();
        infoLeecher1.waitForText(TIMEOUT);
        infoLeecher2.waitForText(TIMEOUT);
        infoLeecher3.waitForText(TIMEOUT);
        serverInstance.removeAllLeechers();
    }

    @Test
    public void testModuleShutdownRegisteredWithStopHandlers() throws BallerinaTestException {
        Path projectPath = Paths.get("src", "test", "resources", "packaging", "module_shutdown_order_project");

        BServerInstance serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(projectPath.toAbsolutePath().toString(), projectPath.getFileName().toString(), null,
                null, null);
        LogLeecher logLeecherA = new LogLeecher("Stopped current module");
        LogLeecher logLeecherB = new LogLeecher("Stopped moduleB");
        LogLeecher logLeecherC = new LogLeecher("Stopped moduleA");
        LogLeecher logLeecherD = new LogLeecher("Stopped moduleC");
        serverInstance.addLogLeecher(logLeecherA);
        serverInstance.addLogLeecher(logLeecherB);
        serverInstance.addLogLeecher(logLeecherC);
        serverInstance.addLogLeecher(logLeecherD);
        serverInstance.shutdownServer();
        logLeecherA.waitForText(TIMEOUT);
        logLeecherB.waitForText(TIMEOUT);
        logLeecherC.waitForText(TIMEOUT);
        logLeecherD.waitForText(TIMEOUT);
        serverInstance.removeAllLeechers();
    }

    @Test
    public void testStopHandlerExecutionOrder() throws BallerinaTestException {
        Path projectPath = Paths.get("src", "test", "resources", "packaging",
                "stop_handler_execution_order_test");

        BServerInstance serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(projectPath.toAbsolutePath().toString(), projectPath.getFileName().toString(), null,
                null, null);
        LogLeecher logLeecherA = new LogLeecher("StopHandlerFunc3 in current module");
        LogLeecher logLeecherB = new LogLeecher("stopHandlerFunc3 in moduleA");
        LogLeecher logLeecherC = new LogLeecher("StopHandlerFunc5 in current module");
        LogLeecher logLeecherD = new LogLeecher("StopHandlerFunc5 in current module");
        LogLeecher logLeecherE = new LogLeecher("StopHandlerFunc2 in current module");
        LogLeecher logLeecherF = new LogLeecher("Stopped current module");
        LogLeecher logLeecherG = new LogLeecher("stopHandlerFunc1 in moduleA");
        LogLeecher logLeecherH = new LogLeecher("Stopped moduleB", LogLeecher.LeecherType.ERROR);
        serverInstance.addLogLeecher(logLeecherA);
        serverInstance.addLogLeecher(logLeecherB);
        serverInstance.addLogLeecher(logLeecherC);
        serverInstance.addLogLeecher(logLeecherD);
        serverInstance.addLogLeecher(logLeecherE);
        serverInstance.addLogLeecher(logLeecherF);
        serverInstance.addLogLeecher(logLeecherG);
        serverInstance.addErrorLogLeecher(logLeecherH);
        serverInstance.shutdownServer();
        logLeecherA.waitForText(TIMEOUT);
        logLeecherB.waitForText(TIMEOUT);
        logLeecherC.waitForText(TIMEOUT);
        logLeecherD.waitForText(TIMEOUT);
        logLeecherE.waitForText(TIMEOUT);
        logLeecherF.waitForText(TIMEOUT);
        logLeecherG.waitForText(TIMEOUT);
        logLeecherH.waitForText(TIMEOUT);
        serverInstance.removeAllLeechers();
    }

    @Test
    public void testListenerStopHandlerAsyncCall() throws BallerinaTestException {
        Path projectPath = Paths.get("src", "test", "resources", "packaging",
                "listener_stophandler_async_call_test");

        BServerInstance serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(projectPath.toAbsolutePath().toString(), projectPath.getFileName().toString(), null,
                null, null);
        LogLeecher logLeecherD = new LogLeecher("StopHandler2 of current module");
        LogLeecher logLeecherE = new LogLeecher("StopHandler1 of current module");
        LogLeecher logLeecherF = new LogLeecher("StopHandler2 of moduleA");
        LogLeecher logLeecherG = new LogLeecher("StopHandler1 of moduleA");
        LogLeecher logLeecherH = new LogLeecher("graceful stop of current module", LogLeecher.LeecherType.ERROR);
        LogLeecher logLeecherI = new LogLeecher("graceful stop of ModuleA", LogLeecher.LeecherType.ERROR);
        serverInstance.addLogLeecher(logLeecherD);
        serverInstance.addLogLeecher(logLeecherE);
        serverInstance.addLogLeecher(logLeecherF);
        serverInstance.addLogLeecher(logLeecherG);
        serverInstance.addErrorLogLeecher(logLeecherH);
        serverInstance.addErrorLogLeecher(logLeecherI);
        serverInstance.shutdownServer();
        logLeecherD.waitForText(TIMEOUT);
        logLeecherE.waitForText(TIMEOUT);
        logLeecherF.waitForText(TIMEOUT);
        logLeecherG.waitForText(TIMEOUT);
        logLeecherH.waitForText(TIMEOUT);
        logLeecherI.waitForText(TIMEOUT);
        serverInstance.removeAllLeechers();
    }

    @Test
    public void testStopHandlerAsyncCall() throws BallerinaTestException {
        Path projectPath = Paths.get("src", "test", "resources", "packaging",
                "stop_handler_async_call_test");
        BServerInstance serverInstance = new BServerInstance(balServer);
        serverInstance.startServer(projectPath.toAbsolutePath().toString(), projectPath.getFileName().toString(), null,
                null, null);
        LogLeecher infoLeecher1 = new LogLeecher("Stopped stopHandlerFunc3");
        LogLeecher infoLeecher2 = new LogLeecher("Stopped stopHandlerFunc4");
        LogLeecher infoLeecher3 = new LogLeecher("Stopped stopHandlerFunc2");
        LogLeecher infoLeecher4 = new LogLeecher("Stopped stopHandlerFunc1");
        serverInstance.addLogLeecher(infoLeecher1);
        serverInstance.addLogLeecher(infoLeecher2);
        serverInstance.addLogLeecher(infoLeecher3);
        serverInstance.addLogLeecher(infoLeecher4);
        serverInstance.shutdownServer();
        infoLeecher1.waitForText(TIMEOUT);
        infoLeecher2.waitForText(TIMEOUT);
        infoLeecher3.waitForText(TIMEOUT);
        infoLeecher4.waitForText(TIMEOUT);
        serverInstance.removeAllLeechers();
    }
}

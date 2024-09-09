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
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Tests order of execution of listener methods.
 */
public class ModuleExecutionFlowTests extends BaseTest {

    private BMainInstance bMainInstance;
    private final String sourceRoot = Paths.get("src", "test", "resources", "packaging").toAbsolutePath().toString();

    @BeforeClass()
    public void setUp() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test
    public void testModuleExecutionOrder() throws BallerinaTestException {
        runAndAssert("proj1");
    }

    @Test
    public void testModuleMainExecutionOrder() throws BallerinaTestException {
        runAndAssert("proj6");
    }

    @Test
    public void testImportModuleHasListener() throws BallerinaTestException {
        bMainInstance.runMain(this.sourceRoot, "proj2", null, null, new LogLeecher[]{new LogLeecher("Stopped module A",
                LogLeecher.LeecherType.ERROR)});
    }

    private void runAndAssert(String packagePath) throws BallerinaTestException {
        LogLeecher errLeecherA = new LogLeecher("Stopped module A", LogLeecher.LeecherType.ERROR);
        LogLeecher errLeecherB = new LogLeecher("Stopped module B", LogLeecher.LeecherType.ERROR);
        LogLeecher errLeecherC = new LogLeecher("Stopped module C", LogLeecher.LeecherType.ERROR);
        LogLeecher[] leechers = new LogLeecher[]{errLeecherA, errLeecherB, errLeecherC};
        bMainInstance.runMain(this.sourceRoot, packagePath, leechers);
    }

    @Test(description = "Test 'init' is called only once for each module at runtime")
    public void testModuleDependencyChainForInit() throws BallerinaTestException {
        LogLeecher errLeecherCurrent = new LogLeecher("Stopped module current", LogLeecher.LeecherType.ERROR);
        LogLeecher errLeecherDep1 = new LogLeecher("Stopped module second dependent", LogLeecher.LeecherType.ERROR);
        LogLeecher errLeecherDep2 = new LogLeecher("Stopped module first dependent", LogLeecher.LeecherType.ERROR);
        LogLeecher errLeecherBasic = new LogLeecher("Stopped module basic", LogLeecher.LeecherType.ERROR);
        LogLeecher[] leechers = new LogLeecher[]{errLeecherCurrent, errLeecherDep1, errLeecherDep2, errLeecherBasic};
        bMainInstance.runMain(this.sourceRoot, "module_invocation_project", null, null, leechers);
    }

    @Test
    public void testDynamicListenerExecution() throws BallerinaTestException {
        runAssertDynamicListener("dynamic_listener_execution");
    }

    @Test
    public void testDynamicListenerDeregister() throws BallerinaTestException {
        runAssertDynamicListener("dynamic_listener_deregister");
    }

    @Test
    public void testMultipleDynamicListenersWithAsyncCall() throws BallerinaTestException {
        runAssertDynamicListener("dynamic_listener_async_call_test_project");
    }

    private void runAssertDynamicListener(String packagePath) throws BallerinaTestException {
        LogLeecher errLeecherA = new LogLeecher("Stopped module A", LogLeecher.LeecherType.ERROR);
        LogLeecher[] leechers = new LogLeecher[]{errLeecherA};
        bMainInstance.runMain(this.sourceRoot, packagePath, null, null, leechers);
    }

    @Test
    public void testStopHandlerExecution() throws BallerinaTestException {
        LogLeecher infoLeecher1 = new LogLeecher("Stopped stopHandlerFunc3");
        LogLeecher infoLeecher2 = new LogLeecher("Stopped stopHandlerFunc2");
        LogLeecher infoLeecher3 = new LogLeecher("Stopped stopHandlerFunc1");
        LogLeecher infoLeecher4 = new LogLeecher("Stopped inlineStopHandler");
        LogLeecher[] leechers = new LogLeecher[]{infoLeecher1, infoLeecher2, infoLeecher3, infoLeecher4};
        bMainInstance.runMain(this.sourceRoot, "stop_handler_execution", null, null, leechers);
    }

    @Test
    public void testModuleShutdownRegisteredWithStopHandlers() throws BallerinaTestException {
        LogLeecher logLeecherA = new LogLeecher("Stopped current module");
        LogLeecher logLeecherB = new LogLeecher("Stopped moduleB");
        LogLeecher logLeecherC = new LogLeecher("Stopped moduleA");
        LogLeecher logLeecherD = new LogLeecher("Stopped moduleC");
        LogLeecher[] leechers = new LogLeecher[]{logLeecherA, logLeecherB, logLeecherC, logLeecherD};
        bMainInstance.runMain(this.sourceRoot, "module_shutdown_order_project", null, null, leechers);
    }

    @Test
    public void testStopHandlerExecutionOrder() throws BallerinaTestException {
        LogLeecher logLeecherA = new LogLeecher("StopHandlerFunc3 in current module");
        LogLeecher logLeecherB = new LogLeecher("stopHandlerFunc3 in moduleA");
        LogLeecher logLeecherC = new LogLeecher("StopHandlerFunc5 in current module");
        LogLeecher logLeecherD = new LogLeecher("StopHandlerFunc5 in current module");
        LogLeecher logLeecherE = new LogLeecher("StopHandlerFunc2 in current module");
        LogLeecher logLeecherF = new LogLeecher("Stopped current module");
        LogLeecher logLeecherG = new LogLeecher("stopHandlerFunc1 in moduleA");
        LogLeecher logLeecherH = new LogLeecher("Stopped moduleB", LogLeecher.LeecherType.ERROR);
        LogLeecher[] leechers = new LogLeecher[]{logLeecherA, logLeecherB, logLeecherC, logLeecherD, logLeecherE,
                logLeecherF, logLeecherG, logLeecherH};
        bMainInstance.runMain(this.sourceRoot, "stop_handler_execution_order_test", null, null, leechers);
    }

    @Test
    public void testListenerStopHandlerShutdownOrderWithErrorReturn() throws BallerinaTestException {
        LogLeecher logLeecherA = new LogLeecher("Calling stop for static listener");
        LogLeecher logLeecherB = new LogLeecher("error: error during the gracefulStop call of static listener",
                LogLeecher.LeecherType.ERROR);
        LogLeecher logLeecherC = new LogLeecher("Calling stop for dynamic listener");
        LogLeecher logLeecherD = new LogLeecher("error: error during the gracefulStop call of dynamic listener",
                LogLeecher.LeecherType.ERROR);
        LogLeecher logLeecherE = new LogLeecher("stopHandler2 called");
        LogLeecher logLeecherF = new LogLeecher("error: error during the gracefulStop call of StopHandler2",
                LogLeecher.LeecherType.ERROR);
        LogLeecher logLeecherI = new LogLeecher("stopHandler1 called");
        LogLeecher[] leechers = new LogLeecher[]{logLeecherA, logLeecherB, logLeecherC, logLeecherD, logLeecherE,
                logLeecherF, logLeecherI};
        bMainInstance.runMain(this.sourceRoot, "listener_stophandler_shutdown_order_project", null, null, leechers);
    }

    @Test
    public void testListenerStopHandlerAsyncCall() throws BallerinaTestException {
        LogLeecher logLeecherA = new LogLeecher("calling gracefulStop for dynamic listener of current module");
        LogLeecher logLeecherB = new LogLeecher("calling StopHandler2 of current module");
        LogLeecher logLeecherC = new LogLeecher("calling StopHandler1 of current module");
        LogLeecher logLeecherD = new LogLeecher("calling StopHandler2 of moduleA");
        LogLeecher logLeecherE = new LogLeecher("calling StopHandler1 of moduleA");
        LogLeecher[] leechers = new LogLeecher[]{logLeecherA, logLeecherB, logLeecherC, logLeecherD, logLeecherE};
        bMainInstance.runMain(this.sourceRoot, "listener_stophandler_async_call_test", null, null, leechers);
    }

    @Test
    public void testStopHandlerAsyncCall() throws BallerinaTestException {
        LogLeecher infoLeecher1 = new LogLeecher("Stopped stopHandlerFunc3");
        LogLeecher infoLeecher2 = new LogLeecher("Stopped stopHandlerFunc4");
        LogLeecher infoLeecher3 = new LogLeecher("Stopped stopHandlerFunc2");
        LogLeecher infoLeecher4 = new LogLeecher("Stopped stopHandlerFunc1");
        LogLeecher[] leechers = new LogLeecher[]{infoLeecher1, infoLeecher2, infoLeecher3, infoLeecher4};
        bMainInstance.runMain(this.sourceRoot, "stop_handler_async_call_test", null, null, leechers);
    }

    @Test
    public void testModuleExecuteFunctionOrder() throws BallerinaTestException {
        LogLeecher listenerInitLeecher = new LogLeecher("Calling init for 'current'", LogLeecher.LeecherType.INFO);
        LogLeecher moduleInitLeecher = new LogLeecher("Initializing module 'current'", LogLeecher.LeecherType.INFO);
        LogLeecher mainLeecher =
                new LogLeecher("main function invoked for 'current' module", LogLeecher.LeecherType.INFO);
        LogLeecher listenerStartLeecher = new LogLeecher("Calling start for 'current'", LogLeecher.LeecherType.INFO);
        LogLeecher[] leechers = new LogLeecher[]{listenerInitLeecher, moduleInitLeecher, mainLeecher,
                listenerStartLeecher};
        bMainInstance.runMain(this.sourceRoot, "module_execute_invocation_project", null, null, leechers);
    }

    @Test
    public void testModuleInitWithBusyWorkerAndListener() throws BallerinaTestException {
        LogLeecher listenerInitLeecher = new LogLeecher("Calling init for 'current'", LogLeecher.LeecherType.INFO);
        LogLeecher moduleInitLeecher = new LogLeecher("Initializing module 'current'", LogLeecher.LeecherType.INFO);
        LogLeecher mainLeecher = new LogLeecher("main function invoked for 'current' module",
                LogLeecher.LeecherType.INFO);
        LogLeecher listenerStartLeecher = new LogLeecher("Calling start for 'current'", LogLeecher.LeecherType.INFO);
        LogLeecher workerLeecher = new LogLeecher("executing worker 'w1'", LogLeecher.LeecherType.INFO);
        LogLeecher[] leechers = new LogLeecher[]{listenerInitLeecher, moduleInitLeecher, mainLeecher,
                listenerStartLeecher, workerLeecher};
        bMainInstance.runMain(this.sourceRoot, "module_init_worker_project", null, null, leechers);
    }

    @Test
    public void testModuleInitWithBusyWorkerAndDynamicListener() throws BallerinaTestException {
        LogLeecher moduleInitLeecher = new LogLeecher("Initializing module 'current'", LogLeecher.LeecherType.INFO);
        LogLeecher mainLeecher = new LogLeecher("main function invoked for 'current' module",
                LogLeecher.LeecherType.INFO);
        LogLeecher workerLeecher = new LogLeecher("executing worker 'w1'", LogLeecher.LeecherType.INFO);
        LogLeecher listenerInitLeecher = new LogLeecher("Calling init for 'dynamic'", LogLeecher.LeecherType.INFO);
        LogLeecher listenerStartLeecher = new LogLeecher("Calling start for 'dynamic'", LogLeecher.LeecherType.INFO);
        LogLeecher listenerStopLeecher = new LogLeecher("Calling stop for 'dynamic'", LogLeecher.LeecherType.INFO);
        LogLeecher[] leechers = new LogLeecher[]{moduleInitLeecher, mainLeecher, workerLeecher, listenerInitLeecher,
                listenerStartLeecher, listenerStopLeecher};
        bMainInstance.runMain(this.sourceRoot, "module_init_worker_dynamic_listener_project", null, null, leechers);
    }

    @Test
    public void testModuleInitWithBusyWorkerTerminating() throws BallerinaTestException {
        LogLeecher moduleInitLeecher = new LogLeecher("Initializing module 'current'", LogLeecher.LeecherType.INFO);
        LogLeecher mainLeecher = new LogLeecher("main function invoked for 'current' module",
                LogLeecher.LeecherType.INFO);
        LogLeecher workerLeecher = new LogLeecher("executing worker 'w1'", LogLeecher.LeecherType.INFO);
        LogLeecher[] leechers = new LogLeecher[]{moduleInitLeecher, mainLeecher, workerLeecher};
        bMainInstance.runMain(this.sourceRoot, "module_init_worker_no_listener_project", null, null, leechers);
    }
}

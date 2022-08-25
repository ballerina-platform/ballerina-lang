/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.execution;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.BRunUtil.ExitDetails;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for program execution order tests.
 *
 * @since 1.0.0
 */
public class ModuleExecutionFlowTests {

    @Test
    public void testModuleInitReturningError() {
        CompileResult compileResult = BCompileUtil.compileWithoutInitInvocation("test-src/execution/proj2");
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module a" + System.lineSeparator() +
                "Initializing module b";
        String expectedErrorString = "error: error returned while initializing module B";
        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleStartReturningError() {
        CompileResult compileResult = BCompileUtil.compileWithoutInitInvocation("test-src/execution/proj3");
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module a" + System.lineSeparator() +
                "Initializing module b" + System.lineSeparator() +
                "Initializing module c" + System.lineSeparator() +
                "Module c main function invoked" + System.lineSeparator() +
                "a:ABC listener start called, service name - ModA" + System.lineSeparator() +
                "a:ABC listener start called, service name - ModB" + System.lineSeparator() +
                "a:ABC listener gracefulStop called, service name - ModB" + System.lineSeparator() +
                "a:ABC listener gracefulStop called, service name - ModA";

        String expectedErrorString = "error: error returned while starting module B";
        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleInitPanic() {
        CompileResult compileResult = BCompileUtil.compileWithoutInitInvocation("test-src/execution/proj4");
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module a" + System.lineSeparator() +
                "Initializing module b";
        String expectedErrorString = "error: panicked while initializing module B" + System.lineSeparator() +
                "\tat unit_tests.proj4.b.0:init(main.bal:6)";
        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testListenerInitReturnsError() {
        CompileResult compileResult = BCompileUtil.compileWithoutInitInvocation(
                "test-src/execution/listener_return_error_project");
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "init invoked";
        String expectedErrorString = "error: Listener init failed";
        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testListenerInitReturnsErrorInServiceDecl() {
        CompileResult compileResult = BCompileUtil.compileWithoutInitInvocation(
                "test-src/execution/service_decl_listener_return_error_project");
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "init invoked" + System.lineSeparator() +
                "init invoked" + System.lineSeparator() +
                "init invoked";
        String expectedErrorString = "error: ModA-inline-2 errored!!!";
        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleStartPanic() {
        CompileResult compileResult = BCompileUtil.compileWithoutInitInvocation("test-src/execution/proj5");
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module a" + System.lineSeparator() +
                "Initializing module b" + System.lineSeparator() +
                "Initializing module c" + System.lineSeparator() +
                "Module c main function invoked" + System.lineSeparator() +
                "a:ABC listener start called, service name - ModA" + System.lineSeparator() +
                "a:ABC listener start called, service name - ModB" + System.lineSeparator() +
                "a:ABC listener gracefulStop called, service name - ModB" + System.lineSeparator() +
                "a:ABC listener gracefulStop called, service name - ModA";

        String expectedErrorString = "error: panicked while starting module B" + System.lineSeparator() +
                "\tat unit_tests.proj5.a.0.ABC:start(main.bal:21)";
        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleMainReturnError() {
        CompileResult compileResult = BCompileUtil.compileWithoutInitInvocation("test-src/execution/proj7");
        ExitDetails output = run(compileResult, new String[]{});

        String expectedString = "Initializing module a" + System.lineSeparator() +
                "Initializing module b" + System.lineSeparator() +
                "Initializing module c" + System.lineSeparator() +
                "Module c main function invoked";
        String expectedErrorString = "error: error returned while executing main method";
        Assert.assertEquals(output.consoleOutput, expectedString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleMainPanicError() {
        CompileResult compileResult = BCompileUtil.compileWithoutInitInvocation("test-src/execution/proj8");
        ExitDetails output = run(compileResult, new String[]{});

        String expectedString = "Initializing module a" + System.lineSeparator() +
                "Initializing module b" + System.lineSeparator() +
                "Initializing module c" + System.lineSeparator() +
                "Module c main function invoked";
        String expectedErrorString = "error: panicked while executing main method" + System.lineSeparator() +
                "\tat unit_tests.proj8.0:main(main.bal:12)";
        Assert.assertEquals(output.consoleOutput, expectedString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleStartAndStopPanic() {
        CompileResult compileResult =
                BCompileUtil.compileWithoutInitInvocation("test-src/execution/start_stop_failing_project");
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module 'basic'" + System.lineSeparator() +
                "Initializing module 'dependent'" + System.lineSeparator() +
                "Initializing module 'current'" + System.lineSeparator() +
                "main function invoked for current module" + System.lineSeparator() +
                "basic:TestListener listener start called, service name - basic" + System.lineSeparator() +
                "basic:TestListener listener start called, service name - dependent" + System.lineSeparator() +
                "listener start panicked for service name - dependent" + System.lineSeparator() +
                "basic:TestListener listener gracefulStop called, service name - dependent" + System.lineSeparator() +
                "listener gracefulStop panicked, service name - dependent" + System.lineSeparator() +
                "basic:TestListener listener gracefulStop called, service name - basic";

        String expectedErrorString = "error: panicked while starting module 'dependent'" + System.lineSeparator() +
                "\tat testorg.start_stop_failing_project:start(basic.bal:35)";
        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    private ExitDetails run(CompileResult compileResult, String[] args) {
        try {
            return BRunUtil.run(compileResult, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}

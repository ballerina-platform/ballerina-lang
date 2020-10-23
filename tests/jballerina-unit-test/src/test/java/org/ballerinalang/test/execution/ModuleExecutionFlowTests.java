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

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BCompileUtil.ExitDetails;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for program execution order tests.
 *
 * @since 1.0.0
 */
public class ModuleExecutionFlowTests {

    @Test
    public void testModuleExecutionOrder() {
        CompileResult compileResult = BCompileUtil.compile("test-src/execution/proj1", "c", false);
        ExitDetails output = run(compileResult, new String[]{});

        String expectedString = "Initializing module a\n" +
                "Initializing module b\n" +
                "Initializing module c\n" +
                "Module c main function invoked\n" +
                "a:ABC listener __start called, service name - ModA\n" +
                "a:ABC listener __start called, service name - ModB\n" +
                "a:ABC listener __start called, service name - ModC\n" +
                "a:ABC listener __gracefulStop called, service name - ModC\n" +
                "a:ABC listener __gracefulStop called, service name - ModB\n" +
                "a:ABC listener __gracefulStop called, service name - ModA";

        Assert.assertEquals(output.consoleOutput, expectedString, "evaluated to invalid value");
    }

    @Test
    public void testModuleInitReturningError() {
        CompileResult compileResult = BCompileUtil.compile("test-src/execution/proj2", "c", false);
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module a\n" +
                "Initializing module b";
        String expectedErrorString = "error: error returned while initializing module B {}";
        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleStartReturningError() {
        CompileResult compileResult = BCompileUtil.compile("test-src/execution/proj3", "c", false);
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module a\n" +
                "Initializing module b\n" +
                "Initializing module c\n" +
                "Module c main function invoked\n" +
                "a:ABC listener __start called, service name - ModA\n" +
                "a:ABC listener __start called, service name - ModB\n" +
                "a:ABC listener __gracefulStop called, service name - ModB\n" +
                "a:ABC listener __gracefulStop called, service name - ModA";

        String expectedErrorString = "error: error returned while starting module B {}";
        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleInitPanic() {
        CompileResult compileResult = BCompileUtil.compile("test-src/execution/proj4", "c", false);
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module a\n" +
                "Initializing module b";
        String expectedErrorString = "error: panicked while initializing module B\n" +
                "\tat unit-tests.b.0_1_0:init(main.bal:6)";
        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleStartPanic() {
        CompileResult compileResult = BCompileUtil.compile("test-src/execution/proj5", "c", false);
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module a\n" +
                "Initializing module b\n" +
                "Initializing module c\n" +
                "Module c main function invoked\n" +
                "a:ABC listener __start called, service name - ModA\n" +
                "a:ABC listener __start called, service name - ModB\n" +
                "a:ABC listener __gracefulStop called, service name - ModB\n" +
                "a:ABC listener __gracefulStop called, service name - ModA";

        String expectedErrorString = "error: panicked while starting module B\n" +
                "\tat unit-tests.a.0_1_0.ABC:__start(main.bal:23)";
        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleImmediateStopPanic() {
        CompileResult compileResult = BCompileUtil.compile("test-src/execution/proj6", "c", false);
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module a\n" +
                "Initializing module b\n" +
                "Initializing module c\n" +
                "Module c main function invoked\n" +
                "a:ABC listener __start called, service name - ModA\n" +
                "a:ABC listener __start called, service name - ModB\n" +
                "a:ABC listener __start called, service name - ModC\n" +
                "a:ABC listener __gracefulStop called, service name - ModC\n" +
                "a:ABC listener __gracefulStop called, service name - ModB\n" +
                "a:ABC listener __gracefulStop called, service name - ModA";

        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
    }

    @Test
    public void testModuleMainReturnError() {
        CompileResult compileResult = BCompileUtil.compile("test-src/execution/proj7", "c", false);
        ExitDetails output = run(compileResult, new String[]{});

        String expectedString = "Initializing module a\n" +
                "Initializing module b\n" +
                "Initializing module c\n" +
                "Module c main function invoked";
        String expectedErrorString = "error: error returned while executing main method {}";
        Assert.assertEquals(output.consoleOutput, expectedString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleMainPanicError() {
        CompileResult compileResult = BCompileUtil.compile("test-src/execution/proj8", "c", false);
        ExitDetails output = run(compileResult, new String[]{});

        String expectedString = "Initializing module a\n" +
                "Initializing module b\n" +
                "Initializing module c\n" +
                "Module c main function invoked";
        String expectedErrorString = "error: panicked while executing main method\n" +
                "\tat unit-tests.c.0_1_0:main(main.bal:12)";
        Assert.assertEquals(output.consoleOutput, expectedString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleStartAndStopPanic() {
        CompileResult compileResult =
                BCompileUtil.compile("test-src/execution/StartStopFailingProject", "current", false);
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module 'basic'\n" +
                "Initializing module 'dependent'\n" +
                "Initializing module 'current'\n" +
                "main function invoked for current module\n" +
                "basic:TestListener listener __start called, service name - basic\n" +
                "basic:TestListener listener __start called, service name - dependent\n" +
                "listener __start panicked for service name - dependent\n" +
                "basic:TestListener listener __gracefulStop called, service name - dependent\n" +
                "listener __gracefulStop panicked, service name - dependent\n" +
                "basic:TestListener listener __gracefulStop called, service name - basic";

        String expectedErrorString = "error: panicked while starting module 'dependent'\n" +
                "\tat test.basic.0_1_0.TestListener:__start(main.bal:40)";
        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test
    public void testModuleStopPanic() {
        CompileResult compileResult =
                BCompileUtil.compile("test-src/execution/ModuleStopFailingProject", "current", false);
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module 'basic'\n" +
                "Initializing module 'dependent'\n" +
                "Initializing module 'current'\n" +
                "main function invoked for current module\n" +
                "basic:TestListener listener __start called, service name - basic\n" +
                "basic:TestListener listener __start called, service name - dependent\n" +
                "basic:TestListener listener __start called, service name - current\n" +
                "basic:TestListener listener __gracefulStop called, service name - current\n" +
                "basic:TestListener listener __gracefulStop called, service name - dependent\n" +
                "listener __gracefulStop panicked, service name - dependent\n" +
                "basic:TestListener listener __gracefulStop called, service name - basic";
        String expectedErrorString = "error: panicked while stopping module 'dependent'\n" +
                "\tat test.basic.0_1_0.TestListener:__gracefulStop(main.bal:44)";

        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
        Assert.assertEquals(output.errorOutput, expectedErrorString, "evaluated to invalid value");
    }

    @Test(description = "Test 'init' is called only once for each module at runtime")
    public void testModuleDependencyChainForInit() {
        CompileResult compileResult =
                BCompileUtil.compile("test-src/execution/ModuleInitInvocationProject", "current", false);
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module 'basic'\n" +
                "Initializing module 'dependent1'\n" +
                "Initializing module 'dependent2'\n" +
                "Initializing module 'current'\n" +
                "main function invoked for current module\n" +
                "basic:TestListener listener __start called, service name - basic\n" +
                "basic:TestListener listener __start called, service name - first dependent\n" +
                "basic:TestListener listener __start called, service name - second dependent\n" +
                "basic:TestListener listener __start called, service name - current\n" +
                "basic:TestListener listener __gracefulStop called, service name - current\n" +
                "basic:TestListener listener __gracefulStop called, service name - second dependent\n" +
                "basic:TestListener listener __gracefulStop called, service name - first dependent\n" +
                "basic:TestListener listener __gracefulStop called, service name - basic";

        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
    }

    @Test(description = "Test 'start' is called only once for each module at runtime")
    public void testModuleDependencyChainForStart() {
        CompileResult compileResult =
                BCompileUtil.compile("test-src/execution/ModuleStartInvocationProject", "current", false);
        ExitDetails output = run(compileResult, new String[]{});

        String expectedConsoleString = "Initializing module 'basic'\n" +
                "Initializing module 'dependent1'\n" +
                "Initializing module 'dependent2'\n" +
                "Initializing module 'current'\n" +
                "main function invoked for current module\n" +
                "basic:TestListener listener __start called, service name - basic\n" +
                "basic:TestListener listener __start called, service name - first dependent\n" +
                "basic:TestListener listener __start called, service name - second dependent\n" +
                "basic:TestListener listener __start called, service name - current\n" +
                "basic:TestListener listener __gracefulStop called, service name - current\n" +
                "basic:TestListener listener __gracefulStop called, service name - second dependent\n" +
                "basic:TestListener listener __gracefulStop called, service name - first dependent\n" +
                "basic:TestListener listener __gracefulStop called, service name - basic";

        Assert.assertEquals(output.consoleOutput, expectedConsoleString, "evaluated to invalid value");
    }

    private ExitDetails run(CompileResult compileResult, String[] args) {
        try {
            return BCompileUtil.run(compileResult, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}

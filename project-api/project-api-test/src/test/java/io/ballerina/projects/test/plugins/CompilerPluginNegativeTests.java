/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects.test.plugins;

import io.ballerina.projects.ProjectException;
import io.ballerina.projects.test.TestUtils;
import org.ballerinalang.test.BCompileUtil;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains negative cases to test compiler plugin loading and running.
 *
 * @since 2.0.0
 */
public class CompilerPluginNegativeTests {

    private static final String EXCEPTION_MESSAGE_1 = "Failed to load the compiler plugin in package: " +
            "'samjs:package_compiler_plugin_1:1.1.0'. Cannot find class " +
            "'io.samjs.plugins.badsad.BadSadCompilerPlugin1'";
    private static final String EXCEPTION_MESSAGE_3 = "Failed to load the compiler " +
            "plugin in package: 'samjs:package_compiler_plugin_3:1.1.0'. " +
            "Specified class 'io.samjs.plugins.badsad.BadSadCompilerPlugin3' is not a " +
            "subclass of 'io.ballerina.projects.plugins.CompilerPlugin'";
    private static final String EXCEPTION_MESSAGE_4 = "Failed to load the compiler plugin in package: " +
            "'samjs:package_compiler_plugin_4:1.1.0'. Cannot find the default constructor in class: " +
            "'io.samjs.plugins.badsad.BadSadCompilerPlugin4'";
    private static final String EXCEPTION_MESSAGE_5 = "Failed to load the compiler plugin in package: " +
            "'samjs:package_compiler_plugin_5:1.1.0'. Cannot create a new instance of the class " +
            "'io.samjs.plugins.badsad.BadSadCompilerPlugin5', reason: java.lang.NoClassDefFoundError: " +
            "io/samjs/jarlibrary/diagnosticutils/DiagnosticUtils";
    private static final String EXCEPTION_MESSAGE_6 = "Failed to load the compiler plugin in package: " +
            "'samjs:package_compiler_plugin_6:1.1.0'. io/samjs/jarlibrary/diagnosticutils/DiagnosticUtils";
    private static final String EXCEPTION_MESSAGE_20 = "Failed to initialize the compiler plugin in package: " +
            "'samjs:package_compiler_plugin_20:1.1.0'. null";
    private static final String EXCEPTION_MESSAGE_21 = "Failed to initialize the compiler plugin in package: " +
            "'samjs:package_compiler_plugin_21:1.1.0'. The value cannot be less than zero";
    private static final String EXCEPTION_MESSAGE_40 = "The compiler extension in package " +
            "'samjs:package_compiler_plugin_40:1.1.0' failed to complete. The value cannot be less than zero";
    private static final String EXCEPTION_MESSAGE_41 = "The compiler extension in package " +
            "'samjs:package_compiler_plugin_41:1.1.0' failed to complete. The value cannot be less than zero";

    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/compiler_plugin_tests/negative_cases").toAbsolutePath();

    @Test(expectedExceptions = ProjectException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_1)
    public void testLoadingCompilerPluginCase1() {
        loadCompilationPackage(1);
    }

    @Test(expectedExceptions = ProjectException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_3)
    public void testLoadingCompilerPluginCase3() {
        loadCompilationPackage(3);
    }

    @Test(expectedExceptions = ProjectException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_4)
    public void testLoadingCompilerPluginCase4() {
        loadCompilationPackage(4);
    }

    @Test(expectedExceptions = ProjectException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_5)
    public void testLoadingCompilerPluginCase5() {
        loadCompilationPackage(5);
    }

    @Test(expectedExceptions = ProjectException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_6)
    public void testLoadingCompilerPluginCase6() {
        loadCompilationPackage(6);
    }

    @Test(expectedExceptions = ProjectException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_20)
    public void testLoadingCompilerPluginCase20() {
        loadCompilationPackage(20);
    }

    @Test(expectedExceptions = ProjectException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_21)
    public void testLoadingCompilerPluginCase21() {
        loadCompilationPackage(21);
    }

    @Test(expectedExceptions = ProjectException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_40)
    public void testLoadingCompilerPluginCase40() {
        loadCompilationPackage(40);
    }

    @Test(expectedExceptions = ProjectException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE_41)
    public void testLoadingCompilerPluginCase41() {
        loadCompilationPackage(41);
    }

    private void loadCompilationPackage(int testCase) {
        BCompileUtil.compileAndCacheBala(
                "compiler_plugin_tests/negative_cases/package_compiler_plugin_" + testCase);
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_that_imports_plugin_" + testCase);
        TestUtils.loadBuildProject(projectDirPath).currentPackage().getCompilation();
    }
}

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

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains cases to test compiler plugin loading and running.
 *
 * @since 2.0.0
 */
public class CompilerPluginTests {
    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/compiler_plugin_tests").toAbsolutePath();
    private static final PrintStream OUT = System.out;

    @BeforeSuite
    public void init() {
        BCompileUtil.compileAndCacheBala("compiler_plugin_tests/package_comp_plugin_1");
        BCompileUtil.compileAndCacheBala(
                "compiler_plugin_tests/package_comp_plugin_with_one_java_dependency");
        BCompileUtil.compileAndCacheBala(
                "compiler_plugin_tests/package_comp_plugin_with_two_java_dependencies");
        BCompileUtil.compileAndCacheBala(
                "compiler_plugin_tests/package_comp_plugin_with_func_node_analyzer");
    }

    @Test
    public void testCompilerPluginBasic() {
        Package currentPackage = loadPackage("package_plugin_user_1");
        PackageCompilation compilation = currentPackage.getCompilation();

        // TODO Use diagnostics to check test the execution of compiler plugins

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.errors().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        Assert.assertEquals(currentPackage.packageDependencies().size(), 1,
                "Unexpected number of dependencies");
    }

    @Test
    public void testCompilerPluginWithOneJavaLibDependency() {
        assertDiagnostics(loadPackage("package_plugin_user_2"));
    }

    @Test
    public void testCompilerPluginWithTwoJavaLibDependencies() {
        Package currentPackage = loadPackage("package_plugin_user_3");
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = currentPackage.getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 5,
                "Unexpected number of compilation diagnostics");

        Assert.assertEquals(diagnosticResult.errorCount(), 1);
        Assert.assertEquals(diagnosticResult.warningCount(), 4);
    }

    @Test
    public void testFunctionNodeAnalyzerCompilerPlugin() {
        Package currentPackage = loadPackage("package_plugin_user_4");
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = currentPackage.getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 6,
                "Unexpected number of compilation diagnostics");

        Assert.assertEquals(diagnosticResult.errorCount(), 3);
        Assert.assertEquals(diagnosticResult.warningCount(), 3);
    }

    @Test
    public void testTheExistenceOfMultipleCompilerPlugins() {
        Package currentPackage = loadPackage("package_plugin_user_5");
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = currentPackage.getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 16,
                "Unexpected number of compilation diagnostics");

        Assert.assertEquals(diagnosticResult.errorCount(), 7);
        Assert.assertEquals(diagnosticResult.warningCount(), 9);
    }

    public void assertDiagnostics(Package currentPackage) {
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = currentPackage.getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 2,
                "Unexpected number of compilation diagnostics");

        // Sort diagnostics based on the message
        List<Diagnostic> reportedDiagnostics = diagnosticResult.diagnostics()
                .stream()
                .sorted(Comparator.comparing(Diagnostic::message))
                .collect(Collectors.toList());

        Assert.assertEquals(reportedDiagnostics.get(0).diagnosticInfo().severity(), DiagnosticSeverity.ERROR);
        Assert.assertEquals(reportedDiagnostics.get(1).diagnosticInfo().severity(), DiagnosticSeverity.WARNING);

        // Check direct package dependencies
        Assert.assertEquals(currentPackage.packageDependencies().size(), 1,
                "Unexpected number of dependencies");
    }

    private Package loadPackage(String path) {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve(path);
        BuildProject buildProject = BuildProject.load(projectDirPath);
        return buildProject.currentPackage();
    }
}

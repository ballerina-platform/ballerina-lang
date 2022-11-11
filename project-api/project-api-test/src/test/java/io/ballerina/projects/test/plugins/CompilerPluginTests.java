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

import io.ballerina.projects.CodeGeneratorResult;
import io.ballerina.projects.CodeModifierResult;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.Resource;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.test.TestUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
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
        BCompileUtil.compileAndCacheBala(
                "compiler_plugin_tests/package_comp_plugin_lifecycle_listener");
        BCompileUtil.compileAndCacheBala(
                "compiler_plugin_tests/package_comp_plugin_with_codeactions");
        BCompileUtil.compileAndCacheBala(
                "compiler_plugin_tests/package_comp_plugin_codegen_init_function");
        BCompileUtil.compileAndCacheBala(
                "compiler_plugin_tests/package_comp_plugin_code_modify_add_function");
        BCompileUtil.compileAndCacheBala(
                "compiler_plugin_tests/package_comp_plugin_diagnostic_init_function");
        BCompileUtil.compileAndCacheBala(
                "compiler_plugin_tests/immutable_type_definition_with_code_modifier_test/defns");
    }

    @Test
    public void testCompilerPluginBasic() {
        Package currentPackage = loadPackage("package_plugin_user_1");
        PackageCompilation compilation = currentPackage.getCompilation();

        // TODO Use diagnostics to check test the execution of compiler plugins

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.diagnostics().forEach(OUT::println);
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

    @Test
    public void testCodeGenerationCompletedPlugin() {
        String path = RESOURCE_DIRECTORY.resolve("package_plugin_user_6").toString();
        CompileResult result = BCompileUtil.compileAndCacheBala(path);
        Assert.assertEquals(result.getWarnCount(), 1);
        BAssertUtil.validateWarning(result, 0, "End of codegen", 1, 1);
    }

    @Test
    public void testInBuiltCompilerPluginBuildProject() throws IOException {
        Package currentPackage = loadPackage("package_test_inbuilt_plugin");
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = currentPackage.getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 3,
                "Unexpected number of compilation diagnostics");
        Assert.assertEquals(diagnosticResult.errorCount(), 1);
        Assert.assertEquals(diagnosticResult.warningCount(), 2);

        Path logFilePath = Paths.get("build/logs/diagnostics.log");
        Assert.assertTrue(Files.exists(logFilePath));
        String logFileContent = Files.readString(logFilePath, Charset.defaultCharset());
        Assert.assertTrue(logFileContent.contains(diagnosticResult.warnings().stream().findFirst().get().toString()));
        Assert.assertTrue(logFileContent.contains(diagnosticResult.errors().stream().findFirst().get().toString()));
    }

    @Test
    public void testInBuiltCompilerPluginSingleFile() throws IOException {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("package_test_inbuilt_plugin/single-file/main.bal");
        Package currentPackage = TestUtils.loadSingleFileProject(projectDirPath).currentPackage();
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = currentPackage.getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 2,
                "Unexpected number of compilation diagnostics");
        Assert.assertEquals(diagnosticResult.errorCount(), 1);
        Assert.assertEquals(diagnosticResult.warningCount(), 1);

        Path logFilePath = Paths.get("build/logs/single-file/diagnostics.log");
        Assert.assertTrue(Files.exists(logFilePath));
        String logFileContent = Files.readString(logFilePath, Charset.defaultCharset());
        Assert.assertTrue(logFileContent.contains(diagnosticResult.warnings().stream().findFirst().get().toString()));
        Assert.assertTrue(logFileContent.contains(diagnosticResult.errors().stream().findFirst().get().toString()));
    }

    @Test(description = "Test `package-semantic-analyzer` compiler plugin by checking invalid export "
            + "modules in `Ballerina.toml`")
    public void testPkgInvalidExportedModule() {
        Package currentPackage = loadPackage("package_invalid_exported_modules");
        DiagnosticResult diagnosticResult = currentPackage.getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 3, "Unexpected number of compilation diagnostics");
        Iterator<Diagnostic> diagnosticIterator = diagnosticResult.diagnostics().iterator();
        Assert.assertEquals(diagnosticIterator.next().toString(), "ERROR [Ballerina.toml:(8:1,8:65)] "
                + "could not locate dependency path '../libs/ballerina-runtime-api-2.0.0-beta.2-SNAPSHOT.jar'");
        Assert.assertEquals(diagnosticIterator.next().toString(), "ERROR [Ballerina.toml:(5:11,5:16)] "
                + "exported module 'abc' is not a module of the package");
        Assert.assertEquals(diagnosticIterator.next().toString(), "ERROR [Ballerina.toml:(5:18,5:23)] "
                + "exported module 'xyz' is not a module of the package");
    }

    @Test(description = "Test `package-toml-semantic-analyzer` compiler plugin by checking valid export "
            + "modules in `Ballerina.toml`")
    public void testPkgValidExportedModule() {
        Package currentPackage = loadPackage("package_valid_exported_modules");
        DiagnosticResult diagnosticResult = currentPackage.getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0, "Unexpected diagnostics exists");
    }

    @Test
    public void testCompilerPluginWithNoCodeGenerators() {
        Package currentPackage = loadPackage("package_plugin_user_1");

        // Check the document count in the current package
        Assert.assertEquals(1, currentPackage.getDefaultModule().documentIds().size());

        //  Running the compilation
        currentPackage.getCompilation();

        // Running the code generation
        CodeGeneratorResult codeGeneratorResult = currentPackage.runCodeGeneratorPlugins();

        // Compiling the new package
        Package newPackage = codeGeneratorResult.updatedPackage().orElse(null);
        Assert.assertNull(newPackage, "Should be null, because there exist no code generators");
    }

    @Test
    public void testCompilerPluginCodegenBasic() {
        Package currentPackage = loadPackage("package_plugin_codegen_user_1");
        // Check the document count in the current package
        Assert.assertEquals(1, currentPackage.getDefaultModule().documentIds().size());

        //  Running the compilation
        currentPackage.getCompilation();

        // Check direct package dependencies
        Assert.assertEquals(currentPackage.packageDependencies().size(), 1,
                "Unexpected number of dependencies");

        // Running the code generation
        CodeGeneratorResult codeGeneratorResult = currentPackage.runCodeGeneratorPlugins();

        // Compiling the new package
        Project project = currentPackage.project();
        Package newPackage = codeGeneratorResult.updatedPackage().orElse(null);
        Assert.assertNotNull(newPackage, "Cannot be null, because there exist code generators");
        Assert.assertSame(newPackage.project(), project);
        Assert.assertSame(newPackage, project.currentPackage());

        // The code generator produce 4 files.
        // 3 files for three functions, one file importing another package and main.bal.
        Assert.assertEquals(newPackage.getDefaultModule().documentIds().size(), 5);
        // The code generator produce 4 test files.
        // 3 files for three functions and one file importing another package.
        Assert.assertEquals(newPackage.getDefaultModule().testDocumentIds().size(), 4);
        PackageCompilation compilation = newPackage.getCompilation();

        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.diagnostics().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 9, "Unexpected compilation diagnostics");

        // Check direct package dependencies
        // Code generator produces a file that has an import.
        // This import causes the dependencies count to be updated to 2.
        Assert.assertEquals(newPackage.packageDependencies().size(), 2,
                "Unexpected number of dependencies");

        // Check resources
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            if (!module.isDefaultModule()) {
                Assert.assertEquals(module.resourceIds().size(), 0);
                continue;
            }
            Assert.assertEquals(module.resourceIds().size(), 1);
            Resource resource = module.resource(module.resourceIds().stream().findFirst().orElseThrow());
            Assert.assertEquals(resource.name(), "openapi-spec.yaml");
            Assert.assertEquals(resource.content(), "".getBytes());
            Assert.assertEquals(resource.module(), module);
        }

        // Check test resources
        for (ModuleId moduleId : project.currentPackage().moduleIds()) {
            Module module = project.currentPackage().module(moduleId);
            if (!module.isDefaultModule()) {
                Assert.assertEquals(module.testResourceIds().size(), 0);
                continue;
            }
            Assert.assertEquals(module.testResourceIds().size(), 1);
            Resource testResource = module.resource(module.testResourceIds().stream().findFirst().orElseThrow());
            Assert.assertEquals(testResource.name(), "sample.json");
            Assert.assertEquals(testResource.content(), "".getBytes());
            Assert.assertEquals(testResource.module(), module);
        }
    }

    @Test(description = "Test basic package code modify using code modifier plugin")
    public void testCompilerPluginCodeModifyBasic() {
        Package currentPackage = loadPackage("package_plugin_code_modify_user_1");
        // Check the document count in the current package
        Assert.assertEquals(currentPackage.getDefaultModule().documentIds().size(), 2);

        //  Running the compilation
        currentPackage.getCompilation();

        // Check direct package dependencies
        Assert.assertEquals(currentPackage.packageDependencies().size(), 1,
                "Unexpected number of dependencies");

        // Running the code generation
        CodeModifierResult codeModifierResult = currentPackage.runCodeModifierPlugins();

        // Compiling the new package
        Project project = currentPackage.project();
        Package newPackage = codeModifierResult.updatedPackage().orElse(null);
        Assert.assertNotNull(newPackage, "Cannot be null, because there exist code modifiers");
        Assert.assertSame(newPackage.project(), project);
        Assert.assertSame(newPackage, project.currentPackage());

        // Modified source files
        Assert.assertEquals(newPackage.getDefaultModule().documentIds().size(), 2);
        for (DocumentId documentId : newPackage.getDefaultModule().documentIds()) {
            Document document = newPackage.getDefaultModule().document(documentId);
            // The code generator adds specific function to the end of every source file.
            String specificFunction = "public function newFunctionByCodeModifier"
                    + document.name().replace(".bal", "").replace("/", "_")
                    + "(string params) returns error? {\n}";
            Assert.assertTrue(document.syntaxTree().toSourceCode().contains(specificFunction));
        }

        // Modified test source files
        Assert.assertEquals(newPackage.getDefaultModule().testDocumentIds().size(), 1);
        for (DocumentId documentId : newPackage.getDefaultModule().testDocumentIds()) {
            Document document = newPackage.getDefaultModule().document(documentId);
            // The code generator adds specific function to the end of every source file.
            String specificFunction = "public function newFunctionByCodeModifier"
                    + document.name().replace(".bal", "").replace("/", "_").replace("-", "_")
                    + "(string params) returns error? {\n}";
            Assert.assertTrue(document.syntaxTree().toSourceCode().contains(specificFunction));
        }

        PackageCompilation compilation = newPackage.getCompilation();
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.diagnostics().forEach(OUT::println);
        Assert.assertFalse(diagnosticResult.hasErrors(), "Unexpected errors in compilation");
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 8, "Unexpected compilation diagnostics");

        // Check direct package dependencies count is 1
        Assert.assertEquals(newPackage.packageDependencies().size(), 1, "Unexpected number of dependencies");
    }

    @Test(description = "Test basic single bal file code modify using code modifier plugin")
    public void testCompilerPluginSingleBalFileCodeModifyBasic() {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve("single_bal_plugin_code_modify_user_1").resolve("main.bal");
        SingleFileProject singleFileProject = TestUtils.loadSingleFileProject(projectDirPath);
        Package currentPackage = singleFileProject.currentPackage();

        // Check the document count in the current package
        Assert.assertEquals(currentPackage.getDefaultModule().documentIds().size(), 1);

        //  Running the compilation
        currentPackage.getCompilation();

        // Check direct package dependencies
        Assert.assertEquals(currentPackage.packageDependencies().size(), 1,
                "Unexpected number of dependencies");

        // Running the code generation
        CodeModifierResult codeModifierResult = currentPackage.runCodeModifierPlugins();

        // Compiling the new package
        Project project = currentPackage.project();
        Package newPackage = codeModifierResult.updatedPackage().orElse(null);
        Assert.assertNotNull(newPackage, "Cannot be null, because there exist code modifiers");
        Assert.assertSame(newPackage.project(), project);
        Assert.assertSame(newPackage, project.currentPackage());

        Assert.assertEquals(newPackage.getDefaultModule().documentIds().size(), 1);
        for (DocumentId documentId : newPackage.getDefaultModule().documentIds()) {
            Document document = newPackage.getDefaultModule().document(documentId);
            // The code generator adds specific function to the end of every source file.
            String specificFunction = "public function newFunctionByCodeModifier"
                    + document.name().replace(".bal", "")
                    + "(string params) returns error? {\n}";
            Assert.assertTrue(document.syntaxTree().toSourceCode().contains(specificFunction));
        }

        PackageCompilation compilation = newPackage.getCompilation();
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        diagnosticResult.diagnostics().forEach(OUT::println);
        Assert.assertFalse(diagnosticResult.hasErrors(), "Unexpected errors in compilation");
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 4, "Unexpected compilation diagnostics");

        // Check direct package dependencies count is 1
        Assert.assertEquals(newPackage.packageDependencies().size(), 1, "Unexpected number of dependencies");
    }

    @Test
    public void testCodeAnalyzerCompilerPluginForTestSources() {
        Package currentPackage = loadPackage("package_plugin_diagnostic_user_1");
        // Check whether there are any diagnostics
        DiagnosticResult diagnosticResult = currentPackage.getCompilation().diagnosticResult();
        diagnosticResult.diagnostics().forEach(OUT::println);
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 5,
                "Unexpected number of compilation diagnostics");

        Iterator<Diagnostic> diagnosticIterator = diagnosticResult.diagnostics().iterator();
        Assert.assertEquals(diagnosticIterator.next().toString(),
                "INFO [main.bal:(3:8,3:16)] main");
        Assert.assertEquals(diagnosticIterator.next().toString(),
                "INFO [main.bal:(6:1,6:9)] foo");
        Assert.assertEquals(diagnosticIterator.next().toString(),
                "INFO [main.bal:(9:1,9:9)] bar");
        Assert.assertEquals(diagnosticIterator.next().toString(),
                "INFO [tests/test.bal:(3:1,3:9)] testFoo");
        Assert.assertEquals(diagnosticIterator.next().toString(),
                "INFO [tests/test.bal:(6:1,6:9)] testYee");
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

    @Test
    public void testImmutableTypeDefsWithRepeatedCompilationWithCodeModifierPlugin() {
        Package currentPackage = loadPackage("immutable_type_definition_with_code_modifier_test/usage");
        currentPackage.getCompilation();
        CodeModifierResult codeModifierResult = currentPackage.runCodeModifierPlugins();
        Package newPackage = codeModifierResult.updatedPackage().orElse(null);
        Assert.assertNotNull(newPackage, "Cannot be null, because there exist code modifiers");

        PackageCompilation packageCompilation = newPackage.getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
        CompileResult compileResult = new CompileResult(newPackage, jBallerinaBackend);

        try {
            BRunUtil.runInit(compileResult);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("error while invoking init method");
        }

        Object mainResult = BRunUtil.invoke(compileResult, "main");
        Assert.assertNull(mainResult);
    }

    private Package loadPackage(String path) {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve(path);
        BuildProject buildProject = TestUtils.loadBuildProject(projectDirPath);
        return buildProject.currentPackage();
    }
}

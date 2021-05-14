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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.BallerinaModuleID;
import io.ballerina.compiler.api.impl.symbols.BallerinaIntTypeSymbol;
import io.ballerina.compiler.api.impl.symbols.TypesFactory;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.CodeActionManager;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.CodeActionPluginContext;
import io.ballerina.projects.plugins.codeaction.CodeActionPluginContextImpl;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnostic;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.diagnostic.properties.BSymbolicProperty;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    @Test
    public void testCodeGenerationCompletedPlugin() {
        String path = RESOURCE_DIRECTORY.resolve("package_plugin_user_6").toString();
        CompileResult result = BCompileUtil.compileAndCacheBala(path);
        Assert.assertEquals(result.getWarnCount(), 1);
        BAssertUtil.validateWarning(result, 0, "End of codegen", 1, 1);
    }

    @Test
    public void testCodeActions() {
        String path = RESOURCE_DIRECTORY.resolve("package_plugin_user_with_codeactions_1").toString();
        CompileResult result = BCompileUtil.compileAndCacheBala(path);

        Path filePath = Paths.get(path, "main.bal");
        Project project = result.project();
        DocumentId documentId = project.documentId(filePath);
        Module module = project.currentPackage().module(documentId.moduleId());
        Document document = module.document(documentId);

        PackageCompilation packageCompilation = project.currentPackage().getCompilation();
        CodeActionManager codeActionManager = packageCompilation.getCodeActionManager();

        // Get code actions
        CodeActionPluginContext codeActionPluginContext = CodeActionPluginContextImpl.from(filePath.toUri().toString(),
                filePath, LinePosition.from(6, 5), document, module.getCompilation().getSemanticModel());

        Location location = new BLangDiagnosticLocation(filePath.toUri().toString(), 6, 6, 3, 14);
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo("BCE2526", "ignored", DiagnosticSeverity.ERROR);

        TypeSymbol typeSymbol = Mockito.mock(TypeSymbol.class);
        Mockito.when(typeSymbol.signature()).thenReturn("int");
        Diagnostic diagnostic = new BLangDiagnostic(location, "variable assignment is required", diagnosticInfo, DiagnosticErrorCode.ASSIGNMENT_REQUIRED, List.of(new BSymbolicProperty(typeSymbol)));
        List<CodeActionInfo> codeActionInfos = codeActionManager.codeActions(codeActionPluginContext, diagnostic);

        Assert.assertFalse(codeActionInfos.isEmpty());
        Optional<CodeActionInfo> info = codeActionInfos.stream()
                .filter(codeActionInfo -> "lstest_package_comp_plugin_with_codeactions_CREATE_VAR".equals(codeActionInfo.getProviderName()))
                .filter(codeActionInfo -> "Introduce Variable".equals(codeActionInfo.getTitle()))
                .findFirst();
        Assert.assertTrue(info.isPresent());

        Gson gson = new Gson();
        List<CodeActionArgument> arguments = info.get().getArguments();
        arguments = arguments.stream()
                .map(codeActionArgument -> CodeActionArgument.from(gson.toJsonTree(codeActionArgument)))
                .collect(Collectors.toList());

        Assert.assertFalse(arguments.isEmpty());

        Assert.assertTrue(arguments.stream().anyMatch(argument -> argument.key().equals("node.range") &&
                argument.valueAs(LineRange.class).equals(location.lineRange())));
        Assert.assertTrue(arguments.stream().anyMatch(argument -> argument.key().equals("var.type") &&
                argument.valueAs(String.class).equals("int")));

        // Execute code action
        List<DocumentEdit> documentEdits = codeActionManager.executeCodeAction(info.get().getProviderName(), codeActionPluginContext, arguments);
        Assert.assertFalse(documentEdits.isEmpty());

        Assert.assertTrue(documentEdits.stream().anyMatch(documentEdit -> {
            if (!filePath.toUri().toString().equals(documentEdit.getFileUri())) {
                return false;
            }

            SyntaxTree modifiedSyntaxTree = documentEdit.getModifiedSyntaxTree();
            return modifiedSyntaxTree.textDocument().line(location.lineRange().startLine().line())
                    .text().contains("int myVar = createInt();");
        }));
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

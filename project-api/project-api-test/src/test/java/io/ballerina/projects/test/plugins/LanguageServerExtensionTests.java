/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.projects.test.plugins;

import com.google.gson.Gson;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.CodeActionManager;
import io.ballerina.projects.CodeActionResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionContextImpl;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContextImpl;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Tests for language server's code action extension.
 */
@Test
public class LanguageServerExtensionTests {

    private static final Path RESOURCE_DIRECTORY = Paths.get(
            "src/test/resources/compiler_plugin_tests").toAbsolutePath();

    @BeforeSuite
    public void init() {
        BCompileUtil.compileAndCacheBala("compiler_plugin_tests/package_comp_plugin_with_codeactions");
    }

    @Test
    public void testOneCompilerPluginWithOneCodeAction() {
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
        Location location = new BLangDiagnosticLocation(filePath.toUri().toString(), 6, 6, 3, 14);
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo("BCE2526", "ignored", DiagnosticSeverity.ERROR);

        TypeSymbol typeSymbol = Mockito.mock(TypeSymbol.class);
        Mockito.when(typeSymbol.signature()).thenReturn("int");
        Diagnostic diagnostic = new BLangDiagnostic(location, "variable assignment is required", diagnosticInfo,
                DiagnosticErrorCode.ASSIGNMENT_REQUIRED, List.of(new BSymbolicProperty(typeSymbol)));

        CodeActionContext codeActionContext = CodeActionContextImpl.from(filePath.toUri().toString(),
                filePath, LinePosition.from(6, 5), document, module.getCompilation().getSemanticModel(), diagnostic);

        CodeActionResult codeActionResult = codeActionManager.codeActions(codeActionContext);

        Assert.assertFalse(codeActionResult.getCodeActions().isEmpty());
        Optional<CodeActionInfo> info = codeActionResult.getCodeActions().stream()
                .filter(codeActionInfo -> "BCE2526/lstest/package_comp_plugin_with_codeactions/CREATE_VAR"
                        .equals(codeActionInfo.getProviderName()))
                .filter(codeActionInfo -> "Introduce Variable".equals(codeActionInfo.getTitle()))
                .findFirst();
        Assert.assertTrue(info.isPresent());

        // Has to convert arguments to json elements because LS expects them in that format when executing command
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
        CodeActionExecutionContext executionContext = CodeActionExecutionContextImpl.from(filePath.toUri().toString(),
                filePath, LinePosition.from(6, 5), document, module.getCompilation().getSemanticModel(), arguments);

        List<DocumentEdit> documentEdits = codeActionManager.executeCodeAction(info.get().getProviderName(),
                executionContext);
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

}

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
package io.ballerina.compiler.linter.codeactions.test;

import com.google.gson.Gson;
import io.ballerina.projects.CodeActionManager;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.plugins.codeaction.CodeActionArgument;
import io.ballerina.projects.plugins.codeaction.CodeActionContextImpl;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContext;
import io.ballerina.projects.plugins.codeaction.CodeActionExecutionContextImpl;
import io.ballerina.projects.plugins.codeaction.CodeActionInfo;
import io.ballerina.projects.plugins.codeaction.DocumentEdit;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper methods for writing code action tests.
 *
 * @since 2.0.0
 */
public class CodeActionUtils {

    public static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    public static final String NEW = ".new";

    /**
     * Get codeactions for the provided cursor position in the provided source file.
     *
     * @param project   Project
     * @param filePath  Source file path
     * @param cursorPos Cursor position
     * @return List of codeactions for the cursor position
     */
    public static List<CodeActionInfo> getCodeActions(Project project, Path filePath, LinePosition cursorPos) {
        Package currentPackage = project.currentPackage();
        PackageCompilation compilation = currentPackage.getCompilation();
        // Codeaction manager is our interface to obtaining codeactions and executing them
        CodeActionManager codeActionManager = compilation.getCodeActionManager();

        DocumentId documentId = project.documentId(filePath);
        Document document = currentPackage.getDefaultModule().document(documentId);

        return compilation.diagnosticResult().diagnostics().stream()
                // Filter diagnostics available for the cursor position
                .filter(diagnostic -> isWithinRange(diagnostic.location().lineRange(), cursorPos) &&
                        filePath.endsWith(diagnostic.location().lineRange().filePath()))
                .flatMap(diagnostic -> {
                    CodeActionContextImpl context = CodeActionContextImpl.from(
                            filePath.toUri().toString(),
                            filePath,
                            cursorPos,
                            document,
                            compilation.getSemanticModel(documentId.moduleId()),
                            diagnostic);
                    // Get codeactions for the diagnostic
                    return codeActionManager.codeActions(context).getCodeActions().stream();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get the document edits returned when the provided codeaction is executed.
     *
     * @param project    Project
     * @param filePath   Source file path
     * @param codeAction Codeaction to be executed
     * @return List of edits returned by the codeaction execution
     */
    public static List<DocumentEdit> executeCodeAction(Project project, Path filePath, CodeActionInfo codeAction) {
        Package currentPackage = project.currentPackage();
        PackageCompilation compilation = currentPackage.getCompilation();

        DocumentId documentId = project.documentId(filePath);
        Document document = currentPackage.getDefaultModule().document(documentId);

        // Need to convert the arguments to JsonElement because internally we convert
        // JsonElements to objects via deserialization.
        Gson gson = new Gson();
        List<CodeActionArgument> codeActionArguments = codeAction.getArguments().stream()
                .map(arg -> CodeActionArgument.from(gson.toJsonTree(arg)))
                .collect(Collectors.toList());

        CodeActionExecutionContext executionContext = CodeActionExecutionContextImpl.from(
                filePath.toUri().toString(),
                filePath,
                null,
                document,
                compilation.getSemanticModel(document.documentId().moduleId()),
                codeActionArguments);

        // Execute and get documents edits for the codeaction
        return compilation.getCodeActionManager()
                .executeCodeAction(codeAction.getProviderName(), executionContext);
    }

    /**
     * Get source path from the test resources.
     *
     * @param sourceFilePath Source file path
     * @return True if position is within the provided line range
     */
    public static Path getProjectPath(String sourceFilePath) {

        Path sourcePath = Paths.get(sourceFilePath);
        String sourceFileName = sourcePath.getFileName().toString();
        Path sourceRoot = RES_DIR.resolve(sourcePath.getParent());
        return Paths.get(sourceRoot.toString(), sourceFileName);
    }

    /**
     * Read .new file from the file system.
     *
     * @param filePath Source file
     * @return Content of the file
     * @throws IOException If file not found
     */
    public static String getExpectedSourceCode(String filePath) throws IOException {
        return Files.readString(CodeActionUtils.getProjectPath(filePath + NEW));
    }


    /**
     * Check if provided position is within the provided line range.
     *
     * @param lineRange Line range
     * @param pos       Position
     * @return True if position is within the provided line range
     */
    private static boolean isWithinRange(LineRange lineRange, LinePosition pos) {
        int sLine = lineRange.startLine().line();
        int sCol = lineRange.startLine().offset();
        int eLine = lineRange.endLine().line();
        int eCol = lineRange.endLine().offset();

        return ((sLine == eLine && pos.line() == sLine) &&
                (pos.offset() >= sCol && pos.offset() <= eCol)
        ) || ((sLine != eLine) && (pos.line() > sLine && pos.line() < eLine ||
                pos.line() == eLine && pos.offset() <= eCol ||
                pos.line() == sLine && pos.offset() >= sCol
        ));
    }

}

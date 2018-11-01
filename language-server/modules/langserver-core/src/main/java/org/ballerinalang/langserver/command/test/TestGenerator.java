/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.command.test;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.command.test.renderer.BLangPkgBasedRendererOutput;
import org.ballerinalang.langserver.command.test.renderer.RendererOutput;
import org.ballerinalang.langserver.command.test.renderer.TemplateBasedRendererOutput;
import org.ballerinalang.langserver.command.test.template.RootTemplate;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * This class is responsible for generating tests for a given source file.
 *
 * @since 0.981.2
 */
public class TestGenerator {

    private TestGenerator() {
    }

    /**
     * Creates a test file for a given BLangPackage in source file path.
     *
     * @param documentManager document manager
     * @param bLangNodePair   A pair of {@link BLangNode} and fallback node
     * @param builtSourceFile built {@link BLangPackage} source file
     * @param pkgRelativePath package relative path
     * @param testFile        test file
     * @return generated test file path
     * @throws TestGeneratorException when test case generation fails
     */
    public static List<TextEdit> generate(WorkspaceDocumentManager documentManager,
                                          Pair<BLangNode, Object> bLangNodePair, BLangPackage builtSourceFile,
                                          String pkgRelativePath, File testFile) throws TestGeneratorException {
        RootTemplate rootTemplate = getRootTemplateForBLangNode(pkgRelativePath, bLangNodePair, builtSourceFile);
        RendererOutput rendererOutput = getRendererOutput(documentManager, testFile);
        rootTemplate.render(rendererOutput);
        return rendererOutput.getRenderedTextEdits();
    }

    private static RendererOutput getRendererOutput(WorkspaceDocumentManager documentManager, File testFile)
            throws TestGeneratorException {
        // If exists, read the test file content
        String testContent = "";
        if (testFile.exists()) {
            try {
                // Reading through document manager since amendments are handled as text-edits
                testContent = documentManager.getFileContent(testFile.toPath());
            } catch (WorkspaceDocumentException e) {
                throw new TestGeneratorException("Error occurred while compiling file path:" + testFile.toString(), e);
            }
        }

        // Create file template
        RendererOutput fileTemplate;
        if (testContent.isEmpty()) {
            fileTemplate = new TemplateBasedRendererOutput("rootTest.bal");
        } else {
            BallerinaFile ballerinaFile;
            try {
                ballerinaFile = LSCompiler.compileContent(testContent, CompilerPhase.COMPILER_PLUGIN);
            } catch (LSCompilerException e) {
                throw new TestGeneratorException("Could not compile the test content", e);
            }
            Optional<BLangPackage> optBLangPackage = ballerinaFile.getBLangPackage();
            if (optBLangPackage.isPresent()) {
                fileTemplate = new BLangPkgBasedRendererOutput(optBLangPackage.get());
            } else {
                String msg = "Appending failed! unknown error occurred while appending to:" + testFile.toString();
                throw new TestGeneratorException(msg);
            }
        }
        return fileTemplate;
    }

    private static RootTemplate getRootTemplateForBLangNode(String fileName, Pair<BLangNode, Object> result,
                                                            BLangPackage builtTestFile) {
        BLangNode bLangNode = result.getLeft();
        Object fallBackNode = result.getRight();
        boolean fallback = false;

        if (bLangNode instanceof BLangFunction) {
            // A function
            return RootTemplate.fromFunction((BLangFunction) bLangNode, builtTestFile);

        } else if (bLangNode instanceof BLangService || (fallback = fallBackNode instanceof BLangService)) {
            // A Service
            BLangService service = (!fallback) ? ((BLangService) bLangNode) : (BLangService) fallBackNode;
            switch (service.serviceTypeStruct.typeName.value) {
                case "Service": {
                    return RootTemplate.fromHttpService(service, builtTestFile);
                }
                case "WebSocketService": {
                    return RootTemplate.fromHttpWSService(service, builtTestFile);
                }
                case "WebSocketClientService":
                    return RootTemplate.fromHttpClientWSService(service, builtTestFile);
                default:
            }
        }
        // Whole file
        return new RootTemplate(fileName, builtTestFile);
    }
}

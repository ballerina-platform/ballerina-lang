/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.sourcegen;

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.formatting.FormattingSourceGen;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 * Source generation test suit.
 */
public class SourceGenTest {
    private Path examples = Paths.get("../../../examples").toAbsolutePath();
    private Path unitTestResources = Paths.get("../../../tests/ballerina-unit-test/src/test/resources/test-src")
            .toAbsolutePath();
    private List<File> ballerinaTestResources;
    private Endpoint serviceEndpoint;

    @BeforeClass
    public void loadExampleFiles() throws IOException {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
        this.ballerinaTestResources = getBallerinaUnitTestFiles();
    }

    @Test(description = "Source gen test suit for formatting source gen", dataProvider = "unitTestFiles")
    public void formattingSourceGenTests(File file) {
        LSServiceOperationContext formatContext = new LSServiceOperationContext();
        try {
            Path filePath = Paths.get(file.getPath());
            formatContext.put(DocumentServiceKeys.FILE_URI_KEY, filePath.toUri().toString());

            WorkspaceDocumentManager documentManager = WorkspaceDocumentManagerImpl.getInstance();
            byte[] encoded1 = Files.readAllBytes(filePath);
            String expected = new String(encoded1, StandardCharsets.UTF_8);
            TestUtil.openDocument(serviceEndpoint, filePath);
            LSCompiler lsCompiler = new LSCompiler(documentManager);
            JsonObject ast = TextDocumentFormatUtil.getAST(filePath.toUri().toString(), lsCompiler, documentManager,
                    formatContext);
            FormattingSourceGen sourceGen = new FormattingSourceGen();
            sourceGen.build(ast.getAsJsonObject("model"), null, "CompilationUnit");
            String actual = sourceGen.getSourceOf(ast.getAsJsonObject("model"));
            TestUtil.closeDocument(serviceEndpoint, filePath);
            Assert.assertEquals(actual, expected, "Generated source didn't match the expected for file: " +
                    file.getName());
        } catch (Exception e) {
            // This error being catch to print failing source-gen file.
            Assert.fail("Exception occurred while processing file: " + file.getName() + "\nException:" +
                    e.toString(), e);
        }
    }

    @AfterClass
    public void shutdownLanguageServer() throws IOException {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @DataProvider
    public Object[] unitTestFiles() {
        return this.ballerinaTestResources.toArray();
    }

    private List<File> getBallerinaUnitTestFiles() throws IOException {
        List<File> files = new ArrayList<>();
        FileVisitor fileVisitor = new FileVisitor(files);
        Files.walkFileTree(unitTestResources, fileVisitor);
        Files.walkFileTree(examples, fileVisitor);
        return files;
    }

    /**
     * File visitor for file walker.
     */
    static class FileVisitor extends SimpleFileVisitor<Path> {
        private List<File> files;
        private String[] ignoredFiles = {"grpc_bidirectional_streaming_client.bal", "compensate-stmt.bal",
                "function-with-two-rest-params.bal", "redundant-compression-config.bal", "symbolic-string-test.bal",
                "identifier-literal-success.bal", "entity-body-with-charset-test.bal",
                "taintchecking/annotations/lambda.bal", "high_loc.bal", "test_objects.bal",
                "lang/annotations/variable-as-attribute-value.bal", "lang/annotations/constant-as-attribute-value.bal",
                "lang/annotations/multityped-attribute-array.bal", "lang/annotations/default-values.bal",
                "lang/annotations/lang.annotations.pkg.first/annotation.bal", "structs/struct-equivalency.bal",
                "lang/annotations/wrongly-attached-annot.bal", "structs/eq/eq1.bal", "structs/req/eq2.bal",
                "lang/annotations/lang.annotations.foo/annotations.bal", "structs/req2/eq2.bal",
                "lang/annotations/attribute-value-type-mismatch.bal", "structs/org.foo.attached_funcs/funcs.bal",
                "lang/annotations/lang.annotations.doc1/doc-annotation.bal", "structs/eq2/eq2.bal",
                "workers/fork-join-some-map.bal", "services/session/http-session-test.bal",
                "streamingv2-aggregation-groupby-test.bal", "streamingv2-aggregation-test.bal",
                "streamingv2-external-window-test.bal", "streamingv2-aggregation-with-groupby-test.bal"};

        FileVisitor(List<File> ballerinaFiles) {
            this.files = ballerinaFiles;
        }

        @Override
        public FileVisitResult visitFile(Path filePath,
                                         BasicFileAttributes attr) {
            if (attr.isRegularFile()
                    && !(filePath.getFileName().toString().contains("negative")
                    || filePath.getFileName().toString().contains("invalid"))) {
                File file = new File(filePath.toString());
                if (file.getName().endsWith(".bal") && !isIgnoredFile(file.getPath())) {
                    this.files.add(file);
                }
            }

            return CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (attrs.isSymbolicLink()
                    || dir.getFileName().toString().contains("negative")
                    || dir.getFileName().toString().contains("invalid")) {
                return SKIP_SUBTREE;
            }
            return CONTINUE;
        }

        private boolean isIgnoredFile(String fileName) {
            boolean isIgnored = false;
            for (String s : ignoredFiles) {
                if (fileName.endsWith(s)) {
                    isIgnored = true;
                    break;
                }
            }
            return isIgnored;
        }
    }
}

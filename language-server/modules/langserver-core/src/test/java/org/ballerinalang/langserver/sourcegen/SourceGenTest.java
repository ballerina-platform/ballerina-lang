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
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.LSTestOperationContext;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Path unitTestResources = Paths.get("../../../tests/jballerina-unit-test/src/test/resources/test-src")
            .toAbsolutePath();
    private List<File> ballerinaTestResources;
    private Endpoint serviceEndpoint;
    private WorkspaceDocumentManager documentManager;
    private static final Logger log = LoggerFactory.getLogger(SourceGenTest.class);

    @BeforeClass
    public void loadExampleFiles() throws IOException {
        Pair<Endpoint, WorkspaceDocumentManager> pair = TestUtil.initializeLanguageSeverAndGetDocManager();
        this.serviceEndpoint = pair.getLeft();
        this.documentManager = pair.getRight();
        this.ballerinaTestResources = getBallerinaUnitTestFiles();
    }

    @Test(description = "Source gen test suit for formatting source gen", dataProvider = "unitTestFiles")
    public void formattingSourceGenTests(File file) {
        LSContext formatContext = new LSTestOperationContext
                .LSTestOperationContextBuilder(LSContextOperation.TXT_FORMATTING)
                .build();
        try {
            Path filePath = Paths.get(file.getPath());
            formatContext.put(DocumentServiceKeys.FILE_URI_KEY, filePath.toUri().toString());

            byte[] encoded1 = Files.readAllBytes(filePath);
            String expected = new String(encoded1, StandardCharsets.UTF_8);
            TestUtil.openDocument(serviceEndpoint, filePath);
            JsonObject ast = TextDocumentFormatUtil.getAST(filePath, documentManager, formatContext);
            FormattingSourceGen.build(ast.getAsJsonObject("model"), "CompilationUnit");
            String actual = FormattingSourceGen.getSourceOf(ast.getAsJsonObject("model"));
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
        log.info("Test Source Gen");
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
                "taintchecking" + File.separator + "annotations" + File.separator + "lambda.bal", "high_loc.bal",
                "test_objects.bal", "lang" + File.separator + "annotations" + File.separator +
                "variable-as-attribute-value.bal", "lang" + File.separator + "annotations" + File.separator +
                "constant-as-attribute-value.bal", "lang" + File.separator + "annotations" + File.separator +
                "multityped-attribute-array.bal", "lang" + File.separator + "annotations" + File.separator +
                "default-values.bal", "lang" + File.separator + "annotations" + File.separator +
                "lang.annotations.pkg.first" + File.separator + "annotation.bal", "structs" + File.separator +
                "struct-equivalency.bal", "lang" + File.separator + "annotations" + File.separator +
                "wrongly-attached-annot.bal", "structs" + File.separator + "eq" + File.separator + "eq1.bal",
                "structs" + File.separator + "req" + File.separator + "eq2.bal", "lang" + File.separator +
                "annotations" + File.separator + "lang.annotations.foo" + File.separator + "annotations.bal",
                "structs" + File.separator + "req2" + File.separator + "eq2.bal", "lang" + File.separator +
                "annotations" + File.separator + "attribute-value-type-mismatch.bal", "structs" + File.separator +
                "org.foo.attached_funcs" + File.separator + "funcs.bal", "lang" + File.separator + "annotations" +
                File.separator + "lang.annotations.doc1" + File.separator + "doc-annotation.bal", "structs" +
                File.separator + "eq2" + File.separator + "eq2.bal", "workers" + File.separator +
                "fork-join-some-map.bal", "services" + File.separator + "session" + File.separator +
                "http-session-test.bal", "streamingv2-aggregation-groupby-test.bal",
                "streamingv2-aggregation-test.bal", "streamingv2-external-window-test.bal",
                "streamingv2-aggregation-with-groupby-test.bal", "not-enough-args-to-return-3.bal",
                "too-many-args-to-return-1.bal", "file_ops.bal", "valid-service.bal", "match_stmt_basic.bal",
                "checkpoint.bal", "http_load_balancer_test.bal", "content_based_routing_test.bal", "http_cors_test.bal",
                "header_based_routing_test.bal", "http_compression_test.bal", "http_access_logs_test.bal",
                "immutable_values_test.bal", "trap_error_test.bal", "http_redirects_test.bal",
                "xml-native-functions.bal"};

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

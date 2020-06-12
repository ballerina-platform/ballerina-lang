/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.document;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.document.ASTModification;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaASTResponse;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Test visible endpoint detection.
 */
public class SyntaxTreeModifyTest {

    private static final String OS = System.getProperty("os.name").toLowerCase();
    private Endpoint serviceEndpoint;

    private Path mainFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("main.bal");

    private Path mainEmptyFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("mainEmpty.bal");

    private Path mainNatsFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("mainNats.bal");

    private Path emptyFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("empty.bal");

    private Path mainHttpCallFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("mainHttpCall.bal");

    private Path mainHttpCallWithPrintFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("mainHttpCallWithPrint.bal");

    private Path serviceNatsFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("serviceNats.bal");

    public static void skipOnWindows() {
        if (OS.contains("win")) {
            throw new SkipException("Skipping the test case on Windows");
        }
    }

    @BeforeClass
    public void startLangServer() throws IOException {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    private Path createTempFile(Path filePath) throws IOException {
        Path tempFilePath = FileUtils.BUILD_DIR.resolve("tmp")
                .resolve(UUID.randomUUID() + ".bal");
        Files.copy(filePath, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        return tempFilePath;
    }

    @Test(description = "Remove content.")
    public void testDelete() throws IOException {
        skipOnWindows();
        Path tempFile = createTempFile(mainFile);
        TestUtil.openDocument(serviceEndpoint, tempFile);
        ASTModification modification = new ASTModification(4, 5, 4, 33, "delete", null);
        BallerinaSyntaxTreeResponse astModifyResponse = LSExtensionTestUtil
                .modifyAndGetBallerinaSyntaxTree(tempFile.toString(),
                        new ASTModification[]{modification}, this.serviceEndpoint);
        Assert.assertTrue(astModifyResponse.isParseSuccess());
        BallerinaSyntaxTreeResponse astResponse = LSExtensionTestUtil.getBallerinaSyntaxTree(
                mainEmptyFile.toString(), this.serviceEndpoint);
        Assert.assertEquals(astModifyResponse.getSyntaxTree(), astResponse.getSyntaxTree());
        TestUtil.closeDocument(this.serviceEndpoint, tempFile);
    }

    @Test(description = "Insert content.")
    public void testInsert() throws IOException {
        skipOnWindows();
        Path tempFile = createTempFile(mainFile);
        TestUtil.openDocument(serviceEndpoint, tempFile);

        Gson gson = new Gson();
        ASTModification modification1 = new ASTModification(1, 1, 1, 1, "IMPORT",
                gson.fromJson("{\"TYPE\":\"ballerina/http\"}", JsonObject.class));
        ASTModification modification2 = new ASTModification(4, 1, 4, 1, "DECLARATION",
                gson.fromJson("{\"TYPE\":\"http:Client\", \"VARIABLE\":\"clientEndpoint\"," +
                        "\"PARAMS\": [\"\\\"http://postman-echo.com\\\"\"]}", JsonObject.class));
        ASTModification modification3 = new ASTModification(4, 1, 4, 1,
                "REMOTE_SERVICE_CALL_CHECK",
                gson.fromJson("{\"TYPE\":\"http:Response\", \"VARIABLE\":\"response\"," +
                        "\"CALLER\":\"clientEndpoint\", \"FUNCTION\":\"get\"," +
                        "\"PARAMS\": [\"\\\"/get?test=123\\\"\"]}", JsonObject.class));
        BallerinaSyntaxTreeResponse astModifyResponse = LSExtensionTestUtil
                .modifyAndGetBallerinaSyntaxTree(tempFile.toString(),
                        new ASTModification[]{modification1, modification2, modification3}, this.serviceEndpoint);
        BallerinaSyntaxTreeResponse astResponse = LSExtensionTestUtil.getBallerinaSyntaxTree(
                mainHttpCallWithPrintFile.toString(), this.serviceEndpoint);
        Assert.assertEquals(astModifyResponse.getSyntaxTree(), astResponse.getSyntaxTree());
        TestUtil.closeDocument(this.serviceEndpoint, tempFile);
    }

    @Test(description = "Update content.")
    public void testUpdate() throws IOException {
        skipOnWindows();
        Path tempFile = createTempFile(mainFile);
        TestUtil.openDocument(serviceEndpoint, tempFile);

        Gson gson = new Gson();
        ASTModification modification1 = new ASTModification(1, 1, 3, 1, "IMPORT",
                gson.fromJson("{\"TYPE\":\"ballerina/nats\"}", JsonObject.class));
        ASTModification modification2 = new ASTModification(4, 1, 5, 1, "DECLARATION",
                gson.fromJson("{\"TYPE\":\"nats:Connection\", \"VARIABLE\":\"connection\"," +
                        "\"PARAMS\": []}", JsonObject.class));
        ASTModification modification3 = new ASTModification(5, 1, 5, 1, "DECLARATION",
                gson.fromJson("{\"TYPE\":\"nats:Producer\", \"VARIABLE\":\"producer\"," +
                        "\"PARAMS\": [connection]}", JsonObject.class));
        ASTModification modification4 = new ASTModification(5, 1, 5, 1,
                "REMOTE_SERVICE_CALL",
                gson.fromJson("{\"TYPE\":\"nats:Error?\", \"VARIABLE\":\"result\"," +
                                "\"CALLER\":\"producer\", \"FUNCTION\":\"publish\"," +
                                "\"PARAMS\": [\"\\\"Foo\\\"\", \"\\\"Test Message\\\"\"]}",
                        JsonObject.class));
        BallerinaSyntaxTreeResponse astModifyResponse = LSExtensionTestUtil
                .modifyAndGetBallerinaSyntaxTree(tempFile.toString(),
                        new ASTModification[]{modification1, modification2, modification3, modification4},
                        this.serviceEndpoint);
        Assert.assertTrue(astModifyResponse.isParseSuccess());

        BallerinaSyntaxTreeResponse astResponse = LSExtensionTestUtil.getBallerinaSyntaxTree(
                mainNatsFile.toString(), this.serviceEndpoint);
        Assert.assertEquals(astModifyResponse.getSyntaxTree(), astResponse.getSyntaxTree());
        TestUtil.closeDocument(this.serviceEndpoint, tempFile);
    }


    @Test(description = "Main content.")
    public void testMain() throws IOException {
        skipOnWindows();
        Path tempFile = createTempFile(emptyFile);
        TestUtil.openDocument(serviceEndpoint, tempFile);

        Gson gson = new Gson();
        ASTModification modification1 = new ASTModification(1, 1, 1, 1, "IMPORT",
                gson.fromJson("{\"TYPE\":\"ballerina/nats\"}", JsonObject.class));
        ASTModification modification2 = new ASTModification(1, 1, 1, 1, "MAIN_START",
                gson.fromJson("{}", JsonObject.class));
        ASTModification modification3 = new ASTModification(1, 1, 1, 1, "DECLARATION",
                gson.fromJson("{\"TYPE\":\"nats:Connection\", \"VARIABLE\":\"connection\"," +
                        "\"PARAMS\": []}", JsonObject.class));
        ASTModification modification4 = new ASTModification(1, 1, 1, 1, "DECLARATION",
                gson.fromJson("{\"TYPE\":\"nats:Producer\", \"VARIABLE\":\"producer\"," +
                        "\"PARAMS\": [connection]}", JsonObject.class));
        ASTModification modification5 = new ASTModification(1, 1, 1, 1,
                "REMOTE_SERVICE_CALL",
                gson.fromJson("{\"TYPE\":\"nats:Error?\", \"VARIABLE\":\"result\"," +
                                "\"CALLER\":\"producer\", \"FUNCTION\":\"publish\"," +
                                "\"PARAMS\": [\"\\\"Foo\\\"\", \"\\\"Test Message\\\"\"]}",
                        JsonObject.class));
        ASTModification modification6 = new ASTModification(1, 1, 1, 1, "MAIN_END",
                gson.fromJson("{}", JsonObject.class));

        BallerinaSyntaxTreeResponse astModifyResponse = LSExtensionTestUtil
                .modifyAndGetBallerinaSyntaxTree(tempFile.toString(),
                        new ASTModification[]{modification1, modification2, modification3, modification4,
                                modification5, modification6}, this.serviceEndpoint);
        Assert.assertTrue(astModifyResponse.isParseSuccess());

        BallerinaSyntaxTreeResponse astResponse = LSExtensionTestUtil.getBallerinaSyntaxTree(
                mainNatsFile.toString(), this.serviceEndpoint);
        Assert.assertEquals(astModifyResponse.getSyntaxTree(), astResponse.getSyntaxTree());
        TestUtil.closeDocument(this.serviceEndpoint, tempFile);
    }

    @Test(description = "Main content insert.")
    public void testMainInsert() throws IOException {
        skipOnWindows();
        Path tempFile = createTempFile(emptyFile);
        TestUtil.openDocument(serviceEndpoint, tempFile);

        Gson gson = new Gson();
        ASTModification modification1 = new ASTModification(1, 1, 1, 1, "MAIN_START",
                gson.fromJson("{}", JsonObject.class));
        ASTModification modification2 = new ASTModification(1, 1, 1, 1, "MAIN_END",
                gson.fromJson("{}", JsonObject.class));

        BallerinaASTResponse astModifyResponse = LSExtensionTestUtil
                .modifyAndGetBallerinaAST(tempFile.toString(),
                        new ASTModification[]{modification1, modification2}, this.serviceEndpoint);
        Assert.assertTrue(astModifyResponse.isParseSuccess());

        ASTModification modification3 = new ASTModification(1, 1, 1, 1, "IMPORT",
                gson.fromJson("{\"TYPE\":\"ballerina/http\"}", JsonObject.class));
        ASTModification modification4 = new ASTModification(2, 1, 2, 1, "DECLARATION",
                gson.fromJson("{\"TYPE\":\"http:Client\", \"VARIABLE\":\"clientEndpoint\"," +
                        "\"PARAMS\": [\"\\\"http://postman-echo.com\\\"\"]}", JsonObject.class));
        ASTModification modification5 = new ASTModification(2, 1, 2, 1,
                "REMOTE_SERVICE_CALL_CHECK",
                gson.fromJson("{\"TYPE\":\"http:Response\", \"VARIABLE\":\"response\"," +
                        "\"CALLER\":\"clientEndpoint\", \"FUNCTION\":\"get\"," +
                        "\"PARAMS\": [\"\\\"/get?test=123\\\"\"]}", JsonObject.class));

        BallerinaSyntaxTreeResponse astModifyResponse2 = LSExtensionTestUtil
                .modifyAndGetBallerinaSyntaxTree(tempFile.toString(),
                        new ASTModification[]{modification3, modification4, modification5}, this.serviceEndpoint);
        Assert.assertTrue(astModifyResponse2.isParseSuccess());

        BallerinaSyntaxTreeResponse astResponse = LSExtensionTestUtil.getBallerinaSyntaxTree(
                mainHttpCallFile.toString(), this.serviceEndpoint);
        Assert.assertEquals(astModifyResponse2.getSyntaxTree(), astResponse.getSyntaxTree());
        TestUtil.closeDocument(this.serviceEndpoint, tempFile);
    }

    @Test(description = "Service.")
    public void testService() throws IOException {
        skipOnWindows();
        Path tempFile = createTempFile(emptyFile);
        TestUtil.openDocument(serviceEndpoint, tempFile);

        Gson gson = new Gson();
        ASTModification modification0 = new ASTModification(1, 1, 1, 1, "IMPORT",
                gson.fromJson("{\"TYPE\":\"ballerina/http\"}", JsonObject.class));
        ASTModification modification1 = new ASTModification(1, 1, 1, 1, "IMPORT",
                gson.fromJson("{\"TYPE\":\"ballerina/nats\"}", JsonObject.class));
        ASTModification modification2 = new ASTModification(1, 1, 1, 1, "SERVICE_START",
                gson.fromJson("{\"SERVICE\":\"hello\", \"RESOURCE\":\"sayHello\", \"PORT\":\"9090\"}",
                        JsonObject.class));
        ASTModification modification3 = new ASTModification(1, 1, 1, 1, "DECLARATION",
                gson.fromJson("{\"TYPE\":\"nats:Connection\", \"VARIABLE\":\"connection\"," +
                        "\"PARAMS\": []}", JsonObject.class));
        ASTModification modification4 = new ASTModification(1, 1, 1, 1, "DECLARATION",
                gson.fromJson("{\"TYPE\":\"nats:Producer\", \"VARIABLE\":\"producer\"," +
                        "\"PARAMS\": [\"connection\"]}", JsonObject.class));
        ASTModification modification5 = new ASTModification(1, 1, 1, 1,
                "REMOTE_SERVICE_CALL",
                gson.fromJson("{\"TYPE\":\"nats:Error?\", \"VARIABLE\":\"result\"," +
                        "\"CALLER\":\"producer\", \"FUNCTION\":\"publish\"," +
                        "\"PARAMS\": [\"\\\"Foo\\\"\", \"\\\"Test Message\\\"\"]}", JsonObject.class));
        ASTModification modification6 = new ASTModification(1, 1, 1, 1, "SERVICE_END",
                gson.fromJson("{}", JsonObject.class));

        BallerinaSyntaxTreeResponse astModifyResponse = LSExtensionTestUtil
                .modifyAndGetBallerinaSyntaxTree(tempFile.toString(),
                        new ASTModification[]{modification0, modification1, modification2, modification3,
                                modification4, modification5, modification6}, this.serviceEndpoint);
        Assert.assertTrue(astModifyResponse.isParseSuccess());

        BallerinaSyntaxTreeResponse astResponse = LSExtensionTestUtil.getBallerinaSyntaxTree(
                serviceNatsFile.toString(), this.serviceEndpoint);
        Assert.assertEquals(astModifyResponse.getSyntaxTree(), astResponse.getSyntaxTree());
        TestUtil.closeDocument(this.serviceEndpoint, tempFile);
    }

    @AfterClass
    public void stopLangServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}

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
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaSyntaxTreeResponse;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test visible endpoint detection.
 */
public class ASTModifyTest {

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

    private Path mainAccuweatherFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("mainAccuweather.bal");

    private Path mainTwilioFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("ast")
            .resolve("modify")
            .resolve("mainTwilio.bal");

    @BeforeClass
    public void startLangServer() throws IOException {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Remove content.")
    public void testDelete() throws IOException {
        TestUtil.openDocument(serviceEndpoint, mainFile);
        ASTModification modification = new ASTModification(47, 80, "delete", null);
        BallerinaSyntaxTreeResponse astModifyResponse = LSExtensionTestUtil
                .modifyAndGetBallerinaSyntaxTree(mainFile.toString(),
                        new ASTModification[]{modification}, this.serviceEndpoint);
        Assert.assertTrue(astModifyResponse.isParseSuccess());
        BallerinaSyntaxTreeResponse astResponse = LSExtensionTestUtil.getBallerinaSyntaxTree(
                mainEmptyFile.toString(), this.serviceEndpoint);
        Assert.assertEquals(astModifyResponse.getSyntaxTree(), astResponse.getSyntaxTree());
        TestUtil.closeDocument(this.serviceEndpoint, mainFile);
    }

    @Test(description = "Insert content.")
    public void testInsert() throws IOException {
        TestUtil.openDocument(serviceEndpoint, mainFile);
        Gson gson = new Gson();
        ASTModification modification1 = new ASTModification(0, 0, "IMPORT",
                gson.fromJson("{\"TYPE\":\"ballerina/accuweather\"}", JsonObject.class));
        ASTModification modification2 = new ASTModification(47, 47, "DECLARATION",
                gson.fromJson("{\"TYPE\":\"accuweather:Client\", \"VARIABLE\":\"accuweatherClient\"," +
                        "\"PARAMS\": [\"\\\"8dbh68Zg2J6WxAK37Cy2jVJTSMdnyAmV\\\"\"]}", JsonObject.class));
        ASTModification modification3 = new ASTModification(47, 47,
                "REMOTE_SERVICE_CALL_CHECK",
                gson.fromJson("{\"TYPE\":\"accuweather:WeatherResponse\", \"VARIABLE\":\"accuweatherResult\"," +
                        "\"CALLER\":\"accuweatherClient\", \"FUNCTION\":\"getDailyWeather\"," +
                        "\"PARAMS\": [\"\\\"80000\\\"\"]}", JsonObject.class));
        BallerinaSyntaxTreeResponse astModifyResponse = LSExtensionTestUtil
                .modifyAndGetBallerinaSyntaxTree(mainFile.toString(),
                        new ASTModification[]{modification1, modification2, modification3}, this.serviceEndpoint);
        Assert.assertTrue(astModifyResponse.isParseSuccess());

        BallerinaSyntaxTreeResponse astResponse = LSExtensionTestUtil.getBallerinaSyntaxTree(
                mainAccuweatherFile.toString(), this.serviceEndpoint);
        Assert.assertEquals(astModifyResponse.getSyntaxTree(), astResponse.getSyntaxTree());
        TestUtil.closeDocument(this.serviceEndpoint, mainFile);
    }

    @Test(description = "Update content.")
    public void testUpdate() throws IOException {
        TestUtil.openDocument(serviceEndpoint, mainFile);
        Gson gson = new Gson();
        ASTModification modification1 = new ASTModification(0, 21, "IMPORT",
                gson.fromJson("{\"TYPE\":\"ballerina/twilio\"}", JsonObject.class));
        ASTModification modification2 = new ASTModification(47, 80, "DECLARATION",
                gson.fromJson("{\"TYPE\":\"twilio:Client\", \"VARIABLE\":\"twilioClient\"," +
                        "\"PARAMS\": [" +
                        "   \"\\\"ACb2e9f049adcb98c7c31b913f8be70733\\\"\", " +
                        "   \"\\\"34b2e025b2db33da04cc53ead8ce09bf\\\"\", " +
                        "   \"\\\"\\\"\"]}", JsonObject.class));
        ASTModification modification3 = new ASTModification(80, 80,
                "REMOTE_SERVICE_CALL_CHECK",
                gson.fromJson("{\"TYPE\":\"twilio:WhatsAppResponse\", \"VARIABLE\":\"twilioResult\"," +
                                "\"CALLER\":\"twilioClient\", \"FUNCTION\":\"sendWhatsAppMessage\"," +
                                "\"PARAMS\": [\"\\\"+14155238886\\\"\", \"\\\"+94773898282\\\"\", " +
                                "\"dataMapperResult\"]}",
                        JsonObject.class));
        BallerinaSyntaxTreeResponse astModifyResponse = LSExtensionTestUtil
                .modifyAndGetBallerinaSyntaxTree(mainFile.toString(),
                        new ASTModification[]{modification1, modification2, modification3}, this.serviceEndpoint);
        Assert.assertTrue(astModifyResponse.isParseSuccess());

        BallerinaSyntaxTreeResponse astResponse = LSExtensionTestUtil.getBallerinaSyntaxTree(
                mainTwilioFile.toString(), this.serviceEndpoint);
        Assert.assertEquals(astModifyResponse.getSyntaxTree(), astResponse.getSyntaxTree());
        TestUtil.closeDocument(this.serviceEndpoint, mainFile);
    }

    @AfterClass
    public void stopLangServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}

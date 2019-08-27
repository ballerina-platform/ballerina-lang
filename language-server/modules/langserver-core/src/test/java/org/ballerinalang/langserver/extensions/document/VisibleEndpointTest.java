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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.extensions.LSExtensionTestUtil;
import org.ballerinalang.langserver.extensions.ballerina.document.BallerinaASTResponse;
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
public class VisibleEndpointTest {

    private Endpoint serviceEndpoint;

    private Path nestedVisibleEPsFile = FileUtils.RES_DIR.resolve("extensions")
                                        .resolve("document")
                                        .resolve("sources")
                                        .resolve("nestedVisibleEndpoints.bal");

    private Path transactionVisibleEPsFile = FileUtils.RES_DIR.resolve("extensions")
            .resolve("document")
            .resolve("sources")
            .resolve("visibleEndpointsInTransactions.bal");

    @BeforeClass
    public void startLangServer() throws IOException {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
        TestUtil.openDocument(serviceEndpoint, nestedVisibleEPsFile);
    }


    @Test(description = "Test nested visible endpoint detection.")
    public void testNestedVisibleEndpoints() {
        BallerinaASTResponse astResponse = LSExtensionTestUtil
                .getBallerinaDocumentAST(nestedVisibleEPsFile.toString(), this.serviceEndpoint);
        Assert.assertTrue(astResponse.isParseSuccess());
        assertVisibleEndpoints(astResponse.getAst());
    }

    @Test(description = "Test visible endpoints in transactions.")
    public void testVisibleEndpointsInTransactions() {
        BallerinaASTResponse astResponse = LSExtensionTestUtil
                .getBallerinaDocumentAST(transactionVisibleEPsFile.toString(), this.serviceEndpoint);
        Assert.assertTrue(astResponse.isParseSuccess());
        JsonArray topLevelNodes = ((JsonObject) astResponse.getAst()).getAsJsonArray("topLevelNodes");

        // validate first function
        JsonObject func = topLevelNodes.get(2).getAsJsonObject();
        JsonObject transaction = func.getAsJsonObject("body")
                .getAsJsonArray("statements")
                .get(0).getAsJsonObject(); // first statement is the transaction
        Assert.assertNotNull(transaction);

        // EP in transaction body
        String trBodyEP = transaction.getAsJsonObject("transactionBody")
                .getAsJsonArray("VisibleEndpoints")
                .get(0).getAsJsonObject().get("name")
                .getAsString();
        Assert.assertEquals(trBodyEP, "clientEPInTransactionBody");

        // EP in retry body
        String retryBodyEP = transaction.getAsJsonObject("onRetryBody")
                .getAsJsonArray("VisibleEndpoints")
                .get(0).getAsJsonObject().get("name")
                .getAsString();
        Assert.assertEquals(retryBodyEP, "clientEPInRetryBody");

        // EP in committed body
        String committedEP = transaction.getAsJsonObject("committedBody")
                .getAsJsonArray("VisibleEndpoints")
                .get(0).getAsJsonObject().get("name")
                .getAsString();
        Assert.assertEquals(committedEP, "clientEPInCommittedBody");

        // EP in aborted body
        String abortedBodyEP = transaction.getAsJsonObject("abortedBody")
                .getAsJsonArray("VisibleEndpoints")
                .get(0).getAsJsonObject().get("name")
                .getAsString();
        Assert.assertEquals(abortedBodyEP, "clientEPInAbortedBody");

    }

    private void assertVisibleEndpoints(JsonElement ast) {
        JsonArray topLevelNodes = ((JsonObject) ast).getAsJsonArray("topLevelNodes");

        // validate first function
        JsonObject funcOne = topLevelNodes.get(1).getAsJsonObject();
        JsonObject wrapperIf = funcOne.getAsJsonObject("body")
                .getAsJsonArray("statements")
                .get(0).getAsJsonObject() // first statement is a If
                .getAsJsonObject("body");

        JsonArray visibleEpsInNestedIf = wrapperIf
                .getAsJsonArray("statements")
                .get(0).getAsJsonObject() // first statement is a If
                .getAsJsonObject("body")
                .getAsJsonArray("VisibleEndpoints");
        Assert.assertEquals(visibleEpsInNestedIf.get(0).getAsJsonObject().get("name").getAsString(),
                "clientEPInNestedIf");

        JsonArray visibleEpsInNestedWhile = wrapperIf
                .getAsJsonArray("statements")
                .get(1).getAsJsonObject() // second statement is a While
                .getAsJsonObject("body")
                .getAsJsonArray("VisibleEndpoints");
        Assert.assertEquals(visibleEpsInNestedWhile.get(0).getAsJsonObject().get("name").getAsString(),
                "clientEPInNestedWhile");

        // validate second function
        JsonObject funcTwo = topLevelNodes.get(1).getAsJsonObject();
        JsonObject wrapperWhile = funcTwo.getAsJsonObject("body")
                .getAsJsonArray("statements")
                .get(0).getAsJsonObject() // first statement is a While
                .getAsJsonObject("body");

        JsonArray visibleEpsInNestedIfF2 = wrapperWhile
                .getAsJsonArray("statements")
                .get(0).getAsJsonObject() // first statement is a If
                .getAsJsonObject("body")
                .getAsJsonArray("VisibleEndpoints");
        Assert.assertEquals(visibleEpsInNestedIfF2.get(0).getAsJsonObject().get("name").getAsString(),
                "clientEPInNestedIf");

        JsonArray visibleEpsInNestedWhileF2 = wrapperWhile
                .getAsJsonArray("statements")
                .get(1).getAsJsonObject() // second statement is a While
                .getAsJsonObject("body")
                .getAsJsonArray("VisibleEndpoints");
        Assert.assertEquals(visibleEpsInNestedWhileF2.get(0).getAsJsonObject().get("name").getAsString(),
                "clientEPInNestedWhile");
    }

    @AfterClass
    public void stopLangServer() {
        TestUtil.closeDocument(this.serviceEndpoint, nestedVisibleEPsFile);
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}

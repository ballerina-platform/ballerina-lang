/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.datamapper.codeaction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ballerinalang.datamapper.util.FileUtils;
import org.ballerinalang.datamapper.util.TestUtil;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
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

import static org.ballerinalang.datamapper.util.DataMapperTestUtils.getCodeActionResponse;


/**
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
public class CodeActionTest {

    private static Endpoint serviceEndpoint;
    private static final Logger log = LoggerFactory.getLogger(CodeActionTest.class);
    private static Server server;

    @BeforeClass
    private void init() throws Exception {
        serviceEndpoint = TestUtil.initializeLanguageSever();
        String startConfigPath = "codeaction" + File.separator + "config" + File.separator +
                "startConfig.json";
        JsonObject configs = FileUtils.fileContentAsObject(startConfigPath);
        TestUtil.setWorkspaceConfig(serviceEndpoint, configs);

        server = new Server(8080);
        Connector connector = new ServerConnector(server);
        server.addConnector(connector);

        String responseData = "{\"answer\":{\"maths\":\"student.grades.maths\",\"chemistry\":" +
                "\"student.grades.chemistry\",\"physics\":\"student.grades.physics\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-1")
    public void testDataMapperCodeAction_1(String config, String source) throws Exception {
        checkAssertion(config, source);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-2")
    public void testDataMapperCodeAction_2(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"city\":\"applicant.applying_student.city\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-3")
    public void testDataMapperCodeAction_3(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"country\":\"home.birth_country\",\"lane\":" +
                "\"home.lane\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-4")
    public void testDataMapperCodeAction_4(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"ID\":\"supplier.supplier_details.id\",\"email\":\"supplier.email\"," +
                "\"user\":{\"age\":\"supplier.supplier_details.age\",\"id\":\"supplier.id\",\"name\":\"" +
                "supplier.supplier_details.name\"}}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-5")
    public void testDataMapperCodeAction_5(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"history\":\"mark.history\",\"maths\":\"mark.maths\",\"physics\":\"" +
                "mark.physics\"}}";
        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-6")
    public void testDataMapperCodeAction_6(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"address\":\"student.address\",\"age\":\"student.age\",\"name\":\"" +
                "student.name\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-7")
    public void testDataMapperCodeAction_7(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"address\":\"student.address\",\"age\":\"student.age\"," +
                "\"contact_person\":\"student.contact_person\",\"name\":\"student.name\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-8")
    public void testDataMapperCodeAction_8(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"address\":\"student.contact.address\",\"number\":\"" +
                "student.contact.number\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-9")
    public void testDataMapperCodeAction_9(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"age\":\"student.age\",\"name\":\"student.name\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source);
    }

    @Test(dataProvider = "codeAction-data-mapper-data-provider-10")
    public void testDataMapperCodeAction_10(String config, String source) throws Exception {

        stopServer();

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(null));
        server.start();

        // Read expected results
        String configJsonPath = "codeaction" + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        // Get code action from language server
        JsonObject responseJson = getCodeActionResponse(source, configJsonObject, serviceEndpoint);

        if (responseJson.getAsJsonArray("result").isEmpty()) {
            Assert.assertFalse(false);
        } else {
            JsonObject expectedResponse = configJsonObject.get("expected").getAsJsonObject();
            String title = expectedResponse.get("title").getAsString();

            boolean codeActionFound = false;
            for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
                JsonElement right = jsonElement.getAsJsonObject().get("right");
                if (right.getAsJsonObject().get("title").getAsString().equals(title)) {
                    codeActionFound = true;
                }
            }
            Assert.assertFalse(codeActionFound, "Found a data mapper code action");
        }
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-1")
    public Object[][] codeActionDataMapperDataProvider_1() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper1.json", "dataMapper1.bal"},
                {"dataMapper5.json", "dataMapper5.bal"},
                {"dataMapper6.json", "dataMapper6.bal"},
                {"dataMapper7.json", "dataMapper7.bal"},
                {"dataMapper8.json", "dataMapper8.bal"},
                {"dataMapper9.json", "dataMapper9.bal"},
                {"dataMapper10.json", "dataMapper10.bal"},
                {"dataMapper17.json", "dataMapper17.bal"},
                {"module-response/defaultDataMapper1.json", "datamapper-module-test/defaultDataMapper1.bal"},
                {"module-response/defaultDataMapper2.json", "datamapper-module-test/defaultDataMapper2.bal"},
                {"module-response/defaultDataMapper3.json", "datamapper-module-test/defaultDataMapper3.bal"},
                {"module-response/defaultDataMapper4.json", "datamapper-module-test/defaultDataMapper4.bal"},
                {"module-response/moduleDataMapper1.json",
                        "datamapper-module-test/modules/module1/moduleDataMapper1.bal"},
                {"module-response/moduleDataMapper2.json",
                        "datamapper-module-test/modules/module2/moduleDataMapper2.bal"},
                {"module-response/moduleDataMapper3.json",
                        "datamapper-module-test/modules/module3/moduleDataMapper3.bal"},
        };
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-2")
    public Object[][] codeActionDataMapperDataProvider_2() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper2.json", "dataMapper2.bal"},
        };
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-3")
    public Object[][] codeActionDataMapperDataProvider_3() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper3.json", "dataMapper3.bal"},
        };
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-4")
    public Object[][] codeActionDataMapperDataProvider_4() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper4.json", "dataMapper4.bal"},
        };
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-5")
    public Object[][] codeActionDataMapperDataProvider_5() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper11.json", "dataMapper11.bal"},
        };
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-6")
    public Object[][] codeActionDataMapperDataProvider_6() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper12.json", "dataMapper12.bal"},
        };
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-7")
    public Object[][] codeActionDataMapperDataProvider_7() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper13.json", "dataMapper13.bal"},
        };
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-8")
    public Object[][] codeActionDataMapperDataProvider_8() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper14.json", "dataMapper14.bal"},
        };
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-9")
    public Object[][] codeActionDataMapperDataProvider_9() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper15.json", "dataMapper15.bal"},
        };
    }

    @DataProvider(name = "codeAction-data-mapper-data-provider-10")
    public Object[] codeActionDataMapperDataProvider_10() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper18.json", "dataMapper18.bal"},
        };
    }

    @AfterClass
    private void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(serviceEndpoint);
        try {
            server.stop();
            server.destroy();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void checkAssertion(String config, String source) throws Exception {

        JsonArray editResponse = null;
        // Read expected results
        String configJsonPath = "codeaction" + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        // Get code action from language server
        JsonObject responseJson = getCodeActionResponse(source, configJsonObject, serviceEndpoint);

        JsonObject expectedResponse = configJsonObject.get("expected").getAsJsonObject();
        String title = expectedResponse.get("title").getAsString();

        int numberOfDataMappingCodeAction = 0;
        boolean codeActionFound = false;
        boolean codeActionFoundOnlyOnce = false;
        for (JsonElement jsonElement : responseJson.getAsJsonArray("result")) {
            JsonElement right = jsonElement.getAsJsonObject().get("right");
            if (right.getAsJsonObject().get("title").getAsString().equals("Generate mapping function")) {
                JsonElement editText = right.getAsJsonObject().get("edit");
                if (editText == null) {
                    continue;
                }
                JsonArray edit = editText.getAsJsonObject().get("documentChanges")
                        .getAsJsonArray().get(0).getAsJsonObject().get("edits").getAsJsonArray();
                editResponse = edit;
                boolean editsMatched = expectedResponse.get("edits").getAsJsonArray().equals(edit);
                if (right.getAsJsonObject().get("title").getAsString().equals(title) && editsMatched) {
                    codeActionFound = true;
                    numberOfDataMappingCodeAction = numberOfDataMappingCodeAction + 1;
                }
            }
        }
        if (codeActionFound && numberOfDataMappingCodeAction == 1) {
            codeActionFoundOnlyOnce = true;
        }
        Assert.assertTrue(
                codeActionFoundOnlyOnce, "Cannot find expected Code Action for: " + title +
                        "\n expected response : " + expectedResponse.get("edits").getAsJsonArray() +
                        "\n actual response : " + editResponse);
    }

    public void stopServer() {
        try {
            server.stop();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

class DataMapperServiceHandler extends AbstractHandler {
    String responseData;
    int responseCode = 200;

    public DataMapperServiceHandler(String responseData) {
        this.responseData = responseData;
    }

    @Override
    public void handle(String target, Request jettyRequest, HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        // Mark the request as handled by this Handler.
        jettyRequest.setHandled(true);

        response.setStatus(responseCode);
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().print(responseData);
    }
}

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
package org.ballerinalang.datamapper.command;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ballerinalang.datamapper.AIDataMapperExecutor;
import org.ballerinalang.datamapper.util.FileUtils;
import org.ballerinalang.datamapper.util.TestUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.lsp4j.ExecuteCommandParams;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;



/**
 * Test Cases for data mapper commands.
 *
 * @since 2.0.0
 */
public class CommandExecutionTest {

    private static Endpoint serviceEndpoint;
    private static final Logger log = LoggerFactory.getLogger(CommandExecutionTest.class);
    private static Server server;
    private final Gson gson = new Gson();
    private final JsonParser parser = new JsonParser();
    private final Path resourcesPath = new File(getClass().getClassLoader().getResource("command").getFile()).toPath();

    @BeforeClass
    private void init() throws Exception {
        serviceEndpoint = TestUtil.initializeLanguageSever();
        String startConfigPath = "command" + File.separator + "config" + File.separator +
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

    @Test(dataProvider = "command-data-mapper-data-provider-1")
    public void testDataMapperCommand_1(String config, String source) throws Exception {
        checkAssertion(config, source, AIDataMapperExecutor.COMMAND);
    }

    @Test(dataProvider = "command-data-mapper-data-provider-2")
    public void testDataMapperCommand_2(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"city\":\"applicant.applying_student.city\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source, AIDataMapperExecutor.COMMAND);
    }

    @Test(dataProvider = "command-data-mapper-data-provider-3")
    public void testDataMapperCommand_3(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"country\":\"home.birth_country\",\"lane\":" +
                "\"home.lane\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source, AIDataMapperExecutor.COMMAND);
    }

    @Test(dataProvider = "command-data-mapper-data-provider-4")
    public void testDataMapperCommand_4(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"ID\":\"supplier.supplier_details.id\",\"email\":\"supplier.email\"," +
                "\"user\":{\"age\":\"supplier.supplier_details.age\",\"id\":\"supplier.id\",\"name\":\"" +
                "supplier.supplier_details.name\"}}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source, AIDataMapperExecutor.COMMAND);
    }

    @Test(dataProvider = "command-data-mapper-data-provider-5")
    public void testDataMapperCommand_5(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"history\":\"mark.history\",\"maths\":\"mark.maths\",\"physics\":\"" +
                "mark.physics\"}}";
        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source, AIDataMapperExecutor.COMMAND);
    }

    @Test(dataProvider = "command-data-mapper-data-provider-6")
    public void testDataMapperCommand_6(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"address\":\"student.address\",\"age\":\"student.age\",\"name\":\"" +
                "student.name\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source, AIDataMapperExecutor.COMMAND);
    }

    @Test(dataProvider = "command-data-mapper-data-provider-7")
    public void testDataMapperCommand_7(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"address\":\"student.address\",\"age\":\"student.age\"," +
                "\"contact_person\":\"student.contact_person\",\"name\":\"student.name\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source, AIDataMapperExecutor.COMMAND);
    }

    @Test(dataProvider = "command-data-mapper-data-provider-8")
    public void testDataMapperCommand_8(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"address\":\"student.contact.address\",\"number\":\"" +
                "student.contact.number\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source, AIDataMapperExecutor.COMMAND);
    }

    @Test(dataProvider = "command-data-mapper-data-provider-9")
    public void testDataMapperCommand_9(String config, String source) throws Exception {

        stopServer();

        String responseData = "{\"answer\":{\"age\":\"student.age\",\"name\":\"student.name\"}}";

        // Set the Handler.
        server.setHandler(new DataMapperServiceHandler(responseData));
        server.start();

        checkAssertion(config, source, AIDataMapperExecutor.COMMAND);
    }


    @DataProvider(name = "command-data-mapper-data-provider-1")
    public Object[][] commandDataMapperDataProvider_1() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper1.json", "dataMapper1.bal"},
//                {"dataMapper5.json", "dataMapper5.bal"},
//                {"dataMapper6.json", "dataMapper6.bal"},
//                {"dataMapper7.json", "dataMapper7.bal"},
//                {"dataMapper8.json", "dataMapper8.bal"},
//                {"dataMapper9.json", "dataMapper9.bal"},
//                {"dataMapper10.json", "dataMapper10.bal"},
//                {"dataMapper17.json", "dataMapper17.bal"},
//                {"module-response/defaultDataMapper1.json", "datamapper-module-test/defaultDataMapper1.bal"},
//                {"module-response/defaultDataMapper2.json", "datamapper-module-test/defaultDataMapper2.bal"},
//                {"module-response/defaultDataMapper3.json", "datamapper-module-test/defaultDataMapper3.bal"},
//                {"module-response/defaultDataMapper4.json", "datamapper-module-test/defaultDataMapper4.bal"},
//                {"module-response/moduleDataMapper1.json",
//                        "datamapper-module-test/modules/module1/moduleDataMapper1.bal"},
//                {"module-response/moduleDataMapper2.json",
//                        "datamapper-module-test/modules/module2/moduleDataMapper2.bal"},
//                {"module-response/moduleDataMapper3.json",
//                        "datamapper-module-test/modules/module3/moduleDataMapper3.bal"},
        };
    }

    @DataProvider(name = "command-data-mapper-data-provider-2")
    public Object[][] commandDataMapperDataProvider_2() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper2.json", "dataMapper2.bal"},
        };
    }

    @DataProvider(name = "command-data-mapper-data-provider-3")
    public Object[][] commandDataMapperDataProvider_3() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper3.json", "dataMapper3.bal"},
        };
    }

    @DataProvider(name = "command-data-mapper-data-provider-4")
    public Object[][] commandDataMapperDataProvider_4() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper4.json", "dataMapper4.bal"},
        };
    }

    @DataProvider(name = "command-data-mapper-data-provider-5")
    public Object[][] commandDataMapperDataProvider_5() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper11.json", "dataMapper11.bal"},
        };
    }

    @DataProvider(name = "command-data-mapper-data-provider-6")
    public Object[][] commandDataMapperDataProvider_6() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper12.json", "dataMapper12.bal"},
        };
    }

    @DataProvider(name = "command-data-mapper-data-provider-7")
    public Object[][] commandDataMapperDataProvider_7() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper13.json", "dataMapper13.bal"},
        };
    }

    @DataProvider(name = "command-data-mapper-data-provider-8")
    public Object[][] commandDataMapperDataProvider_8() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper14.json", "dataMapper14.bal"},
        };
    }

    @DataProvider(name = "command-data-mapper-data-provider-9")
    public Object[][] commandDataMapperDataProvider_9() {
        log.info("Test textDocument/codeAction QuickFixes");
        return new Object[][]{
                {"dataMapper15.json", "dataMapper15.bal"},
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

    public void checkAssertion(String config, String source, String command) throws Exception {

        String configJsonPath = "command" + File.separator + config;
        Path sourcePath = resourcesPath.resolve("source").resolve(source);
        org.ballerinalang.langserver.util.TestUtil.openDocument(serviceEndpoint, sourcePath);
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonObject expected = configJsonObject.get("expected").getAsJsonObject();

        List<Object> args = getArgs(configJsonObject, sourcePath);

        JsonObject responseJson = getCommandResponse(args, command);
        responseJson.get("result").getAsJsonObject().get("edit").getAsJsonObject().getAsJsonArray("documentChanges")
                .forEach(element -> element.getAsJsonObject().remove("textDocument"));

        log.debug("Actual response: {}", responseJson.get("result").getAsJsonObject().get("edit"));
        log.debug("Expected response: {}", expected.get("result").getAsJsonObject().get("edit"));

        org.ballerinalang.langserver.util.TestUtil.closeDocument(serviceEndpoint, sourcePath);
        Assert.assertEquals(responseJson, expected, "Test Failed for: " + config);
    }

    public void stopServer() {
        try {
            server.stop();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private List argsToJson(List<Object> args) {
        List<JsonObject> jsonArgs = new ArrayList<>();
        for (Object arg : args) {
            jsonArgs.add((JsonObject) gson.toJsonTree(arg));
        }
        return jsonArgs;
    }

    private JsonObject getCommandResponse(List<Object> args, String command) {
        List argsList = argsToJson(args);
        ExecuteCommandParams params = new ExecuteCommandParams(command, argsList);
        String response = org.ballerinalang.langserver.util.TestUtil.getExecuteCommandResponse(params,
                this.serviceEndpoint).replace("\\r\\n", "\\n");
        JsonObject responseJson = parser.parse(response).getAsJsonObject();
        responseJson.remove("id");
        return responseJson;
    }

    /**
     * Get args to be sent to LS.
     *
     * @param configJson Config json
     * @return List of args
     */
    private List<Object> getArgs(JsonObject configJson, Path sourcePath) {
        List<Object> args = new ArrayList<>();
        JsonArray arguments = configJson.get("arguments").getAsJsonArray();
        args.add(CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, sourcePath.toUri().toString()));
        args.add(CommandArgument.from("node.range", arguments.get(0).getAsJsonObject().get("value")));
        args.add(CommandArgument.from("ProcessedData", arguments.get(1).getAsJsonObject().get("value")));
        return args;
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

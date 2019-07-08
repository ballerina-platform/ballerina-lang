/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.definition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test goto definition language server feature.
 */
public class DefinitionTest {
    private Path configRoot;
    private Path sourceRoot;
    private Path projectPath = FileUtils.RES_DIR.resolve("referencesProject");
    protected Gson gson = new Gson();
    protected JsonParser parser = new JsonParser();
    protected Endpoint serviceEndpoint;

    @BeforeClass
    public void init() throws Exception {
        configRoot = FileUtils.RES_DIR.resolve("definition").resolve("expected");
        sourceRoot = FileUtils.RES_DIR.resolve("definition").resolve("sources");
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(description = "Test goto definitions", dataProvider = "testDataProvider")
    public void test(String configPath, String configDir) throws IOException {
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve(configDir)
                .resolve(configPath).toString());
        JsonObject source = configObject.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);
        this.compareResults(sourcePath, position, configObject, sourceRoot);
    }

    @Test(description = "Test Go to definition between two files in same module")
    public void testDifferentFiles() throws IOException {
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve("multifile")
                .resolve("defMultiFile1.json").toString());
        JsonObject source = configObject.getAsJsonObject("source");
        String dirPath = source.get("dir").getAsString().replace("/", CommonUtil.FILE_SEPARATOR);
        Path sourcePath = projectPath.resolve(dirPath).resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);
        this.compareResults(sourcePath, position, configObject, projectPath);
    }

    @Test(description = "Test Go to definition between two modules")
    public void testDifferentModule() throws IOException {
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve("multipkg")
                .resolve("defMultiPkg1.json").toString());
        JsonObject source = configObject.getAsJsonObject("source");
        String dirPath = source.get("dir").getAsString().replace("/", CommonUtil.FILE_SEPARATOR);
        Path sourcePath = projectPath.resolve(dirPath).resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);
        this.compareResults(sourcePath, position, configObject, projectPath);
    }

    private void compareResults(Path sourcePath, Position position, JsonObject configObject, Path root)
            throws IOException {
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String actualStr = TestUtil.getDefinitionResponse(sourcePath.toString(), position, serviceEndpoint);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

        JsonArray expected = configObject.getAsJsonArray("result");
        JsonArray actual = parser.parse(actualStr).getAsJsonObject().getAsJsonArray("result");
        this.alterExpectedUri(expected, root);
        this.alterActualUri(actual);
        Assert.assertEquals(actual, expected);
    }

    @DataProvider
    public Object[][] testDataProvider() throws IOException {
        return new Object[][]{
                {"defXMLNS1.json", "xmlns"},
                {"defXMLNS2.json", "xmlns"},
                {"defXMLNS3.json", "xmlns"},
                {"defFunction1.json", "function"},
                {"defFunction3.json", "function"},
                {"defFunction4.json", "function"},
                {"defFunction5.json", "function"},
                {"defService1.json", "service"},
                {"defService2.json", "service"},
                {"defService3.json", "service"},
                {"defService4.json", "service"},
                {"defService5.json", "service"},
                {"defArrays1.json", "array"},
                {"defArrays2.json", "array"},
                {"defArrays3.json", "array"},
                {"defArrays4.json", "array"},
                {"defArrays5.json", "array"},
                {"defArrays6.json", "array"},
                {"defArrays7.json", "array"},
                {"defArrays8.json", "array"},
                {"defArrays9.json", "array"},
                {"defArrays10.json", "array"},
                {"defArrays11.json", "array"},
                {"defArrays12.json", "array"},
                {"defArrays13.json", "array"},
                {"defArrays14.json", "array"},
                {"defArrays15.json", "array"},
                {"defArrays16.json", "array"},
                {"defArrays17.json", "array"},
                {"defArrays19.json", "array"},
                {"defAssignment1.json", "assignment"},
                {"defAssignment2.json", "assignment"},
                {"defAssignment3.json", "assignment"},
                {"defAssignment4.json", "assignment"},
                {"defAssignment6.json", "assignment"},
                {"defAssignment7.json", "assignment"},
                {"defAssignment8.json", "assignment"},
                {"defCompoundAssignment1.json", "compoundassignment"},
                {"defForeach1.json", "foreach"},
                {"defForeach2.json", "foreach"},
                {"defForeach3.json", "foreach"},
                {"defForeach4.json", "foreach"},
                {"defForeach5.json", "foreach"},
                {"defForeach6.json", "foreach"},
                {"defForeach7.json", "foreach"},
                {"defForeach8.json", "foreach"},
                {"defForeach9.json", "foreach"},
                {"defForeach10.json", "foreach"},
                {"defForeach11.json", "foreach"},
                {"defForeach12.json", "foreach"},
                {"defForeach13.json", "foreach"},
                {"defForeach14.json", "foreach"},
                {"defForeach15.json", "foreach"},
                {"defForeach16.json", "foreach"},
                {"defForeach17.json", "foreach"},
                {"defForeach18.json", "foreach"},
                {"defForeach19.json", "foreach"},
                {"defForeach20.json", "foreach"},
                {"defForeach21.json", "foreach"},
                {"defForeach22.json", "foreach"},
                {"defForeach23.json", "foreach"},
                {"defForeach24.json", "foreach"},
                {"defForeach25.json", "foreach"},
                {"defForeach26.json", "foreach"},
                {"defIfElse1.json", "ifelse"},
                {"defIfElse2.json", "ifelse"},
                {"defIfElse3.json", "ifelse"},
                {"defIfElse4.json", "ifelse"},
                {"defIfElse5.json", "ifelse"},
                {"defIfElse6.json", "ifelse"},
                {"defMatchStmt1.json", "matchstmt"},
                {"defMatchStmt2.json", "matchstmt"},
                {"defMatchStmt3.json", "matchstmt"},
                {"defMatchStmt4.json", "matchstmt"},
                {"defMatchStmt5.json", "matchstmt"},
                {"defMatchStmt6.json", "matchstmt"},
                {"defTransaction1.json", "transaction"},
                {"defGlobal1.json", "global"},
                {"defRecord1.json", "record"},
                {"defError1.json", "error"},
                {"defWaitExpression1.json", "waitexpression"},
                {"defExpressionConnectorInit.json", "expression"},
//                TODO: enable
//                {"defExpressionNamedArgs.json", "expression"},
        };
    }
    
    @AfterClass
    public void shutDownLanguageServer() throws IOException {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    private void alterExpectedUri(JsonArray expected, Path root) throws IOException {
        for (JsonElement jsonElement : expected) {
            JsonObject item = jsonElement.getAsJsonObject();
            String[] uriComponents = item.get("uri").toString().replace("\"", "").split("/");
            Path expectedPath = Paths.get(root.toUri());
            for (String uriComponent : uriComponents) {
                expectedPath = expectedPath.resolve(uriComponent);
            }
            item.remove("uri");
            item.addProperty("uri", expectedPath.toFile().getCanonicalPath());
        }
    }

    private void alterActualUri(JsonArray actual) throws IOException {
        for (JsonElement jsonElement : actual) {
            JsonObject item = jsonElement.getAsJsonObject();
            String uri = item.get("uri").toString().replace("\"", "");
            String canonicalPath = new File(URI.create(uri)).getCanonicalPath();
            item.remove("uri");
            item.addProperty("uri", canonicalPath);
        }
    }
}

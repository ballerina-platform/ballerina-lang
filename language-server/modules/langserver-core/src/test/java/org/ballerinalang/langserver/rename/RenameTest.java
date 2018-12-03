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
package org.ballerinalang.langserver.rename;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.completion.util.FileUtils;
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
import java.nio.file.Path;

/**
 * Test class for Renaming.
 *
 * @since 0.982.0
 */
public class RenameTest {

    private Endpoint serviceEndpoint;

    private JsonParser parser = new JsonParser();

    private Path sourcesPath = FileUtils.RES_DIR.resolve("rename");

    @BeforeClass
    public void init() {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test(dataProvider = "rename-data-provider")
    public void test(String config, String source)
            throws IOException {
        String configJsonPath = "rename" + File.separator + config;
        Path sourcePath = sourcesPath.resolve("source").resolve(source);

        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        JsonArray expectedJson = configJsonObject.get("expected").getAsJsonArray();

        TestUtil.openDocument(this.serviceEndpoint, sourcePath);
        String response = getRenameResponse(configJsonObject, sourcePath);
        TestUtil.closeDocument(this.serviceEndpoint, sourcePath);

        JsonObject json = parser.parse(response).getAsJsonObject();

        JsonArray responseJson = new JsonArray();
        JsonArray changes = json.getAsJsonObject("result").getAsJsonArray("documentChanges");
        for (JsonElement change : changes) {
            responseJson.addAll(change.getAsJsonObject().getAsJsonArray("edits"));
        }

        Assert.assertTrue(isSubArray(expectedJson, responseJson), "Test failed for: " + sourcePath.getFileName());
    }

    private boolean isSubArray(JsonArray subArray, JsonArray array) {
        boolean isSubList = true;
        for (JsonElement exp : subArray) {
            isSubList = false;
            for (JsonElement res : array) {
                if (exp.equals(res)) {
                    isSubList = true;
                    break;
                }
            }
            if (!isSubList) {
                break;
            }
        }
        return isSubList;
    }

    @DataProvider(name = "rename-data-provider")
    public Object[][] dataProvider() {
        return new Object[][] {
                {"renameMultiPackagesObjFunc.json", "renameMultiPackagesObj/main.bal"},
                {"renameMultiPackagesObjType.json", "renameMultiPackagesObj/main.bal"},
                {"renameMultiPackagesVar.json", "renameMultiPackagesVar/main.bal"},
                {"renameService2.json", "renameService2/renameService2.bal"},
                {"renameResource.json", "renameService/renameService.bal"},
                {"renameResource2.json", "renameService/renameService.bal"},
//                {"renameVar.json", "renameVar/renameVar.bal"},
                {"renameXml.json", "renameXml/renameXml.bal"},
                {"renameJson.json", "renameService/renameService.bal"},
                {"renameClient.json", "renameClient/renameClient.bal"},
                {"renameHttpResponse.json", "renameService/renameService.bal"},
                {"renameEndpoint.json", "renameService/renameService.bal"},
                {"renameService.json", "renameService/renameService.bal"},
                {"renameObjectAttribute.json", "renameObjectAttribute/renameObjectAttribute.bal"},
                {"renameFunction.json", "renameFunction/renameFunction.bal"},
//                {"renameObjectFunction.json", "renameObject/renameObject.bal"},
                {"renameObjectInstance.json", "renameObject/renameObject.bal"},
                {"renameObjectType.json", "renameObject/renameObject.bal"},
                {"renameArray.json", "renameArray/renameArray.bal"},
                {"renameSimpleVariable.json", "renameSimpleVariable/renameSimpleVariable.bal"},
                {"renameMap.json", "renameMap/renameMap.bal"},
                {"renameWithinTransaction.json", "renameWithinTransaction/renameWithinTransaction.bal"},
                {"renameWithinWhile.json", "renameWithinWhile/renameWithinWhile.bal"},
        };
    }

    private String getRenameResponse(JsonObject config, Path sourcePath) throws IOException {
        JsonObject positionObj = config.get("position").getAsJsonObject();
        String newName = config.get("newName").getAsString();
        Position position = new Position();
        position.setLine(positionObj.get("line").getAsInt());
        position.setCharacter(positionObj.get("character").getAsInt());
        return TestUtil.getRenameResponse(sourcePath.toString(), position, newName, this.serviceEndpoint);
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}

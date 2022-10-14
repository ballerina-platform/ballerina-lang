/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.util.TestUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Unit tests for {@link org.ballerinalang.langserver.codeaction.providers.GenerateModuleForClientDeclCodeAction} .
 *
 * @since 2201.3.0
 */
public class GenerateModuleForClientDeclCodeActionTest extends AbstractCodeActionTest {

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"generate_module_for_client_decl1.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "generate-module-for-client-decl";
    }

    @Override
    protected boolean validateAndModifyArguments(JsonObject actualCommand, 
                                                 JsonArray actualArgs,
                                                 JsonArray expArgs, 
                                                 Path sourceRoot, 
                                                 Path sourcePath) {
        for (JsonElement actualArg : actualArgs) {
            JsonObject arg = actualArg.getAsJsonObject();
            if ("doc.uri".equals(arg.get("key").getAsString())) {
                Optional<Path> docPath = PathUtil.getPathFromURI(arg.get("value").getAsString());
                Optional<String> actualFilePath =
                        docPath.map(path -> path.toString().replace(sourceRoot.toString(), ""));
                JsonObject docUriObj = expArgs.get(0).getAsJsonObject();
                String expectedFilePath = docUriObj.get("value").getAsString();
                if (docPath.isPresent()) {
                    // We just check file names, since one refers to file in build/ while
                    // the other refers to the file in test resources
                    String actualPath = actualFilePath.get();
                    if (actualFilePath.get().startsWith("/") || actualFilePath.get().startsWith("\\")) {
                        actualPath = actualFilePath.get().substring(1);
                    }
                    if (sourceRoot.resolve(actualPath).equals(sourceRoot.resolve(expectedFilePath))) {
                        return true;
                    }
                    JsonArray newArgs = new JsonArray();
                    docUriObj.addProperty("value", actualPath);
                    newArgs.add(docUriObj);
                    actualCommand.add("arguments", newArgs);
                    return false;
                }
                return false;
            }
        }
        return TestUtil.isArgumentsSubArray(actualArgs, expArgs);
    }
}

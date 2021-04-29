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
package org.ballerinalang.langserver.codeaction;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CompilerPluginCodeActionTest {

    private Endpoint serviceEndpoint;

    private final Gson gson = new Gson();

    private final JsonParser parser = new JsonParser();

    @BeforeClass
    public void init() throws Exception {
        this.serviceEndpoint = TestUtil.initializeLanguageSever();
    }

    @Test
    public void test() throws IOException {
        Path sourcePath = Paths.get("/home/imesha/Documents/WSO2/Ballerina/ballerina-lang/language-server/modules/langserver-core/src/test/resources/compiler_plugin/main.bal");
        TestUtil.openDocument(serviceEndpoint, sourcePath);

        Range range = new Range(new Position(9, 4), new Position(9, 4));
        String res = TestUtil.getCodeActionResponse(serviceEndpoint, sourcePath.toString(), range, new CodeActionContext());
        JsonObject codeActionResponse = parser.parse(res).getAsJsonObject();

        JsonArray result = codeActionResponse.getAsJsonArray("result");
        Assert.assertTrue(result.size() > 0);

        JsonObject right = result.get(0).getAsJsonObject().getAsJsonObject("right");
        String command = right.getAsJsonObject("command").get("command").getAsString();
        JsonArray arguments = right.getAsJsonObject("command").getAsJsonArray("arguments");

        System.out.println(right.toString());
        Assert.assertNotNull(command);
        Assert.assertTrue(arguments.size() > 0);
        
        List<Object> args = new LinkedList<>();
        for (JsonElement argument : arguments) {
            args.add(argument.getAsJsonObject());
        }
        JsonObject jsonResonse = getCommandResponse(args, command);
        System.out.println(jsonResonse.toString());
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
        String response = TestUtil.getExecuteCommandResponse(params, this.serviceEndpoint).replace("\\r\\n", "\\n");
        JsonObject responseJson = parser.parse(response).getAsJsonObject();
        responseJson.remove("id");
        return responseJson;
    }
}

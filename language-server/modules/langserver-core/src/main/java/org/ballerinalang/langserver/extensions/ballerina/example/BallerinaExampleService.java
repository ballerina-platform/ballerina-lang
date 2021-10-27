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
package org.ballerinalang.langserver.extensions.ballerina.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Ballerina example service.
 *
 * @since 0.981.2
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("ballerinaExample")
public class BallerinaExampleService implements ExtendedLanguageServerService {
    private static final String BBE_DEF_JSON = "index.json";
    private static final String EXAMPLES_DIR = "examples";
    private static final Type EXAMPLE_CATEGORY_TYPE = new TypeToken<List<BallerinaExampleCategory>>() {
    }.getType();
    private  LSClientLogger clientLogger;

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager,
                     LanguageServerContext serverContext) {
        this.clientLogger = LSClientLogger.getInstance(serverContext);
    }

    @JsonRequest
    public CompletableFuture<BallerinaExampleListResponse> list(BallerinaExampleListRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaExampleListResponse response = new BallerinaExampleListResponse();
            Gson gson = new Gson();
            Path bbeJSONPath = Paths.get(CommonUtil.BALLERINA_HOME).resolve(EXAMPLES_DIR).resolve(BBE_DEF_JSON);
            try {
                InputStreamReader fileReader = new InputStreamReader(
                        new FileInputStream(bbeJSONPath.toFile()), StandardCharsets.UTF_8);
                JsonReader jsonReader = new JsonReader(fileReader);
                List<BallerinaExampleCategory> data = gson.fromJson(jsonReader, EXAMPLE_CATEGORY_TYPE);
                response.setSamples(data);
            } catch (Throwable e) {
                String msg = "Operation 'ballerinaExample/list' failed!";
                this.clientLogger.logError(ExampleContext.EXAMPLE_LIST, msg, e,
                                           new TextDocumentIdentifier(bbeJSONPath.toString()),
                                           (Position) null);
                response.setSamples(new ArrayList<>());
            }
            return response;
        });
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }
}

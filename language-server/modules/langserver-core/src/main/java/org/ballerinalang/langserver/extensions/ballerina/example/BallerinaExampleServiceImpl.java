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
import org.ballerinalang.langserver.LSGlobalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Ballerina example service.
 *
 * @since 0.981.2
 */
public class BallerinaExampleServiceImpl implements BallerinaExampleService {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaExampleServiceImpl.class);

    private static final String BBE_DEF_JSON = "examples/all-bbes.json";

    private static final Type EXAMPLE_CATEGORY_TYPE = new TypeToken<List<BallerinaExampleCategory>>(){}.getType();

    private LSGlobalContext lsGlobalContext;

    public BallerinaExampleServiceImpl(LSGlobalContext lsGlobalContext) {
        this.lsGlobalContext = lsGlobalContext;
    }

    @Override
    public CompletableFuture<BallerinaExampleListResponse> list(BallerinaExampleListRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaExampleListResponse response = new BallerinaExampleListResponse();
            Gson gson = new Gson();
            InputStreamReader inputStreamReader = new InputStreamReader(Thread.currentThread()
                    .getContextClassLoader().getResourceAsStream(BBE_DEF_JSON), StandardCharsets.UTF_8);
            JsonReader reader = new JsonReader(inputStreamReader);
            List<BallerinaExampleCategory> data = gson.fromJson(reader, EXAMPLE_CATEGORY_TYPE);
            response.setSamples(data);
            return response;
        });
    }
}

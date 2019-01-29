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
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
public class BallerinaExampleServiceImpl implements BallerinaExampleService {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaExampleServiceImpl.class);

    private static final String BBE_DEF_JSON = "index.json";

    private static final String EXAMPLES_DIR = "examples";

    private static final Type EXAMPLE_CATEGORY_TYPE = new TypeToken<List<BallerinaExampleCategory>>() { }.getType();

    private LSGlobalContext lsGlobalContext;

    public BallerinaExampleServiceImpl(LSGlobalContext lsGlobalContext) {
        this.lsGlobalContext = lsGlobalContext;
    }

    @Override
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
            } catch (FileNotFoundException e) {
                if (CommonUtil.LS_DEBUG_ENABLED) {
                    String msg = e.getMessage();
                    logger.error("Error while fetching the list of examples" + ((msg != null) ? ": " + msg : ""), e);
                }
                response.setSamples(new ArrayList<>());
            }
            return response;
        });
    }
}

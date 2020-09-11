/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.openapi.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiType;
import org.ballerinalang.openapi.utils.TypeExtractorUtil;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This for test the filter of tags and operation in TypeExtractorUtil.
 */
public class TypeExtractorUtilTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/typeExtractor").toAbsolutePath();
    private OpenAPI api;
    private BallerinaOpenApiType ballerinaOpenApiType;

    @Test(enabled = false, description = "Test input Tags")
    public void testTag() throws BallerinaOpenApiException {
        Path contractPath = RES_DIR.resolve("petstore.yaml");
        api = new OpenAPIV3Parser().read(contractPath.toString());
        List<String> l1 = new ArrayList<>();
        List<String> l2 = new ArrayList<>();
        Filter filter = new Filter(l1, l2);
        l1.add("pets");
        l1.add("list");
        ballerinaOpenApiType = TypeExtractorUtil.extractOpenApiObject(api, filter);

    }

    @Test(enabled = false, description = "Test input operations")
    public void testOperations() throws BallerinaOpenApiException {
        Path contractPath = RES_DIR.resolve("petstore.yaml");
        api = new OpenAPIV3Parser().read(contractPath.toString());
        List<String> l1 = new ArrayList<>();
        List<String> l2 = new ArrayList<>();
        Filter filter = new Filter(l1, l2);
        l2.add("listPets");
        ballerinaOpenApiType = TypeExtractorUtil.extractOpenApiObject(api, filter);
    }
}

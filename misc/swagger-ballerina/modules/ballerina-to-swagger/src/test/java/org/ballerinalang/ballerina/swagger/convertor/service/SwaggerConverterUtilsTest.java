/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.ballerina.swagger.convertor.service;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Swagger related utility classes.
 */

public class SwaggerConverterUtilsTest {
    @Test(description = "Get application count test")
    public void testGetApplicationCount() {
        String ballerinaSource = "package passthroughservice.samples;\n" +
                "\n" +
                "import ballerina.net.http;\n" +
                "\n" +
                "@http:configuration {basePath:\"/nyseStock\"}\n" +
                "service<http> nyseStockQuote {\n" +
                "\n" +
                "    @http:resourceConfig {\n" +
                "        methods:[\"GET\"]\n" +
                "    }\n" +
                "    resource stocks (http:Connection conn, http:InRequest inReq) {\n" +
                "        http:OutResponse res = {};\n" +
                "        json payload = {\"exchange\":\"nyse\", \"name\":\"IBM\", \"value\":\"127.50\"};\n" +
                "        res.setJsonPayload(payload);\n" +
                "        _ = conn.respond(res);\n" +
                "    }\n" +
                "}\n";
        String serviceName = "nyseStockQuote";
        try {
            String swaggerDefinition = SwaggerConverterUtils.generateSwaggerDefinitions(ballerinaSource, serviceName);
            Assert.assertNotNull(swaggerDefinition);
        } catch (IOException e) {
        }


    }

}

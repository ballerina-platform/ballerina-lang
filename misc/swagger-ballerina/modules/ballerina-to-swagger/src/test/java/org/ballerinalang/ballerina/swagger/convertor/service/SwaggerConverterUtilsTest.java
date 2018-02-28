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

package org.ballerinalang.ballerina.swagger.convertor.service;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Ballerina conversion to swagger and open API will test in this class.
 */

public class SwaggerConverterUtilsTest {
    //Sample Ballerina Service definitions to be used for tests.
    private static String sampleBallerinaServiceString = "package passthroughservice.samples;\n" +
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

    private static String complexServiceSample = "import ballerina.net.http;\n" +
            "import ballerina.net.http.resiliency;\n" +
            "import ballerina.runtime;service<http> failoverService {\n" +
            "    int[] failoverHttpStatusCodes = [400, 404, 500];\n" +
            "    resiliency:FailoverConfig errorCode = {failoverCodes:failoverHttpStatusCodes};\n" +
            "    @http:resourceConfig {\n" +
            "        path:\"/\"\n" +
            "    }    resource failoverPostResource (http:Connection conn, http:InRequest req) {\n" +
            "        endpoint<http:HttpClient> endPoint {\n" +
            "            create resiliency:Failover(\n" +
            "                    [create http:HttpClient(\"http://localhost:23456/mock\", {}),\n" +
            "                     create http:HttpClient(\"http://localhost:9090/echo\",\n" +
            "                                    {endpointTimeout:5000}),\n" +
            "                     create http:HttpClient(\"http://localhost:9090/mock\", {})],\n" +
            "                     errorCode);\n" +
            "        }        http:InResponse inResponse = {};\n" +
            "        http:HttpConnectorError err;        http:OutRequest outRequest = {};\n" +
            "        json requestPayload = {\"name\":\"Ballerina\"};\n" +
            "        outRequest.setJsonPayload(requestPayload);\n" +
            "        inResponse, err = endPoint.post(\"/\", outRequest);        " +
            "http:OutResponse outResponse = {};\n" +
            "        if (err != null) {\n" +
            "            outResponse.statusCode = err.statusCode;\n" +
            "            outResponse.setStringPayload(err.message);\n" +
            "            _ = conn.respond(outResponse);\n" +
            "        } else {\n" +
            "            _ = conn.forward(inResponse);\n" +
            "        }\n" +
            "    }}\n" +
            "service<http> echo {\n" +
            "    @http:resourceConfig {\n" +
            "        methods:[\"POST\", \"PUT\", \"GET\"],\n" +
            "        path:\"/\"\n" +
            "    }\n" +
            "    resource echoResource (http:Connection conn, http:InRequest req) {\n" +
            "        http:OutResponse outResponse = {};\n" +
            "        runtime:sleepCurrentWorker(30000);\n" +
            "        outResponse.setStringPayload(\"Resource is invoked\");\n" +
            "        _ = conn.respond(outResponse);\n" +
            "    }\n" +
            "}service<http> mock {\n" +
            "    @http:resourceConfig {\n" +
            "        methods:[\"POST\", \"PUT\", \"GET\"],\n" +
            "        path:\"/\"\n" +
            "    }\n" +
            "    resource mockResource (http:Connection conn, http:InRequest req) {\n" +
            "        http:OutResponse outResponse = {};\n" +
            "        outResponse.setStringPayload(\"Mock Resource is Invoked.\");\n" +
            "        _ = conn.respond(outResponse);\n" +
            "    }\n" +
            "}";

    @Test(description = "Test ballerina to swagger conversion")
    public void testBallerinaToSwaggerConversion() {
        String serviceName = "nyseStockQuote";
        try {
            String swaggerDefinition = SwaggerConverterUtils.generateSwaggerDefinitions(sampleBallerinaServiceString,
                    serviceName);
            Assert.assertNotNull(swaggerDefinition);
        } catch (IOException e) {
            Assert.fail("Error while converting ballerina service to swagger definition");
        }
    }


    @Test(description = "Test OAS definition generation from ballerina service")
    public void testOASGeneration() {
        String serviceName = "nyseStockQuote";
        try {
            String swaggerDefinition = SwaggerConverterUtils.generateOAS3Definitions(sampleBallerinaServiceString,
                    serviceName);
            Assert.assertNotNull(swaggerDefinition);
        } catch (IOException e) {
            Assert.fail("Error while converting ballerina service to swagger definition");
        }
    }


    @Test(description = "Test OAS definition generation from ballerina service with empty service name")
    public void testOASGenerationWithEmptyServiceName() {
        String serviceName = "";
        try {
            String swaggerDefinition = SwaggerConverterUtils.generateOAS3Definitions(sampleBallerinaServiceString,
                    serviceName);
            Assert.assertNotNull(swaggerDefinition);
        } catch (IOException e) {
            Assert.fail("Error while converting ballerina service to swagger definition with empty service name");
        }
    }


    @Test(description = "Test OAS definition generation from ballerina service with complex service")
    public void testOASGenerationWithEmptyServiceNameM() {
        String serviceName = "mock";
        try {
            String swaggerDefinition = SwaggerConverterUtils.generateOAS3Definitions(complexServiceSample,
                    serviceName);
            Assert.assertNotNull(swaggerDefinition);
        } catch (IOException e) {
            Assert.fail("Error while converting ballerina service to swagger definition with empty service name");
        }
    }

}

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
    private static String sampleBallerinaServiceString = "import ballerina/http;\n" +
            "\n" +
            "\n" +
            "endpoint http:Listener backendEP {\n" +
            "   port:8081\n" +
            "};\n" +
            "\n" +
            "endpoint http:Client backendClientEP {\n" +
            "   targets: [{url: \"http://localhost:8081\"}]\n" +
            "};\n" +
            "\n" +
            "\n" +
            "@http:ServiceConfig {\n" +
            "   methods:[\"GET\"],\n" +
            "   basePath:\"/hello\"\n" +
            "   \n" +
            "}\n" +
            "service hello bind backendEP {\n" +
            "\n" +
            "   @http:ResourceConfig {\n" +
            "       path:\"/\"\n" +
            "   }\n" +
            "   sayHello (endpoint outboundEP, http:Request request) {\n" +
            "       http:Response response = new;\n" +
            "       response.setStringPayload(\"Hello World!!!\");\n" +
            "       _ = outboundEP -> respond(response);\n" +
            "   }\n" +
            "}\n";

    private static String complexServiceSample = "import ballerina/http;\n" +
            "\n" +
            "\n" +
            "endpoint http:Listener backendEP {\n" +
            "   port:8081\n" +
            "};\n" +
            "\n" +
            "endpoint http:Client backendClientEP {\n" +
            "   targets: [{url: \"http://localhost:8081\"}]\n" +
            "};\n" +
            "\n" +
            "\n" +
            "@http:ServiceConfig {\n" +
            "   methods:[\"GET\"],\n" +
            "   basePath:\"/hello\"\n" +
            "   \n" +
            "}\n" +
            "service hello bind backendEP {\n" +
            "\n" +
            "   @http:ResourceConfig {\n" +
            "       path:\"/\"\n" +
            "   }\n" +
            "   sayHello (endpoint outboundEP, http:Request request) {\n" +
            "       http:Response response = new;\n" +
            "       response.setStringPayload(\"Hello World!!!\");\n" +
            "       _ = outboundEP -> respond(response);\n" +
            "   }\n" +
            "}\n";

    //@Test(description = "Test ballerina to swagger conversion")
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


    //@Test(description = "Test OAS definition generation from ballerina service")
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


    //@Test(description = "Test OAS definition generation from ballerina service with complex service")
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

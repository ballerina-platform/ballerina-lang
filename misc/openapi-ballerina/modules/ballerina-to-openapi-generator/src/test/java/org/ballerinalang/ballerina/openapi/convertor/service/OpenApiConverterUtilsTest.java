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

package org.ballerinalang.ballerina.openapi.convertor.service;

import org.ballerinalang.ballerina.openapi.convertor.OpenApiConverterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Ballerina conversion to OpenApi will test in this class.
 */

public class OpenApiConverterUtilsTest {
    //Sample Ballerina Service definitions to be used for tests.
    private static String echoServiceBal = "import ballerina/http;\n" +
            "\n" +
            "listener http:Listener ep = new (9090);\n" +
            "\n" +
            "service hello on ep {\n" +
            "    resource function hi (http:Caller caller, http:Request request) {\n" +
            "        http:Response res;\n" +
            "        res.setStringPayload(\"Hello World!\\n\");\n" +
            "        _ = caller->respond(res);\n" +
            "    }\n" +
            "}";

    private static String invalidEchoServiceBal = "import ballerina/http;\n" +
            "\n" +
            "listener http:Listener ep = new (9090);\n" +
            "\n" +
            "service hello on ep \n" +
            "    resource function hi (http:Caller caller, http:Request request) {\n" +
            "        http:Response res;\n" +
            "        res.setStringPayload(\"Hello World!\\n\");\n" +
            "        _ = caller->respond(res);\n" +
            "    }\n" +
            "}";

    private static String serviceWithMultipleHTTPMethodsInResourceLevel = "import ballerina/http;\n" +
            "\n" +
            "\n" +
            "listener http:Listener backendEP = new (8081);" +
            "\n" +
            "http:Client backendClientEP = new(\"http://localhost:8081\");\n" +
            "\n" +
            "@http:ServiceConfig {\n" +
            "   basePath:\"/hello\"\n" +
            "   \n" +
            "}\n" +
            "service hello on backendEP {\n" +
            "\n" +
            "   @http:ResourceConfig {\n" +
            "   methods:[\"GET, PUT\"],\n" +
            "       path:\"/\"\n" +
            "   }\n" +
            "   resource function sayHello (http:Caller outboundEP, http:Request request) {\n" +
            "       http:Response response = new;\n" +
            "       response.setStringPayload(\"Hello World!!!\");\n" +
            "       _ = outboundEP -> respond(response);\n" +
            "   }\n" +
            "}\n";

    private static String serviceWithNoHTTPMethodsAtResourceLevel = "import ballerina/http;\n" +
            "\n" +
            "\n" +
            "listener http:Listener backendEP = new (8081);" +
            "\n" +
            "http:Client backendClientEP = new(\"http://localhost:8081\");\n" +
            "\n" +
            "@http:ServiceConfig {\n" +
            "   basePath:\"/hello\"\n" +
            "   \n" +
            "}\n" +
            "service hello on backendEP {\n" +
            "\n" +
            "   @http:ResourceConfig {\n" +
            "       path:\"/\"\n" +
            "   }\n" +
            "   resource function sayHello (http:Caller outboundEP, http:Request request) {\n" +
            "       http:Response response = new;\n" +
            "       response.setStringPayload(\"Hello World!!!\");\n" +
            "       _ = outboundEP -> respond(response);\n" +
            "   }\n" +
            "}\n";

    private static String serviceWithOneHTTPMethod = "import ballerina/http;\n" +
            "\n" +
            "\n" +
            "listener http:Listener backendEP = new (8081);" +
            "\n" +
            "http:Client backendClientEP = new(\"http://localhost:8081\");\n" +
            "\n" +
            "@http:ServiceConfig {\n" +
            "   basePath:\"/hello\"\n" +
            "   \n" +
            "}\n" +
            "service hello on backendEP {\n" +
            "\n" +
            "   @http:ResourceConfig {\n" +
            "   methods:[\"GET\"],\n" +
            "       path:\"/\"\n" +
            "   }\n" +
            "   resource function sayHello (http:Caller outboundEP, http:Request request) {\n" +
            "       http:Response response = new;\n" +
            "       response.setStringPayload(\"Hello World!!!\");\n" +
            "       _ = outboundEP -> respond(response);\n" +
            "   }\n" +
            "}\n";

    String sampleOpenApiWithTwoResources = "import ballerina/http;\n" +
            "\n" +
            "\n" +
            "listener http:Listener backendEP = new (8081);" +
            "\n" +
            "http:Client backendClientEP = new(\"http://localhost:8081\");\n" +
            "\n" +
            "@http:ServiceConfig {\n" +
            "   basePath:\"/hello\"\n" +
            "   \n" +
            "}\n" +
            "service hello on backendEP {\n" +
            "\n" +
            "   @http:ResourceConfig {\n" +
            "   methods:[\"GET\"],\n" +
            "       path:\"/goma\"\n" +
            "   }\n" +
            "   resource function sayHello (http:Caller outboundEP, http:Request request) {\n" +
            "       http:Response response = new;\n" +
            "       response.setStringPayload(\"Hello World!!!\");\n" +
            "       _ = outboundEP -> respond(response);\n" +
            "   }\n" +
            "\n" +
            "   @http:ResourceConfig {\n" +
            "   methods:[\"PUT\"],\n" +
            "       path:\"/ela\"\n" +
            "   }\n" +
            "   resource function sayHelloPut (http:Caller outboundEP, http:Request request) {\n" +
            "       http:Response response = new;\n" +
            "       response.setStringPayload(\"Hello World!!!\");\n" +
            "       _ = outboundEP -> respond(response);\n" +
            "   }\n" +
            "}";
    String sampleOpenApiWithMultipleResourceAndVerbs = "import ballerina/http;\n" +
            "\n" +
            "\n" +
            "listener http:Listener backendEP = new (8081);" +
            "\n" +
            "http:Client backendClientEP = new(\"http://localhost:8081\");\n" +
            "\n" +
            "@http:ServiceConfig {\n" +
            "   basePath:\"/hello\"\n" +
            "   \n" +
            "}\n" +
            "service hello on backendEP {\n" +
            "\n" +
            "   @http:ResourceConfig {\n" +
            "   methods:[\"GET\"],\n" +
            "       path:\"/goma\"\n" +
            "   }\n" +
            "   resource function sayHello (http:Caller outboundEP, http:Request request) {\n" +
            "       http:Response response = new;\n" +
            "       response.setStringPayload(\"Hello World!!!\");\n" +
            "       _ = outboundEP -> respond(response);\n" +
            "   }\n" +
            "\n" +
            "   @http:ResourceConfig {\n" +
            "   methods:[\"PUT\"],\n" +
            "       path:\"/ela\"\n" +
            "   }\n" +
            "   resource function sayHelloPut (http:Caller outboundEP, http:Request request) {\n" +
            "       http:Response response = new;\n" +
            "       response.setStringPayload(\"Hello World!!!\");\n" +
            "       _ = outboundEP -> respond(response);\n" +
            "   }\n" +
            "\n" +
            "@http:ResourceConfig {\n" +
            "   methods:[\"POST\"],\n" +
            "       path:\"/ela\"\n" +
            "   }\n" +
            "   resource function sayHelloPost (http:Caller outboundEP, http:Request request) {\n" +
            "       http:Response response = new;\n" +
            "       response.setStringPayload(\"Hello World!!!\");\n" +
            "       _ = outboundEP -> respond(response);\n" +
            "   }\n" +
            "}";

    @Test(enabled = false, description = "Test ballerina to openApi conversion")
    public void testBallerinaToOpenApiConversion() {
        String serviceName = "";
        try {
            String openApiDefinition = OpenApiConverterUtils.generateOpenApiDefinitions(
                    serviceWithMultipleHTTPMethodsInResourceLevel, serviceName);
            Assert.assertNotNull(openApiDefinition);
        } catch (IOException e) {
            Assert.fail("Error while converting ballerina service to openApi definition");
        }
    }


    @Test(description = "Test OAS definition generation from ballerina service which contains multiple HTTP methods" +
            "defined in same resource definition inline")
    public void testOASGenerationWithMultipleHTTPMethodsInResourceLevel() {
        String serviceName = "";
        try {
            String openApiDefinition = OpenApiConverterUtils.generateOAS3Definitions(
                    serviceWithMultipleHTTPMethodsInResourceLevel, serviceName);
            Assert.assertNotNull(openApiDefinition);
        } catch (OpenApiConverterException e) {
            Assert.fail("Error while converting ballerina service to openApi definition");
        }
    }


    @Test(description = "Test OAS definition generation from ballerina service which do not have any HTTP methods" +
            "defined at resource level")
    public void testOASGenerationWithNoHTTPMethodsAtResourceLevel() {
        String serviceName = "hello";
        try {
            String openApiDefinition = OpenApiConverterUtils.generateOAS3Definitions(
                    serviceWithNoHTTPMethodsAtResourceLevel, serviceName);
            Assert.assertNotNull(openApiDefinition);
        } catch (OpenApiConverterException e) {
            Assert.fail("Error while converting ballerina service to openApi definition");
        }
    }

    @Test(description = "Test OAS definition generation from ballerina service with resource which contains only" +
            "one http method defined")
    public void testOASGenerationWithSingleHTTPVerbResource() {
        String serviceName = "hello";
        try {
            String openApiDefinition = OpenApiConverterUtils.generateOAS3Definitions(serviceWithOneHTTPMethod,
                    serviceName);
            Assert.assertNotNull(openApiDefinition);
        } catch (OpenApiConverterException e) {
            Assert.fail("Error while converting ballerina service to openApi definition");
        }
    }

    @Test(description = "Test OAS definition generation from ballerina service which contains 2 resource which " +
            "contains only one http method in resource level")
    public void testOASGenerationWithTwoResource() {
        String serviceName = "hello";
        try {
            String openApiDefinition = OpenApiConverterUtils.generateOAS3Definitions(
                    sampleOpenApiWithTwoResources, serviceName);
            Assert.assertNotNull(openApiDefinition);
        } catch (OpenApiConverterException e) {
            Assert.fail("Error while converting ballerina service to openApi definition");
        }
    }

    @Test(description = "Test OAS and OpenApi definition generation from ballerina service with multiple resource" +
            " which contains more than http method defined")
    public void testWithTwoResourceWithMultipleVerbs() {
        String serviceName = "hello";
        try {
            String openAPI3Definition = OpenApiConverterUtils.generateOAS3Definitions(
                    sampleOpenApiWithMultipleResourceAndVerbs, serviceName);
            String openApiDefinition = OpenApiConverterUtils.generateOpenApiDefinitions(
                    sampleOpenApiWithMultipleResourceAndVerbs, serviceName);
            Assert.assertNotNull(openApiDefinition);
            Assert.assertNotNull(openAPI3Definition);
        } catch (IOException e) {
            Assert.fail("Error while converting ballerina service to openApi definition with empty service name");
        } catch (OpenApiConverterException e) {
            Assert.fail("Error while converting ballerina service to openApi definition");
        }
    }

    @Test(description = "Test OAS and OpenApi definition generation from ballerina echo service")
    public void testWithEchoService() {
        try {
            String openAPI3Definition = OpenApiConverterUtils.generateOAS3Definitions(
                    echoServiceBal, null);
            String openApiDefinition = OpenApiConverterUtils.generateOpenApiDefinitions(
                    echoServiceBal, null);
            Assert.assertNotNull(openApiDefinition);
            Assert.assertNotNull(openAPI3Definition);
        } catch (IOException e) {
            Assert.fail("Error while converting ballerina echo service to openApi definition");
        } catch (OpenApiConverterException e) {
            Assert.fail("Error while converting ballerina service to openApi definition");
        }
    }

    @Test(description = "Test OAS definition generation when input ballerina service is invalid", enabled = false)
    public void testServiceWithTypo() {
        try {
            String oasDef = OpenApiConverterUtils.generateOAS3Definitions(invalidEchoServiceBal, null);
            Assert.fail("Error was not thrown for invalid ballerina service");
        } catch (OpenApiConverterException e) {
            Assert.assertEquals(e.getMessage(), "Please check if input source is valid and complete");
        }
    }
}

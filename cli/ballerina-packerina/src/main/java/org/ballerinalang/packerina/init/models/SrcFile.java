/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.init.models;

/**
 * Model class for a source file.
 */
public class SrcFile {

    private static final String SERVICE_CONTENT = "// A system package containing protocol access constructs\n" +
                                                  "// Package objects referenced with 'http:' in code\n" +
                                                  "import ballerina/http;\n" +
                                                  "\n" +
                                                  "documentation {\n" +
                                                  "   A service endpoint represents a listener.\n" +
                                                  "}\n" +
                                                  "endpoint http:Listener listener {\n" +
                                                  "    port:9090\n" +
                                                  "};\n" +
                                                  "\n" +
                                                  "documentation {\n" +
                                                  "   A service is a network-accessible API\n" +
                                                  "   Advertised on '/hello', port comes from listener endpoint\n" +
                                                  "}\n" +
                                                  "service<http:Service> hello bind listener {\n" +
                                                  "\n" +
                                                  "    documentation {\n" +
                                                  "       A resource is an invokable API method\n" +
                                                  "       Accessible at '/hello/sayHello\n" +
                                                  "       'caller' is the client invoking this resource \n" +
                                                  "\n" +
                                                  "       P{{caller}} Server Connector\n" +
                                                  "       P{{request}} Request\n" +
                                                  "    }\n" +
                                                  "    sayHello (endpoint caller, http:Request request) {\n" +
                                                  "\n" +
                                                  "        // Create object to carry data back to caller\n" +
                                                  "        http:Response response = new;\n" +
                                                  "\n" +
                                                  "        // Objects and structs can have function calls\n" +
                                                  "        response.setTextPayload(\"Hello Ballerina!\\n\");\n" +
                                                  "\n" +
                                                  "        // Send a response back to caller\n" +
                                                  "        // Errors are ignored with '_'\n" +
                                                  "        // -> indicates a synchronous network-bound call\n" +
                                                  "        _ = caller -> respond(response);\n" +
                                                  "    }\n" +
                                                  "}";

    private static final String MAIN_FUNCTION_CONTENT = "import ballerina/io;\n" +
                                                        "\n" +
                                                        "documentation {\n" +
                                                        "   Prints `Hello World`.\n" +
                                                        "}\n" +
                                                        "function main(string... args) {\n" +
                                                        "    io:println(\"Hello World!\");\n" +
                                                        "}\n";

    private static final String MAIN_FUNCTION_TEST_CONTENT = "import ballerina/test;\n" +
                                                             "import ballerina/io;\n" +
                                                             "\n" +
                                                             "documentation {\n" +
                                                             "   Before Suite Function\n" +
                                                             "}\n" +
                                                             "@test:BeforeSuite\n" +
                                                             "function beforeSuiteFunc () {\n" +
                                                             "    io:println(\"I'm the before suite function!\");\n" +
                                                             "}\n" +
                                                             "\n" +
                                                             "documentation {\n" +
                                                             "   Before test function\n" +
                                                             "}\n" +
                                                             "function beforeFunc () {\n" +
                                                             "    io:println(\"I'm the before function!\");\n" +
                                                             "}\n" +
                                                             "\n" +
                                                             "documentation {\n" +
                                                             "   Test function\n" +
                                                             "}\n" +
                                                             "@test:Config{\n" +
                                                             "    before:\"beforeFunc\",\n" +
                                                             "    after:\"afterFunc\"\n" +
                                                             "}\n" +
                                                             "function testFunction () {\n" +
                                                             "    io:println(\"I'm in test function!\");\n" +
                                                             "    test:assertTrue(true , msg = \"Failed!\");\n" +
                                                             "}\n" +
                                                             "\n" +
                                                             "documentation {\n" +
                                                             "   After test function\n" +
                                                             "}\n" +
                                                             "function afterFunc () {\n" +
                                                             "    io:println(\"I'm the after function!\");\n" +
                                                             "}\n" +
                                                             "\n" +
                                                             "documentation {\n" +
                                                             "   After Suite Function\n" +
                                                             "}\n" +
                                                             "@test:AfterSuite\n" +
                                                             "function afterSuiteFunc () {\n" +
                                                             "    io:println(\"I'm the after suite function!\");\n" +
                                                             "}";

    private static final String SERVICE_TEST_CONTENT = "import ballerina/test;\n" +
                                                       "import ballerina/io;\n" +
                                                       "\n" +
                                                        "documentation {\n" +
                                                        "   Before Suite Function\n" +
                                                        "}\n" +
                                                       "@test:BeforeSuite\n" +
                                                       "function beforeSuiteServiceFunc () {\n" +
                                                       "    io:println(\"I'm the before suite service function!\");\n" +
                                                       "}\n" +
                                                       "\n" +
                                                       "documentation {\n" +
                                                       "   Test function\n" +
                                                       "}\n" +
                                                       "@test:Config\n" +
                                                       "function testServiceFunction () {\n" +
                                                       "    io:println(\"Do your service Tests!\");\n" +
                                                       "    test:assertTrue(true , msg = \"Failed!\");\n" +
                                                       "}\n" +
                                                       "\n" +
                                                       "documentation {\n" +
                                                       "   After Suite Function\n" +
                                                       "}\n" +
                                                       "@test:AfterSuite\n" +
                                                       "function afterSuiteServiceFunc () {\n" +
                                                       "    io:println(\"I'm the after suite service function!\");\n" +
                                                       "}";

    private FileType srcFileType;
    private String content;
    private String testFileContent;
    private String name;
    private String testFileName;
    public SrcFile(String name, FileType fileType) {
        this.srcFileType = fileType;
        this.name = name;
        switch (fileType) {
            case SERVICE:
                content = SERVICE_CONTENT;
                testFileContent = SERVICE_TEST_CONTENT;
                testFileName = "hello_service_test.bal";
                break;
            case MAIN:
            default:
                content = MAIN_FUNCTION_CONTENT;
                testFileContent = MAIN_FUNCTION_TEST_CONTENT;
                testFileName = "main_test.bal";
                break;
        }
    }
    
    public FileType getSrcFileType() {
        return srcFileType;
    }
    
    public String getContent() {
        return content;
    }

    public String getTestContent() {
        return testFileContent;
    }
    
    public String getName() {
        return name;
    }

    /**
     * Returns the name of the test file according to the type.
     * @return name of the test file
     */
    public String getTestFileName() {
        return testFileName;
    }
}

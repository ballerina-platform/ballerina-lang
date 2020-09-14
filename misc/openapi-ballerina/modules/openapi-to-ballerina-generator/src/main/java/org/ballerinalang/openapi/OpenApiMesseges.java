/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.openapi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class contains the messages constants required for OpenApi tool.
 */
public class OpenApiMesseges {

    public static final String CLIENT_MANDATORY = "Client name is mandatory to generate the ballerina client. " +
            "\nE.g. ballerina openapi gen-client [<module>]:<servicename> <openapicontract>";
    public static final String OPENAPI_FILE_MANDATORY = "An OpenApi definition file is required to " +
            "generate the client. \nE.g: ballerina openapi gen-client [<module>]:<servicename> <OpenApiContract>";
    public static final String OPENAPI_CLIENT_EXCEPTION = "Error occurred when generating client for openapi contract";


    public static final String CONTRACT_SERVICE_MANDATORY = "A service name is mandatory to generate an OpenApi " +
            "contract. \nballerina openapi gen-contract [moduleName]:serviceName [-i: ballerinaFile]";
    public static final String CONTRACT_BALLERINA_DOC_MANDATORY = "Please specify a ballerina document path in" +
            " order generate an OpenApi contract for the service \nballerina openapi gen-contract " +
            "[moduleName]:serviceName [-i: ballerinaFile]";

    public static final String GEN_CLIENT_PROJECT_ROOT = "Output path is not a valid ballerina project directory." +
            "\nUse 'ballerina new' to generate a new project";
    public static final String GEN_SERVICE_MODULE_ARGS_REQUIRED = "A module name and a service name is required " +
            "in order to generate the ballerina service for the provided OpenApi contract. \nE.g. ballerina " +
            "openapi gen-service <module_name>:<service_name> <openapi_contract>";
    public static final String GEN_SERVICE_MODULE_REQUIRED = "A module name is required in order to generate the " +
            "ballerina service for the provided OpenApi contract. \nE.g. ballerina openapi gen-service " +
            "<module_name>:<service_name> <openapi_contract>";
    public static final String GEN_SERVICE_SERVICE_NAME_REQUIRED = "A service name is required in order to generate " +
            "the ballerina service for the provided OpenApi contract. \nE.g. ballerina openapi gen-service " +
            "<module_name>:<service_name> <openapi_contract>";
    public static final String GEN_SERVICE_PROJECT_ROOT = "Ballerina service generation should be done from the " +
            "project root. \nIf you like to start with a new project " +
            "use `ballerina new` command to create a new project.";
    public static final String MODULE_DIRECTORY_EXCEPTION = "Unable to create module directory. File system error " +
            "occured.";
    public static final String RESOURCE_DIRECTORY_EXCEPTION = "Unable to create resource directory. File system error" +
            " occured.";
    public static final String TESTS_DIRECTORY_EXCEPTION = "Unable to create tests directory. File system error " +
            "occured";
    public static final String SOURCE_DIRECTORY_EXCEPTION = "Unable to create source directory. File system error " +
            "occured.";
    public static final String MODULE_MD_EXCEPTION = "Unable to create moudle.md file. File system error occured.";
    public static final String DEFINITION_EXISTS = "There is already an OpenApi contract in the location.";
    public static final String EXPERIMENTAL_FEATURE = "Note: This is an experimental tool, which only" +
            " supports a limited set of functionality.";
    public static final String MESSAGE_FOR_MISSING_INPUT = "An OpenApi definition file is required to generate the " +
            "service. \nE.g: ballerina openapi --input <OpenApiContract> or <Ballerina file>";
    public static final String MESSAGE_FOR_INVALID_MODULE = "The module provided is not found in the " +
            "current location.";

    //TODO Update keywords if Ballerina Grammer changes
    private static final String[] KEYWORDS = new String[]{"abort", "aborted", "abstract", "all", "annotation",
            "any", "anydata", "boolean", "break", "byte", "catch", "channel", "check", "checkpanic", "client",
            "committed", "const", "continue", "decimal", "else", "error", "external", "fail", "final", "finally",
            "float", "flush", "fork", "function", "future", "handle", "if", "import", "in", "int", "is", "join",
            "json", "listener", "lock", "match", "new", "object", "OBJECT_INIT", "onretry", "parameter", "panic",
            "private", "public", "record", "remote", "resource", "retries", "retry", "return", "returns", "service",
            "source", "start", "stream", "string", "table", "transaction", "try", "type", "typedesc", "typeof",
            "trap", "throw", "wait", "while", "with", "worker", "var", "version", "xml", "xmlns", "BOOLEAN_LITERAL",
            "NULL_LITERAL", "ascending", "descending", "foreach", "map", "group", "from", "default", "field"};
    
    private static final String[] TYPES = new String[]{"int", "any", "anydata", "boolean", "byte", "float", "int",
                                                       "json", "string", "table", "var", "xml"};

    public static final List<String> BAL_KEYWORDS;
    public static final List<String> BAL_TYPES;

    static {
        BAL_KEYWORDS = Collections.unmodifiableList(Arrays.asList(KEYWORDS));
        BAL_TYPES = Collections.unmodifiableList(Arrays.asList(TYPES));
    }

    private OpenApiMesseges() {
        throw new AssertionError();
    }

}

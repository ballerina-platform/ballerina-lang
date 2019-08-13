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

/**
 * This class contains the messages constants required for OpenApi tool.
 */
public class OpenApiMesseges {

    public static final String CLIENT_MANDATORY = "Client name is mandatory to generate the ballerina client. " +
            "\nE.g. ballerina openapi gen-client [<module>]:<servicename> <openapicontract>";
    public static final String OPENAPI_FILE_MANDATORY = "An OpenApi definition file is required to " +
            "generate the client. \nE.g: ballerina openapi gen-client [<module>]:<servicename> <OpenApiContract>";
    public static final String OPENAPI_CLIENT_EXCEPTION = "Error occurred when generating client for openapi contract";


    public static final String CONTRACT_SERVICE_MANDATORY = "A service name is mandatory to generate an " +
            "OpenApi contract.";
    public static final String CONTRACT_BALLERINA_DOC_MANDATORY = "Please specify a ballerina document path in " +
            "order generate an OpenApi contract for the service ";

    private OpenApiMesseges() {
        throw new AssertionError();
    }

}

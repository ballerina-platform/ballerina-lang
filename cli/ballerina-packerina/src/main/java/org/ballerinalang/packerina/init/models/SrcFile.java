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

import java.util.Locale;

/**
 * Model class for a source file.
 */
public class SrcFile {
    private static final String SERVICE_CONTENT = "package %s;%n" +
                                                  "import ballerina/net.http;%n" +
                                                  "%n" +
                                                  "%n" +
                                                  "endpoint http:ServiceEndpoint serviceEndpoint {%n" +
                                                  "   port:9090%n" +
                                                  "};%n" +
                                                  "%n" +
                                                  "service<http:Service> service1 bind serviceEndpoint {%n" +
                                                  "    @http:ResourceConfig {%n" +
                                                  "        methods: [\"GET\"],%n" +
                                                  "        path: \"/\"%n" +
                                                  "    }%n" +
                                                  "    echo1 (endpoint outboundEP, http:Request req) {%n" +
                                                  "         http:Response response = {};%n" +
                                                  "         response.setStringPayload(\"Hello World!\");%n" +
                                                  "         _ = outboundEP -> respond(response);%n" +
                                                  "    }%n" +
                                                  "}%n";
    
    private static final String MAIN_FUNCTION_CONTENT = "import ballerina/io;\n" +
                                                        "function main(string[] args) {\n" +
                                                        "    io:println(\"Hello World!\");\n" +
                                                        "}\n";
    private SrcFileType srcFileType;
    private String content;
    private String name;
    public SrcFile(String name, SrcFileType fileType) {
        this.srcFileType = fileType;
        this.name = name;
        switch (fileType) {
            case SERVICE:
                content = String.format(SERVICE_CONTENT, this.name);
                break;
            case MAIN:
            default:
                if (!this.name.toLowerCase(Locale.getDefault()).endsWith(".bal")) {
                    this.name = this.name + ".bal";
                }
                content = MAIN_FUNCTION_CONTENT;
                break;
        }
    }
    
    public SrcFileType getSrcFileType() {
        return srcFileType;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Enum for the source file type.
     */
    public enum SrcFileType {
        SERVICE,
        MAIN
    }
}


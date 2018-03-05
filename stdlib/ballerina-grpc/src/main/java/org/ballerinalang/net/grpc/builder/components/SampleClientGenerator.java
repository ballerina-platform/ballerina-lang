/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.builder.components;

import static org.ballerinalang.net.grpc.builder.BalGenConstants.DEFAULT_SAMPLE_CONNECTOR_PORT;
import static org.ballerinalang.net.grpc.builder.BalGenConstants.NEW_LINE_CHARACTER;

/**
 * Generate the sample client to user.
 */
public class SampleClientGenerator {
    private String packageName;
    private String serviceName;
    private boolean generateBlocking;
    private boolean generateNonBlocking;
    
    public SampleClientGenerator(String packageName, String serviceName, boolean generateBlocking,
                                 boolean generateNonBlocking) {
        this.packageName = packageName;
        this.serviceName = serviceName;
        this.generateBlocking = generateBlocking;
        this.generateNonBlocking = generateNonBlocking;
    }
    
    public String build() {
        StringBuilder endpoints = new StringBuilder(NEW_LINE_CHARACTER);
        if (generateBlocking) {
            endpoints.append(getStub("BlockingStub")).append(NEW_LINE_CHARACTER);
        }
        if (generateNonBlocking) {
            endpoints.append(getStub("NonBlockingStub")).append(NEW_LINE_CHARACTER);
        }
        String str =
                "package %s;" + NEW_LINE_CHARACTER +
                        NEW_LINE_CHARACTER +
                        "function main (string[] args) {" + NEW_LINE_CHARACTER +
                        "    %s" +
                        "}" + NEW_LINE_CHARACTER;
        return String.format(str, packageName, endpoints.toString());
    }
    
    private String getStub(String stubName) {
        String str =
                "    endpoint<%s%s> %sStub%s {" + NEW_LINE_CHARACTER +
                "        create %s%s(\"localhost\", " + DEFAULT_SAMPLE_CONNECTOR_PORT + ");" + NEW_LINE_CHARACTER +
                "    }" + NEW_LINE_CHARACTER;
        return String.format(str, this.serviceName, stubName, this.serviceName, stubName
                .replace("Stub", ""), this.serviceName, stubName);
    }
    
}

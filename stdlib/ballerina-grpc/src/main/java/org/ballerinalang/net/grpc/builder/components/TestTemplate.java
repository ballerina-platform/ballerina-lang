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

import java.io.IOException;

import static org.ballerinalang.net.grpc.builder.components.BalGenerationUtils.DEFAULT_SKELETON_DIR;
import static org.ballerinalang.net.grpc.builder.components.BalGenerationUtils.SKELETON_TEMPLATE_NAME;
import static org.ballerinalang.net.grpc.builder.components.BalGenerationUtils.writeBallerina;

/**
 * .
 */
public class TestTemplate {
    
    public static void main(String[] args) throws IOException {
        ClientStubBal context = new ClientStubBal("client");
        String[] types = {"string", "int"};
        String[] names = {"name", "age"};
        context.addStruct("yasara", names, types);
        context.addStruct("chathura", names, types);
        Connector connector = new Connector("HellowWorld", "blocking");
        connector.addBlockingAction("hello", "string", "int",
                "hello/hello");
        connector.addNonBlockingAction("hello", "string", "hello/hello");
        context.addConnector(connector);
        writeBallerina(context, DEFAULT_SKELETON_DIR,
                SKELETON_TEMPLATE_NAME, "/home/yasara/Desktop/testTemplate.bal");
    }
}

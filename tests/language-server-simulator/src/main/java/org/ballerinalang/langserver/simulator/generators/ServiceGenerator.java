/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.simulator.generators;

import org.ballerinalang.annotation.JavaSPIService;

/**
 * Service code snippet generator.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.simulator.generators.CodeSnippetGenerator")
public class ServiceGenerator extends CodeSnippetGenerator {

    @Override
    public String generate() {
        return generateRandomService();
    }

    @Override
    public Generators.Type type() {
        return Generators.Type.SERVICE;
    }

    public String generateRandomService() {
        return "\nservice /context1 on new http:Listener(8080) {\n" +
                "    resource function get path1(http:Caller caller, http:Request req) {\n" +
                "        \n" +
                "    }\n" +
                "}\n";
    }
}

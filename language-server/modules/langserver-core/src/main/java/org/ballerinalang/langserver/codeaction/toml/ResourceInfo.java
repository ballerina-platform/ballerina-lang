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
package org.ballerinalang.langserver.codeaction.toml;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;

/**
 * Represents Resource of a service in a ballerina document.
 *
 * @since 2.0.0
 */
public class ResourceInfo {
    private FunctionDefinitionNode node;
    private String httpMethod;
    private String path;

    public ResourceInfo(FunctionDefinitionNode node, String httpMethod, String path) {
        this.node = node;
        this.httpMethod = httpMethod;
        this.path = path;
    }

    public FunctionDefinitionNode getNode() {
        return node;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "ResourceInfo{" +
                ", httpMethod='" + httpMethod + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}

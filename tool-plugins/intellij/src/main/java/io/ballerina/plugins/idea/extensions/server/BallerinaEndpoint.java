/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.plugins.idea.extensions.server;

/**
 * Represents an Ballerina Endpoint.
 */
public class BallerinaEndpoint {
    public String name;
    public String packageName;
    private String orgName;
    private String fileName;

    public BallerinaEndpoint(String name) {
        this.name = name;
    }

    public BallerinaEndpoint(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    public BallerinaEndpoint(String name, String packageName, String orgName) {
        this.name = name;
        this.packageName = packageName;
        this.orgName = orgName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

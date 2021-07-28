/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.handlers.help;

/**
 * Helper class to Read BBE Records.
 */
public class BbeRecord {

    private String name;
    private String url;
    private String verifyBuild;
    private String verifyOutput;
    private String isLearnByExample;

    public BbeRecord(String name, String url, String verifyBuild, String verifyOutput, String isLearnByExample) {
        this.name = name;
        this.url = url;
        this.verifyBuild = verifyBuild;
        this.verifyOutput = verifyOutput;
        this.isLearnByExample = isLearnByExample;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getVerifyBuild() {
        return verifyBuild;
    }

    public String getVerifyOutput() {
        return verifyOutput;
    }

    public String getIsLearnByExample() {
        return isLearnByExample;
    }
}

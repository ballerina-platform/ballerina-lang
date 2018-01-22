/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.composer.service.ballerina.parser.service.model;

/**
 * Wraps BFile as String.
 */
public class BFile {

    private String content = "";

    private String fileName = "untitled.bal";

    private String filePath = "temp";

    private String packageName = "";

    private boolean includeTree = false;

    private boolean includePackageInfo = false;

    private boolean includeProgramDir = false;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean needTree() {
        return includeTree;
    }

    public void setIncludeTree(boolean includeTree) {
        this.includeTree = includeTree;
    }

    public boolean needPackageInfo() {
        return includePackageInfo;
    }

    public void setIncludePackageInfo(boolean includePackageInfo) {
        this.includePackageInfo = includePackageInfo;
    }

    public boolean needProgramDir() {
        return includeProgramDir;
    }

    public void setIncludeProgramDir(boolean includeProgramDir) {
        this.includeProgramDir = includeProgramDir;
    }
}

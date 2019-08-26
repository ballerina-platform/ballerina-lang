/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.openapi.cmd;

import java.nio.file.Path;

/**
 * Container to hold ballerina project structure.
 */
public class OpenAPIBallerinaProject {
    private Path balProjectPath;
    private Path srcPath;
    private Path implPath;

    OpenAPIBallerinaProject() {
        this.balProjectPath = null;
        this.srcPath = null;
        this.implPath = null;
    }

    public OpenAPIBallerinaProject(Path balProjectPath, Path srcPath, Path implPath, Path resourcePath) {
        this.balProjectPath = balProjectPath;
        this.srcPath = srcPath;
        this.implPath = implPath;
    }

    public Path getBalProjectPath() {
        return balProjectPath;
    }

    void setBalProjectPath(Path balProjectPath) {
        this.balProjectPath = balProjectPath;
    }

    public Path getSrcPath() {
        return srcPath;
    }

    void setSrcPath(Path srcPath) {
        this.srcPath = srcPath;
    }

    public Path getImplPath() {
        return implPath;
    }

    void setImplPath(Path implPath) {
        this.implPath = implPath;
    }

    public Path getResourcePath() {
        if (this.implPath != null) {
            return this.implPath.resolve("resources");
        }
        return null;
    }
}

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
package org.ballerinalang.langserver.extensions.ballerina.packages;

import java.util.ArrayList;
import java.util.List;

/**
 * Object that holds package data.
 */
public class PackageObject {
    private final String name;
    private final String filePath;

    private List<ModuleObject> modules = new ArrayList<>();

    protected PackageObject(String name, String filePath) {
        this.name = name;
        this.filePath = filePath;
    }

    protected void addModule(ModuleObject moduleObject) {
        this.modules.add(moduleObject);
    }

    protected String getName() {
        return name;
    }

    protected String getFilePath() {
        return filePath;
    }
}

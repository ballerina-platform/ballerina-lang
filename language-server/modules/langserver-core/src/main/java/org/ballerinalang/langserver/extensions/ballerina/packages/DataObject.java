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
 * Object that holds the component data.
 */
public class DataObject {
    private final String name;
    private final String filePath;
    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;

    private List<DataObject> functions;
    private List<DataObject> resources;

    protected DataObject(String name, String filePath, int startLine, int startColumn, int endLine, int endColumn) {
        this.name = name;
        this.filePath = filePath;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    protected void addFunction(DataObject dataObject) {
        if (this.functions == null) {
            this.functions = new ArrayList<>();
        }
        this.functions.add(dataObject);
    }

    protected void addResource(DataObject dataObject) {
        if (this.resources == null) {
            this.resources = new ArrayList<>();
        }
        this.resources.add(dataObject);
    }

    protected String getName() {
        return name;
    }

    protected String getFilePath() {
        return filePath;
    }

    protected int getStartLine() {
        return startLine;
    }

    protected int getStartColumn() {
        return startColumn;
    }

    protected int getEndLine() {
        return endLine;
    }

    protected int getEndColumn() {
        return endColumn;
    }
}

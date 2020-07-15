/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.docgen.generator.model;

/**
 * Represents a page context of api docs.
 */
public class PageContext {
    public String rootPath;
    public String title;
    public boolean excludeIndex;

    public PageContext(String rootPath, String title, boolean excludeIndex) {
        this.rootPath = rootPath;
        this.title = title;
        this.excludeIndex = excludeIndex;
    }
}

/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects.internal;

/**
 * {@code ModuleFileData} represents a Ballerina source file (.bal).
 *
 * @since 2.0.0
 */
public class DocumentData {
    //TODO: Remove this class and use DocumentConfig for creating a document
    private final String name;
    private String content;

    private DocumentData(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public static DocumentData from(String name, String content) {
        return new DocumentData(name, content);
    }

    public String content() {
        return content;
    }

    public String name() {
        return name;
    }
}

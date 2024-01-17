/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.projects;

import java.nio.file.Path;

/**
 * {@code ResourceConfig} contains necessary configuration elements required
 * to create an instance of a {@code Resource}.
 *
 * @since 2.0.0
 */
public class ResourceConfig {

    private final DocumentId documentId;
    private final Path path;
    private final String name;
    private final byte[] content;

    protected ResourceConfig(DocumentId documentId, Path path, String name, byte[] content) {
        this.documentId = documentId;
        this.path = path;
        this.name = name;
        this.content = content;
    }

    /**
     * Create a resource configuration to be added to a module.
     *
     * @param documentId documentId to uniquely identify a resource
     * @param name       proposed filename of the resource.
     *                   The name can be a file name or a relative path with UNIX separators
     *                   (e.g. config/default.conf).
     * @param content    resource content as a byte array
     *
     * @return ResourceConfig instance
     */
    public static ResourceConfig from(DocumentId documentId, String name, byte[] content) {
        return new ResourceConfig(documentId, null, name, content);
    }

    public DocumentId documentId() {
        return documentId;
    }

    public String name() {
        return name;
    }

    byte[] content() {
        return content;
    }

    Path path() {
        return path;
    }
}

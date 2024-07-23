/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * A wrapper class for keeping code generated binary content and metadata of a program jar file.
 *
 * @since 2.0.0
 */
public class CompiledJarFile {

    private JAREntries jarEntries;

    public CompiledJarFile(JAREntries jarEntries, Map<String, byte[]> resources) {
        this.jarEntries = jarEntries;
        for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
            jarEntries.putJarArchiveEntry(entry.getKey(), entry.getValue());
        }
        jarEntries.end();
    }

    public ByteArrayOutputStream toByteArrayStream() throws IOException {
        return jarEntries.getByteArrayOutputStream();
    }
}

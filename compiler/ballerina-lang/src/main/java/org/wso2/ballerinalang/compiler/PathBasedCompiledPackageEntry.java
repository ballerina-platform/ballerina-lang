/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler;

import org.ballerinalang.repository.CompilerOutputEntry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@code PathBasedCompiledPackageEntry} represents compiled package entry.
 *
 * @since 0.970.0
 */
public class PathBasedCompiledPackageEntry implements CompilerOutputEntry {

    private Path path;
    private Kind kind;

    public PathBasedCompiledPackageEntry(Path path, Kind kind) {
        this.path = path;
        this.kind = kind;
    }

    @Override
    public String getEntryName() {
        return this.path.toString();
    }

    @Override
    public Kind getEntryKind() {
        return this.kind;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(this.path);
    }
}

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
package io.ballerina.projects.repos;

import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@code CompilationCache} instance that caches artifacts in a temp directory.
 *
 * @since 2.0.0
 */
public class TempDirCompilationCache extends FileSystemCache {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private TempDirCompilationCache(Project project, Path cacheDirPath) {
        super(project, cacheDirPath);
    }

    public static TempDirCompilationCache from(Project project) {
        Path targetPath = createTempProjectRoot();
        return new TempDirCompilationCache(project, targetPath);
    }

    @Override
    public byte[] getBir(ModuleName moduleName) {
        // Do not return the cached BIR in the target directory
        // We can improve this implementation to cache the BIR later to enable incremental compilation.
        return EMPTY_BYTE_ARRAY;
    }

    private static Path createTempProjectRoot() {
        try {
            return Files.createTempDirectory("ballerina-compilation-cache" + System.nanoTime());
        } catch (IOException e) {
            throw new RuntimeException("Error while creating a temp directory.", e);
        }
    }
}

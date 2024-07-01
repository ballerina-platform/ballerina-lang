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
package io.ballerina.projects;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Represents the compilation cache of a single Package.
 *
 * @since 2.0.0
 */
public abstract class CompilationCache {
    protected final Project project;

    public CompilationCache(Project project) {
        this.project = project;
    }

    public abstract byte[] getBir(ModuleName moduleName);

    public abstract void cacheBir(ModuleName moduleName, ByteArrayOutputStream birContent);

    public abstract Optional<Path> getPlatformSpecificLibrary(CompilerBackend compilerBackend, String libraryName,
                                                              boolean isOptimizedLibrary);

    public abstract void cachePlatformSpecificLibrary(CompilerBackend compilerBackend, String libraryName,
                                                      ByteArrayOutputStream libraryContent, boolean isOptimizedLibrary);
}

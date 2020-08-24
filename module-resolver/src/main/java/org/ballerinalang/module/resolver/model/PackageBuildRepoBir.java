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

package org.ballerinalang.module.resolver.model;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.module.resolver.exceptions.ModuleResolveException;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageBinary;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BIR_EXT;

/**
 * Concrete package entry for BIR file.
 */
public class PackageBuildRepoBir implements PackageBinary {
    private final PackageID moduleId;
    private final CompilerInput birContent;
    private final Path sourcePath;

    public PackageBuildRepoBir(PackageID moduleId, Path sourcePath) {
        this.moduleId = moduleId;
        this.sourcePath = sourcePath;
        this.birContent = getBirContent();
    }

    private CompilerInput getBirContent() {
        try (FileSystem zipFileSystem = FileSystems
                .newFileSystem(URI.create("jar:" + this.sourcePath.toUri()), new HashMap<>())) {
            Path birPath = zipFileSystem.getPath("bir", moduleId.getName().getValue() + BLANG_COMPILED_PKG_BIR_EXT);
            byte[] code = Files.readAllBytes(birPath);
            return new CompilerInputImpl(code, birPath);
        } catch (IOException e) {
            throw new ModuleResolveException("reading bir from build repo failed");
        }
    }

    @Override
    public CompilerInput getCompilerInput() {
        return this.birContent;
    }

    @Override
    public PackageID getPackageId() {
        return this.moduleId;
    }

    @Override
    public Kind getKind() {
        return Kind.COMPILED_BIR;
    }

    @Override
    public String getName() {
        return moduleId.getName().getValue();
    }

    @Override
    public RepoHierarchy getRepoHierarchy() {
        throw new UnsupportedOperationException();
    }
}

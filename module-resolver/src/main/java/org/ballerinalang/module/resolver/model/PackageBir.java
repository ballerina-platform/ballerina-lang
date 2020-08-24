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
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Concrete package entry for BIR file.
 */
public class PackageBir implements PackageBinary {
    private final PackageID moduleId;
    private final CompilerInput birContent;
    private final Path sourcePath;

    public PackageBir(PackageID moduleId, Path sourcePath) {
        this.moduleId = moduleId;
        this.sourcePath = sourcePath;
        this.birContent = getBirContent();
    }

    private CompilerInput getBirContent() {
        try {
            Path birPath = this.sourcePath.resolve(this.moduleId.getName().getValue() + ".bir");
            return new CompilerInputImpl(Files.readAllBytes(birPath), this.sourcePath);
        } catch (IOException e) {
            throw new ModuleResolveException("reading bir failed");
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

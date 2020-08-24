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
import org.ballerinalang.repository.PackageSource;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Concrete package entry for single bal file.
 */
public class SingleBalEntity implements PackageSource {
    private final PackageID moduleId;
    private final List<CompilerInput> sourceFiles;
    private final Path sourcePath;
    private final Path moduleRoot;

    public SingleBalEntity(PackageID moduleId, Path sourcePath) {
        this.moduleId = moduleId;
        this.moduleRoot = sourcePath;
        this.sourcePath = sourcePath.resolve(moduleId.sourceFileName.getValue()); //bal file name append to module root
        this.sourceFiles = Collections.singletonList(getSourceFile());
    }

    private CompilerInput getSourceFile() {
        try {
            return new CompilerInputImpl(Files.readAllBytes(this.sourcePath), this.sourcePath, this.moduleRoot);
        } catch (IOException e) {
            throw new ModuleResolveException("reading source file failed: " + this.sourcePath);
        }
    }

    @Override
    public PackageID getPackageId() {
        return this.moduleId;
    }

    @Override
    public Kind getKind() {
        return Kind.SOURCE;
    }

    @Override
    public String getName() {
        return this.moduleId.getName().getValue();
    }

    @Override
    public RepoHierarchy getRepoHierarchy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getEntryNames() {
        return null;
    }

    @Override
    public CompilerInput getPackageSourceEntry(String name) {
        return null;
    }

    @Override
    public List<CompilerInput> getPackageSourceEntries() {
        return this.sourceFiles;
    }
}

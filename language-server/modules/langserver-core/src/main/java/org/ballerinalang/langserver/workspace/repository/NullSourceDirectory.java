/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.workspace.repository;

import org.ballerinalang.repository.CompiledPackage;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Null source directory.
 */
public class NullSourceDirectory implements SourceDirectory {
    @Override
    public boolean canHandle(Path dirPath) {
        return true;
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public List<String> getSourceFileNames() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getSourcePackageNames() {
        return Collections.emptyList();
    }

    @Override
    public InputStream getManifestContent() {
        return new ByteArrayInputStream("".getBytes());
    }

    @Override
    public InputStream getLockFileContent() {
        return null;
    }

    @Override
    public Path saveCompiledProgram(InputStream source, String fileName) {
        return null;
    }

    @Override
    public void saveCompiledPackage(CompiledPackage compiledPackage, Path dirPath, String fileName) throws IOException {
    }

    @Override
    public Converter<Path> getConverter() {
        return null;
    }

}

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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.repository.CompiledPackage;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File system based project directory implementation.
 *
 * @since 0.965.0
 */
public class FileSystemProgramDirectory implements SourceDirectory {
    private final Path programDirPath;

    public FileSystemProgramDirectory(Path programDirPath) {
        this.programDirPath = programDirPath;
    }

    @Override
    public boolean canHandle(Path dirPath) {
        return true;
    }

    @Override
    public Path getPath() {
        return programDirPath;
    }

    @Override
    public List<String> getSourceFileNames() {
        if (!Files.isDirectory(programDirPath)) {
            return Collections.emptyList();
        }
        try {
            return Files.list(programDirPath)
                    .map(ProjectDirs::getLastComp)
                    .filter(ProjectDirs::isSourceFile)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (SecurityException | AccessDeniedException e) {
            throw new BLangCompilerException("permission denied: " + programDirPath.toString());
        } catch (IOException e) {
            throw new BLangCompilerException("error reading directory: " + programDirPath.toString());
        }
    }

    @Override
    public List<String> getSourcePackageNames() {
        return new ArrayList<>(0);
    }

    @Override
    public InputStream getManifestContent() {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public InputStream getLockFileContent() {
        throw new UnsupportedOperationException("lock file is not available in a program directory");
    }

    @Override
    public Path saveCompiledProgram(InputStream source, String fileName) {
        Path targetFilePath = programDirPath.resolve(fileName);
        try {
            Files.copy(source, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
            return targetFilePath;
        } catch (DirectoryNotEmptyException e) {
            throw new BLangCompilerException("A directory exists with the same name as the file name '" +
                    targetFilePath.toString() + "'");
        } catch (IOException e) {
            // TODO Improve error messages.
            throw new BLangCompilerException("failed to write the compiled program to '" +
                    targetFilePath.toString() + "'");
        }
    }

    @Override
    public void saveCompiledPackage(CompiledPackage compiledPackage,
                                    Path dirPath,
                                    String fileName)  throws IOException {

    }

    @Override
    public Converter<Path> getConverter() {
        return new PathConverter(programDirPath);
    }
}

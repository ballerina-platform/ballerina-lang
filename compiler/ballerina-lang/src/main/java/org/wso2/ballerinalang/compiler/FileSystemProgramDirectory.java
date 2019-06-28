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
import org.ballerinalang.repository.CompilerOutputEntry;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;
import org.wso2.ballerinalang.compiler.util.FileUtils;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.util.LambdaExceptionUtils.rethrow;

/**
 * File system based project directory implementation.
 *
 * @since 0.965.0
 */
public class FileSystemProgramDirectory implements SourceDirectory {
    private final Path programDirPath;
    private static PrintStream outStream = System.out;

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
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public Path saveCompiledProgram(InputStream source, String fileName) {
        // When building a single bal file the executable (balx) should be generated in the current directory of
        // the user
        Path targetFilePath = Paths.get(fileName);
        try {
            outStream.println("    " + fileName);
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
        // Creates the package directory if it doesn't exits
        Files.createDirectories(dirPath);
        Path compiledPkgPath = dirPath.resolve(fileName);
        FileUtils.deleteFile(compiledPkgPath);

        Map<String, String> fsEnv = new HashMap<String, String>() {{
            put("create", "true");
        }};
        URI filepath = compiledPkgPath.toUri();
        URI zipFileURI;
        try {
            zipFileURI = new URI("jar:" + filepath.getScheme(),
                    filepath.getUserInfo(), filepath.getHost(), filepath.getPort(),
                    filepath.getPath() + "!/",
                    filepath.getQuery(), filepath.getFragment());
            try (FileSystem fs = FileSystems.newFileSystem(zipFileURI, fsEnv)) {
                compiledPackage.getAllEntries()
                        .forEach(rethrow(entry -> addCompilerOutputEntry(fs, entry)));
            }
        } catch (URISyntaxException e) {
            throw new BLangCompilerException("error creating artifact: " + compiledPkgPath.getFileName());
        }
    }

    private void addCompilerOutputEntry(FileSystem fs, CompilerOutputEntry outputEntry) throws IOException {
        String rootDirName = getTopLevelDirNameInPackage(outputEntry.getEntryKind(), fs);
        Path rootDirPath = fs.getPath(rootDirName);
        Path destPath = rootDirPath.resolve(outputEntry.getEntryName());

        Path parent = destPath.getParent();
        if (Files.notExists(parent)) {
            Files.createDirectories(parent);
        }

        Files.copy(outputEntry.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Converter<Path> getConverter() {
        return new PathConverter(programDirPath);
    }

    private String getTopLevelDirNameInPackage(CompilerOutputEntry.Kind kind, FileSystem fs) {
        switch (kind) {
            case SRC:
            case BIR:
            case OBJ:
                return kind.getValue();
            case ROOT:
                return fs.getSeparator();

        }
        return null;
    }
}

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
import org.wso2.ballerinalang.compiler.util.FileUtils;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;

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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.ballerinalang.repository.CompilerOutputEntry.Kind;
import static org.wso2.ballerinalang.util.LambdaExceptionUtils.rethrow;

/**
 * File system based project directory implementation.
 *
 * @since 0.965.0
 */
public class FileSystemProjectDirectory extends FileSystemProgramDirectory {
    private final Path projectDirPath;
    private final Path sourceDirPath;
    private List<String> packageNames;
    protected boolean scanned = false;
    private static PrintStream outStream = System.out;

    public FileSystemProjectDirectory(Path projectDirPath) {
        super(projectDirPath);
        // TODO This path expect absolute path. This is validated by the SourceDirectoryManager
        this.projectDirPath = projectDirPath;
        this.sourceDirPath = projectDirPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME);
    }

    @Override
    public boolean canHandle(Path dirPath) {
        return RepoUtils.isBallerinaProject(dirPath);
    }

    @Override
    public Path getPath() {
        return this.projectDirPath;
    }

    @Override
    public List<String> getSourceFileNames() {
        return new ArrayList<>(0);
    }

    @Override
    public List<String> getSourcePackageNames() {
        if (scanned) {
            return this.packageNames;
        }

        try {
            try (Stream<Path> stream = Files.list(sourceDirPath)) {
                this.packageNames = stream.filter(path -> Files.isDirectory(path))
                        .filter(ProjectDirs::containsSourceFiles)
                        .map(ProjectDirs::getLastComp)
                        .map(Path::toString)
                        .toList();
            }
        } catch (SecurityException | AccessDeniedException e) {
            throw new BLangCompilerException("permission denied: " + projectDirPath.toString());
        } catch (IOException e) {
            throw new BLangCompilerException("error reading directory: " + projectDirPath.toString());
        }
        this.scanned = true;
        return this.packageNames;
    }

    @Override
    public InputStream getManifestContent() {
        Path tomlFilePath = projectDirPath.resolve("Ballerina.toml");
        if (Files.exists(tomlFilePath)) {
            try {
                return Files.newInputStream(tomlFilePath);
            } catch (IOException ignore) {
            }
        }
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public InputStream getLockFileContent() {
        Path tomlFilePath = projectDirPath.resolve("Ballerina.lock");
        if (Files.exists(tomlFilePath)) {
            try {
                return Files.newInputStream(tomlFilePath);
            } catch (IOException ignore) {
            }
        }
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public Path saveCompiledProgram(InputStream source, String fileName) {
        Path targetFilePath = ensureAndGetTargetDirPath().resolve(fileName);
        try {
            outStream.println("    ./target/" + fileName);
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
                                    String fileName) throws IOException {

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

    // private methods

    private void createDirectory(Path targetPath) {
        try {
            Files.createDirectory(targetPath);
        } catch (IOException e) {
            throw new BLangCompilerException("failed to create directory '" + targetPath.toString() + "'");
        }
    }

    private Path ensureAndGetTargetDirPath() {
        Path targetPath = this.projectDirPath.resolve(ProjectDirConstants.TARGET_DIR_NAME);
        if (!Files.exists(targetPath)) {
            createDirectory(targetPath);
        }

        return targetPath;
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

    private String getTopLevelDirNameInPackage(Kind kind, FileSystem fs) {
        return switch (kind) {
            case SRC, BIR, OBJ -> kind.getValue();
            case ROOT -> fs.getSeparator();
        };
    }
}

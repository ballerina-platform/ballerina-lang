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
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.exceptions.TomlException;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class is responsible for finding packages with entry points
 * inside the project directory.
 *
 * @since 0.965.0
 */
public class SourceDirectoryManager {
    private static final CompilerContext.Key<SourceDirectoryManager> PROJECT_DIR_KEY =
            new CompilerContext.Key<>();

    private final CompilerOptions options;
    private final Names names;
    private final SourceDirectory sourceDirectory;

    public static SourceDirectoryManager getInstance(CompilerContext context) {
        SourceDirectoryManager sourceDirectoryManager = context.get(PROJECT_DIR_KEY);
        if (sourceDirectoryManager == null) {
            sourceDirectoryManager = new SourceDirectoryManager(context);
        }
        return sourceDirectoryManager;
    }

    private SourceDirectoryManager(CompilerContext context) {
        context.put(PROJECT_DIR_KEY, this);

        this.names = Names.getInstance(context);
        this.options = CompilerOptions.getInstance(context);
        this.sourceDirectory = initializeAndGetSourceDirectory(context);
    }

    public Stream<PackageID> listSourceFilesAndPackages() {
        List<String> sourceFileNames = this.sourceDirectory.getSourceFileNames();
        List<String> packageNames = this.sourceDirectory.getSourcePackageNames();
        return Stream.concat(sourceFileNames.stream().map(this::getPackageID),
                packageNames.stream().map(this::getPackageID)
        );
    }

    private Manifest getManifest() {
        Manifest manifest = new Manifest();
        try {
            if (sourceDirectory instanceof FileSystemProjectDirectory) {
                manifest = ManifestProcessor.parseTomlContentAsStream(sourceDirectory.getManifestContent());
            }
        } catch (TomlException tomlException) {
            throw new BLangCompilerException(tomlException.getMessage());
        }
        
        if (manifest.getProject().getVersion().isEmpty()) {
            manifest.getProject().setVersion(Names.DEFAULT_VERSION.getValue());
        }
        return manifest;
    }

    public PackageID getPackageID(String sourcePackage) {
        List<String> sourceFileNames = this.sourceDirectory.getSourceFileNames();
        Manifest manifest = getManifest();
        Name orgName = getOrgName(manifest);
        Name version = new Name(manifest.getProject().getVersion());

        //Check for built-in packages
        if (orgName.equals(Names.BUILTIN_ORG)) {
            return new PackageID(orgName, names.fromString(sourcePackage), version);
        }

        //Check for source files
        if (sourceFileNames.contains(sourcePackage)) {
            return new PackageID(sourcePackage);
        }

        //Check for packages
        List<String> packageNames = this.sourceDirectory.getSourcePackageNames();
        if (packageNames.contains(sourcePackage)) {
            return new PackageID(orgName, names.fromString(sourcePackage), version);
        }

        return null;
    }

    public SourceDirectory getSourceDirectory() {
        return sourceDirectory;
    }

    // private methods

    private SourceDirectory initializeAndGetSourceDirectory(CompilerContext context) {
        try {
            SourceDirectory srcDirectory = context.get(SourceDirectory.class);
            if (srcDirectory != null) {
                return srcDirectory;
            }
        
            String srcDirPathName = options.get(CompilerOptionName.PROJECT_DIR);
            if (srcDirPathName == null || srcDirPathName.isEmpty()) {
                throw new IllegalArgumentException("invalid project directory path");
            }
        
            Path sourceRoot = Paths.get(srcDirPathName);
        
            if (Files.notExists(sourceRoot)) {
                throw new BLangCompilerException("'" + sourceRoot + "' project directory does not exist.");
            }
        
            if (!Files.isDirectory(sourceRoot)) {
                throw new BLangCompilerException("'" + sourceRoot + "' project directory does not exist.");
            }
        
            if (Files.isSymbolicLink(sourceRoot)) {
                throw new BLangCompilerException("'" + sourceRoot + "' project directory is symlink.");
            }
        
            if (!Files.isWritable(sourceRoot)) {
                throw new BLangCompilerException("'" + sourceRoot + "' is not writable.");
            }
        
            sourceRoot = sourceRoot.normalize().toAbsolutePath();
        
            String sourceType = options.get(CompilerOptionName.SOURCE_TYPE);
            if (null != sourceType) {
                switch (sourceType) {
                    case "SINGLE_BAL_FILE":
                        srcDirectory = new FileSystemProgramDirectory(sourceRoot);
                        break;
                    case "SINGLE_MODULE":
                    case "ALL_MODULES":
                        // if src folder is missing
                        if (Files.notExists(sourceRoot.resolve(ProjectDirConstants.SOURCE_DIR_NAME))) {
                            throw new BLangCompilerException("cannot find module(s) to build/compile as 'src' " +
                                                             "directory is missing. modules should be placed inside " +
                                                             "an 'src' directory of the project.");
                        }
                        srcDirectory = new FileSystemProjectDirectory(sourceRoot);
                        break;
                    default:
                }
            } else {
                // resort to 'canHandle'
                srcDirectory = new FileSystemProjectDirectory(sourceRoot);
                if (!srcDirectory.canHandle(sourceRoot)) {
                    srcDirectory = new FileSystemProgramDirectory(sourceRoot);
                }
            }
        
            // validate Ballerina.toml
            if (srcDirectory instanceof FileSystemProjectDirectory) {
                Path manifestPath = sourceRoot.resolve(ProjectDirConstants.MANIFEST_FILE_NAME);
                ManifestProcessor.parseTomlContentFromFile(manifestPath);
            }
        
            context.put(SourceDirectory.class, srcDirectory);
            return srcDirectory;
        } catch (IOException e) {
            throw new BLangCompilerException("error occurred in finding manifest");
        } catch (TomlException tomlException) {
            throw new BLangCompilerException(tomlException.getMessage());
        }
    }

    private Name getOrgName(Manifest manifest) {
        return manifest.getProject().getOrgName() == null || manifest.getProject().getOrgName().isEmpty() ?
                Names.ANON_ORG : names.fromString(manifest.getProject().getOrgName());
    }

    /**
     * Check if sources exists in the package.
     *
     * @param pkg package name
     * @return true if ballerina sources exists, else false
     */
    boolean checkIfSourcesExists(String pkg) {
        // Check if it is a valid ballerina project.
        if (ProjectDirs.isProject(this.sourceDirectory.getPath()) &&
                !RepoUtils.isBallerinaStandaloneFile(this.sourceDirectory.getPath().resolve(pkg))) {
            return ProjectDirs.containsSourceFiles(this.sourceDirectory.getPath()
                    .resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(pkg));
        } else {
            return ProjectDirs.containsSourceFiles(this.sourceDirectory.getPath().resolve(pkg));
        }
    }
}

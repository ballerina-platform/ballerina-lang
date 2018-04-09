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

import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

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
        Manifest manifest = getManifest();
        return Stream.concat(sourceFileNames.stream().map(PackageID::new),
                             packageNames.stream().map(name -> new PackageID(Names.ANON_ORG,
                                                                             names.fromString(name),
                                                                             new Name(manifest.getVersion()))));
    }

    private Manifest getManifest() {
        Manifest manifest;
        if (sourceDirectory.getManifestContent() == null) {
            manifest = new Manifest();
        } else {
            manifest = ManifestProcessor.parseTomlContentAsStream(sourceDirectory.getManifestContent());
        }
        if (manifest.getVersion() == null) {
            manifest.setVersion(Names.DEFAULT_VERSION.getValue());
        }
        return manifest;
    }

    public Stream<PackageID> listPackages() {
        List<String> pkgNames = sourceDirectory.getSourcePackageNames();
        return pkgNames.stream().map(name -> new PackageID(Names.ANON_ORG,
                                                           names.fromString(name), Names.DEFAULT_VERSION));
    }

    public PackageID getPackageID(String sourcePackage) {
        List<String> sourceFileNames = this.sourceDirectory.getSourceFileNames();
        if (sourceFileNames.contains(sourcePackage)) {
            return new PackageID(sourcePackage);
        }

        List<String> packageNames = this.sourceDirectory.getSourcePackageNames();
        if (packageNames.contains(sourcePackage)) {
            Manifest manifest = getManifest();
            return new PackageID(Names.ANON_ORG, names.fromString(sourcePackage), new Name(manifest.getVersion()));
        }

        return null;
    }


    // private methods

    private SourceDirectory initializeAndGetSourceDirectory(CompilerContext context) {
        SourceDirectory srcDirectory = context.get(SourceDirectory.class);
        if (srcDirectory != null) {
            return srcDirectory;
        }

        String srcDirPathName = options.get(CompilerOptionName.PROJECT_DIR);
        if (srcDirPathName == null || srcDirPathName.isEmpty()) {
            throw new IllegalArgumentException("invalid project directory path");
        }

        Path projectDirPath = Paths.get(srcDirPathName);
        // TODO Validate projectDirPath, exists, get Absolute path etc. no simlinks
        // TODO Validate the project directory
        // TODO Check whether it is a directory and it exists.
        // TODO to real path.. isReadable isWritable etc..
        srcDirectory = new FileSystemProjectDirectory(projectDirPath);
        if (!srcDirectory.canHandle(projectDirPath)) {
            srcDirectory = new FileSystemProgramDirectory(projectDirPath);
        }

        context.put(SourceDirectory.class, srcDirectory);
        return srcDirectory;
    }
}

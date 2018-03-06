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
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;

/**
 * This class is responsible for finding packages with entry points
 * inside the project directory.
 *
 * @since 0.965.0
 */
public class ProjectDirectory {

    private static final CompilerContext.Key<ProjectDirectory> PROJECT_DIR_KEY =
            new CompilerContext.Key<>();

    private CompilerOptions options;
    private PackageLoader packageLoader;


    // Absolute path of the current directory.
    private Path projectDirPath;
    private Path manifestPath;
    private Path lockFilePath;
    private Path targetPath;
    private Path localRepoPath;
    private List<BLangPackage> packagesInProjectDir;
    private boolean scanned = true;

    private PathMatcher sourceFileMatcher = FileSystems.getDefault().getPathMatcher(
            "glob:*" + BLANG_SOURCE_EXT);

    public static ProjectDirectory getInstance(CompilerContext context) {
        ProjectDirectory projectDirectory = context.get(PROJECT_DIR_KEY);
        if (projectDirectory == null) {
            projectDirectory = new ProjectDirectory(context);
        }
        return projectDirectory;
    }

    private ProjectDirectory(CompilerContext context) {
        context.put(PROJECT_DIR_KEY, this);

        this.options = CompilerOptions.getInstance(context);
        this.packageLoader = PackageLoader.getInstance(context);
        this.projectDirPath = Paths.get(options.get(CompilerOptionName.PROJECT_DIR));
        this.packagesInProjectDir = new ArrayList<>();
    }

    public void listEntryPointPackages() {
    }

    public void listPackages() {
    }



    public Stream<BLangPackage> list() {
        // Return the next entry point

        // Get the project root

        // Scan the file system


        // TODO Validate the project directory
        // TODO Check whether it is a directory and it exists.

        return null;
    }


    // private methods

    private Stream<BLangPackage> listPackages0() {
        if (scanned) {
            return packagesInProjectDir.stream();
        }

        return null;
    }

    private boolean isPackage(Path path) {
        return true;
    }

    private boolean isSourceFile(Path path) {
        return sourceFileMatcher.matches(path);
    }

    private static class SourceFileFinder extends SimpleFileVisitor<Path> {
        private boolean sourceFileFound = false;
        private PathMatcher sourceFileMatcher;

        SourceFileFinder(PathMatcher sourceFileMatcher) {
            this.sourceFileMatcher = sourceFileMatcher;
        }

        boolean isSourceFileFound() {
            return sourceFileFound;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            super.visitFile(file, attrs);
            // TODO Consider test and resource packages

            if (sourceFileMatcher.matches(file)) {
                sourceFileFound = true;
                return FileVisitResult.TERMINATE;
            }

            return FileVisitResult.CONTINUE;
        }
    }
}

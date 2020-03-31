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
package org.wso2.ballerinalang.compiler.util;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;

/**
 * This class contains project directory specific utility methods.
 *
 * @since 0.965.0
 */
public class ProjectDirs {

    private static PathMatcher sourceFileMatcher = FileSystems.getDefault().getPathMatcher(
            "glob:*" + BLANG_SOURCE_EXT);

    private static PathMatcher testFileMatcher = FileSystems.getDefault().getPathMatcher(
            "glob:../src/*/tests/**" + BLANG_SOURCE_EXT);

    private static PathMatcher testResourceFileMatcher = FileSystems.getDefault().getPathMatcher(
            "glob:../src/*/tests/resources/**" + BLANG_SOURCE_EXT);

    public static boolean isSourceFile(Path path) {
        return !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) && sourceFileMatcher.matches(path);
    }

    public static Path getLastComp(Path path) {
        return path.getName(path.getNameCount() - 1);
    }

    public static BLangCompilerException getPackageNotFoundError(PackageID packageID) {
        if (packageID.isUnnamed) {
            return new BLangCompilerException("cannot find file '" + packageID + "'");
        }

        return new BLangCompilerException("cannot find module '" + packageID + "'");
    }

    public static BLangCompilerException getPackageNotFoundError(String sourcePackage) {
        if (sourcePackage.endsWith(ProjectDirConstants.BLANG_SOURCE_EXT)) {
            return new BLangCompilerException("cannot find file '" + sourcePackage + "'");
        }

        return new BLangCompilerException("cannot find module '" + sourcePackage + "'");
    }

    /**
     * Checks if the package contains ballerina source files.
     *
     * @param pkgPath package path
     * @return true if source files exists else false
     */
    public static boolean containsSourceFiles(Path pkgPath) throws BLangCompilerException {
        List<Path> sourceFiles = new ArrayList<>();
        try {
            sourceFiles = Files.find(pkgPath, Integer.MAX_VALUE, (path, attrs) ->
                    path.toString().endsWith(ProjectDirConstants.BLANG_SOURCE_EXT)).collect(Collectors.toList());
        } catch (IOException ignored) {
            // Here we are trying to check if there are source files inside the package to be compiled. If an error
            // occurs when trying to visit the files inside the package then we simply return the empty list created.
        } catch (UncheckedIOException e) {
            // Files#find returns an UncheckedIOException instead of an AccessDeniedException when there is a file to
            // which user doesn't have required permission.
            if (e.getCause() instanceof AccessDeniedException) {
                throw new BLangCompilerException("permission denied for path " + pkgPath.toString()
                        + ", cause: " + e.getMessage());
            } else {
                throw e;
            }
        }
        return sourceFiles.size() > 0;
    }

    /**
     * Checks if the source is from tests.
     * @param sourcePath source path
     * @param sourceRoot source root path
     * @param pkg package name
     * @return true if its a test source, else false
     */
    public static boolean isTestSource(Path sourcePath, Path sourceRoot, String pkg) {
        // CASE 1: Check if it the package name is "." i.e. if its a single ballerina source file, if so it should be
        // added to the bLangPackage
        if (Names.DOT.value.equals(pkg)) {
            return false;
        }
    
        // If the pkg is not a part of the sourceRoot project then it wont be a test source.
        if (!isModuleExist(sourceRoot, pkg)) {
            return false;
        }
        
        // Resolve package path with the source root
        Path pkgPath = sourceRoot.resolve(pkg);
        Path relativizePath = pkgPath.relativize(sourcePath);
        // Bal files should be inside tests directory but not in test resources
        return testFileMatcher.matches(relativizePath) && !testResourceFileMatcher.matches(relativizePath);
    }

    /**
     * Check of given path is a valid ballerina project.
     * @param path project path
     * @return true if a project
     */
    public static boolean isProject(Path path) {
        return Files.exists(path.resolve(ProjectDirConstants.MANIFEST_FILE_NAME));
    }

    /**
     * Find the project root by recursively up to the root.
     *
     * @param projectDir project path
     * @return project root
     */
    public static Path findProjectRoot(Path projectDir) {
        Path path = projectDir.resolve(ProjectDirConstants.MANIFEST_FILE_NAME);
        if (Files.exists(path)) {
            return projectDir;
        }
        Path parentsParent = projectDir.getParent();
        if (null != parentsParent) {
            return findProjectRoot(parentsParent);
        }
        return null;
    }

    /**
     * Check if a ballerina module exist.
     * @param projectPath project path
     * @param moduleName module name
     * @return module exist
     */
    public static boolean isModuleExist(Path projectPath, String moduleName) {
        Path modulePath = projectPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME).resolve(moduleName);
        return Files.exists(modulePath);
    }
}

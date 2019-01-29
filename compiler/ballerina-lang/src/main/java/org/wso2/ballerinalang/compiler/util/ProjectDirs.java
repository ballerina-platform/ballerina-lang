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
    public static boolean containsSourceFiles(Path pkgPath) {
        List<Path> sourceFiles = new ArrayList<>();
        try {
            sourceFiles = Files.find(pkgPath, Integer.MAX_VALUE, (path, attrs) ->
                    path.toString().endsWith(ProjectDirConstants.BLANG_SOURCE_EXT)).collect(Collectors.toList());
        } catch (IOException ignored) {
            // Here we are trying to check if there are source files inside the package to be compiled. If an error
            // occurs when trying to visit the files inside the package then we simply return the empty list created.
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
        // Resolve package path with the source root
        Path pkgPath = sourceRoot.resolve(pkg);
        // Construct a relative path between the package path and the ballerina source file path.
        // Lets assume source root is "/home/user/project" and pkg is "a".
        //  * If the source path is "/home/user/project/a/a.bal", then after relativizing the relative path will be
        //    a.bal
        //  * If the source path is "/home/user/project/a/foo/foo.bal", then after relativizing the relative path will
        //    be foo/foo.bal
        //  * If the source path is "/home/user/project/a/tests/test.bal", then after relativizing the relative path
        //    will be tests/test.bal
        Path relativizePath = pkgPath.relativize(sourcePath);
        // CASE 2: Check if the parent is null, if its null then it's a source file directly inside the package and i
        // should be added to the the bLangPackage. Else its a source file inside another directory of the package or
        // its a test source inside the "tests" directory
        if (relativizePath.getParent() == null) {
            return false;
        }
        // CASE 3: The source file can be a file inside another directory of the package or can be a test source inside
        // the "tests" directory. Get the file name of the element that is closest to the root in the directory
        // hierarchy i.e. which has index 0.
        Path rootPath = relativizePath.getName(0);
        if (rootPath != null) {
            Path rootFileName = rootPath.getFileName();
            if (rootFileName != null) {
                // Check if the root file name is equal to the "tests" directory, if its equal the source is from the
                // tests folder and it should be added to the testable package. Else if its not equal it should be added
                // to the bLangPackage.
                return ProjectDirConstants.TEST_DIR_NAME.equals(rootFileName.toString());
            }
        }
        return false;
    }
}

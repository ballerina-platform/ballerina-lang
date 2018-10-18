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

        return new BLangCompilerException("cannot find package '" + packageID + "'");
    }

    public static BLangCompilerException getPackageNotFoundError(String sourcePackage) {
        if (sourcePackage.endsWith(ProjectDirConstants.BLANG_SOURCE_EXT)) {
            return new BLangCompilerException("cannot find file '" + sourcePackage + "'");
        }

        return new BLangCompilerException("cannot find package '" + sourcePackage + "'");
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
}

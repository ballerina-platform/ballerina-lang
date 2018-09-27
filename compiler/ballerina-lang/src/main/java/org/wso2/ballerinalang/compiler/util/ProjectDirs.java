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

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

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
     * Checks if the source is from tests.
     * @param sourcePath source path
     * @param sourceRoot source root path
     * @param pkg package name
     * @return true if its a test source, else false
     */
    public static boolean isTestSource(Path sourcePath, Path sourceRoot, String pkg) {
        // Its a top level source file
        if (pkg.equals(Names.DOT.value)) {
            return false;
        }
        // Relativize source path with the source root
        // Lets assume source root is "/home/user/project", pkg is "a" and source path is "/home/user/project/a/a.bal"
        // Then after relativizing to get a relative path the source path will be a.bal
        Path relativizePath = sourceRoot.resolve(pkg).relativize(sourcePath);
        // Its a source file directly inside the package
        if (relativizePath.getParent() == null) {
            return false;
        }
        // Its a source file inside another package or its a test source
        Path parentPath = relativizePath.getName(0);
        if (parentPath != null) {
            Path parentFileName = parentPath.getFileName();
            if (parentFileName != null) {
                return ProjectDirConstants.TEST_DIR_NAME.equals(parentFileName.toString());
            }
        }
        return false;
    }
}

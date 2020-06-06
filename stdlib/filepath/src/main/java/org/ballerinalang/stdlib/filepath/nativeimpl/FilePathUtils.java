/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.filepath.nativeimpl;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.stdlib.filepath.Constants;
import org.ballerinalang.stdlib.filepath.Utils;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotLinkException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.regex.PatternSyntaxException;

/**
 * Native function implementations for the filepath module APIs.
 *
 * @since 1.0.5
 */
public class FilePathUtils {
    private static final String GLOB_SYNTAX_FLAVOR = "glob:";

    public static Object absolute(BString inputPath) {
        try {
            return StringUtils.fromString(
                    FileSystems.getDefault().getPath(inputPath.getValue()).toAbsolutePath().toString());
        } catch (InvalidPathException ex) {
            return Utils.getPathError(Constants.INVALID_PATH_ERROR, "Invalid path " + inputPath);
        }
    }

    public static Object matches(BString inputPath, BString pattern) {
        FileSystem fs = FileSystems.getDefault();
        PathMatcher matcher;
        try {
            if (!pattern.getValue().startsWith(GLOB_SYNTAX_FLAVOR)) {
                matcher = fs.getPathMatcher(GLOB_SYNTAX_FLAVOR + pattern);
            } else {
                matcher = fs.getPathMatcher(pattern.getValue());
            }
        } catch (PatternSyntaxException ex) {
            return Utils.getPathError(Constants.INVALID_PATTERN_ERROR, "Invalid pattern " + pattern);
        }
        if (inputPath == null) {
            return false;
        }
        return matcher.matches(Paths.get(inputPath.getValue()));
    }

    public static Object resolve(BString inputPath) {
        try {
            Path realPath = Files.readSymbolicLink(Paths.get(inputPath.getValue()).toAbsolutePath());
            return StringUtils.fromString(realPath.toString());
        } catch (NotLinkException ex) {
            return Utils.getPathError(Constants.NOT_LINK_ERROR, "Path is not a symbolic link " + inputPath);
        } catch (NoSuchFileException ex) {
            return Utils.getPathError(Constants.FILE_NOT_FOUND_ERROR, "File does not exist at " + inputPath);
        } catch (IOException ex) {
            return Utils.getPathError(Constants.IO_ERROR, "IO error for " + inputPath);
        } catch (SecurityException ex) {
            return Utils.getPathError(Constants.SECURITY_ERROR, "Security error for " + inputPath);
        }
    }
}

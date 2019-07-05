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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.filepath.Constants;
import org.ballerinalang.stdlib.filepath.Utils;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.regex.PatternSyntaxException;

/**
 * The native class to get real value of the path after evaluating any symbolic links.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = Constants.ORG_NAME,
        packageName = Constants.PACKAGE_NAME,
        functionName = "matches",
        isPublic = true
)
public class Matches extends BlockingNativeCallableUnit {

    private static final String GLOB_SYNTAX_FLAVOR = "glob:";

    @Override
    public void execute(Context context) {
    }

    public static Object matches(Strand strand, String inputPath, String pattern) {
        FileSystem fs = FileSystems.getDefault();
        PathMatcher matcher;
        try {
            if (!pattern.startsWith(GLOB_SYNTAX_FLAVOR)) {
                matcher = fs.getPathMatcher(GLOB_SYNTAX_FLAVOR + pattern);
            } else {
                matcher = fs.getPathMatcher(pattern);
            }
        } catch (PatternSyntaxException ex) {
            return Utils.getPathError("INVALID_PATTERN", ex);
        }
        if (inputPath == null) {
            return false;
        }
        return matcher.matches(Paths.get(inputPath));
    }
}

/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.plugins.idea.debugger;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import io.ballerina.plugins.idea.BallerinaConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains utility methods used for debugging.
 */
public class BallerinaDebuggerUtils {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^version\\s*=\\s*\"(\\d+\\.\\d+\\.\\d+)\"");
    private static final Pattern ORG_NAME_PATTERN = Pattern.compile("^org-name\\s*=\\s*\"(.*)\"");

    static String getOrgName(@NotNull Project project) {
        return readConfig(project, null, ORG_NAME_PATTERN);
    }

    public static String getVersion(@NotNull Project project) {
        return readConfig(project, "0.0.0", VERSION_PATTERN);
    }

    private static String readConfig(@NotNull Project project, @Nullable String defaultValue,
                                     @NotNull Pattern pattern) {
        VirtualFile baseDir = project.getBaseDir();
        VirtualFile relativeFile = VfsUtilCore.findRelativeFile(BallerinaConstants.BALLERINA_CONFIG_FILE_NAME, baseDir);
        if (relativeFile == null) {
            return defaultValue;
        }
        try (InputStream inputStream = relativeFile.getInputStream()) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line.trim());
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            bufferedReader.close();
            inputStreamReader.close();
        } catch (IOException e) {
            // Ignore errors
        }
        return defaultValue;
    }
}

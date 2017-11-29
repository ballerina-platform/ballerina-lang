/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compilation unit builder is for building ballerina compilation units.
 */
public class TextDocumentServiceUtil {
    private static final String PACKAGE_REGEX = "package\\s+([a-zA_Z_][\\.\\w]*);";


    public static String getSourceRoot(Path filePath, String pkgName) {
        if (filePath == null || filePath.getParent() == null) {
            return null;
        }
        Path parentPath = filePath.getParent();
        if (parentPath == null) {
            return null;
        }
        List<String> pathParts = Arrays.asList(parentPath.toString().split(Pattern.quote(File.separator)));

        List<String> pkgParts = "".equals(pkgName) ?
                new ArrayList<>() : Arrays.asList(pkgName.split(Pattern.quote(".")));
        Collections.reverse(pkgParts);
        boolean foundProgramDir = true;
        for (int i = 1; i <= pkgParts.size(); i++) {
            if (!pathParts.get(pathParts.size() - i).equals(pkgParts.get(i - 1))) {
                foundProgramDir = false;
                break;
            }
        }
        if (!foundProgramDir) {
            return null;
        }

        List<String> programDirParts = pathParts.subList(0, pathParts.size() - pkgParts.size());
        return String.join(File.separator, programDirParts);
    }

    /**
     * Get the package from file content.
     * @param fileContent - content of the file
     * @return - package declaration
     */
    public static String getPackageFromContent(String fileContent) {
        Pattern pkgPattern = Pattern.compile(PACKAGE_REGEX);
        Matcher pkgMatcher = pkgPattern.matcher(fileContent);

        if (!pkgMatcher.find()) {
            return "";
        }

        return pkgMatcher.group(1);
    }
}

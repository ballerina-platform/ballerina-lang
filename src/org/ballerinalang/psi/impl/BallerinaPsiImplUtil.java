/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.psi.impl;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

public class BallerinaPsiImplUtil {

    public static String getLocalPackageName(@NotNull String importPath) {
        String fileName = !StringUtil.endsWithChar(importPath, '/') && !StringUtil.endsWithChar(importPath, '\\')
                ? PathUtil.getFileName(importPath) : "";
        StringBuilder name = null;
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            if (!(Character.isLetter(c) || c == '_' || i != 0 && Character.isDigit(c))) {
                if (name == null) {
                    name = new StringBuilder(fileName.length());
                    name.append(fileName, 0, i);
                }
                name.append('_');
            } else if (name != null) {
                name.append(c);
            }
        }
        return name == null ? fileName : name.toString();
    }
}

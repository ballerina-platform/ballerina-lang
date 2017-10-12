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

package org.ballerinalang.util.program;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class contains a set of static methods to operate on {@code BLangPackage} objects. This class contains methods
 * to load packages or files from the given package repository.
 *
 * @since 0.8.0
 */
public class BLangPackages {

    public static Path convertToPackageName(Path packagePath) {
        int nameCount = packagePath.getNameCount();
        StringBuilder sb = new StringBuilder(nameCount);
        sb.append(packagePath.getName(0));
        for (int i = 1; i < nameCount; i++) {
            sb.append(".").append(packagePath.getName(i));
        }

        return Paths.get(sb.toString());
    }

}

/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.docgen.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Documentation model for representing a package name.
 */
public class PackageName {
    public final String prefix;
    public final String suffix;
    public final String name;
    
    public PackageName(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
        name = this.prefix + this.suffix;
    }
    
    public static List<Link> convertList(List<String> packageNames) {
    
        // Contains the common part of all packages
        List<String> commonPackagePart = null;
        for (String packageName : packageNames) {
            String[] packageNameParts = packageName.split("\\.");
        
            // Setting the first common package.
            if (commonPackagePart == null) {
                commonPackagePart = new LinkedList<>(Arrays.asList(packageNameParts));
                if (packageNameParts.length - 1 > 0) {
                    // Last package path is ignored.
                    commonPackagePart.remove(packageNameParts.length - 1);
                    continue;
                }
            }
        
            // Evaluating the common package.
            for (int i = 0; i < commonPackagePart.size(); i++) {
                if (!packageNameParts[i].equals(commonPackagePart.get(i))) {
                    List<String> newCommonPackagePart = new ArrayList<>();
                    newCommonPackagePart.addAll(Arrays.asList(packageNameParts).subList(0, i));
                    commonPackagePart = newCommonPackagePart;
                }
            }
        }
    
        // Collecting the package names.
        if (null != commonPackagePart) {
            String prefix = String.join(".", commonPackagePart);
            if (!"".equals(prefix)) {
                prefix += ".";
            }
            String finalPrefix = prefix;
            return packageNames.stream()
                    .map((packageName) -> new PackageName(finalPrefix, packageName.replace(finalPrefix, "")))
                    .map((packageObj -> new Link(packageObj, packageObj.name.toLowerCase(Locale.getDefault()), true)))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }
    
    @Override
    public String toString() {
        return "PackageName{" + "prefix='" + prefix + '\'' + ", suffix='" + suffix + '\'' + '}';
    }
}

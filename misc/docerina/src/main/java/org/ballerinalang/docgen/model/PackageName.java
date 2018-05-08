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

import org.ballerinalang.docgen.docs.BallerinaDocDataHolder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Documentation model for representing a package name.
 */
public class PackageName extends Caption {
    public final boolean isPackageName;
    public final String prefix;
    public final String suffix;
    
    public PackageName(String prefix, String suffix) {
        super(prefix + suffix);
        this.prefix = prefix;
        this.suffix = suffix;
        this.isPackageName = true;
    }
    
    /**
     * Converts a string list of package names to a {@link Link} list. While conversion, the {@link PackageName}'s
     * suffix and prefix is populated. The common part of all packages will set as prefix and the rest be as suffix.
     * @param packageNames The package names.
     * @return Package names as a link.
     */
    public static List<Link> convertList(List<String> packageNames) {
        // TODO currently org name doesn't come, hence hard coding ballerina/
        return packageNames.stream().map(packageName -> new PackageName(BallerinaDocDataHolder.getInstance()
                .getOrgName(), packageName)).map(packageObj -> new Link(packageObj, packageObj.suffix, true)).collect
                (Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PackageName{" + "prefix='" + prefix + '\'' + ", suffix='" + suffix + '\'' + '}';
    }
}

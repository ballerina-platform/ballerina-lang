/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml.model;

import org.ballerinalang.toml.util.PathUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Defines dependency object fields. The same object will be used to define patches.
 *
 * @since 0.964
 */
public class DependencyMetadata {
    private String version;
    private String path;

    /**
     * Get version of the dependency.
     *
     * @return version of the dependency
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the version of the dependency.
     *
     * @param version version of the dependency
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get the path/location of the dependency.
     *
     * @return location of the dependency
     */
    public Path getPath() {
        if (PathUtils.getPath(this.path) == null) {
            return null;
        }
        return Paths.get(PathUtils.getPath(this.path));
    }

    /**
     * Set the path/location of the dependency.
     *
     * @param path path of the dependency
     */
    public void setPath(String path) {
        this.path = path;
    }
}

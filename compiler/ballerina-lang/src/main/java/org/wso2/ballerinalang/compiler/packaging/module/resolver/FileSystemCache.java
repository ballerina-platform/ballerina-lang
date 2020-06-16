/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.packaging.module.resolver;

import org.ballerinalang.model.elements.PackageID;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.ballerinalang.compiler.packaging.module.resolver.Util.isValidVersion;

/**
 * Repo for file system cache.
 */
public abstract class FileSystemCache extends Cache {

    Path fileSystemCachePath;

    FileSystemCache(Path fileSystemCachePath) {
        this.fileSystemCachePath = fileSystemCachePath;
    }

    @Override
    public List<String> resolveVersions(PackageID moduleId, String filter) {
        List<String> versions = new ArrayList<>();
        Path modulePath = Paths.get(String.valueOf(this.fileSystemCachePath), moduleId.getOrgName().getValue(),
                moduleId.getName().getValue());
        File[] fileEntries = new File(String.valueOf(modulePath)).listFiles();
        if (modulePath.toFile().exists() && fileEntries != null) {
            for (final File fileEntry : fileEntries) {
                if (fileEntry.isDirectory() && isValidVersion(fileEntry.getName(), filter)) {
                    versions.add(fileEntry.getName());
                }
            }
        }
        return versions;
    }

    @Override
    public boolean isModuleExists(PackageID moduleId) {
        Path modulePath = Paths.get(String.valueOf(this.fileSystemCachePath), moduleId.getOrgName().getValue(),
                moduleId.getName().getValue(), moduleId.version.getValue());
        return modulePath.toFile().exists();
    }
}

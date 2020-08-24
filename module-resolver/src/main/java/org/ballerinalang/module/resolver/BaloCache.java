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

package org.ballerinalang.module.resolver;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.module.resolver.model.PackageBalo;
import org.ballerinalang.repository.PackageEntity;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Balo Cache representation for Home Balo Cache and Distribution Balo Cache.
 */
public class BaloCache extends FileSystemCache {

    public BaloCache(Path fileSystemCachePath) {
        super(fileSystemCachePath);
    }

    @Override
    public PackageEntity getModule(PackageID moduleId) {
        Path srcPath = Paths.get(String.valueOf(this.fileSystemCachePath), moduleId.getOrgName().getValue(),
                moduleId.getName().getValue(), moduleId.version.getValue());
        if (srcPath.toFile().exists()) {
            File[] files = new File(String.valueOf(srcPath)).listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.getPath().endsWith(".balo")) {
                        return new PackageBalo(moduleId, Paths.get(file.getPath()));
                    }
                }
            }
        }
        return null;
    }
}

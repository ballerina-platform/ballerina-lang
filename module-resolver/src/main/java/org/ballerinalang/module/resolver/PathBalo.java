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

import java.nio.file.Path;
import java.util.List;

/**
 * Repo to get module from path balos.
 */
public class PathBalo extends Cache {
    private Path baloPath;

    PathBalo(Path baloPath) {
        this.baloPath = baloPath;
    }

    @Override
    public List<String> resolveVersions(PackageID moduleId, String filter) {
        throw new  UnsupportedOperationException();
    }

    @Override
    public boolean isModuleExists(PackageID moduleId) {
        throw new  UnsupportedOperationException();
    }

    @Override
    PackageEntity getModule(PackageID moduleId) {
        if (this.baloPath.toFile().exists()) {
            return new PackageBalo(moduleId, baloPath);
        } else {
            return null;
        }
    }
}

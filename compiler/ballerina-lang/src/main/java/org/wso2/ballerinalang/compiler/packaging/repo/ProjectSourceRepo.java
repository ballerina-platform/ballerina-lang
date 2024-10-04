/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.model.Manifest;
import org.jetbrains.annotations.Nullable;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.nio.file.Path;

/**
 * Calculate bal files' path pattens in a project, excluding test files.
 */
public class ProjectSourceRepo extends NonSysRepo<Path> {
    private final boolean testEnabled;
    private final Manifest manifest;

    public ProjectSourceRepo(@Nullable Converter<Path> converter, Manifest manifest, boolean testEnabled) {
        super(converter);
        this.testEnabled = testEnabled;
        this.manifest = manifest;
    }

    public ProjectSourceRepo(Path projectRoot, Manifest manifest, boolean testEnabled) {
        this(new PathConverter(projectRoot), manifest, testEnabled);
    }

    @Override
    public Patten calculateNonSysPkg(PackageID moduleID) {
        // check module has same organization
        if (null != this.manifest && !moduleID.orgName.value.equals(this.manifest.getProject().getOrgName())) {
            return Patten.NULL;
        }
        
        if (testEnabled) {
            return new Patten(Patten.path(ProjectDirConstants.SOURCE_DIR_NAME), Patten.path(moduleID.getName().value),
                              Patten.WILDCARD_SOURCE_WITH_TEST);
        }
        return new Patten(Patten.path(ProjectDirConstants.SOURCE_DIR_NAME), Patten.path(moduleID.getName().value),
                          Patten.WILDCARD_SOURCE);
    }
}

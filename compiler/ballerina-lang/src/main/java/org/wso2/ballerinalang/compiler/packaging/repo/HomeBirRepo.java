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
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;

import static org.wso2.ballerinalang.compiler.packaging.Patten.LATEST_VERSION_DIR;
import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

/**
 * Repo for bir_cache in home repository.
 */
public class HomeBirRepo implements Repo<Path> {
    private PathConverter pathConverter;
    
    public HomeBirRepo() {
        Path repoLocation = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME + "-" +
                                                                          RepoUtils.getBallerinaVersion());
        this.pathConverter = new PathConverter(repoLocation);
    }
    
    @Override
    public Patten calculate(PackageID moduleID) {
        String orgName = moduleID.getOrgName().getValue();
        String pkgName = moduleID.getName().getValue();
        Patten.Part version;
        String versionStr = moduleID.getPackageVersion().getValue();
        if (versionStr.isEmpty()) {
            version = LATEST_VERSION_DIR;
        } else {
            version = path(versionStr);
        }
        
        return new Patten(path(orgName, pkgName), version, path(pkgName + ".bir"));
    }
    
    @Override
    public Converter<Path> getConverterInstance() {
        return this.pathConverter;
    }
    
    @Override
    public String toString() {
        return "{t:'HomeBirRepo', c:'" + this.pathConverter + "'}";
    }
}

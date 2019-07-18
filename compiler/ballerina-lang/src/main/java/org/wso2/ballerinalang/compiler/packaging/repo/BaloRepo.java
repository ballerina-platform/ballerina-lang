/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.packaging.repo;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.ZipConverter;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.nio.file.Path;

import static org.wso2.ballerinalang.compiler.packaging.Patten.LATEST_VERSION_DIR;
import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

/**
 * Repo for balo_cache.
 */
public class BaloRepo implements Repo<Path> {
    ZipConverter zipConverter;
    
    public BaloRepo(Path repo) {
        this.zipConverter = new ZipConverter(repo.resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME));
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
    
        return new Patten(path(orgName, pkgName),
                version,
                path(pkgName + ".balo", ProjectDirConstants.SOURCE_DIR_NAME, pkgName),
                Patten.WILDCARD_SOURCE);
    }
    
    @Override
    public Converter<Path> getConverterInstance() {
        return this.zipConverter;
    }
    
    @Override
    public String toString() {
        return "{t:'BaloRepo', c:'" + this.zipConverter + "'}";
    }
}

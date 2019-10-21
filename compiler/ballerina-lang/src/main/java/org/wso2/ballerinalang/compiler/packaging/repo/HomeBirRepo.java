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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.wso2.ballerinalang.compiler.packaging.Patten.LATEST_VERSION_DIR;
import static org.wso2.ballerinalang.compiler.packaging.Patten.path;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BIR_BALLERINA_VERSION_CACHE_FILE_NAME;

/**
 * Repo for bir_cache in home repository.
 */
public class HomeBirRepo implements Repo<Path> {
    private Path birCache;
    private PathConverter pathConverter;
    
    public HomeBirRepo() {
        this.birCache = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME);
        this.pathConverter = new PathConverter(this.birCache);
    }
    
    @Override
    public Patten calculate(PackageID moduleID) {
        try {
            String orgName = moduleID.getOrgName().getValue();
            String pkgName = moduleID.getName().getValue();
            Patten.Part version;
            String versionStr = moduleID.getPackageVersion().getValue();
            if (versionStr.isEmpty()) {
                version = LATEST_VERSION_DIR;
            } else {
                version = path(versionStr);
            }
        
            Path ballerinaVersionCachePath = this.birCache.resolve(orgName).resolve(pkgName)
                    .resolve(BIR_BALLERINA_VERSION_CACHE_FILE_NAME);
            
            // if ballerina version cache file does not exists,
            // then consider it that the current ballerina version it not
            // compatible with the bir(if such exists)
            if (Files.notExists(ballerinaVersionCachePath)) {
                return Patten.NULL;
            }
        
            if (Files.exists(ballerinaVersionCachePath)) {
                String ballerinaVersion = new String(Files.readAllBytes(ballerinaVersionCachePath),
                        StandardCharsets.UTF_8);
                // if ballerina version cache file exists but its a different version, then consider it that the
                // current ballerina version it not compatible with the bir(if such exists)
                if (!RepoUtils.getBallerinaVersion().equals(ballerinaVersion)) {
                    return Patten.NULL;
                }
            }
        
            return new Patten(path(orgName, pkgName), version, path(pkgName + ".bir"));
        } catch (IOException e) {
            return Patten.NULL;
        }
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

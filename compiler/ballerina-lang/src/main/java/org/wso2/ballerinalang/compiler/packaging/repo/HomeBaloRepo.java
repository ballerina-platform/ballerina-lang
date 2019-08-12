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
import org.wso2.ballerinalang.compiler.packaging.converters.SortablePath;
import org.wso2.ballerinalang.compiler.packaging.converters.ZipConverter;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.packaging.Patten.path;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.IMPLEMENTATION_VERSION;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * Repo for balo_cache.
 */
public class HomeBaloRepo implements Repo<Path> {
    private Path repoLocation;
    ZipConverter zipConverter;
    
    public HomeBaloRepo(Path repo) {
        this.repoLocation = repo.resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME);
        this.zipConverter = new ZipConverter(repo.resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME));
    }
    
    public Patten calculate(PackageID moduleID) {
        try {
            String orgName = moduleID.getOrgName().getValue();
            String pkgName = moduleID.getName().getValue();
            String versionStr = moduleID.getPackageVersion().getValue();
            
            // if the module doesn't exists at all stop looking for it.
            if (Files.notExists(this.repoLocation.resolve(orgName).resolve(pkgName))) {
                return Patten.NULL;
            }
            
            // get platforms supported
            List<String> supportedPlatforms = Arrays.stream(SUPPORTED_PLATFORMS).collect(Collectors.toList());
            // add any platform to the end of supported platforms list
            supportedPlatforms.add("any");
            for (String platform : supportedPlatforms) {
                Path baloFilePath;
                // check if version is empty. If so get the latest balo file.
                if (versionStr.isEmpty()) {
                    baloFilePath = getLatestBaloFile(this.repoLocation.resolve(orgName).resolve(pkgName), pkgName,
                            platform);
                    
                } else {
                    // Get the existing balo file.
                    String baloFileName = pkgName + "-" + IMPLEMENTATION_VERSION + "-" + platform + "-" + versionStr +
                                          ".balo";
                    baloFilePath = this.repoLocation.resolve(orgName).resolve(pkgName).resolve(versionStr)
                            .resolve(baloFileName);
                }
    
                // return Patten only if balo file exists.
                if (Files.exists(baloFilePath)) {
                    return new Patten(path(orgName, pkgName),
                            path(versionStr),
                            path(baloFilePath.getFileName().toString(), ProjectDirConstants.SOURCE_DIR_NAME, pkgName),
                            Patten.WILDCARD_SOURCE);
                }
            }
        
            return Patten.NULL;
        } catch (IOException e) {
            return Patten.NULL;
        }
    }
    
    /**
     * Get the latest balo for a given module name and platform.
     * <p>
     * Iterates through the module folder(<org_name>/<module-name>/*) and find the latest version. Return the path to
     * balo file with the found version.
     *
     * @param moduleFolder Folder where versions of a module is located(<org_name>/<module-name>).
     * @param moduleName   Name of the module.
     * @param platform     The platform of the module.
     * @return The path to the balo file.
     * @throws IOException Error when getting the list of version of the module folder.
     */
    private Path getLatestBaloFile(Path moduleFolder, String moduleName, String platform) throws IOException {
        Optional<Path> latestVersionPath = Files.list(moduleFolder)
                                        .map(SortablePath::new)
                                        .filter(SortablePath::valid)
                                        .sorted(Comparator.reverseOrder())
                                        .limit(1)
                                        .map(SortablePath::getPath)
                                        .findFirst();
    
        String latestVersion = latestVersionPath.get().getFileName().toString();
        String baloFileName = moduleName + "-" + IMPLEMENTATION_VERSION + "-" + platform + "-" + latestVersion + ".balo";
        return moduleFolder.resolve(latestVersion).resolve(baloFileName);
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

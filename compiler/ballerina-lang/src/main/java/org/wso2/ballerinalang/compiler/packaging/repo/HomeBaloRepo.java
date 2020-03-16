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
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.SortablePath;
import org.wso2.ballerinalang.compiler.packaging.converters.ZipConverter;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.packaging.Patten.path;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.IMPLEMENTATION_VERSION;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * Repo for balo_cache.
 */
public class HomeBaloRepo implements Repo<Path> {
    private Path repoLocation;
    private ZipConverter zipConverter;
    private List<String> supportedPlatforms = Arrays.stream(SUPPORTED_PLATFORMS).collect(Collectors.toList());
    private Map<PackageID, Manifest> dependencyManifests;
    
    public HomeBaloRepo(Map<PackageID, Manifest> dependencyManifests) {
        this.repoLocation = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME);
        this.dependencyManifests = dependencyManifests;
        this.zipConverter = new ZipConverter(this.repoLocation);
        supportedPlatforms.add("any");
    }
    
    public Patten calculate(PackageID moduleID) {
        try {
            // if path to balo is not given in the manifest file
            String orgName = moduleID.getOrgName().getValue();
            String pkgName = moduleID.getName().getValue();
            String versionStr = moduleID.getPackageVersion().getValue();
            
            // if the module doesn't exists at all stop looking for it.
            if (Files.notExists(this.repoLocation.resolve(orgName).resolve(pkgName))) {
                return Patten.NULL;
            }
            
            for (String platform : supportedPlatforms) {
                Path baloFilePath;
                // check if version is empty. If so get the latest balo file directly.
                if (versionStr.isEmpty()) {
                    Optional<Path> latestVersionPath = getLatestBaloFile(this.repoLocation.resolve(orgName)
                            .resolve(pkgName));
        
                    if (latestVersionPath.isPresent()) {
                        Path latestVersionDirectoryName = latestVersionPath.get().getFileName();
                        if (null != latestVersionDirectoryName) {
                            versionStr = latestVersionDirectoryName.toString();
                            baloFilePath = findBaloPath(this.repoLocation, orgName, pkgName, platform, versionStr);
                        } else {
                            return Patten.NULL;
                        }
                    } else {
                        return Patten.NULL;
                    }
                } else {
                    // Get the existing balo file.
                    baloFilePath = findBaloPath(this.repoLocation, orgName, pkgName, platform, versionStr);
                }
                
                // return Patten only if balo file exists.
                Path baloFileName = baloFilePath.getFileName();
                if (Files.exists(baloFilePath) && null != baloFileName) {
                    moduleID.version = new Name(versionStr);
    
                    // update dependency manifests map for imports of this moduleID.
                    this.dependencyManifests.put(moduleID,
                            RepoUtils.getManifestFromBalo(baloFilePath.toAbsolutePath()));
    
                    return new Patten(path(orgName, pkgName),
                            path(versionStr),
                            path(baloFileName.toString(), ProjectDirConstants.SOURCE_DIR_NAME, pkgName),
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
     * @return The path to the balo file.
     * @throws IOException Error when getting the list of version of the module folder.
     */
    private Optional<Path> getLatestBaloFile(Path moduleFolder) throws IOException {
        Optional<Path> path;
        try (Stream<Path> fileStream = Files.list(moduleFolder)) {
            path = fileStream.map(SortablePath::new)
                    .filter(SortablePath::valid)
                    .sorted(Comparator.reverseOrder())
                    .limit(1)
                    .map(SortablePath::getPath)
                    .findFirst();
        }
        return path;
    }
    
    @Override
    public Converter<Path> getConverterInstance() {
        return this.zipConverter;
    }
    
    @Override
    public String toString() {
        return "{t:'HomeBaloRepo', c:'" + this.zipConverter + "'}";
    }


    private Path findBaloPath(Path repoLocation, String orgName, String pkgName, String platform, String versionStr)
            throws IOException {
        Path baloFilePath = this.repoLocation.resolve(orgName).resolve(pkgName).resolve(versionStr);
        // try to find a compatible balo file
        if (Files.exists(baloFilePath)) {
            Stream<Path> list = Files.list(baloFilePath);
            PathMatcher pathMatcher = baloFilePath.getFileSystem()
                    .getPathMatcher("glob:**/" + pkgName + "-*-" +
                            platform + "-" + versionStr + ".balo");
            for (Path file : (Iterable<Path>) list::iterator) {
                if (pathMatcher.matches(file)) {
                    return file;
                }
            }
        }
        // if a similar file is not found assume the default balo name
        String baloFileName = pkgName + "-" + IMPLEMENTATION_VERSION + "-" + platform + "-" + versionStr +
                ".balo";
        return baloFilePath.resolve(baloFileName);
    }
}

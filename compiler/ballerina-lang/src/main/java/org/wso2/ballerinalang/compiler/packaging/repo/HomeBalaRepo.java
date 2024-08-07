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
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * Repo for bala_cache.
 */
public class HomeBalaRepo implements Repo<Path> {
    private Path repoLocation;
    private ZipConverter zipConverter;
    private List<String> supportedPlatforms = Arrays.stream(SUPPORTED_PLATFORMS).collect(Collectors.toList());
    private Map<PackageID, Manifest> dependencyManifests;
    
    public HomeBalaRepo(Map<PackageID, Manifest> dependencyManifests) {
        this.repoLocation = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BALA_CACHE_DIR_NAME);
        this.dependencyManifests = dependencyManifests;
        this.zipConverter = new ZipConverter(this.repoLocation);
        supportedPlatforms.add("any");
    }
    
    @Override
    public Patten calculate(PackageID moduleID) {
        try {
            // if path to bala is not given in the manifest file
            String orgName = moduleID.getOrgName().getValue();
            String pkgName = moduleID.getName().getValue();
            String versionStr = moduleID.getPackageVersion().getValue();
            
            // if the module doesn't exists at all stop looking for it.
            if (Files.notExists(this.repoLocation.resolve(orgName).resolve(pkgName))) {
                return Patten.NULL;
            }
            
            for (String platform : supportedPlatforms) {
                Path balaFilePath;
                // check if version is empty. If so get the latest bala file directly.
                if (versionStr.isEmpty()) {
                    Optional<Path> latestVersionPath = getLatestBalaFile(this.repoLocation.resolve(orgName)
                            .resolve(pkgName));
        
                    if (latestVersionPath.isPresent()) {
                        Path latestVersionDirectoryName = latestVersionPath.get().getFileName();
                        if (null != latestVersionDirectoryName) {
                            versionStr = latestVersionDirectoryName.toString();
                            balaFilePath = findBalaPath(this.repoLocation, orgName, pkgName, platform, versionStr);
                        } else {
                            return Patten.NULL;
                        }
                    } else {
                        return Patten.NULL;
                    }
                } else {
                    // Get the existing bala file.
                    balaFilePath = findBalaPath(this.repoLocation, orgName, pkgName, platform, versionStr);
                }
                
                // return Patten only if bala file exists.
                Path balaFileName = balaFilePath.getFileName();
                if (Files.exists(balaFilePath) && null != balaFileName) {
                    moduleID.version = new Name(versionStr);
    
                    // update dependency manifests map for imports of this moduleID.
                    this.dependencyManifests.put(moduleID,
                            RepoUtils.getManifestFromBala(balaFilePath.toAbsolutePath()));
    
                    return new Patten(path(orgName, pkgName),
                            path(versionStr),
                            path(balaFileName.toString(), ProjectDirConstants.SOURCE_DIR_NAME, pkgName),
                            Patten.WILDCARD_SOURCE);
                }
            }
        
            return Patten.NULL;
        } catch (IOException e) {
            return Patten.NULL;
        }
    }
    
    /**
     * Get the latest bala for a given module name and platform.
     * <p>
     * Iterates through the module folder(<org_name>/<module-name>/*) and find the latest version. Return the path to
     * bala file with the found version.
     *
     * @param moduleFolder Folder where versions of a module is located(<org_name>/<module-name>).
     * @return The path to the bala file.
     * @throws IOException Error when getting the list of version of the module folder.
     */
    private Optional<Path> getLatestBalaFile(Path moduleFolder) throws IOException {
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
        return "{t:'HomeBalaRepo', c:'" + this.zipConverter + "'}";
    }


    private Path findBalaPath(Path repoLocation, String orgName, String pkgName, String platform, String versionStr)
            throws IOException {
        Path balaFilePath = this.repoLocation.resolve(orgName).resolve(pkgName).resolve(versionStr);
        // try to find a compatible bala file
        if (Files.exists(balaFilePath)) {
            try (Stream<Path> list = Files.list(balaFilePath)) {
                PathMatcher pathMatcher = balaFilePath.getFileSystem()
                        .getPathMatcher("glob:**/" + pkgName + "-*-" +
                                platform + "-" + versionStr + ".bala");
                for (Path file : (Iterable<Path>) list::iterator) {
                    if (pathMatcher.matches(file)) {
                        return file;
                    }
                }
            }
        }
        // if a similar file is not found assume the default bala name
        String balaFileName = pkgName + "-" + orgName + "-" + platform + "-" + versionStr +
                ".bala";
        return balaFilePath.resolve(balaFileName);
    }
}

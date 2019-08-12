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
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.packaging.Patten.LATEST_VERSION_DIR;
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
    
    @Override
    public Patten calculate(PackageID moduleID) {
        try {
            String orgName = moduleID.getOrgName().getValue();
            String pkgName = moduleID.getName().getValue();
            Patten.Part version;
            String versionStr = moduleID.getPackageVersion().getValue();
    
            List<String> supportedPlatforms = Arrays.stream(SUPPORTED_PLATFORMS).collect(Collectors.toList());
            supportedPlatforms.add("any");
    
            for (String platform : supportedPlatforms) {
                if (versionStr.isEmpty()) {
                    getLatestBaloFile(this.repoLocation.resolve(orgName).resolve(pkgName), pkgName, platform)
                } else {
                    String baloFileName = pkgName + "-" + IMPLEMENTATION_VERSION + "-" + platform + "-" + versionStr + ".balo";
                    Path baloFilePath = this.repoLocation.resolve(orgName).resolve(pkgName).resolve(versionStr).resolve(baloFileName);
                    if (Files.exists(baloFilePath)) {
                        return new Patten(path(orgName, pkgName),
                                path(versionStr),
                                path(baloFileName, ProjectDirConstants.SOURCE_DIR_NAME, pkgName),
                                Patten.WILDCARD_SOURCE);
                    }
                }
                
                
                Path baloFileParentPath = this.repoLocation.resolve(orgName).resolve(pkgName).resolve(versionStr);
                String baloFileName = version != LATEST_VERSION_DIR ? baloFileName :
                                      getLatestBaloFile(baloFileParentPath, pkgName, platform);
                Path baloFilePath = baloFileParentPath
                        .resolve(baloFileName);
                if (Files.exists(baloFilePath)) {
                    return new Patten(path(orgName, pkgName),
                            version,
                            path(baloFileName, ProjectDirConstants.SOURCE_DIR_NAME, pkgName),
                            Patten.WILDCARD_SOURCE);
                }
            }
            
            return Patten.NULL;
            
            if (versionStr.isEmpty()) {
                version = LATEST_VERSION_DIR;
            } else {
                version = path(versionStr);
            }
    
            if (Files.notExists(this.repoLocation.resolve(orgName).resolve(pkgName))) {
                return Patten.NULL;
            }
    
            List<String> supportedPlatforms = Arrays.stream(SUPPORTED_PLATFORMS).collect(Collectors.toList());
            supportedPlatforms.add("any");
            for (String platform : supportedPlatforms) {
                Path baloFileParentPath = this.repoLocation.resolve(orgName).resolve(pkgName).resolve(versionStr);
                String baloFileName = version != LATEST_VERSION_DIR ?
                                      pkgName + "-" + IMPLEMENTATION_VERSION + "-" + platform + "-" + versionStr +
                                      ".balo" :
                                      getLatestBaloFile(baloFileParentPath, pkgName, platform);
                Path baloFilePath = baloFileParentPath
                        .resolve(baloFileName);
                if (Files.exists(baloFilePath)) {
                    return new Patten(path(orgName, pkgName),
                            version,
                            path(baloFileName, ProjectDirConstants.SOURCE_DIR_NAME, pkgName),
                            Patten.WILDCARD_SOURCE);
                }
            }
        
            return Patten.NULL;
        } catch (IOException e) {
            return Patten.NULL;
        }
    }
    
    private String getLatestBaloFile(Path baloFileParentPath, String pkgName, String platform) throws IOException {
        String glob = "glob:" + pkgName + "-" + IMPLEMENTATION_VERSION + "-" + platform + "-*.balo";
        final PathMatcher baloMatcher = FileSystems.getDefault().getPathMatcher(glob);
        List<Path> matchedBalos = new LinkedList<>();
        Files.walkFileTree(baloFileParentPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (baloMatcher.matches(path)) {
                    matchedBalos.add(path);
                }
                return FileVisitResult.CONTINUE;
            }
        
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
    
        Optional<Path> latestBaloFile =
                matchedBalos.stream()
                        .map(SortablePath::new)
                        .filter(SortablePath::valid)
                        .sorted(Comparator.reverseOrder())
                        .limit(1)
                        .map(SortablePath::getPath)
                        .findFirst();
    
        // there will always be a file
        return latestBaloFile.get().getFileName().toString();
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

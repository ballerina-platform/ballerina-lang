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
import java.math.BigInteger;
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
        String orgName = moduleID.getOrgName().getValue();
        String pkgName = moduleID.getName().getValue();
        Patten.Part version;
        String versionStr = moduleID.getPackageVersion().getValue();
        if (versionStr.isEmpty()) {
            version = LATEST_VERSION_DIR;
        } else {
            version = path(versionStr);
        }
        List<String> supportedPlatforms = Arrays.asList(SUPPORTED_PLATFORMS);
        supportedPlatforms.add("any");
        for (String platform : supportedPlatforms) {
            Path baloFileParentPath = this.repoLocation.resolve(orgName).resolve(pkgName).resolve(versionStr);
            String baloFileName = version != LATEST_VERSION_DIR ?
                                  pkgName + "-" + IMPLEMENTATION_VERSION + "-" + platform + "-" + version + ".balo" :
                                  getLatestBaloFile(baloFileParentPath, pkgName, IMPLEMENTATION_VERSION, platform);
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
    }
    
    private String getLatestBaloFile(Path baloFileParentPath, String pkgName, String implementationVersion,
                                     String platform) throws IOException {
        String glob = "glob:" + pkgName + "-" + IMPLEMENTATION_VERSION + "-" + platform + "-*.balo";
        final PathMatcher baloMatcher = FileSystems.getDefault().getPathMatcher(glob);
        List<Path> matchedBalos = new LinkedList<>();
        Files.walkFileTree(baloFileParentPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (baloMatcher.matches(path)) {
                    matchedBalos.add(path);
                }
                return FileVisitResult.CONTINUE;
            }
        
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
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
    
        if (latestBaloFile.isPresent())
        return latestBaloFile.get()
    }
    
    private List<String> extractVersions(List<Path> matchedBalos) {
        matchedBalos.replaceAll();
    }
    
    private BigInteger semVerToBigInt(String version) {
        int[] versionArray = Arrays.stream(version.split("\\.")).mapToInt(Integer::parseInt).toArray();
        BigInteger sum = BigInteger.valueOf(versionArray[0]);
        sum = sum.shiftLeft(Integer.SIZE);
        sum = sum.add(BigInteger.valueOf(versionArray[1]));
        sum = sum.shiftLeft(Integer.SIZE);
        sum = sum.add(BigInteger.valueOf(versionArray[2]));
        return sum;
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

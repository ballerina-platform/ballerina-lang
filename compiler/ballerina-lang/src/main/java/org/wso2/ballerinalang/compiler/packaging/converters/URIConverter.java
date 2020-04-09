/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.packaging.converters;

import org.ballerinalang.cli.module.Pull;
import org.ballerinalang.cli.module.exeptions.CommandException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Proxy;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.repo.HomeBaloRepo;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.programfile.ProgramFileConstants.IMPLEMENTATION_VERSION;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * Provide functions need to covert a patten to steam of by paths, by downloading them as url.
 */
public class URIConverter implements Converter<URI> {

    private HomeBaloRepo homeBaloRepo;
    protected URI base;
    protected final Map<PackageID, Manifest> dependencyManifests;
    private boolean isBuild = true;
    private PrintStream errStream = System.err;

    public URIConverter(URI base, Map<PackageID, Manifest> dependencyManifests) {
        this.base = URI.create(base.toString() + "/modules/");
        this.dependencyManifests = dependencyManifests;
        this.homeBaloRepo = new HomeBaloRepo(this.dependencyManifests);
    }

    public URIConverter(URI base, Map<PackageID, Manifest> dependencyManifests, boolean isBuild) {
        this.base = URI.create(base.toString() + "/modules/");
        this.dependencyManifests = dependencyManifests;
        this.isBuild = isBuild;
        this.homeBaloRepo = new HomeBaloRepo(this.dependencyManifests);
    }

    /**
     * Create the dir path provided.
     *
     * @param dirPath destination dir path
     */
    public void createDirectory(Path dirPath) {
        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                throw new RuntimeException("error occurred when creating the directory path " + dirPath);
            }
        }
    }

    @Override
    public URI start() {
        return base;
    }

    @Override
    public URI combine(URI s, String p) {
        return s.resolve(p + '/');
    }

    @Override
    public Stream<URI> getLatestVersion(URI u, PackageID packageID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<URI> expandBalWithTest(URI uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<URI> expandBal(URI u) {
        throw new UnsupportedOperationException();

    }

    public Stream<CompilerInput> finalize(URI remoteURI, PackageID moduleID) {
        // if path to balo is not given in the manifest file
        String orgName = moduleID.getOrgName().getValue();
        String moduleName = moduleID.getName().getValue();
        Path modulePathInBaloCache = RepoUtils.createAndGetHomeReposPath()
                .resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME)
                .resolve(orgName)
                .resolve(moduleName);
        
        // create directory path in balo cache
        createDirectory(modulePathInBaloCache);
        Proxy proxy = TomlParserUtils.readSettings().getProxy();

        String supportedVersionRange = "";
        boolean nightlyBuild = RepoUtils.getBallerinaVersion().contains("SNAPSHOT");
        for (String supportedPlatform : SUPPORTED_PLATFORMS) {
            String errorMessage = "";
            try {
                Pull.execute(remoteURI.toString(), modulePathInBaloCache.toString(), orgName + "/" + moduleName,
                        proxy.getHost(), proxy.getPort(), proxy.getUserName(), proxy.getPassword(),
                        supportedVersionRange, this.isBuild, nightlyBuild, IMPLEMENTATION_VERSION, supportedPlatform);
            } catch (CommandException e) {
                errorMessage = e.getMessage().trim();
            }

            if (!"".equals(errorMessage)) {
                // removing the error stack
                if (errorMessage.contains("\n\tat")) {
                    errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n\tat"));
                }

                // if module already exists in home repository
                if (errorMessage.contains("module already exists in the home repository") && this.isBuild) {
                    // Need to update the version of moduleID that was resolved by remote. But since the version
                    // cannot be returned by the call done to module_pull.bal file we need to set the version from
                    // the downloaded balo file.
                    Patten patten = this.homeBaloRepo.calculate(moduleID);
                    return patten.convertToSources(this.homeBaloRepo.getConverterInstance(), moduleID);
                }

                // check if the message is empty or not. Empty means module not found. Else some other error.
                // Log if it is some other error.
                if (!"".equals(errorMessage.replace("error: \t", "").trim())) {
                    this.errStream.println(errorMessage.trim());
                    return Stream.of();
                }
            } else {
                // Need to update the version of moduleID that was resolved by remote. But since the version cannot
                // be returned by the call done to module_pull.bal file we need to set the version from the
                // downloaded balo file.
                Patten patten = this.homeBaloRepo.calculate(moduleID);
                return patten.convertToSources(this.homeBaloRepo.getConverterInstance(), moduleID);
            }
        }
        return Stream.of();
    }

    @Override
    public String toString() {
        return base.toString();
    }

}

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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.toml.model.Proxy;
import org.ballerinalang.util.EmbeddedExecutorProvider;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.repo.CacheRepo;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.programfile.ProgramFileConstants.IMPLEMENTATION_VERSION;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * Provide functions need to covert a patten to steam of by paths, by downloading them as url.
 */
public class URIConverter implements Converter<URI> {

    private static CacheRepo binaryRepo = new CacheRepo(RepoUtils.createAndGetHomeReposPath(),
            ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME, CompilerPhase.BIR_GEN); // TODO check phase
    private final URI base;
    private boolean isBuild = true;
    private PrintStream errStream = System.err;

    public URIConverter(URI base) {
        this.base = URI.create(base.toString() + "/modules/");
    }

    public URIConverter(URI base, boolean isBuild) {
        this.base = URI.create(base.toString() + "/modules/");
        this.isBuild = isBuild;
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
                throw new RuntimeException("Error occurred when creating the directory path " + dirPath);
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
        String orgName = moduleID.getOrgName().getValue();
        String moduleName = moduleID.getName().getValue();
        Path modulePathInBaloCache = RepoUtils.createAndGetHomeReposPath()
                .resolve(ProjectDirConstants.BALO_CACHE_DIR_NAME)
                .resolve(orgName)
                .resolve(moduleName);
        
        createDirectory(modulePathInBaloCache);
        try {
            String modulePath = orgName + "/" + moduleName;
            Proxy proxy = TomlParserUtils.readSettings().getProxy();

            String supportedVersionRange = "";
            String nightlyBuild = String.valueOf(RepoUtils.getBallerinaVersion().contains("SNAPSHOT"));
            EmbeddedExecutor executor = EmbeddedExecutorProvider.getInstance().getExecutor();
            RuntimeException runtimeException = null;
            for (String supportedPlatform : SUPPORTED_PLATFORMS) {
                Optional<RuntimeException> execute = executor.executeMainFunction("module_pull",
                        remoteURI.toString(), modulePathInBaloCache.toString(), modulePath, proxy.getHost(),
                        proxy.getPort(), proxy.getUserName(), proxy.getPassword(), RepoUtils.getTerminalWidth(),
                        supportedVersionRange, String.valueOf(isBuild), nightlyBuild, IMPLEMENTATION_VERSION,
                        supportedPlatform);
                // Check if error has occurred or not.
                if (execute.isPresent()) {
                    runtimeException = execute.get();
                } else {
                    Patten patten = binaryRepo.calculate(moduleID);
                    return patten.convertToSources(binaryRepo.getConverterInstance(), moduleID);
                }
            }
    
            throw runtimeException;
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (!"".equals(errorMessage.trim())) {
                errStream.println(errorMessage);
            }
        }
        return Stream.of();
    }

    @Override
    public String toString() {
        return base.toString();
    }

}

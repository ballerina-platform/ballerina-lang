/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.maven;


import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maven dependency resolving.
 */
public class MavenResolverClient {
    public static final String COULD_NOT_FIND_ARTIFACT = "Could not find artifact";
    public static final String PACKAGE_FOUND = "package found";
    private static final String PACKAGE_NOT_FOUND = "package not found";
    public static final String PLATFORM = "platform";
    public static final String PLATFORM_ANY = "any";
    RepositorySystem system;
    DefaultRepositorySystemSession session;

    List<RemoteRepository> repositories;

    /**
     * Resolver will be initialized to specified to location.
     */
    public MavenResolverClient() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        system = locator.getService(RepositorySystem.class);
        session = MavenRepositorySystemUtils.newSession();
        repositories = new ArrayList<>();
    }

    /**
     * Resolves provided artifact into resolver location.
     *
     * @param groupId    group ID of the dependency
     * @param artifactId artifact ID of the dependency
     * @param version    version of the dependency
     * @return found/ not found status of the package
     * @throws MavenResolverClientException when specified dependency cannot be resolved
     */
    public String pullPackage(String groupId, String artifactId, String version, String targetLocation) throws
            MavenResolverClientException {

        LocalRepository localRepo = new LocalRepository(targetLocation);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        Artifact artifact = new DefaultArtifact(groupId, artifactId, "bala", version);
        Path balaFileDestPath = Paths.get(targetLocation);
        Path temporaryExtractionPath;
        try {
            session.setTransferListener(new TransferListenerForClient());
            ArtifactRequest artifactRequest = new ArtifactRequest();
            artifactRequest.setArtifact(artifact);
            artifactRequest.setRepositories(repositories);
            system.resolveArtifact(session, artifactRequest);
//            String artifactName = artifactResult.getArtifact().getFile().getName();
//            Path balaFilePath = artifactResult.getArtifact().getFile().toPath();
//            temporaryExtractionPath = balaFileDestPath.resolve(groupId).resolve(artifactId).resolve(version)
//                                        .resolve(PLATFORM);
//
//            try {
//                extractBala(balaFilePath, temporaryExtractionPath);
//                Files.delete(balaFilePath);
//            } catch (IOException e) {
//                throw new MavenResolverClientException("error occurred extracting the bala file: " + e.getMessage());
//            }
//
//            Path packageJsonPath = temporaryExtractionPath.resolve("package.json");
//            String platform = PLATFORM_ANY;
//            try (BufferedReader bufferedReader = Files.newBufferedReader(packageJsonPath, StandardCharsets.UTF_8)) {
//                JsonObject resultObj = new Gson().fromJson(bufferedReader, JsonObject.class);
//                platform = resultObj.get(PLATFORM).getAsString();
//            } catch (IOException e) {
//                throw new MavenResolverClientException("unable to read the package.json file from the bala: " + e.getMessage());
//            }
//
//            balaFileDestPath = balaFileDestPath.resolve(groupId).resolve(artifactId).resolve(version).resolve(platform);
//
//            try {
//                if (Files.isDirectory(balaFileDestPath) && Files.list(balaFileDestPath).findAny().isPresent()) {
//                    throw new MavenResolverClientException("package already exists in the home repository: " +
//                            balaFileDestPath);
//                }
//            } catch (IOException e) {
//                throw new MavenResolverClientException("error accessing bala : " + balaFileDestPath);
//            }
//
//            temporaryExtractionPath.toFile().renameTo(balaFileDestPath.toFile());

        } catch (ArtifactResolutionException e) {
            if (e.getMessage().contains(COULD_NOT_FIND_ARTIFACT)) {
                return PACKAGE_NOT_FOUND;
            }
            throw new MavenResolverClientException(e.getMessage());
        }
        return PACKAGE_FOUND;
    }

    /**
     * Specified repository will be added to remote repositories.
     *
     * @param id  identifier of the repository
     * @param url url of the repository
     */
    public void addRepository(String id, String url) {
        repositories.add(new RemoteRepository.Builder(id, "default", url).build());
    }

    /**
     * Specified repository will be added to remote repositories.
     *
     * @param id       identifier of the repository
     * @param url      url of the repository
     * @param username username which has authentication access
     * @param password password which has authentication access
     */
    public void addRepository(String id, String url, String username, String password) {
        Authentication authentication =
                new AuthenticationBuilder()
                        .addUsername(username)
                        .addPassword(password)
                        .build();
        repositories.add(new RemoteRepository.Builder(id, "default", url)
                .setAuthentication(authentication).build());
    }

    private static void extractBala(Path balaFilePath, Path balaFileDestPath) throws IOException {
        Files.createDirectories(balaFileDestPath);
        URI zipURI = URI.create("jar:" + balaFilePath.toUri().toString());
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            Path packageRoot = zipFileSystem.getPath("/");
            List<Path> paths = Files.walk(packageRoot).filter(path -> path != packageRoot).collect(Collectors.toList());
            for (Path path : paths) {
                Path destPath = balaFileDestPath.resolve(packageRoot.relativize(path).toString());
                if (destPath.toFile().isDirectory()) {
                    FileUtils.deleteDirectory(destPath.toFile());
                }
                Files.copy(path, destPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static String getPlatformFromBala(String balaName, String packageName, String version) {
        return balaName.split(packageName + "-")[1].split("-" + version)[0];
    }
}

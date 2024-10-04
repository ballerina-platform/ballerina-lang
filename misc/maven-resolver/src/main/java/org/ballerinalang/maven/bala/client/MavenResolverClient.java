/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.maven.bala.client;


import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;


/**
 * Maven bala dependency resolving.
 *
 * @since 2201.8.0
 */

public class MavenResolverClient {
    public static final String PLATFORM = "platform";
    public static final String BALA_EXTENSION = "bala";
    public static final String POM = "pom";
    public static final String DEFAULT_REPO = "default";
    public static final String ARTIFACT_SEPERATOR = "-";
    RepositorySystem system;
    DefaultRepositorySystemSession session;

    RemoteRepository.Builder repository;

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
    }

    /**
     * Resolves provided artifact into resolver location.
     *
     * @param groupId    group ID of the dependency
     * @param artifactId artifact ID of the dependency
     * @param version    version of the dependency
     * @throws MavenResolverClientException when specified dependency cannot be resolved
     */
    public void pullPackage(String groupId, String artifactId, String version, String targetLocation) throws
            MavenResolverClientException {

        LocalRepository localRepo = new LocalRepository(targetLocation);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        Artifact artifact = new DefaultArtifact(groupId, artifactId, BALA_EXTENSION, version);
        try {
            session.setTransferListener(new TransferListenerForClient());
            ArtifactRequest artifactRequest = new ArtifactRequest();
            artifactRequest.setArtifact(artifact);
            artifactRequest.addRepository(repository.build());
            system.resolveArtifact(session, artifactRequest);
        } catch (ArtifactResolutionException e) {
            throw new MavenResolverClientException(e.getMessage());
        }
    }


    /**
     * Deploys provided artifact into the repository.
     * @param balaPath      path to the bala
     * @param orgName       organization name
     * @param packageName   package name
     * @param version       version of the package
     * @throws MavenResolverClientException when deployment fails
     */
    public void pushPackage(Path balaPath, String orgName, String packageName, String version, Path localRepoPath)
            throws MavenResolverClientException {
        LocalRepository localRepo = new LocalRepository(localRepoPath.toAbsolutePath().toString());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setRepository(this.repository.build());
        Artifact mainArtifact = new DefaultArtifact(
                orgName, packageName, BALA_EXTENSION, version).setFile(balaPath.toFile());
        deployRequest.addArtifact(mainArtifact);
        try {
            File temporaryPom = generatePomFile(orgName, packageName, version);
            deployRequest.addArtifact(new SubArtifact(mainArtifact, "", POM, temporaryPom));
            system.deploy(session, deployRequest);
        } catch (DeploymentException | IOException e) {
            throw new MavenResolverClientException(e.getMessage());
        }
    }

    /**
     * Specified repository will be added to remote repositories.
     *
     * @param id  identifier of the repository
     * @param url url of the repository
     */
    public void addRepository(String id, String url) {
        this.repository = new RemoteRepository.Builder(id, DEFAULT_REPO, url);
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
        this.repository = new RemoteRepository.Builder(id, DEFAULT_REPO, url)
                .setAuthentication(authentication);
    }

    /**
     * Proxy will be set to the repository.
     * @param url url of the proxy
     * @param port port of the proxy
     * @param username username of the proxy
     * @param password password of the proxy
     */
    public void setProxy(String url, int port, String username, String password) {
        if (url.isEmpty() || port == 0) {
            return;
        }

        Proxy proxy;
        if ((!(username).isEmpty() && !(password).isEmpty())) {
            Authentication authentication =
                    new AuthenticationBuilder()
                            .addUsername(username)
                            .addPassword(password)
                            .build();
            proxy = new Proxy(null, url, port, authentication);
        } else {
            proxy = new Proxy(null, url, port);
        }

        this.repository.setProxy(proxy);
    }

    private File generatePomFile(String groupId, String artifactId, String version) throws IOException {
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId(groupId);
        model.setArtifactId(artifactId);
        model.setVersion(version);
        model.setPackaging(BALA_EXTENSION);
        File tempFile = File.createTempFile(groupId + ARTIFACT_SEPERATOR + artifactId + ARTIFACT_SEPERATOR +
                version, "." + POM);
        tempFile.deleteOnExit();
        Writer fw = WriterFactory.newXmlWriter(tempFile);
        new MavenXpp3Writer().write(fw, model);
        fw.close();
        return tempFile;
    }
}

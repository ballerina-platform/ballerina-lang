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
package org.ballerinalang.maven.bala.client;


import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
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
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.apache.maven.model.Model;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import java.nio.file.Path;


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

    RemoteRepository repository;

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
     * @return found/ not found status of the package
     * @throws MavenResolverClientException when specified dependency cannot be resolved
     */
    public String pullPackage(String groupId, String artifactId, String version, String targetLocation) throws
            MavenResolverClientException {

        LocalRepository localRepo = new LocalRepository(targetLocation);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        Artifact artifact = new DefaultArtifact(groupId, artifactId, "bala", version);
        try {
            session.setTransferListener(new TransferListenerForClient());
            ArtifactRequest artifactRequest = new ArtifactRequest();
            artifactRequest.setArtifact(artifact);
            artifactRequest.addRepository(repository);
            system.resolveArtifact(session, artifactRequest);

        } catch (ArtifactResolutionException e) {
            if (e.getMessage().contains(COULD_NOT_FIND_ARTIFACT)) {
                return PACKAGE_NOT_FOUND;
            }
            throw new MavenResolverClientException(e.getMessage());
        }
        return PACKAGE_FOUND;
    }


    /**
     * Deploys provided artifact into the repository.
     * @param balaPath path to the bala
     * @param orgName organization name
     * @param packageName package name
     * @param version version of the package
     * @throws MavenResolverClientException when deployment fails
     */
    public void pushPackage(Path balaPath, String orgName, String packageName, String version, Path localRepoPath)
            throws MavenResolverClientException {
        LocalRepository localRepo = new LocalRepository(localRepoPath.toAbsolutePath().toString());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setRepository(this.repository);
        Artifact mainArtifact = new DefaultArtifact(
                orgName, packageName, "bala", version).setFile(balaPath.toFile());
        deployRequest.addArtifact(mainArtifact);
        try {
            File temporaryPom = generatePomFile(orgName, packageName, version, "bala");
            deployRequest.addArtifact(new SubArtifact(mainArtifact, "", "pom", temporaryPom));
            system.deploy(session, deployRequest);
        } catch (DeploymentException |IOException e) {
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
        this.repository = new RemoteRepository.Builder(id, "default", url).build();
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
        this.repository = new RemoteRepository.Builder(id, "default", url)
                .setAuthentication(authentication).build();
    }

    private File generatePomFile(String groupId, String artifactId, String version, String packaging) throws IOException {
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId(groupId);
        model.setArtifactId(artifactId);
        model.setVersion(version);
        model.setPackaging(packaging);
        File tempFile = File.createTempFile(groupId + "-" + artifactId + "-" + version, ".pom");
        tempFile.deleteOnExit();
        Writer fw = WriterFactory.newXmlWriter(tempFile);
        new MavenXpp3Writer().write(fw, model);
        fw.close();
        fw = null;
        IOUtil.close(fw);
        return tempFile;
    }
}

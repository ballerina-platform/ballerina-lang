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
import org.ballerinalang.maven.exceptions.MavenResolverException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.graph.visitor.TreeDependencyVisitor;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for Maven dependency resolving.
 */
public class MavenResolver {

    private List<RemoteRepository> repositories;
    RepositorySystem system;
    DefaultRepositorySystemSession session;

    /**
     * Resolver will be initialized to specified to location.
     *
     * @param targetLocation file path the target
     */
    public MavenResolver(String targetLocation) {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        system = locator.getService(RepositorySystem.class);
        session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository(targetLocation);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        repositories = new ArrayList<>();
        repositories.add(new RemoteRepository.Builder(
                "central", "default", "https://repo.maven.apache.org/maven2/").build());
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

    /**
     * Resolves provided artifact into resolver location.
     *
     * @param groupId                       group ID of the dependency
     * @param artifactId                    artifact ID of the dependency
     * @param version                       version of the dependency
     * @param resolveTransitiveDependencies should resolve resolve transitive dependencies
     * @return List of resolved dependencies
     * @throws MavenResolverException when specified dependency cannot be resolved
     */
    public List<org.ballerinalang.maven.Dependency> resolve(String groupId, String artifactId, String version,
                                                            boolean resolveTransitiveDependencies)
            throws MavenResolverException {
        List<org.ballerinalang.maven.Dependency> resolvedDependencies = new ArrayList<>();
        Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + version);
        if (resolveTransitiveDependencies) {
            try {
                DependencyFilter classpathFilter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
                CollectRequest collectRequest = new CollectRequest();
                collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
                collectRequest.setRepositories(repositories);

                DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFilter);
                List<ArtifactResult> artifactResults =
                        system.resolveDependencies(session, dependencyRequest).getArtifactResults();

//                for (ArtifactResult artifactResult : artifactResults) {
//                    for (DependencyNode dependency : artifactResult.getRequest().getDependencyNode().getChildren()) {
//                        resolve(dependency.getArtifact().getGroupId(), dependency.getArtifact().getArtifactId(),
//                                dependency.getArtifact().getVersion(), false);
//                        resolvedDependencies.add(new org.ballerinalang.maven.Dependency(
//                                artifact.getGroupId(), artifact.getGroupId(), artifact.getVersion()));
//                    }
//                }


                CollectResult collectResult = system.collectDependencies(session, collectRequest);
                DependencyNode node = collectResult.getRoot();
                node.accept(new TreeDependencyVisitor(new DependencyVisitor() {
                    String indent = "";
                    @Override
                    public boolean visitEnter(DependencyNode dependencyNode) {
                        System.out.println(indent + dependencyNode.getArtifact());
                        indent += "    ";
                        return true;
                    }

                    @Override
                    public boolean visitLeave(DependencyNode dependencyNode) {
                        indent = indent.substring(0, indent.length() - 4);
                        return true;
                    }
                }));
            } catch (DependencyResolutionException | DependencyCollectionException e) {
                throw new MavenResolverException(e.getMessage());
            }
        } else {
            try {
                ArtifactRequest artifactRequest = new ArtifactRequest();
                artifactRequest.setArtifact(artifact);
                artifactRequest.setRepositories(repositories);
                system.resolveArtifact(session, artifactRequest);
                resolvedDependencies.add(new org.ballerinalang.maven.Dependency(
                        artifact.getGroupId(), artifact.getGroupId(), artifact.getVersion()));
            } catch (ArtifactResolutionException e) {
                throw new MavenResolverException(e.getMessage());
            }
        }
        return resolvedDependencies;
    }
}

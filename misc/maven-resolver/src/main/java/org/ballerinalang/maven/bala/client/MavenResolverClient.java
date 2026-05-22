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


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.ballerinalang.maven.bala.client.model.ConnectorPackageInfo;
import org.ballerinalang.maven.bala.client.model.ConnectorSearchEntry;
import org.ballerinalang.maven.bala.client.model.ConnectorSearchMavenMetadata;
import org.ballerinalang.maven.bala.client.model.PackageMavenMetadata;
import org.ballerinalang.maven.bala.client.model.PackageResolutionResponse;
import org.ballerinalang.maven.bala.client.model.PackageSearchEntry;
import org.ballerinalang.maven.bala.client.model.PkgSearchMavenMetadata;
import org.ballerinalang.maven.bala.client.model.PkgSearchSolrEntry;
import org.ballerinalang.maven.bala.client.model.PkgSearchSolrMavenMetadata;
import org.ballerinalang.maven.bala.client.model.SymbolSearchEntry;
import org.ballerinalang.maven.bala.client.model.SymbolSearchMavenMetadata;
import org.ballerinalang.maven.bala.client.model.ToolMavenMetadata;
import org.ballerinalang.maven.bala.client.model.ToolSearchEntry;
import org.ballerinalang.maven.bala.client.model.ToolSearchMavenMetadata;
import org.ballerinalang.maven.bala.client.model.Version;
import org.codehaus.plexus.util.WriterFactory;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.metadata.DefaultMetadata;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.MetadataRequest;
import org.eclipse.aether.resolution.MetadataResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


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
    private static final String METADATA_UPDATE_INTERVAL = "interval:10";
    private static final int CACHE_MAX_SIZE = 100;

    private final RepositorySystem system;
    private final DefaultRepositorySystemSession session;

    // LRU caches bounded to CACHE_MAX_SIZE to prevent unbounded memory growth.
    private final Map<String, PackageMavenMetadata> pkgMetadataCache =
            Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, PackageMavenMetadata> eldest) {
                    return size() > CACHE_MAX_SIZE;
                }
            });
    private final Map<String, ToolMavenMetadata> toolMetadataCache =
            Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, ToolMavenMetadata> eldest) {
                    return size() > CACHE_MAX_SIZE;
                }
            });

    // The builder is retained so that setProxy() can augment it after addRepository() is called.
    // remoteRepository is always rebuilt after any mutation to ensure consistency.
    private RemoteRepository.Builder repositoryBuilder;
    private RemoteRepository remoteRepository;

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
     * @param groupId        group ID of the dependency
     * @param artifactId     artifact ID of the dependency
     * @param version        version of the dependency
     * @param targetLocation path to the target location
     * @throws MavenResolverClientException when specified dependency cannot be resolved
     */
    public void pullPackage(String groupId, String artifactId, String version, String targetLocation) throws
            MavenResolverClientException {
        LocalRepository localRepo = new LocalRepository(targetLocation);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_IGNORE);
        Artifact artifact = new DefaultArtifact(groupId, artifactId, BALA_EXTENSION, version);
        try {
            session.setTransferListener(new TransferListenerForClient());
            ArtifactRequest artifactRequest = new ArtifactRequest();
            artifactRequest.setArtifact(artifact);
            artifactRequest.addRepository(remoteRepository);
            system.resolveArtifact(session, artifactRequest);
        } catch (ArtifactResolutionException e) {
            throw new MavenResolverClientException(e.getMessage());
        }
    }

    /**
     * Deploys provided artifact into the repository.
     *
     * @param balaPath      path to the bala
     * @param orgName       organization name
     * @param packageName   package name
     * @param version       version of the package
     * @param localRepoPath path to the local Maven repository
     * @throws MavenResolverClientException when deployment fails
     */
    public void pushPackage(Path balaPath, String orgName, String packageName, String version, Path localRepoPath)
            throws MavenResolverClientException {
        LocalRepository localRepo = new LocalRepository(localRepoPath.toAbsolutePath().toString());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setRepository(remoteRepository);
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
     * Get all versions of a package from the Maven repository.
     *
     * @param groupId       group ID of the package
     * @param artifactId    artifact ID of the package
     * @param localRepoPath path to the local Maven repository
     * @return list of version strings
     * @throws MavenResolverClientException when version resolution fails
     */
    public List<String> getPackageVersions(String groupId, String artifactId, Path localRepoPath) throws
            MavenResolverClientException {
        LocalRepository localRepo = new LocalRepository(localRepoPath.toAbsolutePath().toString());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setOffline(false);
        session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
        session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_IGNORE);

        Artifact artifact = new DefaultArtifact(groupId, artifactId, BALA_EXTENSION, "[0,)");
        VersionRangeRequest versionRangeRequest = new VersionRangeRequest();
        versionRangeRequest.setArtifact(artifact);
        versionRangeRequest.addRepository(remoteRepository);

        try {
            VersionRangeResult versionRangeResult = system.resolveVersionRange(session, versionRangeRequest);
            return versionRangeResult.getVersions().stream()
                    .map(org.eclipse.aether.version.Version::toString)
                    .collect(Collectors.toList());
        } catch (VersionRangeResolutionException e) {
            throw new MavenResolverClientException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all versions of a package from the central proxy Maven repository, using a cached metadata lookup.
     *
     * @param groupId            group ID of the package
     * @param artifactId         artifact ID of the package
     * @param ballerinaVersion   current Ballerina distribution version for compatibility filtering
     * @param localRepoPath      path to the local Maven repository
     * @return list of version strings
     * @throws MavenResolverClientException when version resolution fails
     */
    public List<String> getPackageVersionsInCentralProxy(String groupId, String artifactId,
                                                         String ballerinaVersion, Path localRepoPath) throws
            MavenResolverClientException {
        try {
            String cacheKey = groupId + ":" + artifactId;
            if (!pkgMetadataCache.containsKey(cacheKey)) {
                pkgMetadataCache.put(cacheKey, fetchPackageMetadata(groupId, artifactId, localRepoPath,
                        ballerinaVersion));
            }
            return pkgMetadataCache.get(cacheKey).getVersions().stream()
                    .filter(v -> isPkgDistVersionCompatible(ballerinaVersion, v.getBallerinaVersion()))
                    .map(Version::getVersion)
                    .collect(Collectors.toList());
        } catch (MavenResolverClientException e) {
            throw new MavenResolverClientException("Failed to get package metadata: " + e.getMessage());
        }
    }

    /**
     * Get the Ballerina distribution version required by a specific package version.
     *
     * @param org              organization name
     * @param pkgName          package name
     * @param version          package version
     * @param ballerinaVersion current Ballerina distribution version
     * @param localRepoPath    path to the local Maven repository
     * @return Ballerina version string
     * @throws MavenResolverClientException when metadata resolution fails
     */
    public String getBallerinaVersionForPackage(String org, String pkgName, String version,
                                                String ballerinaVersion, Path localRepoPath)
            throws MavenResolverClientException {
        try {
            String cacheKey = org + ":" + pkgName;
            if (!pkgMetadataCache.containsKey(cacheKey)) {
                pkgMetadataCache.put(cacheKey, fetchPackageMetadata(org, pkgName, localRepoPath, ballerinaVersion));
            }
            return pkgMetadataCache.get(cacheKey).getVersions().stream()
                    .filter(v -> v.getVersion().equals(version))
                    .map(Version::getBallerinaVersion)
                    .findFirst()
                    .orElseThrow(() -> new MavenResolverClientException(
                            "No matching version found for: " + pkgName + ":" + version));
        } catch (MavenResolverClientException e) {
            throw new MavenResolverClientException("Failed to get package metadata: " + e.getMessage());
        }
    }

    /**
     * Get the deprecation status of a specific package version.
     *
     * @param org              organization name
     * @param pkgName          package name
     * @param version          package version
     * @param ballerinaVersion current Ballerina distribution version
     * @param localRepoPath    path to the local Maven repository
     * @return true if the version is deprecated
     * @throws MavenResolverClientException when metadata resolution fails
     */
    public boolean getDeprecationStatus(String org, String pkgName, String version,
                                        String ballerinaVersion, Path localRepoPath)
            throws MavenResolverClientException {
        try {
            String cacheKey = org + ":" + pkgName;
            if (!pkgMetadataCache.containsKey(cacheKey)) {
                pkgMetadataCache.put(cacheKey, fetchPackageMetadata(org, pkgName, localRepoPath, ballerinaVersion));
            }
            return pkgMetadataCache.get(cacheKey).getVersions().stream()
                    .filter(v -> v.getVersion().equals(version))
                    .map(Version::isDeprecated)
                    .findFirst()
                    .orElseThrow(() -> new MavenResolverClientException(
                            "No matching version found for: " + pkgName + ":" + version));
        } catch (MavenResolverClientException e) {
            throw new MavenResolverClientException("Failed to get package metadata: " + e.getMessage());
        }
    }

    /**
     * Resolves provided dependency graph into resolver location.
     *
     * @param orgName          group ID of the dependency
     * @param packageName       artifact ID of the dependency
     * @param version          version of the dependency
     * @param ballerinaVersion current Ballerina distribution version
     * @param targetLocation   path to the target location
     * @return PackageResolutionResponse with the dependency graph
     * @throws MavenResolverClientException when specified dependency cannot be resolved
     */
    public PackageResolutionResponse resolveDependency(String orgName, String packageName, String version,
                                                       String ballerinaVersion, String targetLocation)
            throws MavenResolverClientException {
        LocalRepository localRepo = new LocalRepository(targetLocation);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_IGNORE);
        Artifact artifact = new DefaultArtifact(orgName, packageName, "depgraph", "json", version);
        try {
            session.setTransferListener(new TransferListenerForClient());
            ArtifactRequest artifactRequest = new ArtifactRequest();
            artifactRequest.setArtifact(artifact);
            artifactRequest.addRepository(remoteRepository);
            system.resolveArtifact(session, artifactRequest);
            String dependencyGraphStr = Files.readString(Paths.get(targetLocation).resolve(orgName).resolve(packageName)
                    .resolve(version).resolve(packageName + "-" + version + "-depgraph.json"));
            PackageResolutionResponse resolutionResponse = new Gson().fromJson(dependencyGraphStr,
                    PackageResolutionResponse.class);
            resolutionResponse.resolved().getFirst().setDeprecated(getDeprecationStatus(orgName, packageName, version,
                    ballerinaVersion, Paths.get(targetLocation)));
            return resolutionResponse;
        } catch (ArtifactResolutionException | IOException e) {
            throw new MavenResolverClientException(e.getMessage());
        }
    }

    /**
     * Get the list of listeners for a package from the Maven repository.
     *
     * @param orgName          organization name
     * @param packageName      package name
     * @param version          version of the package
     * @param ballerinaVersion current Ballerina distribution version
     * @param targetLocation   path to the target location
     * @return list of listener name strings across all modules
     * @throws MavenResolverClientException when the artifact cannot be resolved or the response cannot be parsed
     */
    public List<String> getListeners(String orgName, String packageName, String version,
                                     String ballerinaVersion, String targetLocation)
            throws MavenResolverClientException {
        LocalRepository localRepo = new LocalRepository(targetLocation);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_IGNORE);
        Artifact artifact = new DefaultArtifact(orgName, packageName, "listeners", "json", version);
        try {
            session.setTransferListener(new TransferListenerForClient());
            ArtifactRequest artifactRequest = new ArtifactRequest();
            artifactRequest.setArtifact(artifact);
            artifactRequest.addRepository(remoteRepository);
            system.resolveArtifact(session, artifactRequest);
            String listenersJson = Files.readString(Paths.get(targetLocation).resolve(orgName).resolve(packageName)
                    .resolve(version).resolve(packageName + "-" + version + "-listeners.json"));
            return parseListenersResponse(listenersJson);
        } catch (ArtifactResolutionException | IOException e) {
            throw new MavenResolverClientException(e.getMessage());
        }
    }

    /**
     * Get tool metadata including organization and name information, with caching.
     *
     * @param toolId           tool ID to retrieve metadata for
     * @param ballerinaVersion current Ballerina distribution version
     * @param localRepoPath    path to the local Maven repository
     * @return ToolMavenMetadata containing org, name, and version information
     * @throws MavenResolverClientException when metadata resolution fails
     */
    public ToolMavenMetadata getToolMetadata(String toolId, String ballerinaVersion, Path localRepoPath)
            throws MavenResolverClientException {
        ToolMavenMetadata metadata = toolMetadataCache.get(toolId);
        if (metadata == null) {
            metadata = fetchToolMetadata(toolId, localRepoPath, ballerinaVersion);
            toolMetadataCache.put(toolId, metadata);
        }
        return metadata;
    }

    /**
     * Get all compatible tool versions that match the given Ballerina distribution version.
     *
     * @param toolId           tool ID to resolve
     * @param ballerinaVersion Ballerina version to match
     * @param localRepoPath    path to the local Maven repository
     * @return list of compatible version strings
     * @throws MavenResolverClientException when metadata resolution fails
     */
    public List<String> getCompatibleToolVersions(String toolId, String ballerinaVersion, Path localRepoPath)
            throws MavenResolverClientException {
        ToolMavenMetadata metadata = toolMetadataCache.get(toolId);
        if (metadata == null) {
            metadata = fetchToolMetadata(toolId, localRepoPath, ballerinaVersion);
            toolMetadataCache.put(toolId, metadata);
        }
        return metadata.getVersions();
    }

    /**
     * Get package search metadata from the Maven repository by parsing the maven-metadata.xml file.
     *
     * @param query            artifact ID of the package search query
     * @param ballerinaVersion current Ballerina distribution version
     * @param localRepoPath    path to the local Maven repository
     * @return PkgSearchMavenMetadata object containing parsed XML metadata
     * @throws MavenResolverClientException when metadata resolution or XML parsing fails
     */
    public PkgSearchMavenMetadata getPkgSearchMetadata(String query, String ballerinaVersion, Path localRepoPath)
            throws MavenResolverClientException {
        configureMetadataSession(localRepoPath);
        try {
            String encodedQuery = Base64.getEncoder().withoutPadding()
                    .encodeToString(query.getBytes(StandardCharsets.UTF_8));
            File metadataFile = resolveMetadataFile("__packagesearch__", encodedQuery, ballerinaVersion);
            Document document = parseXmlFile(metadataFile);
            return parsePkgSearchMetadata(document);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new MavenResolverClientException("Failed to parse metadata XML: " + e.getMessage());
        }
    }

    /**
     * Get Solr-based package search metadata from the Maven repository by parsing the maven-metadata.xml file.
     *
     * @param query            artifact ID of the package search query
     * @param ballerinaVersion current Ballerina distribution version
     * @param localRepoPath    path to the local Maven repository
     * @return PkgSearchSolrMavenMetadata object containing parsed XML metadata
     * @throws MavenResolverClientException when metadata resolution or XML parsing fails
     */
    public PkgSearchSolrMavenMetadata getPkgSearchSolrMetadata(String query, String ballerinaVersion,
                                                               Path localRepoPath)
            throws MavenResolverClientException {
        configureMetadataSession(localRepoPath);
        try {
            String encodedQuery = Base64.getEncoder().withoutPadding()
                    .encodeToString(query.getBytes(StandardCharsets.UTF_8));
            File metadataFile = resolveMetadataFile("__packagesearchsolr__", encodedQuery, ballerinaVersion);
            Document document = parseXmlFile(metadataFile);
            return parsePkgSearchSolrMetadata(document);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new MavenResolverClientException("Failed to parse metadata XML: " + e.getMessage());
        }
    }

    /**
     * Get tool search metadata from the Maven repository by parsing the maven-metadata.xml file.
     *
     * @param query            artifact ID of the tool search query
     * @param ballerinaVersion current Ballerina distribution version
     * @param localRepoPath    path to the local Maven repository
     * @return ToolSearchMavenMetadata object containing parsed XML metadata
     * @throws MavenResolverClientException when metadata resolution or XML parsing fails
     */
    public ToolSearchMavenMetadata getToolSearchMetadata(String query, String ballerinaVersion, Path localRepoPath)
            throws MavenResolverClientException {
        configureMetadataSession(localRepoPath);
        try {
            String encodedQuery = Base64.getEncoder().withoutPadding()
                    .encodeToString(query.getBytes(StandardCharsets.UTF_8));
            File metadataFile = resolveMetadataFile("__toolsearch__", encodedQuery, ballerinaVersion);
            Document document = parseXmlFile(metadataFile);
            return parseToolSearchMetadata(document);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new MavenResolverClientException("Failed to parse metadata XML: " + e.getMessage());
        }
    }

    /**
     * Get symbol search metadata from the Maven repository by parsing the maven-metadata.xml file.
     *
     * @param query            artifact ID of the symbol search query
     * @param ballerinaVersion current Ballerina distribution version
     * @param localRepoPath    path to the local Maven repository
     * @return SymbolSearchMavenMetadata object containing parsed XML metadata
     * @throws MavenResolverClientException when metadata resolution or XML parsing fails
     */
    public SymbolSearchMavenMetadata getSymbolSearchMetadata(String query, String ballerinaVersion, Path localRepoPath)
            throws MavenResolverClientException {
        configureMetadataSession(localRepoPath);
        try {
            String encodedQuery = Base64.getEncoder().withoutPadding()
                    .encodeToString(query.getBytes(StandardCharsets.UTF_8));
            File metadataFile = resolveMetadataFile("__symbolsearch__", encodedQuery, ballerinaVersion);
            Document document = parseXmlFile(metadataFile);
            return parseSymbolSearchMetadata(document);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new MavenResolverClientException("Failed to parse metadata XML: " + e.getMessage());
        }
    }

    /**
     * Get connector search metadata from the Maven repository by parsing the maven-metadata.xml file.
     *
     * @param query            artifact ID of the connector search query
     * @param ballerinaVersion current Ballerina distribution version
     * @param localRepoPath    path to the local Maven repository
     * @return ConnectorSearchMavenMetadata object containing parsed XML metadata
     * @throws MavenResolverClientException when metadata resolution or XML parsing fails
     */
    public ConnectorSearchMavenMetadata getConnectorSearchMetadata(String query, String ballerinaVersion,
                                                                   Path localRepoPath)
            throws MavenResolverClientException {
        configureMetadataSession(localRepoPath);
        try {
            String encodedQuery = Base64.getEncoder().withoutPadding()
                    .encodeToString(query.getBytes(StandardCharsets.UTF_8));
            File metadataFile = resolveMetadataFile("__connectorsearch__", encodedQuery, ballerinaVersion);
            Document document = parseXmlFile(metadataFile);
            return parseConnectorSearchMetadata(document);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new MavenResolverClientException("Failed to parse metadata XML: " + e.getMessage());
        }
    }

    /**
     * Specified repository will be added to remote repositories.
     *
     * @param id  identifier of the repository
     * @param url url of the repository
     */
    public void addRepository(String id, String url) {
        this.repositoryBuilder = new RemoteRepository.Builder(id, DEFAULT_REPO, url);
        this.remoteRepository = this.repositoryBuilder.build();
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
        Authentication authentication = new AuthenticationBuilder()
                .addUsername(username)
                .addPassword(password)
                .build();
        this.repositoryBuilder = new RemoteRepository.Builder(id, DEFAULT_REPO, url)
                .setAuthentication(authentication);
        this.remoteRepository = this.repositoryBuilder.build();
    }

    /**
     * Proxy will be set to the repository. Must be called after {@link #addRepository}.
     *
     * @param url      url of the proxy
     * @param port     port of the proxy
     * @param username username of the proxy
     * @param password password of the proxy
     */
    public void setProxy(String url, int port, String username, String password) {
        if (url.isEmpty() || port == 0) {
            return;
        }

        Proxy proxy;
        if (!username.isEmpty() && !password.isEmpty()) {
            Authentication authentication = new AuthenticationBuilder()
                    .addUsername(username)
                    .addPassword(password)
                    .build();
            proxy = new Proxy(null, url, port, authentication);
        } else {
            proxy = new Proxy(null, url, port);
        }

        this.repositoryBuilder.setProxy(proxy);
        this.remoteRepository = this.repositoryBuilder.build();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Configure session settings shared by all metadata fetch operations.
     * Uses a fixed refresh interval to balance freshness with network overhead.
     */
    private void configureMetadataSession(Path localRepoPath) {
        LocalRepository localRepo = new LocalRepository(localRepoPath.toAbsolutePath().toString());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setOffline(false);
        session.setUpdatePolicy(METADATA_UPDATE_INTERVAL);
        session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_IGNORE);
    }

    /**
     * Resolve a maven-metadata.xml file from the remote repository and return the local cached {@link File}.
     * The groupId is prefixed with the transformed Ballerina version (e.g. {@code v2201-13-0.groupId}).
     *
     * @param groupId          Maven groupId
     * @param artifactId       Maven artifactId
     * @param ballerinaVersion current Ballerina distribution version (e.g. {@code 2201.13.2})
     * @return the resolved local metadata file
     * @throws MavenResolverClientException when the file cannot be resolved
     */
    private File resolveMetadataFile(String groupId, String artifactId, String ballerinaVersion)
            throws MavenResolverClientException {
        String versionedGroupId = transformBallerinaVersion(ballerinaVersion) + "." + groupId;
        Metadata metadata = new DefaultMetadata(
                versionedGroupId, artifactId, "maven-metadata.xml", Metadata.Nature.RELEASE_OR_SNAPSHOT);
        MetadataRequest metadataRequest = new MetadataRequest(metadata, remoteRepository, null);
        MetadataResult result = system.resolveMetadata(
                session, Collections.singletonList(metadataRequest)).get(0);
        Metadata resolved = result.getMetadata();
        if (resolved != null) {
            File metadataFile = resolved.getFile();
            if (metadataFile != null && metadataFile.exists()) {
                return metadataFile;
            }
        }
        throw new MavenResolverClientException("Metadata file not found or could not be resolved");
    }

    /**
     * Fetch and parse package metadata from the remote Maven repository.
     */
    private PackageMavenMetadata fetchPackageMetadata(String orgName, String packageName, Path localRepoPath,
                                                      String ballerinaVersion)
            throws MavenResolverClientException {
        configureMetadataSession(localRepoPath);
        try {
            File metadataFile = resolveMetadataFile(orgName, packageName, ballerinaVersion);
            Document document = parseXmlFile(metadataFile);
            return parsePackageMetadata(document);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new MavenResolverClientException("Failed to parse metadata XML: " + e.getMessage());
        }
    }

    /**
     * Fetch and parse tool metadata from the remote Maven repository.
     */
    private ToolMavenMetadata fetchToolMetadata(String toolId, Path localRepoPath, String ballerinaVersion)
            throws MavenResolverClientException {
        configureMetadataSession(localRepoPath);
        try {
            File metadataFile = resolveMetadataFile("__tools__", toolId, ballerinaVersion);
            Document document = parseXmlFile(metadataFile);
            return parseToolMetadata(document);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new MavenResolverClientException("Failed to parse metadata XML: " + e.getMessage());
        }
    }

    /**
     * Check if the current Ballerina distribution version is compatible with the tool's required distribution version.
     * The current version must have the same major version and a minor version greater than or equal to the tool's.
     */
    private boolean isPkgDistVersionCompatible(String currentDistVersion, String toolDistVersion) {
        try {
            String[] currentParts = currentDistVersion.split("\\.");
            String[] toolParts = toolDistVersion.split("\\.");

            if (currentParts.length < 2 || toolParts.length < 2) {
                return currentDistVersion.equals(toolDistVersion);
            }

            int currentMajor = Integer.parseInt(currentParts[0]);
            int currentMinor = Integer.parseInt(currentParts[1]);
            int toolMajor = Integer.parseInt(toolParts[0]);
            int toolMinor = Integer.parseInt(toolParts[1]);

            return currentMajor == toolMajor && currentMinor >= toolMinor;
        } catch (Exception e) {
            return currentDistVersion.equals(toolDistVersion);
        }
    }

    private PackageMavenMetadata parsePackageMetadata(Document document) {
        PackageMavenMetadata metadata = new PackageMavenMetadata();
        metadata.setGroupId(getTagValue(document, "groupId"));
        metadata.setArtifactId(getTagValue(document, "artifactId"));

        List<Version> versions = new ArrayList<>();
        NodeList versionsWrapper = document.getElementsByTagName("versions");
        if (versionsWrapper.getLength() > 0) {
            NodeList versionNodes = ((Element) versionsWrapper.item(0)).getElementsByTagName("version");
            for (int i = 0; i < versionNodes.getLength(); i++) {
                Node node = versionNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    versions.add(parseVersion((Element) node));
                }
            }
        }
        metadata.setVersions(versions);
        return metadata;
    }

    private PkgSearchMavenMetadata parsePkgSearchMetadata(Document document) {
        PkgSearchMavenMetadata metadata = new PkgSearchMavenMetadata();
        metadata.setGroupId(getTagValue(document, "groupId"));
        metadata.setArtifactId(getTagValue(document, "artifactId"));

        NodeList packageNodes = document.getElementsByTagName("package");
        List<PackageSearchEntry> packages = new ArrayList<>();
        for (int i = 0; i < packageNodes.getLength(); i++) {
            Node node = packageNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                packages.add(parsePackageSearchEntry((Element) node));
            }
        }
        metadata.setPackages(packages);
        return metadata;
    }

    private PkgSearchSolrMavenMetadata parsePkgSearchSolrMetadata(Document document) {
        PkgSearchSolrMavenMetadata metadata = new PkgSearchSolrMavenMetadata();
        metadata.setGroupId(getTagValue(document, "groupId"));
        metadata.setArtifactId(getTagValue(document, "artifactId"));
        metadata.setCount(parseIntTag(document, "count"));
        metadata.setLimit(parseIntTag(document, "limit"));
        metadata.setOffset(parseIntTag(document, "offset"));

        NodeList packageNodes = document.getElementsByTagName("package");
        List<PkgSearchSolrEntry> packages = new ArrayList<>();
        for (int i = 0; i < packageNodes.getLength(); i++) {
            Node node = packageNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                packages.add(parsePkgSearchSolrEntry((Element) node));
            }
        }
        metadata.setPackages(packages);
        return metadata;
    }

    private PkgSearchSolrEntry parsePkgSearchSolrEntry(Element element) {
        PkgSearchSolrEntry pkg = new PkgSearchSolrEntry();
        pkg.setId(parseLongContent(getElementTextContent(element, "id")));
        pkg.setOrg(getElementTextContent(element, "org"));
        pkg.setName(getElementTextContent(element, "name"));
        pkg.setVersion(getElementTextContent(element, "version"));
        pkg.setSummary(getElementTextContent(element, "summary"));
        pkg.setCreatedDate(parseLongContent(getElementTextContent(element, "createdDate")));
        pkg.setBalToolId(getElementTextContent(element, "balToolId"));
        pkg.setPullCount(parseLongContent(getElementTextContent(element, "pullCount")));

        List<String> authors = new ArrayList<>();
        Element authorsElement = (Element) element.getElementsByTagName("authors").item(0);
        if (authorsElement != null) {
            NodeList authorNodes = authorsElement.getElementsByTagName("author");
            for (int i = 0; i < authorNodes.getLength(); i++) {
                Node authorNode = authorNodes.item(i);
                if (authorNode.getNodeType() == Node.ELEMENT_NODE) {
                    authors.add(authorNode.getTextContent().trim());
                }
            }
        }
        pkg.setAuthors(authors);

        List<String> keywords = new ArrayList<>();
        Element keywordsElement = (Element) element.getElementsByTagName("keywords").item(0);
        if (keywordsElement != null) {
            NodeList keywordNodes = keywordsElement.getElementsByTagName("keyword");
            for (int i = 0; i < keywordNodes.getLength(); i++) {
                Node keywordNode = keywordNodes.item(i);
                if (keywordNode.getNodeType() == Node.ELEMENT_NODE) {
                    keywords.add(keywordNode.getTextContent().trim());
                }
            }
        }
        pkg.setKeywords(keywords);
        return pkg;
    }

    private SymbolSearchMavenMetadata parseSymbolSearchMetadata(Document document) {
        SymbolSearchMavenMetadata metadata = new SymbolSearchMavenMetadata();
        metadata.setGroupId(getTagValue(document, "groupId"));
        metadata.setArtifactId(getTagValue(document, "artifactId"));
        metadata.setCount(parseIntTag(document, "count"));
        metadata.setLimit(parseIntTag(document, "limit"));
        metadata.setOffset(parseIntTag(document, "offset"));

        NodeList symbolNodes = document.getElementsByTagName("symbol");
        List<SymbolSearchEntry> symbols = new ArrayList<>();
        for (int i = 0; i < symbolNodes.getLength(); i++) {
            Node node = symbolNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                symbols.add(parseSymbolSearchEntry((Element) node));
            }
        }
        metadata.setSymbols(symbols);
        return metadata;
    }

    private SymbolSearchEntry parseSymbolSearchEntry(Element element) {
        SymbolSearchEntry symbol = new SymbolSearchEntry();
        symbol.setId(getElementTextContent(element, "id"));
        symbol.setPackageID(getElementTextContent(element, "packageID"));
        symbol.setName(getElementTextContent(element, "name"));
        symbol.setOrg(getElementTextContent(element, "org"));
        symbol.setVersion(getElementTextContent(element, "version"));
        symbol.setCreatedDate(parseLongContent(getElementTextContent(element, "createdDate")));
        symbol.setIcon(getElementTextContent(element, "icon"));
        symbol.setSymbolType(getElementTextContent(element, "symbolType"));
        symbol.setSymbolParent(getElementTextContent(element, "symbolParent"));
        symbol.setSymbolName(getElementTextContent(element, "symbolName"));
        symbol.setDescription(getElementTextContent(element, "description"));
        symbol.setSymbolSignature(getElementTextContent(element, "symbolSignature"));
        symbol.setIsolated(Boolean.parseBoolean(getElementTextContent(element, "isIsolated")));
        symbol.setRemote(Boolean.parseBoolean(getElementTextContent(element, "isRemote")));
        symbol.setResource(Boolean.parseBoolean(getElementTextContent(element, "isResource")));
        symbol.setClosed(Boolean.parseBoolean(getElementTextContent(element, "isClosed")));
        symbol.setDistinct(Boolean.parseBoolean(getElementTextContent(element, "isDistinct")));
        symbol.setReadOnly(Boolean.parseBoolean(getElementTextContent(element, "isReadOnly")));
        return symbol;
    }

    private ConnectorSearchMavenMetadata parseConnectorSearchMetadata(Document document) {
        ConnectorSearchMavenMetadata metadata = new ConnectorSearchMavenMetadata();
        metadata.setGroupId(getTagValue(document, "groupId"));
        metadata.setArtifactId(getTagValue(document, "artifactId"));
        metadata.setCount(parseIntTag(document, "count"));
        metadata.setLimit(parseIntTag(document, "limit"));
        metadata.setOffset(parseIntTag(document, "offset"));

        NodeList connectorNodes = document.getElementsByTagName("connector");
        List<ConnectorSearchEntry> connectors = new ArrayList<>();
        for (int i = 0; i < connectorNodes.getLength(); i++) {
            Node node = connectorNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                connectors.add(parseConnectorSearchEntry((Element) node));
            }
        }
        metadata.setConnectors(connectors);
        return metadata;
    }

    private ConnectorSearchEntry parseConnectorSearchEntry(Element element) {
        ConnectorSearchEntry connector = new ConnectorSearchEntry();
        connector.setId(getElementTextContent(element, "id"));
        connector.setName(getElementTextContent(element, "name"));
        connector.setDisplayName(getElementTextContent(element, "displayName"));
        connector.setModuleName(getElementTextContent(element, "moduleName"));
        connector.setIcon(getElementTextContent(element, "icon"));
        connector.setDocumentation(getElementTextContent(element, "documentation"));

        NodeList packageNodes = element.getElementsByTagName("package");
        if (packageNodes.getLength() > 0 && packageNodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
            connector.setPackageInfo(parseConnectorPackageInfo((Element) packageNodes.item(0)));
        }
        return connector;
    }

    private ConnectorPackageInfo parseConnectorPackageInfo(Element element) {
        ConnectorPackageInfo pkg = new ConnectorPackageInfo();
        pkg.setId(getElementTextContent(element, "id"));
        pkg.setOrganization(getElementTextContent(element, "organization"));
        pkg.setName(getElementTextContent(element, "name"));
        pkg.setVersion(getElementTextContent(element, "version"));
        pkg.setPlatform(getElementTextContent(element, "platform"));
        pkg.setLanguageSpecificationVersion(getElementTextContent(element, "languageSpecificationVersion"));
        pkg.setDeprecated(Boolean.parseBoolean(getElementTextContent(element, "isDeprecated")));
        pkg.setDeprecateMessage(getElementTextContent(element, "deprecateMessage"));
        pkg.setUrl(getElementTextContent(element, "URL"));
        pkg.setBalaVersion(getElementTextContent(element, "balaVersion"));
        pkg.setBalaURL(getElementTextContent(element, "balaURL"));
        pkg.setDigest(getElementTextContent(element, "digest"));
        pkg.setSummary(getElementTextContent(element, "summary"));
        pkg.setTemplate(Boolean.parseBoolean(getElementTextContent(element, "template")));
        pkg.setSourceCodeLocation(getElementTextContent(element, "sourceCodeLocation"));
        pkg.setBallerinaVersion(getElementTextContent(element, "ballerinaVersion"));
        pkg.setIcon(getElementTextContent(element, "icon"));
        pkg.setOwnerUUID(getElementTextContent(element, "ownerUUID"));
        pkg.setCreatedDate(parseLongContent(getElementTextContent(element, "createdDate")));
        pkg.setPullCount(parseLongContent(getElementTextContent(element, "pullCount")));
        pkg.setVisibility(getElementTextContent(element, "visibility"));
        pkg.setBalToolId(getElementTextContent(element, "balToolId"));
        pkg.setGraalvmCompatible(getElementTextContent(element, "graalvmCompatible"));

        List<String> licenses = new ArrayList<>();
        Element licensesElement = (Element) element.getElementsByTagName("licenses").item(0);
        if (licensesElement != null) {
            NodeList licenseNodes = licensesElement.getElementsByTagName("license");
            for (int i = 0; i < licenseNodes.getLength(); i++) {
                Node licenseNode = licenseNodes.item(i);
                if (licenseNode.getNodeType() == Node.ELEMENT_NODE) {
                    licenses.add(licenseNode.getTextContent().trim());
                }
            }
        }
        pkg.setLicenses(licenses);

        List<String> authors = new ArrayList<>();
        Element authorsElement = (Element) element.getElementsByTagName("authors").item(0);
        if (authorsElement != null) {
            NodeList authorNodes = authorsElement.getElementsByTagName("author");
            for (int i = 0; i < authorNodes.getLength(); i++) {
                Node authorNode = authorNodes.item(i);
                if (authorNode.getNodeType() == Node.ELEMENT_NODE) {
                    authors.add(authorNode.getTextContent().trim());
                }
            }
        }
        pkg.setAuthors(authors);

        List<String> keywords = new ArrayList<>();
        Element keywordsElement = (Element) element.getElementsByTagName("keywords").item(0);
        if (keywordsElement != null) {
            NodeList keywordNodes = keywordsElement.getElementsByTagName("keyword");
            for (int i = 0; i < keywordNodes.getLength(); i++) {
                Node keywordNode = keywordNodes.item(i);
                if (keywordNode.getNodeType() == Node.ELEMENT_NODE) {
                    keywords.add(keywordNode.getTextContent().trim());
                }
            }
        }
        pkg.setKeywords(keywords);
        return pkg;
    }

    private ToolSearchMavenMetadata parseToolSearchMetadata(Document document) {
        ToolSearchMavenMetadata metadata = new ToolSearchMavenMetadata();
        metadata.setGroupId(getTagValue(document, "groupId"));
        metadata.setArtifactId(getTagValue(document, "artifactId"));
        metadata.setCount(parseIntTag(document, "count"));
        metadata.setLimit(parseIntTag(document, "limit"));
        metadata.setOffset(parseIntTag(document, "offset"));

        NodeList toolNodes = document.getElementsByTagName("tool");
        List<ToolSearchEntry> tools = new ArrayList<>();
        for (int i = 0; i < toolNodes.getLength(); i++) {
            Node node = toolNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                tools.add(parseToolSearchEntry((Element) node));
            }
        }
        metadata.setTools(tools);
        return metadata;
    }

    private ToolMavenMetadata parseToolMetadata(Document document) {
        ToolMavenMetadata metadata = new ToolMavenMetadata();
        metadata.setGroupId(getTagValue(document, "groupId"));
        metadata.setArtifactId(getTagValue(document, "artifactId"));
        metadata.setOrg(getTagValue(document, "org"));
        metadata.setName(getTagValue(document, "package"));

        List<String> versions = new ArrayList<>();
        NodeList versionsWrapper = document.getElementsByTagName("versions");
        if (versionsWrapper.getLength() > 0) {
            NodeList versionNodes = ((Element) versionsWrapper.item(0)).getElementsByTagName("version");
            for (int i = 0; i < versionNodes.getLength(); i++) {
                Node node = versionNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String version = node.getTextContent().trim();
                    if (!version.isEmpty()) {
                        versions.add(version);
                    }
                }
            }
        }
        metadata.setVersions(versions);
        return metadata;
    }

    private PackageSearchEntry parsePackageSearchEntry(Element element) {
        PackageSearchEntry pkg = new PackageSearchEntry();
        pkg.setOrg(getElementTextContent(element, "org"));
        pkg.setName(getElementTextContent(element, "name"));
        pkg.setVersion(getElementTextContent(element, "version"));
        pkg.setSummary(getElementTextContent(element, "summary"));
        pkg.setCreatedDate(parseLongContent(getElementTextContent(element, "createdDate")));

        List<String> authors = new ArrayList<>();
        Element authorsElement = (Element) element.getElementsByTagName("authors").item(0);
        if (authorsElement != null) {
            NodeList authorNodes = authorsElement.getElementsByTagName("author");
            for (int i = 0; i < authorNodes.getLength(); i++) {
                Node authorNode = authorNodes.item(i);
                if (authorNode.getNodeType() == Node.ELEMENT_NODE) {
                    authors.add(authorNode.getTextContent().trim());
                }
            }
        }
        pkg.setAuthors(authors);
        return pkg;
    }

    private ToolSearchEntry parseToolSearchEntry(Element element) {
        ToolSearchEntry tool = new ToolSearchEntry();
        tool.setOrg(getElementTextContent(element, "org"));
        tool.setName(getElementTextContent(element, "name"));
        tool.setVersion(getElementTextContent(element, "version"));
        tool.setSummary(getElementTextContent(element, "summary"));
        tool.setBalToolId(getElementTextContent(element, "balToolId"));
        tool.setCreatedDate(parseLongContent(getElementTextContent(element, "createdDate")));
        return tool;
    }

    private Version parseVersion(Element element) {
        Version version = new Version();
        version.setVersion(getElementTextContent(element, "number"));
        version.setPlatform(getElementTextContent(element, "platform"));
        version.setIsDeprecated(Boolean.parseBoolean(getElementTextContent(element, "isDeprecated")));
        version.setBallerinaVersion(getElementTextContent(element, "ballerinaVersion"));
        return version;
    }

    private Document parseXmlFile(File xmlFile)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }

    private String getTagValue(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return node.getTextContent();
            }
        }
        return null;
    }

    private String getElementTextContent(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent().trim();
        }
        return "";
    }

    private long parseLongContent(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseIntTag(Document document, String tagName) {
        String value = getTagValue(document, tagName);
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Transform a Ballerina distribution version into the versioned groupId prefix format.
     * Takes the first two dot-separated segments and appends {@code 0} as the patch segment,
     * so any suffix (e.g. {@code -SNAPSHOT}) on the third segment is discarded naturally.
     *
     * <pre>
     *   2201.13.2          -> v2201-13-0
     *   2201.13.2-SNAPSHOT -> v2201-13-0
     * </pre>
     */
    private String transformBallerinaVersion(String ballerinaVersion) {
        String[] parts = ballerinaVersion.split("\\.");
        return "v" + parts[0] + "-" + parts[1] + "-0";
    }

    /**
     * Parse the listeners JSON response and collect all listener names across all modules.
     *
     * <p>Expected JSON structure:
     * <pre>
     * {
     *   "data": {
     *     "apiDocs": {
     *       "docsData": {
     *         "modules": [
     *           { "listeners": "[listenerA,listenerB]" }
     *         ]
     *       }
     *     }
     *   }
     * }
     * </pre>
     * The {@code listeners} field value is appended as-is to the result list.
     */
    private List<String> parseListenersResponse(String json) {
        JsonObject root = new Gson().fromJson(json, JsonObject.class);
        var modules = root.getAsJsonObject("data")
                .getAsJsonObject("apiDocs")
                .getAsJsonObject("docsData")
                .getAsJsonArray("modules");

        List<String> listeners = new ArrayList<>();
        for (JsonElement moduleElement : modules) {
            JsonElement listenersElement = moduleElement.getAsJsonObject().get("listeners");
            if (listenersElement == null || listenersElement.isJsonNull()) {
                continue;
            }
            listeners.add(listenersElement.getAsString());
        }
        return listeners;
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

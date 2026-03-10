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
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.ballerinalang.maven.bala.client.model.PackageResolutionResponse;
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
import org.eclipse.aether.version.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    RepositorySystem system;
    DefaultRepositorySystemSession session;
    Map<String, PackageMavenMetadata> pkgMetadataCache = new HashMap<>();
    Map<String, ToolMavenMetadata> toolMetadataCache = new HashMap<>();

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
        // Disable checksum validation and hash file downloading
        session.setChecksumPolicy(org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_IGNORE);
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
        // Disable checksum validation and hash file downloading
        session.setChecksumPolicy(org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_IGNORE);

        Artifact artifact = new DefaultArtifact(groupId, artifactId, BALA_EXTENSION, "[0,)");
        VersionRangeRequest versionRangeRequest = new VersionRangeRequest();
        versionRangeRequest.setArtifact(artifact);
        versionRangeRequest.addRepository(repository.build());

        try {
            VersionRangeResult versionRangeResult = system.resolveVersionRange(session, versionRangeRequest);
            return versionRangeResult.getVersions().stream()
                    .map(Version::toString)
                    .collect(Collectors.toList());
        } catch (VersionRangeResolutionException e) {
            throw new MavenResolverClientException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public List<String> getPackageVersionsInCentralProxy(String groupId, String artifactId, Path localRepoPath) throws
            MavenResolverClientException {
         try {
            String cacheKey = groupId + ":" + artifactId;
            if (!pkgMetadataCache.containsKey(cacheKey)) {
                PackageMavenMetadata metadata = getPackageMetadata(groupId, artifactId, localRepoPath);
                pkgMetadataCache.put(cacheKey, metadata);
            }
            return pkgMetadataCache.get(cacheKey).getVersions().stream()
                    .map(BVersion::getNumber)
                    .collect(Collectors.toList());
        } catch (MavenResolverClientException e) {
                    throw new MavenResolverClientException("Failed to get package metadata: " + e.getMessage());
        }
    }

    public String getBallerinaVersionForPackage(String org, String pkgName, String version, Path localRepoPath)
            throws MavenResolverClientException {
        try {
            String cacheKey = org + ":" + pkgName;
            if (!pkgMetadataCache.containsKey(cacheKey)) {
                PackageMavenMetadata metadata = getPackageMetadata(org, pkgName, localRepoPath);
                pkgMetadataCache.put(cacheKey, metadata);
            }
            return pkgMetadataCache.get(cacheKey).getVersions().stream().filter(v -> v.getNumber().equals(version))
                    .map(BVersion::getBallerinaVersion)
                    .toList().getFirst();
        } catch (MavenResolverClientException e) {
            throw new MavenResolverClientException("Failed to get package metadata: " + e.getMessage());
        }
    }

    public boolean getDeprecationStatus(String org, String pkgName, String version, Path localRepoPath)
            throws MavenResolverClientException {
        try {
            String cacheKey = org + ":" + pkgName;
            if (!pkgMetadataCache.containsKey(cacheKey)) {
                PackageMavenMetadata metadata = getPackageMetadata(org, pkgName, localRepoPath);
                pkgMetadataCache.put(cacheKey, metadata);
            }
            return pkgMetadataCache.get(cacheKey).getVersions().stream().filter(v -> v.getNumber().equals(version))
                    .map(BVersion::isDeprecated)
                    .toList().getFirst();
        } catch (MavenResolverClientException e) {
            throw new MavenResolverClientException("Failed to get package metadata: " + e.getMessage());
        }
    }

    /**
     * Resolves provided dependency graph into resolver location.
     *
     * @param groupId    group ID of the dependency
     * @param artifactId artifact ID of the dependency
     * @param version    version of the dependency
     * @return
     * @throws MavenResolverClientException when specified dependency cannot be resolved
     */
    public PackageResolutionResponse resolveDependency(String groupId, String artifactId, String version,
                                                       String targetLocation) throws MavenResolverClientException {

        LocalRepository localRepo = new LocalRepository(targetLocation);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        // Disable checksum validation and hash file downloading
        session.setChecksumPolicy(org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_IGNORE);
        Artifact artifact = new DefaultArtifact(groupId, artifactId, "depgraph", "json", version);
        try {
            session.setTransferListener(new TransferListenerForClient());
            ArtifactRequest artifactRequest = new ArtifactRequest();
            artifactRequest.setArtifact(artifact);
            artifactRequest.addRepository(repository.build());
            system.resolveArtifact(session, artifactRequest);
            String dependencyGraphStr = Files.readString(Paths.get(targetLocation).resolve(groupId).resolve(artifactId)
                    .resolve(version).resolve(artifactId + "-" + version + "-depgraph.json"));
            PackageResolutionResponse resolutionResponse = new Gson().fromJson(dependencyGraphStr,
                    PackageResolutionResponse.class);
            resolutionResponse.resolved().getFirst().setDeprecated(getDeprecationStatus(groupId, artifactId, version,
                    Paths.get(targetLocation)));
            return resolutionResponse;
        } catch (ArtifactResolutionException | IOException e) {
            throw new MavenResolverClientException(e.getMessage());
        }
    }

    /**
     * Get package metadata from the Maven repository, including all versions and their details, by parsing the
     * maven-metadata.xml file.
     *
     * @param groupId       group ID of the package
     * @param artifactId    artifact ID of the package
     * @param localRepoPath path to the local Maven repository
     * @return PackageMetadata object containing parsed XML metadata
     * @throws Exception when metadata resolution or XML parsing fails
     */
    private PackageMavenMetadata getPackageMetadata(String groupId, String artifactId, Path localRepoPath)
            throws MavenResolverClientException {
        LocalRepository localRepo = new LocalRepository(localRepoPath.toAbsolutePath().toString());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setOffline(false);
        session.setUpdatePolicy("interval:10"); // TODO :  use an interval as "interval:1"
        session.setChecksumPolicy(org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_IGNORE);
        Metadata metadata = new DefaultMetadata(
                groupId,
                artifactId,
                "maven-metadata.xml",
                Metadata.Nature.RELEASE_OR_SNAPSHOT
        );
        MetadataRequest metadataRequest = new MetadataRequest(
                metadata,
                this.repository.build(),
                null
        );
        MetadataResult result = system.resolveMetadata(
                session,
                Collections.singletonList(metadataRequest)
        ).get(0);
        Metadata metadataResult = result.getMetadata();
        
        // Parse the XML file
        File metadataFile = metadataResult.getFile();
        if (metadataFile != null && metadataFile.exists()) {
            try {
                Document document = parseXmlFile(metadataFile);
                return parsePackageMetadata(document, groupId, artifactId);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                throw new MavenResolverClientException("Failed to parse metadata XML: " + e.getMessage());
            }
        }
        throw new MavenResolverClientException("Metadata file not found or could not be resolved");
    }

    /**
     * Get tool metadata from the Maven repository, including all versions and their details, by parsing the
     * maven-metadata.xml file.
     *
     * @param toolId        tool ID of the tool
     * @param localRepoPath path to the local Maven repository
     * @return ToolMavenMetadata object containing parsed XML metadata
     * @throws MavenResolverClientException when metadata resolution or XML parsing fails
     */
    private ToolMavenMetadata getToolMetadata(String toolId, Path localRepoPath)
            throws MavenResolverClientException {
        LocalRepository localRepo = new LocalRepository(localRepoPath.toAbsolutePath().toString());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setOffline(false);
        session.setUpdatePolicy("interval:10"); // TODO :  use an interval as "interval:1"
        session.setChecksumPolicy(org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_IGNORE);
        Metadata metadata = new DefaultMetadata(
                "__tools__",
                toolId,
                "maven-metadata.xml",
                Metadata.Nature.RELEASE_OR_SNAPSHOT
        );
        MetadataRequest metadataRequest = new MetadataRequest(
                metadata,
                this.repository.build(),
                null
        );
        MetadataResult result = system.resolveMetadata(
                session,
                Collections.singletonList(metadataRequest)
        ).get(0);
        Metadata metadataResult = result.getMetadata();

        // Parse the XML file
        File metadataFile = metadataResult.getFile();
        if (metadataFile != null && metadataFile.exists()) {
            try {
                Document document = parseXmlFile(metadataFile);
                return parseToolMetadata(document);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                throw new MavenResolverClientException("Failed to parse metadata XML: " + e.getMessage());
            }
        }
        throw new MavenResolverClientException("Metadata file not found or could not be resolved");
    }

    /**
     * Get tool metadata including organization and name information.
     *
     * @param toolId        tool ID to retrieve metadata for
     * @param localRepoPath path to the local Maven repository
     * @return ToolMavenMetadata containing org, name, and version information
     * @throws MavenResolverClientException when metadata resolution fails
     */
    public ToolMavenMetadata getToolMetadataInfo(String toolId, Path localRepoPath)
            throws MavenResolverClientException {
        // Check cache first
        ToolMavenMetadata metadata = toolMetadataCache.get(toolId);
        if (metadata == null) {
            metadata = getToolMetadata(toolId, localRepoPath);
            toolMetadataCache.put(toolId, metadata);
        }
        return metadata;
    }

    /**
     * Check if the current Ballerina distribution version is compatible with the tool's required distribution version.
     * The current version must have the same major version and a minor version greater than or equal to the tool's required version.
     *
     * @param currentDistVersion  current Ballerina distribution version
     * @param toolDistVersion     tool's required distribution version
     * @return true if compatible, false otherwise
     */
    private boolean isCompatibleWithToolDistVersion(String currentDistVersion, String toolDistVersion) {
        try {
            String[] currentParts = currentDistVersion.split("\\.");
            String[] toolParts = toolDistVersion.split("\\.");

            if (currentParts.length < 2 || toolParts.length < 2) {
                // If version format is unexpected, fall back to exact match
                return currentDistVersion.equals(toolDistVersion);
            }

            int currentMajor = Integer.parseInt(currentParts[0]);
            int currentMinor = Integer.parseInt(currentParts[1]);
            int toolMajor = Integer.parseInt(toolParts[0]);
            int toolMinor = Integer.parseInt(toolParts[1]);

            return currentMajor == toolMajor && currentMinor >= toolMinor;
        } catch (Exception e) {
            // If version parsing fails, fall back to exact match
            return currentDistVersion.equals(toolDistVersion);
        }
    }

    /**
     * Get all compatible tool versions that match the given Ballerina version.
     * Returns all tool version strings that are compatible with the specified Ballerina version.
     *
     * @param toolId            tool ID to resolve
     * @param ballerinaVersion  Ballerina version to match
     * @param localRepoPath     path to the local Maven repository
     * @return list of version strings that match the Ballerina version
     * @throws MavenResolverClientException when metadata resolution fails
     */
    public List<String> getCompatibleToolVersions(String toolId, String ballerinaVersion, Path localRepoPath)
            throws MavenResolverClientException {
        ToolMavenMetadata metadata = toolMetadataCache.get(toolId);
        if (metadata == null) {
            metadata = getToolMetadata(toolId, localRepoPath);
            toolMetadataCache.put(toolId, metadata);
        }
        return metadata.getVersions().stream()
                .filter(v -> isCompatibleWithToolDistVersion(ballerinaVersion, v.getBallerinaVersion()))
                .map(ToolVersion::getVersion)
                .toList();
    }


    /**
     * Get package search metadata from the Maven repository, including all packages and their details, by parsing the
     * maven-metadata.xml file.
     *
     * @param query    artifact ID of the package
     * @param localRepoPath path to the local Maven repository
     * @return PkgSearchMavenMetadata object containing parsed XML metadata
     * @throws MavenResolverClientException when metadata resolution or XML parsing fails
     */
    public PkgSearchMavenMetadata getPkgSearchMetadata(String query, Path localRepoPath)
            throws MavenResolverClientException {
        LocalRepository localRepo = new LocalRepository(localRepoPath.toAbsolutePath().toString());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setOffline(false);
        session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS); // TODO :  use an interval as "interval:1"
        session.setChecksumPolicy(org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_IGNORE);
        Metadata metadata = new DefaultMetadata(
                "__packagesearch__",
                query,
                "maven-metadata.xml",
                Metadata.Nature.RELEASE_OR_SNAPSHOT
        );
        MetadataRequest metadataRequest = new MetadataRequest(
                metadata,
                this.repository.build(),
                null
        );
        MetadataResult result = system.resolveMetadata(
                session,
                Collections.singletonList(metadataRequest)
        ).get(0);
        Metadata metadataResult = result.getMetadata();

        // Parse the XML file
        File metadataFile = metadataResult.getFile();
        if (metadataFile != null && metadataFile.exists()) {
            try {
                Document document = parseXmlFile(metadataFile);
                return parsePkgSearchMetadata(document, "__packagesearch__", query);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                throw new MavenResolverClientException("Failed to parse metadata XML: " + e.getMessage());
            }
        }
        throw new MavenResolverClientException("Metadata file not found or could not be resolved");
    }

    /**
     * Resolve and retrieve tool search metadata from the remote Maven repository.
     *
     * @param query    artifact ID of the tool search query
     * @param localRepoPath path to the local Maven repository
     * @return ToolSearchMavenMetadata object containing parsed XML metadata
     * @throws MavenResolverClientException when metadata resolution or XML parsing fails
     */
    public ToolSearchMavenMetadata getToolSearchMetadata(String query, Path localRepoPath)
            throws MavenResolverClientException {
        LocalRepository localRepo = new LocalRepository(localRepoPath.toAbsolutePath().toString());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setOffline(false);
        session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
        session.setChecksumPolicy(org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_IGNORE);
        Metadata metadata = new DefaultMetadata(
                "__toolsearch__",
                query,
                "maven-metadata.xml",
                Metadata.Nature.RELEASE_OR_SNAPSHOT
        );
        MetadataRequest metadataRequest = new MetadataRequest(
                metadata,
                this.repository.build(),
                null
        );
        MetadataResult result = system.resolveMetadata(
                session,
                Collections.singletonList(metadataRequest)
        ).get(0);
        Metadata metadataResult = result.getMetadata();

        // Parse the XML file
        File metadataFile = metadataResult.getFile();
        if (metadataFile != null && metadataFile.exists()) {
            try {
                Document document = parseXmlFile(metadataFile);
                return parseToolSearchMetadata(document, "__toolsearch__", query);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                throw new MavenResolverClientException("Failed to parse metadata XML: " + e.getMessage());
            }
        }
        throw new MavenResolverClientException("Metadata file not found or could not be resolved");
    }

    /**
     * Parse the XML Document into a PackageMetadata object.
     *
     * @param document the XML document
     * @param groupId  the group ID
     * @param artifactId the artifact ID
     * @return PackageMetadata object with all parsed data
     */
    private PackageMavenMetadata parsePackageMetadata(Document document, String groupId, String artifactId) {
        PackageMavenMetadata metadata = new PackageMavenMetadata();
        metadata.setGroupId(getTagValue(document, "groupId"));
        metadata.setArtifactId(getTagValue(document, "artifactId"));
        
        // Parse Bversions
        NodeList bversionNodes = document.getElementsByTagName("Bversion");
        List<BVersion> versions = new ArrayList<>();
        
        for (int i = 0; i < bversionNodes.getLength(); i++) {
            Node node = bversionNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element bversionElement = (Element) node;
                BVersion version = parseBVersion(bversionElement);
                versions.add(version);
            }
        }
        
        metadata.setVersions(versions);
        return metadata;
    }

    /**
     * Parse the XML Document into a PkgSearchMavenMetadata object.
     *
     * @param document the XML document
     * @param groupId  the group ID
     * @param artifactId the artifact ID
     * @return PkgSearchMavenMetadata object with all parsed data
     */
    private PkgSearchMavenMetadata parsePkgSearchMetadata(Document document, String groupId, String artifactId) {
        PkgSearchMavenMetadata metadata = new PkgSearchMavenMetadata();
        metadata.setGroupId(getTagValue(document, "groupId"));
        metadata.setArtifactId(getTagValue(document, "artifactId"));
        
        // Parse packages
        NodeList packageNodes = document.getElementsByTagName("package");
        List<Package> packages = new ArrayList<>();
        
        for (int i = 0; i < packageNodes.getLength(); i++) {
            Node node = packageNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element packageElement = (Element) node;
                Package pkg = parsePackage(packageElement);
                packages.add(pkg);
            }
        }
        
        metadata.setPackages(packages);
        return metadata;
    }

    /**
     * Parse the XML Document into a ToolSearchMavenMetadata object.
     *
     * @param document   the XML document
     * @param groupId    the group ID
     * @param artifactId the artifact ID
     * @return ToolSearchMavenMetadata object with all parsed data
     */
    private ToolSearchMavenMetadata parseToolSearchMetadata(Document document, String groupId, String artifactId) {
        ToolSearchMavenMetadata metadata = new ToolSearchMavenMetadata();
        metadata.setGroupId(getTagValue(document, "groupId"));
        metadata.setArtifactId(getTagValue(document, "artifactId"));

        String countStr = getTagValue(document, "count");
        if (countStr != null && !countStr.isEmpty()) {
            try {
                metadata.setCount(Integer.parseInt(countStr.trim()));
            } catch (NumberFormatException e) {
                metadata.setCount(0);
            }
        }
        String limitStr = getTagValue(document, "limit");
        if (limitStr != null && !limitStr.isEmpty()) {
            try {
                metadata.setLimit(Integer.parseInt(limitStr.trim()));
            } catch (NumberFormatException e) {
                metadata.setLimit(0);
            }
        }
        String offsetStr = getTagValue(document, "offset");
        if (offsetStr != null && !offsetStr.isEmpty()) {
            try {
                metadata.setOffset(Integer.parseInt(offsetStr.trim()));
            } catch (NumberFormatException e) {
                metadata.setOffset(0);
            }
        }

        // Parse tools
        NodeList toolNodes = document.getElementsByTagName("tool");
        List<ToolSearchEntry> tools = new ArrayList<>();

        for (int i = 0; i < toolNodes.getLength(); i++) {
            Node node = toolNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element toolElement = (Element) node;
                ToolSearchEntry tool = parseToolSearchEntry(toolElement);
                tools.add(tool);
            }
        }

        metadata.setTools(tools);
        return metadata;
    }

    /**
     * Parse a single tool element into a ToolSearchEntry object.
     *
     * @param toolElement the tool XML element
     * @return ToolSearchEntry object
     */
    private ToolSearchEntry parseToolSearchEntry(Element toolElement) {
        ToolSearchEntry tool = new ToolSearchEntry();
        tool.setOrg(getElementTextContent(toolElement, "org"));
        tool.setName(getElementTextContent(toolElement, "name"));
        tool.setVersion(getElementTextContent(toolElement, "version"));
        tool.setSummary(getElementTextContent(toolElement, "summary"));
        tool.setBalToolId(getElementTextContent(toolElement, "balToolId"));
        String createdDateStr = getElementTextContent(toolElement, "createdDate");
        if (!createdDateStr.isEmpty()) {
            try {
                tool.setCreatedDate(Long.parseLong(createdDateStr));
            } catch (NumberFormatException e) {
                tool.setCreatedDate(0);
            }
        }
        return tool;
    }

    /**
     * Parse a single package element into a Package object.
     *
     * @param packageElement the package XML element
     * @return Package object
     */
    private Package parsePackage(Element packageElement) {
        Package pkg = new Package();
        pkg.setOrg(getElementTextContent(packageElement, "org"));
        pkg.setName(getElementTextContent(packageElement, "name"));
        pkg.setVersion(getElementTextContent(packageElement, "version"));
        pkg.setSummary(getElementTextContent(packageElement, "summary"));
        String createdDateStr = getElementTextContent(packageElement, "createdDate");
        if (!createdDateStr.isEmpty()) {
            try {
                pkg.setCreatedDate(Long.parseLong(createdDateStr));
            } catch (NumberFormatException e) {
                pkg.setCreatedDate(0);
            }
        }

        List<String> authors = new ArrayList<>();
        Element authorsElement = (Element) packageElement.getElementsByTagName("authors").item(0);
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

    /**
     * Parse a single Bversion element into a BVersion object.
     *
     * @param bversionElement the Bversion XML element
     * @return BVersion object
     */
    private BVersion parseBVersion(Element bversionElement) {
        BVersion version = new BVersion();
        version.setNumber(getElementTextContent(bversionElement, "number"));
        version.setPlatform(getElementTextContent(bversionElement, "platform"));
        version.setLanguageSpecificationVersion(getElementTextContent(bversionElement, "languageSpecificationVersion"));
        version.setIsDeprecated(Boolean.parseBoolean(getElementTextContent(bversionElement, "isDeprecated")));
        version.setDeprecateMessage(getElementTextContent(bversionElement, "deprecateMessage"));
        version.setBallerinaVersion(getElementTextContent(bversionElement, "ballerinaVersion"));
        version.setBalToolId(getElementTextContent(bversionElement, "balToolId"));
        version.setGraalvmCompatible(getElementTextContent(bversionElement, "graalvmCompatible"));
        
        // Parse modules
        List<Module> modules = new ArrayList<>();
        NodeList moduleNodes = bversionElement.getElementsByTagName("module");
        for (int i = 0; i < moduleNodes.getLength(); i++) {
            Node moduleNode = moduleNodes.item(i);
            if (moduleNode.getNodeType() == Node.ELEMENT_NODE) {
                Element moduleElement = (Element) moduleNode;
                Module module = new Module();
                module.setName(moduleElement.getTextContent());
                modules.add(module);
            }
        }
        version.setModules(modules);
        
        return version;
    }

    /**
     * Parse the XML Document into a ToolMavenMetadata object.
     * Handles the tool-specific metadata XML structure.
     *
     * @param document the XML document
     * @return ToolMavenMetadata object with all parsed data
     */
    private ToolMavenMetadata parseToolMetadata(Document document) {
        ToolMavenMetadata metadata = new ToolMavenMetadata();
        metadata.setGroupId(getTagValue(document, "groupId"));
        metadata.setArtifactId(getTagValue(document, "artifactId"));
        metadata.setOrg(getTagValue(document, "org"));
        metadata.setName(getTagValue(document, "package"));

        // Parse versions
        NodeList versionNodes = document.getElementsByTagName("version");
        List<ToolVersion> versions = new ArrayList<>();

        for (int i = 0; i < versionNodes.getLength(); i++) {
            Node node = versionNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element versionElement = (Element) node;
                String version = getElementTextContent(versionElement, "number");
                String platform = getElementTextContent(versionElement, "platform");
                String ballerinaVersion = getElementTextContent(versionElement, "ballerinaVersion");

                if (!version.isEmpty()) {
                    versions.add(new ToolVersion(version, platform, ballerinaVersion));
                }
            }
        }

        metadata.setVersions(versions);
        return metadata;
    }

    /**
     * Helper method to get text content from a child element.
     *
     * @param parentElement the parent element
     * @param tagName the child tag name
     * @return the text content of the child element, or empty string if not found
     */
    private String getElementTextContent(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent().trim();
        }
        return "";
    }

    /**
     * Parse XML file and return a Document object.
     *
     * @param xmlFile the XML file to parse
     * @return Document object containing parsed XML
     * @throws ParserConfigurationException if parser configuration fails
     * @throws IOException                  if file reading fails
     * @throws SAXException                 if XML parsing fails
     */
    private Document parseXmlFile(File xmlFile) 
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }

    /**
     * Helper method to get tag value from Document.
     *
     * @param document the XML document
     * @param tagName  the tag name to retrieve
     * @return the text content of the tag, or null if not found
     */
    public String getTagValue(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return node.getTextContent();
            }
        }
        return null;
    }

    /**
     * Helper method to get all values for a specific tag.
     *
     * @param document the XML document
     * @param tagName  the tag name to retrieve
     * @return list of text contents for all matching tags
     */
    public List<String> getAllTagValues(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);
        List<String> values = new java.util.ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                values.add(node.getTextContent());
            }
        }
        return values;
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

    /**
     * Data class representing package metadata from the Maven XML.
     */
    static class PackageMavenMetadata {
        private String groupId;
        private String artifactId;
        private List<BVersion> versions;

        public PackageMavenMetadata() {
            this.versions = new ArrayList<>();
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public List<BVersion> getVersions() {
            return versions;
        }

        public void setVersions(List<BVersion> versions) {
            this.versions = versions;
        }

        @Override
        public String toString() {
            return "PackageMetadata{" +
                    "groupId='" + groupId + '\'' +
                    ", artifactId='" + artifactId + '\'' +
                    ", versions=" + versions +
                    '}';
        }
    }

    /**
     * Data class representing tool metadata from the Maven XML.
     */
    public static class ToolMavenMetadata {
        private String groupId;
        private String artifactId;
        private String org;
        private String name;
        private List<ToolVersion> versions;

        public ToolMavenMetadata() {
            this.versions = new ArrayList<>();
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getOrg() {
            return org;
        }

        public void setOrg(String org) {
            this.org = org;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<ToolVersion> getVersions() {
            return versions;
        }

        public void setVersions(List<ToolVersion> versions) {
            this.versions = versions;
        }

        @Override
        public String toString() {
            return "ToolMavenMetadata{" +
                    "groupId='" + groupId + '\'' +
                    ", artifactId='" + artifactId + '\'' +
                    ", org='" + org + '\'' +
                    ", name='" + name + '\'' +
                    ", versions=" + versions +
                    '}';
        }
    }

    /**
     * Data class representing a single tool version.
     */
    public static class ToolVersion {
        private String version;
        private String platform;
        private String ballerinaVersion;

        public ToolVersion(String version, String platform, String ballerinaVersion) {
            this.version = version;
            this.platform = platform;
            this.ballerinaVersion = ballerinaVersion;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getBallerinaVersion() {
            return ballerinaVersion;
        }

        public void setBallerinaVersion(String ballerinaVersion) {
            this.ballerinaVersion = ballerinaVersion;
        }

        @Override
        public String toString() {
            return "ToolVersion{" +
                    "version='" + version + '\'' +
                    ", platform='" + platform + '\'' +
                    ", ballerinaVersion='" + ballerinaVersion + '\'' +
                    '}';
        }
    }

    /**
     * Data class representing package search metadata from the Maven XML.
     */
    public static class PkgSearchMavenMetadata {
        private String groupId;
        private String artifactId;
        private List<Package> packages;

        public PkgSearchMavenMetadata() {
            this.packages = new ArrayList<>();
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public List<Package> getPackages() {
            return packages;
        }

        public void setPackages(List<Package> packages) {
            this.packages = packages;
        }

        @Override
        public String toString() {
            return "PkgSearchMavenMetadata{" +
                    "groupId='" + groupId + '\'' +
                    ", artifactId='" + artifactId + '\'' +
                    ", packages=" + packages +
                    '}';
        }
    }

    /**
     * Data class representing tool search metadata from the Maven XML.
     */
    public static class ToolSearchMavenMetadata {
        private String groupId;
        private String artifactId;
        private List<ToolSearchEntry> tools;
        private int count;
        private int limit;
        private int offset;

        public ToolSearchMavenMetadata() {
            this.tools = new ArrayList<>();
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public List<ToolSearchEntry> getTools() {
            return tools;
        }

        public void setTools(List<ToolSearchEntry> tools) {
            this.tools = tools;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        @Override
        public String toString() {
            return "ToolSearchMavenMetadata{" +
                    "groupId='" + groupId + '\'' +
                    ", artifactId='" + artifactId + '\'' +
                    ", tools=" + tools +
                    ", count=" + count +
                    ", limit=" + limit +
                    ", offset=" + offset +
                    '}';
        }
    }

    /**
     * Data class representing a single tool in the tool search metadata.
     */
    public static class ToolSearchEntry {
        private String org;
        private String name;
        private String version;
        private String summary;
        private long createdDate;
        private String balToolId;

        public ToolSearchEntry() {
        }

        public String getOrg() {
            return org;
        }

        public void setOrg(String org) {
            this.org = org;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public long getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(long createdDate) {
            this.createdDate = createdDate;
        }

        public String getBalToolId() {
            return balToolId;
        }

        public void setBalToolId(String balToolId) {
            this.balToolId = balToolId;
        }

        @Override
        public String toString() {
            return "ToolSearchEntry{" +
                    "org='" + org + '\'' +
                    ", name='" + name + '\'' +
                    ", version='" + version + '\'' +
                    ", summary='" + summary + '\'' +
                    ", createdDate=" + createdDate +
                    ", balToolId='" + balToolId + '\'' +
                    '}';
        }
    }

    /**
     * Data class representing a package in the package search metadata.
     */
    public static class Package {
        private String org;
        private String name;
        private String version;
        private String summary;
        private long createdDate;
        private List<String> authors;

        public Package() {
        }

        public String getOrg() {
            return org;
        }

        public void setOrg(String org) {
            this.org = org;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        @Override
        public String toString() {
            return "Package{" +
                    "org='" + org + '\'' +
                    ", name='" + name + '\'' +
                    ", version='" + version + '\'' +
                    ", summary='" + summary + '\'' +
                    ", createdDate=" + createdDate +
                    ", authors=" + authors +
                    '}';
        }

        public long getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(long createdDate) {
            this.createdDate = createdDate;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public void setAuthors(List<String> authors) {
            this.authors = authors;
        }
    }

    /**
     * Data class representing a single Ballerina version.
     */
    public static class BVersion {
        private String number;
        private String platform;
        private String languageSpecificationVersion;
        private boolean isDeprecated;
        private String deprecateMessage;
        private String ballerinaVersion;
        private String balToolId;
        private String graalvmCompatible;
        private List<Module> modules;

        public BVersion() {
            this.modules = new ArrayList<>();
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getLanguageSpecificationVersion() {
            return languageSpecificationVersion;
        }

        public void setLanguageSpecificationVersion(String languageSpecificationVersion) {
            this.languageSpecificationVersion = languageSpecificationVersion;
        }

        public boolean isDeprecated() {
            return isDeprecated;
        }

        public void setIsDeprecated(boolean deprecated) {
            isDeprecated = deprecated;
        }

        public String getDeprecateMessage() {
            return deprecateMessage;
        }

        public void setDeprecateMessage(String deprecateMessage) {
            this.deprecateMessage = deprecateMessage;
        }

        public String getBallerinaVersion() {
            return ballerinaVersion;
        }

        public void setBallerinaVersion(String ballerinaVersion) {
            this.ballerinaVersion = ballerinaVersion;
        }

        public String getBalToolId() {
            return balToolId;
        }

        public void setBalToolId(String balToolId) {
            this.balToolId = balToolId;
        }

        public String getGraalvmCompatible() {
            return graalvmCompatible;
        }

        public void setGraalvmCompatible(String graalvmCompatible) {
            this.graalvmCompatible = graalvmCompatible;
        }

        public List<Module> getModules() {
            return modules;
        }

        public void setModules(List<Module> modules) {
            this.modules = modules;
        }

        @Override
        public String toString() {
            return "BVersion{" +
                    "number='" + number + '\'' +
                    ", platform='" + platform + '\'' +
                    ", languageSpecificationVersion='" + languageSpecificationVersion + '\'' +
                    ", isDeprecated=" + isDeprecated +
                    ", ballerinaVersion='" + ballerinaVersion + '\'' +
                    ", graalvmCompatible='" + graalvmCompatible + '\'' +
                    ", modules=" + modules +
                    '}';
        }
    }

    /**
     * Data class representing a module within a Ballerina version.
     */
    static class Module {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Module{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
}

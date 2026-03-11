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
    Map<String, PackageMavenMetadata> metadataCache = new HashMap<>();

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
                    .map(org.eclipse.aether.version.Version::toString)
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
            if (!metadataCache.containsKey(cacheKey)) {
                PackageMavenMetadata metadata = getPackageMetadata(groupId, artifactId, localRepoPath);
                metadataCache.put(cacheKey, metadata);
            }
            return metadataCache.get(cacheKey).getVersions().stream()
                    .map(Version::getVersion)
                    .collect(Collectors.toList());
        } catch (MavenResolverClientException e) {
                    throw new MavenResolverClientException("Failed to get package metadata: " + e.getMessage());
        }
    }

    public String getBallerinaVersionForPackage(String org, String pkgName, String version, Path localRepoPath)
            throws MavenResolverClientException {
        try {
            String cacheKey = org + ":" + pkgName;
            if (!metadataCache.containsKey(cacheKey)) {
                PackageMavenMetadata metadata = getPackageMetadata(org, pkgName, localRepoPath);
                metadataCache.put(cacheKey, metadata);
            }
            return metadataCache.get(cacheKey).getVersions().stream().filter(v -> v.getVersion().equals(version))
                    .map(Version::getBallerinaVersion)
                    .toList().getFirst();
        } catch (MavenResolverClientException e) {
            throw new MavenResolverClientException("Failed to get package metadata: " + e.getMessage());
        }
    }

    public boolean getDeprecationStatus(String org, String pkgName, String version, Path localRepoPath)
            throws MavenResolverClientException {
        try {
            String cacheKey = org + ":" + pkgName;
            if (!metadataCache.containsKey(cacheKey)) {
                PackageMavenMetadata metadata = getPackageMetadata(org, pkgName, localRepoPath);
                metadataCache.put(cacheKey, metadata);
            }
            return metadataCache.get(cacheKey).getVersions().stream().filter(v -> v.getVersion().equals(version))
                    .map(Version::isDeprecated)
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

        // Parse versions from <versions><version>...</version></versions>
        List<Version> versions = new ArrayList<>();
        NodeList versionsNodes = document.getElementsByTagName("versions");
        if (versionsNodes.getLength() > 0 && versionsNodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
            Element versionsElement = (Element) versionsNodes.item(0);
            NodeList versionNodes = versionsElement.getElementsByTagName("version");
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
     * Parse a single version element into a BVersion object.
     *
     * @param versionElement the version XML element
     * @return BVersion object
     */
    private Version parseVersion(Element versionElement) {
        Version version = new Version();
        version.setVersion(getElementTextContent(versionElement, "number"));
        version.setPlatform(getElementTextContent(versionElement, "platform"));
        version.setIsDeprecated(Boolean.parseBoolean(getElementTextContent(versionElement, "isDeprecated")));
        version.setBallerinaVersion(getElementTextContent(versionElement, "ballerinaVersion"));
        return version;
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
        private List<Version> versions;

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

        public List<Version> getVersions() {
            return versions;
        }

        public void setVersions(List<Version> versions) {
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
    static class Version {
        private String version;
        private String platform;
        private boolean isDeprecated;
        private String ballerinaVersion;

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

        public boolean isDeprecated() {
            return isDeprecated;
        }

        public void setIsDeprecated(boolean deprecated) {
            isDeprecated = deprecated;
        }

        public String getBallerinaVersion() {
            return ballerinaVersion;
        }

        public void setBallerinaVersion(String ballerinaVersion) {
            this.ballerinaVersion = ballerinaVersion;
        }

        @Override
        public String toString() {
            return "Version{" +
                    "version='" + version + '\'' +
                    ", platform='" + platform + '\'' +
                    ", isDeprecated=" + isDeprecated +
                    ", ballerinaVersion='" + ballerinaVersion + '\'' +
                    '}';
        }
    }
}

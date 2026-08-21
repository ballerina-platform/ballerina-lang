/*
 *  Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.projects.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ballerina.projects.CompilerBackend;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.JarLibrary;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PlatformLibrary;
import io.ballerina.projects.PlatformLibraryScope;
import io.ballerina.projects.ResolvedPackageDependency;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Generates a CycloneDX JSON BOM (specVersion 1.6) for a Ballerina package.
 *
 * <p>The BOM is built from the resolved dependency graph and the platform libraries the bala writer packages, so
 * it describes what the bala actually ships.</p>
 *
 * @since 2201.14.0
 */
public final class SbomGenerator {

    private static final String BOM_FORMAT = "CycloneDX";
    private static final String SPEC_VERSION = "1.6";
    private static final String PURL_PREFIX_BALLERINA = "pkg:ballerina/";
    private static final String PURL_PREFIX_MAVEN = "pkg:maven/";
    private static final String JAR_REF_PREFIX = "ballerina:jar/";
    private static final String COMPONENT_TYPE_APPLICATION = "application";
    private static final String COMPONENT_TYPE_LIBRARY = "library";
    private static final String HASH_ALG_SHA_256 = "SHA-256";
    private static final String PROP_PLATFORM_SCOPE = "ballerina:platform:scope";
    private static final String PROP_JAR_ROLE = "ballerina:jar:role";

    public static final String ROLE_COMPILER_PLUGIN = "compiler-plugin";
    public static final String ROLE_BAL_TOOL = "bal-tool";

    private SbomGenerator() {
    }

    /**
     * Build a CycloneDX BOM for the given resolved dependency graph.
     *
     * @param dependencyGraph resolved package dependency graph
     * @param backend         backend reporting the platform libraries packaged for each package, or {@code null}
     * @return the BOM as pretty-printed JSON, or {@code null} if the graph has no root package to describe
     */
    public static String generateBom(DependencyGraph<ResolvedPackageDependency> dependencyGraph,
                                     CompilerBackend backend) {
        return generateBom(dependencyGraph, backend, Map.of());
    }

    /**
     * Build a CycloneDX BOM for the given resolved dependency graph, including JARs bundled into the bala outside
     * {@code platform/}.
     *
     * @param dependencyGraph   resolved package dependency graph
     * @param backend           backend reporting the platform libraries packaged for each package, or {@code null}
     * @param bundledJarsByRole JARs the bala ships outside {@code platform/}, keyed by role
     *                          ({@link #ROLE_COMPILER_PLUGIN}, {@link #ROLE_BAL_TOOL})
     * @return the BOM as pretty-printed JSON, or {@code null} if the graph has no root package to describe
     */
    public static String generateBom(DependencyGraph<ResolvedPackageDependency> dependencyGraph,
                                     CompilerBackend backend,
                                     Map<String, ? extends Collection<Path>> bundledJarsByRole) {
        if (dependencyGraph == null || dependencyGraph.isEmpty()) {
            return null;
        }
        ResolvedPackageDependency rootDependency = dependencyGraph.getRoot();
        if (rootDependency == null) {
            return null;
        }
        PackageDescriptor rootDescriptor = rootDependency.packageInstance().descriptor();
        String rootPurl = ballerinaPurl(rootDescriptor);

        Map<String, Map<String, Object>> componentsByRef = new TreeMap<>();
        Map<String, Set<String>> dependsOnByRef = new TreeMap<>();


        List<ResolvedPackageDependency> sortedNodes = new ArrayList<>(dependencyGraph.getNodes());
        sortedNodes.sort(Comparator.comparing(node -> ballerinaPurl(node.packageInstance().descriptor())));

        for (ResolvedPackageDependency node : sortedNodes) {
            if (node.scope() == PackageDependencyScope.TEST_ONLY) {
                continue;
            }

            Package nodePackage = node.packageInstance();
            PackageDescriptor descriptor = nodePackage.descriptor();
            String purl = ballerinaPurl(descriptor);

            if (!purl.equals(rootPurl)) {
                componentsByRef.computeIfAbsent(purl, key -> ballerinaComponent(descriptor, purl));
            }

            Set<String> dependsOn = dependsOnByRef.computeIfAbsent(purl, key -> new TreeSet<>());
            for (ResolvedPackageDependency directDependency : dependencyGraph.getDirectDependencies(node)) {
                if (directDependency.scope() == PackageDependencyScope.TEST_ONLY) {
                    continue;
                }
                dependsOn.add(ballerinaPurl(directDependency.packageInstance().descriptor()));
            }

            collectPlatformDependencies(nodePackage, backend, componentsByRef, dependsOnByRef, dependsOn);

            if (purl.equals(rootPurl)) {
                collectBundledJars(nodePackage, bundledJarsByRole, componentsByRef, dependsOnByRef, dependsOn);
            }
        }

        return buildBomJson(rootDescriptor, rootPurl, componentsByRef, dependsOnByRef);
    }

    /**
     * Collect the Java platform libraries packaged for a package and attach them to that package's dependsOn set.
     *
     * @param pkg             package whose platform libraries are being collected
     * @param backend         backend reporting the packaged platform libraries, or {@code null} to skip this step
     * @param componentsByRef accumulating component map, keyed by bom-ref
     * @param dependsOnByRef  accumulating dependency map, so each library also gets its own leaf entry
     * @param dependsOn       dependsOn set of the declaring package
     */
    private static void collectPlatformDependencies(Package pkg,
                                                    CompilerBackend backend,
                                                    Map<String, Map<String, Object>> componentsByRef,
                                                    Map<String, Set<String>> dependsOnByRef,
                                                    Set<String> dependsOn) {
        if (backend == null) {
            return;
        }
        Collection<PlatformLibrary> platformLibraries = backend.platformLibraryDependencies(pkg.packageId());
        for (PlatformLibrary platformLibrary : platformLibraries) {
            if (!(platformLibrary instanceof JarLibrary jarLibrary)) {
                continue;
            }
            if (jarLibrary.scope() == PlatformLibraryScope.TEST_ONLY) {
               continue;
            }

            String ref = jarRef(jarLibrary, pkg);
            componentsByRef.computeIfAbsent(ref, key -> jarComponent(jarLibrary, ref));

            dependsOnByRef.computeIfAbsent(ref, key -> new TreeSet<>());
            dependsOn.add(ref);
        }
    }

    /**
     * Collect the JARs the bala ships outside {@code platform/} and attach them to the root package.
     *
     * @param rootPackage       the package this bala describes
     * @param bundledJarsByRole JARs keyed by role; may be empty
     * @param componentsByRef   accumulating component map, keyed by bom-ref
     * @param dependsOnByRef    accumulating dependency map, so each JAR also gets its own leaf entry
     * @param dependsOn         dependsOn set of the root package
     */
    private static void collectBundledJars(Package rootPackage,
                                           Map<String, ? extends Collection<Path>> bundledJarsByRole,
                                           Map<String, Map<String, Object>> componentsByRef,
                                           Map<String, Set<String>> dependsOnByRef,
                                           Set<String> dependsOn) {
        if (bundledJarsByRole == null || bundledJarsByRole.isEmpty()) {
            return;
        }
        for (String role : new TreeSet<>(bundledJarsByRole.keySet())) {
            Collection<Path> jarPaths = bundledJarsByRole.get(role);
            if (jarPaths == null) {
                continue;
            }
            for (Path jarPath : jarPaths) {
                if (jarPath == null) {
                    continue;
                }
                Path name = jarPath.getFileName();
                String fileName = name != null ? name.toString() : jarPath.toString();
                String ref = JAR_REF_PREFIX + rootPackage.packageOrg().value() + "/"
                        + rootPackage.packageName().value() + "/" + role + "/" + fileName;
                componentsByRef.computeIfAbsent(ref, key -> bundledJarComponent(fileName, ref, role, jarPath));
                dependsOnByRef.computeIfAbsent(ref, key -> new TreeSet<>());
                dependsOn.add(ref);
            }
        }
    }

    /**
     * Build a component entry for a JAR bundled outside {@code platform/}.
     *
     * @param fileName file name of the JAR
     * @param ref      bom-ref of the JAR
     * @param role     role of the JAR within the bala
     * @param jarPath  path to the JAR on disk, used to hash it
     * @return CycloneDX component
     */
    private static Map<String, Object> bundledJarComponent(String fileName, String ref, String role, Path jarPath) {
        Map<String, Object> component = new LinkedHashMap<>();
        component.put("type", COMPONENT_TYPE_LIBRARY);
        component.put("bom-ref", ref);
        component.put("name", fileName);

        String sha256 = sha256(jarPath);
        if (sha256 != null) {
            Map<String, Object> hash = new LinkedHashMap<>();
            hash.put("alg", HASH_ALG_SHA_256);
            hash.put("content", sha256);
            component.put("hashes", List.of(hash));
        }

        Map<String, Object> property = new LinkedHashMap<>();
        property.put("name", PROP_JAR_ROLE);
        property.put("value", role);
        component.put("properties", List.of(property));
        return component;
    }

    /**
     * Assemble the CycloneDX BOM document.
     *
     * @param rootDescriptor  descriptor of the package the BOM describes
     * @param rootPurl        purl of the root package, also used as its bom-ref
     * @param componentsByRef components excluding the root, keyed by bom-ref
     * @param dependsOnByRef  dependency edges, keyed by the bom-ref of the dependent
     * @return pretty-printed CycloneDX JSON
     */
    private static String buildBomJson(PackageDescriptor rootDescriptor,
                                       String rootPurl,
                                       Map<String, Map<String, Object>> componentsByRef,
                                       Map<String, Set<String>> dependsOnByRef) {
        Map<String, Object> bom = new LinkedHashMap<>();
        bom.put("bomFormat", BOM_FORMAT);
        bom.put("specVersion", SPEC_VERSION);

        bom.put("serialNumber", "urn:uuid:" + UUID.nameUUIDFromBytes(rootPurl.getBytes(StandardCharsets.UTF_8)));
        bom.put("version", 1);

        Map<String, Object> rootComponent = new LinkedHashMap<>();
        rootComponent.put("type", COMPONENT_TYPE_APPLICATION);
        rootComponent.put("bom-ref", rootPurl);
        rootComponent.put("group", rootDescriptor.org().value());
        rootComponent.put("name", rootDescriptor.name().value());
        rootComponent.put("version", rootDescriptor.version().toString());
        rootComponent.put("purl", rootPurl);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("component", rootComponent);
        bom.put("metadata", metadata);

        bom.put("components", new ArrayList<>(componentsByRef.values()));

        List<Map<String, Object>> dependencies = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : dependsOnByRef.entrySet()) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("ref", entry.getKey());
            node.put("dependsOn", new ArrayList<>(entry.getValue()));
            dependencies.add(node);
        }
        bom.put("dependencies", dependencies);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(bom);
    }

    /**
     * Build a component entry for a Ballerina package.
     *
     * @param descriptor package descriptor
     * @param purl       purl of the package, also used as its bom-ref
     * @return CycloneDX component
     */
    private static Map<String, Object> ballerinaComponent(PackageDescriptor descriptor, String purl) {
        Map<String, Object> component = new LinkedHashMap<>();
        component.put("type", COMPONENT_TYPE_LIBRARY);
        component.put("bom-ref", purl);
        component.put("group", descriptor.org().value());
        component.put("name", descriptor.name().value());
        component.put("version", descriptor.version().toString());
        component.put("purl", purl);
        return component;
    }

    /**
     * Build a component entry for a Java platform library.
     *
     * @param jarLibrary packaged JAR library
     * @param ref        bom-ref of the library
     * @return CycloneDX component
     */
    private static Map<String, Object> jarComponent(JarLibrary jarLibrary, String ref) {
        Map<String, Object> component = new LinkedHashMap<>();
        component.put("type", COMPONENT_TYPE_LIBRARY);
        component.put("bom-ref", ref);

        Optional<String> groupId = jarLibrary.groupId();
        Optional<String> artifactId = jarLibrary.artifactId();
        Optional<String> version = jarLibrary.version();
        if (groupId.isPresent() && artifactId.isPresent() && version.isPresent()) {
            component.put("group", groupId.get());
            component.put("name", artifactId.get());
            component.put("version", version.get());
            component.put("purl", mavenPurl(groupId.get(), artifactId.get(), version.get()));
        } else {
            component.put("name", fileName(jarLibrary));
        }

        String sha256 = sha256(jarLibrary.path());
        if (sha256 != null) {
            Map<String, Object> hash = new LinkedHashMap<>();
            hash.put("alg", HASH_ALG_SHA_256);
            hash.put("content", sha256);
            component.put("hashes", List.of(hash));
        }

        if (jarLibrary.scope() == PlatformLibraryScope.PROVIDED) {
            Map<String, Object> property = new LinkedHashMap<>();
            property.put("name", PROP_PLATFORM_SCOPE);
            property.put("value", PlatformLibraryScope.PROVIDED.getStringValue());
            component.put("properties", List.of(property));
        }
        return component;
    }

    /**
     * Build the bom-ref of a platform library.
     *
     * @param jarLibrary packaged JAR library
     * @param pkg        package that declares the library
     * @return bom-ref string
     */
    private static String jarRef(JarLibrary jarLibrary, Package pkg) {
        Optional<String> groupId = jarLibrary.groupId();
        Optional<String> artifactId = jarLibrary.artifactId();
        Optional<String> version = jarLibrary.version();
        if (groupId.isPresent() && artifactId.isPresent() && version.isPresent()) {
            return mavenPurl(groupId.get(), artifactId.get(), version.get());
        }
        return JAR_REF_PREFIX + pkg.packageOrg().value() + "/" + pkg.packageName().value() + "/"
                + fileName(jarLibrary);
    }

    /**
     * Build the purl of a Ballerina package: {@code pkg:ballerina/<org>/<name>@<version>}.
     *
     * @param descriptor package descriptor
     * @return purl string
     */
    private static String ballerinaPurl(PackageDescriptor descriptor) {
        return PURL_PREFIX_BALLERINA + descriptor.org().value() + "/" + descriptor.name().value()
                + "@" + descriptor.version();
    }

    /**
     * Build the purl of a Maven library: {@code pkg:maven/<groupId>/<artifactId>@<version>}.
     *
     * @param groupId    Maven group id
     * @param artifactId Maven artifact id
     * @param version    Maven version
     * @return purl string
     */
    private static String mavenPurl(String groupId, String artifactId, String version) {
        return PURL_PREFIX_MAVEN + groupId + "/" + artifactId + "@" + version;
    }

    /**
     * Resolve the file name of a JAR library.
     *
     * @param jarLibrary packaged JAR library
     * @return file name, or the full path when it has no name element
     */
    private static String fileName(JarLibrary jarLibrary) {
        Path path = jarLibrary.path();
        Path name = path.getFileName();
        return name != null ? name.toString() : path.toString();
    }

    /**
     * Compute the SHA-256 hash of a file.
     *
     * @param path file to hash
     * @return lower-case hex digest, or {@code null} when the file cannot be read or hashed
     */
    private static String sha256(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALG_SHA_256);
            try (InputStream in = Files.newInputStream(path);
                 DigestInputStream digestStream = new DigestInputStream(in, digest)) {
                byte[] buffer = new byte[8192];
                while (digestStream.read(buffer) != -1) {
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            return null;
        }
    }
}

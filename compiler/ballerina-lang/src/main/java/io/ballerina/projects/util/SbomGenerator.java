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

import org.ballerinalang.toml.exceptions.TomlException;
import com.moandjiezana.toml.Toml;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Minimal SBOM generator that follows the CycloneDX standard JSON bom (specVersion 1.4) for a Ballerina project.
 * This implementation avoids external dependencies to keep the change small. It produces a minimal
 * bom.json containing project metadata and component entries for direct dependencies.
 */
public final class SbomGenerator {
    private SbomGenerator() {
    }

    /**
     * Generate a minimal bom.json for the project located at the given manifestPath.
     * The manifestPath should point to a Ballerina.toml (or Dependencies.toml) file.
     * The generated bom.json will be written to outputPath.
     *
     * @param manifestPath path to Ballerina.toml or dependencies toml
     * @param outputPath   path to write bom.json
     * @throws IOException   on IO errors
     * @throws TomlException on parse errors
     */
    public static void generateBom(Path manifestPath, Path outputPath) throws IOException, TomlException {
        // Use a map keyed by purl to keep components unique and ordered
        Map<String, Map<String, Object>> componentsByPurl = new LinkedHashMap<>();
        List<Map<String, Object>> properties = new ArrayList<>();

        // Entries with {ref, dependsOn}
        List<Map<String, Object>> dependencyNodes = new ArrayList<>();

        // First, try to extract dependencies from Ballerina.toml
        Path ballerinaTomlPath = manifestPath.getFileName().toString().equalsIgnoreCase("Ballerina.toml")
                ? manifestPath
                : manifestPath.getParent().resolve("Ballerina.toml");

        if (Files.exists(ballerinaTomlPath)) {
            try {
                Toml toml = new Toml().read(ballerinaTomlPath.toFile());
                // Handle single [package] table (common) or [[package]] tables (array)
                java.util.List<Toml> pkgTables = getTomlTables(toml, "package");
                if (!pkgTables.isEmpty()) {
                    extractPackageMetadata(pkgTables, properties);
                }
                extractMavenDependencies(toml, componentsByPurl, dependencyNodes);
            } catch (IllegalArgumentException e) {
                TomlException te = new TomlException("Required fields are missing in Ballerina.toml at " + ballerinaTomlPath + ": " + e.getMessage());
                te.initCause(e);
                throw te;
            } catch (Exception e) {
                TomlException te = new TomlException("Unable to parse Ballerina.toml at " + ballerinaTomlPath + ": " + e.getMessage());
                te.initCause(e);
                throw te;
            }
        }


        // Extract Ballerina package dependencies from dependencies.toml
        Path dependenciesTomlPath = manifestPath.getParent().resolve("dependencies.toml");
        if (Files.exists(dependenciesTomlPath)) {
            try {
                Toml toml = new Toml().read(dependenciesTomlPath.toFile());
//                Get all package tables
                java.util.List<Toml> pkgTables = getTomlTables(toml, "package");
                if (!pkgTables.isEmpty()) {
//                    Extract and add the package dependencies to the components and dependency graph
                    extractBallerinaDependencies(pkgTables, componentsByPurl, dependencyNodes);
                }
            } catch (Exception e) {
                TomlException te = new TomlException("Unable to parse dependencies.toml at " + dependenciesTomlPath + ": " + e.getMessage());
                te.initCause(e);
                throw te;
            }
        }

        // If no components found via files above, try the provided manifestPath as fallback
        if (componentsByPurl.isEmpty()) {
            try {
                Toml toml = new Toml().read(manifestPath.toFile());

                List<Toml> pkgTables = getTomlTables(toml, "package");
                if (!pkgTables.isEmpty()) {
                    extractBallerinaDependencies(pkgTables, componentsByPurl, dependencyNodes);
                }
            } catch (Exception e) {
                // If manifestPath fails, throw original exception
                TomlException te = new TomlException("Unable to parse TOML file at " + manifestPath + ": " + e.getMessage());
                te.initCause(e);
                throw te;
            }
        }

        // Generate BOM JSON with collected components and dependency graph
        if (!componentsByPurl.isEmpty()) {
            // Merge dependency nodes by 'ref' so a given package has only a single entry with combined dependsOn
            Map<String, LinkedHashSet<String>> mergedDeps = new LinkedHashMap<>();

            for (Map<String, Object> dn : dependencyNodes) {
                String ref = Objects.toString(dn.get("ref"), null);
                if (ref == null) {
                    continue;
                }
                mergedDeps.putIfAbsent(ref, new LinkedHashSet<>());

                // Ensure ref exists as a component (add minimal placeholder when needed)
                if (!componentsByPurl.containsKey(ref)) {
                    //  If any dependency is not included as a component, then add it
                    componentsByPurl.put(ref, parsePurlToComponent(ref));
                }

                Object depsObj = dn.get("dependsOn");
                if (depsObj instanceof List) {
                    for (Object o : (List<?>) depsObj) {
                        String reference = o == null ? null : o.toString();
                        if (reference == null) continue;
                        mergedDeps.get(ref).add(reference);
                        // Ensure each dependency exists as a component placeholder
                        if (!componentsByPurl.containsKey(reference)) {
                            //  Adds any sub-dependency is not included as a component
                            componentsByPurl.put(reference, parsePurlToComponent(reference));
                        }
                    }
                }
            }

            // Collect third-party (Maven) purls so they can be attached to the project root
            List<String> thirdPartyPurls = new ArrayList<>();
            for (String purlKey : componentsByPurl.keySet()) {
                if (purlKey != null && purlKey.startsWith("pkg:maven/")) {
                    thirdPartyPurls.add(purlKey);
                }
            }

            // If we have project metadata, fold third-party purls into the project's merged dependsOn
            String rootPurl = null;
            if (!properties.isEmpty()) {
                Map<String, Object> meta = properties.get(0);
                if (meta != null) {
                    rootPurl = Objects.toString(meta.get("purl"), null);
                }
            }
            if (rootPurl != null && !thirdPartyPurls.isEmpty()) {
                mergedDeps.putIfAbsent(rootPurl, new LinkedHashSet<>());
                mergedDeps.get(rootPurl).addAll(thirdPartyPurls);
            }

            // Build final dependency nodes list from merged map, skipping standalone maven nodes
            List<Map<String, Object>> finalDependencyNodes = new ArrayList<>();
            for (Map.Entry<String, LinkedHashSet<String>> e : mergedDeps.entrySet()) {
                String dnRef = e.getKey();
                if (dnRef != null && dnRef.startsWith("pkg:maven/")) {
                    // skip adding maven components as independent dependency nodes; they are referenced by the project
                    continue;
                }
                Map<String, Object> node = new LinkedHashMap<>();
                node.put("ref", dnRef);
                node.put("dependsOn", new ArrayList<>(e.getValue()));
                finalDependencyNodes.add(node);
            }

            String bomJson = buildBomJson(componentsByPurl, properties, finalDependencyNodes);

            // Ensure output path ends with .cdx.json
            Path finalOutputPath = ensureCdxJsonExtension(outputPath);

            if (finalOutputPath.getParent() != null) {
                Files.createDirectories(finalOutputPath.getParent());
            }

            try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(finalOutputPath))) {
                out.write(bomJson);
            }
        }
    }

    /**
     * Ensure the output path ends with .cdx.json
     */
    private static Path ensureCdxJsonExtension(Path outputPath) {

        Path fileNamePath = outputPath.getFileName();
        if (fileNamePath == null) {
            throw new IllegalArgumentException("Output path must have a file name: " + outputPath);
        }
        String fileName = fileNamePath.toString();

        // Case-insensitive extension handling to avoid appending twice or creating weird filenames
        String fileNameLower = fileName.toLowerCase();
        String cdxJsonFileName;
        if (fileNameLower.endsWith(".cdx.json")) {
            // Already has the correct extension (any case variant)
            cdxJsonFileName = fileName;
        } else if (fileNameLower.endsWith(".json")) {
            // Replace .json with .cdx.json
            cdxJsonFileName = fileName.substring(0, fileName.length() - 5) + ".cdx.json";
        } else {
            // Append .cdx.json
            cdxJsonFileName = fileName + ".cdx.json";
        }

        Path parent = outputPath.getParent();
        if (parent != null) {
            return parent.resolve(cdxJsonFileName);
        }
        return outputPath.resolveSibling(cdxJsonFileName);
    }

    private static void extractPackageMetadata(java.util.List<Toml> toml, List<Map<String, Object>> properties) {
        if (toml == null) {
            return;
        }

        // Loop through the packages and extract each dependency individually
        for (Toml packageToml : toml) {
            String org = packageToml.getString("org");
            String name = packageToml.getString("name");
            String version = packageToml.getString("version");
            if (name != null && version != null) {
                org = org == null ? "" : org;
                Map<String, Object> comp = new java.util.LinkedHashMap<>();
                comp.put("type", "application");
                comp.put("group", org);
                comp.put("name", name);
                comp.put("version", version);
                comp.put("purl", buildPurl(org, name, version));
                properties.add(comp);
            }
            else {
                throw new IllegalArgumentException("Package metadata is incomplete in the TOML file. 'name' and 'version' are required fields.");
            }
        }
    }

    /**
     * Extract Maven dependencies from platform.java*.dependency sections in Ballerina.toml
     */
    private static void extractMavenDependencies(Toml toml, Map<String, Map<String, Object>> componentsByPurl,
                                                 List<Map<String, Object>> dependencyNodes) {
        // Look for all java/maven dependencies
        for (String javaVersion : new String[]{"java8", "java11", "java17", "java21"}) {
            String platformKey = "platform." + javaVersion;
            Toml platformTable = toml.getTable(platformKey);
            if (platformTable != null) {
                // Get the dependency(s) from the platform table
                List<Toml> depTables = getTomlTables(platformTable, "dependency");
                for (Toml depTable : depTables) {
                    // Convert the dependency table to a map
                    Map<String, Object> depMap = depTable.toMap();
                    // Build component and collect dependencies
                    String groupId = safeString(depMap.get("groupId"));
                    String artifactId = safeString(depMap.get("artifactId"));
                    String version = safeString(depMap.get("version"));

                    if (groupId != null && artifactId != null && version != null) {
                        String purl = buildPurl(groupId, artifactId, version);
                        Map<String, Object> comp = new LinkedHashMap<>();
                        comp.put("type", "library");
                        comp.put("group", groupId);
                        comp.put("name", artifactId);
                        comp.put("version", version);
                        comp.put("purl", purl);
                        componentsByPurl.putIfAbsent(purl, comp);

                        Object depsObj = depMap.get("dependencies");
                        if (depsObj == null) {
                            depsObj = depMap.get("dependsOn");
                        }

                        //  Extracts the dependsOn dependencies and store them in the form of purl's
                        List<String> depRefs = extractDependencyRefs(depsObj, componentsByPurl);

                        //  Bring ref & dependsOn dependencies to a single data structure
                        Map<String, Object> depNode = new LinkedHashMap<>();
                        depNode.put("ref", purl);
                        depNode.put("dependsOn", depRefs);
                        dependencyNodes.add(depNode);
                    } else {
                        throw new IllegalArgumentException("Maven dependency is incomplete in the TOML file. 'groupId', 'artifactId', and 'version' are required fields.");
                    }
                }
            }
        }
    }

    /**
     * Extract Ballerina dependencies from [[package]] tables
     */
    private static void extractBallerinaDependencies(java.util.List<Toml> pkgTables,
                                                      Map<String, Map<String, Object>> componentsByPurl,
                                                      List<Map<String, Object>> dependencyNodes) {
        for (Toml pkgToml : pkgTables) {
            String org = pkgToml.getString("org");
            String name = pkgToml.getString("name");
            String version = pkgToml.getString("version");
            if (name != null && version != null) {
                org = org == null ? "" : org;
                String purl = buildPurl(org, name, version);
                Map<String, Object> comp = new java.util.LinkedHashMap<>();
                comp.put("type", "library");
                comp.put("group", org);
                comp.put("name", name);
                comp.put("version", version);
                comp.put("purl", purl);
                componentsByPurl.putIfAbsent(purl, comp);

                // Extract any declared transitive dependencies
                Object depsObj = null;
                // Prefer list form (strings or tables)
                List<?> list = pkgToml.getList("dependencies");
                if (list != null && !list.isEmpty()) {
                    depsObj = list;
                } else {
                    // Try array-of-tables or single table forms
                    List<Toml> tables = getTomlTables(pkgToml, "dependency");
                    if (!tables.isEmpty()) {
                        depsObj = tables;
                    } else {
                        // Alternate key
                        list = pkgToml.getList("dependsOn");
                        if (list != null && !list.isEmpty()) {
                            depsObj = list;
                        } else {
                            tables = getTomlTables(pkgToml, "dependsOn");
                            if (!tables.isEmpty()) {
                                depsObj = tables;
                            }
                        }
                    }
                }
                //  Extracts the dependsOn dependencies and store them in the form of purl's
                List<String> depRefs = extractDependencyRefs(depsObj, componentsByPurl);

                Map<String, Object> depNode = new java.util.LinkedHashMap<>();
                depNode.put("ref", purl);
                depNode.put("dependsOn", depRefs);
                dependencyNodes.add(depNode);
            } else {
                throw new IllegalArgumentException("Ballerina package dependency is incomplete in the TOML file. 'name' and 'version' are required fields.");
            }
        }
    }

    // Find an existing component purl by matching group and name regardless of version
    private static String findComponentPurlByGroupAndName(Map<String, Map<String, Object>> componentsByPurl,
                                                          String group, String name) {
         if (componentsByPurl == null || group == null || name == null) {
             return null;
         }
         //  Loop through each entry
         for (Map.Entry<String, Map<String, Object>> entry : componentsByPurl.entrySet()) {
            Map<String, Object> comp = entry.getValue();
            String compGroup = Objects.toString(comp.get("group"), "");
            String compName = Objects.toString(comp.get("name"), "");
            //  Check if the group and name match
            if (group.equals(compGroup) && name.equals(compName)) {
                //  Returns the key which is the purl
                return entry.getKey();
            }
        }
         return null;
     }

     // Helper: get tables for a key, handling single table vs array-of-tables and simple plural alternates.
     private static List<Toml> getTomlTables(Toml toml, String key) {
        if (toml == null || key == null) {
            return List.of();
        }
        try {
            List<Toml> list = toml.getTables(key);
            if (list != null && !list.isEmpty()) {
                return list;
            }
        } catch (ClassCastException ignored) {
            // fall through
        }
        Toml single = toml.getTable(key);
        if (single != null) {
            return List.of(single);
        }

        // Try a simple plural alternate (dependency -> dependencies, package -> packages)
        String alt = key + "s";
        try {
            List<Toml> list = toml.getTables(alt);
            if (list != null && !list.isEmpty()) {
                return list;
            }
        } catch (ClassCastException ignored) {
            // ignore
        }
        Toml singleAlt = toml.getTable(alt);
        if (singleAlt != null) {
            return List.of(singleAlt);
        }
        return List.of();
    }

    // Helper: defensive string conversion from arbitrary objects
    private static String safeString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    // Parse common dependency string forms into purl
    private static String parseDependencyString(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        s = s.trim();
        // Common maven GAV: groupId:artifactId:version or groupId:artifactId:version:classifier
        String[] colonParts = s.split(":");
        if (colonParts.length >= 2) {
            String group = colonParts[0];
            String artifact = colonParts[1];
            String version = colonParts.length >= 3 ? colonParts[2] : null;
            return buildPurl(group, artifact, version);
        }

        // Ballerina pkg like org/name@version or name@version
        if (s.contains("@")) {
            String[] at = s.split("@", 2);
            String left = at[0];
            String version = at[1];
            if (left.contains("/")) {
                String[] parts = left.split("/", 2);
                String org = parts[0];
                String name = parts[1];
                return buildPurl(org, name, version);
            } else {
                // no org provided
                return buildPurl("", left, version);
            }
        }

        // Maven slash form or simple path-like: group/artifact@version
        if (s.contains("/") && s.contains("@")) {
            int atPos = s.indexOf('@');
            String left = s.substring(0, atPos);
            String version = s.substring(atPos + 1);
            int slash = left.indexOf('/');
            if (slash > 0) {
                String group = left.substring(0, slash);
                String artifact = left.substring(slash + 1);
                return buildPurl(group, artifact, version);
            }
        }

        // As a last resort, return null to avoid malformed purls
        return null;
    }

    // Helper: parse various representations of dependency declarations into purl refs
    private static List<String> extractDependencyRefs(Object depsObj, Map<String, Map<String, Object>> componentsByPurl) {
        List<Object> rawList = new ArrayList<>();
        if (depsObj instanceof java.util.List<?>) {
            rawList.addAll((java.util.List<?>) depsObj);
        } else if (depsObj != null) {
            rawList.add(depsObj);
        }

        List<String> refs = new ArrayList<>();
        if (rawList.isEmpty()) {
            return refs;
        }

        for (Object item : rawList) {
            if (item == null) {
                continue;
            }

            // Inline map form: either Maven-style (groupId/artifactId) or Ballerina (org/name)
            if (item instanceof Map<?, ?>) {
                Map<?, ?> itemMap = (Map<?, ?>) item;
                if (itemMap.containsKey("groupId") || itemMap.containsKey("artifactId")) {
                    String groupIdFromMap = safeString(itemMap.get("groupId"));
                    String artifactIdFromMap = safeString(itemMap.get("artifactId"));
                    String versionFromMap = safeString(itemMap.get("version"));
                    if (groupIdFromMap != null && artifactIdFromMap != null) {
                        refs.add(resolvePurlIfMissingVersion(buildPurl(groupIdFromMap, artifactIdFromMap, versionFromMap), componentsByPurl));
                    }
                } else {
                    String orgFromMap = safeString(itemMap.get("org"));
                    String nameFromMap = safeString(itemMap.get("name"));
                    String versionFromMap = safeString(itemMap.get("version"));
                    if (nameFromMap != null) {
                        refs.add(resolvePurlIfMissingVersion(buildPurl(orgFromMap == null ? "" : orgFromMap, nameFromMap, versionFromMap), componentsByPurl));
                    }
                }
                continue;
            }

            // Toml table node (array-of-tables yields Toml items)
            if (item instanceof Toml) {
                Toml tomlTable = (Toml) item;
                String groupIdFromToml = tomlTable.getString("groupId");
                String artifactIdFromToml = tomlTable.getString("artifactId");
                String versionFromToml = tomlTable.getString("version");
                if (groupIdFromToml != null || artifactIdFromToml != null) {
                    if (groupIdFromToml != null && artifactIdFromToml != null) {
                        refs.add(resolvePurlIfMissingVersion(buildPurl(groupIdFromToml, artifactIdFromToml, versionFromToml), componentsByPurl));
                    }
                } else {
                    String orgFromToml = tomlTable.getString("org");
                    String nameFromToml = tomlTable.getString("name");
                    String versionFromTomlAlt = tomlTable.getString("version");
                    if (nameFromToml != null) {
                        refs.add(resolvePurlIfMissingVersion(buildPurl(orgFromToml == null ? "" : orgFromToml, nameFromToml, versionFromTomlAlt), componentsByPurl));
                    }
                }
                continue;
            }

            // String / simple forms
            if (item instanceof String) {
                String parsed = parseDependencyString((String) item);
                if (parsed != null) {
                    refs.add(resolvePurlIfMissingVersion(parsed, componentsByPurl));
                }
                continue;
            }

            // Fallback: stringify and try to parse
            String parsed = parseDependencyString(item.toString());
            if (parsed != null) {
                refs.add(resolvePurlIfMissingVersion(parsed, componentsByPurl));
            }
        }

        return refs;
    }

    // If a purl lacks a version, try to resolve it to an existing component purl by matching group & name
    private static String resolvePurlIfMissingVersion(String purl, Map<String, Map<String, Object>> componentsByPurl) {
        if (purl == null) {
            return null;
        }
        if (componentsByPurl == null) {
            return purl;
        }
        // If purl already includes a version, keep it
        if (purl.contains("@")) {
            return purl;
        }
        try {
            //  Check if the package is maven or ballerina and try to find a matching component purl by group and name
            if (purl.startsWith("pkg:maven/")) {
                String body = purl.substring("pkg:maven/".length());
                int slash = body.lastIndexOf('/');
                if (slash > 0) {
                    //  Extract group and artifact from the purl
                    String group = body.substring(0, slash);
                    String artifact = body.substring(slash + 1);
                    //  Find the component purl by groupId and artifactName
                    String found = findComponentPurlByGroupAndName(componentsByPurl, group, artifact);
                    if (found != null) {
                        return found;
                    }
                }
            } else if (purl.startsWith("pkg:ballerina/")) {
                String body = purl.substring("pkg:ballerina/".length());
                int slash = body.indexOf('/');
                String group;
                String name;
                if (slash > 0) {
                    group = body.substring(0, slash);
                    name = body.substring(slash + 1);
                } else {
                    group = "";
                    name = body;
                }

                String found = findComponentPurlByGroupAndName(componentsByPurl, group, name);
                if (found != null) {
                    return found;
                }
            }
        } catch (Exception ignored) {
        }
        return purl;
    }

     /**
      * Build the BOM JSON string from components map and dependencies
      */
     private static String buildBomJson(Map<String, Map<String, Object>> componentsByPurl,
                                        List<Map<String, Object>> properties,
                                        List<Map<String, Object>> dependencyNodes) {
         String serial = "urn:uuid:" + UUID.randomUUID();

         Map<String, Object> bom = new java.util.LinkedHashMap<>();
         bom.put("bomFormat", "CycloneDX");
         bom.put("specVersion", "1.4");
         bom.put("serialNumber", serial);

         // Metadata component
         String metaType = "application";
         String metaGroup = "";
         String metaName = "";
         String metaVersion = "";
         if (!properties.isEmpty()) {
             Map<String, Object> meta = properties.get(0);
             if (meta != null) {
                 metaGroup = Objects.toString(meta.get("group"), "");
                 metaName = Objects.toString(meta.get("name"), "");
                 metaVersion = Objects.toString(meta.get("version"), "");
                 metaType = Objects.toString(meta.get("type"), metaType);
             }
         }

         Map<String, Object> metadata = new java.util.LinkedHashMap<>();
         Map<String, Object> metadataComponent = new java.util.LinkedHashMap<>();
         metadataComponent.put("type", metaType);
         metadataComponent.put("group", metaGroup);
         metadataComponent.put("name", metaName);
         metadataComponent.put("version", metaVersion);
         metadata.put("component", metadataComponent);
         bom.put("metadata", metadata);

         // Components
         List<Map<String, Object>> compsList = new ArrayList<>();
         for (Map<String, Object> comp : componentsByPurl.values()) {
             Map<String, Object> c = new java.util.LinkedHashMap<>();
             c.put("type", Objects.toString(comp.get("type"), ""));
             c.put("group", Objects.toString(comp.get("group"), ""));
             c.put("name", Objects.toString(comp.get("name"), ""));
             c.put("version", Objects.toString(comp.get("version"), ""));
             c.put("purl", Objects.toString(comp.get("purl"), ""));
             compsList.add(c);
         }
         bom.put("components", compsList);

         // Dependencies (CycloneDX style: list of {ref, dependsOn: [{ref}]})
         List<Map<String, Object>> depsList = new ArrayList<>();
         for (Map<String, Object> dn : dependencyNodes) {
             Map<String, Object> entry = new LinkedHashMap<>();
             entry.put("ref", Objects.toString(dn.get("ref"), ""));
             // Ensure dependsOn references are present in components; filter nulls
             List<String> dependsOn = new ArrayList<>();
             Object rawDepsObj = dn.get("dependsOn");
             if (rawDepsObj instanceof List) {
                 for (Object o : (List<?>) rawDepsObj) {
                     if (o == null) continue;
                     dependsOn.add(o.toString());
                 }
             }
             entry.put("dependsOn", dependsOn);
             depsList.add(entry);
         }
         bom.put("dependencies", depsList);

         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         return gson.toJson(bom);
     }

     /**
      * Build a Package URL (purl) for a Ballerina package or Maven dependency.
      * Ballerina packages use the format: pkg:ballerina/org/name@version
      * Maven dependencies use the format: pkg:maven/groupId/artifactId@version
      *
      * @param org     organization/groupId (used to determine if this is a Maven or Ballerina package)
      * @param name    package/artifact name
      * @param version package version
      * @return PURL string
      */
     private static String buildPurl(String org, String name, String version) {
         // Determine if this is a Maven dependency or a Ballerina package
         boolean isMavenDependency = org != null && org.contains(".");

         StringBuilder purl = new StringBuilder();
         if (isMavenDependency) {
             // Format for Maven: pkg:maven/groupId/artifactId@version
             purl.append("pkg:maven/").append(org).append("/").append(name);
         } else {
             // Format for Ballerina: pkg:ballerina/org/name@version
             purl.append("pkg:ballerina/");
             if (org != null && !org.isEmpty()) {
                 purl.append(org).append("/");
             }
             purl.append(name);
         }

         if (version != null && !version.isEmpty()) {
             purl.append("@").append(version);
         }
         return purl.toString();
     }

    // Create a minimal component map from a purl string so referenced deps appear in components
    private static Map<String, Object> parsePurlToComponent(String purl) {
        Map<String, Object> comp = new java.util.LinkedHashMap<>();
        comp.put("type", "library");
        comp.put("purl", purl);

        if (purl == null) {
            comp.put("group", "");
            comp.put("name", "");
            comp.put("version", "");
            return comp;
        }
        try {
            if (purl.startsWith("pkg:maven/")) {
                // pkg:maven/group/artifact@version
                String body = purl.substring("pkg:maven/".length());
                String ver = null;
                int at = body.indexOf('@');
                if (at >= 0) {
                    //  Extracts the version
                    ver = body.substring(at + 1);
                    //  Extracts org/name
                    body = body.substring(0, at);
                }
                int slash = body.lastIndexOf('/');
                if (slash > 0) {
                    String group = body.substring(0, slash);
                    String artifact = body.substring(slash + 1);
                    comp.put("group", group);
                    comp.put("name", artifact);
                    comp.put("version", ver == null ? "" : ver);
                } else {
                    comp.put("group", "");
                    comp.put("name", body);
                    comp.put("version", ver == null ? "" : ver);
                }
            } else if (purl.startsWith("pkg:ballerina/")) {
                // pkg:ballerina/org/name@version or pkg:ballerina/name@version
                String body = purl.substring("pkg:ballerina/".length());
                String ver = null;
                int at = body.indexOf('@');
                if (at >= 0) {
                    ver = body.substring(at + 1);
                    body = body.substring(0, at);
                }
                int slash = body.indexOf('/');
                if (slash > 0) {
                    String org = body.substring(0, slash);
                    String name = body.substring(slash + 1);
                    comp.put("group", org);
                    comp.put("name", name);
                    comp.put("version", ver == null ? "" : ver);
                } else {
                    comp.put("group", "");
                    comp.put("name", body);
                    comp.put("version", ver == null ? "" : ver);
                }
            } else {
                // Unknown purl format; store entire purl as name
                comp.put("group", "");
                comp.put("name", purl);
                comp.put("version", "");
            }
        } catch (Exception e) {
            comp.put("group", "");
            comp.put("name", purl);
            comp.put("version", "");
        }

        return comp;
    }
}

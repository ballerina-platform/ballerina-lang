/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.task;

import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.Project;
import org.ballerinalang.maven.Dependency;
import org.ballerinalang.maven.MavenResolver;
import org.ballerinalang.maven.Utils;
import org.ballerinalang.maven.exceptions.MavenResolverException;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.projects.JarLibrary.KEY_DEPENDS_ON;
import static io.ballerina.projects.JarLibrary.KEY_TRANSITIVE_DEPENDENCIES;

/**
 * Resolve maven dependencies.
 */
public class ResolveMavenDependenciesTask implements Task {
    private final transient PrintStream out;
    private final boolean skipTask;

    public ResolveMavenDependenciesTask(PrintStream out) {
        this.out = out;
        skipTask = false;
    }

    public ResolveMavenDependenciesTask(PrintStream out, boolean skipTask) {
        this.out = out;
        this.skipTask = skipTask;
    }

    @Override
    public void execute(Project project) {
        List<Map<String, Object>> platformLibraries = new ArrayList<>();
        List<Map<String, Object>> platformRepositories = new ArrayList<>();
        PackageManifest.Platform platform;
        for (JvmTarget jvmTarget : JvmTarget.values()) {
            platform = project.currentPackage().manifest().platform(jvmTarget.code());
            if (platform != null) {
                platformLibraries.addAll(platform.dependencies());
                platformRepositories.addAll(platform.repositories());
            }
        }
        if (platformLibraries.isEmpty()) {
            return;
        }

        List<Map<String, Object>> mavenCustomRepos = new ArrayList<>();
        List<Map<String, Object>> mavenDependencies = new ArrayList<>();

        String targetRepo = project.sourceRoot().resolve("target").resolve("platform-libs").toAbsolutePath().toString();
        MavenResolver resolver = new MavenResolver(targetRepo);

        for (Map<String, Object> repository : platformRepositories) {
            if (repository.get("id") == null || repository.get("url") == null) {
                throw createLauncherException("custom maven repository properties are not specified for " +
                        "given platform repository.");
            }
            mavenCustomRepos.add(repository);
        }

        if (!mavenCustomRepos.isEmpty()) {
            for (Map<String, Object> repository : mavenCustomRepos) {
                if (repository.get("id") != null && repository.get("url") != null &&
                        repository.get("username") != null && repository.get("password") != null) {
                    resolver.addRepository(repository.get("id").toString(), repository.get("url").toString(),
                            repository.get("username").toString(), repository.get("password").toString());
                    continue;
                }
                resolver.addRepository(repository.get("id").toString(), repository.get("url").toString());
            }
        }

        for (Map<String, Object> library : platformLibraries) {
            if (library.get("path") == null) {
                if (library.get("artifactId") == null || library.get("groupId") == null
                        || library.get("version") == null) {
                    throw createLauncherException("artifact-id, group-id, and version should be specified to " +
                            "resolve the maven dependency.");
                }
                mavenDependencies.add(library);
            }
        }

        if (!mavenDependencies.isEmpty()) {
            out.println("Resolving Maven dependencies" + (skipTask ? " (UP-TO-DATE)\n" :
                    "\n\tDownloading dependencies into " + targetRepo));
            if (skipTask) {
                return;
            }
            for (Map<String, Object> library : mavenDependencies) {
                String groupId = library.get("groupId").toString();
                String artifactId = library.get("artifactId").toString();
                String version = library.get("version").toString();
                Dependency dependency;
                try {
                    dependency = resolver.resolveWithDependencyTree(groupId, artifactId, version);
                } catch (MavenResolverException e) {
                    throw createLauncherException("cannot resolve " + artifactId + ": " + e.getMessage());
                }
                library.put(KEY_TRANSITIVE_DEPENDENCIES, flattenDependencyTree(dependency));
                library.put("path", Utils.getJarPath(targetRepo, dependency));
            }
            out.println();
        }
    }


    private static List<Map<String, Object>> flattenDependencyTree(Dependency root) {
        List<Map<String, Object>> flattened = new ArrayList<>();
        collectDependencyNodes(root, flattened, new HashSet<>());
        return flattened;
    }

    private static void collectDependencyNodes(Dependency node, List<Map<String, Object>> flattened,
                                               Set<String> visited) {

        if (!visited.add(coordinate(node))) {
            return;
        }
        List<String> dependsOn = new ArrayList<>();
        for (Dependency child : node.getDepedencies()) {
            dependsOn.add(coordinate(child));
        }

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("groupId", node.getGroupId());
        entry.put("artifactId", node.getArtifactId());
        entry.put("version", node.getVersion());
        entry.put(KEY_DEPENDS_ON, dependsOn);
        flattened.add(entry);

        for (Dependency child : node.getDepedencies()) {
            collectDependencyNodes(child, flattened, visited);
        }
    }

    private static String coordinate(Dependency dependency) {
        return dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
    }
}

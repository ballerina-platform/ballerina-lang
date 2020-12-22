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

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;

/**
 * Resolve maven dependencies.
 */
public class ResolveMavenDependenciesTask implements Task {
    private final transient PrintStream out;

    public ResolveMavenDependenciesTask(PrintStream out) {
        this.out = out;
    }

    @Override
    public void execute(Project project) {
        PackageManifest.Platform platform = project.currentPackage().manifest().platform(JvmTarget.JAVA_11.code());

        if (platform == null) {
            return;
        }

        List<Map<String, Object>> platformLibraries = platform.dependencies();
        //List<Repository> mavenCustomRepos = manifest.getPlatform().getRepositories();
        List<Map<String, Object>> mavenDependencies = new ArrayList<>();
        if (platformLibraries == null) {
            return;
        }

        String targetRepo = project.sourceRoot().toString() + File.separator + "target" + File.separator
                + "platform-libs";
        MavenResolver resolver = new MavenResolver(targetRepo);

//        if (mavenCustomRepos != null && mavenCustomRepos.size() > 0) {
//            for (Repository repository : mavenCustomRepos) {
//                String id = repository.getId();
//                String url = repository.getUrl();
//                if (id == null && url == null) {
//                    throw LauncherUtils
//                            .createLauncherException("custom maven repository properties are not specified for " +
//                            "given platform repository.");
//                }
//
//                String username = repository.getUsername();
//                String password = repository.getPassword();
//                if (username != null && password != null) {
//                    resolver.addRepository(id, url, username, password);
//                    continue;
//                }
//                resolver.addRepository(id, url);
//            }
//        }

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

        if (mavenDependencies.size() > 0) {
            out.println("Resolving Maven dependencies\n\tDownloading dependencies into " + targetRepo);
            for (Map<String, Object> library : mavenDependencies) {
                try {
                    Dependency dependency = resolver.resolve(library.get("groupId").toString(),
                            library.get("artifactId").toString(),
                            library.get("version").toString(), false);
                    library.put("path", Utils.getJarPath(targetRepo, dependency));
                } catch (MavenResolverException e) {
                    throw createLauncherException("cannot resolve "
                            + library.get("artifactId").toString() + ": " + e.getMessage());
                }
            }
            out.println();
        }
    }
}

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

package org.ballerinalang.packerina.task;

import org.ballerinalang.maven.Dependency;
import org.ballerinalang.maven.MavenResolver;
import org.ballerinalang.maven.Utils;
import org.ballerinalang.maven.exceptions.MavenResolverException;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.toml.model.Library;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Repository;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Resolve maven dependencies.
 */
public class ResolveMavenDependenciesTask implements Task {

    @Override
    public void execute(BuildContext buildContext) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        Manifest manifest = ManifestProcessor.getInstance(context).getManifest();
        List<Library> platformLibs = manifest.getPlatform().getLibraries();
        List<Repository> mavenCustomRepos = manifest.getPlatform().getRepositories();
        List<Library> mavenDependencies = new ArrayList<>();
        if (platformLibs == null) {
            return;
        }

        String targetRepo = buildContext.get(BuildContextField.TARGET_DIR).toString() + File.separator
                + "platform-libs";
        MavenResolver resolver = new MavenResolver(targetRepo);

        if (mavenCustomRepos != null && mavenCustomRepos.size() > 0) {
            for (Repository repository : mavenCustomRepos) {
                String id = repository.getId();
                String url = repository.getUrl();
                if (id == null && url == null) {
                    throw createLauncherException("custom maven repository properties are not specified for " +
                            "given platform repository.");
                }

                String username = repository.getUsername();
                String password = repository.getPassword();
                if (username != null && password != null) {
                    resolver.addRepository(id, url, username, password);
                    continue;
                }
                resolver.addRepository(id, url);
            }
        }

        for (Library library : platformLibs) {
            if (library.getPath() == null) {
                if (library.getArtifactId() == null && library.getGroupId() == null && library.getVersion() == null) {
                    throw createLauncherException("path or maven dependency properties are not specified for " +
                            "given platform library dependency.");
                }

                if (library.getArtifactId() == null || library.getGroupId() == null || library.getVersion() == null) {
                    throw createLauncherException("artifact-id, group-id, and version should be specified to " +
                                    "resolve the maven dependency.");
                }
                mavenDependencies.add(library);
            }
        }

        if (mavenDependencies.size() > 0) {
            buildContext.out().println("Resolving Maven dependencies\n\tDownloading dependencies into " + targetRepo);
            for (Library library : mavenDependencies) {
                try {
                    Dependency dependency = resolver.resolve(library.getGroupId(), library.getArtifactId(),
                            library.getVersion(), false);
                    library.setPath(Utils.getJarPath(targetRepo, dependency));
                } catch (MavenResolverException e) {
                    throw createLauncherException("cannot resolve " + library.getArtifactId() + ": " + e.getMessage());
                }
            }
            buildContext.out().println();
        }
    }
}

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
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.File;

/**
 * Resolve maven dependencies.
 */
public class ResolveMavenDependenciesTask implements Task {

    @Override
    public void execute(BuildContext buildContext) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        Manifest manifest = ManifestProcessor.getInstance(context).getManifest();
        if (manifest.getPlatform().getLibraries() == null) {
            return;
        }

        String targetRepo = buildContext.get(BuildContextField.TARGET_DIR).toString() + File.separator
                + "platform-libs";
        MavenResolver resolver = new MavenResolver(targetRepo);

        buildContext.out().println();
        for (Library library : manifest.getPlatform().getLibraries()) {
            if (library.getPath() == null) {
                buildContext.out().println("Resolving " + library.getArtifactId());
                try {
                    Dependency dependency = resolver.resolve(library.getGroupId(), library.getArtifactId(),
                            library.getVersion(), false);
                    library.setPath(Utils.getJarPath(targetRepo, dependency));
                } catch (MavenResolverException e) {
                    buildContext.err().print("cannot resolve " + library.getArtifactId());
                }
            }
        }
    }
}

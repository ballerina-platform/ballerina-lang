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

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.wso2.ballerinalang.compiler.semantics.analyzer.ObserverbilitySymbolCollectorRunner;
import org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Copy collected observability symbol to module jar.
 *
 * @since 2.0.0
 */
public class CopyObservabilitySymbolsTask implements Task {

    @Override
    public void execute(BuildContext buildContext) {
        List<BLangPackage> modulesWithEntryPoints = buildContext.getModules().stream()
                .filter(m -> m.symbol.entryPointExists)
                .collect(Collectors.toList());

        if (!modulesWithEntryPoints.isEmpty()) {
            CompilerContext compilerContext = buildContext.get(BuildContextField.COMPILER_CONTEXT);
            ObservabilitySymbolCollector observabilitySymbolCollector
                    = ObserverbilitySymbolCollectorRunner.getInstance(compilerContext);

            for (BLangPackage module : modulesWithEntryPoints) {
                PackageID packageID = module.packageID;
                Path moduleJarPath = buildContext.getJarPathFromTargetCache(packageID);
                URI uberJarUri = URI.create("jar:" + moduleJarPath.toUri().toString());

                try (FileSystem toFs = FileSystems.newFileSystem(uberJarUri, Collections.emptyMap())) {
                    Path jarRoot = toFs.getRootDirectories().iterator().next();
                    observabilitySymbolCollector.writeCollectedSymbols(module, jarRoot);
                } catch (IOException | NoSuchAlgorithmException e) {
                    throw createLauncherException("error while adding observability symbols to module jar :"
                            + e.getMessage());
                }
            }
        }
    }
}

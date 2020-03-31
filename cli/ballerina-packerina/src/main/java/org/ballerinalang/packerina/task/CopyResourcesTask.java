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

import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Copy resources to module jar.
 *
 * @since 1.1.2
 */
public class CopyResourcesTask implements Task {

    @Override
    public void execute(BuildContext buildContext) {
        Path sourceRoot = buildContext.get(BuildContextField.SOURCE_ROOT);
        List<BLangPackage> moduleBirMap = buildContext.getModules();
        for (BLangPackage module : moduleBirMap) {
            copyResourcesToJar(buildContext, sourceRoot, module);
        }
    }

    private void copyResourcesToJar(BuildContext buildContext, Path sourceRootPath, BLangPackage module) {
        // Get resources directory
        Path resourceDir = sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                .resolve(module.packageID.name.value)
                .resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
        if (!resourceDir.toFile().exists()) {
            return;
        }
        // Get the module jar
        Path moduleJarPath = buildContext.getJarPathFromTargetCache(module.packageID);
        URI uberJarUri = URI.create("jar:" + moduleJarPath.toUri().toString());
        try (FileSystem toFs = FileSystems.newFileSystem(uberJarUri, Collections.emptyMap())) {
            Path to = toFs.getRootDirectories().iterator().next().resolve("resources")
                    .resolve(module.packageID.orgName.value)
                    .resolve(module.packageID.name.value);
            Files.walkFileTree(resourceDir, new Copy(resourceDir, to));
        } catch (IOException e) {
            throw createLauncherException("error while adding resources to module jar :" + e.getMessage());
        }
    }

    static class Copy extends SimpleFileVisitor<Path> {

        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;

        Copy(Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        Copy(Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path toFile = toPath.resolve(fromPath.relativize(file).toString());
            Files.copy(file, toFile, copyOption);
            return FileVisitResult.CONTINUE;
        }
    }
}

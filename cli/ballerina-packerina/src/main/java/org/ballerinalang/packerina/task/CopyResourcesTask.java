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

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.StringJoiner;

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
        Path moduleJarPath = buildContext.getJarPathFromTargetCache(module.packageID);
        try (ZipArchiveOutputStream outStream = new ZipArchiveOutputStream(new BufferedOutputStream(
                new FileOutputStream(String.valueOf(moduleJarPath))))) {
            String copyEntryPath = new StringJoiner("/")
                    .add(ProjectDirConstants.RESOURCE_DIR_NAME)
                    .add(module.packageID.orgName.value)
                    .add(module.packageID.name.value).toString();
            Files.walkFileTree(resourceDir, new CopyResourceFileVisitor(outStream, resourceDir.toString(),
                                                                        copyEntryPath));
        } catch (IOException e) {
            throw createLauncherException("unable to extract the uber jar :" + e.getMessage());
        }
    }

    static class CopyResourceFileVisitor extends SimpleFileVisitor<Path> {

        private ZipArchiveOutputStream outStream;
        private String resourcesSourcePath;
        private String resourcesEntryPath;

        private CopyResourceFileVisitor(ZipArchiveOutputStream outStream, String resourcesSourcePath,
                                        String resourcesEntryPath) {
            this.outStream = outStream;
            this.resourcesSourcePath = resourcesSourcePath;
            this.resourcesEntryPath = resourcesEntryPath;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try (InputStream is = new FileInputStream(file.toFile())) {
                JarArchiveEntry e =
                        new JarArchiveEntry(resourcesEntryPath + file.toString().replace(resourcesSourcePath, ""));
                outStream.putArchiveEntry(e);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }
                outStream.closeArchiveEntry();
            }
            return FileVisitResult.CONTINUE;
        }
    }
}

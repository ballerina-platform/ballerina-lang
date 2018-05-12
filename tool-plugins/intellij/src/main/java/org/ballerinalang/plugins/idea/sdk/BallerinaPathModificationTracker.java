/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.ballerinalang.plugins.idea.sdk;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import org.ballerinalang.plugins.idea.BallerinaConstants;
import org.ballerinalang.plugins.idea.project.BallerinaApplicationLibrariesService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Tracks Ballerina path modifications.
 */
public class BallerinaPathModificationTracker {

    private final Set<String> pathsToTrack = ContainerUtil.newHashSet();
    private final Collection<VirtualFile> ballerinaPathRoots = ContainerUtil.newLinkedHashSet();

    private final List<VirtualFile> orgNames = ContainerUtil.newArrayList();

    private final Map<String, List<VirtualFile>> packageMap = ContainerUtil.newHashMap();

    public BallerinaPathModificationTracker() {
        String ballerinaRepository = BallerinaEnvironmentUtil.retrieveRepositoryPathFromEnvironment();
        if (ballerinaRepository != null) {
            ballerinaRepository = Paths.get(ballerinaRepository,
                    BallerinaConstants.BALLERINA_REPOSITORY_SOURCE_DIRECTORY).toAbsolutePath().toString();
            String home = SystemProperties.getUserHome();
            for (String s : StringUtil.split(ballerinaRepository, File.pathSeparator)) {
                if (s.contains("$HOME")) {
                    s = s.replaceAll("\\$HOME", home);
                }
                pathsToTrack.add(s);
            }
        } else {
            Path caches = Paths.get(SystemProperties.getUserHome(), ".ballerina", "caches", "central.ballerina.io");
            pathsToTrack.add(caches.toString());
        }

        recalculateFiles();

        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileAdapter() {
            @Override
            public void fileCreated(@NotNull VirtualFileEvent event) {
                handleEvent(event);
            }

            @Override
            public void fileDeleted(@NotNull VirtualFileEvent event) {
                handleEvent(event);
            }

            @Override
            public void fileMoved(@NotNull VirtualFileMoveEvent event) {
                handleEvent(event);
            }

            @Override
            public void fileCopied(@NotNull VirtualFileCopyEvent event) {
                handleEvent(event);
            }

            private void handleEvent(VirtualFileEvent event) {
                for (String path : pathsToTrack) {
                    if (event.getFile().getPath().startsWith(path)) {
                        recalculateFiles();
                    }
                }
            }
        });
    }

    private void recalculateFiles() {
        Collection<VirtualFile> result = ContainerUtil.newLinkedHashSet();
        for (String path : pathsToTrack) {

            VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(path);
            if (fileByPath != null) {

                VirtualFile[] children = fileByPath.getChildren();
                for (VirtualFile organization : children) {
                    String organizationName = organization.getName();
                    if (!organization.isDirectory() || "$anon".equals(organizationName)) {
                        continue;
                    }

                    orgNames.add(organization);

                    VirtualFile[] packages = organization.getChildren();

                    List<VirtualFile> packageNames = ContainerUtil.newArrayList();

                    for (VirtualFile aPackage : packages) {

                        if (!aPackage.isDirectory()) {
                            continue;
                        }
                        String packageName = aPackage.getName();
                        packageNames.add(aPackage);

                        VirtualFile[] versions = aPackage.getChildren();
                        int versionCount = versions.length;

                        if (versionCount == 0) {
                            continue;
                        }

                        // Todo - Get the correct version
                        VirtualFile latestVersion = versions[0];

                        VirtualFile[] files = latestVersion.getChildren();

                        for (VirtualFile file : files) {

                            if (file.getName().equals(packageName + ".zip")) {
                                try {
                                    String destinationDirectory = latestVersion.getPath() + File.separator +
                                            packageName;
                                    unzip(file.getPath(), destinationDirectory);
                                    // String srcPath = destinationDirectory + File.separator + "src";
                                    // VirtualFile srcDirectory = LocalFileSystem.getInstance().findFileByPath(srcPath);
                                    // ContainerUtil.addIfNotNull(result, srcDirectory);
                                    // ContainerUtil.addIfNotNull(result, aPackage);
                                } catch (IOException e) {

                                }
                            }
                        }
                    }
                    packageMap.put(organizationName, packageNames);
                    ContainerUtil.addIfNotNull(result, organization);
                }
            }
        }
        updateBallerinaPathRoots(result);
    }

    private void unzip(String fileZipPath, String destinationDirectory) throws IOException {

        byte[] buffer = new byte[1024 * 4];

        //get the zip file content
        ZipInputStream zis =
                new ZipInputStream(new FileInputStream(fileZipPath));
        //get the zipped file list entry
        ZipEntry ze = zis.getNextEntry();

        while (ze != null) {

            String fileName = ze.getName();
            File newFile = new File(destinationDirectory + File.separator + fileName);

            if (ze.isDirectory()) {
                newFile.mkdir();
                ze = zis.getNextEntry();
                continue;
            }

            //create all non exists folders
            //else you will hit FileNotFoundException for compressed folder
            new File(newFile.getParent()).mkdirs();

            FileOutputStream fos = new FileOutputStream(newFile);

            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            fos.close();
            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    private synchronized void updateBallerinaPathRoots(Collection<VirtualFile> newRoots) {
        ballerinaPathRoots.clear();
        ballerinaPathRoots.addAll(newRoots);
    }

    private synchronized Collection<VirtualFile> getBallerinaPathRoots() {
        return ballerinaPathRoots;
    }

    private synchronized List<VirtualFile> getAllOrganizations() {
        return orgNames;
    }

    private synchronized List<VirtualFile> getAllPackages(String organization) {
        if (BallerinaApplicationLibrariesService.getInstance().isUseBallerinaPathFromSystemEnvironment()) {
            return packageMap.get(organization);
        }
        return ContainerUtil.newArrayList();
    }

    public static Collection<VirtualFile> getBallerinaEnvironmentPathRoots() {
        return ServiceManager.getService(BallerinaPathModificationTracker.class).getBallerinaPathRoots();
    }

    public static List<VirtualFile> getAllOrganizationsInUserRepo() {
        if (BallerinaApplicationLibrariesService.getInstance().isUseBallerinaPathFromSystemEnvironment()) {
            return ServiceManager.getService(BallerinaPathModificationTracker.class).getAllOrganizations();
        }
        return ContainerUtil.newArrayList();
    }

    @Nullable
    public static VirtualFile getOrganizationInUserRepo(String organizationName) {
        if (BallerinaApplicationLibrariesService.getInstance().isUseBallerinaPathFromSystemEnvironment()) {
            List<VirtualFile> organizations = ServiceManager.getService(BallerinaPathModificationTracker.class)
                    .getAllOrganizations();
            for (VirtualFile virtualFile : organizations) {
                if (virtualFile.getName().equals(organizationName)) {
                    return virtualFile;
                }
            }
        }
        return null;
    }

    public static List<VirtualFile> getPackagesFromOrganization(String orgName) {
        List<VirtualFile> allPackages =
                ServiceManager.getService(BallerinaPathModificationTracker.class).getAllPackages(orgName);
        return ContainerUtil.notNullize(allPackages);
    }
}

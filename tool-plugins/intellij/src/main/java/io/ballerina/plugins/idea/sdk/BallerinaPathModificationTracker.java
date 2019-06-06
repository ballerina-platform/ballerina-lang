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
 */

package io.ballerina.plugins.idea.sdk;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import io.ballerina.plugins.idea.BallerinaConstants;
import io.ballerina.plugins.idea.project.BallerinaApplicationLibrariesService;
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

    // Paths which we should track.
    private static final Set<String> pathsToTrack = ContainerUtil.newHashSet();
    // Paths which will be used to find sources.
    private static final Collection<VirtualFile> ballerinaPathRoots = ContainerUtil.newLinkedHashSet();
    // List which will be used to store organizations.
    private static final List<VirtualFile> organizationNames = ContainerUtil.newArrayList();
    // Map which will contain packages correspond to organizations. Key - organization, value - package list.
    private static final Map<String, List<VirtualFile>> packageMap = ContainerUtil.newHashMap();

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
                addPath(s);
            }
        } else {
            Path caches = Paths.get(SystemProperties.getUserHome(), ".ballerina", "caches", "central.ballerina.io");
            addPath(caches.toString());
        }

        recalculateFiles();

        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
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

    public static synchronized void addPath(String path) {
        pathsToTrack.add(path);
        recalculateFiles();
    }

    public static synchronized void removePath(String path) {
        pathsToTrack.remove(path);
        recalculateFiles();
    }

    private static void recalculateFiles() {
        organizationNames.clear();
        packageMap.clear();
        Collection<VirtualFile> result = ContainerUtil.newLinkedHashSet();
        // Iterate through all paths which are tracked.
        for (String path : pathsToTrack) {
            // Find the corresponding virtual file.
            VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(path);
            if (fileByPath != null) {
                // Get the children.
                VirtualFile[] children = fileByPath.getChildren();
                // Iterate through children. Each child represent an organization.
                for (VirtualFile organization : children) {
                    // Get the organization name.
                    String organizationName = organization.getName();
                    // If the organization is not a directory or its name is "$anon", we skip it.
                    // Skips the "ballerina" organization since already added from "src".
                    // Todo - Include "ballerina" after refactoring
                    if (!organization.isDirectory() || "$anon".equals(organizationName)
                            || "ballerina".equals(organizationName)) {
                        continue;
                    }
                    // Add the organization to the list. This list is later used for code completion.
                    organizationNames.add(organization);
                    // Get the children. These will correspond to packages.
                    VirtualFile[] packages = organization.getChildren();

                    // We create a new list to save all packages which we will later use for code completion.
                    List<VirtualFile> packageNames = ContainerUtil.newArrayList();

                    // Iterate through all packages.
                    for (VirtualFile aPackage : packages) {
                        // If it is not a directory, skip it.
                        if (!aPackage.isDirectory()) {
                            continue;
                        }
                        // Add the package to the list.
                        packageNames.add(aPackage);
                        // Get the name of the package.
                        String packageName = aPackage.getName();

                        // Get the children. These will correspond to versions.
                        VirtualFile[] versions = aPackage.getChildren();
                        // Get the latest version.
                        VirtualFile latestVersion = getLatestVersion(versions);
                        if (latestVersion == null) {
                            continue;
                        }

                        // Get and iterate through the children.
                        VirtualFile[] files = latestVersion.getChildren();
                        for (VirtualFile file : files) {
                            // If we encounter a zip file with the same name as the package name, we extract it.
                            // This zip file contains the sources.
                            if (file.getName().equals(packageName + ".zip")) {
                                try {
                                    String destinationDirectory = latestVersion.getPath() + File.separator +
                                            packageName;
                                    unzip(file.getPath(), destinationDirectory);
                                } catch (IOException ignored) {

                                }
                            }
                        }
                    }
                    // Update the package map.
                    packageMap.put(organizationName, packageNames);
                    // Update the results.
                    ContainerUtil.addIfNotNull(result, organization);
                }
            }
        }
        updateBallerinaPathRoots(result);
    }

    @Nullable
    private static VirtualFile getLatestVersion(@Nullable VirtualFile[] files) {
        if (files == null || files.length == 0) {
            return null;
        }
        for (int i = files.length - 1; i >= 0; i--) {
            VirtualFile file = files[i];
            if (file.isDirectory()) {
                return file;
            }
        }
        return null;
    }

    private static void unzip(String fileZipPath, String destinationDirectory) throws IOException {

        byte[] buffer = new byte[1024 * 4];
        // Get the zip file content.
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(fileZipPath));
        // Get the zipped file entry.
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while (zipEntry != null) {
            // Get the name.
            String fileName = zipEntry.getName();
            // Construct the output file.
            File outputFile = new File(destinationDirectory + File.separator + fileName);
            // Skip extraction of the file, if Arbitrary File Write attack attempts (Zip Slip) was detected.
            if (!outputFile.getCanonicalPath().startsWith(new File(destinationDirectory).getCanonicalPath())) {
                BallerinaSdkService.LOG.info("Arbitrary File Write attack attempted via an archive file. File name: " +
                        outputFile.getName());
                continue;
            }
            // If the zip entry is for a directory, we create the directory and continue with the next entry.
            if (zipEntry.isDirectory()) {
                outputFile.mkdir();
                zipEntry = zipInputStream.getNextEntry();
                continue;
            }

            // Create all non-existing directories.
            new File(outputFile.getParent()).mkdirs();
            // Create a new file output stream.
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            // Write the content from zip input stream to the file output stream.
            int len;
            while ((len = zipInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
            }
            // Close the file output stream.
            fileOutputStream.close();
            // Continue with the next entry.
            zipEntry = zipInputStream.getNextEntry();
        }
        // Close zip input stream.
        zipInputStream.closeEntry();
        zipInputStream.close();
    }

    private static synchronized void updateBallerinaPathRoots(Collection<VirtualFile> newRoots) {
        ballerinaPathRoots.clear();
        ballerinaPathRoots.addAll(newRoots);
    }

    /**
     * Used to retrieve path roots.
     *
     * @return list of paths.
     */
    private synchronized Collection<VirtualFile> getBallerinaPathRoots() {
        return ballerinaPathRoots;
    }

    /**
     * Used to retrieve all organizations from user repo.
     *
     * @return list of organizations.
     */
    private synchronized List<VirtualFile> getAllOrganizations() {
        return organizationNames;
    }

    /**
     * Used to retrieve packages from the given organization.
     *
     * @param organization organization name.
     * @return list of packages
     */
    private synchronized List<VirtualFile> getAllPackages(String organization) {
        if (BallerinaApplicationLibrariesService.getInstance().isUseBallerinaPathFromSystemEnvironment()) {
            return packageMap.get(organization);
        }
        return ContainerUtil.newArrayList();
    }

    /**
     * Used to retrieve path roots.
     *
     * @return list of paths.
     */
    public static Collection<VirtualFile> getBallerinaEnvironmentPathRoots() {
        return ServiceManager.getService(BallerinaPathModificationTracker.class).getBallerinaPathRoots();
    }

    /**
     * Used to retrieve all organizations from user repo.
     *
     * @return list of organizations.
     */
    public static List<VirtualFile> getAllOrganizationsInUserRepo() {
        if (BallerinaApplicationLibrariesService.getInstance().isUseBallerinaPathFromSystemEnvironment()) {
            return ServiceManager.getService(BallerinaPathModificationTracker.class).getAllOrganizations();
        }
        return ContainerUtil.newArrayList();
    }

    /**
     * Used to retrieve the given organization in the user repo.
     *
     * @param organization organization name.
     * @return given organization location.
     */
    @Nullable
    public static VirtualFile getOrganizationInUserRepo(String organization) {
        if (BallerinaApplicationLibrariesService.getInstance().isUseBallerinaPathFromSystemEnvironment()) {
            List<VirtualFile> organizations = ServiceManager.getService(BallerinaPathModificationTracker.class)
                    .getAllOrganizations();
            for (VirtualFile virtualFile : organizations) {
                if (virtualFile.getName().equals(organization)) {
                    return virtualFile;
                }
            }
        }
        return null;
    }

    /**
     * Returns all packages in the given organization.
     * @param organization organization name
     * @return list of packages
     */
    public static List<VirtualFile> getPackagesFromOrganization(String organization) {
        List<VirtualFile> allPackages =
                ServiceManager.getService(BallerinaPathModificationTracker.class).getAllPackages(organization);
        return ContainerUtil.notNullize(allPackages);
    }
}

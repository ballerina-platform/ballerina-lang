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

package org.ballerinalang.plugins.idea.service;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;

/**
 * Tracks Ballerina home modifications.
 */
public class BallerinaHomeModificationTracker {

    private final Set<String> pathsToTrack = ContainerUtil.newHashSet();
    private final Collection<VirtualFile> ballerinaPathRoots = ContainerUtil.newLinkedHashSet();

    public BallerinaHomeModificationTracker() {
        Path caches = Paths.get(SystemProperties.getUserHome(), ".ballerina", "caches", "central.ballerina.io");
        pathsToTrack.add(caches.toString());
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
                if (pathsToTrack.contains(event.getFile().getPath())) {
                    recalculateFiles();
                }
            }
        });
    }

    private void recalculateFiles() {
        Collection<VirtualFile> result = ContainerUtil.newLinkedHashSet();
        for (String path : pathsToTrack) {
            ContainerUtil.addIfNotNull(result, LocalFileSystem.getInstance().findFileByPath(path));
        }
        updateBallerinaPathRoots(result);
    }

    private synchronized void updateBallerinaPathRoots(Collection<VirtualFile> newRoots) {
        ballerinaPathRoots.clear();
        ballerinaPathRoots.addAll(newRoots);
    }

    private synchronized Collection<VirtualFile> getBallerinaPathRoots() {
        return ballerinaPathRoots;
    }

    public static Collection<VirtualFile> getBallerinaEnvironmentPathRoots() {
        return ServiceManager.getService(BallerinaHomeModificationTracker.class).getBallerinaPathRoots();
    }
}

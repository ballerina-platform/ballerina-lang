/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.project;

import com.intellij.ide.util.importProject.ModuleDescriptor;
import com.intellij.ide.util.importProject.ProjectDescriptor;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectJdkForModuleStep;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.DetectedSourceRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.openapi.util.io.FileUtil;
import org.ballerinalang.plugins.idea.BallerinaModuleType;
import org.ballerinalang.plugins.idea.sdk.BallerinaSdkType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Icon;

public class BallerinaProjectStructureDetector extends ProjectStructureDetector {

    private final static Pattern BAL_FILE_PATTERN = Pattern.compile(".*\\.bal");

    @NotNull
    @Override
    public DirectoryProcessingResult detectRoots(@NotNull File dir, @NotNull File[] children, @NotNull File base,
                                                 @NotNull List<DetectedProjectRoot> result) {
        // We check all files in the dir.
        processDirectories(children, result);
        // We add the "dir" as the project root.
        List<File> filesByMask = FileUtil.findFilesByMask(BAL_FILE_PATTERN, dir);
        if (!filesByMask.isEmpty()) {
            result.add(new DetectedProjectRoot(base) {

                @NotNull
                @Override
                public String getRootTypeName() {
                    return "Ballerina";
                }
            });
        }
        return DirectoryProcessingResult.SKIP_CHILDREN;
    }

    private void processDirectories(@NotNull File[] children, @NotNull List<DetectedProjectRoot> result) {
        // Iterate through all files.
        for (File child : children) {
            // We only want to check directories.
            if (child.isDirectory()) {
                // If the directory name is "src", it can contains source files.
                if ("src".equals(child.getName())) {
                    // We check whether there are any .bal files exists inside the directory or sub directory.
                    List<File> filesByMask = FileUtil.findFilesByMask(BAL_FILE_PATTERN, child);
                    // If there are no .bal file, continue with the next directory.
                    if (filesByMask.isEmpty()) {
                        continue;
                    }
                    // If there are .bal files, add the directory as a source root.
                    result.add(new DetectedSourceRoot(child, "") {

                        @NotNull
                        @Override
                        public String getRootTypeName() {
                            return "Ballerina";
                        }
                    });
                } else {
                    // If the directory name is not "src", we check sub directories if the current directory or sub
                    // directory contains .bal files.
                    List<File> filesByMask = FileUtil.findFilesByMask(BAL_FILE_PATTERN, child);
                    // If there are no .bal file, continue with the next directory.
                    if (filesByMask.isEmpty()) {
                        continue;
                    }
                    // Get the file list.
                    File[] files = child.listFiles();
                    if (files != null) {
                        // Process files.
                        processDirectories(files, result);
                    }
                }
            }
        }
    }

    @Override
    public void setupProjectStructure(@NotNull Collection<DetectedProjectRoot> roots,
                                      @NotNull ProjectDescriptor projectDescriptor,
                                      @NotNull ProjectFromSourcesBuilder builder) {
        // If there are no roots detected, we don't need to process anything.
        if (roots.isEmpty()) {
            return;
        }

        // There can be multiple source roots in a module. So we keep all of them in the following list.
        List<DetectedSourceRoot> detectedSourceRoots = new LinkedList<>();
        // We need the project root to create a module descriptor.
        DetectedProjectRoot projectRoot = null;

        // Iterate through all DetectedProjectRoots.
        for (DetectedProjectRoot root : roots) {
            // If the root is a DetectedSourceRoot, add it as a source root to the list.
            if (root instanceof DetectedSourceRoot) {
                detectedSourceRoots.add(((DetectedSourceRoot) root));
            } else if (root instanceof DetectedProjectRoot) {
                projectRoot = root;
                // If the root is a DetectedProjectRoot, we need to add it as a source as well since it might contain
                // sources.
                DetectedSourceRoot detectedSourceRoot = new DetectedSourceRoot(root.getDirectory(), "") {

                    @NotNull
                    @Override
                    public String getRootTypeName() {
                        return "Ballerina";
                    }
                };
                detectedSourceRoots.add(detectedSourceRoot);
            }
        }

        // If we find a project root, add a module descriptor.
        if (projectRoot != null) {
            ModuleDescriptor rootModuleDescriptor = new ModuleDescriptor(projectRoot.getDirectory(),
                    BallerinaModuleType.getInstance(), detectedSourceRoots);
            List<ModuleDescriptor> moduleDescriptors = new LinkedList<>();
            moduleDescriptors.add(rootModuleDescriptor);
            projectDescriptor.setModules(moduleDescriptors);
        }
    }

    @NotNull
    @Override
    public List<ModuleWizardStep> createWizardSteps(@NotNull ProjectFromSourcesBuilder builder,
                                                    ProjectDescriptor projectDescriptor, Icon stepIcon) {
        ProjectJdkForModuleStep projectJdkForModuleStep = new ProjectJdkForModuleStep(builder.getContext(),
                BallerinaSdkType.getInstance());
        return Collections.singletonList(projectJdkForModuleStep);
    }
}

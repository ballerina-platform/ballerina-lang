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
import com.intellij.util.containers.ContainerUtil;
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

        processDirectories(children, result);
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
        for (File child : children) {
            if ("src".equals(child.getName())) {
                List<File> filesByMask = FileUtil.findFilesByMask(BAL_FILE_PATTERN, child);
                if (filesByMask.isEmpty()) {
                    continue;
                }
                result.add(new DetectedSourceRoot(child, "") {

                    @NotNull
                    @Override
                    public String getRootTypeName() {
                        return "Ballerina";
                    }
                });
            } else {
                List<File> filesByMask = FileUtil.findFilesOrDirsByMask(BAL_FILE_PATTERN, child);
                if (filesByMask.isEmpty()) {
                    continue;
                }
                File[] files = child.listFiles();
                if (files != null) {
                    processDirectories(files, result);
                }
            }
        }
    }

    @Override
    public void setupProjectStructure(@NotNull Collection<DetectedProjectRoot> roots,
                                      @NotNull ProjectDescriptor projectDescriptor,
                                      @NotNull ProjectFromSourcesBuilder builder) {
        if (roots.isEmpty()) {
            return;
        }
        List<ModuleDescriptor> moduleDescriptors = new LinkedList<>();
        ModuleDescriptor rootModuleDescriptor = null;
        ModuleDescriptor moduleDescriptor;
        for (DetectedProjectRoot root : roots) {
            if (root instanceof DetectedSourceRoot) {
                moduleDescriptor = new ModuleDescriptor(root.getDirectory().getParentFile(),
                        BallerinaModuleType.getInstance(), ((DetectedSourceRoot) root));
            } else if (root instanceof DetectedProjectRoot) {
                DetectedSourceRoot detectedSourceRoot = new DetectedSourceRoot(root.getDirectory(), "") {

                    @NotNull
                    @Override
                    public String getRootTypeName() {
                        return "Ballerina";
                    }
                };
                moduleDescriptor = new ModuleDescriptor(root.getDirectory(), BallerinaModuleType.getInstance(),
                        detectedSourceRoot);
                rootModuleDescriptor = moduleDescriptor;
            } else {
                moduleDescriptor = new ModuleDescriptor(root.getDirectory(), BallerinaModuleType.getInstance(),
                        ContainerUtil.emptyList());
            }
            moduleDescriptors.add(moduleDescriptor);
        }
        // Add root module as a dependency to all modules.
        for (ModuleDescriptor descriptor : moduleDescriptors) {
            if (descriptor.equals(rootModuleDescriptor)) {
                continue;
            }
            descriptor.addDependencyOn(rootModuleDescriptor);
        }
        projectDescriptor.setModules(moduleDescriptors);
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

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

package io.ballerina.plugins.idea.project;

import com.intellij.ide.util.importProject.ModuleDescriptor;
import com.intellij.ide.util.importProject.ProjectDescriptor;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectJdkForModuleStep;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.DetectedSourceRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.openapi.util.io.FileUtil;
import io.ballerina.plugins.idea.BallerinaModuleType;
import io.ballerina.plugins.idea.sdk.BallerinaSdkType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Icon;

/**
 * Detects project structure if the project is imported from an already created source.
 */
public class BallerinaProjectStructureDetector extends ProjectStructureDetector {

    @NotNull
    @Override
    public DirectoryProcessingResult detectRoots(@NotNull File dir, @NotNull File[] children, @NotNull File base,
                                                 @NotNull List<DetectedProjectRoot> result) {
        // We only need to mark the root as a Ballerina project root only if there is at least one Ballerina file.
        Pattern pattern = Pattern.compile(".*\\.bal");
        List<File> filesByMask = FileUtil.findFilesByMask(pattern, base);
        if (!filesByMask.isEmpty()) {
            // There can be only one project root, it is the directory that the user selects.
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

    @Override
    public void setupProjectStructure(@NotNull Collection<DetectedProjectRoot> roots,
                                      @NotNull ProjectDescriptor projectDescriptor,
                                      @NotNull ProjectFromSourcesBuilder builder) {
        // If there are no roots detected, we don't need to process anything.
        if (roots.isEmpty()) {
            return;
        }

        // Detected project root will be the first element in the collection.
        DetectedProjectRoot projectRoot = roots.iterator().next();
        // We need to create a source root element as well. This is used when we create the module descriptor.
        DetectedSourceRoot detectedSourceRoot = new DetectedSourceRoot(projectRoot.getDirectory(), "") {

            @NotNull
            @Override
            public String getRootTypeName() {
                return "Ballerina";
            }
        };

        // Create a new module descriptor.
        ModuleDescriptor rootModuleDescriptor = new ModuleDescriptor(projectRoot.getDirectory(),
                BallerinaModuleType.getInstance(), detectedSourceRoot);
        List<ModuleDescriptor> moduleDescriptors = new LinkedList<>();
        moduleDescriptors.add(rootModuleDescriptor);
        // Set the module list. This will be displayed to the user.
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

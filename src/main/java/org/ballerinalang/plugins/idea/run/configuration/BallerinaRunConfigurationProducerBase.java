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

package org.ballerinalang.plugins.idea.run.configuration;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackagePathNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.run.configuration.application.BallerinaApplicationConfiguration;
import org.ballerinalang.plugins.idea.run.configuration.file.BallerinaRunFileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public abstract class BallerinaRunConfigurationProducerBase<T extends BallerinaRunConfigurationWithMain>
        extends RunConfigurationProducer<T> implements Cloneable {

    protected BallerinaRunConfigurationProducerBase(@NotNull ConfigurationType configurationType) {
        super(configurationType);
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull T configuration, @NotNull ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {
        PsiFile file = getFileFromContext(context);
        // Get the element. This will be an identifier element.
        PsiElement element = sourceElement.get();
        // Get the FunctionNode parent from element
        FunctionNode functionNode = PsiTreeUtil.getParentOfType(element, FunctionNode.class);

        // Get the declared package in the file if available.
        String packageInFile = "";
        boolean isPackageDeclared = false;
        if (file != null) {
            // Get the PackageDeclarationNode if available.
            PackageDeclarationNode packageDeclarationNode = PsiTreeUtil.findChildOfType(file,
                    PackageDeclarationNode.class);
            if (packageDeclarationNode != null) {
                isPackageDeclared = true;
            }
            PackagePathNode packagePathNode = PsiTreeUtil.findChildOfType(packageDeclarationNode,
                    PackagePathNode.class);
            if (packagePathNode != null) {
                packageInFile = packagePathNode.getText().replaceAll("\\.", File.separator);
            }
        }

        // If FunctionNode parent is available, that means that the sourceElement is within a function. We need to
        // check whether this is a main function or not as well.
        if (BallerinaRunUtil.hasMainFunction(file) && functionNode != null) {
            // Set the configuration info.
            configuration.setName(getConfigurationName(file));
            configuration.setFilePath(file.getVirtualFile().getPath());
            Module module = context.getModule();
            if (module != null) {
                configuration.setModule(module);
            }
            configuration.setRunKind(RunConfigurationKind.MAIN);
            // Check whether a package is declared.
            if (isPackageDeclared) {
                // Get existing configuration if available.
                RunnerAndConfigurationSettings existingConfigurations = context.findExisting();
                if (existingConfigurations != null) {
                    // Get the RunConfiguration.
                    RunConfiguration existingConfiguration = existingConfigurations.getConfiguration();
                    // Run configuration might be an application configuration. So we need to check the type.
                    if (existingConfiguration instanceof BallerinaApplicationConfiguration) {
                        // If it is a BallerinaRunFileConfiguration, set the kind to MAIN.
                        BallerinaApplicationConfiguration applicationConfiguration =
                                (BallerinaApplicationConfiguration) existingConfiguration;
                        applicationConfiguration.setRunKind(RunConfigurationKind.MAIN);
                        applicationConfiguration.setPackage(packageInFile);
                        return true;
                    }
                } else {
                    // Get the project.
                    Project project = context.getProject();
                    // Get the RunManger. This has details about all run configs.
                    RunManager runManager = RunManager.getInstance(project);
                    // Get the current selected run config.
                    RunnerAndConfigurationSettings selectedConfigurationSettings =
                            runManager.getSelectedConfiguration();
                    // If there is run configs available, IDEA will create a config using
                    // createTemplateConfiguration
                    // in BallerinaRunServiceFileConfigurationType class.
                    if (selectedConfigurationSettings != null) {
                        // Get the configuration.
                        RunConfiguration currentRunConfiguration = selectedConfigurationSettings.getConfiguration();
                        // Check the type.
                        if (currentRunConfiguration instanceof BallerinaApplicationConfiguration) {
                            // Set the kind to MAIN.
                            BallerinaApplicationConfiguration applicationConfiguration =
                                    (BallerinaApplicationConfiguration) currentRunConfiguration;
                            applicationConfiguration.setRunKind(RunConfigurationKind.MAIN);
                            applicationConfiguration.setPackage(packageInFile);
                            return true;
                        }
                    }
                }
                if (configuration instanceof BallerinaApplicationConfiguration) {
                    BallerinaApplicationConfiguration applicationConfiguration =
                            (BallerinaApplicationConfiguration) configuration;
                    applicationConfiguration.setPackage(packageInFile);
                    return true;
                }
            } else {
                // We need to set/change the current configuration's kind as well. We need to check for the
                // configuration
                // type here before doing anything else because the configuration might be an application configuration.
                if (configuration instanceof BallerinaRunFileConfiguration) {
                    // Set the run kind to MAIN because we are in a main function.
                    // There can be an existing configuration for the current context as well. If that is the case, this
                    // config will be used to run the file. If there is an existing config for the context, we change
                    // the run kind of that config. Otherwise we change the current selected run configs kind.
                    RunnerAndConfigurationSettings existingConfigurations = context.findExisting();
                    if (existingConfigurations != null) {
                        // Get the RunConfiguration.
                        RunConfiguration existingConfiguration = existingConfigurations.getConfiguration();
                        // Run configuration might be an application configuration. So we need to check the type.
                        if (existingConfiguration instanceof BallerinaRunFileConfiguration) {
                            // If it is a BallerinaRunFileConfiguration, set the kind to MAIN.
                            BallerinaRunFileConfiguration fileConfiguration =
                                    (BallerinaRunFileConfiguration) existingConfiguration;
                            fileConfiguration.setRunKind(RunConfigurationKind.MAIN);
                        }
                    } else {
                        // Get the project.
                        Project project = context.getProject();
                        // Get the RunManger. This has details about all run configs.
                        RunManager runManager = RunManager.getInstance(project);
                        // Get the current selected run config.
                        RunnerAndConfigurationSettings selectedConfigurationSettings =
                                runManager.getSelectedConfiguration();
                        // If there is run configs available, IDEA will create a config using
                        // createTemplateConfiguration
                        // in BallerinaRunServiceFileConfigurationType class.
                        if (selectedConfigurationSettings != null) {
                            // Get the configuration.
                            RunConfiguration currentRunConfiguration = selectedConfigurationSettings.getConfiguration();
                            // Check the type.
                            if (currentRunConfiguration instanceof BallerinaRunFileConfiguration) {
                                // Set the kind to MAIN.
                                BallerinaRunFileConfiguration fileConfiguration =
                                        (BallerinaRunFileConfiguration) currentRunConfiguration;
                                fileConfiguration.setRunKind(RunConfigurationKind.MAIN);
                            }
                        }
                    }
                    return true;
                } else if (configuration instanceof BallerinaApplicationConfiguration) {
                    RunnerAndConfigurationSettings existingConfigurations = context.findExisting();
                    if (existingConfigurations != null) {
                        // Get the RunConfiguration.
                        RunConfiguration existingConfiguration = existingConfigurations.getConfiguration();
                        // Run configuration might be an application configuration. So we need to check the type.
                        if (existingConfiguration instanceof BallerinaApplicationConfiguration) {
                            // If it is a BallerinaRunFileConfiguration, set the kind to MAIN.
                            BallerinaApplicationConfiguration applicationConfiguration =
                                    (BallerinaApplicationConfiguration) existingConfiguration;
                            applicationConfiguration.setRunKind(RunConfigurationKind.MAIN);
                            applicationConfiguration.setPackage("org");
                        }
                    } else {
                        // Get the project.
                        Project project = context.getProject();
                        // Get the RunManger. This has details about all run configs.
                        RunManager runManager = RunManager.getInstance(project);
                        // Get the current selected run config.
                        RunnerAndConfigurationSettings selectedConfigurationSettings =
                                runManager.getSelectedConfiguration();
                        // If there is run configs available, IDEA will create a config using
                        // createTemplateConfiguration
                        // in BallerinaRunServiceFileConfigurationType class.
                        if (selectedConfigurationSettings != null) {
                            // Get the configuration.
                            RunConfiguration currentRunConfiguration = selectedConfigurationSettings.getConfiguration();
                            // Check the type.
                            if (currentRunConfiguration instanceof BallerinaApplicationConfiguration) {
                                // Set the kind to MAIN.
                                BallerinaApplicationConfiguration applicationConfiguration =
                                        (BallerinaApplicationConfiguration) currentRunConfiguration;
                                applicationConfiguration.setRunKind(RunConfigurationKind.MAIN);
                                applicationConfiguration.setPackage("org");
                            }
                        }
                    }
                }
            }
        }
        // Check configuration for service as well.
        ServiceDefinitionNode serviceDefinitionNode = PsiTreeUtil.getParentOfType(element, ServiceDefinitionNode.class);
        if (BallerinaRunUtil.hasServices(file) && serviceDefinitionNode != null) {
            configuration.setName(getConfigurationName(file));
            configuration.setFilePath(file.getVirtualFile().getPath());
            Module module = context.getModule();
            if (module != null) {
                configuration.setModule(module);
            }
            configuration.setRunKind(RunConfigurationKind.SERVICE);
            if (isPackageDeclared) {
                RunnerAndConfigurationSettings existingConfigurations = context.findExisting();
                if (existingConfigurations != null) {
                    // Get the RunConfiguration.
                    RunConfiguration existingConfiguration = existingConfigurations.getConfiguration();
                    // Run configuration might be an application configuration. So we need to check the type.
                    if (existingConfiguration instanceof BallerinaApplicationConfiguration) {
                        // If it is a BallerinaApplicationConfiguration, set the kind to SERVICE.
                        BallerinaApplicationConfiguration applicationConfiguration =
                                (BallerinaApplicationConfiguration) existingConfiguration;
                        applicationConfiguration.setRunKind(RunConfigurationKind.SERVICE);
                        applicationConfiguration.setPackage(packageInFile);
                        return true;
                    }
                } else {
                    // Get the project.
                    Project project = context.getProject();
                    // Get the RunManger. This has details about all run configs.
                    RunManager runManager = RunManager.getInstance(project);
                    // Get the current selected run config.
                    RunnerAndConfigurationSettings selectedConfigurationSettings =
                            runManager.getSelectedConfiguration();
                    // If there is run configs available, IDEA will create a config using createTemplateConfiguration
                    // in BallerinaRunServiceFileConfigurationType class.
                    if (selectedConfigurationSettings != null) {
                        // Get the configuration.
                        RunConfiguration currentRunConfiguration = selectedConfigurationSettings.getConfiguration();
                        // Check the type.
                        if (currentRunConfiguration instanceof BallerinaApplicationConfiguration) {
                            // Set the kind to SERVICE.
                            BallerinaApplicationConfiguration applicationConfiguration =
                                    (BallerinaApplicationConfiguration) currentRunConfiguration;
                            applicationConfiguration.setRunKind(RunConfigurationKind.SERVICE);
                            applicationConfiguration.setPackage(packageInFile);
                            return true;
                        }
                    }
                }
                if (configuration instanceof BallerinaApplicationConfiguration) {
                    BallerinaApplicationConfiguration applicationConfiguration =
                            (BallerinaApplicationConfiguration) configuration;
                    applicationConfiguration.setPackage(packageInFile);
                    return true;
                }
            } else {
                if (configuration instanceof BallerinaRunFileConfiguration) {
                    RunnerAndConfigurationSettings existingConfigurations = context.findExisting();
                    if (existingConfigurations != null) {
                        RunConfiguration existingConfiguration = existingConfigurations.getConfiguration();
                        if (existingConfiguration instanceof BallerinaRunFileConfiguration) {
                            ((BallerinaRunFileConfiguration) existingConfiguration).setRunKind(
                                    RunConfigurationKind.SERVICE);
                        }
                    } else {
                        Project project = context.getProject();
                        RunManager runManager = RunManager.getInstance(project);
                        RunnerAndConfigurationSettings selectedConfigurationSettings =
                                runManager.getSelectedConfiguration();
                        if (selectedConfigurationSettings != null) {
                            RunConfiguration currentRunConfiguration = selectedConfigurationSettings.getConfiguration();
                            if (currentRunConfiguration instanceof BallerinaRunFileConfiguration) {
                                ((BallerinaRunFileConfiguration) currentRunConfiguration).setRunKind(
                                        RunConfigurationKind.SERVICE);
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    protected abstract String getConfigurationName(@NotNull PsiFile file);

    @Override
    public boolean isConfigurationFromContext(@NotNull T configuration, ConfigurationContext context) {
        BallerinaFile file = getFileFromContext(context);
        return (file != null) && FileUtil.pathsEqual(configuration.getFilePath(), file.getVirtualFile().getPath());
    }

    @Nullable
    private static BallerinaFile getFileFromContext(@Nullable ConfigurationContext context) {
        PsiElement contextElement = BallerinaRunUtil.getContextElement(context);
        PsiFile psiFile = contextElement != null ? contextElement.getContainingFile() : null;
        return psiFile instanceof BallerinaFile ? (BallerinaFile) psiFile : null;
    }
}

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

package io.ballerina.plugins.idea.runconfig;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import io.ballerina.plugins.idea.BallerinaConstants;
import io.ballerina.plugins.idea.runconfig.application.BallerinaApplicationConfiguration;
import io.ballerina.plugins.idea.runconfig.test.BallerinaTestConfiguration;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.BallerinaFunctionDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaServiceDefinition;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides the capability to identify the configuration when we try to run a file from the gutter run icon.
 *
 * @param <T> run configuration type
 */
public abstract class BallerinaRunConfigurationProducerBase<T extends BallerinaRunConfigurationWithMain>
        extends RunConfigurationProducer<T> implements Cloneable {

    protected BallerinaRunConfigurationProducerBase(@NotNull ConfigurationType configurationType) {
        super(configurationType);
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull T configuration, @NotNull ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {
        // This method will be called with each configuration type. So we need to return true for correct
        // configuration type after updating the configurations.
        PsiFile file = getFileFromContext(context);
        if (file == null) {
            return false;
        }

        // Get the element. This will be an identifier element.
        PsiElement element = sourceElement.get();
        // Get the FunctionDefinitionNode parent from element (if exists).
        BallerinaFunctionDefinition functionNode = PsiTreeUtil.getParentOfType(element,
                BallerinaFunctionDefinition.class);
        // Get the ServiceDefinitionNode parent from element (if exists).
        BallerinaServiceDefinition serviceDefinitionNode = PsiTreeUtil.getParentOfType(element,
                BallerinaServiceDefinition.class);

        // Setup configuration for Ballerina test files.
        //        if (file.getName().endsWith(BallerinaConstants.BALLERINA_TEST_FILE_SUFFIX) && functionNode != null &&
        //                BallerinaRunUtil.isTestFunction(functionNode)) {
        //            if (!(configuration instanceof BallerinaTestConfiguration)) {
        //                return false;
        //            }
        //            // Todo - update
        //            BallerinaPackageDeclaration packageDeclarationNode = PsiTreeUtil.findChildOfType(file,
        //                    BallerinaPackageDeclaration.class);
        //            // Get the package path node. We need this to get the package path of the file.
        //            BallerinaCompletePackageName fullyQualifiedPackageNameNode = PsiTreeUtil.findChildOfType
        //                    (packageDeclarationNode, BallerinaCompletePackageName.class);
        //            String packageInFile = "";
        //            if (fullyQualifiedPackageNameNode != null) {
        //                // Regardless of the OS, separator character will be "/".
        //                packageInFile = fullyQualifiedPackageNameNode.getText().replaceAll("\\.", "/");
        //            }
        //
        //            RunnerAndConfigurationSettings existingConfigurations = context.findExisting();
        //            if (existingConfigurations != null) {
        //                // Get the RunConfiguration.
        //                RunConfiguration existingConfiguration = existingConfigurations.getConfiguration();
        //                // Run configuration might be an application configuration. So we need to check the type.
        //                if (existingConfiguration instanceof BallerinaTestConfiguration) {
        //                    // Set other configurations.
        //                    setTestConfigurations((BallerinaTestConfiguration) existingConfiguration, file,
        // packageInFile);
        //                    return true;
        //                }
        //                return false;
        //            } else {
        //                // If an existing configuration is not found and the configuration provided is of correct
        // type.
        //                String configName = getConfigurationName(file);
        //                // Set the config name. This will be the file name.
        //                configuration.setName(configName);
        //                // Set the file path.
        //                configuration.setFilePath(file.getVirtualFile().getPath());
        //                // Set the module.
        //                Module module = context.getModule();
        //                if (module != null) {
        //                    configuration.setModule(module);
        //                }
        //                // Set other configurations.
        //                setTestConfigurations((BallerinaTestConfiguration) configuration, file, packageInFile);
        //                return true;
        //            }
        //        }

        // Get the declared package in the file if available.
        //        String packageInFile = "";
        boolean isPackageDeclared = false;
        // Get the PackageDeclarationNode if available.
        //        BallerinaPackageDeclaration packageDeclarationNode = PsiTreeUtil.findChildOfType(file,
        //                BallerinaPackageDeclaration.class);
        //        if (packageDeclarationNode != null) {
        //            isPackageDeclared = true;
        //        }

        String filePackage = BallerinaPsiImplUtil.getPackage(file);
        if (!filePackage.isEmpty()) {
            isPackageDeclared = true;
        }


        // Get the package path node. We need this to get the package path of the file.
        //        BallerinaCompletePackageName fullyQualifiedPackageNameNode = PsiTreeUtil.findChildOfType
        //                (packageDeclarationNode, BallerinaCompletePackageName.class);
        //        if (fullyQualifiedPackageNameNode != null) {
        //            // Regardless of the OS, separator character will be "/".
        //            packageInFile = fullyQualifiedPackageNameNode.getText().replaceAll("\\.", "/");
        //        }

        // Get existing configuration if available.
        RunnerAndConfigurationSettings existingConfigurations = context.findExisting();
        if (existingConfigurations != null) {
            // Get the RunConfiguration.
            RunConfiguration existingConfiguration = existingConfigurations.getConfiguration();
            // Run configuration might be an application configuration. So we need to check the type.
            if (existingConfiguration instanceof BallerinaApplicationConfiguration) {
                // Set other configurations.
                setConfigurations((BallerinaApplicationConfiguration) existingConfiguration, file, functionNode,
                        serviceDefinitionNode, filePackage, isPackageDeclared);
                return true;
            }
        } else if (configuration instanceof BallerinaApplicationConfiguration) {
            // If an existing configuration is not found and the configuration provided is of correct type.
            String configName = getConfigurationName(file);
            // Set the config name. This will be the file name.
            configuration.setName(configName);
            // Set the file path.
            configuration.setFilePath(file.getVirtualFile().getPath());
            // Set the module.
            Module module = context.getModule();
            if (module != null) {
                configuration.setModule(module);
            }
            // Set other configurations.
            setConfigurations((BallerinaApplicationConfiguration) configuration, file, functionNode,
                    serviceDefinitionNode, filePackage, isPackageDeclared);
            return true;
        }
        // Return false if the provided configuration type cannot be applied.
        return false;
    }

    private void setConfigurations(@NotNull BallerinaApplicationConfiguration configuration, @NotNull PsiFile file,
                                   @Nullable BallerinaFunctionDefinition functionNode,
                                   @Nullable BallerinaServiceDefinition serviceDefinitionNode,
                                   @NotNull String packageInFile, boolean isPackageDeclared) {
        // Set the run kind.
        if (BallerinaRunUtil.hasMainFunction(file) && functionNode != null) {
            // Set the kind to MAIN.
            configuration.setRunKind(RunConfigurationKind.MAIN);
        } else if (BallerinaRunUtil.hasServices(file) && serviceDefinitionNode != null) {
            // Set the kind to SERVICE.
            configuration.setRunKind(RunConfigurationKind.SERVICE);
        }

        // Set the package.
        if (isPackageDeclared) {
            configuration.setPackage(packageInFile);
        } else {
            configuration.setPackage("");
        }

        // Set the working directory. If this is not set, package in sub modules will not run properly.
        Module module = ModuleUtilCore.findModuleForPsiElement(file);
        if (module == null) {
            return;
        }
        VirtualFile moduleFile = module.getModuleFile();
        if (moduleFile == null) {
            return;
        }
        String workingDirectory = moduleFile.getParent().getPath();
        if (workingDirectory.endsWith(BallerinaConstants.IDEA_CONFIG_DIRECTORY)) {
            workingDirectory = workingDirectory.replace(BallerinaConstants.IDEA_CONFIG_DIRECTORY, "");
        }
        configuration.setWorkingDirectory(workingDirectory);
    }

    private void setTestConfigurations(@NotNull BallerinaTestConfiguration configuration, @NotNull PsiFile file,
                                       @NotNull String packageInFile) {
        configuration.setPackage(packageInFile);
        // Set the working directory. If this is not set, package in sub modules will not run properly.
        Module module = ModuleUtilCore.findModuleForPsiElement(file);
        if (module == null) {
            return;
        }
        VirtualFile moduleFile = module.getModuleFile();
        if (moduleFile == null) {
            return;
        }
        String workingDirectory = moduleFile.getParent().getPath();
        if (workingDirectory.endsWith(BallerinaConstants.IDEA_CONFIG_DIRECTORY)) {
            workingDirectory = workingDirectory.replace(BallerinaConstants.IDEA_CONFIG_DIRECTORY, "");
        }
        configuration.setWorkingDirectory(workingDirectory);
    }


    @NotNull
    protected abstract String getConfigurationName(@NotNull PsiFile file);

    @Override
    public boolean isConfigurationFromContext(@NotNull T configuration, ConfigurationContext context) {
        BallerinaFile file = getFileFromContext(context);
        return file != null && FileUtil.pathsEqual(configuration.getFilePath(), file.getVirtualFile().getPath());
    }

    @Nullable
    private static BallerinaFile getFileFromContext(@Nullable ConfigurationContext context) {
        PsiElement contextElement = BallerinaRunUtil.getContextElement(context);
        PsiFile psiFile = contextElement != null ? contextElement.getContainingFile() : null;
        return psiFile instanceof BallerinaFile ? (BallerinaFile) psiFile : null;
    }
}

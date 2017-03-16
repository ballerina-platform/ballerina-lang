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
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.run.configuration.file.GoRunFileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GoRunConfigurationProducerBase<T extends GoRunConfigurationWithMain>
        extends RunConfigurationProducer<T> implements Cloneable {

    protected GoRunConfigurationProducerBase(@NotNull ConfigurationType configurationType) {
        super(configurationType);
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull T configuration, @NotNull ConfigurationContext context,
                                                    Ref<PsiElement> sourceElement) {

        PsiFile file = getFileFromContext(context);
        PsiElement element = sourceElement.get();
        FunctionNode functionNode = PsiTreeUtil.getParentOfType(element, FunctionNode.class);
        ServiceDefinitionNode serviceDefinitionNode = PsiTreeUtil.getParentOfType(element,
                ServiceDefinitionNode.class);
        if (GoRunUtil.hasMainFunction(file) && functionNode != null) {


            configuration.setName(getConfigurationName(file));
            configuration.setFilePath(file.getVirtualFile().getPath());
            Module module = context.getModule();
            if (module != null) {
                configuration.setModule(module);
            }
            if (configuration instanceof GoRunFileConfiguration) {
                configuration.setRunKind(GoRunFileConfiguration.Kind.APPLICATION);

                Project project = context.getProject();
                RunManager runManager = RunManager.getInstance(project);


                RunnerAndConfigurationSettings selectedConfigurationSettings = runManager.getSelectedConfiguration();
                if (selectedConfigurationSettings != null) {

                    RunConfiguration currentRunConfiguration = selectedConfigurationSettings
                            .getConfiguration();
                    ((GoRunFileConfiguration) currentRunConfiguration).setRunKind(GoRunFileConfiguration.Kind
                            .APPLICATION);
                }

                return true;
            }


            //
            //            List<RunnerAndConfigurationSettings> configurationSettings = runManager.getAllSettings();
            //            for (RunnerAndConfigurationSettings configurationSetting : configurationSettings) {
            //                RunConfiguration config = configurationSetting.getConfiguration();
            //                if (config instanceof GoRunMainFileConfiguration) {
            //                    runManager.setSelectedConfiguration(configurationSetting);
            //                    return true;
            //                }
            //            }
            //
            //            //Todo - create a new lightweight config
            //            GoRunMainFileConfigurationType configurationType = GoRunMainFileConfigurationType
            // .getInstance();
            //            ConfigurationFactory[] configurationFactories = configurationType.getConfigurationFactories();
            //            if (configurationFactories.length == 0) {
            //                return false;
            //            }
            //            RunnerAndConfigurationSettings configurationTemplate =
            //                    runManager.getConfigurationTemplate(configurationFactories[0]);
            //            runManager.setSelectedConfiguration(configurationTemplate);
            //            return true;

        }
        if (GoRunUtil.hasServices(file) && serviceDefinitionNode != null) {

            //            if (!(configuration instanceof GoRunServiceFileConfiguration)) {
            //                return false;
            //            }

            configuration.setName(getConfigurationName(file));
            configuration.setFilePath(file.getVirtualFile().getPath());
            Module module = context.getModule();
            if (module != null) {
                configuration.setModule(module);
            }
            if (configuration instanceof GoRunFileConfiguration) {
                configuration.setRunKind(GoRunFileConfiguration.Kind.SERVICE);

                Project project = context.getProject();
                RunManager runManager = RunManager.getInstance(project);
                RunnerAndConfigurationSettings selectedConfigurationSettings = runManager.getSelectedConfiguration();
                if (selectedConfigurationSettings != null) {

                    RunConfiguration currentRunConfiguration = selectedConfigurationSettings
                            .getConfiguration();
                    ((GoRunFileConfiguration) currentRunConfiguration).setRunKind(GoRunFileConfiguration.Kind
                            .SERVICE);
                }

                return true;
            }

            //            if (configuration instanceof GoRunServiceFileConfiguration) {
            //                return true;
            //            }


            //            Project project = context.getProject();
            //            RunManager runManager = RunManager.getInstance(project);
            //
            //            RunnerAndConfigurationSettings selectedConfiguration = runManager.getSelectedConfiguration();
            //            if (selectedConfiguration != null) {
            //                RunConfiguration selectedRunConfiguration = selectedConfiguration.getConfiguration();
            //                if (selectedRunConfiguration instanceof GoRunServiceFileConfiguration) {
            //                    return true;
            //                }
            //            }
            //            List<RunnerAndConfigurationSettings> configurationSettings = runManager.getAllSettings();
            //            for (RunnerAndConfigurationSettings configurationSetting : configurationSettings) {
            //                RunConfiguration config = configurationSetting.getConfiguration();
            //                if (config instanceof GoRunServiceFileConfiguration) {
            //                    runManager.setSelectedConfiguration(configurationSetting);
            //                    return true;
            //                }
            //            }
            //
            //            //Todo - create a new lightweight config
            //            GoRunServiceFileConfigurationType configurationType =
            //                    GoRunServiceFileConfigurationType.getInstance();
            //            ConfigurationFactory[] configurationFactories = configurationType
            //                    .getConfigurationFactories();
            //            if (configurationFactories.length == 0) {
            //                return false;
            //            }
            //            RunnerAndConfigurationSettings configurationTemplate =
            //                    runManager.getConfigurationTemplate(configurationFactories[0]);
            //            runManager.setSelectedConfiguration(configurationTemplate);
            //            return true;
        }


        //    }


        //        PsiFile file = getFileFromContext(context);
        //        if (configuration instanceof GoRunMainFileConfiguration) {
        //            PsiElement element = sourceElement.get();
        //            FunctionNode functionNode = PsiTreeUtil.getParentOfType(element, FunctionNode.class);
        //            if (GoRunUtil.hasMainFunction(file) && functionNode != null) {
        //                configuration.setName(getConfigurationName(file));
        //                configuration.setFilePath(file.getVirtualFile().getPath());
        //                Module module = context.getModule();
        //                if (module != null) {
        //                    configuration.setModule(module);
        //                }
        //
        //                //                Project project = context.getProject();
        //                //                RunManager runManager = RunManager.getInstance(project);
        //                //                RunnerAndConfigurationSettings selectedConfigurationSettings = runManager
        //                // .getSelectedConfiguration();
        //                //                if (selectedConfigurationSettings != null) {
        //                //                    RunConfiguration currentRunConfiguration = selectedConfigurationSettings
        //                // .getConfiguration();
        //                //                    if (!(currentRunConfiguration instanceof GoRunMainFileConfiguration)) {
        //                //
        //                //                        List<RunnerAndConfigurationSettings> configurationSettings =
        // runManager
        //                // .getAllSettings();
        //                //                        for (RunnerAndConfigurationSettings configurationSetting :
        //                // configurationSettings) {
        //                //                            RunConfiguration config = configurationSetting
        // .getConfiguration();
        //                //                            if (config instanceof GoRunMainFileConfiguration) {
        //                //                                runManager.setSelectedConfiguration(configurationSetting);
        //                //                                return true;
        //                //                            }
        //                //                        }
        //                //
        //                //
        //                //                        //                        List<RunConfigurationProducer<?>>
        // producers =
        //                //                        // GoRunMainFileConfigurationProducer.getProducers
        //                //                        //                                (project);
        //                //                        //
        //                //                        //                        RunConfigurationProducer<?>
        //                // runConfigurationProducer = producers
        //                //                        // .get(0);
        //                //                        //                        RunnerAndConfigurationSettings
        //                // existingConfiguration =
        //                //                        // runConfigurationProducer
        //                //                        //                                .findExistingConfiguration
        // (context);
        //                //
        //                //
        //                //
        //                //                        //Todo - create a new lightweight config
        //                //                        GoRunMainFileConfigurationType configurationType =
        //                // GoRunMainFileConfigurationType.getInstance();
        //                //                        ConfigurationFactory[] configurationFactories = configurationType
        //                // .getConfigurationFactories();
        //                //                        if (configurationFactories.length == 0) {
        //                //                            return false;
        //                //                        }
        //                //                        RunnerAndConfigurationSettings configurationTemplate =
        //                //                                runManager.getConfigurationTemplate
        // (configurationFactories[0]);
        //                //                        runManager.setSelectedConfiguration(configurationTemplate);
        //                //                        return true;
        //                //                    }
        //                //                }
        //
        //
        //                return true;
        //            }
        //        } else if (configuration instanceof GoRunServiceFileConfiguration) {
        //            PsiElement element = sourceElement.get();
        //            ServiceDefinitionNode serviceDefinitionNode = PsiTreeUtil.getParentOfType(element,
        //                    ServiceDefinitionNode.class);
        //            if (GoRunUtil.hasServices(file) && serviceDefinitionNode != null) {
        //                configuration.setName(getConfigurationName(file));
        //                configuration.setFilePath(file.getVirtualFile().getPath());
        //                Module module = context.getModule();
        //                if (module != null) {
        //                    configuration.setModule(module);
        //                }
        //
        //                //                Project project = context.getProject();
        //                //                RunManager runManager = RunManager.getInstance(project);
        //                //                RunnerAndConfigurationSettings selectedConfiguration = runManager
        //                // .getSelectedConfiguration();
        //                //                if (selectedConfiguration != null) {
        //                //                    RunConfiguration selectedRunConfiguration = selectedConfiguration
        //                // .getConfiguration();
        //                //                    if (!(selectedRunConfiguration instanceof
        // GoRunServiceFileConfiguration)) {
        //                //
        //                //                        List<RunnerAndConfigurationSettings> configurationSettings =
        // runManager
        //                // .getAllSettings();
        //                //                        for (RunnerAndConfigurationSettings configurationSetting :
        //                // configurationSettings) {
        //                //                            RunConfiguration config = configurationSetting
        // .getConfiguration();
        //                //                            if (config instanceof GoRunServiceFileConfiguration) {
        //                //                                runManager.setSelectedConfiguration(configurationSetting);
        //                //                                return true;
        //                //                            }
        //                //                        }
        //                //
        //                //                        //Todo - create a new lightweight config
        //                //                        GoRunServiceFileConfigurationType configurationType =
        //                //                                GoRunServiceFileConfigurationType.getInstance();
        //                //                        ConfigurationFactory[] configurationFactories = configurationType
        //                // .getConfigurationFactories();
        //                //                        if (configurationFactories.length == 0) {
        //                //                            return false;
        //                //                        }
        //                //                        RunnerAndConfigurationSettings configurationTemplate =
        //                //                                runManager.getConfigurationTemplate
        // (configurationFactories[0]);
        //                //                        runManager.setSelectedConfiguration(configurationTemplate);
        //                //                        return true;
        //                //                    }
        //                //                }
        //                return true;
        //            }
        //        }

        //        ApplicationManager.getApplication().invokeLater(FileContentUtil::reparseOpenedFiles);
        //        PsiFile file = getFileFromContext(context);
        //        if (GoRunUtil.isMainGoFile(file)) {
        //            configuration.setName(getConfigurationName(file));
        //            configuration.setFilePath(file.getVirtualFile().getPath());
        //            Module module = context.getModule();
        //            if (module != null) {
        //                configuration.setModule(module);
        //            }
        //            return true;
        //        }
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
        PsiElement contextElement = GoRunUtil.getContextElement(context);
        PsiFile psiFile = contextElement != null ? contextElement.getContainingFile() : null;
        return psiFile instanceof BallerinaFile ? (BallerinaFile) psiFile : null;
    }
}

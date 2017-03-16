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
import org.ballerinalang.plugins.idea.run.configuration.file.BallerinaRunFileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BallerinaRunConfigurationProducerBase<T extends BallerinaRunConfigurationWithMain>
        extends RunConfigurationProducer<T> implements Cloneable {

    protected BallerinaRunConfigurationProducerBase(@NotNull ConfigurationType configurationType) {
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
        if (BallerinaRunUtil.hasMainFunction(file) && functionNode != null) {


            configuration.setName(getConfigurationName(file));
            configuration.setFilePath(file.getVirtualFile().getPath());
            Module module = context.getModule();
            if (module != null) {
                configuration.setModule(module);
            }
            if (configuration instanceof BallerinaRunFileConfiguration) {
                configuration.setRunKind(BallerinaRunFileConfiguration.Kind.APPLICATION);

                Project project = context.getProject();
                RunManager runManager = RunManager.getInstance(project);


                RunnerAndConfigurationSettings selectedConfigurationSettings = runManager.getSelectedConfiguration();
                if (selectedConfigurationSettings != null) {

                    RunConfiguration currentRunConfiguration = selectedConfigurationSettings
                            .getConfiguration();
                    ((BallerinaRunFileConfiguration) currentRunConfiguration).setRunKind
                            (BallerinaRunFileConfiguration.Kind.APPLICATION);
                }

                return true;
            }
        }
        if (BallerinaRunUtil.hasServices(file) && serviceDefinitionNode != null) {
            configuration.setName(getConfigurationName(file));
            configuration.setFilePath(file.getVirtualFile().getPath());
            Module module = context.getModule();
            if (module != null) {
                configuration.setModule(module);
            }
            if (configuration instanceof BallerinaRunFileConfiguration) {
                configuration.setRunKind(BallerinaRunFileConfiguration.Kind.SERVICE);

                Project project = context.getProject();
                RunManager runManager = RunManager.getInstance(project);
                RunnerAndConfigurationSettings selectedConfigurationSettings = runManager.getSelectedConfiguration();
                if (selectedConfigurationSettings != null) {

                    RunConfiguration currentRunConfiguration = selectedConfigurationSettings
                            .getConfiguration();
                    ((BallerinaRunFileConfiguration) currentRunConfiguration).setRunKind
                            (BallerinaRunFileConfiguration.Kind
                                    .SERVICE);
                }

                return true;
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

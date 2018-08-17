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

package io.ballerina.plugins.idea;

import com.intellij.compiler.CompilerWorkspaceConfiguration;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.ide.util.projectWizard.SourcePathsBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.ContainerUtil;
import io.ballerina.plugins.idea.sdk.BallerinaSdkType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Responsible for creating a Ballerina module.
 */
public class BallerinaModuleBuilder extends JavaModuleBuilder implements SourcePathsBuilder, ModuleBuilderListener {

    private static final Logger LOG = Logger.getInstance(BallerinaModuleBuilder.class);

    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        addListener(this);
        super.setupRootModel(modifiableRootModel);
    }

    // Note - Removing this override will create src directory in the project root.
    public List<Pair<String, String>> getSourcePaths() {
        String ballerinaCacheRoot = getContentEntryPath() + File.separator + ".ballerina";
        new File(ballerinaCacheRoot).mkdirs();
        String ballerinaTomlFile = getContentEntryPath() + File.separator +
                BallerinaConstants.BALLERINA_CONFIG_FILE_NAME;
        File file = new File(ballerinaTomlFile);
        try {
            file.createNewFile();
            // Todo - Add some content to the toml file?
        } catch (IOException e) {
            LOG.debug(e);
        }
        return ContainerUtil.emptyList();
    }

    @NotNull
    @Override
    public ModuleType getModuleType() {
        return BallerinaModuleType.getInstance();
    }

    @Override
    public boolean isSuitableSdkType(SdkTypeId sdkType) {
        return sdkType == BallerinaSdkType.getInstance();
    }

    @Override
    public void moduleCreated(@NotNull Module module) {
        CompilerWorkspaceConfiguration.getInstance(module.getProject()).CLEAR_OUTPUT_DIRECTORY = false;
    }
}

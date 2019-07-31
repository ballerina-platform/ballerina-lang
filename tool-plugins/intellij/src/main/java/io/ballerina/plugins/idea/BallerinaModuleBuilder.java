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

import com.google.common.base.Strings;
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
import io.ballerina.plugins.idea.sdk.BallerinaSdkService;
import io.ballerina.plugins.idea.sdk.BallerinaSdkType;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Responsible for creating a Ballerina module.
 */
public class BallerinaModuleBuilder extends JavaModuleBuilder implements SourcePathsBuilder, ModuleBuilderListener {

    private static final Logger LOG = Logger.getInstance(BallerinaModuleBuilder.class);
    private ModifiableRootModel rootModel;

    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        this.rootModel = modifiableRootModel;
        addListener(this);
        super.setupRootModel(modifiableRootModel);
    }

    @Override
    public List<Pair<String, String>> getSourcePaths() {

        String ballerinaSdkPath = BallerinaSdkService.getInstance(rootModel.getProject()).
                getSdkHomePath(rootModel.getModule());

        if (Strings.isNullOrEmpty(ballerinaSdkPath)) {
            LOG.info("Ballerina SDK is not found. Trying to auto detect ballerina distribution to init" +
                    " the ballerina project");
            ballerinaSdkPath = BallerinaSdkUtils.autoDetectSdk(rootModel.getProject());
        }

        if (!ballerinaSdkPath.isEmpty()) {
            LOG.info("Initiating ballerina project using ballerina init command");
            try {
                String command = String.format("%s%s%s init", ballerinaSdkPath, File.separator,
                        BallerinaConstants.BALLERINA_EXEC_PATH);
                File dir = new File(Objects.requireNonNull(getContentEntryPath()));
                Runtime.getRuntime().exec(command, null, dir);
            } catch (IOException e) {
                LOG.warn("Failed to execute ballerina init command due to:", e);
                addBallerinaProjectArtifacts();
            }
        } else {
            addBallerinaProjectArtifacts();
        }
        return ContainerUtil.emptyList();
    }

    private void addBallerinaProjectArtifacts() {
        try {
            LOG.info("Initiating ballerina project by adding required project artifacts manually");

            // Creates "src" dir.
            String ballerinaSrcDir = getContentEntryPath() + File.separator + "src";
            new File(ballerinaSrcDir).mkdirs();

            // Creates .toml file.
            String ballerinaTomlFile = getContentEntryPath() + File.separator +
                    BallerinaConstants.BALLERINA_CONFIG_FILE_NAME;
            File file = new File(ballerinaTomlFile);
            file.createNewFile();
        } catch (Exception e) {
            LOG.warn("Error occurred when initiating ballerina project due to:", e);
        }
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

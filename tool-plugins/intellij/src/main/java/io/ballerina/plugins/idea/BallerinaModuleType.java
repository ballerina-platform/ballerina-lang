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

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectJdkForModuleStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import icons.BallerinaIcons;
import io.ballerina.plugins.idea.sdk.BallerinaSdkType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * Represents a Ballerina module.
 */
public class BallerinaModuleType extends ModuleType<BallerinaModuleBuilder> {

    public BallerinaModuleType() {
        super(BallerinaConstants.MODULE_TYPE_ID);
    }

    @NotNull
    public static BallerinaModuleType getInstance() {
        return (BallerinaModuleType) ModuleTypeManager.getInstance().findByID(BallerinaConstants.MODULE_TYPE_ID);
    }

    @NotNull
    @Override
    public BallerinaModuleBuilder createModuleBuilder() {
        return new BallerinaModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return "Ballerina Module";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Ballerina modules are used for developing <b>Ballerina</b> applications.";
    }

    @Nullable
    public Icon getBigIcon() {
        return BallerinaIcons.ICON;
    }

    @Nullable
    @Override
    public Icon getNodeIcon(boolean isOpened) {
        return BallerinaIcons.ICON;
    }

    @NotNull
    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext,
                                                @NotNull BallerinaModuleBuilder moduleBuilder,
                                                @NotNull ModulesProvider modulesProvider) {
        return new ModuleWizardStep[]{new ProjectJdkForModuleStep(wizardContext, BallerinaSdkType.getInstance()) {
            @Override
            public void updateDataModel() {
                super.updateDataModel();
                moduleBuilder.setModuleJdk(getJdk());
            }
        }};
    }
}

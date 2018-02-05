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

package org.ballerinalang.plugins.idea.configuration;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurableBase;
import org.ballerinalang.plugins.idea.project.BallerinaModuleSettings;
import org.jetbrains.annotations.NotNull;

public class BallerinaModuleSettingsConfigurable extends
        ConfigurableBase<BallerinaModuleSettingsUI, BallerinaModuleSettings> {

    @NotNull
    private final Module myModule;
    private final boolean myDialogMode;

    public BallerinaModuleSettingsConfigurable(@NotNull Module module, boolean dialogMode) {
        super("Ballerina.project.settings", "Ballerina Project Settings", null);
        myModule = module;
        myDialogMode = dialogMode;
    }

    @NotNull
    @Override
    protected BallerinaModuleSettings getSettings() {
        return BallerinaModuleSettings.getInstance(myModule);
    }

    @Override
    protected BallerinaModuleSettingsUI createUi() {
        return new BallerinaModuleSettingsUI(myModule, myDialogMode);
    }
}

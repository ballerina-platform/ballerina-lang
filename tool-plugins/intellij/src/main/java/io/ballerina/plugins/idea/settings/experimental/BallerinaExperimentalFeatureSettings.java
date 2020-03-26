/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.settings.experimental;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.Nullable;

/**
 * Settings for ballerina experimental features.
 */
@State(name = "BallerinaAllowExperimental",
        storages = @Storage(value = "editor.allow.experimental.xml"))
public class BallerinaExperimentalFeatureSettings implements PersistentStateComponent
        <BallerinaExperimentalFeatureSettings> {

    @Attribute
    private boolean allowExperimental = false;

    public static BallerinaExperimentalFeatureSettings getInstance(Project project) {
        return ServiceManager.getService(project, BallerinaExperimentalFeatureSettings.class);
    }

    @Nullable
    @Override
    public BallerinaExperimentalFeatureSettings getState() {
        return this;
    }

    @Override
    public void loadState(BallerinaExperimentalFeatureSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public boolean isAllowedExperimental() {
        return allowExperimental;
    }

    public void setAllowExperimental(boolean allowExperimental) {
        this.allowExperimental = allowExperimental;
    }
}

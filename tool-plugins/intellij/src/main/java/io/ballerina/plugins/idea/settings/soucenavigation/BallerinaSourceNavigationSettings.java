/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.settings.soucenavigation;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.Nullable;

/**
 * Settings for ballerina source navigation support.
 */
@State(name = "BallerinaSourceNavigation",
        storages = @Storage(value = "editor.source.navigation.xml"))
public class BallerinaSourceNavigationSettings implements PersistentStateComponent
        <BallerinaSourceNavigationSettings> {

    @Attribute
    private boolean enableStdlibGotoDef = true;

    public static BallerinaSourceNavigationSettings getInstance(Project project) {
        return ServiceManager.getService(project, BallerinaSourceNavigationSettings.class);
    }

    @Nullable
    @Override
    public BallerinaSourceNavigationSettings getState() {
        return this;
    }

    @Override
    public void loadState(BallerinaSourceNavigationSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
    public boolean isEnableStdlibGotoDef() {
        return enableStdlibGotoDef;
    }
    public void setEnableStdlibGotoDef(boolean enableStdlibGotoDef) {
        this.enableStdlibGotoDef = enableStdlibGotoDef;
    }
}

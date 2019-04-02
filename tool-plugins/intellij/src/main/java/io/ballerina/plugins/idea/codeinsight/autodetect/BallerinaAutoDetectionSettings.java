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

package io.ballerina.plugins.idea.codeinsight.autodetect;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.Nullable;

/**
 * Language server auto detection settings provider.
 */
@State(name = "BallerinaLangServerAutoDetection",
       storages = @Storage(value = "editor.detect.langserver.xml"))
public class BallerinaAutoDetectionSettings implements PersistentStateComponent<BallerinaAutoDetectionSettings> {

    @Attribute
    private boolean autoDetectBalHome = true;

    public static BallerinaAutoDetectionSettings getInstance() {
        return ServiceManager.getService(BallerinaAutoDetectionSettings.class);
    }

    @Nullable
    @Override
    public BallerinaAutoDetectionSettings getState() {
        return this;
    }

    @Override
    public void loadState(BallerinaAutoDetectionSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public boolean autoDetectBalHome() {
        return autoDetectBalHome;
    }

    public void setAutoDetectBalHome(boolean autoDetectBalHome) {
        this.autoDetectBalHome = autoDetectBalHome;
    }
}

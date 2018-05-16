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
 *
 */

package org.ballerinalang.plugins.idea.codeinsight.semanticanalyzer;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Semantic analyzer settings provider.
 */
@State(
        name = "BallerinaSemanticAnalyzer",
        storages = @Storage(file = StoragePathMacros.APP_CONFIG + "/editor.semanticAnalyzer.xml")
)
public class BallerinaSemanticAnalyzerSettings implements PersistentStateComponent<BallerinaSemanticAnalyzerSettings> {

    private boolean myUseSemanticAnalyzer = true;

    public static BallerinaSemanticAnalyzerSettings getInstance() {
        return ServiceManager.getService(BallerinaSemanticAnalyzerSettings.class);
    }

    @Nullable
    @Override
    public BallerinaSemanticAnalyzerSettings getState() {
        return this;
    }

    @Override
    public void loadState(BallerinaSemanticAnalyzerSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public boolean useSemanticAnalyzer() {
        return myUseSemanticAnalyzer;
    }

    public void setUseSemanticAnalyzer(boolean useSemanticAnalyzer) {
        myUseSemanticAnalyzer = useSemanticAnalyzer;
    }
}

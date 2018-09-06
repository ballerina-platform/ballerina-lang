/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.languageserver;

import com.github.gtache.lsp.client.languageserver.serverdefinition.LanguageServerDefinition$;
import com.github.gtache.lsp.client.languageserver.serverdefinition.RawCommandServerDefinition;
import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.RootProvider;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BallerinaPreloadingActivity extends PreloadingActivity {

    @Override
    public void preload(@NotNull ProgressIndicator indicator) {
        Sdk mockSdk = new Sdk() {
            @NotNull
            @Override
            public SdkTypeId getSdkType() {
                return null;
            }

            @NotNull
            @Override
            public String getName() {
                return null;
            }

            @Nullable
            @Override
            public String getVersionString() {
                return null;
            }

            @Nullable
            @Override
            public String getHomePath() {
                return "/usr/lib/ballerina/ballerina-0.981.0";
            }

            @Nullable
            @Override
            public VirtualFile getHomeDirectory() {
                return null;
            }

            @NotNull
            @Override
            public RootProvider getRootProvider() {
                return null;
            }

            @NotNull
            @Override
            public SdkModificator getSdkModificator() {
                return null;
            }

            @Nullable
            @Override
            public SdkAdditionalData getSdkAdditionalData() {
                return null;
            }

            @Nullable
            @Override
            public <T> T getUserData(@NotNull Key<T> key) {
                return null;
            }

            @Override
            public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

            }

            @NotNull
            @Override
            public Object clone() {
                return null;
            }
        };

        BallerinaLanguageServerDefinition langServerDefinition = new BallerinaLanguageServerDefinition(mockSdk);
        String[] command = { langServerDefinition.getInitCommand(false, false) };
        LanguageServerDefinition$.MODULE$.register(new RawCommandServerDefinition("bal", command));
    }
}

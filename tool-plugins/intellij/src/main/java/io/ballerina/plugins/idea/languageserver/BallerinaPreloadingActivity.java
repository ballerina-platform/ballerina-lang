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
import org.jetbrains.annotations.NotNull;

public class BallerinaPreloadingActivity extends PreloadingActivity {
    @Override
    public void preload(@NotNull ProgressIndicator indicator) {
        // BallerinaLanguageServerDefinition langServerDefinition = new BallerinaLanguageServerDefinition(sdkPath);
        // String[] command = { langServerDefinition.getInitCommand(false, false) };
        String[] command = {"/home/nino/Desktop/ls-launcher/launcher.sh"};
        LanguageServerDefinition$.MODULE$.register(new RawCommandServerDefinition("bal", command));
    }
}

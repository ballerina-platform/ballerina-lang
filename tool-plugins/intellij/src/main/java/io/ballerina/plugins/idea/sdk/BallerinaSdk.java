/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.plugins.idea.sdk;


import org.jetbrains.annotations.Nullable;

public class BallerinaSdk {

    @Nullable
    private String sdkPath;
    private boolean langServerSupport;
    private boolean webviewSupport;

    public BallerinaSdk(@Nullable String sdkPath, boolean langServerSupport, boolean webviewSupport) {
        this.sdkPath = sdkPath;
        this.langServerSupport = langServerSupport;
        this.webviewSupport = webviewSupport;
    }

    public boolean hasLangServerSupport() {
        return langServerSupport;
    }

    public boolean hasWebviewSupport() {
        return webviewSupport;
    }

    @Nullable
    public String getSdkPath() {
        //If the project does not have a ballerina SDK attached, ballerinaSdkPath will be null.
        return sdkPath;
    }
}

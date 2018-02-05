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

package org.ballerinalang;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.ballerinalang.plugins.idea.BallerinaModuleType;
import org.ballerinalang.plugins.idea.project.BallerinaApplicationLibrariesService;
import org.ballerinalang.plugins.idea.sdk.BallerinaSdkType;
import org.jetbrains.annotations.NotNull;

import java.io.File;

abstract public class BallerinaCodeInsightFixtureTestCase extends LightPlatformCodeInsightFixtureTestCase {

    protected static String getTestDataPath(String path) {
        return "src/test/resources/testData/" + path;
    }

    @NotNull
    private static DefaultLightProjectDescriptor createMockProjectDescriptor() {
        return new DefaultLightProjectDescriptor() {

            @NotNull
            @Override
            public Sdk getSdk() {
                return createMockSdk("0.95.4");
            }

            @NotNull
            @Override
            public ModuleType getModuleType() {
                return BallerinaModuleType.getInstance();
            }
        };
    }

    protected void setUpProjectSdk() {
        ApplicationManager.getApplication().runWriteAction(() -> {
            Sdk sdk = getProjectDescriptor().getSdk();
            ProjectJdkTable.getInstance().addJdk(sdk);
            ProjectRootManager.getInstance(myFixture.getProject()).setProjectSdk(sdk);
        });
    }

    @NotNull
    private static Sdk createMockSdk(@NotNull String version) {
        String homePath = new File(getTestDataPath("mockSdk-") + version + "/").getAbsolutePath();
        BallerinaSdkType sdkType = BallerinaSdkType.getInstance();
        ProjectJdkImpl sdk = new ProjectJdkImpl("Ballerina " + version, sdkType, homePath, version);
        sdkType.setupSdkPaths(sdk);
        sdk.setVersionString(version);
        return sdk;
    }

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return isSdkAware() ? createMockProjectDescriptor() : null;
    }

    protected boolean isSdkAware() {
        return annotatedWith(BallerinaSDKAware.class);
    }
}
